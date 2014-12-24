// ChangeDepartureTimesAction.java

package jmri.jmrit.operations.trains;

import java.awt.event.ActionEvent;
import java.awt.Frame;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register a TrainCopyFrame object.
 * 
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2013
 * @version $Revision: 17977 $
 */
public class ChangeDepartureTimesAction extends AbstractAction {

    /**
	 * 
	 */
	private static final long serialVersionUID = -1524083857693353335L;
	public ChangeDepartureTimesAction(String s) {
    	super(s);
    }

    ChangeDepartureTimesFrame f = null;
    public void actionPerformed(ActionEvent e) {
        // create a copy train frame
    	if (f == null || !f.isVisible()){
    		f = new ChangeDepartureTimesFrame();
    	}
    	f.setExtendedState(Frame.NORMAL);
	   	f.setVisible(true);	// this also brings the frame into focus
    }
}

/* @(#)ChangeDepartureTimesAction.java */
