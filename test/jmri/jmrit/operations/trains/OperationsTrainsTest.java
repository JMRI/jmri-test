// OperationsTrainsTest.java

package jmri.jmrit.operations.trains;

import java.io.File;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.rollingstock.engines.Consist;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineTypes;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;

import java.util.List;
import jmri.jmrit.XmlFile;
import jmri.jmrit.operations.locations.LocationManagerXml;
import jmri.jmrit.operations.rollingstock.cars.CarManagerXml;
import jmri.jmrit.operations.rollingstock.engines.EngineManagerXml;
import jmri.jmrit.operations.routes.RouteManager;
import jmri.jmrit.operations.routes.RouteManagerXml;
import jmri.jmrit.operations.setup.OperationsXml;

/**
 * Tests for the Operations Trains class
 * Last manually cross-checked on 20090131
 * 
 * Still to do:
 *  Train: DepartureTime, ArrivalTime
 *  Train: numberCarsWorked
 *  Train: isTraininRoute
 *  Train: getBuild, setBuild, buildIfSelected
 *  Train: printBuildReport, printManifest, printReport
 *  Train: getPrint, setPrint, printIfSelected
 *  Train: setTrainIconCoordinates
 *  Train: terminateIfSelected
 *  Train: load/move/get/create Train Icon
 *  Train: get/set Lead Engine
 *  Train: setIconColor
 *  Train: reset
 *  Train: xml read/write
 *  Train: Most build scenarios.
 * 
 *  TrainBuilder: Everything.
 *  TrainSwitchLists: Everything.
 *  
 * @author	Bob Coleman Copyright (C) 2008, 2009
 * @version $Revision: 1.12 $
 */
public class OperationsTrainsTest extends TestCase {

    synchronized void releaseThread() {
		try {
		    Thread.sleep(20);
			// super.wait(100);
		}
		catch (InterruptedException e) {
		    Assert.fail("failed due to InterruptedException");
		}
	}
	

	// test Train creation
	public void testCreate() {
		Train train1 = new Train("TESTTRAINID", "TESTTRAINNAME");

		Assert.assertEquals("Train Id", "TESTTRAINID", train1.getId());
		Assert.assertEquals("Train Name", "TESTTRAINNAME", train1.getName());
	}

	// test Train public constants
	public void testConstants() {
		Train train1 = new Train("TESTTRAINID", "TESTTRAINNAME");

		Assert.assertEquals("Train Id", "TESTTRAINID", train1.getId());
		Assert.assertEquals("Train Name", "TESTTRAINNAME", train1.getName());

		Assert.assertEquals("Train Constant NONE", 0, Train.NONE);
		Assert.assertEquals("Train Constant CABOOSE", 1, Train.CABOOSE);
		Assert.assertEquals("Train Constant FRED", 2, Train.FRED);

		Assert.assertEquals("Train Constant ALLROADS", "All", Train.ALLROADS);
		Assert.assertEquals("Train Constant INCLUDEROADS", "Include", Train.INCLUDEROADS);
		Assert.assertEquals("Train Constant EXCLUDEROADS", "Exclude", Train.EXCLUDEROADS);
                
		Assert.assertEquals("Train Constant DISPOSE_CHANGED_PROPERTY", "dispose", Train.DISPOSE_CHANGED_PROPERTY);
		Assert.assertEquals("Train Constant STOPS_CHANGED_PROPERTY", "stops", Train.STOPS_CHANGED_PROPERTY);
		Assert.assertEquals("Train Constant TYPES_CHANGED_PROPERTY", "Types", Train.TYPES_CHANGED_PROPERTY);
		Assert.assertEquals("Train Constant ROADS_CHANGED_PROPERTY", "Road", Train.ROADS_CHANGED_PROPERTY);
		Assert.assertEquals("Train Constant LENGTH_CHANGED_PROPERTY", "length", Train.LENGTH_CHANGED_PROPERTY);
		Assert.assertEquals("Train Constant ENGINELOCATION_CHANGED_PROPERTY", "EngineLocation", Train.ENGINELOCATION_CHANGED_PROPERTY);
		Assert.assertEquals("Train Constant NUMBERCARS_CHANGED_PROPERTY", "numberCarsMoves", Train.NUMBERCARS_CHANGED_PROPERTY);
		Assert.assertEquals("Train Constant STATUS_CHANGED_PROPERTY", "status", Train.STATUS_CHANGED_PROPERTY);
		Assert.assertEquals("Train Constant DEPARTURETIME_CHANGED_PROPERTY", "departureTime", Train.DEPARTURETIME_CHANGED_PROPERTY);

//  Comment out test that relies upon a typo in the properties until that gets fixed
//		Assert.assertEquals("Train Constant AUTO", "Auto", t1.AUTO);
	}

