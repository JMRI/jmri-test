// CompositeVariableValueTest.java

package jmri.jmrit.symbolicprog;

import org.apache.log4j.Logger;
import jmri.progdebugger.ProgDebugger;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JComboBox;

import java.util.*;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test CompositeVariableValue class.
 *
 * @author	Bob Jacobsen Copyright 2006
 * @version $Revision$
 */
public class CompositeVariableValueTest extends VariableValueTest {

    // abstract members invoked by tests in parent VariableValueTest class
    VariableValue makeVar(String label, String comment, String cvName,
                          boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
                          int cvNum, String mask, int minVal, int maxVal,
                          Vector<CvValue> v, JLabel status, String item) {
        // make sure next CV exists
        CvValue cvNext = new CvValue(cvNum+1,p);
        cvNext.setValue(0);
        v.setElementAt(cvNext, cvNum+1);
        return new CompositeVariableValue(label, comment, "", readOnly, infoOnly, writeOnly, opsOnly, cvNum, mask, minVal, maxVal, v, status, item);
    }


    void setValue(VariableValue var, String val) {
        ((JTextField)var.getCommonRep()).setText(val);
        ((JTextField)var.getCommonRep()).postActionEvent();
    }

    void setReadOnlyValue(VariableValue var, String val) {
        ((CompositeVariableValue)var).setValue(Integer.valueOf(val).intValue());
    }

    void checkValue(VariableValue var, String comment, String val) {
        Assert.assertEquals(comment, val, var.getCommonRep().toString() );
    }

    void checkReadOnlyValue(VariableValue var, String comment, String val) {
        Assert.assertEquals(comment, val, var.getCommonRep().toString() );
    }

    // end of abstract members

    // some of the premade tests don't quite make sense; override them here.
    // (This is removing 13 of 18 tests, which seems rather much)
    public void testVariableValueCreate() {}// mask is ignored 
    public void testVariableSynch() {}     // low CV is upper part of address
    public void testVariableReadOnly() {}     // low CV is upper part of address
    public void testVariableFromCV() {}     // low CV is upper part of address
    public void testVariableValueRead() {}	// due to multi-cv nature
    public void testVariableValueStates() {}	// due to multi-cv nature
    public void testVariableValueStateColor() {}	// due to multi-cv nature
    public void testVariableRepStateColor() {}	// due to multi-cv nature
    public void testVariableValueRepStateColor() {}	// due to multi-cv nature
    public void testVariableVarChangeColorRep() {}	// due to multi-cv nature
    public void testVariableValueWrite() {} // due to multi-cv nature
    public void testVariableCvWrite() {}    // due to multi-cv nature
    public void testWriteSynch2() {}        // programmer synch is different

    // rest of tests are new, for just this type of variable

    // can we create three variables, then manipulate the composite variable to change them?
    public void testCompositeCreateAndSet() {
        
        CompositeVariableValue testVar = createTestVar();
        
        // set value
        ((JComboBox)testVar.getCommonRep()).setSelectedIndex(1);
        
        // see if the variables were updated
        Assert.assertEquals("var17 value when set to second", 21, var17.getIntValue());
        Assert.assertEquals("var18 value when set to second", 22, var18.getIntValue());
        Assert.assertEquals("var19 value when set to second", 23, var19.getIntValue());

        // see if the CVs were updated
        Assert.assertEquals("cv17 value when set to second", 21, cv17.getValue());
        Assert.assertEquals("cv18 value when set to second", 22, cv18.getValue());
        Assert.assertEquals("cv19 value when set to second", 23, cv19.getValue());
    }

    // can we change the CVs and see the result in the Variable?
    public void testValueFromCV() {

        CompositeVariableValue testVar = createTestVar();
        
        // set value
        ((JComboBox)testVar.getCommonRep()).setSelectedIndex(1);

        // set the CVs
        cv17.setValue(11);
        cv18.setValue(12);
        cv19.setValue(13);
        
        // check for change
        Assert.assertEquals("composite index when set to first via CV", 0, ((JComboBox)testVar.getCommonRep()).getSelectedIndex());
    }

    List<java.beans.PropertyChangeEvent> evtList = null;  // holds a list of ParameterChange events

    public void testRead() {

        CompositeVariableValue testVar = createTestVar();
        
        // set initial value
        ((JComboBox)testVar.getCommonRep()).setSelectedIndex(1);

        // register a listener for parameter changes
        java.beans.PropertyChangeListener listen = new java.beans.PropertyChangeListener() {
                public void propertyChange(java.beans.PropertyChangeEvent e) {
                    evtList.add(e);
                    if (e.getPropertyName().equals("Busy") && ((Boolean)e.getNewValue()).equals(Boolean.FALSE))
                        log.debug("=============== Busy false seen in test scaffold =================");
                }
            };
        evtList = new ArrayList<java.beans.PropertyChangeEvent>();
        testVar.addPropertyChangeListener(listen);

        // execute the test read
        log.debug("============ execute test read ===========");
        testVar.setToRead(true);
        testVar.readAll();
        log.debug("============ end test read ===============");
        
        // wait for reply (normally, done by callback; will check that later)
        int i = 0;
        log.debug("============== enter loop  =================");
        while ( testVar.isBusy() && i++ < 10 )  {
            try {
                Thread.sleep(100);
            } catch (Exception e) {
            }
        }
        log.debug("============== out of loop  ===================");
        Assert.assertTrue("wait satisfied ", i<10);

        int nBusyFalse = 0;
        for (int k = 0; k < evtList.size(); k++) {
            java.beans.PropertyChangeEvent e = evtList.get(k);
            if (e.getPropertyName().equals("Busy") && ((Boolean)e.getNewValue()).equals(Boolean.FALSE))
                nBusyFalse++;
        }
        log.debug("checks");
        Assert.assertEquals("exactly one Busy -> false transition ", 1, nBusyFalse);

        Assert.assertEquals("CV 17 value ", 123, cv17.getValue());
        Assert.assertEquals("CV 18 value ", 123, cv18.getValue());
        Assert.assertEquals("CV 19 value ", 123, cv19.getValue());
        Assert.assertEquals("var value after read",2, ((JComboBox)testVar.getCommonRep()).getSelectedIndex() ); 
        Assert.assertEquals("Var state", AbstractValue.READ, testVar.getState() );
        log.debug("end testRead");
    }


