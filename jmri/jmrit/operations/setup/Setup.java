package jmri.jmrit.operations.setup;

/**
 * Operations settings. 
 * 
 * @author Daniel Boudreau Copyright (C) 2008
 * @version $Revision: 1.19 $
 */
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.List;
import javax.swing.JComboBox;

import jmri.jmrit.XmlFile;
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
	
	// initial weight in milli oz from NMRA
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
	
	// additional weight in milli oz from NMRA
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
	private static final int HO_RATIO_TONS = 20;		// 20 tons per oz
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
	
	public static final String MONOSPACED = "Monospaced"; // printer fonts
	public static final String SANSERIF = "SansSerif";
	public static final String LENGTHABV =rb.getString("LengthSymbol");
	
	public static final String BUILD_REPORT_MINIMAL = "1";
	public static final String BUILD_REPORT_NORMAL = "3";
	public static final String BUILD_REPORT_DETAILED = "5";
	public static final String BUILD_REPORT_VERY_DETAILED = "7";
	
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
	private static String panelName ="Panel";
	private static String buildReportLevel = BUILD_REPORT_NORMAL;
	private static int carSwitchTime = 3;		// how long it take to move a car
	private static int travelTime = 4;// how long it take a train to move one location
	private static String iconNorthColor ="";
	private static String iconSouthColor ="";
	private static String iconEastColor ="";
	private static String iconWestColor ="";
	private static String iconLocalColor ="";
	private static String iconTerminateColor ="";
	
	private static boolean enableTrainIconXY = true;
	private static boolean appendTrainIcon = false;		//when true, append engine number to train name
	private static boolean showCarLoad = false;			//when true, show car load in manifests 
	private static boolean appendCarComment = false;	//when true, append car comment to manifests 
	
	private static boolean mainMenuEnabled = false;		//when true add operations menu to main menu bar
	
	public static boolean isMainMenuEnabled(){
		OperationsXml.instance(); // load file
		return mainMenuEnabled;
	}
	
	public static void setMainMenuEnabled(boolean enabled){
		mainMenuEnabled = enabled;
	}
	
	public static String getRailroadName(){
		return railroadName;
	}
	
	public static void setRailroadName(String name){
		railroadName = name;
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
	
	public static String getCarTypes(){
		return carTypes;
	}
	
	public static void setCarTypes(String types){
		carTypes = types;
	}
	
	public static void  setAppendCarCommentEnabled(boolean enable){
		appendCarComment = enable;
	}
	
	public static boolean isAppendCarCommentEnabled(){
		return appendCarComment;
	}
	
	public static void  setShowCarLoadEnabled(boolean enable){
		showCarLoad = enable;
	}
	
	public static boolean isShowCarLoadEnabled(){
		return showCarLoad;
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
     * @return int representation of a direction
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
    		return 0; // return unkown
    }
    
    public static org.jdom.Element store(){
    	Element values;
    	Element e = new Element("operations");
    	e.addContent(values = new Element("railRoad"));
    	values.setAttribute("name", getRailroadName());
    	
    	e.addContent(values = new Element("settings"));
    	values.setAttribute("mainMenu", isMainMenuEnabled()?"true":"false");
    	values.setAttribute("trainDirection", Integer.toString(getTrainDirection()));
    	values.setAttribute("trainLength", Integer.toString(getTrainLength()));
    	values.setAttribute("maxEngines", Integer.toString(getEngineSize()));
    	values.setAttribute("scale", Integer.toString(getScale()));
    	values.setAttribute("carTypes", getCarTypes());
    	values.setAttribute("switchTime", Integer.toString(getSwitchTime()));
    	values.setAttribute("travelTime", Integer.toString(getTravelTime()));
    	values.setAttribute("showCarLoad", isShowCarLoadEnabled()?"true":"false");
    	values.setAttribute("addCarComment", isAppendCarCommentEnabled()?"true":"false");
    	
    	e.addContent(values = new Element("panel"));
    	values.setAttribute("name", getPanelName());
    	values.setAttribute("trainIconXY", isTrainIconCordEnabled()?"true":"false");
    	values.setAttribute("trainIconAppend", isTrainIconAppendEnabled()?"true":"false");
 
       	e.addContent(values = new Element("fontName"));
    	values.setAttribute("name", getFontName());
    	
    	e.addContent(values = new Element("buildReport"));
    	values.setAttribute("level", getBuildReportLevel());
    	
       	e.addContent(values = new Element("owner"));
    	values.setAttribute("name", getOwnerName());
     	
    	e.addContent(values = new Element("iconColor"));
    	values.setAttribute("north", getTrainIconColorNorth());
    	values.setAttribute("south", getTrainIconColorSouth());
    	values.setAttribute("east", getTrainIconColorEast());
    	values.setAttribute("west", getTrainIconColorWest());
    	values.setAttribute("local", getTrainIconColorLocal());
    	values.setAttribute("terminate", getTrainIconColorTerminate());
    	return e;
    }
    
    public static void load(org.jdom.Element e) {
        if (log.isDebugEnabled()) XmlFile.dumpElement(e);
        
        if (e.getChild("operations") == null){
        	log.debug("operation setup values missing");
        	return;
        }
        Element operations = e.getChild("operations");
        org.jdom.Attribute a;
        
        if ((a = operations.getChild("railRoad").getAttribute("name"))!= null){
        	String name = a.getValue();
           	if (log.isDebugEnabled()) log.debug("railroadName: "+name);
           	Setup.setRailroadName(name);
        }
        if ((a = operations.getChild("settings").getAttribute("mainMenu"))!= null){
        	String enabled = a.getValue();
           	if (log.isDebugEnabled()) log.debug("mainMenu: "+enabled);
           	Setup.setMainMenuEnabled(enabled.equals("true"));
        }
        if ((a = operations.getChild("settings").getAttribute("trainDirection"))!= null){
        	String dir = a.getValue();
           	if (log.isDebugEnabled()) log.debug("direction: "+dir);
           	Setup.setTrainDirection(Integer.parseInt(dir));
        }
        if ((a = operations.getChild("settings").getAttribute("trainLength"))!= null){
        	String length = a.getValue();
           	if (log.isDebugEnabled()) log.debug("Max train length: "+length);
           	Setup.setTrainLength(Integer.parseInt(length));
        }
        if ((a = operations.getChild("settings").getAttribute("maxEngines"))!= null){
        	String size = a.getValue();
           	if (log.isDebugEnabled()) log.debug("Max number of engines: "+size);
           	Setup.setEngineSize(Integer.parseInt(size));
        }
        if ((a = operations.getChild("settings").getAttribute("scale"))!= null){
        	String scale = a.getValue();
           	if (log.isDebugEnabled()) log.debug("scale: "+scale);
           	Setup.setScale(Integer.parseInt(scale));
        }
        if ((a = operations.getChild("settings").getAttribute("carTypes"))!= null){
        	String types = a.getValue();
           	if (log.isDebugEnabled()) log.debug("CarTypes: "+types);
           	Setup.setCarTypes(types);
        }
        if ((a = operations.getChild("settings").getAttribute("showCarLoad"))!= null){
        	String enable = a.getValue();
           	if (log.isDebugEnabled()) log.debug("showCarLoad: "+enable);
           	Setup.setShowCarLoadEnabled(enable.equals("true"));
        }
        if ((a = operations.getChild("settings").getAttribute("addCarComment"))!= null){
        	String enable = a.getValue();
           	if (log.isDebugEnabled()) log.debug("addCarComment: "+enable);
           	Setup.setAppendCarCommentEnabled(enable.equals("true"));
        }
        if ((a = operations.getChild("settings").getAttribute("switchTime"))!= null){
        	String minutes = a.getValue();
           	if (log.isDebugEnabled()) log.debug("switchTime: "+minutes);
           	Setup.setSwitchTime(Integer.parseInt(minutes));
        }
        if ((a = operations.getChild("settings").getAttribute("travelTime"))!= null){
        	String minutes = a.getValue();
           	if (log.isDebugEnabled()) log.debug("travelTime: "+minutes);
           	Setup.setTravelTime(Integer.parseInt(minutes));
        }
        if ((a = operations.getChild("panel").getAttribute("name"))!= null){
        	String panel = a.getValue();
           	if (log.isDebugEnabled()) log.debug("panel: "+panel);
           	Setup.setPanelName(panel);
        }
        if ((a = operations.getChild("panel").getAttribute("trainIconXY"))!= null){
        	String enable = a.getValue();
           	if (log.isDebugEnabled()) log.debug("TrainIconXY: "+enable);
           	Setup.setTrainIconCordEnabled(enable.equals("true"));
        }
        if ((a = operations.getChild("panel").getAttribute("trainIconAppend"))!= null){
        	String enable = a.getValue();
           	if (log.isDebugEnabled()) log.debug("TrainIconAppend: "+enable);
           	Setup.setTrainIconAppendEnabled(enable.equals("true"));
        }
        if ((a = operations.getChild("fontName").getAttribute("name"))!= null){
        	String font = a.getValue();
           	if (log.isDebugEnabled()) log.debug("fontName: "+font);
           	Setup.setFontName(font);
        }
        if (operations.getChild("buildReport") != null
				&& (a = operations.getChild("buildReport").getAttribute("level")) != null) {
        	String level = a.getValue();
           	if (log.isDebugEnabled()) log.debug("buildReport: "+level);
           	Setup.setBuildReportLevel(level);
        }
        if ((a = operations.getChild("owner").getAttribute("name"))!= null){
        	String owner = a.getValue();
           	if (log.isDebugEnabled()) log.debug("owner: "+owner);
           	Setup.setOwnerName(owner);
        }
        if ((a = operations.getChild("iconColor").getAttribute("north"))!= null){
        	String color = a.getValue();
           	if (log.isDebugEnabled()) log.debug("north color: "+color);
           	Setup.setTrainIconColorNorth(color);
        }
        if ((a = operations.getChild("iconColor").getAttribute("south"))!= null){
        	String color = a.getValue();
           	if (log.isDebugEnabled()) log.debug("south color: "+color);
           	Setup.setTrainIconColorSouth(color);
        }
        if ((a = operations.getChild("iconColor").getAttribute("east"))!= null){
        	String color = a.getValue();
           	if (log.isDebugEnabled()) log.debug("east color: "+color);
           	Setup.setTrainIconColorEast(color);
        }
        if ((a = operations.getChild("iconColor").getAttribute("west"))!= null){
        	String color = a.getValue();
           	if (log.isDebugEnabled()) log.debug("west color: "+color);
           	Setup.setTrainIconColorWest(color);
        }
        if ((a = operations.getChild("iconColor").getAttribute("local"))!= null){
        	String color = a.getValue();
           	if (log.isDebugEnabled()) log.debug("local color: "+color);
           	Setup.setTrainIconColorLocal(color);
        }
        if ((a = operations.getChild("iconColor").getAttribute("terminate"))!= null){
        	String color = a.getValue();
           	if (log.isDebugEnabled()) log.debug("terminate color: "+color);
           	Setup.setTrainIconColorTerminate(color);
        }
    }
    

	
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(Setup.class.getName());

}

