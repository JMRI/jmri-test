// Backup.java

package jmri.jmrit.operations.setup;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;

import jmri.jmrit.XmlFile;
import jmri.jmrit.operations.locations.LocationManagerXml;
import jmri.jmrit.operations.rollingstock.cars.CarManagerXml;
import jmri.jmrit.operations.rollingstock.engines.EngineManagerXml;
import jmri.jmrit.operations.routes.RouteManagerXml;
import jmri.jmrit.operations.trains.TrainManagerXml;

/**
 * Backs up operation files. Creates the "backups" and date directories along
 * with backup files in the operations directory.
 * 
 * @author Daniel Boudreau Copyright (C) 2008
 * @version $Revision: 1.1 $
 */
public class Backup extends XmlFile {

	public Backup() {
	}

	public void backupFiles() {
		try {
			if (!checkFile(fullBackupFilename(OperationsXml.instance()
					.getOperationsFileName()))) {
				// The file/directory does not exist, create it before writing
				File file = new java.io.File(fullBackupFilename(null));
				File parentDir = file.getParentFile();
				if (!parentDir.exists()) {
					if (!parentDir.mkdirs()) {
						log.error("backup directory not created");
					}
				}
			}
			OperationsXml.instance().writeFile(
					fullBackupFilename(OperationsXml.instance()
							.getOperationsFileName()));
			CarManagerXml.instance().writeFile(
					fullBackupFilename(CarManagerXml.instance()
							.getOperationsFileName()));
			EngineManagerXml.instance().writeFile(
					fullBackupFilename(EngineManagerXml.instance()
							.getOperationsFileName()));
			TrainManagerXml.instance().writeFile(
					fullBackupFilename(TrainManagerXml.instance()
							.getOperationsFileName()));
			LocationManagerXml.instance().writeFile(
					fullBackupFilename(LocationManagerXml.instance()
							.getOperationsFileName()));
			RouteManagerXml.instance().writeFile(
					fullBackupFilename(RouteManagerXml.instance()
							.getOperationsFileName()));
		} catch (Exception e) {
			log.error("Exception while making backup, may not be complete: "
					+ e);
		}
	}

	public String[] getBackupList() {
		String[] backupDirectoryNames = {"<Empty>"};
		try {
			File file = new File(backupDirectory);
			if (!file.exists()) {
				log.error("backup directory does not exist");
				return backupDirectoryNames;
			}
			
			backupDirectoryNames = file.list();
			
		} catch (Exception e) {
			log.error("Exception while making backup list, may not be complete: "
					+ e);
		}
		return backupDirectoryNames;
	}
	
	/**
	 * Copies operation files from directoryName
	 * @param directoryName
	 * @return true if successful, false if not.
	 */
	public boolean restore(String directoryName) {
		try {
			File file = new File(backupDirectory + File.separator + directoryName);
			if (!file.exists())
				return false;
			String[] operationFileNames = file.list();
			// check for at least 6 operation files
			if (operationFileNames.length < 6){
				log.error("Only "+operationFileNames.length+" files found in directory "+backupDirectory + File.separator + directoryName);
				return false;
			}
			// TODO check for the correct operation file names
			for (int i = 0; i < operationFileNames.length; i++) {
				log.debug("found file: " + operationFileNames[i]);
				file = new File(backupDirectory + File.separator + directoryName
						+ File.separator + operationFileNames[i]);
				File fileout = new File(operationsDirectory + File.separator + operationFileNames[i]);

				FileReader in = new FileReader(file);
				FileWriter out = new FileWriter(fileout);
				int c;

				while ((c = in.read()) != -1)
					out.write(c);

				in.close();
				out.close();
			}
			return true;
		} catch (Exception e) {
			log.error("Exception while restoring operations files, may not be complete: "
					+ e);
			return false; 
		}
	}

	private String fullBackupFilename(String name) {
		return backupDirectory + File.separator + date() + File.separator + name;
	}

	private String backupDirectory = XmlFile.prefsDir() + "operations" + File.separator + "backups";

	private String operationsDirectory = XmlFile.prefsDir() + "operations";
	
	private String date() {
		Calendar now = Calendar.getInstance();
		String date = "" + now.get(Calendar.YEAR) + "_"
				+ (now.get(Calendar.MONTH) + 1) + "_" + now.get(Calendar.DATE);
		return date;
	}

	static org.apache.log4j.Category log = org.apache.log4j.Category
			.getInstance(Backup.class.getName());
}
