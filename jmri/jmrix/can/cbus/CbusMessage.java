/*
 * CbusMessage.java
 *
 */

package jmri.jmrix.can.cbus;

import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.Programmer;


/**
 * Class to allow use of CBUS concepts to access the underlying
 * can message
 *
 * @author          Andrew Crosland Copyright (C) 2008
 * @version         $Revision: 1.7 $
 */
public class CbusMessage {
    /* Methods that take a CanMessage as argument */
    public static int getId(CanMessage m) { return m.getHeader()&0x7f; }
    public static int getPri(CanMessage m) { return (m.getHeader()>>7)&0x0F; }

    public static int getOpcode(CanMessage m) {
        return m.getElement(0);
    }
    public static int getDataLength(CanMessage m) {
        return m.getElement(0)>>5;
    }
    public static int getNodeNumber(CanMessage m) {
        if (isEvent(m))
            return m.getElement(1)*256 + m.getElement(2);
        else
            return 0;
    }
    public static int getEvent(CanMessage m) {
        if (isEvent(m))
            return m.getElement(3)*256 + m.getElement(4);
        else
            return 0;
    }
    public static int getEventType(CanMessage m) {
        if ((m.getElement(0) & 1) == 1)
            return CbusConstants.EVENT_OFF;
        else
            return CbusConstants.EVENT_ON;
    }
    public static boolean isEvent(CanMessage m) {
        if ((m.getElement(0) == 0x90) || (m.getElement(0) == 0x91))
            return true;
        else
            return false;
    }

    public static void setId(CanMessage m, int id) {
        if ( (id& ~0x7f) != 0 ) 
            throw new IllegalArgumentException("invalid ID value: "+id);
        int update = m.getHeader();
        m.setHeader( (update&~0x07f) | id);
    }

    public static void setPri(CanMessage m, int pri) {
        if ( (pri& ~0x0F) != 0 )
            throw new IllegalArgumentException("invalid CBUS Pri value: "+pri);
        int update = m.getHeader();
        m.setHeader( (update&~0x780) | (pri << 7));
    }
    
     public static String toAddress(CanMessage m) {
        if (m.getElement(0) == 0x90) {
            // + form
            return "+n"+(m.getElement(1)*256+m.getElement(2))+"e"+(m.getElement(3)*256+m.getElement(4));
        } else if (m.getElement(0) == 0x91) {
            // - form
            return "-n"+(m.getElement(1)*256+m.getElement(2))+"e"+(m.getElement(3)*256+m.getElement(4));
        } else {
            // hex form
            return "x"+m.toString().replaceAll(" ","");
        }      
    }

    public static boolean isRequestTrackOff(CanMessage m) {
        if (m.getOpCode() == CbusConstants.CBUS_RTOF) return true;
        return false;
    }

    public static boolean isRequestTrackOn(CanMessage m) {
        if (m.getOpCode() == CbusConstants.CBUS_RTON) return true;
        return false;
    }

    /* 
     * Methods that take a CanReply as argument
     */
    public static int getId(CanReply r) { return r.getHeader()&0x7f; }
    public static int getPri(CanReply r) { return (r.getHeader()>>7)&0x0F; }

    public static int getOpcode(CanReply r) {
        return r.getElement(0);
    }
    public static int getDataLength(CanReply r) {
        return r.getElement(0)>>5;
    }
    public static int getNodeNumber(CanReply r) {
        if (isEvent(r))
            return r.getElement(1)*256 + r.getElement(2);
        else
            return 0;
    }
    public static int getEvent(CanReply r) {
        if (isEvent(r))
            return r.getElement(3)*256 + r.getElement(4);
        else
            return 0;
    }
    public static int getEventType(CanReply r) {
        if ((r.getElement(0) & 1) == 1)
            return CbusConstants.EVENT_OFF;
        else
            return CbusConstants.EVENT_ON;
    }
    public static boolean isEvent(CanReply r) {
        if ((r.getElement(0) == 0x90) || (r.getElement(0) == 0x91))
            return true;
        else
            return false;
    }

    public static void setId(CanReply r, int id) {
        if ( (id& ~0x7f) != 0 )
            throw new IllegalArgumentException("invalid ID value: "+id);
        int update = r.getHeader();
        r.setHeader( (update&~0x07f) | id);
    }

