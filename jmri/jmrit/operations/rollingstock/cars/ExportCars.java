// ExportCars.java

package jmri.jmrit.operations.rollingstock.cars;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import jmri.jmrit.XmlFile;
import jmri.jmrit.operations.setup.OperationsSetupXml;

/**
 * Exports the car roster into a comma delimitated file (CSV).
 * @author Daniel Boudreau Copyright (C) 2010
 * @version	$Revision: 1.4 $
 *
 */
public class ExportCars extends XmlFile {
	
	private String del = ","; 	// delimiter
	
	public ExportCars(){
		
	}
	
	public void setDeliminter (String delimiter){
		del = delimiter;
	}
	
	/**
     * Store the all of the operation car objects in the default place, including making a backup if needed
     */
    public void writeOperationsCarFile() {
    	makeBackupFile(defaultOperationsFilename());
        try {
          	 if(!checkFile(defaultOperationsFilename()))
             {
                 //The file does not exist, create it before writing
                 java.io.File file=new java.io.File(defaultOperationsFilename());
                 java.io.File parentDir=file.getParentFile();
                 if (!parentDir.exists()){
                     if (!parentDir.mkdir())
                     	log.error("Directory wasn't created");
                  }
                  if (file.createNewFile())
                 	 log.debug("File created");
             }
        	writeFile(defaultOperationsFilename());
        } catch (Exception e) {
            log.error("Exception while writing the new CSV operations file, may not be complete: "+e);
        }
    }
    
    public void writeFile(String name){
        if (log.isDebugEnabled()) log.debug("writeFile "+name);
        // This is taken in large part from "Java and XML" page 368
        File file = findFile(name);
        if (file == null) {
            file = new File(name);
        }
        
        PrintWriter fileOut;

		try {
			fileOut = new PrintWriter(new BufferedWriter(new FileWriter(file)),
					true);
		} catch (IOException e) {
			log.error("can not open car roster CSV file");
			return;
		}
        
        CarManager manager = CarManager.instance();
        List<String> carList = manager.getByNumberList();
        String line ="";
        // check for delimiter in the following car fields
        String carType;
        String carLocationName;
        String carTrackName;
        
        for (int i=0; i<carList.size(); i++){
        	// store car number, road, type, length, weight, color, owner, built date, location and track
        	Car car = manager.getById(carList.get(i));
        	carType = car.getType();
        	if (carType.contains(del)){
        		log.debug("Car ("+car.getRoad()+" "+car.getNumber()+") has delimiter in type field: "+carType);
        		carType = "\""+car.getType()+"\"";
        	}
        	carLocationName = car.getLocationName();
        	if (carLocationName.contains(del)){
        		log.debug("Car ("+car.getRoad()+" "+car.getNumber()+") has delimiter in location field: "+carLocationName);
        		carLocationName = "\""+car.getLocationName()+"\"";
        	}
        	carTrackName = car.getTrackName();
        	if (carTrackName.contains(del)){
        		log.debug("Car ("+car.getRoad()+" "+car.getNumber()+") has delimiter in track field: "+carTrackName);
        		carTrackName = "\""+car.getTrackName()+"\"";
        	}
			line = car.getNumber() + del + car.getRoad() + del + carType
					+ del + car.getLength() + del + car.getWeight() + del
					+ car.getColor() + del + car.getOwner() + del + car.getBuilt()
					+ del + carLocationName + ",-," +carTrackName;
			fileOut.println(line);
        }
		fileOut.flush();
		fileOut.close();
		log.info("Exported "+carList.size()+" cars to file "+defaultOperationsFilename());
    }
    
    // Operation files always use the same directory
    public static String defaultOperationsFilename() { 
    	return OperationsSetupXml.getFileLocation()+OperationsSetupXml.getOperationsDirectoryName()+File.separator+getOperationsFileName();
    }

    public static void setOperationsFileName(String name) { OperationsFileName = name; }
    public static String getOperationsFileName(){
    	return OperationsFileName;
    }
    private static String OperationsFileName = "ExportOperationsCarRoster.csv";
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ExportCars.class.getName());


}
