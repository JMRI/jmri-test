package jmri.jmrit.operations.setup;

/**
 * Operations settings. 
 * 
 * @author Daniel Boudreau Copyright (C) 2008, 2010
 * @version $Revision: 1.58 $
 */
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Color;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.List;
import javax.swing.JComboBox;

import jmri.jmrit.operations.rollingstock.RollingStockLogger;
import jmri.jmrit.operations.trains.TrainLogger;

import org.jdom.Element;


public class Setup {
	
	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.setup.JmritOperationsSetupBundle");
		
	// scale ratios from NMRA
	private static final int Z_RATIO = 220;
	private static final int N_RATIO = 160;
	private static final int TT_RATIO = 120;
	private static final int OO_RATIO = 76;			//actual ratio 76.2
	private static final int HO_RATIO = 87;
	private static final int S_RATIO = 64;
	private static final int O_RATIO = 48;
	private static final int G_RATIO = 32;			// NMRA #1
	
	// initial weight in milli ounces from NMRA
	private static final int Z_INITIAL_WEIGHT = 364;		// not specified by NMRA
	private static final int N_INITIAL_WEIGHT = 500;
	private static final int TT_INITIAL_WEIGHT = 750;
	private static final int HOn3_INITIAL_WEIGHT = 750;
	private static final int OO_INITIAL_WEIGHT = 750;	// not specified by NMRA
	private static final int HO_INITIAL_WEIGHT = 1000;
	private static final int Sn3_INITIAL_WEIGHT = 1000;
	private static final int S_INITIAL_WEIGHT = 2000;
	private static final int On3_INITIAL_WEIGHT = 1500;
	private static final int O_INITIAL_WEIGHT = 5000;
	private static final int G_INITIAL_WEIGHT = 10000;		// not specified by NMRA
	
	// additional weight in milli ounces from NMRA
	private static final int Z_ADD_WEIGHT = 100;			// not specified by NMRA
	private static final int N_ADD_WEIGHT = 150;
	private static final int TT_ADD_WEIGHT = 375;
	private static final int HOn3_ADD_WEIGHT = 375;
	private static final int OO_ADD_WEIGHT = 500;		// not specified by NMRA
	private static final int HO_ADD_WEIGHT = 500;
	private static final int Sn3_ADD_WEIGHT = 500;
	private static final int S_ADD_WEIGHT = 500;
	private static final int On3_ADD_WEIGHT = 750;
	private static final int O_ADD_WEIGHT = 1000;
	private static final int G_ADD_WEIGHT = 2000;		// not specified by NMRA
	
	// actual weight to tons conversion ratios (based on 40' boxcar at ~80 tons)
	private static final int Z_RATIO_TONS = 130;
	private static final int N_RATIO_TONS = 80;
	private static final int TT_RATIO_TONS = 36;
	private static final int HOn3_RATIO_TONS = 20;
	private static final int OO_RATIO_TONS = 20;
	private static final int HO_RATIO_TONS = 20;		// 20 tons per ounce
	private static final int Sn3_RATIO_TONS = 16;
	private static final int S_RATIO_TONS = 14;
	private static final int On3_RATIO_TONS = 8;
	private static final int O_RATIO_TONS = 5;
	private static final int G_RATIO_TONS = 2;			
	
	public static final int Z_SCALE = 1;
	public static final int N_SCALE = 2;
	public static final int TT_SCALE = 3;
	public static final int HOn3_SCALE = 4;
	public static final int OO_SCALE = 5;			
	public static final int HO_SCALE = 6;
	public static final int Sn3_SCALE = 7;
	public static final int S_SCALE = 8;
	public static final int On3_SCALE = 9;
	public static final int O_SCALE = 10;
	public static final int G_SCALE = 11;			// NMRA #1
	
	public static final int EAST = 1;		// train direction serviced by this location
	public static final int WEST = 2;
	public static final int NORTH = 4;
	public static final int SOUTH = 8;
	
	public static final String EAST_DIR = rb.getString("East");
	public static final String WEST_DIR = rb.getString("West");
	public static final String NORTH_DIR = rb.getString("North");
	public static final String SOUTH_DIR = rb.getString("South");
	
	public static final String DESCRIPTIVE = "Descriptive"; // Car types
	public static final String AAR = "ARR Codes"; // Car types
	
	public static final String COURIER = "Courier"; // printer fonts
	public static final String GARAMOND = "Garamond"; // printer fonts
	public static final String MONOSPACED = "Monospaced"; // printer fonts
	public static final String SANSERIF = "SansSerif";
	public static final String SERIF = "Serif";
	public static final String LENGTHABV =rb.getString("LengthSymbol");
	
	public static final String BUILD_REPORT_MINIMAL = "1";
	public static final String BUILD_REPORT_NORMAL = "3";
	public static final String BUILD_REPORT_DETAILED = "5";
	public static final String BUILD_REPORT_VERY_DETAILED = "7";
	
	public static final String ROAD = rb.getString("Road");		// the supported message format options
	public static final String NUMBER = rb.getString("Number");
	public static final String TYPE = rb.getString("Type");
	public static final String MODEL = rb.getString("Model");
	public static final String LENGTH = rb.getString("Length");
	public static final String LOAD = rb.getString("Load");
	public static final String COLOR = rb.getString("Color");
	public static final String DESTINATION = rb.getString("Destination");
	public static final String DEST_TRACK = rb.getString("DestAndTrack");
	public static final String LOCATION = rb.getString("Location");
	public static final String COMMENT = rb.getString("Comment");
	public static final String DROP_COMMENT = rb.getString("DropComment");
	public static final String PICKUP_COMMENT = rb.getString("PickupComment");
	public static final String HAZARDOUS = rb.getString("Hazardous");
	public static final String NONE = " ";				// none has be a character or a space
	public static final String BOX = " [ ] ";
	
	public static final String BLACK = rb.getString("Black");	// the supported pick up and set out colors
	public static final String BLUE = rb.getString("Blue");
	public static final String GREEN = rb.getString("Green");
	public static final String RED = rb.getString("Red");
	
