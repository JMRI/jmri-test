// EditableListTest.java
package jmri.swing;

import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.swing.EditableList class.
 *
 * @author	Bob Jacobsen Copyright 2014
 * @version	$Revision$
 */
public class EditableListTest extends NbTestCase {

    public void testCtor() {
        new EditableList<String>();
        new EditableList<Integer>();
        new EditableList<Object>();
    }

    public void testCtorWithMode() {
        new EditableList<String>(new DefaultEditableListModel<String>());
        new EditableList<Integer>(new DefaultEditableListModel<Integer>());
        new EditableList<Object>(new DefaultEditableListModel<Object>());
    }

    // from here down is testing infrastructure
    public EditableListTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {EditableListTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(EditableListTest.class);
        return suite;
    }

    static Logger log = LoggerFactory.getLogger(EditableListTest.class.getName());

}
