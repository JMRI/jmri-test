// Ash2_0AlgorithmTest.java
package jmri.jmrix.rps;

import javax.vecmath.Point3d;
import org.netbeans.junit.NbTestSuite;

/**
 * JUnit tests for the rps.Ash2_0Algorithm class.
 *
 * This algorithm tends to pick arbitrary solutions with only three sensors, so
 * we test with four and more.
 *
 * The default transmitter location for the 7, 13, 13, 13 readings is (0,0,12)
 *
 * @author	Bob Jacobsen Copyright 2007
 * @version	$Revision$
 */
public class Ash2_0AlgorithmTest extends AbstractAlgorithmTest {

    public Ash2_0AlgorithmTest(String name) {
        super(name);
    }

    Calculator getAlgorithm(Point3d[] pts, double vs) {
        return new Ash2_0Algorithm(pts, vs);
    }

    // from here down is testing infrastructure
    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {Ash2_0AlgorithmTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        apps.tests.AllTest.initLogging();
        NbTestSuite suite = new NbTestSuite(Ash2_0AlgorithmTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }
}
