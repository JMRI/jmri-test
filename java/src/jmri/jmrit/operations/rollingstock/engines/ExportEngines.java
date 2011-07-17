// ExportEngines.java

package jmri.jmrit.operations.rollingstock.engines;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.swing.JOptionPane;

import jmri.jmrit.XmlFile;
import jmri.jmrit.operations.setup.OperationsSetupXml;

/**
 * Exports the Engine roster into a comma delimitated file (CSV).
 * @author Daniel Boudreau Copyright (C) 2010
 * @version	$Revision: 1.6 $
 *
 */
public class ExportEngines extends XmlFile {
	
	private String del = ","; 	// delimiter
	
	public ExportEngines(){
		
	}
	
	public void setDeliminter (String delimiter){
		del = delimiter;
	}
	
	/**
     * Store the all of the operation Engine objects in the default place, including making a backup if needed
     */
    public void writeOperationsEngineFile() {
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
			log.error("can not open Engine roster CSV file");
			return;
		}
        
        EngineManager manager = EngineManager.instance();
        List<String> engineList = manager.getByNumberList();
        String line ="";
        // check for delimiter in the following Engine fields
        String engineModel;
        String engineLocationName;
        String engineTrackName;
        // assume delimiter in the value field
        String value;
        
        for (int i=0; i<engineList.size(); i++){
        	// store engine number, road, model, length, weight, owner, built date, location and track
        	Engine engine = manager.getById(engineList.get(i));
        	engineModel = engine.getModel();
        	if (engineModel.contains(del)){
        		log.debug("Engine ("+engine.getRoad()+" "+engine.getNumber()+") has delimiter in model field: "+engineModel);
        		engineModel = "\""+engine.getModel()+"\"";
        	}
        	engineLocationName = engine.getLocationName();
        	if (engineLocationName.contains(del)){
        		log.debug("Engine ("+engine.getRoad()+" "+engine.getNumber()+") has delimiter in location field: "+engineLocationName);
        		engineLocationName = "\""+engine.getLocationName()+"\"";
        	}
        	engineTrackName = engine.getTrackName();
        	if (engineTrackName.contains(del)){
        		log.debug("Engine ("+engine.getRoad()+" "+engine.getNumber()+") has delimiter in track field: "+engineTrackName);
        		engineTrackName = "\""+engine.getTrackName()+"\"";
        	}
           	// only export value field if value has been set.
        	value = "";
        	if (!engine.getValue().equals("")){
        		value = del + "\""+engine.getValue()+"\"";
        	}
			line = engine.getNumber() + del + engine.getRoad() + del
					+ engineModel + del + engine.getLength() + del
					+ engine.getOwner() + del + engine.getBuilt() + del
					+ engineLocationName + ",-," + engineTrackName
					+ value;
			fileOut.println(line);
        }
		fileOut.flush();
		fileOut.close();
		log.info("Exported "+engineList.size()+" engines to file "+defaultOperationsFilename());
		JOptionPane.showMessageDialog(null,"Exported "+engineList.size()+" engines to file "+defaultOperationsFilename(),
				"Export complete",
				JOptionPane.INFORMATION_MESSAGE);

    }
    
    // Operation files always use the same directory
    public static String defaultOperationsFilename() { 
    	return OperationsSetupXml.getFileLocation()+OperationsSetupXml.getOperationsDirectoryName()+File.separator+getOperationsFileName();
    }

    public static void setOperationsFileName(String name) { OperationsFileName = name; }
    public static String getOperationsFileName(){
    	return OperationsFileName;
    }
    private static String OperationsFileName = "ExportOperationsEngineRoster.csv";
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ExportEngines.class.getName());


}
