/**
 * SimpleTurnoutCtrlAction.java
 *
 * Description:		Swing action to create and register a
 *       			SimpleTurnoutCtrlFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version
 */

package jmri.jmrit.simpleturnoutctrl;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class SimpleTurnoutCtrlAction 			extends AbstractAction {

    public SimpleTurnoutCtrlAction(String s) { 
	super(s);

	// disable ourself if there is no primary turnout manager available
        if (jmri.InstanceManager.turnoutManagerInstance()==null ||
            (((jmri.managers.AbstractProxyManager)jmri.InstanceManager
                                                 .turnoutManagerInstance())
                                                 .systemLetter()=='\0')) {
            setEnabled(false);
        }



    }
    public SimpleTurnoutCtrlAction() { this("Turnouts");}

    public void actionPerformed(ActionEvent e) {

		SimpleTurnoutCtrlFrame f = new SimpleTurnoutCtrlFrame();
		f.setVisible(true);

	}
   static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SimpleTurnoutCtrlAction.class.getName());
}


/* @(#)SimpleTurnoutCtrlAction.java */
