package jmri.jmrix.sprog;

import jmri.LocoAddress;
import jmri.DccLocoAddress;

import jmri.jmrix.AbstractThrottle;

import jmri.jmrix.sprog.SprogCommandStation;

/**
 * An implementation of DccThrottle with code specific to a SPROG Command
 * Station connection.
 * <P>
 * Addresses of 99 and below are considered short addresses, and
 * over 100 are considered long addresses.
 * <P>
 *
 * @author	Andrew Crosland  Copyright (C) 2006
 * @version     $Revision: 1.9 $
 */
public class SprogCSThrottle extends AbstractThrottle
{
    /**
     * Constructor.
     */
    public SprogCSThrottle(LocoAddress address)
    {
        super();

        // cache settings.
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
        this.address      = ((DccLocoAddress)address).getNumber();
        this.isForward    = true;
        
        //@TODO - this needs a little work. Current implementation looks like it
        //should support other modes, but doesn't in practice.  
        //@see AbstractThrottleManager.supportedSpeedModes()

        // Find our command station
        commandStation = (SprogCommandStation) jmri.InstanceManager.commandStationInstance();

    }

    private SprogCommandStation commandStation;
    private int address;

    /**
     * Send the message to set the state of functions F0, F1, F2, F3, F4 by
     * adding it to the S queue
     */
    protected void sendFunctionGroup1() {
       commandStation.function0Through4Packet(address,
                                              getF0(), getF0Momentary(), 
                                              getF1(), getF1Momentary(),
                                              getF2(), getF2Momentary(),
                                              getF3(), getF3Momentary(),
                                              getF4(), getF4Momentary());

    }
    

    /**
     * Send the message to set the state of functions F5, F6, F7, F8 by#
     * adding it to the S queue
     */
    protected void sendFunctionGroup2() {
        commandStation.function5Through8Packet(address,
                                               getF5(), getF5Momentary(),
                                               getF6(), getF6Momentary(),
                                               getF7(), getF7Momentary(),
                                               getF8(), getF8Momentary());
    }

    /**
     * Send the message to set the state of functions F9, F10, F11, F12 by
     * adding it to the S queue
     */
    protected void sendFunctionGroup3() {
        commandStation.function9Through12Packet(address,
                                                getF9(), getF9Momentary(),
                                                getF10(), getF10Momentary(),
                                                getF11(), getF11Momentary(),
                                                getF12(), getF12Momentary());
    }

    /**
     * Set the speed & direction.
     * <P>
     * This intentionally skips the emergency stop value of 1.
     * @param speed Number from 0 to 1; less than zero is emergency stop
     */
    public void setSpeedSetting(float speed) {
        float oldSpeed = this.speedSetting;
        this.speedSetting = speed;
        int value = (int)((127-1)*speed);     // -1 for rescale to avoid estop
        if (value>0) value = value+1;  // skip estop
        if (value>127) value = 127;    // max possible speed
        if (value<0) value = 1;        // emergency stop
        commandStation.setSpeed(address, value, isForward );
//        if (oldSpeed != this.speedSetting)
        if (Math.abs(oldSpeed - this.speedSetting) > 0.0001)
            notifyPropertyChangeListener("SpeedSetting", oldSpeed, this.speedSetting );
    }

    public void setIsForward(boolean forward) {
        boolean old = isForward; 
        isForward = forward;
        setSpeedSetting(speedSetting);  // Update the speed setting
        if (old != isForward)
            notifyPropertyChangeListener("IsForward", old, isForward );
    }

    /**
     * Finished with this throttle.  Right now, this does nothing
     * except notify the SprogThrottleManager
     * but it could set the speed to zero, turn off functions, etc.
     */
    public void release() {
        if (!active) log.warn("release called when not active");
        commandStation.release(address);
        dispose();
    }

    /**
     * Dispose when finished with this object.  After this, further usage of
     * this Throttle object will result in a JmriException.
     */
    public void dispose() {
        // if this object has registered any listeners, remove those.
        super.dispose();
    }

    public LocoAddress getLocoAddress() {
        return new DccLocoAddress(address, SprogCSThrottleManager.isLongAddress(address));
    }

    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SprogCSThrottle.class.getName());

}
