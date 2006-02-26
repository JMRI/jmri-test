package jmri.jmrix.tmcc;

import jmri.LocoAddress;
import jmri.DccLocoAddress;

import jmri.jmrix.AbstractThrottleManager;

/**
 * Implementation of a ThrottleManager.
 * <P>
 * @author	    Bob Jacobsen  Copyright (C) 2001, 2006
 * @version         $Revision: 1.1 $
 */
public class SerialThrottleManager extends AbstractThrottleManager {

    /**
     * Constructor.
     */
    public SerialThrottleManager() {
        super();
    }

    public void requestThrottleSetup(LocoAddress a) {
        // the protocol doesn't require an interaction with the command
        // station for this, so immediately trigger the callback.
        DccLocoAddress address = (DccLocoAddress) a;
        log.debug("new throttle for "+address);
        notifyThrottleKnown(new SerialThrottle(address), address);
    }

    /**
     * Address 1 and above can be long
     **/
    public boolean canBeLongAddress(int address) {
        return (address>=1);
    }
    
    /**
     * The full range of short addresses are available
     **/
    public boolean canBeShortAddress(int address) {
        return (address<=127);
    }

    /**
     * Are there any ambiguous addresses (short vs long) on this system?
     */
    public boolean addressTypeUnique() { return false; }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialThrottleManager.class.getName());

}