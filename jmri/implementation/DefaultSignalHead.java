// DefaultSignalHead.java

package jmri.implementation;


 /**
 * Default implementation of the basic logic of the SignalHead interface.
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 * @version     $Revision: 1.3 $
 */
public abstract class DefaultSignalHead extends AbstractSignalHead {

    public DefaultSignalHead(String systemName, String userName) {
        super(systemName, userName);
    }

    public DefaultSignalHead(String systemName) {
        super(systemName);
    }

    public void setAppearance(int newAppearance) {
        int oldAppearance = mAppearance;
        mAppearance = newAppearance;
        if ( mLit && ((newAppearance == FLASHGREEN) ||
            (newAppearance == FLASHYELLOW) ||
            (newAppearance == FLASHRED) ||
            (newAppearance == FLASHLUNAR) ) )
                startFlash();
        if ( (!mLit) || ( (newAppearance != FLASHGREEN) &&
            (newAppearance != FLASHYELLOW) &&
            (newAppearance != FLASHRED) &&
            (newAppearance != FLASHLUNAR) ) )
                stopFlash();
                
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
            if ( mLit && ((mAppearance == FLASHGREEN) ||
                    (mAppearance == FLASHYELLOW) ||
                    (mAppearance == FLASHRED) ||
                    (mAppearance == FLASHLUNAR) ) )
                startFlash();
            if (!mLit) stopFlash();
            updateOutput();
            // notify listeners, if any
            firePropertyChange("Lit", new Boolean(oldLit), new Boolean(newLit));
        }
        
    }
    
    /**
     * Set the held parameter.
     * <P>
     * Note that this does not directly effect the output on the layout;
     * the held parameter is a local variable which effects the aspect
     * only via higher-level logic
     */
    public void setHeld(boolean newHeld) {
        boolean oldHeld = mHeld;
        mHeld = newHeld;
        if (oldHeld != newHeld) {
            // notify listeners, if any
            firePropertyChange("Held", new Boolean(oldHeld), new Boolean(newHeld));
        }
        
    }
    
    /**
     * Type-specific routine to handle output to the layout hardware.
     * 
     * Does not notify listeners of changes; that's done elsewhere.
     * Should use the following variables to determine what to send:
     *<UL>
     *<LI>mAppearance
     *<LI>mLit
     *<LI>mFlashOn
     *</ul>
     */
    abstract protected void updateOutput();
    
    /**
     * Should a flashing signal be on (lit) now?
     */
    protected boolean mFlashOn = true;
    
    javax.swing.Timer timer = null;
    /**
     * On or off time of flashing signal
     */
    int delay = 750;
    
    /*
     * Start the timer that controls flashing
     */
    protected void startFlash() {
        // note that we don't force mFlashOn to be true at the start
        // of this; that way a flash in process isn't disturbed.
        if (timer==null) {
            timer = new javax.swing.Timer(delay, new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        timeout();
                    }
                });
            timer.setInitialDelay(delay);
            timer.setRepeats(true);
        }
        timer.start();
    }
    
    private void timeout() {
        if (mFlashOn) mFlashOn = false;
        else mFlashOn = true;
        
        updateOutput();
    }
    
    /*
     * Stop the timer that controls flashing.
     *
     * This is only a resource-saver; the actual use of 
     * flashing happens elsewere
     */
    protected void stopFlash() {
        if (timer!=null) timer.stop();
        mFlashOn = true;
    }
}

/* @(#)DefaultSignalHead.java */
