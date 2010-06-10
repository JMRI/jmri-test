package jmri.jmrit.operations.trains;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;

import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Schedule;
import jmri.jmrit.operations.locations.ScheduleItem;
import jmri.jmrit.operations.locations.ScheduleManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarLoads;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.rollingstock.engines.EngineTypes;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;

/**
 * Builds a train and creates the train's manifest. 
 * 
 * @author Daniel Boudreau  Copyright (C) 2008, 2009, 2010
 * @version             $Revision: 1.77 $
 */
public class TrainBuilder extends TrainCommon{
	
	static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.trains.JmritOperationsTrainsBundle");
	private static final String BOX = " [ ] ";
	
	// build status
	private static final String BUILDFAILED = rb.getString("BuildFailed");
	private static final String BUILDING = rb.getString("Building");
	private static final String BUILT = rb.getString("Built") + " ";
	private static final String PARTIALBUILT = rb.getString("Partial") + " ";
			
	// build variables shared between local routines
	Train train;			// the train being built
	int numberCars;			// how many cars are moved by this train
	int reqNumEngines = 0; 	// the number of engines required for this train
	int numberEngines = 0;	// the number of engines assigned to this train
	int carIndex;			// index for carList
	List<String> carList;	// list of cars available for this train
	List<String> routeList;	// list of locations from departure to termination served by this train
	int moves;				// the number of pickup car moves for a location
	double maxWeight;		// the maximum weight of cars in train
	int reqNumOfMoves;		// the requested number of car moves for a location
	Location departLocation;	// train departs this location
	Track departStageTrack;		// departure staging track (null if not staging)
	Location terminateLocation; // train terminate at this location
	Track terminateStageTrack; 	// terminate staging track (null if not staging)
	boolean success;			// true when enough cars have been picked up from a location
	PrintWriter buildReport;	// build report for this train
	
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
	 * 5. All cars and engines must leave staging tracks
	 * 6  When departing staging find a track matching train requirements
	 * 7. If a train is assigned to staging, all cars and engines must go there  
	 * 8. Service locations based on train direction, location car types and roads
	 * 9. Ignore track direction when train is a local (serves one location)
	 *
	 * @param train the train that is to be built
	 * 
	 * message windows.
	 */
	public void build(Train train){
		log.debug("Building train "+train.getName());
		this.train = train;
		train.setStatus(BUILDING);
		train.setBuilt(false);
		train.setLeadEngine(null);
		numberCars = 0;
		maxWeight = 0;
		
		// create build status file
		File file = TrainManagerXml.instance().createTrainBuildReportFile(train.getName());
		try {
			buildReport = new PrintWriter(new BufferedWriter(new FileWriter(file)),
					true);
		} catch (IOException e) {
			log.error("can not open build status file");
			return;
		}		
		addLine(buildReport, ONE, MessageFormat.format(rb.getString("BuildReportMsg"),new Object[]{train.getName(), new Date()}));
		
		if (train.getRoute() == null){
			buildFailed(MessageFormat.format(rb.getString("buildErrorRoute"),new Object[]{train.getName()}));
			return;
		}
		// get the train's route
		routeList = train.getRoute().getLocationsBySequenceList();
		if (routeList.size() < 1){
			buildFailed(MessageFormat.format(rb.getString("buildErrorNeedRoute"),new Object[]{train.getName()}));
			return;
		}
		// train departs
		departLocation = locationManager.getLocationByName(train.getTrainDepartsName());
		if (departLocation == null){
			buildFailed(MessageFormat.format(rb.getString("buildErrorNeedDepLoc"),new Object[]{train.getName()}));
			return;
		}
		// train terminates
		terminateLocation = locationManager.getLocationByName(train.getTrainTerminatesName());
		if (terminateLocation == null){
			buildFailed(MessageFormat.format(rb.getString("buildErrorNeedTermLoc"),new Object[]{train.getName()}));
			return;
		}		  
		// TODO: DAB control minimal build by each train
		if (train.getTrainDepartsRouteLocation().getMaxCarMoves() > departLocation.getNumberRS() && Control.fullTrainOnly){
			buildFailed(MessageFormat.format(rb.getString("buildErrorCars"),new Object[]{Integer.toString(departLocation.getNumberRS()),
				train.getTrainDepartsName(), train.getName()}));
			return;
		}
		// get the number of requested car moves for this train
		int requested = 0;
		for (int i=0; i<routeList.size(); i++){
			RouteLocation rl = train.getRoute().getLocationById(routeList.get(i));
			// check to see if there's a location for each stop in the route
			Location l = locationManager.getLocationByName(rl.getName());
			if (l == null){
				buildFailed(MessageFormat.format(rb.getString("buildErrorLocMissing"),new Object[]{train.getRoute().getName()}));
				return;
			}
			// train doesn't drop or pickup cars from staging locations found in middle of a route
			List<String> slStage = l.getTracksByMovesList(Track.STAGING);
			if (slStage.size() > 0 && i!=0 && i!=routeList.size()-1){
				addLine(buildReport, ONE, MessageFormat.format(rb.getString("buildLocStaging"),new Object[]{rl.getName()}));
				rl.setCarMoves(rl.getMaxCarMoves());	// don't allow car moves for this location
			}
			// if a location is skipped, no car drops or pickups
			else if(train.skipsLocation(rl.getId())){
				addLine(buildReport, THREE, "Location (" +rl.getName()+ ") is skipped by train ("+train.getName()+")");
				rl.setCarMoves(rl.getMaxCarMoves());	// don't allow car moves for this location
			}
			// skip if a location doesn't allow drops or pickups
			else if(!rl.canDrop() && !rl.canPickup()){
				addLine(buildReport, THREE, "Location (" +rl.getName()+ ") does not allow drops or pickups");
				rl.setCarMoves(rl.getMaxCarMoves());	// don't allow car moves for this location
			}
			else{
				// we're going to use this location, so initialize the location
				requested = requested + rl.getMaxCarMoves(); // add up the total number of car moves requested
				rl.setCarMoves(0);					// clear the number of moves
				rl.setStagingTrack(null);			// used for staging only
				addLine(buildReport, THREE, "Location (" +rl.getName()+ ") requests " +rl.getMaxCarMoves()+ " moves");
			}
			rl.setTrainWeight(0);					// clear the total train weight 
			rl.setTrainLength(0);					// and length
		}
		int numMoves = requested;	// number of car moves
		if(routeList.size()> 1)
			requested = requested/2;  // only need half as many cars to meet requests
		addLine(buildReport, ONE, MessageFormat.format(rb.getString("buildRouteRequest"),new Object[]{train.getRoute().getName(), Integer.toString(requested), Integer.toString(numMoves)}));
		
		// show car types that this train will service
		String[] types =train.getTypeNames();
		StringBuffer sbuf = new StringBuffer("");
    	for (int i=0; i<types.length; i++){
    		sbuf = sbuf.append(types[i]+", ");
    	}  	
        if (sbuf.length() > 2) sbuf.setLength(sbuf.length()-2);	// remove trailing separators
    	addLine(buildReport, FIVE, "Train ("+train.getName()+") services rolling stock types: "+sbuf.toString());
		// show road names that this train will service
		if (!train.getRoadOption().equals(Train.ALLROADS)){
			String[] roads = train.getRoadNames();
			sbuf = new StringBuffer("");    	
	    	for (int i=0; i<roads.length; i++){
	    		sbuf = sbuf.append(roads[i]+", ");
	    	}	       	
	        if (sbuf.length() > 2) sbuf.setLength(sbuf.length()-2);	// remove trailing separators
	    	addLine(buildReport, FIVE, "Train ("+train.getName()+") "+train.getRoadOption()+" roads: "+sbuf.toString());
		}
		// show owner names that this train will service
		if (!train.getOwnerOption().equals(Train.ALLOWNERS)){
			String[] owners = train.getOwnerNames();
			sbuf = new StringBuffer("");    	
	    	for (int i=0; i<owners.length; i++){
	    		sbuf = sbuf.append(owners[i]+", ");
	    	}
	        if (sbuf.length() > 2) sbuf.setLength(sbuf.length()-2); // remove trailing separators
	    	addLine(buildReport, FIVE, "Train ("+train.getName()+") "+train.getOwnerOption()+" owners: "+sbuf.toString());
		}
		// show built date serviced
		if (!train.getBuiltStartYear().equals(""))
			addLine(buildReport, FIVE, "Train ("+train.getName()+") will service rolling stock built after "+train.getBuiltStartYear());
		if (!train.getBuiltEndYear().equals(""))
			addLine(buildReport, FIVE, "Train ("+train.getName()+") will service rolling stock built before "+train.getBuiltEndYear());
		
		// show engine info
		if (train.getNumberEngines().equals(Train.AUTO)){
			reqNumEngines = getAutoEngines();
		} else {
			reqNumEngines = Integer.parseInt(train.getNumberEngines());
		}	
		// show engine types that this train will service
		if (reqNumEngines >0){
			String[] engineTypes = EngineTypes.instance().getNames();
			sbuf = new StringBuffer("");
			for (int i=0; i<engineTypes.length; i++){
				if (train.acceptsTypeName(engineTypes[i]))
					sbuf = sbuf.append(engineTypes[i]+", ");
			}   		
			
			if (sbuf.length() > 2) sbuf.setLength(sbuf.length()-2);	// remove trailing separators
			addLine(buildReport, FIVE, "Train ("+train.getName()+") services engine types: "+sbuf.toString());
		}		
		// show engine requirements for this train
		if (reqNumEngines == 0)
			addLine(buildReport, ONE, rb.getString("buildTrainReq0Engine"));
		else if (reqNumEngines == 1)
			addLine(buildReport, ONE, MessageFormat.format(rb.getString("buildTrainReq1Engine"),new Object[]{train.getEngineModel(), train.getEngineRoad()}));
		else
			addLine(buildReport, ONE, MessageFormat.format(rb.getString("buildTrainReqEngine"),new Object[]{train.getNumberEngines(), train.getEngineModel(), train.getEngineRoad()}));
	
		// does train terminate into staging?
		terminateStageTrack = null;
		List<String> stagingTracksTerminate = terminateLocation.getTracksByMovesList(Track.STAGING);
		if (stagingTracksTerminate.size() > 0){
			addLine(buildReport, ONE, MessageFormat.format(rb.getString("buildTerminateStaging"),new Object[]{terminateLocation.getName(), Integer.toString(stagingTracksTerminate.size())}));
			for (int i=0; i<stagingTracksTerminate.size(); i++){
				terminateStageTrack = terminateLocation.getTrackById(stagingTracksTerminate.get(i));
				if (checkTerminateStagingTrack()){
					addLine(buildReport, ONE, MessageFormat.format(rb.getString("buildStagingAvail"),new Object[]{terminateStageTrack.getName(), terminateLocation.getName()}));
					break;
				} 
				terminateStageTrack = null;
			}
			if (terminateStageTrack == null){
				buildFailed(MessageFormat.format(rb.getString("buildErrorStagingFull"),new Object[]{terminateLocation.getName()}));
				return;
			}
		}
		
		// determine if train is departing staging
		departStageTrack = null;
		List<String> stagingTracks = departLocation.getTracksByMovesList(Track.STAGING);
		if (stagingTracks.size()>0){
			addLine(buildReport, ONE, MessageFormat.format(rb.getString("buildDepartStaging"),new Object[]{Integer.toString(stagingTracks.size())}));
			for (int i=0; i<stagingTracks.size(); i++ ){
				departStageTrack = departLocation.getTrackById(stagingTracks.get(i));
				addLine(buildReport, ONE, MessageFormat.format(rb.getString("buildStagingHas"),new Object[]{
						departStageTrack.getName(), Integer.toString(departStageTrack.getNumberEngines()),
						Integer.toString(departStageTrack.getNumberCars())}));
				// is the departure track available?
				if (!checkDepartureStagingTrack()){
					departStageTrack = null;
					continue;
				}
				// is the staging track direction correct for this train?
				if ((departStageTrack.getTrainDirections() & train.getRoute().getLocationById(routeList.get(0)).getTrainDirection()) == 0){
					addLine(buildReport, THREE, "Staging track ("+departStageTrack.getName()+") does not service this train's direction");
					departStageTrack = null;
					continue;
				}
				// get the required engines from staging for this train
				if (departStageTrack.getNumberRS()>0 && getEngines()){
					break;
				} 
				departStageTrack = null;
			}
			if (departStageTrack == null){
				buildFailed(MessageFormat.format(rb.getString("buildErrorStagingReq"),new Object[]{departLocation.getName()}));
				return;
			}
		}
		// load engines for this train
		else if (!getEngines()){
			buildFailed(rb.getString("buildErrorEngines"));
			return; // there was a failure
		}
		// remove unwanted cars
		if (!removeCars(requested))
			return;	// there was a failure

		// get caboose or car with FRED if needed for train
		if(!getCabooseOrFred())
			return;	// there was a failure
		addLine(buildReport, THREE, "Requested cars (" +requested+ ") for train (" +train.getName()+ ") the number available (" +carList.size()+ ") building train!");

		// now find destinations for cars 
		if (Setup.isBuildAggressive()){
			// perform a two pass build for this train
			// find destination for 50% of the available moves
			if (!placeCars(50))
				return;	// build failed
		}
		if (!placeCars(100))
			return;	// build failed

		// done finding cars for this train!
		train.setCurrentLocation(train.getTrainDepartsRouteLocation());
		if (numberCars < requested){
			train.setStatus(PARTIALBUILT + train.getNumberCarsWorked() +"/" + requested + " "+ rb.getString("cars"));
			addLine(buildReport, ONE, PARTIALBUILT + train.getNumberCarsWorked() +"/" + requested + " "+ rb.getString("cars"));
		}else{
			train.setStatus(BUILT + train.getNumberCarsWorked() + " "+ rb.getString("cars"));
			addLine(buildReport, ONE, BUILT + train.getNumberCarsWorked() + " "+ rb.getString("cars"));
		}
		train.setBuilt(true);
		buildReport.flush();
		buildReport.close();

		// now make manifest
		makeManifest();
		// now create and place train icon
		train.moveTrainIcon(train.getTrainDepartsRouteLocation());
		log.debug("Done building train "+train.getName());
	}
	
