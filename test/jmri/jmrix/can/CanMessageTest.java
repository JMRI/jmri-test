// CanMessageTest.java

package jmri.jmrix.can;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.can.CanMessage class
 *
 * @author      Bob Jacobsen  Copyright 2008, 2009
 * @version   $Revision: 1.2 $
 */
public class CanMessageTest extends CanMRCommonTest {

    public void testCopyCtor() {
        CanMessage m1 = new CanMessage();
        m1.setExtended(true);
        m1.setHeader(0x12);
        
        CanMessage m2 = new CanMessage(m1);
        Assert.assertTrue("extended", m2.isExtended());
        Assert.assertTrue("header", m2.getHeader()==0x12);
    }

    public void testEqualsOp() {
        CanMessage m1 = new CanMessage();
        m1.setExtended(true);
        m1.setHeader(0x12);

        CanMessage m2 = new CanMessage();
        m2.setExtended(true);
        m2.setHeader(0x12);

        CanMessage m3 = new CanMessage();
        m3.setExtended(false);
        m3.setHeader(0x12);

        Assert.assertTrue("equals self", m1.equals(m1));
        Assert.assertTrue("equals copy", m1.equals(new CanMessage(m1)));        
        Assert.assertTrue("equals same", m1.equals(m2));
        Assert.assertTrue("not equals diff Ext", !m1.equals(m3));
    }
    
    public void testEqualsData() {
        CanMessage m1 = new CanMessage();
        m1.setNumDataElements(2);
        m1.setElement(0, 0x81);
        m1.setElement(1, 0x12);
        
        CanMessage m2 = new CanMessage();
        m2.setNumDataElements(2);
        m2.setElement(0, 0x81);
        m2.setElement(1, 0x12);

        CanMessage m3 = new CanMessage();
        m3.setNumDataElements(2);
        m3.setElement(0, 0x01);
        m3.setElement(1, 0x82);

        Assert.assertTrue("equals self", m1.equals(m1));
        Assert.assertTrue("equals copy", m1.equals(new CanMessage(m1)));        
        Assert.assertTrue("equals same", m1.equals(m2));
        Assert.assertTrue("not equals diff Ext", !m1.equals(m3));
    }
    
    public void testHeaderAccessors() {
        CanMessage m = new CanMessage();
        
        m.setHeader(0x555);
        Assert.assertTrue("Header 0x555", m.getHeader() == 0x555);
        
    }
    
    public void testIdAccessors() {
        CanMessage m = new CanMessage();
        
        m.setCbusId(0x03);
        Assert.assertTrue("Id 0x03", m.getCbusId() == 0x03);
        Assert.assertTrue("Header 0x03", m.getHeader() == 0x03);
    }
    
    public void testRtrBit() {
        CanMessage m = new CanMessage();
        Assert.assertTrue("not rtr at start", !m.isRtr());
        m.setRtr(true);
        Assert.assertTrue("rtr set", m.isRtr());
        m.setRtr(false);
        Assert.assertTrue("rtr unset", !m.isRtr());
    }

    public void testStdExt() {
        CanMessage m = new CanMessage();
        Assert.assertTrue("std at start", !m.isExtended());
        m.setExtended(true);
        Assert.assertTrue("extended", m.isExtended());
        m.setExtended(false);
        Assert.assertTrue("std at end", !m.isExtended());
    }
    
    public void testPriAccessors() {
        CanMessage m = new CanMessage();
        m.setHeader(0);
        
        m.setCbusPri(0x00);
        Assert.assertTrue("Pri 0x00", m.getCbusPri() == 0x00);
        Assert.assertEquals("Header 0x000", 0x000, m.getHeader());

        m.setCbusPri(0x01);
        Assert.assertTrue("Pri 0x01", m.getCbusPri() == 0x01);
        Assert.assertEquals("Header 0x020", 0x200, m.getHeader());

        m.setCbusPri(0x03);
        Assert.assertTrue("Pri 0x03", m.getCbusPri() == 0x03);
        Assert.assertEquals("Header 0x060", 0x600, m.getHeader());

        m.setCbusPri(0x01);
        Assert.assertTrue("Pri 0x01 2nd", m.getCbusPri() == 0x01);
        Assert.assertEquals("Header 0x020", 0x200, m.getHeader());
    }

    public void testDataElements() {
        CanMessage m = new CanMessage();

        m.setNumDataElements(0);
        Assert.assertTrue("0 Elements", m.getNumDataElements() == 0);

        m.setNumDataElements(1);
        Assert.assertTrue("1 Elements", m.getNumDataElements() == 1);
        
        m.setNumDataElements(8);
        Assert.assertTrue("8 Elements", m.getNumDataElements() == 8);
        
        m.setNumDataElements(3);
        m.setElement(0, 0x81);
        m.setElement(1, 0x02);
        m.setElement(2, 0x83);
        Assert.assertTrue("3 Elements", m.getNumDataElements() == 3);
        Assert.assertTrue("3 Element 0", m.getElement(0) == 0x81);
        Assert.assertTrue("3 Element 1", m.getElement(1) == 0x02);
        Assert.assertTrue("3 Element 2", m.getElement(2) == 0x83);
    }

    // from here down is testing infrastructure

    public CanMessageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        apps.tests.AllTest.initLogging();
        String[] testCaseName = {"-noloading", CanMessageTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite(CanMessageTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
}
