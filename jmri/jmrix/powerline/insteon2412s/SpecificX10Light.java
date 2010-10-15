// SpecificX10Light.java

package jmri.jmrix.powerline.insteon2412s;

import jmri.jmrix.powerline.*;

/**
 * Implementation of the Light Object for X10 receivers on Insteon 2412S interfaces.
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
 * @author      Dave Duchamp Copyright (C) 2004
 * @author      Bob Jacobsen Copyright (C) 2006, 2007, 2008, 2009, 2010
 * @author      Ken Cameron Copyright (C) 2009, 2010
 * @version     $Revision: 1.4 $
 */
public class SpecificX10Light extends jmri.jmrix.powerline.SerialX10Light {

    /**
     * Create a Light object, with only system name.
     * <P>
     * 'systemName' was previously validated in SerialLightManager
     */
    public SpecificX10Light(String systemName) {
        super(systemName);
    }
    /**
     * Create a Light object, with both system and user names.
     * <P>
     * 'systemName' was previously validated in SerialLightManager
     */
    public SpecificX10Light(String systemName, String userName) {
        super(systemName, userName);
    }

    // System-dependent instance variables

    /** 
     * Current output step 0 to maxDimStep.
     * <p>
     *  -1 means unknown
     */
    int lastOutputStep = -1;
    
    /**
     * Largest Insteon dim step number available.
     */
     int maxDimStep = 22;
     
    /**
     * Send a Dim/Bright commands to the X10 hardware 
     * to reach a specific intensity. Acts immediately, and 
     * changes no general state.
     *<p>
     * This sends "Dim" commands.  
     */
    protected void sendIntensity(double intensity) {
    	if (log.isDebugEnabled()) {
    		log.debug("sendIntensity(" + intensity + ")" + " lastOutputStep: " + lastOutputStep + " maxDimStep: " + maxDimStep);
    	}
                    
        // if we don't know the dim count, force it to a value.
        initIntensity(intensity);

        // find the new correct dim count
        int newStep = (int)Math.round(intensity * maxDimStep);  // maxDimStep is full on, 0 is full off, etc
        
        // check for errors
        if ((newStep < 0) || (newStep > maxDimStep))
            log.error("newStep wrong: " + newStep + " intensity: " + intensity);

        // find the number to send
        int sendSteps = newStep - lastOutputStep; // + for bright, - for dim
        
        // figure out the function code
        int function;
        if (sendSteps == 0) {
            // nothing to do!
            if (log.isDebugEnabled()) {
            	log.debug("intensity " + intensity + " within current step, return");
            }
            return;
        
        } else if (sendSteps > 0) {
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
        if ((sendSteps <- maxDimStep) || (sendSteps > maxDimStep))
            log.error("sendSteps wrong: " + sendSteps + " intensity: " + intensity);
            
        int deltaDim = Math.abs(sendSteps);

        lastOutputStep = newStep;
        
        // create output sequence of address, then function
        X10Sequence out = new X10Sequence();
        out.addAddress(housecode, devicecode);
        out.addFunction(housecode, function, deltaDim);
        // send
        SerialTrafficController.instance().sendX10Sequence(out, null);

    	if (log.isDebugEnabled()) {
    	    log.debug("sendIntensity(" + intensity + ") house " + X10Sequence.houseValueToText(housecode) + " device " + devicecode + " deltaDim: " + deltaDim + " funct: " + function);
        }
    }
    

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SpecificX10Light.class.getName());
}

/* @(#)SpecificX10Light.java */
