// NamedBean.java

package jmri;

/**
 * Provides common services for classes representing objects
 * on the layout, and allows a common form of access by their Managers.
 * <P>
 * Each object has a two names.  The "user" name is entirely free form, and
 * can be used for any purpose.  The "system" name is provided by the system-specific
 * implementations, and provides a unique mapping to the layout control system
 * (e.g. LocoNet, NCE, etc) and address within that system.
 *
 * @author	Bob Jacobsen  Copyright (C) 2001, 2002, 2003, 2004
 * @version	$Revision: 1.6 $
 * @see         jmri.AbstractManager
 * @see         jmri.AbstractNamedBean
 */
public interface NamedBean {

    /**
     * Constant representing an "unknown" state, indicating that the
     * object's state is not necessarily that of the actual layout hardware.
     * This is the initial state of a newly created object before
     * communication with the layout.
     */
    public static final int UNKNOWN      = 0x01;

    /**
     * Constant representing an "inconsistent" state, indicating that
     * some inconsistency has been detected in the hardware readback.
     */
    public static final int INCONSISTENT = 0x08;

    // user identification, _bound_ parameter so manager(s) can listen
    public String getUserName();
    public void setUserName(String s);

    /**
     * Get a system-specific name.  This encodes the hardware addressing
     * information.
     */
    public String getSystemName();

    /**
     * Request a call-back when a bound property changes.
     * Bound properties are the known state, commanded state, user and system names.
     */
    public void addPropertyChangeListener(java.beans.PropertyChangeListener l);

    /**
     * Remove a request for a call-back when a bound property changes.
     */
    public void removePropertyChangeListener(java.beans.PropertyChangeListener l);

    /**
     * Number of current listeners. May return -1 if the 
     * information is not available for some reason.
     */
    public int getNumPropertyChangeListeners();

    /**
     * Remove references to and from this object, so that it can
     * eventually be garbage-collected.
     */
    public void dispose();  // remove _all_ connections!
   
    /**
     * Provide generic access to internal state.
     *<P>
     * This generally shouldn't be used by Java code; use 
     * the class-specific form instead. (E.g. setCommandedState in Turnout)
     * This provided to make Jython
     * script access easier to read.  
     * @throws JmriException general error when cant do the needed operation
     */
    public void setState(int s) throws JmriException;
    
    /**
     * Provide generic access to internal state.
     *<P>
     * This generally shouldn't be used by Java code; use 
     * the class-specific form instead. (E.g. getCommandedState in Turnout)
     * This provided to make Jython
     * script access easier to read.  
     */
    public int getState();


}

/* @(#)NamedBean.java */
