// XNetSensorTest.java
package jmri.jmrix.lenz;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.lenz.XNetSensor class.
 * @author	    Paul Bender  Copyright 2004
 * @version         $Revision: 2.4 $
 */
public class XNetSensorTest extends TestCase {

    public void testXNetSensorCreate() {
        XNetSensor t = new XNetSensor("XS042");

        // created in UNKNOWN state
        Assert.assertTrue(t.getKnownState() == jmri.Sensor.UNKNOWN);
    }

    // XNetSensor test for incoming status message
    public void testXNetSensorStatusMsg() {
        XNetInterfaceScaffold xnis = new XNetInterfaceScaffold(new LenzCommandStation());

        XNetSensor t = new XNetSensor("XS044");
        XNetReply m;


        // Verify this was created in UNKNOWN state
        Assert.assertTrue(t.getKnownState() == jmri.Sensor.UNKNOWN);

        // notify the Sensor that somebody else changed it...

        m = new XNetReply();
	m.setElement(0, 0x42);     // Opcode for feedback response
        m.setElement(1, 0x05);     // The feedback encoder address
        m.setElement(2, 0x48);     // A bit pattern telling which
                                    // bits of the lower nibble
                                    // are on in the message.
        m.setElement(3, 0x0f);     // The XOR of everything above
        //xnis.sendTestMessage(m);
        t.message(m);
        Assert.assertEquals("Known state after activate ", jmri.Sensor.ACTIVE, t.getKnownState());

        m = new XNetReply();
	m.setElement(0, 0x42);     // Opcode for feedback response
        m.setElement(1, 0x05);     // The feedback encoder address
        m.setElement(2, 0x40);     // A bit pattern telling which
                                    // bits of the lower nibble
                                    // are on in the message.
        m.setElement(3, 0x07);     // The XOR of everything above
        //xnis.sendTestMessage(m);
        t.message(m);

        Assert.assertEquals("Known state after inactivate ", jmri.Sensor.INACTIVE, t.getKnownState());

    }


    // XNetSensor test for setting state
    public void testXNetSensorSetState() throws jmri.JmriException {
        XNetSensor t = new XNetSensor("XS043");

        t.setKnownState(jmri.Sensor.ACTIVE);
        Assert.assertTrue(t.getKnownState() == jmri.Sensor.ACTIVE);
        t.setKnownState(jmri.Sensor.INACTIVE);
        Assert.assertTrue(t.getKnownState() == jmri.Sensor.INACTIVE);
    }

    // XNetSensor test for outgoing status request
    public void testXNetSensorStatusRequest() {
        XNetInterfaceScaffold xnis = new XNetInterfaceScaffold(new LenzCommandStation());

        XNetSensor t = new XNetSensor("XS042");

        t.requestUpdateFromLayout();
        // check that the correct message was sent
        Assert.assertEquals("Sensor Status Request Sent","42 05 80 00",xnis.outbound.elementAt(0).toString());

    }

    // from here down is testing infrastructure

    public XNetSensorTest(String s) {
    	super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", XNetSensor.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(XNetSensorTest.class);
        return suite;
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XNetSensorTest.class.getName());

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

}
