package jmri.jmrix.rfid.configurexml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdom2.Element;

/**
 * Provides load and store functionality for
 * configuring RfidSensorManagers.
 * <P>
 * Uses the store method from the abstract base class, but
 * provides a load method here.
 *
 * @author      Bob Jacobsen Copyright: Copyright (c) 2003, 2006, 2007, 2008
 * @author      Matthew Harris  Copyright (C) 2011
 * @version     $Revision$
 * @since       2.11.4
 */
public class RfidSensorManagerXml extends jmri.managers.configurexml.AbstractSensorManagerConfigXML {

    public RfidSensorManagerXml() {
        super();
    }

    public void setStoreElementClass(Element sensors) {
        sensors.setAttribute("class",this.getClass().getName());
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public boolean load(Element sensors) throws jmri.configurexml.JmriConfigureXmlException {
        // load individual sensors
        return loadSensors(sensors);
    }

    private static final Logger log = LoggerFactory.getLogger(RfidSensorManagerXml.class.getName());
}
