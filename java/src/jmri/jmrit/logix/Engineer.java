package jmri.jmrit.logix;

import java.util.concurrent.locks.ReentrantLock;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.implementation.SignalSpeedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Execute a throttle command script for a warrant
 *
 * @version $Revision$
 * @author  Pete Cressman  Copyright (C) 2009, 2010, 2011
 */
 
    /************************** Thread running the train *****************/

public class Engineer extends Thread implements Runnable, java.beans.PropertyChangeListener {

//    private static final long serialVersionUID = 7088050907933847146L;

    private int     _idxCurrentCommand;     // current throttle command
    private float   _currentSpeed = 0;      // Actual current throttle setting
    private float   _normalSpeed = 0;       // current commanded throttle setting (unmodified)
    private float   _maxSpeed;              // maximum throttle setting of commands
    private String  _speedType = Warrant.Normal;    // current speed name
    private boolean _abort = false;
    private boolean _halt = false;  // halt/resume from user's control
    private boolean _waitForClear = false;  // waits for signals/occupancy/allocation to clear
    private boolean _waitForSync = false;  // waits for train to catch up to commands
    private boolean _waitForSensor = false; // wait for sensor event
    private boolean _speedOverride = false; // speed changing due to signal or occupancy
    private boolean _runOnET = false;   // Execute commands on ET only - do not synch
    private boolean _setRunOnET= false; // Need to delay _runOnET from the block that set it
    private int     _syncIdx;           // block order index of current command
    private DccThrottle _throttle;
    private Warrant _warrant;
    private Sensor  _waitSensor;
    private int     _sensorWaitState;
    private ThrottleRamp _ramp;
    final ReentrantLock _lock = new ReentrantLock();
    SignalSpeedMap  _speedMap;

    Engineer(Warrant warrant, DccThrottle throttle) {
        _warrant = warrant;
        _idxCurrentCommand = 0;
        _throttle = throttle;
        _syncIdx = -1;
        _waitForSensor = false;
        _speedMap = SignalSpeedMap.getMap();
    }

