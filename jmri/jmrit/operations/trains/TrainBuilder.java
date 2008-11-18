package jmri.jmrit.operations.trains;

import java.awt.Font;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;

import jmri.jmrit.XmlFile;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.cars.Kernel;
import jmri.jmrit.operations.rollingstock.engines.Consist;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.routes.RouteManager;

import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;



import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.setup.Control;

import jmri.util.davidflanagan.HardcopyWriter;
import jmri.jmrit.display.PanelMenu;
import jmri.jmrit.display.PanelEditor;
import jmri.jmrit.display.LayoutEditor;
import jmri.jmrit.display.LocoIcon;


import org.jdom.Element;

/**
 * Utilities to build trains and move them. 
 * 
 * @author Daniel Boudreau  Copyright (C) 2008
 * @version             $Revision: 1.16 $
 */
public class TrainBuilder extends TrainCommon{
	
	static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.trains.JmritOperationsTrainsBundle");

	// build status
	private static final String BUILDFAILED = rb.getString("BuildFailed");
	private static final String BUILDING = rb.getString("Building");
	private static final String BUILT = rb.getString("Built") + " ";
	private static final String PARTIALBUILT = rb.getString("Partial") + " ";
	private static final String FEET = Setup.FEET;
	private static final String BOX = " [ ] ";
	
	// build variables shared between local routines
	Train train;		// the train being built
	int numberCars;		// how many cars are moved by this train
	int numberEngines;	// the number of engines assigned to this train
	int carIndex;		// index for carList
	List carList;		// list of cars available for this train
	List routeList;		// list of locations from departure to termination served by this train
	int moves;			// the number of pickup car moves for a location
	double maxWeight;	// the maximum weight of cars in train
	int reqNumOfMoves;	// the requested number of car moves for a location
	Location departLocation;	// train departs this location
	Location terminateLocation; // train terminate at this location
	boolean success;	// true when enough cars have been picked up from a location
	
	// managers 
	CarManager carManager = CarManager.instance();
	LocationManager locationManager = LocationManager.instance();
	EngineManager engineManager = EngineManager.instance();
		
