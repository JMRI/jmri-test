// AllocationRequest.java

package jmri.jmrit.dispatcher;

import org.apache.log4j.Logger;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * This class holds information and options for an AllocationRequestt. 
 * <P>
 * An AllocationRequest holds the following information:
 *	Section to be allocated
 *  Active Train requesting the allocation
  * <P>
 * A AllocationRequests is referenced via a list in DispatcherFrame, which serves as 
 *	a manager for AllocationRequest objects.
 * <P>
 * AllocationRequests are transient, and are not saved to disk.
 *
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
 *
 * @author	Dave Duchamp  Copyright (C) 2008-2010
 * @version	$Revision$
 */
public class AllocationRequest {

	/**
	 * Main constructor method
	 */
	public AllocationRequest(jmri.Section s, int num, int dir, ActiveTrain at) {
        mSection = s;
        mActiveTrain = at;
		mSectionSeqNum = num;
		mSectionDirection = dir;
		// listen for changes in Section occupancy
		if (mSection!=null) {
			mSection.addPropertyChangeListener(mSectionListener = new java.beans.PropertyChangeListener() {
                public void propertyChange(java.beans.PropertyChangeEvent e) { handleSectionChange(e); }
            });
        }
	}

	static final ResourceBundle rb = ResourceBundle
			.getBundle("jmri.jmrit.dispatcher.DispatcherBundle");
	    
	// instance variables
	private jmri.Section mSection = null;
	private ActiveTrain mActiveTrain = null;
	private int mSectionSeqNum = 0;
	private int mSectionDirection = jmri.Section.UNKNOWN;
	private java.beans.PropertyChangeListener mSectionListener = null;
	// instance variables related to automatic allocation of Sections
	private boolean mWaitingForTrain = false;
	private ArrayList<ActiveTrain> mMeetingTrainList = new ArrayList<ActiveTrain>();
	
	/**
     * Access methods 
     */
    public jmri.Section getSection() { 
		return mSection; 
	}
	public String getSectionName() {
		String s = mSection.getSystemName();
		String u = mSection.getUserName();
		if ( (u!=null) && (!u.equals("")) ) {
			return (s+"( "+u+" )");
		}
		return s;
	}
	protected ActiveTrain getActiveTrain() { return mActiveTrain; }
	protected String getActiveTrainName() {
		return (mActiveTrain.getTrainName()+"/"+mActiveTrain.getTransitName());
	}
	protected int getSectionSeqNumber(){return mSectionSeqNum;}
	protected int getSectionDirection(){return mSectionDirection;}
	protected String getSectionDirectionName() {
		if (mSectionDirection==jmri.Section.FORWARD) return rb.getString("FORWARD");
		if (mSectionDirection==jmri.Section.REVERSE) return rb.getString("REVERSE");
		return rb.getString("UNKNOWN");
	}
	protected boolean getWaitingForTrain() {return mWaitingForTrain;}
	protected void setWaitingForTrain(boolean set) {mWaitingForTrain = set;}
	protected void addMeetingTrain(ActiveTrain at) {mMeetingTrainList.add(at);}
	protected void removeMeetingTrain(ActiveTrain at) {
		for (int i=0;i<mMeetingTrainList.size();i++) {
			if (at == mMeetingTrainList.get(i)) {
				mMeetingTrainList.remove(i);
				return;
			}
		}
	}
	protected ArrayList<ActiveTrain> getMeetingTrainList() {return mMeetingTrainList;}
	
	/** 
	 * Methods
	 */
	private void handleSectionChange(java.beans.PropertyChangeEvent e) {
		DispatcherFrame.instance().sectionOccupancyChanged();
	}
		
	public void dispose() {
		if ( (mSectionListener!=null) && (mSection!=null) ) {
			mSection.removePropertyChangeListener(mSectionListener);
		}
		mSectionListener = null;
		mSection = null;
		mActiveTrain = null;
    }
			    
    static Logger log = Logger.getLogger(AllocationRequest.class.getName());
}

/* @(#)AllocationRequest.java */
