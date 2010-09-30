// AutoActiveTrain.java

package jmri.jmrit.dispatcher;

import java.util.ArrayList;
import java.util.ResourceBundle;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import jmri.Block;
import jmri.EntryPoint;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.Section;
import jmri.Sensor;
import jmri.SignalHead;
import jmri.Timebase;
import jmri.Transit;
import jmri.ThrottleListener;

/**
 * This class holds information and options for an ActiveTrain when it is running in AUTOMATIC mode.  It ia
 *   an extension to Active Train for automatic running.
 * <P>
 * This class implements logic that follows a train around a layout.  Train follows signals, provided the 
 *   next Section is allocated to it, and its ActiveTrain's status is RUNNING.
 * <P>
 * This class is linked via it's parent ActiveTrain object.
 * <P>
 * This file is part of JMRI.
 * <P>
 * JMRI is open source software; you can redistribute it and/or modify it 
 * under the terms of version 2 of the GNU General Public License as 
 * published by the Free Software Foundation. See the "COPYING" file for 
 * a copy of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 * <P>
 * The AutoEngineer sub class is based in part on code by Pete Cressman contained in Warrants.java
 *
 * @author	Dave Duchamp  Copyright (C) 2010
 * @version	$Revision: 1.4 $
 */
public class AutoActiveTrain implements ThrottleListener {
	
	/**
	 * Main constructor method
	 */
	public AutoActiveTrain(ActiveTrain at) {
		_activeTrain = at;
		at.setAutoActiveTrain(this);
		_autoTrainAction = new AutoTrainAction(this);
		_lbManager = InstanceManager.layoutBlockManagerInstance();
	}
	
	static final ResourceBundle rb = ResourceBundle
						.getBundle("jmri.jmrit.dispatcher.DispatcherBundle");
	
    /* Speed aspects as defined by Doughlas A. Kerr - "Rail Signal Aspects and Indications"
     * doug.kerr.home.att.net/pumpkin/Rail_Signal_Aspects.pdf (from Pete Cressman)
     */
    public static final int SPEED_MASK      = 0x07;     // least significant 3 bits
    public static final int STOP_SPEED      = 0x01;     // No Speed
    public static final int RESTRICTED_SPEED = 0x02;    // Train able to stop within 1/2 visual range (10mph)
    public static final int SLOW_SPEED      = 0x03;     // Typically 15 mph  (25% of NORMAL)
    public static final int MEDIUM_SPEED    = 0x04;     // Typically 30 mph (40% of NORMAL)
    public static final int LIMITED_SPEED   = 0x05;     // Typically 40-45 mph  (65% of NORMAL)
    public static final int NORMAL_SPEED    = 0x06;     // Varies with road and location
    public static final int MAXIMUM_SPEED   = 0x07;     // "full" throttle

    private Float[] _speedRatio = { -1.0F, 0.0F, 0.15F, 0.25F, 0.40F, 0.65F, 0.9F, 1.15F };
	
	/* The ramp rates below are in addition to what the decoder itself does
	 */
	public static final int RAMP_NONE       = 0x00;		// No ramping - set speed immediately
	public static final int RAMP_FAST		= 0x01;     // Fast ramping
	public static final int RAMP_MEDIUM		= 0x02;		// Medium ramping
	public static final int RAMP_MED_SLOW	= 0x03;		// Medium/slow ramping
	public static final int RAMP_SLOW		= 0x04;		// Slow ramping

	// operational instance variables
	private ActiveTrain _activeTrain = null;
	private AutoTrainAction _autoTrainAction = null;
	private DccThrottle _throttle = null;
	private AutoEngineer _autoEngineer = null;
	private int _address = -1;
	private boolean _forward = true;		
	private float _targetSpeed = 0.0f;
	private int _savedStatus = ActiveTrain.RUNNING;
	private int _currentRampRate = RAMP_NONE;     // current Ramp Rate
	private boolean _pausingActive = false;		// true if train pausing thread is active
	
	// persistent instance variables (saved with train info)
	private int _rampRate = RAMP_NONE;     // default Ramp Rate
	private float _speedFactor = 1.0f;		// default speed factor
	private float _maxSpeed = 0.6f;			// default maximum train speed
	private boolean _resistanceWheels = true;  // true if all train cars show occupancy
	private boolean _runInReverse = false;  // true if the locomotive should run through Transit in reverse
	private boolean _soundDecoder = false;  // true if locomotive has a sound decoder
	private float _maxTrainLength = 200.0f;  // default train length (scale feet/meters)
	
