//TrainManifestOptionAction.java

package jmri.jmrit.operations.trains;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;


/**
 * Swing action to load the train manifest options frame.
 * 
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2010
 * @version $Revision$
 */
public class TrainManifestOptionAction extends AbstractAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5997184366069609071L;
	TrainEditFrame frame = null;

    public TrainManifestOptionAction(String s, TrainEditFrame frame) {
    	super(s);
    	this.frame = frame;
    }

    TrainManifestOptionFrame f = null;
    public void actionPerformed(ActionEvent e) {
        // create a settings frame
    	if (f == null || !f.isVisible()){
    		f = new TrainManifestOptionFrame();
    		f.initComponents(frame);
    	}
        f.setExtendedState(Frame.NORMAL);
	   	f.setVisible(true);	// this also brings the frame into focus
    }
    
	static Logger log = LoggerFactory
	.getLogger(TrainManifestOptionAction.class.getName());
}

/* @(#)TrainManifestOptionAction.java */
