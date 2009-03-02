// PrintTrainsAction.java

package jmri.jmrit.operations.trains;

import jmri.util.davidflanagan.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

import javax.swing.*;

import java.util.List;
import java.util.ResourceBundle;


/**
 * Action to print a summary of the Roster contents
 * <P>
 * This uses the older style printing, for compatibility with Java 1.1.8 in
 * Macintosh MRJ
 *
 * @author	Bob Jacobsen   Copyright (C) 2003
 * @author  Dennis Miller  Copyright (C) 2005
 * @author Daniel Boudreau Copyright (C) 2009
 * @version     $Revision: 1.1 $
 */
public class PrintTrainsAction  extends AbstractAction {
	
	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.trains.JmritOperationsTrainsBundle");
	String newLine = "\n";
	TrainManager manager = TrainManager.instance();
	public static final int MAX_NAME_LENGTH = 15;

    public PrintTrainsAction(String actionName, Frame frame, boolean preview, Component pWho) {
        super(actionName);
        mFrame = frame;
        isPreview = preview;
        panel = (TrainsTableFrame)pWho;
    }

    /**
     * Frame hosting the printing
     */
    Frame mFrame;
    /**
     * Variable to set whether this is to be printed or previewed
     */
    boolean isPreview;
    TrainsTableFrame panel;
    

    public void actionPerformed(ActionEvent e) {

        // obtain a HardcopyWriter to do this
        HardcopyWriter writer = null;
        try {
            writer = new HardcopyWriter(mFrame, rb.getString("TitleTrainsTable"), 10, .5, .5, .5, .5, isPreview);
        } catch (HardcopyWriter.PrintCanceledException ex) {
            log.debug("Print cancelled");
            return;
        }
  
        // Loop through the Roster, printing as needed
       
        List trains = panel.getSortByList();
        
        try {
        	String s = rb.getString("Name") + "\t\t"
					+ rb.getString("Description") + "\t" 
					+ rb.getString("Route") + "\t\t"
					+ rb.getString("Departs") + "\t\t"
					+ rb.getString("Time") + "  "
					+ rb.getString("Terminates") + "\t"
					+ newLine;
        	writer.write(s, 0, s.length());
        	for (int i=0; i<trains.size(); i++){
        		Train train = manager.getTrainById((String)trains.get(i));
        		String name = train.getName();
        		name = truncate(name);
           		String desc = train.getDescription();
        		desc = truncate(desc);
           		String route = train.getTrainRouteName();
        		route = truncate(route);
        		String departs = train.getTrainDepartsName();
        		departs = truncate(departs);
        		String terminates = train.getTrainTerminatesName();
        		terminates = truncate(terminates); 
     
         		s = name + " " + desc + " "	+ route + " "
								+ departs + " "
								+ train.getDepartureTime() + " "
								+ terminates + newLine;
        		writer.write(s, 0, s.length());		
        	}
        	
        	// and force completion of the printing
        	writer.close();
        } catch (IOException we) {
        	log.error("Error printing ConsistRosterEntry: " + e);
        }
    }
    
    private String truncate (String string){
		string = string.trim();
		if (string.length()>MAX_NAME_LENGTH)
			string = string.substring(0, MAX_NAME_LENGTH);
		// pad out the string
		for (int j=string.length(); j < MAX_NAME_LENGTH; j++) {
			string += " ";
		}
		return string;
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PrintTrainsAction.class.getName());
}
