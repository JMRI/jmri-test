// MrcReply.java

package jmri.jmrix.mrc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Carries the reply to an MrcMessage.
 * <p>
 * Some of the message formats used in this class are Copyright MRC, Inc.
 * and used with permission as part of the JMRI project.  That permission
 * does not extend to uses in other software products.  If you wish to
 * use this code, algorithm or these message formats outside of JMRI, please
 * contact MRC Inc for separate permission.
 * <p>
 * @author		Bob Jacobsen  Copyright (C) 2001, 2004
 * @author      Kevin Dickerson    Copyright (C) 2014
 * @author		kcameron Copyright (C) 2014
 * @version             $Revision$
 */
public class MrcReply extends jmri.jmrix.AbstractMRReply {

    // create a new one
    public  MrcReply() {
        super();
    }
    public MrcReply(String s) {
        super(s);
    }
    public MrcReply(MrcReply l) {
        super(l);
    }

    protected int skipPrefix(int index) {
		// start at index, passing any whitespace & control characters at the start of the buffer
		while (index < getNumDataElements()-1 &&
			((char)getElement(index) <= ' '))
				index++;
		return index;
    }

    /**
     * Extracts Read-CV returned value from a message.  Returns
     * -1 if message can't be parsed or is in error. Expects a message of the
     * formnat "xx00xx00" where xx is the hexadecimal cv repeated
     * if the second xx doesn't match the first then it is in error
     */
	public int value() {
        int val = -1;
        if(startsWith(this, readCVHeaderReply)){
            if(getElement(4)==getElement(6))
                val = getElement(4)&0xff;
            else
                log.error("Error in format of the returned CV value");
        } else {
            log.error("Not a CV Read formated packet");
        }
		return val;
	}
    
    boolean poll = false;
    
    public void setPollMessage(){
        poll = true;
    }
    
    public boolean isPollMessage(){return poll;}
    
    final protected static int[] readCVHeaderReply = new int[]{0x66,0x00,0x66,0x00};
    final protected static int[] badCmdRecieved = new int[]{0xEE,0x00,0xEE,0x00};
    final protected static int[] goodCmdRecieved = new int[]{0x55,0x00,0x55,0x00};
    final protected static int[] progCmdSent = new int[]{0x33,0x00,0x33,0x00};
    
    final protected static int[] locoSoleControl = new int[]{0x22,0x00,0x22,0x00};  //Reply indicates that we are the sole controller of the loco
    final protected static int[] locoDblControl = new int[]{0xDD,0x00,0xDD,0x00};  //Reply indicates that another throttle also has controll of the loco

    
    public static boolean startsWith(jmri.jmrix.AbstractMRReply source, int[] match) {
        if (match.length > (source.getNumDataElements())) {
            return false;
        }

        for (int i = 0; i < match.length; i++) {
            if (source.getElement(i) != match[i]) {
                return false;
            }
        }
        return true;
    }
    
    public boolean isRetransmittableErrorMsg() {
        if(startsWith(this, badCmdRecieved)){
        //if(getElement(0)==0xee && getElement(1)==0x00 && getElement(2)==0xee && getElement(3) ==0x00)
            return true;
        }
        return false;
    }
    
    static public final int DEFAULTMAXSIZE = 20;
    
    static Logger log = LoggerFactory.getLogger(MrcReply.class.getName());

}


/* @(#)MrcReply.java */
