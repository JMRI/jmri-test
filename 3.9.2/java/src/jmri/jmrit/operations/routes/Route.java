package jmri.jmrit.operations.routes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JComboBox;

import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;

import org.jdom.Attribute;
import org.jdom.Element;

/**
 * Represents a route on the layout
 * 
 * @author Daniel Boudreau Copyright (C) 2008, 2010
 * @version $Revision$
 */
public class Route implements java.beans.PropertyChangeListener {

	protected String _id = "";
	protected String _name = "";
	protected String _comment = "";

	// stores location names for this route
	protected Hashtable<String, RouteLocation> _routeHashTable = new Hashtable<String, RouteLocation>();
	protected int _IdNumber = 0; // each location in a route gets its own id
	protected int _sequenceNum = 0; // each location has a unique sequence number

	public static final int EAST = 1; // train direction
	public static final int WEST = 2;
	public static final int NORTH = 4;
	public static final int SOUTH = 8;

	public static final String LISTCHANGE_CHANGED_PROPERTY = "routeListChange"; // NOI18N
	public static final String DISPOSE = "routeDispose"; // NOI18N

	public static final String OKAY = Bundle.getMessage("Okay");
	public static final String ORPHAN = Bundle.getMessage("Orphan");
	public static final String ERROR = Bundle.getMessage("Error");

	public Route(String id, String name) {
		log.debug("New route ({}) id: {}", name, id);
		_name = name;
		_id = id;
	}

	public String getId() {
		return _id;
	}

	public void setName(String name) {
		String old = _name;
		_name = name;
		if (!old.equals(name)) {
			setDirtyAndFirePropertyChange("nameChange", old, name); // NOI18N
		}
	}

	// for combo boxes
	public String toString() {
		return _name;
	}

	public String getName() {
		return _name;
	}

	public void setComment(String comment) {
		String old = _comment;
		_comment = comment;
		if (!old.equals(comment)) {
			setDirtyAndFirePropertyChange("commentChange", old, comment); // NOI18N
		}
	}

	public String getComment() {
		return _comment;
	}

	public void dispose() {
		setDirtyAndFirePropertyChange(DISPOSE, null, DISPOSE);
	}

	/**
	 * Adds a location to the end of this route
	 * 
	 * @param location
	 * @return RouteLocation created for the location added
	 */
	public RouteLocation addLocation(Location location) {
		_IdNumber++;
		_sequenceNum++;
		String id = _id + "r" + Integer.toString(_IdNumber);
		log.debug("adding new location to (" + getName() + ") id: " + id);
		RouteLocation rl = new RouteLocation(id, location);
		rl.setSequenceId(_sequenceNum);
		Integer old = Integer.valueOf(_routeHashTable.size());
		_routeHashTable.put(rl.getId(), rl);

		setDirtyAndFirePropertyChange(LISTCHANGE_CHANGED_PROPERTY, old, Integer.valueOf(_routeHashTable.size()));
		// listen for drop and pick up changes to forward
		rl.addPropertyChangeListener(this);
		return rl;
	}

	/**
	 * Add a route location at a specific place (sequence) in the route Allowable sequence numbers are 0 to max size of
	 * route;
	 * 
	 * @param location
	 * @param sequence
	 * @return route location
	 */
	public RouteLocation addLocation(Location location, int sequence) {
		RouteLocation rl = addLocation(location);
		if (sequence < 0 || sequence > _routeHashTable.size())
			return rl;
		for (int i = 0; i < _routeHashTable.size() - sequence; i++)
			moveLocationUp(rl);
		return rl;
	}

	/**
	 * Remember a NamedBean Object created outside the manager.
	 */
	public void register(RouteLocation rl) {
		Integer old = Integer.valueOf(_routeHashTable.size());
		_routeHashTable.put(rl.getId(), rl);

		// find last id created
		String[] getId = rl.getId().split("r");
		int id = Integer.parseInt(getId[1]);
		if (id > _IdNumber)
			_IdNumber = id;
		// find highest sequence number
		if (rl.getSequenceId() > _sequenceNum)
			_sequenceNum = rl.getSequenceId();
		setDirtyAndFirePropertyChange(LISTCHANGE_CHANGED_PROPERTY, old, Integer.valueOf(_routeHashTable.size()));
		// listen for drop and pick up changes to forward
		rl.addPropertyChangeListener(this);
	}

