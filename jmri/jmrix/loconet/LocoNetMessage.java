// LocoNetMessage.java

package jmri.jmrix.loconet;

import java.io.Serializable;

/**
 * Represents a single command or response on the LocoNet.
 *<P>
 * Content is represented with ints to avoid the problems with
 * sign-extension that bytes have, and because a Java char is
 * actually a variable number of bytes in Unicode.
 *<P>
 * At present, this is primarily a wrapper class for the
 * raw message contents.  Only rudimentary tools for
 * creating and parsing messages are provided here, in part because
 * this class was created early in the project.  For a more recent
 * implementation of a similar idea, see the NceMessage class.  Many of the
 * ideas being tested there will eventually be moved back to here.
 * <P>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project.  That permission
 * does not extend to uses in other software products.  If you wish to
 * use this code, algorithm or these message formats outside of JMRI, please
 * contact Digitrax Inc for separate permission.
 * <P>
 *
 * @author			Bob Jacobsen  Copyright (C) 2001
 * @version			$Revision: 1.13 $
 * @see             jmri.jmrix.nce.NceMessage
 *
 */
public class LocoNetMessage implements Serializable {

	/** Create a new object, representing a specific-length message.
	 * @parameter len Total bytes in message, including opcode and error-detection byte.
	 */
	public LocoNetMessage(int len) {
		if (len<1)
			log.error("invalid length in call to ctor: "+len);
		_nDataBytes = len;
		_dataBytes = new int[len];
	}

        /**
         * Create a message with specified contents
         * @param contents The array of contents for the message. The error
         * check word must be present, e.g. a 4-byte message must have
         * four values in the array
         */
        public LocoNetMessage(int[] contents) {
            this(contents.length);
            for (int i=0; i<contents.length; i++) this.setElement(i, contents[i]);
        }

	public void setOpCode(int i) { _dataBytes[0]=i;}
	public int getOpCode() {return _dataBytes[0];}

	/** Get a String representation of the op code in hex */
	public String getOpCodeHex() { return "0x"+Integer.toHexString(getOpCode()); }

	/** Get length, including op code and error-detection byte */
	public int getNumDataElements() {return _nDataBytes;}
	public int getElement(int n) {
		if (n < 0 || n >= _dataBytes.length)
					log.error("reference element "+n
								+" in message of "+_dataBytes.length
								+" elements: "+this.toString());
		return _dataBytes[n];
	}
	public void setElement(int n, int v) {
		if (n < 0 || n >= _dataBytes.length)
					log.error("reference element "+n
								+" in message of "+_dataBytes.length
								+" elements: "+this.toString());
		_dataBytes[n] = v;
	}

	/** Get a String representation of the entire message in hex */
	public String toString() {
		String s = "";
		for (int i=0; i<_nDataBytes; i++) s+=Integer.toHexString(_dataBytes[i])+" ";
		return s;
	}

	/**
	 * check whether the message has a valid parity
	 */
	public boolean checkParity() {
		int len = getNumDataElements();
		int chksum = 0xff;  /* the seed */
   		int loop;

    	for(loop = 0; loop < len-1; loop++) {  // calculate contents for data part
        	chksum ^= getElement(loop);
        }
		return (chksum == getElement(len-1));
	}

    // decode messages of a particular form

    // create messages of a particular form
    /**
     * Get the 8 data bytes from an OPC_PEER_XFR message
     * @return int[8] data bytes
     */
    public int[] getPeerXfrData() {
        if (getOpCode()!=LnConstants.OPC_PEER_XFER)
            log.error("getPeerXfrData called with wrong opcode "+getOpCode());
        if (getElement(1)!=0x10)
            log.error("getPeerXfrData called with wrong secondary code "+getElement(1));
        if (getNumDataElements()!=16)
            log.error("getPeerXfrData called with wrong length "+getNumDataElements());

        int[] data = new int[]{0,0,0,0,  0,0,0,0};

        int pxct1 = getElement(5);
        int pxct2 = getElement(10);

        // fill the 8 data items
        data[0]= (getElement(6)&0x7F)+((pxct1&0x01)!=0?0x80:0);
        data[1]= (getElement(7)&0x7F)+((pxct1&0x02)!=0?0x80:0);
        data[2]= (getElement(8)&0x7F)+((pxct1&0x04)!=0?0x80:0);
        data[3]= (getElement(9)&0x7F)+((pxct1&0x08)!=0?0x80:0);

        data[4]= (getElement(11)&0x7F)+((pxct2&0x01)!=0?0x80:0);
        data[5]= (getElement(12)&0x7F)+((pxct2&0x02)!=0?0x80:0);
        data[6]= (getElement(13)&0x7F)+((pxct2&0x04)!=0?0x80:0);
        data[7]= (getElement(14)&0x7F)+((pxct2&0x08)!=0?0x80:0);

        return data;
    }

