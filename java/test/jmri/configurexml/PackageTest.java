// PackageTest.java
package jmri.configurexml;

import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/**
 * Test the jmri.configxml package.
 *
 * @author	Bob Jacobsen
 * @version $Revision$
 */
public class PackageTest extends NbTestCase {

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", PackageTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests, including others in the package
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite("jmri.config.ConfigXmlTest");  // no tests in this class itself

        suite.addTest(SchemaTest.suite());
        suite.addTest(LoadAndCheckTest.suite());
        suite.addTest(LoadAndStoreTest.suite());

        suite.addTest(ConfigXmlManagerTest.suite());

        suite.addTest(BlockManagerXmlTest.suite());
        //suite.addTest(OBlockManagerXmlTest.suite());
        suite.addTest(SectionManagerXmlTest.suite());

        suite.addTest(DefaultJavaBeanConfigXMLTest.suite());

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
