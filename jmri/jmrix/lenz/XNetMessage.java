// XNetMessage.java

package jmri.jmrix.lenz;

import java.io.Serializable;

/**
 * Represents a single command or response on the XpressNet.
 *<P>
 * Content is represented with ints to avoid the problems with
 * sign-extension that bytes have, and because a Java char is
 * actually a variable number of bytes in Unicode.
 *
 * @author			Bob Jacobsen  Copyright (C) 2002
 * @author			Paul Bender  Copyright (C) 2003-2009
 * @version			$Revision: 2.16 $
 *
 */
public class XNetMessage extends jmri.jmrix.AbstractMRMessage implements Serializable {

	static protected int _nRetries = 5;
	
	/* According to the specification, XPressNet has a maximum timing 
	   interval of 500 milliseconds durring normal communications */
	static private final int XNetProgrammingTimeout = 10000;
	static private int XNetMessageTimeout = 5000;


	/** Create a new object, representing a specific-length message.
	 * @param len Total bytes in message, including opcode and error-detection byte.
	 */
	public XNetMessage(int len) {
        super(len);
	setBinary(true);
	setRetries(_nRetries);
	setTimeout(XNetMessageTimeout);
        if (len>15||len<0) log.error("Invalid length in ctor: "+len);
	_nDataChars=len;
	}

	/** Create a new object, that is a copy of an existing message.
	 * @param message existing message.
	 */
	public XNetMessage(XNetMessage message) {
           super(message);
	   setBinary(true);
	   setRetries(_nRetries);
	   setTimeout(XNetMessageTimeout);
	}

	/**
	 * Create an XNetMessage from an XNetReply.
    	 */
	public XNetMessage(XNetReply message) {
	    super(message.getNumDataElements());                            
       	    setBinary(true);
	    setRetries(_nRetries);
	    setTimeout(XNetMessageTimeout);
            for(int i=0;i<message.getNumDataElements();i++)
       		{
          	   setElement(i,message.getElement(i));
       		}
    	}

    // note that the opcode is part of the message, so we treat it
    // directly
    // WARNING: use this only with opcodes that have a variable number 
    // of arguments following included. Otherwise, just use setElement
	public void setOpCode(int i) {
        if (i>0xF || i<0) {
            log.error("Opcode invalid: "+i);
        }
        setElement(0,((i*16)&0xF0)|((getNumDataElements()-2)&0xF));
    }

	public int getOpCode() {return (getElement(0)/16)&0xF;}

	/** Get a String representation of the op code in hex */
	public String getOpCodeHex() { return "0x"+Integer.toHexString(getOpCode()); }

	/**
	 * check whether the message has a valid parity
	 */
	public boolean checkParity() {
		int len = getNumDataElements();
		int chksum = 0x00;  /* the seed */
   		int loop;

    	for(loop = 0; loop < len-1; loop++) {  // calculate contents for data part
        	chksum ^= getElement(loop);
        }
		return ((chksum&0xFF) == getElement(len-1));
	}

    public void setParity() {
		int len = getNumDataElements();
		int chksum = 0x00;  /* the seed */
   		int loop;

    	for(loop = 0; loop < len-1; loop++) {  // calculate contents for data part
        	chksum ^= getElement(loop);
        }
		setElement(len-1, chksum&0xFF);
    }

    /** Get an integer representation of a BCD value */
	public Integer getElementBCD(int n) { return Integer.decode(Integer.toHexString(getElement(n))); }

    /**   
      * return the message length 
      */
      public int length() { return _nDataChars; }


     /** changing the default number of retries for an XPressNet 
      * message 
      * @param r number of retries to attempt.
      */
     static public void setXNetMessageRetries(int t){
        _nRetries = t;
     }

     /** changing the default timeout for an XPressNet message 
      * @param t Timeout in milliseconds
      */
     static public void setXNetMessageTimeout(int t){
        XNetMessageTimeout = t;
     }

    // decode messages of a particular form

    // create messages of a particular form

