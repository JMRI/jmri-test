// Qualifier.java

package jmri.jmrit.symbolicprog;

/**
 * Define capability to watch other things
 * and "Qualify" CVs and Variables.
 *
 * @author			Bob Jacobsen   Copyright (C) 2014
 * @version			$Revision$
 *
 */
public interface Qualifier  {

    /**
     * Process the current value & do whatever is needed.
     */
    public void update();
    
    /**
     * Check whether this Qualifier is currently in the OK,
     * qualified-to-act state.
     *
     * @returns true if this Qualifier is currently saying OK
     */
    public boolean currentDesiredState();

}
