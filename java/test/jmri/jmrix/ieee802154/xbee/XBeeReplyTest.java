package jmri.jmrix.ieee802154.xbee;

import junit.framework.Assert;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * XBeeReplyTest.java
 *
 * Description:	tests for the jmri.jmrix.ieee802154.xbee.XBeeReply class
 *
 * @author	Paul Bender
 * @version $Revision$
 */
public class XBeeReplyTest extends NbTestCase {

    XBeeTrafficController qt = new XBeeTrafficController() {
        public void setInstance() {
        }
    };

    public void testCtor() {
        XBeeReply m = new XBeeReply(qt);
        Assert.assertNotNull("exists", m);
    }

    // from here down is testing infrastructure
    public XBeeReplyTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", XBeeReplyTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(XBeeReplyTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

    static Logger log = LoggerFactory.getLogger(XBeeReplyTest.class.getName());

}
