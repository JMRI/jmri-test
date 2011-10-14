// SignalHeadIcon.java

package jmri.jmrit.display;

import jmri.SignalHead;
import jmri.InstanceManager;
import jmri.jmrit.catalog.ImageIndexEditor;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.palette.ItemPalette;
import jmri.jmrit.display.palette.SignalHeadItemPanel;
import jmri.jmrit.picker.PickListModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButtonMenuItem;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;
import jmri.NamedBeanHandle;

/**
 * An icon to display a status of a SignalHead.
 * <P>
 * SignalHeads are located via the SignalHeadManager, which in turn is located
 * via the InstanceManager.
 *
 * @see jmri.SignalHeadManager
 * @see jmri.InstanceManager
 * @author Bob Jacobsen Copyright (C) 2001, 2002
 * @version $Revision$
 */

public class SignalHeadIcon extends PositionableIcon implements java.beans.PropertyChangeListener {


    String[] _validKey;

    public SignalHeadIcon(Editor editor){
        super(editor);
    }

    public Positionable deepClone() {
        SignalHeadIcon pos = new SignalHeadIcon(_editor);
        return finishClone(pos);
    }

    public Positionable finishClone(Positionable p) {
        SignalHeadIcon pos = (SignalHeadIcon)p;
        pos.setSignalHead(getNamedSignalHead().getName());
        Enumeration <String> e = _iconMap.keys();
        while (e.hasMoreElements()) {
            String key = e.nextElement();
            pos.setIcon(key, _iconMap.get(key));
        }
        pos.setClickMode(getClickMode());
        pos.setLitMode(getLitMode());
        return super.finishClone(pos);
    }


//    private SignalHead mHead;
    private NamedBeanHandle<SignalHead> namedHead;

    /**
     * Attached a signalhead element to this display item
     * @param sh Specific SignalHead object
     */
    public void setSignalHead(NamedBeanHandle<SignalHead> sh) {
        if (namedHead != null) {
            getSignalHead().removePropertyChangeListener(this);
        }
        namedHead = sh;
        if (namedHead != null) {
            _iconMap = new Hashtable <String, NamedIcon>();
            _validKey = getSignalHead().getValidStateNames();
            displayState(headState());
            getSignalHead().addPropertyChangeListener(this);
        }
    }
    
     /**
     * Taken from the layout editor
     * Attached a numbered element to this display item
     * @param pName Used as a system/user name to lookup the SignalHead object
     */
    public void setSignalHead(String pName) {
        SignalHead mHead = InstanceManager.signalHeadManagerInstance().getBySystemName(pName);
        if (mHead == null) mHead = InstanceManager.signalHeadManagerInstance().getByUserName(pName);
        if (mHead == null) log.warn("did not find a SignalHead named "+pName);
        else {
            setSignalHead(new NamedBeanHandle<SignalHead>(pName, mHead));
        }
    }

    public NamedBeanHandle<SignalHead> getNamedSignalHead() {
        return namedHead;
    }

    public SignalHead getSignalHead(){
        if (namedHead==null)
            return null;
        return namedHead.getBean();
    }

    /**
    * Check that device supports the state
    * valid state names returned by the bean are localized
    */
    private boolean isValidState(String key) {
        if (key==null) {
            return false;
        }
        if (key.equals(rbean.getString("SignalHeadStateDark"))
                     || key.equals(rbean.getString("SignalHeadStateHeld")) ) {
            if (log.isDebugEnabled()) log.debug(key+" is a valid state. ");
            return true;
        }
        for (int i=0; i<_validKey.length; i++) {
            if (key.equals(_validKey[i])) {
                if (log.isDebugEnabled()) log.debug(key+" is a valid state. ");
                return true;
            }
        }
        if (log.isDebugEnabled()) log.debug(key+" is NOT a valid state. ");
        return false;
    }
    
    /**
    * Place icon by its bean state name key found in jmri.NamedBeanBundle.properties
    * Place icon by its localized bean state name
    */
    public void setIcon(String state, NamedIcon icon) {
        if (log.isDebugEnabled()) log.debug("setIcon for "+state);
        if (isValidState(state)) {
            _iconMap.put(state, icon);
            displayState(headState());
        }
    }

    /**
     * Get current appearance of the head
     * @return An appearance variable from a SignalHead, e.g. SignalHead.RED
     */
    public int headState() {
        if (getSignalHead()==null) return 0;
        else return getSignalHead().getAppearance();
    }

