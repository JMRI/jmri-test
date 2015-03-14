// PackageDemo.java
package jmri.jmrix.openlcb.swing.networktree;

import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/**
 * Demos for the jmri.jmrix.openlcb package.
 *
 * @author Bob Jacobsen Copyright 2009, 2012, 2014
 * @version $Revision$
 */
public class PackageDemo extends NbTestCase {

    public void testDefinitions() {
    }

    // from here down is testing infrastructure
    public PackageDemo(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        apps.tests.AllTest.initLogging();
        String[] testCaseName = {"-noloading", PackageDemo.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite("jmri.jmrix.openlcb.swing.networktree.PackageDemo");

        suite.addTest(CdiPanelDemo.suite());
        suite.addTest(NetworkTreePaneDemo.suite());

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
