// Ash2_2AlgorithmTest.java
package jmri.jmrix.rps;

import javax.vecmath.Point3d;
import org.netbeans.junit.NbTestSuite;

/**
 * JUnit tests for the rps.Ash2_2Algorithm class.
 *
 * This algorithm tends to pick arbitrary solutions with only three sensors, so
 * we test with four and more.
 *
 * @author	Bob Jacobsen Copyright 2007
 * @version	$Revision$
 */
public class Ash2_2AlgorithmTest extends AbstractAlgorithmTest {

    public Ash2_2AlgorithmTest(String name) {
        super(name);
    }

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
    public static NbTestSuite suite() {
        apps.tests.AllTest.initLogging();
        NbTestSuite suite = new NbTestSuite(Ash2_2AlgorithmTest.class);
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
