// AboutDialog.java
package jmri.swing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Locale;
import javax.swing.*;
import jmri.Application;
import jmri.Version;
import jmri.jmrix.ConnectionConfig;

/**
 * About dialog.
 *
 * @author Randall Wood Copyright (C) 2012
 * @version $Revision$
 */
public class AboutDialog extends JDialog {

    ConnectionConfig[] connection = {null, null, null, null};

    // this should probably be changed to a JmriAbstractAction that opens a JOptionPane with the contents and an OK button instead.
    public AboutDialog(JFrame frame, boolean modal) {

        super(frame, modal);

        log.debug("Start UI");

        JPanel pane = new JPanel();
        pane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
        pane.add(namePane());
        pane.add(infoPane());
        this.add(pane);
        this.pack();
        this.setResizable(false);
        this.setLocationRelativeTo(null); // center on screen
        this.setTitle(Bundle.getMessage("TitleAbout", Application.getApplicationName()));
        log.debug("End constructor");
    }

    protected JPanel namePane() {
        String logo = Application.getLogo();
        JPanel pane = new JPanel();
        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
        if (log.isDebugEnabled()) {
            log.debug("Fetch main logo: " + logo + " " + getToolkit().getImage(logo));
        }
        addCenteredComponent(new JLabel(new ImageIcon(getToolkit().getImage(logo), "JMRI logo"), JLabel.CENTER), pane);
        pane.add(Box.createRigidArea(new Dimension(0, 15)));
        JLabel appName = new JLabel(Application.getApplicationName(), JLabel.CENTER);
        appName.setFont(pane.getFont().deriveFont(Font.BOLD, pane.getFont().getSize() * 1.2f));
        addCenteredComponent(appName, pane);
        addCenteredComponent(new JLabel(Application.getURL(), JLabel.CENTER), pane);
        pane.add(Box.createRigidArea(new Dimension(0, 15)));
        pane.setAlignmentX(Component.CENTER_ALIGNMENT);
        return pane;
    }

    protected JPanel infoPane() {
        JPanel pane1 = new JPanel();
        pane1.setLayout(new BoxLayout(pane1, BoxLayout.Y_AXIS));

        log.debug("start labels");

        // add listener for Com port updates
        ArrayList<Object> connList = jmri.InstanceManager.configureManagerInstance().getInstanceList(jmri.jmrix.ConnectionConfig.class);
        if (connList != null && !connList.isEmpty()) {
            for (Object conn : connList) {
                if (!((ConnectionConfig) conn).getDisabled()) {
                    pane1.add(new ConnectionLabel((ConnectionConfig) conn));
                }
            }
        } else {
            JLabel error = new JLabel("Unable to read connections list");
            error.setForeground(Color.red);
            pane1.add(error);
        }
        pane1.add(Box.createRigidArea(new Dimension(0, 15)));

        pane1.add(new JLabel(Bundle.getMessage("DefaultVersionCredit", Version.name())));
        pane1.add(new JLabel(Version.getCopyright()));
        pane1.add(new JLabel(Bundle.getMessage("JavaVersionCredit", 
                System.getProperty("java.version", "<unknown>"),
                Locale.getDefault().toString())));
        pane1.setAlignmentX(Component.CENTER_ALIGNMENT);
        return pane1;
    }

    protected void addCenteredComponent(JComponent c, JPanel p) {
        c.setAlignmentX(Component.CENTER_ALIGNMENT); // doesn't work
        p.add(c);
    }
    private static final Logger log = LoggerFactory.getLogger(AboutDialog.class.getName());
}
