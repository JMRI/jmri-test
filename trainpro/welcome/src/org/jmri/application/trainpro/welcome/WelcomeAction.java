package org.jmri.application.trainpro.welcome;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

@ActionID(
        category = "Help",
        id = "org.jmri.application.trainpro.welcome.WelcomeAction"
)
@ActionRegistration(
        iconBase = "org/jmri/application/trainpro/welcome/welcome.gif",
        displayName = "#CTL_WelcomeAction",
        iconInMenu = false
)
@ActionReference(path = "Menu/Help", position = 1400)
@Messages("CTL_WelcomeAction=Start &Page")
public final class WelcomeAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        WelcomeTopComponent welcome = null;
        Set<TopComponent> tcs = TopComponent.getRegistry().getOpened();
        for (TopComponent tc : tcs) {
            if (tc instanceof WelcomeTopComponent) {
                welcome = (WelcomeTopComponent) tc;
                break;
            }
        }
        if (welcome == null) {
            welcome = (WelcomeTopComponent) WindowManager.getDefault().findTopComponent("WelcomeTopComponent");
        }
        if (welcome != null) {
            welcome.open();
            welcome.requestActive();
        }
    }
}
