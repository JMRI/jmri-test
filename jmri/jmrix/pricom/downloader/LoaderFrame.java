// LoaderFrame.java

package jmri.jmrix.pricom.downloader;

import java.awt.Dimension;
import java.util.ResourceBundle;

import javax.swing.JMenuBar;

import jmri.util.JmriJFrame;

/**
 * Frame for downloading (mangled) .hex files
 *
 * This is just an enclosure for the LoaderPane, which does the real work.
 *
 * @author		Bob Jacobsen   Copyright (C) 2005
 * @version             $Revision: 1.3 $
 */
public class LoaderFrame extends JmriJFrame {

    // GUI member declarations
    LoaderPane pane	= new LoaderPane();

    public LoaderFrame() {
        super(ResourceBundle.getBundle("jmri.jmrix.pricom.downloader.Loader").getString("TitleLoader"));
        // general GUI config

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        
        // install items in GUI
        getContentPane().add(pane);
        pack();
    }

    // Clean up this window
    public void dispose() {
        pane.dispose();
        super.dispose();
    }
}
