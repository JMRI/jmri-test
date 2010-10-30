// RouteEditTableModel.java

package jmri.jmrit.operations.routes;

import java.beans.*;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

import jmri.jmrit.operations.setup.Setup;

/**
 * Table Model for edit of route locations used by operations
 *
 * @author Daniel Boudreau Copyright (C) 2008
 * @version   $Revision: 1.3 $
 */
public class RouteEditTableModel extends javax.swing.table.AbstractTableModel implements PropertyChangeListener {

	static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.routes.JmritOperationsRoutesBundle");
    
    // Defines the columns
    private static final int IDCOLUMN   = 0;
    private static final int NAMECOLUMN   = IDCOLUMN +1;
    private static final int TRAINCOLUMN = NAMECOLUMN +1;
    private static final int MAXMOVESCOLUMN = TRAINCOLUMN +1;
    private static final int PICKUPCOLUMN = MAXMOVESCOLUMN +1;
    private static final int DROPCOLUMN = PICKUPCOLUMN +1;
    private static final int WAITCOLUMN = DROPCOLUMN +1;
    private static final int MAXLENGTHCOLUMN = WAITCOLUMN +1;
    private static final int GRADE = MAXLENGTHCOLUMN +1;
    private static final int TRAINICONX = GRADE +1;
    private static final int TRAINICONY = TRAINICONX + 1;
    private static final int COMMENTCOLUMN = TRAINICONY +1;
    private static final int UPCOLUMN = COMMENTCOLUMN +1;
    private static final int DOWNCOLUMN = UPCOLUMN +1;
    private static final int DELETECOLUMN = DOWNCOLUMN +1;
    
    private static final int HIGHESTCOLUMN = DELETECOLUMN+1;
    
    private boolean _showWait = true;
    private JTable _table;

    public RouteEditTableModel() {
        super();
    }
    
    public void setWait(boolean showWait){
    	_showWait = showWait;
    	fireTableStructureChanged();
    	initTable(_table);
    }
 
    Route _route;
    
    synchronized void updateList() {
    	if (_route == null)
    		return;
		// first, remove listeners from the individual objects
    	removePropertyChangeRouteLocations();
 		list = _route.getLocationsBySequenceList();
		// and add them back in
		for (int i = 0; i < list.size(); i++){
			log.debug("location ids: " + list.get(i));
			_route.getLocationById(list.get(i))
					.addPropertyChangeListener(this);
		}
	}

	List<String> list = new ArrayList<String>();
    
	void initTable(JTable table, Route route) {
		_table = table;
		_route = route;
		if (_route != null)
			_route.addPropertyChangeListener(this);
		initTable(table);
	}
	
