// SchedulesByLoadAction.java

package jmri.jmrit.operations.locations;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.rollingstock.cars.CarLoads;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.trains.TrainScheduleManager;

/**
 * Action to print a summary of locations and tracks that service specific
 * car types.
 * @author Daniel Boudreau Copyright (C) 2012
 * @version     $Revision: 17977 $
 */

public class SchedulesByLoadAction extends AbstractAction {

	private SchedulesByLoadFrame _slf;
	
	public SchedulesByLoadAction(String actionName){
		super(actionName);
	}
	
	public void actionPerformed(ActionEvent e) {
		if (_slf != null)
			_slf.dispose();
		_slf = new SchedulesByLoadFrame();			
	}

}

/**
 * Frame to display spurs with schedules and their loads
 * 
 * @author Dan Boudreau Copyright (C) 2012
 * @version $Revision: 17977 $
 */

class SchedulesByLoadFrame extends OperationsFrame implements java.beans.PropertyChangeListener {
	
	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.locations.JmritOperationsLocationsBundle");
	
	// combo box
	JComboBox typesComboBox = CarTypes.instance().getComboBox();
	JComboBox loadsComboBox = new JComboBox();
	
	// panels
	JPanel locationsPanel;
	
	// managers'
	LocationManager locationManager = LocationManager.instance();
	
	public SchedulesByLoadFrame(){
		super();
		
		// the following code sets the frame's initial state
	    getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
	    
		// load the panel
    	JPanel p1 = new JPanel();
    	p1.setLayout(new BoxLayout(p1,BoxLayout.X_AXIS));
    	//p1.setMinimumSize(new Dimension(100, 100));
    	
    	JPanel type = new JPanel();
    	type.setBorder(BorderFactory.createTitledBorder(rb.getString("Type")));
    	type.add(typesComboBox);
    	
    	JPanel load = new JPanel();
    	load.setBorder(BorderFactory.createTitledBorder(rb.getString("Load")));
    	load.add(loadsComboBox);
    	
    	p1.add(type);
    	p1.add(load);
    	
    	locationsPanel = new JPanel();
    	locationsPanel.setLayout(new GridBagLayout());
    	JScrollPane locationsPane = new JScrollPane(locationsPanel);
    	locationsPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    	locationsPane.setBorder(BorderFactory.createTitledBorder(rb.getString("Locations")));
    	
    	getContentPane().add(p1);
    	getContentPane().add(locationsPane);
    	
    	addComboBoxAction(typesComboBox);
    	addComboBoxAction(loadsComboBox);
    	
    	//property changes
    	locationManager.addPropertyChangeListener(this);
    	CarTypes.instance().addPropertyChangeListener(this);
    	CarLoads.instance().addPropertyChangeListener(this);
    	
    	setTitle(rb.getString("MenuItemShowSchedulesByLoad"));
    	pack();
		if (getWidth()<750)
			setSize(750, getHeight());
    	setVisible(true);	    	
	}
	
	public void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == typesComboBox)
			updateLoadComboBox();
		if (ae.getSource() == loadsComboBox)
			updateLocations();
		
	}
	
	private void updateLoadComboBox() {
		if (typesComboBox.getSelectedItem() != null){
			String type = (String)typesComboBox.getSelectedItem();
			CarLoads.instance().updateComboBox(type, loadsComboBox);
		}		
	}
	
	private void updateLocations(){
		String type = (String)typesComboBox.getSelectedItem();
		String load = (String)loadsComboBox.getSelectedItem();
		log.debug("Update locations for type "+type+" load "+load);
		locationsPanel.removeAll();
		int x=0;
		addItemLeft(locationsPanel, new JLabel(rb.getString("trackSchedule")), 1, x);
		addItemLeft(locationsPanel, new JLabel(rb.getString("receiveTypeLoad")), 2, x);
		addItemLeft(locationsPanel, new JLabel(rb.getString("Ship")), 3, x);
		addItemLeft(locationsPanel, new JLabel(rb.getString("destinationTrack")), 4, x++);
		List<String> locations = locationManager.getLocationsByIdList();
		for (int i=0; i<locations.size(); i++){
			Location l = locationManager.getLocationById(locations.get(i));
			// don't show staging
			if (l.getLocationOps() == Location.NORMAL){
				addItemLeft(locationsPanel, new JLabel(l.getName()), 0, x++);
				// now look for a spur with a schedule
				List<String> spurs = l.getTracksByNameList(Track.SIDING);
				for (int j=0; j<spurs.size(); j++){
					Track spur = l.getTrackById(spurs.get(j));
					Schedule sch = spur.getSchedule();
					if (sch != null){
						// listen for changes
						sch.removePropertyChangeListener(this);
						sch.addPropertyChangeListener(this);
						List<String> items = sch.getItemsBySequenceList();
						// determine if schedule is requesting car type and load
						for (int k=0; k<items.size(); k++){
							ScheduleItem item = sch.getItemById(items.get(k));
							if (item.getType().equals(type) && item.getLoad().equals(load) 
									|| item.getType().equals(type) && item.getShip().equals(load)){
								addItemLeft(locationsPanel, new JLabel(spur.getName()+" ("+spur.getScheduleName()+")"), 1, x);
								// create string (type, timetable, road, load)
								String s = item.getType();
								if (!item.getTrainScheduleId().equals(""))
									s = s + ", "+ TrainScheduleManager.instance().getScheduleById(item.getTrainScheduleId()).getName();
								if (!item.getRoad().equals(""))
									s = s + ", "+ item.getRoad();
								s = s + ", "+item.getLoad();
								addItemLeft(locationsPanel, new JLabel(rb.getString("Receive")+" ("+s+")"), 2, x);
								addItemLeft(locationsPanel, new JLabel(rb.getString("Ship")+" ("+item.getShip()+")"), 3, x++);
								if (item.getDestination() != null)
									addItemLeft(locationsPanel, new JLabel(item.getDestinationName()+" ("+item.getDestinationTrackName()+")"), 4, x-1);
								//break;	// done, only report first occurrence
							}
						}
					}
				}
			}
		}
		locationsPanel.revalidate();
		validate();
		repaint();
	}
	
	public void dispose(){
		locationManager.removePropertyChangeListener(this);
		CarTypes.instance().removePropertyChangeListener(this);
		CarLoads.instance().removePropertyChangeListener(this);
		super.dispose();
	}
	
	public void propertyChange(java.beans.PropertyChangeEvent e) {
		if (log.isDebugEnabled()) 
			log.debug("Property change " +e.getPropertyName()+ " old: "+e.getOldValue()+ " new: "+e.getNewValue());
		if (e.getPropertyName().equals(CarTypes.CARTYPES_LENGTH_CHANGED_PROPERTY))
			CarTypes.instance().updateComboBox(typesComboBox);
		if (e.getSource().getClass().equals(CarLoads.class))
			CarLoads.instance().updateComboBox((String)typesComboBox.getSelectedItem(), loadsComboBox);
		if (e.getSource().getClass().equals(Schedule.class) || e.getSource().getClass().equals(LocationManager.class))
			updateLocations();
 	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LocationsByCarTypeFrame.class.getName());
	
}
