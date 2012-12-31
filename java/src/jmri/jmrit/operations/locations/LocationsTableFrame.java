// LocationsTableFrame.java

package jmri.jmrit.operations.locations;
 
import jmri.jmrit.operations.rollingstock.cars.CarManagerXml;
import jmri.jmrit.operations.rollingstock.engines.EngineManagerXml;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.OperationsFrame;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;



/**
 * Frame for adding and editing the location roster for operations.
 *
 * @author		Bob Jacobsen   Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2008
 * @version             $Revision$
 */
public class LocationsTableFrame extends OperationsFrame {
	
	LocationsTableModel locationsModel = new LocationsTableModel();
	javax.swing.JTable locationsTable = new javax.swing.JTable(locationsModel);
	JScrollPane locationsPane;
	
	// labels
	JLabel textSort = new JLabel(Bundle.getString("SortBy"));
	JLabel textSep = new JLabel("          ");
	
	// radio buttons
    javax.swing.JRadioButton sortByName = new javax.swing.JRadioButton(Bundle.getString("Name"));
    javax.swing.JRadioButton sortById = new javax.swing.JRadioButton(Bundle.getString("Id"));

	// major buttons
	JButton addButton = new JButton(Bundle.getString("Add"));

    public LocationsTableFrame() {
        // general GUI config

        getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));

    	// Set up the jtable in a Scroll Pane..
    	locationsPane = new JScrollPane(locationsTable);
    	locationsPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    	locationsPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
       	locationsModel.initTable(locationsTable);
     	getContentPane().add(locationsPane);
     	
     	// Set up the control panel
    	JPanel controlPanel = new JPanel();
    	controlPanel.setLayout(new FlowLayout());
    	
    	controlPanel.add(textSort);
    	controlPanel.add(sortByName);
    	controlPanel.add(sortById);
    	controlPanel.add(textSep);
		controlPanel.add (addButton);
		controlPanel.setMaximumSize(new Dimension(Control.panelWidth, 50));
	   	
		getContentPane().add(controlPanel);
	   	
    	sortByName.setSelected(true);
	   	
		// setup buttons
		addButtonAction(addButton);
		
		addRadioButtonAction (sortByName);
		addRadioButtonAction (sortById);
    	
		//	build menu
		JMenuBar menuBar = new JMenuBar();
		JMenu toolMenu = new JMenu(Bundle.getString("Tools"));
		toolMenu.add(new SchedulesTableAction(Bundle.getString("Schedules")));
		toolMenu.add(new ModifyLocationsAction(Bundle.getString("TitleModifyLocations")));
		toolMenu.add(new ShowCarsByLocationAction(false, null, null));
		if (Setup.isVsdPhysicalLocationEnabled())
			toolMenu.add(new SetPhysicalLocationAction(Bundle.getString("MenuSetPhysicalLocation"), null));
		toolMenu.add(new PrintLocationsAction(Bundle.getString("MenuItemPrint"), new Frame(), false, this));
		toolMenu.add(new PrintLocationsAction(Bundle.getString("MenuItemPreview"), new Frame(), true, this));
		menuBar.add(toolMenu);
		menuBar.add(new jmri.jmrit.operations.OperationsMenu());
		setJMenuBar(menuBar);
    	addHelpMenu("package.jmri.jmrit.operations.Operations_Locations", true);	// NOI18N
    	
    	pack();
    	if ((getWidth()<670)) setSize(670, getHeight());
    	
     	// now load the cars and engines
    	CarManagerXml.instance();
    	EngineManagerXml.instance();
    }
    
	public void radioButtonActionPerformed(java.awt.event.ActionEvent ae) {
		log.debug("radio button activated");
		if (ae.getSource() == sortByName){
			sortByName.setSelected(true);
			sortById.setSelected(false);
			locationsModel.setSort(locationsModel.SORTBYNAME);
		}
		if (ae.getSource() == sortById){
			sortByName.setSelected(false);
			sortById.setSelected(true);
			locationsModel.setSort(locationsModel.SORTBYID);
		}
	}
    
	// add button
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
//		log.debug("location button activated");
		if (ae.getSource() == addButton){
			LocationEditFrame f = new LocationEditFrame();
			f.initComponents(null);
			f.setTitle(Bundle.getString("TitleLocationAdd"));
		}
	}

    public void dispose() {
    	locationsModel.dispose();
        super.dispose();
    }
    
	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(LocationsTableFrame.class.getName());
}
