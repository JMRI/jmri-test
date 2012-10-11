/**
 * XNetProgrammer.java
 */

 // Convert the jmri.Programmer interface into commands for the Lenz XpressNet

package jmri.jmrix.lenz;

import jmri.Programmer;
import jmri.jmrix.AbstractProgrammer;
import java.util.Vector;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/**
 * Programmer support for Lenz XpressNet.
 * <P>
 * The read operation state sequence is:
 * <UL>
 * <LI>Send Register Mode / Paged mode /Direct Mode read request
 * <LI>Wait for Broadcast Service Mode Entry message
 * <LI>Send Request for Service Mode Results request
 * <LI>Wait for results reply, interpret
 * <LI>Send Resume Operations request
 * <LI>Wait for Normal Operations Resumed broadcast
 * </UL>
 * @author Bob Jacobsen     Copyright (c) 2002, 2007
 * @author Paul Bender      Copyright (c) 2003-2010
 * @author Giorgio Terdina  Copyright (c) 2007
 * @version $Revision$
 */
public class XNetProgrammer extends AbstractProgrammer implements XNetListener {

	static protected final int XNetProgrammerTimeout = 90000;

	// keep track of whether or not the command station is in service 
        // mode.  Used for determining if "OK" message is an aproriate 
	// response to a request to a programming request. 
	protected boolean _service_mode = false;

	public XNetProgrammer(XNetTrafficController tc) {
	   // error if more than one constructed?

           _controller=tc;

           // connect to listen
           controller().addXNetListener(XNetInterface.CS_INFO |
			     XNetInterface.COMMINFO |
			     XNetInterface.INTERFACE,
			     this);

        }


	// handle mode
	protected int _mode = Programmer.DIRECTBYTEMODE;

    /**
     * Switch to a new programming mode.  Lenz can now only
     * do register, page, and direct mode. If you attempt to 
     * switch to any others, the new mode will set & notify, 
     * then set back to the original.  This lets the listeners
     * know that a change happened, and then was undone.
     * @param mode The new mode, use values from the jmri.Programmer interface
     */
	public synchronized void setMode(int mode) {
        int oldMode = _mode;  // preserve this in case we need to go back
		if (mode != _mode) {
			notifyPropertyChange("Mode", _mode, mode);
			_mode = mode;
		}
		if (_mode != Programmer.PAGEMODE && _mode != Programmer.REGISTERMODE
                && mode != Programmer.DIRECTBITMODE && mode != Programmer.DIRECTBYTEMODE ) {
            // attempt to switch to unsupported mode, switch back to previous
			_mode = oldMode;
			notifyPropertyChange("Mode", mode, _mode);
		}
	}
	synchronized public int getMode() { return _mode; }
    /**
     * Signifies mode's available
     * @param mode
     * @return True if paged,register,or Direct Mode (Bit or Byte) mode
     */
    public boolean hasMode(int mode) {
        if ( mode == Programmer.PAGEMODE ||
             mode == Programmer.REGISTERMODE ||
             mode == Programmer.DIRECTBITMODE ||
             mode == Programmer.DIRECTBYTEMODE ) {
            log.debug("hasMode request on mode "+mode+" returns true");
            return true;
        }
        log.debug("hasMode returns false on mode "+mode);
        return false;
    }

    public boolean getCanRead() {
		// Multimaus cannot read CVs, unless Rocomotion interface is used, assume other Command Stations do.
		// To be revised if and when a Rocomotion adapter is introduced!!!
		return (controller().getCommandStation().getCommandStationType() != 0x10);
    }

	// notify property listeners - see AbstractProgrammer for more

	@SuppressWarnings("unchecked")
	protected void notifyPropertyChange(String name, int oldval, int newval) {
		// make a copy of the listener vector to synchronized not needed for transmit
		Vector<PropertyChangeListener> v;
		synchronized(this) {
			v = (Vector<PropertyChangeListener>) propListeners.clone();
		}
		// forward to all listeners
		int cnt = v.size();
		for (int i=0; i < cnt; i++) {
			PropertyChangeListener client = v.elementAt(i);
			client.propertyChange(new PropertyChangeEvent(this, name,Integer.valueOf(oldval),Integer.valueOf(newval)));
		}
	}

	// members for handling the programmer interface

	protected int progState = 0;
	static protected final int NOTPROGRAMMING = 0; // is notProgramming
	static protected final int REQUESTSENT    = 1; // waiting reply to command to go into programming mode
	static protected final int INQUIRESENT    = 2; // read/write command sent, waiting reply
	protected boolean  _progRead = false;
	protected int _val;	// remember the value being read/written for confirmative reply
	protected int _cv;	// remember the cv being read/written

