// Timebase.java

package jmri;

import java.util.Date;

/**
 * Provide access to clock capabilities in hardware or software.
 * <P>
 * The Rate parameter determines how much faster than real time
 * this timebase runs.  E.g. a value of 2.0 means that the value
 * returned by getTime will advance an hour for every half-hour of
 * wall-clock time elapsed.
 * <P>
 * The Rate and Run parameters are bound, so you can listen for them
 * changing.  The Time parameters is not bound, because it changes
 * continuously.  Ask for its value when needed, or add a 
 * a listener for the changes in the "minute" value using {@link #addMinuteChangeListener}
 * <P>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 * <P>
 * @author			Bob Jacobsen Copyright (C) 2004, 2007
 * @version			$Revision: 1.5 $
 */
public interface Timebase {

    // methods for setting and getting the current time
    public void setTime(Date d);
    public Date getTime();

	// methods for setting and getting run status of the fast clock
    public void setRun(boolean y);
    public boolean getRun();

	// methods for setting and getting fast clock rate
    public void setRate(double factor) throws TimebaseRateException;
    public double getRate();

	// methods for setting and getting master time source
	public void setInternalMaster(boolean master, boolean update);
	public boolean getInternalMaster();
	// the following provide for choosing among hardware clocks if hardware master		
	public void setMasterName(String name);
	public String getMasterName();
	
	// methods for setting and getting synchronize option
	public void setSynchronize(boolean synchronize, boolean update);
	public boolean getSynchronize();
	
	// methods for setting and getting hardware correction option
	public void setCorrectHardware(boolean correct, boolean update);
	public boolean getCorrectHardware();
 
	/**
	 * Methods for setting and getting 12 or 24 hour display option
	 * 'display' should be true if a 12-hour display is requested, false for 24-hour display
	 */
	public void set12HourDisplay(boolean display, boolean update);
	public boolean use12HourDisplay();

	// methods for start up with clock stopped option
	public void setStartStopped(boolean stopped);
	public boolean getStartStopped();
	
    // methods to get set time at start up option, and start up time		
	public void setStartSetTime(boolean set, Date time);
	public boolean getStartSetTime();
	public Date getStartTime();
	
    // methods to get set clock start start up option		
	public void setStartClockOption(int option);
	public int getStartClockOption();
	// Note the following method should only be invoked at start up
	public void initializeClock();
	// clock start options
	public static final int NONE			= 0x00;
	public static final int NIXIE_CLOCK     = 0x01;
	public static final int ANALOG_CLOCK	= 0x02;
	public static final int LCD_CLOCK       = 0x04;
	
	// method to initialize hardware clock at start up after all options are set up
	// Note: This method is always called at start up. It should be ignored if there
	//			is no communication with a hardware clock
	public void initializeHardwareClock();

    /**
     * Request a call-back when the bound Rate or Run property changes.
     */
    public void addPropertyChangeListener(java.beans.PropertyChangeListener l);

    /**
     * Remove a request for a call-back when a bound property changes.
     */
    public void removePropertyChangeListener(java.beans.PropertyChangeListener l);

    /**
     * Request a call-back when the minutes place of the time changes.
     */
    public void addMinuteChangeListener(java.beans.PropertyChangeListener l);

    /**
     * Remove a request for call-back when the minutes place of the time changes.
     */
    public void removeMinuteChangeListener(java.beans.PropertyChangeListener l);

    /**
     * Remove references to and from this object, so that it can
     * eventually be garbage-collected.
     */
    public void dispose();  // remove _all_ connections!


}

/* @(#)Timebase.java */
