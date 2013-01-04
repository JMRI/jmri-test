// TrainManager.java

package jmri.jmrit.operations.trains;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JComboBox;

import org.jdom.Element;

import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.OperationsSetupXml;

/**
 * Manages trains.
 * 
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Daniel Boudreau Copyright (C) 2008, 2009, 2010, 2011, 2012
 * @version $Revision$
 */
public class TrainManager implements java.beans.PropertyChangeListener {

	// Train frame attributes
	private String _trainAction = TrainsTableFrame.MOVE; // Trains frame table button action
	private boolean _buildMessages = true; // when true, show build messages
	private boolean _buildReport = false; // when true, print/preview build reports
	private boolean _printPreview = false; // when true, preview train manifest
	private boolean _openFile = false; // when true, open CSV file manifest

	// Train frame table column widths (12), starts with Time column and ends with Edit
	private int[] _tableColumnWidths = { 50, 50, 72, 100, 140, 120, 120, 120, 120, 120, 90, 70 };

	private int[] _tableScheduleColumnWidths = { 50, 70, 120 };
	private String _trainScheduleActiveId = "";

	// Scripts
	protected List<String> _startUpScripts = new ArrayList<String>(); // list of script pathnames to run at start up
	protected List<String> _shutDownScripts = new ArrayList<String>(); // list of script pathnames to run at shut down

	// property changes
	public static final String LISTLENGTH_CHANGED_PROPERTY = "TrainsListLength";
	public static final String PRINTPREVIEW_CHANGED_PROPERTY = "TrainsPrintPreview";
	public static final String OPEN_FILE_CHANGED_PROPERTY = "TrainsOpenFile";
	public static final String TRAIN_ACTION_CHANGED_PROPERTY = "TrainsAction";
	public static final String ACTIVE_TRAIN_SCHEDULE_ID = "ActiveTrainScheduleId";

	public TrainManager() {
	}

	/** record the single instance **/
	private static TrainManager _instance = null;
	private int _id = 0; // train ids

	public static synchronized TrainManager instance() {
		if (_instance == null) {
			if (log.isDebugEnabled())
				log.debug("TrainManager creating instance");
			// create and load
			_instance = new TrainManager();
			OperationsSetupXml.instance(); // load setup
			TrainManagerXml.instance(); // load trains
		}
		if (Control.showInstance && log.isDebugEnabled())
			log.debug("TrainManager returns instance " + _instance);
		return _instance;
	}

	/**
	 * 
	 * @return true if build messages are enabled
	 */
	public boolean isBuildMessagesEnabled() {
		return _buildMessages;
	}

	public void setBuildMessagesEnabled(boolean enable) {
		boolean old = _buildMessages;
		_buildMessages = enable;
		firePropertyChange("BuildMessagesEnabled", enable, old);
	}

	/**
	 * 
	 * @return true if build reports are enabled
	 */
	public boolean isBuildReportEnabled() {
		return _buildReport;
	}

	public void setBuildReportEnabled(boolean enable) {
		boolean old = _buildReport;
		_buildReport = enable;
		firePropertyChange("BuildReportEnabled", enable, old);
	}

	/**
	 * 
	 * @return true if print preview is enabled
	 */
	public boolean isOpenFileEnabled() {
		return _openFile;
	}

	public void setOpenFileEnabled(boolean enable) {
		boolean old = _openFile;
		_openFile = enable;
		firePropertyChange(OPEN_FILE_CHANGED_PROPERTY, old ? "true" : "false", enable ? "true"
				: "false");
	}

	/**
	 * 
	 * @return true if print preview is enabled
	 */
	public boolean isPrintPreviewEnabled() {
		return _printPreview;
	}

	public void setPrintPreviewEnabled(boolean enable) {
		boolean old = _printPreview;
		_printPreview = enable;
		firePropertyChange(PRINTPREVIEW_CHANGED_PROPERTY, old ? "Preview" : "Print",
				enable ? "Preview" : "Print");
	}

	public String getTrainsFrameTrainAction() {
		return _trainAction;
	}

	public void setTrainsFrameTrainAction(String action) {
		String old = _trainAction;
		_trainAction = action;
		if (!old.equals(action))
			firePropertyChange(TRAIN_ACTION_CHANGED_PROPERTY, old, action);
	}

