/** 
 * SymbolicProgFrame.java
 *
 * Description:		Frame providing a command station programmer from decoder definition files
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			
 */

package jmri.jmrit.symbolicprog.symbolicframe;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

import java.io.File;
import java.io.FileInputStream;
import com.sun.java.util.collections.List;

import jmri.Programmer;
import jmri.ProgListener;
import jmri.ProgModePane;
import jmri.jmrit.symbolicprog.*;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.DocType;
import org.jdom.output.XMLOutputter;
import org.jdom.JDOMException;
import org.jdom.input.DOMBuilder;
import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;

public class SymbolicProgFrame extends javax.swing.JFrame  {

	// GUI member declarations

	JTextField locoRoadName 	= new JTextField();
	JTextField locoRoadNumber 	= new JTextField();
	JTextField locoMfg 			= new JTextField();
	JTextField locoModel 		= new JTextField();
	
	JLabel progStatus       	= new JLabel(" OK ");
	
	JButton selectFileButton 	= new JButton();
	JButton storeFileButton 	= new JButton();

	CvTableModel	cvModel		= new CvTableModel(progStatus);
	JTable			cvTable		= new JTable(cvModel);
	JScrollPane 	cvScroll	= new JScrollPane(cvTable);

	VariableTableModel		variableModel	= new VariableTableModel(progStatus,
					new String[]  {"Name", "Value", "Range", "State", "Read", "Write", "CV", "Mask", "Comment" },
					cvModel);
	JTable		variableTable	= new JTable(variableModel);
	JScrollPane variableScroll	= new JScrollPane(variableTable);
	
	JButton  newCvButton 		= new JButton();
	JLabel   newCvLabel  		= new JLabel();
	JTextField newCvNum  		= new JTextField();

	JButton  	newVarButton 	= new JButton();
	JLabel   	newVarNameLabel = new JLabel();
	JTextField 	newVarName  	= new JTextField();
	JLabel   	newVarCvLabel 	= new JLabel();
	JTextField 	newVarCv  		= new JTextField();
	JLabel   	newVarMaskLabel = new JLabel();
	JTextField 	newVarMask  	= new JTextField();

	ProgModePane   modePane 	= new ProgModePane(BoxLayout.X_AXIS);
			
	JLabel decoderMfg  			= new JLabel("         ");
	JLabel decoderModel   		= new JLabel("         ");
	
	// member to find and remember the configuration file in and out
	final JFileChooser fci 		= new JFileChooser("xml"+File.separator+"decoders"+File.separator);
	final JFileChooser fco 		= new JFileChooser("xml"+File.separator+"decoders"+File.separator);

