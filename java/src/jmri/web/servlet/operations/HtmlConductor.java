package jmri.web.servlet.operations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarLoad;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainCommon;
import jmri.util.FileUtil;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author rhwood
 */
public class HtmlConductor extends HtmlTrainCommon {

    private final static Logger log = LoggerFactory.getLogger(HtmlConductor.class);

    public HtmlConductor(Locale locale, Train train) throws IOException {
        super(locale, train);
        this.resourcePrefix = "Conductor";
    }

    public String getLocation() throws IOException {
        RouteLocation location = train.getCurrentLocation();
        if (location == null) {
            return String.format(locale, FileUtil.readURL(FileUtil.findURL(Bundle.getMessage(locale,
                    "ConductorSnippet.html"))), train.getIconName(), StringEscapeUtils.escapeHtml4(train
                            .getDescription()), StringEscapeUtils.escapeHtml4(train.getComment()), Setup
                    .isPrintRouteCommentsEnabled() ? train.getRoute().getComment() : "", strings
                    .getProperty("Terminated"), "", // terminated train has nothing to do
                    "", // engines in separate section
                    "", // pickup=true, local=false
                    "", // pickup=false, local=false
                    "", // pickup=false, local=true
                    "", // engines in separate section
                    "", // terminate with null string, use empty string to indicate terminated
                    strings.getProperty("Terminated"));
        }

        List<Engine> engineList = EngineManager.instance().getByTrainBlockingList(train);
        List<Car> carList = CarManager.instance().getByTrainDestinationList(train);
        log.debug("Train has {} cars assigned to it", carList.size());

        String pickups = performWork(true, false); // pickup=true, local=false
        String setouts = performWork(false, false); // pickup=false, local=false
        String localMoves = performWork(false, true); // pickup=false, local=true

        return String.format(locale, FileUtil.readURL(FileUtil.findURL(Bundle.getMessage(locale,
                "ConductorSnippet.html"))), train.getIconName(), StringEscapeUtils.escapeHtml4(train.getDescription()),
                StringEscapeUtils.escapeHtml4(train.getComment()), Setup.isPrintRouteCommentsEnabled() ? train
                        .getRoute().getComment() : "", getCurrentAndNextLocation(),
                getLocationComments(),
                pickupEngines(engineList, location), // engines in separate section
                pickups, setouts, localMoves,
                dropEngines(engineList, location), // engines in separate section
                (train.getNextLocation(train.getCurrentLocation()) != null) ? train.getNextLocationName() : null,
                getMoveButton());
    }

    private String getCurrentAndNextLocation() {
        if (train.getCurrentLocation() != null && train.getNextLocation(train.getCurrentLocation()) != null) {
            return String.format(locale, strings.getProperty("CurrentAndNextLocation"), StringEscapeUtils
                    .escapeHtml4(train.getCurrentLocationName()), StringEscapeUtils.escapeHtml4(train
                            .getNextLocationName()));
        } else if (train.getCurrentLocation() != null) {
            return StringEscapeUtils.escapeHtml4(train.getCurrentLocationName());
        }
        return strings.getProperty("Terminated");
    }

    private String getMoveButton() {
        if (train.getNextLocation(train.getCurrentLocation()) != null) {
            return String.format(locale, strings.getProperty("MoveTo"), StringEscapeUtils.escapeHtml4(train
                    .getNextLocationName()));
        } else if (train.getCurrentLocation() != null) {
            return strings.getProperty("Terminate");
        }
        return strings.getProperty("Terminated");
    }

    // needed for location comments, not yet in formatter
    private String getEngineChanges(RouteLocation location) {
        // engine change or helper service?
        if (train.getSecondLegOptions() != Train.NO_CABOOSE_OR_FRED) {
            if (location == train.getSecondLegStartLocation()) {
                return engineChange(location, train.getSecondLegOptions());
            }
            if (location == train.getSecondLegEndLocation() && train.getSecondLegOptions() == Train.HELPER_ENGINES) {
                return String.format(strings.getProperty("RemoveHelpersAt"), splitString(location.getName())); // NOI18N
            }
        }
        if (train.getThirdLegOptions() != Train.NO_CABOOSE_OR_FRED) {
            if (location == train.getThirdLegStartLocation()) {
                return engineChange(location, train.getSecondLegOptions());
            }
            if (location == train.getThirdLegEndLocation() && train.getThirdLegOptions() == Train.HELPER_ENGINES) {
                return String.format(strings.getProperty("RemoveHelpersAt"), splitString(location.getName())); // NOI18N
            }
        }
        return "";
    }

