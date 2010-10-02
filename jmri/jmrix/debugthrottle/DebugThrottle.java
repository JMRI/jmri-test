package jmri.jmrix.debugthrottle;

import jmri.LocoAddress;
import jmri.DccLocoAddress;
import jmri.jmrix.AbstractThrottle;

/**
 * An implementation of DccThrottle for debugging use.
 *
 * @author	Bob Jacobsen  Copyright (C) 2003
 * @version     $Revision: 1.8 $
 */
public class DebugThrottle extends AbstractThrottle
{
    /**
     * Constructor
     */
    public DebugThrottle(DccLocoAddress address)
    {
        super();

        // cache settings. It would be better to read the
        // actual state, but I don't know how to do this
        this.speedSetting = 0;
        this.f0           = false;
        this.f1           = false;
        this.f2           = false;
        this.f3           = false;
        this.f4           = false;
        this.f5           = false;
        this.f6           = false;
        this.f7           = false;
        this.f8           = false;
        this.f9           = false;
        this.f10           = false;
        this.f11           = false;
        this.f12           = false;
        this.address      = address;
        this.isForward    = true;
        this.address      = address;
    }

    DccLocoAddress address;

    public LocoAddress getLocoAddress() { return address; }

    public String toString() {
        return getLocoAddress().toString();
    }

    /**
     * Send the message to set the state of functions F0, F1, F2, F3, F4
     */
    protected void sendFunctionGroup1() {
    }

    /**
     * Send the message to set the state of
     * functions F5, F6, F7, F8
     */
    protected void sendFunctionGroup2() {

    }

    /**
     * Send the message to set the state of
     * functions F9, F10, F11, F12
     */
    protected void sendFunctionGroup3() {
    }

    /**
     * Set the speed & direction
     * <P>
     * This intentionally skips the emergency stop value of 1.
     * @param speed Number from 0 to 1; less than zero is emergency stop
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="FE_FLOATING_POINT_EQUALITY") // OK to compare floating point, notify on any change
    public void setSpeedSetting(float speed) {
        float oldSpeed = this.speedSetting;
        if (speed>1.0) {
            log.warn("Speed was set too high: "+speed);
        }
        this.speedSetting = speed;
        if (oldSpeed != this.speedSetting)
            notifyPropertyChangeListener("SpeedSetting", oldSpeed, this.speedSetting );
    }

    public void setIsForward(boolean forward) {
        boolean old = isForward; 
        isForward = forward;
        setSpeedSetting(speedSetting);  // send the command
        if (old != isForward)
            notifyPropertyChangeListener("IsForward", old, isForward );
    }

    /**
     * Finished with this throttle.  Right now, this does nothing,
     * but it could set the speed to zero, turn off functions, etc.
     */
    public void release() {
        if (!active) log.warn("release called when not active");
        dispose();
    }

    /**
     * Dispose when finished with this object.  After this, further usage of
     * this Throttle object will result in a JmriException.
     */
    public void dispose() {
        log.debug("dispose");
 
        // if this object has registered any listeners, remove those.
        super.dispose();
    }

    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DebugThrottle.class.getName());

}