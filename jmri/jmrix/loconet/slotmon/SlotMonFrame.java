// SlotMonFrame.java

package jmri.jmrix.loconet.slotmon;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

/**
 * Frame provinging a command station slot manager
 * @author	Bob Jacobsen   Copyright (C) 2001
 * @version	$Revision: 1.6 $
 */
public class SlotMonFrame extends javax.swing.JFrame {

    // GUI member declarations
    javax.swing.JCheckBox 	showAllCheckBox = new javax.swing.JCheckBox();
    JButton                     estopAllButton  = new JButton("estop all");
    SlotMonDataModel		slotModel 	= new SlotMonDataModel(128,16);
    JTable			slotTable	= new JTable(slotModel);
    JScrollPane 		slotScroll	= new JScrollPane(slotTable);

    public SlotMonFrame() {

        // configure items for GUI
        showAllCheckBox.setText("show all slots");
        showAllCheckBox.setVisible(true);
        showAllCheckBox.setSelected(true);
        showAllCheckBox.setToolTipText("if checked, even empty/idle slots will appear");


        slotModel.configureTable(slotTable);

        // add listener object so checkbox functions
        showAllCheckBox.addActionListener(new CheckNotify(slotModel, showAllCheckBox));

        // add listener object so stop all button functions
        estopAllButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                slotModel.estopAll();
            }
        });

        // general GUI config
        setTitle("Slot Monitor");
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // install items in GUI
        JPanel pane1 = new JPanel();
        pane1.setLayout(new FlowLayout());

        pane1.add(showAllCheckBox);
        pane1.add(estopAllButton);

        getContentPane().add(pane1);
        getContentPane().add(slotScroll);
        pack();
        pane1.setMaximumSize(pane1.getSize());
        pack();
    }

    // inner class to handle messaging for the "show all slots" check box
    class CheckNotify implements ActionListener {
        private SlotMonDataModel _model;
        javax.swing.JCheckBox _box;
        public CheckNotify(SlotMonDataModel model, javax.swing.JCheckBox box)
        {_model = model; _box=box;}
        public void actionPerformed(ActionEvent ev) {
            // checkbox action received; forward state
            _model.showAllSlots(_box.isSelected());
            _model.fireTableDataChanged();
        }
    }

    private boolean mShown = false;

    public void addNotify() {
        super.addNotify();

        if (mShown)
            return;

        // resize frame to account for menubar
        JMenuBar jMenuBar = getJMenuBar();
        if (jMenuBar != null) {
            int jMenuBarHeight = jMenuBar.getPreferredSize().height;
            Dimension dimension = getSize();
            dimension.height += jMenuBarHeight;
            setSize(dimension);
        }
        mShown = true;
    }

    // Close the window when the close box is clicked
    void thisWindowClosing(java.awt.event.WindowEvent e) {
        setVisible(false);
        dispose();
	// and disconnect from the SlotManager

    }

    public void dispose() {
        slotModel.dispose();
        slotModel = null;
        slotTable = null;
        slotScroll = null;
        super.dispose();
    }
}
