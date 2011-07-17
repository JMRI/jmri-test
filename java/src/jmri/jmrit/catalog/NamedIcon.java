package jmri.jmrit.catalog;

import java.awt.Component;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.image.ColorModel;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.net.URL;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.RenderingHints;
import java.awt.Graphics2D;

import javax.swing.ImageIcon;

/**
 * Extend an ImageIcon to remember the name from which it was created
 * and provide rotation & scaling services.
 *<p>
 * We store both a "URL" for finding the file this was made from
 * (so we can load this later), plus a shorter "name" for display.
 * <p>
 * These can be persisted by storing their name and rotation
 *
 * @see jmri.jmrit.display.configurexml.PositionableLabelXml
 * @author Bob Jacobsen  Copyright 2002, 2008
 * @author  Pete Cressman Copyright: Copyright (c) 2009, 2010
 * @version $Revision: 1.30 $
 */

public class NamedIcon extends ImageIcon {

    /**
     * Create a NamedIcon that is a complete copy
     * of an existing NamedIcon
     * @param pOld Object to copy
     * i.e. copy of the original icon, but NOT a complete
     * copy of pOld (no transformations done)  
     */
    public NamedIcon(NamedIcon pOld) {
        this(pOld.mURL, pOld.mName);
    }
    
    /**
     * Create a NamedIcon that is really a complete copy 
     * of an existing NamedIcon
     * @param pOld Object to copy
     */
    public NamedIcon(NamedIcon pOld, Component comp) {
        this(pOld.mURL, pOld.mName);
        setLoad(pOld._deg, pOld._scale, comp);
        setRotation(pOld.mRotation, comp);
    }
    
    /**
     * Create a named icon that includes an image 
     * to be loaded from a URL.
     * <p>
     * The default access form is "file:", so a 
     * bare pathname to an icon file will also work
     * for the URL argument
     *
     * @param pUrl URL of image file to load
     * @param pName Human-readable name for the icon
     */
    public NamedIcon(String pUrl, String pName) {
        super(jmri.util.FileUtil.getExternalFilename(pUrl));
    	File fp = new File(jmri.util.FileUtil.getExternalFilename(pUrl));
    	if (!fp.exists()){
    		log.warn("Could not load image from "+pUrl);
    	}
        mDefaultImage = getImage();
        if (mDefaultImage == null) log.warn("Could not load image from "+pUrl);
        mName = pName;
        mURL = jmri.util.FileUtil.getPortableFilename(pUrl);
        mRotation = 0;
    }

    /**
     * Create a named icon that includes an image 
     * to be loaded from a URL.
     *
     * @param pUrl String-form URL of image file to load
     * @param pName Human-readable name for the icon
     */
    public NamedIcon(URL pUrl, String pName) {
        this(pUrl.toString(), pName);
    }

    /**
     * Find the NamedIcon corresponding to a name. Understands the 
     * <a href="http://jmri.org/help/en/html/doc/Technical/FileNames.shtml">standard portable filename prefixes</a>.
     * 
     * @param pName The name string, possibly starting with file: or resource:
     * @return the desired icon with this same pName as its name.
     */
    static public NamedIcon getIconByName(String pName) {
        if (pName == null || pName.length() == 0) {
            return null;
        }
        java.io.File file = new java.io.File(jmri.util.FileUtil.getExternalFilename(pName));
        if (!file.exists()) {
            return null;
        }
        return new NamedIcon(pName, pName);
    }

    /**
     * Return the human-readable name of this icon
     */
    public String getName() { return mName; }

    /**
    * Actually it is mName that is the URL that loads the icon!
    */
    public void setName(String name) { mName = name; }

    /**
     * Return the URL of this icon
     */
    public String getURL() { return mURL; }

    /**
     * Return the 0-3 number of 90-degree rotations needed to
     * properly display this icon
     */
    public int getRotation() { return mRotation; }
    
    /**
     * Set the 0-3 number of 90-degree rotations needed to properly
     * display this icon
     */
    public void setRotation(int pRotation, Component comp) {
    	// don't transform a blinking icon, it will no longer blink!
    	if (pRotation == 0)	
    		return;
        if (pRotation>3) pRotation = 0;
        if (pRotation<0) pRotation = 3;
        mRotation = pRotation;
        setImage(createRotatedImage(mDefaultImage, comp, mRotation));
        _transformR = new AffineTransform();      // each rotation type normalizes the other
        _deg = 0;
        int w = (int)Math.ceil(_scale*getIconWidth());
        int h = (int)Math.ceil(_scale*getIconHeight());
        transformImage(w, h, _transformS, comp);
    }

