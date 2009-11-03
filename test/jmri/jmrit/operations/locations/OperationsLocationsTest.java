//OperationsLocationsTest.java

package jmri.jmrit.operations.locations;

import jmri.jmrit.XmlFile;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;

import java.util.List;
import java.io.File;
import javax.swing.JComboBox;
import java.awt.Dimension;
import java.awt.Point;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.jmrit.operations.rollingstock.engines.EngineManagerXml;
import jmri.jmrit.operations.rollingstock.cars.CarManagerXml;
import jmri.jmrit.operations.routes.RouteManagerXml;
import jmri.jmrit.operations.setup.OperationsXml;
import jmri.jmrit.operations.trains.TrainManagerXml;

/**
 * Tests for the Operations Locations class
 * Last manually cross-checked on 20090131
 * 
 * Still to do:
 *   ScheduleItem: XML read/write
 *   Schedule: Register, List, XML read/write
 *   Track: AcceptsDropTrain, AcceptsDropRoute
 *   Track: AcceptsPickupTrain, AcceptsPickupRoute
 *   Track: CheckScheduleValid
 *   Track: XML read/write
 *   Location: Track support  <-- I am here
 *   Location: XML read/write
 *  
 * @author	Bob Coleman Copyright (C) 2008, 2009
 * @version $Revision: 1.19 $
 */
public class OperationsLocationsTest extends TestCase {

	// test Location Class (part one)
	// test Location creation
	public void testCreate() {
		Location l = new Location("Test id", "Test Name");
		Assert.assertEquals("Location id", "Test id", l.getId());
		Assert.assertEquals("Location Name", "Test Name", l.getName());
		l.setName("New Test Name");
		Assert.assertEquals("New Location Name", "New Test Name", l.getName());
		l.setComment("Test Location Comment");
		Assert.assertEquals("Location Comment", "Test Location Comment", l.getComment());
	}

	// test Location public constants
	public void testLocationConstants() {
		Location l = new Location("Test id", "Test Name");
		Assert.assertEquals("Location id", "Test id", l.getId());
		Assert.assertEquals("Location Name", "Test Name", l.getName());

		Assert.assertEquals("Location Constant NORMAL", 1, Location.NORMAL);
		Assert.assertEquals("Location Constant STAGING", 2, Location.STAGING);

		Assert.assertEquals("Location Constant EAST", 1, Location.EAST);
		Assert.assertEquals("Location Constant WEST", 2, Location.WEST);
		Assert.assertEquals("Location Constant NORTH", 4, Location.NORTH);
		Assert.assertEquals("Location Constant SOUTH", 8, Location.SOUTH);

		Assert.assertEquals("Location Constant YARDLISTLENGTH_CHANGED_PROPERTY", "yardListLength", Location.YARDLISTLENGTH_CHANGED_PROPERTY);
		Assert.assertEquals("Location Constant SIDINGLISTLENGTH_CHANGED_PROPERTY", "sidingListLength", Location.SIDINGLISTLENGTH_CHANGED_PROPERTY);
		Assert.assertEquals("Location Constant INTERCHANGELISTLENGTH_CHANGED_PROPERTY", "sidingListLength", Location.INTERCHANGELISTLENGTH_CHANGED_PROPERTY);
		Assert.assertEquals("Location Constant STAGINGLISTLENGTH_CHANGED_PROPERTY", "sidingListLength", Location.STAGINGLISTLENGTH_CHANGED_PROPERTY);
		Assert.assertEquals("Location Constant TYPES_CHANGED_PROPERTY", "types", Location.TYPES_CHANGED_PROPERTY);
		Assert.assertEquals("Location Constant TRAINDIRECTION_CHANGED_PROPERTY", "trainDirection", Location.TRAINDIRECTION_CHANGED_PROPERTY);
		Assert.assertEquals("Location Constant LENGTH_CHANGED_PROPERTY", "length", Location.LENGTH_CHANGED_PROPERTY);
		Assert.assertEquals("Location Constant USEDLENGTH_CHANGED_PROPERTY", "usedLength", Location.USEDLENGTH_CHANGED_PROPERTY);
		Assert.assertEquals("Location Constant NAME_CHANGED_PROPERTY", "name", Location.NAME_CHANGED_PROPERTY);
		Assert.assertEquals("Location Constant SWITCHLIST_CHANGED_PROPERTY", "switchList", Location.SWITCHLIST_CHANGED_PROPERTY);
		Assert.assertEquals("Location Constant DISPOSE_CHANGED_PROPERTY", "dispose", Location.DISPOSE_CHANGED_PROPERTY);
	}

	// test ScheduleItem class
	// test ScheduleItem public constants
	public void testScheduleItemConstants() {
		Assert.assertEquals("Location ScheduleItem Constant NUMBER_CHANGED_PROPERTY", "number", ScheduleItem.NUMBER_CHANGED_PROPERTY);
		Assert.assertEquals("Location ScheduleItem Constant TYPE_CHANGED_PROPERTY", "type", ScheduleItem.TYPE_CHANGED_PROPERTY);
		Assert.assertEquals("Location ScheduleItem Constant ROAD_CHANGED_PROPERTY", "road", ScheduleItem.ROAD_CHANGED_PROPERTY);
		Assert.assertEquals("Location ScheduleItem Constant LOAD_CHANGED_PROPERTY", "load", ScheduleItem.LOAD_CHANGED_PROPERTY);
		Assert.assertEquals("Location ScheduleItem Constant DISPOSE", "dispose", ScheduleItem.DISPOSE);
	}

	// test ScheduleItem attributes
	public void testScheduleItemAttributes() {
		ScheduleItem ltsi = new ScheduleItem("Test id", "Test Type");
		Assert.assertEquals("Location ScheduleItem id", "Test id", ltsi.getId());
		Assert.assertEquals("Location ScheduleItem Type", "Test Type", ltsi.getType());

		ltsi.setType("New Test Type");
		Assert.assertEquals("Location ScheduleItem set Type", "New Test Type", ltsi.getType());

		ltsi.setComment("New Test Comment");
		Assert.assertEquals("Location ScheduleItem set Comment", "New Test Comment", ltsi.getComment());

		ltsi.setRoad("New Test Road");
		Assert.assertEquals("Location ScheduleItem set Road", "New Test Road", ltsi.getRoad());

		ltsi.setLoad("New Test Load");
		Assert.assertEquals("Location ScheduleItem set Load", "New Test Load", ltsi.getLoad());

		ltsi.setShip("New Test Ship");
		Assert.assertEquals("Location ScheduleItem set Ship", "New Test Ship", ltsi.getShip());

		ltsi.setSequenceId(22);
		Assert.assertEquals("Location ScheduleItem set SequenceId", 22, ltsi.getSequenceId());

		ltsi.setCount(222);
		Assert.assertEquals("Location ScheduleItem set Count", 222, ltsi.getCount());
	}

	// test Schedule class
	// test schedule public constants
	public void testScheduleConstants() {
		Assert.assertEquals("Location Schedule Constant LISTCHANGE_CHANGED_PROPERTY", "listChange", Schedule.LISTCHANGE_CHANGED_PROPERTY);
		Assert.assertEquals("Location Schedule Constant DISPOSE", "dispose", Schedule.DISPOSE);
	}

	// test schedule attributes
	public void testScheduleAttributes() {
		Schedule lts = new Schedule("Test id", "Test Name");
		Assert.assertEquals("Location Schedule id", "Test id", lts.getId());
		Assert.assertEquals("Location Schedule Name", "Test Name", lts.getName());

		lts.setName("New Test Name");
		Assert.assertEquals("Location Schedule set Name", "New Test Name", lts.getName());
		Assert.assertEquals("Location Schedule toString", "New Test Name", lts.toString());

		lts.setComment("New Test Comment");
		Assert.assertEquals("Location Schedule set Comment", "New Test Comment", lts.getComment());
	}

	// test schedule scheduleitem
	public void testScheduleScheduleItems() {
		Schedule lts = new Schedule("Test id", "Test Name");
		Assert.assertEquals("Location Schedule ScheduleItem id", "Test id", lts.getId());
		Assert.assertEquals("Location Schedule ScheduleItem Name", "Test Name", lts.getName());

		ScheduleItem ltsi1, ltsi2, ltsi3, ltsi4;

		ltsi1 = lts.addItem("New Test Type");
		Assert.assertEquals("Location Schedule ScheduleItem Check Type", "New Test Type", ltsi1.getType());

		String testid = ltsi1.getId();
		ltsi2 = lts.getItemByType("New Test Type");
		Assert.assertEquals("Location Schedule ScheduleItem Check Ids", testid, ltsi2.getId());
		Assert.assertEquals("Location Schedule ScheduleItem Check Seq 0", 1, ltsi2.getSequenceId());

		ltsi3 = lts.addItem("New Second Test Type");
		Assert.assertEquals("Location Schedule ScheduleItem Check Seq 1", 1, ltsi1.getSequenceId());
		Assert.assertEquals("Location Schedule ScheduleItem Check Seq 2", 2, ltsi3.getSequenceId());

		lts.moveItemUp(ltsi3);
		Assert.assertEquals("Location Schedule ScheduleItem Check Seq 3", 2, ltsi1.getSequenceId());
		Assert.assertEquals("Location Schedule ScheduleItem Check Seq 4", 1, ltsi3.getSequenceId());

		ltsi4 = lts.addItem("New Third Test Type", 2);
		Assert.assertEquals("Location Schedule ScheduleItem Check Seq 5", 3, ltsi1.getSequenceId());
		Assert.assertEquals("Location Schedule ScheduleItem Check Seq 6", 1, ltsi3.getSequenceId());
		Assert.assertEquals("Location Schedule ScheduleItem Check Seq 7", 2, ltsi4.getSequenceId());

		lts.moveItemDown(ltsi3);
		Assert.assertEquals("Location Schedule ScheduleItem Check Seq 8", 3, ltsi1.getSequenceId());
		Assert.assertEquals("Location Schedule ScheduleItem Check Seq 9", 2, ltsi3.getSequenceId());
		Assert.assertEquals("Location Schedule ScheduleItem Check Seq 10", 1, ltsi4.getSequenceId());

		lts.deleteItem(ltsi3);
		Assert.assertEquals("Location Schedule ScheduleItem Check Seq 8", 2, ltsi1.getSequenceId());
		Assert.assertEquals("Location Schedule ScheduleItem Check Seq 9", 1, ltsi4.getSequenceId());
	}

