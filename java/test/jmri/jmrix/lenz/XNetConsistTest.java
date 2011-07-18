package jmri.jmrix.lenz;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * XNetConsistTest.java
 *
 * Description:	    tests for the jmri.jmrix.lenz.XNetConsist class
 * @author			Paul Bender
 * @version         $Revision$
 */
public class XNetConsistTest extends TestCase {

    public void testCtor() {
       // infrastructure objects
       XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new LenzCommandStation());

        XNetConsist c = new XNetConsist(5,tc);
        Assert.assertNotNull(c);
    }

	// from here down is testing infrastructure

	public XNetConsistTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", XNetConsistTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(XNetConsistTest.class);
		return suite;
	}

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(XNetConsistTest.class.getName());

}
