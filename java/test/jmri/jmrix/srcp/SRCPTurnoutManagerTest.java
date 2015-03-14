package jmri.jmrix.srcp;

import junit.framework.Assert;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SRCPTurnoutManagerTest.java
 *
 * Description:	tests for the jmri.jmrix.srcp.SRCPTurnoutManager class
 *
 * @author	Bob Jacobsen
 * @version $Revision$
 */
public class SRCPTurnoutManagerTest extends NbTestCase {

    public void testCtor() {
        SRCPTurnoutManager m = new SRCPTurnoutManager();
        Assert.assertNotNull(m);
    }

    public void testBusCtor() {
        SRCPTrafficController et = new SRCPTrafficController() {
            @Override
            public void sendSRCPMessage(SRCPMessage m, SRCPListener l) {
                // we aren't actually sending anything to a layout.
            }
        };
        SRCPBusConnectionMemo memo = new SRCPBusConnectionMemo(et, "TEST", 1);
        SRCPTurnoutManager m = new SRCPTurnoutManager(memo, memo.getBus());
        Assert.assertNotNull(m);
    }

    // from here down is testing infrastructure
    public SRCPTurnoutManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", SRCPTurnoutManagerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(SRCPTurnoutManagerTest.class);
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
    static Logger log = LoggerFactory.getLogger(SRCPTurnoutManagerTest.class.getName());
}