	// accessor functions
	public ActiveTrain getActiveTrain() {return _activeTrain;}
	public AutoEngineer getAutoEngineer() {return _autoEngineer;}
	public AutoTrainAction getAutoTrainAction() {return _autoTrainAction;}
	public boolean getForward() {return _forward;}
	public void setForward(boolean set) {_forward = set;}
	public synchronized float getTargetSpeed() {return _targetSpeed;}
	public synchronized void setTargetSpeed(float speed) {_targetSpeed = speed;}
	public int getSavedStatus() {return _savedStatus;}
	public void setSavedStatus(int status) {_savedStatus=status;}
	public synchronized void setCurrentRampRate(int rate) {_currentRampRate = rate;}
	public int getRampRate() {return _rampRate;}
	public void setRampRate(int rate) {
		_rampRate = rate;
		_currentRampRate = rate;
	} 
	public float getSpeedFactor() {return _speedFactor;}
	public void setSpeedFactor (float factor) {_speedFactor = factor;}
	public float getMaxSpeed() {return _maxSpeed;}
	public void setMaxSpeed (float speed) {_maxSpeed = speed;}
	public boolean getResistanceWheels() {return _resistanceWheels;}
	public void setResistanceWheels(boolean set) {_resistanceWheels = set;}
	public boolean getRunInReverse() {return _runInReverse;}
	public void setRunInReverse(boolean set) {
		_runInReverse = set;
		if (_runInReverse) _forward = false;
		else _forward = true;
	}
	public boolean getSoundDecoder() {return _soundDecoder;}
	public void setSoundDecoder(boolean set) {_soundDecoder = set;}
	public float getMaxTrainLength() {return _maxTrainLength;}
	public void setMaxTrainLength (float length) {_maxTrainLength = length;}
	
	/**
	 * Initialize new Auto Active Train or get a new throttle after WORKING
	 *    Sets up the DCC address and initiates creation of a throttle to run the train.
	 */ 
	public boolean initialize() {
		// get decoder address
		_address = Integer.valueOf(_activeTrain.getDccAddress()).intValue();
		if ( (_address<1) || (_address>9999) ) {
			log.warn("invalid dcc address for "+_activeTrain.getTrainName());
			return false;
		}
		// request a throttle for automatic operation, throttle returned via callback below
		boolean ok = true;
        ok = InstanceManager.throttleManagerInstance().requestThrottle(_address,this); 
		if (!ok) {
			log.warn("Throttle for locomotive address "+_address+" could not be setup.");
			_activeTrain.setMode(ActiveTrain.DISPATCHED);
			return false;
		}
		return true;
	}
	// Throttle feedback method - Initiates running AutoEngineer with the new throttle
	public void notifyThrottleFound(DccThrottle t) {
		_throttle = t;
		if (_throttle==null) {
			javax.swing.JOptionPane.showMessageDialog(null,java.text.MessageFormat.format(rb.getString(
					"Error28"),new Object[] { _activeTrain.getTrainName() }), rb.getString("InformationTitle"),
						javax.swing.JOptionPane.INFORMATION_MESSAGE);
			log.warn("null throttle returned for train  "+_activeTrain.getTrainName()+ "during automatic initialization.");
			_activeTrain.setMode(ActiveTrain.DISPATCHED);
			return;
		}
		_autoEngineer = new AutoEngineer();
		new Thread(_autoEngineer).start();
		_activeTrain.setMode(ActiveTrain.AUTOMATIC);
		if (_resumingAutomatic) {
			_resumingAutomatic = false;
			_activeTrain.setStatus(ActiveTrain.RUNNING);
			if (_runInReverse) {
				_forward = false;
			}
			else {
				_forward = true;
			}
			setSpeedBySignal();
		}	
	}

	// more operational variables
	private ArrayList<AllocatedSection> _allocatedSectionList = new ArrayList<AllocatedSection>();
	private jmri.jmrit.display.layoutEditor.LayoutBlockManager _lbManager = null;
	private AllocatedSection _lastAllocatedSection = null;
	private boolean _initialized = false;
	private Section _nextSection = null;	                     // train has not reached this Section yet
	private AllocatedSection _currentAllocatedSection = null;    // head of the train is in this Section
	private AllocatedSection _previousAllocatedSection = null;   // previous Section - part of train could still be in this section
	private SignalHead _controllingSignal = null;
	private PropertyChangeListener _conSignalListener = null;
	private Block _conSignalProtectedBlock = null;
	private Block _currentBlock = null;
	private Block _nextBlock = null;
	private Block _previousBlock = null;
	private boolean _stoppingBySensor = false;
	private Sensor _stopSensor = null;
	private PropertyChangeListener _stopSensorListener = null;
	private boolean _stoppingByBlockOccupancy = false;    // if true, stop when _stoppingBlock goes UNOCCUPIED
	private Block _stoppingBlock = null;
	private boolean _resumingAutomatic = false;  // if true, resuming automatic mode after WORKING session
	private boolean _needSetSpeed = false;  // if true, train will set speed according to signal instead of stopping