	private static int scale = HO_SCALE;	// Default scale	
	private static int ratio = HO_RATIO;
	private static int ratioTons = HO_RATIO_TONS;
	private static int initWeight = HO_INITIAL_WEIGHT;
	private static int addWeight = HO_ADD_WEIGHT;
	private static String railroadName ="";
	private static int traindir = EAST+WEST+NORTH+SOUTH;
	private static int trainLength = 1000;
	private static int engineSize = 6;
	private static int carMoves = 5;
	private static String carTypes = DESCRIPTIVE;
	private static String ownerName ="";
	private static String fontName = MONOSPACED;
	private static int fontSize = 10;
	private static String pickupColor = BLACK;
	private static String dropColor = BLACK;
	private static String[] pickupEngineMessageFormat = {ROAD, NUMBER, NONE, MODEL, NONE, NONE, LOCATION, COMMENT};
	private static String[] dropEngineMessageFormat = {ROAD, NUMBER, NONE, MODEL, NONE, NONE, DESTINATION, COMMENT};
	private static String[] pickupCarMessageFormat = {ROAD, NUMBER, TYPE, LENGTH, COLOR, LOAD, HAZARDOUS, LOCATION, COMMENT, PICKUP_COMMENT};
	private static String[] dropCarMessageFormat = {ROAD, NUMBER, TYPE, LENGTH, COLOR, LOAD, HAZARDOUS, DESTINATION, COMMENT, DROP_COMMENT};
	private static String[] missingCarMessageFormat = {ROAD, NUMBER, TYPE, LENGTH, COLOR, COMMENT};
	private static String pickupEnginePrefix = BOX + rb.getString("PickUpPrefix");
	private static String dropEnginePrefix = BOX + rb.getString("SetOutPrefix");
	private static String pickupCarPrefix = BOX + rb.getString("PickUpPrefix");
	private static String dropCarPrefix = BOX + rb.getString("SetOutPrefix");
	private static boolean tab = false;
	private static String miaComment = rb.getString("misplacedCars");
	private static String logoURL ="";
	private static String panelName ="Panel";
	private static String buildReportLevel = BUILD_REPORT_NORMAL;
	private static boolean buildReportEditorEnabled = false;	// when true use text editor to view build report
	private static int carSwitchTime = 3;		// how long it take to move a car
	private static int travelTime = 4;// how long it take a train to move one location
	private static String yearModeled = ""; 	// year being modeled
	private static String iconNorthColor ="";
	private static String iconSouthColor ="";
	private static String iconEastColor ="";
	private static String iconWestColor ="";
	private static String iconLocalColor ="";
	private static String iconTerminateColor ="";
	
	private static boolean enableTrainIconXY = true;
	private static boolean appendTrainIcon = false;		//when true, append engine number to train name
		
	private static boolean mainMenuEnabled = false;		//when true add operations menu to main menu bar
	private static boolean closeWindowOnSave = false;	//when true, close window when save button is activated
	private static boolean enableRfid = false;			//when true show RFID fields for rolling stock
	private static boolean carRoutingEnabled = true;	//when true enable car routing
	private static boolean carRoutingStaging = false;	//when true staging tracks can be used for car routing
	private static boolean forwardToYardEnabled = true;	//when true forward car to yard if track is full
	private static boolean carLogger = false;			//when true car logger is enabled
	private static boolean engineLogger = false;		//when true engine logger is enabled
	private static boolean trainLogger = false;			//when true train logger is enabled
	
	private static boolean aggressiveBuild = false;		//when true subtract car length from track reserve length
	private static boolean allowLocalInterchangeMoves = false;	// when true local interchange to interchange moves are allowed
	private static boolean allowLocalYardMoves = false;		// when true local yard to yard moves are allowed
	private static boolean allowLocalSidingMoves = false;	// when true local siding to siding moves are allowed
	private static boolean trainIntoStagingCheck = true;	// when true staging track must accept train's rolling stock types and roads
	private static boolean promptFromStaging = false;		// when true prompt user to specify which staging track to use
	private static boolean generateCsvManifest = false;		// when true generate csv manifest
	
	private static boolean printLocationComments = false;	// when true print location comments on the manifest
	private static boolean printLoadsAndEmpties	= false;	// when true print Loads and Empties on the manifest

	
	// Setup frame attributes
	private static OperationsSetupFrame _operationsSetupFrame = null;
	private static Dimension _operationsSetupFrameDimension = null;
	private static Point _operationsSetupFramePosition = null;
	
	public static void setOperationsSetupFrame(OperationsSetupFrame frame){
		_operationsSetupFrame = frame;
	}

	public static Dimension getOperationsSetupFrameSize(){
		return _operationsSetupFrameDimension;
	}

	public static Point getOperationsSetupFramePosition(){
		return _operationsSetupFramePosition;
	}

	public static boolean isMainMenuEnabled(){
		OperationsSetupXml.instance(); // load file
		return mainMenuEnabled;
	}
	
	public static void setMainMenuEnabled(boolean enabled){
		mainMenuEnabled = enabled;
	}
	
	public static boolean isCloseWindowOnSaveEnabled(){
		return closeWindowOnSave;
	}
	
	public static void setCloseWindowOnSaveEnabled(boolean enabled){
		closeWindowOnSave = enabled;
	}
	
	public static boolean isRfidEnabled(){
		return enableRfid;
	}
	
	public static void setRfidEnabled(boolean enabled){
		enableRfid = enabled;
	}
	
	public static boolean isCarRoutingEnabled(){
		return carRoutingEnabled;
	}
	
	public static void setCarRoutingEnabled(boolean enabled){
		carRoutingEnabled = enabled;
	}
	
	public static boolean isCarRoutingViaStagingEnabled(){
		return carRoutingStaging;
	}
	
	public static void setCarRoutingViaStagingEnabled(boolean enabled){
		carRoutingStaging = enabled;
	}
	
	public static boolean isForwardToYardEnabled(){
		return forwardToYardEnabled;
	}
	
	public static void setForwardToYardEnabled(boolean enabled){
		forwardToYardEnabled = enabled;
	}
	
	public static boolean isBuildAggressive(){
		return aggressiveBuild;
	}
	
	public static void setBuildAggressive(boolean enabled){
		aggressiveBuild = enabled;
	}
	
	public static boolean isLocalInterchangeMovesEnabled(){
		return allowLocalInterchangeMoves;
	}
	
	public static void setLocalInterchangeMovesEnabled(boolean enabled){
		allowLocalInterchangeMoves = enabled;
	}
	
	public static boolean isLocalYardMovesEnabled(){
		return allowLocalYardMoves;
	}
	
	public static void setLocalYardMovesEnabled(boolean enabled){
		allowLocalYardMoves = enabled;
	}
	
	public static boolean isLocalSidingMovesEnabled(){
		return allowLocalSidingMoves;
	}
	
	public static void setLocalSidingMovesEnabled(boolean enabled){
		allowLocalSidingMoves = enabled;
	}
	
	public static boolean isTrainIntoStagingCheckEnabled(){
		return trainIntoStagingCheck;
	}
	
	public static void setTrainIntoStagingCheckEnabled(boolean enabled){
		trainIntoStagingCheck = enabled;
	}
	
	public static boolean isPromptFromStagingEnabled(){
		return promptFromStaging;
	}
	
