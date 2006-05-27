// AbstractAutomaton.java

package jmri.jmrit.automat;

import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.ProgListener;
import jmri.Programmer;
import jmri.ProgrammerException;
import jmri.Sensor;
import jmri.Turnout;
import jmri.ThrottleListener;
import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextArea;

/**
 * Abstract base for user automaton classes, which provide
 * individual bits of automation.
 * <P>
 * Each individual automaton runs in a separate thread, so they
 * can operate independently.  This class handles thread
 * creation and scheduling, and provides a number of services
 * for the user code.
 * <P>
 * Subclasses provide a "handle()" function, which does the needed
 * work, and optionally a "init()" function.
 * These can use any JMRI resources for input and output.  It should
 * not spin on a condition without explicit wait requests; it is more efficient
 * to use the explicit wait services when waiting for some specific
 * condition.
 * <P>
 * handle() is executed repeatedly until either the Automate object is
 * halted(), or it returns "false".  Returning "true" will just cause
 * handle() to be invoked again, so you can cleanly restart the Automaton
 * by returning from multiple points in the function.
 * <P>
 * Since handle() executes outside the GUI thread, it is important that
 * access to GUI (AWT, Swing) objects be scheduled through the
 * various service routines.
 * <P>
 * Services are provided by public member functions, described below.
 * They must only be invoked from the init and handle methods, as they
 * must be used in a delayable thread.  If invoked from the GUI thread,
 * for example, the program will appear to hang. To help ensure this,
 * a warning will be logged if they are used before the thread starts.
 * <P>
 * For general use, e.g. in scripts, the most useful functions are:
 *<UL>
 *<LI>Wait for a specific number of milliseconds: {@link #waitMsec(int)} 
 *<LI>Wait for a specific sensor to be active: {@link #waitSensorActive(jmri.Sensor)} 
 This is also available
 in a form that will wait for any of a group of sensors to be active.
 *<LI>Wait for a specific sensor to be inactive: {@link #waitSensorInactive(jmri.Sensor)} 
 This is also available
 in a form that will wait for any of a group of sensors to be inactive. 
 *<LI>Wait for a specific sensor to be in a specific state: {@link #waitSensorState(jmri.Sensor, int)} 
 *<LI>Wait for a specific sensor to change: {@link #waitSensorChange(int, jmri.Sensor)} 
 *<LI>Set a group of turnouts and wait for them to be consistent (actual position matches desired position): {@link #setTurnouts(jmri.Turnout[], jmri.Turnout[])} 
 *<LI>Wait for a group of turnouts to be consistent (actually as set): {@link #waitTurnoutConsistent(jmri.Turnout[])} 
 *<LI>Wait for any one of a number of Sensors, Turnouts and/or other objects to change: {@link #waitChange(jmri.NamedBean[])} 
 *<LI>Obtain a DCC throttle: {@link #getThrottle} 
 *<LI>Read a CV from decoder on programming track: {@link #readServiceModeCV} 
 *<LI>Write a value to a CV in a decoder on the programming track: {@link #writeServiceModeCV} 
 *<LI>Write a value to a CV in a decoder on the main track: {@link #writeOpsModeCV} 
 *</UL>
 * <P>
 * Although this is named an "Abstract" class, it's actually concrete
 * so that Jython code can easily use some of the methods.
 *
 * @author	Bob Jacobsen    Copyright (C) 2003
 * @version     $Revision: 1.30 $
 */
public class AbstractAutomaton implements Runnable {

    public AbstractAutomaton() {
    	String className = this.getClass().getName();
    	int lastdot = className.lastIndexOf(".");
    	setName(className.substring(lastdot+1, className.length()));
    }
    public AbstractAutomaton(String name) {
    	setName(name);
    }

    AutomatSummary summary = AutomatSummary.instance();

    Thread currentThread = null;
    /**
     * Start this automat processing.
     *
     * Overrides the superclass method to do local accounting.
     */
    public void start() {
        if (currentThread != null) log.error("Start with currentThread not null!");
        currentThread = new Thread(this);
        currentThread.start();
    	summary.register(this);
    	count = 0;
    }

