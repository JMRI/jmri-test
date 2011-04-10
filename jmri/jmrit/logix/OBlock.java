package jmri.jmrit.logix;

import java.util.ResourceBundle;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import jmri.Path;
import jmri.Sensor;

//import jmri.jmrit.display.controlPanelEditor.CircuitBuilder;

/**
 * OBlock extends jmri.Block to be used in Logix Conditionals and Warrants. It is the smallest
 * piece of track that can have occupancy detection.  A better name would be Detection Circuit.
 * However, an OBlock can be defined without an occupancy sensor and used to calculate routes.  
 *
 * Additional states are defined to indicate status of the track and trains to control panels.
 *
 *<P>
 * Entrances (exits when train moves in opposute direction) to OBlocks have Portals. A
 * Portal object is a pair of OBlocks.  Each OBlock has a list of its Portals.
 *
 *<P>
 * When an OBlock (Detection Circuit) has a Portal whose entrance to the OBlock has a signal,
 * then the OBlock and its chains of adjacent OBlocks up to the next OBlock having an entrance
 * Portal with a signal, can be considered a "Block" in the sense of a prototypical railroad.
 * Preferrably all entrances to the "Block" should have entrance Portals with a signal. 
 *
 *
 *<P>
 * A Portal has a list of paths (OPath objects) for each OBlock it separates.  The paths are
 * determined by the turnout settings of the turnouts contained in the block. 
 * Paths are contained within the Block boundaries. Names of OPath objects only need be unique
 * within an OBlock.
 *
 *<P>
 *
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 * <P>
 *
 * @version $Revision: 1.30 $
 * @author	Pete Cressman (C) 2009
 */
public class OBlock extends jmri.Block implements java.beans.PropertyChangeListener {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.logix.WarrantBundle");

    /*
    * Block states.
    * NamedBean.UNKNOWN                 = 0x01;
    * Block.OCCUPIED =  Sensor.ACTIVE =   0x02;
    * Block.UNOCCUPIED = Sensor.INACTIVE= 0x04;
    * NamedBean.INCONSISTENT            = 0x08;
    * Add the following to the 4 sensor states.
    * States are OR'ed to show combination.  e.g. ALLOCATED | OCCUPIED = allocated block is occupied by rouge
    */
    static final public int ALLOCATED = 0x10;   // reserve the block for subsequent use by a train
    static final public int RUNNING = 0x20;     // Block that running train has reached 
    static final public int OUT_OF_SERVICE = 0x40;     // Block that running train has reached 
    static final public int DARK = 0x01;        // Block has no Sensor, same as UNKNOWN
    static final public int TRACK_ERROR = 0x80; // Block has Error

    static final Hashtable<String, Integer> _oldstatusMap = new Hashtable<String, Integer>();
    static final Hashtable<String, Integer> _statusMap = new Hashtable<String, Integer>();
    static final Hashtable<String, String> _statusNameMap = new Hashtable<String, String>();
    {
        _oldstatusMap.put(rb.getString("unoccupied"), Integer.valueOf(UNOCCUPIED));
        _oldstatusMap.put(rb.getString("occupied"), Integer.valueOf(OCCUPIED));
        _oldstatusMap.put(rb.getString("allocated"), Integer.valueOf(ALLOCATED));
        _oldstatusMap.put(rb.getString("running"), Integer.valueOf(RUNNING));
        _oldstatusMap.put(rb.getString("outOfService"), Integer.valueOf(OUT_OF_SERVICE));
        _oldstatusMap.put(rb.getString("dark"), Integer.valueOf(DARK));
        _oldstatusMap.put(rb.getString("powerError"), Integer.valueOf(TRACK_ERROR));

        _statusMap.put("unoccupied", Integer.valueOf(UNOCCUPIED));
        _statusMap.put("occupied", Integer.valueOf(OCCUPIED));
        _statusMap.put("allocated", Integer.valueOf(ALLOCATED));
        _statusMap.put("running", Integer.valueOf(RUNNING));
        _statusMap.put("outOfService", Integer.valueOf(OUT_OF_SERVICE));
        _statusMap.put("dark", Integer.valueOf(DARK));
        _statusMap.put("powerError", Integer.valueOf(TRACK_ERROR));

        _statusNameMap.put(rb.getString("unoccupied"), "unoccupied");
        _statusNameMap.put(rb.getString("occupied"), "occupied");
        _statusNameMap.put(rb.getString("allocated"), "allocated");
        _statusNameMap.put(rb.getString("running"), "running");
        _statusNameMap.put(rb.getString("outOfService"), "outOfService");
        _statusNameMap.put(rb.getString("dark"), "dark");
        _statusNameMap.put(rb.getString("powerError"), "powerError");
    }

