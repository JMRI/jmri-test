package jmri.jmrit.display.configurexml;

import jmri.configurexml.*;
import jmri.jmrit.catalog.*;
import jmri.jmrit.display.*;
import org.jdom.*;

/**
 * Handle configuration for display.SignalHeadIcon objects
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @version $Revision: 1.5 $
 */
public class SignalHeadIconXml implements XmlAdapter {

    public SignalHeadIconXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * SignalHeadIcon
     * @param o Object to store, of type SignalHeadIcon
     * @return Element containing the complete info
     */
    public Element store(Object o) {

        SignalHeadIcon p = (SignalHeadIcon)o;
        if (!p.isActive()) return null;  // if flagged as inactive, don't store

        Element element = new Element("signalheadicon");

        // include contents
        element.addAttribute("aspectgenerator", p.getAspectGenerator().getSEName());
        element.addAttribute("head", ""+p.getHeadNumber());
        element.addAttribute("x", ""+p.getX());
        element.addAttribute("y", ""+p.getY());
        element.addAttribute("red", p.getRedIcon().getName());
        element.addAttribute("yellow", p.getYellowIcon().getName());
        element.addAttribute("flashyellow", p.getFlashYellowIcon().getName());
        element.addAttribute("green", p.getGreenIcon().getName());
        element.addAttribute("rotate", String.valueOf(p.getGreenIcon().getRotation()));

        element.addAttribute("class", "jmri.jmrit.display.configurexml.SignalHeadIconXml");

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
        String name;

        SignalHeadIcon l = new SignalHeadIcon();
        l.setSignalHead(element.getAttribute("aspectgenerator").getValue(),
                    Integer.parseInt(element.getAttribute("head").getValue())
                    );

        NamedIcon red;
        name = element.getAttribute("red").getValue();
        l.setRedIcon(red = p.catalog.getIconByName(name));

        NamedIcon yellow;
        name = element.getAttribute("yellow").getValue();
        l.setYellowIcon(yellow = p.catalog.getIconByName(name));

        NamedIcon flashyellow;
        name = element.getAttribute("flashyellow").getValue();
        l.setFlashYellowIcon(flashyellow = p.catalog.getIconByName(name));

        NamedIcon green;
        name = element.getAttribute("green").getValue();
        l.setGreenIcon(green = p.catalog.getIconByName(name));

        try {
            Attribute a = element.getAttribute("rotate");
            if (a!=null) {
                int rotation = element.getAttribute("rotate").getIntValue();
                red.setRotation(rotation, l);
                yellow.setRotation(rotation, l);
                flashyellow.setRotation(rotation, l);
                green.setRotation(rotation, l);
            }
        } catch (org.jdom.DataConversionException e) {}

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
        l.setSize(l.getPreferredSize().width, l.getPreferredSize().height);
        p.putSignal(l);

    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SignalHeadIconXml.class.getName());

}