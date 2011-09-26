/* SRCPProgrammerManager.java */

package jmri.jmrix.srcp;

import jmri.managers.DefaultProgrammerManager;
import jmri.Programmer;

/**
 * Extend DefaultProgrammerManager to provide ops mode programmers for SRCP systems
 *
 * @see         jmri.ProgrammerManager
 * @author	Bob Jacobsen Copyright (C) 2002, 2008
 * @version	$Revision$
 */
public class SRCPProgrammerManager  extends DefaultProgrammerManager {

    //private Programmer localProgrammer;

    public SRCPProgrammerManager(Programmer serviceModeProgrammer) {
        super(serviceModeProgrammer);
        //localProgrammer = serviceModeProgrammer;

    }

    /**
     * Works with command station to provide Ops Mode, so say it works
     * @return true
     */
    public boolean isAddressedModePossible() {return true;}

    public Programmer getAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return new SRCPOpsModeProgrammer(pAddress, pLongAddress);
    }

    public Programmer reserveAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return null;
    }
}


/* @(#)SRCPProgrammerManager.java */
