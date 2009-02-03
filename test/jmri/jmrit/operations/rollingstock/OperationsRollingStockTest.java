// OperationsRollingStockTest.java

package jmri.jmrit.operations.rollingstock;

import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SignalHead;
import jmri.Turnout;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.setup.Setup;
import jmri.managers.InternalSensorManager;
import jmri.managers.InternalTurnoutManager;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the Operations RollingStock class
 * Last manually cross-checked on 20090131
 * 
 * Still to do:
 *   RollingStock: Location Length change (set)
 *   RollingStock: Destination
 *   RollingStock: Train, Route
 *   RollingStock: XML read/write
 * 
 * @author	Bob Coleman Copyright (C) 2009
 * 
 */

public class OperationsRollingStockTest extends TestCase {

	// test creation
	public void testCreate() {
		RollingStock rs1 = new RollingStock("TESTROAD", "TESTNUMBER1");
                
                Assert.assertEquals("Car Road", "TESTROAD", rs1.getRoad());
		Assert.assertEquals("Car Number", "TESTNUMBER1", rs1.getNumber());
		Assert.assertEquals("Car ID", "TESTROAD"+"TESTNUMBER1", rs1.getId());
                
		rs1.setType("TESTTYPE");
		rs1.setLength("TESTLENGTH");
		rs1.setColor("TESTCOLOR");
		rs1.setWeight("TESTWEIGHT");
		rs1.setWeightTons("TESTWEIGHTTONS");
		rs1.setBuilt("TESTBUILT");
		rs1.setOwner("TESTOWNER");
		rs1.setComment("TESTCOMMENT");
		rs1.setMoves(5);

		Assert.assertEquals("RollingStock Type", "TESTTYPE", rs1.getType());
                
                /* Also need to test location length */
		Assert.assertEquals("RollingStock Length", "TESTLENGTH", rs1.getLength());
                
		Assert.assertEquals("RollingStock Color", "TESTCOLOR", rs1.getColor());
                /* More appropriate Weight tests below */
		Assert.assertEquals("RollingStock Weight", "TESTWEIGHT", rs1.getWeight());
		Assert.assertEquals("RollingStock WeightTons", "TESTWEIGHTTONS", rs1.getWeightTons());

                Assert.assertEquals("RollingStock Built", "TESTBUILT", rs1.getBuilt());
		Assert.assertEquals("RollingStock Owner", "TESTOWNER", rs1.getOwner());
		Assert.assertEquals("RollingStock Comment", "TESTCOMMENT", rs1.getComment());
		Assert.assertEquals("RollingStock Moves", 5, rs1.getMoves());
	}

	// test RollingStock weight and weighttons
	public void testRollingStockWeight() {
		RollingStock rs1 = new RollingStock("TESTROAD", "TESTNUMBER1");
		Assert.assertEquals("RollingStock Road", "TESTROAD", rs1.getRoad());
		Assert.assertEquals("RollingStock Number", "TESTNUMBER1", rs1.getNumber());

                Setup.setScale(Setup.N_SCALE);
		rs1.setWeight("20");
		Assert.assertEquals("RollingStock Weight Real test", "20", rs1.getWeight());
		Assert.assertEquals("RollingStock WeightTons Real test", "1600", rs1.getWeightTons());
	}

	// test RollingStock public constants
	public void testRollingStockConstants() {
		RollingStock rs1 = new RollingStock("TESTROAD", "TESTNUMBER1");
		Assert.assertEquals("RollingStock Road", "TESTROAD", rs1.getRoad());
		Assert.assertEquals("RollingStock Number", "TESTNUMBER1", rs1.getNumber());

		Assert.assertEquals("RollingStock Constant OKAY", "okay", RollingStock.OKAY);
		Assert.assertEquals("RollingStock Constant LENGTH", "length", RollingStock.LENGTH);
		Assert.assertEquals("RollingStock Constant TYPE", "type", RollingStock.TYPE);
		Assert.assertEquals("RollingStock Constant ROAD", "road", RollingStock.ROAD);
		Assert.assertEquals("RollingStock Constant SCHEDULE", "schedule", RollingStock.SCHEDULE);
		Assert.assertEquals("RollingStock Constant LOAD", "load", RollingStock.LOAD);
                
		Assert.assertEquals("RollingStock Constant LOCATION_CHANGED_PROPERTY", "rolling stock location", RollingStock.LOCATION_CHANGED_PROPERTY);
		Assert.assertEquals("RollingStock Constant TRACK_CHANGED_PROPERTY", "rolling stock track location", RollingStock.TRACK_CHANGED_PROPERTY);
		Assert.assertEquals("RollingStock Constant DESTINATION_CHANGED_PROPERTY", "rolling stock destination", RollingStock.DESTINATION_CHANGED_PROPERTY);
		Assert.assertEquals("RollingStock Constant DESTINATIONTRACK_CHANGED_PROPERTY", "rolling stock track destination", RollingStock.DESTINATIONTRACK_CHANGED_PROPERTY);

		Assert.assertEquals("RollingStock Constant COUPLER", 4, RollingStock.COUPLER);
	}

