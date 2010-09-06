//OperationsTrainsGuiTest.java

package jmri.jmrit.operations.trains;

import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.setup.Control;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.extensions.jfcunit.finder.*;
import junit.extensions.jfcunit.eventdata.*;
import jmri.util.JmriJFrame;

import java.awt.Dimension;
import java.awt.Point;
import java.util.List;

/**
 * Tests for the Operations Trains GUI class
 *  
 * @author	Dan Boudreau Copyright (C) 2009
 * @version $Revision: 1.19 $
 */
public class OperationsTrainsGuiTest extends jmri.util.SwingTestCase {

	/**
	 * Adds some cars for the various tests in this suite
	 */
	public void testTrainsAddCars(){
		CarManager cm = CarManager.instance();
		// add caboose to the roster
		Car c = cm.newCar("NH", "687");
		c.setCaboose(true);
		c = cm.newCar("CP", "435");
		c.setCaboose(true);
		
	}
	
	public void testTrainsTableFrame(){
		TrainManager tmanager = TrainManager.instance();
		// turn off build fail messages
		tmanager.setBuildMessages(true);
		// turn off print preview
		tmanager.setPrintPreview(false);

		TrainsTableFrame f = new TrainsTableFrame();
		f.setVisible(true);
		f.setLocation(10,20);
		getHelper().enterClickAndLeave( new MouseEventData( this, f.sortByName ) );
		getHelper().enterClickAndLeave( new MouseEventData( this, f.saveButton ) );
		
		// frame location can move just a bit on MacOS
		Point p = f.getLocation();
				
		Assert.assertEquals("sort by name", TrainsTableFrame.NAME, tmanager.getTrainsFrameSortBy());
		Assert.assertEquals("location 1", p, tmanager.getTrainsFramePosition());
		Assert.assertEquals("default size", new Dimension(Control.panelWidth,Control.panelHeight), tmanager.getTrainsFrameSize());
		Assert.assertTrue("Build Messages", tmanager.getBuildMessages());
		Assert.assertFalse("Build Report", tmanager.getBuildReport());
		Assert.assertFalse("Print Review", tmanager.getPrintPreview());
		
		getHelper().enterClickAndLeave( new MouseEventData( this, f.sortByTime ) );
		getHelper().enterClickAndLeave( new MouseEventData( this, f.buildMsgBox ) );
		getHelper().enterClickAndLeave( new MouseEventData( this, f.buildReportBox ) );
		getHelper().enterClickAndLeave( new MouseEventData( this, f.saveButton ) );
		
		// frame location can move just a bit on MacOS
		p = f.getLocation();		
		
		f.setSize(610,250);
		f.setLocation(20,10);
		f.validate();
		
		Assert.assertEquals("sort by time", TrainsTableFrame.TIME, tmanager.getTrainsFrameSortBy());
		Assert.assertFalse("Build Messages 2", tmanager.getBuildMessages());
		Assert.assertTrue("Build Report 2", tmanager.getBuildReport());
		Assert.assertFalse("Print Review 2", tmanager.getPrintPreview());

		// frame location shouldn't have moved yet
		Assert.assertEquals("location check", p, tmanager.getTrainsFramePosition());
		Assert.assertEquals("size check", new Dimension(Control.panelWidth,Control.panelHeight), tmanager.getTrainsFrameSize());
		
		getHelper().enterClickAndLeave( new MouseEventData( this, f.sortById ) );
		getHelper().enterClickAndLeave( new MouseEventData( this, f.buildMsgBox ) );
		getHelper().enterClickAndLeave( new MouseEventData( this, f.printPreviewBox ) );
		getHelper().enterClickAndLeave( new MouseEventData( this, f.saveButton ) );
		
		// frame location can move just a bit on MacOS
		p = f.getLocation();
		
		Assert.assertEquals("sort by id", TrainsTableFrame.ID, tmanager.getTrainsFrameSortBy());
		Assert.assertEquals("location 3", p, tmanager.getTrainsFramePosition());
		Assert.assertEquals("size 3", new Dimension(610,250), tmanager.getTrainsFrameSize());
		Assert.assertTrue("Build Messages 3", tmanager.getBuildMessages());
		Assert.assertTrue("Build Report 3", tmanager.getBuildReport());
		Assert.assertTrue("Print Review 3", tmanager.getPrintPreview());

		// create the TrainEditFrame
		getHelper().enterClickAndLeave( new MouseEventData( this, f.addButton ) );		
	    // confirm panel creation
		JmriJFrame tef = JmriJFrame.getFrame("Add Train");
        Assert.assertNotNull("train edit frame", tef);
		
		// create the TrainSwichListEditFrame
		getHelper().enterClickAndLeave( new MouseEventData( this, f.printSwitchButton ) );
	    // confirm panel creation
		JmriJFrame tsle = JmriJFrame.getFrame("Switch Lists by Location");
        Assert.assertNotNull("train switchlist edit frame", tsle);
		
        // kill panels
        tef.dispose();
        tsle.dispose();
		f.dispose();
	}
	
