package jmri.jmrit.throttle;

import jmri.DccThrottle;
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.jdom.Element;

/**
 * A JInternalFrame that contains buttons for each decoder function.
 */
public class FunctionPanel extends JInternalFrame implements FunctionListener,java.beans.PropertyChangeListener
{
    public static final int NUM_FUNCTION_BUTTONS = 13;
    private DccThrottle throttle;

    private FunctionButton functionButton[];

    /**
     * Constructor
     */
    public FunctionPanel()
    {
        initGUI();
    }

	public void destroy()
	{
		if (throttle != null)
		{
			throttle.setF0(false);
			throttle.setF1(false);
			throttle.setF2(false);
			throttle.setF3(false);
			throttle.setF4(false);
			throttle.setF5(false);
			throttle.setF6(false);
			throttle.setF7(false);
			throttle.setF8(false);
			throttle.setF9(false);
			throttle.setF10(false);
			throttle.setF11(false);
			throttle.setF12(false);
		}
	}

    /**
     * Get notification that a throttle has been found as we requested.
     * Use reflection to find the proper getF? method for each button.
     *
     * @param t An instantiation of the DccThrottle with the address requested.
     */
    public void notifyThrottleFound(DccThrottle t)
    {
        log.debug("Throttle found");
        this.throttle = t;
        for (int i=0; i<this.NUM_FUNCTION_BUTTONS; i++)
        {
           try
           {
                int functionNumber = functionButton[i].getIdentity();
                java.lang.reflect.Method getter =
                        throttle.getClass().getMethod("getF"+functionNumber,null);
                Boolean state = (Boolean)getter.invoke(throttle, null);
                functionButton[i].setState(state.booleanValue());
           }
           catch (java.lang.NoSuchMethodException ex1)
           {
               log.warn("Exception in notifyThrottleFound: "+ex1);
           }
           catch (java.lang.IllegalAccessException ex2)
           {
               log.warn("Exception in notifyThrottleFound: "+ex2);
           }
           catch (java.lang.reflect.InvocationTargetException ex3)
           {
               log.warn("Exception in notifyThrottleFound: "+ex3);
           }
        }
        this.setEnabled(true);
        this.throttle.addPropertyChangeListener(this);
    }

	public void notifyThrottleDisposed()
	{
		this.setEnabled(false);
		this.throttle.removePropertyChangeListener(this);
		throttle = null;
	}

    /**
     * Get notification that a function has changed state
     * @param functionNumber The function that has changed (0-9).
     * @param isSet True if the function is now active (or set).
     */
    public void notifyFunctionStateChanged(int functionNumber, boolean isSet)
    {
        switch (functionNumber)
        {
            case 0: throttle.setF0(isSet); break;
            case 1: throttle.setF1(isSet); break;
            case 2: throttle.setF2(isSet); break;
            case 3: throttle.setF3(isSet); break;
            case 4: throttle.setF4(isSet); break;
            case 5: throttle.setF5(isSet); break;
            case 6: throttle.setF6(isSet); break;
            case 7: throttle.setF7(isSet); break;
            case 8: throttle.setF8(isSet); break;
            case 9: throttle.setF9(isSet); break;
            case 10: throttle.setF10(isSet); break;
            case 11: throttle.setF11(isSet); break;
            case 12: throttle.setF12(isSet); break;
        }
    }

    /**
     * Get notification that a function's lockable status has changed.
     * @param functionNumber The function that has changed (0-9).
     * @param isLockable True if the function is now Lockable 
     * (continuously active).
     */
    public void notifyFunctionLockableChanged(int functionNumber, boolean isLockable)
    {
        if (throttle==null) {
            log.error("throttle pointer null in notifyFunctionLockableChanged");
            return;
        }
        
        switch (functionNumber)
        {
            case 0: throttle.setF0Momentary(!isLockable); break;
            case 1: throttle.setF1Momentary(!isLockable); break;
            case 2: throttle.setF2Momentary(!isLockable); break;
            case 3: throttle.setF3Momentary(!isLockable); break;
            case 4: throttle.setF4Momentary(!isLockable); break;
            case 5: throttle.setF5Momentary(!isLockable); break;
            case 6: throttle.setF6Momentary(!isLockable); break;
            case 7: throttle.setF7Momentary(!isLockable); break;
            case 8: throttle.setF8Momentary(!isLockable); break;
            case 9: throttle.setF9Momentary(!isLockable); break;
            case 10: throttle.setF10Momentary(!isLockable); break;
            case 11: throttle.setF11Momentary(!isLockable); break;
            case 12: throttle.setF12Momentary(!isLockable); break;
        }
    }

    /**
     * Enable or disable all the buttons
     */
    public void setEnabled(boolean isEnabled)
    {
        //super.setEnabled(isEnabled);
        for (int i=0; i < NUM_FUNCTION_BUTTONS; i++)
        {
            functionButton[i].setEnabled(isEnabled);
        }
    }


