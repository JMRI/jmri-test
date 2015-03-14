// PackageTest.java
package jmri.web.servlet.frameimage;

import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/**
 * Invokes complete set of tests in the jmri.web.servlet.frameimage tree
 *
 * @author	Bob Jacobsen Copyright 2013
 * @version $Revision$
 */
public class PackageTest extends NbTestCase {

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {PackageTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite("jmri.web.servlet.frameimage.PackageTest");   // no tests in this class itself
        suite.addTest(JmriJFrameServletTest.suite());
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
