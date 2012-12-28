// BackupBase.java

package jmri.jmrit.operations.setup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

import jmri.jmrit.XmlFile;
import jmri.jmrit.operations.OperationsXml;

/**
 * Base class for backing up and restoring Operations working files. Derived
 * classes implement specifics for working with different backup set stores,
 * such as Automatic and Default backups.
 * 
 * @author Gregory Madsen Copyright (C) 2012
 */
public abstract class BackupBase {
	static org.apache.log4j.Logger log = org.apache.log4j.Logger
			.getLogger(BackupBase.class.getName());

	// Just for testing......
	// If this is not null, it will be thrown to simulate various IO exceptions
	// that are hard to reproduce when running tests..
	public RuntimeException testException = null;

	// The root directory for all Operations files, usually
	// "user / name / JMRI / operations"
	protected File _operationsRoot = null;

	// This will be set to the appropriate backup root directory from the
	// derived
	// classes, as their constructor will fill in the correct directory.
	protected File _backupRoot;

	public File getBackupRoot() {
		return _backupRoot;
	}

	// These constitute the set of files for a complete backup set.
	private String[] _backupSetFileNames = new String[] { "Operations.xml",
			"OperationsCarRoster.xml", "OperationsEngineRoster.xml",
			"OperationsLocationRoster.xml", "OperationsRouteRoster.xml",
			"OperationsTrainRoster.xml" };

	private String _demoPanelFileName = "Operations Demo Panel.xml";

	public String[] getBackupSetFileNames() {
		return _backupSetFileNames.clone();
	}

	/**
	 * Creates a BackupBase instance and initializes the Operations root
	 * directory to its normal value.
	 * 
	 * @throws IOException
	 */
	protected BackupBase(String rootName) {
		// A root directory name for the backups must be supplied, which will be
		// from the derived class constructors.
		if (rootName == null)
			throw new IllegalArgumentException("Backup root name can't be null");

		_operationsRoot = new File(XmlFile.prefsDir(),
				OperationsXml.getOperationsDirectoryName());

		_backupRoot = new File(_operationsRoot, rootName);

		// Make sure it exists
		if (!_backupRoot.exists()) {
			Boolean ok = _backupRoot.mkdirs();
			if (!ok) {
				throw new RuntimeException("Unable to make directory: "
						+ _backupRoot.getAbsolutePath());
			}
		}

		// We maybe want to check if it failed and throw an exception.
	}

	/**
	 * Backs up Operations files to the named backup set under the backup root
	 * directory.
	 * 
	 * @param setName
	 *            The name of the new backup set
	 * @throws Exception
	 */
	public void backupFilesToSetName(String setName) throws IOException {
		validateNotNullOrEmpty(setName);

		copyBackupSet(_operationsRoot, new File(_backupRoot, setName));
	}

	private void validateNotNullOrEmpty(String s) {
		if (s == null || s.trim().length() == 0) {
			throw new IllegalArgumentException(
					"string cannot be null or empty.");
		}

	}

	/**
	 * Creates backup files for the directory specified. Assumes that
	 * backupDirectory is a fully qualified path where the individual files will
	 * be created. This will backup files to any directory which does not have
	 * to be part of the JMRI hierarchy.
	 * 
	 * @param backupDirectory
	 *            The directory to use for the backup.
	 * @throws Exception
	 */
	public void backupFilesToDirectory(File backupDirectory) throws IOException {
		copyBackupSet(_operationsRoot, backupDirectory);
	}

	/**
	 * Returns a list of the Backup Sets under the backup root.
	 * 
	 */
	public String[] getBackupSetList() {
		String[] setList = _backupRoot.list();

		return setList;
	}

	public File[] getBackupSetDirs() {
		// Returns a list of File objects for the backup sets in the
		// backup store.
		// Not used at the moment, and can probably be removed in favor of
		// getBackupSets()
		File[] dirs = _backupRoot.listFiles();

		return dirs;
	}

	public BackupSet[] getBackupSets() {
		// This is a bit of a kludge for now, until I learn more about dynamic
		// sets
		File[] dirs = _backupRoot.listFiles();
		BackupSet[] sets = new BackupSet[dirs.length];

		for (int i = 0; i < dirs.length; i++) {
			sets[i] = new BackupSet(dirs[i]);
		}

		return sets;
	}

	/**
	 * Check to see if the given backup set already exists in the backup store.
	 * 
	 * @param setName
	 * @return true if it exists
	 */
	public boolean checkIfBackupSetExists(String setName) {
		// This probably needs to be simplified, but leave for now.
		validateNotNullOrEmpty(setName);

		try {
			File file = new File(_backupRoot, setName);

			if (file.exists())
				return true;
		} catch (Exception e) {
			log.error("Exception during backup set directory exists check");
		}
		return false;
	}

