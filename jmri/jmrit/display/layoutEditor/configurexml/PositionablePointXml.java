// PositionablePointXml.java

package jmri.jmrit.display.layoutEditor.configurexml;

import jmri.configurexml.AbstractXmlAdapter;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrit.display.layoutEditor.PositionablePoint;
import org.jdom.Attribute;
import org.jdom.Element;
import java.awt.geom.*;

/**
 * This module handles configuration for display.PositionablePoint objects for a LayoutEditor.
 *
 * @author David Duchamp Copyright (c) 2007
 * @version $Revision: 1.1 $
 */
public class PositionablePointXml extends AbstractXmlAdapter {

    public PositionablePointXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * PositionablePoint
     * @param o Object to store, of type PositionablePoint
     * @return Element containing the complete info
     */
    public Element store(Object o) {

        PositionablePoint p = (PositionablePoint)o;

        Element element = new Element("positionablepoint");

        // include attributes
        element.setAttribute("ident", p.getID());
        element.setAttribute("type", ""+p.getType());
		Point2D coords = p.getCoords();
		element.setAttribute("x", ""+coords.getX());
		element.setAttribute("y", ""+coords.getY());
		if (p.getConnect1() != null) {
			element.setAttribute("connect1name", p.getConnect1().getID());
		}
		if (p.getConnect2() != null) {
			element.setAttribute("connect2name", p.getConnect2().getID());
		}
		if ( (p.getEastBoundSignal()!=null) && (p.getEastBoundSignal().length()>0) ) {
			element.setAttribute("eastboundsignal", p.getEastBoundSignal());
		}
		if ( (p.getWestBoundSignal()!=null) && (p.getWestBoundSignal().length()>0) ) {
			element.setAttribute("westboundsignal", p.getWestBoundSignal());
		}

        element.setAttribute("class", "jmri.jmrit.display.configurexml.PositionablePointXml");
        return element;
    }

    public boolean load(Element element) {
        log.error("Invalid method called");
        return false;
    }

    /**
     * Load, starting with the layoutblock element, then
     * all the value-icon pairs
     * @param element Top level Element to unpack.
     * @param o  LayoutEditor as an Object
     */
    public void load(Element element, Object o) {
        // create the objects
        LayoutEditor p = (LayoutEditor)o;
		
		// get attributes
        String name = element.getAttribute("ident").getValue();
		int type = PositionablePoint.ANCHOR;
		double x = 0.0;
		double y = 0.0;
		try {
			x = element.getAttribute("x").getFloatValue();
			y = element.getAttribute("y").getFloatValue();
			type = element.getAttribute("type").getIntValue();
		} catch (org.jdom.DataConversionException e) {
            log.error("failed to convert positionablepoint attribute");
        }
		
		// create the new PositionablePoint
        PositionablePoint l = new PositionablePoint(name,type,new Point2D.Double(x,y),p);
		
		// get remaining attributes
		Attribute a = element.getAttribute("connect1name");
		if (a != null) {
			l.trackSegment1Name = a.getValue();
		}
		a = element.getAttribute("connect2name");
		if (a != null) {
			l.trackSegment2Name = a.getValue();
		}
		a = element.getAttribute("eastboundsignal");
		if (a != null) {
			l.setEastBoundSignal(a.getValue());
		}
		a = element.getAttribute("westboundsignal");
		if (a != null) {
			l.setWestBoundSignal(a.getValue());
		}
		p.pointList.add(l);
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PositionablePointXml.class.getName());
}