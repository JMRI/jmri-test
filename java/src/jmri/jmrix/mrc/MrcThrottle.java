package jmri.jmrix.mrc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.LocoAddress;
import jmri.DccLocoAddress;
import jmri.jmrix.AbstractThrottle;

/**
 * An implementation of DccThrottle with code specific to an MRC connection.
 * <P>
 * Addresses of 99 and below are considered short addresses, and
 * over 100 are considered long addresses.  This is not the MRC system
 * standard, but is used as an expedient here.
 * <P>
 * Based on Glen Oberhauser's original LnThrottleManager implementation
 *
 * @author	Bob Jacobsen  Copyright (C) 2001
 * @version     $Revision: 25048 $
 */
public class MrcThrottle extends AbstractThrottle{

    private MrcTrafficController tc = null;
    
    /**
     * Constructor.
     */
    public MrcThrottle(MrcSystemConnectionMemo memo, DccLocoAddress address)
    {
        super(memo);
        this.tc = memo.getMrcTrafficController();
        super.speedStepMode = SpeedStepMode128;

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
        this.f13           = false;
        this.f14           = false;
        this.f15           = false;
        this.f16           = false;
        this.f17           = false;
        this.f18           = false;
        this.f19           = false;
        this.f20           = false;
        this.f21           = false;
        this.f22           = false;
        this.f23           = false;
        this.f24           = false;
        this.f25           = false;
        this.f26           = false;
        this.f27           = false;
        this.f28           = false;
        this.address      = address;
        this.isForward    = true;
        if(address.isLongAddress()){
            addressLo = (byte)(address.getNumber());
            addressHi = (byte)(address.getNumber()>>8);
            addressHi = (byte)(addressHi + 0xc0); //We add 0xc0 to the high byte.
        } else {
            addressLo = (byte) address.getNumber();
        }
    }

    DccLocoAddress address;
    
    byte addressLo = 0x00;
    byte addressHi = 0x00;

    public LocoAddress getLocoAddress() { return address; }

    /**
     * Send the message to set the state of functions F0, F1, F2, F3, F4.
     */
    protected void sendFunctionGroup1() {
		setSpeedSetting(this.speedSetting);
			byte[] result = jmri.NmraPacket.function0Through4Packet(address
					.getNumber(), address.isLongAddress(), getF0(), getF1(),
					getF2(), getF3(), getF4());
			//MrcMessage m = MrcMessage.sendPacketMessage(tc, result);
			//tc.sendMrcMessage(m, null);
	}

    /**
	 * Send the message to set the state of functions F5, F6, F7, F8.
	 */
	protected void sendFunctionGroup2() {
		setSpeedSetting(this.speedSetting);
        byte[] result = jmri.NmraPacket.function5Through8Packet(address
                .getNumber(), address.isLongAddress(), getF5(), getF6(),
                getF7(), getF8());
        //MrcMessage m = MrcMessage.sendPacketMessage(tc, result);
        //tc.sendMrcMessage(m, null);
	}

    /**
	 * Send the message to set the state of functions F9, F10, F11, F12.
	 */
    protected void sendFunctionGroup3() {
		setSpeedSetting(this.speedSetting);
		byte[] result = jmri.NmraPacket.function9Through12Packet(address
					.getNumber(), address.isLongAddress(), getF9(), getF10(),
					getF11(), getF12());
		//	MrcMessage m = MrcMessage.sendPacketMessage(tc, result);
		//	tc.sendMrcMessage(m, null);
	}

    /**
	 * Send the message to set the state of functions F13, F14, F15, F16, F17, F18, F19, F20
	 */
    protected void sendFunctionGroup4() {
		setSpeedSetting(this.speedSetting);
			// Note MRC EPROM 2004 doesn't support LOCO_CMD_FG4
			byte[] result = jmri.NmraPacket.function13Through20Packet(address
					.getNumber(), address.isLongAddress(), getF13(), getF14(),
					getF15(), getF16(), getF17(), getF18(), getF19(), getF20());
		//	MrcMessage m = MrcMessage.sendPacketMessage(tc, result);
		//	tc.sendMrcMessage(m, null);
	}

    /**
	 * Send the message to set the state of functions F21, F22, F23, F24, F25, F26, F27, F28
	 */
    protected void sendFunctionGroup5() {
		setSpeedSetting(this.speedSetting);
			byte[] result = jmri.NmraPacket.function21Through28Packet(address
					.getNumber(), address.isLongAddress(), getF21(), getF22(),
					getF23(), getF24(), getF25(), getF25(), getF27(), getF28());
		//	MrcMessage m = MrcMessage.sendPacketMessage(tc, result);
		//	tc.sendMrcMessage(m, null);
	}

    /**
	 * Set the speed & direction.
	 * <P>
	 * 
	 * @param speed
	 *            Number from 0 to 1; less than zero is emergency stop
	 */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="FE_FLOATING_POINT_EQUALITY") // OK to compare floating point, notify on any change
    public void setSpeedSetting(float speed) {
        float oldSpeed = this.speedSetting;
		this.speedSetting = speed;
        if (log.isDebugEnabled()) log.debug("setSpeedSetting= "+speed);
        //byte[] bl;
        int value;
        
        //if (super.speedStepMode == SpeedStepMode128) {
            value = (int)((127-1)*speed);     // -1 for rescale to avoid estop
            if (value>0) value = value+1;  // skip estop
            if (value>127) value = 127;    // max possible speed
            if (value<0) value = 1;        // emergency stop
        //} else {

                /* [A Crosland 05Feb12] There is a potential issue in the way
                 * the float speed value is converted to integer speed step.
                 * A max speed value of 1 is first converted to int 28 then incremented
                 * to 29 which is too large. The next highest speed value also
                 * results in a value of 28. So two discrete throttle steps
                 * both map to speed step 28.
                 *
                 * This is compounded by the bug in speedStep28Packet() which
                 * cannot generate a DCC packet with speed step 28.
                 *
                 * Suggested correct code is
         *   value = (int) ((31-3) * speed); // -3 for rescale to avoid stop and estop x2
                 * 		if (value > 0) value = value + 3; // skip stop and estop x2
                 * 		if (value > 31) value = 31; // max possible speed
                 * 		if (value < 0)	value = 2; // emergency stop
                 * 		bl = jmri.NmraPacket.speedStep28Packet(true, address.getNumber(),
                 * 				address.isLongAddress(), value, isForward);
                 */
        /*    value = (int) ((28) * speed); // -1 for rescale to avoid estop
            if (value > 0) value = value + 1; // skip estop
            if (value > 28) value = 28; // max possible speed
            if (value < 0)	value = 1; // emergency stop
        }*/
        if(isForward){
            value = value+128;
        }
        MrcMessage m = MrcMessage.getSpeed(addressLo, addressHi, (byte)value);
        //MrcMessage m = MrcMessage.queuePacketMessage(tc, bl);
        tc.sendMrcMessage(m, null);

        if (oldSpeed != this.speedSetting)
            notifyPropertyChangeListener("SpeedSetting", oldSpeed, this.speedSetting );
        record(speed);
    }

    public void setIsForward(boolean forward) {
        boolean old = isForward;
        isForward = forward;
        setSpeedSetting(speedSetting);  // send the command
        if (log.isDebugEnabled()) log.debug("setIsForward= "+forward);
        if (old != isForward)
            notifyPropertyChangeListener("IsForward", old, isForward );
    }

    protected void throttleDispose(){ finishRecord(); }

    // initialize logging
    static Logger log = LoggerFactory.getLogger(MrcThrottle.class.getName());

}
