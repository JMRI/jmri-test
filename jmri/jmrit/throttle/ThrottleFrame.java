package jmri.jmrit.throttle;

import java.awt.*;
import java.awt.event.*;
import java.io.PrintWriter;
import javax.swing.*;
import javax.swing.event.*;

import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.ThrottleListener;
import jmri.ThrottleManager;

import org.jdom.Element;

/**
 *  A JFrame to contain throttle elements such as speed control, address
 *  chooser, function panel, and maybe others. <p>
 *
 *  This class requests a DccThrottle and calls methods in that object as
 *  directed by the interface.
 *
 * @author     Glen Oberhauser
 * @created    March 25, 2003
 * @version
 */
public class ThrottleFrame extends JFrame implements AddressListener, ThrottleListener
{
	private final Integer PANEL_LAYER = new Integer(1);
	private static int NEXT_FRAME_KEY = KeyEvent.VK_RIGHT;
	private static int PREV_FRAME_KEY = KeyEvent.VK_LEFT;

	private static int ADDRESS_PANEL_INDEX = 0;
	private static int CONTROL_PANEL_INDEX = 1;
	private static int FUNCTION_PANEL_INDEX = 2;
	private static int NUM_FRAMES = 3;

	private JInternalFrame[] frameList;
	private int activeFrame;

	private ControlPanel controlPanel;
	private FunctionPanel functionPanel;
	private AddressPanel addressPanel;

	private JCheckBoxMenuItem viewControlPanel;
	private JCheckBoxMenuItem viewFunctionPanel;
	private JCheckBoxMenuItem viewAddressPanel;
	
	private DccThrottle throttle;

	/**
	 *  Default constructor
	 */
	public ThrottleFrame()
	{
		initGUI();
	}

    /**
     * Get notification that a throttle has been found as you requested.
     * @param t An instantiation of the DccThrottle with the address requested.
     */
    public void notifyThrottleFound(DccThrottle t)
	{
		this.throttle = t;
		addressPanel.notifyThrottleFound(t);
		controlPanel.notifyThrottleFound(t);
		functionPanel.notifyThrottleFound(t);
	}

    /**
     * Receive notification that a new address has been selected.
     * @param newAddress The address that is now selected.
     */
    public void notifyAddressChosen(int address)
	{
		boolean throttleInUse = 
			InstanceManager.throttleManagerInstance().requestThrottle(address, this);
		if (throttleInUse)
		{
			JOptionPane.showMessageDialog(this, "Address in use by another throttle.");
		}
	}
	
	/**
	 * Receive notification that an address has been released or dispatched
	 * @param address The address released/dispatched
	 */
	public void notifyAddressReleased(int address)
	{
		InstanceManager.throttleManagerInstance().cancelThrottleRequest(address, this);
		controlPanel.notifyThrottleDisposed();
		functionPanel.notifyThrottleDisposed();
		addressPanel.notifyThrottleDisposed();
		throttle = null;
	}
	
