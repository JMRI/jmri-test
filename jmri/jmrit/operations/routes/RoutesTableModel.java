// RoutesTableModel.java

package jmri.jmrit.operations.routes;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;

import jmri.jmrit.operations.setup.Control;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

/**
 * Table Model for edit of routes used by operations
 *
 * @author Daniel Boudreau Copyright (C) 2008
 * @version   $Revision: 1.7 $
 */
public class RoutesTableModel extends javax.swing.table.AbstractTableModel implements PropertyChangeListener {

	static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.routes.JmritOperationsRoutesBundle");
   
    RouteManager manager;						// There is only one manager
 
    // Defines the columns
    private static final int IDCOLUMN   = 0;
    private static final int NAMECOLUMN   = IDCOLUMN+1;

    private static final int EDITCOLUMN = NAMECOLUMN+1;
    
    private static final int HIGHESTCOLUMN = EDITCOLUMN+1;

    public RoutesTableModel() {
        super();
        manager = RouteManager.instance();
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
    	removePropertyChangeRoutes();
    	
		if (_sort == SORTBYID)
			sysList = manager.getRoutesByIdList();
		else
			sysList = manager.getRoutesByNameList();
		// and add them back in
		for (int i = 0; i < sysList.size(); i++){
//			log.debug("route ids: " + (String) sysList.get(i));
			manager.getRouteById(sysList.get(i))
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
		// set column preferred widths
		table.getColumnModel().getColumn(IDCOLUMN).setPreferredWidth(30);
		table.getColumnModel().getColumn(NAMECOLUMN).setPreferredWidth(200);
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

        case EDITCOLUMN: return "";		//edit column
        default: return "unknown";
        }
    }

    public Class<?> getColumnClass(int col) {
        switch (col) {
        case IDCOLUMN: return String.class;
        case NAMECOLUMN: return String.class;
        case EDITCOLUMN: return JButton.class;
        default: return null;
        }
    }

    public boolean isCellEditable(int row, int col) {
        switch (col) {
        case EDITCOLUMN: 
        	return true;
        default: 
        	return false;
        }
    }

    public Object getValueAt(int row, int col) {
    	// Funky code to put the ref frame in focus after the edit table buttons is used.
    	// The button editor for the table does a repaint of the button cells after the setValueAt code
    	// is called which then returns the focus back onto the table.  We need the edit frame
    	// in focus.
    	if (focusRef){
    		focusRef = false;
    		ref.requestFocus();
    	}
    	String locId = sysList.get(row);
    	Route l = manager.getRouteById(locId);
        switch (col) {
        case IDCOLUMN: return l.getId();
        case NAMECOLUMN: return l.getName();

        case EDITCOLUMN: return rb.getString("Edit");
        default: return "unknown "+col;
        }
    }

    public void setValueAt(Object value, int row, int col) {
        switch (col) {
        case EDITCOLUMN: editRoute (row);
        	break;
        default:
            break;
        }
    }
    boolean focusRef = false;
    RouteEditFrame ref = null;
    private void editRoute (int row){
    	log.debug("Edit route");
    	if (ref != null)
    		ref.dispose();
   		ref = new RouteEditFrame();
    	ref.setTitle(rb.getString("TitleRouteEdit"));
    	Route route = manager.getRouteById(sysList.get(row));
    	ref.initComponents(route);
    	focusRef = true;
    }

    public void propertyChange(PropertyChangeEvent e) {
    	if (Control.showProperty && log.isDebugEnabled()) log.debug("Property change " +e.getPropertyName()+ " old: "+e.getOldValue()+ " new: "+e.getNewValue());
    	 if (e.getPropertyName().equals("listLength")) {
             updateList();
             fireTableDataChanged();
    	 }
    	 else {
    		 String locId = ((Route) e.getSource()).getId();
    		 int row = sysList.indexOf(locId);
    		 if (Control.showProperty && log.isDebugEnabled()) log.debug("Update route table row: "+row + " id: " + locId);
    		 if (row >= 0)
    			 fireTableRowsUpdated(row, row);
    	 }
    }
    
    private void removePropertyChangeRoutes() {
    	if (sysList != null) {
    		for (int i = 0; i < sysList.size(); i++) {
    			// if object has been deleted, it's not here; ignore it
    			Route l = manager.getRouteById(sysList.get(i));
    			if (l != null)
    				l.removePropertyChangeListener(this);
    		}
    	}
    }

    public void dispose() {
        if (log.isDebugEnabled()) log.debug("dispose");
        if (ref != null)
        	ref.dispose();
        manager.removePropertyChangeListener(this);
        removePropertyChangeRoutes();
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RoutesTableModel.class.getName());
}

