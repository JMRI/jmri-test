package jmri.jmrit.throttle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import jmri.jmrit.XmlFile;

/**
 *  Interface for allocating and deallocating throttles frames. Not to be
 *  confused with ThrottleManager
 *
 * @author     Glen Oberhauser
 * @version    $Revision: 1.17 $
 */
public class ThrottleFrameManager
{
    /** record the single instance of Roster **/
    private static ThrottleFrameManager instance = null;

	private static int NEXT_THROTTLE_KEY = KeyEvent.VK_RIGHT;
	private static int PREV_THROTTLE_KEY = KeyEvent.VK_LEFT;

	private int activeFrame;
	private ThrottleCyclingKeyListener throttleCycler;

	private ArrayList<ThrottleWindow> throttleWindows;
	private FunctionButtonPropertyEditor functionButtonEditor;
	private ThrottleFramePropertyEditor throttleFramePropertyEditor;
	
	private ThrottlesPreferences throttlesPref ;


	/**
	 *  Constructor for the ThrottleFrameManager object
	 */
	public ThrottleFrameManager()
	{
		throttleCycler = new ThrottleCyclingKeyListener();
		throttleWindows = new ArrayList<ThrottleWindow>(0);
		throttlesPref = new ThrottlesPreferences(XmlFile.prefsDir()+ "throttle" +File.separator+ "ThrottlesPreferences.xml");
	}

	/**
	 * Get the singleton instance of this class.
	 */
	public static ThrottleFrameManager instance()
	{
		if (instance == null)
		{
			instance = new ThrottleFrameManager();
		}
		return instance;
	}


	/**
	 *  Tell this manager that a new ThrottleFrame was created.
	 * @return The newly created ThrottleFrame
	 */
	public ThrottleFrame createThrottleFrame()
	{
		ThrottleWindow tw = new ThrottleWindow();
		tw.pack();
		KeyListenerInstaller.installKeyListenerOnAllComponents(throttleCycler, tw);
		throttleWindows.add(tw);
		activeFrame = throttleWindows.indexOf(tw);
		return tw.getCurentThrottleFrame() ;
	}
	
	/**
	 *  Request that this manager destroy a throttle frame.
	 *
	 * @param  frame  The to-be-destroyed ThrottleFrame
	 */
	public void requestThrottleFrameDestruction(ThrottleWindow frame)
	{
		if (frame != null)
		{
			throttleWindows.remove(throttleWindows.indexOf(frame));
			destroyThrottleFrame(frame);
			if (throttleWindows.size() > 0)
			{
				requestFocusForNextFrame();
			}
		}
	}

	public void requestAllThrottleFramesDestroyed()
	{
		for (Iterator<ThrottleWindow> i = throttleWindows.iterator(); i.hasNext();)
		{
			ThrottleWindow frame = i.next();
			destroyThrottleFrame(frame);
		}
		throttleWindows = new ArrayList<ThrottleWindow>(0);
	}

	/**
	 * Perform the destruction of a ThrottleFrame. This method will not
	 * affect the throttleFrames list, thus ensuring no synchronozation problems.
	 * @param frame The ThrottleFrame to be destroyed.
	 */
	private void destroyThrottleFrame(ThrottleWindow frame)
	{
		frame.dispose();
	}

	/**
	 *  Retrieve an Iterator over all the ThrottleFrames in existence.
	 *
	 * @return    The Iterator on the list of ThrottleFrames.
	 */
	public Iterator<ThrottleWindow> getThrottleFrames()
	{
		return throttleWindows.iterator();
	}

	/**
	 *  Get a reference to the Function Editor Allows us to have one editor without
	 *  disposing and creating each time.
	 */
	public FunctionButtonPropertyEditor getFunctionButtonEditor()
	{
		if (functionButtonEditor == null)
		{
			functionButtonEditor = new FunctionButtonPropertyEditor();
		}
		return functionButtonEditor;
	}

	/**
	 *  Get a reference to the ThrottleFrame Editor. Allows us to have one editor without
	 *  disposing and creating each time.
	 */
	public ThrottleFramePropertyEditor getThrottleFrameEditor()
	{
		if (throttleFramePropertyEditor == null)
		{
			throttleFramePropertyEditor = new ThrottleFramePropertyEditor();
		}
		return throttleFramePropertyEditor;
	}
	
	public int getNumberThrottles(){
		return throttleWindows.size();
	}

	private void requestFocusForNextFrame()
	{
		activeFrame = (activeFrame + 1) % throttleWindows.size();
		ThrottleWindow tf = throttleWindows.get(activeFrame);
		tf.requestFocus();
		tf.toFront();
	}

	private void requestFocusForPreviousFrame()
	{
		activeFrame--;
		if (activeFrame < 0)
		{
			activeFrame = throttleWindows.size() - 1;
		}
		ThrottleWindow tf = throttleWindows.get(activeFrame);
		tf.requestFocus();
		tf.toFront();
	}
	
	public ThrottleWindow getCurentThrottleFrame() {
		if (throttleWindows == null) return null;
		if (throttleWindows.size() == 0) return null;
		return throttleWindows.get(activeFrame);
	}
	
	public ThrottlesPreferences getThrottlesPreferences() {
		return throttlesPref; 
	}

	/**
	 *  Description of the Class
	 *
	 * @author     glen
	 */
	class ThrottleCyclingKeyListener extends KeyAdapter	{
		/**
		 *  Description of the Method
		 *
		 * @param  e  Description of the Parameter
		 */
		public void keyReleased(KeyEvent e)	{
			if (e.isShiftDown() && e.getKeyCode() == NEXT_THROTTLE_KEY)		
				requestFocusForNextFrame();			
			else if (e.isShiftDown() && e.getKeyCode() == PREV_THROTTLE_KEY)
				requestFocusForPreviousFrame();			
		}
	}
	
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ThrottleFrameManager.class.getName());
}


