// InterchangeTableModel.java

package jmri.jmrit.operations.locations;

import java.beans.*;
import javax.swing.*;
import jmri.jmrit.operations.setup.Control;

/**
 * Table Model for edit of interchanges used by operations
 *
 * @author Daniel Boudreau Copyright (C) 2008
 * @version   $Revision: 1.11 $
 */
public class InterchangeTableModel extends TrackTableModel {

	public InterchangeTableModel() {
		super();
	}

	public void initTable(JTable table, Location location) {
		super.initTable(table, location, Track.INTERCHANGE);
	}

	public String getColumnName(int col) {
		switch (col) {
		case NAMECOLUMN: return rb.getString("InterchangeName");
		}
		return super.getColumnName(col);
	}

	protected void editTrack (int row){
		log.debug("Edit interchange");
		if (tef != null){
			tef.dispose();
		}
		tef = new InterchangeEditFrame();
		String interchangeId = tracksList.get(row);
		Track interchange = _location.getTrackById(interchangeId);
		tef.initComponents(_location, interchange);
		tef.setTitle(rb.getString("EditInterchange"));
		focusEditFrame = true;
	}

    // this table listens for changes to a location and it's interchanges
    public void propertyChange(PropertyChangeEvent e) {
    	if (Control.showProperty && log.isDebugEnabled()) 
    		log.debug("Property change " +e.getPropertyName()+ " old: "+e.getOldValue()+ " new: "+e.getNewValue());
    	if (e.getPropertyName().equals(Location.TRACK_LISTLENGTH_CHANGED_PROPERTY)) {
    		updateList();
    		fireTableDataChanged();
    	}

    	if (e.getSource().getClass().equals(Track.class)){
    		String type = ((Track) e.getSource()).getLocType();
    		if (type.equals(Track.INTERCHANGE)){
    			String interchangeId = ((Track) e.getSource()).getId();
    			int row = tracksList.indexOf(interchangeId);
    			if (Control.showProperty && log.isDebugEnabled()) 
    				log.debug("Update interchange table row: "+ row + " id: " + interchangeId);
    			if (row >= 0)
    				fireTableRowsUpdated(row, row);
    		}
    	}
    }
 
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(InterchangeTableModel.class.getName());
}

