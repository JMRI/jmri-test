// XNetLightManagerXml.java

package jmri.jmrix.lenz.configurexml;

import org.jdom.Element;
import jmri.jmrix.lenz.*;

/**
 * Provides load and store functionality for
 * configuring XNetLightManagers.
 * <P>
 * Uses the store method from the abstract base class, but
 * provides a load method here.
 * <P>
 * @author Dave Duchamp Copyright (c) 2006
 * @version $Revision: 1.3 $
 */
public class XNetLightManagerXml extends jmri.managers.configurexml.AbstractLightManagerConfigXML {

    public XNetLightManagerXml() {
        super();
    }

    public void setStoreElementClass(Element lights) {
        lights.setAttribute("class","jmri.jmrix.lenz.configurexml.XNetLightManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public void load(Element lights) {
        // create the master object
        XNetLightManager.instance();
        // load individual lights
        loadLights(lights);
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(XNetLightManagerXml.class.getName());
}
