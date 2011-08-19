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

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import jmri.jmrit.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import jmri.ThrottleListener;
import jmri.DccThrottle;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.util.swing.*;

import java.io.File;
import jmri.jmrit.XmlFile;
import java.util.ResourceBundle;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Arrays;


/**
 * Virtual Sound Decoder for playing sounds off of LocoNet messages.
 * Based on the LocoMon tool by Bob Jacobsen
 * @author	   Mark Underwood   Copyright (C) 2011
 * @version   $Revision$
 */
public class VSDecoderPane extends JmriPanel {

    //private static final ResourceBundle vsdBundle = VSDecoderBundle.bundle();

    public static enum PropertyChangeID { ADDRESS_CHANGE, PROFILE_SELECT }

    private static final Map<PropertyChangeID, String> PCIDMap;
    static {
	Map<PropertyChangeID, String> aMap = new HashMap<PropertyChangeID, String>();
	aMap.put(PropertyChangeID.ADDRESS_CHANGE, "AddressChange");
	aMap.put(PropertyChangeID.PROFILE_SELECT, "ProfileSelect");
	PCIDMap = Collections.unmodifiableMap(aMap);
    }

    String decoder_id;
    VSDecoderManager decoder_mgr;

    final static String BASICPANEL = "Basic";
    final static String COMBOPANEL = "Sounds";
    final static String OPTIONPANEL = "Options";

    // GUI Components

    private VSDecoderFrame parent;

    private JTabbedPane tabbedPane;
    private VSDConfigPanel configPanel;
    private JPanel soundsPanel;
    private JPanel optionPanel;

    private static String VSDecoderFileLocation = null;

    //private List<JMenu> menuList;

    public VSDecoderPane(VSDecoderFrame p) {
        super();
	parent = p;
	decoder_mgr = VSDecoderManager.instance();
    }

    
    public String getHelpTarget() { return "package.jmri.jmrix.vsdecoder.VSDecoderPane"; }
    /*
    public String getTitle() { 
        return LocoNetBundle.bundle().getString("MenuItemVirtualSoundDecoder");
    }
    */

    public static String getDefaultVSDecoderFolder() {
        if (VSDecoderFileLocation == null)
            return XmlFile.prefsDir()+"vsdecoder"+File.separator ;
        return VSDecoderFileLocation;
    }



    public void init() {}
    
    public void initContext(Object context) {
	//initComponents();
	/*
        if (context instanceof LocoNetSystemConnectionMemo ) {
            initComponents((LocoNetSystemConnectionMemo) context);
        }
	*/
    }

    public void initComponents() {
	log.debug("initComponents()");
	//buildMenu();

	setLayout(new BorderLayout(10, 10));
	setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

	// Add the tabbed pane to the VSDecoderPane.  The tabbedPane will contain all the other panes.
	tabbedPane = new JTabbedPane();
	add(tabbedPane);

	//-------------------------------------------------------------------
	// configPanel
	// The configPanel holds the stuff for addressing and configuration.
        configPanel = new VSDConfigPanel(decoder_id, this);

	tabbedPane.addTab("Config", configPanel);

	//-------------------------------------------------------------------
	// soundsPanel
	// The optionPanel holds controls for selecting sound options.
	optionPanel = new VSDOptionPanel(decoder_id, this);

	tabbedPane.addTab("Options", optionPanel);
	

	//-------------------------------------------------------------------
	// soundsPanel
	// The soundsPanel holds buttons for specific sounds.
        soundsPanel = new VSDSoundsPanel(decoder_id, this);
	tabbedPane.addTab("Sounds", soundsPanel);
    }

    // PROPERTY CHANGE EVENT FUNCTIONS

    // VSDecoderManager Events
    public void addPropertyChangeListener(PropertyChangeListener listener) {
	List<PropertyChangeListener> l = Arrays.asList(listenerList.getListeners(PropertyChangeListener.class));
	if (!l.contains(listener))
	    listenerList.add(PropertyChangeListener.class, listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
	listenerList.remove(PropertyChangeListener.class, listener);
    }

    public void firePropertyChange(PropertyChangeID id, Object oldProp, Object newProp) {
	String pcname;

	// map the property change ID
	pcname = PCIDMap.get(id);
	// Fire the actual PropertyChangeEvent
	firePropertyChange(new PropertyChangeEvent(this, pcname, oldProp, newProp));
    }

    void firePropertyChange(PropertyChangeEvent evt) {
	//Object[] listeners = listenerList.getListenerList();

	for (PropertyChangeListener l : listenerList.getListeners(PropertyChangeListener.class)) {
	    l.propertyChange(evt);
	}
    }

    public VSDecoder getDecoder() {
	VSDecoder d = VSDecoderManager.instance().getVSDecoderByID(decoder_id);
	addPropertyChangeListener(d);
	return(d);
    }

    public VSDecoder getDecoder(String profile) {
	VSDecoder d = VSDecoderManager.instance().getVSDecoder(profile);
	addPropertyChangeListener(d);
	return(d);
    }

    public void setDecoder(VSDecoder dec) {
	if (dec != null) {
	    // Store the new decoder
	    decoder_id = dec.getID();
	    log.debug("Decoder ID = " + decoder_id + " Decoder = " + dec);
	    // Update the sounds pane
	    tabbedPane.remove(soundsPanel);
	    soundsPanel = new VSDSoundsPanel(decoder_id, this);
	    tabbedPane.addTab("Sounds", soundsPanel);
	    tabbedPane.revalidate();
	    tabbedPane.repaint();
	}
	
    }

    public void setAddress(DccLocoAddress a) {
	if (a != null) {
	    VSDecoder decoder = VSDecoderManager.instance().getVSDecoderByID(decoder_id);
	    decoder.setAddress(a);
	    decoder.enable();
	    this.setTitle(a);
	}
    }

    public void setTitle(DccLocoAddress a) {
	if (a != null) {
	    parent.setTitle("VSDecoder - " + a.toString());
	}
    }


    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(VSDecoderPane.class.getName());
}
