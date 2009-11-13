// CarsTableModel.java

package jmri.jmrit.operations.rollingstock.cars;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.ResourceBundle;
import java.awt.Frame;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;

import jmri.jmrit.operations.setup.Control;


/**
 * Table Model for edit of cars used by operations
 *
 * @author Daniel Boudreau Copyright (C) 2008
 * @version   $Revision: 1.22 $
 */
public class CarsTableModel extends javax.swing.table.AbstractTableModel implements PropertyChangeListener {

	static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.rollingstock.cars.JmritOperationsCarsBundle");
   
    CarManager manager = CarManager.instance();		// There is only one manager
 
    // Defines the columns
    private static final int NUMCOLUMN   = 0;
    private static final int ROADCOLUMN   = 1;
    private static final int TYPECOLUMN = 2;
    private static final int LENGTHCOLUMN = 3;
    private static final int COLORCOLUMN = 4;	// also the Load column
    //private static final int LOADCOLUMN = 4;
    private static final int KERNELCOLUMN  = 5;
    private static final int LOCATIONCOLUMN  = 6;
    private static final int DESTINATIONCOLUMN = 7;
    private static final int TRAINCOLUMN = 8;
    private static final int MOVESCOLUMN = 9;	// also the Owner and RFID column
    private static final int SETCOLUMN = 10;
    private static final int EDITCOLUMN = 11;
    
    private static final int HIGHESTCOLUMN = EDITCOLUMN+1;
    
    private boolean showColor = true;
    private static final int SHOWMOVES = 0;
    private static final int SHOWBUILT = 1;
    private static final int SHOWOWNER = 2;
    private static final int SHOWRFID = 3;
    private int showMoveCol = SHOWMOVES;
    
    public final int SORTBYNUMBER = 1;
    public final int SORTBYROAD = 2;
    public final int SORTBYTYPE = 3;
    public final int SORTBYLOCATION = 4;
    public final int SORTBYDESTINATION = 5;
    public final int SORTBYTRAIN = 6;
    public final int SORTBYMOVES = 7;
    public final int SORTBYKERNEL = 8;
    public final int SORTBYLOAD = 9;
    public final int SORTBYCOLOR = 10;
    public final int SORTBYBUILT = 11;
    public final int SORTBYOWNER = 12;
    public final int SORTBYRFID = 13;
    
    private int _sort = SORTBYNUMBER;
    
	List<String> sysList = null;	// list of car ids
	boolean showAllCars = true;		// when true show all cars
	String locationName = null;		// only show cars with this location
	String trackName = null;		// only show cars with this track
	JTable _table;
    
    public CarsTableModel(boolean showAllCars, String locationName, String trackName) {
        super();
        this.showAllCars = showAllCars;
        this.locationName = locationName;
        this.trackName = trackName;
        manager.addPropertyChangeListener(this);
        updateList();
    }
 
    public void setSort (int sort){
    	_sort = sort;
    	updateList();
    	if (sort == SORTBYCOLOR && !showColor){
    		showColor = true;
    		fireTableStructureChanged();
    		initTable(_table);
    	}
    	else if (sort == SORTBYLOAD && showColor){
    		showColor = false;
    		fireTableStructureChanged();
    		initTable(_table);
    	}
       	else if (sort == SORTBYMOVES){
    		showMoveCol = SHOWMOVES;
       		fireTableStructureChanged();
    		initTable(_table);
    	}
       	else if (sort == SORTBYBUILT){
    		showMoveCol = SHOWBUILT;
       		fireTableStructureChanged();
    		initTable(_table);
    	}
    	else if (sort == SORTBYOWNER){
    		showMoveCol = SHOWOWNER;
       		fireTableStructureChanged();
    		initTable(_table);
    	}
    	else if (sort == SORTBYRFID){
    		showMoveCol = SHOWRFID;
       		fireTableStructureChanged();
    		initTable(_table);
    	}
    	else
    		fireTableDataChanged();
    }
    
    String _roadNumber = "";
    int _index = 0;
    
    /**
     * Search for car by road number
     * @param roadNumber
     * @return -1 if not found, table row number if found
     */
    public int findCarByRoadNumber (String roadNumber){
		if (sysList != null) {
			if (!roadNumber.equals(_roadNumber))
				return getIndex(0, roadNumber);
			int index = getIndex(_index, roadNumber);
			if (index > 0)
				return index;
			return getIndex(0, roadNumber);
		}
		return -1;
    }
    
