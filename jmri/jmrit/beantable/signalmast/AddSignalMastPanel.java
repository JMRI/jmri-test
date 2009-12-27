// AddSignalMastPanel.java

package jmri.jmrit.beantable.sensor;

import jmri.*;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import java.util.ResourceBundle;

/**
 * JPanel to create a new SignalMast
 *
 * @author	Bob Jacobsen    Copyright (C) 2009
 * @version     $Revision: 1.1 $
 */

public class AddSignalMastPanel extends JPanel {

    public AddSignalMastPanel() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            JPanel p;
            p = new JPanel(); 
            p.setLayout(new FlowLayout());
            p.setLayout(new java.awt.GridBagLayout());
            java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
            c.gridwidth  = 1;
            c.gridheight = 1;
            c.gridx = 0;
            c.gridy = 0;
            c.anchor = java.awt.GridBagConstraints.EAST;
            p.add(sysNameLabel,c);
            c.gridy = 1;
            p.add(userNameLabel,c);
            c.gridx = 1;
            c.gridy = 0;
            c.anchor = java.awt.GridBagConstraints.WEST;
            c.weightx = 1.0;
            c.fill = java.awt.GridBagConstraints.HORIZONTAL;  // text field will expand
            p.add(sysName,c);
            c.gridy = 1;
            p.add(userName,c);
            add(p);

            JButton ok;
            add(ok = new JButton(rb.getString("ButtonOK")));
            ok.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    okPressed(e);
                }
            });
    }
    
    JTextField sysName = new JTextField(5);
    JTextField userName = new JTextField(5);
    JLabel sysNameLabel = new JLabel(rb.getString("LabelSystemName"));
    JLabel userNameLabel = new JLabel(rb.getString("LabelUserName"));

    void okPressed(ActionEvent e) {
        SignalMast m = InstanceManager.signalMastManagerInstance().provideSignalMast("IF$shsm:"+sysName.getText());
        String user = userName.getText();
        if (!user.equals("")) m.setUserName(user);
    }

    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.beantable.BeanTableBundle");
    static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AddSignalMastPanel.class.getName());
}


/* @(#)SensorTableAction.java */
