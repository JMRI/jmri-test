// TreeFrame.java

package jmri.jmrix.jinput.treecontrol;

import java.awt.*;

import java.util.ResourceBundle;

import javax.swing.*;

/**
 * Frame for controlling JInput access to USN
 * @author	 Bob Jacobsen   Copyright (C) 2008
 * @version	 $Revision: 1.4 $
 */
public class TreeFrame extends jmri.util.JmriJFrame {

    ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.jinput.treecontrol.TreeBundle");

    public void initComponents() throws Exception {

        // set the frame's initial state
        setTitle(rb.getString("WindowTitle"));

        Container contentPane = getContentPane();        
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        // add only content pane 
        contentPane.add(new TreePanel());
        
        // add help menu
        addHelpMenu("package.jmri.jmrix.jinput.treecontrol.TreeFrame", true);
        
        // pack for display
        pack();
    }


    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TreeFrame.class.getName());
	
}

/* @(#)TreeFrame.java */
