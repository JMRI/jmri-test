package jmri.jmrix.loconet;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.loconet package.
 * @author	Bob Jacobsen Copyright 2001, 2003
 * @version     $Revision: 1.22 $
 */
public class LocoNetTest extends TestCase {

    // from here down is testing infrastructure

    public LocoNetTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {LocoNetTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrix.loconet.LocoNetTest");  // no tests in this class itself

        suite.addTest(jmri.jmrix.loconet.LocoNetThrottledTransmitterTest.suite());

        if (!System.getProperty("jmri.headlesstest","false").equals("true")) {
            suite.addTest(jmri.jmrix.loconet.sdf.SdfTest.suite());
            suite.addTest(jmri.jmrix.loconet.locomon.LocoMonTest.suite());
            suite.addTest(jmri.jmrix.loconet.locostats.LocoStatsTest.suite());
            suite.addTest(jmri.jmrix.loconet.soundloader.SoundLoaderTest.suite());
            suite.addTest(jmri.jmrix.loconet.spjfile.SpjFileTest.suite());
            suite.addTest(new TestSuite(Se8AlmImplementationTest.class));
            suite.addTest(new TestSuite(SecurityElementTest.class));
        }
        
        suite.addTest(new TestSuite(SlotManagerTest.class));
        suite.addTest(new TestSuite(LocoNetSlotTest.class));
        suite.addTest(new TestSuite(LnOpsModeProgrammerTest.class));
        suite.addTest(new TestSuite(LocoNetMessageTest.class));
        suite.addTest(new TestSuite(LnTrafficControllerTest.class));
        suite.addTest(new TestSuite(LnTrafficRouterTest.class));
        suite.addTest(new TestSuite(LnPacketizerTest.class));
        suite.addTest(LnPowerManagerTest.suite());
        suite.addTest(LnTurnoutTest.suite());
        suite.addTest(LnTurnoutManagerTest.suite());
        suite.addTest(LnReporterTest.suite());
        suite.addTest(LnSensorTest.suite());
        suite.addTest(LnSensorAddressTest.suite());
        suite.addTest(LnSensorManagerTest.suite());

        if (!System.getProperty("jmri.headlesstest","false").equals("true")) {
            suite.addTest(jmri.jmrix.loconet.locoio.LocoIOTest.suite());
            suite.addTest(jmri.jmrix.loconet.locogen.LocoGenPanelTest.suite());
        }
        
        return suite;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LocoNetTest.class.getName());

}
