// EnginesTableFrame.java

 package jmri.jmrit.operations.rollingstock.engines;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;

import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.setup.Control;

/**
 * Frame for adding and editing the engine roster for operations.
 *
 * @author		Bob Jacobsen   Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2008
 * @version             $Revision: 1.9 $
 */
public class EnginesTableFrame extends OperationsFrame implements PropertyChangeListener{
	
	static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.rollingstock.engines.JmritOperationsEnginesBundle");

	EnginesTableModel enginesModel = new EnginesTableModel();
	javax.swing.JTable enginesTable = new javax.swing.JTable(enginesModel);
	JScrollPane enginesPane;
	
	// labels
	javax.swing.JLabel numEngines = new javax.swing.JLabel();
	javax.swing.JLabel textEngines = new javax.swing.JLabel();
	javax.swing.JLabel textSort = new javax.swing.JLabel(rb.getString("SortBy"));
	javax.swing.JLabel textSep1 = new javax.swing.JLabel("          ");
	javax.swing.JLabel textSep2 = new javax.swing.JLabel();
	
	// radio buttons	
    javax.swing.JRadioButton sortByNumber = new javax.swing.JRadioButton(rb.getString("Number"));
    javax.swing.JRadioButton sortByRoad = new javax.swing.JRadioButton(rb.getString("Road"));
    javax.swing.JRadioButton sortByModel = new javax.swing.JRadioButton(rb.getString("Model"));
    javax.swing.JRadioButton sortByConsist = new javax.swing.JRadioButton(rb.getString("Consist"));
    javax.swing.JRadioButton sortByLocation = new javax.swing.JRadioButton(rb.getString("Location"));
    javax.swing.JRadioButton sortByDestination = new javax.swing.JRadioButton(rb.getString("Destination"));
    javax.swing.JRadioButton sortByTrain = new javax.swing.JRadioButton(rb.getString("Train"));
    javax.swing.JRadioButton sortByMoves = new javax.swing.JRadioButton(rb.getString("Moves"));
    JRadioButton sortByBuilt = new JRadioButton(rb.getString("Built"));
    JRadioButton sortByOwner = new JRadioButton(rb.getString("Owner"));
    ButtonGroup group = new ButtonGroup();
    
	// major buttons
	javax.swing.JButton addButton = new javax.swing.JButton(rb.getString("Add"));
	javax.swing.JButton findButton = new javax.swing.JButton(rb.getString("Find"));
	
	javax.swing.JTextField findEngineTextBox = new javax.swing.JTextField(6);

