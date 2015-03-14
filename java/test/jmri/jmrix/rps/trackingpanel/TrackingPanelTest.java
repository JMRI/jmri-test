// TrackingPanelTest.java
package jmri.jmrix.rps.trackingpanel;

import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/**
 * Tests for the jmri.jmrix.rps package.
 *
 * @author Bob Jacobsen Copyright 2006
 * @version $Revision$
 */
public class TrackingPanelTest extends NbTestCase {

    // from here down is testing infrastructure
    public TrackingPanelTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {TrackingPanelTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        apps.tests.AllTest.initLogging();
        NbTestSuite suite = new NbTestSuite("jmri.jmrix.rps.trackingpanel.TrackingPanelTest");
        suite.addTest(RpsTrackingFrameTest.suite());
        suite.addTest(RpsTrackingPanelTest.suite());
        return suite;
    }

}
