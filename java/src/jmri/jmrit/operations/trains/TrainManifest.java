// TrainCsvManifest.java

package jmri.jmrit.operations.trains;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.List;

import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarLoad;
import jmri.jmrit.operations.rollingstock.cars.CarLoads;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Setup;

/**
 * Builds a train's manifest using Comma Separated Values (csv). 
 * 
 * @author Daniel Boudreau  Copyright (C) 2011
 * @version             $Revision: 1 $
 */
public class TrainManifest extends TrainCommon {
	
	EngineManager engineManager = EngineManager.instance();
	CarManager carManager = CarManager.instance();
	LocationManager locationManager = LocationManager.instance();
	
	public TrainManifest(Train train){
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
		if (!train.getRailroadName().equals(""))
			addLine(fileOut, train.getRailroadName());
		else
			addLine(fileOut, Setup.getRailroadName());
		newLine(fileOut);
		addLine(fileOut, rb.getString("ManifestForTrain")+" (" + train.getName() + ") "+ train.getDescription());
		
		String valid = MessageFormat.format(rb.getString("Valid"), new Object[]{getDate()});
		
		if (Setup.isPrintTimetableNameEnabled()){
			TrainSchedule sch = TrainScheduleManager.instance().getScheduleById(TrainManager.instance().getTrainScheduleActiveId());
			if (sch != null)
				valid = valid + " ("+sch.getName()+")"; 
		}
		
		addLine(fileOut, valid);
		if (!train.getComment().equals("")){
			addLine(fileOut, train.getComment());
		}
		
		List<String> engineList = engineManager.getByTrainList(train);		
		pickupEngines(fileOut, engineList, train.getTrainDepartsRouteLocation());
		
		if (Setup.isPrintRouteCommentsEnabled() && !train.getRoute().getComment().equals(""))
			addLine(fileOut, train.getRoute().getComment());
		
		List<String> carList = carManager.getByTrainDestinationList(train);
		log.debug("Train has " + carList.size() + " cars assigned to it");
		int cars = 0;
		int emptyCars = 0;
		boolean work = false;
		boolean newWork = false;
		String previousRouteLocationName = null;
		List<String> routeList = train.getRoute().getLocationsBySequenceList();
		for (int r = 0; r < routeList.size(); r++) {
			RouteLocation rl = train.getRoute().getLocationById(routeList.get(r));
			// add line break between locations without work and ones with work
			boolean oldWork = work;
			work = isThereWorkAtLocation(carList, rl);
			if (oldWork == false && work == true)
				newLine(fileOut);		
			
			// print info only if new location
			String routeLocationName = splitString(rl.getName());
			if (!routeLocationName.equals(previousRouteLocationName) ||
					(routeLocationName.equals(previousRouteLocationName) && oldWork == false && work == true && newWork == false)){
				if (work){
					newWork = true;
					if (r == 0){
						addLine(fileOut, rb.getString("ScheduledWorkIn")+" " + routeLocationName 
								+", "+rb.getString("departureTime")+" "+train.getFormatedDepartureTime());
					} else if (!rl.getDepartureTime().equals("")){
						addLine(fileOut, rb.getString("ScheduledWorkIn")+" " + routeLocationName 
								+", "+rb.getString("departureTime")+" "+rl.getFormatedDepartureTime());
					} else {
						addLine(fileOut, rb.getString("ScheduledWorkIn")+" " + routeLocationName 
								+", "+rb.getString("estimatedArrival")+" "+train.getExpectedArrivalTime(rl));
					}
				} else {
					if (r == 0){
						addLine(fileOut, MessageFormat.format(rb.getString("NoScheduledWorkAt"), new Object[]{routeLocationName})
								+", "+rb.getString("departureTime")+" "+train.getDepartureTime());
					} else if (!rl.getDepartureTime().equals("")){
						addLine(fileOut, MessageFormat.format(rb.getString("NoScheduledWorkAt"), new Object[]{routeLocationName}) 
								+", "+rb.getString("departureTime")+" "+rl.getFormatedDepartureTime());
					} else {
						addLine(fileOut, MessageFormat.format(rb.getString("NoScheduledWorkAt"), new Object[]{routeLocationName}));
					}
				}
				// add location comment
				if (Setup.isPrintLocationCommentsEnabled()){
					Location l = locationManager.getLocationByName(rl.getName());
					if (!l.getComment().equals(""))
						addLine(fileOut, l.getComment());				
				}
			}
			// add route comment
			if (!rl.getComment().equals(""))
				addLine(fileOut, rl.getComment());
			
			// engine change or helper service?
			if (train.getSecondLegOptions() != Train.NONE){
				if (rl == train.getSecondLegStartLocation()){
					engineChange(fileOut, rl, train.getSecondLegOptions());
					dropEngines(fileOut, engineList, rl);
					pickupEngines(fileOut, engineList, rl);
				}
				if (rl == train.getSecondLegEndLocation())
					addLine(fileOut, MessageFormat.format(rb.getString("RemoveHelpersAt"), new Object[]{rl.getName()}));
			}
			if (train.getThirdLegOptions() != Train.NONE){
				if (rl == train.getThirdLegStartLocation()){
					engineChange(fileOut, rl, train.getThirdLegOptions());
					dropEngines(fileOut, engineList, rl);
					pickupEngines(fileOut, engineList, rl);
				}
				if (rl == train.getThirdLegEndLocation())
					addLine(fileOut, MessageFormat.format(rb.getString("RemoveHelpersAt"), new Object[]{rl.getName()}));
			}

			// block cars by destination
			for (int j = r; j < routeList.size(); j++) {
				RouteLocation rld = train.getRoute().getLocationById(routeList.get(j));
				for (int k = 0; k < carList.size(); k++) {
					Car car = carManager.getById(carList.get(k));
					if (car.getRouteLocation() == rl
							&& car.getRouteDestination() == rld) {
						pickupCar(fileOut, car);
						cars++;
						newWork = true;
						if (CarLoads.instance().getLoadType(car.getType(), car.getLoad()).equals(CarLoad.LOAD_TYPE_EMPTY))
							emptyCars++;
					}
				}
			}
			for (int j = 0; j < carList.size(); j++) {
				Car car = carManager.getById(carList.get(j));
				if (car.getRouteDestination() == rl) {
					dropCar(fileOut, car);
					cars--;
					newWork = true;
					if (CarLoads.instance().getLoadType(car.getType(), car.getLoad()).equals(CarLoad.LOAD_TYPE_EMPTY))
						emptyCars--;
				}
			}
			if (r != routeList.size() - 1) {
				// Is the next location the same as the previous?
				RouteLocation rlNext = train.getRoute().getLocationById(routeList.get(r+1));
				String nextRouteLocationName = splitString(rlNext.getName());
				if (!routeLocationName.equals(nextRouteLocationName)){
					if (newWork){
						if (Setup.isPrintLoadsAndEmptiesEnabled()){
							addLine(fileOut, rb.getString("TrainDeparts")+ " " + routeLocationName +" "+ rl.getTrainDirectionString()
									+ rb.getString("boundWith") +" " + (cars-emptyCars) + " " +rb.getString("Loads")
									+", " + emptyCars + " " + rb.getString("Empties")+ ", " +rl.getTrainLength()
									+" "+rb.getString("feet")+", "+rl.getTrainWeight()+" "+rb.getString("tons"));
						} else {
							addLine(fileOut, rb.getString("TrainDeparts")+ " " + routeLocationName +" "+ rl.getTrainDirectionString()
									+ rb.getString("boundWith") +" " + cars + " " +rb.getString("cars")+", " +rl.getTrainLength()
									+" "+rb.getString("feet")+", "+rl.getTrainWeight()+" "+rb.getString("tons"));
						}
						newWork = false;
						newLine(fileOut);
					}
				}
			} else {
				dropEngines(fileOut, engineList, rl);
				addLine(fileOut, rb.getString("TrainTerminatesIn")+ " " + routeLocationName);
			}
			previousRouteLocationName = routeLocationName;
		}
		// Are there any cars that need to be found?
		getCarsLocationUnknown(fileOut);
		
		fileOut.flush();
		fileOut.close();	
	}
	
	// returns true if there's work at location
	private boolean isThereWorkAtLocation(List<String> carList, RouteLocation rl){
		for (int i = 0; i < carList.size(); i++) {
			Car car = carManager.getById(carList.get(i));
			if (car.getRouteLocation() == rl || car.getRouteDestination() == rl)
				return true;
		}
		return false;
	}
	
	private void engineChange(PrintWriter fileOut, RouteLocation rl, int legOptions){
		if ((legOptions & Train.HELPER_ENGINES) == Train.HELPER_ENGINES)
			addLine(fileOut, MessageFormat.format(rb.getString("AddHelpersAt"), new Object[]{rl.getName()}));
		else if ((legOptions & Train.CHANGE_CABOOSE) == Train.CHANGE_CABOOSE)
			addLine(fileOut, MessageFormat.format(rb.getString("EngineAndCabooseChangeAt"), new Object[]{rl.getName()}));
		else if ((legOptions & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES)
			addLine(fileOut, MessageFormat.format(rb.getString("EngineChangeAt"), new Object[]{rl.getName()}));
	}
}

