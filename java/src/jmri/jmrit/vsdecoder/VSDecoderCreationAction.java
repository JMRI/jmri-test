package jmri.jmrit.vsdecoder;

/*
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
 * <P>
 *
 * @author			Mark Underwood Copyright (C) 2011
 * @version			$Revision$
 */

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import java.io.File;

/**
 * Create a new VSDecoder Pane.
 *
 * @author			Glen Oberhauser
 * @version     $Revision$
 */
public class VSDecoderCreationAction extends AbstractAction {

    /**
     * Constructor
     * @param s Name for the action.
     */
    public VSDecoderCreationAction(String s) {
        super(s);
    }

    public VSDecoderCreationAction() {
        //this(ThrottleBundle.bundle().getString("MenuItemNewThrottle"));
        this("Virtual Sound Decoder");
    }

    /**
     * The action is performed. Create a new ThrottleFrame.
     * @param e The event causing the action.
     */
    public void actionPerformed(ActionEvent e) {
	String fp = null, fn = null;
	log.debug("actionPerformed()");
    	VSDecoderFrame tf = new VSDecoderFrame();
	if (VSDecoderManager.instance().getVSDecoderPreferences().isAutoLoadingDefaultVSDFile()) {
	    // Force load of a VSD file
	    fp = VSDecoderManager.instance().getVSDecoderPreferences().getDefaultVSDFilePath();
	    fn = VSDecoderManager.instance().getVSDecoderPreferences().getDefaultVSDFileName();
	    log.debug("Loading VSD File: " + fp + File.separator + fn);
	    LoadVSDFileAction.loadVSDFile(fp, fn);
	    //f = new File(fp + File.separator + fn);
	    //LoadVSDFileAction.loadVSDFile(f);
	}
	tf.toFront();
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(VSDecoderCreationAction.class.getName());
}
