// SpecificReply.java

package jmri.jmrix.powerline.cm11;

import jmri.jmrix.powerline.X10Sequence;
import jmri.jmrix.powerline.SerialReply;

/**
 * Contains the data payload of a serial reply
 * packet.  Note that its _only_ the payload.
 *
 * @author	Bob Jacobsen  Copyright (C) 2002, 2006, 2007, 2008
 * @version     $Revision: 1.1 $
 */
public class SpecificReply extends jmri.jmrix.powerline.SerialReply {

    // create a new one
    public  SpecificReply() {
        super();
        setBinary(true);
    }
    public SpecificReply(String s) {
        super(s);
        setBinary(true);
    }
    public SpecificReply(SerialReply l) {
        super(l);
        setBinary(true);
    }

    /**
     * Is reply to poll message
     */
    public int getAddr() { 
        log.error("getAddr should not be called");
        new Exception().printStackTrace();
        return getElement(0);
    }

    protected int skipPrefix(int index) {
        // doesn't have to do anything
        return index;
    }

    public String toMonitorString() {
        // check for valid length
        if (getNumDataElements() == 1) {
            String val;
            int msg = getElement(0);
            switch (msg&0xFF) {
                case Constants.POLL_REQ: val = "Data Available\n";break;
                case Constants.TIME_REQ: val = "CP11 time request\n";break;
                case 0xA6: val = "CP10 time request\n";break;
                case 0xF3: val = "Input Filter Failed\n";break;
                case Constants.READY_REQ: val = "Interface Ready\n";break;
                default: val = "One byte, probably CRC\n";break;
            }
            return val;
        } else if ((getNumDataElements() == 2) && ((getElement(1)&0xFF) == Constants.READY_REQ)) {
            return "CRC 0x"+jmri.util.StringUtil.twoHexFromInt(getElement(0))+" and Interface Ready\n";    
        } else if ((getElement(0)&0xFF) == Constants.POLL_REQ ) { 
            // must be received data
            String s = "Receive data, "+(getElement(1)&0xFF)+ " bytes: ";
            int last = (getElement(1)&0xFF)+1;
            int bits = (getElement(2)&0xFF);
            for (int i=3; i<=last; i++) {
                if (i!=3) s+="; ";  // separate all but last command
                if ((bits&0x01) != 0)
                    s+=X10Sequence.formatCommandByte(getElement(i)&0xFF);
                else
                    s+=X10Sequence.formatAddressByte(getElement(i)&0xFF);
                bits = bits>>1;  // shift over before next byte
            }
            return s+"\n";
        } else {
            // don't know, just show
            String s = "Unknown reply of length "+getNumDataElements()+" "+toString();
            return s+"\n";
        }
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SpecificReply.class.getName());

}

/* @(#)SpecificReply.java */
