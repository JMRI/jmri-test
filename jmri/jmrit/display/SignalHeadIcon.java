package jmri.jmrit.display;

import jmri.InstanceManager;
import jmri.SignalHead;
import jmri.jmrit.catalog.NamedIcon;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 * An icon to display a status of a SignalHead.
 * <P>
 * SignalHeads are located via the SignalHeadManager, which in turn is located
 * via the InstanceManager.
 *
 * @see jmri.SignalHeadManager
 * @see jmri.InstanceManager
 * @author Bob Jacobsen Copyright (C) 2001, 2002
 * @version $Revision: 1.22 $
 */

public class SignalHeadIcon extends PositionableLabel implements java.beans.PropertyChangeListener {

    public SignalHeadIcon() {
        // super ctor call to make sure this is an icon label
        super(new NamedIcon("resources/icons/smallschematics/searchlights/left-red-marker.gif",
                            "resources/icons/smallschematics/searchlights/left-red-marker.gif"));
        icon = true;
        text = false;

        displayState(SignalHead.RED);
    }

    private SignalHead mHead;

    /**
     * Attached a numbered element to this display item
     * @param pName Used as a system/user name to lookup the SignalHead object
     */
    public void setSignalHead(String pName) {
        mHead = InstanceManager.signalHeadManagerInstance().getBySystemName(pName);
        if (mHead == null) mHead = InstanceManager.signalHeadManagerInstance().getByUserName(pName);
        if (mHead == null) log.warn("did not find a SignalHead named "+pName);
        else {
            displayState(headState());
            mHead.addPropertyChangeListener(this);
            setProperToolTip();
        }
    }

    public SignalHead getSignalHead() {
        return mHead;
    }

    // display icons
    String redName = "resources/icons/smallschematics/searchlights/left-red-marker.gif";
    NamedIcon red = new NamedIcon(redName, redName);

    String flashRedName = "resources/icons/smallschematics/searchlights/left-flashred-marker.gif";
    NamedIcon flashRed = new NamedIcon(flashRedName, flashRedName);

    String yellowName = "resources/icons/smallschematics/searchlights/left-yellow-marker.gif";
    NamedIcon yellow = new NamedIcon(yellowName, yellowName);

    String flashYellowName = "resources/icons/smallschematics/searchlights/left-flashyellow-marker.gif";
    NamedIcon flashYellow = new NamedIcon(flashYellowName, flashYellowName);

    String greenName = "resources/icons/smallschematics/searchlights/left-green-marker.gif";
    NamedIcon green = new NamedIcon(greenName, greenName);

    String flashGreenName = "resources/icons/smallschematics/searchlights/left-flashgreen-marker.gif";
    NamedIcon flashGreen = new NamedIcon(flashGreenName, flashGreenName);

    String darkName = "resources/icons/smallschematics/searchlights/left-dark-marker.gif";
    NamedIcon dark = new NamedIcon(darkName, darkName);

    public NamedIcon getDarkIcon() { return dark; }
    public void setDarkIcon(NamedIcon i) {
        dark = i;
        displayState(headState());
    }

    public NamedIcon getRedIcon() { return red; }
    public void setRedIcon(NamedIcon i) {
        red = i;
        displayState(headState());
    }

    public NamedIcon getFlashRedIcon() { return flashRed; }
    public void setFlashRedIcon(NamedIcon i) {
        flashRed = i;
        displayState(headState());
    }

    public NamedIcon getYellowIcon() { return yellow; }
    public void setYellowIcon(NamedIcon i) {
        yellow = i;
        displayState(headState());
    }

    public NamedIcon getFlashYellowIcon() { return flashYellow; }
    public void setFlashYellowIcon(NamedIcon i) {
        flashYellow = i;
        displayState(headState());
    }

    public NamedIcon getGreenIcon() { return green; }
    public void setGreenIcon(NamedIcon i) {
        green = i;
        displayState(headState());
    }

    public NamedIcon getFlashGreenIcon() { return flashGreen; }
    public void setFlashGreenIcon(NamedIcon i) {
        flashGreen = i;
        displayState(headState());
    }

    protected int maxHeight() {
        return Math.max(
                Math.max( (red!=null) ? red.getIconHeight() : 0,
                        (green!=null) ? green.getIconHeight() : 0),
                Math.max((yellow!=null) ? yellow.getIconHeight() : 0,
                        (flashYellow!=null) ? flashYellow.getIconHeight() : 0)
            );
    }
    protected int maxWidth() {
        return Math.max(
                Math.max((red!=null) ? red.getIconWidth() : 0,
                        (green!=null) ? green.getIconWidth() : 0),
                Math.max((yellow!=null) ? yellow.getIconWidth() : 0,
                        (flashYellow!=null) ? flashYellow.getIconWidth() : 0)
            );
    }

