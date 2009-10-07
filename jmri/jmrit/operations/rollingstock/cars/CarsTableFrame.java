// CarsTableFrame.java

 package jmri.jmrit.operations.rollingstock.cars;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import javax.swing.event.TableModelListener; 
import javax.swing.event.TableModelEvent;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;

/**
 * Frame for adding and editing the car roster for operations.
 *
 * @author		Bob Jacobsen   Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2008
 * @version             $Revision: 1.16 $
 */
public class CarsTableFrame extends OperationsFrame implements TableModelListener{
	
	static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.rollingstock.cars.JmritOperationsCarsBundle");

	CarsTableModel carsModel;
	JTable carsTable;
	boolean showAllCars;
	String locationName;
	String trackName;
		
	// labels
	JLabel numCars = new JLabel();
	JLabel textCars = new JLabel(rb.getString("cars"));
	JLabel textSort = new JLabel(rb.getString("SortBy"));
	JLabel textSep1 = new JLabel("      ");
	
	// radio buttons
	
    JRadioButton sortByNumber = new JRadioButton(rb.getString("Number"));
    JRadioButton sortByRoad = new JRadioButton(rb.getString("Road"));
    JRadioButton sortByType = new JRadioButton(rb.getString("Type"));
    JRadioButton sortByColor = new JRadioButton(rb.getString("Color"));
    JRadioButton sortByLoad = new JRadioButton(rb.getString("Load"));
    JRadioButton sortByKernel = new JRadioButton(rb.getString("Kernel"));
    JRadioButton sortByLocation = new JRadioButton(rb.getString("Location"));
    JRadioButton sortByDestination = new JRadioButton(rb.getString("Destination"));
    JRadioButton sortByTrain = new JRadioButton(rb.getString("Train"));
    JRadioButton sortByMoves = new JRadioButton(rb.getString("Moves"));
    JRadioButton sortByBuilt = new JRadioButton(rb.getString("Built"));
    JRadioButton sortByOwner = new JRadioButton(rb.getString("Owner"));
    JRadioButton sortByRfid = new JRadioButton(rb.getString("Rfid"));
    ButtonGroup group = new ButtonGroup();
    
	// major buttons
	JButton addButton = new JButton(rb.getString("Add"));
	JButton findButton = new JButton(rb.getString("Find"));
	
	JTextField findCarTextBox = new JTextField(6);

