package jmri.jmrix.srcp;

import junit.framework.Assert;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SRCPPowerManagerTest.java
 *
 * Description:	tests for the jmri.jmrix.srcp.SRCPPowerManager class
 *
 * @author	Bob Jacobsen
 * @version $Revision$
 */
public class SRCPPowerManagerTest extends NbTestCase {

    public void testCtor() {
        SRCPPowerManager m = new SRCPPowerManager();
        Assert.assertNotNull(m);
    }

    public void testBusSpeciticCtor() {
        SRCPBusConnectionMemo sm = new SRCPBusConnectionMemo(new SRCPTrafficController() {
            @Override
            public void sendSRCPMessage(SRCPMessage m, SRCPListener reply) {
            }
        }, "A", 1);
        SRCPPowerManager m = new SRCPPowerManager(sm, 1);
        Assert.assertNotNull(m);
    }

    // from here down is testing infrastructure
    public SRCPPowerManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", SRCPPowerManagerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(SRCPPowerManagerTest.class);
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
    static Logger log = LoggerFactory.getLogger(SRCPPowerManagerTest.class.getName());
}
