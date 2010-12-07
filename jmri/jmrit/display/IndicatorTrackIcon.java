package jmri.jmrit.display;

import jmri.InstanceManager;
import jmri.Sensor;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.logix.OBlock;

import jmri.jmrit.display.palette.IndicatorItemPanel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPopupMenu;
import java.util.Hashtable;
import jmri.util.NamedBeanHandle;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * An icon to display the status of a track segment in a block
 * <P>
 * This responds to the following conditions: 
 *  1. KnownState of an occupancy sensor of the block where the track segment appears
 *  2. Allocation of a route by a Warrant where the track segment appears
 *  3. Current position of a train being run under a Warrant where the track segment appears in a block of the route
 *  4. Out of Service for a block that cannot or should not be used
 *  5. An error state of the block where the track segment appears (short/no power etc.) 
 * <P>
 * A click on the icon does not change any of the above conditions..
 *<P>
 * @author Pete Cressman  Copyright (c) 2010
 * @version $Revision: 1.5 $
 */

public class IndicatorTrackIcon extends PositionableLabel 
                        implements java.beans.PropertyChangeListener {

    Hashtable<String, NamedIcon> _iconMap;

    private NamedBeanHandle<Sensor> namedOccSensor = null;
    private NamedBeanHandle<OBlock> namedOccBlock = null;
    private NamedBeanHandle<Sensor> namedErrSensor = null;

    private String _status;     // is a key for _iconMap
    private String _train;
    private boolean _showTrain; // this track should display _train when occupied

    public IndicatorTrackIcon(Editor editor) {
        // super ctor call to make sure this is an icon label
        super(new NamedIcon("resources/icons/smallschematics/tracksegments/block.gif",
                            "resources/icons/smallschematics/tracksegments/block.gif"), editor);
        setPopupUtility(null);
        _status = "DontUseTrack";
        _iconMap = new Hashtable<String, NamedIcon>();
    }

    public Positionable deepClone() {
        IndicatorTrackIcon pos = new IndicatorTrackIcon(_editor);        
        return finishClone(pos);
    }

    public Positionable finishClone(Positionable p) {
        IndicatorTrackIcon pos = (IndicatorTrackIcon)p;
        pos.setOccSensorHandle(namedOccSensor);
        pos.setErrSensorHandle(namedErrSensor);
        pos.setOccBlockHandle(namedOccBlock);
        pos._iconMap = cloneMap(_iconMap, pos);
        return super.finishClone(pos);
    }

    public Hashtable<String, NamedIcon> getIconMap() {
        return _iconMap;
    }

    /**
     * Attached a named sensor to display status
     * @param pName Used as a system/user name to lookup the sensor object
     */
     public void setOccSensor(String pName) {
         if (pName==null || pName.trim().length()==0) {
             setOccSensorHandle(null);
             return;
         }
         if (InstanceManager.sensorManagerInstance()!=null) {
             Sensor sensor = InstanceManager.sensorManagerInstance().provideSensor(pName);
             if (sensor != null) {
                 setOccSensorHandle(new NamedBeanHandle<Sensor>(pName, sensor));                
             } else {
                 log.error("Occupancy Sensor '"+pName+"' not available, icon won't see changes");
             }
         } else {
             log.error("No SensorManager for this protocol, block icons won't see changes");
         }
     }
     public void setOccSensorHandle(NamedBeanHandle<Sensor> sen) {
         if (namedOccSensor != null) {
             getOccSensor().removePropertyChangeListener(this);
         }
         namedOccSensor = sen;
         if (namedOccSensor != null) {
             if (_iconMap==null) {
                 _iconMap = new Hashtable<String, NamedIcon>();
             }
             getOccSensor().addPropertyChangeListener(this);
         } 
     }
     public Sensor getOccSensor() {
         if (namedOccSensor==null) {
             return null;
         }
         return namedOccSensor.getBean(); 
     }    
     public NamedBeanHandle <Sensor> getNamedOccSensor() { return namedOccSensor; }

    /**
     * Attached a named OBlock to display status
     * @param pName Used as a system/user name to lookup the OBlock object
     */
     public void setOccBlock(String pName) {
         if (pName==null || pName.trim().length()==0) {
             setOccBlockHandle(null);
             return;
         }
         OBlock block = InstanceManager.oBlockManagerInstance().getOBlock(pName);
         if (block != null) {
             setOccBlockHandle(new NamedBeanHandle<OBlock>(pName, block));                
         } else {
             log.error("Detection OBlock '"+pName+"' not available, icon won't see changes");
         }
     }   
    public void setOccBlockHandle(NamedBeanHandle<OBlock> block) {
        if (namedOccBlock != null) {
            getOccBlock().removePropertyChangeListener(this);
        }
        namedOccBlock = block;
        if (namedOccBlock != null) {
            if (_iconMap==null) {
                _iconMap = new Hashtable<String, NamedIcon>();
            }
            getOccBlock().addPropertyChangeListener(this);
        } 
    }
    public OBlock getOccBlock() { 
        if (namedOccBlock==null) {
            return null;
        }
        return namedOccBlock.getBean(); 
    }    
    public NamedBeanHandle <OBlock> getNamedOccBlock() { return namedOccBlock; }

    /**
     * Attached a named sensor to display status from error detector
     * @param pName Used as a system/user name to lookup the sensor object
     */
     public void setErrSensor(String pName) {
         if (pName==null || pName.trim().length()==0) {
             setErrSensorHandle(null);
             return;
         }
         if (InstanceManager.sensorManagerInstance()!=null) {
             Sensor sensor = InstanceManager.sensorManagerInstance().provideSensor(pName);
             if (sensor != null) {
                 setErrSensorHandle(new NamedBeanHandle<Sensor>(pName, sensor));                
             } else {
                 log.error("Error Sensor '"+pName+"' not available, icon won't see changes");
             }
         } else {
             log.error("No SensorManager for this protocol, error icon won't see changes");
         }
     }

    public void setErrSensorHandle(NamedBeanHandle<Sensor> sen) {
        if (namedErrSensor != null) {
            getErrSensor().removePropertyChangeListener(this);
        }
        namedErrSensor = sen;
        if (namedErrSensor != null) {
            if (_iconMap==null) {
                _iconMap = new Hashtable<String, NamedIcon>();
            }
            getErrSensor().addPropertyChangeListener(this);
        }
    }
    
    public Sensor getErrSensor() { 
        if (namedErrSensor==null) {
            return null;
        }
        return namedErrSensor.getBean(); 
    }    
    public NamedBeanHandle <Sensor> getNamedErrSensor() { return namedErrSensor; }

    public void setShowTrain(boolean set) {
        _showTrain = set;
    }
    public boolean showTrain() {
        return _showTrain;
    }

    /**
    * Place icon by its bean state name
    */
    public void setIcon(String name, NamedIcon icon) {
        if (log.isDebugEnabled()) log.debug("set \""+name+"\" icon= "+icon);
        _iconMap.put(name, icon);
        setIcon(_iconMap.get("ClearTrack"));
    }

    public NamedIcon getIcon(String name) {
        if (log.isDebugEnabled()) log.debug("get \""+name+"\" icon");
        return _iconMap.get(name);
    }

    public String getStatus() {
        return _status;
    }


    public int maxHeight() {
        int max = 0;
        if (_iconMap!=null) {
            Iterator<NamedIcon> iter = _iconMap.values().iterator();
            while (iter.hasNext()) {
                max = Math.max(iter.next().getIconHeight(), max);
            }
        }
        return max;
    }
    public int maxWidth() {
        int max = 0;
        if (_iconMap!=null) {
            Iterator<NamedIcon> iter = _iconMap.values().iterator();
            while (iter.hasNext()) {
                max = Math.max(iter.next().getIconWidth(), max);
            }
        }
        return max;
    }

    public void propertyChange(java.beans.PropertyChangeEvent evt) {
		if (log.isDebugEnabled())
			log.debug("property change: " + getNameString() + " property " + evt.getPropertyName() + " is now "
					+ evt.getNewValue()+" from "+evt.getSource().getClass().getName());

        if (namedOccBlock!=null && evt.getSource() instanceof OBlock) {
            if ("state".equals(evt.getPropertyName())) {
                int now = ((Integer)evt.getNewValue()).intValue();
                if ((now & OBlock.OUT_OF_SERVICE)!=0) {
                    _status = "DontUseTrack";
                } else if ((now & OBlock.UNOCCUPIED)!=0) {
                    _status = "ClearTrack";
                }
                if ((now & OBlock.OCCUPIED)!=0) {
                    if ((now & OBlock.RUNNING)!=0) {
                        _status = "PositionTrack";
                        OBlock block = (OBlock)evt.getSource();
                        _train = (String)block.getValue();
                    } else {
                        _status = "OccupiedTrack";
                    }
                } else if ((now & OBlock.ALLOCATED)!=0) {
                    _status = "AllocatedTrack";
                }
            }
        } else if (evt.getPropertyName().equals("KnownState") && evt.getSource() instanceof Sensor) {
                int now = ((Integer)evt.getNewValue()).intValue();
                if (namedOccSensor!=null) {
                    if (now==Sensor.ACTIVE) {
                        _status = "OccupiedTrack";
                    } else if (now==Sensor.INACTIVE) {
                        _status = "ClearTrack";
                    } else if (now==Sensor.UNKNOWN) {
                        _status = "DontUseTrack";
                    } else {
                        _status = "ErrorTrack";
                    }
                }
                if (evt.getSource().equals(getErrSensor())) {
                    if (now==Sensor.ACTIVE) {
                        _status = "ErrorTrack";
                    } else {
                        _status = "DontUseTrack";
                    }
                }
        }
        displayState(_status);
	}

    public String getNameString() {
        return "ITrack";
    }

    /**
     * Pop-up displays unique attributes
     */
    public boolean showPopUp(JPopupMenu popup) {
        return false;
	}

    /******** popup AbstractAction.actionPerformed method overrides *********/

    protected void rotateOrthogonal() {
        Iterator<NamedIcon> it = _iconMap.values().iterator();
        while (it.hasNext()) {
            NamedIcon icon = it.next();
            icon.setRotation(icon.getRotation()+1, this);
        }
        displayState(_status);
    }

    public void setScale(double s) {
        Iterator<NamedIcon> it = _iconMap.values().iterator();
        while (it.hasNext()) {
            it.next().scale(s, this);
        }
        displayState(_status);
    }

    public void rotate(int deg) {
        Iterator<NamedIcon> it = _iconMap.values().iterator();
        while (it.hasNext()) {
            it.next().rotate(deg, this);
        }
        displayState(_status);
    }

    /**
	 * Drive the current state of the display from the state of the turnout.
	 */
    void displayState(String status) {
        log.debug(getNameString() +" displayStatus "+_status);
        if (_showTrain && "PositionTrack".equals(_status)) {
            super.setText(_train);
        } else {
            super.setText("");
        }
        NamedIcon icon = getIcon(status);
        if (icon!=null) {
            super.setIcon(icon);
        }
        updateSize();
    }

    public boolean setEditItemMenu(JPopupMenu popup) {
        String txt = java.text.MessageFormat.format(rb.getString("EditItem"), rb.getString("IndicatorTrack"));
        popup.add(new javax.swing.AbstractAction(txt) {
                public void actionPerformed(ActionEvent e) {
                    editItem();
                }
            });
        return true;
    }

    IndicatorItemPanel _trackPanel;
    protected void editItem() {
        makePalettteFrame(java.text.MessageFormat.format(rb.getString("EditItem"), rb.getString("IndicatorTO")));
        _trackPanel = new IndicatorItemPanel(_paletteFrame, "IndicatorTrack", _editor);
        _trackPanel.init( new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                updateItem();
            }
        });
        if (namedErrSensor!=null) {
            _trackPanel.setErrSensor(namedErrSensor.getName());
        }
        if (namedOccSensor!=null) {
            _trackPanel.setOccDetector(namedOccSensor.getName());
        }
        if (namedOccBlock!=null) {
            _trackPanel.setOccDetector(namedOccBlock.getName());
        }
        _trackPanel.setShowTrainName(_showTrain);
        _paletteFrame.add(_trackPanel);
        _paletteFrame.setLocationRelativeTo(this);
        _paletteFrame.toFront();
        _paletteFrame.pack();
        _paletteFrame.setVisible(true);
    }

    void updateItem() {
        setErrSensor(_trackPanel.getErrSensor());
        setOccSensor(_trackPanel.getOccSensor());
        setOccBlock(_trackPanel.getOccBlock());
        _showTrain = _trackPanel.getShowTrainName();
        Hashtable<String, NamedIcon> oldMap = cloneMap(_iconMap, null);
        Hashtable<String, NamedIcon> iconMap = _trackPanel.getIconMap();

        Iterator<Entry<String, NamedIcon>> it = iconMap.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, NamedIcon> entry = it.next();
            if (log.isDebugEnabled()) log.debug("key= "+entry.getKey());
            NamedIcon newIcon = entry.getValue();
            NamedIcon oldIcon = oldMap.get(entry.getKey());
            newIcon.setLoad(oldIcon.getDegrees(), oldIcon.getScale(), this);
            newIcon.setRotation(oldIcon.getRotation(), this);
            setIcon(entry.getKey(), newIcon);
        }
        _paletteFrame.dispose();
        _paletteFrame = null;
        _trackPanel = null;
        invalidate();
    }

    public void dispose() {
        if (namedOccSensor != null) {
            getOccSensor().removePropertyChangeListener(this);
        }
        namedOccSensor = null;

        if (namedOccBlock != null) {
            getOccBlock().removePropertyChangeListener(this);
        }
        namedOccBlock = null;

        if (namedErrSensor != null) {
            getErrSensor().removePropertyChangeListener(this);
        }
        namedErrSensor = null;
        _iconMap = null;
        super.dispose();
    }

    protected Hashtable<String, NamedIcon> cloneMap(Hashtable<String, NamedIcon> map,
                                                     IndicatorTrackIcon pos) {
        Hashtable<String, NamedIcon> clone = new Hashtable<String, NamedIcon>();
        if (map!=null) {
            Iterator<Entry<String, NamedIcon>> it = map.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, NamedIcon> entry = it.next();
                clone.put(entry.getKey(), cloneIcon(entry.getValue(), pos));
                if (pos!=null) {
                    pos.setIcon(entry.getKey(), _iconMap.get(entry.getKey()));
                }
            }
        }
        return clone;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(IndicatorTrackIcon.class.getName());
}

