/** 
 * LongAddrVariableValue.java
 *
 * Description:		Extends VariableValue to represent a NMRA long address
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			
 *
 */

package jmri.jmrit.symbolicprog;

import jmri.Programmer;
import jmri.InstanceManager;
import jmri.ProgListener;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.util.Vector;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.text.Document;

public class LongAddrVariableValue extends VariableValue implements ActionListener, PropertyChangeListener {

	public LongAddrVariableValue(String name, String comment, boolean readOnly,
							int cvNum, String mask, int minVal, int maxVal,
							Vector v, JLabel status, String stdname) {
		super(name, comment, readOnly, cvNum, mask, v, status, stdname);
		_maxVal = maxVal;
		_minVal = minVal;
		_value = new JTextField(5);
		_defaultColor = _value.getBackground();
		_value.setBackground(COLOR_UNKNOWN);
		// connect to the JTextField value, cv
		_value.addActionListener(this);
		// connect for notification
		((CvValue)_cvVector.elementAt(getCvNum())).addPropertyChangeListener(this);
		((CvValue)_cvVector.elementAt(getCvNum()+1)).addPropertyChangeListener(this);
	}
	
	// the connection is to cvNum and cvNum+1
	
	int _maxVal;
	int _minVal;
	
	public Object rangeVal() {
		return new String("Long address");
	}
	
	public void actionPerformed(ActionEvent e) {
		// called for new values - set the CV as needed
		CvValue cv17 = (CvValue)_cvVector.elementAt(getCvNum());
		CvValue cv18 = (CvValue)_cvVector.elementAt(getCvNum()+1);
		// no masking involved for long address
		int newVal;
		try { newVal = Integer.valueOf(_value.getText()).intValue(); }
			catch (java.lang.NumberFormatException ex) { newVal = 0; }
			
		// no masked combining of old value required, as this fills the two CVs
		int newCv17 = ((newVal/256)&0x3F) | 0xc0;
		int newCv18 = newVal & 0xFF;
		cv17.setValue(newCv17);
		cv18.setValue(newCv18);
		if (log.isDebugEnabled()) log.debug("new value "+newVal+" gives CV17="+newCv17+" CV18="+newCv18);
	}
	
	// to complete this class, fill in the routines to handle "Value" parameter
	// and to read/write/hear parameter changes. 
	public String getValueString() {
		return _value.getText();
	}
	public void setIntValue(int i) {
		_value.setText(""+i);
	}
	
	public Component getValue()  { 
	 	if (getReadOnly())  //
	 		return new JLabel(_value.getText());
	 	else
	 		return _value; 
	}

	public void setValue(int value) { 
		int oldVal;
		try { oldVal = Integer.valueOf(_value.getText()).intValue(); }
			catch (java.lang.NumberFormatException ex) { oldVal = 0; }	
		if (oldVal != value || getState() == VariableValue.UNKNOWN) 
			prop.firePropertyChange("Value", null, new Integer(value));
		_value.setText(""+value);
	}

	Color _defaultColor;
	// implement an abstract member to set colors
	void setColor(Color c) {
		if (c != null) _value.setBackground(c);
		else _value.setBackground(_defaultColor);
		prop.firePropertyChange("Value", null, null);
	}

	public Component getRep(String format)  { 
		return new VarTextField(_value.getDocument(),_value.getText(), 5, this);
	}
	private int _progState = 0;
	private static final int IDLE = 0;
	private static final int READING_FIRST = 1;
	private static final int READING_SECOND = 2;
	private static final int WRITING_FIRST = 3;
	private static final int WRITING_SECOND = 4;
	
	// 
	public void read() {
		if (log.isDebugEnabled()) log.debug("longAddr read() invoked");
 		setBusy(true);  // will be reset when value changes
		super.setState(READ);
		if (_progState != IDLE) log.warn("Programming state "+_progState+", not IDLE, in read()");
		_progState = READING_FIRST;
		if (log.isDebugEnabled()) log.debug("invoke CV read");		
		((CvValue)_cvVector.elementAt(getCvNum())).read(_status);
	}
	
 	public void write() {
		if (log.isDebugEnabled()) log.debug("write() invoked");
 		if (getReadOnly()) log.error("unexpected write operation when readOnly is set");
 		setBusy(true);  // will be reset when value changes
 		super.setState(STORED);
		if (_progState != IDLE) log.warn("Programming state "+_progState+", not IDLE, in write()");
		_progState = WRITING_FIRST;
		if (log.isDebugEnabled()) log.debug("invoke CV write");		
 		((CvValue)_cvVector.elementAt(getCvNum())).write(_status);
 	}

