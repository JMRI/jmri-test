// TrainCsvManifest.java

package jmri.jmrit.operations.trains;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;

import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarLoad;
import jmri.jmrit.operations.rollingstock.cars.CarLoads;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.FileUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds a train's manifest using Comma Separated Values (csv).
 * 
 * @author Daniel Boudreau Copyright (C) 2011
 * @version $Revision: 1 $
 */
public class TrainCsvManifest extends TrainCsvCommon {

	EngineManager engineManager = EngineManager.instance();
	CarManager carManager = CarManager.instance();
	LocationManager locationManager = LocationManager.instance();

        private final static Logger log = LoggerFactory.getLogger(TrainCsvManifest.class);

	public TrainCsvManifest(Train train) {
		// create comma separated value manifest file
		File file = TrainManagerXml.instance().createTrainCsvManifestFile(train.getName());

		PrintWriter fileOut;

		try {
			fileOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8")),// NOI18N
					true); // NOI18N
		} catch (IOException e) {
			log.error("Can not open CSV manifest file: "+file.getName());
			return;
		}
		// build header
		addLine(fileOut, HEADER);
		addLine(fileOut, RN + ESC + Setup.getRailroadName() + ESC);
		addLine(fileOut, TN + train.getName());
		addLine(fileOut, TM + ESC + train.getDescription() + ESC);
		addLine(fileOut, PRNTR + ESC
				+ locationManager.getLocationByName(train.getTrainDepartsName()).getDefaultPrinterName() + ESC);
		// add logo
		String logoURL = FileUtil.getExternalFilename(Setup.getManifestLogoURL());
		if (!train.getManifestLogoURL().equals(""))
			logoURL = FileUtil.getExternalFilename(train.getManifestLogoURL());
		if (!logoURL.equals(""))
			addLine(fileOut, LOGO + logoURL);
		addLine(fileOut, VT + getDate(true));
		// train comment can have multiple lines
		if (!train.getComment().equals("")) {
			String[] comments = train.getComment().split(NEW_LINE); // NOI18N
			for (String comment : comments)
				addLine(fileOut, TC + ESC + comment + ESC);
		}
		if (Setup.isPrintRouteCommentsEnabled())
			addLine(fileOut, RC + ESC + train.getRoute().getComment() + ESC);

		// get engine and car lists
		List<Engine> engineList = engineManager.getByTrainBlockingList(train);
		List<Car> carList = carManager.getByTrainDestinationList(train);

		int cars = 0;
		int emptyCars = 0;
		boolean newWork = false;
		String previousRouteLocationName = null;
		List<RouteLocation> routeList = train.getRoute().getLocationsBySequenceList();
		for (int r = 0; r < routeList.size(); r++) {
			RouteLocation rl = routeList.get(r);
			// print info only if new location
			String routeLocationName = splitString(rl.getName());
			String locationName = routeLocationName;
			if (locationName.contains(DEL)) {
				log.debug("location name has delimiter: " + locationName);
				locationName = ESC + routeLocationName + ESC;
			}
			if (!routeLocationName.equals(previousRouteLocationName)) {
				addLine(fileOut, LN + locationName);
				if (r != 0)
					addLine(fileOut, AT + train.getExpectedArrivalTime(rl));
				if (r == 0)
					addLine(fileOut, DT + train.getDepartureTime());
				else if (!rl.getDepartureTime().equals(""))
					addLine(fileOut, DTR + rl.getDepartureTime());
				else
					addLine(fileOut, EDT + train.getExpectedDepartureTime(rl));

				Location loc = locationManager.getLocationByName(rl.getName());
				// add location comment
				if (Setup.isPrintLocationCommentsEnabled() && !loc.getComment().equals("")) {
					// location comment can have multiple lines
					String[] comments = loc.getComment().split(NEW_LINE); // NOI18N
					for (String comment : comments)
						addLine(fileOut, LC + ESC + comment + ESC);
				}
				if (Setup.isTruncateManifestEnabled() && loc.isSwitchListEnabled())
					addLine(fileOut, TRUN);
			}
			// add route comment
			if (!rl.getComment().equals("")) {
				addLine(fileOut, RLC + ESC + rl.getComment() + ESC);
			}
			
			printTrackComments(fileOut, rl, carList);
			
			// engine change or helper service?
			if (train.getSecondLegOptions() != Train.NO_CABOOSE_OR_FRED) {
				if (rl == train.getSecondLegStartLocation()) {
					engineCsvChange(fileOut, rl, train.getSecondLegOptions());
				}
				if (rl == train.getSecondLegEndLocation())
					addLine(fileOut, RH);
			}
			if (train.getThirdLegOptions() != Train.NO_CABOOSE_OR_FRED) {
				if (rl == train.getThirdLegStartLocation()) {
					engineCsvChange(fileOut, rl, train.getThirdLegOptions());
				}
				if (rl == train.getThirdLegEndLocation())
					addLine(fileOut, RH);
			}

			for (Engine engine : engineList) {
				if (engine.getRouteLocation() == rl)
					fileOutCsvEngine(fileOut, engine, PL);
			}
			for (Engine engine : engineList) {
				if (engine.getRouteDestination() == rl)
					fileOutCsvEngine(fileOut, engine, SL);
			}

			// block cars by destination
			for (int j = r; j < routeList.size(); j++) {
				RouteLocation rld = routeList.get(j);
				for (Car car : carList) {
					if (car.getRouteLocation() == rl && car.getRouteDestination() == rld) {
						cars++;
						newWork = true;
						if (car.getLoadType().equals(CarLoad.LOAD_TYPE_EMPTY))
							emptyCars++;
						int count = 0;
						if (car.isUtility()) {
							count = countPickupUtilityCars(carList, car, rl, rld, true);
							if (count == 0)
								continue; // already done this set of utility cars
						}
						fileOutCsvCar(fileOut, car, PC, count);
					}
				}
			}
			// car set outs
			for (Car car : carList) {
				if (car.getRouteDestination() == rl) {
					cars--;
					newWork = true;
					if (CarLoads.instance().getLoadType(car.getTypeName(), car.getLoadName()).equals(
							CarLoad.LOAD_TYPE_EMPTY))
						emptyCars--;
					int count = 0;
					if (car.isUtility()) {
						count = countSetoutUtilityCars(carList, car, rl, false, true);
						if (count == 0)
							continue; // already done this set of utility cars
					}
					fileOutCsvCar(fileOut, car, SC, count);
				}
			}
			if (r != routeList.size() - 1) {
				// Is the next location the same as the previous?
				RouteLocation rlNext = routeList.get(r + 1);
				String nextRouteLocationName = splitString(rlNext.getName());
				if (!routeLocationName.equals(nextRouteLocationName)) {
					if (newWork) {
						addLine(fileOut, TD + locationName + DEL + rl.getTrainDirectionString());
						addLine(fileOut, TL + train.getTrainLength(rl) + DEL + emptyCars + DEL + cars);
						addLine(fileOut, TW + train.getTrainWeight(rl));
						newWork = false;
					} else {
						addLine(fileOut, NW);
					}
				}
			} else {
				addLine(fileOut, TT + locationName);
			}
			previousRouteLocationName = routeLocationName;
		}
		// TODO Are there any cars that need to be found?
		// getCarsLocationUnknown(fileOut);

		fileOut.flush();
		fileOut.close();
	}
}
