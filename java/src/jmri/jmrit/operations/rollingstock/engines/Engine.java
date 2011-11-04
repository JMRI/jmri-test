package jmri.jmrit.operations.rollingstock.engines;

import java.beans.PropertyChangeEvent;

import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.routes.RouteLocation;

/**
 * Represents an engine on the layout
 * 
 * @author Daniel Boudreau (C) Copyright 2008
 * @version $Revision$
 */
public class Engine extends RollingStock {
	
	private Consist _consist = null;
	private String _model = "";
	
	EngineModels engineModels = EngineModels.instance();
	
	public Engine(String road, String number) {
		super(road, number);
		log.debug("New engine " + road + " " + number);
		addPropertyChangeListeners();
	}
	
	/**
	 * Set the engine's model. Note a model has only one length, type, and
	 * horsepower rating.
	 * 
	 * @param model
	 */
	public void setModel (String model){
		String old = _model;
		_model = model;
		if (!old.equals(model))
			firePropertyChange("engine model", old, model);
	}
	
	public String getModel(){
		return _model;
	}
	
	/**
	 * Set the engine type for this engine's model
	 * @param type Engine type: Steam, Diesel, Gas Turbine, etc.
	 */
	public void setType (String type){
		if(getModel().equals(""))
			return;
		String old = getType();
		engineModels.setModelType(getModel(), type);
		if (!old.equals(type))
			firePropertyChange(TYPE_CHANGED_PROPERTY, old, type);	
	}
	
	public String getType(){
		String type = engineModels.getModelType(getModel());
		if(type == null)
			type = super.getType();
		return type;
	}
	
	/**
	 * Set the engine horsepower rating for this engine's model
	 * @param hp engine horsepower
	 */
	public void setHp (String hp){
		if(getModel().equals(""))
			return;
		String old = getHp();
		engineModels.setModelHorsepower(getModel(), hp);
		if (!old.equals(hp))
			firePropertyChange("hp", old, hp);
	}
	
	public String getHp(){
		String hp = engineModels.getModelHorsepower(getModel());
		if(hp == null)
			hp = "";
		return hp;
	}
	
	/**
	 * Set the engine length for this engine's model
	 * @param length engine length
	 */
	public void setLength(String length){
		if(getModel().equals(""))
			return;
		String old = getLength();
		engineModels.setModelLength(getModel(), length);
		if (!old.equals(length))
			firePropertyChange(LENGTH_CHANGED_PROPERTY, old, length);
	}
	
	public String getLength(){
		String length = engineModels.getModelLength(getModel());
		if(length == null)
			length = "";
		return length;
	}
	
	/**
	 * Set the engine weight for this engine's model
	 * @param weight engine weight
	 */
	public void setWeightTons(String weight){
		if(getModel().equals(""))
			return;
		String old = getWeight();
		engineModels.setModelWeight(getModel(), weight);
		if (!old.equals(weight))
			firePropertyChange(LENGTH_CHANGED_PROPERTY, old, weight);
	}
	
	public String getWeightTons(){
		String weight = engineModels.getModelWeight(getModel());
		if(weight == null)
			weight = "";
		return weight;
	}
	
	/**
	 * Place engine in a consist
	 * @param consist
	 */
	public void setConsist(Consist consist) {
		if (_consist == consist)
			return;
		String old ="";
		if (_consist != null){
			old = _consist.getName();
			_consist.delete(this);
		}
		_consist = consist;
		String newName ="";
		if (_consist != null){
			_consist.add(this);
			newName = _consist.getName();
		}
		
		if (!old.equals(newName))
			firePropertyChange("consist", old, newName);
	}

	/**
	 * Get the consist for this engine
	 * @return null if engine isn't in a consist
	 */
	public Consist getConsist() {
		return _consist;
	}
	
	public String getConsistName() {
		if (_consist != null)
			return _consist.getName();
		return "";
	}

	/**
	 * Used to check destination track to see if it will accept engine
	 * @return status, see RollingStock.java
	 */
	public String testDestination(Location destination, Track track) {
		return super.testDestination(destination, track);
	}
	