	// ctor
	public SymbolicProgFrame() {

		// configure GUI elements
		selectFileButton.setText("Read File");
		selectFileButton.setVisible(true);
		selectFileButton.setToolTipText("Press to select & read a configuration file");
		
		storeFileButton.setText("Store File");
		storeFileButton.setVisible(true);
		storeFileButton.setToolTipText("Press to store the configuration file");
		
		variableTable.setDefaultRenderer(JTextField.class, new ValueRenderer());
		variableTable.setDefaultRenderer(JButton.class, new ValueRenderer());
		variableTable.setDefaultEditor(JTextField.class, new ValueEditor());
		variableTable.setDefaultEditor(JButton.class, new ValueEditor());
		variableScroll.setColumnHeaderView(variableTable.getTableHeader());
		// have to shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
		// instead of forcing the columns to fill the frame (and only fill)
		variableTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		cvTable.setDefaultRenderer(JButton.class, new ValueRenderer());
		cvTable.setDefaultEditor(JButton.class, new ValueEditor());
		cvScroll.setColumnHeaderView(cvTable.getTableHeader());
		// have to shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
		// instead of forcing the columns to fill the frame (and only fill)
		cvTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		newCvButton.setText("Create new CV");
		newCvLabel.setText("CV number:");
		newCvNum.setText("");
		
		newVarButton.setText("Create new variable");
		newVarNameLabel.setText("Name:");
		newVarName.setText("");
		newVarCvLabel.setText("Cv number:");
		newVarCv.setText("");
		newVarMaskLabel.setText("Bit mask:");
		newVarMask.setText("VVVVVVVV");
		
		// add actions to buttons
		selectFileButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				selectFileButtonActionPerformed(e);
			}
		});
		storeFileButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				writeFile();
			}
		});
		newCvButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				cvModel.addCV(newCvNum.getText());
			}
		});
		newVarButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				newVarButtonPerformed();
			}
		});
			
			setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
			addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				thisWindowClosing(e);
			}
		});

		// general GUI config
		setTitle("Symbolic Programmer");
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		// install items in GUI
		JPanel tPane1 = new JPanel();
			tPane1.setLayout(new BoxLayout(tPane1, BoxLayout.X_AXIS));
			tPane1.add(new JLabel("Road Name: "));  
			tPane1.add(locoRoadName);
			tPane1.add(new JLabel("Number: "));  
			tPane1.add(locoRoadNumber);
			tPane1.add(new JLabel("Manufacturer: "));  
			tPane1.add(locoMfg);
			tPane1.add(new JLabel("Model: "));  
			tPane1.add(locoModel);
		getContentPane().add(tPane1);
		
		JPanel tPane3 = new JPanel();
			tPane3.setLayout(new BoxLayout(tPane3, BoxLayout.X_AXIS));
			tPane3.add(selectFileButton);  
			tPane3.add(Box.createHorizontalGlue());
			tPane3.add(new JLabel("Decoder Manufacturer: "));
			tPane3.add(decoderMfg);
			tPane3.add(new JLabel(" Model: "));
			tPane3.add(decoderModel);
			tPane3.add(Box.createHorizontalGlue());
			tPane3.add(storeFileButton);  
			tPane3.add(Box.createHorizontalGlue());
		getContentPane().add(tPane3);

		JPanel tPane2 = new JPanel();
			tPane2.setLayout(new BoxLayout(tPane2, BoxLayout.X_AXIS));
			tPane2.add(modePane);
			tPane2.add(Box.createHorizontalGlue());
		getContentPane().add(tPane2);
		
		getContentPane().add(new JSeparator());

		getContentPane().add(progStatus);
		
		getContentPane().add(variableScroll);
		
		getContentPane().add(cvScroll);

		JPanel tPane4 = new JPanel();
			tPane4.setLayout(new BoxLayout(tPane4, BoxLayout.X_AXIS));
			tPane4.add(newCvButton);
			tPane4.add(newCvLabel);
			tPane4.add(newCvNum);
			tPane4.add(Box.createHorizontalGlue());
		getContentPane().add(tPane4);

		tPane4 = new JPanel();
			tPane4.setLayout(new BoxLayout(tPane4, BoxLayout.X_AXIS));
			tPane4.add(newVarButton);
			tPane4.add(newVarNameLabel);
			tPane4.add(newVarName);
			tPane4.add(newVarCvLabel);
			tPane4.add(newVarCv);
			tPane4.add(newVarMaskLabel);
			tPane4.add(newVarMask);
			tPane4.add(Box.createHorizontalGlue());
		getContentPane().add(tPane4);
		
		// for debugging
		
		pack();
	}
  	
  	protected void selectFileButtonActionPerformed(java.awt.event.ActionEvent e) {
		// show dialog
		int retVal = fci.showOpenDialog(this);

		// handle selection or cancel
		if (retVal == JFileChooser.APPROVE_OPTION) {
			File file = fci.getSelectedFile();
			if (log.isInfoEnabled()) log.info("selectFileButtonActionPerformed: located file "+file+" for XML processing");
			progStatus.setText("Reading file...");
			// handle the file (later should be outside this thread?)
			readAndParseConfigFile(file);
			progStatus.setText("OK");
			if (log.isInfoEnabled()) log.info("selectFileButtonActionPerformed: parsing complete");

		}
  	}	

	protected void newVarButtonPerformed()  {
		String name = newVarName.getText();
		int CV = Integer.valueOf(newVarCv.getText()).intValue();
		String mask = newVarMask.getText();
		
		// ask Table model to do the actuall add
		variableModel.newDecVariableValue(name, CV, mask);
		variableModel.configDone();
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
		// check for various types of dirty - first table data not written back
		if (cvModel.decoderDirty() || variableModel.decoderDirty() ) {
			if (JOptionPane.showConfirmDialog(null, 
		   		"Some changes have not been written to the decoder. They will be lost. Close window?", 
		    	"choose one", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) return;
		    }
		if (variableModel.fileDirty() ) {
			if (JOptionPane.showConfirmDialog(null, 
		    	"Some changes have not been written to a configuration file. Close window?", 
		    	"choose one", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) return;
		    }

		//OK, close
		setVisible(false);
		modePane.dispose();
		dispose();	
	}
	
	void readAndParseConfigFile(File file) {
		try {
			// This is taken in large part from "Java and XML" page 354
			
			// Open and parse file
			Namespace ns = Namespace.getNamespace("decoder",
										"http://jmri.sourceforge.net/xml/decoder");
			SAXBuilder builder = new SAXBuilder(true);  // argument controls validation, on for now
			Document doc = builder.build(new FileInputStream(file),"xml"+File.separator);
			// find root
			Element root = doc.getRootElement();
			
			// decode type, invoke proper processing routine if a decoder file
			if (root.getChild("decoder", ns) != null) processDecoderFile(root.getChild("decoder", ns), ns);
			else { // try again as a loco file
				ns = Namespace.getNamespace("locomotive",
										"http://jmri.sourceforge.net/xml/locomotive");
				builder = new SAXBuilder(true);  // argument controls validation, on for now
				doc = builder.build(new FileInputStream(file),"xml"+File.separator);
				root = doc.getRootElement();
				if (root.getChild("locomotive", ns) != null) processLocoFile(root.getChild("locomotive", ns));
				else log.error("Unrecognized config file contents");
			}
		} catch (Exception e) {
			log.warn("readAndParseDecoderConfig: readAndParseDecoderConfig exception: "+e);
		}
	}
		
	void processDecoderFile(Element base, Namespace ns) {
			// find decoder id, assuming first decoder is fine for now (e.g. one per file)
			Element decoderID = base.getChild("id",ns);
			
			// store name, type
			decoderMfg.setText(base.getChild("id",ns).getAttribute("mfg").getValue());
			decoderModel.setText(base.getChild("id",ns).getAttribute("model").getValue());
			
			// start loading variables to table
			List varList = base.getChild("variables",ns).getChildren("variable",ns);
			for (int i=0; i<varList.size(); i++) {
				// load each row
				variableModel.setRow(i, (Element)(varList.get(i)), ns);
				}
			variableModel.configDone();
	}

	void processLocoFile(Element loco) {
			// switch to a correct namespace definition
			Namespace ns = Namespace.getNamespace("locomotive",
										"http://jmri.sourceforge.net/xml/locomotive");
			// load the name et al
			locoRoadName.setText(loco.getAttributeValue("roadName"));
			locoRoadNumber.setText(loco.getAttributeValue("roadNumber"));
			locoMfg.setText(loco.getAttributeValue("mfg"));
			locoModel.setText(loco.getAttributeValue("model"));
			// load the variable definitions for the decoder
			Element decoder = loco.getChild("decoder", ns);
			if (decoder != null) {
				// get the file name
				String mfg = decoder.getAttribute("mfg").getValue();
				String model = decoder.getAttribute("model").getValue();
				String filename = "xml"+File.separator+mfg+"_"+model+".xml";
				if (log.isInfoEnabled()) log.info("will read decoder info from "+filename);
				readAndParseConfigFile(new File(filename));
				if (log.isDebugEnabled()) log.debug("finished processing decoder file for loco file");
			} else log.error("No decoder element found in config file");
			
			// get the CVs and load
			Element values = loco.getChild("values", ns);
			if (values != null) {
			// get the CV values and load
				List varList = values.getChildren("CVvalue",ns);
				if (log.isDebugEnabled()) log.debug("Found "+varList.size()+" CVvalues");
				
				for (int i=0; i<varList.size(); i++) {
					// locate the row 
					if ( ((Element)(varList.get(i))).getAttribute("name") == null) {
						  if (log.isDebugEnabled()) log.debug("unexpected null in name "+((Element)(varList.get(i)))+" "+((Element)(varList.get(i))).getAttributes());
						  break;
					}
					if ( ((Element)(varList.get(i))).getAttribute("value") == null) {
						  if (log.isDebugEnabled()) log.debug("unexpected null in value "+((Element)(varList.get(i)))+" "+((Element)(varList.get(i))).getAttributes());
						  break;
					}

					String name = ((Element)(varList.get(i))).getAttribute("name").getValue();
					String value = ((Element)(varList.get(i))).getAttribute("value").getValue();
					if (log.isDebugEnabled()) log.debug("CV: "+i+"th entry, CV number "+name+" has value: "+value);

					int cv = Integer.valueOf(name).intValue();
					CvValue cvObject = (CvValue)(cvModel.allCvVector().elementAt(cv));
					cvObject.setValue(Integer.valueOf(value).intValue());
					cvObject.setState(CvValue.FROMFILE);
				}
				variableModel.configDone();
			} else log.error("no values element found in config file; CVs not configured");
			
			// get the variable values and load
			Element decoderDef = values.getChild("decoderDef", ns);
			if (decoderDef != null) {
				List varList = decoderDef.getChildren("varValue",ns);
				if (log.isDebugEnabled()) log.debug("Found "+varList.size()+" varValues");
				
				for (int i=0; i<varList.size(); i++) {
					// locate the row 
					if ( ((Element)(varList.get(i))).getAttribute("name") == null) {
						  if (log.isDebugEnabled()) log.debug("unexpected null in name "+((Element)(varList.get(i))));
						  break;
					}
					if ( ((Element)(varList.get(i))).getAttribute("value") == null) {
						  if (log.isDebugEnabled()) log.debug("unexpected null in value "+((Element)(varList.get(i))));
						  break;
					}

					String name = ((Element)(varList.get(i))).getAttribute("name").getValue();
					String value = ((Element)(varList.get(i))).getAttribute("value").getValue();
					if (log.isDebugEnabled()) log.debug("Variable "+i+" is "+name+" value: "+value);

					int row;
					for (row=0; row<variableModel.getRowCount(); row++) {
						if (variableModel.getName(row).equals(name)) break;
					}
					if (log.isDebugEnabled()) log.debug("Variable "+name+" is row "+row);
					if ( ! value.equals("")) { // don't set if no value was stored
						variableModel.setIntValue(row, Integer.valueOf(value).intValue());
					}
					variableModel.setState(row, VariableValue.FROMFILE);
				}
				variableModel.configDone();
			} else log.error("no decoderDef element found in config file");
		
			// the act of loading values marks as dirty, but we're actually in synch
			variableModel.setFileDirty(false);
	}

	void writeFile() {
		try {
			// get the file
			int retVal = fco.showSaveDialog(this);
			// handle selection or cancel
			if (retVal != JFileChooser.APPROVE_OPTION) return; // leave early
				
			File file = fco.getSelectedFile();

			// This is taken in large part from "Java and XML" page 368 
			Namespace ns = Namespace.getNamespace("locomotive",
										"http://jmri.sourceforge.net/xml/locomotive");

			// create root element
			Element root = new Element("locomotive-config", ns);
			Document doc = new Document(root);
			doc.setDocType(new DocType("locomotive:locomotive-config","DTD/locomotive-config.dtd"));
		
			// add top-level elements
			Element values;
			root.addContent(new Element("locomotive",ns)		// locomotive values are first item
					.addAttribute("roadNumber",locoRoadNumber.getText())
					.addAttribute("roadName",locoRoadName.getText())
					.addAttribute("mfg",locoMfg.getText())
					.addAttribute("model",locoModel.getText())
					.addContent(new Element("decoder", ns)
									.addAttribute("model",decoderModel.getText())
									.addAttribute("mfg",decoderMfg.getText())
									.addAttribute("versionID","")
									.addAttribute("mfgID","")
								)
						  .addContent(values = new Element("values", ns))
					)
				;
					
			// Append a decoderDef element to values
			Element decoderDef;
			values.addContent(decoderDef = new Element("decoderDef", ns));
			// add the variable values to the decoderDef Element
			for (int i = 0; i < variableModel.getRowCount(); i++) {
				decoderDef.addContent(new Element("varValue", ns)
									.addAttribute("name", variableModel.getName(i))
									.addAttribute("value", variableModel.getValString(i))
						);
			}
			// add the CV values to the values Element
			for (int i = 0; i < cvModel.getRowCount(); i++) {
				values.addContent(new Element("CVvalue", ns)
									.addAttribute("name", cvModel.getName(i))
									.addAttribute("value", cvModel.getValString(i))
						);
			}
			
			// write the result to selected file
			java.io.FileOutputStream o = new java.io.FileOutputStream(file);
			XMLOutputter fmt = new XMLOutputter();
			fmt.setNewlines(true);   // pretty printing
			fmt.setIndent(true);
			fmt.output(doc, o);
			
			// make file as OK
			variableModel.setFileDirty(false);
			}
		catch (Exception e) {
			log.error(e);
		}
	}
	
	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SymbolicProgFrame.class.getName());

}
