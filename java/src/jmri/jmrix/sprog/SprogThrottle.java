package jmri.jmrix.sprog;

import jmri.LocoAddress;
import jmri.DccLocoAddress;

import jmri.jmrix.AbstractThrottle;

/**
 * An implementation of DccThrottle with code specific to an SPROG connection.
 * <P>
 * Addresses of 127 and below are considered short addresses, and
 * 128 and over are considered long addresses.
 * <P>
 * Based on the {@link jmri.jmrix.nce.NceThrottle} implementation.
 *
 * @author	Bob Jacobsen  Copyright (C) 2003
 * @version     $Revision: 1.10 $
 */
public class SprogThrottle extends AbstractThrottle
{
    /**
     * Constructor.
     */
    public SprogThrottle(LocoAddress address)
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

    }

    SprogCommandStation station = new SprogCommandStation();
    private int address;

    /**
     * Send the message to set the state of functions F0, F1, F2, F3, F4.
     */
    protected void sendFunctionGroup1() {
        byte[] result = jmri.NmraPacket.function0Through4Packet(address, (address>=128),
                                         getF0(), getF1(), getF2(), getF3(), getF4());

        station.sendPacket(result, 1);
    }

    /**
     * Send the message to set the state of
     * functions F5, F6, F7, F8.
     */
    protected void sendFunctionGroup2() {

        byte[] result = jmri.NmraPacket.function5Through8Packet(address, (address>=128),
                                         getF5(), getF6(), getF7(), getF8());

        station.sendPacket(result, 1);
    }

    /**
     * Send the message to set the state of
     * functions F9, F10, F11, F12.
     */
    protected void sendFunctionGroup3() {

        byte[] result = jmri.NmraPacket.function9Through12Packet(address, (address>=128),
                                         getF9(), getF10(), getF11(), getF12());

        station.sendPacket(result, 1);
    }
    
    /**
     * Send the message to set the state of
     * functions F13 F14, F15, F16.
     */
    protected void sendFunctionGroup4() {

        byte[] result = jmri.NmraPacket.function13Through20Packet(address, (address>=128),
                                         getF13(), getF14(), getF15(), getF16(),
                                         getF17(), getF18(), getF19(), getF20());

        station.sendPacket(result, 1);
    }
    
        /**
     * Send the message to set the state of
     * functions F17 F18, F19, F20.
     */
    protected void sendFunctionGroup5() {

        byte[] result = jmri.NmraPacket.function21Through28Packet(address, (address>=128),
                                        getF21(), getF22(), getF23(), getF24(),
                                        getF25(), getF26(), getF27(), getF28());

        station.sendPacket(result, 1);
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
//        int value = (int)((127-1)*speed);     // -1 for rescale to avoid estop
        int value = Math.round((127-1)*speed);     // -1 for rescale to avoid estop
        if (value>0) value = value+1;  // skip estop
        if (value>127) value = 127;    // max possible speed
        if (value<0) value = 1;        // emergency stop

        String step = ""+value;

        SprogMessage m = new SprogMessage(1+step.length());
        int i = 0;  // message index counter
        if (isForward) m.setElement(i++, '>');
        else           m.setElement(i++, '<');

        for (int j = 0; j<step.length(); j++) {
            m.setElement(i++, step.charAt(j));
        }

        SprogTrafficController.instance().sendSprogMessage(m, null);
//        if (oldSpeed != this.speedSetting)
        if (Math.abs(oldSpeed - this.speedSetting) > 0.0001)
            notifyPropertyChangeListener("SpeedSetting", oldSpeed, this.speedSetting );
    }

    public void setIsForward(boolean forward) {
        boolean old = isForward; 
        isForward = forward;
        setSpeedSetting(speedSetting);  // send the command
        if (old != isForward)
            notifyPropertyChangeListener("IsForward", old, isForward );
    }

    public LocoAddress getLocoAddress() {
        return new DccLocoAddress(address, SprogThrottleManager.isLongAddress(address));
    }

    protected void throttleDispose(){ }

    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SprogThrottle.class.getName());

}