//CarEditFrame.java

package jmri.jmrit.operations.rollingstock.cars;

import java.awt.GridBagLayout;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;


/**
 * Frame for user edit of car
 * 
 * @author Dan Boudreau Copyright (C) 2008
 * @version $Revision: 1.3 $
 */

public class CarEditFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.rollingstock.cars.JmritOperationsCarsBundle");

	CarManager manager = CarManager.instance();
	CarManagerXml managerXml = CarManagerXml.instance();
	LocationManager locationManager = LocationManager.instance();

	Car _car;

	// labels
	JLabel textRoad = new JLabel(rb.getString("Road"));
	JLabel textRoadNumber = new JLabel(rb.getString("RoadNumber"));
	JLabel textColor = new JLabel(rb.getString("Color"));
	JLabel textBuilt = new JLabel(rb.getString("BuildDate"));
	JLabel textLength = new JLabel(rb.getString("Length"));
	JLabel textType = new JLabel(rb.getString("Type"));
	JLabel textWeight = new JLabel(rb.getString("Weight"));
	JLabel textWeightTons = new JLabel(rb.getString("WeightTons"));
	JLabel textLocation = new JLabel(rb.getString("Location"));
	JLabel textLoad = new JLabel(rb.getString("Load"));
	JLabel textKernel = new JLabel(rb.getString("Kernel"));
	JLabel textOwner = new JLabel(rb.getString("Owner"));
	JLabel textComment = new JLabel(rb.getString("Comment"));
	JLabel textRfid = new JLabel(rb.getString("Rfid"));

	// major buttons
	JButton editRoadButton = new JButton(rb.getString("Edit"));
	JButton clearRoadNumberButton = new JButton(rb.getString("Clear"));
	JButton editTypeButton = new JButton(rb.getString("Edit"));
	JButton editColorButton = new JButton(rb.getString("Edit"));
	JButton editLengthButton = new JButton(rb.getString("Edit"));
	JButton fillWeightButton = new JButton(rb.getString("Calculate"));
	JButton editLoadButton = new JButton(rb.getString("Edit"));
	JButton editKernelButton = new JButton(rb.getString("Edit"));
	JButton editOwnerButton = new JButton(rb.getString("Edit"));

	JButton saveButton = new JButton(rb.getString("Save"));
	JButton deleteButton = new JButton(rb.getString("Delete"));
	JButton addButton = new JButton(rb.getString("Add"));

	// check boxes
	JCheckBox autoCheckBox = new JCheckBox(rb.getString("Auto"));
	JCheckBox cabooseCheckBox = new JCheckBox(rb.getString("Caboose"));
	JCheckBox fredCheckBox = new JCheckBox(rb.getString("Fred"));
	JCheckBox hazardousCheckBox = new JCheckBox(rb.getString("Hazardous"));

	// text field
	JTextField roadNumberTextField = new JTextField(8);
	JTextField builtTextField = new JTextField(8);
	JTextField weightTextField = new JTextField(4);
	JTextField weightTonsTextField = new JTextField(4);
	JTextField commentTextField = new JTextField(35);
	JTextField rfidTextField = new JTextField(16);

	// combo boxes
	JComboBox roadComboBox = CarRoads.instance().getComboBox();
	JComboBox typeComboBox = CarTypes.instance().getComboBox();
	JComboBox colorComboBox = CarColors.instance().getComboBox();
	JComboBox lengthComboBox = CarLengths.instance().getComboBox();
	JComboBox ownerComboBox = CarOwners.instance().getComboBox();
	JComboBox locationBox = locationManager.getComboBox();
	JComboBox trackLocationBox = new JComboBox();
	JComboBox loadComboBox = CarLoads.instance().getComboBox(null);
	JComboBox kernelComboBox = manager.getKernelComboBox(); 
	
 
	public static final String ROAD = rb.getString("Road");
	public static final String TYPE = rb.getString("Type");
	public static final String COLOR = rb.getString("Color");
	public static final String LENGTH = rb.getString("Length");
	public static final String OWNER = rb.getString("Owner");
	public static final String KERNEL = rb.getString("Kernel");
	public static final String DISPOSE = "dispose" ;

	public CarEditFrame() {
		super();
	}

	public void initComponents() {
		// the following code sets the frame's initial state

		// load tool tips
		weightTextField.setToolTipText(rb.getString("TipCarWeightOz"));
		weightTonsTextField.setToolTipText(rb.getString("TipCarWeightTons"));
		autoCheckBox.setToolTipText(rb.getString("TipCarAutoCalculate"));
		fredCheckBox.setToolTipText(rb.getString("TipCarFred"));
		fillWeightButton.setToolTipText(rb.getString("TipCalculateCarWeight"));
		builtTextField.setToolTipText(rb.getString("TipBuildDate"));
		rfidTextField.setToolTipText(rb.getString("TipRfid"));
		
		// default check box selections
		autoCheckBox.setSelected(true);
		cabooseCheckBox.setSelected(false);
		fredCheckBox.setSelected(false);	
		hazardousCheckBox.setSelected(false);

		// create panel
		JPanel pPanel = new JPanel();
		pPanel.setLayout(new GridBagLayout());

		// Layout the panel by rows
		// row 1
		addItem(pPanel, textRoad, 0, 1);
		addItem(pPanel, roadComboBox, 1, 1);
		addItem(pPanel, editRoadButton, 2, 1);
		// row 2
		addItem(pPanel, textRoadNumber, 0, 2);
		addItem(pPanel, roadNumberTextField, 1, 2);
		addItem(pPanel, clearRoadNumberButton, 2, 2);
		// row 3
		addItem(pPanel, textType, 0, 3);
		addItem(pPanel, typeComboBox, 1, 3);
		addItem(pPanel, editTypeButton, 2, 3);
		// row 4
		addItem(pPanel, textLength, 0, 4);
		addItem(pPanel, lengthComboBox, 1, 4);
		addItem(pPanel, editLengthButton, 2, 4);
		// row 5
		addItem(pPanel, textColor, 0, 5);
		addItem(pPanel, colorComboBox, 1, 5);
		addItem(pPanel, editColorButton, 2, 5);

		// row 7
		addItem(pPanel, textWeight, 0, 7);
		addItem(pPanel, weightTextField, 1, 7);
		addItem(pPanel, fillWeightButton, 2, 7);
		addItem(pPanel, autoCheckBox, 3, 7);
		
		// row 8
		addItem(pPanel, textWeightTons, 0, 8);
		addItem(pPanel, weightTonsTextField, 1, 8);
		
		// row 10
		addItem(pPanel, cabooseCheckBox, 1, 10);
		addItem(pPanel, fredCheckBox, 2, 10);
		addItem(pPanel, hazardousCheckBox, 3, 10);

		// row 11
		addItem(pPanel, textLocation, 0, 11);
		addItem(pPanel, locationBox, 1, 11);
		addItemWidth(pPanel, trackLocationBox, 2, 2, 11);

		// optional panel
		JPanel pOptional = new JPanel();
		pOptional.setLayout(new GridBagLayout());
		pOptional.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutOptional")));
		
		// row 13
		addItem(pOptional, textLoad, 0, 13);
		addItem(pOptional, loadComboBox, 1, 13);
		addItem(pOptional, editLoadButton, 2, 13);
		
		// row 15
		addItem(pOptional, textKernel, 0, 15);
		addItem(pOptional, kernelComboBox, 1, 15);
		addItem(pOptional, editKernelButton, 2, 15);

		// row 17
		addItem(pOptional, textBuilt, 0, 17);
		addItem(pOptional, builtTextField, 1, 17);
		
		// row 19
		addItem(pOptional, textOwner, 0, 19);
		addItem(pOptional, ownerComboBox, 1, 19);
		addItem(pOptional, editOwnerButton, 2, 19);

		// row 20
		if(Setup.isRfidEnabled()){
			addItem(pOptional, textRfid, 0, 20);
			addItem(pOptional, rfidTextField, 1, 20);
		}
		
		// row 21
		addItem(pOptional, textComment, 0, 21);
		addItemWidth(pOptional, commentTextField, 2, 1, 21);
				
		// button panel
		JPanel pButtons = new JPanel();
		pButtons.setLayout(new GridBagLayout());
		addItem(pButtons, deleteButton, 0, 25);
		addItem(pButtons, addButton, 1, 25);
		addItem(pButtons, saveButton, 3, 25);
		
		// add panels
		getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
		getContentPane().add(pPanel);
		getContentPane().add(pOptional);
		getContentPane().add(pButtons);

		// setup buttons
		addEditButtonAction(editRoadButton);
		addButtonAction(clearRoadNumberButton);
		addEditButtonAction(editTypeButton);
		addEditButtonAction(editLengthButton);
		addEditButtonAction(editColorButton);
		addEditButtonAction(editKernelButton);
		addEditButtonAction(editOwnerButton);

		addButtonAction(deleteButton);
		addButtonAction(addButton);
		addButtonAction(saveButton);
		addButtonAction(fillWeightButton);
		addButtonAction(editLoadButton);

		// setup combobox
		addComboBoxAction(typeComboBox);
		addComboBoxAction(lengthComboBox);
		addComboBoxAction(locationBox);
		
		// setup checkbox
		addCheckBoxAction(cabooseCheckBox);
		addCheckBoxAction(fredCheckBox);
		
		// build menu
//		JMenuBar menuBar = new JMenuBar();
//		JMenu toolMenu = new JMenu("Tools");
//		menuBar.add(toolMenu);
//		setJMenuBar(menuBar);
		addHelpMenu("package.jmri.jmrit.operations.Operations_Cars", true);

		//	 get notified if combo box gets modified
		CarRoads.instance().addPropertyChangeListener(this);
		CarLoads.instance().addPropertyChangeListener(this);
		CarTypes.instance().addPropertyChangeListener(this);
		CarLengths.instance().addPropertyChangeListener(this);
		CarColors.instance().addPropertyChangeListener(this);
		CarOwners.instance().addPropertyChangeListener(this);
		locationManager.addPropertyChangeListener(this);
		manager.addPropertyChangeListener(this);

		// set frame size and location for display
		pack();
		if ( (getWidth()<400)) 
			setSize(450, getHeight()+20);
		else
			setSize(getWidth()+50, getHeight()+20);
		setLocation(Control.panelX, Control.panelY);
		setVisible(true);	
	}

	public void loadCar(Car car){
		_car = car;

		if (!CarRoads.instance().containsName(car.getRoad())){
			if (JOptionPane.showConfirmDialog(this,
					MessageFormat.format(rb.getString("roadNameNotExist"),new Object[]{car.getRoad()}),
					rb.getString("carAddRoad"),
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
				CarRoads.instance().addName(car.getRoad());
			}
		}
		roadComboBox.setSelectedItem(car.getRoad());

		roadNumberTextField.setText(car.getNumber());

		if (!CarTypes.instance().containsName(car.getType())){
			if (JOptionPane.showConfirmDialog(this,
					MessageFormat.format(rb.getString("typeNameNotExist"),new Object[]{car.getType()}),
					rb.getString("carAddType"),
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
				CarTypes.instance().addName(car.getType());
			}
		}
		typeComboBox.setSelectedItem(car.getType());

		if (!CarLengths.instance().containsName(car.getLength())){
			if (JOptionPane.showConfirmDialog(this,
					MessageFormat.format(rb.getString("lengthNameNotExist"),new Object[]{car.getLength()}),
					rb.getString("carAddLength"),
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
				CarLengths.instance().addName(car.getLength());
			}
		}
		lengthComboBox.setSelectedItem(car.getLength());

		if (!CarColors.instance().containsName(car.getColor())){
			if (JOptionPane.showConfirmDialog(this,
					MessageFormat.format(rb.getString("colorNameNotExist"),new Object[]{car.getColor()}),
					rb.getString("carAddColor"),
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
				CarColors.instance().addName(car.getColor());
			}
		}
		colorComboBox.setSelectedItem(car.getColor());
		weightTextField.setText(car.getWeight());
		weightTonsTextField.setText(car.getWeightTons());
		cabooseCheckBox.setSelected(car.isCaboose());
		fredCheckBox.setSelected(car.hasFred());
		hazardousCheckBox.setSelected(car.isHazardous());

		locationBox.setSelectedItem(car.getLocation());
		Location l = locationManager.getLocationById(car.getLocationId());
		if (l != null){
			l.updateComboBox(trackLocationBox);
			trackLocationBox.setSelectedItem(car.getTrack());
		} else {
			trackLocationBox.removeAllItems();
		}

		builtTextField.setText(car.getBuilt());

		if (!CarOwners.instance().containsName(car.getOwner())){
			if (JOptionPane.showConfirmDialog(this,
					MessageFormat.format(rb.getString("ownerNameNotExist"),new Object[]{car.getOwner()}),
					rb.getString("addOwner"),
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
				CarOwners.instance().addName(car.getOwner());
			}
		}
		ownerComboBox.setSelectedItem(car.getOwner());
		
		if (!CarLoads.instance().containsName(car.getType(), car.getLoad())){
			if (JOptionPane.showConfirmDialog(this,
					MessageFormat.format(rb.getString("loadNameNotExist"),new Object[]{car.getLoad()}),
					rb.getString("addLoad"),
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
				CarLoads.instance().addName(car.getType(), car.getLoad());
			}
		}
		// listen for changes in car load
		car.addPropertyChangeListener(this);
		CarLoads.instance().updateComboBox(car.getType(), loadComboBox);
		loadComboBox.setSelectedItem(car.getLoad());
		
		kernelComboBox.setSelectedItem(car.getKernelName());	

		commentTextField.setText(car.getComment());
		rfidTextField.setText(car.getRfid());
	}

	// combo boxes
	public void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource()== typeComboBox && typeComboBox.getSelectedItem() != null){
			log.debug("Type comboBox sees change, update car loads");
			CarLoads.instance().updateComboBox((String)typeComboBox.getSelectedItem(), loadComboBox);
		}
		if (ae.getSource()== locationBox){
			if (locationBox.getSelectedItem() != null){
				if (locationBox.getSelectedItem().equals("")){
					trackLocationBox.removeAllItems();
				}else{
					log.debug("CarsSetFrame sees location: "+ locationBox.getSelectedItem());
					Location l = ((Location)locationBox.getSelectedItem());
					l.updateComboBox(trackLocationBox);
				}
			}
		}
		if (ae.getSource() == lengthComboBox && autoCheckBox.isSelected()){
			calculateWeight();
		}
	}
	
	public void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
		JCheckBox b =  (JCheckBox)ae.getSource();
		log.debug("checkbox change "+ b.getText());
		if (ae.getSource() == cabooseCheckBox && cabooseCheckBox.isSelected()){
			fredCheckBox.setSelected(false);
		}
		if (ae.getSource() == fredCheckBox && fredCheckBox.isSelected()){
			cabooseCheckBox.setSelected(false);
		}
	}

	// Save, Delete, Add, Clear, Calculate, Edit Load buttons
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == saveButton){
			// log.debug("car save button actived");
			if (!checkCar(_car))
				return;
			// delete car if edit and road or road number has changed
			if (_car != null){
				if (_car.getRoad() != null && !_car.getRoad().equals("")){
					if (!_car.getRoad().equals(roadComboBox.getSelectedItem().toString())
							|| !_car.getNumber().equals(roadNumberTextField.getText())) {
						// transfer car attributes since road name and number have changed
						Car oldCar = _car;
						Car newCar = addCar();
						// set the car's destination and train
						newCar.setDestination(oldCar.getDestination(), oldCar.getDestinationTrack());
						newCar.setTrain(oldCar.getTrain());
						oldCar.removePropertyChangeListener(this);
						manager.deregister(oldCar);
						managerXml.writeOperationsCarFile();
						return;
					}
				}
			}
			addCar ();
			// save car file
			managerXml.writeOperationsCarFile();
		}
		if (ae.getSource() == deleteButton){
			log.debug("car delete button actived");
			if (_car != null
					&& _car.getRoad().equals(roadComboBox.getSelectedItem().toString())
					&& _car.getNumber().equals(roadNumberTextField.getText())) {
				manager.deregister(_car);
				_car = null;
				// save car file
				managerXml.writeOperationsCarFile();
			} else {
				Car car = manager.getCarByRoadAndNumber(roadComboBox.getSelectedItem().toString(),
						roadNumberTextField.getText());
				if (car != null){
					manager.deregister(car);
					// save car file
					managerXml.writeOperationsCarFile();
				}
			}
		}
		if (ae.getSource() == addButton){
			if (!checkCar(null))
				return;
			addCar();
			// save car file
			managerXml.writeOperationsCarFile();
		}
		if (ae.getSource() == clearRoadNumberButton){
			roadNumberTextField.setText("");
			roadNumberTextField.requestFocus();
		}

		if (ae.getSource() == fillWeightButton){
			calculateWeight ();
		}
		if (ae.getSource() == editLoadButton){
			if (lef != null)
				lef.dispose();
			lef = new CarLoadEditFrame();
			lef.setLocationRelativeTo(this);
			lef.initComponents((String)typeComboBox.getSelectedItem());
		}
	}
	
	CarLoadEditFrame lef = null;
	
	private boolean checkCar(Car c){
		String roadNum = roadNumberTextField.getText();
		if (roadNum.length() > 10){
			JOptionPane.showMessageDialog(this,rb.getString("carRoadNum"),
					rb.getString("carRoadLong"),
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		// check to see if car with road and number already exists
		Car car = manager.getCarByRoadAndNumber(roadComboBox.getSelectedItem().toString(),
				roadNumberTextField.getText());
		if (car != null){
			if (c == null || !car.getId().equals(c.getId())){
				JOptionPane.showMessageDialog(this,
						rb.getString("carRoadExists"), rb.getString("carCanNotUpdate"),
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		// check car's weight has proper format
		try{
			Double.parseDouble(weightTextField.getText());
		}catch (Exception e){
			JOptionPane.showMessageDialog(this,
					rb.getString("carWeightFormat"), rb.getString("carActualWeight"),
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		// check car's weight in tons has proper format
		try{
			Integer.parseInt(weightTonsTextField.getText());
		}catch (Exception e){
			JOptionPane.showMessageDialog(this,
					rb.getString("carWeightFormatTon"), rb.getString("carWeightTon"),
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	private void calculateWeight() {
		if (lengthComboBox.getSelectedItem() != null) {
			String item = (String) lengthComboBox.getSelectedItem();
			try {
				double carLength = Double.parseDouble(item)*12/Setup.getScaleRatio();
				double carWeight = (Setup.getInitalWeight() + carLength
						* Setup.getAddWeight()) / 1000;
				NumberFormat nf = NumberFormat.getNumberInstance();
				nf.setMaximumFractionDigits(1);
				weightTextField.setText((nf.format(carWeight)));
				weightTonsTextField.setText(Integer.toString((int)(carWeight*Setup.getScaleTonRatio())));
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(this,
						rb.getString("carLengthMustBe"), rb.getString("carWeigthCanNot"),
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private Car addCar() {
		if (roadComboBox.getSelectedItem() != null
				&& !roadComboBox.getSelectedItem().toString().equals("")) {
			if (_car == null
					|| !_car.getRoad().equals(roadComboBox.getSelectedItem().toString())
					|| !_car.getNumber().equals(roadNumberTextField.getText())) {
				_car = manager.newCar(roadComboBox.getSelectedItem().toString(),
						roadNumberTextField.getText());
				_car.addPropertyChangeListener(this);
			}
			if (typeComboBox.getSelectedItem() != null)
				_car.setType(typeComboBox.getSelectedItem().toString());
			if (lengthComboBox.getSelectedItem() != null)
				_car.setLength(lengthComboBox.getSelectedItem().toString());
			if (colorComboBox.getSelectedItem() != null)
				_car.setColor(colorComboBox.getSelectedItem().toString());
			_car.setWeight(weightTextField.getText());
			_car.setWeightTons(weightTonsTextField.getText());
			_car.setCaboose(cabooseCheckBox.isSelected());
			_car.setFred(fredCheckBox.isSelected());
			_car.setHazardous(hazardousCheckBox.isSelected());
			_car.setBuilt(builtTextField.getText());
			if (ownerComboBox.getSelectedItem() != null)
				_car.setOwner(ownerComboBox.getSelectedItem().toString());
			if (loadComboBox.getSelectedItem() != null)
				_car.setLoad(loadComboBox.getSelectedItem().toString());
			if (kernelComboBox.getSelectedItem() != null){
				if (kernelComboBox.getSelectedItem().equals(""))
					_car.setKernel(null);
				else
					_car.setKernel(manager.getKernelByName((String)kernelComboBox.getSelectedItem()));
			}
			if (locationBox.getSelectedItem() != null){
				if (locationBox.getSelectedItem().equals("")) {
					_car.setLocation(null, null);
				} else {
					if (trackLocationBox.getSelectedItem() == null
							|| trackLocationBox.getSelectedItem().equals("")) {
						JOptionPane.showMessageDialog(this,
								rb.getString("carFullySelect"), rb.getString("carCanNotLoc"),
								JOptionPane.ERROR_MESSAGE);

					} else {
						String status = _car.setLocation((Location)locationBox.getSelectedItem(),
								(Track)trackLocationBox.getSelectedItem());
						if (!status.equals(Car.OKAY)){
							log.debug ("Can't set car's location because of "+ status);
							JOptionPane.showMessageDialog(this,
									rb.getString("carCanNotLocMsg")+ status,
									rb.getString("carCanNotLoc"),
									JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			}
			_car.setComment(commentTextField.getText());
			_car.setRfid(rfidTextField.getText());
			return _car;
		}
		return null;
	}

	private void addEditButtonAction(JButton b) {
		b.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				buttonEditActionPerformed(e);
			}
		});
	}

	private boolean editActive = false;
	CarAttributeEditFrame f;

	// edit buttons only one frame active at a time
	public void buttonEditActionPerformed(java.awt.event.ActionEvent ae) {
		if (editActive){
			f.dispose();
		}
		f = new CarAttributeEditFrame();
		f.setLocationRelativeTo(this);
		f.addPropertyChangeListener(this);
		editActive = true;

		if(ae.getSource() == editRoadButton)
			f.initComponents(ROAD);
		if(ae.getSource() == editTypeButton)
			f.initComponents(TYPE);
		if(ae.getSource() == editColorButton)
			f.initComponents(COLOR);
		if(ae.getSource() == editLengthButton)
			f.initComponents(LENGTH);
		if(ae.getSource() == editOwnerButton)
			f.initComponents(OWNER);
		if(ae.getSource() == editKernelButton)
			f.initComponents(KERNEL);
	}

	public void dispose(){
		removePropertyChangeListeners();
		super.dispose();
	}

	private void removePropertyChangeListeners(){
		CarRoads.instance().removePropertyChangeListener(this);
		CarLoads.instance().removePropertyChangeListener(this);
		CarTypes.instance().removePropertyChangeListener(this);
		CarLengths.instance().removePropertyChangeListener(this);
		CarColors.instance().removePropertyChangeListener(this);
		CarOwners.instance().removePropertyChangeListener(this);
		locationManager.removePropertyChangeListener(this);
		manager.removePropertyChangeListener(this);
		if (_car != null)
			_car.removePropertyChangeListener(this);
	}

	public void propertyChange(java.beans.PropertyChangeEvent e) {
		if(Control.showProperty && log.isDebugEnabled()) 
			log.debug ("CarEditFrame sees propertyChange "+e.getPropertyName()+" "+e.getNewValue());
		if (e.getPropertyName().equals(CarRoads.CARROADS_LENGTH_CHANGED_PROPERTY)){
			CarRoads.instance().updateComboBox(roadComboBox);
			if (_car != null)
			roadComboBox.setSelectedItem(_car.getRoad());
		}
		if (e.getPropertyName().equals(CarTypes.CARTYPES_LENGTH_CHANGED_PROPERTY)){
			CarTypes.instance().updateComboBox(typeComboBox);
			if (_car != null)
				typeComboBox.setSelectedItem(_car.getType());
		}
		if (e.getPropertyName().equals(CarColors.CARCOLORS_CHANGED_PROPERTY)){
			CarColors.instance().updateComboBox(colorComboBox);
			if (_car != null)
				colorComboBox.setSelectedItem(_car.getColor());
		}
		if (e.getPropertyName().equals(CarLengths.CARLENGTHS_CHANGED_PROPERTY)){
			CarLengths.instance().updateComboBox(lengthComboBox);
			if (_car != null)
				lengthComboBox.setSelectedItem(_car.getLength());
		}
		if (e.getPropertyName().equals(CarManager.KERNELLISTLENGTH_CHANGED_PROPERTY)){
			manager.updateKernelComboBox(kernelComboBox);
			if (_car != null) 
				kernelComboBox.setSelectedItem(_car.getKernelName());
		}
		if (e.getPropertyName().equals(CarOwners.CAROWNERS_CHANGED_PROPERTY)){
			CarOwners.instance().updateComboBox(ownerComboBox);
			if (_car != null)
				ownerComboBox.setSelectedItem(_car.getOwner());
		}
		if (e.getPropertyName().equals(LocationManager.LISTLENGTH_CHANGED_PROPERTY)){
			LocationManager.instance().updateComboBox(locationBox);
			if (_car != null)
				locationBox.setSelectedItem(_car.getLocation());
		}
		if (e.getPropertyName().equals(Car.LOAD_CHANGED_PROPERTY)){
			if (_car != null)
				loadComboBox.setSelectedItem(_car.getLoad());
		}
		if (e.getPropertyName().equals(CarLoads.LOAD_CHANGED_PROPERTY)){
			if (_car != null){
				CarLoads.instance().updateComboBox((String)typeComboBox.getSelectedItem(), loadComboBox);
				loadComboBox.setSelectedItem(_car.getLoad());
			}
		}
		if (e.getPropertyName().equals(DISPOSE)){
			editActive = false;
		}
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(CarEditFrame.class.getName());
}
