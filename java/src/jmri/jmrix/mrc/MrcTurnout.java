// MrcTurnout.java

package jmri.jmrix.mrc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Date;
import jmri.implementation.AbstractTurnout;
import jmri.Turnout;

/**
 * New MRC implementation of the Turnout interface
 * From Xpa+Modem implementation of the Turnout interface.
 * <P>
 *
 * @author	Paul Bender Copyright (C) 2004
 * @author      Martin Wade  Copyright (C) 2014
 * @version	$Revision: 22821 $
 */
public class MrcTurnout extends AbstractTurnout implements MrcTrafficListener{

    // Private data member to keep track of what turnout we control.
    int _number;
    MrcTrafficController tc = null;
	String prefix = "";

    /**
     * Mrc turnouts use any address allowed as an accessory decoder address 
     * on the particular command station.
     */
    public MrcTurnout(MrcTrafficController tc, String p, int number) {
        super(p+"T"+number);
        _number = number;
        this.tc = tc;
    	this.prefix = p + "T";
    }
    

    public int getNumber() { return _number; }

    // Handle a request to change state by sending a formatted DCC packet
    protected void forwardCommandChangeToLayout(int s) {
        MrcMessage m=null;
        // sort out states
        if ( (s & Turnout.CLOSED) > 0) {
            // first look for the double case, which we can't handle
            if ( (s & Turnout.THROWN) > 0) {
                // this is the disaster case!
                log.error("Cannot command both CLOSED and THROWN "+s);
                return;
            } else {
                // send a CLOSED command
                m=MrcMessage.getSwitchMsg(_number, true);
            }
        } else {
            // send a THROWN command
            m=MrcMessage.getSwitchMsg(_number, false);
        }
	if(m!=null) {
	   tc.sendMrcMessage(m);
	}

    }
    
    public void notifyRcv(Date timestamp, MrcMessage m) { /*message(m);*/ }
    public void notifyXmit(Date timestamp, MrcMessage m) {/* message(m); */}
    public void notifyFailedXmit(Date timestamp, MrcMessage m) { /*message(m);*/ }

    protected void turnoutPushbuttonLockout(boolean pushButtonLockout) { }
    
    static Logger log = LoggerFactory.getLogger(MrcTurnout.class.getName());

}

/* @(#)MrcTurnout.java */
