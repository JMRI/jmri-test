// OlcbSensorTest.java

package jmri.jmrix.openlcb;

import jmri.Sensor;

import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.TestTrafficController;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.openlcb.OlcbSensor class.
 *
 * @author	Bob Jacobsen Copyright 2008, 2010
 * @version     $Revision$
 */
public class OlcbSensorTest extends TestCase {

    public void testIncomingChange() {
        // load dummy TrafficController
        TestTrafficController t = new TestTrafficController();
        Assert.assertNotNull("exists", t );
        OlcbSensor s = new OlcbSensor("MS1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.9");
        
        // message for Active and Inactive
        CanMessage mActive = new CanMessage( //[182df123] 01 02 03 04 05 06 07 08
                    new int[]{1,2,3,4,5,6,7,8}
        );
        mActive.setHeader(0x182df123);
        mActive.setExtended(true);
        
        CanMessage mInactive = new CanMessage( //[182df123] 01 02 03 04 05 06 07 09
                    new int[]{1,2,3,4,5,6,7,9}
        );
        mInactive.setHeader(0x182df123);
        mInactive.setExtended(true);

        // check states
        Assert.assertTrue(s.getKnownState()==Sensor.UNKNOWN);
        
        s.message(mActive);
        Assert.assertTrue(s.getKnownState()==Sensor.ACTIVE);
        
        s.message(mInactive);
        Assert.assertTrue(s.getKnownState()==Sensor.INACTIVE);
        
    }

    public void testLocalChange() throws jmri.JmriException {
        // load dummy TrafficController
        TestTrafficController t = new TestTrafficController();
        
        OlcbSensor s = new OlcbSensor("MS1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.9");
        t.rcvMessage = null;
        s.setKnownState(Sensor.ACTIVE);
        Assert.assertTrue(s.getKnownState()==Sensor.ACTIVE);
        Assert.assertTrue(new OlcbAddress("1.2.3.4.5.6.7.8").match(t.rcvMessage));
        
        t.rcvMessage = null;
        s.setKnownState(Sensor.INACTIVE);
        Assert.assertTrue(s.getKnownState()==Sensor.INACTIVE);
        Assert.assertTrue(new OlcbAddress("1.2.3.4.5.6.7.9").match(t.rcvMessage));
    }
    
    // from here down is testing infrastructure

    public OlcbSensorTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
    	String[] testCaseName = {OlcbSensorTest.class.getName()};
    	junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(OlcbSensorTest.class);
        return suite;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(OlcbSensorTest.class.getName());
    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
}
