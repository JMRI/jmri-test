/** 
 * NceProgrammer.java
 *
 * Description:		<describe the NceProgrammer class here>
 * @author			Bob Jacobsen  Copyright (C) 2001
 * @version			
 */
 
 // Convert the jmri.Programmer interface into commands for the NCE powerstation

package jmri.jmrix.nce;

import jmri.Programmer;

import java.util.Vector;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

public class NceProgrammer implements NceListener, Programmer {
	
	public NceProgrammer() { 
		// error if more than one constructed?
		if (self != null) 
			log.error("Creating too many NceProgrammer objects");

		// register this as the default, register as the Programmer
		self = this; 
		jmri.InstanceManager.setProgrammer(this);
			
		}

	/* 
	 * method to find the existing NceProgrammer object, if need be creating one
	 */
	static public final NceProgrammer instance() { 
		if (self == null) self = new NceProgrammer();
		return self;
		}
	static private NceProgrammer self = null;

	// handle mode
	protected int _mode = 0;
	
	public void setMode(int mode) {
		if (mode != _mode) {
			notifyPropertyChange("Mode", _mode, mode);
			_mode = mode;
		}
	}
	public int getMode() { return _mode; }
	

// data members to hold contact with the property listeners
	private Vector propListeners = new Vector();
	
	public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
		// add only if not already registered
		if (!propListeners.contains(l)) {
			propListeners.addElement(l);
		}
	}

	public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
		if (propListeners.contains(l)) {
			propListeners.removeElement(l);
		}
	}

	protected void notifyPropertyChange(String name, int oldval, int newval) {
		// make a copy of the listener vector to synchronized not needed for transmit
		Vector v;
		synchronized(this) {
			v = (Vector) propListeners.clone();
		}
		// forward to all listeners
		int cnt = v.size();
		for (int i=0; i < cnt; i++) {
			PropertyChangeListener client = (PropertyChangeListener) v.elementAt(i);
			client.propertyChange(new PropertyChangeEvent(this, name, new Integer(oldval), new Integer(newval)));
		}
	}
	
	// members for handling the programmer interface
	
	int progState = 0;
		// 1 is commandPending
		// 0 is notProgramming
	boolean  _progRead = false;
	
	
	// programming interface
	public void writeCV(int CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
		useProgrammer(p);
		_progRead = false;
		// set commandPending state
		progState = 1;
		
		// format and send message
		controller().sendNceMessage(progTaskStart(getMode(), val, CV), this);
	}
		
	public void readCV(int CV, jmri.ProgListener p) throws jmri.ProgrammerException {
		useProgrammer(p);
		_progRead = true;
		// set commandPending state
		progState = 1;
		
		// format and send message
		controller().sendNceMessage(progTaskStart(getMode(), -1, CV), this);
	}

	private jmri.ProgListener _usingProgrammer = null;
	
	// internal method to remember who's using the programmer
	protected void useProgrammer(jmri.ProgListener p) throws jmri.ProgrammerException {
		// test for only one!
		if (_usingProgrammer != null && _usingProgrammer != p) {
				if (log.isInfoEnabled()) log.info("programmer already in use by "+_usingProgrammer);
				throw new jmri.ProgrammerException("programmer in use");
			}
		else {
			_usingProgrammer = p;
			return;
		}
	}
	
	// internal method to create the NceMessage for programmer task start
	protected NceMessage progTaskStart(int mode, int val, int cvnum) throws jmri.ProgrammerException {
		// val = -1 for read command; mode is direct, etc
		if (val < 0) {
			// read
			return NceMessage.getReadPagedCV(cvnum);
		} else {
			// write
			return NceMessage.getWritePagedCV(cvnum, val);
		}
	}
	
	public void message(NceMessage m) {
		log.error("message received unexpectedly: "+m.toString());
	}
	
	public void reply(NceReply m) {
		if (progState == 0) {
			log.error("reply received unexpectedly: "+m.toString());
			return;
		}
		// here is expected, set ProgState back to not expected
		progState = 0;
		// see why waiting
		if (_progRead) {
			// read was in progress - get return value
			notifyProgListenerEnd(m.value(), jmri.ProgListener.OK);
		} else {
			// write was in progress
			notifyProgListenerEnd(200, jmri.ProgListener.OK);
		}
	}
	
	// internal method to notify of the final result
	protected void notifyProgListenerEnd(int value, int status) {
		_usingProgrammer.programmingOpReply(value, status);
		_usingProgrammer = null;
	}

	NceTrafficController _controller = null;

	protected NceTrafficController controller() {
		// connect the first time
		if (_controller == null) {
			_controller = NceTrafficController.instance();
			_controller.addNceListener(this);	
		}
		return _controller;
	}

   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NceProgrammer.class.getName());

}


/* @(#)NceProgrammer.java */
