package jmri.jmrit.display;

import jmri.InstanceManager;
import jmri.Turnout;
import jmri.jmrit.catalog.NamedIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import jmri.util.NamedBeanHandle;

/**
 * An icon to display a status of a turnout.<P>
 * This responds to only KnownState, leaving CommandedState to some other
 * graphic representation later.
 * <P>
 * A click on the icon will command a state change. Specifically, it
 * will set the CommandedState to the opposite (THROWN vs CLOSED) of
 * the current KnownState.
 *<P>
 * The default icons are for a left-handed turnout, facing point
 * for east-bound traffic.
 * @author Bob Jacobsen  Copyright (c) 2002
 * @version $Revision: 1.47 $
 */

public class TurnoutIcon extends PositionableLabel implements java.beans.PropertyChangeListener {

    public TurnoutIcon() {
        // super ctor call to make sure this is an icon label
        super(new NamedIcon("resources/icons/smallschematics/tracksegments/os-lefthand-east-closed.gif",
                            "resources/icons/smallschematics/tracksegments/os-lefthand-east-closed.gif"));
        setDisplayLevel(PanelEditor.TURNOUTS);
        displayState(turnoutState());
        icon = true;
        text = false;
    }

    // the associated Turnout object
    //Turnout turnout = null;
    private NamedBeanHandle<Turnout> namedTurnout = null;

    /**
     * Attached a named turnout to this display item
     * @param pName Used as a system/user name to lookup the turnout object
     */
     public void setTurnout(String pName) {
         if (InstanceManager.turnoutManagerInstance()!=null) {
            Turnout turnout = InstanceManager.turnoutManagerInstance().
                 provideTurnout(pName);
             if (turnout != null) {
                 setTurnout(new NamedBeanHandle<Turnout>(pName, turnout));
             } else {
                 log.error("Turnout '"+pName+"' not available, icon won't see changes");
             }
         } else {
             log.error("No TurnoutManager for this protocol, icon won't see changes");
         }
     }

    public void setTurnout(NamedBeanHandle<Turnout> to) {
        if (namedTurnout != null) {
            getTurnout().removePropertyChangeListener(this);
        }
        namedTurnout = to;
        if (namedTurnout != null) {
            displayState(turnoutState());
            getTurnout().addPropertyChangeListener(this);
            setProperToolTip();
        } 
    }

    public Turnout getTurnout() { return namedTurnout.getBean(); }
    
    public NamedBeanHandle <Turnout> getNamedTurnout() {
        return namedTurnout;
    }

    // display icons
    String closedLName = "resources/icons/smallschematics/tracksegments/os-lefthand-east-closed.gif";
    NamedIcon closed = new NamedIcon(closedLName, closedLName);
    String thrownLName = "resources/icons/smallschematics/tracksegments/os-lefthand-east-thrown.gif";
    NamedIcon thrown = new NamedIcon(thrownLName, thrownLName);
    String inconsistentLName = "resources/icons/smallschematics/tracksegments/os-lefthand-east-error.gif";
    NamedIcon inconsistent = new NamedIcon(inconsistentLName, inconsistentLName);
    String unknownLName = "resources/icons/smallschematics/tracksegments/os-lefthand-east-unknown.gif";
    NamedIcon unknown = new NamedIcon(unknownLName, unknownLName);

    public NamedIcon getClosedIcon() { return closed; }
    public void setClosedIcon(NamedIcon i) {
        closed = i;
        displayState(turnoutState());
    }

    public NamedIcon getThrownIcon() { return thrown; }
    public void setThrownIcon(NamedIcon i) {
        thrown = i;
        displayState(turnoutState());
    }

    public NamedIcon getInconsistentIcon() { return inconsistent; }
    public void setInconsistentIcon(NamedIcon i) {
        inconsistent = i;
        displayState(turnoutState());
    }

    public NamedIcon getUnknownIcon() { return unknown; }
    public void setUnknownIcon(NamedIcon i) {
        unknown = i;
        displayState(turnoutState());
    }

