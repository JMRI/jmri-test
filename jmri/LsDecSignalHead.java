// LsDecSignalHead.java

// This file is part of JMRI.
//
// JMRI is free software; you can redistribute it and/or modify it under 
// the terms of version 2 of the GNU General Public License as published 
// by the Free Software Foundation. See the "COPYING" file for a copy
// of this license.
// 
// JMRI is distributed in the hope that it will be useful, but WITHOUT 
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
// FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
// for more details.

package jmri;

/**
 * Implement SignalHead for Littfinski Daten Technik (LDT) signals.
 *<p>
 * These decoders can display up
 * to 8 aspects. One position of a turnout is associated with one signal aspect.
 * The class assigns turnout positions to all 7 JMRI signal aspects.
 * <P>
 * For more info on the signals, see 
 * <A HREF="http://www.ldt-infocenter.com">http://www.ldt-infocenter.com</a>.
 *
 * @author	Petr Koud'a     Copyright (C) 2007
 */
public class LsDecSignalHead extends DefaultSignalHead {
    
    public LsDecSignalHead(String sys, String user, Turnout t1, int s1, Turnout t2, int s2, Turnout t3, int s3, Turnout t4, int s4, Turnout t5, int s5, Turnout t6, int s6, Turnout t7, int s7) {
        super(sys, user);
        mGreen = t1;
        mYellow = t2;
        mRed = t3;
        mFlashGreen = t4;
        mFlashYellow = t5;
        mFlashRed = t6;
        mDark = t7;
        mGreenState = s1;
        mYellowState = s2;
        mRedState = s3;
        mFlashGreenState = s4;
        mFlashYellowState = s5;
        mFlashRedState = s6;
        mDarkState = s7;
    }

    public LsDecSignalHead(String sys, Turnout t1, int s1, Turnout t2, int s2, Turnout t3, int s3, Turnout t4, int s4, Turnout t5, int s5, Turnout t6, int s6, Turnout t7, int s7) {
        super(sys);
        mGreen = t1;
        mYellow = t2;
        mRed = t3;
        mFlashGreen = t4;
        mFlashYellow = t5;
        mFlashRed = t6;
        mDark = t7;
        mGreenState = s1;
        mYellowState = s2;
        mRedState = s3;
        mFlashGreenState = s4;
        mFlashYellowState = s5;
        mFlashRedState = s6;
        mDarkState = s7;
    }

    /*
     * Modified from DefaultSignalHead
     * removed software flashing!!!
     */
    public void setAppearance(int newAppearance) {
        int oldAppearance = mAppearance;
        mAppearance = newAppearance;
 
        if (oldAppearance != newAppearance) {
            updateOutput();
		
            // notify listeners, if any
            firePropertyChange("Appearance", new Integer(oldAppearance), new Integer(newAppearance));
        }
    }
    
    public void setLit(boolean newLit) {
        boolean oldLit = mLit;
        mLit = newLit;
        if (oldLit != newLit) {
            updateOutput();
            // notify listeners, if any
            firePropertyChange("Lit", new Boolean(oldLit), new Boolean(newLit));
        }    
    }
        
    protected void updateOutput() {
        // assumes that writing a turnout to an existing state is cheap!
	if (mLit == false) {
            mDark.setCommandedState(mDarkState);
            return;
        } else {
            switch (mAppearance) {
                case RED:
                    mRed.setCommandedState(mRedState);
                    break;
        	case FLASHRED:
                    mFlashRed.setCommandedState(mFlashRedState);
                    break;
        	case YELLOW:
                    mYellow.setCommandedState(mYellowState);
                    break;
        	case FLASHYELLOW:
                    mFlashYellow.setCommandedState(mFlashYellowState);
                    break;
        	case GREEN:
                    mGreen.setCommandedState(mGreenState);
                    break;
        	case FLASHGREEN:
                    mFlashGreen.setCommandedState(mFlashGreenState);
                    break;
        	default:
                    log.warn("Unexpected new appearance: "+mAppearance);
                // go dark
        	case DARK:
                    mDark.setCommandedState(mDarkState);
                    break;
            }
        }
    }
	    
    /**
     * Remove references to and from this object, so that it can
     * eventually be garbage-collected.
     */
    public void dispose() {
        mRed = null;
        mYellow = null;
        mGreen = null;
        mFlashRed = null;
        mFlashYellow = null;
        mFlashGreen = null;
        mDark = null;
        super.dispose();
    }

    Turnout mRed;
    Turnout mYellow;
    Turnout mGreen;
    Turnout mFlashRed;
    Turnout mFlashYellow;
    Turnout mFlashGreen;
    Turnout mDark;

    int mRedState;
    int mYellowState;
    int mGreenState;
    int mFlashRedState;
    int mFlashYellowState;
    int mFlashGreenState;
    int mDarkState;

    public Turnout getRed() {return mRed;}
    public Turnout getYellow() {return mYellow;}
    public Turnout getGreen() {return mGreen;}
    public Turnout getFlashRed() {return mFlashRed;}
    public Turnout getFlashYellow() {return mFlashYellow;}
    public Turnout getFlashGreen() {return mFlashGreen;}
    public Turnout getDark() {return mDark;}
    public int getRedState() {return mRedState;}
    public int getYellowState() {return mYellowState;}
    public int getGreenState() {return mGreenState;}
    public int getFlashRedState() {return mFlashRedState;}
    public int getFlashYellowState() {return mFlashYellowState;}
    public int getFlashGreenState() {return mFlashGreenState;}
    public int getDarkState() {return mDarkState;}
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LsDecSignalHead.class.getName());
    
}
