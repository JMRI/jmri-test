package jmri.jmrix.roco;

import junit.framework.Assert;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RocoConnectionTypeListTest.java
 *
 * Description:	tests for the jmri.jmrix.roco.RocoConnectionTypeList class
 *
 * @author	Paul Bender
 * @version $Revision$
 */
public class RocoConnectionTypeListTest extends NbTestCase {

    public void testCtor() {

        RocoConnectionTypeList c = new RocoConnectionTypeList();
        Assert.assertNotNull(c);
    }

    // from here down is testing infrastructure
    public RocoConnectionTypeListTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", RocoConnectionTypeListTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(RocoConnectionTypeListTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

    static Logger log = LoggerFactory.getLogger(RocoConnectionTypeListTest.class.getName());

}
