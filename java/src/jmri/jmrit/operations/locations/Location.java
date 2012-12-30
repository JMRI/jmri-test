package jmri.jmrit.operations.locations;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JComboBox;

import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.rollingstock.engines.EngineTypes;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.PhysicalLocation;

import org.jdom.Attribute;
import org.jdom.Element;

/**
 * Represents a location on the layout
 * 
 * @author Daniel Boudreau Copyright (C) 2008, 2012
 * @version $Revision$
 */
public class Location implements java.beans.PropertyChangeListener {
	
	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.locations.JmritOperationsLocationsBundle");

	protected String _id = "";
	protected String _name = "";
	protected int _IdNumber = 0;
	protected int _numberRS = 0;
	protected int _pickupRS = 0;
	protected int _dropRS = 0;
	protected int _locationOps = NORMAL;			//type of operations at this location
	protected int _trainDir = EAST+WEST+NORTH+SOUTH; //train direction served by this location
	protected int _length = 0;						//length of all tracks at this location
	protected int _usedLength = 0;					//length of track filled by cars and engines 
	protected String _comment = "";
	protected String _switchListComment = "";		//optional switch list comment
	protected boolean _switchList = true;			//when true print switchlist for this location
	protected String _defaultPrinter = "";			//the default printer name when printing a switchlist
	protected String _status = UNKNOWN;				//print switch list status
	protected int _switchListState = SW_CREATE;		//switch list state, saved between sessions
	protected Point _trainIconEast = new Point();	//coordinates of east bound train icons
	protected Point _trainIconWest = new Point();
	protected Point _trainIconNorth = new Point();
	protected Point _trainIconSouth = new Point();
	protected Hashtable<String, Track> _trackHashTable = new Hashtable<String, Track>();
	protected PhysicalLocation _physicalLocation = new PhysicalLocation();
    protected List<String> _listTypes  = new ArrayList<String>();
	
	// Pool
	protected int _idPoolNumber = 0;
	protected Hashtable<String, Pool> _poolHashTable = new Hashtable<String, Pool>();
	
	public static final int NORMAL = 1;		// types of track allowed at this location
	public static final int STAGING = 2;	// staging only
	
	public static final int EAST = 1;		// train direction serviced by this location
	public static final int WEST = 2;
	public static final int NORTH = 4;
	public static final int SOUTH = 8;
	
	// Switch list status
	public static final String UNKNOWN = "";
	public static final String PRINTED = rb.getString("Printed");
	public static final String CSV_GENERATED = rb.getString("CsvGenerated");
	public static final String MODIFIED = rb.getString("Modified");
	public static final String UPDATED = rb.getString("Updated");
	
	// Switch list states
	public static final int SW_CREATE = 0;		// create new switch list
	public static final int SW_APPEND = 1;		// append train into to switch list
	public static final int SW_PRINTED = 2;		// switch list printed
	
	// For property change
	public static final String TRACK_LISTLENGTH_CHANGED_PROPERTY = "trackListLength";	// NOI18N
	public static final String TYPES_CHANGED_PROPERTY = "types";						// NOI18N
	public static final String TRAINDIRECTION_CHANGED_PROPERTY = "trainDirection";		// NOI18N
	public static final String LENGTH_CHANGED_PROPERTY = "length";						// NOI18N
	public static final String USEDLENGTH_CHANGED_PROPERTY = "usedLength";				// NOI18N
	public static final String NAME_CHANGED_PROPERTY = "name";							// NOI18N
	public static final String SWITCHLIST_CHANGED_PROPERTY = "switchList";				// NOI18N
	public static final String DISPOSE_CHANGED_PROPERTY = "dispose";					// NOI18N
	public static final String STATUS_CHANGED_PROPERTY = "locationStatus";				// NOI18N
	public static final String POOL_LENGTH_CHANGED_PROPERTY = "PoolLengthChanged";		// NOI18N
	public static final String SWITCHLIST_COMMENT_CHANGED_PROPERTY = "switchListComment";// NOI18N

	public Location(String id, String name) {
		log.debug("New location " + name + " " + id);
		_name = name;
		_id = id;
		// a new location accepts all types
		setTypeNames(CarTypes.instance().getNames());
		setTypeNames(EngineTypes.instance().getNames());
	}

	public String getId() {
		return _id;
	}

