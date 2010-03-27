package jmri.jmrit.withrottle;

/**
 * Handle two-way communications regarding track power.
 *
 *
 *	@author Brett Hoffman   Copyright (C) 2010
 *	@version $Revision: 1.1 $
 */

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.PowerManager;

public class TrackPowerController extends AbstractController implements PropertyChangeListener{
    
    private PowerManager pwrMgr = null;
    
    public TrackPowerController(){
        pwrMgr = InstanceManager.powerManagerInstance();
        if (pwrMgr == null){
            log.info("No power manager instance.");
            isValid = false;
        }else {
            pwrMgr.addPropertyChangeListener(this);
            isValid = true;
        }
    }

    public boolean verifyCreation(){
        return isValid;
    }

    public void handleMessage(String message){
        if (message.charAt(0) == 'A'){
            if (message.charAt(1) == '1'){
                setTrackPowerOn();
            }else if (message.charAt(1) == '0'){
                setTrackPowerOff();
            }else log.warn("Unknown Track Power message from wi-fi device");
        }
    }

    private void setTrackPowerOn() {
    	if (pwrMgr != null){
            try {
                pwrMgr.setPower(PowerManager.ON);
            }
            catch (JmriException e) {
                log.error("Cannot turn power on.");
            }
        }
    }

    private void setTrackPowerOff() {
    	if (pwrMgr != null){
            try {
                pwrMgr.setPower(PowerManager.OFF);
            }
            catch (JmriException e) {
                log.error("Cannot turn power off.");
            }
        }
    }

    public void sendCurrentState(){
        if (listeners == null) return;
        String message = null;
        try {
            if (pwrMgr.getPower()==PowerManager.ON) message = "PPA1";
            else if (pwrMgr.getPower()==PowerManager.OFF) message = "PPA0";
            else if (pwrMgr.getPower()==PowerManager.UNKNOWN) message = "PPA2";
            else log.error("Unexpected state value: +"+pwrMgr.getPower());
        }catch (JmriException e) {
            log.error("Power Manager exception");
        }

        for (ControllerInterface listener : listeners){
            listener.sendPacketToDevice(message);
        }
        
    }
    
    public void propertyChange(PropertyChangeEvent event){
        sendCurrentState();
    }

    public void dispose(){
        pwrMgr.removePropertyChangeListener(this);
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TrackPowerController.class.getName());
}
