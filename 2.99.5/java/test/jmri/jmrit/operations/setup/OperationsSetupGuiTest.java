//OperationsSetupGuiTest.java

package jmri.jmrit.operations.setup;

import jmri.jmrit.XmlFile;
import jmri.jmrit.operations.locations.LocationManagerXml;
import jmri.jmrit.operations.rollingstock.engines.EngineManagerXml;
import jmri.jmrit.operations.rollingstock.cars.CarManagerXml;
import jmri.jmrit.operations.routes.RouteManagerXml;
import jmri.jmrit.operations.setup.OperationsSetupXml;
import jmri.jmrit.operations.trains.TrainManagerXml;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.extensions.jfcunit.eventdata.*;
import jmri.jmrit.display.LocoIcon;

import java.io.File;
import java.util.Locale;

/**
 * Tests for the Operations Setup GUI class
 *  
 * @author	Dan Boudreau Copyright (C) 2009
 * @version $Revision$
 */
public class OperationsSetupGuiTest extends jmri.util.SwingTestCase {
	
	public void testDirectionCheckBoxes(){
		OperationsSetupFrame f = new OperationsSetupFrame();
		f.setLocation(0, 0);	// entire panel must be visible for tests to work properly
		f.initComponents();
				
		//both east/west and north/south checkboxes should be set	
		Assert.assertTrue("North selected", f.northCheckBox.isSelected());
		Assert.assertTrue("East selected", f.eastCheckBox.isSelected());
		
		getHelper().enterClickAndLeave( new MouseEventData( this, f.northCheckBox ) );	
		Assert.assertFalse("North deselected", f.northCheckBox.isSelected());
		Assert.assertTrue("East selected", f.eastCheckBox.isSelected());
		
		getHelper().enterClickAndLeave( new MouseEventData( this, f.eastCheckBox ) );		
		Assert.assertTrue("North selected", f.northCheckBox.isSelected());
		Assert.assertFalse("East deselected", f.eastCheckBox.isSelected());
		
		getHelper().enterClickAndLeave( new MouseEventData( this, f.eastCheckBox ) );
		Assert.assertTrue("North selected", f.northCheckBox.isSelected());
		Assert.assertTrue("East selected", f.eastCheckBox.isSelected());
		
		//done
		f.dispose();
	}
	
	public void testSetupFrameWrite(){
		// force creation of backup
		Setup.setCarTypes(Setup.AAR);
		
		OperationsSetupFrame f = new OperationsSetupFrame();
		f.setLocation(0, 0);	// entire panel must be visible for tests to work properly
		f.initComponents();
		
		f.railroadNameTextField.setText("Test Railroad Name");
		f.maxLengthTextField.setText("1234");
		f.maxEngineSizeTextField.setText("6");
		f.switchTimeTextField.setText("3");
		f.travelTimeTextField.setText("4");
		//f.ownerTextField.setText("Bob J");
		
		getHelper().enterClickAndLeave( new MouseEventData( this, f.scaleHO ) );
		getHelper().enterClickAndLeave( new MouseEventData( this, f.typeDesc ) );
		
		f.panelTextField.setText("Test Panel Name");
		
		f.eastComboBox.setSelectedItem(LocoIcon.RED);
		f.westComboBox.setSelectedItem(LocoIcon.BLUE);
		f.northComboBox.setSelectedItem(LocoIcon.WHITE);
		f.southComboBox.setSelectedItem(LocoIcon.GREEN);
		f.terminateComboBox.setSelectedItem(LocoIcon.GRAY);
		f.localComboBox.setSelectedItem(LocoIcon.YELLOW);

		getHelper().enterClickAndLeave( new MouseEventData( this, f.saveButton ) );
		//done
		f.dispose();
	}
	
