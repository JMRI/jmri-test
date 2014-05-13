// MrcProgrammer.java

package jmri.jmrix.mrc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.Programmer;
import jmri.jmrix.AbstractProgrammer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;
import static jmri.jmrix.mrc.MrcReply.readCVHeaderReply;

/**
 * Convert the jmri.Programmer interface into commands for the MRC power house.
 * <P>
 * This has two states:  NOTPROGRAMMING, and COMMANDSENT.  The transitions
 * to and from programming mode are now handled in the TrafficController code.
 * @author			Bob Jacobsen Copyright (C) 2002
 * @author	Ken Cameron Copyright (C) 2014
 * @auther  Kevin Dickerson Copyright (C) 2014
 * @version     $Revision: 24290 $
 */
public class MrcProgrammer extends AbstractProgrammer implements MrcListener {
	
    protected MrcTrafficController tc;

    public MrcProgrammer(MrcTrafficController tc) {
    	this.tc = tc;
        super.SHORT_TIMEOUT = 4000;
    }

    // handle mode
    protected int _mode = Programmer.PAGEMODE;

    /**
     * Switch to a new programming mode.  Note that MRC 
     * doesn't support the setting of mode, the command station works it out.
     * @param mode The new mode, use values from the jmri.Programmer interface
     */
    public void setMode(int mode) {
        int oldMode = _mode;  // preserve this in case we need to go back
        if (mode != _mode) {
            notifyPropertyChange("Mode", _mode, mode);
            _mode = mode;
        }
        if (!hasMode(_mode)) {
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
        return true;
    }

    public int getMode() { return _mode; }

    @Override
    public boolean getCanRead() {
    	return true;
    }
    /*Need to check this out */
    public boolean getCanWrite(int mode, String cv) {
        return true;
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
            client.propertyChange(new PropertyChangeEvent(this, name, Integer.valueOf(oldval), Integer.valueOf(newval)));
        }
    }

    // members for handling the programmer interface

    int progState = 0;
    static final int NOTPROGRAMMING = 0;// is notProgramming
    static final int READCOMMANDSENT = 2; 	// read command sent, waiting reply
    static final int WRITECOMMANDSENT = 4; // POM write command sent 
    static final int POMCOMMANDSENT = 6;	// ops programming mode, send msg twice
    boolean  _progRead = false;
    int _val;	// remember the value being read/written for confirmative reply
    int _cv;	// remember the cv being read/written

    // programming interface
    public synchronized void writeCV(int CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        log.debug("writeCV {} listens {}", CV, p);
        useProgrammer(p);
        _progRead = false;
        // set state
        progState = WRITECOMMANDSENT;
        _val = val;
        _cv = CV;

        try {
            // start the error timer
            startLongTimer();
            // format and send the write message
            tc.sendMrcMessage(progTaskStart(getMode(), _val, _cv), this);
        } catch (jmri.ProgrammerException e) {
            progState = NOTPROGRAMMING;
            throw e;
        }
    }

    public void confirmCV(int CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        readCV(CV, p);
    }

    public synchronized void readCV(int CV, jmri.ProgListener p) throws jmri.ProgrammerException {
        log.debug("readCV {} listens {}", CV, p);
        useProgrammer(p);
        _progRead = true;

        // set commandPending state
        progState = READCOMMANDSENT;
        _cv = CV;

        try {
            // start the error timer
            startLongTimer();
            
            // format and send the write message
            tc.sendMrcMessage(progTaskStart(getMode(), -1, _cv), this);
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

    // internal method to create the MrcMessage for programmer task start
    /* todo MRC doesn't set the porg mode the command station sorts it out.*/
    protected MrcMessage progTaskStart(int mode, int val, int cvnum) throws jmri.ProgrammerException {
        // val = -1 for read command; mode is direct, etc
        if (val < 0) {
            // read
            return MrcMessage.getReadCV(cvnum);
        } else {
            return MrcMessage.getWriteCV((byte)cvnum, (byte)val);
        }
    }

    public void message(MrcMessage m) {
        log.error("message received unexpectedly: "+m.toString());
    }
    


    public synchronized void reply(MrcReply m) {
        if (progState == NOTPROGRAMMING) {
            // we get the complete set of replies now, so ignore these
            log.debug("reply in NOTPROGRAMMING state");
            return;
        }
        if(m.isUnsolicited() || m.isPollMessage() || m.isPacketInError()){
            //if(MrcReply.startsWith(m, MrcReply.goodCmdRecieved))
            return;
        }
        if(MrcReply.startsWith(m, MrcReply.badCmdRecieved)){
            //Command station rejected packet
            notifyProgListenerEnd(_val, jmri.ProgListener.CommError);
        }
        if(MrcReply.startsWith(m, MrcReply.goodCmdRecieved)){
            //Wait for the confirmation.
            return;
        } else if (MrcReply.startsWith(m, MrcReply.progCmdSent)){
            progState = NOTPROGRAMMING;
            notifyProgListenerEnd(_val, jmri.ProgListener.OK);
        } else if (MrcReply.startsWith(m, MrcReply.readCVHeaderReply) && progState == READCOMMANDSENT) {
            progState = NOTPROGRAMMING;
            //Currently we have no way to know if the write was sucessful or not.
            if (_progRead) {
                log.info("prog Read " + _cv);
                // read was in progress - get return value
                _val = m.value();
            }
            // if this was a read, we retrieved the value above.  If its a
            // write, we're to return the original write value
            log.info("Has value " + _val);
            notifyProgListenerEnd(_val, jmri.ProgListener.OK);
        
        } else {
            log.info("reply in un-decoded state cv:" + _cv + " " + m.toString());
        }
    }

    /**
     * Internal routine to handle a timeout
     */
    protected synchronized void timeout() {
        if (progState != NOTPROGRAMMING) {
            // we're programming, time to stop
            log.debug("timeout!");
            // perhaps no loco present? Fail back to end of programming
            progState = NOTPROGRAMMING;
            cleanup();
            notifyProgListenerEnd(_val, jmri.ProgListener.FailedTimeout);
        }
    }

    // Internal method to cleanup in case of a timeout. Separate routine
    // so it can be changed in subclasses.
    void cleanup() {
    }
    // internal method to notify of the final result
    protected void notifyProgListenerEnd(int value, int status) {
        log.debug("notifyProgListenerEnd value {} status {}", value, status);
        // the programmingOpReply handler might send an immediate reply, so
        // clear the current listener _first_
        jmri.ProgListener temp = _usingProgrammer;
        _usingProgrammer = null;
        temp.programmingOpReply(value, status);
    }

    static Logger log = LoggerFactory.getLogger(MrcProgrammer.class.getName());

}


/* @(#)MrcProgrammer.java */

