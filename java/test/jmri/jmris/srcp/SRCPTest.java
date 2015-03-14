//SRCPTest.java
package jmri.jmris.srcp;

import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmris.srcp package
 *
 * @author Paul Bender
 * @version $Revision$
 */
public class SRCPTest extends NbTestCase {

    // from here down is testing infrastructure
    public SRCPTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SRCPTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite("jmri.jmris.srcp.SRCPTest");  // no tests in this class itself
        suite.addTest(jmri.jmris.srcp.JmriSRCPServerTest.suite());
        suite.addTest(jmri.jmris.srcp.parser.SRCPParserTests.suite());
        suite.addTest(jmri.jmris.srcp.JmriSRCPTurnoutServerTest.suite());
        suite.addTest(jmri.jmris.srcp.JmriSRCPSensorServerTest.suite());
        suite.addTest(jmri.jmris.srcp.JmriSRCPPowerServerTest.suite());
        suite.addTest(jmri.jmris.srcp.JmriSRCPProgrammerServerTest.suite());
        suite.addTest(jmri.jmris.srcp.JmriSRCPTimeServerTest.suite());

        if (!System.getProperty("jmri.headlesstest", "false").equals("true")) {
            // put any tests that require a UI here.
        }

        return suite;
    }

    static Logger log = LoggerFactory.getLogger(SRCPTest.class.getName());

}
