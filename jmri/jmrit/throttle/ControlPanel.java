package jmri.jmrit.throttle;

import jmri.DccThrottle;
import jmri.util.SwingUtil;
import jmri.util.MouseInputAdapterInstaller;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.event.MouseInputAdapter;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JLayeredPane;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jdom.Element;

/**
 *  A JInternalFrame that contains a JSlider to control loco speed, and buttons
 *  for forward, reverse and STOP.
 *  <P>
 *  TODO: fix speed increments (14, 28)
 *
 * @author     glen   Copyright (C) 2002
 * @version    $Revision: 1.41 $
 */
public class ControlPanel extends JInternalFrame implements java.beans.PropertyChangeListener,ActionListener
{
	private DccThrottle throttle;

	private JSlider speedSlider;
	private JSpinner speedSpinner;
	private JRadioButton SpeedStep128Button;
	private JRadioButton SpeedStep28Button;
	private JRadioButton SpeedStep27Button;
	private JRadioButton SpeedStep14Button;
	private GridBagConstraints sliderConstraints;
	private JRadioButton forwardButton, reverseButton;
	private JButton stopButton;
	private JButton idleButton;
	private JPanel buttonPanel;
	private int speedIncrement;
        private boolean internalAdjust = false;

	private JPopupMenu propertiesPopup;
	private	JPanel spinnerPanel;
	private	JPanel sliderPanel;

	/* Constants for speed selection method */
	final public static int SLIDERDISPLAY = 0;
	final public static int STEPDISPLAY = 1;

	private int _displaySlider = SLIDERDISPLAY;

	public int accelerateKey = 107; // numpad +;
	public int decelerateKey = 109; // numpad -;
        public int accelerateKey1 = KeyEvent.VK_LEFT ; // Left Arrow
        public int decelerateKey1 = KeyEvent.VK_RIGHT ; // Left Arrow
        public int accelerateKey2 = KeyEvent.VK_PAGE_UP ; // Left Arrow
        public int decelerateKey2 = KeyEvent.VK_PAGE_DOWN ; // Left Arrow
	public int reverseKey = KeyEvent.VK_DOWN;
	public int forwardKey = KeyEvent.VK_UP;
	public int stopKey = 111; // numpad /
	public int idleKey = 106; // numpad *

        // LocoNet really only has 126 speed steps i.e. 0..127 - 1 for em stop
	private int MAX_SPEED = 126;

	// Save the speed step mode to aid in storage of the throttle.
	private int _speedStepMode = DccThrottle.SpeedStepMode128;
	/**
	 *  Constructor.
	 */
	public ControlPanel()
	{
		speedSlider = new JSlider(0, MAX_SPEED);
		speedSlider.setValue(0);
		SwingUtil.setFocusable(speedSlider,false);
		
		try {
		speedSpinner = new JSpinner();
                SpinnerNumberModel speedSpinnerModel=(SpinnerNumberModel)speedSpinner.getModel();
                speedSpinnerModel.setMaximum(new Integer(MAX_SPEED));
                speedSpinner.setModel(speedSpinnerModel);
                speedSpinner.setValue(new Integer(0));
		SwingUtil.setFocusable(speedSpinner,false);
		} catch (NoClassDefFoundError e1) {
			// we can't use a JSpinner Object.
			speedSpinner = null;
		} catch (Exception e2) {
			// we can't use a JSpinner Object.
			speedSpinner = null;
		}
		SpeedStep128Button = new JRadioButton("128 SS");
		SpeedStep28Button = new JRadioButton("28 SS");
		SpeedStep27Button = new JRadioButton("27 SS");
		SpeedStep14Button= new JRadioButton("14 SS");

		forwardButton = new JRadioButton("Forward");
		reverseButton = new JRadioButton("Reverse");

		propertiesPopup = new JPopupMenu();
		initGUI();
	}

	public void notifyThrottleDisposed()
	{
		this.setEnabled(false);
                this.throttle.removePropertyChangeListener(this);
		throttle = null;
	}

	public void destroy()
	{
		if (throttle != null)
		{
			throttle.setSpeedSetting(0);
		}
	}

	/**
	 *  Get notification that a throttle has been found as we requested.
	 *
	 * @param  t  An instantiation of the DccThrottle with the address requested.
	 */
	public void notifyThrottleFound(DccThrottle t)
	{
		this.throttle = t;
		this.setEnabled(true);
		this.setIsForward(throttle.getIsForward());
		this.setSpeedValues((int) throttle.getSpeedIncrement(),
				(int) throttle.getSpeedSetting());
                this.throttle.addPropertyChangeListener(this);
	}

