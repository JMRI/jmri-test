// MrcMessage.java

package jmri.jmrix.mrc;

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
public class MrcMessage {

    // create a new one
    public  MrcMessage(int len) {
        if (len<1)
            log.error("invalid length in call to ctor: "+len);
        _nDataChars = len;
        _dataChars = new int[len];
    }

    // copy one
    public MrcMessage(MrcMessage original) {
        this(original._dataChars);
    }
    
    public MrcMessage(int[] contents) {
        this(contents.length);
        for (int i=0; i<contents.length; i++) this.setElement(i, contents[i]);
    }

    public MrcMessage(byte[] contents) {
        this(contents.length);
        for (int i=0; i<contents.length; i++) this.setElement(i, contents[i]&0xFF);
    }
    
    MrcTrafficListener source = null;
    
    public void setSource(MrcTrafficListener s){
        source = s;
    }
    
    public MrcTrafficListener getSource(){
        return source;
    }
    
    int msgClass = ~0;
    
    void setMessageClass(int i){
        msgClass = i;
    }
    
    public int getMessageClass(){
        return msgClass;
    }
    
    public void replyNotExpected(){
        replyExpected=false;
    }
    
    boolean replyExpected =true;
    
    public boolean isReplyExpected(){
        return replyExpected;
    }
    
    int SHORT_TIMEOUT = 150;
    int SHORT_PROG_TIMEOUT = 4000;
    //int LONG_TIMEOUT = 4000;
    
    int timeout = SHORT_TIMEOUT;
    
    void setTimeout(int i) { timeout = i; }
    public int getTimeout() { return timeout; }
    
    int retries = 3;
    public int getRetries() { return retries; }
    public void setRetries(int i) { retries=i; }
    /*protected int lengthOfByteStream(AbstractMRMessage m) {
        int len = m.getNumDataElements();
        int cr = 0;
        if (! m.isBinary()) cr = 1;  // space for return
        return len+cr;
    }*/
    
    boolean inError = false;
    
    public void setMessageInError(){
        inError = true;
    }
    
    public boolean isPacketInError(){return inError;}
    
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
    
    static public MrcMessage getSendSpeed128(int addressLo, int addressHi, int speed){
        MrcMessage m = new MrcMessage(MrcPackets.getThrottlePacketLength());
        m.setMessageClass(MrcInterface.THROTTLEINFO);
        int i = m.putHeader(MrcPackets.THROTTLEPACKETHEADER);
        
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
    //    m.setTimeout(100);
        return m;
    }
    
    static public MrcMessage getSendSpeed28(int addressLo, int addressHi, int speed, boolean fwd){
        MrcMessage m = new MrcMessage(MrcPackets.getThrottlePacketLength());
        m.setMessageClass(MrcInterface.THROTTLEINFO);
        int i = m.putHeader(MrcPackets.THROTTLEPACKETHEADER);
        
        int speedC = (speed&0x1E) >> 1;
        int c = (speed&0x01) << 4;
        speedC = speedC + c;
        speedC = (fwd ? 0x60 : 0x40)| speedC;
        
        m.setElement(i++,addressHi);
        m.setElement(i++, 0x00);
        m.setElement(i++,addressLo);
        m.setElement(i++,0x00);
        m.setElement(i++,speedC);
        m.setElement(i++,0x00);
        m.setElement(i++,0x00);
        m.setElement(i++,0x00);
        m.setElement(i++,getCheckSum(addressHi, addressLo, speed, 0x02));
        m.setElement(i++,0x00);
    //    m.setTimeout(100);
        return m;
    }
    
