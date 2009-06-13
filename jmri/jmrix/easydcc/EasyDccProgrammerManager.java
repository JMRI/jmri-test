/* EasyDccProgrammerManager.java */

package jmri.jmrix.easydcc;

import jmri.managers.DefaultProgrammerManager;
import jmri.Programmer;

/**
 * Extend DefaultProgrammerManager to provide ops mode programmers for EasyDcc systems
 *
 * @see         jmri.ProgrammerManager
 * @author	Bob Jacobsen Copyright (C) 2002
 * @version	$Revision: 1.7 $
 */
public class EasyDccProgrammerManager  extends DefaultProgrammerManager {

    //private Programmer localProgrammer;

    public EasyDccProgrammerManager(Programmer serviceModeProgrammer) {
        super(serviceModeProgrammer);
    //    localProgrammer = serviceModeProgrammer;

    }

    /**
     * Works with command station to provide Ops Mode, so say it works
     * @return true
     */
    public boolean isAddressedModePossible() {return true;}

    public Programmer getAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return new EasyDccOpsModeProgrammer(pAddress, pLongAddress);
    }

    public Programmer reserveAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return null;
    }
}


/* @(#)EasyDccProgrammerManager.java */
