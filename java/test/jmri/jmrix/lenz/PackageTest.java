// PackageTest.java
package jmri.jmrix.lenz;

import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.lenz package
 *
 * @author	Bob Jacobsen
 * @version	$Revision$
 */
public class PackageTest extends NbTestCase {

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {PackageTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite("jmri.jmrix.lenz.XNetTest");  // no tests in this class itself
        suite.addTest(new NbTestSuite(LenzCommandStationTest.class));
        suite.addTest(new NbTestSuite(LenzConnectionTypeListTest.class));
        suite.addTest(new NbTestSuite(XNetMessageTest.class));
        suite.addTest(new NbTestSuite(XNetReplyTest.class));
        suite.addTest(new NbTestSuite(XNetTurnoutTest.class));
        suite.addTest(new NbTestSuite(XNetSensorTest.class));
        suite.addTest(new NbTestSuite(XNetLightTest.class));
        suite.addTest(new NbTestSuite(XNetPacketizerTest.class));
        suite.addTest(new NbTestSuite(XNetTurnoutManagerTest.class));
        suite.addTest(new NbTestSuite(XNetSensorManagerTest.class));
        suite.addTest(new NbTestSuite(XNetLightManagerTest.class));
        suite.addTest(new NbTestSuite(XNetTrafficControllerTest.class));
        suite.addTest(new NbTestSuite(XNetSystemConnectionMemoTest.class));
        suite.addTest(new NbTestSuite(XNetThrottleTest.class));
        suite.addTest(new NbTestSuite(XNetConsistManagerTest.class));
        suite.addTest(new NbTestSuite(XNetConsistTest.class));
        suite.addTest(new NbTestSuite(XNetInitializationManagerTest.class));
        suite.addTest(new NbTestSuite(XNetProgrammerTest.class));
        suite.addTest(new NbTestSuite(XNetProgrammerManagerTest.class));
        suite.addTest(new NbTestSuite(XNetOpsModeProgrammerTest.class));
        suite.addTest(new NbTestSuite(XNetPowerManagerTest.class));
        suite.addTest(new NbTestSuite(XNetThrottleManagerTest.class));
        suite.addTest(new NbTestSuite(XNetExceptionTest.class));
        suite.addTest(new NbTestSuite(XNetMessageExceptionTest.class));
        suite.addTest(new NbTestSuite(XNetStreamPortControllerTest.class));
        suite.addTest(jmri.jmrix.lenz.li100.LI100Test.suite());
        suite.addTest(jmri.jmrix.lenz.li100f.LI100FTest.suite());
        suite.addTest(jmri.jmrix.lenz.li101.LI101Test.suite());
        suite.addTest(jmri.jmrix.lenz.liusb.LIUSBTest.suite());
        suite.addTest(jmri.jmrix.lenz.xntcp.XnTcpTest.suite());
        suite.addTest(jmri.jmrix.lenz.liusbserver.LIUSBServerTest.suite());
        suite.addTest(jmri.jmrix.lenz.liusbethernet.LIUSBEthernetTest.suite());
        suite.addTest(jmri.jmrix.lenz.xnetsimulator.XNetSimulatorTest.suite());
        suite.addTest(jmri.jmrix.lenz.hornbyelite.EliteTest.suite());

        if (!System.getProperty("jmri.headlesstest", "false").equals("true")) {
            suite.addTest(jmri.jmrix.lenz.swing.SwingTest.suite());
        }

        return suite;
    }

    static Logger log = LoggerFactory.getLogger(PackageTest.class.getName());

}
