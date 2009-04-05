// RouteCopyFrame.java

package jmri.jmrit.operations.routes;
 
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;

import java.awt.GridBagLayout;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import java.util.List;



/**
 * Frame for copying a route for operations.
 *
 * @author		Bob Jacobsen   Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2008
 * @version             $Revision: 1.5 $
 */
public class RouteCopyFrame extends OperationsFrame {
	
	static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.routes.JmritOperationsRoutesBundle");

	RoutesTableModel routesModel = new RoutesTableModel();
	javax.swing.JTable routesTable = new javax.swing.JTable(routesModel);
	JScrollPane routesPane;
	
	// labels
	javax.swing.JLabel textCopyRoute = new javax.swing.JLabel(rb.getString("CopyRoute"));
	javax.swing.JLabel textRouteName = new javax.swing.JLabel(rb.getString("RouteName"));
	
	// text field
	javax.swing.JTextField routeNameTextField = new javax.swing.JTextField(20);
	
	// check boxes
    javax.swing.JCheckBox invertCheckBox = new javax.swing.JCheckBox(rb.getString("Invert"));

	// major buttons
	javax.swing.JButton copyButton = new javax.swing.JButton(rb.getString("Copy"));
	
	// combo boxes
	javax.swing.JComboBox routeBox = RouteManager.instance().getComboBox();

    public RouteCopyFrame() {
        super(ResourceBundle.getBundle("jmri.jmrit.operations.routes.JmritOperationsRoutesBundle").getString("TitleRouteCopy"));
        // general GUI config

        getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
	    
        //      Set up the panels
    	JPanel p1 = new JPanel();
    	p1.setLayout(new GridBagLayout());
				
		// Layout the panel by rows
    	// row 1 textRouteName
		addItem(p1, textRouteName, 0, 1);
		addItemWidth(p1, routeNameTextField, 3, 1, 1);
    	
		// row 2
		addItem(p1, textCopyRoute, 0, 2);
		addItemWidth(p1, routeBox, 3, 1, 2);
		
		// row 4
		addItem(p1, invertCheckBox, 0, 4);
		addItem(p1, copyButton, 1, 4);
		
		getContentPane().add(p1);
    	
        // add help menu to window
    	addHelpMenu("package.jmri.jmrit.operations.Operations_Routes", true);
    	
    	pack();
    	setSize(getWidth()+20, getHeight()+20);
    	
    	// setup buttons
		addButtonAction(copyButton);
    }
    
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == copyButton){
			log.debug("copy route button actived");
			if (!checkName())
				return;
			RouteManager manager = RouteManager.instance();
			Route newRoute = manager.getRouteByName(routeNameTextField.getText());
			if (newRoute != null){
				reportRouteExists(rb.getString("add"));
				return;
			}
			if (routeBox.getSelectedItem() == null || routeBox.getSelectedItem().equals("")){
				reportRouteDoesNotExist();
				return;
			}
			Route oldRoute = (Route)routeBox.getSelectedItem();
			if (oldRoute == null){
				reportRouteDoesNotExist();
				return;
			}
			newRoute = manager.newRoute(routeNameTextField.getText());
			// now copy
			List<String> oldRouteLocations = oldRoute.getLocationsBySequenceList();
			if (!invertCheckBox.isSelected()){
				for (int i=0; i<oldRouteLocations.size(); i++){
					copyRouteLocation(oldRoute, newRoute, oldRouteLocations.get(i), null, invertCheckBox.isSelected());
				}
			// invert route order
			} else {
				for (int i=oldRouteLocations.size()-1; i>=0; i--){
					int y = i-1;
					if (y<0)
						y=0;
					copyRouteLocation(oldRoute, newRoute, (String)oldRouteLocations.get(i), (String)oldRouteLocations.get(y),invertCheckBox.isSelected());
				}
			}
			RouteEditFrame f = new RouteEditFrame();
			f.initComponents(newRoute);
			f.setTitle(rb.getString("TitleRouteEdit"));
			f.setVisible(true);
		}
	}
	
	LocationManager locationManager = LocationManager.instance();
	private void copyRouteLocation(Route oldRoute, Route newRoute, String id, String nextId, boolean invert){
		RouteLocation oldRl = oldRoute.getLocationById(id);
		RouteLocation oldNextRl = null;
		if (nextId != null)
			oldNextRl = oldRoute.getLocationById(nextId);
		Location l = locationManager.getLocationByName(oldRl.getName());
		RouteLocation newRl = newRoute.addLocation(l);
		// now copy the route location objects we want
		newRl.setMaxCarMoves(oldRl.getMaxCarMoves());
		if(!invert){
			newRl.setCanDrop(oldRl.canDrop());
			newRl.setCanPickup(oldRl.canPickup());
			newRl.setGrade(oldRl.getGrade());
			newRl.setTrainDirection(oldRl.getTrainDirection());
			newRl.setMaxTrainLength(oldRl.getMaxTrainLength());
		}else{
			// flip drops and pickups
			newRl.setCanDrop(oldRl.canPickup());
			newRl.setCanPickup(oldRl.canDrop());
			// invert train directions
			int oldDirection = oldRl.getTrainDirection();
			if (oldDirection == oldRl.NORTH)
				newRl.setTrainDirection(newRl.SOUTH);
			else if (oldDirection == oldRl.SOUTH)
				newRl.setTrainDirection(newRl.NORTH);
			else if (oldDirection == oldRl.EAST)
				newRl.setTrainDirection(newRl.WEST);
			else if (oldDirection == oldRl.WEST)
				newRl.setTrainDirection(oldRl.EAST);
			// get the max length between location
			newRl.setMaxTrainLength(oldNextRl.getMaxTrainLength());
		}
		newRl.setTrainIconX(oldRl.getTrainIconX());
		newRl.setTrainIconY(oldRl.getTrainIconY());
	}
	
	private void reportRouteExists(String s){
		log.info("Can not " + s + ", route already exists");
		JOptionPane.showMessageDialog(this,
				rb.getString("ReportExists"), MessageFormat.format(rb.getString("CanNotRoute"),new Object[]{s}),
				JOptionPane.ERROR_MESSAGE);
	}
	
	private void reportRouteDoesNotExist(){
		log.debug("route does not exist");
		JOptionPane.showMessageDialog(this,
				rb.getString("CopyRoute"), rb.getString("CopyRoute"),
				JOptionPane.ERROR_MESSAGE);
	}
	
	/**
	 * 
	 * @return true if name is less than 26 characters
	 */
	private boolean checkName(){
		if (routeNameTextField.getText().trim().equals(""))
			return false;
		if (routeNameTextField.getText().length() > 25){
			log.error("Route name must be less than 26 charaters");
			JOptionPane.showMessageDialog(this,
					rb.getString("RouteNameLess"), rb.getString("CanNotAddRoute"),
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

    public void dispose() {
    	routesModel.dispose();
        super.dispose();
    }
    
	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(RouteCopyFrame.class.getName());
}