    /**
     * Get current appearance of the head
     * @return An appearance variable from a SignalHead, e.g. SignalHead.RED
     */
    public int headState() {
        if (mHead==null) return 0;
        else return mHead.getAppearance();
    }

    // update icon as state of turnout changes
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (log.isDebugEnabled()) log.debug("property change: "+e.getPropertyName()
                                            +" current state: "+headState());
        displayState(headState());
    }

    public void setProperToolTip() {
        setToolTipText(getNameString());
    }

    String getNameString() {
        String name;
        if (mHead == null) name = "<Not connected>";
        else if (mHead.getUserName() == null)
            name = mHead.getSystemName();
        else
            name = mHead.getUserName()+" ("+mHead.getSystemName()+")";
        return name;
    }

    /**
     * Pop-up just displays the name
     */
    protected void showPopUp(MouseEvent e) {
        if (!getEditable()) return;
        ours = this;
        if (popup==null) {
            popup = new JPopupMenu();
            popup.add(new JMenuItem(getNameString()));
            if (icon) popup.add(new AbstractAction("Rotate") {
                    public void actionPerformed(ActionEvent e) {
                        green.setRotation(green.getRotation()+1, ours);
                        red.setRotation(red.getRotation()+1, ours);
                        yellow.setRotation(yellow.getRotation()+1, ours);
                        flashYellow.setRotation(flashYellow.getRotation()+1, ours);
                        displayState(headState());
                    }
                });

            addDisableMenuEntry(popup);
            
            popup.add(new AbstractAction("Remove") {
                public void actionPerformed(ActionEvent e) {
                    remove();
                    dispose();
                }
            });

            popup.add(new AbstractAction("Edit Logic...") {
                public void actionPerformed(ActionEvent e) {
                    jmri.jmrit.blockboss.BlockBossFrame f = new jmri.jmrit.blockboss.BlockBossFrame();
                    String name;
                    if (mHead.getUserName()==null || mHead.getUserName().equals(""))
                        name = mHead.getSystemName();
                    else
                        name = mHead.getUserName();
                    f.setTitle("Signal logic for"+name);
                    f.setSignal(name);
                    f.show();
                }
            });
        } // end creation of pop-up menu

        popup.show(e.getComponent(), e.getX(), e.getY());
    }

    /**
     * Drive the current state of the display from the state of the
     * turnout.
     */
    public void displayState(int state) {
        if (mHead == null)
            log.debug("Display state "+state+", disconnected");
        else
            log.debug("Display state "+state+" for "+mHead.getSystemName());
        updateSize();
        switch (state) {
        case SignalHead.RED:
            if (text) super.setText("<red>");
            if (icon) super.setIcon(red);
            break;
        case SignalHead.FLASHRED:
            if (text) super.setText("<flash red>");
            if (icon) super.setIcon(flashRed);
            break;
        case SignalHead.YELLOW:
            if (text) super.setText("<yellow>");
            if (icon) super.setIcon(yellow);
            break;
        case SignalHead.FLASHYELLOW:
            if (text) super.setText("<flash yellow>");
            if (icon) super.setIcon(flashYellow);
            break;
        case SignalHead.GREEN:
            if (text) super.setText("<green>");
            if (icon) super.setIcon(green);
            break;
        case SignalHead.FLASHGREEN:
            if (text) super.setText("<flash green>");
            if (icon) super.setIcon(flashGreen);
            break;
        case SignalHead.DARK:
            if (text) super.setText("<dark>");
            if (icon) super.setIcon(dark);
            break;

        default:
            log.error("unexpected state during display: "+state);
        }

        return;
    }

    /**
     * Change the SignalHead state when the icon is clicked.
     * Note that this change may not be permanent if there is
     * logic controlling the signal head.
     * @param e
     */
    public void mouseClicked(java.awt.event.MouseEvent e) {
        if (!getControlling()) return;
        if (getForceControlOff()) return;
        if (e.isMetaDown() || e.isAltDown() ) return;
        if (mHead==null) {
            log.error("No turnout connection, can't process click");
            return;
        }
        switch (mHead.getAppearance()) {
        case jmri.SignalHead.RED:
        case jmri.SignalHead.FLASHRED:
            mHead.setAppearance(jmri.SignalHead.YELLOW);
            break;
        case jmri.SignalHead.YELLOW:
        case jmri.SignalHead.FLASHYELLOW:
            mHead.setAppearance(jmri.SignalHead.GREEN);
            break;
        case jmri.SignalHead.GREEN:
        case jmri.SignalHead.FLASHGREEN:
            mHead.setAppearance(jmri.SignalHead.RED);
            break;
        default:
            mHead.setAppearance(jmri.SignalHead.RED);
        }
    }

    private static boolean warned = false;

    public void dispose() {
        mHead.removePropertyChangeListener(this);
        mHead = null;

        red = null;
        yellow = null;
        flashYellow = null;
        green = null;

        super.dispose();
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SignalHeadIcon.class.getName());
}