	// test RollingStock location and track
	public void testRollingStockLocation() {
		RollingStock rs1 = new RollingStock("TESTROAD", "TESTNUMBER1");
                /* Rolling Stock needs a valid type */
                rs1.setType("TESTTYPE");
                /* Type needs to be in CarTypes or EngineTypes */
                CarTypes.instance().addName("TESTTYPE");

                Assert.assertEquals("RollingStock Road", "TESTROAD", rs1.getRoad());
		Assert.assertEquals("RollingStock Number", "TESTNUMBER1", rs1.getNumber());
		Assert.assertEquals("RollingStock Type", "TESTTYPE", rs1.getType());

                /* Rolling Stock not placed on layout yet */
                Assert.assertEquals("RollingStock null Location Name", "", rs1.getLocationName());
                Assert.assertEquals("RollingStock null Location Id", "", rs1.getLocationId());
                Assert.assertEquals("RollingStock null Track Name", "", rs1.getTrackName());
                Assert.assertEquals("RollingStock null Track Id", "", rs1.getTrackId());

                String testresult;

                /* Place Rolling Stock on layout */
                Location testlocation1 = new Location("Loc1", "Test Town");
                Track testtrack1 = new Track("Trk1", "Testees Office", Track.SIDING);
                
                testresult = rs1.setLocation(testlocation1, testtrack1);
                Assert.assertEquals("RollingStock null Set Location", "type", testresult);
                
                /* type needs to be valid for Track */
                testtrack1.addTypeName("TESTTYPE");
                testresult = rs1.setLocation(testlocation1, testtrack1);
                Assert.assertEquals("RollingStock null Set Location Track type", "type", testresult);
                
                /* type needs to be valid for Location */
                testlocation1.addTypeName("TESTTYPE");
                testresult = rs1.setLocation(testlocation1, testtrack1);
                Assert.assertEquals("RollingStock null Set Location type", "length", testresult);
                
                /* track needs to have a defined length */
                rs1.setLength("41");
                testresult = rs1.setLocation(testlocation1, testtrack1);
                Assert.assertEquals("RollingStock null Set Length null", "length", testresult);
                
                /* track needs to be long enough */
                testtrack1.setLength(40);
                rs1.setLength("41");
                testresult = rs1.setLocation(testlocation1, testtrack1);
                Assert.assertEquals("RollingStock null Set Length short", "length", testresult);
                
                /* track needs to be long enough */
                testtrack1.setLength(44);  // rs length + Coupler == 4
                rs1.setLength("40");
                testresult = rs1.setLocation(testlocation1, testtrack1);
                Assert.assertEquals("RollingStock null Set Length match", "okay", testresult);
                
                /* track needs to accept road */
                testtrack1.setRoadOption(Track.INCLUDEROADS);
                testresult = rs1.setLocation(testlocation1, testtrack1);
                Assert.assertEquals("RollingStock null Set includeroads", "road", testresult);
                
                /* track needs to accept road */
                testtrack1.setRoadOption(Track.INCLUDEROADS);
                testtrack1.addRoadName("TESTROAD");
                testresult = rs1.setLocation(testlocation1, testtrack1);
                Assert.assertEquals("RollingStock Set includeroads", "okay", testresult);
                
                /* track needs to accept road */
                testtrack1.setRoadOption(Track.EXCLUDEROADS);
                testresult = rs1.setLocation(testlocation1, testtrack1);
                Assert.assertEquals("RollingStock Set excluderoads", "road", testresult);
                
                /* track needs to accept road */
                testtrack1.setRoadOption(Track.ALLROADS);
                testresult = rs1.setLocation(testlocation1, testtrack1);
                Assert.assertEquals("RollingStock Set allroads", "okay", testresult);
                
                /* track needs to accept road */
                testtrack1.setRoadOption(Track.EXCLUDEROADS);
                testtrack1.deleteRoadName("TESTROAD");
                testresult = rs1.setLocation(testlocation1, testtrack1);
                Assert.assertEquals("RollingStock Set null excluderoads", "okay", testresult);
        }


    // Ensure minimal setup for log4J

    Turnout t1, t2, t3;
    Sensor s1, s2, s3, s4, s5;
    SignalHead h1, h2, h3, h4;
    
    /**
    * Test-by test initialization.
    * Does log4j for standalone use, and then
    * creates a set of turnouts, sensors and signals
    * as common background for testing
    */
    @Override
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
        
        // create a new instance manager
        InstanceManager i = new InstanceManager(){
            @Override
            protected void init() {
                root = null;
                super.init();
                root = this;
            }
        };
        
        InstanceManager.setTurnoutManager(new InternalTurnoutManager());
        t1 = InstanceManager.turnoutManagerInstance().newTurnout("IT1", "1");
        t2 = InstanceManager.turnoutManagerInstance().newTurnout("IT2", "2");
        t3 = InstanceManager.turnoutManagerInstance().newTurnout("IT3", "3");

        InstanceManager.setSensorManager(new InternalSensorManager());
        s1 = InstanceManager.sensorManagerInstance().newSensor("IS1", "1");
        s2 = InstanceManager.sensorManagerInstance().newSensor("IS2", "2");
        s3 = InstanceManager.sensorManagerInstance().newSensor("IS3", "3");
        s4 = InstanceManager.sensorManagerInstance().newSensor("IS4", "4");
        s5 = InstanceManager.sensorManagerInstance().newSensor("IS5", "5");

        h1 = new jmri.VirtualSignalHead("IH1");
        InstanceManager.signalHeadManagerInstance().register(h1);
        h2 = new jmri.VirtualSignalHead("IH2");
        InstanceManager.signalHeadManagerInstance().register(h2);
        h3 = new jmri.VirtualSignalHead("IH3");
        InstanceManager.signalHeadManagerInstance().register(h3);
        h4 = new jmri.VirtualSignalHead("IH4");
        InstanceManager.signalHeadManagerInstance().register(h4);
    }

	public OperationsRollingStockTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", OperationsRollingStockTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(OperationsRollingStockTest.class);
		return suite;
	}

    // The minimal setup for log4J
    @Override
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
}
