package jmri.jmrix.xpa;

import junit.framework.Assert;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * XpaMessageTest.java
 *
 * Description:	tests for the jmri.jmrix.xpa.XpaMessage class
 *
 * @author	Paul Bender
 * @version $Revision$
 */
public class XpaMessageTest extends NbTestCase {

    public void testCtor() {
        XpaMessage m = new XpaMessage(3);
        Assert.assertEquals("length", 3, m.getNumDataElements());
    }

    // from here down is testing infrastructure
    public XpaMessageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", XpaMessageTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(XpaMessageTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

    static Logger log = LoggerFactory.getLogger(XpaMessageTest.class.getName());

}
