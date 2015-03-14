// CM11Test.java
package jmri.jmrix.powerline.insteon2412s;

import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/**
 * Tests for the jmri.jmrix.powerline.insteon2412s package.
 *
 * @author Bob Jacobsen Copyright 2003, 2007, 2008, 2009
 * @version $Revision$
 */
public class Insteon2412sTest extends NbTestCase {

    // from here down is testing infrastructure
    public Insteon2412sTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {Insteon2412sTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        apps.tests.AllTest.initLogging();
        NbTestSuite suite = new NbTestSuite("jmri.jmrix.powerline.insteon2412s.Insteon2412sTest");
        suite.addTest(SpecificMessageTest.suite());
        suite.addTest(SpecificReplyTest.suite());
        suite.addTest(SpecificTrafficControllerTest.suite());
        return suite;
    }

}