    public static Enumeration<String> getLocalStatusNames() {
        Enumeration<String> keys = _statusNameMap.keys();
        return keys;
    }

    public static String getLocalStatusName(String str) {
        try {
            return rb.getString(str);
        } catch (java.util.MissingResourceException mre) {
            return str;
        }
    }

    public static String getSystemStatusName(String str) {
        return _statusNameMap.get(str);
    }

    ArrayList<Portal> _portals = new ArrayList <Portal>();     // portals to this block

    private Warrant _warrant;       // when not null, block is allocated to this warrant
    private String  _pathName;      // when not null, this is the allocated path
    private float _scaleRatio   = 87.1f;
    private boolean _metric     = false; // desired display mode
    private Sensor _errSensor;      // optional sensor to indicate shorts or other power problems
    
    public OBlock(String systemName) {
        super(systemName);
        setState(DARK);
    }

    public OBlock(String systemName, String userName) {
        super(systemName, userName);
        setState(DARK);
    }
    // override to determine if not DARK
    public void setSensor(Sensor sensor) {
        super.setSensor(sensor);
        if (sensor!=null) {
            setState(getState() & ~DARK);
        }
        if (log.isDebugEnabled()) log.debug("setSensor block \""+getDisplayName()+"\" state= "+getState());
    }

    public void setErrorSensor(Sensor sensor) {
        if (_errSensor != null) {
            _errSensor.removePropertyChangeListener(this);
        }
        _errSensor = sensor;
        if (_errSensor != null) {
            _errSensor.addPropertyChangeListener(this);
        }
    }
    public Sensor getErrorSensor() {
        return _errSensor;
    }

    public void propertyChange(java.beans.PropertyChangeEvent evt) {
		if (log.isDebugEnabled())
			log.debug("property change: " + getDisplayName() + " property " + evt.getPropertyName() + " is now "
					+ evt.getNewValue()+" from "+evt.getSource().getClass().getName());
        if (evt.getSource().equals(_errSensor)) {
            if (evt.getPropertyName().equals("KnownState")) {
                int errState = ((Integer)evt.getNewValue()).intValue();
                int oldState = getState();
                if (errState==Sensor.ACTIVE) {
                    setState(oldState | TRACK_ERROR);
                } else {
                    setState(oldState & ~TRACK_ERROR);
                }
                firePropertyChange("path", Integer.valueOf(oldState), Integer.valueOf(getState()));
            }
        }
    }

    protected Warrant getWarrant() { return _warrant; }

    public String getAllocatedPathName() { return _pathName; }

    public float getLengthScaleFeet() {
        return getLengthIn()/12*_scaleRatio;
    }
    public float getLengthMeters() {
        return getLengthMm()/1000*_scaleRatio;
    }

    public void setMetricUnits(boolean type) {
        _metric = type;
    }
    public boolean isMetric() {
        return _metric;
    }

    public void setScaleRatio(float sr) {
        _scaleRatio = sr;
    }
    public float getScaleRatio() {
        return _scaleRatio;
    }

    /*
    *  From the universal name for block status, check if it is the current status
    */
    public boolean statusIs(String statusName) {
        Integer i = _statusMap.get(statusName);
        if (i==null) {
            i = _oldstatusMap.get(statusName);
        }
        if (i!=null) {
            return ((getState() & i.intValue()) != 0);
        }
        log.error("\""+statusName+
                    "\" resource not found.  Please update Conditional State Variable testing OBlock \""
                    +getDisplayName()+"\" status.");
        return false;
    }

    /**
    *  Test that block is not occupied and and not allocated 
    */
    public boolean isFree() {
        int state = getState();
        return ((state & ALLOCATED)==0 && (state & OCCUPIED)==0);
    }

