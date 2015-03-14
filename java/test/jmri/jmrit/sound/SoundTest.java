// SoundTest.java
package jmri.jmrit.sound;

import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/**
 * Invokes complete set of tests in the jmri.jmrit.sound tree
 *
 * @author	Bob Jacobsen Copyright 2001, 2003
 * @version $Revision$
 */
public class SoundTest extends NbTestCase {

    // from here down is testing infrastructure
    public SoundTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", SoundTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite("jmri.jmrit.sound.SoundTest");
        suite.addTest(jmri.jmrit.sound.WavBufferTest.suite());
        suite.addTest(jmri.jmrit.sound.SoundUtilTest.suite());
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
