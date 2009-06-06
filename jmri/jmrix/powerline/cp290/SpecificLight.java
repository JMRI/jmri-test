// SpecificLight.java

package jmri.jmrix.powerline.cp290;

import jmri.jmrix.powerline.*;

/**
 * Implementation of the Light Object for X10 for CP290 interfaces.
 * <P>
 * Uses X10 dimming commands to set intensity unless
 * the value is 0.0 or 1.0, in which case it uses on/off commands only.
 * <p>
 * Since the dim/bright step of the hardware is unknown then the Light
 * object is first created, the first time the intensity (not state)
 * is set to other than 0.0 or 1.0, 
 * the output is run to it's maximum dim or bright step so
 * that we know the count is right.
 * <p>
 * Keeps track of the controller's "dim count", and if 
 * not certain forces it to zero to be sure.
 * <p>
 * 
 *
 *
 * @author      Dave Duchamp Copyright (C) 2004
 * @author      Bob Jacobsen Copyright (C) 2006, 2007, 2008
 * @version     $Revision: 1.6 $
 */
public class SpecificLight extends jmri.jmrix.powerline.SerialLight {

    /**
     * Create a Light object, with only system name.
     * <P>
     * 'systemName' was previously validated in SerialLightManager
     */
    public SpecificLight(String systemName) {
        super(systemName);
        maxDimStep = SerialTrafficController.instance().maxX10DimStep();
    }
    /**
     * Create a Light object, with both system and user names.
     * <P>
     * 'systemName' was previously validated in SerialLightManager
     */
    public SpecificLight(String systemName, String userName) {
        super(systemName, userName);
        maxDimStep = SerialTrafficController.instance().maxX10DimStep();
    }
        
    /**
     * Optionally, force control to a known "dim count".
     * <p>
     * Invoked the first time intensity is set.
     */
    protected void initIntensity(double intensity) {
        maxDimStep = SerialTrafficController.instance().maxX10DimStep();

        // Set initial state
            
        // see if going to stabilize at on or off
        if (intensity<= 0.5) {
            // create output sequence
            X10Sequence out = new X10Sequence();
            // going to low, first set off
            out.addAddress(housecode, devicecode);
            out.addFunction(housecode, X10Sequence.FUNCTION_OFF, 0);
            // then set to full dim
            out.addFunction(housecode, X10Sequence.FUNCTION_DIM, maxDimStep);
            // send
            SerialTrafficController.instance().sendX10Sequence(out, null);

            lastOutputStep = 0;
            
            if (log.isDebugEnabled()) {
            	log.debug("initIntensity: sent dim reset");
            }
        } else {
            // create output sequence
            X10Sequence out = new X10Sequence();
            // going to high, first set on
            out.addAddress(housecode, devicecode);
            out.addFunction(housecode, X10Sequence.FUNCTION_ON, 0);
            // then set to full dim
            out.addFunction(housecode, X10Sequence.FUNCTION_BRIGHT, maxDimStep);
            // send
            SerialTrafficController.instance().sendX10Sequence(out, null);
            
            lastOutputStep = maxDimStep;
            
            if (log.isDebugEnabled()) {
            	log.debug("initIntensity: sent bright reset");
            }
        }
    }
    
    // System-dependent instance variables

    /** 
     * Current output step 0 to maxDimStep.
     * <p>
     *  -1 means unknown
     */
    int lastOutputStep = -1;
    
    /**
     * Largest X10 dim step number available.
     * <p>
     * Loaded from SerialTrafficController.maxX10DimStep();
     */
     int maxDimStep = 0;
   
    /**
     * Send a Dim/Bright commands to the X10 hardware 
     * to reach a specific intensity.
     */
    protected void sendIntensity(double intensity) {
        
    	if (log.isDebugEnabled()) {
    		log.debug("sendIntensity(" + intensity + ")");
    	}
                    
        // if we don't know the dim count, force it to a value.
        if (lastOutputStep < 0) initIntensity(intensity);

        // find the new correct dim count
        int newStep = (int)Math.round(intensity*maxDimStep);  // maxDimStep is full on, 0 is full off, etc
        
        // check for errors
        if (newStep <0 || newStep>maxDimStep)
            log.error("newStep wrong: "+newStep+" intensity: "+intensity);

        // find the number to send
        int sendSteps = newStep-lastOutputStep; // + for bright, - for dim
        
        // figure out the function code
        int function;
        if (sendSteps == 0) {
            // nothing to do!
            if (log.isDebugEnabled()) {
            	log.debug("intensity "+intensity+" within current step, return");
            }
            return;
        
        } else if (sendSteps >0) {
            function = X10Sequence.FUNCTION_BRIGHT;
        	if (log.isDebugEnabled()) {
        		log.debug("function bright");
        	}
        }
        else {
            function = X10Sequence.FUNCTION_DIM;
        	if (log.isDebugEnabled()) {
        		log.debug("function dim");
        	}
        }

        // check for errors
        if (sendSteps <-maxDimStep || sendSteps>maxDimStep)
            log.error("sendSteps wrong: "+sendSteps+" intensity: "+intensity);
            
        int deltaDim = Math.abs(sendSteps);

        lastOutputStep = newStep;
        
        // create output sequence of address, then function
        X10Sequence out = new X10Sequence();
        out.addAddress(housecode, devicecode);
        out.addFunction(housecode, function, deltaDim);
        // send
        SerialTrafficController.instance().sendX10Sequence(out, null);

    	if (log.isDebugEnabled()) {
    		log.debug("sendIntensity(" + intensity + ") house " + housecode + " device " + devicecode + " deltaDim: " + deltaDim + " funct: " + function);
        }
    }

    /**
     *  Send a On/Off Command to the hardware
     */
    protected void sendOnOffCommand(int newState) {
    	if (log.isDebugEnabled()) {
    		log.debug("sendOnOff(" + newState + ") Current: " + mState);
    	}

        // figure out command 
        int function;
        double newDim;
        if (newState == ON) {
        	function = X10Sequence.FUNCTION_ON;
        	newDim = 1;
        }
        else if (newState==OFF) {
        	function = X10Sequence.FUNCTION_OFF;
        	newDim = 0;
        }
        else {
            log.warn("illegal state requested for Light: "+getSystemName());
            return;
        }

        log.debug("set state "+newState+" house "+housecode+" device "+devicecode);

        // create output sequence of address, then function
        X10Sequence out = new X10Sequence();
        out.addAddress(housecode, devicecode);
        out.addFunction(housecode, function, 0);
        // send
        SerialTrafficController.instance().sendX10Sequence(out, null);
        
    	if (log.isDebugEnabled()) {
    		log.debug("sendOnOff(" + newDim + ")  house " + housecode + " device " + devicecode + " funct: " + function);
        }
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SpecificLight.class.getName());
}

/* @(#)SpecificLight.java */
