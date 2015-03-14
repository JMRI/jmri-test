// TieToolFrameDemo.java
package jmri.jmrix.openlcb.swing.tie;

import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/**
 * Tests for the jmri.jmrix.can.swing.tie.TieToolFrame class
 *
 * @author Bob Jacobsen Copyright 2008
 * @version $Revision$
 */
public class TieToolFrameDemo extends NbTestCase {

    public void testCreateAndShow() throws Exception {
        jmri.util.JmriJFrame f = new TieToolFrame();
        f.initComponents();
        f.pack();
        f.setVisible(true);

        // close frame
        f.dispose();
    }

    // from here down is testing infrastructure
    public TieToolFrameDemo(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        apps.tests.AllTest.initLogging();
        String[] testCaseName = {"-noloading", TieToolFrameDemo.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        apps.tests.AllTest.initLogging();
        NbTestSuite suite = new NbTestSuite(TieToolFrameDemo.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }
}
