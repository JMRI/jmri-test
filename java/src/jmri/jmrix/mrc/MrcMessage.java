// MrcMessage.java

package jmri.jmrix.mrc;

import jmri.jmrix.AbstractMRMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encodes a message to an MRC command station.  The MrcReply
 * class handles the response from the command station.
 * <P>
 * The {@link MrcReply}
 * class handles the response from the command station.
 * <p>
 * Some of the message formats used in this class are Copyright MRC, Inc.
 * and used with permission as part of the JMRI project.  That permission
 * does not extend to uses in other software products.  If you wish to
 * use this code, algorithm or these message formats outside of JMRI, please
 * contact MRC Inc for separate permission.
 * <p>
 * @author			Bob Jacobsen  Copyright (C) 2001, 2004
 * @author      Kevin Dickerson    Copyright (C) 2014
 * @author		kcameron Copyright (C) 2014
 * @version			$Revision$
 */
public class MrcMessage extends jmri.jmrix.AbstractMRMessage {

    public MrcMessage() {
        super();
    }

    // create a new one
    public  MrcMessage(int i) {
        super(i);
        setRetries(2);
    }

    // copy one
    public  MrcMessage(MrcMessage m) {
        super(m);
    }

    // from String
    /*public  MrcMessage(String m) {
        super(m);
    }*/
    
    /**
     * Creates a new MrcMessage containing a byte array to represent
     * a packet to output
     * @param packet The contents of the packet
     */
    public MrcMessage(byte [] packet ) {
    	this((packet.length));
        int i = 0; // counter of byte in output message
        int j = 0; // counter of byte in input packet
        setBinary(true);
        // add each byte of the input message
        for (j=0; j<packet.length; j++) {
            this.setElement(i, packet[i]);
            i++;
        }
        setRetries(2);
    }
    
    byte bytePre[];
    
    public void setByte(){
        bytePre = new byte[lengthOfByteStream(this)];

        // add data content
        int len = getNumDataElements();
        for (int i=0; i< len; i++)
            bytePre[i] = (byte)this.getElement(i);
        
    }
    
    protected int lengthOfByteStream(AbstractMRMessage m) {
        int len = m.getNumDataElements();
        int cr = 0;
        if (! m.isBinary()) cr = 1;  // space for return
        return len+cr;
    }
    
    
    protected byte[] getByte(){
        return bytePre;
    }
    
    int putHeader(int[] insert){
        int i = 0;
        for (i=0; i<insert.length; i++) {
            this.setElement(i, insert[i]);
        }
        return i;
    }
    