    /**
     * Part of the implementation; not for general use.
     */
    public void run() {
        inThread = true;
        init();
        // the real processing in the next statement is in handle();
        // and the loop call is just doing accounting
        while (handle()) {
        	count++;
        	summary.loop(this);
        }
        done();
    }

    /**
     * Stop the thread immediately.
     *
     * Overrides superclass method to handle local accounting.
     */
    public void stop() {
        if (currentThread == null) log.error("Stop with currentThread null!");
        currentThread.stop();
        currentThread = null;
        done();
    }

	/**
     * Part of the internal implementation; not for general use.
     *
	 * Common internal end-time processing
	 */
	void done() {
		summary.remove(this);
	}

	private String name = null;

	private int count;

	/**
	 * Returns the number of times the handle routine has executed.
	 *
	 * Used by e.g. {@link jmri.jmrit.automat.monitor} to monitor progress
	 */
	public int getCount() {
		return count;
	}

	/**
	 * Gives access to the thread name.
	 * Used internally for e.g. {@link jmri.jmrit.automat.monitor}
	 */
	public String getName() {
		return name;
	}
	/**
     * Update the name of this object.
	 *
	 * name is not a bound parameter, so changes are not
     * notified to listeners
     * @see #getName
	 */
	public void setName(String name) {
		this.name = name;
	}

	void defaultName() {
	}

    /**
     * User-provided initialization routine.
     *
     * This is called exactly once for each object created.
     * This is where you put all the code that needs to be
     * run when your object starts up:  Finding sensors and turnouts,
     * getting a throttle, etc.
     */
    protected void init() {}

    /**
     * User-provided main routine. 
     * 
     * This is run repeatedly until
     * it signals the end by returning false.  Many automata
     * are intended to run forever, and will always return true.
     *
     * @return false to terminate the automaton, for example due to an error.
     */
    protected boolean handle() { return false; }

    /**
     * Control optional debugging prompt.  
     * If this is set true,
     * each call to wait() will prompt the user whether to continue.
     */
    protected boolean promptOnWait = false;

    /**
     * Wait for a specified number of milliseconds, and then
     * return control.
     */
    public void waitMsec( int milliseconds ) {
        long target = System.currentTimeMillis() + milliseconds;
        while (true) {
            long stillToGo = target - System.currentTimeMillis();
            if (stillToGo <= 0) {
                break;
            }
            try {
                Thread.sleep(stillToGo);
            } catch (InterruptedException e) {}
        }
    }

    /**
     * Part of the intenal implementation, not intended for users.
     * <P>
     * This handles exceptions internally,
     * so they needn't clutter up the code.  Note that the current
     * implementation doesn't guarantee the time, either high or low.
     * <P>
     * Because of the way Jython access handles synchronization, this
     * is explicitly synchronized internally.
     * @param milliseconds
     */
    protected void wait(int milliseconds){
        if (!inThread) log.debug("wait invoked from invalid context");
        synchronized(this) {
            try {
                if (milliseconds <0) {
                    super.wait();
                } else
                    super.wait(milliseconds);
            } catch (InterruptedException e) {
                // do nothing for now exception mention
                log.warn("interrupted in wait");
            }
        }
        if (promptOnWait) debuggingWait();
    }

    /**
     * Flag used to ensure that service routines
     * are only invoked in the automaton thread.
     */
    private boolean inThread = false;

    private AbstractAutomaton self = this;

