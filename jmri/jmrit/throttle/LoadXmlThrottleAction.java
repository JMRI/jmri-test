package jmri.jmrit.throttle;

import java.awt.event.ActionEvent;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import com.sun.java.util.collections.List;
import jmri.InstanceManager;
import jmri.jmrit.XmlFile;
import jmri.jmrit.throttle.ThrottleFrame;
import jmri.jmrit.throttle.ThrottleFrameManager;
import java.util.Iterator;
import java.util.ArrayList;

import org.jdom.Element;

/**
 *  Load throttles from XML
 *
 * @author     Glen Oberhauser
 * @created    March 27, 2003
 * @version
 */
public class LoadXmlThrottleAction extends AbstractAction
{

	/**
	 *  Constructor
	 *
	 * @param  s  Name for the action.
	 */
	public LoadXmlThrottleAction(String s)
	{
		super(s);
	}

	/**
	 *  The action is performed. Let the user choose the file to load from. Read
	 *  XML for each ThrottleFrame.
	 *
	 * @param  e  The event causing the action.
	 */
	public void actionPerformed(ActionEvent e)
	{
		JFileChooser fileChooser = new JFileChooser(XmlFile.prefsDir());
		int retVal = fileChooser.showOpenDialog(null);
		if (retVal != JFileChooser.APPROVE_OPTION)
		{
			return;
			// give up if no file selected
		}

		// if exising frames are open ask to destroy those or merge.
		if (InstanceManager.throttleFrameManagerInstance().getThrottleFrames().hasNext())
		{
			Object[] possibleValues = {"Merge", "Replace", "Cancel"};
			int selectedValue = JOptionPane.showOptionDialog(null,
					"Throttles are currently open.\nDo you wish to Merge "
					 + "saved throttles with open throttles or\n"
					 + "Replace open throttles",
					"Loading Throttles",
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.INFORMATION_MESSAGE, null,
					possibleValues, possibleValues[0]);
			if (selectedValue == JOptionPane.NO_OPTION)
			{
				// replace chosen - close all then load
				InstanceManager.throttleFrameManagerInstance().requestAllThrottleFramesDestroyed();
			}
		}
		loadThrottles(fileChooser.getSelectedFile());
	}

	/**
	 *  Parse the XML file and create ThrottleFrames.
	 *
	 * @param  f  The XML file containing throttles.
	 */
	private void loadThrottles(java.io.File f)
	{
		try
		{
			ThrottlePrefs prefs = new ThrottlePrefs();
			Element root = prefs.rootFromFile(f);
			List throttles = root.getChildren("ThrottleFrame");
			for (com.sun.java.util.collections.Iterator i = throttles.iterator(); i.hasNext(); )
			{
				ThrottleFrame tf = InstanceManager.throttleFrameManagerInstance().createThrottleFrame();
				tf.setXml((Element) i.next());
				tf.setVisible(true);
			}

		}
		catch (org.jdom.JDOMException ex)
		{
			log.warn("Loading Throttles exception:"+ex);
		}
		catch (FileNotFoundException ex)
		{
			log.warn("Loading Throttles exception:"+ex);
		}

	}


	/**
	 *  An extension of the abstract XmlFile. No changes made to that class.
	 *
	 * @author     glen
	 * @created    March 27, 2003
	 */
	class ThrottlePrefs extends XmlFile
	{

	}
	// initialize logging
	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LoadXmlThrottleAction.class.getName());

}
