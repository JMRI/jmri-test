// PrintLocationsByCarTypesAction.java

package jmri.jmrit.operations.locations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;

import jmri.util.davidflanagan.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

import javax.swing.*;

import java.util.List;

/**
 * Action to print a summary of locations and tracks that service specific car types.
 * <P>
 * This uses the older style printing, for compatibility with Java 1.1.8 in Macintosh MRJ
 * 
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Dennis Miller Copyright (C) 2005
 * @author Daniel Boudreau Copyright (C) 2010
 * @version $Revision$
 */
public class PrintLocationsByCarTypesAction extends AbstractAction {

	static final String NEW_LINE = "\n"; // NOI18N
	static final String TAB = "\t"; // NOI18N

	LocationManager locManager = LocationManager.instance();

	public PrintLocationsByCarTypesAction(String actionName, Frame frame, boolean preview,
			Component pWho) {
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
			writer = new HardcopyWriter(mFrame, Bundle.getMessage("TitleLocationsByType"), 10, .5,
					.5, .5, .5, isPreview);
		} catch (HardcopyWriter.PrintCanceledException ex) {
			log.debug("Print cancelled");
			return;
		}

		// Loop through the car types showing which locations and tracks will
		// service that car type
		String carTypes[] = CarTypes.instance().getNames();

		List<Location> locations = locManager.getLocationsByNameList();

		try {
			// title line
			String s = Bundle.getMessage("Type") + TAB + Bundle.getMessage("Location") + TAB
					+ Bundle.getMessage("Track") + NEW_LINE;
			writer.write(s);
			// car types
			for (int t = 0; t < carTypes.length; t++) {
				s = carTypes[t] + NEW_LINE;
				writer.write(s);
				// locations
				for (int i = 0; i < locations.size(); i++) {
					Location location = locations.get(i);
					if (location.acceptsTypeName(carTypes[t])) {
						s = TAB + location.getName() + NEW_LINE;
						writer.write(s);
						// tracks
						List<Track> tracks = location.getTrackByNameList(null);
						for (Track track : tracks) {
							if (track.acceptsTypeName(carTypes[t])) {
								s = TAB + TAB + TAB + track.getName() + NEW_LINE;
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

	static Logger log = LoggerFactory
			.getLogger(PrintLocationsByCarTypesAction.class.getName());
}
