// AutoSpeedAction.java

package jmri.jmrit.symbolicprog.autospeed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;

import jmri.Programmer;
import jmri.InstanceManager;
import jmri.jmrit.decoderdefn.*;
import jmri.jmrit.roster.*;
import jmri.jmrit.symbolicprog.*;
import jmri.jmrit.symbolicprog.tabbedframe.*;

/**
 * Action to start the an "Auto Speed Configurer" by
 * creating a frame to select loco, etc.
 * <P>
 * The resulting JFrame
 * is constructed on the fly here, and has no specific type.
 *
 * @see  jmri.jmrit.symbolicprog.tabbedframe.PaneOpsProgAction
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			$Revision$
 */
public class AutoSpeedAction 			extends AbstractAction {

    /**
	 * 
	 */
	private static final long serialVersionUID = 9045736525279746469L;
	Object o1, o2, o3, o4;
    JLabel statusLabel;

    public AutoSpeedAction(String s) {
        super(s);

        statusLabel = new JLabel("idle");

        // disable ourself if ops programming is not possible
        if (jmri.InstanceManager.programmerManagerInstance()==null ||
            !jmri.InstanceManager.programmerManagerInstance().isAddressedModePossible()) {
            setEnabled(false);
        }

    }

    public void actionPerformed(ActionEvent e) {

        if (log.isInfoEnabled()) log.debug("auto speed tool requested");

        // create the initial frame that steers
        final JFrame f = new JFrame("Auto-speed Tool Setup");
        f.getContentPane().setLayout(new BoxLayout(f.getContentPane(), BoxLayout.Y_AXIS));

        // add the Roster menu
        JMenuBar menuBar = new JMenuBar();
        // menuBar.setBorder(new BevelBorder(BevelBorder.RAISED));
        menuBar.add(new jmri.jmrit.roster.swing.RosterMenu("Roster", jmri.jmrit.roster.swing.RosterMenu.MAINMENU, f));
        f.setJMenuBar(menuBar);

        // known loco on main track
        JPanel pane1 = new KnownLocoSelPane(false){  // no ident in ops mode yet

            /**
			 * 
			 */
			private static final long serialVersionUID = -4507849769156281853L;

			protected void startProgrammer(DecoderFile decoderFile, RosterEntry re,
                                                String filename) {
                String title = "Set speed info for "+re.getId()+" on main track";
                // find the ops-mode programmer
                int address = Integer.parseInt(re.getDccAddress());
                boolean longAddr = true;
                if (address<100) longAddr = false;
                Programmer programmer = InstanceManager.programmerManagerInstance()
                                            .getAddressedProgrammer(longAddr, address);
                // and created the frame
                JFrame p = new PaneOpsProgFrame(decoderFile, re,
                                                 title, "programmers"+File.separator+filename+".xml",
                                                 programmer);
                p.pack();
                p.setVisible(true);
                f.setVisible(false);
                f.dispose();
            }
        };

        // load primary frame
        pane1.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        f.getContentPane().add(pane1);

        f.pack();
        if (log.isInfoEnabled()) log.debug("setup created");
        f.setVisible(true);
    }

    static Logger log = LoggerFactory.getLogger(AutoSpeedAction.class.getName());

}

/* @(#)AutoSpeedAction.java */