	/**
	 * Delete a RouteLocation
	 * 
	 * @param rl
	 */
	public void deleteLocation(RouteLocation rl) {
		if (rl != null) {
			rl.removePropertyChangeListener(this);
			// subtract from the locations's available track length
			String id = rl.getId();
			rl.dispose();
			Integer old = Integer.valueOf(_routeHashTable.size());
			_routeHashTable.remove(id);
			resequenceIds();
			setDirtyAndFirePropertyChange(LISTCHANGE_CHANGED_PROPERTY, old, Integer.valueOf(_routeHashTable.size()));
		}
	}

	public int size() {
		return _routeHashTable.size();
	}

	/**
	 * Reorder the location sequence numbers for this route
	 */
	private void resequenceIds() {
		List<RouteLocation> routeList = getLocationsBySequenceList();
		int i;
		for (i = 0; i < routeList.size(); i++) {
			routeList.get(i).setSequenceId(i + 1); // start sequence numbers at 1
		}
		_sequenceNum = i;
	}

	/**
	 * Get the first location in a route
	 * 
	 * @return the first route location
	 */
	public RouteLocation getDepartsRouteLocation() {
		List<RouteLocation> list = getLocationsBySequenceList();
		if (list.size() > 0) {
			return list.get(0);
		}
		return null;
	}

	/**
	 * Get location by name (gets last route location with name)
	 * 
	 * @param name
	 * @return route location
	 */
	public RouteLocation getLastLocationByName(String name) {
		List<RouteLocation> routeList = getLocationsBySequenceList();
		RouteLocation rl;

		for (int i = routeList.size() - 1; i >= 0; i--) {
			rl = routeList.get(i);
			if (rl.getName().equals(name))
				return rl;
		}
		return null;
	}

	/**
	 * Get a RouteLocation by id
	 * 
	 * @param id
	 * @return route location
	 */
	public RouteLocation getLocationById(String id) {
		return _routeHashTable.get(id);
	}

	private List<RouteLocation> getLocationsByIdList() {
		List<RouteLocation> out = new ArrayList<RouteLocation>();
		Enumeration<RouteLocation> en = _routeHashTable.elements();
		while (en.hasMoreElements()) {
			out.add(en.nextElement());
		}
		return out;
	}

	/**
	 * Get a list of RouteLocations sorted by route order
	 * 
	 * @return list of RouteLocations ordered by sequence
	 */
	public List<RouteLocation> getLocationsBySequenceList() {
		// now re-sort
		List<RouteLocation> out = new ArrayList<RouteLocation>();
		for (RouteLocation rl : getLocationsByIdList()) {
			for (int j = 0; j < out.size(); j++) {
				if (rl.getSequenceId() < out.get(j).getSequenceId()) {
					out.add(j, rl);
					break;
				}
			}
			if (!out.contains(rl)) {
				out.add(rl);
			}
		}
		return out;
	}

	/**
	 * Moves a RouteLocation earlier in the route by decrementing the sequenceId for the RouteLocation
	 * 
	 * @param rl
	 */
	public void moveLocationUp(RouteLocation rl) {
		int sequenceId = rl.getSequenceId();
		sequenceId--;
		if (sequenceId <= 0)
			return;
		rl.setSequenceId(sequenceId);
		int searchId = sequenceId;
		sequenceId++;
		// now find and adjust the other location taken by this one
		boolean found = false;
		List<RouteLocation> sortList = getLocationsByIdList();
		while (!found) {
			for (RouteLocation rladjust : sortList) {
				if (rladjust.getSequenceId() == searchId && rladjust != rl) {
					rladjust.setSequenceId(sequenceId);
					found = true;
					break;
				}
			}
			searchId--;
			if (searchId < 1)
				found = true;
		}
		setDirtyAndFirePropertyChange(LISTCHANGE_CHANGED_PROPERTY, null, Integer.toString(sequenceId));
	}

	/**
	 * Moves a RouteLocation later in the route by incrementing the sequenceId for the RouteLocation
	 * 
	 * @param rl
	 */
	public void moveLocationDown(RouteLocation rl) {
		int sequenceId = rl.getSequenceId();
		sequenceId++;
		if (sequenceId > _sequenceNum)
			return;
		rl.setSequenceId(sequenceId);
		int searchId = sequenceId;
		sequenceId--;
		// now find and adjust the other location taken by this one
		boolean found = false;
		List<RouteLocation> sortList = getLocationsByIdList();
		while (!found) {
			for (RouteLocation rladjust : sortList) {
				if (rladjust.getSequenceId() == searchId && rladjust != rl) {
					rladjust.setSequenceId(sequenceId);
					found = true;
					break;
				}
			}
			searchId++;
			if (searchId > _sequenceNum)
				found = true;
		}
		setDirtyAndFirePropertyChange(LISTCHANGE_CHANGED_PROPERTY, null, Integer.toString(sequenceId));
	}