    /**
     * Place and initialize all the buttons.
     */
    private void initGUI()
    {
        JPanel mainPanel = new JPanel();
        this.setContentPane(mainPanel);
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        mainPanel.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
        functionButton = new FunctionButton[NUM_FUNCTION_BUTTONS];
        for (int i=0; i<NUM_FUNCTION_BUTTONS; i++)
        {
            functionButton[i] = new FunctionButton();
            functionButton[i].setIdentity(i);
            functionButton[i].setFunctionListener(this);
            functionButton[i].setText("F"+String.valueOf(i));
            if (i > 0)
            {
                mainPanel.add(functionButton[i]);
            }
        }
        mainPanel.add(functionButton[0]);

		functionButton[0].setKeyCode(KeyEvent.VK_NUMPAD0);
		functionButton[1].setKeyCode(KeyEvent.VK_NUMPAD1);
		functionButton[2].setKeyCode(KeyEvent.VK_NUMPAD2);
		functionButton[3].setKeyCode(KeyEvent.VK_NUMPAD3);
		functionButton[4].setKeyCode(KeyEvent.VK_NUMPAD4);
		functionButton[5].setKeyCode(KeyEvent.VK_NUMPAD5);
		functionButton[6].setKeyCode(KeyEvent.VK_NUMPAD6);
		functionButton[7].setKeyCode(KeyEvent.VK_NUMPAD7);
		functionButton[8].setKeyCode(KeyEvent.VK_NUMPAD8);
		functionButton[9].setKeyCode(KeyEvent.VK_NUMPAD9);
		functionButton[10].setKeyCode(110); // numpad decimal (f10 button causes problems)
		functionButton[11].setKeyCode(KeyEvent.VK_F11);
		functionButton[12].setKeyCode(KeyEvent.VK_F12);
		KeyListenerInstaller.installKeyListenerOnAllComponents(
				new FunctionButtonKeyListener(), this);
    }


	/**
	 *  A KeyAdapter that listens for the keys that work the function buttons
	 *
	 * @author     glen
          * @version    $Revision: 1.28 $
	 */
	class FunctionButtonKeyListener extends KeyAdapter
	{
		private boolean keyReleased = true;

		/**
		 *  Description of the Method
		 *
		 * @param  e  Description of the Parameter
		 */
		public void keyPressed(KeyEvent e)
		{
			if (keyReleased)
			{
				log.debug("Pressed");
				for (int i=0; i<NUM_FUNCTION_BUTTONS; i++)
				{
					if ( functionButton[i].checkKeyCode(e.getKeyCode()) )
					{
						functionButton[i].changeState(!functionButton[i].isSelected());
					}
				}
			}
			keyReleased = false;
		}

		public void keyTyped(KeyEvent e)
		{
			log.debug("Typed");
		}

		public void keyReleased(KeyEvent e)
		{
 			log.debug("Released");
			for (int i=0; i<NUM_FUNCTION_BUTTONS; i++)
			{
				if ( functionButton[i].checkKeyCode(e.getKeyCode()) )
				{
					if (!functionButton[i].getIsLockable())
					{
						functionButton[i].changeState(!functionButton[i].isSelected());
					}
				}
			}
			keyReleased = true;
		}
	}