	public void testSetupFrameRead(){
		OperationsSetupFrame f = new OperationsSetupFrame();
		f.setLocation(0, 0);	// entire panel must be visible for tests to work properly
		f.initComponents();
		
		Assert.assertEquals("railroad name", "Test Railroad Name", f.railroadNameTextField.getText());
		Assert.assertEquals("max length", "1234", f.maxLengthTextField.getText());
		Assert.assertEquals("max engines", "6", f.maxEngineSizeTextField.getText());
		Assert.assertEquals("switch time", "3", f.switchTimeTextField.getText());
		Assert.assertEquals("travel time", "4", f.travelTimeTextField.getText());
		//Assert.assertEquals("owner", "Bob J", f.ownerTextField.getText());
				
		Assert.assertTrue("HO scale", f.scaleHO.isSelected());
		Assert.assertFalse("N scale", f.scaleN.isSelected());
		Assert.assertFalse("Z scale", f.scaleZ.isSelected());
		Assert.assertFalse("TT scale", f.scaleTT.isSelected());
		Assert.assertFalse("HOn3 scale", f.scaleHOn3.isSelected());
		Assert.assertFalse("OO scale", f.scaleOO.isSelected());
		Assert.assertFalse("Sn3 scale", f.scaleSn3.isSelected());
		Assert.assertFalse("S scale", f.scaleS.isSelected());
		Assert.assertFalse("On3 scale", f.scaleOn3.isSelected());
		Assert.assertFalse("O scale", f.scaleO.isSelected());
		Assert.assertFalse("G scale", f.scaleG.isSelected());
		
		Assert.assertTrue("descriptive", f.typeDesc.isSelected());
		Assert.assertFalse("AAR", f.typeAAR.isSelected());
		
		Assert.assertEquals("panel name", "Test Panel Name", f.panelTextField.getText());
		
		Assert.assertEquals("east color", LocoIcon.RED, f.eastComboBox.getSelectedItem());
		Assert.assertEquals("west color", LocoIcon.BLUE, f.westComboBox.getSelectedItem());
		Assert.assertEquals("north color", LocoIcon.WHITE, f.northComboBox.getSelectedItem());
		Assert.assertEquals("south color", LocoIcon.GREEN, f.southComboBox.getSelectedItem());
		Assert.assertEquals("terminate color", LocoIcon.GRAY, f.terminateComboBox.getSelectedItem());
		Assert.assertEquals("local color", LocoIcon.YELLOW, f.localComboBox.getSelectedItem());
		//done
		f.dispose();
	}

	public void testOptionFrameWrite(){
		OptionFrame f = new OptionFrame();
		f.setLocation(0, 0);	// entire panel must be visible for tests to work properly
		f.initComponents();		
		
		// confirm defaults
		Assert.assertTrue("build normal", f.buildNormal.isSelected());
		Assert.assertFalse("build aggressive", f.buildAggressive.isSelected());
		Assert.assertFalse("local", f.localSidingCheckBox.isSelected());
		Assert.assertFalse("interchange", f.localInterchangeCheckBox.isSelected());
		Assert.assertFalse("yard", f.localYardCheckBox.isSelected());
		Assert.assertFalse("rfid", f.rfidCheckBox.isSelected());
		Assert.assertFalse("car logger", f.carLoggerCheckBox.isSelected());
		Assert.assertFalse("engine logger", f.engineLoggerCheckBox.isSelected());
		Assert.assertTrue("router", f.routerCheckBox.isSelected());
		
		getHelper().enterClickAndLeave( new MouseEventData( this, f.buildAggressive ) );
		Assert.assertFalse("build normal", f.buildNormal.isSelected());
		Assert.assertTrue("build aggressive", f.buildAggressive.isSelected());
		
		getHelper().enterClickAndLeave( new MouseEventData( this, f.localSidingCheckBox ) );
		Assert.assertTrue("local", f.localSidingCheckBox.isSelected());
		
		getHelper().enterClickAndLeave( new MouseEventData( this, f.localInterchangeCheckBox ) );
		Assert.assertTrue("interchange", f.localInterchangeCheckBox.isSelected());
		
		getHelper().enterClickAndLeave( new MouseEventData( this, f.localYardCheckBox ) );
		Assert.assertTrue("yard", f.localYardCheckBox.isSelected());
		
		getHelper().enterClickAndLeave( new MouseEventData( this, f.rfidCheckBox ) );
		Assert.assertTrue("rfid", f.rfidCheckBox.isSelected());
		
		getHelper().enterClickAndLeave( new MouseEventData( this, f.carLoggerCheckBox ) );
		Assert.assertTrue("car logger", f.carLoggerCheckBox.isSelected());
		
		getHelper().enterClickAndLeave( new MouseEventData( this, f.engineLoggerCheckBox ) );
		Assert.assertTrue("engine logger", f.engineLoggerCheckBox.isSelected());
		
		getHelper().enterClickAndLeave( new MouseEventData( this, f.routerCheckBox ) );
		Assert.assertFalse("router", f.routerCheckBox.isSelected());
		
		getHelper().enterClickAndLeave( new MouseEventData( this, f.saveButton ) );
		//done
		f.dispose();
	}
	
