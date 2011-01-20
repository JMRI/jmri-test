package jmri.jmrit.operations.rollingstock.cars;

import java.beans.PropertyChangeEvent;
import java.util.List;

import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.ScheduleManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.locations.Schedule;
import jmri.jmrit.operations.locations.ScheduleItem;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.router.Router;

/**
 * Represents a car on the layout
 * 
 * @author Daniel Boudreau Copyright (C) 2008, 2009, 2010
 * @version             $Revision: 1.65 $
 */
public class Car extends RollingStock {
	
	CarLoads carLoads = CarLoads.instance();
	
	protected boolean _passenger = false;
	protected boolean _hazardous = false;
	protected boolean _caboose = false;
	protected boolean _fred = false;
	protected Kernel _kernel = null;
	protected String _load = carLoads.getDefaultEmptyName();
	protected int _wait = 0;
	
	protected Location _rweDestination = null;	// return when empty destination
	protected Track _rweDestTrack = null;		// return when empty track
	
	// schedule items
	protected String _nextLoad = "";			// next load by schedule	
	protected int _nextWait = 0;				// next wait by schedule
	protected Location _nextDestination = null;	// next destination by schedule
	protected Track _nextDestTrack = null;		// next track by schedule
		
	public static final String LOAD_CHANGED_PROPERTY = "Car load changed";  		// property change descriptions
	public static final String WAIT_CHANGED_PROPERTY = "Car wait changed";
	public static final String NEXTWAIT_CHANGED_PROPERTY = "Next wait changed";
	public static final String NEXT_DESTINATION_CHANGED_PROPERTY = "Next destination changed";
	public static final String NEXT_DESTINATION_TRACK_CHANGED_PROPERTY = "Next destination track changed";
	public static final String RETURN_WHEN_EMPTY_CHANGED_PROPERTY = "Return when empty changed";
	
	public Car(){
		
	}
	
	public Car(String road, String number) {
		super(road, number);
		log.debug("New car " + road + " " + number);
		addPropertyChangeListeners();
	}

	public void setHazardous(boolean hazardous){
		boolean old = _hazardous;
		_hazardous = hazardous;
		if (!old == hazardous)
			firePropertyChange("car hazardous", old?"true":"false", hazardous?"true":"false");
	}
	
	public boolean isHazardous(){
		return _hazardous;
	}
	
	public void setPassenger(boolean passenger){
		boolean old = _passenger;
		_passenger = passenger;
		if (!old == passenger)
			firePropertyChange("car passenger", old?"true":"false", passenger?"true":"false");
	}
	
	public boolean isPassenger(){
		return _passenger;
	}
	
	public void setFred(boolean fred){
		boolean old = _fred;
		_fred = fred;
		if (!old == fred)
			firePropertyChange("car fred", old?"true":"false", fred?"true":"false");
	}
	
	public boolean hasFred(){
		return _fred;
	}
	
	public void setLoad(String load){
		String old = _load;
		_load = load;
		if (!old.equals(load))
			firePropertyChange(LOAD_CHANGED_PROPERTY, old, load);
	}
	
	public String getLoad(){
		return _load;
	}
	
	public void setNextLoad(String load){
		String old = _nextLoad;
		_nextLoad = load;
		if (!old.equals(load))
			firePropertyChange(LOAD_CHANGED_PROPERTY, old, load);
	}
	
	public String getNextLoad(){
		return _nextLoad;
	}
	
	public String getPriority(){
		return (carLoads.getPriority(_type, _load));
	}
	
	public int getAdjustedWeightTons(){
		int weightTons =0;
		try {
			// get loaded weight
			weightTons = Integer.parseInt(getWeightTons());
			// adjust for empty weight if car is empty, 1/3 of loaded weight
			if (!isCaboose() && !isPassenger() && getLoad().equals(CarLoads.instance().getDefaultEmptyName()))
				weightTons = weightTons / 3;
		} catch (Exception e){
			log.debug ("Car ("+toString()+") weight not set");
		}
		return weightTons;
	}
	
