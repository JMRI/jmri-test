// MonitorFrameTest.java

package jmri.jmrix.openlcb.swing.monitor;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.can.swing.monitor.MonitorFrame class
 *
 * @author      Bob Jacobsen  Copyright 2010
 * @version   $Revision$
 */
public class MonitorFrameTest extends TestCase {

    String testFormatted;
    String testRaw;
    
    public void testFormatMsg() throws Exception {
        MonitorFrame f = new MonitorFrame(){
            public void nextLine(String s1, String s2) {
                testFormatted = s1;
                testRaw = s2;
            }
        };
        
        jmri.jmrix.can.CanMessage msg 
            = new jmri.jmrix.can.CanMessage(
                new int[]{1,2});
        msg.setExtended(true);
        msg.setHeader(0x12345678);
        
        f.message(msg);
        
        Assert.assertEquals("formatted", "M: [12345678] 01 02\n", testFormatted);
        Assert.assertEquals("raw", "01 02", testRaw);
    }
    
    public void testFormatReply() throws Exception {
        MonitorFrame f = new MonitorFrame(){
            public void nextLine(String s1, String s2) {
                testFormatted = s1;
                testRaw = s2;
            }
        };
        
        jmri.jmrix.can.CanReply msg 
            = new jmri.jmrix.can.CanReply(
                new int[]{1,2});
        msg.setExtended(true);
        msg.setHeader(0x12345678);
        
        f.reply(msg);
        
        Assert.assertEquals("formatted", "R: [12345678] 01 02\n", testFormatted);
        Assert.assertEquals("raw", "01 02", testRaw);
    }
    
    // from here down is testing infrastructure

    public MonitorFrameTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        apps.tests.AllTest.initLogging();
        String[] testCaseName = {"-noloading", MonitorFrameTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite(MonitorFrameTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
}