    private String mName=null;
    private String mURL=null;
    private Image mDefaultImage;

    /**
     * Valid values are <UL>
     * <LI>0 - no rotation
     * <LI>1 - 90 degrees counter-clockwise
     * <LI>2 - 180 degrees counter-clockwise
     * <LI>3 - 270 degrees counter-clockwise
     * </UL>
     */
    int mRotation;

    /**
     * The following was based on a text-rotating applet from
     * David Risner, available at http://www.risner.org/java/rotate_text.html
     * @param pImage Image to transform
     * @param pComponent Component containing the image, needed to obtain
     *                  a MediaTracker to process the image consistently with display
     * @param pRotation 0-3 number of 90-degree rotations needed
     * @return new Image object containing the rotated input image
     */
    public Image createRotatedImage(Image pImage, Component pComponent, int pRotation) {
        if (log.isDebugEnabled()) log.debug("createRotatedImage: pRotation= "+pRotation+
                                       ", mRotation= "+mRotation);
        if (pRotation == 0) return pImage;

        MediaTracker mt = new MediaTracker(pComponent);
        mt.addImage(pImage, 0);
        try {
            mt.waitForAll();
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt(); // retain if needed later
        }

        int w = pImage.getWidth(null);
        int h = pImage.getHeight(null);

        int[] pixels = new int[w*h];
        PixelGrabber pg = new PixelGrabber(pImage, 0, 0, w, h, pixels, 0, w);
        try {
            pg.grabPixels();
        } catch (InterruptedException ie) {}
        int[] newPixels = new int[w*h];

        // transform the pixels
        MemoryImageSource imageSource = null;
        switch (pRotation) {
        case 1:  // 90 degrees
            for (int y=0; y < h; ++y) {
                for (int x=0; x < w; ++x) {
                    newPixels[x*h + y] = pixels[y*w + (w-1-x)];
                }
            }
            imageSource = new MemoryImageSource(h, w,
                ColorModel.getRGBdefault(), newPixels, 0, h);
            break;
        case 2: // 180 degrees
            for (int y=0; y < h; ++y) {
                for (int x=0; x < w; ++x) {
                    newPixels[x*h + y] = pixels[(w-1-x)*h + (h-1-y)];
                }
            }
            imageSource = new MemoryImageSource(w, h,
                ColorModel.getRGBdefault(), newPixels, 0, w);
            break;
        case 3: // 270 degrees
            for (int y=0; y < h; ++y) {
                for (int x=0; x < w; ++x) {
                    newPixels[x*h + y] = pixels[(h-1-y)*w + x];
                }
            }
            imageSource = new MemoryImageSource(h, w,
                ColorModel.getRGBdefault(), newPixels, 0, h);
            break;
        }

        Image myImage = pComponent.createImage(imageSource);
        mt.addImage(myImage, 1);
        try {
            mt.waitForAll();
        } catch (InterruptedException ie) {}
        return myImage;
    }
    private int _deg = 0;
    private double _scale = 1.0;
    private AffineTransform _transformR = new AffineTransform();    // rotations
    private AffineTransform _transformS = new AffineTransform();    // scaling

    public int getDegrees() { return _deg; }
    public double getScale() { return _scale; }

    public void setLoad(int d, double s, Component comp) {
        if (d!=0 || s!=1.0) {
            setImage(createRotatedImage(mDefaultImage, comp, 0));
            //mRotation = 3;
        }
        if (d!=0) {
            rotate(d, comp);
        }
        if (s!=1.0) {
            scale(s, comp);
        }
    }

    public void transformImage(int w, int h, AffineTransform t, Component comp) {
        if (w<=0 || h<=0) {
            if (log.isDebugEnabled()) log.debug("transformImage bad coords "+
                                           ((jmri.jmrit.display.Positionable)comp).getNameString());
            return;
        }
        BufferedImage bufIm = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bufIm.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, 
                             RenderingHints.VALUE_RENDER_QUALITY); 
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                             RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, 
                             RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                             RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.drawImage(getImage(), t, comp);
        setImage(bufIm);
        g2d.dispose();
    }
