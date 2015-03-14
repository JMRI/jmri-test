/**
 * PackageTest.java
 *
 * Description:	tests for the jmri.jmrit.catalog package
 *
 * @author	Bob Jacobsen 2009
 * @version $Revision$
 */
package jmri.jmrit.catalog;

import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

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
        NbTestSuite suite = new NbTestSuite("jmri.jmrit.catalog");   // no tests in this class itself
        suite.addTest(CatalogTreeFSTest.suite());
        suite.addTest(CatalogTreeIndexTest.suite());
        return suite;
    }

}
