// LIUSBConfigAction.java

package jmri.jmrix.lenz.liusb;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register an LIUSBConfigFrame object.
 * <P>
 * The {@link LIUSBConfigFrame} is a configuration tool. 
 *
 * @author			Paul Bender    Copyright (C) 2009
 * @version			$Revision: 1.2 $
 */
public class LIUSBConfigAction extends AbstractAction {

    private jmri.jmrix.lenz.XNetSystemConnectionMemo _memo = null;

    public LIUSBConfigAction(String s,jmri.jmrix.lenz.XNetSystemConnectionMemo memo) { 
      super(s);
      _memo=memo;
    }

    public LIUSBConfigAction(jmri.jmrix.lenz.XNetSystemConnectionMemo memo) {
        this("LIUSB Configuration Manager",memo);
    }

    public void actionPerformed(ActionEvent e) {
        // create an LIUSBConfigFrame
        LIUSBConfigFrame f = new LIUSBConfigFrame(_memo);
        f.setVisible(true);
    }
}

/* @(#)LIUSBConfigAction.java */