	public void testOptionFrameRead(){
		OptionFrame f = new OptionFrame();
		f.setLocation(0, 0);	// entire panel must be visible for tests to work properly
		f.initComponents();		
		
		Assert.assertFalse("build normal",f.buildNormal.isSelected());
		Assert.assertTrue("build aggressive",f.buildAggressive.isSelected());
		Assert.assertTrue("local", f.localSidingCheckBox.isSelected());
		Assert.assertTrue("interchange", f.localInterchangeCheckBox.isSelected());
		Assert.assertTrue("yard", f.localYardCheckBox.isSelected());
		Assert.assertTrue("rfid", f.rfidCheckBox.isSelected());
		Assert.assertTrue("car logger", f.carLoggerCheckBox.isSelected());
		Assert.assertTrue("engine logger", f.engineLoggerCheckBox.isSelected());
		Assert.assertFalse("router", f.routerCheckBox.isSelected());
		
		//done
		f.dispose();
	}
	
	public void testBackupFileCreation(){
		String dirName = XmlFile.prefsDir()+OperationsSetupXml.getOperationsDirectoryName()+ File.separator + "backups";
		
		File dir = new File(dirName);		
		Assert.assertNotNull("backup directory exists", dir);
		
		String[] backupDirectoryNames = dir.list();
		Assert.assertTrue("There should be at least one directory", backupDirectoryNames.length>0);
		
		for (int i = 0; i < backupDirectoryNames.length; i++) {
			File backDir = new File(dirName + File.separator + backupDirectoryNames[i]);
			Assert.assertNotNull("backup directory", backDir);
			// delete file names
			String[] backupFileNames = backDir.list();
			Assert.assertEquals("There should be 6 files", 6, backupFileNames.length);
			for (int j = 0; j < backupFileNames.length; j++) {
				File file = new File(dirName + File.separator + backupDirectoryNames[i] + File.separator +  backupFileNames[j]);
				Assert.assertNotNull("operations backup file", file);
				Assert.assertTrue("delete file", file.delete());
			}
			// now delete the directory
			Assert.assertTrue("delete backup directory", backDir.delete());
		}
		Assert.assertTrue("delete directory", dir.delete());
	}

	
	// Ensure minimal setup for log4J
	@Override
    protected void setUp() throws Exception { 
        super.setUp();
		apps.tests.Log4JFixture.setUp();
		
		// set the locale to US English
		Locale.setDefault(Locale.ENGLISH);
		
		// Repoint OperationsSetupXml to JUnitTest subdirectory
		OperationsSetupXml.setOperationsDirectoryName("operations"+File.separator+"JUnitTest");
		// Change file names to ...Test.xml
		OperationsSetupXml.instance().setOperationsFileName("OperationsJUnitTest.xml"); 
		RouteManagerXml.instance().setOperationsFileName("OperationsJUnitTestRouteRoster.xml");
		EngineManagerXml.instance().setOperationsFileName("OperationsJUnitTestEngineRoster.xml");
		CarManagerXml.instance().setOperationsFileName("OperationsJUnitTestCarRoster.xml");
		LocationManagerXml.instance().setOperationsFileName("OperationsJUnitTestLocationRoster.xml");
		TrainManagerXml.instance().setOperationsFileName("OperationsJUnitTestTrainRoster.xml");

	}

	public OperationsSetupGuiTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", OperationsSetupGuiTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(OperationsSetupGuiTest.class);
		return suite;
	}

	// The minimal setup for log4J
	@Override
    protected void tearDown() throws Exception { 
        apps.tests.Log4JFixture.tearDown();
        super.tearDown();
    }
}
