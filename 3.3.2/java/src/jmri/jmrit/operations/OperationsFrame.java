//OperationsFrame.java

package jmri.jmrit.operations;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
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
import jmri.implementation.swing.SwingShutDownTask;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.util.com.sun.TableSorter;
import jmri.util.swing.XTableColumnModel;


/**
 * Frame for operations
 * 
 * @author Dan Boudreau Copyright (C) 2008, 2012
 * @version $Revision$
 */

public class OperationsFrame extends jmri.util.JmriJFrame {
	
	@edu.umd.cs.findbugs.annotations.SuppressWarnings(value="MS_CANNOT_BE_FINAL")
	public static SwingShutDownTask trainDirtyTask;

	public OperationsFrame(String s) {
		super(s);
		setEscapeKeyClosesWindow(true);
	}
	
	public OperationsFrame() {
		super();
		setEscapeKeyClosesWindow(true);
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
	
	protected void addItemTop(JPanel p, JComponent c, int x, int y) {
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx = x;
		gc.gridy = y;
		gc.weightx = 100;
		gc.weighty = 100;
		gc.anchor = GridBagConstraints.NORTH;
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
	
	/**
	 * Gets the number of checkboxes(+1) that can fix in one row
	 * see OperationsFrame.minCheckboxes and OperationsFrame.maxCheckboxes
	 * @return the number of checkboxes, minimum is 5 (6 checkboxes)
	 */
	protected int getNumberOfCheckboxes(){
		return getNumberOfCheckboxes(getPreferredSize());
	}

	private int getNumberOfCheckboxes(Dimension size){
		if (size== null)
			return minCheckboxes;	// default is 6 checkboxes per row
		StringBuffer pad = new StringBuffer("X");
		for (int i=0; i<CarTypes.instance().getMaxNameSubTypeLength(); i++)
			pad.append("X");
		
		JCheckBox box = new JCheckBox(pad.toString());
		int number = size.width/(box.getPreferredSize().width);
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
		String tableref = getWindowFrameRef() + ":table";	// NOI18N
		try {
			sorter = (TableSorter) table.getModel();
		} catch (Exception e) {
			log.debug("table "+tableref+" doesn't use sorter");
		} 
		
		// is the table using XTableColumnModel?  If so, there can be hidden columns.
		if (sorter != null && sorter.getColumnCount() != table.getColumnCount()) {
			log.debug("Sort column count: "+sorter.getColumnCount()+" table column count: "+table.getColumnCount()+" XTableColumnModel in use");
			XTableColumnModel tcm = (XTableColumnModel) table.getColumnModel();
			int j=0;
			for (int i = 0; i <sorter.getColumnCount(); i++) {
				int sortStatus = sorter.getSortingStatus(i);
				int width = tcm.getColumnByModelIndex(i).getPreferredWidth();
				boolean visible = tcm.isColumnVisible(tcm.getColumnByModelIndex(i));
				p.setTableColumnPreferences(tableref, sorter.getColumnName(i), i, width, sortStatus, !visible);
				log.debug("Column "+i+" table name: "+table.getColumnName(j)+", sorter name: "+sorter.getColumnName(i));
				//if (!table.getColumnName(j).equals(sorter.getColumnName(i)))
					//log.debug("Column "+i+" table name: "+table.getColumnName(j)+" sorter name: "+sorter.getColumnName(i));
				if (visible)
					j++;
			}
		} 

		else for (int i = 0; i <table.getColumnCount(); i++) {
			int sortStatus = 0;
			if (sorter != null)
				sortStatus = sorter.getSortingStatus(i);
			p.setTableColumnPreferences(tableref, table.getColumnName(i), i, table.getColumnModel().getColumn(i).getPreferredWidth(), sortStatus, false);
		}
	}
	
	/**
	 * Loads the table's width, position, and sorting status from the user preferences file.
	 * @param table The table to be adjusted.
	 * @return true if table has been adjusted by saved xml file.
	 */
	public boolean loadTableDetails(JTable table) {
		UserPreferencesManager p = InstanceManager.getDefault(UserPreferencesManager.class);
		TableSorter sorter = null;
		String tableref = getWindowFrameRef() + ":table";	// NOI18N
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
			} else {
				// name not found so use one that exists
				String name = p.getTableColumnAtNum(tableref, i);
				if (name != null){
					width = p.getTableColumnWidth(tableref, name);
					table.getColumnModel().getColumn(i).setPreferredWidth(width);
				}
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
			if (i != order && order < table.getColumnCount()) {
				table.moveColumn(i, order);
				log.debug("Move column number " + i + " name " +columnName+" to "+order);
				sortDone = false;
			}
		}
		return sortDone;	
	}
	
	protected void clearTableSort(JTable table){
		TableSorter sorter = null;
		try {
			sorter = (TableSorter) table.getModel();
		} catch (Exception e) {
			log.debug("table doesn't use sorter");
		} 
		if (sorter == null)
			return;
		for (int i = 0; i <table.getColumnCount(); i++) {
			sorter.setSortingStatus(i, TableSorter.NOT_SORTED);
		}
	}
	
	protected synchronized void createShutDownTask(){
		if (jmri.InstanceManager.shutDownManagerInstance() != null && trainDirtyTask == null) {
			trainDirtyTask = new SwingShutDownTask(
					"Operations Train Window Check", Bundle.getMessage("PromptQuitWindowNotWritten"),	// NOI18N
					Bundle.getMessage("PromptSaveQuit"), this) {
				public boolean checkPromptNeeded() {
					return !OperationsXml.areFilesDirty();
				}

				public boolean doPrompt() {
					storeValues();
					return true;
				}
				
				public boolean doClose() {
					storeValues();
					return true;
				}
			};
			jmri.InstanceManager.shutDownManagerInstance().register(trainDirtyTask);        
		}
	}
	
	protected void storeValues(){
		OperationsXml.save();
	}
	
	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(OperationsFrame.class.getName());
}