/*
    void debugDraw(String op, Component c) {
        jmri.jmrit.display.Positionable pos = (jmri.jmrit.display.Positionable)c;
        java.awt.Rectangle r = c.getBounds();
        log.debug(pos.getNameString()+" "+op);
        System.out.println("\tBounds at ("+r.x+", "+r.y+") width= "+r.width+", height= "+r.height); 
        System.out.println("\tLocation at ("+c.getX()+", "+c.getY()+") width= "+
                           c.getWidth()+", height= "+c.getHeight()); 
    }
*/
    /**
    *  Scale as a percentage
    *
    public void scale(int s, Component comp) {
        //log.info("scale= "+s+", "+getDescription());
        if (s<1) { return; }
        scale(s/100.0, comp);
    }
    */

    public void scale(double scale, Component comp) {
//        if (log.isDebugEnabled()) debugDraw("Before scaling of "+scale, comp);
        if (scale==1.0) {
            double w = getIconWidth();
            double h = getIconHeight();
            setImage(mDefaultImage);
            _transformS = new AffineTransform();
            _scale=1.0;
            double rad = _deg*Math.PI/180.0;
            int width = (int)Math.ceil(Math.abs(h*Math.sin(rad)) + Math.abs(w*Math.cos(rad)));
            int heigth = (int)Math.ceil(Math.abs(h*Math.cos(rad)) + Math.abs(w*Math.sin(rad)));
            transformImage(width, heigth, _transformR, comp);
//            if (log.isDebugEnabled()) debugDraw("After scaling _scale= "+_scale, comp);
            return;
        }
        int w = (int)Math.ceil(scale*getIconWidth());
        int h = (int)Math.ceil(scale*getIconHeight());
        AffineTransform t = AffineTransform.getScaleInstance(scale, scale);
        transformImage(w, h, t, comp);
        _transformS.preConcatenate(t);
        _scale *= scale;
//        if (log.isDebugEnabled()) debugDraw("After scaling _scale= "+_scale, comp);
    }
    
    /**
    * Rotate from anchor point (upper left corner) and shift into place
    */
    public void rotate(int deg, Component comp) {
//        if (log.isDebugEnabled()) debugDraw("Before Rotation of "+deg, comp);
        if (deg==0) {
            setImage(mDefaultImage);
            _transformR = new AffineTransform();
            _deg = 0;
            int w = (int)Math.ceil(_scale*getIconWidth());
            int h = (int)Math.ceil(_scale*getIconHeight());
            transformImage(w, h, _transformS, comp);
            return;
        }
        int degree = deg%360;
        if (degree<0){
            degree +=360;
        }
        double rad = degree*Math.PI/180.0;
        double w = getIconWidth();
        double h = getIconHeight();
        int width = (int)Math.ceil(Math.abs(h*Math.sin(rad)) + Math.abs(w*Math.cos(rad)));
        int heigth = (int)Math.ceil(Math.abs(h*Math.cos(rad)) + Math.abs(w*Math.sin(rad)));
        AffineTransform t = null;
        if (0<=degree && degree<90) {
            t = AffineTransform.getTranslateInstance(h*Math.sin(rad), 0.0);
        } else if (90<=degree && degree<180) {
            t = AffineTransform.getTranslateInstance(h*Math.sin(rad)-w*Math.cos(rad), -h*Math.cos(rad));
        } else if (180<=degree && degree<270) {
            t = AffineTransform.getTranslateInstance(-w*Math.cos(rad), -w*Math.sin(rad)-h*Math.cos(rad));
        } else /*if (270<=degree && degree<360)*/ {
            t = AffineTransform.getTranslateInstance(0.0, -w*Math.sin(rad));
        }
        AffineTransform r = AffineTransform.getRotateInstance(rad);
        t.concatenate(r);
        transformImage(width, heigth, t, comp);
        _transformR.preConcatenate(t);
        // convert total angle into degrees only
        _deg += 90*mRotation;
        mRotation=0;
        _deg += deg;
        _deg =_deg%360;
//        if (log.isDebugEnabled()) debugDraw("After Rotation _deg="+_deg, comp);
    }

    /**
    *  If necessary, reduce this image to within 'width' x 'height' dimensions.
    * limit the reduction by 'limit'
    */
    public double reduceTo(int width, int height, double limit) {
        int w = getIconWidth();
        int h = getIconHeight();
        double scale = 1.0;
        if (w > width) {
            scale = ((double)width)/w;
        }
        if (h > height) {
            scale = Math.min(scale, ((double)height)/h);
        }
        if (scale < 1) { // make a thumbnail
            if (limit > 0.0) {
                scale = Math.max(scale, limit);  // but not too small
            }
            AffineTransform t = AffineTransform.getScaleInstance(scale, scale);
            transformImage((int)Math.ceil(scale*w), (int)Math.ceil(scale*h), t, null);
        }
        return scale;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(NamedIcon.class.getName());

}