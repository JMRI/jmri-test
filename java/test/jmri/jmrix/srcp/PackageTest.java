// PackageTest.java
package jmri.jmrix.srcp;

import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.srcp package
 *
 * @author	Paul Bender
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
        NbTestSuite suite = new NbTestSuite("jmri.jmrix.srcp.SRCPTest");  // no tests in this class itself
        suite.addTest(new NbTestSuite(SRCPReplyTest.class));
        suite.addTest(new NbTestSuite(SRCPMessageTest.class));
        suite.addTest(new NbTestSuite(SRCPTrafficControllerTest.class));
        suite.addTest(new NbTestSuite(SRCPSystemConnectionMemoTest.class));
        suite.addTest(new NbTestSuite(SRCPBusConnectionMemoTest.class));
        suite.addTest(new NbTestSuite(SRCPTurnoutManagerTest.class));
        suite.addTest(new NbTestSuite(SRCPTurnoutTest.class));
        suite.addTest(new NbTestSuite(SRCPSensorManagerTest.class));
        suite.addTest(new NbTestSuite(SRCPSensorTest.class));
        suite.addTest(new NbTestSuite(SRCPThrottleManagerTest.class));
        suite.addTest(new NbTestSuite(SRCPThrottleTest.class));
        suite.addTest(new NbTestSuite(SRCPPowerManagerTest.class));
        suite.addTest(new NbTestSuite(SRCPProgrammerTest.class));
        suite.addTest(new NbTestSuite(SRCPProgrammerManagerTest.class));
        suite.addTest(new NbTestSuite(SRCPClockControlTest.class));
        suite.addTest(jmri.jmrix.srcp.parser.SRCPClientParserTests.suite());

        return suite;
    }

    static Logger log = LoggerFactory.getLogger(PackageTest.class.getName());

}
