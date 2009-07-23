package jmri.jmrit.display;

import jmri.InstanceManager;
import jmri.Sensor;
import jmri.jmrit.catalog.NamedIcon;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
//import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JCheckBoxMenuItem;

/**
 * An icon to display a status of a Sensor.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @version $Revision: 1.37 $
 */

public class SensorIcon extends PositionableLabel implements java.beans.PropertyChangeListener {

    public SensorIcon() {
        // super ctor call to make sure this is an icon label
        super(new NamedIcon("resources/icons/smallschematics/tracksegments/circuit-error.gif",
                            "resources/icons/smallschematics/tracksegments/circuit-error.gif"));
        icon = true;
        text = false;

        setDisplayLevel(PanelEditor.SENSORS);
        displayState(sensorState());
    }

    // the associated Sensor object
    Sensor sensor = null;

    /**
     * Attached a named sensor to this display item
     * @param pName System/user name to lookup the sensor object
     */
    public void setSensor(String pName) {
        if (InstanceManager.sensorManagerInstance()!=null) {
            sensor = InstanceManager.sensorManagerInstance().provideSensor(pName);
            if (sensor != null) {
                displayState(sensorState());
                sensor.addPropertyChangeListener(this);
                setProperToolTip();
            } else {
                log.error("Sensor '"+pName+"' not available, icon won't see changes");
            }
        } else {
            log.error("No SensorManager for this protocol, icon won't see changes");
        }
    }
    /**
     * Attached a named sensor to this display item
     * @param s the Sensor
     */
    public void setSensor(Sensor s) {
        if (sensor != null) {
            sensor.removePropertyChangeListener(this);
        }
        sensor = s;
        if (sensor != null) {
            displayState(sensorState());
            sensor.addPropertyChangeListener(this);
            setProperToolTip();
        }
    }

    public Sensor getSensor() {
        return sensor;
    }

    // display icons
    String activeName = "resources/icons/smallschematics/tracksegments/circuit-occupied.gif";
    NamedIcon active = new NamedIcon(activeName, activeName);

    String inactiveName = "resources/icons/smallschematics/tracksegments/circuit-empty.gif";
    NamedIcon inactive = new NamedIcon(inactiveName, inactiveName);

    String inconsistentName = "resources/icons/smallschematics/tracksegments/circuit-error.gif";
    NamedIcon inconsistent = new NamedIcon(inconsistentName, inconsistentName);

    String unknownName = "resources/icons/smallschematics/tracksegments/circuit-error.gif";
    NamedIcon unknown = new NamedIcon(unknownName, unknownName);

    public NamedIcon getActiveIcon() { return active; }
    public void setActiveIcon(NamedIcon i) {
        active = i;
        displayState(sensorState());
    }

    public NamedIcon getInactiveIcon() { return inactive; }
    public void setInactiveIcon(NamedIcon i) {
        inactive = i;
        displayState(sensorState());
    }

    public NamedIcon getInconsistentIcon() { return inconsistent; }
    public void setInconsistentIcon(NamedIcon i) {
        inconsistent = i;
        displayState(sensorState());
    }

    public NamedIcon getUnknownIcon() { return unknown; }
    public void setUnknownIcon(NamedIcon i) {
        unknown = i;
        displayState(sensorState());
    }

    /**
     * Get current state of attached sensor
     * @return A state variable from a Sensor, e.g. Sensor.ACTIVE
     */
    int sensorState() {
        if (sensor != null) return sensor.getKnownState();
        else return Sensor.UNKNOWN;
    }

