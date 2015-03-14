// PackageTest.java
package jmri.jmrix.jmriclient;

import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.jmriclient package
 *
 * @author	Bob Jacobsen
 * @version	$Revision: 18472 $
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
        NbTestSuite suite = new NbTestSuite("jmri.jmrix.jmriclient.JMRiClientTest");  // no tests in this class itself
        suite.addTest(new NbTestSuite(JMRIClientMessageTest.class));
        suite.addTest(new NbTestSuite(JMRIClientReplyTest.class));
        suite.addTest(new NbTestSuite(JMRIClientTurnoutTest.class));
        suite.addTest(new NbTestSuite(JMRIClientSensorTest.class));
        suite.addTest(new NbTestSuite(JMRIClientReporterTest.class));
        suite.addTest(new NbTestSuite(JMRIClientTurnoutManagerTest.class));
        suite.addTest(new NbTestSuite(JMRIClientSensorManagerTest.class));
        suite.addTest(new NbTestSuite(JMRIClientReporterManagerTest.class));
        suite.addTest(new NbTestSuite(JMRIClientTrafficControllerTest.class));
        suite.addTest(new NbTestSuite(JMRIClientSystemConnectionMemoTest.class));
        suite.addTest(new NbTestSuite(JMRIClientPowerManagerTest.class));

        // if (!System.getProperty("jmri.headlesstest","false").equals("true")) {
        // there are currently no swing tests.
        // }
        return suite;
    }

    static Logger log = LoggerFactory.getLogger(PackageTest.class.getName());

}
