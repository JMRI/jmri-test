/* DefaultProgrammerManager.java */

package jmri;

/**
 * Provides a very-basic implementation of ProgrammerManager.  You give it
 * a service-mode Programmer at construction time; Ops Mode requests
 * get a null in response.
 *
 * @see             jmri.ProgrammerManager
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			$Revision: 1.6 $
 */
public class DefaultProgrammerManager implements ProgrammerManager {

    private Programmer mProgrammer;

    public DefaultProgrammerManager(Programmer pProgrammer) {
        mProgrammer = pProgrammer;
    }


    public Programmer getServiceModeProgrammer() {
        if (log.isDebugEnabled()) log.debug("return default service-mode programmer");
        return mProgrammer;
    }
    public Programmer getOpsModeProgrammer(boolean pLongAddress, int pAddress) {
        return null;
    }

    public Programmer reserveServiceModeProgrammer() {
        return mProgrammer;
    }
    public void releaseServiceModeProgrammer(Programmer p) {}

    public Programmer reserveOpsModeProgrammer(boolean pLongAddress, int pAddress) {
        return null;
    };
    public void releaseOpsModeProgrammer(Programmer p) {}

    /**
     * Default programmer does not provide Ops Mode
     * @return false if there's no chance of getting one
     */
    public boolean isOpsModePossible() {return false;}

    /**
     * Allow for implementations that do not support Service mode programming
     * @return false if there's no chance of getting one
     */
    public boolean isServiceModePossible() {return true;}

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DefaultProgrammerManager.class.getName());
}
/* @(#)DefaultProgrammerManager.java */
