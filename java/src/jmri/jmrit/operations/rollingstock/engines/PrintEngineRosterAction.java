// PrintEngineRosterAction.java

package jmri.jmrit.operations.rollingstock.engines;

import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
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
 * @author Daniel Boudreau Copyright (C) 2008, 2011
 * @version     $Revision$
 */
public class PrintEngineRosterAction  extends AbstractAction {
	
	private static final int numberCharPerLine = 90;
	final int ownerMaxLen = 4;	// Only show the first 4 characters of the owner's name
	
	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.rollingstock.engines.JmritOperationsEnginesBundle");
	
	EngineManager manager = EngineManager.instance();

    public PrintEngineRosterAction(String actionName, Frame frame, boolean preview, Component pWho) {
        super(actionName);
        mFrame = frame;
        isPreview = preview;
        panel = (EnginesTableFrame)pWho;
    }

    /**
     * Frame hosting the printing
     */
    Frame mFrame;
    /**
     * Variable to set whether this is to be printed or previewed
     */
    boolean isPreview;
    EnginesTableFrame panel;
    

    public void actionPerformed(ActionEvent e) {

        // obtain a HardcopyWriter to do this
        HardcopyWriter writer = null;
        try {
            writer = new HardcopyWriter(mFrame, rb.getString("TitleEngineRoster"), 10, .5, .5, .5, .5, isPreview);
        } catch (HardcopyWriter.PrintCanceledException ex) {
            log.debug("Print cancelled");
            return;
        }
        
        // Loop through the Roster, printing as needed
        String newLine = "\n";
        String number;       
        String road;
        String model;
        String type;
        String length;
        String owner = "";
        String consist = "";
        String built = "";
        String value = "";
        String rfid = "";
        String location;
 
        List<String> engines = panel.getSortByList();
        try {
        	// header
        	String s = rb.getString("Number") + "\t" + rb.getString("Road")
					+ "\t" + rb.getString("Model") + "\t     "
					+ rb.getString("Type") + "      " + rb.getString("Length")
					+ " " + (panel.sortByConsist.isSelected()?rb.getString("Consist")+"     ":rb.getString("Owner"))
					+ (panel.sortByValue.isSelected()?" " +padAttribute(Setup.getValueLabel(), Control.MAX_LEN_STRING_ATTRIBUTE):"")
					+ (panel.sortByRfid.isSelected()?" " +padAttribute(Setup.getRfidLabel(), Control.MAX_LEN_STRING_ATTRIBUTE):"")
					+ ((!panel.sortByValue.isSelected() && !panel.sortByRfid.isSelected())?" " +rb.getString("Built"):"")
					+ " " + rb.getString("Location")
					+ newLine;
        	writer.write(s);
        	for (int i=0; i<engines.size(); i++){
        		Engine engine = manager.getById(engines.get(i));
        		
        		// loco number
        		number = padAttribute(engine.getNumber().trim(), 7);     		
        		road = padAttribute(engine.getRoad().trim(), 7);     		
        		model = padAttribute(engine.getModel().trim(), Control.MAX_LEN_STRING_ATTRIBUTE);     		
        		type = padAttribute(engine.getType().trim(), Control.MAX_LEN_STRING_ATTRIBUTE);       		
      			length = padAttribute(engine.getLength().trim(), Control.MAX_LEN_STRING_LENGTH_NAME); 
        				
    			if (panel.sortByConsist.isSelected())
       				consist = padAttribute(engine.getConsistName().trim(), Control.MAX_LEN_STRING_ATTRIBUTE); 
    			else
    				owner = padAttribute(engine.getOwner().trim(), ownerMaxLen);
         		
    			if (panel.sortByValue.isSelected())
    				value = padAttribute(engine.getValue().trim(), Control.MAX_LEN_STRING_ATTRIBUTE);
    			else if (panel.sortByRfid.isSelected())
    				rfid = padAttribute(engine.getRfid().trim(), Control.MAX_LEN_STRING_ATTRIBUTE);
    			else
    				built = padAttribute(engine.getBuilt().trim(), Control.MAX_LEN_STRING_BUILT_NAME);
    			
        		location = "";
        		if (!engine.getLocationName().equals("")){
        			location = engine.getLocationName() + " - " + engine.getTrackName();
        		}
         		          		
				s = number + road + model + type + length + owner + consist+ value + rfid + built + location;			
    			if (s.length() > numberCharPerLine)
    				s = s.substring(0, numberCharPerLine);
        		writer.write(s+newLine);
        	}

        	// and force completion of the printing
        	writer.close();
        } catch (IOException we) {
        	log.error("Error printing ConsistRosterEntry: " + e);
        }
    }
    
    private String padAttribute(String attribute, int length){
			if (attribute.length() > length)
				attribute = attribute.substring(0, length);
			StringBuffer buf = new StringBuffer(attribute);
			for (int i=attribute.length(); i<length+1; i++)
   				buf.append(" ");
			return buf.toString(); 	
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PrintEngineRosterAction.class.getName());
}
