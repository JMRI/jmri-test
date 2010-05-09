//EngineEditFrame.java

package jmri.jmrit.operations.rollingstock.engines;

import java.awt.GridBagLayout;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.LocationManagerXml;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.cars.CarManagerXml;
import jmri.jmrit.operations.rollingstock.cars.CarOwners;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.TrainManagerXml;


/**
 * Frame for user edit of engine
 * 
 * @author Dan Boudreau Copyright (C) 2008
 * @version $Revision: 1.10 $
 */

public class EngineEditFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.rollingstock.engines.JmritOperationsEnginesBundle");

	EngineManager manager = EngineManager.instance();
	EngineManagerXml managerXml = EngineManagerXml.instance();
	EngineModels engineModels = EngineModels.instance();
	EngineTypes engineTypes = EngineTypes.instance();
	EngineLengths engineLengths = EngineLengths.instance();
	CarManagerXml carManagerXml = CarManagerXml.instance();
	LocationManager locationManager = LocationManager.instance();

	Engine _engine;

	// major buttons
	JButton editRoadButton = new JButton(rb.getString("Edit"));
	JButton clearRoadNumberButton = new JButton(rb.getString("Clear"));
	JButton editModelButton = new JButton(rb.getString("Edit"));
	JButton editTypeButton = new JButton(rb.getString("Edit"));
	JButton editColorButton = new JButton(rb.getString("Edit"));
	JButton editLengthButton = new JButton(rb.getString("Edit"));
	JButton fillWeightButton = new JButton();
	JButton editConsistButton = new JButton(rb.getString("Edit"));
	JButton editOwnerButton = new JButton(rb.getString("Edit"));

	JButton saveButton = new JButton(rb.getString("Save"));
	JButton deleteButton = new JButton(rb.getString("Delete"));
	JButton copyButton = new JButton(rb.getString("Copy"));
	JButton addButton = new JButton(rb.getString("Add"));

	// check boxes

	// text field
	JTextField roadNumberTextField = new JTextField(8);
	JTextField builtTextField = new JTextField(8);
	JTextField hpTextField = new JTextField(8);
	JTextField weightTextField = new JTextField(4);
	JTextField commentTextField = new JTextField(35);
	JTextField rfidTextField = new JTextField(16);

	// combo boxes
	JComboBox roadComboBox = CarRoads.instance().getComboBox();
	JComboBox modelComboBox = engineModels.getComboBox();
	JComboBox typeComboBox = engineTypes.getComboBox();
	JComboBox lengthComboBox = engineLengths.getComboBox();
	JComboBox ownerComboBox = CarOwners.instance().getComboBox();
	JComboBox locationBox = locationManager.getComboBox();
	JComboBox trackLocationBox = new JComboBox();
	JComboBox consistComboBox = manager.getConsistComboBox(); 

	public static final String ROAD = rb.getString("Road");
	public static final String MODEL = rb.getString("Model");
	public static final String TYPE = rb.getString("Type");
	public static final String COLOR = rb.getString("Color");
	public static final String LENGTH = rb.getString("Length");
	public static final String OWNER = rb.getString("Owner");
	public static final String CONSIST = rb.getString("Consist");


	public EngineEditFrame() {
		super();
	}

	public void initComponents() {
		// set tips
		builtTextField.setToolTipText(rb.getString("buildDateTip"));
		rfidTextField.setToolTipText(rb.getString("TipRfid"));

		// create panel
		JPanel pPanel = new JPanel();
		pPanel.setLayout(new BoxLayout(pPanel,BoxLayout.Y_AXIS));

		// Layout the panel by rows
		// row 1
		JPanel pRoad = new JPanel();
		pRoad.setLayout(new GridBagLayout());
		pRoad.setBorder(BorderFactory.createTitledBorder(rb.getString("Road")));
		addItem(pRoad, roadComboBox, 1, 0);
		addItem(pRoad, editRoadButton, 2, 0);
		pPanel.add(pRoad);
		
		// row 2
		JPanel pRoadNumber = new JPanel();
		pRoadNumber.setLayout(new GridBagLayout());
		pRoadNumber.setBorder(BorderFactory.createTitledBorder(rb.getString("RoadNumber")));
		addItem(pRoadNumber, roadNumberTextField, 1, 0);
		addItem(pRoadNumber, clearRoadNumberButton, 2, 0);
		pPanel.add(pRoadNumber);
		
		// row 3
		JPanel pModel = new JPanel();
		pModel.setLayout(new GridBagLayout());
		pModel.setBorder(BorderFactory.createTitledBorder(rb.getString("Model")));
		addItem(pModel, modelComboBox, 1, 0);
		addItem(pModel, editModelButton, 2, 0);
		pPanel.add(pModel);
		
		// row 4
		JPanel pType = new JPanel();
		pType.setLayout(new GridBagLayout());
		pType.setBorder(BorderFactory.createTitledBorder(rb.getString("Type")));
		addItem(pType, typeComboBox, 1, 0);
		addItem(pType, editTypeButton, 2, 0);
		pPanel.add(pType);
		
		// row 5
		JPanel pLength = new JPanel();
		pLength.setLayout(new GridBagLayout());
		pLength.setBorder(BorderFactory.createTitledBorder(rb.getString("Length")));
		addItem(pLength, lengthComboBox, 1, 0);
		addItem(pLength, editLengthButton, 2, 0);
		pPanel.add(pLength);

		// row 6
		JPanel pLocation = new JPanel();
		pLocation.setLayout(new GridBagLayout());
		pLocation.setBorder(BorderFactory.createTitledBorder(rb.getString("Location")));
		addItem(pLocation, locationBox, 1, 0);
		addItem(pLocation, trackLocationBox, 2, 0);
		pPanel.add(pLocation);

		// optional panel
		JPanel pOptional = new JPanel();
		pOptional.setLayout(new BoxLayout(pOptional,BoxLayout.Y_AXIS));
		JScrollPane optionPane = new JScrollPane(pOptional);
		optionPane.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutOptional")));
		
		// row 11
		JPanel pWeightTons = new JPanel();
		pWeightTons.setLayout(new GridBagLayout());
		pWeightTons.setBorder(BorderFactory.createTitledBorder(rb.getString("WeightTons")));
		addItem(pWeightTons, weightTextField, 0, 0);
		pOptional.add(pWeightTons);
		
		// row 12
		JPanel pHp = new JPanel();
		pHp.setLayout(new GridBagLayout());
		pHp.setBorder(BorderFactory.createTitledBorder(rb.getString("Hp")));
		addItem(pHp, hpTextField, 0, 0);
		pOptional.add(pHp);
		
		// row 13
		JPanel pConsist = new JPanel();
		pConsist.setLayout(new GridBagLayout());
		pConsist.setBorder(BorderFactory.createTitledBorder(rb.getString("Consist")));
		addItem(pConsist, consistComboBox, 1, 0);
		addItem(pConsist, editConsistButton, 2, 0);
		pOptional.add(pConsist);

		// row 14
		JPanel pBuilt = new JPanel();
		pBuilt.setLayout(new GridBagLayout());
		pBuilt.setBorder(BorderFactory.createTitledBorder(rb.getString("Built")));
		addItem(pBuilt, builtTextField, 1, 0);
		pOptional.add(pBuilt);

		// row 15
		JPanel pOwner = new JPanel();
		pOwner.setLayout(new GridBagLayout());
		pOwner.setBorder(BorderFactory.createTitledBorder(rb.getString("Owner")));
		addItem(pOwner, ownerComboBox, 1, 0);
		addItem(pOwner, editOwnerButton, 2, 0);
		pOptional.add(pOwner);
		
		// row 18
		if(Setup.isRfidEnabled()){
			JPanel pRfid = new JPanel();
			pRfid.setLayout(new GridBagLayout());
			pRfid.setBorder(BorderFactory.createTitledBorder(rb.getString("Rfid")));
			addItem(pRfid, rfidTextField, 1, 0);
			pOptional.add(pRfid);
		}

		// row 20
		JPanel pComment = new JPanel();
		pComment.setLayout(new GridBagLayout());
		pComment.setBorder(BorderFactory.createTitledBorder(rb.getString("Comment")));
		addItem(pComment, commentTextField, 1, 0);
		pOptional.add(pComment);

		// button panel
		JPanel pButtons = new JPanel();
		pButtons.setLayout(new GridBagLayout());
		addItem(pButtons, deleteButton, 0, 25);
		addItem(pButtons, addButton, 1, 25);
		addItem(pButtons, saveButton, 3, 25);
		
		// add panels
		getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
		getContentPane().add(pPanel);
		getContentPane().add(optionPane);
		getContentPane().add(pButtons);

		// setup buttons
		addEditButtonAction(editRoadButton);
		addButtonAction(clearRoadNumberButton);
		addEditButtonAction(editModelButton);
		addEditButtonAction(editTypeButton);
		addEditButtonAction(editLengthButton);
		addEditButtonAction(editColorButton);
		addEditButtonAction(editConsistButton);
		addEditButtonAction(editOwnerButton);

		addButtonAction(deleteButton);
		addButtonAction(addButton);
		addButtonAction(copyButton);
		addButtonAction(saveButton);
		addButtonAction(fillWeightButton);

		// setup combobox
		addComboBoxAction(modelComboBox);
		addComboBoxAction(locationBox);
		
		// setup checkbox

		// build menu
//		JMenuBar menuBar = new JMenuBar();
//		JMenu toolMenu = new JMenu("Tools");
//		menuBar.add(toolMenu);
//		setJMenuBar(menuBar);
		addHelpMenu("package.jmri.jmrit.operations.Operations_Engines", true);

		//	 get notified if combo box gets modified
		CarRoads.instance().addPropertyChangeListener(this);
		engineModels.addPropertyChangeListener(this);
		engineTypes.addPropertyChangeListener(this);
		engineLengths.addPropertyChangeListener(this);
		CarOwners.instance().addPropertyChangeListener(this);
		locationManager.addPropertyChangeListener(this);
		manager.addPropertyChangeListener(this);

		// set frame size and location for display
		pack();
		if (manager.getEditFrameSize()!= null)
			setSize(manager.getEditFrameSize());
		else if (getWidth()<400)
			setSize(450, getHeight());
		else
			setSize (getWidth()+50, getHeight());
		if (manager.getEditFramePosition()!= null){
			setLocation(manager.getEditFramePosition());
		}
		setVisible(true);	
	}

	public void loadEngine(Engine engine){
		_engine = engine;

		if (!CarRoads.instance().containsName(engine.getRoad())){
			String msg = MessageFormat.format(rb.getString("roadNameNotExist"),new Object[]{engine.getRoad()});
			if (JOptionPane.showConfirmDialog(this,
					msg, rb.getString("engineAddRoad"),
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
				CarRoads.instance().addName(engine.getRoad());
			}
		}
		roadComboBox.setSelectedItem(engine.getRoad());

		roadNumberTextField.setText(engine.getNumber());

		if (!engineModels.containsName(engine.getModel())){
			String msg = MessageFormat.format(rb.getString("modelNameNotExist"),new Object[]{engine.getModel()});
			if (JOptionPane.showConfirmDialog(this,
					msg, rb.getString("engineAddModel"),
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
				engineModels.addName(engine.getModel());
			}
		}
		modelComboBox.setSelectedItem(engine.getModel());
		
		if (!engineTypes.containsName(engine.getType())){
			String msg = MessageFormat.format(rb.getString("typeNameNotExist"),new Object[]{engine.getType()});
			if (JOptionPane.showConfirmDialog(this,
					msg, rb.getString("engineAddType"),
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
				engineTypes.addName(engine.getType());
			}
		}
		typeComboBox.setSelectedItem(engine.getType());

		if (!engineLengths.containsName(engine.getLength())){
			String msg = MessageFormat.format(rb.getString("lengthNameNotExist"),new Object[]{engine.getLength()});
			if (JOptionPane.showConfirmDialog(this,
					msg, rb.getString("engineAddLength"),
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
				engineLengths.addName(engine.getLength());
			}
		}
		lengthComboBox.setSelectedItem(engine.getLength());
		weightTextField.setText(engine.getWeightTons());
		hpTextField.setText(engine.getHp());

		locationBox.setSelectedItem(engine.getLocation());
		Location l = locationManager.getLocationById(engine.getLocationId());
		if (l != null){
			l.updateComboBox(trackLocationBox);
			trackLocationBox.setSelectedItem(engine.getTrack());
		} else {
			trackLocationBox.removeAllItems();
		}

		builtTextField.setText(engine.getBuilt());

		if (!CarOwners.instance().containsName(engine.getOwner())){
			String msg = MessageFormat.format(rb.getString("ownerNameNotExist"),new Object[]{engine.getOwner()});
			if (JOptionPane.showConfirmDialog(this,
					msg, rb.getString("addOwner"),
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
				CarOwners.instance().addName(engine.getOwner());
			}
		}
		consistComboBox.setSelectedItem(engine.getConsistName());
				
		ownerComboBox.setSelectedItem(engine.getOwner());
		rfidTextField.setText(engine.getRfid());
		commentTextField.setText(engine.getComment());
	}

	// combo boxes
	public void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource()== modelComboBox){
			if (modelComboBox.getSelectedItem() != null){
				String model = (String)modelComboBox.getSelectedItem();
				// load the default hp and length for the model selected
				hpTextField.setText(engineModels.getModelHorsepower(model));
				weightTextField.setText(engineModels.getModelWeight(model));
				if(engineModels.getModelLength(model)!= null && !engineModels.getModelLength(model).equals(""))
					lengthComboBox.setSelectedItem(engineModels.getModelLength(model));
				if(engineModels.getModelType(model)!= null && !engineModels.getModelType(model).equals(""))
					typeComboBox.setSelectedItem(engineModels.getModelType(model));
			}
		}
		if (ae.getSource()== locationBox){
			if (locationBox.getSelectedItem() != null){
				if (locationBox.getSelectedItem().equals("")){
					trackLocationBox.removeAllItems();
				}else{
					log.debug("EnginesSetFrame sees location: "+ locationBox.getSelectedItem());
					Location l = ((Location)locationBox.getSelectedItem());
					l.updateComboBox(trackLocationBox);
				}
			}
		}
	}
	
	public void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
		JCheckBox b =  (JCheckBox)ae.getSource();
		log.debug("checkbox change "+ b.getText());
	}

	// Save, Delete, Add, Clear, Calculate buttons
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == saveButton){
			// log.debug("engine save button activated");
			String roadNum = roadNumberTextField.getText();
			if (roadNum.length() > 10){
				JOptionPane.showMessageDialog(this,rb.getString("engineRoadNum"),
						rb.getString("engineRoadLong"),
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			// check to see if engine with road and number already exists
			Engine engine = manager.getByRoadAndNumber(roadComboBox.getSelectedItem().toString(), roadNumberTextField.getText());
			if (engine != null){
				if (_engine == null || !engine.getId().equals(_engine.getId())){
					JOptionPane.showMessageDialog(this,
							rb.getString("engineExists"), rb.getString("engineCanNotUpdate"),
							JOptionPane.ERROR_MESSAGE);
					return;
				}
			}

			// delete engine if edit and road or road number has changed
			if (_engine != null){
				if (_engine.getRoad() != null && !_engine.getRoad().equals("")){
					if (!_engine.getRoad().equals(roadComboBox.getSelectedItem().toString())
							|| !_engine.getNumber().equals(roadNumberTextField.getText())) {
						// transfer engine attributes since road name and number have changed
						Engine oldengine = _engine;
						Engine newEngine = addEngine();
						newEngine.setDestination(oldengine.getDestination(), oldengine.getDestinationTrack());
						newEngine.setTrain(oldengine.getTrain());
						manager.deregister(oldengine);
						writeFiles();
						return;
					}
				}
			}
			addEngine();
			// save frame size and position
			manager.setEditFrame(this);
			writeFiles();
		}
		if (ae.getSource() == deleteButton){
			log.debug("engine delete button actived");
			if (_engine != null
					&& _engine.getRoad().equals(roadComboBox.getSelectedItem().toString())
					&& _engine.getNumber().equals(roadNumberTextField.getText())) {
				manager.deregister(_engine);
				// save engine file
				writeFiles();
			} else {
				Engine e = manager.getByRoadAndNumber(roadComboBox.getSelectedItem().toString(), roadNumberTextField.getText());
				if (e != null){
					manager.deregister(e);
					// save engine file
					writeFiles();
				}
			}
		}
		if (ae.getSource() == addButton){
			String roadNum = roadNumberTextField.getText();
			if (roadNum.length() > 10){
				JOptionPane.showMessageDialog(this, rb.getString("engineRoadNum"),
						rb.getString("engineRoadLong"),
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			Engine e = manager.getByRoadAndNumber(roadComboBox.getSelectedItem().toString(), roadNumberTextField.getText() );
			if (e != null){
				log.info("Can not add, engine already exists");
				JOptionPane.showMessageDialog(this,
						rb.getString("engineExists"), rb.getString("engineCanNotUpdate"),
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			addEngine();
			// save engine file
			writeFiles();
		}
		if (ae.getSource() == clearRoadNumberButton){
			roadNumberTextField.setText("");
			roadNumberTextField.requestFocus();
		}
	}

	private Engine addEngine() {
		if (roadComboBox.getSelectedItem() != null
				&& !roadComboBox.getSelectedItem().toString().equals("")) {
			if (_engine == null
					|| !_engine.getRoad().equals(roadComboBox.getSelectedItem().toString())
					|| !_engine.getNumber().equals(roadNumberTextField.getText())) {
				_engine = manager.newEngine(roadComboBox.getSelectedItem().toString(),
						roadNumberTextField.getText());
			}
			if (modelComboBox.getSelectedItem() != null)
				_engine.setModel(modelComboBox.getSelectedItem().toString());
			if (typeComboBox.getSelectedItem() != null)
				_engine.setType(typeComboBox.getSelectedItem().toString());
			if (lengthComboBox.getSelectedItem() != null)
				_engine.setLength(lengthComboBox.getSelectedItem().toString());
			_engine.setBuilt(builtTextField.getText());
			if (ownerComboBox.getSelectedItem() != null)
				_engine.setOwner(ownerComboBox.getSelectedItem().toString());
			if (consistComboBox.getSelectedItem() != null){
				if (consistComboBox.getSelectedItem().equals(""))
					_engine.setConsist(null);
				else
					_engine.setConsist(manager.getConsistByName((String)consistComboBox.getSelectedItem()));
			}
			// confirm that weight is a number
			if (!weightTextField.getText().equals("") ){
				try{
					Integer.parseInt(weightTextField.getText());
					_engine.setWeightTons(weightTextField.getText());
				} catch (Exception e){
					JOptionPane.showMessageDialog(this,
							rb.getString("engineWeight"), rb.getString("engineCanNotWeight"),
							JOptionPane.ERROR_MESSAGE);
				}
			}
			// confirm that horsepower is a number
			if (!hpTextField.getText().equals("") ){
				try{
					Integer.parseInt(hpTextField.getText());
					_engine.setHp(hpTextField.getText());
				} catch (Exception e){
					JOptionPane.showMessageDialog(this,
							rb.getString("engineHorsepower"), rb.getString("engineCanNotHp"),
							JOptionPane.ERROR_MESSAGE);
				}
			}
			if (locationBox.getSelectedItem() != null){
				if (locationBox.getSelectedItem().equals("")) {
					_engine.setLocation(null, null);
				} else {
					if (trackLocationBox.getSelectedItem() == null
							|| trackLocationBox.getSelectedItem()
							.equals("")) {
						JOptionPane.showMessageDialog(this,
								rb.getString("engineFullySelect"), rb.getString("engineCanNotLoc"),
								JOptionPane.ERROR_MESSAGE);

					} else {
						String status = _engine.setLocation((Location)locationBox.getSelectedItem(),
								(Track)trackLocationBox.getSelectedItem());
						if (!status.equals(Engine.OKAY)){
							log.debug ("Can't set engine's location because of "+ status);
							JOptionPane.showMessageDialog(this,
									rb.getString("engineCanNotLocMsg")+ status, rb.getString("engineCanNotLoc"),
									JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			}
			_engine.setComment(commentTextField.getText());
			_engine.setRfid(rfidTextField.getText());
			return _engine;
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
	
	static boolean filesModified = false;
	/**
	 * Need to also write the location and train files if a road name
	 * was deleted. Need to also write files if car type was changed.
	 */
	private void writeFiles(){
		managerXml.writeOperationsEngineFile();		//save engine file
		if (filesModified){
			filesModified = false;
			carManagerXml.writeOperationsCarFile(); 	//save road names, and owners
			LocationManagerXml.instance().writeOperationsLocationFile();
			TrainManagerXml.instance().writeOperationsTrainFile();
		}	
	}

	private boolean editActive = false;
	EngineAttributeEditFrame f;

	// edit buttons only one frame active at a time
	public void buttonEditActionPerformed(java.awt.event.ActionEvent ae) {
		if (editActive){
			f.dispose();
		}
		f = new EngineAttributeEditFrame();
		f.setLocationRelativeTo(this);
		f.addPropertyChangeListener(this);
		editActive = true;

		if(ae.getSource() == editRoadButton)
			f.initComponents(ROAD);
		if(ae.getSource() == editModelButton)
			f.initComponents(MODEL);
		if(ae.getSource() == editTypeButton)
			f.initComponents(TYPE);
		if(ae.getSource() == editColorButton)
			f.initComponents(COLOR);
		if(ae.getSource() == editLengthButton)
			f.initComponents(LENGTH);
		if(ae.getSource() == editOwnerButton)
			f.initComponents(OWNER);
		if(ae.getSource() == editConsistButton)
			f.initComponents(CONSIST);
	}

	public void dispose(){
		removePropertyChangeListeners();
		super.dispose();
	}

	private void removePropertyChangeListeners(){
		CarRoads.instance().removePropertyChangeListener(this);
		engineModels.removePropertyChangeListener(this);
		engineTypes.removePropertyChangeListener(this);
		engineLengths.removePropertyChangeListener(this);
		CarOwners.instance().removePropertyChangeListener(this);
		locationManager.removePropertyChangeListener(this);
		manager.removePropertyChangeListener(this);
	}

	public void propertyChange(java.beans.PropertyChangeEvent e) {
		log.debug ("EngineEditFrame sees propertyChange "+e.getPropertyName()+" "+e.getNewValue());
		if (e.getPropertyName().equals(CarRoads.CARROADS_LENGTH_CHANGED_PROPERTY)){
			filesModified = true;
			CarRoads.instance().updateComboBox(roadComboBox);
			if (_engine != null)
			roadComboBox.setSelectedItem(_engine.getRoad());
		}
		if (e.getPropertyName().equals(EngineModels.ENGINEMODELS_CHANGED_PROPERTY)){
			engineModels.updateComboBox(modelComboBox);
			if (_engine != null)
				modelComboBox.setSelectedItem(_engine.getModel());
		}
		if (e.getPropertyName().equals(EngineTypes.ENGINETYPES_LENGTH_CHANGED_PROPERTY)){
			filesModified = true;
			engineTypes.updateComboBox(typeComboBox);
			if (_engine != null)
				typeComboBox.setSelectedItem(_engine.getType());
		}
		if (e.getPropertyName().equals(EngineLengths.ENGINELENGTHS_CHANGED_PROPERTY)){
			engineLengths.updateComboBox(lengthComboBox);
			if (_engine != null)
				lengthComboBox.setSelectedItem(_engine.getLength());
		}
		if (e.getPropertyName().equals(EngineManager.CONSISTLISTLENGTH_CHANGED_PROPERTY)){
			manager.updateConsistComboBox(consistComboBox);
			if (_engine != null) 
				consistComboBox.setSelectedItem(_engine.getConsistName());
		}
		if (e.getPropertyName().equals(CarOwners.CAROWNERS_CHANGED_PROPERTY)){
			filesModified = true;
			CarOwners.instance().updateComboBox(ownerComboBox);
			if (_engine != null)
				ownerComboBox.setSelectedItem(_engine.getOwner());
		}
		if (e.getPropertyName().equals(LocationManager.LISTLENGTH_CHANGED_PROPERTY)){
			LocationManager.instance().updateComboBox(locationBox);
			if (_engine != null)
				locationBox.setSelectedItem(_engine.getLocation());
		}
		if (e.getPropertyName().equals(EngineAttributeEditFrame.DISPOSE)){
			editActive = false;
		}
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(EngineEditFrame.class.getName());
}
