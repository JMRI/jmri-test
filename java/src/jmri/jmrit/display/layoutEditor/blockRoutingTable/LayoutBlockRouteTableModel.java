// LayoutBlockRouteTableModel.java

package jmri.jmrit.display.layoutEditor.blockRoutingTable;

import jmri.jmrit.display.layoutEditor.*;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;

/**
 * Table data model for display of Roster variable values.
 *<P>
 * Any desired ordering, etc, is handled outside this class.
 *<P>
 * The initial implementation doesn't automatically update when
 * roster entries change, doesn't allow updating of the entries,
 * and only shows some of the fields.  But it's a start....
 *
 * @author              Bob Jacobsen   Copyright (C) 2009, 2010
 * @version             $Revision: 1.3 $
 * @since 2.7.5
 */
public class LayoutBlockRouteTableModel extends javax.swing.table.AbstractTableModel implements PropertyChangeListener {

    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.layoutEditor.LayoutEditorBundle");
    
    public static final int DESTCOL = 0;
    static final int NEXTHOPCOL = 1;
    static final int HOPCOUNTCOL = 2;
    static final int DIRECTIONCOL = 3;
    static final int METRICCOL = 4;
    static final int STATECOL = 5;
    static final int VALIDCOL = 6;

    static final int NUMCOL = 6+1;
    
    boolean editable = false;
    
    public LayoutBlockRouteTableModel(boolean editable, LayoutBlock lBlock) {
        this.editable = editable;
        this.lBlock = lBlock;
        lBlock.addPropertyChangeListener(this);
    }
    
    public int getRowCount() {
        return lBlock.getNumberOfRoutes();
    }

    public int getColumnCount( ){
        return NUMCOL;
    }
    @Override
    public String getColumnName(int col) {
        switch (col) {
        case DESTCOL:         return rb.getString("Destination");
        case NEXTHOPCOL:    return rb.getString("NextHop");
        case HOPCOUNTCOL:    return rb.getString("HopCount");
        case DIRECTIONCOL:   return rb.getString("Direction");
        case METRICCOL:     return rb.getString("Metric");
        case STATECOL:        return rb.getString("State");
        case VALIDCOL:       return rb.getString("Valid");
        
        default:            return "<UNKNOWN>";
        }
    }
    
    @Override
    public Class<?> getColumnClass(int col) {
        if (col == HOPCOUNTCOL) return Integer.class;
        else if (col == METRICCOL) return Integer.class;
        else return String.class;
    }
    
    /**
     * Editable state must be set in ctor.
     */
    @Override
    public boolean isCellEditable(int row, int col) {
        return false;
    }
    
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals("length")) {
            // a new NamedBean is available in the manager
            //updateNameList();
            //log.debug("Table changed length to "+sysNameList.size());
            fireTableDataChanged();
        } else if (matchPropertyName(e)){
            //System.out.println("Matched");
            // a value changed.  Find it, to avoid complete redraw
            int row;
            row = (Integer)e.getNewValue();
            // since we can add columns, the entire row is marked as updated
            //int row = sysNameList.indexOf(name);
            fireTableRowsUpdated(row, row);
        }
    }
    
    	protected boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
		return (e.getPropertyName().indexOf("state")>=0 || e.getPropertyName().indexOf("hop")>=0 
		        || e.getPropertyName().indexOf("metric")>=0) || e.getPropertyName().indexOf("valid")>=0;
	}
    
    /**
     * Provides the empty String if attribute doesn't exist.
     */
    public Object getValueAt(int row, int col) {
        // get roster entry for row
        if (lBlock == null){
        	log.debug("layout Block is null!");
        	return "Error";
        }    
        switch (col) {
        case DESTCOL:       return lBlock.getRouteDestBlockAtIndex(row).getDisplayName();
        case NEXTHOPCOL:    String nextBlock = lBlock.getRouteNextBlockAtIndex(row).getDisplayName();
                            if (nextBlock.equals(lBlock.getDisplayName()))
                                nextBlock = rb.getString("DirectConnect");
                            return nextBlock;
        case HOPCOUNTCOL:   return Integer.valueOf(lBlock.getRouteHopCountAtIndex(row)).intValue();
        case DIRECTIONCOL:  return jmri.Path.decodeDirection(Integer.valueOf(lBlock.getRouteDirectionAtIndex(row)).intValue());
        case METRICCOL:     return Integer.valueOf(lBlock.getRouteMetric(row)).intValue();
        case STATECOL:      return lBlock.getRouteStateAsString(row);
        case VALIDCOL:      String value ="";
                            if(lBlock.getRouteValid(row))
                                value = "*";
                            return value;
        default:            return "<UNKNOWN>";
        }
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        return;
    }

    public int getPreferredWidth(int column) {
        int retval = 20; // always take some width
        retval = Math.max(retval, new javax.swing.JLabel(getColumnName(column)).getPreferredSize().width+15);  // leave room for sorter arrow
        for (int row = 0 ; row < getRowCount(); row++) {
            if (getColumnClass(column).equals(String.class))
                retval = Math.max(retval, new javax.swing.JLabel(getValueAt(row, column).toString()).getPreferredSize().width);
            else if (getColumnClass(column).equals(Integer.class))
                retval = Math.max(retval, new javax.swing.JLabel(getValueAt(row, column).toString()).getPreferredSize().width);
        }    
        return retval+5;
    }
    
    // drop listeners
    public void dispose() {
    }

    public jmri.Manager getManager() { return jmri.InstanceManager.layoutBlockManagerInstance(); }

    LayoutBlock lBlock;
    
    static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LayoutBlockRouteTableModel.class.getName());
}
