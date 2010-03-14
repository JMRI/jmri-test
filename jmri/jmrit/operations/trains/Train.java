// Train.java

package jmri.jmrit.operations.trains;

import java.awt.Frame;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.ResourceBundle;

import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.rollingstock.engines.EngineTypes;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.routes.RouteManager;
import jmri.jmrit.operations.routes.RouteManagerXml;
import jmri.jmrit.operations.locations.LocationManager;

import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;

import jmri.util.davidflanagan.HardcopyWriter;
import jmri.jmrit.display.PanelMenu;
import jmri.jmrit.display.Editor;


/**
 * Represents a train on the layout
 * 
 * @author Daniel Boudreau Copyright (C) 2008, 2009
 * @version $Revision: 1.67 $
 */
public class Train implements java.beans.PropertyChangeListener {
	
	// WARNING DO NOT LOAD CAR OR ENGINE MANAGERS WHEN Train.java IS CREATED
	// IT CAUSES A RECURSIVE LOOP AT LOAD TIME, SEE EXAMPLES BELOW
	// CarManager carManager = CarManager.instance();
	// EngineManager engineManager = EngineManager.instance();
	
	static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.trains.JmritOperationsTrainsBundle");

	protected String _id = "";
	protected String _name = "";		
	protected String _description = "";
	protected RouteLocation _current = null;// where the train is located in its route
	protected String _status = "";
	protected boolean _built = false;		// when true, a train manifest has been built
	protected boolean _build = true;		// when true, build this train
	protected boolean _buildFailed = false;	// when true, build for this train failed
	protected boolean _printed = false;		// when true, manifest has been printed
	protected Route _route = null;
	protected String _roadOption = ALLROADS;// train road name restrictions
	protected int _requires = 0;			// train requirements, caboose, fred
	protected String _numberEngines = "0";	// number of engines this train requires
	protected String _engineRoad = "";		// required road name for engines assigned to this train 
	protected String _engineModel = "";		// required model of engines assigned to this train
	protected String _cabooseRoad = "";		// required road name for cabooses assigned to this train
	protected Calendar _departureTime = Calendar.getInstance();	// departure time for this train
	protected String _leadEngineId ="";		// lead engine for train icon info
	protected String _builtStartYear = "";	// built start year 
	protected String _builtEndYear = "";	// built end year
	protected String _ownerOption = ALLOWNERS;// train road name restrictions
	
	
	protected String _comment = "";
	
	// property change names
	public static final String DISPOSE_CHANGED_PROPERTY = "TrainDispose";
	public static final String STOPS_CHANGED_PROPERTY = "TrainStops";
	public static final String TYPES_CHANGED_PROPERTY = "TrainTypes";
	public static final String ROADS_CHANGED_PROPERTY = "TrainRoads";
	public static final String OWNERS_CHANGED_PROPERTY = "TrainOwners";
	public static final String NAME_CHANGED_PROPERTY = "TrainName";
	public static final String DESCRIPTION_CHANGED_PROPERTY = "TrainDescription";
	public static final String STATUS_CHANGED_PROPERTY = "TrainStatus";
	public static final String DEPARTURETIME_CHANGED_PROPERTY = "TrainDepartureTime";
	public static final String TRAIN_LOCATION_CHANGED_PROPERTY = "TrainLocation";
	
	// Train status
	public static final String TERMINATED = rb.getString("Terminated");
	public static final String TRAINRESET = rb.getString("TrainReset");
	private static final String TRAININROUTE = rb.getString("TrainInRoute");
	
	public static final int NONE = 0;		// train requirements
	public static final int CABOOSE = 1;
	public static final int FRED = 2;
	
	// road options
	public static final String ALLROADS = rb.getString("All");			// train services all road names 
	public static final String INCLUDEROADS = rb.getString("Include");
	public static final String EXCLUDEROADS = rb.getString("Exclude");
	
	// owner options
	public static final String ALLOWNERS = rb.getString("All");			// train services all road names 
	public static final String INCLUDEOWNERS = rb.getString("Include");
	public static final String EXCLUDEOWNERS = rb.getString("Exclude");

	
	public static final String AUTO = rb.getString("Auto");				// how engines are assigned to this train
	
	public Train(String id, String name) {
		log.debug("New train " + name + " " + id);
		_name = name;
		_id = id;
		// a new train accepts all types
		setTypeNames(CarTypes.instance().getNames());
		setTypeNames(EngineTypes.instance().getNames());
	}

	public String getId() {
		return _id;
	}

	public void setName(String name) {
		String old = _name;
		_name = name;
		if (!old.equals(name)){
			firePropertyChange(NAME_CHANGED_PROPERTY, old, name);
		}
	}
	
	// for combo boxes
	public String toString(){
		return _name;
	}

	public String getName() {
		return _name;
	}
	
	/**
	 * Get's trains departure time
	 * @return train's departure time in the String format hh:mm
	 */
	public String getDepartureTime(){
		int hour = _departureTime.get(Calendar.HOUR_OF_DAY);
		int minute = _departureTime.get(Calendar.MINUTE);
		String h = Integer.toString(hour);
		if (hour < 10)
			h = "0"+h;
		if (minute < 10)
			return h+":0"+minute;
		return h+":"+minute;
	}
	
	/**
	 * Get train's departure time in minutes from midnight for sorting
	 * @return int hh*60+mm
	 */
	public int getDepartTimeMinutes(){
		int hour = _departureTime.get(Calendar.HOUR_OF_DAY);
		int minute = _departureTime.get(Calendar.MINUTE);
		return (hour*60)+minute;
	}
	