	/**
	 *  Place and initialize the GUI elements.
	 *  <ul>
	 *    <li> ControlPanel
	 *    <li> FunctionPanel
	 *    <li> AddressPanel
	 *    <li> JMenu
	 *  </ul>
	 *
	 */
	private void initGUI()
	{
		setTitle("Throttle");
		JDesktopPane desktop = new JDesktopPane();
		this.setContentPane(desktop);
		this.addWindowListener(
			new WindowAdapter()
			{
				public void windowClosing(WindowEvent e)
				{
					ThrottleFrame me = (ThrottleFrame)e.getSource();
					InstanceManager.throttleFrameManagerInstance().requestThrottleFrameDestruction(me);
				}
			});

		initializeMenu();

		FrameListener frameListener = new FrameListener();

		controlPanel = new ControlPanel();
		controlPanel.setResizable(true);
		controlPanel.setClosable(true);
		controlPanel.setIconifiable(true);
		controlPanel.setTitle("Control Panel");
		controlPanel.setSize(100, 330);
		controlPanel.setVisible(true);
		controlPanel.setEnabled(false);
		controlPanel.addInternalFrameListener(frameListener);

		functionPanel = new FunctionPanel();
		functionPanel.setResizable(true);
		functionPanel.setClosable(true);
		functionPanel.setIconifiable(true);
		functionPanel.setTitle("Function Panel");
		functionPanel.setSize(200, 204);
		functionPanel.setLocation(100, 0);
		functionPanel.setVisible(true);
		functionPanel.setEnabled(false);
		functionPanel.addInternalFrameListener(frameListener);

		addressPanel = new AddressPanel();
		addressPanel.setResizable(true);
		addressPanel.setClosable(true);
		addressPanel.setIconifiable(true);
		addressPanel.setTitle("Address Panel");
		addressPanel.setSize(200, 126);
		addressPanel.setLocation(100, 204);
		addressPanel.setVisible(true);
		addressPanel.addInternalFrameListener(frameListener);
		addressPanel.addAddressListener(this);

		desktop.add(controlPanel, PANEL_LAYER);
		desktop.add(functionPanel, PANEL_LAYER);
		desktop.add(addressPanel, PANEL_LAYER);

		frameList = new JInternalFrame[3];
		frameList[ADDRESS_PANEL_INDEX] = addressPanel;
		frameList[CONTROL_PANEL_INDEX] = controlPanel;
		frameList[FUNCTION_PANEL_INDEX] = functionPanel;
		activeFrame = ADDRESS_PANEL_INDEX;

		desktop.setPreferredSize(new Dimension(300, 340));

		KeyListenerInstaller.installKeyListenerOnAllComponents(
						new FrameCyclingKeyListener(), this);

		try
		{
			addressPanel.setSelected(true);

		}
		catch (java.beans.PropertyVetoException ex)
		{
			log.error("Error selecting InternalFrame:" + ex);
		}

	}


