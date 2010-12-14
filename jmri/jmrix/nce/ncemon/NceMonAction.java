/**
 * NceMonAction.java
 *
 * Description:		Swing action to create and register a
 *       			NceMonFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version
 */

package jmri.jmrix.nce.ncemon;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

import jmri.jmrix.nce.NceTrafficController;


@Deprecated
public class NceMonAction 			extends AbstractAction {

    private NceTrafficController tc = null;
    
    public NceMonAction(NceTrafficController t, String s) {
    	super(s);
    	this.tc = t;
	}
    
    public NceMonAction(String s) { super(s);}
    
    public NceMonAction() { this("NCE message monitor");}

    public void actionPerformed(ActionEvent e) {
		// create a NceMonFrame
		NceMonFrame f = new NceMonFrame(tc);
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.warn("NceMonAction starting NceMonFrame: Exception: "+ex.toString());
			}
		f.setVisible(true);
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(NceMonAction.class.getName());

}


/* @(#)NceMonAction.java */
