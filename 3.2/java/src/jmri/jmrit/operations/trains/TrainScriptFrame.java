// TrainScriptFrame.java

package jmri.jmrit.operations.trains;

import java.awt.GridBagLayout;
import java.util.ResourceBundle;
import java.util.List;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
//import javax.swing.JMenu;
//import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.FileUtil;


/**
 * Frame for user edit of a train's script options. Allows the user to execute
 * scripts when a train is built, moved or terminated.
 * 
 * @author Bob Jacobsen Copyright (C) 2004
 * @author Dan Boudreau Copyright (C) 2010, 2011
 * @version $Revision$
 */

public class TrainScriptFrame extends OperationsFrame {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.trains.JmritOperationsTrainsBundle");
	
	TrainManager manager;
	TrainManagerXml managerXml;

	Train _train = null;

	// script panels
	JPanel pBuildScript = new JPanel();
	JPanel pAfterBuildScript = new JPanel();
	JPanel pMoveScript = new JPanel();
	JPanel pTerminationScript = new JPanel();
	JScrollPane buildScriptPane;
	JScrollPane afterBuildScriptPane;
	JScrollPane moveScriptPane;
	JScrollPane terminationScriptPane;

	// labels
	JLabel trainName = new JLabel();
	JLabel trainDescription = new JLabel();

	// major buttons
	JButton addBuildScriptButton = new JButton(rb.getString("AddScript"));
	JButton addAfterBuildScriptButton = new JButton(rb.getString("AddScript"));
	JButton addMoveScriptButton = new JButton(rb.getString("AddScript"));
	JButton addTerminationScriptButton = new JButton(rb.getString("AddScript"));
	
	JButton runBuildScriptButton = new JButton(rb.getString("RunScripts"));
	JButton runAfterBuildScriptButton = new JButton(rb.getString("RunScripts"));
	JButton runMoveScriptButton = new JButton(rb.getString("RunScripts"));
	JButton runTerminationScriptButton = new JButton(rb.getString("RunScripts"));
	
	JButton saveTrainButton = new JButton(rb.getString("SaveTrain"));

	public TrainScriptFrame() {
		super();
 	}

	public void initComponents(TrainEditFrame parent) {
    	// Set up script options in a Scroll Pane..
     	buildScriptPane = new JScrollPane(pBuildScript);
      	buildScriptPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
      	buildScriptPane.setBorder(BorderFactory.createTitledBorder(rb.getString("ScriptsBeforeBuild")));
      	
     	afterBuildScriptPane = new JScrollPane(pAfterBuildScript);
      	afterBuildScriptPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
      	afterBuildScriptPane.setBorder(BorderFactory.createTitledBorder(rb.getString("ScriptsAfterBuild")));

     	moveScriptPane = new JScrollPane(pMoveScript);
      	moveScriptPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
      	moveScriptPane.setBorder(BorderFactory.createTitledBorder(rb.getString("ScriptsWhenMoved")));
 
      	terminationScriptPane = new JScrollPane(pTerminationScript);
      	terminationScriptPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
      	terminationScriptPane.setBorder(BorderFactory.createTitledBorder(rb.getString("ScriptsWhenTerminated")));  	

		// remember who called us
		parent.setChildFrame(this);
		_train = parent._train;

		// load managers
		manager = TrainManager.instance();
		managerXml = TrainManagerXml.instance();
	
	    getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
				
		// Layout the panel by rows
	   	JPanel p1 = new JPanel();
    	p1.setLayout(new BoxLayout(p1,BoxLayout.X_AXIS));
				
		// row 1a
       	JPanel pName = new JPanel();
    	pName.setLayout(new GridBagLayout());
    	pName.setBorder(BorderFactory.createTitledBorder(rb.getString("Name")));
    	addItem(pName, trainName, 0, 0);

		// row 1b
       	JPanel pDesc = new JPanel();
    	pDesc.setLayout(new GridBagLayout());
    	pDesc.setBorder(BorderFactory.createTitledBorder(rb.getString("Description")));
    	addItem(pDesc, trainDescription, 0, 0);
		
    	p1.add(pName);
    	p1.add(pDesc);
    	
		// row 2
    	updateBuildScriptPanel();
    	
		// row 3
    	updateAfterBuildScriptPanel();
    
		// row 4
    	updateMoveScriptPanel();
    	
		// row 6
    	updateTerminationScriptPanel();
 
		// row 8 buttons
	   	JPanel pB = new JPanel();
    	pB.setLayout(new GridBagLayout());		
		addItem(pB, saveTrainButton, 3, 0);
		
		getContentPane().add(p1);
		getContentPane().add(buildScriptPane);
		getContentPane().add(afterBuildScriptPane);
		getContentPane().add(moveScriptPane);
		getContentPane().add(terminationScriptPane);
      	getContentPane().add(pB);
		
		// setup buttons
      	addButtonAction(addBuildScriptButton);
      	addButtonAction(addAfterBuildScriptButton);
      	addButtonAction(addMoveScriptButton);
		addButtonAction(addTerminationScriptButton);		
      	addButtonAction(runBuildScriptButton);
      	addButtonAction(runAfterBuildScriptButton);
      	addButtonAction(runMoveScriptButton);
		addButtonAction(runTerminationScriptButton);		
		addButtonAction(saveTrainButton);
		
		if (_train != null){
			trainName.setText(_train.getName());
			trainDescription.setText(_train.getDescription());
			enableButtons(true);
		} else {
			enableButtons(false);
		}
		addHelpMenu("package.jmri.jmrit.operations.Operations_TrainScripts", true);
		packFrame();
	}
	