 	// update the state of this panel if any of the properties change
        public void propertyChange(java.beans.PropertyChangeEvent e) {
                if (e.getPropertyName().equals("F0")) {
                     boolean function=((Boolean) e.getNewValue()).booleanValue();
		     functionButton[0].changeState(function);
                } else if (e.getPropertyName().equals("F1")) {
                   boolean function=((Boolean) e.getNewValue()).booleanValue();
		   functionButton[1].changeState(function);
                } else if (e.getPropertyName().equals("F2")) {
                   boolean function=((Boolean) e.getNewValue()).booleanValue();
		   functionButton[2].changeState(function);
                } else if (e.getPropertyName().equals("F3")) {
                   boolean function=((Boolean) e.getNewValue()).booleanValue();
		   functionButton[3].changeState(function);
                } else if (e.getPropertyName().equals("F4")) {
                   boolean function=((Boolean) e.getNewValue()).booleanValue();
		   functionButton[4].changeState(function);
                } else if (e.getPropertyName().equals("F5")) {
                   boolean function=((Boolean) e.getNewValue()).booleanValue();
		   functionButton[5].changeState(function);
                } else if (e.getPropertyName().equals("F6")) {
                   boolean function=((Boolean) e.getNewValue()).booleanValue();
		   functionButton[6].changeState(function);
                } else if (e.getPropertyName().equals("F7")) {
                   boolean function=((Boolean) e.getNewValue()).booleanValue();
		   functionButton[7].changeState(function);
                } else if (e.getPropertyName().equals("F8")) {
                   boolean function=((Boolean) e.getNewValue()).booleanValue();
		   functionButton[8].changeState(function);
                } else if (e.getPropertyName().equals("F9")) {
                   boolean function=((Boolean) e.getNewValue()).booleanValue();
		   functionButton[9].changeState(function);
                } else if (e.getPropertyName().equals("F10")) {
                   boolean function=((Boolean) e.getNewValue()).booleanValue();
		   functionButton[10].changeState(function);
                } else if (e.getPropertyName().equals("F11")) {
                   boolean function=((Boolean) e.getNewValue()).booleanValue();
		   functionButton[11].changeState(function);
                } else if (e.getPropertyName().equals("F12")) {
                   boolean function=((Boolean) e.getNewValue()).booleanValue();
		   functionButton[12].changeState(function);
                } else if (e.getPropertyName().equals("F0Momentary")) {
                   boolean lockable=!((Boolean) e.getNewValue()).booleanValue();
		   functionButton[0].setIsLockable(lockable);
                } else if (e.getPropertyName().equals("F1Momentary")) {
                   boolean lockable=!((Boolean) e.getNewValue()).booleanValue();
		   functionButton[1].setIsLockable(lockable);
                } else if (e.getPropertyName().equals("F2Momentary")) {
                   boolean lockable=!((Boolean) e.getNewValue()).booleanValue();
		   functionButton[2].setIsLockable(lockable);
                } else if (e.getPropertyName().equals("F3Momentary")) {
                   boolean lockable=!((Boolean) e.getNewValue()).booleanValue();
		   functionButton[3].setIsLockable(lockable);
                } else if (e.getPropertyName().equals("F4Momentary")) {
                   boolean lockable=!((Boolean) e.getNewValue()).booleanValue();
		   functionButton[4].setIsLockable(lockable);
                } else if (e.getPropertyName().equals("F5Momentary")) {
                   boolean lockable=!((Boolean) e.getNewValue()).booleanValue();
		   functionButton[5].setIsLockable(lockable);
                } else if (e.getPropertyName().equals("F6Momentary")) {
                   boolean lockable=!((Boolean) e.getNewValue()).booleanValue();
		   functionButton[6].setIsLockable(lockable);
                } else if (e.getPropertyName().equals("F7Momentary")) {
                   boolean lockable=!((Boolean) e.getNewValue()).booleanValue();
		   functionButton[7].setIsLockable(lockable);
                } else if (e.getPropertyName().equals("F8Momentary")) {
                   boolean lockable=!((Boolean) e.getNewValue()).booleanValue();
		   functionButton[8].setIsLockable(lockable);
                } else if (e.getPropertyName().equals("F9Momentary")) {
                   boolean lockable=!((Boolean) e.getNewValue()).booleanValue();
		   functionButton[9].setIsLockable(lockable);
                } else if (e.getPropertyName().equals("F10Momentary")) {
                   boolean lockable=!((Boolean) e.getNewValue()).booleanValue();
		   functionButton[10].setIsLockable(lockable);
                } else if (e.getPropertyName().equals("F11Momentary")) {
                   boolean lockable=!((Boolean) e.getNewValue()).booleanValue();
		   functionButton[11].setIsLockable(lockable);
                } else if (e.getPropertyName().equals("F12Momentary")) {
                   boolean lockable=!((Boolean) e.getNewValue()).booleanValue();
		   functionButton[12].setIsLockable(lockable);
                }


        }


    /**
     * Collect the prefs of this object into XML Element
     * <ul>
     * <li> Window prefs
     * <li> Each button has id, text, lock state.
     * </ul>
     * @return the XML of this object.
     */
    public Element getXml()
    {
        Element me = new Element("FunctionPanel");
        Element window = new Element("window");
        WindowPreferences wp = new WindowPreferences();
        java.util.ArrayList children =
                new java.util.ArrayList(1);
        children.add(wp.getPreferences(this));
        for (int i=0; i<this.NUM_FUNCTION_BUTTONS; i++)
        {
            children.add(functionButton[i].getXml());
        }
        me.setContent(children);
        return me;
    }

    /**
     * Set the preferences based on the XML Element.
     * <ul>
     * <li> Window prefs
     * <li> Each button has id, text, lock state.
     * </ul>
     * @param e The Element for this object.
     */
    public void setXml(Element e)
    {
        Element window = e.getChild("window");
        WindowPreferences wp = new WindowPreferences();
        wp.setPreferences(this, window);

        java.util.List buttonElements =
                e.getChildren("FunctionButton");

        int i = 0;
        for (java.util.Iterator iter =
             buttonElements.iterator(); iter.hasNext();)
        {
            Element buttonElement = (Element)iter.next();
            functionButton[i++].setXml(buttonElement);
        }
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(FunctionPanel.class.getName());
}
