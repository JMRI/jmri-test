// RaspberryPiSensorManager.java

package jmri.jmrix.pi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.Sensor;
import jmri.JmriException;

/**
 * Manage the RaspberryPi specific Sensor implementation.
 *
 * System names are "PSnnn", where nnn is the sensor number without padding.
 *
 * @author			Paul Bender Copyright (C) 2015
 * @version			$Revision$
 */
public class RaspberryPiSensorManager extends jmri.managers.AbstractSensorManager {

    // ctor has to register for RaspberryPi events
    public RaspberryPiSensorManager(String prefix) {
        super();
        this.prefix=prefix;
    }

    /**
     * Provides access to the system prefix string.
     * This was previously called the "System letter"
     */
    @Override
    public String getSystemPrefix(){ return prefix; }

    private String prefix = null;

    // to free resources when no longer used
    public void dispose() {
        super.dispose();
    }

    @Override
    public Sensor createNewSensor(String systemName, String userName) {
        return new RaspberryPiSensor(systemName, userName);
    }

    static Logger log = LoggerFactory.getLogger(RaspberryPiSensorManager.class.getName());

}

/* @(#)RaspberryPiSensorManager.java */
