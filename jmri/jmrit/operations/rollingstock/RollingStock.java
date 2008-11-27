package jmri.jmrit.operations.rollingstock;

import java.beans.PropertyChangeEvent;
import java.util.List;

import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Setup;

import org.jdom.Element;

/**
 * Represents rolling stock, both powered (engines) and not powered (cars) on
 * the layout.
 * 
 * @author Daniel Boudreau
 * @version $Revision: 1.4 $
 */
public class RollingStock implements java.beans.PropertyChangeListener{

	protected String _id = "";
	protected String _number = "";
	protected String _road = "";
	protected String _type = "";
	protected String _length = "";
	protected String _color = "";
	protected String _weight = "";
	protected String _weightTons = "";
	protected String _built = "";
	protected String _owner = "";
	protected String _comment = "";
	protected String _routeId = "";  		// saved route for interchange tracks
	
	protected Location _location = null;
	protected Track _trackLocation = null;
	protected Location _destination = null;
	protected Track _trackDestination = null;
	protected Train _train = null;
	protected RouteLocation _routeLocation = null;
	protected RouteLocation _routeDestination = null;
	protected int _moves = 0;
	
	public static final String OKAY = "Okay";			// return status when placing rolling stock at a location
	public static final String LENGTH = "length";
	public static final String TYPE = "type";
	public static final String ROAD = "road";
	
	public static final String LOCATION = "rolling stock location";  		// property change descriptions
	public static final String TRACK = "rolling stock track location";
	public static final String DESTINATION = "rolling stock destination";
	public static final String DESTINATIONTRACK = "rolling stock track destination";
	
	public static final int COUPLER = 4;		// draw bar length between rolling stocks

	LocationManager locationManager = LocationManager.instance();
	
	public RollingStock(){
		
	}
	
