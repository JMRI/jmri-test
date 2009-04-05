package jmri.jmrit.display.configurexml;

import jmri.configurexml.XmlAdapter;
import jmri.jmrit.catalog.CatalogPane;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.LayoutEditor;
import jmri.jmrit.display.LayoutPositionableLabel;
import java.awt.Color;

import org.jdom.Attribute;
import org.jdom.DataConversionException;
import org.jdom.Element;

/**
 * Handle configuration for display.LayoutPositionableLabel objects
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @version $Revision: 1.3 $
 */
public class LayoutPositionableLabelXml implements XmlAdapter {

    public LayoutPositionableLabelXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * LayoutPositionableLabel
     * @param o Object to store, of type LayoutPositionableLabel
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        LayoutPositionableLabel p = (LayoutPositionableLabel)o;

        if (!p.isActive()) return null;  // if flagged as inactive, don't store

        Element element = new Element("positionablelabel");
        element.setAttribute("forcecontroloff", p.getForceControlOff()?"true":"false");
        element.setAttribute("fixed", p.getFixed()?"true":"false");
        element.setAttribute("showtooltip", p.getShowTooltip()?"true":"false");
        element.setAttribute("class", "jmri.jmrit.display.configurexml.LayoutPositionableLabelXml");

        // include contents
        element.setAttribute("x", String.valueOf(p.getX()));
        element.setAttribute("y", String.valueOf(p.getY()));
        element.setAttribute("level", String.valueOf(p.getDisplayLevel()));
        if (p.isText() && p.getText()!=null) {
            element.setAttribute("text", p.getText());
            element.setAttribute("size", ""+p.getFont().getSize());
            element.setAttribute("style", ""+p.getFont().getStyle());
            if (!p.getForeground().equals(Color.black)) {
                element.setAttribute("red", ""+p.getForeground().getRed());
                element.setAttribute("green", ""+p.getForeground().getGreen());
                element.setAttribute("blue", ""+p.getForeground().getBlue());
            }
        }
        if (p.isIcon() && p.getIcon()!=null) {
            NamedIcon icon = (NamedIcon)p.getIcon();
            element.setAttribute("icon", icon.getName());
            element.setAttribute("rotate", String.valueOf(icon.getRotation()));
        }

        return element;
    }

    /**
     * Store the text formatting information.
     * <p>
     * This is always stored, even if the icon isn't in text mode,
     * because some uses (subclasses) of LayoutPositionableLabel flip
     * back and forth between icon and text, and want to remember their
     * formatting.
     */
    protected void storeTextInfo(LayoutPositionableLabel p, Element element) {
        if (p.getText()!=null) element.setAttribute("text", p.getText());
        element.setAttribute("size", ""+p.getFont().getSize());
        element.setAttribute("style", ""+p.getFont().getStyle());
        if (!p.getForeground().equals(Color.black)) {
            element.setAttribute("red", ""+p.getForeground().getRed());
            element.setAttribute("green", ""+p.getForeground().getGreen());
            element.setAttribute("blue", ""+p.getForeground().getBlue());
        }
    }

    public void load(Element element) {
        log.error("Invalid method called");
    }

    /**
     * Create a LayoutPositionableLabel, then add to a target JLayeredPane
     * @param element Top level Element to unpack.
     * @param o  LayoutEditor as an Object
     */
    public void load(Element element, Object o) {
        // create the objects
        LayoutEditor p = (LayoutEditor)o;
        LayoutPositionableLabel l = null;
        if (element.getAttribute("text")!=null) {
            l = new LayoutPositionableLabel(element.getAttribute("text").getValue());
            Attribute a = element.getAttribute("size");
            try {
                if (a!=null) l.setFontSize(a.getFloatValue());
            } catch (DataConversionException ex) {
                log.warn("invalid size attribute value");
            }
            a = element.getAttribute("style");
            try {
                if (a!=null) l.setFontStyle(a.getIntValue(), 0);  // label is created plain, so don't need to drop
            } catch (DataConversionException ex) {
                log.warn("invalid style attribute value");
            }

        } else if (element.getAttribute("icon")!=null) {
            String name = element.getAttribute("icon").getValue();
            NamedIcon icon = CatalogPane.getIconByName(name);
            l = new LayoutPositionableLabel(icon);
            try {
                Attribute a = element.getAttribute("rotate");
                if (a!=null) {
                    int rotation = element.getAttribute("rotate").getIntValue();
                    icon.setRotation(rotation, l);
                }
            } catch (org.jdom.DataConversionException e) {}
        }

        Attribute a = element.getAttribute("forcecontroloff");
        if ( (a!=null) && a.getValue().equals("true"))
            l.setForceControlOff(true);
        else
            l.setForceControlOff(false);
            
        a = element.getAttribute("fixed");
        if ( (a!=null) && a.getValue().equals("true"))
            l.setFixed(true);
        else
            l.setFixed(false);
            
        a = element.getAttribute("showtooltip");
        if ( (a!=null) && a.getValue().equals("false"))
            l.setShowTooltip(false);
        else
            l.setShowTooltip(true);
            
        // find coordinates
        int x = 0;
        int y = 0;
        int height = 10;
        int width = 10;
        try {
            x = element.getAttribute("x").getIntValue();
            y = element.getAttribute("y").getIntValue();
        } catch ( org.jdom.DataConversionException e) {
            log.error("failed to convert LayoutEditor's attribute");
        }
        // find display level
        int level = LayoutEditor.LABELS.intValue();
        if (element.getAttribute("icon")!=null) level = LayoutEditor.ICONS.intValue();
        try {
            level = element.getAttribute("level").getIntValue();
        } catch ( org.jdom.DataConversionException e) {
            log.warn("Could not parse level attribute!");
        } catch ( NullPointerException e) {  // considered normal if the attribute not present
        }
        l.setDisplayLevel(level);

        // set color if needed
        try {
            int red = element.getAttribute("red").getIntValue();
            int blue = element.getAttribute("blue").getIntValue();
            int green = element.getAttribute("green").getIntValue();
            l.setForeground(new Color(red, green, blue));
        } catch ( org.jdom.DataConversionException e) {
            log.warn("Could not parse color attributes!");
        } catch ( NullPointerException e) {  // considered normal if the attributes are not present
        }

        // and activate the result
        l.setLocation(x,y);
        l.setSize(l.getPreferredSize().width, l.getPreferredSize().height);
        p.putLabel(l);
	}
   
    protected void loadTextInfo(LayoutPositionableLabel l, Element element) {
        Attribute a = element.getAttribute("size");
        try {
            if (a!=null) l.setFontSize(a.getFloatValue());
        } catch (DataConversionException ex) {
            log.warn("invalid size attribute value");
        }
        a = element.getAttribute("style");
        try {
            if (a!=null) l.setFontStyle(a.getIntValue(), 0);  // label is created plain, so don't need to drop
        } catch (DataConversionException ex) {
            log.warn("invalid style attribute value");
        }

        // set color if needed
        try {
            int red = element.getAttribute("red").getIntValue();
            int blue = element.getAttribute("blue").getIntValue();
            int green = element.getAttribute("green").getIntValue();
            l.setForeground(new Color(red, green, blue));
        } catch ( org.jdom.DataConversionException e) {
            log.warn("Could not parse color attributes!");
        } catch ( NullPointerException e) {  // considered normal if the attributes are not present
        }
	}

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LayoutPositionableLabelXml.class.getName());

}