package jmri.jmrit.display.configurexml;

import jmri.configurexml.*;
import jmri.jmrit.catalog.*;
import jmri.jmrit.display.*;
import org.jdom.*;

/**
 * Handle configuration for display.PositionableLabel objects
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @version $Revision: 1.7 $
 */
public class PositionableLabelXml implements XmlAdapter {

    public PositionableLabelXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * PositionableLabel
     * @param o Object to store, of type PositionableLabel
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        PositionableLabel p = (PositionableLabel)o;

        if (!p.isActive()) return null;  // if flagged as inactive, don't store

        Element element = new Element("positionablelabel");
        element.addAttribute("class", "jmri.jmrit.display.configurexml.PositionableLabelXml");

        // include contents
        element.addAttribute("x", String.valueOf(p.getX()));
        element.addAttribute("y", String.valueOf(p.getY()));
        if (p.isText() && p.getText()!=null) {
            element.addAttribute("text", p.getText());
            element.addAttribute("size", ""+p.getFont().getSize());
            element.addAttribute("style", ""+p.getFont().getStyle());
        }
        if (p.isIcon() && p.getIcon()!=null) {
            NamedIcon icon = (NamedIcon)p.getIcon();
            element.addAttribute("icon", icon.getName());
            element.addAttribute("rotate", String.valueOf(icon.getRotation()));
        }

        return element;
    }


    public void load(Element element) {
        log.error("Invalid method called");
    }

    /**
     * Create a PositionableLabel, then add to a target JLayeredPane
     * @param element Top level Element to unpack.
     * @param o  PanelEditor as an Object
     */
    public void load(Element element, Object o) {
        // create the objects
        PanelEditor p = (PanelEditor)o;
        PositionableLabel l = null;
        if (element.getAttribute("text")!=null) {
            l = new PositionableLabel(element.getAttribute("text").getValue());
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
            l = new PositionableLabel(icon);
            try {
                Attribute a = element.getAttribute("rotate");
                if (a!=null) {
                    int rotation = element.getAttribute("rotate").getIntValue();
                    icon.setRotation(rotation, l);
                }
            } catch (org.jdom.DataConversionException e) {}
        }
        // find coordinates
        int x = 0;
        int y = 0;
        int height = 10;
        int width = 10;
        try {
            x = element.getAttribute("x").getIntValue();
            y = element.getAttribute("y").getIntValue();
        } catch ( org.jdom.DataConversionException e) {
            log.error("failed to convert PanelEditor's attribute");
        }
        l.setLocation(x,y);
        l.setSize(l.getPreferredSize().width, l.getPreferredSize().height);
        if (element.getAttribute("text")!=null) {
            p.putLabel(l);
        } else if (element.getAttribute("icon")!=null) {
            p.putIcon(l);
        }

    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PanelEditorXml.class.getName());

}