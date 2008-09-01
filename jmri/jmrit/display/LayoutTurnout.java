package jmri.jmrit.display;

import jmri.util.JmriJFrame;
import jmri.InstanceManager;
import jmri.Turnout;

import java.awt.*;
import java.awt.geom.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import java.util.ResourceBundle;

import javax.swing.*;

/**
 * A LayoutTurnout corresponds to a turnout on the layout. A LayoutTurnout is an
 *      extension of the standard Turnout object with drawing and connectivity
 *      information added. 
 * <P> 
 *  Six types are supported:
 *		right-hand, left-hand, wye, double crossover, right-handed single crossover,
 *      and left-handed single crossover.  Note that double-slip
 *      turnouts can be handled as two turnouts, throat to throat, and three-way
 *		turnouts can be handles as two turnouts, left-hand and right-hand, 
 *      arranged throat to continuing route.
 * <P>
 * A LayoutTurnout has three or four connection points, designated A, B, C, and D.
 *		For right-handed or left-handed turnouts, A corresponds to the throat.
 *		At the crossing, A-B (and C-D for crossovers) is a straight segment
 *		(continuing route).  A-C (and B-D for crossovers) is the diverging
 *		route.  B-C (and A-D for crossovers) is an illegal condition.
 * <P>     
 * A LayoutTurnout carries Block information.  For right-handed, left-handed, and wye
 *      turnouts, the entire turnout is in one block,however, a block border may occur 
 *      at any connection (A,B,C,D). For a double crossover turnout, up to four blocks
 *      may be assigned, one for each connection point, but if only one block is assigned,
 *      that block applies to the entire turnout.
 * <P>
 * For drawing purposes, each LayoutTurnout carries a center point and displacements
 *		for B and C. For right-handed or left-handed turnouts, the displacement for 
 *		A = - the displacement for B, and the center point is at the junction of the
 *		diverging route and the straight through continuing route.  For double 
 *		crossovers, the center point is at the center of the turnout, and the 
 *		displacement for A = - the displacement for C and the displacement for D = 
 *		- the displacement for B.  The center point and these displacements may be 
 *		adjusted by the user when in edit mode.  For double crossovers, AB and BC
 *      are constrained to remain perpendicular.  For single crossovers, AB and CD 
 *		are constrained to remain parallel, and AC and BD are constrained to remain 
 *      parallel.
 * <P>
 * When LayoutTurnouts are first created, a rotation (degrees) is provided.
 *		For 0.0 rotation, the turnout lies on the east-west line with A facing
 *		east.  Rotations are performed in a clockwise direction.
 * <P>
 * When LayoutTurnouts are first created, there are no connections.  Block information
 *		and connections may be added when available.
 * <P>  
 * When a LayoutTurnout is first created, it is enabled for control of an assigned
 *		actual turnout. Clicking on the turnout center point will toggle the turnout.
 *		This can be disabled via the popup menu.
 * <P>
 * Signal Head names are saved here to keep track of where signals are. LayoutTurnout 
 *		only serves as a storage place for signal head names. The names are placed here
 *		by tools, Set Signals at Turnout, and Set Signals at Double Crossover.
 *
 * @author Dave Duchamp Copyright (c) 2004-2007
 * @version $Revision: 1.8 $
 */

public class LayoutTurnout
{

	// Defined text resource
	ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.LayoutEditorBundle");

	// defined constants - turnout types
	public static final int RH_TURNOUT = 1;
	public static final int LH_TURNOUT = 2;
	public static final int WYE_TURNOUT = 3;
	public static final int DOUBLE_XOVER = 4;
	public static final int RH_XOVER = 5;
	public static final int LH_XOVER = 6;	

	// operational instance variables (not saved between sessions)
	private Turnout turnout = null;
	private LayoutBlock block = null;
	private LayoutBlock blockB = null;  // Xover - second block, if there is one
	private LayoutBlock blockC = null;  // Xover - third block, if there is one
	private LayoutBlock blockD = null;  // Xover - fourth block, if there is one
	private LayoutTurnout instance = null;
	private LayoutEditor layoutEditor = null;
	private java.beans.PropertyChangeListener mTurnoutListener = null;
	
	// persistent instances variables (saved between sessions)
	public String ident;   // name of this layout turnout (hidden from user)
	public String turnoutName = "";   // should be the name (system or user) of
								//	an existing physical turnout
	public String blockName = "";  // name for block, if there is one
	public String blockBName = "";  // Xover - name for second block, if there is one
	public String blockCName = "";  // Xover - name for third block, if there is one
	public String blockDName = "";  // Xover - name for fourth block, if there is one
	public String signalA1Name = ""; // signal 1 (continuing) (throat for RH, LH, WYE)
	public String signalA2Name = ""; // signal 2 (diverging) (throat for RH, LH, WYE)
	public String signalB1Name = ""; // continuing (RH, LH, WYE) signal 1 (double crossover)
	public String signalB2Name = ""; // LH_Xover and double crossover only
	public String signalC1Name = ""; // diverging (RH, LH, WYE) signal 1 (double crossover)
	public String signalC2Name = ""; // RH_Xover and double crossover only
	public String signalD1Name = ""; // single or double crossover only
	public String signalD2Name = ""; // LH_Xover and double crossover only
	public int type = RH_TURNOUT;
	public Object connectA = null;		// throat of LH, RH, RH Xover, LH Xover, and WYE turnouts
	public Object connectB = null;		// straight leg of LH and RH turnouts
	public Object connectC = null;		
	public Object connectD = null;		// double xover, RH Xover, LH Xover only
	public int continuingSense = Turnout.CLOSED;
	public boolean disabled = false;
	public Point2D center = new Point2D.Double(50.0,50.0);
	public Point2D dispB = new Point2D.Double(20.0,0.0);
	public Point2D dispC = new Point2D.Double(20.0,10.0);

