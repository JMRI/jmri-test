package jmri.jmrix.srcp;

import junit.framework.Assert;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SRCPThrottleTest.java
 *
 * Description:	tests for the jmri.jmrix.srcp.SRCPThrottle class
 *
 * @author	Bob Jacobsen
 * @version $Revision$
 */
public class SRCPThrottleTest extends NbTestCase {

    public void testCtor() {
        SRCPBusConnectionMemo sm = new SRCPBusConnectionMemo(new SRCPTrafficController() {
            @Override
            public void sendSRCPMessage(SRCPMessage m, SRCPListener reply) {
            }
        }, "TEST", 1);
        SRCPThrottle s = new SRCPThrottle(sm, new jmri.DccLocoAddress(1, true));
        Assert.assertNotNull(s);
    }

    // from here down is testing infrastructure
    public SRCPThrottleTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", SRCPThrottleTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(SRCPThrottleTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    @Override
    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }
    static Logger log = LoggerFactory.getLogger(SRCPThrottleTest.class.getName());
}
