package jmri.jmrix.lenz.li101;

import junit.framework.Assert;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LI101AdapterTest.java
 *
 * Description:	tests for the jmri.jmrix.lenz.li101.LI101Adapter class
 *
 * @author	Paul Bender
 * @version $Revision$
 */
public class LI101AdapterTest extends NbTestCase {

    public void testCtor() {
        LI101Adapter a = new LI101Adapter();
        Assert.assertNotNull(a);
    }

    // from here down is testing infrastructure
    public LI101AdapterTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", LI101AdapterTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(LI101AdapterTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

    static Logger log = LoggerFactory.getLogger(LI101AdapterTest.class.getName());

}
