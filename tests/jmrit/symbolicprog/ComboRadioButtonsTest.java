/** 
 * ComboRadioButtonsTest.java
 *
 * Description:	
 * @author			Bob Jacobsen
 * @version			
 */

package jmri.jmrit.symbolicprog;

import java.io.*;
import java.util.*;
import javax.swing.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Assert;

import jmri.*;
import jmri.progdebugger.*;

public class ComboRadioButtonsTest extends TestCase {

	public void testAppearance() {
		// create an enum variable pointed at CV 81 and connect
		Vector v = createCvVector();
		CvValue cv = new CvValue(81);
		cv.setValue(3);
		v.setElementAt(cv, 81);
		EnumVariableValue var = new EnumVariableValue("name", "comment", false, 81, "XXVVVVXX", 0, 255, v, null, null);
		var.addItem("Value0");
		var.addItem("Value1");
		var.addItem("Value2");
		JComboBox combo = (JComboBox)(var.getValue());
		
		// create object under test
		ComboRadioButtons b = new ComboRadioButtons(combo, var);
		
		// check length
		Assert.assertEquals("expected item count ", 3, b.v.size());
	}
	
	public void testToOriginal() {
		// create an enum variable pointed at CV 81 and connect
		Vector v = createCvVector();
		CvValue cv = new CvValue(81);
		cv.setValue(3);
		v.setElementAt(cv, 81);
		EnumVariableValue var = new EnumVariableValue("name", "comment", false, 81, "XXVVVVXX", 0, 255, v, null, null);
		var.addItem("Value0");
		var.addItem("Value1");
		var.addItem("Value2");
		JComboBox combo = (JComboBox)(var.getValue());
		
		// create object under test
		ComboRadioButtons b = new ComboRadioButtons(combo, var);
		
		// click middle button & test state
		((JRadioButton)(b.v.elementAt(1))).doClick();
		Assert.assertEquals("1 click button on ", true, ((JRadioButton)(b.v.elementAt(1))).isSelected());
		Assert.assertEquals("1 click button 0 off ", false, ((JRadioButton)(b.v.elementAt(0))).isSelected());
		Assert.assertEquals("1 click button 2 off ", false, ((JRadioButton)(b.v.elementAt(2))).isSelected());
		Assert.assertEquals("1 click original state ", 1, combo.getSelectedIndex());
		
		// click top button & test state
		((JRadioButton)(b.v.elementAt(0))).doClick();
		Assert.assertEquals("0 click button on ", true, ((JRadioButton)(b.v.elementAt(0))).isSelected());
		Assert.assertEquals("0 click button 1 off ", false, ((JRadioButton)(b.v.elementAt(1))).isSelected());
		Assert.assertEquals("0 click button 2 off ", false, ((JRadioButton)(b.v.elementAt(2))).isSelected());
		Assert.assertEquals("0 click original state ", 0, combo.getSelectedIndex());

		// click bottom button & test state
		((JRadioButton)(b.v.elementAt(2))).doClick();
		Assert.assertEquals("2 click button on ", true, ((JRadioButton)(b.v.elementAt(2))).isSelected());
		Assert.assertEquals("2 click button 0 off ", false, ((JRadioButton)(b.v.elementAt(0))).isSelected());
		Assert.assertEquals("2 click button 1 off ", false, ((JRadioButton)(b.v.elementAt(1))).isSelected());
		Assert.assertEquals("2 click original state ", 2, combo.getSelectedIndex());
		
	}
	
	public void testFromOriginal() {
		// create an enum variable pointed at CV 81 and connect
		Vector v = createCvVector();
		CvValue cv = new CvValue(81);
		cv.setValue(3);
		v.setElementAt(cv, 81);
		EnumVariableValue var = new EnumVariableValue("name", "comment", false, 81, "XXVVVVXX", 0, 255, v, null, null);
		var.addItem("Value0");
		var.addItem("Value1");
		var.addItem("Value2");
		JComboBox combo = (JComboBox)(var.getValue());
		
		// create object under test
		ComboRadioButtons b = new ComboRadioButtons(combo, var);
		
		// set combo box to 1 and check state
		combo.setSelectedIndex(1);
		Assert.assertEquals("1 click button on ", true, ((JRadioButton)(b.v.elementAt(1))).isSelected());
		Assert.assertEquals("1 click button 0 off ", false, ((JRadioButton)(b.v.elementAt(0))).isSelected());
		Assert.assertEquals("1 click button 2 off ", false, ((JRadioButton)(b.v.elementAt(2))).isSelected());
		Assert.assertEquals("1 click original state ", 1, combo.getSelectedIndex());
		
		// set combo box to 2 and check state
		combo.setSelectedIndex(2);
		Assert.assertEquals("2 click button on ", true, ((JRadioButton)(b.v.elementAt(2))).isSelected());
		Assert.assertEquals("2 click button 0 off ", false, ((JRadioButton)(b.v.elementAt(0))).isSelected());
		Assert.assertEquals("2 click button 1 off ", false, ((JRadioButton)(b.v.elementAt(1))).isSelected());
		Assert.assertEquals("2 click original state ", 2, combo.getSelectedIndex());
		
		// set combo box to 0 and check state
		combo.setSelectedIndex(0);
		Assert.assertEquals("0 click button on ", true, ((JRadioButton)(b.v.elementAt(0))).isSelected());
		Assert.assertEquals("0 click button 1 off ", false, ((JRadioButton)(b.v.elementAt(1))).isSelected());
		Assert.assertEquals("0 click button 2 off ", false, ((JRadioButton)(b.v.elementAt(2))).isSelected());
		Assert.assertEquals("0 click original state ", 0, combo.getSelectedIndex());
		
	}
	
	protected Vector createCvVector() {
		Vector v = new Vector(512);
		for (int i=0; i < 512; i++) v.addElement(null);
		return v;
	}


	// from here down is testing infrastructure
	
	public ComboRadioButtonsTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {ComboRadioButtonsTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}
	
	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(ComboRadioButtonsTest.class);
		return suite;
	}
	
	// static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ComboRadioButtonsTest.class.getName());

}
