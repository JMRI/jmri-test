// CM11Test.java
package jmri.jmrix.powerline.cm11;

import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/**
 * Tests for the jmri.jmrix.powerline.cm11 package.
 *
 * @author Bob Jacobsen Copyright 2003, 2007, 2008
 * @version $Revision$
 */
public class CM11Test extends NbTestCase {

    // from here down is testing infrastructure
    public CM11Test(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {CM11Test.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        apps.tests.AllTest.initLogging();
        NbTestSuite suite = new NbTestSuite("jmri.jmrix.powerline.cm11.CM11Test");
        suite.addTest(SpecificMessageTest.suite());
        suite.addTest(SpecificReplyTest.suite());
        suite.addTest(SpecificTrafficControllerTest.suite());
        return suite;
    }

}
