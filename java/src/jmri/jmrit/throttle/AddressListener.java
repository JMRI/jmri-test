package jmri.jmrit.throttle;

import java.util.EventListener;

import jmri.DccThrottle;

/**
 * Interface for classes that wish to get notification that a new
 * decoder address has been selected.
 *
 * @author     glen   Copyright (C) 2002
 * @version    $Revision: 1.7 $
 */
public interface AddressListener extends EventListener
{
    /**
     * Receive notification that a new address has been selected.
     * @param newAddress The address that is now selected.
     */
    public void notifyAddressChosen(int newAddress, boolean isLong);
	
	/**
	 * Receive notification that an address has been released/dispatched
	 * @param address The address released/dispatched
	 */
	public void notifyAddressReleased(int address, boolean isLong);
	
	/**
	 * Receive notification that a throttle has been found
	 * @param throttle The throttle
	 */	
	public void notifyAddressThrottleFound(DccThrottle throttle);
}