    /**
    * Allocate (reserves) the block for the Warrant that is the 'value' object
    * Note the block may be OCCUPIED by a non-warranted train, but the allocation is permitted.
    * @param warrant
    * @return name of block if block is already allocated to another warrant
    */
    protected String allocate(Warrant warrant) {
        if (warrant==null) {
            return "ERROR! Allocate called with null warrant in block \""+getDisplayName()+"\"!";
        }
        if (_warrant!=null) {
            if (_warrant.equals(warrant)) {
                return null;
            } else {
                // allocated to another warrant
                return java.text.MessageFormat.format(rb.getString("AllocatedToWarrant"),
                                                      _warrant.getDisplayName(), getDisplayName()); 
            }
        }
        String path = warrant.getRoutePathInBlock(this);
        if (_pathName!=null && !_pathName.equals(path)) {
            return java.text.MessageFormat.format(rb.getString("AllocatedToPath"),
                                          path, getDisplayName(), _pathName); 
        }
        _warrant = warrant;
        _pathName = path;
        // firePropertyChange signaled in super.setState()
        setState(getState() | ALLOCATED);
        if (log.isDebugEnabled()) log.debug("Allocated OBlock path \""+_pathName+"\" in block \""
                                            +getSystemName()+"\", state= "+getState());
        return null;
    }

    /**
    * Allocate (reserves) the block for the Warrant that is the 'value' object
    * Note the block may be OCCUPIED by a non-warranted train, but the allocation is permitted.
    * @return name of block if block is already allocated to another warrant
    */
    public String allocate(String pathName) {
        if (pathName==null) {
            log.error("allocate called with null pathName in block \""+getDisplayName()+"\"!");
            return null;
        } else if (_warrant!=null) {
            // allocated to another warrant
            return java.text.MessageFormat.format(rb.getString("AllocatedToWarrant"),
                                                  _warrant.getDisplayName(), getDisplayName()); 
        }
        if (_pathName!=null && !_pathName.equals(pathName)) {
            return java.text.MessageFormat.format(rb.getString("AllocatedToPath"),
                                          pathName, getDisplayName(), _pathName); 
        }
        _pathName = pathName;
        // firePropertyChange signaled in super.setState()
        setState(getState() | ALLOCATED);
        if (log.isDebugEnabled()) log.debug("Allocated OBlock path \""+pathName+"\" in block \""
                                            +getSystemName()+"\", state= "+getState());
        return null;
    }

    /**
    * Remove allocation state
    * Remove listener regardless of ownership
    */
    public String deAllocate(Warrant warrant) {
        if (log.isDebugEnabled()) log.debug("deAllocate \""+_pathName+"\" in block \""
                                            +getSystemName());
        String msg = null;
        if (_warrant!=null) {
            if (!_warrant.equals(warrant)) {
                msg = "cannot deAllocate. warrant \""+_warrant.getDisplayName()+"\" owns block \""+getDisplayName()+"\"!";
            } else {
                removePropertyChangeListener(warrant);
            }
        } else if (warrant!=null && _pathName!=null) {
            msg = "cannot deAllocate. path \""+_pathName+"\" is allocated in block \""+getDisplayName()+"\"!";
        }
        if (msg!=null) {
            log.warn(msg);
            if (warrant==null) {
                msg = null;
            }
        } else {
            _warrant = null;
            _pathName = null;
            //setValue(null);
            int state = getState();
            setState(state & ~(ALLOCATED | RUNNING));  // unset allocated and running bits
        }
        return msg;
    }

    public void setOutOfService(boolean set) {
        if (set) {
            setState(getState() | OUT_OF_SERVICE);  // set OoS bit
        } else {
            setState(getState() & ~OUT_OF_SERVICE);  // unset OoS bit
        }
    }

    /**
    * Enforce unique portal names.  Portals are not managed beans and multiple
    * config loads will create multiple Portal Objects that are "duplicates".
    */
    public void addPortal(Portal portal) {
        String name = getDisplayName();
        if (!name.equals(portal.getFromBlockName()) && !name.equals(portal.getToBlockName())) {
            log.warn(portal.toString()+" not in block "+getDisplayName());
            return;
        }
        String pName = portal.getName();
        if (pName!=null) {  // pName may be null if called from Portal ctor
            for (int i=0; i<_portals.size(); i++) {
                if (pName.equals(_portals.get(i).getName())) {
                    return;
                }
            }
        }
        int oldSize = _portals.size(); 
        _portals.add(portal);
        if (log.isDebugEnabled()) log.debug("addPortal: portal= \""+portal.getName()+
                                            "\", to Block= \""+getDisplayName()+"\"." ); 
        firePropertyChange("portalCount", Integer.valueOf(oldSize), Integer.valueOf(_portals.size()));
    }

