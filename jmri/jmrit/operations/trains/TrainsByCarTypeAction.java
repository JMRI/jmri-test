// TrainsByCarTypeAction.java

package jmri.jmrit.operations.trains;

import java.awt.event.ActionEvent;
import java.awt.Frame;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register a TrainsByCarTypeFrame object.
 * 
 * @author Daniel Boudreau Copyright (C) 2009
 * @version $Revision: 1.2 $
 */
public class TrainsByCarTypeAction extends AbstractAction {
    static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.trains.JmritOperationsTrainsBundle");

    public TrainsByCarTypeAction(String s) {
    	super(s);
    }
    
    public TrainsByCarTypeAction(){
    	super(rb.getString("TitleModifyTrains"));
    }

    TrainsByCarTypeFrame f = null;
    public void actionPerformed(ActionEvent e) {
        // create a frame
    	if (f == null || !f.isVisible()){
    		f = new TrainsByCarTypeFrame();
    		f.initComponents("");
     	}
    	f.setExtendedState(Frame.NORMAL);
   		f.setVisible(true);
    }
}

/* @(#)TrainsByCarTypeAction.java */
