/** 
 * PaneProgFrame.java
 *
 * Description:		Frame providing a command station programmer from decoder definition files
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			
 */

package jmri.jmrit.symbolicprog.tabbedframe;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

import java.io.*;
import com.sun.java.util.collections.List;
import com.sun.java.util.collections.ArrayList;

import jmri.Programmer;
import jmri.ProgListener;
import jmri.ProgModePane;
import jmri.jmrit.symbolicprog.*;
import jmri.jmrit.decoderdefn.*;
import jmri.jmrit.roster.*;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.Attribute;
import org.jdom.DocType;
import org.jdom.output.XMLOutputter;
import org.jdom.JDOMException;
import org.jdom.input.DOMBuilder;
import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;

public class PaneProgFrame extends javax.swing.JFrame 
							implements java.beans.PropertyChangeListener  {

	// members to contain working variable, CV values
	JLabel 				progStatus     	= new JLabel("idle");
	CvTableModel		cvModel			= new CvTableModel(progStatus);
	VariableTableModel  variableModel	= new VariableTableModel(progStatus,
														new String[]  {"Name", "Value"},
														cvModel);
	RosterEntry _rosterEntry = null;
	RosterEntryPane _rPane = null;
	
	List paneList = new ArrayList();
	
	// GUI member declarations
	JTabbedPane tabPane = new JTabbedPane();
	JButton readAll = new JButton("Read all");
	JButton writeAll = new JButton("Write all");
	JButton confirmAll = new JButton("Confirm all");
	
	protected void installComponents() {
		// configure GUI elements
		confirmAll.setEnabled(false);
		confirmAll.setToolTipText("disabled because not yet implemented");
		
		// general GUI config
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		readAll.addActionListener( new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				readAll();
			}
		});
		writeAll.addActionListener( new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				writeAll();
			}
		});

		// most of the GUI is done from XML in readConfig() function
		getContentPane().add(tabPane);
		
		// add buttons
		JPanel bottom = new JPanel();
		bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
		bottom.add(readAll);
		bottom.add(confirmAll);
		bottom.add(writeAll);
		getContentPane().add(bottom);
		
		getContentPane().add(progStatus);
		// pack  - this should be done again later after config
		pack();
	}
	
	// ctors
	public PaneProgFrame() {
		super();
		installComponents();
	}
  		
	public PaneProgFrame(DecoderFile decoderFile, String locoFile, RosterEntry r, String name) {
		super(name);
		installComponents();

		if (locoFile != null) readLocoFile(locoFile);  // read, but don't process

		if (decoderFile != null) loadDecoderFile(decoderFile);
		else					 loadDecoderFromLoco(r);

		// finally fill the CV values
		if (locoFile != null) loadLocoFile();
		
		// and build the GUI
		loadProgrammerFile(r);
	}
  	
	Namespace lns = null;
	Element lroot = null;
	
  	protected void readLocoFile(String locoFile) {
  		if (locoFile == null) {
  			log.info("loadLocoFile file invoked with null filename");
  			return;
  		}
		LocoFile lf = new LocoFile();  // used as a temporary
		lns = lf.getNamespace();
		lroot = null;
		try {
			lroot = lf.rootFromFile(lf.fileLocation+File.separator+locoFile, true);
		} catch (Exception e) { log.error("Exception while loading loco XML file: "+e); }
  	}
  	
  	protected void loadLocoFile() {
		// load CVs from the loco file tree
		LocoFile.loadCvModel(lroot.getChild("locomotive", lns), lns, cvModel);
  	}
  	
  	protected void loadDecoderFromLoco(RosterEntry r) {
  		// get a DecoderFile from the locomotive xml
		String decoderModel = r.getDecoderModel();
		String decoderFamily = r.getDecoderFamily();
		if (log.isDebugEnabled()) log.debug("selected loco uses decoder "+decoderFamily+" "+decoderModel);
		// locate a decoder like that.
		List l = DecoderIndexFile.instance().matchingDecoderList(null, decoderFamily, null, null, decoderModel);
		if (log.isDebugEnabled()) log.debug("found "+l.size()+" matches");
		if (l.size() > 0) {
			DecoderFile d = (DecoderFile)l.get(0);
			loadDecoderFile(d);
		} else {
			log.warn("Loco uses "+decoderFamily+" "+decoderModel+" decoder, but no such decoder defined");
		} 		
	}

  	protected void loadDecoderFile(DecoderFile df) {
  		if (df == null) {
  			log.warn("loadDecoder file invoked with null object");
  			return;
  		}
		
		Namespace dns = df.getNamespace();
		Element droot = null;
		try {
			droot = df.rootFromFile(df.fileLocation+File.separator+df.getFilename(), true);
		} catch (Exception e) { log.error("Exception while loading decoder XML file: "+e); }
		// load variables from decoder tree
		df.loadVariableModel(droot.getChild("decoder", dns), dns, variableModel);
  	}
  	
  	protected void loadProgrammerFile(RosterEntry r) {
		// Open and parse programmer file
		File pfile = new File("xml"+File.separator+"programmers"+File.separator+"MultiPane.xml");
		Namespace pns = Namespace.getNamespace("programmer",
										"http://jmri.sourceforge.net/xml/programmer");
		SAXBuilder pbuilder = new SAXBuilder(true);  // argument controls validation, on for now
		Document pdoc = null;
		try {
			pdoc = pbuilder.build(new FileInputStream(pfile),"xml"+File.separator);
		}
		catch (Exception e) {
			log.error("Exception in programmer SAXBuilder "+e);
		}
		// find root
		Element proot = pdoc.getRootElement();
					
		// load programmer config from programmer tree
		readConfig(proot, pns, r);
  	}
  	
	// handle resizing when first shown
  	private boolean mShown = false;
	public void addNotify() {
		super.addNotify();
		if (mShown)
			return;			
		// resize frame to account for menubar
		JMenuBar jMenuBar = getJMenuBar();
		if (jMenuBar != null) {
			int jMenuBarHeight = jMenuBar.getPreferredSize().height;
			Dimension dimension = getSize();
			dimension.height += jMenuBarHeight;
			setSize(dimension);
		}
		mShown = true;
	}

	// Close the window when the close box is clicked
	void thisWindowClosing(java.awt.event.WindowEvent e) {
		//OK, close
		setVisible(false);
		dispose();	
	}
	
	void readConfig(Element root, Namespace ns, RosterEntry r) {
		// check for "programmer" element at start
		Element base;	
		if ( (base = root.getChild("programmer", ns)) == null) {
			log.error("xml file top element is not programmer");
			return;
		}

		// for all "pane" elements ...
		List paneList = base.getChildren("pane",ns);
		for (int i=0; i<paneList.size(); i++) {
			// load each pane
			String name = ((Element)(paneList.get(i))).getAttribute("name").getValue();
			newPane( name, ((Element)(paneList.get(i))), ns);
		}
		
		// add the Info tab
		tabPane.addTab("Info", makeInfoPane(r));
	}
	
	protected JPanel makeInfoPane(RosterEntry r) {
		// create the identification pane (not configured by file now; maybe later?
		JPanel body = new JPanel();
		body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
		
		// add roster info
		_rPane = new RosterEntryPane(r);
		_rosterEntry = r;		
		_rPane.setMaximumSize(_rPane.getPreferredSize());
		body.add(_rPane);
		
		// add the store button
		JButton store = new JButton("      Store to file       ");
		store.addActionListener( new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				storeFile();
			}
		});
		body.add(store);
		
		// arrange for the dcc address to be updated
		java.beans.PropertyChangeListener dccNews = new java.beans.PropertyChangeListener() {
			public void propertyChange(java.beans.PropertyChangeEvent e) { updateDccAddress(); }
		};
		primaryAddr = findVar("Primary Address");
		if (primaryAddr==null) log.warn("DCC Address monitor didnt find a Primary Address variable");
		else primaryAddr.addPropertyChangeListener(dccNews);
		extendAddr = findVar("Extended Address");
		if (extendAddr==null) log.warn("DCC Address monitor didnt find an Extended Address variable");
		else extendAddr.addPropertyChangeListener(dccNews);
		addMode = findVar("Address Format");
		if (addMode==null) log.warn("DCC Address monitor didnt find an Address Format variable");
		else addMode.addPropertyChangeListener(dccNews);
		
		return body;
	}

	// hold refs to variables to check dccAddress
	VariableValue primaryAddr = null;
	VariableValue extendAddr = null;
	VariableValue addMode = null;
		
	void updateDccAddress() {
		if (log.isDebugEnabled()) 
			log.debug("updateDccAddress: primary "+(primaryAddr==null?"<null>":primaryAddr.getValueString())+
						" extended "+(extendAddr==null?"<null>":extendAddr.getValueString())+
						" mode "+(addMode==null?"<null>":addMode.getValueString()));
		String newAddr = null;
		if (addMode == null || extendAddr == null || !addMode.getValueString().equals("1")) {
			// short address mode
			if (primaryAddr != null && !primaryAddr.getValueString().equals(""))
				newAddr = primaryAddr.getValueString();
		}
		else {
			// long address
			if (extendAddr != null && !extendAddr.getValueString().equals(""))
				newAddr = extendAddr.getValueString();
		}
		// update if needed
		if (newAddr!=null) _rPane.setDccAddress(newAddr);
	}
		
	VariableValue findVar(String name) {
		for (int i=0; i<variableModel.getRowCount(); i++) 
			if (name.equals(variableModel.getStdName(i))) return variableModel.getVariable(i);
		for (int i=0; i<variableModel.getRowCount(); i++) 
			if (name.equals(variableModel.getName(i))) return  variableModel.getVariable(i);
		return null;
	}

	public void newPane(String name, Element pane, Namespace ns) {
	
		// create a panel to hold columns
		JPanel p = new PaneProgPane(name, pane, ns, cvModel, variableModel);
		
		// add the tab to the frame
		tabPane.addTab(name, p);
		
		// and remember it for programming
		paneList.add(p);
	}
	
	/**
	 * invoked by "Read All" button, this sets in motion a 
	 * continuing sequence of "read" operations on the 
	 * panes. Each invocation of this method reads one [ane; completion
	 * of that request will cause it to happen again, reading the next pane, until
	 * there's nothing left to read.
	 * <P>
	 * Returns true is a read has been started, false if the operation is complete.
	 */
	public boolean readAll() {
		if (log.isDebugEnabled()) log.debug("readAll starts");
		_read = true;
		for (int i=0; i<paneList.size(); i++) {
			if (log.isDebugEnabled()) log.debug("readAll calls readPane on "+i);
			_programmingPane = (PaneProgPane)paneList.get(i);
			if (_programmingPane.readPane()) {
				// operation in progress, register to hear results, then stop loop
			    _programmingPane.addPropertyChangeListener(this);
				if (log.isDebugEnabled()) log.debug("readAll expecting callback from readPane "+i);
				return true;
			}
		}
		// nothing to program, end politely
		_programmingPane = null;
		if (log.isDebugEnabled()) log.debug("readAll found nothing to do");
		return false;	
	}

	/**
	 * invoked by "Write All" button, this sets in motion a 
	 * continuing sequence of "write" operations on each pane.  
	 * Each invocation of this method writes one pane; completion
	 * of that request will cause it to happen again, writing the next pane, until
	 * there's nothing left to write.
	 * <P>
	 * Returns true is a write has been started, false if the operation is complete.
	 */
	public boolean writeAll() {
		if (log.isDebugEnabled()) log.debug("writeAll starts");
		_read = false;
		for (int i=0; i<paneList.size(); i++) {
			if (log.isDebugEnabled()) log.debug("writeAll calls writePane on "+i);
			_programmingPane = (PaneProgPane)paneList.get(i);
			if (_programmingPane.writePane()) {
				// operation in progress, register to hear results, then stop loop
			    _programmingPane.addPropertyChangeListener(this);
				if (log.isDebugEnabled()) log.debug("writeAll expecting callback from writePane "+i);
				return true;
			}
		}
		// nothing to program, end politely
		_programmingPane = null;
		if (log.isDebugEnabled()) log.debug("writeAll found nothing to do");
		return false;	
	}
	
	boolean _read = true;
	PaneProgPane _programmingPane = null;
	
	/** 
	 * get notification of a variable property change in the pane, specifically "busy" going to 
	 * false at the end of a programming operation
	 */
	public void propertyChange(java.beans.PropertyChangeEvent e) {
		// check for the right event
		if (_programmingPane == null) {
			log.warn("unexpected propertChange: "+e);
			return;
		} else if (log.isDebugEnabled()) log.debug("property changed: "+e.getPropertyName()
													+" new value: "+e.getNewValue());
		log.debug("check valid: "+(e.getSource() == _programmingPane)+" "+(!e.getPropertyName().equals("Busy"))+" "+(((Boolean)e.getNewValue()).equals(Boolean.FALSE)));
		if (e.getSource() == _programmingPane &&
				e.getPropertyName().equals("Busy") &&
				((Boolean)e.getNewValue()).equals(Boolean.FALSE) )  { 
			
			if (log.isDebugEnabled()) log.debug("end of a programming pane operation, remove");

			// remove existing listener
			_programmingPane.removePropertyChangeListener(this);
			_programmingPane = null;
			// restart the operation
			if (_read) { 
				if (log.isDebugEnabled()) log.debug("restart readAll");
				readAll();
			}
			else {
				if (log.isDebugEnabled()) log.debug("restart writeAll");
				writeAll();
			}
		}
	}
	
	/**
	 * Write everything to a file.
	 */
	public void storeFile() {
		log.info("storeFile starts");

		// reload the RosterEntry
		updateDccAddress();
		_rPane.update(_rosterEntry);

		// id has to be set!
		if (_rosterEntry.getId().equals("") || _rosterEntry.getId().equals("<new loco>")) {
			log.info("storeFile without a filename; issued dialog");
			JOptionPane.showMessageDialog(this, "Please fill in the ID field first");
			return;
		}
		// if there isn't a filename, store using the id
		if (_rosterEntry.getFileName().equals("")) {
			_rosterEntry.setFileName(_rosterEntry.getId()+".xml");
			log.debug("new filename: "+_rosterEntry.getFileName());
		}
		String filename = _rosterEntry.getFileName();
		
		// create a DecoderFile to represent this
		LocoFile df = new LocoFile();
		
		// do I/O
		try {
			String fullFilename = LocoFile.fileLocation+File.separator+filename;
			File f = new File(fullFilename);
			// make sure file doesn't exist
			if (f.exists()) {
				log.debug("Output locomotive file already exists: "+fullFilename);
				f.renameTo(LocoFile.backupFileName(filename));
				// name a new file object for the new file
				f = new File(fullFilename);
			}

			// and finally write the file
			df.writeFile(f, cvModel, variableModel, _rosterEntry);
			
			//and store an updated roster file
			Roster.writeRosterFile();
			
		} catch (Exception e) {
			log.error("error during locomotive file output: "+e);
		}
		
	}
	
	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PaneProgFrame.class.getName());

}
