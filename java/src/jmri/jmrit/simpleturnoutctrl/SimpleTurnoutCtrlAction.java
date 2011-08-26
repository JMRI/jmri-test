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
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.WindowInterface;
import javax.swing.Icon;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class SimpleTurnoutCtrlAction 			extends JmriAbstractAction {

    public SimpleTurnoutCtrlAction(String s, WindowInterface wi) {
    	super(s, wi);
    }
     
 	public SimpleTurnoutCtrlAction(String s, Icon i, WindowInterface wi) {
    	super(s, i, wi);
    }

    public SimpleTurnoutCtrlAction(String s) { 
	super(s);

	// disable ourself if there is no primary turnout manager available
        if (jmri.InstanceManager.turnoutManagerInstance()==null ) {
            setEnabled(false);
        }



    }
    public SimpleTurnoutCtrlAction() { this("Turnouts");}

    public void actionPerformed(ActionEvent e) {

		SimpleTurnoutCtrlFrame f = new SimpleTurnoutCtrlFrame();
		f.setVisible(true);

	}
    
    // never invoked, because we overrode actionPerformed above
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }
    
   static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SimpleTurnoutCtrlAction.class.getName());
}


/* @(#)SimpleTurnoutCtrlAction.java */
