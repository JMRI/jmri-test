/**
 * SerialMonAction.java
 *
 * Description:		Swing action to create and register a
 *       			SerialMonFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			$Id: SerialMonAction.java,v 1.2 2003-05-11 02:00:34 jacobsen Exp $
 */

package jmri.jmrix.cmri.serial.serialmon;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;


public class SerialMonAction 			extends AbstractAction {

	public SerialMonAction(String s) { super(s);}

    public SerialMonAction() {
        this("C/MRI monitor");
    }

    public void actionPerformed(ActionEvent e) {
		// create a SerialMonFrame
		SerialMonFrame f = new SerialMonFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.warn("SerialMonAction starting SerialMonFrame: Exception: "+ex.toString());
			}
		f.show();
	}

	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialMonAction.class.getName());

}


/* @(#)SerialMonAction.java */
