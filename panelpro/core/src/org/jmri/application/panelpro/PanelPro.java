package org.jmri.application.panelpro;

import apps.Apps;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.jmri.application.AppsBundle;
import org.jmri.application.ClassicApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author rhwood
 */
public class PanelPro extends ClassicApplication {

    static Logger log = LoggerFactory.getLogger(PanelPro.class);

    PanelPro(JFrame p) {
        super(p);
        log.info(ClassicApplication.startupInfo("PanelPro"));
    }

    @Override
    protected String logo() {
        return "resources/PanelPro.gif";
    }

    @Override
    protected String mainWindowHelpID() {
        return "package.apps.PanelPro.PanelPro";
    }

    @Override
    protected String line1() {
        return AppsBundle.getMessage("PanelProVersionCredit",
                new Object[]{jmri.Version.name()});
    }

    @Override
    protected String line2() {
        return "http://jmri.org/PanelPro";
    }

    @Override
    protected JPanel statusPanel() {
        JPanel j = new JPanel();
        j.setLayout(new BoxLayout(j, BoxLayout.Y_AXIS));
        j.add(super.statusPanel());

        // Buttons
        Action quit = new AbstractAction(AppsBundle.getMessage("MenuItemQuit")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                Apps.handleQuit();
            }
        };

        JPanel p3 = new JPanel();
        p3.setLayout(new java.awt.FlowLayout());
        JButton h1 = new JButton(AppsBundle.getMessage("ButtonHelp"));
        jmri.util.HelpUtil.addHelpToComponent(h1, "html.apps.PanelPro.PanelPro");
        h1.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        p3.add(h1);
        JButton q1 = new JButton(AppsBundle.getMessage("ButtonQuit"));
        q1.addActionListener(quit);
        q1.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        p3.add(q1);
        j.add(p3);

        return j;
    }
}
