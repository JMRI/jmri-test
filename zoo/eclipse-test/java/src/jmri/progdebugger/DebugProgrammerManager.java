/* DebugProgrammerManager.java */

package jmri.progdebugger;

import jmri.managers.DefaultProgrammerManager;
import jmri.Programmer;
import java.util.Hashtable;

/**
 * Provides an implementation of ProgrammerManager for the
 * debug programmer. It will consistently return the same
 * ProgDebugger instance for a given request.
 * <P>
 * It uses the DefaultProgrammerManager to handle the service
 * mode operations.
 *
 * @see             jmri.ProgrammerManager
 * @author			Bob Jacobsen Copyright (C) 2002
 * @version			$Revision$
 */
public class DebugProgrammerManager extends DefaultProgrammerManager {


    public DebugProgrammerManager() {
        super(new ProgDebugger());
    }

    /**
     * Save the mapping from addresses to Programmer objects.
     * Short addresses are saved as negative numbers.
     */
    Hashtable<Integer,ProgDebugger> opsProgrammers = new Hashtable<Integer,ProgDebugger>();


    public Programmer getAddressedProgrammer(boolean pLongAddress, int pAddress) {
        int address = pAddress;
        if (!pLongAddress) address = -address;
        // look for an existing entry by getting something from hash table
        ProgDebugger saw = opsProgrammers.get(Integer.valueOf(address));
        if (saw!=null) {
            if (log.isDebugEnabled()) log.debug("return existing ops-mode programmer "
                                                +pAddress+" "+pLongAddress);
            return saw;
        }
        // if not, save a new one & return it
        opsProgrammers.put(Integer.valueOf(address), saw = new ProgDebugger());
        if (log.isDebugEnabled()) log.debug("return new ops-mode programmer "
                                                +pAddress+" "+pLongAddress);
        return saw;
    }

    public Programmer reserveAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return null;
    }

    /**
     * Debug programmer does provide Ops Mode
     * @return true
     */
    public boolean isAddressedModePossible() {return true;}

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DebugProgrammerManager.class.getName());
}


/* @(#)DefaultProgrammerManager.java */