    /* 
     * The next group of routines are used by Feedback and/or turnout 
     * control code.  These are used in multiple places within the code, 
     * so they appear here. 
     */

    /**
     * Generate a message to change turnout state
     */
    public static XNetMessage getTurnoutCommandMsg(int pNumber, boolean pClose,
                                            boolean pThrow, boolean pOn) {
        XNetMessage l = new XNetMessage(4);
        l.setElement(0, XNetConstants.ACC_OPER_REQ);
        
        // compute address byte fields
        int hiadr = (pNumber-1)/4;
        int loadr = ((pNumber-1)-hiadr*4)*2;
        // The MSB of the upper nibble is required to be set on
        // The rest of the upper nibble should be zeros.
        // The MSB of the lower nibble says weather or not the
        // accessory line should be "on" or off
        if (!pOn) { loadr |= 0x80; }
	else loadr |= 0x88;
        // If we are sending a "throw" command, we set the LSB of the 
        // lower nibble on, otherwise, we leave it off.
        if (pThrow) loadr |= 0x01;
        
        // we don't know how to command both states right now!
        if (pClose & pThrow)
            log.error("XPressNet turnout logic can't handle both THROWN and CLOSED yet");
        // store and send
        l.setElement(1,hiadr);
        l.setElement(2,loadr);
        
        return l;
    }

    /**
     * Generate a message to recieve the feedback information for an upper 
     * or lower nibble of the feedback address in question
     */
    public static XNetMessage getFeedbackRequestMsg(int pNumber, 
                                             boolean pLowerNibble) {
        XNetMessage l = new XNetMessage(4);
        l.setElement(0, XNetConstants.ACC_INFO_REQ);
        
        // compute address byte field
        l.setElement(1,(pNumber-1)/4);
        // The MSB of the upper nibble is required to be set on
        // The rest of the upper nibble should be zeros.
        // The LSB of the lower nibble says weather or not the
        // information request is for the upper or lower nibble.
        if (pLowerNibble) { l.setElement(2,0x80);
	} else { l.setElement(2,0x81); }
        
        return l;
    }
    
    /* 
     * Next, we have some messages related to sending programing commands.
     */
    public static XNetMessage getServiceModeResultsMsg() {
        XNetMessage m = new XNetMessage(3);
	m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
	m.setTimeout(XNetProgrammingTimeout);
        m.setElement(0, XNetConstants.CS_REQUEST);
        m.setElement(1, XNetConstants.SERVICE_MODE_CSRESULT);
        return m;
    }
    
    public static XNetMessage getExitProgModeMsg() {
        XNetMessage m = new XNetMessage(3);
	m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        m.setElement(0, XNetConstants.CS_REQUEST);
        m.setElement(1, XNetConstants.RESUME_OPS);
	m.setParity();
        return m;
    }
    
    public static XNetMessage getReadPagedCVMsg(int cv) {
        XNetMessage m = new XNetMessage(4);
	m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
	m.setTimeout(XNetProgrammingTimeout);
        m.setElement(0, XNetConstants.PROG_READ_REQUEST);
        m.setElement(1, XNetConstants.PROG_READ_MODE_PAGED);
        m.setElement(2,  (0xff & cv));
        return m;
    }
    
    public static XNetMessage getReadDirectCVMsg(int cv) {
        XNetMessage m = new XNetMessage(4);
	m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
	m.setTimeout(XNetProgrammingTimeout);
        m.setElement(0, XNetConstants.PROG_READ_REQUEST);
        if(cv<0x0100)      /* Use the version 3.5 command for CVs <= 256 */
          m.setElement(1, XNetConstants.PROG_READ_MODE_CV);
        else if(cv==0x0400) /* For CV1024, we need to send the version 3.6
                               command for CVs 1 to 256, sending a 0 for the
                               CV */
          m.setElement(1,0x18);
        else               /* and the version 3.6 command for CVs > 256 */
          m.setElement(1,0x18|((cv&0x0300)>>8));
        m.setElement(2, (0xff &cv));
        return m;
    }
    
