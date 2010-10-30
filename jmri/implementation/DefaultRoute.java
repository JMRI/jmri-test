// DefaultRoute.java

package jmri.implementation;

 /**
 * Class providing the basic logic of the Route interface.
 *
 * @author	Dave Duchamp Copyright (C) 2004
 * @author Bob Jacobsen Copyright (C) 2006, 2007
 * @author Simon Reader Copyright (C) 2008
 * @author Pete Cressman Copyright (C) 2009
 *
 * @version     $Revision: 1.8 $
 */
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import jmri.*;

public class DefaultRoute extends AbstractNamedBean
    implements Route, java.io.Serializable {

    public DefaultRoute(String systemName, String userName) {
        super(systemName.toUpperCase(), userName);
    }

    public DefaultRoute(String systemName) {
        super(systemName.toUpperCase());
    }

    /**
     *  Persistant instance variables (saved between runs)
     */
    protected String mControlTurnout = "";
    protected int mControlTurnoutState = jmri.Turnout.THROWN;
	protected int mDelay = 0;
	
    protected String mLockControlTurnout = "";
    protected int mLockControlTurnoutState = jmri.Turnout.THROWN;

    protected String mTurnoutsAlignedSensor = "";
    
    protected String soundFilename;
    protected String scriptFilename;
    
    private static final long serialVersionUID = 1L;
    
    /**
     *  Operational instance variables (not saved between runs)
     */
    ArrayList <OutputSensor> _outputSensorList = new ArrayList<OutputSensor>();
    private class OutputSensor {
        Sensor _sensor;
        int _state = Sensor.ACTIVE;
        OutputSensor(String name) {
            _sensor = InstanceManager.sensorManagerInstance().provideSensor(name);
        }

        String getName() {
            if (_sensor != null)
            {
                return _sensor.getSystemName();
            }
            return null;
        }
        boolean setState(int state) {
            if (_sensor == null) {
                return false;
            }
            if ((state!=Sensor.ACTIVE) && (state!=Sensor.INACTIVE) && (state!=Route.TOGGLE)) {
                log.warn("Illegal Sensor state for Route: "+getName() );
                return false;
            }        
            _state = state;
            return true;
        }
        int getState() {
            return _state;
        }
        Sensor getSensor() {
            return _sensor;
        }
    }

    ArrayList <ControlSensor> _controlSensorList = new ArrayList<ControlSensor>();
    private class ControlSensor extends OutputSensor implements PropertyChangeListener {

        ControlSensor (String name) {
            super(name);
        }
        boolean setState(int state) {
            if (_sensor == null) {
                return false;
            }
            _state = state;
            return true;
        }
        void addListener() {
            _sensor.addPropertyChangeListener(this);
        }
        void removeListener() {
            _sensor.removePropertyChangeListener(this);
        }
        public void propertyChange(PropertyChangeEvent e) {
            if (e.getPropertyName().equals("KnownState")) {
                int now = ((Integer) e.getNewValue()).intValue();
                int then = ((Integer) e.getOldValue()).intValue();
                checkSensor(now, then, (Sensor)e.getSource());
            }
        }
    }
    protected Turnout mTurnout = null;
    protected transient PropertyChangeListener mTurnoutListener = null;
    protected Turnout mLockTurnout = null;
    protected transient PropertyChangeListener mLockTurnoutListener = null;
    
    ArrayList <OutputTurnout> _outputTurnoutList = new ArrayList<OutputTurnout>();
    private class OutputTurnout implements PropertyChangeListener {
        Turnout _turnout;
        int _state;

        OutputTurnout(String name) {
            _turnout = InstanceManager.turnoutManagerInstance().provideTurnout(name);
        }

        String getName() {
            if (_turnout != null)
            {
                return _turnout.getSystemName();
            }
            return null;
        }
        boolean setState(int state) {
            if (_turnout == null) {
                return false;
            }
            if ((state!=Turnout.THROWN) && (state!=Turnout.CLOSED) && (state!=Route.TOGGLE)) {
                log.warn("Illegal Turnout state for Route: "+getName() );
                return false;
            }        
            _state = state;
            return true;
        }
        int getState() {
            return _state;
        }
        Turnout getTurnout() {
            return _turnout;
        }
        void addListener() {
            _turnout.addPropertyChangeListener(this);
        }
        void removeListener() {
            _turnout.removePropertyChangeListener(this);
        }
        public void propertyChange(PropertyChangeEvent e) {
            if (e.getPropertyName().equals("KnownState")
                         || e.getPropertyName().equals("CommandedState")) {
                //check alignement of all turnouts in route
                checkTurnoutAlignment();
            }
        }
    }
	private boolean busy = false;     
    private boolean _enabled = true;

    public boolean getEnabled() { 
        return _enabled; 
    }
    public void setEnabled(boolean v) { 
        boolean old = _enabled;
        _enabled = v;
        if (old != v) firePropertyChange("Enabled", Boolean.valueOf(old), Boolean.valueOf(v));
    }
    
    private boolean _locked = false;
    public boolean getLocked() {
         return _locked; 
    }
    public void setLocked(boolean v) {
        lockTurnouts(v);
        boolean old = _locked;
        _locked = v;
        if (old != v) firePropertyChange("Locked", Boolean.valueOf(old), Boolean.valueOf(v));
    }
    /**
	 * Determine if route can be locked. Requres at least one turnout that can
	 * be locked
	 */
	public boolean canLock() {
        for (int i=0; i<_outputTurnoutList.size(); i++) {
            if (_outputTurnoutList.get(i).getTurnout().canLock(Turnout.CABLOCKOUT)) {
				return true;
            }
        }
		return false;
	}
 
    
    /**
     * Add an output Turnout to this Route
     * @param turnoutSystemName The turnout system name
     * @param turnoutState must be Turnout.CLOSED, Turnout.THROWN, or Route.TOGGLE, 
     *      which determines how the Turnout is to be switched when this Route is set
     */
    public boolean addOutputTurnout(String turnoutSystemName, int turnoutState) {
        OutputTurnout outputTurnout = new OutputTurnout(turnoutSystemName);
        if (!outputTurnout.setState(turnoutState) ) {
            return false;
        }
        _outputTurnoutList.add(outputTurnout);
        return true;
    }

    /**
     * Delete all output Turnouts from this Route
     */
    public void clearOutputTurnouts() {
        _outputTurnoutList = new ArrayList<OutputTurnout>();
    }

    public int getNumOutputTurnouts() {
        return _outputTurnoutList.size();
    }

    /**
     * Method to get a Route Turnout System Name by Index
     *  Returns null if there is no turnout with that index
     */
    public String getOutputTurnoutByIndex(int index) {
        try {
            return _outputTurnoutList.get(index).getName();
        } catch (IndexOutOfBoundsException ioob) {
            return null;
        }
    }
    
    /**
     * Method to inquire if a Turnout is included in this Route.
     * <P>
     * Complicated by the fact that either the argument or the
     * internal names might be user or system names
     */
    public boolean isOutputTurnoutIncluded(String turnoutName) {
        Turnout t1 = InstanceManager.turnoutManagerInstance().provideTurnout(turnoutName);
        for (int i=0; i<_outputTurnoutList.size(); i++) {
            if ( _outputTurnoutList.get(i).getTurnout() == t1 ) {
                // Found turnout
                return true;
            }
        }
        return false;
    }
    
    /**
     * Method to get the Set State of a Turnout included in this Route
     * <P>
     * Noth the input and internal names can be either a user or system name
     * @return -1 if there are less than 'k' Turnouts defined
     */
    public int getOutputTurnoutSetState(String name) {
        Turnout t1 = InstanceManager.turnoutManagerInstance().provideTurnout(name);
        for (int i=0; i<_outputTurnoutList.size(); i++) {
            if( _outputTurnoutList.get(i).getTurnout() == t1 ) {
                // Found turnout
                return _outputTurnoutList.get(i).getState();
            }
        }
        return -1;
    }

    /**
     * Method to return the 'k'th Turnout of the Route.
     * @return null if there are less than 'k' Turnouts defined
	 */
    public Turnout getOutputTurnout(int k) {
        try {
            return _outputTurnoutList.get(k).getTurnout();
        } catch (IndexOutOfBoundsException ioob) {
            return null;
        }
	}
	
    /**
     * Method to get the desired state of 'k'th Turnout of the Route.
     *   Returns -1 if there are less than 'k' Turnouts defined
	 */
    public int getOutputTurnoutState(int k) {
        try {
            return _outputTurnoutList.get(k).getState();
        } catch (IndexOutOfBoundsException ioob) {
            return -1;
        }
	}

    // output sensors (new interface only)

    /**
     * Add an output Sensor to this Route
     * @param systemName The sensor system name
     * @param state must be Sensor.ACTIVE, Sensor.INACTIVE, or Route.TOGGLE, 
     *      which determines how the Sensor is to be set when this Route is set
     */
    public boolean addOutputSensor(String systemName, int state) {
        OutputSensor outputSensor = new OutputSensor(systemName);
        if (!outputSensor.setState(state) ) {
            return false;
        }
        _outputSensorList.add(outputSensor);
        return true;
    }

    /**
     * Delete all output Sensors from this Route
     */
    public void clearOutputSensors() {
        _outputSensorList = new ArrayList<OutputSensor>();
    }
    
    public int getNumOutputSensors() {
        return _outputSensorList.size();
    }

    /**
     * Method to get an ouput Sensor system name by Index
     *  Returns null if there is no sensor with that index
     */
    public String getOutputSensorByIndex(int index) {
        try {
            return _outputSensorList.get(index).getName();
        } catch (IndexOutOfBoundsException ioob) {
            return null;
        }
    }
    
    /**
     * Method to inquire if a Sensor is included in this Route
     */
    public boolean isOutputSensorIncluded(String systemName) {
        Sensor s1 = InstanceManager.sensorManagerInstance().provideSensor(systemName);
        for (int i=0; i<_outputSensorList.size(); i++) {
            if ( _outputSensorList.get(i).getSensor() == s1 ) {
                // Found turnout
                return true;
            }
        }
        return false;
    }

    /**
     * Method to get the Set State of a Sensor included in this Route
     *   If the Sensor is not found, -1 is returned.
     * <P>
     * Both the input or internal names can be either system or user names
     */
    public int getOutputSensorSetState(String name) {
        Sensor s1 = InstanceManager.sensorManagerInstance().provideSensor(name);
        for (int i=0; i<_outputSensorList.size(); i++) {
            if( _outputSensorList.get(i).getSensor() == s1 ) {
                // Found turnout
                return _outputSensorList.get(i).getState();
            }
        }
        return -1;
    }
 
    /**
     * Method to return the 'k'th Sensor of the Route.
     * @return null if there are less than 'k' Sensors defined
	 */
    public Sensor getOutputSensor(int k) {
        try {
            return _outputSensorList.get(k).getSensor();
        } catch (IndexOutOfBoundsException ioob) {
            return null;
        }
	}
	
    /**
     * Method to get the desired state of 'k'th Sensor of the Route.
     *   Returns -1 if there are less than 'k' Sensors defined
	 */
    public int getOutputSensorState(int k) {
        try {
            return _outputSensorList.get(k).getState();
        } catch (IndexOutOfBoundsException ioob) {
            return -1;
        }
	}
	
	
    /** 
     * Set name of script file to be run when Route is fired
     */
    public void setOutputScriptName(String filename) {
        scriptFilename = filename;
    }
    
    /** 
     * Get name of script file to be run when Route is fired
     */
    public String getOutputScriptName() {
        return scriptFilename;
    }
    
    /** 
     * Set name of sound file to be played when Route is fired
     */
    public void setOutputSoundName(String filename) {
        soundFilename = filename;
    }
    
    /** 
     * Get name of sound file to be played when Route is fired
     */
    public String getOutputSoundName() {
        return soundFilename;
    }
    
    /**
     * Method to set turnouts aligned sensor
     */
    public void setTurnoutsAlignedSensor(String sensorSystemName){
        if (log.isDebugEnabled()) log.debug("setTurnoutsAlignedSensor "+getSystemName()+" "+sensorSystemName);

        mTurnoutsAlignedSensor = sensorSystemName;
    }
    
    /**
     * Method to get turnouts aligned sensor
     */
    public String getTurnoutsAlignedSensor(){
        
        return mTurnoutsAlignedSensor;
    }
    // Inputs ----------------

    /**
     * Method to delete all control Sensors from this Route
     */
    public void clearRouteSensors() {
        _controlSensorList = new ArrayList<ControlSensor>();
    }
    
    /**
     * Method to add a Sensor to the list of control Sensors for this Route.
     * @param sensorSystemName nominally a system name, we'll try to
     * convert this to a system name if it's not already one
     */
    public boolean addSensorToRoute(String sensorSystemName, int mode) {
        if (_controlSensorList.size() >= MAX_CONTROL_SENSORS) {
            // reached maximum
            log.warn("Reached maximum number of control Sensors for Route: "+
                                                            getSystemName() );
        }
        ControlSensor sensor = new ControlSensor(sensorSystemName);
        if (log.isDebugEnabled()) log.debug("addSensorToRoute "+getSystemName()+" "+sensorSystemName);
        if (!sensor.setState(mode) )
        {
            return false;
        }
        _controlSensorList.add(sensor);
        return true;
    }
    
    /**
     * Method to get the SystemName of a control Sensor in this Route
     *  'index' is the index in the Sensor array of the requested 
     *      Sensor.  
     *  If there is no Sensor with that 'index', or if 'index'
     *      is not in the range 0 thru MAX_SENSORS-1, null is returned.
     */
    public String getRouteSensorName(int index) {
        try {
            return _controlSensorList.get(index).getName();
        } catch (IndexOutOfBoundsException ioob) {
            return null;
        }
    }
    /**
     * Method to get the control Sensor in this Route
     *  'index' is the index in the Sensor array of the requested 
     *      Sensor.  
     *  If there is no Sensor with that 'index', or if 'index'
     *      is not in the range 0 thru MAX_SENSORS-1, null is returned.
     */
    public Sensor getRouteSensor(int index) {
        try {
            return _controlSensorList.get(index).getSensor();
        } catch (IndexOutOfBoundsException ioob) {
            return null;
        }
    }
    /**
     * Method to get the mode associated with a control Sensor in this Route
     *  'index' is the index in the Sensor array of the requested 
     *      Sensor.  
     *  If there is no Sensor with that 'index', or if 'index'
     *      is not in the range 0 thru MAX_SENSORS-1, 
     *      ONACTIVE is returned
     */
    public int getRouteSensorMode(int index) {
        try {
            return _controlSensorList.get(index).getState();
        } catch (IndexOutOfBoundsException ioob) {
            return 0;
        }
    }

    /**
     * Method to set the SystemName of a control Turnout for this Route
     */
    public void setControlTurnout(String turnoutSystemName) {
        mControlTurnout = turnoutSystemName;
        if (mControlTurnout.length()<=2) mControlTurnout = null;
    }

    /**
     * Method to get the SystemName of a control Turnout for this Route
     */
    public String getControlTurnout() {
        return mControlTurnout;
    }
    
    /**
     * Method to set the SystemName of a lock control Turnout for this Route
     */
    public void setLockControlTurnout(String turnoutSystemName) {
        mLockControlTurnout = turnoutSystemName;
        if (mLockControlTurnout.length()<=2) mLockControlTurnout = null;
    }

    /**
     * Method to get the SystemName of a lock control Turnout for this Route
     */
    public String getLockControlTurnout() {
        return mLockControlTurnout;
    }

    /**
     * Method to set delay (milliseconds) between issuing Turnout commands
     */
    public void setRouteCommandDelay(int delay) {
		if (delay >= 0)
			mDelay = delay;
	}

    /**
     * Method to get delay (milliseconds) between issuing Turnout commands
     */
    public int getRouteCommandDelay() {
		return mDelay;
	}

    /**
     * Method to set the State of control Turnout that fires this Route
     */
    public void setControlTurnoutState(int turnoutState) {
        if ( (turnoutState == Route.ONTHROWN) 
               || (turnoutState == Route.ONCLOSED) 
               || (turnoutState == Route.ONCHANGE) 
               || (turnoutState == Route.VETOCLOSED) 
               || (turnoutState == Route.VETOTHROWN) 
            ) {
            mControlTurnoutState = turnoutState;
        } else {
            log.error("Attempt to set invalid control Turnout state for Route.");
        }
    }

    /**
     * Method to get the State of control Turnout that fires this Route
     */
    public int getControlTurnoutState() {
        return (mControlTurnoutState);
    }
    
    /**
     * Method to set the State of lock control Turnout
     */
    public void setLockControlTurnoutState(int turnoutState) {
		if ((turnoutState == Route.ONTHROWN)
				|| (turnoutState == Route.ONCLOSED)
				|| (turnoutState == Route.ONCHANGE)) {
			mLockControlTurnoutState = turnoutState;
		} else {
			log.error("Attempt to set invalid lock control Turnout state for Route.");
		}
	}

    /**
	 * Method to get the State of lock control Turnout
	 */
    public int getLockControlTurnoutState() {
        return (mLockControlTurnoutState);
    }
    
	/**
	 * Lock or unlock turnouts that are part of a route
	 */
	private void lockTurnouts(boolean lock) {
		// determine if turnout should be locked
		for (int i=0; i < _outputTurnoutList.size(); i++) {
			_outputTurnoutList.get(i).getTurnout().setLocked(
                Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, lock);
		}
	}


    /**
     * Method to set the Route
     * Sets all Route Turnouts to the state shown in the Route definition
	 * This call is ignored if the Route is 'busy', i.e., if there is a 
	 *    thread currently sending commands to this Route's turnouts.
     */
    public void setRoute() {
        if ((_outputTurnoutList.size()>0) 
                || (_outputSensorList.size()>0)
                || (soundFilename != null)
                || (scriptFilename != null)
            ) {
			if (!busy) {
				setRouteBusy();
				SetRouteThread thread = new SetRouteThread(this);
				thread.start();
			}
        }
    }

    /**
     * Handle sensor update event to see if that will set the route.
     * <P>
     * Called when a "KnownState" event is received, it assumes that
     * only one sensor is changing right now, so can use state calls 
     * for everything other than this sensor.
     *<P>
     * This will fire the route if the conditions are correct
     * <P>
     * Returns noting explicitly, but has the side effect of firing route
     */
    protected void checkSensor(int newState, int oldState, Sensor sensor) {
        // check for veto of change
        if (isVetoed()) return; // don't fire

        String name = sensor.getSystemName();
        if (log.isDebugEnabled()) log.debug("check Sensor "+name+" for "+getSystemName());
        boolean fire = false;  // dont fire unless we find something
        for (int i=0; i<_controlSensorList.size(); i++) {
            if (getRouteSensor(i).equals(sensor)) {
                // here for match, check mode & handle onActive, onInactive
                int mode = getRouteSensorMode(i);
                if (log.isDebugEnabled()) log.debug("match mode: "+mode+" new state: "+newState+" old state: "+oldState);

                // if in target mode, note whether to act
                if (  ( (mode==ONACTIVE) && (newState==Sensor.ACTIVE) )
                    || ( (mode==ONINACTIVE) && (newState==Sensor.INACTIVE) )
                    || ( (mode==ONCHANGE) && (newState!=oldState) )
                    )
                   fire = true;
                   
                // if any other modes, just skip because
                // the sensor might be in list more than once
            }
        }
        
        log.debug("check activated");
        if (!fire) return;
        
        // and finally set the route
        if (log.isDebugEnabled()) log.debug("call setRoute for "+getSystemName());
        setRoute();
    }
    
    /**
     * Turnout has changed, check to see if this fires
     * @return will fire route if appropriate
     */
     void checkTurnout(int newState, int oldState, Turnout t) {
        if (isVetoed()) return; // skip setting route
        switch (mControlTurnoutState) {
            case ONCLOSED:
                if (newState == Turnout.CLOSED) setRoute();
                return;
            case ONTHROWN:
                if (newState == Turnout.THROWN) setRoute();
                return;
            case ONCHANGE:
                if (newState != oldState) setRoute();
                return;
            default:
                // if not a firing state, return
                return;
        }
    }
     
     /**
      * Turnout has changed, check to see if this 
      * will lock or unlock route
      */
      void checkLockTurnout(int newState, int oldState, Turnout t) {
         switch (mLockControlTurnoutState) {
         case ONCLOSED:
             if (newState == Turnout.CLOSED) setLocked(true);
                else setLocked(false);
             return;
         case ONTHROWN:
             if (newState == Turnout.THROWN) setLocked(true);
                else setLocked(false);
             return;
         case ONCHANGE:
             if (newState != oldState){
            	 if (getLocked())
                        setLocked(false);
            	 else
            		 setLocked(true);
             }
             return;
         default:
             // if none, return
             return;
         }
     }
     
    /**
     * Method to check if the turnouts for this route are correctly aligned.
     *  Sets turnouits aligned sensor (if there is one) to active if the turnouts are aligned.
     *  Sets the sensor to inactive if they are not aligned
     */
    public void checkTurnoutAlignment(){
        
        //check each of the output turnouts in turn
        //turnouts are deemed not aligned if:
        // - commanded and known states don't agree
        // - non-toggle turnouts known state not equal to desired state
        // turnouts aligned sensor is then set accordingly
        
        if (mTurnoutsAlignedSensor!="") {
            boolean aligned = true;
            for (int k=0; k<_outputTurnoutList.size(); k++) {
                Turnout t = _outputTurnoutList.get(k).getTurnout();
                if (!t.isConsistentState()) {
                    aligned= false;
                    break;
                }
                int targetState = _outputTurnoutList.get(k).getState();
                if (targetState!=Route.TOGGLE && targetState!=t.getKnownState()) {
                    aligned= false;
                    break;
                }
            }
            Sensor s = InstanceManager.sensorManagerInstance().provideSensor(mTurnoutsAlignedSensor);
            try {
                if (aligned) {
                    s.setKnownState(jmri.Sensor.ACTIVE);
                } else {
                    s.setKnownState(jmri.Sensor.INACTIVE);
                }
            } catch (JmriException ex) {
                    log.warn("Exception setting sensor "+s.getSystemName()+" in route");
            }
        }
    }
    
    
    /**
     * Method to activate the Route via Sensors and control Turnout
     * Sets up for Route activation based on a list of Sensors and a control Turnout
     * Registers to receive known state changes for output turnouts
     */
    public void activateRoute() {
        
        //register output turnouts to return Known State if a turnouts aligned sensor is defined
        if (mTurnoutsAlignedSensor!="") {
            
            for (int k=0; k< _outputTurnoutList.size(); k++) {
                _outputTurnoutList.get(k).addListener();
            }
        }         
        
        for (int k=0; k< _controlSensorList.size(); k++) {
            _controlSensorList.get(k).addListener();
        }
        if ( (mControlTurnout!=null) && (mControlTurnout.length() > 2)) {
            mTurnout = InstanceManager.turnoutManagerInstance().
                                            provideTurnout(mControlTurnout);
            if (mTurnout!=null) {
                mTurnout.addPropertyChangeListener(mTurnoutListener =
                                                new java.beans.PropertyChangeListener() {
                        public void propertyChange(java.beans.PropertyChangeEvent e) {
                            if (e.getPropertyName().equals("KnownState")) {
                                int now = ((Integer) e.getNewValue()).intValue();
                                int then = ((Integer) e.getOldValue()).intValue();
                                checkTurnout(now, then, (Turnout)e.getSource());
                            }
                        }
                    });
            } else {
                // control turnout does not exist
                log.error("Route "+getSystemName()+" is linked to a Turnout that does not exist: "+
                                             mControlTurnout);
            }
        }
        if ( (mLockControlTurnout!=null) && (mLockControlTurnout.length() > 2)) {
            mLockTurnout = InstanceManager.turnoutManagerInstance().
                                            provideTurnout(mLockControlTurnout);
            if (mLockTurnout!=null) {
                mLockTurnout.addPropertyChangeListener(mLockTurnoutListener =
                                                new java.beans.PropertyChangeListener() {
                        public void propertyChange(java.beans.PropertyChangeEvent e) {
                            if (e.getPropertyName().equals("KnownState")) {
                                int now = ((Integer) e.getNewValue()).intValue();
                                int then = ((Integer) e.getOldValue()).intValue();
                                checkLockTurnout(now, then, (Turnout)e.getSource());
                            }
                        }
                    });
            } else {
                // control turnout does not exist
                log.error("Route "+getSystemName()+" is linked to a Turnout that does not exist: "+
                                             mLockControlTurnout);
            }
        }
// register for updates to the Output Turnouts

        
        
    }
    /**
     * Internal method to check whether
     * operation of the route has been vetoed by a sensor
     * or turnout setting.
     * @returns true if veto, i.e. don't fire route; false if no veto, OK to fire
     */
    boolean isVetoed() {
        log.debug("check for veto");
        // check this route not enabled
        if (!_enabled) return true;
        
        // check sensors
        for (int i=0; i<_controlSensorList.size(); i++) {
            ControlSensor controlSensor = _controlSensorList.get(i);
            int s = controlSensor.getSensor().getKnownState();
            int mode = controlSensor.getState();
            if (  ( (mode==VETOACTIVE) && (s==Sensor.ACTIVE) )
                    || ( (mode==VETOINACTIVE) && (s==Sensor.INACTIVE) ) )
                 return true;  // veto set
        }
        // check control turnout
        if ( mTurnout != null) {
            int tstate = mTurnout.getKnownState();
            if (mControlTurnoutState==Route.VETOCLOSED && tstate==Turnout.CLOSED) return true;
            if (mControlTurnoutState==Route.VETOTHROWN && tstate==Turnout.THROWN) return true;
        }
        return false;
    }
    
    /**
     * Method to deactivate the Route 
     * Deactivates Route based on a list of Sensors and two control Turnouts
     */
    public void deActivateRoute() {
        // remove control turnout if there's one 
        for (int k=0; k<_controlSensorList.size(); k++) {
            _controlSensorList.get(k).removeListener();
        }
        if (mTurnoutListener!=null) {
            mTurnout.removePropertyChangeListener(mTurnoutListener);
            mTurnoutListener = null;
        }
        // remove lock control turnout if there's one 
        if (mLockTurnoutListener!=null) {
            mLockTurnout.removePropertyChangeListener(mLockTurnoutListener);
            mLockTurnoutListener = null;
        }
        //remove listeners on output turnouts if there are any
        if (mTurnoutsAlignedSensor!=""){
            for (int k=0; k< _outputTurnoutList.size(); k++) {
                _outputTurnoutList.get(k).removeListener();
            }
        }
    }

    /**
     * Method to set Route busy when commands are being issued to 
     *   Route turnouts
	 */
    public void setRouteBusy() {
		busy = true;
	}

    /**
     * Method to set Route not busy when all commands have been
     *   issued to Route turnouts
	 */
    public void setRouteNotBusy() {
		busy = false;
	}

    /**
     * Method to query if Route is busy (returns true if commands are
     *   being issued to Route turnouts)
	 */
    public boolean isRouteBusy() {
		return (busy);
	}

    /**
     * Not needed for Routes - included to complete implementation of the NamedBean interface.
     */
    public int getState() {
        log.warn("Unexpected call to getState in DefaultRoute.");
        return UNKNOWN;
    }
    
    
    /**
     * Not needed for Routes - included to complete implementation of the NamedBean interface.
     */
    public void setState(int state) {
        log.warn("Unexpected call to setState in DefaultRoute.");
        return;
    }


    static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DefaultRoute.class.getName());
    
    
}

