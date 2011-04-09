// AbstractTurnoutManager.java

package jmri.managers;

import jmri.*;
import jmri.managers.AbstractManager;


/**
 * Abstract partial implementation of a TurnoutManager.
 *
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			$Revision: 1.16 $
 */
public abstract class AbstractTurnoutManager extends AbstractManager
    implements TurnoutManager {
	
	public AbstractTurnoutManager() {
		TurnoutOperationManager.getInstance();		// force creation of an instance
	}

    final java.util.ResourceBundle rbt = java.util.ResourceBundle.getBundle("jmri.NamedBeanBundle");

    public char typeLetter() { return 'T'; }

    public Turnout provideTurnout(String name) {
        Turnout t = getTurnout(name);
        if (t!=null) return t;
        if (name.startsWith(getSystemPrefix()+typeLetter()))
            return newTurnout(name, null);
        else
            return newTurnout(makeSystemName(name), null);
    }

    public Turnout getTurnout(String name) {
        Turnout t = getByUserName(name);
        if (t!=null) return t;

        return getBySystemName(name);
    }

    public Turnout getBySystemName(String name) {
        return (Turnout)_tsys.get(name);
    }

    public Turnout getByUserName(String key) {
        return (Turnout)_tuser.get(key);
    }

    public Turnout newTurnout(String systemName, String userName) {
        if (log.isDebugEnabled()) log.debug("newTurnout:"
                                            +( (systemName==null) ? "null" : systemName)
                                            +";"+( (userName==null) ? "null" : userName));
        if (systemName == null){
        	log.error("SystemName cannot be null. UserName was "
        			+( (userName==null) ? "null" : userName));
        	throw new IllegalArgumentException("SystemName cannot be null. UserName was "
        			+( (userName==null) ? "null" : userName));
        }
        // is system name in correct format?
        if (!systemName.startsWith(getSystemPrefix()+typeLetter())) {
            log.error("Invalid system name for turnout: "+systemName
                            +" needed "+getSystemPrefix()+typeLetter());
            throw new IllegalArgumentException("Invalid system name for turnout: "+systemName
                            +" needed "+getSystemPrefix()+typeLetter());
        }

        // return existing if there is one
        Turnout s;
        if ( (userName!=null) && ((s = getByUserName(userName)) != null)) {
            if (getBySystemName(systemName)!=s)
                log.error("inconsistent user ("+userName+") and system name ("+systemName+") results; userName related to ("+s.getSystemName()+")");
            return s;
        }
        if ( (s = getBySystemName(systemName)) != null) {
			if ((s.getUserName() == null) && (userName != null))
				s.setUserName(userName);
            else if (userName != null) log.warn("Found turnout via system name ("+systemName
                                    +") with non-null user name ("+userName+")");
            return s;
        }

        // doesn't exist, make a new one
        s = createNewTurnout(systemName, userName);

        // if that failed, blame it on the input arguements
        if (s == null) throw new IllegalArgumentException("Unable to create turnout from "+systemName);

        // save in the maps if successful
        register(s);
        try {
            s.setStraightSpeed("Global");
        } catch (jmri.JmriException ex){
            log.error(ex.toString());
        }
        
        try {
            s.setDivergingSpeed("Global");
        } catch (jmri.JmriException ex){
            log.error(ex.toString());
        }
        return s;
    }
    	
	/**
	 * Get text to be used for the Turnout.CLOSED state in user communication.
	 * Allows text other than "CLOSED" to be use with certain hardware system 
	 * to represent the Turnout.CLOSED state.
	 */
	public String getClosedText() { return rbt.getString("TurnoutStateClosed"); }
	
	/**
	 * Get text to be used for the Turnout.THROWN state in user communication.
	 * Allows text other than "THROWN" to be use with certain hardware system 
	 * to represent the Turnout.THROWN state.
	 */
	public String getThrownText() { return rbt.getString("TurnoutStateThrown"); }
	
	/**
	 * Get from the user, the number of addressed bits used to control a turnout. 
	 * Normally this is 1, and the default routine returns 1 automatically.  
	 * Turnout Managers for systems that can handle multiple control bits 
	 * should override this method with one which asks the user to specify the
	 * number of control bits.
	 * If the user specifies more than one control bit, this method should 
	 * check if the additional bits are available (not assigned to another object).
	 * If the bits are not available, this method should return 0 for number of 
	 * control bits, after informing the user of the problem.
	 */
	 public int askNumControlBits(String systemName) {return 1; }
     
     public boolean isNumControlBitsSupported(String systemName) { return false; }

	/**
	 * Get from the user, the type of output to be used bits to control a turnout. 
	 * Normally this is 0 for 'steady state' control, and the default routine 
	 * returns 0 automatically.  
	 * Turnout Managers for systems that can handle pulsed control as well as  
	 * steady state control should override this method with one which asks 
	 * the user to specify the type of control to be used.  The routine should 
	 * return 0 for 'steady state' control, or n for 'pulsed' control, where n
	 * specifies the duration of the pulse (normally in seconds).  
	 */
	 public int askControlType(String systemName) {return 0; }
     
     public boolean isControlTypeSupported(String systemName) { return false; }

    /**
     * Internal method to invoke the factory, after all the
     * logic for returning an existing method has been invoked.
     * @return never null
     */
    abstract protected Turnout createNewTurnout(String systemName, String userName);
    
    /*
     * Provide list of supported operation types.
     * <p>
     * Order is important because
     * they will be tried in the order specified.
     */
    public String[] getValidOperationTypes() {
        if (jmri.InstanceManager.commandStationInstance()!=null) {
            return new String[]{"Sensor", "Raw", "NoFeedback"};
       } else {
       	    return new String[]{"Sensor", "NoFeedback"};
       }
    }
    
    /**
    * A temporary method that determines if it is possible to add a range
    * of turnouts in numerical order eg 10 to 30
    **/
    
    public boolean allowMultipleAdditions(String systemName) { return true;  }
    
    public String getNextValidAddress(String curAddress, String prefix){
        //If the hardware address past does not already exist then this can
        //be considered the next valid address.
        Turnout t = getBySystemName(prefix+typeLetter()+curAddress);
        if(t==null){
            return curAddress;
        }
        
        // This bit deals with handling the curAddress, and how to get the next address.
        int iName = 0;
        try {
            iName = Integer.parseInt(curAddress);
        } catch (NumberFormatException ex) {
            log.error("Unable to convert " + curAddress + " Hardware Address to a number");
            jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                                showInfoMessage("Error","Unable to convert " + curAddress + " to a valid Hardware Address",""+ex, "",true, false, org.apache.log4j.Level.ERROR);
            return null;
        }
        //The Number of Output Bits of the previous turnout will help determine the next
        //valid address.
        iName = iName + t.getNumberOutputBits();
        //Check to determine if the systemName is in use, return null if it is,
        //otherwise return the next valid address.
        t = getBySystemName(prefix+typeLetter()+iName);
        if(t!=null){
            for(int x = 1; x<10; x++){
                iName = iName + t.getNumberOutputBits();
                t = getBySystemName(prefix+typeLetter()+iName);
                if(t==null)
                    return Integer.toString(iName);
            }
            return null;
        } else {
            return Integer.toString(iName);
        }
    }
    
   
    String defaultClosedSpeed = "Normal";
    String defaultThrownSpeed = "Restricted";
    
    public void setDefaultClosedSpeed(String speed) throws JmriException {
        if((speed!=null) && (defaultClosedSpeed.equals(speed)))
            return;
        try {
            Float.parseFloat(speed);
        } catch (NumberFormatException nx) {
            try{
                jmri.implementation.SignalSpeedMap.getMap().getSpeed(speed);
            } catch (Exception ex){
                throw new JmriException("Value of requested turnout default closed speed is not valid");
            }
        }
        String oldSpeed = defaultClosedSpeed;
        defaultClosedSpeed = speed;
        firePropertyChange("DefaultTurnoutClosedSpeedChange", oldSpeed, speed);
    }
    
    public void setDefaultThrownSpeed(String speed) throws JmriException{
        if((speed!=null) && (defaultThrownSpeed.equals(speed)))
            return;
        try {
            Float.parseFloat(speed);
        } catch (NumberFormatException nx) {
            try{
                jmri.implementation.SignalSpeedMap.getMap().getSpeed(speed);
            } catch (Exception ex){
                throw new JmriException("Value of requested turnout default thrown speed is not valid");
            }
        }
        String oldSpeed = defaultThrownSpeed;
        defaultThrownSpeed = speed;
        firePropertyChange("DefaultTurnoutThrownSpeedChange", oldSpeed, speed);
    }

    public String getDefaultThrownSpeed(){
        return defaultThrownSpeed;
    }
    
    public String getDefaultClosedSpeed(){
        return defaultClosedSpeed;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractTurnoutManager.class.getName());
}

/* @(#)AbstractTurnoutManager.java */