    @Override
    public void run() {
        if (log.isDebugEnabled()) log.debug("Engineer started warrant "+_warrant.getDisplayName());

        int cmdBlockIdx = 0;
        float timeRatio = 1.0f;     // ratio to extend scripted time when speed is modified
        while (_idxCurrentCommand < _warrant._commands.size()) {
            long et = System.currentTimeMillis();
            ThrottleSetting ts = _warrant._commands.get(_idxCurrentCommand);
            int idx = _warrant.getIndexOfBlock(ts.getBlockName(), cmdBlockIdx);
            if (idx>=0) {
                cmdBlockIdx = idx;
            }
            _runOnET = _setRunOnET;     // OK to set here
//            long time = ts.getTime();
            long time = (long)(ts.getTime()*timeRatio);
//            if (log.isDebugEnabled()) log.debug("Start Cmd #"+(_idxCurrentCommand)+" for block \""+ts.getBlockName()+
//                  "\" currently in \""+_warrant.getBlockAt(cmdBlockIdx).getDisplayName()+"\". Warrant "+_warrant.getDisplayName());
            if (cmdBlockIdx < _warrant.getCurrentOrderIndex()) {
                // Train advancing too fast, need to process commands more quickly,
                // allowing half second for whistle toots etc.
                time = Math.min(time, 500);
            }
            String command = ts.getCommand().toUpperCase();
            // actual playback total elapsed time is "ts.getTime()" before record time.
            // current block at playback may also be before current block at record
            synchronized(this) {
                if (_abort) { break; }
                try {
                    if (time > 0) {
                        wait(time);
                    }
                    if (_abort) { break; }
                    //if (!command.equals("SET SENSOR") && !command.equals("WAIT SENSOR")
                    //          && !command.equals("RUN WARRANT")) {
                        _syncIdx = cmdBlockIdx;
                        // Having waited, time=ts.getTime(), so blocks should agree.  if not,
                        // wait for train to arrive at block and send sync notification.
                        // note, blind runs cannot detect entrance.
                        if (!_runOnET && _syncIdx > _warrant.getCurrentOrderIndex()) {
                            // commands are ahead of current train position
                            // When the next block goes active or a control command is made, a call to rampSpeedTo()
                            // will test these indexes again and can trigger a notify() to free the wait
                            if (log.isDebugEnabled()) log.debug("Wait for train to enter \""+ts.getBlockName()+
                                                      "\".  Warrant "+_warrant.getDisplayName());
                            _waitForSync = true;
                            _warrant.fireRunStatus("Command", Integer.valueOf(_idxCurrentCommand-1), Integer.valueOf(_idxCurrentCommand));
                            wait();
                        }
                    //}
                } catch (InterruptedException ie) {
                    log.error("InterruptedException "+ie);
                } catch (java.lang.IllegalArgumentException iae) {
                    log.error("IllegalArgumentException "+iae);
                }

                _waitForSync = false;
                if (_abort) { break; }

                try {
                    if (_waitForClear || _halt) {
                        wait();
                    }
                } catch (InterruptedException ie) {
                    log.error("InterruptedException "+ie);
                }
                if (_abort) { break; }
            }

            try {
                if (command.equals("SPEED")) {
                    float speed = Float.parseFloat(ts.getValue());
                    _lock.lock();
                    try {
                        _normalSpeed = speed;
                        float speedMod  = modifySpeed(speed, _speedType);
                        if (Math.abs(speed - speedMod)>.0001f) {
                            timeRatio = speed/speedMod;                            
                        } else {
                            timeRatio = 1.0f;                            
                        }
                        setSpeed(speedMod);
                        _warrant.fireRunStatus("SpeedChange", null, _speedType);
                    } finally {
                      _lock.unlock();
                    }
                } else if (command.equals("SPEEDSTEP")) {
                    int step = Integer.parseInt(ts.getValue());
                    setSpeedStepMode(step);
                } else if (command.equals("FORWARD")) {
                    boolean isForward = Boolean.parseBoolean(ts.getValue());
                    _throttle.setIsForward(isForward);
                } else if (command.startsWith("F")) {
                    int cmdNum = Integer.parseInt(command.substring(1));
                    boolean isTrue = Boolean.parseBoolean(ts.getValue());
                    setFunction(cmdNum, isTrue);
                } else if (command.startsWith("LOCKF")) {
                    int cmdNum = Integer.parseInt(command.substring(5));
                    boolean isTrue = Boolean.parseBoolean(ts.getValue());
                    setLockFunction(cmdNum, isTrue);
                } else if (command.equals("SET SENSOR")) {
                    setSensor(ts.getBlockName(), ts.getValue());
                } else if (command.equals("WAIT SENSOR")) {
                    getSensor(ts.getBlockName(), ts.getValue());
                } else if (command.equals("START TRACKER")) {
                    _warrant.startTracker();
                } else if (command.equals("RUN WARRANT")) {
                    runWarrant(ts);
                } else if (_runOnET && command.equals("NOOP")) {    // let warrant know engineer expects entry into dark block
                    _warrant.goingActive(_warrant.getBlockAt(cmdBlockIdx));
                }
                _warrant.fireRunStatus("Command", Integer.valueOf(_idxCurrentCommand), Integer.valueOf(_idxCurrentCommand));
                _idxCurrentCommand++;
                et = System.currentTimeMillis()-et;
                if (log.isDebugEnabled()) log.debug("Cmd #"+(_idxCurrentCommand)+": "+
                                                    ts.toString()+" et= "+et+" warrant "+_warrant.getDisplayName());
            } catch (NumberFormatException e) {
                  log.error("Command failed! "+ts.toString()+" - "+e);
            }
         }
        // shut down
        _warrant.stopWarrant(false);
    }
    
    protected int getCurrentCommandIndex() {
        return _idxCurrentCommand;
    }
    protected float getCurrentSpeed() {
        return _throttle.getSpeedSetting();
    }

    private void setSpeedStepMode(int stepMode) {
        _throttle.setSpeedStepMode(stepMode);
    }

    /**
     * Cannot set _runOnET until current NOOP command completes
     * @param set
     */
    protected void setRunOnET(Boolean set) {
        if (log.isDebugEnabled()) log.debug("setRunOnET "+set+" command #"+(_idxCurrentCommand)+                
                " warrant "+_warrant.getDisplayName());
        checkHalt();
        _setRunOnET = set;          
        if (!set) {
            _runOnET = set;         
        }
    }
    
    synchronized protected void setWaitforClear(boolean set) {
        _waitForClear = set;
        checkHalt();
        if (!_waitForClear && !_waitForSync) {
            this.notify();            
        }
    }

