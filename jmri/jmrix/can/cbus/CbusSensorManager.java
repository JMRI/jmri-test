// CbusSensorManager.java

package jmri.jmrix.can.cbus;

import jmri.Sensor;

import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.TrafficController;

/**
 * Manage the CBUS-specific Sensor implementation.
 *
 * System names are "MSnnn", where nnn is the sensor number without padding.
 *
 * @author			Bob Jacobsen Copyright (C) 2008
 * @version			$Revision: 1.2 $
 */
public class CbusSensorManager extends jmri.AbstractSensorManager implements CanListener {

    public char systemLetter() { return 'M'; }

    static public CbusSensorManager instance() {
        if (mInstance == null) new CbusSensorManager();
        return mInstance;
    }
    static private CbusSensorManager mInstance = null;

    // to free resources when no longer used
    public void dispose() {
        TrafficController.instance().removeCanListener(this);
        super.dispose();
    }

    // CBUS-specific methods

    public Sensor createNewSensor(String systemName, String userName) {
        // first, check validity
        CbusAddress a = new CbusAddress(systemName.substring(2,systemName.length()));
        CbusAddress[] v = a.split();
        if (v==null) {
            log.error("Did not find usable system name: "+systemName);
            return null;
        }
        if (v.length<1 || v.length>2) {
            log.error("Wrong number of events in address: "+systemName);
            return null;
        }
        // OK, make
        return new CbusSensor(systemName, userName);
    }

    // ctor has to register for LocoNet events
    public CbusSensorManager() {
        TrafficController.instance().addCanListener(this);
        mInstance = this;
    }

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

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CbusSensorManager.class.getName());

}

/* @(#)CbusSensorManager.java */
