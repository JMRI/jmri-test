// TrainsTableAction.java
package jmri.jmrit.operations.trains;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;

/**
 * Swing action to create and register a TrainTableFrame object.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2008
 * @version $Revision$
 */
@ActionID(id = "jmri.jmrit.operations.trains.TrainsTableAction",
        category = "Operations")
@ActionRegistration(iconInMenu = false,
        displayName = "jmri.jmrit.operations.JmritOperationsBundle#MenuTrains",
        iconBase = "org/jmri/core/ui/toolbar/generic.gif")
@ActionReference(path = "Menu/Operations",
        position = 4380)
public class TrainsTableAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = -7608766876884479719L;

    public TrainsTableAction(String s) {
        super(s);
    }

    public TrainsTableAction() {
        this(Bundle.getMessage("MenuTrains"));	// NOI18N
    }

    static TrainsTableFrame f = null;

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
    public void actionPerformed(ActionEvent e) {
        // create a train table frame
        if (f == null || !f.isVisible()) {
            f = new TrainsTableFrame();
        }
        f.setExtendedState(Frame.NORMAL);
        f.setVisible(true);	// this also brings the frame into focus
    }
}

/* @(#)TrainsTableAction.java */
