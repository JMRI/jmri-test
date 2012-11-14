package jmri.jmrit.vsdecoder;

/**
 * Virtual Sound Decoder
 * 
 * Implements a software "decoder" that responds to throttle inputs
 * and generates sounds in responds to them.
 *
 * Each VSDecoder implements exactly one Sound Profile (describes a
 * particular type of locomtive, say, an EMD GP7).
 *
 */

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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import jmri.LocoAddress;
import jmri.DccLocoAddress;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import jmri.util.PhysicalLocation;
import jmri.jmrit.vsdecoder.VSDecoderEvent;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.locations.Location;

import org.jdom.Element;

import jmri.jmrit.vsdecoder.swing.VSDControl;

public class VSDecoder implements PropertyChangeListener {

    boolean initialized = false;   // This decoder has been initialized
    boolean enabled = false;       // This decoder is enabled
    private boolean is_default = false;  // This decoder is the default for its file

    private VSDConfig config;

    // List of registered event listeners
    protected javax.swing.event.EventListenerList listenerList = new javax.swing.event.EventListenerList();

    HashMap<String, VSDSound> sound_list;   // list of sounds
    HashMap<String, Trigger> trigger_list;  // list of triggers
    HashMap<String, SoundEvent> event_list; // list of events
    

    /**
     * public VSDecoder(String id, String name)
     *
     * Construct a VSDecoder with a given name and ID (system name)
     *
     * Parameters:
     * @param id   (String) System Name of this VSDecoder
     * @param name (String) Sound Profile name for this VSDecoder
     */
    @Deprecated
    public VSDecoder(String id, String name) {

	config = new VSDConfig();
	config.setProfileName(name);
	config.setID(id);
	
	sound_list = new HashMap<String, VSDSound>();
	trigger_list = new HashMap<String, Trigger>();
	event_list = new HashMap<String, SoundEvent>();
	    
	// Force re-initialization
	initialized = _init();
    }

    /** public VSDecoder(VSDConfig cfg)
     *
     * Construct a VSDecoder with the given system name (id)
     * and configuration (config)
     *
     * Parameters:
     * @param cfg  (VSDConfig) Configuration
     */
    public VSDecoder(VSDConfig cfg) {
	config = cfg;

	sound_list = new HashMap<String, VSDSound>();
	trigger_list = new HashMap<String, Trigger>();
	event_list = new HashMap<String, SoundEvent>();
	    
	// Force re-initialization
	initialized = _init();

	try {
	    VSDFile vsdfile = new VSDFile(config.getVSDPath());
	    if (vsdfile.isInitialized()) {
		log.debug("Constructor: vsdfile init OK, loading XML...");
		this.setXml(vsdfile, config.getProfileName());
	    } else {
		log.debug("Constructor: vsdfile init FAILED.");
		initialized = false;
	    }
	} catch (java.util.zip.ZipException e) {
	    log.error("ZipException loading VSDecoder from " + config.getVSDPath());
	    // would be nice to pop up a dialog here...
	} catch (java.io.IOException ioe) {
	    log.error("IOException loading VSDecoder from " + config.getVSDPath());
	    // would be nice to pop up a dialog here...
	}

	// Since the Config already has the address set, we need to call
	// our own setAddress() to register the throttle listener
	this.setAddress(config.getLocoAddress());
	this.enable();
    }

    /**
     * public VSDecoder(String id, String name, String path)
     *
     * Construct a VSDecoder with the given system name (id), profile name
     * and VSD file path
     *
     * Parameters:
     * @param id    (String) System name for this VSDecoder
     * @param name  (String) Profile name
     * @param path  (String) Path to a VSD file to pull the given Profile from
     */
    public VSDecoder(String id, String name, String path) {
	
	config = new VSDConfig();
	config.setProfileName(name);
	config.setID(id);
	
	sound_list = new HashMap<String, VSDSound>();
	trigger_list = new HashMap<String, Trigger>();
	event_list = new HashMap<String, SoundEvent>();
	    
	// Force re-initialization
	initialized = _init();

	config.setVSDPath(path);

	try {
	    VSDFile vsdfile = new VSDFile(path);
	    if (vsdfile.isInitialized()) {
		log.debug("Constructor: vsdfile init OK, loading XML...");
		this.setXml(vsdfile, name);
	    } else {
		log.debug("Constructor: vsdfile init FAILED.");
		initialized = false;
	    }
	} catch (java.util.zip.ZipException e) {
	    log.error("ZipException loading VSDecoder from " + path);
	    // would be nice to pop up a dialog here...
	} catch (java.io.IOException ioe) {
	    log.error("IOException loading VSDecoder from " + path);
	    // would be nice to pop up a dialog here...
	}
    }

