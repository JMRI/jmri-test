// EnumVariableValue.java

package jmri.jmrit.symbolicprog;

import jmri.Programmer;
import jmri.InstanceManager;
import jmri.ProgListener;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;
import javax.swing.*;
import java.awt.Color;

import com.sun.java.util.collections.List;
import com.sun.java.util.collections.ArrayList;

/** 
 * Extends VariableValue to represent a enumerated variable.
 *
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Id: EnumVariableValue.java,v 1.8 2001-11-27 03:27:15 jacobsen Exp $
 *
 */
public class EnumVariableValue extends VariableValue implements ActionListener, PropertyChangeListener {

	public EnumVariableValue(String name, String comment, boolean readOnly,
							int cvNum, String mask, int minVal, int maxVal,
							Vector v, JLabel status, String stdname) {
		super(name, comment, readOnly, cvNum, mask, v, status, stdname);
		_maxVal = maxVal;
		_minVal = minVal;
		_value.setActionCommand("");
		_defaultColor = _value.getBackground();
		_value.setBackground(COLOR_UNKNOWN);
		// connect to the JComboBox model and the CV so we'll see changes.
		_value.addActionListener(this);
		((CvValue)_cvVector.elementAt(getCvNum())).addPropertyChangeListener(this);
	}
	
  	/** 
  	 * Create a null object.  Normally only used for tests and to pre-load classes.
  	 */ 
   	public EnumVariableValue() {}
  	
	public void addItem(String s) {
		_value.addItem(s);
	}
	
	private int _maxVal;
	private int _minVal;
	Color _defaultColor;
	
	public Object rangeVal() {
		return new String("enum: "+_minVal+" - "+_maxVal);
	}
	
	public void actionPerformed(ActionEvent e) {
		// see if this is from _value itself, or from an alternate rep.
		// if from an alternate rep, it will contain the value to select
		if (!(e.getActionCommand().equals(""))) {
			// is from alternate rep
			_value.setSelectedItem(e.getActionCommand());
		}
		if (log.isDebugEnabled()) log.debug("action event: "+e);
		// called for new values - set the CV as needed
		CvValue cv = (CvValue)_cvVector.elementAt(getCvNum());
		// compute new cv value by combining old and request
		int oldCv = cv.getValue();
		int newVal;
		try { newVal = _value.getSelectedIndex(); }
			catch (java.lang.NumberFormatException ex) { newVal = 0; }
		int newCv = newValue(oldCv, newVal, getMask());
		if (newCv != oldCv) cv.setValue(newCv);  // to prevent CV going EDITTED during loading of decoder file
	}
	
	// to complete this class, fill in the routines to handle "Value" parameter
	// and to read/write/hear parameter changes. 
	public String getValueString() {
		return ""+_value.getSelectedIndex();
	}
	public void setIntValue(int i) {
		_value.setSelectedIndex(i);
	}
	
	public Component getValue()  { return _value; }
	public void setValue(int value) { 
		int oldVal = _value.getSelectedIndex();
		if (oldVal != value || getState() == VariableValue.UNKNOWN) 
			prop.firePropertyChange("Value", null, new Integer(value));
		_value.setSelectedIndex(value);
	}

	public Component getRep(String format) {
		// sort on format type
		if (format.equals("checkbox")) {
			// this only makes sense if there are exactly two options
			ComboCheckBox b = new ComboCheckBox(_value, this);
			comboCBs.add(b);
			return b;
		}
		else if (format.equals("radiobuttons")) {
			ComboRadioButtons b = new ComboRadioButtons(_value, this);
			comboRBs.add(b);
			return b;
		}
		else {
			// return a new JComboBox representing the same model
			VarComboBox b = new VarComboBox(_value.getModel(), this);
			comboVars.add(b);
			return b;
		}
	}
	
	List comboCBs = new ArrayList();
	List comboVars = new ArrayList();
	List comboRBs = new ArrayList();
	