	public void setName(String name) {
		String old = _name;
		_name = name;
		if (!old.equals(name)){
			setDirtyAndFirePropertyChange(NAME_CHANGED_PROPERTY, old, name);
		}
	}
	
	// for combo boxes
	public String toString(){
		return _name;
	}

	public String getName() {
		return _name;
	}

	public PhysicalLocation getPhysicalLocation() {
		return(_physicalLocation);
	}

	public void setPhysicalLocation(PhysicalLocation l) {
		_physicalLocation = l;
	}

	/**
	 * Set total length of all tracks for this location
	 * @param length
	 */
	public void setLength(int length) {
		int old = _length;
		_length = length;
		if (old != length)
			setDirtyAndFirePropertyChange(LENGTH_CHANGED_PROPERTY, Integer.toString(old), Integer.toString(length));
	}

	/**
	 * 
	 * @return total length of all tracks for this location
	 */
	public int getLength() {
		return _length;
	}
	
	public void setUsedLength(int length) {
		int old = _usedLength;
		_usedLength = length;
		if (old != length)
			setDirtyAndFirePropertyChange(USEDLENGTH_CHANGED_PROPERTY, Integer.toString(old), Integer.toString(length));
	}
	
	/**
	 * 
	 * @return The length of the track that is occupied by cars and engines
	 */
	public int getUsedLength() {
		return _usedLength;
	}
	
	/**
	 * Set the operations mode for this location 
	 * @param ops NORMAL STAGING
	 */
	public void setLocationOps(int ops){
		int old = _locationOps;
		_locationOps = ops;
		if (old != ops)
			setDirtyAndFirePropertyChange("locationOps", Integer.toString(old), Integer.toString(ops));	// NOI18N
	}
	
	public int getLocationOps() {
		return _locationOps;
	}
	
	/**
	 * Sets the train directions that this location can service.
	 * EAST means that an Eastbound train can service the location.
	 * @param direction Any combination of EAST WEST NORTH SOUTH
	 */
	public void setTrainDirections(int direction){
		int old = _trainDir;
		_trainDir = direction;
		if (old != direction)
			setDirtyAndFirePropertyChange(TRAINDIRECTION_CHANGED_PROPERTY, Integer.toString(old), Integer.toString(direction));
	}
	
	public int getTrainDirections(){
		return _trainDir;
	}
	
	/**
	 * Sets the number of cars and or engines on for this location
	 * @param number
	 */
	public void setNumberRS(int number) {
		int old = _numberRS;
		_numberRS = number;
		if (old != number)
			setDirtyAndFirePropertyChange("numberRS", Integer.toString(old), Integer.toString(number));	// NOI18N
	}
	
	/**
	 * Gets the number of cars and engines at this location
	 * @return number of cars at this location
	 */
	public int getNumberRS() {
		return _numberRS;
	}
	
	/**
	 * When true, a switchlist is desired for this location.
	 * Used for preview and printing a manifest for a single location
	 * @param switchList
	 */
	public void setSwitchListEnabled(boolean switchList) {
		boolean old = _switchList;
		_switchList = switchList;
		if (old != switchList)
			setDirtyAndFirePropertyChange(SWITCHLIST_CHANGED_PROPERTY, old?"true":"false", switchList?"true":"false");	// NOI18N
	}
	
	/**
	 * Used to determine if switch list is needed for this location
	 * @return true if switch list needed
	 */
	public boolean isSwitchListEnabled() {
		return _switchList;
	}
	
	public void setDefaultPrinterName(String name){
		String old = _defaultPrinter;
		_defaultPrinter = name;
		if (!old.equals(name))
			setDirtyAndFirePropertyChange("defaultPrinter", old, name);	// NOI18N
	}
	
	public String getDefaultPrinterName(){
		return _defaultPrinter;
	}
	
	/**
	 * Automatically sets the print status for this location's switch list
	 * 
	 */
	public void setStatus(){
		if (getStatus().equals(PRINTED) || getStatus().equals(CSV_GENERATED) || !Setup.isSwitchListRealTime())
			setStatus(MODIFIED);
	}
	
	/**
	 * Sets the print status for this location's switch list
	 * @param status UNKNOWN PRINTED MODIFIED
	 */
	public void setStatus(String status){
		String old = _status;
		_status = status;
		if (!old.equals(status))
			setDirtyAndFirePropertyChange(STATUS_CHANGED_PROPERTY, old, status);
	}
	