    static public MrcMessage getSendFunction(int group, int addressLo, int addressHi, int data){
        MrcMessage m = new MrcMessage(MrcPackets.getFunctionPacketLength());
        m.setMessageClass(MrcInterface.THROTTLEINFO);
        m.replyNotExpected();
        int i= 0;
        switch(group){
            case 1: i = m.putHeader(MrcPackets.FUNCTIONGROUP1PACKETHEADER);
                    break;
            case 2: i = m.putHeader(MrcPackets.FUNCTIONGROUP2PACKETHEADER);
                    break;
            case 3: i = m.putHeader(MrcPackets.FUNCTIONGROUP3PACKETHEADER);
                    break;
            case 4: i= m.putHeader(MrcPackets.FUNCTIONGROUP4PACKETHEADER);
                    break;
            case 5: i = m.putHeader(MrcPackets.FUNCTIONGROUP5PACKETHEADER);
                    break;
            case 6: i = m.putHeader(MrcPackets.FUNCTIONGROUP6PACKETHEADER);
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
    //    m.setTimeout(100);
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
        m.setMessageClass(MrcInterface.PROGRAMMING);
        m.setTimeout(LONG_TIMEOUT);
        //m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        int i = m.putHeader(MrcPackets.READCVHEADER);

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
        m.setMessageClass(MrcInterface.PROGRAMMING);
        int i = m.putHeader(MrcPackets.WRITECVPOMHEADER);
        
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
        m.setMessageClass(MrcInterface.PROGRAMMING);
        int i = m.putHeader(MrcPackets.WRITECVPROGHEADER);
        
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
    
    public boolean validCheckSum() {
        if (getNumDataElements() > 6) {
            int result = 0;
            for (int i = 4; i < getNumDataElements() - 2; i++) {
                result = (getElement(i) & 255) ^ result;
            }
            if (result == (getElement(getNumDataElements() - 2) & 255)) {
                return true;
            }
        }
        return false;
    }
    
    public int value(){
        int val = -1;
        if(MrcPackets.startsWith(this, MrcPackets.READCVHEADERREPLY)){
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
    
    public int getLocoAddress(){
        if(getMessageClass()!=MrcInterface.THROTTLEINFO && getMessageClass()!=MrcInterface.PROGRAMMING)
            return -1;
        int hi = getElement(4);
        int lo = getElement(6);
        if (hi == 0) {
            return lo;
        } else {
            hi = (((hi & 255) - 192) << 8);
            hi = hi + (lo & 255);
            return hi;
        }
    }
    
    public int getAccAddress(){
        if(getMessageClass()!=MrcInterface.TURNOUTS)
            return -1;
        int lowbyte = getElement(4)&0x3f;
        int highbyte = (getElement(6)&0x70)>>4;
        highbyte = (~highbyte & 0x07);
        highbyte=(highbyte<<6);
        int address = lowbyte+highbyte;
        return address;
    }
    
    public int getAccState(){
        int dBits = (( (getAccAddress()-1) & 0x03) << 1 ); 
        if((getElement(6)&0x07)==dBits){
            return jmri.Turnout.THROWN;
        } else if((getElement(6)&0x07)==(dBits | 1)){
            return jmri.Turnout.CLOSED;
        }
        return jmri.Turnout.UNKNOWN;
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
        m.setMessageClass(MrcInterface.CLOCK);
        int i = m.putHeader(MrcPackets.SETCLOCKRATIOHEADER);
        
        m.setElement(i++, ratio);
        m.setElement(i++, 0x00);
        m.setElement(i++, getCheckSum(ratio, 0x00, 0x00, 0x00));
        m.replyNotExpected();
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
        m.setMessageClass(MrcInterface.CLOCK);
        int i = m.putHeader(MrcPackets.SETCLOCKTIMEHEADER);
        
        m.setElement(i++, hour);
        m.setElement(i++, 0x00);
        m.setElement(i++, minute);
        m.setElement(i++, 0x00);
        m.setElement(i++, getCheckSum(hour, 0x00, minute, 0x00));
        m.replyNotExpected();
        return m;
    }

    /**
     * Toggle the AM/PM vs 24 hour mode
     * @return
     */
    static public MrcMessage setClockAmPm() {
        MrcMessage m = new MrcMessage(MrcPackets.getSetClockAmPmPacketLength());
        m.setMessageClass(MrcInterface.CLOCK);
        int i = m.putHeader(MrcPackets.SETCLOCKAMPMHEADER);
        
        m.setElement(i++, 0x32);
        m.setElement(i++, 0x00);
        m.setElement(i++, getCheckSum(0x32, 0x00, 0x00, 0x00));
        m.replyNotExpected();
        return m;
    }
    
    /**
     * Set Track Power Off/Emergency Stop
     * @return
     */
    static public MrcMessage setPowerOff() {
        MrcMessage m = new MrcMessage(MrcPackets.getPowerOffPacketLength());
        m.setMessageClass(MrcInterface.POWER);
        m.putHeader(MrcPackets.POWEROFF);
        m.replyNotExpected();
        return m;
    }
    
    static public MrcMessage setPowerOn() {
        MrcMessage m = new MrcMessage(MrcPackets.getPowerOffPacketLength());
        m.setMessageClass(MrcInterface.POWER);
        m.putHeader(MrcPackets.POWERON);
        m.replyNotExpected();
        return m;
    }
    
    /**
       Get a message for a "Switch Possition Normal" command
       to a specific accessory decoder on the layout.
    */
    static MrcMessage getSwitchMsg(int address, boolean closed){
        MrcMessage m = new MrcMessage(MrcPackets.getAccessoryPacketLength());
        m.setMessageClass(MrcInterface.TURNOUTS);
        m.putHeader(MrcPackets.ACCESSORYPACKETHEADER);
        byte[] packet = jmri.NmraPacket.accDecoderPkt(address, closed);
        if(packet==null){
            return null;
        }
        //placeholder until code is written
        m.setElement(4,packet[0]);
        m.setElement(5,0x00);
        m.setElement(6,packet[1]);
        m.setElement(7,0x00);
        m.setElement(8,packet[2]);
        m.setElement(9,0x00);
        m.setTimeout(0); 
        m.setRetries(0);
        m.setByteStream();
	return m;
    }

    static public MrcMessage setNoData(){
        MrcMessage m = new MrcMessage(4);
        m.setMessageClass(MrcInterface.POLL);
        m.setElement(0,0x00);
        m.setElement(1,0x00);
        m.setElement(2,0x00);
        m.setElement(3,0x00);
//Message is throw away, so if it doesn't get transmited correctly then forget about it, don't attempt retry.
        m.setTimeout(0); 
        m.setRetries(0);
        m.setByteStream();
        return m;
    }
    
    byte[] byteStream;
    
    void setByteStream(){
        int len = getNumDataElements();
        byteStream = new byte[len];
        for (int i=0; i< len; i++){
             byteStream[i] = (byte) getElement(i);
        }
    }
    
    byte[] getByteStream(){ return byteStream; }
    
    public int getElement(int n) {        return _dataChars[n];}


    // accessors to the bulk data
    public int getNumDataElements() {        return _nDataChars;}

    public void setElement(int n, int v) {         _dataChars[n] = v;  }
    
    // contents (private)
    private int _nDataChars = 0;
    private int _dataChars[] = null;
    
    static Logger log = LoggerFactory.getLogger(MrcMessage.class.getName());

}


/* @(#)MrcMessage.java */