    /**
    * If waiting to sync entrance to a block boundary with recorded wait time,
    * or waiting for clearance ahead for rogue occupancy, stop aspect or sharing
    *  of turnouts, this call will free the wait.
    */  
    synchronized private void checkHalt() {
        if (!_halt && !_waitForSensor) {
            if (_syncIdx <= _warrant.getCurrentOrderIndex()) { 
                this.notify();
            }
        }       
    }

    /**
    * Occupancy of blocks and aspects of Portal signals may modify normal train speed
    * Ramp speed change.
    */
    synchronized protected void rampSpeedTo(String endSpeedType) {
        checkHalt();
        if (_speedType.equals(endSpeedType)) {
            return;
        }
        if (_currentSpeed<=0 && (endSpeedType.equals(Warrant.Stop) || endSpeedType.equals(Warrant.EStop))) {
            _waitForClear = true;
            _speedType = endSpeedType;
            return;
        }
        if (_ramp!=null) {
            _ramp.stop();
            _ramp = null;
        }
        if (log.isDebugEnabled()) log.debug("rampSpeedTo: \""+endSpeedType+"\" from \""+
                _speedType+"\" for warrant "+_warrant.getDisplayName());
        _ramp = new ThrottleRamp(endSpeedType);
        new Thread(_ramp).start();
    }

    private float modifySpeed(float tSpeed, String sType) {
        float mapSpeed = -1.0f;
        float throttleSpeed = tSpeed;
        if (sType.equals(Warrant.EStop)) {
            return mapSpeed;
        }
        if (sType.equals(Warrant.Normal)) {
            return throttleSpeed;
        }
        mapSpeed = _speedMap.getSpeed(sType);          

        switch (_speedMap.getInterpretation()) {
            case SignalSpeedMap.PERCENT_NORMAL:
                throttleSpeed *= mapSpeed/100;      // ratio of normal
                break;
            case SignalSpeedMap.PERCENT_THROTTLE:
                mapSpeed = mapSpeed/100;            // ratio of full throttle setting
                if (mapSpeed<throttleSpeed) {
                    throttleSpeed = mapSpeed;                  
                }
                break;
            case SignalSpeedMap.SPEED_MPH:          // miles per hour
                mapSpeed = mapSpeed*_warrant.getThrottleFactor()*12*5280/(3600*1000);
                if (mapSpeed<throttleSpeed) {
                    throttleSpeed = mapSpeed;                  
                }
                break;
            case SignalSpeedMap.SPEED_KMPH:
                mapSpeed = mapSpeed*_warrant.getThrottleFactor()*1000/(3600*25.4f);
                if (mapSpeed<throttleSpeed) {
                    throttleSpeed = mapSpeed;                  
                }
                break;
        }
        return throttleSpeed;
    }

    private void setSpeed(float s) {
        float speed = s;
        float minIncre = _throttle.getSpeedIncrement();
        if (0.0f < speed && speed < minIncre) {    // don't let speed be less than 1 speed step
            speed = 0.0f;
        }
        _throttle.setSpeedSetting(speed);
        _currentSpeed = speed;
        if (log.isDebugEnabled()) log.debug("_speedType="+_speedType+", Speed set to "+
                speed+" _waitForClear= "+_waitForClear+", warrant "+_warrant.getDisplayName());
    }
    
    protected float getSpeed() {
        return _currentSpeed;
    }
    
    synchronized public int getRunState() {
        if (_abort) {
            return Warrant.ABORT;
        } else  if (_halt) {
            return Warrant.HALT;
        } else if (_waitForClear) {
            return Warrant.WAIT_FOR_CLEAR;
        } else if (_waitForSync) {
            return Warrant.WAIT_FOR_TRAIN;
        } else if (_waitForSensor) {
            return Warrant.WAIT_FOR_SENSOR;
        } else if (!_speedType.equals(Warrant.Normal)) {
            return Warrant.SPEED_RESTRICTED;
        } else if (_idxCurrentCommand<=0) {
            return 0;
        }
        return Warrant.RUNNING;
    }

