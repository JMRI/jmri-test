// TrainsScheduleAction.java

package jmri.jmrit.operations.trains;

import java.awt.event.ActionEvent;
import java.awt.Frame;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register a TrainsScheduleTableFrame object.
 * 
 * @author Daniel Boudreau Copyright (C) 2010
 * @version $Revision: 1.1 $
 */
public class TrainsScheduleAction extends AbstractAction {
    static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.trains.JmritOperationsTrainsBundle");

    public TrainsScheduleAction(String s) {
    	super(s);
    }

    TrainsScheduleTableFrame f = null;
    public void actionPerformed(ActionEvent e) {
        // create a frame
    	if (f == null || !f.isVisible()){
    		f = new TrainsScheduleTableFrame();
     	}
    	f.setExtendedState(Frame.NORMAL);
   		f.setVisible(true);
    }
}

/* @(#)TrainsScheduleAction.java */
