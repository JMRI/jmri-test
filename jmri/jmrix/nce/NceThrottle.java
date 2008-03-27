package jmri.jmrix.nce;

import jmri.LocoAddress;
import jmri.DccLocoAddress;
import jmri.jmrix.AbstractThrottle;

/**
 * An implementation of DccThrottle with code specific to an NCE connection.
 * <P>
 * Addresses of 99 and below are considered short addresses, and
 * over 100 are considered long addresses.  This is not the NCE system
 * standard, but is used as an expedient here.
 * <P>
 * Based on Glen Oberhauser's original LnThrottleManager implementation
 *
 * @author	Bob Jacobsen  Copyright (C) 2001
 * @version     $Revision: 1.18 $
 */
public class NceThrottle extends AbstractThrottle
{
    /**
     * Constructor.
     */
    public NceThrottle(DccLocoAddress address)
    {
        super();
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

    }

    DccLocoAddress address;

    public LocoAddress getLocoAddress() { return address; }

    /**
     * Send the message to set the state of functions F0, F1, F2, F3, F4.
     */
    protected void sendFunctionGroup1() {
		// The NCE USB doesn't support the NMRA packet format
		if (NceUSB.getUsbSystem() != NceUSB.USB_SYSTEM_NONE) {
			int locoAddr = address.getNumber();
			if (address.isLongAddress())
				locoAddr += 0xC000;
			
			int data = 0x00 |
			( f0 ? 0x10 : 0) |
			( f1 ? 0x01 : 0) |
			( f2 ? 0x02 : 0) |
			( f3 ? 0x04 : 0) |
			( f4 ? 0x08 : 0);
			
			byte[] bl = NceBinaryCommand.nceLocoCmd(locoAddr,
					NceBinaryCommand.LOCO_CMD_FG1, (byte) data);
			NceMessage m = NceMessage.createBinaryMessage(bl);
			NceTrafficController.instance().sendNceMessage(m, null);

			// This code can be eliminated once we confirm that the NCE 0xA2
			// commands work properly
		} else {
			byte[] result = jmri.NmraPacket.function0Through4Packet(address
					.getNumber(), address.isLongAddress(), getF0(), getF1(),
					getF2(), getF3(), getF4());
			NceMessage m = NceMessage.sendPacketMessage(result);
			NceTrafficController.instance().sendNceMessage(m, null);
		}
	}

    /**
	 * Send the message to set the state of functions F5, F6, F7, F8.
	 */
	protected void sendFunctionGroup2() {
		// The NCE USB doesn't support the NMRA packet format
		if (NceUSB.getUsbSystem() != NceUSB.USB_SYSTEM_NONE) {
			int locoAddr = address.getNumber();
			if (address.isLongAddress())
				locoAddr += 0xC000;
			
			int data = 0x00 |
			(f8 ? 0x08 : 0) |
			(f7 ? 0x04 : 0)	|
			(f6 ? 0x02 : 0) |
			(f5 ? 0x01 : 0);
			
			byte[] bl = NceBinaryCommand.nceLocoCmd(locoAddr,
					NceBinaryCommand.LOCO_CMD_FG2, (byte) data);
			NceMessage m = NceMessage.createBinaryMessage(bl);
			NceTrafficController.instance().sendNceMessage(m, null);

			// This code can be eliminated once we confirm that the NCE 0xA2
			// commands work properly
		} else {
			byte[] result = jmri.NmraPacket.function5Through8Packet(address
					.getNumber(), address.isLongAddress(), getF5(), getF6(),
					getF7(), getF8());
			NceMessage m = NceMessage.sendPacketMessage(result);
			NceTrafficController.instance().sendNceMessage(m, null);
		}
	}