	public void testScheduleManager(){
		LocationManager lm = LocationManager.instance();
		Location l = lm.newLocation("new test location");
		Track t = l.addTrack("track 1", Track.SIDING);
		
		ScheduleManager sm = ScheduleManager.instance();
		
		// clear out any previous schedules
		sm.dispose();
		sm = ScheduleManager.instance();
		
		Schedule s1 = sm.newSchedule("new schedule");
		Schedule s2 = sm.newSchedule("newer schedule");
		ScheduleItem i1 = s1.addItem("BoxCar");
		i1.setRoad("new road");
		i1.setLoad("new load");
		i1.setShip("new ship load");
		ScheduleItem i2 = s1.addItem("Caboose");
		i2.setRoad("road");
		i2.setLoad("load");
		i2.setShip("ship load");
		
		Assert.assertEquals("1 First schedule name", "new schedule", s1.getName());
		Assert.assertEquals("1 First schedule name", "newer schedule", s2.getName());
		
		List<String> names = sm.getSchedulesByNameList();
		Assert.assertEquals("There should be 2 schedules", 2, names.size());
		Schedule sch1 = sm.getScheduleById(names.get(0));
		Schedule sch2 = sm.getScheduleById(names.get(1));
		Assert.assertEquals("2 First schedule name", "new schedule", sch1.getName());
		Assert.assertEquals("2 First schedule name", "newer schedule", sch2.getName());
		Assert.assertEquals("Schedule 1", sch1, sm.getScheduleByName("new schedule"));
		Assert.assertEquals("Schedule 2", sch2, sm.getScheduleByName("newer schedule"));
		
		JComboBox box = sm.getComboBox();
		Assert.assertEquals("3 First schedule name", "", box.getItemAt(0));
		Assert.assertEquals("3 First schedule name", sch1, box.getItemAt(1));
		Assert.assertEquals("3 First schedule name", sch2, box.getItemAt(2));
		
		JComboBox box2 = sm.getSidingsByScheduleComboBox(s1);
		Assert.assertEquals("First siding name", null, box2.getItemAt(0));
		
		// now add a schedule to siding
		t.setScheduleName("new schedule");
		
		JComboBox box3 = sm.getSidingsByScheduleComboBox(s1);
		LocationTrackPair ltp = (LocationTrackPair)box3.getItemAt(0);
		
		Assert.assertEquals("Location track pair location", l, ltp.getLocation()); 
		Assert.assertEquals("Location track pair track", t, ltp.getTrack()); 
		
		Assert.assertEquals("1 Schedule Item 1 type", "BoxCar", i1.getType());
		Assert.assertEquals("1 Schedule Item 1 road", "new road", i1.getRoad());
		Assert.assertEquals("1 Schedule Item 1 load", "new load", i1.getLoad());
		Assert.assertEquals("1 Schedule Item 1 ship", "new ship load", i1.getShip());
		
		Assert.assertEquals("1 Schedule Item 2 type", "Caboose", i2.getType());
		Assert.assertEquals("1 Schedule Item 2 road", "road", i2.getRoad());
		Assert.assertEquals("1 Schedule Item 2 load", "load", i2.getLoad());
		Assert.assertEquals("1 Schedule Item 2 ship", "ship load", i2.getShip());
		
		sm.replaceRoad("new road", "replaced road");
		
		Assert.assertEquals("2 Schedule Item 1 type", "BoxCar", i1.getType());
		Assert.assertEquals("2 Schedule Item 1 road", "replaced road", i1.getRoad());
		Assert.assertEquals("2 Schedule Item 1 load", "new load", i1.getLoad());
		Assert.assertEquals("2 Schedule Item 1 ship", "new ship load", i1.getShip());
		
		Assert.assertEquals("2 Schedule Item 2 type", "Caboose", i2.getType());
		Assert.assertEquals("2 Schedule Item 2 road", "road", i2.getRoad());
		Assert.assertEquals("2 Schedule Item 2 load", "load", i2.getLoad());
		Assert.assertEquals("2 Schedule Item 2 ship", "ship load", i2.getShip());
		
		sm.replaceType("BoxCar", "replaced car type");
		
		Assert.assertEquals("3 Schedule Item 1 type", "replaced car type", i1.getType());
		Assert.assertEquals("3 Schedule Item 1 road", "replaced road", i1.getRoad());
		Assert.assertEquals("3 Schedule Item 1 load", "new load", i1.getLoad());
		Assert.assertEquals("3 Schedule Item 1 ship", "new ship load", i1.getShip());
		
		Assert.assertEquals("3 Schedule Item 2 type", "Caboose", i2.getType());
		Assert.assertEquals("3 Schedule Item 2 road", "road", i2.getRoad());
		Assert.assertEquals("3 Schedule Item 2 load", "load", i2.getLoad());
		Assert.assertEquals("3 Schedule Item 2 ship", "ship load", i2.getShip());

		sm.replaceType("Caboose", "BoxCar");
		
		Assert.assertEquals("4 Schedule Item 1 type", "replaced car type", i1.getType());
		Assert.assertEquals("4 Schedule Item 1 road", "replaced road", i1.getRoad());
		Assert.assertEquals("4 Schedule Item 1 load", "new load", i1.getLoad());
		Assert.assertEquals("4 Schedule Item 1 ship", "new ship load", i1.getShip());
		
		Assert.assertEquals("4 Schedule Item 2 type", "BoxCar", i2.getType());
		Assert.assertEquals("4 Schedule Item 2 road", "road", i2.getRoad());
		Assert.assertEquals("4 Schedule Item 2 load", "load", i2.getLoad());
		Assert.assertEquals("4 Schedule Item 2 ship", "ship load", i2.getShip());
		
		sm.replaceLoad("BoxCar", "load", "new load");
		
		Assert.assertEquals("5 Schedule Item 1 type", "replaced car type", i1.getType());
		Assert.assertEquals("5 Schedule Item 1 road", "replaced road", i1.getRoad());
		Assert.assertEquals("5 Schedule Item 1 load", "new load", i1.getLoad());
		Assert.assertEquals("5 Schedule Item 1 ship", "new ship load", i1.getShip());
		
		Assert.assertEquals("5 Schedule Item 2 type", "BoxCar", i2.getType());
		Assert.assertEquals("5 Schedule Item 2 road", "road", i2.getRoad());
		Assert.assertEquals("5 Schedule Item 2 load", "new load", i2.getLoad());
		Assert.assertEquals("5 Schedule Item 2 ship", "ship load", i2.getShip());

		sm.replaceLoad("BoxCar", "new load", "next load");
		
		Assert.assertEquals("6 Schedule Item 1 type", "replaced car type", i1.getType());
		Assert.assertEquals("6 Schedule Item 1 road", "replaced road", i1.getRoad());
		Assert.assertEquals("6 Schedule Item 1 load", "new load", i1.getLoad());
		Assert.assertEquals("6 Schedule Item 1 ship", "new ship load", i1.getShip());
		
		Assert.assertEquals("6 Schedule Item 2 type", "BoxCar", i2.getType());
		Assert.assertEquals("6 Schedule Item 2 road", "road", i2.getRoad());
		Assert.assertEquals("6 Schedule Item 2 load", "next load", i2.getLoad());
		Assert.assertEquals("6 Schedule Item 2 ship", "ship load", i2.getShip());
		
		// remove all schedules
		sm.dispose();
		
		names = sm.getSchedulesByNameList();
		Assert.assertEquals("There should be no schedules", 0, names.size());
		
	}

