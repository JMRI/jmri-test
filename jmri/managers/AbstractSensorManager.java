// AbstractSensorManager.java

package jmri.managers;

import jmri.*;
import jmri.managers.AbstractManager;

/**
 * Abstract base implementation of the SensorManager interface.
 * @author			Bob Jacobsen Copyright (C) 2001, 2003
 * @version			$Revision: 1.5 $
 */
public abstract class AbstractSensorManager extends AbstractManager implements SensorManager {

    public char typeLetter() { return 'S'; }

    public Sensor provideSensor(String name) {
        Sensor t = getSensor(name);
        if (t!=null) return t;
		String sName = name.toUpperCase();
        if (isNumber(name))
            return newSensor(makeSystemName(sName), null);
        else
            return newSensor(sName, null);
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

    public Sensor getByUserName(String key) {
        return (Sensor)_tuser.get(key);
    }

    protected String normalizeSystemName(String sysName) {
        return sysName.toUpperCase();
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
        if (!systemName.startsWith(""+systemLetter()+typeLetter())) {
            log.error("Invalid system name for sensor: "+systemName
                            +" needed "+systemLetter()+typeLetter());
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

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractSensorManager.class.getName());
}

/* @(#)AbstractSensorManager.java */
