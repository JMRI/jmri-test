/** 
 * PaneProgAction.java
 *
 * Description:		Swing action to create and register a 
 *       			SymbolicProg object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			
 */

package jmri.jmrit.symbolicprog.tabbedframe;

import jmri.jmrit.symbolicprog.*;
import jmri.jmrit.decoderdefn.*;
import jmri.jmrit.roster.*;

import java.awt.event.*;

import java.awt.*;
import java.io.*;
import javax.swing.*;
import javax.swing.border.*;
import org.jdom.*;
import org.jdom.input.*;

public class PaneProgAction 			extends AbstractAction {

	Object o1, o2, o3, o4;
	JLabel statusLabel;
	
	public PaneProgAction(String s) { 
		super(s);

		statusLabel = new JLabel("idle");
		
		// start a low priority request for the Roster & DecoderInstance
		Thread xmlThread = new Thread( new Runnable() {
			public void run() { 
				Roster.instance();
				DecoderIndexFile.instance();
				jmri.jmrit.NameFile.instance(); 
				if (log.isInfoEnabled()) log.info("xml loading thread finishes reading Roster, DecoderIndexFIle, NameFile");
			}
		}, "read roster, decoderIndex, names");
		xmlThread.setPriority(Thread.NORM_PRIORITY-2);	
		xmlThread.start();

		// start a read low priority request to load some classes
		final ClassLoader loader = this.getClass().getClassLoader();
		Thread classLoadingThread = new Thread( new Runnable() {
				public void run() { 
					// load classes by requesting objects
					new PaneProgFrame();
					new PaneProgPane();
					new EnumVariableValue();
					new SpeedTableVarValue();
					
					if (log.isInfoEnabled()) log.info("class loading thread finishes");
				}
			}, "loading classes");	
		classLoadingThread.setPriority(Thread.MIN_PRIORITY);	
		classLoadingThread.start();
		
	}
	
    public void actionPerformed(ActionEvent e) {

		if (log.isInfoEnabled()) log.info("Pane programmer requested");
		
		// create the initial frame that steers
		final JFrame f = new JFrame("Tab-Programmer Setup");
		f.getContentPane().setLayout(new BoxLayout(f.getContentPane(), BoxLayout.Y_AXIS));

		// new Loco on programming track
		JLabel last;
		JPanel pane1 = new CombinedLocoSelPane(statusLabel){
			protected void startProgrammer(DecoderFile decoderFile, String locoFile, RosterEntry re, String filename) {
				String title = "Program new decoder";
				if (re!=null) title = "Program "+re.getId();
				JFrame p = new PaneProgFrame(decoderFile, locoFile, re, 
												title, "programmers"+File.separator+filename+".xml");
				p.pack();
				p.show();
				f.setVisible(false);
				f.dispose();
			}
		};
					
		// update roster button
		JPanel pane4 = new JPanel();
			JButton updateRoster;
			pane4.add(updateRoster = new JButton("Update Roster"));
			pane4.setBorder(new EmptyBorder(6,6,6,6));
			pane4.setAlignmentX(JLabel.RIGHT_ALIGNMENT);
			updateRoster.setEnabled(false);
			updateRoster.setToolTipText("disable because not yet implemented");
			
		// load primary frame
		f.getContentPane().add(pane1);
		f.getContentPane().add(new JSeparator(javax.swing.SwingConstants.HORIZONTAL));
		f.getContentPane().add(pane4);
		statusLabel.setAlignmentX(JLabel.RIGHT_ALIGNMENT);
		f.getContentPane().add(statusLabel);
		
		f.pack();
		if (log.isInfoEnabled()) log.info("Tab-Programmer setup created");
		f.show();	
	}

	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PaneProgAction.class.getName());

}


/* @(#)PanecProgAction.java */