	public void setWait(int count){
		int old = _wait;
		_wait = count;
		if (old != count)
			firePropertyChange(NEXTWAIT_CHANGED_PROPERTY, old, count);
	}
	
	public int getWait(){
		return _wait;
	}
	
	public void setNextWait(int count){
		int old = _nextWait;
		_nextWait = count;
		if (old != count)
			firePropertyChange(NEXTWAIT_CHANGED_PROPERTY, old, count);
	}
	
	public int getNextWait(){
		return _nextWait;
	}
	
	public void setNextDestination(Location destination){
		Location old = _nextDestination;
		if (old != null)
			old.removePropertyChangeListener(this);
		_nextDestination = destination;
		if (_nextDestination != null)
			_nextDestination.addPropertyChangeListener(this);
		log.debug("Next destination for car ("+toString()+") old: "+old+" new: "+destination);
		if ((old != null && !old.equals(destination)) || (destination != null && !destination.equals(old)))
			firePropertyChange(NEXT_DESTINATION_CHANGED_PROPERTY, old, destination);
	}
	
	public Location getNextDestination(){
		return _nextDestination;
	}
	
	public String getNextDestinationName() {
		if (_nextDestination != null)
			return _nextDestination.getName();
		return "";
	}
	
	public void setNextDestTrack(Track track){
		Track old = _nextDestTrack;
		if (old != null)
			old.removePropertyChangeListener(this);
		_nextDestTrack = track;
		if (_nextDestTrack != null)
			_nextDestTrack.addPropertyChangeListener(this);
		if ((old != null && !old.equals(track)) || (track != null && !track.equals(old)))
			firePropertyChange(NEXT_DESTINATION_TRACK_CHANGED_PROPERTY, old, track);
	}
	
	public Track getNextDestTrack(){
		return _nextDestTrack;
	}
	
	public String getNextDestTrackName(){
		if (_nextDestTrack != null)
			return _nextDestTrack.getName();
		return "";			
	}
	
	public void setReturnWhenEmptyDestination(Location destination){
		Location old = _rweDestination;
		_rweDestination = destination;
		if ((old != null && !old.equals(destination)) || (destination != null && !destination.equals(old)))
			firePropertyChange(RETURN_WHEN_EMPTY_CHANGED_PROPERTY, null, null);
	}
	
	public Location getReturnWhenEmptyDestination(){
		return _rweDestination;
	}
	
	public void setReturnWhenEmptyDestTrack(Track track){
		Track old = _rweDestTrack;
		_rweDestTrack = track;
		if ((old != null && !old.equals(track)) || (track != null && !track.equals(old)))
			firePropertyChange(RETURN_WHEN_EMPTY_CHANGED_PROPERTY, null, null);

	}
	
	public Track getReturnWhenEmptyDestTrack(){
		return _rweDestTrack;
	}
	
	public String getReturnWhenEmptyDestName(){
		if (getReturnWhenEmptyDestination() != null && getReturnWhenEmptyDestTrack() != null)
			return getReturnWhenEmptyDestination().getName()+"("+getReturnWhenEmptyDestTrack().getName()+")";
		else if (getReturnWhenEmptyDestination() != null)
			return getReturnWhenEmptyDestination().getName()+"()";
		else
			return "";
	}
	
	public void setCaboose(boolean caboose){
		boolean old = _caboose;
		_caboose = caboose;
		if (!old == caboose)
			firePropertyChange("car caboose", old?"true":"false", caboose?"true":"false");
	}
	
	public boolean isCaboose(){
		return _caboose;
	}
	
	/**
	 * A kernel is a group of cars that are switched as
	 * a unit. 
	 * @param kernel
	 */
	public void setKernel(Kernel kernel) {
		if (_kernel == kernel)
			return;
		String old ="";
		if (_kernel != null){
			old = _kernel.getName();
			_kernel.delete(this);
		}
		_kernel = kernel;
		String newName ="";
		if (_kernel != null){
			_kernel.add(this);
			newName = _kernel.getName();
		}
		if (!old.equals(newName))
			firePropertyChange("kernel", old, newName);
	}

