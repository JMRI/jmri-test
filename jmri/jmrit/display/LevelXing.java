package jmri.jmrit.display;

import jmri.util.JmriJFrame;

import java.awt.*;
import java.awt.geom.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import java.util.ArrayList;

import java.util.ResourceBundle;

import javax.swing.*;

/**
 * A LevelXing is two track segment on a layout that cross at an angle.
 * <P>
 * A LevelXing has four connection points, designated A, B, C, and D.
 *		At the crossing, A-C and B-D are straight segments.  A train proceeds
 *		through the crossing on either of these segments.
 * <P>
 * Each straight segment carries Block information.  A-C and B-D may be in the
 *		same or different Layout Blocks.
 * <P>
 * For drawing purposes, each LevelXing carries a center point and displacements
 *		for A and B.  The displacements for C = - the displacement for A, and the
 *		displacement for D = - the displacement for B.  The center point and these
 *      displacements may be adjusted by the user when in edit mode.
 * <P>
 * When LevelXings are first created, there are no connections.  Block information
 *		and connections are added when available.  
 *
 * @author Dave Duchamp Copyright (c) 2004-2007
 * @version $Revision: 1.1 $
 */

public class LevelXing 
{

	// Defined text resource
	ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.LayoutEditorBundle");

	// defined constants 

	// operational instance variables (not saved between sessions)
	private LayoutBlock blockAC = null;
	private LayoutBlock blockBD = null;
	private LevelXing instance = null;
	private LayoutEditor layoutEditor = null;
	
	// persistent instances variables (saved between sessions)
	private String ident = "";
	private String blockNameAC = "";
	private String blockNameBD = "";
	private Object connectA = null;
	private Object connectB = null;
	private Object connectC = null;
	private Object connectD = null;
	private Point2D center = new Point2D.Double(50.0,50.0);
	private Point2D dispA = new Point2D.Double(-20.0,0.0);
	private Point2D dispB = new Point2D.Double(-14.0,14.0);
	
    
	/** 
	 * constructor method
	 */  
	public LevelXing(String id, Point2D c, LayoutEditor myPanel) {
		instance = this;
		layoutEditor = myPanel;
		ident = id;
		center = c;
    }

	/**
	 * Accessor methods
	*/
	public String getID() {return ident;}
	public String getBlockNameAC() {return blockNameAC;}
	public String getBlockNameBD() {return blockNameBD;}
	public Object getConnectA() {return connectA;}
	public Object getConnectB() {return connectB;}
	public Object getConnectC() {return connectC;}
	public Object getConnectD() {return connectD;}
	public void setConnectA(Object o,int type) {
		connectA = o;
		if (type!=LayoutEditor.TRACK) {
			log.error("unexpected type of A connection to levelXing - "+type);
		}
	}
	public void setConnectB(Object o,int type) {
		connectB = o;
		if (type!=LayoutEditor.TRACK) {
			log.error("unexpected type of B connection to levelXing - "+type);
		}	
	}
	public void setConnectC(Object o,int type) {
		connectC = o;
		if (type!=LayoutEditor.TRACK) {
			log.error("unexpected type of C connection to levelXing - "+type);
		}	
	}
	public void setConnectD(Object o,int type) {
		connectD = o;
		if (type!=LayoutEditor.TRACK) {
			log.error("unexpected type of D connection to levelXing - "+type);
		}	
	}
	public LayoutBlock getLayoutBlockAC() {
		if ( (blockAC==null) && (blockNameAC.length()>0) ) {
			blockAC = layoutEditor.provideLayoutBlock(blockNameAC);
			if ( (blockAC!=null) && (blockAC==blockBD) )
					blockAC.decrementUse();
		}				
		return blockAC;
	}
	public LayoutBlock getLayoutBlockBD() {
		if ( (blockBD==null) && (blockNameBD.length()>0) ) {
			blockBD = layoutEditor.provideLayoutBlock(blockNameBD);
			if ( (blockBD!=null) && (blockAC==blockBD) )
					blockBD.decrementUse();
		}				
		return blockBD;
	}
	public Point2D getCoordsCenter() {return center;}
	public Point2D getCoordsA() {
		double x = center.getX() + dispA.getX();
		double y = center.getY() + dispA.getY();
		return new Point2D.Double(x,y);
	}
	public Point2D getCoordsB() {
		double x = center.getX() + dispB.getX();
		double y = center.getY() + dispB.getY();
		return new Point2D.Double(x,y);
	}
	public Point2D getCoordsC() {
		double x = center.getX() - dispA.getX();
		double y = center.getY() - dispA.getY();
		return new Point2D.Double(x,y);
	}
	public Point2D getCoordsD() {
		double x = center.getX() - dispB.getX();
		double y = center.getY() - dispB.getY();
		return new Point2D.Double(x,y);
	}

