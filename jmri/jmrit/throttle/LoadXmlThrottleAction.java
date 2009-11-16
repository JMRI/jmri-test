package jmri.jmrit.throttle;

import jmri.jmrit.XmlFile;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import java.util.List;
import java.util.ResourceBundle;

import org.jdom.Element;

/**
 *  Load throttles from XML
 *
 * @author     Glen Oberhauser 2004
 * @version     $Revision: 1.21 $
 */
public class LoadXmlThrottleAction extends AbstractAction {
	ResourceBundle rb = ResourceBundle
			.getBundle("jmri.jmrit.throttle.ThrottleBundle");

	/**
	 *  Constructor
	 *
	 * @param  s  Name for the action.
	 */
	public LoadXmlThrottleAction(String s) {
		super(s);
		// disable the ourselves if there is no throttle Manager
		if (jmri.InstanceManager.throttleManagerInstance() == null) {
			setEnabled(false);
		}
	}

	public LoadXmlThrottleAction() {
		this("Load Throttle");
	}

	JFileChooser fileChooser;

	/**
	 *  The action is performed. Let the user choose the file to load from. Read
	 *  XML for each ThrottleFrame.
	 *
	 * @param  e  The event causing the action.
	 */
	public void actionPerformed(ActionEvent e) {
		if (fileChooser == null) {
			fileChooser = jmri.jmrit.XmlFile.userFileChooser(rb
					.getString("PromptXmlFileTypes"), "xml");
			fileChooser.setCurrentDirectory(new File(StoreXmlThrottleAction
					.defaultThrottleDirectory()));
		}
		int retVal = fileChooser.showOpenDialog(null);
		if (retVal != JFileChooser.APPROVE_OPTION) {
			return;
			// give up if no file selected
		}

		// if exising frames are open ask to destroy those or merge.
		if (ThrottleFrameManager.instance().getThrottleWindows().hasNext()) {
			Object[] possibleValues = { rb.getString("LabelMerge"),
					rb.getString("LabelReplace"), rb.getString("LabelCancel") };
			int selectedValue = JOptionPane.showOptionDialog(null, rb
					.getString("DialogMergeOrReplace"), rb
					.getString("OptionLoadingThrottles"),
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.INFORMATION_MESSAGE, null, possibleValues,
					possibleValues[0]);
			if (selectedValue == JOptionPane.NO_OPTION) {
				// replace chosen - close all then load
				ThrottleFrameManager.instance().requestAllThrottleWindowsDestroyed();
			}
		}
		try {
		    loadThrottles(fileChooser.getSelectedFile());
	    } catch (java.io.IOException e1) {
	        log.warn("Exception while reading file", e1);
	    }
	}

	/**
	 *  Parse the XML file and create ThrottleFrames.
	 *  Returns true if throttle loaded successfully.
	 *
	 * @param  f  The XML file containing throttles.
	 */
	@SuppressWarnings("unchecked")
	public boolean loadThrottles(java.io.File f) throws java.io.IOException {
		try {
			ThrottlePrefs prefs = new ThrottlePrefs();
			Element root = prefs.rootFromFile(f);
			List<Element> throttles = root.getChildren("ThrottleFrame");
			if ((throttles != null) && (throttles.size()>0)) { // OLD FORMAT				
				for (java.util.Iterator<Element> i = throttles.iterator(); i.hasNext();) {
					ThrottleFrame tf = ThrottleFrameManager.instance().createThrottleFrame();
					tf.setXml(i.next());
					tf.setVisible(true);
				}
			}
			else {
				throttles = root.getChildren("ThrottleWindow");
				for (java.util.Iterator<Element> i = throttles.iterator(); i.hasNext();) {
					ThrottleWindow tw = ThrottleFrameManager.instance().createThrottleWindow();
					tw.setXml(i.next());
					tw.setVisible(true);
				}
				Element tlp = root.getChild("ThrottlesListPanel");
				if (tlp!=null) {
					ThrottleFrameManager.instance().getThrottlesListPanel().setXml(tlp);
				}
			}
		} catch (org.jdom.JDOMException ex) {
			log.warn("Loading Throttles exception",ex);
			return false;
		}
		return true;
	}

	/**
	 * An extension of the abstract XmlFile. No changes made to that class.
	 * 
	 * @author glen
	 * @version $Revision: 1.21 $
	 */
	class ThrottlePrefs extends XmlFile {

	}

	// initialize logging
	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(LoadXmlThrottleAction.class.getName());

}
