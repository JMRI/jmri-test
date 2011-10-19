// TrainEditBuildOptionsFrame.java

package jmri.jmrit.operations.trains;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
//import java.text.MessageFormat;
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
//import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.JTextField;

import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.rollingstock.cars.CarLoads;
import jmri.jmrit.operations.rollingstock.cars.CarOwners;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.rollingstock.engines.EngineModels;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;


/**
 * Frame for user edit of a train's build options
 * 
 * @author Dan Boudreau Copyright (C) 2010
 * @version $Revision$
 */

public class TrainEditBuildOptionsFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.trains.JmritOperationsTrainsBundle");
	static final ResourceBundle rbr = ResourceBundle.getBundle("jmri.jmrit.operations.routes.JmritOperationsRoutesBundle");
	
	TrainManager manager;
	TrainManagerXml managerXml;

	Train _train = null;
	TrainEditFrame _trainEditFrame;

	JPanel panelRoadNames = new JPanel();
	JPanel panelLoadNames = new JPanel();
	JPanel panelOwnerNames = new JPanel();
	JPanel panelBuilt = new JPanel();
	JPanel panelTrainReq1 = new JPanel();
	JPanel panelTrainReq2 = new JPanel();
	JScrollPane roadPane;
	JScrollPane loadPane;
	JScrollPane ownerPane;
	JScrollPane builtPane;
	JScrollPane trainReq1Pane;
	JScrollPane trainReq2Pane;
	
	JPanel engine1Option = new JPanel();
	JPanel engine1DropOption = new JPanel();
	JPanel engine1caboose = new JPanel();
	
	JPanel engine2Option = new JPanel();
	JPanel engine2DropOption = new JPanel();
	JPanel engine2caboose = new JPanel();

	// labels
	JLabel trainName = new JLabel();
	JLabel trainDescription = new JLabel();
	JLabel before = new JLabel(rb.getString("Before"));
	JLabel after = new JLabel(rb.getString("After"));

	// major buttons
	JButton addRoadButton = new JButton(rb.getString("AddRoad"));
	JButton deleteRoadButton = new JButton(rb.getString("DeleteRoad"));
	
	JButton addLoadButton = new JButton(rb.getString("AddLoad"));
	JButton deleteLoadButton = new JButton(rb.getString("DeleteLoad"));
	JButton deleteAllLoadsButton = new JButton(rb.getString("DeleteAllLoads"));
	
	JButton addOwnerButton = new JButton(rb.getString("AddOwner"));
	JButton deleteOwnerButton = new JButton(rb.getString("DeleteOwner"));	
	JButton saveTrainButton = new JButton(rb.getString("SaveTrain"));

	// radio buttons    
    JRadioButton roadNameAll = new JRadioButton(rb.getString("AcceptAll"));
    JRadioButton roadNameInclude = new JRadioButton(rb.getString("AcceptOnly"));
    JRadioButton roadNameExclude = new JRadioButton(rb.getString("Exclude"));
    
    JRadioButton loadNameAll = new JRadioButton(rb.getString("AcceptAll"));
    JRadioButton loadNameInclude = new JRadioButton(rb.getString("AcceptOnly"));
    JRadioButton loadNameExclude = new JRadioButton(rb.getString("Exclude"));
    
    JRadioButton ownerNameAll = new JRadioButton(rb.getString("AcceptAll"));
    JRadioButton ownerNameInclude = new JRadioButton(rb.getString("AcceptOnly"));
    JRadioButton ownerNameExclude = new JRadioButton(rb.getString("Exclude"));
    
    JRadioButton builtDateAll = new JRadioButton(rb.getString("AcceptAll"));
    JRadioButton builtDateAfter = new JRadioButton(rb.getString("After"));
    JRadioButton builtDateBefore = new JRadioButton(rb.getString("Before"));
    JRadioButton builtDateRange = new JRadioButton(rb.getString("Range"));
    
    ButtonGroup roadGroup = new ButtonGroup();
    ButtonGroup loadGroup = new ButtonGroup();
    ButtonGroup ownerGroup = new ButtonGroup();
    ButtonGroup builtGroup = new ButtonGroup();
    
	// train requirements 1st set
    JRadioButton none1 = new JRadioButton(rb.getString("None"));
    JRadioButton change1Engine = new JRadioButton(rb.getString("EngineChange"));
    JRadioButton helper1Service = new JRadioButton(rb.getString("HelperService"));
    JRadioButton keep1Caboose = new JRadioButton(rb.getString("KeepCaboose"));
    JRadioButton change1Caboose = new JRadioButton(rb.getString("ChangeCaboose"));
    
    ButtonGroup trainReq1Group = new ButtonGroup();
    ButtonGroup cabooseOption1Group = new ButtonGroup();
    
	// train requirements 2nd set
    JRadioButton none2 = new JRadioButton(rb.getString("None"));
    JRadioButton change2Engine = new JRadioButton(rb.getString("EngineChange"));
    JRadioButton helper2Service = new JRadioButton(rb.getString("HelperService"));
    JRadioButton keep2Caboose = new JRadioButton(rb.getString("KeepCaboose"));
    JRadioButton change2Caboose = new JRadioButton(rb.getString("ChangeCaboose"));

    ButtonGroup trainReq2Group = new ButtonGroup();
    ButtonGroup cabooseOption2Group = new ButtonGroup();
    
    // check boxes
    JCheckBox sendToTerminalcheckBox = new JCheckBox();
	
	// text field
    JTextField builtAfterTextField = new JTextField(10);
    JTextField builtBeforeTextField = new JTextField(10);
	
	// combo boxes
	JComboBox roadBox = CarRoads.instance().getComboBox();
	JComboBox typeBox = CarTypes.instance().getComboBox();
	JComboBox loadBox = CarLoads.instance().getComboBox(null);
	JComboBox ownerBox = CarOwners.instance().getComboBox();
	
	// train requirements 1st set
	JComboBox routePickup1Box = new JComboBox();
	JComboBox routeDrop1Box = new JComboBox();
	JComboBox roadCaboose1Box = new JComboBox();
	JComboBox roadEngine1Box = CarRoads.instance().getComboBox();
	JComboBox modelEngine1Box = EngineModels.instance().getComboBox();
	JComboBox numEngines1Box = new JComboBox();
	
	// train requirements 2nd set
	JComboBox routePickup2Box = new JComboBox();
	JComboBox routeDrop2Box = new JComboBox();
	JComboBox roadCaboose2Box = new JComboBox();
	JComboBox roadEngine2Box = CarRoads.instance().getComboBox();
	JComboBox modelEngine2Box = EngineModels.instance().getComboBox();
	JComboBox numEngines2Box = new JComboBox();

	public static final String DISPOSE = "dispose" ;

	public TrainEditBuildOptionsFrame() {
		super();
 	}

	public void initComponents(TrainEditFrame parent) {
		
    	// Set up the jtable in a Scroll Pane..
      	roadPane = new JScrollPane(panelRoadNames);
    	roadPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    	roadPane.setBorder(BorderFactory.createTitledBorder(rb.getString("RoadsTrain")));
    	
      	loadPane = new JScrollPane(panelLoadNames);
      	loadPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
      	loadPane.setBorder(BorderFactory.createTitledBorder(rb.getString("LoadsTrain")));
      	
      	ownerPane = new JScrollPane(panelOwnerNames);
      	ownerPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
      	ownerPane.setBorder(BorderFactory.createTitledBorder(rb.getString("OwnersTrain")));
      	
      	builtPane = new JScrollPane(panelBuilt);
      	builtPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
      	builtPane.setBorder(BorderFactory.createTitledBorder(rb.getString("BuiltTrain")));
      	
      	trainReq1Pane = new JScrollPane(panelTrainReq1);
      	trainReq1Pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
      	trainReq1Pane.setBorder(BorderFactory.createTitledBorder(rb.getString("TrainRequires")));
      	
      	trainReq2Pane = new JScrollPane(panelTrainReq2);
      	trainReq2Pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
      	trainReq2Pane.setBorder(BorderFactory.createTitledBorder(rb.getString("TrainRequires")));
      	
		_trainEditFrame = parent;
		_trainEditFrame.setChildFrame(this);
		_train = _trainEditFrame._train;

		// load managers
		manager = TrainManager.instance();
		managerXml = TrainManagerXml.instance();
	
	    getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
				
		// Layout the panel by rows
	   	JPanel p1 = new JPanel();
    	p1.setLayout(new BoxLayout(p1,BoxLayout.X_AXIS));
				
		// Layout the panel by rows
		// row 1a
       	JPanel pName = new JPanel();
    	pName.setLayout(new GridBagLayout());
    	pName.setBorder(BorderFactory.createTitledBorder(rb.getString("Name")));
    	addItem(pName, trainName, 0, 0);

		// row 1b
       	JPanel pDesc = new JPanel();
    	pDesc.setLayout(new GridBagLayout());
    	pDesc.setBorder(BorderFactory.createTitledBorder(rb.getString("Description")));
    	addItem(pDesc, trainDescription, 0, 0);
		
    	p1.add(pName);
    	p1.add(pDesc);
    	
    	// row 2
    	JPanel pOption = new JPanel();
    	pOption.setLayout(new GridBagLayout());
    	pOption.setBorder(BorderFactory.createTitledBorder(rb.getString("Options")));
    	addItem(pOption, sendToTerminalcheckBox, 0, 0);
    		
		// row 3
		panelRoadNames.setLayout(new GridBagLayout());
		roadGroup.add(roadNameAll);
		roadGroup.add(roadNameInclude);
		roadGroup.add(roadNameExclude);
		
		// row 5
		panelLoadNames.setLayout(new GridBagLayout());
		loadGroup.add(loadNameAll);
		loadGroup.add(loadNameInclude);
		loadGroup.add(loadNameExclude);
		
		// row 7
		panelOwnerNames.setLayout(new GridBagLayout());
		ownerGroup.add(ownerNameAll);
		ownerGroup.add(ownerNameInclude);
		ownerGroup.add(ownerNameExclude);
		
		// row 9
		panelBuilt.setLayout(new GridBagLayout());
		builtAfterTextField.setToolTipText(rb.getString("EnterYearTip"));
		builtBeforeTextField.setToolTipText(rb.getString("EnterYearTip"));
		addItem(panelBuilt, builtDateAll, 0, 0);
		addItem(panelBuilt, builtDateAfter, 1, 0);
		addItem(panelBuilt, builtDateBefore, 2, 0);
		addItem(panelBuilt, builtDateRange, 3, 0);
		addItem(panelBuilt, after, 1, 1);
		addItem(panelBuilt, builtAfterTextField, 2, 1);
		addItem(panelBuilt, before, 1, 2);
		addItem(panelBuilt, builtBeforeTextField, 2, 2);
		builtGroup.add(builtDateAll);
		builtGroup.add(builtDateAfter);
		builtGroup.add(builtDateBefore);
		builtGroup.add(builtDateRange);
		
		// row 11
		panelTrainReq1.setLayout(new BoxLayout(panelTrainReq1, BoxLayout.Y_AXIS));
		
		JPanel trainOption1 = new JPanel();
		trainOption1.add(none1);
		trainOption1.add(change1Engine);
		trainOption1.add(helper1Service);
		panelTrainReq1.add(trainOption1);
		
		trainReq1Group.add(none1);
		trainReq1Group.add(change1Engine);
		trainReq1Group.add(helper1Service);
		
		// engine options
		engine1Option.setLayout(new GridBagLayout());
		
    	for (int i=0; i<Setup.getEngineSize()+1; i++){
    		numEngines1Box.addItem(Integer.toString(i));
    	}
    	numEngines1Box.setMinimumSize(new Dimension(50,20));
    	modelEngine1Box.insertItemAt("",0);
    	modelEngine1Box.setSelectedIndex(0);
    	modelEngine1Box.setMinimumSize(new Dimension(120,20));
    	modelEngine1Box.setToolTipText(rb.getString("ModelEngineTip"));
    	roadEngine1Box.insertItemAt("",0);
    	roadEngine1Box.setSelectedIndex(0);
    	roadEngine1Box.setMinimumSize(new Dimension(120,20));
    	roadEngine1Box.setToolTipText(rb.getString("RoadEngineTip"));
		addItem (engine1Option, new JLabel(rb.getString("ChangeEnginesAt")), 0, 0);
		addItem (engine1Option, routePickup1Box, 1, 0);
    	addItem (engine1Option, new JLabel(rb.getString("Engines")), 2, 0);
    	addItem (engine1Option, numEngines1Box, 3, 0);
    	addItem (engine1Option, new JLabel(rb.getString("Model")), 4, 0);
    	addItem (engine1Option, modelEngine1Box, 5, 0);
    	addItem (engine1Option, new JLabel(rb.getString("Road")), 6, 0);
    	addItem (engine1Option, roadEngine1Box, 7, 0);
    	panelTrainReq1.add(engine1Option);
    	
    	// caboose options
    	//roadCaboose1Box.setBorder(BorderFactory.createTitledBorder(rb.getString("Caboose")));
    	roadCaboose1Box.setMinimumSize(new Dimension(120,20));
    	roadCaboose1Box.setToolTipText(rb.getString("RoadCabooseTip"));
    	addItem (engine1caboose, keep1Caboose, 2, 6);
    	addItem (engine1caboose, change1Caboose, 3, 6);
     	addItem (engine1caboose, new JLabel(rb.getString("Road")), 5, 6);
     	addItem (engine1caboose, roadCaboose1Box, 6, 6);
       	panelTrainReq1.add(engine1caboose);
    	
    	cabooseOption1Group.add(keep1Caboose);
    	cabooseOption1Group.add(change1Caboose);
    	
    	// drop engine panel
    	addItem(engine1DropOption, new JLabel(rb.getString("DropEnginesAt")), 0 , 0);
    	addItem(engine1DropOption, routeDrop1Box, 1, 0);
    	panelTrainReq1.add(engine1DropOption);
		
		// row 13
		panelTrainReq2.setLayout(new BoxLayout(panelTrainReq2, BoxLayout.Y_AXIS));
		
		JPanel trainOption2 = new JPanel();
		trainOption2.add(none2);
		trainOption2.add(change2Engine);
		trainOption2.add(helper2Service);
		panelTrainReq2.add(trainOption2);
		
		trainReq2Group.add(none2);
		trainReq2Group.add(change2Engine);
		trainReq2Group.add(helper2Service);
		
		// engine options
		engine2Option.setLayout(new GridBagLayout());
		
    	for (int i=0; i<Setup.getEngineSize()+1; i++){
    		numEngines2Box.addItem(Integer.toString(i));
    	}
    	numEngines2Box.setMinimumSize(new Dimension(50,20));
    	modelEngine2Box.insertItemAt("",0);
    	modelEngine2Box.setSelectedIndex(0);
    	modelEngine2Box.setMinimumSize(new Dimension(120,20));
    	modelEngine2Box.setToolTipText(rb.getString("ModelEngineTip"));
    	roadEngine2Box.insertItemAt("",0);
    	roadEngine2Box.setSelectedIndex(0);
    	roadEngine2Box.setMinimumSize(new Dimension(120,20));
    	roadEngine2Box.setToolTipText(rb.getString("RoadEngineTip"));
		addItem (engine2Option, new JLabel(rb.getString("ChangeEnginesAt")), 0, 0);
		addItem (engine2Option, routePickup2Box, 1, 0);
    	addItem (engine2Option, new JLabel(rb.getString("Engines")), 2, 0);
    	addItem (engine2Option, numEngines2Box, 3, 0);
    	addItem (engine2Option, new JLabel(rb.getString("Model")), 4, 0);
    	addItem (engine2Option, modelEngine2Box, 5, 0);
    	addItem (engine2Option, new JLabel(rb.getString("Road")), 6, 0);
    	addItem (engine2Option, roadEngine2Box, 7, 0);
    	panelTrainReq2.add(engine2Option);
    	
    	// caboose options
    	//roadCaboose2Box.setBorder(BorderFactory.createTitledBorder(rb.getString("Caboose")));
    	roadCaboose2Box.setMinimumSize(new Dimension(120,20));
    	roadCaboose2Box.setToolTipText(rb.getString("RoadCabooseTip"));
    	addItem (engine2caboose, keep2Caboose, 2, 6);
    	addItem (engine2caboose, change2Caboose, 3, 6);
     	addItem (engine2caboose, new JLabel(rb.getString("Road")), 5, 6);
     	addItem (engine2caboose, roadCaboose2Box, 6, 6);
       	panelTrainReq2.add(engine2caboose);
    	
    	cabooseOption2Group.add(keep2Caboose);
    	cabooseOption2Group.add(change2Caboose);
    	
    	// drop engine panel
    	addItem(engine2DropOption, new JLabel(rb.getString("DropEnginesAt")), 0 , 0);
    	addItem(engine2DropOption, routeDrop2Box, 1, 0);
    	panelTrainReq2.add(engine2DropOption);
		
		// row 15 buttons
	   	JPanel pB = new JPanel();
    	pB.setLayout(new GridBagLayout());		
		addItem(pB, saveTrainButton, 3, 0);
		
		getContentPane().add(p1);
		getContentPane().add(pOption);
		getContentPane().add(roadPane);
		getContentPane().add(loadPane);
		getContentPane().add(ownerPane);
		getContentPane().add(builtPane);
		getContentPane().add(trainReq1Pane);
		getContentPane().add(trainReq2Pane);
      	getContentPane().add(pB);
		
		// setup buttons
		addButtonAction(deleteRoadButton);
		addButtonAction(addRoadButton);
		addButtonAction(deleteLoadButton);
		addButtonAction(deleteAllLoadsButton);
		addButtonAction(addLoadButton);
		addButtonAction(deleteOwnerButton);
		addButtonAction(addOwnerButton);
		addButtonAction(saveTrainButton);
		
		addRadioButtonAction(roadNameAll);
		addRadioButtonAction(roadNameInclude);
		addRadioButtonAction(roadNameExclude);
		addRadioButtonAction(loadNameAll);
		addRadioButtonAction(loadNameInclude);
		addRadioButtonAction(loadNameExclude);
		addRadioButtonAction(ownerNameAll);
		addRadioButtonAction(ownerNameInclude);
		addRadioButtonAction(ownerNameExclude);
		addRadioButtonAction(builtDateAll);		
		addRadioButtonAction(builtDateAfter);
		addRadioButtonAction(builtDateBefore);
		addRadioButtonAction(builtDateRange);
		
		addRadioButtonAction(none1);
		addRadioButtonAction(change1Engine);
		addRadioButtonAction(helper1Service);
		addRadioButtonAction(keep1Caboose);
		addRadioButtonAction(change1Caboose);		
		addRadioButtonAction(none2);
		addRadioButtonAction(change2Engine);
		addRadioButtonAction(helper2Service);
		addRadioButtonAction(keep2Caboose);
		addRadioButtonAction(change2Caboose);
		
		addComboBoxAction(typeBox);
		
		addComboBoxAction(numEngines1Box);
		addComboBoxAction(modelEngine1Box);
		addComboBoxAction(numEngines2Box);
		addComboBoxAction(modelEngine2Box);
		
		if (_train != null){
			trainName.setText(_train.getName());
			trainDescription.setText(_train.getDescription());
			sendToTerminalcheckBox.setSelected(_train.isSendCarsToTerminalEnabled());
			sendToTerminalcheckBox.setText(MessageFormat.format(rb.getString("SendToTerminal"),new Object[] {_train.getTrainTerminatesName()}));
			builtAfterTextField.setText(_train.getBuiltStartYear());
			builtBeforeTextField.setText(_train.getBuiltEndYear());
			setBuiltRadioButton();
			enableButtons(true);
			// listen for train changes
			_train.addPropertyChangeListener(this);
		} else {
			enableButtons(false);
		}
		addHelpMenu("package.jmri.jmrit.operations.Operations_TrainOptions", true);
		updateRoadNames();
		updateTypeComboBoxes();
		updateLoadComboBoxes();
		updateLoadNames();
		updateOwnerNames();
		updateBuilt();
		updateTrainRequires1Option();
		updateTrainRequires2Option();

		// get notified if car roads, loads, and owners gets modified
		CarTypes.instance().addPropertyChangeListener(this);
		CarRoads.instance().addPropertyChangeListener(this);
		CarLoads.instance().addPropertyChangeListener(this);
		CarOwners.instance().addPropertyChangeListener(this);
		setVisible(true);
	}
	
	// Save
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (_train != null){
			if (ae.getSource() == saveTrainButton){
				log.debug("train save button actived");
				saveTrain();
			}
			if (ae.getSource() == addRoadButton){
				if(_train.addRoadName((String) roadBox.getSelectedItem()))
					updateRoadNames();
				selectNextItemComboBox(roadBox);
			}
			if (ae.getSource() == deleteRoadButton){
				if(_train.deleteRoadName((String) roadBox.getSelectedItem()))
					updateRoadNames();
				selectNextItemComboBox(roadBox);
			}
			if (ae.getSource() == addLoadButton){
				if(_train.addLoadName((String) loadBox.getSelectedItem()))
					updateLoadNames();
				selectNextItemComboBox(loadBox);
			}
			if (ae.getSource() == deleteLoadButton){
				if(_train.deleteLoadName((String) loadBox.getSelectedItem()))
					updateLoadNames();
				selectNextItemComboBox(loadBox);
			}
			if (ae.getSource() == deleteAllLoadsButton){
				deleteAllRoads();
			}
			if (ae.getSource() == addOwnerButton){
				if(_train.addOwnerName((String) ownerBox.getSelectedItem()))
					updateOwnerNames();
				selectNextItemComboBox(ownerBox);
			}
			if (ae.getSource() == deleteOwnerButton){
				if(_train.deleteOwnerName((String) ownerBox.getSelectedItem()))
					updateOwnerNames();
				selectNextItemComboBox(ownerBox);
			}
		}
	}
	
	public void radioButtonActionPerformed(java.awt.event.ActionEvent ae) {
		log.debug("radio button activated");
		if (_train != null){
			if (ae.getSource() == roadNameAll){
				_train.setRoadOption(Train.ALLROADS);
				updateRoadNames();
			}
			if (ae.getSource() == roadNameInclude){
				_train.setRoadOption(Train.INCLUDEROADS);
				updateRoadNames();
			}
			if (ae.getSource() == roadNameExclude){
				_train.setRoadOption(Train.EXCLUDEROADS);
				updateRoadNames();
			}
			if (ae.getSource() == loadNameAll){
				_train.setLoadOption(Train.ALLLOADS);
				updateLoadNames();
			}
			if (ae.getSource() == loadNameInclude){
				_train.setLoadOption(Train.INCLUDELOADS);
				updateLoadNames();
			}
			if (ae.getSource() == loadNameExclude){
				_train.setLoadOption(Train.EXCLUDELOADS);
				updateLoadNames();
			}
			if (ae.getSource() == ownerNameAll){
				_train.setOwnerOption(Train.ALLOWNERS);
				updateOwnerNames();
			}
			if (ae.getSource() == ownerNameInclude){
				_train.setOwnerOption(Train.INCLUDEOWNERS);
				updateOwnerNames();
			}
			if (ae.getSource() == ownerNameExclude){
				_train.setOwnerOption(Train.EXCLUDEOWNERS);
				updateOwnerNames();
			}
			if (ae.getSource() == builtDateAll ||
					ae.getSource() == builtDateAfter ||
					ae.getSource() == builtDateBefore ||
					ae.getSource() == builtDateRange ){
				updateBuilt();	
			}
			if (ae.getSource() == none1){
				_train.setSecondLegOptions(Train.NONE);
				updateTrainRequires1Option();
			}
			if (ae.getSource() == change1Engine){
				_train.setSecondLegOptions(Train.CHANGE_ENGINES);
				updateTrainRequires1Option();
			}
			if (ae.getSource() == helper1Service){
				_train.setSecondLegOptions(Train.HELPER_ENGINES);
				updateTrainRequires1Option();
			}
			if (ae.getSource() == keep1Caboose ||
					ae.getSource() == change1Caboose){
				roadCaboose1Box.setEnabled(change1Caboose.isSelected());
			}
			if (ae.getSource() == none2){
				_train.setThirdLegOptions(Train.NONE);
				updateTrainRequires2Option();
			}
			if (ae.getSource() == change2Engine){
				_train.setThirdLegOptions(Train.CHANGE_ENGINES);
				updateTrainRequires2Option();
			}
			if (ae.getSource() == helper2Service){
				_train.setThirdLegOptions(Train.HELPER_ENGINES);
				updateTrainRequires2Option();
			}
			if (ae.getSource() == keep2Caboose ||
					ae.getSource() == change2Caboose){
				roadCaboose2Box.setEnabled(change2Caboose.isSelected());
			}
		}
	}
	
	// Car type combo box has been changed, show loads associated with this car type
	public void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == typeBox){
			updateLoadComboBoxes();
		}
		if (ae.getSource() == numEngines1Box){
			modelEngine1Box.setEnabled(!numEngines1Box.getSelectedItem().equals("0"));
			roadEngine1Box.setEnabled(!numEngines1Box.getSelectedItem().equals("0"));
		}
		if (ae.getSource() == modelEngine1Box){
			updateEngineRoadComboBox(roadEngine1Box, (String)modelEngine1Box.getSelectedItem());
		}
		if (ae.getSource() == numEngines2Box){
			modelEngine2Box.setEnabled(!numEngines2Box.getSelectedItem().equals("0"));
			roadEngine2Box.setEnabled(!numEngines2Box.getSelectedItem().equals("0"));
		}
		if (ae.getSource() == modelEngine2Box){
			updateEngineRoadComboBox(roadEngine2Box, (String)modelEngine2Box.getSelectedItem());
		}
	}
	
	private void updateRoadNames(){
		panelRoadNames.removeAll();
		
    	JPanel p = new JPanel();
    	p.setLayout(new GridBagLayout());
    	p.add(roadNameAll, 0);
    	p.add(roadNameInclude, 1);
    	p.add(roadNameExclude, 2);
    	GridBagConstraints gc = new GridBagConstraints();
    	gc.gridwidth = 6;
    	panelRoadNames.add(p, gc);
		
		int y = 1;		// vertical position in panel

		if(_train != null){
			// set radio button
			roadNameAll.setSelected(_train.getRoadOption().equals(Train.ALLROADS));
			roadNameInclude.setSelected(_train.getRoadOption().equals(Train.INCLUDEROADS));
			roadNameExclude.setSelected(_train.getRoadOption().equals(Train.EXCLUDEROADS));
			
			if (!roadNameAll.isSelected()){
		    	p = new JPanel();
		    	p.setLayout(new FlowLayout());
		    	p.add(roadBox);
		    	p.add(addRoadButton);
		    	p.add(deleteRoadButton);
				gc.gridy = y++;
		    	panelRoadNames.add(p, gc);

		    	String[]carRoads = _train.getRoadNames();
		    	int x = 0;
		    	for (int i =0; i<carRoads.length; i++){
		    		JLabel road = new JLabel();
		    		road.setText(carRoads[i]);
		    		addItem(panelRoadNames, road, x++, y);
		    		if (x > 6){
		    			y++;
		    			x = 0;
		    		}
		    	}
			}
		} else {
			roadNameAll.setSelected(true);
		}
		panelRoadNames.revalidate();
		packFrame();
	}
	
	private void updateLoadNames(){
		panelLoadNames.removeAll();
		
    	JPanel p = new JPanel();
    	p.setLayout(new GridBagLayout());
    	p.add(loadNameAll, 0);
    	p.add(loadNameInclude, 1);
    	p.add(loadNameExclude, 2);
    	GridBagConstraints gc = new GridBagConstraints();
    	gc.gridwidth = 6;
    	panelLoadNames.add(p, gc);
		
		int y = 1;		// vertical position in panel

		if(_train != null){
			// set radio button
			loadNameAll.setSelected(_train.getLoadOption().equals(Train.ALLLOADS));
			loadNameInclude.setSelected(_train.getLoadOption().equals(Train.INCLUDEROADS));
			loadNameExclude.setSelected(_train.getLoadOption().equals(Train.EXCLUDEROADS));
			
			if (!loadNameAll.isSelected()){
		    	p = new JPanel();
		    	p.setLayout(new FlowLayout());
		    	p.add(typeBox);
		    	p.add(loadBox);
		    	p.add(addLoadButton);
		    	p.add(deleteLoadButton);
		    	p.add(deleteAllLoadsButton);
				gc.gridy = y++;
		    	panelLoadNames.add(p, gc);

		    	String[]carLoads = _train.getLoadNames();
		    	int x = 0;
		    	for (int i =0; i<carLoads.length; i++){
		    		JLabel load = new JLabel();
		    		load.setText(carLoads[i]);
		    		addItem(panelLoadNames, load, x++, y);
		    		if (x > 6){
		    			y++;
		    			x = 0;
		    		}
		    	}
			}
		} else {
			loadNameAll.setSelected(true);
		}
		panelLoadNames.revalidate();
		packFrame();
	}
	
	private void deleteAllRoads(){
		if(_train != null){
			String [] trainLoads = _train.getLoadNames();
			for (int i=0; i<trainLoads.length; i++){
				_train.deleteLoadName(trainLoads[i]);
			}
		}
		updateLoadNames();
	}
	
	private void updateOwnerNames(){
		panelOwnerNames.removeAll();
		
    	JPanel p = new JPanel();
    	p.setLayout(new GridBagLayout());
    	p.add(ownerNameAll, 0);
    	p.add(ownerNameInclude, 1);
    	p.add(ownerNameExclude, 2);
    	GridBagConstraints gc = new GridBagConstraints();
    	gc.gridwidth = 6;
    	panelOwnerNames.add(p, gc);
		
		int y = 1;		// vertical position in panel

		if(_train != null){
			// set radio button
			ownerNameAll.setSelected(_train.getOwnerOption().equals(Train.ALLOWNERS));
			ownerNameInclude.setSelected(_train.getOwnerOption().equals(Train.INCLUDEOWNERS));
			ownerNameExclude.setSelected(_train.getOwnerOption().equals(Train.EXCLUDEOWNERS));
			
			if (!ownerNameAll.isSelected()){
		    	p = new JPanel();
		    	p.setLayout(new FlowLayout());
		    	p.add(ownerBox);
		    	p.add(addOwnerButton);
		    	p.add(deleteOwnerButton);
				gc.gridy = y++;
		    	panelOwnerNames.add(p, gc);

		    	String[]carOwners = _train.getOwnerNames();
		    	int x = 0;
		    	for (int i =0; i<carOwners.length; i++){
		    		JLabel owner = new JLabel();
		    		owner.setText(carOwners[i]);
		    		addItem(panelOwnerNames, owner, x++, y);
		    		if (x > 6){
		    			y++;
		    			x = 0;
		    		}
		    	}
			}
		} else {
			ownerNameAll.setSelected(true);
		}
		panelOwnerNames.revalidate();
		packFrame();
	}
	
	private void setBuiltRadioButton(){
		if (_train.getBuiltStartYear().equals("") && _train.getBuiltEndYear().equals(""))
			builtDateAll.setSelected(true);
		else if (!_train.getBuiltStartYear().equals("") && !_train.getBuiltEndYear().equals(""))
			builtDateRange.setSelected(true);
		else if (!_train.getBuiltStartYear().equals(""))
			builtDateAfter.setSelected(true);
		else if (!_train.getBuiltEndYear().equals(""))
			builtDateBefore.setSelected(true);		
	}
	
	private void updateBuilt(){
		builtAfterTextField.setVisible(false);
		builtBeforeTextField.setVisible(false);
		after.setVisible(false);
		before.setVisible(false);
		if (builtDateAll.isSelected()){
			builtAfterTextField.setText("");
			builtBeforeTextField.setText("");
		}else if (builtDateAfter.isSelected()){
			builtBeforeTextField.setText("");
			builtAfterTextField.setVisible(true);
			after.setVisible(true);
		}else if (builtDateBefore.isSelected()){
			builtAfterTextField.setText("");
			builtBeforeTextField.setVisible(true);
			before.setVisible(true);
		}else if (builtDateRange.isSelected()){
			after.setVisible(true);
			before.setVisible(true);
			builtAfterTextField.setVisible(true);
			builtBeforeTextField.setVisible(true);
		}
		packFrame();
	}
	
	private void updateTrainRequires1Option(){
		none1.setSelected(true);
		if (_train != null){
			
			updateCabooseRoadComboBox(roadCaboose1Box);
			updateEngineRoadComboBox(roadEngine1Box, (String)modelEngine1Box.getSelectedItem());
			if (_train.getRoute() != null){
				_train.getRoute().updateComboBox(routePickup1Box);
				_train.getRoute().updateComboBox(routeDrop1Box);
			}

			change1Engine.setSelected((_train.getSecondLegOptions() & Train.CHANGE_ENGINES)>0);
			helper1Service.setSelected((_train.getSecondLegOptions() & Train.HELPER_ENGINES)>0);
			numEngines1Box.setSelectedItem(_train.getSecondLegNumberEngines());
			modelEngine1Box.setSelectedItem(_train.getSecondLegEngineModel());
			routePickup1Box.setSelectedItem(_train.getSecondLegStartLocation());
			routeDrop1Box.setSelectedItem(_train.getSecondLegEndLocation());
			roadEngine1Box.setSelectedItem(_train.getSecondLegEngineRoad());
			keep1Caboose.setSelected(true);
			change1Caboose.setSelected((_train.getSecondLegOptions() & Train.CHANGE_CABOOSE)>0);
			roadCaboose1Box.setEnabled(change1Caboose.isSelected());
			roadCaboose1Box.setSelectedItem(_train.getSecondLegCabooseRoad());
		} 
		engine1Option.setVisible(!none1.isSelected());
		engine1caboose.setVisible(change1Engine.isSelected() && (_train.getRequirements() & Train.CABOOSE)>0);
		engine1DropOption.setVisible(helper1Service.isSelected());
		engine1Option.setBorder(BorderFactory.createTitledBorder(rb.getString("EngineChange")));
		if (helper1Service.isSelected())
			engine1Option.setBorder(BorderFactory.createTitledBorder(rb.getString("AddHelpers")));
		packFrame();
	}
	
	private void updateTrainRequires2Option(){
		none2.setSelected(true);
		if (_train != null){
			
			updateCabooseRoadComboBox(roadCaboose2Box);
			updateEngineRoadComboBox(roadEngine2Box, (String)modelEngine2Box.getSelectedItem());
			if (_train.getRoute() != null){
				_train.getRoute().updateComboBox(routePickup2Box);
				_train.getRoute().updateComboBox(routeDrop2Box);
			}

			change2Engine.setSelected((_train.getThirdLegOptions() & Train.CHANGE_ENGINES)>0);
			helper2Service.setSelected((_train.getThirdLegOptions() & Train.HELPER_ENGINES)>0);
			numEngines2Box.setSelectedItem(_train.getThirdLegNumberEngines());
			modelEngine2Box.setSelectedItem(_train.getThirdLegEngineModel());
			routePickup2Box.setSelectedItem(_train.getThirdLegStartLocation());
			routeDrop2Box.setSelectedItem(_train.getThirdLegEndLocation());
			roadEngine2Box.setSelectedItem(_train.getThirdLegEngineRoad());
			keep2Caboose.setSelected(true);
			change2Caboose.setSelected((_train.getThirdLegOptions() & Train.CHANGE_CABOOSE)>0);
			roadCaboose2Box.setEnabled(change2Caboose.isSelected());
			roadCaboose2Box.setSelectedItem(_train.getThirdLegCabooseRoad());
		} 
		engine2Option.setVisible(!none2.isSelected());
		engine2caboose.setVisible(change2Engine.isSelected() && (_train.getRequirements() & Train.CABOOSE)>0);
		engine2DropOption.setVisible(helper2Service.isSelected());
		engine2Option.setBorder(BorderFactory.createTitledBorder(rb.getString("EngineChange")));
		if (helper2Service.isSelected())
			engine2Option.setBorder(BorderFactory.createTitledBorder(rb.getString("AddHelpers")));
		packFrame();
	}
	
	private void saveTrain(){
		if (!checkInput())
			return;
		_train.setSendCarsToTerminalEnabled(sendToTerminalcheckBox.isSelected());
		_train.setBuiltStartYear(builtAfterTextField.getText().trim());
		_train.setBuiltEndYear(builtBeforeTextField.getText().trim());
		
		if (change1Caboose.isSelected())
			_train.setSecondLegOptions(_train.getSecondLegOptions() | Train.CHANGE_CABOOSE);
		else if (change1Engine.isSelected())
			_train.setSecondLegOptions(Train.CHANGE_ENGINES);
		if (routePickup1Box.getSelectedItem() != null && !routePickup1Box.getSelectedItem().equals(""))
			_train.setSecondLegStartLocation((RouteLocation)routePickup1Box.getSelectedItem());
		else
			_train.setSecondLegStartLocation(null);
		if (routeDrop1Box.getSelectedItem() != null && !routeDrop1Box.getSelectedItem().equals(""))
			_train.setSecondLegEndLocation((RouteLocation)routeDrop1Box.getSelectedItem());
		else
			_train.setSecondLegEndLocation(null);
		_train.setSecondLegNumberEngines((String)numEngines1Box.getSelectedItem());
		_train.setSecondLegEngineModel((String)modelEngine1Box.getSelectedItem());
		_train.setSecondLegEngineRoad((String)roadEngine1Box.getSelectedItem());
		_train.setSecondLegCabooseRoad((String)roadCaboose1Box.getSelectedItem());
		
		if (change2Caboose.isSelected())
			_train.setThirdLegOptions(_train.getThirdLegOptions() | Train.CHANGE_CABOOSE);
		else if (change2Engine.isSelected())
			_train.setThirdLegOptions(Train.CHANGE_ENGINES);
		if (routePickup2Box.getSelectedItem() != null && !routePickup2Box.getSelectedItem().equals(""))
			_train.setThirdLegStartLocation((RouteLocation)routePickup2Box.getSelectedItem());
		else
			_train.setThirdLegStartLocation(null);
		if (routeDrop2Box.getSelectedItem() != null && !routeDrop2Box.getSelectedItem().equals(""))
			_train.setThirdLegEndLocation((RouteLocation)routeDrop2Box.getSelectedItem());
		else
			_train.setThirdLegEndLocation(null);
		_train.setThirdLegNumberEngines((String)numEngines2Box.getSelectedItem());
		_train.setThirdLegEngineModel((String)modelEngine2Box.getSelectedItem());
		_train.setThirdLegEngineRoad((String)roadEngine2Box.getSelectedItem());
		_train.setThirdLegCabooseRoad((String)roadCaboose2Box.getSelectedItem());
		
		manager.save();
		if (Setup.isCloseWindowOnSaveEnabled())
			dispose();
	}
	
	private boolean checkInput(){
		if ((!none1.isSelected() && (routePickup1Box.getSelectedItem() == null || routePickup1Box.getSelectedItem().equals("")))
				|| (!none2.isSelected() && (routePickup2Box.getSelectedItem() == null || routePickup2Box.getSelectedItem().equals("")))){
			JOptionPane.showMessageDialog(this,
					rb.getString("SelectLocationEngChange"), rb.getString("CanNotSave"),
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		if ((helper1Service.isSelected() && (routeDrop1Box.getSelectedItem() == null || routeDrop1Box.getSelectedItem().equals("")))
				|| (helper2Service.isSelected() && (routeDrop2Box.getSelectedItem() == null || routeDrop2Box.getSelectedItem().equals("")))){
			JOptionPane.showMessageDialog(this,
					rb.getString("SelectLocationEndHelper"), rb.getString("CanNotSave"),
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
	
	private void enableButtons(boolean enabled){
		//clearButton.setEnabled(enabled);
		//setButton.setEnabled(enabled);
		roadNameAll.setEnabled(enabled);
		roadNameInclude.setEnabled(enabled);
		roadNameExclude.setEnabled(enabled);
		loadNameAll.setEnabled(enabled);
		loadNameInclude.setEnabled(enabled);
		loadNameExclude.setEnabled(enabled);
		addRoadButton.setEnabled(enabled);
		deleteRoadButton.setEnabled(enabled);
		ownerNameAll.setEnabled(enabled);
		ownerNameInclude.setEnabled(enabled);
		ownerNameExclude.setEnabled(enabled);
		builtDateAll.setEnabled(enabled);
		builtDateAfter.setEnabled(enabled);
		builtDateBefore.setEnabled(enabled);
		builtDateRange.setEnabled(enabled);
		
		none1.setEnabled(enabled);
		change1Engine.setEnabled(enabled);
		helper1Service.setEnabled(enabled);
		none2.setEnabled(enabled);
		change2Engine.setEnabled(enabled);
		helper2Service.setEnabled(enabled);
		
		saveTrainButton.setEnabled(enabled);
	}
	
	private void updateTypeComboBoxes(){
		CarTypes.instance().updateComboBox(typeBox);
		// remove types not serviced by this train
		for (int i = typeBox.getItemCount()-1; i>=0; i--){
			String type = (String)typeBox.getItemAt(i);
			if (_train != null && !_train.acceptsTypeName(type)){
				typeBox.removeItem(type);
			}
		}
	}
	
	private void updateRoadComboBoxes(){
		CarRoads.instance().updateComboBox(roadBox);
	}
	
	private void updateLoadComboBoxes(){
		String carType = (String)typeBox.getSelectedItem();
		CarLoads.instance().updateComboBox(carType, loadBox);
	}
	
	private void updateOwnerComboBoxes(){
		CarOwners.instance().updateComboBox(ownerBox);
	}
	
	// update caboose road box based on radio selection
	private void updateCabooseRoadComboBox(JComboBox box){
		box.removeAllItems();
		box.addItem("");
		List<String> roads = CarManager.instance().getCabooseRoadNames();
		for (int i=0; i<roads.size(); i++){
			box.addItem(roads.get(i));
		}
	}
	
	private void updateEngineRoadComboBox(JComboBox box, String engineModel){
		if (engineModel == null)
			return;
		box.removeAllItems();
		box.addItem("");
		List<String> roads = EngineManager.instance().getEngineRoadNames(engineModel);
		for (int i=0; i<roads.size(); i++){
			box.addItem(roads.get(i));
		}
	}
	
	/*
	private boolean checkModel(String model, String numberEngines){
		if (numberEngines.equals("0") || model.equals(""))
			return true;
		String type = EngineModels.instance().getModelType(model);
		if(_train.acceptsTypeName(type))
			return true;
		JOptionPane.showMessageDialog(this,
				MessageFormat.format(rb.getString("TrainModelService"), new Object[] {model, type}), MessageFormat.format(rb.getString("CanNot"),
						new Object[] {rb.getString("save")}),
				JOptionPane.ERROR_MESSAGE);
		return false;
	}
	*/
	
    private void packFrame(){
    	setPreferredSize(null);
    	setVisible(false);
 		pack();
 		validate();
 		if(getWidth()<550)
 			setSize(550, getHeight());
		setVisible(true);
    }
	
	public void dispose() {
		CarTypes.instance().removePropertyChangeListener(this);	
		CarRoads.instance().removePropertyChangeListener(this);	
		CarLoads.instance().removePropertyChangeListener(this);
		CarOwners.instance().removePropertyChangeListener(this);	
		if (_train != null){
			_train.removePropertyChangeListener(this);
		}
		_trainEditFrame.setChildFrame(null);
		super.dispose();
	}

 	public void propertyChange(java.beans.PropertyChangeEvent e) {
		if (Control.showProperty && log.isDebugEnabled()) log.debug("Property change " +e.getPropertyName()
				+ " old: "+e.getOldValue()+ " new: "+e.getNewValue());
		if (e.getPropertyName().equals(CarRoads.CARROADS_LENGTH_CHANGED_PROPERTY)){
			updateRoadComboBoxes();
			updateRoadNames();
		}
		if (e.getPropertyName().equals(CarLoads.LOAD_NAME_CHANGED_PROPERTY) ||
				e.getPropertyName().equals(CarLoads.LOAD_CHANGED_PROPERTY)){
			updateLoadComboBoxes();
			updateLoadNames();
		}
		if (e.getPropertyName().equals(CarOwners.CAROWNERS_LENGTH_CHANGED_PROPERTY)){
			updateOwnerComboBoxes();
			updateOwnerNames();
		}
		if (e.getPropertyName().equals(CarTypes.CARTYPES_LENGTH_CHANGED_PROPERTY) ||
				e.getPropertyName().equals(Train.TYPES_CHANGED_PROPERTY)){
			updateTypeComboBoxes();
		}
	}
 	
	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(TrainEditBuildOptionsFrame.class.getName());
}