	// keeps track of and restores previous speed
	private float _savedSpeed = 0.0f;
	protected void saveSpeed() {_savedSpeed = _targetSpeed;}
	protected void restoreSavedSpeed() {_targetSpeed = _savedSpeed;}
	
	// keeps track of number of horn execution threads that are active
	private int _activeHornThreads = 0;	
	protected void decrementHornExecution() {_activeHornThreads --;}
	protected void incrementHornExecution() {_activeHornThreads ++;}
	
	/**
	 * Notification methods 
	 *    Handle notification of change in state and occupancy of Sections and Blocks to track the train around the Transit
	 */
	protected void handleSectionStateChange(AllocatedSection as) {
		if (!isInAllocatedList(as)) addAllocatedSection(as);		
	}
	protected void handleSectionOccupancyChange(AllocatedSection as) {
// djd debugging
//		log.error("entered handleSectionOccupancyChange for "+as.getSection().getSystemName());
		if (!isInAllocatedList(as)) {
			log.warn("Unnexpected occupancy change notification - Section "+as.getSection().getSystemName());
			return;
		}
		if (as.getSection().getOccupancy()==Section.OCCUPIED) {		
			// Section changed to OCCUPIED - process if expected next Section
			if (as.getSection()==_nextSection) {
				setNewCurrentSection(as);
			}
		}
		else if (as.getSection().getOccupancy()==Section.UNOCCUPIED) {
			jmri.TransitSection ts = as.getTransitSection();
			if (ts!=null) {
				_autoTrainAction.removeTransitSection(ts);
			}
		}			
	}
	protected void handleBlockStateChange(AllocatedSection as, Block b) {
 		if (b.getState()==Block.OCCUPIED) {
			log.debug("Reached handleBlockStateChange to OCCUPIED - Section "+as.getSection().getSystemName()+
											", Block - "+b.getUserName()+", speed = "+_targetSpeed);
			// Block changed to OCCUPIED - train has entered this block
			if (b == _nextBlock) {
				_previousBlock = _currentBlock;
				_currentBlock = _nextBlock;
				_nextBlock = getNextBlock(b,as);
				if (_nextBlock!=null) {
					setupNewCurrentSignal();
				}
				else {
					// reached last block in this transit
					removeCurrentSignal();
					log.debug("block occupied stop in Current Section, Block - "+b.getUserName());					
					stopInCurrentSection();
				}
			}
			else if (b != _currentBlock) {
				log.warn("block going occupied - "+b.getUserName()+" - is not _nextBlock or _currentBlock - ignored.");
				return;
			}
		}
		else if (b.getState()==Block.UNOCCUPIED) {
			log.debug("Reached handleBlockStateChange to UNOCCUPIED - Section "+as.getSection().getSystemName()+
											", Block - "+b.getUserName()+", speed = "+_targetSpeed);													
			if ( _stoppingByBlockOccupancy && (b == _stoppingBlock) ) {
				log.debug ("setStopNow from Block unoccupied, Block = "+b.getSystemName());							
				_stoppingByBlockOccupancy = false;
				_stoppingBlock = null;
				if (_needSetSpeed) {
					_needSetSpeed = false;
					setSpeedBySignal();
				}
				else {
					setStopNow();
				}
			}
		}
		_autoTrainAction.handleBlockStateChange(as,b);
	}

