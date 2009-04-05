/**
 * LocoMonAction.java
 *
 * Description:		Swing action to create and register a
 *       			LocoMonFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version
 */

package jmri.jmrix.loconet.locomon;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class LocoMonAction 			extends AbstractAction {

	public LocoMonAction(String s) { super(s);}

    public LocoMonAction() {
        this("LocoNet monitor");
    }

    public void actionPerformed(ActionEvent e) {
		// create a LocoMonFrame
                log.debug("starting LocoMon frame creation");
		LocoMonFrame f = new LocoMonFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.warn("LocoMonAction starting LocoMonFrame: Exception: "+ex.toString());
			}
		f.setVisible(true);

	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LocoMonAction.class.getName());

}


/* @(#)LocoMonAction.java */
