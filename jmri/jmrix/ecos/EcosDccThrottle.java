package jmri.jmrix.ecos;

import jmri.LocoAddress;
import jmri.DccLocoAddress;
import jmri.jmrix.AbstractThrottle;

import javax.swing.*;

/**
 * An implementation of DccThrottle with code specific to an ECoS connection.
*
 * Based on Glen Oberhauser's original LnThrottleManager implementation
 *
 * @author	Bob Jacobsen  Copyright (C) 2001, modified 2009 by Kevin Dickerson
 * @version     $Revision: 1.9 $
 */
public class EcosDccThrottle extends AbstractThrottle implements EcosListener
{
    /**
     * Constructor.
     */
    String objectNumber;
    int ecosretry = 0;
    private EcosLocoAddress objEcosLoco;
    private EcosLocoAddressManager objEcosLocoManager;
    final EcosPreferences p = jmri.InstanceManager.getDefault(jmri.jmrix.ecos.EcosPreferences.class);
    //This boolean is used to prevent un-necessary commands from being sent to the ECOS if we have already lost
    //control of the object
    private boolean _haveControl = false;
    private boolean _hadControl = false;
    
    public EcosDccThrottle(DccLocoAddress address, EcosTrafficController etc)
    {
        super();
        super.speedStepMode = SpeedStepMode128;
        tc=etc;
        //The script will go through and read the values from the Ecos

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
        
                // extended values
        this.f8           = false;
        this.f9           = false;
        this.f10          = false;
        this.f11          = false;
        this.f12          = false;
        this.f13          = false;
        this.f14          = false;
        this.f15          = false;
        this.f16          = false;
        this.f17          = false;
        this.f18          = false;
        this.f19          = false;
        this.f20          = false;
        this.f21          = false;
        this.f22          = false;
        this.f23          = false;
        this.f24          = false;
        this.f25          = false;
        this.f26          = false;
        this.f27          = false;
        this.f28          = false;
        
        this.address      = address;
        this.isForward    = true;

        ecosretry         = 0;
        //objEcosLocoManager = (EcosLocoAddressManager)jmri.InstanceManager.getDefault(EcosLocoAddressManager.class);
        //objEcosLocoManager = jmri.jmrix.ecos.EcosLocoAddressManager.instance();

        objEcosLocoManager = jmri.InstanceManager.getDefault(EcosLocoAddressManager.class);

        //We go on a hunt to find an object with the dccaddress sent by our controller.
        objEcosLoco = objEcosLocoManager.provideByDccAddress(address.getNumber());

        this.objectNumber = objEcosLoco.getEcosObject();
        if (this.objectNumber==null){
            createEcosLoco();
        }
        else getControl();

    }
    
    private void getControl(){
 
        String message = "request("+this.objectNumber+", view, control)";
        EcosMessage m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);

        m = new EcosMessage(message);

