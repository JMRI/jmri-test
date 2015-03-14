// NodeConfigToolActionTest.java
package jmri.jmrix.can.cbus.swing.nodeconfig;

import jmri.jmrix.can.TestTrafficController;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/**
 * Tests for the jmri.jmrix.can.cbus.swing.nodeconfig package.
 *
 * @author Bob Jacobsen Copyright 2008
 * @version $Revision$
 */
public class NodeConfigToolActionTest extends NbTestCase {

    // from here down is testing infrastructure
    public NodeConfigToolActionTest(String s) {
        super(s);
    }

    public void testAction() {
        // load dummy TrafficController
        new TestTrafficController();
        new NodeConfigToolAction().actionPerformed(null);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", NodeConfigToolActionTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        apps.tests.AllTest.initLogging();
        NbTestSuite suite = new NbTestSuite(NodeConfigToolActionTest.class);
        return suite;
    }

}
