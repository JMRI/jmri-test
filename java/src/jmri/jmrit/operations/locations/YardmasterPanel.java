// YardmasterFrame.java
package jmri.jmrit.operations.locations;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import jmri.jmrit.operations.CommonConductorYardmasterPanel;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainCommon;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.jmrit.operations.trains.TrainManifestText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Yardmaster Frame. Shows work at one location.
 *
 * @author Dan Boudreau Copyright (C) 2013
 * @version $Revision: 18630 $
 */
public class YardmasterPanel extends CommonConductorYardmasterPanel {

    /**
	 * 
	 */
	private static final long serialVersionUID = -88218348551032298L;

	protected static final boolean isManifest = false;

    int _visitNumber = 1;

    // text panes
    JTextPane textSwitchListComment = new JTextPane();

    // combo boxes
    JComboBox trainComboBox = new JComboBox();
    JComboBox trainVisitComboBox = new JComboBox();

    // buttons
    JButton nextButton = new JButton(Bundle.getMessage("Next"));

    // panels
    JPanel pTrainVisit = new JPanel();

    public void initComponents(Location location) {
        super.initComponents();

        _location = location;

        // row 2
        JPanel pRow2 = new JPanel();
        pRow2.setLayout(new BoxLayout(pRow2, BoxLayout.X_AXIS));

        pRow2.add(pLocationName); // row 2a (location name)
        pRow2.add(pRailRoadName); // row 2b (railroad name)

        // row 5 (switch list comment)
        JPanel pSwitchListComment = new JPanel();
        pSwitchListComment.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Comment")));
        pSwitchListComment.add(textSwitchListComment);
        textSwitchListComment.setBackground(null);

        // row 6
        JPanel pRow6 = new JPanel();
        pRow6.setLayout(new BoxLayout(pRow6, BoxLayout.X_AXIS));

        // row 6a (train name)
        JPanel pTrainName = new JPanel();
        pTrainName.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Train")));
        pTrainName.add(trainComboBox);
        // add next button for web server
        pTrainName.add(nextButton);

        // row 6b (train visit)
        pTrainVisit.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Visit")));
        pTrainVisit.add(trainVisitComboBox);

        pRow6.add(pTrainName);
        pRow6.add(pTrainVisit);
        pRow6.add(pTrainDescription); // row 6c (train description)

        pButtons.setMaximumSize(new Dimension(2000, 200));

        add(pRow2);
        add(pLocationComment);
        add(pSwitchListComment);
        add(pRow6);
        add(pTrainComment);
        add(pTrainRouteComment);
        add(pTrainRouteLocationComment);
        add(locoPane);
        add(pWorkPanes);
        add(movePane);
        add(pStatus);
        add(pButtons);

        if (_location != null) {
            textLocationName.setText(_location.getName());
            textLocationComment.setText(lineWrap(_location.getComment()));
            pLocationComment.setVisible(!_location.getComment().equals(Location.NONE) && Setup.isPrintLocationCommentsEnabled());
            textSwitchListComment.setText(lineWrap(_location.getSwitchListComment()));
            pSwitchListComment.setVisible(!_location.getSwitchListComment().equals(Location.NONE));
            updateTrainsComboBox();
        }

        update();

        addComboBoxAction(trainComboBox);
        addComboBoxAction(trainVisitComboBox);

        addButtonAction(nextButton);

        // listen for trains being built
        addTrainListeners();

        setVisible(true);

    }

    // Select, Clear, and Set Buttons
    @Override
    public void buttonActionPerformed(ActionEvent ae) {
        if (ae.getSource() == nextButton) {
            nextButtonAction();
        }
        super.buttonActionPerformed(ae);
        update();
    }

    private void nextButtonAction() {
        log.debug("next button activated");
        if (trainComboBox.getItemCount() > 1) {
            if (pTrainVisit.isVisible()) {
                int index = trainVisitComboBox.getSelectedIndex() + 1;
                if (index < trainVisitComboBox.getItemCount()) {
                    trainVisitComboBox.setSelectedIndex(index);
                    return; // done
                }
            }
            int index = trainComboBox.getSelectedIndex() + 1;
            if (index >= trainComboBox.getItemCount()) {
                index = 0;
            }
            trainComboBox.setSelectedIndex(index);
        }
    }

    // Select Train and Visit
    @Override
    protected void comboBoxActionPerformed(ActionEvent ae) {
        // made the combo box not visible during updates, so ignore if not visible
        if (ae.getSource() == trainComboBox && trainComboBox.isVisible()) {
            _train = null;
            if (trainComboBox.getSelectedItem() != null && !trainComboBox.getSelectedItem().equals(TrainManager.NONE)) {
                _train = (Train) trainComboBox.getSelectedItem();
                _visitNumber = 1;
            }
            clearAndUpdate();
        }
        // made the combo box not visible during updates, so ignore if not visible
        if (ae.getSource() == trainVisitComboBox && trainVisitComboBox.isVisible()) {
            if (trainVisitComboBox.getSelectedItem() != null) {
                _visitNumber = (Integer) trainVisitComboBox.getSelectedItem();
                clearAndUpdate();
            }
        }
    }

    private void clearAndUpdate() {
        trainCommon.clearUtilityCarTypes(); // reset the utility car counts
        carCheckBoxes.clear();
        isSetMode = false;
        update();
    }