	// test Track class
	// test Track public constants
	public void testTrackConstants() {
		Assert.assertEquals("Location Track Constant ANY", "Any", Track.ANY);
		Assert.assertEquals("Location Track Constant TRAINS", "trains", Track.TRAINS);
		Assert.assertEquals("Location Track Constant ROUTES", "routes", Track.ROUTES);

		Assert.assertEquals("Location Track Constant STAGING", "Staging", Track.STAGING);
		Assert.assertEquals("Location Track Constant INTERCHANGE", "Interchange", Track.INTERCHANGE);
		Assert.assertEquals("Location track Constant YARD", "Yard", Track.YARD);
		Assert.assertEquals("Location Track Constant SIDING", "Siding", Track.SIDING);

		Assert.assertEquals("Location Track Constant EAST", 1, Track.EAST);
		Assert.assertEquals("Location Track Constant WEST", 2, Track.WEST);
		Assert.assertEquals("Location track Constant NORTH", 4, Track.NORTH);
		Assert.assertEquals("Location Track Constant SOUTH", 8, Track.SOUTH);

		Assert.assertEquals("Location Track Constant ALLROADS", "All", Track.ALLROADS);
		Assert.assertEquals("Location Track Constant INCLUDEROADS", "Include", Track.INCLUDEROADS);
		Assert.assertEquals("Location track Constant EXCLUDEROADS", "Exclude", Track.EXCLUDEROADS);

		Assert.assertEquals("Location Track Constant TYPES_CHANGED_PROPERTY", "types", Track.TYPES_CHANGED_PROPERTY);
		Assert.assertEquals("Location Track Constant ROADS_CHANGED_PROPERTY", "roads", Track.ROADS_CHANGED_PROPERTY);
		Assert.assertEquals("Location track Constant SCHEDULE_CHANGED_PROPERTY", "schedule change", Track.SCHEDULE_CHANGED_PROPERTY);
		Assert.assertEquals("Location track Constant DISPOSE_CHANGED_PROPERTY", "dispose", Track.DISPOSE_CHANGED_PROPERTY);
	}

	// test Track attributes
	public void testTrackAttributes() {
		Track t = new Track("Test id", "Test Name", "Test Type");
		Assert.assertEquals("Location Track id", "Test id", t.getId());
		Assert.assertEquals("Location Track Name", "Test Name", t.getName());
		Assert.assertEquals("Location Track Type", "Test Type", t.getLocType());

		t.setName("New Test Name");
		Assert.assertEquals("Location Track set Name", "New Test Name", t.getName());

		t.setComment("New Test Comment");
		Assert.assertEquals("Location Track set Comment", "New Test Comment", t.getComment());

		t.setMoves(40);
		Assert.assertEquals("Location Track Moves", 40, t.getMoves());

		t.setLength(400);
		Assert.assertEquals("Location Track Length", 400, t.getLength());

		t.setReserved(200);
		Assert.assertEquals("Location Track Reserved", 200, t.getReserved());

		t.setUsedLength(100);
		Assert.assertEquals("Location Track Used Length", 100, t.getUsedLength());

		t.setTrainDirections(Track.NORTH);
		Assert.assertEquals("Location Track Direction North", Track.NORTH, t.getTrainDirections());

		t.setTrainDirections(Track.SOUTH);
		Assert.assertEquals("Location Track Direction South", Track.SOUTH, t.getTrainDirections());

		t.setTrainDirections(Track.EAST);
		Assert.assertEquals("Location Track Direction East", Track.EAST, t.getTrainDirections());

		t.setTrainDirections(Track.WEST);
		Assert.assertEquals("Location Track Direction West", Track.WEST, t.getTrainDirections());

		t.setTrainDirections(Track.NORTH+Track.SOUTH);
		Assert.assertEquals("Location Track Direction North+South", Track.NORTH+Track.SOUTH, t.getTrainDirections());

		t.setTrainDirections(Track.EAST+Track.WEST);
		Assert.assertEquals("Location Track Direction East+West", Track.EAST+Track.WEST, t.getTrainDirections());

		t.setTrainDirections(Track.NORTH+Track.SOUTH+Track.EAST+Track.WEST);
		Assert.assertEquals("Location Track Direction North+South+East+West", Track.NORTH+Track.SOUTH+Track.EAST+Track.WEST, t.getTrainDirections());

		t.setRoadOption("New Test Road Option");
		Assert.assertEquals("Location Track set Road Option", "New Test Road Option", t.getRoadOption());

		t.setDropOption("New Test Drop Option");
		Assert.assertEquals("Location Track set Drop Option", "New Test Drop Option", t.getDropOption());

		t.setPickupOption("New Test Pickup Option");
		Assert.assertEquals("Location Track set Pickup Option", "New Test Pickup Option", t.getPickupOption());
	}

	// test Track car support
	public void testTrackCarSupport() {
		Track t = new Track("Test id", "Test Name", "Test Type");
		Assert.assertEquals("Location Track Car id", "Test id", t.getId());
		Assert.assertEquals("Location Track Car Name", "Test Name", t.getName());
		Assert.assertEquals("Location Track Car Type", "Test Type", t.getLocType());

		Assert.assertEquals("Location Track Car Start Used Length", 0, t.getUsedLength());
		Assert.assertEquals("Location Track Car Start Number of Rolling Stock", 0, t.getNumberRS());
		Assert.assertEquals("Location Track Car Start Number of Cars", 0, t.getNumberCars());
		Assert.assertEquals("Location Track Car Start Number of Engines", 0, t.getNumberEngines());

		jmri.jmrit.operations.rollingstock.cars.Car c1 = new jmri.jmrit.operations.rollingstock.cars.Car("TESTROAD", "TESTNUMBER1");
		c1.setLength("40");
		t.addRS(c1);

		Assert.assertEquals("Location Track Car First Number of Rolling Stock", 1, t.getNumberRS());
		Assert.assertEquals("Location Track Car First Number of Cars", 1, t.getNumberCars());
		Assert.assertEquals("Location Track Car First Number of Engines", 0, t.getNumberEngines());
		Assert.assertEquals("Location Track Car First Used Length", 40+4, t.getUsedLength()); // Drawbar length is 4

		jmri.jmrit.operations.rollingstock.cars.Car c2 = new jmri.jmrit.operations.rollingstock.cars.Car("TESTROAD", "TESTNUMBER2");
		c2.setLength("33");
		t.addRS(c2);

		Assert.assertEquals("Location Track Car 2nd Number of Rolling Stock", 2, t.getNumberRS());
		Assert.assertEquals("Location Track Car 2nd Number of Cars", 2, t.getNumberCars());
		Assert.assertEquals("Location Track Car 2nd Number of Engines", 0, t.getNumberEngines());
		Assert.assertEquals("Location Track Car 2nd Used Length", 40+4+33+4, t.getUsedLength()); // Drawbar length is 4

		jmri.jmrit.operations.rollingstock.engines.Engine e1 = new jmri.jmrit.operations.rollingstock.engines.Engine("TESTROAD", "TESTNUMBERE1");
		e1.setModel("E8");  // Default length == 70
		t.addRS(e1);

		Assert.assertEquals("Location Track Car 3rd Number of Rolling Stock", 3, t.getNumberRS());
		Assert.assertEquals("Location Track Car 3rd Number of Cars", 2, t.getNumberCars());
		Assert.assertEquals("Location Track Car 3rd Number of Engines", 1, t.getNumberEngines());
		Assert.assertEquals("Location Track Car 3rd Used Length", 40+4+33+4+70+4, t.getUsedLength()); // Drawbar length is 4

		jmri.jmrit.operations.rollingstock.cars.Car c3 = new jmri.jmrit.operations.rollingstock.cars.Car("TESTROAD", "TESTNUMBER3");
		c3.setLength("50");
		t.addRS(c3);

		Assert.assertEquals("Location Track Car 4th Number of Rolling Stock", 4, t.getNumberRS());
		Assert.assertEquals("Location Track Car 4th Number of Cars", 3, t.getNumberCars());
		Assert.assertEquals("Location Track Car 4th Number of Engines", 1, t.getNumberEngines());
		Assert.assertEquals("Location Track Car 4th Used Length", 40+4+33+4+70+4+50+4, t.getUsedLength()); // Drawbar length is 4

		jmri.jmrit.operations.rollingstock.engines.Engine e2 = new jmri.jmrit.operations.rollingstock.engines.Engine("TESTROAD", "TESTNUMBERE2");
		e2.setModel("E8");  // Default length == 70
		t.addRS(e2);

		Assert.assertEquals("Location Track Car 5th Number of Rolling Stock", 5, t.getNumberRS());
		Assert.assertEquals("Location Track Car 5th Number of Cars", 3, t.getNumberCars());
		Assert.assertEquals("Location Track Car 5th Number of Engines", 2, t.getNumberEngines());
		Assert.assertEquals("Location Track Car 5th Used Length", 40+4+33+4+70+4+50+4+70+4, t.getUsedLength()); // Drawbar length is 4

		t.deleteRS(c2);

		Assert.assertEquals("Location Track Car 6th Number of Rolling Stock", 4, t.getNumberRS());
		Assert.assertEquals("Location Track Car 6th Number of Cars", 2, t.getNumberCars());
		Assert.assertEquals("Location Track Car 6th Number of Engines", 2, t.getNumberEngines());
		Assert.assertEquals("Location Track Car 6th Used Length", 40+4+70+4+50+4+70+4, t.getUsedLength()); // Drawbar length is 4

		t.deleteRS(c1);

		Assert.assertEquals("Location Track Car 7th Number of Rolling Stock", 3, t.getNumberRS());
		Assert.assertEquals("Location Track Car 7th Number of Cars", 1, t.getNumberCars());
		Assert.assertEquals("Location Track Car 7th Number of Engines", 2, t.getNumberEngines());
		Assert.assertEquals("Location Track Car 7th Used Length", 70+4+50+4+70+4, t.getUsedLength()); // Drawbar length is 4

		t.deleteRS(e2);

		Assert.assertEquals("Location Track Car 8th Number of Rolling Stock", 2, t.getNumberRS());
		Assert.assertEquals("Location Track Car 8th Number of Cars", 1, t.getNumberCars());
		Assert.assertEquals("Location Track Car 8th Number of Engines", 1, t.getNumberEngines());
		Assert.assertEquals("Location Track Car 8th Used Length", 70+4+50+4, t.getUsedLength()); // Drawbar length is 4

		t.deleteRS(e1);

		Assert.assertEquals("Location Track Car 9th Number of Rolling Stock", 1, t.getNumberRS());
		Assert.assertEquals("Location Track Car 9th Number of Cars", 1, t.getNumberCars());
		Assert.assertEquals("Location Track Car 9th Number of Engines", 0, t.getNumberEngines());
		Assert.assertEquals("Location Track Car 9th Used Length", 50+4, t.getUsedLength()); // Drawbar length is 4

		t.deleteRS(c3);

		Assert.assertEquals("Location Track Car Last Number of Rolling Stock", 0, t.getNumberRS());
		Assert.assertEquals("Location Track Car Last Number of Cars", 0, t.getNumberCars());
		Assert.assertEquals("Location Track Car Last Number of Engines", 0, t.getNumberEngines());
		Assert.assertEquals("Location Track Car Last Used Length", 0, t.getUsedLength()); // Drawbar length is 4
	}

