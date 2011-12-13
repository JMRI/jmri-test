package jmri.jmrit.withrottle;

/**
 *  WiThrottle GUI
 *
 *  @author Brett Hoffman   Copyright (C) 2009
 *  @version $Revision$
 *
 */

import java.awt.event.ActionEvent;

import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.WindowInterface;
import javax.swing.Icon;

public class WiThrottleCreationAction extends JmriAbstractAction{

    public WiThrottleCreationAction(String s, WindowInterface wi) {
    	super(s, wi);
    }
     
 	public WiThrottleCreationAction(String s, Icon i, WindowInterface wi) {
    	super(s, i, wi);
    }

    static UserInterface UI;

    /**
     * Create a new network server.
     * @param name Labels frame in GUI
     */
    public WiThrottleCreationAction(String name) {
        super(name);
        if (jmri.InstanceManager.throttleManagerInstance()==null) {
            setEnabled(false);
        }         
    }

    /**
     * Create a new network server.
     */
    public WiThrottleCreationAction() {
        this("Start WiThrottle");
    }

    /**
     * Start the server end of WiThrottle.
     * @param e The event causing the action.
     */
    public void actionPerformed(ActionEvent e) {
    	if (UI == null){    //  Only allow one to be created
            UI = new UserInterface();
        }
    }

    // never invoked, because we overrode actionPerformed above
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(WiThrottleCreationAction.class.getName());

}