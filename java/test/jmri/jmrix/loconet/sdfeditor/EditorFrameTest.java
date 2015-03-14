// EditorFrameTest.java
package jmri.jmrix.loconet.sdfeditor;

import jmri.jmrix.loconet.sdf.SdfBuffer;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.loconet.sdfeditor.EditorPane class.
 *
 * @author	Bob Jacobsen Copyright 2007
 * @version $Revision$
 */
public class EditorFrameTest extends NbTestCase {

    public void testShowPane() throws java.io.IOException {
        SdfBuffer buff = new SdfBuffer("java/test/jmri/jmrix/loconet/sdf/test2.sdf");
        new EditorFrame(buff).setVisible(true);
    }

    // from here down is testing infrastructure
    public EditorFrameTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {EditorPaneTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(EditorFrameTest.class);
        return suite;
    }

    static Logger log = LoggerFactory.getLogger(EditorFrameTest.class.getName());

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