	/**
	 * support methods
	 */
	private void addAllocatedSection(AllocatedSection as) {
		_allocatedSectionList.add(as);
		as.initializeMonitorBlockOccupancy();
		if (!_initialized) { 
			// this is first allocated section, get things started
			_initialized = true;
			_nextSection = as.getSection();
			_currentBlock = _activeTrain.getStartBlock();
			if ( as.getSection().containsBlock(_currentBlock) ) {			
				// starting Block is in this allocated section - find next Block
				setNewCurrentSection(as);
				_nextBlock = getNextBlock(_currentBlock, as);
			}
			else if ( as.getSection().connectsToBlock(_currentBlock) ) {
				// starting Block is connected to a Block in this allocated section
				EntryPoint ep = as.getSection().getEntryPointFromBlock(_currentBlock,as.getDirection());
				if (ep!=null) {
					_nextBlock = ep.getBlock();
				}
				else {
					log.error("failure to get entry point to Transit from Block "+_currentBlock.getSystemName());
				}				
			}
			if ( _nextBlock != null ) {			
				// set up new current signal
				setupNewCurrentSignal();
			}	
		}
		// if train is stopping for lack of an allocation, set flag to restart it
		if (!_pausingActive && (_lastAllocatedSection==_currentAllocatedSection) && 
				isStopping() &&	(_activeTrain.getStatus()==ActiveTrain.RUNNING) ) {
			_needSetSpeed = true;			
		}
		// request next allocation if appropriate--Dispatcher must decide whether to allocate it and when
		if ( (_lastAllocatedSection==null) || (_lastAllocatedSection.getNextSection()==as.getSection()) ) {
			_lastAllocatedSection = as;
			if (as.getNextSection()!=null) {
				Section nSection = as.getNextSection();
				int nextSeq = as.getNextSectionSequence();
				int nextDir = _activeTrain.getTransit().getDirectionFromSectionAndSeq(nSection,nextSeq);
				DispatcherFrame.instance().requestAllocation(_activeTrain,nSection,nextDir,nextSeq,true,null);
			}
		}
	}
	protected void removeAllocatedSection(AllocatedSection as) {
		int index = -1;
		for (int i = _allocatedSectionList.size(); i>0; i--) {
			if (_allocatedSectionList.get(i-1)==as) {
				index = i-1;
			}
		}
		if (index>=0) {
			_allocatedSectionList.remove(index);
		}
		else {
			log.warn("unexpected call to removeAllocatedSection - Section "+as.getSection().getSystemName());
		}
	}
	private boolean isStopping() {
		// here add indicator for new stopping methods, if any are added
		return ( _stoppingBySensor || _stoppingByBlockOccupancy );
	}
	private boolean isInAllocatedList(AllocatedSection as) {
		for (int i=0; i<_allocatedSectionList.size(); i++) {
			if (_allocatedSectionList.get(i)==as) {
				return true;
			}
		}
		return false;
	}
	private boolean isSectionInAllocatedList(Section s) {
		for (int i=0; i<_allocatedSectionList.size(); i++) {
			if ((_allocatedSectionList.get(i)).getSection()==s) {
				return true;
			}
		}
		return false;
	}
	private void removeCurrentSignal() {
		if (_conSignalListener!=null) {		
			_controllingSignal.removePropertyChangeListener(_conSignalListener);
			_conSignalListener = null;
		}
		_controllingSignal = null;
	}	
	private synchronized void setupNewCurrentSignal() {
		removeCurrentSignal();
		SignalHead sh = _lbManager.getFacingSignalHead(_currentBlock,_nextBlock);
		if (sh!=null) {
			_controllingSignal = sh;
			_conSignalProtectedBlock = _nextBlock;
			sh.addPropertyChangeListener(_conSignalListener = new PropertyChangeListener() {
					public void propertyChange(PropertyChangeEvent e) { 
						if (e.getPropertyName().equals("Appearance")) {
							// controlling signal has changed appearance
							setSpeedBySignal();
						}
					}
				});
			log.debug("new current signal = "+sh.getSystemName());
			setSpeedBySignal();			
		}
		// Note: null signal head will result when exiting throat-to-throat blocks.
		else log.debug("new current signal is null - sometimes OK");
	}
	private Block getNextBlock(Block b, AllocatedSection as) {
		if (_nextSection != null) {
			EntryPoint ep = as.getSection().getExitPointToSection(_nextSection,as.getDirection());
			if ( (ep!=null) && (ep.getBlock()==b)) {
				// this block is connected to a block in the next section
				return ep.getFromBlock();
			}	
		}
		// this allocated section has multiple blocks _or_ there is no next Section
		Block blk = as.getSection().getEntryBlock();
		while (blk!=null) { 		
			if (b==blk) return as.getSection().getNextBlock();
			blk = as.getSection().getNextBlock();
		}		
		return null;
	}
	private void setNewCurrentSection(AllocatedSection as) {
		if (as.getSection() == _nextSection) {
			_previousAllocatedSection = _currentAllocatedSection;
			_currentAllocatedSection = as;
			_nextSection = as.getNextSection();
			jmri.TransitSection ts = as.getTransitSection();
			if (ts!=null) {
				_autoTrainAction.addTransitSection(ts);
			}
			// check if new next Section exists but is not allocated to this train
			if ( (_nextSection!=null) && !isSectionInAllocatedList(_nextSection) ) {
				// next section is not allocated to this train, must not enter it, even if signal is OK.
				stopInCurrentSection();
				_needSetSpeed = false;
			}
		}
	}
	// called by above or when resuming after stopped action
	protected synchronized void setSpeedBySignal() {	
		if ( _pausingActive || ( (_activeTrain.getStatus()!=ActiveTrain.RUNNING) && 
				(_activeTrain.getStatus()!=ActiveTrain.WAITING) ) || (_controllingSignal==null) || 
					(_activeTrain.getMode()!=ActiveTrain.AUTOMATIC) ) {		
			// train is pausing or not RUNNING or WAITING in AUTOMATIC mode, or no controlling signal, 
			//			don't set speed based on controlling signal
			return;
		}
		switch (_controllingSignal.getAppearance()) {
			case SignalHead.DARK:
			case SignalHead.RED:
			case SignalHead.FLASHRED:					
					// May get here from signal changing before Block knows it is occupied, so must 
					//      check Block occupancy sensor, which must change before signal.
					if (_conSignalProtectedBlock.getSensor().getState()==Block.OCCUPIED) {
						// Train has just passed this signal - ignore this signal
					}
					else {
						stopInCurrentSection();
					}
					break;
			case SignalHead.YELLOW:
			case SignalHead.FLASHYELLOW:
					setSpeed(SLOW_SPEED);
					_activeTrain.setStatus(ActiveTrain.RUNNING);
					break;
			case SignalHead.GREEN:
			case SignalHead.FLASHGREEN:
					setSpeed(NORMAL_SPEED);
					_activeTrain.setStatus(ActiveTrain.RUNNING);
					break;
			case SignalHead.LUNAR:
			case SignalHead.FLASHLUNAR:
					setSpeed(RESTRICTED_SPEED);
					_activeTrain.setStatus(ActiveTrain.RUNNING);
					break;
		}
	}
	private synchronized void stopInCurrentSection() {
		if (_currentAllocatedSection==null) {
			log.error("Current allocated section null on entry to stopInCurrentSection");
			setStopNow();
			return;
		}
		if ( ( _targetSpeed == 0.0f ) || isStopping() ) {
			// ignore if train is already stopped or if stopping is in progress
			return;
		}
		// if Section has stopping sensors, use them
		if (_currentAllocatedSection.getSection().getState()==Section.FORWARD) {
			_stopSensor = _currentAllocatedSection.getSection().getForwardStoppingSensor();
		}
		else {
			_stopSensor = _currentAllocatedSection.getSection().getReverseStoppingSensor();
		}
		if (_stopSensor!=null) {
			if (_stopSensor.getKnownState()==Sensor.ACTIVE) {
				// stop sensor is already active, stop now
				setStopNow();
			}
			else {
				// sensor is not active
				setSpeed(RESTRICTED_SPEED);
				_stopSensor.addPropertyChangeListener(_stopSensorListener =
													  new java.beans.PropertyChangeListener() {
					public void propertyChange(java.beans.PropertyChangeEvent e) {
						if (e.getPropertyName().equals("KnownState")) {
							handleStopSensorChange();
						}
					}
				});
				_stoppingBySensor = true;
			}
		}
		else if ( _currentAllocatedSection.getLength() < _maxTrainLength ) {
			// train will not fit comfortably in the Section, stop it immediately
			setStopNow();
		}
		else if ( _resistanceWheels ) {
			// try to stop by watching Section Block occupancy
			if (_currentAllocatedSection.getSection().getNumBlocks()==1) { 
				if ( (_previousBlock!=null) && (_previousBlock.getState() == Block.OCCUPIED) ) {
					_stoppingBlock = _previousBlock;
					setStopByBlockOccupancy();
				}
				else {
					setStopNow();
				}
			}
			else {
				// Section has multiple blocks
				Block exitBlock = _currentAllocatedSection.getExitBlock();
				Block enterBlock = _currentAllocatedSection.getEnterBlock(_previousAllocatedSection);
				if (exitBlock==null) {
					// this is the final Section of the Transit
					Block testBlock = _currentAllocatedSection.getSection().getEntryBlock();
					// skip over unused leading blocks, if any
					while ( (testBlock!=null) && (testBlock!=enterBlock) ) {
						testBlock = _currentAllocatedSection.getSection().getNextBlock();
					}
					// is there room in the remaining blocks to hold the train?
					int testLength = getBlockLength(testBlock);
					while (testBlock!=null) {
						testBlock = _currentAllocatedSection.getSection().getNextBlock();
						if (testBlock!=null) testLength += getBlockLength(testBlock);
					}
					if ( (testLength>_maxTrainLength) && (_previousBlock!=null) && 
						(_previousBlock.getState() == Block.OCCUPIED) ) {
						// fits, pull train entirely into the last Section
						_stoppingBlock = _previousBlock;
						setStopByBlockOccupancy();
					}
					else {
						setStopNow();
					}
				}
				else if (enterBlock==null) {
					// this is the first Section of the Transit, with train starting in this Section
					setStopNow();					
				}
				else if (exitBlock==enterBlock) { 
					// entry and exit are from the same Block
					if ( (_previousBlock!=null) && (_previousBlock.getState() == Block.OCCUPIED) &&
							(getBlockLength(exitBlock) > _maxTrainLength) ) {
						_stoppingBlock = _previousBlock;
						setStopByBlockOccupancy();
					}
					else {
						setStopNow();
					}
				}
				else {
					// try to move train as far into the Section as it will comfortably fit
					int tstLength = getBlockLength(exitBlock);
					Block tstBlock = exitBlock;
					int tstBlockSeq = _currentAllocatedSection.getSection().getBlockSequenceNumber(tstBlock);
					while ( (tstLength < _maxTrainLength) && (tstBlock!=enterBlock) ) {
						int newSeqNumber = tstBlockSeq-1;
						if (_currentAllocatedSection.getDirection()==Section.REVERSE) {
							newSeqNumber = tstBlockSeq+1;
						}
						tstBlock = _currentAllocatedSection.getSection().getBlockBySequenceNumber(newSeqNumber);
						tstBlockSeq = newSeqNumber;
						tstLength += getBlockLength(tstBlock);
					}
					if (tstLength < _maxTrainLength) {
						setStopNow();						
					}
					else if (tstBlock==enterBlock) {
						// train fits, but needs all available Blocks
						if ( (_previousBlock!=null) && (_previousBlock.getState() == Block.OCCUPIED) ) {
							_stoppingBlock = _previousBlock;
							setStopByBlockOccupancy();						}
						else {
							setStopNow();
						}
					}
					else {
						// train fits, and doesn't need all available Blocks
						int xSeqNumber = tstBlockSeq-1;
						if (_currentAllocatedSection.getDirection()==Section.REVERSE) {
							xSeqNumber = tstBlockSeq+1;
						}
						_stoppingBlock = _currentAllocatedSection.getSection().
																	getBlockBySequenceNumber(xSeqNumber);
						setStopByBlockOccupancy();
					}
				}
			}
		}
		else {
			// train will fit, but no way to stop it reliably
			setStopNow();
		}
	}
	private synchronized void handleStopSensorChange() {
		if (_stopSensor.getState()==Sensor.ACTIVE) {
			_stopSensor.removePropertyChangeListener(_stopSensorListener);
			_stoppingBySensor = false;	
			_stopSensorListener = null;
			_stopSensor = null;
			if (_needSetSpeed) {
				_needSetSpeed = false;
				setSpeedBySignal();
			}
			else {
				setStopNow();
			}
		}
	}		
	private synchronized void setStopNow() {
		setSpeed(STOP_SPEED);
		if (_currentAllocatedSection.getNextSection()==null) {
			_activeTrain.setStatus(ActiveTrain.DONE);
		}
		else {
			_activeTrain.setStatus(ActiveTrain.WAITING);
		}
	}
	private void setStopByBlockOccupancy() {
		// note: _stoppingBlock must be set before invoking this method
// djd debugging
//		log.error("entered setStopByBlockOccupancy");
		_stoppingByBlockOccupancy = true;
		setSpeed(RESTRICTED_SPEED);	
	}
	private synchronized void setSpeed(int aspect) {
		_autoEngineer.setHalt(false);
	    if (aspect>STOP_SPEED) {
			float speed = _speedRatio[aspect];	
			if (speed>_maxSpeed) {
				speed = _maxSpeed;
			}	
			_targetSpeed = speed*_speedFactor;
		}
		else {
			_targetSpeed = _speedRatio[aspect];
			_autoEngineer.setHalt(true);
		}
	}
	private int getBlockLength(Block b) {
		if (b==null) return (0);
		float fLength = b.getLengthMm()/(float)(jmri.Scale.getScaleFactor(DispatcherFrame.instance().getScale()));
		if (DispatcherFrame.instance().getUseScaleMeters()) return (int)(fLength*0.001f);			
		return (int)(fLength*0.00328084f);
	}
	
