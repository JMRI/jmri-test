// ConfigXmlTest.java

package jmri.configurexml;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test the jmri.configxml package.
 * @author	Bob Jacobsen
 * @version         $Revision: 1.6 $
 */
public class ConfigXmlTest extends TestCase {

    // from here down is testing infrastructure

    public ConfigXmlTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", ConfigXmlTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests, including others in the package
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.config.ConfigXmlTest");  // no tests in this class itself
        suite.addTest(jmri.configurexml.LoadFileTest.suite());
        suite.addTest(jmri.configurexml.ConfigXmlManagerTest.suite());
        suite.addTest(jmri.configurexml.BlockManagerXmlTest.suite());
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

}
