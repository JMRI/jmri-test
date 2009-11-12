package jmri.jmrit.logix;

import jmri.DccThrottle;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.PowerManager;
import jmri.ThrottleListener;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.powerpanel.PowerPane;
import jmri.util.JmriJFrame;
import jmri.jmrit.DccLocoAddressSelector;

//import org.jdom.Element;

import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.throttle.FunctionButton;
import jmri.jmrit.throttle.KeyListenerInstaller;
import jmri.jmrit.throttle.ThrottleFramePropertyEditor;
import jmri.jmrit.throttle.ThrottleFrameManager;

/**
 *  A JFrame to contain throttle elements such as speed control,
 *  function panel.  It keeps a record of the throttle commands
 *  for plyback leter. <p>
 *
 *  Modeled on package jmri.jmrit.throttle by
 * @author     Glen Oberhauser
 * @author     Bob Jacobsen    Copyright 2008
 
 * @author     Pete Cressman   Copyright 2009
 * @version    $Revision: 1.1 $
 */

public class LearnThrottleFrame extends JmriJFrame implements java.beans.PropertyChangeListener
{
    ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.throttle.ThrottleBundle");
    static int STRUT_SIZE = 10;
    
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
    
    
    private ControlPanel _controlPanel;
    private FunctionPanel _functionPanel;
    private ButtonFrame _buttonPanel;
    private WarrantFrame _warrantFrame;
        
    private DccThrottle _throttle;
    
    PowerPane powerControl  = new PowerPane();
    PowerManager powerMgr = null;
    JButton powerLight;
    // Load the power lights as icons to be placed in an invisible JButton so the light 
    // can be clicked to change the power status
    NamedIcon powerOnIcon = new NamedIcon("resources/GreenPowerLED.gif", "resources/GreenPowerLED.gif");
    NamedIcon powerOffIcon = new NamedIcon("resources/RedPowerLED.gif", "resources/RedPowerLED.gif");
    NamedIcon powerXIcon = new NamedIcon("resources/YellowPowerLED.gif", "resources/YellowPowerLED.gif");
    NamedIcon directionOnIcon = new NamedIcon("resources/icons/USS/sensor/amber-on.gif", "amber-on");
    NamedIcon directionOffIcon = new NamedIcon("resources/icons/USS/sensor/amber-off.gif", "amber-off");
    NamedIcon stopIcon = new NamedIcon("resources/icons/USS/sensor/red-on.gif", "red-on");
    
    /**
     *  Default constructor
     */
    public LearnThrottleFrame(WarrantFrame warrantFrame)
    {
        super();
        _warrantFrame = warrantFrame;
        powerMgr = InstanceManager.powerManagerInstance();
        if (powerMgr == null) {
            log.info("No power manager instance found, panel not active");
        }
        else powerMgr.addPropertyChangeListener(this);
        initGUI();
        setVisible(true);
    }

    /**
     * Get notification that a throttle has been found as you requested.
     * @param t An instantiation of the DccThrottle with the address requested.
     */
    public void notifyThrottleFound(DccThrottle t)
    {
        if(log.isDebugEnabled()) {
           log.debug("notifyThrottleFound address= " +t.getLocoAddress().toString());
        }
        _throttle = t;
        _controlPanel.notifyThrottleFound(t);
        _functionPanel.notifyThrottleFound(t);
        _buttonPanel.notifyThrottleFound(t);
        setSpeedSetting(0.0f);      // be sure loco is stopped.
        RosterEntry train = _warrantFrame.getTrain();
        String name = "";
        if (train != null) {
            name = train.getId();
        }
        setTitle(name+" ("+_throttle.getLocoAddress().toString()+")");
    }
        
    public void notifyThrottleLost(jmri.LocoAddress dccAddress) {
        JOptionPane.showMessageDialog(this, java.text.MessageFormat.format(
                rb.getString("ThrottleLost"), dccAddress,toString()),
                rb.getString("WarningTitle"), JOptionPane.WARNING_MESSAGE);
        _warrantFrame.StopRunTrain();
    }