	// test TrainIcon attributes
	public void testTrainIconAttributes() {
		Train train1 = new Train("TESTTRAINID", "TESTTRAINNAME");

		Assert.assertEquals("Train Id", "TESTTRAINID", train1.getId());
		Assert.assertEquals("Train Name", "TESTTRAINNAME", train1.getName());
		Assert.assertEquals("Train toString", "TESTTRAINNAME", train1.toString());

                TrainIcon trainicon1 = new TrainIcon();
                trainicon1.setTrain(train1);
		Assert.assertEquals("TrainIcon set train", "TESTTRAINNAME", trainicon1.getTrain().getName());
 	}

	// test Train attributes
	public void testAttributes() {
		Train train1 = new Train("TESTTRAINID", "TESTTRAINNAME");

		Assert.assertEquals("Train Id", "TESTTRAINID", train1.getId());
		Assert.assertEquals("Train Name", "TESTTRAINNAME", train1.getName());
		Assert.assertEquals("Train toString", "TESTTRAINNAME", train1.toString());

		train1.setName("TESTNEWNAME");
		Assert.assertEquals("Train New Name", "TESTNEWNAME", train1.getName());
		train1.setComment("TESTCOMMENT");
		Assert.assertEquals("Train Comment", "TESTCOMMENT", train1.getComment());
		train1.setDescription("TESTDESCRIPTION");
		Assert.assertEquals("Train Description", "TESTDESCRIPTION", train1.getDescription());
		train1.setCabooseRoad("TESTCABOOSEROAD");
		Assert.assertEquals("Train Caboose Road", "TESTCABOOSEROAD", train1.getCabooseRoad());
		train1.setEngineModel("TESTENGINEMODEL");
		Assert.assertEquals("Train Engine Model", "TESTENGINEMODEL", train1.getEngineModel());
		train1.setEngineRoad("TESTENGINEROAD");
		Assert.assertEquals("Train Engine Road", "TESTENGINEROAD", train1.getEngineRoad());
		train1.setBuilt(true);
		Assert.assertTrue("Train Built true", train1.getBuilt());
		train1.setBuilt(false);
		Assert.assertFalse("Train Built false", train1.getBuilt());
		train1.setNumberEngines("13");
		Assert.assertEquals("Train Number Engines", "13", train1.getNumberEngines());
		train1.setRoadOption("INCLUDEROADS");
		Assert.assertEquals("Train Road Option INCLUDEROADS", "INCLUDEROADS", train1.getRoadOption());
		train1.setRoadOption("EXCLUDEROADS");
		Assert.assertEquals("Train Road Option EXCLUDEROADS", "EXCLUDEROADS", train1.getRoadOption());
		train1.setRoadOption("ALLROADS");
		Assert.assertEquals("Train Road Option ALLROADS", "ALLROADS", train1.getRoadOption());
		train1.setStatus("TESTSTATUS");
		Assert.assertEquals("Train Status", "TESTSTATUS", train1.getStatus());
		train1.setRequirements(Train.CABOOSE);
		Assert.assertEquals("Train Requirements CABOOSE", 1, train1.getRequirements());
		train1.setRequirements(Train.FRED);
		Assert.assertEquals("Train Requirements FRED", 2, train1.getRequirements());
		train1.setRequirements(Train.NONE);
		Assert.assertEquals("Train Requirements NONE", 0, train1.getRequirements());
	}

