// LocoIOFrame.java

package jmri.jmrix.loconet.locoio;

import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;

/**
 * Frame displaying and programming a LocoIO configuration.
 *
 * @author	Bob Jacobsen   Copyright (C) 2002
 * @version	$Revision: 1.10 $
 */
public class LocoIOFrame extends JFrame {

    public LocoIOFrame() {
        super("LocoIO programmer");
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // creating the table (done here to ensure order OK)
        status      = new JTextField(14);
        model 	    = new LocoIOTableModel(Integer.valueOf(addrField.getText(),16).intValue(), status);
        table	    = new JTable(model);
        scroll	    = new JScrollPane(table);

        // have to shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        getContentPane().add(scroll);

        TableColumnModel tcm = table.getColumnModel();
        // install a ComboBox editor on the OnMode column
        JComboBox comboOnBox = new JComboBox(LocoIOTableModel.getValidOnModes());
        comboOnBox.setEditable(true);
        DefaultCellEditor onEditor = new DefaultCellEditor(comboOnBox);
        tcm.getColumn(LocoIOTableModel.ONMODECOLUMN).setCellEditor(onEditor);

        // install a button renderer & editor in the Read column
        ButtonRenderer buttonRenderer = new ButtonRenderer();
        tcm.getColumn(LocoIOTableModel.READCOLUMN).setCellRenderer(buttonRenderer);
        TableCellEditor buttonEditor = new ButtonEditor(new JButton());
        tcm.getColumn(LocoIOTableModel.READCOLUMN).setCellEditor(buttonEditor);

        // install those same ones in the Write, Compare columns
        tcm.getColumn(LocoIOTableModel.WRITECOLUMN).setCellRenderer(buttonRenderer);
        tcm.getColumn(LocoIOTableModel.WRITECOLUMN).setCellEditor(buttonEditor);
        tcm.getColumn(LocoIOTableModel.CAPTURECOLUMN).setCellRenderer(buttonRenderer);
        tcm.getColumn(LocoIOTableModel.CAPTURECOLUMN).setCellEditor(buttonEditor);

        // ensure the table rows, columns have enough room for buttons
        table.setRowHeight(new JButton("Capture").getPreferredSize().height);
        table.getColumnModel().getColumn(LocoIOTableModel.CAPTURECOLUMN)
            .setPreferredWidth(model.getPreferredWidth(LocoIOTableModel.CAPTURECOLUMN));
        table.getColumnModel().getColumn(LocoIOTableModel.READCOLUMN)
            .setPreferredWidth(model.getPreferredWidth(LocoIOTableModel.READCOLUMN));
        table.getColumnModel().getColumn(LocoIOTableModel.WRITECOLUMN)
            .setPreferredWidth(model.getPreferredWidth(LocoIOTableModel.WRITECOLUMN));
        table.getColumnModel().getColumn(LocoIOTableModel.ADDRCOLUMN)
            .setPreferredWidth(model.getPreferredWidth(LocoIOTableModel.ADDRCOLUMN));
        table.getColumnModel().getColumn(LocoIOTableModel.ONMODECOLUMN)
            .setPreferredWidth(model.getPreferredWidth(LocoIOTableModel.ONMODECOLUMN));

        // add the other buttons in a separate pane
        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
        p1.add(new JLabel("LocoIO address (hex):"));
        addrField.setMaximumSize(addrField.getPreferredSize());
        p1.add(addrField);
        addrSetButton = new JButton("Set address");
        addrSetButton.setEnabled(false);
        p1.add(addrSetButton);
        p1.add(new JSeparator(JSeparator.VERTICAL));

        readAllButton = new JButton("Read All");
        p1.add(readAllButton);

        writeAllButton = new JButton("Write All");
        p1.add(writeAllButton);

        getContentPane().add(p1);
        JPanel p2 = new JPanel();
        p2.setLayout(new BoxLayout(p2, BoxLayout.X_AXIS));
        openButton = new JButton("Open...");
        openButton.setEnabled(false);
        p2.add(openButton);

        saveButton = new JButton("Save...");
        saveButton.setEnabled(false);
        p2.add(saveButton);

        getContentPane().add(p2);

        // updating the address needs to be conveyed to the table
        addrField.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                model.setUnitAddress(Integer.valueOf(addrField.getText(),16).intValue());
            }
        });

        addrField.addFocusListener( new FocusListener() {
            public void focusGained(FocusEvent e) {}
            public void focusLost(FocusEvent e) {
                model.setUnitAddress(Integer.valueOf(addrField.getText(),16).intValue());
            }
        });

        // install read all, write all button handlers
        readAllButton.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                model.readAll();
            }
        });
        writeAllButton.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                model.writeAll();
            }
        });

        // add status
        getContentPane().add(status);

        // notice the window is closing
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                thisWindowClosing(e);
            }
        });

        // and prep for display
        pack();
    }

    JTextField addrField = new JTextField("0151");
    JTextField status = null;

    JButton addrSetButton = null;

    JButton readAllButton = null;
    JButton writeAllButton = null;

    JButton saveButton = null;
    JButton openButton = null;

    LocoIOTableModel	model;
    JTable		table;
    JScrollPane 	scroll;

    // Close the window when the close box is clicked
    void thisWindowClosing(java.awt.event.WindowEvent e) {
        setVisible(false);
        dispose();
    }

    public void dispose() {
        // dispose of the model
        model.dispose();
        // take apart the JFrame
        super.dispose();
        model = null;
        table = null;
        scroll = null;
        readAllButton  = null;
        writeAllButton = null;
        addrField = null;
        saveButton = null;
        openButton = null;
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LocoIOFrame.class.getName());

}
