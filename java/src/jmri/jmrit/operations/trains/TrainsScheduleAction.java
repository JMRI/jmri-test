// TrainsScheduleAction.java
package jmri.jmrit.operations.trains;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Swing action to create and register a TrainsScheduleTableFrame object.
 *
 * @author Daniel Boudreau Copyright (C) 2010
 * @version $Revision$
 */
public class TrainsScheduleAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = -2153851570593170952L;

    public TrainsScheduleAction(String s) {
        super(s);
    }

    TrainsScheduleTableFrame f = null;

    public void actionPerformed(ActionEvent e) {
        // create a frame
        if (f == null || !f.isVisible()) {
            f = new TrainsScheduleTableFrame();
        }
        f.setExtendedState(Frame.NORMAL);
        f.setVisible(true);	// this also brings the frame into focus
    }
}

/* @(#)TrainsScheduleAction.java */
