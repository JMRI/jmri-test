//EasyDccProgrammer.java

package jmri.jmrix.easydcc;

import org.apache.log4j.Logger;
import jmri.Programmer;
import jmri.jmrix.AbstractProgrammer;
import java.util.Vector;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/**
 * Implements the jmri.Programmer interface via commands for the EasyDcc powerstation
 *
 * @author			Bob Jacobsen  Copyright (C) 2001
 * @version			$Revision$
 */
public class EasyDccProgrammer extends AbstractProgrammer implements EasyDccListener {

    public EasyDccProgrammer() {
        // need a longer LONG_TIMEOUT
        LONG_TIMEOUT=180000;
    }

    // handle mode
    protected int _mode = Programmer.PAGEMODE;

    /**
     * Switch to a new programming mode.  Note that EasyDCC can only
     * do register and page mode. If you attempt to switch to
     * any others, the new mode will set & notify, then
     * set back to the original.  This lets the listeners
     * know that a change happened, and then was undone.
     * @param mode The new mode, use values from the jmri.Programmer interface
     */
    public void setMode(int mode) {
        int oldMode = _mode;  // preserve this in case we need to go back
        if (mode != _mode) {
            notifyPropertyChange("Mode", _mode, mode);
            _mode = mode;
        }
        if (_mode != Programmer.PAGEMODE && _mode != Programmer.REGISTERMODE) {
            // attempt to switch to unsupported mode, switch back to previous
            _mode = oldMode;
            notifyPropertyChange("Mode", mode, _mode);
        }
    }
    /**
     * Signifies mode's available
     * @param mode
     * @return True if paged or register mode
     */
    public boolean hasMode(int mode) {
        if ( mode == Programmer.PAGEMODE ||
             mode == Programmer.REGISTERMODE ) {
            log.debug("hasMode request on mode "+mode+" returns true");
            return true;
        }
        log.debug("hasMode returns false on mode "+mode);
        return false;
    }
    public int getMode() { return _mode; }

    public boolean getCanRead() { return true; }

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
            client.propertyChange(new PropertyChangeEvent(this, name, Integer.valueOf(oldval), Integer.valueOf(newval)));
        }
    }

    // members for handling the programmer interface

    int progState = 0;
    static final int NOTPROGRAMMING = 0;// is notProgramming
    static final int COMMANDSENT = 2; 	// read/write command sent, waiting reply
    boolean  _progRead = false;
    int _val;	// remember the value being read/written for confirmative reply
    int _cv;	// remember the cv being read/written

    // programming interface
    public synchronized void writeCV(int CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        if (log.isDebugEnabled()) log.debug("writeCV "+CV+" listens "+p);
        useProgrammer(p);
        _progRead = false;
        // set commandPending state
        progState = COMMANDSENT;
        _val = val;
        _cv = CV;

        try {
            // start the error timer
            startLongTimer();

            // format and send the write message
            controller().sendEasyDccMessage(progTaskStart(getMode(), _val, _cv), this);
        } catch (jmri.ProgrammerException e) {
            progState = NOTPROGRAMMING;
            throw e;
        }
    }

    public synchronized void confirmCV(int CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        readCV(CV, p);
    }

    public synchronized void readCV(int CV, jmri.ProgListener p) throws jmri.ProgrammerException {
        if (log.isDebugEnabled()) log.debug("readCV "+CV+" listens "+p);
        useProgrammer(p);
        _progRead = true;

        progState = COMMANDSENT;
        _cv = CV;

        try {
            // start the error timer
            startLongTimer();

            // format and send the write message
            controller().sendEasyDccMessage(progTaskStart(getMode(), -1, _cv), this);
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
            if (log.isDebugEnabled()) log.debug("programmer already in use by "+_usingProgrammer);
            throw new jmri.ProgrammerException("programmer in use");
        }
        else {
            _usingProgrammer = p;
            return;
        }
    }

    // internal method to create the EasyDccMessage for programmer task start
    protected EasyDccMessage progTaskStart(int mode, int val, int cvnum) throws jmri.ProgrammerException {
        // val = -1 for read command; mode is direct, etc
        if (val < 0) {
            // read
            if (_mode == Programmer.PAGEMODE)
                return EasyDccMessage.getReadPagedCV(cvnum);
            else
                return EasyDccMessage.getReadRegister(registerFromCV(cvnum));
        } else {
            // write
            if (_mode == Programmer.PAGEMODE)
                return EasyDccMessage.getWritePagedCV(cvnum, val);
            else
                return EasyDccMessage.getWriteRegister(registerFromCV(cvnum), val);
        }
    }

    public void message(EasyDccMessage m) {
        log.error("message received unexpectedly: "+m.toString());
    }

    synchronized public void reply(EasyDccReply m) {
        if (progState == NOTPROGRAMMING) {
            // we get the complete set of replies now, so ignore these
            if (log.isDebugEnabled()) log.debug("reply in NOTPROGRAMMING state");
            return;
        } else if (progState == COMMANDSENT) {
            if (log.isDebugEnabled()) log.debug("reply in COMMANDSENT state");
            // operation done, capture result, then have to leave programming mode
            progState = NOTPROGRAMMING;
            // check for errors
            if (m.match("--") >= 0) {
                if (log.isDebugEnabled()) log.debug("handle error reply "+m);
                // perhaps no loco present? Fail back to end of programming
                notifyProgListenerEnd(-1, jmri.ProgListener.NoLocoDetected);
            } else {
                // see why waiting
                if (_progRead) {
                // read was in progress - get return value
                    _val = m.value();
                }
                // if this was a read, we retreived the value above.  If its a
                // write, we're to return the original write value
                notifyProgListenerEnd(_val, jmri.ProgListener.OK);
            }
        }
    }

    /**
     * Internal routine to handle a timeout
     */
    synchronized protected void timeout() {
        if (progState != NOTPROGRAMMING) {
            // we're programming, time to stop
            if (log.isDebugEnabled()) log.debug("timeout!");
            // perhaps no loco present? Fail back to end of programming
            progState = NOTPROGRAMMING;
            cleanup();
            notifyProgListenerEnd(_val, jmri.ProgListener.FailedTimeout);
        }
    }

    /**
     * Internal method to send a cleanup message (if needed) on timeout.
     * <P>
     * Here, it sends a request to exit from programming mode.  But
     * subclasses, e.g. ops mode, may redefine that.
     */
    void cleanup() {
        controller().sendEasyDccMessage(EasyDccMessage.getExitProgMode(), this);
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

    EasyDccTrafficController _controller = null;

    protected EasyDccTrafficController controller() {
        // connect the first time
        if (_controller == null) {
            _controller = EasyDccTrafficController.instance();
        }
        return _controller;
    }

    static Logger log = Logger.getLogger(EasyDccProgrammer.class.getName());

}


/* @(#)EasyDccProgrammer.java */
