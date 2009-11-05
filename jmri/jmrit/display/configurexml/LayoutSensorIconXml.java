package jmri.jmrit.display.configurexml;

import jmri.configurexml.XmlAdapter;

import org.jdom.Element;

/**
 * Dummy class, just present so files that refer to this 
 * class (e.g. pre JMRI 2.7.8 files) can still be read by
 * deferring to the present class.
 *
 * @author David Duchamp Copyright (c) 2007
 * @author Kevin Dickerson, Deprecated
 * @version $Revision: 1.11 $
 * @deprecated 2.7.8
 */
 
 @Deprecated
public class LayoutSensorIconXml implements XmlAdapter {

    public LayoutSensorIconXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * LayoutSensorIcon
     * @param o Object to store, of type LayoutSensorIcon
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        SensorIconXml tmp = new SensorIconXml();
        return tmp.store(o);
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
        SensorIconXml tmp = new SensorIconXml();
        tmp.load(element, o);

    }
    
    

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LayoutSensorIconXml.class.getName());

}