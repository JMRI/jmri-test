// MeasurementTest.java
package jmri.jmrix.rps;

import junit.framework.Assert;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/**
 * JUnit tests for the rps.Measurement class.
 *
 * @author	Bob Jacobsen Copyright 2006
 * @version	$Revision$
 */
public class MeasurementTest extends NbTestCase {

    public void testCtorAndID() {
        Reading r = new Reading("21", new double[]{0., 0., 0.});
        Measurement m = new Measurement(r);
        Assert.assertEquals("ID ok", "21", m.getID());
    }

    // from here down is testing infrastructure
    public MeasurementTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {MeasurementTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(MeasurementTest.class);
        return suite;
    }

}
