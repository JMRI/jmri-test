// GridConnectReply.java

package jmri.jmrix.can.adapters.gridconnect;

import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.can.CanReply;

/**
 * Class for replies in a GridConnect based message/reply protocol.
 * <P>
 * This is a message in the GridConnect format, e.g. ":S123N12345678;"
 * 
 * @author                      Andrew Crosland Copyright (C) 2008
 * @author                      Bob Jacobsen Copyright (C) 2008
 * @version			$Revision: 1.8 $
 */
public class GridConnectReply extends AbstractMRReply {
    
    static final int MAXLEN = 27;
    
    // Creates a new instance of GridConnectReply
    public GridConnectReply() {
        _nDataChars = 0;
        _dataChars = new int[MAXLEN];
    }

    public GridConnectReply(String s) {
        _nDataChars = s.length();
        for (int i = 0; i<s.length(); i++)
            _dataChars[i] = s.charAt(i);
    }
    
    public CanReply createReply() {
        CanReply ret = new CanReply();

        if (log.isDebugEnabled()) log.debug("createReply converts from ["+this+"]");
        // Is it an Extended frame?
	    if (isExtended()) ret.setExtended(true);
	    
	    // Copy the header
        ret.setHeader(getHeader());

        // Is it an RTR frame?
	    if (isRtr()) ret.setRtr(true);

        // Get the data
        for (int i = 0; i < getNumBytes(); i++) {
            ret.setElement(i, getByte(i));
        }
        ret.setNumDataElements(getNumBytes());
        return ret;
    }
    
    protected int skipPrefix(int index) {
        while (_dataChars[index] == ':') { index++; }
        return index;
    }

    // accessors to the bulk data
    public int getNumDataElements() { return _nDataChars;}
    public void setNumDataElements(int n) { _nDataChars = (n <= MAXLEN) ? n : MAXLEN; }
    public int getElement(int n) {return _dataChars[n];}
    public void setElement(int n, int v) {
        if (n < MAXLEN) {
            _dataChars[n] = v;
            _nDataChars = Math.max(_nDataChars, n+1);
        }
    }

    public boolean isExtended() {return (getElement(1) == 'X');}
    public boolean isRtr() {return (getElement(_RTRoffset) == 'R');}
    
    // 
    public int maxSize() { return MAXLEN; }
    
    public void setData(int [] d) {
        int len = (d.length <=MAXLEN) ? d.length : MAXLEN;
        for (int i = 0; i < len; i++) {
            _dataChars[i] = d[i];
        }
    }

    // pointer to the N or R character
    int _RTRoffset = -1;
    
    /**
     * Get the CAN header by using chars from 2 to up to 9
     *
     * @return the CAN header as an int
     */        
    public int getHeader() {
        int val = 0;
        for (int i = 2; i<=10; i++) {
            _RTRoffset = i;
            if (_dataChars[i] == 'N') return val;
            if (_dataChars[i] == 'R') return val;
            val = val*16 + getHexDigit(i);
        }
        return val;
    }
        
    /**
     * Get the number of data bytes
     *
     * @return int the number of bytes
     */
    public int getNumBytes() {
        // subtract framing and ID bytes, etc and each byte is two ASCII hex digits
        return (_nDataChars - (_RTRoffset+1))/2;
    }
    
    /**
     * Get a hex data byte from the message
     * <P>
     * Data bytes are encoded as two ASCII hex digits starting at byte 7 of the
     * message.
     *
     * @param b The byte offset (0 - 7)
     * @return The value
     */
    public int getByte(int b) {
        if ((b >= 0) && (b <= 7)) {
            int index = b*2 + _RTRoffset+1;
            int hi = getHexDigit(index++);
            int lo = getHexDigit(index);
            if ((hi < 16) && (lo < 16)) {
                return (hi*16 + lo);
            }
        }
        return 0;
    }
    
    // Get a single hex digit. returns 0 if digit is invalid
    private int getHexDigit(int index) {
        int b = 0;
        b = _dataChars[index];
        if ((b >= '0') && (b <= '9')) {
            b = b - '0';
        } else if ((b >= 'A') && (b <= 'F')) {
            b = b - 'A' + 10;
        } else if ((b >= 'a') && (b <= 'f')) {
            b = b - 'a' + 10;
        } else {
            b = 0;
        }
        return (byte)b;
    }
   
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(GridConnectReply.class.getName());
}

/* @(#)GridConnectReply.java */
