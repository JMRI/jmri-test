package jmri.jmrix.can.cbus.configurexml;

import jmri.jmrix.can.cbus.CbusSensorManager;

import org.jdom.Element;

/**
 * Provides load and store functionality for
 * configuring CbusSensorManagers.
 * <P>
 * Uses the store method from the abstract base class, but
 * provides a load method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2008
 * @version $Revision: 1.4 $
 * @since 2.3.1
 */
public class CbusSensorManagerXml extends jmri.managers.configurexml.AbstractSensorManagerConfigXML {

    public CbusSensorManagerXml() {
        super();
    }

    public void setStoreElementClass(Element sensors) {
        sensors.setAttribute("class",this.getClass().getName());
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public boolean load(Element sensors) {
    	boolean result = true;
        // create the master object
        CbusSensorManager mgr = CbusSensorManager.instance();
        // load individual sensors
        result = loadSensors(sensors);
		// Request the status of these sensors from the layout, if appropriate.
		mgr.updateAll();
		return result;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CbusSensorManagerXml.class.getName());
}