	// test Train route
	public void testRoute() {
		Train train1 = new Train("TESTTRAINID", "TESTTRAINNAME");

		Assert.assertEquals("Train Id", "TESTTRAINID", train1.getId());
		Assert.assertEquals("Train Name", "TESTTRAINNAME", train1.getName());

		Route r1 = new Route("TESTROUTEID", "TESTROUTENAME");

		train1.setRoute(r1);
		Assert.assertEquals("Train Route Name", "TESTROUTENAME", train1.getTrainRouteName());

		Route rnew = new Route("TESTROUTEID2", "TESTNEWROUTENAME");
		RouteLocation rladd;
		Location l1 = new Location("TESTLOCATIONID1", "TESTNEWROUTEDEPTNAME");
		rladd = rnew.addLocation(l1);
		Location l2 = new Location("TESTLOCATIONID2", "TESTLOCATIONNAME2");
		rladd = rnew.addLocation(l2);
		Location l3 = new Location("TESTLOCATIONID3", "TESTNEWROUTECURRNAME");
		rladd = rnew.addLocation(l3);
		Location l4 = new Location("TESTLOCATIONID4", "TESTLOCATIONNAME4");
		rladd = rnew.addLocation(l4);
		Location l5 = new Location("TESTLOCATIONID5", "TESTNEWROUTETERMNAME");
		rladd = rnew.addLocation(l5);

		train1.setRoute(rnew);
		Assert.assertEquals("Train New Route Name", "TESTNEWROUTENAME", train1.getTrainRouteName());

		Assert.assertEquals("Train New Route Departure Name", "TESTNEWROUTEDEPTNAME", train1.getTrainDepartsName());
		Assert.assertEquals("Train New Route Terminates Name", "TESTNEWROUTETERMNAME", train1.getTrainTerminatesName());

		RouteLocation rl1test;
		rl1test= rnew.getLocationByName("TESTNEWROUTECURRNAME");
		train1.setCurrentLocation(rl1test);
		Assert.assertEquals("Train New Route Current Name", "TESTNEWROUTECURRNAME", train1.getCurrentLocationName());
		rl1test= train1.getCurrentLocation();
		Assert.assertEquals("Train New Route Current Name by Route Location", "TESTNEWROUTECURRNAME", rl1test.getName());
	}

	// test Train skip locations support
	public void testSkipLocations() {
		Train train1 = new Train("TESTTRAINID", "TESTTRAINNAME");

		Assert.assertEquals("Train Id", "TESTTRAINID", train1.getId());
		Assert.assertEquals("Train Name", "TESTTRAINNAME", train1.getName());

                train1.addTrainSkipsLocation("TESTLOCATIONID2");
		Assert.assertTrue("Location 2 to be skipped", train1.skipsLocation("TESTLOCATIONID2"));

		train1.addTrainSkipsLocation("TESTLOCATIONID4");
		Assert.assertTrue("Location 4 to be skipped", train1.skipsLocation("TESTLOCATIONID4"));

		train1.deleteTrainSkipsLocation("TESTLOCATIONID2");
		Assert.assertFalse("Location 2 not to be skipped", train1.skipsLocation("TESTLOCATIONID2"));
		Assert.assertTrue("Location 4 still to be skipped", train1.skipsLocation("TESTLOCATIONID4"));

		train1.deleteTrainSkipsLocation("TESTLOCATIONID4");
		Assert.assertFalse("Location 2 still not to be skipped", train1.skipsLocation("TESTLOCATIONID2"));
		Assert.assertFalse("Location 4 not to be skipped", train1.skipsLocation("TESTLOCATIONID4"));
	}

	// test Train accepts types support
	public void testAcceptsTypes() {
		Train train1 = new Train("TESTTRAINID", "TESTTRAINNAME");

		Assert.assertEquals("Train Id", "TESTTRAINID", train1.getId());
		Assert.assertEquals("Train Name", "TESTTRAINNAME", train1.getName());

		train1.addTypeName("Caboose");
		Assert.assertTrue("Train accepts type name Caboose", train1.acceptsTypeName("Caboose"));
		Assert.assertFalse("Train does not accept type name Hopper", train1.acceptsTypeName("Hopper"));

		train1.addTypeName("Hopper");
		Assert.assertTrue("Train still accepts type name Caboose", train1.acceptsTypeName("Caboose"));
		Assert.assertTrue("Train accepts type name Hopper", train1.acceptsTypeName("Hopper"));

		train1.deleteTypeName("Caboose");
		Assert.assertFalse("Train no longer accepts type name Caboose", train1.acceptsTypeName("Caboose"));
		Assert.assertTrue("Train still accepts type name Hopper", train1.acceptsTypeName("Hopper"));
	}

