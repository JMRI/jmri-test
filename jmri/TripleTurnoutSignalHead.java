// TripleTurnoutSignalHead.java

package jmri;

/**
 * Drive a single signal head via three "Turnout" objects.
 * <P>
 * After much confusion, the user-level terminology 
 * was changed to call these "Triple Output"; the class
 * name remains the same to reduce recoding.
 * <P>
 * The three Turnout objects are provided during construction,
 * and each drives a specific color (RED, YELLOW and GREEN).
 * Normally, "THROWN" is on, and "CLOSED" is off.
 * <P>
 * This class doesn't currently listen to the Turnout's to see if they've
 * been changed via some other mechanism.
 *
 * @author	Bob Jacobsen Copyright (C) 2003, 2008
 * @version	$Revision: 1.8 $
 */
public class TripleTurnoutSignalHead extends DefaultSignalHead {

    public TripleTurnoutSignalHead(String sys, String user, Turnout green, Turnout yellow, Turnout red) {
        super(sys, user);
        mRed = red;
        mYellow = yellow;
        mGreen = green;
    }

    public TripleTurnoutSignalHead(String sys, Turnout green, Turnout yellow, Turnout red) {
        super(sys);
        mRed = red;
        mYellow = yellow;
        mGreen = green;
    }
	
	protected void updateOutput() {
	    // assumes that writing a turnout to an existing state is cheap!
		if (mLit == false) {
            mRed.setCommandedState(Turnout.CLOSED);
            mYellow.setCommandedState(Turnout.CLOSED);
            mGreen.setCommandedState(Turnout.CLOSED);
			return;
        } else if ( !mFlashOn &&
            ( (mAppearance == FLASHGREEN) ||
            (mAppearance == FLASHYELLOW) ||
            (mAppearance == FLASHRED) ) ) {
                // flash says to make output dark
                mRed.setCommandedState(Turnout.CLOSED);
                mYellow.setCommandedState(Turnout.CLOSED);
                mGreen.setCommandedState(Turnout.CLOSED);
			    return;

		} else {
        	switch (mAppearance) {
        		case RED:
        		case FLASHRED:
            		mRed.setCommandedState(Turnout.THROWN);
            		mYellow.setCommandedState(Turnout.CLOSED);
            		mGreen.setCommandedState(Turnout.CLOSED);
            		break;
        		case YELLOW:
        		case FLASHYELLOW:
            		mRed.setCommandedState(Turnout.CLOSED);
            		mYellow.setCommandedState(Turnout.THROWN);
            		mGreen.setCommandedState(Turnout.CLOSED);
            		break;
        		case GREEN:
        		case FLASHGREEN:
            		mRed.setCommandedState(Turnout.CLOSED);
            		mYellow.setCommandedState(Turnout.CLOSED);
            		mGreen.setCommandedState(Turnout.THROWN);
            		break;
        		default:
            		log.warn("Unexpected new appearance: "+mAppearance);
            		// go dark
        		case DARK:
            		mRed.setCommandedState(Turnout.CLOSED);
            		mYellow.setCommandedState(Turnout.CLOSED);
            		mGreen.setCommandedState(Turnout.CLOSED);
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
        super.dispose();
    }

    Turnout mRed;
    Turnout mYellow;
    Turnout mGreen;

    public Turnout getRed() {return mRed;}
    public Turnout getYellow() {return mYellow;}
    public Turnout getGreen() {return mGreen;}
	public void setRed(Turnout t) {mRed=t;}
	public void setYellow(Turnout t) {mYellow=t;}
	public void setGreen(Turnout t) {mGreen=t;}

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(TripleTurnoutSignalHead.class.getName());
}

/* @(#)TripleTurnoutSignalHead.java */