    public void testWrite() {

        CompositeVariableValue testVar = createTestVar();
        
        // set initial value
        ((JComboBox)testVar.getCommonRep()).setSelectedIndex(1);

        // register a listener for parameter changes
        java.beans.PropertyChangeListener listen = new java.beans.PropertyChangeListener() {
                public void propertyChange(java.beans.PropertyChangeEvent e) {
                    evtList.add(e);
                    if (e.getPropertyName().equals("Busy") && ((Boolean)e.getNewValue()).equals(Boolean.FALSE))
                        log.debug("Busy false seen in test");
                }
            };
        evtList = new ArrayList<java.beans.PropertyChangeEvent>();
        testVar.addPropertyChangeListener(listen);

        testVar.setToWrite(true);
        testVar.writeAll();
        // wait for reply (normally, done by callback; will check that later)
        int i = 0;
        while ( testVar.isBusy() && i++ < 100 )  {
            try {
                Thread.sleep(100);
            } catch (Exception e) {
            }
        }
        Assert.assertTrue("wait satisfied ", i<100);

        int nBusyFalse = 0;
        for (int k = 0; k < evtList.size(); k++) {
            java.beans.PropertyChangeEvent e = evtList.get(k);
            if (e.getPropertyName().equals("Busy") && ((Boolean)e.getNewValue()).equals(Boolean.FALSE))
                nBusyFalse++;
        }
        Assert.assertEquals("only one Busy -> false transition ", 1, nBusyFalse);

        Assert.assertEquals("value after write",1, ((JComboBox)testVar.getCommonRep()).getSelectedIndex() ); 
        Assert.assertEquals("Var state", AbstractValue.STORED, testVar.getState() );

        Assert.assertEquals("CV 17 value ", 21, cv17.getValue());
        Assert.assertEquals("CV 18 value ", 22, cv18.getValue());
        Assert.assertEquals("CV 19 value ", 23, cv19.getValue());
        
    }
    
    public void testIsChanged() {

        CompositeVariableValue testVar = createTestVar();
        
        // initially, nobody changed
        Assert.assertEquals("main not changed initially", false, testVar.isChanged());
        Assert.assertEquals("Var 17 not changed initially ", false, var17.isChanged());

        // set value value
        ((JComboBox)testVar.getCommonRep()).setSelectedIndex(1);

        // now changed, check
        Assert.assertEquals("Var 17 changed ", true, var17.isChanged());
        Assert.assertEquals("main changed", true, testVar.isChanged());
        
    }
    // variables for checking the results of manipulating a test CompositeVariableValue
    CvValue cv17;
    CvValue cv18;
    CvValue cv19;
    
    DecVariableValue var17;
    DecVariableValue var18;
    DecVariableValue var19;
    
    
    // create and load the an object to test
    protected CompositeVariableValue createTestVar() {
    
        ProgDebugger p = new ProgDebugger();

        // create 3 CVs
        Vector<CvValue> v = createCvVector();
        cv17 = new CvValue(17, p);
        cv18 = new CvValue(18, p);
        cv19 = new CvValue(19, p);
        cv17.setValue(2);
        cv18.setValue(3);
        cv18.setValue(4);
        v.setElementAt(cv17, 17);
        v.setElementAt(cv18, 18);
        v.setElementAt(cv19, 19);
        
        // create variables for each CV
        var17 = new DecVariableValue("label17", "comment17", "", false, false, false, false, 17, "VVVVVVVV", 0, 255, v, null, null);
        var18 = new DecVariableValue("label18", "comment18", "", false, false, false, false, 18, "VVVVVVVV", 0, 255, v, null, null);
        var19 = new DecVariableValue("label19", "comment19", "", false, false, false, false, 19, "VVVVVVVV", 0, 255, v, null, null);

        // create composite variable
        CompositeVariableValue testVar = new CompositeVariableValue("testVariable", "commentTest", "", false, false, false, false, 17, "VVVVVVVV", 0, 2, v, null, null);

        // two choices
        testVar.addChoice("first");
        testVar.addSetting("first", "label17", var17, "11");
        testVar.addSetting("first", "label18", var18, "12");
        testVar.addSetting("first", "label19", var19, "13");
        
        testVar.addChoice("second");
        testVar.addSetting("second", "label17", var17, "21");
        testVar.addSetting("second", "label18", var18, "22");
        testVar.addSetting("second", "label19", var19, "23");
        
        testVar.addChoice("third");
        testVar.addSetting("third", "label17", var17, "123");
        testVar.addSetting("third", "label18", var18, "123");
        testVar.addSetting("third", "label19", var19, "123");
        
        testVar.lastItem();
        
        return testVar;
    }
    
    protected Vector<CvValue> createCvVector() {
        Vector<CvValue> v = new Vector<CvValue>(512);
        for (int i=0; i < 512; i++) v.addElement(null);
        return v;
    }

    // from here down is testing infrastructure

    public  CompositeVariableValueTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", CompositeVariableValueTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite( CompositeVariableValueTest.class);
        return suite;
    }

    static Logger log = Logger.getLogger( CompositeVariableValueTest.class.getName());

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

}

