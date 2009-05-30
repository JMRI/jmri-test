// VariableTableModelTest.java

package jmri.jmrit.symbolicprog;

import javax.swing.*;

import jmri.progdebugger.*;
import junit.framework.*;
import org.jdom.*;

import java.util.Vector;

/**
 * VariableTableModelTest.java
 *
 * Description:
 * @author			Bob Jacobsen Copyright 2005
 * @version    $Revision: 1.12 $
 */
public class VariableTableModelTest extends TestCase {

    ProgDebugger p = new ProgDebugger();

    public void testStart() {
        // create one with some dummy arguments
        new VariableTableModel(
                               new JLabel(""),
                               new String[] {"Name", "Value"},
                               new CvTableModel(new JLabel(""), p),
                               new IndexedCvTableModel(new JLabel(""), p)
                               );
    }


    // Can we create a table?
    public void testVarTableCreate() {
        String[] args = {"CV", "Name"};
        VariableTableModel t = new VariableTableModel(null, args, null, null);  // CvTableModel ref is null for this test
    }

    // Check column count member fn, column names
    public void testVarTableColumnCount() {
        String[] args = {"CV", "Name"};
        VariableTableModel t = new VariableTableModel(null, args, null, null);
        Assert.assertTrue(t.getColumnCount() == 2);
        Assert.assertTrue(t.getColumnName(1) == "Name");
    }

    // Check loading two columns, three rows
    public void testVarTableLoad_2_3() {
        String[] args = {"CV", "Name"};
        VariableTableModel t = new VariableTableModel(null, args, new CvTableModel(null, p), null);

        // create a JDOM tree with just some elements
        Element root = new Element("decoder-config");
        Document doc = new Document(root);
        doc.setDocType(new DocType("decoder-config","decoder-config.dtd"));

        // add some elements
        Element el0, el1;
        root.addContent(new Element("decoder")		// the sites information here lists all relevant
            .addContent(new Element("variables")
                .addContent(el0 = new Element("variable")
                    .setAttribute("CV","1")
                    .setAttribute("label","one")
                    .setAttribute("mask","VVVVVVVV")
                    .setAttribute("item", "really two")
                    .setAttribute("readOnly","no")
                    .addContent( new Element("decVal")
                        .setAttribute("max","31")
                        .setAttribute("min","1")
                        )
                    )
                .addContent(el1 = new Element("variable")
                    .setAttribute("CV","4")
                    .setAttribute("readOnly","no")
                    .setAttribute("mask","XXXVVVVX")
                    .setAttribute("label","two")
                    .addContent( new Element("decVal")
                        .setAttribute("max","31")
                        .setAttribute("min","1")
                        )
                    )
                )	// variables element
            ) // decoder element
            ; // end of adding contents

        // print JDOM tree, to check
        //OutputStream o = System.out;
        //XMLOutputter fmt = new XMLOutputter();
        //fmt.setNewlines(true);   // pretty printing
        //fmt.setIndent(true);
        //try {
        //	 fmt.output(doc, o);
        //} catch (Exception e) { System.out.println("error writing XML: "+e);}

        // and test reading this
        t.setRow(0, el0);
        Assert.assertTrue(t.getValueAt(0,0).equals("1"));
        Assert.assertTrue(t.getValueAt(0,1).equals("one"));

        // check that the variable names were set right
        Assert.assertEquals("check loaded label ", "one", t.getLabel(0));
        Assert.assertEquals("check loaded item ", "really two", t.getItem(0));

        t.setRow(1, el1);
        Assert.assertTrue(t.getValueAt(1,0).equals("4"));
        Assert.assertTrue(t.getValueAt(1,1).equals("two"));

        Assert.assertTrue(t.getRowCount() == 2);

        // check finding
        Assert.assertEquals("find variable two ",1, t.findVarIndex("two"));
        Assert.assertEquals("find nonexistant variable ",-1, t.findVarIndex("not there, eh?"));

    }

