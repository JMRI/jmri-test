// PollTableActionTest.java
package jmri.jmrix.rps.swing.polling;

import javax.swing.JFrame;
import junit.framework.Assert;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/**
 * Tests for the jmri.jmrix.rps.swing.polling package.
 *
 * @author Bob Jacobsen Copyright 2008
 * @version $Revision$
 */
public class PollTableActionTest extends NbTestCase {

    // Show the window
    public void testDisplay() {
        new PollTableAction().actionPerformed(null);
        // confirm window was created
        JFrame f = jmri.util.JmriJFrame.getFrame("RPS Polling Control");
        Assert.assertTrue("found frame", f != null);
        f.dispose();
    }

    // from here down is testing infrastructure
    public PollTableActionTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {PollTableActionTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        apps.tests.AllTest.initLogging();
        NbTestSuite suite = new NbTestSuite(PollTableActionTest.class);
        return suite;
    }

}