	/**
	 *  Enable/Disable all buttons and slider.
	 *
	 * @param  isEnabled  True if the buttons/slider should be enabled, false
	 *      otherwise.
	 */
	public void setEnabled(boolean isEnabled)
	{
		//super.setEnabled(isEnabled);
		forwardButton.setEnabled(isEnabled);
		reverseButton.setEnabled(isEnabled);
		SpeedStep128Button.setEnabled(isEnabled);
		SpeedStep28Button.setEnabled(isEnabled);
		SpeedStep27Button.setEnabled(isEnabled);
		SpeedStep14Button.setEnabled(isEnabled);
		stopButton.setEnabled(isEnabled);
		idleButton.setEnabled(isEnabled);
		speedSlider.setEnabled(isEnabled);
		if(speedSpinner!=null)
			speedSpinner.setEnabled(isEnabled);
	}

	/**
	 *  Set the GUI to match that the loco is set to forward.
	 *
	 * @param  isForward  True if the loco is set to forward, false otherwise.
	 */
	public void setIsForward(boolean isForward)
	{
		forwardButton.setSelected(isForward);
		reverseButton.setSelected(!isForward);
	}

	/**
	 *  Set the GUI to match the speed steps of the current address.
	 *
	 * @param  steps Desired number of speed steps. One of 14,27,28,or 128.  Defaults to 128 
	 * step mode
	 */
	public void setSpeedSteps(int steps)
	{
		if(steps == DccThrottle.SpeedStepMode14) {
			SpeedStep14Button.setSelected(true);
			SpeedStep27Button.setSelected(false);
			SpeedStep28Button.setSelected(false);
			SpeedStep128Button.setSelected(false);
			MAX_SPEED=14;
		} else  if(steps == DccThrottle.SpeedStepMode27) {
			SpeedStep14Button.setSelected(false);
			SpeedStep27Button.setSelected(true);
			SpeedStep28Button.setSelected(false);
			SpeedStep128Button.setSelected(false);
			MAX_SPEED=27;
		} else  if(steps == DccThrottle.SpeedStepMode28) {
			SpeedStep14Button.setSelected(false);
			SpeedStep27Button.setSelected(false);
			SpeedStep28Button.setSelected(true);
			SpeedStep128Button.setSelected(false);
			MAX_SPEED=28;
		} else  {
			SpeedStep14Button.setSelected(false);
			SpeedStep27Button.setSelected(false);
			SpeedStep28Button.setSelected(false);
			SpeedStep128Button.setSelected(true);
			MAX_SPEED=126;
		}
		_speedStepMode=steps;
		if(speedSpinner!=null) {
             	   SpinnerNumberModel speedSpinnerModel=(SpinnerNumberModel)speedSpinner.getModel();
                   speedSpinnerModel.setMaximum(new Integer(MAX_SPEED));
                   speedSpinner.setModel(speedSpinnerModel);
		}
	}

	/**
	 *  Is this Speed Control selection method possible?
     *
     *  @param displaySlider integer value. possible values:
	 *	SLIDERDISPLAY  = use speed slider display
	 *      STEPDISPLAY = use speed step display
	 */
	public boolean isSpeedControllerAvailable(int displaySlider) {
		switch(displaySlider) {
		   case STEPDISPLAY: 
			return(speedSpinner!=null);
		   case SLIDERDISPLAY:
			return(speedSlider!=null);
		   default:
			return false;
		}
	}

	/**
	 *  Set the Speed Control selection method
     *
     *  @param displaySlider integer value. possible values:
	 *	SLIDERDISPLAY  = use speed slider display
	 *      STEPDISPLAY = use speed step display
	 */
	public void setSpeedController(int displaySlider) {
		switch(displaySlider) {
		   case STEPDISPLAY: {
			this.getLayeredPane().moveToFront(spinnerPanel);
			sliderPanel.setVisible(false);
			spinnerPanel.setVisible(true);
			break;
			}
		   default: {
			this.getLayeredPane().moveToFront(sliderPanel);
			sliderPanel.setVisible(true);
			spinnerPanel.setVisible(false);
		   }
		}
		_displaySlider=displaySlider;
	}

	/**
         *  Get the value indicating what speed input we're displaying
	 *  
         */
	public int getDisplaySlider() {
		return _displaySlider;
	}

