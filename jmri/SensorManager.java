/** 
 * SensorManager.java
 *
 * Description:		Interface for controlling sensors
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			
 */

package jmri;


public interface SensorManager {

	// to free resources when no longer used
	public void dispose() throws JmriException;

	public Sensor newSensor(String systemName, String userName);
	
	public Sensor getByUserName(String s);
	public Sensor getBySystemName(String s);

}


/* @(#)SensorManager.java */
