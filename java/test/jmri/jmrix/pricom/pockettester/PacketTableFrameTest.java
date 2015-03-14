// PacketTableFrameTest.java
package jmri.jmrix.pricom.pockettester;

import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/**
 * JUnit tests for the MonitorFrame class
 *
 * @author	Bob Jacobsen Copyright 2005
 * @version	$Revision$
 */
public class PacketTableFrameTest extends NbTestCase {

    public void testCreate() {
        new PacketTableFrame();
    }

    // create and show, with some data present
    public void testShow() throws Exception {
        PacketTableFrame f = new PacketTableFrame();
        f.initComponents();
        f.setVisible(true);
        f.asciiFormattedMessage(PocketTesterTest.speed012A);
        f.asciiFormattedMessage(PocketTesterTest.speed0123A);
        f.asciiFormattedMessage(PocketTesterTest.speed012A);
        f.asciiFormattedMessage(PocketTesterTest.acc0222A);

        // close frame
        f.dispose();
    }

    // from here down is testing infrastructure
    public PacketTableFrameTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {PacketTableFrameTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(PacketTableFrameTest.class);
        return suite;
    }

}
