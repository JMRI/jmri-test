// VariableValueTest.java

package jmri.jmrit.symbolicprog;

import javax.swing.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.List;
import org.jdom.Element;

import jmri.progdebugger.*;

/**
 * Base for tests of classes inheriting from FnMapPanel abstract class
 * @author	Bob Jacobsen, Copyright 2009
 * @version     $Revision: 1.3 $
 */
public class FnMapPanelTest extends TestCase {

    public void testCtor() {
        ProgDebugger p = new ProgDebugger();
        VariableTableModel tableModel = new VariableTableModel(
                               new JLabel(""),
                               new String[] {"Name", "Value"},
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
                               new String[] {"Name", "Value"},
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
    public static Test suite() {
        TestSuite suite = new TestSuite(FnMapPanelTest.class);
        return suite;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(FnMapPanelTest.class.getName());

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

}