	// test Track pickup support
	public void testTrackPickUpSupport() {
		Track t = new Track("Test id", "Test Name", "Test Type");
		Assert.assertEquals("Location Track Car id", "Test id", t.getId());
		Assert.assertEquals("Location Track Car Name", "Test Name", t.getName());
		Assert.assertEquals("Location Track Car Type", "Test Type", t.getLocType());

		Assert.assertEquals("Location Track Pick Ups Start", 0, t.getPickupRS());

		t.addPickupRS();
		Assert.assertEquals("Location Track Pick Ups 1st", 1, t.getPickupRS());

		t.addPickupRS();
		Assert.assertEquals("Location Track Pick Ups 2nd", 2, t.getPickupRS());

		t.deletePickupRS();
		Assert.assertEquals("Location Track Pick Ups 3rd", 1, t.getPickupRS());

		t.deletePickupRS();
		Assert.assertEquals("Location Track Pick Ups 4th", 0, t.getPickupRS());
	}

	// test Track drop support
	public void testTrackDropSupport() {
		Track t = new Track("Test id", "Test Name", "Test Type");
		Assert.assertEquals("Location Track Car id", "Test id", t.getId());
		Assert.assertEquals("Location Track Car Name", "Test Name", t.getName());
		Assert.assertEquals("Location Track Car Type", "Test Type", t.getLocType());

		Assert.assertEquals("Location Track Drops Start", 0, t.getDropRS());
		Assert.assertEquals("Location Track Drops Start Reserved", 0, t.getReserved());

		jmri.jmrit.operations.rollingstock.cars.Car c1 = new jmri.jmrit.operations.rollingstock.cars.Car("TESTROAD", "TESTNUMBER1");
		c1.setLength("40");
		t.addDropRS(c1);
		Assert.assertEquals("Location Track Drops 1st", 1, t.getDropRS());
		Assert.assertEquals("Location Track Drops 1st Reserved", 40+4, t.getReserved());

		jmri.jmrit.operations.rollingstock.cars.Car c2 = new jmri.jmrit.operations.rollingstock.cars.Car("TESTROAD", "TESTNUMBER2");
		c2.setLength("50");
		t.addDropRS(c2);
		Assert.assertEquals("Location Track Drops 2nd", 2, t.getDropRS());
		Assert.assertEquals("Location Track Drops 2nd Reserved", 40+4+50+4, t.getReserved());

		t.deleteDropRS(c2);
		Assert.assertEquals("Location Track Drops 3rd", 1, t.getDropRS());
		Assert.assertEquals("Location Track Drops 3rd Reserved", 40+4, t.getReserved());

		t.deleteDropRS(c1);
		Assert.assertEquals("Location Track Drops 4th", 0, t.getDropRS());
		Assert.assertEquals("Location Track Drops 4th Reserved", 0, t.getReserved());
	}

	// test Track typename support
	public void testTrackTypeNameSupport() {
		Track t = new Track("Test id", "Test Name", "Test Type");
		Assert.assertEquals("Location Track Car id", "Test id", t.getId());
		Assert.assertEquals("Location Track Car Name", "Test Name", t.getName());
		Assert.assertEquals("Location Track Car Type", "Test Type", t.getLocType());

		/* Test Type Name */
		Assert.assertEquals("Location Track Accepts Type Name undefined", false, t.acceptsTypeName("TestTypeName"));

		t.addTypeName("TestTypeName");
		Assert.assertEquals("Location Track Accepts Type Name defined", false, t.acceptsTypeName("TestTypeName"));

		// now add to car types
		CarTypes ct = CarTypes.instance();
		ct.addName("TestTypeName");
		t.addTypeName("TestTypeName");
		Assert.assertEquals("Location Track Accepts Type Name defined after ct", true, t.acceptsTypeName("TestTypeName"));

		t.deleteTypeName("TestTypeName");
		Assert.assertEquals("Location Track Accepts Type Name deleted", false, t.acceptsTypeName("TestTypeName"));

		/* Needed so later tests will behave correctly */
		ct.deleteName("TestTypeName");

		ct.addName("Baggager");

		t.addTypeName("Baggager");

		Assert.assertEquals("Location Track Accepts Type Name Baggager", true, t.acceptsTypeName("Baggager"));

		/* Test Road Name */
		t.setRoadOption(Track.INCLUDEROADS);
		Assert.assertEquals("Location Track set Road Option INCLUDEROADS", "Include", t.getRoadOption());

		Assert.assertEquals("Location Track Accepts Road Name undefined", false, t.acceptsRoadName("TestRoadName"));

		t.addRoadName("TestRoadName");
		Assert.assertEquals("Location Track Accepts Road Name defined", true, t.acceptsRoadName("TestRoadName"));

		t.addRoadName("TestOtherRoadName");
		Assert.assertEquals("Location Track Accepts Road Name other defined", true, t.acceptsRoadName("TestRoadName"));

		t.deleteRoadName("TestRoadName");
		Assert.assertEquals("Location Track Accepts Road Name deleted", false, t.acceptsRoadName("TestRoadName"));

		t.setRoadOption(Track.ALLROADS);
		Assert.assertEquals("Location Track set Road Option AllROADS", "All", t.getRoadOption());
		Assert.assertEquals("Location Track Accepts All Road Names", true, t.acceptsRoadName("TestRoadName"));

		t.setRoadOption(Track.EXCLUDEROADS);
		Assert.assertEquals("Location Track set Road Option EXCLUDEROADS", "Exclude", t.getRoadOption());
		Assert.assertEquals("Location Track Excludes Road Names", true, t.acceptsRoadName("TestRoadName"));

		t.addRoadName("TestRoadName");
		Assert.assertEquals("Location Track Excludes Road Names 2", false, t.acceptsRoadName("TestRoadName"));

		/* Test Drop IDs */
		Assert.assertEquals("Location Track Accepts Drop ID undefined", false, t.containsDropId("TestDropId"));

		t.addDropId("TestDropId");
		Assert.assertEquals("Location Track Accepts Drop ID defined", true, t.containsDropId("TestDropId"));

		t.addDropId("TestOtherDropId");
		Assert.assertEquals("Location Track Accepts Drop ID other defined", true, t.containsDropId("TestDropId"));

		t.deleteDropId("TestDropId");
		Assert.assertEquals("Location Track Accepts Drop ID deleted", false, t.containsDropId("TestDropId"));

		/* Test Pickup IDs */
		Assert.assertEquals("Location Track Accepts Pickup ID undefined", false, t.containsPickupId("TestPickupId"));

		t.addPickupId("TestPickupId");
		Assert.assertEquals("Location Track Accepts Pickup ID defined", true, t.containsPickupId("TestPickupId"));

		t.addPickupId("TestOtherPickupId");
		Assert.assertEquals("Location Track Accepts Pickup ID other defined", true, t.containsPickupId("TestPickupId"));

		t.deletePickupId("TestPickupId");
		Assert.assertEquals("Location Track Accepts Pickup ID deleted", false, t.containsPickupId("TestPickupId"));
	}

	// test Track schedule support
	public void testTrackScheduleSupport() {
		Track t = new Track("Test id", "Test Name", "Test Type");
		Assert.assertEquals("Location Track Car id", "Test id", t.getId());
		Assert.assertEquals("Location Track Car Name", "Test Name", t.getName());
		Assert.assertEquals("Location Track Car Type", "Test Type", t.getLocType());

		t.setScheduleName("Test Schedule Name");
		Assert.assertEquals("Location Track set Schedule Name", "Test Schedule Name", t.getScheduleName());
		t.setScheduleItemId("Test Schedule Item Id");
		Assert.assertEquals("Location Track set Schedule Item Id", "Test Schedule Item Id", t.getScheduleItemId());
		t.setScheduleCount(2);
		Assert.assertEquals("Location Track set Schedule Count", 2, t.getScheduleCount());

	}

