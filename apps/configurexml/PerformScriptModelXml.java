package apps.configurexml;

import apps.PerformScriptModel;

import org.jdom.Element;

/**
 * Handle XML persistance of PerformScriptModel objects
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @version $Revision: 1.10 $
 * @see apps.PerformScriptPanel
 */
public class PerformScriptModelXml extends jmri.configurexml.AbstractXmlAdapter {

    public PerformScriptModelXml() {
    }

    /**
     * Default implementation for storing the model contents
     * @param o Object to store, of type PerformActonModel
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        Element e = new Element("perform");
        PerformScriptModel g = (PerformScriptModel) o;

        e.setAttribute("name", g.getFileName());
        e.setAttribute("type", "ScriptFile");
        e.setAttribute("class", this.getClass().getName());
        return e;
    }

    /**
     * Create object from XML file
     * @param e Top level Element to unpack.
     * @return true if successful
      */
    public boolean load(Element e) {
    	boolean result = true;
        String fileName = e.getAttribute("name").getValue();
        log.debug("Run file "+fileName);

        // run the script
        jmri.util.PythonInterp.runScript(fileName);

        // leave an updated object around
        PerformScriptModel m = new PerformScriptModel();
        m.setFileName(fileName);
        PerformScriptModel.rememberObject(m);
        jmri.InstanceManager.configureManagerInstance().registerPref(new apps.PerformScriptPanel());
        return result;
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
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PerformScriptModelXml.class.getName());

}