	void initTable(JTable table) {
		// Install the button handlers
		TableColumnModel tcm = table.getColumnModel();
		ButtonRenderer buttonRenderer = new ButtonRenderer();
		TableCellEditor buttonEditor = new ButtonEditor(new javax.swing.JButton());
		tcm.getColumn(COMMENTCOLUMN).setCellRenderer(buttonRenderer);
		tcm.getColumn(COMMENTCOLUMN).setCellEditor(buttonEditor);
		tcm.getColumn(UPCOLUMN).setCellRenderer(buttonRenderer);
		tcm.getColumn(UPCOLUMN).setCellEditor(buttonEditor);
		tcm.getColumn(DOWNCOLUMN).setCellRenderer(buttonRenderer);
		tcm.getColumn(DOWNCOLUMN).setCellEditor(buttonEditor);
		tcm.getColumn(DELETECOLUMN).setCellRenderer(buttonRenderer);
		tcm.getColumn(DELETECOLUMN).setCellEditor(buttonEditor);
        table.setDefaultRenderer(JComboBox.class, new jmri.jmrit.symbolicprog.ValueRenderer());
        table.setDefaultEditor(JComboBox.class, new jmri.jmrit.symbolicprog.ValueEditor());

		// set column preferred widths
		table.getColumnModel().getColumn(IDCOLUMN).setPreferredWidth(45);
		table.getColumnModel().getColumn(NAMECOLUMN).setPreferredWidth(150);
		table.getColumnModel().getColumn(TRAINCOLUMN).setPreferredWidth(90);
		table.getColumnModel().getColumn(MAXMOVESCOLUMN).setPreferredWidth(45);
		table.getColumnModel().getColumn(PICKUPCOLUMN).setPreferredWidth(60);
		table.getColumnModel().getColumn(DROPCOLUMN).setPreferredWidth(50);
		table.getColumnModel().getColumn(WAITCOLUMN).setPreferredWidth(55);
		table.getColumnModel().getColumn(MAXLENGTHCOLUMN).setPreferredWidth(75);
		table.getColumnModel().getColumn(GRADE).setPreferredWidth(50);
		table.getColumnModel().getColumn(TRAINICONX).setPreferredWidth(40);
		table.getColumnModel().getColumn(TRAINICONY).setPreferredWidth(40);
		table.getColumnModel().getColumn(COMMENTCOLUMN).setPreferredWidth(70);
		table.getColumnModel().getColumn(UPCOLUMN).setPreferredWidth(70);
		table.getColumnModel().getColumn(DOWNCOLUMN).setPreferredWidth(70);
		table.getColumnModel().getColumn(DELETECOLUMN).setPreferredWidth(70);
        updateList();
		// have to shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	}
    
    public int getRowCount() { return list.size(); }

    public int getColumnCount( ){ return HIGHESTCOLUMN;}

    public String getColumnName(int col) {
        switch (col) {
        case IDCOLUMN: return rb.getString("Id");
        case NAMECOLUMN: return rb.getString("Location");
        case TRAINCOLUMN: return rb.getString("TrainDirection");
        case MAXMOVESCOLUMN: return rb.getString("MaxMoves");
        case PICKUPCOLUMN: return rb.getString("Pickups");
        case DROPCOLUMN: return rb.getString("Drops");
        case WAITCOLUMN: {
        	if (_showWait)
        		return rb.getString("Wait");
        	else
        		return rb.getString("Time");
        }
        case MAXLENGTHCOLUMN: return rb.getString("MaxLength");
        case GRADE: return rb.getString("Grade");
        case TRAINICONX: return rb.getString("X");
        case TRAINICONY: return rb.getString("Y");
        case COMMENTCOLUMN: return rb.getString("Comment");
        case UPCOLUMN: return "";
        case DOWNCOLUMN: return "";
        case DELETECOLUMN: return "";		//edit column
        default: return "unknown";
        }
    }

    public Class<?> getColumnClass(int col) {
        switch (col) {
        case IDCOLUMN: return String.class;
        case NAMECOLUMN: return String.class;
        case TRAINCOLUMN: return JComboBox.class;
        case MAXMOVESCOLUMN: return String.class;
        case PICKUPCOLUMN: return JComboBox.class;
        case DROPCOLUMN: return JComboBox.class;
        case WAITCOLUMN: {
        	if (_showWait)
        		return String.class;
        	else
        		return JComboBox.class;
        }
        case MAXLENGTHCOLUMN: return String.class;
        case GRADE: return String.class;
        case TRAINICONX: return String.class;
        case TRAINICONY: return String.class;
        case COMMENTCOLUMN: return JButton.class;
        case UPCOLUMN: return JButton.class;
        case DOWNCOLUMN: return JButton.class;
        case DELETECOLUMN: return JButton.class;
        default: return null;
        }
    }

    public boolean isCellEditable(int row, int col) {
        switch (col) {
        case DELETECOLUMN:
        case TRAINCOLUMN:
        case MAXMOVESCOLUMN:
        case PICKUPCOLUMN:
        case DROPCOLUMN:
        case WAITCOLUMN:
        case MAXLENGTHCOLUMN:
        case GRADE:
        case TRAINICONX:
        case TRAINICONY:
        case COMMENTCOLUMN:
        case UPCOLUMN:
        case DOWNCOLUMN:
        	return true;
        default: 
        	return false;
        }
    }

    public Object getValueAt(int row, int col) {
    	if (row >= list.size())
    		return "ERROR unknown "+row;
    	RouteLocation rl = _route.getLocationById(list.get(row));
    	if (rl == null)
    		return "ERROR unknown route location "+row;
        switch (col) {
        case IDCOLUMN: return rl.getId();
        case NAMECOLUMN: return rl.getName();
        case TRAINCOLUMN:{
        	JComboBox cb = Setup.getComboBox();
        	cb.setSelectedItem(rl.getTrainDirectionString());	
        	return cb;
        }
        case MAXMOVESCOLUMN: return Integer.toString(rl.getMaxCarMoves());
        case PICKUPCOLUMN:{
        	JComboBox cb = getYesNoComboBox();
        	cb.setSelectedItem(rl.canPickup()?rb.getString("yes"):rb.getString("no")); 
        	return cb;
        }
        case DROPCOLUMN:{
        	JComboBox cb = getYesNoComboBox();
        	cb.setSelectedItem(rl.canDrop()?rb.getString("yes"):rb.getString("no")); 
        	return cb;
        }
        case WAITCOLUMN: {
        	if (_showWait){
        		return Integer.toString(rl.getWait());
        	} else {
        		JComboBox cb = getTimeComboBox();
        		cb.setSelectedItem(rl.getDepartureTime());
        		return cb;
        	}
        }
        case MAXLENGTHCOLUMN: return Integer.toString(rl.getMaxTrainLength());
        case GRADE: return Double.toString(rl.getGrade());
        case TRAINICONX: return Integer.toString(rl.getTrainIconX());
        case TRAINICONY: return Integer.toString(rl.getTrainIconY());
        case COMMENTCOLUMN: {
        	if (rl.getComment().equals(""))
        		return rb.getString("Add");
        	else
        		return rb.getString("Edit");
        }
        case UPCOLUMN: return rb.getString("Up");
        case DOWNCOLUMN: return rb.getString("Down");
        case DELETECOLUMN: return rb.getString("Delete");
        default: return "unknown "+col;
        }
    }

    public void setValueAt(Object value, int row, int col) {
    	if (value == null){
    		log.debug("Warning route table row "+row+" still in edit");
    		return;
    	}
        switch (col) {
        case COMMENTCOLUMN: setComment(row);
        	break;
        case UPCOLUMN: moveUpRouteLocation(row);
        	break;
        case DOWNCOLUMN: moveDownRouteLocation(row);
        	break;
        case DELETECOLUMN:
			deleteRouteLocation(row);
			break;
		case TRAINCOLUMN:
			setTrainDirection(value, row);
			break;
		case MAXMOVESCOLUMN:
			setMaxTrainMoves(value, row);
			break;
		case PICKUPCOLUMN:
			setPickup(value, row);
			break;
		case DROPCOLUMN:
			setDrop(value, row);
			break;
		case WAITCOLUMN:{
			if (_showWait)
				setWait(value, row);
			else
				setDepartureTime(value, row);
		}
			break;
		case MAXLENGTHCOLUMN:
			setMaxTrainLength(value, row);
			break;
		case GRADE:
			setGrade(value, row);
			break;
		case TRAINICONX:
			setTrainIconX(value, row);
			break;
		case TRAINICONY:
			setTrainIconY(value, row);
			break;
		default:
			break;
		}
	}
    
    private void moveUpRouteLocation (int row){
    	log.debug("move location up");
		String id = list.get(row);
		RouteLocation rl = _route.getLocationById(id);
    	_route.moveLocationUp(rl);
    }
    
    private void moveDownRouteLocation (int row){
    	log.debug("move location down");
		String id = list.get(row);
		RouteLocation rl = _route.getLocationById(id);
    	_route.moveLocationDown(rl);
    }

    private void deleteRouteLocation (int row){
    	log.debug("Delete location");
		String id = list.get(row);
		RouteLocation rl = _route.getLocationById(id);
    	_route.deleteLocation(rl);
    }
    
   private int _trainDirection = Setup.getDirectionInt((String)Setup.getComboBox().getItemAt(0));
   
   public int getLastTrainDirection(){
	   return _trainDirection;
   }
    
    private void setTrainDirection (Object value, int row){
    	RouteLocation rl = _route.getLocationById(list.get(row));
    	_trainDirection = Setup.getDirectionInt((String)((JComboBox)value).getSelectedItem());
    	rl.setTrainDirection(_trainDirection);
    	// update train icon 
    	rl.setTrainIconCoordinates();
    }
    
    private void setMaxTrainMoves (Object value, int row){
    	RouteLocation rl = _route.getLocationById(list.get(row));
    	int moves;
    	try{
     		moves = Integer.parseInt(value.toString());
    	} catch(NumberFormatException e) {
    		log.error("Location moves must be a number");
    		return;
    	}
     	if (moves <= 100){
     		rl.setMaxCarMoves(moves);
     	}else{
     		log.error("Location moves can not exceed 100");
			JOptionPane.showMessageDialog(null,
					"Location moves can not exceed 100", "Can not change number of moves!",
					JOptionPane.ERROR_MESSAGE);
     	}
    }
    
    private void setDrop (Object value, int row){
    	RouteLocation rl = _route.getLocationById(list.get(row));
    	rl.setCanDrop(((String)((JComboBox)value).getSelectedItem()).equals(rb.getString("yes")));
    }
    
    private void setPickup (Object value, int row){
    	RouteLocation rl = _route.getLocationById(list.get(row));
    	rl.setCanPickup(((String)((JComboBox)value).getSelectedItem()).equals(rb.getString("yes")));
    }
    
    private int _maxTrainLength = Setup.getTrainLength();
    
    public int getLastMaxTrainLength(){
    	return _maxTrainLength;
    }
    
    private void setWait (Object value, int row){
    	RouteLocation rl = _route.getLocationById(list.get(row));
    	int wait;
    	try{
     		wait = Integer.parseInt(value.toString());
    	} catch(NumberFormatException e) {
    		log.error("Location wait must be a number");
			JOptionPane.showMessageDialog(null,
					"Enter wait time in minutes", "Wait time isn't valid",
					JOptionPane.ERROR_MESSAGE);
    		return;
    	}
     	rl.setWait(wait);
    }
    
    private void setDepartureTime (Object value, int row){
    	RouteLocation rl = _route.getLocationById(list.get(row));
    	rl.setDepartureTime(((String)((JComboBox)value).getSelectedItem()));
    }
    
    private void setMaxTrainLength (Object value, int row){
    	RouteLocation rl = _route.getLocationById(list.get(row));
    	int length;
    	try{
     		length = Integer.parseInt(value.toString());
    	} catch(NumberFormatException e) {
    		log.error("Location length must be a number");
    		return;
    	}
     	if (length <= Setup.getTrainLength()){
     		rl.setMaxTrainLength(length);
     		_maxTrainLength = length;
     	}else{
     		log.error("Location length can not exceed max train length");
			JOptionPane.showMessageDialog(null,
					"Location length can not exceed max train length", "Can not change max train length!",
					JOptionPane.ERROR_MESSAGE);
     	}
    }
    
    private void setGrade (Object value, int row){
    	RouteLocation rl = _route.getLocationById(list.get(row));
    	double grade;
    	try{
     		grade = Double.parseDouble(value.toString());
    	} catch(NumberFormatException e) {
    		log.error("grade must be a number");
    		return;
    	}
     	if (grade <= 6){
     		rl.setGrade(grade);
     	}else{
     		log.error("Maximum grade is 6 percent");
			JOptionPane.showMessageDialog(null,
					"Maximum grade is 6 percent", "Can not change grade!",
					JOptionPane.ERROR_MESSAGE);
     	}
    }
    
    private void setTrainIconX (Object value, int row){
    	RouteLocation rl = _route.getLocationById(list.get(row));
    	int x;
    	try{
     		x = Integer.parseInt(value.toString());
    	} catch(NumberFormatException e) {
    		log.error("Train icon x coordinate must be a number");
    		return;
    	}
    	rl.setTrainIconX(x);
    }
    
    private void setTrainIconY (Object value, int row){
    	RouteLocation rl = _route.getLocationById(list.get(row));
    	int y;
    	try{
     		y = Integer.parseInt(value.toString());
    	} catch(NumberFormatException e) {
    		log.error("Train icon y coordinate must be a number");
    		return;
    	}
    	rl.setTrainIconY(y);
    }
    
    private void setComment(int row){
    	log.debug("Set comment for row "+row);
    	RouteLocation rl = _route.getLocationById(list.get(row));
		Object comment =  JOptionPane.showInputDialog(null,
				"Comment", "Enter comment for location "+rl.getName(),
				JOptionPane.PLAIN_MESSAGE, null, null, rl.getComment());
		if(comment == null)
			return;
		rl.setComment((String)comment);
    }

    private JComboBox getYesNoComboBox(){
    	JComboBox cb = new JComboBox();
    	cb.addItem(rb.getString("yes"));
    	cb.addItem(rb.getString("no"));
    	return cb;
    }
    
    private JComboBox getTimeComboBox(){
    	JComboBox timeBox = new JComboBox();
    	String hour;
    	String minute;
    	timeBox.addItem("");
		for (int i=0; i<24; i++){
			if (i<10)
				hour = "0"+Integer.toString(i);
			else
				hour = Integer.toString(i);
			for (int j=0; j<60; j=j+5){
				if (j<10)
					minute = "0"+Integer.toString(j);
				else
					minute = Integer.toString(j);
				
				timeBox.addItem(hour+":"+minute);
			}
		}
    	return timeBox;
    }

    // this table listens for changes to a route and it's locations
    public void propertyChange(PropertyChangeEvent e) {
    	if (log.isDebugEnabled()) log.debug("Property change " +e.getPropertyName()+ " old: "+e.getOldValue()+ " new: "+e.getNewValue());
    	if (e.getPropertyName().equals(Route.LISTCHANGE_CHANGED_PROPERTY)) {
    		updateList();
    		fireTableDataChanged();
    	}

    	if (e.getSource() != _route){
    			String id = ((RouteLocation) e.getSource()).getId();
    			int row = list.indexOf(id);
    			if (log.isDebugEnabled()) log.debug("Update route table row: "+ row + " id: " + id);
    			if (row >= 0)
    				fireTableRowsUpdated(row, row);

    	}
    }
    
    private void removePropertyChangeRouteLocations() {
    	for (int i = 0; i < list.size(); i++) {
    		// if object has been deleted, it's not here; ignore it
    		RouteLocation rl = _route.getLocationById(list.get(i));
    		if (rl != null)
    			rl.removePropertyChangeListener(this);
    	}
    }

    public void dispose() {
        if (log.isDebugEnabled()) log.debug("dispose");
        removePropertyChangeRouteLocations();
        if (_route != null)
        	_route.removePropertyChangeListener(this);
        list.clear();
        fireTableDataChanged();
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RouteEditTableModel.class.getName());
}

