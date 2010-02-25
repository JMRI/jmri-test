// AbstractMemoryManager.java

package jmri.managers;

import jmri.*;
import jmri.managers.AbstractManager;

/**
 * Abstract partial implementation of a MemoryManager.
 *
 * @author			Bob Jacobsen Copyright (C) 2004
 * @version			$Revision: 1.5 $
 */
public abstract class AbstractMemoryManager extends AbstractManager
    implements MemoryManager {

    public char typeLetter() { return 'M'; }

    public Memory provideMemory(String sName) {
        Memory t = getMemory(sName);
        if (t!=null) return t;
        if (sName.startsWith(""+systemLetter()+typeLetter()))
            return newMemory(sName, null);
        else
            return newMemory(makeSystemName(sName), null);
    }

    public Memory getMemory(String name) {
        Memory t = getByUserName(name);
        if (t!=null) return t;

        return getBySystemName(name);
    }

    public Memory getBySystemName(String name) {
        return (Memory)_tsys.get(name);
    }

    public Memory getByUserName(String key) {
        return (Memory)_tuser.get(key);
    }

	public Memory newMemory(String systemName, String userName) {
        if (log.isDebugEnabled()) log.debug("new Memory:"
                                            +( (systemName==null) ? "null" : systemName)
                                            +";"+( (userName==null) ? "null" : userName));
        // return existing if there is one
        Memory s;
        if ( (userName!=null) && ((s = getByUserName(userName)) != null)) {
            if (getBySystemName(systemName)!=s)
                log.error("inconsistent user ("+userName+") and system name ("+systemName+") results; userName related to ("+s.getSystemName()+")");
            return s;
        }
        if ( (s = getBySystemName(systemName)) != null) {
			if ((s.getUserName() == null) && (userName != null))
				s.setUserName(userName);
            else if (userName != null) log.warn("Found memory via system name ("+systemName
                                    +") with non-null user name ("+userName+")");
            return s;
        }

        // doesn't exist, make a new one
        s = createNewMemory(systemName, userName);

        // if that failed, blame it on the input arguements
        if (s == null) throw new IllegalArgumentException();

        // save in the maps
        register(s);

        return s;
    }

    /**
     * Internal method to invoke the factory, after all the
     * logic for returning an existing method has been invoked.
     * @return never null
     */
    abstract protected Memory createNewMemory(String systemName, String userName);

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractMemoryManager.class.getName());
}

/* @(#)AbstractMemoryManager.java */
