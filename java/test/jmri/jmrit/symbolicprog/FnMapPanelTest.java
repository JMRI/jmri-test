// VariableValueTest.java
package jmri.jmrit.symbolicprog;

import java.util.List;
import javax.swing.JLabel;
import jmri.progdebugger.ProgDebugger;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdom2.Element;

/**
 * Base for tests of classes inheriting from FnMapPanel abstract class
 *
 * @author	Bob Jacobsen, Copyright 2009
 * @version $Revision$
 */
public class FnMapPanelTest extends NbTestCase {

    public void testCtor() {
        ProgDebugger p = new ProgDebugger();
        VariableTableModel tableModel = new VariableTableModel(
                new JLabel(""),
                new String[]{"Name", "Value"},
                new CvTableModel(new JLabel(""), p),
                new IndexedCvTableModel(new JLabel(""), p)
        );
        List<Integer> varsUsed = null;
        Element model = new Element("model");

        new FnMapPanel(tableModel, varsUsed, model);
    }

    public void testLargeNumbers() {
        ProgDebugger p = new ProgDebugger();
        VariableTableModel tableModel = new VariableTableModel(
                new JLabel(""),
                new String[]{"Name", "Value"},
                new CvTableModel(new JLabel(""), p),
                new IndexedCvTableModel(new JLabel(""), p)
        );
        List<Integer> varsUsed = null;
        Element model = new Element("model");
        model.setAttribute("numFns", "28");

        new FnMapPanel(tableModel, varsUsed, model);
    }

    // from here down is testing infrastructure
    public FnMapPanelTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", FnMapPanelTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests, including others in the package
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(FnMapPanelTest.class);
        return suite;
    }

    static Logger log = LoggerFactory.getLogger(FnMapPanelTest.class.getName());

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