    private String getLocationComments() {
        List<Car> carList = CarManager.instance().getByTrainDestinationList(train);
        StringBuilder builder = new StringBuilder();
        RouteLocation routeLocation = train.getCurrentLocation();
        boolean work = isThereWorkAtLocation(train, routeLocation.getLocation());

        // print info only if new location
        String routeLocationName = StringEscapeUtils.escapeHtml4(splitString(routeLocation.getName()));
        if (work) {
            if (!train.isShowArrivalAndDepartureTimesEnabled()) {
                builder.append(String.format(locale, strings.getProperty("ScheduledWorkAt"), routeLocationName)); // NOI18N
            } else if (routeLocation == train.getRoute().getDepartsRouteLocation()) {
                builder.append(String.format(locale, strings.getProperty("WorkDepartureTime"), routeLocationName, train
                        .getFormatedDepartureTime())); // NOI18N
            } else if (!routeLocation.getDepartureTime().equals("")) {
                builder.append(String.format(locale, strings.getProperty("WorkDepartureTime"), routeLocationName,
                        routeLocation.getFormatedDepartureTime())); // NOI18N
            } else if (Setup.isUseDepartureTimeEnabled()
                    && routeLocation != train.getRoute().getTerminatesRouteLocation()) {
                builder.append(String.format(locale, strings.getProperty("WorkDepartureTime"), routeLocationName, train
                        .getExpectedDepartureTime(routeLocation))); // NOI18N
            } else if (!train.getExpectedArrivalTime(routeLocation).equals("-1")) { // NOI18N
                builder.append(String.format(locale, strings.getProperty("WorkArrivalTime"), routeLocationName, train
                        .getExpectedArrivalTime(routeLocation))); // NOI18N
            } else {
                builder.append(String.format(locale, strings.getProperty("ScheduledWorkAt"), routeLocationName)); // NOI18N
            }
            // add route comment
            if (!routeLocation.getComment().trim().equals("")) {
                builder.append(String.format(locale, strings.getProperty("RouteLocationComment"), StringEscapeUtils
                        .escapeHtml4(routeLocation.getComment())));
            }

            builder.append(getTrackComments(routeLocation, carList));

            // add location comment
            if (Setup.isPrintLocationCommentsEnabled() && !routeLocation.getLocation().getComment().equals("")) {
                builder.append(String.format(locale, strings.getProperty("LocationComment"), StringEscapeUtils
                        .escapeHtml4(routeLocation.getLocation().getComment())));
            }
        }

        // engine change or helper service?
        builder.append(this.getEngineChanges(routeLocation));

        if (routeLocation != train.getRoute().getTerminatesRouteLocation()) {
            if (work) {
                if (!Setup.isPrintLoadsAndEmptiesEnabled()) {
                    // Message format: Train departs Boston Westbound with 12 cars, 450 feet, 3000 tons
                    builder.append(String.format(strings.getProperty("TrainDepartsCars"), routeLocationName,
                            routeLocation.getTrainDirectionString(), train.getTrainLength(routeLocation), Setup
                            .getLengthUnit().toLowerCase(), train.getTrainWeight(routeLocation), train
                            .getNumberCarsInTrain(routeLocation)));
                } else {
                    // Message format: Train departs Boston Westbound with 4 loads, 8 empties, 450 feet, 3000 tons
                    int emptyCars = train.getNumberEmptyCarsInTrain(routeLocation);
                    builder.append(String.format(strings.getProperty("TrainDepartsLoads"), routeLocationName,
                            routeLocation.getTrainDirectionString(), train.getTrainLength(routeLocation), Setup
                            .getLengthUnit().toLowerCase(), train.getTrainWeight(routeLocation), train
                            .getNumberCarsInTrain(routeLocation)
                            - emptyCars, emptyCars));
                }
            } else {
                if (routeLocation.getComment().trim().isEmpty()) {
                    // no route comment, no work at this location
                    if (train.isShowArrivalAndDepartureTimesEnabled()) {
                        if (routeLocation == train.getRoute().getDepartsRouteLocation()) {
                            builder.append(String.format(locale, strings
                                    .getProperty("NoScheduledWorkAtWithDepartureTime"), routeLocationName, train
                                    .getFormatedDepartureTime()));
                        } else if (!routeLocation.getDepartureTime().isEmpty()) {
                            builder.append(String.format(locale, strings
                                    .getProperty("NoScheduledWorkAtWithDepartureTime"), routeLocationName,
                                    routeLocation.getFormatedDepartureTime()));
                        } else if (Setup.isUseDepartureTimeEnabled()) {
                            builder.append(String.format(locale, strings
                                    .getProperty("NoScheduledWorkAtWithDepartureTime"), routeLocationName, train
                                    .getExpectedDepartureTime(routeLocation)));
                        }
                    } else {
                        builder.append(String.format(locale, strings.getProperty("NoScheduledWorkAt"),
                                routeLocationName));
                    }
                } else {
                    // route comment, so only use location and route comment (for passenger trains)
                    if (train.isShowArrivalAndDepartureTimesEnabled()) {
                        if (routeLocation == train.getRoute().getDepartsRouteLocation()) {
                            builder.append(String.format(locale, strings.getProperty("CommentAtWithDepartureTime"),
                                    routeLocationName, train.getFormatedDepartureTime(), StringEscapeUtils
                                    .escapeHtml4(routeLocation.getComment())));
                        } else if (!routeLocation.getDepartureTime().isEmpty()) {
                            builder.append(String.format(locale, strings.getProperty("CommentAtWithDepartureTime"),
                                    routeLocationName, routeLocation.getFormatedDepartureTime(), StringEscapeUtils
                                    .escapeHtml4(routeLocation.getComment())));
                        }
                    } else {
                        builder.append(String.format(locale, strings.getProperty("CommentAt"), routeLocationName, null,
                                StringEscapeUtils.escapeHtml4(routeLocation.getComment())));
                    }
                }
                // add location comment
                if (Setup.isPrintLocationCommentsEnabled() && !routeLocation.getLocation().getComment().isEmpty()) {
                    builder.append(String.format(locale, strings.getProperty("LocationComment"), StringEscapeUtils
                            .escapeHtml4(routeLocation.getLocation().getComment())));
                }
            }
        } else {
            builder.append(String.format(strings.getProperty("TrainTerminatesIn"), routeLocationName));
        }
        return builder.toString();
    }