	/**
	 * This test relies on OperationsTrainsTest having been run to initialize
	 * the train fields.
	 */
	public void testTrainEditFrame(){
		
		TrainEditFrame trainEditFrame = new TrainEditFrame();
		trainEditFrame.setTitle("Test Add Train Frame");
		trainEditFrame.initComponents(null);
		// fill in name and description fields
		trainEditFrame.trainNameTextField.setText("Test Train Name");
		trainEditFrame.trainDescriptionTextField.setText("Test Train Description");
		trainEditFrame.commentTextArea.setText("Test Train Comment");
		getHelper().enterClickAndLeave( new MouseEventData( this, trainEditFrame.addTrainButton ) );
		
		TrainManager tmanager = TrainManager.instance();
		Train t = tmanager.getTrainByName("Test Train Name");
		
		// test defaults
		Assert.assertEquals("train name", "Test Train Name", t.getName());
		Assert.assertEquals("train description", "Test Train Description", t.getDescription());
		Assert.assertEquals("train comment", "Test Train Comment", t.getComment());
		Assert.assertEquals("train depart time", "00:00", t.getDepartureTime());
		Assert.assertEquals("train route", null, t.getRoute());
		Assert.assertTrue("train accepts car type Flat", t.acceptsTypeName("Flat"));
		Assert.assertEquals("train roads", Train.ALLROADS, t.getRoadOption());
		Assert.assertEquals("train requirements", Train.NONE, t.getRequirements());
		
		// test departure time fields
		trainEditFrame.hourBox.setSelectedItem("15");
		trainEditFrame.minuteBox.setSelectedItem("45");
		// shouldn't change until Save
		Assert.assertEquals("train comment", "00:00", t.getDepartureTime());
		getHelper().enterClickAndLeave( new MouseEventData( this, trainEditFrame.saveTrainButton ) );
		Assert.assertEquals("train comment", "15:45", t.getDepartureTime());
		
		// test route field
		trainEditFrame.routeBox.setSelectedIndex(3);	// the 3rd item should be "Test Route C"
		Assert.assertEquals("train route 2", "Test Route C", t.getRoute().getName());
		// test route edit button
		getHelper().enterClickAndLeave( new MouseEventData( this, trainEditFrame.editButton ) );
	    // confirm panel creation
		JmriJFrame ref = JmriJFrame.getFrame("Edit Route");
        Assert.assertNotNull("route add frame", ref);
		
		// test car types using the clear and set buttons
		getHelper().enterClickAndLeave( new MouseEventData( this, trainEditFrame.clearButton ) );
		Assert.assertFalse("train accepts car type Flat 1", t.acceptsTypeName("Flat"));
		getHelper().enterClickAndLeave( new MouseEventData( this, trainEditFrame.setButton ) );
		Assert.assertTrue("train accepts car type Flat 2", t.acceptsTypeName("Flat"));
			
		// test engine fields
		Assert.assertEquals("number of engines", "0", t.getNumberEngines());
		Assert.assertEquals("engine model", "", t.getEngineModel());
		Assert.assertEquals("engine road", "", t.getEngineRoad());
		// now change them
		trainEditFrame.numEnginesBox.setSelectedItem("3");
		trainEditFrame.modelEngineBox.setSelectedItem("FT");
		trainEditFrame.roadEngineBox.setSelectedItem("UP");
		// shouldn't change until Save
		Assert.assertEquals("number of engines 1", "0", t.getNumberEngines());
		Assert.assertEquals("engine model 1", "", t.getEngineModel());
		Assert.assertEquals("engine road 1", "", t.getEngineRoad());
		getHelper().enterClickAndLeave( new MouseEventData( this, trainEditFrame.saveTrainButton ) );
		Assert.assertEquals("number of engines 2", "3", t.getNumberEngines());
		Assert.assertEquals("engine model 2", "FT", t.getEngineModel());
		Assert.assertEquals("engine road 2", "UP", t.getEngineRoad());
		
		// test caboose and FRED buttons and fields
		// require a car with FRED
		getHelper().enterClickAndLeave( new MouseEventData( this, trainEditFrame.fredRadioButton ) );
		// shouldn't change until Save
		Assert.assertEquals("train requirements 1", Train.NONE, t.getRequirements());
		getHelper().enterClickAndLeave( new MouseEventData( this, trainEditFrame.saveTrainButton ) );
		Assert.assertEquals("train requirements 2", Train.FRED, t.getRequirements());
		getHelper().enterClickAndLeave( new MouseEventData( this, trainEditFrame.cabooseRadioButton ) );
		getHelper().enterClickAndLeave( new MouseEventData( this, trainEditFrame.saveTrainButton ) );
		Assert.assertEquals("train requirements 3", Train.CABOOSE, t.getRequirements());
		Assert.assertEquals("caboose road 1", "", t.getCabooseRoad());
		// shouldn't change until Save
		trainEditFrame.roadCabooseBox.setSelectedItem("NH");
		Assert.assertEquals("caboose road 2", "", t.getCabooseRoad());
		getHelper().enterClickAndLeave( new MouseEventData( this, trainEditFrame.saveTrainButton ) );
		Assert.assertEquals("caboose road 3", "NH", t.getCabooseRoad());
		getHelper().enterClickAndLeave( new MouseEventData( this, trainEditFrame.noneRadioButton ) );
		getHelper().enterClickAndLeave( new MouseEventData( this, trainEditFrame.saveTrainButton ) );
		Assert.assertEquals("train requirements 4", Train.NONE, t.getRequirements());

		// test frame size and location
		trainEditFrame.setSize(650,600);
		trainEditFrame.setLocation(25,30);
		getHelper().enterClickAndLeave( new MouseEventData( this, trainEditFrame.saveTrainButton ) );

		// frame location can move just a bit on MacOS
		Point p = trainEditFrame.getLocation();
		Assert.assertEquals("location 1", p, tmanager.getTrainEditFramePosition());
		Assert.assertEquals("size 1", new Dimension(650,600), tmanager.getTrainEditFrameSize());
		
		// test delete button
		// the delete opens a dialog window to confirm the delete
		getHelper().enterClickAndLeave( new MouseEventData( this, trainEditFrame.deleteTrainButton ) );
		// don't delete, we need this train for the next two tests 
		// testTrainBuildOptionFrame() and testTrainEditFrameRead()
		pressDialogButton(trainEditFrame, "No");
		
		ref.dispose();
		trainEditFrame.dispose();
	}
	