	/**
	 * Build rules:
	 * 1. Need at least one location in route to build train
	 * 2. Select only engines and cars the that train can service
	 * 3. Optional, train must depart with the required number of moves (cars)
	 * 4. Add caboose or car with FRED to train if required
	 * 5. All cars and engines must leave stagging tracks
	 * 6. If a train is assigned to stagging, all cars and engines must go there  
	 * 7. Service locations based on train direction, location car types and roads
	 * 8. Ignore train/track direction when servicing the last location in a route
	 * 9. Ignore track direction when train is a local (serves one location)
	 *
	 */
	public void build(Train train){
		this.train = train;
		train.setStatus(BUILDING);
		train.setBuilt(false);
		train.setLeadEngine(null);
		numberCars = 0;
		maxWeight = 0;
		
		// create build status file
		File file = TrainManagerXml.instance().createTrainBuildReportFile(train.getName());
		PrintWriter fileOut = null;

		try {
			fileOut = new PrintWriter(new BufferedWriter(new FileWriter(file)),
					true);
		} catch (IOException e) {
			log.error("can not open build status file");
			return;
		}

		Date now = new Date();
		addLine(fileOut, "Build report for train ("+train.getName()+") built on "+now);
		
		if (train.getRoute() == null){
			buildFailed(fileOut, "ERROR Can't build train ("+train.getName()+"), needs a route");
			return;
		}
		routeList = train.getRoute().getLocationsBySequenceList();
		if (routeList.size() < 1){
			buildFailed(fileOut, "ERROR Route needs at least one location to build train ("+train.getName()+")");
			return;
		}
		// train departs
		departLocation = locationManager.getLocationByName(train.getTrainDepartsName());
		if (departLocation == null){
			buildFailed(fileOut, "ERROR Route departure location missing for train ("+train.getName()+")");
			return;
		}
		// train terminates
		terminateLocation = locationManager.getLocationByName(train.getTrainTerminatesName());
		if (terminateLocation == null){
			buildFailed(fileOut, "ERROR Route terminate location missing for train ("+train.getName()+")");
			return;
		}
		// TODO: DAB control minimal build by each train
		if (train.getTrainDepartsRouteLocation().getMaxCarMoves() > departLocation.getNumberRS() && Control.fullTrainOnly){
			buildFailed(fileOut, "Not enough cars ("+departLocation.getNumberRS()+") at departure ("+train.getTrainDepartsName()+") to build train ("+train.getName()+")");
			return;
		}
		// get the number of requested car moves
		int requested = 0;
		for (int i=0; i<routeList.size(); i++){
			RouteLocation rl = train.getRoute().getLocationById((String)routeList.get(i));
			// check to see if there's a location for each stop in the route
			Location l = locationManager.getLocationByName(rl.getName());
			if (l == null){
				buildFailed(fileOut, "ERROR location missing in route ("+train.getRoute().getName()+")");
				return;
			}
			// train doesn't drop or pickup cars from staging locations found in middle of a route
			List slStage = l.getTracksByMovesList(Track.STAGING);
			if (slStage.size() > 0 && i!=0 && i!=routeList.size()-1){
				addLine(fileOut, "Location ("+rl.getName()+") has only staging tracks");
				rl.setCarMoves(rl.getMaxCarMoves());	// don't allow car moves for this location
			}
			// if a location is skipped, no drops or pickups
			else if(train.skipsLocation(rl.getId())){
				addLine(fileOut, "Location (" +rl.getName()+ ") is skipped by train "+train.getName());
				rl.setCarMoves(rl.getMaxCarMoves());	// don't allow car moves for this location
			// we're going to use this location, so initialize the location
			}else{
				requested = requested + rl.getMaxCarMoves();
				rl.setCarMoves(0);					// clear the number of moves
				rl.setStagingTrack(null);		// used for staging only
				addLine(fileOut, "Location (" +rl.getName()+ ") requests " +rl.getMaxCarMoves()+ " moves");
			}
			rl.setTrainWeight(0);					// clear the total train weight 
		}
		int carMoves = requested;
		if(routeList.size()> 1)
			requested = requested/2;  // only need half as many cars to meet requests
		addLine(fileOut, "Route (" +train.getRoute().getName()+ ") requests " + requested + " cars and " + carMoves +" moves");

		// determine if train is departing staging
		Track departStageTrack = null;
		List stagingTracks = departLocation.getTracksByMovesList(Track.STAGING);
		if (stagingTracks.size()>0){
			addLine(fileOut, "Train will depart staging, there are "+stagingTracks.size()+" tracks");
			for (int i=0; i<stagingTracks.size(); i++ ){
				departStageTrack = departLocation.getTrackById((String)stagingTracks.get(i));
				addLine(fileOut, "Staging track ("+departStageTrack.getName()+") has "+departStageTrack.getNumberRS()+" engines and cars");
				if (departStageTrack.getNumberRS()>0 && getEngines(fileOut, departStageTrack)){
					break;
				} else {
					departStageTrack = null;
				}
			}
		}
		if (stagingTracks.size()>0 && departStageTrack == null){
			buildFailed(fileOut, "Could not meet train requirements from staging ("+departLocation.getName()+")");
			return;
		}
		// load engines for this train
		if (departStageTrack == null && !getEngines(fileOut, null)){
			buildFailed(fileOut, "Could not get the required engines for this train");
			return;
		}

		// get list of cars for this route
		carList = carManager.getCarsAvailableTrainList(train);
		// TODO: DAB this needs to be controled by each train
		if (requested > carList.size() && Control.fullTrainOnly){
			buildFailed(fileOut, "The number of requested cars (" +requested+ ") for train (" +train.getName()+ ") is greater than the number available (" +carList.size()+ ")");
			return;
		}
		// get any requirements for this train
		boolean requiresCaboose = false;		// start off without any requirements
		boolean requiresFred = false;
		boolean foundFred = true;
		boolean foundCaboose = true;
		String textRequires = "none";
		if (train.getRequirements()>0){
			if ((train.getRequirements()& train.FRED) > 0){
				requiresFred = true;
				foundFred = false;
				textRequires = "FRED";
			} 
			if ((train.getRequirements()& train.CABOOSE) > 0){
				requiresCaboose = true;
				foundCaboose = false;
				textRequires = "caboose";
			}
			if (!train.getCabooseRoad().equals("")){
				textRequires += " road ("+train.getCabooseRoad()+")";
			}
			addLine(fileOut, "Train ("+train.getName()+") requires "+textRequires);
		}
		// show road names that this train will service
		if (!train.getRoadOption().equals(train.ALLROADS)){
			String[] roads = train.getRoadNames();
	    	String roadNames ="";
	    	for (int i=0; i<roads.length; i++){
	    		roadNames = roadNames + roads[i]+" ";
	    	}
	    	addLine(fileOut, "Train ("+train.getName()+") "+train.getRoadOption()+" roads "+roadNames);
		}
		// show car types that this train will service
		String[] types =train.getTypeNames();
		String typeNames ="";
    	for (int i=0; i<types.length; i++){
    		typeNames = typeNames + types[i]+" ";
    	}
    	addLine(fileOut, "Train ("+train.getName()+") services car types: "+typeNames);
    	for (carIndex=0; carIndex<carList.size(); carIndex++){
    		Car c = carManager.getCarById((String) carList.get(carIndex));
    		// remove cars that don't have a valid track location
    		if (c.getTrack() == null){
    			addLine(fileOut, "ERROR Exclude car ("+c.getId()+") at location ("+c.getLocationName()+", "+c.getTrackName()+") no track location");
				carList.remove(carList.get(carIndex));
				carIndex--;
				continue;
    		}
    		// all cars in staging must be accepted, so don't exclude if in staging
    		if (departStageTrack == null || !c.getTrack().getName().equals(departStageTrack.getName())){
    			if (!train.acceptsRoadName(c.getRoad())){
    				addLine(fileOut, "Exclude car ("+c.getId()+") road ("+c.getRoad()+") at location ("+c.getLocationName()+", "+c.getTrackName()+")");
    				carList.remove(carList.get(carIndex));
    				carIndex--;
    				continue;
    			}
    			if (!train.acceptsTypeName(c.getType())){
    				addLine(fileOut, "Exclude car ("+c.getId()+") type ("+c.getType()+") at location ("+c.getLocationName()+", "+c.getTrackName()+")");
    				carList.remove(carList.get(carIndex));
    				carIndex--;
    				continue;
    			}
    		}
    		// is car at interchange?
    		if (c.getTrack().getLocType().equals(Track.INTERCHANGE)){
    			// don't service a car at interchange and has been dropped of by this train
    			if (c.getTrack().getPickupOption().equals(Track.ANY) && c.getSavedRouteId().equals(train.getRoute().getId())){
    				addLine(fileOut, "Exclude car ("+c.getId()+") previously droped by this train at interchange ("+c.getLocationName()+", "+c.getTrackName()+")");
    				carList.remove(carList.get(carIndex));
    				carIndex--;
    				continue;
    			}
    			if (c.getTrack().getPickupOption().equals(Track.TRAINS)){
    				if (c.getTrack().acceptsPickupTrain(train)){
    					log.debug("Car ("+c.getId()+") can be picked up by this train");
    				} else {
    					addLine(fileOut, "Exclude car ("+c.getId()+") by train, can't pickup this car at interchange ("+c.getLocationName()+", "+c.getTrackName()+")");
    					carList.remove(carList.get(carIndex));
    					carIndex--;
    					continue;
    				}
    			}
    			else if (c.getTrack().getPickupOption().equals(Track.ROUTES)){
    				if (c.getTrack().acceptsPickupRoute(train.getRoute())){
    					log.debug("Car ("+c.getId()+") can be picked up by this route");
    				} else {
    					addLine(fileOut, "Exclude car ("+c.getId()+") by route, can't pickup this car at interchange ("+c.getLocationName()+", "+c.getTrackName()+")");
    					carList.remove(carList.get(carIndex));
    					carIndex--;
    					continue;
    				}
    			}
    		}
		}

		addLine(fileOut, "Found " +carList.size()+ " cars for train (" +train.getName()+ ")");

		// adjust carlist to only have cars from one staging track
		if (departStageTrack != null){
			// Make sure that all cars in staging are moved
			train.getTrainDepartsRouteLocation().setCarMoves(train.getTrainDepartsRouteLocation().getMaxCarMoves()-departStageTrack.getNumberRS());  // neg number moves more cars
			int numCarsFromStaging = 0; 
			for (carIndex=0; carIndex<carList.size(); carIndex++){
				Car c = carManager.getCarById((String) carList.get(carIndex));
//				addLine(fileOut, "Check car ("+c.getId()+") at location ("+c.getLocationName()+" "+c.getTrackName()+")");
				if (c.getLocationName().equals(departLocation.getName())){
					if (c.getTrackName().equals(departStageTrack.getName())){
						addLine(fileOut, "Staging car ("+c.getId()+") at location ("+c.getLocationName()+", "+c.getTrackName()+")");
						numCarsFromStaging++;
					} else {
						addLine(fileOut, "Exclude car ("+c.getId()+") at location ("+c.getLocationName()+", "+c.getTrackName()+") from car list");
						carList.remove(carList.get(carIndex));
						carIndex--;
					}
				}
			}
			// error if all of the cars in staging aren't available
			if (numCarsFromStaging + numberEngines != departStageTrack.getNumberRS()){
				buildFailed(fileOut, "ERROR not all cars or engines in staging can be serviced by this train, " +(departStageTrack.getNumberRS()- (numCarsFromStaging + numberEngines))+" cars or engines can't be serviced");
				return;
			}
		}
		// now go through the car list and remove any that don't belong
		for (carIndex=0; carIndex<carList.size(); carIndex++){
			Car c = carManager.getCarById((String) carList.get(carIndex));
			addLine(fileOut, "Car (" +c.getId()+ ") at location (" +c.getLocationName()+ ", " +c.getTrackName()+ ") with " + c.getMoves()+ " moves");
			// use only the lead car in a kernel for building trains
			if (c.getKernel() != null){
				addLine(fileOut, "Car (" +c.getId()+ ") is part of kernel ("+c.getKernelName()+")");
				if (!c.getKernel().isLeadCar(c)){
					carList.remove(carList.get(carIndex));		// remove this car from the list
					carIndex--;
					continue;
				}
			}
			if (this.equals(c.getTrain())){
				addLine(fileOut, "Car (" +c.getId()+ ") already assigned to this train");
			}
			// does car have a destination that is part of this train's route?
			if (c.getDestination() != null) {
				addLine(fileOut, "Car (" + c.getId()+ ") has a destination (" +c.getDestination().getName()+ ")");
				RouteLocation rld = train.getRoute().getLocationByName(c.getDestination().getName());
				if (rld == null){
					addLine(fileOut, "Exclude car (" + c.getId()+ ") destination (" +c.getDestination().getName()+ ") not part of this train's route (" +train.getRoute().getName() +")");
					// is this car departing staging?
					if (c.getLocationName().equals(departLocation.getName()) && departStageTrack != null){
						buildFailed(fileOut, "Car (" + c.getId()+ ") departing staging with destination that isn't part of this train's route");
						return;
					}
					carList.remove(carList.get(carIndex));		// remove this car from the list
					carIndex--;
				}
			}
		}
		// now go through the car list and find a caboose or fred if required
		// try and find a caboose that matches the engine's road
		if(requiresCaboose && train.getCabooseRoad().equals("") && train.getLeadEngine() != null){
			for (carIndex=0; carIndex<carList.size(); carIndex++){
				Car car = carManager.getCarById((String) carList.get(carIndex));
				if (car.isCaboose() && car.getLocationName().equals(train.getTrainDepartsName()) && car.getRoad().equals(train.getLeadEngine().getRoad())){
					if (car.getDestination() == null || car.getDestination() == terminateLocation){
						addLine(fileOut,"Found caboose (" +car.getId()+ ") that matches engine road");
						// remove all other cabooses from list
						for (int i=0; i<carList.size(); i++){
							Car testCar = carManager.getCarById((String) carList.get(i));
							if (testCar.isCaboose() && testCar != car){
								addLine(fileOut, "Exclude caboose ("+testCar.getId()+") at location ("+testCar.getLocationName()+", "+testCar.getTrackName()+") from car list");
								carList.remove(carList.get(i));		// remove this car from the list
								i--;
							}
						}
						break;
					}
				}
			}
		}
		for (carIndex=0; carIndex<carList.size(); carIndex++){
			Car c = carManager.getCarById((String) carList.get(carIndex));
			// find a caboose or card with FRED for this train if needed
			// check for caboose or car with FRED
			if (c.isCaboose()){
				addLine(fileOut, "Car (" +c.getId()+ ") is a caboose");
				if (departStageTrack != null) foundCaboose = false;		// must move caboose from staging   
			}
			if (c.hasFred()){
				addLine(fileOut, "Car (" +c.getId()+ ") has a FRED");
				if (departStageTrack != null) foundFred = false;		// must move car with FRED from staging
			}
			
			// remove cabooses and cars with FRED if not needed for train
			if (c.isCaboose() && foundCaboose || c.hasFred() && foundFred){
				addLine(fileOut, "Exclude car ("+c.getId()+") at location ("+c.getLocationName()+", "+c.getTrackName()+") from car list");
				carList.remove(carList.get(carIndex));		// remove this car from the list
				carIndex--;
				continue;
			}
			if (c.isCaboose() && !foundCaboose || c.hasFred() && !foundFred){	
				if(c.getLocationName().equals(train.getTrainDepartsName())){
					if (c.getDestination() == null || c.getDestination() == terminateLocation || departStageTrack != null){
						if (train.getCabooseRoad().equals("") || train.getCabooseRoad().equals(c.getRoad()) || departStageTrack != null){
							// find a track to place car
							if (train.getTrainTerminatesRouteLocation().getStagingTrack() == null){
								List sls = terminateLocation.getTracksByMovesList(null);
								for (int s = 0; s < sls.size(); s++){
									Track destTrack = terminateLocation.getTrackById((String)sls.get(s));
									if (c.testDestination(terminateLocation, destTrack).equals(c.OKAY)){
										//TODO check to see if the caboose or car with FRED would exceed train length
										addCarToTrain(fileOut, c, train.getTrainDepartsRouteLocation(), train.getTrainTerminatesRouteLocation(), terminateLocation, destTrack);
										if (c.isCaboose())
											foundCaboose = true;
										if (c.hasFred())
											foundFred = true;
										break;
									}
								}
								if (!foundCaboose || !foundFred)
									addLine(fileOut,"Could not find a destination for ("+c.getId()+")");
							// terminate into staging	
							} else if (c.testDestination(terminateLocation, train.getTrainTerminatesRouteLocation().getStagingTrack()).equals(c.OKAY)){
								//TODO check to see if the caboose or car with FRED would exceed train length
								addCarToTrain(fileOut, c, train.getTrainDepartsRouteLocation(), train.getTrainTerminatesRouteLocation(), terminateLocation, train.getTrainTerminatesRouteLocation().getStagingTrack());
								if (c.isCaboose())
									foundCaboose = true;
								if (c.hasFred())
									foundFred = true;
							}
						}
					}
				} // caboose or FRED not at departure locaton so remove from list
				if(!foundCaboose || !foundFred) {
					addLine(fileOut, "Exclude car ("+c.getId()+") at location ("+c.getLocationName()+" "+c.getTrackName()+") from car list");
					carList.remove(carList.get(carIndex));		// remove this car from the list
					carIndex--;
				}
			}
		}
		if (requiresFred && !foundFred || requiresCaboose && !foundCaboose){
			buildFailed(fileOut, "Train ("+train.getName()+") requires "+textRequires+", none found at departure ("+train.getTrainDepartsName()+")");
			return;
		}
		addLine(fileOut, "Requested cars (" +requested+ ") for train (" +train.getName()+ ") the number available (" +carList.size()+ ") building train!");

		// now find destinations for cars 
		int numLocs = routeList.size();
		if (numLocs > 1)  // don't find car destinations for the last location in the route
			numLocs--;
		for (int locationIndex=0; locationIndex<numLocs; locationIndex++){
			RouteLocation rl = train.getRoute().getLocationById((String)routeList.get(locationIndex));
			if(train.skipsLocation(rl.getId())){
				addLine(fileOut, "Location (" +rl.getName()+ ") is skipped by train (" +train.getName()+ ")");
			}else{
				moves = 0;
				success = false;
				reqNumOfMoves = rl.getMaxCarMoves()-rl.getCarMoves();
				int saveReqMoves = reqNumOfMoves;
				addLine(fileOut, "Location (" +rl.getName()+ ") needs " +reqNumOfMoves+ " moves");
				if (reqNumOfMoves <= 0)
					success = true;
				while (reqNumOfMoves > 0){
					for (carIndex=0; carIndex<carList.size(); carIndex++){
						boolean noMoreMoves = true;  // false when there are are locations with moves
						Car c = carManager.getCarById((String) carList.get(carIndex));
						// find a car at this location
						if (c.getLocationName().equals(rl.getName())){
							// can this car be picked up?
							if(!checkPickUpTrainDirection(fileOut, c, rl))
								continue; // no
							// does car have a destination?
							if (c.getDestination() != null) {
								addLine(fileOut, "Car (" + c.getId()+ ") at location (" +c.getLocation()+ ") has a destination (" +c.getDestination()+ ")");
								RouteLocation rld = train.getRoute().getLocationByName(c.getDestination().getName());
								if (rld == null){
									addLine(fileOut, "Car (" + c.getId()+ ") destination not part of route (" +train.getRoute().getName() +")");
								} else {
									if (c.getRouteLocation() != null){ 
										addLine(fileOut, "Car (" + c.getId()+ ") already assigned to this train");
									} 
									if (rld.getCarMoves() < rld.getMaxCarMoves() && 
											addCarToTrain(fileOut, c, rl, rld, c.getDestination(), c.getDestinationTrack())&& success){
										break;
									}
								}
							// car does not have a destination, search for one	
							} else {
								addLine(fileOut, "Find destinations for car ("+c.getId()+") at location (" +c.getLocationName()+", " +c.getTrackName()+ ")");
								int start = locationIndex;				// start looking after car's current location
								RouteLocation rld = null;				// the route location destination being checked for the car
								RouteLocation rldSave = null;			// holds the best route location destination for the car
								Track trackSave = null;					// holds the best track at destination for the car
								Location destinationSave = null;		// holds the best destination for the car
						
								// more than one location in this route?
								if (routeList.size()>1)
									start++;		//yes!, no car drops at departure
								for (int k = start; k<routeList.size(); k++){
									rld = train.getRoute().getLocationById((String)routeList.get(k));
									addLine(fileOut, "Searching location ("+rld.getName()+") for possible destination");
									// don't move car to same location unless the route only has one location (local moves)
									if (!rl.getName().equals(rld.getName()) || routeList.size() == 1){
										Location destinationTemp = null;
										Track trackTemp = null;
										// any moves left at this location?
										if (rld.getMaxCarMoves()-rld.getCarMoves()>0){
											// get a "test" destination and a list of the track locations available
											noMoreMoves = false;
											Location testDestination = locationManager.getLocationByName(rld.getName());
											if (testDestination == null){
												buildFailed(fileOut, "Route ("+train.getRoute().getName()+") missing location ("+rld.getName()+")");
												return;
											}
											// is there a track assigned for staging cars?
											if (rld.getStagingTrack() == null){
												List sls = testDestination.getTracksByMovesList(null);
												for (int s = 0; s < sls.size(); s++){
													Track testTrack = testDestination.getTrackById((String)sls.get(s));
													// log.debug("track (" +testTrack.getName()+ ") has "+ testTrack.getMoves() + " moves");
													// need to find a track that is isn't the same as the car's current
													String status = c.testDestination(testDestination, testTrack);
													if (testTrack != c.getTrack() 
															&& status.equals(c.OKAY) 
															&& checkDropTrainDirection(fileOut, c, rld, testDestination, testTrack)){
														// staging track with zero cars?
														if (testTrack.getLocType().equals(testTrack.STAGING) && testTrack.getNumberRS() == 0){
															rld.setStagingTrack(testTrack);	// Use this location for all cars
															trackTemp = testTrack;
															destinationTemp = testDestination;
															break;
														}
														// No local moves from siding to siding
														if (routeList.size() == 1 && testTrack.getLocType().equals(testTrack.SIDING) && c.getTrack().getLocType().equals(testTrack.SIDING)){
															log.debug("Local siding to siding move not allowed (" +testTrack.getName()+ ")");
															continue;
														}
														// No local moves from yard to yard
														if (routeList.size() == 1 && testTrack.getLocType().equals(testTrack.YARD) && c.getTrack().getLocType().equals(testTrack.YARD)){
															log.debug("Local yard to yard move not allowed (" +testTrack.getName()+ ")");
															continue;
														}
														// No local moves from interchange to interchange
														if (routeList.size() == 1 && testTrack.getLocType().equals(testTrack.INTERCHANGE) && c.getTrack().getLocType().equals(testTrack.INTERCHANGE)){
															log.debug("Local interchange to interchange move not allowed (" +testTrack.getName()+ ")");
															continue;
														}
														// drop to interchange?
														if (testTrack.getLocType().equals(testTrack.INTERCHANGE)){
															if (testTrack.getDropOption().equals(testTrack.TRAINS)){
																if (testTrack.acceptsDropTrain(train)){
																	log.debug("Car ("+c.getId()+" can be droped by train to interchange (" +testTrack.getName()+")");
																} else {
																	addLine(fileOut, "Can't drop car ("+c.getId()+") by train to interchange (" +testTrack.getName()+")");
																	continue;
																}
															}
															if (testTrack.getDropOption().equals(testTrack.ROUTES)){
																if (testTrack.acceptsDropRoute(train.getRoute())){
																	log.debug("Car ("+c.getId()+" can be droped by route to interchange (" +testTrack.getName()+")");
																} else {
																	addLine(fileOut, "Can't drop car ("+c.getId()+") by route to interchange (" +testTrack.getName()+")");
																	continue;
																}
															}
														}
														// not staging, then use
														if (!testTrack.getLocType().equals(testTrack.STAGING)){
															trackTemp = testTrack;
															destinationTemp = testDestination;
															break;
														}
													}
													// car's current track is the test track or car can't be dropped
												}
											// all cars in this train go to one staging track
											} else {
												// will staging accept this car?
												String status = c.testDestination(testDestination, rld.getStagingTrack());
												if (status.equals(c.OKAY)){
													trackTemp = rld.getStagingTrack();
													destinationTemp = testDestination;
												}
											}
											if(destinationTemp != null){
												addLine(fileOut, "car ("+c.getId()+") has available destination (" +destinationTemp.getName()+ ", " +trackTemp.getName()+ ") with " +rld.getCarMoves()+ "/" +rld.getMaxCarMoves()+" moves");
												// if there's more than one available destination use the one with the least moves
												if (rldSave != null){
													double saveCarMoves = rldSave.getCarMoves();
													double saveRatio = saveCarMoves/rldSave.getMaxCarMoves();
													double nextCarMoves = rld.getCarMoves();
													double nextRatio = nextCarMoves/rld.getMaxCarMoves();
													log.debug("Save = "+Double.toString(saveRatio)+ " Next = "+Double.toString(nextRatio));
													if (saveRatio < nextRatio){
														rld = rldSave;					// the saved is better than the last found
														destinationTemp = destinationSave;
														trackTemp = trackSave;
													}
												}
												// every time through, save the best route location, destination, and track
												rldSave = rld;
												destinationSave = destinationTemp;
												trackSave = trackTemp;
											} else {
												addLine(fileOut, "Could not find a valid destination for car ("+c.getId()+") at location (" + rld.getName()+")");
											}
										} else {
											addLine(fileOut, "No available moves for destination ("+rld.getName()+")");
										}
									} else{
										addLine(fileOut, "Car ("+c.getId()+") location is equal to destination ("+rld.getName()+"), skiping this destination");
									}
								}
								boolean carAdded = false; // all cars departing staging must be included or build failure
								if (destinationSave != null){
									carAdded = addCarToTrain(fileOut, c, rl, rldSave, destinationSave, trackSave);
									if (carAdded && success){
										log.debug("done with location ("+destinationSave.getName()+")");
										break;
									}
								} 
								// car leaving staging without a destinaton?
								if (c.getTrack().getLocType().equals(Track.STAGING) && (!carAdded  || destinationSave == null)){
									buildFailed(fileOut, "Could not find a destination for car ("+c.getId()+") at location (" + rld.getName()+")");
									return;
									// are there still moves available?
								} 
								if (noMoreMoves) {
									log.debug("No available destinations for any car");
									reqNumOfMoves = 0;
									break;
								}
							}
						}
						// car not at location or has fred or caboose
					}
					// could not find enough cars
					reqNumOfMoves = 0;
				}
				addLine(fileOut, (success?"Success, ":"Partial, ") +moves+ "/" +saveReqMoves+ " cars at location (" +rl.getName()+ ") assigned to train ("+train.getName()+")");
			}
		}

		train.setCurrentLocation(train.getTrainDepartsRouteLocation());
		if (numberCars < requested){
			train.setStatus(PARTIALBUILT + train.getNumberCarsWorked() +"/" + requested + " "+ rb.getString("moves"));
			addLine(fileOut, PARTIALBUILT + train.getNumberCarsWorked() +"/" + requested + " "+ rb.getString("moves"));
		}else{
			train.setStatus(BUILT + train.getNumberCarsWorked() + " "+ rb.getString("moves"));
			addLine(fileOut, BUILT + train.getNumberCarsWorked() + " "+ rb.getString("moves"));
		}
		train.setBuilt(true);
		if (fileOut != null){
			fileOut.flush();
			fileOut.close();
		}

		// now build manifest
		makeManifest();
		// now create and place train icon
		train.moveTrainIcon(train.getTrainDepartsRouteLocation());

	}
	