	public RollingStock(String road, String number) {
		log.debug("New rolling stock " + road + " " + number);
		_road = road;
		_number = number;
		_id = createId(road, number);
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

	public void setWeight(String weight) {
		String old = _weight;
		_weight = weight;
		if (!old.equals(weight))
			firePropertyChange("rolling stock weight", old, weight);
	}

	public String getWeight() {
		return _weight;
	}
	
	public void setWeightTons(String weight) {
		String old = _weight;
		_weightTons = weight;
		if (!old.equals(weight))
			firePropertyChange("rolling stock weight tons", old, weight);
	}

	public String getWeightTons() {
		if (!_weightTons.equals(""))
			return _weightTons;
		else {
			double weight = 0;
			try{
				weight = Double.parseDouble(getWeight());
			}catch (Exception e){
				// log.debug("Weight not set for rolling stock ("+getId()+")");
			}
			return Integer.toString((int)(weight*Setup.getScaleTonRatio()));
		}
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
	
	public Location getLocation() {
		return _location;
	}
	
	public String getLocationName() {
		if (_location != null)
			return _location.getName();
		else
			return "";
	}
	
	public String getLocationId() {
		if (_location != null)
			return _location.getId();
		else
			return "";
	}
	
	public Track getTrack() {
		return _trackLocation;
	}
	
	public String getTrackName() {
		if (_trackLocation != null)
			return _trackLocation.getName();
		else
			return "";
	}
	
	public String getTrackId() {
		if (_trackLocation != null)
			return _trackLocation.getId();
		else
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
	public String setLocation(Location location, Track track) {
		// first determine if rolling stock can be move to the new location
		if (location != null && track != null && !location.acceptsTypeName(getType())){
			log.debug("Can't set (" + getId() + ") type (" +getType()+ ") at location: "+ location.getName() + " wrong type");
			return TYPE;
		}
		if (track != null && !track.acceptsTypeName(getType())){
			log.debug("Can't set (" + getId() + ") type (" +getType()+ ") at track location: "+ location.getName() + " " + track.getName() + " wrong type");
			return TYPE;
		}
		if (track != null && !track.acceptsRoadName(getRoad())){
			log.debug("Can't set (" + getId() + ") road (" +getRoad()+ ") at track location: "+ location.getName() + " " + track.getName() + " wrong road");
			return ROAD;
		}
		// now determine if there's enough space for the rolling stock
		try{
			Integer.parseInt(getLength());
		} catch (Exception e){
			return LENGTH;
		}
		if (track != null && _trackLocation != track &&
				(track.getUsedLength() - track.getReserved() + Integer.parseInt(getLength()) + COUPLER) > track.getLength()){
			log.debug("Can't set (" + getId() + ") at track location ("+ location.getName() + ", " + track.getName() + ") no room!");
			return LENGTH;	
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
						oldTrack.deletePickupRS();
					}
				}
			}
			if (_location != null) {
				_location.addRS(this);
				//	Need to know if location name changes so we can forward to listerners 
				_location.addPropertyChangeListener(this);
			} 
			if (_trackLocation != null){
				_trackLocation.addRS(this);
				//	Need to know if location name changes so we can forward to listerners 
				_trackLocation.addPropertyChangeListener(this);
				// if there's a destination then there's a pickup
				if (_destination != null){
					_location.addPickupRS();
					_trackLocation.addPickupRS();
				}
			} 
			firePropertyChange(LOCATION, oldLocation, location);
			firePropertyChange(TRACK, oldTrack, track);
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
		// first determine if rolling stock can be move to the new destination
		String status = RsTestDestination(destination, track);
		if (!status.equals(OKAY)){
			return status;
		}
		// now set the rolling stock destination!	
		Location oldDestination = _destination;
		_destination = destination;
		Track oldTrack = _trackDestination;
		_trackDestination = track;

		if (oldDestination != (destination) || oldTrack != track) {
			if (oldDestination != null){
				oldDestination.deleteDropRS();
				oldDestination.removePropertyChangeListener(this);
				// delete pickup in case destination is null
				if(_location != null){
					_location.deletePickupRS();
					if (_trackLocation != null)
						_trackLocation.deletePickupRS();
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
					_trackLocation.addPickupRS();
				}
			
				// Need to know if destination name changes so we can forward to listerners 
				_destination.addPropertyChangeListener(this);
			} 
			if (_trackDestination != null){
				_trackDestination.addDropRS(this);
				// Need to know if destination name changes so we can forward to listerners 
				_trackDestination.addPropertyChangeListener(this);
			} else {
				// rolling stock has been terminated bump rolling stock moves
				setMoves(++_moves);
				if (getTrain() != null && getTrain().getRoute() != null)
					setSavedRouteId(getTrain().getRoute().getId());
				setRouteLocation(null);
				setRouteDestination(null);
			}
			firePropertyChange(DESTINATION, oldDestination, destination);
			firePropertyChange(DESTINATIONTRACK, oldTrack, track);
		}
		return status;
	}
	
	public String testDestination(Location destination, Track track) {
		return RsTestDestination(destination, track);
	}
	
	private String RsTestDestination(Location destination, Track track) {
		// first determine if rolling stock can be move to the new destination
		if (destination != null && !destination.acceptsTypeName(getType())){
			log.debug("Can't set (" + getId() + ") type (" +getType()+ ") at destination ("+ destination.getName() + ") wrong type");
			return TYPE;
		}
		if (track != null && !track.acceptsTypeName(getType())){
			log.debug("Can't set (" + getId() + ") type (" +getType()+ ") at track destination ("+ destination.getName() + ", " +track.getName() + ") wrong type");
			return TYPE;
		}
		if (track != null && !track.acceptsRoadName(getRoad())){
			log.debug("Can't set (" + getId() + ") road (" +getRoad()+ ") at track location ("+ destination.getName() + ", " + track.getName() + ") wrong road");
			return ROAD;
		}
		// now determine if there's enough space for the rolling stock
		int length = 0;
		try {
			length = Integer.parseInt(getLength())+ COUPLER;
		} catch (Exception e){
			return LENGTH;
		}
		
		if (track != null &&
				track.getUsedLength() + track.getReserved()+ length > track.getLength()){
			log.debug("Can't set (" + getId() + ") at track destination ("+ destination.getName() + ", " + track.getName() + ") no room!");
			return LENGTH;	
		}
		return OKAY;
	}

	public Location getDestination() {
		return _destination;
	}
	
	public String getDestinationName() {
		if (_destination != null)
			return _destination.getName();
		else
			return "";
	}
	
	public String getDestinationId() {
		if (_destination != null)
			return _destination.getId();
		else
			return "";
	}

	public Track getDestinationTrack() {
		return _trackDestination;
	}
	
	public String getDestinationTrackName() {
		if (_trackDestination != null)
			return _trackDestination.getName();
		else
			return "";
	}
	
	public String getDestinationTrackId() {
		if (_trackDestination != null)
			return _trackDestination.getId();
		else
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
			firePropertyChange("rolling stock train", old, train);
		}
	}

	public Train getTrain() {
		return _train;
	}
	
	public void setRouteLocation (RouteLocation routeLocation){
		if(_location == null){
			log.debug("WARNING rolling stock ("+getId()+") does not have an assigned location");
		}
		else if(routeLocation != null && _location != null && !routeLocation.getName().equals(_location.getName()))
			log.debug("WARNING route location name("+routeLocation.getName()+") not equal to location name ("+_location.getName()+") for rolling stock ("+getId()+")" );
		_routeLocation = routeLocation;
	}
	
	public RouteLocation getRouteLocation(){
		return _routeLocation;
	}
	