	public void testTrainEditFrameBuildOptionFrame(){
		// test build options
		TrainManager tmanager = TrainManager.instance();
		Train t = tmanager.getTrainByName("Test Train Name");
		
		TrainEditFrame trainEditFrame = new TrainEditFrame();
		trainEditFrame.setTitle("Test Build Options Train Frame");
		trainEditFrame.initComponents(t);
		
		TrainEditBuildOptionsFrame f = new TrainEditBuildOptionsFrame();
		f.initComponents(trainEditFrame);
		f.setTitle("Test Train Build Options");
		
		// test car road options
		getHelper().enterClickAndLeave( new MouseEventData( this, f.roadNameExclude ) );
		Assert.assertEquals("train car road exclude", Train.EXCLUDEROADS, t.getRoadOption());
		getHelper().enterClickAndLeave( new MouseEventData( this, f.roadNameInclude ) );
		Assert.assertEquals("train car road include", Train.INCLUDEROADS, t.getRoadOption());		
		getHelper().enterClickAndLeave( new MouseEventData( this, f.roadNameAll ) );
		Assert.assertEquals("train car road all", Train.ALLROADS, t.getRoadOption());
		
		// test car owner options
		getHelper().enterClickAndLeave( new MouseEventData( this, f.ownerNameExclude ) );
		Assert.assertEquals("train car owner exclude", Train.EXCLUDEOWNERS, t.getOwnerOption());
		getHelper().enterClickAndLeave( new MouseEventData( this, f.ownerNameInclude ) );
		Assert.assertEquals("train car owner include", Train.INCLUDEOWNERS, t.getOwnerOption());		
		getHelper().enterClickAndLeave( new MouseEventData( this, f.ownerNameAll ) );
		Assert.assertEquals("train car owner all", Train.ALLOWNERS, t.getOwnerOption());

		// test car date options
		getHelper().enterClickAndLeave( new MouseEventData( this, f.builtDateAfter ) );
		f.builtAfterTextField.setText("1956");
		getHelper().enterClickAndLeave( new MouseEventData( this, f.saveTrainButton ) );
		Assert.assertEquals("train car built after", "1956", t.getBuiltStartYear());
		
		getHelper().enterClickAndLeave( new MouseEventData( this, f.builtDateBefore ) );
		f.builtBeforeTextField.setText("2010");
		getHelper().enterClickAndLeave( new MouseEventData( this, f.saveTrainButton ) );
		Assert.assertEquals("train car built before", "2010", t.getBuiltEndYear());		
		
		getHelper().enterClickAndLeave( new MouseEventData( this, f.builtDateRange ) );
		f.builtAfterTextField.setText("1888");
		f.builtBeforeTextField.setText("2000");
		getHelper().enterClickAndLeave( new MouseEventData( this, f.saveTrainButton ) );
		Assert.assertEquals("train car built after range", "1888", t.getBuiltStartYear());
		Assert.assertEquals("train car built before range", "2000", t.getBuiltEndYear());	
		
		getHelper().enterClickAndLeave( new MouseEventData( this, f.builtDateAll ) );
		getHelper().enterClickAndLeave( new MouseEventData( this, f.saveTrainButton ) );
		Assert.assertEquals("train car built after all", "", t.getBuiltStartYear());
		Assert.assertEquals("train car built before all", "", t.getBuiltEndYear());		

		trainEditFrame.dispose();
		f.dispose();
	}
	
