/* XNetOpsModeProgrammer.java */

package jmri.jmrix.lenz;

import java.beans.*;

import jmri.*;

/**
 * Provides an Ops mode programing interface for XPressNet
 * Currently only Byte mode is implemented, though XPressNet also supports
 * bit mode writes for POM
 *
 * @see            jmri.Programmer
 * @author         Paul Bender Copyright (C) 2003
 * @author         Girgio Terdina Copyright (C) 2007
 * @version        $Revision: 2.8 $
*/

public class XNetOpsModeProgrammer implements Programmer,XNetListener 
{

    private int _mode;
    int mAddressHigh;
    int mAddressLow;
    int progState=0;
    int value;
    jmri.ProgListener progListener = null;

    public XNetOpsModeProgrammer(int pAddress) {
	if(log.isDebugEnabled()) log.debug("Creating Ops Mode Programmer for Address " + pAddress);
	mAddressLow=LenzCommandStation.getDCCAddressLow(pAddress);
	mAddressHigh=LenzCommandStation.getDCCAddressHigh(pAddress);
	if(log.isDebugEnabled()) log.debug("High Address: " + mAddressHigh +" Low Address: " +mAddressLow);
        // register as a listener
        XNetTrafficController.instance().addXNetListener(
			XNetInterface.COMMINFO|XNetInterface.CS_INFO,this);
    }

    /**
     * Send an ops-mode write request to the XPressnet.
     */
    public void writeCV(int CV, int val, ProgListener p) throws ProgrammerException {
        XNetMessage msg=XNetMessage.getWriteOpsModeCVMsg(mAddressHigh,mAddressLow,CV,val);
	XNetTrafficController.instance().sendXNetMessage(msg,this);
        /* we need to save the programer and value so we can send messages 
        back to the screen when the programing screen when we recieve 
        something from the command station */
        progListener=p;
        value=val;
        progState=XNetProgrammer.REQUESTSENT;
    }

    public void readCV(int CV, ProgListener p) throws ProgrammerException {
           XNetMessage msg=XNetMessage.getVerifyOpsModeCVMsg(mAddressHigh,mAddressLow,CV,value);
	   XNetTrafficController.instance().sendXNetMessage(msg,this);
           /* We can trigger a read to an LRC120, but the information is not
	      currently sent back to us via the XPressNet */
           p.programmingOpReply(CV,jmri.ProgListener.NotImplemented);
    }

    public void confirmCV(int CV, int val, ProgListener p) throws ProgrammerException {
           XNetMessage msg=XNetMessage.getVerifyOpsModeCVMsg(mAddressHigh,mAddressLow,CV,val);
	   XNetTrafficController.instance().sendXNetMessage(msg,this);
           /* We can trigger a read to an LRC120, but the information is not
	      currently sent back to us via the XPressNet */
           p.programmingOpReply(val,jmri.ProgListener.NotImplemented);
    }

    public void setMode(int mode) {
        if (mode==Programmer.OPSBYTEMODE) {
		_mode=mode;
	} else {
            reportBadMode(mode);
        }
    }

    void reportBadMode(int mode) {
        log.error("Can't switch to mode "+mode);
    }

    public int  getMode() {
        return _mode;
    }

    public boolean hasMode(int mode) {
        return (mode==Programmer.OPSBYTEMODE);
    }

    /**
     * Can this ops-mode programmer read back values?
     * Indirectly we can, though this requires an external display 
     * (a Lenz LRC120) and enabling railcom.
     * @return true to allow us to trigger an ops mode read
     */
    public boolean getCanRead() {
                // An operations mode read can be triggered on command 
                // stations which support Operations Mode Writes (LZ100,
                // LZV100,MultiMouse).  Whether or not the operation produces
                // a result depends on additional external hardware (a booster 
                // with an enabled  RailCom cutout (LV102 or similar) and a 
                // RailCom receiver circuit (LRC120 or similar)).
                // We have no way of determining if the required external 
                // hardware is present, so we return true for all command 
                // stations on which the Operations Mode Programmer is enabled.
		return(true);
    }

    public String decodeErrorCode(int i) {
                    return("");
    }

    public void addPropertyChangeListener(PropertyChangeListener p) {
    }

    public void removePropertyChangeListener(PropertyChangeListener p) {
    }


    synchronized public void message(XNetReply l) {
	if (progState == XNetProgrammer.NOTPROGRAMMING) {
           // We really don't care about any messages unless we send a 
           // request, so just ignore anything that comes in
           return;
        } else if (progState==XNetProgrammer.REQUESTSENT) {
            if(l.isOkMessage()) {
                  progState=XNetProgrammer.NOTPROGRAMMING;
	  	  progListener.programmingOpReply(value,jmri.ProgListener.OK);
	    } else {
              /* this is an error */
              if(l.getElement(0)==XNetConstants.LI_MESSAGE_RESPONSE_HEADER &&
		((l.getElement(1)==XNetConstants.LI_MESSAGE_RESPONSE_UNKNOWN_DATA_ERROR ||
		  l.getElement(1)==XNetConstants.LI_MESSAGE_RESPONSE_CS_DATA_ERROR ||
		  l.getElement(1)==XNetConstants.LI_MESSAGE_RESPONSE_PC_DATA_ERROR ||
		  l.getElement(1)==XNetConstants.LI_MESSAGE_RESPONSE_TIMESLOT_ERROR))) {   
                     /* this is a communications error */
	             progState=XNetProgrammer.NOTPROGRAMMING;
                     progListener.programmingOpReply(value,jmri.ProgListener.FailedTimeout);
	      } else if(l.getElement(0)==XNetConstants.CS_INFO &&
		        l.getElement(2)==XNetConstants.CS_NOT_SUPPORTED) {
	                   progState=XNetProgrammer.NOTPROGRAMMING;
		     	   progListener.programmingOpReply(value,jmri.ProgListener.NotImplemented);
	      } else if(l.getElement(0)==XNetConstants.CS_INFO &&
		        l.getElement(2)==XNetConstants.CS_BUSY) {
	                   progState=XNetProgrammer.NOTPROGRAMMING;
		     	   progListener.programmingOpReply(value,jmri.ProgListener.ProgrammerBusy);
              } else { 
                        /* this is an unknown error */
	                progState=XNetProgrammer.NOTPROGRAMMING;
                   	progListener.programmingOpReply(value,jmri.ProgListener.UnknownError);
              }
            }
	}
    }

    // listen for the messages to the LI100/LI101
    public synchronized void message(XNetMessage l) {
    }


    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XNetOpsModeProgrammer.class.getName());

}

/* @(#)XnetOpsModeProgrammer.java */
