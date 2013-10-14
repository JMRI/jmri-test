/* LnOpsModeProgrammer.java */

package jmri.jmrix.loconet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.beans.*;

import jmri.*;

/**
 * Provide an Ops Mode Programmer via a wrapper what works with the LocoNet
 * SlotManager object.
 * @see             jmri.Programmer
 * @author			Bob Jacobsen Copyright (C) 2002
 * @version			$Revision$
 */
public class LnOpsModeProgrammer implements Programmer  {

    SlotManager mSlotMgr;
    int mAddress;
    boolean mLongAddr;
    public LnOpsModeProgrammer(SlotManager pSlotMgr,
                               int pAddress, boolean pLongAddr) {
        mSlotMgr = pSlotMgr;
        mAddress = pAddress;
        mLongAddr = pLongAddr;
    }

    /**
     * Forward a write request to an ops-mode write operation
     */
    public void writeCV(int CV, int val, ProgListener p) throws ProgrammerException {
        mSlotMgr.writeCVOpsMode(CV, val, p, mAddress, mLongAddr);
    }

    public void readCV(int CV, ProgListener p) throws ProgrammerException {
        mSlotMgr.readCVOpsMode(CV, p, mAddress, mLongAddr);
    }

    public void confirmCV(int CV, int val, ProgListener p) throws ProgrammerException {
        mSlotMgr.confirmCVOpsMode(CV, val, p, mAddress, mLongAddr);
    }

    public void writeCV(String CV, int val, ProgListener p) throws ProgrammerException {
        writeCV(Integer.parseInt(CV), val, p);
    }
    public void readCV(String CV, ProgListener p) throws ProgrammerException {
        readCV(Integer.parseInt(CV), p);
    }
    public void confirmCV(String CV, int val, ProgListener p) throws ProgrammerException {
        confirmCV(Integer.parseInt(CV), val, p);
    }

    public void setMode(int mode) {
        if (mode!=Programmer.OPSBYTEMODE) {
            reportBadMode(mode);
        }
    }

    void reportBadMode(int mode) {
        log.error("Can't switch to mode "+mode);
    }

    public int  getMode() {
        return Programmer.OPSBYTEMODE;
    }

    public boolean hasMode(int mode) {
        return (mode==Programmer.OPSBYTEMODE);
    }

    /**
     * Can this ops-mode programmer read back values?  Yes,
     * if transponding hardware is present, but we don't check that here.
     * @return always true
     */
    public boolean getCanRead() {
        return true;
    }
    public boolean getCanRead(String addr) { 
        return getCanRead() && Integer.parseInt(addr)<=2048; }
    public boolean getCanRead(int mode, String addr) { return getCanRead(addr); }
    
    public boolean getCanWrite()  { return true; }
    public boolean getCanWrite(String addr) { return Integer.parseInt(addr)<=2048; }
    public boolean getCanWrite(int mode, String addr)  { return getCanWrite(addr); }

    public void addPropertyChangeListener(PropertyChangeListener p) {
        mSlotMgr.addPropertyChangeListener(p);
    }
    public void removePropertyChangeListener(PropertyChangeListener p) {
        mSlotMgr.removePropertyChangeListener(p);
    }

    public String decodeErrorCode(int i) {
        return mSlotMgr.decodeErrorCode(i);
    }
    // initialize logging
    static Logger log = LoggerFactory.getLogger(LnOpsModeProgrammer.class.getName());

}

/* @(#)LnOpsModeProgrammer.java */
