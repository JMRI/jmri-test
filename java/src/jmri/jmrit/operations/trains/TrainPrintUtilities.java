// TrainPrintUtilities

package jmri.jmrit.operations.trains;

import java.awt.Color;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import jmri.jmrit.operations.setup.Setup;
import jmri.util.davidflanagan.HardcopyWriter;

/**
 * Train print utilities
 * 
 * @author Daniel Boudreau (C) 2010
 * @version $Revision$
 * 
 */
public class TrainPrintUtilities {
	
	static final String NEW_LINE = "\n";	// NOI18N

	/**
	 * Print or preview a train manifest, build report, or switch list.
	 * 
	 * @param file
	 *            File to be printed or previewed
	 * @param name
	 *            Title of document
	 * @param isPreview
	 *            true if preview
	 * @param fontName
	 *            optional font to use when printing document
	 * @param isBuildReport
	 *            true if build report
	 * @param logoURL
	 *            optional pathname for logo
	 */
	public static void printReport(File file, String name, boolean isPreview, String fontName,
			boolean isBuildReport, String logoURL) {
		printReport(file, name, isPreview, fontName, isBuildReport, logoURL, "", Setup.PORTRAIT);
	}

	/**
	 * Print or preview a train manifest, build report, or switch list.
	 * 
	 * @param file
	 *            File to be printed or previewed
	 * @param name
	 *            Title of document
	 * @param isPreview
	 *            true if preview
	 * @param fontName
	 *            optional font to use when printing document
	 * @param isBuildReport
	 *            true if build report
	 * @param logoURL
	 *            optional pathname for logo
	 * @param printerName
	 *            optional default printer name
	 */
	public static void printReport(File file, String name, boolean isPreview, String fontName,
			boolean isBuildReport, String logoURL, String printerName, String orientation) {
		// obtain a HardcopyWriter to do this
		HardcopyWriter writer = null;
		Frame mFrame = new Frame();
		boolean isLandScape = false;
		boolean printHeader = true;
		if (orientation.equals(Setup.LANDSCAPE))
			isLandScape = true;
		if (orientation.equals(Setup.HANDHELD))
			printHeader = false;
		try {
			writer = new HardcopyWriter(mFrame, name, Setup.getFontSize(), .5, .5, .5, .5,
					isPreview, printerName, isLandScape, printHeader);
		} catch (HardcopyWriter.PrintCanceledException ex) {
			log.debug("Print cancelled");
			return;
		}
		// set font
		if (!fontName.equals(""))
			writer.setFontName(fontName);

		// now get the build file to print
		BufferedReader in;
		try {
			in = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			log.debug("Build file doesn't exist");
			return;
		}
		String line = " ";

		if (!isBuildReport && (!logoURL.equals(""))) {
			ImageIcon icon = new ImageIcon(logoURL);
			writer.write(icon.getImage(), new JLabel(icon));
		}
		Color c = null;
		while (true) {
			try {
				line = in.readLine();
			} catch (IOException e) {
				log.debug("Print read failed");
				break;
			}
			if (line == null)
				break;
			// check for build report print level
			if (isBuildReport) {
				line = filterBuildReport(line, false); // no indent
				if (line.equals(""))
					continue;
				// printing the train manifest
			} else {
				// determine if line is a pickup or drop
				if ((!Setup.getPickupEnginePrefix().equals("") && line.startsWith(Setup
						.getPickupEnginePrefix()))
						|| (!Setup.getPickupCarPrefix().equals("") && line.startsWith(Setup
								.getPickupCarPrefix()))
						|| (!Setup.getSwitchListPickupCarPrefix().equals("") && line
								.startsWith(Setup.getSwitchListPickupCarPrefix()))) {
					// log.debug("found a pickup line");
					c = Setup.getPickupColor();
				} else if ((!Setup.getDropEnginePrefix().equals("") && line.startsWith(Setup
						.getDropEnginePrefix()))
						|| (!Setup.getDropCarPrefix().equals("") && line.startsWith(Setup
								.getDropCarPrefix()))
						|| (!Setup.getSwitchListDropCarPrefix().equals("") && line.startsWith(Setup
								.getSwitchListDropCarPrefix()))) {
					// log.debug("found a drop line");
					c = Setup.getDropColor();
				} else if ((!Setup.getLocalPrefix().equals("") && line.startsWith(Setup
						.getLocalPrefix()))
						|| (!Setup.getSwitchListLocalPrefix().equals("") && line.startsWith(Setup
								.getSwitchListLocalPrefix()))) {
					// log.debug("found a drop line");
					c = Setup.getLocalColor();
				} else if (!line.startsWith(TrainCommon.TAB)) {
					c = null;
				}
				if (c != null) {
					try {
						writer.write(c, line + NEW_LINE);
						continue;
					} catch (IOException e) {
						log.debug("Print write color failed");
						break;
					}
				}
			}
			try {
				writer.write(line + NEW_LINE);
			} catch (IOException e) {
				log.debug("Print write failed");
				break;
			}
		}
		// and force completion of the printing
		try {
			in.close();
		} catch (IOException e) {
			log.debug("Print close failed");
		}
		writer.close();
	}