	private void updateBuildScriptPanel(){
		pBuildScript.removeAll();
	   	pBuildScript.setLayout(new GridBagLayout());
    	addItem(pBuildScript, addBuildScriptButton, 0, 0);
    	
    	// load any existing train build scripts
    	if (_train != null){
    		List<String> scripts = _train.getBuildScripts();
    		if (scripts.size()>0)
    			addItem(pBuildScript, runBuildScriptButton, 1, 0);
    		for (int i=0; i<scripts.size(); i++){
    			JButton removeBuildScripts = new JButton(rb.getString("RemoveScript"));
    			removeBuildScripts.setName(scripts.get(i));
    			removeBuildScripts.addActionListener(new java.awt.event.ActionListener() {
    				public void actionPerformed(java.awt.event.ActionEvent e) {
    					buttonActionRemoveBuildScript(e);
    				}
    			});
    			addButtonAction(removeBuildScripts);
    			JLabel pathname = new JLabel(scripts.get(i));
    			addItem(pBuildScript, removeBuildScripts, 0, i+1);
    			addItem(pBuildScript, pathname, 1, i+1);
    		}
    	}
	}
	
	private void updateAfterBuildScriptPanel(){
		pAfterBuildScript.removeAll();
	   	pAfterBuildScript.setLayout(new GridBagLayout());
    	addItem(pAfterBuildScript, addAfterBuildScriptButton, 0, 0);
    	
    	// load any existing train build scripts
    	if (_train != null){
    		List<String> scripts = _train.getAfterBuildScripts();
    		if (scripts.size()>0)
    			addItem(pAfterBuildScript, runAfterBuildScriptButton, 1, 0);
    		for (int i=0; i<scripts.size(); i++){
    			JButton removeAfterBuildScripts = new JButton(rb.getString("RemoveScript"));
    			removeAfterBuildScripts.setName(scripts.get(i));
    			removeAfterBuildScripts.addActionListener(new java.awt.event.ActionListener() {
    				public void actionPerformed(java.awt.event.ActionEvent e) {
    					buttonActionRemoveAfterBuildScript(e);
    				}
    			});
    			addButtonAction(removeAfterBuildScripts);
    			JLabel pathname = new JLabel(scripts.get(i));
    			addItem(pAfterBuildScript, removeAfterBuildScripts, 0, i+1);
    			addItem(pAfterBuildScript, pathname, 1, i+1);
    		}
    	}
	}
	
	private void updateMoveScriptPanel(){
		pMoveScript.removeAll();
	   	pMoveScript.setLayout(new GridBagLayout());
    	addItem(pMoveScript, addMoveScriptButton, 0, 0);
    	
    	// load any existing train move scripts
    	if (_train != null){
    		List<String> scripts = _train.getMoveScripts();
      		if (scripts.size()>0)
    			addItem(pMoveScript, runMoveScriptButton, 1, 0);
    		for (int i=0; i<scripts.size(); i++){
    			JButton removeMoveScripts = new JButton(rb.getString("RemoveScript"));
    			removeMoveScripts.setName(scripts.get(i));
    			removeMoveScripts.addActionListener(new java.awt.event.ActionListener() {
    				public void actionPerformed(java.awt.event.ActionEvent e) {
    					buttonActionRemoveMoveScript(e);
    				}
    			});
    			addButtonAction(removeMoveScripts);
    			JLabel pathname = new JLabel(scripts.get(i));
    			addItem(pMoveScript, removeMoveScripts, 0, i+1);
    			addItem(pMoveScript, pathname, 1, i+1);
    		}
    	}
	}
	
	private void updateTerminationScriptPanel(){
		pTerminationScript.removeAll();
	   	pTerminationScript.setLayout(new GridBagLayout());
    	addItem(pTerminationScript, addTerminationScriptButton, 0, 0);
    	
    	// load any existing train termination scripts
    	if (_train != null){
    		List<String> scripts = _train.getTerminationScripts();
      		if (scripts.size()>0)
    			addItem(pTerminationScript, runTerminationScriptButton, 1, 0);
    		for (int i=0; i<scripts.size(); i++){
    			JButton removeTerminationScripts = new JButton(rb.getString("RemoveScript"));
    			removeTerminationScripts.setName(scripts.get(i));
       			removeTerminationScripts.addActionListener(new java.awt.event.ActionListener() {
    				public void actionPerformed(java.awt.event.ActionEvent e) {
    					buttonActionRemoveTerminationScript(e);
    				}
    			});
    			JLabel pathname = new JLabel(scripts.get(i));
    			addItem(pTerminationScript, removeTerminationScripts, 0, i+1);
    			addItem(pTerminationScript, pathname, 1, i+1);
    		}
    	}
	}
	
