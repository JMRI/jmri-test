package jmri.jmrix.lenz.hornbyelite;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EliteConnectionTypeListTest.java
 *
 * Description:	tests for the jmri.jmrix.lenz.EliteConnectionTypeList class
 *
 * @author	Paul Bender
 * @version $Revision$
 */
public class EliteConnectionTypeListTest extends TestCase {

    public void testCtor() {

        EliteConnectionTypeList c = new EliteConnectionTypeList();
        Assert.assertNotNull(c);
    }

    // from here down is testing infrastructure
    public EliteConnectionTypeListTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", EliteConnectionTypeListTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(EliteConnectionTypeListTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

    static Logger log = LoggerFactory.getLogger(EliteConnectionTypeListTest.class.getName());

}