	public String getStatus(){
		return _status;
	}
	
	public void setSwitchListState(int state){
		int old = _switchListState;
		_switchListState = state;
		if (old != state)
			setDirtyAndFirePropertyChange("SwitchListState", old, state);	// NOI18N
	}
	
	public int getSwitchListState(){
		return _switchListState;
	}
	
	/**
	 * Sets the train icon coordinates for an eastbound train
	 * arriving at this location.
	 * @param point The XY coordinates on the panel.
	 */
	public void setTrainIconEast(Point point){
		Point old = _trainIconEast;
		_trainIconEast = point;
		setDirtyAndFirePropertyChange("TrainIconEast", old.toString(), point.toString());	// NOI18N
	}
	
	public Point getTrainIconEast(){
		return _trainIconEast;
	}
	
	public void setTrainIconWest(Point point){
		Point old = _trainIconWest;
		_trainIconWest = point;
		setDirtyAndFirePropertyChange("TrainIconWest", old.toString(), point.toString());	// NOI18N
	}
	
	public Point getTrainIconWest(){
		return _trainIconWest;
	}
	
	public void setTrainIconNorth(Point point){
		Point old = _trainIconNorth;
		_trainIconNorth = point;
		setDirtyAndFirePropertyChange("TrainIconNorth", old.toString(), point.toString());	// NOI18N
	}
	
	public Point getTrainIconNorth(){
		return _trainIconNorth;
	}
	
	public void setTrainIconSouth(Point point){
		Point old = _trainIconSouth;
		_trainIconSouth = point;
		setDirtyAndFirePropertyChange("TrainIconSouth", old.toString(), point.toString());	// NOI18N
	}
	
	public Point getTrainIconSouth(){
		return _trainIconSouth;
	}
	
	
	/**
	 * Adds rolling stock to a specific location.  
	 * @param rs
	 */	
	public void addRS(RollingStock rs){
		setNumberRS(getNumberRS()+1);
		setUsedLength(getUsedLength() + Integer.parseInt(rs.getLength())+ RollingStock.COUPLER);
	}
	
	public void deleteRS(RollingStock rs){
		setNumberRS(getNumberRS()-1);
		setUsedLength(getUsedLength() - (Integer.parseInt(rs.getLength())+ RollingStock.COUPLER));
	}

	/**
	 * Increments the number of cars and or engines that will be picked up by a train
	 * at this location.
	 */
	public void addPickupRS() {
		int old = _pickupRS;
		_pickupRS++;
		setDirtyAndFirePropertyChange("addPickupRS", Integer.toString(old), Integer.toString(_pickupRS));	// NOI18N
	}
	
	/**
	 * Decrements the number of cars and or engines that will be picked up by a train
	 * at this location.
	 */
	public void deletePickupRS() {
		int old = _pickupRS;
		_pickupRS--;
		setDirtyAndFirePropertyChange("deletePickupRS", Integer.toString(old), Integer.toString(_pickupRS));	// NOI18N
	}
	
	/**
	 * Increments the number of cars and or engines that will be dropped off by trains at this
	 * location.
	 */
	public void addDropRS() {
		int old = _dropRS;
		_dropRS++;
		setDirtyAndFirePropertyChange("addDropRS", Integer.toString(old), Integer.toString(_dropRS));	// NOI18N
	}
	
	/**
	 * Decrements the number of cars and or engines that will be dropped off by trains at this
	 * location.
	 */
	public void deleteDropRS() {
		int old = _dropRS;
		_dropRS--;
		setDirtyAndFirePropertyChange("deleteDropRS", Integer.toString(old), Integer.toString(_dropRS));	// NOI18N
	}
	
	/**
	 * 
	 * @return the number of cars and engines that are scheduled for pick up at this
	 *         location.
	 */
	public int getPickupRS() {
		return _pickupRS;
	}

	/**
	 * 
	 * @return the number of cars and engines that are scheduled for drop at this
	 *         location.
	 */
	public int getDropRS() {
		return _dropRS;
	}

	public void setComment(String comment) {
		String old = _comment;
		_comment = comment;
		if (!old.equals(comment))
			setDirtyAndFirePropertyChange ("LocationComment", old, comment);	// NOI18N
	}

	public String getComment() {
		return _comment;
	}
	
