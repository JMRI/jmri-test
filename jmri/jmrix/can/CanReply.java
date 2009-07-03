// CanReply.java

package jmri.jmrix.can;

import jmri.jmrix.AbstractMRReply;

/**
 * Base class for replies in a CANbus based message/reply protocol.
 * <P>
 * It is expected that any CAN based system will be based upon basic CANbus
 * concepts such as ID (standard or extended), Normal and RTR frames and
 * a data field.
 *<p>
 * "header" refers to the full 11 or 29 bit header; which mode is separately
 * set via the "extended" parameter
 *<p>
 * CBUS uses a 2-bit "Pri" field and 7-bit "ID" ("CAN ID") field, with
 * separate accessors.
 *
 * @author      Andrew Crosland Copyright (C) 2008
 * @author      Bob Jacobsen Copyright (C) 2008, 2009
 * @version         $Revision: 1.12 $
 */
public class CanReply extends AbstractMRReply {
        
    // Creates a new instance of CanMessage
    public CanReply() {
        _isExtended = false;
        _isRtr = false;
        _nDataChars = 8;
        setBinary(true);
        _dataChars = new int[8];
    }
    
    // create a new one of given length
    public CanReply(int i) {
        this();
        _nDataChars = (i <= 8) ? i : 8;
    }
    
    // create a new one from an array
    public CanReply(int [] d) {
        this();
        _nDataChars = (d.length <= 8) ? d.length : 8;
        for (int i = 0; i < _nDataChars; i++) {
            _dataChars[i] = d[i];
        }
    }
    
    // copy one
    @SuppressWarnings("null")
	public  CanReply(CanReply m) {
        if (m == null)
            log.error("copy ctor of null message");
        _header = m._header;
        _isExtended = m._isExtended;
        _isRtr = m._isRtr;
        _nDataChars = m._nDataChars;
        setBinary(true);
        _dataChars = new int[_nDataChars];
        for (int i = 0; i<_nDataChars; i++)
            _dataChars[i] = m._dataChars[i];
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
    
    protected int skipPrefix(int index) { return index; }
    
    // accessors to the bulk data
    public int getNumDataElements() { return _nDataChars;}
    public void setNumDataElements(int n) { _nDataChars = (n <= 8) ? n : 8;}
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
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CanReply.class.getName());
}

/* @(#)CanReply.java */
