//AbstractSensorServer.java

package jmri.jmris;

import java.io.*;
import java.lang.*;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Sensor;

/**
 * Abstract interface between the a JMRI sensor and a 
 * network connection
 * @author          Paul Bender Copyright (C) 2010
 * @version         $Revision: 1.1 $
 */

abstract public class AbstractSensorServer implements java.beans.PropertyChangeListener {

   public AbstractSensorServer(){
   }

    /*
     * Protocol Specific Abstract Functions
     */

     abstract public void sendStatus(int Status) throws IOException; 
     abstract public void sendErrorStatus() throws IOException;
     abstract public void parseStatus(String statusString) throws jmri.JmriException,java.io.IOException;
	
    public void setSensorActive(java.lang.String Sensor) {
		// load address from switchAddrTextField
		try {
			if (sensor != null)
				sensor.removePropertyChangeListener(this);
			sensor = InstanceManager.sensorManagerInstance().provideSensor(Sensor);
			if (sensor == null) {
				log.error("Sensor " + sensor.getSystemName()
						+ " is not available");
			} else {
				sensor.addPropertyChangeListener(this);
				if (log.isDebugEnabled())
					log.debug("about to set sensor Active");
				// and set state to ACTIVE
				sensor.setKnownState(jmri.Sensor.ACTIVE);
			}
		} catch (Exception ex) {
			log.error("set sensor active, exception: "
							+ ex.toString());
		}
	}

        public void setSensorInactive(java.lang.String Sensor) {
		// load address from switchAddrTextField
		try {
			if (sensor != null)
				sensor .removePropertyChangeListener(this);
			sensor= InstanceManager.sensorManagerInstance().provideSensor(Sensor);

			if (sensor== null) {
				log.error("Sensor " + sensor.getSystemName()
						+ " is not available");
			} else {
				sensor.addPropertyChangeListener(this);
				if (log.isDebugEnabled())
					log.debug("about to set sensor INACTIVE ");
				// and set state to INACTIVE
				sensor.setKnownState(jmri.Sensor.INACTIVE);
			}
		} catch (Exception ex) {
			log.error("set sensor inactive, exception: "
							+ ex.toString());
		}
	}

    // update state as state of sensor changes
    public void propertyChange(java.beans.PropertyChangeEvent e) {
    	// If the Commanded State changes, show transition state as "<inconsistent>" 
        if (e.getPropertyName().equals("KnownState")) {
            int now = ((Integer) e.getNewValue()).intValue();
            try {
               sendStatus(now);
            } catch(java.io.IOException ie) {
                  log.error("Error Sending Status");
            }
        }
     }
    
    protected Sensor sensor = null;

    String newState = "";


    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractSensorServer.class.getName());

}
