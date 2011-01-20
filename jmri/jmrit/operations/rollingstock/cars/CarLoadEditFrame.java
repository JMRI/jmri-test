// CarLoadEditFrame.java

 package jmri.jmrit.operations.rollingstock.cars;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.ScheduleManager;



/**
 * Frame for adding and editing the car roster for operations.
 *
 * @author Daniel Boudreau Copyright (C) 2009, 2010
 * @version             $Revision: 1.16 $
 */
public class CarLoadEditFrame extends OperationsFrame implements java.beans.PropertyChangeListener{
	
	final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.rollingstock.cars.JmritOperationsCarsBundle");
	
	CarLoads carLoads = CarLoads.instance();
	
	// labels
	JLabel textSep = new JLabel();
	JLabel quanity = new JLabel("0");

	// major buttons
	JButton addButton = new JButton(rb.getString("Add"));
	JButton deleteButton = new JButton(rb.getString("Delete"));
	JButton replaceButton = new JButton(rb.getString("Replace"));
	JButton saveButton = new JButton(rb.getString("Save"));
	
	// combo boxes
	JComboBox comboBox;
	JComboBox priorityComboBox;
	
	// text boxes
	JTextField addTextBox = new JTextField(10);
	JTextField pickupCommentTextField = new JTextField(35);
	JTextField dropCommentTextField = new JTextField(35);
	
    public CarLoadEditFrame() {}
    
    String _type;
    boolean menuActive = false;
    
    public void initComponents(String type, String select) {
    	
    	getContentPane().removeAll();
     	
        setTitle(MessageFormat.format(rb.getString("TitleCarEditLoad"),new Object[]{type}));
        
        // track which combo box is being edited 
        _type = type;
        loadComboboxes();
        comboBox.setSelectedItem(select);
        updatePriority();
        
        // general GUI config    
        quanity.setVisible(showQuanity);
        
		// load panel
		JPanel pLoad = new JPanel();
		pLoad.setLayout(new GridBagLayout());
		pLoad.setBorder(BorderFactory.createTitledBorder(rb.getString("Load")));
        
		// row 2
		addItem(pLoad, addTextBox, 2, 2);
        addItem(pLoad, addButton, 3, 2);
        
        // row 3
        addItem(pLoad, quanity, 1, 3);
        addItem(pLoad, comboBox, 2, 3);
        addItem(pLoad, deleteButton, 3, 3);
        
        // row 4 
        addItem(pLoad, replaceButton, 3, 4);
        
        // row 5
        JPanel pPriority = new JPanel();
		pPriority.setLayout(new BoxLayout(pPriority, BoxLayout.Y_AXIS));
		pPriority.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutPriority")));
		addItem(pPriority, priorityComboBox, 0, 0);
        
        // row 6
		// optional panel
		JPanel pOptionalPickup = new JPanel();
		pOptionalPickup.setLayout(new BoxLayout(pOptionalPickup, BoxLayout.Y_AXIS));
		pOptionalPickup.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutOptionalPickup")));		
		addItem(pOptionalPickup, pickupCommentTextField, 0, 0);
		
		// row 8
		JPanel pOptionalDrop = new JPanel();
		pOptionalDrop.setLayout(new BoxLayout(pOptionalDrop, BoxLayout.Y_AXIS));
		pOptionalDrop.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutOptionalDrop")));		
		addItem(pOptionalDrop, dropCommentTextField, 0, 0);
		
		// row 10
		JPanel pControl = new JPanel();
		pControl.setLayout(new BoxLayout(pControl, BoxLayout.Y_AXIS));	
		addItem(pControl, saveButton, 0, 0);

		// add panels
		getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
		getContentPane().add(pLoad);
		getContentPane().add(pPriority);
		getContentPane().add(pOptionalPickup);
		getContentPane().add(pOptionalDrop);
		getContentPane().add(pControl);
        
		addButtonAction(addButton);
        addButtonAction(deleteButton);
		addButtonAction(replaceButton);
		addButtonAction(saveButton);
		
		addComboBoxAction(comboBox);
		
		updateCarCommentFields();
 
		// build menu
		JMenuBar menuBar = new JMenuBar();
		JMenu toolMenu = new JMenu(rb.getString("Tools"));
		toolMenu.add(new CarLoadAttributeAction(rb.getString("CarQuanity"), this));
		menuBar.add(toolMenu);
		setJMenuBar(menuBar);
        // add help menu to window
		addHelpMenu("package.jmri.jmrit.operations.Operations_EditCarLoads", true);
		
    	pack();
    	if ((getWidth()<300)) 
    		setSize(getWidth()+50, getHeight()+10);
    	setVisible(true);
    }
 
