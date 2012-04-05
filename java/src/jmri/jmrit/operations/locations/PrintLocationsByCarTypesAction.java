// PrintLocationsByCarTypesAction.java

package jmri.jmrit.operations.locations;

import jmri.jmrit.operations.rollingstock.cars.CarTypes;

import jmri.util.davidflanagan.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

import javax.swing.*;

import java.util.List;
import java.util.ResourceBundle;


/**
 * Action to print a summary of locations and tracks that service specific
 * car types.
 * <P>
 * This uses the older style printing, for compatibility with Java 1.1.8 in
 * Macintosh MRJ
 *
 * @author	Bob Jacobsen   Copyright (C) 2003
 * @author  Dennis Miller  Copyright (C) 2005
 * @author Daniel Boudreau Copyright (C) 2010
 * @version     $Revision$
 */
public class PrintLocationsByCarTypesAction  extends AbstractAction {
	
	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.locations.JmritOperationsLocationsBundle");
	String newLine = "\n";
	LocationManager locManager = LocationManager.instance();

    public PrintLocationsByCarTypesAction(String actionName, Frame frame, boolean preview, Component pWho) {
        super(actionName);
        mFrame = frame;
        isPreview = preview;
    }

    /**
     * Frame hosting the printing
     */
    Frame mFrame;
    /**
     * Variable to set whether this is to be printed or previewed
     */
    boolean isPreview;
    HardcopyWriter writer;

	public void actionPerformed(ActionEvent e) {
		// obtain a HardcopyWriter
		try {
			writer = new HardcopyWriter(mFrame, rb.getString("TitleLocationsByType"), 10, .5, .5, .5, .5,
					isPreview);
		} catch (HardcopyWriter.PrintCanceledException ex) {
			log.debug("Print cancelled");
			return;
		}

		// Loop through the car types showing which locations and tracks will
		// service that car type
		String carTypes[] = CarTypes.instance().getNames();

		List<String> locations = locManager.getLocationsByNameList();

		try {
			// title line
			String s = rb.getString("Type") + "\t" + rb.getString("Location")
					+ "\t" + rb.getString("Track") + "\t" + newLine;
			writer.write(s);
			// car types
			for (int t = 0; t < carTypes.length; t++) {
				s = carTypes[t] + newLine;
				writer.write(s);
				// locations
				for (int i = 0; i < locations.size(); i++) {
					Location location = locManager.getLocationById(locations.get(i));
					if (location.acceptsTypeName(carTypes[t])) {
						s = "\t" + location.getName() + newLine;
						writer.write(s);
						// tracks
						List<String> tracks = location.getTrackIdsByNameList(null);
						for (int j = 0; j < tracks.size(); j++) {
							Track track = location.getTrackById(tracks.get(j));
							if (track.acceptsTypeName(carTypes[t])) {
								s = "\t\t\t" + track.getName() + newLine;
								writer.write(s);
							}
						}
					}
				}
			}
			// and force completion of the printing
			writer.close();
		} catch (IOException we) {
			log.error("Error printing PrintLocationAction: " + we);
		}
	}        	
 
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PrintLocationsByCarTypesAction.class.getName());
}
