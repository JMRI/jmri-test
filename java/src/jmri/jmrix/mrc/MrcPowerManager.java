// LnPowerManager.java

package jmri.jmrix.mrc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.PowerManager;
import jmri.JmriException;
import java.util.Date;

/**
 * PowerManager implementation for controlling layout power
 * <P>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project.  That permission
 * does not extend to uses in other software products.  If you wish to
 * use this code, algorithm or these message formats outside of JMRI, please
 * contact Digitrax Inc for separate permission.
 * <P>
 * @author	Bob Jacobsen Copyright (C) 2001
 * @version         $Revision: 22998 $
 */
public class MrcPowerManager
        extends jmri.managers.AbstractPowerManager
        implements PowerManager, MrcTrafficListener {

    public MrcPowerManager(MrcSystemConnectionMemo memo) {
        super(memo);
        // standard Mrc - connect
        if(memo.getMrcTrafficController()==null){
            log.error("Power Manager Created, yet there is no Traffic Controller");
            return;
        }
        this.tc = memo.getMrcTrafficController();
        tc.addTrafficListener(MrcInterface.POWER, this);
        
        //updateTrackPowerStatus();  // this delays a while then reads slot 0 to get current track status
    }
    
    protected int power = UNKNOWN;

    public void setPower(int v) throws JmriException {
        power = UNKNOWN;

        checkTC();
        if (v==ON) {
            // send GPON
            MrcMessage l = new MrcMessage(2);
            tc.sendMrcMessage(l);
        } else if (v==OFF) {
            // send GPOFF
            MrcMessage l = new MrcMessage(2);
            tc.sendMrcMessage(l);
        }

        firePropertyChange("Power", null, null);
    }

	public int getPower() { return power;}
    
    // these next three public methods have been added so that other classes
    // do not need to reference the static final values "ON", "OFF", and "UKNOWN".
    public boolean isPowerOn() {return (power == ON);}
    public boolean isPowerOff() {return (power == OFF);}
    public boolean isPowerUnknown() {return (power == UNKNOWN);}

    // to free resources when no longer used
    public void dispose() {
		if (tc!=null) tc.removeTrafficListener(MrcInterface.POWER, this);
        tc = null;
    }

    MrcTrafficController tc = null;

    private void checkTC() throws JmriException {
		if (tc == null) throw new JmriException("Use power manager after dispose");
        }
        
    public void notifyRcv(Date timestamp, MrcMessage m) { /*message(m);*/ }
    public void notifyXmit(Date timestamp, MrcMessage m) {/* message(m);*/ }
    public void notifyFailedXmit(Date timestamp, MrcMessage m) { /*message(m);*/ }
    
    static Logger log = LoggerFactory.getLogger(MrcPowerManager.class.getName());
}

/* @(#)LnPowerManager.java */