    private int getIndex (int start, String roadNumber){
		for (int index = start; index < sysList.size(); index++) {
			Car c = manager.getCarById(sysList.get(index));
			if (c != null){
				String[] number = c.getNumber().split("-");
				if (c.getNumber().equals(roadNumber) || number[0].equals(roadNumber)){
					_roadNumber = roadNumber;
					_index = index + 1;
					return index;
				}
			}
		}
		_roadNumber ="";
		return -1;
    }
    
    synchronized void updateList() {
		// first, remove listeners from the individual objects
    	removePropertyChangeCars();
     	sysList = getSelectedCarList();
 		// and add listeners back in
     	addPropertyChangeCars();
	}
    
    public List<String> getSelectedCarList(){
    	List<String> list;
		if (_sort == SORTBYROAD)
			list = manager.getCarsByRoadNameList();
		else if (_sort == SORTBYTYPE)
			list = manager.getCarsByTypeList();
		else if (_sort == SORTBYLOCATION)
			list = manager.getCarsByLocationList();
		else if (_sort == SORTBYDESTINATION)
			list = manager.getCarsByDestinationList();
		else if (_sort == SORTBYTRAIN)
			list = manager.getCarsByTrainList();
		else if (_sort == SORTBYMOVES)
			list = manager.getCarsByMovesList();
		else if (_sort == SORTBYKERNEL)
			list = manager.getCarsByKernelList();
		else if (_sort == SORTBYLOAD)
			list = manager.getCarsByLoadList();
		else if (_sort == SORTBYCOLOR)
			list = manager.getCarsByColorList();
		else if (_sort == SORTBYOWNER)
			list = manager.getCarsByOwnerList();
		else if (_sort == SORTBYBUILT)
			list = manager.getCarsByBuiltList();
		else if (_sort == SORTBYRFID)
			list = manager.getCarsByRfidList();
		else
			list = manager.getCarsByNumberList();
    	filterList(list);
		return list;
    }
    
    private void filterList(List<String> list){
    	if (showAllCars)
    		return;
    	for (int i=0; i<list.size(); i++){
    		Car car = manager.getCarById(list.get(i));
    		if (car.getLocationName().equals("")){
    			list.remove(i--);
    			continue;
    		}
    		// filter out cars that don't have a location name that matches
    		if (locationName != null) {
    			if (car.getLocationName().equals(locationName)){
    				if (trackName != null){
    					if (car.getTrackName().equals(trackName)){
    						continue;
    					}else{
    						list.remove(i--);
    					}
    				}else{
    					continue;
    				}
    			}else{
    				list.remove(i--);
    			}
    		}
    	}
    }
    