    /**
    * Remove portal from block and stub all paths using this portal to
    * be dead end spurs.
    */
    public void removePortal(Portal portal) {
        int oldSize = _portals.size();
        int oldPathSize = getPaths().size();
        if (portal != null){
            //String name = portal.getName();
            Iterator <Path> iter = getPaths().iterator();
            while (iter.hasNext()) {
                OPath path = (OPath)iter.next();
                if (portal.equals(path.getFromPortal())) {
                    path.setFromPortal(null);
                    if (log.isDebugEnabled()) log.debug("removed Portal "+portal.getName()+" from Path "+
                              path.getName()+" in block "+getSystemName());
                }
                if (portal.equals(path.getToPortal())) {
                    path.setToPortal(null);
                    if (log.isDebugEnabled()) log.debug("removed Portal "+portal.getName()+" from Path "+
                              path.getName()+" in block "+getSystemName());
                }
                if (path.getFromPortal()==null && path.getToPortal()==null) {
                    removePath(path);
                    if (log.isDebugEnabled()) log.debug("removed Path "+
                                              path.getName()+" in block "+getSystemName());
                }
            }
            //_portals.remove(portal);
            for (int i=0; i < _portals.size(); i++) {
                if (portal.equals(_portals.get(i))) {
                    _portals.remove(i);
                    log.debug("removed portal "+portal.getName()+" from block "+getSystemName());
                    i--;
                }
            }
        }
        log.debug("removePortal: block "+getSystemName()+" portals decreased from "
                  +oldSize+" to "+_portals.size()+" - paths decreased from "+
                  oldPathSize+" to "+getPaths().size());
        firePropertyChange("portalCount", Integer.valueOf(oldSize), Integer.valueOf(_portals.size()));
    }

    public Portal getPortalByName(String name) {
        //if (log.isDebugEnabled()) log.debug("getPortalByName: name= \""+name+"\"." ); 
        for (int i=0; i<_portals.size(); i++)  {
            if (_portals.get(i).getName().equals(name)) {
                return _portals.get(i);
            }
        }
        return null;
    }

    public List <Portal> getPortals() {
        return _portals;
    }

    public OPath getPathByName(String name) {
        Iterator <Path> iter = getPaths().iterator();
        while (iter.hasNext()) {
            OPath path = (OPath)iter.next();
            if (path.getName().equals(name)) {
                return path;
            }
        }
        return null;
    }

    /**
    * Enforce unique path names within block
    */
    public boolean addPath(OPath path) {
        String pName = path.getName();
        if (log.isDebugEnabled()) log.debug("addPath \""+pName+"\" to OBlock "+getSystemName());
        List <Path> list = getPaths();
        for (int i=0; i<list.size(); i++) {
            if (pName.equals(((OPath)list.get(i)).getName())) {
                if (log.isDebugEnabled()) log.debug("\""+pName+"\" duplicated in OBlock "+getSystemName());
                return false;
            }
        }
        path.setBlock(this);
        Portal portal = path.getFromPortal();
        if (portal!=null) {
            if (!portal.addPath(path)) { 
                if (log.isDebugEnabled()) log.debug("\""+pName+"\" rejected by portal  "+portal.getName());
                return false; 
            }
        }
        portal = path.getToPortal();
        if (portal!=null) {
            if (!portal.addPath(path)) { 
                if (log.isDebugEnabled()) log.debug("\""+pName+"\" rejected by portal  "+portal.getName());
                return false;
            }
        }
        int oldSize = list.size();
        super.addPath(path);
        firePropertyChange("pathCount", Integer.valueOf(oldSize), Integer.valueOf(getPaths().size()));
        return true;
    }

    public void removePath(Path path) {
        if (!getSystemName().equals(path.getBlock().getSystemName())) {
            return;
        }
//        if (log.isDebugEnabled()) log.debug("Path "+((OPath)path).getName()+" removed from "+getSystemName());
        path.clearSettings();
        int oldSize = getPaths().size();
        super.removePath(path);
        firePropertyChange("pathCount", Integer.valueOf(oldSize), Integer.valueOf(getPaths().size()));
    }

