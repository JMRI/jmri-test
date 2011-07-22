// TrainSwitchLists.java

package jmri.jmrit.operations.trains;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.ResourceBundle;
import java.util.List;

import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarColors;
import jmri.jmrit.operations.rollingstock.cars.CarLengths;
import jmri.jmrit.operations.rollingstock.cars.CarLoads;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;

/**
 * Common routines for trains
 * @author Daniel Boudreau (C) Copyright 2008, 2009, 2010, 2011
 *
 */
public class TrainCommon {
	
	static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.trains.JmritOperationsTrainsBundle");
	private static final String LENGTHABV = Setup.LENGTHABV;
	protected static final String TAB = "    ";
	private static final boolean pickup = true;
	private static final boolean local = true;
	EngineManager engineManager = EngineManager.instance();
	
	protected void pickupEngines(PrintWriter fileOut, List<String> engineList, RouteLocation rl){
		for (int i =0; i < engineList.size(); i++){
			Engine engine = engineManager.getById(engineList.get(i));
			if (engine.getRouteLocation() == rl && !engine.getTrackName().equals(""))
				pickupEngine(fileOut, engine);
		}
	}
	
	protected void dropEngines(PrintWriter fileOut, List<String> engineList, RouteLocation rl){
		for (int i =0; i < engineList.size(); i++){
			Engine engine = engineManager.getById(engineList.get(i));
			if (engine.getRouteDestination() == rl)
				dropEngine(fileOut, engine);
		}
	}
	
	
	protected void pickupEngine(PrintWriter file, Engine engine){
		StringBuffer buf = new StringBuffer(Setup.getPickupEnginePrefix());
		String[] format = Setup.getPickupEngineMessageFormat();
		for (int i=0; i<format.length; i++){
			buf.append(getEngineAttribute(engine, format[i], pickup));
		}
		addLine(file, buf.toString());
	}
	
	protected void dropEngine(PrintWriter file, Engine engine){
		StringBuffer buf = new StringBuffer(Setup.getDropEnginePrefix());
		String[] format = Setup.getDropEngineMessageFormat();
		for (int i=0; i<format.length; i++){
			buf.append(getEngineAttribute(engine, format[i], !pickup));
		}
		addLine(file, buf.toString());
	}
	
	protected void pickupCar(PrintWriter file, Car car){
		StringBuffer buf = new StringBuffer(Setup.getPickupCarPrefix());
		if (car.getRouteLocation().equals(car.getRouteDestination()))
			return; // print nothing local move, see dropCar
		String[] format = Setup.getPickupCarMessageFormat();
		for (int i=0; i<format.length; i++){
			String s = getCarAttribute(car, format[i], pickup, !local);
			if (buf.length()+s.length()>lineLength()){
				addLine(file, buf.toString());
				buf = new StringBuffer(TAB);
			}
			buf.append(s);
		}
		addLine(file, buf.toString());
	}
	
	protected void dropCar(PrintWriter file, Car car){
		StringBuffer buf = new StringBuffer(Setup.getDropCarPrefix());
		String[] format = Setup.getDropCarMessageFormat();
		// local move?
		boolean local = false;
		if (car.getRouteLocation().equals(car.getRouteDestination()) && car.getTrack()!=null){
			buf = new StringBuffer(Setup.getLocalPrefix());
			format = Setup.getLocalMessageFormat();
			local = true;
		}
		for (int i=0; i<format.length; i++){
			String s = getCarAttribute(car, format[i], !pickup, local);
			if (buf.length()+s.length()>lineLength()){
				addLine(file, buf.toString());
				buf = new StringBuffer(TAB);
			}
			buf.append(s);
		}
		addLine(file, buf.toString());
	}
	
	// writes string with level to console and file
	protected void addLine (PrintWriter file, String level, String string){
		if(log.isDebugEnabled())
			log.debug(string);
		if (file != null)
			file.println(level +"- " + string);
	}
	
	// writes string to console and file
	protected void addLine (PrintWriter file, String string){
		if(log.isDebugEnabled())
			log.debug(string);
		if (file != null)
			file.println(string);
	}
	
	protected void newLine (PrintWriter file){
		file.println(" ");
	}
	
	/**
	 * Splits a string (example-number) as long as the second part of
	 * the string is an integer.
	 * @param name
	 * @return First half the string.
	 */
	protected static String splitString(String name){
		String[] fullname = name.split("-");
		String parsedName = fullname[0].trim();
		// is the hyphen followed by a number?
		if (fullname.length>1){
			try{
				Integer.parseInt(fullname[1]);
			}
			catch (NumberFormatException e){
				// no return full name
				parsedName = name;
			}
		}
		return parsedName;
	}
	
	protected void getCarsLocationUnknown(PrintWriter file){
		CarManager cManager = CarManager.instance();
		List<String> cars = cManager.getCarsLocationUnknown();
		if (cars.size() == 0)
			return;	// no cars to search for!
		newLine(file);
		addLine(file, Setup.getMiaComment());
		for (int i=0; i<cars.size(); i++){
			Car car = cManager.getById(cars.get(i));
			searchForCar(file, car);
		}
	}
	
	protected void searchForCar(PrintWriter file, Car car){
		StringBuffer buf = new StringBuffer();
		String[] format = Setup.getMissingCarMessageFormat();
		for (int i=0; i<format.length; i++){
			buf.append(getCarAttribute(car, format[i], false, false));
		}
		addLine(file, buf.toString());
	}
	

	// @param pickup true when rolling stock is being picked up 	
	protected String getEngineAttribute(Engine engine, String attribute, boolean pickup){
		if (attribute.equals(Setup.MODEL))
			return " "+ engine.getModel();
		if (attribute.equals(Setup.CONSIST))
			return " "+ engine.getConsistName();
		return getRollingStockAttribute(engine, attribute, pickup, false);
	}
	
