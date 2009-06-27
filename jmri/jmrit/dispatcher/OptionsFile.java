// OptionsFile.java

package jmri.jmrit.dispatcher;

import jmri.Scale;
import jmri.jmrit.XmlFile;
import jmri.jmrit.display.LayoutEditor;
import java.io.File;

import java.util.ArrayList;
import java.util.ResourceBundle;

import org.jdom.Document;
import org.jdom.Element;

/**
 * Handles reading and writing of Dispatcher options to disk as an XML file
 *    called "dispatcher-options.xml" in the user's preferences area
 * <p>
 * This class manipulates the files conforming to the dispatcher-options DTD
 * <p>
 * The file is written when the user requests that options be saved. If the 
 *    dispatcher-options.xmlfile is present when Dispatcher is requested, it is 
 *    read and options set accordingly
 *
 * <P>
 * This file is part of JMRI.
 * <P>
 * JMRI is open source software; you can redistribute it and/or modify it 
 * under the terms of version 2 of the GNU General Public License as 
 * published by the Free Software Foundation. See the "COPYING" file for 
 * a copy of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 *
 * @author			Dave Duchamp    Copyright (C) 2008
 * @version			$Revision: 1.6 $
 */

public class OptionsFile extends jmri.jmrit.XmlFile {

	static final ResourceBundle rb = ResourceBundle
			.getBundle("jmri.jmrit.dispatcher.DispatcherBundle");

	public OptionsFile () {
		super();
	}
	
	// operational variables
	protected DispatcherFrame dispatcher = null;
	private static String defaultFileName = XmlFile.prefsDir()+"dispatcheroptions.xml";
	private Document doc = null;
	private Element root = null;
	
	/*
	 *  Reads Dispatcher Options from a file in the user's preferences directory
	 *  If the file containing Dispatcher Options does not exist this routine returns quietly.
	 */
	public void readDispatcherOptions(DispatcherFrame f) throws org.jdom.JDOMException, java.io.IOException {
		log.debug("entered readDispatcherOptions");
		// check if file exists
		if (checkFile(defaultFileName)) {
			// file is present, 
			root = rootFromName(defaultFileName);
			dispatcher = f;
			if (root!=null) {
				// there is a file
				Element options = root.getChild("options");
				if (options!=null) {	
					// there are options defined, read and set Dispatcher options
					if (options.getAttribute("lename")!=null) {
						// there is a layout editor name selected
						String leName = options.getAttribute("lename").getValue();
						// get list of Layout Editor panels
						ArrayList<LayoutEditor> layoutEditorList = jmri.jmrit.display.
												PanelMenu.instance().getLayoutEditorPanelList();
						if (layoutEditorList.size()==0) {
							log.warn("Dispatcher options specify a Layout Editor panel that is not present.");
						}
						else {
							boolean found = false;
							for (int i = 0; i<layoutEditorList.size(); i++) {
								if (leName.equals(layoutEditorList.get(i).getTitle())) {
									found = true;
									dispatcher.setLayoutEditor(layoutEditorList.get(i));
								}
							}
							if (!found) {
								log.warn("Layout Editor panel - "+leName+" - not found.");
							}									
						}
					}
					if (options.getAttribute("useconnectivity")!=null) {
						dispatcher.setUseConnectivity(true);
						if (options.getAttribute("useconnectivity").getValue().equals("no"))
							dispatcher.setUseConnectivity(false);
					}
					if (options.getAttribute("trainsfromroster")!=null) {
						dispatcher.setTrainsFromRoster(true);
						if (options.getAttribute("trainsfromroster").getValue().equals("no"))
							dispatcher.setTrainsFromRoster(false);
					}
					if (options.getAttribute("trainsfromtrains")!=null) {
						dispatcher.setTrainsFromTrains(true);
						if (options.getAttribute("trainsfromtrains").getValue().equals("no"))
							dispatcher.setTrainsFromTrains(false);
					}
					if (options.getAttribute("trainsfromuser")!=null) {
						dispatcher.setTrainsFromUser(true);
						if (options.getAttribute("trainsfromuser").getValue().equals("no"))
							dispatcher.setTrainsFromUser(false);
					}
					if (options.getAttribute("autoallocate")!=null) {
						dispatcher.setAutoAllocate(true);
						if (options.getAttribute("autoallocate").getValue().equals("no"))
							dispatcher.setAutoAllocate(false);
					}
					if (options.getAttribute("autoturnouts")!=null) {
						dispatcher.setAutoTurnouts(true);
						if (options.getAttribute("autoturnouts").getValue().equals("no"))
							dispatcher.setAutoTurnouts(false);
					}
					if (options.getAttribute("hasoccupancydetection")!=null) {
						dispatcher.setHasOccupancyDetection(true);
						if (options.getAttribute("hasoccupancydetection").getValue().equals("no"))
							dispatcher.setHasOccupancyDetection(false);
					}
					if (options.getAttribute("shortactivetrainnamesr")!=null) {
						dispatcher.setShortActiveTrainNames(true);
						if (options.getAttribute("shortactivetrainnames").getValue().equals("no"))
							dispatcher.setShortActiveTrainNames(false);
					}
					if (options.getAttribute("shortnameinblock")!=null) {
						dispatcher.setShortNameInBlock(true);
						if (options.getAttribute("shortnameinblock").getValue().equals("no"))
							dispatcher.setShortNameInBlock(false);
					}
					if (options.getAttribute("layoutscale")!=null) {
						String s = (options.getAttribute("layoutscale")).getValue();
						for (int i = 1; i<=Scale.NUM_SCALES; i++) {
							if (Scale.getShortScaleID(i).equals(s)) {
								dispatcher.setScale(i);
							}
						}
					}
				}
			}
		} 
	}
	
