/* NceOpsModeProgrammer.java */

package jmri.jmrix.nce;

import jmri.*;

/**
 * Provide an Ops Mode Programmer via a wrapper what works with the NCE command
 * station object.
 * <P>
 * Functionally, this just creates packets to send via the command station.
 *
 * @see             jmri.Programmer
 * @author			Bob Jacobsen Copyright (C) 2002
 * @version			$Revision: 1.11.2.1 $
 */
public class NceOpsModeProgrammer extends NceProgrammer  {

    int mAddress;
    boolean mLongAddr;
    NceTrafficController tc;
    
    public NceOpsModeProgrammer(NceTrafficController tc, int pAddress, boolean pLongAddr) {
    	super(tc);
    	this.tc = tc;
        log.debug("NCE ops mode programmer "+pAddress+" "+pLongAddr);
        mAddress = pAddress;
        mLongAddr = pLongAddr;
    }

    /**
     * Forward a write request to an ops-mode write operation
     */
    public synchronized void writeCV(int CV, int val, ProgListener p) throws ProgrammerException {
        if (log.isDebugEnabled()) log.debug("write CV="+CV+" val="+val);
        NceMessage msg;
        // USB can't send a NMRA packet, must use new ops mode command
        if (tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_POWERCAB
				|| tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_SB3) {
        	int locoAddr = mAddress;
        	if (mLongAddr)
        		locoAddr += 0xC000;
        	byte[] bl = NceBinaryCommand.usbOpsModeLoco(locoAddr, CV, val);
        	msg = NceMessage.createBinaryMessage(bl);

		} else {
			// create the message and fill it,
			byte[] contents = NmraPacket.opsCvWriteByte(mAddress, mLongAddr,
					CV, val);
			msg = NceMessage.sendPacketMessage(contents, 5);	// retry 5 times
		}
        // record state. COMMANDSENT is just waiting for a reply...
        useProgrammer(p);
        _progRead = false;
        progState = COMMANDSENT_2;
        _val = val;
        _cv = CV;

        // start the error timer
        startShortTimer();

        // send it twice (2x5) so NCE CS will send at least two consecutive commands to decoder
        controller().sendNceMessage(msg, this);
        controller().sendNceMessage(msg, this);
    }

    public synchronized void readCV(int CV, ProgListener p) throws ProgrammerException {
        if (log.isDebugEnabled()) log.debug("read CV="+CV);
        log.error("readCV not available in this protocol");
        throw new ProgrammerException();
    }

    public synchronized void confirmCV(int CV, int val, ProgListener p) throws ProgrammerException {
        if (log.isDebugEnabled()) log.debug("confirm CV="+CV);
        log.error("confirmCV not available in this protocol");
        throw new ProgrammerException();
    }
    
    // add 200mSec between commands, so NCE command station queue doesn't get overrun
    protected void notifyProgListenerEnd(int value, int status) {
    	if (log.isDebugEnabled()) log.debug("NceOpsModeProgrammer adds 200mSec delay to response");
		try{
			wait(200);
		}catch (InterruptedException e){
			log.debug("unexpected exception "+e);
		}
    	super.notifyProgListenerEnd(value, status);
    }

    public void setMode(int mode) {
        if (mode!=Programmer.OPSBYTEMODE)
            log.error("Can't switch to mode "+mode);
    }

    public int  getMode() {
        return Programmer.OPSBYTEMODE;
    }

    public boolean hasMode(int mode) {
        return (mode==Programmer.OPSBYTEMODE);
    }

    /**
     * Can this ops-mode programmer read back values?  For now, no,
     * but maybe later.
     * @return always false for now
     */
    public boolean getCanRead() {
        return false;
    }


    /**
     * Ops-mode programming doesn't put the command station in programming
     * mode, so we don't have to send an exit-programming command at end.
     * Therefore, this routine does nothing except to replace the parent
     * routine that does something.
     */
    void cleanup() {
    }

    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(NceOpsModeProgrammer.class.getName());

}

/* @(#)NceOpsModeProgrammer.java */
