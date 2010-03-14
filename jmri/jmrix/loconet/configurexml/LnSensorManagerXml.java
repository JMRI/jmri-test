package jmri.jmrix.loconet.configurexml;

import jmri.jmrix.loconet.LnSensorManager;

import org.jdom.Element;

/**
 * Provides load and store functionality for
 * configuring LnSensorManagers.
 * <P>
 * Uses the store method from the abstract base class, but
 * provides a load method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @version $Revision: 1.9 $
 */
public class LnSensorManagerXml extends jmri.managers.configurexml.AbstractSensorManagerConfigXML {

    public LnSensorManagerXml() {
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
        loadSensors(sensors);
		return true;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LnSensorManagerXml.class.getName());
}