	public static void setPromptFromStagingEnabled(boolean enabled){
		promptFromStaging = enabled;
	}
	
	public static boolean isGenerateCsvManifestEnabled(){
		return generateCsvManifest;
	}
	
	public static void setGenerateCsvManifestEnabled(boolean enabled){
		generateCsvManifest = enabled;
	}
	
	public static String getRailroadName(){
		return railroadName;
	}
	
	public static void setRailroadName(String name){
		railroadName = name;
	}
	
	public static String getMiaComment(){
		return miaComment;
	}
	
	public static void setMiaComment(String comment){
		miaComment = comment;
	}
	
	public static void setTrainDirection(int direction){
		traindir = direction;
	}
	
	public static int getTrainDirection(){
		return traindir;
	}
	
	public static void setTrainLength(int length){
		trainLength = length;
	}
	
	public static int getTrainLength(){
		return trainLength;
	}
	
	public static void setEngineSize(int size){
		engineSize = size;
	}
	
	public static int getEngineSize(){
		return engineSize;
	}
	
	public static void setCarMoves(int moves){
		carMoves = moves;
	}
	
	public static int getCarMoves(){
		return carMoves;
	}
	
	public static String getPanelName(){
		return panelName;
	}
	
	public static void setPanelName(String name){
		panelName = name;
	}
	
	public static String getYearModeled(){
		return yearModeled;
	}
	
	public static void setYearModeled(String year){
		yearModeled = year;
	}
	
	public static String getCarTypes(){
		return carTypes;
	}
	
	public static void setCarTypes(String types){
		carTypes = types;
	}

	public static void  setTrainIconCordEnabled(boolean enable){
		enableTrainIconXY = enable;
	}
	
	public static boolean isTrainIconCordEnabled(){
		return enableTrainIconXY;
	}
	
	public static void  setTrainIconAppendEnabled(boolean enable){
		appendTrainIcon = enable;
	}
	
	public static boolean isTrainIconAppendEnabled(){
		return appendTrainIcon;
	}
	
	public static void  setBuildReportLevel(String level){
		buildReportLevel = level;
	}
	
	public static String getBuildReportLevel(){
		return buildReportLevel;
	}
	
	public static void setBuildReportEditorEnabled(boolean enable){
		buildReportEditorEnabled = enable;
	}
	
	public static boolean isBuildReportEditorEnabled(){
		return buildReportEditorEnabled;
	}
	
	public static void setPrintLocationCommentsEnabled(boolean enable){
		printLocationComments = enable;
	}
	
	public static boolean isPrintLocationCommentsEnabled(){
		return printLocationComments;
	}
	
	public static void setPrintLoadsAndEmptiesEnabled(boolean enable){
		printLoadsAndEmpties = enable;
	}
	
	public static boolean isPrintLoadsAndEmptiesEnabled(){
		return printLoadsAndEmpties;
	}
	
	public static void setSwitchTime(int minutes){
		carSwitchTime = minutes;
	}
	
	public static int getSwitchTime(){
		return carSwitchTime;
	}
	
	public static void setTravelTime(int minutes){
		travelTime = minutes;
	}
	
	public static int getTravelTime(){
		return travelTime;
	}
	
	public static void setTrainIconColorNorth (String color){
		iconNorthColor = color;
	}
	
	public static String getTrainIconColorNorth(){
		return iconNorthColor;
	}
	
	public static void setTrainIconColorSouth (String color){
		iconSouthColor = color;
	}
	
	public static String getTrainIconColorSouth(){
		return iconSouthColor;
	}
	
	public static void setTrainIconColorEast (String color){
		iconEastColor = color;
	}
	
	public static String getTrainIconColorEast(){
		return iconEastColor;
	}
	
	public static void setTrainIconColorWest (String color){
		iconWestColor = color;
	}
	
	public static String getTrainIconColorWest(){
		return iconWestColor;
	}
	
	public static void setTrainIconColorLocal (String color){
		iconLocalColor = color;
	}
	
	public static String getTrainIconColorLocal(){
		return iconLocalColor;
	}
	
	public static void setTrainIconColorTerminate (String color){
		iconTerminateColor = color;
	}
	
	public static String getTrainIconColorTerminate(){
		return iconTerminateColor;
	}
	
	public static String getFontName(){
		return fontName;
	}
	
	public static void setFontName(String name){
		fontName = name;
	}
	
	public static int getFontSize(){
		return fontSize;
	}
	
	public static void setFontSize(int size){
		fontSize = size;
	}
	
	public static boolean isTabEnabled(){
		return tab;
	}
	
	public static void setTabEnabled(boolean enable){
		tab = enable;
	}
	
	public static boolean isCarLoggerEnabled(){
		return carLogger;
	}
	
	public static void setCarLoggerEnabled(boolean enable){
		carLogger = enable;
		RollingStockLogger.instance().enableCarLogging(enable);
	}
	
	public static boolean isEngineLoggerEnabled(){
		return engineLogger;
	}
	
	public static void setEngineLoggerEnabled(boolean enable){
		engineLogger = enable;
		RollingStockLogger.instance().enableEngineLogging(enable);
	}
	
	public static boolean isTrainLoggerEnabled(){
		return trainLogger;
	}
	
	public static void setTrainLoggerEnabled(boolean enable){
		trainLogger = enable;
		TrainLogger.instance().enableTrainLogging(enable);
	}
	
	public static String getPickupEnginePrefix(){
		return pickupEnginePrefix;
	}
	
	public static void setPickupEnginePrefix(String prefix){
		pickupEnginePrefix = prefix;
	}
	
	public static String getDropEnginePrefix(){
		return dropEnginePrefix;
	}
	
	public static void setDropEnginePrefix(String prefix){
		dropEnginePrefix = prefix;
	}
	
	public static String getPickupCarPrefix(){
		return pickupCarPrefix;
	}
	
	public static void setPickupCarPrefix(String prefix){
		pickupCarPrefix = prefix;
	}
	
	public static String getDropCarPrefix(){
		return dropCarPrefix;
	}
	
	public static void setDropCarPrefix(String prefix){
		dropCarPrefix = prefix;
	}
	
	public static String[] getPickupEngineMessageFormat(){
		return pickupEngineMessageFormat.clone();
	}
	
	@edu.umd.cs.findbugs.annotations.SuppressWarnings("EI_EXPOSE_STATIC_REP2")
	public static void setPickupEngineMessageFormat(String[] format){
		pickupEngineMessageFormat = format;
	}
	
	public static String[] getDropEngineMessageFormat(){
		return dropEngineMessageFormat.clone();
	}
	
	@edu.umd.cs.findbugs.annotations.SuppressWarnings("EI_EXPOSE_STATIC_REP2")
	public static void setDropEngineMessageFormat(String[] format){
		dropEngineMessageFormat = format;
	}
	
