package jmri.jmrit.display;

import jmri.InstanceManager;
import jmri.Sensor;
import jmri.Turnout;
import jmri.jmrit.catalog.NamedIcon;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import com.sun.java.util.collections.ArrayList;

/**
 * Provides a simple editor for adding jmri.jmrit.display items
 * to a captive JFrame.
 * <P>GUI is structured as a band of common parameters across the
 * top, then a series of things you can add.
 * <P>
 * All created objects are put specific levels depending on their
 * type (higher levels are in front):
 * <UL>
 * <LI>BKG background
 * <LI>ICONS icons and other drawing symbols
 * <LI>LABELS text labels
 * <LI>TURNOUTS turnouts and other variable track items
 * <LI>SENSORS sensors and other independently modified objects
 * </UL>
 * Note that higher numbers appear behind lower numbers.
 * <P>
 * The "contents" List keeps track of all the objects added to the target
 * frame for later manipulation.
 * <P>
 * If you close the Editor window, the target is left alone and
 * the editor window is just hidden, not disposed.
 * If you close the target, the editor and target are removed,
 * and dispose is run. To make this logic work, the PanelEditor
 * is descended from a JFrame, not a JPanel.  That way it
 * can control its own visibility.
 * <P>
 * The title of the target and the editor panel are kept
 * consistent via the {#setTitle} method.
 *
 * @author Bob Jacobsen  Copyright: Copyright (c) 2002, 2003
 * @version $Revision: 1.38 $
 */

public class PanelEditor extends JFrame {

    final public static Integer BKG       = new Integer(1);
    final public static Integer ICONS     = new Integer(3);
    final public static Integer LABELS    = new Integer(5);
    final public static Integer SECURITY  = new Integer(6);
    final public static Integer TURNOUTS  = new Integer(7);
    final public static Integer SIGNALS   = new Integer(9);
    final public static Integer SENSORS   = new Integer(10);

    JTextField nextX = new JTextField("20",4);
    JTextField nextY = new JTextField("30",4);

    JCheckBox editableBox = new JCheckBox("Panel items popup menus active");
    JCheckBox positionableBox = new JCheckBox("Panel items can be repositioned");
    JCheckBox controllingBox = new JCheckBox("Panel items control layout");

    JButton labelAdd = new JButton("Add text:");
    JTextField nextLabel = new JTextField(10);

    JButton iconAdd = new JButton("Add icon:");
    MultiIconEditor iconEditor;
    JFrame iconFrame;

    JButton turnoutAddR = new JButton("Add right-hand turnout:");
    JTextField nextTurnoutR = new JTextField(5);
    MultiIconEditor turnoutRIconEditor;
    JFrame turnoutRFrame;

    JButton turnoutAddL = new JButton("Add left-hand turnout:");
    JTextField nextTurnoutL = new JTextField(5);
    MultiIconEditor turnoutLIconEditor;
    JFrame turnoutLFrame;

    JButton sensorAdd = new JButton("Add sensor:");
    JTextField nextSensor = new JTextField(5);
    MultiIconEditor sensorIconEditor;
    JFrame sensorFrame;

    JButton signalAdd = new JButton("Add signal:");
    JTextField nextSignalHead = new JTextField(5);
    MultiIconEditor signalIconEditor;
    JFrame signalFrame;

    JButton backgroundAddButton = new JButton("Pick background image...");

    public PanelEditor() { this("Panel Editor");}