	// test train accepts road names support
	public void testAcceptsRoadNames() {
		Train train1 = new Train("TESTTRAINID", "TESTTRAINNAME");

		Assert.assertEquals("Train Id", "TESTTRAINID", train1.getId());
		Assert.assertEquals("Train Name", "TESTTRAINNAME", train1.getName());

		train1.setRoadOption("ALLROADS");
		Assert.assertTrue("Train accepts (ALLROADS) Road name CP", train1.acceptsRoadName("CP"));
		Assert.assertTrue("Train accepts (ALLROADS) Road name VIA", train1.acceptsRoadName("VIA"));

		train1.setRoadOption("Include");
		train1.addRoadName("CP");
		Assert.assertTrue("Train accepts (INCLUDEROADS) Road name CP", train1.acceptsRoadName("CP"));
		Assert.assertFalse("Train does not accept (INCLUDEROADS) Road name VIA", train1.acceptsRoadName("VIA"));

		train1.addRoadName("VIA");
		Assert.assertTrue("Train still accepts (INCLUDEROADS) Road name CP", train1.acceptsRoadName("CP"));
		Assert.assertTrue("Train accepts (INCLUDEROADS) Road name VIA", train1.acceptsRoadName("VIA"));

		train1.deleteRoadName("CP");
		Assert.assertFalse("Train no longer accepts (INCLUDEROADS) Road name CP", train1.acceptsRoadName("CP"));
		Assert.assertTrue("Train still accepts (INCLUDEROADS) Road name VIA", train1.acceptsRoadName("VIA"));

		train1.setRoadOption("Exclude");
		Assert.assertTrue("Train does accept (EXCLUDEROADS) Road name CP", train1.acceptsRoadName("CP"));
		Assert.assertFalse("Train does not accept (EXCLUDEROADS) Road name VIA", train1.acceptsRoadName("VIA"));

		train1.addRoadName("CP");
		Assert.assertFalse("Train does not accept (EXCLUDEROADS) Road name CP", train1.acceptsRoadName("CP"));
		Assert.assertFalse("Train still does not accept (EXCLUDEROADS) Road name VIA", train1.acceptsRoadName("VIA"));

		train1.deleteRoadName("VIA");
		Assert.assertFalse("Train still does not accepts (EXCLUDEROADS) Road name CP", train1.acceptsRoadName("CP"));
		Assert.assertTrue("Train now accepts (EXCLUDEROADS) Road name VIA", train1.acceptsRoadName("VIA"));
	}

