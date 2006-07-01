// AbstractFrameAction.java

package jmri.util;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;

/**
 * Swing action that defers class loading until invoked.
 * <P>
 * The "Frame" in the name refers to this class being optimized to
 * create a JFrame when invoked.
 * <P>
 * This does not manage the JFrame instance; if you invoke this twice, you
 * get two JFrame objects.
 *
 * @author	Bob Jacobsen    Copyright (C) 2003
 * @version     $Revision: 1.1 $
 */

abstract public class AbstractFrameAction extends AbstractAction {

    public AbstractFrameAction(String actionName, String className) {
        super(actionName);
        this.className = className;
    }

    String className;

    public void actionPerformed(ActionEvent e) {
        try {
            JFrame f = (JFrame) Class.forName(className).newInstance();
            f.show();
        } catch (Exception ex) {
            log.error("Error starting JFrame "+className+": "+ex);
        }
    }
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AbstractFrameAction.class.getName());

}
/* @(#)AbstractFrameAction.java */
