package jmri.jmrix.loconet.sdfeditor;

import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.loconet.sdfeditor package.
 *
 * @author	Bob Jacobsen Copyright 2007
 * @version $Revision$
 */
public class SdfEditorTest extends NbTestCase {

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
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite("jmri.jmrix.loconet.sdf.SdfTest");  // no tests in this class itself
        suite.addTest(MonitoringLabelTest.suite());
        suite.addTest(EditorPaneTest.suite());
        suite.addTest(EditorFrameTest.suite());
        return suite;
    }

    static Logger log = LoggerFactory.getLogger(SdfEditorTest.class.getName());

}