	// get the engines for this train. If track != null, then engines must
	// come from that track location (staging).  Returns true if engines found, else false.
	// This routine will also pick the destination track if the train is
	// terminating into staging, therefore this routine should only be called once when return is true.
	private boolean getEngines(PrintWriter fileOut, Track track){
		// show engine requirements for this train
		addLine(fileOut, "Train requires "+train.getNumberEngines()+" engine(s) model ("+train.getEngineModel()+") road (" +train.getEngineRoad()+")");
				
		numberEngines = 0;
		int reqNumEngines = 0; 	
		int engineLength = 0;
		
		if (train.getNumberEngines().equals(train.AUTO)){
			reqNumEngines = getAutoEngines(fileOut);
		} else {
			reqNumEngines = Integer.parseInt(train.getNumberEngines());
		}
		// if leaving staging, use any number of engines if required number is 0
		boolean leavingStaging = false;
		if (track != null && reqNumEngines == 0)
			leavingStaging = true;

		// get list of engines for this route
		
		List engineList = engineManager.getEnginesAvailableTrainList(train);
		// remove engines not at departure, wrong road name, or part of consist (not lead)
		for (int indexEng=0; indexEng<engineList.size(); indexEng++){
			Engine engine = engineManager.getEngineById((String) engineList.get(indexEng));
			addLine(fileOut, "Engine ("+engine.getId()+") road ("+engine.getRoad()+") model ("+engine.getModel()+") at location ("+engine.getLocationName()+", "+engine.getTrackName()+")");
			// remove engines that have been assigned destinations
			if (engine.getDestination() != null && !engine.getDestination().equals(terminateLocation)){
				addLine(fileOut, "Exclude engine ("+engine.getId()+") it has an assigned destination ("+engine.getDestination().getName()+")");
				engineList.remove(indexEng);
				indexEng--;
				continue;
			}
			// determine if engine is departing from staging track (track != null if staging) 
			if(engine.getLocationName().equals(train.getTrainDepartsName()) && (track == null || engine.getTrackName().equals(track.getName()))){
				if ((train.getEngineRoad().equals("") || engine.getRoad().equals(train.getEngineRoad())) && (train.getEngineModel().equals("") || engine.getModel().equals(train.getEngineModel()))){
					// is this engine part of a consist?  Keep only lead engines in consist if required number is correct.
					if (engine.getConsist() != null){
						addLine(fileOut, "Engine ("+engine.getId()+") is part of consist ("+engine.getConsist().getName()+")");
						if (!engine.getConsist().isLeadEngine(engine)){
							// only use lead engines
							engineList.remove(indexEng);
							indexEng--;
						}else{
							List cEngines = engine.getConsist().getEngines();
							if (cEngines.size() == reqNumEngines || leavingStaging){
								log.debug("Consist ("+engine.getConsist().getName()+") has the required number of engines");
							}else{
								log.debug("Consist ("+engine.getConsist().getName()+") doesn't have the required number of engines");
								engineList.remove(indexEng);
								indexEng--;
							}
						}
					}
					continue;
				} 
			}
			addLine(fileOut, "Exclude engine ("+engine.getId()+")");
			engineList.remove(indexEng);
			indexEng--;
		}

		// now load the number of engines into the train
		Track terminateTrack = null;
		for (int indexEng=0; indexEng<engineList.size(); indexEng++){
			Engine engine = engineManager.getEngineById((String) engineList.get(indexEng));
			train.setLeadEngine(engine);	//load lead engine
			// find a track for engine(s) at destination
			List destTracks = terminateLocation.getTracksByMovesList(null);
			for (int s = 0; s < destTracks.size(); s++){
				terminateTrack = terminateLocation.getTrackById((String)destTracks.get(s));
				if (terminateTrack.getLocType().equals(terminateTrack.STAGING) && terminateTrack.getNumberRS()>0){
					terminateTrack = null;
					continue;
				}
				String status = engine.testDestination(terminateLocation, terminateTrack);
				if(status == engine.OKAY){
					break;
				} else {
					terminateTrack = null;
				}
			}
			if (terminateTrack == null && (reqNumEngines>0 || leavingStaging)){
				addLine(fileOut, "Could not find valid destination for engine ("+engine.getId()+") at (" +terminateLocation.getName()+ ") for train (" +train.getName()+ ")");
			}
			if (terminateTrack != null){
				if (engine.getConsist() != null){
					List cEngines = engine.getConsist().getEngines();
					if (cEngines.size() == reqNumEngines || leavingStaging){
						engineLength = engine.getConsist().getLength();
						for (int j=0; j<cEngines.size(); j++){
							numberEngines++;
							Engine cEngine = (Engine)cEngines.get(j);
							addLine(fileOut, "Engine ("+cEngine.getId()+") assigned destination ("+terminateLocation.getName()+", "+terminateTrack.getName()+")");
							cEngine.setTrain(train);
							cEngine.setRouteLocation(train.getTrainDepartsRouteLocation());
							cEngine.setRouteDestination(train.getTrainTerminatesRouteLocation());
							cEngine.setDestination(terminateLocation, terminateTrack);
						}
						break;  // done with loading engines
						// consist has the wrong number of engines, remove 	
					} else {
						addLine(fileOut, "Exclude engine ("+engine.getId()+") consist ("+engine.getConsist().getName()+") number of engines (" +cEngines.size()+ ")");
						engineList.remove(indexEng);
						indexEng--;
					}
					// engine isn't part of a consist
				} else if (reqNumEngines ==1 || leavingStaging){
					numberEngines++;
					addLine(fileOut, "Engine ("+engine.getId()+") assigned destination ("+terminateLocation.getName()+", "+terminateTrack.getName()+")");
					engine.setTrain(train);
					engine.setRouteLocation(train.getTrainDepartsRouteLocation());
					engine.setRouteDestination(train.getTrainTerminatesRouteLocation());
					engine.setDestination(terminateLocation, terminateTrack);
					engineLength = Integer.parseInt(engine.getLength());
					break;  // done with loading engine
				}
			}
		}
		if (numberEngines < reqNumEngines){
			addLine(fileOut, "Could not find the proper engines at departure location");
			return false;
		}
		
		// set the engine length for locations
		for (int i=0; i<routeList.size(); i++){
			RouteLocation rl = train.getRoute().getLocationById((String)routeList.get(i));
			rl.setTrainLength(engineLength);		// load the engine(s) length
		}
		// terminating into staging?
		if (terminateTrack != null && terminateTrack.getLocType().equals(terminateTrack.STAGING)){
			train.getTrainTerminatesRouteLocation().setStagingTrack(terminateTrack);
		}
		return true;
	}
	
