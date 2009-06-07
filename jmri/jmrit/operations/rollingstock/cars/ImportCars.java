// ImportCars.java

package jmri.jmrit.operations.rollingstock.cars;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.setup.Control;

/**
 * This routine will import cars into the operation database.
 * 
 * Each field is space delimited.  Field order:
 * Number Road Type Length Weight Color Owner Year Location
 * Note that all fields must be single words except for Location.
 * @author Dan Boudreau Copyright (C) 2008
 * @version $Revision: 1.8 $
 */
public class ImportCars extends Thread {
	
	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.rollingstock.cars.JmritOperationsCarsBundle");
	
	CarManager manager = CarManager.instance();
	
	javax.swing.JLabel textLine = new javax.swing.JLabel();
	javax.swing.JLabel lineNumber = new javax.swing.JLabel();
	
	// we use a thread so the status frame will work!
	public void run() {
		// Get file to read from
		JFileChooser fc = new JFileChooser(jmri.jmrit.XmlFile.userFileLocationDefault());
		fc.addChoosableFileFilter(new textFilter());
		int retVal = fc.showOpenDialog(null);
		if (retVal != JFileChooser.APPROVE_OPTION)
			return; // cancelled
		if (fc.getSelectedFile() == null)
			return; // cancelled
		File f = fc.getSelectedFile();
		BufferedReader in;
		try {
			in = new BufferedReader(new FileReader(f));
		} catch (FileNotFoundException e) {
			return;
		}
		
		// create a status frame
	   	JPanel ps = new JPanel();
	   	jmri.util.JmriJFrame fstatus = new jmri.util.JmriJFrame("Import cars");
	   	fstatus.setLocationRelativeTo(null);
	   	fstatus.setSize (200,100);

	   	ps.add (textLine);
	   	ps.add(lineNumber);
	   	fstatus.getContentPane().add (ps);
		textLine.setText("Line number: ");
        textLine.setVisible(true);
        lineNumber.setVisible(true);
		fstatus.setVisible (true);

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
		String carColor ="";
		String carOwner ="";
		String carBuilt ="";
		String carLocation ="";
		String carTrack ="";

		while (true) {
			lineNumber.setText(Integer.toString(++lineNum));
			try {
				line = in.readLine();
			} catch (IOException e) {
				break;
			}

			if (line == null){
				importOkay = true;
				break;
			}

			line = line.trim();
			if (log.isDebugEnabled()) {
				log.debug("Import: " + line);
			}
			if (line.equalsIgnoreCase("comma")){
				log.info("Using comma as delimiter for import cars");
				comma = true;
			}
			// use comma as delimiter if found otherwise use spaces
			String[] inputLine;
			if (comma)
				inputLine = line.split(",");
			else
				inputLine = line.split("\\s+");
			
			if (inputLine.length < 1){
				log.debug("Skipping blank line");
				continue;
			}
			int base = 1;
			if (!inputLine[0].equals("")){
				base--;		// skip over any spaces at start of line
			}

			if (inputLine.length > base+5){

				carNumber = inputLine[base+0];
				carRoad = inputLine[base+1];
				carType = inputLine[base+2];
				carLength = inputLine[base+3];
				carWeight = inputLine[base+4];
				carColor = inputLine[base+5];
				carOwner ="";
				carBuilt ="";
				carLocation ="";
				carTrack ="";

				log.debug("Checking car number ("+carNumber+") road ("+carRoad+ ") type ("+carType+ ") length ("+carLength+") weight ("+carWeight+") color ("+carColor+")" );
				if (carNumber.length() > 10){
					JOptionPane.showMessageDialog(null, 
							"Car ("+carRoad+" "+carNumber+") road number ("+carNumber+") too long!",
							rb.getString("carRoadNum"),
							JOptionPane.ERROR_MESSAGE);
					break;
				}
				if (carRoad.length() > Control.MAX_LEN_STRING_ATTRIBUTE){
					JOptionPane.showMessageDialog(null, 
							"Car ("+carRoad+" "+carNumber+") road name ("+carRoad+") too long!",
							MessageFormat.format(rb.getString("carAttribute"),new Object[]{Control.MAX_LEN_STRING_ATTRIBUTE}),
							JOptionPane.ERROR_MESSAGE);
					break;
				}
				if (carType.length() > Control.MAX_LEN_STRING_ATTRIBUTE){
					JOptionPane.showMessageDialog(null, 
							"Car ("+carRoad+" "+carNumber+") type ("+carType+") too long!",
							MessageFormat.format(rb.getString("carAttribute"),new Object[]{Control.MAX_LEN_STRING_ATTRIBUTE}),
							JOptionPane.ERROR_MESSAGE);
					break;
				}
				if (carLength.length() > 4){
					JOptionPane.showMessageDialog(null, 
							"Car ("+carRoad+" "+carNumber+") length ("+carLength+") too long!",
							rb.getString("carAttribute5"),
							JOptionPane.ERROR_MESSAGE);
					break;
				}
				if (carWeight.length() > 4){
					JOptionPane.showMessageDialog(null, 
							"Car ("+carRoad+" "+carNumber+") weight ("+carWeight+") too long!",
							rb.getString("carAttribute5"),
							JOptionPane.ERROR_MESSAGE);
					break;
				}
				if (carColor.length() > Control.MAX_LEN_STRING_ATTRIBUTE){
					JOptionPane.showMessageDialog(null, 
							"Car ("+carRoad+" "+carNumber+") color ("+carColor+") too long!",
							MessageFormat.format(rb.getString("carAttribute"),new Object[]{Control.MAX_LEN_STRING_ATTRIBUTE}),
							JOptionPane.ERROR_MESSAGE);
					break;
				}
				Car c = manager.getCarByRoadAndNumber(carRoad, carNumber);
				if (c != null){
					log.info("Can not add, car number ("+carNumber+") road ("+carRoad+ ") already exists");
				} else {

					if(inputLine.length > base+6){
						carOwner = inputLine[base+6];
						if (carOwner.length() > Control.MAX_LEN_STRING_ATTRIBUTE){
							JOptionPane.showMessageDialog(null, 
									"Car ("+carRoad+" "+carNumber+") owner ("+carOwner+") too long!",
									MessageFormat.format(rb.getString("carAttribute"),new Object[]{Control.MAX_LEN_STRING_ATTRIBUTE}),
									JOptionPane.ERROR_MESSAGE);
							break;
						}
					}
					if(inputLine.length > base+7){
						carBuilt = inputLine[base+7];
						if (carBuilt.length() > 4){
							JOptionPane.showMessageDialog(null, 
									"Car ("+carRoad+" "+carNumber+") built ("+carBuilt+") too long!",
									rb.getString("carAttribute5"),
									JOptionPane.ERROR_MESSAGE);
							break;
						}
					}
					if(inputLine.length > base+8){
						carLocation = inputLine[base+8];

					}
					// Location name can be one to three words 
					if(inputLine.length > base+9){
						if (!inputLine[base+9].equals("-")){
							carLocation = carLocation + " " +inputLine[base+9];
							if(inputLine.length > base+10){
								if (!inputLine[base+10].equals("-"))
									carLocation = carLocation + " " +inputLine[base+10];
							}
							// create track location if there's one
						}
						boolean foundDash = false;
						for (int i=base+9; i<inputLine.length; i++){
							if(inputLine[i].equals("-")){
								foundDash = true;
								if (inputLine.length > i)
									carTrack = inputLine[++i];
							} else if (foundDash)
								carTrack = carTrack + " " +inputLine[i];
						}
						log.debug("Car ("+carRoad+" "+carNumber+") has track ("+carTrack+")");
					}

					if (carLocation.length() > 25){
						JOptionPane.showMessageDialog(null, 
								"Car ("+carRoad+" "+carNumber+") location ("+carLocation+") too long!",
								rb.getString("carAttribute25"),
								JOptionPane.ERROR_MESSAGE);
						break;
					}
					if (carTrack.length() > 25){
						JOptionPane.showMessageDialog(null, 
								"Car ("+carRoad+" "+carNumber+") track ("+carTrack+") too long!",
								rb.getString("carAttribute25"),
								JOptionPane.ERROR_MESSAGE);
						break;
					}
					Location l = LocationManager.instance().getLocationByName(carLocation);
					Track sl = null;
					if (l == null && !carLocation.equals("")){
						JOptionPane.showMessageDialog(null, "Car ("+carRoad+" "+carNumber+") location ("+carLocation+") does not exist",
								rb.getString("carLocation"),
								JOptionPane.ERROR_MESSAGE);
						break;
					}
					if (l != null && !carTrack.equals("")){
						sl = l.getTrackByName(carTrack, null);
						if (sl == null){
							JOptionPane.showMessageDialog(null, "Car ("+carRoad+" "+carNumber+") track location ("+carLocation+", "+carTrack+") does not exist",
									rb.getString("carLocation"),
									JOptionPane.ERROR_MESSAGE);
							break;
						}
					}
					log.debug("Add car ("+carRoad+" "+carNumber+") owner ("+carOwner+") built ("+carBuilt+") location ("+carLocation+", "+carTrack+")");
					Car car = manager.newCar(carRoad, carNumber);
					car.setType(carType);
					car.setLength(carLength);
					car.setWeight(carWeight);
					car.setColor(carColor);
					car.setOwner(carOwner);
					car.setBuilt(carBuilt);
					carsAdded++;
					
					car.setCaboose(carType.equals("Caboose"));

					if (l != null){
						String status = car.setLocation(l,sl);
						if (!status.equals(Car.OKAY)){
							log.debug ("Can't set car's location because of "+ status);
							JOptionPane.showMessageDialog(null,
									"Can't set car ("+carRoad+" "+carNumber+") type ("+carType+") because of location ("+carLocation+", "+carTrack+ ") "+ status,
									"Can not update car location",
									JOptionPane.ERROR_MESSAGE);
							break;
						}
					}else{
//						log.debug("No location for car ("+carRoad+" "+carNumber+")");
					}
				}
			}else{
				log.info("Import line number " + lineNum + " missing one of six required car attributes");
			}
		}
		try {
			in.close();
		} catch (IOException e) {
		}

		// kill status panel
		fstatus.dispose();

		if (importOkay) {
			JOptionPane.showMessageDialog(null, carsAdded+" cars added to roster",
					"Successful import!", JOptionPane.INFORMATION_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(null,
					carsAdded+" cars added to roster",
					"Import failed", JOptionPane.ERROR_MESSAGE);
		}
	}




	
	private class textFilter extends javax.swing.filechooser.FileFilter {
		
		public boolean accept(File f){
			if (f.isDirectory())
			return true;
			String name = f.getName();
			if (name.matches(".*\\.txt"))
				return true;
			else
				return false;
		}
		
		public String getDescription() {
			return "Text Documents (*.txt)";
		}
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(ImportCars.class.getName());
}