	/**
	 *  Set the GUI to match that the loco speed.
	 *
	 * @param  speedIncrement  : TODO
	 * @param  speed           The speed value of the loco.
	 */
	public void setSpeedValues(int speedIncrement, int speed)
	{
		this.speedIncrement = speedIncrement;
		speedSlider.setValue(speed * speedIncrement);
		// Spinner Speed should be the raw integer speed value
		if(speedSpinner!=null)
		   speedSpinner.setValue(new Integer(speed));
	}

	/**
	 *  Create, initialize and place GUI components.
	 */
	private void initGUI()
	{
		//JPanel mainPanel = new JPanel();
		JLayeredPane mainPanel = new JLayeredPane();
		this.setContentPane(mainPanel);
		this.setLayeredPane(mainPanel);
		mainPanel.setLayout(new BorderLayout());
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		sliderPanel = new JPanel();
		sliderPanel.setLayout(new GridBagLayout());

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.gridheight = 1;
		constraints.gridwidth = 1;
		constraints.ipadx = 0;
		constraints.ipady = 0;
		Insets insets = new Insets(2, 2, 2, 2);
		constraints.insets = insets;
		constraints.weightx = 1;
		constraints.weighty = 1;
		constraints.gridx = 0;
		constraints.gridy = 0;

		sliderPanel.add(speedSlider, constraints);
		this.getContentPane().add(sliderPanel,BorderLayout.CENTER);
		speedSlider.setOrientation(JSlider.VERTICAL);
		speedSlider.setMajorTickSpacing(MAX_SPEED/2);
		com.sun.java.util.collections.Hashtable labelTable = new com.sun.java.util.collections.Hashtable();
		labelTable.put(new Integer(MAX_SPEED/2), new JLabel("50%"));
		labelTable.put(new Integer(MAX_SPEED), new JLabel("100%"));
                labelTable.put(new Integer(0), new JLabel("Stop"));
		speedSlider.setLabelTable(labelTable);
		speedSlider.setPaintTicks(true);
		speedSlider.setPaintLabels(true);
		// remove old actions
		speedSlider.addChangeListener(
			new ChangeListener()
			{
				public void stateChanged(ChangeEvent e)
				{
                                    if ( !internalAdjust) {
					if (!speedSlider.getValueIsAdjusting())
					{
                                                float newSpeed = (speedSlider.getValue() / ( MAX_SPEED * 1.0f ) ) ;
                                                log.debug( "stateChanged: slider pos: " + speedSlider.getValue() + " speed: " + newSpeed );
						throttle.setSpeedSetting( newSpeed );
						if(speedSpinner!=null)
						   speedSpinner.setValue(new Integer(speedSlider.getValue()));
					}
				   } else {
					internalAdjust=false;
				   }
				}
			});

		spinnerPanel = new JPanel();
		spinnerPanel.setLayout(new GridBagLayout());

		if(speedSpinner!=null)
			spinnerPanel.add(speedSpinner, constraints);
		this.getContentPane().add(spinnerPanel,BorderLayout.CENTER);

		// remove old actions
		if(speedSpinner!=null)
		   speedSpinner.addChangeListener(
			new ChangeListener()
			{
				public void stateChanged(ChangeEvent e)
				{
                                    if ( !internalAdjust) {
					//if (!speedSpinner.getValueIsAdjusting())
					//{
                                                float newSpeed = (((Integer)speedSpinner.getValue()).floatValue() / ( MAX_SPEED * 1.0f ) ) ;
                                                log.debug( "stateChanged: spinner pos: " + speedSpinner.getValue() + " speed: " + newSpeed );
						throttle.setSpeedSetting( newSpeed );
						speedSlider.setValue(((Integer)speedSpinner.getValue()).intValue());
					//}
				   } else {
					internalAdjust=false;
				   }
				}
			});

		ButtonGroup speedStepButtons = new ButtonGroup();
		speedStepButtons.add(SpeedStep128Button);
		speedStepButtons.add(SpeedStep28Button);
		speedStepButtons.add(SpeedStep27Button);
		speedStepButtons.add(SpeedStep14Button);
		constraints.fill = GridBagConstraints.NONE;
		constraints.gridy = 1;
		spinnerPanel.add(SpeedStep128Button, constraints);
		constraints.gridy = 2;
		spinnerPanel.add(SpeedStep28Button, constraints);
		constraints.gridy = 3;
		spinnerPanel.add(SpeedStep27Button, constraints);
		constraints.gridy = 4;
		spinnerPanel.add(SpeedStep14Button, constraints);

		SpeedStep14Button.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					setSpeedSteps(DccThrottle.SpeedStepMode14);
					throttle.setSpeedStepMode(DccThrottle.SpeedStepMode14);
				}
			});

		SpeedStep27Button.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					setSpeedSteps(DccThrottle.SpeedStepMode27);
					throttle.setSpeedStepMode(DccThrottle.SpeedStepMode27);
				}
			});

		SpeedStep28Button.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					setSpeedSteps(DccThrottle.SpeedStepMode28);
					throttle.setSpeedStepMode(DccThrottle.SpeedStepMode28);
				}
			});

		SpeedStep128Button.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					setSpeedSteps(DccThrottle.SpeedStepMode128);
					throttle.setSpeedStepMode(DccThrottle.SpeedStepMode128);
				}
			});
		
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridBagLayout());
		this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		ButtonGroup directionButtons = new ButtonGroup();
		directionButtons.add(forwardButton);
		directionButtons.add(reverseButton);
		constraints.fill = GridBagConstraints.NONE;
		constraints.gridy = 1;
		buttonPanel.add(forwardButton, constraints);
		constraints.gridy = 2;
		buttonPanel.add(reverseButton, constraints);

		forwardButton.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					throttle.setIsForward(true);
				}
			});

		reverseButton.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					throttle.setIsForward(false);
				}
			});

		stopButton = new JButton("STOP!");
		constraints.gridy = 3;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		buttonPanel.add(stopButton, constraints);
		stopButton.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					stop();
				}
			});

		stopButton.addMouseListener(
			new MouseListener()
			{
				public void mousePressed(MouseEvent e)
				{
					stop();
				}
                                public void mouseExited(MouseEvent e) {}
                                public void mouseEntered(MouseEvent e) {}
                                public void mouseReleased(MouseEvent e) {}
                                public void mouseClicked(MouseEvent e) {}
			});

		idleButton = new JButton("Idle");
		constraints.gridy = 4;
		buttonPanel.add(idleButton, constraints);
		idleButton.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					speedSlider.setValue(0);
					if(speedSpinner!=null)
					   speedSpinner.setValue(new Integer(0));
					throttle.setSpeedSetting(0);
				}
			});

		this.addComponentListener(
			new ComponentAdapter()
			{
				public void componentResized(ComponentEvent e)
				{
					changeOrientation();
				}
			});

		JMenuItem propertiesItem = new JMenuItem("Properties");
		propertiesItem.addActionListener(this);
		propertiesPopup.add(propertiesItem);

                // Add a mouse listener all components to trigger the popup menu.
                MouseInputAdapter popupListener = new PopupListener(propertiesPopup,this);

		MouseInputAdapterInstaller.installMouseInputAdapterOnAllComponents(
					popupListener,this);

		// Install the Key bindings on all Components
		KeyListenerInstaller.installKeyListenerOnAllComponents(
				new ControlPadKeyListener(), this);

		// set by default which speed selection method is on top
		this.getLayeredPane().moveToFront(spinnerPanel);
		this.getLayeredPane().moveToFront(sliderPanel);
		setSpeedController(_displaySlider);
	}

	/**
	 *  Perform an emergency stop
	 */
	private void stop()
	{
                if(this.throttle==null) return;
		speedSlider.setValue(0);
		if(speedSpinner!=null)
		   speedSpinner.setValue(new Integer(0));
		throttle.setSpeedSetting(-1);
	}

	/**
	 *  The user has resized the Frame. Possibly change from Horizontal to Vertical
	 *  layout.
	 */
	private void changeOrientation()
	{
		if (this.getWidth() > this.getHeight())
		{
			speedSlider.setOrientation(JSlider.HORIZONTAL);
			this.remove(buttonPanel);
			this.getContentPane().add(buttonPanel, BorderLayout.EAST);
		}
		else
		{
			speedSlider.setOrientation(JSlider.VERTICAL);
			this.remove(buttonPanel);
			this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		}
	}


	/**
	 *  A KeyAdapter that listens for the keys that work the control pad buttons
	 *
	 * @author     glen
         * @version    $Revision: 1.41 $
	 */
	class ControlPadKeyListener extends KeyAdapter
	{
		/**
		 *  Description of the Method
		 *
		 * @param  e  Description of the Parameter
		 */
		public void keyPressed(KeyEvent e)
		{
			if ( (e.getKeyCode() == accelerateKey) | (e.getKeyCode() == accelerateKey1) )
			{
				if (speedSlider.isEnabled())
				{
					if (speedSlider.getValue() != speedSlider.getMaximum())
					{
						speedSlider.setValue(speedSlider.getValue() + 1);
					}
				}
				if(speedSpinner!=null && speedSpinner.isEnabled())
				{
					if (speedSpinner.getValue() != ((SpinnerNumberModel)speedSpinner.getModel()).getMaximum())
					{
						speedSpinner.setValue(new Integer(((Integer)speedSpinner.getValue()).intValue() + 1));
					}
				}
			}
                        if ( e.getKeyCode() == accelerateKey2 )
                        {
                                if (speedSlider.isEnabled())
                                {
                                        if (speedSlider.getValue() != speedSlider.getMaximum())
                                        {
                                                speedSlider.setValue(speedSlider.getValue() + 10);
                                        }
                                }
                                if (speedSpinner!=null && speedSpinner.isEnabled())
                                {
                                        if (speedSpinner.getValue() != ((SpinnerNumberModel)speedSpinner.getModel()).getMaximum())
                                        {
                                                speedSpinner.setValue(new Integer(((Integer)speedSpinner.getValue()).intValue() + 10));
                                        }
                                }
                        }
			else if ( (e.getKeyCode() == decelerateKey) | (e.getKeyCode() == decelerateKey1) )
			{
				if (speedSlider.isEnabled())
				{
					if (speedSlider.getValue() != speedSlider.getMinimum())
					{
						speedSlider.setValue(speedSlider.getValue() - 1);
					}
				}
				if (speedSpinner!=null && speedSpinner.isEnabled())
				{
					if (speedSpinner.getValue() != ((SpinnerNumberModel)speedSpinner.getModel()).getMinimum())
					{
						speedSpinner.setValue(new Integer(((Integer)speedSpinner.getValue()).intValue() - 1));
					}
				}
			}
                        else if ( e.getKeyCode() == decelerateKey2 )
                        {
                                if (speedSlider.isEnabled())
                                {
                                        if (speedSlider.getValue() != speedSlider.getMinimum())
                                        {
                                                speedSlider.setValue(speedSlider.getValue() - 10);
                                        }
                                }
                                if (speedSpinner!=null && speedSpinner.isEnabled())
                                {
                                        if (speedSpinner.getValue() != ((SpinnerNumberModel)speedSpinner.getModel()).getMinimum())
                                        {
                                                speedSpinner.setValue(new Integer(((Integer)speedSpinner.getValue()).intValue() - 10));
                                        }
                                }
                        }
			else if (e.getKeyCode() == forwardKey)
			{
				if (forwardButton.isEnabled())
				{
					forwardButton.doClick();
				}
			}
			else if (e.getKeyCode() == reverseKey)
			{
				if (reverseButton.isEnabled())
				{
					reverseButton.doClick();
				}
			}
			else if (e.getKeyCode() == stopKey)
			{
				if (speedSlider.isEnabled() || 
				   (speedSpinner!=null && speedSpinner.isEnabled()) )
				{
					stop();
				}
			}
			else if (e.getKeyCode() == idleKey)
			{
				if (speedSlider.isEnabled() || 
				   (speedSpinner!=null && speedSpinner.isEnabled()))
				{
					speedSlider.setValue(0);
				}
			}
		}
	}


	// update the state of this panel if any of the properties change
	public void propertyChange(java.beans.PropertyChangeEvent e) {
		if (e.getPropertyName().equals("SpeedSetting")) {
	           internalAdjust=true;
		   float speed=((Float) e.getNewValue()).floatValue();
                    // multiply by MAX_SPEED, and round to find the new
                    //slider setting.
                   int newSliderSetting = java.lang.Math.round(speed * MAX_SPEED) ;
                   log.debug( "propertyChange: new speed float: " + speed + " slider pos: " + newSliderSetting ) ;
		   speedSlider.setValue( newSliderSetting );
		   if(speedSpinner!=null)
		      speedSpinner.setValue(new Integer(newSliderSetting));
		} else if (e.getPropertyName().equals("SpeedSteps")) {
		   int steps=((Integer) e.getNewValue()).intValue();
	           setSpeedSteps(steps);
		} else if (e.getPropertyName().equals("IsForward")) {
		   boolean Forward=((Boolean) e.getNewValue()).booleanValue();
	           setIsForward(Forward);
		}
	}

       /**
        * Handle the selection from the popup menu.
        * @param e The ActionEvent causing the action.
        */
       public void actionPerformed(ActionEvent e)
       {
		ControlPanelPropertyEditor editor =
			new ControlPanelPropertyEditor(this);
		editor.setVisible(true);
       }

    /**
     * A PopupListener to handle mouse clicks and releases. Handles
     * the popup menu.
     */
    class PopupListener extends MouseInputAdapter
    {

	JPopupMenu _menu;
	JInternalFrame parentFrame;

	PopupListener(JPopupMenu menu,JInternalFrame parent){
		parentFrame = parent;
		_menu=menu;
	}

        /**
         * If the event is the popup trigger, which is dependent on
         * the platform, present the popup menu. Otherwise change
         * the state of the function depending on the locking state
         * of the button.
         * @param e The MouseEvent causing the action.
         */
        public void mousePressed(MouseEvent e)
        {
            if (log.isDebugEnabled()) log.debug("pressed "+(e.getModifiers() & e.BUTTON1_MASK)+" "+e.isPopupTrigger()
                    +" "+(e.getModifiers() & (e.ALT_MASK+ e.META_MASK+e.CTRL_MASK))
                    +(" "+e.ALT_MASK+"/"+e.META_MASK+"/"+e.CTRL_MASK));
            if (e.isPopupTrigger() && parentFrame.isSelected())
            {
		try {
                   _menu.show(e.getComponent(),  
                           e.getX(), e.getY());
		} catch ( java.awt.IllegalComponentStateException cs ) {
			// Message sent to a hidden component, so we need 
		}
		e.consume();
            }
        }

        /**
         * If the event is the popup trigger, which is dependent on
         * the platform, present the popup menu. Otherwise change
         * the state of the function depending on the locking state
         * of the button.
         * @param e The MouseEvent causing the action.
         */
        public void mouseReleased(MouseEvent e)
        {
            if (log.isDebugEnabled()) log.debug("released "+(e.getModifiers()   & e.BUTTON1_MASK)+" "+e.isPopupTrigger()
                    +" "+(e.getModifiers() & (e.ALT_MASK+e.META_MASK+e.CTRL_MASK)));
            if (e.isPopupTrigger())
            {
		try {
                   _menu.show(e.getComponent(),   
                           e.getX(), e.getY());
		} catch ( java.awt.IllegalComponentStateException cs ) {
			// Message sent to a hidden component, so we need 
		}

		e.consume();
            }
        }
    }

	/**
	 *  Collect the prefs of this object into XML Element
	 *  <ul>
	 *    <li> Window prefs
	 *  </ul>
	 *
	 *
	 * @return    the XML of this object.
	 */
	public Element getXml()
	{
		Element me = new Element("ControlPanel");
		me.addAttribute("displaySpeedSlider",String.valueOf(this._displaySlider));		
		me.addAttribute("speedMode",String.valueOf(this._speedStepMode));
		Element window = new Element("window");
		WindowPreferences wp = new WindowPreferences();
		com.sun.java.util.collections.ArrayList children =
				new com.sun.java.util.collections.ArrayList(1);
		children.add(wp.getPreferences(this));
		me.setChildren(children);
		return me;
	}

	/**
	 *  Set the preferences based on the XML Element.
	 *  <ul>
	 *    <li> Window prefs
	 *  </ul>
	 *
	 *
	 * @param  e  The Element for this object.
	 */
	public void setXml(Element e)
	{	
		internalAdjust=true;
		try {			
			this.setSpeedController(e.getAttribute("displaySpeedSlider").getIntValue());
		} catch (org.jdom.DataConversionException ex)
		{
			log.error("DataConverstionException in setXml: "+ex);
		} catch (Exception em)
		{
			// in this case, recover by displaying the speed slider.
			this.setSpeedController(SLIDERDISPLAY);
        	}
		try {			
			setSpeedSteps(e.getAttribute("speedMode").getIntValue());
			if(throttle!=null)
			   throttle.setSpeedStepMode(e.getAttribute("speedMode").getIntValue());
		} catch (org.jdom.DataConversionException ex)
		{
			log.error("DataConverstionException in setXml: "+ex);
		} catch (Exception em)
		{
			// in this case, recover by defaulting to 128 speed step mode.
			setSpeedSteps(DccThrottle.SpeedStepMode128);
			if(throttle!=null)
			   throttle.setSpeedStepMode(DccThrottle.SpeedStepMode128);
        	}
		Element window = e.getChild("window");
		WindowPreferences wp = new WindowPreferences();
		wp.setPreferences(this, window);
	}

        // initialize logging
        static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ControlPanel.class.getName());
}