	/**
	 * Initiates running in manual mode with external throttle
	 *	   This method is triggered by an action in the Transit
	 *     The throttle in use for automatic operation is dispatched
	 */
	protected void initiateWorking() {
		if (_activeTrain.getStatus()!=ActiveTrain.WORKING) {
			if (_autoEngineer!=null) {
				_autoEngineer.setHalt(true);
				waitUntilStopped();
				_autoEngineer.abort();
				_throttle.release();
				_autoEngineer = null;
				_throttle = null;
			}
			_activeTrain.setMode(ActiveTrain.MANUAL);
			_activeTrain.setStatus(ActiveTrain.WORKING);
		}
	}
	/**
	 * Returns when train is stopped
	 *  Note: Provides for _autoEngineer becoming null during wait
	 */
	protected void waitUntilStopped() {
		boolean doneWaiting = false;
		while (!doneWaiting) {
			if (_autoEngineer!=null) {
				doneWaiting = _autoEngineer.isStopped();
			}
			else {
				doneWaiting = true;
			}
			if (!doneWaiting) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// ignore this exception
				}
			}
		}
	}
	
	/** 
	 * Resumes automatic running after a working session using an external throttle
	 *    This method is triggered by the dispatcher hitting the "Resume Auto Running" button
	 *    A new throttle is acquired to allow automatic running to resume
	 */
	protected void resumeAutomaticRunning() {
		if ( (_activeTrain.getStatus()==ActiveTrain.WORKING) || 
							(_activeTrain.getStatus()==ActiveTrain.READY) ) {
			_autoTrainAction.cancelDoneSensor();
			if (initialize()) {
				_resumingAutomatic = true;
			}
			else {
				log.error("Failed to initialize throttle when resuming automatic mode.");
			}
		}
	}
	
	/**
	 * Pauses the auto active train for a specified number of fast clock minutes
	 * Pausing operation is performed in a separate thread
	 */
	public Thread pauseTrain(int fastMinutes) {
// djd debugging
//  log.error("entered pauseTrain - fastMinutes = "+fastMinutes);
		if (_pausingActive) {		
			// if a pause train thread is currently active, ignore this call
			return (null);
		}
		Runnable pauseTrain = new PauseTrain(fastMinutes);
		Thread tPause = new Thread(pauseTrain);
		tPause.start();
		return tPause;
	}

	public void terminate() {
		// here add code to stop the train and release its throttle if it is in autoRun
		while (_activeHornThreads>0) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// ignore this exception
			}
		}			
		_autoTrainAction.clearRemainingActions();
		if (_autoEngineer!=null) {
			_autoEngineer.abort();
			_throttle.release();
		}
	}
    
	public void dispose() {
		
	}