    /**
	 * Send the message to set the state of functions F9, F10, F11, F12.
	 */
    protected void sendFunctionGroup3() {
		// The NCE USB doesn't support the NMRA packet format
		if (NceUSB.getUsbSystem() != NceUSB.USB_SYSTEM_NONE) {
			int locoAddr = address.getNumber();
			if (address.isLongAddress())
				locoAddr += 0xC000;
			
			int data = 0x00 |
            ( f12 ? 0x08 : 0) |
            ( f11 ? 0x04 : 0) |
            ( f10 ? 0x02 : 0) |
            ( f9  ? 0x01 : 0);
			
			byte[] bl = NceBinaryCommand.nceLocoCmd(locoAddr,
					NceBinaryCommand.LOCO_CMD_FG3, (byte) data);
			NceMessage m = NceMessage.createBinaryMessage(bl);
			NceTrafficController.instance().sendNceMessage(m, null);

			// This code can be eliminated once we confirm that the NCE 0xA2
			// commands work properly
		} else {
			byte[] result = jmri.NmraPacket.function9Through12Packet(address
					.getNumber(), address.isLongAddress(), getF9(), getF10(),
					getF11(), getF12());
			NceMessage m = NceMessage.sendPacketMessage(result);
			NceTrafficController.instance().sendNceMessage(m, null);
		}
	}

    /**
	 * Send the message to set the state of functions F13, F14, F15, F16, F17, F18, F19, F20
	 */
    protected void sendFunctionGroup4() {
		// The NCE USB doesn't support the NMRA packet format
		if (NceUSB.getUsbSystem() != NceUSB.USB_SYSTEM_NONE) {
			int locoAddr = address.getNumber();
			if (address.isLongAddress())
				locoAddr += 0xC000;
			
			int data = 0x00 |
	        ( f20 ? 0x80 : 0) |
	        ( f19 ? 0x40 : 0) |
	        ( f18 ? 0x20 : 0) |
	        ( f17 ? 0x10 : 0) |
            ( f16 ? 0x08 : 0) |
            ( f15 ? 0x04 : 0) |
            ( f14 ? 0x02 : 0) |
            ( f13 ? 0x01 : 0);
			
			byte[] bl = NceBinaryCommand.nceLocoCmd(locoAddr,
					NceBinaryCommand.LOCO_CMD_FG4, (byte) data);
			NceMessage m = NceMessage.createBinaryMessage(bl);
			NceTrafficController.instance().sendNceMessage(m, null);
			
		} else {
			// Note NCE EPROM 2004 doesn't support LOCO_CMD_FG4
			byte[] result = jmri.NmraPacket.function13Through20Packet(address
					.getNumber(), address.isLongAddress(), getF13(), getF14(),
					getF15(), getF16(), getF17(), getF18(), getF19(), getF20());
			NceMessage m = NceMessage.sendPacketMessage(result);
			NceTrafficController.instance().sendNceMessage(m, null);
		}
	}

    /**
	 * Send the message to set the state of functions F21, F22, F23, F24, F25, F26, F27, F28
	 */
    protected void sendFunctionGroup5() {
		// The NCE USB doesn't support the NMRA packet format
		if (NceUSB.getUsbSystem() != NceUSB.USB_SYSTEM_NONE) {
			int locoAddr = address.getNumber();
			if (address.isLongAddress())
				locoAddr += 0xC000;
			
			int data = 0x00 |
	        ( f28 ? 0x80 : 0) |
	        ( f27 ? 0x40 : 0) |
	        ( f26 ? 0x20 : 0) |
	        ( f25 ? 0x10 : 0) |
            ( f24 ? 0x08 : 0) |
            ( f23 ? 0x04 : 0) |
            ( f22 ? 0x02 : 0) |
            ( f21 ? 0x01 : 0);
			
			byte[] bl = NceBinaryCommand.nceLocoCmd(locoAddr,
					NceBinaryCommand.LOCO_CMD_FG5, (byte) data);
			NceMessage m = NceMessage.createBinaryMessage(bl);
			NceTrafficController.instance().sendNceMessage(m, null);

		} else {
			// Note NCE EPROM 2004 doesn't support LOCO_CMD_FG5
			byte[] result = jmri.NmraPacket.function21Through28Packet(address
					.getNumber(), address.isLongAddress(), getF21(), getF22(),
					getF23(), getF24(), getF25(), getF25(), getF27(), getF28());
			NceMessage m = NceMessage.sendPacketMessage(result);
			NceTrafficController.instance().sendNceMessage(m, null);
		}
	}

