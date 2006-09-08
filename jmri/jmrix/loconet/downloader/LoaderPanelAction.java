// LoaderPanelAction.java

package jmri.jmrix.loconet.downloader;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register a
 * LoaderFrame object.
 *
 * @author	    Bob Jacobsen    Copyright (C) 2005
 * @version         $Revision: 1.2 $
 */
public class LoaderPanelAction extends AbstractAction {
    static ResourceBundle res = ResourceBundle.getBundle("jmri.jmrix.loconet.downloader.Loader");

    public LoaderPanelAction(String s) {
	    super(s);

    }

    public LoaderPanelAction() {
        this(res.getString("TitleLoader"));
    }

    public void actionPerformed(ActionEvent e) {
        // create a PowerPanelFrame
        LoaderFrame f = new LoaderFrame();
        f.setVisible(true);
    }
}

/* @(#)LoaderPanelAction.java */
