// CarOwnersTest.java
package jmri.jmrit.operations.rollingstock.cars;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import javax.swing.JComboBox;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManagerXml;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.engines.EngineManagerXml;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.routes.RouteManagerXml;
import jmri.jmrit.operations.setup.OperationsSetupXml;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManagerXml;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import jmri.util.JUnitUtil;
import org.jdom2.JDOMException;

/**
 * Tests for the Operations RollingStock Cars class Last manually cross-checked
 * on 20090131
 *
 * @author	Bob Coleman Copyright (C) 2008, 2009
 * @version $Revision$
 */
public class CarOwnersTest extends TestCase {

    public void testCarOwners() {
        CarOwners co1 = CarOwners.instance();

        co1.addName("Rich Guy 1");
        Assert.assertTrue("Car Owner Add", co1.containsName("Rich Guy 1"));
        Assert.assertFalse("Car Owner Never Added", co1.containsName("Richer Guy 2"));
        co1.addName("Really Rich 3");
        Assert.assertTrue("Car Owner Still Has", co1.containsName("Rich Guy 1"));
        Assert.assertTrue("Car Owner Add second", co1.containsName("Really Rich 3"));
        String[] owners = co1.getNames();
        Assert.assertEquals("First owner name", "Really Rich 3", owners[0]);
        Assert.assertEquals("2nd owner name", "Rich Guy 1", owners[1]);
        JComboBox<?> box = co1.getComboBox();
        Assert.assertEquals("First comboBox owner name", "Really Rich 3", box.getItemAt(0));
        Assert.assertEquals("2nd comboBox owner name", "Rich Guy 1", box.getItemAt(1));
        co1.deleteName("Really Rich 3");
        Assert.assertFalse("Car Owner Delete", co1.containsName("Really Rich 3"));
        co1.deleteName("Rich Guy 1");
        Assert.assertFalse("Car Owner Delete second", co1.containsName("Rich Guy 1"));
    }

    // from here down is testing infrastructure
    // Ensure minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initDebugThrottleManager();
        JUnitUtil.initIdTagManager();
        jmri.InstanceManager.setShutDownManager( new
                 jmri.managers.DefaultShutDownManager() {
                    @Override
                    public void register(jmri.ShutDownTask s){
                       // do nothing with registered shutdown tasks for testing.
                    }
                 });
        // set the locale to US English
        Locale.setDefault(Locale.ENGLISH);

        // Repoint OperationsSetupXml to JUnitTest subdirectory
        OperationsSetupXml.setOperationsDirectoryName("operations" + File.separator + "JUnitTest");
        // Change file names to ...Test.xml
        OperationsSetupXml.instance().setOperationsFileName("OperationsJUnitTest.xml");
        RouteManagerXml.instance().setOperationsFileName("OperationsJUnitTestRouteRoster.xml");
        EngineManagerXml.instance().setOperationsFileName("OperationsJUnitTestEngineRoster.xml");
        CarManagerXml.instance().setOperationsFileName("OperationsJUnitTestCarRoster.xml");
        LocationManagerXml.instance().setOperationsFileName("OperationsJUnitTestLocationRoster.xml");
        TrainManagerXml.instance().setOperationsFileName("OperationsJUnitTestTrainRoster.xml");

        // Need to clear out CarManager global variables
        CarManager manager = CarManager.instance();
        CarColors.instance().dispose();
        CarLengths.instance().dispose();
        CarLoads.instance().dispose();
        CarRoads.instance().dispose();
        CarTypes.instance().dispose();
        manager.dispose();
    }

    public CarOwnersTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", CarOwnersTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(CarOwnersTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void tearDown() throws Exception {
       JUnitUtil.resetInstanceManager();
       apps.tests.Log4JFixture.tearDown();
       super.tearDown();
    }
}
