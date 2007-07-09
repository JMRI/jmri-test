// SerialTurnout.java

package jmri.jmrix.tmcc;

import jmri.AbstractTurnout;
import jmri.Turnout;

/**
 * SerialTurnout.java
 *
 *  This object doesn't listen to the TMCC communications.  This is because
 *  it should be the only object that is sending messages for this turnout;
 *  more than one Turnout object pointing to a single device is not allowed.
 *
 * Description:		extend jmri.AbstractTurnout for TMCC serial layouts
 * @author			Bob Jacobsen Copyright (C) 2003, 2006
 * @version			$Revision: 1.3 $
 */
public class SerialTurnout extends AbstractTurnout {

    public SerialTurnout(int number) {
        super("TT"+number);
        _number = number;
    }
    int _number;
    
    /**
     * Handle a request to change state by sending a turnout command
     */
    protected void forwardCommandChangeToLayout(int s) {

        // sort out states
        if ( (s & Turnout.CLOSED) > 0) {
            // first look for the double case, which we can't handle
            if ( (s & Turnout.THROWN) > 0) {
                // this is the disaster case!
                log.error("Cannot command both CLOSED and THROWN "+s);
                return;
            } else {
                // send a CLOSED command
                sendMessage(true^getInverted());
            }
        } else {
            // send a THROWN command
            sendMessage(false^getInverted());
        }
    }

    public void dispose() {}  // no connections need to be broken

    protected void sendMessage(boolean closed) {
        SerialMessage m = new SerialMessage();
        m.setOpCode(0xFE);
        if (closed)
            m.putAsWord(0x4000+_number*128);
        else
            m.putAsWord(0x401F+_number*128);
        SerialTrafficController.instance().sendSerialMessage(m, null);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialTurnout.class.getName());
}

/* @(#)SerialTurnout.java */