    protected int maxHeight() {
        return Math.max(
                Math.max( (closed!=null) ? closed.getIconHeight() : 0,
                        (thrown!=null) ? thrown.getIconHeight() : 0),
                Math.max((unknown!=null) ? unknown.getIconHeight() : 0,
                        (inconsistent!=null) ? inconsistent.getIconHeight() : 0)
            );
    }
    protected int maxWidth() {
        return Math.max(
                Math.max((closed!=null) ? closed.getIconWidth() : 0,
                        (thrown!=null) ? thrown.getIconWidth() : 0),
                Math.max((unknown!=null) ? unknown.getIconWidth() : 0,
                        (inconsistent!=null) ? inconsistent.getIconWidth() : 0)
            );
    }

    /**
     * Get current state of attached turnout
     * @return A state variable from a Turnout, e.g. Turnout.CLOSED
     */
    int turnoutState() {
        if (namedTurnout != null) return getTurnout().getKnownState();
        else return Turnout.UNKNOWN;
    }
    
    // update icon as state of turnout changes
    public void propertyChange(java.beans.PropertyChangeEvent e) {
		if (log.isDebugEnabled())
			log.debug("property change: " + getNameString() + " " + e.getPropertyName() + " is now "
					+ e.getNewValue());

		// when there's feedback, transition through inconsistent icon for better
		// animation
		if (super.getTristate()
				&& (getTurnout().getFeedbackMode() != Turnout.DIRECT)
				&& (e.getPropertyName().equals("CommandedState"))) {
			if (getTurnout().getCommandedState() != getTurnout().getKnownState()) {
				int now = Turnout.INCONSISTENT;
				displayState(now);
			}
			// this takes care of the quick double click
			if (getTurnout().getCommandedState() == getTurnout().getKnownState()) {
				int now = ((Integer) e.getNewValue()).intValue();
				displayState(now);
			}
		}

		if (e.getPropertyName().equals("KnownState")) {
			int now = ((Integer) e.getNewValue()).intValue();
			displayState(now);
		}
	}

    public void setProperToolTip() {
        setToolTipText(getNameString());
    }

    public String getNameString() {
        String name;
        if (namedTurnout == null) name = rb.getString("NotConnected");
        else name = namedTurnout.getName();
        /*else if (getTurnout().getUserName()!=null)
            name = getTurnout().getUserName()+" ("+getTurnout().getSystemName()+")";
        else
            name = getTurnout().getSystemName();*/
        return name;
    }


    /**
     * Pop-up displays the turnout name, allows you to rotate the icons
     */
    protected void showPopUp(MouseEvent e) {
		if (!getEditable())
			return;
		ours = this;
// create popup each time called, tristate added if turnout has feedback
		popup = new JPopupMenu();
		popup.add(new JMenuItem(getNameString()));
		if (icon) popup.add(new AbstractAction(rb.getString("Rotate")) {
				public void actionPerformed(ActionEvent e) {
					closed.setRotation(closed.getRotation() + 1, ours);
					thrown.setRotation(thrown.getRotation() + 1, ours);
					unknown.setRotation(unknown.getRotation() + 1, ours);
					inconsistent.setRotation(inconsistent.getRotation() + 1,
							ours);
					displayState(turnoutState());
	                // bug fix, must repaint icons that have same width and height
                    repaint();
				}
			});
        checkLocationEditable(popup, getNameString());

        addFixedItem(popup);

		addDisableMenuEntry(popup);

		// add tristate option if turnout has feedback
		if (namedTurnout != null && getTurnout().getFeedbackMode() != Turnout.DIRECT) {
			addTristateEntry(popup);
		}

        popup.add(new AbstractAction(rb.getString("EditIcon")) {
                public void actionPerformed(ActionEvent e) {
                    edit();
                }
            });
		popup.add(new AbstractAction(rb.getString("Remove")) {
			public void actionPerformed(ActionEvent e) {
				remove();
				dispose();
			}
		});

		// end creation of pop-up menu
		popup.show(e.getComponent(), e.getX(), e.getY());
	}

