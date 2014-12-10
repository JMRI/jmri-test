// LcdClockAction.java

 package jmri.jmrit.lcdclock;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;

/**
 * Swing action to create and register a
 *  LcdClockFrame object
 *
 * @author			Ken Cameron    Copyright (C) 2007
 * @version			$Revision$
 * 
 * This was a direct steal form the Nixie clock code, ver 1.5.
 * Thank you Bob Jacobsen.
 */
@ActionID(
        id = "jmri.jmrit.lcdclock.LcdClockAction",
        category = "JMRI"
)
@ActionRegistration(
        iconBase = "org/jmri/core/ui/toolbar/generic.gif",
        displayName = "jmri.jmrit.Bundle#MenuItemLcdClock",
        iconInMenu = false
)
@ActionReference(
        path = "Menu/Tools/Clocks",
        position = 620
)
 public class LcdClockAction extends AbstractAction {
	 
	 public LcdClockAction() {
         this("LCD Clock");
     }

 	public LcdClockAction(String s) {
    	super(s);
     }

     public void actionPerformed(ActionEvent e) {

         LcdClockFrame f = new LcdClockFrame();
         f.setVisible(true);

     }

 }

/* @(#)LcdClockAction.java */