	/**
	 * Creates a new build report file with the print detail numbers replaced by indentations. Then calls open desktop
	 * editor.
	 * 
	 * @param file
	 *            build file
	 * @param name
	 *            train name
	 */
	public static void editReport(File file, String name) {
		// make a new file with the build report levels removed
		BufferedReader in;
		try {
			in = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			log.debug("Build report file doesn't exist");
			return;
		}
		java.io.PrintWriter out;
		File buildReport = TrainManagerXml.instance().createTrainBuildReportFile(
				Bundle.getMessage("Report") + " " + name);
		try {
			out = new java.io.PrintWriter(new java.io.BufferedWriter(new java.io.FileWriter(
					buildReport)), true);
		} catch (IOException e) {
			log.error("Can not create build report file");
			return;
		}
		String line = " ";
		while (true) {
			try {
				line = in.readLine();
				if (line == null)
					break;
				line = filterBuildReport(line, Setup.isBuildReportIndentEnabled());
				if (line.equals(""))
					continue;
				out.println(line); // indent lines for each level
			} catch (IOException e) {
				log.debug("Print read failed");
				break;
			}
		}
		// and force completion of the printing
		try {
			in.close();
		} catch (IOException e) {
			log.debug("Close failed");
		}
		out.close();
		// open editor
		openDesktopEditor(buildReport);
	}

	/*
	 * Removes the print levels from the build report
	 */
	private static String filterBuildReport(String line, boolean indent) {
		String[] inputLine = line.split("\\s+");	// NOI18N
		if (inputLine.length == 0)
			return "";
		if (inputLine[0].equals(Setup.BUILD_REPORT_VERY_DETAILED + "-")
				|| inputLine[0].equals(Setup.BUILD_REPORT_DETAILED + "-")
				|| inputLine[0].equals(Setup.BUILD_REPORT_NORMAL + "-")
				|| inputLine[0].equals(Setup.BUILD_REPORT_MINIMAL + "-")) {

			if (Setup.getBuildReportLevel().equals(Setup.BUILD_REPORT_MINIMAL)) {
				if (inputLine[0].equals(Setup.BUILD_REPORT_NORMAL + "-")
						|| inputLine[0].equals(Setup.BUILD_REPORT_DETAILED + "-")
						|| inputLine[0].equals(Setup.BUILD_REPORT_VERY_DETAILED + "-")) {
					return ""; // don't print this line
				}
			}
			if (Setup.getBuildReportLevel().equals(Setup.BUILD_REPORT_NORMAL)) {
				if (inputLine[0].equals(Setup.BUILD_REPORT_DETAILED + "-")
						|| inputLine[0].equals(Setup.BUILD_REPORT_VERY_DETAILED + "-")) {
					return ""; // don't print this line
				}
			}
			if (Setup.getBuildReportLevel().equals(Setup.BUILD_REPORT_DETAILED)) {
				if (inputLine[0].equals(Setup.BUILD_REPORT_VERY_DETAILED + "-")) {
					return ""; // don't print this line
				}
			}
			// do not indent if false
			int start = 0;
			if (indent) {
				// indent lines based on level
				if (inputLine[0].equals(Setup.BUILD_REPORT_VERY_DETAILED + "-")) {
					inputLine[0] = "   ";
				} else if (inputLine[0].equals(Setup.BUILD_REPORT_DETAILED + "-")) {
					inputLine[0] = "  ";
				} else if (inputLine[0].equals(Setup.BUILD_REPORT_NORMAL + "-")) {
					inputLine[0] = " ";
				} else if (inputLine[0].equals(Setup.BUILD_REPORT_MINIMAL + "-")) {
					inputLine[0] = "";
				}
			} else {
				start = 1;
			}
			// rebuild line
			StringBuffer buf = new StringBuffer();
			for (int i = start; i < inputLine.length; i++) {
				buf.append(inputLine[i] + " ");
			}
			// blank line?
			if (buf.length() == 0)
				return " ";
			return buf.toString();
		} else {
			log.debug("ERROR first characters of build report not valid (" + line + ")");
			return "ERROR " + line;	// NOI18N
		}
	}

	/**
	 * This method uses Desktop which is supported in Java 1.6. Since we're currently limiting the code to Java 1.5,
	 * this method must be commented out.
	 */
	public static void openDesktopEditor(File file) {
		if (!java.awt.Desktop.isDesktopSupported()) {
			log.warn("desktop not supported");
			return;
		}
		java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
		if (!desktop.isSupported(java.awt.Desktop.Action.EDIT)) {
			log.warn("desktop edit not supported");
			return;
		}
		try {
			desktop.edit(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method replaces the method above for compatibility with Java 1.5.
	 */
	/*
	 * public static void openDesktopEditor(File file){
	 * log.info("Open file using editor not supported yet!  Requires Java 1.6"); String path = file.getAbsolutePath();
	 * JOptionPane.showMessageDialog(null, "Open file using editor not available, file path: "+path +
	 * "\n If you want to use this feature, download replacement jmri.jar file from:" +
	 * "\n http://home.comcast.net/~daboudreau/JMRI_JAVA1.6/jmri.jar", "Requires custom jmri.jar file and Java 1.6",
	 * JOptionPane.INFORMATION_MESSAGE); return; }
	 */

	public static JComboBox getPrinterJComboBox() {
		JComboBox box = new JComboBox();
		PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
		for (int i = 0; i < services.length; i++) {
			box.addItem(services[i].getName());
			// log.debug(services[i].getName());
		}

		// Set to default printer
		box.setSelectedItem(getDefaultPrinterName());

		return box;
	}

	public static String getDefaultPrinterName() {
		if (PrintServiceLookup.lookupDefaultPrintService() != null)
			return PrintServiceLookup.lookupDefaultPrintService().getName();
		return ""; // no default printer specified
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger
			.getLogger(TrainPrintUtilities.class.getName());
}