	/**
	 * Restores a Backup Set with the given name from the backup store.
	 * 
	 * @param setName
	 * @throws Exception
	 */
	public void restoreFilesFromSetName(String setName) throws IOException {
		copyBackupSet(new File(_backupRoot, setName), _operationsRoot);
	}

	/**
	 * Restores a Backup Set from the given directory.
	 * 
	 * @param directory
	 * @throws Exception
	 */
	public void restoreFilesFromDirectory(File directory) throws IOException {
		log.debug("restoring files from directory "
				+ directory.getAbsolutePath());

		copyBackupSet(directory, _operationsRoot);
	}

	/**
	 * Copies a complete set of Operations files from one directory to another
	 * directory. Usually used to copy to or from a backup location. Creates the
	 * destination directory if it does not exist.
	 * 
	 * Only copies files that are included in the list of Operations files.
	 * 
	 * @param sourceDir
	 * @param destDir
	 * @throws IOException
	 * @throws SetupException
	 */
	public void copyBackupSet(File sourceDir, File destDir) throws IOException {
		log.debug("copying backup set from: " + sourceDir + " to: " + destDir);

		if (!sourceDir.exists())
			// This throws an exception, as the dir should
			// exist.
			throw new IOException("Backup Set source directory: "
					+ sourceDir.getAbsolutePath() + " does not exist");

		// See how many Operations files we have. If they are all there, carry
		// on, if there are none, just return, any other number MAY be an error,
		// so just log it.
		// We can't throw an exception, as this CAN be a valid state.
		// There is no way to tell if a missing file is an error or not the way
		// the files are created.

		int sourceCount = getSourceFileCount(sourceDir);

		if (sourceCount == 0) {
			log.debug("No source files found in " + sourceDir.getAbsolutePath()
					+ ", so skipping copy.");
			return;
		}

		if (sourceCount != _backupSetFileNames.length) {
			log.warn("Only " + sourceCount
					+ " file(s) found in directory "
					+ sourceDir.getAbsolutePath());
			// throw new IOException("Only " + sourceCount
			// + " file(s) found in directory "
			// + sourceDir.getAbsolutePath());
		}

		// Ensure destination directory exists
		if (!destDir.exists()) {
			// Note that mkdirs does NOT throw an exception on error.
			// It will return false if the directory already exists.
			boolean result = destDir.mkdirs();

			if (!result) {
				// This needs to use a better Exception class.....
				throw new IOException(
						destDir.getAbsolutePath()
								+ " (Could not create all or part of the Backup Set path)");
			}
		}

		// Just copy the specific Operations files, now that we know they are
		// all there.
		for (String name : _backupSetFileNames) {
			log.debug("copying file: " + name);

			File src = new File(sourceDir, name);

			if (src.exists()) {
				File dst = new File(destDir, name);

				FileHelper.copy(src.getAbsolutePath(), dst.getAbsolutePath(), true);
			}
			else{
				log.debug("Source file: " + src.getAbsolutePath() + " does not exist, and is not copied.");
			}
		
		}

		// Throw a test exception, if we have one.
		if (testException != null) {
			testException.fillInStackTrace();
			throw testException;
		}
	}

	/**
	 * Checks to see how many of the Operations files are present in the source
	 * directory.
	 * 
	 * @return number of files
	 */
	public int getSourceFileCount(File sourceDir) {
		int count = 0;
		Boolean exists;

		for (String name : _backupSetFileNames) {
			exists = new File(sourceDir, name).exists();
			if (exists) {
				count++;
			}
		}

		return count;
	}

	/**
	 * Reloads the demo Operations files that are distributed with JMRI.
	 * 
	 * @throws Exception
	 */
	public void loadDemoFiles() throws IOException {
		File fromDir = new File(XmlFile.xmlDir(), "demoOperations");
		copyBackupSet(fromDir, _operationsRoot);

		// and the demo panel file
		log.debug("copying file: " + _demoPanelFileName);

		File src = new File(fromDir, _demoPanelFileName);
		File dst = new File(_operationsRoot, _demoPanelFileName);

		FileHelper.copy(src.getAbsolutePath(), dst.getAbsolutePath(), true);

	}

