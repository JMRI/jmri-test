package jmri.jmrit.logix;

import jmri.DccThrottle;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ResourceBundle;

import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.throttle.FunctionListener;
import jmri.jmrit.throttle.FunctionButton;
import jmri.jmrit.throttle.KeyListenerInstaller;


/**
 * A JInternalFrame that contains buttons for each decoder function.
 */
public class FunctionPanel extends JInternalFrame implements FunctionListener
{
	ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.throttle.ThrottleBundle");
	
	public static final int NUM_FUNCTION_BUTTONS = 29;
    public static final int NUM_FUNC_BUTTONS_INIT = 16;	//only show 16 function buttons at start
    private DccThrottle throttle;
    private RosterEntry _rosterEntry;
    private LearnThrottleFrame _throttleFrame;

    private FunctionButton functionButton[];
    javax.swing.JToggleButton alt1Button = new javax.swing.JToggleButton();
    javax.swing.JToggleButton alt2Button = new javax.swing.JToggleButton();

    /**
     * Constructor
     */
    public FunctionPanel(RosterEntry rosterEntry, LearnThrottleFrame learnFrame) {
        super("Functions");
        _rosterEntry = rosterEntry;
        _throttleFrame = learnFrame;
        initGUI();
    }

    /**
     * Get notification that a throttle has been found as we requested.
     * Use reflection to find the proper getF? method for each button.
     *
     * @param t An instantiation of the DccThrottle with the address requested.
     */
    public void notifyThrottleFound(DccThrottle t)
    {
        if (log.isDebugEnabled()) log.debug("Throttle found");
        this.throttle = t;
        for (int i=0; i<this.NUM_FUNCTION_BUTTONS; i++)
        {
           try {
                int functionNumber = functionButton[i].getIdentity();
                java.lang.reflect.Method getter =
                        throttle.getClass().getMethod("getF"+functionNumber,(Class[])null);
                Boolean state = (Boolean)getter.invoke(throttle, (Object[])null);
                functionButton[i].setState(state.booleanValue());
                if (_rosterEntry != null){
                	String text = _rosterEntry.getFunctionLabel(functionNumber);
                	if (text != null){
                		functionButton[i].setText(text);
                		// adjust button width for text
                		int butWidth = functionButton[i].getFontMetrics(functionButton[i].getFont()).stringWidth(text);
                		butWidth = butWidth + 20;	// pad out the width a bit
                		if (butWidth < FunctionButton.BUT_WDTH) butWidth = FunctionButton.BUT_WDTH;
                		functionButton[i].setPreferredSize(new Dimension(butWidth,FunctionButton.BUT_HGHT));
                		functionButton[i].setIsLockable(_rosterEntry.getFunctionLockable(functionNumber));
                	}
                }
           } catch (java.lang.NoSuchMethodException ex1) {
               log.warn("Exception in notifyThrottleFound: "+ex1);
           } catch (java.lang.IllegalAccessException ex2)  {
               log.warn("Exception in notifyThrottleFound: "+ex2);
           } catch (java.lang.reflect.InvocationTargetException ex3) {
               log.warn("Exception in notifyThrottleFound: "+ex3);
           }
        }
        this.setEnabled(true);
    }

//    public FunctionButton[] getFunctionButtons() { return functionButton; }
    
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
            case 13: throttle.setF13(isSet); break;
            case 14: throttle.setF14(isSet); break;
            case 15: throttle.setF15(isSet); break;
            case 16: throttle.setF16(isSet); break;
            case 17: throttle.setF17(isSet); break;
            case 18: throttle.setF18(isSet); break;
            case 19: throttle.setF19(isSet); break;
            case 20: throttle.setF20(isSet); break;
            case 21: throttle.setF21(isSet); break;
            case 22: throttle.setF22(isSet); break;
            case 23: throttle.setF23(isSet); break;
            case 24: throttle.setF24(isSet); break;
            case 25: throttle.setF25(isSet); break;
            case 26: throttle.setF26(isSet); break;
            case 27: throttle.setF27(isSet); break;
            case 28: throttle.setF28(isSet); break;
        }
        _throttleFrame.setFunctionState(functionNumber, isSet);
    }

    /**
     * Get notification that a function's lockable status has changed.
     * @param functionNumber The function that has changed (0-28).
     * @param isLockable True if the function is now Lockable 
     * (continuously active).
     */
    public void notifyFunctionLockableChanged(int functionNumber, boolean isLockable)
    {
        if (throttle==null) {
        	// throttle can be null when loading throttle layout
        	if (log.isDebugEnabled())log.warn("throttle pointer null in notifyFunctionLockableChanged");
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
            case 13: throttle.setF13Momentary(!isLockable); break;
            case 14: throttle.setF14Momentary(!isLockable); break;
            case 15: throttle.setF15Momentary(!isLockable); break;
            case 16: throttle.setF16Momentary(!isLockable); break;
            case 17: throttle.setF17Momentary(!isLockable); break;
            case 18: throttle.setF18Momentary(!isLockable); break;
            case 19: throttle.setF19Momentary(!isLockable); break;
            case 20: throttle.setF20Momentary(!isLockable); break;
            case 21: throttle.setF21Momentary(!isLockable); break;
            case 22: throttle.setF22Momentary(!isLockable); break;
            case 23: throttle.setF23Momentary(!isLockable); break;
            case 24: throttle.setF24Momentary(!isLockable); break;
            case 25: throttle.setF25Momentary(!isLockable); break;
            case 26: throttle.setF26Momentary(!isLockable); break;
            case 27: throttle.setF27Momentary(!isLockable); break;
            case 28: throttle.setF28Momentary(!isLockable); break;
        }
        _throttleFrame.setFunctionLock(functionNumber, isLockable);
    }

    /**
     * Enable or disable all the buttons
     */
    public void setEnabled(boolean isEnabled)
    {
        super.setEnabled(isEnabled);
        for (int i=0; i < NUM_FUNCTION_BUTTONS; i++)
        {
            functionButton[i].setEnabled(isEnabled);
        }
    }
    
    protected void resetFuncButtons(){
        functionButton = null;
    	initGUI();
    	setEnabled(true);
    }
    
    JPanel mainPanel = new JPanel();
    /**
     * Place and initialize all the buttons.
     */
    public void initGUI(){
        mainPanel.removeAll();
        this.setContentPane(mainPanel);
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        mainPanel.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
        functionButton = new FunctionButton[NUM_FUNCTION_BUTTONS];
        for (int i=0; i<NUM_FUNCTION_BUTTONS; i++)
        {
            functionButton[i] = new FunctionButton();
            functionButton[i].setIdentity(i);
            functionButton[i].setFunctionListener(this);
            if(i < 3)
            	functionButton[i].setText(rb.getString("F"+String.valueOf(i)));
            else
            	functionButton[i].setText("F"+String.valueOf(i));
             if (i > 0)
            {
                mainPanel.add(functionButton[i]);
                if (i >= NUM_FUNC_BUTTONS_INIT){
                	functionButton[i].setVisible(false);
                }
            }
        }
        alt1Button.setText("Alt");
        alt1Button.setPreferredSize(new Dimension(FunctionButton.BUT_WDTH,FunctionButton.BUT_HGHT));
        alt1Button.setToolTipText(java.util.ResourceBundle.getBundle("jmri/jmrit/throttle/ThrottleBundle").getString("Push_for_alternate_set_of_function_keys"));
        alt1Button.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				buttonActionCmdPerformed();
			}
		});
        mainPanel.add(alt1Button);
        
        mainPanel.add(functionButton[0]);
        
        alt2Button.setText("#");
        alt2Button.setPreferredSize(new Dimension(FunctionButton.BUT_WDTH,FunctionButton.BUT_HGHT));
        alt2Button.setToolTipText(java.util.ResourceBundle.getBundle("jmri/jmrit/throttle/ThrottleBundle").getString("currently_not_used"));
        mainPanel.add(alt2Button);

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
		functionButton[13].setKeyCode(KeyEvent.VK_F13);
		functionButton[14].setKeyCode(KeyEvent.VK_F14);
		functionButton[15].setKeyCode(KeyEvent.VK_F15);
		functionButton[16].setKeyCode(KeyEvent.VK_F16);
		functionButton[17].setKeyCode(KeyEvent.VK_F17);
		functionButton[18].setKeyCode(KeyEvent.VK_F18);
		functionButton[19].setKeyCode(KeyEvent.VK_F19);
		functionButton[20].setKeyCode(KeyEvent.VK_F20);
		functionButton[21].setKeyCode(KeyEvent.VK_F21);
		functionButton[22].setKeyCode(KeyEvent.VK_F22);
		functionButton[23].setKeyCode(KeyEvent.VK_F23);
		functionButton[24].setKeyCode(KeyEvent.VK_F24);
		functionButton[25].setKeyCode(0xF00C);			// keycodes 25 - 28 don't exist in KeyEvent
		functionButton[26].setKeyCode(0xF00D);
		functionButton[27].setKeyCode(0xF00E);
		functionButton[28].setKeyCode(0xF00F);
		KeyListenerInstaller.installKeyListenerOnAllComponents(new FunctionButtonKeyListener(), this);
		// Make F2 (Horn) momentary
		functionButton[2].setIsLockable(false);
    }
    
    // activated when alt1Button is pressed or released
    public void buttonActionCmdPerformed(){
		// swap f3 through f15 with f16 through f28
		for (int i = 3; i < NUM_FUNCTION_BUTTONS; i++) {

			if (alt1Button.isSelected()) {
				if (i < NUM_FUNC_BUTTONS_INIT) {
					functionButton[i].setVisible(false);
				} else {
					functionButton[i].setVisible(functionButton[i].getDisplay());
				}

			} else {
				if (i < NUM_FUNC_BUTTONS_INIT) {
					functionButton[i].setVisible(functionButton[i].getDisplay());
				} else {
					functionButton[i].setVisible(false);
				}
			}
		}
	}


    /**
     * Make sure that all function buttons are being displayed
     */
    public void showAllFnButtons() {
    	// should show all, or just the initial ones?
    	for (int i=0; i < NUM_FUNCTION_BUTTONS; i++) {
    		functionButton[i].setDisplay(true);
    		if (i<3)
    			functionButton[i].setVisible(true);
    	}
    	buttonActionCmdPerformed();
    }
    
	/**
	 * A KeyAdapter that listens for the keys that work the function buttons
	 * 
	 * @author glen
	 * @version $Revision: 1.2 $
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
	// did not add f13 - f28 dboudreau, maybe I should have? 
	public void setFunctionButton(java.beans.PropertyChangeEvent e) {
		if (e.getPropertyName().equals("F0")) {
			boolean function=((Boolean) e.getNewValue()).booleanValue();
			functionButton[0].setState(function);
		} else if (e.getPropertyName().equals("F1")) {
			boolean function=((Boolean) e.getNewValue()).booleanValue();
			functionButton[1].setState(function);
		} else if (e.getPropertyName().equals("F2")) {
			boolean function=((Boolean) e.getNewValue()).booleanValue();
			functionButton[2].setState(function);
		} else if (e.getPropertyName().equals("F3")) {
			boolean function=((Boolean) e.getNewValue()).booleanValue();
			functionButton[3].setState(function);
		} else if (e.getPropertyName().equals("F4")) {
			boolean function=((Boolean) e.getNewValue()).booleanValue();
			functionButton[4].setState(function);
		} else if (e.getPropertyName().equals("F5")) {
			boolean function=((Boolean) e.getNewValue()).booleanValue();
			functionButton[5].setState(function);
		} else if (e.getPropertyName().equals("F6")) {
			boolean function=((Boolean) e.getNewValue()).booleanValue();
			functionButton[6].setState(function);
		} else if (e.getPropertyName().equals("F7")) {
			boolean function=((Boolean) e.getNewValue()).booleanValue();
			functionButton[7].setState(function);
		} else if (e.getPropertyName().equals("F8")) {
			boolean function=((Boolean) e.getNewValue()).booleanValue();
			functionButton[8].setState(function);
		} else if (e.getPropertyName().equals("F9")) {
			boolean function=((Boolean) e.getNewValue()).booleanValue();
			functionButton[9].setState(function);
		} else if (e.getPropertyName().equals("F10")) {
			boolean function=((Boolean) e.getNewValue()).booleanValue();
			functionButton[10].setState(function);
		} else if (e.getPropertyName().equals("F11")) {
			boolean function=((Boolean) e.getNewValue()).booleanValue();
			functionButton[11].setState(function);
		} else if (e.getPropertyName().equals("F12")) {
			boolean function=((Boolean) e.getNewValue()).booleanValue();
			functionButton[12].setState(function);
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


    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(FunctionPanel.class.getName());
}
