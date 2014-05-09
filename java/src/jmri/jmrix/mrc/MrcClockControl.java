// MrcClockControl.java

package jmri.jmrix.mrc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.implementation.DefaultClockControl;
import jmri.InstanceManager;
import jmri.Timebase;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.ResourceBundle;

/**
 * MrcClockControl.java
 *
 * Implementation of the Hardware Fast Clock for Mrc
 * <P>
 * This module is based on the NCE version.
 * <P>
 * <hr>
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
 *
 * @author      Ken Cameron Copyright (C) 2014
 * @author      Dave Duchamp Copyright (C) 2007
 * @author		Bob Jacobsen, Alex Shepherd
 * @version     $Revision: 22887 $
 */
public class MrcClockControl extends DefaultClockControl implements MrcListener
{
    ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.mrc.MrcClockControlBundle");

    /**
     * Create a ClockControl object for a Mrc clock
     */
    public MrcClockControl(MrcTrafficController tc, String prefix) {
        super();
        this.tc = tc;
        this.prefix = prefix;
        

        // Create a Timebase listener for the Minute change events
        internalClock = InstanceManager.timebaseInstance();
        if (internalClock == null){
            log.error("No Timebase Instance");
            return;
        }
        minuteChangeListener = new java.beans.PropertyChangeListener() {
                public void propertyChange(java.beans.PropertyChangeEvent e) {
                    newInternalMinute();
                }
            } ;
        if (minuteChangeListener == null){
            log.error("No minuteChangeListener");
            return;
        }
        internalClock.addMinuteChangeListener(minuteChangeListener);
        tc.addMrcListener(this);
    }
    @SuppressWarnings("unused")
	private String prefix = "";
    private MrcTrafficController tc = null;
	
    /* constants, variables, etc */
    
    private static final boolean DEBUG_SHOW_PUBLIC_CALLS = true;	// enable debug for each public interface
    private static final boolean DEBUG_SHOW_SYNC_CALLS = false;	// enable debug for sync logic
    
    public static final int CS_CLOCK_SCALE = 0x00;
    public static final int CS_CLOCK_MINUTES = 0x03;
    public static final int CS_CLOCK_HOURS = 0x04;
    public static final int CS_CLOCK_AMPM = 0x05;
    public static final int CS_CLOCK_1224 = 0x06;
    public static final int CS_CLOCK_STATUS = 0x0D;
    public static final int CMD_CLOCK_SET_TIME_SIZE = 0x03;
    public static final int CMD_CLOCK_SET_PARAM_SIZE = 0x02;
    public static final int CMD_CLOCK_SET_RUN_SIZE = 0x01;
    public static final int CMD_CLOCK_SET_REPLY_SIZE = 0x01;
    public static final int CMD_MEM_SET_REPLY_SIZE = 0x01;
    public static final int MAX_ERROR_ARRAY = 4;
    public static final double TARGET_SYNC_DELAY = 55;
    public static final int SYNCMODE_OFF = 0;				//0 - clocks independent
    public static final int SYNCMODE_Mrc_MASTER = 1;		//1 - Mrc sets Internal
    public static final int SYNCMODE_INTERNAL_MASTER = 2;	//2 - Internal sets Mrc
    public static final int WAIT_CMD_EXECUTION = 1000;
    
    DecimalFormat fiveDigits = new DecimalFormat("0.00000");
    DecimalFormat fourDigits = new DecimalFormat("0.0000");
    DecimalFormat threeDigits = new DecimalFormat("0.000");
    DecimalFormat twoDigits = new DecimalFormat("0.00");
    
    private int waiting = 0;
    private int clockMode = SYNCMODE_OFF;
    private boolean waitingForCmdRead = false;
    private boolean waitingForCmdStop = false;
    private boolean waitingForCmdStart = false;
    private boolean waitingForCmdRatio = false;
    private boolean waitingForCmdTime = false;
    private boolean waitingForCmd1224 = false;
    private MrcReply lastClockReadPacket = null;
    //private Date lastClockReadAtTime;
    private int	mrcLastHour;
    private int mrcLastMinute;
    private int mrcLastSecond;
    private int mrcLastRatio;
    private boolean mrcLastAmPm;
    private boolean mrcLast1224;
    //private boolean mrcLastRunning;
    //private double internalLastRatio;
    //private boolean internalLastRunning;
    //private double syncInterval = TARGET_SYNC_DELAY;
    //private int internalSyncInitStateCounter = 0;
    //private int internalSyncRunStateCounter = 0;
    private boolean issueDeferredGetTime = false;
    //private boolean issueDeferredGetRate = false;
    //private boolean initNeverCalledBefore = true;
    
    private int mrcSyncInitStateCounter = 0;	// MRC master sync initialzation state machine
    private int	mrcSyncRunStateCounter = 0;	// MRC master sync runtime state machine
    //private int	alarmDisplayStateCounter = 0;	// manages the display update from the alarm
    
