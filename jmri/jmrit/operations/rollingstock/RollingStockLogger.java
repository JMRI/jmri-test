// RollingStockLogger.java

package jmri.jmrit.operations.rollingstock;

import java.beans.PropertyChangeEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.ResourceBundle;

import jmri.jmrit.XmlFile;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.OperationsSetupXml;
import jmri.jmrit.operations.setup.Setup;

import java.util.List;

/**
 * Logs rolling stock movements by writing their locations to a file.
 * 
 * @author Daniel Boudreau Copyright (C) 2010
 * @version $Revision: 1.5 $
 */
public class RollingStockLogger extends XmlFile implements java.beans.PropertyChangeListener{
	
	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.rollingstock.cars.JmritOperationsCarsBundle");
	
	File fileLogger;
	private boolean newFile = true;
	private boolean engLog = false;	// when true logging engine movements
	private boolean carLog = false;	// when true logging car movements
	private String del = ","; 		// delimiter

	public RollingStockLogger() {
	}
	
	/** record the single instance **/
	private static RollingStockLogger _instance = null;

	public static synchronized RollingStockLogger instance() {
		if (_instance == null) {
			if (log.isDebugEnabled()) log.debug("RollingStockLogger creating instance");
			// create and load
			_instance = new RollingStockLogger();
		}
		if (Control.showInstance && log.isDebugEnabled()) log.debug("RollingStockLogger returns instance "+_instance);
		return _instance;
	}
	
	public void enableCarLogging(boolean enable){
		if (enable){
			createFile();
			addCarListeners();
		} else {
			removeCarListeners();
		}
	}
	
	public void enableEngineLogging(boolean enable){
		if (enable){
			createFile();
			addEngineListeners();
		} else {
			removeEngineListeners();
		}
	}
	
	private void createFile(){
		if (!Setup.isEngineLoggerEnabled() && !Setup.isCarLoggerEnabled())
			return;
		if (fileLogger != null)
			return;	// log file has already been created
		// create the logging file for this session
		try {
			if (!checkFile(getFullLoggerFileName())) {
				// The file/directory does not exist, create it before writing
				fileLogger = new java.io.File(getFullLoggerFileName());
				File parentDir = fileLogger.getParentFile();
				if (!parentDir.exists()) {
					if (!parentDir.mkdirs()) {
						log.error("backup directory not created");
					}
				}
				if (fileLogger.createNewFile())
					log.debug("new file created");
			} else {
				fileLogger = new java.io.File(getFullLoggerFileName());
				newFile = false;
			}
		} catch (Exception e) {
			log.error("Exception while making logging directory: "+ e);
		}
		
	}
	
	private boolean mustHaveTrack = true;	// when true only updates that have a track are saved
	private void store(RollingStock rs){
		if (fileLogger == null){
			log.error("Log file doesn't exist");
			return;
		}
		if (rs.getTrack() == null && mustHaveTrack)
			return;
		
        PrintWriter fileOut;

		try {
			// FileWriter is set to append
			fileOut = new PrintWriter(new BufferedWriter(new FileWriter(fileLogger, true)),
					true);
		} catch (IOException e) {
			log.error("Exception while opening log file: "+e.getLocalizedMessage());
			return;
		}
		if (newFile){
			String header = rb.getString("Number") +del+ rb.getString("Road") 
			+del+ rb.getString("Type") +del+ rb.getString("Load") 
			+del+ rb.getString("Location") +del+ rb.getString("Track") 
			+del+ rb.getString("Train") +del+ rb.getString("Moves") 
			+del+ rb.getString("DateAndTime");
			fileOut.println(header);
			newFile = false;
		}
			
       	String rsType = rs.getType();
    	if (rsType.contains(del)){
    		log.debug("RS ("+rs.toString()+") has delimiter in type field: "+rsType);
    		rsType = "\""+rs.getType()+"\"";
    	}
    	String rsLocationName = rs.getLocationName();
    	if (rsLocationName.contains(del)){
    		log.debug("RS ("+rs.toString()+") has delimiter in location field: "+rsLocationName);
    		rsLocationName = "\""+rs.getLocationName()+"\"";
    	}
    	String rsTrackName = rs.getTrackName();
    	if (rsTrackName.contains(del)){
    		log.debug("RS ("+rs.toString()+") has delimiter in track field: "+rsTrackName);
    		rsTrackName = "\""+rs.getTrackName()+"\"";
    	}
    	String carLoad = " ";
    	if (rs.getClass().equals(Car.class)){
    		Car car = (Car)rs;
    		carLoad = car.getLoad();
    		if (carLoad.contains(del)){
    			log.debug("RS ("+rs.toString()+") has delimiter in car load field: "+carLoad);
    			carLoad = "\""+car.getLoad()+"\"";
    		}
    	}

		String line = rs.getNumber() +del+ rs.getRoad() +del+ rsType
		+del+ carLoad +del+ rsLocationName +del+ rsTrackName +del+ rs.getTrainName() 
		+del+ rs.getMoves() +del+ getTime();
		
		log.debug("Log: "+line);

		fileOut.println(line);
		fileOut.flush();
		fileOut.close();
	}
	

	
	private void addCarListeners(){
		if (Setup.isCarLoggerEnabled() && !carLog){
			log.debug("Rolling Stock Logger adding car listerners");
			carLog = true;
			List<String> cars = CarManager.instance().getByIdList();
			for (int i=0; i<cars.size(); i++){
				Car car = CarManager.instance().getById(cars.get(i));
				if (car != null)
					car.addPropertyChangeListener(this);
			}
			// listen for new rolling stock being added
			CarManager.instance().addPropertyChangeListener(this);
		}
	}
	