/**
 * Class providing a thread to set route turnouts
 */
class SetRouteThread extends Thread {
	/**
	 * Constructs the thread
	 */
    public SetRouteThread(DefaultRoute aRoute) {
		r = aRoute;
	}
	
	/** 
	 * Runs the thread - performs operations in the order:
	 * <ul>
	 * <li>Run script (can run in parallel)
	 * <li>Play Sound (runs in parallel)
	 * <li>Set Turnouts
	 * <li>Set Sensors
	 * </UL>
	 */
    public void run() {
	
        // run script defined for start of route set
	    if ((r.getOutputScriptName() != null) && (!r.getOutputScriptName().equals(""))) {
	        jmri.util.PythonInterp.runScript(jmri.util.FileUtil.getExternalFilename(r.getOutputScriptName()));
	    }
	    
        // play sound defined for start of route set
	    if ((r.getOutputSoundName() != null) && (!r.getOutputSoundName().equals(""))) {
	        jmri.jmrit.Sound snd = new jmri.jmrit.Sound(jmri.util.FileUtil.getExternalFilename(r.getOutputSoundName()));
	        snd.play();
	    }
	    
        // set sensors at
        for (int k = 0; k < r.getNumOutputSensors(); k++) {
            Sensor t = r.getOutputSensor(k);
            int state = r.getOutputSensorState(k);
            if (state==Route.TOGGLE) {
                int st = t.getKnownState();
                if (st==Sensor.ACTIVE) {
                    state = Sensor.INACTIVE;
                } else {
                    state = Sensor.ACTIVE;
                }
            }
            try {
                t.setKnownState(state);
            } catch (JmriException e) {
                log.warn("Exception setting sensor "+t.getSystemName()+" in route");
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // retain if needed later
            }
		}
		
        // set turnouts
        int delay = r.getRouteCommandDelay();

        for (int k=0; k<r.getNumOutputTurnouts(); k++) {
            Turnout t = r.getOutputTurnout(k);
            int state = r.getOutputTurnoutState(k);
            if (state==Route.TOGGLE) {
                int st = t.getKnownState();
                if (st==Turnout.CLOSED) {
                    state = Turnout.THROWN;
                } else {
                    state = Turnout.CLOSED;
                }
            }
            t.setCommandedState(state);
            try {
                Thread.sleep(250 + delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // retain if needed later
            }
        }
        //set route not busy
		r.setRouteNotBusy();
        //check turnout alignment
        r.checkTurnoutAlignment();       
	}
	
	private DefaultRoute r;
    static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SetRouteThread.class.getName());
}

/* @(#)DefaultRoute.java */