	// Save train, add scripts buttons
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (_train != null){
			if (ae.getSource() == addBuildScriptButton){
				log.debug("train add build script button activated");
				File f = selectFile();
				if (f != null){
					_train.addBuildScript(FileUtil.getPortableFilename(f));
					updateBuildScriptPanel();
					packFrame();
				}
			}
			if (ae.getSource() == addAfterBuildScriptButton){
				log.debug("train add after build script button activated");
				File f = selectFile();
				if (f != null){
					_train.addAfterBuildScript(FileUtil.getPortableFilename(f));
					updateAfterBuildScriptPanel();
					packFrame();
				}
			}
			if (ae.getSource() == addMoveScriptButton){
				log.debug("train add move script button activated");
				File f = selectFile();
				if (f != null){
					_train.addMoveScript(FileUtil.getPortableFilename(f));
					updateMoveScriptPanel();
					packFrame();
				}
			}
			if (ae.getSource() == addTerminationScriptButton){
				log.debug("train add termination script button activated");
				File f = selectFile();
				if (f != null){
					_train.addTerminationScript(FileUtil.getPortableFilename(f));
					updateTerminationScriptPanel();
					packFrame();
				}
			}
			if (ae.getSource() == runBuildScriptButton){
				runScripts(_train.getBuildScripts());				
			}
			if (ae.getSource() == runAfterBuildScriptButton){
				runScripts(_train.getAfterBuildScripts());				
			}
			if (ae.getSource() == runMoveScriptButton){
				runScripts(_train.getMoveScripts());				
			}
			if (ae.getSource() == runTerminationScriptButton){
				runScripts(_train.getTerminationScripts());
			}
			if (ae.getSource() == saveTrainButton){
				log.debug("train save button activated");
				OperationsXml.save();
				if (Setup.isCloseWindowOnSaveEnabled())
					dispose();
			}
		}
	}
	
	public void buttonActionRemoveBuildScript(java.awt.event.ActionEvent ae){
		if (_train != null){
			JButton rb = (JButton)ae.getSource();
			log.debug("remove build script button activated "+rb.getName());
			_train.deleteBuildScript(rb.getName());
			updateBuildScriptPanel();
			packFrame();
		}
	}
	
	public void buttonActionRemoveAfterBuildScript(java.awt.event.ActionEvent ae){
		if (_train != null){
			JButton rb = (JButton)ae.getSource();
			log.debug("remove after build script button activated "+rb.getName());
			_train.deleteAfterBuildScript(rb.getName());
			updateAfterBuildScriptPanel();
			packFrame();
		}
	}
	
	public void buttonActionRemoveMoveScript(java.awt.event.ActionEvent ae){
		if (_train != null){
			JButton rb = (JButton)ae.getSource();
			log.debug("remove move script button activated "+rb.getName());
			_train.deleteMoveScript(rb.getName());
			updateMoveScriptPanel();
			packFrame();
		}
	}
	
	public void buttonActionRemoveTerminationScript(java.awt.event.ActionEvent ae){
		if (_train != null){
			JButton rb = (JButton)ae.getSource();
			log.debug("remove termination script button activated "+rb.getName());
			_train.deleteTerminationScript(rb.getName());
			updateTerminationScriptPanel();
			packFrame();
		}
	}
	
	private void runScripts(List<String> scripts){
		for (int i=0; i<scripts.size(); i++){
			jmri.util.PythonInterp.runScript(jmri.util.FileUtil.getExternalFilename(scripts.get(i)));
		}
	}
	

	/**
	 * We always use the same file chooser in this class, so that
	 * the user's last-accessed directory remains available.
	 */
	JFileChooser fc = jmri.jmrit.XmlFile.userFileChooser("Python script files", "py");

	private File selectFile() {
		if (fc==null) {
        	log.error("Could not find user directory");
        } else {
            fc.setDialogTitle("Find desired script file");
            // when reusing the chooser, make sure new files are included
            fc.rescanCurrentDirectory();
        }

        int retVal = fc.showOpenDialog(null);
        // handle selection or cancel
        if (retVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            // Run the script from it's filename
            return file;
        }
        return null;
    }
	
	private void enableButtons(boolean enabled){
		addBuildScriptButton.setEnabled(enabled);
		addAfterBuildScriptButton.setEnabled(enabled);
		addMoveScriptButton.setEnabled(enabled);
		addTerminationScriptButton.setEnabled(enabled);
		saveTrainButton.setEnabled(enabled);
	}
	
    private void packFrame(){
    	setPreferredSize(null);
 		pack();
 		if(getWidth()<600)
 			setSize(600, getHeight());
 		if (getHeight()<Control.panelHeight)
 			setSize(getWidth(), Control.panelHeight);
		setVisible(true);
    }
 	
	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(TrainScriptFrame.class.getName());
}
