// AbstractReporterManager.java

package jmri.managers;

import jmri.*;
import jmri.implementation.AbstractManager;

/**
 * Abstract partial implementation of a ReporterManager.
 *
 * @author			Bob Jacobsen Copyright (C) 2004
 * @version			$Revision: 1.1 $
 */
public abstract class AbstractReporterManager extends AbstractManager
    implements ReporterManager {

    public char typeLetter() { return 'R'; }

    public Reporter provideReporter(String name) {
        Reporter t = getReporter(name);
        if (t!=null) return t;
		String sName = name.toUpperCase();
        if (sName.startsWith(""+systemLetter()+typeLetter()))
            return newReporter(sName, null);
        else
            return newReporter(makeSystemName(sName), null);
    }

    public Reporter getReporter(String name) {
        Reporter t = getByUserName(name);
        if (t!=null) return t;

        return getBySystemName(name);
    }

    public Reporter getBySystemName(String key) {
		String name = key.toUpperCase();
        return (Reporter)_tsys.get(name);
    }

    public Reporter getByUserName(String key) {
        return (Reporter)_tuser.get(key);
    }

    public Reporter newReporter(String sysName, String userName) {
		String systemName = sysName.toUpperCase();
        if (log.isDebugEnabled()) log.debug("new Reporter:"
                                            +( (systemName==null) ? "null" : systemName)
                                            +";"+( (userName==null) ? "null" : userName));
        if (systemName == null) log.error("SystemName cannot be null. UserName was "
                                        +( (userName==null) ? "null" : userName));
        // is system name in correct format?
        if (!systemName.startsWith(""+systemLetter()+typeLetter())) {
            log.error("Invalid system name for Reporter: "+systemName
                            +" needed "+systemLetter()+typeLetter());
            return null;
        }

        // return existing if there is one
        Reporter s;
        if ( (userName!=null) && ((s = getByUserName(userName)) != null)) {
            if (getBySystemName(systemName)!=s)
                log.error("inconsistent user ("+userName+") and system name ("+systemName+") results; userName related to ("+s.getSystemName()+")");
            return s;
        }
        if ( (s = getBySystemName(systemName)) != null) {
			if ((s.getUserName() == null) && (userName != null))
				s.setUserName(userName);
            else if (userName != null) log.warn("Found reporter via system name ("+systemName
                                    +") with non-null user name ("+userName+")");
            return s;
        }

        // doesn't exist, make a new one
        s = createNewReporter(systemName, userName);

        // save in the maps
        register(s);

        return s;
    }

    /**
     * Internal method to invoke the factory, after all the
     * logic for returning an existing method has been invoked.
     * @return never null
     */
    abstract protected Reporter createNewReporter(String systemName, String userName);

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractReporterManager.class.getName());
}

/* @(#)AbstractReporterManager.java */