    public static XNetMessage getWritePagedCVMsg(int cv, int val) {
        XNetMessage m = new XNetMessage(5);
	m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
	m.setTimeout(XNetProgrammingTimeout);
        m.setElement(0, XNetConstants.PROG_WRITE_REQUEST);
        m.setElement(1, XNetConstants.PROG_WRITE_MODE_PAGED);
        m.setElement(2, (0xff & cv));
        m.setElement(3, val);
        return m;
    }
    
    public static XNetMessage getWriteDirectCVMsg(int cv, int val) {
        XNetMessage m = new XNetMessage(5);
	m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
	m.setTimeout(XNetProgrammingTimeout);
        m.setElement(0, XNetConstants.PROG_WRITE_REQUEST);
        if(cv<0x0100)          /* Use the version 3.5 command for CVs <= 256 */
          m.setElement(1, XNetConstants.PROG_WRITE_MODE_CV);
        else if(cv==0x0400) /* For CV1024, we need to send the version 3.6
                               command for CVs 1 to 256, sending a 0 for the
                               CV */
          m.setElement(1,0x1c);
        else                   /* and the version 3.6 command for CVs > 256 */
          m.setElement(1,0x1c|((cv&0x0300)>>8));
        m.setElement(2, (0xff & cv));
        m.setElement(3, val);
        return m;
    }
    
    public static XNetMessage getReadRegisterMsg(int reg) {
        if (reg>8) log.error("register number too large: "+reg);
        XNetMessage m = new XNetMessage(4);
	m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
	m.setTimeout(XNetProgrammingTimeout);
        m.setElement(0, XNetConstants.PROG_READ_REQUEST);
        m.setElement(1, XNetConstants.PROG_READ_MODE_REGISTER);
        m.setElement(2, (0x0f & reg));
        return m;
    }
    
    public static XNetMessage getWriteRegisterMsg(int reg, int val) {
        if (reg>8) log.error("register number too large: "+reg);
        XNetMessage m = new XNetMessage(5);
	m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
	m.setTimeout(XNetProgrammingTimeout);
        m.setElement(0, XNetConstants.PROG_WRITE_REQUEST);
        m.setElement(1, XNetConstants.PROG_WRITE_MODE_REGISTER);
        m.setElement(2, (0x0f & reg));
        m.setElement(3, val);
        return m;
    }

    public static XNetMessage getWriteOpsModeCVMsg(int AH,int AL,int cv, int val) {
        XNetMessage m = new XNetMessage(8);
        m.setElement(0, XNetConstants.OPS_MODE_PROG_REQ);
        m.setElement(1, XNetConstants.OPS_MODE_PROG_WRITE_REQ);
        m.setElement(2, AH);
        m.setElement(3, AL);
        /* Element 4 is 0xEC + the upper two  bits of the 10 bit CV address.
        NOTE: This is the track packet CV, not the human readable CV, so 
        it's value actually is one less than what we normally think of it as.*/
        int temp=(cv -1) & 0x0300;
        temp=temp/0x00FF;
        m.setElement(4,0xEC+temp);
        /* Element 5 is the lower 8 bits of the cv */
        m.setElement(5, ((0x00ff & cv)-1));
        m.setElement(6, val);
        return m;
    }

    public static XNetMessage getVerifyOpsModeCVMsg(int AH,int AL,int cv,int val) {
        XNetMessage m = new XNetMessage(8);
        m.setElement(0, XNetConstants.OPS_MODE_PROG_REQ);
        m.setElement(1, XNetConstants.OPS_MODE_PROG_WRITE_REQ);
        m.setElement(2, AH);
        m.setElement(3, AL);
        /* Element 4 is 0xE4 + the upper two  bits of the 10 bit CV address.
        NOTE: This is the track packet CV, not the human readable CV, so 
        it's value actually is one less than what we normally think of it as.*/
        int temp=(cv -1) & 0x0300;
        temp=temp/0x00FF;
        m.setElement(4,0xE4+temp);
        /* Element 5 is the lower 8 bits of the cv */
        m.setElement(5, ((0x00ff & cv)-1));
        m.setElement(6, val);
        return m;
    }

