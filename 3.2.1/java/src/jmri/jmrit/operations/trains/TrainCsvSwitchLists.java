// TrainCsvSwitchLists.java

package jmri.jmrit.operations.trains;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.ResourceBundle;

import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Setup;

/**
 * Builds a comma separated value (csv) switch list for a location on the railroad
 * @author Daniel Boudreau (C) Copyright 2011
 *
 */
public class TrainCsvSwitchLists extends TrainCsvCommon {
	
	static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.trains.JmritOperationsTrainsBundle");
	
	TrainManager trainManager = TrainManager.instance();
	
	// builds a switch list for a location
	public void buildSwitchList(Location location){
		boolean newTrainsOnly = !Setup.isSwitchListRealTime();
		// create csv switch list file
		File file = TrainManagerXml.instance().createCsvSwitchListFile(
				location.getName());
		PrintWriter fileOut;

		try {
			fileOut = new PrintWriter(new BufferedWriter(new FileWriter(file)), true);
		} catch (IOException e) {
			log.error("can not open cvs switchlist file");
			return;
		}
		// build header
		addLine(fileOut, HEADER);
		addLine(fileOut, RN+"\""+Setup.getRailroadName()+"\"");

		addLine(fileOut, LN+"\""+splitString(location.getName())+"\"");
		addLine(fileOut, VT+getDate());
		
		// get a list of trains
		List<String> trains = trainManager.getTrainsByTimeList();
		CarManager carManager = CarManager.instance();
		EngineManager engineManager = EngineManager.instance();
		for (int i=0; i<trains.size(); i++){
			int pickupCars = 0;
			int dropCars = 0;
			int stops = 1;
			boolean trainDone = false;
			Train train = trainManager.getTrainById(trains.get(i));
			if (!train.isBuilt())
				continue;	// train wasn't built so skip
			if (newTrainsOnly && train.getSwitchListStatus().equals(Train.PRINTED))
				continue;	// already printed this train
			List<String> carList = carManager.getByTrainDestinationList(train);
			List<String> enginesList = engineManager.getByTrainList(train);
			// does the train stop once or more at this location?
			Route route = train.getRoute();
			if (route == null)
				continue;	// no route for this train
			List<String> routeList = route.getLocationsBySequenceList();
			for (int r=0; r<routeList.size(); r++){
				RouteLocation rl = route.getLocationById(routeList.get(r));
				if (splitString(rl.getName()).equals(splitString(location.getName()))){
					String expectedArrivalTime = train.getExpectedArrivalTime(rl);
					if (expectedArrivalTime.equals("-1")){
						trainDone = true;
					}
					// First time a train stops at a location provide:
					// train name
					// train description
					// if the train has started its route
					// the arrival time or relative time if the train has started its route
					// the departure location
					// the departure time
					// the train's direction when it arrives
					// if it terminate at this location
					if (stops == 1){
						newLine(fileOut);
						addLine(fileOut, TN+train.getName());
						addLine(fileOut, TM+train.getDescription());

						if (train.isTrainInRoute()){
							addLine(fileOut, TIR);
							addLine(fileOut, ETE+expectedArrivalTime);
						} else {						
							addLine(fileOut, DL+splitString(splitString(train.getTrainDepartsName())));
							addLine(fileOut, DT+train.getDepartureTime());
							if (r == 0 && routeList.size()>1)
								addLine(fileOut, TD+splitString(rl.getName())+del+rl.getTrainDirectionString());
							if (r != 0){
								addLine(fileOut, ETA+expectedArrivalTime);
								addLine(fileOut, TA+splitString(rl.getName())+del+rl.getTrainDirectionString());
							}
						}
						if (r == routeList.size()-1)
							addLine(fileOut, TT+splitString(rl.getName()));
					}
					if (stops > 1) {
						// Print visit number, etc. only if previous location wasn't the same
						RouteLocation rlPrevious = route.getLocationById(routeList.get(r-1));
						if (!splitString(rl.getName()).equals(splitString(rlPrevious.getName()))){
							// After the first time a train stops at a location provide:
							// if the train has started its route
							// the arrival time or relative time if the train has started its route
							// the train's direction when it arrives
							// if it terminate at this location

							addLine(fileOut, VN+stops);
							if (train.isTrainInRoute()){
								addLine(fileOut, ETE+expectedArrivalTime);
							} else {						
								addLine(fileOut, ETA+expectedArrivalTime);
							}
							addLine(fileOut, TA+splitString(rl.getName())+del+rl.getTrainDirectionString());
							if (r == routeList.size()-1)
								addLine(fileOut, TT+splitString(rl.getName()));
						} else {
							stops--;	// don't bump stop count, same location
							// Does the train change direction?
							if (rl.getTrainDirection() != rlPrevious.getTrainDirection())
								addLine(fileOut, TDC+rl.getTrainDirectionString());
						}	
					}
					// go through the list of engines and determine if the engine departs here
					for (int j =0; j < enginesList.size(); j++){
						Engine engine = engineManager.getById(enginesList.get(j));
						if (engine.getRouteLocation() == rl && !engine.getTrackName().equals(""))
							fileOutCsvEngine(fileOut, engine, PL);
					}

					// get a list of cars and determine if this location is serviced
					// block cars by destination
					for (int j = 0; j < routeList.size(); j++) {
						RouteLocation rld = train.getRoute().getLocationById(routeList.get(j));
						for (int k = 0; k < carList.size(); k++) {
							Car car = carManager.getById(carList.get(k));
							if (car.getRouteLocation() == rl && !car.getTrackName().equals("")
									&& car.getRouteDestination() == rld) {
								fileOutCsvCar(fileOut, car, PC);
								pickupCars++;
							}
						}
					}
					
					for (int j =0; j < enginesList.size(); j++){
						Engine engine = engineManager.getById(enginesList.get(j));
						if (engine.getRouteDestination() == rl)
							fileOutCsvEngine(fileOut, engine, SL);
					}	
					
					for (int j=0; j<carList.size(); j++){
						Car car = carManager.getById(carList.get(j));
						if (car.getRouteDestination() == rl){
							fileOutCsvCar(fileOut, car, SC);
							dropCars++;
						}
					}
					stops++;
				}
			}
			if (trainDone && pickupCars == 0 && dropCars == 0){
				addLine(fileOut, TDONE);
			} else {
				if (stops > 1 && pickupCars == 0){
					addLine(fileOut, NCPU);
				}

				if (stops > 1 && dropCars == 0){
					addLine(fileOut, NCSO);
				}
			}
		}
		// TODO Are there any cars that need to be found?
		//getCarsLocationUnknown(fileOut);
		fileOut.flush();
		fileOut.close();
	}
	
	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(TrainCsvSwitchLists.class.getName());
}