    Timebase internalClock ;
    javax.swing.Timer alarmSyncUpdate = null;
    java.beans.PropertyChangeListener minuteChangeListener;

    //  ignore replies
    public void  message(MrcMessage m) {
        log.error("message received: " + m);
    }  
    
    public void reply(MrcReply r) {
    	if (r.getNumDataElements() != 6 || r.getElement(0) != 0 || r.getElement(1) != 1 ||
    			r.getElement(3) != 0 || r.getElement(5) != 0) {
    		// not a clock packet
    		return;
    	}
    	if (false && log.isDebugEnabled()){
            log.debug("MrcReply(len " + r.getNumDataElements() + ") waiting: " + waiting +
        		" watingForRead: " + waitingForCmdRead +
        		" waitingForCmdTime: " + waitingForCmdTime +
        		" waitingForCmd1224: " + waitingForCmd1224 +
        		" waitingForCmdRatio: " + waitingForCmdRatio +
        		" waitingForCmdStop: " + waitingForCmdStop +
        		" waitingForCmdStart: " + waitingForCmdStart
        	);
    		
    	}
//        if (waiting <= 0) {
//            log.error(rb.getString("LogReplyEnexpected"));
//            return;
//        }
//        waiting--;
        readClockPacket(r);

//        if (waitingForCmdTime) {
//            if (r.getNumDataElements() != CMD_CLOCK_SET_REPLY_SIZE) {
//                log.error(rb.getString("LogMrcClockReplySizeError") + r.getNumDataElements());
//                return;
//            } else {
//                waitingForCmdTime = false;
//                if (r.getElement(0) != '!') {
//                    log.error("MRC set clock replied: " + r.getElement(0));
//                }
//                return;
//            }
//        }
//        if (r.getNumDataElements() != CMD_CLOCK_SET_REPLY_SIZE) {
//            log.error(rb.getString("LogMrcClockReplySizeError") + r.getNumDataElements());
//            return;
//        } else {
//            if (waitingForCmd1224) {
//                waitingForCmd1224 = false;
//                if (r.getElement(0) != '!') {
//                    log.error(rb.getString("LogMrcClock1224CmdError") + r.getElement(0));
//                }
//                return;
//            }
//            if (waitingForCmdRatio) {
//                waitingForCmdRatio = false;
//                if (r.getElement(0) != '!') {
//                    log.error(rb.getString("LogMrcClockRatioCmdError") + r.getElement(0));
//                }
//                return;
//            }
//            if (waitingForCmdStop) {
//                waitingForCmdStop = false;
//                if (r.getElement(0) != '!') {
//                    log.error(rb.getString("LogMrcClockStopCmdError") + r.getElement(0));
//                }
//                return;
//            }
//            if (waitingForCmdStart) {
//                waitingForCmdStart = false;
//                if (r.getElement(0) != '!') {
//                    log.error(rb.getString("LogMrcClockStartCmdError") + r.getElement(0));
//                }
//                return;
//            }
//        }
        return;
    }
    
    /** name of Mrc clock */
	public String getHardwareClockName() {
		if (DEBUG_SHOW_PUBLIC_CALLS && log.isDebugEnabled()){
			log.debug("getHardwareClockName");
		}
		return ("Mrc Fast Clock");
	}
	
	/** Mrc clock runs stable enough */
	public boolean canCorrectHardwareClock() {
		if (DEBUG_SHOW_PUBLIC_CALLS && log.isDebugEnabled()){
			log.debug("getHardwareClockName");
		}
		return false;
	}

	/** Mrc clock supports 12/24 operation */
	public boolean canSet12Or24HourClock() {
		if (DEBUG_SHOW_PUBLIC_CALLS && log.isDebugEnabled()){
			log.debug("canSet12Or24HourClock");
		}
		return false;
	}
	
//	/** sets Mrc clock speed, must be 1 to 15 */
//	public void setRate(double newRate) {
//		if (DEBUG_SHOW_PUBLIC_CALLS && log.isDebugEnabled()){
//			log.debug("setRate: " + newRate);
//		}
//		int newRatio = (int)newRate;
//		if (newRatio < 1 || newRatio > 15) {
//			log.error(rb.getString("LogMrcClockRatioRangeError"));
//		} else {
//        	issueClockRatio(newRatio);
//		}
//	}
	
	/** Mrc only supports integer rates */
	public boolean requiresIntegerRate() {
		if (DEBUG_SHOW_PUBLIC_CALLS && log.isDebugEnabled()){
			log.debug("requiresIntegerRate");
		}
		return true;
	}
	