    private String performWork(boolean pickup, boolean local) {
        if (pickup) { // pick up
            StringBuilder builder = new StringBuilder();
            RouteLocation location = train.getCurrentLocation();
            List<Car> carList = CarManager.instance().getByTrainDestinationList(train);
            List<Track> tracks = location.getLocation().getTrackByNameList(null);
            List<String> trackNames = new ArrayList<String>();
            List<String> pickedUp = new ArrayList<String>();
            this.clearUtilityCarTypes();
            for (Track track : tracks) {
                if (trackNames.contains(splitString(track.getName()))) {
                    continue;
                }
                trackNames.add(splitString(track.getName())); // use a track name once
                // block cars by destination
                for (RouteLocation rld : train.getRoute().getLocationsBySequenceList()) {
                    for (Car car : carList) {
                        if (pickedUp.contains(car.getId())
                                || (Setup.isSortByTrackEnabled() && !splitString(track.getName()).equals(
                                        splitString(car.getTrackName())))) {
                            continue;
                        }
                        // note that a car in train doesn't have a track assignment
                        if (car.getRouteLocation() == location && car.getTrack() != null
                                && car.getRouteDestination() == rld) {
                            pickedUp.add(car.getId());
                            if (car.isUtility()) {
                                builder.append(pickupUtilityCars(carList, car, location, rld, TrainCommon.IS_MANIFEST));
                            } // use truncated format if there's a switch list
                            else if (Setup.isTruncateManifestEnabled() && location.getLocation().isSwitchListEnabled()) {
                                builder.append(pickUpCar(car, Setup.getPickupTruncatedManifestMessageFormat()));
                            } else {
                                builder.append(pickUpCar(car, Setup.getPickupManifestMessageFormat()));
                            }
                            pickupCars = true;
                            cars++;
                            newWork = true;
                            if (car.getLoadType().equals(CarLoad.LOAD_TYPE_EMPTY)) {
                                emptyCars++;
                            }
                        }
                    }
                }
            }
            return builder.toString();
        } else { // local move
            return dropCars(local);
        }
    }

    private String dropCars(boolean local) {
        StringBuilder builder = new StringBuilder();
        RouteLocation location = train.getCurrentLocation();
        List<Car> carList = CarManager.instance().getByTrainDestinationList(train);
        List<Track> tracks = location.getLocation().getTrackByNameList(null);
        List<String> trackNames = new ArrayList<String>();
        List<String> dropped = new ArrayList<String>();
        for (Track track : tracks) {
            if (trackNames.contains(splitString(track.getName()))) {
                continue;
            }
            trackNames.add(splitString(track.getName())); // use a track name once
            for (Car car : carList) {
                if (dropped.contains(car.getId())
                        || (Setup.isSortByTrackEnabled() && !splitString(track.getName()).equals(
                                splitString(car.getDestinationTrackName())))) {
                    continue;
                }
                if (isLocalMove(car) == local
                        && (car.getRouteDestination() == location && car.getDestinationTrack() != null)) {
                    dropped.add(car.getId());
                    if (car.isUtility()) {
                        builder.append(setoutUtilityCars(carList, car, location, local));
                        // } else if (Setup.isTruncateManifestEnabled() && location.getLocation().isSwitchListEnabled())
                        // {
                        // // use truncated format if there's a switch list
                        // builder.append(dropCar(car, Setup.getDropTruncatedManifestMessageFormat(), local));
                    } else {
                        String[] format = (!local) ? Setup.getDropManifestMessageFormat() : Setup
                                .getLocalManifestMessageFormat();
                        // if (Setup.isSwitchListFormatSameAsManifest()) {
                        // format = (!local) ? Setup.getDropCarMessageFormat() : Setup.getLocalMessageFormat();
                        // }
                        builder.append(dropCar(car, format, local));
                    }
                    dropCars = true;
                    cars--;
                    newWork = true;
                    if (car.getLoadType().equals(CarLoad.LOAD_TYPE_EMPTY)) {
                        emptyCars--;
                    }
                }
            }
        }
        return builder.toString();
    }
}