	/**
	 *  Description of the Method
	 */
	private void initializeMenu()
	{
		JMenu viewMenu = new JMenu("View");
		viewAddressPanel = new JCheckBoxMenuItem("Address Panel");
		viewAddressPanel.setSelected(true);
		viewAddressPanel.addItemListener(
			new ItemListener()
			{
				public void itemStateChanged(ItemEvent e)
				{
					addressPanel.setVisible(e.getStateChange() == e.SELECTED);
				}
			});

		viewControlPanel = new JCheckBoxMenuItem("Control Panel");
		viewControlPanel.setSelected(true);
		viewControlPanel.addItemListener(
			new ItemListener()
			{
				public void itemStateChanged(ItemEvent e)
				{
					controlPanel.setVisible(e.getStateChange() == e.SELECTED);
				}
			});
		viewFunctionPanel = new JCheckBoxMenuItem("Function Panel");
		viewFunctionPanel.setSelected(true);
		viewFunctionPanel.addItemListener(
			new ItemListener()
			{
				public void itemStateChanged(ItemEvent e)
				{
					functionPanel.setVisible(e.getStateChange() == e.SELECTED);
				}
			});

		viewMenu.add(viewAddressPanel);
		viewMenu.add(viewControlPanel);
		viewMenu.add(viewFunctionPanel);

		JMenu editMenu = new JMenu("Edit");
		JMenuItem preferencesItem = new JMenuItem("Frame Properties");
		editMenu.add(preferencesItem);
		preferencesItem.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					editPreferences();
				}
			});
		this.setJMenuBar(new JMenuBar());
		this.getJMenuBar().add(viewMenu);
		this.getJMenuBar().add(editMenu);
	}

	private void editPreferences()
	{
		ThrottleFramePropertyEditor editor = 
			InstanceManager.throttleFrameManagerInstance().getThrottleFrameEditor();
		editor.setThrottleFrame(this);
		//editor.setLocation(this.getLocationOnScreen());
		editor.setLocationRelativeTo(this);
		editor.setVisible(true);
	}
	
	/**
	 *  Handle my own destruction.
	 *  <ol>
	 *    <li> dispose of sub windows.
	 *    <li> notify my manager of my demise.
	 *  </ol>
	 *
	 */
	public void dispose()
	{
		// check for any special disposing in InternalFrames
		controlPanel.dispose();
		functionPanel.dispose();
		// dispose of this last because it will release and destroy throttle.
		addressPanel.dispose();
		
		// Handle disposing of the throttle
		if (throttle != null)
		{
			InstanceManager.throttleManagerInstance().
				cancelThrottleRequest(throttle.getDccAddress(), this);
		}
		
		super.dispose();
	}


	/**
	 *  A KeyAdapter that listens for the key that cycles through
	 * the JInternalFrames.
	 *
	 * @author     glen
	 * @created    March 30, 2003
	 */
	class FrameCyclingKeyListener extends KeyAdapter
	{
		/**
		 *  Description of the Method
		 *
		 * @param  e  Description of the Parameter
		 */
		public void keyPressed(KeyEvent e)
		{
			if (e.isControlDown() && e.getKeyCode() == NEXT_FRAME_KEY)
			{
				try
				{
					activeFrame = (activeFrame + 1) % NUM_FRAMES;
					frameList[activeFrame].setSelected(true);
				}
				catch (java.beans.PropertyVetoException ex)
				{
					log.warn("Exception selecting internal frame:" + ex);
				}

			}
			else if (e.isControlDown() && e.getKeyCode() == PREV_FRAME_KEY)
			{
				try
				{
					activeFrame--;
					if (activeFrame < 0)
					{
						activeFrame = NUM_FRAMES - 1;
					}
					frameList[activeFrame].setSelected(true);
				}
				catch (java.beans.PropertyVetoException ex)
				{
					log.warn("Exception selecting internal frame:" + ex);
				}
			}
		}
	}


	/**
	 *  An extension of InternalFrameAdapter for listening to the closing of of
	 *  this frame's internal frames.
	 *
	 * @author     glen
	 * @created    March 25, 2003
	 */
	class FrameListener extends InternalFrameAdapter
	{
		/**
		 *  Listen for the closing of an internal frame and set the "View" menu
		 *  appropriately. Then hide the closing frame
		 *
		 * @param  e  The InternalFrameEvent leading to this action
		 */
		public void internalFrameClosing(InternalFrameEvent e)
		{
			if (e.getSource() == controlPanel)
			{
				viewControlPanel.setSelected(false);
				controlPanel.setVisible(false);
			}
			else if (e.getSource() == addressPanel)
			{
				viewAddressPanel.setSelected(false);
				addressPanel.setVisible(false);
			}
			else if (e.getSource() == functionPanel)
			{
				viewFunctionPanel.setSelected(false);
				functionPanel.setVisible(false);
			}
		}

		/**
		 *  Listen for the activation of an internal frame record this property for
		 *  coorect processing of the frame cycling key.
		 *
		 * @param  e  The InternalFrameEvent leading to this action
		 */
		public void internalFrameActivated(InternalFrameEvent e)
		{
			if (e.getSource() == controlPanel)
			{
				activeFrame = CONTROL_PANEL_INDEX;
			}
			else if (e.getSource() == addressPanel)
			{
				activeFrame = ADDRESS_PANEL_INDEX;
			}
			else if (e.getSource() == functionPanel)
			{
				activeFrame = FUNCTION_PANEL_INDEX;
			}
		}

	}


	/**
	 *  Collect the prefs of this object into XML Element
	 *  <ul>
	 *    <li> Window prefs
	 *    <li> ControlPanel
	 *    <li> FunctionPanel
	 *    <li> AddressPanel
	 *  </ul>
	 *
	 *
	 * @return    the XML of this object.
	 */
	public Element getXml()
	{
		Element me = new Element("ThrottleFrame");
		me.addAttribute("title", this.getTitle());
		com.sun.java.util.collections.ArrayList children =
				new com.sun.java.util.collections.ArrayList(1);
		WindowPreferences wp = new WindowPreferences();
		
		children.add(wp.getPreferences(this));
		children.add(controlPanel.getXml());
		children.add(functionPanel.getXml());
		children.add(addressPanel.getXml());
		me.setChildren(children);
		return me;
	}

	/**
	 *  Set the preferences based on the XML Element.
	 *  <ul>
	 *    <li> Window prefs
	 *	  <li> Frame title
	 *    <li> ControlPanel
	 *    <li> FunctionPanel
	 *    <li> AddressPanel
	 *  </ul>
	 *
	 *
	 * @param  e  The Element for this object.
	 */
	public void setXml(Element e)
	{
		this.setTitle(e.getAttribute("title").getValue());
		Element window = e.getChild("window");
		WindowPreferences wp = new WindowPreferences();
		wp.setPreferences(this, window);
		Element controlPanelElement = e.getChild("ControlPanel");
		controlPanel.setXml(controlPanelElement);
		Element functionPanelElement = e.getChild("FunctionPanel");
		functionPanel.setXml(functionPanelElement);
		Element addressPanelElement = e.getChild("AddressPanel");
		addressPanel.setXml(addressPanelElement);
	}

	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ThrottleFrame.class.getName());

}

