package jmri.jmrix.oaktree.configurexml;

import org.apache.log4j.Logger;
import org.jdom.Element;
import jmri.jmrix.oaktree.*;

/**
 * Provides load and store functionality for
 * configuring SerialSensorManagers.
 * <P>
 * Uses the store method from the abstract base class, but
 * provides a load method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003, 2006
 * @version $Revision$
 */
public class SerialSensorManagerXml extends jmri.managers.configurexml.AbstractSensorManagerConfigXML {

    public SerialSensorManagerXml() {
        super();
    }

    public void setStoreElementClass(Element sensors) {
        sensors.setAttribute("class","jmri.jmrix.oaktree.configurexml.SerialSensorManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public boolean load(Element sensors) throws jmri.configurexml.JmriConfigureXmlException {
        // create the master object
        SerialSensorManager.instance();
        // load individual sensors
        return loadSensors(sensors);
    }

    static Logger log = Logger.getLogger(SerialSensorManagerXml.class.getName());
}