    public String getSpeedRestriction() {
        if (_speedOverride) {
            return "Changing to "+_speedType;
        }
        else if (_currentSpeed == 0.0f) {
            return "At Stop";
        } else {
            String units;
//            float scale =  SignalSpeedMap.getMap().getLayoutScale();
            float speed;
            if ( SignalSpeedMap.getMap().getInterpretation() == SignalSpeedMap.SPEED_KMPH) {
                units= "Kmph";
                speed = _currentSpeed*3600*25.4f/(_warrant.getThrottleFactor()*1000);
            } else {
                units = "Mph";
                speed = _currentSpeed*3600*1000/(_warrant.getThrottleFactor()*12*5280);
            }
            return Bundle.getMessage("atSpeed", _speedType, Math.round(speed), units);
        }
    }
    protected String getSpeedType() {
        return _speedType;
    }

    /**
    * Flag from user's control
    */
    synchronized public void setHalt(boolean halt) {
        _halt = halt;
        if (!_halt) { 
            _lock.lock();
            try {
                setSpeed(modifySpeed(_normalSpeed, _speedType));
                if (!_waitForClear && !_waitForSensor) {
                    this.notify();
                }
            } finally {
                _lock.unlock();
            }
        } else {
            if (_ramp!=null) {
                _ramp.stop();
                _ramp = null;
            }
            setSpeed(0.0f);
        }
        if (log.isDebugEnabled()) log.debug("setHalt("+halt+"): throttle speed= "+_throttle.getSpeedSetting()+
                                    " _waitForClear= "+_waitForClear+" warrant "+_warrant.getDisplayName());
    }

    /**
    * Flag from user to end run
    */
    synchronized public void abort() {
        _abort = true;
        if (_waitSensor!=null) {
            _waitSensor.removePropertyChangeListener(this);
        }
        if (_throttle != null) {
            _throttle.setSpeedSetting(-1.0f);
            setSpeed(0.0f);     // prevent creep after EStop - according to Jim Betz
            for (int i=0; i<10; i++) {
                setFunction(i, false);               
            }
           try {
                InstanceManager.throttleManagerInstance().releaseThrottle(_throttle, _warrant);
            } catch (Exception e) {
                // null pointer catch and maybe other such.
                log.warn("Throttle release and cancel threw: "+e);
            }
        }
        if (log.isDebugEnabled()) log.debug("Engineer shut down. warrant "+_warrant.getDisplayName());
    }
    
    protected void releaseThrottle() {
        InstanceManager.throttleManagerInstance().releaseThrottle(_throttle, _warrant);
    }

    private void setFunction(int cmdNum, boolean isSet) {
        switch (cmdNum)
        {
            case 0: _throttle.setF0(isSet); break;
            case 1: _throttle.setF1(isSet); break;
            case 2: _throttle.setF2(isSet); break;
            case 3: _throttle.setF3(isSet); break;
            case 4: _throttle.setF4(isSet); break;
            case 5: _throttle.setF5(isSet); break;
            case 6: _throttle.setF6(isSet); break;
            case 7: _throttle.setF7(isSet); break;
            case 8: _throttle.setF8(isSet); break;
            case 9: _throttle.setF9(isSet); break;
            case 10: _throttle.setF10(isSet); break;
            case 11: _throttle.setF11(isSet); break;
            case 12: _throttle.setF12(isSet); break;
            case 13: _throttle.setF13(isSet); break;
            case 14: _throttle.setF14(isSet); break;
            case 15: _throttle.setF15(isSet); break;
            case 16: _throttle.setF16(isSet); break;
            case 17: _throttle.setF17(isSet); break;
            case 18: _throttle.setF18(isSet); break;
            case 19: _throttle.setF19(isSet); break;
            case 20: _throttle.setF20(isSet); break;
            case 21: _throttle.setF21(isSet); break;
            case 22: _throttle.setF22(isSet); break;
            case 23: _throttle.setF23(isSet); break;
            case 24: _throttle.setF24(isSet); break;
            case 25: _throttle.setF25(isSet); break;
            case 26: _throttle.setF26(isSet); break;
            case 27: _throttle.setF27(isSet); break;
            case 28: _throttle.setF28(isSet); break;
        }
    }