	// test train staging to staging
	public void testStagingtoStaging() {
                TrainManager tmanager = TrainManager.instance();
                RouteManager rmanager = RouteManager.instance();
                LocationManager lmanager = LocationManager.instance();
                EngineManager emanager = EngineManager.instance();
                CarManager cmanager = CarManager.instance();
        		CarTypes ct = CarTypes.instance();
        		EngineTypes et = EngineTypes.instance();
        		
        		// register the car and engine types used
        		ct.addName("Boxcar");
        		ct.addName("Caboose");
        		et.addName("Diesel");

                // Set up four engines in two consists 
                Consist con1 = new Consist("C16");
                Consist con2 = new Consist("C14");
                
                Engine e1 = new Engine("CP", "5016");
                e1.setModel("GP40");
                e1.setConsist(con1);
		Assert.assertEquals("Engine 1 Length", "59", e1.getLength());
                emanager.register(e1);
                
                Engine e2 = new Engine("CP", "5019");
                e2.setModel("GP40");
                e2.setConsist(con1);
		Assert.assertEquals("Engine 2 Length", "59", e2.getLength());
                emanager.register(e2);
                
                Engine e3 = new Engine("CP", "5524");
                e3.setModel("SD45");
                e3.setConsist(con2);
		Assert.assertEquals("Engine 3 Length", "66", e3.getLength());
                emanager.register(e3);

                Engine e4 = new Engine("CP", "5559");
                e4.setModel("SD45");
                e4.setConsist(con2);
		Assert.assertEquals("Engine 4 Length", "66", e4.getLength());
                emanager.register(e4);
                
                // Set up two cabooses and four box cars
                Car c1 = new Car("CP", "C10099");
                c1.setType("Caboose");
                c1.setLength("32");
                c1.setCaboose(true);
		Assert.assertEquals("Caboose 1 Length", "32", c1.getLength());
                cmanager.register(c1);
                
                Car c2 = new Car("CP", "C20099");
                c2.setType("Caboose");
                c2.setLength("32");
                c2.setCaboose(true);
		Assert.assertEquals("Caboose 2 Length", "32", c2.getLength());
                cmanager.register(c2);
                
                Car c3 = new Car("CP", "X10001");
                c3.setType("Boxcar");
                c3.setLength("40");
		Assert.assertEquals("Box Car X10001 Length", "40", c3.getLength());
                cmanager.register(c3);
                
                Car c4 = new Car("CP", "X10002");
                c4.setType("Boxcar");
                c4.setLength("40");
		Assert.assertEquals("Box Car X10002 Length", "40", c4.getLength());
                cmanager.register(c4);
                
                Car c5 = new Car("CP", "X20001");
                c5.setType("Boxcar");
                c5.setLength("40");
		Assert.assertEquals("Box Car X20001 Length", "40", c5.getLength());
                cmanager.register(c5);
                
                Car c6 = new Car("CP", "X20002");
                c6.setType("Boxcar");
                c6.setLength("40");
		Assert.assertEquals("Box Car X20002 Length", "40", c6.getLength());
                cmanager.register(c6);

                
                // Set up a route of 3 locations: North End Staging (2 tracks), 
                // North Industries, and South End Staging (2 tracks).
                Location l1 = new Location("1", "North End");
		Assert.assertEquals("Location 1 Id", "1", l1.getId());
		Assert.assertEquals("Location 1 Name", "North End", l1.getName());
		Assert.assertEquals("Location 1 Initial Length", 0, l1.getLength());
                l1.setLocationOps(Location.STAGING);
                l1.setTrainDirections(15);
                l1.setSwitchList(true);
                l1.addTypeName("Diesel");
                l1.addTypeName("Boxcar");
                l1.addTypeName("Caboose");
                lmanager.register(l1);
                
                Track l1s1 = new Track("1s1", "North End 1", Track.STAGING);
                l1s1.setLength(300);
		Assert.assertEquals("Location 1s1 Id", "1s1", l1s1.getId());
		Assert.assertEquals("Location 1s1 Name", "North End 1", l1s1.getName());
		Assert.assertEquals("Location 1s1 LocType", "Staging", l1s1.getLocType());
		Assert.assertEquals("Location 1s1 Length", 300, l1s1.getLength());
                l1s1.setTrainDirections(15);
                l1s1.addTypeName("Diesel");
                l1s1.addTypeName("Boxcar");
                l1s1.addTypeName("Caboose");
                l1s1.setRoadOption("All");
                l1s1.setDropOption("Any");
                l1s1.setPickupOption("Any");

                Track l1s2 = new Track("1s2", "North End 2", Track.STAGING);
                l1s2.setLength(400);
		Assert.assertEquals("Location 1s2 Id", "1s2", l1s2.getId());
		Assert.assertEquals("Location 1s2 Name", "North End 2", l1s2.getName());
		Assert.assertEquals("Location 1s2 LocType", "Staging", l1s2.getLocType());
		Assert.assertEquals("Location 1s2 Length", 400, l1s2.getLength());
                l1s2.setTrainDirections(15);
                l1s2.addTypeName("Diesel");
                l1s2.addTypeName("Boxcar");
                l1s2.addTypeName("Caboose");
                l1s2.setRoadOption("All");
                l1s2.setDropOption("Any");
                l1s2.setPickupOption("Any");

                l1.addTrack("North End 1", Track.STAGING);
                l1.addTrack("North End 2", Track.STAGING);
                List templist1 = l1.getTracksByNameList("");
        	for (int i = 0; i < templist1.size(); i++){
                    if (i == 0) {
        		Assert.assertEquals("RL 1 Staging 1 Name", "North End 1", templist1.get(i));
                    }
                    if (i == 1) {
        		Assert.assertEquals("RL 1 Staging 2 Name", "North End 2", templist1.get(i));
                    }
          	}
                
                l1.register(l1s1);
                l1.register(l1s2);
                
		Assert.assertEquals("Location 1 Length", 700, l1.getLength());
                
                Location l2 = new Location("2", "North Industries");
		Assert.assertEquals("Location 2 Id", "2", l2.getId());
		Assert.assertEquals("Location 2 Name", "North Industries", l2.getName());
                l2.setLocationOps(Location.NORMAL);
                l2.setTrainDirections(15);
                l2.setSwitchList(true);
                l2.addTypeName("Diesel");
                l2.addTypeName("Boxcar");
                l2.addTypeName("Caboose");
                l2.setLength(200);
                lmanager.register(l2);
		Assert.assertEquals("Location 2 Length", 200, l2.getLength());

                Location l3 = new Location("3", "South End");
		Assert.assertEquals("Location 3 Id", "3", l3.getId());
		Assert.assertEquals("Location 3 Name", "South End", l3.getName());
		Assert.assertEquals("Location 3 Initial Length", 0, l3.getLength());
                l3.setLocationOps(Location.STAGING);
                l3.setTrainDirections(15);
                l3.setSwitchList(true);
                l3.addTypeName("Diesel");
                l3.addTypeName("Boxcar");
                l3.addTypeName("Caboose");
                lmanager.register(l3);

                Track l3s1 = new Track("3s1", "South End 1", Track.STAGING);
                l3s1.setLength(300);
		Assert.assertEquals("Location 3s1 Id", "3s1", l3s1.getId());
		Assert.assertEquals("Location 3s1 Name", "South End 1", l3s1.getName());
		Assert.assertEquals("Location 3s1 LocType", "Staging", l3s1.getLocType());
		Assert.assertEquals("Location 3s1 Length", 300, l3s1.getLength());
                l3s1.setTrainDirections(15);
                l3s1.addTypeName("Diesel");
                l3s1.addTypeName("Boxcar");
                l3s1.addTypeName("Caboose");
                l3s1.setRoadOption("All");
                l3s1.setDropOption("Any");
                l3s1.setPickupOption("Any");
                
                Track l3s2 = new Track("3s2", "South End 2", Track.STAGING);
                l3s2.setLength(400);
		Assert.assertEquals("Location 3s2 Id", "3s2", l3s2.getId());
		Assert.assertEquals("Location 3s2 Name", "South End 2", l3s2.getName());
		Assert.assertEquals("Location 3s2 LocType", "Staging", l3s2.getLocType());
		Assert.assertEquals("Location 3s2 Length", 400, l3s2.getLength());
                l3s2.setTrainDirections(15);
                l3s2.addTypeName("Diesel");
                l3s2.addTypeName("Boxcar");
                l3s2.addTypeName("Caboose");
                l3s2.setRoadOption("All");
                l3s2.setDropOption("Any");
                l3s2.setPickupOption("Any");
                
                l3.addTrack("South End 1", Track.STAGING);
                l3.addTrack("South End 2", Track.STAGING);
                List templist3 = l3.getTracksByNameList("");
        	for (int i = 0; i < templist3.size(); i++){
                    if (i == 0) {
        		Assert.assertEquals("RL 3 Staging 1 Name", "South End 1", templist3.get(i));
                    }
                    if (i == 1) {
        		Assert.assertEquals("RL 3 Staging 2 Name", "South End 2", templist3.get(i));
                    }
          	}
                
                l3.register(l3s1);
                l3.register(l3s2);
                
		Assert.assertEquals("Location 3 Length", 700, l3.getLength());

                // Place Engines on Staging tracks
                Assert.assertEquals("Location 1s1 Init Used Length", 0, l1s1.getUsedLength());
                Assert.assertEquals("Location 1 Init Used Length", 0, l1s1.getUsedLength());
                Assert.assertEquals("Place e1", Engine.OKAY, e1.setLocation(l1, l1s1));
		Assert.assertEquals("Location 1s1 e1 Used Length", 63, l1s1.getUsedLength());
		Assert.assertEquals("Location 1 e1 Used Length", 63, l1.getUsedLength());
                Assert.assertEquals("Place e2", Engine.OKAY, e2.setLocation(l1, l1s1));
		Assert.assertEquals("Location 1s1 e2 Used Length", 126, l1s1.getUsedLength());
		Assert.assertEquals("Location 1 e2 Used Length", 126, l1.getUsedLength());

                Assert.assertEquals("Location 1s2 Init Used Length", 0, l1s2.getUsedLength());
                Assert.assertEquals("Place e3", Engine.OKAY, e3.setLocation(l1, l1s2));
		Assert.assertEquals("Location 1s2 e3 Used Length", 70, l1s2.getUsedLength());
		Assert.assertEquals("Location 1 e3 Used Length", 196, l1.getUsedLength());
                Assert.assertEquals("Place e4", Engine.OKAY, e4.setLocation(l1, l1s2));
		Assert.assertEquals("Location 1s2 e4 Used Length", 140, l1s2.getUsedLength());
		Assert.assertEquals("Location 1 e4 Used Length", 266, l1.getUsedLength());

                // Place Boxcars on Staging tracks
                Assert.assertTrue("l1 Accepts Boxcar", l1.acceptsTypeName("Boxcar"));
                Assert.assertTrue("l1s1 Accepts Boxcar", l1s1.acceptsTypeName("Boxcar"));

                Assert.assertEquals("Place c3", Car.OKAY, c3.setLocation(l1, l1s1));
		Assert.assertEquals("Location 1s1 c3 Used Length", 170, l1s1.getUsedLength());
		Assert.assertEquals("Location 1 c3 Used Length", 310, l1.getUsedLength());
                Assert.assertEquals("Place c4", Car.OKAY, c4.setLocation(l1, l1s1));
		Assert.assertEquals("Location 1s1 c4 Used Length", 214, l1s1.getUsedLength());
		Assert.assertEquals("Location 1 c4 Used Length", 354, l1.getUsedLength());

                Assert.assertEquals("Place c5", Car.OKAY, c5.setLocation(l1, l1s2));
		Assert.assertEquals("Location 1s2 c5 Used Length", 184, l1s2.getUsedLength());
		Assert.assertEquals("Location 1 c5 Used Length", 398, l1.getUsedLength());
                Assert.assertEquals("Place c6", Car.OKAY, c6.setLocation(l1, l1s2));
		Assert.assertEquals("Location 1s2 c6 Used Length", 228, l1s2.getUsedLength());
		Assert.assertEquals("Location 1 c6 Used Length", 442, l1.getUsedLength());

                // Place Cabooses on Staging tracks
                Assert.assertEquals("Place c1", Car.OKAY, c1.setLocation(l1, l1s1));
		Assert.assertEquals("Location 1s1 c1 Used Length", 250, l1s1.getUsedLength());
		Assert.assertEquals("Location 1 c1 Used Length", 478, l1.getUsedLength());

                Assert.assertEquals("Place c2", Car.OKAY, c2.setLocation(l1, l1s2));
		Assert.assertEquals("Location 1s2 c2 Used Length", 264, l1s2.getUsedLength());
		Assert.assertEquals("Location 1 c2 Used Length", 514, l1.getUsedLength());

                // Define the route.
                Route r1 = new Route("1", "Southbound Main Route");
		Assert.assertEquals("Route Id", "1", r1.getId());
		Assert.assertEquals("Route Name", "Southbound Main Route", r1.getName());
                
		RouteLocation rl1 = new RouteLocation("1r1", l1);
                rl1.setSequenceId(1);
                rl1.setTrainDirection(RouteLocation.SOUTH);
                rl1.setMaxCarMoves(5);
                rl1.setMaxTrainLength(1000);
                Assert.assertEquals("Route Location 1 Id", "1r1", rl1.getId());
		Assert.assertEquals("Route Location 1 Name", "North End", rl1.getName());
		RouteLocation rl2 = new RouteLocation("1r2", l2);
                rl2.setSequenceId(2);
                rl2.setTrainDirection(RouteLocation.SOUTH);
                rl2.setMaxCarMoves(5);
                rl2.setMaxTrainLength(1000);
		Assert.assertEquals("Route Location 2 Id", "1r2", rl2.getId());
		Assert.assertEquals("Route Location 2 Name", "North Industries", rl2.getName());
		RouteLocation rl3 = new RouteLocation("1r3", l3);
                rl3.setSequenceId(3);
                rl3.setTrainDirection(RouteLocation.SOUTH);
                rl3.setMaxCarMoves(5);
                rl3.setMaxTrainLength(1000);
		Assert.assertEquals("Route Location 3 Id", "1r3", rl3.getId());
		Assert.assertEquals("Route Location 3 Name", "South End", rl3.getName());

                r1.register(rl1);
                r1.register(rl2);
                r1.register(rl3);
                
                rmanager.register(r1);

                // Finally ready to define the train.
                Train train1 = new Train("1", "Southbound Through Freight");
		Assert.assertEquals("Train Id", "1", train1.getId());
		Assert.assertEquals("Train Name", "Southbound Through Freight", train1.getName());
                train1.setEngineRoad("CP");
//                train1.setEngineModel("SD45");
                train1.setNumberEngines("2");
                train1.setRequirements(Train.CABOOSE);
                train1.setCabooseRoad("CP");
                train1.addTypeName("Caboose");
                train1.addTypeName("Boxcar");
                train1.addTypeName("Diesel");
                train1.setRoadOption("All");
                train1.addTrainSkipsLocation("North Industries");
                train1.setRoute(r1);

                tmanager.register(train1);
                
                //  Last minute checks.
                Assert.assertEquals("Train 1 Departs Name", "North End", train1.getTrainDepartsName());
                Assert.assertEquals("Train 1 Route Departs Name", "North End", train1.getTrainDepartsRouteLocation().getName());
                Assert.assertEquals("Train 1 Terminates Name", "South End", train1.getTrainTerminatesName());
                Assert.assertEquals("Train 1 Route Terminates Name", "South End", train1.getTrainTerminatesRouteLocation().getName());
                Assert.assertEquals("Train 1 Next Location Name", "", train1.getNextLocationName());
                Assert.assertEquals("Train 1 Route Name", "Southbound Main Route", train1.getRoute().getName());

                //  Build the train!!
                train1.build();
                Assert.assertEquals("Train 1 After Build Departs Name", "North End", train1.getTrainDepartsName());
                Assert.assertEquals("Train 1 After Build Terminates Name", "South End", train1.getTrainTerminatesName());
                Assert.assertEquals("Train 1 After Build Next Location Name", "North Industries", train1.getNextLocationName());
                
                //  Move the train!!
                train1.move();
		Assert.assertEquals("Train 1 After 1st Move Current Name", "North Industries", train1.getCurrentLocationName());
                Assert.assertEquals("Train 1 After 1st Move Next Location Name", "South End", train1.getNextLocationName());

                //  Move the train again!!
                train1.move();
		Assert.assertEquals("Train 1 After 2nd Move Current Name", "South End", train1.getCurrentLocationName());
                Assert.assertEquals("Train 1 After 2nd Move Next Location Name", "South End", train1.getNextLocationName());

                //  Move the train again!!
                train1.move();
		Assert.assertEquals("Train 1 After 3rd Move Current Name", "South End", train1.getCurrentLocationName());
                Assert.assertEquals("Train 1 After 3rd Move Next Location Name", "South End", train1.getNextLocationName());

        }
        
