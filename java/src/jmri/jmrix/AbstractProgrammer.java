// AbstractProgrammer.java

package jmri.jmrix;

import jmri.ProgListener;
import jmri.Programmer;
import jmri.ProgrammerException;
import java.beans.PropertyChangeListener;
import java.util.Vector;

/**
 * Common implementations for the Programmer interface.
 *
 * @author	Bob Jacobsen  Copyright (C) 2001, 2012
 * @version     $Revision$
 */
public abstract class AbstractProgrammer implements Programmer {

    public String decodeErrorCode(int code) {
        if (code == ProgListener.OK) return Bundle.getString("StatusOK");
        StringBuffer sbuf = new StringBuffer("");
        // add each code; terminate each string with ";" please.
        if ((code & ProgListener.NoLocoDetected) != 0) sbuf.append(Bundle.getString("NoLocoDetected")+" ");
        if ((code & ProgListener.ProgrammerBusy) != 0) sbuf.append(Bundle.getString("ProgrammerBusy")+" ");
        if ((code & ProgListener.NotImplemented) != 0) sbuf.append(Bundle.getString("NotImplemented")+" ");
        if ((code & ProgListener.UserAborted) != 0) sbuf.append(Bundle.getString("UserAborted")+" ");
        if ((code & ProgListener.ConfirmFailed) != 0) sbuf.append(Bundle.getString("ConfirmFailed")+" ");
        if ((code & ProgListener.FailedTimeout) != 0) sbuf.append(Bundle.getString("FailedTimeout")+" ");
        if ((code & ProgListener.UnknownError) != 0) sbuf.append(Bundle.getString("UnknownError")+" ");
        if ((code & ProgListener.NoAck) != 0) sbuf.append(Bundle.getString("NoAck")+" ");
	    if ((code & ProgListener.ProgrammingShort) != 0) sbuf.append(Bundle.getString("ProgrammingShort")+" ");
	    if ((code & ProgListener.SequenceError) != 0) sbuf.append(Bundle.getString("SequenceError")+" ");
	    if ((code & ProgListener.CommError) != 0) sbuf.append(Bundle.getString("CommError")+" ");

        // remove trailing separators
        if (sbuf.length() > 2) sbuf.setLength(sbuf.length()-2);

        String retval = sbuf.toString();
        if (retval.equals(""))
            return "unknown status code: "+code;
        else return retval;
    }

    // data members to hold contact with the property listeners
    protected Vector<PropertyChangeListener> propListeners = new Vector<PropertyChangeListener>();

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

    /**
     * Internal routine to start timer to protect the mode-change.
     */
    protected void startShortTimer() {
        restartTimer(SHORT_TIMEOUT);
    }

    /**
     * Internal routine to restart timer with a long delay
     */
    protected void startLongTimer() {
        restartTimer(LONG_TIMEOUT);
    }

    /**
     * Internal routine to stop timer, as all is well
     */
    protected synchronized void stopTimer() {
        if (timer!=null) timer.stop();
    }

    /**
     * Internal routine to handle timer starts & restarts
     */
    protected synchronized void restartTimer(int delay) {
        if (log.isDebugEnabled()) log.debug("restart timer with delay "+delay);
        if (timer==null) {
            timer = new javax.swing.Timer(delay, new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        timeout();
                    }
                });
        }
        timer.stop();
        timer.setInitialDelay(delay);
        timer.setRepeats(false);
        timer.start();
    }

    /**
     * Find the register number that corresponds to a specific
     * CV number.
     * @throws ProgrammerException if the requested CV does not correspond
     *          to a register
     * @param cv CV number (1 through 512) for which equivalent register is desired
     * @return register number corresponding to cv
     */

    public int registerFromCV(int cv) throws ProgrammerException {
        if (cv<=4) return cv;
        switch (cv) {
        case 29:
            return 5;
        case 7:
            return 7;
        case 8:
            return 8;
        }
        throw new ProgrammerException();
    }

    /**
     * Internal routine to handle a timeout, should be synchronized!
     */
    abstract protected void timeout();

    protected int SHORT_TIMEOUT=2000;
    protected int LONG_TIMEOUT=60000;

    javax.swing.Timer timer = null;

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractProgrammer.class.getName());

}

