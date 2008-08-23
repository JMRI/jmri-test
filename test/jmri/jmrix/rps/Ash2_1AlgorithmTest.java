// Ash2_1AlgorithmTest.java

package jmri.jmrix.rps;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.vecmath.Point3d;

/**
 * JUnit tests for the rps.Ash2_1Algorithm class.
 * 
 * This algorithm tends to pick arbitrary solutions with only
 * three sensors, so we test with four and more.
 * 
 * The default transmitter location for the 7, 13, 13, 13 readings
 * is (0,0,12)
 * 
 * @author	Bob Jacobsen Copyright 2007
 * @version	$Revision: 1.2 $
 */
public class Ash2_1AlgorithmTest extends AbstractAlgorithmTest {

    Calculator getAlgorithm(Point3d[] pts, double vs) {
        return new Ash2_1Algorithm(pts, vs);
    }

	// from here down is testing infrastructure

	// Main entry point
	static public void main(String[] args) {
            String[] testCaseName = {Ash2_1AlgorithmTest.class.getName()};
            junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
            TestSuite suite = new TestSuite(Ash2_1AlgorithmTest.class);
            return suite;
	}

}