	public void setSwitchListComment(String comment) {
		String old = _switchListComment;
		_switchListComment = comment;
		if (!old.equals(comment))
			setDirtyAndFirePropertyChange (SWITCHLIST_COMMENT_CHANGED_PROPERTY, old, comment);		
	}

	public String getSwitchListComment() {
		return _switchListComment;
	}
   
    private String[] getTypeNames(){
      	String[] types = new String[_listTypes.size()];
     	for (int i=0; i<_listTypes.size(); i++)
     		types[i] = _listTypes.get(i);
   		return types;
    }
    
    private void setTypeNames(String[] types){
    	if (types.length == 0) return;
    	jmri.util.StringUtil.sort(types);
 		for (int i=0; i<types.length; i++)
 			_listTypes.add(types[i]);
    }
    
    /**
     * Adds the specific type of rolling stock to the will service list
     * @param type of rolling stock that location will service
     */
    public void addTypeName(String type){
    	// insert at start of list, sort later
    	if (_listTypes.contains(type))
    		return;
    	_listTypes.add(0,type);
    	log.debug("location ("+getName()+") add rolling stock type "+type);
    	setDirtyAndFirePropertyChange (TYPES_CHANGED_PROPERTY, _listTypes.size()-1, _listTypes.size());
    }
    
    public void deleteTypeName(String type){
    	if (!_listTypes.contains(type))
    		return;
    	_listTypes.remove(type);
    	log.debug("location ("+getName()+") delete rolling stock type "+type);
     	setDirtyAndFirePropertyChange (TYPES_CHANGED_PROPERTY, _listTypes.size()+1, _listTypes.size());
     }
    
    public boolean acceptsTypeName(String type){
    	if (!CarTypes.instance().containsName(type) && !EngineTypes.instance().containsName(type))
    		return false;
    	return _listTypes.contains(type);
    }
  
	/** 
	 * Adds a track to this location.  Valid tracks are
	 * spurs, yards, staging and interchange tracks.
	 *  @param name of track
	 * @param type of track
	 * @return Track
	 */
    public Track addTrack(String name, String type){
		Track track = getTrackByName(name, type);
		if (track == null){
			_IdNumber++;
			String id = _id + "s"+ Integer.toString(_IdNumber);
			log.debug("adding new "+ type +" to "+getName()+ " id: " + id);
	   		track = new Track(id, name, type, this);
	   		register(track);
 		}
		resetMoves();	// give all of the tracks equal weighting
		return track;
	}
    
 
   /**
     * Remember a NamedBean Object created outside the manager.
 	 */
    public void register(Track track){
    	Integer old = Integer.valueOf(_trackHashTable.size());
        _trackHashTable.put(track.getId(), track);
        // add to the locations's available track length
        setLength(getLength() + track.getLength());
        // find last id created
        String[] getId = track.getId().split("s");
        int id = Integer.parseInt(getId[1]);
        if (id > _IdNumber)
        	_IdNumber = id;
        setDirtyAndFirePropertyChange(TRACK_LISTLENGTH_CHANGED_PROPERTY, old, Integer.valueOf(_trackHashTable.size()));
         // listen for name and state changes to forward
        track.addPropertyChangeListener(this);
    }
    
    public void deleteTrack(Track track){
    	if (track != null){
    		track.removePropertyChangeListener(this);
    		// subtract from the locations's available track length
            setLength(getLength() - track.getLength());
    		track.dispose();
    		Integer old = Integer.valueOf(_trackHashTable.size());
    		_trackHashTable.remove(track.getId());
    		setDirtyAndFirePropertyChange(TRACK_LISTLENGTH_CHANGED_PROPERTY, old, Integer.valueOf(_trackHashTable.size()));
    	}
    }
    
	/**
	 * Get track location by name and type
	 * @param name track's name
	 * @param type track type
	 * @return track location
	 */
    
    public Track getTrackByName(String name, String type) {
    	Track track;
    	Enumeration<Track> en =_trackHashTable.elements();
    	for (int i = 0; i < _trackHashTable.size(); i++){
    		track = en.nextElement();
    		if (type == null){
    			if (track.getName().equals(name))
    				return track;
    		} else if (track.getName().equals(name) && track.getLocType().equals(type))
    			return track;
    	}
    	return null;
    }
    
    public Track getTrackById (String id){
    	return _trackHashTable.get(id);
    }
    
