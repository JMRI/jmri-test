// TrainsTableFrame.java

package jmri.jmrit.operations.trains;
 
import jmri.jmrit.operations.locations.LocationManagerXml;
import jmri.jmrit.operations.rollingstock.cars.CarManagerXml;
import jmri.jmrit.operations.rollingstock.engines.EngineManagerXml;
import jmri.jmrit.operations.routes.RouteManagerXml;
import jmri.jmrit.operations.setup.Control;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ResourceBundle;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JCheckBox;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import jmri.util.JmriJFrame;

/**
 * Frame for adding and editing the route roster for operations.
 *
 * @author		Bob Jacobsen   Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2008
 * @version             $Revision: 1.10 $
 */
public class TrainsTableFrame extends JmriJFrame {
	
	static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.trains.JmritOperationsTrainsBundle");
	public static final String NAME = "Name";	// Sort by choices
	public static final String TIME = "Time";
	public static final String ID = "Id";

	CarManagerXml carMangerXml = CarManagerXml.instance();			
	EngineManagerXml engineMangerXml = EngineManagerXml.instance();
	TrainManager trainManager = TrainManager.instance();
	TrainManagerXml trainManagerXml = TrainManagerXml.instance();
	LocationManagerXml locationManagerXml = LocationManagerXml.instance();
	RouteManagerXml routeManagerXml = RouteManagerXml.instance();

	
	TrainsTableModel trainsModel = new TrainsTableModel();
	javax.swing.JTable trainsTable = new javax.swing.JTable(trainsModel);
	JScrollPane trainsPane;
	
	// labels
	javax.swing.JLabel textSort = new javax.swing.JLabel();
	javax.swing.JLabel textSep1 = new javax.swing.JLabel();
	javax.swing.JLabel textSep2 = new javax.swing.JLabel();
	
	// radio buttons
    javax.swing.JRadioButton sortByName = new javax.swing.JRadioButton(NAME);
    javax.swing.JRadioButton sortByTime = new javax.swing.JRadioButton(TIME);
    javax.swing.JRadioButton sortById = new javax.swing.JRadioButton(ID);
     

	// major buttons
	javax.swing.JButton addButton = new javax.swing.JButton();
	javax.swing.JButton buildButton = new javax.swing.JButton();
	javax.swing.JButton printButton = new javax.swing.JButton();
	javax.swing.JButton printSwitchButton = new javax.swing.JButton();
	javax.swing.JButton terminateButton = new javax.swing.JButton();
	javax.swing.JButton saveButton = new javax.swing.JButton();
	
	// check boxes
	javax.swing.JCheckBox buildReportBox = new javax.swing.JCheckBox();
	javax.swing.JCheckBox printPreviewBox = new javax.swing.JCheckBox();

