// PowerPanelTest.java
package jmri.jmrit.powerpanel;

import java.util.ResourceBundle;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/**
 * Tests for the jmrit.PowerPanel package
 *
 * @author	Bob Jacobsen
 * @version $Revision$
 */
public class PowerPanelTest extends NbTestCase {

    static ResourceBundle res = ResourceBundle.getBundle("jmri.jmrit.powerpanel.PowerPanelBundle");

    // from here down is testing infrastructure
    public PowerPanelTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {PowerPanelTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite("jmri.jmrit.powerpanel.PowerPanelTest"); // no tests in class itself
        suite.addTest(jmri.jmrit.powerpanel.PowerPaneTest.suite());
        return suite;
    }

}
