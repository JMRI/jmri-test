// MDI.java

package apps.gui3.mdi;

import jmri.util.swing.mdi.MdiMainFrame;

/**
 * The JMRI application for developing the 3rd GUI
 * <P>
 *
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 *
 * @author	Bob Jacobsen   Copyright 2003, 2004, 2007, 2009, 2010
 * @version     $Revision: 1.1 $
 */
public class MDI extends apps.gui3.Apps3 {

    protected void createMainFrame() {
        // create and populate main window
        mainFrame = new MdiMainFrame(nameString, "apps/demo");
    }
    
    // Main entry point
    public static void main(String args[]) {

        // do processing needed immediately, before
        // we attempt anything else
        preInit();
        
        // create the program object
        MDI app = new MDI();
        
        // do final post initialization processing
        app.postInit();
        
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MDI.class.getName());
}


