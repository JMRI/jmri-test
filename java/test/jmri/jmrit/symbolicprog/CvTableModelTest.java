// CvTableModelTest.java
package jmri.jmrit.symbolicprog;

import javax.swing.JLabel;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/**
 * @author	Bob Jacobsen
 * @version	$Revision$
 */
public class CvTableModelTest extends NbTestCase {

    public void testStart() {
        new CvTableModel(new JLabel(), null);
    }

    // from here down is testing infrastructure
    public CvTableModelTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {CvTableModelTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(CvTableModelTest.class);
        return suite;
    }

    // static Logger log = LoggerFactory.getLogger(CvTableModelTest.class.getName());
}
