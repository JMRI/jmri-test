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
        return MrcPackets.toString(this);
    }
    
    static public MrcMessage getSendSpeed(int addressLo, int addressHi, int speed){
        MrcMessage m = new MrcMessage(MrcPackets.getThrottlePacketLength());
        int i = m.putHeader(MrcPackets.throttlePacketHeader);
        
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
        MrcMessage m = new MrcMessage(MrcPackets.getFunctionPacketLength());
        int i= 0;
        switch(group){
            case 1: i = m.putHeader(MrcPackets.functionGroup1PacketHeader);
                    break;
            case 2: i = m.putHeader(MrcPackets.functionGroup2PacketHeader);
                    break;
            case 3: i = m.putHeader(MrcPackets.functionGroup3PacketHeader);
                    break;
            case 4: i= m.putHeader(MrcPackets.functionGroup4PacketHeader);
                    break;
            case 5: i = m.putHeader(MrcPackets.functionGroup5PacketHeader);
                    break;
            case 6: i = m.putHeader(MrcPackets.functionGroup6PacketHeader);
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
        
        MrcMessage m = new MrcMessage(MrcPackets.getReadCVPacketLength());
        m.setTimeout(LONG_TIMEOUT);
        //m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        int i = m.putHeader(MrcPackets.readCVHeader);

        m.setElement(i++, cvHi);
        m.setElement(i++,0x00);
        m.setElement(i++, cvLo);
        m.setElement(i++, 0x00);
        m.setElement(i++, getCheckSum(0x00, 0x00, cvHi, cvLo));
        m.setElement(i++, 0x00);
        return m;
    }
    
    static public MrcMessage getPOM(int addressLo, int addressHi, int cv, int val){
        MrcMessage m = new MrcMessage(MrcPackets.getWriteCVPOMPacketLength());
        int i = m.putHeader(MrcPackets.writeCVPOMHeader);
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
        MrcMessage m = new MrcMessage(MrcPackets.getWriteCVPROGPacketLength());
        int i = m.putHeader(MrcPackets.writeCVPROGHeader);
        
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
        MrcMessage m = new MrcMessage(MrcPackets.getSetClockRatioPacketLength());
        int i = m.putHeader(MrcPackets.setClockRatioHeader);
        
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
        MrcMessage m = new MrcMessage(MrcPackets.getSetClockTimePacketLength());
        int i = m.putHeader(MrcPackets.setClockTimeHeader);
        
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
        MrcMessage m = new MrcMessage(MrcPackets.getSetClockAmPmPacketLength());
        int i = m.putHeader(MrcPackets.setClockAmPmHeader);
        
        m.setElement(i++, 0x32);
        m.setElement(i++, 0x00);
        m.setElement(i++, getCheckSum(0x32, 0x00, 0x00, 0x00));
        return m;
    }

    static Logger log = LoggerFactory.getLogger(MrcMessage.class.getName());

}


/* @(#)MrcMessage.java */

