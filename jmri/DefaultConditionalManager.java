// DefaultConditionalManager.java

package jmri;

import java.util.List;

/**
 * Basic Implementation of a ConditionalManager.
 * <P>
 * Note that Conditionals always have an associated parent Logix.
 * <P>
 * Logix system names must begin with IX, and be followed by a 
 * string, usually, but not always, a number. The system names of Conditionals 
 * always begin with the parent Logix's system name, then there is a capital C 
 * and a number.
 *<P>  
 * Conditional system names are set automatically when the Conditional is created.
 * All alphabetic characters in a Conditional system name must be upper case. This 
 * is enforced when a new Conditional is created via LogixTableAction.java.
 *
 * @author      Dave Duchamp Copyright (C) 2007
 * @version	$Revision: 1.4 $
 */
public class DefaultConditionalManager extends AbstractManager
    implements ConditionalManager, java.beans.PropertyChangeListener {

    public DefaultConditionalManager() {
        super();
    }

    public char systemLetter() { return 'I'; }
    public char typeLetter() { return 'X'; }
    
    /**
     * Method to create a new Conditional if the Conditional does not exist
     *   Returns null if a Conditional with the same systemName or userName
     *       already exists, or if there is trouble creating a new Conditional
	 *   If the parent Logix cannot be found, the userName cannot be checked, but
	 *		the Conditional is still created. The scenario can happen when a Logix
	 *      is loaded from a file after its Conditionals.
     */
    public Conditional createNewConditional(String systemName, String userName) {
		// Check that the parent Logix exists, if not can't check user Name
		String sName = systemName.toUpperCase().trim();
		Logix x = getParentLogix(sName);
        Conditional c;
		if (x!=null) {
			// Check that Conditional with same user name does not already exist for this Logix
			if (userName!= null && !userName.equals("")) {
				c = getByUserName(x,userName);
				if (c!=null) return null;
			}
		}
		// check that Conditional with same system name does not already exist
        c = getBySystemName(systemName);
		if (c==null) getBySystemName(sName);
        if (c!=null) return null;
        // Conditional does not exist, create a new Conditional
        c = new DefaultConditional(sName,userName);
        if (c!=null) {
            // save in the maps
            register(c);
        }
        return c;
    }
	
	/** 
	 * Parses the Conditional system name to get the parent Logix system name, then
	 *    gets the parent Logix, and returns it.
	 * @param name - system name of Conditional (must be trimmed and upper case)
	 */
	public Logix getParentLogix(String name) {
		if (name.length()<4) return null;
		for (int i = name.length()-1;i>2;i--) {
			if (name.charAt(i) == 'C') {
				return InstanceManager.logixManagerInstance().getBySystemName(
													name.substring(0,i));
			}
		}
		return null;
	}

    /**
     * Remove an existing Conditional. Parent Logix must have been deactivated
     * before invoking this.
     */
    public void deleteConditional(Conditional c) {
        deregister(c);
    }

    /** 
     * Method to get an existing Conditional.  
	 *	First looks up assuming that name is a User Name. Note: the parent Logix
	 *		must be passed in x for user name lookup.
	 *	If this fails, or if x == null, looks up assuming
     *      that name is a System Name.  If both fail, returns null.
	 * @param x - parent Logix (may be null)
     * @param name - name to look up
     * @return null if no match found
     */
    public Conditional getConditional(Logix x,String name) {
		Conditional c = null;
		if (x != null) {
			c = getByUserName(x,name);
			if (c!=null) return c;
		}
        return getBySystemName(name);
    }

    public Conditional getByUserName(Logix x,String key) {
		if (x == null) return null;
		for (int i = 0;i<x.getNumConditionals();i++) {
			Conditional c = getBySystemName(x.getConditionalByNumberOrder(i));
			if (c!=null) {
				String uName = c.getUserName();
				if (key.equals(uName)) return c;
			}
		}	
        return null;
    }

    public Conditional getBySystemName(String name) {
		String key = name.toUpperCase();
        return (Conditional)_tsys.get(key);
    }

	/**
	 * Get a list of all Conditional system names with the specified Logix parent
	 */
	public List getSystemNameListForLogix(Logix x) {
		log.error("getSystemNameListForLogix - Not implemented yet.");
		return null;
	}
    
    static DefaultConditionalManager _instance = null;
    static public DefaultConditionalManager instance() {
        if (_instance == null) {
            _instance = new DefaultConditionalManager();
        }
        return (_instance);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DefaultConditionalManager.class.getName());
}



/* @(#)DefaultConditionalManager.java */
