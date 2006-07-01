// LongAddrVariableValueTest.java

package jmri.jmrit.symbolicprog;

import java.util.Vector;

import javax.swing.*;

import com.sun.java.util.collections.ArrayList;
import com.sun.java.util.collections.List;
import jmri.*;
import jmri.progdebugger.*;
import junit.framework.*;

/**
 * LongAddrVariableValueTest.java
 *
 * @todo need a check of the MIXED state model for long address
 * @author	Bob Jacobsen Copyright 2001, 2002
 * @version $Revision: 1.7 $
 */
public class LongAddrVariableValueTest extends VariableValueTest {

    ProgDebugger p = new ProgDebugger();

    // abstract members invoked by tests in parent VariableValueTest class
    VariableValue makeVar(String label, String comment, boolean readOnly,
                          int cvNum, String mask, int minVal, int maxVal,
                          Vector v, JLabel status, String item) {
        // make sure next CV exists
        CvValue cvNext = new CvValue(cvNum+1,p);
        cvNext.setValue(0);
        v.setElementAt(cvNext, cvNum+1);
        return new LongAddrVariableValue(label, comment, readOnly, cvNum, mask, minVal, maxVal, v, status, item);
    }


    void setValue(VariableValue var, String val) {
        ((JTextField)var.getValue()).setText(val);
        ((JTextField)var.getValue()).postActionEvent();
    }

    void setReadOnlyValue(VariableValue var, String val) {
        ((LongAddrVariableValue)var).setValue(Integer.valueOf(val).intValue());
    }

    void checkValue(VariableValue var, String comment, String val) {
        Assert.assertEquals(comment, val, ((JTextField)var.getValue()).getText() );
    }

    void checkReadOnlyValue(VariableValue var, String comment, String val) {
        Assert.assertEquals(comment, val, ((JLabel)var.getValue()).getText() );
    }

    // end of abstract members

    // some of the premade tests don't quite make sense; override them here.

    public void testVariableValueCreate() {}// mask is ignored by LongAddr
    public void testVariableFromCV() {}     // low CV is upper part of address
    public void testVariableValueRead() {}	// due to multi-cv nature of LongAddr
    public void testVariableValueWrite() {} // due to multi-cv nature of LongAddr
    public void testVariableCvWrite() {}    // due to multi-cv nature of LongAddr
    public void testWriteSynch2() {}        // programmer synch is different
    // can we create long address , then manipulate the variable to change the CV?
    public void testLongAddressCreate() {
        Vector v = createCvVector();
        CvValue cv17 = new CvValue(17, p);
        CvValue cv18 = new CvValue(18, p);
        cv17.setValue(2);
        cv18.setValue(3);
        v.setElementAt(cv17, 17);
        v.setElementAt(cv18, 18);
        // create a variable pointed at CV 17&18, check name
        LongAddrVariableValue var = new LongAddrVariableValue("label", "comment", false, 17, "VVVVVVVV", 0, 255, v, null, null);
        Assert.assertTrue(var.label() == "label");
        // pretend you've edited the value, check its in same object
        ((JTextField)var.getValue()).setText("4797");
        Assert.assertTrue( ((JTextField)var.getValue()).getText().equals("4797") );
        // manually notify
        var.actionPerformed(new java.awt.event.ActionEvent(var, 0, ""));
        // see if the CV was updated
        Assert.assertTrue(cv17.getValue() == 210);
        Assert.assertTrue(cv18.getValue() == 189);
    }

    // can we change both CVs and see the result in the Variable?
    public void testLongAddressFromCV() {
        Vector v = createCvVector();
        CvValue cv17 = new CvValue(17, p);
        CvValue cv18 = new CvValue(18, p);
        cv17.setValue(2);
        cv18.setValue(3);
        v.setElementAt(cv17, 17);
        v.setElementAt(cv18, 18);
        // create a variable pointed at CV 17 & 18
        LongAddrVariableValue var = new LongAddrVariableValue("name", "comment", false, 17, "VVVVVVVV", 0, 255, v, null, null);
        ((JTextField)var.getValue()).setText("1029");
        var.actionPerformed(new java.awt.event.ActionEvent(var, 0, ""));

        // change the CV, expect to see a change in the variable value
        cv17.setValue(210);
        Assert.assertTrue(cv17.getValue() == 210);
        cv18.setValue(189);
        Assert.assertTrue( ((JTextField)var.getValue()).getText().equals("4797") );
        Assert.assertTrue(cv18.getValue() == 189);
    }

    List evtList = null;  // holds a list of ParameterChange events