	public void testTrainEditFrameRead(){
		TrainManager tmanager = TrainManager.instance();
		Train t = tmanager.getTrainByName("Test Train Name");
		
		// change the train so it doesn't match the add test
		t.setRequirements(Train.CABOOSE);
		t.setCabooseRoad("CP");
		
		TrainEditFrame f = new TrainEditFrame();
		f.setTitle("Test Edit Train Frame");
		f.initComponents(t);
		
		Assert.assertEquals("train name", "Test Train Name", f.trainNameTextField.getText());
		Assert.assertEquals("train description", "Test Train Description", f.trainDescriptionTextField.getText());
		Assert.assertEquals("train comment", "Test Train Comment", f.commentTextArea.getText());
		Assert.assertEquals("train depart hour", "15", f.hourBox.getSelectedItem());
		Assert.assertEquals("train depart minute", "45", f.minuteBox.getSelectedItem());
		Assert.assertEquals("train route", t.getRoute(), f.routeBox.getSelectedItem());
		Assert.assertEquals("number of engines", "3", f.numEnginesBox.getSelectedItem());
		Assert.assertEquals("engine model", "FT", f.modelEngineBox.getSelectedItem());
		Assert.assertEquals("engine road", "UP", f.roadEngineBox.getSelectedItem());
		Assert.assertEquals("caboose road", "CP", f.roadCabooseBox.getSelectedItem());
		// check radio buttons	
		Assert.assertTrue("caboose selected", f.cabooseRadioButton.isSelected());
		Assert.assertFalse("none selected", f.noneRadioButton.isSelected());
		Assert.assertFalse("FRED selected", f.fredRadioButton.isSelected());
		
		// test frame size and location
		//Assert.assertEquals("location 1", new Point(25,30), tmanager.getTrainEditFramePosition());
		Assert.assertEquals("size 1", new Dimension(650,600), tmanager.getTrainEditFrameSize());
		
		f.dispose();
	}
	
