// TrainSwitchListEditFrame.java

package jmri.jmrit.operations.trains;


import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.ScrollPaneConstants;

import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.LocationManagerXml;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;

import java.beans.PropertyChangeEvent;


/**
 * Frame for user selection of switch lists
 * 
 * @author Dan Boudreau Copyright (C) 2008, 2012
 * @version $Revision$
 */

public class TrainSwitchListEditFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.trains.JmritOperationsTrainsBundle");
	
	JScrollPane switchPane;
	
	// load managers
	LocationManager manager = LocationManager.instance(); 
	List<JCheckBox> locationCheckBoxes = new ArrayList<JCheckBox>();
	List<JComboBox> locationComboBoxes = new ArrayList<JComboBox>();
	JPanel locationPanelCheckBoxes = new JPanel();

	// labels
	JLabel textName = new JLabel(rb.getString("Location"));
	JLabel textStatus = new JLabel(rb.getString("Status"));
	JLabel textPrinter = new JLabel(rb.getString("Printer"));
	JLabel space1 = new JLabel("        ");
	JLabel space2 = new JLabel("        ");

	// major buttons
	JButton clearButton = new JButton(rb.getString("Clear"));
	JButton setButton = new JButton(rb.getString("Select"));
	JButton printButton = new JButton(rb.getString("PrintSwitchLists"));
	JButton previewButton = new JButton(rb.getString("PreviewSwitchLists"));
	JButton changeButton = new JButton(rb.getString("PrintChanges"));
	JButton csvGenerateButton = new JButton(rb.getString("CsvGenerate"));
	JButton csvChangeButton = new JButton(rb.getString("CsvChanges"));
	JButton updateButton = new JButton(rb.getString("Update"));
	JButton saveButton = new JButton(rb.getString("Save"));

	// text field
	
	// combo boxes

	public TrainSwitchListEditFrame() {
		super();
    	// Set up the jtable in a Scroll Pane..
	}

	public void initComponents() {
		// listen for any changes in the number of locations
		manager.addPropertyChangeListener(this);
		
		// the following code sets the frame's initial state
	    getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
	    
    	switchPane = new JScrollPane(locationPanelCheckBoxes);
    	switchPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    	getContentPane().add(switchPane);
		
	    // Layout the panel by rows
	   	locationPanelCheckBoxes.setLayout(new GridBagLayout());
		updateLocationCheckboxes();
		
		// row 2
    	JPanel controlpanel = new JPanel();
    	controlpanel.setLayout(new GridBagLayout());
 
    	addItem(controlpanel, clearButton, 0, 1);
    	addItem(controlpanel, setButton, 1, 1);
    	addItem(controlpanel, saveButton, 2, 1);
    	
    	// row 3
    	addItem(controlpanel, previewButton, 0, 2);
    	addItem(controlpanel, printButton, 1, 2);
    	addItem(controlpanel, changeButton, 2, 2);
    	
    	// row 4
    	if (!Setup.isSwitchListRealTime()){
    		addItem(controlpanel, updateButton, 0, 3);
    	}
    	if (Setup.isGenerateCsvSwitchListEnabled()){
    		addItem(controlpanel, csvGenerateButton, 1, 3);
    		addItem(controlpanel, csvChangeButton, 2, 3);
    	}
    	
		getContentPane().add(controlpanel);
		
		// setup buttons
		addButtonAction(clearButton);
		addButtonAction(setButton);
       	addButtonAction(printButton);
		addButtonAction(previewButton);
		addButtonAction(changeButton);
		addButtonAction(csvGenerateButton);
		addButtonAction(csvChangeButton);
		addButtonAction(updateButton);
		addButtonAction(saveButton);
		
        // add help menu to window
    	addHelpMenu("package.jmri.jmrit.operations.Operations_SwitchList", true);
		// set frame size and train for display
    	pack();
		setTitle(rb.getString("TitleSwitchLists"));
		setVisible(true);
	}
	
	// Buttons 
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == clearButton){
			selectCheckboxes(false);
		}
		if (ae.getSource() == setButton){
			selectCheckboxes(true);
		}
		if (ae.getSource() == previewButton){
			buildSwitchList(true, !Setup.isSwitchListRealTime(), false, false);
		}
		if (ae.getSource() == printButton){
			buildSwitchList(false, !Setup.isSwitchListRealTime(), false, false);
		}
		if (ae.getSource() == changeButton){
			buildSwitchList(false, true, false, false);
		}
		if (ae.getSource() == csvGenerateButton){
			buildSwitchList(false, false, true, false);
		}
		if (ae.getSource() == csvChangeButton){
			buildSwitchList(false, true, true, false);
		}
		if (ae.getSource() == updateButton){
			buildSwitchList(true, true, false, true);
		}
		if (ae.getSource() == saveButton){
			save();
			if (Setup.isCloseWindowOnSaveEnabled())
				dispose();
		}
	}
	
	// save printer selection
	private void save(){
		for (int i =0; i<locationCheckBoxes.size(); i++){
			String locationName = locationCheckBoxes.get(i).getName();
			Location l = manager.getLocationByName(locationName);
			JComboBox comboBox = locationComboBoxes.get(i);
			String printerName = (String)comboBox.getSelectedItem();
			if (printerName.equals(TrainPrintUtilities.getDefaultPrinterName())){
				l.setDefaultPrinterName("");
			} else {
				log.debug("Location "+l.getName()+" has selected printer "+printerName);
				l.setDefaultPrinterName(printerName);
			}
		}
		// save location file
		LocationManagerXml.instance().writeOperationsFile();
	}
	
	private void buildSwitchList(boolean isPreview, boolean isChanged, boolean isCsv, boolean isUpdate){
		TrainSwitchLists ts = new TrainSwitchLists();
		for (int i =0; i<locationCheckBoxes.size(); i++){
			String locationName = locationCheckBoxes.get(i).getName();
			Location location = manager.getLocationByName(locationName);
			if (location.isSwitchListEnabled() 
					&& (location.getStatus().equals(Location.MODIFIED) || !isChanged)){
				if (!isCsv)
					ts.buildSwitchList(location, isChanged);
				if (isCsv && Setup.isGenerateCsvSwitchListEnabled()){
					TrainCsvSwitchLists tCSVs = new TrainCsvSwitchLists();
					tCSVs.buildSwitchList(location, isChanged);
					location.setStatus(Location.CSV_GENERATED);
				}
			}
			if (location.isSwitchListEnabled() && !isCsv){
				// change status to updated if not running in real time mode for switch lists
				if (location.getStatus().equals(Location.MODIFIED) && !Setup.isSwitchListRealTime())
					location.setStatus(Location.UPDATED);
				if (!isUpdate)
					ts.printSwitchList(location, isPreview);
				if (!isPreview){
					location.setStatus(Location.PRINTED);
					location.setSwitchListState(Location.SW_PRINTED);
				}
			}
		}
		// set trains switch lists printed
		TrainManager trainManager = TrainManager.instance();
		List<String> trains = trainManager.getTrainsByTimeList();
		for (int i=0; i<trains.size(); i++){
			Train train = trainManager.getTrainById(trains.get(i));
			if (!train.isBuilt())
				continue;	// train wasn't built so skip
			train.setSwitchListStatus(Train.PRINTED);
		}
		
	}
	
	private void selectCheckboxes(boolean enable){
		for (int i =0; i<locationCheckBoxes.size(); i++){
			String locationName = locationCheckBoxes.get(i).getName();
			Location l = manager.getLocationByName(locationName);
			l.setSwitchListEnabled(enable);
		}
	}
	
	// name change or number of locations has changed
	private void updateLocationCheckboxes(){
		changeButton.setEnabled(false);
		csvChangeButton.setEnabled(false);
		List<String> locations = manager.getLocationsByNameList();		
		synchronized (this) {
			for (int i =0; i<locations.size(); i++){
				Location l = manager.getLocationById(locations.get(i));
				l.removePropertyChangeListener(this);
			}
		}
		
		locationCheckBoxes.clear();
		locationComboBoxes.clear();		// remove printer selection
		locationPanelCheckBoxes.removeAll();
		
	    addItem(locationPanelCheckBoxes, textName, 0, 0);
	    addItem(locationPanelCheckBoxes, space1, 1, 0);
	    addItem(locationPanelCheckBoxes, textStatus, 2, 0);
	    addItem(locationPanelCheckBoxes, space2, 3, 0);
	    addItem(locationPanelCheckBoxes, textPrinter, 4, 0);
		
		int y = 1;		// vertical position in panel

		String previousName = "";
		
		for (int i =0; i<locations.size(); i++){
			Location l = manager.getLocationById(locations.get(i));
			String name = TrainCommon.splitString(l.getName());
			if (name.equals(previousName))
				continue;
			previousName = name;
			
			JCheckBox checkBox = new JCheckBox();
			locationCheckBoxes.add(checkBox);
			checkBox.setSelected(l.isSwitchListEnabled());
			checkBox.setText(name);
			checkBox.setName(l.getName());
			addLocationCheckBoxAction(checkBox);
			addItemLeft(locationPanelCheckBoxes, checkBox, 0, y);
			
			JLabel status = new JLabel(l.getStatus());
			if (l.getStatus().equals(Location.MODIFIED)){
				changeButton.setEnabled(true);
				csvChangeButton.setEnabled(true);
			}
			addItem(locationPanelCheckBoxes, status, 2, y);
			
			JComboBox comboBox = TrainPrintUtilities.getPrinterJComboBox();
			locationComboBoxes.add(comboBox);
			comboBox.setSelectedItem(l.getDefaultPrinterName());
			addItem(locationPanelCheckBoxes, comboBox, 4, y++);
			
			l.addPropertyChangeListener(this);					
		}

		Border border = BorderFactory.createEtchedBorder();
		locationPanelCheckBoxes.setBorder(border);
		locationPanelCheckBoxes.revalidate();
		pack();
		repaint();
	}
	
	// The print switch list for a location has changed
	private void changeLocationCheckboxes(PropertyChangeEvent e){
		Location l = (Location)e.getSource();
		for (int i=0; i<locationCheckBoxes.size(); i++){
			JCheckBox checkBox = locationCheckBoxes.get(i);
			if (checkBox.getName().equals(l.getName())){
				checkBox.setSelected(l.isSwitchListEnabled());
				break;
			}
		}
	}

	private void addLocationCheckBoxAction(JCheckBox b) {
		b.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				locationCheckBoxActionPerformed(e);
			}
		});
	}

	public void locationCheckBoxActionPerformed(ActionEvent ae) {
		JCheckBox b =  (JCheckBox)ae.getSource();
		log.debug("checkbox change "+ b.getName());
		Location l = manager.getLocationByName(b.getName());
		l.setSwitchListEnabled(b.isSelected());
	}

	public void dispose() {
		manager.removePropertyChangeListener(this);
		List<String> locations = manager.getLocationsByNameList();
		for (int i =0; i<locations.size(); i++){
			Location l = manager.getLocationById(locations.get(i));
			l.removePropertyChangeListener(this);
		}
		super.dispose();
	}

	public void propertyChange(PropertyChangeEvent e) {
		if (Control.showProperty && log.isDebugEnabled()) log.debug("Property change " +e.getPropertyName()+ " old: "+e.getOldValue()+ " new: "+e.getNewValue());
		if(e.getPropertyName().equals(Location.SWITCHLIST_CHANGED_PROPERTY)) 
			changeLocationCheckboxes(e);
		if (e.getPropertyName().equals(LocationManager.LISTLENGTH_CHANGED_PROPERTY) ||
				e.getPropertyName().equals(Location.NAME_CHANGED_PROPERTY) ||
				e.getPropertyName().equals(Location.STATUS_CHANGED_PROPERTY))
			updateLocationCheckboxes();
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(TrainSwitchListEditFrame.class.getName());
}