    /**
     * Gets a list of track ids ordered by id for this location.
     * @return list of track ids for this location
     */
    public List<String> getTrackIdsByIdList() {
		String[] arr = new String[_trackHashTable.size()];
		List<String> out = new ArrayList<String>();
		Enumeration<String> en = _trackHashTable.keys();
		int i = 0;
		while (en.hasMoreElements()) {
			arr[i] = en.nextElement();
			i++;
		}
		jmri.util.StringUtil.sort(arr);
		for (i = 0; i < arr.length; i++)
			out.add(arr[i]);
		return out;
	}
    
    /**
	 * Sort ids by track location name. Returns a list of ids of a given track type.
	 * If type is null returns all track ids for the location.
	 * @param type track type: Track.YARD, Track.SIDING, Track.INTERCHANGE, Track.STAGING
	 * @return list of track ids ordered by name
	 */
    public List<String> getTrackIdsByNameList(String type) {
		// first get id list
		List<String> sortList = getTrackIdsByIdList();
		// now re-sort
		List<String> out = new ArrayList<String>();
		String locName = "";
		boolean locAdded = false;
		Track track;
		Track trackOut;

		for (int i = 0; i < sortList.size(); i++) {
			locAdded = false;
			track = getTrackById(sortList.get(i));
			locName = track.getName();
			for (int j = 0; j < out.size(); j++) {
				trackOut = getTrackById(out.get(j));
				String outLocName = trackOut.getName();
				if (locName.compareToIgnoreCase(outLocName) < 0
						&& (type != null && track.getLocType().equals(type) || type == null)) {
					out.add(j, sortList.get(i));
					locAdded = true;
					break;
				}
			}
			if (!locAdded
					&& (type != null && track.getLocType().equals(type) || type == null)) {
				out.add(sortList.get(i));
			}
		}
		return out;
	}
    
    /**
     * Sort ids by track moves.  Returns a list of ids of a given track type.
     * If type is null returns all track ids for the location are returned.  
     * Tracks with schedules are placed at the start of the list.
     * @param type track type: Track.YARD, Track.SIDING, Track.INTERCHANGE, Track.STAGING
     * @return list of track ids ordered by moves
     */
    public List<String> getTrackIdsByMovesList(String type) {
		// first get id list
		List<String> sortList = getTrackIdsByIdList();
		// now re-sort
		List<String> moveList = new ArrayList<String>();
		boolean locAdded = false;
		Track track;
		Track trackOut;

		for (int i = 0; i < sortList.size(); i++) {
			locAdded = false;
			track = getTrackById(sortList.get(i));
			int moves = track.getMoves();
			for (int j = 0; j < moveList.size(); j++) {
				trackOut = getTrackById(moveList.get(j));
				int outLocMoves = trackOut.getMoves();
				if (moves < outLocMoves
						&& (type != null && track.getLocType().equals(type) || type == null)) {
					moveList.add(j, sortList.get(i));
					locAdded = true;
					break;
				}
			}
			if (!locAdded
					&& (type != null && track.getLocType().equals(type) || type == null)) {
				moveList.add(sortList.get(i));
			}
		}
		// bias tracks with schedules
		List<String> out = new ArrayList<String>();
		for (int i=0; i<moveList.size(); i++){
			track = getTrackById(moveList.get(i));
			if (!track.getScheduleId().equals("")){
				out.add(moveList.get(i));
				moveList.remove(i);
				i--;
			}
		}
		for (int i=0; i<moveList.size(); i++){
			out.add(moveList.get(i));
		}
		return out;
	}
    
    public boolean isTrackAtLocation(Track track){
    	if (track == null)
    		return true;
    	return _trackHashTable.contains(track);  	
    }
    
    /**
     * Reset the move count for all tracks at this location
     */
    public void resetMoves(){
    	List<String> tracks = getTrackIdsByIdList();
    	for (int i=0; i<tracks.size(); i++) {
    		Track track = getTrackById(tracks.get(i));
    		track.setMoves(0);
    	}
    }
      
    /**
     * Updates a JComboBox with all of the track locations for
     * this location.
     * @param box JComboBox to be updated.
     */
    public void updateComboBox(JComboBox box) {
    	box.removeAllItems();
    	box.addItem("");
    	List<String> tracks = getTrackIdsByNameList(null);
		for (int i = 0; i < tracks.size(); i++){
			box.addItem(getTrackById(tracks.get(i)));
		}
    }
    