	protected void moveRollingStock(RouteLocation old, RouteLocation next){
		if(old == getRouteLocation()){
			if (getConsist() == null || (getConsist() != null && getConsist().isLead(this))){
				if (getTrain() != null && getRouteLocation() != getRouteDestination() && getTrain().getLeadEngine() != this){
					log.debug("New lead engine ("+toString()+") for train " + getTrain().getName());
					getTrain().setLeadEngine(this);
					getTrain().createTrainIcon();
				}
			}
		}
		super.moveRollingStock(old, next);
	}
	
	public void dispose(){
		setConsist(null);
		EngineTypes.instance().removePropertyChangeListener(this);
		EngineLengths.instance().removePropertyChangeListener(this);
		super.dispose();
	}
	
	/**
	 * Construct this Entry from XML. This member has to remain synchronized
	 * with the detailed DTD in operations-engines.dtd
	 * 
	 * @param e
	 *            Engine XML element
	 */
	public Engine(org.jdom.Element e) {
		org.jdom.Attribute a;
		// must set _model first so engine hp, length, type and weight is set properly
		if ((a = e.getAttribute("model")) != null)
			_model = a.getValue();
		if ((a = e.getAttribute("hp")) != null)
			setHp(a.getValue());
		if ((a = e.getAttribute("length")) != null)
			setLength(a.getValue());
		if ((a = e.getAttribute("type")) != null)
			setType(a.getValue());
		if ((a = e.getAttribute("weightTons")) != null)
			setWeightTons(a.getValue());
		if ((a = e.getAttribute("consist")) != null){
			Consist c = EngineManager.instance().getConsistByName(a.getValue());
			if (c != null){
				setConsist(c);
				if ((a = e.getAttribute("leadConsist")) != null && a.getValue().equals("true")){
					_consist.setLead(this);
				}
				if ((a = e.getAttribute("consistNum")) != null){
					_consist.setConsistNumber(Integer.parseInt(a.getValue()));
				}
			} else {
				log.error("Consist "+a.getValue()+" does not exist");
			}
		}
		super.rollingStock(e); 
		addPropertyChangeListeners();
	}
	
	boolean verboseStore = false;

	/**
	 * Create an XML element to represent this Entry. This member has to remain
	 * synchronized with the detailed DTD in operations-engines.dtd.
	 * 
	 * @return Contents in a JDOM Element
	 */
	public org.jdom.Element store() {
		org.jdom.Element e = new org.jdom.Element("engine");
		super.store(e);
		e.setAttribute("model", getModel());
		e.setAttribute("hp", getHp());
		if (getConsist() != null){
			e.setAttribute("consist", getConsistName());
			if (getConsist().isLead(this)){
				e.setAttribute("leadConsist", "true");
				if (getConsist().getConsistNumber()>0)
					e.setAttribute("consistNum", Integer.toString(getConsist().getConsistNumber()));
			}
		}
		return e;
	}
	
	private void addPropertyChangeListeners(){
		EngineTypes.instance().addPropertyChangeListener(this);
		EngineLengths.instance().addPropertyChangeListener(this);
	}
	
    public void propertyChange(PropertyChangeEvent e) {
    	super.propertyChange(e);
       	if (e.getPropertyName().equals(EngineTypes.ENGINETYPES_NAME_CHANGED_PROPERTY)){
    		if (e.getOldValue().equals(getType())){
    			if (log.isDebugEnabled()) log.debug("Engine (" +toString()+") sees type name change old: "+e.getOldValue()+" new: "+e.getNewValue());
    			setType((String)e.getNewValue());
    		}
    	}
       	if (e.getPropertyName().equals(EngineLengths.ENGINELENGTHS_NAME_CHANGED_PROPERTY)){
    		if (e.getOldValue().equals(getLength())){
    			if (log.isDebugEnabled()) log.debug("Engine (" +toString()+") sees length name change old: "+e.getOldValue()+" new: "+e.getNewValue());
    			setLength((String)e.getNewValue());
    		}
    	}
    }
	
	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(Engine.class.getName());

}
