// PowerPanelFrame.java

 package jmri.jmrit.powerpanel;

import java.awt.Dimension;
import java.util.ResourceBundle;

import javax.swing.JMenuBar;

import jmri.util.JmriJFrame;

/**
 * Frame for controlling layout power via a PowerManager.
 *
 * @author		Bob Jacobsen   Copyright (C) 2001
 * @version             $Revision: 1.6 $
 */
public class PowerPanelFrame extends JmriJFrame {

    // GUI member declarations
    PowerPane pane	= new PowerPane();

    public PowerPanelFrame() {
        super(ResourceBundle.getBundle("jmri.jmrit.powerpanel.PowerPanelBundle").getString("TitlePowerPanel"));
        // general GUI config

        // install items in GUI
        getContentPane().add(pane);
        pack();
    }

    public void dispose() {
        pane.dispose();
        super.dispose();
    }
}
