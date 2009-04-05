// NcePacketGenFrameTest.java

package jmri.jmrix.nce.packetgen;

import jmri.jmrix.nce.*;

import junit.framework.*;

/**
 * Tests for the jmri.jmrix.nce.packetgen.NcePacketGenFrame class
 * @author	Bob Jacobsen
 * @version     $Revision: 1.3 $
 */
public class NcePacketGenFrameTest extends TestCase {

    public void testFrameCreate() {
        new NcePacketGenFrame();
    }

    public void testPacketNull() {
        NcePacketGenFrame t = new NcePacketGenFrame();
        NceMessage m = t.createPacket("");
        Assert.assertEquals("null pointer",null,m);
    }

    public void testPacketCreate() {
        NcePacketGenFrame t = new NcePacketGenFrame();
        NceMessage m = t.createPacket("12 34 AB 3 19 6 B B1");
        Assert.assertEquals("length",8,m.getNumDataElements());
        Assert.assertEquals("0th byte",0x12,m.getElement(0)&0xFF);
        Assert.assertEquals("1st byte",0x34,m.getElement(1)&0xFF);
        Assert.assertEquals("2nd byte",0xAB,m.getElement(2)&0xFF);
        Assert.assertEquals("3rd byte",0x03,m.getElement(3)&0xFF);
        Assert.assertEquals("4th byte",0x19,m.getElement(4)&0xFF);
        Assert.assertEquals("5th byte",0x06,m.getElement(5)&0xFF);
        Assert.assertEquals("6th byte",0x0B,m.getElement(6)&0xFF);
        Assert.assertEquals("7th byte",0xB1,m.getElement(7)&0xFF);
    }

    // from here down is testing infrastructure

    public NcePacketGenFrameTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {NcePacketGenFrameTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(NcePacketGenFrameTest.class);
        return suite;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(NcePacketGenFrameTest.class.getName());

}
