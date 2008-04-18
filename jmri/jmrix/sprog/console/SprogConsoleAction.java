// SprogConsoleAction.java

package jmri.jmrix.sprog.console;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register a SprogConsoleFrame object
 *
 * @author			Andrew Crosland    Copyright (C) 2008
 * @version			$Revision: 1.1 $
 */
public class SprogConsoleAction extends AbstractAction {

	public SprogConsoleAction(String s) { super(s);}

    public void actionPerformed(ActionEvent e) {
		SprogConsoleFrame f = new SprogConsoleFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.error("Exception: "+ex.toString());
			}
		f.setVisible(true);
	}
   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SprogConsoleAction.class.getName());
}


/* @(#)SprogConsoleAction.java */