    private boolean _init() {
	// Do nothing for now
	return(true);
    }

    /**
     * public String getID()
     *
     * Get the ID (System Name) of this VSDecoder
     *
     * @return (String) system name of this VSDecoder
     */
    public String getID() {
	return(config.getID());
    }

    /**
     * public boolean isInitialized()
     *
     * Check whether this VSDecoder has completed initialization
     *
     * @return (boolean) true if initialization is complete.
     */

    public boolean isInitialized() { return(initialized); }

    /**
     * public void setVSDFilePath(String p)
     *
     * Set the VSD File path for this VSDecoder to use
     *
     * @param p  (String) path to VSD File
     * @return void
     */
    public void setVSDFilePath(String p) {
	config.setVSDPath(p);
    }

    /**
     * public String getVSDFilePath()
     *
     * Get the current VSD File path for this VSDecoder
     *
     * @return (String) path to VSD file
     */
    public String getVSDFilePath() {
	return(config.getVSDPath());
    }

    // VSDecoder Events

    /**
     * public String addEventListener(VSDecoderListener listener)
     *
     * Add a listener for this object's events
     *
     * @param (VSDecoderListener) listener handle
     * @return void
     */
    public void addEventListener(VSDecoderListener listener) {
	listenerList.add(VSDecoderListener.class, listener);
    }

    /**
     * public String removeEventListener(VSDecoderListener listener)
     *
     * Remove a listener for this object's events
     *
     * @param (VSDecoderListener) listener handle
     * @return void
     */
    public void removeEventListener(VSDecoderListener listener) {
	listenerList.remove(VSDecoderListener.class, listener);
    }

    /** Fire an event to this object's listeners */
    private void fireMyEvent(VSDecoderEvent evt) {
	for (VSDecoderListener l : listenerList.getListeners(VSDecoderListener.class)) {
	    l.eventAction(evt);
	}
    }

    /**
     * public void windowChange(java.awt.event.WindowEvent e)
     *
     * Handle Window events from this VSDecoder's GUI window.
     *
     * @param e  (java.awt.event.WindowEvent) the window event to handle
     * @return void
     */
    public void windowChange(java.awt.event.WindowEvent e) {
	log.debug("decoder.windowChange() - " + e.toString());
	log.debug("param string = " + e.paramString());
	//if (e.paramString().equals("WINDOW_CLOSING")) {
	    // Shut down the sounds.
	    this.shutdown();
	    
	//}
    }

    /**
     * public void shutdown()
     *
     * Shut down this VSDecoder and all of its associated sounds.
     *
     * @return void
     */
    public void shutdown() {
	log.debug("Shutting down sounds...");
	for (VSDSound vs : sound_list.values()) {
	    log.debug("Stopping sound: " + vs.getName());
	    vs.shutdown();
	}
    }

    /**
     * protected void throttlePropertyChange(PropertyChangeEvent event)
     *
     * Handle the details of responding to a PropertyChangeEvent from a throttle.
     *
     * @param event (PropertyChangeEvent) Throttle event to respond to
     * @return void
     */
    protected void throttlePropertyChange(PropertyChangeEvent event) {
	//WARNING: FRAGILE CODE
	// This will break if the return type of the event.getOld/NewValue() changes.
	
	String eventName = event.getPropertyName();
	Object oldValue = event.getOldValue();
	Object newValue = event.getNewValue();

	// Skip this if disabled
	if (!enabled) {
	    log.debug("VSDecoder disabled. Take no action.");
	    return;
	}

	log.warn("VSDecoderPane throttle property change: " + eventName);

	if (oldValue != null)
	    log.warn("Old: " + oldValue.toString());
	if (newValue != null)
	    log.warn("New: " + newValue.toString());

	// Iterate through the list of sound events, forwarding the propertyChange event.
	for (SoundEvent t : event_list.values()) {
	    t.propertyChange(event);
	}

	// Iterate through the list of triggers, forwarding the propertyChange event.
	for (Trigger t : trigger_list.values()) {
	    t.propertyChange(event);
	}
    }

