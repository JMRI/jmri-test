// CarsSetFrame.java

package jmri.jmrit.operations.rollingstock.cars;

import javax.swing.JOptionPane;
import javax.swing.JTable;


/**
 * Frame for user to place a group of cars on the layout
 * 
 * @author Dan Boudreau Copyright (C) 2011
 * @version $Revision$
 */

public class CarsSetFrame extends CarSetFrame implements java.beans.PropertyChangeListener {
	
	CarsTableModel _carsTableModel;
	JTable _carsTable;
		
	public CarsSetFrame() {
		super();
	}
	
	//Ignore checkbox states
	private static boolean ignoreStatusCheckBoxSelected = false;
	private static boolean ignoreLocationCheckBoxSelected = false;
	private static boolean ignoreRWECheckBoxSelected = false;
	private static boolean ignoreLoadCheckBoxSelected = false;
	private static boolean ignoreDestinationCheckBoxSelected = false;
	private static boolean ignoreFinalDestinationCheckBoxSelected = false;
	private static boolean ignoreTrainCheckBoxSelected = false;

	public void initComponents(CarsTableModel carsTableModel, JTable carsTable){
		_carsTableModel = carsTableModel;
		_carsTable = carsTable;
		
		super.initComponents();
		
		// modify Save button text to "Change"
		saveButton.setText(rb.getString("Change"));
		// disable edit load button if no cars selected
		editLoadButton.setEnabled(false);
		// show ignore checkboxes
		ignoreStatusCheckBox.setVisible(true);
		ignoreLocationCheckBox.setVisible(true);
		ignoreRWECheckBox.setVisible(true);
		ignoreLoadCheckBox.setVisible(true);
		ignoreDestinationCheckBox.setVisible(true);
		ignoreFinalDestinationCheckBox.setVisible(true);
		ignoreTrainCheckBox.setVisible(true);
	
		// set the last state
		ignoreStatusCheckBox.setSelected(ignoreStatusCheckBoxSelected);
		ignoreLocationCheckBox.setSelected(ignoreLocationCheckBoxSelected);
		ignoreRWECheckBox.setSelected(ignoreRWECheckBoxSelected);
		ignoreLoadCheckBox.setSelected(ignoreLoadCheckBoxSelected);
		ignoreDestinationCheckBox.setSelected(ignoreDestinationCheckBoxSelected);
		ignoreFinalDestinationCheckBox.setSelected(ignoreFinalDestinationCheckBoxSelected);
		ignoreTrainCheckBox.setSelected(ignoreTrainCheckBoxSelected);
		
		Car car;
		int rows[] = _carsTable.getSelectedRows();
		if (rows.length >0)
			car = _carsTableModel.getCarAtIndex(rows[0]);
		else
			return;		
		super.loadCar(car);
	}
	
	@edu.umd.cs.findbugs.annotations.SuppressWarnings(value="ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
	protected boolean save(){
		// save ignore states
		ignoreStatusCheckBoxSelected = ignoreStatusCheckBox.isSelected();
		ignoreLocationCheckBoxSelected = ignoreLocationCheckBox.isSelected();
		ignoreRWECheckBoxSelected = ignoreRWECheckBox.isSelected();
		ignoreLoadCheckBoxSelected = ignoreLoadCheckBox.isSelected();
		ignoreDestinationCheckBoxSelected = ignoreDestinationCheckBox.isSelected();
		ignoreFinalDestinationCheckBoxSelected = ignoreFinalDestinationCheckBox.isSelected();
		ignoreTrainCheckBoxSelected = ignoreTrainCheckBox.isSelected();

		int rows[] = _carsTable.getSelectedRows();
		if (rows.length == 0)					
			JOptionPane.showMessageDialog(this,
				rb.getString("selectCars"),
				rb.getString("carNoneSelected"),
				JOptionPane.WARNING_MESSAGE);
			
		for (int i=0; i<rows.length; i++){
			Car car = _carsTableModel.getCarAtIndex(rows[i]);
			if (!super.change(car))
				return false;
		}
		return true;
	}


	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(CarsSetFrame.class.getName());
}
