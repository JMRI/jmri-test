// RoutesEditFrame.java

package jmri.jmrit.operations.routes;

import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.OperationsFrame;

import java.awt.*;

import javax.swing.*;

import java.text.MessageFormat;
import java.util.ResourceBundle;


/**
 * Frame for user edit of route
 * 
 * @author Dan Boudreau Copyright (C) 2008, 2010
 * @version $Revision: 1.38 $
 */

public class RouteEditFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.routes.JmritOperationsRoutesBundle");
	
	RouteEditTableModel routeModel = new RouteEditTableModel();
	JTable routeTable = new JTable(routeModel);
	JScrollPane routePane;
	
	RouteManager manager;
	RouteManagerXml managerXml;
	LocationManager locationManager = LocationManager.instance();

	Route _route = null;
	RouteLocation _routeLocation = null;

	// major buttons
	JButton addLocationButton = new JButton(rb.getString("AddLocation"));
	JButton saveRouteButton = new JButton(rb.getString("SaveRoute"));
	JButton deleteRouteButton = new JButton(rb.getString("DeleteRoute"));
	JButton addRouteButton = new JButton(rb.getString("AddRoute"));

	// check boxes
	JCheckBox checkBox;
	
	// radio buttons
    JRadioButton addLocAtTop = new JRadioButton(rb.getString("Top"));
    JRadioButton addLocAtBottom = new JRadioButton(rb.getString("Bottom"));
    ButtonGroup group = new ButtonGroup();
    
    JRadioButton showWait = new JRadioButton(rb.getString("Wait"));
    JRadioButton showDepartTime = new JRadioButton(rb.getString("DepartTime"));
    ButtonGroup groupTime = new ButtonGroup();
	
	// text field
	JTextField routeNameTextField = new JTextField(20);
	JTextField commentTextField = new JTextField(35);
	
	// combo boxes
	JComboBox locationBox = LocationManager.instance().getComboBox();

	public static final String NAME = rb.getString("Name");
	public static final String DISPOSE = "dispose" ;

	public RouteEditFrame() {
		super();
	}

	public void initComponents(Route route) {
				
		_route = route;
		String routeName = null;

		// load managers
		manager = RouteManager.instance();
		managerXml = RouteManagerXml.instance();
		
	   	// Set up the jtable in a Scroll Pane..
    	routePane = new JScrollPane(routeTable);
    	routePane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    	routePane.setBorder(BorderFactory.createTitledBorder(""));
 		
		if (_route != null){
			routeName = _route.getName();
			routeNameTextField.setText(routeName);
			commentTextField.setText(_route.getComment());
	      	routeModel.initTable(routeTable, route);
	      	enableButtons(true);
		} else {
			enableButtons(false);
		}
		
	    getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));

	    //      Set up the panels
	    JPanel p1 = new JPanel();
	    p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
	    
	    // name panel
    	JPanel pName = new JPanel();
    	pName.setLayout(new GridBagLayout());
    	pName.setBorder(BorderFactory.createTitledBorder(rb.getString("Name")));
		addItem(pName, routeNameTextField, 1, 1);
		
		// comment panel
	   	JPanel pComment = new JPanel();
    	pComment.setLayout(new GridBagLayout());
    	pComment.setBorder(BorderFactory.createTitledBorder(rb.getString("Comment")));
		addItem(pComment, commentTextField, 0, 0);
		
		p1.add(pName);
		p1.add(pComment);
		
	    JPanel p2 = new JPanel();
	    p2.setLayout(new BoxLayout(p2, BoxLayout.X_AXIS));
		
		// location panel
    	JPanel pLoc = new JPanel();
    	pLoc.setLayout(new GridBagLayout());
    	pLoc.setBorder(BorderFactory.createTitledBorder(rb.getString("Location")));
    	addItem(pLoc, locationBox, 0, 1);
    	addItem(pLoc, addLocationButton, 1, 1);
    	addItem(pLoc, addLocAtTop, 2, 1);
    	addItem(pLoc, addLocAtBottom, 3, 1);
    	group.add(addLocAtTop);
    	group.add(addLocAtBottom);
    	addLocAtBottom.setSelected(true);
    	
    	// Wait or Depart Time panel
    	JPanel pWait = new JPanel();
    	pWait.setLayout(new GridBagLayout());
    	pWait.setBorder(BorderFactory.createTitledBorder(rb.getString("Display")));
    	addItem(pWait, showWait, 0, 1);
    	addItem(pWait, showDepartTime, 1, 1);
    	groupTime.add(showWait);
    	groupTime.add(showDepartTime);
    	showWait.setSelected(true);

    	p2.add(pLoc);
    	p2.add(pWait);
    	
		// row 12 buttons
    	JPanel pB = new JPanel();
    	pB.setLayout(new GridBagLayout());
    	pB.setBorder(BorderFactory.createTitledBorder(""));
		addItem(pB, deleteRouteButton, 0, 0);
		addItem(pB, addRouteButton, 1, 0);
		addItem(pB, saveRouteButton, 3, 0);
		
		getContentPane().add(p1);
       	getContentPane().add(routePane);
       	getContentPane().add(p2);
       	getContentPane().add(pB);
		
		// setup buttons
		addButtonAction(addLocationButton);
		addButtonAction(deleteRouteButton);
		addButtonAction(addRouteButton);
		addButtonAction(saveRouteButton);
		
		// setup radio buttons
		addRadioButtonAction(showWait);
		addRadioButtonAction(showDepartTime);

		//	build menu
		JMenuBar menuBar = new JMenuBar();
		JMenu toolMenu = new JMenu(rb.getString("Tools"));
		toolMenu.add(new PrintRouteAction(rb.getString("MenuItemPrint"), new Frame(), false, _route));
		toolMenu.add(new PrintRouteAction(rb.getString("MenuItemPreview"), new Frame(), true, _route));
		toolMenu.add(new RouteCopyAction(rb.getString("MenuItemCopy"), routeName));
		toolMenu.add(new SetTrainIconRouteAction(rb.getString("MenuSetTrainIconRoute"), routeName));
		menuBar.add(toolMenu);
		setJMenuBar(menuBar);
		addHelpMenu("package.jmri.jmrit.operations.Operations_EditRoute", true);

		//	 get notified if combo box gets modified
		LocationManager.instance().addPropertyChangeListener(this);
		
		// set frame size and route for display
		pack();
		setSize(Control.panelWidth, Control.panelHeight);
		setVisible(true);
	}
	
	// Save, Delete, Add 
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == addLocationButton){
			log.debug("route add location button actived");
			if (locationBox.getSelectedItem() != null){
				if (locationBox.getSelectedItem().equals(""))
					return;
				addNewRouteLocation();
			}
		}
		if (ae.getSource() == saveRouteButton){
			log.debug("route save button actived");
			Route route = manager.getRouteByName(routeNameTextField.getText());
			if (_route == null && route == null){
				saveNewRoute();
			} else {
				if (route != null && route != _route){
					reportRouteExists(rb.getString("save"));
					return;
				}
				saveRoute();
			}
		}
		if (ae.getSource() == deleteRouteButton){
			log.debug("route delete button actived");
			Route route = manager.getRouteByName(routeNameTextField.getText());
			if (route == null)
				return;
			
			manager.deregister(route);
			_route = null;

			enableButtons(false);
			routeModel.dispose();
			// save route file
			managerXml.writeOperationsFile();
		}
		if (ae.getSource() == addRouteButton){
			Route route = manager.getRouteByName(routeNameTextField.getText());
			if (route != null){
				reportRouteExists(rb.getString("add"));
				return;
			}
			saveNewRoute();
		}
	}
	
	public void radioButtonActionPerformed(java.awt.event.ActionEvent ae) {
		routeModel.setWait(showWait.isSelected());
	}
	
	private void addNewRouteLocation(){
		// add location to this route
		Location l = (Location)locationBox.getSelectedItem();
		RouteLocation rl;
		if (addLocAtTop.isSelected())
			rl = _route.addLocation(l,0);
		else
			rl =_route.addLocation(l);
		rl.setTrainDirection(routeModel.getLastTrainDirection());
		rl.setMaxTrainLength(routeModel.getLastMaxTrainLength());
		// set train icon location
		rl.setTrainIconCoordinates();
	}
	
	private void saveNewRoute(){
		if (!checkName(rb.getString("add")))
			return;
		Route route = manager.newRoute(routeNameTextField.getText());
		routeModel.initTable(routeTable, route);
		_route = route;
		// enable checkboxes
		enableButtons(true);
		saveRoute();
	}
	
	private void saveRoute (){
		if (!checkName(rb.getString("save")))
			return;
		_route.setName(routeNameTextField.getText());
		_route.setComment(commentTextField.getText());
		
		if(routeTable.isEditing()){
			log.debug("route table edit true");
			routeTable.getCellEditor().stopCellEditing();
		}

		// save route file
		managerXml.writeOperationsFile();
	}
	

	/**
	 * 
	 * @return true if name is less than 26 characters
	 */
	private boolean checkName(String s){
		if (routeNameTextField.getText().trim().equals("")){
			log.debug("Must enter a name for the route");
			JOptionPane.showMessageDialog(this,
					rb.getString("MustEnterName"), MessageFormat.format(rb.getString("CanNotRoute"), new Object[] {s}),
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		if (routeNameTextField.getText().length() > 25){
			log.error("Route name must be less than 26 charaters");
			JOptionPane.showMessageDialog(this,
					rb.getString("RouteNameLess"), MessageFormat.format(rb.getString("CanNotRoute"), new Object[] {s}),
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
	
	private void reportRouteExists(String s){
		log.info("Can not " + s + ", route already exists");
		JOptionPane.showMessageDialog(this,
				rb.getString("ReportExists"), MessageFormat.format(rb.getString("CanNotRoute"),new Object[]{s}),
				JOptionPane.ERROR_MESSAGE);
	}
	
	private void enableButtons(boolean enabled){
		locationBox.setEnabled(enabled);
		addLocationButton.setEnabled(enabled);
		addLocAtTop.setEnabled(enabled);
		addLocAtBottom.setEnabled(enabled);
		saveRouteButton.setEnabled(enabled);
		deleteRouteButton.setEnabled(enabled);
		routeTable.setEnabled(enabled);
		// the inverse!
		addRouteButton.setEnabled(!enabled);
	}
	
	public void dispose() {
		routeModel.dispose();
		super.dispose();
	}
	
	private void updateComboBoxes(){
		locationManager.updateComboBox(locationBox);
	}
	
 	public void propertyChange(java.beans.PropertyChangeEvent e) {
		if (log.isDebugEnabled()) log.debug("Property change " +e.getPropertyName()+ " old: "+e.getOldValue()+ " new: "+e.getNewValue());
		if (e.getPropertyName().equals(LocationManager.LISTLENGTH_CHANGED_PROPERTY)){
			updateComboBoxes();
		}
	}
 	
	static org.apache.log4j.Logger log = org.apache.log4j.Logger
			.getLogger(RouteEditFrame.class.getName());
}
