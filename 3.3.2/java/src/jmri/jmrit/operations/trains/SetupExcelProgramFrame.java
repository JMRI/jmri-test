// RunExcelProgramFrame.java

package jmri.jmrit.operations.trains;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.text.MessageFormat;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.setup.Setup;

/**
 * Frame for user edit of the name and directory of an Excel program.
 * 
 * @author Dan Boudreau Copyright (C) 2013
 * @version $Revision: 22249 $
 */

public class SetupExcelProgramFrame extends OperationsFrame {

	TrainManager manager = TrainManager.instance();

	// text windows
	JTextField fileName = new JTextField(30);

	// major buttons
	JButton testButton = new JButton(Bundle.getMessage("Test"));
	JButton saveButton = new JButton(Bundle.getMessage("Save"));

	public SetupExcelProgramFrame() {
		super();
	}

	public void initComponents() {

		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		// Layout the panel by rows

		// row 1
		JPanel pDirectoryName = new JPanel();
		pDirectoryName.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("DirectoryName")));
		pDirectoryName.add(new JLabel(CustomManifest.getDirectoryName()));

		JPanel pFileName = new JPanel();
		pFileName.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("FileName")));
		pFileName.add(fileName);
		
		fileName.setText(CustomManifest.getFileName());

		// row 4 buttons
		JPanel pB = new JPanel();
		pB.setLayout(new GridBagLayout());
		addItem(pB, testButton, 1, 0);
		addItem(pB, saveButton, 3, 0);

		getContentPane().add(pDirectoryName);
		getContentPane().add(pFileName);
		getContentPane().add(pB);

		// setup buttons
		addButtonAction(testButton);
		addButtonAction(saveButton);

		addHelpMenu("package.jmri.jmrit.operations.Operations_TrainScripts", true); // NOI18N
		setTitle(Bundle.getMessage("MenuItemSetupExcelProgram"));
		
		setMinimumSize(new Dimension(300, 200));
		pack();
		setVisible(true);
	}

	// Save and Test
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		
		CustomManifest.setFileName(fileName.getText());

		if (ae.getSource() == testButton) {
			if (CustomManifest.manifestCreatorFileExists()) {
				JOptionPane.showMessageDialog(this, MessageFormat.format(Bundle
						.getMessage("DirectoryNameFileName"), new Object[] {
						CustomManifest.getDirectoryName(), CustomManifest.getFileName() }), Bundle
						.getMessage("ManifestCreatorFound"), JOptionPane.INFORMATION_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(this, MessageFormat.format(Bundle
						.getMessage("DirectoryNameFileName"), new Object[] {
						CustomManifest.getDirectoryName(), CustomManifest.getFileName() }), Bundle
						.getMessage("ManifestCreatorNotFound"), JOptionPane.ERROR_MESSAGE);
			}
		}
		if (ae.getSource() == saveButton) {
			log.debug("Save button activated");		
			TrainManagerXml.instance().writeOperationsFile();
			if (Setup.isCloseWindowOnSaveEnabled())
				dispose();
		}
	}

	public void dispose() {
		super.dispose();
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SetupExcelProgramFrame.class
			.getName());
}
