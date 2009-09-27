// LocationsByCarTypeFrame.java

package jmri.jmrit.operations.locations;

import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.OperationsFrame;

import java.awt.*;

import javax.swing.*;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;


/**
 * Frame to display which locations service certain car types
 * 
 * @author Dan Boudreau Copyright (C) 2009
 * @version $Revision: 1.1 $
 */

public class LocationsByCarTypeFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.locations.JmritOperationsLocationsBundle");
	
	LocationManager manager;

	ArrayList<JCheckBox> locationList = new ArrayList<JCheckBox>();
	ArrayList<JCheckBox> trackList = new ArrayList<JCheckBox>();
	JPanel locationCheckBoxes = new JPanel();
	
	// panels
	JPanel pLocations;

	// major buttons
	JButton clearButton = new JButton(rb.getString("Clear"));
	JButton setButton = new JButton(rb.getString("Select"));
	JButton saveButton = new JButton(rb.getString("Save"));
	
	// check boxes
	JCheckBox checkBox;
	
	// radio buttons
        
	// text field

	// for padding out panel
	
	// combo boxes
	JComboBox typeComboBox = CarTypes.instance().getComboBox();

	public LocationsByCarTypeFrame() {
		super();
	}

	public void initComponents(String carType) {

		// load managers
		manager = LocationManager.instance();
		
		// general GUI config
		getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
		
	    //      Set up the panels
    	JPanel pCarType = new JPanel();
    	pCarType.setLayout(new GridBagLayout());
    	pCarType.setBorder(BorderFactory.createTitledBorder(rb.getString("Type")));
    	
    	addItem(pCarType, typeComboBox, 0,0);
    	typeComboBox.setSelectedItem(carType);

    	pLocations = new JPanel();
    	pLocations.setLayout(new GridBagLayout());
    	JScrollPane locationPane = new JScrollPane(pLocations);
    	locationPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    	locationPane.setBorder(BorderFactory.createTitledBorder(rb.getString("Locations")));
    	updateLocations();
    	
    	JPanel pButtons = new JPanel();
    	pButtons.setLayout(new GridBagLayout());
    	pButtons.setBorder(BorderFactory.createEtchedBorder());
    	
    	addItem(pButtons, clearButton, 0, 0);
    	addItem(pButtons, setButton, 1, 0);
    	addItem(pButtons, saveButton, 2, 0);
    	
    	getContentPane().add(pCarType);
    	getContentPane().add(locationPane);
    	getContentPane().add(pButtons);
    	
		// setup combo box
		addComboBoxAction(typeComboBox);
		
    	// setup buttons
		addButtonAction(setButton);
		addButtonAction(clearButton);
		addButtonAction(saveButton);
		
		manager.addPropertyChangeListener(this);

		pack();
		setSize(getWidth()+30, getHeight());
		setTitle(rb.getString("TitleModifyLocations"));
		setVisible(true);
	}
		
	public void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
		log.debug("combo box action");
		updateLocations();
	}
	
	// Save, Delete, Add 
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == saveButton)
			save();
		if (ae.getSource() == setButton)
			selectCheckboxes(true);
		if (ae.getSource() == clearButton)
			selectCheckboxes(false);
	}
	
	/**
	 * Update the car types that locations and tracks service.
	 * Note that the checkbox name is the id of the location or
	 * track.
	 */
	private void save(){
		log.debug("save "+locationList.size());
		removePropertyChangeLocations();
		for (int i=0; i<locationList.size(); i++){
			JCheckBox cb = locationList.get(i);
			Location loc = manager.getLocationById(cb.getName());
			if (cb.isSelected()){
				loc.addTypeName((String)typeComboBox.getSelectedItem());
				// save tracks that have the same id as the location
				for (int j=0; j<trackList.size(); j++){
					cb = trackList.get(j);
					String[] id = cb.getName().split("s");
					if (loc.getId().equals(id[0])){
						Track track = loc.getTrackById(cb.getName());
						if (cb.isSelected()){
							track.addTypeName((String)typeComboBox.getSelectedItem());
						} else {
							track.deleteTypeName((String)typeComboBox.getSelectedItem());
						}
					}
				}
			} else {
				loc.deleteTypeName((String)typeComboBox.getSelectedItem());
			}
		}
		LocationManagerXml.instance().writeOperationsLocationFile();
		updateLocations();
	}
	
	private void updateLocations(){
		log.debug("update");
		removePropertyChangeLocations();
		locationList.clear();
		trackList.clear();
		int x=0;
		pLocations.removeAll();
		String carType = (String)typeComboBox.getSelectedItem();
		List<String> locations = manager.getLocationsByNameList();
		for (int i=0; i<locations.size(); i++){
			Location loc = manager.getLocationById(locations.get(i));
			loc.addPropertyChangeListener(this);
			JCheckBox cb = new JCheckBox(loc.getName());
			cb.setName(loc.getId());
			cb.setToolTipText(MessageFormat.format(rb.getString("TipLocCarType"),new Object[]{carType}));
			addCheckBoxAction(cb);
			locationList.add(cb);
			boolean locAcceptsType = loc.acceptsTypeName(carType);
			cb.setSelected(locAcceptsType);
			addItemLeft(pLocations, cb, 0, x++);
			List<String> tracks = loc.getTracksByNameList(null);
			for (int j=0; j<tracks.size(); j++){
				Track track = loc.getTrackById(tracks.get(j));
				cb = new JCheckBox(track.getName());
				cb.setName(track.getId());
				cb.setToolTipText(MessageFormat.format(rb.getString("TipTrackCarType"),new Object[]{carType}));
				addCheckBoxAction(cb);
				trackList.add(cb);
				cb.setSelected(locAcceptsType && track.acceptsTypeName(carType));
				addItemLeft(pLocations, cb, 1, x++);
			}
		}
		pLocations.revalidate();
		repaint();
	}
	
	private void selectCheckboxes(boolean b){
		for (int i=0; i<locationList.size(); i++){
			JCheckBox cb = locationList.get(i);
			cb.setSelected(b);
		}
		for (int i=0; i<trackList.size(); i++){
			JCheckBox cb = trackList.get(i);
			cb.setSelected(b);
		}
	}
	
	public void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
		JCheckBox cb =  (JCheckBox)ae.getSource();
		log.debug("Checkbox "+cb.getName()+" text: "+cb.getText());
		if (locationList.contains(cb)){
			log.debug("Checkbox location "+cb.getText());
			// must deselect tracks if location is deselect
			if (!cb.isSelected()){
				String locId = cb.getName();
				for (int i=0; i<trackList.size(); i++){
					cb = trackList.get(i);
					String[] id = cb.getName().split("s");
					if (locId.equals(id[0])){
						cb.setSelected(false);
					}				
				}
			}
			
		}else if (trackList.contains(cb)){
			log.debug("Checkbox track "+cb.getText());
			// Must select location if track is selected
			if (cb.isSelected()){
				String[] loc = cb.getName().split("s");
				for (int i=0; i<locationList.size(); i++){
					cb = locationList.get(i);
					if (cb.getName().equals(loc[0])){
						cb.setSelected(true);
						break;
					}				
				}
			}
		}else{
			log.error("Error checkbox not found");
		}
	}

	private void removePropertyChangeLocations() {
		if (locationList != null) {
			for (int i = 0; i < locationList.size(); i++) {
				// if object has been deleted, it's not here; ignore it
				Location l = manager.getLocationById(locationList.get(i).getName());
				if (l != null)
					l.removePropertyChangeListener(this);
			}
		}
	}

	public void dispose(){
		manager.removePropertyChangeListener(this);
		removePropertyChangeLocations();
		super.dispose();
	}
	
 	public void propertyChange(java.beans.PropertyChangeEvent e) {
		if (log.isDebugEnabled()) 
			log.debug("Property change " +e.getPropertyName()+ " old: "+e.getOldValue()+ " new: "+e.getNewValue());
		if (e.getPropertyName().equals(LocationManager.LISTLENGTH_CHANGED_PROPERTY) ||
				e.getPropertyName().equals(Location.TYPES_CHANGED_PROPERTY) ||
				e.getPropertyName().equals(Location.NAME_CHANGED_PROPERTY) ||
				e.getPropertyName().equals(Location.INTERCHANGELISTLENGTH_CHANGED_PROPERTY) ||
				e.getPropertyName().equals(Location.SIDINGLISTLENGTH_CHANGED_PROPERTY) ||
				e.getPropertyName().equals(Location.STAGINGLISTLENGTH_CHANGED_PROPERTY) ||
				e.getPropertyName().equals(Location.YARDLISTLENGTH_CHANGED_PROPERTY))
				 {
			updateLocations();
		}
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(LocationsByCarTypeFrame.class.getName());
}