    /**
    * Set Turnouts for the path
    * Called by warrants to set turnouts for a train it is able to run.  The warrant parameter
    * is verifies that the block is indeed allocated to the warrant,  If the block is unwarranted
    * then the block is allocated to the calling warrant.  A logix conditional may also call this 
    * method with a null warrant parameter for manual logix control.  However, if the block is 
    * under a warrant the call will be rejected.
    * @param pathName name of the path
    * @param warrant warrant the block is allocated to
    * @return error message if the call fails.  null if the call succeeds
    */
    public String setPath(String pathName, Warrant warrant) {
        String msg = null;
        if (_warrant!=null && !_warrant.equals(warrant)) {
            msg = java.text.MessageFormat.format(rb.getString("AllocatedToWarrant"),
                                                  _warrant.getDisplayName(), getDisplayName());
            log.error(msg);
            return msg; 
        }
        _warrant = warrant;
        OPath path = getPathByName(pathName);
        if (path==null) {
            msg = java.text.MessageFormat.format(rb.getString("PathNotFound"), pathName, getDisplayName()); 
            log.error(msg);
            return msg; 
        } else {
            if (_warrant!=null) {
                if (!pathName.equals(warrant.getRoutePathInBlock(this))) {
                    msg = java.text.MessageFormat.format(rb.getString("PathNotFound"), pathName, getDisplayName()); 
                    log.error(msg);
                    return msg; 
                }
            }
            _pathName = pathName;
            path.setTurnouts(0);
            firePropertyChange("path", Integer.valueOf(0), Integer.valueOf(getState()));
        }
        if (log.isDebugEnabled()) log.debug("setPath: Block \""+getSystemName()+" path set to \""+
                                            _pathName+"\"");
        return msg;
    }

    /**
    * Call for Circuit Builder to make icon color changes for its GUI
    */
    public void pseudoPropertyChange(String propName, Object old, Object n) {
        if (log.isDebugEnabled()) log.debug("pseudoPropertyChange: Block \""+getSystemName()+" property \""+
                                            propName+"\" new value= "+n.toString());
        firePropertyChange(propName, old, n);
    }

    /**
     * (Override)  Handles Block sensor going INACTIVE: this block is empty.
     * Called by handleSensorChange
     */
    public void goingInactive() {
//        setState((getState() & ~(OCCUPIED | RUNNING)) | UNOCCUPIED);
        if (log.isDebugEnabled()) log.debug("Allocated OBlock \""+getSystemName()+
                                            "\" goes UNOCCUPIED. from state= "+getState());
        if (_warrant!=null) {
            _warrant.goingInactive(this);
            setValue(null);
        }
        setState((getState() & ~(OCCUPIED | RUNNING)) | UNOCCUPIED);
    }

     /**
     * (Override) Handles Block sensor going ACTIVE: this block is now occupied, 
     * figure out from who and copy their value. Called by handleSensorChange
     */
	public void goingActive() {
        setState((getState() & ~UNOCCUPIED) | OCCUPIED);
//        if (log.isDebugEnabled()) log.debug("Allocated OBlock \""+getSystemName()+
//                                            "\" goes OCCUPIED. state= "+getState());
        if (_warrant!=null) {
            _warrant.goingActive(this);
        } else if (_pathName!=null) {
            // must be a manual path allocation.  unset iccupied bit and set manual path detection
            setState((getState() & ~UNOCCUPIED) | (OCCUPIED | RUNNING));
        }
        if (log.isDebugEnabled()) log.debug("Block \""+getSystemName()+" went active, path= "+
                                            _pathName+", state= "+getState());
    }

    public void dispose() {

        List <Portal> list = getPortals();
        for (int i=0; i<list.size(); i++) {
            Portal portal = list.get(i);
            OBlock opBlock = portal.getOpposingBlock(this);
            // remove portal and stub paths through portal in opposing block
            opBlock.removePortal(portal);
        }
        List <Path> pathList = getPaths();
        for (int i=0; i<pathList.size(); i++) {
            removePath(pathList.get(i));
        }
        jmri.InstanceManager.oBlockManagerInstance().deregister(this);
        super.dispose();
    }
   
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(OBlock.class.getName());
}