	public Kernel getKernel() {
		return _kernel;
	}
	
	public String getKernelName() {
		if (_kernel != null)
			return _kernel.getName();
		return "";
	}
	
	public String testLocation(Location location, Track track) {
		String status = super.testLocation(location, track);
		if (!status.equals(OKAY))
			return status;
		if (location != null && track != null && !track.acceptsLoadName(getLoad())){
			log.debug("Can't set (" + toString() + ") load (" +getLoad()+ ") at location ("+ location.getName() + ", " + track.getName() + ") wrong load");
			return LOAD+ " ("+getLoad()+")";
		}
		return OKAY;
	}
	
	public String testDestination(Location destination, Track track) {
		String status = super.testDestination(destination, track);
		if (!status.equals(OKAY))
			return status;
		if (destination != null && track != null && !track.acceptsLoadName(getLoad())){
			log.debug("Can't set (" + toString() + ") load (" +getLoad()+ ") at destination ("+ destination.getName() + ", " + track.getName() + ") wrong load");
			return LOAD+ " ("+getLoad()+")";
		}
		// does car already have this destination?
		if (destination == getDestination() && track == getDestinationTrack())
			return OKAY;
		// is car returning to same track?
		if (track == getTrack())
			return OKAY;
		// now check to see if car is in a kernel and can fit 
		if (getKernel() != null && destination != null && track != null &&
				track.getUsedLength() + track.getReserved()+ getKernel().getLength() > track.getLength()){
			log.debug("Can't set car (" + getId() + ") in kernel ("+getKernel().getName()+") at track destination ("+ destination.getName() + ", " + track.getName() + ") no room!");
			return LENGTH+ " kernel ("+getKernel().getLength()+")";	
		}
		// now check to see if the track has a schedule
		return testSchedule(track);
	}
	
	private String testSchedule(Track track){
		if (track == null)
			return SCHEDULE +" track is null";
		if (track.getScheduleId().equals("")){
			// does car have a scheduled load?
			if (getLoad().equals(carLoads.getDefaultEmptyName()) || getLoad().equals(carLoads.getDefaultLoadName()))
				return OKAY; //no
			// can't place a car with a scheduled load at a siding
			else if (!track.getLocType().equals(Track.SIDING))
				return OKAY;
			else
				return "Car has a " +SCHEDULE+ " " +LOAD+ " ("+getLoad()+")";
		}
		// only sidings can have a schedule
		if (!track.getLocType().equals(Track.SIDING))
			return OKAY;
		log.debug("Track ("+track.getName()+") has schedule ("+track.getScheduleName()+")");
		ScheduleManager scheduleManager = ScheduleManager.instance();
		Schedule sch = scheduleManager.getScheduleById(track.getScheduleId());
		if (sch == null){
			log.warn("Could not find schedule ("+track.getScheduleId()+") for track ("+track.getName()+")");
			return OKAY;
		}
		ScheduleItem si = sch.getItemById(track.getScheduleItemId());
		if (si == null){
			log.warn("Could not find schedule item id ("+track.getScheduleItemId()+") for schedule ("+track.getScheduleName()+")");
			// reset and get first schedule item on the list
			si = sch.getItemById(sch.getItemsBySequenceList().get(0));
			track.setScheduleItemId(si.getId());
		}
		if (getType().equals(si.getType())) {
			if (si.getRoad().equals("") || getRoad().equals(si.getRoad()))
				if (si.getLoad().equals("") || getLoad().equals(si.getLoad()))
					return OKAY;
				else
					return SCHEDULE + " (" + track.getScheduleName()
							+ ") request car "+TYPE+" (" + si.getType()
							+ ") "+ROAD+" (" + si.getRoad() + ") "+LOAD+" ("
							+ si.getLoad() + ")";
			else
				return SCHEDULE + " (" + track.getScheduleName()
						+ ") request car "+TYPE+" (" + si.getType()
						+ ") "+ROAD+" (" + si.getRoad() + ")";
		} else
			return SCHEDULE + " (" + track.getScheduleName()
					+ ") request car "+TYPE+" (" + si.getType() + ")";
	}
	