	/*
	 *  Writes out Dispatcher options to a file in the user's preferences directory
	 */
	public void writeDispatcherOptions(DispatcherFrame f) throws java.io.IOException {
		log.debug("entered writeDispatcherOptions");
		dispatcher = f;
		root = new Element("dispatcheroptions");
		doc = newDocument(root, dtdLocation+"dispatcher-options.dtd");
		// add XSLT processing instruction
		// <?xml-stylesheet type="text/xsl" href="XSLT/block-values.xsl"?>
		java.util.Map<String,String> m = new java.util.HashMap<String,String>();
		m.put("type", "text/xsl");
		m.put("href", xsltLocation+"dispatcheroptions.xsl");
		org.jdom.ProcessingInstruction p = new org.jdom.ProcessingInstruction("xml-stylesheet", m);
		doc.addContent(0,p);
			
		// save Dispatcher Options in xml format
		Element options = new Element("options");
		LayoutEditor le = dispatcher.getLayoutEditor();
		if (le!=null) {
			options.setAttribute("lename", le.getTitle());
		}
		options.setAttribute("useconnectivity", ""+(dispatcher.getUseConnectivity()?"yes":"no"));
		options.setAttribute("trainsfromroster", ""+(dispatcher.getTrainsFromRoster()?"yes":"no"));
		options.setAttribute("trainsfromtrains", ""+(dispatcher.getTrainsFromTrains()?"yes":"no"));
		options.setAttribute("trainsfromuser", ""+(dispatcher.getTrainsFromUser()?"yes":"no"));
		options.setAttribute("autoallocate", ""+(dispatcher.getAutoAllocate()?"yes":"no"));
		options.setAttribute("autoturnouts", ""+(dispatcher.getAutoTurnouts()?"yes":"no"));
		options.setAttribute("hasoccupancydetection", ""+(dispatcher.getHasOccupancyDetection()?"yes":"no"));
		options.setAttribute("shortactivetrainnames", ""+(dispatcher.getShortActiveTrainNames()?"yes":"no"));
		options.setAttribute("shortnameinblock", ""+(dispatcher.getShortNameInBlock()?"yes":"no"));
		options.setAttribute("layoutscale", Scale.getShortScaleID(dispatcher.getScale()));
		root.addContent(options);
			
		// write out the file
		try {
			if (!checkFile(defaultFileName)) {
				// file does not exist, create it
				File file = new File(defaultFileName);
				file.createNewFile();
			}
			// write content to file
			writeXML(findFile(defaultFileName),doc);
		}
		catch (java.io.IOException ioe) {
			log.error("IO Exception "+ioe);
			throw (ioe);
		}
	}
  
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(OptionsFile.class.getName());
}

/* @(#)OptionsFile.java */
