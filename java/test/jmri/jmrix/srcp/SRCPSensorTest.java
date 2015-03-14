package jmri.jmrix.srcp;

import junit.framework.Assert;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SRCPSensorTest.java
 *
 * Description:	tests for the jmri.jmrix.srcp.SRCPSensor class
 *
 * @author	Bob Jacobsen
 * @version $Revision$
 */
public class SRCPSensorTest extends NbTestCase {

    public void testCtor() {
        SRCPBusConnectionMemo sm = new SRCPBusConnectionMemo(new SRCPTrafficController() {
            @Override
            public void sendSRCPMessage(SRCPMessage m, SRCPListener reply) {
            }
        }, "A", 1);
        SRCPSensor s = new SRCPSensor(1, sm);
        Assert.assertNotNull(s);
    }

    // from here down is testing infrastructure
    public SRCPSensorTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", SRCPSensorTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(SRCPSensorTest.class);
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
    static Logger log = LoggerFactory.getLogger(SRCPSensorTest.class.getName());
}
