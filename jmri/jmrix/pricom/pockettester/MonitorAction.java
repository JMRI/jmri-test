// pricom.pockettester.MonitorAction.java

package jmri.jmrix.pricom.pockettester;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a
 * pricom.pockettester.MonitorAction object
 *
 * @see jmri.jmrix.pricom.pockettester.MonitorFrame
 *
 * @author			Bob Jacobsen    Copyright (C) 2002,2004
 * @version			$Revision: 1.1 $
 */
public class MonitorAction extends AbstractAction  {

    public MonitorAction(String s) { super(s);}
    public MonitorAction() {
        this("PRICOM Pocket Tester");
    }

    public void actionPerformed(ActionEvent e) {
		// create a SerialDriverFrame
		MonitorFrame f = new MonitorFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.error("starting MonitorFrame caught exception: "+ex.toString());
			}
		f.show();
	};

   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(MonitorAction.class.getName());

}


/* @(#)MonitorAction.java */
