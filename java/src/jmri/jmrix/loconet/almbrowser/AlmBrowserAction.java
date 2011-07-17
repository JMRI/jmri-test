// AlmBrowserAction.java

package jmri.jmrix.loconet.almbrowser;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

import jmri.jmrix.loconet.LnTrafficController;


/** Swing action to create and register a
 * AlmBrowser object

 * @author			Bob Jacobsen    Copyright (C) 2002
 * @version			$Version:$
 */
public class AlmBrowserAction 			extends AbstractAction {

	public AlmBrowserAction(String s) { super(s);}

    public void actionPerformed(ActionEvent e) {
		// create a AlmBrowser
		AlmBrowserFrame f = new AlmBrowserFrame();
//		try {
			f.initComponents();
//			}
//		catch (Exception ex) {
//			log.warn("AlmBrowserAction starting AlmBrowser: Exception: "+ex.toString());
//			}
		// connect to the LnTrafficController
		f.connect(LnTrafficController.instance());
                // make visible
		f.setVisible(true);

	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AlmBrowserAction.class.getName());

}


/* @(#)AlmBrowserAction.java */
