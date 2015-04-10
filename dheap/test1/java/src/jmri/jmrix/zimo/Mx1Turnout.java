// Mx1Turnout.java
package jmri.jmrix.zimo;

import jmri.Turnout;
import jmri.implementation.AbstractTurnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * New Zimo Binary implementation of the Turnout interface
 * <P>
 *
 * @author	Kevin Dickerson Copyright (C) 2014
 * @version	$Revision: 22821 $
 */
public class Mx1Turnout extends AbstractTurnout /*implements Mx1TrafficListener*/ {

    /**
     *
     */
    private static final long serialVersionUID = 8921768041774861293L;
    // Private data member to keep track of what turnout we control.
    int _number;
    Mx1TrafficController tc = null;
    String prefix = "";

    /**
     * Mx1 turnouts use any address allowed as an accessory decoder address on
     * the particular command station.
     */
    public Mx1Turnout(int number, Mx1TrafficController tc, String p) {
        super(p + "T" + number);
        _number = number;
        this.tc = tc;
        this.prefix = p + "T";
        //tc.addMx1Listener(Mx1Interface.TURNOUTS, null);
    }

    public int getNumber() {
        return _number;
    }

    // Handle a request to change state by sending a formatted DCC packet
    protected void forwardCommandChangeToLayout(int s) {
        // sort out states
        if ((s & Turnout.CLOSED) > 0) {
            // first look for the double case, which we can't handle
            if ((s & Turnout.THROWN) > 0) {
                // this is the disaster case!
                log.error("Cannot command both CLOSED and THROWN " + s); //IN18N
                return;
            } else {
                // send a CLOSED command
                forwardToCommandStation(jmri.Turnout.THROWN);
            }
        } else {
            // send a THROWN command
            forwardToCommandStation(jmri.Turnout.CLOSED);
        }
    }

    void forwardToCommandStation(int state) {
        Mx1Message m = null;
        m = Mx1Message.getSwitchMsg(_number, state, true);
        tc.sendMx1Message(m, null);
    }

    protected void turnoutPushbuttonLockout(boolean pushButtonLockout) {
    }

    static Logger log = LoggerFactory.getLogger(Mx1Turnout.class.getName());

}

/* @(#)Mx1Turnout.java */