    public CarsTableFrame(boolean showAllCars, String locationName, String trackName) {
        super(ResourceBundle.getBundle("jmri.jmrit.operations.rollingstock.cars.JmritOperationsCarsBundle").getString("TitleCarsTable"));
        this.showAllCars = showAllCars;
        this.locationName = locationName;
        this.trackName = trackName;
        // general GUI configuration
        getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));

    	// Set up the table in a Scroll Pane..
        carsModel = new CarsTableModel(showAllCars, locationName, trackName);
        carsTable = new JTable(carsModel);
        JScrollPane carsPane = new JScrollPane(carsTable);
    	carsPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
       	carsModel.initTable(carsTable);
     	
    	// load the number of cars and listen for changes
       	updateNumCars();
    	carsModel.addTableModelListener(this);
    	
    	// Set up the control panel
    	
    	//row 1
    	JPanel cp1 = new JPanel();
    	cp1.add(textSort);
    	cp1.add(sortByNumber);
    	cp1.add(sortByRoad);
    	cp1.add(sortByType);
    	cp1.add(sortByColor);
    	cp1.add(sortByLoad);
    	cp1.add(sortByKernel);
    	cp1.add(sortByLocation);
    	cp1.add(sortByDestination);
    	cp1.add(sortByTrain);
    	cp1.add(sortByMoves);
    	cp1.add(sortByBuilt);
    	cp1.add(sortByOwner);
    	if(Setup.isRfidEnabled()){
    		cp1.add(sortByRfid);
    	}
    	
    	// row 2
    	JPanel cp2 = new JPanel();
		findButton.setToolTipText(rb.getString("findCar"));
		findCarTextBox.setToolTipText(rb.getString("findCar"));
		
    	cp2.add(numCars);
    	cp2.add(textCars); 
    	cp2.add(textSep1);
		cp2.add(addButton);	
		cp2.add(findButton);
		cp2.add(findCarTextBox);	
		
		// place controls in scroll pane
		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new GridBagLayout());
		addItem(controlPanel, cp1, 0, 0 );
		addItem(controlPanel, cp2, 0, 1);
		
	    JScrollPane controlPane = new JScrollPane(controlPanel);
	    // make sure panel doesn't get too short
	    controlPane.setMinimumSize(new Dimension(50,90));
	    controlPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		
    	getContentPane().add(carsPane);
	   	getContentPane().add(controlPane);
	   	
		// setup buttons
		addButtonAction(addButton);
		addButtonAction(findButton);
		
    	sortByNumber.setSelected(true);
		addRadioButtonAction (sortByNumber);
		addRadioButtonAction (sortByRoad);
		addRadioButtonAction (sortByType);
		addRadioButtonAction (sortByColor);
		addRadioButtonAction (sortByLoad);
		addRadioButtonAction (sortByKernel);
		addRadioButtonAction (sortByLocation);
		addRadioButtonAction (sortByDestination);
		addRadioButtonAction (sortByTrain);
		addRadioButtonAction (sortByMoves);
		addRadioButtonAction (sortByBuilt);
		addRadioButtonAction (sortByOwner);
		addRadioButtonAction (sortByRfid);
		
		group.add(sortByNumber);
		group.add(sortByRoad);
		group.add(sortByType);
		group.add(sortByColor);
		group.add(sortByLoad);
		group.add(sortByKernel);
		group.add(sortByLocation);
		group.add(sortByDestination);
		group.add(sortByTrain);
		group.add(sortByMoves);
		group.add(sortByBuilt);
		group.add(sortByOwner);
		group.add(sortByRfid);
		
		// sort by location
		if (!showAllCars){
			sortByLocation.doClick();
			if (locationName != null){
				String title = rb.getString("TitleCarsTable") +" "+ locationName;
				if (trackName != null){
					title = title + " "+trackName;
				}
				setTitle(title);
			}
		}
    	
 		// build menu
		JMenuBar menuBar = new JMenuBar();
		JMenu toolMenu = new JMenu(rb.getString("Tools"));
		toolMenu.add(new CarRosterMenu("Roster", CarRosterMenu.MAINMENU, this));
		menuBar.add(toolMenu);
		setJMenuBar(menuBar);
    	addHelpMenu("package.jmri.jmrit.operations.Operations_Cars", true);
    	
    	pack();
    	if ((getWidth()<Control.panelWidth)) setSize(Control.panelWidth, getHeight());
    	
    }
    
	public void radioButtonActionPerformed(java.awt.event.ActionEvent ae) {
		log.debug("radio button actived");
		if (ae.getSource() == sortByNumber){
			carsModel.setSort(carsModel.SORTBYNUMBER);
		}
		if (ae.getSource() == sortByRoad){
			carsModel.setSort(carsModel.SORTBYROAD);
		}
		if (ae.getSource() == sortByType){
			carsModel.setSort(carsModel.SORTBYTYPE);
		}
		if (ae.getSource() == sortByColor){
			carsModel.setSort(carsModel.SORTBYCOLOR);
		}
		if (ae.getSource() == sortByLoad){
			carsModel.setSort(carsModel.SORTBYLOAD);
		}
		if (ae.getSource() == sortByKernel){
			carsModel.setSort(carsModel.SORTBYKERNEL);
		}
		if (ae.getSource() == sortByLocation){
			carsModel.setSort(carsModel.SORTBYLOCATION);
		}
		if (ae.getSource() == sortByDestination){
			carsModel.setSort(carsModel.SORTBYDESTINATION);
		}
		if (ae.getSource() == sortByTrain){
			carsModel.setSort(carsModel.SORTBYTRAIN);
		}
		if (ae.getSource() == sortByMoves){
			carsModel.setSort(carsModel.SORTBYMOVES);
		}
		if (ae.getSource() == sortByBuilt){
			carsModel.setSort(carsModel.SORTBYBUILT);
		}
		if (ae.getSource() == sortByOwner){
			carsModel.setSort(carsModel.SORTBYOWNER);
		}
		if (ae.getSource() == sortByRfid){
			carsModel.setSort(carsModel.SORTBYRFID);
		}
	}
	
	public List<String> getSortByList(){
		return carsModel.getSelectedCarList();
	}
    
	CarEditFrame f = null;
	
	// add or find button
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
//		log.debug("car button activated");
		if (ae.getSource() == findButton){
			int rowindex = carsModel.findCarByRoadNumber(findCarTextBox.getText());
			if (rowindex < 0){
				JOptionPane.showMessageDialog(this,
						"Car with road number "+ findCarTextBox.getText()+ " not found", "Could not find car!",
						JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			carsTable.changeSelection(rowindex, 0, false, false);
			return;
		}
		if (ae.getSource() == addButton){
			if (f != null)
				f.dispose();
			f = new CarEditFrame();
			f.initComponents();
			f.setTitle(rb.getString("TitleCarAdd"));
			f.setVisible(true);
		}
	}

    public void dispose() {
    	carsModel.removeTableModelListener(this);
    	carsModel.dispose();
    	if (f != null)
    		f.dispose();
        super.dispose();
    }
    
    public void tableChanged(TableModelEvent e){
    	if(Control.showProperty && log.isDebugEnabled()) log.debug("Table changed");
    	updateNumCars();
    }
    
    private void updateNumCars(){
    	String totalNumber = Integer.toString(CarManager.instance().getNumEntries());
    	if (showAllCars){
    		numCars.setText(totalNumber);
    		return;
    	}
    	String showNumber = Integer.toString(getSortByList().size());
       	numCars.setText(showNumber + "/" + totalNumber);
    }
    
	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(CarsTableFrame.class.getName());
}
