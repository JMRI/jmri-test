package jmri.jmrit.display;

import jmri.InstanceManager;
import jmri.Sensor;
import jmri.Turnout;

import jmri.jmrit.picker.PickListModel;
import jmri.jmrit.display.palette.IndicatorTOItemPanel;

import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.logix.OBlock;
import jmri.NamedBeanHandle;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;
//import javax.swing.JPopupMenu;

/**
 * An icon to display a status and state of a color coded turnout.<P>
 * This responds to only KnownState, leaving CommandedState to some other
 * graphic representation later. 
 * <p>"state" is the state of the underlying turnout ("closed", "thrown", etc.)
 * <p>"status" is the operating condition of the track ("clear", "occupied", etc.)
 * <P>
 * A click on the icon will command a state change. Specifically, it
 * will set the CommandedState to the opposite (THROWN vs CLOSED) of
 * the current KnownState. This will display the setting of the turnout points.
 *<P>
 * The status is indicated by color and changes are done only done by the occupancy
 * sensing - OBlock or other sensor.
 * <p>
 * The default icons are for a left-handed turnout, facing point
 * for east-bound traffic.
 * @author Bob Jacobsen  Copyright (c) 2002
 * @version $Revision$
 */

public class IndicatorTurnoutIcon extends TurnoutIcon implements IndicatorTrack {

    Hashtable<String, Hashtable<Integer, NamedIcon>> _iconMaps;
    ArrayList <String> _paths;      // list of paths that include this icon


    private NamedBeanHandle<Sensor> namedOccSensor = null;
    private NamedBeanHandle<OBlock> namedOccBlock = null;

    private String _status;
    private boolean _showTrain; // this track should display _loco when occupied
    private LocoIcon _loco = null;

    public IndicatorTurnoutIcon(Editor editor) {
        super(editor);
        log.debug("IndicatorTurnoutIcon ctor: isIcon()= "+isIcon()+", isText()= "+isText());
        _status = "DontUseTrack";
        _iconMaps = initMaps();

    }

    Hashtable<String, Hashtable<Integer, NamedIcon>> initMaps() {
        Hashtable<String, Hashtable<Integer, NamedIcon>> iconMaps = new Hashtable<String, Hashtable<Integer, NamedIcon>>();
        iconMaps.put("ClearTrack", new Hashtable <Integer, NamedIcon>());
        iconMaps.put("OccupiedTrack", new Hashtable <Integer, NamedIcon>());
        iconMaps.put("PositionTrack", new Hashtable <Integer, NamedIcon>());
        iconMaps.put("AllocatedTrack", new Hashtable <Integer, NamedIcon>());
        iconMaps.put("DontUseTrack", new Hashtable <Integer, NamedIcon>());
        iconMaps.put("ErrorTrack", new Hashtable <Integer, NamedIcon>());
        return iconMaps;
    }