	/**
	 * Sets the car's destination on the layout
	 * @param destination 
	 * @param track (yard, siding, staging, or interchange track)
	 * @return "okay" if successful, "type" if the rolling stock's type isn't 
	 * acceptable, or "length" if the rolling stock length didn't fit, or 
	 * Schedule if the destination will not accept the car because the siding
	 * has a schedule and the car doesn't meet the schedule requirements.
	 * Also changes the car load status when the car reaches its destination.
	 */
	public String setDestination(Location destination, Track track) {
		return setDestination(destination, track, false);
	}
	
	/**
	 * Sets the car's destination on the layout
	 * @param destination 
	 * @param track (yard, siding, staging, or interchange track)
	 * @param force when true ignore track length, type, & road when setting destination
	 * @return "okay" if successful, "type" if the rolling stock's type isn't 
	 * acceptable, or "length" if the rolling stock length didn't fit, or 
	 * Schedule if the destination will not accept the car because the siding
	 * has a schedule and the car doesn't meet the schedule requirements.
	 * Also changes the car load status when the car reaches its destination.
	 */
	public String setDestination(Location destination, Track track, boolean force) {
		// save destination name and track in case car has reached its destination
		String destinationName = getDestinationName();
		Track oldDestTrack = getDestinationTrack();
		String status = super.setDestination(destination, track, force);
		// return if not Okay 
		if (!status.equals(OKAY))
			return status;
		// now check to see if the track has a schedule
		if (oldDestTrack != track)
			scheduleNext(track);
		// update next destination and load only when car reaches destination and was in train
		if (destinationName.equals("") || (destination != null) || getTrain() == null)
			return status;
		// update load when car reaches a siding
		loadNext(oldDestTrack);
		// set destination and destination track if available
		if (!Router.instance().setDestination(this))
			super.setDestination(null, null);	// couldn't route car, maybe next time
		return status;
	}
	

	
	/**
	 * Check to see if track has schedule and if it does will schedule the next
	 * item in the list.  Load the car with the next schedule load if one exists.
	 * 
	 * @param track
	 */
	private void scheduleNext(Track track){
		if (getDestination() != null && getDestination().equals(getNextDestination())
				&& getDestinationTrack() != null && getDestinationTrack().equals(getNextDestTrack())){
			setNextDestination(null);
			setNextDestTrack(null);
		}
		if (track == null || track.getScheduleId().equals("") || loading)
			return;
		ScheduleItem currentSi = track.getCurrentScheduleItem();
		log.debug("Destination track ("+track.getName()+") has schedule ("+track.getScheduleName()+") item id: "+currentSi.getId());
		// is car part of a kernel?
		if (getKernel()!=null && !getKernel().isLead(this)){
			log.debug("Car ("+toString()+") is part of kernel ("+getKernelName()+")");
			return;
		}
		if (getType().equals(currentSi.getType())){
			// set the car's next load
			setNextLoad(currentSi.getShip());
			// set the car's next destination and track
			setNextDestination(currentSi.getDestination());
			setNextDestTrack(currentSi.getDestinationTrack());
			// set the wait count
			setNextWait(currentSi.getWait());

			log.debug("Car ("+toString()+") type ("+getType()+") next load ("+getNextLoad()+") next destination ("+getNextDestinationName()+", "+getNextDestTrackName()+") next wait: "+getWait());

			// set all cars in kernel to the next load
			if (getKernel() != null){
				List<Car> cars = getKernel().getCars();
				for (int i=0; i<cars.size(); i++){
					Car c = cars.get(i);
					if (c.getType().equals(getType())
							|| getNextLoad().equals(CarLoads.instance().getDefaultEmptyName())
							|| getNextLoad().equals(CarLoads.instance().getDefaultLoadName()))
						c.setNextLoad(currentSi.getShip());
				}
			}		
			// bump schedule
			track.bumpSchedule();
		} else {
			log.debug("Car ("+toString()+") type ("+getType()+") arrived out of sequence, current type needed ("+currentSi.getType()+")");
		}
	}
	