	public static String[] getPickupCarMessageFormat(){
		return pickupCarMessageFormat.clone();
	}
	
	@edu.umd.cs.findbugs.annotations.SuppressWarnings("EI_EXPOSE_STATIC_REP2")
	public static void setPickupCarMessageFormat(String[] format){
		pickupCarMessageFormat = format;
	}
	
	public static String[] getDropCarMessageFormat(){
		return dropCarMessageFormat.clone();
	}
	
	@edu.umd.cs.findbugs.annotations.SuppressWarnings("EI_EXPOSE_STATIC_REP2")
	public static void setDropCarMessageFormat(String[] format){
		dropCarMessageFormat = format;
	}
	
	public static String[] getMissingCarMessageFormat(){
		return missingCarMessageFormat.clone();
	}
	
	@edu.umd.cs.findbugs.annotations.SuppressWarnings("EI_EXPOSE_STATIC_REP2")
	public static void setMissingCarMessageFormat(String[] format){
		missingCarMessageFormat = format;
	}
	
	public static String getDropTextColor(){
		return dropColor;
	}
	
	public static void setDropTextColor(String color){
		dropColor = color;
	}
	
	public static String getPickupTextColor(){
		return pickupColor;
	}
	
	public static void setPickupTextColor(String color){
		pickupColor = color;
	}
	
	public static Color getPickupColor(){
		if (pickupColor.equals(BLUE))
			return Color.blue;
		if (pickupColor.equals(GREEN))
			return Color.green;
		if (pickupColor.equals(RED))
			return Color.red;
		return Color.black;	// default
	}
	
	public static Color getDropColor(){
		if (dropColor.equals(BLUE))
			return Color.blue;
		if (dropColor.equals(GREEN))
			return Color.green;
		if (dropColor.equals(RED))
			return Color.red;
		return Color.black;	// default
	}
	
	public static String getManifestLogoURL(){
		return logoURL;
	}
	
	public static void setManifestLogoURL(String pathName){
		logoURL = pathName;
	}
	
	public static String getOwnerName(){
		return ownerName;
	}
	
	public static void setOwnerName(String name){
		ownerName = name;
	}
	
	public static int getScaleRatio(){
		if (scale == 0)
			log.error("Scale not set");
		return ratio;
	}
	
	public static int getScaleTonRatio(){
		if (scale == 0)
			log.error("Scale not set");
		return ratioTons;
	}
	
	public static int getInitalWeight(){
		if (scale == 0)
			log.error("Scale not set");
		return initWeight;
	}
	
	public static int getAddWeight(){
		if (scale == 0)
			log.error("Scale not set");
		return addWeight;
	}
	
	public static int getScale(){
		return scale;
	}
	
	public static void setScale(int s){
		scale = s;
		switch (scale){
		case Z_SCALE:
			ratio = Z_RATIO;
			initWeight = Z_INITIAL_WEIGHT;
			addWeight = Z_ADD_WEIGHT;
			ratioTons = Z_RATIO_TONS;
			break;
		case N_SCALE:
			ratio = N_RATIO;
			initWeight = N_INITIAL_WEIGHT;
			addWeight = N_ADD_WEIGHT;
			ratioTons = N_RATIO_TONS;
			break;
		case TT_SCALE:
			ratio = TT_RATIO;
			initWeight = TT_INITIAL_WEIGHT;
			addWeight = TT_ADD_WEIGHT;
			ratioTons = TT_RATIO_TONS;
			break;
		case HOn3_SCALE:
			ratio = HO_RATIO;
			initWeight = HOn3_INITIAL_WEIGHT;
			addWeight = HOn3_ADD_WEIGHT;
			ratioTons = HOn3_RATIO_TONS;
			break;
		case OO_SCALE:
			ratio = OO_RATIO;
			initWeight = OO_INITIAL_WEIGHT;
			addWeight = OO_ADD_WEIGHT;
			ratioTons = OO_RATIO_TONS;
			break;
		case HO_SCALE:
			ratio = HO_RATIO;
			initWeight = HO_INITIAL_WEIGHT;
			addWeight = HO_ADD_WEIGHT;
			ratioTons = HO_RATIO_TONS;
			break;
		case Sn3_SCALE:
			ratio = S_RATIO;
			initWeight = Sn3_INITIAL_WEIGHT;
			addWeight = Sn3_ADD_WEIGHT;
			ratioTons = Sn3_RATIO_TONS;
			break;
		case S_SCALE:
			ratio = S_RATIO;
			initWeight = S_INITIAL_WEIGHT;
			addWeight = S_ADD_WEIGHT;
			ratioTons = S_RATIO_TONS;
			break;
		case On3_SCALE:
			ratio = O_RATIO;
			initWeight = On3_INITIAL_WEIGHT;
			addWeight = On3_ADD_WEIGHT;
			ratioTons = On3_RATIO_TONS;
			break;
		case O_SCALE:
			ratio = O_RATIO;
			initWeight = O_INITIAL_WEIGHT;
			addWeight = O_ADD_WEIGHT;
			ratioTons = O_RATIO_TONS;
			break;
		case G_SCALE:
			ratio = G_RATIO;
			initWeight = G_INITIAL_WEIGHT;
			addWeight = G_ADD_WEIGHT;
			ratioTons = G_RATIO_TONS;
			break;
		default:
			log.error ("Unknown scale");
		}
	}
	
	public static JComboBox getFontComboBox(){
		JComboBox box = new JComboBox();
		//java.awt.Font fonts[] = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
		//for (int i=0; i<fonts.length; i++){
		//	box.addItem(fonts[i]);
		//}
		box.addItem(COURIER);
		box.addItem(GARAMOND);
		box.addItem(MONOSPACED);
		box.addItem(SANSERIF);
		box.addItem(SERIF);
		return box;
	}
	
	/**
	 * 
	 * @return the available text colors used for printing
	 */
	public static JComboBox getPrintColorComboBox(){
		JComboBox box = new JComboBox();
		box.addItem(BLACK);
		box.addItem(BLUE);
		box.addItem(GREEN);
		box.addItem(RED);
		return box;
	}
	
	public static JComboBox getEngineMessageComboBox(){
		JComboBox box = new JComboBox();
		box.addItem(NONE);
		box.addItem(ROAD);
		box.addItem(NUMBER);
		box.addItem(TYPE);
		box.addItem(MODEL);
		box.addItem(LENGTH);
		box.addItem(LOCATION);
		box.addItem(DESTINATION);
		box.addItem(COMMENT);
		return box;
	}
	
