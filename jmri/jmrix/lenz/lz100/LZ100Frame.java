// LZ100Frame.java

package jmri.jmrix.lenz.lz100;

import java.awt.event.*;
import javax.swing.*;
import java.util.ResourceBundle;

/**
 * Frame displaying the LZ100 configuration utility
 *
 * This is a container for the LZ100 configuration utility. The actual 
 * utiliy is defined in {@link LZ100InternalFrame}
 *
 * @author			Paul Bender  Copyright (C) 2005
 * @version			$Revision: 1.5 $
 */
public class LZ100Frame extends jmri.util.JmriJFrame {

    private ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.lenz.lz100.LZ100Bundle");

    public LZ100Frame() {
	    this("LZ100 Configuration Utility");
    }

    public LZ100Frame(String FrameName) {
        super(FrameName);
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

 	javax.swing.JInternalFrame LZ100IFrame=new LZ100InternalFrame();

	javax.swing.JPanel pane0 = new JPanel();
	pane0.add(LZ100IFrame);
        getContentPane().add(pane0);

        JPanel pane1 = new JPanel();
        pane1.add(closeButton);
        getContentPane().add(pane1);

        // and prep for display
        pack();

        // install close button handler
        closeButton.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                	setVisible(false);
        		dispose();
                }
            }
        );

    }


    JToggleButton closeButton = new JToggleButton("Close");

    public void dispose() {
        // take apart the JFrame
        super.dispose();
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LZ100Frame.class.getName());

}
