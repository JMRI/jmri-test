// CarLoadEditFrame.java

 package jmri.jmrit.operations.rollingstock.cars;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.locations.ScheduleManager;



/**
 * Frame for adding and editing the car roster for operations.
 *
 * @author Daniel Boudreau Copyright (C) 2009
 * @version             $Revision: 1.6 $
 */
public class CarLoadEditFrame extends OperationsFrame implements java.beans.PropertyChangeListener{
	
	final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.rollingstock.cars.JmritOperationsCarsBundle");
	
	CarLoads carLoads = CarLoads.instance();
	
	// labels
	JLabel textLoad = new JLabel();
	JLabel textSep = new JLabel();
	JLabel quanity = new JLabel("0");

	// major buttons
	JButton addButton = new JButton();
	JButton deleteButton = new JButton();
	JButton replaceButton = new JButton();
	
	// combo box
	JComboBox comboBox;
	
	// text box
	JTextField addTextBox = new JTextField(10);
	
    public CarLoadEditFrame() {}
    
    String _type;
    boolean menuActive = false;
    
    public void initComponents(String type) {
    	
    	getContentPane().removeAll();
     	
        setTitle(MessageFormat.format(rb.getString("TitleCarEditLoad"),new Object[]{type}));
        
        // track which combo box is being edited 
        _type = type;
        loadCombobox();
        
        // general GUI config
        getContentPane().setLayout(new GridBagLayout());
        
        textLoad.setText(rb.getString("Load"));

		addButton.setText(rb.getString("Add"));
		addButton.setVisible(true);
        deleteButton.setText(rb.getString("Delete"));
		deleteButton.setVisible(true);
        replaceButton.setText(rb.getString("Replace"));
        replaceButton.setVisible(true);
        
        quanity.setVisible(showQuanity);
        
		// row 1
		addItem(textLoad,2,1);
		// row 2
		addItem(addTextBox, 2, 2);
        addItem(addButton, 3, 2);
        
        // row 3
        addItem(quanity, 1, 3);
        addItem(comboBox, 2, 3);
        addItem(deleteButton, 3, 3);
        
        // row 4 
        addItem(replaceButton, 3, 4);
        
		addButtonAction(addButton);
        addButtonAction(deleteButton);
		addButtonAction(replaceButton);
		
		addComboBoxAction(comboBox);
 
		// build menu
		JMenuBar menuBar = new JMenuBar();
		JMenu toolMenu = new JMenu("Tools");
		toolMenu.add(new CarLoadAttributeAction(rb.getString("CarQuanity"), this));
		menuBar.add(toolMenu);
		setJMenuBar(menuBar);
        // add help menu to window
		addHelpMenu("package.jmri.jmrit.operations.Operations_Cars", true);
		
    	pack();
    	if ((getWidth()<200)) 
    		setSize(250, getHeight()+10);
    	else
    		setSize(getWidth()+50, getHeight()+10);
    	setVisible(true);
    }
 
	// add or delete button
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
	}
	
	protected void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
		log.debug("Combo box action");
		updateCarQuanity();
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
		CarManager manager = CarManager.instance();
		List<String> cars = manager.getCarsByIdList();
		for (int i = 0; i < cars.size(); i++) {
			Car car = manager.getCarById(cars.get(i));
			if (car.getType().equals(type) && car.getLoad().equals(oldLoad))
				car.setLoad(newLoad);
		}
		//	now adjust schedules
		ScheduleManager.instance().replaceLoad(type, oldLoad, newLoad);
	}
	
	private void loadCombobox(){ 
		comboBox = carLoads.getComboBox(_type);
		carLoads.addPropertyChangeListener(this);
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
		List<String> cars = manager.getCarsByIdList();
		for (int i=0; i<cars.size(); i++){
			Car car = manager.getCarById(cars.get(i));
			if (car.getLoad().equals(item))
				number++;
		}
		quanity.setText(Integer.toString(number));
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
