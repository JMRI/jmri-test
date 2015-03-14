// PackageTest.java
package jmri.jmrit.display.layoutEditor;

import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/**
 * Tests for the jmrit.display.layoutEditor package
 *
 * @author	Bob Jacobsen Copyright 2008, 2009, 2010
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
        NbTestSuite suite = new NbTestSuite(PackageTest.class.getName());

        suite.addTest(SchemaTest.suite());

        if (!System.getProperty("jmri.headlesstest", "false").equals("true")) {
            suite.addTest(LayoutEditorWindowTest.suite());
            suite.addTest(LEConnectivityTest.suite());
        }

        return suite;
    }
}