	protected String getCarAttribute(Car car, String attribute, boolean pickup, boolean local){
		if (attribute.equals(Setup.LOAD))
			return (car.isCaboose() || car.isPassenger())? tabString("", CarLoads.instance().getCurMaxNameLength()+1) 
					: " "+tabString(car.getLoad(), CarLoads.instance().getCurMaxNameLength());
		else if (attribute.equals(Setup.HAZARDOUS))
			return (car.isHazardous()? " ("+rb.getString("Hazardous")+")" : "");
		else if (attribute.equals(Setup.DROP_COMMENT))
			return " "+CarLoads.instance().getDropComment(car.getType(), car.getLoad());
		else if (attribute.equals(Setup.PICKUP_COMMENT))
			return " "+CarLoads.instance().getPickupComment(car.getType(), car.getLoad());
		else if (attribute.equals(Setup.KERNEL))
			return " "+tabString(car.getKernelName(), Control.MAX_LEN_STRING_ATTRIBUTE);
		return getRollingStockAttribute(car, attribute, pickup, local);
	}

	protected String getRollingStockAttribute(RollingStock rs, String attribute, boolean pickup, boolean local){
		if (attribute.equals(Setup.NUMBER))
			return " "+tabString(splitString(rs.getNumber()), Control.MAX_LEN_STRING_ROAD_NUMBER-4);
		else if (attribute.equals(Setup.ROAD))
			return " "+tabString(rs.getRoad(), CarRoads.instance().getCurMaxNameLength());
		else if (attribute.equals(Setup.TYPE)){
			String[] type = rs.getType().split("-");	// second half of string can be anything
			return " "+tabString(type[0], CarTypes.instance().getCurMaxNameLength());
		}
		else if (attribute.equals(Setup.LENGTH))
			return " "+tabString(rs.getLength()+ LENGTHABV, CarLengths.instance().getCurMaxNameLength());
		else if (attribute.equals(Setup.COLOR))
			return " "+tabString(rs.getColor(), CarColors.instance().getCurMaxNameLength());
		else if (attribute.equals(Setup.LOCATION) && (pickup || local))
			return " "+rb.getString("from")+ " "+splitString(rs.getTrackName());
		else if (attribute.equals(Setup.LOCATION) && !pickup && !local)
			return " "+rb.getString("from")+ " "+splitString(rs.getLocationName());
		else if (attribute.equals(Setup.DESTINATION) && pickup){
			if (Setup.isTabEnabled())
				return " "+rb.getString("dest")+ " "+splitString(rs.getDestinationName());
			else
				return " "+rb.getString("destination")+ " "+splitString(rs.getDestinationName());
		}
		else if (attribute.equals(Setup.DESTINATION) && !pickup)
			return " "+rb.getString("to")+ " "+splitString(rs.getDestinationTrackName());
		else if (attribute.equals(Setup.DEST_TRACK))
			return " "+rb.getString("dest")+ " "+splitString(rs.getDestinationName())
					+ ", "+splitString(rs.getDestinationTrackName());
		else if (attribute.equals(Setup.COMMENT))
			return " "+rs.getComment();
		else if (attribute.equals(Setup.NONE))
			return "";
		return " error ";		
	}
	
	protected String getDate(){
		Calendar calendar = Calendar.getInstance();
		
		String year = Setup.getYearModeled();
		if (year.equals(""))
			year = Integer.toString(calendar.get(Calendar.YEAR));
		year = year.trim();
		
		// Use 24 hour clock
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		
		if (Setup.is12hrFormatEnabled()){
			hour = calendar.get(Calendar.HOUR);
			if (hour == 0)
				hour = 12;
		}
		
		String h  = Integer.toString(hour);
		if (hour <10)
			h = "0"+ Integer.toString(hour);
		
		int minute = calendar.get(Calendar.MINUTE);
		String m = Integer.toString(minute);
		if (minute <10)
			m = "0"+ Integer.toString(minute);
					
		//AM_PM field
		String AM_PM = "";
		if (Setup.is12hrFormatEnabled()){
			AM_PM = (calendar.get(Calendar.AM_PM)== Calendar.AM)? "AM":"PM";
		}
		
		// Java 1.6 methods calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()
		// Java 1.6 methods calendar.getDisplayName(Calendar.AM_PM, Calendar.LONG, Locale.getDefault())
		String date = calendar.get(Calendar.MONTH)+1
				+ "/"
				+ calendar.get(Calendar.DAY_OF_MONTH) + ", " + year + " "
				+ h + ":" + m + " " 
				+ AM_PM;
		return date;
	}
	
	public static String tabString(String s, int fieldSize){
		if (!Setup.isTabEnabled())
			return s;
		StringBuffer buf = new StringBuffer(s);
		while (buf.length() < fieldSize){
			buf.append(" ");
		}
		return buf.toString();
	}
	
	int chars_per_line = 0;
	private int lineLength(){
		if (chars_per_line == 0){
			// page size has been adjusted to account for margins of .5
			// Dimension pagesize = new Dimension(612,792);
			Dimension pagesize = new Dimension(540,792);
			// Metrics don't always work for the various font names, so use Monospaced
			Font font = new Font("Monospaced", Font.PLAIN, Setup.getFontSize());
			Frame frame = new Frame();
			FontMetrics metrics = frame.getFontMetrics(font);

			int charwidth = metrics.charWidth('m');

			// compute lines and columns within margins
			chars_per_line = pagesize.width / charwidth;
		}
		return chars_per_line;
	}
	
	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(TrainCommon.class.getName());
}
