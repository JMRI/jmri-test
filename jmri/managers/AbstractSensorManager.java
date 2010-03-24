// AbstractSensorManager.java

package jmri.managers;

import jmri.*;
import jmri.managers.AbstractManager;

/**
 * Abstract base implementation of the SensorManager interface.
 * @author			Bob Jacobsen Copyright (C) 2001, 2003
 * @version			$Revision: 1.9 $
 */
public abstract class AbstractSensorManager extends AbstractManager implements SensorManager {

    public char typeLetter() { return 'S'; }

    public Sensor provideSensor(String name) {
        Sensor t = getSensor(name);
        if (t!=null) return t;
        if (isNumber(name))
            return newSensor(makeSystemName(name), null);
        else
            return newSensor(name, null);
    }

    public Sensor getSensor(String name) {
        Sensor t = getByUserName(name);
        if (t!=null) return t;
	
        return getBySystemName(name);
    }

    static final java.util.regex.Matcher numberMatcher = java.util.regex.Pattern.compile("\\d++").matcher("");
    boolean isNumber(String s) {
        return numberMatcher.reset(s).matches();
    }
    
    public Sensor getBySystemName(String key) {
        if (isNumber(key))
            key = makeSystemName(key);
		String name = normalizeSystemName(key);
        return (Sensor)_tsys.get(name);
    }

    protected Object getInstanceBySystemName(String systemName) {
        return getBySystemName(systemName);
    }

    public Sensor getByUserName(String key) {
        return (Sensor)_tuser.get(key);
    }

    protected String normalizeSystemName(String sysName) {
        return sysName;
    }
    
    public Sensor newSensor(String sysName, String userName) {
		String systemName = normalizeSystemName(sysName);
        if (log.isDebugEnabled()) log.debug("newSensor:"
                                            +( (systemName==null) ? "null" : systemName)
                                            +";"+( (userName==null) ? "null" : userName));
        if (systemName == null){ 
        	log.error("SystemName cannot be null. UserName was "
        			+( (userName==null) ? "null" : userName));
        	return null;
        }
        // is system name in correct format?
        if (!systemName.startsWith(getSystemPrefix()+typeLetter())) {
            log.error("Invalid system name for sensor: "+systemName
                            +" needed "+getSystemPrefix()+typeLetter());
            return null;
        }

        // return existing if there is one
        Sensor s;
        if ( (userName!=null) && ((s = getByUserName(userName)) != null)) {
            if (getBySystemName(systemName)!=s)
                log.error("inconsistent user ("+userName+") and system name ("+systemName+") results; userName related to ("+s.getSystemName()+")");
            return s;
        }
        if ( (s = getBySystemName(systemName)) != null) {
			if ((s.getUserName() == null) && (userName != null))
				s.setUserName(userName);
            else if (userName != null) log.warn("Found sensor via system name ("+systemName
                                    +") with non-null user name ("+userName+")");
            return s;
        }

        // doesn't exist, make a new one
        s = createNewSensor(systemName, userName);

        // if that failed, blame it on the input arguements
        if (s == null) throw new IllegalArgumentException();

        // save in the maps
        register(s);

        return s;
    }

    /**
     * Internal method to invoke the factory, after all the
     * logic for returning an existing method has been invoked.
     * @return new null
     */
    abstract protected Sensor createNewSensor(String systemName, String userName);
	
    /**
     * Requests status of all layout sensors under this Sensor Manager.
	 * This method may be invoked whenever the status of sensors needs to be updated from
	 *		the layout, for example, when an XML configuration file is read in.
	 * Note that this null implementation only needs be implemented in system-specific 
	 *		Sensor Managers where readout of sensor status from the layout is possible.
	 */
	public void updateAll() { }
    
   /**
    * A method that determines if it is possible to add a range of sensors in numerical
    * order eg 10 to 30, primarily used to enable/disable the add range box in the add sensor panel
    **/
    
    public boolean allowMultipleAdditions(String systemName) { return false;  }

    public String getNextValidAddress(String curAddress, String prefix){
        //If the hardware address past does not already exist then this can
        //be considered the next valid address.
        Sensor s = getBySystemName(prefix+typeLetter()+curAddress);
        if(s==null){
            return curAddress;
        }
        
        // This bit deals with handling the curAddress, and how to get the next address.
        int iName = 0;
        try {
            iName = Integer.parseInt(curAddress);
        } catch (NumberFormatException ex) {
            log.error("Unable to convert " + curAddress + " Hardware Address to a number");
            jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                                showInfoMessage("Error","Unable to convert " + curAddress + " to a valid Hardware Address",""+ex,true, false, org.apache.log4j.Level.ERROR);
            return null;
        }
        
        //Check to determine if the systemName is in use, return null if it is,
        //otherwise return the next valid address.
        s = getBySystemName(prefix+typeLetter()+iName);
        if(s!=null){
            for(int x = 1; x<10; x++){
                iName++;
                s = getBySystemName(prefix+typeLetter()+iName);
                if(s==null)
                    return Integer.toString(iName);
            }
            return null;
        } else {
            return Integer.toString(iName);
        }
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractSensorManager.class.getName());
}

/* @(#)AbstractSensorManager.java */
