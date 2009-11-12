// SchedulesTableModel.java

package jmri.jmrit.operations.locations;

import java.beans.*;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;

import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;

import jmri.jmrit.operations.setup.Control;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;
import java.util.Hashtable;

/**
 * Table Model for edit of schedules used by operations
 *
 * @author Daniel Boudreau Copyright (C) 2009
 * @version   $Revision: 1.8 $
 */
public class SchedulesTableModel extends javax.swing.table.AbstractTableModel implements PropertyChangeListener {

	static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.locations.JmritOperationsLocationsBundle");
   
    ScheduleManager manager;						// There is only one manager
 
    // Defines the columns
    private static final int IDCOLUMN   = 0;
    private static final int NAMECOLUMN   = 1;
    private static final int SCH_STATUSCOLUMN = 2;
    private static final int SIDINGSCOLUMN = 3;
    private static final int STATUSCOLUMN = 4;
    private static final int EDITCOLUMN = 5;
    
    private static final int HIGHESTCOLUMN = EDITCOLUMN+1;

    public SchedulesTableModel() {
        super();
        manager = ScheduleManager.instance();
        manager.addPropertyChangeListener(this);
        updateList();
    }
    
    public final int SORTBYNAME = 1;
    public final int SORTBYID = 2;
    
    private int _sort = SORTBYNAME;
    
    public void setSort (int sort){
    	_sort = sort;
        updateList();
        fireTableDataChanged();
    }
     
    synchronized void updateList() {
		// first, remove listeners from the individual objects
    	removePropertyChangeSchedules();
    	
		if (_sort == SORTBYID)
			sysList = manager.getSchedulesByIdList();
		else
			sysList = manager.getSchedulesByNameList();
		// and add them back in
		for (int i = 0; i < sysList.size(); i++){
//			log.debug("schedule ids: " + (String) sysList.get(i));
			manager.getScheduleById(sysList.get(i))
					.addPropertyChangeListener(this);
		}
	}

	List<String> sysList = null;
    
