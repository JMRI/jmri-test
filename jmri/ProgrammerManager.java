/* ProgrammerManager.java */

package jmri;

/**
 * Get access to available {@link Programmer} objects.
 * <P>
 * Programmers come in two types:
 * <UL>
 * <LI>Service Mode, e.g. on a programming track
 * <LI>Ops Mode, e.g. "programming on the main"
 * </UL>
 * You get a Programmer object from a ProgrammerManager, which in turn can be located
 * from the InstanceManager.
 * <P>
 * The ProgramerManager also provides a reserve/release
 * system for tools that want to pretend they have exclusive use of a Programmer.
 * This is a cooperative reservation; both tools (first and second reserver) must
 * be using the reserve/release interface.
 * @see             jmri.Programmer
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			$Revision: 1.5 $
 */
public interface ProgrammerManager  {

    /**
     * Gain access to the Service Mode Programmer without reservation.
     * @return null only if there isn't a Service Mode Programmer in the system
     */
    public Programmer getServiceModeProgrammer();
    /**
     * Gain access to a Ops Mode Programmer without reservation.
     * @param pLongAddress true if this is a long (14 bit) address, else false
     * @param pAddress Specific decoder address to use.
     * @return null only if there isn't an Ops Mode Programmer in the system
     */
    public Programmer getOpsModeProgrammer(boolean pLongAddress, int pAddress);

    /**
     * Gain access to the Service Mode Programmer, in the process reserving it
     * for yourself.
     * @return null if the existing Service Mode programmer is in use
     */
    public Programmer reserveServiceModeProgrammer();
    /**
     * Return access to the Service Mode Programmer, so that it can
     * be used elsewhere.
     */
    public void releaseServiceModeProgrammer(Programmer p);

    /**
     * Gain access to a (the) Ops Mode Programmer, in the process
     * reserving it for yourself.
     * @param pLongAddress true if this is a long (14 bit) address, else false
     * @param pAddress Specific decoder address to use.
     * @return null if the address is in use by a reserved programmer
     */
    public Programmer reserveOpsModeProgrammer(boolean pLongAddress, int pAddress);
    /**
     * Return access to the Service Mode Programmer, so that it can
     * be used elsewhere.
     */
    public void releaseOpsModeProgrammer(Programmer p);

    /**
     * Convenience method to check whether you'll be able to get
     * an Ops Mode programmer.
     * @return false if there's no chance of getting one
     */
    public boolean isOpsModePossible();

}


/* @(#)Programmer.java */
