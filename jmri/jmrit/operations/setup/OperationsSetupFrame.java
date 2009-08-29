// OperationsSetupFrame.java

package jmri.jmrit.operations.setup;

import java.awt.GridBagLayout;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import jmri.jmrit.display.LocoIcon;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.rollingstock.cars.CarOwners;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;


/**
 * Frame for user edit of operation parameters
 * 
 * @author Dan Boudreau Copyright (C) 2008
 * @version $Revision: 1.32 $
 */

public class OperationsSetupFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.setup.JmritOperationsSetupBundle");
	
	// labels
	JLabel textScale = new JLabel(" "+ rb.getString("Scale"));
	JLabel textCarType = new JLabel(" "+ rb.getString("CarTypes"));
	JLabel textRailroadName = new JLabel(" " + rb.getString("RailroadName") + " ");
	JLabel textDirection = new JLabel(rb.getString("direction"));
	JLabel textMaxTrain = new JLabel(rb.getString("MaxLength"));
	JLabel textMaxEngine = new JLabel(rb.getString("MaxEngine"));
	JLabel textMoveTime = new JLabel(rb.getString("MoveTime"));
	JLabel textTravelTime = new JLabel(rb.getString("TravelTime"));
	JLabel textOwner = new JLabel(" "+rb.getString("Owner"));
	JLabel textPrinter = new JLabel(rb.getString("PrinterFont"));
	JLabel textBuildReport = new JLabel(rb.getString("BuildReport"));
	JLabel textManifest = new JLabel(rb.getString("Manifest"));
	JLabel textPanel = new JLabel(" "+rb.getString("Panel"));
	JLabel textIconNorth = new JLabel(rb.getString("IconNorth"));
	JLabel textIconSouth = new JLabel(rb.getString("IconSouth"));
	JLabel textIconEast = new JLabel(rb.getString("IconEast"));
	JLabel textIconWest = new JLabel(rb.getString("IconWest"));
	JLabel textIconLocal = new JLabel(rb.getString("IconLocal"));
	JLabel textIconTerminate = new JLabel(rb.getString("IconTerminate"));
	JLabel textComment = new JLabel(rb.getString("Comment"));

	// major buttons	
	JButton backupButton = new JButton(rb.getString("Backup"));
	JButton restoreButton = new JButton(rb.getString("Restore"));
	JButton saveButton = new JButton(rb.getString("Save"));

	// radio buttons	
    JRadioButton scaleZ = new JRadioButton("Z");
    JRadioButton scaleN = new JRadioButton("N");
    JRadioButton scaleTT = new JRadioButton("TT");
    JRadioButton scaleHOn3 = new JRadioButton("HOn3");
    JRadioButton scaleOO = new JRadioButton("OO");
    JRadioButton scaleHO = new JRadioButton("HO");
    JRadioButton scaleSn3 = new JRadioButton("Sn3");
    JRadioButton scaleS = new JRadioButton("S");
    JRadioButton scaleOn3 = new JRadioButton("On3");
    JRadioButton scaleO = new JRadioButton("O");
    JRadioButton scaleG = new JRadioButton("G");
    
    JRadioButton typeDesc = new JRadioButton(rb.getString("Descriptive"));
    JRadioButton typeAAR = new JRadioButton(rb.getString("AAR"));
		
    JRadioButton mono = new JRadioButton(rb.getString("Monospaced"));
    JRadioButton sanSerif = new JRadioButton(rb.getString("SansSerif"));
    
    JRadioButton buildReportMin = new JRadioButton(rb.getString("Minimal"));
    JRadioButton buildReportNor = new JRadioButton(rb.getString("Normal"));
    JRadioButton buildReportMax = new JRadioButton(rb.getString("Detailed"));
    JRadioButton buildReportVD = new JRadioButton(rb.getString("VeryDetailed"));
    
    // check boxes
    JCheckBox eastCheckBox = new JCheckBox(rb.getString("eastwest"));
	JCheckBox northCheckBox = new JCheckBox(rb.getString("northsouth"));
	JCheckBox mainMenuCheckBox = new JCheckBox(rb.getString("MainMenu"));
	JCheckBox showLengthCheckBox = new JCheckBox(rb.getString("ShowCarLength"));
	JCheckBox showLoadCheckBox = new JCheckBox(rb.getString("ShowCarLoad"));
	JCheckBox showColorCheckBox = new JCheckBox(rb.getString("ShowCarColor"));
	JCheckBox showDestinationCheckBox = new JCheckBox(rb.getString("ShowCarDestination"));
	JCheckBox appendCommentCheckBox = new JCheckBox(rb.getString("CarComment"));
	JCheckBox iconCheckBox = new JCheckBox(rb.getString("trainIcon"));
	JCheckBox appendCheckBox = new JCheckBox(rb.getString("trainIconAppend"));
	JCheckBox rfidCheckBox = new JCheckBox(rb.getString("EnableRfid"));
	
	// text field
	JTextField ownerTextField = new JTextField(10);
	JTextField panelTextField = new JTextField(35);
	JTextField railroadNameTextField = new JTextField(35);
	JTextField maxLengthTextField = new JTextField(10);
	JTextField maxEngineSizeTextField = new JTextField(3);
	JTextField switchTimeTextField = new JTextField(3);
	JTextField travelTimeTextField = new JTextField(3);
	JTextField commentTextField = new JTextField(35);
	
	// combo boxes
	JComboBox northComboBox = new JComboBox();
	JComboBox southComboBox = new JComboBox();
	JComboBox eastComboBox = new JComboBox();
	JComboBox westComboBox = new JComboBox();
	JComboBox localComboBox = new JComboBox();
	JComboBox terminateComboBox = new JComboBox();

	public OperationsSetupFrame() {
		super(ResourceBundle.getBundle("jmri.jmrit.operations.setup.JmritOperationsSetupBundle").getString("TitleOperationsSetup"));
	}

	public void initComponents() {
		
		// the following code sets the frame's initial state
		
    	// create manager to load operation settings
		OperationsXml.instance();
		
		// load fields
		railroadNameTextField.setText(Setup.getRailroadName());				
		maxLengthTextField.setText(Integer.toString(Setup.getTrainLength()));
		maxEngineSizeTextField.setText(Integer.toString(Setup.getEngineSize()));
		switchTimeTextField.setText(Integer.toString(Setup.getSwitchTime()));
		travelTimeTextField.setText(Integer.toString(Setup.getTravelTime()));
		panelTextField.setText(Setup.getPanelName());
		ownerTextField.setText(Setup.getOwnerName());

		// load checkboxes
		mainMenuCheckBox.setSelected(Setup.isMainMenuEnabled());
		rfidCheckBox.setSelected(Setup.isRfidEnabled());
		iconCheckBox.setSelected(Setup.isTrainIconCordEnabled());
		appendCheckBox.setSelected(Setup.isTrainIconAppendEnabled());		
		showLengthCheckBox.setSelected(Setup.isShowCarLengthEnabled());
		showLoadCheckBox.setSelected(Setup.isShowCarLoadEnabled());
		showColorCheckBox.setSelected(Setup.isShowCarColorEnabled());
		showDestinationCheckBox.setSelected(Setup.isShowCarDestinationEnabled());		
		appendCommentCheckBox.setSelected(Setup.isAppendCarCommentEnabled());

		// add tool tips
		backupButton.setToolTipText(rb.getString("BackupToolTip"));
		restoreButton.setToolTipText(rb.getString("RestoreToolTip"));
		saveButton.setToolTipText(rb.getString("SaveToolTip"));

		// Layout the panel by rows
		// row 1
		getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
		JPanel panel = new JPanel();
		JScrollPane panelPane = new JScrollPane(panel);
		panel.setLayout(new GridBagLayout());
		addItem (panel, textRailroadName, 0, 1);
		addItemWidth (panel, railroadNameTextField, 3, 1, 1);
		
		// row 2
		addItem (panel, textDirection, 0, 2);
		addItemLeft (panel, northCheckBox, 1, 2);
		addItemLeft (panel, eastCheckBox, 2, 2);
		setDirectionCheckBox(Setup.getTrainDirection());
		
		// row 3
		addItem (panel, textMaxTrain, 0, 3);
		addItemLeft (panel, maxLengthTextField, 1, 3);
		
		// row 4
		addItem (panel, textMaxEngine, 0, 4);
		addItemLeft (panel, maxEngineSizeTextField, 1, 4);
		
		// row 5
		addItem (panel, textMoveTime, 0, 5);
		addItemLeft (panel, switchTimeTextField, 1, 5);
		
		// row 6
		addItem (panel, textTravelTime, 0, 6);
		addItemLeft (panel, travelTimeTextField, 1, 6);
		
		// row 7
		JPanel p = new JPanel();

		ButtonGroup scaleGroup = new ButtonGroup();
		scaleGroup.add(scaleZ);
		scaleGroup.add(scaleN);
		scaleGroup.add(scaleTT);
		scaleGroup.add(scaleHOn3);
		scaleGroup.add(scaleOO);
		scaleGroup.add(scaleHO);
		scaleGroup.add(scaleSn3);
		scaleGroup.add(scaleS);
		scaleGroup.add(scaleOn3);
		scaleGroup.add(scaleO);
		scaleGroup.add(scaleG);
		
		p.add(scaleZ);
		p.add(scaleN);
		p.add(scaleTT);
		p.add(scaleHOn3);
		p.add(scaleOO);
		p.add(scaleHO);
		p.add(scaleSn3);
		p.add(scaleS);
		p.add(scaleOn3);
		p.add(scaleO);
		p.add(scaleG);
		addItem(panel, textScale, 0, 7);
		addItemWidth(panel, p, 3, 1, 7);
		setScale();
		
		// row 9
		JPanel carTypeButtons = new JPanel();
		ButtonGroup carTypeGroup = new ButtonGroup();
		carTypeGroup.add(typeDesc);
		carTypeGroup.add(typeAAR);
		carTypeButtons.add(typeDesc);
		carTypeButtons.add(typeAAR);
		addItem (panel, textCarType, 0, 9);
		addItemWidth(panel, carTypeButtons, 3, 1, 9);
		setCarTypes();
		
		// row 10
		addItem (panel, textOwner, 0, 10);
		addItemLeft (panel, ownerTextField, 1, 10);
		
		// Option panel
		JPanel options = new JPanel();
		options.setLayout(new GridBagLayout());
		options.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutOptions")));
		addItem (options, mainMenuCheckBox, 1,7);
		addItem (options, rfidCheckBox, 1,8);		
			
		// Printer panel
		JPanel pPrinter = new JPanel();
		pPrinter.setLayout(new GridBagLayout());
		pPrinter.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutPrintOptions")));
		ButtonGroup printerGroup = new ButtonGroup();
		printerGroup.add(mono);
		printerGroup.add(sanSerif);
		ButtonGroup buildReportGroup = new ButtonGroup();
		buildReportGroup.add(buildReportMin);
		buildReportGroup.add(buildReportNor);
		buildReportGroup.add(buildReportMax);
		buildReportGroup.add(buildReportVD);
		
		// manifest options
		addItem (pPrinter, textPrinter, 0, 8);
		addItemLeft (pPrinter, mono, 1, 8);
		addItemLeft (pPrinter, sanSerif, 2, 8);
		addItem (pPrinter, textManifest, 0, 12);
		addItemLeft (pPrinter, showLengthCheckBox, 1, 12);
		addItemLeft (pPrinter, showLoadCheckBox, 2, 12);
		addItemLeft (pPrinter, showColorCheckBox, 3, 12);
		addItemLeft (pPrinter, showDestinationCheckBox, 4, 12);
		addItemWidth (pPrinter, appendCommentCheckBox, 2, 1, 14);
		// build report options
		addItem (pPrinter, textBuildReport, 0, 16);
		addItemLeft (pPrinter, buildReportMin, 1, 16);
		addItemLeft (pPrinter, buildReportNor, 2, 16);
		addItemLeft (pPrinter, buildReportMax, 3, 16);
		addItemLeft (pPrinter, buildReportVD, 4, 16);

			
		setPrinterFontRadioButton();
		setBuildReportRadioButton();

		// Icon panel
		JPanel pIcon = new JPanel();
		pIcon.setLayout(new GridBagLayout());	
		JScrollPane pIconPane = new JScrollPane(pIcon);
		pIconPane.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutPanelOptions")));

		addItem (pIcon, textPanel, 0, 1);
		addItemLeft (pIcon, panelTextField, 1, 1);
		panelTextField.setToolTipText(rb.getString("EnterPanelName"));
		addItem (pIcon, iconCheckBox, 0, 2);
		addItem (pIcon, appendCheckBox, 0, 3);
		addItem (pIcon, textIconNorth, 0, 4);
		addItemLeft (pIcon, northComboBox, 1, 4);
		addItem (pIcon, textIconSouth, 0, 5);
		addItemLeft (pIcon, southComboBox, 1, 5);
		addItem (pIcon, textIconEast, 0, 8);
		addItemLeft (pIcon, eastComboBox, 1, 8);
		addItem (pIcon, textIconWest, 0, 9);
		addItemLeft (pIcon, westComboBox, 1, 9);
		addItem (pIcon, textIconLocal, 0, 10);
		addItemLeft (pIcon, localComboBox, 1, 10);
		addItem (pIcon, textIconTerminate, 0, 11);
		addItemLeft (pIcon, terminateComboBox, 1, 11);
		loadIconComboBox(northComboBox);
		loadIconComboBox(southComboBox);
		loadIconComboBox(eastComboBox);
		loadIconComboBox(westComboBox);
		loadIconComboBox(localComboBox);
		loadIconComboBox(terminateComboBox);
		northComboBox.setSelectedItem(Setup.getTrainIconColorNorth());
		southComboBox.setSelectedItem(Setup.getTrainIconColorSouth());
		eastComboBox.setSelectedItem(Setup.getTrainIconColorEast());
		westComboBox.setSelectedItem(Setup.getTrainIconColorWest());
		localComboBox.setSelectedItem(Setup.getTrainIconColorLocal());
		terminateComboBox.setSelectedItem(Setup.getTrainIconColorTerminate());
				
		// row 11
		JPanel pControl = new JPanel();
		pControl.setLayout(new GridBagLayout());
		addItem(pControl, restoreButton, 0, 9);
		addItem(pControl, backupButton, 1, 9);
		addItem(pControl, saveButton, 3, 9);
		
		getContentPane().add(panelPane);
		getContentPane().add(options);
		getContentPane().add(pPrinter);
		getContentPane().add(pIconPane);
		getContentPane().add(pControl);

		// setup buttons
		addButtonAction(backupButton);
		addButtonAction(restoreButton);
		addButtonAction(saveButton);
		addCheckBoxAction(eastCheckBox);
		addCheckBoxAction(northCheckBox);

		//	build menu
		JMenuBar menuBar = new JMenuBar();
		JMenu toolMenu = new JMenu(rb.getString("Tools"));
		toolMenu.add(new LoadDemoAction(rb.getString("LoadDemo")));
		menuBar.add(toolMenu);
		setJMenuBar(menuBar);
		addHelpMenu("package.jmri.jmrit.operations.Operations_Settings", true);

		// set frame size and location for display
		pack();
		if (Setup.getOperationsSetupFrameSize()!= null){
			setSize(Setup.getOperationsSetupFrameSize());
		} 
		if (Setup.getOperationsSetupFramePosition()!= null){
			setLocation(Setup.getOperationsSetupFramePosition());
		}	
		setVisible(true);
	}
	
	BackupFrame bf = null;
	RestoreFrame rf = null;
	
	// Save, Delete, Add buttons
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == backupButton){
			if (bf != null)
				bf.dispose();
			bf = new BackupFrame();
			bf.initComponents();
		}
		if (ae.getSource() == restoreButton){
			if(rf != null)
				rf.dispose();
			rf = new RestoreFrame();
			rf.initComponents();
		}
		if (ae.getSource() == saveButton){
			String addOwner = ownerTextField.getText();
			if (addOwner.length() > Control.MAX_LEN_STRING_ATTRIBUTE){
				JOptionPane.showMessageDialog(this, MessageFormat.format(rb.getString("OwnerText"), new Object[]{Integer.toString(Control.MAX_LEN_STRING_ATTRIBUTE)}),
						rb.getString("CanNotAddOwner"),
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			// check input fields
			try {
				Integer.parseInt(maxLengthTextField.getText());
				Integer.parseInt(maxEngineSizeTextField.getText());
				Integer.parseInt(switchTimeTextField.getText());
				Integer.parseInt(travelTimeTextField.getText());
			} catch (NumberFormatException e){
				JOptionPane.showMessageDialog(this, e.getLocalizedMessage(),
						rb.getString("CanNotAcceptNumber"),
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			// add owner name to setup
			Setup.setOwnerName(addOwner);
			// add owner name to list
			CarOwners.instance().addName(addOwner);
			// set car types
			if (typeDesc.isSelected()){
				if (!Setup.getCarTypes().equals(Setup.DESCRIPTIVE)){
					CarTypes.instance().changeDefaultNames(Setup.DESCRIPTIVE);
					Setup.setCarTypes(Setup.DESCRIPTIVE);
				}
			} else {
				if (!Setup.getCarTypes().equals(Setup.AAR)){
					CarTypes.instance().changeDefaultNames(Setup.AAR);
					Setup.setCarTypes(Setup.AAR);
				}
			}
			// main menu enabled?
			Setup.setMainMenuEnabled(mainMenuCheckBox.isSelected());
			// RFID enabled?
			Setup.setRfidEnabled(rfidCheckBox.isSelected());
			// set printer font
			if (mono.isSelected())
				Setup.setFontName(Setup.MONOSPACED);
			else
				Setup.setFontName(Setup.SANSERIF);
			// show car attributes
			Setup.setShowCarLengthEnabled(showLengthCheckBox.isSelected());
			Setup.setShowCarLoadEnabled(showLoadCheckBox.isSelected());
			Setup.setShowCarColorEnabled(showColorCheckBox.isSelected());
			Setup.setShowCarDestinationEnabled(showDestinationCheckBox.isSelected());
			// append car comment
			Setup.setAppendCarCommentEnabled(appendCommentCheckBox.isSelected());
			// build report level
			if (buildReportMin.isSelected())
				Setup.setBuildReportLevel(Setup.BUILD_REPORT_MINIMAL);
			else if (buildReportNor.isSelected())
				Setup.setBuildReportLevel(Setup.BUILD_REPORT_NORMAL);
			else if (buildReportMax.isSelected())
				Setup.setBuildReportLevel(Setup.BUILD_REPORT_DETAILED);
			else if (buildReportVD.isSelected())
				Setup.setBuildReportLevel(Setup.BUILD_REPORT_VERY_DETAILED);
			// add panel name to setup
			Setup.setPanelName(panelTextField.getText());
			// train Icon X&Y
			Setup.setTrainIconCordEnabled(iconCheckBox.isSelected());
			Setup.setTrainIconAppendEnabled(appendCheckBox.isSelected());
			// save train icon colors
			Setup.setTrainIconColorNorth((String)northComboBox.getSelectedItem());
			Setup.setTrainIconColorSouth((String)southComboBox.getSelectedItem());
			Setup.setTrainIconColorEast((String)eastComboBox.getSelectedItem());
			Setup.setTrainIconColorWest((String)westComboBox.getSelectedItem());
			Setup.setTrainIconColorLocal((String)localComboBox.getSelectedItem());
			Setup.setTrainIconColorTerminate((String)terminateComboBox.getSelectedItem());
			// set train direction
			int direction = 0;
			if (eastCheckBox.isSelected())
				direction = Setup.EAST + Setup.WEST;
			if (northCheckBox.isSelected())
				direction += Setup.NORTH + Setup.SOUTH;
			Setup.setTrainDirection(direction);
			// set max train length
			Setup.setTrainLength(Integer.parseInt(maxLengthTextField.getText()));
			// set max engine length
			Setup.setEngineSize(Integer.parseInt(maxEngineSizeTextField.getText()));
			// set switch time
			Setup.setSwitchTime(Integer.parseInt(switchTimeTextField.getText()));
			// set travel time
			Setup.setTravelTime(Integer.parseInt(travelTimeTextField.getText()));
			// set scale
			if (scaleZ.isSelected())
				Setup.setScale(Setup.Z_SCALE);
			if (scaleN.isSelected())
				Setup.setScale(Setup.N_SCALE);
			if (scaleTT.isSelected())
				Setup.setScale(Setup.TT_SCALE);
			if (scaleOO.isSelected())
				Setup.setScale(Setup.OO_SCALE);
			if (scaleHOn3.isSelected())
				Setup.setScale(Setup.HOn3_SCALE);
			if (scaleHO.isSelected())
				Setup.setScale(Setup.HO_SCALE);
			if (scaleSn3.isSelected())
				Setup.setScale(Setup.Sn3_SCALE);
			if (scaleS.isSelected())
				Setup.setScale(Setup.S_SCALE);
			if (scaleOn3.isSelected())
				Setup.setScale(Setup.On3_SCALE);
			if (scaleO.isSelected())
				Setup.setScale(Setup.O_SCALE);
			if (scaleG.isSelected())
				Setup.setScale(Setup.G_SCALE);
			Setup.setRailroadName(railroadNameTextField.getText());
			// save panel size and position
			Setup.setOperationsSetupFrame(this);
			OperationsXml.instance().writeOperationsFile();
		}
	}
	
	public void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == northCheckBox){
			if (!northCheckBox.isSelected()){
				eastCheckBox.setSelected(true);
			}
		}
		if (ae.getSource() == eastCheckBox){
			if (!eastCheckBox.isSelected()){
				northCheckBox.setSelected(true);
			}
		}
		int direction = 0;
		if(eastCheckBox.isSelected()){
			direction += Setup.EAST;
		}
		if(northCheckBox.isSelected()){
			direction += Setup.NORTH;
		}
		setDirectionCheckBox(direction);
		pack();
		//setSize(getWidth(),getHeight()+getHeight()*1/10);
	}
	
	private void setScale(){
		int scale = Setup.getScale();
		switch (scale){
		case Setup.Z_SCALE:
			scaleZ.setSelected(true);
			break;
		case Setup.N_SCALE:
			scaleN.setSelected(true);
			break;
		case Setup.TT_SCALE:
			scaleTT.setSelected(true);
			break;
		case Setup.HOn3_SCALE:
			scaleHOn3.setSelected(true);
			break;
		case Setup.OO_SCALE:
			scaleOO.setSelected(true);
			break;
		case Setup.HO_SCALE:
			scaleHO.setSelected(true);
			break;
		case Setup.Sn3_SCALE:
			scaleSn3.setSelected(true);
			break;
		case Setup.S_SCALE:
			scaleS.setSelected(true);
			break;
		case Setup.On3_SCALE:
			scaleOn3.setSelected(true);
			break;
		case Setup.O_SCALE:
			scaleO.setSelected(true);
			break;
		case Setup.G_SCALE:
			scaleG.setSelected(true);
			break;
		default:
			log.error ("Unknown scale");
		}
	}
	
	private void setCarTypes(){
		typeDesc.setSelected(Setup.getCarTypes().equals(Setup.DESCRIPTIVE));
		typeAAR.setSelected(Setup.getCarTypes().equals(Setup.AAR));
	}
	
	private void setDirectionCheckBox(int direction){
		eastCheckBox.setSelected((direction & Setup.EAST) >0);
		textIconEast.setVisible((direction & Setup.EAST) >0);
		eastComboBox.setVisible((direction & Setup.EAST) >0);
		textIconWest.setVisible((direction & Setup.EAST) >0);
		westComboBox.setVisible((direction & Setup.EAST) >0);
		northCheckBox.setSelected((direction & Setup.NORTH) >0);
		textIconNorth.setVisible((direction & Setup.NORTH) >0);
		northComboBox.setVisible((direction & Setup.NORTH) >0);
		textIconSouth.setVisible((direction & Setup.NORTH) >0);
		southComboBox.setVisible((direction & Setup.NORTH) >0);
	}
	
	private void loadIconComboBox (JComboBox comboBox){
		LocoIcon li = new LocoIcon();
    	String[] colors = li.getLocoColors();
    	for (int i=0; i<colors.length; i++){
    		comboBox.addItem(colors[i]);
    	}
	}
	
	private void setPrinterFontRadioButton(){
		mono.setSelected(Setup.getFontName().equals(Setup.MONOSPACED));
		sanSerif.setSelected(Setup.getFontName().equals(Setup.SANSERIF));
	}
	
	private void setBuildReportRadioButton(){
		buildReportMin.setSelected(Setup.getBuildReportLevel().equals(Setup.BUILD_REPORT_MINIMAL));
		buildReportNor.setSelected(Setup.getBuildReportLevel().equals(Setup.BUILD_REPORT_NORMAL));
		buildReportMax.setSelected(Setup.getBuildReportLevel().equals(Setup.BUILD_REPORT_DETAILED));
		buildReportVD.setSelected(Setup.getBuildReportLevel().equals(Setup.BUILD_REPORT_VERY_DETAILED));
	}

	public void propertyChange(java.beans.PropertyChangeEvent e) {
		log.debug ("OperationsSetupFrame sees propertyChange "+e.getPropertyName()+" "+e.getNewValue());

	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(OperationsSetupFrame.class.getName());
}
