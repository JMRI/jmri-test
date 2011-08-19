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

import jmri.jmrit.XmlFile;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

import java.util.ResourceBundle;
import java.util.Enumeration;

import org.jdom.Element;

import java.util.zip.ZipEntry;


/**
 *  Load VSDecoder Profiles from XML
 *
 * Adapted from LoadXmlThrottleProfileAction
 * by Glen Oberhauser (2004)
 *
 * @author     Mark Underwood 2011
 * @version     $Revision$
 */
public class LoadVSDFileAction extends AbstractAction {
    static final ResourceBundle rb = VSDecoderBundle.bundle();
    
    /**
     *  Constructor
     *
     * @param  s  Name for the action.
     */
    public LoadVSDFileAction(String s) {
	super(s);
    }
    
    public LoadVSDFileAction() {
	this(rb.getString("LoadVSDFileChoserTitle")); // Shouldn't this be in the resource bundle?
    }
    
    JFileChooser fileChooser;
    
    /**
     *  The action is performed. Let the user choose the file to load from. Read
     *  XML for each VSDecoder Profile.
     *
     * @param  e  The event causing the action.
     */
    public void actionPerformed(ActionEvent e) {
	if (fileChooser == null) {
	    String default_dir = VSDecoderManager.instance().getVSDecoderPreferences().getDefaultVSDFilePath();
	    log.debug("Default path: " + default_dir);
	    fileChooser = new JFileChooser(VSDecoderManager.instance().getVSDecoderPreferences().getDefaultVSDFilePath());
	    jmri.util.FileChooserFilter filt = new jmri.util.FileChooserFilter(rb.getString("LoadVSDFileChooserFilterLabel"));
	    filt.addExtension("vsd");
	    filt.addExtension("zip");
	    fileChooser.setFileFilter(filt);
	    fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
	    fileChooser.setCurrentDirectory(new File(VSDecoderManager.instance().getVSDecoderPreferences().getDefaultVSDFilePath()));
	}
	int retVal = fileChooser.showOpenDialog(null);
	if (retVal != JFileChooser.APPROVE_OPTION) {
	    return;
	    // give up if no file selected
	}

	loadVSDFile(fileChooser.getSelectedFile());
    }
    
    
    @SuppressWarnings("unchecked")
    public static boolean loadVSDFile(java.io.File f) {
	VSDFile vsdfile;
	// Create a VSD (zip) file.
	try {
	    vsdfile = new VSDFile(f);
	    log.debug("VSD File name = " + vsdfile.getName());
	    if (vsdfile.isInitialized()) {	
		Element root = vsdfile.getRoot();
		VSDecoderManager.instance().loadProfiles(vsdfile);
	    }
	    // Cleanup and close files.
	    vsdfile.close();

	    return(vsdfile.isInitialized());

	} catch (java.util.zip.ZipException ze) {
	    log.error("ZipException opening file " + f.toString(), ze);
	    return(false);
	} catch (java.io.IOException ze) {
	    log.error("IOException opening file " + f.toString(), ze);
	    return(false);
	}
    }
    
    public static boolean loadVSDFile(String fp) {
	VSDFile vsdfile;

	try {
	    // Create a VSD (zip) file.
	    vsdfile = new VSDFile(fp);
	    log.debug("VSD File name = " + vsdfile.getName());
	    if (vsdfile.isInitialized()) {	
	    Element root = vsdfile.getRoot();
	    VSDecoderManager.instance().loadProfiles(vsdfile);
	}
	    // Cleanup and close files.
	    vsdfile.close();
	    
	    return(vsdfile.isInitialized());
	} catch (java.util.zip.ZipException ze) {
	    log.error("ZipException opening file " + fp, ze);
	    return(false);
	} catch (java.io.IOException ze) {
	    log.error("IOException opening file " + fp, ze);
	    return(false);
	}

	/*
	File f = null;
	try {
	    f = new File(fp);
	    return(loadVSDFile(f));
	} catch (java.io.IOException ioe) {
	    log.warn("IO Error auto-loading VSD File: " + (f==null?"(null)":f.getAbsolutePath()) + " ", ioe);
	    return(false);
	} catch (NullPointerException npe) {
	    log.warn("NP Error auto-loading VSD File: FP = " + fp, npe);
	    return(false);
	}
	*/
    }

	// initialize logging
	private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LoadVSDFileAction.class.getName());

}
