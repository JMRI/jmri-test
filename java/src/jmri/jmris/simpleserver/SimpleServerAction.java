// SimpleServerAction.java

package jmri.jmris.simpleserver;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a
 * SimpleServerControlFrame object
 *
 * @author              Paul Bender Copyright (C) 2010
 * @version             $Revision$
 */
 public class SimpleServerAction extends AbstractAction {

    public SimpleServerAction(String s) {
	super(s);
        }

    public SimpleServerAction() { this("Start Simple Jmri Server");}

    public void actionPerformed(ActionEvent e) {

		// SimpleServerFrame f = new SimpleServerFrame();
		// f.setVisible(true);
                SimpleServer.instance().start();
	}
   static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SimpleServerAction.class.getName());
}


/* @(#)SimpleServerAction.java */
