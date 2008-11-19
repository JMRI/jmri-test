// TrainSwitchLists.java

package jmri.jmrit.operations.trains;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Setup;

public class TrainCommon {
	
	static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.trains.JmritOperationsTrainsBundle");
	private static final String FEET = Setup.FEET;
	private static final String BOX = " [ ] ";

	protected void pickupEngine(PrintWriter file, Engine engine){
		String comment = (Setup.isAppendCarCommentEnabled() ? " "
				+ engine.getComment(): "");
		addLine(file, BOX + rb.getString("Pickup") +" "
				+ rb.getString("Engine") + " "
				+ engine.getRoad() + " "
				+ engine.getNumber() + " ("
				+ engine.getModel() + ") "
				+ rb.getString("from") + " "
				+ engine.getTrackName() + comment);
	}
	
	protected void dropEngine(PrintWriter file, Engine engine){
		String comment = (Setup.isAppendCarCommentEnabled() ? " "
				+ engine.getComment(): "");
		addLine(file, BOX + rb.getString("Drop") +" "
				+ rb.getString("Engine") + " "
				+ engine.getRoad() + " "
				+ engine.getNumber() + " ("
				+ engine.getModel() + ") "
				+ rb.getString("to") + " "
				+ engine.getDestinationTrackName() + comment);
	}
	
	protected void  pickupCar(PrintWriter file, Car car){
		String[] carNumber = car.getNumber().split("-"); // ignore any duplicate car numbers
		String[] carType = car.getType().split("-"); // ignore lading
		String carComment = (Setup.isAppendCarCommentEnabled() ? " "+car.getComment() : "");
		addLine(file, BOX + rb.getString("Pickup")+" " + car.getRoad() + " "
				+ carNumber[0] + " " + carType[0] + " "
				+ car.getLength() + FEET + " " + car.getColor()
				+ (car.isHazardous() ? " ("+rb.getString("Hazardous")+")" : "")
				+ (car.hasFred() ? " ("+rb.getString("fred")+")" : "") + " " + rb.getString("from")+ " "
				+ car.getTrackName() + carComment);
	}
	
	protected void dropCar(PrintWriter file, Car car){
		String[] carNumber = car.getNumber().split("-"); // ignore any duplicate car numbers
		String[] carType = car.getType().split("-"); // ignore lading
		String carComment = (Setup.isAppendCarCommentEnabled() ? " "+car.getComment() : "");
		addLine(file, BOX + rb.getString("Drop")+ " " + car.getRoad() + " "
				+ carNumber[0] + " " + carType[0] + " "
				+ car.getLength() + FEET + " " + car.getColor()
				+ (car.isHazardous() ? " ("+rb.getString("Hazardous")+") " : " ")
				+ rb.getString("to") + " " + car.getDestinationTrackName()
				+ carComment);
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
	
	static org.apache.log4j.Category log = org.apache.log4j.Category
	.getInstance(TrainCommon.class.getName());
}
