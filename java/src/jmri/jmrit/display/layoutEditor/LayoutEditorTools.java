// jmri.jmrit.display.LayoutEditorTools.java

package jmri.jmrit.display.layoutEditor;

import jmri.InstanceManager;
import jmri.util.JmriJFrame;
import jmri.Conditional;
import jmri.ConditionalAction;
import jmri.implementation.DefaultConditionalAction;
import jmri.ConditionalVariable;
import jmri.Logix;
import jmri.util.swing.JmriBeanComboBox;
import java.awt.*;
import java.awt.BorderLayout;
import java.awt.geom.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ResourceBundle;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.Border;

import jmri.Sensor;
import jmri.Turnout;
import jmri.SignalHead;
import jmri.SignalMast;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.blockboss.BlockBossLogic;

import jmri.jmrit.display.SensorIcon;
import jmri.jmrit.display.SignalMastIcon;
import jmri.jmrit.display.SignalHeadIcon;
import jmri.jmrit.display.PositionableIcon;

/**
 * Layout Editor Tools provides tools making use of layout connectivity available 
 *	in Layout Editor panels.
 * <P>
 * The tools in this module are accessed via the Tools menu in Layout Editor.
 * <P>
 * @author Dave Duchamp Copyright (c) 2007
 * @version $Revision$
 */

public class LayoutEditorTools 
{

	// Defined text resource
	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.layoutEditor.LayoutEditorBundle");
    static final ResourceBundle rbean = ResourceBundle.getBundle("jmri.NamedBeanBundle");
	
	// constants
	private int NONE = 0;  // Signal at Turnout Positions
	private int A1 = 1;
	private int A2 = 2;
	private int A3 = 3;
	private int B1 = 4;
	private int B2 = 5;
	private int C1 = 6;
	private int C2 = 7;
	private int D1 = 8;
	private int D2 = 9;
	
	// operational instance variables shared between tools
	private LayoutEditor layoutEditor = null;
    private MultiIconEditor signalIconEditor = null;
    private JFrame signalFrame = null;
	private boolean needRedraw = false;
	private BlockBossLogic logic = null;
	private SignalHead auxSignal = null;
	
	// constructor method
	public LayoutEditorTools(LayoutEditor thePanel) {
		layoutEditor = thePanel;
	}

	/**
	 * Tool to set signals at a turnout, including placing the signal icons and 
	 *		optionally setup of Simple Signal Logic for each signal head
	 * <P>
	 * This tool assumes left facing signal head icons have been selected, and 
	 *		will rotate the signal head icons accordingly.
	 * <P>
	 * This tool will place throat icons on the right side of the track, and 
	 *		continuing and diverging icons on the outside edge of the turnout.
	 * <P>
	 * This tool only places signal icons if the turnout is either mostly vertical 
	 *		or mostly horizontal. Some user adjustment may be needed.
	 */	
	
	// operational variables for Set Signals at Turnout tool
	private JmriJFrame setSignalsFrame = null;
	private boolean setSignalsOpen = false;
	private JTextField turnoutNameField = new JTextField(16);
	private JTextField throatContinuingField = new JTextField(16);
	private JTextField throatDivergingField = new JTextField(16);
	private JTextField continuingField = new JTextField(16);
	private JTextField divergingField = new JTextField(16);
	private JCheckBox setThroatContinuing = new JCheckBox(rb.getString("PlaceHead"));
	private JCheckBox setupLogicThroatContinuing = new JCheckBox(rb.getString("SetLogic"));
	private JCheckBox setThroatDiverging = new JCheckBox(rb.getString("PlaceHead"));
	private JCheckBox setupLogicThroatDiverging = new JCheckBox(rb.getString("SetLogic"));
	private JCheckBox setContinuing = new JCheckBox(rb.getString("PlaceHead"));
	private JCheckBox setupLogicContinuing = new JCheckBox(rb.getString("SetLogic"));
	private JCheckBox setDiverging = new JCheckBox(rb.getString("PlaceHead"));
	private JCheckBox setupLogicDiverging = new JCheckBox(rb.getString("SetLogic"));
	private JButton getSavedSignalHeads = null;
	private JButton changeSignalIcon = null;
	private JButton setSignalsDone = null;
	private JButton setSignalsCancel = null;
	private LayoutTurnout layoutTurnout = null;
	private boolean layoutTurnoutHorizontal = false;
	private boolean layoutTurnoutVertical = false;
	private boolean layoutTurnoutThroatLeft = false;
	private boolean layoutTurnoutThroatUp = false;
	private boolean layoutTurnoutBUp = false;
	private boolean layoutTurnoutBLeft = false;
	private boolean turnoutFromMenu = false;
	private Turnout turnout = null;
	private SignalHead throatContinuingHead = null;
	private SignalHead throatDivergingHead = null;
	private SignalHead continuingHead = null;
	private SignalHead divergingHead = null;

	// display dialog for Set Signals at Turnout tool
	public void setSignalsAtTurnoutFromMenu( LayoutTurnout to, 
					MultiIconEditor theEditor, JFrame theFrame ) {
		turnoutFromMenu = true;
		layoutTurnout = to;
		turnout = to.getTurnout();
		turnoutNameField.setText(to.getTurnoutName());
		setSignalsAtTurnout(theEditor,theFrame);
	}
	public void setSignalsAtTurnout( MultiIconEditor theEditor, JFrame theFrame ) {
		signalIconEditor = theEditor;
		signalFrame = theFrame;
		if (setSignalsOpen) {
			setSignalsFrame.setVisible(true);
			return;
		}
		// Initialize if needed
		if (setSignalsFrame == null) {
            setSignalsFrame = new JmriJFrame( rb.getString("SignalsAtTurnout"), false, true);
            setSignalsFrame.addHelpMenu("package.jmri.jmrit.display.SetSignalsAtTurnout", true);
            setSignalsFrame.setLocation(70,30);
            Container theContentPane = setSignalsFrame.getContentPane();        
            theContentPane.setLayout(new BoxLayout(theContentPane, BoxLayout.Y_AXIS));
			JPanel panel1 = new JPanel(); 
            panel1.setLayout(new FlowLayout());
			if (turnoutFromMenu) {
				JLabel turnoutNameLabel = new JLabel( rb.getString("Turnout")+" "+
					rb.getString("Name")+" : "+layoutTurnout.getTurnoutName());
				panel1.add(turnoutNameLabel);
			}
			else {
				JLabel turnoutNameLabel = new JLabel( rb.getString("Turnout")+" "+
																rb.getString("Name") );
				panel1.add(turnoutNameLabel);
				panel1.add(turnoutNameField);
				turnoutNameField.setToolTipText(rb.getString("SignalsTurnoutNameHint"));
			}
            theContentPane.add(panel1);
			theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));
            JPanel panel2 = new JPanel();
            panel2.setLayout(new FlowLayout());
			JLabel shTitle = new JLabel(rb.getString("SignalHeads"));
			panel2.add(shTitle);
			panel2.add(new JLabel("   "));
            panel2.add(getSavedSignalHeads = new JButton(rb.getString("GetSaved")));
            getSavedSignalHeads.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						turnoutSignalsGetSaved(e);
					}
				});
            getSavedSignalHeads.setToolTipText( rb.getString("GetSavedHint") );			
			theContentPane.add(panel2);
            JPanel panel21 = new JPanel();
            panel21.setLayout(new FlowLayout());
			JLabel throatContinuingLabel = new JLabel(rb.getString("ThroatContinuing")+" : ");
			panel21.add(throatContinuingLabel);
			panel21.add(throatContinuingField);
			theContentPane.add(panel21);
			throatContinuingField.setToolTipText(rb.getString("SignalHeadNameHint"));
            JPanel panel22 = new JPanel();
            panel22.setLayout(new FlowLayout());
			panel22.add(new JLabel("   "));
			panel22.add(setThroatContinuing);
			setThroatContinuing.setToolTipText(rb.getString("PlaceHeadHint"));
			panel22.add(new JLabel("  "));
			panel22.add(setupLogicThroatContinuing);
			setupLogicThroatContinuing.setToolTipText(rb.getString("SetLogicHint"));
			theContentPane.add(panel22);
            JPanel panel31 = new JPanel();
            panel31.setLayout(new FlowLayout());
			JLabel throatDivergingLabel = new JLabel(rb.getString("ThroatDiverging")+" : ");
			panel31.add(throatDivergingLabel);
			panel31.add(throatDivergingField);
			theContentPane.add(panel31);
			throatDivergingField.setToolTipText(rb.getString("SignalHeadNameHint"));
            JPanel panel32 = new JPanel();
            panel32.setLayout(new FlowLayout());
			panel32.add(new JLabel("   "));
			panel32.add(setThroatDiverging);
			setThroatDiverging.setToolTipText(rb.getString("PlaceHeadHint"));
			panel32.add(new JLabel("  "));
			panel32.add(setupLogicThroatDiverging);
			setupLogicThroatDiverging.setToolTipText(rb.getString("SetLogicHint"));
			theContentPane.add(panel32);
            JPanel panel41 = new JPanel();
            panel41.setLayout(new FlowLayout());
			JLabel continuingLabel = new JLabel(rb.getString("Continuing")+" : ");
			panel41.add(continuingLabel);
			panel41.add(continuingField);
			theContentPane.add(panel41);
			continuingField.setToolTipText(rb.getString("SignalHeadNameHint"));
            JPanel panel42 = new JPanel();
            panel42.setLayout(new FlowLayout());
			panel42.add(new JLabel("   "));
			panel42.add(setContinuing);
			setContinuing.setToolTipText(rb.getString("PlaceHeadHint"));
			panel42.add(new JLabel("  "));
			panel42.add(setupLogicContinuing);
			setupLogicContinuing.setToolTipText(rb.getString("SetLogicHint"));
			theContentPane.add(panel42);
            JPanel panel51 = new JPanel();
            panel51.setLayout(new FlowLayout());
			JLabel divergingLabel = new JLabel(rb.getString("Diverging")+" : ");
			panel51.add(divergingLabel);
			panel51.add(divergingField);
			theContentPane.add(panel51);
			divergingField.setToolTipText(rb.getString("SignalHeadNameHint"));
            JPanel panel52 = new JPanel();
            panel52.setLayout(new FlowLayout());
			panel52.add(new JLabel("   "));
			panel52.add(setDiverging);
			setDiverging.setToolTipText(rb.getString("PlaceHeadHint"));
			panel52.add(new JLabel("  "));
			panel52.add(setupLogicDiverging);
			setupLogicDiverging.setToolTipText(rb.getString("SetLogicHint"));
			theContentPane.add(panel52);
			theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));
            JPanel panel6 = new JPanel();
            panel6.setLayout(new FlowLayout());
            panel6.add(changeSignalIcon = new JButton(rb.getString("ChangeSignalIcon")));
            changeSignalIcon.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						signalFrame.setVisible(true);
					}
				});
            changeSignalIcon.setToolTipText( rb.getString("ChangeSignalIconHint") );
			panel6.add(new JLabel("  "));
            panel6.add(setSignalsDone = new JButton(rb.getString("Done")));
            setSignalsDone.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setSignalsDonePressed(e);
                }
            });
            setSignalsDone.setToolTipText( rb.getString("SignalDoneHint") );
            panel6.add(setSignalsCancel = new JButton(rb.getString("Cancel")));
            setSignalsCancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setSignalsCancelPressed(e);
                }
            });
            setSignalsCancel.setToolTipText( rb.getString("CancelHint") );
            theContentPane.add(panel6);
			setSignalsFrame.addWindowListener(new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent e) {
					setSignalsCancelPressed(null);
				}
			});
			if (turnoutFromMenu) turnoutSignalsGetSaved(null);
		}
        setSignalsFrame.pack();
        setSignalsFrame.setVisible(true);		
		setSignalsOpen = true;
	}
	private void turnoutSignalsGetSaved (ActionEvent a) {
		if ( !getTurnoutInformation(false) ) return;
		throatContinuingField.setText(layoutTurnout.getSignalA1Name());	
		throatDivergingField.setText(layoutTurnout.getSignalA2Name());
		continuingField.setText(layoutTurnout.getSignalB1Name());
		divergingField.setText(layoutTurnout.getSignalC1Name());	
	}
	private void setSignalsCancelPressed (ActionEvent a) {
		setSignalsOpen = false;
		turnoutFromMenu = false;
		setSignalsFrame.setVisible(false);
	}
	private void setSignalsDonePressed (ActionEvent a) {
		// process turnout name
		if ( !getTurnoutInformation(false) ) return;
		// process signal head names
		if ( !getTurnoutSignalHeadInformation() ) return;
		// place signals as requested
		if (setThroatContinuing.isSelected()) {
			if (isHeadOnPanel(throatContinuingHead) &&
				(throatContinuingHead!=getHeadFromName(layoutTurnout.getSignalA1Name()))) {
				JOptionPane.showMessageDialog(setSignalsFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError6"),
						new Object[]{throatContinuingField.getText().trim()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else if ( (!layoutTurnoutHorizontal) && (!layoutTurnoutVertical) ) {
				JOptionPane.showMessageDialog(setSignalsFrame,
					rb.getString("InfoMessage2"),"",JOptionPane.INFORMATION_MESSAGE);
				if (throatContinuingHead!=getHeadFromName(layoutTurnout.getSignalA1Name())) {				
					removeSignalHeadFromPanel(layoutTurnout.getSignalA1Name());
					removeAssignment(throatContinuingHead);
					layoutTurnout.setSignalA1Name(throatContinuingField.getText().trim());
				}
			}				
			else {
				removeSignalHeadFromPanel(layoutTurnout.getSignalA1Name());
				placeThroatContinuing();
				removeAssignment(throatContinuingHead);
				layoutTurnout.setSignalA1Name(throatContinuingField.getText().trim());
				needRedraw = true;
			}		
		}
		else {
			int assigned = isHeadAssignedHere(throatContinuingHead,layoutTurnout);
			if (assigned == NONE) {
				if ( isHeadOnPanel(throatContinuingHead) && 
									isHeadAssignedAnywhere(throatContinuingHead) ) {
					JOptionPane.showMessageDialog(setSignalsFrame,
						java.text.MessageFormat.format(rb.getString("SignalsError8"),
							new Object[]{throatContinuingField.getText().trim()}), 
								rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
					return;
				}		
				else {
					removeSignalHeadFromPanel(layoutTurnout.getSignalA1Name());
					removeAssignment(throatContinuingHead);
					layoutTurnout.setSignalA1Name(throatContinuingField.getText().trim());
				}
			}
			else if (assigned!=A1) {
// need to figure out what to do in this case.			
			}
		}
		if ( (setThroatDiverging.isSelected()) && (throatDivergingHead!=null) ) {
			if (isHeadOnPanel(throatDivergingHead) && 
				(throatDivergingHead!=getHeadFromName(layoutTurnout.getSignalA2Name()))) {
				JOptionPane.showMessageDialog(setSignalsFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError6"),
						new Object[]{throatDivergingField.getText().trim()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else if ( (!layoutTurnoutHorizontal) && (!layoutTurnoutVertical) ) {
				JOptionPane.showMessageDialog(setSignalsFrame,
					rb.getString("InfoMessage2"),"",JOptionPane.INFORMATION_MESSAGE);
				if (throatDivergingHead!=getHeadFromName(layoutTurnout.getSignalA2Name())) {
					removeSignalHeadFromPanel(layoutTurnout.getSignalA2Name());
					removeAssignment(throatDivergingHead);
					layoutTurnout.setSignalA2Name(throatDivergingField.getText().trim());
				}
			}				
			else {
				removeSignalHeadFromPanel(layoutTurnout.getSignalA2Name());
				placeThroatDiverging();
				removeAssignment(throatDivergingHead);
				layoutTurnout.setSignalA2Name(throatDivergingField.getText().trim());
				needRedraw = true;
			}		
		}
		else if (throatDivergingHead!=null) {
			int assigned = isHeadAssignedHere(throatDivergingHead,layoutTurnout);
			if (assigned == NONE) {
				if (isHeadOnPanel(throatDivergingHead) && 
									isHeadAssignedAnywhere(throatDivergingHead) ) {
					JOptionPane.showMessageDialog(setSignalsFrame,
						java.text.MessageFormat.format(rb.getString("SignalsError8"),
							new Object[]{throatDivergingField.getText().trim()}), 
								rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
					return;
				}		
				else {
					removeSignalHeadFromPanel(layoutTurnout.getSignalA2Name());
					removeAssignment(throatDivergingHead);
					layoutTurnout.setSignalA2Name(throatDivergingField.getText().trim());
				}
			}
			else if (assigned!=A2) {
// need to figure out what to do in this case.			
			}
		}
		else if (throatDivergingHead==null) {
			removeSignalHeadFromPanel(layoutTurnout.getSignalA2Name());
			layoutTurnout.setSignalA2Name("");
		}
		if (setContinuing.isSelected()) {
			if (isHeadOnPanel(continuingHead) && 
				(continuingHead!=getHeadFromName(layoutTurnout.getSignalB1Name()))) {
				JOptionPane.showMessageDialog(setSignalsFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError6"),
						new Object[]{continuingField.getText().trim()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else if ( (!layoutTurnoutHorizontal) && (!layoutTurnoutVertical) ) {
				JOptionPane.showMessageDialog(setSignalsFrame,
					rb.getString("InfoMessage2"),"",JOptionPane.INFORMATION_MESSAGE);
				if (continuingHead!=getHeadFromName(layoutTurnout.getSignalB1Name())) {
					removeSignalHeadFromPanel(layoutTurnout.getSignalB1Name());
					removeAssignment(continuingHead);
					layoutTurnout.setSignalB1Name(continuingField.getText().trim());
				}
			}				
			else {
				removeSignalHeadFromPanel(layoutTurnout.getSignalB1Name());
				if (layoutTurnout.getContinuingSense()==Turnout.CLOSED) 
					placeContinuing(continuingField.getText().trim());
				else
					placeDiverging(continuingField.getText().trim());
				removeAssignment(continuingHead);
				layoutTurnout.setSignalB1Name(continuingField.getText().trim());
				needRedraw = true;
			}		
		}
		else {
			int assigned = isHeadAssignedHere(continuingHead,layoutTurnout);
			if (assigned == NONE) {
				if (isHeadOnPanel(continuingHead)  && 
									isHeadAssignedAnywhere(continuingHead) ) {
					JOptionPane.showMessageDialog(setSignalsFrame,
						java.text.MessageFormat.format(rb.getString("SignalsError8"),
							new Object[]{continuingField.getText().trim()}), 
								rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
					return;
				}		
				else {
					removeSignalHeadFromPanel(layoutTurnout.getSignalB1Name());
					removeAssignment(continuingHead);
					layoutTurnout.setSignalB1Name(continuingField.getText().trim());
				}
			}
			else if (assigned!=B1) {
// need to figure out what to do in this case.			
			}
		}
		if (setDiverging.isSelected()) {
			if (isHeadOnPanel(divergingHead) && 
				(divergingHead!=getHeadFromName(layoutTurnout.getSignalC1Name()))) {
				JOptionPane.showMessageDialog(setSignalsFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError6"),
						new Object[]{divergingField.getText().trim()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else if ( (!layoutTurnoutHorizontal) && (!layoutTurnoutVertical) ) {
				JOptionPane.showMessageDialog(setSignalsFrame,
					rb.getString("InfoMessage2"),"",JOptionPane.INFORMATION_MESSAGE);
				if (divergingHead!=getHeadFromName(layoutTurnout.getSignalC1Name())) {
					removeSignalHeadFromPanel(layoutTurnout.getSignalC1Name());
					removeAssignment(divergingHead);
					layoutTurnout.setSignalC1Name(divergingField.getText().trim());
				}
			}				
			else {
				removeSignalHeadFromPanel(layoutTurnout.getSignalC1Name());
				if (layoutTurnout.getContinuingSense()==Turnout.CLOSED) 
					placeDiverging(divergingField.getText().trim());
				else
					placeContinuing(divergingField.getText().trim());
				removeAssignment(divergingHead);
				layoutTurnout.setSignalC1Name(divergingField.getText().trim());
				needRedraw = true;
			}		
		}
		else {
			int assigned = isHeadAssignedHere(divergingHead,layoutTurnout);
			if (assigned == NONE) {
				if (isHeadOnPanel(divergingHead) && 
									isHeadAssignedAnywhere(divergingHead) ) {
					JOptionPane.showMessageDialog(setSignalsFrame,
						java.text.MessageFormat.format(rb.getString("SignalsError8"),
							new Object[]{divergingField.getText().trim()}), 
								rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
					return;
				}		
				else {
					removeSignalHeadFromPanel(layoutTurnout.getSignalC1Name());
					removeAssignment(divergingHead);
					layoutTurnout.setSignalC1Name(divergingField.getText().trim());
				}
			}
			else if (assigned!=C1) {
// need to figure out what to do in this case.			
			}
		}
		// setup Logic if requested and enough information is available
		if (setupLogicThroatContinuing.isSelected()) {
			setLogicThroatContinuing();
		}
		if ( (throatDivergingHead!=null) && setupLogicThroatDiverging.isSelected() ) {
			setLogicThroatDiverging();
		}
		if (setupLogicContinuing.isSelected()) {
			setLogicContinuing();
		}
		if ( setupLogicDiverging.isSelected() ) {
			setLogicDiverging();
		}		
		// make sure this layout turnout is not linked to another
		layoutTurnout.setLinkType(LayoutTurnout.NO_LINK);
		layoutTurnout.setLinkedTurnoutName("");
		// finish up
		setSignalsOpen = false;
		turnoutFromMenu = false;
		setSignalsFrame.setVisible(false);
		if (needRedraw) {
			layoutEditor.redrawPanel();
			needRedraw = false;
			layoutEditor.setDirty();
		}
	}
	private boolean getTurnoutInformation(boolean crossover) {
		LayoutTurnout t = null;
		String str = "";
		if ( (!turnoutFromMenu && !crossover) ||
					(!xoverFromMenu && crossover) ) {
			turnout = null;
			layoutTurnout = null;
			if (!crossover) str = turnoutNameField.getText().trim();
			else str = xoverTurnoutName.trim();
			if ( (str==null) || (str.equals("")) ) {
				JOptionPane.showMessageDialog(setSignalsFrame,rb.getString("SignalsError1"),
									rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return false;
			}
			turnout = jmri.InstanceManager.turnoutManagerInstance().getTurnout(str);
			if (turnout==null) {
				JOptionPane.showMessageDialog(setSignalsFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError2"),
						new Object[]{str}), rb.getString("Error"),
							JOptionPane.ERROR_MESSAGE);
				return false ;
			}
			else if ( (turnout.getUserName()==null) || (turnout.getUserName().equals("")) ||
									!turnout.getUserName().equals(str) ) {
				str = str.toUpperCase();
				if (!crossover) 
					turnoutNameField.setText(str);
				else
					xoverTurnoutName = str;
			}
			for (int i=0;i<layoutEditor.turnoutList.size();i++) {
				t = layoutEditor.turnoutList.get(i);
				if (t.getTurnout() == turnout) {
					layoutTurnout = t;
					if ( ( (t.getTurnoutType()==LayoutTurnout.DOUBLE_XOVER) || 
							(t.getTurnoutType()==LayoutTurnout.RH_XOVER) || 
								(t.getTurnoutType()==LayoutTurnout.LH_XOVER) ) &&
									(!crossover) ) {
						javax.swing.JOptionPane.showMessageDialog(layoutEditor,
								rb.getString("InfoMessage1"),"",
								javax.swing.JOptionPane.INFORMATION_MESSAGE);
						setSignalsCancelPressed(null);
						return false;
					}
					if ( (!( (t.getTurnoutType()==LayoutTurnout.DOUBLE_XOVER) || 
							(t.getTurnoutType()==LayoutTurnout.RH_XOVER) || 
								(t.getTurnoutType()==LayoutTurnout.LH_XOVER) ) ) &&
									crossover ) {
						javax.swing.JOptionPane.showMessageDialog(layoutEditor,
								rb.getString("InfoMessage8"),"",
								javax.swing.JOptionPane.INFORMATION_MESSAGE);
						setXoverSignalsCancelPressed(null);
						return false;
					}
				}
			}
		}
		t = layoutTurnout;
		if (t!=null) {
			double delX = t.getCoordsA().getX() - t.getCoordsB().getX();
			double delY = t.getCoordsA().getY() - t.getCoordsB().getY();
			layoutTurnoutHorizontal = false;
			layoutTurnoutVertical = false;
			layoutTurnoutThroatLeft = false;
			layoutTurnoutThroatUp = false;
			layoutTurnoutBUp = false;
			layoutTurnoutBLeft = false;
			if (Math.abs(delX) > 2.0*Math.abs(delY)) {
				layoutTurnoutHorizontal = true;
				if (delX < 0.0) layoutTurnoutThroatLeft = true;
				if (t.getCoordsB().getY() < t.getCoordsC().getY())
						layoutTurnoutBUp = true;
			}
			if (Math.abs(delY) > 2.0*Math.abs(delX)) { 
				layoutTurnoutVertical = true;
				if (delY <0.0) layoutTurnoutThroatUp = true;
				if (t.getCoordsB().getX() < t.getCoordsC().getX())
					layoutTurnoutBLeft = true;
			}
			return true;
		}
		JOptionPane.showMessageDialog(setSignalsFrame,
				java.text.MessageFormat.format(rb.getString("SignalsError3"),
						new Object[]{str}), rb.getString("Error"),
							JOptionPane.ERROR_MESSAGE);
		return false;
	}
	private boolean getTurnoutSignalHeadInformation() {
		throatContinuingHead = getSignalHeadFromEntry(throatContinuingField,true,setSignalsFrame);
		if (throatContinuingHead==null) return false;
		throatDivergingHead = getSignalHeadFromEntry(throatDivergingField,false,setSignalsFrame);
		continuingHead = getSignalHeadFromEntry(continuingField,true,setSignalsFrame);
		if (continuingHead==null) return false;
		divergingHead = getSignalHeadFromEntry(divergingField,true,setSignalsFrame);
		if (divergingHead==null) return false;
		return true;
	}
	private NamedIcon testIcon = null;
	private void placeThroatContinuing() {
		if (testIcon == null)
			testIcon = signalIconEditor.getIcon(0);
		if( layoutTurnoutHorizontal && layoutTurnoutThroatLeft ) {
			setSignalHeadOnPanel(2,throatContinuingField.getText().trim(),
				(int)(layoutTurnout.getCoordsA().getX()-testIcon.getIconWidth()),
				(int)(layoutTurnout.getCoordsA().getY()+4) );
		}
		else if( layoutTurnoutHorizontal && (!layoutTurnoutThroatLeft) ) {
			setSignalHeadOnPanel(0,throatContinuingField.getText().trim(),
				(int)(layoutTurnout.getCoordsA().getX()),
				(int)(layoutTurnout.getCoordsA().getY()-4-testIcon.getIconHeight()) );
		}
		else if( layoutTurnoutVertical && layoutTurnoutThroatUp ) {
			setSignalHeadOnPanel(1,throatContinuingField.getText().trim(),
				(int)(layoutTurnout.getCoordsA().getX()-4-testIcon.getIconWidth()),
				(int)(layoutTurnout.getCoordsA().getY()-testIcon.getIconHeight()) );
		}
		else if( layoutTurnoutVertical && (!layoutTurnoutThroatUp) ) {
			setSignalHeadOnPanel(3,throatContinuingField.getText().trim(),
				(int)(layoutTurnout.getCoordsA().getX()+4),
				(int)(layoutTurnout.getCoordsA().getY()) );
		}
	}
	private void placeThroatDiverging() {
		if (testIcon == null)
			testIcon = signalIconEditor.getIcon(0);
		if( layoutTurnoutHorizontal && layoutTurnoutThroatLeft ) {
			setSignalHeadOnPanel(2,throatDivergingField.getText().trim(),
				(int)(layoutTurnout.getCoordsA().getX()-4-(2*testIcon.getIconWidth())),
				(int)(layoutTurnout.getCoordsA().getY()+4) );
		}
		else if( layoutTurnoutHorizontal && (!layoutTurnoutThroatLeft) ) {
			setSignalHeadOnPanel(0,throatDivergingField.getText().trim(),
				(int)(layoutTurnout.getCoordsA().getX()+4+testIcon.getIconWidth()),
				(int)(layoutTurnout.getCoordsA().getY()-4-testIcon.getIconHeight()));
		}
		else if( layoutTurnoutVertical && layoutTurnoutThroatUp ) {
			setSignalHeadOnPanel(1,throatDivergingField.getText().trim(),
				(int)(layoutTurnout.getCoordsA().getX()-4-testIcon.getIconWidth()),
				(int)(layoutTurnout.getCoordsA().getY()-4-(2*testIcon.getIconHeight())));
		}
		else if( layoutTurnoutVertical && (!layoutTurnoutThroatUp) ) {
			setSignalHeadOnPanel(3,throatDivergingField.getText().trim(),
				(int)(layoutTurnout.getCoordsA().getX()+4),
				(int)(layoutTurnout.getCoordsA().getY()+4+testIcon.getIconHeight()));
		}
	}
	private void placeContinuing(String signalHeadName) {
		if (testIcon == null)
			testIcon = signalIconEditor.getIcon(0);
		if( layoutTurnoutHorizontal && layoutTurnoutThroatLeft && layoutTurnoutBUp) {
			setSignalHeadOnPanel(0,signalHeadName,
				(int)(layoutTurnout.getCoordsB().getX()),
				(int)(layoutTurnout.getCoordsB().getY()-4-testIcon.getIconHeight()) );
		}
		else if( layoutTurnoutHorizontal && layoutTurnoutThroatLeft && (!layoutTurnoutBUp) ) {
			setSignalHeadOnPanel(0,signalHeadName,
				(int)(layoutTurnout.getCoordsB().getX()),
				(int)(layoutTurnout.getCoordsB().getY()+4) );
		}
		else if( layoutTurnoutHorizontal && (!layoutTurnoutThroatLeft) && layoutTurnoutBUp ) {
			setSignalHeadOnPanel(2,signalHeadName,
				(int)(layoutTurnout.getCoordsB().getX()-testIcon.getIconWidth()),
				(int)(layoutTurnout.getCoordsB().getY()-4-testIcon.getIconHeight()) );
		}
		else if( layoutTurnoutHorizontal && (!layoutTurnoutThroatLeft) && (!layoutTurnoutBUp) ) {
			setSignalHeadOnPanel(2,signalHeadName,
				(int)(layoutTurnout.getCoordsB().getX()-testIcon.getIconWidth()),
				(int)(layoutTurnout.getCoordsB().getY()+4) );
		}
		else if( layoutTurnoutVertical && layoutTurnoutThroatUp && layoutTurnoutBLeft ) {
			setSignalHeadOnPanel(3,signalHeadName,
				(int)(layoutTurnout.getCoordsB().getX()-4-testIcon.getIconWidth()),
				(int)(layoutTurnout.getCoordsB().getY()) );
		}
		else if( layoutTurnoutVertical && layoutTurnoutThroatUp && (!layoutTurnoutBLeft) ) {
			setSignalHeadOnPanel(3,signalHeadName,
				(int)(layoutTurnout.getCoordsB().getX()+4),
				(int)(layoutTurnout.getCoordsB().getY()) );
		}
		else if( layoutTurnoutVertical && (!layoutTurnoutThroatUp) && layoutTurnoutBLeft ) {
			setSignalHeadOnPanel(1,signalHeadName,
				(int)(layoutTurnout.getCoordsB().getX()-4-testIcon.getIconWidth()),
				(int)(layoutTurnout.getCoordsB().getY()-testIcon.getIconHeight()) );
		}
		else if( layoutTurnoutVertical && (!layoutTurnoutThroatUp) && (!layoutTurnoutBLeft) ) {
			setSignalHeadOnPanel(1,signalHeadName,
				(int)(layoutTurnout.getCoordsB().getX()+4),
				(int)(layoutTurnout.getCoordsB().getY()-testIcon.getIconHeight()) );
		}
	}
	private void placeDiverging(String signalHeadName) {
		if (testIcon == null)
			testIcon = signalIconEditor.getIcon(0);
		if( layoutTurnoutHorizontal && layoutTurnoutThroatLeft && layoutTurnoutBUp) {
			setSignalHeadOnPanel(0,signalHeadName,
				(int)(layoutTurnout.getCoordsC().getX()),
				(int)(layoutTurnout.getCoordsC().getY()+4) );
		}
		else if( layoutTurnoutHorizontal && layoutTurnoutThroatLeft && (!layoutTurnoutBUp) ) {
			setSignalHeadOnPanel(0,signalHeadName,
				(int)(layoutTurnout.getCoordsC().getX()),
				(int)(layoutTurnout.getCoordsC().getY()-4-testIcon.getIconHeight()) );
		}
		else if( layoutTurnoutHorizontal && (!layoutTurnoutThroatLeft) && layoutTurnoutBUp ) {
			setSignalHeadOnPanel(2,signalHeadName,
				(int)(layoutTurnout.getCoordsC().getX()-testIcon.getIconWidth()),
				(int)(layoutTurnout.getCoordsC().getY()+4) );
		}
		else if( layoutTurnoutHorizontal && (!layoutTurnoutThroatLeft) && (!layoutTurnoutBUp) ) {
			setSignalHeadOnPanel(2,signalHeadName,
				(int)(layoutTurnout.getCoordsC().getX()-testIcon.getIconWidth()),
				(int)(layoutTurnout.getCoordsC().getY()-4-testIcon.getIconHeight()) );
		}
		else if( layoutTurnoutVertical && layoutTurnoutThroatUp && layoutTurnoutBLeft ) {
			setSignalHeadOnPanel(3,signalHeadName,
				(int)(layoutTurnout.getCoordsC().getX()+4),
				(int)(layoutTurnout.getCoordsC().getY()) );
		}
		else if( layoutTurnoutVertical && layoutTurnoutThroatUp && (!layoutTurnoutBLeft) ) {
			setSignalHeadOnPanel(3,signalHeadName,
				(int)(layoutTurnout.getCoordsC().getX()-4-testIcon.getIconWidth()),
				(int)(layoutTurnout.getCoordsC().getY()) );
		}
		else if( layoutTurnoutVertical && (!layoutTurnoutThroatUp) && layoutTurnoutBLeft ) {
			setSignalHeadOnPanel(1,signalHeadName,
				(int)(layoutTurnout.getCoordsC().getX()+4),
				(int)(layoutTurnout.getCoordsC().getY()-testIcon.getIconHeight()) );
		}
		else if( layoutTurnoutVertical && (!layoutTurnoutThroatUp) && (!layoutTurnoutBLeft) ) {
			setSignalHeadOnPanel(1,signalHeadName,
				(int)(layoutTurnout.getCoordsC().getX()-4-testIcon.getIconWidth()),
				(int)(layoutTurnout.getCoordsC().getY()-testIcon.getIconHeight()) );
		}
	}
	private void setLogicThroatContinuing() {
		TrackSegment track = null;
		if (layoutTurnout.getContinuingSense()==Turnout.CLOSED) 
			track = (TrackSegment)layoutTurnout.getConnectB();
		else
			track = (TrackSegment)layoutTurnout.getConnectC();
		if (track==null) {
			JOptionPane.showMessageDialog(setSignalsFrame,
					rb.getString("InfoMessage7"),"",JOptionPane.INFORMATION_MESSAGE);			
			return;
		}
		LayoutBlock block = track.getLayoutBlock();
		if (block==null) {
			JOptionPane.showMessageDialog(setSignalsFrame,
					rb.getString("InfoMessage6"),"",JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		Sensor occupancy = block.getOccupancySensor();
		if (occupancy==null) {
			JOptionPane.showMessageDialog(setSignalsFrame,
				java.text.MessageFormat.format(rb.getString("InfoMessage4"),
					new Object[]{block.getUserName()}), 
						null,JOptionPane.INFORMATION_MESSAGE);						
			return;
		}
		SignalHead nextHead = getNextSignalFromObject(track,
				layoutTurnout, throatContinuingField.getText().trim(), setSignalsFrame);
		if ( (nextHead==null) && (!reachedEndBumper()) ) {
			JOptionPane.showMessageDialog(setSignalsFrame,
				java.text.MessageFormat.format(rb.getString("InfoMessage5"),
					new Object[]{block.getUserName()}), 
						null,JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		if (throatDivergingHead!=null) {
			if (!initializeBlockBossLogic(throatContinuingField.getText().trim())) return;
			logic.setMode(BlockBossLogic.TRAILINGMAIN);
			logic.setTurnout(turnout.getSystemName());
			logic.setSensor1(occupancy.getSystemName());
			if (nextHead!=null) {
				logic.setWatchedSignal1(nextHead.getSystemName(),false);
			}
			if (auxSignal!=null) {
				logic.setWatchedSignal1Alt(auxSignal.getSystemName());
			}
			finalizeBlockBossLogic();
			return;
		}
		SignalHead savedAuxSignal = auxSignal;
		TrackSegment track2 = null;
		if (layoutTurnout.getContinuingSense()==Turnout.CLOSED) 
			track2 = (TrackSegment)layoutTurnout.getConnectC();
		else
			track2 = (TrackSegment)layoutTurnout.getConnectB();
		if (track2==null) {
			JOptionPane.showMessageDialog(setSignalsFrame,
					rb.getString("InfoMessage7"),"",JOptionPane.INFORMATION_MESSAGE);			
			return;
		}
		LayoutBlock block2 = track2.getLayoutBlock();
		if (block2==null) {
			JOptionPane.showMessageDialog(setSignalsFrame,
					rb.getString("InfoMessage6"),"",JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		Sensor occupancy2 = block2.getOccupancySensor();
		if (occupancy2==null) {
			JOptionPane.showMessageDialog(setSignalsFrame,
				java.text.MessageFormat.format(rb.getString("InfoMessage4"),
					new Object[]{block2.getUserName()}), 
						null,JOptionPane.INFORMATION_MESSAGE);						
			return;
		}
		SignalHead nextHead2 = getNextSignalFromObject(track2,
				layoutTurnout, throatContinuingField.getText().trim(), setSignalsFrame);
		if ( (nextHead2==null) && (!reachedEndBumper()) ) {
			JOptionPane.showMessageDialog(setSignalsFrame,
				java.text.MessageFormat.format(rb.getString("InfoMessage5"),
					new Object[]{block2.getUserName()}), 
						null,JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		if (!initializeBlockBossLogic(throatContinuingField.getText().trim())) return;
		logic.setMode(BlockBossLogic.FACING);
		logic.setTurnout(turnout.getSystemName());
		logic.setWatchedSensor1(occupancy.getSystemName());
		logic.setWatchedSensor2(occupancy2.getSystemName());
		if (nextHead!=null) {
			logic.setWatchedSignal1(nextHead.getSystemName(),false);
		}
		if (savedAuxSignal!=null) {
			logic.setWatchedSignal1Alt(savedAuxSignal.getSystemName());
		}
		if (nextHead2!=null) {
			logic.setWatchedSignal2(nextHead2.getSystemName());
		}
		if (auxSignal!=null) {
			logic.setWatchedSignal2Alt(auxSignal.getSystemName());
		}		
		if (!layoutTurnout.isMainlineC())
			logic.setLimitSpeed2(true);
		finalizeBlockBossLogic();
	}
	private void setLogicThroatDiverging() {
		TrackSegment track = null;
		if (layoutTurnout.getContinuingSense()==Turnout.CLOSED)
			track = (TrackSegment)layoutTurnout.getConnectC();
		else
			track = (TrackSegment)layoutTurnout.getConnectB();		
		if (track==null) {
			JOptionPane.showMessageDialog(setSignalsFrame,
					rb.getString("InfoMessage7"),"",JOptionPane.INFORMATION_MESSAGE);			
			return;
		}
		LayoutBlock block = track.getLayoutBlock();
		if (block==null) {
			JOptionPane.showMessageDialog(setSignalsFrame,
					rb.getString("InfoMessage6"),"",JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		Sensor occupancy = block.getOccupancySensor();
		if (occupancy==null) {
			JOptionPane.showMessageDialog(setSignalsFrame,
				java.text.MessageFormat.format(rb.getString("InfoMessage4"),
					new Object[]{block.getUserName()}), 
						null,JOptionPane.INFORMATION_MESSAGE);						
			return;
		}
		SignalHead nextHead = getNextSignalFromObject(track,
				layoutTurnout, throatDivergingField.getText().trim(), setSignalsFrame);
		if ( (nextHead==null) && (!reachedEndBumper()) ) {
			JOptionPane.showMessageDialog(setSignalsFrame,
				java.text.MessageFormat.format(rb.getString("InfoMessage5"),
					new Object[]{block.getUserName()}), 
						null,JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		if (!initializeBlockBossLogic(throatDivergingField.getText().trim())) return;
		logic.setMode(BlockBossLogic.TRAILINGDIVERGING);
		logic.setTurnout(turnout.getSystemName());
		logic.setSensor1(occupancy.getSystemName());
		if (nextHead!=null)
			logic.setWatchedSignal1(nextHead.getSystemName(),false);
		if (auxSignal!=null) {
			logic.setWatchedSignal1Alt(auxSignal.getSystemName());
		}
		if (!layoutTurnout.isMainlineC())
			logic.setLimitSpeed2(true);
		finalizeBlockBossLogic();			
	}
	private void setLogicContinuing() {
		TrackSegment track = (TrackSegment)layoutTurnout.getConnectA();
		if (track==null) {
			JOptionPane.showMessageDialog(setSignalsFrame,
					rb.getString("InfoMessage7"),"",JOptionPane.INFORMATION_MESSAGE);			
			return;
		}
		LayoutBlock block = track.getLayoutBlock();
		if (block==null) {
			JOptionPane.showMessageDialog(setSignalsFrame,
					rb.getString("InfoMessage6"),"",JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		Sensor occupancy = block.getOccupancySensor();
		if (occupancy==null) {
			JOptionPane.showMessageDialog(setSignalsFrame,
				java.text.MessageFormat.format(rb.getString("InfoMessage4"),
					new Object[]{block.getUserName()}), 
						null,JOptionPane.INFORMATION_MESSAGE);						
			return;
		}
		SignalHead nextHead = getNextSignalFromObject(track,
					layoutTurnout, continuingField.getText().trim(), setSignalsFrame);
		if ( (nextHead==null) && (!reachedEndBumper()) ) {
			JOptionPane.showMessageDialog(setSignalsFrame,
				java.text.MessageFormat.format(rb.getString("InfoMessage5"),
					new Object[]{block.getUserName()}), 
						null,JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		if (!initializeBlockBossLogic(continuingField.getText().trim())) return;
		logic.setMode(BlockBossLogic.TRAILINGMAIN);
		logic.setTurnout(turnout.getSystemName());
		logic.setSensor1(occupancy.getSystemName());
		if (nextHead!=null) {
			logic.setWatchedSignal1(nextHead.getSystemName(),false);
		}
		if (auxSignal!=null) {
			logic.setWatchedSignal1Alt(auxSignal.getSystemName());
		}
		finalizeBlockBossLogic();	
	}
	private void setLogicDiverging() {
		TrackSegment track = (TrackSegment)layoutTurnout.getConnectA();
		if (track==null) {
			JOptionPane.showMessageDialog(setSignalsFrame,
					rb.getString("InfoMessage7"),"",JOptionPane.INFORMATION_MESSAGE);			
			return;
		}
		LayoutBlock block = track.getLayoutBlock();
		if (block==null) {
			JOptionPane.showMessageDialog(setSignalsFrame,
					rb.getString("InfoMessage6"),"",JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		Sensor occupancy = block.getOccupancySensor();
		if (occupancy==null) {
			JOptionPane.showMessageDialog(setSignalsFrame,
				java.text.MessageFormat.format(rb.getString("InfoMessage4"),
					new Object[]{block.getUserName()}), 
						null,JOptionPane.INFORMATION_MESSAGE);						
			return;
		}
		SignalHead nextHead = getNextSignalFromObject(track,
				layoutTurnout, divergingField.getText().trim(), setSignalsFrame);
		if ( (nextHead==null) && (!reachedEndBumper()) ) {
			JOptionPane.showMessageDialog(setSignalsFrame,
				java.text.MessageFormat.format(rb.getString("InfoMessage5"),
					new Object[]{block.getUserName()}), 
						null,JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		if (!initializeBlockBossLogic(divergingField.getText().trim())) return;
		logic.setMode(BlockBossLogic.TRAILINGDIVERGING);
		logic.setTurnout(turnout.getSystemName());
		logic.setSensor1(occupancy.getSystemName());
		if (nextHead!=null) {
			logic.setWatchedSignal1(nextHead.getSystemName(),false);
		}
		if (auxSignal!=null) {
			logic.setWatchedSignal1Alt(auxSignal.getSystemName());
		}
		if (!layoutTurnout.isMainlineC())
			logic.setLimitSpeed2(true);
		finalizeBlockBossLogic();		
	}
	
	/** 
	 * Utility routines used by multiple tools
	 */
	 
	/**
	 * Returns the layout turnout corresponding to a given turnout.
	 * If require double crossover is requested, and error message is sent to the user if the 
	 *		layout turnout is not a double crossover, and null is returned.
	 * If a layout turnout corresponding to the turnout is not found, an error message
	 *		is sent to the user and null is returned.
	 */
	public LayoutTurnout getLayoutTurnoutFromTurnout(Turnout turnout, boolean requireDoubleXover,
				String str, JFrame theFrame) {
		LayoutTurnout t = null;
		for (int i=0;i<layoutEditor.turnoutList.size();i++) {
			t = layoutEditor.turnoutList.get(i);
			if (t.getTurnout() == turnout) {
				// have the layout turnout corresponding to the turnout
				if ( (t.getTurnoutType()==LayoutTurnout.DOUBLE_XOVER) &&
									(!requireDoubleXover) ) {
					javax.swing.JOptionPane.showMessageDialog(theFrame,
								rb.getString("InfoMessage1"),"",
								javax.swing.JOptionPane.INFORMATION_MESSAGE);
					return null;
				}
				if (requireDoubleXover && (t.getTurnoutType()!=LayoutTurnout.DOUBLE_XOVER) ) {
					javax.swing.JOptionPane.showMessageDialog(theFrame,
								rb.getString("InfoMessage8"),"",
								javax.swing.JOptionPane.INFORMATION_MESSAGE);
					return null;
				}					
				return t;
			}
		}
		// layout turnout not found
		JOptionPane.showMessageDialog(theFrame,
				java.text.MessageFormat.format(rb.getString("SignalsError3"),
						new Object[]{str}), rb.getString("Error"),
							JOptionPane.ERROR_MESSAGE);
		return null;
	}
	 
	/**
	 * Returns the SignalHead corresponding to an entry field in the specified dialog.
	 *		This also takes care of UpperCase and trimming of leading and trailing blanks.
	 *		If entry is required, and no entry is present, and error message is sent.
	 *      An error message also results if a signal head with the entered name is not
	 *      found in the SignalTable.
	 */
	public SignalHead getSignalHeadFromEntry(JTextField signalName,boolean requireEntry,
					JmriJFrame frame) {
		String str = signalName.getText().trim();
		if ( (str==null) || (str.equals("")) ) {
			if (requireEntry) {
				JOptionPane.showMessageDialog(frame,rb.getString("SignalsError5"),
									rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
			}
			return null;
		}
		SignalHead head = jmri.InstanceManager.signalHeadManagerInstance().getSignalHead(str);
		if (head==null) {
			JOptionPane.showMessageDialog(frame,
					java.text.MessageFormat.format(rb.getString("SignalsError4"),
						new Object[]{str}), rb.getString("Error"),
							JOptionPane.ERROR_MESSAGE);
			return null ;
		}
		else if ( (head.getUserName()==null) || (head.getUserName().equals("")) ||
									!head.getUserName().equals(str) ) {
			str = str.toUpperCase();
			signalName.setText(str);
		}
		return (head);
	}
	/**
	 * Returns a SignalHead given a name
	 */
	public SignalHead getHeadFromName(String str) {
		if ( (str==null) || (str.equals("")) ) {
			return null;
		}
		return  (jmri.InstanceManager.signalHeadManagerInstance().getSignalHead(str));
	}
    
	/**
	 * Places a signal head icon on the panel after rotation at the designated place, with all
	 *		with all icons taken care of.
	 */
	public void setSignalHeadOnPanel(int rotation, String headName,
					int xLoc, int yLoc) {
		SignalHeadIcon l = new SignalHeadIcon(layoutEditor);
		l.setSignalHead(headName);
        l.setIcon(rbean.getString("SignalHeadStateRed"), signalIconEditor.getIcon(0));
        l.setIcon(rbean.getString("SignalHeadStateFlashingRed"), signalIconEditor.getIcon(1));
        l.setIcon(rbean.getString("SignalHeadStateYellow"), signalIconEditor.getIcon(2));
        l.setIcon(rbean.getString("SignalHeadStateFlashingYellow"), signalIconEditor.getIcon(3));
        l.setIcon(rbean.getString("SignalHeadStateGreen"), signalIconEditor.getIcon(4));
        l.setIcon(rbean.getString("SignalHeadStateFlashingGreen"), signalIconEditor.getIcon(5));
        l.setIcon(rbean.getString("SignalHeadStateDark"), signalIconEditor.getIcon(6));
        l.setIcon(rbean.getString("SignalHeadStateHeld"), signalIconEditor.getIcon(7));
        l.setIcon(rbean.getString("SignalHeadStateLunar"), signalIconEditor.getIcon(8));
        l.setIcon(rbean.getString("SignalHeadStateFlashingLunar"), signalIconEditor.getIcon(9));
		l.setLocation(xLoc,yLoc);
		if (rotation>0) {
            java.util.Enumeration<String> e = l.getIconStateNames();
            while (e.hasMoreElements()) {
                l.getIcon(e.nextElement()).setRotation(rotation,l);
            }
		}		
		layoutEditor.putSignal(l);
	}
	/** 
	 * Returns an index if the specified signal head is assigned to the
	 *		LayoutTurnout initialized. Otherwise returns the NONE index.
	 *		The index specifies the turnout position of the signal head
	 *		according to the code listed at the beginning of this module.
	 */
	private int isHeadAssignedHere(SignalHead head, LayoutTurnout lTurnout) {
		String sysName = head.getSystemName();
		String uName = head.getUserName();
		String name = lTurnout.getSignalA1Name();
		if ( (name!=null) && (name.length()>0) && ((name.equals(uName)) || 
						(name.equals(sysName))) ) return A1;
		name = lTurnout.getSignalA2Name();
		if ( (name!=null) && (name.length()>0) && ((name.equals(uName)) || 
						(name.equals(sysName))) ) return A2;
		name = lTurnout.getSignalA3Name();
		if ( (name!=null) && (name.length()>0) && ((name.equals(uName)) || 
						(name.equals(sysName))) ) return A3;
		name = lTurnout.getSignalB1Name();
		if ( (name!=null) && (name.length()>0) && ((name.equals(uName)) || 
						(name.equals(sysName))) ) return B1;
		name = lTurnout.getSignalB2Name();
		if ( (name!=null) && (name.length()>0) && ((name.equals(uName)) || 
						(name.equals(sysName))) ) return B2;
		name = lTurnout.getSignalC1Name();
		if ( (name!=null) && (name.length()>0) && ((name.equals(uName)) || 
						(name.equals(sysName))) ) return C1;
		name = lTurnout.getSignalC2Name();
		if ( (name!=null) && (name.length()>0) && ((name.equals(uName)) || 
						(name.equals(sysName))) ) return C2;
		name = lTurnout.getSignalD1Name();
		if ( (name!=null) && (name.length()>0) && ((name.equals(uName)) || 
						(name.equals(sysName))) ) return D1;
		name = lTurnout.getSignalD2Name();
		if ( (name!=null) && (name.length()>0) && ((name.equals(uName)) || 
						(name.equals(sysName))) ) return D2;
		return NONE;
	}
	/**
	 * Returns true if an icon for the specified SignalHead is on the panel
	 */
	public boolean isHeadOnPanel(SignalHead head) 
	{
		SignalHeadIcon h = null;
		for (int i=0;i<layoutEditor.signalList.size();i++) {
			h = layoutEditor.signalList.get(i);
			if (h.getSignalHead() == head) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Returns true if the specified Signal Head is assigned to an object
	 *		on the panel, regardless of whether an icon is displayed or not
	 */
	public boolean isHeadAssignedAnywhere(SignalHead head) 
	{
		String sName = head.getSystemName();
		String uName = head.getUserName();
		for (int i=0;i<layoutEditor.turnoutList.size();i++) {
			LayoutTurnout to = layoutEditor.turnoutList.get(i);
			if ((to.getSignalA1Name()!=null) &&
					(to.getSignalA1Name().equals(sName) || ((uName!=null) && 
					(to.getSignalA1Name().equals(uName))))) return true;
			if ((to.getSignalA2Name()!=null) &&
					(to.getSignalA2Name().equals(sName) || ((uName!=null) && 
					(to.getSignalA2Name().equals(uName))))) return true;
			if ((to.getSignalA3Name()!=null) &&
					(to.getSignalA3Name().equals(sName) || ((uName!=null) && 
					(to.getSignalA3Name().equals(uName))))) return true;
			if ((to.getSignalB1Name()!=null) &&
					(to.getSignalB1Name().equals(sName) || ((uName!=null) && 
					(to.getSignalB1Name().equals(uName))))) return true;
			if ((to.getSignalB2Name()!=null) &&
					(to.getSignalB2Name().equals(sName) || ((uName!=null) && 
					(to.getSignalB2Name().equals(uName))))) return true;
			if ((to.getSignalC1Name()!=null) &&
					(to.getSignalC1Name().equals(sName) || ((uName!=null) && 
					(to.getSignalC1Name().equals(uName))))) return true;
			if ((to.getSignalC2Name()!=null) &&
					(to.getSignalC2Name().equals(sName) || ((uName!=null) && 
					(to.getSignalC2Name().equals(uName))))) return true;
			if ((to.getSignalD1Name()!=null) &&
					(to.getSignalD1Name().equals(sName) || ((uName!=null) && 
					(to.getSignalD1Name().equals(uName))))) return true;
			if ((to.getSignalD2Name()!=null) &&
					(to.getSignalD2Name().equals(sName) || ((uName!=null) && 
					(to.getSignalD2Name().equals(uName))))) return true;
		}
		for (int i=0;i<layoutEditor.pointList.size();i++) {
			PositionablePoint po = layoutEditor.pointList.get(i);
			if ((po.getEastBoundSignal()!=null) &&
					(po.getEastBoundSignal().equals(sName) || ((uName!=null) && 
					(po.getEastBoundSignal().equals(uName))))) return true;
			if ((po.getWestBoundSignal()!=null) &&
					(po.getWestBoundSignal().equals(sName) || ((uName!=null) && 
					(po.getWestBoundSignal().equals(uName))))) return true;
		}
		for (int i=0;i<layoutEditor.xingList.size();i++) {
			LevelXing x = layoutEditor.xingList.get(i);
			if ((x.getSignalAName()!=null) &&
					(x.getSignalAName().equals(sName) || ((uName!=null) && 
					(x.getSignalAName().equals(uName))))) return true;
			if ((x.getSignalBName()!=null) &&
					(x.getSignalBName().equals(sName) || ((uName!=null) && 
					(x.getSignalBName().equals(uName))))) return true;
			if ((x.getSignalCName()!=null) &&
					(x.getSignalCName().equals(sName) || ((uName!=null) && 
					(x.getSignalCName().equals(uName))))) return true;
			if ((x.getSignalDName()!=null) &&
					(x.getSignalDName().equals(sName) || ((uName!=null) && 
					(x.getSignalDName().equals(uName))))) return true;
		}
		return false;
	}
	/**
	 * Removes the assignment of the specified SignalHead to either a turnout, 
	 *		a positionable point, or a level crossing wherever it is assigned
	 */
	public void removeAssignment(SignalHead head) 
	{
		String sName = head.getSystemName();
		String uName = head.getUserName();
		for (int i=0;i<layoutEditor.turnoutList.size();i++) {
			LayoutTurnout to = layoutEditor.turnoutList.get(i);
			if ((to.getSignalA1Name()!=null) &&
					(to.getSignalA1Name().equals(sName) || ((uName!=null) && 
					(to.getSignalA1Name().equals(uName))))) to.setSignalA1Name("");
			if ((to.getSignalA2Name()!=null) &&
					(to.getSignalA2Name().equals(sName) || ((uName!=null) && 
					(to.getSignalA2Name().equals(uName))))) to.setSignalA2Name("");
			if ((to.getSignalA3Name()!=null) &&
					(to.getSignalA3Name().equals(sName) || ((uName!=null) && 
					(to.getSignalA3Name().equals(uName))))) to.setSignalA3Name("");
			if ((to.getSignalB1Name()!=null) &&
					(to.getSignalB1Name().equals(sName) || ((uName!=null) && 
					(to.getSignalB1Name().equals(uName))))) to.setSignalB1Name("");
			if ((to.getSignalB2Name()!=null) &&
					(to.getSignalB2Name().equals(sName) || ((uName!=null) && 
					(to.getSignalB2Name().equals(uName))))) to.setSignalB2Name("");
			if ((to.getSignalC1Name()!=null) &&
					(to.getSignalC1Name().equals(sName) || ((uName!=null) && 
					(to.getSignalC1Name().equals(uName))))) to.setSignalC1Name("");
			if ((to.getSignalC2Name()!=null) &&
					(to.getSignalC2Name().equals(sName) || ((uName!=null) && 
					(to.getSignalC2Name().equals(uName))))) to.setSignalC2Name("");
			if ((to.getSignalD1Name()!=null) &&
					(to.getSignalD1Name().equals(sName) || ((uName!=null) && 
					(to.getSignalD1Name().equals(uName))))) to.setSignalD1Name("");
			if ((to.getSignalD2Name()!=null) &&
					(to.getSignalD2Name().equals(sName) || ((uName!=null) && 
					(to.getSignalD2Name().equals(uName))))) to.setSignalD2Name("");
		}
		for (int i=0;i<layoutEditor.pointList.size();i++) {
			PositionablePoint po = layoutEditor.pointList.get(i);
			if ((po.getEastBoundSignal()!=null) &&
					(po.getEastBoundSignal().equals(sName) || ((uName!=null) && 
					(po.getEastBoundSignal().equals(uName))))) 
				po.setEastBoundSignal("");
			if ((po.getWestBoundSignal()!=null) &&
					(po.getWestBoundSignal().equals(sName) || ((uName!=null) && 
					(po.getWestBoundSignal().equals(uName))))) 
				po.setWestBoundSignal("");
		}
		for (int i=0;i<layoutEditor.xingList.size();i++) {
			LevelXing x = layoutEditor.xingList.get(i);
			if ((x.getSignalAName()!=null) &&
					(x.getSignalAName().equals(sName) || ((uName!=null) && 
					(x.getSignalAName().equals(uName))))) x.setSignalAName("");
			if ((x.getSignalBName()!=null) &&
					(x.getSignalBName().equals(sName) || ((uName!=null) && 
					(x.getSignalBName().equals(uName))))) x.setSignalBName("");
			if ((x.getSignalCName()!=null) &&
					(x.getSignalCName().equals(sName) || ((uName!=null) && 
					(x.getSignalCName().equals(uName))))) x.setSignalCName("");
			if ((x.getSignalDName()!=null) &&
					(x.getSignalDName().equals(sName) || ((uName!=null) && 
					(x.getSignalDName().equals(uName))))) x.setSignalDName("");
		}
	}
	/**
	 * Removes the SignalHead with the specified name from the panel and from
	 *		assignment to any turnout, positionable point, or level crossing
	 */
	@SuppressWarnings("null")
	public void removeSignalHeadFromPanel(String signalName) {
		if ( (signalName==null) || (signalName.length()<1) ) return;
		SignalHead head = jmri.InstanceManager.signalHeadManagerInstance().
														getSignalHead(signalName);
		removeAssignment(head);
		SignalHeadIcon h = null;
		int index = -1;
		for (int i=0;(i<layoutEditor.signalList.size())&&(index==-1);i++) {
			h = layoutEditor.signalList.get(i);
			if (h.getSignalHead() == head) {
				index = i;
			}
		}
		if (index!=(-1)) {
			layoutEditor.signalList.remove(index);
			h.remove();
			h.dispose();
			needRedraw = true;
		}
	}
	/* 
	 * Initializes a BlockBossLogic for creation of a signal logic for the signal
	 *		head named in "signalHeadName".
	 * Should not be called until enough informmation has been gathered to allow
	 *		configuration of the Simple Signal Logic.
	 */
	public boolean initializeBlockBossLogic(String signalHeadName) {
		logic = BlockBossLogic.getStoppedObject(signalHeadName);
		if (logic==null) {
			log.error("Trouble creating BlockBossLogic for '"+signalHeadName+"'.");
			return false;
		}
		return true;
	}
	/* 
	 * Finalizes a successfully created signal logic 
	 */
	public void finalizeBlockBossLogic() {
		if (logic==null) return;
		logic.retain();
		logic.start();
		logic = null;
	}

	/*
	 * Returns the signal head at the end of the block "track" is assigned to.
	 *		"track" is the Track Segment leaving "object".
	 *		"object" must be either an anchor point or one of the connecting
	 *			points of a turnout or level crossing.
	 * Note: returns 'null' is signal is not present where it is expected, or
	 *		if an End Bumper is reached. To test for end bumper, use the 
	 *      associated routine "reachedEndBumper()". Reaching a turntable ray
	 *		track connection is considered reaching an end bumper.
	 * Note: Normally this routine requires a signal at any turnout it finds. 
	 *		However, if 'skipIncludedTurnout' is true, this routine will skip 
	 *		over an absent signal at an included turnout, that is a turnout  
	 *		with its throat track segment and its continuing track segment in 
	 *		the same block. When this happens, the user is warned.
	 */		
	public SignalHead getNextSignalFromObject(TrackSegment track, Object object,
								String headName, JmriJFrame frame) {
		hitEndBumper = false;
		auxSignal = null;
		TrackSegment t = track;
		Object obj = object;
		boolean inBlock = true;
		int type = 0;
		Object connect = null;
		while (inBlock) {
			if (t.getConnect1()==obj) {
				type = t.getType2();
				connect = t.getConnect2();
			}
			else {
				type = t.getType1();
				connect = t.getConnect1();
			}
			if (type==LayoutEditor.POS_POINT) {
				PositionablePoint p = (PositionablePoint)connect;
				if (p.getType()==PositionablePoint.END_BUMPER) {
					hitEndBumper = true;
					return null;
				}
				if (p.getConnect1()==t)
					t=p.getConnect2();
				else
					t=p.getConnect1();
				if (t==null) return null;
				if (track.getLayoutBlock()!=t.getLayoutBlock()) {
					// p is a block boundary - should be signalled
					String signalName;
					if (isAtWestEndOfAnchor(t,p)) 
						signalName = p.getWestBoundSignal();
					else signalName = p.getEastBoundSignal();
					if ((signalName==null)||(signalName.equals(""))) return null;
					return jmri.InstanceManager.signalHeadManagerInstance().
									getSignalHead(signalName);
				}
				obj = p;
			}
			else if (type==LayoutEditor.TURNOUT_A) {
				// Reached turnout throat, should be signalled
				LayoutTurnout to = (LayoutTurnout)connect;
				String signalName = to.getSignalA2Name();
				if ((!(signalName==null))&&(!(signalName.equals("")))) 
					auxSignal = jmri.InstanceManager.signalHeadManagerInstance().
									getSignalHead(signalName);
				signalName = to.getSignalA1Name();
				if ((signalName==null)||(signalName.equals(""))) {
					if (!layoutEditor.skipIncludedTurnout) 
						return null;
					t = getContinuingTrack(to,type);
					if ( (t==null) || (track.getLayoutBlock()!=t.getLayoutBlock()) ) 
						return null;
					warnOfSkippedTurnout(frame, to.getTurnoutName(), headName);
					obj = to;					
				}
				else {
					return jmri.InstanceManager.signalHeadManagerInstance().
									getSignalHead(signalName);
				}
			}
			else if (type==LayoutEditor.TURNOUT_B) {
				// Reached turnout continuing, should be signalled
				LayoutTurnout to = (LayoutTurnout)connect;
				String signalName = to.getSignalB2Name();
				if (to.getContinuingSense()==Turnout.THROWN)
					signalName = to.getSignalC2Name();
				if ((!(signalName==null))&&(!(signalName.equals("")))) 
					auxSignal = jmri.InstanceManager.signalHeadManagerInstance().
									getSignalHead(signalName);
				if (to.getContinuingSense()==Turnout.CLOSED)
					signalName = to.getSignalB1Name();
				else
					signalName = to.getSignalC1Name();
				if ((signalName==null)||(signalName.equals("")))  {
					if (!layoutEditor.skipIncludedTurnout)
						return null;
					t = getContinuingTrack(to,type);
					if ( (t==null) || (track.getLayoutBlock()!=t.getLayoutBlock()) ) 
						return null;
					warnOfSkippedTurnout(frame, to.getTurnoutName(), headName);
					obj = to;					
				}
				else {
					return jmri.InstanceManager.signalHeadManagerInstance().
									getSignalHead(signalName);
				}
			}
			else if (type==LayoutEditor.TURNOUT_C) {
				// Reached turnout diverging, should be signalled
				LayoutTurnout to = (LayoutTurnout)connect;
				String signalName = to.getSignalC2Name();
				if (to.getContinuingSense()==Turnout.THROWN)
					signalName = to.getSignalB2Name();
				if ((!(signalName==null))&&(!(signalName.equals("")))) 
					auxSignal = jmri.InstanceManager.signalHeadManagerInstance().
									getSignalHead(signalName);
				if (to.getContinuingSense()==Turnout.CLOSED)
					signalName = to.getSignalC1Name();
				else
					signalName = to.getSignalB1Name();
				if ((signalName==null)||(signalName.equals("")))  {
					if (!layoutEditor.skipIncludedTurnout)
						return null;
					t = getContinuingTrack(to,type);
					if ( (t==null) || (track.getLayoutBlock()!=t.getLayoutBlock()) ) 
						return null;
					warnOfSkippedTurnout(frame, to.getTurnoutName(), headName);
					obj = to;					
				}
				else {
					return jmri.InstanceManager.signalHeadManagerInstance().
									getSignalHead(signalName);
				}
			}
			else if (type==LayoutEditor.TURNOUT_D) {
				// Reached turnout xover 4, should be signalled
				LayoutTurnout to = (LayoutTurnout)connect;
				String signalName = to.getSignalD2Name();
				if ((!(signalName==null))&&(!(signalName.equals("")))) 
					auxSignal = jmri.InstanceManager.signalHeadManagerInstance().
									getSignalHead(signalName);
				signalName = to.getSignalD1Name();
				if ((signalName==null)||(signalName.equals("")))   {
					if (!layoutEditor.skipIncludedTurnout)
						return null;
					t = getContinuingTrack(to,type);
					if ( (t==null) || (track.getLayoutBlock()!=t.getLayoutBlock()) ) 
						return null;
					warnOfSkippedTurnout(frame, to.getTurnoutName(), headName);
					obj = to;					
				}
				else {
					return jmri.InstanceManager.signalHeadManagerInstance().
									getSignalHead(signalName);
				}
			}
			else if (type==LayoutEditor.LEVEL_XING_A) {
				// Reached level crossing that may or may not be a block boundary
				LevelXing x = (LevelXing)connect;
				String signalName = x.getSignalAName();
				if ((signalName!=null)&&(!signalName.equals(""))) 
					return jmri.InstanceManager.signalHeadManagerInstance().
									getSignalHead(signalName);
				t=(TrackSegment)x.getConnectC();
				if (t==null) return null;
				if (track.getLayoutBlock()!=t.getLayoutBlock()) return null;
				obj = x;				
			}
			else if (type==LayoutEditor.LEVEL_XING_B) {
				// Reached level crossing that may or may not be a block boundary
				LevelXing x = (LevelXing)connect;
				String signalName = x.getSignalBName();
				if ((signalName!=null)&&(!signalName.equals(""))) 
					return jmri.InstanceManager.signalHeadManagerInstance().
									getSignalHead(signalName);
				t=(TrackSegment)x.getConnectD();
				if (t==null) return null;
				if (track.getLayoutBlock()!=t.getLayoutBlock()) return null;
				obj = x;				
			}
			else if (type==LayoutEditor.LEVEL_XING_C) {
				// Reached level crossing that may or may not be a block boundary
				LevelXing x = (LevelXing)connect;
				String signalName = x.getSignalCName();
				if ((signalName!=null)&&(!signalName.equals(""))) 
					return jmri.InstanceManager.signalHeadManagerInstance().
									getSignalHead(signalName);
				t=(TrackSegment)x.getConnectA();
				if (t==null) return null;
				if (track.getLayoutBlock()!=t.getLayoutBlock()) return null;
				obj = x;				
			}
			else if (type==LayoutEditor.LEVEL_XING_D) {
				// Reached level crossing that may or may not be a block boundary
				LevelXing x = (LevelXing)connect;
				String signalName = x.getSignalDName();
				if ((signalName!=null)&&(!signalName.equals(""))) 
					return jmri.InstanceManager.signalHeadManagerInstance().
									getSignalHead(signalName);
				t=(TrackSegment)x.getConnectB();
				if (t==null) return null;
				if (track.getLayoutBlock()!=t.getLayoutBlock()) return null;
				obj = x;				
			}
			else if (type>=LayoutEditor.TURNTABLE_RAY_OFFSET) {
				hitEndBumper = true;
				return null;
			}
		}
		return null;
	}
	private boolean hitEndBumper = false;
	private void warnOfSkippedTurnout(JFrame frame, String turnoutName, String headName) {
		JOptionPane.showMessageDialog(frame,
				java.text.MessageFormat.format(rb.getString("SignalsWarn2"),
					new Object[]{turnoutName, headName}), 
						null,JOptionPane.WARNING_MESSAGE);	
	}
	private TrackSegment getContinuingTrack(LayoutTurnout to, int type)	{
		int ty = to.getTurnoutType();
		if ( (ty==LayoutTurnout.RH_TURNOUT) || (ty==LayoutTurnout.LH_TURNOUT) || 
													(ty==LayoutTurnout.RH_TURNOUT) ) {
			if (type == LayoutEditor.TURNOUT_A) {
				if (to.getContinuingSense()==Turnout.CLOSED) return (TrackSegment)to.getConnectB();
				else return (TrackSegment)to.getConnectC();
			}
			else return (TrackSegment)to.getConnectA();
		}
		else if ( (ty==LayoutTurnout.DOUBLE_XOVER) || (ty==LayoutTurnout.RH_XOVER) ||
					(ty==LayoutTurnout.LH_XOVER) ) {
			if (type==LayoutEditor.TURNOUT_A) return (TrackSegment)to.getConnectB();
			else if (type==LayoutEditor.TURNOUT_B) return (TrackSegment)to.getConnectA();
			else if (type==LayoutEditor.TURNOUT_C) return (TrackSegment)to.getConnectD();
			else if (type==LayoutEditor.TURNOUT_D) return (TrackSegment)to.getConnectC();
		}
		log.error ("Bad connection type around turnout "+to.getTurnoutName());
		return null;				
	}
	/*
	 * Returns 'true' if an end bumper was reached during the last call to 
	 *		GetNextSignalFromObject. Also used in the odd case of reaching a
	 *		turntable ray track connection, which is treated as an end 
	 *		bumper here.
	 */
	public boolean reachedEndBumper() {return hitEndBumper;}
	/* 
	 * Returns 'true' if "track" enters a block boundary at the west(north) end of
	 *		"point". Returns "false" otherwise. If track is neither horizontal or 
	 *      vertical, assumes horizontal, as done when setting signals at block boundary.
	 *	"track" is a TrackSegment connected to "point".
	 *  "point" is an anchor point serving as a block boundary.
	 */
	public boolean isAtWestEndOfAnchor(TrackSegment t, PositionablePoint p) {
		TrackSegment tx = null;
		if (p.getConnect1()==t)
			tx = p.getConnect2();
		else if (p.getConnect2()==t)
			tx = p.getConnect1();
		else {
			log.error("track not connected to anchor point");
			return false;
		}
		Point2D point1;
		if (t.getConnect1()==p) 
			point1 = layoutEditor.getCoords(t.getConnect2(),t.getType2());
		else 
			point1 = layoutEditor.getCoords(t.getConnect1(),t.getType1());
		Point2D point2;
        if(tx!=null) {
            if (tx.getConnect1()==p)
                point2 = layoutEditor.getCoords(tx.getConnect2(),tx.getType2());
            else 
                point2 = layoutEditor.getCoords(tx.getConnect1(),tx.getType1());
        } else {
            if (t.getConnect1()==p) 
                point2 = layoutEditor.getCoords(t.getConnect1(),t.getType1());
            else 
                point2 = layoutEditor.getCoords(t.getConnect2(),t.getType2());
        }
		double delX = point1.getX() - point2.getX();
		double delY = point1.getY() - point2.getY();
		if (Math.abs(delX) > 2.0*Math.abs(delY)) {
			// track is Horizontal
			if (delX>0.0) return false;
			else return true;
		}
		else if(Math.abs(delY) > 2.0*Math.abs(delX)) {
			// track is Vertical
			if (delY>0.0) return false;
			else return true;
		}
		// track is not vertical or horizontal, assume horizontal
//		log.error ("Track is not vertical or horizontal at anchor");
		if (delX>0.0) return false;
		return true;
	}

	/** 
	 * Tool to set signals at a block boundary, including placing the signal icons and 
	 *		setup of Simple Signal Logic for each signal head
	 * <P>
	 * Block boundary must be at an Anchor Point on the LayoutEditor panel.
	 */
	// operational variables for Set Signals at Block Boundary tool
	private JmriJFrame setSignalsAtBoundaryFrame = null;
	private boolean setSignalsAtBoundaryOpen = false;
	private JTextField block1NameField = new JTextField(16);
	private JTextField block2NameField = new JTextField(16);
	private JTextField eastBoundField = new JTextField(16);
	private JTextField westBoundField = new JTextField(16);
	private JCheckBox setEastBound = new JCheckBox(rb.getString("PlaceHead"));
	private JCheckBox setupLogicEastBound = new JCheckBox(rb.getString("SetLogic"));
	private JCheckBox setWestBound = new JCheckBox(rb.getString("PlaceHead"));
	private JCheckBox setupLogicWestBound = new JCheckBox(rb.getString("SetLogic"));
	private JButton getAnchorSavedSignalHeads = null;
	private JButton changeSignalAtBoundaryIcon = null;
	private JButton setSignalsAtBoundaryDone = null;
	private JButton setSignalsAtBoundaryCancel = null;
	private LayoutBlock block1 = null;
	private LayoutBlock block2 = null;
	private TrackSegment eastTrack = null;
	private TrackSegment westTrack = null;
	private boolean trackHorizontal = false;
	private boolean trackVertical = false;
	private boolean boundaryFromMenu = false;
	private PositionablePoint boundary = null;
	private SignalHead eastBoundHead = null;
	private SignalHead westBoundHead = null;

	// display dialog for Set Signals at Block Boundary tool
	public void setSignalsAtBlockBoundaryFromMenu( PositionablePoint p, MultiIconEditor theEditor, 
					JFrame theFrame ) {
		boundaryFromMenu = true;
		boundary = p;
		block1NameField.setText(boundary.getConnect1().getLayoutBlock().getID());
		block2NameField.setText(boundary.getConnect2().getLayoutBlock().getID());
		setSignalsAtBlockBoundary(theEditor,theFrame);
		return;
	}		
		
	public void setSignalsAtBlockBoundary( MultiIconEditor theEditor, JFrame theFrame ) {
		signalIconEditor = theEditor;
		signalFrame = theFrame;
		if (setSignalsAtBoundaryOpen) {
			setSignalsAtBoundaryFrame.setVisible(true);
			return;
		}
		// Initialize if needed
		if (setSignalsAtBoundaryFrame == null) {
            setSignalsAtBoundaryFrame = new JmriJFrame( rb.getString("SignalsAtBoundary"), false, true );
            setSignalsAtBoundaryFrame.addHelpMenu("package.jmri.jmrit.display.SetSignalsAtBoundary", true);
            setSignalsAtBoundaryFrame.setLocation(70,30);
            Container theContentPane = setSignalsAtBoundaryFrame.getContentPane();  
            theContentPane.setLayout(new BoxLayout(theContentPane, BoxLayout.Y_AXIS));
			JPanel panel11 = new JPanel(); 
            panel11.setLayout(new FlowLayout());
			if (boundaryFromMenu) {
				JLabel block1NameLabel = new JLabel( rb.getString("Block")+" 1 "+
					rb.getString("Name")+" : "+boundary.getConnect1().getLayoutBlock().getID());
				panel11.add(block1NameLabel);			
			}
			else {
				JLabel block1NameLabel = new JLabel( rb.getString("Block")+" 1 "+
																rb.getString("Name")+" : ");
				panel11.add(block1NameLabel);
				panel11.add(block1NameField);
				block1NameField.setToolTipText(rb.getString("SignalsBlockNameHint"));
			}
            theContentPane.add(panel11);
			JPanel panel12 = new JPanel(); 
            panel12.setLayout(new FlowLayout());
			if (boundaryFromMenu) {
				JLabel block2NameLabel = new JLabel( rb.getString("Block")+" 2 "+
					rb.getString("Name")+" : "+boundary.getConnect2().getLayoutBlock().getID());
				panel12.add(block2NameLabel);			
			}
			else {
				JLabel block2NameLabel = new JLabel( rb.getString("Block")+" 2 "+
																rb.getString("Name")+" : ");
				panel12.add(block2NameLabel);
				panel12.add(block2NameField);
				block2NameField.setToolTipText(rb.getString("SignalsBlockNameHint"));
			}
            theContentPane.add(panel12);
			theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));
            JPanel panel2 = new JPanel();
            panel2.setLayout(new FlowLayout());
			JLabel shTitle = new JLabel(rb.getString("SignalHeads"));
			panel2.add(shTitle);
			panel2.add(new JLabel("   "));
            panel2.add(getAnchorSavedSignalHeads = new JButton(rb.getString("GetSaved")));
            getAnchorSavedSignalHeads.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						getSavedAnchorSignals(e);
					}
				});
            getAnchorSavedSignalHeads.setToolTipText( rb.getString("GetSavedHint") );			
			theContentPane.add(panel2);
            JPanel panel21 = new JPanel();
            panel21.setLayout(new FlowLayout());
			JLabel eastBoundLabel = new JLabel(rb.getString("East/SouthBound")+" : ");
			panel21.add(eastBoundLabel);
			panel21.add(eastBoundField);
			theContentPane.add(panel21);
			eastBoundField.setToolTipText(rb.getString("SignalHeadEastNameHint"));
            JPanel panel22 = new JPanel();
            panel22.setLayout(new FlowLayout());
			panel22.add(new JLabel("   "));
			panel22.add(setEastBound);
			setEastBound.setToolTipText(rb.getString("AnchorPlaceHeadHint"));
			panel22.add(new JLabel("  "));
			panel22.add(setupLogicEastBound);
			setupLogicEastBound.setToolTipText(rb.getString("SetLogicHint"));
			theContentPane.add(panel22);
            JPanel panel31 = new JPanel();
            panel31.setLayout(new FlowLayout());
			JLabel westBoundLabel = new JLabel(rb.getString("West/NorthBound")+" : ");
			panel31.add(westBoundLabel);
			panel31.add(westBoundField);
			theContentPane.add(panel31);
			westBoundField.setToolTipText(rb.getString("SignalHeadWestNameHint"));
            JPanel panel32 = new JPanel();
            panel32.setLayout(new FlowLayout());
			panel32.add(new JLabel("   "));
			panel32.add(setWestBound);
			setWestBound.setToolTipText(rb.getString("AnchorPlaceHeadHint"));
			panel32.add(new JLabel("  "));
			panel32.add(setupLogicWestBound);
			setupLogicWestBound.setToolTipText(rb.getString("SetLogicHint"));
			theContentPane.add(panel32);			
			theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));
            JPanel panel6 = new JPanel();
            panel6.setLayout(new FlowLayout());
            panel6.add(changeSignalAtBoundaryIcon = new JButton(rb.getString("ChangeSignalIcon")));
            changeSignalAtBoundaryIcon.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						signalFrame.setVisible(true);
					}
				});
            changeSignalAtBoundaryIcon.setToolTipText( rb.getString("ChangeSignalIconHint") );
			panel6.add(new JLabel("  "));
            panel6.add(setSignalsAtBoundaryDone = new JButton(rb.getString("Done")));
            setSignalsAtBoundaryDone.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setSignalsAtBoundaryDonePressed(e);
                }
            });
            setSignalsAtBoundaryDone.setToolTipText( rb.getString("SignalDoneHint") );
            panel6.add(setSignalsAtBoundaryCancel = new JButton(rb.getString("Cancel")));
            setSignalsAtBoundaryCancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setSignalsAtBoundaryCancelPressed(e);
                }
            });
            setSignalsAtBoundaryCancel.setToolTipText( rb.getString("CancelHint") );
            theContentPane.add(panel6);
			setSignalsAtBoundaryFrame.addWindowListener(new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent e) {
					setSignalsAtBoundaryCancelPressed(null);
				}
			});
			if (boundaryFromMenu) getSavedAnchorSignals(null);
		}
        setSignalsAtBoundaryFrame.pack();
        setSignalsAtBoundaryFrame.setVisible(true);		
		setSignalsAtBoundaryOpen = true;
	}
	private void getSavedAnchorSignals (ActionEvent a) {
		if ( !getBlockInformation() ) return;
		eastBoundField.setText(boundary.getEastBoundSignal());	
		westBoundField.setText(boundary.getWestBoundSignal());
	}
	private void setSignalsAtBoundaryCancelPressed (ActionEvent a) {
		setSignalsAtBoundaryOpen = false;
		boundaryFromMenu = false;
		setSignalsAtBoundaryFrame.setVisible(false);
	}
	private void setSignalsAtBoundaryDonePressed (ActionEvent a) {
		if ( !getBlockInformation() ) return;
		eastBoundHead = getSignalHeadFromEntry(eastBoundField,false,setSignalsAtBoundaryFrame);
		westBoundHead = getSignalHeadFromEntry(westBoundField,false,setSignalsAtBoundaryFrame);
		if ( (eastBoundHead==null) && (westBoundHead==null) ) {
			JOptionPane.showMessageDialog(setSignalsAtBoundaryFrame,
							rb.getString("SignalsError12"),
								rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
			return;
		}
		// place or update signals as requested
		if ( (eastBoundHead!=null) && setEastBound.isSelected() ) {
			if (isHeadOnPanel(eastBoundHead) && 
					(eastBoundHead!=getHeadFromName(boundary.getEastBoundSignal()))) { 
				JOptionPane.showMessageDialog(setSignalsAtBoundaryFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError6"),
						new Object[]{eastBoundField.getText().trim()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else if ( (!trackHorizontal) && (!trackVertical) ) {
				JOptionPane.showMessageDialog(setSignalsAtBoundaryFrame,
					rb.getString("InfoMessage3"),"",JOptionPane.INFORMATION_MESSAGE);
				if (eastBoundHead!=getHeadFromName(boundary.getEastBoundSignal())) {
					removeSignalHeadFromPanel(boundary.getEastBoundSignal());
					removeAssignment(eastBoundHead);
					boundary.setEastBoundSignal(eastBoundField.getText().trim());
				}
			}				
			else {
				removeSignalHeadFromPanel(boundary.getEastBoundSignal());
				placeEastBound();
				removeAssignment(eastBoundHead);
				boundary.setEastBoundSignal(eastBoundField.getText().trim());
				needRedraw = true;
			}		
		}
		else if ( (eastBoundHead!=null) && 
				(eastBoundHead!=getHeadFromName(boundary.getEastBoundSignal())) &&
				(eastBoundHead!=getHeadFromName(boundary.getWestBoundSignal())) ) {
			if (isHeadOnPanel(eastBoundHead)) {
				JOptionPane.showMessageDialog(setSignalsAtBoundaryFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError13"),
						new Object[]{eastBoundField.getText().trim()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}		
			else {
				removeSignalHeadFromPanel(boundary.getEastBoundSignal());
				removeAssignment(eastBoundHead);
				boundary.setEastBoundSignal(eastBoundField.getText().trim());
			}
		}
		else if ( (eastBoundHead!=null) &&  
				(eastBoundHead==getHeadFromName(boundary.getWestBoundSignal())) ) {
// need to figure out what to do in this case.			
		}
		if ( (westBoundHead!=null) && setWestBound.isSelected() ) {
			if (isHeadOnPanel(westBoundHead) &&
					(westBoundHead!=getHeadFromName(boundary.getWestBoundSignal()))) { 
				JOptionPane.showMessageDialog(setSignalsAtBoundaryFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError6"),
						new Object[]{westBoundField.getText().trim()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else if ( (!trackHorizontal) && (!trackVertical) ) {
				JOptionPane.showMessageDialog(setSignalsAtBoundaryFrame,
					rb.getString("InfoMessage3"),"",JOptionPane.INFORMATION_MESSAGE);
				if (westBoundHead!=getHeadFromName(boundary.getWestBoundSignal())) {
					removeSignalHeadFromPanel(boundary.getWestBoundSignal());
					removeAssignment(westBoundHead);
					boundary.setWestBoundSignal(westBoundField.getText().trim());
				}
			}				
			else {
				removeSignalHeadFromPanel(boundary.getWestBoundSignal());
				placeWestBound();
				removeAssignment(westBoundHead);
				boundary.setWestBoundSignal(westBoundField.getText().trim());
				needRedraw = true;
			}		
		}
		else if ( (westBoundHead!=null) && 
				(westBoundHead!=getHeadFromName(boundary.getEastBoundSignal())) &&
				(westBoundHead!=getHeadFromName(boundary.getWestBoundSignal())) ) {
			if (isHeadOnPanel(westBoundHead)) {
				JOptionPane.showMessageDialog(setSignalsAtBoundaryFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError13"),
						new Object[]{westBoundField.getText().trim()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}		
			else {
				removeSignalHeadFromPanel(boundary.getWestBoundSignal());
				removeAssignment(westBoundHead);
				boundary.setWestBoundSignal(westBoundField.getText().trim());
			}
		}
		else if ( (westBoundHead!=null) &&  
				(westBoundHead==getHeadFromName(boundary.getEastBoundSignal())) ) {
// need to figure out what to do in this case.			
		}
		if ( (eastBoundHead!=null) && setupLogicEastBound.isSelected() ) {
			setLogicEastBound();
		}
		if ( (westBoundHead!=null) && setupLogicWestBound.isSelected() ) {
			setLogicWestBound();
		}
		setSignalsAtBoundaryOpen = false;
		boundaryFromMenu = false;
		setSignalsAtBoundaryFrame.setVisible(false);	
		if (needRedraw) {
			layoutEditor.redrawPanel();
			needRedraw = false;
			layoutEditor.setDirty();
		}
	}
    /*
    * Do some thing here for end bumpers.
    *
    */
	private boolean getBlockInformation() {
        //might have to do something to trick it with an end bumper
		if (!boundaryFromMenu) {
			block1 = getBlockFromEntry(block1NameField);
			if (block1==null) return false;
			block2 = getBlockFromEntry(block2NameField);
			if (block2==null) return false;
			PositionablePoint p = null;
			boundary = null;
			for (int i = 0;(i<layoutEditor.pointList.size()) && (boundary==null);i++) {
				p = layoutEditor.pointList.get(i);
				if (p.getType() == PositionablePoint.ANCHOR) {
					LayoutBlock bA = null;
					LayoutBlock bB = null;
					if (p.getConnect1()!=null) bA = p.getConnect1().getLayoutBlock();
					if (p.getConnect2()!=null) bB = p.getConnect2().getLayoutBlock();
					if ( (bA!=null) && (bB!=null) && (bA!=bB) ) {
						if ( ( (bA==block1) && (bB==block2) ) ||
									( (bA==block2) && (bB==block1) ) ) {
							boundary = p;
						}
					}
				}
			}
			if (boundary==null) {
				JOptionPane.showMessageDialog(setSignalsAtBoundaryFrame,
							rb.getString("SignalsError7"),
									rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		// set track orientation at boundary
		eastTrack = null;
		westTrack = null;
		TrackSegment track1 = boundary.getConnect1();
		Point2D point1;
		if (track1.getConnect1()==boundary) 
			point1 = layoutEditor.getCoords(track1.getConnect2(),track1.getType2());
		else 
			point1 = layoutEditor.getCoords(track1.getConnect1(),track1.getType1());
		TrackSegment track2 = boundary.getConnect2();
		Point2D point2;
        if(boundary.getType()==PositionablePoint.END_BUMPER)
            return true;
        if (track2.getConnect1()==boundary) 
            point2 = layoutEditor.getCoords(track2.getConnect2(),track2.getType2());
        else 
            point2 = layoutEditor.getCoords(track2.getConnect1(),track2.getType1());
		double delX = point1.getX() - point2.getX();
		double delY = point1.getY() - point2.getY();
		trackVertical = false;
		trackHorizontal = false;
		if (Math.abs(delX) > 2.0*Math.abs(delY)) {
			trackHorizontal = true;
			if (delX>0.0) {
				eastTrack = track1;
				westTrack = track2;
			}
			else {
				eastTrack = track2;
				westTrack = track1;
			}
		}
		if (Math.abs(delY) > 2.0*Math.abs(delX)) {
			trackVertical = true;
			if (delY>0.0) {
				eastTrack = track1;		// south
				westTrack = track2;		// north
			}
			else {
				eastTrack = track2;		// south
				westTrack = track1;		// north
			}
		}
		if (eastTrack==null) {
			// did not meet the horizontal or vertical test, assume horizontal
			if (delX>0.0) {
				eastTrack = track1;
				westTrack = track2;
			}
			else {
				eastTrack = track2;
				westTrack = track1;
			}						
		}
		return true;
	}		
	private LayoutBlock getBlockFromEntry(JTextField blockNameField) {
		String str = blockNameField.getText().trim();
		if ( (str==null) || (str.equals("")) ) {
			JOptionPane.showMessageDialog(setSignalsAtBoundaryFrame,rb.getString("SignalsError9"),
									rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
			return null;
		}
		LayoutBlock block = jmri.InstanceManager.layoutBlockManagerInstance().
														getByUserName(str);
		if (block==null) {
			JOptionPane.showMessageDialog(setSignalsAtBoundaryFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError10"),
						new Object[]{str}), rb.getString("Error"),
							JOptionPane.ERROR_MESSAGE);
			return null ;
		}
		if ( !block.isOnPanel(layoutEditor) ) {
			JOptionPane.showMessageDialog(setSignalsAtBoundaryFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError11"),
						new Object[]{str}), rb.getString("Error"),
							JOptionPane.ERROR_MESSAGE);
			return null ;
		}
		return (block);
	}
	private void placeEastBound() {
		if (testIcon == null)
			testIcon = signalIconEditor.getIcon(0);
		Point2D p = boundary.getCoords();
		if (trackHorizontal) {
			setSignalHeadOnPanel(2,eastBoundField.getText().trim(),
				(int)(p.getX()-testIcon.getIconHeight()-8),
				(int)(p.getY()-testIcon.getIconWidth()) );
		}
		else if (trackVertical) {
			setSignalHeadOnPanel(1,eastBoundField.getText().trim(),
				(int)(p.getX()-4-testIcon.getIconHeight()),
				(int)(p.getY()-4-testIcon.getIconWidth()) );
		}
	}
	private void placeWestBound() {
		if (testIcon == null)
			testIcon = signalIconEditor.getIcon(0);
		Point2D p = boundary.getCoords();
		if (trackHorizontal) {
			setSignalHeadOnPanel(0,westBoundField.getText().trim(),
				(int)(p.getX()+4),
				(int)(p.getY()+5) );
		}
		else if (trackVertical) {
			setSignalHeadOnPanel(3,westBoundField.getText().trim(),
				(int)(p.getX()+5),
				(int)(p.getY())+4 );
		}	
	}
	private void setLogicEastBound() {
		LayoutBlock eastBlock = eastTrack.getLayoutBlock();
		Sensor eastBlockOccupancy = eastBlock.getOccupancySensor();
		if (eastBlockOccupancy==null) {
			JOptionPane.showMessageDialog(setSignalsAtBoundaryFrame,
				java.text.MessageFormat.format(rb.getString("InfoMessage4"),
					new Object[]{eastBlock.getUserName()}), 
						null,JOptionPane.INFORMATION_MESSAGE);						
			return;
		}
		SignalHead nextHead = getNextSignalFromObject(eastTrack,
				boundary, eastBoundField.getText().trim(), setSignalsAtBoundaryFrame);
		if ( (nextHead==null) && (!reachedEndBumper()) ) {
			JOptionPane.showMessageDialog(setSignalsAtBoundaryFrame,
				java.text.MessageFormat.format(rb.getString("InfoMessage5"),
					new Object[]{eastBlock.getUserName()}), 
						null,JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		if (!initializeBlockBossLogic(eastBoundField.getText().trim())) return;
		logic.setMode(BlockBossLogic.SINGLEBLOCK);
		logic.setSensor1(eastBlockOccupancy.getSystemName());
		if (nextHead!=null) {
			logic.setWatchedSignal1(nextHead.getSystemName(),false);
		}
		if (auxSignal!=null) {
			logic.setWatchedSignal1Alt(auxSignal.getSystemName());
		}
		finalizeBlockBossLogic();
	}
	private void setLogicWestBound() {
		LayoutBlock westBlock = westTrack.getLayoutBlock();
		Sensor westBlockOccupancy = westBlock.getOccupancySensor();
		if (westBlockOccupancy==null) {
			JOptionPane.showMessageDialog(setSignalsAtBoundaryFrame,
				java.text.MessageFormat.format(rb.getString("InfoMessage4"),
					new Object[]{westBlock.getUserName()}), 
						null,JOptionPane.INFORMATION_MESSAGE);			
			return;
		}
		SignalHead nextHead = getNextSignalFromObject(westTrack,
				boundary, westBoundField.getText().trim(), setSignalsAtBoundaryFrame);
		if ( (nextHead==null) && (!reachedEndBumper()) ) {
			JOptionPane.showMessageDialog(setSignalsAtBoundaryFrame,
				java.text.MessageFormat.format(rb.getString("InfoMessage5"),
					new Object[]{westBlock.getUserName()}), 
						null,JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		if (!initializeBlockBossLogic(westBoundField.getText().trim())) return;
		logic.setMode(BlockBossLogic.SINGLEBLOCK);
		logic.setSensor1(westBlockOccupancy.getSystemName());
		if (nextHead!=null) {
			logic.setWatchedSignal1(nextHead.getSystemName(),false);
		}
		if (auxSignal!=null) {
			logic.setWatchedSignal1Alt(auxSignal.getSystemName());
		}
		finalizeBlockBossLogic();
	}

	/**
	 * Tool to set signals at a double crossover turnout, including placing the 
	 *		signal icons and setup of Simple Signal Logic for each signal head
	 * <P>
	 * This tool assumes left facing signal head icons have been selected, and 
	 *		will rotate the signal head icons accordingly.
	 * <P>
	 * This tool will place icons on the outside edge of the turnout.
	 * <P>
	 * At least one signal at each of the four connection points is 
	 *		required. A second signal at each is optional.
	 * <P>
	 * This tool only places signal icons if the turnout is either mostly vertical 
	 *		or mostly horizontal. Some user adjustment may be needed.
	 */	
	
	// operational variables for Set Signals at Double Crossover Turnout tool
	private JmriJFrame setSignalsAtXoverFrame = null;
	private boolean setSignalsAtXoverOpen = false;
	//private JTextField xoverTurnoutNameField = new JTextField(16);
	private JTextField a1Field = new JTextField(16);
	private JTextField a2Field = new JTextField(16);
	private JTextField b1Field = new JTextField(16);
	private JTextField b2Field = new JTextField(16);
	private JTextField c1Field = new JTextField(16);
	private JTextField c2Field = new JTextField(16);
	private JTextField d1Field = new JTextField(16);
	private JTextField d2Field = new JTextField(16);
	private JCheckBox setA1Head = new JCheckBox(rb.getString("PlaceHead"));
	private JCheckBox setupA1Logic = new JCheckBox(rb.getString("SetLogic"));
	private JCheckBox setA2Head = new JCheckBox(rb.getString("PlaceHead"));
	private JCheckBox setupA2Logic = new JCheckBox(rb.getString("SetLogic"));
	private JCheckBox setB1Head = new JCheckBox(rb.getString("PlaceHead"));
	private JCheckBox setupB1Logic = new JCheckBox(rb.getString("SetLogic"));
	private JCheckBox setB2Head = new JCheckBox(rb.getString("PlaceHead"));
	private JCheckBox setupB2Logic = new JCheckBox(rb.getString("SetLogic"));
	private JCheckBox setC1Head = new JCheckBox(rb.getString("PlaceHead"));
	private JCheckBox setupC1Logic = new JCheckBox(rb.getString("SetLogic"));
	private JCheckBox setC2Head = new JCheckBox(rb.getString("PlaceHead"));
	private JCheckBox setupC2Logic = new JCheckBox(rb.getString("SetLogic"));
	private JCheckBox setD1Head = new JCheckBox(rb.getString("PlaceHead"));
	private JCheckBox setupD1Logic = new JCheckBox(rb.getString("SetLogic"));
	private JCheckBox setD2Head = new JCheckBox(rb.getString("PlaceHead"));
	private JCheckBox setupD2Logic = new JCheckBox(rb.getString("SetLogic"));
	private JButton getSavedXoverSignalHeads = null;
	private JButton changeXoverSignalIcon = null;
	private JButton setXoverSignalsDone = null;
	private JButton setXoverSignalsCancel = null;
	private boolean xoverFromMenu = false;
	private SignalHead a1Head = null;
	private SignalHead a2Head = null;
	private SignalHead b1Head = null;
	private SignalHead b2Head = null;
	private SignalHead c1Head = null;
	private SignalHead c2Head = null;
	private SignalHead d1Head = null;
	private SignalHead d2Head = null;
	private int xoverType = LayoutTurnout.DOUBLE_XOVER;  // changes to RH_XOVER or LH_XOVER as required
	private String xoverTurnoutName = "";
	private JLabel xoverTurnoutNameLabel = new JLabel("");

	// display dialog for Set Signals at Crossover Turnout tool
	public void setSignalsAtXoverTurnoutFromMenu( LayoutTurnout to, 
					MultiIconEditor theEditor, JFrame theFrame ) {
		xoverFromMenu = true;
		layoutTurnout = to;
		turnout = to.getTurnout();
		xoverType = layoutTurnout.getTurnoutType();
		if ( (xoverType!=LayoutTurnout.DOUBLE_XOVER) && (xoverType!=LayoutTurnout.RH_XOVER) && 
											(xoverType!=LayoutTurnout.LH_XOVER) ) {
			log.error ("entered Set Signals at XOver, with a non-crossover turnout");
			return;
		}
		xoverTurnoutName = layoutTurnout.getTurnoutName();
		setSignalsAtXoverTurnout(theEditor,theFrame);
	}
	public void setSignalsAtXoverTurnout( MultiIconEditor theEditor, JFrame theFrame ) {
		signalIconEditor = theEditor;
		signalFrame = theFrame;
		if (!xoverFromMenu) {
			xoverTurnoutName = JOptionPane.showInputDialog(layoutEditor, 
											rb.getString("EnterXOverTurnout")+" :");
			if (xoverTurnoutName.length()<3) return;  // cancelled			
		}
		if (!getTurnoutInformation(true))
			return;
		xoverTurnoutNameLabel.setText(rb.getString("Turnout")+" "+
							rb.getString("Name")+" : "+xoverTurnoutName);
		xoverType = layoutTurnout.getTurnoutType();			
		if (setSignalsAtXoverOpen) {
			setSignalsAtXoverFrame.setVisible(true);
			return;
		}
		// Initialize if needed
		if (setSignalsAtXoverFrame == null) {
            setSignalsAtXoverFrame = new JmriJFrame( rb.getString("SignalsAtXoverTurnout"), false, true );
            setSignalsAtXoverFrame.addHelpMenu("package.jmri.jmrit.display.SetSignalsAtXoverTurnout", true);
            setSignalsAtXoverFrame.setLocation(70,30);
            Container theContentPane = setSignalsAtXoverFrame.getContentPane();        
            theContentPane.setLayout(new BoxLayout(theContentPane, BoxLayout.Y_AXIS));
			JPanel panel1 = new JPanel(); 
            panel1.setLayout(new FlowLayout());
			panel1.add(xoverTurnoutNameLabel);
            theContentPane.add(panel1);
			theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));
            JPanel panel2 = new JPanel();
            panel2.setLayout(new FlowLayout());
			JLabel shTitle = new JLabel(rb.getString("SignalHeads"));
			panel2.add(shTitle);
			panel2.add(new JLabel("   "));
            panel2.add(getSavedXoverSignalHeads = new JButton(rb.getString("GetSaved")));
            getSavedXoverSignalHeads.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						xoverTurnoutSignalsGetSaved(e);
					}
				});
            getSavedXoverSignalHeads.setToolTipText( rb.getString("GetSavedHint") );			
			theContentPane.add(panel2);
            JPanel panel21 = new JPanel();
            panel21.setLayout(new FlowLayout());
			JLabel a1Label = new JLabel(rb.getString("AContinuing")+" : ");
			panel21.add(a1Label);
			panel21.add(a1Field);
			theContentPane.add(panel21);
			a1Field.setToolTipText(rb.getString("SignalHeadNameHint"));
            JPanel panel22 = new JPanel();
            panel22.setLayout(new FlowLayout());
			panel22.add(new JLabel("   "));
			panel22.add(setA1Head);
			setA1Head.setToolTipText(rb.getString("PlaceHeadHint"));
			panel22.add(new JLabel("  "));
			panel22.add(setupA1Logic);
			setupA1Logic.setToolTipText(rb.getString("SetLogicHint"));
			theContentPane.add(panel22);
			if (!(xoverType == LayoutTurnout.LH_XOVER)) {
				JPanel panel23 = new JPanel();
				panel23.setLayout(new FlowLayout());
				JLabel a2Label = new JLabel(rb.getString("ADiverging")+" : ");
				panel23.add(a2Label);
				panel23.add(a2Field);
				theContentPane.add(panel23);
				a2Field.setToolTipText(rb.getString("SignalHeadNameHint"));
				JPanel panel24 = new JPanel();
				panel24.setLayout(new FlowLayout());
				panel24.add(new JLabel("   "));
				panel24.add(setA2Head);
				setA2Head.setToolTipText(rb.getString("PlaceHeadHint"));
				panel24.add(new JLabel("  "));
				panel24.add(setupA2Logic);
				setupA2Logic.setToolTipText(rb.getString("SetLogicHint"));
				theContentPane.add(panel24);
			}
            JPanel panel31 = new JPanel();
            panel31.setLayout(new FlowLayout());
			JLabel b1Label = new JLabel(rb.getString("BContinuing")+" : ");
			panel31.add(b1Label);
			panel31.add(b1Field);
			theContentPane.add(panel31);
			b1Field.setToolTipText(rb.getString("SignalHeadNameHint"));
            JPanel panel32 = new JPanel();
            panel32.setLayout(new FlowLayout());
			panel32.add(new JLabel("   "));
			panel32.add(setB1Head);
			setB1Head.setToolTipText(rb.getString("PlaceHeadHint"));
			panel32.add(new JLabel("  "));
			panel32.add(setupB1Logic);
			setupB1Logic.setToolTipText(rb.getString("SetLogicHint"));
			theContentPane.add(panel32);
			if (!(xoverType == LayoutTurnout.RH_XOVER)) {
				JPanel panel33 = new JPanel();
				panel33.setLayout(new FlowLayout());
				JLabel b2Label = new JLabel(rb.getString("BDiverging")+" : ");
				panel33.add(b2Label);
				panel33.add(b2Field);
				theContentPane.add(panel33);
				b2Field.setToolTipText(rb.getString("SignalHeadNameHint"));
				JPanel panel34 = new JPanel();
				panel34.setLayout(new FlowLayout());
				panel34.add(new JLabel("   "));
				panel34.add(setB2Head);
				setB2Head.setToolTipText(rb.getString("PlaceHeadHint"));
				panel34.add(new JLabel("  "));
				panel34.add(setupB2Logic);
				setupB2Logic.setToolTipText(rb.getString("SetLogicHint"));
				theContentPane.add(panel34);
			}
            JPanel panel41 = new JPanel();
            panel41.setLayout(new FlowLayout());
			JLabel c1Label = new JLabel(rb.getString("CContinuing")+" : ");
			panel41.add(c1Label);
			panel41.add(c1Field);
			theContentPane.add(panel41);
			c1Field.setToolTipText(rb.getString("SignalHeadNameHint"));
            JPanel panel42 = new JPanel();
            panel42.setLayout(new FlowLayout());
			panel42.add(new JLabel("   "));
			panel42.add(setC1Head);
			setC1Head.setToolTipText(rb.getString("PlaceHeadHint"));
			panel42.add(new JLabel("  "));
			panel42.add(setupC1Logic);
			setupC1Logic.setToolTipText(rb.getString("SetLogicHint"));
			theContentPane.add(panel42);
			if (!(xoverType == LayoutTurnout.LH_XOVER)) {
				JPanel panel43 = new JPanel();
				panel43.setLayout(new FlowLayout());
				JLabel c2Label = new JLabel(rb.getString("CDiverging")+" : ");
				panel43.add(c2Label);
				panel43.add(c2Field);
				theContentPane.add(panel43);
				c2Field.setToolTipText(rb.getString("SignalHeadNameHint"));
				JPanel panel44 = new JPanel();
				panel44.setLayout(new FlowLayout());
				panel44.add(new JLabel("   "));
				panel44.add(setC2Head);
				setC2Head.setToolTipText(rb.getString("PlaceHeadHint"));
				panel44.add(new JLabel("  "));
				panel44.add(setupC2Logic);
				setupC2Logic.setToolTipText(rb.getString("SetLogicHint"));
				theContentPane.add(panel44);
			}
            JPanel panel51 = new JPanel();
            panel51.setLayout(new FlowLayout());
			JLabel d1Label = new JLabel(rb.getString("DContinuing")+" : ");
			panel51.add(d1Label);
			panel51.add(d1Field);
			theContentPane.add(panel51);
			d1Field.setToolTipText(rb.getString("SignalHeadNameHint"));
            JPanel panel52 = new JPanel();
            panel52.setLayout(new FlowLayout());
			panel52.add(new JLabel("   "));
			panel52.add(setD1Head);
			setD1Head.setToolTipText(rb.getString("PlaceHeadHint"));
			panel52.add(new JLabel("  "));
			panel52.add(setupD1Logic);
			setupD1Logic.setToolTipText(rb.getString("SetLogicHint"));
			theContentPane.add(panel52);
			if (!(xoverType == LayoutTurnout.RH_XOVER)) {
				JPanel panel53 = new JPanel();
				panel53.setLayout(new FlowLayout());
				JLabel d2Label = new JLabel(rb.getString("DDiverging")+" : ");
				panel53.add(d2Label);
				panel53.add(d2Field);
				theContentPane.add(panel53);
				d2Field.setToolTipText(rb.getString("SignalHeadNameHint"));
				JPanel panel54 = new JPanel();
				panel54.setLayout(new FlowLayout());
				panel54.add(new JLabel("   "));
				panel54.add(setD2Head);
				setD2Head.setToolTipText(rb.getString("PlaceHeadHint"));
				panel54.add(new JLabel("  "));
				panel54.add(setupD2Logic);
				setupD2Logic.setToolTipText(rb.getString("SetLogicHint"));
				theContentPane.add(panel54);
			}
			theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));
            JPanel panel6 = new JPanel();
            panel6.setLayout(new FlowLayout());
            panel6.add(changeXoverSignalIcon = new JButton(rb.getString("ChangeSignalIcon")));
            changeXoverSignalIcon.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						signalFrame.setVisible(true);
					}
				});
            changeXoverSignalIcon.setToolTipText( rb.getString("ChangeSignalIconHint") );
			panel6.add(new JLabel("  "));
            panel6.add(setXoverSignalsDone = new JButton(rb.getString("Done")));
            setXoverSignalsDone.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setXoverSignalsDonePressed(e);
                }
            });
            setXoverSignalsDone.setToolTipText( rb.getString("SignalDoneHint") );
            panel6.add(setXoverSignalsCancel = new JButton(rb.getString("Cancel")));
            setXoverSignalsCancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setXoverSignalsCancelPressed(e);
                }
            });
            setXoverSignalsCancel.setToolTipText( rb.getString("CancelHint") );
            theContentPane.add(panel6);
			setSignalsAtXoverFrame.addWindowListener(new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent e) {
					setXoverSignalsCancelPressed(null);
				}
			});
			if (xoverFromMenu) xoverTurnoutSignalsGetSaved(null);
		}
        setSignalsAtXoverFrame.pack();
        setSignalsAtXoverFrame.setVisible(true);		
		setSignalsAtXoverOpen = true;
	}	
	private void xoverTurnoutSignalsGetSaved (ActionEvent a) {
		a1Field.setText(layoutTurnout.getSignalA1Name());	
		a2Field.setText(layoutTurnout.getSignalA2Name());
		b1Field.setText(layoutTurnout.getSignalB1Name());
		b2Field.setText(layoutTurnout.getSignalB2Name());
		c1Field.setText(layoutTurnout.getSignalC1Name());	
		c2Field.setText(layoutTurnout.getSignalC2Name());	
		d1Field.setText(layoutTurnout.getSignalD1Name());	
		d2Field.setText(layoutTurnout.getSignalD2Name());	
	}
	private void setXoverSignalsCancelPressed (ActionEvent a) {
		setSignalsAtXoverOpen = false;
		xoverFromMenu = false;
		setSignalsAtXoverFrame.setVisible(false);
	}
	private void setXoverSignalsDonePressed (ActionEvent a) {
		if ( !getXoverSignalHeadInformation() ) return;
		// place signal icons if requested, and assign signal heads to this turnout
		if (setA1Head.isSelected()) {
			if (isHeadOnPanel(a1Head) &&
				(a1Head!=getHeadFromName(layoutTurnout.getSignalA1Name()))) {
				JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError6"),
						new Object[]{a1Field.getText().trim()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else if ( (!layoutTurnoutHorizontal) && (!layoutTurnoutVertical) ) {
				JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
					rb.getString("InfoMessage2"),"",JOptionPane.INFORMATION_MESSAGE);
				if (a1Head!=getHeadFromName(layoutTurnout.getSignalA1Name())) {				
					removeSignalHeadFromPanel(layoutTurnout.getSignalA1Name());
					removeAssignment(a1Head);
					layoutTurnout.setSignalA1Name(a1Field.getText().trim());
				}
			}				
			else {
				removeSignalHeadFromPanel(layoutTurnout.getSignalA1Name());
				placeA1();
				removeAssignment(a1Head);
				layoutTurnout.setSignalA1Name(a1Field.getText().trim());
				needRedraw = true;
			}		
		}
		else {
			int assigned = isHeadAssignedHere(a1Head,layoutTurnout);
			if (assigned == NONE) {
				if ( isHeadOnPanel(a1Head) && 
									isHeadAssignedAnywhere(a1Head) ) {
					JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
						java.text.MessageFormat.format(rb.getString("SignalsError8"),
							new Object[]{a1Field.getText().trim()}), 
								rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
					return;
				}		
				else {
					removeSignalHeadFromPanel(layoutTurnout.getSignalA1Name());
					removeAssignment(a1Head);
					layoutTurnout.setSignalA1Name(a1Field.getText().trim());
				}
			}
			else if (assigned!=A1) {
// need to figure out what to do in this case.			
			}
		}
		if ( (a2Head!=null) && setA2Head.isSelected() ) {
			if (isHeadOnPanel(a2Head) &&
				(a2Head!=getHeadFromName(layoutTurnout.getSignalA2Name()))) {
				JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError6"),
						new Object[]{a2Field.getText().trim()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else if ( (!layoutTurnoutHorizontal) && (!layoutTurnoutVertical) ) {
				JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
					rb.getString("InfoMessage2"),"",JOptionPane.INFORMATION_MESSAGE);
				if (a2Head!=getHeadFromName(layoutTurnout.getSignalA2Name())) {				
					removeSignalHeadFromPanel(layoutTurnout.getSignalA2Name());
					removeAssignment(a2Head);
					layoutTurnout.setSignalA2Name(a2Field.getText().trim());
				}
			}				
			else {
				removeSignalHeadFromPanel(layoutTurnout.getSignalA2Name());
				placeA2();
				removeAssignment(a2Head);
				layoutTurnout.setSignalA2Name(a2Field.getText().trim());
				needRedraw = true;
			}		
		}
		else if (a2Head!=null) {
			int assigned = isHeadAssignedHere(a2Head,layoutTurnout);
			if (assigned == NONE) {
				if ( isHeadOnPanel(a2Head) && 
									isHeadAssignedAnywhere(a2Head) ) {
					JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
						java.text.MessageFormat.format(rb.getString("SignalsError8"),
							new Object[]{a2Field.getText().trim()}), 
								rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
					return;
				}		
				else {
					removeSignalHeadFromPanel(layoutTurnout.getSignalA2Name());
					removeAssignment(a2Head);
					layoutTurnout.setSignalA2Name(a2Field.getText().trim());
				}
			}
			else if (assigned!=A2) {
// need to figure out what to do in this case.			
			}
		}
		else if (a2Head==null) {
			removeSignalHeadFromPanel(layoutTurnout.getSignalA2Name());
			layoutTurnout.setSignalA2Name("");
		}
		if (setB1Head.isSelected()) {
			if (isHeadOnPanel(b1Head) &&
				(b1Head!=getHeadFromName(layoutTurnout.getSignalB1Name()))) {
				JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError6"),
						new Object[]{b1Field.getText().trim()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else if ( (!layoutTurnoutHorizontal) && (!layoutTurnoutVertical) ) {
				JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
					rb.getString("InfoMessage2"),"",JOptionPane.INFORMATION_MESSAGE);
				if (b1Head!=getHeadFromName(layoutTurnout.getSignalB1Name())) {				
					removeSignalHeadFromPanel(layoutTurnout.getSignalB1Name());
					removeAssignment(b1Head);
					layoutTurnout.setSignalB1Name(b1Field.getText().trim());
				}
			}				
			else {
				removeSignalHeadFromPanel(layoutTurnout.getSignalB1Name());
				placeB1();
				removeAssignment(b1Head);
				layoutTurnout.setSignalB1Name(b1Field.getText().trim());
				needRedraw = true;
			}		
		}
		else {
			int assigned = isHeadAssignedHere(b1Head,layoutTurnout);
			if (assigned == NONE) {
				if ( isHeadOnPanel(b1Head) && 
									isHeadAssignedAnywhere(b1Head) ) {
					JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
						java.text.MessageFormat.format(rb.getString("SignalsError8"),
							new Object[]{b1Field.getText().trim()}), 
								rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
					return;
				}		
				else {
					removeSignalHeadFromPanel(layoutTurnout.getSignalB1Name());
					removeAssignment(b1Head);
					layoutTurnout.setSignalB1Name(b1Field.getText().trim());
				}
			}
			else if (assigned!=B1) {
// need to figure out what to do in this case.			
			}
		}
		if ( (b2Head!=null) && setB2Head.isSelected() ) {
			if (isHeadOnPanel(b2Head) &&
				(b2Head!=getHeadFromName(layoutTurnout.getSignalB2Name()))) {
				JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError6"),
						new Object[]{b2Field.getText().trim()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else if ( (!layoutTurnoutHorizontal) && (!layoutTurnoutVertical) ) {
				JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
					rb.getString("InfoMessage2"),"",JOptionPane.INFORMATION_MESSAGE);
				if (b2Head!=getHeadFromName(layoutTurnout.getSignalB2Name())) {				
					removeSignalHeadFromPanel(layoutTurnout.getSignalB2Name());
					removeAssignment(b2Head);
					layoutTurnout.setSignalB2Name(b2Field.getText().trim());
				}
			}				
			else {
				removeSignalHeadFromPanel(layoutTurnout.getSignalB2Name());
				placeB2();
				removeAssignment(b2Head);
				layoutTurnout.setSignalB2Name(b2Field.getText().trim());
				needRedraw = true;
			}		
		}
		else if (b2Head!=null) {
			int assigned = isHeadAssignedHere(b2Head,layoutTurnout);
			if (assigned == NONE) {
				if ( isHeadOnPanel(b2Head) && 
									isHeadAssignedAnywhere(b2Head) ) {
					JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
						java.text.MessageFormat.format(rb.getString("SignalsError8"),
							new Object[]{b2Field.getText().trim()}), 
								rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
					return;
				}		
				else {
					removeSignalHeadFromPanel(layoutTurnout.getSignalB2Name());
					removeAssignment(b2Head);
					layoutTurnout.setSignalB2Name(b2Field.getText().trim());
				}
			}
			else if (assigned!=B2) {
// need to figure out what to do in this case.			
			}
		}
		else if (b2Head==null) {
			removeSignalHeadFromPanel(layoutTurnout.getSignalB2Name());
			layoutTurnout.setSignalB2Name("");
		}
		if (setC1Head.isSelected()) {
			if (isHeadOnPanel(c1Head) &&
				(c1Head!=getHeadFromName(layoutTurnout.getSignalC1Name()))) {
				JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError6"),
						new Object[]{c1Field.getText().trim()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else if ( (!layoutTurnoutHorizontal) && (!layoutTurnoutVertical) ) {
				JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
					rb.getString("InfoMessage2"),"",JOptionPane.INFORMATION_MESSAGE);
				if (c1Head!=getHeadFromName(layoutTurnout.getSignalC1Name())) {				
					removeSignalHeadFromPanel(layoutTurnout.getSignalC1Name());
					removeAssignment(c1Head);
					layoutTurnout.setSignalC1Name(c1Field.getText().trim());
				}
			}				
			else {
				removeSignalHeadFromPanel(layoutTurnout.getSignalC1Name());
				placeC1();
				removeAssignment(c1Head);
				layoutTurnout.setSignalC1Name(c1Field.getText().trim());
				needRedraw = true;
			}		
		}
		else {
			int assigned = isHeadAssignedHere(c1Head,layoutTurnout);
			if (assigned == NONE) {
				if ( isHeadOnPanel(c1Head) && 
									isHeadAssignedAnywhere(c1Head) ) {
					JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
						java.text.MessageFormat.format(rb.getString("SignalsError8"),
							new Object[]{c1Field.getText().trim()}), 
								rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
					return;
				}		
				else {
					removeSignalHeadFromPanel(layoutTurnout.getSignalC1Name());
					removeAssignment(c1Head);
					layoutTurnout.setSignalC1Name(c1Field.getText().trim());
				}
			}
			else if (assigned!=C1) {
// need to figure out what to do in this case.			
			}
		}
		if ( (c2Head!=null) && setC2Head.isSelected() ) {
			if (isHeadOnPanel(c2Head) &&
				(c2Head!=getHeadFromName(layoutTurnout.getSignalC2Name()))) {
				JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError6"),
						new Object[]{c2Field.getText().trim()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else if ( (!layoutTurnoutHorizontal) && (!layoutTurnoutVertical) ) {
				JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
					rb.getString("InfoMessage2"),"",JOptionPane.INFORMATION_MESSAGE);
				if (c2Head!=getHeadFromName(layoutTurnout.getSignalC2Name())) {				
					removeSignalHeadFromPanel(layoutTurnout.getSignalC2Name());
					removeAssignment(c2Head);
					layoutTurnout.setSignalC2Name(c2Field.getText().trim());
				}
			}				
			else {
				removeSignalHeadFromPanel(layoutTurnout.getSignalC2Name());
				placeC2();
				removeAssignment(c2Head);
				layoutTurnout.setSignalC2Name(c2Field.getText().trim());
				needRedraw = true;
			}		
		}
		else if (c2Head!=null) {
			int assigned = isHeadAssignedHere(c2Head,layoutTurnout);
			if (assigned == NONE) {
				if ( isHeadOnPanel(c2Head) && 
									isHeadAssignedAnywhere(c2Head) ) {
					JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
						java.text.MessageFormat.format(rb.getString("SignalsError8"),
							new Object[]{c2Field.getText().trim()}), 
								rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
					return;
				}		
				else {
					removeSignalHeadFromPanel(layoutTurnout.getSignalC2Name());
					removeAssignment(c2Head);
					layoutTurnout.setSignalC2Name(c2Field.getText().trim());
				}
			}
			else if (assigned!=C2) {
// need to figure out what to do in this case.			
			}
		}
		else if (c2Head==null) {
			removeSignalHeadFromPanel(layoutTurnout.getSignalC2Name());
			layoutTurnout.setSignalC2Name("");
		}
		if (setD1Head.isSelected()) {
			if (isHeadOnPanel(d1Head) &&
				(d1Head!=getHeadFromName(layoutTurnout.getSignalD1Name()))) {
				JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError6"),
						new Object[]{d1Field.getText().trim()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else if ( (!layoutTurnoutHorizontal) && (!layoutTurnoutVertical) ) {
				JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
					rb.getString("InfoMessage2"),"",JOptionPane.INFORMATION_MESSAGE);
				if (d1Head!=getHeadFromName(layoutTurnout.getSignalD1Name())) {				
					removeSignalHeadFromPanel(layoutTurnout.getSignalD1Name());
					removeAssignment(d1Head);
					layoutTurnout.setSignalD1Name(d1Field.getText().trim());
				}
			}				
			else {
				removeSignalHeadFromPanel(layoutTurnout.getSignalD1Name());
				placeD1();
				removeAssignment(d1Head);
				layoutTurnout.setSignalD1Name(d1Field.getText().trim());
				needRedraw = true;
			}		
		}
		else {
			int assigned = isHeadAssignedHere(d1Head,layoutTurnout);
			if (assigned == NONE) {
				if ( isHeadOnPanel(d1Head) && 
									isHeadAssignedAnywhere(d1Head) ) {
					JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
						java.text.MessageFormat.format(rb.getString("SignalsError8"),
							new Object[]{d1Field.getText().trim()}), 
								rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
					return;
				}		
				else {
					removeSignalHeadFromPanel(layoutTurnout.getSignalD1Name());
					removeAssignment(d1Head);
					layoutTurnout.setSignalD1Name(d1Field.getText().trim());
				}
			}
			else if (assigned!=D1) {
// need to figure out what to do in this case.			
			}
		}
		if ( (d2Head!=null) && setD2Head.isSelected() ) {
			if (isHeadOnPanel(d2Head) &&
				(d2Head!=getHeadFromName(layoutTurnout.getSignalD2Name()))) {
				JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError6"),
						new Object[]{d2Field.getText().trim()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else if ( (!layoutTurnoutHorizontal) && (!layoutTurnoutVertical) ) {
				JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
					rb.getString("InfoMessage2"),"",JOptionPane.INFORMATION_MESSAGE);
				if (d2Head!=getHeadFromName(layoutTurnout.getSignalD2Name())) {				
					removeSignalHeadFromPanel(layoutTurnout.getSignalD2Name());
					removeAssignment(d2Head);
					layoutTurnout.setSignalD2Name(d2Field.getText().trim());
				}
			}				
			else {
				removeSignalHeadFromPanel(layoutTurnout.getSignalD2Name());
				placeD2();
				removeAssignment(d2Head);
				layoutTurnout.setSignalD2Name(d2Field.getText().trim());
				needRedraw = true;
			}		
		}
		else if (d2Head!=null) {
			int assigned = isHeadAssignedHere(d2Head,layoutTurnout);
			if (assigned == NONE) {
				if ( isHeadOnPanel(d2Head) && 
									isHeadAssignedAnywhere(d2Head) ) {
					JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
						java.text.MessageFormat.format(rb.getString("SignalsError8"),
							new Object[]{d2Field.getText().trim()}), 
								rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
					return;
				}		
				else {
					removeSignalHeadFromPanel(layoutTurnout.getSignalD2Name());
					removeAssignment(d2Head);
					layoutTurnout.setSignalD2Name(d2Field.getText().trim());
				}
			}
			else if (assigned!=D2) {
// need to figure out what to do in this case.			
			}
		}
		else if (d2Head==null) {
			removeSignalHeadFromPanel(layoutTurnout.getSignalD2Name());
			layoutTurnout.setSignalD2Name("");
		}
		// setup logic if requested
		if (setupA1Logic.isSelected() || setupA2Logic.isSelected()) {
			if (xoverType == LayoutTurnout.LH_XOVER) {
				setLogicXoverContinuing(a1Head,(TrackSegment)layoutTurnout.getConnectB());
			}
			else {
				setLogicXover(a1Head,(TrackSegment)layoutTurnout.getConnectB(),a2Head,
						(TrackSegment)layoutTurnout.getConnectC(),setupA1Logic.isSelected(),
								setupA2Logic.isSelected());
			}
		}
		if (setupB1Logic.isSelected() || setupB2Logic.isSelected()) {
			if (xoverType == LayoutTurnout.RH_XOVER) {
				setLogicXoverContinuing(b1Head,(TrackSegment)layoutTurnout.getConnectA());
			}
			else {
				setLogicXover(b1Head,(TrackSegment)layoutTurnout.getConnectA(),b2Head,
						(TrackSegment)layoutTurnout.getConnectD(),setupB1Logic.isSelected(),
								setupB2Logic.isSelected());
			}
		}
		if (setupC1Logic.isSelected() || setupC2Logic.isSelected()) {
			if (xoverType == LayoutTurnout.LH_XOVER) {
				setLogicXoverContinuing(c1Head,(TrackSegment)layoutTurnout.getConnectD());			
			}
			else {
				setLogicXover(c1Head,(TrackSegment)layoutTurnout.getConnectD(),c2Head,
						(TrackSegment)layoutTurnout.getConnectA(),setupC1Logic.isSelected(),
								setupC2Logic.isSelected());
			}
		}
		if (setupD1Logic.isSelected() || setupD2Logic.isSelected()) {
			if (xoverType == LayoutTurnout.RH_XOVER) {
				setLogicXoverContinuing(d1Head,(TrackSegment)layoutTurnout.getConnectC());			
			}
			else {
				setLogicXover(d1Head,(TrackSegment)layoutTurnout.getConnectC(),d2Head,
						(TrackSegment)layoutTurnout.getConnectB(),setupD1Logic.isSelected(),
								setupD2Logic.isSelected());
			}
		}
		// make sure this layout turnout is not linked to another
		layoutTurnout.setLinkType(LayoutTurnout.NO_LINK);
		layoutTurnout.setLinkedTurnoutName("");
		// finish up
		setSignalsAtXoverOpen = false;
		xoverFromMenu = false;
		setSignalsAtXoverFrame.setVisible(false);
		if (needRedraw) {
			layoutEditor.redrawPanel();
			needRedraw = false;
			layoutEditor.setDirty();
		}		
	}
	private boolean getXoverSignalHeadInformation() {
		a1Head = getSignalHeadFromEntry(a1Field,true,setSignalsAtXoverFrame);
		if (a1Head==null) return false;
		if (!(xoverType == LayoutTurnout.LH_XOVER)) 
			a2Head = getSignalHeadFromEntry(a2Field,false,setSignalsAtXoverFrame);
		else
			a2Head = null;
		b1Head = getSignalHeadFromEntry(b1Field,true,setSignalsAtXoverFrame);
		if (b1Head==null) return false;
		if (!(xoverType == LayoutTurnout.RH_XOVER)) 
			b2Head = getSignalHeadFromEntry(b2Field,false,setSignalsAtXoverFrame);
		else
			b2Head = null;
		c1Head = getSignalHeadFromEntry(c1Field,true,setSignalsAtXoverFrame);
		if (c1Head==null) return false;
		if (!(xoverType == LayoutTurnout.LH_XOVER)) 
			c2Head = getSignalHeadFromEntry(c2Field,false,setSignalsAtXoverFrame);
		else
			c2Head = null;
		d1Head = getSignalHeadFromEntry(d1Field,true,setSignalsAtXoverFrame);
		if (d1Head==null) return false;
		if (!(xoverType == LayoutTurnout.RH_XOVER)) 
			d2Head = getSignalHeadFromEntry(d2Field,false,setSignalsAtXoverFrame);
		else
			d2Head = null;
		return true;
	}
	private void placeA1() {
		if (testIcon == null)
			testIcon = signalIconEditor.getIcon(0);
		if( layoutTurnoutHorizontal && layoutTurnoutThroatLeft ) {
			setSignalHeadOnPanel(2,a1Field.getText().trim(),
				(int)(layoutTurnout.getCoordsA().getX()-testIcon.getIconWidth()),
				(int)(layoutTurnout.getCoordsA().getY()+4) );
		}
		else if( layoutTurnoutHorizontal && (!layoutTurnoutThroatLeft) ) {
			setSignalHeadOnPanel(0,a1Field.getText().trim(),
				(int)(layoutTurnout.getCoordsA().getX()),
				(int)(layoutTurnout.getCoordsA().getY()-4-testIcon.getIconHeight()) );
		}
		else if( layoutTurnoutVertical && layoutTurnoutThroatUp ) {
			setSignalHeadOnPanel(1,a1Field.getText().trim(),
				(int)(layoutTurnout.getCoordsA().getX()-4-testIcon.getIconWidth()),
				(int)(layoutTurnout.getCoordsA().getY()-testIcon.getIconHeight()) );
		}
		else if( layoutTurnoutVertical && (!layoutTurnoutThroatUp) ) {
			setSignalHeadOnPanel(3,a1Field.getText().trim(),
				(int)(layoutTurnout.getCoordsA().getX()+4),
				(int)(layoutTurnout.getCoordsA().getY()) );
		}
	}
	private void placeA2() {
		if (testIcon == null)
			testIcon = signalIconEditor.getIcon(0);
		if( layoutTurnoutHorizontal && layoutTurnoutThroatLeft ) {
			setSignalHeadOnPanel(2,a2Field.getText().trim(),
				(int)(layoutTurnout.getCoordsA().getX()-4-(2*testIcon.getIconWidth())),
				(int)(layoutTurnout.getCoordsA().getY()+4) );
		}
		else if( layoutTurnoutHorizontal && (!layoutTurnoutThroatLeft) ) {
			setSignalHeadOnPanel(0,a2Field.getText().trim(),
				(int)(layoutTurnout.getCoordsA().getX()+4+testIcon.getIconWidth()),
				(int)(layoutTurnout.getCoordsA().getY()-4-testIcon.getIconHeight()));
		}
		else if( layoutTurnoutVertical && layoutTurnoutThroatUp ) {
			setSignalHeadOnPanel(1,a2Field.getText().trim(),
				(int)(layoutTurnout.getCoordsA().getX()-4-testIcon.getIconWidth()),
				(int)(layoutTurnout.getCoordsA().getY()-4-(2*testIcon.getIconHeight())));
		}
		else if( layoutTurnoutVertical && (!layoutTurnoutThroatUp) ) {
			setSignalHeadOnPanel(3,a2Field.getText().trim(),
				(int)(layoutTurnout.getCoordsA().getX()+4),
				(int)(layoutTurnout.getCoordsA().getY()+4+testIcon.getIconHeight()));
		}
	}
	private void placeB1() {
		if (testIcon == null)
			testIcon = signalIconEditor.getIcon(0);
		if( layoutTurnoutHorizontal && layoutTurnoutThroatLeft ) {
			setSignalHeadOnPanel(0,b1Field.getText().trim(),
				(int)(layoutTurnout.getCoordsB().getX()),
				(int)(layoutTurnout.getCoordsB().getY()-4-testIcon.getIconHeight()) );
		}
		else if( layoutTurnoutHorizontal && (!layoutTurnoutThroatLeft) ) {
			setSignalHeadOnPanel(2,b1Field.getText().trim(),
				(int)(layoutTurnout.getCoordsB().getX()-testIcon.getIconWidth()),
				(int)(layoutTurnout.getCoordsB().getY()+4) );
		}
		else if( layoutTurnoutVertical && layoutTurnoutThroatUp ) {
			setSignalHeadOnPanel(3,b1Field.getText().trim(),
				(int)(layoutTurnout.getCoordsB().getX()+4),
				(int)(layoutTurnout.getCoordsB().getY()) );
		}
		else if( layoutTurnoutVertical && (!layoutTurnoutThroatUp) ) {
			setSignalHeadOnPanel(1,b1Field.getText().trim(),
				(int)(layoutTurnout.getCoordsB().getX()-4-testIcon.getIconWidth()),
				(int)(layoutTurnout.getCoordsB().getY()-testIcon.getIconHeight()) );
		}
	}
	private void placeB2() {
		if (testIcon == null)
			testIcon = signalIconEditor.getIcon(0);
		if( layoutTurnoutHorizontal && layoutTurnoutThroatLeft ) {
			setSignalHeadOnPanel(0,b2Field.getText().trim(),
				(int)(layoutTurnout.getCoordsB().getX()+4+testIcon.getIconWidth()),
				(int)(layoutTurnout.getCoordsB().getY()-4-testIcon.getIconHeight()) );
		}
		else if( layoutTurnoutHorizontal && (!layoutTurnoutThroatLeft) ) {
			setSignalHeadOnPanel(2,b2Field.getText().trim(),
				(int)(layoutTurnout.getCoordsB().getX()-4-(2*testIcon.getIconWidth())),
				(int)(layoutTurnout.getCoordsB().getY()+4) );
		}
		else if( layoutTurnoutVertical && layoutTurnoutThroatUp ) {
			setSignalHeadOnPanel(3,b2Field.getText().trim(),
				(int)(layoutTurnout.getCoordsB().getX()+4),
				(int)(layoutTurnout.getCoordsB().getY()+4+testIcon.getIconHeight()) );
		}
		else if( layoutTurnoutVertical && (!layoutTurnoutThroatUp) ) {
			setSignalHeadOnPanel(1,b2Field.getText().trim(),
				(int)(layoutTurnout.getCoordsB().getX()-4-testIcon.getIconWidth()),
				(int)(layoutTurnout.getCoordsB().getY()-4-(2*testIcon.getIconHeight())) );
		}
	}
	private void placeC1() {
		if (testIcon == null)
			testIcon = signalIconEditor.getIcon(0);
		if( layoutTurnoutHorizontal && layoutTurnoutThroatLeft ) {
			setSignalHeadOnPanel(0,c1Field.getText().trim(),
				(int)(layoutTurnout.getCoordsC().getX()),
				(int)(layoutTurnout.getCoordsC().getY()-4-testIcon.getIconHeight()) );
		}
		else if( layoutTurnoutHorizontal && (!layoutTurnoutThroatLeft) ) {
			setSignalHeadOnPanel(2,c1Field.getText().trim(),
				(int)(layoutTurnout.getCoordsC().getX()-testIcon.getIconWidth()),
				(int)(layoutTurnout.getCoordsC().getY()+4) );
		}
		else if( layoutTurnoutVertical && layoutTurnoutThroatUp ) {
			setSignalHeadOnPanel(3,c1Field.getText().trim(),
				(int)(layoutTurnout.getCoordsC().getX()+4),
				(int)(layoutTurnout.getCoordsC().getY()) );
		}
		else if( layoutTurnoutVertical && (!layoutTurnoutThroatUp) ) {
			setSignalHeadOnPanel(1,c1Field.getText().trim(),
				(int)(layoutTurnout.getCoordsC().getX()-4-testIcon.getIconWidth()),
				(int)(layoutTurnout.getCoordsC().getY()-testIcon.getIconHeight()) );
		}
	}
	private void placeC2() {
		if (testIcon == null)
			testIcon = signalIconEditor.getIcon(0);
		if( layoutTurnoutHorizontal && layoutTurnoutThroatLeft ) {
			setSignalHeadOnPanel(0,c2Field.getText().trim(),
				(int)(layoutTurnout.getCoordsC().getX()+4+testIcon.getIconWidth()),
				(int)(layoutTurnout.getCoordsC().getY()-4-testIcon.getIconHeight()) );
		}
		else if( layoutTurnoutHorizontal && (!layoutTurnoutThroatLeft) ) {
			setSignalHeadOnPanel(2,c2Field.getText().trim(),
				(int)(layoutTurnout.getCoordsC().getX()-4-(2*testIcon.getIconWidth())),
				(int)(layoutTurnout.getCoordsC().getY()+4) );
		}
		else if( layoutTurnoutVertical && layoutTurnoutThroatUp ) {
			setSignalHeadOnPanel(3,c2Field.getText().trim(),
				(int)(layoutTurnout.getCoordsC().getX()+4),
				(int)(layoutTurnout.getCoordsC().getY()+4+testIcon.getIconHeight()) );
		}
		else if( layoutTurnoutVertical && (!layoutTurnoutThroatUp) ) {
			setSignalHeadOnPanel(1,c2Field.getText().trim(),
				(int)(layoutTurnout.getCoordsC().getX()-4-testIcon.getIconWidth()),
				(int)(layoutTurnout.getCoordsC().getY()-4-(2*testIcon.getIconHeight())) );
		}
	}
	private void placeD1() {
		if (testIcon == null)
			testIcon = signalIconEditor.getIcon(0);
		if( layoutTurnoutHorizontal && layoutTurnoutThroatLeft ) {
			setSignalHeadOnPanel(2,d1Field.getText().trim(),
				(int)(layoutTurnout.getCoordsD().getX()-testIcon.getIconWidth()),
				(int)(layoutTurnout.getCoordsD().getY()+4) );
		}
		else if( layoutTurnoutHorizontal && (!layoutTurnoutThroatLeft) ) {
			setSignalHeadOnPanel(0,d1Field.getText().trim(),
				(int)(layoutTurnout.getCoordsD().getX()),
				(int)(layoutTurnout.getCoordsD().getY()-4-testIcon.getIconHeight()) );
		}
		else if( layoutTurnoutVertical && layoutTurnoutThroatUp ) {
			setSignalHeadOnPanel(1,d1Field.getText().trim(),
				(int)(layoutTurnout.getCoordsD().getX()-4-testIcon.getIconWidth()),
				(int)(layoutTurnout.getCoordsD().getY()-testIcon.getIconHeight()) );
		}
		else if( layoutTurnoutVertical && (!layoutTurnoutThroatUp) ) {
			setSignalHeadOnPanel(3,d1Field.getText().trim(),
				(int)(layoutTurnout.getCoordsD().getX()+4),
				(int)(layoutTurnout.getCoordsD().getY()) );
		}
	}
	private void placeD2() {
		if (testIcon == null)
			testIcon = signalIconEditor.getIcon(0);
		if( layoutTurnoutHorizontal && layoutTurnoutThroatLeft ) {
			setSignalHeadOnPanel(2,d2Field.getText().trim(),
				(int)(layoutTurnout.getCoordsD().getX()-4-(2*testIcon.getIconWidth())),
				(int)(layoutTurnout.getCoordsD().getY()+4) );
		}
		else if( layoutTurnoutHorizontal && (!layoutTurnoutThroatLeft) ) {
			setSignalHeadOnPanel(0,d2Field.getText().trim(),
				(int)(layoutTurnout.getCoordsD().getX()+4+testIcon.getIconWidth()),
				(int)(layoutTurnout.getCoordsD().getY()-4-testIcon.getIconHeight()));
		}
		else if( layoutTurnoutVertical && layoutTurnoutThroatUp ) {
			setSignalHeadOnPanel(1,d2Field.getText().trim(),
				(int)(layoutTurnout.getCoordsD().getX()-4-testIcon.getIconWidth()),
				(int)(layoutTurnout.getCoordsD().getY()-4-(2*testIcon.getIconHeight())));
		}
		else if( layoutTurnoutVertical && (!layoutTurnoutThroatUp) ) {
			setSignalHeadOnPanel(3,d2Field.getText().trim(),
				(int)(layoutTurnout.getCoordsD().getX()+4),
				(int)(layoutTurnout.getCoordsD().getY()+4+testIcon.getIconHeight()));
		}
	}
	@SuppressWarnings("null")
	private void setLogicXover(SignalHead head,TrackSegment track,SignalHead secondHead,TrackSegment track2,
				boolean setup1, boolean setup2) {
		if ( (track==null) && setup1 ) {
			JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
					rb.getString("InfoMessage7"),"",JOptionPane.INFORMATION_MESSAGE);			
			return;
		}
		Sensor occupancy = null;
		SignalHead nextHead = null;
		if ( (track!=null) && setup1) {
			LayoutBlock block = track.getLayoutBlock();
			if (block==null) {
				JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
					rb.getString("InfoMessage6"),"",JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			occupancy = block.getOccupancySensor();
			if (occupancy==null) {
				JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
					java.text.MessageFormat.format(rb.getString("InfoMessage4"),
						new Object[]{block.getUserName()}), 
							null,JOptionPane.INFORMATION_MESSAGE);						
				return;
			}
			nextHead = getNextSignalFromObject(track,
					layoutTurnout, head.getSystemName(), setSignalsAtXoverFrame);
			if ( (nextHead==null) && (!reachedEndBumper()) ) {
				JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
					java.text.MessageFormat.format(rb.getString("InfoMessage5"),
						new Object[]{block.getUserName()}), 
							null,JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			if (secondHead!=null) {
				if (!initializeBlockBossLogic(head.getSystemName())) return;
				logic.setMode(BlockBossLogic.TRAILINGMAIN);
				logic.setTurnout(turnout.getSystemName());
				logic.setSensor1(occupancy.getSystemName());
				if (nextHead!=null) {
					logic.setWatchedSignal1(nextHead.getSystemName(),false);
				}
				if (auxSignal!=null) {
					logic.setWatchedSignal1Alt(auxSignal.getSystemName());
				}
				finalizeBlockBossLogic();
			}
		}
		if ( (secondHead!=null) && !setup2 ) return;
		SignalHead savedAuxSignal = auxSignal;
		if (track2==null) {
			JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
					rb.getString("InfoMessage7"),"",JOptionPane.INFORMATION_MESSAGE);			
			return;
		}
		LayoutBlock block2 = track2.getLayoutBlock();
		if (block2==null) {
			JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
					rb.getString("InfoMessage6"),"",JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		Sensor occupancy2 = block2.getOccupancySensor();
		if (occupancy2==null) {
			JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
				java.text.MessageFormat.format(rb.getString("InfoMessage4"),
					new Object[]{block2.getUserName()}), 
						null,JOptionPane.INFORMATION_MESSAGE);						
			return;
		}
		String headName = head.getSystemName();
		if (secondHead!=null) headName = secondHead.getSystemName();
		SignalHead nextHead2 = getNextSignalFromObject(track2,
				layoutTurnout, headName, setSignalsAtXoverFrame);
		if ( (nextHead2==null) && (!reachedEndBumper()) ) {
			JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
				java.text.MessageFormat.format(rb.getString("InfoMessage5"),
					new Object[]{block2.getUserName()}), 
						null,JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		if ( (secondHead==null) && (track!=null) && setup1 ) {
			if (!initializeBlockBossLogic(head.getSystemName())) return;
			logic.setMode(BlockBossLogic.FACING);
			logic.setTurnout(turnout.getSystemName());
			logic.setWatchedSensor1(occupancy.getSystemName());
			logic.setWatchedSensor2(occupancy2.getSystemName());
			if (nextHead!=null) {
				logic.setWatchedSignal1(nextHead.getSystemName(),false);
			}
			if (savedAuxSignal!=null) {
				logic.setWatchedSignal1Alt(savedAuxSignal.getSystemName());
			}
			if (nextHead2!=null) {
				logic.setWatchedSignal2(nextHead2.getSystemName());
			}
			if (auxSignal!=null) {
				logic.setWatchedSignal2Alt(auxSignal.getSystemName());
			}		
			logic.setLimitSpeed2(true);
			finalizeBlockBossLogic();
		}
		else if ( (secondHead!=null) && setup2) {
			if (!initializeBlockBossLogic(secondHead.getSystemName())) return;
			logic.setMode(BlockBossLogic.TRAILINGDIVERGING);
			logic.setTurnout(turnout.getSystemName());
			logic.setSensor1(occupancy2.getSystemName());
			if (nextHead2!=null) {
				logic.setWatchedSignal1(nextHead2.getSystemName(),false);
			}
			if (auxSignal!=null) {
				logic.setWatchedSignal1Alt(auxSignal.getSystemName());
			}
			logic.setLimitSpeed2(true);
			finalizeBlockBossLogic();			
		}
	}
	private void setLogicXoverContinuing(SignalHead head,TrackSegment track) {
		if (track==null) {
			JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
					rb.getString("InfoMessage7"),"",JOptionPane.INFORMATION_MESSAGE);			
			return;
		}
		LayoutBlock block = track.getLayoutBlock();
		if (block==null) {
			JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
					rb.getString("InfoMessage6"),"",JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		Sensor occupancy = block.getOccupancySensor();
		if (occupancy==null) {
			JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
				java.text.MessageFormat.format(rb.getString("InfoMessage4"),
					new Object[]{block.getUserName()}), 
						null,JOptionPane.INFORMATION_MESSAGE);						
			return;
		}
		SignalHead nextHead = getNextSignalFromObject(track,
				layoutTurnout, head.getSystemName(), setSignalsAtXoverFrame);
		if ( (nextHead==null) && (!reachedEndBumper()) ) {
			JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
				java.text.MessageFormat.format(rb.getString("InfoMessage5"),
					new Object[]{block.getUserName()}), 
						null,JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		if (!initializeBlockBossLogic(head.getSystemName())) return;
		logic.setMode(BlockBossLogic.TRAILINGMAIN);
		logic.setTurnout(turnout.getSystemName());
		logic.setSensor1(occupancy.getSystemName());
		if (nextHead!=null) {
			logic.setWatchedSignal1(nextHead.getSystemName(),false);
		}
		if (auxSignal!=null) {
			logic.setWatchedSignal1Alt(auxSignal.getSystemName());
		}
		finalizeBlockBossLogic();	
	}

	/**
	 * Tool to set signals at a level crossing, including placing the 
	 *		signal icons and setup of Simple Signal Logic for each signal head
	 * <P>
	 * This tool assumes left facing signal head icons have been selected, and 
	 *		will rotate the signal head icons accordingly.
	 * <P>
	 * This tool will place icons on the right side of each track.
	 * <P>
	 * Both tracks do not need to be signalled. If one signal for a track,
	 *		A-C or B-D, the other must also be present.
	 * <P>
	 * Some user adjustment of turnout positions may be needed.
	 */	
	
	// operational variables for Set Signals at Level Crossing tool
	private JmriJFrame setSignalsAtXingFrame = null;
	private boolean setSignalsAtXingOpen = false;
	private JTextField blockANameField = new JTextField(16);
	private JTextField blockCNameField = new JTextField(16);
	private JTextField aField = new JTextField(16);
	private JTextField bField = new JTextField(16);
	private JTextField cField = new JTextField(16);
	private JTextField dField = new JTextField(16);
	private JCheckBox setAHead = new JCheckBox(rb.getString("PlaceHead"));
	private JCheckBox setupALogic = new JCheckBox(rb.getString("SetLogic"));
	private JCheckBox setBHead = new JCheckBox(rb.getString("PlaceHead"));
	private JCheckBox setupBLogic = new JCheckBox(rb.getString("SetLogic"));
	private JCheckBox setCHead = new JCheckBox(rb.getString("PlaceHead"));
	private JCheckBox setupCLogic = new JCheckBox(rb.getString("SetLogic"));
	private JCheckBox setDHead = new JCheckBox(rb.getString("PlaceHead"));
	private JCheckBox setupDLogic = new JCheckBox(rb.getString("SetLogic"));
	private JButton getSavedXingSignalHeads = null;
	private JButton changeXingSignalIcon = null;
	private JButton setXingSignalsDone = null;
	private JButton setXingSignalsCancel = null;
	//private TrackSegment xingTrackA = null;
	//private TrackSegment xingTrackB = null;
	//private TrackSegment xingTrackC = null;
	//private TrackSegment xingTrackD = null;
	private boolean levelXingACHorizontal = false;	
	private boolean levelXingACVertical = false;	
	private boolean levelXingALeft = false;
	private boolean levelXingAUp = false;
	private boolean levelXingBUp = false;
	private boolean levelXingBLeft = false;
	private boolean xingFromMenu = false;
	private LevelXing levelXing = null;
	private SignalHead aHead = null;
	private SignalHead bHead = null;
	private SignalHead cHead = null;
	private SignalHead dHead = null;

	// display dialog for Set Signals at Level Crossing tool
	public void setSignalsAtLevelXingFromMenu (LevelXing xing, MultiIconEditor theEditor, 
			JFrame theFrame ) {
		xingFromMenu = true;
		levelXing = xing;
		blockANameField.setText(levelXing.getBlockNameAC());
		blockCNameField.setText(levelXing.getBlockNameBD());
		setSignalsAtLevelXing(theEditor,theFrame);
		return;
	}		
	public void setSignalsAtLevelXing( MultiIconEditor theEditor, JFrame theFrame ) {
		signalIconEditor = theEditor;
		signalFrame = theFrame;
		if (setSignalsAtXingOpen) {
			setSignalsAtXingFrame.setVisible(true);
			return;
		}
		aField.setText("");
		bField.setText("");
		cField.setText("");
		dField.setText("");
		// Initialize if needed
		if (setSignalsAtXingFrame == null) {
            setSignalsAtXingFrame = new JmriJFrame( rb.getString("SignalsAtLevelXing"), false, true );
            setSignalsAtXingFrame.addHelpMenu("package.jmri.jmrit.display.SetSignalsAtLevelXing", true);
            setSignalsAtXingFrame.setLocation(70,30);
            Container theContentPane = setSignalsAtXingFrame.getContentPane();        
            theContentPane.setLayout(new BoxLayout(theContentPane, BoxLayout.Y_AXIS));
			JPanel panel11 = new JPanel(); 
            panel11.setLayout(new FlowLayout());
			if (xingFromMenu) {
				JLabel blockANameLabel = new JLabel( rb.getString("BlockAtA")+" "+
							rb.getString("Name")+" : "+levelXing.getBlockNameAC());
			
				panel11.add(blockANameLabel);
			}
			else {
				JLabel blockANameLabel = new JLabel( rb.getString("BlockAtA")+" "+
																rb.getString("Name")+" : ");
				panel11.add(blockANameLabel);
				panel11.add(blockANameField);
				blockANameField.setToolTipText(rb.getString("SignalsBlockNameHint"));
			}
            theContentPane.add(panel11);
			JPanel panel12 = new JPanel(); 
            panel12.setLayout(new FlowLayout());
			if (xingFromMenu) {
				JLabel blockCNameLabel = new JLabel( rb.getString("BlockAtC")+" "+
							rb.getString("Name")+" : "+levelXing.getBlockNameBD());
			
				panel12.add(blockCNameLabel);
			}
			else {
				JLabel blockCNameLabel = new JLabel( rb.getString("BlockAtC")+" "+
																rb.getString("Name")+" : ");
				panel12.add(blockCNameLabel);
				panel12.add(blockCNameField);
				blockCNameField.setToolTipText(rb.getString("SignalsBlockNameHint"));
			}
            theContentPane.add(panel12);
			theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));
            JPanel panel2 = new JPanel();
            panel2.setLayout(new FlowLayout());
			JLabel shTitle = new JLabel(rb.getString("SignalHeads"));
			panel2.add(shTitle);
			panel2.add(new JLabel("   "));
            panel2.add(getSavedXingSignalHeads = new JButton(rb.getString("GetSaved")));
            getSavedXingSignalHeads.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						xingSignalsGetSaved(e);
					}
				});
            getSavedXingSignalHeads.setToolTipText( rb.getString("GetSavedHint") );			
			theContentPane.add(panel2);
            JPanel panel21 = new JPanel();
            panel21.setLayout(new FlowLayout());
			JLabel aLabel = new JLabel(rb.getString("ATrack")+" : ");
			panel21.add(aLabel);
			panel21.add(aField);
			theContentPane.add(panel21);
			aField.setToolTipText(rb.getString("SignalHeadNameHint"));
            JPanel panel22 = new JPanel();
            panel22.setLayout(new FlowLayout());
			panel22.add(new JLabel("   "));
			panel22.add(setAHead);
			setAHead.setToolTipText(rb.getString("PlaceHeadHint"));
			panel22.add(new JLabel("  "));
			panel22.add(setupALogic);
			setupALogic.setToolTipText(rb.getString("SetLogicHint"));
			theContentPane.add(panel22);
            JPanel panel31 = new JPanel();
            panel31.setLayout(new FlowLayout());
			JLabel bLabel = new JLabel(rb.getString("BTrack")+" : ");
			panel31.add(bLabel);
			panel31.add(bField);
			theContentPane.add(panel31);
			bField.setToolTipText(rb.getString("SignalHeadNameHint"));
            JPanel panel32 = new JPanel();
            panel32.setLayout(new FlowLayout());
			panel32.add(new JLabel("   "));
			panel32.add(setBHead);
			setBHead.setToolTipText(rb.getString("PlaceHeadHint"));
			panel32.add(new JLabel("  "));
			panel32.add(setupBLogic);
			setupBLogic.setToolTipText(rb.getString("SetLogicHint"));
			theContentPane.add(panel32);
            JPanel panel41 = new JPanel();
            panel41.setLayout(new FlowLayout());
			JLabel cLabel = new JLabel(rb.getString("CTrack")+" : ");
			panel41.add(cLabel);
			panel41.add(cField);
			theContentPane.add(panel41);
			cField.setToolTipText(rb.getString("SignalHeadNameHint"));
            JPanel panel42 = new JPanel();
            panel42.setLayout(new FlowLayout());
			panel42.add(new JLabel("   "));
			panel42.add(setCHead);
			setCHead.setToolTipText(rb.getString("PlaceHeadHint"));
			panel42.add(new JLabel("  "));
			panel42.add(setupCLogic);
			setupCLogic.setToolTipText(rb.getString("SetLogicHint"));
			theContentPane.add(panel42);
            JPanel panel51 = new JPanel();
            panel51.setLayout(new FlowLayout());
			JLabel dLabel = new JLabel(rb.getString("DTrack")+" : ");
			panel51.add(dLabel);
			panel51.add(dField);
			theContentPane.add(panel51);
			dField.setToolTipText(rb.getString("SignalHeadNameHint"));
            JPanel panel52 = new JPanel();
            panel52.setLayout(new FlowLayout());
			panel52.add(new JLabel("   "));
			panel52.add(setDHead);
			setDHead.setToolTipText(rb.getString("PlaceHeadHint"));
			panel52.add(new JLabel("  "));
			panel52.add(setupDLogic);
			setupDLogic.setToolTipText(rb.getString("SetLogicHint"));
			theContentPane.add(panel52);
			theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));
            JPanel panel6 = new JPanel();
            panel6.setLayout(new FlowLayout());
            panel6.add(changeXingSignalIcon = new JButton(rb.getString("ChangeSignalIcon")));
            changeXingSignalIcon.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						signalFrame.setVisible(true);
					}
				});
            changeXingSignalIcon.setToolTipText( rb.getString("ChangeSignalIconHint") );
			panel6.add(new JLabel("  "));
            panel6.add(setXingSignalsDone = new JButton(rb.getString("Done")));
            setXingSignalsDone.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setXingSignalsDonePressed(e);
                }
            });
            setXingSignalsDone.setToolTipText( rb.getString("SignalDoneHint") );
            panel6.add(setXingSignalsCancel = new JButton(rb.getString("Cancel")));
            setXingSignalsCancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setXingSignalsCancelPressed(e);
                }
            });
            setXingSignalsCancel.setToolTipText( rb.getString("CancelHint") );
            theContentPane.add(panel6);
			setSignalsAtXingFrame.addWindowListener(new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent e) {
					setXingSignalsCancelPressed(null);
				}
			});
			if (xingFromMenu) xingSignalsGetSaved(null);
		}
        setSignalsAtXingFrame.pack();
        setSignalsAtXingFrame.setVisible(true);		
		setSignalsAtXingOpen = true;
	}	
	private void xingSignalsGetSaved (ActionEvent a) {
		if ( !getLevelCrossingInformation() ) return;
		aField.setText(levelXing.getSignalAName());	
		bField.setText(levelXing.getSignalBName());
		cField.setText(levelXing.getSignalCName());	
		dField.setText(levelXing.getSignalDName());	
	}
	private void setXingSignalsCancelPressed (ActionEvent a) {
		setSignalsAtXingOpen = false;
		setSignalsAtXingFrame.setVisible(false);
		xingFromMenu = false;
	}
	private void setXingSignalsDonePressed (ActionEvent a) {
		if ( !getLevelCrossingInformation() ) return;
		if ( !getXingSignalHeadInformation() ) return;
		// place or update signals as requested
		if ( (aHead!=null) && setAHead.isSelected() ) {
			if (isHeadOnPanel(aHead) && 
					(aHead!=getHeadFromName(levelXing.getSignalAName())) ) { 
				JOptionPane.showMessageDialog(setSignalsAtXingFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError6"),
						new Object[]{aField.getText().trim()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else if ( (!levelXingACHorizontal) && (!levelXingACVertical) ) {
				JOptionPane.showMessageDialog(setSignalsAtXingFrame,
					rb.getString("InfoMessage3"),"",JOptionPane.INFORMATION_MESSAGE);
				if ( aHead!=getHeadFromName(levelXing.getSignalAName()) ) {				
					removeSignalHeadFromPanel(levelXing.getSignalAName());
					removeAssignment(aHead);
					levelXing.setSignalAName(aField.getText().trim());
				}
			}				
			else {
				removeSignalHeadFromPanel(levelXing.getSignalAName());
				placeXingA();
				removeAssignment(aHead);
				levelXing.setSignalAName(aField.getText().trim());
				needRedraw = true;
			}		
		}
		else if ( (aHead!=null) && 
				(aHead!=getHeadFromName(levelXing.getSignalAName())) &&
				(aHead!=getHeadFromName(levelXing.getSignalBName())) &&
				(aHead!=getHeadFromName(levelXing.getSignalCName())) &&
				(aHead!=getHeadFromName(levelXing.getSignalDName())) ) {
			if (isHeadOnPanel(aHead)) {
				JOptionPane.showMessageDialog(setSignalsAtXingFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError13"),
						new Object[]{aField.getText().trim()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}		
			else {
				removeSignalHeadFromPanel(levelXing.getSignalAName());
				removeAssignment(aHead);
				levelXing.setSignalAName(aField.getText().trim());
			}
		}
		else if ( (aHead!=null) &&  
				( (aHead==getHeadFromName(levelXing.getSignalBName())) ||
					(aHead==getHeadFromName(levelXing.getSignalCName())) ||
					(aHead==getHeadFromName(levelXing.getSignalDName())) ) ) {
// need to figure out what to do in this case.			
		}
		else if (aHead==null) {
			removeSignalHeadFromPanel(levelXing.getSignalAName());
			levelXing.setSignalAName("");		
		}
		if ( (bHead!=null) && setBHead.isSelected() ) {
			if (isHeadOnPanel(bHead) && 
					(bHead!=getHeadFromName(levelXing.getSignalBName()))) { 
				JOptionPane.showMessageDialog(setSignalsAtXingFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError6"),
						new Object[]{bField.getText().trim()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else if ( (!levelXingACHorizontal) && (!levelXingACVertical) ) {
				JOptionPane.showMessageDialog(setSignalsAtXingFrame,
					rb.getString("InfoMessage3"),"",JOptionPane.INFORMATION_MESSAGE);
				if (bHead!=getHeadFromName(levelXing.getSignalBName())) {				
					removeSignalHeadFromPanel(levelXing.getSignalBName());
					removeAssignment(bHead);
					levelXing.setSignalBName(bField.getText().trim());
				}
			}				
			else {
				removeSignalHeadFromPanel(levelXing.getSignalBName());
				placeXingB();
				removeAssignment(bHead);
				levelXing.setSignalBName(bField.getText().trim());
				needRedraw = true;
			}		
		}
		else if ( (bHead!=null) && 
				(bHead!=getHeadFromName(levelXing.getSignalAName())) &&
				(bHead!=getHeadFromName(levelXing.getSignalBName())) &&
				(bHead!=getHeadFromName(levelXing.getSignalCName())) &&
				(bHead!=getHeadFromName(levelXing.getSignalDName())) ) {
			if (isHeadOnPanel(bHead)) {
				JOptionPane.showMessageDialog(setSignalsAtXingFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError13"),
						new Object[]{bField.getText().trim()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}		
			else {
				removeSignalHeadFromPanel(levelXing.getSignalBName());
				removeAssignment(bHead);
				levelXing.setSignalBName(bField.getText().trim());
			}
		}
		else if ( (bHead!=null) &&  
				( (bHead==getHeadFromName(levelXing.getSignalAName())) ||
					(bHead==getHeadFromName(levelXing.getSignalCName())) ||
					(bHead==getHeadFromName(levelXing.getSignalDName())) ) ) {
// need to figure out what to do in this case.			
		}
		else if (bHead==null) {
			removeSignalHeadFromPanel(levelXing.getSignalBName());
			levelXing.setSignalBName("");		
		}
		if ( (cHead!=null) && setCHead.isSelected() ) {
			if (isHeadOnPanel(cHead) && 
					(cHead!=getHeadFromName(levelXing.getSignalCName())) ) { 
				JOptionPane.showMessageDialog(setSignalsAtXingFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError6"),
						new Object[]{cField.getText().trim()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else if ( (!levelXingACHorizontal) && (!levelXingACVertical) ) {
				JOptionPane.showMessageDialog(setSignalsAtXingFrame,
					rb.getString("InfoMessage3"),"",JOptionPane.INFORMATION_MESSAGE);
				if ( cHead!=getHeadFromName(levelXing.getSignalCName()) ) {				
					removeSignalHeadFromPanel(levelXing.getSignalCName());
					removeAssignment(cHead);
					levelXing.setSignalCName(cField.getText().trim());
				}
			}				
			else {
				removeSignalHeadFromPanel(levelXing.getSignalCName());
				placeXingC();
				removeAssignment(cHead);
				levelXing.setSignalCName(cField.getText().trim());
				needRedraw = true;
			}		
		}
		else if ( (cHead!=null) && 
				(cHead!=getHeadFromName(levelXing.getSignalAName())) &&
				(cHead!=getHeadFromName(levelXing.getSignalBName())) &&
				(cHead!=getHeadFromName(levelXing.getSignalCName())) &&
				(cHead!=getHeadFromName(levelXing.getSignalDName())) ) {
			if (isHeadOnPanel(cHead)) {
				JOptionPane.showMessageDialog(setSignalsAtXingFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError13"),
						new Object[]{cField.getText().trim()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}		
			else {
				removeSignalHeadFromPanel(levelXing.getSignalCName());
				removeAssignment(cHead);
				levelXing.setSignalCName(cField.getText().trim());
			}
		}
		else if ( (cHead!=null) &&  
				( (cHead==getHeadFromName(levelXing.getSignalBName())) ||
					(cHead==getHeadFromName(levelXing.getSignalAName())) ||
					(cHead==getHeadFromName(levelXing.getSignalDName())) ) ) {
// need to figure out what to do in this case.			
		}
		else if (cHead==null) {
			removeSignalHeadFromPanel(levelXing.getSignalCName());
			levelXing.setSignalCName("");		
		}
		if ( (dHead!=null) && setDHead.isSelected() ) {
			if (isHeadOnPanel(dHead) && 
					(dHead!=getHeadFromName(levelXing.getSignalDName())) ) { 
				JOptionPane.showMessageDialog(setSignalsAtXingFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError6"),
						new Object[]{dField.getText().trim()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else if ( (!levelXingACHorizontal) && (!levelXingACVertical) ) {
				JOptionPane.showMessageDialog(setSignalsAtXingFrame,
					rb.getString("InfoMessage3"),"",JOptionPane.INFORMATION_MESSAGE);
				if ( dHead!=getHeadFromName(levelXing.getSignalDName()) ) {				
					removeSignalHeadFromPanel(levelXing.getSignalDName());
					removeAssignment(dHead);
					levelXing.setSignalDName(dField.getText().trim());
				}
			}				
			else {
				removeSignalHeadFromPanel(levelXing.getSignalDName());
				placeXingD();
				removeAssignment(dHead);
				levelXing.setSignalDName(dField.getText().trim());
				needRedraw = true;
			}		
		}
		else if ( (dHead!=null) && 
				(dHead!=getHeadFromName(levelXing.getSignalAName())) &&
				(dHead!=getHeadFromName(levelXing.getSignalBName())) &&
				(dHead!=getHeadFromName(levelXing.getSignalCName())) &&
				(dHead!=getHeadFromName(levelXing.getSignalDName())) ) {
			if (isHeadOnPanel(dHead)) {
				JOptionPane.showMessageDialog(setSignalsAtXingFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError13"),
						new Object[]{dField.getText().trim()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}		
			else {
				removeSignalHeadFromPanel(levelXing.getSignalDName());
				removeAssignment(dHead);
				levelXing.setSignalDName(dField.getText().trim());
			}
		}
		else if ( (dHead!=null) &&  
				( (dHead==getHeadFromName(levelXing.getSignalBName())) ||
					(dHead==getHeadFromName(levelXing.getSignalCName())) ||
					(dHead==getHeadFromName(levelXing.getSignalAName())) ) ) {
// need to figure out what to do in this case.			
		}
		else if (dHead==null) {
			removeSignalHeadFromPanel(levelXing.getSignalDName());
			levelXing.setSignalDName("");		
		}
		// setup logic if requested
		if ( setupALogic.isSelected() && (aHead!=null) ) {
			setLogicXing( aHead, (TrackSegment)levelXing.getConnectC(),
				levelXing.getLayoutBlockBD(), (TrackSegment)levelXing.getConnectB(),
					(TrackSegment)levelXing.getConnectD(), aField.getText());
		}
		if ( setupBLogic.isSelected() && (bHead!=null) ) {
			setLogicXing( bHead, (TrackSegment)levelXing.getConnectD(),
				levelXing.getLayoutBlockAC(), (TrackSegment)levelXing.getConnectA(),
					(TrackSegment)levelXing.getConnectC(), bField.getText());
		}
		if ( setupCLogic.isSelected() && (cHead!=null) ) {
			setLogicXing( cHead, (TrackSegment)levelXing.getConnectA(),
				levelXing.getLayoutBlockBD(), (TrackSegment)levelXing.getConnectB(),
					(TrackSegment)levelXing.getConnectD(), cField.getText());
		}
		if ( setupDLogic.isSelected() && (dHead!=null) ) {
			setLogicXing( dHead, (TrackSegment)levelXing.getConnectB(),
				levelXing.getLayoutBlockAC(), (TrackSegment)levelXing.getConnectA(),
					(TrackSegment)levelXing.getConnectC(), dField.getText());
		}
		// finish up
		setSignalsAtXingOpen = false;
		setSignalsAtXingFrame.setVisible(false);
		xingFromMenu = false;	
		if (needRedraw) {
			layoutEditor.redrawPanel();
			needRedraw = false;
			layoutEditor.setDirty();
		}		
	}
	private boolean getLevelCrossingInformation() {
		if (!xingFromMenu) {
			levelXing = null;
			if (layoutEditor.xingList.size()<=0) {
				JOptionPane.showMessageDialog(setSignalsAtXingFrame,
					rb.getString("SignalsError15"),
								rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return false;
			}
			else if (layoutEditor.xingList.size()==1) {
				levelXing = layoutEditor.xingList.get(0);
			}
			else {
				LayoutBlock xingBlockA = null;
				LayoutBlock xingBlockC = null;
				xingBlockA = getBlockFromEntry(blockANameField);
				if (xingBlockA==null) return false;
				if (blockCNameField.getText().trim().length()>0) {
					xingBlockC = getBlockFromEntry(blockCNameField);
					if (xingBlockC==null) return false;
				}
				LevelXing x = null;
				int foundCount = 0;
				// make two block tests first
				if (xingBlockC!=null) {
					for (int i = 0;(i<layoutEditor.xingList.size());i++) {
						x = layoutEditor.xingList.get(i);
						LayoutBlock xA = null;
						LayoutBlock xB = null;
						LayoutBlock xC = null;
						LayoutBlock xD = null;
						LayoutBlock xAC = x.getLayoutBlockAC();
						LayoutBlock xBD = x.getLayoutBlockBD();
						if (x.getConnectA()!=null) xA = ((TrackSegment)x.getConnectA()).getLayoutBlock();
						if (x.getConnectB()!=null) xB = ((TrackSegment)x.getConnectB()).getLayoutBlock();
						if (x.getConnectC()!=null) xC = ((TrackSegment)x.getConnectC()).getLayoutBlock();
						if (x.getConnectD()!=null) xD = ((TrackSegment)x.getConnectD()).getLayoutBlock();
						if ( ( (xA!=null) && (xC!=null) && ( ((xA==xingBlockA)&&(xC==xingBlockC)) ||
								((xA==xingBlockC)&&(xC==xingBlockA)) ) ) ||
								( (xB!=null) && (xD!=null) && ( ((xB==xingBlockA)&&(xD==xingBlockC)) ||
								((xB==xingBlockC)&&(xD==xingBlockA)) ) ) ) {
							levelXing = x;
							foundCount ++;
						}
						else if ( (xAC!=null) && (xBD!=null) && ( ((xAC==xingBlockA) && (xBD==xingBlockC)) ||
									((xAC==xingBlockC) && (xBD==xingBlockA)) ) ) {
							levelXing = x;
							foundCount ++;
						}				
					}
				}
				if (foundCount==0) {
					// try one block test
					for (int i = 0;(i<layoutEditor.xingList.size());i++) {
						x = layoutEditor.xingList.get(i);
						if ((xingBlockA == x.getLayoutBlockAC()) || (xingBlockA == x.getLayoutBlockBD())) {
							levelXing = x;
							foundCount ++;
						}				
					}
				}
				if (foundCount>1) {
					JOptionPane.showMessageDialog(setSignalsAtXingFrame,
							java.text.MessageFormat.format(rb.getString("SignalsError16"),
								new Object[]{" "+foundCount+" "}), 
									rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
					return false;
				}
				if (levelXing==null) {
					JOptionPane.showMessageDialog(setSignalsAtXingFrame,
								rb.getString("SignalsError17"),
										rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
					return false;
				}
			}
		}
		//if (levelXing.getConnectA()!=null) xingTrackA = ((TrackSegment)levelXing.getConnectA());
		//if (levelXing.getConnectB()!=null) xingTrackB = ((TrackSegment)levelXing.getConnectB());
		//if (levelXing.getConnectC()!=null) xingTrackC = ((TrackSegment)levelXing.getConnectC());
		//if (levelXing.getConnectD()!=null) xingTrackD = ((TrackSegment)levelXing.getConnectD());
		double delX = levelXing.getCoordsA().getX() - levelXing.getCoordsC().getX();
		double delY = levelXing.getCoordsA().getY() - levelXing.getCoordsC().getY();
		levelXingACHorizontal = false;
		levelXingACVertical = false;
		levelXingALeft = false;
		levelXingAUp = false;
		levelXingBUp = false;
		levelXingBLeft = false;
		if (Math.abs(delX) > 2.0*Math.abs(delY)) {
			levelXingACHorizontal = true;
			if (delX < 0.0) levelXingALeft = true;
			if (levelXing.getCoordsB().getY() < levelXing.getCoordsD().getY())
				levelXingBUp = true;
		}
		else if (Math.abs(delY) > 2.0*Math.abs(delX)) { 
			levelXingACVertical = true;
			if (delY <0.0) levelXingAUp = true;
			if (levelXing.getCoordsB().getX() < levelXing.getCoordsD().getX())
				levelXingBLeft = true;
		}
		return true;
	}
	private boolean getXingSignalHeadInformation() {
		// note that all heads are optional, but pairs must be present
		aHead = getSignalHeadFromEntry(aField,false,setSignalsAtXingFrame);
		bHead = getSignalHeadFromEntry(bField,false,setSignalsAtXingFrame);
		cHead = getSignalHeadFromEntry(cField,false,setSignalsAtXingFrame);
		dHead = getSignalHeadFromEntry(dField,false,setSignalsAtXingFrame);
		if ( ((aHead!=null) && (cHead==null)) || ((aHead==null) && (cHead!=null)) ||
				((bHead!=null) && (dHead==null)) || ((bHead==null) && (dHead!=null)) ) {
			JOptionPane.showMessageDialog(setSignalsAtXingFrame,
					rb.getString("SignalsError14"),
						rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
			return false;
		}
		if ( (aHead==null) && (bHead==null) && (cHead==null) && (dHead==null) ) {
			JOptionPane.showMessageDialog(setSignalsAtXingFrame,
					rb.getString("SignalsError12"),
						rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
	private void placeXingA() {
		if (testIcon == null)
			testIcon = signalIconEditor.getIcon(0);
		Point2D p = levelXing.getCoordsA();
		if (levelXingACHorizontal && levelXingALeft) {
			setSignalHeadOnPanel(2,aField.getText().trim(),
				(int)(p.getX()-testIcon.getIconWidth()),
				(int)(p.getY()+4) );
		}
		else if (levelXingACHorizontal && (!levelXingALeft) ) {
			setSignalHeadOnPanel(0,aField.getText().trim(),
				(int)(p.getX()),
				(int)(p.getY()-4-testIcon.getIconHeight()) );
		}
		else if (levelXingACVertical && levelXingAUp) {
			setSignalHeadOnPanel(1,aField.getText().trim(),
				(int)(p.getX()-2-testIcon.getIconWidth()),
				(int)(p.getY()-testIcon.getIconHeight()) );
		}
		else if (levelXingACVertical && (!levelXingAUp) ) {
			setSignalHeadOnPanel(3,aField.getText().trim(),
				(int)(p.getX()+4),
				(int)(p.getY()+2) );
		}
	}
	private void placeXingB() {
		if (testIcon == null)
			testIcon = signalIconEditor.getIcon(0);
		Point2D p = levelXing.getCoordsB();
		if (levelXingACVertical && levelXingBLeft) {
			setSignalHeadOnPanel(2,bField.getText().trim(),
				(int)(p.getX()-testIcon.getIconWidth()),
				(int)(p.getY()+4) );
		}
		else if (levelXingACVertical && (!levelXingBLeft) ) {
			setSignalHeadOnPanel(0,bField.getText().trim(),
				(int)(p.getX()),
				(int)(p.getY()-4-testIcon.getIconHeight()) );
		}
		else if (levelXingACHorizontal && levelXingBUp) {
			setSignalHeadOnPanel(1,bField.getText().trim(),
				(int)(p.getX()-2-testIcon.getIconWidth()),
				(int)(p.getY()-testIcon.getIconHeight()) );
		}
		else if (levelXingACHorizontal && (!levelXingBUp) ) {
			setSignalHeadOnPanel(3,bField.getText().trim(),
				(int)(p.getX()+4),
				(int)(p.getY()+2) );
		}
	}
	private void placeXingC() {
		if (testIcon == null)
			testIcon = signalIconEditor.getIcon(0);
		Point2D p = levelXing.getCoordsC();
		if (levelXingACHorizontal && (!levelXingALeft) ) {
			setSignalHeadOnPanel(2,cField.getText().trim(),
				(int)(p.getX()-testIcon.getIconWidth()),
				(int)(p.getY()+4) );
		}
		else if (levelXingACHorizontal && levelXingALeft) {
			setSignalHeadOnPanel(0,cField.getText().trim(),
				(int)(p.getX()),
				(int)(p.getY()-4-testIcon.getIconHeight()) );
		}
		else if (levelXingACVertical && (!levelXingAUp) ) {
			setSignalHeadOnPanel(1,cField.getText().trim(),
				(int)(p.getX()-2-testIcon.getIconWidth()),
				(int)(p.getY()-testIcon.getIconHeight()) );
		}
		else if (levelXingACVertical && levelXingAUp) {
			setSignalHeadOnPanel(3,cField.getText().trim(),
				(int)(p.getX()+4),
				(int)(p.getY()+2) );
		}
	}
	private void placeXingD() {
		if (testIcon == null)
			testIcon = signalIconEditor.getIcon(0);
		Point2D p = levelXing.getCoordsD();
		if (levelXingACVertical && (!levelXingBLeft) ) {
			setSignalHeadOnPanel(2,dField.getText().trim(),
				(int)(p.getX()-testIcon.getIconWidth()),
				(int)(p.getY()+4) );
		}
		else if (levelXingACVertical && levelXingBLeft) {
			setSignalHeadOnPanel(0,dField.getText().trim(),
				(int)(p.getX()),
				(int)(p.getY()-4-testIcon.getIconHeight()) );
		}
		else if (levelXingACHorizontal && (!levelXingBUp) ) {
			setSignalHeadOnPanel(1,dField.getText().trim(),
				(int)(p.getX()-2-testIcon.getIconWidth()),
				(int)(p.getY()-testIcon.getIconHeight()) );
		}
		else if (levelXingACHorizontal && levelXingBUp) {
			setSignalHeadOnPanel(3,dField.getText().trim(),
				(int)(p.getX()+4),
				(int)(p.getY()+2) );
		}
	}
	@SuppressWarnings("null")
	private void setLogicXing(SignalHead head, TrackSegment track, LayoutBlock crossBlock,
				TrackSegment crossTrack1, TrackSegment crossTrack2, String headName) {
		if (track==null) {
			JOptionPane.showMessageDialog(setSignalsAtXingFrame,
					rb.getString("InfoMessage7"),"",JOptionPane.INFORMATION_MESSAGE);			
			return;
		}
		Sensor occupancy = null;
		Sensor crossOccupancy = null;
		Sensor track1Occupancy = null;
		Sensor track2Occupancy = null;
		SignalHead nextHead = null;
		LayoutBlock block = track.getLayoutBlock();
		if (block==null) {
			JOptionPane.showMessageDialog(setSignalsAtXingFrame,
					rb.getString("InfoMessage6"),"",JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		occupancy = block.getOccupancySensor();
		if (occupancy==null) {
			JOptionPane.showMessageDialog(setSignalsAtXingFrame,
					java.text.MessageFormat.format(rb.getString("InfoMessage4"),
						new Object[]{block.getUserName()}), 
							null,JOptionPane.INFORMATION_MESSAGE);						
			return;
		}
		if (crossBlock!=null) {
			crossOccupancy = crossBlock.getOccupancySensor();
		}
		if (crossTrack1!=null) {
			block = crossTrack1.getLayoutBlock();
			if (block!=null) {
				track1Occupancy = block.getOccupancySensor();
				if (track1Occupancy==crossOccupancy) track1Occupancy = null;
			}
		}
		if (crossTrack2!=null) {
			block = crossTrack2.getLayoutBlock();
			if (block!=null) {
				track2Occupancy = block.getOccupancySensor();
				if ( (track2Occupancy==crossOccupancy) ||
				     (track2Occupancy==track1Occupancy) ) track2Occupancy = null;
			}
		}			
		nextHead = getNextSignalFromObject(track,levelXing, 
								head.getSystemName(), setSignalsAtXoverFrame);
		if ( (nextHead==null) && (!reachedEndBumper()) ) {
			JOptionPane.showMessageDialog(setSignalsAtXingFrame,
					java.text.MessageFormat.format(rb.getString("InfoMessage5"),
						new Object[]{block.getUserName()}), 
							null,JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		if ( (crossOccupancy==null) && (track1Occupancy==null) && (track2Occupancy==null) ) {
			JOptionPane.showMessageDialog(setSignalsAtXingFrame,
					java.text.MessageFormat.format(rb.getString("SignalsWarn1"),
						new Object[]{headName}), 
							null,JOptionPane.WARNING_MESSAGE);
		}
		if (!initializeBlockBossLogic(head.getSystemName())) return;
		logic.setMode(BlockBossLogic.SINGLEBLOCK);
		logic.setSensor1(occupancy.getSystemName());
		if (nextHead!=null) {
			logic.setWatchedSignal1(nextHead.getSystemName(),false);
		}
		if (auxSignal!=null) {
			logic.setWatchedSignal1Alt(auxSignal.getSystemName());
		}
		if (crossOccupancy!=null) {
			logic.setSensor2(crossOccupancy.getSystemName());
			if (track1Occupancy!=null) {
				logic.setSensor3(track1Occupancy.getSystemName());
				if (track2Occupancy!=null) 
					logic.setSensor4(track2Occupancy.getSystemName());
			}
			else if (track2Occupancy!=null)
				logic.setSensor3(track2Occupancy.getSystemName());
		}
		else if (track1Occupancy!=null) {
			logic.setSensor2(track1Occupancy.getSystemName());
			if (track2Occupancy!=null)
				logic.setSensor3(track2Occupancy.getSystemName());
		}
		else if (track2Occupancy!=null)
			logic.setSensor2(track2Occupancy.getSystemName());
		finalizeBlockBossLogic();
	}

	/**
	 * Tool to set signals at throat-to-throat turnouts, including placing the 
	 *		signal icons and setup of signal logic for each signal head
	 * <P>
	 * This tool can only be accessed from the Tools menu. There is no access
	 *		from a turnout pop-up menu.
	 * <P>
	 * This tool requires a situation where two turnouts are connected throat-
	 *		to-throat by a single "short" track segment.  The actual length of 
	 *		the track segment is not tested. If this situation is not found,
	 *		and error message is sent to the user.  To get started with this 
	 *      the user needs to enter at least one of the two connected turnouts.
	 * <P>
	 * This tool assumes two turnouts connected throat-to-throat, as would be
	 *		used to represent a double slip turnout. The turnouts may be either
	 *		left-handed, right-handed, wye, or any pair of these. This tool also
	 *		assumes that there are no signals at the throat junction. The signal
	 *		heads will be rotated to face outward--away from the throats. Four
	 *		sets of one or two signal heads will be placed, one at each of the
	 *		converging and diverging for each turnout.
	 * <P>
	 * This tool assumes that each of the four tracks is contained in a 
	 *		different block. Things work best if the two throat-to-throat
	 *		turnouts are in their own separate block, but this is not necessary.
	 * <P>
	 * This tool will place icons on the outside edges of each turnout.
	 * <P>
	 * At least one signal at each of the four connection points is 
	 *		required. A second signal at each is optional.
	 * <P>
	 * This tool only places signal icons if the turnout is either mostly vertical 
	 *		or mostly horizontal. Some user adjustment may be needed.
	 */	
	
	// operational variables for Set Signals at Double Crossover Turnout tool
	private JmriJFrame setSignalsAtTToTFrame = null;
	private boolean setSignalsAtTToTOpen = false;
	private JTextField turnout1NameField = new JTextField(16);
	private JTextField turnout2NameField = new JTextField(16);
	//private JTextField ttNameField = new JTextField(16);
	private JTextField a1TToTField = new JTextField(16);
	private JTextField a2TToTField = new JTextField(16);
	private JTextField b1TToTField = new JTextField(16);
	private JTextField b2TToTField = new JTextField(16);
	private JTextField c1TToTField = new JTextField(16);
	private JTextField c2TToTField = new JTextField(16);
	private JTextField d1TToTField = new JTextField(16);
	private JTextField d2TToTField = new JTextField(16);
	private JCheckBox setA1TToTHead = new JCheckBox(rb.getString("PlaceHead"));
	private JCheckBox setupA1TToTLogic = new JCheckBox(rb.getString("SetLogic"));
	private JCheckBox setA2TToTHead = new JCheckBox(rb.getString("PlaceHead"));
	private JCheckBox setupA2TToTLogic = new JCheckBox(rb.getString("SetLogic"));
	private JCheckBox setB1TToTHead = new JCheckBox(rb.getString("PlaceHead"));
	private JCheckBox setupB1TToTLogic = new JCheckBox(rb.getString("SetLogic"));
	private JCheckBox setB2TToTHead = new JCheckBox(rb.getString("PlaceHead"));
	private JCheckBox setupB2TToTLogic = new JCheckBox(rb.getString("SetLogic"));
	private JCheckBox setC1TToTHead = new JCheckBox(rb.getString("PlaceHead"));
	private JCheckBox setupC1TToTLogic = new JCheckBox(rb.getString("SetLogic"));
	private JCheckBox setC2TToTHead = new JCheckBox(rb.getString("PlaceHead"));
	private JCheckBox setupC2TToTLogic = new JCheckBox(rb.getString("SetLogic"));
	private JCheckBox setD1TToTHead = new JCheckBox(rb.getString("PlaceHead"));
	private JCheckBox setupD1TToTLogic = new JCheckBox(rb.getString("SetLogic"));
	private JCheckBox setD2TToTHead = new JCheckBox(rb.getString("PlaceHead"));
	private JCheckBox setupD2TToTLogic = new JCheckBox(rb.getString("SetLogic"));
	private JButton getSavedTToTSignalHeads = null;
	private JButton changeTToTSignalIcon = null;
	private JButton setTToTSignalsDone = null;
	private JButton setTToTSignalsCancel = null;
	private LayoutTurnout layoutTurnout1 = null;
	private LayoutTurnout layoutTurnout2 = null;
	private Turnout turnout1 = null;
	private Turnout turnout2 = null;
	private TrackSegment connectorTrack = null;
	private SignalHead a1TToTHead = null;
	private SignalHead a2TToTHead = null;
	private SignalHead b1TToTHead = null;
	private SignalHead b2TToTHead = null;
	private SignalHead c1TToTHead = null;
	private SignalHead c2TToTHead = null;
	private SignalHead d1TToTHead = null;
	private SignalHead d2TToTHead = null;
	private boolean	layoutTurnout1Horizontal = false;
	private boolean	layoutTurnout1Vertical = false;
	private boolean	layoutTurnout2Horizontal = false;
	private boolean	layoutTurnout2Vertical = false;
	private boolean	layoutTurnout1ThroatLeft = false;
	private boolean	layoutTurnout1ThroatUp = false;
	private boolean	layoutTurnout2ThroatLeft = false;
	private boolean	layoutTurnout2ThroatUp = false;
	private boolean	layoutTurnout1BUp = false;
	private boolean	layoutTurnout1BLeft = false;
	private boolean	layoutTurnout2BUp = false;
	private boolean	layoutTurnout2BLeft = false;
	public void setThroatToThroatFromMenu( LayoutTurnout to, String linkedTurnoutName,
					MultiIconEditor theEditor, JFrame theFrame ) {
		turnout1NameField.setText(to.getTurnoutName());
		turnout2NameField.setText(linkedTurnoutName);
		a1TToTField.setText("");
		a2TToTField.setText("");
		b1TToTField.setText("");
		b2TToTField.setText("");
		c1TToTField.setText("");
		c2TToTField.setText("");
		d1TToTField.setText("");
		d2TToTField.setText("");
		setSignalsAtTToTTurnouts(theEditor,theFrame);
	}	
	public void setSignalsAtTToTTurnouts( MultiIconEditor theEditor, JFrame theFrame ) {
		signalIconEditor = theEditor;
		signalFrame = theFrame;
		if (setSignalsAtTToTOpen) {
			setSignalsAtTToTFrame.setVisible(true);
			return;
		}
		// Initialize if needed
		if (setSignalsAtTToTFrame == null) {
            setSignalsAtTToTFrame = new JmriJFrame( rb.getString("SignalsAtTToTTurnout"), false, true );
            setSignalsAtTToTFrame.addHelpMenu("package.jmri.jmrit.display.SetSignalsAtTToTTurnout", true);
            setSignalsAtTToTFrame.setLocation(70,30);
            Container theContentPane = setSignalsAtTToTFrame.getContentPane();        
            theContentPane.setLayout(new BoxLayout(theContentPane, BoxLayout.Y_AXIS));
			JPanel panel1 = new JPanel();
            panel1.setLayout(new FlowLayout());
			JLabel turnout1NameLabel = new JLabel( rb.getString("Turnout")+" 1 "+
																rb.getString("Name") );
			panel1.add(turnout1NameLabel);
			panel1.add(turnout1NameField);
			turnout1NameField.setToolTipText(rb.getString("SignalsTurnoutNameHint"));
            theContentPane.add(panel1);
			JPanel panel11 = new JPanel();
            panel11.setLayout(new FlowLayout());
			JLabel turnout2NameLabel = new JLabel( rb.getString("Turnout")+" 2 "+
																rb.getString("Name") );
			panel11.add(turnout2NameLabel);
			panel11.add(turnout2NameField);
			turnout2NameField.setToolTipText(rb.getString("SignalsTurnoutNameHint"));
			theContentPane.add(panel11);
			theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));
			// Provide for retrieval of names of previously saved signal heads
            JPanel panel2 = new JPanel();
			JLabel shTitle = new JLabel(rb.getString("SignalHeads"));
			panel2.add(shTitle);			
			panel2.add(new JLabel("     "));
            panel2.add(getSavedTToTSignalHeads = new JButton(rb.getString("GetSaved")));
            getSavedTToTSignalHeads.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						tToTTurnoutSignalsGetSaved(e);
					}
				});
            getSavedTToTSignalHeads.setToolTipText( rb.getString("GetSavedHint") );
			theContentPane.add(panel2);
			theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));
			// Signal heads located at turnout 1			
			JPanel panel21x = new JPanel();
            panel21x.setLayout(new FlowLayout());
			panel21x.add(new JLabel(rb.getString("SignalLocated")+" "+rb.getString("Turnout")+" 1 - "+
											rb.getString("ContinuingTrack")));
			theContentPane.add(panel21x);
			JPanel panel21 = new JPanel();
            panel21.setLayout(new FlowLayout());
			panel21.add(new JLabel(rb.getString("ProtectsTurnout")+" 2 - "+rb.getString("ContinuingTrack")+" : "));
			panel21.add(a1TToTField);
			theContentPane.add(panel21);
			a1TToTField.setToolTipText(rb.getString("SignalHeadNameHint"));
            JPanel panel22 = new JPanel();
            panel22.setLayout(new FlowLayout());
			panel22.add(new JLabel(rb.getString("OrBoth")+" 2 "+rb.getString("Tracks)")+"   "));
			panel22.add(setA1TToTHead);
			setA1TToTHead.setToolTipText(rb.getString("PlaceHeadHint"));
			panel22.add(new JLabel("  "));
			panel22.add(setupA1TToTLogic);
			setupA1TToTLogic.setToolTipText(rb.getString("SetLogicHint"));
			theContentPane.add(panel22);
            JPanel panel23 = new JPanel();
            panel23.setLayout(new FlowLayout());
			panel23.add(new JLabel(rb.getString("ProtectsTurnout")+" 2 - "+rb.getString("DivergingTrack")+" : "));
			panel23.add(a2TToTField);
			theContentPane.add(panel23);
			a2TToTField.setToolTipText(rb.getString("SignalHeadNameHint"));
            JPanel panel24 = new JPanel();
            panel24.setLayout(new FlowLayout());
			panel24.add(new JLabel("                "));
			panel24.add(setA2TToTHead);
			setA2TToTHead.setToolTipText(rb.getString("PlaceHeadHint"));
			panel24.add(new JLabel("  "));
			panel24.add(setupA2TToTLogic);
			setupA2TToTLogic.setToolTipText(rb.getString("SetLogicHint"));
			theContentPane.add(panel24);
			JPanel panel31x = new JPanel();
            panel31x.setLayout(new FlowLayout());
			panel31x.add(new JLabel(rb.getString("SignalLocated")+" "+rb.getString("Turnout")+" 1 - "+
											rb.getString("DivergingTrack")));
			theContentPane.add(panel31x);
            JPanel panel31 = new JPanel();
            panel31.setLayout(new FlowLayout());
			panel31.add(new JLabel(rb.getString("ProtectsTurnout")+" 2 - "+rb.getString("ContinuingTrack")+" : "));
			panel31.add(b1TToTField);
			theContentPane.add(panel31);
			b1TToTField.setToolTipText(rb.getString("SignalHeadNameHint"));
            JPanel panel32 = new JPanel();
            panel32.setLayout(new FlowLayout());
			panel32.add(new JLabel(rb.getString("OrBoth")+" 2 "+rb.getString("Tracks)")+"   "));
			panel32.add(setB1TToTHead);
			setB1TToTHead.setToolTipText(rb.getString("PlaceHeadHint"));
			panel32.add(new JLabel("  "));
			panel32.add(setupB1TToTLogic);
			setupB1TToTLogic.setToolTipText(rb.getString("SetLogicHint"));
			theContentPane.add(panel32);
            JPanel panel33 = new JPanel();
            panel33.setLayout(new FlowLayout());
			panel33.add(new JLabel(rb.getString("ProtectsTurnout")+" 2 - "+rb.getString("DivergingTrack")+" : "));
			panel33.add(b2TToTField);
			theContentPane.add(panel33);
			b2TToTField.setToolTipText(rb.getString("SignalHeadNameHint"));
            JPanel panel34 = new JPanel();
            panel34.setLayout(new FlowLayout());
			panel34.add(new JLabel("                "));
			panel34.add(setB2TToTHead);
			setB2TToTHead.setToolTipText(rb.getString("PlaceHeadHint"));
			panel34.add(new JLabel("  "));
			panel34.add(setupB2TToTLogic);
			setupB2TToTLogic.setToolTipText(rb.getString("SetLogicHint"));
			theContentPane.add(panel34);
			theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));
			// Signal heads located at turnout 2			
			JPanel panel41x = new JPanel();
            panel41x.setLayout(new FlowLayout());
			panel41x.add(new JLabel(rb.getString("SignalLocated")+" "+rb.getString("Turnout")+" 2 - "+
											rb.getString("ContinuingTrack")));
			theContentPane.add(panel41x);
			JPanel panel41 = new JPanel();
            panel41.setLayout(new FlowLayout());
			panel41.add(new JLabel(rb.getString("ProtectsTurnout")+" 1 - "+rb.getString("ContinuingTrack")+" : "));
			panel41.add(c1TToTField);
			theContentPane.add(panel41);
			c1TToTField.setToolTipText(rb.getString("SignalHeadNameHint"));
            JPanel panel42 = new JPanel();
            panel42.setLayout(new FlowLayout());
			panel42.add(new JLabel(rb.getString("OrBoth")+" 1 "+rb.getString("Tracks)")+"   "));
			panel42.add(setC1TToTHead);
			setC1TToTHead.setToolTipText(rb.getString("PlaceHeadHint"));
			panel42.add(new JLabel("  "));
			panel42.add(setupC1TToTLogic);
			setupC1TToTLogic.setToolTipText(rb.getString("SetLogicHint"));
			theContentPane.add(panel42);
            JPanel panel43 = new JPanel();
            panel43.setLayout(new FlowLayout());
			panel43.add(new JLabel(rb.getString("ProtectsTurnout")+" 1 - "+rb.getString("DivergingTrack")+" : "));
			panel43.add(c2TToTField);
			theContentPane.add(panel43);
			c2TToTField.setToolTipText(rb.getString("SignalHeadNameHint"));
            JPanel panel44 = new JPanel();
            panel44.setLayout(new FlowLayout());
			panel44.add(new JLabel("                "));
			panel44.add(setC2TToTHead);
			setC2TToTHead.setToolTipText(rb.getString("PlaceHeadHint"));
			panel44.add(new JLabel("  "));
			panel44.add(setupC2TToTLogic);
			setupC2TToTLogic.setToolTipText(rb.getString("SetLogicHint"));
			theContentPane.add(panel44);
			JPanel panel51x = new JPanel();
            panel51x.setLayout(new FlowLayout());
			panel51x.add(new JLabel(rb.getString("SignalLocated")+" "+rb.getString("Turnout")+" 2 - "+
											rb.getString("DivergingTrack")));
			theContentPane.add(panel51x);
            JPanel panel51 = new JPanel();
            panel51.setLayout(new FlowLayout());
			panel51.add(new JLabel(rb.getString("ProtectsTurnout")+" 1 - "+rb.getString("ContinuingTrack")+" : "));
			panel51.add(d1TToTField);
			theContentPane.add(panel51);
			d1TToTField.setToolTipText(rb.getString("SignalHeadNameHint"));
            JPanel panel52 = new JPanel();
            panel52.setLayout(new FlowLayout());
			panel52.add(new JLabel(rb.getString("OrBoth")+" 1 "+rb.getString("Tracks)")+"   "));
			panel52.add(setD1TToTHead);
			setD1TToTHead.setToolTipText(rb.getString("PlaceHeadHint"));
			panel52.add(new JLabel("  "));
			panel52.add(setupD1TToTLogic);
			setupD1TToTLogic.setToolTipText(rb.getString("SetLogicHint"));
			theContentPane.add(panel52);
            JPanel panel53 = new JPanel();
            panel53.setLayout(new FlowLayout());
			panel53.add(new JLabel(rb.getString("ProtectsTurnout")+" 1 - "+rb.getString("DivergingTrack")+" : "));
			panel53.add(d2TToTField);
			theContentPane.add(panel53);
			d2TToTField.setToolTipText(rb.getString("SignalHeadNameHint"));
            JPanel panel54 = new JPanel();
            panel54.setLayout(new FlowLayout());
			panel54.add(new JLabel("                "));
			panel54.add(setD2TToTHead);
			setD2TToTHead.setToolTipText(rb.getString("PlaceHeadHint"));
			panel54.add(new JLabel("  "));
			panel54.add(setupD2TToTLogic);
			setupD2TToTLogic.setToolTipText(rb.getString("SetLogicHint"));
			theContentPane.add(panel54);
			theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));
            JPanel panel6 = new JPanel();
            panel6.setLayout(new FlowLayout());
            panel6.add(changeTToTSignalIcon = new JButton(rb.getString("ChangeSignalIcon")));
            changeTToTSignalIcon.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						signalFrame.setVisible(true);
					}
				});
            changeTToTSignalIcon.setToolTipText( rb.getString("ChangeSignalIconHint") );
			panel6.add(new JLabel("  "));
            panel6.add(setTToTSignalsDone = new JButton(rb.getString("Done")));
            setTToTSignalsDone.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setTToTSignalsDonePressed(e);
                }
            });
            setTToTSignalsDone.setToolTipText( rb.getString("SignalDoneHint") );
            panel6.add(setTToTSignalsCancel = new JButton(rb.getString("Cancel")));
            setTToTSignalsCancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setTToTSignalsCancelPressed(e);
                }
            });
            setTToTSignalsCancel.setToolTipText( rb.getString("CancelHint") );
            theContentPane.add(panel6);
			setSignalsAtTToTFrame.addWindowListener(new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent e) {
					setTToTSignalsCancelPressed(null);
				}
			});
		}
        setSignalsAtTToTFrame.pack();
        setSignalsAtTToTFrame.setVisible(true);		
		setSignalsAtTToTOpen = true;
	}	
	private void tToTTurnoutSignalsGetSaved (ActionEvent a) {
		if ( !getTToTTurnoutInformation() ) return;
		a1TToTField.setText(layoutTurnout1.getSignalB1Name());	
		a2TToTField.setText(layoutTurnout1.getSignalB2Name());
		b1TToTField.setText(layoutTurnout1.getSignalC1Name());
		b2TToTField.setText(layoutTurnout1.getSignalC2Name());
		c1TToTField.setText(layoutTurnout2.getSignalB1Name());	
		c2TToTField.setText(layoutTurnout2.getSignalB2Name());	
		d1TToTField.setText(layoutTurnout2.getSignalC1Name());	
		d2TToTField.setText(layoutTurnout2.getSignalC2Name());	
	}
	private void setTToTSignalsCancelPressed (ActionEvent a) {
		setSignalsAtTToTOpen = false;
		setSignalsAtTToTFrame.setVisible(false);
	}
	private boolean getTToTTurnoutInformation() {
		int type = 0;
		Object connect = null;
		String str = "";
		turnout1 = null;
		turnout2 = null;
		layoutTurnout1 = null;
		layoutTurnout2 = null;
		str = turnout1NameField.getText().trim();
		if ( (str==null) || (str.equals("")) ) {
			// turnout 1 not entered, test turnout 2
			str = turnout2NameField.getText().trim();
			if ( (str==null) || (str.equals("")) ) {
				// no entries in turnout fields 
				JOptionPane.showMessageDialog(setSignalsAtTToTFrame,rb.getString("SignalsError1"),
									rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return false;
			}
			turnout2 = jmri.InstanceManager.turnoutManagerInstance().getTurnout(str);
			if (turnout2==null) {
				JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError2"),
						new Object[]{str}), rb.getString("Error"),
							JOptionPane.ERROR_MESSAGE);
				return false ;
			}
			if ( (turnout2.getUserName()==null) || (turnout2.getUserName().equals("")) ||
									!turnout2.getUserName().equals(str) ) {
				str = str.toUpperCase();
				turnout2NameField.setText(str);
			}
			layoutTurnout2 = getLayoutTurnoutFromTurnout(turnout2,false,str,setSignalsAtTToTFrame);
			if (layoutTurnout2==null)
				return false;
			// have turnout 2 and layout turnout 2 - look for turnout 1
			connectorTrack = (TrackSegment)layoutTurnout2.getConnectA();
			if (connectorTrack == null) {
				// Inform user of error, and terminate
				JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
						rb.getString("SignalsError18"),
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return false;
			}
			type = connectorTrack.getType1();
			connect = connectorTrack.getConnect1();
			if (connect == layoutTurnout2) {
				type = connectorTrack.getType2();
				connect = connectorTrack.getConnect2();
			}
			if ( (type != LayoutEditor.TURNOUT_A) || (connect==null) ) {
				// Not two turnouts connected throat-to-throat by a single Track Segment
				// Inform user of error and terminate
				JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
						rb.getString("SignalsError18"),
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return false;
			}
			layoutTurnout1 = (LayoutTurnout)connect;
			turnout1 = layoutTurnout1.getTurnout();
			if (turnout1==null) {
				JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
						rb.getString("SignalsError18"),
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return false;
			}
			turnout1NameField.setText(layoutTurnout1.getTurnoutName());
		}
		else {
			// something was entered in the turnout 1 field
			turnout1 = jmri.InstanceManager.turnoutManagerInstance().getTurnout(str);
			if (turnout1==null) {
				JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError2"),
						new Object[]{str}), rb.getString("Error"),
							JOptionPane.ERROR_MESSAGE);
				return false ;
			}
			if ( (turnout1.getUserName()==null) || (turnout1.getUserName().equals("")) ||
									!turnout1.getUserName().equals(str) ) {
				str = str.toUpperCase();
				turnout1NameField.setText(str);
			}
			// have turnout 1 - get corresponding layoutTurnout
			layoutTurnout1 = getLayoutTurnoutFromTurnout(turnout1,false,str,setSignalsAtTToTFrame);
			if (layoutTurnout1 == null)
				return false;
			turnout1NameField.setText(str);
			// have turnout 1 and layout turnout 1 - was something entered for turnout 2
			str = turnout2NameField.getText().trim();
			if ( (str==null) || (str.equals("")) ) {
				// no entry for turnout 2
				connectorTrack = (TrackSegment)layoutTurnout1.getConnectA();
				if (connectorTrack == null) {
					// Inform user of error, and terminate
					JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
							rb.getString("SignalsError18"),
								rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
					return false;
				}
				type = connectorTrack.getType1();
				connect = connectorTrack.getConnect1();
				if (connect == layoutTurnout1) {
					type = connectorTrack.getType2();
					connect = connectorTrack.getConnect2();
				}
				if ( (type != LayoutEditor.TURNOUT_A) || (connect==null) ) {
					// Not two turnouts connected throat-to-throat by a single Track Segment
					// Inform user of error and terminate
					JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
							rb.getString("SignalsError18"),
								rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
					return false;
				}
				layoutTurnout2 = (LayoutTurnout)connect;
				turnout2 = layoutTurnout2.getTurnout();
				if (turnout2==null) {
					JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
							rb.getString("SignalsError18"),
								rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
					return false;
				}
				turnout2NameField.setText(layoutTurnout2.getTurnoutName());
			}
			else {
				// turnout 2 entered also
				turnout2 = jmri.InstanceManager.turnoutManagerInstance().getTurnout(str);
				if (turnout2==null) {
					JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
						java.text.MessageFormat.format(rb.getString("SignalsError2"),
								new Object[]{str}), rb.getString("Error"),
									JOptionPane.ERROR_MESSAGE);
					return false ;
				}
				if ( (turnout2.getUserName()==null) || (turnout2.getUserName().equals("")) ||
									!turnout2.getUserName().equals(str) ) {
					str = str.toUpperCase();
					turnout2NameField.setText(str);
				}
				layoutTurnout2 = getLayoutTurnoutFromTurnout(turnout2,false,str,setSignalsAtTToTFrame);
				if (layoutTurnout2==null)
					return false;
				turnout2NameField.setText(str);
				// check that layout turnout 1 and layout turnout 2 are connected throat-to-throat 
				if (layoutTurnout1.getConnectA()!=layoutTurnout2.getConnectA()) {
					// Not two turnouts connected throat-to-throat by a single Track Segment
					// Inform user of error and terminate
					JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
							rb.getString("SignalsError18"),
								rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
					return false;
				}
				connectorTrack = (TrackSegment)layoutTurnout1.getConnectA();
			}
		}
		// have both turnouts, correctly connected - complete initialization 
		layoutTurnout1Horizontal = false;
		layoutTurnout1Vertical = false;
		layoutTurnout2ThroatLeft = false;
		layoutTurnout2Vertical = false;
		layoutTurnout1ThroatLeft = false;
		layoutTurnout1ThroatUp = false;
		layoutTurnout2ThroatLeft = false;
		layoutTurnout2ThroatUp = false;
		layoutTurnout1BUp = false;
		layoutTurnout1BLeft = false;
		layoutTurnout2BUp = false;
		layoutTurnout2BLeft = false;
		double delX = layoutTurnout1.getCoordsA().getX() - layoutTurnout1.getCoordsB().getX();
		double delY = layoutTurnout1.getCoordsA().getY() - layoutTurnout1.getCoordsB().getY();
		if (Math.abs(delX) > 2.0*Math.abs(delY)) {
			layoutTurnout1Horizontal = true;
			if (delX < 0.0) layoutTurnout1ThroatLeft = true;
			if (layoutTurnout1.getCoordsB().getY() < layoutTurnout1.getCoordsC().getY())
				layoutTurnout1BUp = true;
		}
		else if (Math.abs(delY) > 2.0*Math.abs(delX)) { 
			layoutTurnout1Vertical = true;
			if (delY <0.0) layoutTurnout1ThroatUp = true;
			if (layoutTurnout1.getCoordsB().getX() < layoutTurnout1.getCoordsC().getX())
				layoutTurnout1BLeft = true;
		}
		delX = layoutTurnout2.getCoordsA().getX() - layoutTurnout2.getCoordsB().getX();
		delY = layoutTurnout2.getCoordsA().getY() - layoutTurnout2.getCoordsB().getY();
		if (Math.abs(delX) > 2.0*Math.abs(delY)) {
			layoutTurnout2Horizontal = true;
			if (delX < 0.0) layoutTurnout2ThroatLeft = true;
			if (layoutTurnout2.getCoordsB().getY() < layoutTurnout2.getCoordsC().getY())
				layoutTurnout2BUp = true;
		}
		else if (Math.abs(delY) > 2.0*Math.abs(delX)) { 
			layoutTurnout2Vertical = true;
			if (delY <0.0) layoutTurnout2ThroatUp = true;
			if (layoutTurnout2.getCoordsB().getX() < layoutTurnout2.getCoordsC().getX())
				layoutTurnout2BLeft = true;
		}
		return true;
	}
	private void setTToTSignalsDonePressed (ActionEvent a) {
		if ( !getTToTTurnoutInformation() ) return;
		if ( !getTToTSignalHeadInformation() ) return;
		// place signal icons if requested, and assign signal heads to this turnout
		if (setA1TToTHead.isSelected()) {
			if (isHeadOnPanel(a1TToTHead) &&
				(a1TToTHead!=getHeadFromName(layoutTurnout1.getSignalB1Name()))) {
				JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError6"),
						new Object[]{a1Field.getText().trim()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else if ( (!layoutTurnout1Horizontal) && (!layoutTurnout1Vertical) ) {
				JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
					rb.getString("InfoMessage2"),"",JOptionPane.INFORMATION_MESSAGE);
				if (a1TToTHead!=getHeadFromName(layoutTurnout1.getSignalB1Name())) {				
					removeSignalHeadFromPanel(layoutTurnout1.getSignalB1Name());
					removeAssignment(a1TToTHead);
					layoutTurnout1.setSignalB1Name(a1TToTField.getText().trim());
				}
			}				
			else {
				removeSignalHeadFromPanel(layoutTurnout1.getSignalB1Name());
				if (layoutTurnout1.getContinuingSense()==Turnout.CLOSED)
					placeA1TToT(a1TToTField.getText().trim());
				else 
					placeB1TToT(a1TToTField.getText().trim());
				removeAssignment(a1TToTHead);
				layoutTurnout1.setSignalB1Name(a1TToTField.getText().trim());
				needRedraw = true;
			}		
		}
		else {
			int assigned = isHeadAssignedHere(a1TToTHead,layoutTurnout1);
			if (assigned == NONE) {
				if ( isHeadOnPanel(a1TToTHead) && 
									isHeadAssignedAnywhere(a1TToTHead) ) {
					JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
						java.text.MessageFormat.format(rb.getString("SignalsError8"),
							new Object[]{a1TToTField.getText().trim()}), 
								rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
					return;
				}		
				else {
					removeSignalHeadFromPanel(layoutTurnout1.getSignalB1Name());
					removeAssignment(a1TToTHead);
					layoutTurnout1.setSignalB1Name(a1TToTField.getText().trim());
				}
			}
			else if (assigned!=B1) {
// need to figure out what to do in this case - assigned to a different position on the same turnout.			
			}
		}
		if ( (a2TToTHead!=null) && setA2TToTHead.isSelected() ) {
			if (isHeadOnPanel(a2TToTHead) &&
				(a2TToTHead!=getHeadFromName(layoutTurnout1.getSignalB2Name()))) {
				JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError6"),
						new Object[]{a2Field.getText().trim()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else if ( (!layoutTurnout1Horizontal) && (!layoutTurnout1Vertical) ) {
				JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
					rb.getString("InfoMessage2"),"",JOptionPane.INFORMATION_MESSAGE);
				if (a2TToTHead!=getHeadFromName(layoutTurnout1.getSignalB2Name())) {				
					removeSignalHeadFromPanel(layoutTurnout1.getSignalB2Name());
					removeAssignment(a2TToTHead);
					layoutTurnout1.setSignalB2Name(a2TToTField.getText().trim());
				}
			}				
			else {
				removeSignalHeadFromPanel(layoutTurnout1.getSignalB2Name());
				if (layoutTurnout1.getContinuingSense()==Turnout.CLOSED)
					placeA2TToT(a2TToTField.getText().trim());
				else 
					placeB2TToT(a2TToTField.getText().trim());
				removeAssignment(a2TToTHead);
				layoutTurnout1.setSignalB2Name(a2TToTField.getText().trim());
				needRedraw = true;
			}		
		}
		else if (a2TToTHead!=null) {
			int assigned = isHeadAssignedHere(a2TToTHead,layoutTurnout1);
			if (assigned == NONE) {
				if ( isHeadOnPanel(a2TToTHead) && 
									isHeadAssignedAnywhere(a2TToTHead) ) {
					JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
						java.text.MessageFormat.format(rb.getString("SignalsError8"),
							new Object[]{a2TToTField.getText().trim()}), 
								rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
					return;
				}		
				else {
					removeSignalHeadFromPanel(layoutTurnout1.getSignalB2Name());
					removeAssignment(a2TToTHead);
					layoutTurnout1.setSignalB2Name(a2TToTField.getText().trim());
				}
			}
			else if (assigned!=B2) {
// need to figure out what to do in this case.			
			}
		}
		else if (a2TToTHead==null) {
			removeSignalHeadFromPanel(layoutTurnout1.getSignalB2Name());
			layoutTurnout1.setSignalB2Name("");
		}
		if (setB1TToTHead.isSelected()) {
			if (isHeadOnPanel(b1TToTHead) &&
				(b1TToTHead!=getHeadFromName(layoutTurnout1.getSignalC1Name()))) {
				JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError6"),
						new Object[]{b1TToTField.getText().trim()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else if ( (!layoutTurnout1Horizontal) && (!layoutTurnout1Vertical) ) {
				JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
					rb.getString("InfoMessage2"),"",JOptionPane.INFORMATION_MESSAGE);
				if (b1TToTHead!=getHeadFromName(layoutTurnout1.getSignalC1Name())) {				
					removeSignalHeadFromPanel(layoutTurnout.getSignalC1Name());
					removeAssignment(b1TToTHead);
					layoutTurnout1.setSignalC1Name(b1TToTField.getText().trim());
				}
			}				
			else {
				removeSignalHeadFromPanel(layoutTurnout1.getSignalC1Name());
				if (layoutTurnout1.getContinuingSense()==Turnout.CLOSED)
					placeB1TToT(b1TToTField.getText().trim());
				else 
					placeA1TToT(b1TToTField.getText().trim());
				removeAssignment(b1TToTHead);
				layoutTurnout1.setSignalC1Name(b1TToTField.getText().trim());
				needRedraw = true;
			}		
		}
		else {
			int assigned = isHeadAssignedHere(b1TToTHead,layoutTurnout1);
			if (assigned == NONE) {
				if ( isHeadOnPanel(b1TToTHead) && 
									isHeadAssignedAnywhere(b1TToTHead) ) {
					JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
						java.text.MessageFormat.format(rb.getString("SignalsError8"),
							new Object[]{b1TToTField.getText().trim()}), 
								rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
					return;
				}		
				else {
					removeSignalHeadFromPanel(layoutTurnout1.getSignalC1Name());
					removeAssignment(b1TToTHead);
					layoutTurnout1.setSignalC1Name(b1TToTField.getText().trim());
				}
			}
			else if (assigned!=C1) {
// need to figure out what to do in this case.			
			}
		}
		if ( (b2TToTHead!=null) && setB2TToTHead.isSelected() ) {
			if (isHeadOnPanel(b2TToTHead) &&
				(b2TToTHead!=getHeadFromName(layoutTurnout1.getSignalC2Name()))) {
				JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError6"),
						new Object[]{b2TToTField.getText().trim()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else if ( (!layoutTurnout1Horizontal) && (!layoutTurnout1Vertical) ) {
				JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
					rb.getString("InfoMessage2"),"",JOptionPane.INFORMATION_MESSAGE);
				if (b2TToTHead!=getHeadFromName(layoutTurnout1.getSignalC2Name())) {				
					removeSignalHeadFromPanel(layoutTurnout1.getSignalC2Name());
					removeAssignment(b2TToTHead);
					layoutTurnout1.setSignalC2Name(b2TToTField.getText().trim());
				}
			}				
			else {
				removeSignalHeadFromPanel(layoutTurnout1.getSignalC2Name());
				if (layoutTurnout1.getContinuingSense()==Turnout.CLOSED)
					placeB2TToT(b2TToTField.getText().trim());
				else 
					placeA2TToT(b2TToTField.getText().trim());
				removeAssignment(b2TToTHead);
				layoutTurnout1.setSignalC2Name(b2TToTField.getText().trim());
				needRedraw = true;
			}		
		}
		else if (b2TToTHead!=null) {
			int assigned = isHeadAssignedHere(b2TToTHead,layoutTurnout1);
			if (assigned == NONE) {
				if ( isHeadOnPanel(b2TToTHead) && 
									isHeadAssignedAnywhere(b2TToTHead) ) {
					JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
						java.text.MessageFormat.format(rb.getString("SignalsError8"),
							new Object[]{b2TToTField.getText().trim()}), 
								rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
					return;
				}		
				else {
					removeSignalHeadFromPanel(layoutTurnout1.getSignalC2Name());
					removeAssignment(b2TToTHead);
					layoutTurnout1.setSignalC2Name(b2TToTField.getText().trim());
				}
			}
			else if (assigned!=C2) {
// need to figure out what to do in this case.			
			}
		}
		else if (b2TToTHead==null) {
			removeSignalHeadFromPanel(layoutTurnout1.getSignalC2Name());
			layoutTurnout1.setSignalC2Name("");
		}
		// signal heads on turnout 2
		if (setC1TToTHead.isSelected()) {
			if (isHeadOnPanel(c1TToTHead) &&
				(c1TToTHead!=getHeadFromName(layoutTurnout2.getSignalB1Name()))) {
				JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError6"),
						new Object[]{c1TToTField.getText().trim()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else if ( (!layoutTurnout2Horizontal) && (!layoutTurnout2Vertical) ) {
				JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
					rb.getString("InfoMessage2"),"",JOptionPane.INFORMATION_MESSAGE);
				if (c1TToTHead!=getHeadFromName(layoutTurnout2.getSignalB1Name())) {				
					removeSignalHeadFromPanel(layoutTurnout2.getSignalB1Name());
					removeAssignment(c1TToTHead);
					layoutTurnout2.setSignalB1Name(c1TToTField.getText().trim());
				}
			}				
			else {
				removeSignalHeadFromPanel(layoutTurnout2.getSignalB1Name());
				if (layoutTurnout2.getContinuingSense()==Turnout.CLOSED)
					placeC1TToT(c1TToTField.getText().trim());
				else 
					placeD1TToT(c1TToTField.getText().trim());
				removeAssignment(c1TToTHead);
				layoutTurnout2.setSignalB1Name(c1TToTField.getText().trim());
				needRedraw = true;
			}		
		}
		else {
			int assigned = isHeadAssignedHere(c1TToTHead,layoutTurnout2);
			if (assigned == NONE) {
				if ( isHeadOnPanel(c1TToTHead) && 
									isHeadAssignedAnywhere(c1TToTHead) ) {
					JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
						java.text.MessageFormat.format(rb.getString("SignalsError8"),
							new Object[]{c1TToTField.getText().trim()}), 
								rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
					return;
				}		
				else {
					removeSignalHeadFromPanel(layoutTurnout2.getSignalB1Name());
					removeAssignment(c1TToTHead);
					layoutTurnout2.setSignalB1Name(c1TToTField.getText().trim());
				}
			}
			else if (assigned!=B1) {
// need to figure out what to do in this case.			
			}
		}
		if ( (c2TToTHead!=null) && setC2TToTHead.isSelected() ) {
			if (isHeadOnPanel(c2TToTHead) &&
				(c2TToTHead!=getHeadFromName(layoutTurnout2.getSignalB2Name()))) {
				JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError6"),
						new Object[]{c2TToTField.getText().trim()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else if ( (!layoutTurnout2Horizontal) && (!layoutTurnout2Vertical) ) {
				JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
					rb.getString("InfoMessage2"),"",JOptionPane.INFORMATION_MESSAGE);
				if (c2TToTHead!=getHeadFromName(layoutTurnout2.getSignalB2Name())) {				
					removeSignalHeadFromPanel(layoutTurnout2.getSignalB2Name());
					removeAssignment(c2TToTHead);
					layoutTurnout2.setSignalC2Name(c2TToTField.getText().trim());
				}
			}				
			else {
				removeSignalHeadFromPanel(layoutTurnout2.getSignalB2Name());
				if (layoutTurnout2.getContinuingSense()==Turnout.CLOSED)
					placeC2TToT(c2TToTField.getText().trim());
				else 
					placeD2TToT(c2TToTField.getText().trim());
				removeAssignment(c2TToTHead);
				layoutTurnout2.setSignalB2Name(c2TToTField.getText().trim());
				needRedraw = true;
			}		
		}
		else if (c2TToTHead!=null) {
			int assigned = isHeadAssignedHere(c2TToTHead,layoutTurnout2);
			if (assigned == NONE) {
				if ( isHeadOnPanel(c2TToTHead) && 
									isHeadAssignedAnywhere(c2TToTHead) ) {
					JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
						java.text.MessageFormat.format(rb.getString("SignalsError8"),
							new Object[]{c2TToTField.getText().trim()}), 
								rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
					return;
				}		
				else {
					removeSignalHeadFromPanel(layoutTurnout2.getSignalB2Name());
					removeAssignment(c2TToTHead);
					layoutTurnout2.setSignalB2Name(c2TToTField.getText().trim());
				}
			}
			else if (assigned!=B2) {
// need to figure out what to do in this case.			
			}
		}
		else if (c2TToTHead==null) {
			removeSignalHeadFromPanel(layoutTurnout2.getSignalB2Name());
			layoutTurnout2.setSignalB2Name("");
		}
		if (setD1TToTHead.isSelected()) {
			if (isHeadOnPanel(d1TToTHead) &&
				(d1TToTHead!=getHeadFromName(layoutTurnout2.getSignalC1Name()))) {
					JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError6"),
						new Object[]{d1TToTField.getText().trim()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else if ( (!layoutTurnout2Horizontal) && (!layoutTurnout2Vertical) ) {
				JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
					rb.getString("InfoMessage2"),"",JOptionPane.INFORMATION_MESSAGE);
				if (d1TToTHead!=getHeadFromName(layoutTurnout2.getSignalC1Name())) {				
					removeSignalHeadFromPanel(layoutTurnout2.getSignalC1Name());
					removeAssignment(d1TToTHead);
					layoutTurnout2.setSignalC1Name(d1TToTField.getText().trim());
				}
			}				
			else {
				removeSignalHeadFromPanel(layoutTurnout2.getSignalC1Name());
				if (layoutTurnout2.getContinuingSense()==Turnout.CLOSED)
					placeD1TToT(d1TToTField.getText().trim());
				else 
					placeC1TToT(d1TToTField.getText().trim());
				removeAssignment(d1TToTHead);
				layoutTurnout2.setSignalC1Name(d1TToTField.getText().trim());
				needRedraw = true;
			}		
		}
		else {
			int assigned = isHeadAssignedHere(d1TToTHead,layoutTurnout2);
			if (assigned == NONE) {
				if ( isHeadOnPanel(d1TToTHead) && 
									isHeadAssignedAnywhere(d1TToTHead) ) {
					JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
						java.text.MessageFormat.format(rb.getString("SignalsError8"),
							new Object[]{d1TToTField.getText().trim()}), 
								rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
					return;
				}		
				else {
					removeSignalHeadFromPanel(layoutTurnout2.getSignalC1Name());
					removeAssignment(d1TToTHead);
					layoutTurnout2.setSignalC1Name(d1TToTField.getText().trim());
				}
			}
			else if (assigned!=C1) {
// need to figure out what to do in this case.			
			}
		}
		if ( (d2TToTHead!=null) && setD2TToTHead.isSelected() ) {
			if (isHeadOnPanel(d2TToTHead) &&
				(d2TToTHead!=getHeadFromName(layoutTurnout2.getSignalC2Name()))) {
				JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError6"),
						new Object[]{d2TToTField.getText().trim()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else if ( (!layoutTurnout2Horizontal) && (!layoutTurnout2Vertical) ) {
				JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
					rb.getString("InfoMessage2"),"",JOptionPane.INFORMATION_MESSAGE);
				if (d2TToTHead!=getHeadFromName(layoutTurnout2.getSignalC2Name())) {				
					removeSignalHeadFromPanel(layoutTurnout2.getSignalC2Name());
					removeAssignment(d2TToTHead);
					layoutTurnout2.setSignalC2Name(d2TToTField.getText().trim());
				}
			}				
			else {
				removeSignalHeadFromPanel(layoutTurnout2.getSignalC2Name());
				if (layoutTurnout2.getContinuingSense()==Turnout.CLOSED)
					placeD2TToT(d2TToTField.getText().trim());
				else 
					placeC2TToT(d2TToTField.getText().trim());
				removeAssignment(d2TToTHead);
				layoutTurnout2.setSignalC2Name(d2TToTField.getText().trim());
				needRedraw = true;
			}		
		}
		else if (d2TToTHead!=null) {
			int assigned = isHeadAssignedHere(d2TToTHead,layoutTurnout2);
			if (assigned == NONE) {
				if ( isHeadOnPanel(d2TToTHead) && 
									isHeadAssignedAnywhere(d2TToTHead) ) {
					JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
						java.text.MessageFormat.format(rb.getString("SignalsError8"),
							new Object[]{d2TToTField.getText().trim()}), 
								rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
					return;
				}		
				else {
					removeSignalHeadFromPanel(layoutTurnout2.getSignalC2Name());
					removeAssignment(d2TToTHead);
					layoutTurnout2.setSignalC2Name(d2TToTField.getText().trim());
				}
			}
			else if (assigned!=C2) {
// need to figure out what to do in this case.			
			}
		}
		else if (d2TToTHead==null) {
			removeSignalHeadFromPanel(layoutTurnout2.getSignalC2Name());
			layoutTurnout2.setSignalC2Name("");
		}
		
		// setup logic if requested
		if (setupA1TToTLogic.isSelected() || setupA2TToTLogic.isSelected()) {
			setLogicTToT(a1TToTHead,(TrackSegment)layoutTurnout2.getConnectB(),a2TToTHead,
					(TrackSegment)layoutTurnout2.getConnectC(),setupA1TToTLogic.isSelected(),
					setupA2TToTLogic.isSelected(),true,layoutTurnout2,layoutTurnout1);
		}
		if (setupB1TToTLogic.isSelected() || setupB2TToTLogic.isSelected()) {
			setLogicTToT(b1TToTHead,(TrackSegment)layoutTurnout2.getConnectB(),b2TToTHead,
					(TrackSegment)layoutTurnout2.getConnectC(),setupB1TToTLogic.isSelected(),
					setupB2TToTLogic.isSelected(),false,layoutTurnout2,layoutTurnout1);
		}
		if (setupC1TToTLogic.isSelected() || setupC2TToTLogic.isSelected()) {
			setLogicTToT(c1TToTHead,(TrackSegment)layoutTurnout1.getConnectB(),c2TToTHead,
					(TrackSegment)layoutTurnout1.getConnectC(),setupC1TToTLogic.isSelected(),
					setupC2TToTLogic.isSelected(),true,layoutTurnout1,layoutTurnout2);
		}
		if (setupD1TToTLogic.isSelected() || setupD2TToTLogic.isSelected()) {
			setLogicTToT(d1TToTHead,(TrackSegment)layoutTurnout1.getConnectB(),d2TToTHead,
					(TrackSegment)layoutTurnout1.getConnectC(),setupD1TToTLogic.isSelected(),
					setupD2TToTLogic.isSelected(),false,layoutTurnout1,layoutTurnout2);
		}
		// link the two turnouts
		layoutTurnout1.setLinkedTurnoutName(turnout2NameField.getText().trim());
		layoutTurnout1.setLinkType(LayoutTurnout.THROAT_TO_THROAT);
		layoutTurnout2.setLinkedTurnoutName(turnout1NameField.getText().trim());
		layoutTurnout2.setLinkType(LayoutTurnout.THROAT_TO_THROAT);
		// finish up
		setSignalsAtTToTOpen = false;
		setSignalsAtTToTFrame.setVisible(false);
		if (needRedraw) {
			layoutEditor.redrawPanel();
			needRedraw = false;
			layoutEditor.setDirty();
		}		
	}
	private boolean getTToTSignalHeadInformation() {
		a1TToTHead = getSignalHeadFromEntry(a1TToTField,true,setSignalsAtTToTFrame);
		if (a1TToTHead==null) return false;
		a2TToTHead = getSignalHeadFromEntry(a2TToTField,false,setSignalsAtTToTFrame);
		b1TToTHead = getSignalHeadFromEntry(b1TToTField,true,setSignalsAtTToTFrame);
		if (b1TToTHead==null) return false;
		b2TToTHead = getSignalHeadFromEntry(b2TToTField,false,setSignalsAtTToTFrame);
		c1TToTHead = getSignalHeadFromEntry(c1TToTField,true,setSignalsAtTToTFrame);
		if (c1TToTHead==null) return false;
		c2TToTHead = getSignalHeadFromEntry(c2TToTField,false,setSignalsAtTToTFrame);
		d1TToTHead = getSignalHeadFromEntry(d1TToTField,true,setSignalsAtTToTFrame);
		if (d1TToTHead==null) return false;
		d2TToTHead = getSignalHeadFromEntry(d2TToTField,false,setSignalsAtTToTFrame);
		return true;
	}
	private void placeA1TToT(String headName) {
		// place head near the continuing track of turnout 1
		if (testIcon == null)
			testIcon = signalIconEditor.getIcon(0);
		if( layoutTurnout1Horizontal && layoutTurnout1ThroatLeft && layoutTurnout1BUp ) {
			setSignalHeadOnPanel(0,headName,
				(int)(layoutTurnout1.getCoordsB().getX()),
				(int)(layoutTurnout1.getCoordsB().getY()-4-testIcon.getIconHeight()) );
		}
		else if( layoutTurnout1Horizontal && layoutTurnout1ThroatLeft && (!layoutTurnout1BUp) ) {
			setSignalHeadOnPanel(0,headName,
				(int)(layoutTurnout1.getCoordsB().getX()),
				(int)(layoutTurnout1.getCoordsB().getY()+4) );
		}
		else if( layoutTurnout1Horizontal && (!layoutTurnout1ThroatLeft) && layoutTurnout1BUp ) {
			setSignalHeadOnPanel(2,headName,
				(int)(layoutTurnout1.getCoordsB().getX()-testIcon.getIconWidth()),
				(int)(layoutTurnout1.getCoordsB().getY()-4-testIcon.getIconHeight()) );
		}
		else if( layoutTurnout1Horizontal && (!layoutTurnout1ThroatLeft) && (!layoutTurnout1BUp) ) {
			setSignalHeadOnPanel(2,headName,
				(int)(layoutTurnout1.getCoordsB().getX()-testIcon.getIconWidth()),
				(int)(layoutTurnout1.getCoordsB().getY()+4) );
		}
		else if( layoutTurnout1Vertical && layoutTurnout1ThroatUp && layoutTurnout1BLeft ) {
			setSignalHeadOnPanel(3,headName,
				(int)(layoutTurnout1.getCoordsB().getX()-4-testIcon.getIconWidth()),
				(int)(layoutTurnout1.getCoordsB().getY()) );
		}
		else if( layoutTurnout1Vertical && layoutTurnout1ThroatUp && (!layoutTurnout1BLeft) ) {
			setSignalHeadOnPanel(3,headName,
				(int)(layoutTurnout1.getCoordsB().getX()+4),
				(int)(layoutTurnout1.getCoordsB().getY()) );
		}
		else if( layoutTurnout1Vertical && (!layoutTurnout1ThroatUp) && layoutTurnout1BLeft ) {
			setSignalHeadOnPanel(1,headName,
				(int)(layoutTurnout1.getCoordsB().getX()-4-testIcon.getIconWidth()),
				(int)(layoutTurnout1.getCoordsB().getY()-testIcon.getIconHeight()) );
		}
		else if( layoutTurnout1Vertical && (!layoutTurnout1ThroatUp) && (!layoutTurnout1BLeft) ) {
			setSignalHeadOnPanel(1,headName,
				(int)(layoutTurnout1.getCoordsB().getX()+4),
				(int)(layoutTurnout1.getCoordsB().getY()-testIcon.getIconHeight()) );
		}
	}
	private void placeA2TToT(String headName) {
		if (testIcon == null)
			testIcon = signalIconEditor.getIcon(0);
		if( layoutTurnout1Horizontal && layoutTurnout1ThroatLeft && layoutTurnout1BUp ) {
			setSignalHeadOnPanel(0,headName,
				(int)(layoutTurnout1.getCoordsB().getX()+4+testIcon.getIconWidth()),
				(int)(layoutTurnout1.getCoordsB().getY()-4-testIcon.getIconHeight()) );
		}
		else if( layoutTurnout1Horizontal && layoutTurnout1ThroatLeft && (!layoutTurnout1BUp) ) {
			setSignalHeadOnPanel(0,headName,
				(int)(layoutTurnout1.getCoordsB().getX()+4+testIcon.getIconWidth()),
				(int)(layoutTurnout1.getCoordsB().getY()+4) );
		}
		else if( layoutTurnout1Horizontal && (!layoutTurnout1ThroatLeft) && layoutTurnout1BUp ) {
			setSignalHeadOnPanel(2,headName,
				(int)(layoutTurnout1.getCoordsB().getX()-4-(2*testIcon.getIconWidth())),
				(int)(layoutTurnout1.getCoordsB().getY()-4-testIcon.getIconHeight()) );
		}
		else if( layoutTurnout1Horizontal && (!layoutTurnout1ThroatLeft) && (!layoutTurnout1BUp) ) {
			setSignalHeadOnPanel(2,headName,
				(int)(layoutTurnout1.getCoordsB().getX()-4-(2*testIcon.getIconWidth())),
				(int)(layoutTurnout1.getCoordsB().getY()+4) );
		}
		else if( layoutTurnout1Vertical && layoutTurnout1ThroatUp && layoutTurnout1BLeft ) {
			setSignalHeadOnPanel(3,headName,
				(int)(layoutTurnout1.getCoordsB().getX()-4-testIcon.getIconWidth()),
				(int)(layoutTurnout1.getCoordsB().getY()+4+testIcon.getIconHeight()) );
		}
		else if( layoutTurnout1Vertical && layoutTurnout1ThroatUp && (!layoutTurnout1BLeft) ) {
			setSignalHeadOnPanel(3,headName,
				(int)(layoutTurnout1.getCoordsB().getX()+4),
				(int)(layoutTurnout1.getCoordsB().getY()+4+testIcon.getIconHeight()) );
		}
		else if( layoutTurnout1Vertical && (!layoutTurnout1ThroatUp) && layoutTurnout1BLeft ) {
			setSignalHeadOnPanel(1,headName,
				(int)(layoutTurnout1.getCoordsB().getX()-4-testIcon.getIconWidth()),
				(int)(layoutTurnout1.getCoordsB().getY()-4-(2*testIcon.getIconHeight())) );
		}
		else if( layoutTurnout1Vertical && (!layoutTurnout1ThroatUp) && (!layoutTurnout1BLeft) ) {
			setSignalHeadOnPanel(1,headName,
				(int)(layoutTurnout1.getCoordsB().getX()+4),
				(int)(layoutTurnout1.getCoordsB().getY()-4-(2*testIcon.getIconHeight())) );
		}
	}
	private void placeB1TToT(String headName) {
		if (testIcon == null)
			testIcon = signalIconEditor.getIcon(0);
		if( layoutTurnout1Horizontal && layoutTurnout1ThroatLeft && layoutTurnout1BUp) {
			setSignalHeadOnPanel(0,headName,
				(int)(layoutTurnout1.getCoordsC().getX()),
				(int)(layoutTurnout1.getCoordsC().getY()+4) );
		}
		else if( layoutTurnout1Horizontal && layoutTurnout1ThroatLeft && (!layoutTurnout1BUp) ) {
			setSignalHeadOnPanel(0,headName,
				(int)(layoutTurnout1.getCoordsC().getX()),
				(int)(layoutTurnout1.getCoordsC().getY()-4-testIcon.getIconHeight()) );
		}
		else if( layoutTurnout1Horizontal && (!layoutTurnout1ThroatLeft) && layoutTurnout1BUp ) {
			setSignalHeadOnPanel(2,headName,
				(int)(layoutTurnout1.getCoordsC().getX()-testIcon.getIconWidth()),
				(int)(layoutTurnout1.getCoordsC().getY()+4) );
		}
		else if( layoutTurnout1Horizontal && (!layoutTurnout1ThroatLeft) && (!layoutTurnout1BUp) ) {
			setSignalHeadOnPanel(2,headName,
				(int)(layoutTurnout1.getCoordsC().getX()-testIcon.getIconWidth()),
				(int)(layoutTurnout1.getCoordsC().getY()-4-testIcon.getIconHeight()) );
		}
		else if( layoutTurnout1Vertical && layoutTurnout1ThroatUp && layoutTurnout1BLeft ) {
			setSignalHeadOnPanel(3,headName,
				(int)(layoutTurnout1.getCoordsC().getX()+4),
				(int)(layoutTurnout1.getCoordsC().getY()) );
		}
		else if( layoutTurnout1Vertical && layoutTurnout1ThroatUp && (!layoutTurnout1BLeft) ) {
			setSignalHeadOnPanel(3,headName,
				(int)(layoutTurnout1.getCoordsC().getX()-4-testIcon.getIconWidth()),
				(int)(layoutTurnout1.getCoordsC().getY()) );
		}
		else if( layoutTurnout1Vertical && (!layoutTurnout1ThroatUp) && layoutTurnout1BLeft ) {
			setSignalHeadOnPanel(1,headName,
				(int)(layoutTurnout1.getCoordsC().getX()+4),
				(int)(layoutTurnout1.getCoordsC().getY()-testIcon.getIconHeight()) );
		}
		else if( layoutTurnout1Vertical && (!layoutTurnout1ThroatUp) && (!layoutTurnout1BLeft) ) {
			setSignalHeadOnPanel(1,headName,
				(int)(layoutTurnout1.getCoordsC().getX()-4-testIcon.getIconWidth()),
				(int)(layoutTurnout1.getCoordsC().getY()-testIcon.getIconHeight()) );
		}
	}
	private void placeB2TToT(String headName) {
		if (testIcon == null)
			testIcon = signalIconEditor.getIcon(0);
		if( layoutTurnout1Horizontal && layoutTurnout1ThroatLeft && layoutTurnout1BUp) {
			setSignalHeadOnPanel(0,headName,
				(int)(layoutTurnout1.getCoordsC().getX()+4+testIcon.getIconWidth()),
				(int)(layoutTurnout1.getCoordsC().getY()+4) );
		}
		else if( layoutTurnout1Horizontal && layoutTurnout1ThroatLeft && (!layoutTurnout1BUp) ) {
			setSignalHeadOnPanel(0,headName,
				(int)(layoutTurnout1.getCoordsC().getX()+4+testIcon.getIconWidth()),
				(int)(layoutTurnout1.getCoordsC().getY()-4-testIcon.getIconHeight()) );
		}
		else if( layoutTurnout1Horizontal && (!layoutTurnout1ThroatLeft) && layoutTurnout1BUp ) {
			setSignalHeadOnPanel(2,headName,
				(int)(layoutTurnout1.getCoordsC().getX()-4-(2*testIcon.getIconWidth())),
				(int)(layoutTurnout1.getCoordsC().getY()+4) );
		}
		else if( layoutTurnout1Horizontal && (!layoutTurnout1ThroatLeft) && (!layoutTurnout1BUp) ) {
			setSignalHeadOnPanel(2,headName,
				(int)(layoutTurnout1.getCoordsC().getX()-4-(2*testIcon.getIconWidth())),
				(int)(layoutTurnout1.getCoordsC().getY()-4-testIcon.getIconHeight()) );
		}
		else if( layoutTurnout1Vertical && layoutTurnout1ThroatUp && layoutTurnout1BLeft ) {
			setSignalHeadOnPanel(3,headName,
				(int)(layoutTurnout1.getCoordsC().getX()+4),
				(int)(layoutTurnout1.getCoordsC().getY()+4+testIcon.getIconHeight()) );
		}
		else if( layoutTurnout1Vertical && layoutTurnout1ThroatUp && (!layoutTurnout1BLeft) ) {
			setSignalHeadOnPanel(3,headName,
				(int)(layoutTurnout1.getCoordsC().getX()-4-testIcon.getIconWidth()),
				(int)(layoutTurnout1.getCoordsC().getY()+4+testIcon.getIconHeight()) );
		}
		else if( layoutTurnout1Vertical && (!layoutTurnout1ThroatUp) && layoutTurnout1BLeft ) {
			setSignalHeadOnPanel(1,headName,
				(int)(layoutTurnout1.getCoordsC().getX()+4),
				(int)(layoutTurnout1.getCoordsC().getY()-4-(2*testIcon.getIconHeight())) );
		}
		else if( layoutTurnout1Vertical && (!layoutTurnout1ThroatUp) && (!layoutTurnout1BLeft) ) {
			setSignalHeadOnPanel(1,headName,
				(int)(layoutTurnout1.getCoordsC().getX()-4-testIcon.getIconWidth()),
				(int)(layoutTurnout1.getCoordsC().getY()-4-(2*testIcon.getIconHeight())) );
		}
	}
	private void placeC1TToT(String headName) {
		if (testIcon == null)
			testIcon = signalIconEditor.getIcon(0);
		if( layoutTurnout2Horizontal && layoutTurnout2ThroatLeft && layoutTurnout2BUp ) {
			setSignalHeadOnPanel(0,headName,
				(int)(layoutTurnout2.getCoordsB().getX()),
				(int)(layoutTurnout2.getCoordsB().getY()-4-testIcon.getIconHeight()) );
		}
		else if( layoutTurnout2Horizontal && layoutTurnout2ThroatLeft && (!layoutTurnout2BUp) ) {
			setSignalHeadOnPanel(0,headName,
				(int)(layoutTurnout2.getCoordsB().getX()),
				(int)(layoutTurnout2.getCoordsB().getY()+4) );
		}
		else if( layoutTurnout2Horizontal && (!layoutTurnout2ThroatLeft) && layoutTurnout2BUp ) {
			setSignalHeadOnPanel(2,headName,
				(int)(layoutTurnout2.getCoordsB().getX()-testIcon.getIconWidth()),
				(int)(layoutTurnout2.getCoordsB().getY()-4-testIcon.getIconHeight()) );
		}
		else if( layoutTurnout2Horizontal && (!layoutTurnout2ThroatLeft) && (!layoutTurnout2BUp) ) {
			setSignalHeadOnPanel(2,headName,
				(int)(layoutTurnout2.getCoordsB().getX()-testIcon.getIconWidth()),
				(int)(layoutTurnout2.getCoordsB().getY()+4) );
		}
		else if( layoutTurnout2Vertical && layoutTurnout2ThroatUp && layoutTurnout2BLeft ) {
			setSignalHeadOnPanel(3,headName,
				(int)(layoutTurnout2.getCoordsB().getX()-4-testIcon.getIconWidth()),
				(int)(layoutTurnout2.getCoordsB().getY()) );
		}
		else if( layoutTurnout2Vertical && layoutTurnout2ThroatUp && (!layoutTurnout2BLeft) ) {
			setSignalHeadOnPanel(3,headName,
				(int)(layoutTurnout2.getCoordsB().getX()+4),
				(int)(layoutTurnout2.getCoordsB().getY()) );
		}
		else if( layoutTurnout2Vertical && (!layoutTurnout2ThroatUp) && layoutTurnout2BLeft ) {
			setSignalHeadOnPanel(1,headName,
				(int)(layoutTurnout2.getCoordsB().getX()-4-testIcon.getIconWidth()),
				(int)(layoutTurnout2.getCoordsB().getY()-testIcon.getIconHeight()) );
		}
		else if( layoutTurnout2Vertical && (!layoutTurnout2ThroatUp) && (!layoutTurnout2BLeft) ) {
			setSignalHeadOnPanel(1,headName,
				(int)(layoutTurnout2.getCoordsB().getX()+4),
				(int)(layoutTurnout2.getCoordsB().getY()-testIcon.getIconHeight()) );
		}
	}
	private void placeC2TToT(String headName) {
		if (testIcon == null)
			testIcon = signalIconEditor.getIcon(0);
		if( layoutTurnout2Horizontal && layoutTurnout2ThroatLeft && layoutTurnout2BUp ) {
			setSignalHeadOnPanel(0,headName,
				(int)(layoutTurnout2.getCoordsB().getX()+4+testIcon.getIconWidth()),
				(int)(layoutTurnout2.getCoordsB().getY()-4-testIcon.getIconHeight()) );
		}
		else if( layoutTurnout2Horizontal && layoutTurnout2ThroatLeft && (!layoutTurnout2BUp) ) {
			setSignalHeadOnPanel(0,headName,
				(int)(layoutTurnout2.getCoordsB().getX()+4+testIcon.getIconWidth()),
				(int)(layoutTurnout2.getCoordsB().getY()+4) );
		}
		else if( layoutTurnout2Horizontal && (!layoutTurnout2ThroatLeft) && layoutTurnout2BUp ) {
			setSignalHeadOnPanel(2,headName,
				(int)(layoutTurnout2.getCoordsB().getX()-4-(2*testIcon.getIconWidth())),
				(int)(layoutTurnout2.getCoordsB().getY()-4-testIcon.getIconHeight()) );
		}
		else if( layoutTurnout2Horizontal && (!layoutTurnout2ThroatLeft) && (!layoutTurnout2BUp) ) {
			setSignalHeadOnPanel(2,headName,
				(int)(layoutTurnout2.getCoordsB().getX()-4-(2*testIcon.getIconWidth())),
				(int)(layoutTurnout2.getCoordsB().getY()+4) );
		}
		else if( layoutTurnout2Vertical && layoutTurnout2ThroatUp && layoutTurnout2BLeft ) {
			setSignalHeadOnPanel(3,headName,
				(int)(layoutTurnout2.getCoordsB().getX()-4-testIcon.getIconWidth()),
				(int)(layoutTurnout2.getCoordsB().getY()+4+testIcon.getIconHeight()) );
		}
		else if( layoutTurnout2Vertical && layoutTurnout2ThroatUp && (!layoutTurnout2BLeft) ) {
			setSignalHeadOnPanel(3,headName,
				(int)(layoutTurnout2.getCoordsB().getX()+4),
				(int)(layoutTurnout2.getCoordsB().getY()+4+testIcon.getIconHeight()) );
		}
		else if( layoutTurnout2Vertical && (!layoutTurnout2ThroatUp) && layoutTurnout2BLeft ) {
			setSignalHeadOnPanel(1,headName,
				(int)(layoutTurnout2.getCoordsB().getX()-4-testIcon.getIconWidth()),
				(int)(layoutTurnout2.getCoordsB().getY()-4-(2*testIcon.getIconHeight())) );
		}
		else if( layoutTurnout2Vertical && (!layoutTurnout2ThroatUp) && (!layoutTurnout2BLeft) ) {
			setSignalHeadOnPanel(1,headName,
				(int)(layoutTurnout2.getCoordsB().getX()+4),
				(int)(layoutTurnout2.getCoordsB().getY()-4-(2*testIcon.getIconHeight())) );
		}
	}
	private void placeD1TToT(String headName) {
		if (testIcon == null)
			testIcon = signalIconEditor.getIcon(0);
		if( layoutTurnout2Horizontal && layoutTurnout2ThroatLeft && layoutTurnout2BUp) {
			setSignalHeadOnPanel(0,headName,
				(int)(layoutTurnout2.getCoordsC().getX()),
				(int)(layoutTurnout2.getCoordsC().getY()+4) );
		}
		else if( layoutTurnout2Horizontal && layoutTurnout2ThroatLeft && (!layoutTurnout2BUp) ) {
			setSignalHeadOnPanel(0,headName,
				(int)(layoutTurnout2.getCoordsC().getX()),
				(int)(layoutTurnout2.getCoordsC().getY()-4-testIcon.getIconHeight()) );
		}
		else if( layoutTurnout2Horizontal && (!layoutTurnout2ThroatLeft) && layoutTurnout2BUp ) {
			setSignalHeadOnPanel(2,headName,
				(int)(layoutTurnout2.getCoordsC().getX()-testIcon.getIconWidth()),
				(int)(layoutTurnout2.getCoordsC().getY()+4) );
		}
		else if( layoutTurnout2Horizontal && (!layoutTurnout2ThroatLeft) && (!layoutTurnout2BUp) ) {
			setSignalHeadOnPanel(2,headName,
				(int)(layoutTurnout2.getCoordsC().getX()-testIcon.getIconWidth()),
				(int)(layoutTurnout2.getCoordsC().getY()-4-testIcon.getIconHeight()) );
		}
		else if( layoutTurnout2Vertical && layoutTurnout2ThroatUp && layoutTurnout2BLeft ) {
			setSignalHeadOnPanel(3,headName,
				(int)(layoutTurnout2.getCoordsC().getX()+4),
				(int)(layoutTurnout2.getCoordsC().getY()) );
		}
		else if( layoutTurnout2Vertical && layoutTurnout2ThroatUp && (!layoutTurnout2BLeft) ) {
			setSignalHeadOnPanel(3,headName,
				(int)(layoutTurnout2.getCoordsC().getX()-4-testIcon.getIconWidth()),
				(int)(layoutTurnout2.getCoordsC().getY()) );
		}
		else if( layoutTurnout2Vertical && (!layoutTurnout2ThroatUp) && layoutTurnout2BLeft ) {
			setSignalHeadOnPanel(1,headName,
				(int)(layoutTurnout2.getCoordsC().getX()+4),
				(int)(layoutTurnout2.getCoordsC().getY()-testIcon.getIconHeight()) );
		}
		else if( layoutTurnout2Vertical && (!layoutTurnout2ThroatUp) && (!layoutTurnout2BLeft) ) {
			setSignalHeadOnPanel(1,headName,
				(int)(layoutTurnout2.getCoordsC().getX()-4-testIcon.getIconWidth()),
				(int)(layoutTurnout2.getCoordsC().getY()-testIcon.getIconHeight()) );
		}
	}
	private void placeD2TToT(String headName) {
		if (testIcon == null)
			testIcon = signalIconEditor.getIcon(0);
		if( layoutTurnout2Horizontal && layoutTurnout2ThroatLeft && layoutTurnout2BUp) {
			setSignalHeadOnPanel(0,headName,
				(int)(layoutTurnout2.getCoordsC().getX()+4+testIcon.getIconWidth()),
				(int)(layoutTurnout2.getCoordsC().getY()+4) );
		}
		else if( layoutTurnout2Horizontal && layoutTurnout2ThroatLeft && (!layoutTurnout2BUp) ) {
			setSignalHeadOnPanel(0,headName,
				(int)(layoutTurnout2.getCoordsC().getX()+4+testIcon.getIconWidth()),
				(int)(layoutTurnout2.getCoordsC().getY()-4-testIcon.getIconHeight()) );
		}
		else if( layoutTurnout2Horizontal && (!layoutTurnout2ThroatLeft) && layoutTurnout2BUp ) {
			setSignalHeadOnPanel(2,headName,
				(int)(layoutTurnout2.getCoordsC().getX()-4-(2*testIcon.getIconWidth())),
				(int)(layoutTurnout2.getCoordsC().getY()+4) );
		}
		else if( layoutTurnout2Horizontal && (!layoutTurnout2ThroatLeft) && (!layoutTurnout2BUp) ) {
			setSignalHeadOnPanel(2,headName,
				(int)(layoutTurnout2.getCoordsC().getX()-4-(2*testIcon.getIconWidth())),
				(int)(layoutTurnout2.getCoordsC().getY()-4-testIcon.getIconHeight()) );
		}
		else if( layoutTurnout2Vertical && layoutTurnout2ThroatUp && layoutTurnout2BLeft ) {
			setSignalHeadOnPanel(3,headName,
				(int)(layoutTurnout2.getCoordsC().getX()+4),
				(int)(layoutTurnout2.getCoordsC().getY()+4+testIcon.getIconHeight()) );
		}
		else if( layoutTurnout2Vertical && layoutTurnout2ThroatUp && (!layoutTurnout2BLeft) ) {
			setSignalHeadOnPanel(3,headName,
				(int)(layoutTurnout2.getCoordsC().getX()-4-testIcon.getIconWidth()),
				(int)(layoutTurnout2.getCoordsC().getY()+4+testIcon.getIconHeight()) );
		}
		else if( layoutTurnout2Vertical && (!layoutTurnout2ThroatUp) && layoutTurnout2BLeft ) {
			setSignalHeadOnPanel(1,headName,
				(int)(layoutTurnout2.getCoordsC().getX()+4),
				(int)(layoutTurnout2.getCoordsC().getY()-4-(2*testIcon.getIconHeight())) );
		}
		else if( layoutTurnout2Vertical && (!layoutTurnout2ThroatUp) && (!layoutTurnout2BLeft) ) {
			setSignalHeadOnPanel(1,headName,
				(int)(layoutTurnout2.getCoordsC().getX()-4-testIcon.getIconWidth()),
				(int)(layoutTurnout2.getCoordsC().getY()-4-(2*testIcon.getIconHeight())) );
		}
	}
	@SuppressWarnings("null")
	private void setLogicTToT(SignalHead head,TrackSegment track1,SignalHead secondHead,TrackSegment track2,
					boolean setup1, boolean setup2, boolean continuing,
						LayoutTurnout farTurnout, LayoutTurnout nearTurnout) {
		// initialize common components and ensure all is defined
		LayoutBlock connectorBlock = connectorTrack.getLayoutBlock();
		LayoutBlock nearTurnoutBlock = nearTurnout.getLayoutBlock();
		LayoutBlock farTurnoutBlock = farTurnout.getLayoutBlock();
		Sensor connectorOccupancy = null;
		if ( (connectorBlock==null) || (nearTurnoutBlock==null) || (farTurnoutBlock==null) ) {
			JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
					rb.getString("InfoMessage6"),"",JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		connectorOccupancy = connectorBlock.getOccupancySensor();
		if (connectorOccupancy==null) {
			JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
					java.text.MessageFormat.format(rb.getString("InfoMessage4"),
						new Object[]{connectorBlock.getUserName()}), 
							null,JOptionPane.INFORMATION_MESSAGE);						
			return;
		}
		// setup signal head for continuing track of far turnout (or both tracks of far turnout)
		if ( (track1==null) && setup1 ) {
			JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
					rb.getString("InfoMessage7"),"",JOptionPane.INFORMATION_MESSAGE);			
			return;
		}			
		Sensor occupancy = null;
		SignalHead nextHead = null;
		if ( (track1!=null) && setup1) {
			LayoutBlock block = track1.getLayoutBlock();
			if (block==null) {
				JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
					rb.getString("InfoMessage6"),"",JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			occupancy = block.getOccupancySensor();
			if (occupancy==null) {
				JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
					java.text.MessageFormat.format(rb.getString("InfoMessage4"),
						new Object[]{block.getUserName()}), 
							null,JOptionPane.INFORMATION_MESSAGE);						
				return;
			}
			nextHead = getNextSignalFromObject(track1,farTurnout,
										head.getSystemName(), setSignalsAtTToTFrame);
			if ( (nextHead==null) && (!reachedEndBumper()) ) {
				JOptionPane.showMessageDialog(setSignalsFrame,
					java.text.MessageFormat.format(rb.getString("InfoMessage5"),
						new Object[]{block.getUserName()}), 
							null,JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			if (secondHead!=null) {
				// this head signals only the continuing track of the far turnout
				if (!initializeBlockBossLogic(head.getSystemName())) return;
				logic.setMode(BlockBossLogic.TRAILINGMAIN);
				logic.setTurnout(farTurnout.getTurnout().getSystemName());
				logic.setSensor1(occupancy.getSystemName());
				if (occupancy!=connectorOccupancy)
					logic.setSensor2(connectorOccupancy.getSystemName());
				if (nextHead!=null) {
					logic.setWatchedSignal1(nextHead.getSystemName(),false);
				}
				if (auxSignal!=null) {
					logic.setWatchedSignal1Alt(auxSignal.getSystemName());
				}
				String nearSensorName = setupNearLogix(nearTurnout,continuing,head);
				addNearSensorToLogic(nearSensorName);
				finalizeBlockBossLogic();
			}
		}
		if ( (secondHead!=null) && !setup2 ) return;
		SignalHead savedAuxSignal = auxSignal;
		if (track2==null) {
			JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
					rb.getString("InfoMessage7"),"",JOptionPane.INFORMATION_MESSAGE);			
			return;
		}
		LayoutBlock block2 = track2.getLayoutBlock();
		if (block2==null) {
			JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
					rb.getString("InfoMessage6"),"",JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		Sensor occupancy2 = block2.getOccupancySensor();
		if (occupancy2==null) {
			JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
				java.text.MessageFormat.format(rb.getString("InfoMessage4"),
					new Object[]{block2.getUserName()}), 
						null,JOptionPane.INFORMATION_MESSAGE);						
			return;
		}
		SignalHead nextHead2 = null;
		if (secondHead!=null) {
			nextHead2 = getNextSignalFromObject(track2,
					farTurnout, secondHead.getSystemName(), setSignalsAtTToTFrame);
			if ( (nextHead2==null) && (!reachedEndBumper()) ) {
				JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
					java.text.MessageFormat.format(rb.getString("InfoMessage5"),
						new Object[]{block2.getUserName()}), 
							null,JOptionPane.INFORMATION_MESSAGE);
				return;
			}
		}
		if ( (secondHead==null) && (track1!=null) && setup1 ) {
			if (!initializeBlockBossLogic(head.getSystemName())) return;
			logic.setMode(BlockBossLogic.FACING);
			logic.setTurnout(farTurnout.getTurnout().getSystemName());
			logic.setWatchedSensor1(occupancy.getSystemName());
			logic.setWatchedSensor2(occupancy2.getSystemName());
			logic.setSensor2(connectorOccupancy.getSystemName());
			if (nextHead!=null) {
				logic.setWatchedSignal1(nextHead.getSystemName(),false);
			}
			if (savedAuxSignal!=null) {
				logic.setWatchedSignal1Alt(savedAuxSignal.getSystemName());
			}
			if (nextHead2!=null) {
				logic.setWatchedSignal2(nextHead2.getSystemName());
			}
			if (auxSignal!=null) {
				logic.setWatchedSignal2Alt(auxSignal.getSystemName());
			}		
			String nearSensorName = setupNearLogix(nearTurnout,continuing,head);
			addNearSensorToLogic(nearSensorName);
			logic.setLimitSpeed2(true);
			finalizeBlockBossLogic();
		}
		else if ( (secondHead!=null) && setup2) {
			if (!initializeBlockBossLogic(secondHead.getSystemName())) return;
			logic.setMode(BlockBossLogic.TRAILINGDIVERGING);
			logic.setTurnout(farTurnout.getTurnout().getSystemName());
			logic.setSensor1(occupancy2.getSystemName());
			if (occupancy2!=connectorOccupancy)
				logic.setSensor2(connectorOccupancy.getSystemName());
			if (nextHead2!=null) {
				logic.setWatchedSignal1(nextHead2.getSystemName(),false);
			}
			if (auxSignal!=null) {
				logic.setWatchedSignal1Alt(auxSignal.getSystemName());
			}
			String nearSensorName = setupNearLogix(nearTurnout,continuing,head);
			addNearSensorToLogic(nearSensorName);
			logic.setLimitSpeed2(true);
			finalizeBlockBossLogic();			
		}
	}
	/* 
	 * Sets up a Logix to set a sensor active if a turnout is set against
	 *      a track.  This routine creates an internal sensor for the purpose.
	 * Note: The sensor and logix are named IS or IX followed by TTT_X_HHH where 
	 *		TTT is the system name of the turnout, X is either C or T depending 
	 *      on "continuing", and HHH is the system name of the signal head. 
	 * Note: If there is any problem, a string of "" is returned, and a warning 
	 *		message is issued.
	 */
	private String setupNearLogix(LayoutTurnout nearTurnout, boolean continuing, 
					SignalHead head) {
		String turnoutName = nearTurnout.getTurnout().getSystemName();
		String namer = turnoutName+"_T_"+head.getSystemName();
		if (!continuing) namer = turnoutName+"_C_"+head.getSystemName();
		String sensorName = "IS"+namer;
		String logixName = "IX"+namer;
		Sensor sensor = InstanceManager.sensorManagerInstance().provideSensor(sensorName);
		if (sensor==null) {
			log.error("Trouble creating sensor "+sensorName+" while setting up Logix.");
			return "";
		}
		if (InstanceManager.logixManagerInstance().getBySystemName(logixName)==null) {
			// Logix does not exist, create it
			Logix x = InstanceManager.logixManagerInstance().createNewLogix(logixName,"");
			if (x==null) {
				log.error("Trouble creating logix "+logixName+" while setting up signal logic.");
				return "";
			}
			String cName = x.getSystemName()+"C1";
			Conditional c = InstanceManager.conditionalManagerInstance().
												createNewConditional(cName,"");
			if (c==null) {
				log.error("Trouble creating conditional "+cName+" while setting up Logix.");
				return "";
			}
			int type = Conditional.TYPE_TURNOUT_THROWN;
			if (!continuing) type = Conditional.TYPE_TURNOUT_CLOSED;
            ArrayList <ConditionalVariable> variableList = c.getCopyOfStateVariables();
            variableList.add(new ConditionalVariable(false, Conditional.OPERATOR_AND, 
                                                 type, turnoutName, true));
            c.setStateVariables(variableList);
            ArrayList <ConditionalAction> actionList = c.getCopyOfActions();
            actionList.add(new DefaultConditionalAction(Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE,
                                                 Conditional.ACTION_SET_SENSOR, sensorName,
                                                 Sensor.ACTIVE, ""));
            actionList.add(new DefaultConditionalAction(Conditional.ACTION_OPTION_ON_CHANGE_TO_FALSE,
                                                 Conditional.ACTION_SET_SENSOR, sensorName,
                                                 Sensor.INACTIVE, ""));
			c.setAction(actionList);										// string data
			x.addConditional(cName,-1);
			x.activateLogix();
		}
		return sensorName;
	}
	/*
	 * Adds the sensor specified to the open BlockBossLogic, provided it is not already there and 
	 *		provided there is an open slot. If 'name' is null or empty, returns without doing anything.
	 */
	private void addNearSensorToLogic(String name) {
		if ( (name==null) || name.equals("") ) return;
		// return if a sensor by this name is already present
		if ( (logic.getSensor1()!=null) && (logic.getSensor1()).equals(name) ) return;
		if ( (logic.getSensor2()!=null) && (logic.getSensor2()).equals(name) ) return;
		if ( (logic.getSensor3()!=null) && (logic.getSensor3()).equals(name) ) return;
		if ( (logic.getSensor4()!=null) && (logic.getSensor4()).equals(name) ) return;
		if ( (logic.getSensor5()!=null) && (logic.getSensor5()).equals(name) ) return;
		// add in the first available slot
		if (logic.getSensor1()==null) logic.setSensor1(name);
		else if (logic.getSensor2()==null) logic.setSensor2(name);
		else if (logic.getSensor3()==null) logic.setSensor3(name);
		else if (logic.getSensor4()==null) logic.setSensor4(name);
		else if (logic.getSensor5()==null) logic.setSensor5(name);
		else log.error("Error - could not add sensor to SSL for signal head "+logic.getDrivenSignal());
	}
	
	/**
	 * Tool to set signals at a three-way turnout, including placing the 
	 *		signal icons and setup of signal logic for each signal head
	 * <P>
	 * This tool can only be accessed from the Tools menu. There is no access
	 *		from a turnout pop-up menu.
	 * <P>
	 * This tool requires a situation where two turnouts are connected to model a 3-way
	 *		turnout, with the throat of the second turnout connected to the continuing 
	 *		leg of the first turnout by a very short track segment. The actual length of 
	 *		the track segment is not tested. If this situation is not found,
	 *		and error message is sent to the user.  
	 * <P>
	 * This tool assumes two turnouts connected with the throat of the second turnout 
	 *		connected to the continuing leg of the first turnou, as used to
	 *		represent a 3-way turnout. The turnouts may be either
	 *		left-handed, or right-handed, or any pair of these. This tool also
	 *		assumes that there are no signals between the two turnouts. Signal
	 *		heads are allowed/required at the continuing leg of the second turnout, at 
	 *		each of the diverging legs, and at the throat. At the throat, either one or 
	 *		three heads are provided for. So four or six heads will be placed.
	 * <P>
	 * This tool assumes that each of the four tracks, the continuing, the two diverging,
	 *		and the throat is contained in a different block. The two turnouts used to 
	 *		model the 3-way turnout must be in the same block. Things work best if the 
	 *		two turnouts are in the same block as the track connecting at the throat, or 
	 *		if the two turnouts are in their own separate block, either works fine.
	 * <P>
	 * This tool only places signal icons if the turnout is either mostly vertical 
	 *		or mostly horizontal. Some user adjustment may be needed.
	 */	
	
	// operational variables for Set Signals at 3-Way Turnout tool
	private JmriJFrame setSignalsAt3WayFrame = null;
	private boolean setSignalsAt3WayOpen = false;
	private JTextField turnoutANameField = new JTextField(16);
	private JTextField turnoutBNameField = new JTextField(16);
	private JTextField a13WayField = new JTextField(16);
	private JTextField a23WayField = new JTextField(16);
	private JTextField a33WayField = new JTextField(16);
	private JTextField b3WayField = new JTextField(16);
	private JTextField c3WayField = new JTextField(16);
	private JTextField d3WayField = new JTextField(16);
	private JCheckBox setA13WayHead = new JCheckBox(rb.getString("PlaceHead"));
	private JCheckBox setupA13WayLogic = new JCheckBox(rb.getString("SetLogic"));
	private JCheckBox setA23WayHead = new JCheckBox(rb.getString("PlaceHead"));
	private JCheckBox setupA23WayLogic = new JCheckBox(rb.getString("SetLogic"));
	private JCheckBox setA33WayHead = new JCheckBox(rb.getString("PlaceHead"));
	private JCheckBox setupA33WayLogic = new JCheckBox(rb.getString("SetLogic"));
	private JCheckBox setB3WayHead = new JCheckBox(rb.getString("PlaceHead"));
	private JCheckBox setupB3WayLogic = new JCheckBox(rb.getString("SetLogic"));
	private JCheckBox setC3WayHead = new JCheckBox(rb.getString("PlaceHead"));
	private JCheckBox setupC3WayLogic = new JCheckBox(rb.getString("SetLogic"));
	private JCheckBox setD3WayHead = new JCheckBox(rb.getString("PlaceHead"));
	private JCheckBox setupD3WayLogic = new JCheckBox(rb.getString("SetLogic"));
	private JButton getSaved3WaySignalHeads = null;
	private JButton change3WaySignalIcon = null;
	private JButton set3WaySignalsDone = null;
	private JButton set3WaySignalsCancel = null;
	private LayoutTurnout layoutTurnoutA = null;
	private LayoutTurnout layoutTurnoutB = null;
	private Turnout turnoutA = null;
	private Turnout turnoutB = null;
	//private TrackSegment conTrack = null;
	private SignalHead a13WayHead = null;   // saved in A1 of Turnout A - Throat - continuing
	private SignalHead a23WayHead = null;   // saved in A2 of Turnout A - Throat - diverging A (optional)
	private SignalHead a33WayHead = null;   // saved in A3 of Turnout A - Throat - diverging B (optional)
	private SignalHead b3WayHead = null;    // saved in C1 of Turnout A - at diverging A
	private SignalHead c3WayHead = null;    // saved in B1 of Turnout B - at continuing
	private SignalHead d3WayHead = null;    // saved in C1 of Turnout B - at diverging B
	private boolean	layoutTurnoutAHorizontal = false;
	private boolean	layoutTurnoutAVertical = false;
	private boolean	layoutTurnoutBHorizontal = false;
	private boolean	layoutTurnoutBVertical = false;
	private boolean	layoutTurnoutAThroatLeft = false;
	private boolean	layoutTurnoutAThroatUp = false;
	private boolean	layoutTurnoutBThroatLeft = false;
	private boolean	layoutTurnoutBThroatUp = false;
	private boolean	layoutTurnoutABUp = false;
	private boolean	layoutTurnoutABLeft = false;
	private boolean	layoutTurnoutBBUp = false;
	private boolean	layoutTurnoutBBLeft = false;

	public void set3WayFromMenu(String aName, String bName,
					MultiIconEditor theEditor, JFrame theFrame ) {
		turnoutANameField.setText(aName);
		turnoutBNameField.setText(bName);
		a13WayField.setText("");
		a23WayField.setText("");
		a33WayField.setText("");
		b3WayField.setText("");
		c3WayField.setText("");
		d3WayField.setText("");
		setSignalsAt3WayTurnout(theEditor,theFrame);
	}		
	public void setSignalsAt3WayTurnout( MultiIconEditor theEditor, JFrame theFrame ) {
		signalIconEditor = theEditor;
		signalFrame = theFrame;
		if (setSignalsAt3WayOpen) {
			setSignalsAt3WayFrame.setVisible(true);
			return;
		}
		// Initialize if needed
		if (setSignalsAt3WayFrame == null) {
            setSignalsAt3WayFrame = new JmriJFrame( rb.getString("SignalsAt3WayTurnout"), false, true );
            setSignalsAt3WayFrame.addHelpMenu("package.jmri.jmrit.display.SetSignalsAt3WayTurnout", true);
            setSignalsAt3WayFrame.setLocation(70,30);
            Container theContentPane = setSignalsAt3WayFrame.getContentPane();        
            theContentPane.setLayout(new BoxLayout(theContentPane, BoxLayout.Y_AXIS));
			JPanel panel1 = new JPanel();
            panel1.setLayout(new FlowLayout());
			JLabel turnoutANameLabel = new JLabel( rb.getString("TurnoutAName") );
			panel1.add(turnoutANameLabel);
			panel1.add(turnoutANameField);
			turnoutANameField.setToolTipText(rb.getString("SignalsTurnoutNameHint"));
            theContentPane.add(panel1);
			JPanel panel11 = new JPanel();
            panel11.setLayout(new FlowLayout());
			JLabel turnoutBNameLabel = new JLabel( rb.getString("TurnoutBName") );
			panel11.add(turnoutBNameLabel);
			panel11.add(turnoutBNameField);
			turnoutBNameField.setToolTipText(rb.getString("SignalsTurnoutNameHint"));
			theContentPane.add(panel11);
			theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));
			// Provide for retrieval of names of previously saved signal heads
            JPanel panel2 = new JPanel();
			JLabel shTitle = new JLabel(rb.getString("SignalHeads"));
			panel2.add(shTitle);			
			panel2.add(new JLabel("     "));
            panel2.add(getSaved3WaySignalHeads = new JButton(rb.getString("GetSaved")));
            getSaved3WaySignalHeads.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
							getSaved3WaySignals(e);
					}
				});
            getSaved3WaySignalHeads.setToolTipText( rb.getString("GetSavedHint") );
			theContentPane.add(panel2);
			theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));
			// Signal heads located at turnout A
			JPanel panel20	= new JPanel();		
            panel20.setLayout(new FlowLayout());
			panel20.add(new JLabel(rb.getString("SignalLocated")+" "+rb.getString("Turnout")+" A "));
			theContentPane.add(panel20);
			JPanel panel21 = new JPanel();
            panel21.setLayout(new FlowLayout());
			panel21.add(new JLabel("    "+rb.getString("Throat")+" - "+
											rb.getString("Continuing")+" : "));
			panel21.add(a13WayField);
			a13WayField.setToolTipText(rb.getString("SignalHeadNameHint"));
			theContentPane.add(panel21);
            JPanel panel22 = new JPanel();
            panel22.setLayout(new FlowLayout());
			panel22.add(new JLabel("   "));
			panel22.add(setA13WayHead);
			setA13WayHead.setToolTipText(rb.getString("PlaceHeadHint"));
			panel22.add(new JLabel("  "));
			panel22.add(setupA13WayLogic);
			setupA13WayLogic.setToolTipText(rb.getString("SetLogicHint"));
			theContentPane.add(panel22);
			JPanel panel23 = new JPanel();
            panel23.setLayout(new FlowLayout());
			panel23.add(new JLabel("    "+rb.getString("Throat")+" - "+
											rb.getString("DivergingA")+" : "));
			panel23.add(a23WayField);
			a23WayField.setToolTipText(rb.getString("SignalHeadNameHint"));
			theContentPane.add(panel23);
            JPanel panel24 = new JPanel();
            panel24.setLayout(new FlowLayout());
			panel24.add(new JLabel("   "));
			panel24.add(setA23WayHead);
			setA23WayHead.setToolTipText(rb.getString("PlaceHeadHint"));
			panel24.add(new JLabel("  "));
			panel24.add(setupA23WayLogic);
			setupA23WayLogic.setToolTipText(rb.getString("SetLogicHint"));
			theContentPane.add(panel24);
			JPanel panel25 = new JPanel();
            panel25.setLayout(new FlowLayout());
			panel25.add(new JLabel("    "+rb.getString("Throat")+" - "+
											rb.getString("DivergingB")+" : "));
			panel25.add(a33WayField);
			a33WayField.setToolTipText(rb.getString("SignalHeadNameHint"));
			theContentPane.add(panel25);
            JPanel panel26 = new JPanel();
            panel26.setLayout(new FlowLayout());
			panel26.add(new JLabel("   "));
			panel26.add(setA33WayHead);
			setA33WayHead.setToolTipText(rb.getString("PlaceHeadHint"));
			panel26.add(new JLabel("  "));
			panel26.add(setupA33WayLogic);
			setupA33WayLogic.setToolTipText(rb.getString("SetLogicHint"));
			theContentPane.add(panel26);
			JPanel panel31 = new JPanel();
            panel31.setLayout(new FlowLayout());
			panel31.add(new JLabel("        "+rb.getString("DivergingA")+" : "));
			panel31.add(b3WayField);
			b3WayField.setToolTipText(rb.getString("SignalHeadNameHint"));
			theContentPane.add(panel31);
            JPanel panel32 = new JPanel();
            panel32.setLayout(new FlowLayout());
			panel32.add(new JLabel("   "));
			panel32.add(setB3WayHead);
			setB3WayHead.setToolTipText(rb.getString("PlaceHeadHint"));
			panel32.add(new JLabel("  "));
			panel32.add(setupB3WayLogic);
			setupB3WayLogic.setToolTipText(rb.getString("SetLogicHint"));
			theContentPane.add(panel32);
			theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));
			// Signal heads located at turnout B
			JPanel panel40 = new JPanel();		
            panel40.setLayout(new FlowLayout());
			panel40.add(new JLabel(rb.getString("SignalLocated")+" "+rb.getString("Turnout")+" B "));
			theContentPane.add(panel40);
			JPanel panel41 = new JPanel();
            panel41.setLayout(new FlowLayout());
			panel41.add(new JLabel("        "+rb.getString("Continuing")+" : "));
			panel41.add(c3WayField);
			c3WayField.setToolTipText(rb.getString("SignalHeadNameHint"));
			theContentPane.add(panel41);
            JPanel panel42 = new JPanel();
            panel42.setLayout(new FlowLayout());
			panel42.add(new JLabel("   "));
			panel42.add(setC3WayHead);
			setC3WayHead.setToolTipText(rb.getString("PlaceHeadHint"));
			panel42.add(new JLabel("  "));
			panel42.add(setupC3WayLogic);
			setupC3WayLogic.setToolTipText(rb.getString("SetLogicHint"));
			theContentPane.add(panel42);
			JPanel panel43 = new JPanel();
            panel43.setLayout(new FlowLayout());
			panel43.add(new JLabel("        "+rb.getString("DivergingB")+" : "));
			panel43.add(d3WayField);
			d3WayField.setToolTipText(rb.getString("SignalHeadNameHint"));
			theContentPane.add(panel43);
            JPanel panel44 = new JPanel();
            panel44.setLayout(new FlowLayout());
			panel44.add(new JLabel("   "));
			panel44.add(setD3WayHead);
			setD3WayHead.setToolTipText(rb.getString("PlaceHeadHint"));
			panel44.add(new JLabel("  "));
			panel44.add(setupD3WayLogic);
			setupD3WayLogic.setToolTipText(rb.getString("SetLogicHint"));
			theContentPane.add(panel44);
			theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));
			// buttons
            JPanel panel6 = new JPanel();
            panel6.setLayout(new FlowLayout());
            panel6.add(change3WaySignalIcon = new JButton(rb.getString("ChangeSignalIcon")));
            change3WaySignalIcon.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						signalFrame.setVisible(true);
					}
				});
            change3WaySignalIcon.setToolTipText( rb.getString("ChangeSignalIconHint") );
			panel6.add(new JLabel("  "));
            panel6.add(set3WaySignalsDone = new JButton(rb.getString("Done")));
            set3WaySignalsDone.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    set3WaySignalsDonePressed(e);
                }
            });
            set3WaySignalsDone.setToolTipText( rb.getString("SignalDoneHint") );
            panel6.add(set3WaySignalsCancel = new JButton(rb.getString("Cancel")));
            set3WaySignalsCancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    set3WaySignalsCancelPressed(e);
                }
            });
            set3WaySignalsCancel.setToolTipText( rb.getString("CancelHint") );
            theContentPane.add(panel6);
			setSignalsAt3WayFrame.addWindowListener(new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent e) {
					set3WaySignalsCancelPressed(null);
				}
			});
		}
        setSignalsAt3WayFrame.pack();
        setSignalsAt3WayFrame.setVisible(true);		
		setSignalsAt3WayOpen = true;
	}	
	private void getSaved3WaySignals (ActionEvent a) {
		if ( !get3WayTurnoutInformation() ) return;
		a13WayField.setText(layoutTurnoutA.getSignalA1Name());	
		a23WayField.setText(layoutTurnoutA.getSignalA2Name());
		a33WayField.setText(layoutTurnoutA.getSignalA3Name());
		b3WayField.setText(layoutTurnoutA.getSignalC1Name());
		c3WayField.setText(layoutTurnoutB.getSignalB1Name());	
		d3WayField.setText(layoutTurnoutB.getSignalC1Name());	
	}
	private void set3WaySignalsCancelPressed (ActionEvent a) {
		setSignalsAt3WayOpen = false;
		setSignalsAt3WayFrame.setVisible(false);
	}
	private boolean get3WayTurnoutInformation() {
		int type = 0;
		Object connect = null;
		String str = "";
		turnoutA = null;
		turnoutB = null;
		layoutTurnoutA = null;
		layoutTurnoutB = null;
		str = turnoutANameField.getText().trim();
		if ( (str==null) || (str.equals("")) ) {
			// turnout A not entered, test turnout B
			str = turnoutBNameField.getText().trim();
			if ( (str==null) || (str.equals("")) ) {
				// no entries in turnout fields 
				JOptionPane.showMessageDialog(setSignalsAt3WayFrame,rb.getString("SignalsError1"),
									rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return false;
			}
			turnoutB = jmri.InstanceManager.turnoutManagerInstance().getTurnout(str);
			if (turnoutB==null) {
				JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError2"),
						new Object[]{str}), rb.getString("Error"),
							JOptionPane.ERROR_MESSAGE);
				return false ;
			}
			if ( (turnoutB.getUserName()==null) || (turnoutB.getUserName().equals("")) ||
									!turnoutB.getUserName().equals(str) ) {
				str = str.toUpperCase();
				turnoutBNameField.setText(str);
			}
			layoutTurnoutB = getLayoutTurnoutFromTurnout(turnoutB,false,str,setSignalsAt3WayFrame);
			if (layoutTurnoutB==null)
				return false;
			// have turnout B and layout turnout B - look for turnout A
			connectorTrack = (TrackSegment)layoutTurnoutB.getConnectA();
			if (connectorTrack == null) {
				// Inform user of error, and terminate
				JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
						rb.getString("SignalsError19"),
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return false;
			}
			type = connectorTrack.getType1();
			connect = connectorTrack.getConnect1();
			if (connect == layoutTurnoutB) {
				type = connectorTrack.getType2();
				connect = connectorTrack.getConnect2();
			}
			if ( (type != LayoutEditor.TURNOUT_B) || (connect==null) ) {
				// Not two turnouts connected as required by a single Track Segment
				// Inform user of error and terminate
				JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
						rb.getString("SignalsError19"),
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return false;
			}
			layoutTurnoutA = (LayoutTurnout)connect;
			turnoutA = layoutTurnoutA.getTurnout();
			if (turnoutA==null) {
				JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
						rb.getString("SignalsError19"),
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return false;
			}
			turnoutANameField.setText(layoutTurnoutA.getTurnoutName());
		}
		else {
			// something was entered in the turnout A field
			turnoutA = jmri.InstanceManager.turnoutManagerInstance().getTurnout(str);
			if (turnoutA==null) {
				JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError2"),
						new Object[]{str}), rb.getString("Error"),
							JOptionPane.ERROR_MESSAGE);
				return false ;
			}
			if ( (turnoutA.getUserName()==null) || (turnoutA.getUserName().equals("")) ||
									!turnoutA.getUserName().equals(str) ) {
				str = str.toUpperCase();
				turnoutANameField.setText(str);
			}
			// have turnout A - get corresponding layoutTurnout
			layoutTurnoutA = getLayoutTurnoutFromTurnout(turnoutA,false,str,setSignalsAt3WayFrame);
			if (layoutTurnoutA == null)
				return false;
			turnoutANameField.setText(str);
			// have turnout A and layout turnout A - was something entered for turnout B
			str = turnoutBNameField.getText().trim();
			if ( (str==null) || (str.equals("")) ) {
				// no entry for turnout B
				connectorTrack = (TrackSegment)layoutTurnoutA.getConnectB();
				if (connectorTrack == null) {
					// Inform user of error, and terminate
					JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
							rb.getString("SignalsError19"),
								rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
					return false;
				}
				type = connectorTrack.getType1();
				connect = connectorTrack.getConnect1();
				if (connect == layoutTurnoutA) {
					type = connectorTrack.getType2();
					connect = connectorTrack.getConnect2();
				}
				if ( (type != LayoutEditor.TURNOUT_A) || (connect==null) ) {
					// Not two turnouts connected with the throat of B connected to the continuing of A
					//    by a single Track Segment.  Inform user of error and terminat.e
					JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
							rb.getString("SignalsError19"),
								rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
					return false;
				}
				layoutTurnoutB = (LayoutTurnout)connect;
				turnoutB = layoutTurnoutB.getTurnout();
				if (turnoutB==null) {
					JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
							rb.getString("SignalsError19"),
								rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
					return false;
				}
				turnoutBNameField.setText(layoutTurnoutB.getTurnoutName());
			}
			else {
				// turnout B entered also
				turnoutB = jmri.InstanceManager.turnoutManagerInstance().getTurnout(str);
				if (turnoutB==null) {
					JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
						java.text.MessageFormat.format(rb.getString("SignalsError2"),
								new Object[]{str}), rb.getString("Error"),
									JOptionPane.ERROR_MESSAGE);
					return false ;
				}
				if ( (turnoutB.getUserName()==null) || (turnoutB.getUserName().equals("")) ||
									!turnoutB.getUserName().equals(str) ) {
					str = str.toUpperCase();
					turnoutBNameField.setText(str);
				}
				layoutTurnoutB = getLayoutTurnoutFromTurnout(turnoutB,false,str,setSignalsAt3WayFrame);
				if (layoutTurnoutB==null)
					return false;
				turnoutBNameField.setText(str);
				// check that layout turnout A and layout turnout B are connected as required 
				if (layoutTurnoutA.getConnectB()!=layoutTurnoutB.getConnectA()) {
					// Not two turnouts connected as required by a single Track Segment
					// Inform user of error and terminate
					JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
							rb.getString("SignalsError19"),
								rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
					return false;
				}
				connectorTrack = (TrackSegment)layoutTurnoutA.getConnectB();
			}
		}
		// have both turnouts, correctly connected - complete initialization 
		layoutTurnoutAHorizontal = false;
		layoutTurnoutAVertical = false;
		layoutTurnoutBThroatLeft = false;
		layoutTurnoutBVertical = false;
		layoutTurnoutAThroatLeft = false;
		layoutTurnoutAThroatUp = false;
		layoutTurnoutBThroatLeft = false;
		layoutTurnoutBThroatUp = false;
		layoutTurnoutABUp = false;
		layoutTurnoutABLeft = false;
		layoutTurnoutBBUp = false;
		layoutTurnoutBBLeft = false;
		double delX = layoutTurnoutA.getCoordsA().getX() - layoutTurnoutA.getCoordsB().getX();
		double delY = layoutTurnoutA.getCoordsA().getY() - layoutTurnoutA.getCoordsB().getY();
		if (Math.abs(delX) > 2.0*Math.abs(delY)) {
			layoutTurnoutAHorizontal = true;
			if (delX < 0.0) layoutTurnoutAThroatLeft = true;
			if (layoutTurnoutA.getCoordsB().getY() < layoutTurnoutA.getCoordsC().getY())
				layoutTurnoutABUp = true;
		}
		else if (Math.abs(delY) > 2.0*Math.abs(delX)) { 
			layoutTurnoutAVertical = true;
			if (delY <0.0) layoutTurnoutAThroatUp = true;
			if (layoutTurnoutA.getCoordsB().getX() < layoutTurnoutA.getCoordsC().getX())
				layoutTurnoutABLeft = true;
		}
		delX = layoutTurnoutB.getCoordsA().getX() - layoutTurnoutB.getCoordsB().getX();
		delY = layoutTurnoutB.getCoordsA().getY() - layoutTurnoutB.getCoordsB().getY();
		if (Math.abs(delX) > 2.0*Math.abs(delY)) {
			layoutTurnoutBHorizontal = true;
			if (delX < 0.0) layoutTurnoutBThroatLeft = true;
			if (layoutTurnoutB.getCoordsB().getY() < layoutTurnoutB.getCoordsC().getY())
				layoutTurnoutBBUp = true;
		}
		else if (Math.abs(delY) > 2.0*Math.abs(delX)) { 
			layoutTurnoutBVertical = true;
			if (delY <0.0) layoutTurnoutBThroatUp = true;
			if (layoutTurnoutB.getCoordsB().getX() < layoutTurnoutB.getCoordsC().getX())
				layoutTurnoutBBLeft = true;
		}
		return true;
	}
	private void set3WaySignalsDonePressed (ActionEvent a) {
		// process turnout names
		if ( !get3WayTurnoutInformation() ) return;
		// process signal head names
		if ( !get3WaySignalHeadInformation() ) return;
		// place signals as requested at turnout A
		if (setA13WayHead.isSelected()) {
			if (isHeadOnPanel(a13WayHead) &&
				(a13WayHead!=getHeadFromName(layoutTurnoutA.getSignalA1Name()))) {
				JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError6"),
						new Object[]{a13WayField.getText().trim()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else if ( (!layoutTurnoutAHorizontal) && (!layoutTurnoutAVertical) ) {
				JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
					rb.getString("InfoMessage2"),"",JOptionPane.INFORMATION_MESSAGE);
				if (a13WayHead!=getHeadFromName(layoutTurnoutA.getSignalA1Name())) {				
					removeSignalHeadFromPanel(layoutTurnoutA.getSignalA1Name());
					removeAssignment(a13WayHead);
					layoutTurnoutA.setSignalA1Name(a13WayField.getText().trim());
				}
			}				
			else {
				removeSignalHeadFromPanel(layoutTurnoutA.getSignalA1Name());
				place3WayThroatContinuing();
				removeAssignment(a13WayHead);
				layoutTurnoutA.setSignalA1Name(a13WayField.getText().trim());
				needRedraw = true;
			}		
		}
		else {
			int assigned = isHeadAssignedHere(a13WayHead,layoutTurnoutA);
			if (assigned == NONE) {
				if ( isHeadOnPanel(a13WayHead) && 
									isHeadAssignedAnywhere(a13WayHead) ) {
					JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
						java.text.MessageFormat.format(rb.getString("SignalsError8"),
							new Object[]{a13WayField.getText().trim()}), 
								rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
					return;
				}		
				else {
					removeSignalHeadFromPanel(layoutTurnoutA.getSignalA1Name());
					removeAssignment(a13WayHead);
					layoutTurnoutA.setSignalA1Name(a13WayField.getText().trim());
				}
			}
			else if (assigned!=A1) {
// need to figure out what to do in this case.			
			}
		}
		if ( (setA23WayHead.isSelected()) && (a23WayHead!=null) ) {
			if (isHeadOnPanel(a23WayHead) &&
				(a23WayHead!=getHeadFromName(layoutTurnoutA.getSignalA2Name()))) {
				JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError6"),
						new Object[]{a23WayField.getText().trim()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else if ( (!layoutTurnoutAHorizontal) && (!layoutTurnoutAVertical) ) {
				JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
					rb.getString("InfoMessage2"),"",JOptionPane.INFORMATION_MESSAGE);
				if (a23WayHead!=getHeadFromName(layoutTurnoutA.getSignalA2Name())) {				
					removeSignalHeadFromPanel(layoutTurnoutA.getSignalA2Name());
					removeAssignment(a23WayHead);
					layoutTurnoutA.setSignalA2Name(a23WayField.getText().trim());
				}
			}				
			else {
				removeSignalHeadFromPanel(layoutTurnoutA.getSignalA2Name());
				place3WayThroatDivergingA();
				removeAssignment(a23WayHead);
				layoutTurnoutA.setSignalA2Name(a23WayField.getText().trim());
				needRedraw = true;
			}		
		}
		else if (a23WayHead!=null) {
			int assigned = isHeadAssignedHere(a23WayHead,layoutTurnoutA);
			if (assigned == NONE) {
				if ( isHeadOnPanel(a23WayHead) && 
									isHeadAssignedAnywhere(a23WayHead) ) {
					JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
						java.text.MessageFormat.format(rb.getString("SignalsError8"),
							new Object[]{a23WayField.getText().trim()}), 
								rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
					return;
				}		
				else {
					removeSignalHeadFromPanel(layoutTurnoutA.getSignalA2Name());
					removeAssignment(a23WayHead);
					layoutTurnoutA.setSignalA2Name(a23WayField.getText().trim());
				}
			}
			else if (assigned!=A2) {
// need to figure out what to do in this case.			
			}
		}
		else if (a23WayHead==null) {
			removeSignalHeadFromPanel(layoutTurnoutA.getSignalA2Name());
			layoutTurnoutA.setSignalA2Name("");
		}
		if ( (setA33WayHead.isSelected()) && (a33WayHead!=null) ) {
			if (isHeadOnPanel(a33WayHead) &&
				(a33WayHead!=getHeadFromName(layoutTurnoutA.getSignalA3Name()))) {
				JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError6"),
						new Object[]{a33WayField.getText().trim()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else if ( (!layoutTurnoutAHorizontal) && (!layoutTurnoutAVertical) ) {
				JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
					rb.getString("InfoMessage2"),"",JOptionPane.INFORMATION_MESSAGE);
				if (a33WayHead!=getHeadFromName(layoutTurnoutA.getSignalA3Name())) {				
					removeSignalHeadFromPanel(layoutTurnoutA.getSignalA3Name());
					removeAssignment(a33WayHead);
					layoutTurnoutA.setSignalA3Name(a33WayField.getText().trim());
				}
			}				
			else {
				removeSignalHeadFromPanel(layoutTurnoutA.getSignalA3Name());
				place3WayThroatDivergingB();
				removeAssignment(a33WayHead);
				layoutTurnoutA.setSignalA3Name(a33WayField.getText().trim());
				needRedraw = true;
			}		
		}
		else if (a33WayHead!=null) {
			int assigned = isHeadAssignedHere(a33WayHead,layoutTurnoutA);
			if (assigned == NONE) {
				if ( isHeadOnPanel(a33WayHead) && 
									isHeadAssignedAnywhere(a33WayHead) ) {
					JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
						java.text.MessageFormat.format(rb.getString("SignalsError8"),
							new Object[]{a33WayField.getText().trim()}), 
								rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
					return;
				}		
				else {
					removeSignalHeadFromPanel(layoutTurnoutA.getSignalA3Name());
					removeAssignment(a33WayHead);
					layoutTurnoutA.setSignalA3Name(a33WayField.getText().trim());
				}
			}
			else if (assigned!=A3) {
// need to figure out what to do in this case.			
			}
		}
		else if (a33WayHead==null) {
			removeSignalHeadFromPanel(layoutTurnoutA.getSignalA3Name());
			layoutTurnoutA.setSignalA3Name("");
		}
		if (setB3WayHead.isSelected()) {
			if (isHeadOnPanel(b3WayHead) &&
				(b3WayHead!=getHeadFromName(layoutTurnoutA.getSignalC1Name()))) {
				JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError6"),
						new Object[]{b3WayField.getText().trim()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else if ( (!layoutTurnoutAHorizontal) && (!layoutTurnoutAVertical) ) {
				JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
					rb.getString("InfoMessage2"),"",JOptionPane.INFORMATION_MESSAGE);
				if (b3WayHead!=getHeadFromName(layoutTurnoutA.getSignalB1Name())) {				
					removeSignalHeadFromPanel(layoutTurnoutA.getSignalC1Name());
					removeAssignment(b3WayHead);
					layoutTurnoutA.setSignalC1Name(b3WayField.getText().trim());
				}
			}				
			else {
				removeSignalHeadFromPanel(layoutTurnoutA.getSignalC1Name());
				place3WayDivergingA();
				removeAssignment(b3WayHead);
				layoutTurnoutA.setSignalC1Name(b3WayField.getText().trim());
				needRedraw = true;
			}		
		}
		else {
			int assigned = isHeadAssignedHere(b3WayHead,layoutTurnoutA);
			if (assigned == NONE) {
				if ( isHeadOnPanel(b3WayHead) && 
									isHeadAssignedAnywhere(b3WayHead) ) {
					JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
						java.text.MessageFormat.format(rb.getString("SignalsError8"),
							new Object[]{b3WayField.getText().trim()}), 
								rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
					return;
				}		
				else {
					removeSignalHeadFromPanel(layoutTurnoutA.getSignalC1Name());
					removeAssignment(b3WayHead);
					layoutTurnoutA.setSignalC1Name(b3WayField.getText().trim());
				}
			}
			else if (assigned!=A1) {
// need to figure out what to do in this case.			
			}
		}
		// place signals as requested at Turnout B
		if (setC3WayHead.isSelected()) {
			if (isHeadOnPanel(c3WayHead) && 
				(c3WayHead!=getHeadFromName(layoutTurnoutB.getSignalB1Name()))) {
				JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError6"),
						new Object[]{c3WayField.getText().trim()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else if ( (!layoutTurnoutBHorizontal) && (!layoutTurnoutBVertical) ) {
				JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
					rb.getString("InfoMessage2"),"",JOptionPane.INFORMATION_MESSAGE);
				if (c3WayHead!=getHeadFromName(layoutTurnoutB.getSignalB1Name())) {
					removeSignalHeadFromPanel(layoutTurnoutB.getSignalB1Name());
					removeAssignment(c3WayHead);
					layoutTurnoutB.setSignalB1Name(c3WayField.getText().trim());
				}
			}				
			else {
				removeSignalHeadFromPanel(layoutTurnoutB.getSignalB1Name());
				place3WayContinuing();
				removeAssignment(c3WayHead);
				layoutTurnoutB.setSignalB1Name(c3WayField.getText().trim());
				needRedraw = true;
			}		
		}
		else {
			int assigned = isHeadAssignedHere(c3WayHead,layoutTurnoutB);
			if (assigned == NONE) {
				if (isHeadOnPanel(c3WayHead)  && 
									isHeadAssignedAnywhere(c3WayHead) ) {
					JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
						java.text.MessageFormat.format(rb.getString("SignalsError8"),
							new Object[]{c3WayField.getText().trim()}), 
								rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
					return;
				}		
				else {
					removeSignalHeadFromPanel(layoutTurnoutB.getSignalB1Name());
					removeAssignment(c3WayHead);
					layoutTurnoutB.setSignalB1Name(c3WayField.getText().trim());
				}
			}
			else if (assigned!=B1) {
// need to figure out what to do in this case.			
			}
		}
		if (setD3WayHead.isSelected()) {
			if (isHeadOnPanel(d3WayHead) && 
				(d3WayHead!=getHeadFromName(layoutTurnoutB.getSignalC1Name()))) {
				JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError6"),
						new Object[]{d3WayField.getText().trim()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else if ( (!layoutTurnoutBHorizontal) && (!layoutTurnoutBVertical) ) {
				JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
					rb.getString("InfoMessage2"),"",JOptionPane.INFORMATION_MESSAGE);
				if (d3WayHead!=getHeadFromName(layoutTurnoutB.getSignalC1Name())) {
					removeSignalHeadFromPanel(layoutTurnoutB.getSignalC1Name());
					removeAssignment(d3WayHead);
					layoutTurnoutB.setSignalC1Name(d3WayField.getText().trim());
				}
			}				
			else {
				removeSignalHeadFromPanel(layoutTurnoutB.getSignalC1Name());
				place3WayDivergingB();
				removeAssignment(d3WayHead);
				layoutTurnoutB.setSignalC1Name(d3WayField.getText().trim());
				needRedraw = true;
			}		
		}
		else {
			int assigned = isHeadAssignedHere(d3WayHead,layoutTurnoutB);
			if (assigned == NONE) {
				if (isHeadOnPanel(d3WayHead) && 
									isHeadAssignedAnywhere(d3WayHead) ) {
					JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
						java.text.MessageFormat.format(rb.getString("SignalsError8"),
							new Object[]{d3WayField.getText().trim()}), 
								rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
					return;
				}		
				else {
					removeSignalHeadFromPanel(layoutTurnoutB.getSignalC1Name());
					removeAssignment(d3WayHead);
					layoutTurnoutB.setSignalC1Name(d3WayField.getText().trim());
				}
			}
			else if (assigned!=C1) {
// need to figure out what to do in this case.			
			}
		}
		// setup Logic if requested and enough information is available
		if (setupA13WayLogic.isSelected()) {
			set3WayLogicThroatContinuing();
		}
		if ( (a23WayHead!=null) && setupA23WayLogic.isSelected() ) {
			set3WayLogicThroatDivergingA();
		}
		if ( (a33WayHead!=null) && setupA33WayLogic.isSelected() ) {
			set3WayLogicThroatDivergingB();
		}
		if (setupB3WayLogic.isSelected()) {
			set3WayLogicDivergingA();
		}
		if (setupC3WayLogic.isSelected()) {
			set3WayLogicContinuing();
		}
		if (setupD3WayLogic.isSelected()) {
			set3WayLogicDivergingB();
		}
		// link the two turnouts
		layoutTurnoutA.setLinkedTurnoutName(turnoutBNameField.getText().trim());
		layoutTurnoutA.setLinkType(LayoutTurnout.FIRST_3_WAY);		
		layoutTurnoutB.setLinkedTurnoutName(turnoutANameField.getText().trim());
		layoutTurnoutB.setLinkType(LayoutTurnout.SECOND_3_WAY);		
		// finish up
		setSignalsAt3WayOpen = false;
		setSignalsAt3WayFrame.setVisible(false);
		if (needRedraw) {
			layoutEditor.redrawPanel();
			needRedraw = false;
			layoutEditor.setDirty();
		}
	}
	private boolean get3WaySignalHeadInformation() {
		a13WayHead = getSignalHeadFromEntry(a13WayField,true,setSignalsAt3WayFrame);
		if (a13WayHead==null) return false;
		a23WayHead = getSignalHeadFromEntry(a23WayField,false,setSignalsAt3WayFrame);
		a33WayHead = getSignalHeadFromEntry(a33WayField,false,setSignalsAt3WayFrame);
		if ( ((a23WayHead==null) && (a33WayHead!=null)) || ((a33WayHead==null) && 
						(a23WayHead!=null)) ) return false;
		b3WayHead = getSignalHeadFromEntry(b3WayField,true,setSignalsAt3WayFrame);
		if (b3WayHead==null) return false;
		c3WayHead = getSignalHeadFromEntry(c3WayField,true,setSignalsAt3WayFrame);
		if (c3WayHead==null) return false;
		d3WayHead = getSignalHeadFromEntry(d3WayField,true,setSignalsAt3WayFrame);
		if (d3WayHead==null) return false;
		return true;
	}
	private void place3WayThroatContinuing() {
		if (testIcon == null)
			testIcon = signalIconEditor.getIcon(0);
		if( layoutTurnoutAHorizontal && layoutTurnoutAThroatLeft ) {
			setSignalHeadOnPanel(2,a13WayField.getText().trim(),
				(int)(layoutTurnoutA.getCoordsA().getX()-testIcon.getIconWidth()),
				(int)(layoutTurnoutA.getCoordsA().getY()+4) );
		}
		else if( layoutTurnoutAHorizontal && (!layoutTurnoutAThroatLeft) ) {
			setSignalHeadOnPanel(0,a13WayField.getText().trim(),
				(int)(layoutTurnoutA.getCoordsA().getX()),
				(int)(layoutTurnoutA.getCoordsA().getY()-4-testIcon.getIconHeight()) );
		}
		else if( layoutTurnoutAVertical && layoutTurnoutAThroatUp ) {
			setSignalHeadOnPanel(1,a13WayField.getText().trim(),
				(int)(layoutTurnoutA.getCoordsA().getX()-4-testIcon.getIconWidth()),
				(int)(layoutTurnoutA.getCoordsA().getY()-testIcon.getIconHeight()) );
		}
		else if( layoutTurnoutAVertical && (!layoutTurnoutAThroatUp) ) {
			setSignalHeadOnPanel(3,a13WayField.getText().trim(),
				(int)(layoutTurnoutA.getCoordsA().getX()+4),
				(int)(layoutTurnoutA.getCoordsA().getY()) );
		}
	}
	private void place3WayThroatDivergingA() {
		if (testIcon == null)
			testIcon = signalIconEditor.getIcon(0);
		if( layoutTurnoutAHorizontal && layoutTurnoutAThroatLeft ) {
			setSignalHeadOnPanel(2,a23WayField.getText().trim(),
				(int)(layoutTurnoutA.getCoordsA().getX()-(2*testIcon.getIconWidth())),
				(int)(layoutTurnoutA.getCoordsA().getY()+4) );
		}
		else if( layoutTurnoutAHorizontal && (!layoutTurnoutAThroatLeft) ) {
			setSignalHeadOnPanel(0,a23WayField.getText().trim(),
				(int)(layoutTurnoutA.getCoordsA().getX()+4+testIcon.getIconWidth()),
				(int)(layoutTurnoutA.getCoordsA().getY()-4-testIcon.getIconHeight()));
		}
		else if( layoutTurnoutAVertical && layoutTurnoutAThroatUp ) {
			setSignalHeadOnPanel(1,a23WayField.getText().trim(),
				(int)(layoutTurnoutA.getCoordsA().getX()-4-testIcon.getIconWidth()),
				(int)(layoutTurnoutA.getCoordsA().getY()-4-(2*testIcon.getIconHeight())));
		}
		else if( layoutTurnoutAVertical && (!layoutTurnoutAThroatUp) ) {
			setSignalHeadOnPanel(3,a23WayField.getText().trim(),
				(int)(layoutTurnoutA.getCoordsA().getX()+4),
				(int)(layoutTurnoutA.getCoordsA().getY()+4+testIcon.getIconHeight()));
		}
	}
	private void place3WayThroatDivergingB() {
		if (testIcon == null)
			testIcon = signalIconEditor.getIcon(0);
		if( layoutTurnoutAHorizontal && layoutTurnoutAThroatLeft ) {
			setSignalHeadOnPanel(2,a33WayField.getText().trim(),
				(int)(layoutTurnoutA.getCoordsA().getX()-(3*testIcon.getIconWidth())),
				(int)(layoutTurnoutA.getCoordsA().getY()+4) );
		}
		else if( layoutTurnoutAHorizontal && (!layoutTurnoutAThroatLeft) ) {
			setSignalHeadOnPanel(0,a33WayField.getText().trim(),
				(int)(layoutTurnoutA.getCoordsA().getX()+8+(2*testIcon.getIconWidth())),
				(int)(layoutTurnoutA.getCoordsA().getY()-4-testIcon.getIconHeight()));
		}
		else if( layoutTurnoutAVertical && layoutTurnoutAThroatUp ) {
			setSignalHeadOnPanel(1,a33WayField.getText().trim(),
				(int)(layoutTurnoutA.getCoordsA().getX()-4-testIcon.getIconWidth()),
				(int)(layoutTurnoutA.getCoordsA().getY()-4-(3*testIcon.getIconHeight())));
		}
		else if( layoutTurnoutAVertical && (!layoutTurnoutAThroatUp) ) {
			setSignalHeadOnPanel(3,a33WayField.getText().trim(),
				(int)(layoutTurnoutA.getCoordsA().getX()+4),
				(int)(layoutTurnoutA.getCoordsA().getY()+8+(2*testIcon.getIconHeight())));
		}
	}
	private void place3WayDivergingA() {
		if (testIcon == null)
			testIcon = signalIconEditor.getIcon(0);
		if( layoutTurnoutAHorizontal && layoutTurnoutAThroatLeft && layoutTurnoutABUp) {
			setSignalHeadOnPanel(0,b3WayField.getText().trim(),
				(int)(layoutTurnoutA.getCoordsC().getX()),
				(int)(layoutTurnoutA.getCoordsC().getY()+4) );
		}
		else if( layoutTurnoutAHorizontal && layoutTurnoutAThroatLeft && (!layoutTurnoutABUp) ) {
			setSignalHeadOnPanel(0,b3WayField.getText().trim(),
				(int)(layoutTurnoutA.getCoordsC().getX()),
				(int)(layoutTurnoutA.getCoordsC().getY()-4-testIcon.getIconHeight()) );
		}
		else if( layoutTurnoutAHorizontal && (!layoutTurnoutAThroatLeft) && layoutTurnoutABUp ) {
			setSignalHeadOnPanel(2,b3WayField.getText().trim(),
				(int)(layoutTurnoutA.getCoordsC().getX()-testIcon.getIconWidth()),
				(int)(layoutTurnoutA.getCoordsC().getY()+4) );
		}
		else if( layoutTurnoutAHorizontal && (!layoutTurnoutAThroatLeft) && (!layoutTurnoutABUp) ) {
			setSignalHeadOnPanel(2,b3WayField.getText().trim(),
				(int)(layoutTurnoutA.getCoordsC().getX()-testIcon.getIconWidth()),
				(int)(layoutTurnoutA.getCoordsC().getY()-4-testIcon.getIconHeight()) );
		}
		else if( layoutTurnoutAVertical && layoutTurnoutAThroatUp && layoutTurnoutABLeft ) {
			setSignalHeadOnPanel(3,b3WayField.getText().trim(),
				(int)(layoutTurnoutA.getCoordsC().getX()+4),
				(int)(layoutTurnoutA.getCoordsC().getY()) );
		}
		else if( layoutTurnoutAVertical && layoutTurnoutAThroatUp && (!layoutTurnoutABLeft) ) {
			setSignalHeadOnPanel(3,b3WayField.getText().trim(),
				(int)(layoutTurnoutA.getCoordsC().getX()-4-testIcon.getIconWidth()),
				(int)(layoutTurnoutA.getCoordsC().getY()) );
		}
		else if( layoutTurnoutAVertical && (!layoutTurnoutAThroatUp) && layoutTurnoutABLeft ) {
			setSignalHeadOnPanel(1,b3WayField.getText().trim(),
				(int)(layoutTurnoutA.getCoordsC().getX()+4),
				(int)(layoutTurnoutA.getCoordsC().getY()-testIcon.getIconHeight()) );
		}
		else if( layoutTurnoutAVertical && (!layoutTurnoutAThroatUp) && (!layoutTurnoutABLeft) ) {
			setSignalHeadOnPanel(1,b3WayField.getText().trim(),
				(int)(layoutTurnoutA.getCoordsC().getX()-4-testIcon.getIconWidth()),
				(int)(layoutTurnoutA.getCoordsC().getY()-testIcon.getIconHeight()) );
		}
	}
	private void place3WayContinuing() {
		if (testIcon == null)
			testIcon = signalIconEditor.getIcon(0);
		if( layoutTurnoutBHorizontal && layoutTurnoutBThroatLeft && layoutTurnoutBBUp) {
			setSignalHeadOnPanel(0,c3WayField.getText().trim(),
				(int)(layoutTurnoutB.getCoordsB().getX()),
				(int)(layoutTurnoutB.getCoordsB().getY()-4-testIcon.getIconHeight()) );
		}
		else if( layoutTurnoutBHorizontal && layoutTurnoutBThroatLeft && (!layoutTurnoutBBUp) ) {
			setSignalHeadOnPanel(0,c3WayField.getText().trim(),
				(int)(layoutTurnoutB.getCoordsB().getX()),
				(int)(layoutTurnoutB.getCoordsB().getY()+4) );
		}
		else if( layoutTurnoutBHorizontal && (!layoutTurnoutBThroatLeft) && layoutTurnoutBBUp ) {
			setSignalHeadOnPanel(2,c3WayField.getText().trim(),
				(int)(layoutTurnoutB.getCoordsB().getX()-testIcon.getIconWidth()),
				(int)(layoutTurnoutB.getCoordsB().getY()-4-testIcon.getIconHeight()) );
		}
		else if( layoutTurnoutBHorizontal && (!layoutTurnoutBThroatLeft) && (!layoutTurnoutBBUp) ) {
			setSignalHeadOnPanel(2,c3WayField.getText().trim(),
				(int)(layoutTurnoutB.getCoordsB().getX()-testIcon.getIconWidth()),
				(int)(layoutTurnoutB.getCoordsB().getY()+4) );
		}
		else if( layoutTurnoutBVertical && layoutTurnoutBThroatUp && layoutTurnoutBBLeft ) {
			setSignalHeadOnPanel(3,c3WayField.getText().trim(),
				(int)(layoutTurnoutB.getCoordsB().getX()-4-testIcon.getIconWidth()),
				(int)(layoutTurnoutB.getCoordsB().getY()) );
		}
		else if( layoutTurnoutBVertical && layoutTurnoutBThroatUp && (!layoutTurnoutBBLeft) ) {
			setSignalHeadOnPanel(3,c3WayField.getText().trim(),
				(int)(layoutTurnoutB.getCoordsB().getX()+4),
				(int)(layoutTurnoutB.getCoordsB().getY()) );
		}
		else if( layoutTurnoutBVertical && (!layoutTurnoutBThroatUp) && layoutTurnoutBBLeft ) {
			setSignalHeadOnPanel(1,c3WayField.getText().trim(),
				(int)(layoutTurnoutB.getCoordsB().getX()-4-testIcon.getIconWidth()),
				(int)(layoutTurnoutB.getCoordsB().getY()-testIcon.getIconHeight()) );
		}
		else if( layoutTurnoutBVertical && (!layoutTurnoutBThroatUp) && (!layoutTurnoutBBLeft) ) {
			setSignalHeadOnPanel(1,c3WayField.getText().trim(),
				(int)(layoutTurnoutB.getCoordsB().getX()+4),
				(int)(layoutTurnoutB.getCoordsB().getY()-testIcon.getIconHeight()) );
		}
	}
	private void place3WayDivergingB() {
		if (testIcon == null)
			testIcon = signalIconEditor.getIcon(0);
		if( layoutTurnoutBHorizontal && layoutTurnoutBThroatLeft && layoutTurnoutBBUp) {
			setSignalHeadOnPanel(0,d3WayField.getText().trim(),
				(int)(layoutTurnoutB.getCoordsC().getX()),
				(int)(layoutTurnoutB.getCoordsC().getY()+4) );
		}
		else if( layoutTurnoutBHorizontal && layoutTurnoutBThroatLeft && (!layoutTurnoutBBUp) ) {
			setSignalHeadOnPanel(0,d3WayField.getText().trim(),
				(int)(layoutTurnoutB.getCoordsC().getX()),
				(int)(layoutTurnoutB.getCoordsC().getY()-4-testIcon.getIconHeight()) );
		}
		else if( layoutTurnoutBHorizontal && (!layoutTurnoutBThroatLeft) && layoutTurnoutBBUp ) {
			setSignalHeadOnPanel(2,d3WayField.getText().trim(),
				(int)(layoutTurnoutB.getCoordsC().getX()-testIcon.getIconWidth()),
				(int)(layoutTurnoutB.getCoordsC().getY()+4) );
		}
		else if( layoutTurnoutBHorizontal && (!layoutTurnoutBThroatLeft) && (!layoutTurnoutBBUp) ) {
			setSignalHeadOnPanel(2,d3WayField.getText().trim(),
				(int)(layoutTurnoutB.getCoordsC().getX()-testIcon.getIconWidth()),
				(int)(layoutTurnoutB.getCoordsC().getY()-4-testIcon.getIconHeight()) );
		}
		else if( layoutTurnoutBVertical && layoutTurnoutBThroatUp && layoutTurnoutBBLeft ) {
			setSignalHeadOnPanel(3,d3WayField.getText().trim(),
				(int)(layoutTurnoutB.getCoordsC().getX()+4),
				(int)(layoutTurnoutB.getCoordsC().getY()) );
		}
		else if( layoutTurnoutBVertical && layoutTurnoutBThroatUp && (!layoutTurnoutBBLeft) ) {
			setSignalHeadOnPanel(3,d3WayField.getText().trim(),
				(int)(layoutTurnoutB.getCoordsC().getX()-4-testIcon.getIconWidth()),
				(int)(layoutTurnoutB.getCoordsC().getY()) );
		}
		else if( layoutTurnoutBVertical && (!layoutTurnoutBThroatUp) && layoutTurnoutBBLeft ) {
			setSignalHeadOnPanel(1,d3WayField.getText().trim(),
				(int)(layoutTurnoutB.getCoordsC().getX()+4),
				(int)(layoutTurnoutB.getCoordsC().getY()-testIcon.getIconHeight()) );
		}
		else if( layoutTurnoutBVertical && (!layoutTurnoutBThroatUp) && (!layoutTurnoutBBLeft) ) {
			setSignalHeadOnPanel(1,d3WayField.getText().trim(),
				(int)(layoutTurnoutB.getCoordsC().getX()-4-testIcon.getIconWidth()),
				(int)(layoutTurnoutB.getCoordsC().getY()-testIcon.getIconHeight()) );
		}
	}
	private void set3WayLogicThroatContinuing() {
		TrackSegment track = (TrackSegment)layoutTurnoutB.getConnectB();
		if (track==null) {
			JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
					rb.getString("InfoMessage7"),"",JOptionPane.INFORMATION_MESSAGE);			
			return;
		}
		LayoutBlock block = track.getLayoutBlock();
		if (block==null) {
			JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
					rb.getString("InfoMessage6"),"",JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		Sensor occupancy = block.getOccupancySensor();
		if (occupancy==null) {
			JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
				java.text.MessageFormat.format(rb.getString("InfoMessage4"),
					new Object[]{block.getUserName()}), 
						null,JOptionPane.INFORMATION_MESSAGE);						
			return;
		}
		SignalHead nextHead = getNextSignalFromObject(track, layoutTurnoutB, 
				a13WayField.getText().trim(), setSignalsAt3WayFrame);
		if ( (nextHead==null) && (!reachedEndBumper()) ) {
			JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
				java.text.MessageFormat.format(rb.getString("InfoMessage5"),
					new Object[]{block.getUserName()}), 
						null,JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		if (a23WayHead!=null) {
			// set up logic for continuing head with 3 heads at throat
			if (!initializeBlockBossLogic(a13WayField.getText().trim())) return;
			logic.setMode(BlockBossLogic.TRAILINGMAIN);
			logic.setTurnout(turnoutB.getSystemName());
			logic.setSensor1(occupancy.getSystemName());
			if (nextHead!=null) {
				logic.setWatchedSignal1(nextHead.getSystemName(),false);
			}
			if (auxSignal!=null) {
				logic.setWatchedSignal1Alt(auxSignal.getSystemName());
			}
			String nearSensorName = setupNearLogix(layoutTurnoutA,true,a13WayHead);
			addNearSensorToLogic(nearSensorName);
			finalizeBlockBossLogic();
			return;
		}
		// only one head at the throat
		JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
					rb.getString("InfoMessage9"),"",JOptionPane.INFORMATION_MESSAGE);			
		return;
	}
	private void set3WayLogicThroatDivergingA() {
		TrackSegment track = (TrackSegment)layoutTurnoutA.getConnectC();
		if (track==null) {
			JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
					rb.getString("InfoMessage7"),"",JOptionPane.INFORMATION_MESSAGE);			
			return;
		}
		LayoutBlock block = track.getLayoutBlock();
		if (block==null) {
			JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
					rb.getString("InfoMessage6"),"",JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		Sensor occupancy = block.getOccupancySensor();
		if (occupancy==null) {
			JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
				java.text.MessageFormat.format(rb.getString("InfoMessage4"),
					new Object[]{block.getUserName()}), 
						null,JOptionPane.INFORMATION_MESSAGE);						
			return;
		}
		SignalHead nextHead = getNextSignalFromObject(track, layoutTurnoutA, 
				a23WayField.getText().trim(), setSignalsAt3WayFrame);
		if ( (nextHead==null) && (!reachedEndBumper()) ) {
			JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
				java.text.MessageFormat.format(rb.getString("InfoMessage5"),
					new Object[]{block.getUserName()}), 
						null,JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		if (!initializeBlockBossLogic(a23WayField.getText().trim())) return;
		logic.setMode(BlockBossLogic.TRAILINGDIVERGING);
		logic.setTurnout(turnoutA.getSystemName());
		logic.setSensor1(occupancy.getSystemName());
		if (nextHead!=null)
			logic.setWatchedSignal1(nextHead.getSystemName(),false);
		if (auxSignal!=null) {
			logic.setWatchedSignal1Alt(auxSignal.getSystemName());
		}
		if (!layoutTurnoutA.isMainlineC())
			logic.setLimitSpeed2(true);
		finalizeBlockBossLogic();			
	}
	private void set3WayLogicThroatDivergingB() {
		TrackSegment track = (TrackSegment)layoutTurnoutB.getConnectC();
		if (track==null) {
			JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
					rb.getString("InfoMessage7"),"",JOptionPane.INFORMATION_MESSAGE);			
			return;
		}
		LayoutBlock block = track.getLayoutBlock();
		if (block==null) {
			JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
					rb.getString("InfoMessage6"),"",JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		Sensor occupancy = block.getOccupancySensor();
		if (occupancy==null) {
			JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
				java.text.MessageFormat.format(rb.getString("InfoMessage4"),
					new Object[]{block.getUserName()}), 
						null,JOptionPane.INFORMATION_MESSAGE);						
			return;
		}
		SignalHead nextHead = getNextSignalFromObject(track, layoutTurnoutB, 
				a33WayField.getText().trim(), setSignalsAt3WayFrame);
		if ( (nextHead==null) && (!reachedEndBumper()) ) {
			JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
				java.text.MessageFormat.format(rb.getString("InfoMessage5"),
					new Object[]{block.getUserName()}), 
						null,JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		if (!initializeBlockBossLogic(a33WayField.getText().trim())) return;
		logic.setMode(BlockBossLogic.TRAILINGDIVERGING);
		logic.setTurnout(turnoutB.getSystemName());
		logic.setSensor1(occupancy.getSystemName());
		if (nextHead!=null)
			logic.setWatchedSignal1(nextHead.getSystemName(),false);
		if (auxSignal!=null) {
			logic.setWatchedSignal1Alt(auxSignal.getSystemName());
		}
		String nearSensorName = setupNearLogix(layoutTurnoutA,true,a33WayHead);
		addNearSensorToLogic(nearSensorName);
		if (!layoutTurnoutB.isMainlineC())
			logic.setLimitSpeed2(true);
		finalizeBlockBossLogic();			
	}
	private void set3WayLogicDivergingA() {
		TrackSegment track = (TrackSegment)layoutTurnoutA.getConnectA();
		if (track==null) {
			JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
					rb.getString("InfoMessage7"),"",JOptionPane.INFORMATION_MESSAGE);			
			return;
		}
		LayoutBlock block = track.getLayoutBlock();
		if (block==null) {
			JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
					rb.getString("InfoMessage6"),"",JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		Sensor occupancy = block.getOccupancySensor();
		if (occupancy==null) {
			JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
				java.text.MessageFormat.format(rb.getString("InfoMessage4"),
					new Object[]{block.getUserName()}), 
						null,JOptionPane.INFORMATION_MESSAGE);						
			return;
		}
		SignalHead nextHead = getNextSignalFromObject(track, layoutTurnoutA, 
				b3WayField.getText().trim(), setSignalsAt3WayFrame);
		if ( (nextHead==null) && (!reachedEndBumper()) ) {
			JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
				java.text.MessageFormat.format(rb.getString("InfoMessage5"),
					new Object[]{block.getUserName()}), 
						null,JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		if (!initializeBlockBossLogic(b3WayField.getText().trim())) return;
		logic.setMode(BlockBossLogic.TRAILINGDIVERGING);
		logic.setTurnout(turnoutA.getSystemName());
		logic.setSensor1(occupancy.getSystemName());
		if (nextHead!=null) {
			logic.setWatchedSignal1(nextHead.getSystemName(),false);
		}
		if (auxSignal!=null) {
			logic.setWatchedSignal1Alt(auxSignal.getSystemName());
		}
		if (!layoutTurnoutA.isMainlineC())
			logic.setLimitSpeed2(true);
		finalizeBlockBossLogic();		
	}
	private void set3WayLogicContinuing() {
		TrackSegment track = (TrackSegment)layoutTurnoutA.getConnectA();
		if (track==null) {
			JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
					rb.getString("InfoMessage7"),"",JOptionPane.INFORMATION_MESSAGE);			
			return;
		}
		LayoutBlock block = track.getLayoutBlock();
		if (block==null) {
			JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
					rb.getString("InfoMessage6"),"",JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		Sensor occupancy = block.getOccupancySensor();
		if (occupancy==null) {
			JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
				java.text.MessageFormat.format(rb.getString("InfoMessage4"),
					new Object[]{block.getUserName()}), 
						null,JOptionPane.INFORMATION_MESSAGE);						
			return;
		}
		SignalHead nextHead = getNextSignalFromObject(track, layoutTurnoutA, 
				c3WayField.getText().trim(), setSignalsAt3WayFrame);
		if ( (nextHead==null) && (!reachedEndBumper()) ) {
			JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
				java.text.MessageFormat.format(rb.getString("InfoMessage5"),
					new Object[]{block.getUserName()}), 
						null,JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		if (!initializeBlockBossLogic(c3WayField.getText().trim())) return;
		logic.setMode(BlockBossLogic.TRAILINGMAIN);
		logic.setTurnout(turnoutB.getSystemName());
		logic.setSensor1(occupancy.getSystemName());
		if (nextHead!=null) {
			logic.setWatchedSignal1(nextHead.getSystemName(),false);
		}
		if (auxSignal!=null) {
			logic.setWatchedSignal1Alt(auxSignal.getSystemName());
		}
		String nearSensorName = setupNearLogix(layoutTurnoutA,true,c3WayHead);
		addNearSensorToLogic(nearSensorName);
		if (!layoutTurnoutB.isMainlineB())
			logic.setLimitSpeed2(true);
		finalizeBlockBossLogic();		
	}
	private void set3WayLogicDivergingB() {
		TrackSegment track = (TrackSegment)layoutTurnoutA.getConnectA();
		if (track==null) {
			JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
					rb.getString("InfoMessage7"),"",JOptionPane.INFORMATION_MESSAGE);			
			return;
		}
		LayoutBlock block = track.getLayoutBlock();
		if (block==null) {
			JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
					rb.getString("InfoMessage6"),"",JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		Sensor occupancy = block.getOccupancySensor();
		if (occupancy==null) {
			JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
				java.text.MessageFormat.format(rb.getString("InfoMessage4"),
					new Object[]{block.getUserName()}), 
						null,JOptionPane.INFORMATION_MESSAGE);						
			return;
		}
		SignalHead nextHead = getNextSignalFromObject(track, layoutTurnoutA, 
				d3WayField.getText().trim(), setSignalsAt3WayFrame);
		if ( (nextHead==null) && (!reachedEndBumper()) ) {
			JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
				java.text.MessageFormat.format(rb.getString("InfoMessage5"),
					new Object[]{block.getUserName()}), 
						null,JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		if (!initializeBlockBossLogic(d3WayField.getText().trim())) return;
		logic.setMode(BlockBossLogic.TRAILINGDIVERGING);
		logic.setTurnout(turnoutB.getSystemName());
		logic.setSensor1(occupancy.getSystemName());
		if (nextHead!=null) {
			logic.setWatchedSignal1(nextHead.getSystemName(),false);
		}
		if (auxSignal!=null) {
			logic.setWatchedSignal1Alt(auxSignal.getSystemName());
		}
		String nearSensorName = setupNearLogix(layoutTurnoutA,true,d3WayHead);
		addNearSensorToLogic(nearSensorName);
		if (!layoutTurnoutB.isMainlineC())
			logic.setLimitSpeed2(true);
		finalizeBlockBossLogic();		
	}

    //
    //The following is for placement of sensors and signal masts at points around the layout
    //
    
    //This section deals with assigning a sensor to a specific boundary point
    
    BeanDetails westBoundSensor;
    BeanDetails eastBoundSensor;
    
    private JButton getAnchorSavedSensors = null;
	private JButton changeSensorAtBoundaryIcon = null;
	private JButton setSensorsAtBoundaryDone = null;
	private JButton setSensorsAtBoundaryCancel = null;
    private boolean setSensorsAtBoundaryOpen = false;
    private JmriJFrame setSensorsAtBoundaryFrame = null;

    private JFrame sensorFrame = null;
    private MultiIconEditor sensorIconEditor = null;
    
    public void setSensorsAtBlockBoundaryFromMenu( PositionablePoint p, MultiIconEditor theEditor, 
					JFrame theFrame) {
		boundaryFromMenu = true;
		boundary = p;
		block1NameField.setText(boundary.getConnect1().getLayoutBlock().getID());
        if (boundary.getType()!=PositionablePoint.ANCHOR)
            block2NameField.setText(boundary.getConnect1().getLayoutBlock().getID());
        else
            block2NameField.setText(boundary.getConnect2().getLayoutBlock().getID());
		setSensorsAtBlockBoundary(theEditor,theFrame);
		return;
	}

    public void setSensorsAtBlockBoundary( MultiIconEditor theEditor, JFrame theFrame) {
        sensorIconEditor = theEditor;
		sensorFrame = theFrame;
		if (setSensorsAtBoundaryOpen) {
			setSensorsAtBoundaryFrame.setVisible(true);
			return;
		}
		// Initialize if needed
		if (setSensorsAtBoundaryFrame == null) {
            westBoundSensor = new BeanDetails("Sensor", jmri.InstanceManager.sensorManagerInstance());
            eastBoundSensor = new BeanDetails("Sensor", jmri.InstanceManager.sensorManagerInstance());
            
            setSensorsAtBoundaryFrame = new JmriJFrame( rb.getString("SensorsAtBoundary"), false, true );
            setSensorsAtBoundaryFrame.addHelpMenu("package.jmri.jmrit.display.SetSensorsAtBoundary", true);
            setSensorsAtBoundaryFrame.setLocation(70,30);
            Container theContentPane = setSensorsAtBoundaryFrame.getContentPane();  
            theContentPane.setLayout(new BorderLayout());
            JPanel header = new JPanel();
            header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
			JPanel panel11 = new JPanel(); 
            panel11.setLayout(new FlowLayout());
			if (boundaryFromMenu) {
				JLabel block1NameLabel = new JLabel( rb.getString("Block")+" 1 "+
					rb.getString("Name")+" : "+boundary.getConnect1().getLayoutBlock().getID());
				panel11.add(block1NameLabel);
			}
			else {
				JLabel block1NameLabel = new JLabel( rb.getString("Block")+" 1 "+
																rb.getString("Name")+" : ");
				panel11.add(block1NameLabel);
				panel11.add(block1NameField);
				block1NameField.setToolTipText(rb.getString("SensorsBlockNameHint"));
			}
            header.add(panel11);
			JPanel panel12 = new JPanel(); 
            panel12.setLayout(new FlowLayout());
			if ((boundaryFromMenu) && (boundary.getType()==PositionablePoint.ANCHOR)){
				JLabel block2NameLabel = new JLabel( rb.getString("Block")+" 2 "+
					rb.getString("Name")+" : "+boundary.getConnect2().getLayoutBlock().getID());
				panel12.add(block2NameLabel);			
			}
			else if (boundary.getType()==PositionablePoint.ANCHOR) {
				JLabel block2NameLabel = new JLabel( rb.getString("Block")+" 2 "+
																rb.getString("Name")+" : ");
				panel12.add(block2NameLabel);
				panel12.add(block2NameField);
				block2NameField.setToolTipText(rb.getString("SensorsBlockNameHint"));
			}
            header.add(panel12);
            header.add(new JSeparator(JSeparator.HORIZONTAL));
			theContentPane.add(header, BorderLayout.NORTH);
            JPanel panel2 = new JPanel();
            
            JPanel main = new JPanel();
            main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
            
            panel2.setLayout(new FlowLayout());
			JLabel shTitle = new JLabel(rb.getString("Sensors"));
			panel2.add(shTitle);
			panel2.add(new JLabel("   "));
            panel2.add(getAnchorSavedSensors = new JButton(rb.getString("GetSaved")));
            getAnchorSavedSensors.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						getSavedAnchorSensors(e);
					}
				});
            getAnchorSavedSensors.setToolTipText( rb.getString("GetSavedHint") );			
            if(boundary.getType()!=PositionablePoint.END_BUMPER)
                main.add(panel2);
            
            if(boundary.getType()!=PositionablePoint.END_BUMPER){
                eastBoundSensor.setBoundaryTitle(rb.getString("East/SouthBound"));
                if ((boundaryFromMenu)  && (boundary.getType()==PositionablePoint.ANCHOR)){
                    if(isAtWestEndOfAnchor(boundary.getConnect1(), boundary)){
                        eastBoundSensor.setBoundaryLabelText("Protecting Block : " + boundary.getConnect2().getLayoutBlock().getDisplayName());
                    }
                    else {
                        eastBoundSensor.setBoundaryLabelText("Protecting Block : " + boundary.getConnect1().getLayoutBlock().getDisplayName());
                    }
                }
                eastBoundSensor.getDetailsPanel().setBackground(new Color(255,255,200));
                main.add(eastBoundSensor.getDetailsPanel());
                
                westBoundSensor.setBoundaryTitle(rb.getString("West/NorthBound"));
                if (boundaryFromMenu){
                    if(isAtWestEndOfAnchor(boundary.getConnect1(), boundary)){
                        westBoundSensor.setBoundaryLabelText("Protecting Block : " + boundary.getConnect1().getLayoutBlock().getDisplayName());
                    } else {
                        westBoundSensor.setBoundaryLabelText("Protecting Block : " + boundary.getConnect2().getLayoutBlock().getDisplayName());
                    }
                }
                westBoundSensor.getDetailsPanel().setBackground(new Color(200,255,255));
                main.add(westBoundSensor.getDetailsPanel());
            }
            else {
                if(isAtWestEndOfAnchor(boundary.getConnect1(), boundary) && (boundaryFromMenu)){
                    eastBoundSensor.setBoundaryLabelText("Protecting Block : " + boundary.getConnect1().getLayoutBlock().getDisplayName());
                    eastBoundSensor.getDetailsPanel().setBackground(new Color(200,255,255));
                    main.add(eastBoundSensor.getDetailsPanel());
                } else if (boundaryFromMenu) {
                    westBoundSensor.setBoundaryLabelText("Protecting Block : " + boundary.getConnect2().getLayoutBlock().getDisplayName());
                    westBoundSensor.getDetailsPanel().setBackground(new Color(255,255,200));
                    main.add(westBoundSensor.getDetailsPanel());
                }
                /*if((boundary!=null) && (boundary.getType()!=PositionablePoint.ANCHOR))
                    westBoundSensor.getDetailsPanel().setVisible(false);*/
            }
            main.add(new JSeparator(JSeparator.HORIZONTAL));
            theContentPane.add(main, BorderLayout.CENTER);
            /*westBoundSensor.getDetailsPanel().setBackground(new Color(200,255,255));
            theContentPane.add(westBoundSensor.getDetailsPanel());*/
            
            JPanel panel6 = new JPanel();
            panel6.setLayout(new FlowLayout());
            panel6.add(changeSensorAtBoundaryIcon = new JButton(rb.getString("ChangeSensorIcon")));
            changeSensorAtBoundaryIcon.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						sensorFrame.setVisible(true);
					}
				});
            changeSensorAtBoundaryIcon.setToolTipText( rb.getString("ChangeSensorIconHint") );
			panel6.add(new JLabel("  "));
            panel6.add(setSensorsAtBoundaryDone = new JButton(rb.getString("Done")));
            setSensorsAtBoundaryDone.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setSensorsAtBoundaryDonePressed(e);
                }
            });
            setSensorsAtBoundaryDone.setToolTipText( rb.getString("SensorDoneHint") );
            panel6.add(setSensorsAtBoundaryCancel = new JButton(rb.getString("Cancel")));
            setSensorsAtBoundaryCancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setSensorsAtBoundaryCancelPressed(e);
                }
            });
            setSensorsAtBoundaryCancel.setToolTipText( rb.getString("CancelHint") );
            theContentPane.add(panel6, BorderLayout.SOUTH);
			setSensorsAtBoundaryFrame.addWindowListener(new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent e) {
					setSensorsAtBoundaryCancelPressed(null);
				}
			});
			if (boundaryFromMenu) getSavedAnchorSensors(null);
		} else {
            if (boundaryFromMenu) getSavedAnchorSensors(null);
        }
        setSensorsAtBoundaryFrame.setPreferredSize(null);
        setSensorsAtBoundaryFrame.pack();
        setSensorsAtBoundaryFrame.setVisible(true);		
		setSensorsAtBoundaryOpen = true;
	}

	/**
	 * Returns the Sensor corresponding to an entry field in the specified dialog.
	 *		This also takes care of UpperCase and trimming of leading and trailing blanks.
	 *		If entry is required, and no entry is present, and error message is sent.
	 *      An error message also results if a sensor head with the entered name is not
	 *      found in the SensorTable.
	 */
	public Sensor getSensorFromEntry(String sensorName,boolean requireEntry,
					JmriJFrame frame) {
		String str = sensorName;
		if ( (str==null) || (str.equals("")) ) {
			if (requireEntry) {
				JOptionPane.showMessageDialog(frame,rb.getString("SensorsError5"),
									rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
			}
			return null;
		}
		Sensor head = jmri.InstanceManager.sensorManagerInstance().getSensor(str);
		if (head==null) {
			JOptionPane.showMessageDialog(frame,
					java.text.MessageFormat.format(rb.getString("SensorsError4"),
						new Object[]{str}), rb.getString("Error"),
							JOptionPane.ERROR_MESSAGE);
			return null ;
		}
		return (head);
	}
	/**
	 * Returns a Sensor given a name
	 */
	public Sensor getSensorFromName(String str) {
		if ( (str==null) || (str.equals("")) ) {
			return null;
		}
		return  (jmri.InstanceManager.sensorManagerInstance().getSensor(str));
	}
    
    public SensorIcon getSensorIcon(String sensorName) {
        SensorIcon l = new SensorIcon(new NamedIcon("resources/icons/smallschematics/tracksegments/circuit-error.gif", 
                                                    "resources/icons/smallschematics/tracksegments/circuit-error.gif"),layoutEditor);
        l.setIcon("SensorStateActive", sensorIconEditor.getIcon(0));
        l.setIcon("SensorStateInactive", sensorIconEditor.getIcon(1));
        l.setIcon("BeanStateInconsistent", sensorIconEditor.getIcon(2));
        l.setIcon("BeanStateUnknown", sensorIconEditor.getIcon(3));
		l.setSensor(sensorName);
		return l;
	}
    
    /**
	 * Returns true if the specified Sensor  is assigned to an object
	 *		on the panel, regardless of whether an icon is displayed or not
     * With sensors we do allow the same sensor to be allocated in both directions.
	 */
	public boolean isSensorAssignedAnywhere(Sensor sensor)
	{
		String sName = sensor.getSystemName();
		String uName = sensor.getUserName();
		for (int i=0;i<layoutEditor.pointList.size();i++) {
			PositionablePoint po = layoutEditor.pointList.get(i);
            //We allow the same sensor to be allocated in both directions.
            if (po!=boundary){
                if ((po.getEastBoundSensor()!=null) &&
                        (po.getEastBoundSensor().equals(sName) || ((uName!=null) &&
                        (po.getEastBoundSensor().equals(uName))))){
                            if(!sensorAssignedElseWhere(sensor.getDisplayName()))
                                return true;

                        }
                if ((po.getWestBoundSensor()!=null) &&
                        (po.getWestBoundSensor().equals(sName) || ((uName!=null) &&
                        (po.getWestBoundSensor().equals(uName))))){
                            if(!sensorAssignedElseWhere(sensor.getDisplayName()))
                                return true;
                }
            }
		}
        
        for (int i=0;i<layoutEditor.turnoutList.size();i++) {
			LayoutTurnout to = layoutEditor.turnoutList.get(i);
			if ((to.getSensorA()!=null) &&
					(to.getSensorA().equals(sName) || ((uName!=null) && 
					(to.getSensorA().equals(uName))))){
                        if(!sensorAssignedElseWhere(sensor.getDisplayName()))
                            return true;
                    }
			if ((to.getSensorB()!=null) &&
					(to.getSensorB().equals(sName) || ((uName!=null) && 
					(to.getSensorB().equals(uName))))){
                        if(!sensorAssignedElseWhere(sensor.getDisplayName()))
                            return true;
                    }
			if ((to.getSensorC()!=null) &&
					(to.getSensorC().equals(sName) || ((uName!=null) && 
					(to.getSensorC().equals(uName))))){
                        if(!sensorAssignedElseWhere(sensor.getDisplayName()))
                            return true;
                    }
			if ((to.getSensorD()!=null) &&
					(to.getSensorD().equals(sName) || ((uName!=null) && 
					(to.getSensorD().equals(uName))))){
                        if(!sensorAssignedElseWhere(sensor.getDisplayName()))
                            return true;
                    }
		}

		for (int i=0;i<layoutEditor.xingList.size();i++) {
			LevelXing x = layoutEditor.xingList.get(i);
			if ((x.getSensorAName()!=null) &&
					(x.getSensorAName().equals(sName) || ((uName!=null) && 
					(x.getSensorAName().equals(uName))))){
                        if(!sensorAssignedElseWhere(sensor.getDisplayName()))
                            return true;
                    }
			if ((x.getSensorBName()!=null) &&
					(x.getSensorBName().equals(sName) || ((uName!=null) && 
					(x.getSensorBName().equals(uName))))){
                        if(!sensorAssignedElseWhere(sensor.getDisplayName()))
                            return true;
                    }
			if ((x.getSensorCName()!=null) &&
					(x.getSensorCName().equals(sName) || ((uName!=null) && 
					(x.getSensorCName().equals(uName))))){
                        if(!sensorAssignedElseWhere(sensor.getDisplayName()))
                            return true;
                    }
			if ((x.getSensorDName()!=null) &&
					(x.getSensorDName().equals(sName) || ((uName!=null) && 
					(x.getSensorDName().equals(uName))))){
                        if(!sensorAssignedElseWhere(sensor.getDisplayName()))
                            return true;
                    }
		}
		return false;
	}
    
    boolean sensorAssignedElseWhere(String sensor){
            int i = JOptionPane.showConfirmDialog(null, java.text.MessageFormat.format(rb.getString("DuplicateSensorAssign"),
					new Object[]{ sensor }),
            rb.getString("DuplicateSensorAssignTitle"),
            JOptionPane.YES_NO_OPTION);
        if(i ==0) {
            return true;
        }
        return false;
    }
	/**
	 * Removes the assignment of the specified Sensor to either a turnout, 
	 *		a positionable point, or a level crossing wherever it is assigned
	 */
	public void removeSensorAssignment(Sensor sensor) 
	{
		String sName = sensor.getSystemName();
		String uName = sensor.getUserName();
		for (int i=0;i<layoutEditor.pointList.size();i++) {
			PositionablePoint po = layoutEditor.pointList.get(i);
			if ((po.getEastBoundSensor()!=null) &&
					(po.getEastBoundSensor().equals(sName) || ((uName!=null) && 
					(po.getEastBoundSensor().equals(uName))))) 
				po.setEastBoundSensor("");
			if ((po.getWestBoundSensor()!=null) &&
					(po.getWestBoundSensor().equals(sName) || ((uName!=null) && 
					(po.getWestBoundSensor().equals(uName))))) 
				po.setWestBoundSensor("");
		}
	}
	/**
	 * Removes the Sensor with the specified name from the panel and from
	 *		assignment to any turnout, positionable point, or level crossing
	 */
	@SuppressWarnings("null")
	public void removeSensorFromPanel(String sensorName) {
		if ( (sensorName==null) || (sensorName.length()<1) ) return;
		Sensor sensor = jmri.InstanceManager.sensorManagerInstance().
														getSensor(sensorName);
		removeSensorAssignment(sensor);
		SensorIcon h = null;
		int index = -1;
		for (int i=0;(i<layoutEditor.sensorList.size())&&(index==-1);i++) {
			h = layoutEditor.sensorList.get(i);
			if (h.getSensor() == sensor) {
				index = i;
			}
		}
		if (index!=(-1)) {
			layoutEditor.sensorList.remove(index);
			h.remove();
			h.dispose();
			needRedraw = true;
		}
	}
    
    private void getSavedAnchorSensors (ActionEvent a) {
		if ( !getSimpleBlockInformation() ) return;
		eastBoundSensor.setTextField(boundary.getEastBoundSensor());	
		westBoundSensor.setTextField(boundary.getWestBoundSensor());
        
        if(boundary.getType()!=PositionablePoint.END_BUMPER){
            if(isAtWestEndOfAnchor(boundary.getConnect1(), boundary)){
                eastBoundSensor.setBoundaryLabelText("Protecting Block : " + boundary.getConnect2().getLayoutBlock().getDisplayName());
            }
            else {
                eastBoundSensor.setBoundaryLabelText("Protecting Block : " + boundary.getConnect1().getLayoutBlock().getDisplayName());
            }
            if(isAtWestEndOfAnchor(boundary.getConnect1(), boundary)){
                westBoundSensor.setBoundaryLabelText("Protecting Block : " + boundary.getConnect1().getLayoutBlock().getDisplayName());
            } else {
                westBoundSensor.setBoundaryLabelText("Protecting Block : " + boundary.getConnect2().getLayoutBlock().getDisplayName());
            }
        } else {
            if(isAtWestEndOfAnchor(boundary.getConnect1(), boundary)){
                westBoundSensor.setBoundaryLabelText("End of Block " + boundary.getConnect1().getLayoutBlock().getDisplayName());
            } else{
                eastBoundSensor.setBoundaryLabelText("End of Block " + boundary.getConnect1().getLayoutBlock().getDisplayName());
            }
        }
        
        setSensorsAtBoundaryFrame.setPreferredSize(null);
        setSensorsAtBoundaryFrame.pack();
	}
    
    private void setSensorsAtBoundaryCancelPressed (ActionEvent a) {
		setSensorsAtBoundaryOpen = false;
		boundaryFromMenu = false;
		setSensorsAtBoundaryFrame.setVisible(false);
	}
    
	private void setSensorsAtBoundaryDonePressed (ActionEvent a) {
		if ( !getSimpleBlockInformation() ) return;
		Sensor eastSensor = getSensorFromEntry(eastBoundSensor.getText(),false,setSensorsAtBoundaryFrame);
		Sensor westSensor = getSensorFromEntry(westBoundSensor.getText(),false,setSensorsAtBoundaryFrame);
		if ( (eastSensor==null) && (westSensor==null) ) {
			JOptionPane.showMessageDialog(setSensorsAtBoundaryFrame,
							rb.getString("SensorsError12"),
								rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
			return;
		}
		// place or update signals as requested
		if ( (eastSensor!=null) && eastBoundSensor.addToPanel() ) {
			if (isSensorAssignedAnywhere(eastSensor) &&
					(eastSensor!=getSensorFromName(boundary.getEastBoundSensor()))) { 
				JOptionPane.showMessageDialog(setSensorsAtBoundaryFrame,
					java.text.MessageFormat.format(rb.getString("SensorsError6"),
						new Object[]{eastBoundSensor.getText()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else {
                removeSensorFromPanel(boundary.getEastBoundSensor());

                /*if(boundary.getEastBoundSignalMast()!=null){
                    String mastName = boundary.getEastBoundSignalMast();
                    for(int i = 0; i<editor.signalMastList.size(); i++){
                        SignalMastIcon icon = editor.signalMastList.get(i);
                        if(icon.getNamedSensor().getName().equals(mastName)){
                        
                        }
                    }
                    PositionableIcon p = signalMastList
                } else if (boundary.getEastBoundSignal()!=null) {
                    PositionableIcon p = signalList
                }*/
                
				placeEastBoundIcon(getSensorIcon(eastBoundSensor.getText()), eastBoundSensor.isRightSelected(), 0.0);
				removeSensorAssignment(eastSensor);
				boundary.setEastBoundSensor(eastBoundSensor.getText());
				needRedraw = true;
			}		
		}
		else if ( (eastSensor!=null) && 
				(eastSensor!=getSensorFromName(boundary.getEastBoundSensor())) &&
				(eastSensor!=getSensorFromName(boundary.getWestBoundSensor())) ) {
			if (isSensorAssignedAnywhere(eastSensor)) {
				JOptionPane.showMessageDialog(setSensorsAtBoundaryFrame,
					java.text.MessageFormat.format(rb.getString("SensorsError13"),
						new Object[]{eastBoundSensor.getText()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}		
			else {
				removeSensorFromPanel(boundary.getEastBoundSensor());
				removeSensorAssignment(eastSensor);
				boundary.setEastBoundSensor(eastBoundSensor.getText());
			}
		}
		else if ( (eastSensor!=null) &&  
				(eastSensor==getSensorFromName(boundary.getWestBoundSensor())) ) {
// need to figure out what to do in this case.			
		}
        if (boundary.getType()==PositionablePoint.END_BUMPER){
            //We simply store the same sensor for an anchor in both directions.
            boundary.setWestBoundSensor(eastBoundSensor.getText());
            return;
        }
		if ( (westSensor!=null) && westBoundSensor.addToPanel() ) {
			if (isSensorAssignedAnywhere(westSensor) &&
					(westSensor!=getSensorFromName(boundary.getWestBoundSensor()))) { 
				JOptionPane.showMessageDialog(setSensorsAtBoundaryFrame,
					java.text.MessageFormat.format(rb.getString("SensorsError6"),
						new Object[]{westBoundSensor.getText()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else {
				removeSensorFromPanel(boundary.getWestBoundSensor());
				placeWestBoundIcon(getSensorIcon(westBoundSensor.getText()),  westBoundSensor.isRightSelected(), 0.0);
				removeSensorAssignment(westSensor);
				boundary.setWestBoundSensor(westBoundSensor.getText());
				needRedraw = true;
			}		
		}
		else if ( (westSensor!=null) && 
				(westSensor!=getSensorFromName(boundary.getEastBoundSensor())) &&
				(westSensor!=getSensorFromName(boundary.getWestBoundSensor())) ) {
			if (isSensorAssignedAnywhere(westSensor)) {
                //Need to do this better, so that the sensor can be on panel multiple times but only alocated to one anchor at a time
				JOptionPane.showMessageDialog(setSensorsAtBoundaryFrame,
					java.text.MessageFormat.format(rb.getString("SensorsError13"),
						new Object[]{westBoundSensor.getText()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}		
			else {
				removeSensorFromPanel(boundary.getWestBoundSensor());
				removeSensorAssignment(westSensor);
				boundary.setWestBoundSensor(westBoundSensor.getText());
			}
		}
		else if ( (westSensor!=null) &&  
				(westSensor==getSensorFromName(boundary.getEastBoundSensor())) ) {
// need to figure out what to do in this case.			
		}
		setSensorsAtBoundaryOpen = false;
		boundaryFromMenu = false;
		setSensorsAtBoundaryFrame.setVisible(false);	
		if (needRedraw) {
			layoutEditor.redrawPanel();
			needRedraw = false;
			layoutEditor.setDirty();
		}
	}
    
	public boolean isSensorOnPanel(Sensor sensor) 
	{
		SensorIcon s = null;
		for (int i=0;i<layoutEditor.sensorList.size();i++) {
			s = layoutEditor.sensorList.get(i);
			if (s.getSensor() == sensor) {
				return true;
			}
		}
		return false;
	}
    
    private JButton getAnchorSavedSignalMasts = null;
	private JButton setSignalMastsAtBoundaryDone = null;
	private JButton setSignalMastsAtBoundaryCancel = null;
    private boolean setSignalMastsAtBoundaryOpen = false;
    private JmriJFrame setSignalMastsAtBoundaryFrame = null;
    
    BeanDetails eastSignalMast;
    BeanDetails westSignalMast;
    
	public void setSignalMastsAtBlockBoundaryFromMenu(PositionablePoint p) {
		boundaryFromMenu = true;
		boundary = p;
		block1NameField.setText(boundary.getConnect1().getLayoutBlock().getID());
        if(boundary.getType()==PositionablePoint.ANCHOR)
            block2NameField.setText(boundary.getConnect2().getLayoutBlock().getID());
		setSignalMastsAtBlockBoundary();
		return;
	}
    
	public void setSignalMastsAtBlockBoundary() {
		if (setSignalMastsAtBoundaryOpen) {
			setSignalMastsAtBoundaryFrame.setVisible(true);
			return;
		}
		// Initialize if needed
		if (setSignalMastsAtBoundaryFrame == null) {
            eastSignalMast = new BeanDetails("SignalMast", jmri.InstanceManager.signalMastManagerInstance());
            westSignalMast = new BeanDetails("SignalMast", jmri.InstanceManager.signalMastManagerInstance());
            setSignalMastsAtBoundaryFrame = new JmriJFrame( rb.getString("SignalMastsAtBoundary"), false, true );
            setSignalMastsAtBoundaryFrame.addHelpMenu("package.jmri.jmrit.display.SetSignalMastsAtBoundary", true);
            setSignalMastsAtBoundaryFrame.setLocation(70,30);
            Container theContentPane = setSignalMastsAtBoundaryFrame.getContentPane();  
            theContentPane.setLayout(new BorderLayout());
			JPanel header = new JPanel();
            header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
            JPanel panel11 = new JPanel(); 
            panel11.setLayout(new FlowLayout());
			if (boundaryFromMenu) {
				JLabel block1NameLabel = new JLabel( rb.getString("Block")+" 1 "+
					rb.getString("Name")+" : "+boundary.getConnect1().getLayoutBlock().getID());
				panel11.add(block1NameLabel);			
			}
			else {
				JLabel block1NameLabel = new JLabel( rb.getString("Block")+" 1 "+
																rb.getString("Name")+" : ");
				panel11.add(block1NameLabel);
				panel11.add(block1NameField);
				block1NameField.setToolTipText(rb.getString("SignalMastsBlockNameHint"));
			}
            header.add(panel11);
			JPanel panel12 = new JPanel(); 
            panel12.setLayout(new FlowLayout());
			if ((boundaryFromMenu) && (boundary.getType()==PositionablePoint.ANCHOR)){
				JLabel block2NameLabel = new JLabel( rb.getString("Block")+" 2 "+
					rb.getString("Name")+" : "+boundary.getConnect2().getLayoutBlock().getID());
				panel12.add(block2NameLabel);			
			}
			else if (boundary.getType()==PositionablePoint.ANCHOR) {
				JLabel block2NameLabel = new JLabel( rb.getString("Block")+" 2 "+
																rb.getString("Name")+" : ");
				panel12.add(block2NameLabel);
				panel12.add(block2NameField);
				block2NameField.setToolTipText(rb.getString("SignalMastsBlockNameHint"));
			}
            
            header.add(panel12);
			header.add(new JSeparator(JSeparator.HORIZONTAL));
            theContentPane.add(header, BorderLayout.NORTH);
            
            JPanel main = new JPanel();
            main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
            
            JPanel panel2 = new JPanel();
            panel2.setLayout(new FlowLayout());
			JLabel shTitle = new JLabel(rb.getString("SignalMasts"));
			panel2.add(shTitle);
			panel2.add(new JLabel("   "));
            panel2.add(getAnchorSavedSignalMasts = new JButton(rb.getString("GetSaved")));
            getAnchorSavedSignalMasts.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						getSavedAnchorSignalMasts(e);
					}
				});
            getAnchorSavedSignalMasts.setToolTipText( rb.getString("GetSavedHint") );
            if(boundary.getType()!=PositionablePoint.END_BUMPER)
                main.add(panel2);
            
            if(boundary.getType()!=PositionablePoint.END_BUMPER){
                eastSignalMast.setBoundaryTitle(rb.getString("East/SouthBound"));
                if (boundaryFromMenu){
                    if(isAtWestEndOfAnchor(boundary.getConnect1(), boundary)){
                        eastSignalMast.setBoundaryLabelText("Protecting Block : " + boundary.getConnect2().getLayoutBlock().getDisplayName());
                    }
                    else {
                        eastSignalMast.setBoundaryLabelText("Protecting Block : " + boundary.getConnect1().getLayoutBlock().getDisplayName());
                    }
                }
                eastSignalMast.getDetailsPanel().setBackground(new Color(255,255,200));
                main.add(eastSignalMast.getDetailsPanel());
                
                westSignalMast.setBoundaryTitle(rb.getString("West/NorthBound"));
                if (boundaryFromMenu){
                    if(isAtWestEndOfAnchor(boundary.getConnect1(), boundary)){
                        westSignalMast.setBoundaryLabelText("Protecting Block : " + boundary.getConnect1().getLayoutBlock().getDisplayName());
                    } else {
                        westSignalMast.setBoundaryLabelText("Protecting Block : " + boundary.getConnect2().getLayoutBlock().getDisplayName());
                    }
                }
                westSignalMast.getDetailsPanel().setBackground(new Color(200,255,255));
                main.add(westSignalMast.getDetailsPanel());
            } else {
                if(isAtWestEndOfAnchor(boundary.getConnect1(), boundary) && (boundaryFromMenu)){
                    eastSignalMast.setBoundaryLabelText("End of Block " + boundary.getConnect1().getLayoutBlock().getDisplayName());
                    eastSignalMast.getDetailsPanel().setBackground(new Color(200,255,255));
                    main.add(eastSignalMast.getDetailsPanel());
                } else if (boundaryFromMenu) {
                    westSignalMast.setBoundaryLabelText("End of Block " + boundary.getConnect1().getLayoutBlock().getDisplayName());
                    westSignalMast.getDetailsPanel().setBackground(new Color(255,255,200));
                    main.add(westSignalMast.getDetailsPanel());
                }
            }
            main.add(new JSeparator(JSeparator.HORIZONTAL));
            theContentPane.add(main, BorderLayout.CENTER);
            JPanel panel6 = new JPanel();
            panel6.setLayout(new FlowLayout());
            panel6.add(setSignalMastsAtBoundaryDone = new JButton(rb.getString("Done")));
            setSignalMastsAtBoundaryDone.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setSignalMastsAtBoundaryDonePressed(e);
                }
            });
            setSignalMastsAtBoundaryDone.setToolTipText( rb.getString("SignalMastDoneHint") );
            panel6.add(setSignalMastsAtBoundaryCancel = new JButton(rb.getString("Cancel")));
            setSignalMastsAtBoundaryCancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setSignalMastsAtBoundaryCancelPressed(e);
                }
            });
            setSignalMastsAtBoundaryCancel.setToolTipText( rb.getString("CancelHint") );
            theContentPane.add(panel6, BorderLayout.SOUTH);
			setSignalMastsAtBoundaryFrame.addWindowListener(new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent e) {
					setSignalMastsAtBoundaryCancelPressed(null);
				}
			});
			if (boundaryFromMenu) getSavedAnchorSignalMasts(null);
		} else if (boundaryFromMenu) {
            getSavedAnchorSignalMasts(null);
        }
        refreshSignalMastAtBoundaryComboBox();
        setSignalMastsAtBoundaryFrame.setPreferredSize(null);
        setSignalMastsAtBoundaryFrame.pack();
        setSignalMastsAtBoundaryFrame.setVisible(true);		
		setSignalMastsAtBoundaryOpen = true;
	}

	/**
	 * Returns the SignalMast corresponding to an entry field in the specified dialog.
	 *		This also takes care of UpperCase and trimming of leading and trailing blanks.
	 *		If entry is required, and no entry is present, and error message is sent.
	 *      An error message also results if a signalMast head with the entered name is not
	 *      found in the SignalMastTable.
	 */
	public SignalMast getSignalMastFromEntry(String signalMastName,boolean requireEntry,
					JmriJFrame frame) {
		String str = signalMastName;
		if ( (str==null) || (str.equals("")) ) {
			if (requireEntry) {
				JOptionPane.showMessageDialog(frame,rb.getString("SignalMastsError5"),
									rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
			}
			return null;
		}
		SignalMast head = jmri.InstanceManager.signalMastManagerInstance().getSignalMast(str);
		if (head==null) {
			JOptionPane.showMessageDialog(frame,
					java.text.MessageFormat.format(rb.getString("SignalMastsError4"),
						new Object[]{str}), rb.getString("Error"),
							JOptionPane.ERROR_MESSAGE);
			return null ;
		}
		return (head);
	}
	/**
	 * Returns a SignalMast given a name
	 */
	public SignalMast getSignalMastFromName(String str) {
		if ( (str==null) || (str.equals("")) ) {
			return null;
		}
		return  (jmri.InstanceManager.signalMastManagerInstance().getSignalMast(str));
	}
    
	/**
	 * Returns true if the specified SignalMast  is assigned to an object
	 *		on the panel, regardless of whether an icon is displayed or not
	 */
	public boolean isSignalMastAssignedAnywhere(SignalMast signalMast) 
	{
		String sName = signalMast.getSystemName();
		String uName = signalMast.getUserName();
		for (int i=0;i<layoutEditor.pointList.size();i++) {
			PositionablePoint po = layoutEditor.pointList.get(i);
			if ((po.getEastBoundSignalMast()!=null) &&
					(po.getEastBoundSignalMast().equals(sName) || ((uName!=null) && 
					(po.getEastBoundSignalMast().equals(uName))))) return true;
			if ((po.getWestBoundSignalMast()!=null) &&
					(po.getWestBoundSignalMast().equals(sName) || ((uName!=null) && 
					(po.getWestBoundSignalMast().equals(uName))))) return true;
		}
        
        for (int i=0;i<layoutEditor.turnoutList.size();i++) {
			LayoutTurnout to = layoutEditor.turnoutList.get(i);
			if ((to.getSignalAMast()!=null) &&
					(to.getSignalAMast().equals(sName) || ((uName!=null) && 
					(to.getSignalAMast().equals(uName)))))
                            return true;
			if ((to.getSignalBMast()!=null) &&
					(to.getSignalBMast().equals(sName) || ((uName!=null) && 
					(to.getSignalBMast().equals(uName)))))
                            return true;
			if ((to.getSignalCMast()!=null) &&
					(to.getSignalCMast().equals(sName) || ((uName!=null) && 
					(to.getSignalCMast().equals(uName)))))
                            return true;
			if ((to.getSignalDMast()!=null) &&
					(to.getSignalDMast().equals(sName) || ((uName!=null) && 
					(to.getSignalDMast().equals(uName)))))
                              return true;
		}

		for (int i=0;i<layoutEditor.xingList.size();i++) {
			LevelXing x = layoutEditor.xingList.get(i);
			if ((x.getSignalAMastName()!=null) &&
					(x.getSignalAMastName().equals(sName) || ((uName!=null) && 
					(x.getSignalAMastName().equals(uName)))))
                            return true;
			if ((x.getSignalBMastName()!=null) &&
					(x.getSignalBMastName().equals(sName) || ((uName!=null) && 
					(x.getSignalBMastName().equals(uName)))))
                            return true;
			if ((x.getSignalCMastName()!=null) &&
					(x.getSignalCMastName().equals(sName) || ((uName!=null) && 
					(x.getSignalCMastName().equals(uName)))))
                            return true;
			if ((x.getSignalDMastName()!=null) &&
					(x.getSignalDMastName().equals(sName) || ((uName!=null) && 
					(x.getSignalDMastName().equals(uName)))))
                            return true;
		}
        
		return false;
	}
	/**
	 * Removes the assignment of the specified SignalMast to either a turnout, 
	 *		a positionable point, or a level crossing wherever it is assigned
	 */
	public void removeSignalMastAssignment(SignalMast signalMast) 
	{
        if (signalMast==null)
            return;
		String sName = signalMast.getSystemName();
		String uName = signalMast.getUserName();
		for (int i=0;i<layoutEditor.pointList.size();i++) {
			PositionablePoint po = layoutEditor.pointList.get(i);
			if ((po.getEastBoundSignalMast()!=null) &&
					(po.getEastBoundSignalMast().equals(sName) || ((uName!=null) && 
					(po.getEastBoundSignalMast().equals(uName))))) 
				po.setEastBoundSignalMast("");
			if ((po.getWestBoundSignalMast()!=null) &&
					(po.getWestBoundSignalMast().equals(sName) || ((uName!=null) && 
					(po.getWestBoundSignalMast().equals(uName))))) 
				po.setWestBoundSignalMast("");
		}
	}
	/**
	 * Removes the SignalMast with the specified name from the panel and from
	 *		assignment to any turnout, positionable point, or level crossing
	 */
	@SuppressWarnings("null")
	public void removeSignalMastFromPanel(String signalMastName) {
		if ( (signalMastName==null) || (signalMastName.length()<1) ) return;
		SignalMast signalMast = jmri.InstanceManager.signalMastManagerInstance().
														getSignalMast(signalMastName);
		removeSignalMastAssignment(signalMast);
		SignalMastIcon h = null;
		int index = -1;
		for (int i=0;(i<layoutEditor.signalMastList.size())&&(index==-1);i++) {
			h = layoutEditor.signalMastList.get(i);
			if (h.getSignalMast() == signalMast) {
				index = i;
			}
		}
		if (index!=(-1)) {
			layoutEditor.signalMastList.remove(index);
			h.remove();
			h.dispose();
			needRedraw = true;
		}
	}
    
    private void getSavedAnchorSignalMasts (ActionEvent a) {
		if ( !getSimpleBlockInformation() ) return;
		eastSignalMast.setTextField(boundary.getEastBoundSignalMast());
		westSignalMast.setTextField(boundary.getWestBoundSignalMast());
        if(boundary.getType()!=PositionablePoint.END_BUMPER){
            if(isAtWestEndOfAnchor(boundary.getConnect1(), boundary)){
                eastSignalMast.setBoundaryLabelText("Protecting Block : " + boundary.getConnect2().getLayoutBlock().getDisplayName());
            }
            else {
                eastSignalMast.setBoundaryLabelText("Protecting Block : " + boundary.getConnect1().getLayoutBlock().getDisplayName());
            }
            if(isAtWestEndOfAnchor(boundary.getConnect1(), boundary)){
                westSignalMast.setBoundaryLabelText("Protecting Block : " + boundary.getConnect1().getLayoutBlock().getDisplayName());
            } else {
                westSignalMast.setBoundaryLabelText("Protecting Block : " + boundary.getConnect2().getLayoutBlock().getDisplayName());
            }
        } else {
            if(isAtWestEndOfAnchor(boundary.getConnect1(), boundary)){
                westSignalMast.setBoundaryLabelText("End of Block " + boundary.getConnect1().getLayoutBlock().getDisplayName());
            } else{
                eastSignalMast.setBoundaryLabelText("End of Block " + boundary.getConnect1().getLayoutBlock().getDisplayName());
            }
        }
        setSignalMastsAtBoundaryFrame.setPreferredSize(null);
        setSignalMastsAtBoundaryFrame.pack();
	}
    
    private void setSignalMastsAtBoundaryCancelPressed (ActionEvent a) {
		setSignalMastsAtBoundaryOpen = false;
		boundaryFromMenu = false;
		setSignalMastsAtBoundaryFrame.setVisible(false);
	}
    
    void refreshSignalMastAtBoundaryComboBox(){
        createListUsedSignalMasts();
        usedMasts.remove(eastSignalMast.getBean());
        usedMasts.remove(westSignalMast.getBean());
        eastSignalMast.getCombo().excludeItems(usedMasts);
        westSignalMast.getCombo().excludeItems(usedMasts);
    }
    
	private void setSignalMastsAtBoundaryDonePressed (ActionEvent a) {
		if ( !getSimpleBlockInformation() ) return;
		SignalMast block1BoundSignalMast = getSignalMastFromEntry(eastSignalMast.getText(),false,setSignalMastsAtBoundaryFrame);
		SignalMast block2BoundSignalMast = getSignalMastFromEntry(westSignalMast.getText(),false,setSignalMastsAtBoundaryFrame);
        if(block1BoundSignalMast==null){
            removeSignalMastFromPanel(boundary.getEastBoundSignalMast());
            removeSignalMastAssignment(jmri.InstanceManager.signalMastManagerInstance().getSignalMast(boundary.getEastBoundSignalMast()));
            boundary.setEastBoundSignalMast("");
        }
        if(block2BoundSignalMast==null){
            removeSignalMastFromPanel(boundary.getWestBoundSignalMast());
            removeSignalMastAssignment(jmri.InstanceManager.signalMastManagerInstance().getSignalMast(boundary.getWestBoundSignalMast()));
            boundary.setWestBoundSignalMast("");
        }

		if ( (block1BoundSignalMast!=null) && eastSignalMast.addToPanel() ) {
			if (isSignalMastOnPanel(block1BoundSignalMast) && 
					(block1BoundSignalMast!=getSignalMastFromName(boundary.getEastBoundSignalMast()))) { 
				JOptionPane.showMessageDialog(setSignalMastsAtBoundaryFrame,
					java.text.MessageFormat.format(rb.getString("SignalMastsError6"),
						new Object[]{eastSignalMast.getText()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else {
				removeSignalMastFromPanel(boundary.getEastBoundSignalMast());
                SignalMastIcon l = new SignalMastIcon(layoutEditor);
                l.setSignalMast(eastSignalMast.getText());
				placeEastBoundIcon(l, eastSignalMast.isRightSelected(), 0);
				removeSignalMastAssignment(block1BoundSignalMast);
				boundary.setEastBoundSignalMast(eastSignalMast.getText());
				needRedraw = true;
			}		
		}
		else if ( (block1BoundSignalMast!=null) && 
				(block1BoundSignalMast!=getSignalMastFromName(boundary.getEastBoundSignalMast())) &&
				(block1BoundSignalMast!=getSignalMastFromName(boundary.getWestBoundSignalMast())) ) {
			if (isSignalMastOnPanel(block1BoundSignalMast)) {
				JOptionPane.showMessageDialog(setSignalMastsAtBoundaryFrame,
					java.text.MessageFormat.format(rb.getString("SignalMastsError13"),
						new Object[]{eastSignalMast.getText()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}		
			else {
				removeSignalMastFromPanel(boundary.getEastBoundSignalMast());
				removeSignalMastAssignment(block1BoundSignalMast);
				boundary.setEastBoundSignalMast(eastSignalMast.getText());
			}
		}
		else if ( (block1BoundSignalMast!=null) &&  
				(block1BoundSignalMast==getSignalMastFromName(boundary.getWestBoundSignalMast())) ) {
// need to figure out what to do in this case.			
		}
		if ( (block2BoundSignalMast!=null) && westSignalMast.addToPanel() ) {
			if (isSignalMastAssignedAnywhere(block2BoundSignalMast) &&
					(block2BoundSignalMast!=getSignalMastFromName(boundary.getWestBoundSignalMast()))) { 
				JOptionPane.showMessageDialog(setSignalMastsAtBoundaryFrame,
					java.text.MessageFormat.format(rb.getString("SignalMastsError6"),
						new Object[]{westSignalMast.getText()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else {
				removeSignalMastFromPanel(boundary.getWestBoundSignalMast());
                SignalMastIcon l = new SignalMastIcon(layoutEditor);
                l.setSignalMast(westSignalMast.getText());
				placeWestBoundIcon(l, westSignalMast.isRightSelected(), 0);
				removeSignalMastAssignment(block2BoundSignalMast);
				boundary.setWestBoundSignalMast(westSignalMast.getText());
				needRedraw = true;
			}		
		}
		else if ( (block2BoundSignalMast!=null) && 
				(block2BoundSignalMast!=getSignalMastFromName(boundary.getEastBoundSignalMast())) &&
				(block2BoundSignalMast!=getSignalMastFromName(boundary.getWestBoundSignalMast())) ) {
			if (isSignalMastAssignedAnywhere(block2BoundSignalMast)) {
                //Need to do this better, so that the signalMast can be on panel multiple times but only alocated to one anchor at a time
				JOptionPane.showMessageDialog(setSignalMastsAtBoundaryFrame,
					java.text.MessageFormat.format(rb.getString("SignalMastsError13"),
						new Object[]{westSignalMast.getText()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}		
			else {
				removeSignalMastFromPanel(boundary.getWestBoundSignalMast());
				removeSignalMastAssignment(block2BoundSignalMast);
				boundary.setWestBoundSignalMast(westSignalMast.getText());
			}
		}
		else if ( (block2BoundSignalMast!=null) &&  
				(block2BoundSignalMast==getSignalMastFromName(boundary.getEastBoundSignalMast())) ) {
// need to figure out what to do in this case.			
		}
		setSignalMastsAtBoundaryOpen = false;
        
//		boundaryFromMenu = false;
		setSignalMastsAtBoundaryFrame.setVisible(false);
		if (needRedraw) {
			layoutEditor.redrawPanel();
			needRedraw = false;
			layoutEditor.setDirty();
		}
	}
    
    public void setIconOnPanel(PositionableIcon l, int rotation,
					Point p) {
		l.setLocation((int)p.getX(), (int)p.getY());
		if (rotation>0) {
            l.rotate(rotation);
		}		
        if(l instanceof SignalMastIcon){
            layoutEditor.putSignalMast((SignalMastIcon) l);
        } else if (l instanceof SensorIcon){
            layoutEditor.putSensor((SensorIcon) l);
        } else if (l instanceof SignalHeadIcon){
            layoutEditor.putSignal((SignalHeadIcon) l);
        }
	}
    
    public void setIconOnPanel(PositionableIcon l, int rotation,
					int xLoc, int yLoc) {
		l.setLocation(xLoc,yLoc);
		if (rotation>0) {
            l.rotate(rotation);
		}
        if(l instanceof SignalMastIcon){
            layoutEditor.putSignalMast((SignalMastIcon) l);
        } else if (l instanceof SensorIcon){
            layoutEditor.putSensor((SensorIcon) l);
        } else if (l instanceof SignalHeadIcon){
            layoutEditor.putSignal((SignalHeadIcon) l);
        }
	}
    
	private void placeEastBoundIcon(PositionableIcon icon, boolean right, double fromPoint) {

		Point2D p = boundary.getCoords();
        
        //Track segment is used to determine the alignment, therefore this is opposite to the block that we are protecting
        TrackSegment t = boundary.getConnect2();
        boolean dir = true;
        if(boundary.getType()==PositionablePoint.END_BUMPER){
            t = boundary.getConnect1();
        }
        else {
            if(isAtWestEndOfAnchor(boundary.getConnect1(), boundary)){
                //dir = false;
                t = boundary.getConnect1();
            }
        }
        
        Point2D pt2;
        if(t.getConnect1()==boundary){
            pt2 = layoutEditor.getCoords(t.getConnect2(),t.getType2());
        } else {
            pt2 = layoutEditor.getCoords(t.getConnect1(),t.getType1());
        }
        setIconOnPanel(t, icon, dir, p, pt2, right, fromPoint);

	}
    
    private void placeWestBoundIcon(PositionableIcon icon, boolean right, double fromPoint) {

		Point2D p = boundary.getCoords();
        
        //Track segment is used to determine the alignment, therefore this is opposite to the block that we are protecting
        TrackSegment t = boundary.getConnect1();
        boolean dir = false;
        if(boundary.getType()!=PositionablePoint.END_BUMPER){
            if(isAtWestEndOfAnchor(boundary.getConnect1(), boundary)){
                //dir = false;
                t = boundary.getConnect2();
            }
        }
        
        Point2D pt2;
        if(t.getConnect1()==boundary){
            pt2 = layoutEditor.getCoords(t.getConnect2(),t.getType2());
        } else {
            pt2 = layoutEditor.getCoords(t.getConnect1(),t.getType1());
        }
        setIconOnPanel(t, icon, dir, p, pt2, right, fromPoint);

	}

    //Manually need to calculate if the signal is pointing in an east or west bound direction!
	/*private void placeWestBoundIcon(PositionableIcon icon, boolean right, double fromPoint) {

		Point2D p = boundary.getCoords();
        
        boolean dir = false;
        
        TrackSegment t = boundary.getConnect1();
        Point2D pt2;
        if(boundary.getType()==PositionablePoint.ANCHOR){
            pt2 = layoutEditor.getCoords(t.getConnect1(),t.getType1());
            if(t.getConnect1()==boundary){
                pt2 = layoutEditor.getCoords(t.getConnect2(),t.getType2());
            }
        }
        else {
            if(isAtWestEndOfAnchor(boundary.getConnect1(), boundary)){
                t = boundary.getConnect2();
            }
            
            Point2D point;
            if (t.getConnect1()==boundary){
                point = layoutEditor.getCoords(t.getConnect2(), t.getType2());
                if(log.isDebugEnabled())
                    log.debug("connection from 2 " + boundary.getConnect2().getLayoutBlock().getDisplayName());
            } else {
                point = layoutEditor.getCoords(t.getConnect1(), t.getType1());
                if(log.isDebugEnabled())
                    log.debug("connection from 1 " + boundary.getConnect1().getLayoutBlock().getDisplayName());
            }
            
            if(t.getConnect1()==boundary){
                pt2 = layoutEditor.getCoords(t.getConnect2(),t.getType2());
            } else {
                pt2 = layoutEditor.getCoords(t.getConnect1(),t.getType1());
            }
        }
        setIconOnPanel(t, icon,  dir, p, pt2, right, fromPoint);
	}*/
    
    void setIconOnPanel(TrackSegment t, PositionableIcon l, boolean eastbound, Point2D p, Point2D pt2, boolean side, double fromPoint){
    
        Point2D pt1 = p;
        
        double pt1x;
        double pt1y;
        pt1x = pt1.getX();
        pt1y = pt1.getY();
        
        double pt2x;
        double pt2y;
        pt2x = pt2.getX();
        pt2y = pt2.getY();

        
        double triX = pt2x-pt1x;
        double triY = pt2y-pt1y;
        
        log.debug("X" + triX + " Y" + triY);
        Point loc = new Point(0,0);
        if(triX==0.0D){
            //In a vertical Striaght Line
            if(eastbound){
                loc = northToSouth(p, l, side, fromPoint);
            } else {
                loc = southToNorth(p, l, side, fromPoint);
            }
        }
        else if(triY==0.0D){
            //In a Horizontal Straight Line
            if(eastbound){
                loc = westToEast(p, l, side, fromPoint);
            } else {
                loc = eastToWest(p, l, side, fromPoint);
            }
        }
        else 
        {
            double a;
            double o;
            // Compute arc's chord
            a = pt2x - pt1x;
            o = pt2y - pt1y;
            double radius=Math.sqrt(((a*a)+(o*o)));  //chord equates to radius of circle
            
            double pt1xa;
            double pt1ya;
            pt1xa=pt1x+radius;
            pt1ya=pt1y;
            double a1;
            double o1;
            a1 = pt2x - pt1xa;
            o1 = pt2y - pt1ya;
            double chord=Math.sqrt(((a1*a1)+(o1*o1)));
            
            double rsq = Math.pow(radius,2);
            
            double anglefromdatum = Math.acos((rsq+rsq-Math.pow(chord, 2))/(2*radius*radius));
            
            log.debug("radius " + radius + " Chord " + chord);
            log.debug("Angle from datum line " + Math.toDegrees(anglefromdatum));
            double tanx = o / a;
            
            double angletan = Math.atan(tanx);
            
            int oldHeight = l.maxHeight();
            int oldWidth = l.maxWidth();
            
            int rotate = ((int) Math.toDegrees(anglefromdatum));
            log.debug(Math.toDegrees(angletan) + " " + a + " " + o + " " + Math.toDegrees(tanx));

            
            //pt1 is always our boundary point
            //East side
            if(pt2x>pt1x){
            //East Sides
                if(pt2y>pt1y){
                    //"South East Corner"
                    rotate = rotate + 270;  //Correct for SM111, sm101, sm121, SM80
                    l.rotate(rotate);
                    loc = southEastToNorthWest(p, l, oldWidth, oldHeight, rotate, side, fromPoint);
                } else {
                    //"North East corner" //correct for sm110, sm70, sm131
                    rotate = 270-rotate;
                    l.rotate(rotate);
                    loc = northEastToSouthWest(p, l, oldWidth, oldHeight, rotate, side, fromPoint);
                }
            
            } else {
                //West Side
                if(pt2y>pt1y){
                    //South West //WORKING FOR SM141, sm130, SM71
                    l.rotate(rotate-90);
                    //South West
                    loc = southWestToNorthEast(p, l, oldWidth, oldHeight, rotate, side, fromPoint);
                } else {
                    //North West //Working FOR SM140, SM81, sm120
                    rotate = (180-rotate)+90;
                    l.rotate(rotate);
                    loc = northWestToSouthEast(p, l, oldWidth, oldHeight, rotate, side, fromPoint);
                }
            }
        }
        setIconOnPanel(l, 0, loc);
    }
    
    Point southToNorth(Point2D p, PositionableIcon l, boolean right, double fromPoint){
        int offsetx=0;
        int offsety=(int)(p.getY()+offSetFromPoint+fromPoint);
        if (right){
            offsetx = (int)p.getX()+offSetFromPoint;
        } else {
            offsetx = (int)p.getX()-offSetFromPoint-l.maxWidth();
        }
        return new Point(offsetx, offsety);
    }
    
    Point northToSouth(Point2D p, PositionableIcon l, boolean right, double fromPoint){
        l.rotate(180);
        int offsetx=0;
        int offsety=(int)(p.getY()-(offSetFromPoint+fromPoint)-l.maxHeight());
        if(right){
            offsetx = (int)p.getX()-offSetFromPoint-l.maxWidth();
        } else {
            offsetx = (int)p.getX()+offSetFromPoint;
        }
        return new Point(offsetx, offsety);
    }
    
    Point westToEast(Point2D p, PositionableIcon l, boolean right, double fromPoint){
        l.rotate(90);
        int offsetx=(int)(p.getX()-(l.maxWidth()+(offSetFromPoint+fromPoint-1)));
        int offsety=0;
        if(right){
            offsety = (int)p.getY()+(offSetFromPoint-1);
        } else {
            offsety = (int)p.getY() - (offSetFromPoint) - l.maxHeight();
        }
        return new Point(offsetx, offsety);
    }
    
    Point eastToWest(Point2D p, PositionableIcon l, boolean right, double fromPoint){
        l.rotate(-90);
        int offsetx=(int)(p.getX()+offSetFromPoint+fromPoint);
        int offsety=0;
        if(right){
            offsety = (int)p.getY() - (offSetFromPoint-1) - l.maxHeight();
            
        } else {
            offsety = (int)p.getY()+(offSetFromPoint);
        }
        return new Point(offsetx, offsety);
    }

    /**
    * come back to this as its a bit tight to the rail on SM110 need re checking
    */ 
    Point northEastToSouthWest(Point2D p, PositionableIcon l, int oldWidth, int oldHeight, double angle, boolean right, double fromPoint){
        angle = angle-180;
        if (angle<45){
            //Because of the angle things get shifted about.
            int tmpWidth = oldWidth;
            int tmpHeight=oldHeight;
            oldWidth=tmpHeight;
            oldHeight=tmpWidth;
        }
        double ang = angle;
        double oppAng = 90-ang;
        angle = Math.toRadians(angle);
        double oppAngRad = Math.toRadians(oppAng);
        double iconAdj = Math.sin(angle)*oldHeight;
        double iconAdjOpp = Math.sin(oppAngRad)*oldHeight;
        double bpa = Math.sin(angle)*(offSetFromPoint+fromPoint);
        double bpo = Math.sin(oppAngRad)*(offSetFromPoint+fromPoint);
        double ta = Math.sin(angle)*offSetFromPoint;
        double to = Math.sin(oppAngRad)*offSetFromPoint;
        
        if(log.isDebugEnabled()){
            log.debug("north east to south west " + angle);
            log.debug("oldWidth " + oldWidth + " oldHeight " + oldHeight);
            log.debug("newWidth " + l.maxWidth() + " newHeight " + l.maxHeight());
            log.debug("Icon adj: " + iconAdj + " opp adj: " + iconAdjOpp);
            log.debug("boundary point opp " + bpo);
            log.debug("boundary point adj " + bpa);
            log.debug("track opp " + to);
            log.debug("track adj " + ta);
        }
        int xpos =0;
        int ypos = 0;
        if (right){
            //double x_dist_to_Icon = (l.maxWidth()-iconAdj)-(bpa-bpo);
            //double y_dist_to_Icon = bpa+bpo+l.maxHeight();
            
            double x_dist_to_Icon = (iconAdjOpp)-(bpa-to);
            double y_dist_to_Icon = ta+bpo+l.maxHeight();
            
            log.debug("x dist " + x_dist_to_Icon + ", y dist " + y_dist_to_Icon);
            
            xpos = (int) (p.getX()-x_dist_to_Icon);
            ypos = (int) (p.getY()-y_dist_to_Icon);
        
        } else {
            double y_dist_to_Icon = iconAdjOpp+(bpo-ta);
            double x_dist_to_Icon = to+bpa;
            //double y_dist_to_Icon = (l.maxHeight()-iconAdj)-(ta-bpo);
            //double x_dist_to_Icon = bpa+to;
            log.debug("x dist " + x_dist_to_Icon + ", y dist " + y_dist_to_Icon);
            
            xpos = (int) (p.getX()+x_dist_to_Icon);
            ypos = (int) (p.getY()-y_dist_to_Icon);        
        
        }
        if(log.isDebugEnabled()){
            log.debug("xpos " + xpos);
            log.debug("yPos " + ypos);
        }
        return new Point(xpos, ypos);
        
    }

    Point southWestToNorthEast(Point2D p, PositionableIcon l, int oldWidth, int oldHeight, double angle, boolean right, double fromPoint){
        angle = 180-angle;
        

        double oppAng = angle;
        double ang = 90-oppAng;
        
        //Because of the angle things get shifted about.
        if (ang<45){ //was angle
            int tmpWidth = oldWidth;
            int tmpHeight=oldHeight;
            oldWidth=tmpHeight;
            oldHeight=tmpWidth;
        }
        
        ang = Math.toRadians(ang);
        double oppAngRad = Math.toRadians(oppAng);
        double iconAdj = Math.sin(ang)*oldHeight;
        double iconAdjOpp = Math.sin(oppAngRad)*oldHeight;
        double bpa = Math.sin(ang)*(offSetFromPoint+fromPoint);  //was angle
        double bpo = Math.sin(oppAngRad)*(offSetFromPoint+fromPoint);
        double ta = Math.sin(ang)*offSetFromPoint; //was angle
        double to = Math.sin(oppAngRad)*offSetFromPoint;
        
        if(log.isDebugEnabled()){
            log.debug("south west to north east " + angle);
            log.debug("oldWidth " + oldWidth + " oldHeight " + oldHeight);
            log.debug("newWidth " + l.maxWidth() + " newHeight " + l.maxHeight());
            log.debug("Icon adj: " + iconAdj + " opp adj: " + iconAdjOpp);
            log.debug("boundary point opp " + bpo);
            log.debug("boundary point adj " + bpa);
            log.debug("track opp " + to);
            log.debug("track adj " + ta);
        }
        
        int xpos;
        int ypos;
        
        if(right)
        {
            double x_dist_to_Icon = iconAdj+(bpa-to);
            double y_dist_to_Icon = ta+bpo;
            log.debug("x dist " + x_dist_to_Icon + ", y dist " + y_dist_to_Icon);
        
            xpos = (int) (p.getX()-x_dist_to_Icon);
            log.debug("xpos " + xpos);
            ypos = (int) (p.getY()+y_dist_to_Icon);
            log.debug("yPos " + ypos);
        }
        else {
            double x_dist_to_Icon = (bpa+to)+l.maxWidth();
            //double y_dist_to_Icon = (iconAdj+(ta-bpo));
            double y_dist_to_Icon = (bpo-ta)-(l.maxHeight()-iconAdjOpp);
            //double y_dist_to_Icon = (iconAdj+(ta-bpo));
            log.debug("x dist " + x_dist_to_Icon + ", y dist " + y_dist_to_Icon);
            xpos = (int) (p.getX()-x_dist_to_Icon);
            ypos = (int) (p.getY()+y_dist_to_Icon);
        }
        if(log.isDebugEnabled()){
            log.debug("xpos " + xpos);
            log.debug("yPos " + ypos);
        }
        return new Point(xpos, ypos);

    }
    
    //Working FOR SM140, SM81, sm120
    Point northWestToSouthEast(Point2D p, PositionableIcon l, int oldWidth, int oldHeight, double angledeg, boolean right, double fromPoint){
        log.debug("angle before " + angledeg);
        angledeg=180-angledeg;
        angledeg=90-angledeg;
        log.debug("north west to south east " + angledeg);
        if (angledeg<45){
            //Because of the angle things get shifted about.
            int tmpWidth = oldWidth;
            int tmpHeight=oldHeight;
            oldWidth=tmpHeight;
            oldHeight=tmpWidth;
        }
        log.debug("oldWidth " + oldWidth + " oldHeight " + oldHeight);
        log.debug("newWidth " + l.maxWidth() + " newHeight " + l.maxHeight());
        //double ang = angle;
        double oppAng = 90-angledeg;
        double angle = Math.toRadians(angledeg);
        double oppAngRad = Math.toRadians(oppAng);
        double iconAdj = Math.sin(angle)*oldHeight;
        double iconAdjOpp = Math.sin(oppAngRad)*oldHeight;
        
        double bpa = Math.sin(angle)*(offSetFromPoint+fromPoint);  //distance from point
        double bpo = Math.sin(oppAngRad)*(offSetFromPoint+fromPoint);
        double ta = Math.sin(angle)*offSetFromPoint; //distance from track
        double to = Math.sin(oppAngRad)*offSetFromPoint;
        
        if(log.isDebugEnabled()){
            log.debug("north west to south east " + angledeg);
            log.debug("oldWidth " + oldWidth + " oldHeight " + oldHeight);
            log.debug("newWidth " + l.maxWidth() + " newHeight " + l.maxHeight());
            log.debug("Icon adj: " + iconAdj + " opp adj: " + iconAdjOpp);
            log.debug("boundary point opp " + bpo);
            log.debug("boundary point adj " + bpa);
            log.debug("track opp " + to);
            log.debug("track adj " + ta);
        }
        int xpos = 0;
        int ypos = 0;
        if (right){
            //double x_dist_to_Icon = bpa+bpo+l.maxWidth();
            //double y_dist_to_Icon = bpa-(l.maxHeight()-iconAdj);
            double x_dist_to_Icon = (l.maxWidth()+ta+bpo);
            double y_dist_to_Icon = iconAdj+(bpa-to);

            
            log.debug("right x dist " + x_dist_to_Icon + ", y dist " + y_dist_to_Icon);
            
            xpos = (int) (p.getX()-x_dist_to_Icon);
            ypos = (int) (p.getY()-y_dist_to_Icon); //was +
        } else {
            //This still needs to be worked out.
            //double y_dist_to_Icon = bpa+bpo+l.maxHeight();
            //double x_dist_to_Icon = iconAdj+(bpa-bpo);
            
            double y_dist_to_Icon = l.maxHeight()+bpa+to;//+(l.maxWidth()-iconAdj);
            //double y_dist_to_Icon = bpa-(l.maxHeight()-iconAdj);
            //double y_dist_to_Icon = ta+bpo+l.maxHeight();
            double x_dist_to_Icon = (iconAdjOpp)+(bpo-ta);
            //double x_dist_to_Icon = iconAdj+(bpa-to);            
            log.debug("left x dist " + x_dist_to_Icon + ", y dist " + y_dist_to_Icon);
            
            xpos = (int) (p.getX()-x_dist_to_Icon);
            ypos = (int) (p.getY()-y_dist_to_Icon);
        }
        if(log.isDebugEnabled()){
            log.debug(p.getX() + " xpos " + xpos);
            log.debug(p.getY() + " yPos " + ypos);
        }
        return new Point(xpos, ypos);
    }
    
    double adjust = (5.0/90.0);
    int awayright = 5;
    final int offSetFromPoint = 5;
    
    //Correct for SM111, sm101, sm121, SM80
    Point southEastToNorthWest(Point2D p, PositionableIcon l, int oldWidth, int oldHeight, double angleDeg, boolean right, double fromPoint){
        angleDeg = 360-angleDeg;
        
        if (angleDeg>45){
            //Because of the angle things get shifted about.
            int tmpWidth = oldWidth;
            int tmpHeight=oldHeight;
            oldWidth=tmpWidth;
            oldHeight=tmpHeight;
        }
        
//        double ang = angle;
        double oppAng = 90-angleDeg;
        double angle = Math.toRadians(angleDeg);
        double oppAngRad = Math.toRadians(oppAng);
        double iconAdj = Math.sin(angle)*oldHeight;
        double iconAdjOpp = Math.sin(oppAngRad)*oldHeight;
        double bpa = Math.sin(angle)*(offSetFromPoint+fromPoint);
        double bpo = Math.sin(oppAngRad)*(offSetFromPoint+fromPoint);
        double ta = Math.sin(angle)*offSetFromPoint; //distance from track
        double to = Math.sin(oppAngRad)*offSetFromPoint;
        if(log.isDebugEnabled()){
            log.debug("south east to north west " + angleDeg);
            log.debug("oldWidth " + oldWidth + " oldHeight " + oldHeight);
            log.debug("newWidth " + l.maxWidth() + " newHeight " + l.maxHeight());
            log.debug("Icon adj: " + iconAdj + " opp adj: " + iconAdjOpp);
            log.debug("boundary point opp " + bpo);
            log.debug("boundary point adj " + bpa);
            log.debug("track opp " + to);
            log.debug("track adj " + ta);
        }
        int xpos = 0;
        int ypos = 0;
        if (right){
            //double x_dist_to_Icon = bpa+bpo;
            //double y_dist_to_Icon = (iconAdj+bpa-bpo);
            double x_dist_to_Icon = bpa+to;
            double y_dist_to_Icon = (bpo-ta)-(l.maxHeight()-iconAdjOpp);
            
            log.debug(((bpo-ta)-(l.maxHeight()-iconAdjOpp)));
            log.debug((bpo-(iconAdj+ta)));
            /*if(angleDeg<45){
                y_dist_to_Icon = (bpo-ta)-(l.maxHeight()-iconAdjOpp);
            } else {
                y_dist_to_Icon = bpo-(iconAdj+ta);
            }*/
            //double y_dist_to_Icon = (l.maxHeight()-iconAdj)+(bpo-ta);
            xpos = (int) (p.getX()+x_dist_to_Icon);
            ypos = (int) (p.getY()+y_dist_to_Icon);
            log.debug("right x dist " + x_dist_to_Icon + ", y dist " + y_dist_to_Icon);
        }
        else {
            //double x_dist_to_Icon = l.maxWidth()-(iconAdj+(bpa-bpo));
            //double y_dist_to_Icon = bpa+bpo;
            
            double x_dist_to_Icon = (bpa-to)-(l.maxWidth()-iconAdj);
            double y_dist_to_Icon = bpo+ta;
            
            xpos = (int) (p.getX()+x_dist_to_Icon);
            ypos = (int) (p.getY()+y_dist_to_Icon);
            log.debug("left x dist " + x_dist_to_Icon + ", y dist " + y_dist_to_Icon);
        }
        if(log.isDebugEnabled()){
            log.debug(p.getX() + " xpos " + xpos);
            log.debug(p.getY() + " yPos " + ypos);
        }
        
        return new Point(xpos, ypos);
    }
    
    
	public boolean isSignalMastOnPanel(SignalMast signalMast) 
	{
		SignalMastIcon s = null;
		for (int i=0;i<layoutEditor.signalMastList.size();i++) {
			s = layoutEditor.signalMastList.get(i);
			if (s.getSignalMast() == signalMast) {
				return true;
			}
		}
		return false;
	}
    
    boolean setSignalMastsOpen =false;
    boolean turnoutMastFromMenu = false;
    private JmriJFrame signalMastsJmriFrame = null;

    private JTextField turnoutMastNameField = new JTextField(16);
    private JButton setSignalMastsDone;
    private JButton getSavedSignalMasts;
    private JButton setSignalMastsCancel;
    
    BeanDetails turnoutSignalMastA;
    BeanDetails turnoutSignalMastB;
    BeanDetails turnoutSignalMastC;
    BeanDetails turnoutSignalMastD;
    
    JPanel signalMastTurnoutPanel = new JPanel();
    
    private String[] turnoutBlocks = new String[4];
    
    public void setSignalMastsAtTurnoutFromMenu(LayoutTurnout to, String[] blocks){
    	turnoutMastFromMenu = true;
		layoutTurnout = to;
		turnout = to.getTurnout();
		turnoutMastNameField.setText(to.getTurnoutName());
        turnoutBlocks=new String[4];
        for(int i = 0; i<blocks.length;i++){
            turnoutBlocks[i]=blocks[i];
        }
		setSignalMastsAtTurnouts();
    }
    
    java.util.List<jmri.NamedBean> usedMasts = new ArrayList<jmri.NamedBean>();
    
    void createListUsedSignalMasts(){
        usedMasts = new ArrayList<jmri.NamedBean>();
        for (int i=0;i<layoutEditor.pointList.size();i++) {
			PositionablePoint po = layoutEditor.pointList.get(i);
            //We allow the same sensor to be allocated in both directions.
            if (po!=boundary){
                if (po.getEastBoundSignalMast()!=null && !po.getEastBoundSignalMast().equals(""))
                    usedMasts.add(InstanceManager.signalMastManagerInstance().getSignalMast(po.getEastBoundSignalMast()));
                if (po.getWestBoundSignalMast()!=null && !po.getWestBoundSignalMast().equals(""))
                    usedMasts.add(InstanceManager.signalMastManagerInstance().getSignalMast(po.getWestBoundSignalMast()));
            }
		}
        
        for (int i=0;i<layoutEditor.turnoutList.size();i++) {
			LayoutTurnout to = layoutEditor.turnoutList.get(i);
            //if(to!=layoutTurnout){
                if (to.getSignalAMast()!=null && !to.getSignalAMast().equals(""))
                    usedMasts.add(InstanceManager.signalMastManagerInstance().getSignalMast(to.getSignalAMast()));
                if (to.getSignalBMast()!=null && !to.getSignalBMast().equals(""))
                    usedMasts.add(InstanceManager.signalMastManagerInstance().getSignalMast(to.getSignalBMast()));
                if (to.getSignalCMast()!=null && !to.getSignalCMast().equals(""))
                    usedMasts.add(InstanceManager.signalMastManagerInstance().getSignalMast(to.getSignalCMast()));
                if (to.getSignalDMast()!=null && !to.getSignalDMast().equals("") )
                    usedMasts.add(InstanceManager.signalMastManagerInstance().getSignalMast(to.getSignalDMast()));
            //}
		}

		for (int i=0;i<layoutEditor.xingList.size();i++) {
			LevelXing x = layoutEditor.xingList.get(i);
			if (x.getSignalAMastName()!=null && !x.getSignalAMastName().equals(""))
                usedMasts.add(InstanceManager.signalMastManagerInstance().getSignalMast(x.getSignalAMastName()));
			if (x.getSignalBMastName()!=null && !x.getSignalBMastName().equals(""))
                usedMasts.add(InstanceManager.signalMastManagerInstance().getSignalMast(x.getSignalBMastName()));
			if (x.getSignalCMastName()!=null && !x.getSignalCMastName().equals(""))
                usedMasts.add(InstanceManager.signalMastManagerInstance().getSignalMast(x.getSignalCMastName()));
			if (x.getSignalDMastName()!=null && !x.getSignalDMastName().equals(""))
                usedMasts.add(InstanceManager.signalMastManagerInstance().getSignalMast(x.getSignalDMastName()));
		}
    }
    
    void refreshSignalMastAtTurnoutComboBox(){
        createListUsedSignalMasts();
        usedMasts.remove(turnoutSignalMastA.getBean());
        usedMasts.remove(turnoutSignalMastB.getBean());
        usedMasts.remove(turnoutSignalMastC.getBean());
        usedMasts.remove(turnoutSignalMastD.getBean());
        turnoutSignalMastA.getCombo().excludeItems(usedMasts);
        turnoutSignalMastB.getCombo().excludeItems(usedMasts);
        turnoutSignalMastC.getCombo().excludeItems(usedMasts);
        turnoutSignalMastD.getCombo().excludeItems(usedMasts);
    }
    
    public void setSignalMastsAtTurnouts(){
		if (setSignalMastsOpen) {
            //We will do a refresh in case the block boundaries have changed.
            turnoutSignalMastsGetSaved(null);
			signalMastsJmriFrame.setVisible(true);
			return;
		}
		// Initialize if needed
		if (signalMastsJmriFrame == null) {
            signalMastsJmriFrame = new JmriJFrame( rb.getString("SignalMastsAtTurnout"), false, true );
            signalMastsJmriFrame.addHelpMenu("package.jmri.jmrit.display.SetSignalMastsAtTurnout", true);
            signalMastsJmriFrame.setLocation(70,30);
            Container theContentPane = signalMastsJmriFrame.getContentPane();
            theContentPane.setLayout(new BoxLayout(theContentPane, BoxLayout.Y_AXIS));
			JPanel panel1 = new JPanel(); 
            panel1.setLayout(new FlowLayout());
            turnoutSignalMastA = new BeanDetails("SignalMast", jmri.InstanceManager.signalMastManagerInstance());
            turnoutSignalMastB = new BeanDetails("SignalMast", jmri.InstanceManager.signalMastManagerInstance());
            turnoutSignalMastC = new BeanDetails("SignalMast", jmri.InstanceManager.signalMastManagerInstance());
            turnoutSignalMastD= new BeanDetails("SignalMast", jmri.InstanceManager.signalMastManagerInstance());
			if (turnoutMastFromMenu) {
				JLabel turnoutMastNameLabel = new JLabel( rb.getString("Turnout")+" "+
					rb.getString("Name")+" : "+layoutTurnout.getTurnoutName());
				panel1.add(turnoutMastNameLabel);
                turnoutSignalMastA.setTextField(layoutTurnout.getSignalAMast());
                turnoutSignalMastB.setTextField(layoutTurnout.getSignalBMast());
                turnoutSignalMastC.setTextField(layoutTurnout.getSignalCMast());
                turnoutSignalMastD.setTextField(layoutTurnout.getSignalDMast());
			}
			else {
				JLabel turnoutMastNameLabel = new JLabel( rb.getString("Turnout")+" "+
																rb.getString("Name") );
				panel1.add(turnoutMastNameLabel);
				panel1.add(turnoutMastNameField);
				turnoutMastNameField.setToolTipText(rb.getString("SignalMastsTurnoutNameHint"));
			}
            theContentPane.add(panel1);
			theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));
            JPanel panel2 = new JPanel();
            panel2.setLayout(new FlowLayout());
			JLabel shTitle = new JLabel(rb.getString("SignalMasts"));
			panel2.add(shTitle);
			panel2.add(new JLabel("   "));
            panel2.add(getSavedSignalMasts = new JButton(rb.getString("GetSaved")));
            getSavedSignalMasts.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						turnoutSignalMastsGetSaved(e);
					}
				});
            getSavedSignalMasts.setToolTipText( rb.getString("GetSavedHint") );
			theContentPane.add(panel2);
            
            turnoutSignalMastA.getDetailsPanel().setBackground(new Color(255,255,200));

            turnoutSignalMastB.getDetailsPanel().setBackground(new Color(200,255,255));

            turnoutSignalMastC.getDetailsPanel().setBackground(new Color(200,200,255));

            turnoutSignalMastD.getDetailsPanel().setBackground(new Color(255,200,200));
            
            signalMastTurnoutPanel.setLayout(new GridLayout(0,2));
            
            turnoutSignalMastA.setBoundaryLabel(turnoutBlocks[0]);
            turnoutSignalMastB.setBoundaryLabel(turnoutBlocks[1]);
            turnoutSignalMastC.setBoundaryLabel(turnoutBlocks[2]);
            turnoutSignalMastD.setBoundaryLabel(turnoutBlocks[3]);
            
            if(turnoutBlocks[0]!=null)
                signalMastTurnoutPanel.add(turnoutSignalMastA.getDetailsPanel());
            if(turnoutBlocks[1]!=null)
                signalMastTurnoutPanel.add(turnoutSignalMastB.getDetailsPanel());
            if(turnoutBlocks[2]!=null)
                signalMastTurnoutPanel.add(turnoutSignalMastC.getDetailsPanel());
            if(turnoutBlocks[3]!=null)
                signalMastTurnoutPanel.add(turnoutSignalMastD.getDetailsPanel());
            theContentPane.add(signalMastTurnoutPanel);
            
            
			theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));
            JPanel panel6 = new JPanel();
			panel6.add(new JLabel("  "));
            panel6.add(setSignalMastsDone = new JButton(rb.getString("Done")));
            setSignalMastsDone.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setSignalMastsDonePressed(e);
                }
            });
            setSignalMastsDone.setToolTipText( rb.getString("SignalDoneHint") );
            panel6.add(setSignalMastsCancel = new JButton(rb.getString("Cancel")));
            setSignalMastsCancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setSignalMastsCancelPressed(e);
                }
            });
            setSignalMastsCancel.setToolTipText( rb.getString("CancelHint") );
            theContentPane.add(panel6);
			signalMastsJmriFrame.addWindowListener(new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent e) {
//					setSignalMastsCancelPressed(null);
				}
			});
			if (turnoutFromMenu) turnoutSignalMastsGetSaved(null);
		}
        refreshSignalMastAtTurnoutComboBox();
        signalMastsJmriFrame.setPreferredSize(null);
        signalMastsJmriFrame.pack();
        signalMastsJmriFrame.setVisible(true);
		setSignalMastsOpen = true;
    }
    
    private void turnoutSignalMastsGetSaved (ActionEvent a) {
		if ( !getTurnoutMastInformation() ) return;
        turnoutBlocks = layoutTurnout.getBlockBoundaries();
        turnoutSignalMastA.setTextField(layoutTurnout.getSignalAMast());
        turnoutSignalMastB.setTextField(layoutTurnout.getSignalBMast());
        turnoutSignalMastC.setTextField(layoutTurnout.getSignalCMast());
        turnoutSignalMastD.setTextField(layoutTurnout.getSignalDMast());

        turnoutSignalMastA.setBoundaryLabel(turnoutBlocks[0]);
        turnoutSignalMastB.setBoundaryLabel(turnoutBlocks[1]);
        turnoutSignalMastC.setBoundaryLabel(turnoutBlocks[2]);
        turnoutSignalMastD.setBoundaryLabel(turnoutBlocks[3]);
        signalMastTurnoutPanel.remove(turnoutSignalMastA.getDetailsPanel());
        signalMastTurnoutPanel.remove(turnoutSignalMastB.getDetailsPanel());
        signalMastTurnoutPanel.remove(turnoutSignalMastC.getDetailsPanel());
        signalMastTurnoutPanel.remove(turnoutSignalMastD.getDetailsPanel());

        boolean blockBoundary = false;
        if(turnoutBlocks[0]!=null){
            signalMastTurnoutPanel.add(turnoutSignalMastA.getDetailsPanel());
            blockBoundary = true;
        }
        if(turnoutBlocks[1]!=null){
            signalMastTurnoutPanel.add(turnoutSignalMastB.getDetailsPanel());
            blockBoundary = true;
        }
        if(turnoutBlocks[2]!=null){
            signalMastTurnoutPanel.add(turnoutSignalMastC.getDetailsPanel());
            blockBoundary = true;
        }
        if(turnoutBlocks[3]!=null){
            signalMastTurnoutPanel.add(turnoutSignalMastD.getDetailsPanel());
            blockBoundary = true;
        }
        if (!blockBoundary)
            JOptionPane.showMessageDialog(signalMastsAtXingFrame, "There are no block boundaries on this turnout\nIt is therefore not possible to add Signal Masts to it");
        signalMastsJmriFrame.setPreferredSize(null);
        signalMastsJmriFrame.pack();
	}

    private int isMastAssignedHere(SignalMast mast, LayoutTurnout lTurnout) {
        if ((mast==null) || (lTurnout==null))
            return NONE;
		String sysName = mast.getSystemName();
		String uName = mast.getUserName();
		String name = lTurnout.getSignalAMast();
		if ( (name!=null) && (name.length()>0) && ((name.equals(uName)) || 
						(name.equals(sysName))) ) return A1;
		name = lTurnout.getSignalBMast();
		if ( (name!=null) && (name.length()>0) && ((name.equals(uName)) || 
						(name.equals(sysName))) ) return A2;
		name = lTurnout.getSignalCMast();
		if ( (name!=null) && (name.length()>0) && ((name.equals(uName)) || 
						(name.equals(sysName))) ) return A3;
		name = lTurnout.getSignalDMast();
		if ( (name!=null) && (name.length()>0) && ((name.equals(uName)) || 
						(name.equals(sysName))) ) return B1;
		return NONE;
	}
    
    public void removeAssignment(SignalMast mast) 
	{
		String sName = mast.getSystemName();
		String uName = mast.getUserName();
		for (int i=0;i<layoutEditor.turnoutList.size();i++) {
			LayoutTurnout to = layoutEditor.turnoutList.get(i);
			if ((to.getSignalAMast()!=null) &&
					(to.getSignalAMast().equals(sName) || ((uName!=null) && 
					(to.getSignalAMast().equals(uName))))) to.setSignalAMast("");
			if ((to.getSignalBMast()!=null) &&
					(to.getSignalBMast().equals(sName) || ((uName!=null) && 
					(to.getSignalBMast().equals(uName))))) to.setSignalBMast("");
			if ((to.getSignalCMast()!=null) &&
					(to.getSignalCMast().equals(sName) || ((uName!=null) && 
					(to.getSignalCMast().equals(uName))))) to.setSignalCMast("");
			if ((to.getSignalDMast()!=null) &&
					(to.getSignalDMast().equals(sName) || ((uName!=null) && 
					(to.getSignalDMast().equals(uName))))) to.setSignalDMast("");
		}
		for (int i=0;i<layoutEditor.pointList.size();i++) {
			PositionablePoint po = layoutEditor.pointList.get(i);
			if ((po.getEastBoundSignalMast()!=null) &&
					(po.getEastBoundSignalMast().equals(sName) || ((uName!=null) && 
					(po.getEastBoundSignalMast().equals(uName))))) 
				po.setEastBoundSignalMast("");
			if ((po.getWestBoundSignalMast()!=null) &&
					(po.getWestBoundSignalMast().equals(sName) || ((uName!=null) && 
					(po.getWestBoundSignalMast().equals(uName))))) 
				po.setWestBoundSignalMast("");
		}
		for (int i=0;i<layoutEditor.xingList.size();i++) {
			LevelXing x = layoutEditor.xingList.get(i);
			if ((x.getSignalAMastName()!=null) &&
					(x.getSignalAMastName().equals(sName) || ((uName!=null) && 
					(x.getSignalAMastName().equals(uName))))) x.setSignalAMastName("");
			if ((x.getSignalBMastName()!=null) &&
					(x.getSignalBMastName().equals(sName) || ((uName!=null) && 
					(x.getSignalBMastName().equals(uName))))) x.setSignalBMastName("");
			if ((x.getSignalCMastName()!=null) &&
					(x.getSignalCMastName().equals(sName) || ((uName!=null) && 
					(x.getSignalCMastName().equals(uName))))) x.setSignalCMastName("");
			if ((x.getSignalDMastName()!=null) &&
					(x.getSignalDMastName().equals(sName) || ((uName!=null) && 
					(x.getSignalDMastName().equals(uName))))) x.setSignalDMastName("");
		}
	}
    
    private void setSignalMastsDonePressed (ActionEvent a) {
		// process turnout name
		if ( !getTurnoutMastInformation() ) return;
		// process signal head names
        SignalMast turnoutMast = getSignalMastFromEntry(turnoutSignalMastA.getText(),false,setSignalsFrame);
		SignalMast turnoutMastB = getSignalMastFromEntry(turnoutSignalMastB.getText(),false,setSignalsFrame);
		SignalMast turnoutMastC = getSignalMastFromEntry(turnoutSignalMastC.getText(),false,setSignalsFrame);
		SignalMast turnoutMastD = getSignalMastFromEntry(turnoutSignalMastD.getText(),false,setSignalsFrame);
		// place signals as requested
		if (turnoutSignalMastA.addToPanel()) {
			if (isSignalMastOnPanel(turnoutMast) &&
				(turnoutMast!=getSignalMastFromName(layoutTurnout.getSignalAMast()))) {
				JOptionPane.showMessageDialog(setSignalsFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError6"),
						new Object[]{turnoutSignalMastA.getText()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else {
				removeSignalMastFromPanel(layoutTurnout.getSignalAMast());
                SignalMastIcon l = new SignalMastIcon(layoutEditor);
                l.setSignalMast(turnoutSignalMastA.getText());
				placingBlock(l, turnoutSignalMastA.isRightSelected(), 0.0);
				removeAssignment(turnoutMast);
				layoutTurnout.setSignalAMast(turnoutSignalMastA.getText());
				needRedraw = true;
			}		
		}
		else if (turnoutMast!=null){
			int assigned = isMastAssignedHere(turnoutMast,layoutTurnout);
			if (assigned == NONE) {
				if ( isSignalMastOnPanel(turnoutMast) && 
									isSignalMastAssignedAnywhere(turnoutMast) ) {
					JOptionPane.showMessageDialog(setSignalsFrame,
						java.text.MessageFormat.format(rb.getString("SignalsError8"),
							new Object[]{turnoutSignalMastA.getText()}), 
								rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
					return;
				}		
				else {
					removeSignalMastFromPanel(layoutTurnout.getSignalAMast());
					removeAssignment(turnoutMast);
					layoutTurnout.setSignalAMast(turnoutSignalMastA.getText());
				}
			}
			else if (assigned!=A1) {
// need to figure out what to do in this case.			
			}
		}
        else {
			removeSignalMastFromPanel(layoutTurnout.getSignalAMast());
			layoutTurnout.setSignalAMast("");
		}
		if ( (turnoutSignalMastB.addToPanel()) && (turnoutMastB!=null) ) {
			if (isSignalMastOnPanel(turnoutMastB) && 
				(turnoutMastB!=getSignalMastFromName(layoutTurnout.getSignalBMast()))) {
				JOptionPane.showMessageDialog(setSignalsFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError6"),
						new Object[]{turnoutSignalMastB.getText()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else {
				removeSignalMastFromPanel(layoutTurnout.getSignalBMast());
                SignalMastIcon l = new SignalMastIcon(layoutEditor);
                l.setSignalMast(turnoutSignalMastB.getText());
				placingBlockB(l, turnoutSignalMastB.isRightSelected(), 0.0);
				removeAssignment(turnoutMastB);
				layoutTurnout.setSignalBMast(turnoutSignalMastB.getText());
				needRedraw = true;
			}		
		}
		else if (turnoutMastB!=null) {
			int assigned = isMastAssignedHere(turnoutMastB,layoutTurnout);
			if (assigned == NONE) {
				if (isSignalMastOnPanel(turnoutMastB) && 
									isSignalMastAssignedAnywhere(turnoutMastB) ) {
					JOptionPane.showMessageDialog(setSignalsFrame,
						java.text.MessageFormat.format(rb.getString("SignalsError8"),
							new Object[]{turnoutSignalMastB.getText()}), 
								rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
					return;
				}		
				else {
					removeSignalMastFromPanel(layoutTurnout.getSignalBMast());
					removeAssignment(turnoutMastB);
					layoutTurnout.setSignalBMast(turnoutSignalMastB.getText());
				}
			}
			else if (assigned!=A2) {
// need to figure out what to do in this case.			
			}
		}
		else {
			removeSignalMastFromPanel(layoutTurnout.getSignalBMast());
			layoutTurnout.setSignalBMast("");
		}
        if(turnoutMastC!=null){
            if (turnoutSignalMastC.addToPanel()) {
                if (isSignalMastOnPanel(turnoutMastC) && 
                    (turnoutMastC!=getSignalMastFromName(layoutTurnout.getSignalCMast()))) {
                    JOptionPane.showMessageDialog(setSignalsFrame,
                        java.text.MessageFormat.format(rb.getString("SignalsError6"),
                            new Object[]{turnoutSignalMastC.getText()}), 
                                rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
                    return;
                }
                else {
                    removeSignalMastFromPanel(layoutTurnout.getSignalCMast());
                    SignalMastIcon l = new SignalMastIcon(layoutEditor);
                    l.setSignalMast(turnoutSignalMastC.getText());
                    placingBlockC(l, turnoutSignalMastC.isRightSelected(), 0.0);
                    removeAssignment(turnoutMastC);
                    layoutTurnout.setSignalCMast(turnoutSignalMastC.getText());
                    needRedraw = true;
                }		
            }
            else {
                int assigned = isMastAssignedHere(turnoutMastC,layoutTurnout);
                if (assigned == NONE) {
                    if (isSignalMastOnPanel(turnoutMastC)  && 
                                        isSignalMastAssignedAnywhere(turnoutMastC) ) {
                        JOptionPane.showMessageDialog(setSignalsFrame,
                            java.text.MessageFormat.format(rb.getString("SignalsError8"),
                                new Object[]{turnoutSignalMastC.getText()}), 
                                    rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
                        return;
                    }		
                    else {
                        removeSignalMastFromPanel(layoutTurnout.getSignalCMast());
                        removeAssignment(turnoutMastC);
                        layoutTurnout.setSignalCMast(turnoutSignalMastC.getText());
                    }
                }
                else if (assigned!=A3) {
    // need to figure out what to do in this case.			
                }
            }
        }
        else{
            removeSignalMastFromPanel(layoutTurnout.getSignalCMast());
			layoutTurnout.setSignalCMast("");
        }
        if(turnoutMastD!=null){
            if (turnoutSignalMastD.addToPanel()) {
                if (isSignalMastOnPanel(turnoutMastD) && 
                    (turnoutMastD!=getSignalMastFromName(layoutTurnout.getSignalDMast()))) {
                    JOptionPane.showMessageDialog(setSignalsFrame,
                        java.text.MessageFormat.format(rb.getString("SignalsError6"),
                            new Object[]{divergingField.getText()}), 
                                rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
                    return;
                }
                else {
                    removeSignalMastFromPanel(layoutTurnout.getSignalDMast());
                    SignalMastIcon l = new SignalMastIcon(layoutEditor);
                    l.setSignalMast(turnoutSignalMastD.getText());
                    placingBlockD(l, turnoutSignalMastD.isRightSelected(), 0.0);
                    removeAssignment(turnoutMastD);
                    layoutTurnout.setSignalDMast(turnoutSignalMastD.getText());
                    needRedraw = true;
                }		
            }
            else {
                int assigned = isMastAssignedHere(turnoutMastD,layoutTurnout);
                if (assigned == NONE) {
                    if (isSignalMastOnPanel(turnoutMastD) && 
                                        isSignalMastAssignedAnywhere(turnoutMastD) ) {
                        JOptionPane.showMessageDialog(setSignalsFrame,
                            java.text.MessageFormat.format(rb.getString("SignalsError8"),
                                new Object[]{turnoutSignalMastD.getText()}), 
                                    rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
                        return;
                    }		
                    else {
                        removeSignalMastFromPanel(layoutTurnout.getSignalDMast());
                        removeAssignment(turnoutMastD);
                        layoutTurnout.setSignalDMast(turnoutSignalMastD.getText());
                    }
                }
                else if (assigned!=B1) {
    // need to figure out what to do in this case.			
                }
            }
        } else {
            removeSignalMastFromPanel(layoutTurnout.getSignalDMast());
			layoutTurnout.setSignalDMast("");
        }

		// make sure this layout turnout is not linked to another
		layoutTurnout.setLinkType(LayoutTurnout.NO_LINK);
		layoutTurnout.setLinkedTurnoutName("");
		// finish up
		setSignalMastsOpen = false;
		turnoutFromMenu = false;
		signalMastsJmriFrame.setVisible(false);
		if (needRedraw) {
			layoutEditor.redrawPanel();
			needRedraw = false;
			layoutEditor.setDirty();
		}
	}

    private boolean getTurnoutMastInformation(){
        LayoutTurnout t = null;
		String str = "";
        turnout = null;
        layoutTurnout = null;
        str = turnoutMastNameField.getText();
        if ( (str==null) || (str.equals("")) ) {
            JOptionPane.showMessageDialog(setSignalsFrame,rb.getString("SignalsError1"),
                                rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
            return false;
        }
        turnout = jmri.InstanceManager.turnoutManagerInstance().getTurnout(str);
        if (turnout==null) {
            JOptionPane.showMessageDialog(setSignalsFrame,
                java.text.MessageFormat.format(rb.getString("SignalsError2"),
                    new Object[]{str}), rb.getString("Error"),
                        JOptionPane.ERROR_MESSAGE);
            return false ;
        }
        else if ( (turnout.getUserName()==null) || (turnout.getUserName().equals("")) ||
                                !turnout.getUserName().equals(str) ) {
            turnoutMastNameField.setText(str);
        }
        for (int i=0;i<layoutEditor.turnoutList.size();i++) {
            t = layoutEditor.turnoutList.get(i);
            if (t.getTurnout() == turnout) {
                layoutTurnout = t;
            }
        }
        
		t = layoutTurnout;
		if (t==null) {
		JOptionPane.showMessageDialog(setSignalsFrame,
				java.text.MessageFormat.format(rb.getString("SignalsError3"),
						new Object[]{str}), rb.getString("Error"),
							JOptionPane.ERROR_MESSAGE);
			return false;
		}
        return true;
    }
    
	private void placingBlock(PositionableIcon icon, boolean right, double fromPoint) {
        if(layoutTurnout.getConnectA() instanceof TrackSegment){
            TrackSegment t = (TrackSegment) layoutTurnout.getConnectA();
            Point2D p = layoutTurnout.getCoordsA();
            Point2D end;
            if(t.getConnect1()==layoutTurnout){
                end = layoutEditor.getEndCoords(t.getConnect2(), t.getType2());
            
            } else {
                end = layoutEditor.getEndCoords(t.getConnect1(), t.getType1());
            }
            boolean east = false;
            
            if(end.getX()<p.getX())
                east = true;

            setIconOnPanel(t, icon, east, p, end, right, fromPoint);
            return;
        }
	}
	private void placingBlockB(PositionableIcon icon, boolean right, double fromPoint) {
        
        if(layoutTurnout.getConnectB() instanceof TrackSegment){
            TrackSegment t = (TrackSegment) layoutTurnout.getConnectB();
            Point2D p = layoutTurnout.getCoordsB();
            
            Point2D end;
            if(t.getConnect1()==layoutTurnout){
                end = layoutEditor.getEndCoords(t.getConnect2(), t.getType2());
            
            } else {
                end = layoutEditor.getEndCoords(t.getConnect1(), t.getType1());
            }

            boolean east = false;
            if(end.getX()<p.getX())
                east =true;
            setIconOnPanel(t, icon, east, p, end, right, fromPoint);
            return;
        }
	}
	private void placingBlockC(PositionableIcon icon, boolean right, double fromPoint) {
        
        if(layoutTurnout.getConnectC() instanceof TrackSegment){
            TrackSegment t = (TrackSegment) layoutTurnout.getConnectC();
            Point2D p = layoutTurnout.getCoordsC();

            Point2D end;
            if(t.getConnect1()==layoutTurnout){
                end = layoutEditor.getEndCoords(t.getConnect2(), t.getType2());
            
            } else {
                end = layoutEditor.getEndCoords(t.getConnect1(), t.getType1());
            }
            boolean east = false;

            if(end.getX()<p.getX())
                east = true;
            setIconOnPanel(t, icon, east, p, end, right, fromPoint);
            return;
        }
	}
	private void placingBlockD(PositionableIcon icon, boolean right, double fromPoint) {
        if(layoutTurnout.getConnectD() instanceof TrackSegment){
            TrackSegment t = (TrackSegment) layoutTurnout.getConnectD();
            Point2D p = layoutTurnout.getCoordsA();
            
            Point2D end;
            if(t.getConnect1()==layoutTurnout){
                end = layoutEditor.getEndCoords(t.getConnect2(), t.getType2());
            
            } else {
                end = layoutEditor.getEndCoords(t.getConnect1(), t.getType1());
            }
            
            //TrackSegment t = boundary.getConnect2();
            boolean east = false;
            if(end.getX()<p.getX())
                east = true;
            setIconOnPanel(t, icon, east, p, end, right, fromPoint);
            return;
        }
	}
    
    private void setSignalMastsCancelPressed (ActionEvent a) {
		setSignalMastsOpen = false;
		turnoutFromMenu = false;
		signalMastsJmriFrame.setVisible(false);
	}
    
 	// operational variables for Set SignalMast at Level Crossing tool
	private JmriJFrame signalMastsAtXingFrame = null;
	private boolean setSignalMastsAtXingOpen = false;
	private JTextField blockANameMastField = new JTextField(16);
	private JTextField blockCNameMastField = new JTextField(16);

	private JButton getSavedXingSignalMasts = null;
	private JButton setXingSignalMastsDone = null;
	private JButton setXingSignalMastsCancel = null;

    private boolean xingMastFromMenu = false;
    private String[] xingBlocks = new String[4];

    BeanDetails xingSignalMastA;
    BeanDetails xingSignalMastB;
    BeanDetails xingSignalMastC;
    BeanDetails xingSignalMastD;
    
    JPanel signalMastLevelXingPanel = new JPanel();
    
    Border blackline = BorderFactory.createLineBorder(Color.black);

    
	// display dialog for Set Signals at Level Crossing tool
	public void setSignalMastsAtLevelXingFromMenu (LevelXing xing, String[] blocks,
			JFrame theFrame ) {
		xingMastFromMenu = true;
		levelXing = xing;
		blockANameMastField.setText(levelXing.getBlockNameAC());
		blockCNameMastField.setText(levelXing.getBlockNameBD());
		xingBlocks=new String[4];
        for(int i = 0; i<blocks.length; i++){
            xingBlocks[i]=blocks[i];
        }
        setSignalMastsAtLevelXing(theFrame);
		return;
	}
	public void setSignalMastsAtLevelXing(JFrame theFrame) {
		signalFrame = theFrame;
		if (setSignalMastsAtXingOpen) {
            xingSignalMastsGetSaved(null);
			signalMastsAtXingFrame.setVisible(true);
			return;
		}
		// Initialize if needed
		if (signalMastsAtXingFrame == null) {
            xingSignalMastA = new BeanDetails("SignalMast", jmri.InstanceManager.signalMastManagerInstance());
            xingSignalMastB = new BeanDetails("SignalMast", jmri.InstanceManager.signalMastManagerInstance());
            xingSignalMastC = new BeanDetails("SignalMast", jmri.InstanceManager.signalMastManagerInstance());
            xingSignalMastD = new BeanDetails("SignalMast", jmri.InstanceManager.signalMastManagerInstance());
            
            signalMastsAtXingFrame = new JmriJFrame( rb.getString("SignalMastsAtLevelXing"), false, true );
            signalMastsAtXingFrame.addHelpMenu("package.jmri.jmrit.display.SetSignalsAtLevelXing", true);
            signalMastsAtXingFrame.setLocation(70,30);
            Container theContentPane = signalMastsAtXingFrame.getContentPane();
            theContentPane.setLayout(new BoxLayout(theContentPane, BoxLayout.Y_AXIS));
			JPanel panel11 = new JPanel(); 
            panel11.setLayout(new FlowLayout());
			if (xingMastFromMenu) {
				JLabel blockANameLabel = new JLabel( rb.getString("BlockAtA")+" "+
							rb.getString("Name")+" : "+levelXing.getBlockNameAC());
			
				panel11.add(blockANameLabel);
                xingSignalMastA.setTextField(levelXing.getSignalAMastName());
                xingSignalMastB.setTextField(levelXing.getSignalBMastName());
                xingSignalMastC.setTextField(levelXing.getSignalCMastName());
                xingSignalMastD.setTextField(levelXing.getSignalDMastName());
			}
			else {
				JLabel blockANameLabel = new JLabel( rb.getString("BlockAtA")+" "+
																rb.getString("Name")+" : ");
				panel11.add(blockANameLabel);
				panel11.add(blockANameMastField);
				blockANameMastField.setToolTipText(rb.getString("SignalsBlockNameHint"));
			}
            theContentPane.add(panel11);
			JPanel panel12 = new JPanel(); 
            panel12.setLayout(new FlowLayout());
			if (xingMastFromMenu) {
				JLabel blockCNameLabel = new JLabel( rb.getString("BlockAtC")+" "+
							rb.getString("Name")+" : "+levelXing.getBlockNameBD());
			
				panel12.add(blockCNameLabel);
			}
			else {
				JLabel blockCNameLabel = new JLabel( rb.getString("BlockAtC")+" "+
																rb.getString("Name")+" : ");
				panel12.add(blockCNameLabel);
				panel12.add(blockCNameMastField);
				blockCNameMastField.setToolTipText(rb.getString("SignalsBlockNameHint"));
			}
            theContentPane.add(panel12);
			theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));
            JPanel panel2 = new JPanel();
            panel2.setLayout(new FlowLayout());
			JLabel shTitle = new JLabel(rb.getString("SignalMast"));
			panel2.add(shTitle);
			panel2.add(new JLabel("   "));
            panel2.add(getSavedXingSignalMasts = new JButton(rb.getString("GetSaved")));
            getSavedXingSignalMasts.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						xingSignalMastsGetSaved(e);
					}
				});
            getSavedXingSignalMasts.setToolTipText( rb.getString("GetSavedHint") );			
			theContentPane.add(panel2);

            xingSignalMastA.getDetailsPanel().setBackground(new Color(255,255,200));

            xingSignalMastB.getDetailsPanel().setBackground(new Color(200,255,255));

            xingSignalMastC.getDetailsPanel().setBackground(new Color(200,200,255));
            
            xingSignalMastD.getDetailsPanel().setBackground(new Color(255,200,200));

            signalMastLevelXingPanel.setLayout(new GridLayout(0,2));
            
            xingSignalMastA.setBoundaryLabel(xingBlocks[0]);
            xingSignalMastB.setBoundaryLabel(xingBlocks[1]);
            xingSignalMastC.setBoundaryLabel(xingBlocks[2]);
            xingSignalMastD.setBoundaryLabel(xingBlocks[3]);
            
            if(xingBlocks[0]!=null)
                signalMastLevelXingPanel.add(xingSignalMastA.getDetailsPanel());
            if(xingBlocks[1]!=null)
                signalMastLevelXingPanel.add(xingSignalMastB.getDetailsPanel());
            if(xingBlocks[2]!=null)
                signalMastLevelXingPanel.add(xingSignalMastC.getDetailsPanel());
            if(xingBlocks[3]!=null)
                signalMastLevelXingPanel.add(xingSignalMastD.getDetailsPanel());
            
            theContentPane.add(signalMastLevelXingPanel);
			theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));
            JPanel panel6 = new JPanel();
            panel6.setLayout(new FlowLayout());

			panel6.add(new JLabel("  "));
            panel6.add(setXingSignalMastsDone = new JButton(rb.getString("Done")));
            setXingSignalMastsDone.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setXingSignalMastsDonePressed(e);
                }
            });
            setXingSignalMastsDone.setToolTipText( rb.getString("SignalDoneHint") );
            panel6.add(setXingSignalMastsCancel = new JButton(rb.getString("Cancel")));
            setXingSignalMastsCancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setXingSignalMastsCancelPressed(e);
                }
            });
            setXingSignalMastsCancel.setToolTipText( rb.getString("CancelHint") );
            theContentPane.add(panel6);
			signalMastsAtXingFrame.addWindowListener(new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent e) {
					setXingSignalMastsCancelPressed(null);
				}
			});
			if (xingMastFromMenu) xingSignalMastsGetSaved(null);
		}
        refreshSignalMastAtXingComboBox();
        signalMastsAtXingFrame.setPreferredSize(null);
        signalMastsAtXingFrame.pack();
        signalMastsAtXingFrame.setVisible(true);
		setSignalMastsAtXingOpen = true;
	}

    void refreshSignalMastAtXingComboBox(){
        createListUsedSignalMasts();
        usedMasts.remove(xingSignalMastA.getBean());
        usedMasts.remove(xingSignalMastB.getBean());
        usedMasts.remove(xingSignalMastC.getBean());
        usedMasts.remove(xingSignalMastD.getBean());
        xingSignalMastA.getCombo().excludeItems(usedMasts);
        xingSignalMastB.getCombo().excludeItems(usedMasts);
        xingSignalMastC.getCombo().excludeItems(usedMasts);
        xingSignalMastD.getCombo().excludeItems(usedMasts);
    }
    
	private void xingSignalMastsGetSaved (ActionEvent a) {
		if ( !getLevelCrossingMastInformation() ) return;
        xingBlocks = levelXing.getBlockBoundaries();
		xingSignalMastA.setTextField(levelXing.getSignalAMastName());
		xingSignalMastB.setTextField(levelXing.getSignalBMastName());
		xingSignalMastC.setTextField(levelXing.getSignalCMastName());
		xingSignalMastD.setTextField(levelXing.getSignalDMastName());
        
        xingSignalMastA.setBoundaryLabel(xingBlocks[0]);
        xingSignalMastB.setBoundaryLabel(xingBlocks[1]);
        xingSignalMastC.setBoundaryLabel(xingBlocks[2]);
        xingSignalMastD.setBoundaryLabel(xingBlocks[3]);
        
        boolean boundary = false;
        signalMastLevelXingPanel.remove(xingSignalMastA.getDetailsPanel());
        signalMastLevelXingPanel.remove(xingSignalMastB.getDetailsPanel());
        signalMastLevelXingPanel.remove(xingSignalMastC.getDetailsPanel());
        signalMastLevelXingPanel.remove(xingSignalMastD.getDetailsPanel());
        if(xingBlocks[0]!=null){
            signalMastLevelXingPanel.add(xingSignalMastA.getDetailsPanel());
            boundary = true;
        }
        if(xingBlocks[1]!=null){
            signalMastLevelXingPanel.add(xingSignalMastB.getDetailsPanel());
            boundary = true;
        }
        if(xingBlocks[2]!=null){
            signalMastLevelXingPanel.add(xingSignalMastC.getDetailsPanel());
            boundary = true;
        }
        if(xingBlocks[3]!=null){
            signalMastLevelXingPanel.add(xingSignalMastD.getDetailsPanel());
            boundary = true;
        }
        if (!boundary){
            JOptionPane.showMessageDialog(signalMastsAtXingFrame, "There are no block boundaries on this level crossing\nIt is therefore not possible to add Signal Masts to it");
        }
        signalMastsAtXingFrame.setPreferredSize(null);
        signalMastsAtXingFrame.pack();
	}
    
    private boolean getLevelCrossingMastInformation() {
		if (!xingMastFromMenu) {
			levelXing = null;
			if (layoutEditor.xingList.size()<=0) {
				JOptionPane.showMessageDialog(signalMastsAtXingFrame,
					rb.getString("SignalsError15"),
								rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return false;
			}
			else if (layoutEditor.xingList.size()==1) {
				levelXing = layoutEditor.xingList.get(0);
			}
			else {
				LayoutBlock xingBlockA = null;
				LayoutBlock xingBlockC = null;
				xingBlockA = getBlockFromEntry(blockANameMastField);
				if (xingBlockA==null) return false;
				if (blockCNameMastField.getText().length()>0) {
					xingBlockC = getBlockFromEntry(blockCNameMastField);
					if (xingBlockC==null) return false;
				}
				LevelXing x = null;
				int foundCount = 0;
				// make two block tests first
				if (xingBlockC!=null) {
					for (int i = 0;(i<layoutEditor.xingList.size());i++) {
						x = layoutEditor.xingList.get(i);
						LayoutBlock xA = null;
						LayoutBlock xB = null;
						LayoutBlock xC = null;
						LayoutBlock xD = null;
						LayoutBlock xAC = x.getLayoutBlockAC();
						LayoutBlock xBD = x.getLayoutBlockBD();
						if (x.getConnectA()!=null) xA = ((TrackSegment)x.getConnectA()).getLayoutBlock();
						if (x.getConnectB()!=null) xB = ((TrackSegment)x.getConnectB()).getLayoutBlock();
						if (x.getConnectC()!=null) xC = ((TrackSegment)x.getConnectC()).getLayoutBlock();
						if (x.getConnectD()!=null) xD = ((TrackSegment)x.getConnectD()).getLayoutBlock();
						if ( ( (xA!=null) && (xC!=null) && ( ((xA==xingBlockA)&&(xC==xingBlockC)) ||
								((xA==xingBlockC)&&(xC==xingBlockA)) ) ) ||
								( (xB!=null) && (xD!=null) && ( ((xB==xingBlockA)&&(xD==xingBlockC)) ||
								((xB==xingBlockC)&&(xD==xingBlockA)) ) ) ) {
							levelXing = x;
							foundCount ++;
						}
						else if ( (xAC!=null) && (xBD!=null) && ( ((xAC==xingBlockA) && (xBD==xingBlockC)) ||
									((xAC==xingBlockC) && (xBD==xingBlockA)) ) ) {
							levelXing = x;
							foundCount ++;
						}				
					}
				}
				if (foundCount==0) {
					// try one block test
					for (int i = 0;(i<layoutEditor.xingList.size());i++) {
						x = layoutEditor.xingList.get(i);
						if ((xingBlockA == x.getLayoutBlockAC()) || (xingBlockA == x.getLayoutBlockBD())) {
							levelXing = x;
							foundCount ++;
						}				
					}
				}
				if (foundCount>1) {
					JOptionPane.showMessageDialog(signalMastsAtXingFrame,
							java.text.MessageFormat.format(rb.getString("SignalsError16"),
								new Object[]{" "+foundCount+" "}), 
									rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
					return false;
				}
				if (levelXing==null) {
					JOptionPane.showMessageDialog(signalMastsAtXingFrame,
								rb.getString("SignalsError17"),
										rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
					return false;
				}
			}
		}
        return true;
    }
    
	private void setXingSignalMastsCancelPressed (ActionEvent a) {
		setSignalMastsAtXingOpen = false;
		signalMastsAtXingFrame.setVisible(false);
		xingMastFromMenu = false;
	}
	private void setXingSignalMastsDonePressed (ActionEvent a) {
		if ( !getLevelCrossingMastInformation() ) return;
        SignalMast aMast = getSignalMastFromEntry(xingSignalMastA.getText(),false,signalMastsAtXingFrame);
		SignalMast bMast = getSignalMastFromEntry(xingSignalMastB.getText(),false,signalMastsAtXingFrame);
		SignalMast cMast = getSignalMastFromEntry(xingSignalMastC.getText(),false,signalMastsAtXingFrame);
		SignalMast dMast = getSignalMastFromEntry(xingSignalMastD.getText(),false,signalMastsAtXingFrame);
		//if ( !getXingSignalMastInformation() ) return;
		// place or update signals as requested
		if ( (aMast!=null) && xingSignalMastA.addToPanel() ) {
			if (isSignalMastOnPanel(aMast) && 
					(aMast!=getSignalMastFromName(levelXing.getSignalAMastName())) ) { 
				JOptionPane.showMessageDialog(signalMastsAtXingFrame,
					java.text.MessageFormat.format(rb.getString("SignalMastsError6"),
						new Object[]{xingSignalMastA.getText()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else {
				removeSignalMastFromPanel(levelXing.getSignalAMastName());
                SignalMastIcon l = new SignalMastIcon(layoutEditor);
                l.setSignalMast(xingSignalMastA.getText());
				placeXingAIcon(l, xingSignalMastA.isRightSelected(), 0.0);
				removeAssignment(aMast);
				levelXing.setSignalAMastName(xingSignalMastA.getText());
				needRedraw = true;
			}		
		}
		else if ( (aMast!=null) && 
				(aMast!=getSignalMastFromName(levelXing.getSignalAMastName())) &&
				(aMast!=getSignalMastFromName(levelXing.getSignalBMastName())) &&
				(aMast!=getSignalMastFromName(levelXing.getSignalCMastName())) &&
				(aMast!=getSignalMastFromName(levelXing.getSignalDMastName())) ) {
			if (isSignalMastOnPanel(aMast)) {
				JOptionPane.showMessageDialog(signalMastsAtXingFrame,
					java.text.MessageFormat.format(rb.getString("SignalMastsError13"),
						new Object[]{xingSignalMastA.getText()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}		
			else {
				removeSignalMastFromPanel(levelXing.getSignalAMastName());
				removeAssignment(aMast);
				levelXing.setSignalAMastName(xingSignalMastA.getText());
			}
		}
		else if ( (aMast!=null) &&  
				( (aMast==getSignalMastFromName(levelXing.getSignalBMastName())) ||
					(aMast==getSignalMastFromName(levelXing.getSignalCMastName())) ||
					(aMast==getSignalMastFromName(levelXing.getSignalDMastName())) ) ) {
// need to figure out what to do in this case.			
		}
		else if (aMast==null) {
			removeSignalMastFromPanel(levelXing.getSignalAMastName());
			levelXing.setSignalAMastName("");		
		}
		if ( (bMast!=null) && xingSignalMastB.addToPanel()) {
			if (isSignalMastOnPanel(bMast) && 
					(bMast!=getSignalMastFromName(levelXing.getSignalBMastName()))) { 
				JOptionPane.showMessageDialog(signalMastsAtXingFrame,
					java.text.MessageFormat.format(rb.getString("SignalMastsError6"),
						new Object[]{xingSignalMastB.getText()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else {
				removeSignalMastFromPanel(levelXing.getSignalBMastName());
                SignalMastIcon l = new SignalMastIcon(layoutEditor);
                l.setSignalMast(xingSignalMastB.getText());
				placeXingBIcon(l, xingSignalMastB.isRightSelected(), 0.0);
				removeAssignment(bMast);
				levelXing.setSignalBMastName(xingSignalMastB.getText());
				needRedraw = true;
			}		
		}
		else if ( (bMast!=null) && 
				(bMast!=getSignalMastFromName(levelXing.getSignalAMastName())) &&
				(bMast!=getSignalMastFromName(levelXing.getSignalBMastName())) &&
				(bMast!=getSignalMastFromName(levelXing.getSignalCMastName())) &&
				(bMast!=getSignalMastFromName(levelXing.getSignalDMastName())) ) {
			if (isSignalMastOnPanel(bMast)) {
				JOptionPane.showMessageDialog(signalMastsAtXingFrame,
					java.text.MessageFormat.format(rb.getString("SignalMastsError13"),
						new Object[]{xingSignalMastB.getText()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}		
			else {
				removeSignalMastFromPanel(levelXing.getSignalBMastName());
				removeAssignment(bMast);
				levelXing.setSignalBMastName(xingSignalMastB.getText());
			}
		}
		else if ( (bMast!=null) &&  
				( (bMast==getSignalMastFromName(levelXing.getSignalAMastName())) ||
					(bMast==getSignalMastFromName(levelXing.getSignalCMastName())) ||
					(bMast==getSignalMastFromName(levelXing.getSignalDMastName())) ) ) {
// need to figure out what to do in this case.			
		}
		else if (bMast==null) {
			removeSignalMastFromPanel(levelXing.getSignalBMastName());
			levelXing.setSignalBMastName("");		
		}
		if ( (cMast!=null) && xingSignalMastC.addToPanel() ) {
			if (isSignalMastOnPanel(cMast) && 
					(cMast!=getSignalMastFromName(levelXing.getSignalCMastName())) ) { 
				JOptionPane.showMessageDialog(signalMastsAtXingFrame,
					java.text.MessageFormat.format(rb.getString("SignalMastsError6"),
						new Object[]{xingSignalMastC.getText()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else {
				removeSignalMastFromPanel(levelXing.getSignalCMastName());
                SignalMastIcon l = new SignalMastIcon(layoutEditor);
                l.setSignalMast(xingSignalMastC.getText());
				placeXingCIcon(l, xingSignalMastA.isRightSelected(), 0.0);
				removeAssignment(cMast);
				levelXing.setSignalCMastName(xingSignalMastC.getText());
				needRedraw = true;
			}		
		}
		else if ( (cMast!=null) && 
				(cMast!=getSignalMastFromName(levelXing.getSignalAMastName())) &&
				(cMast!=getSignalMastFromName(levelXing.getSignalBMastName())) &&
				(cMast!=getSignalMastFromName(levelXing.getSignalCMastName())) &&
				(cMast!=getSignalMastFromName(levelXing.getSignalDMastName())) ) {
			if (isSignalMastOnPanel(cMast)) {
				JOptionPane.showMessageDialog(signalMastsAtXingFrame,
					java.text.MessageFormat.format(rb.getString("SignalMastsError13"),
						new Object[]{xingSignalMastC.getText()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}		
			else {
				removeSignalMastFromPanel(levelXing.getSignalCMastName());
				removeAssignment(cMast);
				levelXing.setSignalCMastName(xingSignalMastC.getText());
			}
		}
		else if ( (cMast!=null) &&  
				( (cMast==getSignalMastFromName(levelXing.getSignalBMastName())) ||
					(cMast==getSignalMastFromName(levelXing.getSignalAMastName())) ||
					(cMast==getSignalMastFromName(levelXing.getSignalDMastName())) ) ) {
// need to figure out what to do in this case.			
		}
		else if (cMast==null) {
			removeSignalMastFromPanel(levelXing.getSignalCMastName());
			levelXing.setSignalCName("");		
		}
		if ( (dMast!=null) && xingSignalMastD.addToPanel() ) {
			if (isSignalMastOnPanel(dMast) && 
					(dMast!=getSignalMastFromName(levelXing.getSignalDMastName())) ) { 
				JOptionPane.showMessageDialog(signalMastsAtXingFrame,
					java.text.MessageFormat.format(rb.getString("SignalMastsError6"),
						new Object[]{xingSignalMastD.getText()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else {
				removeSignalMastFromPanel(levelXing.getSignalDMastName());
                SignalMastIcon l = new SignalMastIcon(layoutEditor);
                l.setSignalMast(xingSignalMastD.getText());
				placeXingDIcon(l, xingSignalMastD.isRightSelected(), 0.0);
				removeAssignment(dMast);
				levelXing.setSignalDMastName(xingSignalMastD.getText());
				needRedraw = true;
			}		
		}
		else if ( (dMast!=null) && 
				(dMast!=getSignalMastFromName(levelXing.getSignalAMastName())) &&
				(dMast!=getSignalMastFromName(levelXing.getSignalBMastName())) &&
				(dMast!=getSignalMastFromName(levelXing.getSignalCMastName())) &&
				(dMast!=getSignalMastFromName(levelXing.getSignalDMastName())) ) {
			if (isSignalMastOnPanel(dMast)) {
				JOptionPane.showMessageDialog(signalMastsAtXingFrame,
					java.text.MessageFormat.format(rb.getString("SignalMastsError13"),
						new Object[]{xingSignalMastD.getText()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}		
			else {
				removeSignalMastFromPanel(levelXing.getSignalDMastName());
				removeAssignment(dMast);
				levelXing.setSignalDMastName(xingSignalMastD.getText());
			}
		}
		else if ( (dMast!=null) &&  
				( (dMast==getSignalMastFromName(levelXing.getSignalBMastName())) ||
					(dMast==getSignalMastFromName(levelXing.getSignalCMastName())) ||
					(dMast==getSignalMastFromName(levelXing.getSignalAMastName())) ) ) {
// need to figure out what to do in this case.			
		}
		else if (dMast==null) {
			removeSignalMastFromPanel(levelXing.getSignalDMastName());
			levelXing.setSignalDMastName("");		
		}
		// setup logic if requested
		// finish up
		setSignalMastsAtXingOpen = false;
		signalMastsAtXingFrame.setVisible(false);
		xingMastFromMenu = false;	
		if (needRedraw) {
			layoutEditor.redrawPanel();
			needRedraw = false;
			layoutEditor.setDirty();
		}		
	}

    private void placeXingAIcon(PositionableIcon icon, boolean right, double fromPoint) {
		
        if(levelXing.getConnectA() instanceof TrackSegment){
            TrackSegment t = (TrackSegment) levelXing.getConnectA();
            Point2D p = levelXing.getCoordsA();
            
            Point2D end;
            if(t.getConnect1()==levelXing){
                end = layoutEditor.getEndCoords(t.getConnect2(), t.getType2());
            
            } else {
                end = layoutEditor.getEndCoords(t.getConnect1(), t.getType1());
            }

            boolean east = false;
            
            if(end.getX()<p.getX())
                east = true;

            setIconOnPanel(t, icon, east, p, end, right, fromPoint);
            return;
        }
	}
	private void placeXingBIcon(PositionableIcon icon, boolean right, double fromPoint) {

        if(levelXing.getConnectB() instanceof TrackSegment){
            TrackSegment t = (TrackSegment) levelXing.getConnectB();
            Point2D p = levelXing.getCoordsB();
            
            Point2D end;
            if(t.getConnect1()==levelXing){
                end = layoutEditor.getEndCoords(t.getConnect2(), t.getType2());
            
            } else {
                end = layoutEditor.getEndCoords(t.getConnect1(), t.getType1());
            }
            boolean east = false;
            
            if(end.getX()<p.getX())
                east = true;

            setIconOnPanel(t, icon, east, p, end, right, fromPoint);
            return;
        }
	}
	private void placeXingCIcon(PositionableIcon icon, boolean right, double fromPoint) {
        if(levelXing.getConnectC() instanceof TrackSegment) {
            TrackSegment t = (TrackSegment) levelXing.getConnectC();
            Point2D p = levelXing.getCoordsC();
            
            Point2D end;
            if(t.getConnect1()==levelXing){
                end = layoutEditor.getEndCoords(t.getConnect2(), t.getType2());
            
            } else {
                end = layoutEditor.getEndCoords(t.getConnect1(), t.getType1());
            }
            boolean east = false;
            
            if(end.getX()<p.getX())
                east = true;

            setIconOnPanel(t, icon, east, p, end, right, fromPoint);
            return;
        }
	}

	private void placeXingDIcon(PositionableIcon icon, boolean right, double fromPoint) {
        if(levelXing.getConnectD() instanceof TrackSegment){
            TrackSegment t = (TrackSegment) levelXing.getConnectD();
            Point2D p = levelXing.getCoordsD();
            
            Point2D end;
            if(t.getConnect1()==levelXing){
                end = layoutEditor.getEndCoords(t.getConnect2(), t.getType2());
            
            } else {
                end = layoutEditor.getEndCoords(t.getConnect1(), t.getType1());
            }
            //TrackSegment t = boundary.getConnect2();
            boolean east = false;
            
            if(end.getX()<p.getX())
                east = true;

            setIconOnPanel(t, icon, east, p, end, right, fromPoint);
            return;
        }
	}	// initialize logging
    
    boolean setSensorsOpen =false;
    boolean turnoutSensorFromMenu = false;
    private JmriJFrame setSensorsFrame = null;
    private JFrame turnoutSensorFrame = null;

    private JTextField turnoutSensorNameField = new JTextField(16);
    private JButton setSensorsDone;
    private JButton getSavedSensors;
    private JButton setSensorsCancel;
    private JButton changeSensorIcon = null;

    private String[] turnoutSenBlocks = new String[4];
    
    BeanDetails turnoutSensorA;
    BeanDetails turnoutSensorB;
    BeanDetails turnoutSensorC;
    BeanDetails turnoutSensorD;
    
    JPanel sensorTurnoutPanel = new JPanel();

    public void setSensorsAtTurnoutFromMenu(LayoutTurnout to, String[] blocks, MultiIconEditor theEditor, JFrame frame){
    	turnoutSensorFromMenu = true;
        sensorIconEditor = theEditor;
		layoutTurnout = to;
		turnout = to.getTurnout();
        //turnoutMastNameField.setText(to.getTurnoutName());
		turnoutSensorNameField.setText(to.getTurnoutName());
        turnoutSenBlocks = new String[4];
        for(int i = 0; i<blocks.length; i++){
            turnoutSenBlocks[i]=blocks[i];
        }
		setSensorsAtTurnouts(frame);
    }
    
    public void setSensorsAtTurnouts(JFrame frame){
		turnoutSensorFrame = frame;
		if (setSensorsOpen) {
            turnoutSensorsGetSaved(null);
			setSensorsFrame.setVisible(true);
			return;
		}
		// Initialize if needed
		if (setSensorsFrame == null) {
            setSensorsFrame = new JmriJFrame( rb.getString("SensorsAtTurnout"), false, true );
            setSensorsFrame.addHelpMenu("package.jmri.jmrit.display.SetSensorsAtTurnout", true);
            setSensorsFrame.setLocation(70,30);
            Container theContentPane = setSensorsFrame.getContentPane();        
            theContentPane.setLayout(new BoxLayout(theContentPane, BoxLayout.Y_AXIS));
			JPanel panel1 = new JPanel(); 
            panel1.setLayout(new FlowLayout());
            turnoutSensorA = new BeanDetails("Sensor", jmri.InstanceManager.sensorManagerInstance());
            turnoutSensorB = new BeanDetails("Sensor", jmri.InstanceManager.sensorManagerInstance());
            turnoutSensorC = new BeanDetails("Sensor", jmri.InstanceManager.sensorManagerInstance());
            turnoutSensorD = new BeanDetails("Sensor", jmri.InstanceManager.sensorManagerInstance());
            
			if (turnoutSensorFromMenu) {
				JLabel turnoutSensorNameLabel = new JLabel( rb.getString("Turnout")+" "+
					rb.getString("Name")+" : "+layoutTurnout.getTurnoutName());
				panel1.add(turnoutSensorNameLabel);
                turnoutSensorA.setTextField(layoutTurnout.getSensorA());
                turnoutSensorB.setTextField(layoutTurnout.getSensorB());
                turnoutSensorC.setTextField(layoutTurnout.getSensorC());
                turnoutSensorD.setTextField(layoutTurnout.getSensorD());
			}
			else {
				JLabel turnoutSensorNameLabel = new JLabel( rb.getString("Turnout")+" "+
																rb.getString("Name") );
				panel1.add(turnoutSensorNameLabel);
				panel1.add(turnoutSensorNameField);
				turnoutSensorNameField.setToolTipText(rb.getString("SensorsTurnoutNameHint"));
			}
            theContentPane.add(panel1);
			theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));
            JPanel panel2 = new JPanel();
            panel2.setLayout(new FlowLayout());
			JLabel shTitle = new JLabel(rb.getString("Sensors"));
			panel2.add(shTitle);
			panel2.add(new JLabel("   "));
            panel2.add(getSavedSensors = new JButton(rb.getString("GetSaved")));
            getSavedSensors.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						turnoutSensorsGetSaved(e);
					}
				});
            getSavedSensors.setToolTipText( rb.getString("GetSavedHint") );
			theContentPane.add(panel2);
            
            turnoutSensorA.getDetailsPanel().setBackground(new Color(255,255,200));

            turnoutSensorB.getDetailsPanel().setBackground(new Color(200,255,255));
            
            turnoutSensorC.getDetailsPanel().setBackground(new Color(200,200,255));

            turnoutSensorD.getDetailsPanel().setBackground(new Color(255,200,200));
            
            sensorTurnoutPanel.setLayout(new GridLayout(0,2));
            
            turnoutSensorA.setBoundaryLabel(turnoutSenBlocks[0]);
            turnoutSensorB.setBoundaryLabel(turnoutSenBlocks[1]);
            turnoutSensorC.setBoundaryLabel(turnoutSenBlocks[2]);
            turnoutSensorD.setBoundaryLabel(turnoutSenBlocks[3]);
        
            if(turnoutSenBlocks[0]!=null)
                sensorTurnoutPanel.add(turnoutSensorA.getDetailsPanel());
            if(turnoutSenBlocks[1]!=null)
                sensorTurnoutPanel.add(turnoutSensorB.getDetailsPanel());
            if(turnoutSenBlocks[2]!=null)
                sensorTurnoutPanel.add(turnoutSensorC.getDetailsPanel());
            if(turnoutSenBlocks[3]!=null)
                sensorTurnoutPanel.add(turnoutSensorD.getDetailsPanel());
            theContentPane.add(sensorTurnoutPanel);
            
			theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));
            JPanel panel6 = new JPanel();
            panel6.setLayout(new FlowLayout());
            panel6.add(changeSensorIcon = new JButton(rb.getString("ChangeSensorIcon")));
            changeSensorIcon.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						turnoutSensorFrame.setVisible(true);
					}
				});
            changeSensorIcon.setToolTipText( rb.getString("ChangeSensorIconHint") );
			panel6.add(new JLabel("  "));
            panel6.add(setSensorsDone = new JButton(rb.getString("Done")));
            setSensorsDone.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setSensorsDonePressed(e);
                }
            });
            setSensorsDone.setToolTipText( rb.getString("SensorDoneHint") );
            panel6.add(setSensorsCancel = new JButton(rb.getString("Cancel")));
            setSensorsCancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setSensorsCancelPressed(e);
                }
            });
            setSensorsCancel.setToolTipText( rb.getString("CancelHint") );
            theContentPane.add(panel6);
			setSensorsFrame.addWindowListener(new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent e) {
					setSensorsCancelPressed(null);
				}
			});
		}
        if (turnoutFromMenu) turnoutSensorsGetSaved(null);
        setSensorsFrame.setPreferredSize(null);
        setSensorsFrame.pack();
        setSensorsFrame.setVisible(true);		
		setSensorsOpen = true;
    }
    
    private void turnoutSensorsGetSaved (ActionEvent a) {
		if ( !getTurnoutSensorInformation() ) return;
        turnoutSenBlocks = layoutTurnout.getBlockBoundaries();
		turnoutSensorA.setTextField(layoutTurnout.getSensorA());
		turnoutSensorB.setTextField(layoutTurnout.getSensorB());
		turnoutSensorC.setTextField(layoutTurnout.getSensorC());
		turnoutSensorD.setTextField(layoutTurnout.getSensorD());
        
        turnoutSensorA.setBoundaryLabel(turnoutSenBlocks[0]);
        turnoutSensorB.setBoundaryLabel(turnoutSenBlocks[1]);
        turnoutSensorC.setBoundaryLabel(turnoutSenBlocks[2]);
        turnoutSensorD.setBoundaryLabel(turnoutSenBlocks[3]);
        
        sensorTurnoutPanel.remove(turnoutSensorA.getDetailsPanel());
        sensorTurnoutPanel.remove(turnoutSensorB.getDetailsPanel());
        sensorTurnoutPanel.remove(turnoutSensorC.getDetailsPanel());
        sensorTurnoutPanel.remove(turnoutSensorD.getDetailsPanel());
        
        boolean blockBoundary = false;
        if(turnoutSenBlocks[0]!=null){
            sensorTurnoutPanel.add(turnoutSensorA.getDetailsPanel());
            blockBoundary = true;
        }
        if(turnoutSenBlocks[1]!=null){
            sensorTurnoutPanel.add(turnoutSensorB.getDetailsPanel());
            blockBoundary = true;
        }
        if(turnoutSenBlocks[2]!=null){
            sensorTurnoutPanel.add(turnoutSensorC.getDetailsPanel());
            blockBoundary = true;
        }
        if(turnoutSenBlocks[3]!=null){
            sensorTurnoutPanel.add(turnoutSensorD.getDetailsPanel());
            blockBoundary = true;
        }
        if (!blockBoundary)
            JOptionPane.showMessageDialog(setSensorsFrame, "There are no block boundaries on this turnout\nIt is therefore not possible to add Sensors to it");
        setSensorsFrame.setPreferredSize(null);
        setSensorsFrame.pack();
	}
    
    private int isSensorAssignedHere(Sensor sensor, LayoutTurnout lTurnout) {
        if ((sensor==null) || (lTurnout==null))
            return NONE;
		String sysName = sensor.getSystemName();
		String uName = sensor.getUserName();
		String name = lTurnout.getSensorA();
		if ( (name!=null) && (name.length()>0) && ((name.equals(uName)) || 
						(name.equals(sysName))) ) return A1;
		name = lTurnout.getSensorB();
		if ( (name!=null) && (name.length()>0) && ((name.equals(uName)) || 
						(name.equals(sysName))) ) return A2;
		name = lTurnout.getSensorC();
		if ( (name!=null) && (name.length()>0) && ((name.equals(uName)) || 
						(name.equals(sysName))) ) return A3;
		name = lTurnout.getSensorD();
		if ( (name!=null) && (name.length()>0) && ((name.equals(uName)) || 
						(name.equals(sysName))) ) return B1;
		return NONE;
	}
    
    public void removeAssignment(Sensor sensor) 
	{
		String sName = sensor.getSystemName();
		String uName = sensor.getUserName();
		for (int i=0;i<layoutEditor.turnoutList.size();i++) {
			LayoutTurnout to = layoutEditor.turnoutList.get(i);
			if ((to.getSensorA()!=null) &&
					(to.getSensorA().equals(sName) || ((uName!=null) && 
					(to.getSensorA().equals(uName))))) to.setSensorA("");
			if ((to.getSensorB()!=null) &&
					(to.getSensorB().equals(sName) || ((uName!=null) && 
					(to.getSensorB().equals(uName))))) to.setSensorB("");
			if ((to.getSensorC()!=null) &&
					(to.getSensorC().equals(sName) || ((uName!=null) && 
					(to.getSensorC().equals(uName))))) to.setSensorC("");
			if ((to.getSensorD()!=null) &&
					(to.getSensorD().equals(sName) || ((uName!=null) && 
					(to.getSensorD().equals(uName))))) to.setSensorD("");
		}
		for (int i=0;i<layoutEditor.pointList.size();i++) {
			PositionablePoint po = layoutEditor.pointList.get(i);
			if ((po.getEastBoundSensor()!=null) &&
					(po.getEastBoundSensor().equals(sName) || ((uName!=null) && 
					(po.getEastBoundSensor().equals(uName))))) 
				po.setEastBoundSensor("");
			if ((po.getWestBoundSensor()!=null) &&
					(po.getWestBoundSensor().equals(sName) || ((uName!=null) && 
					(po.getWestBoundSensor().equals(uName))))) 
				po.setWestBoundSensor("");
		}
		for (int i=0;i<layoutEditor.xingList.size();i++) {
			LevelXing x = layoutEditor.xingList.get(i);
			if ((x.getSensorAName()!=null) &&
					(x.getSensorAName().equals(sName) || ((uName!=null) && 
					(x.getSensorAName().equals(uName))))) x.setSensorAName("");
			if ((x.getSensorBName()!=null) &&
					(x.getSensorBName().equals(sName) || ((uName!=null) && 
					(x.getSensorBName().equals(uName))))) x.setSensorBName("");
			if ((x.getSensorCName()!=null) &&
					(x.getSensorCName().equals(sName) || ((uName!=null) && 
					(x.getSensorCName().equals(uName))))) x.setSensorCName("");
			if ((x.getSensorDName()!=null) &&
					(x.getSensorDName().equals(sName) || ((uName!=null) && 
					(x.getSensorDName().equals(uName))))) x.setSensorDName("");
		}
	}

    SensorIcon turnoutSensorBlockIcon;
    
    private void setSensorsDonePressed (ActionEvent a) {
    //Placing of turnouts needs to be better handled
		// process turnout name
		if ( !getTurnoutSensorInformation() ) return;
		// process signal head names
		//if ( !getSensorTurnoutInformation() ) return;
        Sensor sensorA = getSensorFromEntry(turnoutSensorA.getText(),false,setSensorsFrame);
		//if (turnoutSensor==null) return false;
		Sensor sensorB = getSensorFromEntry(turnoutSensorB.getText(),false,setSensorsFrame);
        //if (turnoutSensorB==null) return false;
		Sensor sensorC = getSensorFromEntry(turnoutSensorC.getText(),false,setSensorsFrame);
		//if (turnoutSensorC==null) return false;
		Sensor sensorD = getSensorFromEntry(turnoutSensorD.getText(),false,setSensorsFrame);
		// place signals as requested
		if (turnoutSensorA.addToPanel()) {
			if (isSensorOnPanel(sensorA) &&
				(sensorA!=getSensorFromName(layoutTurnout.getSensorA()))) {
				JOptionPane.showMessageDialog(setSensorsFrame,
					java.text.MessageFormat.format(rb.getString("SensorsError6"),
						new Object[]{turnoutSensorA.getText()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else {
				removeSensorFromPanel(layoutTurnout.getSensorA());
				placingBlock(getSensorIcon(turnoutSensorA.getText()), turnoutSensorA.isRightSelected(), 0.0);
				removeAssignment(sensorA);
				layoutTurnout.setSensorA(turnoutSensorA.getText());
				needRedraw = true;
			}		
		}
		else if (sensorA!=null){
			int assigned = isSensorAssignedHere(sensorA,layoutTurnout);
			if (assigned == NONE) {
				if ( isSensorOnPanel(sensorA) && 
									isSensorAssignedAnywhere(sensorA) ) {
					JOptionPane.showMessageDialog(setSensorsFrame,
						java.text.MessageFormat.format(rb.getString("SensorsError8"),
							new Object[]{turnoutSensorA.getText()}), 
								rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
					return;
				}		
				else {
					removeSensorFromPanel(layoutTurnout.getSensorA());
					removeAssignment(sensorA);
					layoutTurnout.setSensorA(turnoutSensorA.getText());
				}
			}
			else if (assigned!=A1) {
// need to figure out what to do in this case.			
			}
		}
        else {
			removeSensorFromPanel(layoutTurnout.getSensorA());
			layoutTurnout.setSensorA("");
		}
		if ( (turnoutSensorB!=null) && (turnoutSensorB.addToPanel())) {
			if (isSensorOnPanel(sensorB) && 
				(sensorB!=getSensorFromName(layoutTurnout.getSensorB()))) {
				JOptionPane.showMessageDialog(setSensorsFrame,
					java.text.MessageFormat.format(rb.getString("SensorsError6"),
						new Object[]{turnoutSensorB.getText()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else {
				removeSensorFromPanel(layoutTurnout.getSensorB());

				placingBlockB(getSensorIcon(turnoutSensorB.getText()), turnoutSensorB.isRightSelected(), 0.0);
				removeAssignment(sensorB);
				layoutTurnout.setSensorB(turnoutSensorB.getText());
				needRedraw = true;
			}		
		}
		else if (sensorB!=null) {
			int assigned = isSensorAssignedHere(sensorB,layoutTurnout);
			if (assigned == NONE) {
				if (isSensorOnPanel(sensorB) && 
									isSensorAssignedAnywhere(sensorB) ) {
					JOptionPane.showMessageDialog(setSensorsFrame,
						java.text.MessageFormat.format(rb.getString("SensorsError8"),
							new Object[]{turnoutSensorB.getText()}), 
								rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
					return;
				}		
				else {
					removeSensorFromPanel(layoutTurnout.getSensorB());
					removeAssignment(sensorB);
					layoutTurnout.setSensorB(turnoutSensorB.getText());
				}
			}
			else if (assigned!=A2) {
// need to figure out what to do in this case.			
			}
		}
		else {
			removeSensorFromPanel(layoutTurnout.getSensorB());
			layoutTurnout.setSensorB("");
		}
        if(sensorC!=null){
            if (turnoutSensorC.addToPanel()) {
                if (isSensorOnPanel(sensorC) && 
                    (sensorC!=getSensorFromName(layoutTurnout.getSensorC()))) {
                    JOptionPane.showMessageDialog(setSensorsFrame,
                        java.text.MessageFormat.format(rb.getString("SensorsError6"),
                            new Object[]{turnoutSensorC.getText()}), 
                                rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
                    return;
                }
                else {
                    removeSensorFromPanel(layoutTurnout.getSensorC());

                    placingBlockC(getSensorIcon(turnoutSensorC.getText()), turnoutSensorC.isRightSelected(), 0.0);
                    removeAssignment(sensorC);
                    layoutTurnout.setSensorC(turnoutSensorC.getText());
                    needRedraw = true;
                }		
            }
            else {
                int assigned = isSensorAssignedHere(sensorC,layoutTurnout);
                if (assigned == NONE) {
                    if (isSensorOnPanel(sensorC)  && 
                                        isSensorAssignedAnywhere(sensorC) ) {
                        JOptionPane.showMessageDialog(setSensorsFrame,
                            java.text.MessageFormat.format(rb.getString("SensorsError8"),
                                new Object[]{turnoutSensorC.getText()}), 
                                    rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
                        return;
                    }		
                    else {
                        removeSensorFromPanel(layoutTurnout.getSensorC());
                        removeAssignment(sensorC);
                        layoutTurnout.setSensorC(turnoutSensorC.getText());
                    }
                }
                else if (assigned!=A3) {
    // need to figure out what to do in this case.			
                }
            }
        }
        else {
            removeSensorFromPanel(layoutTurnout.getSensorC());
			layoutTurnout.setSensorC("");
        }
        if(sensorD!=null){
            if (turnoutSensorD.addToPanel()) {
                if (isSensorOnPanel(sensorD) && 
                    (sensorD!=getSensorFromName(layoutTurnout.getSensorD()))) {
                    JOptionPane.showMessageDialog(setSensorsFrame,
                        java.text.MessageFormat.format(rb.getString("SensorsError6"),
                            new Object[]{divergingField.getText()}), 
                                rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
                    return;
                }
                else {
                    removeSensorFromPanel(layoutTurnout.getSensorD());
                    placingBlockD(getSensorIcon(turnoutSensorD.getText()), turnoutSensorD.isRightSelected(), 0.0);
                    removeAssignment(sensorD);
                    layoutTurnout.setSensorD(turnoutSensorD.getText());
                    needRedraw = true;
                }		
            }
            else {
                int assigned = isSensorAssignedHere(sensorD,layoutTurnout);
                if (assigned == NONE) {
                    if (isSensorOnPanel(sensorD) && 
                                        isSensorAssignedAnywhere(sensorD) ) {
                        JOptionPane.showMessageDialog(setSensorsFrame,
                            java.text.MessageFormat.format(rb.getString("SensorsError8"),
                                new Object[]{turnoutSensorD.getText()}), 
                                    rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
                        return;
                    }		
                    else {
                        removeSensorFromPanel(layoutTurnout.getSensorD());
                        removeAssignment(sensorD);
                        layoutTurnout.setSensorD(turnoutSensorD.getText());
                    }
                }
                else if (assigned!=B1) {
    // need to figure out what to do in this case.			
                }
            }
        } else {
            removeSensorFromPanel(layoutTurnout.getSensorD());
			layoutTurnout.setSensorD("");
        }

		// make sure this layout turnout is not linked to another
		layoutTurnout.setLinkType(LayoutTurnout.NO_LINK);
		layoutTurnout.setLinkedTurnoutName("");
		// finish up
		setSensorsOpen = false;
		turnoutFromMenu = false;
		setSensorsFrame.setVisible(false);
		if (needRedraw) {
			layoutEditor.redrawPanel();
			needRedraw = false;
			layoutEditor.setDirty();
		}
	}

    private boolean getTurnoutSensorInformation(){
        LayoutTurnout t = null;
		String str = "";
        turnout = null;
        layoutTurnout = null;
        str = turnoutSensorNameField.getText();
        if ( (str==null) || (str.equals("")) ) {
            JOptionPane.showMessageDialog(setSensorsFrame,rb.getString("SensorsError1"),
                                rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
            return false;
        }
        turnout = jmri.InstanceManager.turnoutManagerInstance().getTurnout(str);
        if (turnout==null) {
            JOptionPane.showMessageDialog(setSensorsFrame,
                java.text.MessageFormat.format(rb.getString("SensorsError2"),
                    new Object[]{str}), rb.getString("Error"),
                        JOptionPane.ERROR_MESSAGE);
            return false ;
        }
        else if ( (turnout.getUserName()==null) || (turnout.getUserName().equals("")) ||
                                !turnout.getUserName().equals(str) ) {
            turnoutSensorNameField.setText(str);
        }
        for (int i=0;i<layoutEditor.turnoutList.size();i++) {
            t = layoutEditor.turnoutList.get(i);
            if (t.getTurnout() == turnout) {
                layoutTurnout = t;
            }
        }
        
		t = layoutTurnout;
		if (t==null) {
		JOptionPane.showMessageDialog(setSensorsFrame,
				java.text.MessageFormat.format(rb.getString("SensorsError3"),
						new Object[]{str}), rb.getString("Error"),
							JOptionPane.ERROR_MESSAGE);
			return false;
		}
        return true;
    }
    
    private void setSensorsCancelPressed (ActionEvent a) {
		setSensorsOpen =false;
		turnoutSensorFromMenu = false;
		setSensorsFrame.setVisible(false);
	}
    
     	// operational variables for Set Sensors at Level Crossing tool
	private JmriJFrame sensorsAtXingFrame = null;
	private boolean setSensorsAtXingOpen = false;
	private JTextField blockANameSensorField = new JTextField(16);
	private JTextField blockCNameSensorField = new JTextField(16);

	private JButton getSavedXingSensors = null;
	private JButton setXingSensorsDone = null;
	private JButton setXingSensorsCancel = null;
    private JButton changeSensorXingIcon = null;
    JFrame sensorXingFrame = null;

    private boolean xingSensorFromMenu = false;
    private String[] xingSensorBlocks = new String[4];
    
    BeanDetails xingSensorA;
    BeanDetails xingSensorB;
    BeanDetails xingSensorC;
    BeanDetails xingSensorD;
    
    JPanel sensorXingPanel = new JPanel();
    
	// display dialog for Set Signals at Level Crossing tool
	public void setSensorsAtLevelXingFromMenu (LevelXing xing, String[] blocks, MultiIconEditor theEditor,
			JFrame theFrame ) {
		xingSensorFromMenu = true;
		levelXing = xing;
		blockANameSensorField.setText(levelXing.getBlockNameAC());
		blockCNameSensorField.setText(levelXing.getBlockNameBD());
        for(int i = 0; i<blocks.length; i++){
            xingSensorBlocks[i]=blocks[i];
        }
        setSensorsAtLevelXing(theEditor, theFrame);
		return;
	}
       
    public void setSensorsAtLevelXing(MultiIconEditor theEditor, JFrame theFrame) {
        sensorIconEditor = theEditor;
		sensorXingFrame = theFrame;
		if (setSensorsAtXingOpen) {
            xingSensorsGetSaved(null);
			sensorsAtXingFrame.setVisible(true);
			return;
		}
		// Initialize if needed
		if (sensorsAtXingFrame == null) {
            sensorsAtXingFrame = new JmriJFrame( rb.getString("SensorsAtLevelXing"), false, true );
            sensorsAtXingFrame.addHelpMenu("package.jmri.jmrit.display.SetSensorsAtLevelXing", true);
            sensorsAtXingFrame.setLocation(70,30);
            Container theContentPane = sensorsAtXingFrame.getContentPane();
            theContentPane.setLayout(new BoxLayout(theContentPane, BoxLayout.Y_AXIS));
			JPanel panel11 = new JPanel(); 
            panel11.setLayout(new FlowLayout());
            xingSensorA = new BeanDetails("Sensor", jmri.InstanceManager.sensorManagerInstance());
            xingSensorB = new BeanDetails("Sensor", jmri.InstanceManager.sensorManagerInstance());
            xingSensorC = new BeanDetails("Sensor", jmri.InstanceManager.sensorManagerInstance());
            xingSensorD = new BeanDetails("Sensor", jmri.InstanceManager.sensorManagerInstance());
			if (xingSensorFromMenu) {
				JLabel blockANameLabel = new JLabel( rb.getString("BlockAtA")+" "+
							rb.getString("Name")+" : "+levelXing.getBlockNameAC());
			
				panel11.add(blockANameLabel);
                xingSensorA.setTextField(levelXing.getSensorAName());
                xingSensorB.setTextField(levelXing.getSensorBName());
                xingSensorC.setTextField(levelXing.getSensorCName());
                xingSensorD.setTextField(levelXing.getSensorDName());
			}
			else {
				JLabel blockANameLabel = new JLabel( rb.getString("BlockAtA")+" "+
																rb.getString("Name")+" : ");
				panel11.add(blockANameLabel);
				panel11.add(blockANameSensorField);
				blockANameSensorField.setToolTipText(rb.getString("SensorsBlockNameHint"));
			}
            theContentPane.add(panel11);
			JPanel panel12 = new JPanel(); 
            panel12.setLayout(new FlowLayout());
			if (xingSensorFromMenu) {
				JLabel blockCNameLabel = new JLabel( rb.getString("BlockAtC")+" "+
							rb.getString("Name")+" : "+levelXing.getBlockNameBD());
			
				panel12.add(blockCNameLabel);
			}
			else {
				JLabel blockCNameLabel = new JLabel( rb.getString("BlockAtC")+" "+
																rb.getString("Name")+" : ");
				panel12.add(blockCNameLabel);
				panel12.add(blockCNameSensorField);
				blockCNameSensorField.setToolTipText(rb.getString("SensorsBlockNameHint"));
			}
            theContentPane.add(panel12);
			theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));
            JPanel panel2 = new JPanel();
            panel2.setLayout(new FlowLayout());
			JLabel shTitle = new JLabel(rb.getString("Sensor"));
			panel2.add(shTitle);
			panel2.add(new JLabel("   "));
            panel2.add(getSavedXingSensors = new JButton(rb.getString("GetSaved")));
            getSavedXingSensors.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						xingSensorsGetSaved(e);
					}
				});
            getSavedXingSensors.setToolTipText( rb.getString("GetSavedHint") );			
			theContentPane.add(panel2);

            xingSensorA.getDetailsPanel().setBackground(new Color(255,255,200));

            xingSensorB.getDetailsPanel().setBackground(new Color(200,255,255));

            xingSensorC.getDetailsPanel().setBackground(new Color(200,200,255));

            xingSensorD.getDetailsPanel().setBackground(new Color(255,200,200));
            
            sensorXingPanel.setLayout(new GridLayout(0,2));
            
            xingSensorA.setBoundaryLabel(xingSensorBlocks[0]);
            xingSensorB.setBoundaryLabel(xingSensorBlocks[1]);
            xingSensorC.setBoundaryLabel(xingSensorBlocks[2]);
            xingSensorD.setBoundaryLabel(xingSensorBlocks[3]);
            
            if(xingSensorBlocks[0]!=null)
                sensorXingPanel.add(xingSensorA.getDetailsPanel());
            if(xingSensorBlocks[1]!=null)
                sensorXingPanel.add(xingSensorB.getDetailsPanel());
            if(xingSensorBlocks[2]!=null)
                sensorXingPanel.add(xingSensorC.getDetailsPanel());
            if(xingSensorBlocks[3]!=null)
                sensorXingPanel.add(xingSensorD.getDetailsPanel());
            theContentPane.add(sensorXingPanel);
            
			theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));
            JPanel panel6 = new JPanel();
            panel6.setLayout(new FlowLayout());
            panel6.add(changeSensorXingIcon = new JButton(rb.getString("ChangeSensorIcon")));
            changeSensorXingIcon.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						sensorXingFrame.setVisible(true);
					}
				});
            changeSensorXingIcon.setToolTipText( rb.getString("ChangeSensorIconHint") );
            
			panel6.add(new JLabel("  "));
            panel6.add(setXingSensorsDone = new JButton(rb.getString("Done")));
            setXingSensorsDone.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setXingSensorsDonePressed(e);
                }
            });
            setXingSensorsDone.setToolTipText( rb.getString("SensorDoneHint") );
            panel6.add(setXingSensorsCancel = new JButton(rb.getString("Cancel")));
            setXingSensorsCancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setXingSensorsCancelPressed(e);
                }
            });
            setXingSensorsCancel.setToolTipText( rb.getString("CancelHint") );
            theContentPane.add(panel6);
			sensorsAtXingFrame.addWindowListener(new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent e) {
					setXingSensorsCancelPressed(null);
				}
			});
		}
        if (xingSensorFromMenu) xingSensorsGetSaved(null);
        sensorsAtXingFrame.setPreferredSize(null);
        sensorsAtXingFrame.pack();
        sensorsAtXingFrame.setVisible(true);
		setSensorsAtXingOpen = true;
	}
    
	private void xingSensorsGetSaved (ActionEvent a) {
		if ( !getLevelCrossingSensorInformation() ) return;
        
        xingSensorBlocks = levelXing.getBlockBoundaries();
        xingSensorA.setTextField(levelXing.getSensorAName());
        xingSensorB.setTextField(levelXing.getSensorBName());
        xingSensorC.setTextField(levelXing.getSensorCName());
        xingSensorD.setTextField(levelXing.getSensorDName());
        
        sensorXingPanel.remove(xingSensorA.getDetailsPanel());
        sensorXingPanel.remove(xingSensorB.getDetailsPanel());
        sensorXingPanel.remove(xingSensorC.getDetailsPanel());
        sensorXingPanel.remove(xingSensorD.getDetailsPanel());

        xingSensorA.setBoundaryLabel(xingSensorBlocks[0]);
        xingSensorB.setBoundaryLabel(xingSensorBlocks[1]);
        xingSensorC.setBoundaryLabel(xingSensorBlocks[2]);
        xingSensorD.setBoundaryLabel(xingSensorBlocks[3]);

        boolean boundary = false;
        if(xingSensorBlocks[0]!=null){
            sensorXingPanel.add(xingSensorA.getDetailsPanel());
            boundary = true;
        }
        if(xingSensorBlocks[1]!=null){
            sensorXingPanel.add(xingSensorB.getDetailsPanel());
            boundary = true;
        }
        if(xingSensorBlocks[2]!=null){
            sensorXingPanel.add(xingSensorC.getDetailsPanel());
            boundary = true;
        }
        if(xingSensorBlocks[3]!=null){
            sensorXingPanel.add(xingSensorD.getDetailsPanel());
            boundary = true;
        }
        if (!boundary){
            JOptionPane.showMessageDialog(sensorsAtXingFrame, rb.getString("NoBoundaryXingSensor"));
        }
        sensorsAtXingFrame.setPreferredSize(null);
        sensorsAtXingFrame.pack();
	}
    
    private boolean getLevelCrossingSensorInformation() {
		if (!xingSensorFromMenu) {
			levelXing = null;
			if (layoutEditor.xingList.size()<=0) {
				JOptionPane.showMessageDialog(sensorsAtXingFrame,
					rb.getString("SignalsError15"),
								rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return false;
			}
			else if (layoutEditor.xingList.size()==1) {
				levelXing = layoutEditor.xingList.get(0);
			}
			else {
				LayoutBlock xingSensorBlockA = null;
				LayoutBlock xingSensorBlockC = null;
				xingSensorBlockA = getBlockFromEntry(blockANameSensorField);
				if (xingSensorBlockA==null) return false;
				if (blockCNameSensorField.getText().length()>0) {
					xingSensorBlockC = getBlockFromEntry(blockCNameSensorField);
					if (xingSensorBlockC==null) return false;
				}
				LevelXing x = null;
				int foundCount = 0;
				// make two block tests first
				if (xingSensorBlockC!=null) {
					for (int i = 0;(i<layoutEditor.xingList.size());i++) {
						x = layoutEditor.xingList.get(i);
						LayoutBlock xA = null;
						LayoutBlock xB = null;
						LayoutBlock xC = null;
						LayoutBlock xD = null;
						LayoutBlock xAC = x.getLayoutBlockAC();
						LayoutBlock xBD = x.getLayoutBlockBD();
						if (x.getConnectA()!=null) xA = ((TrackSegment)x.getConnectA()).getLayoutBlock();
						if (x.getConnectB()!=null) xB = ((TrackSegment)x.getConnectB()).getLayoutBlock();
						if (x.getConnectC()!=null) xC = ((TrackSegment)x.getConnectC()).getLayoutBlock();
						if (x.getConnectD()!=null) xD = ((TrackSegment)x.getConnectD()).getLayoutBlock();
						if ( ( (xA!=null) && (xC!=null) && ( ((xA==xingSensorBlockA)&&(xC==xingSensorBlockC)) ||
								((xA==xingSensorBlockC)&&(xC==xingSensorBlockA)) ) ) ||
								( (xB!=null) && (xD!=null) && ( ((xB==xingSensorBlockA)&&(xD==xingSensorBlockC)) ||
								((xB==xingSensorBlockC)&&(xD==xingSensorBlockA)) ) ) ) {
							levelXing = x;
							foundCount ++;
						}
						else if ( (xAC!=null) && (xBD!=null) && ( ((xAC==xingSensorBlockA) && (xBD==xingSensorBlockC)) ||
									((xAC==xingSensorBlockC) && (xBD==xingSensorBlockA)) ) ) {
							levelXing = x;
							foundCount ++;
						}				
					}
				}
				if (foundCount==0) {
					// try one block test
					for (int i = 0;(i<layoutEditor.xingList.size());i++) {
						x = layoutEditor.xingList.get(i);
						if ((xingSensorBlockA == x.getLayoutBlockAC()) || (xingSensorBlockA == x.getLayoutBlockBD())) {
							levelXing = x;
							foundCount ++;
						}				
					}
				}
				if (foundCount>1) {
					JOptionPane.showMessageDialog(sensorsAtXingFrame,
							java.text.MessageFormat.format(rb.getString("SignalsError16"),
								new Object[]{" "+foundCount+" "}), 
									rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
					return false;
				}
				if (levelXing==null) {
					JOptionPane.showMessageDialog(sensorsAtXingFrame,
								rb.getString("SignalsError17"),
										rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
					return false;
				}
			}
		}
        return true;
    }
    
	private void setXingSensorsCancelPressed (ActionEvent a) {
		setSensorsAtXingOpen = false;
		sensorsAtXingFrame.setVisible(false);
		xingSensorFromMenu = false;
	}
	private void setXingSensorsDonePressed (ActionEvent a) {
		if ( !getLevelCrossingSensorInformation() ) return;
        Sensor aSensor = getSensorFromEntry(xingSensorA.getText(),false,sensorsAtXingFrame);
		Sensor bSensor = getSensorFromEntry(xingSensorB.getText(),false,sensorsAtXingFrame);
		Sensor cSensor = getSensorFromEntry(xingSensorC.getText(),false,sensorsAtXingFrame);
		Sensor dSensor = getSensorFromEntry(xingSensorD.getText(),false,sensorsAtXingFrame);
		// place or update signals as requested
		if ( (aSensor!=null) && xingSensorA.addToPanel() ) {
			if (isSensorOnPanel(aSensor) && 
					(aSensor!=getSensorFromName(levelXing.getSensorAName())) ) { 
				JOptionPane.showMessageDialog(sensorsAtXingFrame,
					java.text.MessageFormat.format(rb.getString("SensorsError6"),
						new Object[]{xingSensorA.getText()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else {
				removeSensorFromPanel(levelXing.getSensorAName());
				placeXingAIcon(getSensorIcon(xingSensorA.getText()), xingSensorA.isRightSelected(), 0.0);
				removeAssignment(aSensor);
				levelXing.setSensorAName(xingSensorB.getText());
				needRedraw = true;
			}		
		}
		else if ( (aSensor!=null) && 
				(aSensor!=getSensorFromName(levelXing.getSensorAName())) &&
				(aSensor!=getSensorFromName(levelXing.getSensorBName())) &&
				(aSensor!=getSensorFromName(levelXing.getSensorCName())) &&
				(aSensor!=getSensorFromName(levelXing.getSensorDName())) ) {
			if (isSensorOnPanel(aSensor)) {
				JOptionPane.showMessageDialog(sensorsAtXingFrame,
					java.text.MessageFormat.format(rb.getString("SensorsError13"),
						new Object[]{xingSensorA.getText()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}		
			else {
				removeSensorFromPanel(levelXing.getSensorAName());
				removeAssignment(aSensor);
				levelXing.setSensorAName(xingSensorA.getText());
			}
		}
		else if ( (aSensor!=null) &&  
				( (aSensor==getSensorFromName(levelXing.getSensorBName())) ||
					(aSensor==getSensorFromName(levelXing.getSensorCName())) ||
					(aSensor==getSensorFromName(levelXing.getSensorDName())) ) ) {
// need to figure out what to do in this case.			
		}
		else if (aSensor==null) {
			removeSensorFromPanel(levelXing.getSensorAName());
			levelXing.setSensorAName("");		
		}
		if ( (bSensor!=null) && xingSensorB.addToPanel() ) {
			if (isSensorOnPanel(bSensor) && 
					(bSensor!=getSensorFromName(levelXing.getSensorBName()))) { 
				JOptionPane.showMessageDialog(sensorsAtXingFrame,
					java.text.MessageFormat.format(rb.getString("SensorsError6"),
						new Object[]{xingSensorB.getText()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else {
				removeSensorFromPanel(levelXing.getSensorBName());
				placeXingBIcon(getSensorIcon(xingSensorB.getText()), xingSensorB.isRightSelected(), 0.0);
				removeAssignment(bSensor);
				levelXing.setSensorBName(xingSensorB.getText());
				needRedraw = true;
			}		
		}
		else if ( (bSensor!=null) && 
				(bSensor!=getSensorFromName(levelXing.getSensorAName())) &&
				(bSensor!=getSensorFromName(levelXing.getSensorBName())) &&
				(bSensor!=getSensorFromName(levelXing.getSensorCName())) &&
				(bSensor!=getSensorFromName(levelXing.getSensorDName())) ) {
			if (isSensorOnPanel(bSensor)) {
				JOptionPane.showMessageDialog(sensorsAtXingFrame,
					java.text.MessageFormat.format(rb.getString("SensorsError13"),
						new Object[]{xingSensorB.getText()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}		
			else {
				removeSensorFromPanel(levelXing.getSensorBName());
				removeAssignment(bSensor);
				levelXing.setSensorBName(xingSensorB.getText());
			}
		}
		else if ( (bSensor!=null) &&  
				( (bSensor==getSensorFromName(levelXing.getSensorAName())) ||
					(bSensor==getSensorFromName(levelXing.getSensorCName())) ||
					(bSensor==getSensorFromName(levelXing.getSensorDName())) ) ) {
// need to figure out what to do in this case.			
		}
		else if (bSensor==null) {
			removeSensorFromPanel(levelXing.getSensorBName());
			levelXing.setSensorBName("");		
		}
		if ( (cSensor!=null) && xingSensorC.addToPanel() ) {
			if (isSensorOnPanel(cSensor) && 
					(cSensor!=getSensorFromName(levelXing.getSensorCName())) ) { 
				JOptionPane.showMessageDialog(sensorsAtXingFrame,
					java.text.MessageFormat.format(rb.getString("SensorsError6"),
						new Object[]{xingSensorC.getText()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else {
				removeSensorFromPanel(levelXing.getSensorCName());
				placeXingCIcon(getSensorIcon(xingSensorC.getText()), xingSensorC.isRightSelected(), 0.0);
				removeAssignment(cSensor);
				levelXing.setSensorCName(xingSensorC.getText());
				needRedraw = true;
			}		
		}
		else if ( (cSensor!=null) && 
				(cSensor!=getSensorFromName(levelXing.getSensorAName())) &&
				(cSensor!=getSensorFromName(levelXing.getSensorBName())) &&
				(cSensor!=getSensorFromName(levelXing.getSensorCName())) &&
				(cSensor!=getSensorFromName(levelXing.getSensorDName())) ) {
			if (isSensorOnPanel(cSensor)) {
				JOptionPane.showMessageDialog(sensorsAtXingFrame,
					java.text.MessageFormat.format(rb.getString("SensorsError13"),
						new Object[]{xingSensorC.getText()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}		
			else {
				removeSensorFromPanel(levelXing.getSensorCName());
				removeAssignment(cSensor);
				levelXing.setSensorCName(xingSensorC.getText());
			}
		}
		else if ( (cSensor!=null) &&  
				( (cSensor==getSensorFromName(levelXing.getSensorBName())) ||
					(cSensor==getSensorFromName(levelXing.getSensorAName())) ||
					(cSensor==getSensorFromName(levelXing.getSensorDName())) ) ) {
// need to figure out what to do in this case.			
		}
		else if (cSensor==null) {
			removeSensorFromPanel(levelXing.getSensorCName());
			levelXing.setSignalCName("");		
		}
		if ( (dSensor!=null) && xingSensorD.addToPanel() ) {
			if (isSensorOnPanel(dSensor) && 
					(dSensor!=getSensorFromName(levelXing.getSensorDName())) ) { 
				JOptionPane.showMessageDialog(sensorsAtXingFrame,
					java.text.MessageFormat.format(rb.getString("SensorsError6"),
						new Object[]{xingSensorD.getText()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else {
				removeSensorFromPanel(levelXing.getSensorDName());
				placeXingDIcon(getSensorIcon(xingSensorD.getText()), xingSensorD.isRightSelected(), 0.0);
				removeAssignment(dSensor);
				levelXing.setSensorDName(xingSensorD.getText());
				needRedraw = true;
			}		
		}
		else if ( (dSensor!=null) && 
				(dSensor!=getSensorFromName(levelXing.getSensorAName())) &&
				(dSensor!=getSensorFromName(levelXing.getSensorBName())) &&
				(dSensor!=getSensorFromName(levelXing.getSensorCName())) &&
				(dSensor!=getSensorFromName(levelXing.getSensorDName())) ) {
			if (isSensorOnPanel(dSensor)) {
				JOptionPane.showMessageDialog(sensorsAtXingFrame,
					java.text.MessageFormat.format(rb.getString("SensorsError13"),
						new Object[]{xingSensorD.getText()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}		
			else {
				removeSensorFromPanel(levelXing.getSensorDName());
				removeAssignment(dSensor);
				levelXing.setSensorDName(xingSensorD.getText());
			}
		}
		else if ( (dSensor!=null) &&  
				( (dSensor==getSensorFromName(levelXing.getSensorBName())) ||
					(dSensor==getSensorFromName(levelXing.getSensorCName())) ||
					(dSensor==getSensorFromName(levelXing.getSensorAName())) ) ) {
// need to figure out what to do in this case.			
		}
		else if (dSensor==null) {
			removeSensorFromPanel(levelXing.getSensorDName());
			levelXing.setSensorDName("");		
		}
		// setup logic if requested
		// finish up
		setSensorsAtXingOpen = false;
		sensorsAtXingFrame.setVisible(false);
		xingSensorFromMenu = false;	
		if (needRedraw) {
			layoutEditor.redrawPanel();
			needRedraw = false;
			layoutEditor.setDirty();
		}		
	}
    
    private boolean getSimpleBlockInformation() {
        //might have to do something to trick it with an end bumper
		if (!boundaryFromMenu) {
			block1 = getBlockFromEntry(block1NameField);
			if (block1==null) return false;
			block2 = getBlockFromEntry(block2NameField);
			if (block2==null){
                PositionablePoint p = null;
                for (int i = 0;(i<layoutEditor.pointList.size()) && (boundary==null);i++) {
                    p = layoutEditor.pointList.get(i);
                    if (p.getType() == PositionablePoint.END_BUMPER) {
                        boundary = p;
                    } else {
                        return false;
                    }
                }
            }
			PositionablePoint p = null;
			boundary = null;
			for (int i = 0;(i<layoutEditor.pointList.size()) && (boundary==null);i++) {
				p = layoutEditor.pointList.get(i);
				if (p.getType() == PositionablePoint.ANCHOR) {
					LayoutBlock bA = null;
					LayoutBlock bB = null;
					if (p.getConnect1()!=null) bA = p.getConnect1().getLayoutBlock();
					if (p.getConnect2()!=null) bB = p.getConnect2().getLayoutBlock();
					if ( (bA!=null) && (bB!=null) && (bA!=bB) ) {
						if ( ( (bA==block1) && (bB==block2) ) ||
									( (bA==block2) && (bB==block1) ) ) {
							boundary = p;
						}
					}
				}
			}
			if (boundary==null) {
				JOptionPane.showMessageDialog(setSignalsAtBoundaryFrame,
							rb.getString("SignalsError7"),
									rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
        return true;
    }
    
    static class BeanDetails {
        String beanString;
        JLabel textLabel;
        
        String boundaryLabelText = rb.getString("BoundaryOf");
        JLabel boundary = new JLabel(boundaryLabelText);
        
        jmri.Manager manager;

        JPanel detailsPanel = new JPanel();
        JRadioButton addBeanCheck = new JRadioButton(rb.getString("DoNotPlace"));
        JRadioButton left = new JRadioButton(rb.getString("LeftHandSide"));
        JRadioButton right = new JRadioButton(rb.getString("RightHandSide"));
        ButtonGroup buttonGroup = new ButtonGroup();
        JmriBeanComboBox beanCombo;
        
        JLabel boundaryBlocks = new JLabel();
        
        Border blackline = BorderFactory.createLineBorder(Color.black);
        
        BeanDetails(String beanType, jmri.Manager manager){
            beanCombo = new JmriBeanComboBox(manager);
            beanCombo.setFirstItemBlank(true);
            beanString = rb.getString(beanType);
            textLabel = new JLabel(beanString);
            this.manager = manager;
            //this.beanType = beanType;
            
            buttonGroup.add(addBeanCheck);
            buttonGroup.add(left);
            buttonGroup.add(right);
            addBeanCheck.setSelected(true);

            boundaryBlocks.setAlignmentX(Component.CENTER_ALIGNMENT);
            boundaryBlocks.setOpaque(false);
            detailsPanel.setLayout(new BorderLayout());
            detailsPanel.setBorder(BorderFactory.createTitledBorder(blackline, rb.getString("BlockBoundary")));
            boundary.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            JPanel boundaryDetails = new JPanel();
            boundaryDetails.setOpaque(false);
            boundaryDetails.setLayout(new BoxLayout(boundaryDetails, BoxLayout.Y_AXIS));
            boundaryDetails.add(boundary);
            boundaryDetails.add(boundaryBlocks);
            
            detailsPanel.add(boundaryDetails, BorderLayout.PAGE_START);
            detailsPanel.add(addIconPanel(), BorderLayout.CENTER);
            detailsPanel.add(positionLeftRight(), BorderLayout.PAGE_END);
        }
        
        void setTextField(String value){
            beanCombo.setSelectedBean(manager.getNamedBean(value));
        }
        
        String getText(){
            return beanCombo.getSelectedDisplayName();
        }
        
        jmri.NamedBean getBean(){
            return beanCombo.getSelectedBean();
        }
        
        JPanel getDetailsPanel(){
            return detailsPanel;
        }
        
        boolean addToPanel(){
            return !addBeanCheck.isSelected();
        }
        
        boolean isRightSelected(){
            return right.isSelected();
        }
        
        void setBoundaryTitle(String text){
            detailsPanel.setBorder(BorderFactory.createTitledBorder(blackline, text));
        }
        
        void setBoundaryLabelText(String text){
            boundary.setText(text);
        }
        
        void setBoundaryLabel(String label){
            boundaryBlocks.setText(label);
        }
        
        JmriBeanComboBox getCombo(){
            return beanCombo;
        }
        
        JPanel positionLeftRight(){
            JPanel placementPanel = new JPanel();
            placementPanel.setBorder(BorderFactory.createTitledBorder(blackline, java.text.MessageFormat.format(rb.getString("PlaceItem"),
                            new Object[]{beanString})));
            placementPanel.setLayout(new BoxLayout(placementPanel, BoxLayout.Y_AXIS));
            placementPanel.setOpaque(false);
            placementPanel.add(addBeanCheck);
            placementPanel.add(left);
            placementPanel.add(right);
            addBeanCheck.setOpaque(false);
            left.setOpaque(false);
            right.setOpaque(false);
            
            addBeanCheck.setToolTipText(java.text.MessageFormat.format(rb.getString("PlaceItemToolTip"),
                new Object[]{beanString}));
                
            right.setToolTipText(java.text.MessageFormat.format(rb.getString("PlaceRightToolTip"), 
                new Object[]{beanString}));
                
            left.setToolTipText(java.text.MessageFormat.format(rb.getString("PlaceLeftToolTip"),
                new Object[]{beanString}));
            return placementPanel;
        }
        
        JPanel addIconPanel(){
            JPanel addBeanPanel = new JPanel();
            addBeanPanel.setOpaque(false);
            addBeanPanel.setLayout(new FlowLayout());
            addBeanPanel.add(textLabel);
            textLabel.setOpaque(false);
            addBeanPanel.add(beanCombo);
            return addBeanPanel;
        }
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LayoutEditorTools.class.getName());
}
