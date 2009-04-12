package jmri.jmrit.display;

import jmri.InstanceManager;
import jmri.jmrit.catalog.NamedIcon;
import jmri.Sensor;
import jmri.jmrix.rps.*;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JOptionPane;
import javax.swing.JCheckBoxMenuItem;

/**
 * An icon to display the position of an RPS input.
 *
 * In this initial version, it ignores the ID, so there's only one icon.
 *
 * @author Bob Jacobsen Copyright (C) 2007
 * @version $Revision: 1.13 $
 */

public class RpsPositionIcon extends PositionableLabel implements MeasurementListener {

    public RpsPositionIcon() {
        // super ctor call to make sure this is an icon label
        super(new NamedIcon("resources/icons/smallschematics/tracksegments/circuit-error.gif",
                            "resources/icons/smallschematics/tracksegments/circuit-error.gif"));
        icon = true;
        text = false;

        setDisplayLevel(PanelEditor.SENSORS);
        displayState();
        
        // blow up default font
        setFont(jmri.util.FontUtil.deriveFont(getFont(), (float)24.));
        
        // connect
        Distributor.instance().addMeasurementListener(this);
    }

    // display icon for a correct reading
    String activeName = "resources/icons/smallschematics/tracksegments/circuit-occupied.gif";
    NamedIcon active = new NamedIcon(activeName, activeName);

    // display icon if the last reading not OK
    String errorName = "resources/icons/smallschematics/tracksegments/circuit-error.gif";
    NamedIcon error = new NamedIcon(errorName, errorName);

    public NamedIcon getActiveIcon() { return active; }
    public void setActiveIcon(NamedIcon i) {
        active = i;
        displayState();
    }

    public NamedIcon getErrorIcon() { return error; }
    public void setErrorIcon(NamedIcon i) {
        error = i;
        displayState();
    }

    public void setProperToolTip() {
        setToolTipText(getNameString());
    }

    String getNameString() {
        return "RPS Position Readout";
    }

    /**
     * Pop-up contents
     */
    protected void showPopUp(MouseEvent e) {
        if (!getEditable()) return;
        ours = this;
        popup = new JPopupMenu();
        popup.add(new JMenuItem(getNameString()));
        
        checkLocationEditable(popup, getNameString());
        
        if (icon) popup.add(new AbstractAction("Rotate") {
                public void actionPerformed(ActionEvent e) {
                    active.setRotation(active.getRotation()+1, ours);
                    error.setRotation(error.getRotation()+1, ours);
                    displayState();
                }
            });

        if (showIdItem == null) {
            showIdItem = new JCheckBoxMenuItem("Show ID");
            showIdItem.setSelected(false);
            showIdItem.addActionListener(new ActionListener(){
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    toggleID(showIdItem.isSelected());
                }
            });
        }
        popup.add(showIdItem);

        popup.add(new AbstractAction("Set Origin") {
                public void actionPerformed(ActionEvent e) {
                    setRpsOrigin();
                }
            });

        popup.add(new AbstractAction("Set Current Location") {
                public void actionPerformed(ActionEvent e) {
                    setRpsCurrentLocation();
                }
            });

        notify = new Notifier();
        popup.add(notify);
        
        popup.add(new AbstractAction("Set Filter") {
                public void actionPerformed(ActionEvent e) {
                    setFilterPopup();
                }
            });

        addDisableMenuEntry(popup);
        
        popup.add(new AbstractAction("Remove") {
                public void actionPerformed(ActionEvent e) {
                    remove();
                    dispose();
                }
            });

        // add help item
        JMenuItem item = new JMenuItem("Help");
        jmri.util.HelpUtil.addHelpToComponent(item, "package.jmri.jmrit.display.RpsIcon");
        popup.add(item);
        
