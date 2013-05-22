package jmri.jmrix.ecos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.LocoAddress;
import jmri.DccLocoAddress;
import jmri.jmrix.AbstractThrottle;
import java.awt.HeadlessException;

import javax.swing.*;
import jmri.Throttle;

/**
 * An implementation of DccThrottle with code specific to an ECoS connection.
*
 * Based on Glen Oberhauser's original LnThrottleManager implementation
 *
 * @author	Bob Jacobsen  Copyright (C) 2001, modified 2009 by Kevin Dickerson
 * @version     $Revision$
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
    final EcosPreferences p;
    //This boolean is used to prevent un-necessary commands from being sent to the ECOS if we have already lost
    //control of the object
    private boolean _haveControl = false;
    private boolean _hadControl = false;
    private boolean _control = true;
    
    public EcosDccThrottle(DccLocoAddress address, EcosSystemConnectionMemo memo, boolean control)
    {
        super(memo);
        super.speedStepMode = SpeedStepMode128;
        p = memo.getPreferenceManager();
        tc=memo.getTrafficController();
        objEcosLocoManager = memo.getLocoAddressManager();
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
        this._control      = control;

        ecosretry         = 0;

        //We go on a hunt to find an object with the dccaddress sent by our controller.
        objEcosLoco = objEcosLocoManager.provideByDccAddress(address.getNumber());

        this.objectNumber = objEcosLoco.getEcosObject();
        if (this.objectNumber==null){
            createEcosLoco();
        }
        else getControl();

    }
    
    private void getControl(){
        String message;
        setSpeedStepMode(objEcosLoco.getSpeedStepMode());
        message = "get("+this.objectNumber+", speed)";
        EcosMessage  m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);

        message = "get("+this.objectNumber+", dir)";
        m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);

        if(_control){
            if (p.getLocoControl())
                message = "request("+this.objectNumber+", view, control, force)";
            else
                message = "request("+this.objectNumber+", view, control)";
        }
        else
            message = "request("+this.objectNumber+", view)";
        
        m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);
    }

    //The values here might need a bit of re-working
    /**
     * Convert a Ecos speed integer to a float speed value
     */
    protected float floatSpeed(int lSpeed) {
        if (lSpeed == 0) return 0.0f;
        return ( (lSpeed)/126.f);

    }
    
    /**
     * Send the message to set the state of functions F0, F1, F2, F3, F4.
     */
    @Override
    protected void sendFunctionGroup1() {
        if(!_haveControl) return;
        
        String message = "set("+this.objectNumber+", func[0, "+(getF0()? 1 : 0 )+"])";
        EcosMessage m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);
        
        message = "set("+this.objectNumber+", func[1, "+(getF1()? 1 : 0 )+"])";
        m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);
        
        message = "set("+this.objectNumber+", func[2, "+(getF2()? 1 : 0 )+"])";
        m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);
        
        message = "set("+this.objectNumber+", func[3, "+(getF3()? 1 : 0 )+"])";
        m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);
        
        message = "set("+this.objectNumber+", func[4, "+(getF4()? 1 : 0 )+"])";
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

        String message = "set("+this.objectNumber+", func[5, "+(getF5()? 1 : 0 )+"])";
        EcosMessage m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);
        
        message = "set("+this.objectNumber+", func[6, "+(getF6()? 1 : 0 )+"])";
        m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);
        
        message = "set("+this.objectNumber+", func[7, "+(getF7()? 1 : 0 )+"])";
        m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);
        
        message = "set("+this.objectNumber+", func[8, "+(getF8()? 1 : 0 )+"])";
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
        
        String message = "set("+this.objectNumber+", func[9, "+(getF9()? 1 : 0 )+"])";
        EcosMessage m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);
        
        message = "set("+this.objectNumber+", func[10, "+(getF10()? 1 : 0 )+"])";
        m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);
        
        message = "set("+this.objectNumber+", func[11, "+(getF11()? 1 : 0 )+"])";
        m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);
        
        message = "set("+this.objectNumber+", func[12, "+(getF12()? 1 : 0 )+"])";
        m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);    
    }
    
    @Override
    protected void sendFunctionGroup4() {
        if(!_haveControl) return;

        String message = "set("+this.objectNumber+", func[13, "+(getF13()? 1 : 0 )+"])";
        EcosMessage m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);

        message = "set("+this.objectNumber+", func[14, "+(getF14()? 1 : 0 )+"])";
        m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);

        message = "set("+this.objectNumber+", func[15, "+(getF15()? 1 : 0 )+"])";
        m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);

        message = "set("+this.objectNumber+", func[16, "+(getF16()? 1 : 0 )+"])";
        m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);
        
        message = "set("+this.objectNumber+", func[17, "+(getF17()? 1 : 0 )+"])";
        m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);

        message = "set("+this.objectNumber+", func[18, "+(getF18()? 1 : 0 )+"])";
        m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);
        
        message = "set("+this.objectNumber+", func[19, "+(getF19()? 1 : 0 )+"])";
        m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);

        message = "set("+this.objectNumber+", func[20, "+(getF20()? 1 : 0 )+"])";
        m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);
    }
    
    @Override
    protected void sendFunctionGroup5() {
        if(!_haveControl) return;

        String message = "set("+this.objectNumber+", func[21, "+(getF21()? 1 : 0 )+"])";
        EcosMessage m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);

        message = "set("+this.objectNumber+", func[22, "+(getF22()? 1 : 0 )+"])";
        m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);

        message = "set("+this.objectNumber+", func[23, "+(getF23()? 1 : 0 )+"])";
        m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);

        message = "set("+this.objectNumber+", func[24, "+(getF24()? 1 : 0 )+"])";
        m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);
        

        message = "set("+this.objectNumber+", func[25, "+(getF25()? 1 : 0 )+"])";
        m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);

        message = "set("+this.objectNumber+", func[26, "+(getF26()? 1 : 0 )+"])";
        m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);

        message = "set("+this.objectNumber+", func[27, "+(getF27()? 1 : 0 )+"])";
        m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);

        message = "set("+this.objectNumber+", func[28, "+(getF28()? 1 : 0 )+"])";
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
        if (speed == this.speedSetting){
            return;
        }
        value = (int)((127-1)*speed);     // -1 for rescale to avoid estop
        if (value>128) value = 126;    // max possible speed
        if ((value >0) || (value ==0.0)) {
            this.speedSetting = speed;
            String message = "set("+this.objectNumber+", speed["+value+"])";
            EcosMessage m = new EcosMessage(message);
            tc.sendEcosMessage(m, this);
            speedMessageSent++;
        }
        else {
            //Not sure if this performs an emergency stop or a normal one.
            String message = "set("+this.objectNumber+", stop)";
            this.speedSetting = 0.0f;
            EcosMessage m = new EcosMessage(message);
            tc.sendEcosMessage(m, this);
            
        }
        record(speed);
    }

    EcosTrafficController tc;
    
    int speedMessageSent = 0;

    public void setIsForward(boolean forward) {
        if(!_haveControl) return;
        
        EcosMessage m;
        
        String message = "set("+this.objectNumber+", dir["+(forward?0:1)+"])";
        m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);
    }

    private DccLocoAddress address;
    
    public LocoAddress getLocoAddress() {
        return address;
    }
    
    protected void throttleDispose(){
        EcosMessage m;
        String message = "release("+this.objectNumber+", control)";
        m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);
        _haveControl = false;
        _hadControl = false;
        finishRecord();
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="FE_FLOATING_POINT_EQUALITY") // OK to compare floating point
    public void reply(EcosReply m) {
        /*int tmpstart;
        int tmpend;
        int start;
        int end;
        String msg = m.toString();
        String[] lines = msg.split("\n");
        log.debug("found "+(lines.length)+" response from Ecos");*/
        int resultCode = m.getResultCode();
        if (resultCode==0){
            String replyType = m.getReplyType();
            if(replyType.equals("create")){
                String[] msgDetails = m.getContents();
                for (String line: msgDetails) {
                    if(line.startsWith("10 id[")){
                        String EcosAddr = EcosReply.getContentDetail(line);
                        objEcosLoco.setEcosObject(EcosAddr);
                        objEcosLocoManager.deregister(objEcosLoco);
                        objEcosLocoManager.register(objEcosLoco);
                        objEcosLoco.setEcosTempEntry(true);
                        objEcosLoco.doNotAddToRoster();
                        this.objectNumber=EcosAddr;
                        getControl();
                    }
                }
                return;
            }
            
            /*if (lines[lines.length-1].contains("<END 0 (NERROR_OK)>")){
                //Need to investigate this a bit futher to see what the significance of the message is
                //we may not have to worry much about it.
                log.info("Loco has been created on the ECoS Sucessfully.");
                return;
            }*/
            if(m.getEcosObjectId()!=objEcosLoco.getEcosObjectAsInt()){
                log.debug("message is not for us");
                return;
            }
            if(replyType.equals("set")){
                //This might need to use speedstep, rather than speed
                //This is for standard response to set and request.
                String[] msgDetails = m.getContents();
                for (String line: msgDetails) {
                    if (line.contains("speed")&& !line.contains("speedstep")){
                        if(speedMessageSent==1){
                            Float newSpeed = new Float (floatSpeed(Integer.parseInt(EcosReply.getContentDetails(line, "speed"))) ) ;
                            super.setSpeedSetting(newSpeed);
                        }
                        speedMessageSent--;
                    }
                    else if (line.contains("dir")){
                        boolean newDirection = false;
                        if (EcosReply.getContentDetails(line, "dir").equals("0")) newDirection=true;
                        super.setIsForward(newDirection);
                    }
                }
                if(msgDetails.length==0){
                    //For some reason in recent ECOS software releases we do not get the contents, only a header and End State
                    if(m.toString().contains("speed")&& !m.toString().contains("speedstep")){
                        if(speedMessageSent==1){
                            Float newSpeed = new Float (floatSpeed(Integer.parseInt(EcosReply.getContentDetails(m.toString(), "speed"))) ) ;
                            super.setSpeedSetting(newSpeed);
                        }
                        speedMessageSent--;
                    } else if (m.toString().contains("dir")){
                        boolean newDirection = false;
                        if (EcosReply.getContentDetails(m.toString(), "dir").equals("0")) newDirection=true;
                        super.setIsForward(newDirection);
                    }
                }
            }
            //Treat gets and events as the same.
            else if((replyType.equals("get")) || (m.isUnsolicited())){
                //log.debug("The last command was accepted by the ecos");
                String[] msgDetails = m.getContents();
                for (String line: msgDetails) {
                    if(speedMessageSent>0 && m.isUnsolicited() && line.contains("speed")){
                        //We want to ignore these messages.
                    } else if (speedMessageSent>0 && line.contains("speed") && !line.contains("speedstep")){
                        Float newSpeed = new Float (floatSpeed(Integer.parseInt(EcosReply.getContentDetails(line, "speed"))) ) ;
                        super.setSpeedSetting(newSpeed);
                    } else if (line.contains("dir")){
                        boolean newDirection = false;
                        if (EcosReply.getContentDetails(line, "dir").equals("0")) newDirection=true;
                        super.setIsForward(newDirection);
                    } else if (line.contains("protocol")){
                        String pro = EcosReply.getContentDetails(line, "protocol");
                        if(pro.equals("DCC128")) setSpeedStepMode(SpeedStepMode128);
                        else if (pro.equals("DCC28")) setSpeedStepMode(SpeedStepMode28);
                        else if (pro.equals("DCC14")) setSpeedStepMode(SpeedStepMode14);
                    } else if (line.contains("func")){
                        String funcStr = EcosReply.getContentDetails(line, "func");
                        int function = Integer.parseInt(funcStr.substring(0, funcStr.indexOf(",")));
                        int functionValue = Integer.parseInt(funcStr.substring((funcStr.indexOf(", ")+2), funcStr.length()));
                        boolean functionresult = false;
                        if (functionValue == 1) functionresult = true;
                        switch (function) {
                            case 0: if (this.f0!=functionresult) {
                                        notifyPropertyChangeListener(Throttle.F0, this.f0, functionresult);
                                        this.f0 = functionresult;
                                     }
                                     break;
                            case 1: if (this.f1!=functionresult) {
                                        notifyPropertyChangeListener(Throttle.F1, this.f1, functionresult);
                                        this.f1 = functionresult;
                                     }
                                     break;
                            case 2: if (this.f2!=functionresult) {
                                        notifyPropertyChangeListener(Throttle.F2, this.f2, functionresult);
                                        this.f2 = functionresult;
                                     }
                                     break;
                            case 3: if (this.f3!=functionresult) {
                                        notifyPropertyChangeListener(Throttle.F3, this.f3, functionresult);
                                        this.f3 = functionresult;
                                     }
                                     break;
                            case 4: if (this.f4!=functionresult) {
                                        notifyPropertyChangeListener(Throttle.F4, this.f4, functionresult);
                                        this.f4 = functionresult;
                                     }
                                     break;
                            case 5: if (this.f5!=functionresult) {
                                        notifyPropertyChangeListener(Throttle.F5, this.f5, functionresult);
                                        this.f5 = functionresult;
                                     }
                                     break;
                            case 6: if (this.f6!=functionresult) {
                                        notifyPropertyChangeListener(Throttle.F6, this.f6, functionresult);
                                        this.f6 = functionresult;
                                     }
                                     break;
                            case 7: if (this.f7!=functionresult) {
                                        notifyPropertyChangeListener(Throttle.F7, this.f7, functionresult);
                                        this.f7 = functionresult;
                                     }
                                     break;
                            case 8: if (this.f8!=functionresult) {
                                        notifyPropertyChangeListener(Throttle.F8, this.f8, functionresult);
                                        this.f8 = functionresult;
                                     }
                                     break;
                            case 9: if (this.f9!=functionresult) {
                                        notifyPropertyChangeListener(Throttle.F9, this.f9, functionresult);
                                        this.f9 = functionresult;
                                     }
                                     break;
                            case 10: if (this.f10!=functionresult) {
                                        notifyPropertyChangeListener(Throttle.F10, this.f10, functionresult);
                                        this.f10 = functionresult;
                                     }
                                     break;
                            case 11: if (this.f11!=functionresult) {
                                        notifyPropertyChangeListener(Throttle.F11, this.f11, functionresult);
                                        this.f11 = functionresult;
                                     }
                                     break;
                            case 12: if (this.f12!=functionresult) {
                                        notifyPropertyChangeListener(Throttle.F12, this.f12, functionresult);
                                        this.f12 = functionresult;
                                     }
                                     break;
                            case 13: if (this.f13!=functionresult) {
                                        notifyPropertyChangeListener(Throttle.F13, this.f13, functionresult);
                                        this.f13 = functionresult;
                                     }
                                     break;
                            case 14: if (this.f14!=functionresult) {
                                        notifyPropertyChangeListener(Throttle.F14, this.f14, functionresult);
                                        this.f14 = functionresult;
                                     }
                                     break;
                            case 15: if (this.f15!=functionresult) {
                                        notifyPropertyChangeListener(Throttle.F15, this.f15, functionresult);
                                        this.f15 = functionresult;
                                     }
                                     break;
                            case 16: if (this.f16!=functionresult) {
                                        notifyPropertyChangeListener(Throttle.F16, this.f16, functionresult);
                                        this.f16 = functionresult;
                                     }
                                     break;
                            case 17: if (this.f17!=functionresult) {
                                        notifyPropertyChangeListener(Throttle.F17, this.f17, functionresult);
                                        this.f17 = functionresult;
                                     }
                                     break;
                            case 18: if (this.f18!=functionresult) {
                                        notifyPropertyChangeListener(Throttle.F18, this.f18, functionresult);
                                        this.f18 = functionresult;
                                     }
                                     break;
                            case 19: if (this.f19!=functionresult) {
                                        notifyPropertyChangeListener(Throttle.F19, this.f19, functionresult);
                                        this.f19 = functionresult;
                                     }
                                     break;
                            case 20: if (this.f20!=functionresult) {
                                        notifyPropertyChangeListener(Throttle.F20, this.f20, functionresult);
                                        this.f20 = functionresult;
                                     }
                                     break;
                            case 21: if (this.f21!=functionresult) {
                                        notifyPropertyChangeListener(Throttle.F21, this.f21, functionresult);
                                        this.f21 = functionresult;
                                     }
                                     break;
                            case 22: if (this.f22!=functionresult) {
                                        notifyPropertyChangeListener(Throttle.F22, this.f22, functionresult);
                                        this.f22 = functionresult;
                                    }
                                    break;
                            case 23: if (this.f23!=functionresult) {
                                        notifyPropertyChangeListener(Throttle.F23, this.f23, functionresult);
                                        this.f23 = functionresult;
                                     }
                                     break;
                            case 24: if (this.f24!=functionresult) {
                                        notifyPropertyChangeListener(Throttle.F24, this.f24, functionresult);
                                        this.f24 = functionresult;
                                     }
                                     break;
                            case 25: if (this.f25!=functionresult) {
                                        notifyPropertyChangeListener(Throttle.F25, this.f25, functionresult);
                                        this.f25 = functionresult;
                                     }
                                     break;
                            case 26: if (this.f26!=functionresult) {
                                        notifyPropertyChangeListener(Throttle.F26, this.f26, functionresult);
                                        this.f26 = functionresult;
                                     }
                                     break;
                            case 27: if (this.f27!=functionresult) {
                                        notifyPropertyChangeListener(Throttle.F27, this.f27, functionresult);
                                        this.f27 = functionresult;
                                     }
                                     break;
                            case 28: if (this.f28!=functionresult) {
                                        notifyPropertyChangeListener(Throttle.F28, this.f28, functionresult);
                                        this.f28 = functionresult;
                                     }
                                     break;
                            default : break;
                        }
                    
                    } else if (line.contains("msg")){
                        //We get this lost control error because we have registered as a viewer.
                        if (line.contains("CONTROL_LOST")){
                            retryControl();
                            log.debug("We have no control over the ecos object, but will retry.");
                        }
                    
                    }
                
                }
            }
            else if(replyType.equals("release")){
                log.debug("Released "+this.objectNumber +" from the Ecos");
                _haveControl = false;
            }
            else if(replyType.equals("request")){
                log.debug("We have control over "+this.objectNumber +" from the Ecos");
                ecosretry=0;
                if(_control)
                    _haveControl = true;
                if (!_hadControl){
                    ((EcosDccThrottleManager)adapterMemo.get(jmri.ThrottleManager.class)).throttleSetup(this, this.address, true);
                }
                getInitialStates();
            }
        }
        else if (resultCode==35){
            /**
            * This message occurs when have already created a loco, but have not appended it to
            * the database.  The Ecos will not allow another loco to be created until the previous
            * entry has been appended.
            */
            
            //Potentially need to deal with this error better.
            log.info("Another loco create operation is already taking place unable to create another.");

        }
        else if (resultCode==25){
            /**
            * This section deals with no longer having control over the ecos loco object.
            * we try three times to request control, on the fourth attempt we try a forced
            * control, if that fails we inform the user and reset the counter to zero.
            */
            retryControl();
        }
        else if (resultCode==15){
            log.info("Loco can not be accessed via the Ecos Object Id " + this.objectNumber);
            try {
                javax.swing.JOptionPane.showMessageDialog(null,"Loco is unknown on the Ecos" + "\n" + this.address + "Please try access again","No Control",javax.swing.JOptionPane.WARNING_MESSAGE);
            } catch (HeadlessException he) {
                // silently ignore inability to display dialog
            }
            jmri.InstanceManager.throttleManagerInstance().releaseThrottle(this, null);
        }
        else log.debug("Last Message resulted in an END code we do not understand " + resultCode);
    }

    public void message(EcosMessage m) {
        //System.out.println("Ecos message - "+ m);
        // messages are ignored
    }

    public void forceControl(){
        String message = "request("+this.objectNumber+", control, force)";
        EcosMessage ms = new EcosMessage(message);
        tc.sendEcosMessage(ms, this);
    }
    
    //Converts the int value of the protocol to the ESU protocol string
    private String protocol(LocoAddress.Protocol protocol){
        switch(protocol){
            case MOTOROLA: return "MM28";
            case SELECTRIX: return "SX28";
            case MFX: return "MMFKT";
            default: return "DCC128";
        }
    }
    
    private void createEcosLoco() {
        objEcosLoco.setEcosDescription("Created By JMRI");
        objEcosLoco.setProtocol(protocol(address.getProtocol()));
        String message = "create(10, addr[" + objEcosLoco.getEcosLocoAddress() + "], name[\"Created By JMRI\"], protocol[" + objEcosLoco.getProtocol() + "], append)";
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
            if (p.getForceControlFromEcos()==0x00) {
                try {
                    val = javax.swing.JOptionPane.showConfirmDialog(null,"Unable to gain control of the Loco \n Another operator may have control of the Loco \n Do you want to attempt a forced take over?","No Control", JOptionPane.YES_NO_OPTION,javax.swing.JOptionPane.QUESTION_MESSAGE);
                } catch (HeadlessException he) {
                    val=1;
                }
            }
            else{
                if(p.getForceControlFromEcos()==0x01)
                    val=1;
            }
            if (val==0) {
                String message = "request("+this.objectNumber+", control, force)";
                EcosMessage ms = new EcosMessage(message);
                tc.sendEcosMessage(ms, this);
                log.error("We have no control over the ecos object " + this.objectNumber + "Trying a forced control");
            } else{
                if(_hadControl) {
                    notifyPropertyChangeListener("LostControl", 0, 0);
                    _hadControl=false;
                    ecosretry=0;
                } else {
                     ((EcosDccThrottleManager)adapterMemo.get(jmri.ThrottleManager.class)).throttleSetup(this, this.address, false);
                }
            }
        } else{
            ecosretry=0;
            if(_hadControl) {
                notifyPropertyChangeListener("LostControl", 0, 0);
            } else{
                ((EcosDccThrottleManager)adapterMemo.get(jmri.ThrottleManager.class)).throttleSetup(this, this.address, false);
            }
             ((EcosDccThrottleManager)adapterMemo.get(jmri.ThrottleManager.class)).releaseThrottle(this, null);
        }
    }
    
    void getInitialStates(){
        String message = "get("+this.objectNumber+", speed)";
        EcosMessage m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);        
        message = "get("+this.objectNumber+", dir)";
        m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);
        for(int i=0; i<=28; i++){
            message = "get("+this.objectNumber+", func["+i+"])";
            m = new EcosMessage(message);
            tc.sendEcosMessage(m, this);
        }
    }
    
    // initialize logging
    static Logger log = LoggerFactory.getLogger(EcosDccThrottle.class.getName());

}
