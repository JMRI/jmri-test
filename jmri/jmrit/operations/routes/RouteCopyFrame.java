// RouteCopyFrame.java

package jmri.jmrit.operations.routes;
 
import jmri.jmrit.operations.OperationsFrame;

import java.awt.GridBagLayout;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;



/**
 * Frame for copying a route for operations.
 *
 * @author		Bob Jacobsen   Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2008, 2010
 * @version             $Revision: 1.11 $
 */
public class RouteCopyFrame extends OperationsFrame {
	
	static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.routes.JmritOperationsRoutesBundle");

	RoutesTableModel routesModel = new RoutesTableModel();
	javax.swing.JTable routesTable = new javax.swing.JTable(routesModel);
	RouteManager routeManager = RouteManager.instance();
	JScrollPane routesPane;
	String routeName;
	
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
    	addHelpMenu("package.jmri.jmrit.operations.Operations_CopyRoute", true);
    	
    	pack();
    	setSize(getWidth()+20, getHeight()+20);
    	
    	// setup buttons
		addButtonAction(copyButton);
    }
    
    public void setRouteName(String routeName){
    	routeBox.setSelectedItem(routeManager.getRouteByName(routeName));
    }
    
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == copyButton){
			log.debug("copy route button actived");
			if (!checkName())
				return;

			Route newRoute = routeManager.getRouteByName(routeNameTextField.getText());
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
			
			// now copy
			newRoute = routeManager.copyRoute(oldRoute, routeNameTextField.getText(), invertCheckBox.isSelected());

			RouteEditFrame f = new RouteEditFrame();
			f.initComponents(newRoute);
			f.setTitle(rb.getString("TitleRouteEdit"));
			f.setVisible(true);
		}
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
		if (routeNameTextField.getText().trim().equals("")){
			JOptionPane.showMessageDialog(this,
					rb.getString("EnterRouteName"), rb.getString("EnterRouteName"),
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		if (routeNameTextField.getText().length() > 25){
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