    private void setLockFunction(int cmdNum, boolean isTrue) {
        switch (cmdNum)
        {
            case 0: _throttle.setF0Momentary(!isTrue); break;
            case 1: _throttle.setF1Momentary(!isTrue); break;
            case 2: _throttle.setF2Momentary(!isTrue); break;
            case 3: _throttle.setF3Momentary(!isTrue); break;
            case 4: _throttle.setF4Momentary(!isTrue); break;
            case 5: _throttle.setF5Momentary(!isTrue); break;
            case 6: _throttle.setF6Momentary(!isTrue); break;
            case 7: _throttle.setF7Momentary(!isTrue); break;
            case 8: _throttle.setF8Momentary(!isTrue); break;
            case 9: _throttle.setF9Momentary(!isTrue); break;
            case 10: _throttle.setF10Momentary(!isTrue); break;
            case 11: _throttle.setF11Momentary(!isTrue); break;
            case 12: _throttle.setF12Momentary(!isTrue); break;
            case 13: _throttle.setF13Momentary(!isTrue); break;
            case 14: _throttle.setF14Momentary(!isTrue); break;
            case 15: _throttle.setF15Momentary(!isTrue); break;
            case 16: _throttle.setF16Momentary(!isTrue); break;
            case 17: _throttle.setF17Momentary(!isTrue); break;
            case 18: _throttle.setF18Momentary(!isTrue); break;
            case 19: _throttle.setF19Momentary(!isTrue); break;
            case 20: _throttle.setF20Momentary(!isTrue); break;
            case 21: _throttle.setF21Momentary(!isTrue); break;
            case 22: _throttle.setF22Momentary(!isTrue); break;
            case 23: _throttle.setF23Momentary(!isTrue); break;
            case 24: _throttle.setF24Momentary(!isTrue); break;
            case 25: _throttle.setF25Momentary(!isTrue); break;
            case 26: _throttle.setF26Momentary(!isTrue); break;
            case 27: _throttle.setF27Momentary(!isTrue); break;
            case 28: _throttle.setF28Momentary(!isTrue); break;
        }
    }

    /**
     * Set Sensor state 
     * @param sensorName
     * @param action
     */
    static private void setSensor(String sensorName, String act) {
        String action = act.toUpperCase();    
        jmri.Sensor s = InstanceManager.sensorManagerInstance().getSensor(sensorName);
        if (s != null) {
            try {
                if ("ACTIVE".equals(action)) {
                    s.setKnownState(jmri.Sensor.ACTIVE);
                } else if ("INACTIVE".equals(action)) {
                    s.setKnownState(jmri.Sensor.INACTIVE);
                }
            } catch (jmri.JmriException e) {
                log.warn("Exception setting sensor "+sensorName+" in action");
            }
        } else {
            log.warn("Sensor "+sensorName+" not found.");
        }
    }

    /**
     * Wait for Sensor state event 
     * @param sensorName
     * @param action
     */
//    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="UW_UNCOND_WAIT", justification="Test for wait condition already done in line 470") 
    private void getSensor(String sensorName, String act) {
        String action = act.toUpperCase();    
        if (_waitSensor!=null) {
            _waitSensor.removePropertyChangeListener(this);
        }
        _waitSensor = InstanceManager.sensorManagerInstance().getSensor(sensorName);
        if (_waitSensor != null) {
            if ("ACTIVE".equals(action)) {
                _sensorWaitState = Sensor.ACTIVE;           
            } else if ("INACTIVE".equals(action)) {
                _sensorWaitState = Sensor.INACTIVE;                     
            } else {
                log.error("Bad Sensor command \""+action+"\"+ for sensor "+sensorName);
                return;
            }
            int state = _waitSensor.getKnownState();
            if (state==_sensorWaitState) {
                log.info("Engineer: state of event sensor "+sensorName+" already at state "+action);
                return;
            }
            _waitSensor.addPropertyChangeListener(this);
            if (log.isDebugEnabled()) log.debug("Listen for propertyChange of "+
                    _waitSensor.getDisplayName()+", wait for State= "+_sensorWaitState);
            // suspend commands until sensor changes state
            synchronized(this) {
                _waitForSensor = true;              
                while (_waitForSensor) {
                    try {
                        _warrant.fireRunStatus("Command", Integer.valueOf(_idxCurrentCommand-1), Integer.valueOf(_idxCurrentCommand));
                        wait();
                        clearSensor();
                    } catch (InterruptedException ie) {
                        log.error("InterruptedException "+ie);
                    }                   
                }
            }
        } else {
            log.warn("Sensor "+sensorName+" not found.");
        }
    }
    
