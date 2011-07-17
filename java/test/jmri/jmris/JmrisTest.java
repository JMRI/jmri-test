//JmrisTest.java

package jmri.jmris;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Set of tests for the jmri.jmris package
 * @author	Paul Bender  Copyright 2010
 * @version         $Revision: 1.1 $
 */
public class JmrisTest extends TestCase {

	// from here down is testing infrastructure

	public JmrisTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {JmrisTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite("jmri.jmris.JmrisTest");

		suite.addTest(jmri.jmris.srcp.SRCPTest.suite());
                return suite;
        }

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

}
