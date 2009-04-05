// EngineAttributeEditFrame.java

 package jmri.jmrit.operations.rollingstock.engines;

import java.awt.GridBagLayout;
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;

import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.rollingstock.cars.CarOwners;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;

/**
 * Frame for adding and editing the engine roster for operations.
 *
 * @author Daniel Boudreau Copyright (C) 2008
 * @version             $Revision: 1.18 $
 */
public class EngineAttributeEditFrame extends OperationsFrame implements java.beans.PropertyChangeListener{
	
	final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.rollingstock.engines.JmritOperationsEnginesBundle");
	
	EngineManager manager = EngineManager.instance();
	
	// labels
	javax.swing.JLabel textAttribute = new javax.swing.JLabel();
	javax.swing.JLabel textSep = new javax.swing.JLabel();

	// major buttons
	javax.swing.JButton addButton = new javax.swing.JButton();
	javax.swing.JButton deleteButton = new javax.swing.JButton();
	javax.swing.JButton replaceButton = new javax.swing.JButton();
	
	// combo box
	javax.swing.JComboBox comboBox;
	
	// text box
	javax.swing.JTextField addTextBox = new javax.swing.JTextField(10);

    public EngineAttributeEditFrame() {}
    
    String _comboboxName;		// track which combo box is being edited
    boolean menuActive = false;
    
    public void initComponents(String comboboxName) {
    	
    	getContentPane().removeAll();
     	
    	setTitle(MessageFormat.format(rb.getString("TitleEngineEditAtrribute"),new Object[]{comboboxName}));
        
        // track which combo box is being edited 
        _comboboxName = comboboxName;
        loadCombobox();
        
        // general GUI config
        getContentPane().setLayout(new GridBagLayout());
        
        textAttribute.setText(comboboxName);

		addButton.setText(rb.getString("Add"));
		addButton.setVisible(true);
        deleteButton.setText(rb.getString("Delete"));
		deleteButton.setVisible(true);
        replaceButton.setText(rb.getString("Replace"));
        replaceButton.setVisible(true);
        
		// row 1
		addItem(textAttribute,1,1);
		// row 2
		addItem(addTextBox, 1, 2);
        addItem(addButton, 2, 2);
        
        // row 3
        addItem(comboBox, 1, 3);
        addItem(deleteButton, 2, 3);
        
        // row 4 
        addItem(replaceButton, 2, 4);
        
		addButtonAction(addButton);
        addButtonAction(deleteButton);
		addButtonAction(replaceButton);
 
        // add help menu to window
		addHelpMenu("package.jmri.jmrit.operations.Operations_Engines", true);
		
    	pack();
    	if ((getWidth()<150)) 
    		setSize(200, getHeight()+10);
    	else
    		setSize(getWidth()+50, getHeight()+10);
    	setVisible(true);
    }
 
