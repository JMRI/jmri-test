package jmri.jmrix.lenz.hornbyelite;

import junit.framework.Assert;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EliteAdapterTest.java
 *
 * Description:	tests for the jmri.jmrix.lenz.hornbyelite.EliteAdapter class
 *
 * @author	Paul Bender
 * @version $Revision$
 */
public class EliteAdapterTest extends NbTestCase {

    public void testCtor() {
        EliteAdapter a = new EliteAdapter();
        Assert.assertNotNull(a);
    }

    // from here down is testing infrastructure
    public EliteAdapterTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", EliteAdapterTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(EliteAdapterTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

    static Logger log = LoggerFactory.getLogger(EliteAdapterTest.class.getName());

}
