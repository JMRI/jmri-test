// ValidateConfigFilesTest.java
package apps;

import junit.framework.Test;
import org.netbeans.junit.NbTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test upper level loading of config files
 *
 * @author Bob Jacobsen Copyright 2012
 * @since 2.5.5
 * @version $Revision$
 */
public class ValidateConfigFilesTest extends jmri.util.swing.GuiUtilBaseTest {

    // from here down is testing infrastructure
    // Note setup() and teardown are provided from base class, and 
    // need to be invoked if you add methods here
    public ValidateConfigFilesTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", ValidateConfigFilesTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite("apps.ValidateConfigFilesTest");
        doDirectory(suite, "xml/config");
        return suite;
    }

    static Logger log = LoggerFactory.getLogger(ValidateConfigFilesTest.class.getName());

}