	// test Track load support
	public void testTrackLoadSupport() {
		Track t = new Track("Test id", "Test Name", "Test Type");
		Assert.assertEquals("Location Track Car id", "Test id", t.getId());
		Assert.assertEquals("Location Track Car Name", "Test Name", t.getName());
		Assert.assertEquals("Location Track Car Type", "Test Type", t.getLocType());

		/* Test Load Swapable */
		Assert.assertEquals("Location Track Load Swapable default", false, t.isLoadSwapEnabled());
		t.enableLoadSwaps(true);
		Assert.assertEquals("Location Track Load Swapable true", true, t.isLoadSwapEnabled());
		t.enableLoadSwaps(false);
		Assert.assertEquals("Location Track Load Swapable false", false, t.isLoadSwapEnabled());

		/* Test Remove Loads */
		Assert.assertEquals("Location Track Remove Loads default", false, t.isRemoveLoadsEnabled());
		t.enableRemoveLoads(true);
		Assert.assertEquals("Location Track Remove Loads true", true, t.isRemoveLoadsEnabled());
		t.enableRemoveLoads(false);
		Assert.assertEquals("Location Track Remove Loads false", false, t.isRemoveLoadsEnabled());

		/* Test Add Loads */
		Assert.assertEquals("Location Track Add Loads default", false, t.isAddLoadsEnabled());
		t.enableAddLoads(true);
		Assert.assertEquals("Location Track Add Loads true", true, t.isAddLoadsEnabled());
		t.enableAddLoads(false);
		Assert.assertEquals("Location Track Add Loads false", false, t.isAddLoadsEnabled());
	}

	// test Locations class (part two)
	// test length attributes
	public void testLengthAttributes() {
		Location l = new Location("Test id", "Test Name");
		Assert.assertEquals("Location id", "Test id", l.getId());
		Assert.assertEquals("Location Name", "Test Name", l.getName());

		l.setLength(400);
		Assert.assertEquals("Location Length", 400, l.getLength());

		l.setUsedLength(200);
		Assert.assertEquals("Location Used Length", 200, l.getUsedLength());
	}

	// test operation attributes
	public void testOperationAttributes() {
		Location l = new Location("Test id", "Test Name");
		Assert.assertEquals("Location id", "Test id", l.getId());
		Assert.assertEquals("Location Name", "Test Name", l.getName());

		l.setLocationOps(Location.STAGING);
		Assert.assertEquals("Location Ops Staging", Location.STAGING, l.getLocationOps());

		l.setLocationOps(Location.NORMAL);
		Assert.assertEquals("Location Ops Normal", Location.NORMAL, l.getLocationOps());
	}

	// test direction attributes
	public void testDirectionAttributes() {
		Location l = new Location("Test id", "Test Name");
		Assert.assertEquals("Location id", "Test id", l.getId());
		Assert.assertEquals("Location Name", "Test Name", l.getName());

		l.setTrainDirections(Location.NORTH);
		Assert.assertEquals("Location Direction North", Location.NORTH, l.getTrainDirections());

		l.setTrainDirections(Location.SOUTH);
		Assert.assertEquals("Location Direction South", Location.SOUTH, l.getTrainDirections());

		l.setTrainDirections(Location.EAST);
		Assert.assertEquals("Location Direction East", Location.EAST, l.getTrainDirections());

		l.setTrainDirections(Location.WEST);
		Assert.assertEquals("Location Direction West", Location.WEST, l.getTrainDirections());

		l.setTrainDirections(Location.NORTH+Location.SOUTH);
		Assert.assertEquals("Location Direction North+South", Location.NORTH+Location.SOUTH, l.getTrainDirections());

		l.setTrainDirections(Location.EAST+Location.WEST);
		Assert.assertEquals("Location Direction East+West", Location.EAST+Location.WEST, l.getTrainDirections());

		l.setTrainDirections(Location.NORTH+Location.SOUTH+Location.EAST+Location.WEST);
		Assert.assertEquals("Location Direction North+South+East+West", Location.NORTH+Location.SOUTH+Location.EAST+Location.WEST, l.getTrainDirections());
	}

	// test car attributes
	public void testCarAttributes() {
		Location l = new Location("Test id", "Test Name");
		Assert.assertEquals("Location id", "Test id", l.getId());
		Assert.assertEquals("Location Name", "Test Name", l.getName());

		l.setNumberRS(8);
		Assert.assertEquals("Location Number of Cars", 8, l.getNumberRS());
	}

	// test switchlist attributes
	public void testSwitchlistAttributes() {
		Location l = new Location("Test id", "Test Name");
		Assert.assertEquals("Location id", "Test id", l.getId());
		Assert.assertEquals("Location Name", "Test Name", l.getName());

		l.setSwitchList(true);
		Assert.assertEquals("Location Switch List True", true, l.getSwitchList());

		l.setSwitchList(false);
		Assert.assertEquals("Location Switch List True", false, l.getSwitchList());
	}

	// test typename support
	public void testTypeNameSupport() {
		// use LocationManager to allow replace car type to work properly
		Location l = LocationManager.instance().newLocation("Test Name");
		Assert.assertEquals("Location Name", "Test Name", l.getName());

		Assert.assertEquals("Location Accepts Type Name undefined", false, l.acceptsTypeName("TestTypeName"));

		l.addTypeName("TestTypeName");
		Assert.assertEquals("Location Accepts Type Name defined", false, l.acceptsTypeName("TestTypeName"));

		// now add to car types
		CarTypes ct = CarTypes.instance();
		ct.addName("TestTypeName");
		l.addTypeName("TestTypeName");
		Assert.assertEquals("Location Accepts Type Name defined", true, l.acceptsTypeName("TestTypeName"));

		l.deleteTypeName("TestTypeName");
		Assert.assertEquals("Location Accepts Type Name undefined2", false, l.acceptsTypeName("TestTypeName"));

		ct.addName("Baggage");
		ct.addName("BoxCar");
		ct.addName("Caboose");
		ct.addName("Coal");
		ct.addName("Engine");
		ct.addName("Hopper");
		ct.addName("MOW");
		ct.addName("Passenger");
		ct.addName("Reefer");
		ct.addName("Stock");
		ct.addName("Tank Oil");

		l.addTypeName("Baggage");
		l.addTypeName("BoxCar");
		l.addTypeName("Caboose");
		l.addTypeName("Coal");
		l.addTypeName("Engine");
		l.addTypeName("Hopper");
		l.addTypeName("MOW");
		l.addTypeName("Passenger");
		l.addTypeName("Reefer");
		l.addTypeName("Stock");
		l.addTypeName("Tank Oil");

		Track t = l.addTrack("new track", Track.SIDING);

		Assert.assertEquals("Location Accepts Type Name BoxCar", true, l.acceptsTypeName("BoxCar"));
		Assert.assertEquals("Location Accepts Type Name boxCar", false, l.acceptsTypeName("boxCar"));
		Assert.assertEquals("Location Accepts Type Name MOW", true, l.acceptsTypeName("MOW"));
		Assert.assertEquals("Location Accepts Type Name Caboose", true, l.acceptsTypeName("Caboose"));
		Assert.assertEquals("Location Accepts Type Name BoxCar", true, l.acceptsTypeName("BoxCar"));
		Assert.assertEquals("Location Accepts Type Name undefined3", false, l.acceptsTypeName("TestTypeName"));

		Assert.assertEquals("Track Accepts Type Name BoxCar", true, t.acceptsTypeName("BoxCar"));
		Assert.assertEquals("Track Accepts Type Name boxCar", false, t.acceptsTypeName("boxCar"));
		Assert.assertEquals("Track Accepts Type Name MOW", true, t.acceptsTypeName("MOW"));
		Assert.assertEquals("Track Accepts Type Name Caboose", true, t.acceptsTypeName("Caboose"));
		Assert.assertEquals("Track Accepts Type Name undefined3", false, t.acceptsTypeName("undefined"));

		t.addTypeName("Baggage");
		t.addTypeName("BoxCar");
		t.addTypeName("Caboose");
		t.addTypeName("Coal");
		t.addTypeName("Engine");
		t.addTypeName("Hopper");
		t.addTypeName("MOW");
		t.addTypeName("Passenger");
		t.addTypeName("Reefer");
		t.addTypeName("Stock");
		t.addTypeName("Tank Oil");

		Assert.assertEquals("Track Accepts Type Name BoxCar", true, t.acceptsTypeName("BoxCar"));
		Assert.assertEquals("Track Accepts Type Name boxCar", false, t.acceptsTypeName("boxCar"));
		Assert.assertEquals("Track Accepts Type Name MOW", true, t.acceptsTypeName("MOW"));
		Assert.assertEquals("Track Accepts Type Name Caboose", true, t.acceptsTypeName("Caboose"));
		Assert.assertEquals("Track Accepts Type Name BoxCar", true, t.acceptsTypeName("BoxCar"));
		Assert.assertEquals("Track Accepts Type Name undefined3", false, t.acceptsTypeName("undefined"));

		// test replace	

		// also test replace type in schedules
		ScheduleManager sm = ScheduleManager.instance();
		Schedule s = sm.newSchedule("newest schedule");
		ScheduleItem i1 = s.addItem("BoxCar");
		ScheduleItem i2 = s.addItem("Caboose");

		Assert.assertEquals("ScheudleItem i1 Type BoxCar", "BoxCar", i1.getType());
		Assert.assertEquals("ScheudleItem i2 Type Caboose", "Caboose", i2.getType());

		ct.replaceName("BoxCar", "boxcar");

		Assert.assertFalse("Location Does Not Accepts Type Name BoxCar", l.acceptsTypeName("BoxCar"));
		Assert.assertTrue("Location Accepts Type Name boxcar", l.acceptsTypeName("boxcar"));
		Assert.assertFalse("Track Does Not Accepts Type Name BoxCar", l.acceptsTypeName("BoxCar"));
		Assert.assertTrue("Track Accepts Type Name boxcar", t.acceptsTypeName("boxcar"));
		Assert.assertEquals("ScheudleItem i1 Type boxcar", "boxcar", i1.getType());
		Assert.assertEquals("Check ScheudleItem i2 Type Caboose", "Caboose", i2.getType());
		
		// remove all schedules
		sm.dispose();
	}