	/**
	 * 
	 * @return get an array of table column widths for the trains frame
	 */
	public int[] getTrainsFrameTableColumnWidths() {
		return _tableColumnWidths.clone();
	}

	public int[] getTrainScheduleFrameTableColumnWidths() {
		return _tableScheduleColumnWidths.clone();
	}

	/**
	 * Sets the selected schedule id
	 * 
	 * @param id
	 *            Selected schedule id
	 */
	public void setTrainSecheduleActiveId(String id) {
		String old = _trainScheduleActiveId;
		_trainScheduleActiveId = id;
		if (!old.equals(id))
			firePropertyChange(ACTIVE_TRAIN_SCHEDULE_ID, old, id);
	}

	public String getTrainScheduleActiveId() {
		return _trainScheduleActiveId;
	}

	/**
	 * Add a script to run after trains have been loaded
	 * 
	 * @param pathname
	 *            The script's pathname
	 */
	public void addStartUpScript(String pathname) {
		_startUpScripts.add(pathname);
		firePropertyChange("addStartUpScript", pathname, null);
	}

	public void deleteStartUpScript(String pathname) {
		_startUpScripts.remove(pathname);
		firePropertyChange("deleteStartUpScript", null, pathname);
	}

	/**
	 * Gets a list of pathnames to run after trains have been loaded
	 * 
	 * @return A list of pathnames to run after trains have been loaded
	 */
	public List<String> getStartUpScripts() {
		return _startUpScripts;
	}

	public void runStartUpScripts() {
		List<String> scripts = getStartUpScripts();
		for (int i = 0; i < scripts.size(); i++) {
			jmri.util.PythonInterp.runScript(jmri.util.FileUtil
					.getExternalFilename(getStartUpScripts().get(i)));
		}
	}

	/**
	 * Add a script to run at shutdown
	 * 
	 * @param pathname
	 *            The script's pathname
	 */
	public void addShutDownScript(String pathname) {
		_shutDownScripts.add(pathname);
		firePropertyChange("addShutDownScript", pathname, null);
	}

	public void deleteShutDownScript(String pathname) {
		_shutDownScripts.remove(pathname);
		firePropertyChange("deleteShutDownScript", null, pathname);
	}

	/**
	 * Gets a list of pathnames to run at shutdown
	 * 
	 * @return A list of pathnames to run at shutdown
	 */
	public List<String> getShutDownScripts() {
		return _shutDownScripts;
	}

	public void runShutDownScripts() {
		List<String> scripts = getShutDownScripts();
		for (int i = 0; i < scripts.size(); i++) {
			jmri.util.PythonInterp.runScript(jmri.util.FileUtil
					.getExternalFilename(getShutDownScripts().get(i)));
		}
	}

	public void dispose() {
		_trainHashTable.clear();
		_id = 0;
	}

	// stores known Train instances by id
	private Hashtable<String, Train> _trainHashTable = new Hashtable<String, Train>();

	/**
	 * @return requested Train object or null if none exists
	 */

	public Train getTrainByName(String name) {
		if (!TrainManagerXml.instance().isTrainFileLoaded())
			log.error("TrainManager getTrainByName called before trains completely loaded!");
		Train train;
		Enumeration<Train> en = _trainHashTable.elements();
		for (int i = 0; i < _trainHashTable.size(); i++) {
			train = en.nextElement();
			// windows file names are case independent
			if (train.getName().toLowerCase().equals(name.toLowerCase()))
				return train;
		}
		log.debug("train " + name + " doesn't exist");
		return null;
	}

	public Train getTrainById(String id) {
		if (!TrainManagerXml.instance().isTrainFileLoaded())
			log.error("TrainManager getTrainById called before trains completely loaded!");
		return _trainHashTable.get(id);
	}

