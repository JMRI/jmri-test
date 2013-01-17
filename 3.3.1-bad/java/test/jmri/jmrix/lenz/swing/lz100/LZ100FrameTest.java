package jmri.jmrix.lenz.swing.lz100;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.jmrix.lenz.XNetInterfaceScaffold;
import jmri.jmrix.lenz.LenzCommandStation;
import jmri.jmrix.lenz.XNetSystemConnectionMemo;


/**
 * LZ100FrameTest.java
 *
 * Description:	    tests for the jmri.jmrix.lenz.swing.lz100.LZ100Frame class
 * @author			Paul Bender
 * @version         $Revision$
 */
public class LZ100FrameTest extends TestCase {

    public void testCtor() {

       // infrastructure objects
       XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new LenzCommandStation());


        LZ100Frame f = new LZ100Frame(new XNetSystemConnectionMemo(tc));
        Assert.assertNotNull(f);
    }

	// from here down is testing infrastructure

	public LZ100FrameTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", LZ100FrameTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(LZ100FrameTest.class);
		return suite;
	}

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LZ100FrameTest.class.getName());

}