    void scale(int s) {
        closed.scale(s, this);
        thrown.scale(s, this);
        unknown.scale(s, this);
        inconsistent.scale(s, this);
        displayState(turnoutState());
    }

    void rotate(int deg) {
        closed.rotate(deg, this);
        thrown.rotate(deg, this);
        unknown.rotate(deg, this);
        inconsistent.rotate(deg, this);
        displayState(turnoutState());
    }

    /**
	 * Drive the current state of the display from the state of the turnout.
	 */
    void displayState(int state) {
        log.debug(getNameString() +" displayState "+state);
        updateSize();
        switch (state) {
        case Turnout.UNKNOWN:
            if (text) super.setText(rb.getString("UnKnown"));
            if (icon) super.setIcon(unknown);
            break;
        case Turnout.CLOSED:
            if (text) super.setText(InstanceManager.turnoutManagerInstance().getClosedText());
            if (icon) super.setIcon(closed);
            break;
        case Turnout.THROWN:
            if (text) super.setText(InstanceManager.turnoutManagerInstance().getThrownText());
            if (icon) super.setIcon(thrown);
            break;
        default:
            if (text) super.setText(rb.getString("Inconsistent"));
            if (icon) super.setIcon(inconsistent);
            break;
        }

        return;
    }

    void edit() {
        if (_editorFrame != null) {
            _editorFrame.setLocationRelativeTo(null);
            _editorFrame.toFront();
            return;
        }
        _editor = new IconAdder();
        _editor.setIcon(3, "TurnoutStateClosed", getClosedIcon());
        _editor.setIcon(2, "TurnoutStateThrown", getThrownIcon());
        _editor.setIcon(0, "BeanStateInconsistent", getInconsistentIcon());
        _editor.setIcon(1, "BeanStateUnknown", getUnknownIcon());
        makeAddIconFrame("EditTO", "addIconsToPanel", 
                                           "SelectTO", _editor);
        _editor.makeIconPanel();
        _editor.setPickList(PickListModel.turnoutPickModelInstance());

        ActionListener addIconAction = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                updateTurnout();
            }
        };
        ActionListener changeIconAction = new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    _editor.addCatalog();
                    _editorFrame.pack();
                }
        };
        _editor.complete(addIconAction, changeIconAction, true, true);
        _editor.setSelection(getTurnout());
    }
    void updateTurnout() {
        setClosedIcon(_editor.getIcon("TurnoutStateClosed"));
        setThrownIcon(_editor.getIcon("TurnoutStateThrown"));
        setInconsistentIcon(_editor.getIcon("BeanStateInconsistent"));
        setUnknownIcon(_editor.getIcon("BeanStateUnknown"));
        setTurnout(_editor.getTableSelection().getDisplayName());
        _editorFrame.dispose();
        _editorFrame = null;
        _editor = null;
        invalidate();
    }

    /**
     * Throw the turnout when the icon is clicked
     * @param e
     */
    // Was mouseClicked, changed to mouseRelease to workaround touch screen driver limitation
    public void mouseReleased(java.awt.event.MouseEvent e) {
        super.mouseReleased(e);
        if (!getControlling()) return;
        if (getForceControlOff()) return;
        if (e.isMetaDown() || e.isAltDown() ) return;
        if (namedTurnout==null) {
            log.error("No turnout connection, can't process click");
            return;
        }
        if (getTurnout().getKnownState()==jmri.Turnout.CLOSED)
            getTurnout().setCommandedState(jmri.Turnout.THROWN);
        else
            getTurnout().setCommandedState(jmri.Turnout.CLOSED);
    }

    public void dispose() {
        if (namedTurnout != null) {
            getTurnout().removePropertyChangeListener(this);
        }
        namedTurnout = null;

        closed = null;
        thrown = null;
        inconsistent = null;
        unknown = null;

        super.dispose();
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TurnoutIcon.class.getName());
}