	private void addEngineListeners(){
		if (Setup.isEngineLoggerEnabled() && !engLog){
			engLog = true;
			log.debug("Rolling Stock Logger adding engine listerners");
			List<String>engines = EngineManager.instance().getByIdList();
			for (int i=0; i<engines.size(); i++){
				Engine engine = EngineManager.instance().getById(engines.get(i));
				if (engine != null)
					engine.addPropertyChangeListener(this);
			}
			// listen for new rolling stock being added
			EngineManager.instance().addPropertyChangeListener(this);
		}
	}
	
	private void removeCarListeners(){
		if (carLog){
			log.debug("Rolling Stock Logger removing car listerners");
			List<String> cars = CarManager.instance().getByIdList();
			for (int i=0; i<cars.size(); i++){
				Car car = CarManager.instance().getById(cars.get(i));
				if (car != null)
					car.removePropertyChangeListener(this);
			}
			CarManager.instance().removePropertyChangeListener(this);
		}
		carLog = false;
	}

	private void removeEngineListeners(){
		if (engLog){
			log.debug("Rolling Stock Logger removing engine listerners");
			List<String> engines = EngineManager.instance().getByIdList();
			for (int i=0; i<engines.size(); i++){
				Engine engine = EngineManager.instance().getById(engines.get(i));
				if (engine != null)
					engine.removePropertyChangeListener(this);
			}
			EngineManager.instance().removePropertyChangeListener(this);
		}
		engLog = false;
	}
	
	private void removeListeners(){
		removeCarListeners();
		removeEngineListeners();
	}
	
	public void dispose(){
		removeListeners();
	}

	public void propertyChange(PropertyChangeEvent e) {
		if (e.getPropertyName().equals(Car.TRACK_CHANGED_PROPERTY)){
			if(Control.showProperty && log.isDebugEnabled()) 
				log.debug("Logger sees property change for car "+e.getSource());
			store((RollingStock)e.getSource());
		}
		if (e.getPropertyName().equals(CarManager.LISTLENGTH_CHANGED_PROPERTY)){
			if ((Integer)e.getNewValue() > (Integer)e.getOldValue()){
				// a car or engine has been added
				removeListeners();
				addEngineListeners();
				addCarListeners();
			}
		}
	}
	
	public String getFullLoggerFileName(){
		return loggingDirectory + File.separator + getFileName();
	}
	
	private String operationsDirectory = OperationsSetupXml.getFileLocation()+OperationsSetupXml.getOperationsDirectoryName();	
	private String loggingDirectory = operationsDirectory + File.separator + "logger";
	
	public String getDirectoryName(){
		return loggingDirectory;
	}
	
	public void setDirectoryName(String name){
		loggingDirectory = name;
	}

	private String fileName;
	public String getFileName(){
		if (fileName == null)
			fileName = getDate()+".csv";
		return fileName;
	}

	private String getDate() {
		Calendar now = Calendar.getInstance();
		int month = now.get(Calendar.MONTH) + 1;
		String m = Integer.toString(month);
		if (month < 10){
			m = "0"+Integer.toString(month);
		}
		int day = now.get(Calendar.DATE);
		String d = Integer.toString(day);
		if (day < 10){
			d = "0"+Integer.toString(day);
		}
		String date = "" + now.get(Calendar.YEAR) + "_"	+ m + "_" + d;
		return date;
	}
	
	private String getTime() {
		return Calendar.getInstance().getTime().toString();
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(RollingStockLogger.class.getName());
}