    /**
     * Updates a JComboBox with tracks that can service the rolling stock.
     * @param box JComboBox to be updated.
     * @param rs Rolling Stock to be serviced
     * @param filter When true, remove tracks not able to service rs.
     * @param destination When true, the tracks are destinations for the rs.
     */
    public void updateComboBox(JComboBox box, RollingStock rs, boolean filter, boolean destination){
    	updateComboBox(box);
    	if (!filter || rs == null)
    		return;
       	List<String> tracks = getTrackIdsByNameList(null);
		for (int i = 0; i < tracks.size(); i++){
			Track track = getTrackById(tracks.get(i));
			String status = "";
			if (destination){
				status = rs.testDestination(this, track);
			} else {
				status = rs.testLocation(this, track);
			}
			if (status.equals(Track.OKAY) && (!destination || !track.getLocType().equals(Track.STAGING))){
				box.setSelectedItem(track);
				log.debug("Available track: "+track.getName()+" for location: "+getName());
			} else {
				box.removeItem(track);
			}
		}   	
    }
    
    /**
     * Adds a track pool for this location.  A track pool is a set of tracks
     * where the length of the tracks is shared between all of them.
     * @param name the name of the Pool to create
     * @return Pool
     */
    public Pool addPool(String name){
		Pool pool = getPoolByName(name);
		if (pool == null){
			_idPoolNumber++;
			String id = _id + "p"+ Integer.toString(_idPoolNumber);
			log.debug("creating new pool ("+name+") id: " + id);
	   		pool = new Pool(id, name);
	   		register(pool);
 		}
		return pool;
    }
    
    public void removePool(Pool pool){
    	if (pool != null){
    		_poolHashTable.remove(pool.getId());
    		setDirtyAndFirePropertyChange(POOL_LENGTH_CHANGED_PROPERTY, Integer.valueOf(_poolHashTable.size()+1), Integer.valueOf(_poolHashTable.size()));
    	} 	
    }
    
    public Pool getPoolByName(String name) {
    	Pool pool;
    	Enumeration<Pool> en =_poolHashTable.elements();
    	while (en.hasMoreElements()) {
    		pool = en.nextElement(); 
    		if (pool.getName().equals(name))
    			return pool;
    	}
    	return null;
    }
    
    public void register(Pool pool){
    	Integer old = Integer.valueOf(_poolHashTable.size());
    	 _poolHashTable.put(pool.getId(), pool);
         // find last id created
         String[] getId = pool.getId().split("p");
         int id = Integer.parseInt(getId[1]);
         if (id > _idPoolNumber)
         	_idPoolNumber = id;
    	setDirtyAndFirePropertyChange(POOL_LENGTH_CHANGED_PROPERTY, old, Integer.valueOf(_poolHashTable.size()));
    }

    public void updatePoolComboBox(JComboBox box) {
    	box.removeAllItems();
    	box.addItem("");
    	List<Pool> pools = getPoolsByNameList();
		for (int i = 0; i < pools.size(); i++){
			box.addItem(pools.get(i));
		}
    }
    
    /**
     * Gets a list of Pools for this location.
     * @return A list of Pools
     */
    public List<Pool> getPoolsByNameList(){
    	List<Pool> pools = new ArrayList<Pool>();
    	Enumeration<Pool> en =_poolHashTable.elements();
    	while (en.hasMoreElements()) {
    		pools.add(en.nextElement());
    	}
    	return pools;
    }
    
    /**
     * Used to determine if there are Pools at this location.
     * @return True if there are Pools at this location
     */
    public boolean hasPools(){
    	return _poolHashTable.size()>0;
    }
    
    /**
     * Used to determine if there are any planned pickups at this location.
     * @return True if there are planned pickups
     */
    public boolean hasPlannedPickups(){
       	List<String> tracks = getTrackIdsByIdList();
    	for (int i=0; i<tracks.size(); i++){
    		Track track = getTrackById(tracks.get(i));
    		if (track.getIgnoreUsedLengthPercentage()>0)
    			return true;
    	}
    	return false;
    }
  	    
    public void dispose(){
    	List<String> tracks = getTrackIdsByIdList();
    	for (int i=0; i<tracks.size(); i++){
    		Track track = getTrackById(tracks.get(i));
    		deleteTrack(track);
    	}
    	setDirtyAndFirePropertyChange (DISPOSE_CHANGED_PROPERTY, null, "Dispose");
    }
 	
