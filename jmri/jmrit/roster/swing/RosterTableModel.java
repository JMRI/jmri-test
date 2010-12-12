// RosterTableModel.java

package jmri.jmrit.roster.swing;

import jmri.jmrit.roster.*;
import javax.swing.ImageIcon;

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
 * @version             $Revision: 1.9 $
 * @since 2.7.5
 */
public class RosterTableModel extends javax.swing.table.AbstractTableModel {

    public static final int IDCOL = 0;
    static final int ADDRESSCOL = 1;
    static final int ICONCOL = 2;
    static final int DECODERCOL = 3;
    static final int ROADNAMECOL = 4;
    static final int ROADNUMBERCOL = 5;
    static final int MFGCOL = 6;
    static final int OWNERCOL = 7;
    static final int DATEUPDATECOL = 8;

    static final int NUMCOL = 8+1;
    
    boolean editable = false;
    
    public RosterTableModel() {
        this(false);
    }
    
    public RosterTableModel(boolean editable) {
        this.editable = editable;
    }
    
    @Override
    public int getRowCount() {
        return Roster.instance().numEntries();
    }
    @Override
    public int getColumnCount( ){
        return NUMCOL;
    }
    @Override
    public String getColumnName(int col) {
        switch (col) {
        case IDCOL:         return "ID";
        case ADDRESSCOL:    return "DCC Address";
        case DECODERCOL:    return "Decoder";
        case ROADNAMECOL:   return "Road Name";
        case ROADNUMBERCOL: return "Road Number";
        case MFGCOL:        return "Manufacturer";
        case ICONCOL:       return "Icon";
        case OWNERCOL:      return "Owner";
        case DATEUPDATECOL: return "Last Updated";
        default:            return "<UNKNOWN>";
        }
    }
    
    @Override
    public Class<?> getColumnClass(int col) {
        if (col == ADDRESSCOL) return Integer.class;
        if (col == ICONCOL) return ImageIcon.class;
        return String.class;
    }
    
    /**
     * Editable state must be set in ctor.
     */
    @Override
    public boolean isCellEditable(int row, int col) {
        if (col == ADDRESSCOL) return false;
        if (col == DECODERCOL) return false;
        if (col == ICONCOL) return false;
        if (col == DATEUPDATECOL) return false;
        return editable;
    }
    
    jmri.jmrit.roster.RosterIconFactory iconFactory = null;
    
    ImageIcon getIcon(RosterEntry re) {
        // defer image handling to RosterIconFactory
        if (iconFactory == null)
            iconFactory = new jmri.jmrit.roster.RosterIconFactory(Math.max(19, new javax.swing.JLabel(getColumnName(0)).getPreferredSize().height));
        return iconFactory.getIcon(re);
    }
    
    /**
     * Provides the empty String if attribute doesn't exist.
     */
    @Override
    public Object getValueAt(int row, int col) {
        // get roster entry for row
        RosterEntry re = Roster.instance().getEntry(row);
        if (re == null){
        	log.debug("roster entry is null!");
        	return "Error";
        }    
        switch (col) {
        case IDCOL:         return re.getId();
        case ADDRESSCOL:    return Integer.valueOf(re.getDccLocoAddress().getNumber());
        case DECODERCOL:    return re.getDecoderModel();
        case ROADNAMECOL:   return re.getRoadName();
        case ROADNUMBERCOL: return re.getRoadNumber();
        case MFGCOL:        return re.getMfg();
        case ICONCOL:       return getIcon(re);
        case OWNERCOL:      return re.getOwner();
        case DATEUPDATECOL: return re.getDateUpdated();
        default:            return "<UNKNOWN>";
        }
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        // get roster entry for row
        RosterEntry re = Roster.instance().getEntry(row);
        if (re == null){
        	log.warn("roster entry is null!");
        	return;
        }    
        switch (col) {
        case IDCOL:         re.setId((String)value); break;
        case ROADNAMECOL:   re.setRoadName((String)value); break;
        case ROADNUMBERCOL: re.setRoadNumber((String)value); break;
        case MFGCOL:        re.setMfg((String)value); break;
        case OWNERCOL:      re.setOwner((String)value); break;
        default:            log.error("invalid setValueAt column: "+col); return;
        }
        // need to mark as updated
        re.changeDateUpdated();
    }

    public int getPreferredWidth(int column) {
        int retval = 20; // always take some width
        retval = Math.max(retval, new javax.swing.JLabel(getColumnName(column)).getPreferredSize().width+15);  // leave room for sorter arrow
        for (int row = 0 ; row < getRowCount(); row++) {
            if (getColumnClass(column).equals(String.class))
                retval = Math.max(retval, new javax.swing.JLabel(getValueAt(row, column).toString()).getPreferredSize().width);
            else if (getColumnClass(column).equals(Integer.class))
                retval = Math.max(retval, new javax.swing.JLabel(getValueAt(row, column).toString()).getPreferredSize().width);
            else if (getColumnClass(column).equals(ImageIcon.class))
                retval = Math.max(retval, new javax.swing.JLabel((ImageIcon)getValueAt(row, column)).getPreferredSize().width);
        }    
        return retval+5;
    }
    
    // drop listeners
    public void dispose() {
    }
    
    static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RosterTableModel.class.getName());
}
