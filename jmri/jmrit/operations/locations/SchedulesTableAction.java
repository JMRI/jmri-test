// SchedulesTableAction.java

package jmri.jmrit.operations.locations;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register a ScheduleTableFrame object.
 * 
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2009
 * @version $Revision: 1.1 $
 */
public class SchedulesTableAction extends AbstractAction {
    static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.locations.JmritOperationsLocationsBundle");

    public SchedulesTableAction(String s) {
    	super(s);
    }

    SchedulesTableFrame f = null;
    public void actionPerformed(ActionEvent e) {
        // create a schedule table frame
    	if (f == null || !f.isVisible()){
    		f = new SchedulesTableFrame();
     	}
    	f.setExtendedState(f.NORMAL);
   		f.setVisible(true);
    }
}

/* @(#)SchedulesTableAction.java */
