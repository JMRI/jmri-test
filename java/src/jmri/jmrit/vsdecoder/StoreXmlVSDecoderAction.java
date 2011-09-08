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

import jmri.configurexml.StoreXmlConfigAction;
import jmri.jmrit.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;

import java.util.*;

import org.jdom.*;
import org.jdom.output.*;

/**
 * Save throttles to XML
 *
 * @author			Glen Oberhauser
 * @author Daniel Boudreau (C) Copyright 2008
 * @version     $Revision$
 */
@SuppressWarnings("serial")
public class StoreXmlVSDecoderAction extends AbstractAction {

    static final ResourceBundle rb = VSDecoderBundle.bundle();
    
    /**
     * Constructor
     * @param s Name for the action.
     */
    public StoreXmlVSDecoderAction(String s) {
	super(s);
	// disable this ourselves if there is no throttle Manager
	/*
	if (jmri.InstanceManager.throttleManagerInstance() == null) {
	    setEnabled(false);
	}
	*/
    }
    
    public StoreXmlVSDecoderAction() {
        this("Save Virtual Sound Decoder profile...");
    }
    
    /**
     * The action is performed. Let the user choose the file to save to.
     * Write XML for each ThrottleFrame.
     * @param e The event causing the action.
	 */
    public void actionPerformed(ActionEvent e) {
	JFileChooser fileChooser = jmri.jmrit.XmlFile.userFileChooser(rb.getString("PromptXmlFileTypes"), "xml");
	fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
	fileChooser.setCurrentDirectory(new File( VSDecoderPane.getDefaultVSDecoderFolder()));
	java.io.File file = StoreXmlConfigAction.getFileName(fileChooser);
	if (file == null)
	    return;

	saveVSDecoderProfile(file);
    }
    
    public void saveVSDecoderProfile(java.io.File f) {
	
	try {
	    Element root = new Element("VSDecoderConfig");
	    Document doc = XmlFile.newDocument(root, XmlFile.dtdLocation+ "vsdecoder-config.dtd");
	    
	    // add XSLT processing instruction
	    // <?xml-stylesheet type="text/xsl" href="XSLT/throttle-layout-config.xsl"?>
	    /*TODO			java.util.Map<String,String> m = new java.util.HashMap<String,String>();
	      m.put("type", "text/xsl");
	      m.put("href", jmri.jmrit.XmlFile.xsltLocation + "throttle-layout-config.xsl");
	      ProcessingInstruction p = new ProcessingInstruction("xml-stylesheet", m);
	      doc.addContent(0, p); */
	    
	    java.util.ArrayList<Element> children = new java.util.ArrayList<Element>(5);

	    for (java.util.Iterator<VSDecoder> i = VSDecoderManager.instance().getVSDecoderList().iterator(); i.hasNext();) {
		VSDecoder vsd = i.next();
		children.add(vsd.getXml());
	    }

	    
	    // Throttle-specific stuff below.  Kept for reference
	    /*
	    // throttle list window
	    children.add(ThrottleFrameManager.instance().getThrottlesListPanel().getXml() );
	    
	    // throttle windows
	    for (Iterator<ThrottleWindow> i = ThrottleFrameManager.instance().getThrottleWindows(); i.hasNext();) {
		ThrottleWindow tw = i.next();
		Element throttleElement = tw.getXml();
		children.add(throttleElement);
	    }
	    */
	    // End Throttle-specific stuff.

	    root.setContent(children);
	    
	    FileOutputStream o = new java.io.FileOutputStream(f);
	    try {
		XMLOutputter fmt = new XMLOutputter();
		fmt.setFormat(org.jdom.output.Format.getPrettyFormat());
		fmt.output(doc, o);
	    } catch (IOException ex) {
		log.warn("Exception in storing VSDecoder xml: " + ex);
	    } finally {
		o.close();
            }
	} catch (FileNotFoundException ex) {
	    log.warn("Exception in storing VSDecoder xml: " + ex);
	} catch (IOException ex) {
	    log.warn("Exception in storing VSDecoder xml: " + ex);
	}
    }
    
    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(StoreXmlVSDecoderAction.class.getName());
    
}
