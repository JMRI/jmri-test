// LnTcpDriverAction.java

package jmri.jmrix.loconet.loconetovertcp;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register a
 * LnTcpDriverFrame object.
 *
 * @author			Bob Jacobsen    Copyright (C) 2003
 * @version			$Revision: 1.5 $
 */
public class LnTcpDriverAction extends AbstractAction  {

    public LnTcpDriverAction(String s) { super(s);}

    public void actionPerformed(ActionEvent e) {
        LnTcpDriverFrame f = new LnTcpDriverFrame();
        try {
            f.initComponents();
        }
        catch (Exception ex) {
            log.error("starting LnTcpDriverFrame caught exception: "+ex.toString());
        }
        f.setVisible(true);
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LnTcpDriverAction.class.getName());

}


/* @(#)NetworkDriverAction.java */
