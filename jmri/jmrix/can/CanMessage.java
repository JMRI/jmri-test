// CanMessage.java

package jmri.jmrix.can;

import jmri.jmrix.AbstractMRMessage;

/**
 * Base class for messages in a CANbus based message/reply protocol.
 * <P>
 * It is expected that any CAN based system will be based upon basic CANbus
 * concepts such as ID (standard or extended), Normal and RTR frames and
 * a data field.
 * <P>
 * The _dataChars[] and _nDataChars members refer to the data field, not the
 * entire message.
 *<p>
 * "header" refers to the full 11 or 29 bit header; which mode is separately
 * set via the "extended" parameter
 *<p>
 * CBUS uses a 2-bit "Pri" field and 7-bit "ID" ("CAN ID") field, with
 * separate accessors.
 *
 * @author      Andrew Crosland Copyright (C) 2008
 * @author      Bob Jacobsen Copyright (C) 2008, 2009
 * @version     $Revision: 1.7 $
 */
public class CanMessage extends AbstractMRMessage {
    
    // tag whether translation is needed.
    // a "native" message has been converted already
    boolean _translated = false;
    public void setTranslated(boolean translated) { _translated = translated; }
    public boolean isTranslated() { return _translated; }
    
    // Creates a new instance of CanMessage
    public CanMessage() {
        _header = 0;
        setHeader(0x7a);  // default value
        _isExtended = false;
        _isRtr = false;
        _nDataChars = 8;
        setBinary(true);
        _dataChars = new int[8];
    }
    
    // create a new one of given length
    public CanMessage(int i) {
        this();
        _nDataChars = (i <= 8) ? i : 8;
    }
    
    // create a new one from an array
    public CanMessage(int [] d) {
        this();
        _nDataChars = (d.length <= 8) ? d.length : 8;
        for (int i = 0; i < _nDataChars; i++) {
            _dataChars[i] = d[i];
        }
    }
    
    // copy one
    public  CanMessage(CanMessage m) {
        if (m == null)
            log.error("copy ctor of null message");
        _header = m._header;
        _isExtended = m._isExtended;
        _isRtr = m._isRtr;
        setBinary(true);
        _nDataChars = m._nDataChars;
        _dataChars = new int[_nDataChars];
        for (int i = 0; i<_nDataChars; i++) _dataChars[i] = m._dataChars[i];
    }
    
    /** 
     * Note that a CanMessage and a CanReply can be tested for equality
     */
    public boolean equals(Object a) {
        if (a.getClass().equals(CanMessage.class)) {
            CanMessage m = (CanMessage) a;
            if ( (_header!=m.getHeader())||(_isRtr!=m.isRtr())||(_isExtended!=m.isExtended()))
                return false;
            if ( _nDataChars != m.getNumDataElements() ) return false;
            for (int i = 0; i<_nDataChars; i++) {
                if (_dataChars[i] != m.getElement(i)) return false;
            }
            return true;
        } else if (a.getClass().equals(CanReply.class)) {
            CanReply m = (CanReply) a;
            if ( (_header!=getHeader())||(_isRtr!=m.isRtr())||(_isExtended!=m.isExtended()))
                return false;
            if ( _nDataChars != m.getNumDataElements() ) return false;
            for (int i = 0; i<_nDataChars; i++) {
                if (_dataChars[i] != m.getElement(i)) return false;
            }
            return true;
        } else return false;
    }
    
    public boolean replyExpected() { return false; }
    
    // accessors to the bulk data
    public int getNumDataElements() { return _nDataChars;}
    public void setNumDataElements(int n) { _nDataChars = n; }
    public int getElement(int n) {return _dataChars[n];}
    public void setElement(int n, int v) {
        _dataChars[n] = v;
    }
    
    public void setData(int [] d) {
        int len = (d.length <=8) ? d.length : 8;
        for (int i = 0; i < len; i++) {
            _dataChars[i] = d[i];
        }
    }
    
    
    // CAN header
    public int getHeader() { return _header; }
    public void setHeader(int h) { _header = h; }
    
    public boolean isExtended() { return _isExtended; }
    public void setExtended(boolean b) { _isExtended = b; }
    
    public boolean isRtr() { return _isRtr; }
    public void setRtr(boolean b) { _isRtr = b; }
    
    // contents (private)
    private int _header;
    private boolean _isExtended;
    private boolean _isRtr;
        
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CanMessage.class.getName());
}

/* @(#)CanMessage.java */
