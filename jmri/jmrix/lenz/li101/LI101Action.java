// LI101Action.java

package jmri.jmrix.lenz.li101;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register an LI101Frame object.
 * <P>
 * The {@link LI101Frame} is a configuration tool. Note that this
 * class does not provide port services for the LI101; that's
 * the {@link jmri.jmrix.lenz.li101.LI101PortFrame} class.
 *
 * @author			Paul Bender    Copyright (C) 2003
 * @version			$Revision: 2.4 $
 */
public class LI101Action extends AbstractAction {

    jmri.jmrix.lenz.XNetSystemConnectionMemo _memo = null;

    public LI101Action(String s,jmri.jmrix.lenz.XNetSystemConnectionMemo memo) 
    { 
      super(s);
      _memo=memo;
    }

    public LI101Action(jmri.jmrix.lenz.XNetSystemConnectionMemo memo) {
        this("LI101 Configuration Manager",memo);
    }

    public void actionPerformed(ActionEvent e) {
        // create an LI101Frame
        LI101Frame f = new LI101Frame(_memo);
        f.setVisible(true);
    }
}

/* @(#)LI101Action.java */