        message = "get("+this.objectNumber+", speed)";
        m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);

        message = "get("+this.objectNumber+", dir)";
        m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);

    }

    //The values here might need a bit of re-working
    /**
     * Convert a Ecos speed integer to a float speed value
     */
    protected float floatSpeed(int lSpeed) {
        if (lSpeed == 0) return 0.f;
        else if (lSpeed == 1) return -1.f;   // estop
        else if (super.speedStepMode == SpeedStepMode128)
          return ( (lSpeed-1)/126.f);
        else
          return (int)(lSpeed * 27.f + 0.5) + 1 ;
    }

    /**
     * Convert a float speed value to a Ecos speed integer
     */
    protected int intSpeed(float fSpeed) {
    
          if (fSpeed == 0.f)
            return 0;
          else if (fSpeed < 0.f)
            return 1;   // estop
            // add the 0.5 to handle float to int round for positive numbers
          if (super.speedStepMode == SpeedStepMode128)
            return (int)(fSpeed * 126.f + 0.5) + 1 ;
          else
            return (int)(fSpeed * 27.f + 0.5) + 1 ;
        }
   // }
    
    //private jmri.ThrottleManager tmp;
    /**
     * Send the message to set the state of functions F0, F1, F2, F3, F4.
     */
    @Override
    protected void sendFunctionGroup1() {
        if(!_haveControl) return;
        int function = 0;
        if (getF0()==true) function = 1;
        String message = "set("+this.objectNumber+", func[0, "+function+"])";
        EcosMessage m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);

        if (getF1()==true) function = 1;
        else function = 0;
        message = "set("+this.objectNumber+", func[1, "+function+"])";
        m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);

        if (getF2()==true) function = 1;
        else function = 0;
        message = "set("+this.objectNumber+", func[2, "+function+"])";
        m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);

        if (getF3()==true) function = 1;
        else function = 0;
        message = "set("+this.objectNumber+", func[3, "+function+"])";
        m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);

        if (getF4()==true) function = 1;
        else function = 0;
        message = "set("+this.objectNumber+", func[4, "+function+"])";
        m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);

    }

    /**
     * Send the message to set the state of
     * functions F5, F6, F7, F8.
     */
    @Override
    protected void sendFunctionGroup2() {
        if(!_haveControl) return;
        int function = 0;
        if (getF5()==true) function = 1;
        String message = "set("+this.objectNumber+", func[5, "+function+"])";
        EcosMessage m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);

        if (getF6()==true) function = 1;
        else function = 0;
        message = "set("+this.objectNumber+", func[6, "+function+"])";
        m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);

        if (getF7()==true) function = 1;
        else function = 0;
        message = "set("+this.objectNumber+", func[7, "+function+"])";
        m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);

        if (getF8()==true) function = 1;
        else function = 0;
        message = "set("+this.objectNumber+", func[8, "+function+"])";
        m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);
    }

    /**
     * Send the message to set the state of
     * functions F9, F10, F11, F12.
     */
    @Override
    protected void sendFunctionGroup3() {
        if(!_haveControl) return;
        int function = 0;
        if (getF9()==true) function = 1;
        String message = "set("+this.objectNumber+", func[9, "+function+"])";
        EcosMessage m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);

        if (getF10()==true) function = 1;
        else function = 0;
        message = "set("+this.objectNumber+", func[10, "+function+"])";
        m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);

        if (getF11()==true) function = 1;
        else function = 0;
        message = "set("+this.objectNumber+", func[11, "+function+"])";
        m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);

        if (getF12()==true) function = 1;
        else function = 0;
        message = "set("+this.objectNumber+", func[12, "+function+"])";
        m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);    
    }
    
    @Override
    protected void sendFunctionGroup4() {
        if(!_haveControl) return;
        int function = 0;
        if (getF13()==true) function = 1;
        String message = "set("+this.objectNumber+", func[13, "+function+"])";
        EcosMessage m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);

        if (getF14()==true) function = 1;
        else function = 0;
        message = "set("+this.objectNumber+", func[14, "+function+"])";
        m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);

        if (getF15()==true) function = 1;
        else function = 0;
        message = "set("+this.objectNumber+", func[15, "+function+"])";
        m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);

        if (getF16()==true) function = 1;
        else function = 0;
        message = "set("+this.objectNumber+", func[16, "+function+"])";
        m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);
        
        if (getF17()==true) function = 1;
        else function = 0;
        message = "set("+this.objectNumber+", func[17, "+function+"])";
        m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);

        if (getF18()==true) function = 1;
        else function = 0;
        message = "set("+this.objectNumber+", func[18, "+function+"])";
        m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);
        
        if (getF19()==true) function = 1;
        message = "set("+this.objectNumber+", func[19, "+function+"])";
        m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);

        if (getF20()==true) function = 1;
        else function = 0;
        message = "set("+this.objectNumber+", func[20, "+function+"])";
        m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);
    }
    
    @Override
    protected void sendFunctionGroup5() {
        if(!_haveControl) return;
        int function;
        if (getF21()==true) function = 21;
        else function = 0;
        String message = "set("+this.objectNumber+", func[21, "+function+"])";
        EcosMessage m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);

        if (getF22()==true) function = 1;
        else function = 0;
        message = "set("+this.objectNumber+", func[22, "+function+"])";
        m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);

        if (getF23()==true) function = 1;
        else function = 0;
        message = "set("+this.objectNumber+", func[23, "+function+"])";
        m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);

        if (getF24()==true) function = 1;
        else function = 0;
        message = "set("+this.objectNumber+", func[24, "+function+"])";
        m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);
        

        if (getF25()==true) function = 1;
        message = "set("+this.objectNumber+", func[25, "+function+"])";
        m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);

        if (getF26()==true) function = 1;
        else function = 0;
        message = "set("+this.objectNumber+", func[26, "+function+"])";
        m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);

        if (getF27()==true) function = 1;
        else function = 0;
        message = "set("+this.objectNumber+", func[27, "+function+"])";
        m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);

        if (getF28()==true) function = 1;
        else function = 0;
        message = "set("+this.objectNumber+", func[28, "+function+"])";
        m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);
    
    }

    /**
     * Set the speed & direction.
     * <P>
     * This intentionally skips the emergency stop value of 1.
     * @param speed Number from 0 to 1; less than zero is emergency stop
     */
    //The values here might need a bit of re-working
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="FE_FLOATING_POINT_EQUALITY") // OK to compare floating point
    public void setSpeedSetting(float speed) {
        if(!_haveControl) return;
        int value;
        
        if (speed == this.speedSetting) return;
        /*if (super.speedStepMode == SpeedStepMode128) {
            value = (int)((127-1)*speed);     // -1 for rescale to avoid estop
            if (value>0) value = value+1;  // skip estop
            if (value>127) value = 127;    // max possible speed
            if (value<0) value = 0;        // emergency stop
		} else {
	        value = (int)((28-1)*speed);     // -1 for rescale to avoid estop
	        if (value>0) value = value+1;  	// skip estop
	        if (value>28) value = 28;    	// max possible speed
	        if (value<0) value = 0;        	// emergency stop
		}*/
        // The ecos always references 128 steps, when using the speed command
        // even if the speedsteps for the loco are 28, or 14 (ie marklin)
        value = (int)((128-1)*speed);     // -1 for rescale to avoid estop
        if (value>0) value = value+1;  // skip estop
        if (value>128) value = 128;    // max possible speed
        if (value<0) value = 0;        // emergency stop

        if (value >0) {
            String message = "set("+this.objectNumber+", speed["+value+"])";
            EcosMessage m = new EcosMessage(message);
            tc.sendEcosMessage(m, this);
        }
        else{
            //Not sure if this performs an emergency stop or a normal one.
            String message = "set("+this.objectNumber+", stop)";
            EcosMessage m = new EcosMessage(message);
            tc.sendEcosMessage(m, this);
        }

    }

    EcosTrafficController tc;

    public void setIsForward(boolean forward) {
        if(!_haveControl) return;
        //isForward = forward;
        int dir=1;
        //if (forward = true) dir=0;
        //setSpeedSetting(speedSetting);  // send the command

        EcosMessage m;
        if (forward==true) dir=0;

        String message = "set("+this.objectNumber+", dir["+dir+"])";
        m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);

    }

    private DccLocoAddress address;
    
    public LocoAddress getLocoAddress() {
        return address;
    }
    
    /**
     * Finished with this throttle.  Right now, this does nothing,
     * but it could set the speed to zero, turn off functions, etc.
     *
     */
    @Override
    public void release() {
        if (!active) log.warn("release called when not active");
        //Deleting of the loco is now handled when closing jmri.
        else ReleaseLoco();
        _haveControl = false;
        _hadControl = false;
    }

    /**
     * Dispose when finished with this object.  After this, further usage of
     * this Throttle object will result in a JmriException.
     */
    @Override
    public void dispose() {
        log.debug("dispose");
        release();
        
        super.dispose();
    }
    
    private void ReleaseLoco(){
        EcosMessage m;
        String message = "release("+this.objectNumber+", view, control)";
        m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);
    
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="FE_FLOATING_POINT_EQUALITY") // OK to compare floating point
    public void reply(EcosReply m) {
        int tmpstart;
        int tmpend;
        int start;
        int end;
        String msg = m.toString();
        String[] lines = msg.split("\n");
        log.debug("found "+(lines.length)+" response from Ecos");
        if (lines[lines.length-1].contains("<END 0 (OK)>")){
            if (lines[0].startsWith("<REPLY set("+this.objectNumber+",")){// || msg.startsWith("<EVENT "+this.objectNumber+">")) {
                //log.debug("The last command was accepted by the ecos");
                //This might need to use speedstep, rather than speed
                //This is for standard response to set and request.
                start = msg.indexOf("[");
                end = msg.indexOf("]");
                if (start>0 && end >0) {
                    tmpstart = msg.indexOf(", ");
                    tmpend = msg.indexOf("[");
                    String val;
                    if (tmpstart>0 && tmpend >0) {
                        String result = msg.substring(tmpstart+2, tmpend);
                        if (result.equals("speed")){
                            val = (msg.substring(start+1, end));
                             Float newSpeed = new Float ( floatSpeed(Integer.parseInt(val) ) ) ;
                             this.speedSetting = newSpeed;
                             if (this.speedSetting != newSpeed){
                                 notifyPropertyChangeListener("SpeedSetting", this.speedSetting, newSpeed);
                                 log.debug("new Speed "+ val +", " + newSpeed + " for "+this.address);
                             }
                        }
                        else if(result.equals("stop")){
                            this.speedSetting = Float.valueOf(0).floatValue();
                            log.debug("Stopping the loco");
                        }
                        else if(result.equals("dir")){
                            val = (msg.substring(start+1, end));
                            boolean newDirection;
                            if (val.equals("0")) newDirection=true;
                            else newDirection = false;
                            notifyPropertyChangeListener("IsForward", this.isForward, newDirection);
                            this.isForward = newDirection;
                            log.debug("new direction "+ this.isForward +" for "+this.address);
                        }
                    }
                }
            }
            else if(lines[0].startsWith("<REPLY get("+this.objectNumber+",")||lines[0].startsWith("<EVENT "+this.objectNumber+">")){
                //log.debug("The last command was accepted by the ecos");
                for (int i =1; i<lines.length-1; i++){
                    String object = this.objectNumber;
                    tmpstart = lines[i].indexOf(object + " ")+object.length()+1;
                    tmpend = lines[i].indexOf("[");
                    String val;
                    if (tmpstart>0 && tmpend >0) {
                        String result = lines[i].substring(tmpstart, tmpend);
                        start = lines[i].indexOf("[");
                        end = lines[i].indexOf("]");
                        if (result.equals("protocol")){
                            val = (lines[i].substring(start+1, end));
                            if (val.contains("DCC128")) this.speedStepMode=SpeedStepMode128;
                            else if (val.contains("DCC28")) this.speedStepMode=SpeedStepMode28;
                        }
                        else if (result.equals("msg")){
                            val = (lines[i].substring(start+1, end));
                            //We get this lost control error because we have registered as a viewer.
                            if (val.contains("CONTROL_LOST")){
                                retryControl();
                                log.debug("We have no control over the ecos object");
                                _haveControl = false;
                                javax.swing.JOptionPane.showMessageDialog(null,"We do not have control of loco " + this.address + "\n" + "Press Release and try again","No Control",javax.swing.JOptionPane.WARNING_MESSAGE);
                                release();
                            }
                        }
                        else if (result.equals("speed")){
                             val = (lines[i].substring(start+1, end));
                             Float newSpeed = new Float ( floatSpeed(Integer.parseInt(val) ) ) ;
                             log.debug("set new speed "+val+" for "+this.address);
                             if (this.speedSetting != newSpeed){
                                notifyPropertyChangeListener("SpeedSetting", this.speedSetting, newSpeed);
                                this.speedSetting = newSpeed;
                                log.debug("see new Speed "+ val +", " + newSpeed + " for "+this.address);
                             }
                             else
                                log.debug("Speed has not changed for "+this.address);
                        }
                        else if(result.equals("dir")){
                            val = (lines[i].substring(start+1, end));
                            boolean newDirection;
                            if (val.equals("0")) newDirection = true;
                            else newDirection = false;
                            if (newDirection != this.isForward){
                                notifyPropertyChangeListener("IsForward", this.isForward, newDirection);
                                log.debug("see new direction "+ newDirection +" for "+this.address);
                                this.isForward = newDirection;
                            }
                            log.debug("direction has not changed for "+this.address);
                        }
                        else if (lines[i].contains("func[")) {
                            int funcstart = lines[i].indexOf("[")+1;
                            int funcfinish = lines[i].indexOf(", ");

                            int function = Integer.parseInt(lines[i].substring(funcstart, funcfinish));
                            int valstart = lines[i].indexOf(", ")+2;
                            int valfinish = lines[i].indexOf("]");
                            int functionValue = Integer.parseInt(lines[i].substring(valstart, valfinish));
                            boolean functionresult = false;
                            if (functionValue == 1) functionresult = true;
                            switch (function) {
                                case 0: if (this.f0!=functionresult) {
                                            notifyPropertyChangeListener("F0", this.f0, functionresult);
                                            this.f0 = functionresult;
                                         }
                                         break;
                                case 1: if (this.f1!=functionresult) {
                                            notifyPropertyChangeListener("F1", this.f1, functionresult);
                                            this.f1 = functionresult;
                                         }
                                         break;
                                case 2: if (this.f2!=functionresult) {
                                            notifyPropertyChangeListener("F2", this.f2, functionresult);
                                            this.f2 = functionresult;
                                         }
                                         break;
                                case 3: if (this.f3!=functionresult) {
                                            notifyPropertyChangeListener("F3", this.f3, functionresult);
                                            this.f3 = functionresult;
                                         }
                                         break;
                                case 4: if (this.f4!=functionresult) {
                                            notifyPropertyChangeListener("F4", this.f4, functionresult);
                                            this.f4 = functionresult;
                                         }
                                         break;
                                case 5: if (this.f5!=functionresult) {
                                            notifyPropertyChangeListener("F5", this.f5, functionresult);
                                            this.f5 = functionresult;
                                         }
                                         break;
                                case 6: if (this.f6!=functionresult) {
                                            notifyPropertyChangeListener("F6", this.f6, functionresult);
                                            this.f6 = functionresult;
                                         }
                                         break;
                                case 7: if (this.f7!=functionresult) {
                                            notifyPropertyChangeListener("F7", this.f7, functionresult);
                                            this.f7 = functionresult;
                                         }
                                         break;
                                case 8: if (this.f8!=functionresult) {
                                            notifyPropertyChangeListener("F8", this.f8, functionresult);
                                            this.f8 = functionresult;
                                         }
                                         break;
                                case 9: if (this.f9!=functionresult) {
                                            notifyPropertyChangeListener("F9", this.f9, functionresult);
                                            this.f9 = functionresult;
                                         }
                                         break;
                                case 10: if (this.f10!=functionresult) {
                                            notifyPropertyChangeListener("F10", this.f10, functionresult);
                                            this.f10 = functionresult;
                                         }
                                         break;
                                case 11: if (this.f11!=functionresult) {
                                            notifyPropertyChangeListener("F11", this.f11, functionresult);
                                            this.f11 = functionresult;
                                         }
                                         break;
                                case 12: if (this.f12!=functionresult) {
                                            notifyPropertyChangeListener("F12", this.f12, functionresult);
                                            this.f12 = functionresult;
                                         }
                                         break;
                                case 13: if (this.f13!=functionresult) {
                                            notifyPropertyChangeListener("F13", this.f13, functionresult);
                                            this.f13 = functionresult;
                                         }
                                         break;
                                case 14: if (this.f14!=functionresult) {
                                            notifyPropertyChangeListener("F14", this.f14, functionresult);
                                            this.f14 = functionresult;
                                         }
                                         break;
                                case 15: if (this.f15!=functionresult) {
                                            notifyPropertyChangeListener("F15", this.f15, functionresult);
                                            this.f15 = functionresult;
                                         }
                                         break;
                                case 16: if (this.f16!=functionresult) {
                                            notifyPropertyChangeListener("F16", this.f16, functionresult);
                                            this.f16 = functionresult;
                                         }
                                         break;
                                case 17: if (this.f17!=functionresult) {
                                            notifyPropertyChangeListener("F17", this.f17, functionresult);
                                            this.f17 = functionresult;
                                         }
                                         break;
                                case 18: if (this.f18!=functionresult) {
                                            notifyPropertyChangeListener("F18", this.f18, functionresult);
                                            this.f18 = functionresult;
                                         }
                                         break;
                                case 19: if (this.f19!=functionresult) {
                                            notifyPropertyChangeListener("F19", this.f19, functionresult);
                                            this.f19 = functionresult;
                                         }
                                         break;
                                case 20: if (this.f20!=functionresult) {
                                            notifyPropertyChangeListener("F20", this.f20, functionresult);
                                            this.f20 = functionresult;
                                         }
                                         break;
                                case 21: if (this.f21!=functionresult) {
                                            notifyPropertyChangeListener("F21", this.f21, functionresult);
                                            this.f21 = functionresult;
                                         }
                                         break;
                                case 22: if (this.f22!=functionresult) {
                                            notifyPropertyChangeListener("F22", this.f22, functionresult);
                                            this.f22 = functionresult;
                                        }
                                        break;
                                case 23: if (this.f23!=functionresult) {
                                            notifyPropertyChangeListener("F23", this.f23, functionresult);
                                            this.f23 = functionresult;
                                         }
                                         break;
                                case 24: if (this.f24!=functionresult) {
                                            notifyPropertyChangeListener("F24", this.f24, functionresult);
                                            this.f24 = functionresult;
                                         }
                                         break;
                                case 25: if (this.f25!=functionresult) {
                                            notifyPropertyChangeListener("F25", this.f25, functionresult);
                                            this.f25 = functionresult;
                                         }
                                         break;
                                case 26: if (this.f26!=functionresult) {
                                            notifyPropertyChangeListener("F26", this.f26, functionresult);
                                            this.f26 = functionresult;
                                         }
                                         break;
                                case 27: if (this.f27!=functionresult) {
                                            notifyPropertyChangeListener("F27", this.f27, functionresult);
                                            this.f27 = functionresult;
                                         }
                                         break;
                                case 28: if (this.f28!=functionresult) {
                                            notifyPropertyChangeListener("F28", this.f28, functionresult);
                                            this.f28 = functionresult;
                                         }
                                         break;
                            }
                        }
                    }
                }

            }
            else if (lines[0].startsWith("<REPLY create(10, addr")){
                //String object = Integer.toString(this.objectNumber);
                //log.debug("found "+(lines.length)+" response from Ecos for Create");
                for(int i =1; i<lines.length-1; i++) {
                    if(lines[i].contains("10 id[")){
                        start = lines[i].indexOf("[")+1;
                        end = lines[i].indexOf("]");
                        String EcosAddr = lines[i].substring(start, end);
                        objEcosLoco.setEcosObject(EcosAddr);
                        objEcosLocoManager.deregister(objEcosLoco);
                        objEcosLocoManager.register(objEcosLoco);
                        objEcosLoco.setEcosTempEntry(true);
                        objEcosLoco.doNotAddToRoster();
                        this.objectNumber=EcosAddr;
                        getControl();
                    }
                }
            }
            else if (lines[0].startsWith("<REPLY release("+this.objectNumber)){
                log.debug("Released "+this.objectNumber +" from the Ecos");
                _haveControl = false;
            }
            else if (lines[0].startsWith("<REPLY request("+this.objectNumber)){
                log.debug("We have control over "+this.objectNumber +" from the Ecos");
                _haveControl = true;
                if (!_hadControl){
                    EcosDccThrottleManager.instance().throttleSetup(this, this.address, true);
                }
            }
        }
        else if (lines[lines.length-1].contains("<END 0 (NERROR_OK)>")){
            //Need to investigate this a bit futher to see what the significance of the message is
            //we may not have to worry much about it.
            log.info("Loco has been created on the ECoS Sucessfully.");
        }
        else if (lines[lines.length-1].contains("<END 35 (NERROR_NOAPPEND)>")){
            /**
            * This message occurs when have already created a loco, but have not appended it to
            * the database.  The Ecos will not allow another loco to be created until the previous
            * entry has been appended.
            */
            
            //Potentially need to deal with this error better.
            log.info("Another loco create operation is already taking place unable to create another.");

        }
        else if (lines[lines.length-1].contains("<END 25 (NERROR_NOCONTROL)>")){
            /**
            * This section deals with no longer having control over the ecos loco object.
            * we try three times to request control, on the fourth attempt we try a forced
            * control, if that fails we inform the user and reset the counter to zero.
            */
            retryControl();
        }
        else if (lines[lines.length-1].contains("<END 15 (NERROR_UNKNOWNID)>")){
            log.info("Loco can not be accessed via the Ecos Object Id " + this.objectNumber);
            javax.swing.JOptionPane.showMessageDialog(null,"Loco is unknown on the Ecos" + "\n" + this.address + "Please try access again","No Control",javax.swing.JOptionPane.WARNING_MESSAGE);
            release();
        }
        else log.debug("Last Message resulted in an END code we do not understand\n" + lines[lines.length-1]);
    }

    public void message(EcosMessage m) {
        //System.out.println("Ecos message - "+ m);
        // messages are ignored
    }
    
    private void createEcosLoco() {

        String message = "create(10, addr[" + objEcosLoco.getEcosLocoAddress() + "], name[\"Created By JMRI\"], protocol[DCC128], append)";
        EcosMessage m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);
    
    }

    private void retryControl(){
        if(_haveControl) _hadControl=true;
        _haveControl = false;
        if (ecosretry <3){
            //It might be worth adding in a sleep/pause of discription between retries.
            ecosretry++;

            String message = "request("+this.objectNumber+", control)";
            EcosMessage ms = new EcosMessage(message);
            tc.sendEcosMessage(ms, this);
            log.error("We have no control over the ecos object " + this.objectNumber + " Retrying Attempt " + ecosretry);
        }
        else if(ecosretry==3){
            ecosretry++;
            int val=0;
            if (p.getForceControlFromEcos()==0x00)
                val = javax.swing.JOptionPane.showConfirmDialog(null,"Unable to gain control of the Loco \n Another operator may have control of the Loco \n Do you want to attempt a forced take over?","No Control", JOptionPane.YES_NO_OPTION,javax.swing.JOptionPane.QUESTION_MESSAGE);
            else{
                if(p.getForceControlFromEcos()==0x01)
                    val=1;
            }
            if (val==0)
            {
                String message = "request("+this.objectNumber+", control, force)";
                EcosMessage ms = new EcosMessage(message);
                tc.sendEcosMessage(ms, this);
            }
            else
                if(_hadControl) {
                    notifyPropertyChangeListener("LostControl", 0, 0);
                    _hadControl=false;
                } else
                    EcosDccThrottleManager.instance().throttleSetup(this, this.address, false);
                
                log.error("We have no control over the ecos object " + this.objectNumber + "Trying a forced control");
        }
        else{
            //javax.swing.JOptionPane.showMessageDialog(null,"We have lost control over" + "\n" + this.address + " and the Ecos","No Control",javax.swing.JOptionPane.WARNING_MESSAGE);
            ecosretry=0;
            if(_hadControl) {
                notifyPropertyChangeListener("LostControl", 0, 0);
            } else
                EcosDccThrottleManager.instance().throttleSetup(this, this.address, false);
            release();
        }
    }

    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EcosDccThrottle.class.getName());

}
