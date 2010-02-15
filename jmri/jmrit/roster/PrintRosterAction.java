// PrintRosterAction.java

package jmri.jmrit.roster;

import jmri.util.davidflanagan.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import java.util.List;


/**
 * Action to print a summary of the Roster contents
 * <P>
 * This uses the older style printing, for compatibility with Java 1.1.8 in
 * Macintosh MRJ
 *
 * @author	Bob Jacobsen   Copyright (C) 2003
 * @author  Dennis Miller  Copyright (C) 2005
 * @version     $Revision: 1.9 $
 */
public class PrintRosterAction  extends AbstractAction {

    public PrintRosterAction(String actionName, Frame frame, boolean preview) {
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
    

    public void actionPerformed(ActionEvent e) {

        // obtain a HardcopyWriter to do this
        Roster r = Roster.instance();
        String title = "DecoderPro Roster";
        String rosterGroup = Roster.getRosterGroup();
        if(rosterGroup==null){
            title = title + " All Entries";
        } else {
            title = title + " Group " + rosterGroup + " Entires";
        }
        HardcopyWriter writer = null;
        try {
            writer = new HardcopyWriter(mFrame, title, 10, .5, .5, .5, .5, isPreview);
        } catch (HardcopyWriter.PrintCanceledException ex) {
            log.debug("Print cancelled");
            return;
        }

        // add the image
        ImageIcon icon = new ImageIcon(ClassLoader.getSystemResource("resources/decoderpro.gif"));
        // we use an ImageIcon because it's guaranteed to have been loaded when ctor is complete
        writer.write(icon.getImage(), new JLabel(icon));

        // Loop through the Roster, printing as needed
        List<RosterEntry> l = r.matchingList(null, null, null, null, null, null, null); // take all
        int i=-1;
        log.debug("Roster list size: "+l.size());
        for (i = 0; i<l.size(); i++) {
            if(rosterGroup!=null){
                if(l.get(i).getAttribute(Roster.getRosterGroupWP())!=null){
                    if(l.get(i).getAttribute(Roster.getRosterGroupWP()).equals("yes"))
                        l.get(i).printEntry(writer);
                }
            }
            else
                l.get(i).printEntry(writer);
        }

        // and force completion of the printing
        writer.close();
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PrintRosterAction.class.getName());
}
