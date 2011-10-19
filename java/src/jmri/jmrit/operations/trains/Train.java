// Train.java

package jmri.jmrit.operations.trains;

import java.awt.Color;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.ResourceBundle;

import org.jdom.Element;

import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.cars.CarOwners;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.rollingstock.engines.EngineTypes;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.routes.RouteManager;
import jmri.jmrit.operations.routes.RouteManagerXml;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;

import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;

import jmri.jmrit.display.PanelMenu;
import jmri.jmrit.display.Editor;


/**
 * Represents a train on the layout
 * 
 * @author Daniel Boudreau Copyright (C) 2008, 2009, 2010
 * @author Rodney Black Copyright (C) 2011
 * @version $Revision$
 */
public class Train implements java.beans.PropertyChangeListener {
	/*
	 * WARNING DO NOT LOAD CAR OR ENGINE MANAGERS WHEN Train.java IS CREATED
	 * IT CAUSES A RECURSIVE LOOP AT LOAD TIME, SEE EXAMPLES BELOW
	 * CarManager carManager = CarManager.instance();
	 * EngineManager engineManager = EngineManager.instance();
	 */
	
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
	protected int _requires = 0;			// train requirements, caboose, FRED
	protected String _numberEngines = "0";	// number of engines this train requires
	protected String _engineRoad = "";		// required road name for engines assigned to this train 
	protected String _engineModel = "";		// required model of engines assigned to this train
	protected String _cabooseRoad = "";		// required road name for cabooses assigned to this train
	protected Calendar _departureTime = Calendar.getInstance();	// departure time for this train
	protected String _leadEngineId ="";		// lead engine for train icon info
	protected String _builtStartYear = "";	// built start year 
	protected String _builtEndYear = "";	// built end year
	protected String _loadOption = ALLLOADS;// train load restrictions
	protected String _ownerOption = ALLOWNERS;// train owner name restrictions
	protected List<String> _buildScripts = new ArrayList<String>(); // list of script pathnames to run before train is built
	protected List<String> _afterBuildScripts = new ArrayList<String>(); // list of script pathnames to run after train is built
	protected List<String> _moveScripts = new ArrayList<String>(); // list of script pathnames to run when train is moved
	protected List<String> _terminationScripts = new ArrayList<String>(); // list of script pathnames to run when train is terminated
	protected String _railroadName ="";		// optional railroad name for this train
	protected String _logoURL ="";			// optional manifest logo for this train
	protected Engine _leadEngine = null; 	// lead engine for icon
	protected String _switchListStatus = UNKNOWN;		//print switch list status
	protected String _comment = "";
	
	// Engine change and helper engines
	protected int _leg2Options = 0;					// options
	protected RouteLocation _leg2Start = null;		// route location when 2nd leg begins
	protected RouteLocation _end2Leg = null;		// route location where 2nd leg ends
	protected String _leg2Engines = "0";			// number of engines 2nd leg
	protected String _leg2Road = "";				// engine road name 2nd leg 
	protected String _leg2Model = "";			// engine model 2nd leg
	protected String _leg2CabooseRoad = "";			// road name for caboose 2nd leg
	
	protected int _leg3Options = 0;					// options
	protected RouteLocation _leg3Start = null;		// route location when 3rd leg begins
	protected RouteLocation _leg3End = null;		// route location where 3rd leg ends
	protected String _leg3Engines = "0";			// number of engines 3rd leg
	protected String _leg3Road = "";				// engine road name 3rd leg 
	protected String _leg3Model = "";			// engine model 3rd leg
	protected String _leg3CabooseRoad = "";			// road name for caboose 3rd leg

	//protected Reporter _reporter;					// Reporter for interapplication communication
	
	public static final int CHANGE_ENGINES = 1;		// change engines
	public static final int HELPER_ENGINES = 2;		// add helper engines
	public static final int CHANGE_CABOOSE = 4;		// change caboose
	
	// property change names
	public static final String DISPOSE_CHANGED_PROPERTY = "TrainDispose";
	public static final String STOPS_CHANGED_PROPERTY = "TrainStops";
	public static final String TYPES_CHANGED_PROPERTY = "TrainTypes";
	public static final String BUILT_CHANGED_PROPERTY = "TrainBuilt";
	public static final String BUILT_YEAR_CHANGED_PROPERTY = "TrainBuiltYear";
	public static final String BUILD_CHANGED_PROPERTY = "TrainBuild";
	public static final String ROADS_CHANGED_PROPERTY = "TrainRoads";
	public static final String LOADS_CHANGED_PROPERTY = "TrainLoads";
	public static final String OWNERS_CHANGED_PROPERTY = "TrainOwners";
	public static final String NAME_CHANGED_PROPERTY = "TrainName";
	public static final String DESCRIPTION_CHANGED_PROPERTY = "TrainDescription";
	public static final String STATUS_CHANGED_PROPERTY = "TrainStatus";
	public static final String DEPARTURETIME_CHANGED_PROPERTY = "TrainDepartureTime";
	public static final String TRAIN_LOCATION_CHANGED_PROPERTY = "TrainLocation";
	public static final String TRAIN_ROUTE_CHANGED_PROPERTY = "TrainRoute";
	
	// Train status
	public static final String BUILDFAILED = rb.getString("BuildFailed");
	public static final String BUILDING = rb.getString("Building");
	public static final String BUILT = rb.getString("Built");
	public static final String PARTIALBUILT = rb.getString("Partial");
	public static final String TERMINATED = rb.getString("Terminated");
	public static final String TRAINRESET = rb.getString("TrainReset");
	public static final String TRAININROUTE = rb.getString("TrainInRoute");
	
	public static final int NONE = 0;		// train requirements
	public static final int CABOOSE = 1;
	public static final int FRED = 2;
	
	// road options
	public static final String ALLROADS = rb.getString("All");			// train services all road names 
	public static final String INCLUDEROADS = rb.getString("Include");
	public static final String EXCLUDEROADS = rb.getString("Exclude");
	
	// owner options
	public static final String ALLOWNERS = rb.getString("All");			// train services all owner names 
	public static final String INCLUDEOWNERS = rb.getString("Include");
	public static final String EXCLUDEOWNERS = rb.getString("Exclude");
	
	// load options
	public static final String ALLLOADS = rb.getString("All");			// train services all loads 
	public static final String INCLUDELOADS = rb.getString("Include");
	public static final String EXCLUDELOADS = rb.getString("Exclude");

	// Switch list status
	public static final String UNKNOWN = "";
	public static final String PRINTED = rb.getString("Printed");
	
	public static final String AUTO = rb.getString("Auto");				// how engines are assigned to this train

	// Reporter Strings
	/*
	public static final String USER_PREFIX = "OPS TRAIN::";		// prefix on Reporter user name
	public static final String SYSTEM_PREFIX = "IR_OPS::";		// prefix on Reporter system name
	public static final String LOCATION = "LOCATION=";			// action to move a train to a new location
	public static final String TERMINATE = "TERMINATE";				// action to terminate a train
	public static final String BUILD = "BUILD";					// action to build a train
	public static final String RESET = "RESET";					// action to reset a train
	public static final String LENGTH = "LENGTH=";				// comment for reporting a train's length
	public static final String TONNAGE = "TONNAGE=";			// comment for reporting a train's weight
	*/
	