	// programming interface
	synchronized public void writeCV(int CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
		if (log.isDebugEnabled()) log.debug("writeCV "+CV+" listens "+p);
		useProgrammer(p);
		_progRead = false;
		// set new state & save values
		progState = REQUESTSENT;
		_val = val;
		_cv = 0xff & CV;
		
		try {
		   // start the error timer
		   restartTimer(XNetProgrammerTimeout);

		   // format and send message to go to program mode
        	   if (_mode == Programmer.PAGEMODE) {
		       XNetMessage msg = XNetMessage.getWritePagedCVMsg(CV,val);
		       controller().sendXNetMessage(msg, this);
        	   } else if (_mode == Programmer.DIRECTBITMODE || _mode == Programmer.DIRECTBYTEMODE) {
		       XNetMessage msg = XNetMessage.getWriteDirectCVMsg(CV,val);
		       controller().sendXNetMessage(msg, this);
        	   } else  { // register mode by elimination 
		       XNetMessage msg = XNetMessage.getWriteRegisterMsg(registerFromCV(CV),val);
                       controller().sendXNetMessage(msg,this);
		   }
		} catch (jmri.ProgrammerException e) {
		  progState = NOTPROGRAMMING;
		  throw e;
	        }
	}

	synchronized public void confirmCV(int CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
		readCV(CV, p);
	}

