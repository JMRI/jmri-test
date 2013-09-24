// PrintRouteAction.java

package jmri.jmrit.operations.routes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.util.davidflanagan.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.text.MessageFormat;

import javax.swing.*;

import java.util.List;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Control;


/**
 * Action to print a summary of a route.
 * <P>
 * This uses the older style printing, for compatibility with Java 1.1.8 in
 * Macintosh MRJ
 *
 * @author	Bob Jacobsen   Copyright (C) 2003
 * @author  Dennis Miller  Copyright (C) 2005
 * @author Daniel Boudreau Copyright (C) 2009, 2012
 * @version     $Revision$
 */
public class PrintRouteAction  extends AbstractAction {
	
	static final String NEW_LINE = "\n"; // NOI18N
	static final String TAB = "\t"; // NOI18N
	private static final int MAX_NAME_LENGTH = Control.max_len_string_location_name - 5;

    public PrintRouteAction(String actionName, boolean preview, Route route) {
        super(actionName);
        isPreview = preview;
        this.route = route;
    }

    /**
     * Frame hosting the printing
     */
    Frame mFrame = new Frame();
    /**
     * Variable to set whether this is to be printed or previewed
     */
    boolean isPreview;
    Route route;
    

    public void actionPerformed(ActionEvent e) {
    	if (route == null)
    		return;

    	// obtain a HardcopyWriter to do this
    	HardcopyWriter writer = null;
    	try {
    		writer = new HardcopyWriter(mFrame, MessageFormat.format(Bundle.getMessage("TitleRoute"), new Object[] {route.getName()}), 10, .5, .5, .5, .5, isPreview);
    	} catch (HardcopyWriter.PrintCanceledException ex) {
    		log.debug("Print cancelled");
    		return;
    	}
    	printRoute(writer, route);
    	// and force completion of the printing
    	writer.close();
    }
    	
    protected void printRoute(HardcopyWriter writer, Route route) {
    	try {
    		writer.write(route.getComment()+NEW_LINE);
        	String s = Bundle.getMessage("Location") 
        	+ TAB +"    " + Bundle.getMessage("Direction") 
        	+ " " + Bundle.getMessage("MaxMoves") 
        	+ " " + Bundle.getMessage("Pickups")
        	+ " " + Bundle.getMessage("Drops")
        	+ " " + Bundle.getMessage("Wait")
        	+ TAB + Bundle.getMessage("Length")     	
        	+ TAB + Bundle.getMessage("Grade")
        	+ TAB + Bundle.getMessage("X")
        	+ "    " + Bundle.getMessage("Y")
        	+ NEW_LINE;
        	writer.write(s);
    		List<String> locations = route.getLocationsBySequenceList();
    		for (int i=0; i<locations.size(); i++){
    			RouteLocation rl = route.getLocationById(locations.get(i)); 
    			String name = rl.getName();
    			name = truncate(name);
    			String pad = " ";
    			if (rl.getTrainIconX() < 10)
    				pad = "    ";
    			else if (rl.getTrainIconX() < 100)
    				pad = "   ";
    			else if (rl.getTrainIconX() < 1000)
    				pad = "  ";
    			s = name 
    			+ TAB + rl.getTrainDirectionString() 
    			+ TAB + rl.getMaxCarMoves()
    			+ TAB + (rl.isPickUpAllowed()?Bundle.getMessage("yes"):Bundle.getMessage("no"))
    			+ TAB + (rl.isDropAllowed()?Bundle.getMessage("yes"):Bundle.getMessage("no"))
    			+ TAB + rl.getWait()
    			+ TAB + rl.getMaxTrainLength()
    			+ TAB + rl.getGrade()
    			+ TAB + rl.getTrainIconX()
    			+ pad + rl.getTrainIconY()
    			+ NEW_LINE;
    			writer.write(s);		
    		}
    		s = NEW_LINE + Bundle.getMessage("Location") 
        	+ TAB + Bundle.getMessage("DepartTime") 
        	+ TAB + Bundle.getMessage("Comment")
        	+ NEW_LINE;
    		writer.write(s);
    		for (int i=0; i<locations.size(); i++){
    			RouteLocation rl = route.getLocationById(locations.get(i)); 
    			String name = rl.getName();
    			name = truncate(name);
    			s = name 
    			+ TAB + rl.getDepartureTime()
    			+ TAB + rl.getComment()
    			+ NEW_LINE;
    			writer.write(s);
    		}
    	} catch (IOException we) {
    		log.error("Error printing route");
    	}
    }
    
    private String truncate (String string){
		string = string.trim();
		if (string.length()>MAX_NAME_LENGTH)
			string = string.substring(0, MAX_NAME_LENGTH);
		// pad out the string
		StringBuffer buf = new StringBuffer(string);
		for (int j=string.length(); j < MAX_NAME_LENGTH; j++) {
			buf.append(" ");
		}
		return buf.toString();
    }

    static Logger log = LoggerFactory.getLogger(PrintRouteAction.class.getName());
}
