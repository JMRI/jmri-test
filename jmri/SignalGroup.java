package jmri;

import jmri.util.NamedBeanHandle;

/**
 * SignalGroup.java
 *
 * The Signal Group is used to represent European subsidary signals that would be sited with a 
 * signal mast.  Such subsidary signals would be used to indicated routes, junctions and allowable
 * speeds.  Each such route/junction/speed would be represented by a single output signal, that 
 * is either off or on.  Within the group on one such signal would be allowed on at any one time.
 *
 * The group is attached to a signal mast, and can be configured to be activated depending upon that
 * masts appearance.
 * The
 * Each signal head within the group is defined with a On and Off appearance, and a set of 
 * criteria in the form of matching turnouts and sensor states, that must be met for the head to be 
 * set On.
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
 * @author			Pete Cressman Copyright (C) 2009
 * @version			$Revision 1.0 $
 */


public interface SignalGroup extends NamedBean {

    /**
     * Set enabled status.
     */
     
    public void setEnabled(boolean boo);
    
    /**
     * Get enabled status
    */
    public boolean getEnabled();
    
    /**
     *  Sets the main Signal Mast to which the Group belongs
     */
   
    public void setSignalMast(String pName);
    
     /**
     *  Get the name of the main Signal Mast
     */
    public String getSignalMastName();
    
     /**
     *  Get the SignalMast
     */
    public SignalMast getSignalMast();
    
    /**
     *  Clear the list of SignalMast Appearances that trigger the group
     */
    public void clearSignalMastAppearance();
    
     /**
     *  Add an appearance that can trigger the group activation
     */
    public void addSignalMastAppearance(String sppearance);
    
    public int getNumSignalMastAppearances();
    
    /**
     * Method to get a SignalMast Appearance by Index
     *  Returns null if there are no Appearances with that index
     */
    public String getSignalMastAppearanceByIndex(int x);
    
    /**
     * Inquire if a SignalMast Appearance is included.
     */
    public boolean isSignalMastAppearanceIncluded(String appearance);
    
    /**
    * Remove a SignalMast Appearance from the set of triggers.
    */
    public void deleteSignalMastAppearance(String appearance);
    
     /**
     * Add a Signal Head to the Group
     * @param sh The SignalHead as a Named Bean
     */
    public void addSignalHead(NamedBeanHandle<SignalHead> sh);
    
     /**
     * Add a Signal Head to the Group
     * @param pName The SignalHead as a Name
     */
    public void addSignalHead(String pName);
    
    /**
     * Method to get a SignalHead by Index
     *  Returns null if there are no Signal Heads with that index
     */
    public String getSignalHeadItemNameByIndex(int n);
    
    /**
     * Method to get the On State of a SignalState at Index n
     * <P>
     * @return -1 if there are less than 'n' SignalHeads defined
     */
    public int getSignalHeadOnStateByIndex(int n);
    
    /**
     * Method to get the Off State of a SignalState at Index n
     * <P>
     * @return -1 if there are less than 'n' SignalHeads defined
     */
    public int getSignalHeadOffStateByIndex(int n);
    
    /**
    * Delete Signal Head by Name
    */
    public void deleteSignalHead(String pName);
    
    /**
    * Delete Signal Head by NamedBean
    */    
    public void deleteSignalHead(NamedBeanHandle<SignalHead> sh);

    public int getNumSignalHeadItems();
    
    /**
     * Method to inquire if a Signal Head is included in this Group
     */
    public boolean isSignalIncluded(String systemName);
    
     /**
     * Method to get the On State of Signal Head
     * @param name The name of the SignalHead we are querying
     */
    public int getSignalHeadOnState(String name);
    
    /**
     * Method to get the Off State of Signal Head
     * @param name The name of the SignalHead we are querying
     */
    public int getSignalHeadOffState(String name);
    
    /**
     * Sets the On State of the Signal in the Group
     * @param name The SignalHead Name
     * @param state The Apperance that the SignalHead will change to 
     *      when the conditions are met.
     */
    public void setSignalHeadOnState(String name, int state);
    
    /**
     * Sets the Off State of the Signal in the Group
     * @param name The SignalHead Name
     * @param state The Apperance that the SignalHead will change to 
     *      when the conditions are NOT met.
     */    
    public void setSignalHeadOffState(String name, int state);
    
    /**
    * Sets whether the sensors and turnouts should be treated as seperate
    * calculations or as one, when determining if the signal head should be
    * on or off.
    */
    public void setSensorTurnoutOper(String pSignal, boolean boo);
    
