/** 
 * SlotManager.java
 *
 * Description:		<describe the SlotManager class here>
 * @author			Bob Jacobsen  Copyright (C) 2001
 * @version			
 */
 
 // This is a collection of LocoNetSlots, plus support for coordinating
 // them with the controller

package jmri.jmrix.loconet;

import jmri.Programmer;

import ErrLoggerJ.ErrLog;

import java.util.Vector;

public class SlotManager implements LocoNetListener, Programmer {

	private LocoNetSlot _slots[] = new LocoNetSlot[128];

	public LocoNetSlot slot(int i) {return _slots[i];}
	
	public SlotManager() { 
		// error if more than one constructed?
		if (self != null) 
			ErrLog.msg(ErrLog.error, "SlotManager", 
						"ctor", "Creating too many SlotManager objects");

		// initialize slot array
		for (int i=0; i<=127; i++) _slots[i] = new LocoNetSlot();
		
		// register this as the default, register as the Programmer
		self = this; 
		jmri.InstanceManager.setProgrammer(this);
			
		// listen to the LocoNet
		LnTrafficController.instance().addLocoNetListener(~0, this);	
		}

	// obtain a slot for a particular loco address
	// this will actually require a delayed return value - what impact does that have?
	LocoNetSlot fromLocoAddress(int i) { return null; }
	
	/* 
	 * method to find the existing SlotManager object, if need be creating one
	 */
	static public final SlotManager instance() { 
		if (self == null) self = new SlotManager();
		return self;
		}
	static private SlotManager self = null;

// data members to hold contact with the listeners
	private Vector listeners = new Vector();
	
	public synchronized void addSlotListener(SlotListener l) { 
			// add only if not already registered
			if (!listeners.contains(l)) {
					listeners.addElement(l);
				}
		}

	public synchronized void removeSlotListener(SlotListener l) {
			if (listeners.contains(l)) {
					listeners.removeElement(l);
				}
		}

	protected void notify(LocoNetSlot s) {
		// make a copy of the listener vector to synchronized not needed for transmit
		Vector v;
		synchronized(this)
			{
				v = (Vector) listeners.clone();
			}
		// forward to all listeners
		int cnt = v.size();
		for (int i=0; i < cnt; i++) {
			SlotListener client = (SlotListener) listeners.elementAt(i);
			client.notifyChangedSlot(s);
						}
	}
	

	// listen to the LocoNet
	public void message(LocoNetMessage m) {
		int i = 0;

		switch (m.getOpCode()) {
			case LnConstants.OPC_WR_SL_DATA:
			case LnConstants.OPC_SL_RD_DATA:
				i = m.getElement(2);
				break;
							
			case LnConstants.OPC_LOCO_DIRF:
			case LnConstants.OPC_LOCO_SND:
			case LnConstants.OPC_LOCO_SPD: 
			case LnConstants.OPC_SLOT_STAT1: 
				i = m.getElement(1);
				break;

			case LnConstants.OPC_MOVE_SLOTS:  // handle the follow-on message when it comes
				return; // need to cope with that!!
			
			case LnConstants.OPC_LONG_ACK:
				// handle if reply to slot. There's no slot number in the LACK, unfortunately.
				// If this is a LACK to a Slot op, and progState is command pending, 
				// assume its for us...
				if (m.getElement(1) == 0x6F && progState == 1 ) {
						// check status byte
						if (m.getElement(2) == 1) { // task accepted
							// move to commandExecuting state
							progState = 2;
							}
						else if (m.getElement(2) == 0) { // task aborted as busy
							// move to not programming state
							progState = 0;
							// notify user ProgListener
							notifyProgListenerLack(jmri.ProgListener.ProgrammerBusy);
							}
						else if (m.getElement(2) == 0x7F) { // not implemented
							// move to not programming state
							progState = 0;
							// notify user ProgListener
							notifyProgListenerLack(jmri.ProgListener.NotImplemented);
							}
						else if (m.getElement(2) == 0x40) { // task accepted blind
							// move to not programming state
							progState = 0;
							// notify user ProgListener
							notifyProgListenerLack(jmri.ProgListener.OK);
							}
						else { // not sure how to cope, so complain
							ErrLog.msg(ErrLog.error, "SlotManager", 
								"message", "unexpected LACK reply code "+m.getElement(2));
							// move to not programming state
							progState = 0;
							// notify user ProgListener
							notifyProgListenerLack(jmri.ProgListener.UnknownError);
							}
					}
				else return;
				
			default: 
				// nothing here for us
				return;
			}	
			
		// if here, i holds the slot number, and we expect to be able to parse		
		// and have the slot handle the message
		try {
			_slots[i].setSlot(m);
			}
		catch (LocoNetException e) {
			// must not have been interesting, or at least routed right
			ErrLog.msg(ErrLog.error, "SlotManager", 
						"message", "slot rejected LocoNetMessage"+m);
			return;
			}
		// notify listeners that slots may have changed
		notify(_slots[i]);
		
		// start checking for programming operations
		if (i == 124) {
			// here its an operation on the programmer slot
			switch (progState) {
				case 0:   // notProgramming
					break;
				case 1:   // commandPending
					// we just sit here waiting for a LACK, handled above
					break;
				case 2:   // commandExecuting
					// waiting for slot read, is it present?
					if (m.getOpCode() == LnConstants.OPC_SL_RD_DATA) {	
						// yes, this is the end
						// move to not programming state
						progState = 0;
						
						// parse out value returned
						int value = -1;
						if (_progRead ) {
							// read command, get value
							value = _slots[i].cvval();
							}
						// parse out status
						int status = 0;
						if ( (_slots[i].pcmd() & LnConstants.PSTAT_NO_DECODER ) != 0 )  
							status = status + jmri.ProgListener.NoLocoDetected;
						if ( (_slots[i].pcmd() & LnConstants.PSTAT_WRITE_FAIL ) != 0 )  
							status = status + jmri.ProgListener.NoAck;
						if ( (_slots[i].pcmd() & LnConstants.PSTAT_READ_FAIL ) != 0 )  
							status = status + jmri.ProgListener.NoAck;
						if ( (_slots[i].pcmd() & LnConstants.PSTAT_USER_ABORTED ) != 0 )  
							status = status + jmri.ProgListener.UserAborted;
						
						// and send the notification
						notifyProgListenerEnd(value, status);
					}
					break;
				default:  // error!
					ErrLog.msg(ErrLog.error, "SlotManager", 
						"message", "unexpected programming state "+progState);
					break;
			}
		}
	}