	/**
	 * Add Layout Blocks 
	 */
	public void setLayoutBlockAC (LayoutBlock b) {
		blockAC = b;
		if (b!=null) {
			blockNameAC = b.getID();
		}
	}
	public void setLayoutBlockBD (LayoutBlock b) {
		blockBD = b;
		if (b!=null) {
			blockNameBD = b.getID();
		}
	}
	
	/** 
	 * Methods to test if mainline track or not
	 *  Returns true if either connecting track segment is mainline
	 *  Defaults to not mainline if connecting track segments are missing
	 */
	public boolean isMainlineAC() {
		if ( ((connectA != null) && (((TrackSegment)connectA).getMainline())) ||
			((connectB != null) && (((TrackSegment)connectB).getMainline())) ) {
			return true;
		}
		else {
			return false;
		}
	}
	public boolean isMainlineBD() {
		if ( ((connectB != null) && (((TrackSegment)connectB).getMainline())) ||
			((connectD != null) && (((TrackSegment)connectD).getMainline())) ) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Modify coordinates methods
	 */
	public void setCoordsCenter(Point2D p) {
		center = p;
	}
	public void setCoordsA(Point2D p) {
		double x = center.getX() - p.getX();
		double y = center.getY() - p.getY();
		dispA = new Point2D.Double(-x,-y);
	}
	public void setCoordsB(Point2D p) {
		double x = center.getX() - p.getX();
		double y = center.getY() - p.getY();
		dispB = new Point2D.Double(-x,-y);
	}
	public void setCoordsC(Point2D p) {
		double x = center.getX() - p.getX();
		double y = center.getY() - p.getY();
		dispA = new Point2D.Double(x,y);
	}
	public void setCoordsD(Point2D p) {
		double x = center.getX() - p.getX();
		double y = center.getY() - p.getY();
		dispB = new Point2D.Double(x,y);
	}
	public void scaleCoords(float xFactor, float yFactor) {
		Point2D pt = new Point2D.Double(round(center.getX()*xFactor),
										round(center.getY()*yFactor));
		center = pt;
		pt = new Point2D.Double(round(dispA.getX()*xFactor),
										round(dispA.getY()*yFactor));
		dispA = pt;
		pt = new Point2D.Double(round(dispB.getX()*xFactor),
										round(dispB.getY()*yFactor));
		dispB = pt;
	}
	double round (double x) {
		int i = (int)(x+0.5);
		return ((double)i);
	}
		
	// initialization instance variables (used when loading a LayoutEditor)
	public String connectAName = "";
	public String connectBName = "";
	public String connectCName = "";
	public String connectDName = "";
	public String tBlockNameAC = "";
	public String tBlockNameBD = "";
	/**
	 * Initialization method
	 *   The above variables are initialized by PositionablePointXml, then the following
	 *        method is called after the entire LayoutEditor is loaded to set the specific
	 *        TrackSegment objects.
	 */
	public void setObjects(LayoutEditor p) {
		connectA = (Object)p.findTrackSegmentByName(connectAName);
		connectB = (Object)p.findTrackSegmentByName(connectBName);
		connectC = (Object)p.findTrackSegmentByName(connectCName);
		connectD = (Object)p.findTrackSegmentByName(connectDName);
		if (tBlockNameAC.length()>0) {
			blockAC = p.getLayoutBlock(tBlockNameAC);
			if (blockAC!=null) {
				blockNameAC = tBlockNameAC;
				if (blockAC!=blockBD) 
					blockAC.incrementUse();
			}
			else {
				log.error("bad blocknameac '"+tBlockNameAC+"' in levelxing "+ident);
			}
		}
		if (tBlockNameBD.length()>0) {
			blockBD = p.getLayoutBlock(tBlockNameBD);
			if (blockBD!=null) {
				blockNameBD = tBlockNameBD;
				if (blockAC!=blockBD) 
					blockBD.incrementUse();
			}
			else {
				log.error("bad blocknamebd '"+tBlockNameBD+"' in levelxing "+ident);
			}
		}
	}


    JPopupMenu popup = null;
    /**
     * Display popup menu for information and editing
     */
    protected void showPopUp(MouseEvent e) {
        if (popup != null ) {
			popup.removeAll();
		}
		else {
            popup = new JPopupMenu();
		}
		popup.add(rb.getString("LevelCrossing"));
		if (blockNameAC=="") popup.add(rb.getString("NoBlock1"));
		else popup.add(rb.getString("Block1ID")+": "+getLayoutBlockAC().getID());
		if (blockNameBD=="") popup.add(rb.getString("NoBlock2"));
		else popup.add(rb.getString("Block2ID")+": "+getLayoutBlockBD().getID());
		popup.add(new JSeparator(JSeparator.HORIZONTAL));
		popup.add(new AbstractAction(rb.getString("Edit")) {
				public void actionPerformed(ActionEvent e) {
					editLevelXing(instance);
				}
			});
		popup.add(new AbstractAction("Remove") {
				public void actionPerformed(ActionEvent e) {
					if (layoutEditor.removeLevelXing(instance)) {
						// Returned true if user did not cancel
						remove();
						dispose();
					}
				}
			});
		popup.show(e.getComponent(), e.getX(), e.getY());
    }

	// variables for Edit Level Crossing pane
	JmriJFrame editLevelXingFrame = null;
	JTextField block1Name = new JTextField(16);
	JTextField block2Name = new JTextField(16);
	JButton xingEditDone;
	JButton xingEditCancel;
	JButton xingEdit1Block;
	JButton xingEdit2Block;
	boolean editOpen = false;
	boolean needsRedraw = false;
	
    /**
     * Edit a Level Crossing
     */
	protected void editLevelXing(LevelXing o) {
		if (editOpen) {
			editLevelXingFrame.setVisible(true);
			return;
		}
		// Initialize if needed
		if (editLevelXingFrame == null) {
            editLevelXingFrame = new JmriJFrame( rb.getString("EditXing") );
            editLevelXingFrame.addHelpMenu("package.jmri.jmrit.display.EditLevelXing", true);
            editLevelXingFrame.setLocation(50,30);
            Container contentPane = editLevelXingFrame.getContentPane();        
            contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
			// setup block 1 name
            JPanel panel1 = new JPanel(); 
            panel1.setLayout(new FlowLayout());
			JLabel block1NameLabel = new JLabel( rb.getString("Block1ID") );
            panel1.add(block1NameLabel);
            panel1.add(block1Name);
            block1Name.setToolTipText( rb.getString("EditBlockNameHint") );
            contentPane.add(panel1);
			// setup block 2 name
            JPanel panel2 = new JPanel(); 
            panel2.setLayout(new FlowLayout());
			JLabel block2NameLabel = new JLabel( rb.getString("Block2ID"));
            panel2.add(block2NameLabel);
            panel2.add(block2Name);
            block2Name.setToolTipText( rb.getString("EditBlockNameHint") );
            contentPane.add(panel2);
			// set up Edit 1 Block and Edit 2 Block buttons
            JPanel panel4 = new JPanel();
            panel4.setLayout(new FlowLayout());
			// Edit 1 Block
            panel4.add(xingEdit1Block = new JButton(rb.getString("EditBlock1")));
            xingEdit1Block.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    xingEdit1BlockPressed(e);
                }
            });
            xingEdit1Block.setToolTipText( rb.getString("EditBlockHint") );
			// Edit 2 Block
            panel4.add(xingEdit2Block = new JButton(rb.getString("EditBlock2")));
            xingEdit2Block.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    xingEdit2BlockPressed(e);
                }
            });
            xingEdit2Block.setToolTipText( rb.getString("EditBlockHint") );
            contentPane.add(panel4);		
			// set up Done and Cancel buttons
            JPanel panel5 = new JPanel();
            panel5.setLayout(new FlowLayout());
            panel5.add(xingEditDone = new JButton(rb.getString("Done")));
            xingEditDone.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    xingEditDonePressed(e);
                }
            });
            xingEditDone.setToolTipText( rb.getString("DoneHint") );
			// Cancel
            panel5.add(xingEditCancel = new JButton(rb.getString("Cancel")));
            xingEditCancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    xingEditCancelPressed(e);
                }
            });
            xingEditCancel.setToolTipText( rb.getString("CancelHint") );
            contentPane.add(panel5);		
		}
		// Set up for Edit
		block1Name.setText(blockNameAC);
		block2Name.setText(blockNameBD);
		editLevelXingFrame.addWindowListener(new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent e) {
					xingEditCancelPressed(null);
				}
			});
        editLevelXingFrame.pack();
        editLevelXingFrame.setVisible(true);	
		editOpen = true;	
	}
	void xingEdit1BlockPressed(ActionEvent a) {
		// check if a block name has been entered
		if (!blockNameAC.equals(block1Name.getText()) ) {
			// block has changed, if old block exists, decrement use
			if ( (blockAC!=null) && (blockAC!=blockBD) ) {
				blockAC.decrementUse();
			}
			// get new block, or null if block has been removed
			blockNameAC = block1Name.getText();
			blockAC = layoutEditor.provideLayoutBlock(blockNameAC);
			// decrement use if block was previously counted
			if ( (blockAC!=null) && (blockAC==blockBD) ) blockAC.decrementUse();
		}
		// check if a block exists to edit
		if (blockAC==null) {
			JOptionPane.showMessageDialog(editLevelXingFrame,
					rb.getString("Error1"),
					rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
			return;
		}
		blockAC.editLayoutBlock(editLevelXingFrame);
		needsRedraw = true;
	}
	void xingEdit2BlockPressed(ActionEvent a) {
		// check if a block name has been entered
		if (!blockNameBD.equals(block2Name.getText()) ) {
			// block has changed, if old block exists, decrement use
			if ( (blockBD!=null) && (blockBD!=blockAC) ) {
				blockBD.decrementUse();
			}
			// get new block, or null if block has been removed
			blockNameBD = block2Name.getText();
			blockBD = layoutEditor.provideLayoutBlock(blockNameBD);
			// decrement use if block was previously counted
			if ( (blockBD!=null) && (blockAC==blockBD) ) blockBD.decrementUse();
		}
		// check if a block exists to edit
		if (blockBD==null) {
			JOptionPane.showMessageDialog(editLevelXingFrame,
					rb.getString("Error1"),
					rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
			return;
		}
		blockBD.editLayoutBlock(editLevelXingFrame);
		needsRedraw = true;
	}
	void xingEditDonePressed(ActionEvent a) {
		// check if Blocks changed
		if ( !blockNameAC.equals(block1Name.getText()) ) {
			// block 1 has changed, if old block exists, decrement use
			if ( (blockAC!=null) && (blockAC!=blockBD) ) {
				blockAC.decrementUse();
			}
			// get new block, or null if block has been removed
			blockNameAC = block1Name.getText();
			blockAC = layoutEditor.provideLayoutBlock(blockNameAC);
			// decrement use if block was previously counted
			if ( (blockAC!=null) && (blockAC==blockBD) ) blockAC.decrementUse();
			needsRedraw = true;
		}
		if ( !blockNameBD.equals(block2Name.getText()) ) {
			// block 2 has changed, if old block exists, decrement use
			if ( (blockBD!=null) && (blockBD!=blockAC) ) {
				blockBD.decrementUse();
			}
			// get new block, or null if block has been removed
			blockNameBD = block2Name.getText();
			blockBD = layoutEditor.provideLayoutBlock(blockNameBD);
			// decrement use if block was previously counted
			if ( (blockBD!=null) && (blockAC==blockBD) ) blockBD.decrementUse();
			needsRedraw = true;
		}
		editOpen = false;
		editLevelXingFrame.setVisible(false);
		if (needsRedraw) {
			layoutEditor.redrawPanel();
			layoutEditor.setDirty();
		}
	}
	void xingEditCancelPressed(ActionEvent a) {
		editOpen = false;
		editLevelXingFrame.setVisible(false);
		if (needsRedraw) {
			layoutEditor.redrawPanel();
			layoutEditor.setDirty();
		}
	}

    /**
     * Clean up when this object is no longer needed.  Should not
     * be called while the object is still displayed; see remove()
     */
    void dispose() {
        if (popup != null) popup.removeAll();
        popup = null;
    }

    /**
     * Removes this object from display and persistance
     */
    void remove() {
        // remove from persistance by flagging inactive
        active = false;
    }

    boolean active = true;
    /**
     * "active" means that the object is still displayed, and should be stored.
     */
    public boolean isActive() {
        return active;
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LevelXing.class.getName());

}