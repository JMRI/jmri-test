package jmri.jmrit.operations.rollingstock;

import java.beans.PropertyChangeEvent;
import java.util.ResourceBundle;

import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.cars.CarColors;
import jmri.jmrit.operations.rollingstock.cars.CarOwners;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;

/**
 * Represents rolling stock, both powered (engines) and not powered (cars) on
 * the layout.
 * 
 * @author Daniel Boudreau Copyright (C) 2009, 2010
 * @version $Revision: 1.44 $
 */
public class RollingStock implements java.beans.PropertyChangeListener{

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.rollingstock.cars.JmritOperationsCarsBundle");
	
	private static final String DEFAULT_WEIGHT = "0";
	
	protected String _id = "";
	protected String _number = "";
	protected String _road = "";
	protected String _type = "";
	protected String _length = "";
	protected String _color = "";
	protected String _weight = DEFAULT_WEIGHT;
	protected String _weightTons = DEFAULT_WEIGHT;
	protected String _built = "";
	protected String _owner = "";
	protected String _comment = "";
	protected String _routeId = "";  		// saved route for interchange tracks
	protected String _rfid = "";
	protected boolean _locationUnknown = false;
	protected boolean _outOfService = false;
	
	protected Location _location = null;
	protected Track _trackLocation = null;
	protected Location _destination = null;
	protected Track _trackDestination = null;
	protected Train _train = null;
	protected RouteLocation _routeLocation = null;
	protected RouteLocation _routeDestination = null;
	protected int _moves = 0;
	
	public int number = 0;	// used by managers for sort by number
	
	public static final String OKAY = rb.getString("okay");			// return status when placing rolling stock at a location
	public static final String LENGTH = rb.getString("length");
	public static final String TYPE = rb.getString("type");
	public static final String ROAD = rb.getString("road");
	public static final String SCHEDULE = rb.getString("schedule");
	public static final String LOAD = rb.getString("load");
	
	public static final String LOCATION_CHANGED_PROPERTY = "rolling stock location";  		// property change descriptions
	public static final String TRACK_CHANGED_PROPERTY = "rolling stock track location";
	public static final String DESTINATION_CHANGED_PROPERTY = "rolling stock destination";
	public static final String DESTINATIONTRACK_CHANGED_PROPERTY = "rolling stock track destination";
	public static final String TRAIN_CHANGED_PROPERTY = "rolling stock train";
	
	public static final int COUPLER = 4;		// draw bar length between rolling stocks

	LocationManager locationManager = LocationManager.instance();
	
	public RollingStock(){
		
	}
	
	public RollingStock(String road, String number) {
		log.debug("New rolling stock " + road + " " + number);
		_road = road;
		_number = number;
		_id = createId(road, number);
		addPropertyChangeListeners();
	}

	public static String createId(String road, String number) {
		String id = road + number;
		return id;
	}

	public String getId() {
		return _id;
	}

	public void setNumber(String number) {
		String old = _number;
		_number = number;
		if (!old.equals(number))
			firePropertyChange("rolling stock number", old, number);
	}

	public String getNumber() {
		return _number;
	}

	public void setRoad(String road) {
		String old = _road;
		_road = road;
		if (!old.equals(road))
			firePropertyChange("rolling stock road", old, road);
	}

	public String getRoad() {
		return _road;
	}
	
	/**
	 * For combobox and identification
	 */
	public String toString(){
		return getRoad()+" "+getNumber();
	}

	public void setType(String type) {
		String old = _type;
		_type = type;
		if (!old.equals(type))
			firePropertyChange("rolling stock type", old, type);
	}

	public String getType() {
		return _type;
	}

	public void setLength(String length) {
		String old = _length;
		_length = length;
		// adjust used length if rolling stock is at a location
		if (_location != null && _trackLocation != null){
			_location.setUsedLength(_location.getUsedLength() + Integer.parseInt(length) - Integer.parseInt(old));
			_trackLocation.setUsedLength(_trackLocation.getUsedLength() + Integer.parseInt(length) - Integer.parseInt(old));
		}
		if (!old.equals(length))
			firePropertyChange("rolling stock length", old, length);
	}