    private void clearSensor() {
        if (_waitSensor!=null) {
            _waitSensor.removePropertyChangeListener(this);
        }
        _sensorWaitState = 0;
        _waitForSensor = false;
        _waitSensor = null;
    }
    
    protected Sensor getWaitSensor() {
        return _waitSensor;
    }
    
    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        if (log.isDebugEnabled()) log.debug("propertyChange "+evt.getPropertyName()+
                " new value= "+evt.getNewValue());
        if ((evt.getPropertyName().equals("KnownState") && 
                ((Number)evt.getNewValue()).intValue()== _sensorWaitState) ) {
            synchronized(this) {
                if (!_halt && !_waitForClear) {
                    clearSensor();
                    this.notify();
                }
            }
        }
    }

    /**
     * @param Throttle setting
     */
    private void runWarrant(ThrottleSetting ts) {
        Warrant w = InstanceManager.getDefault(jmri.jmrit.logix.WarrantManager.class).getWarrant(ts.getBlockName());
        if (w==null) {
            log.warn("Warrant \""+ts.getBlockName()+"\" not found.");
            return;
        }
        int num = 0;
        try {
            num = Integer.parseInt(ts.getValue());
        } catch (NumberFormatException nfe) {
            log.error("Could not parse \""+ts.getValue()+"\". "+nfe);
        }
        if (num==0) {
            log.info("Warrant \""+_warrant.getDisplayName()+"\" completed last launch of \""+ts.getBlockName()+"\".");
            return;
        }
        if (num>0) {
            num--;
            ts.setValue(Integer.toString(num));         
        }
        String msg = null;
        WarrantTableFrame f = WarrantTableFrame.getInstance();
        if (_warrant.equals(w)) {
            _idxCurrentCommand = 0;
            w.startupWarrant();
            msg = "Launching warrant \""+_warrant.getDisplayName()+"\" again.";
        } else {
            if (w.getDccAddress().equals(_warrant.getDccAddress())) {
                OBlock block = w.getfirstOrder().getBlock();
                block.deAllocate(_warrant);     // insure w can start
            }
            msg = f.runTrain(w);
            if (msg !=null) {
                w.stopWarrant(true);                
            } else {
                msg = "Launching warrant \""+w.getDisplayName()+"\" from warrant \""+_warrant.getDisplayName()+"\".";               
            }               
        }
        f.setStatusText(msg, java.awt.Color.red, true);             
        if (log.isDebugEnabled())log.debug(msg);            
    }

    /**
     * Note if speedType specifies a speed greater than the commanded speed, negative time is returned.
     * In this case caller must not ramp speed up but just continue at the commanded speed.
     * @param fromSpeedType commanded speed at start of ramp
     * @param toSpeedType speed name to end ramp
     * @return time required for speed change
     */
    protected int rampTimeForSpeedChange(String fromSpeedType, String toSpeedType) {
        float fromSpeed =  modifySpeed(_maxSpeed, fromSpeedType);
        float toSpeed = modifySpeed(_maxSpeed, toSpeedType);
        float delta = _speedMap.getStepIncrement();
        int time = _speedMap.getStepDelay();
        int num = (int)Math.abs((fromSpeed-toSpeed)/delta);
        if (log.isDebugEnabled())log.debug("rampTimeForSpeedChange from "+
                fromSpeedType+ "("+fromSpeed+") to "+toSpeedType+"("+toSpeed+") is "+time*num);          
        return time*num;
    }
    protected long getTimeForDistance(float distance, String speedtype) {
        float fromSpeed =  modifySpeed(_maxSpeed, speedtype);
        return (long)(_speedMap.getLayoutScale()*_warrant.getThrottleFactor()*distance/fromSpeed);
    }
    /**
     * Compute ramp length. Units depend on units of warrant's throttle factor
     * @return length required for speed change
     */
    protected float lookAheadLen() {
        _maxSpeed = 0.0f;
        float speed = 0.0f;
        for (ThrottleSetting ts : _warrant._commands) {
            String command = ts.getCommand().toUpperCase();
            try {
                if (command.equals("SPEED")) {
                    speed = Float.parseFloat(ts.getValue());
                    if (speed>_maxSpeed) {
                        _maxSpeed = speed;
                    }
                }
            } catch (NumberFormatException nfe) {
                  log.error("Bad Speed command "+ ts.toString()+" in warrant "+_warrant.getDisplayName()+" - "+nfe);
            }           
        }

        float delta = _speedMap.getStepIncrement();
        if (delta<=0.005f) {
            log.error("SignalSpeedMap StepIncrement is not set correctly.  Check Preferences->Warrants.");
            return 1.0f;
        }
        int time = _speedMap.getStepDelay();
        float scale = _speedMap.getLayoutScale()*_warrant.getThrottleFactor();
        // assume linear speed change to ramp down to stop
        float maxRampLength = 0.0f;
        speed = _maxSpeed;
        while (speed>=0.0f) {
            maxRampLength += (speed-delta/2)*time/scale;
            speed -= delta;
        }
        if (log.isDebugEnabled()) log.debug("_lookAheadLen(): max throttle= "+_maxSpeed+" maxRampLength= "+maxRampLength);
        return maxRampLength;
    }
    
    protected String minSpeed(String speed1, String speed2) {
        if (secondGreaterThanFirst(speed1, speed2)) {
            return speed1;
        }
        return speed2;
    }
    protected boolean secondGreaterThanFirst(String speed1, String speed2) {
        float s1 = modifySpeed(1.0f, speed1);
        float s2 = modifySpeed(1.0f, speed2);
        return (s1<s2);
    }
    
    private class ThrottleRamp implements Runnable {
        String endSpeedType;
        boolean stop = false;

        ThrottleRamp(String type) {
            endSpeedType = type;
        }
            
//        @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="NN_NAKED_NOTIFY", justification="Shared variable 'stop' not being seen by FindBugs for some reason") 
        synchronized void stop() {
            stop = true;
            notify();
        }

        public void run() {
            _lock.lock();
            _speedOverride = true;
            String old = _speedType;
            _speedType = endSpeedType;   // transition
            _warrant.fireRunStatus("SpeedRestriction", old, _speedType);
            try {
                
                synchronized(this) {
                    if (!_speedType.equals(Warrant.Stop) && !_speedType.equals(Warrant.EStop)) {
                        notify();
                        _waitForClear = false;
                    }
                }
                if (endSpeedType.equals(Warrant.EStop)) {
                    setSpeed(-1);
                } else {
                    float endSpeed = modifySpeed(_normalSpeed, endSpeedType);
                    float speed = _throttle.getSpeedSetting();
//                    _warrant.fireRunStatus("SpeedRestriction", old, 
//                                       (endSpeed > speed ? "increasing" : "decreasing"));

                    float incr = _speedMap.getStepIncrement();
                    int delay = _speedMap.getStepDelay();
                    
                    if (log.isDebugEnabled()) log.debug("ramping Speed from \""+old+"\" to \""+endSpeedType+
                            "\" step increment= "+incr+" time interval= "+delay+
                            " Ramp "+speed+" to "+endSpeed+" on warrant "+_warrant.getDisplayName());

                    if (endSpeed > speed) {
                        synchronized(this) {
                            while (speed < endSpeed) {
                                speed += incr;
                                if (speed > endSpeed) { // don't overshoot
                                    speed = endSpeed;
                                }
                                setSpeed(speed);
                                try {
                                    wait(delay);
                                } catch (InterruptedException ie) {
                                    log.error("InterruptedException "+ie);
                                }
                                if (stop) break;
                            }
                        }
                    } else {
                        synchronized(this) {
                            while (speed > endSpeed) {
                                speed -= incr;
                                if (speed < endSpeed) { // don't undershoot
                                    speed = endSpeed;
                                }
                                setSpeed(speed);
                                try {
                                    wait(delay);
                                } catch (InterruptedException ie) {
                                    log.error("InterruptedException "+ie);
                                }
                                if (stop) break;
                            }
                            if ((_speedType.equals(Warrant.Stop) || _speedType.equals(Warrant.EStop)) && speed<=0.0f) {
                                _waitForClear = true;
                            }
                        }
                    }                   
                }
            } finally {
                _speedOverride = false;
                _warrant.fireRunStatus("SpeedChange", old, _speedType);
                _lock.unlock();
            }
            if (log.isDebugEnabled()) log.debug("rampSpeed complete to \""+endSpeedType+
                    "\" on warrant "+_warrant.getDisplayName());
            checkHalt();
        }
    }

    static Logger log = LoggerFactory.getLogger(Engineer.class.getName());
}
