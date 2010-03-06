// InternalSensorManager.java

package jmri.managers;

import jmri.managers.AbstractSensorManager;
import jmri.implementation.AbstractSensor;
import jmri.Sensor;

/**
 * Implementation of the InternalSensorManager interface.
 * @author			Bob Jacobsen Copyright (C) 2001, 2003, 2006
 * @version			$Revision: 1.4 $
 */
public class InternalSensorManager extends AbstractSensorManager {

    /**
     * Create an internal (dummy) sensor object
     * @return new null
     */
    protected Sensor createNewSensor(String systemName, String userName) {
        return new AbstractSensor(systemName, userName){
            public void requestUpdateFromLayout(){}
        };
    }
	
	public String getSystemPrefix() { return "I"; }
	
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(InternalSensorManager.class.getName());
}

/* @(#)InternalSensorManager.java */
