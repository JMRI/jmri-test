// XNetTest.java


package jmri.jmrix.lenz;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.lenz package
 * @author			Bob Jacobsen
 * @version			$Revision: 2.9 $
 */
public class XNetTest extends TestCase {

    // from here down is testing infrastructure

    public XNetTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {XNetTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrix.lenz.XNetTest");  // no tests in this class itself
        suite.addTest(new TestSuite(XNetMessageTest.class));
        suite.addTest(new TestSuite(XNetReplyTest.class));
        suite.addTest(new TestSuite(XNetTurnoutTest.class));
        suite.addTest(new TestSuite(XNetSensorTest.class));
        suite.addTest(new TestSuite(XNetLightTest.class));
        suite.addTest(new TestSuite(XNetPacketizerTest.class));
        suite.addTest(new TestSuite(XNetTurnoutManagerTest.class));
        suite.addTest(new TestSuite(XNetSensorManagerTest.class));
        suite.addTest(new TestSuite(XNetLightManagerTest.class));
        suite.addTest(new TestSuite(XNetTrafficControllerTest.class));
        suite.addTest(new TestSuite(XNetSystemConnectionMemoTest.class));
        suite.addTest(new TestSuite(XNetTrafficRouterTest.class));
        suite.addTest(new TestSuite(XNetThrottleTest.class));
        suite.addTest(new TestSuite(XNetConsistManagerTest.class));
        suite.addTest(new TestSuite(XNetConsistTest.class));
        suite.addTest(new TestSuite(XNetInitilizationManagerTest.class));
        suite.addTest(new TestSuite(XNetProgrammerTest.class));
        suite.addTest(new TestSuite(XNetProgrammerManagerTest.class));
        suite.addTest(new TestSuite(XNetOpsModeProgrammerTest.class));
        
        if (!System.getProperty("jmri.headlesstest","false").equals("true")) {
            suite.addTest(new TestSuite(jmri.jmrix.lenz.packetgen.PacketGenFrameTest.class));
            suite.addTest(jmri.jmrix.lenz.li100.LI100Test.suite());
            suite.addTest(jmri.jmrix.lenz.li100f.LI100FTest.suite());
            suite.addTest(jmri.jmrix.lenz.li101.LI101Test.suite());
            suite.addTest(jmri.jmrix.lenz.liusb.LIUSBTest.suite());
            suite.addTest(jmri.jmrix.lenz.lz100.LZ100Test.suite());
            suite.addTest(jmri.jmrix.lenz.lzv100.LZV100Test.suite());
            suite.addTest(jmri.jmrix.lenz.xntcp.XnTcpTest.suite());
            suite.addTest(jmri.jmrix.lenz.xnetsimulator.XNetSimulatorTest.suite());
        }
        
        return suite;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(XNetTest.class.getName());

}
