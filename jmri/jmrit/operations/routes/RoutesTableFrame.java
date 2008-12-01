// RoutesTableFrame.java

package jmri.jmrit.operations.routes;
 
import jmri.jmrit.operations.rollingstock.cars.CarManagerXml;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.OperationsFrame;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ResourceBundle;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;



/**
 * Frame for adding and editing the route roster for operations.
 *
 * @author		Bob Jacobsen   Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2008
 * @version             $Revision: 1.5 $
 */
public class RoutesTableFrame extends OperationsFrame {
	
	static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.routes.JmritOperationsRoutesBundle");

	RoutesTableModel routesModel = new RoutesTableModel();
	javax.swing.JTable routesTable = new javax.swing.JTable(routesModel);
	JScrollPane routesPane;
	
	// labels
	javax.swing.JLabel textSort = new javax.swing.JLabel();
	javax.swing.JLabel textSep = new javax.swing.JLabel();
	
	// radio buttons
    javax.swing.JRadioButton sortByName = new javax.swing.JRadioButton(rb.getString("Name"));
    javax.swing.JRadioButton sortById = new javax.swing.JRadioButton(rb.getString("Id"));

	// major buttons
	javax.swing.JButton addButton = new javax.swing.JButton();

    public RoutesTableFrame() {
        super(ResourceBundle.getBundle("jmri.jmrit.operations.routes.JmritOperationsRoutesBundle").getString("TitleRoutesTable"));
        // general GUI config

        getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));

    	// Set up the jtable in a Scroll Pane..
    	routesPane = new JScrollPane(routesTable);
    	routesPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
       	routesModel.initTable(routesTable);
     	getContentPane().add(routesPane);
     	
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
		controlPanel.add (addButton);
		controlPanel.setMaximumSize(new Dimension(Control.panelWidth, 50));
		
	   	getContentPane().add(controlPanel);
	   	
		// setup buttons
		addButtonAction(addButton);
		
		addRadioButtonAction (sortByName);
		addRadioButtonAction (sortById);
    	
        // add help menu to window
    	addHelpMenu("package.jmri.jmrit.operations.Operations_Routes", true);
    	
    	pack();
    	setSize(350, getHeight());
    	
    }
    
	public void radioButtonActionPerformed(java.awt.event.ActionEvent ae) {
		log.debug("radio button actived");
		if (ae.getSource() == sortByName){
			sortByName.setSelected(true);
			sortById.setSelected(false);
			routesModel.setSort(routesModel.SORTBYNAME);
		}
		if (ae.getSource() == sortById){
			sortByName.setSelected(false);
			sortById.setSelected(true);
			routesModel.setSort(routesModel.SORTBYID);
		}
	}
    
	// add button
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
//		log.debug("route button actived");
		if (ae.getSource() == addButton){
			RouteEditFrame f = new RouteEditFrame();
			f.initComponents(null);
			f.setTitle(rb.getString("TitleRouteAdd"));
			f.setVisible(true);
		}
	}

    public void dispose() {
    	routesModel.dispose();
        super.dispose();
    }
    
	static org.apache.log4j.Category log = org.apache.log4j.Category
	.getInstance(RoutesTableFrame.class.getName());
}