	// add or delete button
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		log.debug("edit frame button actived");
		if (ae.getSource() == addButton){
			String addItem = addTextBox.getText();
			if (addItem.equals(""))
					return;
			if (addItem.length() > Control.MAX_LEN_STRING_ATTRIBUTE){
				JOptionPane.showMessageDialog(this, MessageFormat.format(rb.getString("engineAttribute"),new Object[]{Control.MAX_LEN_STRING_ATTRIBUTE}),
						MessageFormat.format(rb.getString("canNotAdd"),new Object[]{_comboboxName}),
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			addItemToCombobox (addItem);
		}
		if (ae.getSource() == deleteButton){
			String deleteItem = (String)comboBox.getSelectedItem();
			deleteItemFromCombobox (deleteItem);
		}
		if (ae.getSource() == replaceButton){
			String newItem = addTextBox.getText();
			if (newItem.equals(""))
				return;
			if (newItem.length() > Control.MAX_LEN_STRING_ATTRIBUTE){
				JOptionPane.showMessageDialog(this, MessageFormat.format(rb.getString("engineAttribute"),new Object[]{Control.MAX_LEN_STRING_ATTRIBUTE}),
						MessageFormat.format(rb.getString("canNotReplace"),new Object[]{_comboboxName}),
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			String oldItem = (String) comboBox.getSelectedItem();
			if (JOptionPane.showConfirmDialog(this,
					MessageFormat.format(rb.getString("replaceMsg"),new Object[]{oldItem, newItem}),
					rb.getString("replaceAll"), JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
				return;
			}
			
			addItemToCombobox (newItem);
			replaceItem(oldItem, newItem);
			deleteItemFromCombobox (oldItem);
		}
	}

	private void deleteItemFromCombobox (String deleteItem){
		if(_comboboxName == EnginesEditFrame.ROAD){
			CarRoads.instance().replaceName(deleteItem, null);
		}
		if(_comboboxName == EnginesEditFrame.MODEL){
			EngineModels.instance().deleteName(deleteItem);
		}
		if(_comboboxName == EnginesEditFrame.TYPE){
			EngineTypes.instance().deleteName(deleteItem);
		}
		if(_comboboxName == EnginesEditFrame.LENGTH){
			EngineLengths.instance().deleteName(deleteItem);
		}
		if(_comboboxName == EnginesEditFrame.OWNER){
			CarOwners.instance().deleteName(deleteItem);
		}
		if(_comboboxName == EnginesEditFrame.CONSIST){
			manager.deleteConsist(deleteItem);
		}
	}
	
	private void addItemToCombobox (String addItem){
		if(_comboboxName == EnginesEditFrame.ROAD){
			CarRoads.instance().addName(addItem);
		}
		if(_comboboxName == EnginesEditFrame.MODEL){
			EngineModels.instance().addName(addItem);
		}
		if(_comboboxName == EnginesEditFrame.TYPE){
			EngineTypes.instance().addName(addItem);
		}
		if(_comboboxName == EnginesEditFrame.LENGTH){
			// convert from inches to feet if needed
			if (addItem.endsWith("\"")){
				addItem = addItem.substring(0, addItem.length()-1);
				try {
					double inches = Double.parseDouble(addItem);
					int feet = (int)(inches * Setup.getScaleRatio() / 12);
					addItem = Integer.toString(feet);
				} catch (NumberFormatException e){
					log.error("can not convert from inches to feet");
					JOptionPane.showMessageDialog(this,
							rb.getString("CanNotConvertFeet"), rb.getString("ErrorEngineLength"),
							JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			if (addItem.endsWith("cm")){
				addItem = addItem.substring(0, addItem.length()-2);
				try {
					double cm = Double.parseDouble(addItem);
					int meter = (int)(cm * Setup.getScaleRatio() / 100);
					addItem = Integer.toString(meter);
				} catch (NumberFormatException e){
					log.error("Can not convert from cm to meters");
					JOptionPane.showMessageDialog(this,
							rb.getString("CanNotConvertMeter"), rb.getString("ErrorEngineLength"),
							JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			// confirm that length is a number and less than 10000 feet
			try {
				int engineLength = Integer.parseInt(addItem);
				if (engineLength > 9999){
					log.error("engine length must be less than 10,000 feet");
					JOptionPane.showMessageDialog(this,rb.getString("engineAttribute5"),
							MessageFormat.format(rb.getString("canNotAdd"),new Object[]{_comboboxName}),
							JOptionPane.ERROR_MESSAGE);
					return;
				}
			} catch (NumberFormatException e){
				log.error("length not an integer");
				return;
			}
			EngineLengths.instance().addName(addItem);
			comboBox.setSelectedItem(addItem);
		}
		if(_comboboxName == EnginesEditFrame.CONSIST){
			manager.newConsist(addItem);
		}
		if(_comboboxName == EnginesEditFrame.OWNER){
			CarOwners.instance().addName(addItem);
		}
	}
	
	private void replaceItem (String oldItem, String newItem){
		List<String> engines = manager.getEnginesByNumberList();
		for (int i=0; i<engines.size(); i++){
			Engine engine = manager.getEngineById(engines.get(i));

			if(_comboboxName == EnginesEditFrame.ROAD){
				if (engine.getRoad().equals(oldItem))
					engine.setRoad(newItem);
			}
			if(_comboboxName == EnginesEditFrame.MODEL){
				if (engine.getModel().equals(oldItem))
					engine.setModel(newItem);
			}
			if(_comboboxName == EnginesEditFrame.TYPE){
				if (engine.getType().equals(oldItem))
					engine.setType(newItem);
			}
			if(_comboboxName == EnginesEditFrame.LENGTH){
				if (engine.getLength().equals(oldItem))
					engine.setLength(newItem);
			}
			if(_comboboxName == EnginesEditFrame.OWNER){
				if (engine.getOwner().equals(oldItem))
					engine.setOwner(newItem);
			}
			if(_comboboxName == EnginesEditFrame.CONSIST){
				if (engine.getConsist() != null && engine.getConsistName().equals(oldItem)){
					Consist consist = manager.newConsist(newItem);
					engine.setConsist(consist);
				}
			}
		}
		//	now adjust locations and trains
		if(_comboboxName == EnginesEditFrame.TYPE){
			EngineTypes.instance().replaceName(oldItem, newItem);
		}
		if(_comboboxName == EnginesEditFrame.ROAD){
			CarRoads.instance().replaceName(oldItem, newItem);
		}
	}
	
	private void loadCombobox(){ 
		if(_comboboxName == EnginesEditFrame.ROAD){
			comboBox = CarRoads.instance().getComboBox();
			CarRoads.instance().addPropertyChangeListener(this);
		}
		if(_comboboxName == EnginesEditFrame.MODEL){
			comboBox = EngineModels.instance().getComboBox();
			EngineModels.instance().addPropertyChangeListener(this);
		}
		if(_comboboxName == EnginesEditFrame.TYPE){
			comboBox = EngineTypes.instance().getComboBox();
			EngineTypes.instance().addPropertyChangeListener(this);
		}
		if(_comboboxName == EnginesEditFrame.LENGTH){
			comboBox = EngineLengths.instance().getComboBox();
			EngineLengths.instance().addPropertyChangeListener(this);
		}
		if(_comboboxName == EnginesEditFrame.OWNER){
			comboBox = CarOwners.instance().getComboBox();
			CarOwners.instance().addPropertyChangeListener(this);
		}
		if(_comboboxName == EnginesEditFrame.CONSIST){
			comboBox = manager.getConsistComboBox();
			manager.addPropertyChangeListener(this);
		}
	}

    public void dispose() {
    	CarRoads.instance().removePropertyChangeListener(this);
    	EngineModels.instance().removePropertyChangeListener(this);
    	EngineTypes.instance().removePropertyChangeListener(this);
		EngineLengths.instance().removePropertyChangeListener(this);
    	CarOwners.instance().removePropertyChangeListener(this);
    	manager.removePropertyChangeListener(this);
		firePcs ("dispose", null, _comboboxName);
        super.dispose();
    }

	public void propertyChange(java.beans.PropertyChangeEvent e) {
		log.debug ("EnginesAttributeFrame sees propertyChange "+e.getPropertyName()+" "+e.getNewValue());
		if (e.getPropertyName().equals(CarRoads.CARROADS_LENGTH_CHANGED_PROPERTY))
			CarRoads.instance().updateComboBox(comboBox);
		if (e.getPropertyName().equals(EngineModels.ENGINEMODELS_CHANGED_PROPERTY))
			EngineModels.instance().updateComboBox(comboBox);
		if (e.getPropertyName().equals(EngineTypes.ENGINETYPES_LENGTH_CHANGED_PROPERTY))
			EngineTypes.instance().updateComboBox(comboBox);
		if (e.getPropertyName().equals(EngineLengths.ENGINELENGTHS_CHANGED_PROPERTY))
			EngineLengths.instance().updateComboBox(comboBox);
		if (e.getPropertyName().equals(CarOwners.CAROWNERS_CHANGED_PROPERTY))
			CarOwners.instance().updateComboBox(comboBox);
		if (e.getPropertyName().equals(EngineManager.CONSISTLISTLENGTH_CHANGED_PROPERTY))
			manager.updateConsistComboBox(comboBox);
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

	//	note firePropertyChange occurs during frame creation
	private void firePcs(String p, Object old, Object n) {
		log.debug("EngineAttribute firePropertyChange " + p +" " );
		pcs.firePropertyChange(p, old, n);
	}
    
	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(EngineAttributeEditFrame.class.getName());
}
