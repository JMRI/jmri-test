// TripleTurnoutSignalHead.java

package jmri;

/**
 * Drive a single signal head via three "Turnout" objects.
 * <P>
 * The three Turnout objects are provided during construction,
 * and each drives a specific color (RED, YELLOW and GREEN).
 * Normally, "THROWN" is on, and "CLOSED" is off.
 * <P>
 * This class doesn't currently do flashing aspects.
 * <P>
 * This class doesn't currently listen to the Turnout's to see if they've
 * been changed via some other mechanism.
 *
 * @author	Bob Jacobsen Copyright (C) 2003
 * @version	$Revision: 1.4 $
 */
public class TripleTurnoutSignalHead extends AbstractSignalHead {

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

    public void setAppearance(int newAppearance) {
        int oldAppearance = mAppearance;
        mAppearance = newAppearance;
		updateOutput();
		
        // notify listeners, if any
        firePropertyChange("Appearance", new Integer(oldAppearance), new Integer(newAppearance));
    }

	public void setLit(boolean newLit) {
        // notify listeners, if any
        boolean oldLit = mLit;
        mLit = newLit;
        updateOutput();
        firePropertyChange("Lit", new Boolean(oldLit), new Boolean(newLit));
	}
	
	protected void updateOutput() {
		if (mLit == false) {
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

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(TripleTurnoutSignalHead.class.getName());
}

/* @(#)TripleTurnoutSignalHead.java */
