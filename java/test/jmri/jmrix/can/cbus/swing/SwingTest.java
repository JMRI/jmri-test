// SwingTest.java
package jmri.jmrix.can.cbus.swing;

import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/**
 * Tests for the jmri.jmrix.can.cbus.swing package.
 *
 * @author Bob Jacobsen Copyright 2008
 * @version $Revision$
 */
public class SwingTest extends NbTestCase {

    // from here down is testing infrastructure
    public SwingTest(String s) {
        super(s);
    }

    public void testDefinitions() {
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", SwingTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        apps.tests.AllTest.initLogging();
        NbTestSuite suite = new NbTestSuite("jmri.jmrix.can.cbus.swing.SwingTest");
        suite.addTest(jmri.jmrix.can.cbus.swing.configtool.ConfigToolActionTest.suite());
        return suite;
    }

}
