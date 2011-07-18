// LogixLoadAction.java

package jmri.jmrit;

import jmri.InstanceManager;

import java.awt.event.*;
import java.util.*;

import javax.swing.*;

/**
 * Provide an action to allow Logixs to be loaded disabled when panel file is loaded
 *
 * @author	Dave Duchamp   Copyright (C) 2007
 * @version	$Revision$
 */
public class LogixLoadAction extends AbstractAction {

    public LogixLoadAction(String s, JPanel who) {
        super(s);
        _who = who;
    }

	ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.JmritDebugBundle");
	
    JPanel _who;

    public void actionPerformed(ActionEvent e) {
		// Set option to force Logixs to be loaded disabled
        
        Object[] options = {"Disable",
                    "Enable"};

        int retval = JOptionPane.showOptionDialog(_who, rb.getString("LogixDisabledMessage"), rb.getString("DebugOption"),
                                                  JOptionPane.YES_NO_OPTION,
                                                  JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        if (retval != 0) {
            InstanceManager.logixManagerInstance().setLoadDisabled(false);
            log.error("Requested load Logixs enabled via Debug menu.");
        } else {
            InstanceManager.logixManagerInstance().setLoadDisabled(true);
            log.error("Requested load Logixs diabled via Debug menu.");
        }

    }

    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LogixLoadAction.class.getName());
}
