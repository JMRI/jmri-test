// SlotMonFrame.java

package jmri.jmrix.loconet.slotmon;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

import jmri.jmrix.loconet.SlotListener;
import jmri.jmrix.loconet.LocoNetSlot;
import jmri.jmrix.loconet.LnConstants;
import jmri.jmrix.loconet.locoio.ButtonRenderer;
import jmri.jmrix.loconet.locoio.ButtonEditor;

/**
 * Frame provinging a command station slot manager
 * @author	Bob Jacobsen   Copyright (C) 2001
 * @version	$Revision: 1.3 $
 */
public class SlotMonFrame extends javax.swing.JFrame {

    // GUI member declarations
    javax.swing.JCheckBox 	showAllCheckBox = new javax.swing.JCheckBox();
    SlotMonDataModel		slotModel 	= new SlotMonDataModel(128,16);
    JTable			slotTable	= new JTable(slotModel);
    JScrollPane 		slotScroll	= new JScrollPane(slotTable);

    public SlotMonFrame() {

        // configure items for GUI
        showAllCheckBox.setText("show all slots");
        showAllCheckBox.setVisible(true);
        showAllCheckBox.setSelected(true);
        showAllCheckBox.setToolTipText("if checked, even empty/idle slots will appear");

        // have to shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
        slotTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        TableColumnModel tcm = slotTable.getColumnModel();
        // install a button renderer & editor in the Read column
        ButtonRenderer buttonRenderer = new ButtonRenderer();
        tcm.getColumn(SlotMonDataModel.DISPCOLUMN).setCellRenderer(buttonRenderer);
        TableCellEditor buttonEditor = new ButtonEditor(new JButton());
        tcm.getColumn(SlotMonDataModel.DISPCOLUMN).setCellEditor(buttonEditor);
        // ensure the table rows, columns have enough room for buttons
        slotTable.setRowHeight(new JButton(" Dispatch ").getPreferredSize().height);
        slotTable.getColumnModel().getColumn(SlotMonDataModel.DISPCOLUMN)
			.setPreferredWidth(new JButton(" Dispatch ").getPreferredSize().width);

        // add listener object so checkbox functions
        showAllCheckBox.addActionListener(new CheckNotify(slotModel, showAllCheckBox));

        // general GUI config
        setTitle("Slot Monitor");
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // install items in GUI
        getContentPane().add(showAllCheckBox);
        getContentPane().add(slotScroll);
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