	public void testRoadNameSupport(){
		// use LocationManager to allow replace car road to work properly
		Location l = LocationManager.instance().newLocation("Test Name 2");
		Assert.assertEquals("Location Name", "Test Name 2", l.getName());

		Track t = l.addTrack("new track", Track.SIDING);

		t.setRoadOption(Track.INCLUDEROADS);
		t.addRoadName("Test Road Name");
		t.addRoadName("Test Road Name 2");

		ScheduleManager sm = ScheduleManager.instance();
		Schedule s = sm.newSchedule("test schedule");
		ScheduleItem i1 = s.addItem("BoxCar");
		ScheduleItem i2 = s.addItem("BoxCar");
		i1.setRoad("Test Road Name");
		i2.setRoad("Test Road Name 2");

		Assert.assertTrue("track should accept road Test Road Name", t.acceptsRoadName("Test Road Name"));
		Assert.assertTrue("track should accept road Test Road Name 2", t.acceptsRoadName("Test Road Name 2"));
		Assert.assertFalse("track should Not accept road New Test Road Name", t.acceptsRoadName("New Test Road Name"));
		Assert.assertEquals("ScheudleItem i1 Road Test Road Name", "Test Road Name", i1.getRoad());
		Assert.assertEquals("ScheudleItem i2 Road Test Road Name", "Test Road Name 2", i2.getRoad());

		CarRoads cr = CarRoads.instance();
		cr.replaceName("Test Road Name", "New Test Road Name");

		Assert.assertFalse("track should Not accept road Test Road Name", t.acceptsRoadName("Test Road Name"));
		Assert.assertTrue("track should accept road Test Road Name 2", t.acceptsRoadName("Test Road Name 2"));
		Assert.assertTrue("track should accept road New Test Road Name", t.acceptsRoadName("New Test Road Name"));
		Assert.assertEquals("ScheudleItem i1 Road Test Road Name", "New Test Road Name", i1.getRoad());
		Assert.assertEquals("Check ScheudleItem i2 Road Test Road Name", "Test Road Name 2", i2.getRoad());
	
		// remove all schedules
		sm.dispose();
	}

	// test pickup support
	public void testPickUpSupport() {
		Location l = new Location("Test id", "Test Name");
		Assert.assertEquals("Location id", "Test id", l.getId());
		Assert.assertEquals("Location Name", "Test Name", l.getName());

		Assert.assertEquals("Location Pick Ups Start Condition", 0 , l.getPickupRS());

		l.addPickupRS();
		Assert.assertEquals("Location Pick Up 1", 1, l.getPickupRS());

		l.addPickupRS();
		Assert.assertEquals("Location Pick Up second", 2, l.getPickupRS());

		l.deletePickupRS();
		Assert.assertEquals("Location Delete Pick Up", 1, l.getPickupRS());

		l.deletePickupRS();
		Assert.assertEquals("Location Delete Pick Up second", 0, l.getPickupRS());
	}

	// test drop support
	public void testDropSupport() {
		Location l = new Location("Test id", "Test Name");
		Assert.assertEquals("Location id", "Test id", l.getId());
		Assert.assertEquals("Location Name", "Test Name", l.getName());

		Assert.assertEquals("Location Drop Start Condition", 0 , l.getPickupRS());

		l.addDropRS();
		Assert.assertEquals("Location Drop 1", 1, l.getDropRS());

		l.addDropRS();
		Assert.assertEquals("Location Drop second", 2, l.getDropRS());

		l.deleteDropRS();
		Assert.assertEquals("Location Delete Drop", 1, l.getDropRS());

		l.deleteDropRS();
		Assert.assertEquals("Location Delete Drop second", 0, l.getDropRS());
	}

	// test car support
	public void testCarSupport() {
		Location l = new Location("Test id", "Test Name");
		Assert.assertEquals("Location id", "Test id", l.getId());
		Assert.assertEquals("Location Name", "Test Name", l.getName());

		Assert.assertEquals("Location Used Length", 0, l.getUsedLength());
		Assert.assertEquals("Location Number of Cars", 0, l.getNumberRS());

		jmri.jmrit.operations.rollingstock.cars.Car c1 = new jmri.jmrit.operations.rollingstock.cars.Car("TESTROAD", "TESTNUMBER1");
		c1.setLength("40");
		l.addRS(c1);

		Assert.assertEquals("Location Number of Cars", 1, l.getNumberRS());
		Assert.assertEquals("Location Used Length one car", 44, l.getUsedLength()); // Drawbar length is 4

		jmri.jmrit.operations.rollingstock.cars.Car c2 = new jmri.jmrit.operations.rollingstock.cars.Car("TESTROAD", "TESTNUMBER2");
		c2.setLength("33");
		l.addRS(c2);

		Assert.assertEquals("Location Number of Cars", 2, l.getNumberRS());
		Assert.assertEquals("Location Used Length one car", 40+4+33+4, l.getUsedLength()); // Drawbar length is 4

		jmri.jmrit.operations.rollingstock.cars.Car c3 = new jmri.jmrit.operations.rollingstock.cars.Car("TESTROAD", "TESTNUMBER3");
		c3.setLength("50");
		l.addRS(c3);

		Assert.assertEquals("Location Number of Cars", 3, l.getNumberRS());
		Assert.assertEquals("Location Used Length one car", 40+4+33+4+50+4, l.getUsedLength()); // Drawbar length is 4

		l.deleteRS(c2);

		Assert.assertEquals("Location Number of Cars", 2, l.getNumberRS());
		Assert.assertEquals("Location Used Length one car", 40+4+50+4, l.getUsedLength()); // Drawbar length is 4

		l.deleteRS(c1);

		Assert.assertEquals("Location Number of Cars", 1, l.getNumberRS());
		Assert.assertEquals("Location Used Length one car", 50+4, l.getUsedLength()); // Drawbar length is 4

		l.deleteRS(c3);

		Assert.assertEquals("Location Number of Cars", 0, l.getNumberRS());
		Assert.assertEquals("Location Used Length one car", 0, l.getUsedLength()); // Drawbar length is 4
	}

	// test car duplicates support
	public void testCarDuplicatesSupport() {
		Location l = new Location("Test id", "Test Name");
		Assert.assertEquals("Location id", "Test id", l.getId());
		Assert.assertEquals("Location Name", "Test Name", l.getName());

		Assert.assertEquals("Location Used Length", 0, l.getUsedLength());
		Assert.assertEquals("Location Number of Cars", 0, l.getNumberRS());

		jmri.jmrit.operations.rollingstock.cars.Car c1 = new jmri.jmrit.operations.rollingstock.cars.Car("TESTROAD", "TESTNUMBER1");
		c1.setLength("40");
		l.addRS(c1);

		Assert.assertEquals("Location Number of Cars", 1, l.getNumberRS());
		Assert.assertEquals("Location Used Length one car", 44, l.getUsedLength()); // Drawbar length is 4

		jmri.jmrit.operations.rollingstock.cars.Car c2 = new jmri.jmrit.operations.rollingstock.cars.Car("TESTROAD", "TESTNUMBER2");
		c2.setLength("33");
		l.addRS(c2);

		Assert.assertEquals("Location Number of Cars", 2, l.getNumberRS());
		Assert.assertEquals("Location Used Length one car", 40+4+33+4, l.getUsedLength()); // Drawbar length is 4

		l.addRS(c1);

		Assert.assertEquals("Location Number of Cars", 3, l.getNumberRS());
		Assert.assertEquals("Location Used Length one car", 40+4+33+4+40+4, l.getUsedLength()); // Drawbar length is 4

	}