    public static XNetMessage getBitWriteOpsModeCVMsg(int AH,int AL,int cv, int bit, boolean value) {
        XNetMessage m = new XNetMessage(8);
        m.setElement(0, XNetConstants.OPS_MODE_PROG_REQ);
        m.setElement(1, XNetConstants.OPS_MODE_PROG_WRITE_REQ);
        m.setElement(2, AH);
        m.setElement(3, AL);
        /* Element 4 is 0xE8 + the upper two  bits of the 10 bit CV address.
        NOTE: This is the track packet CV, not the human readable CV, so 
        it's value actually is one less than what we normally think of it as.*/
        int temp=(cv -1) & 0x0300;
        temp=temp/0x00FF;
        m.setElement(4,0xE8+temp);
        /* Element 5 is the lower 8 bits of the cv */
        m.setElement(5, ((0x00ff & cv)-1));
	/* Since this is a bit write, Element 6 is:
		0xE0 +
		bit 3 is the value to write
		bit's 0-2 are the location of the bit we are changing */
	if(value == true)
        	m.setElement(6, ((0xe8) | (bit & 0xff)) );
	else // value == false
        	m.setElement(6, ((0xe0) | (bit & 0xff)) );
        return m;
    }

    public static XNetMessage getBitVerifyOpsModeCVMsg(int AH,int AL,int cv,int bit, boolean value) {
        XNetMessage m = new XNetMessage(8);
        m.setElement(0, XNetConstants.OPS_MODE_PROG_REQ);
        m.setElement(1, XNetConstants.OPS_MODE_PROG_WRITE_REQ);
        m.setElement(2, AH);
        m.setElement(3, AL);
        /* Element 4 is 0xE8 + the upper two  bits of the 10 bit CV address.
        NOTE: This is the track packet CV, not the human readable CV, so 
        it's value actually is one less than what we normally think of it as.*/
        int temp=(cv -1) & 0x0300;
        temp=temp/0x00FF;
        m.setElement(4,0xE8+temp);
        /* Element 5 is the lower 8 bits of the cv */
        m.setElement(5, ((0x00ff & cv)-1));
	/* Since this is a bit verify, Element 6 is:
		0xF0 +
		bit 3 is the value to write
		bit's 0-2 are the location of the bit we are changing */
	if(value == true)
        	m.setElement(6, ((0xf8) | (bit & 0xff)) );
	else // value == false
        	m.setElement(6, ((0xf0) | (bit & 0xff)) );
        return m;
    }

   /*
    * Next, we have routines to generate XPressNet Messages for building 
    * and tearing down a consist or a double header.
    */

    /*
     * Build a Double Header
     *
     * @param address1 is the first address in the consist
     * @param address2 is the second address in the consist.
     */
    public static XNetMessage getBuildDoubleHeaderMsg(int address1,int address2){
	XNetMessage msg=new XNetMessage(7);
	msg.setElement(0,XNetConstants.LOCO_DOUBLEHEAD);
        msg.setElement(1,XNetConstants.LOCO_DOUBLEHEAD_BYTE2);
        msg.setElement(2,LenzCommandStation.getDCCAddressHigh(address1));
        msg.setElement(3,LenzCommandStation.getDCCAddressLow(address1));
        msg.setElement(4,LenzCommandStation.getDCCAddressHigh(address2));
        msg.setElement(5,LenzCommandStation.getDCCAddressLow(address2));
        msg.setParity();
        return(msg);
    }

    /*
     * Dissolve a Double Header
     *
     * @param address is one of the two addresses in the Double Header 
     */
    public static XNetMessage getDisolveDoubleHeaderMsg(int address){
        // All we have to do is call getBuildDoubleHeaderMsg with the 
        // second address as a zero
        return(getBuildDoubleHeaderMsg(address,0));
    }
   