	public static JComboBox getCarMessageComboBox(){
		JComboBox box = new JComboBox();
		box.addItem(NONE);
		box.addItem(ROAD);
		box.addItem(NUMBER);
		box.addItem(TYPE);
		box.addItem(LENGTH);
		box.addItem(LOAD);
		box.addItem(HAZARDOUS);
		box.addItem(COLOR);
		box.addItem(LOCATION);
		box.addItem(DESTINATION);
		box.addItem(DEST_TRACK);
		box.addItem(COMMENT);
		box.addItem(DROP_COMMENT);
		box.addItem(PICKUP_COMMENT);
		return box;
	}
	
	/**
	 * 
	 * @return JComboBox loaded with the strings (North, South, East,
	 *         West) showing the available train directions for this
	 *         railroad
	 */
    public static JComboBox getComboBox (){
    	JComboBox box = new JComboBox();
    	if ((traindir & EAST)>0)
			box.addItem(EAST_DIR);
    	if ((traindir & WEST)>0)
			box.addItem(WEST_DIR);
    	if ((traindir & NORTH)>0)
			box.addItem(NORTH_DIR);
    	if ((traindir & SOUTH)>0)
			box.addItem(SOUTH_DIR);
    	return box;
    }
    
    /**
     * Get train directions
     * @return List of valid train directions
     */
    public static List<String> getList(){
    	List<String> directions = new ArrayList<String>();
    	if ((traindir & EAST)>0)
    		directions.add(EAST_DIR);
    	if ((traindir & WEST)>0)
    		directions.add(WEST_DIR);
    	if ((traindir & NORTH)>0)
    		directions.add(NORTH_DIR);
    	if ((traindir & SOUTH)>0)
    		directions.add(SOUTH_DIR);
    	return directions;
    }
    
    /**
     * Converts binary direction to String direction
     * @param direction EAST, WEST, NORTH, SOUTH 
     * @return String representation of a direction
     */
    public static String getDirectionString(int direction){
    	switch (direction){
    	case EAST: return EAST_DIR; 
    	case WEST: return WEST_DIR; 
    	case NORTH: return NORTH_DIR; 
    	case SOUTH: return SOUTH_DIR; 
    	default: return "unknown";
    	}
    }
    
    /**
     * Converts String direction to binary direction
     * @param direction EAST_DIR WEST_DIR NORTH_DIR SOUTH_DIR
     * @return integer representation of a direction
     */
    public static int getDirectionInt(String direction){
    	if (direction.equals(EAST_DIR))
    		return EAST;
    	else if (direction.equals(WEST_DIR))
    		return WEST;
    	else if (direction.equals(NORTH_DIR))
    		return NORTH;
    	else if (direction.equals(SOUTH_DIR))
    		return SOUTH;
    	else
    		return 0; // return unknown
    }
    
    public static Element store(){
    	Element values;
    	Element e = new Element("operations");
    	e.addContent(values = new Element("railRoad"));
    	values.setAttribute("name", getRailroadName());
    	
    	e.addContent(values = new Element("settings"));
    	values.setAttribute("mainMenu", isMainMenuEnabled()?"true":"false");
    	values.setAttribute("closeOnSave", isCloseWindowOnSaveEnabled()?"true":"false");
    	values.setAttribute("trainDirection", Integer.toString(getTrainDirection()));
    	values.setAttribute("trainLength", Integer.toString(getTrainLength()));
    	values.setAttribute("maxEngines", Integer.toString(getEngineSize()));
    	values.setAttribute("scale", Integer.toString(getScale()));
    	values.setAttribute("carTypes", getCarTypes());
    	values.setAttribute("switchTime", Integer.toString(getSwitchTime()));
    	values.setAttribute("travelTime", Integer.toString(getTravelTime()));
    	values.setAttribute("showRfid", isRfidEnabled()?"true":"false");
    	values.setAttribute("carRoutingEnabled", isCarRoutingEnabled()?"true":"false");
    	values.setAttribute("carRoutingViaStaging", isCarRoutingViaStagingEnabled()?"true":"false");
    	values.setAttribute("forwardToYard", isForwardToYardEnabled()?"true":"false");
    	values.setAttribute("carLogger", isCarLoggerEnabled()?"true":"false");    	
       	values.setAttribute("engineLogger", isEngineLoggerEnabled()?"true":"false");
       	values.setAttribute("trainLogger", isTrainLoggerEnabled()?"true":"false");
       	values.setAttribute("printLocComments", isPrintLocationCommentsEnabled()?"true":"false");
       	values.setAttribute("printLoadsEmpties", isPrintLoadsAndEmptiesEnabled()?"true":"false");
       	values.setAttribute("yearModeled", getYearModeled());
       	
       	e.addContent(values = new Element("pickupEngFormat"));
       	values.setAttribute("prefix", getPickupEnginePrefix());
        StringBuffer buf = new StringBuffer();
       	for (int i=0; i<pickupEngineMessageFormat.length; i++){
       		buf.append(pickupEngineMessageFormat[i]+",");
       	}
       	values.setAttribute("setting", buf.toString());
    	
      	e.addContent(values = new Element("dropEngFormat"));
      	values.setAttribute("prefix", getDropEnginePrefix());
        buf = new StringBuffer();
       	for (int i=0; i<dropEngineMessageFormat.length; i++){
       		buf.append(dropEngineMessageFormat[i]+",");
       	}
       	values.setAttribute("setting", buf.toString());
    	
      	e.addContent(values = new Element("pickupCarFormat"));
      	values.setAttribute("prefix", getPickupCarPrefix());
        buf = new StringBuffer();
       	for (int i=0; i<pickupCarMessageFormat.length; i++){
       		buf.append(pickupCarMessageFormat[i]+",");
       	}
       	values.setAttribute("setting", buf.toString());
       	
      	e.addContent(values = new Element("dropCarFormat"));
      	values.setAttribute("prefix", getDropCarPrefix());
        buf = new StringBuffer();
       	for (int i=0; i<dropCarMessageFormat.length; i++){
       		buf.append(dropCarMessageFormat[i]+",");
       	}
       	values.setAttribute("setting", buf.toString());
       	
     	e.addContent(values = new Element("missingCarFormat"));
        buf = new StringBuffer();
       	for (int i=0; i<missingCarMessageFormat.length; i++){
       		buf.append(missingCarMessageFormat[i]+",");
       	}
       	values.setAttribute("setting", buf.toString());
    	
    	e.addContent(values = new Element("panel"));
    	values.setAttribute("name", getPanelName());
    	values.setAttribute("trainIconXY", isTrainIconCordEnabled()?"true":"false");
    	values.setAttribute("trainIconAppend", isTrainIconAppendEnabled()?"true":"false");
 
       	e.addContent(values = new Element("fontName"));
    	values.setAttribute("name", getFontName());
    	
       	e.addContent(values = new Element("fontSize"));
    	values.setAttribute("size", Integer.toString(getFontSize()));
    	
      	e.addContent(values = new Element("manifestColors"));
    	values.setAttribute("dropColor", getDropTextColor());
    	values.setAttribute("pickupColor", getPickupTextColor());
    	
    	e.addContent(values = new Element("tab"));
    	values.setAttribute("enabled", isTabEnabled()?"true":"false");
    	
        if (getManifestLogoURL() != ""){
        	values = new Element("manifestLogo");
        	values.setAttribute("name", getManifestLogoURL());
        	e.addContent(values);
        }       
    	
    	e.addContent(values = new Element("buildOptions"));
    	values.setAttribute("aggressive", isBuildAggressive()?"true":"false");
    	values.setAttribute("allowLocalInterchange", isLocalInterchangeMovesEnabled()?"true":"false");
    	values.setAttribute("allowLocalSiding", isLocalSidingMovesEnabled()?"true":"false");
    	values.setAttribute("allowLocalYard", isLocalYardMovesEnabled()?"true":"false");
    	values.setAttribute("stagingRestrictionEnabled", isTrainIntoStagingCheckEnabled()?"true":"false");
    	values.setAttribute("promptStagingEnabled", isPromptFromStagingEnabled()?"true":"false");
    	values.setAttribute("generateCsvManifest", isGenerateCsvManifestEnabled()?"true":"false");
    	
    	e.addContent(values = new Element("buildReport"));
    	values.setAttribute("level", getBuildReportLevel());
    	values.setAttribute("useEditor", isBuildReportEditorEnabled()?"true":"false");
    	
       	e.addContent(values = new Element("owner"));
    	values.setAttribute("name", getOwnerName());
     	
    	e.addContent(values = new Element("iconColor"));
    	values.setAttribute("north", getTrainIconColorNorth());
    	values.setAttribute("south", getTrainIconColorSouth());
    	values.setAttribute("east", getTrainIconColorEast());
    	values.setAttribute("west", getTrainIconColorWest());
    	values.setAttribute("local", getTrainIconColorLocal());
    	values.setAttribute("terminate", getTrainIconColorTerminate());
    	
      	e.addContent(values = new Element("comments"));
    	values.setAttribute("misplacedCars", getMiaComment());
    	
    	Element options;
    	e.addContent(options = new Element("options"));
    	options.addContent(values = new Element("setupFrameOptions"));
        Dimension size = getOperationsSetupFrameSize();
        Point posn = getOperationsSetupFramePosition();
        if (_operationsSetupFrame != null){
        	size = _operationsSetupFrame.getSize();
        	posn = _operationsSetupFrame.getLocation();
        	_operationsSetupFrameDimension = size;
        	_operationsSetupFramePosition = posn;
        }
        if (posn != null){
        	values.setAttribute("x", ""+posn.x);
        	values.setAttribute("y", ""+posn.y);
        }
        if (size != null){
        	values.setAttribute("height", ""+size.height);
        	values.setAttribute("width", ""+size.width); 
        }
    	return e;
    }
    