	/** 
	 * constructor method
	 */  
    public LayoutTurnout(String id, int t, Point2D c, double rot, 
								double xFactor, double yFactor, LayoutEditor myPanel) {
		instance = this;
		turnout = null;
		turnoutName = "";
		mTurnoutListener = null;
		disabled = false;
		block = null;
		blockName = "";
		layoutEditor = myPanel;
		ident = id;
		type = t;
		center = c;
		// adjust initial coordinates
		if (type==LH_TURNOUT) {
			dispB.setLocation(layoutEditor.getTurnoutBX(),0.0);
			dispC.setLocation(layoutEditor.getTurnoutCX(),-layoutEditor.getTurnoutWid());
		}
		else if (type==RH_TURNOUT) {
			dispB.setLocation(layoutEditor.getTurnoutBX(),0.0);
			dispC.setLocation(layoutEditor.getTurnoutCX(),layoutEditor.getTurnoutWid());
		}
		else if (type==WYE_TURNOUT) {
			dispB.setLocation(layoutEditor.getTurnoutBX(),0.5*layoutEditor.getTurnoutWid());
			dispC.setLocation(layoutEditor.getTurnoutBX(),-0.5*layoutEditor.getTurnoutWid());
		}
		else if (type==DOUBLE_XOVER) {
			dispB.setLocation(layoutEditor.getXOverLong(),-layoutEditor.getXOverHWid());
			dispC.setLocation(layoutEditor.getXOverLong(),layoutEditor.getXOverHWid());
			blockB = null;
			blockBName = "";
			blockC = null;
			blockCName = "";
			blockD = null;
			blockDName = "";
		}
		else if (type==RH_XOVER) {
			dispB.setLocation(layoutEditor.getXOverShort(),-layoutEditor.getXOverHWid());
			dispC.setLocation(layoutEditor.getXOverLong(),layoutEditor.getXOverHWid());
			blockB = null;
			blockBName = "";
			blockC = null;
			blockCName = "";
			blockD = null;
			blockDName = "";
		}
		else if (type==LH_XOVER) {
			dispB.setLocation(layoutEditor.getXOverLong(),-layoutEditor.getXOverHWid());
			dispC.setLocation(layoutEditor.getXOverShort(),layoutEditor.getXOverHWid());
			blockB = null;
			blockBName = "";
			blockC = null;
			blockCName = "";
			blockD = null;
			blockDName = "";
		}		
		rotateCoords(rot);
		// adjust size of new turnout
		Point2D pt = new Point2D.Double(round(dispB.getX()*xFactor),
										round(dispB.getY()*yFactor));
		dispB = pt;
		pt = new Point2D.Double(round(dispC.getX()*xFactor),
										round(dispC.getY()*yFactor));
		dispC = pt;		
	}
	private double round (double x) {
		int i = (int)(x+0.5);
		return ((double)i);
	}
		
	private void rotateCoords(double rot) {
		// rotate coordinates
		double sineAng = Math.sin(rot*Math.PI/180.0);
		double cosineAng = Math.cos(rot*Math.PI/180.0);
		double x = (cosineAng*dispB.getX()) - (sineAng*dispB.getY());
		double y = (sineAng*dispB.getX()) + (cosineAng*dispB.getY());
		dispB = new Point2D.Double(x,y);
		x = (cosineAng*dispC.getX()) - (sineAng*dispC.getY());
		y = (sineAng*dispC.getX()) + (cosineAng*dispC.getY());
		dispC = new Point2D.Double(x,y);
    }

	/**
	 * Accessor methods
	*/
	public String getName() {return ident;}
	public String getTurnoutName() {return turnoutName;}
	public String getBlockName() {return blockName;}
	public String getBlockBName() {return blockBName;}
	public String getBlockCName() {return blockCName;}
	public String getBlockDName() {return blockDName;}
	public String getSignalA1Name() {return signalA1Name;}
	public void setSignalA1Name(String signalName) {signalA1Name = signalName;}
	public String getSignalA2Name() {return signalA2Name;}
	public void setSignalA2Name(String signalName) {signalA2Name = signalName;}
	public String getSignalB1Name() {return signalB1Name;}
	public void setSignalB1Name(String signalName) {signalB1Name = signalName;}
	public String getSignalB2Name() {return signalB2Name;}
	public void setSignalB2Name(String signalName) {signalB2Name = signalName;}
	public String getSignalC1Name() {return signalC1Name;}
	public void setSignalC1Name(String signalName) {signalC1Name = signalName;}
	public String getSignalC2Name() {return signalC2Name;}
	public void setSignalC2Name(String signalName) {signalC2Name = signalName;}
	public String getSignalD1Name() {return signalD1Name;}
	public void setSignalD1Name(String signalName) {signalD1Name = signalName;}
	public String getSignalD2Name() {return signalD2Name;}
	public void setSignalD2Name(String signalName) {signalD2Name = signalName;}
	public int getTurnoutType() {return type;}
	public Object getConnectA() {return connectA;}
	public Object getConnectB() {return connectB;}
	public Object getConnectC() {return connectC;}
	public Object getConnectD() {return connectD;}
	public Turnout getTurnout() {
		if (turnout==null) {
			// set physical turnout if possible and needed
			turnout = jmri.InstanceManager.turnoutManagerInstance().
							getTurnout(turnoutName);
			if (turnout!=null) activateTurnout();
		}
		return turnout;
	}
	public int getContinuingSense() {return continuingSense;}
	public void setTurnout(String tName) {
		if (turnout!=null) deactivateTurnout();
		turnoutName = tName;
		turnout = jmri.InstanceManager.turnoutManagerInstance().
                            getTurnout(turnoutName);
		if (turnout!=null) {
			activateTurnout();
			if (turnoutName.toUpperCase().equals(turnout.getSystemName())) {
				// Adjust case of turnout name if needed
				turnoutName = turnout.getSystemName();
			}
		}
		else {
			turnoutName = "";
		}
	}
	public void setContinuingSense(int sense) {continuingSense=sense;}
	public void setDisabled(boolean state) {disabled = state;}
	public boolean isDisabled() {return disabled;}
	public void setConnectA(Object o,int type) {
		connectA = o;
		if ( (type!=LayoutEditor.TRACK) && (type!=LayoutEditor.NONE) ) {
			log.error("unexpected type of A connection to layoutturnout - "+type);
		}
	}
	public void setConnectB(Object o,int type) {
		connectB = o;
		if ( (type!=LayoutEditor.TRACK) && (type!=LayoutEditor.NONE) ) {
			log.error("unexpected type of B connection to layoutturnout - "+type);
		}
	}
	public void setConnectC(Object o,int type) {
		connectC = o;
		if ( (type!=LayoutEditor.TRACK) && (type!=LayoutEditor.NONE) ) {
			log.error("unexpected type of C connection to layoutturnout - "+type);
		}
	}
	public void setConnectD(Object o,int type) {
		connectD = o;
		if ( (type!=LayoutEditor.TRACK) && (type!=LayoutEditor.NONE) ) {
			log.error("unexpected type of D connection to layoutturnout - "+type);
		}
	}
	public LayoutBlock getLayoutBlock() {return block;}
	public LayoutBlock getLayoutBlockB() {
			if (blockB!=null) return blockB;
			return block;
	}
	public LayoutBlock getLayoutBlockC() {
			if (blockC!=null) return blockC;
			return block;
	}
	public LayoutBlock getLayoutBlockD(){
			if (blockD!=null) return blockD;
			return block;
	}
	public Point2D getCoordsCenter() {return center;}
	public Point2D getCoordsA() {
		if ( (type==DOUBLE_XOVER) || (type==LH_XOVER) || (type==RH_XOVER) ) {
			double x = center.getX() - dispC.getX();
			double y = center.getY() - dispC.getY();
			return new Point2D.Double(x,y);
		}
		else if (type==WYE_TURNOUT) {
			double x = center.getX() - (0.5*(dispB.getX() + dispC.getX()));
			double y = center.getY() - (0.5*(dispB.getY() + dispC.getY()));
			return new Point2D.Double(x,y);
		}
		else {
			double x = center.getX() - dispB.getX();
			double y = center.getY() - dispB.getY();
			return new Point2D.Double(x,y);
		}
	}
	public Point2D getCoordsB() {
		double x = center.getX() + dispB.getX();
		double y = center.getY() + dispB.getY();
		return new Point2D.Double(x,y);
	}
	public Point2D getCoordsC() {
		double x = center.getX() + dispC.getX();
		double y = center.getY() + dispC.getY();
		return new Point2D.Double(x,y);
	}
	public Point2D getCoordsD() {
		// only allowed for single and double crossovers
		double x = center.getX() - dispB.getX();
		double y = center.getY() - dispB.getY();
		return new Point2D.Double(x,y);
	}

