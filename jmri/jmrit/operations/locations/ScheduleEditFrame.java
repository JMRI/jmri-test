// ScheduleEditFrame.java

package jmri.jmrit.operations.locations;

import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.OperationsFrame;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;

import java.text.MessageFormat;
import java.util.ResourceBundle;


/**
 * Frame for user edit of a schedule
 * 
 * @author Dan Boudreau Copyright (C) 2008
 * @version $Revision: 1.1 $
 */

public class ScheduleEditFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.locations.JmritOperationsLocationsBundle");
	
	ScheduleTableModel scheduleModel = new ScheduleTableModel();
	JTable scheduleTable = new JTable(scheduleModel);
	JScrollPane schedulePane;
	
	ScheduleManager manager;
	LocationManagerXml managerXml;

	Schedule _schedule = null;
	ScheduleItem _scheduleItem = null;
	Location _location = null;
	Track _track = null;

	// labels
	JLabel textName = new JLabel();
	JLabel textComment = new JLabel();

	// major buttons
	JButton addTypeButton = new JButton();
	JButton saveScheduleButton = new JButton();
	JButton deleteScheduleButton = new JButton();
	JButton addScheduleButton = new JButton();

	// check boxes
	JCheckBox checkBox;
	
	// radio buttons
    JRadioButton addLocAtTop = new JRadioButton(rb.getString("Top"));
    JRadioButton addLocAtBottom = new JRadioButton(rb.getString("Bottom"));
    ButtonGroup group = new ButtonGroup();
	
	// text field
	JTextField scheduleNameTextField = new JTextField(20);
	JTextField commentTextField = new JTextField(35);

	// for padding out panel
	JLabel space1 = new JLabel();
	JLabel space2 = new JLabel();
	JLabel space3 = new JLabel();
	
	// combo boxes
	JComboBox typeBox = new JComboBox();

	public static final String NAME = rb.getString("Name");
	public static final String LENGTH = rb.getString("Length");
	public static final String DISPOSE = "dispose" ;

	public ScheduleEditFrame() {
		super();
	}

	public void initComponents(Schedule schedule, Location location, Track track) {
				
		_schedule = schedule;
		_location = location;
		_track = track;

		// load managers
		manager = ScheduleManager.instance();
		managerXml = LocationManagerXml.instance();
		
		textName.setText(rb.getString("Name"));
		textName.setVisible(true);
		textComment.setText(rb.getString("Comment"));
		textComment.setVisible(true);
		space1.setText("     ");
		space1.setVisible(true);
		space2.setText("     ");
		space2.setVisible(true);

		deleteScheduleButton.setText(rb.getString("DeleteSchedule"));
		deleteScheduleButton.setVisible(true);
		addScheduleButton.setText(rb.getString("AddSchedule"));
		addScheduleButton.setVisible(true);
		saveScheduleButton.setText(rb.getString("SaveSchedule"));
		saveScheduleButton.setVisible(true);
		addTypeButton.setText(rb.getString("AddType"));
		addTypeButton.setVisible(true);
		
	   	// Set up the jtable in a Scroll Pane..
    	schedulePane = new JScrollPane(scheduleTable);
    	schedulePane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

 		
		if (_schedule != null){
			scheduleNameTextField.setText(_schedule.getName());
			commentTextField.setText(_schedule.getComment());
	      	scheduleModel.initTable(scheduleTable, schedule, _location, _track);
	      	enableButtons(true);
		} else {
			enableButtons(false);
		}
		
	    getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));

	    //      Set up the panels
    	JPanel p1 = new JPanel();
    	p1.setLayout(new GridBagLayout());
				
		// Layout the panel by rows
		// row 1
		addItem(p1, textName, 0, 1);
		addItemWidth(p1, scheduleNameTextField, 3, 1, 1);

		// row 2
    	JPanel p3 = new JPanel();
    	p3.setLayout(new GridBagLayout());
    	addItem(p3, typeBox, 0, 1);
    	addItem(p3, addTypeButton, 1, 1);
    	addItem(p3, addLocAtTop, 2, 1);
    	addItem(p3, addLocAtBottom, 3, 1);
    	group.add(addLocAtTop);
    	group.add(addLocAtBottom);
    	addLocAtBottom.setSelected(true);
		Border border = BorderFactory.createEtchedBorder();
		p3.setBorder(border);
		
		// row 9
    	JPanel p4 = new JPanel();
    	p4.setLayout(new GridBagLayout());
		
		// row 10
		int y = 10;
		addItem (p4, space1, 0, ++y);
    	
		// row 11
		addItem(p4, textComment, 0, ++y);
		addItemWidth(p4, commentTextField, 3, 1, y);
				
		// row 12
		addItem(p4, space2, 0, ++y);
		// row 13
		addItem(p4, deleteScheduleButton, 0, ++y);
		addItem(p4, addScheduleButton, 1, y);
		addItem(p4, saveScheduleButton, 3, y);
		
		getContentPane().add(p1);
       	getContentPane().add(schedulePane);
       	getContentPane().add(p3);
       	getContentPane().add(p4);
		
		// setup buttons
		addButtonAction(addTypeButton);
		addButtonAction(deleteScheduleButton);
		addButtonAction(addScheduleButton);
		addButtonAction(saveScheduleButton);
		
		// setup combobox
		loadTypeComboBox();

		// build menu
