// PaneOpsProgAction.java

package jmri.jmrit.symbolicprog.tabbedframe;

import jmri.InstanceManager;
import jmri.Programmer;
import jmri.jmrit.decoderdefn.DecoderFile;
import jmri.jmrit.decoderdefn.DecoderIndexFile;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.symbolicprog.KnownLocoSelPane;
import jmri.util.JmriJFrame;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;

/**
 * Swing action to create and register a
 * frame for selecting the information needed to
 * open a PaneProgFrame in service mode.
 * <P>
 * The name is a historical accident, and probably should have
 * included "ServiceMode" or something.
 * <P>
 * The resulting JFrame
 * is constructed on the fly here, and has no specific type.
 *
 * @see  jmri.jmrit.symbolicprog.tabbedframe.PaneOpsProgAction
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			$Revision: 1.13 $
 */
public class PaneOpsProgAction 	extends AbstractAction {

    Object o1, o2, o3, o4;
    JLabel statusLabel;

    static final java.util.ResourceBundle rbt 
        = java.util.ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle");

    public PaneOpsProgAction() {
        this("DecoderPro ops-mode programmer");
    }
    
    public PaneOpsProgAction(String s) {
        super(s);

        statusLabel = new JLabel(rbt.getString("StateIdle"));

        // disable ourself if ops programming is not possible
        if (jmri.InstanceManager.programmerManagerInstance()==null ||
            !jmri.InstanceManager.programmerManagerInstance().isOpsModePossible()) {
            setEnabled(false);
            // This needs to return so the xmlThread is not started;
	    return;
        }

    }

    public void actionPerformed(ActionEvent e) {

        if (log.isDebugEnabled()) log.debug("Pane programmer requested");

        // create the initial frame that steers
        final JmriJFrame f = new JmriJFrame(rbt.getString("FrameOpsProgrammerSetup"));
        f.getContentPane().setLayout(new BoxLayout(f.getContentPane(), BoxLayout.Y_AXIS));

        // add the Roster menu
        JMenuBar menuBar = new JMenuBar();
        // menuBar.setBorder(new BevelBorder(BevelBorder.RAISED));
        menuBar.add(new jmri.jmrit.roster.RosterMenu(rbt.getString("MenuRoster"),
                             jmri.jmrit.roster.RosterMenu.MAINMENU, f));
        f.setJMenuBar(menuBar);

        // known loco on main track
        JLabel last;
        JPanel pane1 = new KnownLocoSelPane(false){  // no ident in ops mode yet

            protected void startProgrammer(DecoderFile decoderFile, RosterEntry re,
                                                String filename) {
                String title = java.text.MessageFormat.format(rbt.getString("FrameOpsProgrammerTitle"),
                                                        new String[]{re.getId()});
                // find the ops-mode programmer
                int address = Integer.parseInt(re.getDccAddress());
                boolean longAddr = true;
                if (address<100) longAddr = false;
                Programmer programmer = InstanceManager.programmerManagerInstance()
                                            .getOpsModeProgrammer(longAddr, address);
                // and created the frame
                JFrame p = new PaneOpsProgFrame(decoderFile, re,
                                                 title, "programmers"+File.separator+filename+".xml",
                                                 programmer);
                p.pack();
                p.setVisible(true);

                // f.setVisible(false);
                // f.dispose();
            }
        };

        // load primary frame
        pane1.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        f.getContentPane().add(pane1);

        f.pack();
        if (log.isDebugEnabled()) log.debug("Tab-Programmer setup created");
        f.setVisible(true);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PaneOpsProgAction.class.getName());

}

/* @(#)PaneOpsProgAction.java */
