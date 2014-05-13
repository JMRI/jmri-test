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
            if(getElement(4)==getElement(6)){
                val = getElement(4)&0xff;
                log.info("good reply " + val);
            }
            else
                log.error("Error in format of the returned CV value");
        } else {
            log.info(toString());
            log.error("Not a CV Read formated packet");
        }
		return val;
	}
    
    boolean packetInError = false;
    
    public void setPacketInError(){
        packetInError = true;
    }
    
    public boolean isPacketInError(){
        return packetInError;
    }
    boolean poll = false;
    
    public void setPollMessage(){
        poll = true;
    }
    
    public boolean isPollMessage(){return poll;}
    
    public final static int readCVHeaderReplyCode = 0x66;
    final static int[] readCVHeaderReply = new int[]{readCVHeaderReplyCode,0x00,readCVHeaderReplyCode,0x00};
    final static int readCVPacketLength = 4;//need to double check the length of this packet
    public static int getReadCVPacketReplyLength() { return readCVHeaderReply.length+readCVPacketLength; }
    
    public final static int badCmdRecievedCode = 0xEE;
    final static int[] badCmdRecieved = new int[]{badCmdRecievedCode,0x00,badCmdRecievedCode,0x00};
    public final static int goodCmdRecievedCode = 0x55;
    final static int[] goodCmdRecieved = new int[]{goodCmdRecievedCode,0x00,goodCmdRecievedCode,0x00};
    public final static int progCmdSentCode = 0x33;
    final static int[] progCmdSent = new int[]{progCmdSentCode,0x00,progCmdSentCode,0x00};

    public final static int locoSoleControlCode = 0x22;
    final static int[] locoSoleControl = new int[]{locoSoleControlCode,0x00,locoSoleControlCode,0x00};  //Reply indicates that we are the sole controller of the loco
    public final static int locoDblControlCode = 0xDD;
    final static int[] locoDblControl = new int[]{locoDblControlCode,0x00,locoDblControlCode,0x00};  //Reply indicates that another throttle also has controll of the loco
    
    public static boolean startsWith(jmri.jmrix.AbstractMRReply source, int[] match) {
        if (match.length > (source.getNumDataElements())) {
            return false;
        }
        for (int i = 0; i < match.length; i++) {
            if ((source.getElement(i)&0xff) != (match[i]&0xff)) {
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
    
    //Principle last two ar ehte checksum, the first four indicate the packet type and ignored.
    //the rest should be XOR'd.
    public boolean validCheckSum(){
        if(getNumDataElements()>6){
            int result = 0x00;
            for(int i = 4; i<getNumDataElements()-2; i++){
                result = (getElement(i)&0xff)^result;
            }
            if(result==(getElement(getNumDataElements()-2)&0xff)) return true;
        }
        return false;
    }
    
    static public final int DEFAULTMAXSIZE = 20;
    
    /**
     * 
     */
    public String toString() {
    	StringBuilder txt = new StringBuilder();
        if((getNumDataElements() <4) || (getNumDataElements()>=4 && getElement(0)!=getElement(2) && getElement(1)!=0x01)){
            //byte 0 and byte 2 should always be the same except for a clock update packet.
        	if (getNumDataElements() < 4) {
        		txt.append("Short Packet");
        	} else {
        		txt.append("Error in Packet");
        	}
            for (int i=0;i<getNumDataElements(); i++) {
                txt.append(" ");
                txt.append(jmri.util.StringUtil.twoHexFromInt(getElement(i)&0xFF));
            }
        } else {
            switch (getElement(0)) {
            case readCVHeaderReplyCode:
                txt.append("Read CV");
                break;
            case badCmdRecievedCode:
                txt.append("Bad Cmd Ack");
                break;
            case goodCmdRecievedCode:
                txt.append("Good Cmd Ack");
                break;
            case progCmdSentCode:
                txt.append("Pgm Cmd Sent");
                break;
            case locoSoleControlCode:
                txt.append("Single Throttle");
                break;
            case locoDblControlCode:
                txt.append("Multiple Throttle");
                break;
            default:
                if(getNumDataElements()==6){
                    if(getElement(0)==0x00 && getElement(1)==0x01){
                        txt.append("Clock Update");
                        break;
                    } else if(getElement(1)==0x01){
                        txt.append("Poll to Cab " + jmri.util.StringUtil.twoHexFromInt(getElement(0)&0xFF));
                        break;
                    }
                }
                if(getNumDataElements()==4){
                    if(getElement(0)==0x00 && getElement(1)==0x00 && getElement(2)==0x00 && getElement(3)==0x00){
                        txt.append("Cab - No Data To Send");
                        break;
                    }
                }
                txt.append("Unk Code");
                for (int i=0;i<getNumDataElements(); i++) {
                    txt.append(" ");
                    txt.append(jmri.util.StringUtil.twoHexFromInt(getElement(i)&0xFF));
                }
                break;
            }
        }
		return txt.toString();
    }
    
    static Logger log = LoggerFactory.getLogger(MrcReply.class.getName());

}


/* @(#)MrcReply.java */
