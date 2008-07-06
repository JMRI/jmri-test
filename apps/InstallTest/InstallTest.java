// InstallTest.java

package apps.InstallTest;

import apps.AppConfigPanel;
import apps.Apps;

import java.awt.event.ActionEvent;
import java.text.MessageFormat;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import jmri.util.JmriJFrame;

/**
 * The JMRI application for testing JMRI installation.
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
 *
 * @author	Ken Cameron   Copyright 2008
 * @version     $Revision: 1.1 $
 */
public class InstallTest extends Apps {

	InstallTest(JFrame p) {
        super(p);
    }

    protected AppConfigPanel newPrefs() {
        return new AppConfigPanel(configFilename, 1);
    }

    protected String logo() {
        return "resources/InstallTest.gif";
    }

    protected String mainWindowHelpID() {
            return "package.apps.InstallTest.InstallTest";
    }

    protected String line1() {
        return MessageFormat.format(rb.getString("InstallTestVersionCredit"),
                                new String[]{jmri.Version.name()});
    }

    protected String line2() {
        return "http://jmri.sf.net/InstallTest";
    }

    protected JPanel statusPanel() {
        JPanel j = new JPanel();
        j.setLayout(new BoxLayout(j, BoxLayout.Y_AXIS));
        j.add(super.statusPanel());

        Action serviceprog = new jmri.jmrit.symbolicprog.tabbedframe.PaneProgAction(rb.getString("DpButtonUseProgrammingTrack"));
        Action opsprog = new jmri.jmrit.symbolicprog.tabbedframe.PaneOpsProgAction(rb.getString("DpButtonProgramOnMainTrack"));
        Action quit = new AbstractAction(rb.getString("MenuItemQuit")){
                public void actionPerformed(ActionEvent e) {
					Apps.handleQuit();
                }
            };

        // Buttons
        JButton b1 = new JButton(rb.getString("DpButtonUseProgrammingTrack"));
        b1.addActionListener(serviceprog);
        b1.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        j.add(b1);
        if (jmri.InstanceManager.programmerManagerInstance()==null ||
            !jmri.InstanceManager.programmerManagerInstance().isServiceModePossible()) {
            b1.setEnabled(false);
            b1.setToolTipText(rb.getString("MsgServiceButtonDisabled"));
        }
        JButton m1 = new JButton(rb.getString("DpButtonProgramOnMainTrack"));
        m1.addActionListener(opsprog);
        m1.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        j.add(m1);
        if (jmri.InstanceManager.programmerManagerInstance()==null ||
            !jmri.InstanceManager.programmerManagerInstance().isOpsModePossible()) {
            m1.setEnabled(false);
            m1.setToolTipText(rb.getString("MsgOpsButtonDisabled"));
        }

        JButton q1 = new JButton(rb.getString("ButtonQuit"));
        q1.addActionListener(quit);
        q1.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        j.add(q1);
        return j;
    }

    // Main entry point
    public static void main(String args[]) {

        // show splash screen early
        splash(true);

        initLog4J();
        log.info(apps.Apps.startupInfo("InstallTest"));

        setConfigFilename("InstallTestConfig2.xml", args);
        JmriJFrame f = new JmriJFrame("InstallTest");
        createFrame(new InstallTest(f), f);

        log.debug("main initialization done");
        splash(false);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(InstallTest.class.getName());
}


