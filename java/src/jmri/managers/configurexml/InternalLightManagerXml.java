package jmri.managers.configurexml;

import org.jdom.Element;

/**
 * Provides load and store functionality for
 * configuring InternalLightManagers.
 * <P>
 * Uses the store method from the abstract base class, but
 * provides a load method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2009
 * @version $Revision: 1.2 $
 */
public class InternalLightManagerXml extends jmri.managers.configurexml.AbstractLightManagerConfigXML {

    public InternalLightManagerXml() {
        super();
    }

    public void setStoreElementClass(Element lights) {
        lights.setAttribute("class",this.getClass().getName());
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public boolean load(Element lights) {
        // load individual lights
        loadLights(lights);
        return true;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(InternalLightManagerXml.class.getName());
}