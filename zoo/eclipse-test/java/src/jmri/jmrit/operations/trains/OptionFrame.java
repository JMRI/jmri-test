// OptionFrame.java

package jmri.jmrit.operations.trains;

import java.awt.GridBagLayout;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.setup.Setup;

import java.io.File;


/**
 * Frame for user edit of train options
 * 
 * @author Dan Boudreau Copyright (C) 2010
 * @version $Revision$
 */

public class OptionFrame extends OperationsFrame{

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.trains.JmritOperationsTrainsBundle");
	
	Train _train = null;
	TrainEditFrame _trainEditFrame;
	
	// labels
	JLabel textPad = new JLabel("   ");
	JLabel logoURL = new JLabel("");

	// major buttons	
	JButton saveButton = new JButton(rb.getString("Save"));
	JButton addLogoButton = new JButton(rb.getString("AddLogo"));
	JButton removeLogoButton = new JButton(rb.getString("RemoveLogo"));

	// radio buttons		
    
    // check boxes
	
	// text fields
	JTextField railroadNameTextField = new JTextField(35);
	JTextField logoTextField = new JTextField(35);
	
	// combo boxes

	public OptionFrame() {
		super(ResourceBundle.getBundle("jmri.jmrit.operations.setup.JmritOperationsSetupBundle").getString("TitleOptions"));
	}

	public void initComponents(TrainEditFrame parent) {
		
		// the following code sets the frame's initial state
		_trainEditFrame = parent;
		_trainEditFrame.setChildFrame(this);
		_train = _trainEditFrame._train;
		
		// add tool tips
		addLogoButton.setToolTipText(rb.getString("AddLogoToolTip"));
		removeLogoButton.setToolTipText(rb.getString("RemoveLogoToolTip"));
			
		// Option panel
		getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));		
		
		JPanel pOptionName = new JPanel();
		pOptionName.setLayout(new GridBagLayout());
		JScrollPane pOptionNamePane = new JScrollPane(pOptionName);
		pOptionNamePane.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutRailRoadName")));
		addItemWidth (pOptionName, railroadNameTextField, 3, 1, 1);
		
		// manifest logo
		JPanel pOptionLogo = new JPanel();
		pOptionLogo.setLayout(new GridBagLayout());
		JScrollPane pOptionLogoPane = new JScrollPane(pOptionLogo);
		pOptionLogoPane.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutLogo")));
		addItem (pOptionLogo, textPad, 2, 18);
		addItem (pOptionLogo, addLogoButton, 2, 20);
		addItemLeft (pOptionLogo, removeLogoButton, 0, 21);
		addItemWidth (pOptionLogo, logoURL, 6, 1, 21);
		updateLogoButtons();
		
		// row 11
		JPanel pControl = new JPanel();
		pControl.setLayout(new GridBagLayout());
		addItem(pControl, saveButton, 3, 9);
		
		getContentPane().add(pOptionNamePane);
		getContentPane().add(pOptionLogoPane);
		getContentPane().add(pControl);

		// setup buttons
		addButtonAction(addLogoButton);
		addButtonAction(removeLogoButton);
		addButtonAction(saveButton);
		
		// load fields
		if (_train != null){
			railroadNameTextField.setText(_train.getRailroadName());	
		}

		//	build menu		
		addHelpMenu("package.jmri.jmrit.operations.Operations_Trains", true);

		pack();
		if (getWidth()<300)
			setSize(getWidth()+50, getHeight()+25);	// pad out a bit
		setVisible(true);
	}
	
	private void updateLogoButtons(){
		if (_train != null){
			boolean flag = _train.getManifestLogoURL().equals("");
			addLogoButton.setVisible(flag);
			removeLogoButton.setVisible(!flag);
			logoURL.setText(_train.getManifestLogoURL());
			pack();
		}
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
	
	// Save button
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == addLogoButton){
			log.debug("add logo button pressed");
			File f = selectFile();
			if (f != null)
				_train.setManifestLogoURL(f.getAbsolutePath());
			updateLogoButtons();
		}
		if (ae.getSource() == removeLogoButton){
			log.debug("remove logo button pressed");
			_train.setManifestLogoURL("");
			updateLogoButtons();
		}
		if (ae.getSource() == saveButton){
			if (_train != null)
				_train.setRailroadName(railroadNameTextField.getText());
			TrainManagerXml.instance().writeOperationsFile();
			if (Setup.isCloseWindowOnSaveEnabled())
				dispose();
		}
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(OptionFrame.class.getName());
}