    public EnginesTableFrame() {
        super(ResourceBundle.getBundle("jmri.jmrit.operations.rollingstock.engines.JmritOperationsEnginesBundle").getString("TitleEnginesTable"));
        // general GUI config

        getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));

    	// Set up the jtable in a Scroll Pane..
    	enginesPane = new JScrollPane(enginesTable);
    	enginesPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
       	enginesModel.initTable(enginesTable);
     	
       	// load the number of engines and listen for changes
     	numEngines.setText(Integer.toString(EngineManager.instance().getNumEntries()));
    	EngineManager.instance().addPropertyChangeListener(this);
    	textEngines.setText(rb.getString("engines"));

    	// Set up the control panel
    	
    	//row 1
    	JPanel cp1 = new JPanel();
    	
    	cp1.add(textSort);
    	cp1.add(sortByNumber);
    	cp1.add(sortByRoad);
    	cp1.add(sortByModel);
    	cp1.add(sortByConsist);
    	cp1.add(sortByLocation);
    	cp1.add(sortByDestination);
    	cp1.add(sortByTrain);
    	cp1.add(sortByMoves);
       	cp1.add(sortByBuilt);
    	cp1.add(sortByOwner);

       	// row 2
    	JPanel cp2 = new JPanel();
		findButton.setToolTipText(rb.getString("findEngine"));
		findEngineTextBox.setToolTipText(rb.getString("findEngine"));
		
    	cp2.add(numEngines);
    	cp2.add(textEngines);
    	cp2.add(textSep1); 	
		cp2.add (addButton);
		cp2.add (findButton);
		cp2.add (findEngineTextBox);
				
		// place controls in scroll pane
		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new GridBagLayout());
		addItem(controlPanel, cp1, 0, 0 );
		addItem(controlPanel, cp2, 0, 1);
		
	    JScrollPane controlPane = new JScrollPane(controlPanel);
	    // make sure panel doesn't get too short
	    controlPane.setMinimumSize(new Dimension(50,90));
	    controlPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		
    	getContentPane().add(enginesPane);
	   	getContentPane().add(controlPane);
	   	
		// setup buttons
		addButtonAction(addButton);
		addButtonAction(findButton);
		
	   	sortByNumber.setSelected(true);
		addRadioButtonAction (sortByNumber);
		addRadioButtonAction (sortByRoad);
		addRadioButtonAction (sortByModel);
		addRadioButtonAction (sortByConsist);
		addRadioButtonAction (sortByLocation);
		addRadioButtonAction (sortByDestination);
		addRadioButtonAction (sortByTrain);
		addRadioButtonAction (sortByMoves);
		addRadioButtonAction (sortByBuilt);
		addRadioButtonAction (sortByOwner);
		
		group.add(sortByNumber);
		group.add(sortByRoad);
		group.add(sortByModel);
		group.add(sortByConsist);
		group.add(sortByLocation);
		group.add(sortByDestination);
		group.add(sortByTrain);
		group.add(sortByMoves);
		group.add(sortByBuilt);
		group.add(sortByOwner);
    	
 		// build menu
		JMenuBar menuBar = new JMenuBar();
		JMenu toolMenu = new JMenu("Tools");
		toolMenu.add(new EngineRosterMenu("Roster", EngineRosterMenu.MAINMENU, this));
		toolMenu.add(new NceConsistEngineAction(rb.getString("MenuItemNceSync"), this));
		menuBar.add(toolMenu);
		setJMenuBar(menuBar);
    	addHelpMenu("package.jmri.jmrit.operations.Operations_Engines", true);
    	
    	pack();
    	if ((getWidth()<Control.panelWidth)) setSize(Control.panelWidth, getHeight());
    	
    }
    
	public void radioButtonActionPerformed(java.awt.event.ActionEvent ae) {
		log.debug("radio button actived");
		if (ae.getSource() == sortByNumber){
			enginesModel.setSort(enginesModel.SORTBYNUMBER);
		}
		if (ae.getSource() == sortByRoad){
			enginesModel.setSort(enginesModel.SORTBYROAD);
		}
		if (ae.getSource() == sortByModel){
			enginesModel.setSort(enginesModel.SORTBYTYPE);
		}
		if (ae.getSource() == sortByConsist){
			enginesModel.setSort(enginesModel.SORTBYCONSIST);
		}
		if (ae.getSource() == sortByLocation){
			enginesModel.setSort(enginesModel.SORTBYLOCATION);
		}
		if (ae.getSource() == sortByDestination){
			enginesModel.setSort(enginesModel.SORTBYDESTINATION);
		}
		if (ae.getSource() == sortByTrain){
			enginesModel.setSort(enginesModel.SORTBYTRAIN);
		}
		if (ae.getSource() == sortByMoves){
			enginesModel.setSort(enginesModel.SORTBYMOVES);
		}
		if (ae.getSource() == sortByBuilt){
			enginesModel.setSort(enginesModel.SORTBYBUILT);
		}
		if (ae.getSource() == sortByOwner){
			enginesModel.setSort(enginesModel.SORTBYOWNER);
		}
	}
	
	public List<String> getSortByList(){
		return enginesModel.getSelectedEngineList();
	}
    
	EngineEditFrame f = null;
	
	// add or find button
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
//		log.debug("engine button actived");
		if (ae.getSource() == findButton){
			int rowindex = enginesModel.findEngineByRoadNumber(findEngineTextBox.getText());
			if (rowindex < 0){
				JOptionPane.showMessageDialog(this,
						"Engine with road number "+ findEngineTextBox.getText()+ " not found", "Could not find engine!",
						JOptionPane.INFORMATION_MESSAGE);
				return;
				
			}else{
				enginesTable.changeSelection(rowindex, 0, false, false);
			}
			return;
		}
		if (ae.getSource() == addButton){
			if (f != null)
				f.dispose();
			f = new EngineEditFrame();
			f.initComponents();
			f.setTitle(rb.getString("TitleEngineAdd"));
			f.setVisible(true);
		}
	}

    public void dispose() {
    	enginesModel.dispose();
    	if (f != null)
    		f.dispose();
        super.dispose();
    }
    
    public void propertyChange(PropertyChangeEvent e) {
    	if(Control.showProperty && log.isDebugEnabled()) log.debug("Property change " +e.getPropertyName()+ " old: "+e.getOldValue()+ " new: "+e.getNewValue());
    	if (e.getPropertyName().equals(EngineManager.LISTLENGTH_CHANGED_PROPERTY)) {
    		numEngines.setText(Integer.toString(EngineManager.instance().getNumEntries()));
    	}
    }
    
	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(EnginesTableFrame.class.getName());
}
