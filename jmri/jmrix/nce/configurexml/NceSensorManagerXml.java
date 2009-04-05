package jmri.jmrix.nce.configurexml;

import jmri.jmrix.nce.NceSensorManager;

import org.jdom.Element;

/**
 * Provides load and store functionality for
 * configuring NceSensorManagers.
 * <P>
 * Uses the store method from the abstract base class, but
 * provides a load method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @version $Revision: 1.5 $
 */
public class NceSensorManagerXml extends jmri.configurexml.AbstractSensorManagerConfigXML {

    public NceSensorManagerXml() {
        super();
    }

    public void setStoreElementClass(Element sensors) {
        sensors.setAttribute("class",this.getClass().getName());
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public void load(Element sensors) {
        // create the master object
        NceSensorManager.instance();
        // load individual sensors
        loadSensors(sensors);
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(NceSensorManagerXml.class.getName());
}