	public void setDepartureTime(String hour, String minute){
		String oldHour = getDepartureTimeHour();
		String oldMinute = getDepartureTimeMinute();
		// new departure time?
		if (Integer.parseInt(hour) == Integer.parseInt(oldHour) &&
				Integer.parseInt(minute) == Integer.parseInt(oldMinute))
			return;
		_departureTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hour));
		_departureTime.set(Calendar.MINUTE, Integer.parseInt(minute));
		firePropertyChange(DEPARTURETIME_CHANGED_PROPERTY, oldHour+":"+oldMinute, hour+":"+minute);
	}
	
	public String getDepartureTimeHour(){
		int hour = _departureTime.get(Calendar.HOUR_OF_DAY);
		if (hour<10)
			return "0"+Integer.toString(hour);
		return Integer.toString(hour);
	}
	
	public String getDepartureTimeMinute(){
		int minute = _departureTime.get(Calendar.MINUTE);
		if (minute<10)
			return "0"+Integer.toString(minute);
		return Integer.toString(minute);
	}
	
	/**
	 * Gets the expected time when this train will arrive at
	 * the location rl.  Expected arrival time is based on the
	 * number of car pickup and drops for this train.
	 * TODO Doesn't provide correct expected arrival time if train
	 * is in route, instead provides relative time.
	 * @return expected arrival time
	 */
	public String getExpectedArrivalTime(RouteLocation routeLocation){
		int carPickups = 0;
		int carDrops = 0;
		int numberOfLocations = 0;
		boolean trainAt = false;
		CarManager carManager = CarManager.instance();
		List<String> carList = carManager.getCarsByTrainList(this);
		if (getRoute() != null){
			List<String> routeList = getRoute().getLocationsBySequenceList();
			for (int i=0; i<routeList.size(); i++){
				RouteLocation rl = getRoute().getLocationById(routeList.get(i));
				if (rl == routeLocation)
					break; // done
				if (rl == getCurrentLocation())
					trainAt = true;
				if (trainAt)
					numberOfLocations++;
				if (i == 0)
					continue; // don't count work at departure
				for (int j=0; j<carList.size(); j++){
					Car car = carManager.getCarById(carList.get(j));
					if (car.getRouteLocation() == rl && !car.getTrackName().equals("")){
						carPickups++;
					}
					if (car.getRouteDestination() == rl){
						carDrops++;
					}
				}
			}
		}
		log.debug("Calculate arrival time for train (" +getName()+ ") at ("+routeLocation.getName()+"), "+numberOfLocations+" locations and a total " 
				+carPickups+ " pickups and "+carDrops+ " drops");
		// TODO use fast clock to get current time vs departure time
		// for now use relative
		int minutes = numberOfLocations*Setup.getTravelTime() + carPickups*Setup.getSwitchTime() + carDrops*Setup.getSwitchTime(); 
		int hours = 0;
		int days = 0;
		
		if (!isTrainInRoute()){
			minutes += _departureTime.get(Calendar.MINUTE); 
			hours = _departureTime.get(Calendar.HOUR_OF_DAY);
		}
		
		if (minutes >= 60){
			int h = minutes/60;
			minutes = minutes-h*60;
			hours += h;
		}
		
		String d = "";
		if (hours >= 24){
			int nd = hours/24;
			hours = hours-nd*24;
			days += nd;
			d = Integer.toString(days)+":";
		}
		
		String h = Integer.toString(hours);
		if (hours < 10)
			h = "0"+h;
		if (minutes < 10)
			return d+h+":0"+minutes;
		return d+h+":"+minutes;
	}
	
	
	/**
	 * Set train requirements
	 * @param requires NONE CABOOSE FRED
	 */	
	public void setRequirements(int requires){
		int old = _requires;
		_requires = requires;
		if (old != requires)
			firePropertyChange("requires", Integer.toString(old), Integer.toString(requires));
	}
	
	public int getRequirements(){
		return _requires;
	}
	
	public void setRoute(Route route) {
		Route old = _route;
		String oldRoute = "";
		String newRoute = "";
		if (old != null){
			old.removePropertyChangeListener(this);
			oldRoute = old.toString();
		}
		if (route != null){
			route.addPropertyChangeListener(this);
			newRoute = route.toString();
		}
		_route = route;
		_skipLocationsList.clear();
		if (old == null || !old.equals(route)){
			firePropertyChange("route", oldRoute, newRoute);
		}
	}
	
	public Route getRoute() {
		return _route;
	}
	
	public String getTrainRouteName (){
    	if (_route == null)
    		return "";
    	return _route.getName();
    }
    
	/**
	 * 
	 * @return train's departure location's name
	 */
	public String getTrainDepartsName(){
    	if (getTrainDepartsRouteLocation() != null ){
     		return getTrainDepartsRouteLocation().getName();
     	}
    	return "";
    }
	
	protected RouteLocation getTrainDepartsRouteLocation(){
		if (_route == null){
			return null;
		}
		List<String> list = _route.getLocationsBySequenceList();
		if (list.size()>0){
			RouteLocation rl = _route.getLocationById(list.get(0));
			return rl;
		}
		return null;
	}

	public String getTrainTerminatesName(){
		if (getTrainTerminatesRouteLocation() != null){
			return getTrainTerminatesRouteLocation().getName();
		}
		return "";
	}
	
	protected RouteLocation getTrainTerminatesRouteLocation(){
		if (_route == null){
			return null;
		}
		List<String> list = _route.getLocationsBySequenceList();
		if (list.size()>0){
			RouteLocation rl = _route.getLocationById(list.get(list.size()-1));
			return rl;
		}
		return null;
	}

	/**
	 * Set train's current route location
	 */
	public void setCurrentLocation(RouteLocation location) {
		RouteLocation old = _current;
		_current = location;
		if (old == null || !old.equals(location)){
			firePropertyChange("current", old, location);
		}
	}

	/**
	 * @return Train's current route location name
	 */
	public String getCurrentLocationName() {
		if (getCurrentLocation() == null)
			return "";
		return getCurrentLocation().getName();
	}
	
	/**
	 * @return Train's current route location
	 */
	public RouteLocation getCurrentLocation(){
		if (getRoute() == null)
			return null;
		if (_current == null)
			return null;
		// this will verify that the current location still exists
		return getRoute().getLocationById(_current.getId());
	}
	
	/**
	 * @return Train's next route location name
	 */
	public String getNextLocationName(){
		List<String> routeList = getRoute().getLocationsBySequenceList();
		for (int i=0; i<routeList.size(); i++){
			RouteLocation rl = getRoute().getLocationById(routeList.get(i));
			if (rl == getCurrentLocation()){
				i++;
				if (i < routeList.size()){
					rl = getRoute().getLocationById(routeList.get(i));
					return rl.getName();
				} 
				break;
			}
		}
		return getCurrentLocationName();	// At end of route
	}
	
	// Train status
	public void setStatus(String status) {
		String old = _status;
		_status = status;
		if (!old.equals(status)){
			firePropertyChange(STATUS_CHANGED_PROPERTY, old, status);
		}
	}
	
	/**
	 * 
	 * @return Train's status
	 */
	public String getStatus() {
		return _status;
	}
	
	/**
	 * 
	 * @return true if train is in route
	 */
	public boolean isTrainInRoute() {
		return getCurrentLocationName() != "" && getTrainDepartsRouteLocation() != getCurrentLocation();
	}

	List<String> _skipLocationsList = new ArrayList<String>();

	private String[] getTrainSkipsLocations(){
		String[] locationIds = new String[_skipLocationsList.size()];
		for (int i=0; i<_skipLocationsList.size(); i++)
			locationIds[i] = _skipLocationsList.get(i);
		return locationIds;
	}

	private void setTrainSkipsLocations(String[] locationIds){
		if (locationIds.length == 0) return;
		jmri.util.StringUtil.sort(locationIds);
		for (int i=0; i<locationIds.length; i++)
			_skipLocationsList.add(locationIds[i]);
	}

	/**
	 * Train will skip the RouteLocation
	 * @param locationId RouteLocation Id
	 */
	public void addTrainSkipsLocation(String locationId){
		// insert at start of _skipLocationsList, sort later
		if (_skipLocationsList.contains(locationId))
			return;
		_skipLocationsList.add(0,locationId);
		log.debug("train does not stop at "+locationId);
		firePropertyChange (STOPS_CHANGED_PROPERTY, _skipLocationsList.size()-1, _skipLocationsList.size());
	}

	public void deleteTrainSkipsLocation(String locationId){
		_skipLocationsList.remove(locationId);
		log.debug("train will stop at "+locationId);
		firePropertyChange (STOPS_CHANGED_PROPERTY, _skipLocationsList.size()+1, _skipLocationsList.size());
	}

	public boolean skipsLocation(String locationId){
		return _skipLocationsList.contains(locationId);
	}

    List<String> _typeList = new ArrayList<String>();
    public String[] getTypeNames(){
      	String[] types = new String[_typeList.size()];
     	for (int i=0; i<_typeList.size(); i++)
     		types[i] = _typeList.get(i);
   		return types;
    }
    
    /**
     * Set the type of cars this will service, see types in
     * Cars.
     * @param types 
     */
    public void setTypeNames(String[] types){
    	if (types.length == 0) return;
    	jmri.util.StringUtil.sort(types);
 		for (int i=0; i<types.length; i++)
 			_typeList.add(types[i]);
    }
    
    public void addTypeName(String type){
    	// insert at start of list, sort later
    	if (_typeList.contains(type))
    		return;
    	_typeList.add(0,type);
    	log.debug("train add car type "+type);
    	firePropertyChange (TYPES_CHANGED_PROPERTY, _typeList.size()-1, _typeList.size());
    }
    
    public void deleteTypeName(String type){
       	if (!_typeList.contains(type))
    		return;
    	_typeList.remove(type);
    	log.debug("train delete car type "+type);
     	firePropertyChange (TYPES_CHANGED_PROPERTY, _typeList.size()+1, _typeList.size());
     }
    
    public boolean acceptsTypeName(String type){
    	return _typeList.contains(type);
    }
    
	public String getRoadOption (){
    	return _roadOption;
    }
    
 	/**
 	 * Set how this train deals with car road names
 	 * @param option ALLROADS INCLUDEROADS EXCLUDEROADS
 	 */
    public void setRoadOption (String option){
    	_roadOption = option;
    }

    List<String> _roadList = new ArrayList<String>();
    private void setRoadNames(String[] roads){
    	if (roads.length == 0) return;
    	jmri.util.StringUtil.sort(roads);
 		for (int i=0; i<roads.length; i++){
 			if (!roads[i].equals(""))
 				_roadList.add(roads[i]);
 		}
    }
    
    /**
     * Provides a list of road names that the train will
     * either service or exclude.  See setRoadOption
     * @return Array of road names as Strings
     */
    public String[] getRoadNames(){
      	String[] roads = new String[_roadList.size()];
     	for (int i=0; i<_roadList.size(); i++)
     		roads[i] = _roadList.get(i);
     	if (_roadList.size() == 0)
     		return roads;
     	jmri.util.StringUtil.sort(roads);
   		return roads;
    }
    
    /**
     * Add a road name that the train will
     * either service or exclude.  See setRoadOption
     * @return true if road name was added, false if road name
     * wasn't in the list.
     */
    public boolean addRoadName(String road){
     	if (_roadList.contains(road))
    		return false;
    	_roadList.add(road);
    	log.debug("train (" +getName()+ ") add car road "+road);
    	firePropertyChange (ROADS_CHANGED_PROPERTY, _roadList.size()-1, _roadList.size());
    	return true;
    }
    
    /**
     * Delete a road name that the train will
     * either service or exclude.  See setRoadOption
     * @return true if road name was removed, false if road name
     * wasn't in the list.
     */
    public boolean deleteRoadName(String road){
     	if (!_roadList.contains(road))
    		return false;
    	_roadList.remove(road);
    	log.debug("train (" +getName()+ ") delete car road "+road);
    	firePropertyChange (ROADS_CHANGED_PROPERTY, _roadList.size()+1, _roadList.size());
       	return true;
    }
    
    /**
     * Determine if train will service a specific road name.
     * @param road the road name to check.
     * @return true if train will service this road name.
     */
    public boolean acceptsRoadName(String road){
    	if (_roadOption.equals(ALLROADS)){
    		return true;
    	}
       	if (_roadOption.equals(INCLUDEROADS)){
       		return _roadList.contains(road);
       	}
       	// exclude!
       	return !_roadList.contains(road);
    }
    
	public String getOwnerOption (){
    	return _ownerOption;
    }
    
 	/**
 	 * Set how this train deals with car owner names
 	 * @param option ALLOWNERS INCLUDEOWNERS EXCLUDEOWNERS
 	 */
    public void setOwnerOption (String option){
    	_ownerOption = option;
    }

    List<String> _ownerList = new ArrayList<String>();
    private void setOwnerNames(String[] owners){
    	if (owners.length == 0) return;
    	jmri.util.StringUtil.sort(owners);
 		for (int i=0; i<owners.length; i++){
 			if (!owners[i].equals(""))
 				_ownerList.add(owners[i]);
 		}
    }
    
    /**
     * Provides a list of owner names that the train will
     * either service or exclude.  See setOwnerOption
     * @return Array of owner names as Strings
     */
    public String[] getOwnerNames(){
      	String[] owners = new String[_ownerList.size()];
     	for (int i=0; i<_ownerList.size(); i++)
     		owners[i] = _ownerList.get(i);
     	if (_ownerList.size() == 0)
     		return owners;
     	jmri.util.StringUtil.sort(owners);
   		return owners;
    }
    
    /**
     * Add a owner name that the train will
     * either service or exclude.  See setOwnerOption
     * @return true if owner name was added, false if owner name
     * wasn't in the list.
     */
    public boolean addOwnerName(String owner){
     	if (_ownerList.contains(owner))
    		return false;
    	_ownerList.add(owner);
    	log.debug("train (" +getName()+ ") add car owner "+owner);
    	firePropertyChange (OWNERS_CHANGED_PROPERTY, _ownerList.size()-1, _ownerList.size());
    	return true;
    }
    
    /**
     * Delete a owner name that the train will
     * either service or exclude.  See setOwnerOption
     * @return true if owner name was removed, false if owner name
     * wasn't in the list.
     */
    public boolean deleteOwnerName(String owner){
     	if (!_ownerList.contains(owner))
    		return false;
    	_ownerList.remove(owner);
    	log.debug("train (" +getName()+ ") delete car owner "+owner);
    	firePropertyChange (OWNERS_CHANGED_PROPERTY, _ownerList.size()+1, _ownerList.size());
       	return true;
    }
    
    /**
     * Determine if train will service a specific owner name.
     * @param owner the owner name to check.
     * @return true if train will service this owner name.
     */
    public boolean acceptsOwnerName(String owner){
    	if (_ownerOption.equals(ALLOWNERS)){
    		return true;
    	}
       	if (_ownerOption.equals(INCLUDEOWNERS)){
       		return _ownerList.contains(owner);
       	}
       	// exclude!
       	return !_ownerList.contains(owner);
    }
    
    /**
     * Only rolling stock built in or after this year will be used.
     * @param year
     */
    public void setBuiltStartYear(String year){
    	_builtStartYear = year;
    }
    
    public String getBuiltStartYear(){
    	return _builtStartYear;
    }
    
    /**
     * Only rolling stock built in or before this year will be used.
     * @param year
     */
    public void setBuiltEndYear(String year){
    	_builtEndYear = year;
    }
    
    public String getBuiltEndYear(){
    	return _builtEndYear;
    }
    
    /**
     * Determine if train will service rolling stock by built date. 
     * @param date
     * @return true is built date is in the acceptable range.
     */
    public boolean acceptsBuiltDate(String date){
    	if(getBuiltStartYear().equals("") && getBuiltEndYear().equals(""))
    		return true;	// range dates not defined
    	int s = 0;		// default start year;
    	int e = 99999;	// default end year;
    	try{
       		s = Integer.parseInt(getBuiltStartYear());
    	} catch (NumberFormatException e1){
    		log.debug("Built start date not initialized, start: "+getBuiltStartYear());
    	}
    	try{
    		e = Integer.parseInt(getBuiltEndYear());
    	} catch (NumberFormatException e1){
    		log.debug("Built end date not initialized, end: "+getBuiltEndYear());
    	}  	
    	try{
    		int d = Integer.parseInt(date);
    		if (s<d && d<e)
    			return true;
    		else
    			return false;
    	} catch (NumberFormatException e1){
    		log.debug("date: "+date+" isn't an integer");
    		return false;
    	}  	
    }
    
    /**
     * 
     * @return The number of cars worked by this train
     */
    public int getNumberCarsWorked(){
    	List<String> cars = CarManager.instance().getCarsByTrainList(this);
    	// remove cars that haven't been assigned by the train builder
    	for (int i=0; i<cars.size(); i++){
    		Car c = CarManager.instance().getCarById(cars.get(i));
    		if (c.getRouteLocation() == null){
    			cars.remove(i--);
    		}
    	}
    	return cars.size();
    }
    
    /**
     * 
     * @return The number of cars currently in the train
     */
    public int getNumberCarsInTrain(RouteLocation rl){
    	List<String> cars = CarManager.instance().getCarsByTrainList(this);
    	// remove cars that aren't in the train
    	for (int i=0; i<cars.size(); i++){
    		Car c = CarManager.instance().getCarById(cars.get(i));
    		if (c.getRouteLocation() != rl || c.getTrack() != null){
    			cars.remove(i--);
    		}
    	}
    	return cars.size();
    }
    
    public void setDescription(String description) {
		String old = _description;
		_description = description;
		if (!old.equals(description)){
			firePropertyChange(DESCRIPTION_CHANGED_PROPERTY, old, description);
		}
	}
	
	public String getDescription() {
		return _description;
	}
	
	public void setNumberEngines(String number) {
		_numberEngines = number;
	}

	/**
	 * 
	 * @return The number of engines that this train requires.
	 */
	public String getNumberEngines() {
		return _numberEngines;
	}
	
	/**
	 * 
	 * @param road The road name of engines servicing this train.
	 */
	public void setEngineRoad(String road) {
		_engineRoad = road;
	}

	public String getEngineRoad() {
		return _engineRoad;
	}
	
	/**
	 * 
	 * @param model The model name of engines servicing this train.
	 */
	public void setEngineModel(String model) {
		_engineModel = model;
	}

	public String getEngineModel() {
		return _engineModel;
	}
	
	/**
	 * 
	 * @param road The road name of the caboose servicing this train.
	 */
	public void setCabooseRoad(String road) {
		_cabooseRoad = road;
	}

	public String getCabooseRoad() {
		return _cabooseRoad;
	}
	
	public void setComment(String comment) {
		_comment = comment;
	}

	public String getComment() {
		return _comment;
	}
	
	public void setBuilt(boolean built) {
		boolean old = _built;
		_built = built;
		if (old != built){
			firePropertyChange("built", old?"true":"false", built?"true":"false");
		}
	}
	
	/**
	 * 
	 * @return true if the train was successfully built.
	 */
	public boolean getBuilt() {
		return _built;
	}
	
	public void setBuild(boolean build) {
		boolean old = _build;
		_build = build;
		if (old != build){
			firePropertyChange("build", old?"true":"false", build?"true":"false");
		}
	}
	
	public boolean getBuild() {
		return _build;
	}
	
	public boolean buildIfSelected(){
		if(_build && !_built){
			build();
			return true;
		}
		log.debug("Train ("+getName()+") not selected or already built, skipping build");
		return false;
	}

	public void build(){
		reset();
		TrainBuilder tb = new TrainBuilder();
		tb.build(this);
		setPrinted(false);
	}

	public void printBuildReport(){
		File buildFile = TrainManagerXml.instance().getTrainBuildReportFile(getName());
		boolean isPreview = TrainManager.instance().getPrintPreview();
		printReport(buildFile, MessageFormat.format(rb.getString("buildReport"),new Object[]{getDescription()}), isPreview, "", true);
	}
	
	public void setBuildFailed(boolean status) {
		boolean old = _buildFailed;
		_buildFailed = status;
		if (old != status){
			firePropertyChange("buildFailed", old?"true":"false", status?"true":"false");
		}
	}
	
	/**
	 * Returns true if the train build failed. Note that returning false doesn't
	 * mean the build was successful.
	 * 
	 * @return true if train build failed.
	 */
	public boolean getBuildFailed() {
		return _buildFailed;
	}
	
	/**
	 * Print manifest for train if already built.
	 * @return true if print successful.
	 */
	public boolean printManifestIfBuilt(){
		if(_built){
			boolean isPreview = TrainManager.instance().getPrintPreview();
			printManifest(isPreview);
		} else {
			log.debug("Need to build train (" +getName()+ ") before printing manifest");
			return false;
		}
		return true;
	}
	
	/**
	 * Print manifest for train.
	 * @return true if print successful.
	 */
	public boolean printManifest(boolean isPreview){
		File file = TrainManagerXml.instance().getTrainManifestFile(getName());
		if (!file.exists())
			return false;
		printReport(file, "Train Manifest "+getDescription(), isPreview, Setup.getFontName(), false);
		if (!isPreview)
			setPrinted(true);
		return true;
	}
	
	private void setPrinted (boolean printed){
		_printed = printed;
	}
	
	public boolean getPrinted(){
		return _printed;
	}
	
	public static void printReport (File file, String name, boolean isPreview, String fontName, boolean isBuildReport){
	    // obtain a HardcopyWriter to do this
		HardcopyWriter writer = null;
		Frame mFrame = new Frame();
        try {
            writer = new HardcopyWriter(mFrame, name, 10, .5, .5, .5, .5, isPreview);
        } catch (HardcopyWriter.PrintCanceledException ex) {
            log.debug("Print cancelled");
            return;
        }
        // set font
        if (!fontName.equals(""))
        	writer.setFontName(fontName);
        
        // now get the build file to print
		BufferedReader in;
		try {
			in = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			log.debug("Build file doesn't exist");
			return;
		}
		String newLine = "\n";
		String line = " ";

		while (true) {
			try {
				line = in.readLine();
			} catch (IOException e) {
				log.debug("Print read failed");
				break;
			}
			if (line == null)
				break;
			// check for build report print level
			if (isBuildReport) {
				String[] inputLine = line.split("\\s+");
				if (inputLine[0].equals(Setup.BUILD_REPORT_VERY_DETAILED + "-")
						|| inputLine[0].equals(Setup.BUILD_REPORT_DETAILED + "-")
						|| inputLine[0].equals(Setup.BUILD_REPORT_NORMAL + "-")
						|| inputLine[0].equals(Setup.BUILD_REPORT_MINIMAL + "-")) {
		
					if (Setup.getBuildReportLevel().equals(Setup.BUILD_REPORT_MINIMAL)){
						if (inputLine[0].equals(Setup.BUILD_REPORT_NORMAL + "-") 
								|| inputLine[0].equals(Setup.BUILD_REPORT_DETAILED + "-")
								|| inputLine[0].equals(Setup.BUILD_REPORT_VERY_DETAILED + "-")) {
							continue;	// don't print this line
						}
					}
					if (Setup.getBuildReportLevel().equals(Setup.BUILD_REPORT_NORMAL)){
						if (inputLine[0].equals(Setup.BUILD_REPORT_DETAILED + "-")
								|| inputLine[0].equals(Setup.BUILD_REPORT_VERY_DETAILED + "-")){
							continue;	// don't print this line
						}
					}
					if (Setup.getBuildReportLevel().equals(Setup.BUILD_REPORT_DETAILED)){
						if (inputLine[0].equals(Setup.BUILD_REPORT_VERY_DETAILED + "-")){
							continue;	// don't print this line
						}
					}
					// do not indent if false
					int start = 0;
					if (false){
						// indent lines based on level
						if (inputLine[0].equals(Setup.BUILD_REPORT_VERY_DETAILED + "-")){
							inputLine[0] = "   ";
						}
						else if (inputLine[0].equals(Setup.BUILD_REPORT_DETAILED + "-")){
							inputLine[0] = "  ";
						}
						else if (inputLine[0].equals(Setup.BUILD_REPORT_NORMAL + "-")){
							inputLine[0] = " ";
						}
						else if (inputLine[0].equals(Setup.BUILD_REPORT_MINIMAL + "-")){
							inputLine[0] = "";
						}
					} else {
						start = 1;
					}
					// rebuild line
					line = "";
					for (int i = start; i < inputLine.length; i++) {
						line += inputLine[i] + " ";
					}
				} else {
					log.debug("ERROR first characters of build report not valid ("
									+ line + ")");
				}
			// printing the train manifest
			}else{
				Color c = null;
				// determine if line is a pickup or drop
				if(line.startsWith(TrainCommon.BOX + rb.getString("Pickup")) ||
						line.startsWith(TrainCommon.BOX + rb.getString("Engine"))){
					//log.debug("found a pickup line");
					c = Setup.getPickupColor();
				}
				if(line.startsWith(TrainCommon.BOX + rb.getString("Drop"))){
					//log.debug("found a drop line");
					c = Setup.getDropColor();
				}
				if (c != null){
					try {
						writer.write(c, line + newLine);
						continue;
					} catch (IOException e) {
						log.debug("Print write color failed");
						break;
					}
				}
			}
			try {
				writer.write(line + newLine);
			} catch (IOException e) {
				log.debug("Print write failed");
				break;
			}
		}
        // and force completion of the printing
		try {
			in.close();
		} catch (IOException e) {
			log.debug("Print close failed");
		}
        writer.close();
	}
		
	protected RouteLocation trainIconRl = null; // saves the icon current route location

	/**
	 * Sets the panel position for the train icon   
	 * for the current route location.
	 * @return true if train coordinates can be set
	 */
	public boolean setTrainIconCoordinates(){
		if (Setup.isTrainIconCordEnabled()){
			trainIconRl.setTrainIconX(trainIcon.getX());
			trainIconRl.setTrainIconY(trainIcon.getY());
			RouteManagerXml.instance().setDirty(true);
			return true;
		}
		return false;
	}
	
	/**
	 * Terminate train.
	 */
	public void terminate(){
		while(_built)
			move();
	}

	/**
	 * Move train to next location in route.  Will move
	 * engines, cars, and train icon.
	 */
	public void move() {
		log.debug("Move train ("+getName()+")");
		if (getRoute() == null || getCurrentLocation() == null)
			return;
		List<String> routeList = getRoute().getLocationsBySequenceList();
		for (int i = 0; i < routeList.size(); i++) {
			RouteLocation rl = getRoute().getLocationById(routeList.get(i));
			if (getCurrentLocation() == rl) {
				i++;
				RouteLocation rlNew = null;	// use null if end of route
				if (i < routeList.size()) {
					rlNew = getRoute().getLocationById(routeList.get(i));
				} 
				setCurrentLocation(rlNew);				
				moveTrainIcon(rlNew);
				// cars and engines will move via property change
				firePropertyChange(TRAIN_LOCATION_CHANGED_PROPERTY, rl, rlNew);
				updateStatus(rl, rlNew);
				break;
			}
		}
	}
	
	public void loadTrainIcon(){
		if (getCurrentLocation() != null)
			moveTrainIcon(getCurrentLocation());
	}
	
	private boolean animation = true;	// when true use animation for icon moves
	protected void moveTrainIcon(RouteLocation rl){
		trainIconRl = rl;
		// create train icon if at departure or if program has been restarted
		if (rl == getTrainDepartsRouteLocation() || trainIcon == null){
			createTrainIcon();
		}
		if (trainIcon != null && trainIcon.isActive()){
			setTrainIconColor();
			trainIcon.setShowTooltip(true);
            String txt = null;
			if (getCurrentLocationName().equals(""))
				txt= getDescription() + " "+TERMINATED+" ("+ getTrainTerminatesName()+")";
			else
				txt = getDescription() + " at " + getCurrentLocationName() + " next "+getNextLocationName();
            trainIcon.getTooltip().setText(txt);
            trainIcon.getTooltip().setBackgroundColor(Color.white);
			if (rl != null){
				if (rl.getTrainIconX()!=0 || rl.getTrainIconY()!=0){
					if (animation)
						new TrainIconAnimation(trainIcon, rl);
					else
						trainIcon.setLocation(rl.getTrainIconX(), rl.getTrainIconY());
				}
			}
		} 
	}
	
	protected Engine leadEngine = null; 				// lead engine for icon
	public String getIconName(){
		String name = getName();
		if (getBuilt() && getLeadEngine() != null && Setup.isTrainIconAppendEnabled())
			name += " "+ getLeadEngine().getNumber();
		return name;
	}
		
	/**
	 * Gets the lead engine, will create it if the program has been restarted
	 * @return lead engine for this train
	 */
	public Engine getLeadEngine(){
		if (leadEngine == null  && !_leadEngineId.equals("")){
			leadEngine = EngineManager.instance().getEngineById(_leadEngineId);
		}
		return leadEngine;
	}
	
	public void setLeadEngine(Engine engine){
		if (engine == null)
			_leadEngineId = "";
		leadEngine = engine;
	}
	
	protected TrainIcon trainIcon = null;
	private void createTrainIcon() {
		if (trainIcon != null && trainIcon.isActive()) {
			trainIcon.remove();
			trainIcon.dispose();
		}
		Editor editor = PanelMenu.instance().getEditorByName(Setup.getPanelName());
		if (editor != null) { 
            trainIcon = editor.addTrainIcon(getIconName());
			trainIcon.setTrain(this);
			if (getIconName().length() > 9) {
				trainIcon.setFont(jmri.util.FontUtil.deriveFont(trainIcon.getFont(), 8.f));
			}
			// add throttle if there's a throttle manager
			if (jmri.InstanceManager.throttleManagerInstance()!=null) {
				// add throttle if JMRI loco roster entry exist
				RosterEntry entry = null;
				if (getLeadEngine() != null){
					// first try and find a match based on loco road number
					List<RosterEntry> entries = Roster.instance().matchingList(null, getLeadEngine().getNumber(), null, null, null, null, null);
					if (entries.size() > 0){
						entry = entries.get(0);
					}
					if (entry == null){
						// now try finding a match based on DCC address
						entries = Roster.instance().matchingList(null, null, getLeadEngine().getNumber(), null, null, null, null);
						if (entries.size() > 0){
							entry = entries.get(0);
						}
					}
				}
				if (entry != null){
					trainIcon.setRosterEntry(entry);
					if(getLeadEngine().getConsist() != null)
						trainIcon.setConsistNumber(getLeadEngine().getConsist().getConsistNumber());
				} else{
					log.debug("Loco roster entry not found for train ("+getName()+")");
				}
			}
		}
	}

	private void setTrainIconColor(){
		// Terminated train?
		if (getCurrentLocationName().equals("")){
			trainIcon.setLocoColor(Setup.getTrainIconColorTerminate());
			return;
		}
		// local train?
		if (getRoute().getLocationsBySequenceList().size()==1){
			trainIcon.setLocoColor(Setup.getTrainIconColorLocal());
			return;
		}
		// set color based on train direction at current location
		if (trainIconRl.getTrainDirection() == RouteLocation.NORTH)
			trainIcon.setLocoColor(Setup.getTrainIconColorNorth());
		if (trainIconRl.getTrainDirection() == RouteLocation.SOUTH)
			trainIcon.setLocoColor(Setup.getTrainIconColorSouth());
		if (trainIconRl.getTrainDirection() == RouteLocation.EAST)
			trainIcon.setLocoColor(Setup.getTrainIconColorEast());
		if (trainIconRl.getTrainDirection() == RouteLocation.WEST)
			trainIcon.setLocoColor(Setup.getTrainIconColorWest());
	}
	
	LocationManager locationManager = LocationManager.instance();

	private void updateStatus(RouteLocation old, RouteLocation next){
		if (next != null){
			setStatus(TRAININROUTE+" "+getNumberCarsInTrain(next)+" "+rb.getString("cars"));
		}else{
			log.debug("Train ("+getName()+")terminated");
			setStatus(TERMINATED);
			setBuilt(false);
		}
	}
	
	/**
	 * Resets the train, removes engines and cars from this train.
	 * @return true if reset successful
	 */
	public boolean reset(){
		// is this train in route?
		if (isTrainInRoute()){
			log.info("Train has started its route, can not reset");
			return false;
		}
		setCurrentLocation(null);
		setBuilt(false);
		setBuildFailed(false);
		setPrinted(false);
		// remove cars and engines from this train via property change
		setStatus(TRAINRESET);
		// remove train icon
		if (trainIcon != null && trainIcon.isActive()) {
			trainIcon.remove();
			trainIcon.dispose();
		}
		return true;
	}

    public void dispose(){
    	if (getRoute() != null)
    		getRoute().removePropertyChangeListener(this);
    	firePropertyChange (DISPOSE_CHANGED_PROPERTY, null, "Dispose");
    }
  
 	
   /**
     * Construct this Entry from XML. This member has to remain synchronized with the
     * detailed DTD in operations-trains.dtd
     *
     * @param e  Consist XML element
     */
    public Train(org.jdom.Element e) {
        org.jdom.Attribute a;
        if ((a = e.getAttribute("id")) != null )  _id = a.getValue();
        else log.warn("no id attribute in train element when reading operations");
        if ((a = e.getAttribute("name")) != null )  _name = a.getValue();
        if ((a = e.getAttribute("description")) != null )  _description = a.getValue();
        // create the train calendar
        _departureTime = Calendar.getInstance();
		_departureTime.set(2008,10,29,12,00);
        if ((a = e.getAttribute("departHour")) != null ){
        	String hour = a.getValue();
        	if ((a = e.getAttribute("departMinute")) != null ){
        		String minute = a.getValue();
        		setDepartureTime(hour, minute);
        	}
        }
        // try and first get the route by id then by name
        if ((a = e.getAttribute("routeId")) != null ) {
       		setRoute(RouteManager.instance().getRouteById(a.getValue()));
        }else if ((a = e.getAttribute("route")) != null ) {
       		setRoute(RouteManager.instance().getRouteByName(a.getValue()));
        }
        if ((a = e.getAttribute("skip")) != null ) {
        	String locationIds = a.getValue();
           	String[] locs = locationIds.split("%%");
//        	if (log.isDebugEnabled()) log.debug("Train skips : "+locationIds);
           	setTrainSkipsLocations(locs);
        }
        if ((a = e.getAttribute("carTypes")) != null ) {
        	String names = a.getValue();
           	String[] Types = names.split("%%");
//        	if (log.isDebugEnabled()) log.debug("Car types: "+names);
        	setTypeNames(Types);
        }
        if ((a = e.getAttribute("carRoadOperation")) != null )  _roadOption = a.getValue();
        if ((a = e.getAttribute("carRoads")) != null ) {
        	String names = a.getValue();
           	String[] roads = names.split("%%");
        	if (log.isDebugEnabled()) log.debug("Train (" +getName()+ ") " +getRoadOption()+  " car roads: "+ names);
        	setRoadNames(roads);
        }
        if ((a = e.getAttribute("carOwnerOption")) != null )  _ownerOption = a.getValue();
        if ((a = e.getAttribute("builtStartYear")) != null )  _builtStartYear = a.getValue();
        if ((a = e.getAttribute("builtEndYear")) != null )  _builtEndYear = a.getValue();
        if ((a = e.getAttribute("carOwners")) != null ) {
        	String names = a.getValue();
           	String[] owners = names.split("%%");
        	if (log.isDebugEnabled()) log.debug("Train (" +getName()+ ") " +getOwnerOption()+  " car owners: "+ names);
        	setOwnerNames(owners);
        }
        if ((a = e.getAttribute("numberEngines")) != null)
        	_numberEngines = a.getValue();
        if ((a = e.getAttribute("engineRoad")) != null)
        	_engineRoad = a.getValue();
        if ((a = e.getAttribute("engineModel")) != null)
        	_engineModel = a.getValue();
        if ((a = e.getAttribute("requires")) != null)
        	_requires = Integer.parseInt(a.getValue());
        if ((a = e.getAttribute("cabooseRoad")) != null)
        	_cabooseRoad = a.getValue();
 		if ((a = e.getAttribute("built")) != null)
			_built = a.getValue().equals("true");
		if ((a = e.getAttribute("build")) != null)
			_build = a.getValue().equals("true");
		if ((a = e.getAttribute("buildFailed")) != null)
			_buildFailed = a.getValue().equals("true");
		if ((a = e.getAttribute("printed")) != null)
			_printed = a.getValue().equals("true");
		if ((a = e.getAttribute("leadEngine")) != null)
			_leadEngineId = a.getValue();
		if ((a = e.getAttribute("status")) != null )  _status = a.getValue();
        if ((a = e.getAttribute("comment")) != null )  _comment = a.getValue();
        if ((a = e.getAttribute("current")) != null ){
        	if (_route != null){
        		_current = _route.getLocationById(a.getValue());
        	}
        }
    }

    /**
     * Create an XML element to represent this Entry. This member has to remain synchronized with the
     * detailed DTD in operations-trains.dtd.
     * @return Contents in a JDOM Element
     */
    public org.jdom.Element store() {
        org.jdom.Element e = new org.jdom.Element("train");
        e.setAttribute("id", getId());
        e.setAttribute("name", getName());
        e.setAttribute("description", getDescription());
        e.setAttribute("departHour", getDepartureTimeHour());
        e.setAttribute("departMinute", getDepartureTimeMinute());
        Route route = getRoute();
        if (route != null){
        	e.setAttribute("route", getRoute().getName());
        	e.setAttribute("routeId", getRoute().getId());
        }
        // build list of locations that this train skips
        String[] locationIds = getTrainSkipsLocations();
        String names ="";
        for (int i=0; i<locationIds.length; i++){
        	names = names + locationIds[i]+"%%";
        }
        e.setAttribute("skip", names);        
        if (getCurrentLocation() != null)
        	e.setAttribute("current", getCurrentLocation().getId());
    	e.setAttribute("carRoadOperation", getRoadOption());
    	e.setAttribute("carOwnerOption", getOwnerOption());	
    	e.setAttribute("builtStartYear", getBuiltStartYear());
    	e.setAttribute("builtEndYear", getBuiltEndYear());	
        e.setAttribute("numberEngines", getNumberEngines());
        e.setAttribute("engineRoad", getEngineRoad());
        e.setAttribute("engineModel", getEngineModel());
        e.setAttribute("requires", Integer.toString(getRequirements()));
        e.setAttribute("cabooseRoad", getCabooseRoad());
        e.setAttribute("built", getBuilt()?"true":"false");
        e.setAttribute("build", getBuild()?"true":"false");
        e.setAttribute("buildFailed", getBuildFailed()?"true":"false");
        e.setAttribute("printed", getPrinted()?"true":"false");
        if(getLeadEngine()!= null)
        	e.setAttribute("leadEngine", getLeadEngine().getId());
        e.setAttribute("status", getStatus());
        e.setAttribute("comment", getComment());
        // build list of car types for this train
        String[] types = getTypeNames();
        String typeNames ="";
        for (int i=0; i<types.length; i++){
       		// remove types that have been deleted by user
    		if (CarTypes.instance().containsName(types[i]) || EngineTypes.instance().containsName(types[i]))
    			typeNames = typeNames + types[i]+"%%";
        }
        e.setAttribute("carTypes", typeNames);
      	// save list of car roads for this train
        if (!getRoadOption().equals(ALLROADS)){
        	String[] roads = getRoadNames();
        	String roadNames ="";
        	for (int i=0; i<roads.length; i++){
        		roadNames = roadNames + roads[i]+"%%";
        	}
        	e.setAttribute("carRoads", roadNames);
        }
        // save list of car owners for this train
        if (!getOwnerOption().equals(ALLOWNERS)){
        	String[] owners = getOwnerNames();
        	String ownerNames ="";
        	for (int i=0; i<owners.length; i++){
        		ownerNames = ownerNames + owners[i]+"%%";
        	}
        	e.setAttribute("carOwners", ownerNames);
        }
        return e;
    }

    public void propertyChange(java.beans.PropertyChangeEvent e) {
    	if(Control.showProperty && log.isDebugEnabled())
    		log.debug("train (" + getName() + ") sees property change: "
    				+ e.getPropertyName() + " old: " + e.getOldValue() + " new: "
    				+ e.getNewValue());
    	if (e.getPropertyName().equals(Route.DISPOSE)){
    		setRoute(null);
    	}
    	// forward any property changes in this train's route
    	firePropertyChange(e.getPropertyName(), e.getOldValue(), e.getNewValue());
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
	.getLogger(Train.class.getName());

}