	// implement an abstract member to set colors
	void setColor(Color c) {
		if (c != null) _value.setBackground(c);
		else _value.setBackground(_defaultColor);
		prop.firePropertyChange("Value", null, null);
	}

	// member functions to control reading/writing the variables
	public void read() {
 		setBusy(true);  // will be reset when value changes
		super.setState(READ);
		((CvValue)_cvVector.elementAt(getCvNum())).read(_status);
	}
	
 	public void write() {
 		if (getReadOnly()) log.error("unexpected write operation when readOnly is set");
 		setBusy(true);  // will be reset when value changes
 		super.setState(STORED);
 		((CvValue)_cvVector.elementAt(getCvNum())).write(_status);
 	}

	// handle incoming parameter notification
	public void propertyChange(java.beans.PropertyChangeEvent e) {
		// notification from CV; check for Value being changed
		if (e.getPropertyName().equals("Busy")) {
			if (((Boolean)e.getNewValue()).equals(Boolean.FALSE)) setBusy(false);
		}
		else if (e.getPropertyName().equals("State")) {
			CvValue cv = (CvValue)_cvVector.elementAt(getCvNum());
			setState(cv.getState());
		}
		else if (e.getPropertyName().equals("Value")) {
			// update value of Variable
			CvValue cv = (CvValue)_cvVector.elementAt(getCvNum());
			int newVal = (cv.getValue() & maskVal(getMask())) >>> offsetVal(getMask());
			setValue(newVal);  // check for duplicate done inside setVal
		}
	}

	// stored value
	JComboBox _value = new JComboBox();

	/* Internal class extends a JComboBox so that its color is consistent with 
	 * an underlying variable; we return one of these in getRep.
	 *<P>
	 * Unlike similar cases elsewhere, this doesn't have to listen to
	 * value changes.  Those are handled automagically since we're sharing the same
	 * model between this object and the real JComboBox value.
	 *
	 * @author			Bob Jacobsen   Copyright (C) 2001
	 * @version			
	 */
	public class VarComboBox extends JComboBox {

		VarComboBox(ComboBoxModel m, EnumVariableValue var) {
			super(m);
			_var = var;
			_l = new java.beans.PropertyChangeListener() {
				public void propertyChange(java.beans.PropertyChangeEvent e) {
					if (log.isDebugEnabled()) log.debug("VarComboBox saw property change: "+e);
					originalPropertyChanged(e);
				}
			};
			// get the original color right
			setBackground(_var._value.getBackground());
			// listen for changes to original state
			_var.addPropertyChangeListener(_l);		
		}

		EnumVariableValue _var;
		java.beans.PropertyChangeListener _l = null;
	
		void originalPropertyChanged(java.beans.PropertyChangeEvent e) {
			// update this color from original state
			if (e.getPropertyName().equals("State")) {
				setBackground(_var._value.getBackground());
			}	
		}
	
		public void dispose() {
			if (_var != null && _l != null ) _var.removePropertyChangeListener(_l);
			_l = null;
			_var = null;
		}
	}

	// clean up connections when done
	public void dispose() {
		if (log.isDebugEnabled()) log.debug("dispose");
		if (_value != null) _value.removeActionListener(this);
		((CvValue)_cvVector.elementAt(getCvNum())).removePropertyChangeListener(this);
		for (int i = 0; i<comboCBs.size(); i++) {
			((ComboCheckBox)comboCBs.get(i)).dispose();
		}
		for (int i = 0; i<comboVars.size(); i++) {
			((VarComboBox)comboVars.get(i)).dispose();
		}
		for (int i = 0; i<comboRBs.size(); i++) {
			((ComboRadioButtons)comboRBs.get(i)).dispose();
		}
		
		_value = null;
		// do something about the VarComboBox
	}

	// initialize logging	
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(EnumVariableValue.class.getName());
		
}