	// test location Xml create support
	public void testXMLCreate() {
		LocationManager manager = LocationManager.instance();
		manager.dispose();
		// dispose kills instance, so reload manager
		manager = LocationManager.instance();
		
		// load options
		LocationEditFrame frame = new LocationEditFrame();
		frame.setSize(10, 12);
		frame.setLocation(14, 16);
		manager.setLocationEditFrame(frame);
		
		// now load locations
		List<String> locationList = manager.getLocationsByIdList();
		Assert.assertEquals("Starting Number of Locations", 0, locationList.size());
		Location l1 = manager.newLocation("Test Location 2");
		Location l2 = manager.newLocation("Test Location 1");
		Location l3 = manager.newLocation("Test Location 3");
		
		Track t1 = l1.addTrack("A Yard", Track.YARD);
		Track t2 = l1.addTrack("A Siding", Track.SIDING);
		Track t3 =l2.addTrack("An Interchange", Track.INTERCHANGE);
		Track t4 =l3.addTrack("A Stage", Track.STAGING);
		
		t1.addRoadName("Track 1 Road");
		t1.setRoadOption(Track.INCLUDEROADS);
		t2.addTypeName("Track 2 Type");
		t3.addRoadName("Track 3 Road");
		t3.setRoadOption(Track.EXCLUDEROADS);
		t4.addTypeName("Track 4 Type");
		
		CarTypes ct = CarTypes.instance();
		ct.addName("Boxcar");
		ct.addName("boxCar");
		ct.addName("BoxCar");
		ct.addName("Track 2 Type");
		ct.addName("Track 4 Type");
		
		// also test schedules
		
		ScheduleManager sm = ScheduleManager.instance();
		Schedule s1 = sm.newSchedule("Schedule 1 Name");
		s1.setComment("Schedule 1 Comment");
		ScheduleItem s1i1 = s1.addItem("Boxcar");
		s1i1.setRoad("Schedule 1 Item 1 Road");
		s1i1.setLoad("Schedule 1 Item 1 Load");
		s1i1.setShip("Schedule 1 Item 1 Ship");
		s1i1.setCount(321);
		s1i1.setComment("Schedule 1 Item 1 Comment");
		
		ScheduleItem s1i2 = s1.addItem("boxcar");
		s1i2.setRoad("Schedule 1 Item 2 Road");
		s1i2.setLoad("Schedule 1 Item 2 Load");
		s1i2.setShip("Schedule 1 Item 2 Ship");
		s1i2.setCount(222);
		s1i2.setComment("Schedule 1 Item 2 Comment");

		Schedule s2 = sm.newSchedule("Schedule 2 Name");
		s2.setComment("Schedule 2 Comment");
		ScheduleItem s2i1 = s2.addItem("BoxCar");
		s2i1.setRoad("Schedule 2 Item 1 Road");
		s2i1.setLoad("Schedule 2 Item 1 Load");
		s2i1.setShip("Schedule 2 Item 1 Ship");
		s2i1.setCount(123);
		s2i1.setComment("Schedule 2 Item 1 Comment");

		locationList = manager.getLocationsByIdList();
		Assert.assertEquals("New Location by Id 1", "Test Location 2", manager.getLocationById(locationList.get(0)).getName());
		Assert.assertEquals("New Location by Id 2", "Test Location 1", manager.getLocationById(locationList.get(1)).getName());
		Assert.assertEquals("New Location by Id 3", "Test Location 3", manager.getLocationById(locationList.get(2)).getName());

		Assert.assertEquals("New Location by Name 1", "Test Location 1", manager.getLocationByName("Test Location 1").getName());
		Assert.assertEquals("New Location by Name 2", "Test Location 2", manager.getLocationByName("Test Location 2").getName());
		Assert.assertEquals("New Location by Name 3", "Test Location 3", manager.getLocationByName("Test Location 3").getName());

		manager.getLocationByName("Test Location 1").setComment("Test Location 1 Comment");
		manager.getLocationByName("Test Location 1").setLocationOps(Location.NORMAL);
		manager.getLocationByName("Test Location 1").setSwitchList(true);
		manager.getLocationByName("Test Location 1").setTrainDirections(Location.EAST);
		manager.getLocationByName("Test Location 1").addTypeName("Baggage");
		manager.getLocationByName("Test Location 1").addTypeName("BoxCar");
		manager.getLocationByName("Test Location 1").addTypeName("Caboose");
		manager.getLocationByName("Test Location 1").addTypeName("Coal");
		manager.getLocationByName("Test Location 1").addTypeName("Engine");
		manager.getLocationByName("Test Location 1").addTypeName("Hopper");
		manager.getLocationByName("Test Location 2").setComment("Test Location 2 Comment");
		manager.getLocationByName("Test Location 2").setLocationOps(Location.NORMAL);
		manager.getLocationByName("Test Location 2").setSwitchList(false);
		manager.getLocationByName("Test Location 2").setTrainDirections(Location.WEST);
		manager.getLocationByName("Test Location 2").addTypeName("Baggage");
		manager.getLocationByName("Test Location 2").addTypeName("Boxcar");
		manager.getLocationByName("Test Location 2").addTypeName("Caboose");
		manager.getLocationByName("Test Location 2").addTypeName("Coal");
		manager.getLocationByName("Test Location 2").addTypeName("Engine");
		manager.getLocationByName("Test Location 2").addTypeName("Hopper");
		manager.getLocationByName("Test Location 3").setComment("Test Location 3 Comment");
		manager.getLocationByName("Test Location 3").setLocationOps(Location.STAGING);
		manager.getLocationByName("Test Location 3").setSwitchList(true);
		manager.getLocationByName("Test Location 3").setTrainDirections(Location.EAST+Location.WEST+Location.NORTH);
		manager.getLocationByName("Test Location 3").addTypeName("Baggage");
		manager.getLocationByName("Test Location 3").addTypeName("boxCar");
		manager.getLocationByName("Test Location 3").addTypeName("Caboose");
		manager.getLocationByName("Test Location 3").addTypeName("Coal");
		manager.getLocationByName("Test Location 3").addTypeName("Engine");
		manager.getLocationByName("Test Location 3").addTypeName("Hopper");

		locationList = manager.getLocationsByIdList();
		Assert.assertEquals("New Number of Locations", 3, locationList.size());

		for (int i = 0; i < locationList.size(); i++) {
			String locationId = locationList.get(i);
			Location loc = manager.getLocationById(locationId);
			String locname = loc.getName();
			if (i == 0) {
				Assert.assertEquals("New Location by Id List 1", "Test Location 2", locname);
			}
			if (i == 1) {
				Assert.assertEquals("New Location by Id List 2", "Test Location 1", locname);
			}
			if (i == 2) {
				Assert.assertEquals("New Location by Id List 3", "Test Location 3", locname);
			}
		}

		locationList = manager.getLocationsByNameList();
		Assert.assertEquals("New Number of Locations", 3, locationList.size());

		for (int i = 0; i < locationList.size(); i++) {
			String locationId = locationList.get(i);
			Location loc = manager.getLocationById(locationId);
			String locname = loc.getName();
			if (i == 0) {
				Assert.assertEquals("New Location by Name List 1", "Test Location 1", locname);
			}
			if (i == 1) {
				Assert.assertEquals("New Location by Name List 2", "Test Location 2", locname);
			}
			if (i == 2) {
				Assert.assertEquals("New Location by Name List 3", "Test Location 3", locname);
			}
		}

		LocationManagerXml.instance().writeOperationsLocationFile();

		manager.newLocation("Test Location 4");
		manager.newLocation("Test Location 5");
		manager.newLocation("Test Location 6");
		manager.getLocationByName("Test Location 2").setComment("Test Location 2 Changed Comment");

		LocationManagerXml.instance().writeOperationsLocationFile();
		
	}

	// test location Xml read support preparation
	public void testXMLReadPrep() {
		LocationManager manager = LocationManager.instance();
		List<String> locationList = manager.getLocationsByIdList();
		Assert.assertEquals("Starting Number of Locations", 6, locationList.size());

		//  Revert the main xml file back to the backup file.
		LocationManagerXml.instance().revertBackupFile(XmlFile.prefsDir()+File.separator+OperationsXml.getOperationsDirectoryName()+File.separator+LocationManagerXml.getOperationsFileName());
		
		//  Need to dispose of the LocationManager's list and hash table
		manager.dispose();	
		// delete all schedules
		ScheduleManager.instance().dispose();	
	}

