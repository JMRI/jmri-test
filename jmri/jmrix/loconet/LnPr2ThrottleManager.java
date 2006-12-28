package jmri.jmrix.loconet;

import jmri.DccThrottle;
import jmri.ThrottleManager;
import jmri.LocoAddress;
import jmri.DccLocoAddress;

import com.sun.java.util.collections.HashMap;

import jmri.jmrix.AbstractThrottleManager;

/**
 * LocoNet implementation of a ThrottleManager for the PR2
 * <P>
 * Does direct "push" writes to the extended slot in the PR2.
 * <P>
 * The PR2 only allows a single locomotive address to be active,
 * because it implements a single-slot command station.
 *
 * @see AbstractThrottleManager
 * @author		Bob Jacobsen  Copyright (C) 2001, 2006
 * @version 		$Revision: 1.1 $
 */
public class LnPr2ThrottleManager extends AbstractThrottleManager {

    /**
     * Constructor, works via superclass.
     */
    public LnPr2ThrottleManager() {
    	super();
    }

	/**
	 * PR2 allows only one throttle
     */
	protected boolean singleUse() { return true; }


	/** 
	 * Get a new Throttle object.
	 *
	 * This immediately invokes the callback with the
	 * a new throttle object.
	 */
	public void requestThrottleSetup(LocoAddress address) {
        // The PR2 has only one slot, hence
        // doesn't require an interaction with the command
        // station to allocate slot, so immediately trigger the callback.
        DccLocoAddress a = (DccLocoAddress) address;
        log.debug("new Pr2Throttle for "+a);
        notifyThrottleKnown(new Pr2Throttle(a), a);
	}
	    
    /**
     * PR2 does not have a Dispatch function
     **/
    public boolean hasDispatchFunction(){ return false; }     

    /**
     * Address 128 and above is a long address
     **/
    public boolean canBeLongAddress(int address) {
        return isLongAddress(address);
    }
    
    /**
     * Address 127 and below is a short address
     **/
    public boolean canBeShortAddress(int address) {
        return !isLongAddress(address);
    }

    /**
     * Are there any ambiguous addresses (short vs long) on this system?
     */
    public boolean addressTypeUnique() { return true; }

    /*
     * Local method for deciding short/long address
     */
    static boolean isLongAddress(int num) {
        return (num>=128);
    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LnPr2ThrottleManager.class.getName());
}