	public Train(String id, String name) {
		log.debug("New train " + name + " " + id);
		_name = name;
		_id = id;
		// a new train accepts all types
		setTypeNames(CarTypes.instance().getNames());
		setTypeNames(EngineTypes.instance().getNames());
		addPropertyChangeListerners();
		//constructReporter();
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
	/**
	 * Get's a train's name
	 * @return train's name
	 */
	public String toString(){
		return _name;
	}

	/**
	 * Get's a train's name
	 * @return train's name
	 */
	public String getName() {
		return _name;
	}
	
	/**
	 * Get's train's departure time
	 * @return train's departure time in the String format hh:mm
	 */
	public String getDepartureTime(){
		// check to see if the route has a departure time
		RouteLocation rl = getTrainDepartsRouteLocation();
		if (rl != null && !rl.getDepartureTime().equals("")){
			// need to forward any changes to departure time
			rl.removePropertyChangeListener(this);
			rl.addPropertyChangeListener(this);
			return rl.getDepartureTime();
		}			
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
	 * Get's train's departure time in 12hr or 24hr format
	 * @return train's departure time in the String format hh:mm or hh:mm(AM/PM)
	 */
	public String getFormatedDepartureTime(){
		// check to see if the route has a departure time
		RouteLocation rl = getTrainDepartsRouteLocation();
		if (rl != null && !rl.getDepartureTime().equals("")){
			// need to forward any changes to departure time
			rl.removePropertyChangeListener(this);
			rl.addPropertyChangeListener(this);
			return rl.getFormatedDepartureTime();
		}			
		int hour = _departureTime.get(Calendar.HOUR_OF_DAY);
		//AM_PM field
		String am_pm = "";
		if (Setup.is12hrFormatEnabled()){
			hour = _departureTime.get(Calendar.HOUR);
			if (hour == 0)
				hour = 12;
			am_pm = (_departureTime.get(Calendar.AM_PM)== Calendar.AM)? " AM":" PM";
		}
		int minute = _departureTime.get(Calendar.MINUTE);
		String h = Integer.toString(hour);
		if (hour < 10)
			h = "0"+h;
		if (minute < 10)
			return h+":0"+minute+am_pm;
		return h+":"+minute+am_pm;
	}
	
	/**
	 * Get train's departure time in minutes from midnight for sorting
	 * @return int hh*60+mm
	 */
	public int getDepartTimeMinutes(){
		int hour = Integer.parseInt(getDepartureTimeHour());
		int minute = Integer.parseInt(getDepartureTimeMinute());
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
		String[] time = getDepartureTime().split(":");
		return time[0];	
	}
	
	public String getDepartureTimeMinute(){
		String[] time = getDepartureTime().split(":");
		return time[1];	
	}
	
	/**
	 * Gets the expected time when this train will arrive at
	 * the location rl.  Expected arrival time is based on the
	 * number of car pick up and set outs for this train.
	 * TODO Doesn't provide expected arrival time if train
	 * is in route, instead provides relative time.  If train
	 * has passed the location return -1.
	 * @return expected arrival time
	 */
	public String getExpectedArrivalTime(RouteLocation routeLocation){
		int minutes = getExpectedTravelTimeInMinutes(routeLocation);
		if (minutes == -1)
			return "-1";
		log.debug("Calculate arrival time for train (" +getName()+ ") at ("+routeLocation.getName()+"), minutes from departure: "+minutes);
		// TODO use fast clock to get current time vs departure time
		// for now use relative
		return parseTime(minutes);
	}
	
	public String getExpectedDepartureTime(RouteLocation routeLocation){
		int minutes = getExpectedTravelTimeInMinutes(routeLocation);
		if (minutes == -1)
			return "-1";
		log.debug("Calculate departure time for train (" +getName()+ ") at ("+routeLocation.getName()+")");
		
		CarManager carManager = CarManager.instance();
		List<String> carList = carManager.getByTrainList(this);
		// add any work at this location
		for (int j=0; j<carList.size(); j++){
			Car car = carManager.getById(carList.get(j));
			if (car.getRouteLocation() == routeLocation && !car.getTrackName().equals("")){
				minutes += Setup.getSwitchTime();
			}
			if (car.getRouteDestination() == routeLocation){
				minutes += Setup.getSwitchTime();
			}
		}		
		return parseTime(minutes);
	}
	
	private int getExpectedTravelTimeInMinutes(RouteLocation routeLocation){
		int minutes = 0;
		if (!isTrainInRoute()){
			minutes += _departureTime.get(Calendar.MINUTE); 
			minutes += 60*_departureTime.get(Calendar.HOUR_OF_DAY);
		} else {
			minutes = -1;
		}
		//boolean trainAt = false;
		CarManager carManager = CarManager.instance();
		List<String> carList = carManager.getByTrainList(this);
		boolean trainLocFound = false;
		if (getRoute() != null){
			List<String> routeList = getRoute().getLocationsBySequenceList();
			for (int i=0; i<routeList.size(); i++){
				RouteLocation rl = getRoute().getLocationById(routeList.get(i));
				if (rl == routeLocation)
					break; // done
				// start recording time after finding where the train is
				if (!trainLocFound && isTrainInRoute()){
					if (rl == getCurrentLocation()){
						trainLocFound = true;
						// add travel time
						minutes = Setup.getTravelTime();
					}
					continue;
				}
				// is there a departure time from this location?
				if (!rl.getDepartureTime().equals("")){
					String dt = rl.getDepartureTime();
					log.debug("Location "+rl.getName()+" departure time "+dt);
					String[] time = dt.split(":");
					minutes = 60*Integer.parseInt(time[0])+Integer.parseInt(time[1]);
					//log.debug("New minutes: "+minutes);
				}
				// add wait time
				minutes += rl.getWait();
				// add travel time
				minutes += Setup.getTravelTime();	
				// don't count work if there's a departure time
				if (i == 0 || !rl.getDepartureTime().equals(""))
					continue; 
				for (int j=0; j<carList.size(); j++){
					Car car = carManager.getById(carList.get(j));
					if (car.getRouteLocation() == rl && !car.getTrackName().equals("")){
						minutes += Setup.getSwitchTime();
					}
					if (car.getRouteDestination() == rl){
						minutes += Setup.getSwitchTime();
					}
				}
			}
		}
		return minutes;
	}
	
	private String parseTime(int minutes){
		int hours = 0;
		int days = 0;
				
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
		
		//AM_PM field
		String am_pm = "";
		if (Setup.is12hrFormatEnabled()){
			am_pm = " AM";
			if  (hours >= 12){
				hours = hours - 12;
				am_pm = " PM";
			}
			if (hours == 0)
				hours = 12;
		}
		
		String h = Integer.toString(hours);
		if (hours < 10)
			h = "0"+h;
		if (minutes < 10)
			return d+h+":0"+minutes;
		return d+h+":"+minutes+am_pm;
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
	
	/**
	 * Get a train's requirements with regards to the last car in the train.
	 * @return NONE CABOOSE FRED
	 */
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
			firePropertyChange(TRAIN_ROUTE_CHANGED_PROPERTY, oldRoute, newRoute);
		}
	}
	
	/**
	 * Gets the train's route
	 * @return train's route
	 */
	public Route getRoute() {
		return _route;
	}
	
	/**
	 * Get's the train's route name.
	 * @return Train's route name.
	 */
	public String getTrainRouteName (){
    	if (_route == null)
    		return "";
    	return _route.getName();
    }
    
	/**
	 * Get the train's departure location's name
	 * @return train's departure location's name
	 */
	public String getTrainDepartsName(){
    	if (getTrainDepartsRouteLocation() != null ){
     		return getTrainDepartsRouteLocation().getName();
     	}
    	return "";
    }
	
	protected RouteLocation getTrainDepartsRouteLocation(){
		if (_route == null)
			return null;
		return _route.getDepartsRouteLocation();
	}

	/**
	 * Get train's final location's name
	 * @return train's final location's name
	 */
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
		if ((old != null && !old.equals(location)) || (old == null && location != null)){
			firePropertyChange("current", old, location);
		}
	}

	/**
	 * Get train's current location name
	 * @return Train's current route location name
	 */
	public String getCurrentLocationName() {
		if (getCurrentLocation() == null)
			return "";
		return getCurrentLocation().getName();
	}
	
	/**
	 * Get train's current location
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
	 * Get the train's next location name
	 * @return Train's next route location name
	 */
	public String getNextLocationName(){
		RouteLocation rl = getNextLocation();
		if (rl != null)
			return rl.getName();
		return "";
	}
	
	public RouteLocation getNextLocation(){
		if (getRoute() == null)
			return null;
		List<String> routeList = getRoute().getLocationsBySequenceList();
		for (int i=0; i<routeList.size(); i++){
			RouteLocation rl = getRoute().getLocationById(routeList.get(i));
			if (rl == getCurrentLocation()){
				i++;
				if (i < routeList.size()){
					rl = getRoute().getLocationById(routeList.get(i));
					return rl;
				} 
				break;
			}
		}
		return null;	// At end of route
		
	}
	
	/**
	 * Set's the train's status
	 * @param status BUILDING PARTIALBUILT BUILT TERMINATED TRAINRESET TRAININROUTE
	 */
	public void setStatus(String status) {
		String old = _status;
		_status = status;
		if (!old.equals(status)){
			firePropertyChange(STATUS_CHANGED_PROPERTY, old, status);
		}
	}
	
	/**
	 * Get train's status
	 * @return Train's status BUILDING PARTIALBUILT BUILT TERMINATED TRAINRESET TRAININROUTE
	 */
	public String getStatus() {
		return _status;
	}
	
	/**
	 * Used to determine if train has departed its 
	 * @return true if train is in route
	 */
	public boolean isTrainInRoute() {
		return getCurrentLocationName() != "" && getTrainDepartsRouteLocation() != getCurrentLocation();
	}

	List<String> _skipLocationsList = new ArrayList<String>();

	protected String[] getTrainSkipsLocations(){
		String[] locationIds = new String[_skipLocationsList.size()];
		for (int i=0; i<_skipLocationsList.size(); i++)
			locationIds[i] = _skipLocationsList.get(i);
		return locationIds;
	}