	public String getLength() {
		return _length;
	}

	public void setColor(String color) {
		String old = _color;
		_color = color;
		if (!old.equals(color))
			firePropertyChange("rolling stock color", old, color);
	}

	public String getColor() {
		return _color;
	}

	/**
	 * 
	 * @param weight rolling stock weight in ounces.
	 */
	public void setWeight(String weight) {
		String old = _weight;
		_weight = weight;
		if (!old.equals(weight))
			firePropertyChange("rolling stock weight", old, weight);
	}

	public String getWeight() {
		return _weight;
	}
	
	/**
	 * 
	 * @param weight full scale rolling stock weight in tons.
	 */
	public void setWeightTons(String weight) {
		String old = _weightTons;
		_weightTons = weight;
		if (!old.equals(weight))
			firePropertyChange("rolling stock weight tons", old, weight);
	}

	public String getWeightTons() {
		if (!_weightTons.equals(DEFAULT_WEIGHT))
			return _weightTons;

		double weight = 0;
		try{
			weight = Double.parseDouble(getWeight());
		}catch (Exception e){
			// log.debug("Weight not set for rolling stock ("+toString()+")");
		}
		return Integer.toString((int)(weight*Setup.getScaleTonRatio()));
	}
	
	public int getAdjustedWeightTons(){
		int weightTons =0;
		try {
			// get loaded weight
			weightTons = Integer.parseInt(getWeightTons());
		} catch (Exception e){
			log.debug ("Rolling stock ("+toString()+") weight not set");
		}
		return weightTons;
	}

	public void setBuilt(String built) {
		String old = _built;
		_built = built;
		if (!old.equals(built))
			firePropertyChange("rolling stock built", old, built);
	}

	public String getBuilt() {
		return _built;
	}
	
	public String getStatus(){
		return (isLocationUnknown()?"<?> ":(isOutOfService()?"<O> ":""));
	}
	
	public Location getLocation() {
		return _location;
	}
	
	public void setLocation(Location location){
		_location = location;
	}
	
	/**
	 * Get rolling stock's location name
	 * @return empty string if rolling stock isn't on layout
	 */
	public String getLocationName() {
		if (_location != null)
			return _location.getName();
		return "";
	}
	
	/**
	 * Get rolling stock's location id
	 * @return empty string if rolling stock isn't on the layout
	 */
	public String getLocationId() {
		if (_location != null)
			return _location.getId();
		return "";
	}
	
	public Track getTrack() {
		return _trackLocation;
	}
	
	public void setTrack(Track track){
		_trackLocation = track;
	}
	
	/**
	 * Get rolling stock's track name
	 * @return empty string if rolling stock isn't on a track
	 */
	public String getTrackName() {
		if (_trackLocation != null)
			return _trackLocation.getName();
		return "";
	}
	
	/**
	 * Get rolling stock's track id
	 * @return empty string if rolling stock isn't on a track
	 */
	public String getTrackId() {
		if (_trackLocation != null)
			return _trackLocation.getId();
		return "";
	}
	
	/**
	 * Sets rolling stock location on the layout
	 * @param location 
	 * @param track (yard, siding, staging, or interchange track)
	 * 
	 * @return "okay" if successful, "type" if the rolling stock's type isn't 
	 * acceptable, or "length" if the rolling stock length didn't fit.
	 */
	public String setLocation(Location location, Track track){
		return setLocation(location, track, false);
	}
	