	// returns the number of engines needed for this train, minimum 1, 
	// maximum user specified in setup.
	// Based on maximum allowable train length and grade between locations,
	// and the maximum cars that the train can have at the maximum train length.
	// Currently ignores the cars weight and engine horsepower
	private int getAutoEngines(PrintWriter fileOut){
		double numberEngines = 1;
		int moves = 0;
		
		for (int i=0; i<routeList.size()-1; i++){
			RouteLocation rl = train.getRoute().getLocationById((String)routeList.get(i));
			moves += rl.getMaxCarMoves();
			double carDivisor = 16;	// number of 40' cars per engine 1% grade
			// change engine requirements based on grade
			if (rl.getGrade()>1){
				double grade = rl.getGrade();
				carDivisor = carDivisor/grade;
			}
			if (rl.getMaxTrainLength()/(carDivisor*40) > numberEngines){
				numberEngines = rl.getMaxTrainLength()/(carDivisor*(40+Car.COUPLER));
				// round up to next whole integer
				numberEngines = Math.ceil(numberEngines);
				if (numberEngines > moves/carDivisor)
					numberEngines = Math.ceil(moves/carDivisor);
				if (numberEngines < 1)
					numberEngines = 1;
			}
		}
		int nE = (int)numberEngines;
		addLine(fileOut, "Auto engines calculates that "+nE+ " engines are required for this train");
		if (nE > Setup.getEngineSize()){
			addLine(fileOut, "The maximum number of engines that can be assigned is "+Setup.getEngineSize());
			nE = Setup.getEngineSize();
		} 
		return nE;
	}
	
