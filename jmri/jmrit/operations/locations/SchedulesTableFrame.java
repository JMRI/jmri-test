// SchedulesTableFrame.java

package jmri.jmrit.operations.locations;
 
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.OperationsFrame;

import java.awt.Dimension;
import java.awt.FlowLayout;
//import java.awt.Frame;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.swing.BoxLayout;
//import javax.swing.JMenu;
//import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * Frame for adding and editing the Schedule roster for operations.
 *
 * @author		Bob Jacobsen   Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2009
 * @version             $Revision: 1.4 $
 */
public class SchedulesTableFrame extends OperationsFrame {
	
	static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.locations.JmritOperationsLocationsBundle");

	SchedulesTableModel schedulesModel = new SchedulesTableModel();
	javax.swing.JTable schedulesTable = new javax.swing.JTable(schedulesModel);
	JScrollPane schedulesPane;
	
	// labels
	javax.swing.JLabel textSort = new javax.swing.JLabel();
	javax.swing.JLabel textSep = new javax.swing.JLabel();
	
	// radio buttons
    javax.swing.JRadioButton sortByName = new javax.swing.JRadioButton(rb.getString("Name"));
    javax.swing.JRadioButton sortById = new javax.swing.JRadioButton(rb.getString("Id"));

	// major buttons
	javax.swing.JButton addButton = new javax.swing.JButton();

    public SchedulesTableFrame() {
        super(ResourceBundle.getBundle("jmri.jmrit.operations.locations.JmritOperationsLocationsBundle").getString("TitleSchedulesTable"));
        // general GUI config

        getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));

    	// Set up the jtable in a Scroll Pane..
    	schedulesPane = new JScrollPane(schedulesTable);
    	schedulesPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    	schedulesPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
       	schedulesModel.initTable(schedulesTable);
     	getContentPane().add(schedulesPane);
     	
     	// Set up the control panel
    	JPanel controlPanel = new JPanel();
    	controlPanel.setLayout(new FlowLayout());
    	
    	textSort.setText("Sort by");
    	controlPanel.add(textSort);
    	controlPanel.add(sortByName);
    	sortByName.setSelected(true);
    	controlPanel.add(sortById);
    	textSep.setText("          ");
    	controlPanel.add(textSep);

		addButton.setText(rb.getString("Add"));
		addButton.setVisible(true);
		// TODO allow user to add schedule to a siding
		//controlPanel.add (addButton);
		controlPanel.setMaximumSize(new Dimension(Control.panelWidth, 50));
	   	getContentPane().add(controlPanel);
	   	
		// setup buttons
		addButtonAction(addButton);
		
		addRadioButtonAction (sortByName);
		addRadioButtonAction (sortById);
    	
		//	build menu
		//JMenuBar menuBar = new JMenuBar();
		//JMenu toolMenu = new JMenu("Tools");
		//Frame newFrame = new Frame();
		//toolMenu.add(new PrintSchedulesAction(rb.getString("MenuItemPrint"), newFrame, false, this));
		//toolMenu.add(new PrintSchedulesAction(rb.getString("MenuItemPreview"), newFrame, true, this));
		//menuBar.add(toolMenu);
		//setJMenuBar(menuBar);
    	addHelpMenu("package.jmri.jmrit.operations.Operations_Schedules", true);
    	
    	pack();
    	if ((getWidth()<800)) setSize(800, getHeight());
    	
    }
    
	public void radioButtonActionPerformed(java.awt.event.ActionEvent ae) {
		log.debug("radio button actived");
		if (ae.getSource() == sortByName){
			sortByName.setSelected(true);
			sortById.setSelected(false);
			schedulesModel.setSort(schedulesModel.SORTBYNAME);
		}
		if (ae.getSource() == sortById){
			sortByName.setSelected(false);
			sortById.setSelected(true);
			schedulesModel.setSort(schedulesModel.SORTBYID);
		}
	}
    
	// add button
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
//		log.debug("add schedule button activated");
		if (ae.getSource() == addButton){
			ScheduleEditFrame f = new ScheduleEditFrame();
			f.setTitle(MessageFormat.format(rb.getString("TitleScheduleAdd"), new Object[]{"Track Name"}));
			f.initComponents(null, null, null);
			f.setVisible(true);
		}
	}

    public void dispose() {
    	schedulesModel.dispose();
        super.dispose();
    }
    
	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(SchedulesTableFrame.class.getName());
}