	/**
	 * Finds an existing train or creates a new train if needed requires train's name creates a unique id for this train
	 * 
	 * @param name
	 * 
	 * @return new train or existing train
	 */
	public Train newTrain(String name) {
		Train train = getTrainByName(name);
		if (train == null) {
			_id++;
			train = new Train(Integer.toString(_id), name);
			Integer oldSize = Integer.valueOf(_trainHashTable.size());
			_trainHashTable.put(train.getId(), train);
			firePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize,
					Integer.valueOf(_trainHashTable.size()));
		}
		return train;
	}

	/**
	 * Remember a NamedBean Object created outside the manager.
	 */
	public void register(Train train) {
		Integer oldSize = Integer.valueOf(_trainHashTable.size());
		_trainHashTable.put(train.getId(), train);
		// find last id created
		int id = Integer.parseInt(train.getId());
		if (id > _id)
			_id = id;
		firePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize,
				Integer.valueOf(_trainHashTable.size()));
		// listen for name and state changes to forward
	}

	/**
	 * Forget a NamedBean Object created outside the manager.
	 */
	public void deregister(Train train) {
		if (train == null)
			return;
		train.dispose();
		Integer oldSize = Integer.valueOf(_trainHashTable.size());
		_trainHashTable.remove(train.getId());
		firePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize,
				Integer.valueOf(_trainHashTable.size()));
	}

	public void replaceLoad(String oldLoadName, String newLoadName) {
		List<String> trains = getTrainsByIdList();
		for (int i = 0; i < trains.size(); i++) {
			Train train = getTrainById(trains.get(i));
			String[] loadNames = train.getLoadNames();
			for (int j = 0; j < loadNames.length; j++) {
				if (loadNames[j].equals(oldLoadName)) {
					train.deleteLoadName(oldLoadName);
					if (newLoadName != null)
						train.addLoadName(newLoadName);
				}
			}
		}
	}

	/**
	 * 
	 * @return true if there are any trains built
	 */
	public boolean getAnyTrainBuilt() {
		List<String> trains = getTrainsByIdList();
		for (int i = 0; i < trains.size(); i++) {
			Train train = getTrainById(trains.get(i));
			if (train.isBuilt())
				return true;
		}
		return false;
	}

	/**
	 * 
	 * @param car
	 * @return Train that can service car from its current location to the its destination.
	 */
	public Train getTrainForCar(Car car) {
		List<String> trains = getTrainsByIdList();
		for (int i = 0; i < trains.size(); i++) {
			Train train = getTrainById(trains.get(i));
			// does this train service this car?
			if (train.servicesCar(car))
				return train;
		}
		return null;
	}

	/**
	 * Sort by train name
	 * 
	 * @return list of train ids ordered by name
	 */
	public List<String> getTrainsByNameList() {
		return getTrainsByList(getList(), GET_TRAIN_NAME);
	}

	/**
	 * Sort by train departure time
	 * 
	 * @return list of train ids ordered by departure time
	 */
	public List<String> getTrainsByTimeList() {
		return getTrainsByIntList(getTrainsByNameList(), GET_TRAIN_TIME);
	}

	/**
	 * Sort by train departure name
	 * 
	 * @return list of train ids ordered by departure name
	 */
	public List<String> getTrainsByDepartureList() {
		return getTrainsByList(getTrainsByNameList(), GET_TRAIN_DEPARTES_NAME);
	}

	/**
	 * Sort by train termination name
	 * 
	 * @return list of train ids ordered by termination name
	 */
	public List<String> getTrainsByTerminatesList() {
		return getTrainsByList(getTrainsByNameList(), GET_TRAIN_TERMINATES_NAME);
	}

	/**
	 * Sort by train route name
	 * 
	 * @return list of train ids ordered by route name
	 */
	public List<String> getTrainsByRouteList() {
		return getTrainsByList(getTrainsByNameList(), GET_TRAIN_ROUTE_NAME);
	}

	/**
	 * Sort by train route name
	 * 
	 * @return list of train ids ordered by route name
	 */
	public List<String> getTrainsByStatusList() {
		return getTrainsByList(getTrainsByNameList(), GET_TRAIN_STATUS);
	}

	/**
	 * Sort by train id
	 * 
	 * @return list of train ids ordered by id
	 */
	public List<String> getTrainsByIdList() {
		return getTrainsByIntList(getList(), GET_TRAIN_ID);
	}

	private List<String> getTrainsByList(List<String> sortList, int attribute) {
		List<String> out = new ArrayList<String>();
		for (int i = 0; i < sortList.size(); i++) {
			boolean trainAdded = false;
			Train train = getTrainById(sortList.get(i));
			String inTrainAttribute = (String) getTrainAttribute(train, attribute);
			for (int j = 0; j < out.size(); j++) {
				train = getTrainById(out.get(j));
				String outTrainAttribute = (String) getTrainAttribute(train, attribute);
				if (inTrainAttribute.compareToIgnoreCase(outTrainAttribute) < 0) {
					out.add(j, sortList.get(i));
					trainAdded = true;
					break;
				}
			}
			if (!trainAdded) {
				out.add(sortList.get(i));
			}
		}
		return out;
	}

	private List<String> getTrainsByIntList(List<String> sortList, int attribute) {
		List<String> out = new ArrayList<String>();
		for (int i = 0; i < sortList.size(); i++) {
			boolean trainAdded = false;
			Train train = getTrainById(sortList.get(i));
			int inTrainAttribute = (Integer) getTrainAttribute(train, attribute);
			for (int j = 0; j < out.size(); j++) {
				train = getTrainById(out.get(j));
				int outTrainAttribute = (Integer) getTrainAttribute(train, attribute);
				if (inTrainAttribute < outTrainAttribute) {
					out.add(j, sortList.get(i));
					trainAdded = true;
					break;
				}
			}
			if (!trainAdded) {
				out.add(sortList.get(i));
			}
		}
		return out;
	}

	// the various sort options for trains
	private static final int GET_TRAIN_DEPARTES_NAME = 0;
	private static final int GET_TRAIN_NAME = 1;
	private static final int GET_TRAIN_ROUTE_NAME = 2;
	private static final int GET_TRAIN_TERMINATES_NAME = 3;
	private static final int GET_TRAIN_TIME = 4;
	private static final int GET_TRAIN_STATUS = 5;
	private static final int GET_TRAIN_ID = 6;

	private Object getTrainAttribute(Train train, int attribute) {
		switch (attribute) {
		case GET_TRAIN_DEPARTES_NAME:
			return train.getTrainDepartsName();
		case GET_TRAIN_NAME:
			return train.getName();
		case GET_TRAIN_ROUTE_NAME:
			return train.getTrainRouteName();
		case GET_TRAIN_TERMINATES_NAME:
			return train.getTrainTerminatesName();
		case GET_TRAIN_TIME:
			return train.getDepartTimeMinutes();
		case GET_TRAIN_STATUS:
			return train.getStatus();
		case GET_TRAIN_ID:
			return Integer.parseInt(train.getId());
		default:
			return "unknown";
		}
	}

	private List<String> getList() {
		if (!TrainManagerXml.instance().isTrainFileLoaded())
			log.error("TrainManager getList called before trains completely loaded!");
		List<String> out = new ArrayList<String>();
		Enumeration<String> en = _trainHashTable.keys();
		String[] arr = new String[_trainHashTable.size()];
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

	public JComboBox getComboBox() {
		JComboBox box = new JComboBox();
		box.addItem("");
		List<String> trains = getTrainsByNameList();
		for (int i = 0; i < trains.size(); i++) {
			Train train = getTrainById(trains.get(i));
			box.addItem(train);
		}
		return box;
	}

	public void updateComboBox(JComboBox box) {
		box.removeAllItems();
		box.addItem("");
		List<String> trains = getTrainsByNameList();
		for (int i = 0; i < trains.size(); i++) {
			Train train = getTrainById(trains.get(i));
			box.addItem(train);
		}
	}

	/**
	 * Update combo box with trains that will service this car
	 * 
	 * @param box
	 *            the combo box to update
	 * @param car
	 *            the car to be serviced
	 */
	public void updateComboBox(JComboBox box, Car car) {
		box.removeAllItems();
		box.addItem("");
		List<String> trains = getTrainsByNameList();
		for (int i = 0; i < trains.size(); i++) {
			Train train = getTrainById(trains.get(i));
			if (train.servicesCar(car))
				box.addItem(train);
		}
	}

	/**
	 * @return Number of trains
	 */
	public int numEntries() {
		return _trainHashTable.size();
	}

	/**
	 * Makes a copy of an existing train. Only the train's description isn't copied.
	 * 
	 * @param train
	 *            the train to copy
	 * @param trainName
	 *            the name of the new train
	 * @return a copy of train
	 */
	public Train copyTrain(Train train, String trainName) {
		Train newTrain = newTrain(trainName);
		// route, departure time and types
		newTrain.setRoute(train.getRoute());
		newTrain.setTrainSkipsLocations(train.getTrainSkipsLocations());
		newTrain.setDepartureTime(train.getDepartureTimeHour(), train.getDepartureTimeMinute());
		newTrain._typeList.clear(); // remove all types loaded by create
		newTrain.setTypeNames(train.getTypeNames());
		// set road, load, and owner options
		newTrain.setRoadOption(train.getRoadOption());
		newTrain.setRoadNames(train.getRoadNames());
		newTrain.setLoadOption(train.getLoadOption());
		newTrain.setLoadNames(train.getLoadNames());
		newTrain.setOwnerOption(train.getOwnerOption());
		newTrain.setOwnerNames(train.getOwnerNames());
		// build dates
		newTrain.setBuiltStartYear(train.getBuiltStartYear());
		newTrain.setBuiltEndYear(train.getBuiltEndYear());
		// locos start of route
		newTrain.setNumberEngines(train.getNumberEngines());
		newTrain.setEngineModel(train.getEngineModel());
		newTrain.setEngineRoad(train.getEngineRoad());
		newTrain.setRequirements(train.getRequirements());
		newTrain.setCabooseRoad(train.getCabooseRoad());
		// second leg
		newTrain.setSecondLegNumberEngines(train.getSecondLegNumberEngines());
		newTrain.setSecondLegEngineModel(train.getSecondLegEngineModel());
		newTrain.setSecondLegEngineRoad(train.getSecondLegEngineRoad());
		newTrain.setSecondLegOptions(train.getSecondLegOptions());
		newTrain.setSecondLegCabooseRoad(train.getSecondLegCabooseRoad());
		newTrain.setSecondLegStartLocation(train.getSecondLegStartLocation());
		newTrain.setSecondLegEndLocation(train.getSecondLegEndLocation());
		// third leg
		newTrain.setThirdLegNumberEngines(train.getThirdLegNumberEngines());
		newTrain.setThirdLegEngineModel(train.getThirdLegEngineModel());
		newTrain.setThirdLegEngineRoad(train.getThirdLegEngineRoad());
		newTrain.setThirdLegOptions(train.getThirdLegOptions());
		newTrain.setThirdLegCabooseRoad(train.getThirdLegCabooseRoad());
		newTrain.setThirdLegStartLocation(train.getThirdLegStartLocation());
		newTrain.setThirdLegEndLocation(train.getThirdLegEndLocation());
		// scripts
		for (int i = 0; i < train.getBuildScripts().size(); i++)
			newTrain.addBuildScript(train.getBuildScripts().get(i));
		for (int i = 0; i < train.getMoveScripts().size(); i++)
			newTrain.addMoveScript(train.getMoveScripts().get(i));
		for (int i = 0; i < train.getTerminationScripts().size(); i++)
			newTrain.addTerminationScript(train.getTerminationScripts().get(i));
		// options
		newTrain.setRailroadName(train.getRailroadName());
		newTrain.setManifestLogoURL(train.getManifestLogoURL());
		// comment
		newTrain.setComment(train.getComment());

		return newTrain;
	}

	public void options(Element values) {
		if (log.isDebugEnabled())
			log.debug("ctor from element " + values);
		Element e = values.getChild("trainOptions");
		org.jdom.Attribute a;
		if (e != null) {
			if ((a = e.getAttribute("buildMessages")) != null)
				_buildMessages = a.getValue().equals("true");
			if ((a = e.getAttribute("buildReport")) != null)
				_buildReport = a.getValue().equals("true");
			if ((a = e.getAttribute("printPreview")) != null)
				_printPreview = a.getValue().equals("true");
			if ((a = e.getAttribute("openFile")) != null)
				_openFile = a.getValue().equals("true");
			if ((a = e.getAttribute("trainAction")) != null)
				_trainAction = a.getValue();

			// TODO This here is for backwards compatibility, remove after next major release
			if ((a = e.getAttribute("columnWidths")) != null) {
				String[] widths = a.getValue().split(" ");
				for (int i = 0; i < widths.length; i++) {
					try {
						_tableColumnWidths[i] = Integer.parseInt(widths[i]);
					} catch (NumberFormatException ee) {
						log.error("Number format exception when reading trains column widths");
					}
				}
			}
		}

		e = values.getChild("trainScheduleOptions");
		if (e != null) {
			if ((a = e.getAttribute("activeId")) != null) {
				_trainScheduleActiveId = a.getValue();
			}
			// TODO This here is for backwards compatibility, remove after next major release
			if ((a = e.getAttribute("columnWidths")) != null) {
				String[] widths = a.getValue().split(" ");
				_tableScheduleColumnWidths = new int[widths.length];
				for (int i = 0; i < widths.length; i++) {
					try {
						_tableScheduleColumnWidths[i] = Integer.parseInt(widths[i]);
					} catch (NumberFormatException ee) {
						log.error("Number format exception when reading trains column widths");
					}
				}
			}
		}
		// check for scripts
		if (values.getChild("scripts") != null) {
			@SuppressWarnings("unchecked")
			List<Element> lm = values.getChild("scripts").getChildren("startUp");
			for (int i = 0; i < lm.size(); i++) {
				Element es = lm.get(i);
				if ((a = es.getAttribute("name")) != null) {
					addStartUpScript(a.getValue());
				}
			}
			@SuppressWarnings("unchecked")
			List<Element> lt = values.getChild("scripts").getChildren("shutDown");
			for (int i = 0; i < lt.size(); i++) {
				Element es = lt.get(i);
				if ((a = es.getAttribute("name")) != null) {
					addShutDownScript(a.getValue());
				}
			}
		}
	}

	/**
	 * Create an XML element to represent this Entry. This member has to remain synchronized with the detailed DTD in
	 * operations-trains.dtd.
	 * 
	 * @return Contents in a JDOM Element
	 */
	public Element store() {
		Element values = new Element("options");
		Element e = new Element("trainOptions");
		e.setAttribute("buildMessages", isBuildMessagesEnabled() ? "true" : "false");
		e.setAttribute("buildReport", isBuildReportEnabled() ? "true" : "false");
		e.setAttribute("printPreview", isPrintPreviewEnabled() ? "true" : "false");
		e.setAttribute("openFile", isOpenFileEnabled() ? "true" : "false");
		e.setAttribute("trainAction", getTrainsFrameTrainAction());
		values.addContent(e);
		// now save train schedule options
		e = new Element("trainScheduleOptions");
		e.setAttribute("activeId", getTrainScheduleActiveId());
		values.addContent(e);

		// save list of move scripts for this train
		if (getStartUpScripts().size() > 0 || getShutDownScripts().size() > 0) {
			Element es = new Element("scripts");
			for (int i = 0; i < getStartUpScripts().size(); i++) {
				Element em = new Element("startUp");
				em.setAttribute("name", getStartUpScripts().get(i));
				es.addContent(em);
			}
			// save list of termination scripts for this train
			for (int i = 0; i < getShutDownScripts().size(); i++) {
				Element et = new Element("shutDown");
				et.setAttribute("name", getShutDownScripts().get(i));
				es.addContent(et);
			}
			values.addContent(es);
		}
		return values;
	}

	/**
	 * Check for car type and road name replacements. Also check for engine type replacement.
	 * 
	 */
	public void propertyChange(java.beans.PropertyChangeEvent e) {
		log.debug("TrainManager sees property change: " + e.getPropertyName() + " old: "
				+ e.getOldValue() + " new " + e.getNewValue());
		// TODO use listener to determine if load name has changed
		// if (e.getPropertyName().equals(CarLoads.LOAD_NAME_CHANGED_PROPERTY)){
		// replaceLoad((String)e.getOldValue(), (String)e.getNewValue());
		// }
	}

	java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);

	public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
		pcs.addPropertyChangeListener(l);
	}

	public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
		pcs.removePropertyChangeListener(l);
	}

	private void firePropertyChange(String p, Object old, Object n) {
		TrainManagerXml.instance().setDirty(true);
		pcs.firePropertyChange(p, old, n);
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TrainManager.class
			.getName());

}

/* @(#)TrainManager.java */
