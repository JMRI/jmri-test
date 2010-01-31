package jmri.jmrix.ecos;

import jmri.DccThrottle;
import jmri.LocoAddress;
import jmri.DccLocoAddress;

import jmri.jmrix.AbstractThrottleManager;

/**
 * EcosDCC implementation of a ThrottleManager.
 * <P>
 * Based on early NCE code.
 *
 * @author	    Bob Jacobsen  Copyright (C) 2001, 2005
 * @author Modified by Kevin Dickerson
 * @version         $Revision: 1.4 $
 */
public class EcosDccThrottleManager extends AbstractThrottleManager implements EcosListener{

    /**
     * Constructor.
     */
    public EcosDccThrottleManager() {
        super();
        if (mInstance==null) mInstance = this;

    }

    static private EcosDccThrottleManager mInstance = null;
    static public EcosDccThrottleManager instance() {
        return mInstance;
    }

    public void reply(EcosReply m) {
        //We are not sending commands from here yet!

   }

   public void message(EcosMessage m) {
        // messages are ignored
    }

    public void requestThrottleSetup(LocoAddress address) {
        /*Here we do not set notifythrottle, we simply create a new ecos throttle.
        The ecos throttle in turn will notify the throttle manager of a successful or
        unsuccessful throttle connection. */
        log.debug("new EcosDccThrottle for "+address);
        new EcosDccThrottle((DccLocoAddress)address);
        //notifyThrottleKnown(new EcosDccThrottle((DccLocoAddress)address), address);
    }
    
    public boolean hasDispatchFunction() { return false; }
    
    /**
     * Address 100 and above is a long address
     **/
    public boolean canBeLongAddress(int address) {
        return isLongAddress(address);
    }
    
    /**
     * Address 99 and below is a short address
     **/
    public boolean canBeShortAddress(int address) {
        return !isLongAddress(address);
    }

    /**
     * Are there any ambiguous addresses (short vs long) on this system?
     */
    public boolean addressTypeUnique() { return true; }

    protected boolean singleUse() { return false; }

    /*
     * Local method for deciding short/long address
     */
    static boolean isLongAddress(int num) {
        return (num>=100);
    }
    
    public int supportedSpeedModes() {
    	return(DccThrottle.SpeedStepMode128 | DccThrottle.SpeedStepMode28);
        }
        
    public void throttleSetup(EcosDccThrottle throttle, LocoAddress address, boolean result){
        /* this is called by the ecosdccthrottle, to inform the manager if it has successfully gained
        control of a loco, when setting up the throttle.*/
        if (result){
            log.debug("Ecos Throttle has control over loco "+address);
            notifyThrottleKnown(throttle, address);
        }
        else {
            log.debug("Ecos Throttle has NO control over loco "+address);
            failedThrottleRequest((DccLocoAddress) address);
        }
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EcosDccThrottleManager.class.getName());

}