	void initTable(JTable table) {
		_table = table;
		// Install the button handlers
		TableColumnModel tcm = table.getColumnModel();
		ButtonRenderer buttonRenderer = new ButtonRenderer();
		tcm.getColumn(SETCOLUMN).setCellRenderer(buttonRenderer);
		TableCellEditor buttonEditor = new ButtonEditor(new javax.swing.JButton());
		tcm.getColumn(SETCOLUMN).setCellEditor(buttonEditor);
		tcm.getColumn(EDITCOLUMN).setCellRenderer(buttonRenderer);
		tcm.getColumn(EDITCOLUMN).setCellEditor(buttonEditor);
		// set column preferred widths
		table.getColumnModel().getColumn(NUMCOLUMN).setPreferredWidth(60);
		table.getColumnModel().getColumn(ROADCOLUMN).setPreferredWidth(60);
		table.getColumnModel().getColumn(COLORCOLUMN).setPreferredWidth(65);
		table.getColumnModel().getColumn(TYPECOLUMN).setPreferredWidth(65);
		table.getColumnModel().getColumn(KERNELCOLUMN).setPreferredWidth(75);
		table.getColumnModel().getColumn(LENGTHCOLUMN).setPreferredWidth(35);
		table.getColumnModel().getColumn(LOCATIONCOLUMN).setPreferredWidth(190);
		table.getColumnModel().getColumn(DESTINATIONCOLUMN).setPreferredWidth(190);
		table.getColumnModel().getColumn(TRAINCOLUMN).setPreferredWidth(65);
		table.getColumnModel().getColumn(MOVESCOLUMN).setPreferredWidth(50);
		table.getColumnModel().getColumn(SETCOLUMN).setPreferredWidth(70);
		table.getColumnModel().getColumn(EDITCOLUMN).setPreferredWidth(70);
		// have to shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	}
    
    public int getRowCount() { return sysList.size(); }

    public int getColumnCount( ){ return HIGHESTCOLUMN;}

    public String getColumnName(int col) {
        switch (col) {
        case NUMCOLUMN: return rb.getString("Number");
        case ROADCOLUMN: return rb.getString("Road");
        case COLORCOLUMN: {
        	if (showColor)
        		return rb.getString("Color");
        	else
        		return rb.getString("Load");
        }
        case TYPECOLUMN: return rb.getString("Type");
        case LENGTHCOLUMN: return rb.getString("Len");
        case KERNELCOLUMN: return rb.getString("Kernel");
        case LOCATIONCOLUMN: return rb.getString("Location");
        case DESTINATIONCOLUMN: return rb.getString("Destination");
        case TRAINCOLUMN: return rb.getString("Train");
        case MOVESCOLUMN: {
        	if (showMoveCol == SHOWBUILT)
        		return rb.getString("Built");
        	else if (showMoveCol == SHOWOWNER)
        		return rb.getString("Owner");
           	else if (showMoveCol == SHOWRFID)
        		return rb.getString("Rfid");
        	else
        		return rb.getString("Moves");
        }
        case SETCOLUMN: return rb.getString("Location");
        case EDITCOLUMN: return "";		//edit column
        default: return "unknown";
        }
    }

    public Class<?> getColumnClass(int col) {
        switch (col) {
        case NUMCOLUMN: return String.class;
        case ROADCOLUMN: return String.class;
        case COLORCOLUMN: return String.class;
        case LENGTHCOLUMN: return String.class;
        case TYPECOLUMN: return String.class;
        case KERNELCOLUMN: return String.class;
        case LOCATIONCOLUMN: return String.class;
        case DESTINATIONCOLUMN: return String.class;
        case TRAINCOLUMN: return String.class;
        case MOVESCOLUMN: return String.class;
        case SETCOLUMN: return JButton.class;
        case EDITCOLUMN: return JButton.class;
        default: return null;
        }
    }

    public boolean isCellEditable(int row, int col) {
        switch (col) {
        case SETCOLUMN: 
        case EDITCOLUMN: 
        	return true;
        default: 
        	return false;
        }
    }

    public Object getValueAt(int row, int col) {
    	// Funky code to put the csf and cef frames in focus after set and edit table buttons are used.
    	// The button editor for the table does a repaint of the button cells after the setValueAt code
    	// is called which then returns the focus back onto the table.  We need the set and edit frames
    	// in focus.
    	if (focusCsf){
    		focusCsf = false;
    		csf.requestFocus();
    	}
    	if (focusCef){
    		focusCef = false;
    		cef.requestFocus();
    	}
     	if (row >= sysList.size())
    		return "ERROR row "+row;
    	String carId = sysList.get(row);
    	Car c = manager.getCarById(carId);
      	if (c == null)
    		return "ERROR car unknown "+row;
        switch (col) {
        case NUMCOLUMN: return c.getNumber();
        case ROADCOLUMN: return c.getRoad();
        case COLORCOLUMN: {
          	if (showColor)
        		return c.getColor();
        	else
        	   	return c.getLoad();
        }
        case LENGTHCOLUMN: return c.getLength();
        case TYPECOLUMN: {
        	if (c.isCaboose())
        		return c.getType()+" "+rb.getString("(C)");
        	else if (c.hasFred())
        		return c.getType()+" "+rb.getString("(F)");
        	else if (c.isHazardous())
        		return c.getType()+" "+rb.getString("(H)");
        	else
        		return c.getType();
        }
        case KERNELCOLUMN: {
        	if (c.getKernel() != null && c.getKernel().isLeadCar(c))
        		return c.getKernelName()+"*";
        	return c.getKernelName();
        }
        case LOCATIONCOLUMN: {
        	String s ="";
        	if (!c.getLocationName().equals(""))
        		s = c.getLocationName() + " (" + c.getTrackName() + ")";
        	return s;
        }
        case DESTINATIONCOLUMN: {
        	String s ="";
        	if (!c.getDestinationName().equals(""))
        		s = c.getDestinationName() + " (" + c.getDestinationTrackName() + ")";
        	return s;
        }
        case TRAINCOLUMN: {
        	// if train was manually set by user add an asterisk
        	if (c.getTrain() != null && c.getRouteLocation() == null)
        		return c.getTrain().getName()+"*";
        	return c.getTrain();
        }
        case MOVESCOLUMN: {
           	if (showMoveCol == SHOWBUILT)
        		return c.getBuilt();
        	else if (showMoveCol == SHOWOWNER)
        		return c.getOwner();
           	else if (showMoveCol == SHOWRFID)
        		return c.getRfid();
        	else
        		return Integer.toString(c.getMoves());
        }
        case SETCOLUMN: return rb.getString("Set");
        case EDITCOLUMN: return rb.getString("Edit");
 
        default: return "unknown "+col;
        }
    }
    
    boolean focusCef = false;
    boolean focusCsf = false;
    CarEditFrame cef = null;
    CarSetFrame csf = null;
    
    public void setValueAt(Object value, int row, int col) {
		String carId = sysList.get(row);
    	Car car = manager.getCarById(carId);
        switch (col) {
        case SETCOLUMN:
        	log.debug("Set car location");
           	if (csf != null)
           		csf.dispose();
       		csf = new CarSetFrame();
    		csf.initComponents();
	    	csf.loadCar(car);
	    	csf.setTitle(rb.getString("TitleCarSet"));
	    	csf.setVisible(true);
	    	csf.setExtendedState(Frame.NORMAL);
	    	focusCsf = true;
        	break;
        case EDITCOLUMN:
        	log.debug("Edit car");
        	if (cef != null)
        		cef.dispose();
    		cef = new CarEditFrame();
    		cef.initComponents();
	    	cef.loadCar(car);
	    	cef.setTitle(rb.getString("TitleCarEdit"));
	    	cef.setExtendedState(Frame.NORMAL);
	    	focusCef = true;
        	break;
        default:
            break;
        }
    }

    public void dispose() {
    	if (log.isDebugEnabled()) log.debug("dispose CarTableModel");
    	manager.removePropertyChangeListener(this);
    	removePropertyChangeCars();
    	if (csf != null)
    		csf.dispose();
    	if (cef != null)
    		cef.dispose();
    }

    private void addPropertyChangeCars() {
    	List<String> list = manager.getCarsByIdList();
    	for (int i = 0; i < list.size(); i++) {
    		// if object has been deleted, it's not here; ignore it
    		Car car = manager.getCarById(list.get(i));
    		if (car != null)
    			car.addPropertyChangeListener(this);
    	}
    }
    
    private void removePropertyChangeCars() {
    	List<String> list = manager.getCarsByIdList();
    	for (int i = 0; i < list.size(); i++) {
    		// if object has been deleted, it's not here; ignore it
    		Car car = manager.getCarById(list.get(i));
    		if (car != null)
    			car.removePropertyChangeListener(this);
    	}
    }
    
    public void propertyChange(PropertyChangeEvent e) {
    	if(Control.showProperty && log.isDebugEnabled()) log.debug("Property change " +e.getPropertyName()+ " old: "+e.getOldValue()+ " new: "+e.getNewValue());
    	if (e.getPropertyName().equals(CarManager.LISTLENGTH_CHANGED_PROPERTY) || e.getPropertyName().equals(CarManager.KERNELLISTLENGTH_CHANGED_PROPERTY)) {
    		updateList();
    		fireTableDataChanged();
    	}
    	else {
    		// must be a car change
    		String carId = ((Car) e.getSource()).getId();
    		int row = sysList.indexOf(carId);
    		if(Control.showProperty && log.isDebugEnabled()) log.debug("Update car table row: "+row);
    		if (row >= 0){
    			fireTableRowsUpdated(row, row);
    		// add car to table?
    		}else if (e.getPropertyName().equals(Car.LOCATION_CHANGED_PROPERTY) || e.getPropertyName().equals(Car.TRACK_CHANGED_PROPERTY)) {
    			Car car = manager.getCarById(carId);
    			if (car == null){
    				log.error("Null car in table model");
    				return;
    			}
    			if ((locationName == null || car.getLocationName().equals(locationName)) 
    					&& (trackName == null || car.getTrackName().equals(trackName))){
    				updateList();
    				fireTableDataChanged();
    			}
    		}
    	}
    }


    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CarsTableModel.class.getName());
}

