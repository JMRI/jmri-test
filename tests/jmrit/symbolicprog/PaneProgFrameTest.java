/** 
 * PaneProgFrameTest.java
 *
 * Description:	
 * @author			Bob Jacobsen
 * @version			
 */

package jmri.jmrit.symbolicprog.tabbedframe;

import java.awt.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jdom.*;
import org.jdom.output.*;
import org.jdom.input.*;

import jmri.*;
import jmri.progdebugger.*;
import jmri.jmrit.symbolicprog.*;
import jmri.jmrit.decoderdefn.*;
import jmri.jmrit.roster.*;

public class PaneProgFrameTest extends TestCase {

	// test creating a pane in config file
	public void testPane() {
		setupDoc();
		
		// create test object with speciai implementation of the newPane(String) operation
		result = null;

		PaneProgFrame p = new PaneProgFrame();
		//PaneProgFrame p = new PaneProgFrame() {
		//		public void newPane(String s, Element e) { result = s; }
		//	};
	
		// invoke
		result = null;
		p.readConfig(root, new RosterEntry());
		Assert.assertEquals("paneList length ", 3, p.paneList.size());
	}


	// show me the specially-created frame
	public void testFrame() {
		setupDoc();
		PaneProgFrame p = new PaneProgFrame();
		
		// ugly, temporary way to load the decoder info
		jmri.jmrit.decoderdefn.DecoderFileTest t = new jmri.jmrit.decoderdefn.DecoderFileTest("");
		t.setupDecoder();
		DecoderFile df = new DecoderFile();  // used as a temporary
		df.loadVariableModel(t.decoder, p.variableModel);
		
		p.readConfig(root, new RosterEntry());
		p.pack();
		p.show();
	}
	
	// static variables for internal classes to report their interpretations
	static String result = null;
	static int colCount = -1;
	static int varCount = -1;
	
	// static variables for the test XML structures
	Element root = null;
	Document doc = null;
	
	// provide a test document in the above static variables
	void setupDoc() {
		// create a JDOM tree with just some elements
		root = new Element("programmer-config");
		doc = new Document(root);
		doc.setDocType(new DocType("programmer-config","programmer-config.dtd"));
		
		// add some elements
		root.addContent(new Element("programmer")
					.addContent(new Element("pane")
									.addAttribute("name","Basic")
									.addContent(new Element("column")
										.addContent(new Element("display")
													.addAttribute("item", "Address")
													)
										.addContent(new Element("display")
													.addAttribute("item", "Start voltage")
													)
										.addContent(new Element("display")
													.addAttribute("item", "Normal direction of motion")
													)
												)
									.addContent(new Element("column")
										.addContent(new Element("display")
													.addAttribute("item", "Address")
													)
										.addContent(new Element("display")
													.addAttribute("item", "Normal direction of motion")
													)
										.addContent(new Element("display")
													.addAttribute("item", "Normal direction of motion")
													.addAttribute("format","checkbox")
													)
										.addContent(new Element("display")
													.addAttribute("item", "Normal direction of motion")
													.addAttribute("format","radiobuttons")
													)
												)
								)
					.addContent(new Element("pane")
									.addAttribute("name", "CV")
									.addContent(new Element("column")
										.addContent(new Element("cvtable"))
												)
								)
					.addContent(new Element("pane")
									.addAttribute("name", "Other")
									.addContent(new Element("column")
										.addContent(new Element("display")
													.addAttribute("item", "Address")
													)
										.addContent(new Element("display")
													.addAttribute("item", "Normal direction of motion")
													)
												)
								)
				)
			; // end of adding contents
		
		return;
	}

	// from here down is testing infrastructure
	
	public PaneProgFrameTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {PaneProgFrameTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}
	
	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(PaneProgFrameTest.class);
		return suite;
	}
	
	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PaneProgFrameTest.class.getName());

}
