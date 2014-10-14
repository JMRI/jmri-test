package org.jmri.core.ui.toolbar.power;

import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;

@ActionID(
        category = "JMRI",
        id = "org.jmri.core.ui.toolbar.power.PowerAction"
)
@ActionRegistration(
        lazy = false,
        displayName = "CTL_PowerAction"
)
@ActionReference(
        path = "Toolbars/JMRI",
        position = 408
)
@NbBundle.Messages("CTL_PowerAction=Power")
public final class PowerAction extends AbstractAction implements Presenter.Toolbar {

    @Override
    public void actionPerformed(ActionEvent e) {
        // TODO implement action body
    }

    @Override
    public Component getToolbarPresenter() {
        return new PowerPanel();
    }
}
