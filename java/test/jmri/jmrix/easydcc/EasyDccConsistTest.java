/**
 * EasyDccConsistTest.java
 *
 * Description:	tests for the jmri.jmrix.nce.EasyDccConsist class
 *
 * @author	Paul Bender
 * @version
 */
package jmri.jmrix.easydcc;

import junit.framework.Assert;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EasyDccConsistTest extends NbTestCase {

    public void testCtor() {
        EasyDccConsist m = new EasyDccConsist(5);
        Assert.assertNotNull(m);
    }

    // from here down is testing infrastructure
    public EasyDccConsistTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {EasyDccConsistTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(EasyDccConsistTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

    static Logger log = LoggerFactory.getLogger(EasyDccConsistTest.class.getName());

}
