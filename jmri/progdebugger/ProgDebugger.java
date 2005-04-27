// ProgDebugger.java

package jmri.progdebugger;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Hashtable;
import java.util.Vector;
import jmri.ProgListener;
import jmri.Programmer;
import jmri.ProgrammerException;

/**
 * Debugging implementation of Programmer interface
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version         $Revision: 1.17 $
 */
public class ProgDebugger implements Programmer  {

    // write CV is recorded for later use
    private int _lastWriteVal = -1;
    private int _lastWriteCv = -1;
    public int lastWrite() { return _lastWriteVal; }
    public int lastWriteCv() { return _lastWriteCv; }

    // write CV values are remembered for later reads
    Hashtable mValues = new Hashtable();

    public String decodeErrorCode(int i) {
        log.debug("decoderErrorCode "+i);
        return "error "+i;
    }

    public void writeCV(int CV, int val, ProgListener p) throws ProgrammerException
    {
        final ProgListener m = p;
        // log out the request
        log.info("write CV: "+CV+" to: "+val+" mode: "+getMode());
        _lastWriteVal = val;
        _lastWriteCv = CV;
        // save for later retrieval
        mValues.put(new Integer(CV), new Integer(val));

        // return a notification via the queue to ensure end
        Runnable r = new Runnable() {
                ProgListener l = m;
                public void run() {
                    log.debug("write CV reply");
                    if (l!=null) l.programmingOpReply(-1, 0); }  // 0 is OK status
            };
        javax.swing.SwingUtilities.invokeLater(r);
    }

    // read CV values
    // note that the hashTable will be used if the CV has been written
    private int _nextRead = 123;
    public void nextRead(int r) { _nextRead = r; }

    private int _lastReadCv = -1;
    public int lastReadCv() { return _lastReadCv; }

    public boolean _confirmOK = false;

    public void confirmCV(int CV, int val, ProgListener p) throws ProgrammerException {
        final ProgListener m = p;
        log.info("confirm CV: "+CV+" mode: "+getMode()+" will read pass: "+_confirmOK);
        _lastReadCv = CV;
        // return a notification via the queue to ensure end
        Runnable r = new Runnable() {
                ProgListener l = m;
                public void run() {
                    log.debug("read CV reply");
                    if (_confirmOK) l.programmingOpReply(_nextRead, ProgListener.OK);
                    else l.programmingOpReply(_nextRead, ProgListener.ConfirmFailed);
                }
            };
        javax.swing.SwingUtilities.invokeLater(r);

    }

    public void readCV(int CV, ProgListener p) throws ProgrammerException {
        final ProgListener m = p;
        _lastReadCv = CV;

        // try to get something from hash table
        Integer saw = ((Integer)mValues.get(new Integer(CV)));
        if (saw!=null) _nextRead = saw.intValue();

        log.info("read CV: "+CV+" mode: "+getMode()+" will read "+_nextRead);

        // return a notification via the queue to ensure end
        Runnable r = new Runnable() {
                ProgListener l = m;
                public void run() {
                    // log.debug("read CV reply - start sleep");
                    // try { Thread.sleep(100); } catch (Exception e) {}
                    log.debug("read CV reply");
                    l.programmingOpReply(_nextRead, 0); }  // 0 is OK status
            };
        javax.swing.SwingUtilities.invokeLater(r);

    }

    // handle mode
    protected int _mode = 0;

    public void setMode(int mode) {
        log.debug("setMode: old="+_mode+" new="+mode);
        if (mode != _mode) {
            notifyPropertyChange("Mode", _mode, mode);
            _mode = mode;
        }
    }
    public int getMode() { return _mode; }
    public boolean hasMode(int mode) {
        log.debug("pretending to have mode "+mode);
        return true;
    }

    public boolean getCanRead() { return true; }

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
        synchronized(this)
            {
                v = (Vector) propListeners.clone();
            }
        // forward to all listeners
        int cnt = v.size();
        for (int i=0; i < cnt; i++) {
            PropertyChangeListener client = (PropertyChangeListener) v.elementAt(i);
            client.propertyChange(new PropertyChangeEvent(this, name, new Integer(oldval), new Integer(newval)));
        }
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ProgDebugger.class.getName());
}

/* @(#)ProgDebugger.java */