        // update position
        notify.setPosition(getX(), getY());
        popup.show(e.getComponent(), e.getX(), e.getY());
    }

    JCheckBoxMenuItem showIdItem = null;
    
    /** 
     * Internal class to show position in the popup menu.
     * <P>
     * This is updated before the menu is shown, and
     * then appears in the menu.
     */
    class Notifier extends AbstractAction {
        public Notifier() {
            super();
        }
        /** 
         * Does nothing, here to make this work
         */
        public void actionPerformed(ActionEvent e) {
        }

        /**
         * 
         * @param x display coordinate
         * @param y display coordinate
         */
        void setPosition(int x, int y){
            // convert to RPS coordinates
            double epsilon = .00001;
            if ( (sxScale>-epsilon && sxScale<epsilon) ||
                 (syScale>-epsilon && syScale<epsilon)) {
                putValue("Name", "Not Calibrated");
                return;
            }
            
            double xn = (x-sxOrigin)/sxScale;
            double yn = (y-syOrigin)/syScale;
        
            putValue("Name", "At: "+xn+","+yn);
        }
    }
    Notifier notify;
    
    JCheckBoxMenuItem momentaryItem;
    
    // true if valid message received last
    
    boolean state = false;
    
    /**
     * Drive the current state of the display from 
     * whether a valid measurement has been received
     */
    void displayState() {

        if (state) {
            if (icon) super.setIcon(active);
        } else {
            if (icon) super.setIcon(error);
        }
        
        updateSize();
        revalidate();
        return;
    }
    
    protected int maxHeight() {
        return getPreferredSize().height;
    }
    protected int maxWidth() {
        return getPreferredSize().width;
    }
    
    boolean momentary = false;
    public boolean getMomentary() { return momentary; }
    public void setMomentary(boolean m) { momentary = m; }
    
    void toggleID(boolean value) {
        if (value) {
            text = true;
        } else {
            text = false;
            setText(null);
        }
        displayState();
    }
    
    public boolean isShowID() { return text; }
    public void setShowID(boolean mode) { 
        text = mode;
        displayState();
    }
    
    /**
     * Respond to a measurement by moving to new position
     */
    public void notify(Measurement m) {
        // only honor measurements to this icon if filtered
        if (filterNumber != null && m.getReading() != null && 
                !filterNumber.equals(m.getReading().getID())) 
            return;
        
        // remember this measurement for last position, e.g. for
        // alignment    
        lastMeasurement = m;

        // update state based on if valid measurement, fiducial volume
        if (!m.isOkPoint() || m.getZ() < -20 || m.getZ() > 20)
            state = false;
        else 
            state = true;
            
        if (text) super.setText(""+m.getReading().getID());
        displayState();
        
        // if the state is bad, leave icon in last position
        
        if (!state) return;
        
        // Do a 2D, no-rotation conversion using the saved constants.
        // xn, yn are the RPS coordinates; x, y are the display coordinates.
        double xn = m.getX();
        double yn = m.getY();
        
        int x = sxOrigin+(int)(sxScale*xn);
        int y = syOrigin+(int)(syScale*yn);
    
        // and set position
        setLocation(x,y);
    }
    
    public void setFilterPopup(){
        // Popup menu has trigger request for filter value
        String inputValue = JOptionPane.showInputDialog("Please enter a filter value");
        if (inputValue == null) return; // cancelled
        setFilter(inputValue);
    }
    
    public void setFilter(String val) {
        filterNumber = val;
    }
    public String getFilter() { return filterNumber; }
    String filterNumber = null;
    
    /**
     * (Temporarily) change occupancy on click
     * @param e
     */
    public void mouseClicked(java.awt.event.MouseEvent e) {
        super.mouseClicked(e);
        if (e.isAltDown() || e.isMetaDown()) return;
        if (getMomentary()) return; // click is only for non-momentary
        if (!buttonLive()) return;

        // if the click is supposed to do something, do it here
    }

    boolean buttonLive() {
        if (!getControlling()) return false;
        if (getForceControlOff()) return false;
        return true;        
    }

    public void mousePressed(MouseEvent e) {
        if (getMomentary() && buttonLive()) {
            // this is a momentary button

                // take momentary action for mouse down

        }
        // do rest of mouse processing
        super.mousePressed(e);
    }

    public void mouseReleased(MouseEvent e) {
        if (getMomentary() && buttonLive()) {
            // this is a momentary button

                // take momentary action for mouse up

        }
        // do rest of mouse processing
        super.mouseReleased(e);
    }
 
    public void dispose() {
        Distributor.instance().removeMeasurementListener(this);
        active = null;
        error = null;

        super.dispose();
    }

    /**
     * Set the current icon position as the origin
     * (0,0) of the RPS space.
     */
    public void setRpsOrigin() {
        sxOrigin = getX();
        syOrigin = getY();
    }

    public double getXScale() { return sxScale; }
    public double getYScale() { return syScale; }
    
    public int getXOrigin() { return sxOrigin; }
    public int getYOrigin() { return syOrigin; }
    
    public void setTransform(double sxScale, double syScale, int sxOrigin, int syOrigin) {
        this.sxScale = sxScale;
        this.syScale = syScale;
        this.sxOrigin = sxOrigin;
        this.syOrigin = syOrigin;
    }
    /**
     * Matches the icon position on the screen to 
     * it's position in the RPS coordinate system.
     * <P>
     * Typically invoked from the popup menu, you 
     * move the icon (e.g. via drag and drop) to the 
     * correct position on the screen for it's current
     * measured position, and then invoke this method.
     * <P>
     * Requires the origin to have been set, and some
     * other measurement to have been made (and current).
     */
    public void setRpsCurrentLocation() {
        if (lastMeasurement == null) return;

        if (sxOrigin == getX()) return;
        if (syOrigin == getY()) return;
        // if (lastMeasurement.getX()<10. && lastMeasurement.getX()>-10) return;
        // if (lastMeasurement.getY()<10. && lastMeasurement.getY()>-10) return;
        
        sxScale = (getX()-sxOrigin)/lastMeasurement.getX();
        syScale = (getY()-syOrigin)/lastMeasurement.getY();
    }

    // store coordinate system information
    Measurement lastMeasurement;
    
    double sxScale, syScale;
    int sxOrigin, syOrigin;
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SensorIcon.class.getName());
}
