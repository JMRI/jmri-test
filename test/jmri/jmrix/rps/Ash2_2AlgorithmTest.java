// Ash2_2AlgorithmTest.java

package jmri.jmrix.rps;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.vecmath.Point3d;

/**
 * JUnit tests for the rps.Ash2_2Algorithm class.
 * 
 * This algorithm tends to pick arbitrary solutions with only
 * three sensors, so we test with four and more.
 * 
 * @author	Bob Jacobsen Copyright 2007
 * @version	$Revision: 1.3 $
 */
public class Ash2_2AlgorithmTest extends AbstractAlgorithmTest {
        

    Calculator getAlgorithm(Point3d[] pts, double vs) {
        return new Ash2_2Algorithm(pts, vs);
    }

	// from here down is testing infrastructure

	// Main entry point
	static public void main(String[] args) {
        String[] testCaseName = {Ash2_2AlgorithmTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite(Ash2_2AlgorithmTest.class);
        return suite;
	}

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
}
