// PaneEditAction.java

package jmri.jmrit.symbolicprog.tabbedframe;

import jmri.jmrit.decoderdefn.DecoderFile;
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
 * open a PaneProgFrame just for editing, without a programmer.
 * <P>
 * The resulting JFrame
 * is constructed on the fly here, and has no specific type.
 *
 *
 * @author			Bob Jacobsen    Copyright (C) 2008
 * @version			$Revision: 1.5 $
 */
public class PaneEditAction 	extends AbstractAction {

    Object o1, o2, o3, o4;

    static final java.util.ResourceBundle rbt = jmri.jmrit.symbolicprog.SymbolicProgBundle.bundle();

    public PaneEditAction() {
        this("Edit Roster Entry");
    }
    
    public PaneEditAction(String s) {
        super(s);

    }

    public void actionPerformed(ActionEvent e) {

        if (log.isDebugEnabled()) log.debug("Pane programmer requested");

        // create the initial frame that steers
        final JmriJFrame f = new JmriJFrame(rbt.getString("FrameEditEntrySetup"));
        f.getContentPane().setLayout(new BoxLayout(f.getContentPane(), BoxLayout.Y_AXIS));

        // add the Roster menu
        JMenuBar menuBar = new JMenuBar();
        // menuBar.setBorder(new BevelBorder(BevelBorder.RAISED));
        menuBar.add(new jmri.jmrit.roster.RosterMenu(rbt.getString("MenuRoster"),
                             jmri.jmrit.roster.RosterMenu.MAINMENU, f));
        f.setJMenuBar(menuBar);

        // known entry, no programmer
        JPanel pane1 = new KnownLocoSelPane(false){  // not programming
                protected void startProgrammer(DecoderFile decoderFile, RosterEntry re,
                                                String filename) {
                    String title = rbt.getString("FrameEditEntryTitle");
                    JFrame p = new PaneProgFrame(decoderFile, re,
                                                 title, "programmers"+File.separator+filename+".xml",
                                                 null, false){
                        protected JPanel getModePane() { return null; }
                    };
                    p.pack();
                    p.setVisible(true);

                }
        };

        // load primary frame
        pane1.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        f.getContentPane().add(pane1);

        f.pack();
        if (log.isDebugEnabled()) log.debug("Tab-Programmer setup created");
        f.setVisible(true);
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PaneOpsProgAction.class.getName());

}

/* @(#)PaneOpsProgAction.java */
