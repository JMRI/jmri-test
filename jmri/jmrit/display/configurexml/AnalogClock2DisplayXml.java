// AnalogClock2DisplayXml.java

package jmri.jmrit.display.configurexml;

import org.jdom.*;
import jmri.configurexml.*;
import jmri.jmrit.display.*;

/**
 * Handle configuration for display.AnalogClock2Display objects.
 *
 * @author  Howard G. Penny  Copyright (c) 2005
 * @version $Revision: 1.7 $
 */
public class AnalogClock2DisplayXml
    implements XmlAdapter {

    public AnalogClock2DisplayXml() {
    }

    /**
     * Default implementation for storing the contents of an
     * AnalogClock2Display
     * @param o Object to store, of type TurnoutIcon
     * @return Element containing the complete info
     */
    public Element store(Object o) {

        AnalogClock2Display p = (AnalogClock2Display) o;
        if (!p.isActive()) {
            return null; // if flagged as inactive, don't store
        }

        Element element = new Element("fastclock");

        // include contents
        element.setAttribute("x", "" + p.getX());
        element.setAttribute("y", "" + p.getY());
        element.setAttribute("scale", "" + p.getScale());

        element.setAttribute("class",
            "jmri.jmrit.display.configurexml.AnalogClock2DisplayXml");

        return element;
    }

    public boolean load(Element element) {
        log.error("Invalid method called");
        return false;
    }

    /**
     * Create an AnalogClock2Display, then add to a target JLayeredPane
     * @param element Top level Element to unpack.
     * @param o  PanelEditor or LayoutEditor as an Object
     */
	public void load(Element element, Object o) {
		// get object class and create the clock object
		String className = o.getClass().getName();
		int lastDot = className.lastIndexOf(".");
		PanelEditor pe = null;
		LayoutEditor le = null;
		AnalogClock2Display l = null;
		String shortClass = className.substring(lastDot+1,className.length());
		if (shortClass.equals("PanelEditor")) {
			pe = (PanelEditor) o;
			l = new AnalogClock2Display(pe);		
		}
		else if (shortClass.equals("LayoutEditor")) {
			le = (LayoutEditor) o;
			l = new AnalogClock2Display(le);		
		}
		else {
			log.error("Unrecognizable class - "+className);
            return;
		}

        // find coordinates
        int x = 0;
        int y = 0;
        double scale = 1.0;
        try {
            x = element.getAttribute("x").getIntValue();
            y = element.getAttribute("y").getIntValue();
            if (element.getAttribute("scale")!=null) {
                scale = element.getAttribute("scale").getDoubleValue();
            }
        }
        catch (org.jdom.DataConversionException e) {
            log.error("failed to convert positional attribute");
        }
        l.setOpaque(false);
        l.update();
        l.setLocation(x, y);
        if (scale != 1.0 && scale>0.1) { l.setScale(scale);  }
           	
		// add the clock to the panel
		if (pe!=null) {
			int level = PanelEditor.CLOCK.intValue();
			l.setDisplayLevel(level);
			pe.putClock(l);
		}
		else if (le!=null) {
			int level = LayoutEditor.CLOCK.intValue();
			l.setDisplayLevel(level);
			le.putClock(l);
		}
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger
    .getLogger(AnalogClock2DisplayXml.class.getName());
}
