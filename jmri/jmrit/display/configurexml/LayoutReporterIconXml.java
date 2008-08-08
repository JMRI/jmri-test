package jmri.jmrit.display.configurexml;

import jmri.configurexml.XmlAdapter;
import jmri.jmrit.display.LayoutEditor;
import jmri.jmrit.display.LayoutReporterIcon;

import org.jdom.Element;

/**
 * Handle configuration for display.LayoutReporterIcon objects.
 *
 * @author Dave Duchamp Copyright: Copyright (c) 2008
 * @version $Revision: 1.1 $
 */
public class LayoutReporterIconXml extends LayoutPositionableLabelXml {

    public LayoutReporterIconXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * LayoutReporterIcon
     * @param o Object to store, of type LayoutReporterIcon
     * @return Element containing the complete info
     */
    public Element store(Object o) {

        LayoutReporterIcon p = (LayoutReporterIcon)o;

        Element element = new Element("reportericon");

        // include contents
        element.setAttribute("reporter", p.getReporter().getSystemName());
        element.setAttribute("x", ""+p.getX());
        element.setAttribute("y", ""+p.getY());
        element.setAttribute("level", String.valueOf(p.getDisplayLevel()));

        storeTextInfo(p, element);
        
        element.setAttribute("class", "jmri.jmrit.display.configurexml.LayoutReporterIconXml");

        return element;
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
        String name;
        LayoutReporterIcon l = new LayoutReporterIcon();

        loadTextInfo(l, element);

        l.setReporter(element.getAttribute("reporter").getValue());

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
        int level = LayoutEditor.LABELS.intValue();
        try {
            level = element.getAttribute("level").getIntValue();
        } catch ( org.jdom.DataConversionException e) {
            log.warn("Could not parse level attribute!");
        } catch ( NullPointerException e) {  // considered normal if the attribute not present
        }
        l.setDisplayLevel(level);

        l.setSize(l.getPreferredSize().width, l.getPreferredSize().height);
        p.putLabel(l);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LayoutReporterIconXml.class.getName());
}