	/**
	 * Add car to train
	 * @param file
	 * @param car
	 * @param rl the planned origin for this car
	 * @param rld the planned destination for this car
	 * @param destination
	 * @param track the final destination for car
	 * @return true if car was successfully added to train.  Also makes boolean
	 * boolean "success" true if location doesn't need any more pickups. 
	 */
	private boolean addCarToTrain(PrintWriter file, Car car, RouteLocation rl, RouteLocation rld, Location destination, Track track){
		if (checkTrainLength(file, car, rl, rld)){
			int oldNum = moves;
			// car could be part of a kernel
			if (car.getKernel()!=null){
				List kCars = car.getKernel().getCars();
				addLine(file, "Car ("+car.getId()+") is part of kernel ("+car.getKernelName()+") with "+ kCars.size() +" cars");
				// log.debug("kernel length "+car.getKernel().getLength());
				for(int i=0; i<kCars.size(); i++){
					Car kCar = (Car)kCars.get(i);
					addLine(file, "Car ("+kCar.getId()+") assigned destination ("+destination.getName()+", "+track.getName()+")");
					kCar.setTrain(train);
					kCar.setRouteLocation(rl);
					kCar.setRouteDestination(rld);
					kCar.setDestination(destination, track);
				}
				// not part of kernel, add one car	
			} else {
				addLine(file, "Car ("+car.getId()+") assigned destination ("+destination.getName()+", "+track.getName()+")");
				car.setTrain(train);
				car.setRouteLocation(rl);
				car.setRouteDestination(rld);
				car.setDestination(destination, track);
			}
			numberCars++;		// bump number of cars moved by this train
			moves++;			// bump number of car pickup moves for the location
			reqNumOfMoves--; 	// dec number of moves left for the location
			if(reqNumOfMoves <= 0)
				success = true;	// done with this location!
			carList.remove(car.getId());
			carIndex--;  		// removed car from list, so backup pointer 

			rl.setCarMoves(rl.getCarMoves() + 1);
			if (rl != rld)
				rld.setCarMoves(rld.getCarMoves() + 1);
			// now adjust train length and weight for each location that car is in the train
			boolean carInTrain = false;
			for (int i=0; i<routeList.size(); i++){
				int weightTons = 0;
				RouteLocation rlt = train.getRoute().getLocationById((String)routeList.get(i));
				if (rl == rlt){
					carInTrain = true;
				}
				if (rld == rlt){
					carInTrain = false;
				}
				if (carInTrain){
					// car could be part of a kernel
					int length = Integer.parseInt(car.getLength())+ car.COUPLER;
					try {
						weightTons = weightTons + Integer.parseInt(car.getWeightTons());
					} catch (Exception e){
						log.debug ("car ("+car.getId()+") weight not set");
					}
					if (car.getKernel() != null){
						length = car.getKernel().getLength();
						weightTons = car.getKernel().getWeightTons();
					}
					rlt.setTrainLength(rlt.getTrainLength()+length);
					rlt.setTrainWeight(rlt.getTrainWeight()+weightTons);
				}
				if (weightTons > maxWeight){
					maxWeight = weightTons;		// used for AUTO engines
				}
			}
			return true;
		} else {
			return false;
		}
	}