	/**
	 * Sets rolling stock location on the layout
	 * @param location
	 * @param track (yard, siding, staging, or interchange track)
	 * @param force when true place rolling stock ignore track length, type, & road
	 * @return "okay" if successful, "type" if the rolling stock's type isn't 
	 * acceptable, or "length" if the rolling stock length didn't fit.
	 */
	protected String setLocation(Location location, Track track, boolean force) {
		// first determine if rolling stock can be move to the new location
		if (!force){
			String status = testLocation(location, track);
			if (status != OKAY)
				return status;
		}
		// now update
		Location oldLocation = _location;
		_location = location;
		Track oldTrack = _trackLocation;
		_trackLocation = track;

		if (oldLocation != location || oldTrack != track) {
			// update rolling stock location on layout, maybe this should be a property change?
			// first remove rolling stock from existing location
			if (oldLocation != null){
				oldLocation.deleteRS(this);
				oldLocation.removePropertyChangeListener(this);
				// if track is null, then rolling stock is in a train
				if (oldTrack != null){
					oldTrack.deleteRS(this);
					oldTrack.removePropertyChangeListener(this);
					//	if there's a destination then pickup complete
					if (_destination != null){
						oldLocation.deletePickupRS();
						oldTrack.deletePickupRS(this);
					}
				}
			}
			if (_location != null) {
				_location.addRS(this);
				//	Need to know if location name changes so we can forward to listeners 
				_location.addPropertyChangeListener(this);
			} 
			if (_trackLocation != null){
				_trackLocation.addRS(this);
				//	Need to know if location name changes so we can forward to listeners 
				_trackLocation.addPropertyChangeListener(this);
				// if there's a destination then there's a pickup
				if (_destination != null){
					_location.addPickupRS();
					_trackLocation.addPickupRS(this);
				}
			} 
			firePropertyChange(LOCATION_CHANGED_PROPERTY, oldLocation, location);
			firePropertyChange(TRACK_CHANGED_PROPERTY, oldTrack, track);
		}
		return OKAY;
	}
	
	public String testLocation(Location location, Track track){
		if (location == null && track == null)
			return OKAY;
		// first determine if rolling stock can be move to the new location
		if (location != null && !location.acceptsTypeName(getType())){
			log.debug("Can't set (" + toString() + ") type (" +getType()+ ") at location ("+ location.getName() + ") wrong type");
			return TYPE;
		}
		if (location != null && track != null && !track.acceptsTypeName(getType())){
			log.debug("Can't set (" + toString() + ") type (" +getType()+ ") at location ("+ location.getName() + ", " + track.getName() + ") wrong type");
			return TYPE;
		}
		if (location != null && track != null && !track.acceptsRoadName(getRoad())){
			log.debug("Can't set (" + toString() + ") road (" +getRoad()+ ") at location ("+ location.getName() + ", " + track.getName() + ") wrong road");
			return ROAD;
		}
		// now determine if there's enough space for the rolling stock
		try{
			Integer.parseInt(getLength());
		} catch (Exception e){
			return LENGTH;
		}
		if (location != null && track != null && _trackLocation != track &&
				(track.getUsedLength() + track.getReserved() + Integer.parseInt(getLength()) + COUPLER) > track.getLength()){
			log.debug("Can't set (" + toString() + ") at location ("+ location.getName() + ", " + track.getName() + ") no room!");
			return LENGTH;	
		}
		return OKAY;
	}
	
	/**
	 * Sets rolling stock destination on the layout
	 * @param destination 
	 * @param track (yard, siding, staging, or interchange track)
	 * @return "okay" if successful, "type" if the rolling stock's type isn't 
	 * acceptable, or "length" if the rolling stock length didn't fit.
	 */
	public String setDestination(Location destination, Track track) {
		return setDestination(destination, track, false);
	}