//		JMenuBar menuBar = new JMenuBar();
//		JMenu toolMenu = new JMenu("Tools");
//		menuBar.add(toolMenu);
//		setJMenuBar(menuBar);
		addHelpMenu("package.jmri.jmrit.operations.Operations_Schedules", true);

		//	 get notified if car types are changed
		CarTypes.instance().addPropertyChangeListener(this);
		_location.addPropertyChangeListener(this);
		_track.addPropertyChangeListener(this);
		
		// set frame size and schedule for display
		pack();
		if((getWidth()<900)) setSize(900, getHeight());
		setSize(getWidth(), 600);
		setVisible(true);
	}
	
	// Save, Delete, Add 
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == addTypeButton){
			log.debug("schedule add location button actived");
			if (typeBox.getSelectedItem() != null){
				if (typeBox.getSelectedItem().equals(""))
					return;
				addNewScheduleItem();
			}
		}
		if (ae.getSource() == saveScheduleButton){
			log.debug("schedule save button actived");
			Schedule schedule = manager.getScheduleByName(scheduleNameTextField.getText());
			if (_schedule == null && schedule == null){
				saveNewSchedule();
			} else {
				if (schedule != null && schedule != _schedule){
					reportScheduleExists(rb.getString("save"));
					return;
				}
				saveSchedule();
			}
		}
		if (ae.getSource() == deleteScheduleButton){
			log.debug("schedule delete button actived");
			Schedule schedule = manager.getScheduleByName(scheduleNameTextField.getText());
			if (schedule == null)
				return;
			
			manager.deregister(schedule);
			_schedule = null;

			enableButtons(false);
			// save schedule file
			managerXml.writeOperationsLocationFile();
		}
		if (ae.getSource() == addScheduleButton){
			Schedule schedule = manager.getScheduleByName(scheduleNameTextField.getText());
			if (schedule != null){
				reportScheduleExists(rb.getString("add"));
				return;
			}
			saveNewSchedule();
		}
	}
	
	private void addNewScheduleItem(){
		// add item to this schedule
		if (addLocAtTop.isSelected())
			_schedule.addItem((String)typeBox.getSelectedItem(),0);
		else
			_schedule.addItem((String)typeBox.getSelectedItem());
	}
	
	private void saveNewSchedule(){
		if (!checkName())
			return;
		Schedule schedule = manager.newSchedule(scheduleNameTextField.getText());
		scheduleModel.initTable(scheduleTable, schedule, _location, _track);
		_schedule = schedule;
		// enable checkboxes
		enableButtons(true);
		saveSchedule();
	}
	
	private void saveSchedule (){
		if (!checkName())
			return;
		_schedule.setName(scheduleNameTextField.getText());
		_schedule.setComment(commentTextField.getText());

		// save schedule file
		managerXml.writeOperationsLocationFile();
	}
	
	private void loadTypeComboBox(){
		typeBox.removeAllItems();
		String[] types = CarTypes.instance().getNames();
		for (int i=0; i<types.length; i++){
			if (_location.acceptsTypeName(types[i]) && _track.acceptsTypeName(types[i]))
					typeBox.addItem(types[i]);
		}
	}

	/**
	 * 
	 * @return true if name is less than 26 characters
	 */
	private boolean checkName(){
		if (scheduleNameTextField.getText().trim().equals(""))
			return false;
		if (scheduleNameTextField.getText().length() > 25){
			log.error("Schedule name must be less than 26 charaters");
			JOptionPane.showMessageDialog(this,
					rb.getString("ScheduleNameLess"), rb.getString("CanNotAddSchedule"),
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
	
	private void reportScheduleExists(String s){
		log.info("Can not " + s + ", schedule already exists");
		JOptionPane.showMessageDialog(this,
				rb.getString("ReportExists"), MessageFormat.format(rb.getString("CanNotSchedule"),new Object[]{s}),
				JOptionPane.ERROR_MESSAGE);
	}
	
	private void enableButtons(boolean enabled){
		typeBox.setEnabled(enabled);
		addTypeButton.setEnabled(enabled);
		addLocAtTop.setEnabled(enabled);
		addLocAtBottom.setEnabled(enabled);
		saveScheduleButton.setEnabled(enabled);
		deleteScheduleButton.setEnabled(enabled);
		scheduleTable.setEnabled(enabled);
		// the inverse!
		addScheduleButton.setEnabled(!enabled);
	}
	
	public void dispose() {
		CarTypes.instance().removePropertyChangeListener(this);
		_location.removePropertyChangeListener(this);
		_track.removePropertyChangeListener(this);
		scheduleModel.dispose();
		super.dispose();
	}
	
 	public void propertyChange(java.beans.PropertyChangeEvent e) {
		if (log.isDebugEnabled()) log.debug("ScheduleEditFrame sees property change: " +e.getPropertyName()+ " old: "+e.getOldValue()+ " new: "+e.getNewValue());
		if (e.getPropertyName().equals(CarTypes.CARTYPES_LENGTH_CHANGED_PROPERTY) ||
				e.getPropertyName().equals(Track.TYPES_CHANGED_PROPERTY) ||
				e.getPropertyName().equals(Location.TYPES_CHANGED_PROPERTY)){
			loadTypeComboBox();
		}
	}
 	
	static org.apache.log4j.Category log = org.apache.log4j.Category
			.getInstance(ScheduleEditFrame.class.getName());
}