    /*
     * Add a Single address to a specified Advanced consist
     *
     * @param consist is the consist address (1-99)
     * @param address is the locomotive address to add.
     * @param isNormalDir tells us if the locomotive is going forward when 
     * the consist is going forward.
     */
    public static XNetMessage getAddLocoToConsistMsg(int consist,int address,
                                       boolean isNormalDir){
	XNetMessage msg=new XNetMessage(6);
	msg.setElement(0,XNetConstants.LOCO_OPER_REQ);
        if(isNormalDir) {
           msg.setElement(1,XNetConstants.LOCO_ADD_MULTI_UNIT_REQ);
        } else {
           msg.setElement(1,XNetConstants.LOCO_ADD_MULTI_UNIT_REQ | 0x01);
        }
        msg.setElement(2,LenzCommandStation.getDCCAddressHigh(address));
        msg.setElement(3,LenzCommandStation.getDCCAddressLow(address));
        msg.setElement(4,consist);
        msg.setParity();
        return(msg);
    }

    /*
     * Remove a Single address to a specified Advanced consist
     *
     * @param consist is the consist address (1-99)
     * @param address is the locomotive address to remove
     */
    public static XNetMessage getRemoveLocoFromConsistMsg(int consist,int address){
	XNetMessage msg=new XNetMessage(6);
	msg.setElement(0,XNetConstants.LOCO_OPER_REQ);
        msg.setElement(1,XNetConstants.LOCO_REM_MULTI_UNIT_REQ);
        msg.setElement(2,LenzCommandStation.getDCCAddressHigh(address));
        msg.setElement(3,LenzCommandStation.getDCCAddressLow(address));
        msg.setElement(4,consist);
        msg.setParity();
        return(msg);
    }
   

   /*
    * Next, we have routines to generate XPressNet Messages for search
    * and manipulation of the Command Station Database
    */

    /*
     * Given a locomotive address, search the database for the next 
     * member. (if the Address is zero start at the begining of the 
     * database)
     * @param address is the locomotive address
     * @param searchForward indicates to search the database Forward if 
     * true, or backwards if False 
     */
    public static XNetMessage getNextAddressOnStackMsg(int address,boolean searchForward){
	XNetMessage msg=new XNetMessage(5);
	msg.setElement(0,XNetConstants.LOCO_STATUS_REQ);
        if(searchForward) {
           msg.setElement(1,XNetConstants.LOCO_STACK_SEARCH_FWD);
        } else {
           msg.setElement(1,XNetConstants.LOCO_STACK_SEARCH_BKWD);
        }
        msg.setElement(2,LenzCommandStation.getDCCAddressHigh(address));
        msg.setElement(3,LenzCommandStation.getDCCAddressLow(address));
        msg.setParity();
        return(msg);
    }

    /*
     * Given a consist address, search the database for the next Consist 
     * address.
     * @param address is the consist address (in the range 1-99)
     * If the Address is zero start at the begining of the database
     * @param searchForward indicates to search the database Forward if 
     * true, or backwards if False 
     */
    public static XNetMessage getDBSearchMsgConsistAddress(int address,boolean searchForward){
	XNetMessage msg=new XNetMessage(4);
	msg.setElement(0,XNetConstants.CS_MULTI_UNIT_REQ);
        if(searchForward) {
           msg.setElement(1,XNetConstants.CS_MULTI_UNIT_REQ_FWD);
        } else {
           msg.setElement(1,XNetConstants.CS_MULTI_UNIT_REQ_BKWD);
        }
        msg.setElement(2,address);
        msg.setParity();
        return(msg);
    }

