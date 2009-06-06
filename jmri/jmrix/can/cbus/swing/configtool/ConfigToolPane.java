// ConfigToolPane.java

package jmri.jmrix.can.cbus.swing.configtool;

import jmri.InstanceManager;
import jmri.jmrix.can.*;
import jmri.jmrix.can.cbus.CbusMessage;

import java.util.ResourceBundle;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.*;

import javax.swing.*;

/**
 * Pane to ease creation of Sensor, Turnouts and Lights
 * that are linked to CBUS events.
 *
 * @author			Bob Jacobsen   Copyright (C) 2008
 * @version			$Revision: 1.6 $
 * @since 2.3.1
 */
public class ConfigToolPane extends JPanel implements CanListener {

    static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.can.cbus.swing.configtool.ConfigToolBundle");
    
    static final int NRECORDERS = 6;
    CbusEventRecorder[] recorders = new CbusEventRecorder[NRECORDERS];
    
    public ConfigToolPane() {
        super();
        
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        // add event displays
        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));
        for (int i = 0; i<recorders.length; i++) {
            recorders[i] = new CbusEventRecorder();
            p1.add(recorders[i]);
        }
        p1.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutEvents")));
        add(p1);
        
        // add sensor
        makeSensor = new MakeNamedBean("LabelEventActive", "LabelEventInactive"){
            void create(String name) {
                InstanceManager.sensorManagerInstance().provideSensor("MS"+name);
            }
        };
        makeSensor.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutAddSensor")));
        add(makeSensor);

        // add turnout
        makeTurnout = new MakeNamedBean("LabelEventThrown", "LabelEventClosed"){
            void create(String name) {
                InstanceManager.turnoutManagerInstance().provideTurnout("MT"+name);
            }
        };
        makeTurnout.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutAddTurnout")));
        add(makeTurnout);

        // connect
        TrafficController.instance().addCanListener(this);

    }

    MakeNamedBean makeSensor;
    MakeNamedBean makeTurnout;
    
    public void reply(jmri.jmrix.can.CanReply m) {
        // forward to anybody waiting to capture
        makeSensor.reply(m);
        makeTurnout.reply(m);
        for (int i = 0; i<recorders.length; i++) {
            if (recorders[i].waiting()) {
                recorders[i].reply(m);
                break;
            }                    
        }
    }

    public void message(jmri.jmrix.can.CanMessage m) {
        // forward to anybody waiting to capture
        makeSensor.message(m);
        makeTurnout.message(m);
        for (int i = 0; i<recorders.length; i++) {
            if (recorders[i].waiting()) {
                recorders[i].message(m);
                break;
            }                    
        }
    }

    public void dispose() {
        // disconnect from the CBUS
        TrafficController.instance().removeCanListener(this);
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ConfigToolPane.class.getName());

    /** 
     * Class to build one NamedBean
     */
    class MakeNamedBean extends JPanel implements CanListener {
        JTextField f1 = new JTextField(20);
        JTextField f2 = new JTextField(20);

        JButton bc;
        
        JToggleButton b1 = new JToggleButton(rb.getString("ButtonNext"));
        JToggleButton b2 = new JToggleButton(rb.getString("ButtonNext"));
        
        MakeNamedBean(String name1, String name2) {
            // actions
            bc = new JButton(rb.getString("ButtonCreate"));
            bc.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (f2.getText().equals(""))
                        create(f1.getText());
                    else
                        create(f1.getText()+";"+f2.getText());
                }
            });
            
            // GUI
            setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.gridwidth  = 1;
            c.gridheight = 1;

            c.gridx = 0;
            c.gridy = 0;
            c.anchor = GridBagConstraints.EAST;
            add(new JLabel(rb.getString(name1)),c);
            
            c.gridx = 1;
            c.gridy = 0;
            c.anchor = GridBagConstraints.WEST;
            add(f1,c);
            
            c.gridx = 2;
            c.gridy = 0;
            c.anchor = GridBagConstraints.WEST;
            add(b1,c);
            
            c.gridx = 0;
            c.gridy = 1;
            c.anchor = GridBagConstraints.EAST;
            add(new JLabel(rb.getString(name2)),c);
            
            c.gridx = 1;
            c.gridy = 1;
            c.anchor = GridBagConstraints.EAST;
            add(f2,c);

            c.gridx = 2;
            c.gridy = 1;
            c.anchor = GridBagConstraints.WEST;
            add(b2,c);
            
            c.gridx = 1;
            c.gridy = 2;
            c.anchor = GridBagConstraints.WEST;
            add(bc,c);
        }

        void create(String name) {}
        
        public void reply(jmri.jmrix.can.CanReply m) {
            if (b1.isSelected()) {
                f1.setText(CbusMessage.toAddress(m));
                b1.setSelected(false);
            }
            if (b2.isSelected()) {
                f2.setText(CbusMessage.toAddress(m));
                b2.setSelected(false);
            }
        }
    
        public void message(jmri.jmrix.can.CanMessage m) {
            if (b1.isSelected()) {
                f1.setText(CbusMessage.toAddress(m));
                b1.setSelected(false);
            }
            if (b2.isSelected()) {
                f2.setText(CbusMessage.toAddress(m));
                b2.setSelected(false);
            }
        }
    }
    
    /**
     * Class to handle recording and presenting one event.
     */
    class CbusEventRecorder extends JPanel implements CanListener {
        CbusEventRecorder() {
            super();
            this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            add(event);
            add(capture);
            
            event.setEditable(false);
            event.setDragEnabled(true);
            capture.setSelected(true);
        }
        
        JCheckBox capture = new JCheckBox(rb.getString("MsgCaptureNext"));
        JTextField event = new JTextField(20);
        boolean waiting() { return capture.isSelected(); }
        public void reply(jmri.jmrix.can.CanReply m) {
            if (capture.isSelected()) {
                event.setText(CbusMessage.toAddress(m));
                capture.setSelected(false);
            }
        }
    
        public void message(jmri.jmrix.can.CanMessage m) {
            if (capture.isSelected()) {
                event.setText(CbusMessage.toAddress(m));
                capture.setSelected(false);
            }
        }
    }
}
