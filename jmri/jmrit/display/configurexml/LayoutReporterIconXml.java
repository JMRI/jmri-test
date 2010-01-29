package jmri.jmrit.display.configurexml;

import org.jdom.Element;

/**
 * Dummy class, just present so files that refer to this 
 * class (e.g. pre JMRI 2.7.8 files) can still be read by
 * deferring to the present class.
 *
 * Handle configuration for display.LayoutReporterIcon objects.
 *
 * @author Dave Duchamp Copyright: Copyright (c) 2008
 * @author Kevin Dickerson, Deprecated
 * @version $Revision: 1.6 $
 * @deprecated 2.7.8
 */
 
 @Deprecated
public class LayoutReporterIconXml extends PositionableLabelXml {

    public LayoutReporterIconXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * LayoutReporterIcon
     * @param o Object to store, of type LayoutReporterIcon
     * @return Element containing the complete info
     */
    public Element store(Object o) {
    
        ReporterIconXml tmp = new ReporterIconXml();
        return tmp.store(o);
    }


    public boolean load(Element element) {
        log.error("Invalid method called");
        return false;
    }

    /**
     * Create a LayoutPositionableLabel, then add to a target JLayeredPane
     * @param element Top level Element to unpack.
     * @param o  LayoutEditor as an Object
     */
    public void load(Element element, Object o) {
    
        ReporterIconXml tmp = new ReporterIconXml();
        tmp.load(element, o);
        // create the object
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LayoutReporterIconXml.class.getName());
}