package jmri.jmrit.throttle;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;

import org.jdom.Element;


/**
 * A JButton to activate functions on the decoder. FunctionButtons
 * have a right-click popup menu with several configuration options:
 * <ul>
 * <li> Set the text
 * <li> Set the locking state
 * <li> Set visibilty
 * <li> Set Font
 * <li> Set function number identity
 * </ul>
 */
public class FunctionButton extends JToggleButton implements ActionListener
{
    private FunctionListener listener;
    private int identity; // F0, F1, etc?
    private boolean isOn;
    private boolean isLockable = true;
	private int actionKey;

    private JPopupMenu popup;

    /**
     * Construct the FunctionButton.
     */
    public FunctionButton()
    {
        popup = new JPopupMenu();

        JMenuItem propertiesItem = new JMenuItem("Properties");
        propertiesItem.addActionListener(this);
        popup.add(propertiesItem);

        //Add listener to components that can bring up popup menus.
        MouseListener popupListener = new PopupListener();
        this.addMouseListener(popupListener);

        this.setPreferredSize(new Dimension(54,30));
        this.setMargin(new Insets(2,2,2,2));
    }


    /**
     * Set the function number this button will operate
     * @param id An integer from 0 to 9.
     */
    public void setIdentity(int id)
    {
        this.identity = id;
    }

    /**
     * Get the function number this button operates
     * @return An integer from 0 to 9.
     */
    public int getIdentity()
    {
        return identity;
    }

	public void setKeyCode(int key)
	{
		actionKey = key;
	}


	public int getKeyCode()
	{
		return actionKey;
	}

    /**
     * Set the state of the function
     * @param isOn True if the function should be active.
     */
    public void setState(boolean isOn)
    {
        this.isOn = isOn;
		this.setSelected(isOn);
    }

    /**
     * Set the locking state of the button
     * @param isLockable True if the a clicking and releasing the button
     * changes the function state. False if the state is changed
     * back when the button is released
     */
    public void setIsLockable(boolean isLockable)
    {
        this.isLockable = isLockable;
    }

    /**
     * Get the locking state of the function
     * @return True if the a clicking and releasing the button
     * changes the function state. False if the state is changed
     * back when the button is released
     */
    public boolean getIsLockable()
    {
        return isLockable;
    }


    /**
     * Handle the selection from the popup menu.
     * @param e The ActionEvent causing the action.
     */
    public void actionPerformed(ActionEvent e)
    {
        FunctionButtonPropertyEditor editor =
                ThrottleFrameManager.instance().getFunctionButtonEditor();
        editor.setFunctionButton(this);
        editor.setLocation(this.getLocationOnScreen());
        editor.setVisible(true);
    }

    /**
     * Change the state of the function.
     * @param newState The new state. True = Is on, False = Is off.
     */
    public void changeState(boolean newState)
    {
        if (log.isDebugEnabled()) {
        	log.debug("Change state to "+newState);
        	new Exception().printStackTrace();
        }
        isOn = newState;
		this.setSelected(isOn);
        if (listener != null)
        {
            listener.notifyFunctionStateChanged(identity, isOn);
        }
    }


    /**
     * Add a listener to this button, probably some sort of keypad panel.
     * @param l The FunctionListener that wants notifications via the
     * FunctionListener.notifyFunctionStateChanged.
     */
    public void setFunctionListener(FunctionListener l)
    {
        this.listener = l;
    }

    /**
     * A PopupListener to handle mouse clicks and releases. Handles
     * the popup menu.
     */
    class PopupListener extends MouseAdapter
    {
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
                    +" "+(e.getModifiers() & (e.ALT_MASK+e.META_MASK+e.CTRL_MASK))
                    +(" "+e.ALT_MASK+"/"+e.META_MASK+"/"+e.CTRL_MASK));
            JToggleButton button = (JToggleButton)e.getSource();
            if (e.isPopupTrigger())
            {
                popup.show(e.getComponent(),
                           e.getX(), e.getY());
            }
            /* Must check button mask since some platforms wait
            for mouse release to do popup. */
            else if (button.isEnabled()
                     && ((e.getModifiers() & e.BUTTON1_MASK) != 0)
                    && ((e.getModifiers() & (e.ALT_MASK+e.META_MASK+e.CTRL_MASK)) == 0)
                     && !isLockable)
            {
                changeState(true);
            }
            // force button to desired state; click might have changed it
            button.setSelected(isOn);
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
            if (log.isDebugEnabled()) log.debug("released "+(e.getModifiers() & e.BUTTON1_MASK)+" "+e.isPopupTrigger()
                    +" "+(e.getModifiers() & (e.ALT_MASK+e.META_MASK+e.CTRL_MASK)));
            JToggleButton button = (JToggleButton)e.getSource();
            if (e.isPopupTrigger())
            {
                popup.show(e.getComponent(),
                           e.getX(), e.getY());
            }
            // mouse events have to be unmodified; to change function, so that
            // we don't act on 1/2 of a popup request.
            else if (button.isEnabled()
                    && ((e.getModifiers() & e.BUTTON1_MASK) != 0)
                    && ((e.getModifiers() & (e.ALT_MASK+e.META_MASK+e.CTRL_MASK)) == 0) )
            {
                if (!isLockable)
                {
                    changeState(false);
                }
                else
                {
                    changeState(!isOn);
                }
            }
            // force button to desired state
            button.setSelected(isOn);
        }
    }


    /**
     * Collect the prefs of this object into XML Element
     * <ul>
     * <li> identity
     * <li> text
     * <li> isLockable
     * </ul>
     * @return the XML of this object.
     */
    public Element getXml()
    {
        Element me = new Element("FunctionButton");
        me.addAttribute("id", String.valueOf(this.getIdentity()));
        me.addAttribute("text", this.getText());
        me.addAttribute("isLockable", String.valueOf(this.getIsLockable()));
        me.addAttribute("isVisible", String.valueOf(this.isVisible()));
        me.addAttribute("fontSize", String.valueOf(this.getFont().getSize()));
        return me;
    }

    /**
     * Set the preferences based on the XML Element.
     * <ul>
     * <li> identity
     * <li> text
     * <li> isLockable
      * </ul>
     * @param e The Element for this object.
     */
    public void setXml(Element e)
    {
        try
        {
            this.setIdentity(e.getAttribute("id").getIntValue());
            this.setText(e.getAttribute("text").getValue());
            boolean isLockable = e.getAttribute("isLockable").getBooleanValue();
            this.setIsLockable(isLockable);
            boolean isVisible = e.getAttribute("isVisible").getBooleanValue();
            this.setVisible(isVisible);
            this.setFont(new Font("", Font.PLAIN, e.getAttribute("fontSize").getIntValue()));
        }
        catch (org.jdom.DataConversionException ex)
        {
            log.error("DataConverstionException in setXml: "+ex);
        }
    }
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(FunctionButton.class.getName());
}