	/**
	 * Get the engines for this train. If departing from staging
	 * (departStageTrack != null) engines must come from that track.
	 * 
	 * @return true if engines found.
	 */
	private boolean getEngines(){
		// if not departing staging track and engines aren't required done!
		if (departStageTrack == null && reqNumEngines == 0)
			return true;
		
		numberEngines = 0;	
		int engineLength = 0;
		int engineWeight = 0;				
		// if leaving staging, use any number of engines if required number is 0
		boolean leavingStaging = false;
		if (departStageTrack != null)
			if (reqNumEngines == 0)
				leavingStaging = true;
			else if (departStageTrack.getNumberEngines() != reqNumEngines){
				addLine(buildReport, THREE, "Staging track ("+departStageTrack.getName()+") doesn't have the required number of engines");
				return false;
			}

		// get list of engines for this route		
		List<String> engineList = engineManager.getAvailableTrainList(train);
		// remove engines that are the wrong type, wrong track, wrong road name, or part of consist (not lead)
		addLine(buildReport, SEVEN, "Remove engines not serviced by this train");
		for (int indexEng=0; indexEng<engineList.size(); indexEng++){
			Engine engine = engineManager.getById(engineList.get(indexEng));
			// remove engines types that train does not service
			if (!train.acceptsTypeName(engine.getType())){
				addLine(buildReport, THREE, "Exclude engine ("+engine.getRoad()+" "+engine.getNumber()+"), type ("+engine.getType()+") is not serviced by this train");
				engineList.remove(indexEng);
				indexEng--;
				continue;
			}
			// remove engines models that train does not service
			if (!train.getEngineModel().equals("") && !engine.getModel().equals(train.getEngineModel())){
				addLine(buildReport, THREE, "Exclude engine ("+engine.getRoad()+" "+engine.getNumber()+"), model ("+engine.getModel()+") is not serviced by this train");
				engineList.remove(indexEng);
				indexEng--;
				continue;
			}
			// remove engines with roads that train does not service
			if (!train.getEngineRoad().equals("") && !engine.getRoad().equals(train.getEngineRoad())){
				addLine(buildReport, THREE, "Exclude engine ("+engine.getRoad()+" "+engine.getNumber()+"), road ("+engine.getRoad()+") is not serviced by this train");
				engineList.remove(indexEng);
				indexEng--;
				continue;
			}
			// remove engines with roads that train does not service
			if (train.getEngineRoad().equals("") && !train.acceptsRoadName(engine.getRoad())){
				addLine(buildReport, THREE, "Exclude engine ("+engine.getRoad()+" "+engine.getNumber()+"), road ("+engine.getRoad()+") is not serviced by this train");
				engineList.remove(indexEng);
				indexEng--;
				continue;
			}
			// remove engines with owners that train does not service
			if (!train.acceptsOwnerName(engine.getOwner())){
				addLine(buildReport, THREE, "Exclude engine ("+engine.getRoad()+" "+engine.getNumber()+"), owner ("+engine.getOwner()+") is not serviced by this train");
				engineList.remove(indexEng);
				indexEng--;
				continue;
			}
			// remove engines with built dates that train does not service
			if (!train.acceptsBuiltDate(engine.getBuilt())){
				addLine(buildReport, THREE, "Exclude engine ("+engine.getRoad()+" "+engine.getNumber()+"), built ("+engine.getBuilt()+") is not serviced by this train");
				engineList.remove(indexEng);
				indexEng--;
				continue;
			}
			// remove engines on tracks that don't service the train's departure direction
			if (!checkPickUpTrainDirection(engine, train.getRoute().getLocationById(routeList.get(0)))){
				engineList.remove(indexEng);
				indexEng--;
				continue;
			}
			// remove engines that have been assigned destinations that don't match the terminal 
			if (engine.getDestination() != null && !engine.getDestination().equals(terminateLocation)){
				addLine(buildReport, THREE, "Exclude engine ("+engine.getRoad()+" "+engine.getNumber()+") it has an assigned destination ("+engine.getDestination().getName()+")");
				engineList.remove(indexEng);
				indexEng--;
				continue;
			}
			// remove engines that aren't departing from the selected staging track (departStageTrack != null if staging)
			if(!engine.getLocationName().equals(train.getTrainDepartsName()) || ((departStageTrack != null && !engine.getTrackName().equals(departStageTrack.getName())))){
				addLine(buildReport, THREE, "Exclude engine ("+engine.getRoad()+" "+engine.getNumber()+") not on departure track");
				engineList.remove(indexEng);
				indexEng--;
				continue;
			}
			// is this engine part of a consist?  
			if (engine.getConsist() == null){
				// single engine, but does the train require a consist?
				if (reqNumEngines > 1){
					addLine(buildReport, THREE, "Exclude single engine ("+engine.getRoad()+" "+engine.getNumber()+") train requires "+ reqNumEngines +" engines");
					engineList.remove(indexEng);
					indexEng--;
					continue;
				}
			// engine is part of a consist
			}else{
				// Keep only lead engines in consist if required number is correct.
				if (!engine.getConsist().isLeadEngine(engine)){
					addLine(buildReport, THREE, "Engine ("+engine.getRoad()+" "+engine.getNumber()+") is part of consist ("+engine.getConsist().getName()+") and has " + engine.getConsist().getEngines().size() + " engines");
					// remove non-lead engines
					engineList.remove(indexEng);
					indexEng--;
					continue;
				// lead engine in consist
				}else{
					addLine(buildReport, THREE, "Engine ("+engine.getRoad()+" "+engine.getNumber()+") is lead engine for consist ("+engine.getConsist().getName()+") and has " + engine.getConsist().getEngines().size() + " engines");
					List<Engine> cEngines = engine.getConsist().getEngines();
					if (cEngines.size() == reqNumEngines || leavingStaging){
						log.debug("Consist ("+engine.getConsist().getName()+") has the required number of engines");
					}else{
						log.debug("Consist ("+engine.getConsist().getName()+") doesn't have the required number of engines");
						addLine(buildReport, THREE, "Exclude consist ("+engine.getConsist().getName()+") wrong number of engines");
						engineList.remove(indexEng);
						indexEng--;
						continue;
					}
				}
			} 
		}
		// departing staging with 0 engines?
		// test to see if departure track has engines assigned to another train
		if (leavingStaging && engineList.size() == 0 && departStageTrack.getNumberEngines()>0){
			addLine(buildReport, THREE, "Staging track ("+departStageTrack.getName()+") is already assigned to a train");
			return false;	// can't use this track		
		}
		// test to see if departure track has cars assigned to another train
		if (leavingStaging && engineList.size() == 0 && departStageTrack.getNumberCars()>0){
			List<String> carList = carManager.getByIdList();
			for (int i=0; i<carList.size(); i++){
				Car car = carManager.getById(carList.get(i));
				if (car.getTrackName().equals(departStageTrack.getName()) && car.getRouteDestination()!=null){
					addLine(buildReport, THREE, "Cars on staging track ("+departStageTrack.getName()+") are already assigned to a train");
					return false;	// can't use this track		
				}
			}
		}
		// show how many engines were found
		if (engineList.size()>0){
			if (reqNumEngines > 1)
				addLine(buildReport, SEVEN, "Found "+engineList.size()+" consist(s) for this train, now search for termination track");
			else
				addLine(buildReport, SEVEN, "Found "+engineList.size()+" engine(s) for this train, now search for termination track");
		}
		// now find terminal track for engine(s)
		Track terminateTrack = terminateStageTrack;
		for (int indexEng=0; indexEng<engineList.size(); indexEng++){
			Engine engine = engineManager.getById(engineList.get(indexEng));
			addLine(buildReport, FIVE, "Engine ("+engine.getRoad()+" "+engine.getNumber()+") road ("+engine.getRoad()+") model ("+engine.getModel()+") type ("+engine.getType()+")");
			addLine(buildReport, SEVEN, " at location ("+engine.getLocationName()+", "+engine.getTrackName()+")");
			train.setLeadEngine(engine);	//load lead engine
			// find a track for engine(s) at destination
			if (terminateTrack == null){
				List<String> destTracks = terminateLocation.getTracksByMovesList(null);
				for (int s = 0; s < destTracks.size(); s++){
					terminateTrack = terminateLocation.getTrackById(destTracks.get(s));
					if (terminateTrack.getLocType().equals(Track.STAGING) && (terminateTrack.getNumberRS()>0 || terminateTrack.getDropRS()>0)){
						terminateTrack = null;
						continue;
					}
					String status = engine.testDestination(terminateLocation, terminateTrack);
					if(status == Engine.OKAY){
						break;
					} 
					addLine(buildReport, FIVE, "Can't drop engine ("+engine.getRoad()+" "+engine.getNumber()+") to track (" +terminateTrack.getName()+") due to "+status);
					terminateTrack = null;
				}
			}
			if (terminateTrack == null && (reqNumEngines>0 || leavingStaging)){
				addLine(buildReport, ONE, MessageFormat.format(rb.getString("buildNoDestEngine"),new Object[]{engine.getRoad()+" "+engine.getNumber(),
				terminateLocation.getName(), train.getName()}));
			}
			if (terminateTrack != null){
				if (engine.getConsist() != null){
					List<Engine> cEngines = engine.getConsist().getEngines();
					if (cEngines.size() == reqNumEngines || leavingStaging){
						engineLength = engine.getConsist().getLength();
						for (int j=0; j<cEngines.size(); j++){
							numberEngines++;
							Engine cEngine = cEngines.get(j);
							addLine(buildReport, ONE, MessageFormat.format(rb.getString("buildEngineAssigned"),new Object[]{cEngine.getRoad()+" "+cEngine.getNumber(), terminateLocation.getName(), terminateTrack.getName()}));
							cEngine.setTrain(train);
							cEngine.setRouteLocation(train.getTrainDepartsRouteLocation());
							cEngine.setRouteDestination(train.getTrainTerminatesRouteLocation());
							cEngine.setDestination(terminateLocation, terminateTrack);
							int cWeight = 0;
							try {
								cWeight = Integer.parseInt(cEngine.getWeightTons());
							} catch (NumberFormatException e){
								log.warn("engine ("+cEngine.getId()+") does not have a valid weight");
							}
							engineWeight = engineWeight + cWeight;
						}
						break;  // done with loading engines
						// consist has the wrong number of engines, remove 	
					} 
					addLine(buildReport, THREE, "Exclude engine ("+engine.getRoad()+" "+engine.getNumber()+") consist ("+engine.getConsist().getName()+") number of engines (" +cEngines.size()+ ")");
					engineList.remove(indexEng);
					indexEng--;
					// engine isn't part of a consist
				} else if (reqNumEngines ==1 || leavingStaging){
					numberEngines++;
					addLine(buildReport, ONE, MessageFormat.format(rb.getString("buildEngineAssigned"),new Object[]{engine.getRoad()+" "+engine.getNumber(), terminateLocation.getName(), terminateTrack.getName()}));
					engine.setTrain(train);
					engine.setRouteLocation(train.getTrainDepartsRouteLocation());
					engine.setRouteDestination(train.getTrainTerminatesRouteLocation());
					engine.setDestination(terminateLocation, terminateTrack);
					engineLength = Integer.parseInt(engine.getLength());
					try {
						engineWeight = Integer.parseInt(engine.getWeightTons());
					} catch (NumberFormatException e){
						log.warn("engine ("+engine.getId()+") does not have a valid weight");
						engineWeight = 0;
					}
					break;  // done with loading engine
				}
			}
		}
		if (numberEngines < reqNumEngines){
			addLine(buildReport, ONE, rb.getString("buildCouldNotFindEng"));
			return false;
		}
		
		// set the engine length and weight for locations
		for (int i=0; i<routeList.size(); i++){
			RouteLocation rl = train.getRoute().getLocationById(routeList.get(i));
			rl.setTrainLength(engineLength);		// load the engine(s) length
			rl.setTrainWeight(engineWeight);		// load the engine(s) weight
		}
		// terminating into staging without engines?
		if (terminateTrack == null && engineList.size() == 0){
			// Use previously found staging track if there's one
			terminateTrack = terminateStageTrack;
		}
		if (terminateTrack != null && terminateTrack.getLocType().equals(Track.STAGING)){
			train.getTrainTerminatesRouteLocation().setStagingTrack(terminateTrack);
		}
		return true;
	}
	