	/**
	 * Sets rolling stock destination on the layout
	 * @param destination 
	 * @param track (yard, siding, staging, or interchange track)
	 * @param force when true ignore track length, type, & road when setting destination
	 * @return "okay" if successful, "type" if the rolling stock's type isn't 
	 * acceptable, or "length" if the rolling stock length didn't fit.
	 */
	public String setDestination(Location destination, Track track, boolean force) {
		// first determine if rolling stock can be move to the new destination
		String status = RsTestDestination(destination, track);
		if (!force && !status.equals(OKAY)){
			return status;
		}
		// now set the rolling stock destination!	
		Location oldDestination = _destination;
		_destination = destination;
		Track oldTrack = _trackDestination;
		_trackDestination = track;

		if (oldDestination != destination || oldTrack != track) {
			if (oldDestination != null){
				oldDestination.deleteDropRS();
				oldDestination.removePropertyChangeListener(this);
				// delete pickup in case destination is null
				if(_location != null && _trackLocation != null){
					_location.deletePickupRS();
					_trackLocation.deletePickupRS(this);
				}
			}
			if (oldTrack != null){
				oldTrack.deleteDropRS(this);
				oldTrack.removePropertyChangeListener(this);
			}
			if (_destination != null){
				_destination.addDropRS();
				if(_location != null && _trackLocation != null){
					_location.addPickupRS();
					_trackLocation.addPickupRS(this);
				}
			
				// Need to know if destination name changes so we can forward to listeners 
				_destination.addPropertyChangeListener(this);
			} 
			if (_trackDestination != null){
				_trackDestination.addDropRS(this);
				// Need to know if destination name changes so we can forward to listeners 
				_trackDestination.addPropertyChangeListener(this);
			} else {
				// rolling stock has been terminated bump rolling stock moves
				setMoves(++_moves);
				if (getTrain() != null && getTrain().getRoute() != null)
					setSavedRouteId(getTrain().getRoute().getId());
				setRouteLocation(null);
				setRouteDestination(null);
			}

			firePropertyChange(DESTINATION_CHANGED_PROPERTY, oldDestination, destination);
			firePropertyChange(DESTINATIONTRACK_CHANGED_PROPERTY, oldTrack, track);
		}
		return status;
	}
	
	/**
	 * Used to check destination track to see if it will accept rolling stock
	 * @param destination
	 * @param track
	 * @return status
	 */
	public String testDestination(Location destination, Track track) {
		return RsTestDestination(destination, track);
	}
	
	private String RsTestDestination(Location destination, Track track) {
		// first determine if rolling stock can be move to the new destination
		if (destination != null && !destination.acceptsTypeName(getType())){
			log.debug("Can't set (" + toString() + ") type (" +getType()+ ") at destination ("+ destination.getName() + ") wrong type");
			return TYPE + " ("+getType()+")";
		}
		if (destination != null && track != null && !track.acceptsTypeName(getType())){
			log.debug("Can't set (" + toString() + ") type (" +getType()+ ") at destination ("+ destination.getName() + ", " +track.getName() + ") wrong type");
			return TYPE+ " ("+getType()+")";
		}
		if (destination != null && track != null && !track.acceptsRoadName(getRoad())){
			log.debug("Can't set (" + toString() + ") road (" +getRoad()+ ") at destination ("+ destination.getName() + ", " + track.getName() + ") wrong road");
			return ROAD+ " ("+getRoad()+")";
		}
		// does rolling stock already have this destination?
		if (destination == getDestination() && track == getDestinationTrack())
			return OKAY;
		// is rolling stock returning to same track?
		if (track == getTrack())
			return OKAY;
		// now determine if there's enough space for the rolling stock
		int length = 0;
		try {
			length = Integer.parseInt(getLength())+ COUPLER;
		} catch (Exception e){
			return LENGTH+ " ("+getLength()+")";
		}	
		if (destination != null && track != null &&
				track.getUsedLength() + track.getReserved()+ length > track.getLength()){
			log.debug("Can't set (" + toString() + ") at track destination ("+ destination.getName() + ", " + track.getName() + ") no room!");
			return LENGTH+ " ("+getLength()+")";	
		}
		return OKAY;
	}
	
	public Location getDestination() {
		return _destination;
	}
	
	public void setDestination(Location destination){
		_destination = destination;
	}
	
	public String getDestinationName() {
		if (_destination != null)
			return _destination.getName();
		return "";
	}
	
	public String getDestinationId() {
		if (_destination != null)
			return _destination.getId();
		return "";
	}
	
	public void setDestinationTrack(Track track){
		_trackDestination = track;
	}

