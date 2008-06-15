// MessageFrame.java

package jmri.jmrix.loconet.swing.throttlemsg;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import jmri.jmrix.loconet.*;

/**
 * Frame for sending messages to throttles.
 * @author		Bob Jacobsen   Copyright (C) 2008
 * @version             $Revision: 1.1 $
 */
public class MessageFrame extends jmri.util.JmriJFrame {

    // GUI member declarations
    JButton button = new JButton("Send");
    JTextField text = new JTextField(10);

    public MessageFrame() {
        this("Throttle message");
    }

    public MessageFrame(String label) {
        super(label);

        // general GUI config

        // install items in GUI
        getContentPane().setLayout(new FlowLayout());
        getContentPane().add(text);
        getContentPane().add(button);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                LnMessageManager.instance().sendMessage(text.getText());
            }
        });
        
        addHelpMenu("package.jmri.jmrix.loconet.swing.throttlemsg.MessageFrame", true);
        
        pack();
    }
}
