package jmri.jmrit.operations.router;

import java.util.ArrayList;
import java.util.List;

import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.LocationTrackPair;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;

/**
 * Router for car movement. This code attempts to find a way to move a car to
 * its final destination through the use of two or more trains. Code first tries
 * to move car using a single train. If that fails, attempts are made to use two
 * trains via an interchange track , then a yard. Next attempts are made using
 * three or more trains using any combination of interchanges and yards.
 * Currently the router is limited to five trains.
 * 
 * @author Daniel Boudreau Copyright (C) 2010
 * @version $Revision: 1.4 $
 */

public class Router {
	
	private List<LocationTrackPair> firstLocationTrackPairs = new ArrayList<LocationTrackPair>();
	private List<LocationTrackPair> lastLocationTrackPairs = new ArrayList<LocationTrackPair>();
	private List<LocationTrackPair> otherLocationTrackPairs = new ArrayList<LocationTrackPair>();
	
	TrainManager trainManager = TrainManager.instance();
	
	/** record the single instance **/
	private static Router _instance = null;
	
	public static synchronized Router instance() {
		if (_instance == null) {
			if (log.isDebugEnabled()) log.debug("Router creating instance");
			// create and load
			_instance = new Router();
		}
		if (Control.showInstance && log.isDebugEnabled()) log.debug("Router returns instance "+_instance);
		return _instance;
	}
	
	public boolean setCarNextDestination(Car car){
		if (car.getNextDestination() != null){
			log.debug("Car ("+car.toString()+") has next destination ("+car.getNextDestination().getName()+") car routing begins");
			// Has the car accidentally arrived at the car's next destination?
			if (car.getLocation().equals(car.getNextDestination()) && car.getTrack().equals(car.getNextDestTrack())){
				log.debug("Car ("+car.toString()+") has arrieved at next destination");
				car.setNextDestination(null);
				car.setNextDestTrack(null);
				return true;
			}
			String newStatus = car.setDestination(car.getNextDestination(), car.getNextDestTrack());
			if (!newStatus.equals(Car.OKAY)){
				String trk = "null";
				if (car.getNextDestTrack() != null)
					trk = car.getNextDestTrack().getName();
				log.warn("Next destination ("+car.getNextDestination().getName()+", "+trk+") failed for car ("+car.toString()+") due to "+newStatus);
			}
			// check to see if car will move with new destination and a single train
			if (TrainManager.instance().getTrainForCar(car) != null){
				log.debug("Car ("+car.toString()+") destination ("+car.getDestinationName()+", "+car.getDestinationTrackName()+") can be serviced by a single train");
				car.setNextDestination(null);
				car.setNextDestTrack(null);
				return true;
			} else if (Setup.isCarRoutingEnabled()) {
				log.debug("Car ("+car.toString()+") next destination ("+car.getNextDestination().getName()+") is not served by a single train");
				firstLocationTrackPairs.clear();
				lastLocationTrackPairs.clear();
				otherLocationTrackPairs.clear();
				// first try using 2 trains and an interchange track to route the car
				if (setCarDestinationInterchange(car)){
					log.debug("Was able to set route via interchange for car ("+car.toString()+")");
				// now try 2 trains and a yard track
				} else if (setCarDestinationYard(car)){
					log.debug("Was able to set route via yard for car ("+car.toString()+")");
				// now try 3 or more trains to route car
				} else if (setCarDestinationMultipleTrains(car)){
					log.debug("Was able to set multiple train route for car ("+car.toString()+")");
				} else {
					log.debug("Wasn't able to set route for car ("+car.toString()+")");
					return false;	// maybe next time
				}
			} else {
				log.warn("Car ("+car.toString()+") next destination ("+car.getNextDestination().getName()+") is not served directly by any train");
				car.setNextDestination(null);
				car.setNextDestTrack(null);
			}
		}
		return true; // car's destination has been set
	}

	/**
	 * Sets a car's next destination to an interchange track if two trains can
	 * route the car.
	 * 
	 * @param car the car to be routed
	 * @return true if car's destination has been modified to an interchange.
	 *         False if an interchange track wasn't found that could service the
	 *         car's final destination.
	 */
	public boolean setCarDestinationInterchange(Car car){
		return setCarDestinationTwoTrains (car, Track.INTERCHANGE);
	}
	
	/**
	 * Sets a car's next destination to a yard track if two train can route the
	 * car.
	 * 
	 * @param car the car to be routed
	 * @return true if car's destination has been modified to a yard. False if a
	 *         yard track wasn't found that could service the car's final
	 *         destination.
	 */
	public boolean setCarDestinationYard(Car car){
		return setCarDestinationTwoTrains (car, Track.YARD);
	}
	