	/** last known ratio from Mrc clock */
	public double getRate() {
//		issueReadOnlyRequest();	// get the current rate
		//issueDeferredGetRate = true;
		if (DEBUG_SHOW_PUBLIC_CALLS && log.isDebugEnabled()){
			log.debug("getRate: " + mrcLastRatio);
		}
		return(mrcLastRatio);
	}
	
//	/** set the time, the date part is ignored */
//    @SuppressWarnings("deprecation")
//	public void setTime(Date now) {
//		if (DEBUG_SHOW_PUBLIC_CALLS && log.isDebugEnabled()){
//			log.debug("setTime: " + now);
//		}
//		issueClockSet(now.getHours(), now.getMinutes(), now.getSeconds());
//	}
	
	/** returns the current Mrc time, does not have a date component */
    @SuppressWarnings("deprecation")
	public Date getTime() {
//		issueReadOnlyRequest();	// go get the current time value
		issueDeferredGetTime = true;
        Date now = internalClock.getTime();
        if (lastClockReadPacket != null) {
			if (mrcLast1224) {	// is 24 hour mode
	            now.setHours(mrcLastHour);
			} else {
				if (mrcLastAmPm) {	// is AM
		            now.setHours(mrcLastHour);
				} else {
		            now.setHours(mrcLastHour + 12);
				}
			}
            now.setMinutes(mrcLastMinute);
            now.setSeconds(0);
        }
        if (DEBUG_SHOW_PUBLIC_CALLS && log.isDebugEnabled()){
        	log.debug("getTime returning: " + now);
        }
        return(now);
	}
	
//	/** set Mrc clock and start clock */
//    @SuppressWarnings("deprecation")
//	public void startHardwareClock(Date now) {
//		if (DEBUG_SHOW_PUBLIC_CALLS && log.isDebugEnabled()){
//			log.debug("startHardwareClock: " + now);
//		}
//		if (!internalClock.getInternalMaster() && internalClock.getMasterName().equals(getHardwareClockName())){
//			
//		}
//		issueClockSet(now.getHours(), now.getMinutes(), now.getSeconds());
//		issueClockStart();
//	}
	
//	/** stops the Mrc Clock */
//	public void stopHardwareClock() {
//		if (DEBUG_SHOW_PUBLIC_CALLS && log.isDebugEnabled()){
//			log.debug("stopHardwareClock");
//		}
//		issueClockStop();
//	}
//	
//	/** not sure when or if this gets called, but will issue a read to get latest time */
//	public void initiateRead() {
//		if (DEBUG_SHOW_PUBLIC_CALLS && log.isDebugEnabled()){
//			log.debug("initiateRead");
//		}
//		issueReadOnlyRequest();
//	}
	
	/** stops any sync, removes listeners */
    public void dispose() {

		// Remove ourselves from the Timebase minute rollover event
		if (minuteChangeListener != null) {
			internalClock.removeMinuteChangeListener( minuteChangeListener );
			minuteChangeListener = null ;
		}
    }

    /**
     * Handles minute notifications for MRC Clock Monitor/Synchronizer
     */
    public void newInternalMinute()
    {
        if (DEBUG_SHOW_SYNC_CALLS && log.isDebugEnabled()) {
        	log.debug("newInternalMinute clockMode: " + clockMode + " mrcInit: " + mrcSyncInitStateCounter + " mrcRun: " + mrcSyncRunStateCounter);
        }
    }
    
