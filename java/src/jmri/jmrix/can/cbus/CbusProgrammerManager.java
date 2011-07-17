/* CbusProgrammerManager.java */

package jmri.jmrix.can.cbus;

import jmri.managers.DefaultProgrammerManager;
import jmri.Programmer;

/**
 * Extend DefaultProgrammerManager to provide node variable programmer for CBUS
 *
 * @see     jmri.ProgrammerManager
 * @author	Bob Jacobsen Copyright (C) 2008
 * @version	$Revision: 1.3 $
 */
public class CbusProgrammerManager  extends DefaultProgrammerManager {

    public CbusProgrammerManager() {
        super(null);  // no service mode programmer available
    }

    /**
     * Global not relevant for CBUS
     * @return false
     */
    public boolean isGlobalModePossible() {return false;}

    public boolean isAddressedModePossible() {return true;}

    public Programmer getAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return new CbusProgrammer(pAddress);
    }

    public Programmer reserveAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return null;
    }
}


/* @(#)CbusProgrammerManager.java */
