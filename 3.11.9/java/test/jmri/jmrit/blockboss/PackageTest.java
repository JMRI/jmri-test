// PackageTest.java
package jmri.jmrit.blockboss;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmrit.blockboss package
 *
 * @author	Bob Jacobsen
 * @version $Revision$
 */
public class PackageTest extends TestCase {

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
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrit.blockboss.BlockBossTest"); // no tests in class itself
        suite.addTest(jmri.jmrit.blockboss.BlockBossLogicTest.suite());
        return suite;
    }

}
