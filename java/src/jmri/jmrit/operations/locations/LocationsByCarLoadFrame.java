// LocationsByCarLoadFrame.java

package jmri.jmrit.operations.locations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.jmrit.operations.rollingstock.cars.CarLoad;
import jmri.jmrit.operations.rollingstock.cars.CarLoads;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;

import java.awt.*;

import javax.swing.*;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Frame to display which locations service certain car loads
 * 
 * @author Dan Boudreau Copyright (C) 2014
 * @version $Revision: 27492 $
 */

public class LocationsByCarLoadFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

	LocationManager locationManager;

	// checkboxes track id as the checkbox name
	ArrayList<JCheckBox> trackCheckBoxList = new ArrayList<JCheckBox>();
	JPanel locationCheckBoxes = new JPanel();

	// panels
	JPanel pLocations;

	// major buttons
	JButton clearButton = new JButton(Bundle.getMessage("Clear"));
	JButton setButton = new JButton(Bundle.getMessage("Select"));
	JButton saveButton = new JButton(Bundle.getMessage("Save"));

	// check boxes
	// JCheckBox copyCheckBox = new JCheckBox(Bundle.getMessage("Copy"));
	JCheckBox loadAndTypeCheckBox = new JCheckBox(Bundle.getMessage("TypeAndLoad"));

	// radio buttons

	// text field

	// combo boxes
	JComboBox typeComboBox = CarTypes.instance().getComboBox();
	JComboBox loadComboBox = CarLoads.instance().getComboBox(null);

	// selected location
	Location _location;

	public LocationsByCarLoadFrame() {
		super();
	}

	public void initComponents(Location location) {
		this._location = location;
		initComponents();
	}

	public void initComponents() {

		// load managers
		locationManager = LocationManager.instance();

		// general GUI config
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		// Set up the panels
		JPanel pCarType = new JPanel();
		pCarType.setLayout(new GridBagLayout());
		pCarType.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Type")));

		addItem(pCarType, typeComboBox, 0, 0);

		JPanel pLoad = new JPanel();
		pLoad.setLayout(new GridBagLayout());
		pLoad.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Load")));

		addItem(pLoad, loadComboBox, 0, 0);
		addItem(pLoad, loadAndTypeCheckBox, 1, 0);

		pLocations = new JPanel();
		pLocations.setLayout(new GridBagLayout());
		JScrollPane locationPane = new JScrollPane(pLocations);
		locationPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		locationPane.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Locations")));
		updateLoadComboBox();

		JPanel pButtons = new JPanel();
		pButtons.setLayout(new GridBagLayout());
		pButtons.setBorder(BorderFactory.createTitledBorder(""));

		addItem(pButtons, clearButton, 0, 0);
		addItem(pButtons, setButton, 1, 0);
		addItem(pButtons, saveButton, 2, 0);

		getContentPane().add(pCarType);
		getContentPane().add(pLoad);
		getContentPane().add(locationPane);
		getContentPane().add(pButtons);

		// setup combo box
		addComboBoxAction(typeComboBox);
		addComboBoxAction(loadComboBox);

		// setup buttons
		addButtonAction(setButton);
		addButtonAction(clearButton);
		addButtonAction(saveButton);

		// setup checkbox
		addCheckBoxAction(loadAndTypeCheckBox);

		locationManager.addPropertyChangeListener(this);
		CarTypes.instance().addPropertyChangeListener(this);
		CarLoads.instance().addPropertyChangeListener(this);

		// build menu
		JMenuBar menuBar = new JMenuBar();
		JMenu toolMenu = new JMenu(Bundle.getMessage("Tools"));
		toolMenu.add(new PrintLocationsByCarTypesAction(Bundle.getMessage("MenuItemPrintByType"), new Frame(), false,
				this));
		toolMenu.add(new PrintLocationsByCarTypesAction(Bundle.getMessage("MenuItemPreviewByType"), new Frame(), true,
				this));
		menuBar.add(toolMenu);
		setJMenuBar(menuBar);
		addHelpMenu("package.jmri.jmrit.operations.Operations_ModifyLocationsByCarType", true); // NOI18N

		setPreferredSize(null); // we need to resize this frame
		pack();
		setMinimumSize(new Dimension(Control.smallPanelWidth, Control.minPanelHeight));
		setSize(getWidth() + 25, getHeight()); // make a bit wider to eliminate scroll bar
		if (_location != null)
			setTitle(Bundle.getMessage("TitleModifyLocationLoad"));
		else
			setTitle(Bundle.getMessage("TitleModifyLocationsLoad"));
		setVisible(true);
	}

	public void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
		log.debug("combo box action");
		if (ae.getSource() == loadComboBox) {
			log.debug("Load combobox change, selected load: ({})", loadComboBox.getSelectedItem());
			if (loadComboBox.isEnabled() && loadComboBox.getSelectedItem() != null)
				updateLocations();
		}
		if (ae.getSource() == typeComboBox) {
			updateLoadComboBox();
		}
	}

	// Save, Delete, Add
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == saveButton)
			save();
		if (ae.getSource() == setButton)
			selectCheckboxes(true);
		if (ae.getSource() == clearButton)
			selectCheckboxes(false);
	}

	/**
	 * Update the car types that locations and tracks service. Note that the checkbox name is the id of the location or
	 * track.
	 */
	private void save() {
		log.debug("save");
		OperationsXml.save();
		if (Setup.isCloseWindowOnSaveEnabled())
			dispose();
	}

	/**
	 * Update tracks at locations based on car type serviced, or car loads serviced. If car loads, disable any tracks
	 * that don't service the car type selected.
	 */
	private void updateLocations() {
		log.debug("update checkboxes");
		removePropertyChangeLocations();
		trackCheckBoxList.clear();
		int x = 0;
		pLocations.removeAll();
		String type = (String) typeComboBox.getSelectedItem();
		String load = (String) loadComboBox.getSelectedItem();
		log.debug("Selected car type : ({}) load ({})", type, load);
		List<Location> locations = locationManager.getLocationsByNameList();
		for (Location location : locations) {
			// show only one location?
			if (_location != null && _location != location)
				continue;
			location.addPropertyChangeListener(this);
			JLabel locationName = new JLabel(location.getName());
			addItemLeft(pLocations, locationName, 0, x++);
			List<Track> tracks = location.getTrackByNameList(null);
			for (Track track : tracks) {
				track.addPropertyChangeListener(this);
				JCheckBox cb = new JCheckBox(track.getName());
				cb.setName(track.getId());
				addCheckBoxAction(cb);
				trackCheckBoxList.add(cb);
				cb.setEnabled(track.acceptsTypeName(type));
				cb.setSelected(track.acceptsLoad(load, type));
				addItemLeft(pLocations, cb, 1, x++);
				if (cb.isEnabled())
					cb.setToolTipText(MessageFormat.format(Bundle.getMessage("TipTrackCarLoad"), new Object[] { load }));
				else
					cb.setToolTipText(MessageFormat.format(Bundle.getMessage("TipTrackNotThisType"),
							new Object[] { type }));
			}
		}
		pLocations.revalidate();
		repaint();
	}

	private void updateTypeComboBox() {
		log.debug("update type combobox");
		CarTypes.instance().updateComboBox(typeComboBox);
	}

	private void updateLoadComboBox() {
		log.debug("update load combobox");
		if (typeComboBox.getSelectedItem() != null) {
			String type = (String) typeComboBox.getSelectedItem();
			String load = (String) loadComboBox.getSelectedItem();
			log.debug("Selected car type : ({}) load ({})", type, load);
			CarLoads.instance().updateComboBox(type, loadComboBox);
			loadComboBox.setEnabled(false); // used as a flag to prevent updateLocations()
			if (load != null)
				loadComboBox.setSelectedItem(load);
			loadComboBox.setEnabled(true);
			updateLocations();
		}
	}

	private void selectCheckboxes(boolean select) {
		for (JCheckBox cb : trackCheckBoxList) {
			if (cb.isEnabled())
				cb.setSelected(select);
		}
	}
	
	TrackLoadEditFrame tlef; // if there's an issue bring up the load edit window

	public void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == loadAndTypeCheckBox) {
			updateLocations();
			return;
		}
		JCheckBox cb = (JCheckBox) ae.getSource();
		String[] id = cb.getName().split("s");
		Location loc = locationManager.getLocationById(id[0]);
		if (loc != null) {
			Track track = loc.getTrackById(cb.getName());
			// if enabled track services this car type
			log.debug("CheckBox : {} track: ({}) isEnabled: {} isSelected: {}", cb.getName(), track.getName(), cb
					.isEnabled(), cb.isSelected());
			if (cb.isEnabled()) {
				boolean needLoadTrackEditFrame = false;
				String loadName = (String) loadComboBox.getSelectedItem();
				String load = loadName;
				String type = (String) typeComboBox.getSelectedItem();
				log.debug("Selected load ({})", loadName);
				if (loadAndTypeCheckBox.isSelected())
					loadName = type + CarLoad.SPLIT_CHAR + loadName;
				if (cb.isSelected()) {
					if (track.getLoadOption().equals(Track.ALL_LOADS))
						log.debug("All loads selected for track ({})", track.getName());
					else if (track.getLoadOption().equals(Track.INCLUDE_LOADS))
						track.addLoadName(loadName);
					else if (track.getLoadOption().equals(Track.EXCLUDE_LOADS)) {
						track.deleteLoadName(loadName);
						// need to check if load configuration is to exclude all car types using this load
						if (!track.acceptsLoadName(load)) {
							JOptionPane.showMessageDialog(this, MessageFormat.format(Bundle
									.getMessage("WarningExcludeTrackLoad"), new Object[] { track.getName(), load }),
									Bundle.getMessage("Error"), JOptionPane.WARNING_MESSAGE);
							needLoadTrackEditFrame = true;
						} else if (!track.acceptsLoad(load, type)) {
							JOptionPane.showMessageDialog(this, MessageFormat.format(Bundle
									.getMessage("WarningExcludeTrackTypeAndLoad"), new Object[] { track.getName(),
									type, load }), Bundle.getMessage("Error"), JOptionPane.WARNING_MESSAGE);
							needLoadTrackEditFrame = true;
						}
					}
				} else {
					if (track.getLoadOption().equals(Track.INCLUDE_LOADS)) {
						track.deleteLoadName(loadName);
						// need to check if load configuration is to accept all car types using this load
						if (track.acceptsLoadName(load)) {
							JOptionPane.showMessageDialog(this, MessageFormat.format(Bundle
									.getMessage("WarningAcceptTrackLoad"), new Object[] { track.getName(), load }),
									Bundle.getMessage("Error"), JOptionPane.WARNING_MESSAGE);
							needLoadTrackEditFrame = true;
						} else if (track.acceptsLoad(load, type)) {
							JOptionPane.showMessageDialog(this, MessageFormat.format(Bundle
									.getMessage("WarningAcceptTrackTypeAndLoad"), new Object[] { track.getName(),
									type, load }), Bundle.getMessage("Error"), JOptionPane.WARNING_MESSAGE);
							needLoadTrackEditFrame = true;
						}
					} else if (track.getLoadOption().equals(Track.EXCLUDE_LOADS)) {
						track.addLoadName(loadName);
					} else if (track.getLoadOption().equals(Track.ALL_LOADS)) {
						// need to exclude this load
						track.setLoadOption(Track.EXCLUDE_LOADS);
						track.addLoadName(loadName);
					}
				}
				if (needLoadTrackEditFrame) {
					if (tlef != null)
						tlef.dispose();
					tlef = new TrackLoadEditFrame();
					tlef.initComponents(track.getLocation(), track);
				}
			}
		}
	}

	private void removePropertyChangeLocations() {
		for (Location location : locationManager.getList()) {
			location.removePropertyChangeListener(this);
			List<Track> tracks = location.getTrackList();
			for (Track track : tracks) {
				track.removePropertyChangeListener(this);
			}
		}
	}

	public void dispose() {
		locationManager.removePropertyChangeListener(this);
		CarTypes.instance().removePropertyChangeListener(this);
		CarLoads.instance().removePropertyChangeListener(this);
		removePropertyChangeLocations();
		super.dispose();
	}

	public void propertyChange(java.beans.PropertyChangeEvent e) {
		if (log.isDebugEnabled())
			log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
					.getNewValue());
		if (e.getPropertyName().equals(LocationManager.LISTLENGTH_CHANGED_PROPERTY)
				|| e.getPropertyName().equals(Location.TYPES_CHANGED_PROPERTY)
				|| e.getPropertyName().equals(Location.NAME_CHANGED_PROPERTY)
				|| e.getPropertyName().equals(Location.TRACK_LISTLENGTH_CHANGED_PROPERTY)
				|| e.getPropertyName().equals(Track.TYPES_CHANGED_PROPERTY)
				|| e.getPropertyName().equals(Track.NAME_CHANGED_PROPERTY))
			updateLocations();
		if (e.getPropertyName().equals(CarTypes.CARTYPES_CHANGED_PROPERTY)
				|| e.getPropertyName().equals(CarTypes.CARTYPES_NAME_CHANGED_PROPERTY))
			updateTypeComboBox();
		if (e.getPropertyName().equals(CarLoads.LOAD_CHANGED_PROPERTY))
			updateLoadComboBox();
	}

	static Logger log = LoggerFactory.getLogger(LocationsByCarLoadFrame.class.getName());
}