	// returns the number of engines needed for this train, minimum 1, 
	// maximum user specified in setup.
	// Based on maximum allowable train length and grade between locations,
	// and the maximum cars that the train can have at the maximum train length.
	// One engine per sixteen 40' cars for 1% grade.
	// TODO Currently ignores the cars weight and engine horsepower
	private int getAutoEngines(){
		double numberEngines = 1;
		int moves = 0;
		
		for (int i=0; i<routeList.size()-1; i++){
			RouteLocation rl = train.getRoute().getLocationById(routeList.get(i));
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
		addLine(buildReport, ONE, MessageFormat.format(rb.getString("buildAutoBuildMsg"),new Object[]{Integer.toString(nE)}));
		if (nE > Setup.getEngineSize()){
			addLine(buildReport, THREE, "The maximum number of engines that can be assigned is "+Setup.getEngineSize());
			nE = Setup.getEngineSize();
		} 
		return nE;
	}
	
	/**
	 * Adds a caboose or car with FRED to the train if needed.  Caboose or
	 * car with FRED must travel with the train to the last location in the
	 * route.  Also removes all cabooses and cars with FRED that aren't needed by train.
	 * @return true if the correct caboose or car with FRED is found and is also accepted by
	 * the last location in route.
	 */
	private boolean getCabooseOrFred(){
		// get any requirements for this train
		boolean requiresCaboose = false;		// start off without any requirements
		boolean requiresFred = false;
		boolean foundFred = true;
		boolean foundCaboose = true;
		String textRequires = rb.getString("None");
		if (train.getRequirements()>0){
			if ((train.getRequirements()& Train.FRED) > 0){
				requiresFred = true;
				foundFred = false;
				textRequires = rb.getString("FRED");
			} 
			if ((train.getRequirements()& Train.CABOOSE) > 0){
				requiresCaboose = true;
				foundCaboose = false;
				textRequires = rb.getString("Caboose");
			}
			if (!train.getCabooseRoad().equals("")){
				textRequires += " road ("+train.getCabooseRoad()+")";
			}
			addLine(buildReport, ONE, MessageFormat.format(rb.getString("buildTrainRequires"),new Object[]{train.getName(), textRequires}));
		} else {
			addLine(buildReport, SEVEN, "Train does not require caboose or car with FRED");
		}
		// now go through the car list and find a caboose or FRED if required
		// first pass, try and find a caboose that matches the engine's road
		if(requiresCaboose && train.getCabooseRoad().equals("") && train.getLeadEngine() != null){
			for (carIndex=0; carIndex<carList.size(); carIndex++){
				Car car = carManager.getById(carList.get(carIndex));
				if (car.isCaboose() && car.getLocationName().equals(train.getTrainDepartsName()) && car.getRoad().equals(train.getLeadEngine().getRoad())
						&& checkPickUpTrainDirection(car, train.getRoute().getLocationById(routeList.get(0)))){
					if (car.getDestination() == null || car.getDestination() == terminateLocation){
						addLine(buildReport, ONE, MessageFormat.format(rb.getString("buildFoundCaboose"),new Object[]{car.getRoad()+" "+car.getNumber()}));
						// found a caboose with road that matches engine, now remove all other cabooses from list
						for (int i=0; i<carList.size(); i++){
							Car testCar = carManager.getById(carList.get(i));
							if (testCar.isCaboose() && testCar != car){
								// need to keep caboose if departing staging
								if (departStageTrack == null || testCar.getTrack() != departStageTrack){
									addLine(buildReport, FIVE, "Exclude caboose ("+testCar.getRoad()+" "+testCar.getNumber()+") at location ("+testCar.getLocationName()+", "+testCar.getTrackName()+")");
									carList.remove(carList.get(i));		// remove this car from the list
									i--;
								}
							}
						}
						break;	// found a caboose matches engine and removed all others from list
					}
				}
			}
		}
		// second pass looking for caboose or car with FRED and if not needed remove
		for (carIndex=0; carIndex<carList.size(); carIndex++){
			Car c = carManager.getById(carList.get(carIndex));
			// find a caboose or card with FRED for this train if needed
			// check for caboose or car with FRED
			if (c.isCaboose()){
				addLine(buildReport, FIVE, "Car ("+c.getRoad()+" "+c.getNumber()+") is a caboose, road (" +c.getRoad()+ ")");
				if (departStageTrack != null && c.getTrack() == departStageTrack) 
					foundCaboose = false;		// must move caboose from staging   
			}
			if (c.hasFred()){
				addLine(buildReport, FIVE, "Car ("+c.getRoad()+" "+c.getNumber()+") has a FRED, road (" +c.getRoad()+ ")");
				if (departStageTrack != null && c.getTrack() == departStageTrack) 
					foundFred = false;		// must move car with FRED from staging
			}
			
			// remove cabooses and cars with FRED if not needed for train
			if (c.isCaboose() && foundCaboose || c.hasFred() && foundFred){
				addLine(buildReport, THREE, "Exclude car ("+c.getRoad()+" "+c.getNumber()+") type ("+c.getType()+") at location ("+c.getLocationName()+", "+c.getTrackName()+")");
				carList.remove(carList.get(carIndex));		// remove this car from the list
				carIndex--;
				continue;
			}
			// caboose or car with FRED is needed for train, search for one
			if (c.isCaboose() && !foundCaboose || c.hasFred() && !foundFred){
				// remove cars with the wrong road
				if (!train.getCabooseRoad().equals("") && !train.getCabooseRoad().equals(c.getRoad()) && departStageTrack == null){
					addLine(buildReport, THREE, "Exclude car ("+c.getRoad()+" "+c.getNumber()+") type ("+c.getType()+") wrong road ("+c.getRoad()+")");
					carList.remove(carList.get(carIndex));		// remove this car from the list
					carIndex--;
					continue;
				}
				// remove cars not at departure
				if(!c.getLocationName().equals(train.getTrainDepartsName())){
					addLine(buildReport, THREE, "Exclude car ("+c.getRoad()+" "+c.getNumber()+") type ("+c.getType()+") wrong location ("+c.getLocation()+")");
					carList.remove(carList.get(carIndex));		// remove this car from the list
					carIndex--;
					continue;
				}
				// remove cars that can't be picked up due to train and track directions
				if(!checkPickUpTrainDirection(c, train.getRoute().getLocationById(routeList.get(0)))){
					addLine(buildReport, THREE, "Exclude car ("+c.getRoad()+" "+c.getNumber()+") type ("+c.getType()+ ") at location ("+c.getLocationName()+" "+c.getTrackName()+")");
					carList.remove(carList.get(carIndex));		// remove this car from the list
					carIndex--;
					continue;
				}
				// has the car been assigned a destination?
				if (c.getDestination() != null && c.getDestination() != terminateLocation && departStageTrack == null){
					addLine(buildReport, THREE, "Exclude car ("+c.getRoad()+" "+c.getNumber()+") type ("+c.getType()+") wrong destination ("+c.getDestinationName()+")");
					carList.remove(carList.get(carIndex));		// remove this car from the list
					carIndex--;
					continue;
				}
				// car meets all requirements, now find a destination track to place car
				if (train.getTrainTerminatesRouteLocation().getStagingTrack() == null){
					List<String> sls = terminateLocation.getTracksByMovesList(null);
					// TODO need to check to see if car has a destination track already assigned to it
					// loop through the destination tracks to find one that accepts caboose or car with FRED
					for (int s = 0; s < sls.size(); s++){
						Track destTrack = terminateLocation.getTrackById(sls.get(s));
						String status = c.testDestination(terminateLocation, destTrack);
						if (status.equals(Car.OKAY)){
							boolean carAdded = addCarToTrain(c, train.getTrainDepartsRouteLocation(), train.getTrainTerminatesRouteLocation(), terminateLocation, destTrack);
							if (carAdded && c.isCaboose())
								foundCaboose = true;
							if (carAdded && c.hasFred())
								foundFred = true;
							break;
						} 
						addLine(buildReport, SEVEN, "Can't drop car ("+c.getRoad()+" "+c.getNumber()+") to track (" +destTrack.getName()+") due to "+status);
					}
					// if departing staging this is a build failure
					if ((c.isCaboose() && !foundCaboose) || (c.hasFred() && !foundFred)){
						addLine(buildReport, THREE, "Could not find a destination for ("+c.getRoad()+" "+c.getNumber()+")");
						if (departStageTrack != null && c.getTrack() == departStageTrack){
							buildFailed(rb.getString("buildErrorCaboose"));
							return false;
						}
					}
				// terminate into staging
				} else {
					String status = c.testDestination(terminateLocation, train.getTrainTerminatesRouteLocation().getStagingTrack());
					if (status.equals(Car.OKAY)){
						boolean carAdded = addCarToTrain(c, train.getTrainDepartsRouteLocation(), train.getTrainTerminatesRouteLocation(), terminateLocation, train.getTrainTerminatesRouteLocation().getStagingTrack());
						if (carAdded && c.isCaboose())
							foundCaboose = true;
						if (carAdded && c.hasFred())
							foundFred = true;
					} else {
						addLine(buildReport, SEVEN, "Can't drop car ("+c.getRoad()+" "+c.getNumber()+") to track (" +train.getTrainTerminatesRouteLocation().getStagingTrack().getName()+") due to "+status);
					}
				} 
				// remove caboose or FRED from list couldn't find a destination track
				if((c.isCaboose() && !foundCaboose) || (c.hasFred() && !foundFred)) {
					addLine(buildReport, THREE, "Exclude car ("+c.getRoad()+" "+c.getNumber()+") type ("+c.getType()+ ") at location ("+c.getLocationName()+" "+c.getTrackName()+")");
					carList.remove(carList.get(carIndex));		// remove this car from the list
					carIndex--;
				}
			}
		}
		// did we find a needed caboose or FRED?
		if (requiresFred && !foundFred || requiresCaboose && !foundCaboose){
			buildFailed(MessageFormat.format(rb.getString("buildErrorRequirements"),
					new Object[]{train.getName(), textRequires, train.getTrainDepartsName(), train.getTrainTerminatesName()}));
			return false;
		}
		return true;
	}
	
	/**
	 * Remove unwanted cars from the car list.
	 * @return true if no errors
	 */
	private boolean removeCars(int requested){
		// get list of cars for this route
		carList = carManager.getAvailableTrainList(train);
		// TODO: DAB this needs to be controlled by each train
		if (requested > carList.size() && Control.fullTrainOnly){
			buildFailed(MessageFormat.format(rb.getString("buildErrorNumReq"),new Object[]{Integer.toString(requested),
				train.getName(), Integer.toString(carList.size())}));
			return false;
		}

    	// remove cars that don't have a valid track, interchange, road, or type for this train
		addLine(buildReport, SEVEN, "Remove cars not serviced by this train");
		for (carIndex=0; carIndex<carList.size(); carIndex++){
    		Car c = carManager.getById(carList.get(carIndex));
    		// remove cars that don't have a valid track
    		if (c.getTrack() == null){
    			addLine(buildReport, ONE, MessageFormat.format(rb.getString("buildErrorCarNoLoc"),new Object[]{c.getRoad()+" "+c.getNumber(), c.getLocationName(), c.getTrackName()}));
				carList.remove(carList.get(carIndex));
				carIndex--;
				continue;
    		}
    		// all cars in staging must be accepted, so don't exclude if in staging
    		// note that for trains departing staging the engine and car roads and types were
    		// checked in the routine checkDepartureStagingTrack().
    		if (departStageTrack == null || !c.getTrack().getName().equals(departStageTrack.getName())){
    			if (!train.acceptsRoadName(c.getRoad())){
    				addLine(buildReport, FIVE, "Exclude car ("+c.getRoad()+" "+c.getNumber()+") road ("+c.getRoad()+") at location ("+c.getLocationName()+", "+c.getTrackName()+")");
    				carList.remove(carList.get(carIndex));
    				carIndex--;
    				continue;
    			}
    			if (!train.acceptsTypeName(c.getType())){
    				addLine(buildReport, FIVE, "Exclude car ("+c.getRoad()+" "+c.getNumber()+") type ("+c.getType()+") at location ("+c.getLocationName()+", "+c.getTrackName()+")");
    				carList.remove(carList.get(carIndex));
    				carIndex--;
    				continue;
    			}
    			if (!train.acceptsOwnerName(c.getOwner())){
    				addLine(buildReport, FIVE, "Exclude car ("+c.getRoad()+" "+c.getNumber()+") owner ("+c.getOwner()+") at location ("+c.getLocationName()+", "+c.getTrackName()+")");
    				carList.remove(carList.get(carIndex));
    				carIndex--;
    				continue;
    			}
       			if (!train.acceptsBuiltDate(c.getBuilt())){
    				addLine(buildReport, FIVE, "Exclude car ("+c.getRoad()+" "+c.getNumber()+") built ("+c.getBuilt()+") at location ("+c.getLocationName()+", "+c.getTrackName()+")");
    				carList.remove(carList.get(carIndex));
    				carIndex--;
    				continue;
    			}
    		}
    		// is car at interchange?
    		if (c.getTrack().getLocType().equals(Track.INTERCHANGE)){
    			// don't service a car at interchange and has been dropped of by this train
    			if (c.getTrack().getPickupOption().equals(Track.ANY) && c.getSavedRouteId().equals(train.getRoute().getId())){
    				addLine(buildReport, THREE, "Exclude car ("+c.getRoad()+" "+c.getNumber()+") previously droped by this train at interchange ("+c.getLocationName()+", "+c.getTrackName()+")");
    				carList.remove(carList.get(carIndex));
    				carIndex--;
    				continue;
    			}
    			if (c.getTrack().getPickupOption().equals(Track.TRAINS)){
    				if (c.getTrack().acceptsPickupTrain(train)){
    					log.debug("Car ("+c.getRoad()+" "+c.getNumber()+") can be picked up by this train");
    				} else {
    					addLine(buildReport, THREE, "Exclude car ("+c.getRoad()+" "+c.getNumber()+") by train, can't pickup this car at interchange ("+c.getLocationName()+", "+c.getTrackName()+")");
    					carList.remove(carList.get(carIndex));
    					carIndex--;
    					continue;
    				}
    			}
    			else if (c.getTrack().getPickupOption().equals(Track.ROUTES)){
    				if (c.getTrack().acceptsPickupRoute(train.getRoute())){
    					log.debug("Car ("+c.getRoad()+" "+c.getNumber()+") can be picked up by this route");
    				} else {
    					addLine(buildReport, THREE, "Exclude car ("+c.getRoad()+" "+c.getNumber()+") by route, can't pickup this car at interchange ("+c.getLocationName()+", "+c.getTrackName()+")");
    					carList.remove(carList.get(carIndex));
    					carIndex--;
    					continue;
    				}
    			}
    		}
		}

		addLine(buildReport, ONE, MessageFormat.format(rb.getString("buildFoundCars"),new Object[]{Integer.toString(carList.size()), train.getName()}));

		// adjust car list to only have cars from one staging track
		if (departStageTrack != null){
			// Make sure that all cars in staging are moved
			train.getTrainDepartsRouteLocation().setCarMoves(train.getTrainDepartsRouteLocation().getMaxCarMoves()-departStageTrack.getNumberCars());  // negative number moves more cars
			int numCarsFromStaging = 0; 
			for (carIndex=0; carIndex<carList.size(); carIndex++){
				Car c = carManager.getById(carList.get(carIndex));
//				addLine(buildReport, "Check car ("+c.getRoad()+" "+c.getNumber()+") at location ("+c.getLocationName()+" "+c.getTrackName()+")");
				if (c.getLocationName().equals(departLocation.getName())){
					if (c.getTrackName().equals(departStageTrack.getName())){
						addLine(buildReport, THREE, "Staging car ("+c.getRoad()+" "+c.getNumber()+") at location ("+c.getLocationName()+", "+c.getTrackName()+")");
						numCarsFromStaging++;
					} else {
						addLine(buildReport, THREE, "Exclude car ("+c.getRoad()+" "+c.getNumber()+") at location ("+c.getLocationName()+", "+c.getTrackName()+")");
						carList.remove(carList.get(carIndex));
						carIndex--;
					}
				}
			}
			// error if all of the cars in staging aren't available
			if (numCarsFromStaging + numberEngines != departStageTrack.getNumberRS()){
				buildFailed(MessageFormat.format(rb.getString("buildErrorNotAll"),
						new Object[]{Integer.toString(departStageTrack.getNumberRS()- (numCarsFromStaging + numberEngines))}));
				return false;
			}
		}
		// now go through the car list and remove non-lead cars in kernels, destinations that aren't part of this route
		for (carIndex=0; carIndex<carList.size(); carIndex++){
			Car c = carManager.getById(carList.get(carIndex));
			// only print out the first 500 cars
			if (carIndex < 500)
				addLine(buildReport, FIVE, "Car ("+c.getRoad()+" "+c.getNumber()+") at location (" +c.getLocationName()+ ", " +c.getTrackName()+ ") with " + c.getMoves()+ " moves");
			if (carIndex == 500)
				addLine(buildReport, FIVE, " ************* Only the first 500 cars are shown ************* ");
			// use only the lead car in a kernel for building trains
			if (c.getKernel() != null){
				addLine(buildReport, FIVE, "Car ("+c.getRoad()+" "+c.getNumber()+") is part of kernel ("+c.getKernelName()+")");
				if (!c.getKernel().isLeadCar(c)){
					carList.remove(carList.get(carIndex));		// remove this car from the list
					carIndex--;
					continue;
				}
			}
			if (train.equals(c.getTrain())){
				addLine(buildReport, THREE, "Car ("+c.getRoad()+" "+c.getNumber()+") already assigned to this train");
			}
			// does car have a destination that is part of this train's route?
			if (c.getDestination() != null) {
				addLine(buildReport, THREE, "Car ("+c.getRoad()+" "+c.getNumber()+") has assigned destination (" +c.getDestination().getName()+ ")");
				RouteLocation rld = train.getRoute().getLastLocationByName(c.getDestination().getName());
				if (rld == null){
					addLine(buildReport, THREE, "Exclude car ("+c.getRoad()+" "+c.getNumber()+") destination (" +c.getDestination().getName()+ ") not part of this train's route (" +train.getRoute().getName() +")");
					// build failure if car departing staging
					if (c.getLocationName().equals(departLocation.getName()) && departStageTrack != null){
						buildFailed(MessageFormat.format(rb.getString("buildErrorCarNotPartRoute"),
								new Object[]{c.getRoad()+" "+c.getNumber()}));
						return false;
					}
					carList.remove(carList.get(carIndex));		// remove this car from the list
					carIndex--;
				}
			}
		}
		return true;
	}
	
	boolean multipass = false;
	/**
	 * Main routine to place cars into the train.  Can be called multiple times, percent
	 * controls how many cars are placed in any given pass.
	 * @return true if there were no errors
	 */
	private boolean placeCars(int percent){
		if (percent < 100){
			addLine(buildReport, THREE, "Multipass build, find destinations for "+percent+" percent of the available moves");
			multipass = true;
		}
		if (percent == 100 && multipass)
			addLine(buildReport, THREE, "Final build pass, find destinations for the remaining available moves");
		// determine how many locations are serviced by this train
		int numLocs = routeList.size();
		if (numLocs > 1)  // don't find car destinations for the last location in the route
			numLocs--;
		// now go through each location starting at departure and place cars as requested
		for (int routeIndex=0; routeIndex<numLocs; routeIndex++){
			RouteLocation rl = train.getRoute().getLocationById(routeList.get(routeIndex));
			if(train.skipsLocation(rl.getId())){
				addLine(buildReport, ONE, MessageFormat.format(rb.getString("buildLocSkipped"),new Object[]{rl.getName(), train.getName()}));
			}else if(!rl.canPickup()){
				addLine(buildReport, ONE, MessageFormat.format(rb.getString("buildLocNoPickups"),new Object[]{train.getRoute().getName(), rl.getName()}));
			}else{
				moves = 0;
				success = false;
				reqNumOfMoves = rl.getMaxCarMoves()-rl.getCarMoves();
				int saveReqMoves = reqNumOfMoves;	// save a copy for status message
				addLine(buildReport, THREE, "Location (" +rl.getName()+ ") requests " +reqNumOfMoves+ "/" +rl.getMaxCarMoves()+ " moves" );
				// multipass build?
				if (percent < 100)
					reqNumOfMoves = reqNumOfMoves*percent/100;
				if (reqNumOfMoves <= 0)
					success = true;
				while (reqNumOfMoves > 0){
					for (carIndex=0; carIndex<carList.size(); carIndex++){
						boolean noMoreMoves = true;  // false when there are are locations with moves
						Car c = carManager.getById(carList.get(carIndex));
						// find a car at this location
						if (!c.getLocationName().equals(rl.getName()))
							continue;
						// can this car be picked up?
						if(!checkPickUpTrainDirection(c, rl))
							continue; // no
						// does car have a destination?
						if (c.getDestination() != null) {
							addLine(buildReport, THREE, "Car ("+c.getRoad()+" "+c.getNumber()+") at location (" +c.getLocation()+ ") has assigned destination (" +c.getDestination()+ ")");
							RouteLocation rld = train.getRoute().getLastLocationByName(c.getDestination().getName());
							if (rld == null){
								addLine(buildReport, THREE, "Car ("+c.getRoad()+" "+c.getNumber()+") destination not part of route (" +train.getRoute().getName() +")");
							} else {
								if (c.getRouteLocation() != null){
									// this should not occur if train was reset before a build!
									addLine(buildReport, THREE, "Car ("+c.getRoad()+" "+c.getNumber()+") was assigned to this train");
								} 
								// now go through the route and try and find a location with
								// the correct destination name
								boolean carAdded = false;
								int locCount = 0;
								for (int k = routeIndex; k<routeList.size(); k++){
									rld = train.getRoute().getLocationById(routeList.get(k));
									if (rld.getName().equals(c.getDestinationName())){
										locCount++;	// show when this car would be dropped at location
										// are drops allows at this location?
										if (!rld.canDrop()){
											addLine(buildReport, THREE, "Route ("+train.getRoute().getName()+") does not allow drops at location ("+rld.getName()+") stop "+locCount);
										} else if (rld.getCarMoves() < rld.getMaxCarMoves()){
											// check for valid destination track
											if (c.getDestinationTrack() == null){
												addLine(buildReport, THREE, "Car ("+c.getRoad()+" "+c.getNumber()+") doesn't have a valid destination track");
												// is there a track assigned for staging cars?
												if (rld.getStagingTrack() != null){
													addLine(buildReport, THREE, "Car ("+c.getRoad()+" "+c.getNumber()+") assigned to staging track ("+rld.getStagingTrack().getName()+")");
													carAdded = addCarToTrain(c, rl, rld, c.getDestination(), rld.getStagingTrack());
												// no, find a destination track this this car
												} else {
													List<String> tracks = c.getDestination().getTracksByMovesList(null);
													for (int s = 0; s < tracks.size(); s++){
														Track testTrack = c.getDestination().getTrackById(tracks.get(s));
														// log.debug("track (" +testTrack.getName()+ ") has "+ testTrack.getMoves() + " moves");
														// need to find a track that is isn't the same as the car's current
														String status = c.testDestination(c.getDestination(), testTrack);
														// is the testTrack a siding with a Schedule?
														if (testTrack.getLocType().equals(Track.SIDING) && status.contains(Car.SCHEDULE) 
																&& status.contains(Car.LOAD) 
																&& checkDropTrainDirection(c, rld, c.getDestination(), testTrack)){
															addLine(buildReport, THREE, "Can't drop car ("+c.getRoad()+" "+c.getNumber()+") to track ("+testTrack.getName()+") because "+status);
															continue;
														}
														if (!status.equals(Car.OKAY)){
															addLine(buildReport, SEVEN, "Can't drop car ("+c.getRoad()+" "+c.getNumber()+") to track (" +testTrack.getName()+") due to "+status);
															continue;
														}
														if (testTrack != c.getTrack() 
																&& status.equals(Car.OKAY) 
																&& checkDropTrainDirection(c, rld, c.getDestination(), testTrack)){
															carAdded = addCarToTrain(c, rl, rld, c.getDestination(), testTrack);
															break;
														}
													}
												}
											} else {
												// going into staging?
												if (rld.getStagingTrack() == null  || rld.getStagingTrack() == c.getDestinationTrack())
												carAdded = addCarToTrain(c, rl, rld, c.getDestination(), c.getDestinationTrack());
											}
											// done?
											if (carAdded)
												break;	//yes
											else
												addLine(buildReport, THREE, "Car ("+c.getRoad()+" "+c.getNumber()+") can not be delivered to (" + c.getDestination() + ") stop "+locCount);
										} else {
											addLine(buildReport, THREE, "No available moves for destination ("+rld.getName()+") stop "+locCount);
										}		
									}
								}
								// done with this location?
								if (carAdded && success)
									break;	//yes
								// build failure if car departing staging
								if (!carAdded && c.getLocationName().equals(departLocation.getName()) && departStageTrack != null){
									buildFailed(MessageFormat.format(rb.getString("buildErrorCarStageDest"),
											new Object[]{c.getRoad()+" "+c.getNumber()}));
									return false;
								}
							}
						// car does not have a destination, search for one	
						} else {
							addLine(buildReport, FIVE, "Find destinations for car ("+c.getRoad()+" "+c.getNumber()+") at location (" +c.getLocationName()+", " +c.getTrackName()+ ")");
							int start = routeIndex;					// start looking after car's current location
							RouteLocation rld = null;				// the route location destination being checked for the car
							RouteLocation rldSave = null;			// holds the best route location destination for the car
							Track trackSave = null;					// holds the best track at destination for the car
							Location destinationSave = null;		// holds the best destination for the car
							boolean done = false;					// when true car destination found

							// more than one location in this route?
							if (routeList.size()>1)
								start++;		//yes!, no car drops at departure
							for (int k = start; k<routeList.size(); k++){
								rld = train.getRoute().getLocationById(routeList.get(k));
								if (rld.canDrop()){
									addLine(buildReport, SEVEN, "Searching location ("+rld.getName()+") for possible destination");
								}else{
									addLine(buildReport, FIVE, "Route ("+train.getRoute().getName()+") does not allow drops at location ("+rld.getName()+")");
									continue;
								}							
								// don't move car to same location unless the route only has one location (local moves)
								if (rl.getName().equals(rld.getName()) && routeList.size() != 1){
									addLine(buildReport, SEVEN, "Car ("+c.getRoad()+" "+c.getNumber()+") location is equal to destination ("+rld.getName()+"), skiping this destination");
									continue;
								}
								// any moves left at this location?
								if (rld.getMaxCarMoves()-rld.getCarMoves()<=0){
									addLine(buildReport, FIVE, "No available moves for destination ("+rld.getName()+")");
									continue;
								}
								// get a "test" destination and a list of the track locations available
								noMoreMoves = false;
								Location destinationTemp = null;
								Track trackTemp = null;
								Location testDestination = locationManager.getLocationByName(rld.getName());
								if (testDestination == null){
									buildFailed(MessageFormat.format(rb.getString("buildErrorRouteLoc"),
											new Object[]{train.getRoute().getName(), rld.getName()}));
									return false;
								}
								// is there a track assigned for staging cars?
								if (rld.getStagingTrack() == null){
									// no staging track assigned, start search
									List<String> tracks = testDestination.getTracksByMovesList(null);
									for (int s = 0; s < tracks.size(); s++){
										Track testTrack = testDestination.getTrackById(tracks.get(s));
										// log.debug("track (" +testTrack.getName()+ ") has "+ testTrack.getMoves() + " moves");
										// need to find a track that is isn't the same as the car's current
										String status = c.testDestination(testDestination, testTrack);
										// is the testTrack a siding with a Schedule?
										if(testTrack.getLocType().equals(Track.SIDING) && status.contains(Car.SCHEDULE) 
												&& status.contains(Car.LOAD) 
												&& checkDropTrainDirection(c, rld, testDestination, testTrack)){
											log.debug("Siding ("+testTrack.getName()+") has "+status);
											// is car departing a staging track that can generate schedule loads?
											if (c.getTrack().isAddLoadsEnabled() && c.getLoad().equals(CarLoads.instance().getDefaultEmptyName())){
												Schedule sch = ScheduleManager.instance().getScheduleByName(testTrack.getScheduleName());
												ScheduleItem si = sch.getItemById(testTrack.getScheduleItemId());
												addLine(buildReport, FIVE, "Adding schedule load ("+si.getLoad()+") to car ("+c.getRoad()+" "+c.getNumber()+")");
												c.setLoad(si.getLoad());
												status = Car.OKAY;
												trackTemp = testTrack;
												destinationTemp = testDestination;
												rldSave = rld;
												destinationSave = destinationTemp;
												trackSave = trackTemp;
												done = true;
												break;
											}
										}		
										if (testTrack != c.getTrack() 
												&& status.equals(Car.OKAY) 
												&& checkDropTrainDirection(c, rld, testDestination, testTrack)){
											// staging track with zero cars?  TODO (fix testTrack.getDropRS()>0, covers case when caboose or FRED to staging without engines)
											if (testTrack.getLocType().equals(Track.STAGING) && (testTrack.getNumberRS() == 0 || testTrack.getDropRS()>0)){
												rld.setStagingTrack(testTrack);	// Use this location for all cars
												trackTemp = testTrack;
												destinationTemp = testDestination;
												break;
											}
											// No local moves from siding to siding
											if (routeList.size() == 1 && testTrack.getLocType().equals(Track.SIDING) && c.getTrack().getLocType().equals(Track.SIDING)){
												addLine(buildReport, FIVE, "Local siding to siding move not allowed (" +testTrack.getName()+ ")");
												continue;
											}
											// No local moves from yard to yard
											if (routeList.size() == 1 && testTrack.getLocType().equals(Track.YARD) && c.getTrack().getLocType().equals(Track.YARD)){
												addLine(buildReport, FIVE, "Local yard to yard move not allowed (" +testTrack.getName()+ ")");
												continue;
											}
											// No local moves from interchange to interchange
											if (routeList.size() == 1 && testTrack.getLocType().equals(Track.INTERCHANGE) && c.getTrack().getLocType().equals(Track.INTERCHANGE)){
												addLine(buildReport, FIVE, "Local interchange to interchange move not allowed (" +testTrack.getName()+ ")");
												continue;
											}
											// drop to interchange?
											if (testTrack.getLocType().equals(Track.INTERCHANGE)){
												if (testTrack.getDropOption().equals(Track.TRAINS)){
													if (testTrack.acceptsDropTrain(train)){
														log.debug("Car ("+c.getRoad()+" "+c.getNumber()+") can be droped by train to interchange (" +testTrack.getName()+")");
													} else {
														addLine(buildReport, FIVE, "Can't drop car ("+c.getRoad()+" "+c.getNumber()+") by train to interchange (" +testTrack.getName()+")");
														continue;
													}
												}
												if (testTrack.getDropOption().equals(Track.ROUTES)){
													if (testTrack.acceptsDropRoute(train.getRoute())){
														log.debug("Car ("+c.getRoad()+" "+c.getNumber()+") can be droped by route to interchange (" +testTrack.getName()+")");
													} else {
														addLine(buildReport, FIVE, "Can't drop car ("+c.getRoad()+" "+c.getNumber()+") by route to interchange (" +testTrack.getName()+")");
														continue;
													}
												}
											}
											// not staging, then use
											if (!testTrack.getLocType().equals(Track.STAGING)){
												trackTemp = testTrack;
												destinationTemp = testDestination;
												break;
											}
										}
										// car's current track is the test track or car can't be dropped
										if(!status.equals(Car.OKAY)){
											if (status.equals(Car.SCHEDULE))
												addLine(buildReport, SEVEN, "Can't drop car ("+c.getRoad()+" "+c.getNumber()+") load ("+c.getLoad()+") to track (" +testTrack.getName()+") due to "+status);
											else
												addLine(buildReport, SEVEN, "Can't drop car ("+c.getRoad()+" "+c.getNumber()+") to track (" +testTrack.getName()+") due to "+status);
										}
									}
								// all cars in this train go to one staging track
								} else {
									// will staging accept this car?
									String status = c.testDestination(testDestination, rld.getStagingTrack());
									if (status.equals(Car.OKAY)){
										trackTemp = rld.getStagingTrack();
										destinationTemp = testDestination;
									} else {
										addLine(buildReport, SEVEN, "Can't drop car ("+c.getRoad()+" "+c.getNumber()+") to track (" +rld.getStagingTrack().getName()+") due to "+status);
									}
								}
								// check to see if train length would be exceeded by this car
								if(destinationTemp != null){
									if (!checkTrainLength(c, rl, rld)){
										// train length exceeded
										destinationTemp = null;
									}
								}
								// check for programming error
								if(destinationTemp != null){
									if(trackTemp == null){
										buildFailed("Build Failure, trackTemp is null!");
										return false;
									}
									addLine(buildReport, THREE, "Car ("+c.getRoad()+" "+c.getNumber()+") can drop to (" +destinationTemp.getName()+ ", " +trackTemp.getName()+ ") with " 
											+rld.getCarMoves()+ "/" +rld.getMaxCarMoves()+" moves");
									// if there's more than one available destination use the one with the least moves
									if (rldSave != null){
										double saveCarMoves = rldSave.getCarMoves();
										double saveRatio = saveCarMoves/rldSave.getMaxCarMoves();
										double nextCarMoves = rld.getCarMoves();
										double nextRatio = nextCarMoves/rld.getMaxCarMoves();
										// bias cars to the terminal 
										if (rld.getName().equals(terminateLocation.getName())){
											log.debug("Location "+rld.getName()+" is terminate location "+Double.toString(nextRatio));
											nextRatio = nextRatio * nextRatio;
										}
										log.debug(rldSave.getName()+" = "+Double.toString(saveRatio)+ " " + rld.getName()+" = "+Double.toString(nextRatio));
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
									addLine(buildReport, FIVE, "Could not find a valid destination for car ("+c.getRoad()+" "+c.getNumber()+") at location (" + rld.getName()+")");
								}
								if (done) // scheduled load has been placed into car
									break;
							}
							boolean carAdded = false; // all cars departing staging must be included or build failure
							if (destinationSave != null){
								carAdded = addCarToTrain(c, rl, rldSave, destinationSave, trackSave);
								if (carAdded && success){
									//log.debug("done with location ("+destinationSave.getName()+")");
									break;
								}
							} 
							// car leaving staging without a destination?
							if (c.getTrack().getLocType().equals(Track.STAGING) && (!carAdded  || destinationSave == null)){
								buildFailed(MessageFormat.format(rb.getString("buildErrorCarDest"),
										new Object[]{c.getRoad()+" "+c.getNumber(), c.getLocationName()}));
								return false;				
							} 
							// are there still moves available?
							if (noMoreMoves) {
								addLine(buildReport, FIVE, "No available destinations for any car");
								reqNumOfMoves = 0;
								break;
							}
						}
					}
					// could not find enough cars
					reqNumOfMoves = 0;
					// don't use this location again
					//rl.setCarMoves(rl.getMaxCarMoves());
				}
				addLine(buildReport, ONE, MessageFormat.format(rb.getString("buildStatusMsg"),new Object[]{(success? rb.getString("Success"): rb.getString("Partial")),
					Integer.toString(moves), Integer.toString(saveReqMoves), rl.getName(), train.getName()}));
			}
		}
		return true;
	}
	
	/**
	 * Add car to train
	 * @param car
	 * @param rl the planned origin for this car
	 * @param rld the planned destination for this car
	 * @param destination
	 * @param track the final destination for car
	 * @return true if car was successfully added to train.  Also makes boolean
	 * boolean "success" true if location doesn't need any more pickups. 
	 */
	private boolean addCarToTrain(Car car, RouteLocation rl, RouteLocation rld, Location destination, Track track){
		if (checkTrainLength(car, rl, rld)){
			// car could be part of a kernel
			if (car.getKernel()!=null){
				List<Car> kCars = car.getKernel().getCars();
				addLine(buildReport, THREE, "Car ("+car.getRoad()+" "+car.getNumber()+") is part of kernel ("+car.getKernelName()+") with "+ kCars.size() +" cars");
				// log.debug("kernel length "+car.getKernel().getLength());
				for(int i=0; i<kCars.size(); i++){
					Car kCar = kCars.get(i);
					addLine(buildReport, THREE, "Car ("+kCar.getRoad()+" "+kCar.getNumber()+") assigned destination ("+destination.getName()+", "+track.getName()+")");
					kCar.setTrain(train);
					kCar.setRouteLocation(rl);
					kCar.setRouteDestination(rld);
					kCar.setDestination(destination, track);
				}
				// not part of kernel, add one car	
			} else {
				addLine(buildReport, THREE, "Car ("+car.getRoad()+" "+car.getNumber()+") assigned destination ("+destination.getName()+", "+track.getName()+")");
				car.setTrain(train);
				car.setRouteLocation(rl);
				car.setRouteDestination(rld);
				car.setDestination(destination, track);
			}
			numberCars++;		// bump number of cars moved by this train
			moves++;			// bump number of car pickup moves for the location
			reqNumOfMoves--; 	// decrement number of moves left for the location
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
				RouteLocation rlt = train.getRoute().getLocationById(routeList.get(i));
				if (rl == rlt){
					carInTrain = true;
				}
				if (rld == rlt){
					carInTrain = false;
				}
				if (carInTrain){
					// car could be part of a kernel
					int length = Integer.parseInt(car.getLength())+ Car.COUPLER;
					try {
						weightTons = weightTons + Integer.parseInt(car.getWeightTons());
					} catch (Exception e){
						log.debug ("Car ("+car.getRoad()+" "+car.getNumber()+") weight not set");
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
		} 
		return false;
	}

	private boolean checkPickUpTrainDirection(RollingStock rs, RouteLocation rl){
		// check that car or engine is located on a track
		if (rs.getTrack() == null){
			addLine(buildReport, THREE, "Rolling stock ("+rs.getRoad()+" "+rs.getNumber()+") does not have a track assignment");
			return false;
		}
		if (routeList.size() == 1) // ignore local train direction
			return true;
		if ((rl.getTrainDirection() & rs.getLocation().getTrainDirections() & rs.getTrack().getTrainDirections()) >0)
			return true;

		addLine(buildReport, FIVE, "Can't pick up rolling stock ("+rs.getRoad()+" "+rs.getNumber()+") using "
				+rl.getTrainDirectionString()+"bound train, location");
		addLine(buildReport, FIVE, " ("+rs.getLocation().getName()
				+", "+rs.getTrack().getName()+") does not service this direction");
		return false;
	}
	
	
	/**
	 * @param car
	 * @param rl the planned origin for this car
	 * @param rld the planned destination for this car
	 * @return true if car can be added to train
	 */
	private boolean checkTrainLength(Car car, RouteLocation rl, RouteLocation rld) {
		boolean carInTrain = false;
		for (int i=0; i<routeList.size(); i++){
			RouteLocation rlt = train.getRoute().getLocationById(routeList.get(i));
			if (rl == rlt){
				carInTrain = true;
			}
			if (rld == rlt){
				carInTrain = false;
			}
			// car can be a kernel so get total length
			int length = Integer.parseInt(car.getLength())+ Car.COUPLER;
			if (car.getKernel() != null)
				length = car.getKernel().getLength();
			if (carInTrain && rlt.getTrainLength()+ length > rlt.getMaxTrainLength()){
				addLine(buildReport, FIVE, "Can't pick up car ("+car.getRoad()+" "+car.getNumber()+") length ("+length+") using train,");
				addLine(buildReport, FIVE, " it would exceed train length restrication at "+rlt.getName());
				return false;
			}
		}
		return true;
	}
	
	private boolean ignoreTrainDirectionIfLastLoc = false;
	private boolean checkDropTrainDirection (Car car, RouteLocation rld, Location destination, Track track){
		// local?
		if (routeList.size()==1)
			return true;
		// is the destination the last location on the route? 
		if (ignoreTrainDirectionIfLastLoc && rld == train.getTrainTerminatesRouteLocation())
			return true;	// yes, ignore train direction
		// this location only services trains with these directions
		int serviceTrainDir = (destination.getTrainDirections() & track.getTrainDirections()); 
		if ((rld.getTrainDirection() & serviceTrainDir) >0){
			return true;
		} 
		addLine(buildReport, FIVE, "Can't drop car ("+car.getRoad()+" "+car.getNumber()+") using "+rld.getTrainDirectionString()+"bound train,");
		addLine(buildReport, FIVE, " destination track ("+track+") does not service this direction");
		return false;
	}
	
	/**
	 * Check departure staging track to see if engines and cars are available to
	 * a new train.  Also confirms that the engine and car type and road are accepted by the train.
	 * 
	 * @return true is there are engines and cars available.
	 */
	private boolean checkDepartureStagingTrack(){
		if (departStageTrack.getNumberRS()==0)
			return false;
		if (departStageTrack.getNumberEngines()>0){
			List<String> engs = engineManager.getByIdList();
			for (int i=0; i<engs.size(); i++){
				Engine eng = engineManager.getById(engs.get(i));
				if (eng.getTrack() == departStageTrack){
					// has engine been assigned to another train?
					if (eng.getRouteLocation() != null){
						addLine(buildReport, ONE, MessageFormat.format(rb.getString("buildStagingDepart"),
								new Object[]{departStageTrack.getName(), eng.getTrainName()}));
						return false;
					}
					// does the train accept the engine type from the staging track?
					if (!train.acceptsTypeName(eng.getType())){
						addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildStagingDepartEngineType"),
								new Object[]{departStageTrack.getName(), eng.getRoad()+" "+eng.getNumber(), eng.getType(), train.getName()}));
						return false;
					}
					// does the train accept the engine road from the staging track?
					if (!train.acceptsRoadName(eng.getRoad())){
						addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildStagingDepartEngineRoad"),
								new Object[]{departStageTrack.getName(), eng.getRoad()+" "+eng.getNumber(), eng.getRoad(), train.getName()}));
						return false;				
					}
					// does the train accept the engine owner from the staging track?
					if (!train.acceptsOwnerName(eng.getOwner())){
						addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildStagingDepartEngineOwner"),
								new Object[]{departStageTrack.getName(), eng.getRoad()+" "+eng.getNumber(), eng.getOwner(), train.getName()}));
						return false;				
					}
					// does the train accept the engine built date from the staging track?
					if (!train.acceptsBuiltDate(eng.getBuilt())){
						addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildStagingDepartEngineBuilt"),
								new Object[]{departStageTrack.getName(), eng.getRoad()+" "+eng.getNumber(), eng.getBuilt(), train.getName()}));
						return false;				
					}
				}
			}
		}
		if (departStageTrack.getNumberCars()>0){
			List<String> cars = carManager.getByIdList();
			for (int i=0; i<cars.size(); i++){
				Car car = carManager.getById(cars.get(i));
				if (car.getTrack() == departStageTrack){
					// has car been assigned to another train?
					if (car.getRouteLocation() != null){
						addLine(buildReport, ONE, MessageFormat.format(rb.getString("buildStagingDepart"),
								new Object[]{departStageTrack.getName(), car.getTrainName()}));
						return false;
					}
					// does the train accept the car type from the staging track?
					if (!train.acceptsTypeName(car.getType())){
						addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildStagingDepartCarType"),
								new Object[]{departStageTrack.getName(), car.getRoad()+" "+car.getNumber(), car.getType(), train.getName()}));
						return false;
					}
					// does the train accept the car road from the staging track?
					if (!train.acceptsRoadName(car.getRoad())){
						addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildStagingDepartCarRoad"),
								new Object[]{departStageTrack.getName(), car.getRoad()+" "+car.getNumber(), car.getRoad(), train.getName()}));
						return false;
					}
					// does the train accept the car owner from the staging track?
					if (!train.acceptsOwnerName(car.getOwner())){
						addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildStagingDepartCarOwner"),
								new Object[]{departStageTrack.getName(), car.getRoad()+" "+car.getNumber(), car.getOwner(), train.getName()}));
						return false;
					}
					// does the train accept the car built date from the staging track?
					if (!train.acceptsBuiltDate(car.getBuilt())){
						addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildStagingDepartCarBuilt"),
								new Object[]{departStageTrack.getName(), car.getRoad()+" "+car.getNumber(), car.getBuilt(), train.getName()}));
						return false;
					}
				}
			}
		}
		return true;
	}

	/**
	 * Checks to see if staging track can accept train.
	 * @return true if staging track is empty, not reserved, and accepts
	 * car and engine types and roads.
	 */
	private boolean checkTerminateStagingTrack(){
		if (terminateStageTrack.getNumberRS() != 0 || terminateStageTrack.getDropRS() != 0){
			addLine(buildReport, FIVE, "Staging track ("+terminateStageTrack.getName()+") is not available");
			return false;
		}
		// check go see if location/track will accept the train's car and engine types
		String[] types = train.getTypeNames();
		for (int i=0; i<types.length; i++){
			if (!terminateLocation.acceptsTypeName(types[i])){
				addLine(buildReport, FIVE, "Location ("+terminateLocation.getName()+") does not accept type ("+types[i]+")");
				return false;			
			}
			if (!terminateStageTrack.acceptsTypeName(types[i])){
				addLine(buildReport, FIVE, "Staging track ("+terminateStageTrack.getName()+") does not accept type ("+types[i]+")");
				return false;			
			}
		}
		// check go see if track will accept the train's car and engine roads
		if (train.getRoadOption().equals(Train.INCLUDEROADS)){
			String[] roads = train.getRoadNames();
			for (int i=0; i<roads.length; i++){
				if (!terminateStageTrack.acceptsTypeName(roads[i]))
					addLine(buildReport, FIVE, "Staging track ("+terminateStageTrack.getName()+") does not accept raod ("+roads[i]+")");
				return false;
			}
		}
		return true;	
	}

	private void buildFailed(String string){
		train.setStatus(BUILDFAILED);
		train.setBuildFailed(true);
		if(log.isDebugEnabled())
			log.debug(string);
		if(TrainManager.instance().getBuildMessages()){
			JOptionPane.showMessageDialog(null, string,
					MessageFormat.format(rb.getString("buildErrorMsg"),new Object[]{train.getName(), train.getDescription()}),
					JOptionPane.ERROR_MESSAGE);
		}
		if (buildReport != null){
			addLine(buildReport, ONE, string);
			// Write to disk and close buildReport
			addLine(buildReport, ONE, MessageFormat.format(rb.getString("buildFailedMsg"),new Object[]{train.getName()}));
			buildReport.flush();
			buildReport.close();
			if(TrainManager.instance().getBuildReport()){
				File buildFile = TrainManagerXml.instance().getTrainBuildReportFile(train.getName());
				printBuildReport(buildFile, MessageFormat.format(rb.getString("buildFailureReport"),new Object[]{train.getDescription()}), true);
			}
		}
	}
	
	private static void printBuildReport(File file, String name, boolean isPreview){
		Train.printReport(file, name, isPreview, "", true);
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
		addLine(fileOut, MessageFormat.format(rb.getString("Valid"), new Object[]{new Date()}));
		if (!train.getComment().equals("")){
			addLine(fileOut, train.getComment());
		}
		
		List<String> engineList = engineManager.getByTrainList(train);
		Engine engine = null;
		String comment = "";
		for (int i =0; i < engineList.size(); i++){
			engine = engineManager.getById(engineList.get(i));
			comment = (Setup.isAppendCarCommentEnabled() ? " "+engine.getComment() : "");
			addLine(fileOut, BOX + rb.getString("Engine")+" "+ engine.getRoad() + " " + engine.getNumber() + " (" +engine.getModel()+  ") "
					+rb.getString("assignedToThisTrain") + comment);
		}
		
		if (engine != null){
			String pickupText = rb.getString("PickupEngineAt");
			if (engineList.size() > 1)
				pickupText= rb.getString("PickupEnginesAt");
			addLine(fileOut, pickupText+" "+splitString(engine.getLocationName())+", "+engine.getTrackName());
		}
		
		List<String> carList = carManager.getByTrainDestinationList(train);
		log.debug("Train has " + carList.size() + " cars assigned to it");
		int cars = 0;
		List<String> routeList = train.getRoute().getLocationsBySequenceList();
		for (int r = 0; r < routeList.size(); r++) {
			RouteLocation rl = train.getRoute().getLocationById(routeList.get(r));
			newLine(fileOut);
			String routeLocationName = splitString(rl.getName());
			if (r == 0)
				addLine(fileOut, rb.getString("ScheduledWorkIn")+" " + routeLocationName 
						+", "+rb.getString("departureTime")+" "+train.getDepartureTime());
			else
				addLine(fileOut, rb.getString("ScheduledWorkIn")+" " + routeLocationName 
						+", "+rb.getString("estimatedArrival")+" "+train.getExpectedArrivalTime(rl));
			// block cars by destination
			for (int j = r; j < routeList.size(); j++) {
				RouteLocation rld = train.getRoute().getLocationById(routeList.get(j));
				for (int k = 0; k < carList.size(); k++) {
					Car car = carManager.getById(carList.get(k));
					if (car.getRouteLocation() == rl
							&& car.getRouteDestination() == rld) {
						pickupCar(fileOut, car);
						cars++;
					}
				}
			}
			for (int j = 0; j < carList.size(); j++) {
				Car car = carManager.getById(carList.get(j));
				if (car.getRouteDestination() == rl) {
					dropCar(fileOut, car);
					cars--;
				}
			}
			if (r != routeList.size() - 1) {
				addLine(fileOut, rb.getString("TrainDeparts")+ " " + routeLocationName +" "+ rl.getTrainDirectionString()
						+ rb.getString("boundWith") +" " + cars + " " +rb.getString("cars")+", " +rl.getTrainLength()
						+" "+rb.getString("feet")+", "+rl.getTrainWeight()+" "+rb.getString("tons"));
			} else {
				if(engine != null){
					String dropText = rb.getString("DropEngineTo");
					if (engineList.size() > 1)
						dropText = rb.getString("DropEnginesTo");
					addLine(fileOut, BOX +dropText+ " "+ splitString(engine.getDestinationTrackName())); 
				}
				addLine(fileOut, rb.getString("TrainTerminatesIn")+ " " + routeLocationName);
			}
		}
		fileOut.flush();
		fileOut.close();
	}
	
	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(TrainBuilder.class.getName());

}