	// updates connectivity for blocks assigned to this turnout and connected track segments
	private void updateBlockInfo() {
		LayoutBlock bA = null;
		LayoutBlock bB = null;
		LayoutBlock bC = null;
		LayoutBlock bD = null;
		layoutEditor.auxTools.setBlockConnectivityChanged();
		if (block!=null) block.updatePaths();
		if (connectA!=null) {
			bA = ((TrackSegment)connectA).getLayoutBlock();
			if ((bA!=null) && (bA!=block)) bA.updatePaths();
		}
		if ((blockB!=null) && (blockB!=block) && (blockB!=bA)) blockB.updatePaths();
		if (connectB!=null) {
			bB = ((TrackSegment)connectB).getLayoutBlock();
			if ((bB!=null) && (bB!=block) && (bB!=bA) && (bB!=blockB)) bB.updatePaths();
		}
		if ((blockC!=null) && (blockC!=block) && (blockC!=bA) &&
				(blockC!=bB) && (blockC!=blockB)) blockC.updatePaths();
		if (connectC!=null) {
			bC = ((TrackSegment)connectC).getLayoutBlock();
			if ((bC!=null) && (bC!=block) && (bC!=bA) && (bC!=blockB) && (bC!=bB) &&
					(bC!=blockC)) bC.updatePaths();
		}
		if ((blockD!=null) && (blockD!=block) && (blockD!=bA) &&
				(blockD!=bB) && (blockD!=blockB) && (blockD!=bC) &&
					(blockD!=blockC)) blockD.updatePaths();
		if (connectD!=null) {
			bD = ((TrackSegment)connectD).getLayoutBlock();
			if ((bD!=null) && (bD!=block) && (bD!=bA) && (bD!=blockB) && (bD!=bB) &&
				(bD!=blockC) && (bD!=bC) && (bD!=blockD)) bD.updatePaths();
		}
	}	
	
	/**
	 * Set default size parameters to correspond to this turnout's size
	 */
	private void setUpDefaultSize() {
		// remove the overall scale factor
		double bX = dispB.getX()/layoutEditor.getXScale();
		double bY = dispB.getY()/layoutEditor.getYScale();
		double cX = dispC.getX()/layoutEditor.getXScale();
		double cY = dispC.getY()/layoutEditor.getYScale();
		// calculate default parameters according to type of turnout
		double lenB = Math.sqrt((bX*bX) + (bY*bY));
		double lenC = Math.sqrt((cX*cX) + (cY*cY));
		double distBC = Math.sqrt(((bX-cX)*(bX-cX)) + ((bY-cY)*(bY-cY)));
		if ( (type == LH_TURNOUT) || (type == RH_TURNOUT) ) {
			layoutEditor.setTurnoutBX(round(lenB+0.1));
			double xc = ((bX*cX)+(bY*cY))/lenB;
			layoutEditor.setTurnoutCX(round(xc+0.1));
			layoutEditor.setTurnoutWid(round(Math.sqrt((lenC*lenC)-(xc*xc))+0.1));
		}
		else if (type == WYE_TURNOUT) {
			double xx = Math.sqrt((lenB*lenB)-(0.25*(distBC*distBC)));
			layoutEditor.setTurnoutBX(round(xx+0.1));
			layoutEditor.setTurnoutCX(round(xx+0.1));
			layoutEditor.setTurnoutWid(round(distBC+0.1));
		}
		else if (type == DOUBLE_XOVER) {
			double lng = Math.sqrt((lenB*lenB)-(0.25*(distBC*distBC)));
			layoutEditor.setXOverLong(round(lng+0.1));			
			layoutEditor.setXOverHWid(round((0.5*distBC)+0.1));
			layoutEditor.setXOverShort(round((0.5*lng)+0.1));
		}
		else if (type == RH_XOVER) {
			double distDC = Math.sqrt(((bX+cX)*(bX+cX)) + ((bY+cY)*(bY+cY)));
			layoutEditor.setXOverShort(round((0.25*distDC)+0.1));
			layoutEditor.setXOverLong(round((0.75*distDC)+0.1));
			double hwid = Math.sqrt((lenC*lenC)-(0.5625*distDC*distDC));
			layoutEditor.setXOverHWid(round(hwid+0.1));
		}
		else if (type == LH_XOVER) {
			double distDC = Math.sqrt(((bX+cX)*(bX+cX)) + ((bY+cY)*(bY+cY)));
			layoutEditor.setXOverShort(round((0.25*distDC)+0.1));
			layoutEditor.setXOverLong(round((0.75*distDC)+0.1));
			double hwid = Math.sqrt((lenC*lenC)-(0.0625*distDC*distDC));
			layoutEditor.setXOverHWid(round(hwid+0.1));
		}
	}

	/**
	 * Set Up a Layout Block(s) for this Turnout
	 */
	public void setLayoutBlock (LayoutBlock b) {
		block = b;
		if (b!=null) blockName = b.getID();
		else blockName = "";
	}
	public void setLayoutBlockB (LayoutBlock b) {
		if ( (type==DOUBLE_XOVER) || (type==LH_XOVER) || (type==RH_XOVER) ) {
			blockB = b;
			if (b!=null) blockBName = b.getID();
			else blockBName = "";
		}
		else {
			log.error ("Attempt to set block B, but not a crossover");
		}
	}
	public void setLayoutBlockC (LayoutBlock b) {
		if ( (type==DOUBLE_XOVER) || (type==LH_XOVER) || (type==RH_XOVER) ) {
			blockC = b;
			if (b!=null) blockCName = b.getID();
			else blockCName = "";
		}
		else {
			log.error ("Attempt to set block C, but not a crossover");
		}
	}
	public void setLayoutBlockD (LayoutBlock b) {
		if ( (type==DOUBLE_XOVER) || (type==LH_XOVER) || (type==RH_XOVER) ) {
			blockD = b;
			if (b!=null) blockDName = b.getID();
			else blockDName = "";
		}
		else {
			log.error ("Attempt to set block D, but not a crossover");
		}
	}
	public void setLayoutBlockByName (String name) {
		blockName = name;
	}
	public void setLayoutBlockBByName (String name) {
		if ( (type==DOUBLE_XOVER) || (type==LH_XOVER) || (type==RH_XOVER) ) {
			blockBName = name;
		}
		else {
			log.error ("Attempt to set block B name, but not a crossover");
		}
	}
	public void setLayoutBlockCByName (String name) {
		if ( (type==DOUBLE_XOVER) || (type==LH_XOVER) || (type==RH_XOVER) ) {
			blockCName = name;
		}
		else {
			log.error ("Attempt to set block C name, but not a crossover");
		}
	}
	public void setLayoutBlockDByName (String name) {
		if ( (type==DOUBLE_XOVER) || (type==LH_XOVER) || (type==RH_XOVER) ) {
			blockDName = name;
		}
		else {
			log.error ("Attempt to set block D name, but not a crossover");
		}
	}
	
