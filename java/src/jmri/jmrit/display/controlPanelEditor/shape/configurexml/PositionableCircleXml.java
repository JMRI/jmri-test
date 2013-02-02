package jmri.jmrit.display.controlPanelEditor.shape.configurexml;

import org.apache.log4j.Logger;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.controlPanelEditor.shape.*;

import org.jdom.Element;

/**
 * Handle configuration for display.PositionableShape objects
 *
 * @author Pete Cressman Copyright: Copyright (c) 2012
 * @version $Revision: 1 $
 */
public class PositionableCircleXml extends PositionableShapeXml {

    public PositionableCircleXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * PositionableShape
     * @param o Object to store, of type PositionableShape
     * @return Element containing the complete info
     */
    public Element store(Object o) {
    	PositionableCircle p = (PositionableCircle)o;

        if (!p.isActive()) return null;  // if flagged as inactive, don't store

        Element element = new Element("positionableCircle");
        storeCommonAttributes(p, element);
        
        Element elem = new Element("size");
        elem.setAttribute("radius", ""+p.getRadius());
        element.addContent(elem);

        element.setAttribute("class", "jmri.jmrit.display.controlPanelEditor.shape.configurexml.PositionableCircleXml");
        return element;
    }
   
    public boolean load(Element element) {
        log.error("Invalid method called");
        return false;
    }

    /**
     * Create a PositionableShape, then add to a target JLayeredPane
     * @param element Top level Element to unpack.
     * @param o  Editor as an Object
     */
    @SuppressWarnings("unchecked")
    public void load(Element element, Object o) {
        // create the objects
        Editor ed = (Editor)o;
        PositionableCircle ps = new PositionableCircle(ed);
    	
		Element elem = element.getChild("size");
        ps.setRadius(getInt(elem, "radius"));
       
        // get object class and determine editor being used
		Editor editor = (Editor)o;
        editor.putItem(ps);
        // load individual item's option settings after editor has set its global settings
        loadCommonAttributes(ps, Editor.MARKERS, element);
    }

    static Logger log = Logger.getLogger(PositionableCircleXml.class.getName());
}