        // TODO: Add test of build

	// TODO: Add test to create xml file

	// TODO: Add test to read xml file

	// from here down is testing infrastructure

    // Ensure minimal setup for log4J
    @Override
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
        // This test doesn't touch setup but we'll protect
        // Repoint OperationsXml to JUnitTest subdirectory
        String tempstring = OperationsXml.getOperationsDirectoryName();
        if (!tempstring.contains(File.separator+"JUnitTest")){
        	OperationsXml.setOperationsDirectoryName(OperationsXml.getOperationsDirectoryName()+File.separator+"JUnitTest");
        	OperationsXml.setOperationsFileName("OperationsJUnitTest.xml"); 
        }
        
        // This test doesn't touch routes but we'll protect
        // Repoint RouteManagerXml to JUnitTest subdirectory
        tempstring = RouteManagerXml.getOperationsDirectoryName();
        if (!tempstring.contains(File.separator+"JUnitTest")){
        	RouteManagerXml.setOperationsDirectoryName(RouteManagerXml.getOperationsDirectoryName()+File.separator+"JUnitTest");
        	RouteManagerXml.setOperationsFileName("OperationsJUnitTestRouteRoster.xml");
        }
        
        // Repoint EngineManagerXml to JUnitTest subdirectory
        tempstring = EngineManagerXml.getOperationsDirectoryName();
        if (!tempstring.contains(File.separator+"JUnitTest")){
        	EngineManagerXml.setOperationsDirectoryName(EngineManagerXml.getOperationsDirectoryName()+File.separator+"JUnitTest");
        	EngineManagerXml.setOperationsFileName("OperationsJUnitTestEngineRoster.xml");
        }
        
