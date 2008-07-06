// PanelPro.java

package apps.PanelPro;

import apps.Apps;
import jmri.util.JmriJFrame;

import java.text.MessageFormat;

import javax.swing.JFrame;

/**
 * The JMRI program for creating control panels.
 * <P>
 * If an argument is provided at startup, it will be used as the name of
 * the configuration file.  Note that this is just the name, not the path;
 * the file is searched for in the usual way, first in the preferences tree and then in
 * xml/
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
 * <P>
 * @author	Bob Jacobsen   Copyright 2003
 * @version     $Revision: 1.20 $
 */
public class PanelPro extends Apps {

    PanelPro(JFrame p) {
        super(p);
    }

    protected String logo() {
        return "resources/PanelPro.gif";
    }

    protected String mainWindowHelpID() {
            return "package.apps.PanelPro.PanelPro";
    }

    protected String line1() {
        return MessageFormat.format(rb.getString("PanelProVersionCredit"),
                                new String[]{jmri.Version.name()});
    }

    protected String line2() {
        return "http://jmri.sf.net/PanelPro ";
    }

    // Main entry point
    public static void main(String args[]) {

        // show splash screen early
        splash(true);

        initLog4J();
        log.info(apps.Apps.startupInfo("PanelPro"));

        setConfigFilename("PanelProConfig2.xml", args);
        JmriJFrame f = new JmriJFrame("PanelPro");
        createFrame(new PanelPro(f), f);

        log.debug("main initialization done");
        splash(false);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PanelPro.class.getName());
}


