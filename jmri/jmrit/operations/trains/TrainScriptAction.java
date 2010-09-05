// TrainScriptAction.java

package jmri.jmrit.operations.trains;

import java.awt.event.ActionEvent;
import java.awt.Frame;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register a TrainScriptFrame.
 * 
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2010
 * @version $Revision: 1.2 $
 */
public class TrainScriptAction extends AbstractAction {
    static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.trains.JmritOperationsTrainsBundle");

    public TrainScriptAction(String s, TrainEditFrame frame) {
    	super(s);
    	this.frame = frame;
    }
    
    TrainEditFrame frame;	// the parent frame that is launching the TrainScriptFrame.

    TrainScriptFrame f = null;
    public void actionPerformed(ActionEvent e) {
        // create a train scipts frame
    	if (f != null && f.isVisible()){
    		f.dispose();
    	}
    	f = new TrainScriptFrame();
    	f.setLocation(frame.getLocation());
    	f.initComponents(frame);
    	f.setExtendedState(Frame.NORMAL);  	
    	f.setTitle(rb.getString("MenuItemScripts"));
    }
}

/* @(#)TrainScriptAction.java */