	void initTable(JTable table) {
		// Install the button handlers
		TableColumnModel tcm = table.getColumnModel();
		ButtonRenderer buttonRenderer = new ButtonRenderer();
		TableCellEditor buttonEditor = new ButtonEditor(new javax.swing.JButton());
		tcm.getColumn(EDITCOLUMN).setCellRenderer(buttonRenderer);
		tcm.getColumn(EDITCOLUMN).setCellEditor(buttonEditor);
        table.setDefaultRenderer(JComboBox.class, new jmri.jmrit.symbolicprog.ValueRenderer());
        table.setDefaultEditor(JComboBox.class, new jmri.jmrit.symbolicprog.ValueEditor());

		// set column preferred widths
		table.getColumnModel().getColumn(IDCOLUMN).setPreferredWidth(40);
		table.getColumnModel().getColumn(NAMECOLUMN).setPreferredWidth(150);
		table.getColumnModel().getColumn(SCH_STATUSCOLUMN).setPreferredWidth(50);
		table.getColumnModel().getColumn(SIDINGSCOLUMN).setPreferredWidth(300);
		table.getColumnModel().getColumn(STATUSCOLUMN).setPreferredWidth(150);
		table.getColumnModel().getColumn(EDITCOLUMN).setPreferredWidth(70);
		// have to shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	}
    
    public int getRowCount() { return sysList.size(); }

    public int getColumnCount( ){ return HIGHESTCOLUMN;}

    public String getColumnName(int col) {
        switch (col) {
        case IDCOLUMN: return rb.getString("Id");
        case NAMECOLUMN: return rb.getString("Name");
        case SCH_STATUSCOLUMN: return rb.getString("Status");
        case SIDINGSCOLUMN: return rb.getString("Sidings");
        case STATUSCOLUMN: return rb.getString("StatusSiding");
        case EDITCOLUMN: return "";		//edit column
        default: return "unknown";
        }
    }

    public Class<?> getColumnClass(int col) {
        switch (col) {
        case IDCOLUMN: return String.class;
        case NAMECOLUMN: return String.class;
        case SCH_STATUSCOLUMN: return String.class;
        case SIDINGSCOLUMN: return JComboBox.class;
        case STATUSCOLUMN: return String.class;
        case EDITCOLUMN: return JButton.class;
        default: return null;
        }
    }

    public boolean isCellEditable(int row, int col) {
        switch (col) {
        case EDITCOLUMN: 
        case SIDINGSCOLUMN:
        	return true;
        default: 
        	return false;
        }
    }

    public Object getValueAt(int row, int col) {
       	// Funky code to put the sef frame in focus after the edit table buttons is used.
    	// The button editor for the table does a repaint of the button cells after the setValueAt code
    	// is called which then returns the focus back onto the table.  We need the edit frame
    	// in focus.
    	if (focusSef){
    		focusSef = false;
    		sef.requestFocus();
    	}
       	if (row >= sysList.size())
    		return "ERROR row "+row;
    	String id = sysList.get(row);
    	Schedule s = manager.getScheduleById(id);
       	if (s == null)
    		return "ERROR schedule unknown "+row;
        switch (col) {
        case IDCOLUMN: return s.getId();
        case NAMECOLUMN: return s.getName();
        case SCH_STATUSCOLUMN: return getScheduleStatus(row);
        case SIDINGSCOLUMN: {
        	JComboBox box = manager.getSidingsByScheduleComboBox(s);
        	String index = comboSelect.get(sysList.get(row));
        	if (index != null){
        		box.setSelectedIndex(Integer.parseInt(index));
        	}
        	return box;
        }
        case STATUSCOLUMN: return getSidingStatus(row);
        case EDITCOLUMN: return rb.getString("Edit");
        default: return "unknown "+col;
        }
    }

    public void setValueAt(Object value, int row, int col) {
        switch (col) {
        case EDITCOLUMN: editSchedule(row);
        	break;
        case SIDINGSCOLUMN: selectJComboBox(value, row);
        	break;
        default:
            break;
        }
    }

    boolean focusSef = false;
    ScheduleEditFrame sef = null;
    private void editSchedule (int row){
    	log.debug("Edit schedule");
    	if (sef != null)
    		sef.dispose();
    	Schedule s = manager.getScheduleById(sysList.get(row));
    	LocationTrackPair ltp = getLocationTrackPair(row);
    	if (ltp == null){
    		log.debug("Need location track pair");
			JOptionPane.showMessageDialog(null,
					MessageFormat.format(rb.getString("AssignSchedule"),new Object[]{s.getName()}),
					MessageFormat.format(rb.getString("CanNotSchedule"),new Object[]{rb.getString("Edit")}),
					JOptionPane.ERROR_MESSAGE);
    		return;
    	}
       	sef = new ScheduleEditFrame();
    	sef.setTitle(MessageFormat.format(rb.getString("TitleScheduleEdit"), new Object[]{ltp.getTrack().getName()}));
    	sef.initComponents(s, ltp.getLocation(), ltp.getTrack());
    	focusSef = true;
    }

    protected Hashtable<String, String> comboSelect = new Hashtable<String, String>();
    private void selectJComboBox (Object value, int row){
    	String id = sysList.get(row);
    	JComboBox box = (JComboBox)value;
    	comboSelect.put(id, Integer.toString(box.getSelectedIndex()));
    	fireTableRowsUpdated(row, row);
    }
    
    private LocationTrackPair getLocationTrackPair(int row){
       	Schedule s = manager.getScheduleById(sysList.get(row));
       	JComboBox box = manager.getSidingsByScheduleComboBox(s);
    	String index = comboSelect.get(sysList.get(row));
    	LocationTrackPair ltp;
    	if (index != null){
    		ltp = (LocationTrackPair)box.getItemAt(Integer.parseInt(index));
    	} else {
    		ltp = (LocationTrackPair)box.getItemAt(0);
    	}
    	return ltp;
    }
    
    private String getScheduleStatus(int row){
    	Schedule sch = manager.getScheduleById(sysList.get(row));
       	JComboBox box = manager.getSidingsByScheduleComboBox(sch); 
       	for (int i=0; i<box.getItemCount(); i++){
           	LocationTrackPair ltp = (LocationTrackPair)box.getItemAt(i);
           	String status = ltp.getTrack().checkScheduleValid(ltp.getLocation());
           	if (!status.equals(""))
           		return rb.getString("Error");
       	}
       	return rb.getString("Okay");
    }
    
    private String getSidingStatus(int row){
     	LocationTrackPair ltp = getLocationTrackPair(row);
    	if (ltp == null)
    		return "";
    	String status = ltp.getTrack().checkScheduleValid(ltp.getLocation());
    	if (!status.equals(""))
    		return status;
    	return rb.getString("Okay");
    }
    
    private void removePropertyChangeSchedules() {
    	if (sysList != null) {
    		for (int i = 0; i < sysList.size(); i++) {
    			// if object has been deleted, it's not here; ignore it
    			Schedule l = manager.getScheduleById(sysList.get(i));
    			if (l != null)
    				l.removePropertyChangeListener(this);
    		}
    	}
    }

    public void dispose() {
        if (log.isDebugEnabled()) log.debug("dispose");
       	if (sef != null)
    		sef.dispose();
        manager.removePropertyChangeListener(this);
        removePropertyChangeSchedules();
    }

    //check for change in number of schedules, or a change in a schedule
    public void propertyChange(PropertyChangeEvent e) {
    	if (Control.showProperty && log.isDebugEnabled()) log.debug("Property change " +e.getPropertyName()+ " old: "+e.getOldValue()+ " new: "+e.getNewValue());
    	 if (e.getPropertyName().equals(ScheduleManager.LISTLENGTH_CHANGED_PROPERTY)) {
             updateList();
             fireTableDataChanged();
    	 }
    	 else {
    		 String id = ((Schedule) e.getSource()).getId();
    		 int row = sysList.indexOf(id);
    		 if (Control.showProperty && log.isDebugEnabled()) log.debug("Update schedule table row: "+row + " id: " + id);
    		 if (row >= 0)
    			 fireTableRowsUpdated(row, row);
    	 }
    }
    

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SchedulesTableModel.class.getName());
}