	private void loadNext(Track destTrack){
		if (destTrack.getLocType().equals(Track.SIDING)){
			// update wait count
			setWait(getNextWait());
			setNextWait(0);
			if (!getNextLoad().equals("")){
				setLoad(getNextLoad());
				setNextLoad("");
				// is the next load default empty?  Check for car return when empty
				if (getLoad().equals(carLoads.getDefaultEmptyName()))
					setLoadEmpty();
				return;
			}
			// car doesn't have a schedule load, flip load status
			if (getLoad().equals(carLoads.getDefaultEmptyName()))
				setLoad(carLoads.getDefaultLoadName());
			else
				setLoadEmpty();
		}
		// update load optionally when car reaches staging
		if (destTrack.getLocType().equals(Track.STAGING)){
			if (destTrack.isLoadSwapEnabled()){
				if (getLoad().equals(carLoads.getDefaultEmptyName())){
					setLoad(carLoads.getDefaultLoadName());
				} else if (getLoad().equals(carLoads.getDefaultLoadName())){
					setLoadEmpty();
				}
			}
			// empty car if it has a schedule load
			if (destTrack.isRemoveLoadsEnabled() 
					&& !getLoad().equals(carLoads.getDefaultEmptyName()) 
					&& !getLoad().equals(carLoads.getDefaultLoadName()))
				setLoadEmpty();
		}
	}
	
	private void setLoadEmpty(){
		setLoad(carLoads.getDefaultEmptyName());
		if (getReturnWhenEmptyDestination() != null){
			setNextDestination(getReturnWhenEmptyDestination());
			if (getReturnWhenEmptyDestTrack() != null){
				setNextDestTrack(getReturnWhenEmptyDestTrack());
				log.debug("Car ("+toString()+") has return when empty destination ("+getNextDestinationName()+", "+getNextDestTrackName()+")");
			}
		}
	}
	
	public void dispose(){
		setKernel(null);
		setNextDestination(null);	// removes property change listener
		setNextDestTrack(null);		// removes property change listener
		CarTypes.instance().removePropertyChangeListener(this);
		CarLengths.instance().removePropertyChangeListener(this);
		super.dispose();
	}
	
	// used to stop a track's schedule from bumping when loading car database
	private boolean loading = false;

	/**
	 * Construct this Entry from XML. This member has to remain synchronized
	 * with the detailed DTD in operations-cars.dtd
	 * 
	 * @param e  Car XML element
	 */
	public Car(org.jdom.Element e) {
		loading = true;	// stop track schedule from bumping
		super.rollingStock(e);
		loading = false;
		org.jdom.Attribute a;
		if ((a = e.getAttribute("passenger")) != null)
			_passenger = a.getValue().equals("true");
		if ((a = e.getAttribute("hazardous")) != null)
			_hazardous = a.getValue().equals("true");
		if ((a = e.getAttribute("caboose")) != null)
			_caboose = a.getValue().equals("true");
		if ((a = e.getAttribute("fred")) != null)
			_fred = a.getValue().equals("true");
		if ((a = e.getAttribute("locUnknown")) != null)
			_locationUnknown = a.getValue().equals("true");
		if ((a = e.getAttribute("kernel")) != null){
			Kernel k = CarManager.instance().getKernelByName(a.getValue());
			if (k != null){
				setKernel(k);
				if ((a = e.getAttribute("leadKernel")) != null && a.getValue().equals("true")){
					_kernel.setLead(this);
				}
			} else {
				log.error("Kernel "+a.getValue()+" does not exist");
			}
		}
		if ((a = e.getAttribute("load")) != null){
			_load = a.getValue();
		}

		if ((a = e.getAttribute("wait")) != null){
			_wait = Integer.parseInt(a.getValue());
		}
		if ((a = e.getAttribute("nextLoad")) != null){
			_nextLoad = a.getValue();
		}

		if ((a = e.getAttribute("nextWait")) != null){
			_nextWait = Integer.parseInt(a.getValue());
		}
		if ((a = e.getAttribute("nextDestId")) != null){
			setNextDestination(LocationManager.instance().getLocationById(a.getValue()));
		}
		if (getNextDestination() != null && (a = e.getAttribute("nextDestTrackId")) != null){
			setNextDestTrack(getNextDestination().getTrackById(a.getValue()));
		}
		if ((a = e.getAttribute("rweDestId")) != null){
			_rweDestination = LocationManager.instance().getLocationById(a.getValue());
		}
		if (_rweDestination != null && (a = e.getAttribute("rweDestTrackId")) != null){
			_rweDestTrack = _rweDestination.getTrackById(a.getValue());
		}
		addPropertyChangeListeners();
	}
	