    public String toString(){
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
	        switch (getElement(0)&0xFF) {
	        case setClockRatioCmd:
	        	txt.append("Set Clock Ratio: " + getElement(4));
	        	break;
	        case setClockTimeCmd:
	        	txt.append("Set Clock Time: " + getElement(4) + ":" + getElement(6));
	        	break;
	        case setClockAmPmCmd:
	        	txt.append("Set Clock AM/PM");
	        	break;
	        case throttlePacketCmd:
	        	if (getElement(4) != 0) {
	            	txt.append("Loco (L)");
	        	} else {
	            	txt.append("Loco (S)");
	        	}
	        	txt.append(Integer.toString(provideLocoId(getElement(4), getElement(6))));
	        	if ((getElement(8) & 0x80) == 0x80) {
	        		txt.append("Fwd ");
	        	} else {
	        		txt.append("Rev ");
	        	}
	        	txt.append(" Speed: " + Integer.toString((getElement(8) & 0x80)));
	    		break;
	        case functionGroup1PacketCmd:
	        	txt.append("Loco " + Integer.toString(provideLocoId(getElement(4), getElement(6))) + " Group 1");
	        	txt.append(" F1 " + Integer.toString(getElement(8) & 0x01));
	        	txt.append(" F2 " + Integer.toString(getElement(8) & 0x02));
	        	txt.append(" F3 " + Integer.toString(getElement(8) & 0x04));
	        	txt.append(" F4 " + Integer.toString(getElement(8) & 0x08));
	        	txt.append(" F0 " + Integer.toString(getElement(8) & 0x10));
	    		break;
	        case functionGroup2PacketCmd:
	        	txt.append("Loco " + Integer.toString(provideLocoId(getElement(4), getElement(6))) + " Group 2");
	        	txt.append(" F5 " + Integer.toString(getElement(8) & 0x01));
	        	txt.append(" F6 " + Integer.toString(getElement(8) & 0x02));
	        	txt.append(" F7 " + Integer.toString(getElement(8) & 0x04));
	        	txt.append(" F8 " + Integer.toString(getElement(8) & 0x08));
	    		break;
	        case functionGroup3PacketCmd:
	        	txt.append("Loco " + Integer.toString(provideLocoId(getElement(4), getElement(6))) + " Group 3");
	        	txt.append(" F9 " + Integer.toString(getElement(8) & 0x01));
	        	txt.append(" F10 " + Integer.toString(getElement(8) & 0x02));
	        	txt.append(" F11 " + Integer.toString(getElement(8) & 0x04));
	        	txt.append(" F12 " + Integer.toString(getElement(8) & 0x08));
	    		break;
	        case functionGroup4PacketCmd:
	        	txt.append("Loco " + Integer.toString(provideLocoId(getElement(4), getElement(6))) + " Group 4");
	        	txt.append(" F13 " + Integer.toString(getElement(8) & 0x01));
	        	txt.append(" F14 " + Integer.toString(getElement(8) & 0x02));
	        	txt.append(" F15 " + Integer.toString(getElement(8) & 0x04));
	        	txt.append(" F16" + Integer.toString(getElement(8) & 0x08));
	    		break;
	        case functionGroup5PacketCmd:
	        	txt.append("Loco " + Integer.toString(provideLocoId(getElement(4), getElement(6))) + " Group 5");
	        	txt.append(" F17 " + Integer.toString(getElement(8) & 0x01));
	        	txt.append(" F18 " + Integer.toString(getElement(8) & 0x02));
	        	txt.append(" F19 " + Integer.toString(getElement(8) & 0x04));
	        	txt.append(" F20 " + Integer.toString(getElement(8) & 0x08));
	    		break;
	        case functionGroup6PacketCmd:
	        	txt.append("Loco " + Integer.toString(provideLocoId(getElement(4), getElement(6))) + " Group 6");
	        	txt.append(" F21 " + Integer.toString(getElement(8) & 0x01));
	        	txt.append(" F22 " + Integer.toString(getElement(8) & 0x02));
	        	txt.append(" F23 " + Integer.toString(getElement(8) & 0x04));
	        	txt.append(" F24 " + Integer.toString(getElement(8) & 0x08));
	        	txt.append(" F25 " + Integer.toString(getElement(8) & 0x10));
	        	txt.append(" F26 " + Integer.toString(getElement(8) & 0x20));
	        	txt.append(" F27 " + Integer.toString(getElement(8) & 0x40));
	        	txt.append(" F28 " + Integer.toString(getElement(8) & 0x80));
	    		break;
	        case readCVCmd:
	        	int cv = (getElement(4) << 8) + getElement(6);
	        	txt.append("Read CV " + Integer.toString(cv));
	    		break;
	        case readDecoderAddressCmd:
	        	txt.append("Read Decoder Address ");
	    		break;
	        case writeCVPROGCmd:
	        	txt.append("Write PROG CV ");
	        	txt.append(Integer.toString((getElement(4) << 8) + getElement(6)));
	        	txt.append("=");
	        	txt.append(Integer.toString(getElement(8)));
	    		break;
	        case writeCVPOMCmd:
	        	txt.append("Write POM CV ");
	        	txt.append(Integer.toString(getElement(4) << 8) + getElement(6));
	        	txt.append("=");
	        	txt.append(Integer.toString(getElement(8)));
	    		break;
	        default:
	        	txt.append("Unk Cmd Code:");
	            for (int i=0;i<getNumDataElements(); i++) {
	                txt.append(" ");
	                txt.append(jmri.util.StringUtil.twoHexFromInt(getElement(i)&0xFF));
	            }
	        	break;
	        }
        }
        return txt.toString();
    }
    
    private int provideLocoId(int hi, int lo) {
    	if (hi == 0) {
    		return lo & 0x80;
    	} else {
    		return lo + ((hi & 0xC0) << 8);
    	}
    }
    
    public static final int throttlePacketCmd = 0x25;
    final static int[] throttlePacketHeader = new int[]{throttlePacketCmd,0x00,throttlePacketCmd,0x00};
    final static int throttlePacketLength = 10;//length of packet less the header
    public static int getThrottlePacketLength() { return throttlePacketHeader.length+throttlePacketLength; }

    public static final int functionGroup1PacketCmd = 0x34;
    final static int[] functionGroup1PacketHeader = new int[]{functionGroup1PacketCmd,0x00,functionGroup1PacketCmd,0x00};
    public static final int functionGroup2PacketCmd = 0x44;
    final static int[] functionGroup2PacketHeader = new int[]{functionGroup2PacketCmd,0x00,functionGroup2PacketCmd,0x00};
    public static final int functionGroup3PacketCmd = 0x54;
    final static int[] functionGroup3PacketHeader = new int[]{functionGroup3PacketCmd,0x00,functionGroup3PacketCmd,0x00};
    public static final int functionGroup4PacketCmd = 0x74;
    final static int[] functionGroup4PacketHeader = new int[]{functionGroup4PacketCmd,0x00,functionGroup4PacketCmd,0x00};
    public static final int functionGroup5PacketCmd = 0x84;
    final static int[] functionGroup5PacketHeader = new int[]{functionGroup5PacketCmd,0x00,functionGroup5PacketCmd,0x00};
    public static final int functionGroup6PacketCmd = 0xA4;
    final static int[] functionGroup6PacketHeader = new int[]{functionGroup6PacketCmd,0x00,functionGroup6PacketCmd,0x00};
    final static int functionGroupLength = 8;
    public static int getFunctionPacketLength() { return functionGroup1PacketHeader.length+functionGroupLength; }

    public static final int readCVCmd = 0x43;
    final static int[] readCVHeader = new int[]{readCVCmd,0x00,readCVCmd,0x00};
    final private static int readCVLength = 6;
    public static int getReadCVPacketLength() { return readCVHeader.length+readCVLength; }

    public static final int readDecoderAddressCmd = 0x42;
    final static int[] readDecoderAddress = new int[]{readDecoderAddressCmd,0x00,readDecoderAddressCmd,0x00,readDecoderAddressCmd,0x00};
    public static int getReadDecoderAddressLength() { return readDecoderAddress.length; }

    public static final int writeCVPROGCmd = 0x24;
    final static int[] writeCVPROGHeader = new int[]{writeCVPROGCmd,0x00,writeCVPROGCmd,0x00};
    final private static int writeCVPROGLength = 8;
    public static int getWriteCVPROGPacketLength() { return writeCVPROGHeader.length+writeCVPROGLength; }

    public static final int writeCVPOMCmd = 0x56;
    final static int[] writeCVPOMHeader = new int[]{writeCVPOMCmd,0x00,writeCVPOMCmd,0x00};
    final private static int writeCVPOMLength = 12;
    public static int getWriteCVPOMPacketLength() { return writeCVPOMHeader.length+writeCVPOMLength; }

    public static final int setClockRatioCmd = 0x12;
    final static int[] setClockRatioHeader = new int[]{setClockRatioCmd,0x00,setClockRatioCmd,0x00};
    final private static int setClockRatioLength = 10;
    public static int getSetClockRatioPacketLength() { return setClockRatioLength; }

    public static final int setClockTimeCmd = 0x13;
    final static int[] setClockTimeHeader = new int[]{setClockTimeCmd,0x00,setClockTimeCmd,0x00};
    final private static int setClockTimeLength = 10;
    public static int getSetClockTimePacketLength() { return setClockTimeLength; }

    public static final int setClockAmPmCmd = 0x32;
    final static int[] setClockAmPmHeader = new int[]{setClockAmPmCmd,0x00,setClockAmPmCmd,0x00};
    final private static int setClockAmPmLength = 10;
    public static int getSetClockAmPmPacketLength() { return setClockAmPmLength; }
    
    static public MrcMessage getSendSpeed(int addressLo, int addressHi, int speed){
        MrcMessage m = new MrcMessage(getThrottlePacketLength());
        int i = m.putHeader(throttlePacketHeader);
        
        m.setElement(i++,addressHi);
        m.setElement(i++, 0x00);
        m.setElement(i++,addressLo);
        m.setElement(i++,0x00);
        m.setElement(i++,speed);
        m.setElement(i++,0x00);
        m.setElement(i++,0x02);
        m.setElement(i++,0x00);
        m.setElement(i++,getCheckSum(addressHi, addressLo, speed, 0x02));
        m.setElement(i++,0x00);
        m.setTimeout(100);
        return m;
    }
    
    static public MrcMessage getSendFunction(int group, int addressLo, int addressHi, int data){
        MrcMessage m = new MrcMessage(getFunctionPacketLength());
        int i= 0;
        switch(group){
            case 1: i = m.putHeader(functionGroup1PacketHeader);
                    break;
            case 2: i = m.putHeader(functionGroup2PacketHeader);
                    break;
            case 3: i = m.putHeader(functionGroup3PacketHeader);
                    break;
            case 4: i= m.putHeader(functionGroup4PacketHeader);
                    break;
            case 5: i = m.putHeader(functionGroup5PacketHeader);
                    break;
            case 6: i = m.putHeader(functionGroup6PacketHeader);
                    break;
            default: log.error("Invalid function group " + group);
                    return null;
        }

        m.setElement(i++,addressHi);
        m.setElement(i++, 0x00);
        m.setElement(i++,addressLo);
        m.setElement(i++,0x00);
        m.setElement(i++,data);
        m.setElement(i++,0x00);
        m.setElement(i++,getCheckSum(addressHi, addressLo, data, 0x00));
        m.setElement(i++,0x00);
        m.setTimeout(100);
        return m;
    }
    
    static int getCheckSum(int addressHi, int addressLo, int data1, int data2){
        int address = addressHi^addressLo;
        int data = data1^data2;
        return (address^data);
    }
    
    static public MrcMessage getReadCV(int cv) { //R xxx
        int cvLo = (cv);
        int cvHi = (cv>>8);
        
        MrcMessage m = new MrcMessage(getReadCVPacketLength());
        m.setTimeout(LONG_TIMEOUT);
        m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        int i = m.putHeader(readCVHeader);

        m.setElement(i++, cvHi);
        m.setElement(i++,0x00);
        m.setElement(i++, cvLo);
        m.setElement(i++, 0x00);
        m.setElement(i++, getCheckSum(0x00, 0x00, cvHi, cvLo));
        m.setElement(i++, 0x00);
        return m;
    }
    
    static public MrcMessage getPOM(int addressLo, int addressHi, int cv, int val){
        MrcMessage m = new MrcMessage(getWriteCVPOMPacketLength());
        int i = m.putHeader(writeCVPOMHeader);
        cv--;
        m.setElement(i++,addressHi);
        m.setElement(i++, 0x00);
        m.setElement(i++,addressLo);
        m.setElement(i++,0x00);
        m.setElement(i++, 0xEC);
        m.setElement(i++,0x00);
        m.setElement(i++, cv);
        m.setElement(i++, 0x00);
        m.setElement(i++, val);
        m.setElement(i++, 0x00);
        int checksum = getCheckSum(addressHi, addressLo, 0xEC, cv);
        checksum = getCheckSum(checksum, val, 0x00, 0x00);
        m.setElement(i++, checksum);
        return m;
    }
    
    static public MrcMessage getWriteCV(int cv, int val){
        MrcMessage m = new MrcMessage(getWriteCVPROGPacketLength());
        int i = m.putHeader(writeCVPROGHeader);
        
        int cvLo = cv;
        int cvHi = cv>>8;
        
        m.setElement(i++, cvHi);
        m.setElement(i++,0x00);
        m.setElement(i++, cvLo);
        m.setElement(i++, 0x00);
        m.setElement(i++, val);
        m.setElement(i++, 0x00);
        m.setElement(i++, getCheckSum(cvHi, cvLo, val, 0x00));
        return m;
    }
       
    static protected final int LONG_TIMEOUT=65000;  // e.g. for programming options