	public Track getDestinationTrack() {
		return _trackDestination;
	}
	
	public String getDestinationTrackName() {
		if (_trackDestination != null)
			return _trackDestination.getName();
		return "";
	}
	
	public String getDestinationTrackId() {
		if (_trackDestination != null)
			return _trackDestination.getId();
		return "";
	}
	
	public void setMoves(int moves){
		int old = _moves;
		_moves = moves;
		if (old != moves)
			firePropertyChange("rolling stock moves", Integer.toString(old), Integer.toString(moves));
	}
	public int getMoves(){
		return _moves;
	}

	public void setTrain(Train train) {
		Train old = _train;
		_train = train;
		if ((old != null && !old.equals(train)) || old != train){
			if(old != null){
				old.removePropertyChangeListener(this);
			}
			if(train != null)
				train.addPropertyChangeListener(this);
			firePropertyChange(TRAIN_CHANGED_PROPERTY, old, train);
		}
	}

	public Train getTrain() {
		return _train;
	}
	
	public String getTrainName(){
		if (getTrain() != null)
			return getTrain().getName();
		return "";
	}
	
	public void setRouteLocation (RouteLocation routeLocation){
		if(_location == null){
			log.debug("WARNING rolling stock ("+toString()+") does not have an assigned location");
		}
		else if(routeLocation != null && _location != null && !routeLocation.getName().equals(_location.getName()))
			log.debug("WARNING route location name("+routeLocation.getName()+") not equal to location name ("+_location.getName()+") for rolling stock ("+toString()+")" );
		RouteLocation old = _routeLocation;
		_routeLocation = routeLocation;
		if (old != routeLocation)
			firePropertyChange("new route location", old, routeLocation);
	}
	
	public RouteLocation getRouteLocation(){
		return _routeLocation;
	}
	
	public String getRouteLocationId(){
		if(_routeLocation != null)
			return _routeLocation.getId();
		return "";
	}
	
	public String getSavedRouteId(){
		return _routeId;
	}
	
	public void setSavedRouteId(String id){
		_routeId = id;
	}
	
	public String getRfid(){
		return _rfid;
	}
	
	/**
	 * Sets the RFID for this rolling stock.
	 * @param id 12 character RFID string.
	 */
	public void setRfid(String id){
		String old = _rfid;
		_rfid = id;
		if (!old.equals(id))
			firePropertyChange("rolling stock rfid", old, id);
	}
	
	public void setRouteDestination (RouteLocation routeDestination){
		if(routeDestination != null && _destination != null && !routeDestination.getName().equals(_destination.getName()))
			log.debug("WARNING route destination name ("+routeDestination.getName()+") not equal to destination name ("+_destination.getName()+") for rolling stock ("+toString()+")" );
		_routeDestination = routeDestination;
	}
	
	public RouteLocation getRouteDestination(){
		return _routeDestination;
	}
	
	public String getRouteDestinationId(){
		if(_routeDestination != null)
			return _routeDestination.getId();
		return "";
	}

	public void setOwner(String owner) {
		String old = _owner;
		_owner = owner;
		if (!old.equals(owner))
			firePropertyChange("rolling stock owner", old, owner);
	}

	public String getOwner() {
		return _owner;
	}
	
	public void setLocationUnknown(boolean unknown){
		boolean old = _locationUnknown;
		_locationUnknown = unknown;
		if (!old == unknown)
			firePropertyChange("car location known", old?"true":"false", unknown?"true":"false");
	}
	
	/**
	 * 
	 * @return true when car's location is unknown
	 */
	public boolean isLocationUnknown(){
		return _locationUnknown;
	}
	
	public void setOutOfService(boolean outOfService){
		boolean old = _outOfService;
		_outOfService = outOfService;
		if (!old == outOfService)
			firePropertyChange("car out of service", old?"true":"false", outOfService?"true":"false");
	}
	
	/**
	 * 
	 * @return true when rolling stock is out of service
	 */
	public boolean isOutOfService(){
		return _outOfService;
	}
	
