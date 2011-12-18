// PrintOptionFrame.java

package jmri.jmrit.operations.setup;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import jmri.jmrit.operations.OperationsFrame;


/**
 * Frame for user edit of print options
 * 
 * @author Dan Boudreau Copyright (C) 2008, 2010, 2011
 * @version $Revision$
 */

public class PrintOptionFrame extends OperationsFrame{

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.setup.JmritOperationsSetupBundle");
	
	// labels
	JLabel textBuildReport = new JLabel(rb.getString("BuildReport"));
	JLabel logoURL = new JLabel("");

	// major buttons	
	JButton saveButton = new JButton(rb.getString("Save"));
	JButton addLogoButton = new JButton(rb.getString("AddLogo"));
	JButton removeLogoButton = new JButton(rb.getString("RemoveLogo"));

	// radio buttons		
    
    JRadioButton buildReportMin = new JRadioButton(rb.getString("Minimal"));
    JRadioButton buildReportNor = new JRadioButton(rb.getString("Normal"));
    JRadioButton buildReportMax = new JRadioButton(rb.getString("Detailed"));
    JRadioButton buildReportVD = new JRadioButton(rb.getString("VeryDetailed"));
    
    // check boxes
    JCheckBox tabFormatCheckBox = new JCheckBox(rb.getString("TabFormat"));
    JCheckBox formatSwitchListCheckBox = new JCheckBox(rb.getString("SameAsManifest"));
    JCheckBox editManifestCheckBox = new JCheckBox(rb.getString("UseTextEditor"));
	JCheckBox buildReportCheckBox = new JCheckBox(rb.getString("BuildReportEdit"));
	JCheckBox printLocCommentsCheckBox = new JCheckBox(rb.getString("PrintLocationComments"));
	JCheckBox printRouteCommentsCheckBox = new JCheckBox(rb.getString("PrintRouteComments"));
	JCheckBox printLoadsEmptiesCheckBox = new JCheckBox(rb.getString("PrintLoadsEmpties"));
	JCheckBox printTimetableNameCheckBox = new JCheckBox(rb.getString("PrintTimetableName"));
	JCheckBox use12hrFormatCheckBox = new JCheckBox(rb.getString("12hrFormat"));
	
