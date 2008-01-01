// SerialMessage.java

package jmri.jmrix.grapevine;

import jmri.util.StringUtil;

/**
 * Contains the data payload of a serial
 * packet.
 * 
 * @author    Bob Jacobsen  Copyright (C) 2001,2003, 2006, 2007
 * @version   $Revision: 1.1 $
 */

public class SerialMessage extends jmri.jmrix.AbstractMRMessage {
    // is this logically an abstract class?

    public SerialMessage() {
        super(4);  // all messages are four bytes, binary
        setBinary(true);
    }
    
    // copy one
    public  SerialMessage(SerialMessage m) {
        super(m);
        setBinary(true);
    }

    /**
     * This ctor interprets the String as the exact
     * sequence to send, byte-for-byte.
     * @param m
     */
    public  SerialMessage(String m) {
        super(m);
        setBinary(true);
    }

    /**
     * This ctor interprets the byte array as
     * a sequence of characters to send.
     * @param a Array of bytes to send
     */
    public  SerialMessage(byte[] a) {
        super(String.valueOf(a));
        setBinary(true);
    }
    
    // no replies expected, don't wait for them
    public boolean replyExpected() { return false; }

    // static methods to recognize a message
    public int getAddr() { return getElement(0)&0x7F; }

    // static methods to return a formatted message
    
    /**
     * For Grapevine, which doesn't have a data poll,
     * the poll operation is only used to see that the nodes
     * are present.  This is done by sending a "get software version"
     * command.
     */
    static public SerialMessage getPoll(int addr) {
        // eventually this will have to include logic for reading 
        // various bytes on the card, but our supported 
        // cards don't require that yet
        SerialMessage m = new SerialMessage();
        m.setElement(0, addr | 0x80);
        m.setElement(1, 119);  // get software version
        m.setElement(2, addr | 0x80);  // read first two bytes
        m.setElement(3, 119);  // send twice, without parity
        return m;
    }

    public void setBank(int b) {
        if ((b>7) || (b<0)) log.error("Setting back to bad value: "+b);
        int old = getElement(3)&0xF;
        setElement(3,old | ( (b&0x7)<<4 ));
    }
    
    public void setParity() {
        int sum  = getElement(0) & 0x0F;
        sum     += (getElement(0)&0x70)>>4;
        sum     += getElement(1)&0x0F;
        sum     += (getElement(1)&0x70)>>4;
        sum     += (getElement(3)&0x70)>>4;
        int parity = 16 - sum;
        setElement(3, getElement(3) | (parity&0xF));
    }
    
    /**
     * Format the reply as human-readable text.
     */
    public String format() {
        return staticFormat(getElement(0)&0xff, getElement(1)&0xff, getElement(2)&0xff, getElement(3)&0xff);
    }

    static String staticFormat(int b1, int b2, int b3, int b4) {
        String result = "address: "+(b1&0x7F)
                +" data bytes: 0x"+StringUtil.twoHexFromInt(b2)
                +" 0x"+StringUtil.twoHexFromInt(b4)
                +" => ";
                
        // Check special cases
        switch (b2) {
        case 0x77:  // software status query
            result += "software version query "+(b4>>4);
            return result;
        default:
            result += "Misc w bank "+((b4&0x70)>>4);
            result += " signal "+((b2&0x78)>>3);
            int cmd = b2&0x07;
            result += " cmd "+cmd;
            result +=" (set "+colorAsString(cmd);
            if (cmd ==0) result += "/closed";
            if (cmd ==6) result += "/thrown";
            result +=")";
            return result;
        }
    }

    static String[] colors = new String[]{"green","flashing green","yellow","flashing yellow","off","flashing off","red","flashing red"};
    
    static String colorAsString(int color) { return colors[color]; }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialMessage.class.getName());
}

/* @(#)SerialMessage.java */
