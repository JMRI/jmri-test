// RoutesTableModel.java

package jmri.jmrit.operations.routes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;

import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.setup.Control;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

/**
 * Table Model for edit of routes used by operations
 * 
 * @author Daniel Boudreau Copyright (C) 2008
 * @version $Revision$
 */
public class RoutesTableModel extends javax.swing.table.AbstractTableModel implements PropertyChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6951642152186049680L;

	RouteManager manager; // There is only one manager

	// Defines the columns
	public static final int IDCOLUMN = 0;
	public static final int NAMECOLUMN = IDCOLUMN + 1;
	public static final int COMMENTCOLUMN = NAMECOLUMN + 1;
	public static final int STATUSCOLUMN = COMMENTCOLUMN + 1;
	public static final int EDITCOLUMN = STATUSCOLUMN + 1;

	private static final int HIGHESTCOLUMN = EDITCOLUMN + 1;

	public RoutesTableModel() {
		super();
		manager = RouteManager.instance();
		manager.addPropertyChangeListener(this);
		LocationManager.instance().addPropertyChangeListener(this);
		updateList();
	}

	public final int SORTBYNAME = 1;
	public final int SORTBYID = 2;

	private int _sort = SORTBYNAME;

	public void setSort(int sort) {
		synchronized (this) {
			_sort = sort;
		}
		updateList();
		fireTableDataChanged();
	}

	private synchronized void updateList() {
		// first, remove listeners from the individual objects
		removePropertyChangeRoutes();

		if (_sort == SORTBYID)
			sysList = manager.getRoutesByIdList();
		else
			sysList = manager.getRoutesByNameList();
		// and add them back in
		for (Route route : sysList) {
			route.addPropertyChangeListener(this);
		}
	}

	List<Route> sysList = null;

	void initTable(JTable table) {
		// Install the button handlers
		TableColumnModel tcm = table.getColumnModel();
		ButtonRenderer buttonRenderer = new ButtonRenderer();
		TableCellEditor buttonEditor = new ButtonEditor(new javax.swing.JButton());
		tcm.getColumn(EDITCOLUMN).setCellRenderer(buttonRenderer);
		tcm.getColumn(EDITCOLUMN).setCellEditor(buttonEditor);
		// set column preferred widths
		table.getColumnModel().getColumn(IDCOLUMN).setPreferredWidth(30);
		table.getColumnModel().getColumn(NAMECOLUMN).setPreferredWidth(220);
		table.getColumnModel().getColumn(COMMENTCOLUMN).setPreferredWidth(300);
		table.getColumnModel().getColumn(STATUSCOLUMN).setPreferredWidth(70);
		table.getColumnModel().getColumn(EDITCOLUMN).setPreferredWidth(80);
		// have to shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	}

	public synchronized int getRowCount() {
		return sysList.size();
	}

	public int getColumnCount() {
		return HIGHESTCOLUMN;
	}

	public String getColumnName(int col) {
		switch (col) {
		case IDCOLUMN:
			return Bundle.getMessage("Id");
		case NAMECOLUMN:
			return Bundle.getMessage("Name");
		case COMMENTCOLUMN:
			return Bundle.getMessage("Comment");
		case STATUSCOLUMN:
			return Bundle.getMessage("Status");
		case EDITCOLUMN:
			return ""; // edit column
		default:
			return "unknown"; // NOI18N
		}
	}

	public Class<?> getColumnClass(int col) {
		switch (col) {
		case IDCOLUMN:
			return String.class;
		case NAMECOLUMN:
			return String.class;
		case COMMENTCOLUMN:
			return String.class;
		case STATUSCOLUMN:
			return String.class;
		case EDITCOLUMN:
			return JButton.class;
		default:
			return null;
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

	public synchronized Object getValueAt(int row, int col) {
		// Funky code to put the ref frame in focus after the edit table buttons is used.
		// The button editor for the table does a repaint of the button cells after the setValueAt code
		// is called which then returns the focus back onto the table. We need the edit frame
		// in focus.
		if (focusRef) {
			focusRef = false;
			ref.requestFocus();
		}
		if (row >= sysList.size())
			return "ERROR unknown " + row; // NOI18N
		Route r = sysList.get(row);
		if (r == null)
			return "ERROR route unknown " + row; // NOI18N
		switch (col) {
		case IDCOLUMN:
			return r.getId();
		case NAMECOLUMN:
			return r.getName();
		case COMMENTCOLUMN:
			return r.getComment();
		case STATUSCOLUMN:
			return r.getStatus();
		case EDITCOLUMN:
			return Bundle.getMessage("Edit");
		default:
			return "unknown " + col; // NOI18N
		}
	}

	public void setValueAt(Object value, int row, int col) {
		switch (col) {
		case EDITCOLUMN:
			editRoute(row);
			break;
		default:
			break;
		}
	}

	boolean focusRef = false;
	RouteEditFrame ref = null;

	private synchronized void editRoute(int row) {
		log.debug("Edit route");
		if (ref != null)
			ref.dispose();
		ref = new RouteEditFrame();
		Route route = sysList.get(row);
		ref.initComponents(route);
		focusRef = true;
	}

	public void propertyChange(PropertyChangeEvent e) {
		if (Control.showProperty && log.isDebugEnabled())
			log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
					.getNewValue());
		if (e.getPropertyName().equals(LocationManager.LISTLENGTH_CHANGED_PROPERTY)) {
			fireTableDataChanged();
		} else if (e.getPropertyName().equals(RouteManager.LISTLENGTH_CHANGED_PROPERTY)) {
			updateList();
			fireTableDataChanged();
		} else if (e.getSource().getClass().equals(Route.class)) {
			Route route = (Route) e.getSource();
			int row = sysList.indexOf(route);
			if (Control.showProperty && log.isDebugEnabled())
				log.debug("Update route table row: {} id: {}", row, route.getId());
			if (row >= 0)
				fireTableRowsUpdated(row, row);
		}
	}

	private synchronized void removePropertyChangeRoutes() {
		if (sysList != null) {
			for (Route route : sysList) {
				route.removePropertyChangeListener(this);
			}
		}
	}

	public void dispose() {
		if (log.isDebugEnabled())
			log.debug("dispose");
		if (ref != null)
			ref.dispose();
		manager.removePropertyChangeListener(this);
		LocationManager.instance().removePropertyChangeListener(this);
		removePropertyChangeRoutes();
	}

	static Logger log = LoggerFactory.getLogger(RoutesTableModel.class.getName());
}
