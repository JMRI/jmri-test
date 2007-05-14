// SlotMonFrame.java

package jmri.jmrix.loconet.slotmon;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import jmri.util.JTableUtil;

/**
 * Frame provinging a command station slot manager.
 * <P>
 * Slots 102 through 127 are normally not used for loco control,
 * so are shown separately.
 *
 * @author	Bob Jacobsen   Copyright (C) 2001
 * @version	$Revision: 1.11 $
 */
public class SlotMonFrame extends jmri.util.JmriJFrame {

    /**
     * Controls whether not-in-use slots are shown
     */
    javax.swing.JCheckBox 	showAllCheckBox = new javax.swing.JCheckBox();
    /**
     * Controls whether system slots (120-127) are shown
     */
    javax.swing.JCheckBox 	showSystemCheckBox = new javax.swing.JCheckBox();

    JButton                     estopAllButton  = new JButton("estop all");
    SlotMonDataModel	slotModel 	= new SlotMonDataModel(128,16);
    JTable				slotTable;
    JScrollPane 		slotScroll;

    public SlotMonFrame() {

    	slotTable	= JTableUtil.sortableDataModel(slotModel);
    	slotScroll	= new JScrollPane(slotTable);

        // configure items for GUI
        showAllCheckBox.setText("Show unused slots");
        showAllCheckBox.setVisible(true);
        showAllCheckBox.setSelected(false);
        showAllCheckBox.setToolTipText("if checked, even empty/idle slots will appear");

        showSystemCheckBox.setText("Show system slots");
        showSystemCheckBox.setVisible(true);
        showSystemCheckBox.setSelected(false);
        showSystemCheckBox.setToolTipText("if checked, slots reserved for system use will be shown");

        slotModel.configureTable(slotTable);

        // add listener object so checkboxes function
        showAllCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            slotModel.showAllSlots(showAllCheckBox.isSelected());
            slotModel.fireTableDataChanged();
            }
        });
        showSystemCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            slotModel.showSystemSlots(showSystemCheckBox.isSelected());
            slotModel.fireTableDataChanged();
            }
        });

        // add listener object so stop all button functions
        estopAllButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                slotModel.estopAll();
            }
        });
        estopAllButton.addMouseListener(new MouseListener() {
            public void mousePressed(MouseEvent e) {
                slotModel.estopAll();
            }
            public void mouseExited(MouseEvent e) {}
            public void mouseEntered(MouseEvent e) {}
            public void mouseReleased(MouseEvent e) {}
            public void mouseClicked(MouseEvent e) {}
        });

        // adjust model to default settings
        slotModel.showAllSlots(showAllCheckBox.isSelected());
        slotModel.showSystemSlots(showSystemCheckBox.isSelected());

        // general GUI config
        setTitle("Slot Monitor");
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // install items in GUI
        JPanel pane1 = new JPanel();
        pane1.setLayout(new FlowLayout());

        pane1.add(showAllCheckBox);
        pane1.add(showSystemCheckBox);
        pane1.add(estopAllButton);

        getContentPane().add(pane1);
        getContentPane().add(slotScroll);
        pack();
        pane1.setMaximumSize(pane1.getSize());
        pack();

    }

    public void dispose() {
        slotModel.dispose();
        slotModel = null;
        slotTable = null;
        slotScroll = null;
        super.dispose();
    }
}