	public void testTrainModifyFrame(){
		// confirm that train default accepts Boxcars
		TrainManager tmanager = TrainManager.instance();
		Train t = tmanager.getTrainByName("Test Train Name");
		Assert.assertTrue("accepts Boxcar 1", t.acceptsTypeName("Boxcar"));
				
		TrainsByCarTypeFrame f = new TrainsByCarTypeFrame();
		f.initComponents("Boxcar");
		
		// remove Boxcar from trains
		getHelper().enterClickAndLeave( new MouseEventData( this, f.clearButton ) );
		getHelper().enterClickAndLeave( new MouseEventData( this, f.saveButton ) );
		Assert.assertFalse("accepts Boxcar 2", t.acceptsTypeName("Boxcar"));

		// now add Boxcar to trains
		getHelper().enterClickAndLeave( new MouseEventData( this, f.setButton ) );
		getHelper().enterClickAndLeave( new MouseEventData( this, f.saveButton ) );
		Assert.assertTrue("accepts Boxcar 3", t.acceptsTypeName("Boxcar"));
		
		f.dispose();
	}
	
	public void testTrainSwitchListEditFrame(){
		TrainSwitchListEditFrame f = new TrainSwitchListEditFrame();
		f.initComponents();
		
		LocationManager lmanager = LocationManager.instance();
		List<String> locations = lmanager.getLocationsByNameList();
		
		// default switch list will print all locations
		for (int i=0; i<locations.size(); i++){
			Location l = lmanager.getLocationById(locations.get(i));
			Assert.assertTrue("print switchlist 1", l.getSwitchList());
		}
		// now clear all locations
		getHelper().enterClickAndLeave( new MouseEventData( this, f.clearButton ) );
		getHelper().enterClickAndLeave( new MouseEventData( this, f.saveButton ) );
		for (int i=0; i<locations.size(); i++){
			Location l = lmanager.getLocationById(locations.get(i));
			Assert.assertFalse("print switchlist 2", l.getSwitchList());
		}
		// now set all locations
		getHelper().enterClickAndLeave( new MouseEventData( this, f.setButton ) );
		getHelper().enterClickAndLeave( new MouseEventData( this, f.saveButton ) );
		for (int i=0; i<locations.size(); i++){
			Location l = lmanager.getLocationById(locations.get(i));
			Assert.assertTrue("print switchlist 3", l.getSwitchList());
		}
		
		f.dispose();
	}
	
	/**
	 * Test that delete train works
	 */
	public void testTrainEditFrameDelete(){
		TrainManager tmanager = TrainManager.instance();
		Train t = tmanager.getTrainByName("Test Train Name");
		
		TrainEditFrame trainEditFrame = new TrainEditFrame();
		trainEditFrame.setTitle("Test Delete Train Frame");
		trainEditFrame.initComponents(t);
		
		getHelper().enterClickAndLeave( new MouseEventData( this, trainEditFrame.deleteTrainButton ) );
		// And now press the confirmation button
		pressDialogButton(trainEditFrame, "Yes");
		
		t = tmanager.getTrainByName("Test Train Name");
		Assert.assertNull("train deleted", t);
		
		// Now add it back
		getHelper().enterClickAndLeave( new MouseEventData( this, trainEditFrame.addTrainButton ) );
		t = tmanager.getTrainByName("Test Train Name");
		Assert.assertNotNull("train added", t);
		
		trainEditFrame.dispose();
	}
	
	@SuppressWarnings("unchecked")
	private void pressDialogButton(JmriJFrame f, String buttonName){
		//  (with JfcUnit, not pushing this off to another thread)			                                            
		// Locate resulting dialog box
        List<javax.swing.JDialog> dialogList = new DialogFinder(null).findAll(f);
        javax.swing.JDialog d = dialogList.get(0);
        // Find the button
        AbstractButtonFinder finder = new AbstractButtonFinder(buttonName);
        javax.swing.JButton button = ( javax.swing.JButton ) finder.find( d, 0);
        Assert.assertNotNull("button not found", button);   
        // Click button
        getHelper().enterClickAndLeave( new MouseEventData( this, button ) );		
	}
	
	// Ensure minimal setup for log4J
	@Override
	protected void setUp() throws Exception {
        super.setUp();
		apps.tests.Log4JFixture.setUp();
	}

	public OperationsTrainsGuiTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", OperationsTrainsGuiTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(OperationsTrainsGuiTest.class);
		return suite;
	}

	// The minimal setup for log4J
	@Override
	protected void tearDown() throws Exception { 
	    apps.tests.Log4JFixture.tearDown();
        super.tearDown();
    }
}
