// ActiveFlag.java

package jmri.jmrix.sprog;

/**
 * Provide a flag to indicate that the system provided by
 * this package is active.
 * <P>
 * This is a very light-weight class, carrying only the flag,
 * so as to limit the number of unneeded class loading.
 *
 * @author		Andrew Crosland  Copyright (C) 2006
 * @version             $Revision: 1.2 $
 */
abstract public class ActiveFlagCS {

    static private boolean flag = false;
    static public void setActive() {
        flag = true;
    }
    static public boolean isActive() {
        return flag;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ActiveFlagCS.class.getName());

}


/* @(#)ActiveFlagCS.java */