    /**
     * Two messages are the same if their entire data content
     * is the same.  We ignore the error-check byte to ease
     * comparisons before a message is transmitted.
     * @param obj
     * @return
     */
    public boolean equals(LocoNetMessage m) {
        if (m._nDataBytes != this._nDataBytes) return false;
        for (int i = 0; i<_nDataBytes-1; i++)
            if (m._dataBytes[i]!=this._dataBytes[i]) return false;
	return true;
    }

    /**
     * Return a newly created OPC_PEER_XFR message.
     * @param src Source address
     * @param dst Destination address
     * @param d   int[8] for the data contents or null
     * @param code The instruction code placed in the pcxt1 pcxt2 bytes
     * @return    The formatted message
     */
    static public LocoNetMessage makePeerXfr(int src, int dst, int[] d, int code) {
        LocoNetMessage msg = new LocoNetMessage(16);
        msg.setOpCode(0xE5);
        msg.setElement(1, 0x10);  // 2nd part of op code

        // accumulate the pxct1,2 bytes
        int pxct1 = 0;
        int pxct2 = 0;

        // install the "CODE" in pxct1, pxct2
        pxct1 |= (code&0x7)*0x10;
        pxct2 |= ( (code&0x38)/8)*0x10;

        // store the addresses
        msg.setElement(2,src&0x7F); //src
        msg.setElement(3,dst&0x7F); //dstl
        msg.setElement(4,highByte(dst)&0x7F); //dsth

        // store the data bytes
        msg.setElement(6, d[0]&0x7F);
        if (highBit(d[0])) pxct1 |= 0x01;
        msg.setElement(7, d[1]&0x7F);
        if (highBit(d[1])) pxct1 |= 0x02;
        msg.setElement(8, d[2]&0x7F);
        if (highBit(d[2])) pxct1 |= 0x04;
        msg.setElement(9, d[3]&0x7F);
        if (highBit(d[3])) pxct1 |= 0x08;

        msg.setElement(11, d[4]&0x7F);
        if (highBit(d[4])) pxct2 |= 0x01;
        msg.setElement(12, d[5]&0x7F);
        if (highBit(d[5])) pxct2 |= 0x02;
        msg.setElement(13, d[6]&0x7F);
        if (highBit(d[6])) pxct2 |= 0x04;
        msg.setElement(14, d[7]&0x7F);
        if (highBit(d[7])) pxct2 |= 0x08;

        // store the pxct1,2 values
        msg.setElement( 5, pxct1);
        msg.setElement(10, pxct2);

        return msg;
    }

    /**
     * Check if a high bit is set, usually used to store it in some
     * other location (LocoNet does not allow the high bit to be set
     * in data bytes)
     * @param val
     * @return True if the argument has the high bit set
     */
    static protected boolean highBit(int val) {
        if ((val&(~0xFF)) != 0) log.error("highBit called with too large value: "
                                        +Integer.toHexString(val));
        return (0!=(val&0x80));
    }

    static protected int lowByte(int val) { return val&0xFF; }
    static protected int highByte(int val) {
        if ((val&(~0xFFFF)) != 0) log.error("highByte called with too large value: "
                                        +Integer.toHexString(val));
        return (val&0xFF00)/256;
    }

    /**
     * Sensor-format 0-n address
     * @return 0 to n-1 address
     */
    public int sensorAddr() {
        int sw1 = getElement(1);
        int sw2 = getElement(2);
		int as = sw2&0x20;		// should be a LocoNet constant?
        int high = sw2&0x0F;
		int low = sw1&0x7F;
		return high*256+low*2+(as!=0 ? 1 : 0);
    }

    /**
     * If this is an OPC_INPUT_REP, return the 0-n address, else -1
     * @return 0 to n-1 address
     */
    public int inputRepAddr() {
        if (getOpCode()==LnConstants.OPC_INPUT_REP) {
            return sensorAddr();
        } else return -1;
    }

    /**
     * Return the 1-N turnout address
     * @return 1-N address
     */
    public int turnoutAddr() {
        int a1 = getElement(1);
        int a2 = getElement(2);
        return (((a2 & 0x0f) * 128) + (a1 & 0x7f))+1;
    }

	// contents (private)
	private int _nDataBytes = 0;
	private int _dataBytes[] = null;

	// initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LocoNetMessage.class.getName());

}


/* @(#)LocoNetMessage.java */