   /**
     * Construct this Entry from XML. This member has to remain synchronized with the
     * detailed DTD in operations-locations.dtd
     *
     * @param e  Consist XML element
     */
    public Location(Element e) {
    	//if (log.isDebugEnabled()) log.debug("ctor from element "+e);
        Attribute a;
        if ((a = e.getAttribute("id")) != null )  _id = a.getValue();	// NOI18N
        else log.warn("no id attribute in location element when reading operations");
        if ((a = e.getAttribute("name")) != null )  _name = a.getValue();	// NOI18N
        if ((a = e.getAttribute("ops")) != null )  _locationOps = Integer.parseInt(a.getValue());	// NOI18N
        if ((a = e.getAttribute("dir")) != null )  _trainDir = Integer.parseInt(a.getValue());	// NOI18N
        if ((a = e.getAttribute("switchList")) != null )  _switchList = (a.getValue().equals("true"));	// NOI18N
        if ((a = e.getAttribute("switchListState")) != null ) {	// NOI18N
        	_switchListState = Integer.parseInt(a.getValue());
        	if (getSwitchListState() == SW_PRINTED)
        		setStatus(PRINTED);
        }
        if ((a = e.getAttribute("printerName")) != null )  _defaultPrinter = a.getValue();	// NOI18N
        // load train icon coordinates
        Attribute x;
        Attribute y;
        if ((x = e.getAttribute("eastTrainIconX")) != null && (y = e.getAttribute("eastTrainIconY"))!= null){	// NOI18N
        	setTrainIconEast(new Point(Integer.parseInt(x.getValue()),Integer.parseInt(y.getValue())));
        }
        if ((x = e.getAttribute("westTrainIconX")) != null && (y = e.getAttribute("westTrainIconY"))!= null){	// NOI18N
        	setTrainIconWest(new Point(Integer.parseInt(x.getValue()),Integer.parseInt(y.getValue())));
        }
        if ((x = e.getAttribute("northTrainIconX")) != null && (y = e.getAttribute("northTrainIconY"))!= null){	// NOI18N
        	setTrainIconNorth(new Point(Integer.parseInt(x.getValue()),Integer.parseInt(y.getValue())));
        }
        if ((x = e.getAttribute("southTrainIconX")) != null && (y = e.getAttribute("southTrainIconY"))!= null){	// NOI18N
        	setTrainIconSouth(new Point(Integer.parseInt(x.getValue()),Integer.parseInt(y.getValue())));
        }      
        if ((a = e.getAttribute("comment")) != null )  _comment = a.getValue();	// NOI18N
        if ((a = e.getAttribute("switchListComment")) != null )  _switchListComment = a.getValue();	// NOI18N
        if ((a = e.getAttribute("physicalLocation")) != null) _physicalLocation = PhysicalLocation.parse(a.getValue());	// NOI18N
        if ((a = e.getAttribute("carTypes")) != null ) {	// NOI18N
        	String names = a.getValue();
        	String[] Types = names.split("%%");	// NOI18N
        	//if (log.isDebugEnabled()) log.debug("rolling stock types: "+names);
        	setTypeNames(Types);
        }
        // early version of operations called tracks "secondary"
        if (e.getChildren("secondary") != null) {	// NOI18N
        	@SuppressWarnings("unchecked")
            List<Element> l = e.getChildren("secondary");	// NOI18N
            //if (log.isDebugEnabled()) log.debug("location ("+getName()+") has "+l.size()+" secondary locations");
            for (int i=0; i<l.size(); i++) {
                register(new Track(l.get(i), this));
            }
        }
        if (e.getChildren("track") != null) {	// NOI18N
        	@SuppressWarnings("unchecked")
            List<Element> l = e.getChildren("track");	// NOI18N
            if (log.isDebugEnabled()) log.debug("location ("+getName()+") has "+l.size()+" tracks");
            for (int i=0; i<l.size(); i++) {
                register(new Track(l.get(i), this));
            }
        }
    }

