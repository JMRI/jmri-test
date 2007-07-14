// EditorPaneTest.java
package jmri.jmrix.loconet.sdfeditor;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.jmrix.loconet.sdf.SdfBuffer;


/**
 * Tests for the jmri.jmrix.loconet.sdfeditor.EditorPane class.
 *
 * @author			Bob Jacobsen  Copyright 2007
 * @version         $Revision: 1.2 $
 */
public class EditorPaneTest extends TestCase {

    public void testShowPane() throws java.io.IOException {
        SdfBuffer buff = new SdfBuffer("java/test/jmri/jmrix/loconet/sdf/test2.sdf");
        new EditorFrame(buff).setVisible(true);
    }

    // from here down is testing infrastructure

    public EditorPaneTest(String s) {
    	super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", EditorPaneTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(EditorPaneTest.class);
        return suite;
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(EditorPaneTest.class.getName());

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

}