	// normally overridden
	public String getPriority(){
		return "";
	}

	public void setComment(String comment) {
		_comment = comment;
	}

	public String getComment() {
		return _comment;
	}
	
	public void moveRollingStock(RouteLocation old, RouteLocation next){
		if(old == getRouteLocation()){	
			// Arriving at destination?
			if(getRouteLocation() == getRouteDestination()){
				log.debug("Rolling stock ("+toString()+") has arrived at destination ("+getDestination()+")");
				setLocation(getDestination(), getDestinationTrack(), true);	// force RS to destination
				setDestination(null, null); 	// this also clears the route locations
				setTrain(null);
			}else{
				log.debug("Rolling stock ("+toString()+") is in train (" +_train.getName()+") leaves location ("+old.getName()+") destination ("+next.getName()+")");
				Location nextLocation = locationManager.getLocationByName(next.getName());
				setLocation(nextLocation, null, true); // force RS to location
				setRouteLocation(next);
			}
		}
	}
	
	/**
	 * Remove rolling stock.  Releases all listeners.
	 */
	public void dispose(){
       	setTrain(null);
    	setDestination(null, null);
        setLocation(null, null);
        CarRoads.instance().removePropertyChangeListener(this);
        CarOwners.instance().removePropertyChangeListener(this);
        CarColors.instance().removePropertyChangeListener(this);
	}
	
	/**
	 * Construct this Entry from XML. 
	 * 
	 * @param e  RollingStock XML element
	 */
	public void rollingStock(org.jdom.Element e) {
		org.jdom.Attribute a;
		if ((a = e.getAttribute("id")) != null)
			_id = a.getValue();
		else
			log.warn("no id attribute in rolling stock element when reading operations");
		if ((a = e.getAttribute("roadNumber")) != null)
			_number = a.getValue();
		if ((a = e.getAttribute("roadName")) != null)
			_road = a.getValue();
		if ((a = e.getAttribute("type")) != null)
			_type = a.getValue();
		if ((a = e.getAttribute("length")) != null)
			_length = a.getValue();
		if ((a = e.getAttribute("color")) != null)
			_color = a.getValue();
		if ((a = e.getAttribute("weight")) != null)
			_weight = a.getValue();
		if ((a = e.getAttribute("weightTons")) != null)
			setWeightTons(a.getValue());
		if ((a = e.getAttribute("built")) != null)
			_built = a.getValue();
		Location location = null;
		Track track = null;
		if ((a = e.getAttribute("locationId")) != null)
			location = locationManager.getLocationById(a.getValue());
		if ((a = e.getAttribute("secLocationId")) != null && location != null)
			track = location.getTrackById(a.getValue());
		String status = setLocation(location, track, true);		// force location
		if (!status.equals(OKAY) && location!=null && track!=null)
			log.warn("Could not place ("+getRoad()+" "+getNumber()+") at location ("+location.getName()+") track ("+track.getName()
					+") because of ("+status+")");
		Location destination = null;
		Track trackDestination = null;
		if ((a = e.getAttribute("destinationId")) != null)
			destination = locationManager.getLocationById(a.getValue());
		if ((a = e.getAttribute("secDestinationId")) != null  && destination != null)
			trackDestination = destination.getTrackById(a.getValue());
		status = setDestination(destination, trackDestination, true);	// force destination
		if (!status.equals(OKAY) && destination!=null && trackDestination!=null)
			log.warn("Forced destination for rolling stock ("+getRoad()+" "+getNumber()+") destination ("+destination.getName()
					+") track ("+trackDestination.getName()+") because of ("+status+")");
		if ((a = e.getAttribute("moves")) != null)
			_moves = Integer.parseInt(a.getValue());
		if ((a = e.getAttribute("train")) != null){
			setTrain(TrainManager.instance().getTrainByName(a.getValue()));
			if (_train != null && _train.getRoute() != null && (a = e.getAttribute("routeLocationId")) != null){
				_routeLocation = _train.getRoute().getLocationById(a.getValue());
				if((a = e.getAttribute("routeDestinationId")) != null)
					_routeDestination = _train.getRoute().getLocationById(a.getValue());
			}
		}
		if ((a = e.getAttribute("lastRouteId")) != null)
			_routeId = a.getValue();
		if ((a = e.getAttribute("owner")) != null)
			_owner = a.getValue();
		if ((a = e.getAttribute("comment")) != null)
			_comment = a.getValue();
		if ((a = e.getAttribute("rfid")) != null)
			_rfid = a.getValue();
		if ((a = e.getAttribute("outOfService")) != null)
			_outOfService = a.getValue().equals("true");
		addPropertyChangeListeners();
	}

