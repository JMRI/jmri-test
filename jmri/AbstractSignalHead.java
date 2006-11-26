// AbstractSignalHead.java

package jmri;

 /**
 * Abstract class providing the basic logic of the SignalHead interface.
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 * @version     $Revision: 1.11 $
 */
public abstract class AbstractSignalHead extends AbstractNamedBean
    implements SignalHead, java.io.Serializable {

    public AbstractSignalHead(String systemName, String userName) {
        super(systemName, userName);
    }

    public AbstractSignalHead(String systemName) {
        super(systemName);
    }

    protected int mAppearance = DARK;
    public int getAppearance() { return mAppearance; }

    // implementing classes will typically have a function/listener to get
    // updates from the layout, which will then call
    //		public void firePropertyChange(String propertyName,
    //						Object oldValue,
    //						Object newValue)
    // _once_ if anything has changed state


    /**
     * By default, signals are lit.
     */
	protected boolean mLit = true;
	/**
	 * Default behavior for "lit" parameter is
	 * to track value and return it.
	 */
	public boolean getLit() {return mLit;}
	
	/** 
	 * By default, signals are not held.
	 */
	protected boolean mHeld = false;
	/**
	 * "Held" parameter is just tracked and notified.
	 */
	public boolean getHeld() {return mHeld;}
	
    /**
     * Implement a shorter name for setAppearance.
     *<P>
     * This generally shouldn't be used by Java code; use 
     * setAppearance instead.  The is provided to make Jython
     * script access easier to read.  
     */
    public void setState(int s) { setAppearance(s); }
    
    /**
     * Implement a shorter name for getAppearance.
     *<P>
     * This generally shouldn't be used by Java code; use 
     * getAppearance instead.  The is provided to make Jython
     * script access easier to read.  
     */
    public int getState() { return getAppearance(); }

}

/* @(#)AbstractSignalHead.java */
