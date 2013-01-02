// ImportCars.java

package jmri.jmrit.operations.rollingstock.cars;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.NumberFormat;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;

/**
 * This routine will import cars into the operation database.
 * 
 * Each field is space or comma delimited. Field order: Number Road Type Length Weight Color Owner Year Location
 * 
 * @author Dan Boudreau Copyright (C) 2008 2010 2011
 * @version $Revision$
 */
public class ImportCars extends Thread {

	static final String NEW_LINE = "\n"; // NOI18N

	CarManager manager = CarManager.instance();

	javax.swing.JLabel textLine = new javax.swing.JLabel();
	javax.swing.JLabel lineNumber = new javax.swing.JLabel();

	private int weightResults = JOptionPane.NO_OPTION; // Automatically calculate weight for car if weight entry is not
														// found
	private boolean autoCalculate = true;
	private boolean askAutoCreateTypes = true;
	private boolean askAutoCreateLocations = true;
	private boolean askAutoCreateTracks = true;
	private boolean askAutoLocationType = true;
	private boolean askAutoIncreaseTrackLength = true;

	private boolean autoCreateTypes = false;
	private boolean autoCreateLocations = false;
	private boolean autoCreateTracks = false;
	private boolean autoAdjustLocationType = false;
	private boolean autoAdjustTrackLength = false;

	private boolean autoCreateRoads = true;
	private boolean autoCreateLengths = true;
	private boolean autoCreateColors = true;
	private boolean autoCreateOwners = true;