    /**
     * Create an XML element to represent this Entry. This member has to remain synchronized with the
     * detailed DTD in operations-locations.dtd.
     * @return Contents in a JDOM Element
     */
    public Element store() {
        Element e = new Element("location");	// NOI18N
        e.setAttribute("id", getId());	// NOI18N
        e.setAttribute("name", getName());	// NOI18N
        e.setAttribute("ops", Integer.toString(getLocationOps()));	// NOI18N
        e.setAttribute("dir", Integer.toString(getTrainDirections()));	// NOI18N
        e.setAttribute("switchList", isSwitchListEnabled()?"true":"false");	// NOI18N
        if (!Setup.isSwitchListRealTime())
        	e.setAttribute("switchListState", Integer.toString(getSwitchListState()));	// NOI18N
        if (!getDefaultPrinterName().equals("")){
        	e.setAttribute("printerName", getDefaultPrinterName());	// NOI18N
        }
        if (!getTrainIconEast().equals(new Point())){
        	e.setAttribute("eastTrainIconX", Integer.toString(getTrainIconEast().x));	// NOI18N
        	e.setAttribute("eastTrainIconY", Integer.toString(getTrainIconEast().y));	// NOI18N
        }
        if (!getTrainIconWest().equals(new Point())){
        	e.setAttribute("westTrainIconX", Integer.toString(getTrainIconWest().x));	// NOI18N
        	e.setAttribute("westTrainIconY", Integer.toString(getTrainIconWest().y));	// NOI18N
        }
        if (!getTrainIconNorth().equals(new Point())){
        	e.setAttribute("northTrainIconX", Integer.toString(getTrainIconNorth().x));	// NOI18N
        	e.setAttribute("northTrainIconY", Integer.toString(getTrainIconNorth().y));	// NOI18N
        }
        if (!getTrainIconSouth().equals(new Point())){
        	e.setAttribute("southTrainIconX", Integer.toString(getTrainIconSouth().x));	// NOI18N
        	e.setAttribute("southTrainIconY", Integer.toString(getTrainIconSouth().y));	// NOI18N
        }
        // build list of rolling stock types for this location
        String[] types = getTypeNames();
        CarTypes ct = CarTypes.instance();
        EngineTypes et = EngineTypes.instance();
        StringBuffer buf = new StringBuffer();
        for (int i=0; i<types.length; i++){
    		// remove types that have been deleted by user
    		if (ct.containsName(types[i]) || et.containsName(types[i]))
    			buf.append(types[i]+"%%");	// NOI18N
        }
        e.setAttribute("carTypes", buf.toString());	// NOI18N

        if (_physicalLocation != null)
        	e.setAttribute("physicalLocation", _physicalLocation.toString());	// NOI18N

        e.setAttribute("comment", getComment());	// NOI18N
        e.setAttribute("switchListComment", getSwitchListComment());	// NOI18N
        
        List<String> tracks = getTrackIdsByIdList();
        for (int i=0; i<tracks.size(); i++) {
        	String id = tracks.get(i);
        	Track track = getTrackById(id);
	            e.addContent(track.store());
        }
  
        return e;
    }
    
    public void propertyChange(java.beans.PropertyChangeEvent e) {
    	if(Control.showProperty && log.isDebugEnabled())
    		log.debug("location (" + getName() + ") sees property change: "
    				+ e.getPropertyName() + " from track (" +e.getSource()+") old: " + e.getOldValue() + " new: "
    				+ e.getNewValue());
    	// update length of tracks at this location if track length changes
    	if(e.getPropertyName().equals(Track.LENGTH_CHANGED_PROPERTY)){
    		setLength(getLength() - Integer.parseInt((String)e.getOldValue()) + Integer.parseInt((String)e.getNewValue()));
    	}
    	// if a track type change, must update all tables
    	if(e.getPropertyName().equals(Track.TRACK_TYPE_CHANGED_PROPERTY)){
    		setDirtyAndFirePropertyChange(TRACK_LISTLENGTH_CHANGED_PROPERTY, null, null);
    	}
    }

	java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(
			this);

	public synchronized void addPropertyChangeListener(
			java.beans.PropertyChangeListener l) {
		pcs.addPropertyChangeListener(l);
	}

	public synchronized void removePropertyChangeListener(
			java.beans.PropertyChangeListener l) {
		pcs.removePropertyChangeListener(l);
	}

	protected void setDirtyAndFirePropertyChange(String p, Object old, Object n) {
		LocationManagerXml.instance().setDirty(true);
		pcs.firePropertyChange(p, old, n);
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger
			.getLogger(Location.class.getName());

}
