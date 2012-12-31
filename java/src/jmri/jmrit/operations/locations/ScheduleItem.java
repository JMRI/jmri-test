package jmri.jmrit.operations.locations;

import jmri.jmrit.operations.setup.Control;

/**
 * Represents one schedule item of a schedule
 * 
 * @author Daniel Boudreau Copyright (C) 2009, 2010
 * @version             $Revision$
 */
public class ScheduleItem implements java.beans.PropertyChangeListener {

	protected String _id = "";
	protected int _sequenceId = 0;			// used to determine order in schedule
	protected String _trainScheduleId = "";	// which day of the weeks to service this item
	protected String _type = "";			// the type of car
	protected String _road = "";			// the car road
	protected String _load ="";				// the car load requested
	protected String _ship ="";				// the car load shipped
	protected Location _destination = null;	// car destination after load
	protected Track _trackDestination = null;// car destination track after load
	protected int _count = 1;				// the number of times this type of car must be dropped
	protected int _wait = 0;				// how many trains this car must wait before being picked up
	protected int _hits = 0;				// how many times this schedule item has been used
	protected String _comment = "";
			
	public static final String TRAIN_SCHEDULE_CHANGED_PROPERTY = "trainScheduleId";	// NOI18N
	public static final String COUNT_CHANGED_PROPERTY = "scheduleItemCount";	// NOI18N
	public static final String TYPE_CHANGED_PROPERTY = "scheduleItemType";	// NOI18N
	public static final String ROAD_CHANGED_PROPERTY = "scheduleItemRoad";	// NOI18N
	public static final String LOAD_CHANGED_PROPERTY = "scheduleItemLoad";	// NOI18N
	public static final String DESTINATION_CHANGED_PROPERTY = "scheduleItemDestination";	// NOI18N
	public static final String DESTINATION_TRACK_CHANGED_PROPERTY = "scheduleItemDestinationTrack";	// NOI18N
	public static final String WAIT_CHANGED_PROPERTY = "scheduleItemWait";	// NOI18N
	public static final String HITS_CHANGED_PROPERTY = "scheduleItemHits";	// NOI18N
	public static final String DISPOSE = "dispose";	// NOI18N
	
	/**
	 * 
	 * @param id
	 * @param type car type to schedule
	 */
	public ScheduleItem(String id, String type) {
		log.debug("New schedule item type (" + type + ") id: " + id);
		_type = type;
		_id = id;
	}

	public String getId() {
		return _id;
	}

	public String getType() {
		return _type;
	}
	
	/**
	 * Sets the type of car requested.
	 * @param type The car type requested.
	 */
	public void setType(String type){
		String old = _type;
		_type = type;
		firePropertyChange (TYPE_CHANGED_PROPERTY, old, type);
	}
	
	public String getTrainScheduleId() {
		return _trainScheduleId;
	}
	
	public void setTrainScheduleId(String id) {
		String old = _trainScheduleId;
		_trainScheduleId = id;
		firePropertyChange (TRAIN_SCHEDULE_CHANGED_PROPERTY, old, id);
	}
	
	public String getRoad() {
		return _road;
	}
	
	/**
	 * Sets the requested car road name.
	 * @param road The car road requested.
	 */
	public void setRoad(String road){
		String old = _road;
		_road = road;
		firePropertyChange (ROAD_CHANGED_PROPERTY, old, road);
	}
	
	/**
	 * Sets the car load requested.
	 * @param load The load name requested.
	 */
	public void setLoad(String load){
		String old = _load;
		_load = load;
		firePropertyChange (LOAD_CHANGED_PROPERTY, old, load);
	}
	
	public String getLoad() {
		return _load;
	}
	
	/**
	 * Sets the car load that will ship.
	 * @param load The car load shipped.
	 */
	public void setShip(String load){
		String old = _ship;
		_ship = load;
		firePropertyChange (LOAD_CHANGED_PROPERTY, old, load);
	}
	
	public String getShip() {
		return _ship;
	}
	
	public int getSequenceId(){
		return _sequenceId;
	}
	
	public void setSequenceId(int sequence) {
		// property change not needed
		_sequenceId = sequence;
	}
	
	public int getCount(){
		return _count;
	}
	
	public void setCount(int count){
		int old = _count;
		_count = count;
		firePropertyChange (COUNT_CHANGED_PROPERTY, old, count);
	}
	
	public int getWait(){
		return _wait;
	}
	
	public void setWait(int wait){
		int old = _wait;
		_wait = wait;
		firePropertyChange (WAIT_CHANGED_PROPERTY, old, wait);
	}
	
	public int getHits(){
		return _hits;
	}
	
	public void setHits(int hit){
		int old = _hits;
		_hits = hit;
		firePropertyChange (HITS_CHANGED_PROPERTY, old, hit);
	}
	
	public Location getDestination() {
		return _destination;
	}
	
	public void setDestination(Location destination){
		Location old = _destination;
		_destination = destination;
		String oldName = "null";	// NOI18N
		if (old != null)
			oldName = old.getName();
		String newName = "null";	// NOI18N
		if (_destination != null)
			newName = _destination.getName();
		firePropertyChange (DESTINATION_CHANGED_PROPERTY, oldName, newName);
	}
	
	public String getDestinationName() {
		if (_destination != null)
			return _destination.getName();
		return "";
	}
	
	public String getDestinationId() {
		if (_destination != null)
			return _destination.getId();
		return "";
	}