	// we use a thread so the status frame will work!
	public void run() {
		// Get file to read from
		JFileChooser fc = new JFileChooser(jmri.jmrit.XmlFile.userFileLocationDefault());
		fc.addChoosableFileFilter(new ImportFilter());
		int retVal = fc.showOpenDialog(null);
		if (retVal != JFileChooser.APPROVE_OPTION)
			return; // canceled
		if (fc.getSelectedFile() == null)
			return; // canceled
		File f = fc.getSelectedFile();
		BufferedReader in;
		try {
			in = new BufferedReader(new FileReader(f));
		} catch (FileNotFoundException e) {
			return;
		}

		// create a status frame
		JPanel ps = new JPanel();
		jmri.util.JmriJFrame fstatus = new jmri.util.JmriJFrame(Bundle.getString("ImportCars"));
		fstatus.setLocationRelativeTo(null);
		fstatus.setSize(200, 100);

		ps.add(textLine);
		ps.add(lineNumber);
		fstatus.getContentPane().add(ps);
		textLine.setText(Bundle.getString("LineNumber"));
		textLine.setVisible(true);
		lineNumber.setVisible(true);
		fstatus.setVisible(true);

		// Now read the input file
		boolean importOkay = false;
		boolean comma = false;
		int lineNum = 0;
		int carsAdded = 0;
		String line = " ";
		String carNumber;
		String carRoad;
		String carType;
		String carLength;
		String carWeight;
		String carColor = "";
		String carOwner = "";
		String carBuilt = "";
		String carLocation = "";
		String carTrack = "";
		String[] inputLine;

		// does the file name end with .csv?
		if (f.getAbsolutePath().endsWith(".csv")) { // NOI18N
			log.info("Using comma as delimiter for import cars");
			comma = true;
		}

		while (true) {
			lineNumber.setText(Integer.toString(++lineNum));
			try {
				line = in.readLine();
			} catch (IOException e) {
				break;
			}

			if (line == null) {
				importOkay = true;
				break;
			}

			// has user canceled import?
			if (!fstatus.isShowing())
				break;

			line = line.trim();
			if (log.isDebugEnabled()) {
				log.debug("Import: " + line);
			}
			if (line.equalsIgnoreCase("comma")) { // NOI18N
				log.info("Using comma as delimiter for import cars");
				comma = true;
				continue;
			}
			// use comma as delimiter if found otherwise use spaces
			if (comma)
				inputLine = parseCommaLine(line, 11);
			else
				inputLine = line.split("\\s+"); // NOI18N

			if (inputLine.length < 1 || line.equals("")) {
				log.debug("Skipping blank line");
				continue;
			}
			int base = 1;
			if (!inputLine[0].equals("")) {
				base--; // skip over any spaces at start of line
			}

			if (inputLine.length > base + 3) {

				carNumber = inputLine[base + 0];
				carRoad = inputLine[base + 1];
				carType = inputLine[base + 2];
				carLength = inputLine[base + 3];
				carWeight = "0";
				carColor = "";
				carOwner = "";
				carBuilt = "";
				carLocation = "";
				carTrack = "";

				if (inputLine.length > base + 4)
					carWeight = inputLine[base + 4];
				if (inputLine.length > base + 5)
					carColor = inputLine[base + 5];

				log.debug("Checking car number (" + carNumber + ") road (" + carRoad + ") type ("
						+ carType + ") length (" + carLength + ") weight (" + carWeight // NOI18N
						+ ") color (" + carColor + ")"); // NOI18N
				if (carNumber.length() > Control.max_len_string_road_number) {
					JOptionPane.showMessageDialog(null, MessageFormat.format(
							Bundle.getString("CarRoadNumberTooLong"), new Object[] {
									(carRoad + " " + carNumber), carNumber }), MessageFormat
							.format(Bundle.getString("carRoadNum"),
									new Object[] { Control.max_len_string_road_number + 1 }),
							JOptionPane.ERROR_MESSAGE);
					break;
				}
				if (carRoad.length() > Control.max_len_string_attibute) {
					JOptionPane.showMessageDialog(null, MessageFormat.format(
							Bundle.getString("CarRoadNameTooLong"), new Object[] {
									(carRoad + " " + carNumber), carRoad }), MessageFormat.format(
							Bundle.getString("carAttribute"),
							new Object[] { Control.max_len_string_attibute }),
							JOptionPane.ERROR_MESSAGE);
					break;
				}
				if (carType.length() > Control.max_len_string_attibute) {
					JOptionPane.showMessageDialog(null, MessageFormat.format(
							Bundle.getString("CarTypeNameTooLong"), new Object[] {
									(carRoad + " " + carNumber), carType }), MessageFormat.format(
							Bundle.getString("carAttribute"),
							new Object[] { Control.max_len_string_attibute }),
							JOptionPane.ERROR_MESSAGE);
					break;
				}
				if (!CarTypes.instance().containsName(carType)) {
					if (autoCreateTypes) {
						log.debug("Adding car type +carType");
						CarTypes.instance().addName(carType);
					} else {
						int results = JOptionPane.showConfirmDialog(
								null,
								Bundle.getString("Car")
										+ " ("
										+ carRoad
										+ " "
										+ carNumber
										+ ")"
										+ NEW_LINE
										+ MessageFormat.format(
												Bundle.getString("typeNameNotExist"),
												new Object[] { carType }), Bundle
										.getString("carAddType"), JOptionPane.YES_NO_CANCEL_OPTION);
						if (results == JOptionPane.YES_OPTION) {
							CarTypes.instance().addName(carType);
							if (askAutoCreateTypes) {
								results = JOptionPane.showConfirmDialog(null,
										Bundle.getString("DoYouWantToAutoAddCarTypes"),
										Bundle.getString("OnlyAskedOnce"),
										JOptionPane.YES_NO_OPTION);
								if (results == JOptionPane.YES_OPTION)
									autoCreateTypes = true;
							}
							askAutoCreateTypes = false;
						} else if (results == JOptionPane.CANCEL_OPTION) {
							break;
						}
					}
				}
				if (carLength.length() > Control.max_len_string_length_name) {
					JOptionPane.showMessageDialog(null, MessageFormat.format(
							Bundle.getString("CarLengthNameTooLong"), new Object[] {
									(carRoad + " " + carNumber), carLength }), MessageFormat
							.format(Bundle.getString("carAttribute"),
									new Object[] { Control.max_len_string_length_name }),
							JOptionPane.ERROR_MESSAGE);
					break;
				}
				if (carLength.equals("")) {
					log.debug("Car (" + carRoad + " " + carNumber + ") length not specified");
					JOptionPane.showMessageDialog(null, MessageFormat.format(
							Bundle.getString("CarLengthNotSpecified"), new Object[] { (carRoad
									+ " " + carNumber) }), Bundle.getString("CarLengthMissing"),
							JOptionPane.ERROR_MESSAGE);
					break;
				}
				try {
					Integer.parseInt(carLength);
				} catch (NumberFormatException e) {
					JOptionPane.showMessageDialog(null, MessageFormat.format(
							Bundle.getString("CarLengthNameNotNumber"), new Object[] {
									(carRoad + " " + carNumber), carLength }), Bundle
							.getString("CarLengthMissing"), JOptionPane.ERROR_MESSAGE);
					break;
				}
				if (carWeight.length() > Control.max_len_string_weight_name) {
					JOptionPane.showMessageDialog(null, MessageFormat.format(
							Bundle.getString("CarWeightNameTooLong"), new Object[] {
									(carRoad + " " + carNumber), carWeight }), MessageFormat
							.format(Bundle.getString("carAttribute"),
									new Object[] { Control.max_len_string_weight_name }),
							JOptionPane.ERROR_MESSAGE);
					break;
				}
				if (carColor.length() > Control.max_len_string_attibute) {
					JOptionPane.showMessageDialog(null, MessageFormat.format(
							Bundle.getString("CarColorNameTooLong"), new Object[] {
									(carRoad + " " + carNumber), carColor }), MessageFormat.format(
							Bundle.getString("carAttribute"),
							new Object[] { Control.max_len_string_attibute }),
							JOptionPane.ERROR_MESSAGE);
					break;
				}
				// calculate car weight if "0"
				if (carWeight.equals("0")) {
					try {
						double doubleCarLength = Double.parseDouble(carLength) * 12
								/ Setup.getScaleRatio();
						double doubleCarWeight = (Setup.getInitalWeight() + doubleCarLength
								* Setup.getAddWeight()) / 1000;
						NumberFormat nf = NumberFormat.getNumberInstance();
						nf.setMaximumFractionDigits(1);
						carWeight = nf.format(doubleCarWeight); // car weight in ounces.
					} catch (NumberFormatException e) {
						JOptionPane.showMessageDialog(null, Bundle.getString("carLengthMustBe"),
								Bundle.getString("carWeigthCanNot"), JOptionPane.ERROR_MESSAGE);
					}
				}
				Car c = manager.getByRoadAndNumber(carRoad, carNumber);
				if (c != null) {
					log.info("Can not add, car number (" + carNumber + ") road (" + carRoad
							+ ") already exists!"); // NOI18N
				} else {
					if (inputLine.length > base + 6) {
						carOwner = inputLine[base + 6];
						if (carOwner.length() > Control.max_len_string_attibute) {
							JOptionPane.showMessageDialog(null, MessageFormat.format(
									Bundle.getString("CarOwnerNameTooLong"), new Object[] {
											(carRoad + " " + carNumber), carOwner }), MessageFormat
									.format(Bundle.getString("carAttribute"),
											new Object[] { Control.max_len_string_attibute }),
									JOptionPane.ERROR_MESSAGE);
							break;
						}
					}
					if (inputLine.length > base + 7) {
						carBuilt = inputLine[base + 7];
						if (carBuilt.length() > Control.max_len_string_built_name) {
							JOptionPane.showMessageDialog(null, MessageFormat.format(
									Bundle.getString("CarBuiltNameTooLong"), new Object[] {
											(carRoad + " " + carNumber), carBuilt }), MessageFormat
									.format(Bundle.getString("carAttribute"),
											new Object[] { Control.max_len_string_built_name }),
									JOptionPane.ERROR_MESSAGE);
							break;
						}
					}
					if (inputLine.length > base + 8) {
						carLocation = inputLine[base + 8];

					}
					// Location name can be one to three words
					if (inputLine.length > base + 9) {
						if (!inputLine[base + 9].equals("-")) {
							carLocation = carLocation + " " + inputLine[base + 9];
							if (inputLine.length > base + 10) {
								if (!inputLine[base + 10].equals("-"))
									carLocation = carLocation + " " + inputLine[base + 10];
							}
							// create track location if there's one
						}
						boolean foundDash = false;
						for (int i = base + 9; i < inputLine.length; i++) {
							if (inputLine[i].equals("-")) {
								foundDash = true;
								if (inputLine.length > i + 1)
									carTrack = inputLine[++i];
							} else if (foundDash)
								carTrack = carTrack + " " + inputLine[i];
						}
						if (carTrack == null)
							carTrack = "";
						log.debug("Car (" + carRoad + " " + carNumber + ") has track (" + carTrack
								+ ")");
					}

					if (carLocation.length() > Control.max_len_string_location_name) {
						JOptionPane.showMessageDialog(null, MessageFormat.format(
								Bundle.getString("CarLocationNameTooLong"), new Object[] {
										(carRoad + " " + carNumber), carLocation }), MessageFormat
								.format(Bundle.getString("carAttribute"),
										new Object[] { Control.max_len_string_location_name }),
								JOptionPane.ERROR_MESSAGE);
						break;
					}
					if (carTrack.length() > Control.max_len_string_track_name) {
						JOptionPane.showMessageDialog(null, MessageFormat.format(
								Bundle.getString("CarTrackNameTooLong"), new Object[] {
										(carRoad + " " + carNumber), carTrack }), MessageFormat
								.format(Bundle.getString("carAttribute"),
										new Object[] { Control.max_len_string_track_name }),
								JOptionPane.ERROR_MESSAGE);
						break;
					}
					Location l = LocationManager.instance().getLocationByName(carLocation);
					Track sl = null;
					if (l == null && !carLocation.equals("")) {
						if (autoCreateLocations) {
							log.debug("Create location (" + carLocation + ")");
							l = LocationManager.instance().newLocation(carLocation);
						} else {
							JOptionPane.showMessageDialog(null, MessageFormat.format(
									Bundle.getString("CarLocationDoesNotExist"), new Object[] {
											(carRoad + " " + carNumber), carLocation }), Bundle
									.getString("carLocation"), JOptionPane.ERROR_MESSAGE);
							int results = JOptionPane.showConfirmDialog(null, MessageFormat.format(
									Bundle.getString("DoYouWantToCreateLoc"),
									new Object[] { carLocation }), Bundle.getString("carLocation"),
									JOptionPane.YES_NO_OPTION);
							if (results == JOptionPane.YES_OPTION) {
								log.debug("Create location (" + carLocation + ")");
								l = LocationManager.instance().newLocation(carLocation);
								if (askAutoCreateLocations) {
									results = JOptionPane.showConfirmDialog(null,
											Bundle.getString("DoYouWantToAutoCreateLoc"),
											Bundle.getString("OnlyAskedOnce"),
											JOptionPane.YES_NO_OPTION);
									if (results == JOptionPane.YES_OPTION)
										autoCreateLocations = true;
								}
								askAutoCreateLocations = false;
							} else {
								break;
							}
						}
					}
					if (l != null && !carTrack.equals("")) {
						sl = l.getTrackByName(carTrack, null);
						if (sl == null) {
							if (autoCreateTracks) {
								if (l.getLocationOps() == Location.NORMAL) {
									log.debug("Create 1000 foot yard track (" + carTrack + ")");
									sl = l.addTrack(carTrack, Track.YARD);
								} else {
									log.debug("Create 1000 foot staging track (" + carTrack + ")");
									sl = l.addTrack(carTrack, Track.STAGING);
								}
								sl.setLength(1000);
							} else {
								JOptionPane.showMessageDialog(null, MessageFormat.format(
										Bundle.getString("CarTrackDoesNotExist"),
										new Object[] { (carRoad + " " + carNumber), carTrack,
												carLocation }), Bundle.getString("carTrack"),
										JOptionPane.ERROR_MESSAGE);
								int results = JOptionPane.showConfirmDialog(null, MessageFormat
										.format(Bundle.getString("DoYouWantToCreateTrack"),
												new Object[] { carTrack, carLocation }), Bundle
										.getString("carTrack"), JOptionPane.YES_NO_OPTION);
								if (results == JOptionPane.YES_OPTION) {
									if (l.getLocationOps() == Location.NORMAL) {
										log.debug("Create 1000 foot yard track (" + carTrack + ")");
										sl = l.addTrack(carTrack, Track.YARD);
									} else {
										log.debug("Create 1000 foot staging track (" + carTrack
												+ ")");
										sl = l.addTrack(carTrack, Track.STAGING);
									}
									sl.setLength(1000);
									if (askAutoCreateTracks) {
										results = JOptionPane.showConfirmDialog(null,
												Bundle.getString("DoYouWantToAutoCreateTrack"),
												Bundle.getString("OnlyAskedOnce"),
												JOptionPane.YES_NO_OPTION);
										if (results == JOptionPane.YES_OPTION)
											autoCreateTracks = true;
										askAutoCreateTracks = false;
									}
								} else {
									break;
								}
							}
						}
					}

					log.debug("Add car (" + carRoad + " " + carNumber + ") owner (" + carOwner
							+ ") built (" + carBuilt + ") location (" + carLocation + ", " // NOI18N
							+ carTrack + ")");
					Car car = manager.newCar(carRoad, carNumber);
					car.setType(carType);
					car.setLength(carLength);
					car.setWeight(carWeight);
					car.setColor(carColor);
					car.setOwner(carOwner);
					car.setBuilt(carBuilt);
					carsAdded++;

					car.setCaboose(carType.equals("Caboose"));

					// add new roads
					if (!CarRoads.instance().containsName(carRoad)) {
						if (autoCreateRoads) {
							log.debug("add car road " + carRoad);
							CarRoads.instance().addName(carRoad);
						}
					}

					// add new lengths
					if (!CarLengths.instance().containsName(carLength)) {
						if (autoCreateLengths) {
							log.debug("add car length " + carLength);
							CarLengths.instance().addName(carLength);
						}
					}

					// add new colors
					if (!CarColors.instance().containsName(carColor)) {
						if (autoCreateColors) {
							log.debug("add car color " + carColor);
							CarColors.instance().addName(carColor);
						}
					}

					// add new owners
					if (!CarOwners.instance().containsName(carOwner)) {
						if (autoCreateOwners) {
							log.debug("add car owner " + carOwner);
							CarOwners.instance().addName(carOwner);
						}
					}

					if (car.getWeight().equals("")) {
						log.debug("Car (" + carRoad + " " + carNumber + ") weight not specified");
						if (weightResults != JOptionPane.CANCEL_OPTION) {
							weightResults = JOptionPane.showOptionDialog(
									null,
									MessageFormat.format(Bundle.getString("CarWeightNotFound"),
											new Object[] { (carRoad + " " + carNumber) }),
									Bundle.getString("CarWeightMissing"),
									JOptionPane.YES_NO_CANCEL_OPTION,
									JOptionPane.INFORMATION_MESSAGE,
									null,
									new Object[] { Bundle.getString("ButtonYes"),
											Bundle.getString("ButtonNo"),
											Bundle.getString("ButtonDontShow") },
									autoCalculate ? Bundle.getString("ButtonYes") : Bundle
											.getString("ButtonNo"));
						}
						if (weightResults == JOptionPane.NO_OPTION)
							autoCalculate = false;
						if (weightResults == JOptionPane.YES_OPTION || autoCalculate == true
								&& weightResults == JOptionPane.CANCEL_OPTION) {
							autoCalculate = true;
							try {
								double carLen = Double.parseDouble(car.getLength()) * 12
										/ Setup.getScaleRatio();
								double carWght = (Setup.getInitalWeight() + carLen
										* Setup.getAddWeight()) / 1000;
								NumberFormat nf = NumberFormat.getNumberInstance();
								nf.setMaximumFractionDigits(1);
								car.setWeight(nf.format(carWght)); // car weight in ounces.
								int tons = (int) (carWght * Setup.getScaleTonRatio());
								// adjust weight for caboose
								if (car.isCaboose())
									tons = (int) (Double.parseDouble(car.getLength()) * .9); // .9 tons/foot
								car.setWeightTons(Integer.toString(tons));
							} catch (NumberFormatException e) {
								JOptionPane.showMessageDialog(null,
										Bundle.getString("carLengthMustBe"),
										Bundle.getString("carWeigthCanNot"),
										JOptionPane.ERROR_MESSAGE);
							}
						}
					}
					if (l != null && sl != null) {
						String status = car.setLocation(l, sl);
						if (!status.equals(Track.OKAY)) {
							log.debug("Can't set car's location because of " + status);
							if (!autoAdjustLocationType) {
								JOptionPane.showMessageDialog(null, MessageFormat.format(
										Bundle.getString("CanNotSetCarAtLocation"), new Object[] {
												(carRoad + " " + carNumber), carType, carLocation,
												carTrack, status }), Bundle
										.getString("rsCanNotLoc"), JOptionPane.ERROR_MESSAGE);
							}
							if (status.contains(Track.TYPE)) {
								if (autoAdjustLocationType) {
									l.addTypeName(carType);
									sl.addTypeName(carType);
									status = car.setLocation(l, sl);
								} else {
									int results = JOptionPane
											.showConfirmDialog(null,
													MessageFormat.format(Bundle
															.getString("DoYouWantToAllowService"),
															new Object[] { carLocation, carTrack,
																	(carRoad + " " + carNumber),
																	carType }), Bundle
															.getString("ServiceCarType"),
													JOptionPane.YES_NO_OPTION);
									if (results == JOptionPane.YES_OPTION) {
										l.addTypeName(carType);
										sl.addTypeName(carType);
										status = car.setLocation(l, sl);
										log.debug("Set car's location status: " + status);
										if (askAutoLocationType) {
											results = JOptionPane.showConfirmDialog(null, Bundle
													.getString("DoYouWantToAutoAdjustLocations"),
													Bundle.getString("OnlyAskedOnce"),
													JOptionPane.YES_NO_OPTION);
											if (results == JOptionPane.YES_OPTION)
												autoAdjustLocationType = true;
											askAutoLocationType = false;
										}
									} else {
										break;
									}
								}
							}
							if (status.contains(Track.LENGTH)) {
								if (autoAdjustTrackLength) {
									sl.setLength(sl.getLength() + 1000);
									status = car.setLocation(l, sl);
									log.debug("Set track length status: " + status);
								} else {
									int results = JOptionPane.showConfirmDialog(null, MessageFormat
											.format(Bundle.getString("DoYouWantIncreaseLength"),
													new Object[] { carTrack }), Bundle
											.getString("TrackLength"), JOptionPane.YES_NO_OPTION);
									if (results == JOptionPane.YES_OPTION) {
										sl.setLength(sl.getLength() + 1000);
										status = car.setLocation(l, sl);
										log.debug("Set track length status: " + status);
										if (askAutoIncreaseTrackLength) {
											results = JOptionPane.showConfirmDialog(null, Bundle
													.getString("DoYouWantToAutoAdjustTrackLength"),
													Bundle.getString("OnlyAskedOnce"),
													JOptionPane.YES_NO_OPTION);
											if (results == JOptionPane.YES_OPTION)
												autoAdjustTrackLength = true;
											askAutoIncreaseTrackLength = false;
										}
									} else {
										break;
									}
								}
							}
							if (!status.equals(Track.OKAY)) {
								int results = JOptionPane.showConfirmDialog(null, MessageFormat
										.format(Bundle.getString("DoYouWantToForceCar"),
												new Object[] { (carRoad + " " + carNumber),
														carLocation, carTrack }), Bundle
										.getString("OverRide"), JOptionPane.YES_NO_OPTION);
								if (results == JOptionPane.YES_OPTION) {
									car.setLocation(l, sl, true); // force car
								} else {
									break;
								}
							}
						}
					} else {
						// log.debug("No location for car ("+carRoad+" "+carNumber+")");
					}
				}
			} else if (!line.equals("")) {
				log.info("Car import line " + lineNum + " missing attributes: " + line);
				JOptionPane.showMessageDialog(
						null,
						MessageFormat.format(Bundle.getString("ImportMissingAttributes"),
								new Object[] { lineNum })
								+ NEW_LINE
								+ line
								+ NEW_LINE
								+ Bundle.getString("ImportMissingAttributes2"), Bundle
								.getString("CarAttributeMissing"), JOptionPane.ERROR_MESSAGE);
				break;
			}
		}
		try {
			in.close();
		} catch (IOException e) {
		}

		// kill status panel
		fstatus.dispose();

		if (importOkay) {
			JOptionPane.showMessageDialog(null, MessageFormat.format(
					Bundle.getString("ImportCarsAdded"), new Object[] { carsAdded }), Bundle
					.getString("SuccessfulImport"), JOptionPane.INFORMATION_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(null, MessageFormat.format(
					Bundle.getString("ImportCarsAdded"), new Object[] { carsAdded }), Bundle
					.getString("ImportFailed"), JOptionPane.ERROR_MESSAGE);
		}
	}

	protected String[] parseCommaLine(String line, int arraySize) {
		String[] outLine = new String[arraySize];
		if (line.contains("\"")) { // NOI18N
			// log.debug("line number "+lineNum+" has escape char \"");
			String[] parseLine = line.split(",");
			int j = 0;
			for (int i = 0; i < parseLine.length; i++) {
				if (parseLine[i].contains("\"")) { // NOI18N
					StringBuilder sb = new StringBuilder(parseLine[i++]);
					sb.deleteCharAt(0); // delete the "
					outLine[j] = sb.toString();
					while (i < parseLine.length) {
						if (parseLine[i].contains("\"")) { // NOI18N
							sb = new StringBuilder(parseLine[i]);
							sb.deleteCharAt(sb.length() - 1); // delete the "
							outLine[j] = outLine[j] + "," + sb.toString();
							// log.debug("generated string: "+outLine[j]);
							j++;
							break; // done!
						} else {
							outLine[j] = outLine[j] + "," + parseLine[i++];
						}
					}

				} else {
					// log.debug("outLine: "+parseLine[i]);
					outLine[j++] = parseLine[i];
				}
			}
		} else {
			outLine = line.split(",");
		}
		return outLine;
	}

	protected static class ImportFilter extends javax.swing.filechooser.FileFilter {

		public boolean accept(File f) {
			if (f.isDirectory())
				return true;
			String name = f.getName();
			if (name.matches(".*\\.txt")) // NOI18N
				return true;
			if (name.matches(".*\\.csv")) // NOI18N
				return true;
			else
				return false;
		}

		public String getDescription() {
			return Bundle.getString("Text&CSV");
		}
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ImportCars.class
			.getName());
}