	boolean verboseStore = false;
	/**
	 * Add XML elements to represent this Entry. 
	 * 
	 * @return Contents in a JDOM Element
	 */
	public org.jdom.Element store(org.jdom.Element e) {
		e.setAttribute("id", getId());
		e.setAttribute("roadName", getRoad());
		e.setAttribute("roadNumber", getNumber());
		e.setAttribute("type", getType());
		e.setAttribute("length", getLength());
		if(!getColor().equals(""))
			e.setAttribute("color", getColor());
		if(!getWeight().equals(DEFAULT_WEIGHT))
			e.setAttribute("weight", getWeight());
		if (!getWeightTons().equals(""))
			e.setAttribute("weightTons", getWeightTons());
		if (!getBuilt().equals(""))
			e.setAttribute("built", getBuilt());
		if (!getLocationId().equals(""))
			e.setAttribute("locationId", getLocationId());
		if (!getRouteLocationId().equals(""))
			e.setAttribute("routeLocationId", getRouteLocationId());
		if (!getTrackId().equals(""))
			e.setAttribute("secLocationId", getTrackId());
		if (!getDestinationId().equals(""))
			e.setAttribute("destinationId", getDestinationId());
		if (!getRouteDestinationId().equals(""))
			e.setAttribute("routeDestinationId", getRouteDestinationId());
		if (!getDestinationTrackId().equals(""))
			e.setAttribute("secDestinationId", getDestinationTrackId());
		if (!getSavedRouteId().equals(""))
			e.setAttribute("lastRouteId", getSavedRouteId());
		if (verboseStore){
			e.setAttribute("location", getLocationName());
			e.setAttribute("track", getTrackName());
			e.setAttribute("destination", getDestinationName());
			e.setAttribute("desTrack", getDestinationTrackName());
		}
		e.setAttribute("moves", Integer.toString(getMoves()));
		if (!getTrainName().equals(""))
			e.setAttribute("train",	getTrainName());
		if (!getOwner().equals(""))
			e.setAttribute("owner", getOwner());
		if (!getComment().equals("") )
			e.setAttribute("comment", getComment());
		if (!getRfid().equals("") )
			e.setAttribute("rfid", getRfid());
		if (isLocationUnknown())
			e.setAttribute("locUnknown", isLocationUnknown()?"true":"false");
		if (isOutOfService())
			e.setAttribute("outOfService", isOutOfService()?"true":"false");
		return e;
	}
	
	private void addPropertyChangeListeners(){
		CarRoads.instance().addPropertyChangeListener(this);
		CarOwners.instance().addPropertyChangeListener(this);
		CarColors.instance().addPropertyChangeListener(this);
	}
	
