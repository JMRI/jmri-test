// SRCPMonAction.java

package jmri.jmrix.srcp.srcpmon;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;


/**
 * Swing action to create and register a
 *       			SRCPMonFrame object
 * 
 * @author Bob Jacobsen    Copyright (C) 2008
 * @version $Revision: 1.2 $
 */
public class SRCPMonAction 			extends AbstractAction {

	public SRCPMonAction() { super("SRCP Monitor");}
	public SRCPMonAction(String s) { super(s);}

    public void actionPerformed(ActionEvent e) {
		// create a SRCPMonFrame
		SRCPMonFrame f = new SRCPMonFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.warn("SRCPMonAction starting SRCPMonFrame: Exception: "+ex.toString());
			}
		f.setVisible(true);
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SRCPMonAction.class.getName());

}


/* @(#)SRCPMonAction.java */
