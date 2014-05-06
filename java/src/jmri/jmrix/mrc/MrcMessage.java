// MrcMessage.java

package jmri.jmrix.mrc;

import jmri.jmrix.AbstractMRMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encodes a message to an EasyDCC command station.  The MrcReply
 * class handles the response from the command station.
 * <P>
 * The {@link MrcReply}
 * class handles the response from the command station.
 *
 * @author			Bob Jacobsen  Copyright (C) 2001, 2004
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
    public  MrcMessage(String m) {
        super(m);
    }
    
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
            bytePre[i] = (byte) this.getElement(i);
        
    }
    
    protected int lengthOfByteStream(AbstractMRMessage m) {
        int len = m.getNumDataElements();
        int cr = 0;
        if (! m.isBinary()) cr = 1;  // space for return
        return len+cr;
    }
    
    
    public byte[] getByte(){
        return bytePre;
    }
    
    static public MrcMessage getSendSpeed(byte addressLo, byte addressHi, byte speed){
        MrcMessage m = new MrcMessage(14);
        m.setElement(0,0x25);
        m.setElement(1,0x00);
        m.setElement(2,0x25);
        m.setElement(3,0x00);
        m.setElement(4,addressHi);
        m.setElement(5, 0x00);
        m.setElement(6,addressLo);
        m.setElement(7,0x00);
        m.setElement(8,speed);
        m.setElement(9,0x00);
        m.setElement(10,0x02);
        m.setElement(11,0x00);
        m.setElement(12,getCheckSum(addressHi, addressLo, speed, (byte)0x02));
        m.setElement(13,0x00);
        m.setTimeout(100);
        return m;
    }
    
    static public MrcMessage getSendFunction(byte addressLo, byte addressHi, byte function){
        MrcMessage m = new MrcMessage(12);
        m.setElement(0,0x25);
        m.setElement(1,0x00);
        m.setElement(2,0x25);
        m.setElement(3,0x00);
        m.setElement(4,addressHi);
        m.setElement(5, 0x00);
        m.setElement(6,addressLo);
        m.setElement(7,0x00);
        m.setElement(8,function);
        m.setElement(9,0x00);
        m.setElement(10,getCheckSum(addressHi, addressLo, function, (byte)0x00));
        m.setElement(11,0x00);
        m.setTimeout(100);
        return m;
    }
    
    
    
    static byte getCheckSum(byte addressHi, byte addressLo, byte data1, byte data2){
        byte address = (byte)(addressHi^addressLo);
        byte data = (byte)(data1^data2);
        return (byte)(address^data);
    }
    
    static public MrcMessage getReadCV(int cv) { //R xxx
        byte cvLo = (byte)(cv);
        byte cvHi = (byte)(cv>>8);
        
        MrcMessage m = new MrcMessage(10);
        //m.setBinary(false);
        m.setTimeout(LONG_TIMEOUT);
        m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        
        m.setElement(0, 0x43);
        m.setElement(1, 0x00);
        m.setElement(2, 0x43);
        m.setElement(3, 0x00);
        m.setElement(4, cvHi);
        m.setElement(5,0x00);
        m.setElement(6, cvLo);
        m.setElement(7, 0x00);
        m.setElement(8, getCheckSum((byte)0x00, (byte)0x00, cvHi, cvLo));
        m.setElement(9, 0x00);
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

    static Logger log = LoggerFactory.getLogger(MrcMessage.class.getName());

}


/* @(#)MrcMessage.java */

