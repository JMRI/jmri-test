// MonitorFrameTest.java

package jmri.jmrix.pricom.pockettester;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit tests for the MonitorFrame class
 * @author		Bob Jacobsen  Copyright 2005
 * @version		$Revision$
 */
public class MonitorFrameTest extends TestCase {

    public void testCreate() {
        new MonitorFrame();
    }

    // create and show, with some data present
    public void testShow() throws Exception {
        MonitorFrame f = new MonitorFrame();
        f.initComponents();
        f.setVisible(true);
        f.asciiFormattedMessage(PocketTesterTest.version);
        f.asciiFormattedMessage(PocketTesterTest.speed0003A);
        f.asciiFormattedMessage(PocketTesterTest.idlePacket);
    }

    // from here down is testing infrastructure
    public MonitorFrameTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {MonitorFrameTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(MonitorFrameTest.class);
        return suite;
    }

}

