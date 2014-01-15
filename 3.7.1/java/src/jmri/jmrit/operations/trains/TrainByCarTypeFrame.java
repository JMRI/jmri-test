// TrainByCarTypeFrame.java

package jmri.jmrit.operations.trains;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Schedule;
import jmri.jmrit.operations.locations.ScheduleItem;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.OperationsFrame;

import java.awt.*;

import javax.swing.*;

import java.text.MessageFormat;
import java.util.List;

/**
 * Frame to display by rolling stock, the locations serviced by this train
 * 
 * @author Dan Boudreau Copyright (C) 2010, 2013
 * @version $Revision$
 */

public class TrainByCarTypeFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

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
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		// Set up the panels
		JPanel pCarType = new JPanel();
		pCarType.setLayout(new GridBagLayout());
		pCarType.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Type")));
		pCarType.setMaximumSize(new Dimension(2000, 50));

		addItem(pCarType, typeComboBox, 0, 0);
		addItem(pCarType, carsComboBox, 1, 0);

		// increase width of combobox so large text names display properly
		Dimension boxsize = typeComboBox.getMinimumSize();
		if (boxsize != null) {
			boxsize.setSize(boxsize.width + 10, boxsize.height);
			typeComboBox.setMinimumSize(boxsize);
		}

		adjustCarsComboBoxSize();

		pLocations.setLayout(new GridBagLayout());
		JScrollPane locationPane = new JScrollPane(pLocations);
		locationPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		locationPane.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Route")));
		updateCarsComboBox();
		updateRoute();

		getContentPane().add(pCarType);
		getContentPane().add(locationPane);

		// setup combo box
		addComboBoxAction(typeComboBox);
		addComboBoxAction(carsComboBox);

		locationManager.addPropertyChangeListener(this);
		CarTypes.instance().addPropertyChangeListener(this);
		if (train != null) {
			train.addPropertyChangeListener(this);
			setTitle(Bundle.getMessage("MenuItemShowCarTypes") + " " + train.getName());
		}
		// listen to all tracks and locations
		addLocationAndTrackPropertyChange();

		setPreferredSize(null);
		initMinimumSize();
	}

	public void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
		log.debug("combo box action");
		if (ae.getSource().equals(typeComboBox))
			updateCarsComboBox();
		updateRoute();
	}

	private void updateRoute() {
		if (train == null)
			return;
		log.debug("update locations served by train " + train.getName());
		int y = 0;
		pLocations.removeAll();
		String carType = (String) typeComboBox.getSelectedItem();
		if (car != null)
			car.removePropertyChangeListener(this);
		car = null;
		if (carsComboBox.getSelectedItem() != null && !carsComboBox.getSelectedItem().equals(blank)) {
			car = (Car) carsComboBox.getSelectedItem();
			car.addPropertyChangeListener(this);
		}
		Route route = train.getRoute();
		if (route == null)
			return;
		List<RouteLocation> routeList = route.getLocationsBySequenceList();
		for (int i = 0; i < routeList.size(); i++) {
			JLabel loc = new JLabel();
			RouteLocation rl = routeList.get(i);
			String locationName = rl.getName();
			loc.setText(locationName);
			addItemLeft(pLocations, loc, 0, y++);
			Location location = locationManager.getLocationByName(locationName);
			if (car != null && car.getTrack() != null && !car.getTrack().acceptsDestination(location) && car.getLocation() != location) {
				JLabel locText = new JLabel();
				locText.setText(MessageFormat.format(Bundle.getMessage("CarOnTrackDestinationRestriction"),
						new Object[] { car.toString(), car.getTrackName() }));
				addItemWidth(pLocations, locText, 2, 1, y++);
				continue;
			}
			List<String> tracks = location.getTrackIdsByNameList(null);
			for (int j = 0; j < tracks.size(); j++) {
				Track track = location.getTrackById(tracks.get(j));
				// show the car's track if there's a track destination restriction
				if (car != null && car.getTrack() != null && !car.getTrack().acceptsDestination(location)
						&& car.getTrack() != track) {
					continue;
				}
				JLabel trk = new JLabel();
				trk.setText(track.getName());
				addItemLeft(pLocations, trk, 1, y);
				// is the car at this location and track?
				if (car != null && location.equals(car.getLocation()) && track.equals(car.getTrack())) {
					JLabel here = new JLabel("  -->"); // NOI18N
					addItemLeft(pLocations, here, 0, y);
				}
				JLabel op = new JLabel();
				addItemLeft(pLocations, op, 2, y++);
				if (!train.acceptsTypeName(carType))
					op.setText(Bundle.getMessage("X(TrainType)"));
				else if (car != null && !train.acceptsRoadName(car.getRoadName()))
					op.setText(Bundle.getMessage("X(TrainRoad)"));
				// TODO need to do the same tests for caboose changes in the train's route
				else if (car != null && car.isCaboose() && (train.getRequirements() & Train.CABOOSE) > 0
						&& location.equals(car.getLocation()) && track.equals(car.getTrack())
						&& !train.getCabooseRoad().equals("") && !car.getRoadName().equals(train.getCabooseRoad())
						&& location.getName().equals(train.getTrainDepartsName()))
					op.setText(Bundle.getMessage("X(TrainRoad)"));
				else if (car != null && car.hasFred() && (train.getRequirements() & Train.FRED) > 0
						&& location.equals(car.getLocation()) && track.equals(car.getTrack())
						&& !train.getCabooseRoad().equals("") && !car.getRoadName().equals(train.getCabooseRoad())
						&& location.getName().equals(train.getTrainDepartsName()))
					op.setText(Bundle.getMessage("X(TrainRoad)"));
				else if (car != null && !car.isCaboose() && !car.isPassenger()
						&& !train.acceptsLoad(car.getLoadName(), car.getTypeName()))
					op.setText(Bundle.getMessage("X(TrainLoad)"));
				else if (car != null && !train.acceptsBuiltDate(car.getBuilt()))
					op.setText(Bundle.getMessage("X(TrainBuilt)"));
				else if (car != null && !train.acceptsOwnerName(car.getOwner()))
					op.setText(Bundle.getMessage("X(TrainOwner)"));
				else if (train.skipsLocation(rl.getId()))
					op.setText(Bundle.getMessage("X(TrainSkips)"));
				else if (!rl.isDropAllowed() && !rl.isPickUpAllowed())
					op.setText(Bundle.getMessage("X(Route)"));
				else if (rl.getMaxCarMoves() <= 0)
					op.setText(Bundle.getMessage("X(RouteMoves)"));
				else if (!location.acceptsTypeName(carType))
					op.setText(Bundle.getMessage("X(LocationType)"));
				// check route before checking train, check train calls check route
				else if (!track.acceptsPickupRoute(route) && !track.acceptsDropRoute(route))
					op.setText(Bundle.getMessage("X(TrackRoute)"));
				else if (!track.acceptsPickupTrain(train) && !track.acceptsDropTrain(train))
					op.setText(Bundle.getMessage("X(TrackTrain)"));
				else if (!track.acceptsTypeName(carType))
					op.setText(Bundle.getMessage("X(TrackType)"));
				else if (car != null && !track.acceptsRoadName(car.getRoadName()))
					op.setText(Bundle.getMessage("X(TrackRoad)"));
				else if (car != null && !track.acceptsLoad(car.getLoadName(), car.getTypeName()))
					op.setText(Bundle.getMessage("X(TrackLoad)"));
				else if (car != null && !track.acceptsDestination(car.getFinalDestination()))
					op.setText(Bundle.getMessage("X(TrackDestination)"));
				else if ((rl.getTrainDirection() & location.getTrainDirections()) == 0)
					op.setText(Bundle.getMessage("X(DirLoc)"));
				else if ((rl.getTrainDirection() & track.getTrainDirections()) == 0)
					op.setText(Bundle.getMessage("X(DirTrk)"));
				else if (!checkScheduleAttribute(TYPE, carType, null, track))
					op.setText(Bundle.getMessage("X(ScheduleType)"));
				else if (!checkScheduleAttribute(LOAD, carType, car, track))
					op.setText(Bundle.getMessage("X(ScheduleLoad)"));
				else if (!checkScheduleAttribute(ROAD, carType, car, track))
					op.setText(Bundle.getMessage("X(ScheduleRoad)"));
				else if (!checkScheduleAttribute(TIMETABLE, carType, car, track))
					op.setText(Bundle.getMessage("X(ScheduleTimeTable)"));
				else if (!checkScheduleAttribute(ALL, carType, car, track))
					op.setText(Bundle.getMessage("X(Schedule)"));
				else if (!track.acceptsPickupTrain(train)) {
					// can the train drop off car?
					if (rl.isDropAllowed() && track.acceptsDropTrain(train))
						op.setText(Bundle.getMessage("DropOnly"));
					else
						op.setText(Bundle.getMessage("X(TrainPickup)"));
				} else if (!track.acceptsDropTrain(train))
					// can the train pick up car?
					if (rl.isPickUpAllowed() && track.acceptsPickupTrain(train))
						op.setText(Bundle.getMessage("PickupOnly"));
					else
						op.setText(Bundle.getMessage("X(TrainDrop)"));
				else if (rl.isDropAllowed() && rl.isPickUpAllowed())
					op.setText(Bundle.getMessage("OK"));
				else if (rl.isDropAllowed())
					op.setText(Bundle.getMessage("DropOnly"));
				else if (rl.isPickUpAllowed())
					op.setText(Bundle.getMessage("PickupOnly"));
				else
					op.setText("X"); // default shouldn't occur
			}
		}
		pLocations.revalidate();
		repaint();
	}

	private static final String ROAD = "road"; // NOI18N
	private static final String LOAD = "load"; // NOI18N
	private static final String TIMETABLE = "timetable"; // NOI18N
	private static final String TYPE = "type"; // NOI18N
	private static final String ALL = "all"; // NOI18N

	private boolean checkScheduleAttribute(String attribute, String carType, Car car, Track track) {
		Schedule schedule = track.getSchedule();
		if (schedule == null)
			return true;
		// if car is already placed at track, don't check car type and load
		if (car != null && car.getTrack() == track)
			return true;
		List<ScheduleItem> scheduleItems = schedule.getItemsBySequenceList();
		for (int i = 0; i < scheduleItems.size(); i++) {
			ScheduleItem si = scheduleItems.get(i);
			// check to see if schedule services car type
			if (attribute.equals(TYPE) && si.getTypeName().equals(carType))
				return true;
			// check to see if schedule services car type and load
			if (attribute.equals(LOAD)
					&& si.getTypeName().equals(carType)
					&& (si.getReceiveLoadName().equals("") || car == null || si.getReceiveLoadName().equals(
							car.getLoadName())))
				return true;
			// check to see if schedule services car type and road
			if (attribute.equals(ROAD) && si.getTypeName().equals(carType)
					&& (si.getRoadName().equals("") || car == null || si.getRoadName().equals(car.getRoadName())))
				return true;
			// check to see if schedule timetable allows delivery
			if (attribute.equals(TIMETABLE)
					&& si.getTypeName().equals(carType)
					&& (si.getTrainScheduleId().equals("") || TrainManager.instance().getTrainScheduleActiveId()
							.equals(si.getTrainScheduleId())))
				return true;
			// check to see if at least one schedule item can service car
			if (attribute.equals(ALL)
					&& si.getTypeName().equals(carType)
					&& (si.getReceiveLoadName().equals("") || car == null || si.getReceiveLoadName().equals(
							car.getLoadName()))
					&& (si.getRoadName().equals("") || car == null || si.getRoadName().equals(car.getRoadName()))
					&& (si.getTrainScheduleId().equals("") || TrainManager.instance().getTrainScheduleActiveId()
							.equals(si.getTrainScheduleId())))
				return true;
		}
		return false;
	}

	private void updateComboBox() {
		log.debug("update combobox");
		CarTypes.instance().updateComboBox(typeComboBox);
	}

	private void updateCarsComboBox() {
		log.debug("update car combobox");
		carsComboBox.removeAllItems();
		String carType = (String) typeComboBox.getSelectedItem();
		// load car combobox
		carsComboBox.addItem(blank);
		List<RollingStock> cars = CarManager.instance().getByTypeList(carType);
		for (int i = 0; i < cars.size(); i++) {
			Car car = (Car) cars.get(i);
			carsComboBox.addItem(car);
		}
	}

	private void adjustCarsComboBoxSize() {
		List<RollingStock> cars = CarManager.instance().getList();
		for (int i = 0; i < cars.size(); i++) {
			Car car = (Car) cars.get(i);
			carsComboBox.addItem(car);
		}
		Dimension boxsize = carsComboBox.getMinimumSize();
		if (boxsize != null) {
			boxsize.setSize(boxsize.width + 10, boxsize.height);
			carsComboBox.setMinimumSize(boxsize);
		}
		carsComboBox.removeAllItems();
	}

	/**
	 * Add property listeners for locations and tracks
	 */
	private void addLocationAndTrackPropertyChange() {
		List<Location> locations = locationManager.getLocationsByIdList();
		for (int i = 0; i < locations.size(); i++) {
			Location loc = locations.get(i);
			loc.addPropertyChangeListener(this);
			List<String> tracks = loc.getTrackIdsByNameList(null);
			for (int j = 0; j < tracks.size(); j++) {
				Track track = loc.getTrackById(tracks.get(j));
				track.addPropertyChangeListener(this);
				Schedule schedule = track.getSchedule();
				if (schedule != null)
					schedule.addPropertyChangeListener(this);
			}
		}
	}

	/**
	 * Remove property listeners for locations and tracks
	 */
	private void removeLocationAndTrackPropertyChange() {
		List<Location> locations = locationManager.getLocationsByIdList();
		for (int i = 0; i < locations.size(); i++) {
			Location loc = locations.get(i);
			if (loc != null) {
				loc.removePropertyChangeListener(this);
				List<String> tracks = loc.getTrackIdsByNameList(null);
				for (int j = 0; j < tracks.size(); j++) {
					Track track = loc.getTrackById(tracks.get(j));
					if (track != null) {
						track.removePropertyChangeListener(this);
						Schedule schedule = track.getSchedule();
						if (schedule != null)
							schedule.removePropertyChangeListener(this);
					}
				}
			}
		}
	}

	public void dispose() {
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
			log.debug("Property change " + e.getPropertyName() + " old: " + e.getOldValue() + " new: "
					+ e.getNewValue()); // NOI18N
		if (e.getSource().equals(car) || e.getSource().equals(train))
			updateRoute();
		if (e.getSource().getClass().equals(Track.class) || e.getSource().getClass().equals(Location.class)
				|| e.getSource().getClass().equals(Schedule.class))
			updateRoute();
		if (e.getPropertyName().equals(LocationManager.LISTLENGTH_CHANGED_PROPERTY)
				|| e.getPropertyName().equals(Route.LISTCHANGE_CHANGED_PROPERTY))
			updateRoute();
		if (e.getPropertyName().equals(Train.DISPOSE_CHANGED_PROPERTY))
			dispose();
		if (e.getPropertyName().equals(CarTypes.CARTYPES_LENGTH_CHANGED_PROPERTY)
				|| e.getPropertyName().equals(CarTypes.CARTYPES_NAME_CHANGED_PROPERTY))
			updateComboBox();
		if (e.getPropertyName().equals(Location.LENGTH_CHANGED_PROPERTY)) {
			// a track has been add or deleted update property listeners
			removeLocationAndTrackPropertyChange();
			addLocationAndTrackPropertyChange();
		}
	}

	static Logger log = LoggerFactory.getLogger(TrainByCarTypeFrame.class.getName());
}
