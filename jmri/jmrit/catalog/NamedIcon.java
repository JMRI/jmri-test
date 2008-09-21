package jmri.jmrit.catalog;

import java.awt.Component;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.image.ColorModel;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.net.URL;

import javax.swing.ImageIcon;

/**
 * Extend an ImageIcon to remember the name from which it was created
 * and provide rotation services.
 *<p>
 * We store both a "URL" for finding the file this was made from
 * (so we can load this later), plus a shorter "name" for display.
 * <p>
 * These can be persisted by storing their name and rotation
 *
 * @see jmri.jmrit.display.configurexml.PositionableLabelXml
 * @author Bob Jacobsen  Copyright 2002, 2008
 * @version $Revision: 1.9 $
 */

public class NamedIcon extends ImageIcon {

    /**
     * Create a NamedIcon that is a complete copy 
     * of an existing NamedIcon
     * @param pOld Object to copy
     */
    public NamedIcon(NamedIcon pOld) {
        this(pOld.mURL, pOld.mName);
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
        super(pUrl);
        mDefaultImage = getImage();
        if (mDefaultImage == null) log.warn("Could not load image from "+pUrl);
        mName = pName;
        mURL = pUrl;
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
     * Return the human-readable name of this icon
     */
    public String getName() { return mName; }

    /**
     * Return the 0-3 number of 90-degree rotations needed to
     * properly display this icon
     */
    public int getRotation() { return mRotation; }
    
    /**
     * Set the 0-3 number of 90-degree rotations needed to properly
     * display this icon
     */
    public void setRotation(int pRotation, Component pComponent) {
        if (pRotation>3) pRotation = 0;
        if (pRotation<0) pRotation = 3;
        mRotation = pRotation;
        setImage(createRotatedImage(mDefaultImage, pComponent, mRotation));
    }

    private String mName=null;
    private String mURL=null;
    private Image mDefaultImage;

    /**
     * Valid values are <UL>
     * <LI>0 - no rotation
     * <LI>1 - 90 degrees counter-clockwise
     * <LI>2 - 180 degress counter-clockwise
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

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NamedIcon.class.getName());

}