	// handle incoming parameter notification
	public void propertyChange(java.beans.PropertyChangeEvent e) {
		if (log.isDebugEnabled()) log.debug("property changed event - name: "
										+e.getPropertyName());
		// notification from CV; check for Value being changed
		if (e.getPropertyName().equals("Busy")) {
			// see if this was a read or write operation
			if (log.isDebugEnabled()) log.debug("CV getBusy showing "
												+((CvValue)_cvVector.elementAt(getCvNum())).isBusy());		
			switch (_progState) {
				case IDLE:  // no, just a CV update
						if (log.isDebugEnabled()) log.debug("Busy changed with state IDLE");
						setBusy(false);
						return;
				case READING_FIRST:   // read first CV, now read second
						if (log.isDebugEnabled()) log.debug("Busy changed with state READING_FIRST");
						return;
				case READING_SECOND:  // ignore
						if (log.isDebugEnabled()) log.debug("Busy changed with state READING_SECOND");
						_progState = IDLE;
						return;
				case WRITING_FIRST:  // no, just a CV update
						if (log.isDebugEnabled()) log.debug("Busy changed with state WRITING_FIRST");
						setBusy(true);  // will be reset when value changes
 						super.setState(STORED);
						_progState = WRITING_SECOND;
 						((CvValue)_cvVector.elementAt(getCvNum()+1)).write(_status);
						return;
				case WRITING_SECOND:  // now done with complete request
						if (log.isDebugEnabled()) log.debug("Busy changed with state WRITING_SECOND");
						_progState = IDLE;
						setBusy(false);
						return;
				default:  // unexpected!
						log.error("Unexpected state found: "+_progState);
						_progState = IDLE;
						return;
			}
		}
		else if (e.getPropertyName().equals("State")) {
			CvValue cv = (CvValue)_cvVector.elementAt(getCvNum());
			if (log.isDebugEnabled()) log.debug("CV State changed to "+cv.getState());
			setState(cv.getState());
		}
		else if (e.getPropertyName().equals("Value")) {
			
			setBusy(false);
			// update value of Variable
			CvValue cv0 = (CvValue)_cvVector.elementAt(getCvNum());
			CvValue cv1 = (CvValue)_cvVector.elementAt(getCvNum()+1);
			int newVal = (cv0.getValue()&0x3f)*256 + cv1.getValue();
			setValue(newVal);  // check for duplicate done inside setVal
			// state change due to CV state change, so propagate that
			setState(cv0.getState());
			// see if this was a read or write operation
			switch (_progState) {
				case IDLE:  // no, just a CV update
						if (log.isDebugEnabled()) log.debug("Value changed with state IDLE");
						return;
				case READING_FIRST:  // yes, now read second
						if (log.isDebugEnabled()) log.debug("Value changed with state READING_FIRST");
						setBusy(true);  // will be reset when value changes
						super.setState(READ);
						_progState = READING_SECOND;
						((CvValue)_cvVector.elementAt(getCvNum()+1)).read(_status);
						return;
				case READING_SECOND:  // now done with complete request
						if (log.isDebugEnabled()) log.debug("Value changed with state READING_SECOND");
						_progState = IDLE;
						return;
				default:  // unexpected!
						log.error("Unexpected state found: "+_progState);
						_progState = IDLE;
						return;
			}
		}
	}

	// stored value
	JTextField _value = null;

	// clean up connections when done
	public void dispose() {
		if (_value != null) _value.removeActionListener(this);
		((CvValue)_cvVector.elementAt(getCvNum())).removePropertyChangeListener(this);
	}
	
	/* Internal class extends a JTextField so that its color is consistent with 
	 * an underlying variable
	 *
	 * @author			Bob Jacobsen   Copyright (C) 2001
	 * @version			
	 */
	public class VarTextField extends JTextField {

		VarTextField(Document doc, String text, int col, LongAddrVariableValue var) {
			super(doc, text, col);
			_var = var;
			// get the original color right
			setBackground(_var._value.getBackground());
			// listen for changes to ourself
			addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					thisActionPerformed(e);
				}
			});		
			// listen for changes to original state
			_var.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
				public void propertyChange(java.beans.PropertyChangeEvent e) {
					originalPropertyChanged(e);
				}
			});		
		}

		LongAddrVariableValue _var;
	
		void thisActionPerformed(java.awt.event.ActionEvent e) {
			// tell original
			_var.actionPerformed(e);
		}

		void originalPropertyChanged(java.beans.PropertyChangeEvent e) {
			// update this color from original state
			if (e.getPropertyName().equals("State")) {
				setBackground(_var._value.getBackground());
			}	
		}
	
	}

	// initialize logging	
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LongAddrVariableValue.class.getName());
		
}