    public PanelEditor(String name) {
        super(name);
        this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));

        // common items
        JPanel common = new JPanel();
        common.setLayout(new FlowLayout());
        common.add(new JLabel(" x:"));
        common.add(nextX);
        common.add(new JLabel(" y:"));
        common.add(nextY);
        this.getContentPane().add(common);

        // allow naming the panel
        {
            JPanel namep = new JPanel();
            namep.setLayout(new FlowLayout());
            JButton b = new JButton("Set panel name");
            b.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    // prompt for name
                    String newName = JOptionPane.showInputDialog(target, "Enter new name:");
                    if (newName==null) return;  // cancelled

                    if (getTarget().getTopLevelAncestor()!=null) ((JFrame)getTarget().getTopLevelAncestor()).setTitle(newName);
                    setTitle();
                }
            });
            namep.add(b);
            this.getContentPane().add(namep);
        }
        // add a background image
        {
            JPanel panel = new JPanel();
            panel.setLayout(new FlowLayout());
            panel.add(backgroundAddButton);
            panel.add(labelAdd);
            backgroundAddButton.addActionListener( new ActionListener() {
                    public void actionPerformed(ActionEvent a) {
                        addBackground();
                    }
                }
                                                   );
            this.getContentPane().add(panel);
        }

        // add a text label
        {
            JPanel panel = new JPanel();
            panel.setLayout(new FlowLayout());
            panel.add(labelAdd);
            panel.add(nextLabel);
            labelAdd.addActionListener( new ActionListener() {
                    public void actionPerformed(ActionEvent a) {
                        addLabel();
                    }
                }
                                        );
            this.getContentPane().add(panel);
        }

        // Add icon label
        {
            JPanel panel = new JPanel();
            panel.setLayout(new FlowLayout());
            panel.add(iconAdd);
            iconAdd.addActionListener( new ActionListener() {
                    public void actionPerformed(ActionEvent a) {
                        addIcon();
                    }
                }
                                           );

            iconEditor = new MultiIconEditor(1);
            iconEditor.setIcon(0, "","resources/icons/smallschematics/tracksegments/block.gif");
            iconEditor.complete();
            iconFrame = new JFrame("Edit icon");
            iconFrame.getContentPane().add(iconEditor);
            iconFrame.pack();

            JButton j = new JButton("Edit icon...");
            j.addActionListener( new ActionListener() {
                    public void actionPerformed(ActionEvent a) {
                        iconFrame.show();
                    }
                }
                                           );
            panel.add(j);

            this.getContentPane().add(panel);
        }

        // Add a turnout indicator for right-hand
        {
            JPanel panel = new JPanel();
            panel.setLayout(new FlowLayout());
            panel.add(turnoutAddR);
            panel.add(nextTurnoutR);
            turnoutAddR.addActionListener( new ActionListener() {
                    public void actionPerformed(ActionEvent a) {
                        addTurnoutR();
                    }
                }
                                           );

            turnoutRIconEditor = new MultiIconEditor(4);
            turnoutRIconEditor.setIcon(0, "Closed:","resources/icons/smallschematics/tracksegments/os-righthand-west-closed.gif");
            turnoutRIconEditor.setIcon(1, "Thrown", "resources/icons/smallschematics/tracksegments/os-righthand-west-thrown.gif");
            turnoutRIconEditor.setIcon(2, "Inconsistent:", "resources/icons/smallschematics/tracksegments/os-righthand-west-error.gif");
            turnoutRIconEditor.setIcon(3, "Unknown:","resources/icons/smallschematics/tracksegments/os-righthand-west-unknown.gif");
            turnoutRIconEditor.complete();
            turnoutRFrame = new JFrame("Edit RH turnout icons");
            turnoutRFrame.getContentPane().add(turnoutRIconEditor);
            turnoutRFrame.pack();

            JButton j = new JButton("Edit icons...");
            j.addActionListener( new ActionListener() {
                    public void actionPerformed(ActionEvent a) {
                        turnoutRFrame.show();
                    }
                }
                                           );
            panel.add(j);

            this.getContentPane().add(panel);
        }

        // Add a turnout indicator for left-hand
        {
            JPanel panel = new JPanel();
            panel.setLayout(new FlowLayout());
            panel.add(turnoutAddL);
            panel.add(nextTurnoutL);
            turnoutAddL.addActionListener( new ActionListener() {
                    public void actionPerformed(ActionEvent a) {
                        addTurnoutL();
                    }
                }
                                           );

            turnoutLIconEditor = new MultiIconEditor(4);
            turnoutLIconEditor.setIcon(0, "Closed:","resources/icons/smallschematics/tracksegments/os-lefthand-east-closed.gif");
            turnoutLIconEditor.setIcon(1, "Thrown", "resources/icons/smallschematics/tracksegments/os-lefthand-east-thrown.gif");
            turnoutLIconEditor.setIcon(2, "Inconsistent:", "resources/icons/smallschematics/tracksegments/os-lefthand-east-error.gif");
            turnoutLIconEditor.setIcon(3, "Unknown:","resources/icons/smallschematics/tracksegments/os-lefthand-east-unknown.gif");
            turnoutLIconEditor.complete();
            turnoutLFrame = new JFrame("Edit LH turnout icons");
            turnoutLFrame.getContentPane().add(turnoutLIconEditor);
            turnoutLFrame.pack();

            JButton j = new JButton("Edit icons...");
            j.addActionListener( new ActionListener() {
                    public void actionPerformed(ActionEvent a) {
                        turnoutLFrame.show();
                    }
                }
                                           );
            panel.add(j);

            this.getContentPane().add(panel);
        }

        // Add a sensor indicator
        {
            JPanel panel = new JPanel();
            panel.setLayout(new FlowLayout());
            panel.add(sensorAdd);
            panel.add(nextSensor);
            sensorAdd.addActionListener( new ActionListener() {
                    public void actionPerformed(ActionEvent a) {
                        addSensor();
                    }
                }
                                           );

            sensorIconEditor = new MultiIconEditor(4);
            sensorIconEditor.setIcon(0, "Active:","resources/icons/smallschematics/tracksegments/circuit-occupied.gif");
            sensorIconEditor.setIcon(1, "Inactive", "resources/icons/smallschematics/tracksegments/circuit-empty.gif");
            sensorIconEditor.setIcon(2, "Inconsistent:", "resources/icons/smallschematics/tracksegments/circuit-error.gif");
            sensorIconEditor.setIcon(3, "Unknown:","resources/icons/smallschematics/tracksegments/circuit-error.gif");
            sensorIconEditor.complete();
            sensorFrame = new JFrame("Edit sensor icons");
            sensorFrame.getContentPane().add(sensorIconEditor);
            sensorFrame.pack();

            JButton j = new JButton("Edit icons...");
            j.addActionListener( new ActionListener() {
                    public void actionPerformed(ActionEvent a) {
                        sensorFrame.show();
                    }
                }
                                           );
            panel.add(j);

            this.getContentPane().add(panel);
        }

        // Add a signal indicator
        {
            JPanel panel = new JPanel();
            panel.setLayout(new FlowLayout());
            panel.add(signalAdd);
            panel.add(nextSignalHead);
            signalAdd.addActionListener( new ActionListener() {
                    public void actionPerformed(ActionEvent a) {
                        addSignalHead();
                    }
                }
                                           );

            signalIconEditor = new MultiIconEditor(4);
            signalIconEditor.setIcon(0, "Red:","resources/icons/smallschematics/searchlights/left-red-marker.gif");
            signalIconEditor.setIcon(1, "Yellow", "resources/icons/smallschematics/searchlights/left-yellow-marker.gif");
            signalIconEditor.setIcon(2, "Flash yellow:", "resources/icons/smallschematics/searchlights/left-flashyellow-marker.gif");
            signalIconEditor.setIcon(3, "Green:","resources/icons/smallschematics/searchlights/left-green-marker.gif");
            signalIconEditor.complete();
            signalFrame = new JFrame("Edit signal icons");
            signalFrame.getContentPane().add(signalIconEditor);
            signalFrame.pack();

            JButton j = new JButton("Edit icons...");
            j.addActionListener( new ActionListener() {
                    public void actionPerformed(ActionEvent a) {
                        signalFrame.show();
                    }
                }
                                           );
            panel.add(j);

            this.getContentPane().add(panel);
        }
        // edit, position, control controls
        {
            JPanel p;
            this.getContentPane().add(p = new JPanel());
            p.setLayout(new FlowLayout());
            p.add(editableBox);
            editableBox.setSelected(true);
            editableBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setAllEditable(editableBox.isSelected());
                }
            });

            this.getContentPane().add(p = new JPanel());
            p.setLayout(new FlowLayout());
            p.add(positionableBox);
            positionableBox.setSelected(true);
            positionableBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setAllPositionable(positionableBox.isSelected());
                }
            });

            this.getContentPane().add(p = new JPanel());
            p.setLayout(new FlowLayout());
            p.add(controllingBox);
            controllingBox.setSelected(true);
            controllingBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setAllControlling(controllingBox.isSelected());
                }
            });
       }

        // register the resulting panel for later configuration
        InstanceManager.configureManagerInstance().registerUser(this);

        // move this editor panel off the panel's position
        setLocation(250,0);

        // when this window closes, set contents of target uneditable
        addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowClosing(java.awt.event.WindowEvent e) {
                    setAllPositionable(false);
                }
            });
    }  // end ctor

    /**
     * Button pushed, add a background image. Note that a background image
     * differs from a regular icon only in the level at which it's presented.
     */
    void addBackground() {
        JFileChooser inputFileChooser = new JFileChooser(" ");
        int retVal = inputFileChooser.showOpenDialog(this);
        if (retVal != JFileChooser.APPROVE_OPTION) return;  // give up if no file selected
        log.debug("Open image file: "+inputFileChooser.getSelectedFile().getPath());
        NamedIcon icon = new NamedIcon(inputFileChooser.getSelectedFile().getPath(),
                                       inputFileChooser.getSelectedFile().getPath());
        PositionableLabel l = new PositionableLabel(icon);
        l.setSize(icon.getIconWidth(), icon.getIconHeight());
        l.setDisplayLevel(BKG);
        putLabel(l);
    }

    /**
     * Add a turnout indicator to the target
     */
    void addTurnoutR() {
        TurnoutIcon l = new TurnoutIcon();
        l.setClosedIcon(turnoutRIconEditor.getIcon(0));
        l.setThrownIcon(turnoutRIconEditor.getIcon(1));
        l.setInconsistentIcon(turnoutRIconEditor.getIcon(2));
        l.setUnknownIcon(turnoutRIconEditor.getIcon(3));

        Turnout t = InstanceManager.turnoutManagerInstance()
            .provideTurnout(nextTurnoutR.getText());
        l.setTurnout(t.getSystemName(), t.getUserName());

        setNextLocation(l);
        putTurnout(l);
    }
    void addTurnoutL() {
        TurnoutIcon l = new TurnoutIcon();
        l.setClosedIcon(turnoutLIconEditor.getIcon(0));
        l.setThrownIcon(turnoutLIconEditor.getIcon(1));
        l.setInconsistentIcon(turnoutLIconEditor.getIcon(2));
        l.setUnknownIcon(turnoutLIconEditor.getIcon(3));

        Turnout t = InstanceManager.turnoutManagerInstance()
            .provideTurnout(nextTurnoutL.getText());
        l.setTurnout(t.getSystemName(), t.getUserName());

        setNextLocation(l);
        putTurnout(l);
    }
    public void putTurnout(TurnoutIcon l) {
        l.invalidate();
        target.add(l, TURNOUTS);
        contents.add(l);
        // reshow the panel
        target.validate();
    }

    /**
     * Add a sensor indicator to the target
     */
    void addSensor() {
        SensorIcon l = new SensorIcon();
        l.setActiveIcon(sensorIconEditor.getIcon(0));
        l.setInactiveIcon(sensorIconEditor.getIcon(1));
        l.setInconsistentIcon(sensorIconEditor.getIcon(2));
        l.setUnknownIcon(sensorIconEditor.getIcon(3));
        Sensor s = InstanceManager.sensorManagerInstance()
                        .provideSensor(nextSensor.getText());
        l.setSensor(s.getSystemName(), s.getUserName());
        setNextLocation(l);
        putSensor(l);
    }
    public void putSensor(SensorIcon l) {
        l.invalidate();
        target.add(l, SENSORS);
        contents.add(l);
        // reshow the panel
        target.validate();
    }

    /**
     * Add a signal head to the target
     */
    void addSignalHead() {
        SignalHeadIcon l = new SignalHeadIcon();
        l.setRedIcon(signalIconEditor.getIcon(0));
        l.setYellowIcon(signalIconEditor.getIcon(1));
        l.setFlashYellowIcon(signalIconEditor.getIcon(2));
        l.setGreenIcon(signalIconEditor.getIcon(3));
        l.setSignalHead(nextSignalHead.getText());
        setNextLocation(l);
        putSignal(l);
    }
    public void putSignal(SignalHeadIcon l) {
        l.invalidate();
        target.add(l, SIGNALS);
        contents.add(l);
        // reshow the panel
        target.validate();
    }

    /**
     * Add a label to the target
     */
    void addLabel() {
        PositionableLabel l = new PositionableLabel(nextLabel.getText());
        setNextLocation(l);
        l.setSize(l.getPreferredSize().width, l.getPreferredSize().height);
        l.setDisplayLevel(LABELS);
        putLabel(l);
    }
    public void putLabel(PositionableLabel l) {
        l.invalidate();
        target.add(l, l.getDisplayLevel());
        contents.add(l);
        target.validate();
    }

    /**
     * Add an icon to the target
     */
    void addIcon() {
        PositionableLabel l = new PositionableLabel(iconEditor.getIcon(0) );
        setNextLocation(l);
        l.setDisplayLevel(ICONS);
        putLabel(l);
    }

    /**
     * Set an objects location and size as it is created.
     * Size comes from the preferredSize; location comes
     * from the fields where the user can spec it.
     */
    void setNextLocation(JComponent obj) {
        int x = Integer.parseInt(nextX.getText());
        int y = Integer.parseInt(nextY.getText());
        obj.setLocation(x,y);
    }

    /**
     * Set the JLayeredPane containing the objects to be edited.
     */
    public void setTarget(JLayeredPane f) {
        target = f;
    }
    public JLayeredPane getTarget() { return target;}
    public JLayeredPane target;

    /**
     * Get the frame containing the results (not the editor)
     */
    public JFrame getFrame() { return frame; }
    public void setFrame(JFrame f) {
        frame = f;
        // handle target window closes
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowClosing(java.awt.event.WindowEvent e) {
                    targetWindowClosing(e);
                }
            });
    }

    JFrame frame;

    public ArrayList contents = new ArrayList();

    /**
     * Clean up when its time to make it all go away
     */
    public void dispose() {
        // register the result for later configuration
        InstanceManager.configureManagerInstance().deregister(this);

        // clean up local links to push GC
        contents.clear();
        target = null;
        frame = null;

        // clean up GUI aspects
        this.removeAll();
        super.dispose();
    }

    /**
     * The target window has been requested to close, so clean up
     */
    void targetWindowClosing(java.awt.event.WindowEvent e) {
        frame.setVisible(false);  // removes the target window
        this.setVisible(false);        // doesn't remove the editor!

        dispose();
    }
    public void setTitle() {
        String name = "";
        if (getTarget().getTopLevelAncestor()!=null) name=((JFrame)getTarget().getTopLevelAncestor()).getTitle();
        if (name==null || name.equals("")) super.setTitle("Editor");
        super.setTitle(name+" Editor");
    }

    /**
     *  Control whether target panel items are positionable.
     *  Does this by invoke the {@link Positionable#setPositionable} function of
     *  each item on the target panel.
     * @param state true for positionable.
     */
    public void setAllPositionable(boolean state) {
        if (positionableBox.isSelected()!=state) positionableBox.setSelected(state);
        for (int i = 0; i<contents.size(); i++) {
            ((Positionable)contents.get(i)).setPositionable(state);
        }
    }

    /**
     *  Control whether target panel items are editable.
     *  Does this by invoke the {@link Positionable#setEditable} function of
     *  each item on the target panel. This also controls the relevant pop-up menu items
     *  (which are the primary way that items are edited).
     * @param state true for editable.
     */
    public void setAllEditable(boolean state) {
        if (editableBox.isSelected()!=state) editableBox.setSelected(state);
        for (int i = 0; i<contents.size(); i++) {
            ((Positionable)contents.get(i)).setEditable(state);
        }
    }

    /**
     *  Control whether target panel items are controlling layout items.
     *  Does this by invoke the {@link Positionable#setControlling} function of
     *  each item on the target panel. This also controls the relevant pop-up menu items.
     * @param state true for controlling.
     */
    public void setAllControlling(boolean state) {
        if (controllingBox.isSelected()!=state) controllingBox.setSelected(state);
        for (int i = 0; i<contents.size(); i++) {
            ((Positionable)contents.get(i)).setControlling(state);
        }
    }

    public boolean isEditable() {
        return editableBox.isSelected();
    }
    public boolean isPositionable() {
        return positionableBox.isSelected();
    }
    public boolean isControlling() {
        return controllingBox.isSelected();
    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PanelEditor.class.getName());
}
