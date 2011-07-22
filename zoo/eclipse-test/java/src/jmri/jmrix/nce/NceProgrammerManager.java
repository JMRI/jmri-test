/* NceProgrammerManager.java */

package jmri.jmrix.nce;

import jmri.managers.DefaultProgrammerManager;
import jmri.Programmer;

/**
 * Extend DefaultProgrammerManager to provide ops mode programmers for NCE systems
 *
 * @see         jmri.ProgrammerManager
 * @author	Bob Jacobsen Copyright (C) 2002
 * @version	$Revision$
 */
public class NceProgrammerManager  extends DefaultProgrammerManager {
	
	NceTrafficController tc;

    public NceProgrammerManager(NceTrafficController tc, Programmer serviceModeProgrammer) {
        super(serviceModeProgrammer);
    	this.tc = tc;
    }

    /**
     * Works with command station to provide Ops Mode, so say it works
     * @return true
     */
    public boolean isAddressedModePossible() {return true;}
    
    /**
	 * Works with PH command station to provide Service Mode and USB connect to
	 * PowerCab.
	 * 
	 * @return true if not USB connect to SB3
	 */
    public boolean isGlobalProgrammerAvailable() {
		if (tc != null && tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_SB3)
			return false;
		else
			return true;
	}


    public Programmer getAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return new NceOpsModeProgrammer(tc, pAddress, pLongAddress);
    }

    public Programmer reserveAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return null;
    }
}


/* @(#)NceProgrammerManager.java */