	// members for handling the programmer interface
	
	int progState = 0;
		// 1 is commandPending
		// 2 is commandExecuting
		// 0 is notProgramming
	boolean  _progRead = false;
	
	public void writeCV(int CV, int val, int mode, jmri.ProgListener p) throws jmri.ProgrammerException {
		useProgrammer(p);
		_progRead = false;
		// set commandPending state
		progState = 1;
		
		// format and send message
		LnTrafficController.instance().sendLocoNetMessage(progTaskStart(mode, val, CV));

		}
		
	public void readCV(int CV, int mode, jmri.ProgListener p) throws jmri.ProgrammerException {
		useProgrammer(p);
		_progRead = true;
		// set commandPending state
		progState = 1;
		
		// format and send message
		LnTrafficController.instance().sendLocoNetMessage(progTaskStart(mode, -1, CV));
		}

	private jmri.ProgListener _usingProgrammer = null;
	
	// internal method to remember who's using the programmer
	protected void useProgrammer(jmri.ProgListener p) throws jmri.ProgrammerException {
		// test for only one!
		if (_usingProgrammer != null) {
				ErrLog.msg(ErrLog.routine, "SlotManager", 
						"useProgrammer", "programmer already in use by "+_usingProgrammer);
				throw new jmri.ProgrammerException("programmer in use");
			}
		else {
			_usingProgrammer = p;
			return;
		}
	}
	
	// internal method to create the LocoNetMessage for programmer task start
	protected LocoNetMessage progTaskStart(int mode, int val, int cvnum) throws jmri.ProgrammerException {
	
		int addr = cvnum-1;    // cvnum is in human readable form; addr is what's sent over loconet
		
		LocoNetMessage m = new LocoNetMessage(14);

		m.setOpCode(0xEF);
		m.setElement(1, 0x0E);
		m.setElement(2, 0x7C);
		
		// parse the programming command
		int pcmd = 0;
		if (val != -1) pcmd = pcmd | 0x40;  // write command
		if (mode == jmri.Programmer.PAGEMODE) pcmd = pcmd | 0x20;
		else if (mode == jmri.Programmer.DIRECTMODE) pcmd = pcmd | 0x28;
		else if (mode == jmri.Programmer.REGISTERMODE) pcmd = pcmd | 0x10;
		else throw new jmri.ProgrammerException("mode not supported");
		m.setElement(3, pcmd);
		
		// set zero, then zero HOPSA, LOPSA, TRK
		m.setElement(4, 0);
		m.setElement(5, 0);
		m.setElement(6, 0);
		m.setElement(7, 0);
		
		// store address in CVH, CVL. Note CVH format is truely wierd...
		m.setElement(8, (addr&0x300)/16 + (addr&0x80)/128 + (val&0x80)/128*2 );
		m.setElement(9,addr & 0x7F);
		
		// store low bits of CV value 
		m.setElement(10, val&0x7F);
		
		return m;
	}
	
	
	// internal method to notify of the final result
	protected void notifyProgListenerEnd(int value, int status) {
		_usingProgrammer.programmingOpReply(value, status);
		_usingProgrammer = null;
	}

	// internal method to notify of the LACK result
	// a separate routine from nPLRead in case we need to handle something later
	protected void notifyProgListenerLack(int status) {
		_usingProgrammer.programmingOpReply(-1, status);
		_usingProgrammer = null;
	}
}


/* @(#)SlotManager.java */