    Hashtable<String, Hashtable<Integer, NamedIcon>> cloneMaps(IndicatorTurnoutIcon pos) {
        Hashtable<String, Hashtable<Integer, NamedIcon>> iconMaps = initMaps();
        Iterator<Entry<String, Hashtable<Integer, NamedIcon>>> it = _iconMaps.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, Hashtable<Integer, NamedIcon>> entry = it.next();
            Hashtable <Integer, NamedIcon> clone = iconMaps.get(entry.getKey());
            Iterator<Entry<Integer, NamedIcon>> iter = entry.getValue().entrySet().iterator();
            while (iter.hasNext()) {
                Entry<Integer, NamedIcon> ent = iter.next();
//                if (log.isDebugEnabled()) log.debug("key= "+ent.getKey());
                clone.put(ent.getKey(), cloneIcon(ent.getValue(), pos));
            }
        }
        return iconMaps;
    }

    public Positionable deepClone() {
        IndicatorTurnoutIcon pos = new IndicatorTurnoutIcon(_editor);
        return finishClone(pos);
    }

    public Positionable finishClone(Positionable p) {
        IndicatorTurnoutIcon pos = (IndicatorTurnoutIcon)p;
        pos.setOccBlockHandle(namedOccBlock);
        pos.setOccSensorHandle(namedOccSensor);
        pos._iconMaps = cloneMaps(pos);
        if (_paths!=null) {
            pos._paths = new ArrayList<String>();
            for (int i=0; i<_paths.size(); i++) {
                pos._paths.add(_paths.get(i));
            }
        }
        pos._iconFamily = _iconFamily;
        pos._showTrain = _showTrain;
        return super.finishClone(pos);
    }
    
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="EI_EXPOSE_REP", 
            justification="OK until Java 1.6 allows more efficient return of copy") 
    public Hashtable<String, Hashtable<Integer, NamedIcon>> getIconMaps() {
        return _iconMaps;
    }

    /**
     * Attached a named sensor to display status from OBlocks
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
                 setOccSensorHandle(jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(pName, sensor));                
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
            Sensor sensor = getOccSensor();
            sensor.addPropertyChangeListener(this, namedOccSensor.getName(), "Indicator Turnout Icon");
            setStatus(sensor.getKnownState());
            if (_iconMaps!=null) {
                displayState(turnoutState());
            }
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
    public void setOccBlockHandle(NamedBeanHandle<OBlock> blockHandle) {
        if (namedOccBlock != null) {
            getOccBlock().removePropertyChangeListener(this);
        }
        namedOccBlock = blockHandle;
        if (namedOccBlock != null) {
            OBlock block = getOccBlock();
            block.addPropertyChangeListener(this, namedOccBlock.getName(), "Indicator Turnout Icon");
            setStatus(block, block.getState());
            if (_iconMaps!=null) {
                displayState(turnoutState());
            }
            setTooltip(new ToolTip(block.getDescription(), 0, 0));
        } 
    }
    public OBlock getOccBlock() { 
        if (namedOccBlock==null) {
            return null;
        }
        return namedOccBlock.getBean(); 
    }    
    public NamedBeanHandle <OBlock> getNamedOccBlock() { return namedOccBlock; }

    public void setShowTrain(boolean set) {
        _showTrain = set;
    }
    public boolean showTrain() {
        return _showTrain;
    }
    
    public Iterator<String> getPaths() {
        if (_paths==null) {
            return null;
        }
        return _paths.iterator();
    }
    public void setPaths(ArrayList<String>paths) {
        _paths = paths;
    }

    public void addPath(String path) {
        if (_paths==null) {
            _paths = new ArrayList<String>();
        }
        _paths.add(path);
    }
    public void removePath(String path) {
        if (_paths!=null) {
            _paths.remove(path);
        }
    }

    /**
    * Place icon by its localized bean state name
    * @param status - the track condition of the icon
    * @param stateName - NamedBean name of turnout state
    * @param icon - icon corresponding to status and state
    */
    public void setIcon(String status, String stateName, NamedIcon icon) {
        if (log.isDebugEnabled()) log.debug("setIcon for status \""+status+"\", stateName= \""
                                +stateName+" icom= "+icon.getURL());
//                                            ") state= "+_name2stateMap.get(stateName)+
//                                            " icon: w= "+icon.getIconWidth()+" h= "+icon.getIconHeight());
        if (_iconMaps==null) {
            initMaps();
        }
        _iconMaps.get(status).put(_name2stateMap.get(stateName), icon);
        setIcon(_iconMaps.get("ClearTrack").get(_name2stateMap.get("BeanStateInconsistent")));
    }

    /**
    * Get clear icon by its localized bean state name
    */
    public NamedIcon getIcon(String status, int state) {
        log.debug("getIcon: status= "+status+", state= "+state);
        Hashtable<Integer, NamedIcon> map = _iconMaps.get(status);
        if (map==null) { return null; }
        return map.get(Integer.valueOf(state));
    }

    public String getStateName(Integer state) {
        return _state2nameMap.get(state);
    }

    public String getStatus() {
        return _status;
    }

    public int maxHeight() {
        int max = 0;
        if (_iconMaps!=null) {
            Iterator<Hashtable<Integer, NamedIcon>> it = _iconMaps.values().iterator();
            while (it.hasNext()) {
                Iterator<NamedIcon> iter = it.next().values().iterator();
                while (iter.hasNext()) {
                    max = Math.max(iter.next().getIconHeight(), max);
                }
            }
        }
        return max;
    }
    public int maxWidth() {
        int max = 0;
        if (_iconMaps!=null) {
            Iterator<Hashtable<Integer, NamedIcon>> it = _iconMaps.values().iterator();
            while (it.hasNext()) {
                Iterator<NamedIcon> iter = it.next().values().iterator();
                while (iter.hasNext()) {
                    max = Math.max(iter.next().getIconWidth(), max);
                }
            }
        }
        return max;
    }

    /******** popup AbstractAction.actionPerformed method overrides *********/

    protected void rotateOrthogonal() {
        if (_iconMaps!=null) {
            Iterator<Hashtable<Integer, NamedIcon>> it = _iconMaps.values().iterator();
            while (it.hasNext()) {
                Iterator<NamedIcon> iter = it.next().values().iterator();
                while (iter.hasNext()) {
                    NamedIcon icon = iter.next();
                    icon.setRotation(icon.getRotation()+1, this);
                }
            }
        }
        displayState(turnoutState());
    }

    public void setScale(double s) {
        if (_iconMaps!=null) {
            Iterator<Hashtable<Integer, NamedIcon>> it = _iconMaps.values().iterator();
            while (it.hasNext()) {
                Iterator<NamedIcon> iter = it.next().values().iterator();
                while (iter.hasNext()) {
                    iter.next().scale(s, this);
                }
            }
        }
        displayState(turnoutState());
    }

    public void rotate(int deg) {
        if (_iconMaps!=null) {
            Iterator<Hashtable<Integer, NamedIcon>> it = _iconMaps.values().iterator();
            while (it.hasNext()) {
                Iterator<NamedIcon> iter = it.next().values().iterator();
                while (iter.hasNext()) {
                    iter.next().rotate(deg, this);
                }
            }
        }
        displayState(turnoutState());
    }

    /**
	 * Drive the current state of the display from the state of the turnout and status of track.
	 */
    void displayState(int state) {
        if (_loco!=null) {
            _loco.remove();
        }
        if (getNamedTurnout() == null) {
            log.debug("Display state "+state+", disconnected");
        } else {
            if (_status!=null && _iconMaps!=null) {
                NamedIcon icon = getIcon(_status, state);
                if (icon!=null) {
                    super.setIcon(icon);
                }
            }
        }
        super.displayState(state);
        updateSize();
    }

    public String getNameString() {
        String str = "";
        if (namedOccBlock!=null) {
            str = " in "+namedOccBlock.getBean().getDisplayName();
        } else if (namedOccSensor!=null) {
            str = " on "+namedOccSensor.getBean().getDisplayName();
        }
        return "ITrack "+super.getNameString()+str;
    }

    // update icon as state of turnout changes and status of track changes
    // Override
    public void propertyChange(java.beans.PropertyChangeEvent evt) {
		if (log.isDebugEnabled())
			log.debug("property change: "+getNameString()+" property \""+evt.getPropertyName()+"\"= "
					+evt.getNewValue()+" from "+evt.getSource().getClass().getName());

        Object source = evt.getSource();
        if (source instanceof Turnout) {
            super.propertyChange(evt);
        } else if (source instanceof OBlock) {
            if ("state".equals(evt.getPropertyName()) || "path".equals(evt.getPropertyName())) {
                int now = ((Integer)evt.getNewValue()).intValue();
                setStatus((OBlock)source, now);
            }
        } else if (source instanceof Sensor) {
            if (evt.getPropertyName().equals("KnownState")) {
                int now = ((Integer)evt.getNewValue()).intValue();
                if (source.equals(getOccSensor())) {
                    setStatus(now);
                }
            }
        }
        displayState(turnoutState());
	}

    private void setStatus(OBlock block, int state) {
        String pathName = block.getAllocatedPathName();
        if ((state & OBlock.TRACK_ERROR)!=0) {
            _status = "ErrorTrack";
        } else if ((state & OBlock.OUT_OF_SERVICE)!=0) {
            setControlling(false);
            /*
            if ((state & OBlock.OCCUPIED)!=0) {
                _status = "OccupiedTrack";
            } else {
                _status = "DontUseTrack";
            }
            */
            _status = "DontUseTrack";
        } else if ((state & OBlock.OCCUPIED)!=0) {
            setControlling(true);
            if (_showTrain) {
                setLocoIcon((String)block.getValue());
            }
            if ((state & OBlock.RUNNING)!=0) {
                if (_paths!=null && _paths.contains(pathName)) {
                    _status = "PositionTrack";
                } else {
                    _status = "ClearTrack";     // icon not on path
                }
            } else {
                _status = "OccupiedTrack";
            }
        } else {
            setControlling(true);
            if (_loco!=null) {
                _loco.remove();
                _loco = null;
            }
            if ((state & OBlock.ALLOCATED)!=0) {
                if (_paths!=null && _paths.contains(pathName)) {
                    _status = "AllocatedTrack";     // icon on path
                } else {
                    _status = "ClearTrack";
                }
            } else if ((state & Sensor.UNKNOWN)!=0) {
                _status = "DontUseTrack";
            } else {
                _status = "ClearTrack";
            }
        }
    }

    private void setStatus(int state) {
        if (state==Sensor.ACTIVE) {
            _status = "OccupiedTrack";
        } else if (state==Sensor.INACTIVE) {
            _status = "ClearTrack";
        } else if (state==Sensor.UNKNOWN) {
            _status = "DontUseTrack";
        } else {
            _status = "ErrorTrack";
        }
    }

    private void setLocoIcon(String trainName) {
        if (trainName==null) {
            if (_loco!=null) {
                _loco.remove();
                _loco = null;
            }
            return;
        }
        if (_loco!=null) {
            return;
        }
        trainName = trainName.trim();
        _loco = _editor.selectLoco(trainName);
        if (_loco==null) {
            _loco = _editor.addLocoIcon(trainName);
        }
        if (_loco!=null) {
            java.awt.Point pt = getLocation();
            pt.x = pt.x + (getWidth() - _loco.getWidth())/2;
            pt.y = pt.y + (getHeight() - _loco.getHeight())/2;
            _loco.setLocation(pt);
            log.debug("Display Loco \""+trainName+"\" ("+_status+") at ("+pt.x+", "+pt.y+")");
        }
    }
    
    IndicatorTOItemPanel _TOPanel;

    protected void editItem() {
        makePalettteFrame(java.text.MessageFormat.format(rb.getString("EditItem"), rb.getString("IndicatorTO")));
        _TOPanel = new IndicatorTOItemPanel(_paletteFrame, "IndicatorTO", _iconFamily,
                                       PickListModel.turnoutPickModelInstance(), _editor);
        ActionListener updateAction = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                updateItem();
            }
        };
        // Convert _iconMaps state (ints) to Palette's bean names
        Hashtable<String, Hashtable<String, NamedIcon>> iconMaps =
                     new Hashtable<String, Hashtable<String, NamedIcon>>();
        iconMaps.put("ClearTrack", new Hashtable <String, NamedIcon>());
        iconMaps.put("OccupiedTrack", new Hashtable <String, NamedIcon>());
        iconMaps.put("PositionTrack", new Hashtable <String, NamedIcon>());
        iconMaps.put("AllocatedTrack", new Hashtable <String, NamedIcon>());
        iconMaps.put("DontUseTrack", new Hashtable <String, NamedIcon>());
        iconMaps.put("ErrorTrack", new Hashtable <String, NamedIcon>());
        Iterator<Entry<String, Hashtable<Integer, NamedIcon>>> it = _iconMaps.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, Hashtable<Integer, NamedIcon>> entry = it.next();
            Hashtable <String, NamedIcon> clone = iconMaps.get(entry.getKey());
            Iterator<Entry<Integer, NamedIcon>> iter = entry.getValue().entrySet().iterator();
            while (iter.hasNext()) {
                Entry<Integer, NamedIcon> ent = iter.next();
                clone.put(_state2nameMap.get(ent.getKey()), cloneIcon(ent.getValue(), this));
            }
        }
        _TOPanel.initUpdate(updateAction, iconMaps);
        _TOPanel.setSelection(getTurnout());
        if (namedOccSensor!=null) {
            _TOPanel.setOccDetector(namedOccSensor.getName());
        }
        if (namedOccBlock!=null) {
            _TOPanel.setOccDetector(namedOccBlock.getName());
        }
        _TOPanel.setShowTrainName(_showTrain);
        _TOPanel.setPaths(_paths);
        _paletteFrame.add(_TOPanel);
        _paletteFrame.pack();
        _paletteFrame.setVisible(true);
    }

    void updateItem() {
		if (log.isDebugEnabled()) log.debug("updateItem: "+getNameString()+" family= "+_TOPanel.getFamilyName());
        setTurnout(_TOPanel.getTableSelection().getSystemName());
        setOccSensor(_TOPanel.getOccSensor());
        setOccBlock(_TOPanel.getOccBlock());
        _showTrain = _TOPanel.getShowTrainName();
        _iconFamily = _TOPanel.getFamilyName();
        _paths = _TOPanel.getPaths();
        Hashtable<String, Hashtable<String, NamedIcon>> iconMap = _TOPanel.getIconMaps();
        boolean scaleRotate = !_TOPanel.isUpdateWithSameMap();
        if (iconMap!=null) {
            Iterator<Entry<String, Hashtable<String, NamedIcon>>> it = iconMap.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, Hashtable<String, NamedIcon>> entry = it.next();
                String status = entry.getKey();
    //            Hashtable<Integer, NamedIcon> oldMap = cloneMap(_iconMaps.get(status), null);
                Hashtable <Integer, NamedIcon> oldMap = _iconMaps.get(entry.getKey());
                Iterator<Entry<String, NamedIcon>> iter = entry.getValue().entrySet().iterator();
                while (iter.hasNext()) {
                    Entry<String, NamedIcon> ent = iter.next();
                    if (log.isDebugEnabled()) log.debug("key= "+ent.getKey());
                    NamedIcon newIcon = cloneIcon(ent.getValue(), this);
                    NamedIcon oldIcon = oldMap.get(_name2stateMap.get(ent.getKey()));
                    if (scaleRotate) {
                        newIcon.setLoad(oldIcon.getDegrees(), oldIcon.getScale(), this);
                        newIcon.setRotation(oldIcon.getRotation(), this);
                    }
                    setIcon(status, ent.getKey(), newIcon);
                }
            }
        }   // otherwise retain current map
        _paletteFrame.dispose();
        _paletteFrame = null;
        _TOPanel.dispose();
        _TOPanel = null;
        displayState(turnoutState());
    }

    public void dispose() {
        if (namedOccSensor != null) {
            getOccSensor().removePropertyChangeListener(this);
        }
        namedOccSensor = null;
        namedOccSensor = null;
        super.dispose();
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(IndicatorTurnoutIcon.class.getName());
}
