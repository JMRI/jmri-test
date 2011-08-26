// SpeedometerAction.java

package jmri.jmrit.speedometer;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.WindowInterface;
import javax.swing.Icon;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a
 * SpeedometerFrame
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			$Revision$
 */

public class SpeedometerAction 			extends JmriAbstractAction {

    public SpeedometerAction(String s, WindowInterface wi) {
    	super(s, wi);
    }
     
 	public SpeedometerAction(String s, Icon i, WindowInterface wi) {
    	super(s, i, wi);
    }

    public SpeedometerAction(String s) {
        super(s);

	// disable ourself if there is no primary sensor manager available
        if (jmri.InstanceManager.sensorManagerInstance()==null) {
            setEnabled(false);
        }
    }

    public SpeedometerAction() {
        this("Speedometer");
    }

    public void actionPerformed(ActionEvent e) {

        // create a SimpleProgFrame
        SpeedometerFrame f = new SpeedometerFrame();
        f.setVisible(true);

    }
    
    // never invoked, because we overrode actionPerformed above
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }
}

/* @(#)SpeedometerAction.java */