    public static void load(Element e) {
        //if (log.isDebugEnabled()) jmri.jmrit.XmlFile.dumpElement(e);
        
        if (e.getChild("operations") == null){
        	log.debug("operation setup values missing");
        	return;
        }
        Element operations = e.getChild("operations");
        org.jdom.Attribute a;
        
        if ((operations.getChild("railRoad") != null) && 
        		(a = operations.getChild("railRoad").getAttribute("name"))!= null){
        	String name = a.getValue();
           	if (log.isDebugEnabled()) log.debug("railroadName: "+name);
           	setRailroadName(name);
        }
        if (operations.getChild("settings") != null){
        	if ((a = operations.getChild("settings").getAttribute("mainMenu"))!= null){
        		String enabled = a.getValue();
        		if (log.isDebugEnabled()) log.debug("mainMenu: "+enabled);
        		setMainMenuEnabled(enabled.equals("true"));
        	}
           	if ((a = operations.getChild("settings").getAttribute("closeOnSave"))!= null){
        		String enabled = a.getValue();
        		if (log.isDebugEnabled()) log.debug("closeOnSave: "+enabled);
        		setCloseWindowOnSaveEnabled(enabled.equals("true"));
        	}
        	if ((a = operations.getChild("settings").getAttribute("trainDirection"))!= null){
        		String dir = a.getValue();
        		if (log.isDebugEnabled()) log.debug("direction: "+dir);
        		setTrainDirection(Integer.parseInt(dir));
        	}
        	if ((a = operations.getChild("settings").getAttribute("trainLength"))!= null){
        		String length = a.getValue();
        		if (log.isDebugEnabled()) log.debug("Max train length: "+length);
        		setTrainLength(Integer.parseInt(length));
        	}
        	if ((a = operations.getChild("settings").getAttribute("maxEngines"))!= null){
        		String size = a.getValue();
        		if (log.isDebugEnabled()) log.debug("Max number of engines: "+size);
        		setEngineSize(Integer.parseInt(size));
        	}
        	if ((a = operations.getChild("settings").getAttribute("scale"))!= null){
        		String scale = a.getValue();
        		if (log.isDebugEnabled()) log.debug("scale: "+scale);
        		setScale(Integer.parseInt(scale));
        	}
        	if ((a = operations.getChild("settings").getAttribute("carTypes"))!= null){
        		String types = a.getValue();
        		if (log.isDebugEnabled()) log.debug("CarTypes: "+types);
        		setCarTypes(types);
        	}
        	if ((a = operations.getChild("settings").getAttribute("switchTime"))!= null){
        		String minutes = a.getValue();
        		if (log.isDebugEnabled()) log.debug("switchTime: "+minutes);
        		setSwitchTime(Integer.parseInt(minutes));
        	}
        	if ((a = operations.getChild("settings").getAttribute("travelTime"))!= null){
        		String minutes = a.getValue();
        		if (log.isDebugEnabled()) log.debug("travelTime: "+minutes);
        		setTravelTime(Integer.parseInt(minutes));
        	}
        	if ((a = operations.getChild("settings").getAttribute("showRfid"))!= null){
        		String enable = a.getValue();
        		if (log.isDebugEnabled()) log.debug("showRfid: "+enable);
        		setRfidEnabled(enable.equals("true"));
        	}
           	if ((a = operations.getChild("settings").getAttribute("carRoutingEnabled"))!= null){
        		String enable = a.getValue();
        		if (log.isDebugEnabled()) log.debug("carRoutingEnabled: "+enable);
        		setCarRoutingEnabled(enable.equals("true"));
        	}
         	if ((a = operations.getChild("settings").getAttribute("carRoutingViaStaging"))!= null){
        		String enable = a.getValue();
        		if (log.isDebugEnabled()) log.debug("carRoutingViaStaging: "+enable);
        		setCarRoutingViaStagingEnabled(enable.equals("true"));
        	}
        	if ((a = operations.getChild("settings").getAttribute("forwardToYard"))!= null){
        		String enable = a.getValue();
        		if (log.isDebugEnabled()) log.debug("forwardToYard: "+enable);
        		setForwardToYardEnabled(enable.equals("true"));
        	}
          	if ((a = operations.getChild("settings").getAttribute("printLocComments"))!= null){
        		String enable = a.getValue();
        		if (log.isDebugEnabled()) log.debug("printLocComments: "+enable);
        		setPrintLocationCommentsEnabled(enable.equals("true"));
        	}
          	if ((a = operations.getChild("settings").getAttribute("printLoadsEmpties"))!= null){
        		String enable = a.getValue();
        		if (log.isDebugEnabled()) log.debug("printLoadsEmpties: "+enable);
        		setPrintLoadsAndEmptiesEnabled(enable.equals("true"));
        	}
         	if ((a = operations.getChild("settings").getAttribute("yearModeled"))!= null){
        		String year = a.getValue();
        		if (log.isDebugEnabled()) log.debug("yearModeled: "+year);
        		setYearModeled(year);
        	}
        }
        if (operations.getChild("pickupEngFormat") != null){
        	if ((a = operations.getChild("pickupEngFormat").getAttribute("prefix"))!= null)
        		setPickupEnginePrefix(a.getValue());
        	if ((a = operations.getChild("pickupEngFormat").getAttribute("setting"))!= null){
        		String setting = a.getValue();
        		if (log.isDebugEnabled()) log.debug("pickupEngFormat: "+setting);
        		String[] format = setting.split(",");
        		setPickupEngineMessageFormat(format);
        	}
        }
        if (operations.getChild("dropEngFormat") != null){
        	if ((a = operations.getChild("dropEngFormat").getAttribute("prefix"))!= null)
        		setDropEnginePrefix(a.getValue());
        	if ((a = operations.getChild("dropEngFormat").getAttribute("setting"))!= null){
        		String setting = a.getValue();
        		if (log.isDebugEnabled()) log.debug("dropEngFormat: "+setting);
        		String[] format = setting.split(",");
        		setDropEngineMessageFormat(format);
        	}
        }
        if (operations.getChild("pickupCarFormat") != null){
        	if ((a = operations.getChild("pickupCarFormat").getAttribute("prefix"))!= null)
        		setPickupCarPrefix(a.getValue());
        	if ((a = operations.getChild("pickupCarFormat").getAttribute("setting"))!= null){
        		String setting = a.getValue();
        		if (log.isDebugEnabled()) log.debug("pickupCarFormat: "+setting);
        		String[] format = setting.split(",");
        		replaceOldFormat(format);
        		setPickupCarMessageFormat(format);
        	}
        }
        if (operations.getChild("dropCarFormat") != null){
        	if ((a = operations.getChild("dropCarFormat").getAttribute("prefix"))!= null)
        		setDropCarPrefix(a.getValue());
        	if ((a = operations.getChild("dropCarFormat").getAttribute("setting"))!= null){
        		String setting = a.getValue();
        		if (log.isDebugEnabled()) log.debug("dropCarFormat: "+setting);
        		String[] format = setting.split(",");
        		replaceOldFormat(format);
        		setDropCarMessageFormat(format);
        	}
        }
        if (operations.getChild("missingCarFormat") != null){
        	if ((a = operations.getChild("missingCarFormat").getAttribute("setting"))!= null){
        		String setting = a.getValue();
        		if (log.isDebugEnabled()) log.debug("missingCarFormat: "+setting);
        		String[] format = setting.split(",");
        		setMissingCarMessageFormat(format);
        	}
        }
        if (operations.getChild("panel") != null){
        	if ((a = operations.getChild("panel").getAttribute("name"))!= null){
        		String panel = a.getValue();
        		if (log.isDebugEnabled()) log.debug("panel: "+panel);
        		setPanelName(panel);
        	}
        	if ((a = operations.getChild("panel").getAttribute("trainIconXY"))!= null){
        		String enable = a.getValue();
        		if (log.isDebugEnabled()) log.debug("TrainIconXY: "+enable);
        		setTrainIconCordEnabled(enable.equals("true"));
        	}
        	if ((a = operations.getChild("panel").getAttribute("trainIconAppend"))!= null){
        		String enable = a.getValue();
        		if (log.isDebugEnabled()) log.debug("TrainIconAppend: "+enable);
        		setTrainIconAppendEnabled(enable.equals("true"));
        	}
        }
        if ((operations.getChild("fontName") != null) 
        		&& (a = operations.getChild("fontName").getAttribute("name"))!= null){
        	String font = a.getValue();
           	if (log.isDebugEnabled()) log.debug("fontName: "+font);
           	setFontName(font);
        }
        if ((operations.getChild("fontSize") != null) 
        		&& (a = operations.getChild("fontSize").getAttribute("size"))!= null){
        	String size = a.getValue();
           	if (log.isDebugEnabled()) log.debug("fontName: "+size);
           	setFontSize(Integer.parseInt(size));
        }
        if ((operations.getChild("manifestColors") != null)){ 
        	if((a = operations.getChild("manifestColors").getAttribute("dropColor"))!= null){
        		String dropColor = a.getValue();
        		if (log.isDebugEnabled()) log.debug("dropColor: "+dropColor);
        		setDropTextColor(dropColor);
        	}
        	if((a = operations.getChild("manifestColors").getAttribute("pickupColor"))!= null){
        		String pickupColor = a.getValue();
        		if (log.isDebugEnabled()) log.debug("pickupColor: "+pickupColor);
        		setPickupTextColor(pickupColor);
        	}
        }
        if ((operations.getChild("tab") != null)){ 
        	if((a = operations.getChild("tab").getAttribute("enabled"))!= null){
        		String enable = a.getValue();
        		if (log.isDebugEnabled()) log.debug("tab: "+enable);
        		setTabEnabled(enable.equals("true"));
        	}
        }       
       	// get manifest logo
        if ((operations.getChild("manifestLogo") != null)){ 
        	if((a = operations.getChild("manifestLogo").getAttribute("name"))!= null){
        		setManifestLogoURL(a.getValue());
        	}
    	}
        if ((operations.getChild("buildOptions") != null)){
        	if((a = operations.getChild("buildOptions").getAttribute("aggressive")) != null) {
        		String enable = a.getValue();
        		if (log.isDebugEnabled()) log.debug("aggressive: "+enable);
        		setBuildAggressive(enable.equals("true"));
        	}
        	if((a = operations.getChild("buildOptions").getAttribute("allowLocalInterchange")) != null) {
        		String enable = a.getValue();
        		if (log.isDebugEnabled()) log.debug("noLocalInterchange: "+enable);
        		setLocalInterchangeMovesEnabled(enable.equals("true"));
        	}
        	if((a = operations.getChild("buildOptions").getAttribute("allowLocalSiding")) != null) {
        		String enable = a.getValue();
        		if (log.isDebugEnabled()) log.debug("noLocalSiding: "+enable);
        		setLocalSidingMovesEnabled(enable.equals("true"));
        	}
        	if((a = operations.getChild("buildOptions").getAttribute("allowLocalYard")) != null) {
        		String enable = a.getValue();
        		if (log.isDebugEnabled()) log.debug("noLocalYard: "+enable);
        		setLocalYardMovesEnabled(enable.equals("true"));
        	}
           	if((a = operations.getChild("buildOptions").getAttribute("stagingRestrictionEnabled")) != null) {
        		String enable = a.getValue();
        		if (log.isDebugEnabled()) log.debug("stagingRestrictionEnabled: "+enable);
        		setTrainIntoStagingCheckEnabled(enable.equals("true"));
        	}
           	if((a = operations.getChild("buildOptions").getAttribute("promptStagingEnabled")) != null) {
        		String enable = a.getValue();
        		if (log.isDebugEnabled()) log.debug("promptStagingEnabled: "+enable);
        		setPromptFromStagingEnabled(enable.equals("true"));
        	}
          	if((a = operations.getChild("buildOptions").getAttribute("generateCsvManifest")) != null) {
        		String enable = a.getValue();
        		if (log.isDebugEnabled()) log.debug("generateCvsManifest: "+enable);
        		setGenerateCsvManifestEnabled(enable.equals("true"));
        	}
        }
        if (operations.getChild("buildReport") != null){
        	if ((a = operations.getChild("buildReport").getAttribute("level")) != null) {
        		String level = a.getValue();
        		if (log.isDebugEnabled()) log.debug("buildReport: "+level);
        		setBuildReportLevel(level);
        	}
        	if ((a = operations.getChild("buildReport").getAttribute("useEditor")) != null) {
        		String enable = a.getValue();
        		if (log.isDebugEnabled()) log.debug("useEditor: "+enable);
        		setBuildReportEditorEnabled(enable.equals("true"));
        	}
        }
        if ((operations.getChild("owner") != null) 
        		&& (a = operations.getChild("owner").getAttribute("name"))!= null){
        	String owner = a.getValue();
           	if (log.isDebugEnabled()) log.debug("owner: "+owner);
           	setOwnerName(owner);
        }
        if (operations.getChild("iconColor") != null){
        	if ((a = operations.getChild("iconColor").getAttribute("north"))!= null){
        		String color = a.getValue();
        		if (log.isDebugEnabled()) log.debug("north color: "+color);
        		setTrainIconColorNorth(color);
        	}
        	if ((a = operations.getChild("iconColor").getAttribute("south"))!= null){
        		String color = a.getValue();
        		if (log.isDebugEnabled()) log.debug("south color: "+color);
        		setTrainIconColorSouth(color);
        	}
        	if ((a = operations.getChild("iconColor").getAttribute("east"))!= null){
        		String color = a.getValue();
        		if (log.isDebugEnabled()) log.debug("east color: "+color);
        		setTrainIconColorEast(color);
        	}
        	if ((a = operations.getChild("iconColor").getAttribute("west"))!= null){
        		String color = a.getValue();
        		if (log.isDebugEnabled()) log.debug("west color: "+color);
        		setTrainIconColorWest(color);
        	}
        	if ((a = operations.getChild("iconColor").getAttribute("local"))!= null){
        		String color = a.getValue();
        		if (log.isDebugEnabled()) log.debug("local color: "+color);
        		setTrainIconColorLocal(color);
        	}
        	if ((a = operations.getChild("iconColor").getAttribute("terminate"))!= null){
        		String color = a.getValue();
        		if (log.isDebugEnabled()) log.debug("terminate color: "+color);
        		setTrainIconColorTerminate(color);
        	}
        }
        if (operations.getChild("comments") != null){
        	if ((a = operations.getChild("comments").getAttribute("misplacedCars"))!= null){
           		String comment = a.getValue();
        		if (log.isDebugEnabled()) log.debug("Misplaced comment: "+comment);
        		setMiaComment(comment);
        	}
        }
        Element frameOptions;
        if ((operations.getChild("options")!= null)
        		&& (frameOptions = operations.getChild("options").getChild("setupFrameOptions"))!= null){
        	try {
        		int x = frameOptions.getAttribute("x").getIntValue();
        		int y = frameOptions.getAttribute("y").getIntValue();
        		int height = frameOptions.getAttribute("height").getIntValue();
        		int width = frameOptions.getAttribute("width").getIntValue();
        		_operationsSetupFrameDimension = new Dimension(width, height);
        		_operationsSetupFramePosition = new Point(x,y);
        	} catch ( org.jdom.DataConversionException ee) {
        		if (log.isDebugEnabled()) log.debug("Did not find Setup frame attributes");
        	} catch ( NullPointerException ne) {
        		if (log.isDebugEnabled()) log.debug("Did not find Setup frame attributes");
        	}
        }
        // logging has to be last, causes cars and engines to load
        if (operations.getChild("settings") != null){
        	if ((a = operations.getChild("settings").getAttribute("carLogger"))!= null){
        		String enable = a.getValue();
        		if (log.isDebugEnabled()) log.debug("carLogger: "+enable);
        		setCarLoggerEnabled(enable.equals("true"));
        	}
        	if ((a = operations.getChild("settings").getAttribute("engineLogger"))!= null){
        		String enable = a.getValue();
        		if (log.isDebugEnabled()) log.debug("engineLogger: "+enable);
        		setEngineLoggerEnabled(enable.equals("true"));
        	}
           	if ((a = operations.getChild("settings").getAttribute("trainLogger"))!= null){
        		String enable = a.getValue();
        		if (log.isDebugEnabled()) log.debug("trainLogger: "+enable);
        		setTrainLoggerEnabled(enable.equals("true"));
        	}
        }
    }
    
    // replace old pickup and drop message format
    // Change happened from 2.11.3 to 2.11.4
    private static void replaceOldFormat(String[] format){
    	for (int i=0; i<format.length; i++){
    		if (format[i].equals("Pickup Msg"))
    			format[i] = PICKUP_COMMENT;
    		if (format[i].equals("Drop Msg"))
    			format[i] = DROP_COMMENT;   			
    	}
    }
	
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Setup.class.getName());

}