    public boolean getSensorTurnoutOperByIndex(int x);
    
    /**
     * Method to get the number of turnouts used to determine the On state
     * for the signalhead at index x
     * <P>
     * @return -1 if there are less than 'n' SignalHeads defined
     */
    public int getNumSignalHeadTurnoutsByIndex(int x);

    /**
    * Method to add a Turnout and its state to a signal head.
    * <p>
    * @param mHead SignalHead we are adding the turnout to
    * @param mTurn Turnout as a String either User or System Name
    * @param state The State that the turnout must be set to.
    */
    public void setSignalHeadAlignTurnout(String mHead, String mTurn, int state);
    
    /**
     * Inquire if a Turnout is included in the Signal Head Calculation.
     * @param pSignal Name of the Signal Head
     * @param pTurnout Name of the Turnout
     */
    public boolean isTurnoutIncluded(String pSignal, String pTurnout);
    
    /**
    * Gets the state of the Turnout for the given Signal Head in the group
    * @param pSignal Name of the Signal Head
    * @param pTurnout Name of the Turnout within the Group
    * @return -1 if the turnout or signal head is invalid
    */
    public int getTurnoutState(String pSignal, String pTurnout);
    
    /**
    * Gets the state of the Turnout for the given Signal Head at index x
    * @param x Signal Head at index x
    * @param pTurnout Name of the Turnout within the Group
    * @return -1 if the turnout or signal head is invalid
    */
    public int getTurnoutStateByIndex(int x, String pTurnout);
    
    /**
    * Gets the state of the Turnout at index x, for the given Signal Head at index x
    * @param x Signal Head at index x
    * @param pTurnout Turnout at index pTurnout
    * @return -1 if the turnout or signal head is invalid
    */
    public int getTurnoutStateByIndex(int x, int pTurnout);
    
     /**
    * Gets the Name of the Turnout at index x, for the given Signal Head at index x
    * @param x Signal Head at index x
    * @param pTurnout Turnout at index pTurnout
    * @return null if the turnout or signal head is invalid
    */
    public String getTurnoutNameByIndex(int x, int pTurnout);
    
    /**
    * Method to add a Sensor and its state to a signal head.
    * <p>
    * @param mHead SignalHead we are adding the sensor to
    * @param mSensor Sensor as a String either User or System Name
    * @param state The State that the sensor must be set to.
    */
    public void setSignalHeadAlignSensor(String mHead, String mSensor, int state);
    
    /**
     * Inquire if a Sensor is included in the Signal Head Calculation.
     * @param pSignal Name of the Signal Head
     * @param pSensor Name of the Sensor
     */
    public boolean isSensorIncluded(String pSignal, String pSensor);
    
    /**
    * Gets the state of the Sensor for the given Signal Head in the group
    * @param pSignal Name of the Signal Head
    * @param pSensor Name of the Sensor within the Group
    * @return -1 if the sensor or signal head is invalid
    */
    public int getSensorState(String pSignal, String pSensor);
    
    /**
    * Gets the state of the Sensor for the given Signal Head at index x
    * @param x Signal Head at index x
    * @param pSensor Name of the Sensor within the Group
    * @return -1 if the sensor or signal head is invalid
    */
    public int getSensorStateByIndex(int x, int pSensor);
    
    /**
    * Gets the state of the Sensor at index x, for the given Signal Head at index x
    * @param x Signal Head at index x
    * @param pSensor Sensor at index pTurnout
    * @return null if the sensor or signal head is invalid
    */
    public String getSensorNameByIndex(int x, int pSensor);
    
    public boolean getSensorTurnoutOper(String pSignal);
    
    /**
     * Method to get the number of Sensors used to determine the On state
     * for the signalhead at index x
     * <P>
     * @return -1 if there are less than 'n' SignalHeads defined
     */
    public int getNumSignalHeadSensorsByIndex(int x);
    
    /**
     * Delete all Turnouts for a given SignalHead in the group
     * @param pSignal SignalHead Name
     */
    public void clearSignalTurnout(String pSignal);
    
     /**
     * Delete all Sensors for a given SignalHead in the group
     * @param pSignal SignalHead Name
     */
    public void clearSignalSensor(String pSignal);
    
    public int getState();
    
    public void setState(int state);
    
    static final int ONACTIVE = 0;    // route fires if sensor goes active
    static final int ONINACTIVE = 1;  // route fires if sensor goes inactive
    
    static final int ONCLOSED = 2;    // route fires if turnout goes closed
    static final int ONTHROWN = 4;  // route fires if turnout goes thrown

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SignalGroup.class.getName());
}