    private void initGUI()
    {
        setTitle("Throttle");
        this.addWindowListener (new WindowAdapter() {
                                   public void windowClosing(WindowEvent e) {
                                       _warrantFrame.StopRunTrain();
                                       dispose();
                                   }
                               });     
        initializeMenu();
        _functionPanel = new FunctionPanel(_warrantFrame.getTrain(), this);
        // assumes button width of 54, height of 30 (set in class FunctionButton) with
        // horiz and vert gaps of 5 each (set in FunctionPanel class)
        // with 3 buttons across and 6 rows high
        int width = 3*(FunctionButton.BUT_WDTH) + 2*3*5; 		// = 192
        int height = 6*(FunctionButton.BUT_HGHT) + 2*6*5 +10;	// = 240 (another 10 needed?)
        _functionPanel.setSize(width, height);
        _functionPanel.setVisible(true);
        _functionPanel.setEnabled(false);
        //functionPanel.addInternalFrameListener(InternalFrameAdapter);
        
        _controlPanel = new ControlPanel(this);
        _controlPanel.setVisible(true);
        _controlPanel.setEnabled(false);
        _controlPanel.setSize(_controlPanel.getPreferredSize().width, height);        

        _buttonPanel = new ButtonFrame();
        _buttonPanel.setVisible(true);
        _buttonPanel.setEnabled(false);
        _buttonPanel.setSize(_controlPanel.getWidth()+_functionPanel.getWidth(), _buttonPanel.getPreferredSize().height);        

        _buttonPanel.setLocation(0, 0);
        _controlPanel.setLocation(0, _buttonPanel.getHeight());
        _functionPanel.setLocation(_controlPanel.getWidth(), _buttonPanel.getHeight());

        getContentPane().add(_buttonPanel);
        JDesktopPane desktop = new JDesktopPane();
        getContentPane().add(desktop);
        desktop.add(_controlPanel);
        desktop.add(_functionPanel);
        
        desktop.setPreferredSize(new Dimension(
                    Math.max(_controlPanel.getWidth()+_functionPanel.getWidth(),_buttonPanel.getWidth()),
                    Math.max(_functionPanel.getHeight(),_controlPanel.getHeight())+_buttonPanel.getHeight()));
              
        // Install the Key bindings on all Components
        KeyListenerInstaller.installKeyListenerOnAllComponents(new ControlPadKeyListener(), this);
        setResizable(false);
        pack();
    }
    
    
    /**
     *  Set up View, Edit and Power Menus
     */
    private void initializeMenu() {
		JMenu editControl = new JMenu("Speed Control... ");
        ButtonGroup buttonGroup = new ButtonGroup();
        JRadioButtonMenuItem displaySlider = new JRadioButtonMenuItem(rb.getString("ButtonDisplaySpeedSlider"));
        displaySlider.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    _controlPanel.setSpeedController(true); 
                }
            });
        displaySlider.setSelected(true);
        buttonGroup.add(displaySlider);
        editControl.add(displaySlider);
        JRadioButtonMenuItem displaySteps = new JRadioButtonMenuItem(rb.getString("ButtonDisplaySpeedSteps"));
        displaySteps.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    _controlPanel.setSpeedController(false); 
                }
            });
        buttonGroup.add(displaySteps);
        editControl.add(displaySteps);

        JCheckBoxMenuItem trackBox = new JCheckBoxMenuItem(rb.getString("CheckBoxTrackSliderInRealTime"));
        trackBox.addActionListener(new ActionListener() {
                JCheckBoxMenuItem trackBox;
                public void actionPerformed(ActionEvent e) {
                    _controlPanel.setTrackSlider(trackBox.isSelected());
                }
                ActionListener init(JCheckBoxMenuItem tb) {
                    trackBox = tb;
                    return this;
                }
            }.init(trackBox));
        editControl.add(trackBox);

		JMenuItem resetFuncButtonsItem = new JMenuItem("Reset Function Buttons");
		resetFuncButtonsItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_functionPanel.resetFuncButtons();
			}
		});

		JMenu editMenu = new JMenu("Edit");
		editMenu.add(editControl);
		editMenu.add(resetFuncButtonsItem);
		this.setJMenuBar(new JMenuBar());
		this.getJMenuBar().add(editMenu);

		if (powerMgr != null) {
			JMenu powerMenu = new JMenu("  Power");
			JMenuItem powerOn = new JMenuItem("Power On");
			powerMenu.add(powerOn);
			powerOn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					powerControl.onButtonPushed();
				}
			});

			JMenuItem powerOff = new JMenuItem("Power Off");
			powerMenu.add(powerOff);
			powerOff.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					powerControl.offButtonPushed();
				}
			});

			this.getJMenuBar().add(powerMenu);
			powerLight = new JButton();
			setPowerIcons();
			// make the button itself invisible, just display the power LED
			powerLight.setBorderPainted(false);
			powerLight.setContentAreaFilled(false);
			powerLight.setFocusPainted(false);
			this.getJMenuBar().add(powerLight);
			powerLight.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						if (powerMgr.getPower() == PowerManager.ON)
							powerControl.offButtonPushed();
						else if (powerMgr.getPower() == PowerManager.OFF)
							powerControl.onButtonPushed();
						else if (powerMgr.getPower() == PowerManager.UNKNOWN)
							powerControl.offButtonPushed();
					} catch (JmriException ex) {
						powerLight.setIcon(powerXIcon);
					}
				}
			});
		}
		// add help selection
		addHelpMenu("package.jmri.jmrit.throttle.ThrottleFrame", true);
	}
    
    public void dispose() {
        if (powerMgr!=null) powerMgr.removePropertyChangeListener(this);
        _controlPanel.dispose();
        _functionPanel.dispose();
        super.dispose();
    }
    
    /**
     *  implement a property change listener for power and throttle
     *  Set the GUI's to correspond to the throttle settings
     */
    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        if (log.isDebugEnabled()) log.debug("propertyChange "+evt.getPropertyName()+"= "+evt.getNewValue());
		if (evt.getPropertyName().equals("Power")) {
            setPowerIcons();
        }
    }


    /**
    *  Record throttle commands that have been sent to the throttle.
    */
    /* from ControlPanel */
    protected void setSpeedSetting(float speed) {
        _throttle.setSpeedSetting(speed);
        _warrantFrame.setThrottleCommand("Speed", Float.toString(speed));
    }
    /* from ControlPanel */
    protected void setSpeedStepMode(int speedStep) {
        _throttle.setSpeedStepMode(speedStep);
        _warrantFrame.setThrottleCommand("SpeedStep", Integer.toString(speedStep));
    }
    /* from FunctionPanel */
    protected void setFunctionState(int num, boolean isSet) {
        _warrantFrame.setThrottleCommand("F"+num, Boolean.toString(isSet));
    }
    /* from FunctionPanel */
    protected void setFunctionLock(int num, boolean isLockable) {
        _warrantFrame.setThrottleCommand("LockF"+num, Boolean.toString(isLockable));
    }
    /* from ButtonPanel */
    protected void setIsForward(boolean isForward) {
        _throttle.setIsForward(isForward);
        _buttonPanel.setForwardDirection(isForward);
        _warrantFrame.setThrottleCommand("Forward", Boolean.toString(isForward));
    }
    
    /**
     *  change the power LED displayed as appropriate and set corresponding tooltip
     *  
     */
    public void setPowerIcons() {
        if (powerMgr==null) return;
        try {
            if (powerMgr.getPower()==PowerManager.ON) {
                powerLight.setIcon(powerOnIcon);
                powerLight.setToolTipText("Layout Power On.  Click light to turn off, or use Power menu");
            }
            else if (powerMgr.getPower()==PowerManager.OFF) {
                powerLight.setIcon(powerOffIcon);
                powerLight.setToolTipText("Layout Power Off.  Click light to turn on, or use Power menu");
            }
            else if (powerMgr.getPower()==PowerManager.UNKNOWN) {
                powerLight.setIcon(powerXIcon);
                powerLight.setToolTipText("Layout Power state unknown.  Click light to turn off, or use Power menu");
            }
            else {
                powerLight.setIcon(powerXIcon);
                powerLight.setToolTipText("Layout Power state unknown.  Click light to turn off, or use Power menu");
                log.error("Unexpected state value: +"+powerMgr.getPower());
            }
        } catch (JmriException ex) {
            powerLight.setIcon(powerXIcon);
            powerLight.setToolTipText("Layout Power state unknown.  Click light to turn off, or use Power menu");
        }
    }

    /**
     *  A KeyAdapter that listens for the keys that work the control pad buttons
     *
     * @author     glen
     * @version    $Revision: 1.1 $
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
            if ( (e.getKeyCode() == accelerateKey) || (e.getKeyCode() == accelerateKey1) ) {
                _controlPanel.accelerate1();
            } else if ( e.getKeyCode() == accelerateKey2 ) {
                _controlPanel.accelerate10();
            } else if ( (e.getKeyCode() == decelerateKey) || (e.getKeyCode() == decelerateKey1) ) {
                _controlPanel.decelerate1();
            } else if ( e.getKeyCode() == decelerateKey2 ) {
                _controlPanel.decelerate10();
            } else if (e.getKeyCode() == forwardKey) {
                _buttonPanel.forwardButton.doClick();
            } else if (e.getKeyCode() == reverseKey) {
                _buttonPanel.reverseButton.doClick();
            } else if (e.getKeyCode() == stopKey) {
                _buttonPanel.stop();
            }
        }
    }
    
    class ButtonFrame extends JPanel {
        //private GridBagConstraints sliderConstraints;
        private JButton forwardButton, reverseButton;
        private JLabel forwardLight, reverseLight, stopLabel;
        private JButton stopButton;
        
        ButtonFrame() {
            super();
            forwardButton = new JButton(rb.getString("ButtonForward"));
            reverseButton = new JButton(rb.getString("ButtonReverse"));
            initGUI();
        }

        private void initGUI() {
            JPanel _buttonPanel = new JPanel();
            _buttonPanel.setLayout(new BoxLayout(_buttonPanel, BoxLayout.X_AXIS));
            add(_buttonPanel);

            GridBagConstraints constraints = new GridBagConstraints();
            constraints.anchor = GridBagConstraints.CENTER;
            //constraints.fill = GridBagConstraints.BOTH;
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

            forwardLight = new JLabel();
            forwardLight.setIcon(directionOffIcon);
            forwardButton.addActionListener(new ActionListener()  {
                                                public void actionPerformed(ActionEvent e) {
                                                    setIsForward(true);
                                                }
                                            });
            JPanel forwardPanel = new JPanel();
            forwardPanel.setLayout(new GridBagLayout());
            forwardPanel.add(forwardLight, constraints);
            constraints.gridy = 1;
            forwardPanel.add(forwardButton, constraints);

            reverseLight = new JLabel();
            reverseLight.setIcon(directionOffIcon);
            reverseButton.addActionListener(new ActionListener() {
                                                public void actionPerformed(ActionEvent e) {
                                                    setIsForward(false);
                                                }
                                            });
            JPanel reversePanel = new JPanel();
            reversePanel.setLayout(new GridBagLayout());
            constraints.gridy = 0;
            reversePanel.add(reverseLight, constraints);
            constraints.gridy = 1;
            reversePanel.add(reverseButton, constraints);

            stopLabel = new JLabel("Emergency");
            stopButton = new JButton("Stop");
            stopButton.addActionListener(new ActionListener() {
                                             public void actionPerformed(ActionEvent e)  {
                                                 stop();
                                             }
                                         });
            JPanel stopPanel = new JPanel();
            stopPanel.setLayout(new GridBagLayout());
            constraints.gridy = 0;
            stopPanel.add(stopLabel, constraints);
            constraints.gridy = 1;
            stopPanel.add(stopButton, constraints);

            _buttonPanel.add(Box.createHorizontalStrut(STRUT_SIZE));
            _buttonPanel.add(forwardPanel);
            _buttonPanel.add(Box.createHorizontalStrut(STRUT_SIZE));
            _buttonPanel.add(stopPanel);
            _buttonPanel.add(Box.createHorizontalStrut(STRUT_SIZE));
            _buttonPanel.add(reversePanel);
            _buttonPanel.add(Box.createHorizontalStrut(STRUT_SIZE));
            pack();
        }

        /**
         *  Perform an emergency stop
         */
        public void stop()
        {
            _controlPanel.speedSetting(0.0F);
            setSpeedSetting(-1);
            setSpeedSetting(0);
            stopLabel.setIcon(stopIcon);
            stopLabel.setIconTextGap(-stopLabel.getPreferredSize().width/2);
            pack();
        }

        public void notifyThrottleFound(DccThrottle t) {
            boolean isForward = t.getIsForward();
            this.setForwardDirection(isForward);
            setIsForward(isForward);
        }

        /**
         *  Set the GUI to match that the loco is set to forward.
         * @param  isForward  True if the loco is set to forward, false otherwise.
         */
        public void setForwardDirection(boolean isForward) {
            if (isForward) {
                forwardLight.setIcon(directionOnIcon);
                reverseLight.setIcon(directionOffIcon);
            } else {
                forwardLight.setIcon(directionOffIcon);
                reverseLight.setIcon(directionOnIcon);
            }
            pack();
        }


    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LearnThrottleFrame.class.getName());
    
}

