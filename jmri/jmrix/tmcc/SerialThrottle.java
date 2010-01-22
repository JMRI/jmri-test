package jmri.jmrix.tmcc;

import jmri.LocoAddress;
import jmri.DccLocoAddress;
import jmri.jmrix.AbstractThrottle;

/**
 * An implementation of DccThrottle.
 * <P>
 * Addresses of 99 and below are considered short addresses, and
 * over 100 are considered long addresses. 
 *
 * @author	Bob Jacobsen  Copyright (C) 2001, 2006
 * @version     $Revision: 1.3 $
 */
public class SerialThrottle extends AbstractThrottle
{
    /**
     * Constructor.
     */
    public SerialThrottle(DccLocoAddress address)
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

    }

    DccLocoAddress address;

    public LocoAddress getLocoAddress() { return address; }

    public void setF0(boolean f0) {
        this.f0 = f0;
        // AUX2 does headlight
        sendToLayout(0x000D + address.getNumber()*128);
    }

    public void setF1(boolean f1) {
        this.f1 = f1;
        // bell
        sendToLayout(0x001D + address.getNumber()*128);
    }

    public void setF2(boolean f2) {
        this.f2 = f2;
        // horn/whistle 1
        sendToLayout(0x001C + address.getNumber()*128);
    }

    public void setF3(boolean f3) {
        this.f3 = f3;
        // front coupler
        sendToLayout(0x0005 + address.getNumber()*128);
    }

    public void setF4(boolean f4) {
        this.f4 = f4;
        // back coupler
        sendToLayout(0x0006 + address.getNumber()*128);
    }

    public void setF5(boolean f5) {
        this.f5 = f5;
    }

    public void setF6(boolean f6) {
        this.f6 = f6;
    }

    public void setF7(boolean f7) {
        this.f7 = f7;
    }

    public void setF8(boolean f8) {
        this.f8 = f8;
    }

    public void setF9(boolean f9) {
        this.f9 = f9;
    }

    public void setF10(boolean f10) {
        this.f10 = f10;
    }

    public void setF11(boolean f11) {
        this.f11 = f11;
    }

    public void setF12(boolean f12) {
        this.f12 = f12;
    }


    /**
     * Set the speed
     * <P>
     * @param speed Number from 0 to 1; less than zero is emergency stop
     */
    public void setSpeedSetting(float speed) {
        this.speedSetting = speed;
        int value = (int)(32*speed);     // -1 for rescale to avoid estop
        if (value>31) value = 31;    // max possible speed

        SerialMessage m = new SerialMessage();

        if (value<0) {
            // immediate stop
            m.putAsWord(0x0060+address.getNumber()*128+0);
        } else {
            // normal speed setting
            m.putAsWord(0x0060+address.getNumber()*128+value);
        }
        
        SerialTrafficController.instance().sendSerialMessage(m, null);
        SerialTrafficController.instance().sendSerialMessage(m, null);
        SerialTrafficController.instance().sendSerialMessage(m, null);
        SerialTrafficController.instance().sendSerialMessage(m, null);
    }

    public void setIsForward(boolean forward) {
        isForward = forward;

        // notify layout
        SerialMessage m = new SerialMessage();
        if (forward) {
            m.putAsWord(0x0000+address.getNumber()*128);
        } else {
            m.putAsWord(0x0003+address.getNumber()*128);
        }
        SerialTrafficController.instance().sendSerialMessage(m, null);
        SerialTrafficController.instance().sendSerialMessage(m, null);
        SerialTrafficController.instance().sendSerialMessage(m, null);
        SerialTrafficController.instance().sendSerialMessage(m, null);
        
    }

    protected void sendToLayout(int value) {
        SerialTrafficController.instance().sendSerialMessage(new SerialMessage(value), null);
        SerialTrafficController.instance().sendSerialMessage(new SerialMessage(value), null);
        SerialTrafficController.instance().sendSerialMessage(new SerialMessage(value), null);
        SerialTrafficController.instance().sendSerialMessage(new SerialMessage(value), null);
    }
    
    /*
     * setSpeedStepMode - set the speed step value.
     * <P>
     * Only 32 steps is available
     * <P>
     * @param Mode Ignored, as only 32 is valid
     */
     public void setSpeedStepMode(int Mode) {
	    speedStepMode = 32;
     }


    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SerialThrottle.class.getName());

}