// TrainsByCarTypeAction.java

package jmri.jmrit.operations.trains;

import java.awt.event.ActionEvent;
import java.awt.Frame;
import javax.swing.AbstractAction;

/**
 * Swing action to create and register a TrainsByCarTypeFrame object.
 * 
 * @author Daniel Boudreau Copyright (C) 2009
 * @version $Revision$
 */
public class TrainsByCarTypeAction extends AbstractAction {
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 2521837006549809639L;
	public TrainsByCarTypeAction(String s) {
    	super(s);
    }
    
    public TrainsByCarTypeAction(){
    	super(Bundle.getMessage("TitleModifyTrains"));
    }

    TrainsByCarTypeFrame f = null;
    public void actionPerformed(ActionEvent e) {
        // create a frame
    	if (f == null || !f.isVisible()){
    		f = new TrainsByCarTypeFrame();
    		f.initComponents("");
     	}
    	f.setExtendedState(Frame.NORMAL);
	   	f.setVisible(true);	// this also brings the frame into focus
    }
}

/* @(#)TrainsByCarTypeAction.java */