	/** 
	 * Methods to test if turnout legs are mainline track or not
	 *  Returns true if connecting track segment is mainline
	 *  Defaults to not mainline if connecting track segment is missing
	 */
	public boolean isMainlineA() {
		if (connectA != null) 
			return ((TrackSegment)connectA).getMainline();
		else {
			// if no connection, depends on type of turnout
			if ( (type==DOUBLE_XOVER) || (type==LH_XOVER) || (type==RH_XOVER) ) {
				// All crossovers - straight continuing is B
				if (connectB != null) 
					return ((TrackSegment)connectB).getMainline();
			}
			// must be RH, LH, or WYE turnout - A is the switch throat
			else if ( ((connectB != null) && 
					(((TrackSegment)connectB).getMainline())) ||
						((connectC != null) && 
							(((TrackSegment)connectC).getMainline())) )
				return true;	
		}	
		return false;
	}
	public boolean isMainlineB() {
		if (connectB != null) 
			return ((TrackSegment)connectB).getMainline();
		else {
			// if no connection, depends on type of turnout
			if ( (type==DOUBLE_XOVER) || (type==LH_XOVER) || (type==RH_XOVER) ) {
				// All crossovers - straight continuing is A
				if (connectA != null) 
					return ((TrackSegment)connectA).getMainline();
			}
			// must be RH, LH, or WYE turnout - A is the switch throat,
			//		B is normally the continuing straight
			else if (continuingSense == Turnout.CLOSED) {
				// user hasn't changed the continuing turnout state 
				if (connectA != null) 
					// if throat is mainline, this leg must be also 
					return ((TrackSegment)connectA).getMainline();
			}
		}	
		return false;
	}
	public boolean isMainlineC() {
		if (connectC != null) 
			return ((TrackSegment)connectC).getMainline();
		else {
			// if no connection, depends on type of turnout
			if ( (type==DOUBLE_XOVER) || (type==LH_XOVER) || (type==RH_XOVER) ) {
				// All crossovers - straight continuing is D
				if (connectD != null) 
					return ((TrackSegment)connectD).getMainline();
			}
			// must be RH, LH, or WYE turnout - A is the switch throat,
			//		B is normally the continuing straight
			else if (continuingSense == Turnout.THROWN) {
				// user has changed the continuing turnout state 
				if (connectA != null) 
					// if throat is mainline, this leg must be also 
					return ((TrackSegment)connectA).getMainline();
			}
		}	
		return false;
	}
	public boolean isMainlineD() {
		// this is a crossover turnout
		if (connectD != null) 
			return ((TrackSegment)connectD).getMainline();
		else if (connectC != null) 
			return ((TrackSegment)connectC).getMainline();
		return false;
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
		if (type == DOUBLE_XOVER) {
			dispC = new Point2D.Double(x,y);
			// adjust to maintain rectangle
			double oldLength = Math.sqrt( (dispB.getX()*dispB.getX()) + 
													(dispB.getY()*dispB.getY()) );
			double newLength = Math.sqrt( (x*x) + (y*y) );
			x = dispB.getX()*newLength/oldLength;
			y = dispB.getY()*newLength/oldLength;
			dispB = new Point2D.Double(x,y);
		}
		else if ( (type == RH_XOVER) || (type == LH_XOVER) ) {
			dispC = new Point2D.Double(x,y);
			// adjust to maintain the parallelogram
			double a = 0.0;
			double b = -y;
			double xi = 0.0;
			double yi = b;
			if ((dispB.getX() + x)!=0.0) {
				a = (dispB.getY() + y)/(dispB.getX() + x);
				b = -y + (a*x);
				xi = -b/(a + (1.0/a));
				yi = (a*xi) + b;
			}
			if (type == RH_XOVER) {
				x = xi - (0.333333*(-x - xi));
				y = yi - (0.333333*(-y - yi));
			}
			else if (type == LH_XOVER) {
				x = xi - (3.0*(-x - xi));
				y = yi - (3.0*(-y - yi));
			}
			dispB = new Point2D.Double(x,y);
		}
		else if (type == WYE_TURNOUT) {
			// modify both to maintain same angle at wye
			double temX = (dispB.getX() + dispC.getX());
			double temY = (dispB.getY() + dispC.getY());
			double temXx = (dispB.getX() - dispC.getX());
			double temYy = (dispB.getY() - dispC.getY());
			double tan = Math.sqrt( ((temX*temX)+(temY*temY))/
								((temXx*temXx)+(temYy*temYy)) );
			double xx = x + (y/tan);
			double yy = y - (x/tan);
			dispC = new Point2D.Double(xx,yy);
			xx = x - (y/tan);
			yy = y + (x/tan);
			dispB = new Point2D.Double(xx,yy);
		}
		else {
			dispB = new Point2D.Double(x,y);
		}
	}
	public void setCoordsB(Point2D p) {
		double x = center.getX() - p.getX();
		double y = center.getY() - p.getY();
		dispB = new Point2D.Double(-x,-y);
		if ((type == DOUBLE_XOVER) || (type == WYE_TURNOUT)) {
			// adjust to maintain rectangle or wye shape
			double oldLength = Math.sqrt( (dispC.getX()*dispC.getX()) + 
													(dispC.getY()*dispC.getY()) );
			double newLength = Math.sqrt( (x*x) + (y*y) );
			x = dispC.getX()*newLength/oldLength;
			y = dispC.getY()*newLength/oldLength;
			dispC = new Point2D.Double(x,y);
		}
		else if ( (type == RH_XOVER) || (type == LH_XOVER) ) {
			// adjust to maintain the parallelogram
			double a = 0.0;
			double b = y;
			double xi = 0.0;
			double yi = b;
			if ((dispC.getX() - x)!=0.0) {
				a = (dispC.getY() - y)/(dispC.getX() - x);
				b = y - (a*x);
				xi = -b/(a + (1.0/a));
				yi = (a*xi) + b;
			}
			if (type == LH_XOVER) {
				x = xi - (0.333333*(x - xi));
				y = yi - (0.333333*(y - yi));
			}
			else if (type == RH_XOVER) {
				x = xi - (3.0*(x - xi));
				y = yi - (3.0*(y - yi));
			}
			dispC = new Point2D.Double(x,y);
		}
	}
	public void setCoordsC(Point2D p) {
		double x = center.getX() - p.getX();
		double y = center.getY() - p.getY();
		dispC = new Point2D.Double(-x,-y);
		if ((type == DOUBLE_XOVER) || (type == WYE_TURNOUT)) {
			// adjust to maintain rectangle or wye shape
			double oldLength = Math.sqrt( (dispB.getX()*dispB.getX()) + 
													(dispB.getY()*dispB.getY()) );
			double newLength = Math.sqrt( (x*x) + (y*y) );
			x = dispB.getX()*newLength/oldLength;
			y = dispB.getY()*newLength/oldLength;
			dispB = new Point2D.Double(x,y);
		}
		else if ( (type == RH_XOVER) || (type == LH_XOVER) ) {
			double a = 0.0;
			double b = -y;
			double xi = 0.0;
			double yi = b;
			if ((dispB.getX() + x)!=0.0) {
				a = (-dispB.getY() + y)/(-dispB.getX() + x);
				b = -y + (a*x);
				xi = -b/(a + (1.0/a));
				yi = (a*xi) + b;
			}
			if (type == RH_XOVER) {
				x = xi - (0.333333*(-x - xi));
				y = yi - (0.333333*(-y - yi));
			}
			else if (type == LH_XOVER) {
				x = xi - (3.0*(-x - xi));
				y = yi - (3.0*(-y - yi));
			}
			dispB = new Point2D.Double(-x,-y);
		}
	}
	public void setCoordsD(Point2D p) {
		// only used for crossovers
		double x = center.getX() - p.getX();
		double y = center.getY() - p.getY();
		dispB = new Point2D.Double(x,y);
		if (type == DOUBLE_XOVER) {
			// adjust to maintain rectangle
			double oldLength = Math.sqrt( (dispC.getX()*dispC.getX()) + 
													(dispC.getY()*dispC.getY()) );
			double newLength = Math.sqrt( (x*x) + (y*y) );
			x = dispC.getX()*newLength/oldLength;
			y = dispC.getY()*newLength/oldLength;
			dispC = new Point2D.Double(x,y);
		}
		else if ( (type == RH_XOVER) || (type == LH_XOVER) ) {
			// adjust to maintain the parallelogram
			double a = 0.0;
			double b = y;
			double xi = 0.0;
			double yi = b;
			if ((dispC.getX() + x)!=0.0) {
				a = (dispC.getY() + y)/(dispC.getX() + x);
				b = -y + (a*x);
				xi = -b/(a + (1.0/a));
				yi = (a*xi) + b;
			}
			if (type == LH_XOVER) {
				x = xi - (0.333333*(-x - xi));
				y = yi - (0.333333*(-y - yi));
			}
			else if (type == RH_XOVER) {
				x = xi - (3.0*(-x - xi));
				y = yi - (3.0*(-y - yi));
			}
			dispC = new Point2D.Double(x,y);
		}
	}	
	public void scaleCoords(float xFactor, float yFactor) {
		Point2D pt = new Point2D.Double(round(center.getX()*xFactor),
										round(center.getY()*yFactor));
		center = pt;
		pt = new Point2D.Double(round(dispB.getX()*xFactor),
										round(dispB.getY()*yFactor));
		dispB = pt;
		pt = new Point2D.Double(round(dispC.getX()*xFactor),
										round(dispC.getY()*yFactor));
		dispC = pt;
	}
	
