// DS64Action.java

package jmri.jmrix.loconet.ds64;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;


/**
 * Create and register a DS64Frame object.
 *
 * @author	Bob Jacobsen    Copyright (C) 2002, 2005
 * @version	$Revision: 1.3 $
 */
public class DS64Action 			extends AbstractAction {

    public DS64Action(String s) { super(s);}
    public DS64Action() {
        this("DS64 programmer");
    }

    public void actionPerformed(ActionEvent e) {
        // create a BDL16Frame
        DS64Frame f = new DS64Frame();
        f.setVisible(true);
    }
}

/* @(#)DS64Action.java */