	// text field
	JTextField pickupEngPrefix = new JTextField(10);
	JTextField dropEngPrefix = new JTextField(10);
	JTextField pickupCarPrefix = new JTextField(10);
	JTextField dropCarPrefix = new JTextField(10);
	JTextField localPrefix = new JTextField(10);
	JTextField switchListPickupCarPrefix = new JTextField(10);
	JTextField switchListDropCarPrefix = new JTextField(10);
	JTextField switchListLocalPrefix = new JTextField(10);

	
	// text area
	JTextArea commentTextArea	= new JTextArea(2,90);
	JScrollPane commentScroller = new JScrollPane(commentTextArea,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	Dimension minScrollerDim = new Dimension(700,60);
	
	// combo boxes
	JComboBox fontComboBox = Setup.getFontComboBox();
	JComboBox manifestOrientationComboBox = Setup.getOrientationComboBox();
	JComboBox fontSizeComboBox = new JComboBox();
	JComboBox pickupComboBox = Setup.getPrintColorComboBox();	// colors
	JComboBox dropComboBox = Setup.getPrintColorComboBox();
	JComboBox localComboBox = Setup.getPrintColorComboBox();
	JComboBox switchListOrientationComboBox = Setup.getOrientationComboBox();
	
	// message formats
	List<JComboBox> enginePickupMessageList = new ArrayList<JComboBox>();
	List<JComboBox> engineDropMessageList = new ArrayList<JComboBox>();
	List<JComboBox> carPickupMessageList = new ArrayList<JComboBox>();
	List<JComboBox> carDropMessageList = new ArrayList<JComboBox>();
	List<JComboBox> localMessageList = new ArrayList<JComboBox>();
	List<JComboBox> switchListCarPickupMessageList = new ArrayList<JComboBox>();
	List<JComboBox> switchListCarDropMessageList = new ArrayList<JComboBox>();
	List<JComboBox> switchListLocalMessageList = new ArrayList<JComboBox>();
	
	// switch list panels
	JPanel pSwitchListOrientation = new JPanel();
	JPanel pSwPickup = new JPanel();
	JPanel pSwDrop = new JPanel();
	JPanel pSwLocal = new JPanel();

	public PrintOptionFrame() {
		super(ResourceBundle.getBundle("jmri.jmrit.operations.setup.JmritOperationsSetupBundle").getString("TitlePrintOptions"));
	}

	public void initComponents() {

		// the following code sets the frame's initial state

		// add tool tips
		saveButton.setToolTipText(rb.getString("SaveToolTip"));
		addLogoButton.setToolTipText(rb.getString("AddLogoToolTip"));
		removeLogoButton.setToolTipText(rb.getString("RemoveLogoToolTip"));
		tabFormatCheckBox.setToolTipText(rb.getString("TabComment"));
		printLocCommentsCheckBox.setToolTipText(rb.getString("AddLocationComments"));
		printRouteCommentsCheckBox.setToolTipText(rb.getString("AddRouteComments"));
		printLoadsEmptiesCheckBox.setToolTipText(rb.getString("LoadsEmptiesComment"));
		printTimetableNameCheckBox.setToolTipText(rb.getString("ShowTimetableTip"));
		use12hrFormatCheckBox.setToolTipText(rb.getString("Use12hrFormatTip"));
		buildReportCheckBox.setToolTipText(rb.getString("CreatesTextFileTip"));
		editManifestCheckBox.setToolTipText(rb.getString("UseTextEditorTip"));
		
		// Manifest panel
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		JPanel pManifest = new JPanel();
		pManifest.setLayout(new BoxLayout(pManifest, BoxLayout.Y_AXIS));
		JScrollPane pManifestPane = new JScrollPane(pManifest);
		pManifestPane.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutManifestOptions")));
		
		// row 1 font type and size
		JPanel p1 = new JPanel();
		p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
		
		JPanel pFont = new JPanel();
		pFont.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutFont")));
		pFont.add(fontComboBox);
		
		JPanel pFontSize = new JPanel();
		pFontSize.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutFontSize")));
		pFontSize.add(fontSizeComboBox);

		JPanel pOrientation = new JPanel();
		pOrientation.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutOrientation")));
		pOrientation.add(manifestOrientationComboBox);

		JPanel pPickupColor = new JPanel();
		pPickupColor.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutPickupColor")));
		pPickupColor.add( pickupComboBox);
		
		JPanel pDropColor = new JPanel();
		pDropColor.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutDropColor")));
		pDropColor.add(dropComboBox);
		
		JPanel pLocalColor = new JPanel();
		pLocalColor.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutLocalColor")));
		pLocalColor.add(localComboBox);
		
		JPanel pFormat = new JPanel();
		pFormat.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutFormat")));
		pFormat.add(tabFormatCheckBox);
		
		JPanel pSwitchFormat = new JPanel();
		pSwitchFormat.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutSwitchListFormat")));
		pSwitchFormat.add(formatSwitchListCheckBox);

		p1.add(pFont);
		p1.add(pFontSize);
		p1.add(pOrientation);
		p1.add(pPickupColor);
		p1.add(pDropColor);
		p1.add(pLocalColor);
		p1.add(pFormat);
		p1.add(pSwitchFormat);
		
		// engine message format
		JPanel pEngPickup = new JPanel();
		pEngPickup.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutPickupEngine")));
		pEngPickup.add(pickupEngPrefix);
		pickupEngPrefix.setText(Setup.getPickupEnginePrefix());
		String[] format = Setup.getPickupEngineMessageFormat();
		for (int i=0; i<format.length; i++){
			JComboBox b = Setup.getEngineMessageComboBox();
			b.setSelectedItem(format[i]);
			pEngPickup.add(b);
			enginePickupMessageList.add(b);
		}
		
		JPanel pEngDrop = new JPanel();
		pEngDrop.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutDropEngine")));
		pEngDrop.add(dropEngPrefix);
		dropEngPrefix.setText(Setup.getDropEnginePrefix());
		format = Setup.getDropEngineMessageFormat();
		for (int i=0; i<format.length; i++){
			JComboBox b = Setup.getEngineMessageComboBox();
			b.setSelectedItem(format[i]);
			pEngDrop.add(b);
			engineDropMessageList.add(b);
		}
		
		// car pickup message format
		JPanel pPickup = new JPanel();
		pPickup.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutPickupCar")));
		pPickup.add(pickupCarPrefix);
		pickupCarPrefix.setText(Setup.getPickupCarPrefix());
		String[] pickFormat = Setup.getPickupCarMessageFormat();
		for (int i=0; i<pickFormat.length; i++){
			JComboBox b = Setup.getCarMessageComboBox();
			b.setSelectedItem(pickFormat[i]);
			pPickup.add(b);
			carPickupMessageList.add(b);
		}
			
		// car drop message format
		JPanel pDrop = new JPanel();
		pDrop.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutDropCar")));
		pDrop.add(dropCarPrefix);
		dropCarPrefix.setText(Setup.getDropCarPrefix());
		String[] dropFormat = Setup.getDropCarMessageFormat();
		for (int i=0; i<dropFormat.length; i++){
			JComboBox b = Setup.getCarMessageComboBox();
			b.setSelectedItem(dropFormat[i]);
			pDrop.add(b);
			carDropMessageList.add(b);
		}
		
		// local car move message format
		JPanel pLocal = new JPanel();
		pLocal.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutLocal")));
		pLocal.add(localPrefix);
		localPrefix.setText(Setup.getLocalPrefix());
		String[] localFormat = Setup.getLocalMessageFormat();
		for (int i=0; i<localFormat.length; i++){
			JComboBox b = Setup.getCarMessageComboBox();
			b.setSelectedItem(localFormat[i]);
			pLocal.add(b);
			localMessageList.add(b);
		}

		// switch list car pickup message format
		pSwitchListOrientation = new JPanel();
		pSwitchListOrientation.setLayout(new GridBagLayout());
		pSwitchListOrientation.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutSwitchListOrientation")));		
		addItem(pSwitchListOrientation, switchListOrientationComboBox, 0, 0);
		addItem(pSwitchListOrientation, new JLabel(" "), 1, 0);	// pad
		addItem(pSwitchListOrientation, new JLabel(" "), 2, 0);	// pad
		addItem(pSwitchListOrientation, new JLabel(" "), 3, 0);	// pad
		//pSwitchListOrientation.add(switchListOrientationComboBox);
		
		pSwPickup.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutSwitchListPickupCar")));
		pSwPickup.add(switchListPickupCarPrefix);
		switchListPickupCarPrefix.setText(Setup.getSwitchListPickupCarPrefix());
		pickFormat = Setup.getSwitchListPickupCarMessageFormat();
		for (int i=0; i<pickFormat.length; i++){
			JComboBox b = Setup.getCarMessageComboBox();
			b.setSelectedItem(pickFormat[i]);
			pSwPickup.add(b);
			switchListCarPickupMessageList.add(b);
		}
			
		// switch list car drop message format
		pSwDrop.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutSwitchListDropCar")));
		pSwDrop.add(switchListDropCarPrefix);
		switchListDropCarPrefix.setText(Setup.getSwitchListDropCarPrefix());
		dropFormat = Setup.getSwitchListDropCarMessageFormat();
		for (int i=0; i<dropFormat.length; i++){
			JComboBox b = Setup.getCarMessageComboBox();
			b.setSelectedItem(dropFormat[i]);
			pSwDrop.add(b);
			switchListCarDropMessageList.add(b);
		}
		
		// switch list local car move message format
		pSwLocal.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutSwitchListLocal")));
		pSwLocal.add(switchListLocalPrefix);
		switchListLocalPrefix.setText(Setup.getSwitchListLocalPrefix());
		localFormat = Setup.getSwitchListLocalMessageFormat();
		for (int i=0; i<localFormat.length; i++){
			JComboBox b = Setup.getCarMessageComboBox();
			b.setSelectedItem(localFormat[i]);
			pSwLocal.add(b);
			switchListLocalMessageList.add(b);
		}		
		
		JPanel p2 = new JPanel();
		p2.setLayout(new BoxLayout(p2, BoxLayout.X_AXIS));
		
		// Manifest comments
		JPanel pManifestComment = new JPanel();
		pManifestComment.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutComments")));
		pManifestComment.add(printLocCommentsCheckBox);
		pManifestComment.add(printRouteCommentsCheckBox);
		pManifestComment.add(printLoadsEmptiesCheckBox);
		pManifestComment.add(use12hrFormatCheckBox);
		pManifestComment.add(printTimetableNameCheckBox);
		
		// manifest logo
		JPanel pLogo = new JPanel();
		pLogo.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutLogo")));
		pLogo.add(removeLogoButton);
		pLogo.add(addLogoButton);
		pLogo.add(logoURL);
		p2.add(pManifestComment);
		p2.add(pLogo);
		
		pManifest.add(p1);
		pManifest.add(pEngPickup);
		pManifest.add(pEngDrop);
		pManifest.add(pPickup);
		pManifest.add(pDrop);
		pManifest.add(pLocal);
		pManifest.add(pSwitchListOrientation);
		pManifest.add(pSwPickup);
		pManifest.add(pSwDrop);
		pManifest.add(pSwLocal);
		pManifest.add(p2);
		
		// manifest comment
		JPanel pComment = new JPanel();
		pComment.setLayout(new GridBagLayout());
		JScrollPane pCommentPane = new JScrollPane(pComment);
		pCommentPane.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutCommentOptions")));
		addItem (pComment, commentScroller, 0, 0);
		
		// panel options
		JPanel pOptions = new JPanel();
		pOptions.setLayout(new BoxLayout(pOptions, BoxLayout.X_AXIS));
		JScrollPane pOptionsPane = new JScrollPane(pOptions);
		pOptionsPane.setBorder(BorderFactory.createTitledBorder(""));

		JPanel pEdit = new JPanel();
		pEdit.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutManifestPreview")));
		pEdit.add(editManifestCheckBox);
			
		// build report
		JPanel pReport = new JPanel();
		pReport.setLayout(new GridBagLayout());		
		pReport.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutReportOptions")));
		// build report options
		addItem (pReport, textBuildReport, 0, 16);
		addItemLeft (pReport, buildReportMin, 1, 16);
		addItemLeft (pReport, buildReportNor, 2, 16);
		addItemLeft (pReport, buildReportMax, 3, 16);
		addItemLeft (pReport, buildReportVD, 4, 16);
		addItemWidth (pReport, buildReportCheckBox, 3, 1, 17);	
		
		pOptions.add(pEdit);
		pOptions.add(pReport);

		// row 11
		JPanel pControl = new JPanel();
		pControl.setBorder(BorderFactory.createTitledBorder(""));
		pControl.setLayout(new GridBagLayout());
		addItem(pControl, saveButton, 0, 0);
		
		getContentPane().add(pManifestPane);	
		getContentPane().add(pCommentPane);
		getContentPane().add(pOptionsPane);
		getContentPane().add(pControl);
		
		manifestOrientationComboBox.setSelectedItem(Setup.getManifestOrientation());
		switchListOrientationComboBox.setSelectedItem(Setup.getSwitchListOrientation());
		
		tabFormatCheckBox.setSelected(Setup.isTabEnabled());
		formatSwitchListCheckBox.setSelected(Setup.isSwitchListFormatSameAsManifest());
		printLocCommentsCheckBox.setSelected(Setup.isPrintLocationCommentsEnabled());
		printRouteCommentsCheckBox.setSelected(Setup.isPrintRouteCommentsEnabled());
		printLoadsEmptiesCheckBox.setSelected(Setup.isPrintLoadsAndEmptiesEnabled());
		printTimetableNameCheckBox.setSelected(Setup.isPrintTimetableNameEnabled());
		use12hrFormatCheckBox.setSelected(Setup.is12hrFormatEnabled());
		buildReportCheckBox.setSelected(Setup.isBuildReportEditorEnabled());
		editManifestCheckBox.setSelected(Setup.isManifestEditorEnabled());
		
		setSwitchListVisible(!formatSwitchListCheckBox.isSelected());
		
		updateLogoButtons();
		dropComboBox.setSelectedItem(Setup.getDropTextColor());
		pickupComboBox.setSelectedItem(Setup.getPickupTextColor());		
		localComboBox.setSelectedItem(Setup.getLocalTextColor());	
		
		commentTextArea.setText(Setup.getMiaComment());
		
		ButtonGroup buildReportGroup = new ButtonGroup();
		buildReportGroup.add(buildReportMin);
		buildReportGroup.add(buildReportNor);
		buildReportGroup.add(buildReportMax);
		buildReportGroup.add(buildReportVD);
		
		// load font sizes 7 through 14
		for (int i=7; i<15; i++)
			fontSizeComboBox.addItem(i);
		fontSizeComboBox.setSelectedItem(Setup.getFontSize());
		fontComboBox.setSelectedItem(Setup.getFontName());

		// setup buttons
		addButtonAction(addLogoButton);
		addButtonAction(removeLogoButton);
		addButtonAction(saveButton);
		
		addCheckBoxAction(formatSwitchListCheckBox);
		
		setBuildReportRadioButton();

		//	build menu		
		addHelpMenu("package.jmri.jmrit.operations.Operations_PrintOptions", true);

		pack();
		//setSize(getWidth(), getHeight()+55);	// pad out a bit
		setVisible(true);
	}
	
	// Add Remove Logo and Save buttons
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == addLogoButton){
			log.debug("add logo button pressed");
			File f = selectFile();
			if (f != null)
				Setup.setManifestLogoURL(f.getAbsolutePath());
			updateLogoButtons();
		}
		if (ae.getSource() == removeLogoButton){
			log.debug("remove logo button pressed");
			Setup.setManifestLogoURL("");
			updateLogoButtons();
		}
		if (ae.getSource() == saveButton){
			// font name
			Setup.setFontName((String)fontComboBox.getSelectedItem());
			// font size
			Setup.setFontSize((Integer)fontSizeComboBox.getSelectedItem());
			// page orientation
			Setup.setManifestOrientation((String)manifestOrientationComboBox.getSelectedItem());
			Setup.setSwitchListOrientation((String)switchListOrientationComboBox.getSelectedItem());
			// drop and pick up color option
			Setup.setDropTextColor((String)dropComboBox.getSelectedItem());
			Setup.setPickupTextColor((String)pickupComboBox.getSelectedItem());
			Setup.setLocalTextColor((String)localComboBox.getSelectedItem());
			// save engine pick up message format
			Setup.setPickupEnginePrefix(pickupEngPrefix.getText());
			String[] format = new String[enginePickupMessageList.size()];
			for (int i=0; i<enginePickupMessageList.size(); i++){
				JComboBox b = enginePickupMessageList.get(i);
				format[i] = (String)b.getSelectedItem();
			}
			Setup.setPickupEngineMessageFormat(format);
			// save engine drop message format
			Setup.setDropEnginePrefix(dropEngPrefix.getText());
			format = new String[engineDropMessageList.size()];
			for (int i=0; i<engineDropMessageList.size(); i++){
				JComboBox b = engineDropMessageList.get(i);
				format[i] = (String)b.getSelectedItem();
			}
			Setup.setDropEngineMessageFormat(format);
			// save car pick up message format
			Setup.setPickupCarPrefix(pickupCarPrefix.getText());
			format = new String[carPickupMessageList.size()];
			for (int i=0; i<carPickupMessageList.size(); i++){
				JComboBox b = carPickupMessageList.get(i);
				format[i] = (String)b.getSelectedItem();
			}
			Setup.setPickupCarMessageFormat(format);
			// save car drop message format
			Setup.setDropCarPrefix(dropCarPrefix.getText());
			format = new String[carDropMessageList.size()];
			for (int i=0; i<carDropMessageList.size(); i++){
				JComboBox b = carDropMessageList.get(i);
				format[i] = (String)b.getSelectedItem();
			}
			Setup.setDropCarMessageFormat(format);
			// save local message format
			Setup.setLocalPrefix(localPrefix.getText());
			format = new String[localMessageList.size()];
			for (int i=0; i<localMessageList.size(); i++){
				JComboBox b = localMessageList.get(i);
				format[i] = (String)b.getSelectedItem();
			}
			Setup.setLocalMessageFormat(format);
			// save switch list car pick up message format
			Setup.setSwitchListPickupCarPrefix(switchListPickupCarPrefix.getText());
			format = new String[switchListCarPickupMessageList.size()];
			for (int i=0; i<switchListCarPickupMessageList.size(); i++){
				JComboBox b = switchListCarPickupMessageList.get(i);
				format[i] = (String)b.getSelectedItem();
			}
			Setup.setSwitchListPickupCarMessageFormat(format);
			// save switch list car drop message format
			Setup.setSwitchListDropCarPrefix(switchListDropCarPrefix.getText());
			format = new String[switchListCarDropMessageList.size()];
			for (int i=0; i<switchListCarDropMessageList.size(); i++){
				JComboBox b = switchListCarDropMessageList.get(i);
				format[i] = (String)b.getSelectedItem();
			}
			Setup.setSwitchListDropCarMessageFormat(format);
			// save switch list local message format
			Setup.setSwitchListLocalPrefix(switchListLocalPrefix.getText());
			format = new String[switchListLocalMessageList.size()];
			for (int i=0; i<switchListLocalMessageList.size(); i++){
				JComboBox b = switchListLocalMessageList.get(i);
				format[i] = (String)b.getSelectedItem();
			}
			Setup.setSwitchListLocalMessageFormat(format);
			// misplaced car comment
			Setup.setMiaComment(commentTextArea.getText());
			// build report level
			if (buildReportMin.isSelected())
				Setup.setBuildReportLevel(Setup.BUILD_REPORT_MINIMAL);
			else if (buildReportNor.isSelected())
				Setup.setBuildReportLevel(Setup.BUILD_REPORT_NORMAL);
			else if (buildReportMax.isSelected())
				Setup.setBuildReportLevel(Setup.BUILD_REPORT_DETAILED);
			else if (buildReportVD.isSelected())
				Setup.setBuildReportLevel(Setup.BUILD_REPORT_VERY_DETAILED);
			Setup.setTabEnabled(tabFormatCheckBox.isSelected());
			Setup.setSwitchListFormatSameAsManifest(formatSwitchListCheckBox.isSelected());
			Setup.setPrintLocationCommentsEnabled(printLocCommentsCheckBox.isSelected());
			Setup.setPrintRouteCommentsEnabled(printRouteCommentsCheckBox.isSelected());
			Setup.setPrintLoadsAndEmptiesEnabled(printLoadsEmptiesCheckBox.isSelected());
			Setup.set12hrFormatEnabled(use12hrFormatCheckBox.isSelected());
			Setup.setPrintTimetableNameEnabled(printTimetableNameCheckBox.isSelected());
			Setup.setManifestEditorEnabled(editManifestCheckBox.isSelected());
			Setup.setBuildReportEditorEnabled(buildReportCheckBox.isSelected());
			OperationsSetupXml.instance().writeOperationsFile();
			// Check font if user selected tab output
			if (Setup.isTabEnabled() && (!Setup.getFontName().equals(Setup.COURIER) && !Setup.getFontName().equals(Setup.MONOSPACED))){
				JOptionPane.showMessageDialog(this,
						rb.getString("TabWorksBest"), rb.getString("ChangeFont"),
						JOptionPane.WARNING_MESSAGE);				
			}
			if (Setup.isCloseWindowOnSaveEnabled())
				dispose();
		}
	}
	
	public void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
		log.debug("Switch list check box activated");
		setSwitchListVisible(!formatSwitchListCheckBox.isSelected());
	}
	
	private void setSwitchListVisible(boolean b){
		pSwitchListOrientation.setVisible(b);
		pSwPickup.setVisible(b);
		pSwDrop.setVisible(b);
		pSwLocal.setVisible(b);
	}

	/**
	 * We always use the same file chooser in this class, so that the user's
	 * last-accessed directory remains available.
	 */
	JFileChooser fc = jmri.jmrit.XmlFile.userFileChooser("Images");

	private File selectFile() {
		if (fc==null) {
			log.error("Could not find user directory");
		} else {
			fc.setDialogTitle("Find desired image");
			// when reusing the chooser, make sure new files are included
			fc.rescanCurrentDirectory();
		}

		int retVal = fc.showOpenDialog(null);
		// handle selection or cancel
		if (retVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			return file;
		}
		return null;
	}

	private void updateLogoButtons(){
		boolean flag = Setup.getManifestLogoURL().equals("");
		addLogoButton.setVisible(flag);
		removeLogoButton.setVisible(!flag);
		logoURL.setText(Setup.getManifestLogoURL());
		pack();
	}
	
	private void setBuildReportRadioButton(){
		buildReportMin.setSelected(Setup.getBuildReportLevel().equals(Setup.BUILD_REPORT_MINIMAL));
		buildReportNor.setSelected(Setup.getBuildReportLevel().equals(Setup.BUILD_REPORT_NORMAL));
		buildReportMax.setSelected(Setup.getBuildReportLevel().equals(Setup.BUILD_REPORT_DETAILED));
		buildReportVD.setSelected(Setup.getBuildReportLevel().equals(Setup.BUILD_REPORT_VERY_DETAILED));
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(OperationsSetupFrame.class.getName());
}
