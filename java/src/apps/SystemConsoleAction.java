// SystemConsoleAction.java
package apps;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.Icon;
import jmri.util.swing.JmriPanel;
import jmri.util.swing.WindowInterface;
import org.openide.modules.Places;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to display the JMRI System Console
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <P>
 *
 * @author Matthew Harris copyright (c) 2010
 * @version $Revision$
 */
public class SystemConsoleAction extends jmri.util.swing.JmriAbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = -5843920804488337948L;
    private static final Logger log = LoggerFactory.getLogger(SystemConsoleAction.class);

    public SystemConsoleAction(String s, WindowInterface wi) {
        super(s, wi);
    }

    public SystemConsoleAction(String s, Icon i, WindowInterface wi) {
        super(s, i, wi);
    }

    public SystemConsoleAction() {
        super(Bundle.getMessage("TitleConsole"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Show system console
        File userDir = Places.getUserDirectory();
        if (userDir == null) {
            return;
        }
        File f = new File(userDir, "/var/log/messages.log");
        SystemConsoleSupport scs = new SystemConsoleSupport(f);
        try {
            scs.showSystemConsole();
        } catch (IOException ex) {
            log.info("Showing System Console action failed", ex);
        }
    }

    // never invoked, because we overrode actionPerformed above
    @Override
    public JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked"); // NOI18N
    }

}

/* @(#)SystemConsoleAction.java */
