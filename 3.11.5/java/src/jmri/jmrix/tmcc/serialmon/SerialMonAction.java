// SerialMonAction.java
package jmri.jmrix.tmcc.serialmon;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a SerialMonFrame object
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2006
 * @version	$Revision$
 */
public class SerialMonAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = -3766538645555647422L;

    public SerialMonAction(String s) {
        super(s);
    }

    public SerialMonAction() {
        this("TMCC monitor");
    }

    public void actionPerformed(ActionEvent e) {
        // create a SerialMonFrame
        SerialMonFrame f = new SerialMonFrame();
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.warn("SerialMonAction starting SerialMonFrame: Exception: " + ex.toString());
        }
        f.setVisible(true);
    }

    static Logger log = LoggerFactory.getLogger(SerialMonAction.class.getName());

}


/* @(#)SerialMonAction.java */