	/**
	 * Gets the status of the route: OKAY ORPHAN ERROR
	 * 
	 * @return string with status of route.
	 */
	public String getStatus() {
		List<RouteLocation> routeList = getLocationsByIdList();
		if (routeList.size() == 0)
			return ERROR;
		for (RouteLocation rl : routeList) {
			if (rl.getName().equals(RouteLocation.DELETED))
				return ERROR;
		}
		// check to see if this route is used by a train
		for (Train train : TrainManager.instance().getTrainsByIdList()) {
			if (train.getRoute() == this)
				return OKAY;
		}
		return ORPHAN;
	}

	public JComboBox getComboBox() {
		JComboBox box = new JComboBox();
		for (RouteLocation rl : getLocationsBySequenceList())
			box.addItem(rl);
		return box;
	}

	public void updateComboBox(JComboBox box) {
		box.removeAllItems();
		box.addItem("");
		for (RouteLocation rl : getLocationsBySequenceList())
			box.addItem(rl);
	}

	/**
	 * Construct this Entry from XML. This member has to remain synchronized with the detailed DTD in
	 * operations-config.xml
	 * 
	 * @param e
	 *            Consist XML element
	 */
	public Route(Element e) {
		// if (log.isDebugEnabled()) log.debug("ctor from element "+e);
		Attribute a;
		if ((a = e.getAttribute(Xml.ID)) != null)
			_id = a.getValue();
		else
			log.warn("no id attribute in route element when reading operations");
		if ((a = e.getAttribute(Xml.NAME)) != null)
			_name = a.getValue();
		if ((a = e.getAttribute(Xml.COMMENT)) != null)
			_comment = a.getValue();
		if (e.getChildren(Xml.LOCATION) != null) {
			@SuppressWarnings("unchecked")
			List<Element> l = e.getChildren(Xml.LOCATION);
			if (log.isDebugEnabled())
				log.debug("route: " + getName() + " has " + l.size() + " locations");
			for (int i = 0; i < l.size(); i++) {
				register(new RouteLocation(l.get(i)));
			}
		}
	}

	/**
	 * Create an XML element to represent this Entry. This member has to remain synchronized with the detailed DTD in
	 * operations-config.xml.
	 * 
	 * @return Contents in a JDOM Element
	 */
	public Element store() {
		Element e = new Element(Xml.ROUTE);
		e.setAttribute(Xml.ID, getId());
		e.setAttribute(Xml.NAME, getName());
		e.setAttribute(Xml.COMMENT, getComment());
		for (RouteLocation rl : getLocationsBySequenceList()) {
			e.addContent(rl.store());
		}
		return e;
	}

	public void propertyChange(java.beans.PropertyChangeEvent e) {
		if (Control.showProperty && log.isDebugEnabled())
			log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
					.getNewValue());
		// forward drops, pick ups, train direction, and max moves as a list change
		if (e.getPropertyName().equals(RouteLocation.DROP_CHANGED_PROPERTY)
				|| e.getPropertyName().equals(RouteLocation.PICKUP_CHANGED_PROPERTY)
				|| e.getPropertyName().equals(RouteLocation.TRAIN_DIRECTION_CHANGED_PROPERTY)
				|| e.getPropertyName().equals(RouteLocation.MAXMOVES_CHANGED_PROPERTY)) {
			setDirtyAndFirePropertyChange(LISTCHANGE_CHANGED_PROPERTY, null, "RouteLocation"); // NOI18N
		}
	}

	java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);

	public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
		pcs.addPropertyChangeListener(l);
	}

	public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
		pcs.removePropertyChangeListener(l);
	}

	protected void setDirtyAndFirePropertyChange(String p, Object old, Object n) {
		RouteManagerXml.instance().setDirty(true);
		pcs.firePropertyChange(p, old, n);
	}

	static Logger log = LoggerFactory.getLogger(Route.class.getName());

}
