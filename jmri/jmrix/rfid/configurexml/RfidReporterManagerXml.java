package jmri.jmrix.rfid.configurexml;

import jmri.jmrix.rfid.RfidReporterManager;
import org.jdom.Element;

/**
 * Provides load and store functionality for
 * configuring RfidReporterManagers.
 * <P>
 * Uses the store method from the abstract base class, but
 * provides a load method here.
 *
 * @author      Bob Jacobsen Copyright: Copyright (c) 2008
 * @author      Matthew Harris  Copyright (C) 2011
 * @version     $Revision: 1.1 $
 * @since       2.11.4
 */
public class RfidReporterManagerXml extends jmri.managers.configurexml.AbstractReporterManagerConfigXML {

    public RfidReporterManagerXml() {
        super();
    }

    public void setStoreElementClass(Element sensors) {
        sensors.setAttribute("class",this.getClass().getName());
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public boolean load(Element reporters) {
        // load individual sensors
        return loadReporters(reporters);
    }

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RfidReporterManagerXml.class.getName());
}