    // check a long address read operation
    public void testLongAddressRead() {
        log.debug("testLongAddressRead starts");
        // initialize the system

        Vector v = createCvVector();
        CvValue cv17 = new CvValue(17, p);
        CvValue cv18 = new CvValue(18, p);
        v.setElementAt(cv17, 17);
        v.setElementAt(cv18, 18);

        LongAddrVariableValue var = new LongAddrVariableValue("name", "comment", false, 17, "XXVVVVXX", 0, 255, v, null, null);
        // register a listener for parameter changes
        java.beans.PropertyChangeListener listen = new java.beans.PropertyChangeListener() {
                public void propertyChange(java.beans.PropertyChangeEvent e) {
                    evtList.add(e);
                    if (e.getPropertyName().equals("Busy") && ((Boolean)e.getNewValue()).equals(Boolean.FALSE))
                        log.debug("Busy false seen in test");
                }
            };
        evtList = new ArrayList();
        var.addPropertyChangeListener(listen);

        // set to specific value
        ((JTextField)var.getValue()).setText("5");
        var.actionPerformed(new java.awt.event.ActionEvent(var, 0, ""));

        var.read();
        // wait for reply (normally, done by callback; will check that later)
        int i = 0;
        while ( var.isBusy() && i++ < 100 )  {
            try {
                Thread.sleep(100);
            } catch (Exception e) {
            }
        }
        if (log.isDebugEnabled()) log.debug("past loop, i="+i+" value="+((JTextField)var.getValue()).getText()+" state="+var.getState());
        if (i==0) log.warn("testLongAddressRead saw an immediate return from isBusy");
        Assert.assertTrue("wait satisfied ", i<100);

        int nBusyFalse = 0;
        for (int k = 0; k < evtList.size(); k++) {
            java.beans.PropertyChangeEvent e = (java.beans.PropertyChangeEvent) evtList.get(k);
            // System.out.println("name: "+e.getPropertyName()+" new value: "+e.getNewValue());
            if (e.getPropertyName().equals("Busy") && ((Boolean)e.getNewValue()).equals(Boolean.FALSE))
                nBusyFalse++;
        }
        Assert.assertEquals("only one Busy -> false transition ", 1, nBusyFalse);

        Assert.assertEquals("text value ", "15227", ((JTextField)var.getValue()).getText() );  // 15227 = (1230x3f)*256+123
        Assert.assertEquals("Var state", AbstractValue.READ, var.getState() );
        Assert.assertEquals("CV 17 value ", 251, cv17.getValue());  // 123 with 128 bit set
        Assert.assertEquals("CV 18 value ", 123, cv18.getValue());
    }

    // check a long address write operation
    public void testLongAddressWrite() {
        // initialize the system

        Vector v = createCvVector();
        CvValue cv17 = new CvValue(17, p);
        CvValue cv18 = new CvValue(18, p);
        v.setElementAt(cv17, 17);
        v.setElementAt(cv18, 18);

        LongAddrVariableValue var = new LongAddrVariableValue("name", "comment", false, 17, "XXVVVVXX", 0, 255, v, null, null);
        ((JTextField)var.getValue()).setText("4797");
        var.actionPerformed(new java.awt.event.ActionEvent(var, 0, ""));

        var.write();
        // wait for reply (normally, done by callback; will check that later)
        int i = 0;
        while ( var.isBusy() && i++ < 100  )  {
            try {
                Thread.sleep(10);
            } catch (Exception e) {
            }
        }
        if (log.isDebugEnabled()) log.debug("past loop, i="+i+" value="+((JTextField)var.getValue()).getText()
                                            +" state="+var.getState()
                                            +" last write: "+p.lastWrite());
        if (i==0) log.warn("testLongAddressWrite saw an immediate return from isBusy");

        Assert.assertTrue("wait satisfied ", i<100);

        Assert.assertEquals("CV 17 value ", 210, cv17.getValue());
        Assert.assertEquals("CV 18 value ", 189, cv18.getValue());
        Assert.assertTrue( ((JTextField)var.getValue()).getText().equals("4797") );
        Assert.assertEquals("Var state", AbstractValue.STORED, var.getState() );
        Assert.assertTrue(p.lastWrite() == 189);
        // how do you check separation of the two writes?  State model?
    }

    protected Vector createCvVector() {
        Vector v = new Vector(512);
        for (int i=0; i < 512; i++) v.addElement(null);
        return v;
    }

    // from here down is testing infrastructure

    public  LongAddrVariableValueTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = { LongAddrVariableValueTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite( LongAddrVariableValueTest.class);
        return suite;
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance( LongAddrVariableValueTest.class.getName());

}

