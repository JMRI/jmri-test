// LocoIOFrame.java

package jmri.jmrix.loconet.locoio;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

import jmri.jmrix.loconet.LocoNetListener;
import jmri.jmrix.loconet.LnTrafficController;
import jmri.jmrix.loconet.LocoNetMessage;

/** 
 * LocoIOFrame.java
 *
 * Description:		Frame displaying and programming a LocoIO configuration
 * @author			Bob Jacobsen   Copyright (C) 2002
 * @version			$Id: LocoIOFrame.java,v 1.1 2002-03-01 00:01:09 jacobsen Exp $
 */
public class LocoIOFrame extends JFrame implements LocoNetListener {

	public LocoIOFrame() {
		super("LocoIO programmer");
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		
		// creating the table done in the variable definitions		
		// have to shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		getContentPane().add(scroll);
		
		TableColumnModel tcm = table.getColumnModel();
		// install a ComboBox editor on the OnMode column
		JComboBox comboOnBox = new JComboBox(LocoIOTableModel.getValidOnModes());
		comboOnBox.setEditable(true);
		DefaultCellEditor onEditor = new DefaultCellEditor(comboOnBox);
		tcm.getColumn(LocoIOTableModel.ONMODECOLUMN).setCellEditor(onEditor);
		
		// install a ComboBox editor on the DoMode column
		if (LocoIOTableModel.DOMODECOLUMN>=0) {
	 		JComboBox comboDoBox = new JComboBox(LocoIOTableModel.getValidDoModes());
 			comboDoBox.setEditable(true);
			DefaultCellEditor doEditor = new DefaultCellEditor(comboDoBox);
			tcm.getColumn(LocoIOTableModel.DOMODECOLUMN).setCellEditor(doEditor);
		}
		
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
		if (LocoIOTableModel.DOMODECOLUMN>=0)
			table.getColumnModel().getColumn(LocoIOTableModel.DOMODECOLUMN)
				.setPreferredWidth(model.getPreferredWidth(LocoIOTableModel.DOMODECOLUMN));
		table.getColumnModel().getColumn(LocoIOTableModel.ADDRCOLUMN)
			.setPreferredWidth(model.getPreferredWidth(LocoIOTableModel.ADDRCOLUMN));
		table.getColumnModel().getColumn(LocoIOTableModel.ONMODECOLUMN)
			.setPreferredWidth(model.getPreferredWidth(LocoIOTableModel.ONMODECOLUMN));

		// add the other buttons in a separate pane
		JPanel p1 = new JPanel();
		p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
			p1.add(new JLabel("LocoIO unit address (hex):"));
			addrField.setMaximumSize(addrField.getPreferredSize());
			p1.add(addrField);
			p1.add(new JLabel("   "));

			p1.add(new JButton("Set address"));
			p1.add(new JLabel("   "));

			readAllButton = new JButton("Read All");
			readAllButton.setEnabled(false);
			p1.add(readAllButton);

			writeAllButton = new JButton("Write All");
			writeAllButton.setEnabled(false);
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
		
		// register with the LocoNet
		init();
	}

	JTextField addrField = new JTextField("  1081");
	
	JButton readAllButton = null;
	JButton writeAllButton = null;
	
	JButton saveButton = null;
	JButton openButton = null;
	
	LocoIOTableModel	model 		= new LocoIOTableModel();
	JTable				table		= new JTable(model);
	JScrollPane 		scroll		= new JScrollPane(table);
	
	public void message(LocoNetMessage msg) {}
	
	// Close the window when the close box is clicked
	void thisWindowClosing(java.awt.event.WindowEvent e) {
		setVisible(false);
		dispose();
	}
	
	public void dispose() {
		// disconnect from the LnTrafficController
		LnTrafficController.instance().removeLocoNetListener(~0,this);
		// take apart the JFrame
		super.dispose();
	}
	
	protected void init() {
		// connect to the LnTrafficController
		LnTrafficController.instance().addLocoNetListener(~0, this);		
	}

	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LocoIOFrame.class.getName());

}
