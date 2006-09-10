// ConsistDataModel.java

package jmri.jmrit.consisttool;

import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

import jmri.InstanceManager;
import jmri.Consist;
import jmri.DccLocoAddress;
import jmri.ConsistManager;
import jmri.jmrit.roster.*;

import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;



/**
 * Table data model for display of consist information
 * @author		Paul Bender Copyright (c) 2004-2005
 * @version		$Revision: 1.4 $
 */

public class ConsistDataModel extends javax.swing.table.AbstractTableModel {
	static private final int ADDRCOLUMN = 0;    // Locomotive address
	static private final int ROSTERCOLUMN = 1;  // Roster Entry, this exists
	static private final int DIRECTIONCOLUMN = 2;  // Relative Direction
	static private final int DELCOLUMN = 3;     // Remove Button

	static private final int NUMCOLUMN = 4;

	// a place holder for a consist and Consist Manager objects.
	private Consist _consist = null;
	private ConsistManager ConsistMan = null;
	private DccLocoAddress ConsistAddress;

	// Construct a new instance
	ConsistDataModel(int row, int column) {
		ConsistMan = InstanceManager.consistManagerInstance();
	}

	void initTable(JTable ConsistTable) {
		// Install the button handlers for the Delete Buttons
		TableColumnModel tcm = ConsistTable.getColumnModel();
		ButtonRenderer buttonRenderer = new ButtonRenderer();
		tcm.getColumn(DELCOLUMN).setCellRenderer(buttonRenderer);
		TableCellEditor buttonEditor = new ButtonEditor(new javax.swing.JButton());
		tcm.getColumn(DELCOLUMN).setCellEditor(buttonEditor);
	}


	public void setConsist(Consist consist) {
		log.debug("Setting Consist");
		_consist=consist;
		fireTableDataChanged();
	}

	public void setConsist(DccLocoAddress Address) {
		log.debug("Setting Consist using address: "+Address.toString());
		ConsistAddress = Address;
		_consist = ConsistMan.getConsist(Address);
		fireTableDataChanged();
	}

	public Consist getConsist(){
		return _consist;
	}

        public int getRowCount() {
	    try {
		  return(_consist.getConsistList().size()); 
		} catch(NullPointerException e) { 
		  return(0); 
		}
	}

        public int getColumnCount() { return NUMCOLUMN; }

        public String getColumnName(int col) {
	   switch(col) {
	      case ADDRCOLUMN: return "Address";
	      case ROSTERCOLUMN: return "Roster Entry";
	      case DIRECTIONCOLUMN: return "Direction Normal?";
	      default: return "";
           }
	}

	public Class getColumnClass(int col) {
	   switch(col) {
	      case ROSTERCOLUMN: return(javax.swing.JComboBox.class);
	      case DELCOLUMN: return(javax.swing.JButton.class);
	      case DIRECTIONCOLUMN: return(Boolean.class);
	      default: return(String.class);
	   }
	}

        public boolean isCellEditable(int row, int col){ 
	   log.debug("isCellEditable called for row: " +row + " column: " +col);
		if(col==DELCOLUMN) return(true);
		else if(row!=0 && col == DIRECTIONCOLUMN) return(true);
		else return(false);
	}

        public Object getValueAt(int row, int col) {
	   log.debug("getValueAt called for row: " +row +" column: " +col);
 	   if(_consist == null) {
					log.debug("Consist not defined");
					return(null);
				}
	   switch(col) {
	      case ADDRCOLUMN: return(((DccLocoAddress)_consist.getConsistList().get(row)).toString());
	      /*case ROSTERCOLUMN: javax.swing.JComboBox RosterBox = Roster.instance().matchingComboBox(null,null,null,null,null,null,null);
        		      RosterBox.insertItemAt("",0);
        		      RosterBox.setSelectedItem(getValueAt(ADDRCOLUMN,row));
	  		      return RosterBox;*/
	      case DIRECTIONCOLUMN: return(new Boolean(_consist.getLocoDirection((DccLocoAddress)_consist.getConsistList().get(row))));
	      case DELCOLUMN: return "DEL";
	      default: return("");
           }
       }

       public void setValueAt(Object value, int row, int col) {
	   log.debug("setValueAt called for row: " +row +" column: " +col);
	      if(_consist == null) return;
	      switch(col) {
	      case DIRECTIONCOLUMN: _consist.add((DccLocoAddress)_consist.getConsistList().get(row),((Boolean)value).booleanValue());
              			    fireTableDataChanged();
				    break;
	      case DELCOLUMN:   log.debug("Delete Called for row " +row);
              			fireTableRowsDeleted(row,row);
				_consist.remove((DccLocoAddress)_consist.getConsistList().get(row));
              			fireTableDataChanged();
				break;
	      default: log.error("Unknown Consist Operation");
	      }
        }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ConsistDataModel.class.getName());	

}