    // DCC-specific and unused. Deprecate this.
    @Deprecated
    public void releaseAddress(int number, boolean isLong) {
	// remove the listener, if we can...
    }

    // DCC-specific.  Deprecate this.
    @Deprecated
    public void setAddress(int number, boolean isLong) {
	this.setAddress(new DccLocoAddress(number, isLong));
    }
    
    /**
     * public void setAddress(LocoAddress l)
     *
     * Set this VSDecoder's LocoAddress, and register to follow
     * events from the throttle with this address.
     *
     * @param l  (LocoAddress) LocoAddress to be followed
     * @return void
     */
    public void setAddress(LocoAddress l) {
	// Hack for ThrottleManager Dcc dependency
	config.setLocoAddress(l);
	DccLocoAddress dl = new DccLocoAddress(l.getNumber(), l.getProtocol());
	jmri.InstanceManager.throttleManagerInstance().attachListener(dl, new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
		    log.debug("property change name " + event.getPropertyName() + " old " + event.getOldValue() + " new " + event.getNewValue());
		    throttlePropertyChange(event);
		}
	    });
	log.debug("VSDecoder: Address set to " + config.getLocoAddress().toString());
    }
    
    /**
     * public LocoAddress getAddress()
     *
     * Get the currently assigned LocoAddress
     *
     * @return the currently assigned LocoAddress
     */
    public LocoAddress getAddress() {
	return(config.getLocoAddress());
    }

    /**
     * public float getMasterVolume()
     *
     * Get the current master volume setting for this VSDecoder
     *
     * @return (float) volume level (0.0 - 1.0)
     */
    public float getMasterVolume() {
	return(config.getVolume());
    }

    /**
     * public void setMasterVolume(float vol)
     *
     * Set the current master volume setting for this VSDecoder
     *
     * @param vol (float) volume level (0.0 - 1.0)
     * @return void
     */
    public void setMasterVolume(float vol) {
	log.debug("VSD: float volume = " + vol);
	config.setVolume(vol);
	for (VSDSound vs : sound_list.values()) {
	    vs.setVolume(vol);
	}
    }

    /**
     * public boolean isMuted()
     *
     * Is this VSDecoder muted?
     *
     * @return true if muted.
     */
    public boolean isMuted() {
	return(false);
    }

    /**
     * public void mute(boolean m)
     *
     * Mute or un-mute this VSDecoder
     *
     * @param m (boolean) true to mute, false to un-mute
     * @return void
     */
    public void mute(boolean m) {
	for (VSDSound vs : sound_list.values()) {
	    vs.mute(m);
	}
    }
    
    /**
     * public void setPosition(PhysicalLocation p)
     *
     * set the x/y/z position in the soundspace of this VSDecoder
     * Translates the given position to a position relative to the listener
     * for the component VSDSounds.
     *
     * The idea is that the user-preference Listener Position (relative to the
     * USER's chosen origin) is always the OpenAL Context's origin.
     *
     * @param p (PhysicalLocation) location relative to the user's chosen Origin.
     * @return void
     */
    public void setPosition(PhysicalLocation p) {
	// Store the actual position relative to the user's Origin locally.
	config.setPhysicalLocation(p);
	log.debug("( " + this.getAddress() + ") Set Position: " + p.toString());

	// Give all of the VSDSound objects the position translated relative to the listener position.
	// This is a workaround for OpenAL requiring the listener position to always be at (0,0,0).
	PhysicalLocation ref = VSDecoderManager.instance().getVSDecoderPreferences().getListenerPosition();
	if (ref == null)
	    ref = PhysicalLocation.Origin;
	for (VSDSound s : sound_list.values()) {
	    s.setPosition(PhysicalLocation.translate(p, ref));
	}
	fireMyEvent(new VSDecoderEvent(this, VSDecoderEvent.EventType.LOCATION_CHANGE, p));
    }

    /**
     * public PhysicalLocation getPosition()
     *
     * Get the current x/y/z position in the soundspace of this VSDecoder
     *
     * @return PhysicalLocation location of this VSDecoder
     */
    public PhysicalLocation getPosition() {
	return(config.getPhysicalLocation());
    }

    /**
     * public void propertyChange(PropertyChangeEvent evt)
     *
     * Respond to property change events from this VSDecoder's GUI
     *
     * @param evt (PropertyChangeEvent) event to respond to
     * @return void
     */
    @SuppressWarnings("cast")
    public void propertyChange(PropertyChangeEvent evt) {
	// Respond to events from the new GUI.
	if (evt.getSource() instanceof VSDControl) {
	    // Nothing to do... yet...
	    return;
	}

	// Respond to events from the old GUI.
	String property = evt.getPropertyName();
	if (property.equals(VSDecoderPane.PCIDMap.get(VSDecoderPane.PropertyChangeID.ADDRESS_CHANGE))) {
	    log.debug("Decoder set address = " + (LocoAddress)evt.getNewValue());
	    this.setAddress((LocoAddress)evt.getNewValue());
	    this.enable();
	} else if (property.equals(VSDecoderPane.PCIDMap.get(VSDecoderPane.PropertyChangeID.MUTE))) {
	    log.debug("VSD: Mute change. value = " + evt.getNewValue());
	    Boolean b = (Boolean)evt.getNewValue();
	    this.mute(b.booleanValue());
	} else if (property.equals(VSDecoderPane.PCIDMap.get(VSDecoderPane.PropertyChangeID.VOLUME_CHANGE))) {
	    log.debug("VSD: Volume change. value = " + evt.getNewValue());
	    // Slider gives integer 0-100.  Need to change that to a float 0.0-1.0
	    this.setMasterVolume((1.0f * (Integer)evt.getNewValue())/100.0f);
	} else if (property.equals(Train.TRAIN_LOCATION_CHANGED_PROPERTY)) {
	    PhysicalLocation p = getTrainPosition((Train)evt.getSource());
	    if (p != null)
		this.setPosition(getTrainPosition((Train)evt.getSource()));
	    else {
		log.debug("Train has null position");
		this.setPosition(new PhysicalLocation());
	    }
	} else if (property.equals(Train.STATUS_CHANGED_PROPERTY))  {
	    String status = (String)evt.getNewValue();
	    log.debug("Train status changed: " + status);
	    log.debug("New Location: " + getTrainPosition((Train)evt.getSource()));
	    if ((status.startsWith(Train.BUILT)) || (status.startsWith(Train.PARTIALBUILT))){ 
		log.debug("Train built. status = " + status);
		PhysicalLocation p = getTrainPosition((Train)evt.getSource());
		if (p != null)
		    this.setPosition(getTrainPosition((Train)evt.getSource()));
		else {
		    log.debug("Train has null position");
		    this.setPosition(new PhysicalLocation());
		}
	    }
	}
    }

    // Methods for handling location tracking based on JMRI Operations
    
    /** 
     * protected PhysicalLocation getTrainPosition(Train t) 
     *
     * Get the physical location of the given Operations Train
     *
     * @param t (Train) the Train to interrogate
     * @return PhysicalLocation location of the train
     */
    protected PhysicalLocation getTrainPosition(Train t) {
	if (t == null) {
	    log.debug("Train is null.");
	    return(null);
	}
	RouteLocation rloc = t.getCurrentLocation();
	if (rloc == null) {
	    log.debug("RouteLocation is null.");
	    return(null);
	}
	Location loc = rloc.getLocation();
	if (loc == null) {
	    log.debug("Location is null.");
	    return(null);
	}
	return(loc.getPhysicalLocation());
    }

    // Methods for handling the underlying sounds

    /**
     * public VSDSound getSound(String name)
     *
     * Retrieve the VSDSound with the given system name
     *
     * @param name (String) System name of the requested VSDSound
     * @return VSDSound the requested sound
     */
    public VSDSound getSound(String name) {
	return(sound_list.get(name));
    }

    /**
     * public void toggleBell()
     *
     * Turn the bell sound on/off
     *
     * @return void
     */
    public void toggleBell() {
	VSDSound snd = sound_list.get("BELL");
        if(snd.isPlaying())
            snd.stop();
        else
            snd.loop();
    }
    
    /**
     * public void toggleHorn()
     *
     * Turn the horn sound on/off
     *
     * @return void
     */
    public void toggleHorn() {
	VSDSound snd = sound_list.get("HORN");
        if(snd.isPlaying())
            snd.stop();
        else
            snd.loop();
    }

    /**
     * public void playHorn()
     *
     * Turn the horn sound on
     *
     * @return void
     */
    public void playHorn() {
	VSDSound snd = sound_list.get("HORN");
	snd.loop();
    }

    /**
     * public void shortHorn()
     *
     * Turn the horn sound on (Short burst)
     *
     * @return void
     */
    public void shortHorn() {
	VSDSound snd = sound_list.get("HORN");
	snd.play();
    }

    /**
     * public void stopHorn()
     *
     * Turn the horn sound off
     *
     * @return void
     */
    public void stopHorn() {
	VSDSound snd = sound_list.get("HORN");
	snd.stop();
    }

    // Java Bean set/get Functions

    /**
     * public void setProfileName(String pn)
     *
     * Set the profile name to the given string
     *
     * @param pn (String) : name of the profile to set
     * @return void
     */
    public void setProfileName(String pn) {
	config.setProfileName(pn);
    }

    /**
     * public String getProfileName()
     *
     * get the currently selected profile name
     *
     * @return (String) name of the currently selected profile
     */
    public String getProfileName() {
	return(config.getProfileName());
    }

    /**
     * public void enable()
     *
     * Enable this VSDecoder
     *
     * @return void
     */
    public void enable() {
	enabled = true;
    }

    /**
     * public void disable()
     *
     * Disable this VSDecoder
     *
     * @return void
     */
    public void disable() {
	enabled = false;
    }

    /**
     * public Collection<SoundEvent> getEventList()
     *
     * Get a Collection of SoundEvents associated with this VSDecoder
     *
     * @return Collection<SoundEvent> collection of SoundEvents
     */

    public Collection<SoundEvent> getEventList() {
	return(event_list.values());
    }
    
    /**
     * public boolean isDefault()
     *
     * True if this is the default VSDecoder
     *
     * @return boolean true if this is the default VSDecoder
     */
    public boolean isDefault() {
	return(is_default);
    }

    /**
     * public void isDefault(boolean d)
     *
     * Set whether this is the default VSDecoder or not
     *
     * @param d (boolean) True to set this as the default, False if not.
     * @return void
     */
    public void setDefault(boolean d) {
	is_default = d;
    }

    /**
     * public Element getXML()
     *
     * Get an XML representation of this VSDecoder
     * Includes a subtree of Elements for all of the
     * associated SoundEvents, Triggers, VSDSounds, etc.
     *
     * @return Element XML Element for this VSDecoder
     */
    public Element getXml() {
	Element me = new Element("vsdecoder");
	ArrayList<Element> le = new ArrayList<Element>();

	me.setAttribute("name", this.config.getProfileName());

	// If this decoder is marked as default, add the default Element.
	if (is_default)
	    me.addContent(new Element("default"));
	
	for (SoundEvent se : event_list.values()) {
	    le.add(se.getXml());
	}

	for (VSDSound vs : sound_list.values()) {
	    le.add(vs.getXml());
	}

	for (Trigger t : trigger_list.values()) {
	    le.add(t.getXml());
	}

	
	me.addContent(le);

	// Need to add whatever else here.

	return(me);
    }

    /*
    @Deprecated
    public void setXml(Element e) {
	this.setXml(e, null);
    }

    @Deprecated
    public void setXml(Element e, VSDFile vf) {
	this.setXml(vf);
    }

    @Deprecated
    public void setXml(VSDFile vf) { }
    */

    /**
     * public void setXML(VSDFile vf, String pn)
     *
     * Build this VSDecoder from an XML representation
     *
     * @param vf (VSDFile) : VSD File to pull the XML from
     * @param pn (String) : Parameter Name to find within the VSD File.
     * @return void
     */
    @SuppressWarnings({"unchecked", "cast"})
    public void setXml(VSDFile vf, String pn) {
	Iterator<Element> itr;
	Element e = null;
	Element el = null;
	SoundEvent se;
        
        if (vf == null) {
            log.debug("Null VSD File Name");
            return;
        }
	
        log.debug("VSD File Name = " + vf.getName());
	// need to choose one.
	this.setVSDFilePath(vf.getName());

	// Find the <profile/> element that matches the name pn
	//List<Element> profiles = vf.getRoot().getChildren("profile");
	//java.util.Iterator i = profiles.iterator();
	java.util.Iterator<Element> i = vf.getRoot().getChildren("profile").iterator();
	while (i.hasNext()) {
	    e = i.next();
	    if (e.getAttributeValue("name").equals(pn))
		break;
	}
	// E is now the first <profile/> in vsdfile that matches pn.

        if (e == null) {
	    // No matching profile name found.
            return;
        }
        
	// Set this decoder's name.
	this.setProfileName(e.getAttributeValue("name"));
	log.debug("Decoder Name = " + e.getAttributeValue("name"));


	// Read and create all of its components.

	// Check for default element.
	if (e.getChild("default") != null) {
	    log.debug("" + getProfileName() + "is default.");
	    is_default = true;
	}
	else {
	    is_default = false;
	}

	// +++ DEBUG
	// Log and print all of the child elements.
	itr = (e.getChildren()).iterator();
	while(itr.hasNext()) {
	    // Pull each element from the XML file.
	    el = itr.next();
	    log.debug("Element: " + el.toString());
	    if (el.getAttribute("name") != null) {
		log.debug("  Name: " + el.getAttributeValue("name"));
		log.debug("   type: " + el.getAttributeValue("type"));
	    }
	}
	// --- DEBUG


	// First, the sounds.
	itr = (e.getChildren("sound")).iterator();
	while(itr.hasNext()) {
	    el = (Element)itr.next();
	    if (el.getAttributeValue("type") == null) {
		// Empty sound.  Skip.
		log.debug("Skipping empty Sound.");
		continue;
	    } else if (el.getAttributeValue("type").equals("configurable")) {
		// Handle configurable sounds.
		ConfigurableSound cs = new ConfigurableSound(el.getAttributeValue("name"));
		cs.setXml(el, vf);
		sound_list.put(el.getAttributeValue("name"),cs);
	    } else if (el.getAttributeValue("type").equals("diesel")) {
		// Handle a Diesel Engine sound
		DieselSound es = new DieselSound(el.getAttributeValue("name"));
		es.setXml(el, vf);
		sound_list.put(el.getAttributeValue("name"), es);
	    } else if (el.getAttributeValue("type").equals("diesel2")) {
		// Handle a Diesel Engine sound
		Diesel2Sound es = new Diesel2Sound(el.getAttributeValue("name"));
		es.setXml(el, vf);
		sound_list.put(el.getAttributeValue("name"), es);
	    } else if (el.getAttributeValue("type").equals("steam")) {
		// Handle a Diesel Engine sound
		SteamSound es = new SteamSound(el.getAttributeValue("name"));
		es.setXml(el, vf);
		sound_list.put(el.getAttributeValue("name"), es);
	    } else {
		//TODO: Some type other than configurable sound.  Handle appropriately
	    }
	}

	// Next, grab all of the SoundEvents
	// Have to do the sounds first because the SoundEvent's setXml() will
	// expect to be able to look it up.
	itr = (e.getChildren("sound-event")).iterator();
	while (itr.hasNext()) {
	    el = (Element)itr.next();
	    switch(SoundEvent.ButtonType.valueOf(el.getAttributeValue("buttontype").toUpperCase())) {
	    case MOMENTARY:
		se = new MomentarySoundEvent(el.getAttributeValue("name"));
		break;
	    case TOGGLE:
		se = new ToggleSoundEvent(el.getAttributeValue("name"));
		break;
	    case ENGINE:
		se = new EngineSoundEvent(el.getAttributeValue("name"));
		break;
	    case NONE:
	    default:
		se = new SoundEvent(el.getAttributeValue("name"));
	    }
	    se.setParent(this);
	    se.setXml(el, vf);
	    event_list.put(se.getName(), se);
	}

	// Handle other types of children similarly here.
	
    }

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(VSDecoder.class.getName());

}
