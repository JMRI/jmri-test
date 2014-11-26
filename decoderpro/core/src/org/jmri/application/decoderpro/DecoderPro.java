package org.jmri.application.decoderpro;

import apps.Apps;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import jmri.InstanceManager;
import jmri.jmrit.symbolicprog.tabbedframe.PaneOpsProgAction;
import jmri.jmrit.symbolicprog.tabbedframe.PaneProgAction;
import org.jmri.application.AppsBundle;
import org.jmri.application.ClassicApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author rhwood
 */
public class DecoderPro extends ClassicApplication {

    static Logger log = LoggerFactory.getLogger(DecoderPro.class);

    DecoderPro(JFrame p) {
        super(p);
        log.info(ClassicApplication.startupInfo("DecoderPro"));
    }

    @Override
    protected String logo() {
        return "resources/decoderpro.gif";
    }

    @Override
    protected String mainWindowHelpID() {
        return "package.apps.DecoderPro.DecoderPro";
    }

    @Override
    protected String line1() {
        return AppsBundle.getMessage("DecoderProVersionCredit",
                new Object[]{jmri.Version.name()});
    }

    @Override
    protected String line2() {
        return "http://jmri.org/DecoderPro";
    }

    @Override
    protected JPanel statusPanel() {
        JPanel j = new JPanel();
        j.setLayout(new BoxLayout(j, BoxLayout.Y_AXIS));
        j.add(super.statusPanel());

        // Buttons
        Action serviceprog = new PaneProgAction(AppsBundle.getMessage("DpButtonUseProgrammingTrack"));
        Action opsprog = new PaneOpsProgAction(AppsBundle.getMessage("DpButtonProgramOnMainTrack"));
        Action quit = new AbstractAction(AppsBundle.getMessage("MenuItemQuit")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                Apps.handleQuit();
            }
        };

        JButton b1 = new JButton(AppsBundle.getMessage("DpButtonUseProgrammingTrack"));
        b1.addActionListener(serviceprog);
        b1.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        j.add(b1);
        if (InstanceManager.programmerManagerInstance() == null
                || !InstanceManager.programmerManagerInstance().isGlobalProgrammerAvailable()) {
            b1.setEnabled(false);
            b1.setToolTipText(AppsBundle.getMessage("MsgServiceButtonDisabled"));
        }
        JButton m1 = new JButton(AppsBundle.getMessage("DpButtonProgramOnMainTrack"));
        m1.addActionListener(opsprog);
        m1.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        j.add(m1);
        if (InstanceManager.programmerManagerInstance() == null
                || !InstanceManager.programmerManagerInstance().isAddressedModePossible()) {
            m1.setEnabled(false);
            m1.setToolTipText(AppsBundle.getMessage("MsgOpsButtonDisabled"));
        }

        JPanel p3 = new JPanel();
        p3.setLayout(new java.awt.FlowLayout());
        JButton h1 = new JButton(AppsBundle.getMessage("ButtonHelp"));
        jmri.util.HelpUtil.addHelpToComponent(h1, "html.apps.DecoderPro.index");
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