    // update icon as state of turnout changes
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (log.isDebugEnabled()) log.debug("property change: "+e.getPropertyName()
                                            +" current state: "+headState());
        displayState(headState());
		_editor.getTargetPanel().repaint(); 
    }

    public String getNameString() {
        String name;
        if (namedHead == null) name = rb.getString("NotConnected");
        else if (getSignalHead().getUserName()!=null)
            name = getSignalHead().getUserName();
        	//name = getSignalHead().getUserName()+" ("+getSignalHead().getSystemName()+")";
        else
            name = getSignalHead().getSystemName();
        return name;
    }

    ButtonGroup litButtonGroup = null;

    /**
     * Pop-up just displays the name
     */
    public boolean showPopUp(JPopupMenu popup) {
        if (isEditable()) {
            // add menu to select action on click
            JMenu clickMenu = new JMenu(rb.getString("WhenClicked"));
            ButtonGroup clickButtonGroup = new ButtonGroup();
            JRadioButtonMenuItem r;
            r = new JRadioButtonMenuItem(rb.getString("ChangeAspect"));
            r.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) { setClickMode(3); }
            });
            clickButtonGroup.add(r);
            if (clickMode == 3)  r.setSelected(true);
            else r.setSelected(false);
            clickMenu.add(r);
            r = new JRadioButtonMenuItem(rb.getString("Cycle3Aspects"));
            r.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) { setClickMode(0); }
            });
            clickButtonGroup.add(r);
            if (clickMode == 0)  r.setSelected(true);
            else r.setSelected(false);
            clickMenu.add(r);
            r = new JRadioButtonMenuItem(rb.getString("AlternateLit"));
            r.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) { setClickMode(1); }
            });
            clickButtonGroup.add(r);
            if (clickMode == 1)  r.setSelected(true);
            else r.setSelected(false);
            clickMenu.add(r);
            r = new JRadioButtonMenuItem(rb.getString("AlternateHeld"));
            r.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) { setClickMode(2); }
            });
            clickButtonGroup.add(r);
            if (clickMode == 2)  r.setSelected(true);
            else r.setSelected(false);
            clickMenu.add(r);
            popup.add(clickMenu);


            // add menu to select handling of lit parameter
            JMenu litMenu = new JMenu(rb.getString("WhenNotLit"));
            litButtonGroup = new ButtonGroup();
            r = new JRadioButtonMenuItem(rb.getString("ShowAppearance"));
            r.setIconTextGap(10);
            r.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) { setLitMode(false); }
            });
            litButtonGroup.add(r);
            if (!litMode)  r.setSelected(true);
            else r.setSelected(false);
            litMenu.add(r);
            r = new JRadioButtonMenuItem(rb.getString("ShowDarkIcon"));
            r.setIconTextGap(10);
            r.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) { setLitMode(true); }
            });
            litButtonGroup.add(r);
            if (litMode)  r.setSelected(true);
            else r.setSelected(false);
            litMenu.add(r);
            popup.add(litMenu);

            popup.add(new AbstractAction(rb.getString("EditLogic")) {
                public void actionPerformed(ActionEvent e) {
                    jmri.jmrit.blockboss.BlockBossFrame f = new jmri.jmrit.blockboss.BlockBossFrame();
                    String name = getNameString();
                    f.setTitle(java.text.MessageFormat.format(rb.getString("SignalLogic"), name));
                    f.setSignal(name);
                    f.setVisible(true);
                }
            });
            return true;
        }
        return false;
    }
    
    /*************** popup AbstractAction.actionPerformed method overrides ************/

    protected void rotateOrthogonal() {
        super.rotateOrthogonal();
        displayState(headState());
    }

    public void setScale(double s) {
        super.setScale(s);
        displayState(headState());
    }

    public void rotate(int deg) {
        super.rotate(deg);
        displayState(headState());
    }

    /**
     * Drive the current state of the display from the state of the
     * underlying SignalHead object.
     * <UL>
     * <LI>If the signal is held, display that.
     * <LI>If set to monitor the status of the lit parameter
     *     and lit is false, show the dark icon ("dark", when
     *     set as an explicit appearance, is displayed anyway)
     * <LI>Show the icon corresponding to one of the seven appearances.
     * </UL>
     */
    public void displayState(int state) {
        updateSize();
        if (getSignalHead() == null) {
            if (log.isDebugEnabled())log.debug("Display state "+state+", disconnected");
        } else {
            if (log.isDebugEnabled()) log.debug("Display state "+state+" for "+getNameString());
            if (getSignalHead().getHeld()) {
                if (isText()) super.setText(rb.getString("Held"));
                if (isIcon()) super.setIcon(_iconMap.get(rbean.getString("SignalHeadStateHeld")));
                return;
            }
            else if (getLitMode() && !getSignalHead().getLit()) {
                if (isText()) super.setText(rb.getString("Dark"));
                if (isIcon()) super.setIcon(_iconMap.get(rbean.getString("SignalHeadStateDark")));
                return;
            }
        }
        if (isText()) {
            super.setText(getSignalHead().getAppearanceName(state));
        }
        if (isIcon()) {
            NamedIcon icon =_iconMap.get(getSignalHead().getAppearanceName(state));
            if (icon!=null) {
                super.setIcon(icon);
            }
        }
        return;
    }

    SignalHeadItemPanel _itemPanel;

    public boolean setEditItemMenu(JPopupMenu popup) {
        String txt = java.text.MessageFormat.format(rb.getString("EditItem"), rb.getString("SignalHead"));
        popup.add(new AbstractAction(txt) {
                public void actionPerformed(ActionEvent e) {
                    editItem();
                }
            });
        return true;
    }
    
    protected void editItem() {
        makePalettteFrame(java.text.MessageFormat.format(rb.getString("EditItem"), rb.getString("SignalHead")));
        _itemPanel = new SignalHeadItemPanel(_paletteFrame, "SignalHead", getFamily(),
                                       PickListModel.signalHeadPickModelInstance(), _editor);
        ActionListener updateAction = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                updateItem();
                if (ImageIndexEditor.checkImageIndex(_editor)) {
                	ItemPalette.storeIcons();   // write maps to tree
                }
            }
        };
        // _iconMap keys with local names - Let SignalHeadItemPanel figure this out
        _itemPanel.init(updateAction, _iconMap);
        _saveMap = _iconMap;        // setting signalHead creates new map
        _itemPanel.setSelection(getSignalHead());
        _paletteFrame.add(_itemPanel);
        _paletteFrame.pack();
        _paletteFrame.setVisible(true);
    }

    void updateItem() {
        setSignalHead(_itemPanel.getTableSelection().getSystemName());
        setFamily(_itemPanel.getFamilyName());
        Hashtable<String, NamedIcon> map1 = _itemPanel.getIconMap(); 
        boolean scaleRotate = !_itemPanel.isUpdateWithSameMap();
        if (map1!=null) {
            // map1 may be keyed with NamedBean names.  Convert to local name keys.
            // However perhaps keys are local - See above
            Hashtable<String, NamedIcon> map2 = new Hashtable<String, NamedIcon>();
            Iterator<Entry<String, NamedIcon>> it = map1.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, NamedIcon> entry = it.next();
                map2.put(jmri.jmrit.display.palette.ItemPalette.convertText(entry.getKey()), entry.getValue());
            }
            setIcons(map2, scaleRotate);
        }   // otherwise retain current map
        displayState(getSignalHead().getAppearance());
        _paletteFrame.dispose();
        _paletteFrame = null;
        _itemPanel.dispose();
        _itemPanel = null;
        invalidate();
    }

    public boolean setEditIconMenu(JPopupMenu popup) {
        String txt = java.text.MessageFormat.format(rb.getString("EditItem"), rb.getString("SignalHead"));
        popup.add(new AbstractAction(txt) {
                public void actionPerformed(ActionEvent e) {
                    edit();
                }
            });
        return true;
    }

    protected void edit() {
        makeIconEditorFrame(this, "SignalHead", true, null);
        _iconEditor.setPickList(jmri.jmrit.picker.PickListModel.signalHeadPickModelInstance());
        Enumeration <String> e = _iconMap.keys();
        int i=0;
        while (e.hasMoreElements()) {
            String key = e.nextElement();
            _iconEditor.setIcon(i++, key, new NamedIcon(_iconMap.get(key)));
        }
        _saveMap = _iconMap;
        _iconEditor.makeIconPanel(false);

        ActionListener addIconAction = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                updateSignal();
            }
        };
        _iconEditor.complete(addIconAction, true, false, true);
        _iconEditor.setSelection(getSignalHead());
    }

    Hashtable<String, NamedIcon> _saveMap;
    
    /**
     * note this method uses member _saveMap, so either updateSignal() or updateSignal()
     * must be called immediately prior this method. _saveMap is needed to set
     * the same scale and rotation on the new icons that the original icons had.
	*/
    private void setIcons(Hashtable<String, NamedIcon> map, boolean scaleRotate) {
        if (log.isDebugEnabled()) log.debug("setIcons: newmap size= "+map.size()+
                                            ", oldmap size= "+_saveMap.size());
        Iterator<Entry<String, NamedIcon>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, NamedIcon> entry = it.next();
            String name = entry.getKey();
            NamedIcon icon = entry.getValue();
            NamedIcon oldIcon = _saveMap.get(name);
            if (log.isDebugEnabled()) log.debug("key= "+entry.getKey()+", localKey= "+name+
                                                ", newIcon= "+icon+", oldIcon= "+oldIcon);
            if (oldIcon!=null && scaleRotate) {
                icon.setRotation(oldIcon.getRotation(), this);
                icon.rotate(oldIcon.getDegrees(), this);
                icon.scale(oldIcon.getScale(), this);
            }
            _iconMap.put(name, icon);
        }
    }

    void updateSignal() {
        setSignalHead(_iconEditor.getTableSelection().getDisplayName());
        setIcons(_iconEditor.getIconMap(), true);
        displayState(headState());
        _iconEditorFrame.dispose();
        _iconEditorFrame = null;
        _iconEditor = null;
        invalidate();
    }


    /**
     * What to do on click? 0 means 
     * sequence through aspects; 1 means 
     * alternate the "lit" aspect; 2 means
     * alternate the "held" aspect.
     */
    protected int clickMode = 3;
    
    public void setClickMode(int mode) {
        clickMode = mode;
    }
    public int getClickMode() {
        return clickMode;
    }
    
    /**
     * How to handle lit vs not lit?
     * <P>
     * False means ignore (always show R/Y/G/etc appearance on screen);
     * True means show "dark" if lit is set false.
     * <P>
     * Note that setting the appearance "DARK" explicitly
     * will show the dark icon regardless of how this is set.
     */
    protected boolean litMode = false;
    
    public void setLitMode(boolean mode) {
        litMode = mode;
    }
    public boolean getLitMode() {
        return litMode;
    }
    
    /**
     * Change the SignalHead state when the icon is clicked.
     * Note that this change may not be permanent if there is
     * logic controlling the signal head.
     * @param e
     */
    public void doMouseClicked(java.awt.event.MouseEvent e) {
        if (!_editor.getFlag(Editor.OPTION_CONTROLS, isControlling())) return;
        performMouseClicked(e);
    }
    
    /** 
     * This was added in so that the layout editor can handle the mouseclicked when zoomed in
    */
    public void performMouseClicked(java.awt.event.MouseEvent e){
        if (e.isMetaDown() || e.isAltDown() ) return;
        if (getSignalHead()==null) {
            log.error("No turnout connection, can't process click");
            return;
        }
        switch (clickMode) {
            case 0 :
                switch (getSignalHead().getAppearance()) {
                case jmri.SignalHead.RED:
                case jmri.SignalHead.FLASHRED:
                    getSignalHead().setAppearance(jmri.SignalHead.YELLOW);
                    break;
                case jmri.SignalHead.YELLOW:
                case jmri.SignalHead.FLASHYELLOW:
                    getSignalHead().setAppearance(jmri.SignalHead.GREEN);
                    break;
                case jmri.SignalHead.GREEN:
                case jmri.SignalHead.FLASHGREEN:
                    getSignalHead().setAppearance(jmri.SignalHead.RED);
                    break;
                default:
                    getSignalHead().setAppearance(jmri.SignalHead.RED);
                    break;
                }
                return;
            case 1 :
                getSignalHead().setLit(!getSignalHead().getLit());
                return;
            case 2 : 
                getSignalHead().setHeld(!getSignalHead().getHeld());
                return;
            case 3:
                SignalHead sh = getSignalHead();
                int[] states = sh.getValidStates();
                int state = sh.getAppearance();
                for (int i=0; i<states.length; i++) {
//                    if (log.isDebugEnabled()) log.debug("state= "+state+" states["+i+"]= "+states[i]);
                    if (state==states[i]) {
                        i++;
                        if (i>=states.length) {
                            i = 0;
                        }
                        state = states[i];
                        break;
                    }
                }
                sh.setAppearance(state);
                if (log.isDebugEnabled()) log.debug("Set state= "+state);
                return;
            default:
                log.error("Click in mode "+clickMode);
        }
    }

    //private static boolean warned = false;

    public void dispose() {
        if (getSignalHead()!=null){
            getSignalHead().removePropertyChangeListener(this);
        }
        namedHead = null;
        _iconMap = null;
        super.dispose();
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SignalHeadIcon.class.getName());
}