    /*
     * Given a consist and a locomotive address, search the database for 
     * the next Locomotive in the consist.
     * @param consist is the consist address (1-99)
     * If the Consist Address is zero start at the begining of the database
     * @param address is the locomotive address
     * If the Address is zero start at the begining of the consist
     * @param searchForward indicates to search the database Forward if 
     * true, or backwards if False 
     */
    public static XNetMessage getDBSearchMsgNextMULoco(int consist,int address,boolean searchForward){
	XNetMessage msg=new XNetMessage(6);
	msg.setElement(0,XNetConstants.LOCO_IN_MULTI_UNIT_SEARCH_REQ);
        if(searchForward) {
           msg.setElement(1,XNetConstants.LOCO_IN_MULTI_UNIT_REQ_FORWARD);
        } else {
           msg.setElement(1,XNetConstants.LOCO_IN_MULTI_UNIT_REQ_BACKWARD);
        }
        msg.setElement(2,consist);
        msg.setElement(3,LenzCommandStation.getDCCAddressHigh(address));
        msg.setElement(4,LenzCommandStation.getDCCAddressLow(address));
        msg.setParity();
        return(msg);
    }

    /*
     * Given a locomotive address, delete it from the database 
     * @param address is the locomotive address
     */
    public static XNetMessage getDeleteAddressOnStackMsg(int address){
	XNetMessage msg=new XNetMessage(5);
	msg.setElement(0,XNetConstants.LOCO_STATUS_REQ);
        msg.setElement(1,XNetConstants.LOCO_STACK_DELETE);
        msg.setElement(2,LenzCommandStation.getDCCAddressHigh(address));
        msg.setElement(3,LenzCommandStation.getDCCAddressLow(address));
        msg.setParity();
        return(msg);
    }

    /*
     * Given a locomotive address, request its status 
     * @param address is the locomotive address
     */
    public static XNetMessage getLocomotiveInfoRequestMsg(int address){
	XNetMessage msg=new XNetMessage(5);
 	msg.setElement(0,XNetConstants.LOCO_STATUS_REQ); 
        msg.setElement(1,XNetConstants.LOCO_INFO_REQ_V3);
        msg.setElement(2,LenzCommandStation.getDCCAddressHigh(address));
        msg.setElement(3,LenzCommandStation.getDCCAddressLow(address)); 
        msg.setParity();
        return(msg);
    }

    /*
     * Given a locomotive address, request the function state (momentary status) 
     * @param address is the locomotive address
     */
    public static XNetMessage getLocomotiveFunctionStatusMsg(int address){
	XNetMessage msg=new XNetMessage(5);
 	msg.setElement(0,XNetConstants.LOCO_STATUS_REQ); 
        msg.setElement(1,XNetConstants.LOCO_INFO_REQ_FUNC);
        msg.setElement(2,LenzCommandStation.getDCCAddressHigh(address));
        msg.setElement(3,LenzCommandStation.getDCCAddressLow(address)); 
        msg.setParity();
        return(msg);
    }
    /*
     * Given a locomotive address, request the function on/off state 
     * for functions 13-28
     * @param address is the locomotive address
     */
    public static XNetMessage getLocomotiveFunctionHighOnStatusMsg(int address){
	XNetMessage msg=new XNetMessage(5);
 	msg.setElement(0,XNetConstants.LOCO_STATUS_REQ); 
        msg.setElement(1,XNetConstants.LOCO_INFO_REQ_FUNC_HI_ON);
        msg.setElement(2,LenzCommandStation.getDCCAddressHigh(address));
        msg.setElement(3,LenzCommandStation.getDCCAddressLow(address)); 
        msg.setParity();
        return(msg);
    }
 
    /*
     * Given a locomotive address, request the function state (momentary status)
     * for high functions (functions 13-28) 
     * @param address is the locomotive address
     */
    public static XNetMessage getLocomotiveFunctionHighMomStatusMsg(int address){
	XNetMessage msg=new XNetMessage(5);
 	msg.setElement(0,XNetConstants.LOCO_STATUS_REQ); 
        msg.setElement(1,XNetConstants.LOCO_INFO_REQ_FUNC_HI_MOM);
        msg.setElement(2,LenzCommandStation.getDCCAddressHigh(address));
        msg.setElement(3,LenzCommandStation.getDCCAddressLow(address)); 
        msg.setParity();
        return(msg);
    }

	// initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(XNetMessage.class.getName());

}

/* @(#)XNetMessage.java */

