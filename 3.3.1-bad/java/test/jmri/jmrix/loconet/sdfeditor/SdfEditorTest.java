package jmri.jmrix.loconet.sdfeditor;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.loconet.sdfeditor package.
 * @author	Bob Jacobsen Copyright 2007
 * @version     $Revision$
 */
public class SdfEditorTest extends TestCase {

    // from here down is testing infrastructure

    public SdfEditorTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SdfEditorTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrix.loconet.sdf.SdfTest");  // no tests in this class itself
        suite.addTest(MonitoringLabelTest.suite());
        suite.addTest(EditorPaneTest.suite());
        suite.addTest(EditorFrameTest.suite());
        return suite;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SdfEditorTest.class.getName());

}
