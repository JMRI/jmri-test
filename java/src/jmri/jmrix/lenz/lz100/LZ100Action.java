// LZ100Action.java

package jmri.jmrix.lenz.lz100;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import java.util.ResourceBundle;

/**
 * Swing action to create and register an LZ100Frame object.
 * <P>
 * The {@link LZ100Frame} is a configuration tool for the LZ100 command 
 * Station.
 *
 * @author			Paul Bender    Copyright (C) 2005
 * @version			$Revision: 1.3 $
 */
public class LZ100Action extends AbstractAction {

    jmri.jmrix.lenz.XNetSystemConnectionMemo _memo = null;

    public LZ100Action(String s,jmri.jmrix.lenz.XNetSystemConnectionMemo memo) {
       super(s);
       _memo=memo;
    }

    public LZ100Action(jmri.jmrix.lenz.XNetSystemConnectionMemo memo) {
        this("LZ100 Configuration Manager",memo);
    }

    public void actionPerformed(ActionEvent e) {
        // create an LZ100Frame
 	ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.lenz.lz100.LZ100Bundle");
        LZ100Frame f = new LZ100Frame(rb.getString("LZ100Config"),_memo);
        f.setVisible(true);
    }
}

/* @(#)LZ100Action.java */
