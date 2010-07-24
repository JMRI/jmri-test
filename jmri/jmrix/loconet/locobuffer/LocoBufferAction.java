/** 
 * LocoBufferAction.java
 *
 * Description:		Swing action to create and register a 
 *       			LocoBufferFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			
 */

package jmri.jmrix.loconet.locobuffer;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
@Deprecated 
public class LocoBufferAction 			extends AbstractAction {

	public LocoBufferAction(String s) { super(s);}
	
    public void actionPerformed(ActionEvent e) {
		// create a LocoBufferFrame
		LocoBufferFrame f = new LocoBufferFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.error("starting LocoBufferFrame caught exception: "+ex.toString());
			}
		f.setVisible(true);			
	}
 
   static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LocoBufferAction.class.getName());

}


/* @(#)LnHexFileAction.java */