// _________________________________________________________________________________________

	// This class pauses the train in a separate thread
	//  Train is stopped, then restarted after specified number of fast Minutes have elapsed
	
	class PauseTrain implements Runnable
	{
		/** 
		 * A runnable that executes a pause train for a specified number of Fast Clock minutes
		 */
		public PauseTrain(int fastMinutes) {
			_fastMinutes = fastMinutes;
		}
		public void run() {
			// set to pause at a fast ramp rate
			_pausingActive = true;
			setCurrentRampRate(RAMP_FAST);
			_savedTargetSpeed = getTargetSpeed();
			setTargetSpeed(0.0f);
			// wait for train to stop
			boolean waitNow = true;
			boolean keepGoing = true;
			while (waitNow) {
				try {
					Thread.sleep(101);
					if (_autoEngineer!=null) {
						if (_autoEngineer.isStopped()) waitNow = false;
					}
					else {
						waitNow = false;
					}
				}
				catch (InterruptedException e) {
					waitNow = false;
					keepGoing = false;
				}
			}
			_activeTrain.setStatus(ActiveTrain.PAUSED);
			if (keepGoing) {
				// wait for specified fast clock time
				Timebase _clock = InstanceManager.timebaseInstance();
				java.beans.PropertyChangeListener _clockListener = null;
				_clock.addMinuteChangeListener(_clockListener = 
					new java.beans.PropertyChangeListener() {
						public void propertyChange(java.beans.PropertyChangeEvent e) {
							_fastMinutes --;
						}
					});
				// wait for fast minutes to tick away
				waitNow = true;
				while (waitNow) {
					try {
						Thread.sleep(101);
						if (_fastMinutes<=0) waitNow = false;
					}
					catch (InterruptedException e) {
						keepGoing = false;
					}
				}
				_clock.removeMinuteChangeListener(_clockListener);
			}
			if (keepGoing) {
				// this thread was not interrupted
				//   resume running - restore speed, status, and ramp rate
				setCurrentRampRate(getRampRate());
				setTargetSpeed(_savedTargetSpeed);
				setSpeedBySignal();
				_activeTrain.setStatus(ActiveTrain.RUNNING);
			}
			_pausingActive = false;
		}
		private int _fastMinutes = 0;
		private float _savedTargetSpeed = 0.0f;
	}
		