	/**
	 * Searches for an unused directory name, based on the default base name,
	 * under the given directory. A name suffix as appended to the base name and
	 * can range from 00 to 99.
	 * 
	 * @return A backup set name that is not already in use.
	 */
	public String suggestBackupSetName() {
		// Start with a base name that is derived from today's date
		// This checks to see if the default name already exists under the given
		// backup root directory.
		// If it exists, the name is incremented by 1 up to 99 and checked
		// again.
		String baseName = getDate();
		String fullName = null;
		String [] dirNames = _backupRoot.list();

		// Check for up to 100 backup file names to see if they already exist
		for (int i = 0; i < 99; i++) {
			// Create the trial name, then see if it already exists.
			fullName = String.format("%s_%02d", baseName, i);
					
			// bug workaround for Linux/NFS, File.exists() can be cached returning the wrong results (true)
			boolean foundFileNameMatch = false;		
			for (int j = 0; j < dirNames.length; j++) {
				if (dirNames[j].equals(fullName)) {
					foundFileNameMatch = true;
					break;
				}
			}			
			if (!foundFileNameMatch)
				return fullName;

//			File testPath = new File(_backupRoot, fullName);
//
//			if (!testPath.exists()) {
//				return fullName; // Found an unused name


			// Otherwise complain and keep trying...
			log.debug("Operations backup directory: " + fullName
					+ " already exists");
		}

		// If we get here, we have tried all 100 variants without success. This
		// should probably throw an exception, but for now it just returns the
		// last file name tried.
		return fullName;
	}

	/**
	 * Reset Operations by deleting XML files, leaves directories and backup
	 * files in place.
	 */
	public void deleteOperationsFiles() {
		// Maybe this should also only delete specific files used by Operations,
		// and not just all XML files.
		File files = _operationsRoot;

		if (!files.exists())
			return;

		String[] operationFileNames = files.list();
		for (int i = 0; i < operationFileNames.length; i++) {
			// skip non-xml files
			if (!operationFileNames[i].toUpperCase().endsWith(".XML"))
				continue;
			//
			log.debug("deleting file: " + operationFileNames[i]);
			File file = new File(_operationsRoot + File.separator
					+ operationFileNames[i]);
			if (!file.delete())
				log.debug("file not deleted");
			// This should probably throw an exception if a delete fails.
		}
	}

	/**
	 * Returns the current date formatted for use as part of a Backup Set name.
	 */
	private String getDate() {
		// This could use some clean-up.... but works OK for now
		Calendar now = Calendar.getInstance();
		int month = now.get(Calendar.MONTH) + 1;
		String m = Integer.toString(month);
		if (month < 10) {
			m = "0" + Integer.toString(month);
		}
		int day = now.get(Calendar.DATE);
		String d = Integer.toString(day);
		if (day < 10) {
			d = "0" + Integer.toString(day);
		}
		String date = "" + now.get(Calendar.YEAR) + "_" + m + "_" + d;
		return date;
	}

	/**
	 * Helper class for working with Files and Paths. Should probably be moved
	 * into its own public class.
	 * 
	 * Probably won't be needed now that I discovered the File class and it can
	 * glue together paths. Need to explore it a bit more.
	 * 
	 * @author Gregory Madsen Copyright (C) 2012
	 * 
	 */
	private static class FileHelper {

		/**
		 * Copies an existing file to a new file. Overwriting a file of the same
		 * name is allowed. The destination directory must exist.
		 * 
		 * @param sourceFileName
		 * @param destFileName
		 * @param overwrite
		 * @throws IOException
		 */
		@edu.umd.cs.findbugs.annotations.SuppressWarnings(value="OBL_UNSATISFIED_OBLIGATION")
		public static void copy(String sourceFileName, String destFileName,
				Boolean overwrite) throws IOException {

			// If we can't overwrite the destination, check if the destination
			// already exists
			if (!overwrite) {
				if (new File(destFileName).exists()) {
					throw new IOException(
							"Destination file exists and overwrite is false.");
				}
			}

			InputStream source = null;
			OutputStream dest = null;

			try {
				source = new FileInputStream(sourceFileName);
				dest = new FileOutputStream(destFileName);

				byte[] buffer = new byte[1024];

				int len;

				while ((len = source.read(buffer)) > 0) {
					dest.write(buffer, 0, len);
				}
			} catch (Exception ex) {
				if (source != null)
					source.close();
				if (dest != null)
					dest.close();
				String msg = String.format("Error copying file: %s to: %s",
						sourceFileName, destFileName);
				throw new IOException(msg, ex);
			}

			source.close();
			dest.close();

			// Now update the last modified time to equal the source file.
			File src = new File(sourceFileName);
			File dst = new File(destFileName);

			Boolean ok = dst.setLastModified(src.lastModified());
			if (!ok)
				throw new RuntimeException(
						"Failed to set modified time on file: "
								+ dst.getAbsolutePath());
		}
	}

}
