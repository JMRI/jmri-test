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
            addressLo = address.getNumber();
            addressHi = address.getNumber()>>8;
            addressHi = addressHi + 0xc0; //We add 0xc0 to the high byte.
        } else {
            addressLo = address.getNumber();
        }
    }
    
    DccLocoAddress address;
    
    int addressLo = 0x00;
    int addressHi = 0x00;

    public LocoAddress getLocoAddress() { return address; }

    protected void sendFunctionGroup1() {
        
        int data = 0x00 |
        ( f0 ? 0x10 : 0) |
        ( f1 ? 0x01 : 0) |
        ( f2 ? 0x02 : 0) |
        ( f3 ? 0x04 : 0) |
        ( f4 ? 0x08 : 0);
        
        data = data + 0x80;
        MrcMessage m = MrcMessage.getSendFunction(1, addressLo, addressHi, data);
        if(m!=null)
            tc.sendMrcMessage(m, this);
	}

    /**
	 * Send the message to set the state of functions F5, F6, F7, F8.
	 */
	protected void sendFunctionGroup2() {
		// The NCE USB doesn't support the NMRA packet format
		// Always need speed command before function group command to reset consist pointer
        int data = 0x00 |
        (f8 ? 0x08 : 0) |
        (f7 ? 0x04 : 0)	|
        (f6 ? 0x02 : 0) |
        (f5 ? 0x01 : 0);
        
        data = data + 0xB0;
        
        MrcMessage m = MrcMessage.getSendFunction(2, addressLo, addressHi, data);
        if(m!=null)
            tc.sendMrcMessage(m, this);
	}
    
    //Need to work out what MRC have covered in group3
    protected void sendFunctionGroup3() {
        
        int data = 0x00 |
        ( f9 ? 0x01 : 0) |
        ( f10 ? 0x02 : 0) |
        ( f11 ? 0x04 : 0) |
        ( f12 ? 0x08 : 0);
        
        data = data + 0x80;
        MrcMessage m = MrcMessage.getSendFunction(3, addressLo, addressHi, data);
        if(m!=null)
            tc.sendMrcMessage(m, this);
	}
    //Need to work out what MRC have covered in group4
	protected void sendFunctionGroup4() {
		// The NCE USB doesn't support the NMRA packet format
		// Always need speed command before function group command to reset consist pointer
        int data = 0x00 |
        (f16 ? 0x08 : 0) |
        (f15 ? 0x04 : 0)	|
        (f14 ? 0x02 : 0) |
        (f13 ? 0x01 : 0);
        
        data = data + 0xB0;
        
        MrcMessage m = MrcMessage.getSendFunction(4, addressLo, addressHi, data);
        if(m!=null)
            tc.sendMrcMessage(m, this);
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
        //MRC use a value between 0-127 no matter what the controller is set to
        int value = (int)((127-1)*speed);     // -1 for rescale to avoid estop
        if (value>0) value = value+1;  // skip estop
        if (value>127) value = 127;    // max possible speed
        if (value<0) value = 1;        // emergency stop
        if(isForward){
            value = value+128;
        }
        MrcMessage m = MrcMessage.getSendSpeed(addressLo, addressHi, value);
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
    
    //Might need to look at other packets from handsets to see if they also have control of our loco and adjust from that.
    
    public void reply(MrcReply m) {
        if(m.isPollMessage())
            return;
        String raw = "";
        for (int i=0;i<m.getNumDataElements(); i++) {
	        if (i>0) raw+=" ";
            raw = jmri.util.StringUtil.appendTwoHexFromInt(m.getElement(i)&0xFF, raw);
        }
        log.info("Reply received " + raw);
        if(m.isUnsolicited()){
            if(m.getNumDataElements()>8 && m.getElement(5)==addressHi && m.getElement(7)==addressLo){
                //message potentially matches our loco
                if(MrcReply.startsWith(m, MrcMessage.throttlePacketHeader)){
                        log.info("speed Packet from another controller for our loco");
                } else if (MrcReply.startsWith(m, MrcMessage.functionGroup1PacketHeader)){
                        log.info("function Packet 1 from another controller for our loco");
                        //reverse engineer the function
                }  else if (MrcReply.startsWith(m, MrcMessage.functionGroup2PacketHeader)){
                        log.info("function Packet 2 from another controller for our loco");
                        //reverse engineer the function
                }  else if (MrcReply.startsWith(m, MrcMessage.functionGroup3PacketHeader)){
                        log.info("function Packet 3 from another controller for our loco");
                        //reverse engineer the function
                }  else if (MrcReply.startsWith(m, MrcMessage.functionGroup4PacketHeader)){
                        log.info("function Packet 4 from another controller for our loco");
                        //reverse engineer the function
                }  else if (MrcReply.startsWith(m, MrcMessage.functionGroup5PacketHeader)){
                        log.info("function Packet 5 from another controller for our loco");
                        //reverse engineer the function
                }  else if (MrcReply.startsWith(m, MrcMessage.functionGroup6PacketHeader)){
                        log.info("function Packet 6 from another controller for our loco");
                        //reverse engineer the function
                } else {
                    return;
                }
            } else {
                return;
            }
        }

        //need to work out the resend function for if the loco is also controlled by another handset. - Might get done in the MrcTrafficController.
    }
    
    public void message(MrcMessage m) {
        //System.out.println("Ecos message - "+ m);
        // messages are ignored
    }
    // initialize logging
    static Logger log = LoggerFactory.getLogger(MrcThrottle.class.getName());

}
