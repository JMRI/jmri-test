// SerialLight.java

package jmri.jmrix.cmri.serial;

import jmri.AbstractLight;
import jmri.Sensor;
import jmri.Turnout;

/**
 * SerialLight.java
 *
 * Implementation of the Light Object for C/MRI
 * <P>
 *  Based in part on SerialTurnout.java
 *
 * @author      Dave Duchamp Copyright (C) 2004
 * @version     $Revision: 1.5 $
 */
public class SerialLight extends AbstractLight {

    /**
     * Create a Light object, with only system name.
     * <P>
     * 'systemName' was previously validated in SerialLightManager
     */
    public SerialLight(String systemName) {
        super(systemName);
        // Initialize the Light
        initializeLight(systemName);
    }
    /**
     * Create a Light object, with both system and user names.
     * <P>
     * 'systemName' was previously validated in SerialLightManager
     */
    public SerialLight(String systemName, String userName) {
        super(systemName, userName);
        initializeLight(systemName);
    }
        
    /**
     * Sets up system dependent instance variables and sets system
     *    independent instance variables to default values
     * Note: most instance variables are in AbstractLight.java
     */
    private void initializeLight(String systemName) {
        // Save system name
        mSystemName = systemName;
        // Extract the Bit from the name
        mBit = SerialAddress.getBitFromSystemName(systemName);
        // Set initial state
        setState( OFF );
        // Set defaults for all other instance variables
        setControlType( NO_CONTROL );
        setControlSensor( null );
        setControlSensorSense(Sensor.ACTIVE);
        setFastClockControlSchedule( 0,0,0,0 );
        setControlTurnout( null );
        setControlTurnoutState( Turnout.CLOSED );
    }

    /**
     *  System dependent instance variables
     */
    String mSystemName = "";     // system name 
    protected int mState = OFF;  // current state of this light
    int mBit = 0;                // bit within the node

    /**
     *  Return the current state of this Light
     */
    public int getState() { return mState; }

    /**
     *  Set the current state of this Light
     *     This routine requests the hardware to change.
     *     If this is really a change in state of this 
     *         bit (tested in SerialNode), a Transmit packet
     *         will be sent before this Node is next polled.
     */
    public void setState(int newState) {
        SerialNode mNode = SerialAddress.getNodeFromSystemName(mSystemName);
        if (mNode!=null) {
            if (newState==ON) {
                mNode.setOutputBit(mBit,false);
            }
            else if (newState==OFF) {
                mNode.setOutputBit(mBit,true);
            }
            else {
                log.warn("illegal state requested for Light: "+getSystemName());
            }
        }
        mState = newState;
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialLight.class.getName());
}

/* @(#)SerialLight.java */
