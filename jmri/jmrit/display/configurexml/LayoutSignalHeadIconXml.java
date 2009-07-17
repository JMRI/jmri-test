// LayoutSignalHeadIconXml.java

package jmri.jmrit.display.configurexml;

import jmri.configurexml.XmlAdapter;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.LayoutEditor;
import jmri.jmrit.display.LayoutSignalHeadIcon;
import org.jdom.Attribute;
import org.jdom.Element;

/**
 * Handle configuration for display.LayoutSignalHeadIcon objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @version $Revision: 1.6 $
 */
public class LayoutSignalHeadIconXml implements XmlAdapter {

    public LayoutSignalHeadIconXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * LayoutSignalHeadIcon
     * @param o Object to store, of type LayoutSignalHeadIcon
     * @return Element containing the complete info
     */
    public Element store(Object o) {

        LayoutSignalHeadIcon p = (LayoutSignalHeadIcon)o;
        if (!p.isActive()) return null;  // if flagged as inactive, don't store

        Element element = new Element("signalheadicon");

        // include contents
        element.setAttribute("signalhead", ""+p.getSignalHead().getSystemName());
        element.setAttribute("x", ""+p.getX());
        element.setAttribute("y", ""+p.getY());
        element.setAttribute("level", String.valueOf(p.getDisplayLevel()));
        element.setAttribute("held", p.getHeldIcon().getName());
        element.setAttribute("dark", p.getDarkIcon().getName());
        element.setAttribute("red", p.getRedIcon().getName());
        element.setAttribute("yellow", p.getYellowIcon().getName());
        element.setAttribute("flashyellow", p.getFlashYellowIcon().getName());
        element.setAttribute("green", p.getGreenIcon().getName());
        element.setAttribute("flashred", p.getFlashRedIcon().getName());
        element.setAttribute("flashgreen", p.getFlashGreenIcon().getName());
        element.setAttribute("rotate", String.valueOf(p.getGreenIcon().getRotation()));
        element.setAttribute("forcecontroloff", p.getForceControlOff()?"true":"false");
        element.setAttribute("clickmode", ""+p.getClickMode());
        element.setAttribute("litmode", ""+p.getLitMode());

        element.setAttribute("class", "jmri.jmrit.display.configurexml.LayoutSignalHeadIconXml");

        return element;
    }


    public boolean load(Element element) {
        log.error("Invalid method called");
        return false;
    }

    /**
     * Create a PositionableLabel, then add to a target JLayeredPane
     * @param element Top level Element to unpack.
     * @param o  LayoutEditor as an Object
     */
    public void load(Element element, Object o) {
        // create the objects
        LayoutEditor p = (LayoutEditor)o;
        String name;

        LayoutSignalHeadIcon l = new LayoutSignalHeadIcon();
        // handle old format!
        if (element.getAttribute("signalhead") == null) {
            log.error("incorrect information for signal head; must use signalhead name");
            return;
        }

        l.setSignalHead(element.getAttribute("signalhead").getValue());

        NamedIcon red;
        name = element.getAttribute("red").getValue();
        l.setRedIcon(red = NamedIcon.getIconByName(name));

        NamedIcon yellow;
        name = element.getAttribute("yellow").getValue();
        l.setYellowIcon(yellow = NamedIcon.getIconByName(name));

        NamedIcon green;
        name = element.getAttribute("green").getValue();
        l.setGreenIcon(green = NamedIcon.getIconByName(name));

        Attribute a; 

        NamedIcon held = null;
        a = element.getAttribute("held");
        if (a!=null) 
            l.setHeldIcon(held = NamedIcon.getIconByName(a.getValue()));

        NamedIcon dark = null;
        a = element.getAttribute("dark");
        if (a!=null) 
            l.setDarkIcon(dark = NamedIcon.getIconByName(a.getValue()));

        NamedIcon flashred = null;
        a = element.getAttribute("flashred");
        if (a!=null) 
            l.setFlashRedIcon(flashred = NamedIcon.getIconByName(a.getValue()));

        NamedIcon flashyellow = null;
        a = element.getAttribute("flashyellow");
        if (a!=null) 
            l.setFlashYellowIcon(flashyellow = NamedIcon.getIconByName(a.getValue()));

        NamedIcon flashgreen = null;
        a = element.getAttribute("flashgreen");
        if (a!=null) 
            l.setFlashGreenIcon(flashgreen = NamedIcon.getIconByName(a.getValue()));
        
        try {
            a = element.getAttribute("rotate");
            if (a!=null) {
                int rotation = a.getIntValue();
                red.setRotation(rotation, l);
                yellow.setRotation(rotation, l);
                green.setRotation(rotation, l);
                if (flashred!=null) flashred.setRotation(rotation, l);
                if (flashyellow!=null) flashyellow.setRotation(rotation, l);
                if (flashgreen!=null) flashgreen.setRotation(rotation, l);
                if (dark!=null) dark.setRotation(rotation, l);
				if (held!=null) held.setRotation(rotation, l);
            }
        } catch (org.jdom.DataConversionException e) {}

        try {
            a = element.getAttribute("clickmode");
            if (a!=null) {
                l.setClickMode(a.getIntValue());
            }
        } catch (org.jdom.DataConversionException e) {
            log.error("Failed on clickmode attribute: "+e);
        }

        try {
            a = element.getAttribute("litmode");
            if (a!=null) {
                l.setLitMode(a.getBooleanValue());
            }
        } catch (org.jdom.DataConversionException e) {
            log.error("Failed on litmode attribute: "+e);
        }

        a = element.getAttribute("forcecontroloff");
        if ( (a!=null) && a.getValue().equals("true"))
            l.setForceControlOff(true);
        else
            l.setForceControlOff(false);
            
        l.displayState(l.headState());

        // find coordinates
        int x = 0;
        int y = 0;
        try {
            x = element.getAttribute("x").getIntValue();
            y = element.getAttribute("y").getIntValue();
        } catch ( org.jdom.DataConversionException e) {
            log.error("failed to convert positional attribute");
        }
        l.setLocation(x,y);

        // find display level
        int level = LayoutEditor.SIGNALS.intValue();
        try {
            level = element.getAttribute("level").getIntValue();
        } catch ( org.jdom.DataConversionException e) {
            log.warn("Could not parse level attribute!");
        } catch ( NullPointerException e) {  // considered normal if the attribute not present
        }
        l.setDisplayLevel(level);

        p.putSignal(l);
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LayoutSignalHeadIconXml.class.getName());

}