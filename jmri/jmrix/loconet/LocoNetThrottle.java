package jmri.jmrix.loconet;

import jmri.LocoAddress;
import jmri.DccLocoAddress;
import jmri.InstanceManager;

import jmri.jmrix.AbstractThrottle;

/**
 * An implementation of DccThrottle via AbstractThrottle with code specific
 * to a LocoNet connection.
 * <P>
 * Speed in the Throttle interfaces and AbstractThrottle is a float, but in LocoNet is an int
 * with values from 0 to 127.
 * <P>
 * @author  Glen Oberhauser, Bob Jacobsen  Copyright (C) 2003, 2004
 * @version $Revision: 1.20 $
 */
public class LocoNetThrottle extends AbstractThrottle implements SlotListener {
    private LocoNetSlot slot;
    private LocoNetInterface network;
    private int address;

    /**
     * Constructor
     * @param slot The LocoNetSlot this throttle will talk on.
     */
    public LocoNetThrottle(LocoNetSlot slot) {
        super();
        this.slot = slot;
        network = LnTrafficController.instance();
        LocoNetMessage msg = new LocoNetMessage(4);
        msg.setOpCode(LnConstants.OPC_MOVE_SLOTS);
        msg.setElement(1, slot.getSlot());
        msg.setElement(2, slot.getSlot());
        network.sendLocoNetMessage(msg);

        // cache settings
        this.speedSetting = floatSpeed(slot.speed());
        this.f0           = slot.isF0();
        this.f1           = slot.isF1();
        this.f2           = slot.isF2();
        this.f3           = slot.isF3();
        this.f4           = slot.isF4();
        this.f5           = slot.isF5();
        this.f6           = slot.isF6();
        this.f7           = slot.isF7();
        this.f8           = slot.isF8();

        // f9 through 12 are not in the slot; we have to maintain them
        // locally

        this.f9  = false;
        this.f10 = false;
        this.f11 = false;
        this.f12 = false;
        this.address      = slot.locoAddr();
        this.isForward    = slot.isForward();

        switch(slot.decoderType())
        {
            case LnConstants.DEC_MODE_128:
            case LnConstants.DEC_MODE_128A: this.speedIncrement = 1; break;
            case LnConstants.DEC_MODE_28:
            case LnConstants.DEC_MODE_28A:
            case LnConstants.DEC_MODE_28TRI: this.speedIncrement = 4; break;
            case LnConstants.DEC_MODE_14: this.speedIncrement = 8; break;
        }

        // listen for changes
        slot.addSlotListener(this);

        // start periodically sending the speed, to keep this
        // attached
        startRefresh();

    }


    /**
     * Convert a LocoNet speed integer to a float speed value
     */
    protected float floatSpeed(int lSpeed) {
        if (lSpeed == 0) return 0.f;
        else if (lSpeed == 1) return -1.f;   // estop
        else return ( (lSpeed-1)/126.f);
    }

    /**
     * Convert a float speed value to a LocoNet speed integer
     */
    protected int intSpeed(float fSpeed) {
      if (fSpeed == 0.f)
        return 0;
      else if (fSpeed < 0.f)
        return 1;   // estop
        // add the 0.5 to handle float to int round for positive numbers
      return (int)(fSpeed * 126.f + 0.5) + 1 ;
    }

    /**
     * Send the LocoNet message to set the state of locomotive
     * direction and functions F0, F1, F2, F3, F4
     */
    protected void sendFunctionGroup1()
    {
        LocoNetMessage msg = new LocoNetMessage(4);
        msg.setOpCode(LnConstants.OPC_LOCO_DIRF);
        msg.setElement(1, slot.getSlot());
        int bytes = (getIsForward() ? 0 : LnConstants.DIRF_DIR) |
                    (getF0() ? LnConstants.DIRF_F0 : 0) |
                    (getF1() ? LnConstants.DIRF_F1 : 0) |
                    (getF2() ? LnConstants.DIRF_F2 : 0) |
                    (getF3() ? LnConstants.DIRF_F3 : 0) |
                    (getF4() ? LnConstants.DIRF_F4 : 0);
        msg.setElement(2, bytes);
        network.sendLocoNetMessage(msg);
    }

    /**
     * Send the LocoNet message to set the state of
     * functions F5, F6, F7, F8
     */
    protected void sendFunctionGroup2()
    {
        LocoNetMessage msg = new LocoNetMessage(4);
        msg.setOpCode(LnConstants.OPC_LOCO_SND);
        msg.setElement(1, slot.getSlot());
        int bytes =  (getF8() ? LnConstants.SND_F8 : 0) |
                    (getF7() ? LnConstants.SND_F7 : 0) |
                    (getF6() ? LnConstants.SND_F6 : 0) |
                    (getF5() ? LnConstants.SND_F5 : 0);
        msg.setElement(2, bytes);
        network.sendLocoNetMessage(msg);

    }

    protected void sendFunctionGroup3() {
        byte[] result = jmri.NmraPacket.function9Through12Packet(address, (address>=100),
                                         getF9(), getF10(), getF11(), getF12());

        InstanceManager.commandStationInstance().sendPacket(result, 4); // repeat = 4
    }