	// test location Xml read support
	public void testXMLRead() throws Exception  {
		LocationManager manager = LocationManager.instance();
		List<String> locationList = manager.getLocationsByNameList();
		
		CarTypes ct = CarTypes.instance();
		ct.addName("Boxcar");
		ct.addName("boxCar");
		ct.addName("BoxCar");
		ct.addName("Track 2 Type");
		ct.addName("Track 4 Type");

		// The dispose has removed all locations from the Manager.
		Assert.assertEquals("Starting Number of Locations", 0, locationList.size());

		// Need to force a re-read of the xml file.
		LocationManagerXml.instance().readFile(XmlFile.prefsDir()+File.separator+OperationsXml.getOperationsDirectoryName()+File.separator+LocationManagerXml.getOperationsFileName());

		// check options
		Dimension frameDim = manager.getLocationEditFrameSize();
		Point frameLoc = manager.getLocationEditFramePosition();
		Assert.assertEquals("LocationEditFrame size X", 10.0, frameDim.getWidth());
		Assert.assertEquals("LocationEditFrame size Y", 12.0, frameDim.getHeight());
		Assert.assertEquals("LocationEditFrame Postion X", 14.0, frameLoc.getX());
		Assert.assertEquals("LocationEditFrame Postion Y", 16.0, frameLoc.getY());
		
		// check locations
		locationList = manager.getLocationsByNameList();
		Assert.assertEquals("Starting Number of Locations", 3, locationList.size());

		for (int i = 0; i < locationList.size(); i++) {
			String locationId = locationList.get(i);
			Location loc = manager.getLocationById(locationId);

			if (i == 0) {
				Assert.assertEquals("New Location by Name List 1", "Test Location 1", loc.getName());
				Assert.assertEquals("Location 1 operations", Location.NORMAL, loc.getLocationOps());
				Assert.assertEquals("Location 1 direction", Location.EAST, loc.getTrainDirections());
				Assert.assertEquals("Location 1 comment", "Test Location 1 Comment", loc.getComment());
				Assert.assertEquals("Location 1 switchList", true, loc.getSwitchList());
				Assert.assertEquals("Location 1 car type", true, loc.acceptsTypeName("BoxCar"));
				Assert.assertEquals("Location 1 car type", false, loc.acceptsTypeName("boxCar"));
				Assert.assertEquals("Location 1 car type", true, loc.acceptsTypeName("Boxcar"));
				List<String> list = loc.getTracksByNameList(null);
				Assert.assertEquals("Location 1 has n tracks", 1, list.size());
				Track t = loc.getTrackById(list.get(0));
				Assert.assertEquals("Location 1 first track name", "An Interchange", t.getName());
				Assert.assertEquals("Location 1 track road option", Track.EXCLUDEROADS, t.getRoadOption());
				Assert.assertEquals("Location 1 track road", true, t.acceptsRoadName("Track 1 Road"));
				Assert.assertEquals("Location 1 track road", false, t.acceptsRoadName("Track 3 Road"));
			}
			if (i == 1) {
				Assert.assertEquals("New Location by Name List 2", "Test Location 2", loc.getName());
				Assert.assertEquals("Location 2 operations", Location.NORMAL, loc.getLocationOps());
				Assert.assertEquals("Location 2 direction", Location.WEST, loc.getTrainDirections());
				Assert.assertEquals("Location 2 comment", "Test Location 2 Comment", loc.getComment());
				Assert.assertEquals("Location 2 switchList", false, loc.getSwitchList());
				Assert.assertEquals("Location 2 car type", true, loc.acceptsTypeName("Boxcar"));
				Assert.assertEquals("Location 2 car type", false, loc.acceptsTypeName("boxCar"));
				Assert.assertEquals("Location 2 car type", false, loc.acceptsTypeName("BoxCar"));
		
				List<String> list = loc.getTracksByNameList(null);
				Assert.assertEquals("Location 2 has n tracks", 2, list.size());
				Track t = loc.getTrackById(list.get(0));
				Assert.assertEquals("Location 2 first track name", "A Siding", t.getName());
				Assert.assertEquals("Location 2 track 1 road option", Track.ALLROADS, t.getRoadOption());
				Assert.assertEquals("Location 2 track 1 road", true, t.acceptsRoadName("Track 1 Road"));
				Assert.assertEquals("Location 2 track 1 road", true, t.acceptsRoadName("Track 3 Road"));
				Assert.assertEquals("Location 2 track 1 type", true, t.acceptsTypeName("Track 2 Type"));
				Assert.assertEquals("Location 2 track 1 type", false, t.acceptsTypeName("Track 4 Type"));
				t = loc.getTrackById(list.get(1));
				Assert.assertEquals("Location 2 2nd track name", "A Yard", t.getName());
				Assert.assertEquals("Location 2 track 2 road option", Track.INCLUDEROADS, t.getRoadOption());
				Assert.assertEquals("Location 2 track 2 road", true, t.acceptsRoadName("Track 1 Road"));
				Assert.assertEquals("Location 2 track 2 road", false, t.acceptsRoadName("Track 3 Road"));
				Assert.assertEquals("Location 2 track 2 type", false, t.acceptsTypeName("Track 2 Type"));
				Assert.assertEquals("Location 2 track 2 type", false, t.acceptsTypeName("Track 4 Type"));

			}
			if (i == 2) {
				Assert.assertEquals("New Location by Name List 3", "Test Location 3", loc.getName());
				Assert.assertEquals("Location 3 operations", Location.STAGING, loc.getLocationOps());
				Assert.assertEquals("Location 3 direction", Location.EAST+Location.WEST+Location.NORTH, loc.getTrainDirections());
				Assert.assertEquals("Location 3 comment", "Test Location 3 Comment", loc.getComment());
				Assert.assertEquals("Location 3 switchList", true, loc.getSwitchList());
				Assert.assertEquals("Location 3 car type", true, loc.acceptsTypeName("boxCar"));
				Assert.assertEquals("Location 3 car type", false, loc.acceptsTypeName("BoxCar"));
				Assert.assertEquals("Location 3 car type", true, loc.acceptsTypeName("Boxcar"));
		
				List<String> list = loc.getTracksByNameList(null);
				Assert.assertEquals("Location 3 has n tracks", 1, list.size());
				Track t = loc.getTrackById(list.get(0));
				Assert.assertEquals("Location 3 first track name", "A Stage", t.getName());
				Assert.assertEquals("Location 3 track 1 road option", Track.ALLROADS, t.getRoadOption());
				Assert.assertEquals("Location 3 track 1 road", true, t.acceptsRoadName("Track 1 Road"));
				Assert.assertEquals("Location 3 track 1 road", true, t.acceptsRoadName("Track 3 Road"));
				Assert.assertEquals("Location 3 track type", false, t.acceptsTypeName("Track 2 Type"));
				Assert.assertEquals("Location 3 track type", true, t.acceptsTypeName("Track 4 Type"));
			}
		}
		
		// check Schedules
		
		ScheduleManager sm = ScheduleManager.instance();
		List <String>list = sm.getSchedulesByNameList();
		
		Assert.assertEquals("There should be 2 schedules", 2, list.size());
		Schedule s1 = sm.getScheduleById(list.get(0));
		Schedule s2 = sm.getScheduleById(list.get(1));
		
		Assert.assertEquals("Schedule 1 name", "Schedule 1 Name", s1.getName());
		Assert.assertEquals("Schedule 2 name", "Schedule 2 Name", s2.getName());
		Assert.assertEquals("Schedule 1 comment", "Schedule 1 Comment", s1.getComment());
		Assert.assertEquals("Schedule 2 comment", "Schedule 2 Comment", s2.getComment());
		
		List <String> s1items = s1.getItemsBySequenceList(); 
		Assert.assertEquals("There should be 2 items", 2, s1items.size());
		ScheduleItem si1 = s1.getItemById(s1items.get(0));
		Assert.assertEquals("Item 1 type", "Boxcar", si1.getType());
		Assert.assertEquals("Item 1 load", "Schedule 1 Item 1 Load", si1.getLoad());
		Assert.assertEquals("Item 1 ship", "Schedule 1 Item 1 Ship", si1.getShip());
		Assert.assertEquals("Item 1 type", "Schedule 1 Item 1 Comment", si1.getComment());
		Assert.assertEquals("Item 1 road", "Schedule 1 Item 1 Road", si1.getRoad());
		Assert.assertEquals("Item 1 count", 321, si1.getCount());
		
		ScheduleItem si2 = s1.getItemById(s1items.get(1));
		Assert.assertEquals("Item 2 type", "boxcar", si2.getType());
		Assert.assertEquals("Item 2 load", "Schedule 1 Item 2 Load", si2.getLoad());
		Assert.assertEquals("Item 2 ship", "Schedule 1 Item 2 Ship", si2.getShip());
		Assert.assertEquals("Item 2 type", "Schedule 1 Item 2 Comment", si2.getComment());
		Assert.assertEquals("Item 2 road", "Schedule 1 Item 2 Road", si2.getRoad());
		Assert.assertEquals("Item 2 count", 222, si2.getCount());
	
		List <String> s2items = s2.getItemsBySequenceList(); 
		Assert.assertEquals("There should be 1 items", 1, s2items.size());
		ScheduleItem si3 = s2.getItemById(s2items.get(0));
		Assert.assertEquals("Item 3 type", "BoxCar", si3.getType());
		Assert.assertEquals("Item 3 load", "Schedule 2 Item 1 Load", si3.getLoad());
		Assert.assertEquals("Item 3 ship", "Schedule 2 Item 1 Ship", si3.getShip());
		Assert.assertEquals("Item 3 type", "Schedule 2 Item 1 Comment", si3.getComment());
		Assert.assertEquals("Item 3 type", "Schedule 2 Item 1 Road", si3.getRoad());
		Assert.assertEquals("Item 3 count", 123, si3.getCount());
		
		// delete all locations
		manager.dispose();
		// delete all schedules
		sm.dispose();
		// clear out the file
		LocationManagerXml.instance().writeOperationsLocationFile();
	}

	// TODO: Add tests for adding + deleting the same cars

	// TODO: Add tests for track locations

	// TODO: Add test to create xml file

	// TODO: Add test to read xml file
	// from here down is testing infrastructure

	// Ensure minimal setup for log4J

	/**
	 * Test-by test initialization.
	 * Does log4j for standalone use, and then
	 * creates a set of turnouts, sensors and signals
	 * as common background for testing
	 */
	@Override
	protected void setUp() {
		apps.tests.Log4JFixture.setUp();

		// Repoint OperationsXml to JUnitTest subdirectory
		OperationsXml.setOperationsDirectoryName("operations"+File.separator+"JUnitTest");
		// Change file names to ...Test.xml
		OperationsXml.setOperationsFileName("OperationsJUnitTest.xml"); 
		RouteManagerXml.setOperationsFileName("OperationsJUnitTestRouteRoster.xml");
		EngineManagerXml.setOperationsFileName("OperationsJUnitTestEngineRoster.xml");
		CarManagerXml.setOperationsFileName("OperationsJUnitTestCarRoster.xml");
		LocationManagerXml.setOperationsFileName("OperationsJUnitTestLocationRoster.xml");
		TrainManagerXml.setOperationsFileName("OperationsJUnitTestTrainRoster.xml");
	}

	public OperationsLocationsTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", OperationsLocationsTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(OperationsLocationsTest.class);
		suite.addTest(OperationsLocationsGuiTest.suite());
		return suite;
	}

	// The minimal setup for log4J
	@Override
	protected void tearDown() { 
		apps.tests.Log4JFixture.tearDown();
	}
}
