// SampleAutomaton.java

package jmri.jmrit.automat;

import jmri.*;

/**
 * This sample Automaton watches a Sensor, and adjusts the
 * state of a Turnout so that it matches the Sensor's state.
 * <P>
 * The sensor and turnout id's are hardcoded, as this is
 * an example of just the Automaton function.  Adding a GUI
 * to configure these would be straight-forward. The values
 * could be passed via the constructor, or the constructor
 * (which can run in any required thread) could invoke
 * a dialog.
 * <P>
 * For test purposes, one of these objects can be
 * created and invoked by a SampleAutomatonAction.
 *
 * @author	Bob Jacobsen    Copyright (C) 2003
 * @version     $Revision: 1.2 $
 * @see         jmri.jmrit.automat.SampleAutomatonAction
 */
public class SampleAutomaton extends AbstractAutomaton {

    /**
     * References the turnout to be controlled
     */
    Turnout turnout;
    /**
     * References the sensor to be monitored
     */
    Sensor sensor;

    /**
     * By default, monitors sensor "31" and controls turnout "26".
     * <P>
     * This also sets the turnout to the current (initial) state
     * to make sure everything is consistent at the start.
     */
    public SampleAutomaton() {
        super();
        // get references to sample layout objects

        turnout = InstanceManager.turnoutManagerInstance().
                    newTurnout(null,"26");

        sensor = InstanceManager.sensorManagerInstance().
                    newSensor(null,"31");

        // set up the initial correlation
        now = sensor.getKnownState();
        setTurnout(now);
    }

    int now;

    /**
     * Watch "sensor", and when it changes adjust "turnout" to match.
     * @return Always returns true to continue operation
     */
    public boolean handle() {
        log.debug("Waiting for state change");

        // wait until the sensor changes state
        waitSensorChange(now, sensor);

        // get new value
        now = sensor.getKnownState();
        log.debug("Found new state: "+now);

        // match the turnout to the conditions
        setTurnout(now);

        return true;   // never terminate voluntarily
    }

    /**
     * Set "turnout" to match the sensor state
     * @param now The current value of the sensor state.
     */
    void setTurnout(int now) {
        try {
            if (now == Sensor.ACTIVE)
                turnout.setCommandedState(Turnout.THROWN);
            else
                turnout.setCommandedState(Turnout.CLOSED);
        } catch (JmriException e) {
            log.error("exception during startup:"+e);
        }
    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SampleAutomaton.class.getName());

}


/* @(#)SampleAutomaton.java */
