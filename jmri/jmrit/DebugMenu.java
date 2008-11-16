// DebugMenu.java

package jmri.jmrit;

import javax.swing.*;
import java.util.*;

/**
 * Create a "Debug" menu containing the JMRI system-independent 
 * debugging tools.
 *
 * @author	Bob Jacobsen   Copyright 2003
 * @version     $Revision: 1.6 $
 */
public class DebugMenu extends JMenu {
    public DebugMenu(String name, JPanel panel) {
        this(panel);
        setText(name);
    }

    public DebugMenu(JPanel panel) {

        super();

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.JmritDebugBundle");

        setText(rb.getString("MenuDebug"));

        add(new jmri.jmrit.MemoryFrameAction(rb.getString("MenuItemMemoryUsageMonitor")));
        add(new JSeparator());
        add(new jmri.jmrit.decoderdefn.InstallDecoderFileAction(rb.getString("MenuItemImportDecoderFile"), panel));
        add(new jmri.jmrit.decoderdefn.DecoderIndexCreateAction(rb.getString("MenuItemRecreateDecoderIndex")));
        add(new jmri.jmrit.roster.RecreateRosterAction(rb.getString("MenuItemRecreateRoster")));
        add(new JSeparator());
        add(new jmri.jmrit.XmlFileCheckAction(rb.getString("MenuItemCheckXMLFile"), panel));
        add(new jmri.jmrit.XmlFileValidateAction(rb.getString("MenuItemValidateXMLFile"), panel));
        add(new jmri.jmrit.decoderdefn.NameCheckAction(rb.getString("MenuItemCheckDecoderNames"), panel));
        add(new jmri.jmrit.symbolicprog.tabbedframe.ProgCheckAction(rb.getString("MenuItemCheckProgrammerNames"), panel));
        add(new JSeparator());
		add(new jmri.jmrit.LogixLoadAction(rb.getString("MenuItemLogixDisabled"), panel));
        add(new jmri.jmrit.log.LogAction(rb.getString("MenuItemLogAction")));

    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DebugMenu.class.getName());
}


