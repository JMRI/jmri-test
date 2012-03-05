//OperationsFrame.java

package jmri.jmrit.operations;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTable;

import jmri.InstanceManager;
import jmri.UserPreferencesManager;
import jmri.jmrit.operations.setup.Control;
import jmri.util.com.sun.TableSorter;


/**
 * Frame for operations
 * 
 * @author Dan Boudreau Copyright (C) 2008, 2012
 * @version $Revision$
 */

public class OperationsFrame extends jmri.util.JmriJFrame {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.JmritOperationsBundle");


	public OperationsFrame(String s) {
		super(s);
	}
	
	public OperationsFrame() {
		super();
	}

	protected void addItem(JComponent c, int x, int y) {
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx = x;
		gc.gridy = y;
		gc.weightx = 100.0;
		gc.weighty = 100.0;
		getContentPane().add(c, gc);
	}

	protected void addItemLeft(JComponent c, int x, int y) {
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx = x;
		gc.gridy = y;
		gc.weightx = 100.0;
		gc.weighty = 100.0;
		gc.anchor = GridBagConstraints.WEST;
		getContentPane().add(c, gc);
	}
	protected void addItemWidth(JComponent c, int width, int x, int y) {
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx = x;
		gc.gridy = y;
		gc.gridwidth = width;
		gc.weightx = 100.0;
		gc.weighty = 100.0;
		getContentPane().add(c, gc);
	}
	
	protected void addItem(JPanel p, JComponent c, int x, int y) {
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx = x;
		gc.gridy = y;
		gc.weightx = 100.0;
		gc.weighty = 100.0;
		p.add(c, gc);
	}
	
	protected void addItemLeft(JPanel p, JComponent c, int x, int y) {
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx = x;
		gc.gridy = y;
		gc.weightx = 100.0;
		gc.weighty = 100.0;
		gc.anchor = GridBagConstraints.WEST;
		p.add(c, gc);
	}
	
	protected void addItemWidth(JPanel p, JComponent c, int width, int x, int y) {
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx = x;
		gc.gridy = y;
		gc.gridwidth = width;
		gc.weightx = 100.0;
		gc.weighty = 100.0;
		gc.anchor = GridBagConstraints.WEST;
		p.add(c, gc);
	}

	private final int minCheckboxes = 5;
	private final int maxCheckboxes = 11;
	private static int number = 0;
	
	/**
	 * Gets the number of checkboxes(+1) that can fix in one row
	 * @param size window size
	 * @return the number of checkboxes, minimum is 5 (6 checkboxes)
	 */
	protected int getNumberOfCheckboxes(){
		return getNumberOfCheckboxes(getPreferredSize());
	}

	private int getNumberOfCheckboxes(Dimension size){
		if (size== null)
			return minCheckboxes;	// default is 6 checkboxes per row
		String pad ="X";
		for (int i=0; i<Control.MAX_LEN_STRING_ATTRIBUTE; i++) {
			pad = pad+"X";
		}
		JCheckBox box = new JCheckBox(pad);
		number = size.width/(box.getPreferredSize().width);
		if (number < minCheckboxes)
			number = minCheckboxes;
		if (number > maxCheckboxes)
			number = maxCheckboxes;
		return number;
	}

	protected void addButtonAction(JButton b) {
		b.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				buttonActionPerformed(e);
			}
		});
	}
	
	protected void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		log.debug("button action not overridden");
	}
	
	protected void addRadioButtonAction(JRadioButton b) {
		b.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				radioButtonActionPerformed(e);
			}
		});
	}
	
	protected void radioButtonActionPerformed(java.awt.event.ActionEvent ae) {
		log.debug("radio button action not overridden");
	}
	
	protected void addCheckBoxAction(JCheckBox b) {
		b.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				checkBoxActionPerformed(e);
			}
		});
	}
	
	protected void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
		log.debug("check box action not overridden");
	}
	
	protected void addSpinnerChangeListerner(JSpinner s) {
		s.addChangeListener(new javax.swing.event.ChangeListener() {
			public void stateChanged(javax.swing.event.ChangeEvent e) {
				spinnerChangeEvent(e);
			}
		});
	}
	
	protected void spinnerChangeEvent(javax.swing.event.ChangeEvent ae) {
		log.debug("spinner action not overridden");
	}
	
	protected void addComboBoxAction(JComboBox b) {
		b.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				comboBoxActionPerformed(e);
			}
		});
	}
	
	protected void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
		log.debug("combo box action not overridden");
	}
	
	protected void selectNextItemComboBox(JComboBox b){
		int newIndex = b.getSelectedIndex()+1;
		if (newIndex<b.getItemCount())
			b.setSelectedIndex(newIndex);
	}
	
	/**
	 * Saves the table's width, position, and sorting status in the user preferences file
	 * @param table Table to be saved.
	 */
	protected void saveTableDetails(JTable table) {
		UserPreferencesManager p = InstanceManager.getDefault(UserPreferencesManager.class);
		if (p == null)
			return;
		TableSorter sorter = null;
		String tableref = getWindowFrameRef() + ":table";
		try {
			sorter = (TableSorter) table.getModel();
		} catch (Exception e) {
			log.debug("table "+tableref+" doesn't use sorter");
		} 

		for (int i = 0; i <table.getColumnCount(); i++) {
			int sortStatus = 0;
			if (sorter != null)
				sortStatus = sorter.getSortingStatus(i);
			p.setTableColumnPreferences(tableref, table.getColumnName(i), i, table.getColumnModel().getColumn(i).getPreferredWidth(), sortStatus);
		}
	}
	
	/**
	 * Loads the table's width, position, and sorting status from the user preferences file
	 * @param table Table to be loaded
	 */
	public boolean loadTableDetails(JTable table) {
		UserPreferencesManager p = InstanceManager.getDefault(UserPreferencesManager.class);
		TableSorter sorter = null;
		String tableref = getWindowFrameRef() + ":table";
		if (p == null || p.getTablesColumnList(tableref).size() == 0)
			return false;
		try {
			sorter = (TableSorter) table.getModel();
		} catch (Exception e) {
			log.debug("table "+tableref+" doesn't use sorter");
		}    
		// bubble sort
		int count = 0;
		while (!sortTable(table, p, tableref) && count < 10) {
			count++;
		}
		// Some tables have more than one name, so use the current one for size
		for (int i = 0; i <table.getColumnCount(); i++) {
			String columnName = table.getColumnName(i);
			int sort = p.getTableColumnSort(tableref, columnName);
			if (sorter != null)
				sorter.setSortingStatus(i, sort);		
			int width = p.getTableColumnWidth(tableref, columnName);
			if (width != -1) {
				table.getColumnModel().getColumn(i).setPreferredWidth(width);
			}
		}
		return true;
	}
	
	private boolean sortTable(JTable table, UserPreferencesManager p, String tableref){
		boolean sortDone = true;
		for (int i = 0; i <table.getColumnCount(); i++) {
			String columnName = table.getColumnName(i);
			int order = p.getTableColumnOrder(tableref, columnName);
			//log.debug("Column number " + i + " name " +columnName+" order "+order);
			if (order == -1){
				log.debug("Column name "+columnName+" not found in user preference file");
				continue;
			}
			if (i != order) {
				table.moveColumn(i, order);
				log.debug("Move column number " + i + " name " +columnName+" to "+order);
				sortDone = false;
			}
		}
		return sortDone;	
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(OperationsFrame.class.getName());
}
