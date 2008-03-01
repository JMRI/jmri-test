// AbstractLight.java

package jmri;
import javax.swing.Timer;
import java.util.Date;

 /**
 * Abstract class providing partial implementation of the basic 
 *      logic of the Light interface.
 * <P>
 * Light objects require a number of instance variables.  Since 
 *     Light objects are created using the standard JMRI 
 *     systemName/userName concept, accessor routines are provided
 *     for setting and editting these instance variables.
 * <P>
 * Instance variables are divided into system-independent and
 *    system dependent categories.  System independent instance
 *    variables are defined here, and their accessor routines are
 *    implemented here.
 * <P>
 * Based in concept on AbstractSignalHead.java
 *
 * @author	Dave Duchamp Copyright (C) 2004
 * @version     $Revision: 1.12 $
 */
public abstract class AbstractLight extends AbstractNamedBean
    implements Light, java.io.Serializable {

    public AbstractLight(String systemName, String userName) {
        super(systemName, userName);
    }

    public AbstractLight(String systemName) {
        super(systemName);
    }
    
    /**
     *  System independent instance variables (saved between runs)
     */
    protected int mControlType = NO_CONTROL;
    protected int mControlSensorSense = Sensor.ACTIVE;
    protected String mControlSensorName = "";
    protected int mFastClockOnHour = 0;
    protected int mFastClockOnMin = 0;
    protected int mFastClockOffHour = 0;
    protected int mFastClockOffMin = 0;
    protected String mControlTurnoutName = "";
    protected int mTurnoutState = Turnout.CLOSED;
    protected String mTimedSensorName = "";
	protected int mTimeOnDuration = 0;
	
	/**
	 * new internal values for dimmable lights
	 */
	protected boolean mSupportsDimmable = false;
	protected boolean mIsDimmable = false;
	protected int mRequestedDimValue = 0;
	protected int mCurrentDimValue = 0;
	protected double mCurrentDimRate = 0;
	protected Date mLastDimChangeStart;
	protected int mDawnDurration = 0;
	protected int mDuskDurration = 0;
	protected double mMinDimValue = 0;
	protected double mMaxDimValue = 0;
	protected boolean mDimInit = false;
    
    /**
     *  System independent operational instance variables (not saved between runs)
     */
    protected boolean mActive = false;
    protected Sensor mControlSensor = null;
    protected java.beans.PropertyChangeListener mSensorListener = null;
	protected java.beans.PropertyChangeListener mTimebaseListener = null;
	protected Timebase mClock = null;
	protected int mTimeOn = 0;
	protected int mTimeOff = 0;
    protected Turnout mControlTurnout = null;
    protected java.beans.PropertyChangeListener mTurnoutListener = null;
    protected boolean mTimedActive = false;
    protected Sensor mTimedControlSensor = null;
    protected java.beans.PropertyChangeListener mTimedSensorListener = null;
	protected Timer mTimedControlTimer = null;
	protected java.awt.event.ActionListener mTimedControlListener = null;
	protected boolean mLightOnTimerActive = false;
    protected boolean mEnabled = true;

    /**
     * Get enabled status
    */
    public boolean getEnabled() { return mEnabled; }
    /**
     * Set enabled status
     */
    public void setEnabled(boolean v) { 
        boolean old = mEnabled;
        mEnabled = v;
        if (old != v) firePropertyChange("Enabled", new Boolean(old), new Boolean(v));
    }

    /**
     * for a dimmable light, uses true and false
     */
    public boolean isDimSupported() {
    	return(mSupportsDimmable);
    }
    public boolean isCanDim() {
    	return(mIsDimmable);
    }
    public void setCanDim(boolean flag) {
    	boolean old = mIsDimmable;
    	mIsDimmable = flag;
    	if (old != flag) {
    		firePropertyChange("isDimmable", new Boolean(old), new Boolean(flag));
    	}
    }
    public boolean hasBeenDimmed() {
    	return(mDimInit);
    }
    
    /**
     * Dim rate is for number of fast minutes to go from 0 to 100%
     */
    public void setDimRate(double newRate) {
    	mCurrentDimRate = newRate;
    }

    /**
     *  Return the current dim rate of this Light
     */
    public double getDimRate() {
    	return mCurrentDimRate;
    }
    
    /**
     * sets the minimum output for a dimmed light 
     */
    public void setDimMin(double v) {
    	if (v > 1 || v < 0) {
    		return;
    	}
    	mMinDimValue = v;
    }

    /**
     * gets the minimum output for a dimmed light 
     */
    public double getDimMin() {
    	return(mMinDimValue);
    }

    /**
     * sets the minimum output for a dimmed light 
     */
    public void setDimMax(double v) {
    	if (v > 1 || v < 0) {
    		return;
    	}
    	mMaxDimValue = v;
    }

    /**
     * gets the minimum output for a dimmed light 
     */
    public double getDimMax() {
    	return(mMaxDimValue);
    }
    
    /**
     *  Return the control type of this Light
     */    
    public int getControlType() { return mControlType; }
    /**
     *  Set the control type of this Light
     */    
    public void setControlType(int controlType) {
        if ( (controlType==SENSOR_CONTROL) || 
                (controlType==FAST_CLOCK_CONTROL) ||
                (controlType==TURNOUT_STATUS_CONTROL) ||
				(controlType==TIMED_ON_CONTROL) ) {
            mControlType = controlType;
        }
        else {
            mControlType = NO_CONTROL;
        }
    }
    
    /**
     *  Return the controlling Sensor if there is one, else null
     */    
    public String getControlSensorName() { return mControlSensorName; }    
    /**
     *  Set the controlling Sensor if there is one, else null
     */    
    public void setControlSensor(String sensorName) { 
        mControlSensorName = sensorName;
    }    
    
    /**
     *  Return the controlling Sensor Sense. This is the state of the 
     *     controlling Sensor that corresponds to this Light being
     *     ON.
     */    
    public int getControlSensorSense() { return mControlSensorSense; }    
    /**
     *  Set the controlling Sensor Sense.  This is the state of the 
     *     controlling Sensor that corresponds to this Light being
     *     ON.
     *  If 'sense' does not correspond to one of the allowed states of
     *     a Sensor, this call is ignored.
     */    
    public void setControlSensorSense(int sense) {
        if ( (sense==Sensor.ACTIVE) || (sense==Sensor.INACTIVE) ) {
            mControlSensorSense = sense;
        }
    }    
    
    /**
     *  Return the On/Off Schedule if FAST_CLOCK_CONTROL
     */        
    public int getFastClockOnHour() { return mFastClockOnHour; }
    public int getFastClockOnMin() { return mFastClockOnMin; }
    public int getFastClockOffHour() { return mFastClockOffHour; }
    public int getFastClockOffMin() { return mFastClockOffMin; }
    /**
     *  Set the On/Off Schedule if FAST_CLOCK_CONTROL
     */        
    public void setFastClockControlSchedule(int onHour,int onMin,int offHour, int offMin) { 
        if ( (onHour >= 0) && (onHour <= 24) ) {
            // legal value, set it
            mFastClockOnHour = onHour;
        }
        else {
            log.error("Light time on hour not 0 - 24, but is "+onHour);
            mFastClockOnHour = 0;
        }
        if ( (onMin >= 0) && (onMin <= 59) ) {
            // legal value, set it
            mFastClockOnMin = onMin;
        }
        else {
            log.error("Light time on minute not 0 - 59, but is "+onMin);
            mFastClockOnMin = 0;
        }
        if ( (offHour >= 0) && (offHour <= 24) ) {
            // legal value, set it
            mFastClockOffHour = offHour;
        }
        else {
            log.error("Light time off hour not 0 - 24, but is "+offHour);
            mFastClockOffHour = 0;
        }
        if ( (offMin >= 0) && (offMin <= 59) ) {
            // legal value, set it
            mFastClockOffMin = offMin;
        } else {
            log.error("Light time off minute not 0 - 59, but is "+offMin);
            mFastClockOffMin = 0;
        }
    }
    
    /**
     *  Return the controlling Turnout if there is one, else null.
     */    
    public String getControlTurnoutName() { return mControlTurnoutName; }
    /** 
     *  Set the controlling Turnout.  This is the Turnout whose state
     *     controls the ON and OFF of this Light.
     */
    public void setControlTurnout(String turnoutName) {
        mControlTurnoutName = turnoutName;
    }
    /**
     *  Return the state of the controlling Turnout that corresponds to
     *    this light being ON.
     */    
    public int getControlTurnoutState() { return mTurnoutState; }
    /** 
     *  Set the state of the controlling Turnout that corresponds to
     *    this light being ON.
     *  If 'ts' is not a valid state for a turnout, this call is ignored.
     */
    public void setControlTurnoutState(int ts) {
        if ( (ts==Turnout.CLOSED) || (ts==Turnout.THROWN) ) {
            mTurnoutState = ts;
        }
    }
    /**
     *  Return the trigger Sensor system name. This is the Sensor which triggers
     *     the Timed ON state of the light when it moves from inactive to active.
     */    
	public String getControlTimedOnSensorName() {
		return mTimedSensorName;
	}
    /**
     *  Set the trigger Sensor system name. This is the Sensor which triggers
     *     the Timed ON state of the light when it moves from inactive to active.
     */    
	public void setControlTimedOnSensor(String sensorName) {
		mTimedSensorName = sensorName;
	}
    /**
     *  Return the duration (milliseconds) light is to remain ON after
     *    it has been triggered.
     */    
	public int getTimedOnDuration() { return mTimeOnDuration; }            
    /**
     *  Set the duration (milliseconds) light is to remain ON after
     *    it has been triggered.
     */    
	public void setTimedOnDuration(int duration) {
		mTimeOnDuration = duration;
	}

	abstract public void setState(int value);

    /**
	 *  Updates the status of a Light under FAST_CLOCK_CONTROL.  This
	 *   method is called every FastClock minute.
	 */
	public void updateClockControlLight() {
		if (mClock!=null) {
			Date now = mClock.getTime();
			int timeNow = now.getHours() * 60 + now.getMinutes();
			int state = getState();
			if (mTimeOn <= mTimeOff) {
				// on and off the same day
				if ( (timeNow<mTimeOn) || (timeNow>=mTimeOff) ) {
					// Light should be OFF
					if (state == ON) setState(OFF);
				}
				else { 
					// Light should be ON
					if (state == OFF) setState(ON);
				}
			}
			else {
				// on and off - different days
				if ( (timeNow>=mTimeOn) || (timeNow<mTimeOff) ) {
					// Light should be ON
					if (state == OFF) setState(ON);
				}
				else { 
					// Light should be OFF
					if (state == ON) setState(OFF);
				}
			}
		}
	}
	 
    /**
     * Activates a light by control type.  This method tests the 
     *   control type, and set up a control mechanism, appropriate 
     *   for the control type.  Some lights, e.g. signal head lights,
     *   are controlled by the signal head, so no activation is needed
     *   here.
     */
    public void activateLight() {
        // skip if Light is already active
        if (!mActive) {
            // activate according to control type
            switch (mControlType) {
                case SENSOR_CONTROL:
                    mControlSensor = null;
                    if (mControlSensorName.length()>0)
                            mControlSensor= InstanceManager.sensorManagerInstance().
                                                provideSensor(mControlSensorName);
                    if (mControlSensor!=null) {
						// if sensor state is currently known, set light accordingly
						int kState = mControlSensor.getKnownState();
						if (kState==Sensor.ACTIVE) { 
							if (mControlSensorSense==Sensor.ACTIVE) {
								// Turn light on
								setState(ON);
							}
							else {
								// Turn light off
								setState(OFF);
							}
						}
						else if (kState==Sensor.INACTIVE) {
							if (mControlSensorSense==Sensor.INACTIVE) {
								// Turn light on
								setState(ON);
							}
							else {
								// Turn light off
								setState(OFF);
							}
						}
					
						// listen for change in sensor state
                        mControlSensor.addPropertyChangeListener(mSensorListener =
                                                new java.beans.PropertyChangeListener() {
                                public void propertyChange(java.beans.PropertyChangeEvent e) {
									if (!mEnabled) return;  // ignore property change if user disabled light
                                    if (e.getPropertyName().equals("KnownState")) {
                                        int now = mControlSensor.getKnownState();
                                        if (now==Sensor.ACTIVE) { 
                                            if (mControlSensorSense==Sensor.ACTIVE) {
                                                // Turn light on
                                                setState(ON);
                                            }
                                            else {
                                                // Turn light off
                                                setState(OFF);
                                            }
                                        }
                                        else if (now==Sensor.INACTIVE) { 
                                            if (mControlSensorSense==Sensor.INACTIVE) {
                                                // Turn light on
                                                setState(ON);
                                            }
                                            else {
                                                // Turn light off
                                                setState(OFF);
                                            }
                                        }
                                    }
                                }
                        });
                        mActive = true;
                    }
                    else {
                        // control sensor does not exist
                        log.error("Light "+getSystemName()+" is linked to a Sensor that does not exist: "+
                                             mControlSensorName);
                        return;
                    }
                    break;
                    
                case FAST_CLOCK_CONTROL:
					if (mClock==null) {
						mClock = InstanceManager.timebaseInstance();
					}
					// set up time as minutes in a day
					mTimeOn = mFastClockOnHour * 60 + mFastClockOnMin;
					mTimeOff = mFastClockOffHour * 60 + mFastClockOffMin;
					// initialize light based on current fast time
					updateClockControlLight ();
					// set up to listen for time changes on a minute basis
					mClock.addMinuteChangeListener( mTimebaseListener = 
						new java.beans.PropertyChangeListener() {
							public void propertyChange(java.beans.PropertyChangeEvent e) {
								if (mEnabled) {
									// update control if light is enabled
									updateClockControlLight();
								}
							}
						});
					mActive = true;
                    break;
                case TURNOUT_STATUS_CONTROL:
                    mControlTurnout = InstanceManager.turnoutManagerInstance().
                                            provideTurnout(mControlTurnoutName);
                    if (mControlTurnout!=null) {
						// set light based on current turnout state if known
						int tState = mControlTurnout.getKnownState();
						if (tState==Turnout.CLOSED) { 
							if (mTurnoutState==Turnout.CLOSED) {
								// Turn light on
								setState(ON);
							}
							else {
								// Turn light off
								setState(OFF);
							}
						}
						else if (tState==Turnout.THROWN) { 
							if (mTurnoutState==Turnout.THROWN) {
								// Turn light on
								setState(ON);
							}
							else {
								// Turn light off
								setState(OFF);
							}
						}
						
						// listen for change in turnout state
                        mControlTurnout.addPropertyChangeListener(mTurnoutListener =
                                                new java.beans.PropertyChangeListener() {
                                public void propertyChange(java.beans.PropertyChangeEvent e) {
									if (!mEnabled) return;  // ignore property change if user disabled light
									if (e.getPropertyName().equals("KnownState")) {
                                        int now = mControlTurnout.getKnownState();
                                        if (now==Turnout.CLOSED) { 
                                            if (mTurnoutState==Turnout.CLOSED) {
                                                // Turn light on
                                                setState(ON);
                                            }
                                            else {
                                                // Turn light off
                                                setState(OFF);
                                            }
                                        }
                                        else if (now==Turnout.THROWN) { 
                                            if (mTurnoutState==Turnout.THROWN) {
                                                // Turn light on
                                                setState(ON);
                                            }
                                            else {
                                                // Turn light off
                                                setState(OFF);
                                            }
                                        }
                                    }
                                }
                        });
                        mActive = true;
                    }
                    else {
                        // control turnout does not exist
                        log.error("Light "+getSystemName()+" is linked to a Turnout that does not exist: "+
                                             mControlSensorName);
                        return;
                    }
                    break;
                case TIMED_ON_CONTROL:
                    mTimedControlSensor = InstanceManager.sensorManagerInstance().
                                            provideSensor(mTimedSensorName);
                    if (mTimedControlSensor!=null) {
						// set initial state off
						setState(OFF);
						// listen for change in timed control sensor state
                        mTimedControlSensor.addPropertyChangeListener(mTimedSensorListener =
                                                new java.beans.PropertyChangeListener() {
                                public void propertyChange(java.beans.PropertyChangeEvent e) {
									if (!mEnabled) return;  // ignore property change if user disabled light
									if (e.getPropertyName().equals("KnownState")) {
                                        int now = mTimedControlSensor.getKnownState();
										if (!mLightOnTimerActive) {
											if (now==Sensor.ACTIVE) { 
                                                // Turn light on
                                                setState(ON);
												// Create a timer if one does not exist
												if (mTimedControlTimer==null) {
													mTimedControlListener = new TimeLight();
													mTimedControlTimer = new Timer(mTimeOnDuration,
															mTimedControlListener);
												}
												// Start the Timer to turn the light OFF
												mLightOnTimerActive = true;
												mTimedControlTimer.start();
                                            }
                                        }
                                    }
                                }
                        });
                        mActive = true;
                    }
                    else {
                        // timed control sensor does not exist
                        log.error("Light "+getSystemName()+" is linked to a Sensor that does not exist: "+
                                             mTimedSensorName);
                        return;
                    }
                    break;
                case NO_CONTROL:
                    // No control mechanism specified
                    break;
                default:
                    log.warn("Unexpected control type when activating Light: "+getSystemName());
            }
        }    
    }
    
    /**
     * Deactivates a light by control type.  This method tests the 
     *   control type, and deactivates the control mechanism, appropriate 
     *   for the control type.  Some lights, e.g. signal head lights,
     *   are controlled by the signal head, so no deactivation is needed
     *   here.
     */
    public void deactivateLight() {
        // skip if Light is not active
        if (mActive) {
            // deactivate according to control type
            switch (mControlType) {
                case SENSOR_CONTROL:
                    if (mSensorListener!=null) {
                        mControlSensor.removePropertyChangeListener(mSensorListener);
                        mSensorListener = null;
                    }
                    break;
                case FAST_CLOCK_CONTROL:
					if ( (mClock!=null) && (mTimebaseListener!=null) ){
						mClock.removeMinuteChangeListener(mTimebaseListener);
						mTimebaseListener = null;
					}
                    break;
                case TURNOUT_STATUS_CONTROL:
                    if (mTurnoutListener!=null) {
                        mControlTurnout.removePropertyChangeListener(mTurnoutListener);
                        mTurnoutListener = null;
                    }
                    break;
                case TIMED_ON_CONTROL:
                    if (mTimedSensorListener!=null) {
                        mTimedControlSensor.removePropertyChangeListener(mTimedSensorListener);
                        mTimedSensorListener = null;
                    }
					if (mLightOnTimerActive) {
						mTimedControlTimer.stop();
						mLightOnTimerActive = false;
					}
					if (mTimedControlTimer!=null) {
						if (mTimedControlListener!=null) {
							mTimedControlTimer.removeActionListener(mTimedControlListener);
							mTimedControlListener = null;
						}
						mTimedControlTimer = null;
					}
                    break;
                case NO_CONTROL:
                    // No control mechanism specified
                    break;
                default:
                    log.warn("Unexpected control type when activating Light: "+getSystemName());
            }
            mActive = false;
        }    
    }
	/**
	 *	Class for defining ActionListener for TIMED_ON_CONTROL
	 */
	class TimeLight implements java.awt.event.ActionListener 
	{
		public void actionPerformed(java.awt.event.ActionEvent event)
		{
			// Turn Light OFF
			setState(OFF);
			// Turn Timer OFF
			mTimedControlTimer.stop();
			mLightOnTimerActive = false;
		}
	}
}

/* @(#)AbstractLight.java */
