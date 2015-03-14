// SwingTest.java
package jmri.jmrix.rps.swing;

import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/**
 * Tests for the jmri.jmrix.rps.swing package.
 *
 * @author Bob Jacobsen Copyright 2008
 * @version $Revision$
 */
public class SwingTest extends NbTestCase {

    // from here down is testing infrastructure
    public SwingTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SwingTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        apps.tests.AllTest.initLogging();
        NbTestSuite suite = new NbTestSuite("jmri.jmrix.rps.SwingTest");
        suite.addTest(jmri.jmrix.rps.swing.AffineEntryPanelTest.suite());
        suite.addTest(jmri.jmrix.rps.swing.polling.PollTableActionTest.suite());
        suite.addTest(jmri.jmrix.rps.swing.debugger.DebuggerTest.suite()); // do last to display in front
        return suite;
    }

}
