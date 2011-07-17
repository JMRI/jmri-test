// LnHexFileAction.java

package jmri.jmrix.loconet.hexfile;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
/**
 * Swing action to create and register a
 * LnHexFileFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			$Revision: 1.7 $
 */
public class LnHexFileAction 			extends AbstractAction {

    public LnHexFileAction(String s) { super(s);}

    public void actionPerformed(ActionEvent e) {
        // create a LnHexFileFrame
        HexFileFrame f = new HexFileFrame();
        try {
            f.initComponents();
        }
        catch (Exception ex) {
            log.error("starting HexFileFrame exception: "+ex.toString());
        }
        f.pack();
        f.setVisible(true);
        // it connects to the LnTrafficController when the right button is pressed


    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LnHexFileAction.class.getName());

}


/* @(#)LnHexFileAction.java */