	private boolean debugFlag = false;
	
	private boolean setCarDestinationTwoTrains(Car car, String type){
		if (!Setup.isCarRoutingEnabled()){
			log.debug("Car routing is disabled");
			return false;
		}
		log.debug("Find an "+type+" track for car ("+car.toString()+") final destination ("+car.getDestinationName()+", "+car.getDestinationTrackName()+")");
		// create a test car for routing and copy the important stuff
		Car ts = clone(car);
				
		// save car's location, track, destination, and destination track
		Location saveLocation = car.getLocation();
		Track saveTrack = car.getTrack();
		Location saveDestation = car.getDestination();
		Track saveDestTrack = car.getDestinationTrack();
		
		// setup the test car with the save location and destination
		if (saveTrack == null){
			log.debug("Car's track is null! Can't route");
			return false;
		}
		// now search for a yard or interchange that a train can pickup and deliver the car to its destination
		List<String> locations = LocationManager.instance().getLocationsByIdList();
		for (int i=0; i<locations.size(); i++){
			Location location = LocationManager.instance().getLocationById(locations.get(i));
			List<String> tracks = location.getTracksByNameList(type);	// restrict to yards or interchanges
			for (int j=0; j<tracks.size(); j++){
				Track track = location.getTrackById(tracks.get(j));
				if (ts.testLocation(location, track).equals(Car.OKAY)){
					if (debugFlag)
						log.debug("Found "+type+" track ("+location.getName()+", "+track.getName()+") for car ("+car.toString()+")");
					// test to see if there's a train that can deliver the car to its final location
					ts.setLocation(location);
					ts.setTrack(track);
					Train nextTrain = trainManager.getTrainForCar(ts);
					if (nextTrain != null){
						if (debugFlag)
							log.debug("Train ("+nextTrain.getName()+") can service car ("+car.toString()+") from "+type+" ("+ts.getLocationName()+", "+ts.getTrackName()+") to final destination ("+ts.getDestinationName()+", "+ts.getDestinationTrackName()+")");
						lastLocationTrackPairs.add(new LocationTrackPair(location, track));
						ts.setLocation(saveLocation); // restore car's location and track
						ts.setTrack(saveTrack);	
						ts.setDestination(location); // forward car to this destination and track.
						ts.setDestinationTrack(track);
						// test to see if car can be transported from current location to this yard or interchange
						Train firstTrain = trainManager.getTrainForCar(ts);
						String status = ts.testDestination(location, track);
						// restore car's destination
						ts.setDestination(saveDestation);
						ts.setDestinationTrack(saveDestTrack);
						if (status.equals(Car.OKAY) && firstTrain != null){
							car.setDestination(location, track);
							if (debugFlag)
								log.debug("Train ("+firstTrain.getName()+") can service car ("+car.toString()+") from current location ("+car.getLocationName()+", "+car.getTrackName()+") to "+type+" ("+car.getDestinationName()+", "+car.getDestinationTrackName()+")");
							log.info("Route for car ("+car.toString()+") "+car.getLocationName()+"->"+car.getDestinationName()+"->"+car.getNextDestination().getName());
							return true;
						}
					} else if (debugFlag){
						log.debug("Could not find a train to service car from "+type+" ("+location.getName()+", "+track.getName()+") to destination ("+car.getDestinationName()+")");
					}

				}
			}
		}
		return false;	// couldn't find two trains to service this car
	}

