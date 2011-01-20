// TrainByCarTypeFrame.java

package jmri.jmrit.operations.trains;

import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.OperationsFrame;

import java.awt.*;

import javax.swing.*;

import java.util.List;
import java.util.ResourceBundle;


/**
 * Frame to display by rolling stock, the locations serviced by this train
 * 
 * @author Dan Boudreau Copyright (C) 2010
 * @version $Revision: 1.5 $
 */

public class TrainByCarTypeFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.trains.JmritOperationsTrainsBundle");
	
	// train
	Train train = null;
	
	LocationManager locationManager = LocationManager.instance();
	
	// panels
	JPanel pLocations = new JPanel();
	
	// radio buttons

	// for padding out panel
	
	// combo boxes
	JComboBox typeComboBox = CarTypes.instance().getComboBox();
	JComboBox carsComboBox = new JComboBox();
	
	// Blank space
	String blank = "";
	
	// The car currently selected
	Car car;

	public TrainByCarTypeFrame() {
		super();
	}

	public void initComponents(Train train) {
		
		this.train = train;
		
		// general GUI config
		getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
		
	    //      Set up the panels
		JPanel pCarType = new JPanel();
    	pCarType.setLayout(new GridBagLayout());
    	pCarType.setBorder(BorderFactory.createTitledBorder(rb.getString("Type")));
    	
    	addItem(pCarType, typeComboBox, 0,0);
    	addItem(pCarType, carsComboBox, 1,0);

    	pLocations.setLayout(new GridBagLayout());
    	JScrollPane locationPane = new JScrollPane(pLocations);
    	locationPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    	locationPane.setBorder(BorderFactory.createTitledBorder(rb.getString("Route")));
    	updateCarsComboBox();
    	updateRoute();
    	
    	getContentPane().add(pCarType);
    	getContentPane().add(locationPane);
    	
		// setup combo box
		addComboBoxAction(typeComboBox);
		addComboBoxAction(carsComboBox);
		
		locationManager.addPropertyChangeListener(this);
		CarTypes.instance().addPropertyChangeListener(this);
		if (train != null){
			train.addPropertyChangeListener(this);
			setTitle(rb.getString("MenuItemShowCarTypes")+" "+train.getName());
		}
		// listen to all tracks and locations
		addLocationAndTrackPropertyChange();
		
		setPreferredSize(null);
		pack();
		if (getWidth()<300)
			setSize(getWidth()+70, getHeight());	
		setVisible(true);
		
	}
		
	public void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
		log.debug("combo box action");
		if (ae.getSource().equals(typeComboBox))
			updateCarsComboBox();
		updateRoute();		
	}

	private void updateRoute(){
		if (train == null)
			return;
		log.debug("update locations served by train "+train.getName());
		int x=0;
		pLocations.removeAll();
		String CarType = (String)typeComboBox.getSelectedItem();
		if (car != null)
			car.removePropertyChangeListener(this);
		car = null;
		if (carsComboBox.getSelectedItem() != null && !carsComboBox.getSelectedItem().equals(blank)){
			car = (Car)carsComboBox.getSelectedItem();
			car.addPropertyChangeListener(this);
		}
		Route route = train.getRoute();
		if (route == null)
			return;
		List<String> routeIds = route.getLocationsBySequenceList();
		for (int i=0; i<routeIds.size(); i++){
			JLabel loc = new JLabel();
			RouteLocation rl = route.getLocationById(routeIds.get(i));
			String locationName = rl.getName();
			loc.setText(locationName);
			addItemLeft(pLocations, loc, 0, x++);
			Location location = locationManager.getLocationByName(locationName);
			List<String> tracks = location.getTracksByNameList(null);
			for (int j=0; j<tracks.size(); j++){
				Track track = location.getTrackById(tracks.get(j));
				JLabel trk = new JLabel();
				trk.setText(track.getName());
				addItemLeft(pLocations, trk, 1, x);
				// is the car at this location and track?
				if (car !=null && location.equals(car.getLocation()) && track.equals(car.getTrack())){
					JLabel here = new JLabel("  -->");
					addItemLeft(pLocations, here, 0, x);
				}			
				JLabel op = new JLabel();			
				addItemLeft(pLocations, op, 2, x++);
				if (!train.acceptsTypeName(CarType))
					op.setText(rb.getString("X(TrainType)"));
				else if (car != null && !train.acceptsRoadName(car.getRoad()))
					op.setText(rb.getString("X(TrainRoad)"));
				else if (car != null && !car.isCaboose() && !train.acceptsLoadName(car.getLoad()))
					op.setText(rb.getString("X(TrainLoad)"));
				else if (car != null && !train.acceptsBuiltDate(car.getBuilt()))
					op.setText(rb.getString("X(TrainBuilt)"));
				else if (car != null && !train.acceptsOwnerName(car.getOwner()))
					op.setText(rb.getString("X(TrainOwner)"));
				else if (car != null && !train.acceptsLoadName(car.getLoad()))
					op.setText(rb.getString("X(TrainLoad)"));
				else if (train.skipsLocation(rl.getId()))
					op.setText(rb.getString("X(TrainSkips)"));
				else if (!rl.canDrop() && !rl.canPickup())
					op.setText(rb.getString("X(Route)"));
				else if (rl.getMaxCarMoves() <= 0)
					op.setText(rb.getString("X(RouteMoves)"));
				else if (!location.acceptsTypeName(CarType))
					op.setText(rb.getString("X(LocationType)"));
				else if (!track.acceptsTypeName(CarType))
					op.setText(rb.getString("X(TrackType)"));
				else if (car != null && !track.acceptsRoadName(car.getRoad()))
					op.setText(rb.getString("X(TrackRoad)"));
				else if (car != null && !track.acceptsLoadName(car.getLoad()))
					op.setText(rb.getString("X(TrackLoad)"));
				else if ((rl.getTrainDirection() & location.getTrainDirections()) == 0)
					op.setText(rb.getString("X(DirLoc)"));
				else if ((rl.getTrainDirection() & track.getTrainDirections()) == 0)
					op.setText(rb.getString("X(DirTrk)"));
				else if (!track.acceptsPickupTrain(train)){
					// can the train drop off car?
					if (rl.canDrop() && track.acceptsDropTrain(train))
						op.setText(rb.getString("DropOnly"));
					else
						op.setText(rb.getString("X(TrainPickup)"));
				}
				else if (!track.acceptsDropTrain(train))
					// can the train pick up car?
					if (rl.canPickup() && track.acceptsPickupTrain(train))
						op.setText(rb.getString("PickupOnly"));
					else
						op.setText(rb.getString("X(TrainDrop)"));
				else if (rl.canDrop() && rl.canPickup())
					op.setText(rb.getString("OK"));
				else if (rl.canDrop())
					op.setText(rb.getString("DropOnly"));
				else if (rl.canPickup())
					op.setText(rb.getString("PickupOnly"));
				else
					op.setText("X");	//default shouldn't occur
			}
		}
		pLocations.revalidate();
		repaint();
	}
	
	private void updateComboBox(){
		log.debug("update combobox");
		CarTypes.instance().updateComboBox(typeComboBox);
	}
	
	private void updateCarsComboBox(){
		log.debug("update car combobox");
		carsComboBox.removeAllItems();
		String carType = (String)typeComboBox.getSelectedItem();
		// load car combobox
		carsComboBox.addItem(blank);
		List<String> cars = CarManager.instance().getByTypeList(carType);
		for (int i=0; i<cars.size(); i++){
			Car car = CarManager.instance().getById(cars.get(i));	
			carsComboBox.addItem(car);
		}
	}
	
	/**
	 * Add property listeners for locations and tracks
	 */
	private void addLocationAndTrackPropertyChange(){
		List<String> locations = locationManager.getLocationsByIdList();
		for (int i=0; i<locations.size(); i++){
			Location loc = locationManager.getLocationById(locations.get(i));
			loc.addPropertyChangeListener(this);
			List<String> tracks = loc.getTracksByNameList(null);
			for (int j=0; j<tracks.size(); j++){
				Track trk = loc.getTrackById(tracks.get(j));
				trk.addPropertyChangeListener(this);
			}
		}
	}
	
	/**
	 * Remove property listeners for locations and tracks
	 */
	private void removeLocationAndTrackPropertyChange(){
		List<String> locations = locationManager.getLocationsByIdList();
		for (int i=0; i<locations.size(); i++){
			Location loc = locationManager.getLocationById(locations.get(i));
			if (loc != null){
				loc.removePropertyChangeListener(this);
				List<String> tracks = loc.getTracksByNameList(null);
				for (int j=0; j<tracks.size(); j++){
					Track trk = loc.getTrackById(tracks.get(j));
					if (trk != null)
						trk.removePropertyChangeListener(this);
				}
			}
		}
	}
	
	public void dispose(){
		locationManager.removePropertyChangeListener(this);
		CarTypes.instance().removePropertyChangeListener(this);
		removeLocationAndTrackPropertyChange();
		if (train != null)
			train.removePropertyChangeListener(this);
		if (car != null)
			car.removePropertyChangeListener(this);
		super.dispose();
	}
	
 	public void propertyChange(java.beans.PropertyChangeEvent e) {
		if (log.isDebugEnabled()) 
			log.debug("Property change " +e.getPropertyName()+ " old: "+e.getOldValue()+ " new: "+e.getNewValue());
		if (e.getSource().equals(car) || e.getSource().equals(train))
			updateRoute();
		if (e.getSource().getClass().equals(Track.class) || e.getSource().getClass().equals(Location.class))
			updateRoute();
		if (e.getPropertyName().equals(LocationManager.LISTLENGTH_CHANGED_PROPERTY) ||
				e.getPropertyName().equals(Route.LISTCHANGE_CHANGED_PROPERTY))
			updateRoute();
		if (e.getPropertyName().equals(Train.DISPOSE_CHANGED_PROPERTY))
				dispose();
		if (e.getPropertyName().equals(CarTypes.CARTYPES_LENGTH_CHANGED_PROPERTY) ||
				e.getPropertyName().equals(CarTypes.CARTYPES_NAME_CHANGED_PROPERTY))
			updateComboBox();
 	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(TrainByCarTypeFrame.class.getName());
}