    /**
     * Set the speed.
     * <P>
     * This intentionally skips the emergency stop value of 1.
     * @param speed Number from 0 to 1; less than zero is emergency stop
     */
    public void setSpeedSetting(float speed)
    {
        this.speedSetting = speed;
        if (speed<0) this.speedSetting = -1.f;

        LocoNetMessage msg = new LocoNetMessage(4);
        msg.setOpCode(LnConstants.OPC_LOCO_SPD);
        msg.setElement(1, slot.getSlot());
        int value = intSpeed( speed );
        log.debug( "setSpeedSetting: float speed: " + speed + " LocoNet speed: " + value );
        msg.setElement(2, value);
        network.sendLocoNetMessage(msg);
        
        // reset timeout
        mRefreshTimer.stop();
        mRefreshTimer.setRepeats(true);     // refresh until stopped by dispose
        mRefreshTimer.start();
        
    }

    /**
     * LocoNet actually puts forward and backward in the same message
     * as the first function group.
     */
    public void setIsForward(boolean forward)
    {
        isForward = forward;
        sendFunctionGroup1();
    }

    /**
     * Release the loco from this throttle, then clean up the
     * object.
     */
    public void release() {
        if (!active) log.warn("release called when not active");

        // set status to common
        LnTrafficController.instance().sendLocoNetMessage(
                slot.writeStatus(LnConstants.LOCO_COMMON));

        dispose();
    }

    /**
     * Dispatch the loco from this throttle, then clean up the
     * object.
     */
    public void dispatch() {
        if (!active) log.warn("dispatch called when not active");

        // set status to common
        LnTrafficController.instance().sendLocoNetMessage(
                slot.writeStatus(LnConstants.LOCO_COMMON));

        // and dispatch to slot 0
        LnTrafficController.instance().sendLocoNetMessage(slot.dispatchSlot());

        dispose();
    }

    public String toString() {
        return getLocoAddress().toString();
    }
    
    /**
     * Dispose when finished with this object.  After this, further usage of
     * this Throttle object will result in a JmriException.
     */
    public void dispose() {
        log.debug("dispose");

        // stop timeout
        mRefreshTimer.stop();

        // release connections
        slot.removeSlotListener(this);

        mRefreshTimer = null;
        slot = null;
        network = null;

        super.dispose();
     }

    javax.swing.Timer mRefreshTimer = null;

    protected void startRefresh() {
        mRefreshTimer = new javax.swing.Timer(50000, new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                timeout();
            }
        });
        mRefreshTimer.setRepeats(true);     // refresh until stopped by dispose
        mRefreshTimer.start();
    }

    /**
     * Internal routine to resend the speed on a timeout
     */
    synchronized protected void timeout() {
        setSpeedSetting(speedSetting);
    }

    /**
     * Get notified when underlying slot information changes
     */
    public void notifyChangedSlot(LocoNetSlot pSlot) {
        if (slot!=pSlot) log.error("notified of change in different slot");

        // handle change in each state
        if (this.speedSetting != floatSpeed(slot.speed())) {
          Float newSpeed = new Float( floatSpeed(slot.speed() ) ) ;
          log.debug( "notifyChangedSlot: old speed: " + this.speedSetting + " new Speed: " + newSpeed );
          notifyPropertyChangeListener("SpeedSetting",
                    new Float(this.speedSetting), newSpeed );
          this.speedSetting = newSpeed.floatValue() ;
        }

        if (this.isForward != slot.isForward()) {
            notifyPropertyChangeListener("IsForward",
                    new Boolean(this.isForward), new Boolean(this.isForward = slot.isForward()));
        }

        if (this.f0 != slot.isF0()) {
            notifyPropertyChangeListener("F0",
                    new Boolean(this.f0), new Boolean(this.f0 = slot.isF0()));
        }
        if (this.f1 != slot.isF1()) {
            notifyPropertyChangeListener("F1",
                    new Boolean(this.f1), new Boolean(this.f1 = slot.isF1()));
        }
        if (this.f2 != slot.isF2()) {
            notifyPropertyChangeListener("F2",
                    new Boolean(this.f2), new Boolean(this.f2 = slot.isF2()));
        }
        if (this.f3 != slot.isF3()) {
            notifyPropertyChangeListener("F3",
                    new Boolean(this.f3), new Boolean(this.f3 = slot.isF3()));
        }
        if (this.f4 != slot.isF4()) {
            notifyPropertyChangeListener("F4",
                    new Boolean(this.f4), new Boolean(this.f4 = slot.isF4()));
        }
        if (this.f5 != slot.isF5()) {
            notifyPropertyChangeListener("F5",
                    new Boolean(this.f5), new Boolean(this.f5 = slot.isF5()));
        }
        if (this.f6 != slot.isF6()) {
            notifyPropertyChangeListener("F6",
                    new Boolean(this.f6), new Boolean(this.f6 = slot.isF6()));
        }
        if (this.f7 != slot.isF7()) {
            notifyPropertyChangeListener("F7",
                    new Boolean(this.f7), new Boolean(this.f7 = slot.isF7()));
        }
        if (this.f8 != slot.isF8()) {
            notifyPropertyChangeListener("F8",
                    new Boolean(this.f8), new Boolean(this.f8 = slot.isF8()));
        }

        // f9 through f12 are not in the slot

    }

    public LocoAddress getLocoAddress() {
        return new DccLocoAddress(address, LnThrottleManager.isLongAddress(address));
    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LocoNetThrottle.class.getName());

}