	private boolean checkPickUpTrainDirection (PrintWriter file, Car car, RouteLocation rl){
		if (routeList.size() == 1) // ignore local train direction
			return true;
		String trainDirection = rl.getTrainDirection();	// train direction North, South, East and West
		int trainDir = 0;
		if (trainDirection.equals(rl.NORTH))
			trainDir = Track.NORTH;
		if (trainDirection.equals(rl.SOUTH))
			trainDir = Track.SOUTH;
		if (trainDirection.equals(rl.EAST))
			trainDir = Track.EAST;
		if (trainDirection.equals(rl.WEST))
			trainDir = Track.WEST;
		
		if ((trainDir & car.getLocation().getTrainDirections() & car.getTrack().getTrainDirections()) >0)
			return true;
		else {
			addLine(file, "Can't add car ("+car.getId()+") to "
					+trainDirection+"bound train, location ("+car.getLocation().getName()
					+", "+car.getTrack().getName()+") does not service this direction");
			return false;
		}
	}
	
	
	/**
	 * 
	 * @param file
	 * @param car
	 * @param rl the planned origin for this car
	 * @param rld the planned destination for this car
	 * @return true if car can be added to train
	 */
	private boolean checkTrainLength(PrintWriter file, Car car,
			RouteLocation rl, RouteLocation rld) {
		boolean carInTrain = false;
		for (int i=0; i<routeList.size(); i++){
			RouteLocation rlt = train.getRoute().getLocationById((String)routeList.get(i));
			if (rl == rlt){
				carInTrain = true;
			}
			if (rld == rlt){
				carInTrain = false;
			}
			// car can be a kernel so get total length
			int length = Integer.parseInt(car.getLength())+ car.COUPLER;
			if (car.getKernel() != null)
				length = car.getKernel().getLength();
			if (carInTrain && rlt.getTrainLength()+ length > rlt.getMaxTrainLength()){
				addLine(file, "Can't add car ("+car.getId()+") length ("+length+") to train, it would exceed train length restrication at "+rlt.getName());
				return false;
			}
		}
		return true;
	}
	