	// add, delete, replace, and save buttons
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == addButton){
			String addLoad = addTextBox.getText();
			if (addLoad.equals(""))
				return;
			if (addLoad.length() > Control.MAX_LEN_STRING_ATTRIBUTE){
				JOptionPane.showMessageDialog(this, MessageFormat.format(rb.getString("carAttribute"),new Object[]{Control.MAX_LEN_STRING_ATTRIBUTE}),
						MessageFormat.format(rb.getString("canNotAdd"),new Object[]{rb.getString("Load")}),
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			addLoadToCombobox (_type, addLoad);
		}
		if (ae.getSource() == deleteButton){
			String deleteLoad = (String)comboBox.getSelectedItem();
			if (deleteLoad.equals(carLoads.getDefaultEmptyName()) || deleteLoad.equals(carLoads.getDefaultLoadName())){
				JOptionPane.showMessageDialog(this, rb.getString("carLoadDefault"),
						MessageFormat.format(rb.getString("canNotDelete"),new Object[]{rb.getString("Load")}),
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			deleteLoadFromCombobox (_type, deleteLoad);
		}
		if (ae.getSource() == replaceButton){
			String newLoad = addTextBox.getText();
			if (newLoad.equals(""))
				return;
			if (newLoad.length() > Control.MAX_LEN_STRING_ATTRIBUTE){
				JOptionPane.showMessageDialog(this, MessageFormat.format(rb.getString("carAttribute"),new Object[]{Control.MAX_LEN_STRING_ATTRIBUTE}),
						MessageFormat.format(rb.getString("canNotReplace"),new Object[]{rb.getString("Load")}),
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			String oldLoad = (String) comboBox.getSelectedItem();

			if (oldLoad.equals(carLoads.getDefaultEmptyName())){
				if (JOptionPane.showConfirmDialog(this,
						MessageFormat.format(rb.getString("replaceDefaultEmpty"),new Object[]{oldLoad, newLoad}),
						rb.getString("replaceAll"), JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
					return;
				}
				// don't allow the default names for load and empty to be the same
				if (newLoad.equals(carLoads.getDefaultEmptyName()) || newLoad.equals(carLoads.getDefaultLoadName())){
					JOptionPane.showMessageDialog(this, rb.getString("carDefault"),
							MessageFormat.format(rb.getString("canNotReplace"),new Object[]{rb.getString("Load")}),
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				carLoads.setDefaultEmptyName(newLoad);
				replaceAllLoads(oldLoad, newLoad);
				return;
			}
			if (oldLoad.equals(carLoads.getDefaultLoadName())){
				if (JOptionPane.showConfirmDialog(this,
						MessageFormat.format(rb.getString("replaceDefaultLoad"),new Object[]{oldLoad, newLoad}),
						rb.getString("replaceAll"), JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
					return;
				}
				// don't allow the default names for load and empty to be the same
				if (newLoad.equals(carLoads.getDefaultEmptyName()) || newLoad.equals(carLoads.getDefaultLoadName())){
					JOptionPane.showMessageDialog(this, rb.getString("carDefault"),
							MessageFormat.format(rb.getString("canNotReplace"),new Object[]{rb.getString("Load")}),
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				carLoads.setDefaultLoadName(newLoad);
				replaceAllLoads(oldLoad, newLoad);
				return;
			}
			if (JOptionPane.showConfirmDialog(this,
					MessageFormat.format(rb.getString("replaceMsg"),new Object[]{oldLoad, newLoad}),
					rb.getString("replaceAll"), JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
				return;
			}
			addLoadToCombobox (_type, newLoad);
			replaceLoad(_type, oldLoad, newLoad);
			deleteLoadFromCombobox (_type, oldLoad);
		}
		if (ae.getSource() == saveButton){
			log.debug("CarLoadEditFrame save button pressed");
			carLoads.setPriority(_type, (String)comboBox.getSelectedItem(), (String)priorityComboBox.getSelectedItem());
			carLoads.setPickupComment(_type, (String)comboBox.getSelectedItem(), pickupCommentTextField.getText());
			carLoads.setDropComment(_type, (String)comboBox.getSelectedItem(), dropCommentTextField.getText());
			CarManagerXml.instance().writeOperationsFile();
		}
	}
	
	protected void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
		log.debug("Combo box action");
		updateCarQuanity();
		updatePriority();
		updateCarCommentFields();
	}
	
	// replace the default empty and load for all car types
	private void replaceAllLoads (String oldLoad, String newLoad){
		String[] typeNames = CarTypes.instance().getNames();
		for (int i=0; i<typeNames.length; i++){
			addLoadToCombobox (typeNames[i], newLoad);
			replaceLoad(typeNames[i], oldLoad, newLoad);
			deleteLoadFromCombobox (typeNames[i], oldLoad);
		}
	}

	private void deleteLoadFromCombobox (String type, String name){
		carLoads.deleteName(type, name);
	}
	
	private void addLoadToCombobox (String type, String name){
		carLoads.addName(type, name);
	}
	
	private void replaceLoad(String type, String oldLoad, String newLoad) {
		// adjust all cars
		CarManager.instance().replaceLoad(type, oldLoad, newLoad);
		//	now adjust schedules
		ScheduleManager.instance().replaceLoad(type, oldLoad, newLoad);
		// now adjust trains
		TrainManager.instance().replaceLoad(oldLoad, newLoad);
		// now adjust tracks
		LocationManager.instance().replaceLoad(oldLoad, newLoad);
	}
	
	private void loadComboboxes(){ 
		comboBox = carLoads.getComboBox(_type);
		carLoads.addPropertyChangeListener(this);
		priorityComboBox = carLoads.getPriorityComboBox();
	}
	
	boolean showQuanity = false;
	public void toggleShowQuanity(){
		if (showQuanity)
			showQuanity = false;		
		else
			showQuanity = true;
		quanity.setVisible(showQuanity);
		updateCarQuanity();
	}
	
	private void updateCarQuanity(){
		if(!showQuanity)
			return;
		int number = 0;
		String item = (String)comboBox.getSelectedItem();
		CarManager manager = CarManager.instance();
		List<String> cars = manager.getByIdList();
		for (int i=0; i<cars.size(); i++){
			Car car = manager.getById(cars.get(i));
			if (car.getLoad().equals(item))
				number++;
		}
		quanity.setText(Integer.toString(number));
	}
	
	private void updatePriority(){
		priorityComboBox.setSelectedItem(carLoads.getPriority(_type, (String)comboBox.getSelectedItem()));
	}
	
	private void updateCarCommentFields(){
		pickupCommentTextField.setText(carLoads.getPickupComment(_type, (String)comboBox.getSelectedItem()));
		dropCommentTextField.setText(carLoads.getDropComment(_type, (String)comboBox.getSelectedItem()));
	}

    public void dispose() {
    	carLoads.removePropertyChangeListener(this);
        super.dispose();
    }
    
	public void propertyChange(java.beans.PropertyChangeEvent e) {
		log.debug ("CarsLoadEditFrame sees propertyChange "+e.getPropertyName()+" "+e.getNewValue());
		if (e.getPropertyName().equals(CarLoads.LOAD_CHANGED_PROPERTY))
			carLoads.updateComboBox(_type, comboBox);
	}
	
	java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(
			this);

	public synchronized void addPropertyChangeListener(
			java.beans.PropertyChangeListener l) {
		pcs.addPropertyChangeListener(l);
	}

	public synchronized void removePropertyChangeListener(
			java.beans.PropertyChangeListener l) {
		pcs.removePropertyChangeListener(l);
	}
    
	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(CarLoadEditFrame.class.getName());
}

final class CarLoadAttributeAction extends AbstractAction {	
    public CarLoadAttributeAction(String actionName, CarLoadEditFrame clef) {
        super(actionName);
        this.clef = clef;
    }
    
    CarLoadEditFrame clef;
    
    public void actionPerformed(ActionEvent ae) {
    	log.debug("Show attribute quanity");
    	clef.toggleShowQuanity();
    }
	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(CarAttributeEditFrame.class.getName());
}
