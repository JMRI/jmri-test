// Logix.java

package jmri;

/**
 * A Logix is a group of Conditionals that monitor one or more conditions 
 * (internal or on the layout) and take action when these conditionals 
 * change in a user specified way.
 * <P>
 * A Logix can be enabled or not.  By default it is enabled and will act
 * when one or more of its Conditionals changes in a user specified way.
 * When not enabled, the Logix will not trigger actions when its 
 * Conditionals change.  Enabled is a bound property of a Logix.
 * <P>
 * A Logix does not have a "state", however, each of its Conditionals does.
 *
 *
 * <hr>
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
 * @author			Dave Duchamp Copyright (C) 2007
 * @version			$Revision 1.0 $
 */
 
public interface Logix extends NamedBean {

	public static final int MAX_CONDITIONALS = 50;
	public static final int MAX_LISTENERS = 250;
	
	public static final int LISTENER_TYPE_SENSOR = 1;
	public static final int LISTENER_TYPE_TURNOUT = 2;
	public static final int LISTENER_TYPE_LIGHT = 3;
	public static final int LISTENER_TYPE_CONDITIONAL = 4;
	public static final int LISTENER_TYPE_SIGNAL = 5;
	public static final int LISTENER_TYPE_MEMORY = 6;	

    /**
     * Set enabled status.  Enabled is a bound property
	 *   All conditionals are set to UNKNOWN state and recalculated
	 *		when the Logix is enabled, provided the Logix has been 
	 *		previously activated.
     */
    public void setEnabled(boolean state);

    /**
     * Get enabled status
    */
    public boolean getEnabled();
	
	/**
	 * Get number of Conditionals for this Logix
	 */
	public int getNumConditionals();

	/**
	 * Initialize for reordering Conditionals
	 */
	public void initializeReorder();
	
	/**
	 * Make the Conditional with given current order, the next in order
	 * Returns 'true' if reordering is done, returns 'false' if continue
	 */
	public boolean nextConditionalInOrder(int oldOrder);
	
	/**
	 * Get never used number for next Conditional system name
	 */
	public int getNextConditionalNumber();
		
	/**
	 * Returns the system name of the conditional that will calculate in the
	 * specified order. This is also the order the Conditional is listed in
	 * the Add/Edit Logix dialog.
	 * If 'order' is greater than the number of Conditionals for this Logix,
	 * and empty String is returned.
	 * @param order - order in which the Conditional calculates.
	 */	
	public String getConditionalByNumberOrder(int order);
	
	/**
     * Add a Conditional to this Logix
	 * Returns true if Conditional was successfully added, returns false
	 * if the maximum number of conditionals has been exceeded.
     * @param systemName The Conditional system name
	 * @param order - the order this conditional should calculate in
	 *                 if order is negative, the conditional is added
	 *				   at the end of current group of conditionals
     */
    public boolean addConditional(String systemName,int order);
	
	/**
     * Delete a Conditional from this Logix
     * <P>
	 * Note: Since each Logix must have at least one Conditional,
	 *    the last Conditional will not be deleted.
     * <P>
	 * Returns true if Conditional was successfully deleted, otherwise 
	 *    returns false. 
     * @param systemName The Conditional system name
     */
    public boolean deleteConditional(String systemName);	
	
    /**
	 * Calculate all Conditionals, triggering action if the user specified
	 *   conditions are met, and the Logix is enabled.
	 */
	public void calculateConditionals();
	
    /**
     * Activate the Logix, starts Logix processing by connecting all
	 *    inputs that are included the Conditionals in this Logix.
     * <P>
     * A Logix must be activated before it will calculate any of its
	 *    Conditionals.
     */
    public void activateLogix();

    /**
     * Deactivate the Logix. This method disconnects the Logix from
     *    all input objects and stops it from being triggered to calculate.
     * <P>
     * A Logix must be deactivated before it's Conditionals are
	 *   changed.
     */
    public void deActivateLogix();
	
	/** 
	 * Assembles a list of state variables that both trigger the Logix, and are
	 *   changed by it.  Returns true if any such variables were found.  Returns false
	 *   otherwise.
	 */
	public boolean checkLoopCondition();
	 
	/** 
	 * Assembles a string listing state variables that might result in a loop.
	 *    Returns an empty string if there are none, probably because 
	 *    "checkLoopCondition" was not invoked before the call, or returned false.
	 */
	public String getLoopGremlins();
	
	/**
	 * Assembles and returns a list of state variables that are used by conditionals 
	 *   of this Logix including the number of occurances of each variable that 
	 *   trigger a calculation, and the number of occurances where the triggering 
	 *   has been suppressed.
	 * The main use of this method is to return information that can be used to test 
	 *   for inconsistency in suppressing triggering of a calculation among multiple 
	 *   occurances of the same state variable.
	 * Note that FastClockRange state varible type is not returned, since each 
	 *   occurance is considered a unique state variable - there is no duplication 
	 *   possible.
	 * Returns the number of state variables returned.
	 * Note that 'arrayMax' is the dimension of the arrays passed in the call.  If 
	 *   more state variables are found than 'arrayMax', the overflow is skipped.
	 */
	public int getStateVariableList(String[] varName, int[] varListenerType, 
			String[] varListenerProperty, int[] varAppearance, int[] numTriggersCalc, 
								int[] numTriggerSuppressed, int arrayMax);  
}

/* @(#)Logix.java */



 