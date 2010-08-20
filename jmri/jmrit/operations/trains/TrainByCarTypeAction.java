//TrainByCarTypeAction.java

package jmri.jmrit.operations.trains;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register a TrainByCarTypeFrame.
 * 
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2010
 * @version $Revision: 1.1 $
 */
public class TrainByCarTypeAction extends AbstractAction {
	static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.trains.JmritOperationsTrainsBundle");

	public TrainByCarTypeAction(String s, TrainEditFrame frame) {
		super(s);
		this.frame = frame;
	}
	
	TrainEditFrame frame;

	public void actionPerformed(ActionEvent e) {
		// create frame
		TrainByCarTypeFrame f = new TrainByCarTypeFrame();
		f.initComponents(frame._train);
	}
}

/* @(#)TrainByCarTypeAction.java */
