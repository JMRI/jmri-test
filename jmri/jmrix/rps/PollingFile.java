package jmri.jmrix.rps;

import java.io.*;
import org.jdom.*;
import java.util.*;

import jmri.jmrit.XmlFile;
import javax.vecmath.Point3d;

/**
 * Persist RPS polling information
 * <P>
 * @author  Bob Jacobsen   Copyright 2008
 * @version $Revision: 1.5 $
 */
public class PollingFile extends XmlFile {

    Document doc;
    Element root;
    
    /**
     * Initialize for writing information.
     * <P>
     * This is followed by multiple "set" calls, then a "store"
     */
    public void prepare() {
        root = new Element("rpsfile");
        doc = newDocument(root, dtdLocation+"rpsroster.dtd");

        // add XSLT processing instruction
        // <?xml-stylesheet type="text/xsl" href="XSLT/rpsroster.xsl"?>
        java.util.Map m = new java.util.HashMap();
        m.put("type", "text/xsl");
        m.put("href", xsltLocation+"rpsroster.xsl");
        ProcessingInstruction p = new ProcessingInstruction("xml-stylesheet", m);
        doc.addContent(0,p);
        
    }

    public void setPoll() {
        Element v = new Element("poll");
        v.setAttribute("active", Engine.instance().getPolling()?"true":"false");
        v.setAttribute("interval", ""+Engine.instance().getPollingInterval());
        v.setAttribute("bscpoll", Engine.instance().getBscPollMode()?"true":"false");
        v.setAttribute("throttlepoll", Engine.instance().getThrottlePollMode()?"true":"false");
        root.addContent(v);
    }
    
    public void setTransmitter(int r) {
        Element e = new Element("transmitter");
        e.setAttribute("id", Engine.instance().getTransmitter(r).getID());
        e.setAttribute("address", ""+Engine.instance().getTransmitter(r).getAddress());
        e.setAttribute("long", Engine.instance().getTransmitter(r).isLongAddress()?"true":"false");
        e.setAttribute("poll", Engine.instance().getTransmitter(r).isPolled()?"true":"false");
        root.addContent(e);
    }


    public void store(File file) throws JDOMException, IOException {
        writeXML(file, doc);
    }

    /**
     * Read in the file, and make available
     * for examination
     */
    public void loadFile(File f) 
            throws org.jdom.JDOMException, java.io.IOException {
            
        root = rootFromFile(f);
    }
    
    public void getPollValues() {
        Element e = root.getChild("poll");
        
        Attribute a = e.getAttribute("active");
        boolean poll = false;
        if (a != null && a.getValue().equals("true")) poll = true;
        Engine.instance().setPolling(poll);
        
        a = e.getAttribute("interval");
        int value = 0;
        try {
            if (a != null) value = a.getIntValue();
        } catch (org.jdom.DataConversionException ex) {}
        Engine.instance().setPollingInterval(value);

        Engine.instance().setDirectPollMode();
    
        a = e.getAttribute("bscpoll");
        boolean bscpoll = false;
        if (a != null && a.getValue().equals("true")) bscpoll = true;
        if (bscpoll) Engine.instance().setBscPollMode();

        a = e.getAttribute("throttlepoll");
        boolean throttlepoll = false;
        if (a != null && a.getValue().equals("true")) throttlepoll = true;
        if (throttlepoll) Engine.instance().setThrottlePollMode();

    }
    
    /**
     * Get the transmitters from the file
     */
    public void getTransmitters(Engine engine) {
        List l = root.getChildren("transmitter");

        for (int i = 0; i<l.size(); i++) {  // i indexes over the elements in the file
            Element e = (Element)l.get(i);
            String id = e.getAttribute("id").getValue();
            // find the matching transmitter (from Roster) and load poll value
            for (int j = 0; j<engine.getNumTransmitters(); j++) { // j indexes over transmitters
                if (engine.getTransmitter(j).getID().equals(id)) {
                    Attribute a = e.getAttribute("poll");
                    boolean poll = false;
                    if (a != null && a.getValue().equals("true")) poll = true;
                    engine.getTransmitter(j).setPolled(poll);
                    break;
                }
            }
        }
        
        return;
    }
                
    static public String defaultLocation() {
        String location = XmlFile.prefsDir()+File.separator+"rps"+File.separator;
        XmlFile.ensurePrefsPresent(XmlFile.prefsDir());
        XmlFile.ensurePrefsPresent(location);
        return location;
    }
    static public String defaultFilename() {
        return defaultLocation()+"roster.xml";
    }
    
    // initialize logging
    static private org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PollingFile.class.getName());
}