/* Bellow have been taken from the NCE Message left for the time being as examples */
    
    // diagnose format
    public boolean isKillMain() {
        return getOpCode() == 'K';
    }

    public boolean isEnableMain() {
        return getOpCode() == 'E';
    }
    
    // static methods to return a formatted message
    static public MrcMessage getEnableMain() {
        MrcMessage m = new MrcMessage(1);
        m.setBinary(false);
        m.setOpCode('E');
        return m;
    }

    static public MrcMessage getKillMain() {
        MrcMessage m = new MrcMessage(1);
        m.setBinary(false);
        m.setOpCode('K');
        return m;
    }
    
    /* 
     * get a static message to add a locomotive to a Standard Consist 
     * in the normal direction
     * @param ConsistAddress - a consist address in the range 1-255
     * @param LocoAddress - a jmri.DccLocoAddress object representing the 
     * locomotive to add
     * @return an MrcMessage of the form GN cc llll 
     */
    static public MrcMessage getAddConsistNormal(int ConsistAddress,jmri.DccLocoAddress LocoAddress) {
        MrcMessage m = new MrcMessage(10);
        m.setBinary(false);
        m.setOpCode('G');
        m.setElement(1,'N');
        m.setElement(2,' ');
        m.addIntAsTwoHex(ConsistAddress, 3);
        m.setElement(5,' ');
        m.addIntAsFourHex(LocoAddress.getNumber(), 6);
        return m;
    }

    /* 
     * get a static message to add a locomotive to a standard consist in 
     * the reverse direction
     * @param ConsistAddress - a consist address in the range 1-255
     * @param LocoAddress - a jmri.DccLocoAddress object representing the 
     * locomotive to add
     * @return an MrcMessage of the form GS cc llll 
     */
    static public MrcMessage getAddConsistReverse(int ConsistAddress,jmri.DccLocoAddress LocoAddress) {
        MrcMessage m = new MrcMessage(10);
        m.setBinary(false);
        m.setOpCode('G');
        m.setElement(1,'R');
        m.setElement(2,' ');
        m.addIntAsTwoHex(ConsistAddress, 3);
        m.setElement(5,' ');
        m.addIntAsFourHex(LocoAddress.getNumber(), 6);
        return m;
    }

    /* 
     * get a static message to subtract a locomotive from a Standard Consist
     * @param ConsistAddress - a consist address in the range 1-255
     * @param LocoAddress - a jmri.DccLocoAddress object representing the 
     * locomotive to remove
     * @return an MrcMessage of the form GS cc llll 
     */
    static public MrcMessage getSubtractConsist(int ConsistAddress,jmri.DccLocoAddress LocoAddress) {
        MrcMessage m = new MrcMessage(10);
        m.setBinary(false);
        m.setOpCode('G');
        m.setElement(1,'S');
        m.setElement(2,' ');
        m.addIntAsTwoHex(ConsistAddress, 3);
        m.setElement(5,' ');
        m.addIntAsFourHex(LocoAddress.getNumber(), 6);
        return m;
    }

    /* 
     * get a static message to delete a standard consist
     * @param ConsistAddress - a consist address in the range 1-255
     * @return an MrcMessage of the form GK cc 
     */
    static public MrcMessage getKillConsist(int ConsistAddress) {
        MrcMessage m = new MrcMessage(5);
        m.setBinary(false);
        m.setOpCode('G');
        m.setElement(1,'K');
        m.setElement(2,' ');
        m.addIntAsTwoHex(ConsistAddress, 3);
        return m;
    }

    /* 
     * get a static message to display a standard consist
     * @param ConsistAddress - a consist address in the range 1-255
     * @return an MrcMessage of the form GD cc 
     */
    static public MrcMessage getDisplayConsist(int ConsistAddress) {
        MrcMessage m = new MrcMessage(5);
        m.setBinary(false);
        m.setOpCode('G');
        m.setElement(1,'D');
        m.setElement(2,' ');
        m.addIntAsTwoHex(ConsistAddress, 3);
        return m;
    }

    static public MrcMessage getProgMode() {
        MrcMessage m = new MrcMessage(1);
        m.setBinary(false);
        m.setOpCode('M');
        return m;
    }

    static public MrcMessage getExitProgMode() {
        MrcMessage m = new MrcMessage(1);
        m.setBinary(false);
        m.setOpCode('X');
        return m;
    }

    static public MrcMessage getReadPagedCV(int cv) { //R xxx
        MrcMessage m = new MrcMessage(5);
        m.setBinary(false);
        m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        m.setTimeout(LONG_TIMEOUT);
        m.setOpCode('R');
        m.setElement(1,' ');
        m.addIntAsThreeHex(cv, 2);
        return m;
    }

    static public MrcMessage getWritePagedCV(int cv, int val) { //P xxx xx
        MrcMessage m = new MrcMessage(8);
        m.setBinary(false);
        m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        m.setTimeout(LONG_TIMEOUT);
        m.setOpCode('P');
        m.setElement(1,' ');
        m.addIntAsThreeHex(cv, 2);
        m.setElement(5,' ');
        m.addIntAsTwoHex(val, 6);
        return m;
    }

    static public MrcMessage getReadRegister(int reg) { //Vx
        if (reg>8) log.error("register number too large: "+reg);
        MrcMessage m = new MrcMessage(2);
        m.setBinary(false);
        m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        m.setTimeout(LONG_TIMEOUT);
        m.setOpCode('V');
        String s = ""+reg;
        m.setElement(1, s.charAt(s.length()-1));
        return m;
    }

    static public MrcMessage getWriteRegister(int reg, int val) { //Sx xx
        if (reg>8) log.error("register number too large: "+reg);
        MrcMessage m = new MrcMessage(5);
        m.setBinary(false);
        m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        m.setTimeout(LONG_TIMEOUT);
        m.setOpCode('S');
        String s = ""+reg;
        m.setElement(1, s.charAt(s.length()-1));
        m.setElement(2,' ');
        m.addIntAsTwoHex(val, 3);
        return m;
    }

    /**
     * set the fast clock ratio
     * ratio is integer and max of 60 and min of 1
     * @param ratio
     * @return
     */
    static public MrcMessage setClockRatio(int ratio) {
        if (ratio < 0 || ratio > 60) log.error("ratio number too large: "+ratio);
        MrcMessage m = new MrcMessage(getSetClockRatioPacketLength());
        int i = m.putHeader(setClockRatioHeader);
        
        m.setElement(i++, ratio);
        m.setElement(i++, 0x00);
        m.setElement(i++, getCheckSum(ratio, 0x00, 0x00, 0x00));
        return m;
    }

    /**
     * set the fast time clock
     * @param hour
     * @param minute
     * @return
     */
    static public MrcMessage setClockTime(int hour, int minute) {
        if (hour < 0 || hour > 23) log.error("hour number out of range : " + hour);
        if (minute < 0 || minute > 59) log.error("hour minute out of range : " + minute);
        MrcMessage m = new MrcMessage(getSetClockTimePacketLength());
        int i = m.putHeader(setClockTimeHeader);
        
        m.setElement(i++, hour);
        m.setElement(i++, 0x00);
        m.setElement(i++, minute);
        m.setElement(i++, 0x00);
        m.setElement(i++, getCheckSum(hour, 0x00, minute, 0x00));
        return m;
    }

    /**
     * Toggle the AM/PM vs 24 hour mode
     * @return
     */
    static public MrcMessage setClockAmPm() {
        MrcMessage m = new MrcMessage(getSetClockAmPmPacketLength());
        int i = m.putHeader(setClockAmPmHeader);
        
        m.setElement(i++, 0x32);
        m.setElement(i++, 0x00);
        m.setElement(i++, getCheckSum(0x32, 0x00, 0x00, 0x00));
        return m;
    }

    static Logger log = LoggerFactory.getLogger(MrcMessage.class.getName());

}


/* @(#)MrcMessage.java */