        // This test doesn't touch cars but we'll protect
        // Repoint CarManagerXml to JUnitTest subdirectory
        tempstring = CarManagerXml.getOperationsDirectoryName();
        if (!tempstring.contains(File.separator+"JUnitTest")){
        	CarManagerXml.setOperationsDirectoryName(CarManagerXml.getOperationsDirectoryName()+File.separator+"JUnitTest");
        	CarManagerXml.setOperationsFileName("OperationsJUnitTestCarRoster.xml");
        }
        
        // Repoint LocationManagerXml to JUnitTest subdirectory
        tempstring = LocationManagerXml.getOperationsDirectoryName();
        if (!tempstring.contains(File.separator+"JUnitTest")){
        	LocationManagerXml.setOperationsDirectoryName(LocationManagerXml.getOperationsDirectoryName()+File.separator+"JUnitTest");
        	LocationManagerXml.setOperationsFileName("OperationsJUnitTestLocationRoster.xml");
        }
        
        // Repoint TrainManagerXml to JUnitTest subdirectory
        tempstring = TrainManagerXml.getOperationsDirectoryName();
        if (!tempstring.contains(File.separator+"JUnitTest")){
        	TrainManagerXml.setOperationsDirectoryName(TrainManagerXml.getOperationsDirectoryName()+File.separator+"JUnitTest");
        	TrainManagerXml.setOperationsFileName("OperationsJUnitTestTrainRoster.xml");
        }
    	
        XmlFile.ensurePrefsPresent(XmlFile.prefsDir()+File.separator+LocationManagerXml.getOperationsDirectoryName());
    }

	public OperationsTrainsTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", OperationsTrainsTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(OperationsTrainsTest.class);
		return suite;
	}

    // The minimal setup for log4J
    @Override
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
}
