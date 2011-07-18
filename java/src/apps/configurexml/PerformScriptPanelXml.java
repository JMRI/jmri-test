package apps.configurexml;

import apps.PerformScriptPanel;

import jmri.InstanceManager;
import java.awt.Component;

import org.jdom.Element;

/**
 * Handle XML persistance of PerformScriptModel objects
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @version $Revision$
 * @see apps.PerformScriptPanel
 */
public class PerformScriptPanelXml extends jmri.configurexml.AbstractXmlAdapter {

    public PerformScriptPanelXml() {
    }

    /**
     * Arrange for all the model objects to be stored
     * @param o Object to store, of type PerformScriptPanel
     * @return null, after updating state so others are stored
     */
    public Element store(Object o) {
        PerformScriptPanel p = (PerformScriptPanel) o;
        Component[] l = p.getComponents();
        for (int i = 0; i<l.length; i++) {
            if ( (l[i]!= null) && (l[i].getClass().equals(PerformScriptPanel.Item.class))) {
                PerformScriptPanel.Item m = (PerformScriptPanel.Item) l[i];
                InstanceManager.configureManagerInstance().registerPref(m.getModel());
            }
        }
        return null;
    }

    /**
     * Create object from XML file, but this method should never be invoked.
     * @param element Top level Element to unpack.
     * @return true if successful
      */
    public boolean load(Element element) {
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
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PerformScriptPanelXml.class.getName());

}