	private boolean checkDropTrainDirection (PrintWriter file, Car car, RouteLocation rld, Location destination, Track track){
		// is the destination the last location on the route? 
		if (rld == train.getTrainTerminatesRouteLocation())
			return true;	// yes, ignore train direction
		String trainDirection = rld.getTrainDirection();	// train direction North, South, East or West
		// convert train direction to binary bit map, only one bit set 
		int trainDir = 0;
		if (trainDirection.equals(rld.NORTH))
			trainDir = Track.NORTH;
		else if (trainDirection.equals(rld.SOUTH))
			trainDir = Track.SOUTH;
		else if (trainDirection.equals(rld.EAST))
			trainDir = Track.EAST;
		else if (trainDirection.equals(rld.WEST))
			trainDir = Track.WEST;
		int serviceTrainDir = (destination.getTrainDirections() & track.getTrainDirections()); // this location only services trains with these directions
		if ((serviceTrainDir & trainDir) >0){
			return true;
		} else {
			addLine(file, "Can't add car ("+car.getId()+") to "+trainDirection+"bound train, destination ("+track+") does not service this direction");
			return false;
		}
	}

	private void buildFailed(PrintWriter file, String string){
		train.setStatus(BUILDFAILED);
		if(log.isDebugEnabled())
			log.debug(string);
		JOptionPane.showMessageDialog(null, string,
				"Can not build train ("+train.getName()+") " +train.getDescription(),
				JOptionPane.ERROR_MESSAGE);
		if (file != null){
			file.println(string);
			// Write to disk and close file
			file.println("Build failed for train ("+train.getName()+")");
			file.flush();
			file.close();
			if(TrainManager.instance().getBuildReport()){
				File buildFile = TrainManagerXml.instance().getTrainBuildReportFile(train.getName());
				printReport(buildFile, "Train Build Failure Report", true);
			}
		}
	}
	
