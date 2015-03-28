// PackageTest.java
package jmri.jmrix.rfid;

import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/**
 * tests for the jmri.jmrix.rfid package
 *
 * @author	Bob Jacobsen
 * @version $Revision$
 */
public class PackageTest extends NbTestCase {

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // a dummy test to avoid JUnit warning
    public void testDemo() {
        assertTrue(true);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {PackageTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        apps.tests.AllTest.initLogging();
        NbTestSuite suite = new NbTestSuite("jmri.jmrix.rfid.PackageTest");

        suite.addTest(jmri.jmrix.rfid.RfidStreamPortControllerTest.suite());
        suite.addTest(jmri.jmrix.rfid.RfidSystemConnectionMemoTest.suite());

        return suite;
    }

}