	public Track getDestinationTrack() {
		return _trackDestination;
	}
	
	public void setDestinationTrack(Track track){
		Track old = _trackDestination;
		_trackDestination = track;
		String oldName = "null";	// NOI18N
		if (old != null)
			oldName = old.getName();
		String newName = "null";	// NOI18N
		if (_trackDestination != null)
			newName = _trackDestination.getName();
		firePropertyChange (DESTINATION_TRACK_CHANGED_PROPERTY, oldName, newName);
	}
	
	public String getDestinationTrackName() {
		if (_trackDestination != null)
			return _trackDestination.getName();
		return "";
	}
	
	public String getDestinationTrackId() {
		if (_trackDestination != null)
			return _trackDestination.getId();
		return "";
	}
	
	public void setComment(String comment) {
		_comment = comment;
	}

	public String getComment() {
		return _comment;
	}


    public void dispose(){
    	firePropertyChange (DISPOSE, null, DISPOSE);
    }
    
    static final String ID = "id";		// NOI18N
    static final String ITEM = "item";	// NOI18N
    static final String SEQUENCE_ID = "ScheduleId";	// NOI18N
    static final String TRAIN_SCHEDULE_ID = "trainScheduleId";	// NOI18N
    static final String COUNT = "count";	// NOI18N
    static final String WAIT = "wait";	// NOI18N
    static final String TYPE = "type";	// NOI18N
    static final String ROAD = "road";	// NOI18N
    static final String LOAD = "load";	// NOI18N
    static final String SHIP = "ship";	// NOI18N
    static final String DESTINATION_ID = "destinationId";	// NOI18N
    static final String DEST_TRACK_ID = "destTrackId";	// NOI18N
    static final String COMMENT = "comment";	// NOI18N
    static final String HITS = "hits";	// NOI18N
    
	   /**
     * Construct this Entry from XML. This member has to remain synchronized with the
     * detailed DTD in operations-config.xml
     *
     * @param e  Consist XML element
     */
    public ScheduleItem(org.jdom.Element e) {
        //if (log.isDebugEnabled()) log.debug("ctor from element "+e);
        org.jdom.Attribute a;
        if ((a = e.getAttribute(ID)) != null )  _id = a.getValue();
        else log.warn("no id attribute in Schedule Item element when reading operations");
        if ((a = e.getAttribute(SEQUENCE_ID)) != null )  _sequenceId = Integer.parseInt(a.getValue());
        if ((a = e.getAttribute(TRAIN_SCHEDULE_ID)) != null )  _trainScheduleId = a.getValue();
        if ((a = e.getAttribute(COUNT)) != null )  _count = Integer.parseInt(a.getValue());
        if ((a = e.getAttribute(WAIT)) != null )  _wait = Integer.parseInt(a.getValue());
        if ((a = e.getAttribute(TYPE)) != null )  _type = a.getValue();
        if ((a = e.getAttribute(ROAD)) != null )  _road = a.getValue();
        if ((a = e.getAttribute(LOAD)) != null )  _load = a.getValue();
        if ((a = e.getAttribute(SHIP)) != null )  _ship = a.getValue();
		if ((a = e.getAttribute(DESTINATION_ID)) != null)
			_destination = LocationManager.instance().getLocationById(a.getValue());
		if ((a = e.getAttribute(DEST_TRACK_ID)) != null  && _destination != null)
			_trackDestination = _destination.getTrackById(a.getValue());
        if ((a = e.getAttribute(COMMENT)) != null )  _comment = a.getValue();
        if ((a = e.getAttribute(HITS)) != null )  _hits = Integer.parseInt(a.getValue());
    }

    /**
     * Create an XML element to represent this Entry. This member has to remain synchronized with the
     * detailed DTD in operations-config.xml.
     * @return Contents in a JDOM Element
     */
    public org.jdom.Element store() {
    	org.jdom.Element e = new org.jdom.Element(ITEM);
    	e.setAttribute(ID, getId());
    	e.setAttribute(SEQUENCE_ID, Integer.toString(getSequenceId()));
    	e.setAttribute(TRAIN_SCHEDULE_ID, getTrainScheduleId());
    	e.setAttribute(COUNT, Integer.toString(getCount()));
    	e.setAttribute(WAIT, Integer.toString(getWait()));
    	e.setAttribute(TYPE, getType());
    	e.setAttribute(ROAD, getRoad());
    	e.setAttribute(LOAD, getLoad());
    	e.setAttribute(SHIP, getShip());
		if (!getDestinationId().equals(""))
			e.setAttribute(DESTINATION_ID, getDestinationId());
		if (!getDestinationTrackId().equals(""))
			e.setAttribute(DEST_TRACK_ID, getDestinationTrackId());
       	e.setAttribute(COMMENT, getComment());
       	e.setAttribute(HITS, Integer.toString(getHits()));
    	return e;
    }
    
    public void propertyChange(java.beans.PropertyChangeEvent e) {
    	if(Control.showProperty && log.isDebugEnabled())
    		log.debug("ScheduleItem ("+getType()+ ") id (" +getId()+ ") sees property change " + e.getPropertyName() + " old: " + e.getOldValue() + " new: "
				+ e.getNewValue());
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

	protected void firePropertyChange(String p, Object old, Object n) {
		pcs.firePropertyChange(p, old, n);
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(ScheduleItem.class.getName());

}