    /**
     * Wait for a sensor to change state.
     * <P>
     * The current (OK) state of the Sensor is passed to avoid
     * a possible race condition. The new state is returned
     * for a similar reason.
     * <P>
     * This works by registering a listener, which is likely to
     * run in another thread.  That listener then interrupts the automaton's
     * thread, who confirms the change.
     *
     * @param mState Current state of the sensor
     * @param mSensor Sensor to watch
     * @return newly detected Sensor state
     */
    public int waitSensorChange(int mState, Sensor mSensor){
        if (!inThread) log.warn("waitSensorChange invoked from invalid context");
        if (log.isDebugEnabled()) log.debug("waitSensorChange starts: "+mSensor.getSystemName());
        // register a listener
        java.beans.PropertyChangeListener l;
        mSensor.addPropertyChangeListener(l = new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                synchronized (self) {
                    self.notifyAll(); // should be only one thread waiting, but just in case
                }
            }
        });

        int now;
        while (mState == (now = mSensor.getKnownState())) {
            wait(-1);
        }

        // remove the listener & report new state
        mSensor.removePropertyChangeListener(l);

        return now;
    }

    /**
     * Wait for a sensor to be active. (Returns immediately if already active)
     *
     * @param mSensor Sensor to watch
     */
    public void waitSensorActive(Sensor mSensor){
        if (log.isDebugEnabled()) log.debug("waitSensorActive starts");
        waitSensorState(mSensor, Sensor.ACTIVE);
        return;
    }

    /**
     * Wait for a sensor to be inactive. (Returns immediately if already inactive)
     *
     * @param mSensor Sensor to watch
     */
    public void waitSensorInactive(Sensor mSensor){
        if (log.isDebugEnabled()) log.debug("waitSensorInActive starts");
        waitSensorState(mSensor, Sensor.INACTIVE);
        return;
    }

    /**
     * Internal service routine to wait for one sensor to be
     * in (or become in) a specific state.
     * <P>
     * Used by waitSensorActive and waitSensorInactive
     * <P>
     * This works by registering a listener, which is likely to
     * run in another thread.  That listener then interrupts the automaton's
     * thread, who confirms the change.
     */
    public synchronized void waitSensorState(Sensor mSensor, int state){
        if (!inThread) log.warn("waitSensorState invoked from invalid context");
        if (mSensor.getKnownState() == state) return;
        if (log.isDebugEnabled()) log.debug("waitSensorState starts: "+mSensor.getSystemName()+" "+state);
        // register a listener
        java.beans.PropertyChangeListener l;
        mSensor.addPropertyChangeListener(l = new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                synchronized (self) {
                    self.notifyAll(); // should be only one thread waiting, but just in case
                }
            }
        });

        while (state != mSensor.getKnownState()) {
            wait(-1);  // wait for notification
        }

        // remove the listener & report new state
        mSensor.removePropertyChangeListener(l);

        return;
    }
    
    /**
     * Wait for one of a list of sensors to be be inactive.
     */
    public void waitSensorInactive(Sensor[] mSensors){
        if (log.isDebugEnabled()) log.debug("waitSensorInactive[] starts");
        waitSensorState(mSensors, Sensor.INACTIVE);    
    }
    
    /**
     * Wait for one of a list of sensors to be be active.
     */
    public void waitSensorActive(Sensor[] mSensors){
        if (log.isDebugEnabled()) log.debug("waitSensorActive[] starts");
        waitSensorState(mSensors, Sensor.ACTIVE);    
    }
    
    /**
     * Wait for one of a list of sensors to be be in a selected state.
     * <P>
     * This works by registering a listener, which is likely to
     * run in another thread.  That listener then interrupts the automaton's
     * thread, who confirms the change.
     *
     * @param mSensors Array of sensors to watch
     * @param state State to check (static value from jmri.Sensors)
     */
    public synchronized void waitSensorState(Sensor[] mSensors, int state){
        if (!inThread) log.warn("waitSensorState invoked from invalid context");
        if (log.isDebugEnabled()) log.debug("waitSensorState[] starts");

        // do a quick check first, just in case
        if (checkForState(mSensors, state)) {
            log.debug("returns immediately");
            return;
        }
        // register listeners
        int i;
        java.beans.PropertyChangeListener[] listeners =
                new java.beans.PropertyChangeListener[mSensors.length];
        for (i=0; i<mSensors.length; i++) {

            mSensors[i].addPropertyChangeListener(listeners[i] = new java.beans.PropertyChangeListener() {
                public void propertyChange(java.beans.PropertyChangeEvent e) {
                    synchronized (self) {
                        log.debug("notify waitSensorState[] of property change");
                        self.notifyAll(); // should be only one thread waiting, but just in case
                    }
                }
            });

        }

        while (!checkForState(mSensors, state)) {
            wait(-1);
        }

        // remove the listeners
        for (i=0; i<mSensors.length; i++) {
            mSensors[i].removePropertyChangeListener(listeners[i]);
        }

        return;
    }

    /**
     * Wait for a list of turnouts to all be in a consistent state
     * <P>
     * This works by registering a listener, which is likely to
     * run in another thread.  That listener then interrupts the automaton's
     * thread, who confirms the change.
     *
     * @param mTurnouts list of turnouts to watch
     */
    public synchronized void waitTurnoutConsistent(Turnout[] mTurnouts){
        if (!inThread) log.warn("waitTurnoutConsistent invoked from invalid context");
        if (log.isDebugEnabled()) log.debug("waitTurnoutConsistent[] starts");

        // do a quick check first, just in case
        if (checkForConsistent(mTurnouts)) {
            log.debug("returns immediately");
            return;
        }
        // register listeners
        int i;
        java.beans.PropertyChangeListener[] listeners =
                new java.beans.PropertyChangeListener[mTurnouts.length];
        for (i=0; i<mTurnouts.length; i++) {

        	mTurnouts[i].addPropertyChangeListener(listeners[i] = new java.beans.PropertyChangeListener() {
                public void propertyChange(java.beans.PropertyChangeEvent e) {
                    synchronized (self) {
                        log.debug("notify waitTurnoutConsistent[] of property change");
                        self.notifyAll(); // should be only one thread waiting, but just in case
                    }
                }
            });

        }

        while (!checkForConsistent(mTurnouts)) {
            wait(-1);
        }

        // remove the listeners
        for (i=0; i<mTurnouts.length; i++) {
        	mTurnouts[i].removePropertyChangeListener(listeners[i]);
        }

        return;
    }

    /**
     * Convenience function to set a bunch of turnouts and wait until they are all
     * in a consistent state
     * @param closed	turnouts to set to closed state
     * @param thrown	turnouts to set to thrown state
     */
    public void setTurnouts(Turnout[] closed, Turnout[] thrown) {
    	Turnout[] turnouts = new Turnout[closed.length + thrown.length];
    	int ti = 0;
    	for (int i=0; i<closed.length; ++i) {
    		turnouts[ti++] = closed[i];
    		closed[i].setCommandedState(Turnout.CLOSED);
    	}
    	for (int i=0; i<thrown.length; ++i) {
    		turnouts[ti++] = thrown[i];
    		thrown[i].setCommandedState(Turnout.THROWN);
    	}
    	waitTurnoutConsistent(turnouts);
    }
    
    /**
     * Wait for one of a list of NamedBeans (sensors, signal heads and/or turnouts) to change.
     * <P>
     * This works by registering a listener, which is likely to
     * run in another thread.  That listener then interrupts the automaton's
     * thread, who confirms the change.
     *
     * @param mInputs Array of NamedBeans to watch
     */
    public synchronized void waitChange(NamedBean[] mInputs){
        if (!inThread) log.warn("waitChange invoked from invalid context");
        if (log.isDebugEnabled()) log.debug("waitChange[] starts");

        // register listeners
        int i;
        java.beans.PropertyChangeListener[] listeners =
                new java.beans.PropertyChangeListener[mInputs.length];
        for (i=0; i<mInputs.length; i++) {

            mInputs[i].addPropertyChangeListener(listeners[i] = new java.beans.PropertyChangeListener() {
                public void propertyChange(java.beans.PropertyChangeEvent e) {
                    synchronized (self) {
                        log.debug("notify waitChange[] of property change "+e.getPropertyName()+" from "+((NamedBean)e.getSource()).getSystemName());
                        self.notifyAll(); // should be only one thread waiting, but just in case
                    }
                }
            });

        }

        // wait for notify
        wait(-1);

        // remove the listeners
        for (i=0; i<mInputs.length; i++) {
            mInputs[i].removePropertyChangeListener(listeners[i]);
        }

        return;
    }

    /**
     * Wait for one of an array of sensors to change.
     * <P>
     * This is an older method, now superceded by waitChange, which can wait
     * for any NamedBean.
     *
     * @param mSensors Array of sensors to watch
     */
    public synchronized void waitSensorChange(Sensor[] mSensors){
        waitChange(mSensors);
       return;
    }

    /**
     * Check an array of sensors to see if any are in a specific state
     * @param mSensors Array to check
     * @return true if any are ACTIVE
     */
    private boolean checkForState(Sensor[] mSensors, int state) {
        for (int i=0; i<mSensors.length; i++) {
            if (mSensors[i].getKnownState() == state) return true;
        }
        return false;
    }

    private boolean checkForConsistent(Turnout[] mTurnouts) {
    	for (int i=0; i<mTurnouts.length; ++i) {
    		if (!mTurnouts[i].isConsistentState()) {
    			return false;
    		}
    	}
    	return true;
    }
    
    private DccThrottle throttle;
    /**
     * Obtains a DCC throttle, including waiting for the command station response.
     * @param address
     * @param longAddress true if this is a long address, false for a short address
     * @return A usable throttle, or null if error
     */
    public DccThrottle getThrottle(int address, boolean longAddress) {
        if (!inThread) log.warn("getThrottle invoked from invalid context");
        throttle = null;
        boolean ok = true;
        ok = InstanceManager.throttleManagerInstance()
                .requestThrottle(address,new ThrottleListener() {
                    public void notifyThrottleFound(DccThrottle t) {
                        throttle = t;
                        synchronized (self) {
                            self.notifyAll(); // should be only one thread waiting, but just in case
                        }
                    }
                });
                
        // check if reply is coming
        if (!ok) {
        	log.info("Throttle for loco "+address+" not available");
        	return null;
        }
        
        // now wait for reply from identified throttle
        while (throttle == null) {
            log.debug("waiting for throttle");
            wait(10000);
            if (throttle == null) log.warn("Still waiting for throttle "+address+"!");
        }
        return throttle;
    }

    /**
     * Write a CV on the service track, including waiting for completion.
     * @param CV Number 1 through 512
     * @param value
     * @return true if completed OK
     */
    public boolean writeServiceModeCV(int CV, int value) {
        // get service mode programmer
        Programmer programmer = InstanceManager.programmerManagerInstance()
                        .getServiceModeProgrammer();

        // do the write, response will wake the thread
        try {
            programmer.writeCV(CV, value, new ProgListener() {
                public void programmingOpReply(int value, int status) {
                    synchronized (self) { 
                        self.notifyAll(); // should be only one thread waiting, but just in case
                    }
                }
            });
        } catch (ProgrammerException e) {
            log.warn("Exception during writeServiceModeCV: "+e);
            return false;
        }
        // wait for the result
        wait(-1);

        return true;
    }

    private volatile int cvReturnValue;
    private volatile int cvReturnStatus;

    /**
     * Read a CV on the service track, including waiting for completion.
     * @param CV Number 1 through 512
     * @return -1 if error, else value
     */
    public int readServiceModeCV(int CV) {
        // get service mode programmer
        Programmer programmer = InstanceManager.programmerManagerInstance()
                        .getServiceModeProgrammer();

        // do the write, response will wake the thread
        cvReturnValue = -1;
        try {
            programmer.readCV(CV, new ProgListener() {
                public void programmingOpReply(int value, int status) {
                    cvReturnValue = value;
                    cvReturnStatus = status;
                    synchronized (self) { 
                        self.notifyAll(); // should be only one thread waiting, but just in case
                    }
                }
            });
        } catch (ProgrammerException e) {
            log.warn("Exception during writeServiceModeCV: "+e);
            return -1;
        }
        // wait for the result
        wait(-1);
        return cvReturnValue;
    }

    /**
     * Write a CV in ops mode, including waiting for completion.
     * @param CV Number 1 through 512
     * @param value
     * @param loco   Locomotive decoder address
     * @param longAddress true is the locomotive is using a long address
     * @return true if completed OK
     */
    public boolean writeOpsModeCV(int CV, int value, boolean longAddress, int loco) {
        // get service mode programmer
        Programmer programmer = InstanceManager.programmerManagerInstance()
                        .getOpsModeProgrammer(longAddress, loco);

        // do the write, response will wake the thread
        try {
            programmer.writeCV(CV, value, new ProgListener() {
                public void programmingOpReply(int value, int status) {
                    synchronized (self) { 
                        self.notifyAll(); // should be only one thread waiting, but just in case
                    }
                }
            });
        } catch (ProgrammerException e) {
            log.warn("Exception during writeServiceModeCV: "+e);
            return false;
        }
        // wait for the result
        wait(-1);

        return true;
    }

    JFrame messageFrame = null;
    String message = null;

    /**
     * Internal class to show a Frame
     */
    public class MsgFrame implements Runnable {
        String mMessage;
        boolean mPause;
        boolean mShow;
        JFrame mFrame = null;
        JButton mButton;
        JTextArea mArea;

        public void hide() {
            mShow = false;
            // invoke the operation
            javax.swing.SwingUtilities.invokeLater(this);
        }

        /**
         * Show a message in the message frame, and optionally wait for the user to acknowledge
         */
        public void show(String pMessage, boolean pPause) {
            mMessage = pMessage;
            mPause = pPause;
            mShow = true;

            // invoke the operation
            javax.swing.SwingUtilities.invokeLater(this);
            // wait to proceed?
            if (mPause) {
                synchronized(self) {
                    try {
                        self.wait();
                    }  catch (InterruptedException e) {
                        log.warn("Interrupted during pause, not expected");
                    }
                }
            }
        }
        
        
        public void run() {
            // create the frame if it doesn't exist
            if (mFrame==null) {
                mFrame = new JFrame("");
                mArea = new JTextArea();
                mArea.setEditable(false);
                mArea.setLineWrap(false);
                mArea.setWrapStyleWord(true);
                mButton = new JButton("Continue");
                mFrame.getContentPane().setLayout(new BorderLayout());
                mFrame.getContentPane().add(mArea, BorderLayout.CENTER );
                mFrame.getContentPane().add(mButton, BorderLayout.SOUTH );
                mButton.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        synchronized (self) {
                            self.notifyAll(); // should be only one thread waiting, but just in case
                        }
                        mFrame.hide();
                    }
                });
                mFrame.pack();
            }
            if (mShow) {
                // update message, show button if paused
                mArea.setText(mMessage);
                if (mPause) {
                    mButton.setVisible(true);
                } else {
                    mButton.setVisible(false);
                }
                // do optional formatting
                format();
                // center the frame
                mFrame.pack();
                Dimension screen = mFrame.getContentPane().getToolkit().getScreenSize();
                Dimension size = mFrame.getSize();
                mFrame.setLocation((screen.width-size.width)/2,(screen.height-size.height)/2);
                // and show it to the user
                mFrame.show();
            }
            else mFrame.hide();
        }

        /**
         * Abstract method to handle formatting of the text on a show
         */
        protected void format() {}
    }

    JFrame debugWaitFrame = null;

    /**
     * Wait for the user to OK moving forward. This is complicated
     * by not running in the GUI thread, and by not wanting to use
     * a modal dialog.
     */
    private void debuggingWait() {
        // post an event to the GUI pane
        Runnable r = new Runnable() {
            public void run() {
                // create a prompting frame
                if (debugWaitFrame==null) {
                    debugWaitFrame = new JFrame("Automaton paused");
                    JButton b = new JButton("Continue");
                    debugWaitFrame.getContentPane().add(b);
                    b.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            synchronized (self) {
                                self.notifyAll(); // should be only one thread waiting, but just in case
                            }
                            debugWaitFrame.hide();
                        }
                    });
                    debugWaitFrame.pack();
                }
                debugWaitFrame.show();
            }
        };
        javax.swing.SwingUtilities.invokeLater(r);
        // wait to proceed
        try {
            super.wait();
        }  catch (InterruptedException e) {
            log.warn("Interrupted during debugging wait, not expected");
        }
    }
    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AbstractAutomaton.class.getName());
}

/* @(#)AbstractAutomaton.java */
