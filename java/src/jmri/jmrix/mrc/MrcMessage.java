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
    
    int msgClass = 0x00;
    
    void setMessageClass(int i){
        msgClass = i;
    }
    
    public int getMessageClass(){
        return msgClass;
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
    
    static public MrcMessage getSendSpeed(int addressLo, int addressHi, int speed){
        MrcMessage m = new MrcMessage(MrcPackets.getThrottlePacketLength());
        m.setMessageClass(MrcInterface.THROTTLEINFO);
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
    //    m.setTimeout(100);
        return m;
    }
    
    static public MrcMessage getSendFunction(int group, int addressLo, int addressHi, int data){
        MrcMessage m = new MrcMessage(MrcPackets.getFunctionPacketLength());
        m.setMessageClass(MrcInterface.THROTTLEINFO);
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
        m.setMessageClass(MrcInterface.PROGRAMMING);
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
        m.setMessageClass(MrcInterface.PROGRAMMING);
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
        if(MrcPackets.startsWith(this, MrcPackets.readCVHeaderReply)){
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
        m.setMessageClass(MrcInterface.CLOCK);
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
        m.setMessageClass(MrcInterface.CLOCK);
        int i = m.putHeader(MrcPackets.setClockAmPmHeader);
        
        m.setElement(i++, 0x32);
        m.setElement(i++, 0x00);
        m.setElement(i++, getCheckSum(0x32, 0x00, 0x00, 0x00));
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
        log.info("Set byte stream");
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

