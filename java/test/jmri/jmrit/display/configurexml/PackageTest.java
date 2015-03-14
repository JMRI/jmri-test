package jmri.jmrit.display.configurexml;

import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/**
 * PackageTest.java
 *
 * Description:	Tests for the jmrit.display.configurexml package
 *
 * @author	Bob Jacobsen Copyright 2009, 2014
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
        NbTestSuite suite = new NbTestSuite("jmri.jmrit.display.configurexml");   // no tests in this class itself
        suite.addTest(SchemaTest.suite());
        suite.addTest(LoadAndStoreTest.suite());
        return suite;
    }

}