	// rolling stock listens for changes in a location name or if a location is deleted
    public void propertyChange(PropertyChangeEvent e) {
    	//if (log.isDebugEnabled()) log.debug("Property change for rolling stock: " + toString()+ " property name: " +e.getPropertyName()+ " old: "+e.getOldValue()+ " new: "+e.getNewValue());
    	// notify if track or location name changes
    	if (e.getPropertyName().equals(Location.NAME_CHANGED_PROPERTY)){
        	if (log.isDebugEnabled()) log.debug("Property change for rolling stock: " + toString()+ " property name: " +e.getPropertyName()+ " old: "+e.getOldValue()+ " new: "+e.getNewValue());
    		firePropertyChange(e.getPropertyName(), e.getOldValue(), e.getNewValue());
    	}
    	if (e.getPropertyName().equals(Location.DISPOSE_CHANGED_PROPERTY)){
    	    if (e.getSource() == _location){
        	   	if (log.isDebugEnabled()) log.debug("delete location for rolling stock: " + toString());
    	    	setLocation(null, null);
    	    }
    	    if (e.getSource() == _destination){
        	   	if (log.isDebugEnabled()) log.debug("delete destination for rolling stock: " + toString());
    	    	setDestination(null, null);
    	    }
     	}
    	if (e.getPropertyName().equals(Track.DISPOSE_CHANGED_PROPERTY)){
    	    if (e.getSource() == _trackLocation){
        	   	if (log.isDebugEnabled()) log.debug("delete location for rolling stock: " + toString());
    	    	setLocation(_location, null);
    	    }
    	    if (e.getSource() == _trackDestination){
        	   	if (log.isDebugEnabled()) log.debug("delete destination for rolling stock: " + toString());
    	    	setDestination(_destination, null);
    	    }  	    	
    	}
    	if (e.getPropertyName().equals(Train.DISPOSE_CHANGED_PROPERTY) &&
    			e.getSource() == _train){
    		if (log.isDebugEnabled()) log.debug("delete train for rolling stock: " + toString());
    		setTrain(null);
    	}
    	if (e.getPropertyName().equals(Train.TRAIN_LOCATION_CHANGED_PROPERTY) &&
    			e.getSource() == _train){
    		if (log.isDebugEnabled()) log.debug("Rolling stock (" +toString()+") is serviced by train ("+_train.getName()+")");
    		moveRollingStock((RouteLocation)e.getOldValue(),(RouteLocation)e.getNewValue());
    	}
    	if (e.getPropertyName().equals(Train.STATUS_CHANGED_PROPERTY) &&
    			e.getNewValue().equals(Train.TRAINRESET) &&
    			e.getSource() == _train){
    		if (log.isDebugEnabled()) log.debug("Rolling stock (" +toString()+") is removed from train ("+_train.getName()+") by reset");
    		setTrain(null);
    		setDestination(null, null);
    	}
    	if (e.getPropertyName().equals(CarRoads.CARROADS_NAME_CHANGED_PROPERTY)){
    		if (e.getOldValue().equals(getRoad())){
    			if (log.isDebugEnabled()) log.debug("Rolling stock (" +toString()+") sees road name change from "+e.getOldValue()+" to "+e.getNewValue());
    			setRoad((String)e.getNewValue());
    		}
    	}
    	if (e.getPropertyName().equals(CarOwners.CAROWNERS_NAME_CHANGED_PROPERTY)){
    		if (e.getOldValue().equals(getOwner())){
    			if (log.isDebugEnabled()) log.debug("Rolling stock (" +toString()+") sees owner name change from "+e.getOldValue()+" to "+e.getNewValue());
    			setOwner((String)e.getNewValue());
    		}
    	}
    	if (e.getPropertyName().equals(CarColors.CARCOLORS_NAME_CHANGED_PROPERTY)){
    		if (e.getOldValue().equals(getColor())){
    			if (log.isDebugEnabled()) log.debug("Rolling stock (" +toString()+") sees color name change from "+e.getOldValue()+" to "+e.getNewValue());
    			setColor((String)e.getNewValue());
    		}
    	}
    }

	java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(
			this);

	public synchronized void addPropertyChangeListener(
			java.beans.PropertyChangeListener l) {
		pcs.addPropertyChangeListener(l);
	}

	public synchronized void removePropertyChangeListener(
			java.beans.PropertyChangeListener l) {
		pcs.removePropertyChangeListener(l);
	}

	protected void firePropertyChange(String p, Object old, Object n) {
		pcs.firePropertyChange(p, old, n);
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(RollingStock.class.getName());

}