	protected void setTrainSkipsLocations(String[] locationIds){
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

	/**
	 * Determines if this train skips a location 
	 * (doesn't service the location).
	 * @param locationId The route location id.
	 * @return true if the train will not service the location.
	 */
	public boolean skipsLocation(String locationId){
		return _skipLocationsList.contains(locationId);
	}

    List<String> _typeList = new ArrayList<String>();
    /**
     * Get's the type names of rolling stock this train will service
     * @return The type names for cars and or engines
     */
    protected String[] getTypeNames(){
      	String[] types = new String[_typeList.size()];
     	for (int i=0; i<_typeList.size(); i++)
     		types[i] = _typeList.get(i);
   		return types;
    }
    
    /**
     * Set the type of cars or engines this train will service, see types in
     * Cars and Engines.
     * @param types The type names for cars and or engines
     */
    protected void setTypeNames(String[] types){
    	if (types.length == 0) return;
    	jmri.util.StringUtil.sort(types);
 		for (int i=0; i<types.length; i++)
 			_typeList.add(types[i]);
    }
    
    /**
     * Add a car or engine type name that this train will service.
     * @param type The new type name to service.
     */
    public void addTypeName(String type){
    	// insert at start of list, sort later
    	if (_typeList.contains(type))
    		return;
    	_typeList.add(0,type);
    	log.debug("train "+getName()+" add car type "+type);
    	firePropertyChange (TYPES_CHANGED_PROPERTY, _typeList.size()-1, _typeList.size());
    }
    
    public void deleteTypeName(String type){
       	if (!_typeList.contains(type))
    		return;
    	_typeList.remove(type);
    	log.debug("train "+getName()+" delete car type "+type);
     	firePropertyChange (TYPES_CHANGED_PROPERTY, _typeList.size()+1, _typeList.size());
     }
    
    /**
     * Returns true if this train will service the type of car or engine.
     * @param type The car or engine type name. 
     * @return true if this train will service the particular type.
     */
    public boolean acceptsTypeName(String type){
    	return _typeList.contains(type);
    }
    
    public void replaceType(String oldType, String newType){
    	if (acceptsTypeName(oldType)){
    		deleteTypeName(oldType);
    		addTypeName(newType);
    	}
    }
    
    /**
     * Get how this train deals with road names.
     * @return ALLROADS INCLUDEROADS EXCLUDEROADS
     */
	public String getRoadOption (){
    	return _roadOption;
    }
    
 	/**
 	 * Set how this train deals with car road names.
 	 * @param option ALLROADS INCLUDEROADS EXCLUDEROADS
 	 */
    public void setRoadOption (String option){
    	String old = _roadOption;
    	_roadOption = option;
    	firePropertyChange (ROADS_CHANGED_PROPERTY, old, option);
    }

    List<String> _roadList = new ArrayList<String>();
    protected void setRoadNames(String[] roads){
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
    
    public void replaceRoad(String oldRoad, String newRoad){
    	if (deleteRoadName(oldRoad) && newRoad != null)
    		addRoadName(newRoad);
    	if (getEngineRoad().equals(oldRoad))
    		setEngineRoad(newRoad);
    	if (getCabooseRoad().equals(oldRoad))
    		setCabooseRoad(newRoad);
    }
    
    /**
     * Gets the car load option for this train.
     * @return ALLLOADS INCLUDELOADS EXCLUDELOADS
     */
	public String getLoadOption (){
    	return _loadOption;
    }
    
 	/**
 	 * Set how this train deals with car loads
 	 * @param option ALLLOADS INCLUDELOADS EXCLUDELOADS
 	 */
    public void setLoadOption (String option){
    	String old = _loadOption;
    	_loadOption = option;
    	firePropertyChange (LOADS_CHANGED_PROPERTY, old, option);
    }

    List<String> _loadList = new ArrayList<String>();
    protected void setLoadNames(String[] loads){
    	if (loads.length == 0) return;
    	jmri.util.StringUtil.sort(loads);
 		for (int i=0; i<loads.length; i++){
 			if (!loads[i].equals(""))
 				_loadList.add(loads[i]);
 		}
    }
    
    /**
     * Provides a list of loads that the train will
     * either service or exclude.  See setLoadOption
     * @return Array of load names as Strings
     */
    public String[] getLoadNames(){
      	String[] loads = new String[_loadList.size()];
     	for (int i=0; i<_loadList.size(); i++)
     		loads[i] = _loadList.get(i);
     	if (_loadList.size() == 0)
     		return loads;
     	jmri.util.StringUtil.sort(loads);
   		return loads;
    }
    
    /**
     * Add a load that the train will
     * either service or exclude.  See setLoadOption
     * @return true if load name was added, false if load name
     * wasn't in the list.
     */
    public boolean addLoadName(String load){
     	if (_loadList.contains(load))
    		return false;
    	_loadList.add(load);
    	log.debug("train (" +getName()+ ") add car load "+load);
    	firePropertyChange (LOADS_CHANGED_PROPERTY, _loadList.size()-1, _loadList.size());
    	return true;
    }
    
    /**
     * Delete a load name that the train will
     * either service or exclude.  See setLoadOption
     * @return true if load name was removed, false if load name
     * wasn't in the list.
     */
    public boolean deleteLoadName(String load){
     	if (!_loadList.contains(load))
    		return false;
    	_loadList.remove(load);
    	log.debug("train (" +getName()+ ") delete car load "+load);
    	firePropertyChange (LOADS_CHANGED_PROPERTY, _loadList.size()+1, _loadList.size());
       	return true;
    }
    
    /**
     * Determine if train will service a specific load name.
     * @param load the load name to check.
     * @return true if train will service this load.
     */
    public boolean acceptsLoadName(String load){
    	if (_loadOption.equals(ALLLOADS)){
    		return true;
    	}
       	if (_loadOption.equals(INCLUDELOADS)){
       		return _loadList.contains(load);
       	}
       	// exclude!
       	return !_loadList.contains(load);
    }
    
	public String getOwnerOption (){
    	return _ownerOption;
    }
    
 	/**
 	 * Set how this train deals with car owner names
 	 * @param option ALLOWNERS INCLUDEOWNERS EXCLUDEOWNERS
 	 */
    public void setOwnerOption (String option){
    	String old = _ownerOption;
    	_ownerOption = option;
    	firePropertyChange (OWNERS_CHANGED_PROPERTY, old, option);
    }

    List<String> _ownerList = new ArrayList<String>();
    protected void setOwnerNames(String[] owners){
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
    
    public void replaceOwner(String oldName, String newName){
    	if (acceptsOwnerName(oldName)){
    		deleteOwnerName(oldName);
    		addOwnerName(newName);
    	}
    }
    
    /**
     * Only rolling stock built in or after this year will be used.
     * @param year
     */
    public void setBuiltStartYear(String year){
    	String old = _builtStartYear;
    	_builtStartYear = year;
    	firePropertyChange (BUILT_YEAR_CHANGED_PROPERTY, old, year);
    }
    
    public String getBuiltStartYear(){
    	return _builtStartYear;
    }
    
    /**
     * Only rolling stock built in or before this year will be used.
     * @param year
     */
    public void setBuiltEndYear(String year){
    	String old = _builtEndYear;
    	_builtEndYear = year;
    	firePropertyChange (BUILT_YEAR_CHANGED_PROPERTY, old, year);
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
    		log.debug("Train ("+getName()+") built start date not initialized, start: "+getBuiltStartYear());
    	}
    	try{
    		e = Integer.parseInt(getBuiltEndYear());
    	} catch (NumberFormatException e1){
    		log.debug("Train ("+getName()+") built end date not initialized, end: "+getBuiltEndYear());
    	}  	
    	try{
    		int d = Integer.parseInt(date);
    		if (s<d && d<e)
    			return true;
    		else
    			return false;
    	} catch (NumberFormatException e1){
    		//log.debug("Built date: "+date+" isn't an integer");
    		// maybe the built date is in the format month-year
    		String[] built = date.split("-");
    		if (built.length>1)
    			try{
    				int d = Integer.parseInt(built[1]);
    				if (d<100)
    					d = d + 1900;
    			if (s<d && d<e)
    				return true;
    			else
    				return false;
    			} catch (NumberFormatException e2){
    				log.debug("Unable to parse car built date "+date);
    			}
    		return false;
    	}  	
    }
    private boolean debugFlag = false;
    /**
     * Determines if this train will service this car.
     * Note this code doesn't check the location or tracks
     * that needs to be do separately.  See Router.java.
     * @param car The car to be tested.
     * @return true if this train can service the car.
     */
    public boolean servicesCar(Car car){
    	if (acceptsTypeName(car.getType()) && acceptsBuiltDate(car.getBuilt()) 
    			&& acceptsOwnerName(car.getOwner()) && acceptsRoadName(car.getRoad())
    			&& acceptsLoadName(car.getLoad())){
    		Route route = getRoute();
    		if (route != null){
    			List<String> rLocations = route.getLocationsBySequenceList();
    			if (rLocations.size() == 1){
    				RouteLocation rLoc = route.getLocationById(rLocations.get(0));
    				if (rLoc.getName().equals(car.getLocationName()) 
    						&& rLoc.canPickup()  
    						&& rLoc.getMaxCarMoves()>0
    						&& !skipsLocation(rLoc.getId())
    						&& rLoc.getName().equals(car.getDestinationName())
    						&& rLoc.canDrop()){
    					if (car.getTrack() != null && !car.getTrack().acceptsPickupTrain(this)){
    						return false;
    					}
       					if (car.getDestinationTrack() != null && !car.getDestinationTrack().acceptsDropTrain(this)){
    						return false;
    					}
    					if (debugFlag)
    						log.debug("Local switcher "+getName()+" for location "+rLoc.getName());
    					return true;
    				}
    			}
    			// Multiple locations in the train's route
    			else for (int j=0; j<rLocations.size()-1; j++){
    				RouteLocation rLoc = route.getLocationById(rLocations.get(j));
    				if (rLoc.getName().equals(car.getLocationName()) 
    						&& rLoc.canPickup()  
    						&& rLoc.getMaxCarMoves()>0
    						&& !skipsLocation(rLoc.getId())
    						&& (car.getLocation().getTrainDirections() & rLoc.getTrainDirection()) > 0){
    					if (car.getTrack() != null){
    						if ((car.getTrack().getTrainDirections() & rLoc.getTrainDirection()) == 0 
    								|| !car.getTrack().acceptsPickupTrain(this))
    							continue;
    					}
    					if (debugFlag)
    						log.debug("Car ("+car.toString()+") can be picked up by train ("+getName()+") from ("
    								+car.getLocationName()+", "+car.getTrackName()+")");
    					if (car.getDestination() != null){
    						// now check car's destination
    						for (int k=j; k<rLocations.size(); k++){
    							rLoc = route.getLocationById(rLocations.get(k));
    							if (rLoc.getName().equals(car.getDestinationName()) 
    									&& rLoc.canDrop() 
    									&& rLoc.getMaxCarMoves()>0
    									&& !skipsLocation(rLoc.getId())
    									&& (car.getDestination().getTrainDirections() & rLoc.getTrainDirection()) > 0){
    								if (car.getDestinationTrack() != null){
    									if ((car.getDestinationTrack().getTrainDirections() & rLoc.getTrainDirection()) == 0
    											|| !car.getDestinationTrack().acceptsDropTrain(this))
    										continue;
    								}
    		    					// check to see if moves are available
    		    					if (getStatus().equals(BUILDING)){
    		    						if (rLoc.getMaxCarMoves()-rLoc.getCarMoves() == 0){
    		    							if (debugFlag)
    		    								log.debug("No available moves for destination "+rLoc.getName());
    		    							continue;
    		    						}
    		    					}
    								if (debugFlag)
    									log.debug("Car ("+car.toString()+") can be dropped by train ("+getName()+") to ("
    											+car.getDestinationName()+", "+car.getDestinationTrackName()+")");
    								return true;
    							}
    						}
    					} else {
    						if (debugFlag)
        						log.debug("Car ("+car.toString()+") does not have a destination");
    						return true;
    					}
    				}
    			}
    		}
    	}
    	return false;
    }

    /**
     * 
     * @return The number of cars worked by this train
     */
    public int getNumberCarsWorked(){
    	List<String> cars = CarManager.instance().getByTrainList(this);
    	// remove cars that haven't been assigned by the train builder
    	for (int i=0; i<cars.size(); i++){
    		Car c = CarManager.instance().getById(cars.get(i));
    		if (c.getRouteLocation() == null){
    			cars.remove(i--);
    		}
    	}
    	return cars.size();
    }
    
    /**
     * Gets the number of cars in the train at the current location
     * in the train's route.
     * @return The number of cars currently in the train
     */
    public int getNumberCarsInTrain(){
    	RouteLocation rl = getCurrentLocation();
    	int number = 0;
    	List<String> cars = CarManager.instance().getByTrainList(this);
    	// remove cars that aren't in the train
    	for (int i=0; i<cars.size(); i++){
    		Car car = CarManager.instance().getById(cars.get(i));
    		if (car.getRouteLocation() == rl && car.getRouteDestination() != rl){
    			number++;
    		}
    	}
    	return number;
    }
    
    /**
     * Gets the train's length at the current location
     * in the train's route.
     * @return The train length at this location
     */
    public int getTrainLength(){
    	RouteLocation rl = getCurrentLocation();
    	int length = 0;
    	List<String> engines = EngineManager.instance().getByTrainList(this);
    	for (int i=0; i<engines.size(); i++){
       		Engine eng = EngineManager.instance().getById(engines.get(i));
    		if (eng.getRouteLocation() == rl && eng.getRouteDestination() != rl){
    			length = length + Integer.parseInt(eng.getLength()) + Engine.COUPLER;
    		}
    	}
    	List<String> cars = CarManager.instance().getByTrainList(this);
    	for (int i=0; i<cars.size(); i++){
       		Car car = CarManager.instance().getById(cars.get(i));
    		if (car.getRouteLocation() == rl && car.getRouteDestination() != rl){
    			length = length + Integer.parseInt(car.getLength()) + Car.COUPLER;
    		}
    	}
    	return length;
    }
    
    /**
     * Get the train's weight at the current location.
     * @return Train's weight in tons.
     */
    public int getTrainWeight(){
    	RouteLocation rl = getCurrentLocation();
    	int weight = 0;
    	List<String> engines = EngineManager.instance().getByTrainList(this);
    	for (int i=0; i<engines.size(); i++){
       		Engine eng = EngineManager.instance().getById(engines.get(i));
    		if (eng.getRouteLocation() == rl && eng.getRouteDestination() != rl){
    			weight = weight + eng.getAdjustedWeightTons();
    		}
    	}
    	List<String> cars = CarManager.instance().getByTrainList(this);
    	for (int i=0; i<cars.size(); i++){
       		Car car = CarManager.instance().getById(cars.get(i));
    		if (car.getRouteLocation() == rl && car.getRouteDestination() != rl){
    			weight = weight + car.getAdjustedWeightTons();
    		}
    	}
    	return weight;
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
	 * Get the number of engines that this train requires.
	 * @return The number of engines that this train requires.
	 */
	public String getNumberEngines() {
		return _numberEngines;
	}
	
	/**
	 * Get the number of engines needed for the second set.
	 * @return The number of engines needed in route
	 */
	public String getSecondLegNumberEngines() {
		return _leg2Engines;
	}
	
	public void setSecondLegNumberEngines(String number) {
		_leg2Engines = number;
	}

	/**
	 * Get the number of engines needed for the third set.
	 * @return The number of engines needed in route
	 */
	public String getThirdLegNumberEngines() {
		return _leg3Engines;
	}
	
	public void setThirdLegNumberEngines(String number) {
		_leg3Engines = number;
	}

	/**
	 * Set the road name of engines servicing this train.
	 * @param road The road name of engines servicing this train.
	 */
	public void setEngineRoad(String road) {
		_engineRoad = road;
	}

	/**
	 * Get the road name of engines servicing this train.
	 * @return The road name of engines servicing this train.
	 */
	public String getEngineRoad() {
		return _engineRoad;
	}
	
	/**
	 * Set the road name of engines servicing this train 2nd leg.
	 * @param road The road name of engines servicing this train.
	 */
	public void setSecondLegEngineRoad(String road) {
		_leg2Road = road;
	}

	/**
	 * Get the road name of engines servicing this train 2nd leg.
	 * @return The road name of engines servicing this train.
	 */
	public String getSecondLegEngineRoad() {
		return _leg2Road;
	}
	
	/**
	 * Set the road name of engines servicing this train 3rd leg.
	 * @param road The road name of engines servicing this train.
	 */
	public void setThirdLegEngineRoad(String road) {
		_leg3Road = road;
	}

	/**
	 * Get the road name of engines servicing this train 3rd leg.
	 * @return The road name of engines servicing this train.
	 */
	public String getThirdLegEngineRoad() {
		return _leg3Road;
	}
	
	
	/**
	 * Set the model name of engines servicing this train.
	 * @param model The model name of engines servicing this train.
	 */
	public void setEngineModel(String model) {
		_engineModel = model;
	}

	public String getEngineModel() {
		return _engineModel;
	}
	
	/**
	 * Set the model name of engines servicing this train's 2nd leg.
	 * @param model The model name of engines servicing this train.
	 */
	public void setSecondLegEngineModel(String model) {
		_leg2Model = model;
	}

	public String getSecondLegEngineModel() {
		return _leg2Model;
	}
	
	/**
	 * Set the model name of engines servicing this train's 3rd leg.
	 * @param model The model name of engines servicing this train.
	 */
	public void setThirdLegEngineModel(String model) {
		_leg3Model = model;
	}

	public String getThirdLegEngineModel() {
		return _leg3Model;
	}
	
	/**
	 * Set the road name of the caboose servicing this train.
	 * @param road The road name of the caboose servicing this train.
	 */
	public void setCabooseRoad(String road) {
		_cabooseRoad = road;
	}

	public String getCabooseRoad() {
		return _cabooseRoad;
	}
	
	/**
	 * Set the road name of the second leg caboose servicing this train.
	 * @param road The road name of the caboose servicing this train's 2nd leg.
	 */
	public void setSecondLegCabooseRoad(String road) {
		_leg2CabooseRoad = road;
	}

	public String getSecondLegCabooseRoad() {
		return _leg2CabooseRoad;
	}
	
	/**
	 * Set the road name of the third leg caboose servicing this train.
	 * @param road The road name of the caboose servicing this train's 3rd leg.
	 */
	public void setThirdLegCabooseRoad(String road) {
		_leg3CabooseRoad = road;
	}

	public String getThirdLegCabooseRoad() {
		return _leg3CabooseRoad;
	}
	
	public void setSecondLegStartLocation(RouteLocation rl){
		_leg2Start = rl;
	}
	
	public RouteLocation getSecondLegStartLocation(){
		return _leg2Start;
	}
	
	public String getSecondLegStartLocationName(){
		if (getSecondLegStartLocation() == null)
			return "";
		return getSecondLegStartLocation().getName();
	}
	
	public void setThirdLegStartLocation(RouteLocation rl){
		_leg3Start = rl;
	}
	
	public RouteLocation getThirdLegStartLocation(){
		return _leg3Start;
	}
	
	public String getThirdLegStartLocationName(){
		if (getThirdLegStartLocation() == null)
			return "";
		return getThirdLegStartLocation().getName();
	}
	
	public void setSecondLegEndLocation(RouteLocation rl){
		_end2Leg = rl;
	}
	
	public String getSecondLegEndLocationName(){
		if (getSecondLegEndLocation() == null)
			return "";
		return getSecondLegEndLocation().getName();
	}
	
	public RouteLocation getSecondLegEndLocation(){
		return _end2Leg;
	}
	
	public void setThirdLegEndLocation(RouteLocation rl){
		_leg3End = rl;
	}
	
	public RouteLocation getThirdLegEndLocation(){
		return _leg3End;
	}
	
	public String getThirdLegEndLocationName(){
		if (getThirdLegEndLocation() == null)
			return "";
		return getThirdLegEndLocation().getName();
	}
	
	public void setSecondLegOptions(int options){
		_leg2Options = options;
	}
	
	public int getSecondLegOptions(){
		return _leg2Options;
	}
	
	public void setThirdLegOptions(int options){
		_leg3Options = options;
	}
	
	public int getThirdLegOptions(){
		return _leg3Options;
	}
	
	public void setComment(String comment) {
		_comment = comment;
	}

	public String getComment() {
		return _comment;
	}
	
	/**
	 * Add a script to run before a train is built
	 * @param pathname The script's pathname
	 */
	public void addBuildScript(String pathname){
		_buildScripts.add(pathname);
	}
	
	public void deleteBuildScript(String pathname){
		_buildScripts.remove(pathname);
	}
	
	/**
	 * Gets a list of pathnames (scripts) to run before this train is built
	 * @return A list of pathnames to run before this train is built
	 */
	public List<String> getBuildScripts(){
		return _buildScripts;
	}
	
	/**
	 * Add a script to run after a train is built
	 * @param pathname The script's pathname
	 */
	public void addAfterBuildScript(String pathname){
		_afterBuildScripts.add(pathname);
	}
	
	public void deleteAfterBuildScript(String pathname){
		_afterBuildScripts.remove(pathname);
	}
	
	/**
	 * Gets a list of pathnames (scripts) to run after this train is built
	 * @return A list of pathnames to run after this train is built
	 */
	public List<String> getAfterBuildScripts(){
		return _afterBuildScripts;
	}
	
	/**
	 * Add a script to run when train is moved
	 * @param pathname The script's pathname
	 */
	public void addMoveScript(String pathname){
		_moveScripts.add(pathname);
	}
	
	public void deleteMoveScript(String pathname){
		_moveScripts.remove(pathname);
	}
	
	/**
	 * Gets a list of pathnames (scripts) to run when this train moved
	 * @return A list of pathnames to run when this train moved
	 */
	public List<String> getMoveScripts(){
		return _moveScripts;
	}
	
	/**
	 * Add a script to run when train is terminated
	 * @param pathname The script's pathname
	 */
	public void addTerminationScript(String pathname){
		_terminationScripts.add(pathname);
	}
	
	public void deleteTerminationScript(String pathname){
		_terminationScripts.remove(pathname);
	}
	
	/**
	 * Gets a list of pathnames (scripts) to run when this train terminates
	 * @return A list of pathnames to run when this train terminates
	 */
	public List<String> getTerminationScripts(){
		return _terminationScripts;
	}
	
	/**
	 * Gets the optional railroad name for this train.
	 * @return Train's railroad name.
	 */
	public String getRailroadName(){
		return _railroadName;
	}
	
	/**
	 * Overrides the default railroad name for this train.
	 * @param name The railroad name for this train.
	 */
	public void setRailroadName(String name){
		_railroadName = name;
	}
	
	public String getManifestLogoURL(){
		return _logoURL;
	}
	
	/**
	 * Overrides the default logo for this train.
	 * @param pathName file location for the logo.
	 */
	public void setManifestLogoURL(String pathName){
		_logoURL = pathName;
	}
	
	public void setBuilt(boolean built) {
		boolean old = _built;
		_built = built;
		if (old != built){
			firePropertyChange(BUILT_CHANGED_PROPERTY, old?"true":"false", built?"true":"false");
		}
	}
	
	/**
	 * Used to determine if this train has been built.
	 * @return true if the train was successfully built.
	 */
	public boolean isBuilt() {
		return _built;
	}
	
	/**
	 * Deprecated, kept for user scripts.  Use isBuilt()
	 * @return true if the train was successfully built.
	 * 
	 */
	@Deprecated
	public boolean getBuilt() {
		return isBuilt();
	}
	
	/**
	 * Control flag used to decide if this train is to be built.
	 * @param build When true, build this train.
	 */
	public void setBuildEnabled(boolean build) {
		boolean old = _build;
		_build = build;
		if (old != build){
			firePropertyChange(BUILD_CHANGED_PROPERTY, old?"true":"false", build?"true":"false");
		}
	}
	
	/**
	 * Used to determine if train is to be built.
	 * @return true if train is to be built.
	 */
	public boolean isBuildEnabled() {
		return _build;
	}
	
	/**
	 * Deprecated, use setBuildEnabled(build).
	 * @param build When true, build this train.
	 */
	@Deprecated
	public void setBuild(boolean build) {
		setBuildEnabled(build);
	}
	
	/**
	 * Deprecated, use isBuildEnabled()
	 * @return true if train is to be built.
	 */
	@Deprecated
	public boolean getBuild(){
		return isBuildEnabled();
	}
	
	/**
	 * Build this train if the build control flag is true.
	 * @return True only if train is successfully built.
	 */
	public boolean buildIfSelected(){
		if(_build && !_built){
			build();
			return true;
		}
		log.debug("Train ("+getName()+") not selected or already built, skipping build");
		return false;
	}

	/**
	 * Build this train.  Creates a train manifest.
	 */
	public void build(){
		reset();
		// run before build scripts
		runScripts(getBuildScripts());
		TrainBuilder tb = new TrainBuilder();
		tb.build(this);
		setPrinted(false);
		setSwitchListStatus(UNKNOWN);
		// run after build scripts
		runScripts(getAfterBuildScripts());
	}
	
	/**
	 * Run train scripts, waits for completion before returning.
	 */
	private synchronized void runScripts(List<String> scripts) {
		if (scripts.size() > 0) {
			// find the number of active threads
			ThreadGroup root = Thread.currentThread().getThreadGroup();
			int numberOfThreads = root.activeCount();		
			for (int i=0; i<scripts.size(); i++) {
				jmri.util.PythonInterp.runScript(jmri.util.FileUtil.getExternalFilename(scripts.get(i)));
			}
			// need to wait for scripts to complete or 2 seconds maximum
			int count = 0;
			while (root.activeCount() > numberOfThreads) {
				synchronized (this) {
					log.debug("Number of active threads: "+root.activeCount());
					try {
						wait(20);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt(); // retain if needed later
					}
					if (count++ > 100)
						break;	// 2 seconds maximum 20*100 = 2000
				}
			}
		}
	}
	
	public void printBuildReport(){
		boolean isPreview = TrainManager.instance().isPrintPreviewEnabled();
		printBuildReport(isPreview);
	}

	public boolean printBuildReport(boolean isPreview){
		File buildFile = TrainManagerXml.instance().getTrainBuildReportFile(getName());
		if (!buildFile.exists()){
			log.warn("Build file missing for train "+getName());
			return false;
		}
		
		if (isPreview && Setup.isBuildReportEditorEnabled())
			TrainPrintUtilities.editReport(buildFile, getName());
		else
			TrainPrintUtilities.printReport(buildFile, MessageFormat.format(rb.getString("buildReport"),new Object[]{getDescription()}), isPreview, "", true, "");
		return true;
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
			boolean isPreview = TrainManager.instance().isPrintPreviewEnabled();
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
		if (!file.exists()){
			log.warn("Manifest file missing for train "+getName());
			return false;
		}
		if (isPreview && Setup.isManifestEditorEnabled()){
			TrainPrintUtilities.openDesktopEditor(file);
			return true;
		}
		String logoURL = "";
		if (!getManifestLogoURL().equals(""))
			logoURL = getManifestLogoURL();
		else 
			logoURL = Setup.getManifestLogoURL();
		Location departs = LocationManager.instance().getLocationByName(getTrainDepartsName());
		String printerName = departs.getDefaultPrinterName();
		TrainPrintUtilities.printReport(file, "Train Manifest "+getDescription(), isPreview, Setup.getFontName(), false, logoURL, printerName);
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
		
	protected RouteLocation _trainIconRl = null; // saves the icon current route location

	/**
	 * Sets the panel position for the train icon   
	 * for the current route location.
	 * @return true if train coordinates can be set
	 */
	public boolean setTrainIconCoordinates(){
		if (Setup.isTrainIconCordEnabled()){
			_trainIconRl.setTrainIconX(_trainIcon.getX());
			_trainIconRl.setTrainIconY(_trainIcon.getY());
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
	 * engines, cars, and train icon.  Will also terminate a
	 * train after it arrives at its final destination.
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
	
	/**
	 * Move train to a location in the train's route.  Code checks 
	 * to see if the location requested is part of the train's route and
	 * if the train hasn't already visited the location.  This command
	 * can only move the train forward in its route.  Note that you can
	 * not terminate the train using this command.  See move().
	 * @param locationName The name of the location to move this train.
	 * @return true if train was able to move to the named location.
	 */
	public boolean move(String locationName){
		log.info("Move train ("+getName()+") to location ("+locationName+")");
		if (getRoute() == null || getCurrentLocation() == null)
			return false;
		List<String> routeList = getRoute().getLocationsBySequenceList();
		for (int i = 0; i < routeList.size(); i++) {
			RouteLocation rl = getRoute().getLocationById(routeList.get(i));
			if (getCurrentLocation() == rl) {
				for (int j = i + 1; j < routeList.size(); j++){
					rl = getRoute().getLocationById(routeList.get(j));
					if (rl.getName().equals(locationName)){
						log.debug("Found location ("+locationName+") moving train to this location");
						for (j = i + 1; j < routeList.size(); j++){
							rl = getRoute().getLocationById(routeList.get(j));
							move();
							if (rl.getName().equals(locationName))
								return true;
						}						
					}
				}
				break; // done
			}
		}
		return false;
	}

	public void loadTrainIcon(){
		if (getCurrentLocation() != null)
			moveTrainIcon(getCurrentLocation());
	}
	
	private boolean animation = true;	// when true use animation for icon moves
	TrainIconAnimation _ta;
	/*
	 * rl = to the next route location for this train
	 */
	protected void moveTrainIcon(RouteLocation rl){
		_trainIconRl = rl;
		// create train icon if at departure or if program has been restarted
		if (rl == getTrainDepartsRouteLocation() || _trainIcon == null){
			createTrainIcon();
		}
		// is the lead engine still in train
		if (getLeadEngine() != null && getLeadEngine().getRouteDestination() == rl){
			log.debug("Engine ("+getLeadEngine().toString()+") arriving at destination "+rl.getName());			
		}
		if (_trainIcon != null && _trainIcon.isActive()){
			setTrainIconColor();
			_trainIcon.setShowTooltip(true);
            String txt = null;
			if (getCurrentLocationName().equals(""))
				txt= getDescription() + " "+TERMINATED+" ("+ getTrainTerminatesName()+")";
			else
				txt = getDescription() + " at " + getCurrentLocationName() + " next "+getNextLocationName();
            _trainIcon.getTooltip().setText(txt);
            _trainIcon.getTooltip().setBackgroundColor(Color.white);
			if (rl != null){
				if (rl.getTrainIconX()!=0 || rl.getTrainIconY()!=0){
					if (animation){
						TrainIconAnimation ta = new TrainIconAnimation(_trainIcon, rl, _ta);
						ta.start();	// start the animation
						_ta = ta;
					} else {
						_trainIcon.setLocation(rl.getTrainIconX(), rl.getTrainIconY());
					}
				}
			}
		} 
	}
	
	public String getIconName(){
		String name = getName();
		if (isBuilt() && getLeadEngine() != null && Setup.isTrainIconAppendEnabled())
			name += " "+ getLeadEngine().getNumber();
		return name;
	}
		
	/**
	 * Gets the lead engine, will create it if the program has been restarted
	 * @return lead engine for this train
	 */
	public Engine getLeadEngine(){
		if (_leadEngine == null  && !_leadEngineId.equals("")){
			_leadEngine = EngineManager.instance().getById(_leadEngineId);
		}
		return _leadEngine;
	}
	
	public void setLeadEngine(Engine engine){
		if (engine == null)
			_leadEngineId = "";
		_leadEngine = engine;
	}
	
	protected TrainIcon _trainIcon = null;
	public TrainIcon getTrainIcon(){
		return _trainIcon;
	}
	
	public void createTrainIcon() {
		if (_trainIcon != null && _trainIcon.isActive()) {
			_trainIcon.remove();
			_trainIcon.dispose();
		}
		Editor editor = PanelMenu.instance().getEditorByName(Setup.getPanelName());
		if (editor != null) { 
            _trainIcon = editor.addTrainIcon(getIconName());
			_trainIcon.setTrain(this);
			if (getIconName().length() > 9) {
				_trainIcon.setFont(jmri.util.FontUtil.deriveFont(_trainIcon.getFont(), 8.f));
			}
			_trainIcon.setLocation(getCurrentLocation().getTrainIconX(), getCurrentLocation().getTrainIconY());
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
					_trainIcon.setRosterEntry(entry);
					if(getLeadEngine().getConsist() != null)
						_trainIcon.setConsistNumber(getLeadEngine().getConsist().getConsistNumber());
				} else{
					log.debug("Loco roster entry not found for train ("+getName()+")");
				}
			}
		}
	}

	private void setTrainIconColor(){
		// Terminated train?
		if (getCurrentLocationName().equals("")){
			_trainIcon.setLocoColor(Setup.getTrainIconColorTerminate());
			return;
		}
		// local train?
		if (getRoute().getLocationsBySequenceList().size()==1){
			_trainIcon.setLocoColor(Setup.getTrainIconColorLocal());
			return;
		}
		// set color based on train direction at current location
		if (_trainIconRl.getTrainDirection() == RouteLocation.NORTH)
			_trainIcon.setLocoColor(Setup.getTrainIconColorNorth());
		if (_trainIconRl.getTrainDirection() == RouteLocation.SOUTH)
			_trainIcon.setLocoColor(Setup.getTrainIconColorSouth());
		if (_trainIconRl.getTrainDirection() == RouteLocation.EAST)
			_trainIcon.setLocoColor(Setup.getTrainIconColorEast());
		if (_trainIconRl.getTrainDirection() == RouteLocation.WEST)
			_trainIcon.setLocoColor(Setup.getTrainIconColorWest());
	}
	
	LocationManager locationManager = LocationManager.instance();

	private void updateStatus(RouteLocation old, RouteLocation next){
		if (next != null){
			setStatus(TRAININROUTE+" "+getNumberCarsInTrain()+" "+rb.getString("cars")
					+" "+getTrainLength()+" "+rb.getString("feet")
					+", "+getTrainWeight()+" "+rb.getString("tons"));
			// run move scripts
			runScripts(getMoveScripts());
		}else{
			log.debug("Train ("+getName()+") terminated");
			setStatus(TERMINATED);
			setBuilt(false);
			// run termination scripts
			runScripts(getTerminationScripts());
		}
	}
	
	/**
	 * Sets the print status for this location's switch list
	 * @param status UNKNOWN PRINTED
	 */
	public void setSwitchListStatus(String status){
		String old = _switchListStatus;
		_switchListStatus = status;
		if (!old.equals(status))
			firePropertyChange("switch list train status", old, status);
	}
	
	public String getSwitchListStatus(){
		return _switchListStatus;
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
		if (_trainIcon != null && _trainIcon.isActive()) {
			_trainIcon.remove();
			_trainIcon.dispose();
		}
		return true;
	}

    public void dispose(){
    	if (getRoute() != null)
    		getRoute().removePropertyChangeListener(this);
    	CarRoads.instance().removePropertyChangeListener(this);
		CarTypes.instance().removePropertyChangeListener(this);
		EngineTypes.instance().removePropertyChangeListener(this);
		CarOwners.instance().removePropertyChangeListener(this);
		//if (_reporter != null)
		//	InstanceManager.reporterManagerInstance().deregister(_reporter);
			
    	firePropertyChange (DISPOSE_CHANGED_PROPERTY, null, "Dispose");
    }
  

/**
 * creates a JMRI Reporter for the Train through which it can exchange updates
 * with other applications and scripts.
 * 
 * This method first requests a Recorder with a user name constructed from the
 * train name.  If one is not found, it creates an Internal Recorder.	
 */
    /*
    private void constructReporter() {
    	if (Setup.isCreateReportersEnabled() && _reporter == null) {
    		ReporterManager rm = InstanceManager.reporterManagerInstance();
    		Reporter r = null;
    		String uName = USER_PREFIX + getName();
    		if (rm == null) {
    			if (log.isDebugEnabled()) log.debug("JMRI reporter manager was not created.");
    		}
    		else if ((r = rm.getByUserName(uName)) == null) {
    			if (log.isDebugEnabled()) log.debug("JMRI reporter for train (" +
    					getName() + ") was not found, creating one!");
    			String sName = SYSTEM_PREFIX + getName();
    			r = rm.newReporter(sName, uName);
    		}
    		if (r != null) {
    			r.addPropertyChangeListener(new PropertyChangeListener() {
    				public void propertyChange(PropertyChangeEvent change) {
    					log.debug("Train ("+getName()+ ") sees reporter property change: "+change.getPropertyName());
    					if (change.getPropertyName().equals("currentReport")){
    						String operation = ((String) _reporter.getCurrentReport()).trim();
    						String str;
    						if (operation != null) {
    							if (operation.startsWith(LOCATION)) {
    								str = operation.substring(LOCATION.length());
    								if (str != null)
    									if (move(str)){
    										// this is not the right place to report on changes because
    										// of possible recursion, but I don't know where else to put
    										// it.  It should be queued to be run after processing of the
    										// action completes.
    										str = LENGTH + String.valueOf(getTrainLength()) + " | "
    												+ TONNAGE + String.valueOf(getTrainWeight());
    										_reporter.setComment(str);
    									} else {
    										str = "Unable to move train to location "+str;
    										_reporter.setComment(str);
    									}
    							}
    							else if (BUILD.equals(operation)) {
    								build();
    								_reporter.setComment(getStatus());
    							}
    							else if (TERMINATE.equals(operation)) {
    								terminate();
    								_reporter.setComment(getStatus());
    							}
    							else if (RESET.equals(operation)) {
    								reset();
    								_reporter.setComment(getStatus());
    							}
    							else {
    								_reporter.setComment("Command not known");
    							}
    						} 
    					}
    				}
    			});
    		}
    		_reporter = r;
    	}
    }
    */
    
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
    	if ((a = e.getAttribute("carLoadOption")) != null )  _loadOption = a.getValue();
    	if ((a = e.getAttribute("carOwnerOption")) != null )  _ownerOption = a.getValue();
    	if ((a = e.getAttribute("builtStartYear")) != null )  _builtStartYear = a.getValue();
    	if ((a = e.getAttribute("builtEndYear")) != null )  _builtEndYear = a.getValue();
    	if ((a = e.getAttribute("carLoads")) != null ) {
    		String names = a.getValue();
    		String[] loads = names.split("%%");
    		if (log.isDebugEnabled()) log.debug("Train (" +getName()+ ") " +getLoadOption()+  " car loads: "+ names);
    		setLoadNames(loads);
    	}
    	if ((a = e.getAttribute("carOwners")) != null ) {
    		String names = a.getValue();
    		String[] owners = names.split("%%");
    		if (log.isDebugEnabled()) log.debug("Train (" +getName()+ ") " +getOwnerOption()+  " car owners: "+ names);
    		setOwnerNames(owners);
    	}
    	if ((a = e.getAttribute("numberEngines")) != null)
    		_numberEngines = a.getValue();
    	if ((a = e.getAttribute("leg2Engines")) != null)
    		_leg2Engines = a.getValue();
    	if ((a = e.getAttribute("leg3Engines")) != null)
    		_leg3Engines = a.getValue();
    	if ((a = e.getAttribute("engineRoad")) != null)
    		_engineRoad = a.getValue();
    	if ((a = e.getAttribute("leg2Road")) != null)
    		_leg2Road = a.getValue();
    	if ((a = e.getAttribute("leg3Road")) != null)
    		_leg3Road = a.getValue();
    	if ((a = e.getAttribute("engineModel")) != null)
    		_engineModel = a.getValue();
    	if ((a = e.getAttribute("leg2Model")) != null)
    		_leg2Model = a.getValue();
    	if ((a = e.getAttribute("leg3Model")) != null)
    		_leg3Model = a.getValue();
    	if ((a = e.getAttribute("requires")) != null)
    		_requires = Integer.parseInt(a.getValue());
    	if ((a = e.getAttribute("cabooseRoad")) != null)
    		_cabooseRoad = a.getValue();
    	if ((a = e.getAttribute("leg2CabooseRoad")) != null)
    		_leg2CabooseRoad = a.getValue();
    	if ((a = e.getAttribute("leg3CabooseRoad")) != null)
    		_leg3CabooseRoad = a.getValue();
    	if ((a = e.getAttribute("leg2Options")) != null)
    		_leg2Options = Integer.parseInt(a.getValue());
    	if ((a = e.getAttribute("leg3Options")) != null)
    		_leg3Options = Integer.parseInt(a.getValue());
    	if ((a = e.getAttribute("built")) != null)
    		_built = a.getValue().equals("true");
    	if ((a = e.getAttribute("build")) != null)
    		_build = a.getValue().equals("true");
    	if ((a = e.getAttribute("buildFailed")) != null)
    		_buildFailed = a.getValue().equals("true");
    	if ((a = e.getAttribute("printed")) != null)
    		_printed = a.getValue().equals("true");
    	if ((a = e.getAttribute("switchListStatus")) != null)
    		_switchListStatus = a.getValue();
    	if ((a = e.getAttribute("leadEngine")) != null)
    		_leadEngineId = a.getValue();
    	if ((a = e.getAttribute("status")) != null )  _status = a.getValue();
    	if ((a = e.getAttribute("comment")) != null )  _comment = a.getValue();
    	if (_route != null){
    		if ((a = e.getAttribute("current")) != null) 		
    			_current = _route.getLocationById(a.getValue());
    		if ((a = e.getAttribute("leg2Start")) != null) 		
    			_leg2Start = _route.getLocationById(a.getValue());
    		if ((a = e.getAttribute("leg3Start")) != null) 		
    			_leg3Start = _route.getLocationById(a.getValue());
       		if ((a = e.getAttribute("leg2End")) != null) 		
    			_end2Leg = _route.getLocationById(a.getValue());
    		if ((a = e.getAttribute("leg3End")) != null) 		
    			_leg3End = _route.getLocationById(a.getValue());
    	}
    	// check for scripts
    	if (e.getChild("scripts") != null){
    		@SuppressWarnings("unchecked")
    		List<Element> lb = e.getChild("scripts").getChildren("build");
    		for (int i=0; i<lb.size(); i++){
    			Element es = lb.get(i);
    			if ((a = es.getAttribute("name")) != null ){
    				addBuildScript(a.getValue());
    			}
    		}
       		@SuppressWarnings("unchecked")
    		List<Element> lab = e.getChild("scripts").getChildren("afterBuild");
    		for (int i=0; i<lab.size(); i++){
    			Element es = lab.get(i);
    			if ((a = es.getAttribute("name")) != null ){
    				addAfterBuildScript(a.getValue());
    			}
    		}
    		@SuppressWarnings("unchecked")
    		List<Element> lm = e.getChild("scripts").getChildren("move");
    		for (int i=0; i<lm.size(); i++){
    			Element es = lm.get(i);
    			if ((a = es.getAttribute("name")) != null ){
    				addMoveScript(a.getValue());
    			}
    		}
    		@SuppressWarnings("unchecked")
    		List<Element> lt = e.getChild("scripts").getChildren("terminate");
    		for (int i=0; i<lt.size(); i++){
    			Element es = lt.get(i);
    			if ((a = es.getAttribute("name")) != null ){
    				addTerminationScript(a.getValue());
    			}
    		}
    	}
    	// check for optional railroad name and logo
        if ((e.getChild("railRoad") != null) && 
        		(a = e.getChild("railRoad").getAttribute("name"))!= null){
        	String name = a.getValue();
           	setRailroadName(name);
    	}
        if ((e.getChild("manifestLogo") != null)){ 
        	if((a = e.getChild("manifestLogo").getAttribute("name"))!= null){
        		setManifestLogoURL(a.getValue());
        	}
    	}
    	addPropertyChangeListerners();
    	//constructReporter();
    }
    
    private void addPropertyChangeListerners(){
		CarRoads.instance().addPropertyChangeListener(this);
		CarTypes.instance().addPropertyChangeListener(this);
		EngineTypes.instance().addPropertyChangeListener(this);
		CarOwners.instance().addPropertyChangeListener(this);
    }

    /**
     * Create an XML element to represent this Entry. This member has to remain synchronized with the
     * detailed DTD in operations-trains.dtd.
     * @return Contents in a JDOM Element
     */
    public Element store() {
        Element e = new Element("train");
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
        StringBuffer buf = new StringBuffer();
        for (int i=0; i<locationIds.length; i++){
        	buf.append(locationIds[i]+"%%");
        }
        e.setAttribute("skip", buf.toString());        
        if (getCurrentLocation() != null)
        	e.setAttribute("current", getCurrentLocation().getId());
    	e.setAttribute("carRoadOperation", getRoadOption());
    	e.setAttribute("carLoadOption", getLoadOption());	
    	e.setAttribute("carOwnerOption", getOwnerOption());	
    	e.setAttribute("builtStartYear", getBuiltStartYear());
    	e.setAttribute("builtEndYear", getBuiltEndYear());	
        e.setAttribute("numberEngines", getNumberEngines());
        e.setAttribute("engineRoad", getEngineRoad());
        e.setAttribute("engineModel", getEngineModel());
        e.setAttribute("requires", Integer.toString(getRequirements()));
        e.setAttribute("cabooseRoad", getCabooseRoad());
        e.setAttribute("built", isBuilt()?"true":"false");
        e.setAttribute("build", isBuildEnabled()?"true":"false");
        e.setAttribute("buildFailed", getBuildFailed()?"true":"false");
        e.setAttribute("printed", getPrinted()?"true":"false");
        e.setAttribute("switchListStatus", getSwitchListStatus());
        if(getLeadEngine()!= null)
        	e.setAttribute("leadEngine", getLeadEngine().getId());
        e.setAttribute("status", getStatus());
        e.setAttribute("comment", getComment());
        // build list of car types for this train
        String[] types = getTypeNames();
        buf = new StringBuffer();
        for (int i=0; i<types.length; i++){
       		// remove types that have been deleted by user
    		if (CarTypes.instance().containsName(types[i]) || EngineTypes.instance().containsName(types[i]))
    			buf.append(types[i]+"%%");
        }
        e.setAttribute("carTypes", buf.toString());
      	// save list of car roads for this train
        if (!getRoadOption().equals(ALLROADS)){
        	String[] roads = getRoadNames();
        	buf = new StringBuffer();
        	for (int i=0; i<roads.length; i++){
        		buf.append(roads[i]+"%%");
        	}
        	e.setAttribute("carRoads", buf.toString());
        }
        // save list of car loads for this train
        if (!getLoadOption().equals(ALLLOADS)){
        	String[] loads = getLoadNames();
        	buf = new StringBuffer();
        	for (int i=0; i<loads.length; i++){
        		buf.append(loads[i]+"%%");
        	}
        	e.setAttribute("carLoads", buf.toString());
        }
        // save list of car owners for this train
        if (!getOwnerOption().equals(ALLOWNERS)){
        	String[] owners = getOwnerNames();
        	buf = new StringBuffer();
        	for (int i=0; i<owners.length; i++){
        		buf.append(owners[i]+"%%");
        	}
        	e.setAttribute("carOwners", buf.toString());
        }
        // save list of scripts for this train
        if (getBuildScripts().size()>0 || getAfterBuildScripts().size()>0 || getMoveScripts().size()>0 || getTerminationScripts().size()>0){
        	Element es = new Element("scripts");
        	if (getBuildScripts().size()>0){ 
        		for (int i=0; i<getBuildScripts().size(); i++){
        			Element em = new Element("build");
        			em.setAttribute("name", getBuildScripts().get(i));
        			es.addContent(em);
        		}
        	}
        	if (getAfterBuildScripts().size()>0){ 
        		for (int i=0; i<getAfterBuildScripts().size(); i++){
        			Element em = new Element("afterBuild");
        			em.setAttribute("name", getAfterBuildScripts().get(i));
        			es.addContent(em);
        		}
        	}
        	if (getMoveScripts().size()>0){ 
        		for (int i=0; i<getMoveScripts().size(); i++){
        			Element em = new Element("move");
        			em.setAttribute("name", getMoveScripts().get(i));
        			es.addContent(em);
        		}
        	}
        	// save list of termination scripts for this train
        	if (getTerminationScripts().size()>0){
        		for (int i=0; i<getTerminationScripts().size(); i++){
        			Element et = new Element("terminate");
        			et.setAttribute("name", getTerminationScripts().get(i));
        			es.addContent(et);
        		}
        	}
        	e.addContent(es);
        }
        if (!getRailroadName().equals("")){
        	Element r = new Element("railRoad");
        	r.setAttribute("name", getRailroadName());
        	e.addContent(r);
        }
        if (!getManifestLogoURL().equals("")){
        	Element l = new Element("manifestLogo");
        	l.setAttribute("name", getManifestLogoURL());
        	e.addContent(l);
        }
        if (getSecondLegOptions() != Train.NONE){
        	e.setAttribute("leg2Options", Integer.toString(getSecondLegOptions()));
        	e.setAttribute("leg2Engines", getSecondLegNumberEngines());
        	e.setAttribute("leg2Road", getSecondLegEngineRoad());
        	e.setAttribute("leg2Model", getSecondLegEngineModel());
        	e.setAttribute("leg2CabooseRoad", getSecondLegCabooseRoad());
           	if (getSecondLegStartLocation() != null)
        		e.setAttribute("leg2Start", getSecondLegStartLocation().getId());
           	if (getSecondLegEndLocation() != null)
        		e.setAttribute("leg2End", getSecondLegEndLocation().getId());
        }
        if (getThirdLegOptions() != Train.NONE){
        	e.setAttribute("leg3Options", Integer.toString(getThirdLegOptions()));
        	e.setAttribute("leg3Engines", getThirdLegNumberEngines());
        	e.setAttribute("leg3Road", getThirdLegEngineRoad());
        	e.setAttribute("leg3Model", getThirdLegEngineModel());
        	e.setAttribute("leg3CabooseRoad", getThirdLegCabooseRoad());
        	if (getThirdLegStartLocation() != null)
        		e.setAttribute("leg3Start", getThirdLegStartLocation().getId());
          	if (getThirdLegEndLocation() != null)
        		e.setAttribute("leg3End", getThirdLegEndLocation().getId());
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
    	if (e.getPropertyName().equals(CarTypes.CARTYPES_NAME_CHANGED_PROPERTY) ||
    			e.getPropertyName().equals(EngineTypes.ENGINETYPES_NAME_CHANGED_PROPERTY)){
    		replaceType((String)e.getOldValue(), (String)e.getNewValue());
    	}
       	if (e.getPropertyName().equals(CarRoads.CARROADS_NAME_CHANGED_PROPERTY)){
    		replaceRoad((String)e.getOldValue(), (String)e.getNewValue());
    	}
       	if (e.getPropertyName().equals(CarOwners.CAROWNERS_NAME_CHANGED_PROPERTY)){
    		replaceOwner((String)e.getOldValue(), (String)e.getNewValue());
    	}
       	// forward route departure time property changes
       	if (e.getPropertyName().equals(RouteLocation.DEPARTURE_TIME_CHANGED_PROPERTY)){
       		firePropertyChange(DEPARTURETIME_CHANGED_PROPERTY, e.getOldValue(), e.getNewValue());
       	}
    	// forward any property changes in this train's route
       	if (e.getSource().getClass().equals(Route.class))
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