	// note that last location track pairs was loaded by setCarDestinationTwoTrains
	private boolean setCarDestinationMultipleTrains(Car car){
		if (lastLocationTrackPairs.size()== 0)
			return false;
		log.debug("Multiple train routing begins");
		log.debug("Try to find route using 2 trains");
		Car ts = clone(car);
		List<String> locations = LocationManager.instance().getLocationsByIdList();
		for (int i=0; i<locations.size(); i++){
			Location location = LocationManager.instance().getLocationById(locations.get(i));
			List<String> tracks = location.getTracksByNameList(null);	
			for (int j=0; j<tracks.size(); j++){
				Track track = location.getTrackById(tracks.get(j));
				if ((track.getLocType().equals(Track.INTERCHANGE) ||
						track.getLocType().equals(Track.YARD)) 
						&& ts.testLocation(location, track).equals(Car.OKAY)){
					if (debugFlag)
						log.debug("Found "+track.getLocType()+" track ("+location.getName()+", "+track.getName()+") for car ("+car.toString()+")");
					// test to see if there's a train that can deliver the car to this location
					ts.setDestination(location);
					ts.setDestinationTrack(track);
					Train firstTrain = trainManager.getTrainForCar(ts);
					if (firstTrain != null){
						if (debugFlag)
							log.debug("Train ("+firstTrain.getName()+") can service car ("+car.toString()+") from "+track.getLocType()+" ("+ts.getLocationName()+", "+ts.getTrackName()+") to next destination ("+ts.getDestinationName()+", "+ts.getDestinationTrackName()+")");
						firstLocationTrackPairs.add(new LocationTrackPair(location, track));
					} else {
						// don't add to other if already in last location list
						boolean match = false;
						for (int k=0; k<lastLocationTrackPairs.size(); k++){
							LocationTrackPair ltp = lastLocationTrackPairs.get(k);
							if (ltp.getLocation().equals(location)){
								match = true;
								break;
							}
						} if (!match){
							if (debugFlag)
								log.debug("Adding location ("+location.getName()+", "+track.getName()+") to other locations");
							otherLocationTrackPairs.add(new LocationTrackPair(location, track));
						}
					}
				}
			}
		}
		// location track pairs that could be the very next destination for the car
		for (int i=0; i<firstLocationTrackPairs.size(); i++){
			LocationTrackPair ltp = firstLocationTrackPairs.get(i);
			log.debug("First location ("+ltp.getLocation().getName()+", "+ltp.getTrack().getName()+") can service car ("+car.toString()+")");
		}
		// location track pairs that could be the next to last destination for the car
		for (int i=0; i<lastLocationTrackPairs.size(); i++){
			LocationTrackPair ltp = lastLocationTrackPairs.get(i);
			log.debug("Last location ("+ltp.getLocation().getName()+", "+ltp.getTrack().getName()+") can service car ("+car.toString()+")");
		}
		// location track pairs that are not the first or the last
		for (int i=0; i<otherLocationTrackPairs.size(); i++){
			LocationTrackPair ltp = otherLocationTrackPairs.get(i);
			log.debug("Other location ("+ltp.getLocation().getName()+", "+ltp.getTrack().getName()+") may be needed to service car ("+car.toString()+")");
		}
		if (firstLocationTrackPairs.size()>0){
			log.debug("Try to find route using 3 trains");
			for (int i=0; i<firstLocationTrackPairs.size(); i++){
				LocationTrackPair fltp = firstLocationTrackPairs.get(i);
				ts.setLocation(fltp.getLocation()); // set car to this location and track
				ts.setTrack(fltp.getTrack());
				for (int j=0; j<lastLocationTrackPairs.size(); j++){
					LocationTrackPair lltp = lastLocationTrackPairs.get(j);
					ts.setDestination(lltp.getLocation());	// set car to this destination and track
					ts.setDestinationTrack(lltp.getTrack());
					// does a train service these two locations?
					Train middleTrain = trainManager.getTrainForCar(ts);
					if (middleTrain != null){
						log.debug("Found 3 train route, setting car destination ("+ts.getLocationName()+", "+ts.getTrackName()+")");			
						String status = car.setDestination(ts.getLocation(), ts.getTrack());
						if (status.equals(Car.OKAY)){
							log.info("Route for car ("+car.toString()+") "+car.getLocationName()+"->"+car.getDestinationName()+"->"+ts.getDestinationName()+"->"+car.getNextDestination().getName());
							return true;	// done 3 train routing
						} else {
							log.debug("Could not set car ("+car.toString()+") destination");
						}
					}
				}
			}
			log.debug("Using 3 trains to route car was unsuccessful");
			log.debug("Try to find route using 4 trains");
			for (int i=0; i<firstLocationTrackPairs.size(); i++){
				LocationTrackPair fltp = firstLocationTrackPairs.get(i);			
				for (int j=0; j<otherLocationTrackPairs.size(); j++){
					LocationTrackPair mltp = otherLocationTrackPairs.get(j);
					ts.setLocation(fltp.getLocation()); // set car to this location and track
					ts.setTrack(fltp.getTrack());
					ts.setDestination(mltp.getLocation()); // set car to this destination and track
					ts.setDestinationTrack(mltp.getTrack());
					// does a train service these two locations?
					Train middleTrain2 = trainManager.getTrainForCar(ts);
					if (middleTrain2 != null){
						log.debug("Train 2 ("+middleTrain2.getName()+") services car from "+ts.getLocationName()+" to "+ts.getDestinationName()+", "+ts.getDestinationTrackName());										
						for (int k=0; k<lastLocationTrackPairs.size(); k++){
							LocationTrackPair lltp = lastLocationTrackPairs.get(k);
							ts.setLocation(mltp.getLocation()); // set car to this location and track
							ts.setTrack(mltp.getTrack());
							ts.setDestination(lltp.getLocation()); // set car to this destination and track
							ts.setDestinationTrack(lltp.getTrack());
							Train middleTrain3 = trainManager.getTrainForCar(ts);
							if (middleTrain3 != null){
								log.debug("Train 3 ("+middleTrain3.getName()+") services car from "+ts.getLocationName()+" to "+ts.getDestinationName()+", "+ts.getDestinationTrackName());
								log.debug("Found 4 train route, setting car destination ("+fltp.getLocation().getName()+", "+fltp.getTrack().getName()+")");			
								String status = car.setDestination(fltp.getLocation(),fltp.getTrack());
								if (status.equals(Car.OKAY)){
									log.info("Route for car ("+car.toString()+") "+car.getLocationName()+"->"+car.getDestinationName()+"->"+mltp.getLocation().getName()+"->"+lltp.getLocation().getName()+"->"+car.getNextDestination().getName());
									return true;	// done 4 train routing
								} else {
									log.debug("Could not set car ("+car.toString()+") destination");
								}
							}
						}			
					}
				}
			}
			log.debug("Using 4 trains to route car was unsuccessful");
			log.debug("Try to find route using 5 trains");
			for (int i=0; i<firstLocationTrackPairs.size(); i++){
				LocationTrackPair fltp = firstLocationTrackPairs.get(i);			
				for (int j=0; j<otherLocationTrackPairs.size(); j++){
					LocationTrackPair mltp1 = otherLocationTrackPairs.get(j);
					ts.setLocation(fltp.getLocation()); // set car to this location and track
					ts.setTrack(fltp.getTrack());
					ts.setDestination(mltp1.getLocation()); // set car to this destination and track
					ts.setDestinationTrack(mltp1.getTrack());
					// does a train service these two locations?
					Train middleTrain2 = trainManager.getTrainForCar(ts);
					if (middleTrain2 != null){		
						log.debug("Train 2 ("+middleTrain2.getName()+") services car from "+ts.getLocationName()+" to "+ts.getDestinationName()+", "+ts.getDestinationTrackName());										
						for (int k=0; k<otherLocationTrackPairs.size(); k++){
							LocationTrackPair mltp2 = otherLocationTrackPairs.get(k);
							ts.setLocation(mltp1.getLocation()); // set car to this location and track
							ts.setTrack(mltp1.getTrack());
							ts.setDestination(mltp2.getLocation()); // set car to this destination and track
							ts.setDestinationTrack(mltp2.getTrack());
							// does a train service these two locations?
							Train middleTrain3 = trainManager.getTrainForCar(ts);
							if (middleTrain3 != null){
								log.debug("Train 3 ("+middleTrain3.getName()+") services car from "+ts.getLocationName()+" to "+ts.getDestinationName()+", "+ts.getDestinationTrackName());										
								for (int n=0; n<lastLocationTrackPairs.size(); n++){
									LocationTrackPair lltp = lastLocationTrackPairs.get(n);
									ts.setLocation(mltp2.getLocation()); // set car to this location and track
									ts.setTrack( mltp2.getTrack());
									ts.setDestination(lltp.getLocation()); // set car to this destination and track
									ts.setDestinationTrack(lltp.getTrack());
									// does a train service these two locations?
									Train middleTrain4 = trainManager.getTrainForCar(ts);
									if (middleTrain4 != null){
										log.debug("Train 4 ("+middleTrain4.getName()+") services car from "+ts.getLocationName()+" to "+ts.getDestinationName()+", "+ts.getDestinationTrackName());										
										log.debug("Found 5 train route, setting car destination ("+fltp.getLocation().getName()+", "+fltp.getTrack().getName()+")");			
										String status = car.setDestination(fltp.getLocation(),fltp.getTrack());
										if (status.equals(Car.OKAY)){
											log.info("Route for car ("+car.toString()+") "+car.getLocationName()+"->"+car.getDestinationName()+"->"+mltp1.getLocation().getName()+"->"+mltp2.getLocation().getName()+"->"+lltp.getLocation().getName()+"->"+car.getNextDestination().getName());
											return true;	// done 4 train routing
										} else {
											log.debug("Could not set car ("+car.toString()+") destination");
										}
									}
								}
							}
						}			
					}
				}
			}
			log.debug("Using 5 trains to route car was unsuccessful");
		}
		return false;
	}
	
	private Car clone(Car car){
		Car ts = new Car();
		ts.setBuilt(car.getBuilt());
		ts.setLength(car.getLength());
		ts.setLoad(car.getLoad());
		ts.setNumber(car.getNumber());
		ts.setOwner(car.getOwner());
		ts.setRoad(car.getRoad());
		ts.setType(car.getType());
		ts.setLocation(car.getLocation());
		ts.setTrack(car.getTrack());
		ts.setDestination(car.getDestination());
		ts.setDestinationTrack(car.getDestinationTrack());
		return ts;
	}
	
	
	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Router.class.getName());


}