    public TrainsTableFrame() {
        super(ResourceBundle.getBundle("jmri.jmrit.operations.trains.JmritOperationsTrainsBundle").getString("TitleTrainsTable"));
        // general GUI config

        getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));

    	// Set up the jtable in a Scroll Pane..
    	trainsPane = new JScrollPane(trainsTable);
    	trainsPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    	trainsPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
       	trainsModel.initTable(trainsTable, this);
     	getContentPane().add(trainsPane);
     	     	
     	// Set up the control panel
    	JPanel controlPanel = new JPanel();
    	controlPanel.setLayout(new FlowLayout());
     	
    	textSort.setText("Sort by");
    	controlPanel.add(textSort);
    	controlPanel.add(sortByName);
    	sortByName.setSelected(true);
    	controlPanel.add(sortByTime);
    	controlPanel.add(sortById);
    	ButtonGroup sortGroup = new ButtonGroup();
    	sortGroup.add(sortByName);
    	sortGroup.add(sortByTime);
    	sortGroup.add(sortById);
    	
    	textSep1.setText("          ");
    	controlPanel.add(textSep1);
    	
    	buildReportBox.setText(rb.getString("BuildReport"));
    	buildReportBox.setSelected(trainManager.getBuildReport());
    	controlPanel.add(buildReportBox);
    	printPreviewBox.setText(rb.getString("PrintPreview"));
    	printPreviewBox.setSelected(trainManager.getPrintPreview());
    	controlPanel.add(printPreviewBox);
    	textSep2.setText("          ");
    	controlPanel.add(textSep2);

    	addButton.setText(rb.getString("Add"));
    	addButton.setToolTipText(rb.getString("AddTrain"));
		addButton.setVisible(true);
		buildButton.setText(rb.getString("Build"));
		buildButton.setToolTipText(rb.getString("BuildSelected"));
		buildButton.setVisible(true);
		printButton.setText(rb.getString("Print"));
		printButton.setToolTipText(rb.getString("PrintSelected"));
		printButton.setVisible(true);
		printSwitchButton.setText(rb.getString("SwitchLists"));
		printSwitchButton.setToolTipText(rb.getString("PreviewPrintSwitchLists"));
		printSwitchButton.setVisible(true);
		terminateButton.setText(rb.getString("Terminate"));
		terminateButton.setToolTipText(rb.getString("TerminateSelected"));
		terminateButton.setVisible(true);
		saveButton.setText(rb.getString("SaveBuilds"));
		saveButton.setToolTipText(rb.getString("SaveBuildsTip"));
		saveButton.setVisible(true);
		controlPanel.add (addButton);
		controlPanel.add (buildButton);
		controlPanel.add (printButton);
		controlPanel.add (printSwitchButton);
		controlPanel.add (terminateButton);
		controlPanel.add (saveButton);
		controlPanel.setMaximumSize(new Dimension(Control.panelWidth, 50));
		
	   	getContentPane().add(controlPanel);
	   	
		// setup buttons
		addButtonAction(addButton);
		addButtonAction(buildButton);
		addButtonAction(printButton);
		addButtonAction(printSwitchButton);
		addButtonAction(terminateButton);
		addButtonAction(saveButton);
		
		addRadioButtonAction(sortByName);
		addRadioButtonAction(sortByTime);
		addRadioButtonAction(sortById);
		
		addCheckBoxAction(buildReportBox);
		addCheckBoxAction(printPreviewBox);
    	
        // add help menu to window
    	addHelpMenu("package.jmri.jmrit.operations.Operations_Trains", true);
    	
    	pack();
    	setSize(trainManager.getTrainFrameSize());
    	setLocation(trainManager.getTrainFramePosition());
    	setSortBy(trainManager.getTrainFrameSortBy());
    }
    
	private void addRadioButtonAction(JRadioButton b) {
		b.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				radioButtonActionPerformed(e);
			}
		});
	}
	
	public void radioButtonActionPerformed(java.awt.event.ActionEvent ae) {
		log.debug("radio button actived");
		if (ae.getSource() == sortByName){
			trainsModel.setSort(trainsModel.SORTBYNAME);
		}
		if (ae.getSource() == sortById){
			trainsModel.setSort(trainsModel.SORTBYID);
		}
		if (ae.getSource() == sortByTime){
			trainsModel.setSort(trainsModel.SORTBYTIME);
		}
	}
    
	private void addButtonAction(JButton b) {
		b.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				buttonActionPerformed(e);
			}
		});
	}
	
	// add, build, print, switch lists, terminate, and save buttons
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
//		log.debug("train button actived");
		if (ae.getSource() == addButton){
			TrainEditFrame f = new TrainEditFrame();
			f.initComponents(null);
			f.setTitle("Add Train");
			f.setVisible(true);
		}
		if (ae.getSource() == buildButton){
			List trains = getTrainList();
			for (int i=0; i<trains.size(); i++){
				Train train = trainManager.getTrainById((String)trains.get(i));
				boolean build = train.buildIfSelected();
				if (build)
					setModifiedFlag(true);
			}
		}
		if (ae.getSource() == printButton){
			List trains = getTrainList();
			for (int i=0; i<trains.size(); i++){
				Train train = trainManager.getTrainById((String)trains.get(i));
				train.printIfSelected();
			}
		}
		if (ae.getSource() == printSwitchButton){
			TrainSwitchListEditFrame f = new TrainSwitchListEditFrame();
			f.initComponents();
			f.setTitle("Switchlists by location");
			f.setVisible(true);
		}
		if (ae.getSource() == terminateButton){
			List trains = getTrainList();
			for (int i=0; i<trains.size(); i++){
				Train train = trainManager.getTrainById((String)trains.get(i));
				train.terminateIfSelected();
			}
		}
		
		if (ae.getSource() == saveButton){
			storeValues();
		}
	}
	
	private List getTrainList(){
		List trains;
		if (sortById.isSelected())
			trains = trainManager.getTrainsByIdList();
		else if (sortByTime.isSelected())
			trains = trainManager.getTrainsByTimeList();
		else
			trains = trainManager.getTrainsByNameList();
		return trains;
	}
	
	private void setSortBy(String sortBy){
		if(sortBy.equals(TIME)){
			sortByTime.setSelected(true);
			trainsModel.setSort(trainsModel.SORTBYTIME);
		}
		if(sortBy.equals(ID)){
			sortById.setSelected(true);
			trainsModel.setSort(trainsModel.SORTBYID);
		}
	}
	
	private void addCheckBoxAction(JCheckBox b) {
		b.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				checkBoxActionPerformed(e);
			}
		});
	}
	
	public void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
		setModifiedFlag(true);
		if (ae.getSource() == buildReportBox){
			trainManager.setBuildReport(buildReportBox.isSelected());
		}
		if (ae.getSource() == printPreviewBox){
			trainManager.setPrintPreview(printPreviewBox.isSelected());
		}
	}
	
	protected void storeValues(){
		trainManager.setTrainFrame(this);					//save frame size and location
		String sortBy = NAME;
		if (sortById.isSelected())
			sortBy = ID;
		else if (sortByTime.isSelected())
			sortBy = TIME;
		trainManager.setTrainFrameSortBy(sortBy);					//save how the table is sorted
		engineMangerXml.writeOperationsEngineFile();		//Need to save train assignments
		carMangerXml.writeOperationsCarFile();				//Need to save train assignments
		trainManagerXml.writeOperationsTrainFile();			//Need to save train status
		locationManagerXml.writeOperationsLocationFile();	//Need to save "moves" for track loc 
		routeManagerXml.writeOperationsRouteFileIfDirty(); 	//Only if user used setX&Y
		setModifiedFlag(false);
	}

    public void dispose() {
    	trainsModel.dispose();
    	trainManager.setTrainFrame(null);
        super.dispose();
    }
    
	static org.apache.log4j.Category log = org.apache.log4j.Category
	.getInstance(TrainsTableFrame.class.getName());
}
