// JmriServerAction.java

package jmri.jmris;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a
 * JmriServerControlFrame object
 *
 * @author              Paul Bender Copyright (C) 2010
 * @version             $Revision: 1.1 $
 */
 public class JmriServerAction extends AbstractAction {

    public JmriServerAction(String s) {
	super(s);
        }

    public JmriServerAction() { this("Start Jmri Server");}

    public void actionPerformed(ActionEvent e) {

		JmriServerFrame f = new JmriServerFrame();
		f.setVisible(true);

	}
   static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(JmriServerAction.class.getName());
}


/* @(#)JmriServerAction.java */
