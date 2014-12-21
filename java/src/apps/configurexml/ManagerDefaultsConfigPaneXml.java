package apps.configurexml;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.InstanceManager;

import org.jdom2.Element;

/**
 * Handle XML persistence of ManagerDefaultsConfigPane objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2010
 * @version $Revision$
 */
public class ManagerDefaultsConfigPaneXml extends jmri.configurexml.AbstractXmlAdapter {

    public ManagerDefaultsConfigPaneXml() {
    }

    /**
     * Arrange for ManagerDefaultSelector to be stored
     * @param o Object to store, ignored
     * @return null after others arranged
     */
    public Element store(Object o) {
        InstanceManager.configureManagerInstance().registerPref(jmri.managers.ManagerDefaultSelector.instance);
        return null;
    }

    /**
     * Create object from XML file
     * @param e Top level Element to unpack.
      */
    public boolean load(Element e) {
        log.error("load(Element) should not have been invoked");
        return false;
    }

    /**
     * Update static data from XML file
     * @param element Top level Element to unpack.
     * @param o  ignored
     */
    public void load(Element element, Object o) {
        log.error("Unexpected call of load(Element, Object)");
    }
    // initialize logging
    static Logger log = LoggerFactory.getLogger(ManagerDefaultsConfigPaneXml.class.getName());

}
