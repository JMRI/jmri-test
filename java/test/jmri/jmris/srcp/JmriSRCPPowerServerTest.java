//JmriSRCPPowerServerTest.java
package jmri.jmris.srcp;

import junit.framework.Assert;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmris.srcp.JmriSRCPPowerServer class
 *
 * @author Paul Bender
 * @version $Revision$
 */
public class JmriSRCPPowerServerTest extends NbTestCase {

    public void testCtor() {
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    // null output string drops characters
                    // could be replaced by one that checks for specific outputs
                    @Override
                    public void write(int b) throws java.io.IOException {
                    }
                });

        JmriSRCPPowerServer a = new JmriSRCPPowerServer(output);
        jmri.util.JUnitAppender.assertErrorMessage("No power manager instance found");
        Assert.assertNotNull(a);
    }

    // from here down is testing infrastructure
    public JmriSRCPPowerServerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SRCPTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(jmri.jmris.srcp.JmriSRCPPowerServerTest.class);

        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        apps.tests.Log4JFixture.tearDown();
    }

    static Logger log = LoggerFactory.getLogger(JmriSRCPPowerServerTest.class.getName());

}
