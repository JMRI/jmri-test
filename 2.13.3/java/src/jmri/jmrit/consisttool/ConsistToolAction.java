// ConsistToolAction.java

package jmri.jmrit.consisttool;

import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.WindowInterface;
import javax.swing.Icon;

import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a
 * ConsistToolFrame object
 *
 * @author              Paul Bender Copyright (C) 2003
 * @version             $Revision$
 */
 public class ConsistToolAction extends JmriAbstractAction {

 
    public ConsistToolAction(String s, WindowInterface wi) {
    	super(s, wi);
    }
     
 	public ConsistToolAction(String s, Icon i, WindowInterface wi) {
    	super(s, i, wi);
    }
    
    public ConsistToolAction(String s) {
        super(s);

	// disable ourself if there is no consist manager available
        if (jmri.InstanceManager.consistManagerInstance()==null) {
            setEnabled(false);
        }

    }

    public ConsistToolAction() { this("Consist Tool");}

    public void actionPerformed(ActionEvent e) {

		ConsistToolFrame f = new ConsistToolFrame();
		f.setVisible(true);

	}
    
    // never invoked, because we overrode actionPerformed above
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }
   static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ConsistToolAction.class.getName());
}


/* @(#)ConsistToolAction.java */
