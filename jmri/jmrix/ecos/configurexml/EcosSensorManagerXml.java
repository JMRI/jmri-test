package jmri.jmrix.ecos.configurexml;

import org.jdom.Element;
import jmri.jmrix.ecos.EcosSensorManager;

/**
 * Provides load and store functionality for
 * configuring EcosSensorManagers.
 * <P>
 * Uses the store method from the abstract base class, but
 * provides a load method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002, 2008
 * @version $Revision: 1.2 $
 */
public class EcosSensorManagerXml extends jmri.managers.configurexml.AbstractSensorManagerConfigXML {

    public EcosSensorManagerXml() {
        super();
    }

    public void setStoreElementClass(Element sensors) {
        sensors.setAttribute("class","jmri.jmrix.ecos.configurexml.EcosSensorManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public boolean load(Element sensors) throws jmri.configurexml.JmriConfigureXmlException {
        // create the master object
        EcosSensorManager.instance();
        // load individual turnouts
        return loadSensors(sensors);
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EcosSensorManagerXml.class.getName());
}