    // update icon as state of turnout changes
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (log.isDebugEnabled()) log.debug("property change: "+e);
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
        if (sensor == null) name = rb.getString("NotConnected");
        else if (sensor.getUserName()!=null) {
            name = sensor.getUserName();
            if (sensor.getSystemName()!=null) name = name+" ("+sensor.getSystemName()+")";
        } else
            name = sensor.getSystemName();
        return name;
    }

    /**
     * Pop-up just displays the sensor name
     */
    protected void showPopUp(MouseEvent e) {
        if (!getEditable()) return;
        ours = this;
        popup = new JPopupMenu();

        popup.add(new JMenuItem(getNameString()));

        checkLocationEditable(popup, getNameString());

        if (icon) popup.add(new AbstractAction(rb.getString("Rotate")) {
                public void actionPerformed(ActionEvent e) {
                    active.setRotation(active.getRotation()+1, ours);
                    inactive.setRotation(inactive.getRotation()+1, ours);
                    unknown.setRotation(unknown.getRotation()+1, ours);
                    inconsistent.setRotation(inconsistent.getRotation()+1, ours);
                    displayState(sensorState());
                }
            });

        addDisableMenuEntry(popup);

        momentaryItem = new JCheckBoxMenuItem(rb.getString("Momentary"));
        popup.add(momentaryItem);
        momentaryItem.setSelected (getMomentary());
        momentaryItem.addActionListener(new ActionListener(){
            public void actionPerformed(java.awt.event.ActionEvent e) {
                setMomentary(momentaryItem.isSelected());
            }
        });

        popup.add(new AbstractAction(rb.getString("EditIcon")) {
                public void actionPerformed(ActionEvent e) {
                    edit();
                }
            });
        addTextEditEntry(popup);

        popup.add(new AbstractAction(rb.getString("Remove")) {
                public void actionPerformed(ActionEvent e) {
                    remove();
                    dispose();
                }
            });

        popup.show(e.getComponent(), e.getX(), e.getY());
    }

    JCheckBoxMenuItem momentaryItem;
    
    /**
     * Drive the current state of the display from the state of the
     * turnout.
     */
    void displayState(int state) {

        updateSize();

        switch (state) {
        case Sensor.UNKNOWN:
            if (icon) super.setIcon(unknown);
            break;
        case Sensor.ACTIVE:
            if (icon) super.setIcon(active);
            break;
        case Sensor.INACTIVE:
            if (icon) super.setIcon(inactive);
            break;
        default:
            if (icon) super.setIcon(inconsistent);
            break;
        }
        setIconTextGap (-(getWidth()+getPreferredSize().width)/2);
        setSize(getPreferredSize().width, getPreferredSize().height);

        return;
    }

    void edit() {
        if (_editorFrame != null) {
            _editorFrame.setLocationRelativeTo(null);
            _editorFrame.toFront();
            return;
        }
        _editor = new IconAdder();
        _editor.setIcon(3, "SensorStateActive", getActiveIcon());
        _editor.setIcon(2, "SensorStateInactive", getInactiveIcon());
        _editor.setIcon(0, "BeanStateInconsistent", getInconsistentIcon());
        _editor.setIcon(1, "BeanStateUnknown", getUnknownIcon());

        makeAddIconFrame("EditSensor", "addIconsToPanel", "SelectSensor", _editor);
        _editor.makeIconPanel();
        _editor.setPickList(PickListModel.sensorPickModelInstance());


        ActionListener addIconAction = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                updateSensor();
            }
        };
        ActionListener changeIconAction = new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    _editor.addCatalog();
                    _editorFrame.pack();
                }
        };
        _editor.complete(addIconAction, changeIconAction, true);
        _editor.setSelection(sensor);
    }
    void updateSensor() {
        setActiveIcon(_editor.getIcon("SensorStateActive"));
        setInactiveIcon(_editor.getIcon("SensorStateInactive"));
        setInconsistentIcon(_editor.getIcon("BeanStateInconsistent"));
        setUnknownIcon(_editor.getIcon("BeanStateUnknown"));
        setSensor((Sensor)_editor.getTableSelection());
        _editorFrame.dispose();
        _editorFrame = null;
        _editor = null;
        invalidate();
    }

    public void setText(String s) {
        text = true;
        super.setText(s);
    }

    protected int maxHeight() {
        return Math.max(
                Math.max( (active!=null) ? active.getIconHeight() : 0,
                        (inactive!=null) ? inactive.getIconHeight() : 0),
                Math.max((unknown!=null) ? unknown.getIconHeight() : 0,
                        (inconsistent!=null) ? inconsistent.getIconHeight() : 0)
            );
    }
    protected int maxWidth() {
        return Math.max(
                Math.max((active!=null) ? active.getIconWidth() : 0,
                        (inactive!=null) ? inactive.getIconWidth() : 0),
                Math.max((unknown!=null) ? unknown.getIconWidth() : 0,
                        (inconsistent!=null) ? inconsistent.getIconWidth() : 0)
            );
    }

    boolean momentary = false;
    public boolean getMomentary() { return momentary; }
    public void setMomentary(boolean m) { momentary = m; }

    boolean buttonLive() {
        if (!getControlling()) return false;
        if (getForceControlOff()) return false;
        if (sensor==null) {  // no sensor connected for this protocol
            log.error("No sensor connection, can't process click");
            return false;
        }
        return true;        
    }

    public void mousePressed(MouseEvent e) {
        if (getMomentary() && buttonLive()) {
            // this is a momentary button
            try {
                sensor.setKnownState(jmri.Sensor.ACTIVE);
            } catch (jmri.JmriException reason) {
                log.warn("Exception setting momentary sensor: "+reason);
            }        
        }
        // do rest of mouse processing
        super.mousePressed(e);
    }

    public void mouseReleased(MouseEvent e) {
        if (buttonLive()) {
            if (getMomentary()) {
                // this is a momentary button
                try {
                    sensor.setKnownState(jmri.Sensor.INACTIVE);
                } catch (jmri.JmriException reason) {
                    log.warn("Exception setting momentary sensor: "+reason);
                }        
            } else {
                try {
                    if (sensor.getKnownState()==jmri.Sensor.INACTIVE)
                        sensor.setKnownState(jmri.Sensor.ACTIVE);
                    else
                        sensor.setKnownState(jmri.Sensor.INACTIVE);
                } catch (jmri.JmriException reason) {
                    log.warn("Exception flipping sensor: "+reason);
                }
            }
        }
        // do rest of mouse processing
        super.mouseReleased(e);
    }
 
    public void dispose() {
        sensor.removePropertyChangeListener(this);
        sensor = null;

        active = null;
        inactive = null;
        inconsistent = null;
        unknown = null;

        super.dispose();
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SensorIcon.class.getName());
}
