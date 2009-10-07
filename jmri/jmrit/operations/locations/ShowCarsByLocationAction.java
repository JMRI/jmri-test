//ShowCarsByLocation.java

package jmri.jmrit.operations.locations;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import jmri.jmrit.operations.rollingstock.cars.CarsTableFrame;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register a CarsTableFrame object.
 * 
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2009
 * @version $Revision: 1.1 $
 */
public class ShowCarsByLocationAction extends AbstractAction {
	static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.locations.JmritOperationsLocationsBundle");

	public ShowCarsByLocationAction(String s) {
		super(s);
	}

	public ShowCarsByLocationAction(boolean showAllCars, String locationName, String trackName) {
		this(rb.getString("MenuItemShowCars"));
		this.showAllCars = showAllCars;
		this.locationName = locationName;
		this.trackName = trackName;
	}
	
	boolean showAllCars = true;
	String locationName = null;
	String trackName = null;

	public void actionPerformed(ActionEvent e) {
		// create a car table frame
		CarsTableFrame f = new CarsTableFrame(showAllCars, locationName, trackName);
		f.setVisible(true);
	}
}

/* @(#)ShowCarsByLocation.java */
