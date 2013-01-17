package apps.configurexml;

import apps.FileLocationPane;
import org.jdom.Element;
import java.util.List;

/**
 * Handle XML persistance of directory locations.
 * <P>
 *
 * @author Kevin Dickerson Copyright: Copyright (c) 2010
 * @version $Revision$
 */
public class FileLocationPaneXml extends jmri.configurexml.AbstractXmlAdapter {

    public FileLocationPaneXml() {
    }

    /**
     * Default implementation for storing the static contents of the Swing LAF
     * @param o Object to store, of type FileLocationPane
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        Element e = new Element("fileLocations");
        /*e.setAttribute("defaultScriptLocation", jmri.jmrit.XmlFile.scriptsDir());
        e.setAttribute("defaultUserLocation", jmri.jmrit.XmlFile.userFileLocationDefault());
        e.setAttribute("defaultThrottleLocation", jmri.jmrit.throttle.ThrottleFrame.getDefaultThrottleFolder());*/
        storeLocation(e, "defaultScriptLocation", jmri.jmrit.XmlFile.scriptsDir());
        storeLocation(e, "defaultUserLocation", jmri.jmrit.XmlFile.userFileLocationDefault());
        storeLocation(e, "defaultThrottleLocation", jmri.jmrit.throttle.ThrottleFrame.getDefaultThrottleFolder());
        e.setAttribute("class", this.getClass().getName());
        return e;
    }
    
    private void storeLocation(Element locations, String attr, String value){
        Element userLocation = new Element("fileLocation");
        userLocation.setAttribute(attr, value);
        locations.addContent(userLocation);
    }

    /**
     * Update static data from XML file
     * @param e Top level Element to unpack.
     * @return true if successful
      */
    public boolean load(Element e) {
    	boolean result = true;
        String value = loadUserLocations(e, "defaultScriptLocation");
        if (value!=null)
            jmri.jmrit.XmlFile.setScriptsFileLocationDefault(value);
        //Attribute scriptLocation = e.getAttribute("defaultScriptLocation");
        //if (scriptLocation!=null)
        //jmri.jmrit.XmlFile.setScriptsFileLocationDefault(scriptLocation.getValue());
        /*Attribute userLocation = e.getAttribute("defaultUserLocation");
        if (userLocation!=null)
            jmri.jmrit.XmlFile.setUserFileLocationDefault(userLocation.getValue());*/
        value = loadUserLocations(e, "defaultUserLocation");
        if (value!=null)
            jmri.jmrit.XmlFile.setUserFileLocationDefault(value);
        /*Attribute throttleLocation = e.getAttribute("defaultThrottleLocation");
        if (throttleLocation!=null)
            jmri.jmrit.throttle.ThrottleFrame.setDefaultThrottleLocation(userLocation.getValue());*/
        value = loadUserLocations(e, "defaultThrottleLocation");
        if (value!=null)
            jmri.jmrit.throttle.ThrottleFrame.setDefaultThrottleLocation(value);
        jmri.InstanceManager.configureManagerInstance().registerPref(new FileLocationPane());
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private String loadUserLocations(Element messages, String attr){
        List<Element> messageList = messages.getChildren("fileLocation");
        for (int i=0; i<messageList.size();i++){
            if (messageList.get(i).getAttribute(attr)!=null) {
                return messageList.get(i).getAttribute(attr).getValue();
            }
        }
        return null;
    
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
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(FileLocationPaneXml.class.getName());

}