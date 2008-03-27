//SprogVersionAction.java

package jmri.jmrix.sprog.update;

import java.awt.event.ActionEvent;

import javax.swing.*;

/**
 * Swing action to get SPROG frimware version
 *
 * @author			Andrew crosland    Copyright (C) 2004
 * @version			$Revision: 1.1 $
 */

public class SprogVersionAction extends AbstractAction {
    
    public SprogVersionAction(String s) { super(s);}
    
    public void actionPerformed(ActionEvent e) {
        // create a SprogVersionFrame
        SprogVersionFrame f = new SprogVersionFrame();
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.warn("SprogIIUpdateAction starting SprogIIUpdateFrame: Exception: "+ex.toString());
        }
//        f.setVisible(true);
    }
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SprogVersionAction.class.getName());
    
}


/* @(#)SprogVersionAction.java */
