// OlcbSensorManager.java

package jmri.jmrix.openlcb;

import jmri.Sensor;

import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;

import jmri.jmrix.can.CanSystemConnectionMemo;

/**
 * Manage the OpenLCB-specific Sensor implementation.
 *
 * System names are "MSnnn", where nnn is the sensor number without padding.
 *
 * @author			Bob Jacobsen Copyright (C) 2008, 2010
 * @version			$Revision$
 */
public class OlcbSensorManager extends jmri.managers.AbstractSensorManager implements CanListener {

    String prefix = "M";
    
    public String getSystemPrefix() { return prefix; }

    /*static public OlcbSensorManager instance() {
        if (mInstance == null) new OlcbSensorManager();
        return mInstance;
    }
    static private OlcbSensorManager mInstance = null;*/

    // to free resources when no longer used
    public void dispose() {
        memo.getTrafficController().removeCanListener(this);
        super.dispose();
    }
    
    //Implimented ready for new system connection memo
    public OlcbSensorManager(CanSystemConnectionMemo memo){
        this.memo=memo;
        prefix = memo.getSystemPrefix();
        memo.getTrafficController().addCanListener(this);
    }
    
    CanSystemConnectionMemo memo;

    public Sensor createNewSensor(String systemName, String userName) {
        String addr = systemName.substring(getSystemPrefix().length()+1);
        // first, check validity
        OlcbAddress a = new OlcbAddress(addr);
        OlcbAddress[] v = a.split();
        if (v==null) {
            log.error("Did not find usable system name: "+systemName);
            return null;
        }
        if (v.length<1 || v.length>2) {
            log.error("Wrong number of events in address: "+systemName);
            return null;
        }
        // OK, make
        Sensor s =  new OlcbSensor(getSystemPrefix(), addr, memo.getTrafficController());
        s.setUserName(userName);
        return s;
    }

    public boolean allowMultipleAdditions() { return false;  }
    
    public String createSystemName(String curAddress, String prefix) {
        // don't check for integer; should check for validity here
        return prefix+typeLetter()+curAddress;
    }

    public String getNextValidAddress(String curAddress, String prefix) {
        // always return this (the current) name without change
        return curAddress;
    }

    // ctor has to register for LocoNet events
   /* @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
                        justification="temporary until mult-system; only set at startup")
    public OlcbSensorManager() {
        TrafficController.instance().addCanListener(this);
        mInstance = this;
    }*/

    // listen for sensors, creating them as needed
    public void reply(CanReply l) {
        // doesn't do anything, because for now 
        // we want you to create manually
    }
    public void message(CanMessage l) {
        // doesn't do anything, because 
        // messages come from us
    }

    /** No mechanism currently exists to request
     * status updates from all layout sensors.
	 */
	public void updateAll() {
	}

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(OlcbSensorManager.class.getName());

}

/* @(#)OlcbSensorManager.java */
