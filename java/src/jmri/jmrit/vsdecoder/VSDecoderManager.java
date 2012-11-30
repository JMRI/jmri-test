package jmri.jmrit.vsdecoder;

/*
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 * <P>
 *
 * @author			Mark Underwood Copyright (C) 2011
 * @version			$Revision$
 */

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.List;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Map;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.JFrame;
import org.jdom.Element;

import jmri.DccLocoAddress;
import jmri.jmrit.XmlFile;
import jmri.util.JmriJFrame;
import jmri.util.PhysicalLocation;
import jmri.Reporter;
import jmri.LocoAddress;
import jmri.PhysicalLocationReporter;
import jmri.util.PhysicalLocation;
import jmri.IdTag;
import jmri.jmrit.vsdecoder.listener.VSDListener;
import jmri.jmrit.vsdecoder.listener.ListeningSpot;
import jmri.jmrit.vsdecoder.swing.VSDManagerFrame;

// VSDecoderFactory
//
// Builds VSDecoders as needed.  Handles loading from XML if needed.

public class VSDecoderManager implements PropertyChangeListener {


    private static final ResourceBundle rb = VSDecoderBundle.bundle();

    private static final String vsd_property_change_name = "VSDecoder Manager";
    protected jmri.NamedBeanHandleManager nbhm = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class);

    HashMap<String, VSDListener> listenerTable; // list of listeners
    HashMap<String, VSDecoder> decodertable; // list of active decoders by System ID
    HashMap<String, VSDecoder> decoderAddressMap; // List of active decoders by address
    HashMap<String, String> profiletable;    // list of loaded profiles key = profile name, value = path
    List<String> reportertable;        // list of Reporters we are following.

    // List of registered event listeners
    protected javax.swing.event.EventListenerList listenerList = new javax.swing.event.EventListenerList();

    //private static VSDecoderManager instance = null;   // sole instance of this class
    private static VSDecoderManagerThread thread = null; // thread for running the manager

    private VSDecoderPreferences vsdecoderPrefs; // local pointer to the preferences object
    private JmriJFrame vsdecoderPreferencesFrame; // Frame for holding the preferences GUI  (do we need this?)

    private JmriJFrame managerFrame = null;

    private VSDecoder default_decoder = null;  // shortcut pointer to the default decoder (do we need this?)

    private static int vsdecoderID = 0;
    private static int listenerID = 0;

    // Unused?
    //private PhysicalLocation listener_position;

    // constructor - for kicking off by the VSDecoderManagerThread...
    // WARNING: Should only be called from static instance()
    public VSDecoderManager() {
	// Setup the decoder table
	listenerTable = new HashMap<String, VSDListener>();
	decodertable = new HashMap<String, VSDecoder>();
	decoderAddressMap = new HashMap<String, VSDecoder>();
	profiletable = new HashMap<String, String>();  // key = profile name, value = path
	reportertable = new ArrayList<String>();
	// Get preferences
	String dirname = XmlFile.prefsDir()+ "vsdecoder" +File.separator;
	XmlFile.ensurePrefsPresent(dirname);
	vsdecoderPrefs = new VSDecoderPreferences(dirname+ rb.getString("VSDPreferencesFileName"));
	// Listen to ReporterManager for Report List changes
	setupReporterManagerListener();
	// Get a Listener (the only one for now)
	//VSDListener t = new VSDListener(getNextListenerID());
	VSDListener t = new VSDListener();
	listenerTable.put(t.getSystemName(), t);
    }

    public static VSDecoderManager instance() {
	if (thread == null) {
	    thread = VSDecoderManagerThread.instance(true);
	}
	return(VSDecoderManagerThread.manager());
    }

    public VSDecoderPreferences getVSDecoderPreferences() {
	return(vsdecoderPrefs);
    }

    private void buildVSDecoderPreferencesFrame() {
	vsdecoderPreferencesFrame = new JmriJFrame(rb.getString("VSDecoderPreferencesFrameTitle"));
	VSDecoderPreferencesPane tpP = new VSDecoderPreferencesPane(vsdecoderPrefs);
	vsdecoderPreferencesFrame.add(tpP);
	tpP.setContainer(vsdecoderPreferencesFrame);
	vsdecoderPreferencesFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	vsdecoderPreferencesFrame.pack();
    }

    public void showVSDecoderPreferences() {
	if (vsdecoderPreferencesFrame == null) {
	    buildVSDecoderPreferencesFrame();
	}
	vsdecoderPreferencesFrame.pack();
	vsdecoderPreferencesFrame.setVisible(true);
	vsdecoderPreferencesFrame.requestFocus();
    }
	
    public JmriJFrame provideManagerFrame() {
	if (managerFrame == null) {
	    managerFrame = new VSDManagerFrame();
	}
	return(managerFrame);
    }

    private String getNextVSDecoderID() {
	// vsdecoderID initialized to zero, pre-incremented before return...
	// first returned ID value is 1.
	return("IAD:VSD:VSDecoderID" + (++vsdecoderID));
    }

    // To be used in the future
    private String getNextListenerID() {
	// ListenerID initialized to zero, pre-incremented before return...
	// first returned ID value is 1.
	// Prefix is added by the VSDListener constructor
	return("VSDecoderID" + (++listenerID));
    }

    // New version (now)
    public VSDecoder getVSDecoder(String profile_name) {
	VSDecoder vsd;
	String path;
	if (profiletable.containsKey(profile_name)) {
	    path = profiletable.get(profile_name);
	    log.debug("Profile " + profile_name + " is in table.  Path = " + path);
	    vsd = new VSDecoder(getNextVSDecoderID(), profile_name, path);
	    decodertable.put(vsd.getID(), vsd);  // poss. broken for duplicate profile names
	    decoderAddressMap.put(vsd.getAddress().toString(), vsd);
	    return(vsd);
	} else {
	    // Don't have enough info to try to load from file.
	    log.error("Requested profile not loaded: " + profile_name);
	    return(null);
	}
    }

    public VSDecoder getVSDecoder(String profile_name, String path) {
	VSDecoder vsd = new VSDecoder(getNextVSDecoderID(), profile_name, path);
	decodertable.put(vsd.getID(), vsd); // poss. broken for duplicate profile names
	if (vsd.getAddress() != null)
	    decoderAddressMap.put(vsd.getAddress().toString(), vsd);
	return(vsd);
    }

    /** Provide or build a VSDecoder based on a provided configuration */
    public VSDecoder getVSDecoder(VSDConfig config) {
	String path;
	String profile_name = config.getProfileName();
	if (profiletable.containsKey(profile_name)) {
	    path = profiletable.get(profile_name);
	    log.debug("Profile " + profile_name + " is in table.  Path = " + path);
	    config.setVSDPath(path);
	    config.setID(getNextVSDecoderID());
	    VSDecoder vsd = new VSDecoder(config);
	    decodertable.put(vsd.getID(), vsd);
	    decoderAddressMap.put(vsd.getAddress().toString(), vsd);
	    //debugPrintDecoderList();
	    return(vsd);
	} else {
	    // Don't have enough info to try to load from file.
	    log.error("Requested profile not loaded: " + profile_name);
	    return(null);
	}
    }

    public void debugPrintDecoderList() {
	log.debug("Current Decoder List by System ID:");
	Set<Map.Entry<String, VSDecoder>> ids = decodertable.entrySet();
	Iterator<Map.Entry<String, VSDecoder>> idi = ids.iterator();
	while (idi.hasNext()) {
	    Map.Entry<String, VSDecoder> e = idi.next();
	    log.debug("    ID = " +  e.getKey() + " Val = " + e.getValue().getAddress().toString());
	}
	log.debug("Current Decoder List by Address:");
	ids = decoderAddressMap.entrySet();
	idi = ids.iterator();
	while (idi.hasNext()) {
	    Map.Entry<String, VSDecoder> e = idi.next();
	    log.debug("    ID = " +  e.getKey() + " Val = " + e.getValue().getAddress().toString());
	}
    }

    public VSDecoder getVSDecoderByID(String id) {
	VSDecoder v = decodertable.get(id);
	if (v == null)
	    log.debug("No decoder in table! ID = " + id);
	return(decodertable.get(id));
    }

    public VSDecoder getVSDecoderByAddress(String sa) {
	if (sa == null) {
	    log.debug("Decoder Address is Null");
	    return(null);
	}
	log.debug("Decoder Address: " + sa);
	VSDecoder rv = decoderAddressMap.get(sa);
	if (rv == null) {
	    log.debug("Not found.");
	} else {
	    log.debug("Found: " + rv.getAddress());
	}
	return(rv);
    }

    /*
    public VSDecoder getVSDecoderByAddress(String sa) {
	// First, translate the string into a DccLocoAddress
        // no object if no address
        if (sa.equals("")) return null;
        
	DccLocoAddress da = null;
        // ask the Throttle Manager to handle this!
        LocoAddress.Protocol protocol;
        if(InstanceManager.throttleManagerInstance()!=null){
            protocol = InstanceManager.throttleManagerInstance().getProtocolFromString(sa);
            da = (DccLocoAddress)InstanceManager.throttleManagerInstance().getAddress(sa, protocol);
        }

	// now look up the decoder
	if (da != null) {
	    return getVSDecoderByAddress(da);
	}
	return(null);
	
    }
    */

    public void setDefaultVSDecoder(VSDecoder d) {
	default_decoder = d;
    }

    public VSDecoder getDefaultVSDecoder() {
	return(default_decoder);
    }

    public ArrayList<String> getVSDProfileNames() {
	ArrayList<String> sl = new ArrayList<String>();
	for (String p : profiletable.keySet()) {
	    sl.add(p);
	}
	return(sl);
    }

    public Collection<VSDecoder> getVSDecoderList() {
	return(decodertable.values());
    }

    public String getDefaultListenerName() {
	return(VSDListener.ListenerSysNamePrefix + "ListenerID1");
    }

    public ListeningSpot getDefaultListenerLocation() {
	VSDListener l = listenerTable.get(getDefaultListenerName());
	if (l != null)
	    return(l.getLocation());
	else
	    return(null);
    }

    public void setListenerLocation(String id, ListeningSpot sp) {
	VSDListener l = listenerTable.get(id);
	log.debug("Set listener location " + sp + " listener: " + l);
	if (l != null)
	    l.setLocation(sp);
    }

    public void setDecoderPositionByID(String id, PhysicalLocation p) {
	VSDecoder d = decodertable.get(id);
	if (d != null)
	    d.setPosition(p);
    }

    public void setDecoderPositionByAddr(LocoAddress a, PhysicalLocation l) {
	// Find the addressed decoder
	// This is a bit hokey.  Need a better way to index decoder by address
	// OK, this whole LocoAddress vs. DccLocoAddress thing has rendered this SUPER HOKEY.
	if ((a == null) || (l == null)) {
	    log.debug("Decoder Address is Null");
	    return;
	}
	if (l.equals(PhysicalLocation.Origin)) {
	    log.debug("Location : "  + l.toString() + " ... ignoring.");
	    // Physical location at origin means it hasn't been set.
	    return;
	}
	log.debug("Decoder Address: " + a.getNumber());
	for ( VSDecoder d : decodertable.values()) {
	    // Get the Decoder's address protocol.  If it's a DCC_LONG or DCC_SHORT, convert to DCC
	    // since the LnReprter can't tell the difference and will always report "DCC".
	    if (d == null) {
		log.debug("VSdecoder null pointer!");
		return;
	    }
	    LocoAddress pa = d.getAddress();
	    if (pa == null) {
		log.debug("Vsdecoder" + d + " address null!");
		return;
	    }
	    LocoAddress.Protocol p = d.getAddress().getProtocol();
	    if (p == null) {
		log.debug("Vsdecoder" + d + " address = " + pa + " protocol null!");
		return;
	    }
	    if ((p == LocoAddress.Protocol.DCC_LONG) || (p == LocoAddress.Protocol.DCC_SHORT))
		p = LocoAddress.Protocol.DCC;
	    if ((d.getAddress().getNumber() == a.getNumber()) && (p == a.getProtocol())) {
		d.setPosition(l);
		// Loop through all the decoders (assumes N will be "small"), in case
		// there are multiple decoders with the same address.  This will be somewhat broken
		// if there's a DCC_SHORT and a DCC_LONG decoder with the same address number.
		//return;
	    }
	}
	// decoder not found.  Do nothing.
	return;
    }

    // VSDecoderManager Events
    public void addEventListener(VSDManagerListener listener) {
	listenerList.add(VSDManagerListener.class, listener);
    }

    public void removeEventListener(VSDManagerListener listener) {
	listenerList.remove(VSDManagerListener.class, listener);
    }

    void fireMyEvent(VSDManagerEvent evt) {
	//Object[] listeners = listenerList.getListenerList();

	for (VSDManagerListener l : listenerList.getListeners(VSDManagerListener.class)) {
	    l.eventAction(evt);
	}
    }


    /** getProfilePath()
     *
     *  Retrieve the Path for a given Profile name.
     */
    public String getProfilePath(String profile) {
	return(profiletable.get(profile));
    }

    /** Load Profiles from a VSD file
     * Not deprecated anymore. used by the new ConfigDialog.
     */
    public void loadProfiles(String path) {
	try {
	    VSDFile vsdfile = new VSDFile(path);
	    if (vsdfile.isInitialized()) {
		this.loadProfiles(vsdfile);
	    }
	} catch (java.util.zip.ZipException e) {
	    log.error("ZipException loading VSDecoder from " + path);
	    // would be nice to pop up a dialog here...
	} catch (java.io.IOException ioe) {
	    log.error("IOException loading VSDecoder from " + path);
	    // would be nice to pop up a dialog here...
	}
    }

    protected void registerReporterListener(String sysName) {
	Reporter r = jmri.InstanceManager.reporterManagerInstance().getReporter(sysName);
	if (r == null) {
	    return;
	}
	jmri.NamedBeanHandle<Reporter> h = nbhm.getNamedBeanHandle(sysName, r);
	if (h == null) {
	    return;
	}
	// Make sure we aren't already registered.
	ArrayList<java.beans.PropertyChangeListener> ll = r.getPropertyChangeListeners(h.getName());
	if (ll.isEmpty()) { 
	    r.addPropertyChangeListener(this, h.getName(), vsd_property_change_name);
	}
    }

    protected void registerReporterListeners() {
	// Walk through the list of reporters
	for (String sysName : jmri.InstanceManager.reporterManagerInstance().getSystemNameList()) {
	    registerReporterListener(sysName);
	}
    }

    // This listener listens to the ReporterManager for changes to the list of Reporters.
    // Need to trap list length (name="length") changes and add listeners when new ones are added.
    private void setupReporterManagerListener() {
	// Register ourselves as a listener for changes to the Reporter list.  For now, we won't do this. Just force a
	// save and reboot after reporters are added.  We'll fix this later.
	//	jmri.InstanceManager.reporterManagerInstance().addPropertyChangeListener(new PropertyChangeListener() {
	//	public void propertyChange(PropertyChangeEvent event) {
	//		    log.debug("property change name " + event.getPropertyName() + " old " + event.getOldValue() + " new " + event.getNewValue());
	//	    reporterManagerPropertyChange(event);
	//	}
	//   });
	jmri.InstanceManager.reporterManagerInstance().addPropertyChangeListener(this);

	// Now, the Reporter Table might already be loaded and filled out, so we need to get all the Reporters and list them.
	// And add ourselves as a listener to them.
	
	for (String sysName : jmri.InstanceManager.reporterManagerInstance().getSystemNameList()) {
	    registerReporterListener(sysName);
	}
    }

    protected void shutdownDecoders() {
	// Shut down and destroy all running VSDecoders.
	Set<String> vk = decodertable.keySet();
	Iterator<String> it = vk.iterator();
	while(it.hasNext()) {
	    VSDecoder v = decodertable.get(it.next());
	    v.shutdown();
	}
	// Empty the DecoderTable
	vk = decodertable.keySet();
	it = vk.iterator();
	while(it.hasNext()) {
	    decodertable.remove(it.next());
	}
	// Empty the AddressMap
	vk = decoderAddressMap.keySet();
	it = vk.iterator();
	while(it.hasNext()) {
	    decoderAddressMap.remove(it.next());
	}
    }

    public void propertyChange(PropertyChangeEvent evt) {
	log.debug("property change name " + evt.getPropertyName() + " old " + evt.getOldValue() + " new " + evt.getNewValue());
	if (evt.getSource() instanceof jmri.ReporterManager) {
	    reporterManagerPropertyChange(evt);
	} else if (evt.getSource() instanceof jmri.Reporter) {
	    reporterPropertyChange(evt);
	} else if (evt.getSource() instanceof VSDManagerFrame) {
	    if (evt.getPropertyName().equals(VSDManagerFrame.PCIDMap.get(VSDManagerFrame.PropertyChangeID.REMOVE_DECODER))) {
		// Shut down the requested decoder and remove it from the manager's hash maps. 
		// Unless there are "illegal" handles, this should put the decoder on the garbage heap.  I think.
		String sa = (String)evt.getNewValue();
		VSDecoder d = this.getVSDecoderByAddress(sa);
		log.debug("Removing Decoder " + sa + " ... " + d.getAddress());
		d.shutdown();
		decodertable.remove(d.getID());
		decoderAddressMap.remove(sa);
		debugPrintDecoderList();
	    } else if(evt.getPropertyName().equals(VSDManagerFrame.PCIDMap.get(VSDManagerFrame.PropertyChangeID.CLOSE_WINDOW))) {
		// Note this assumes there is only one VSDManagerFrame open at a time.
		shutdownDecoders();
		managerFrame = null;
	    }
	} else {
	    // Un-Handled source. Does nothing ... yet...
	}
	return;
    }

    public void reporterPropertyChange(PropertyChangeEvent event) {
	// Needs to check the ID on the event, look up the appropriate VSDecoder,
	// get the location of the event source, and update the decoder's location.
	@SuppressWarnings("cast")
	String eventName = (String)event.getPropertyName();
	if ((event.getSource() instanceof PhysicalLocationReporter) && (eventName.equals("currentReport"))) {
	    PhysicalLocationReporter arp = (PhysicalLocationReporter) event.getSource();
	    // Need to decide which reporter it is, so we can use different methods
	    // to extract the address and the location.
	    if (event.getNewValue() instanceof String) {
		String newValue = (String)event.getNewValue();
		if (arp.getDirection(newValue) == PhysicalLocationReporter.Direction.ENTER)
		    setDecoderPositionByAddr(arp.getLocoAddress(newValue), arp.getPhysicalLocation(newValue));
	    } else if (event.getNewValue() instanceof IdTag) {
		// newValue is of IdTag type.
		// Dcc4Pc, Ecos, 
		// Assume Reporter "arp" is the most recent seen location
		IdTag newValue = (IdTag) event.getNewValue();
		setDecoderPositionByAddr(arp.getLocoAddress(newValue.getTagID()), arp.getPhysicalLocation(null));
	    } else {
		log.debug("Reporter's return type is not supported.");
		// do nothing
	    }

	} else {
	    log.debug("Reporter doesn't support physical location reporting or isn't reporting new info.");
	}  // Reporting object implements PhysicalLocationReporter
	return;
    }

    public void reporterManagerPropertyChange(PropertyChangeEvent event) {
	String eventName = event.getPropertyName();

	log.debug("VSDecoder received Reporter Manager Property Change: " + eventName);
	if (eventName.equals("length")) {
	    
	    // Re-register for all the reporters. The registerReporterListener() will skip
	    // any that we're already registered for.
	    for (String sysName : jmri.InstanceManager.reporterManagerInstance().getSystemNameList()) {
		registerReporterListener(sysName);
	    }
	 
	    // It could be that we lost a Reporter.  But since we aren't keeping a list anymore
	    // we don't care.
	}
    }

    public void loadProfiles(VSDFile vf) {
	Element root;
	String pname;
	if ((root = vf.getRoot()) == null)
	    return;
	
	ArrayList<String> new_entries = new ArrayList<String>();

	@SuppressWarnings("unchecked")
	java.util.Iterator<Element> i = root.getChildren("profile").iterator();
	while (i.hasNext()) {
	    Element e = i.next();
	    log.debug(e.toString());
	    if ((pname = e.getAttributeValue("name")) != null) {
		profiletable.put(pname, vf.getName());
		new_entries.add(pname);
	    }
	}

	// debug
	/*
	for (String s : new_entries) {
	    log.debug("New entry: " + s);
	}
	*/
	// /debug
	    
	fireMyEvent(new VSDManagerEvent(this, VSDManagerEvent.EventType.PROFILE_LIST_CHANGE, new_entries));
    }

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(VSDecoderManager.class.getName());

}