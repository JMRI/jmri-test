//SprogUpdateAction.java

package jmri.jmrix.sprog.update;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register a
 *       			SprogIIUpdateFrame object
 *
 * @author			Andrew crosland    Copyright (C) 2004
 * @version			$Revision: 1.2 $
 */

public class SprogUpdateAction 	extends AbstractAction {

  public SprogUpdateAction(String s) { super(s);}

  public void actionPerformed(ActionEvent e) {
  }

  static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SprogUpdateAction.class.getName());

}


/* @(#)SprogUpdateAction.java */
