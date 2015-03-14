// AlgorithmsTest.java
package jmri.jmrix.rps;

import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/**
 * Test all the RPS algorithms.
 *
 * Separated from RpsTest to make it easy to run just the algorithms, not all
 * the package tests.
 *
 * @author Bob Jacobsen Copyright 2008
 * @version $Revision$
 */
public class AlgorithmsTest extends NbTestCase {

    // from here down is testing infrastructure
    public AlgorithmsTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {AlgorithmsTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        apps.tests.AllTest.initLogging();
        NbTestSuite suite = new NbTestSuite("jmri.jmrix.rps.AlgorithmsTest");

        suite.addTest(InitialAlgorithmTest.suite());

        // suite.addTest(Ash1_0AlgorithmTest.suite());
        // suite.addTest(Ash1_1AlgorithmTest.suite());
        suite.addTest(Ash2_0AlgorithmTest.suite());
        suite.addTest(Ash2_1AlgorithmTest.suite());
        suite.addTest(Ash2_2AlgorithmTest.suite());

        // suite.addTest(Analytic_AAlgorithmTest.suite());
        return suite;
    }

}