	/**
	 * Create an XML element to represent this Entry. This member has to remain
	 * synchronized with the detailed DTD in operations-cars.dtd.
	 * 
	 * @return Contents in a JDOM Element
	 */
	public org.jdom.Element store() {
		org.jdom.Element e = new org.jdom.Element("car");
		super.store(e);
		if (isPassenger())
			e.setAttribute("passenger", isPassenger()?"true":"false");
		if (isHazardous())
			e.setAttribute("hazardous", isHazardous()?"true":"false");
		if (isCaboose())
			e.setAttribute("caboose", isCaboose()?"true":"false");
		if (hasFred())
			e.setAttribute("fred", hasFred()?"true":"false");
		if (getKernel() != null){
			e.setAttribute("kernel", getKernelName());
			if (getKernel().isLead(this))
				e.setAttribute("leadKernel", "true");
		}
		if (!getLoad().equals("")){
			e.setAttribute("load", getLoad());
		}

		if (getWait() != 0){
			e.setAttribute("wait", Integer.toString(getWait()));
		}
		if (!getNextLoad().equals("")){
			e.setAttribute("nextLoad", getNextLoad());
		}

		if (getNextWait() != 0){
			e.setAttribute("nextWait", Integer.toString(getNextWait()));
		}
		if (getNextDestination() != null){
			e.setAttribute("nextDestId", getNextDestination().getId());
			if (getNextDestTrack() != null)
				e.setAttribute("nextDestTrackId", getNextDestTrack().getId());
		}
		if (getReturnWhenEmptyDestination() != null){
			e.setAttribute("rweDestId", getReturnWhenEmptyDestination().getId());
			if (getReturnWhenEmptyDestTrack() != null)
				e.setAttribute("rweDestTrackId", getReturnWhenEmptyDestTrack().getId());
		}
		return e;
	}
	
	private void addPropertyChangeListeners(){
		CarTypes.instance().addPropertyChangeListener(this);
		CarLengths.instance().addPropertyChangeListener(this);
	}
	
    public void propertyChange(PropertyChangeEvent e) {
    	super.propertyChange(e);
       	if (e.getPropertyName().equals(CarTypes.CARTYPES_NAME_CHANGED_PROPERTY)){
    		if (e.getOldValue().equals(getType())){
    			if (log.isDebugEnabled()) log.debug("Car (" +toString()+") sees type name change old: "+e.getOldValue()+" new: "+e.getNewValue());
    			setType((String)e.getNewValue());
    		}
    	}
       	if (e.getPropertyName().equals(CarLengths.CARLENGTHS_NAME_CHANGED_PROPERTY)){
    		if (e.getOldValue().equals(getLength())){
    			if (log.isDebugEnabled()) log.debug("Car (" +toString()+") sees length name change old: "+e.getOldValue()+" new: "+e.getNewValue());
    			setLength((String)e.getNewValue());
    		}
    	}
    }

	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(Car.class.getName());

}