    public static void setPri(CanReply r, int pri) {
        if ( (pri& ~0x0F) != 0 )
            throw new IllegalArgumentException("invalid CBUS Pri value: "+pri);
        int update = r.getHeader();
        r.setHeader( (update&~0x780) | (pri << 7));
    }

    public static String toAddress(CanReply r) {
        if (r.getElement(0) == 0x90) {
            // + form
            return "+n"+(r.getElement(1)*256+r.getElement(2))+"e"+(r.getElement(3)*256+r.getElement(4));
        } else if (r.getElement(0) == 0x91) {
            // - form
            return "-n"+(r.getElement(1)*256+r.getElement(2))+"e"+(r.getElement(3)*256+r.getElement(4));
        } else {
            // hex form
            return "x"+r.toString().replaceAll(" ","");
        }      
    }
    
    public static boolean isTrackOff(CanReply m) {
        if (m.getOpCode() == CbusConstants.CBUS_TOF) return true;
        return false;
    }

    public static boolean isTrackOn(CanReply m) {
        if (m.getOpCode() == CbusConstants.CBUS_TON) return true;
        return false;
    }

    public static boolean isArst(CanReply m) {
        if (m.getOpCode() == CbusConstants.CBUS_ARST) return true;
        return false;
    }

    /**
     * CBUS programmer commands
    */
    static public CanMessage getReadCV(int cv, int mode) {
        CanMessage m = new CanMessage(5);
        m.setElement(0, CbusConstants.CBUS_QCVS);
        m.setElement(1, CbusConstants.SERVICE_HANDLE);
        m.setElement(2, cv/256);
        m.setElement(3, cv & 0xff);
        if (mode == Programmer.PAGEMODE) {
          m.setElement(4, CbusConstants.CBUS_PROG_PAGED);
        } else if (mode == Programmer.DIRECTBITMODE) {
          m.setElement(4, CbusConstants.CBUS_PROG_DIRECT_BIT);
        } else if (mode == Programmer.DIRECTBYTEMODE) {
          m.setElement(4, CbusConstants.CBUS_PROG_DIRECT_BYTE);
        } else {
          m.setElement(4, CbusConstants.CBUS_PROG_REGISTER);
        }
        setPri(m, 0xb);
        return m;
    }

    static public CanMessage getWriteCV(int cv, int val, int mode) {
        CanMessage m = new CanMessage(6);
        m.setElement(0, CbusConstants.CBUS_WCVS);
        m.setElement(1, CbusConstants.SERVICE_HANDLE);
        m.setElement(2, cv/256);
        m.setElement(3, cv & 0xff);
        if (mode == Programmer.PAGEMODE) {
          m.setElement(4, CbusConstants.CBUS_PROG_PAGED);
        } else if (mode == Programmer.DIRECTBITMODE) {
          m.setElement(4, CbusConstants.CBUS_PROG_DIRECT_BIT);
        } else if (mode == Programmer.DIRECTBYTEMODE) {
          m.setElement(4, CbusConstants.CBUS_PROG_DIRECT_BYTE);
        } else {
          m.setElement(4, CbusConstants.CBUS_PROG_REGISTER);
        }
        m.setElement(5, val);
        setPri(m, 0xb);
        return m;
    }

    /**
     * CBUS Ops mode programmer commands
    */
    static public CanMessage getOpsModeWriteCV(int mAddress, boolean mLongAddr, int cv, int val) {
        CanMessage m = new CanMessage(7);
        int address = mAddress;
        m.setElement(0, CbusConstants.CBUS_WCVOA);
        if (mLongAddr) {
            address = address | 0xc000;
        }
        m.setElement(1, address/256);
        m.setElement(2, address & 0xff);
        m.setElement(3, cv/256);
        m.setElement(4, cv & 0xff);
        m.setElement(5, CbusConstants.CBUS_OPS_BYTE);
        m.setElement(6, val);
        setPri(m, 0xb);
        return m;
    }

    /**
     * CBUS Ppower commands
     */
    static public CanMessage getRequestTrackOn() {
        CanMessage m = new CanMessage(1);
        m.setElement(0, CbusConstants.CBUS_RTON);
        setPri(m, 0xb);
        return m;
    }

    static public CanMessage getRequestTrackOff() {
        CanMessage m = new CanMessage(1);
        m.setElement(0, CbusConstants.CBUS_RTOF);
        setPri(m, 0xb);
        return m;
    }

}