	public String getRouteLocationId(){
		if(_routeLocation != null)
			return _routeLocation.getId();
		else
			return "";
	}
	
	public String getSavedRouteId(){
		return _routeId;
	}
	
	public void setSavedRouteId(String id){
		_routeId = id;
	}
	
	public void setRouteDestination (RouteLocation routeDestination){
		if(routeDestination != null && _destination != null && !routeDestination.getName().equals(_destination.getName()))
			log.debug("WARNING route destination name ("+routeDestination.getName()+") not equal to destination name ("+_destination.getName()+") for rolling stock ("+getId()+")" );
		_routeDestination = routeDestination;
	}
	
	public RouteLocation getRouteDestination(){
		return _routeDestination;
	}
	
	public String getRouteDestinationId(){
		if(_routeDestination != null)
			return _routeDestination.getId();
		else
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

	public void setComment(String comment) {
		_comment = comment;
	}

	public String getComment() {
		return _comment;
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
			_weightTons = a.getValue();
		if ((a = e.getAttribute("built")) != null)
			_built = a.getValue();
		Location location = null;
		Track track = null;
		if ((a = e.getAttribute("locationId")) != null)
			location = locationManager.getLocationById(a.getValue());
		if ((a = e.getAttribute("secLocationId")) != null && location != null)
			track = location.getTrackById(a.getValue());
		setLocation(location, track);
		Location destination = null;
		Track trackDestination = null;
		if ((a = e.getAttribute("destinationId")) != null)
			destination = locationManager.getLocationById(a.getValue());
		if ((a = e.getAttribute("secDestinationId")) != null  && destination != null)
			trackDestination = destination.getTrackById(a.getValue());
		setDestination(destination, trackDestination);
		if ((a = e.getAttribute("moves")) != null)
			_moves = Integer.parseInt(a.getValue());
		if ((a = e.getAttribute("train")) != null){
			_train = TrainManager.instance().getTrainByName(a.getValue());
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
		if(!getWeight().equals(""))
			e.setAttribute("weight", getWeight());
		if (!getWeightTons().equals("0"))
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
			e.setAttribute("secLocation", getTrackName());
			e.setAttribute("destination", getDestinationName());
			e.setAttribute("secDestination", getDestinationTrackName());
		}
		e.setAttribute("moves", Integer.toString(getMoves()));
		if (getTrain() != null)
			e.setAttribute("train",	getTrain().getName());
		if (!getOwner().equals(""))
			e.setAttribute("owner", getOwner());
		if (!getComment().equals("") )
			e.setAttribute("comment", getComment());
		return e;
	}
	
	// rolling stock listens for changes in a location name or if a location is deleted
    public void propertyChange(PropertyChangeEvent e) {
    	// if (log.isDebugEnabled()) log.debug("Property change for rolling stock: " + getId()+ " property name: " +e.getPropertyName()+ " old: "+e.getOldValue()+ " new: "+e.getNewValue());
    	// notify if track location name changes
    	if (e.getPropertyName().equals("name")){
        	if (log.isDebugEnabled()) log.debug("Property change for rolling stock: " + getId()+ " property name: " +e.getPropertyName()+ " old: "+e.getOldValue()+ " new: "+e.getNewValue());
    		firePropertyChange(e.getPropertyName(), e.getOldValue(), e.getNewValue());
    	}
    	if (e.getPropertyName().equals(Location.DISPOSE_CHANGED_PROPERTY)){
    	    if (e.getSource() == _location){
        	   	if (log.isDebugEnabled()) log.debug("delete location for rolling stock: " + getId());
    	    	setLocation(null, null);
    	    }
    	    if (e.getSource() == _destination){
        	   	if (log.isDebugEnabled()) log.debug("delete destination for rolling stock: " + getId());
    	    	setDestination(null, null);
    	    }
     	}
    	if (e.getPropertyName().equals(Track.DISPOSE_CHANGED_PROPERTY)){
    	    if (e.getSource() == _trackLocation){
        	   	if (log.isDebugEnabled()) log.debug("delete location for rolling stock: " + getId());
    	    	setLocation(_location, null);
    	    }
    	    if (e.getSource() == _trackDestination){
        	   	if (log.isDebugEnabled()) log.debug("delete destination for rolling stock: " + getId());
    	    	setDestination(_destination, null);
    	    }
    	    	
    	}
    	if (e.getPropertyName().equals(Train.DISPOSE_CHANGED_PROPERTY)){
    		if (e.getSource() == _train){
        	   	if (log.isDebugEnabled()) log.debug("delete train for rolling stock: " + getId());
        	   	setTrain(null);
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

	static org.apache.log4j.Category log = org.apache.log4j.Category
			.getInstance(RollingStock.class.getName());

}
