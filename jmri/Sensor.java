// Sensor.java

package jmri;

/**
 * General input device representation.  Often subclassed for specific
 * types of sensors.
 *
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			$Revision: 1.3 $
 */
public interface Sensor {

	// user identification, unbound parameter
	public String getID();

	// states are parameters; both closed and thrown is possible!
	public static final int UNKNOWN      = 0x01;
	public static final int ACTIVE       = 0x02;
	public static final int INACTIVE     = 0x04;
	public static final int INCONSISTENT = 0x08;

	// known state on layout is a bound parameter -
	// always returns a answer, if need be the commanded state
	public int getKnownState();

	// request an update from the layout soft/hardware.  May not even
	// happen, and if it does it will happen later; listen for the result.
	public void requestUpdateFromLayout();

	/**
	 * Request a call-back when the bound KnownState property changes.
	 */
	public void addPropertyChangeListener(java.beans.PropertyChangeListener l);

	/**
	 * Remove a request for a call-back when a bound property changes.
	 */
	public void removePropertyChangeListener(java.beans.PropertyChangeListener l);

	/**
	 * Remove references to and from this object, so that it can
	 * eventually be garbage-collected.
	 */
	public void dispose();  // remove _all_ connections!

}


/* @(#)Sensor.java */