	private static void printReport(File file, String name, boolean isPreview){
		Train.printReport(file, name, isPreview, "");
 	}
	
	private void makeManifest() {
		// create manifest file
		File file = TrainManagerXml.instance().createTrainManifestFile(
				train.getName());
		PrintWriter fileOut;

		try {
			fileOut = new PrintWriter(new BufferedWriter(new FileWriter(file)),
					true);
		} catch (IOException e) {
			log.error("can not open train manifest file");
			return;
		}
		// build header
		addLine(fileOut, Setup.getRailroadName());
		newLine(fileOut);
		addLine(fileOut, rb.getString("ManifestForTrain")+" (" + train.getName() + ") "+ train.getDescription());
		addLine(fileOut, "Valid " + new Date());
		addLine(fileOut, "Departs "+train.getDepartureTime());
		if (!train.getComment().equals("")){
			addLine(fileOut, train.getComment());
		}
		
		List engineList = engineManager.getEnginesByTrainList(train);
		Engine engine = null;
		String comment = "";
		for (int i =0; i < engineList.size(); i++){
			engine = engineManager.getEngineById((String) engineList.get(i));
			comment = (Setup.isAppendCarCommentEnabled() ? " "+engine.getComment() : "");
			addLine(fileOut, BOX + rb.getString("Engine")+" "+ engine.getRoad() + " " + engine.getNumber() + " (" +engine.getModel()+  ") "+rb.getString("assignedToThisTrain") + comment);
		}
		
		if (engine != null)
			addLine(fileOut, "Pickup engine(s) at "+engine.getLocationName()+", "+engine.getTrackName());
		
		List carList = carManager.getCarsByTrainList(train);
		log.debug("Train has " + carList.size() + " cars assigned to it");
		int cars = 0;
		List routeList = train.getRoute().getLocationsBySequenceList();
		for (int i = 0; i < routeList.size(); i++) {
			RouteLocation rl = train.getRoute().getLocationById((String) routeList.get(i));
			newLine(fileOut);
			addLine(fileOut, rb.getString("ScheduledWorkIn")+" " + rl.getName());
			// block cars by destination
			for (int j = i; j < routeList.size(); j++) {
				RouteLocation rld = train.getRoute().getLocationById((String) routeList.get(j));
				for (int k = 0; k < carList.size(); k++) {
					Car car = carManager.getCarById((String) carList.get(k));
					if (car.getRouteLocation() == rl
							&& car.getRouteDestination() == rld) {
						pickupCar(fileOut, car);
						cars++;
					}
				}
			}
			for (int j = 0; j < carList.size(); j++) {
				Car car = carManager.getCarById((String) carList.get(j));
				if (car.getRouteDestination() == rl) {
					dropCar(fileOut, car);
					cars--;
				}
			}
			if (i != routeList.size() - 1) {
				addLine(fileOut, rb.getString("TrainDeparts")+ " " + rl.getName() +" "+ rl.getTrainDirection()
						+ rb.getString("boundWith") +" " + cars + " " +rb.getString("cars")+", " +rl.getTrainLength()
						+" "+rb.getString("feet")+", "+rl.getTrainWeight()+" "+rb.getString("tons"));
			} else {
				if(engine != null)
					addLine(fileOut, BOX +rb.getString("DropEngineTo")+ " "+ engine.getDestinationTrackName()); 
				addLine(fileOut, rb.getString("TrainTerminatesIn")+ " " + rl.getName());
			}
		}
		fileOut.flush();
		fileOut.close();
	}
	
	static org.apache.log4j.Category log = org.apache.log4j.Category
			.getInstance(TrainBuilder.class.getName());

}
