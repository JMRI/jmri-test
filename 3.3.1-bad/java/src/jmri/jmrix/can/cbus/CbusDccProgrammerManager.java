/* CbusDccProgrammerManager.java */

package jmri.jmrix.can.cbus;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.managers.DefaultProgrammerManager;
import jmri.Programmer;

/**
 * Extend DefaultProgrammerManager to provide programmers for CBUS systems
 *
 * @see         jmri.ProgrammerManager
 * @author	Andrew crosland Copyright (C) 2009
 * @version	$Revision$
 */
public class CbusDccProgrammerManager  extends DefaultProgrammerManager {
    
    public CbusDccProgrammerManager(Programmer serviceModeProgrammer, CanSystemConnectionMemo memo){
        super(serviceModeProgrammer, memo);
        tc = memo.getTrafficController();
    }
    
    jmri.jmrix.can.TrafficController tc;

    /**
     * MERG CAN_CMD supports ops mode 
     *
     * @return true
     */
    public boolean isAddressedModePossible() {
        return true;
    }

    /**
     * MERG CAN_CMD supports service mode
     *
     * @return true
     */
    public boolean isGlobalProgrammerAvailable() {
        return true;
    }

    public Programmer getAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return new CbusDccOpsModeProgrammer(pAddress, pLongAddress, tc);
    }

    public Programmer reserveAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return null;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CbusDccProgrammerManager.class.getName());
}


/* @(#)CbusDccProgrammerManager.java */