    private void update() {
        log.debug("update, setMode: {}", isSetMode);
        initialize();

        // turn everything off and re-enable if needed
        pButtons.setVisible(false);
        pTrainVisit.setVisible(false);
        trainVisitComboBox.setVisible(false); // Use visible as a flag to ignore updates
        pTrainComment.setVisible(false);
        pTrainRouteComment.setVisible(false);
        pTrainRouteLocationComment.setVisible(false);

        textTrainDescription.setText("");
        textStatus.setText("");

        if (_train != null && _train.getRoute() != null) {
            Route route = _train.getRoute();
            pButtons.setVisible(true);
            textTrainDescription.setText(_train.getDescription());
            // show train comment box only if there's a comment
            if (!_train.getComment().equals("")) {
                pTrainComment.setVisible(true);
                textTrainComment.setText(lineWrap(_train.getComment()));
            }
            // show route comment box only if there's a route comment
            if (!route.getComment().equals("") && Setup.isPrintRouteCommentsEnabled()) {
                pTrainRouteComment.setVisible(true);
                textTrainRouteComment.setText(lineWrap(route.getComment()));
            }
            // Does this train have a unique railroad name?
            if (!_train.getRailroadName().equals("")) {
                textRailRoadName.setText(_train.getRailroadName());
            } else {
                textRailRoadName.setText(Setup.getRailroadName());
            }

            // determine how many times this train visits this location and if it is the last stop
            RouteLocation rl = null;
            boolean lastLocation = false;
            List<RouteLocation> routeList = route.getLocationsBySequenceList();
            int visitNumber = 0;
            for (int i = 0; i < routeList.size(); i++) {
                if (TrainCommon.splitString(routeList.get(i).getName()).equals(
                        TrainCommon.splitString(_location.getName()))) {
                    visitNumber++;
                    if (visitNumber == _visitNumber) {
                        rl = routeList.get(i);
                        if (i == routeList.size() - 1) {
                            lastLocation = true;
                        }
                    }
                }
            }

            if (rl != null) {
                // update visit numbers
                if (visitNumber > 1) {
                    trainVisitComboBox.removeAllItems(); // this fires an action change!
                    for (int i = 0; i < visitNumber; i++) {
                        trainVisitComboBox.addItem(i + 1);
                    }
                    trainVisitComboBox.setSelectedItem(_visitNumber);
                    trainVisitComboBox.setVisible(true); // now pay attention to changes
                    pTrainVisit.setVisible(true); // show the visit panel
                }

                // update comment and location name
                pTrainRouteLocationComment.setVisible(!rl.getComment().equals(RouteLocation.NONE));
                textTrainRouteLocationComment.setText(lineWrap(rl.getComment()));
                textLocationName.setText(rl.getLocation().getName()); // show name including hyphen and number

                // check for locos
                updateLocoPanes(rl);

                // now update the car pick ups and set outs
                blockCars(rl, isManifest);

                if (lastLocation) {
                    textStatus.setText(MessageFormat.format(TrainManifestText.getStringTrainTerminates(),
                            new Object[]{TrainCommon.splitString(_train.getTrainTerminatesName())}));
                } else {
                    textStatus.setText(getStatus(rl));
                }
            }
            updateComplete();
        }
    }

    private void updateTrainsComboBox() {
        Object selectedItem = trainComboBox.getSelectedItem();
        trainComboBox.setVisible(false); // used as a flag to ignore updates
        trainComboBox.removeAllItems();
        trainComboBox.addItem(TrainManager.NONE);
        if (_location != null) {
            List<Train> trains = trainManager.getTrainsArrivingThisLocationList(_location);
            trains.stream().filter((train) -> (TrainCommon.isThereWorkAtLocation(train, _location))).forEach((train) -> {
                trainComboBox.addItem(train);
            });
        }
        if (selectedItem != null) {
            trainComboBox.setSelectedItem(selectedItem);
        }
        trainComboBox.setVisible(true);
    }

    private void addTrainListeners() {
        log.debug("Adding train listerners");
        List<Train> trains = TrainManager.instance().getTrainsByIdList();
        trains.stream().forEach((train) -> {
            train.addPropertyChangeListener(this);
        });
        // listen for new trains being added
        TrainManager.instance().addPropertyChangeListener(this);
    }

    private void removeTrainListeners() {
        log.debug("Removing train listerners");
        List<Train> trains = TrainManager.instance().getTrainsByIdList();
        trains.stream().forEach((train) -> {
            train.removePropertyChangeListener(this);
        });
        TrainManager.instance().removePropertyChangeListener(this);
    }

    public void dispose() {
        removeTrainListeners();
        removePropertyChangeListerners();
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (Control.showProperty && log.isDebugEnabled()) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
                    .getNewValue());
        }
        if ((e.getPropertyName().equals(RollingStock.ROUTE_LOCATION_CHANGED_PROPERTY) && e.getNewValue() == null)
                || (e.getPropertyName().equals(RollingStock.ROUTE_DESTINATION_CHANGED_PROPERTY) && e.getNewValue() == null)
                || e.getPropertyName().equals(RollingStock.TRAIN_CHANGED_PROPERTY)) {
            // remove car from list
            if (e.getSource().getClass().equals(Car.class)) {
                Car car = (Car) e.getSource();
                carCheckBoxes.remove("p" + car.getId());
                carCheckBoxes.remove("s" + car.getId());
                carCheckBoxes.remove("m" + car.getId());
                log.debug("Car ({}) removed from list", car.toString());
            }
            update();
        }
        if (e.getPropertyName().equals(Train.BUILT_CHANGED_PROPERTY)) {
            updateTrainsComboBox();
        }
    }

    static Logger log = LoggerFactory.getLogger(YardmasterPanel.class.getName());
}
