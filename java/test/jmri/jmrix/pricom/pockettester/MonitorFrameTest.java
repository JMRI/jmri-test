// MonitorFrameTest.java
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
public class MonitorFrameTest extends NbTestCase {

    public void testCreate() {
        new MonitorFrame();
    }

    // create and show, with some data present
    public void testShow() throws Exception {
        jmri.InstanceManager.store(jmri.managers.DefaultUserMessagePreferences.getInstance(), jmri.UserPreferencesManager.class);
        MonitorFrame f = new MonitorFrame();
        f.initComponents();
        f.setVisible(true);
        f.asciiFormattedMessage(PocketTesterTest.version);
        f.asciiFormattedMessage(PocketTesterTest.speed0003A);
        f.asciiFormattedMessage(PocketTesterTest.idlePacket);

        f.dispose();
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
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(MonitorFrameTest.class);
        return suite;
    }

}
