/** 
 * VariableValue.java
 *
 * Description:		Represents a single Variable value; abstract base class
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			
 *
 */

package jmri.jmrit.symbolicprog;

import jmri.Programmer;
import jmri.InstanceManager;
import jmri.ProgListener;

import java.util.Vector;
import java.awt.Component;
import javax.swing.JLabel;
import java.awt.Color;

public abstract class VariableValue extends AbstractValue implements java.beans.PropertyChangeListener {

	// The actual stored value is internal, not showing in the interface.
	// Instead, you can get a (Object) representation for display in 
	// a table, etc. Modification of the state of that object then
	// gets reflected back, causing the underlying CV objects to change.
	abstract public Component getValue();	// this one is returning a common value
											// and thus can be called without limit
	abstract public Component getRep(String format); // this one is returning a new object
										// and thus should be called a limited number of times
	
	abstract public String getValueString();
	abstract public void setIntValue(int i);

	// methods to command a read from / write to the decoder of the underlying CVs
	abstract public void read();	
	abstract public void write();

	// handle incoming parameter notification
	abstract public void propertyChange(java.beans.PropertyChangeEvent e);
	abstract public void dispose();
	
	abstract public Object rangeVal();
	
	// methods implemented here:
	public VariableValue(String name, String comment, boolean readOnly,
							int cvNum, String mask, Vector v, JLabel status, String stdname) { 
		_name = name;
		_comment = comment;
		_readOnly = readOnly;
		_cvNum = cvNum;
		_mask = mask;
		_cvVector = v;
		_status = status;
		_stdName = stdname;
	}

  	/** 
  	 * Create a null object.  Normally only used for tests and to pre-load classes.
  	 */ 
   	protected VariableValue() {}

	// common information - none of these are bound
	public String name() { return _name; }
	public String stdName() { return _stdName; }
	private String _name;
	private String _stdName;
	protected Vector _cvVector;   // Vector of 512 CV objects used to look up CVs
	protected JLabel _status = null;
	
	public String getComment() { return _comment; }
	private String _comment;
	
	public boolean getReadOnly() { return _readOnly; }
	private boolean _readOnly;
	
	public int getCvNum() { return _cvNum; }
	private int _cvNum;
	
	public String getMask() { return _mask; }
	private String _mask;

	public int getState()  { return _state; }
	public void setState(int state) {
		switch (state) {
			case UNKNOWN : setColor(COLOR_UNKNOWN ); break;
			case EDITTED : setColor(COLOR_EDITTED ); break;
			case READ    : setColor(COLOR_READ    ); break;
			case STORED  : setColor(COLOR_STORED  ); break;
			case FROMFILE: setColor(COLOR_FROMFILE); break;
			default:      log.error("Inconsistent state: "+_state);
		}
		if (_state != state || _state == UNKNOWN) prop.firePropertyChange("State", new Integer(_state), new Integer(state));
		_state = state;
	}
	private int _state = UNKNOWN;
		
	// busy during read, write operations
	public boolean isBusy() { return _busy; }
	protected void setBusy(boolean newBusy) {
		boolean oldBusy = _busy;
		_busy = newBusy;
		if (newBusy != oldBusy) prop.firePropertyChange("Busy", new Boolean(oldBusy), new Boolean(newBusy));
	}
	private boolean _busy = false;
	
	// handle outgoing parameter notification
	java.beans.PropertyChangeSupport prop = new java.beans.PropertyChangeSupport(this);	
	public void removePropertyChangeListener(java.beans.PropertyChangeListener p) { prop.removePropertyChangeListener(p); }
	public void addPropertyChangeListener(java.beans.PropertyChangeListener p) { prop.addPropertyChangeListener(p); }
	
	// tool to handle masking, updating
	protected int maskVal(String maskString) {
		// convert String mask to int
		int mask = 0;
		for (int i=0; i<8; i++) {
			mask = mask << 1;
			if (maskString.charAt(i) == 'V') {
				mask = mask+1;
			}
		}
		return mask;
	}
	
	protected int offsetVal(String maskString) {
		// convert String mask to int
		int offset = 0;
		for (int i=0; i<8; i++) {
			if (maskString.charAt(i) == 'V') {
				offset = 7-i;  // number of places to shift left
			}
		}
		return offset;
	}
	
	protected int newValue(int oldCv, int newVal, String maskString) {
		int mask = maskVal(maskString);
		int offset = offsetVal(maskString);
		return (oldCv & ~mask) + ((newVal << offset) & mask);
	}

	// initialize logging	
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(VariableValue.class.getName());
		
}