    /**
	 * Set the speed & direction.
	 * <P>
	 * 
	 * @param speed
	 *            Number from 0 to 1; less than zero is emergency stop
	 */
    public void setSpeedSetting(float speed) {
		this.speedSetting = speed;
		
		// The NCE USB doesn't support the NMRA packet format
		if (NceUSB.getUsbSystem() != NceUSB.USB_SYSTEM_NONE) {
			
	        byte[] bl;
	        int value;
			int locoAddr = address.getNumber();
			if (address.isLongAddress())
				locoAddr += 0xC000;
            value = (int)((126-1)*speed);     // -1 for rescale to avoid estop
            if (value>0) value = value+1;  // skip estop
            if (value>126) value = 126;    // max possible speed, 127 can crash PowerCab! 
			
			// emergency stop?
			if (value < 0){
			
			bl = NceBinaryCommand.nceLocoCmd(locoAddr,
					(isForward ? NceBinaryCommand.LOCO_CMD_FWD_ESTOP
							: NceBinaryCommand.LOCO_CMD_REV_ESTOP),
					(byte) 0);
			
			} else if (super.speedStepMode == SpeedStepMode128) {
           		bl = NceBinaryCommand.nceLocoCmd(locoAddr,
						(isForward ? NceBinaryCommand.LOCO_CMD_FWD_128SPEED
								: NceBinaryCommand.LOCO_CMD_REV_128SPEED),
						(byte) value);
			} else {
				// 28 speed step mode
           		bl = NceBinaryCommand.nceLocoCmd(locoAddr,
						(isForward ? NceBinaryCommand.LOCO_CMD_FWD_28SPEED
								: NceBinaryCommand.LOCO_CMD_REV_28SPEED),
						(byte) value);
			}
			NceMessage m = NceMessage.createBinaryMessage(bl);
			NceTrafficController.instance().sendNceMessage(m, null);

		// This code can be eliminated once we confirm that the NCE 0xA2 commands work properly
		} else {
	        byte[] bl;
	        int value;
	        
	        if (super.speedStepMode == SpeedStepMode128) {
	            value = (int)((127-1)*speed);     // -1 for rescale to avoid estop
	            if (value>0) value = value+1;  // skip estop
	            if (value>127) value = 127;    // max possible speed
	            if (value<0) value = 1;        // emergency stop
				bl = jmri.NmraPacket.speedStep128Packet(address.getNumber(),
						address.isLongAddress(), value, isForward);
			} else {
		        value = (int) ((28) * speed); // -1 for rescale to avoid estop
				if (value > 0) value = value + 1; // skip estop
				if (value > 28) value = 28; // max possible speed
				if (value < 0)	value = 1; // emergency stop
				bl = jmri.NmraPacket.speedStep28Packet(address.getNumber(),
						address.isLongAddress(), value, isForward);
			}
			NceMessage m = NceMessage.queuePacketMessage(bl);
			NceTrafficController.instance().sendNceMessage(m, null);
		}
    }

    public void setIsForward(boolean forward) {
        isForward = forward;
        setSpeedSetting(speedSetting);  // send the command
    }

    /**
     * Finished with this throttle.  Right now, this does nothing,
     * but it could set the speed to zero, turn off functions, etc.
     */
    public void release() {
		// release is called twice when throttle frame closed and throttle
		// exists, therefore it is not a reportable error
		if (!active) {
			if (log.isDebugEnabled())
				log.warn("release called when not active");
		} else
			dispose();
	}

    /**
	 * Dispose when finished with this object. After this, further usage of this
	 * Throttle object will result in a JmriException.
	 */
    public void dispose() {
        log.debug("dispose");

        // if this object has registered any listeners, remove those.
        super.dispose();
    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NceThrottle.class.getName());

}