    @SuppressWarnings("deprecation")
    private void readClockPacket (MrcReply r) {
    	//MrcReply priorClockReadPacket = lastClockReadPacket;
    	//int priorMrcRatio = mrcLastRatio;
    	//boolean priorMrcRunning = mrcLastRunning;
        lastClockReadPacket = r;
        //lastClockReadAtTime = internalClock.getTime();
        //log.debug("readClockPacket - at time: " + lastClockReadAtTime);
        mrcLastHour = r.getElement(2) & 0x1F;
        mrcLastMinute = r.getElement(4) & 0xFF;
        if ((r.getElement(2) & 0xC0) == 0x80) {
            mrcLast1224 = true;
        } else {
            mrcLast1224 = false;
        }
        if ((r.getElement(2) & 0xC0) == 0x0) {
            mrcLastAmPm = true;
        } else {
            mrcLastAmPm = false;
        }
		Date now = internalClock.getTime();
		if (mrcLast1224) {	// is 24 hour mode
            now.setHours(mrcLastHour);
		} else {
			if (mrcLastAmPm) {	// is AM
	            now.setHours(mrcLastHour);
			} else {
	            now.setHours(mrcLastHour + 12);
			}
		}
        now.setMinutes(mrcLastMinute);
        now.setSeconds(0);
        internalClock.userSetTime(now);
    }
        
//    private void issueClockRatio(int r) {
//    	log.debug("sending ratio " + r + " to mrc cmd station");
//        byte [] cmd = jmri.jmrix.mrc.MrcMessage.accSetClockRatio(r);
//        MrcMessage cmdMrc = jmri.jmrix.mrc.MrcMessage.createBinaryMessage(tc, cmd, CMD_CLOCK_SET_REPLY_SIZE);
//        waiting++;
//        waitingForCmdRatio = true;
//        tc.sendMrcMessage(cmdMrc, this);
//    }
//    
//    @SuppressWarnings("unused")
//	private void issueClock1224(boolean mode) {
//        byte [] cmd = jmri.jmrix.mrc.MrcMessage.accSetClock1224(mode);
//		MrcMessage cmdMrc = jmri.jmrix.mrc.MrcMessage.createBinaryMessage(tc, cmd, CMD_CLOCK_SET_REPLY_SIZE);
//		waiting++;
//		waitingForCmd1224 = true;
//		tc.sendMrcMessage(cmdMrc, this);
//    }
//    
//    private void issueClockStop() {
//        byte [] cmd = jmri.jmrix.mrc.MrcMessage.accStopClock();
//        MrcMessage cmdMrc = jmri.jmrix.mrc.MrcMessage.createBinaryMessage(tc, cmd, CMD_CLOCK_SET_REPLY_SIZE);
//        waiting++;
//        waitingForCmdStop = true;
//        tc.sendMrcMessage(cmdMrc, this);
//    }
//    
//    private void issueClockStart() {
//        byte [] cmd = jmri.jmrix.mrc.MrcMessage.accStartClock();
//        MrcMessage cmdMrc = jmri.jmrix.mrc.MrcMessage.createBinaryMessage(tc, cmd, CMD_CLOCK_SET_REPLY_SIZE);
//        waiting++;
//        waitingForCmdStart = true;
//        tc.sendMrcMessage(cmdMrc, this);
//    }
//
//    private void issueReadOnlyRequest() {
//        if (!waitingForCmdRead){
//            byte [] cmd = jmri.jmrix.mrc.MrcMessage.accMemoryRead(CS_CLOCK_MEM_ADDR);
//            MrcMessage cmdMrc = jmri.jmrix.mrc.MrcMessage.createBinaryMessage(tc, cmd, CS_CLOCK_MEM_SIZE);
//            waiting++;
//            waitingForCmdRead = true;
//            tc.sendMrcMessage(cmdMrc, this);
//            //			log.debug("issueReadOnlyRequest at " + internalClock.getTime());
//        }
//    }
//
//    private void issueClockSet(int hh, int mm, int ss) {
//        issueClockSetMem(hh, mm, ss);
//    }
//    
//    private void issueClockSetMem(int hh, int mm, int ss){
//        byte [] cmd = jmri.jmrix.mrc.MrcMessage.accMemoryWriteN(CS_CLOCK_MEM_ADDR + CS_CLOCK_SECONDS, 3);
//        cmd[4] = (byte) ss;
//        cmd[5] = (byte) mm;
//        cmd[6] = (byte) hh;
//        MrcMessage cmdMrc = jmri.jmrix.mrc.MrcMessage.createBinaryMessage(tc, cmd, CMD_MEM_SET_REPLY_SIZE);
//        waiting++;
//        waitingForCmdTime = true;
//        tc.sendMrcMessage(cmdMrc, this);
//    }
   
    @SuppressWarnings({ "deprecation", "unused" })
    private Date getMrcDate() {
        Date now = internalClock.getTime();
        if (lastClockReadPacket != null) {
            now.setHours(lastClockReadPacket.getElement(CS_CLOCK_HOURS));
            now.setMinutes(lastClockReadPacket.getElement(CS_CLOCK_MINUTES));
            now.setSeconds(0);
        }
        return(now);
    }

    @SuppressWarnings("unused")
	private double getMrcTime() {
        double mrcTime = 0;
        if (lastClockReadPacket != null) {
            mrcTime = (lastClockReadPacket.getElement(CS_CLOCK_HOURS) * 3600) +
                (lastClockReadPacket.getElement(CS_CLOCK_MINUTES) * 60);
        }
        return(mrcTime);
    }

//    @SuppressWarnings({ "deprecation", "unused" })
//    private double getIntTime() {
//        Date now = internalClock.getTime();
//        int ms = (int)(now.getTime() % 1000);
//        int ss = now.getSeconds();
//        int mm = now.getMinutes();
//        int hh = now.getHours();
//        if (false && log.isDebugEnabled()) {
//            log.debug("getIntTime: " + hh + ":" + mm + ":" + ss + "." + ms);
//        }
//        return((hh * 60 * 60) + (mm * 60) + ss + (ms / 1000));
//    }
    
    static Logger log = LoggerFactory.getLogger(MrcClockControl.class.getName());
}

/* @(#)MrcClockControl.java */
