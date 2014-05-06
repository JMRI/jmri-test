package jmri.jmrix.mrc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.Throttle;
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
public class MrcThrottle extends AbstractThrottle implements MrcListener{

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
    
    public void setF0(boolean f0) {
    	boolean old = this.f0;
        this.f0 = f0;
        if (old != this.f0){
            MrcMessage m = MrcMessage.getSendFunction(addressLo, addressHi, (byte)0x00);
            tc.sendMrcMessage(m, this);
            notifyPropertyChangeListener(Throttle.F0, old, this.f0 );
        }
    }

    public void setF1(boolean f1) {
    	boolean old = this.f1;
        this.f1 = f1;
        if (old != this.f1){
            MrcMessage m = MrcMessage.getSendFunction(addressLo, addressHi, (byte)0x01);
            tc.sendMrcMessage(m, this);
        	notifyPropertyChangeListener(Throttle.F1, old, this.f1 );
        }
    }

    public void setF2(boolean f2) {
    	boolean old = this.f2;
        this.f2 = f2;
        if (old != this.f2) {
            MrcMessage m = MrcMessage.getSendFunction(addressLo, addressHi, (byte)0x02);
            tc.sendMrcMessage(m, this);
        	notifyPropertyChangeListener(Throttle.F2, old, this.f2 );
        }
    }

    public void setF3(boolean f3) {
    	boolean old = this.f3;
        this.f3 = f3;
        if (old != this.f3){
            MrcMessage m = MrcMessage.getSendFunction(addressLo, addressHi, (byte)0x03);
            tc.sendMrcMessage(m, this);
        	notifyPropertyChangeListener(Throttle.F3, old, this.f3 );
        }
    }

    public void setF4(boolean f4) {
    	boolean old = this.f4;
        this.f4 = f4;
        if (old != this.f4){
            MrcMessage m = MrcMessage.getSendFunction(addressLo, addressHi, (byte)0x04);
            tc.sendMrcMessage(m, this);
        	notifyPropertyChangeListener(Throttle.F4, old, this.f4 );
        }
    }

    public void setF5(boolean f5) {
    	boolean old = this.f5;
        this.f5 = f5;
        if (old != this.f5){
            MrcMessage m = MrcMessage.getSendFunction(addressLo, addressHi, (byte)0x05);
            tc.sendMrcMessage(m, this);
        	notifyPropertyChangeListener(Throttle.F5, old, this.f5 );
        }
    }

    public void setF6(boolean f6) {
    	boolean old = this.f6;
        this.f6 = f6;
        if (old != this.f6){
            MrcMessage m = MrcMessage.getSendFunction(addressLo, addressHi, (byte)0x06);
            tc.sendMrcMessage(m, this);
        	notifyPropertyChangeListener(Throttle.F6, old, this.f6 );
        }
    }

    public void setF7(boolean f7) {
    	boolean old = this.f7;
        this.f7 = f7;
        if (old != this.f7){
            MrcMessage m = MrcMessage.getSendFunction(addressLo, addressHi, (byte)0x07);
            tc.sendMrcMessage(m, this);
        	notifyPropertyChangeListener(Throttle.F7, old, this.f7 );
            
        }
    }

    public void setF8(boolean f8) {
    	boolean old = this.f8;
        this.f8 = f8;
        if (old != this.f8) {
            MrcMessage m = MrcMessage.getSendFunction(addressLo, addressHi, (byte)0x08);
            tc.sendMrcMessage(m, this);
        	notifyPropertyChangeListener(Throttle.F8, old, this.f8 );
            
        }
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
        MrcMessage m = MrcMessage.getSendSpeed(addressLo, addressHi, (byte)value);
        //MrcMessage m = MrcMessage.queuePacketMessage(tc, bl);
        tc.sendMrcMessage(m, this);

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
    
    public void reply(MrcReply m) {
        if(m.isUnsolicited())
            return;
        log.info("Reply received " + m.toString());
        //need to work out the resend function for if the loco is also controlled by another handset.
    }
    
    public void message(MrcMessage m) {
        //System.out.println("Ecos message - "+ m);
        // messages are ignored
    }
    // initialize logging
    static Logger log = LoggerFactory.getLogger(MrcThrottle.class.getName());

}