// _________________________________________________________________________________________
	
	// This class runs a throttle to control the train in a separate thread.
	// This class is based on code by Pete Cressman contained in Warrants.java
	
    class AutoEngineer implements Runnable {

        AutoEngineer() {
        }
		
		// operational instance variables and flags
        private float   _minSpeedStep = 1.0f;
        private boolean _abort = false;
        private boolean _halt = false;  // halt/resume from user's control
		private boolean _halted = false; // true if previously halted
		private boolean _ramping = false;  // true if ramping speed to _targetSpeed;
		private float   _currentSpeed = 0.0f;
		private boolean _currentForward = true;
		private int _targetCount[] = {0,1,2,3,4};
		private int _rampTargetCount = 0;
		private int _rampCount = 0;    

        public void run() {
			_throttle.setIsForward(_forward);
			_currentForward = _forward;
			_throttle.setSpeedSetting(_currentSpeed);
			setSpeedStep(_throttle.getSpeedStepMode());		
			// this is the running loop
            while (!_abort) {				
				if (_halt && !_halted) {
					_throttle.setSpeedSetting(-1.0f);
					_throttle.setSpeedSetting(0.0f);
					_currentSpeed = 0.0f;
					_halted = true;
				}
				if (!_halt) {
					// test if need to change direction
					if (_currentForward!=_forward) {
						_throttle.setIsForward (_forward);
						_currentForward = _forward;
					}
					// test if need to change speed
					if (java.lang.Math.abs(_currentSpeed-_targetSpeed)>0.01) {
						if (_currentRampRate==RAMP_NONE) {
							// set speed immediately
							_currentSpeed = _targetSpeed;
							_ramping = false;
						}
						else if (!_ramping) {
							// initialize ramping
							_ramping = true;
							_rampCount = 1;
							_rampTargetCount = _targetCount[_currentRampRate];
						}
						else {
							// ramping the speed
							_rampCount ++;
							if (_rampCount>_rampTargetCount) {
								// step the speed
								if (_currentSpeed<_targetSpeed) {
									// ramp up
									_currentSpeed += _minSpeedStep;
									if (_currentSpeed>=_targetSpeed) {
										_currentSpeed = _targetSpeed;
										_ramping = false;
									}
									else {
										_rampCount = 0;
									}
								}
								else {
									// ramp down
									_currentSpeed -= _minSpeedStep;
									if (_currentSpeed<=_targetSpeed) {
										_currentSpeed = _targetSpeed;
										_ramping = false;
									}
									else {
										_rampCount = 0;
									}
								}
							}
						}
					}
					_throttle.setSpeedSetting(_currentSpeed);
				}
				// delay
				synchronized(this) {
					try {
						if (!_ramping)
							wait(300);
						else	
							wait(50);
					} catch (InterruptedException ie) {
						log.error("InterruptedException in AutoEngineer"+ie);
					}
				}
             }
            // shut down
        }

        private void setSpeedStep(int step) {
            switch (step) {
                case DccThrottle.SpeedStepMode14:
                    _minSpeedStep = 1.0f/15;
					for (int i=1; i<5; i++) {
						_targetCount[i] *= 6;
					}
                    break;
                case DccThrottle.SpeedStepMode27:
                    _minSpeedStep = 1.0f/28;
					for (int i=1; i<5; i++) {
						_targetCount[i] *= 4;
					}
                    break;
                case DccThrottle.SpeedStepMode28:
                    _minSpeedStep = 1.0f/29;
					for (int i=1; i<5; i++) {
						_targetCount[i] *= 4;
					}
                    break;
                default:
                    _minSpeedStep = 1.0f/127;
                    break;
            }
        }

        /**
         * Flag from user's control
		 * Note: Halt here invokes immediate stop.
         */
        public synchronized void setHalt(boolean halt) {
            _halt = halt;
            if (!_halt) { 
				_halted = false;
			}
        }
		
		/**
		 * Allow user to test if train is moving or stopped
		 */
		public synchronized boolean isStopped() {
			if (_currentSpeed > 0.01f) return false;
			return true;
		}
		
		/**
		 * Allow user to test if train is moving at its current requested speed
		 */
		public synchronized boolean isAtSpeed() {
			if (java.lang.Math.abs(_currentSpeed-_targetSpeed)>0.01) return false;
			return true;
		}

        /**
        * Flag from user to end run
        */
        public void abort() {
            _abort = true;
            _throttle.setSpeedSetting(-1.0f);
            _throttle.setSpeedSetting(0.0f);
        }

        protected void setFunction(int cmdNum, boolean isSet) {
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
    }
	
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AutoActiveTrain.class.getName());
}

/* @(#)AutoActiveTrain.java */
