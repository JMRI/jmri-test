package jmri.jmrit.display;

import jmri.InstanceManager;
import jmri.jmrit.catalog.NamedIcon;
import jmri.util.JmriJFrame;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ResourceBundle;

import javax.swing.*;


/**
 * Provides a simple editor for creating a MultiSensorIcon object
 * <p>
 * To work right, the MultiSensorIcon needs to have all
 * images the same size, but this is not enforced here. 
 * It should be.
 *
 * @author  Bob Jacobsen  Copyright (c) 2007
 * @version $Revision: 1.2 $
 */

public class MultiSensorIconFrame extends JmriJFrame {
    JPanel content = new JPanel();
    JFrame defaultsFrame;
    MultiIconEditor defaultIcons;
    PanelEditor panelEditor;
    
    JRadioButton updown = new JRadioButton("Up - Down");
    JRadioButton rightleft = new JRadioButton("Right - Left");
    ButtonGroup group = new ButtonGroup();
    
    MultiSensorIconFrame(PanelEditor p) {
        super("Enter MultiSensor");
        panelEditor = p;
        
        addHelpMenu("package.jmri.jmrit.display.MultiSensorIconFrame", true);
    }
    
    void initComponents() {
        this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(new JLabel("Icon checks click: "));
        group.add(updown);
        group.add(rightleft);
        rightleft.setSelected(true);
        p.add(rightleft);
        p.add(updown);
        this.getContentPane().add(p);
        
        this.getContentPane().add(content);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        // start with two Entrys; there's no reason to have less
        content.add(new Entry(content, this, "resources/icons/USSpanels/Plates/l-left.gif"));
        content.add(new Entry(content, this, "resources/icons/USSpanels/Plates/l-vertical.gif"));
        content.add(new Entry(content, this, "resources/icons/USSpanels/Plates/l-right.gif"));
                
        this.getContentPane().add(new JSeparator());
        JButton b = new JButton("Add Additional Sensor to Icon"); 
        ActionListener a = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                // remove this entry
                self.add(new Entry(self, frame, "resources/icons/USSpanels/Plates/l-vertical.gif"));
                frame.pack();
            }
            JPanel self;
            JFrame frame;
            ActionListener init(JPanel self, JFrame frame) {
                this.frame = frame;
                this.self = self;
                return this;
            }
        }.init(content, this);
        b.addActionListener(a);
        this.getContentPane().add(b);

        this.getContentPane().add(new JSeparator());
        b = new JButton("Set default icons ...");
        defaultIcons = new MultiIconEditor(3);
            defaultIcons.setIcon(0, "Unknown:","resources/icons/USSpanels/Plates/l-vertical-inactive.gif");
            defaultIcons.setIcon(1, "Inconsistent:","resources/icons/USSpanels/Plates/l-vertical-inactive.gif");
            defaultIcons.setIcon(2, "Inactive:","resources/icons/USSpanels/Plates/l-vertical-inactive.gif");
            defaultIcons.complete();
        defaultsFrame = new JFrame("");
            defaultsFrame.getContentPane().add(new JLabel("  Select new file, then click on icon to change  "),BorderLayout.NORTH);
            defaultsFrame.getContentPane().add(defaultIcons);
            defaultsFrame.pack();
        b.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent a){
                defaultsFrame.setVisible(true);
            }
        });
        this.getContentPane().add(b);
        
        this.getContentPane().add(new JSeparator());
        b = new JButton("Create and Add Icon To Panel");
        b.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent a){
                make();
            }
        });        
        this.getContentPane().add(b);
    }
    
    
    // Remove an Entry from the panel, 
    // and therefore from the eventual sensor
    void remove(Entry e) {
        content.remove(e);
        this.pack();
    }
    
    void make() {
        MultiSensorIcon m = new MultiSensorIcon();
        m.setUnknownIcon(defaultIcons.getIcon(0));
        m.setInconsistentIcon(defaultIcons.getIcon(1));
        m.setInactiveIcon(defaultIcons.getIcon(2));
        
        for (int i = 0; i< content.getComponentCount(); i++) {
            Entry e = (Entry)content.getComponent(i);
            m.addEntry(e.sensor.getText(), e.ed.getIcon(0));
        }
        m.setUpDown(updown.isSelected());

        // add it to the panel
        panelEditor.addMultiSensor(m);
    }
    
    class Entry extends JPanel {

        JTextField sensor = new JTextField(5);
        JPanel self;
        MultiIconEditor ed = new MultiIconEditor(1);
        JFrame edf = new JFrame("");
        
        public String toString() {
            return ed.getIcon(0).toString();
        }
        
        Entry(JPanel self, JFrame frame, String name) {
            this.self = self;
            this.setLayout(new FlowLayout());
            this.add(new JLabel("Sensor:"));
            
            this.add(sensor);
            
            ed.setIcon(0, "Active:", name);
            ed.complete();
            edf.getContentPane().add(new JLabel("  Select new file, then click on icon to change  "),BorderLayout.NORTH);
            edf.getContentPane().add(ed);
            edf.pack();

            JButton b = new JButton("Set Icon...");
            b.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    edf.setVisible(true);
                }
            });
            this.add(b);
            
            // button to remove this entry from it's parent 
            b = new JButton("Delete");
            ActionListener a = new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    // remove this entry
                    self.remove(entry);
                    frame.pack();
                }
                Entry entry;
                JPanel self;
                JFrame frame;
                ActionListener init(Entry entry, JPanel self, JFrame frame) {
                    this.entry = entry;
                    this.self = self;
                    this.frame = frame;
                    return this;
                }
            }.init(this, self, frame);
            b.addActionListener(a);
            
            this.add(b);
        }
    }
    
    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(MultiSensorIconFrame.class.getName());
}
