// RpsMonTest.java
package jmri.jmrix.rps.rpsmon;

import javax.swing.JFrame;
import junit.framework.Assert;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/**
 * Tests for the jmri.jmrix.rps.rpsmon package.
 *
 * @author Bob Jacobsen Copyright 2006
 * @version $Revision$
 */
public class RpsMonTest extends NbTestCase {

    // show the window
    public void testDisplay() {
        new RpsMonAction().actionPerformed(null);
    }

    public void testFrameCreation() {
        jmri.InstanceManager.store(jmri.managers.DefaultUserMessagePreferences.getInstance(), jmri.UserPreferencesManager.class);
        JFrame f = jmri.util.JmriJFrame.getFrame("RPS Monitor");
        Assert.assertTrue("found frame", f != null);
        f.dispose();
    }

    // from here down is testing infrastructure
    public RpsMonTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {RpsMonTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        apps.tests.AllTest.initLogging();
        NbTestSuite suite = new NbTestSuite(RpsMonTest.class);
        return suite;
    }

}