	synchronized public void readCV(int CV, jmri.ProgListener p) throws jmri.ProgrammerException {
		if (log.isDebugEnabled()) log.debug("readCV "+CV+" listens "+p);
		// If can't read (e.g. multiMaus CS), this shouldnt be invoked, but
		// still we need to do something rational by returning a NotImplemented error
		if(!getCanRead()) {
			p.programmingOpReply(CV,jmri.ProgListener.NotImplemented);
			return;
		} 
		useProgrammer(p);
		_cv = 0xff & CV;
		_progRead = true;
		// set new state
		progState = REQUESTSENT;
		try {
                  // start the error timer
		   restartTimer(XNetProgrammerTimeout);

		   // format and send message to go to program mode
        	   if (_mode == Programmer.PAGEMODE) {
		       XNetMessage msg=XNetMessage.getReadPagedCVMsg(CV);
		       controller().sendXNetMessage(msg, this);
		   } else if (_mode == Programmer.DIRECTBITMODE || _mode == Programmer.DIRECTBYTEMODE) {
		       XNetMessage msg=XNetMessage.getReadDirectCVMsg(CV);
		       controller().sendXNetMessage(msg, this);
		   } else { // register mode by elimination    
		       XNetMessage msg=XNetMessage.getReadRegisterMsg(registerFromCV(CV));
		       controller().sendXNetMessage(msg, this);
		   }
		} catch (jmri.ProgrammerException e) {
		  progState = NOTPROGRAMMING;
		  throw e;
	        }
	
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

	synchronized public void message(XNetReply m) {
            	if (m.getElement(0)==XNetConstants.CS_INFO && 
                     m.getElement(1)==XNetConstants.BC_SERVICE_MODE_ENTRY) {
                     if(_service_mode == false) {
		        // the command station is in service mode.  An "OK" 
		        // message can trigger a request for service mode 
		        // results if progrstate is REQUESTSENT.
		        _service_mode = true;		   
		     } else if(_service_mode == true) {
                        // Since we get this message as both a broadcast and
                        // a directed message, ignore the message if we're
                        //already in the indicated mode
                        return;
                     }
		}
		if(m.getElement(0)==XNetConstants.CS_INFO &&
		   m.getElement(1)==XNetConstants.BC_NORMAL_OPERATIONS) {
                     if(_service_mode == true) {
		        // the command station is not in service mode.  An 
		        // "OK" message can not trigger a request for service 
		        // mode results if progrstate is REQUESTSENT.
		        _service_mode = false;
		     } else if(_service_mode == false) {
                        // Since we get this message as both a broadcast and
                        // a directed message, ignore the message if we're
                        //already in the indicated mode
                        return;
                     }
		}
		if (progState == NOTPROGRAMMING) {
			// we get the complete set of replies now, so ignore these
			return;

		} else if (progState == REQUESTSENT) {
		   	if (log.isDebugEnabled()) log.debug("reply in REQUESTSENT state");
			// see if reply is the acknowledge of program mode; if not, wait for next
            		if ( (_service_mode && m.isOkMessage()) || 
			     (m.getElement(0)==XNetConstants.CS_INFO && 
                	   (m.getElement(1)==XNetConstants.BC_SERVICE_MODE_ENTRY ||
		            m.getElement(1)==XNetConstants.PROG_CS_READY )) ) {
					if(!getCanRead()) { 
                                         // on systems like the Roco MultiMaus 
                                         // (which does not support reading)
                                         // let a timeout occur so the system
                                         // has time to write data to the 
                                         // decoder
                                                restartTimer(SHORT_TIMEOUT);
						return;
					}
				    
			       // here ready to request the results
			       progState = INQUIRESENT;
                	       //start the error timer
			       restartTimer(XNetProgrammerTimeout);

			       controller().sendXNetMessage(XNetMessage.getServiceModeResultsMsg(),
                                                            this);
                               return;
            		} else if (m.getElement(0)==XNetConstants.CS_INFO && 
                           m.getElement(1)==XNetConstants.CS_NOT_SUPPORTED) {
                           // programming operation not supported by this command station
			   progState = NOTPROGRAMMING;
			   notifyProgListenerEnd(_val, jmri.ProgListener.NotImplemented);
                           return;
            		} else if (m.getElement(0)==XNetConstants.CS_INFO && 
				   m.getElement(1)==XNetConstants.BC_NORMAL_OPERATIONS) {
			   // We Exited Programming Mode early
			   log.error("Service mode exited before sequence complete.");
			   progState = NOTPROGRAMMING;
			   stopTimer();
			   notifyProgListenerEnd(_val, jmri.ProgListener.SequenceError);
            		} else if (m.getElement(0)==XNetConstants.CS_INFO && 
			   m.getElement(1)==XNetConstants.PROG_SHORT_CIRCUIT) {
			   // We experienced a short Circuit on the Programming Track
			   log.error("Short Circuit While Programming Decoder");
			   progState = NOTPROGRAMMING;
			   stopTimer();
			   notifyProgListenerEnd(_val, jmri.ProgListener.ProgrammingShort);
                        } else if(m.isCommErrorMessage()) {
			   // We experienced a communicatiosn error
                           // If this is a Timeslot error, ignore it, 
                           // otherwise report it as an error
                           if(m.getElement(1)==XNetConstants.LI_MESSAGE_RESPONSE_TIMESLOT_ERROR)
                                   return;
			   log.error("Communications error in REQUESTSENT state while programming.  Error: " + m.toString());
			   	progState = NOTPROGRAMMING;
			   stopTimer();
			   notifyProgListenerEnd(_val, jmri.ProgListener.CommError);
			}
		} else if (progState == INQUIRESENT) {
			if (log.isDebugEnabled()) log.debug("reply in INQUIRESENT state");
            		// check for right message, else return
            		if (m.getElement(0)==XNetConstants.CS_SERVICE_MODE_RESPONSE && 
                	    m.getElement(1)==XNetConstants.CS_SERVICE_REG_PAGE_RESPONSE) {
                	    // valid operation response, but does it belong to us?
                            try {
                               // we always save the cv number, but if
                               // we are using register mode, there is
                               // at least one case (CV29) where the value
                               // returned does not match the value we saved. 
			       if(m.getElement(2)!=_cv &&
                                  m.getElement(2)!=registerFromCV(_cv)) {
                                   log.debug(" result for CV " + m.getElement(2) +
                                             " expecting " + _cv);
                                   return;
                               }
                            } catch (jmri.ProgrammerException e) {
                                progState = NOTPROGRAMMING;
			        notifyProgListenerEnd(_val, jmri.ProgListener.UnknownError);
                            }
			    // see why waiting
			    if (_progRead) {
			        // read was in progress - get return value
				_val = m.getElement(3);
			    }
			    progState = NOTPROGRAMMING;
			    stopTimer();
			    // if this was a read, we cached the value earlier.  
			    // If its a write, we're to return the original write value
			    notifyProgListenerEnd(_val, jmri.ProgListener.OK);
                	    return;
            		} else if (m.getElement(0)==XNetConstants.CS_SERVICE_MODE_RESPONSE && 
                		   m.getElement(1)==XNetConstants.CS_SERVICE_DIRECT_RESPONSE) {
                	    // valid operation response, but does it belong to us?
			    if(m.getElement(2)!=_cv) {
                                log.debug(" CV read " + m.getElement(2) +
                                          " expecting " + _cv);
                                return;
                            }

			    // see why waiting
			    if (_progRead) {
				// read was in progress - get return value
				_val = m.getElement(3);
			    }
			    progState = NOTPROGRAMMING;
			    stopTimer();
			    // if this was a read, we cached the value earlier.  If its a
			    // write, we're to return the original write value
			    notifyProgListenerEnd(_val, jmri.ProgListener.OK);
                	    return;
            		} else if (m.getElement(0)==XNetConstants.CS_SERVICE_MODE_RESPONSE && 
                		   (m.getElement(1)&0x14)==(0x14)) {
                	    // valid operation response, but does it belong to us?
                            int sent_cv=(m.getElement(1)&0x03<<2)+m.getElement(2);
			    if(sent_cv!=_cv && (sent_cv==0 && _cv!=0x0400)) return;
			    // see why waiting
			    if (_progRead) {
				// read was in progress - get return value
				_val = m.getElement(3);
			    }
			    progState = NOTPROGRAMMING;
			    stopTimer();
			    // if this was a read, we cached the value earlier.  If its a
			    // write, we're to return the original write value
			    notifyProgListenerEnd(_val, jmri.ProgListener.OK);
                	    return;
            		} else if (m.getElement(0)==XNetConstants.CS_INFO && 
				   m.getElement(1)==XNetConstants.PROG_BYTE_NOT_FOUND) {
                    	   // "data byte not found", e.g. no reply
		    	   progState = NOTPROGRAMMING;
		    	   stopTimer();
		     	   notifyProgListenerEnd(_val, jmri.ProgListener.NoLocoDetected);
               	    	   return;
            		} else if (m.getElement(0)==XNetConstants.CS_INFO && 
				   m.getElement(1)==XNetConstants.BC_NORMAL_OPERATIONS) {
		  	   // We Exited Programming Mode early
		   	   log.error("Service Mode exited before sequence complete.");
		   	   progState = NOTPROGRAMMING;
		   	   stopTimer();
		   	   notifyProgListenerEnd(_val, jmri.ProgListener.SequenceError);
               	    	   return;
            		} else if (m.getElement(0)==XNetConstants.CS_INFO && 
			   m.getElement(1)==XNetConstants.PROG_SHORT_CIRCUIT) {
			   // We experienced a short Circuit on the Programming Track
			   log.error("Short Circuit While Programming Decoder");
			   	progState = NOTPROGRAMMING;
			   stopTimer();
			   notifyProgListenerEnd(_val, jmri.ProgListener.ProgrammingShort);
            		} else if (m.getElement(0)==XNetConstants.CS_INFO && 
				   m.getElement(1)==XNetConstants.PROG_CS_BUSY) {
                           // Command station indicated it was busy in 
                           // programming mode, request results again 
                           // (do not reset timer or change mode)
                           // NOTE: Currently only sent by OpenDCC.
			   controller().sendXNetMessage(XNetMessage.getServiceModeResultsMsg(),
                                                            this);
                           return;
                        } else if(m.isCommErrorMessage()) {
			   // We experienced a communicatiosn error
                           // If this is a Timeslot error, ignore it, 
                           // otherwise report it as an error
                           if(m.getElement(1)==XNetConstants.LI_MESSAGE_RESPONSE_TIMESLOT_ERROR)
                                   return;
			   log.error("Communications error in INQUIRESENT state while programming.  Error: " + m.toString());
			   	progState = NOTPROGRAMMING;
			   stopTimer();
			   notifyProgListenerEnd(_val, jmri.ProgListener.CommError);
			} else {
                           // nothing important, ignore
                           log.debug("Ignoring message " + m.toString());
                           return;
		   	}
	    } else {
		if (log.isDebugEnabled()) log.debug("reply in un-decoded state");
	    }
	}

 	// listen for the messages to the LI100/LI101
    	synchronized public void message(XNetMessage l) {
    	}

        // Handle a timeout notification
        public void notifyTimeout(XNetMessage msg)
        {
           if(log.isDebugEnabled()) log.debug("Notified of timeout on message" + msg.toString());
        }


	/*
         * Since the Lenz programming sequence requires several 
         * operations, We want to be able to check and see if we are 
         * currently programming before allowing the Traffic Controller 
         * to send a request to exit service mode
	 */
	public boolean programmerBusy() {
	    return (progState!=NOTPROGRAMMING);
	} 

	/**
	 * Internal routine to handle a timeout
	 */
        @Override
	synchronized protected void timeout() {
		if (progState != NOTPROGRAMMING) {
			// we're programming, time to stop
			if (log.isDebugEnabled()) log.debug("timeout!");
			// perhaps no loco present? Fail back to end of programming
			progState = NOTPROGRAMMING;
                        if (getCanRead())
			   notifyProgListenerEnd(_val, jmri.ProgListener.FailedTimeout);
                        else 
			   notifyProgListenerEnd(_val, jmri.ProgListener.OK);
		}  
	}

	// internal method to notify of the final result
	protected void notifyProgListenerEnd(int value, int status) {
		if (log.isDebugEnabled()) log.debug("notifyProgListenerEnd value "+value+" status "+status);
		// the programmingOpReply handler might send an immediate reply, so
		// clear the current listener _first_
		jmri.ProgListener temp = _usingProgrammer;
		_usingProgrammer = null;
		temp.programmingOpReply(value, status);
	}

	XNetTrafficController _controller = null;

	protected XNetTrafficController controller() {
		return _controller;
	}

   static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(XNetProgrammer.class.getName());

}


/* @(#)XNetProgrammer.java */
