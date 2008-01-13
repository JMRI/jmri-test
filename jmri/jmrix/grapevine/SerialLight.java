// SerialLight.java

package jmri.jmrix.grapevine;

import jmri.AbstractLight;
import jmri.Sensor;
import jmri.Turnout;

/**
 * SerialLight.java
 *
 * Implementation of the Light Object
 * <P>
 *  Based in part on SerialTurnout.java
 *
 * @author      Dave Duchamp Copyright (C) 2004
 * @author      Bob Jacobsen Copyright (C) 2006, 2007
 * @version     $Revision: 1.2 $
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
                sendMessage(false);
            }
            else if (newState==OFF) {
                sendMessage(true);
            }
            else {
                log.warn("illegal state requested for Light: "+getSystemName());
            }
        }
		if (newState!=mState) {
			int oldState = mState;
			mState = newState;
            // notify listeners, if any
            firePropertyChange("KnownState", new Integer(oldState), new Integer(newState));
		}
    }

    protected void sendMessage(boolean closed) {
        SerialNode tNode = SerialAddress.getNodeFromSystemName(getSystemName());
        if (tNode == null) {
            // node does not exist, ignore call
            log.error("Can't find node for "+getSystemName()+", command ignored");
            return;
        }
        int output = (mBit-1) % 24; /// 0 to 23 range for individual bank
        boolean high = (output>=12);
        if (high) output = output-12;
        int bank = (mBit-1)/24;  
        if ( (bank<0)||(bank>4) ) {
            log.error("invalid bank "+bank+" for Turnout "+getSystemName());
            bank = 0;
        }
        SerialMessage m = new SerialMessage(high?8:4);
        int i = 0;
        if (high) {
            m.setElement(i++,tNode.getNodeAddress()|0x80);  // address 1
            m.setElement(i++,122);   // shift command
            m.setElement(i++,tNode.getNodeAddress()|0x80);  // address 2
            m.setElement(i++,0x10);  // bank 1
            m.setParity(i-4);
        }
        m.setElement(i++,tNode.getNodeAddress()|0x80);  // address 1
        m.setElement(i++, (output<<3)|(closed ? 0 : 6));  // closed is green, thrown is red
        m.setElement(i++,tNode.getNodeAddress()|0x80);  // address 2
        m.setElement(i++, bank<<4); // bank is most significant bits
        m.setParity(i-4);
        SerialTrafficController.instance().sendSerialMessage(m, null);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialLight.class.getName());
}

/* @(#)SerialLight.java */