    // Check creating a longaddr type, walk through its programming
    public void testVarTableLoadLongAddr() {
        String[] args = {"CV", "Name"};
        VariableTableModel t = new VariableTableModel(null, args, new CvTableModel(null, p), null);

        // create a JDOM tree with just some elements
        Element root = new Element("decoder-config");
        Document doc = new Document(root);
        doc.setDocType(new DocType("decoder-config","decoder-config.dtd"));

        // add some elements
        Element el0, el1;
        root.addContent(new Element("decoder")		// the sites information here lists all relevant
            .addContent(new Element("variables")
                .addContent(el0 = new Element("variable")
                    .setAttribute("CV","17")
                    .setAttribute("readOnly","no")
                    .setAttribute("mask","VVVVVVVV")
                    .setAttribute("label","long")
                    .addContent( new Element("longAddressVal")
                        )
                    )
                )	// variables element
            ) // decoder element
            ; // end of adding contents

        // print JDOM tree, to check
        //OutputStream o = System.out;
        //XMLOutputter fmt = new XMLOutputter();
        //fmt.setNewlines(true);   // pretty printing
        //fmt.setIndent(true);
        //try {
        //	 fmt.output(doc, o);
        //} catch (Exception e) { System.out.println("error writing XML: "+e);}

        // and test reading this
        t.setRow(0, el0);
        Assert.assertTrue(t.getValueAt(0,0).equals("17"));
        Assert.assertTrue(t.getValueAt(0,1).equals("long"));

        Assert.assertTrue(t.getRowCount() == 1);

    }

    // Check creating a speed table, then finding it by name
    public void testVarSpeedTable() {
        String[] args = {"CV", "Name"};
        VariableTableModel t = new VariableTableModel(null, args, new CvTableModel(null, p), null);

        // create a JDOM tree with just some elements
        Element root = new Element("decoder-config");
        Document doc = new Document(root);
        doc.setDocType(new DocType("decoder-config","decoder-config.dtd"));

        // add some elements
        Element el0;
        root.addContent(new Element("decoder")		// the sites information here lists all relevant
            .addContent(new Element("variables")
                .addContent(el0 = new Element("variable")
                    .setAttribute("CV","67")
                    .setAttribute("label","Speed Table")
                    .setAttribute("mask","VVVVVVVV")
                    .setAttribute("readOnly", "")
                    .addContent( new Element("speedTableVal")
                        )
                    )
                )	// variables element
            ) // decoder element
            ; // end of adding contents

        // and test reading this
        t.setRow(0, el0);

        // check finding
        Assert.assertEquals("length of variable list ", 1, t.getRowCount());
        Assert.assertEquals("name of 1st variable ", "Speed Table", t.getLabel(0));
        Assert.assertEquals("find Speed Table ",0, t.findVarIndex("Speed Table"));
        Assert.assertEquals("find nonexistant variable ",-1, t.findVarIndex("not there, eh?"));

    }

    // Check creating bogus XML (unknown variable type)
    public void testVarTableLoadBogus() {
        String[] args = {"CV", "Name"};
        VariableTableModel t = new VariableTableModel(null, args, new CvTableModel(null, p), null){
            void reportBogus(){}
        };

        // create a JDOM tree with just some elements
        Element root = new Element("decoder-config");
        Document doc = new Document(root);
        doc.setDocType(new DocType("decoder-config","decoder-config.dtd"));

        // add some elements
        Element el0, el1;
        root.addContent(new Element("decoder")		// the sites information here lists all relevant
            .addContent(new Element("variables")
                .addContent(el0 = new Element("variable")
                    .setAttribute("CV","17")
                    .setAttribute("mask","VVVVVVVV")
                    .setAttribute("readOnly","no")
                    .setAttribute("label","long")
                    .addContent( new Element("bogusVal")
                        )
                    )
                )	// variables element
            ) // decoder element
            ; // end of adding contents

        // print JDOM tree, to check
        //OutputStream o = System.out;
        //XMLOutputter fmt = new XMLOutputter();
        //fmt.setNewlines(true);   // pretty printing
        //fmt.setIndent(true);
        //try {
        //	 fmt.output(doc, o);
        //} catch (Exception e) { System.out.println("error writing XML: "+e);}

        // and test reading this
        t.setRow(0, el0);
        Assert.assertTrue(t.getRowCount() == 0);

    }

    
    // from here down is testing infrastructure

    public VariableTableModelTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", VariableTableModelTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(VariableTableModelTest.class);
        return suite;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(VariableTableModelTest.class.getName());

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

}