	/**
	 * Activate/Deactivate turnout to redraw when turnout state changes
	 */
	private void activateTurnout() {
		if (turnout!=null) {
			turnout.addPropertyChangeListener(mTurnoutListener =
								new java.beans.PropertyChangeListener() {
				public void propertyChange(java.beans.PropertyChangeEvent e) {
					layoutEditor.redrawPanel();
				}
			});
		}
	}
	private void deactivateTurnout() {
		if (mTurnoutListener!=null) {
			turnout.removePropertyChangeListener(mTurnoutListener);
			mTurnoutListener = null;
		}
	}

	/**
	 * Toggle turnout if clicked on, physical turnout exists, and
	 *    not disabled
	 */
	public void toggleTurnout() {
        if ((turnout!=null) && (!disabled)) {
			// toggle turnout
			if (turnout.getKnownState()==jmri.Turnout.CLOSED)
				turnout.setCommandedState(jmri.Turnout.THROWN);
			else
				turnout.setCommandedState(jmri.Turnout.CLOSED);
		}
    }
			
	// initialization instance variables (used when loading a LayoutEditor)
	public String connectAName = "";
	public String connectBName = "";
	public String connectCName = "";
	public String connectDName = "";
	public String tBlockName = "";
	public String tBlockBName = "";
	public String tBlockCName = "";
	public String tBlockDName = "";
	public String tTurnoutName = "";
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
		if (tBlockName.length()>0) {
			block = p.getLayoutBlock(tBlockName);
			if (block!=null) {
				blockName = tBlockName;
				block.incrementUse();
			}
			else {
				log.error("bad blockname '"+tBlockName+"' in layoutturnout "+ident);
			}
		}
		if (tBlockBName.length()>0) {
			blockB = p.getLayoutBlock(tBlockBName);
			if (blockB!=null) {
				blockBName = tBlockBName;
				if (block!=blockB) blockB.incrementUse();
			}
			else {
				log.error("bad blockname '"+tBlockBName+"' in layoutturnout "+ident);
			}
		}
		if (tBlockCName.length()>0) {
			blockC = p.getLayoutBlock(tBlockCName);
			if (blockC!=null) {
				blockCName = tBlockCName;
				if ( (block!=blockC) && (blockB!=blockC) ) blockC.incrementUse();
			}
			else {
				log.error("bad blockname '"+tBlockCName+"' in layoutturnout "+ident);
			}
		}
		if (tBlockDName.length()>0) {
			blockD = p.getLayoutBlock(tBlockDName);
			if (blockD!=null) {
				blockDName = tBlockDName;
				if ( (block!=blockD) && (blockB!=blockD) &&
						(blockC!=blockD) ) blockD.incrementUse();
			}
			else {
				log.error("bad blockname '"+tBlockDName+"' in layoutturnout "+ident);
			}
		}
		if (tTurnoutName.length()>0) {
			turnout = jmri.InstanceManager.turnoutManagerInstance().
													getTurnout(tTurnoutName);
			if (turnout!=null) {
				turnoutName = tTurnoutName;
				activateTurnout();
			}
			else {
				log.error("bad turnoutname '"+tTurnoutName+"' in layoutturnout "+ident);
				turnoutName = "";
			}
		}
	}

    JPopupMenu popup = null;
    JCheckBoxMenuItem disableItem = null;
	LayoutEditorTools tools = null;
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
		switch (getTurnoutType()) {
			case RH_TURNOUT:
				popup.add(rb.getString("RHTurnout"));
				break;
			case LH_TURNOUT:
				popup.add(rb.getString("LHTurnout"));
				break;
			case WYE_TURNOUT:
				popup.add(rb.getString("WYETurnout"));
				break;
			case DOUBLE_XOVER:
				popup.add(rb.getString("XOverTurnout"));
				break;
			case RH_XOVER:
				popup.add(rb.getString("RHXOverTurnout"));
				break;
			case LH_XOVER:
				popup.add(rb.getString("LHXOverTurnout"));
				break;
		}
		if (turnout==null) popup.add(rb.getString("NoTurnout"));
		else popup.add(rb.getString("Turnout")+": "+turnoutName);
		// Rotate if there are no track connections
		if ( (connectA==null) && (connectB==null) &&
					(connectC==null) && (connectD==null) ) {
			JMenuItem rotateItem = new JMenuItem(rb.getString("Rotate")+"...");
			popup.add(rotateItem);
			rotateItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    boolean entering = true;
                    boolean error = false;
					String newAngle = "";
                    while (entering) {
                        // prompt for rotation angle
						error = false;
                        newAngle = JOptionPane.showInputDialog(layoutEditor, 
											rb.getString("EnterRotation")+" :");
                        if (newAngle.length()<1) return;  // cancelled
                        double rot = 0.0;
                        try {
                            rot = Double.parseDouble(newAngle);
                        }
                        catch (Exception e) {
							JOptionPane.showMessageDialog(layoutEditor,rb.getString("Error3")+
								" "+e,rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
                            error = true;
							newAngle = "";
                        }
                        if (!error) {
							entering = false;
                            if (rot!=0.0) {
                               rotateCoords(rot);
                               layoutEditor.redrawPanel();
							}
                        }
                    }
                }
            });
		}
		if (disableItem==null)
			disableItem = new JCheckBoxMenuItem(rb.getString("Disabled"));
        disableItem.setSelected(disabled);
        popup.add(disableItem);
        disableItem.addActionListener(new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					disabled = disableItem.isSelected();
				}
			});
		if (blockName.equals("")) popup.add(rb.getString("NoBlock"));
		else popup.add(rb.getString("Block")+": "+getLayoutBlock().getID());
		if ( (type == DOUBLE_XOVER) || (type == RH_XOVER) || (type == LH_XOVER) ) {
			// check if extra blocks have been entered
			if (blockB!=null) popup.add(rb.getString("Block2ID")+": "+blockBName);
			if (blockC!=null) popup.add(rb.getString("Block3ID")+": "+blockCName);
			if (blockD!=null) popup.add(rb.getString("Block4ID")+": "+blockDName);
		}
		popup.add(new JSeparator(JSeparator.HORIZONTAL));
		popup.add(new AbstractAction(rb.getString("UseSizeAsDefault")) {
				public void actionPerformed(ActionEvent e) {
					setUpDefaultSize();
				}
			});
		popup.add(new AbstractAction(rb.getString("Edit")) {
				public void actionPerformed(ActionEvent e) {
					editLayoutTurnout();
				}
			});
		popup.add(new AbstractAction(rb.getString("Remove")) {
				public void actionPerformed(ActionEvent e) {
					if (layoutEditor.removeLayoutTurnout(instance)) {
						// Returned true if user did not cancel
						remove();
						dispose();
					}
				}
			});
		if (turnout!=null) {
			popup.add(new AbstractAction(rb.getString("SetSignals")) {
				public void actionPerformed(ActionEvent e) {
					if (tools == null) {
						tools = new LayoutEditorTools(layoutEditor);
					}
					if ( (getTurnoutType()==DOUBLE_XOVER) || (getTurnoutType()==RH_XOVER) ||
											(getTurnoutType()==LH_XOVER) ) {	
						tools.setSignalsAtXoverTurnoutFromMenu(instance,
							layoutEditor.signalIconEditor,layoutEditor.signalFrame);						
					}
					else {
						tools.setSignalsAtTurnoutFromMenu(instance,
							layoutEditor.signalIconEditor,layoutEditor.signalFrame);											
					}
				}
			});
		}			
		popup.show(e.getComponent(), e.getX(), e.getY());
    }
	
	// variables for Edit Layout Turnout pane
	private JmriJFrame editLayoutTurnoutFrame = null;
	private JTextField turnoutNameField = new JTextField(16);
	private JTextField blockNameField = new JTextField(16);
	private JTextField blockBNameField = new JTextField(16);
	private JTextField blockCNameField = new JTextField(16);
	private JTextField blockDNameField = new JTextField(16);
    private JComboBox stateBox = new JComboBox();
    private int turnoutClosedIndex;
    private int turnoutThrownIndex;
	private JButton turnoutEditBlock;
	private JButton turnoutEditDone;
	private JButton turnoutEditCancel;
	private JButton turnoutEditBlockB;
	private JButton turnoutEditBlockC;
	private JButton turnoutEditBlockD;
	private boolean editOpen = false;
	private boolean needRedraw = false;
	private boolean needsBlockUpdate = false;

    /**
     * Edit a Layout Turnout 
     */
	protected void editLayoutTurnout() {
		if (editOpen) {
			editLayoutTurnoutFrame.setVisible(true);
			return;
		}
		// Initialize if needed
		if (editLayoutTurnoutFrame == null) {
            editLayoutTurnoutFrame = new JmriJFrame( rb.getString("EditTurnout") );
            editLayoutTurnoutFrame.addHelpMenu("package.jmri.jmrit.display.EditLayoutTurnout", true);
            editLayoutTurnoutFrame.setLocation(50,30);
            Container contentPane = editLayoutTurnoutFrame.getContentPane();        
            contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
			// setup turnout name
            JPanel panel1 = new JPanel(); 
            panel1.setLayout(new FlowLayout());
			JLabel turnoutNameLabel = new JLabel( rb.getString("Turnout")+" "+rb.getString("Name") );
            panel1.add(turnoutNameLabel);
            panel1.add(turnoutNameField);
            turnoutNameField.setToolTipText( rb.getString("EditTurnoutNameHint") );
            contentPane.add(panel1);
			// add continuing state choice, if not crossover
			if ( (type != DOUBLE_XOVER) && (type != RH_XOVER) && (type != LH_XOVER) ) { 
				JPanel panel3 = new JPanel(); 
				panel3.setLayout(new FlowLayout());
				stateBox.removeAllItems();
				stateBox.addItem( InstanceManager.turnoutManagerInstance().getClosedText() );
				turnoutClosedIndex = 0;
				stateBox.addItem( InstanceManager.turnoutManagerInstance().getThrownText() );
				turnoutThrownIndex = 1;
				stateBox.setToolTipText(rb.getString("StateToolTip"));
				panel3.add (new JLabel(rb.getString("ContinuingState")));
				panel3.add (stateBox);
				contentPane.add(panel3);
			}
			// setup block name
            JPanel panel2 = new JPanel(); 
            panel2.setLayout(new FlowLayout());
			JLabel blockNameLabel = new JLabel( rb.getString("BlockID"));
            panel2.add(blockNameLabel);
            panel2.add(blockNameField);
            blockNameField.setToolTipText( rb.getString("EditBlockNameHint") );
            contentPane.add(panel2);
			if ( (type == DOUBLE_XOVER) || (type == RH_XOVER) || (type == LH_XOVER) ) { 
				JPanel panel21 = new JPanel(); 
				panel21.setLayout(new FlowLayout());
				JLabel blockBNameLabel = new JLabel( rb.getString("Block2ID"));
				panel21.add(blockBNameLabel);
				panel21.add(blockBNameField);
				blockBNameField.setToolTipText( rb.getString("EditBlockBNameHint") );
				contentPane.add(panel21);
				JPanel panel22 = new JPanel(); 
				panel22.setLayout(new FlowLayout());
				JLabel blockCNameLabel = new JLabel( rb.getString("Block3ID"));
				panel22.add(blockCNameLabel);
				panel22.add(blockCNameField);
				blockCNameField.setToolTipText( rb.getString("EditBlockCNameHint") );
				contentPane.add(panel22);
				JPanel panel23 = new JPanel(); 
				panel23.setLayout(new FlowLayout());
				JLabel blockDNameLabel = new JLabel( rb.getString("Block4ID"));
				panel23.add(blockDNameLabel);
				panel23.add(blockDNameField);
				blockDNameField.setToolTipText( rb.getString("EditBlockDNameHint") );
				contentPane.add(panel23);
			}
			// set up Edit Block, Done and Cancel buttons
            JPanel panel5 = new JPanel();
            panel5.setLayout(new FlowLayout());
			// Edit Block
            panel5.add(turnoutEditBlock = new JButton(rb.getString("EditBlock")));
            turnoutEditBlock.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    turnoutEditBlockPressed(e);
                }
            });
            turnoutEditBlock.setToolTipText( rb.getString("EditBlockHint") );
			// Done
            panel5.add(turnoutEditDone = new JButton(rb.getString("Done")));
            turnoutEditDone.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    turnoutEditDonePressed(e);
                }
            });
            turnoutEditDone.setToolTipText( rb.getString("DoneHint") );
			// Cancel
            panel5.add(turnoutEditCancel = new JButton(rb.getString("Cancel")));
            turnoutEditCancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    turnoutEditCancelPressed(e);
                }
            });
            turnoutEditCancel.setToolTipText( rb.getString("CancelHint") );
            contentPane.add(panel5);
			if ( (type == DOUBLE_XOVER) || (type == RH_XOVER) || (type == LH_XOVER) ) {
				JPanel panel6 = new JPanel();
				panel6.setLayout(new FlowLayout());
				// Edit Block 2
				panel6.add(turnoutEditBlockB = new JButton(rb.getString("EditBlock2")));
				turnoutEditBlockB.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						turnoutEditBlockBPressed(e);
					}
				});
				turnoutEditBlockB.setToolTipText( rb.getString("EditBlockBHint") );
				// Edit Block 3
				panel6.add(turnoutEditBlockC = new JButton(rb.getString("EditBlock3")));
				turnoutEditBlockC.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						turnoutEditBlockCPressed(e);
					}
				});
				turnoutEditBlockC.setToolTipText( rb.getString("EditBlockCHint") );
				// Edit Block 4
				panel6.add(turnoutEditBlockD = new JButton(rb.getString("EditBlock4")));
				turnoutEditBlockD.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						turnoutEditBlockDPressed(e);
					}
				});
				turnoutEditBlockD.setToolTipText( rb.getString("EditBlockDHint") );
				contentPane.add(panel6);
			}
		}
		// Set up for Edit
		blockNameField.setText(blockName);
		if ( (type == DOUBLE_XOVER) || (type == RH_XOVER) || (type == LH_XOVER) ) {
			blockBNameField.setText(blockBName);
			blockCNameField.setText(blockCName);
			blockDNameField.setText(blockDName);
		}	
		turnoutNameField.setText(turnoutName);
		if ( (type != DOUBLE_XOVER) && (type != RH_XOVER) && (type != LH_XOVER) ) {
			if (continuingSense==Turnout.CLOSED) {
				stateBox.setSelectedIndex(turnoutClosedIndex);
			}
			else {
				stateBox.setSelectedIndex(turnoutThrownIndex);
			}
		}
		editLayoutTurnoutFrame.addWindowListener(new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent e) {
					turnoutEditCancelPressed(null);
				}
			});
        editLayoutTurnoutFrame.pack();
        editLayoutTurnoutFrame.setVisible(true);		
		editOpen = true;
		needsBlockUpdate = false;
	}	
	void turnoutEditBlockPressed(ActionEvent a) {
		// check if a block name has been entered
		if (!blockName.equals(blockNameField.getText().trim()) ) {
			// block has changed, if old block exists, decrement use
			if ( (block!=null) && (block!=blockB) && (block!=blockC)
							&& (block!=blockD) ) {
				block.decrementUse();
			}
			// get new block, or null if block has been removed
			blockName = blockNameField.getText().trim();
			block = layoutEditor.provideLayoutBlock(blockName);
			if (block==null) {
				blockName = "";
			}
			// decrement use if block was already counted
			if ( (block!=null) && ( (block==blockB) || (block==blockC) ||
					(block==blockD) ) ) block.decrementUse();
			needRedraw = true;
			needsBlockUpdate = true;
		}
		// check if a block exists to edit
		if (block==null) {
			JOptionPane.showMessageDialog(editLayoutTurnoutFrame,
					rb.getString("Error1"),
					rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
			return;
		}
		block.editLayoutBlock(editLayoutTurnoutFrame);
		needRedraw = true;
		layoutEditor.setDirty();
	}
	void turnoutEditBlockBPressed(ActionEvent a) {
		// check if a block name has been entered
		if (!blockBName.equals(blockBNameField.getText().trim()) ) {
			// block has changed, if old block exists, decrement use
			if ( (blockB!=null) && (block!=blockB) && (blockB!=blockC)
							&& (blockB!=blockD) ) {
				blockB.decrementUse();
			}
			// get new block, or null if block has been removed
			blockBName = blockBNameField.getText().trim();
			blockB = layoutEditor.provideLayoutBlock(blockBName);
			if (blockB==null) {
				blockBName = "";
			}
			// decrement use if block was already counted
			if ( (blockB!=null) && ( (block==blockB) || (blockB==blockC) ||
					(blockB==blockD) ) ) blockB.decrementUse();
			needRedraw = true;
			needsBlockUpdate = true;
		}
		// check if a block exists to edit
		if (blockB==null) {
			JOptionPane.showMessageDialog(editLayoutTurnoutFrame,
					rb.getString("Error1"),
					rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
			return;
		}
		blockB.editLayoutBlock(editLayoutTurnoutFrame);
		needRedraw = true;
		layoutEditor.setDirty();
	}
	void turnoutEditBlockCPressed(ActionEvent a) {
		// check if a block name has been entered
		if (!blockCName.equals(blockCNameField.getText().trim()) ) {
			// block has changed, if old block exists, decrement use
			if ( (blockC!=null) && (block!=blockC) && (blockB!=blockC)
							&& (blockC!=blockD) ) {
				blockC.decrementUse();
			}
			// get new block, or null if block has been removed
			blockCName = blockCNameField.getText().trim();
			blockC = layoutEditor.provideLayoutBlock(blockCName);
			if (blockC==null) {
				blockCName = "";
			}
			// decrement use if block was already counted
			if ( (blockC!=null) && ( (block==blockC) || (blockB==blockC) ||
					(blockC==blockD) ) ) blockD.decrementUse();
			needRedraw = true;
			needsBlockUpdate = true;
		}
		// check if a block exists to edit
		if (blockC==null) {
			JOptionPane.showMessageDialog(editLayoutTurnoutFrame,
					rb.getString("Error1"),
					rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
			return;
		}
		blockC.editLayoutBlock(editLayoutTurnoutFrame);
		needRedraw = true;
		layoutEditor.setDirty();
	}
	void turnoutEditBlockDPressed(ActionEvent a) {
		// check if a block name has been entered
		if (!blockDName.equals(blockDNameField.getText().trim()) ) {
			// block has changed, if old block exists, decrement use
			if ( (blockD!=null) && (block!=blockD) && (blockB!=blockD)
							&& (blockC!=blockD) ) {
				blockD.decrementUse();
			}
			// get new block, or null if block has been removed
			blockDName = blockDNameField.getText().trim();
			blockD = layoutEditor.provideLayoutBlock(blockDName);
			if (blockD==null) {
				blockDName = "";
			}
			// decrement use if block was already counted
			if ( (blockD!=null) && ( (block==blockD) || (blockB==blockD) ||
					(blockC==blockD) ) ) blockD.decrementUse();
			needRedraw = true;
			needsBlockUpdate = true;
		}
		// check if a block exists to edit
		if (blockD==null) {
			JOptionPane.showMessageDialog(editLayoutTurnoutFrame,
					rb.getString("Error1"),
					rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
			return;
		}
		blockD.editLayoutBlock(editLayoutTurnoutFrame);
		needRedraw = true;
		layoutEditor.setDirty();
	}
	void turnoutEditDonePressed(ActionEvent a) {
		// check if Turnout changed
		if ( !turnoutName.equals(turnoutNameField.getText().trim()) ) {
			// turnout has changed
			String newName = turnoutNameField.getText().trim();
			if ( layoutEditor.validatePhysicalTurnout(newName,
							editLayoutTurnoutFrame) ) {
				setTurnout(newName);
			}
			else {
				turnout = null;
				turnoutName = "";
				turnoutNameField.setText("");
			}
			needRedraw = true;
		}
		// set the continuing route Turnout State
		if ( (type==DOUBLE_XOVER) || (type==LH_XOVER) || (type==RH_XOVER) ) {
			continuingSense = Turnout.CLOSED;
			if ( stateBox.getSelectedIndex() == turnoutThrownIndex ) {
				continuingSense = Turnout.THROWN;
			}
		}
		// check if Block changed
		if ( !blockName.equals(blockNameField.getText().trim()) ) {
			// block has changed, if old block exists, decrement use
			if ( (block!=null) && (block!=blockB) && (block!=blockC) &&
					(block!=blockD) ) {
				block.decrementUse();
			}
			// get new block, or null if block has been removed
			blockName = blockNameField.getText().trim();
			block = layoutEditor.provideLayoutBlock(blockName);
			if (block==null) {
				blockName = "";
			}
			// decrement use if block was already counted
			if ( (block!=null) && ( (block==blockB) || (block==blockC) ||
					(block==blockD) ) ) block.decrementUse();
			needRedraw = true;
			needsBlockUpdate = true;
		}
		if ( (type==DOUBLE_XOVER) || (type==LH_XOVER) || (type==RH_XOVER) ) {
			// check if Block 2 changed
			if ( !blockBName.equals(blockBNameField.getText().trim()) ) {
				// block has changed, if old block exists, decrement use
				if ( (blockB!=null) && (block!=blockB) && (blockB!=blockC)
							&& (blockB!=blockD) ) {
					blockB.decrementUse();
				}
				// get new block, or null if block has been removed
				blockBName = blockBNameField.getText().trim();
				blockB = layoutEditor.provideLayoutBlock(blockBName);
				if (blockB==null) {
					blockBName = "";
				}
				// decrement use if block was already counted
				if ( (blockB!=null) && ( (block==blockB) || (blockB==blockC) ||
						(blockB==blockD) ) ) blockB.decrementUse();
				needRedraw = true;
				needsBlockUpdate = true;
			}
			// check if Block 3 changed
			if (!blockCName.equals(blockCNameField.getText().trim()) ) {
				// block has changed, if old block exists, decrement use
				if ( (blockC!=null) && (block!=blockC) && (blockB!=blockC)
							&& (blockC!=blockD) ) {
					blockC.decrementUse();
				}
				// get new block, or null if block has been removed
				blockCName = blockCNameField.getText().trim();
				blockC = layoutEditor.provideLayoutBlock(blockCName);
				if (blockC==null) {
					blockCName = "";
				}
				// decrement use if block was already counted
				if ( (blockC!=null) && ( (block==blockC) || (blockB==blockC) ||
						(blockC==blockD) ) ) blockC.decrementUse();
				needRedraw = true;
				needsBlockUpdate = true;
			}
			// check if Block 4 changed
			if (!blockDName.equals(blockDNameField.getText().trim()) ) {
				// block has changed, if old block exists, decrement use
				if ( (blockD!=null) && (block!=blockD) && (blockB!=blockD)
							&& (blockC!=blockD) ) {
					blockD.decrementUse();
				}
				// get new block, or null if block has been removed
				blockDName = blockDNameField.getText().trim();
				blockD = layoutEditor.provideLayoutBlock(blockDName);
				if (blockD==null) {
					blockDName = "";
				}
				// decrement use if block was already counted
				if ( (blockD!=null) && ( (block==blockD) || (blockB==blockD) ||
						(blockC==blockD) ) ) blockD.decrementUse();
				needRedraw = true;
				needsBlockUpdate = true;
			}
		}
		editOpen = false;
		editLayoutTurnoutFrame.setVisible(false);
		if (needsBlockUpdate) updateBlockInfo();
		if (needRedraw) {
			layoutEditor.redrawPanel();
			layoutEditor.setDirty();
		}
	}
	void turnoutEditCancelPressed(ActionEvent a) {
		editOpen = false;
		editLayoutTurnoutFrame.setVisible(false);
		if (needsBlockUpdate) updateBlockInfo();
		if (needRedraw) {
			layoutEditor.redrawPanel();
			layoutEditor.setDirty();
		}
	}

    /**
     * Clean up when this object is no longer needed.  Should not
     * be called while the object is still displayed; see remove()
     */
    void dispose() {
    }

    /**
     * Removes this object from display and persistance
     */
    void remove() {
		// if a turnout has been activated, deactivate it
		deactivateTurnout();
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

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LayoutTurnout.class.getName());

}