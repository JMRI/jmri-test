// DecoderPro.java

package apps.DecoderPro;

import jmri.InstanceManager;
import jmri.jmrit.XmlFile;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import apps.*;
import javax.swing.*;

/**
 * The JMRI application for configuring DCC decoders
 * <P>
 * If an argument is provided at startup, it will be used as the name of
 * the configuration file.  Note that this is just the name, not the path;
 * the file is searched for in the usual way, first in the preferences tree and then in
 * xml/
 * @author	Bob Jacobsen   Copyright 2003
 * @version     $Revision: 1.36 $
 */
public class DecoderPro extends Apps {

    DecoderPro(JFrame p) {
        super(p);
        }

    protected AppConfigPanel newPrefs() {
        return new AppConfigPanel(configFilename, 1);
    }
    protected void createMenus(JMenuBar menuBar, JFrame frame) {
        fileMenu(menuBar, frame);
        editMenu(menuBar, frame);
        toolsMenu(menuBar, frame);
        rosterMenu(menuBar, frame);
        panelMenu(menuBar, frame);
        systemsMenu(menuBar, frame);
        debugMenu(menuBar, frame);
    }

    protected String logo() {
        return "resources/decoderpro.gif";
    }

    protected String line1() {
        return MessageFormat.format(rb.getString("DecoderProVersionCredit"),
                                new String[]{jmri.Version.name()});
    }

    protected JPanel statusPanel() {
        JPanel j = new JPanel();
        j.setLayout(new BoxLayout(j, BoxLayout.Y_AXIS));
        j.add(super.statusPanel());

        Action serviceprog = new jmri.jmrit.symbolicprog.tabbedframe.PaneProgAction(rb.getString("DpButtonUseProgrammingTrack"));
        Action opsprog = new jmri.jmrit.symbolicprog.tabbedframe.PaneOpsProgAction(rb.getString("DpButtonProgramOnMainTrack"));
        Action quit = new AbstractAction(rb.getString("MenuItemQuit")){
                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                }
            };

        // Buttons
        JButton b1 = new JButton(rb.getString("DpButtonUseProgrammingTrack"));
        b1.addActionListener(serviceprog);
        b1.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        j.add(b1);

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
        SplashWindow sp = new SplashWindow();

        initLog4J();
        log.info("program starts");
        setConfigFilename("DecoderProConfig.xml", args);
        JFrame f = new JFrame("DecoderPro");
        createFrame(new DecoderPro(f), f);

        log.info("main initialization done");
    }


    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DecoderPro.class.getName());
}


