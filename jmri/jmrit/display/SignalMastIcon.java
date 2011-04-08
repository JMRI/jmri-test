// SignalMastIcon.java

package jmri.jmrit.display;

import jmri.*;

import jmri.jmrit.display.palette.SignalMastItemPanel;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.picker.PickListModel;
import jmri.util.NamedBeanHandle;
import jmri.implementation.DefaultSignalAppearanceMap;

import java.awt.event.*;

import javax.swing.*;
import javax.swing.JOptionPane;

/**
 * An icon to display a status of a SignalMast.
 * <P>
 * For now, this is done via text.
 *
 * @see jmri.SignalMastManager
 * @see jmri.InstanceManager
 * @author Bob Jacobsen Copyright (C) 2009
 * @version $Revision: 1.29 $
 */

public class SignalMastIcon extends PositionableIcon implements java.beans.PropertyChangeListener {

    public SignalMastIcon(Editor editor) {
        // super ctor call to make sure this is an icon label
        super(editor);
        debug = log.isDebugEnabled();
    }
    
    private SignalMast mMast;
    private NamedBeanHandle<SignalMast> namedMast;
    private boolean debug;

    public void setShowAutoText(boolean state) {
        _text = state;
        _icon = !_text;
    }
    
    public Positionable deepClone() {
        SignalMastIcon pos = new SignalMastIcon(_editor);
        return finishClone(pos);
    }

    public Positionable finishClone(Positionable p) {
        SignalMastIcon pos = (SignalMastIcon)p;
        pos.setSignalMast(getPName());        
        pos._iconMap = cloneMap(_iconMap, pos);
        pos.setClickMode(getClickMode());
        pos.setLitMode(getLitMode());
        pos.useIconSet(useIconSet());
        return super.finishClone(pos);
    }

    /**
     * Attached a signalmast element to this display item
     * @param sh Specific SignalMast handle
     */
    public void setSignalMast(NamedBeanHandle<SignalMast> sh) {
        if (mMast != null) {
            mMast.removePropertyChangeListener(this);
        }
        mMast = sh.getBean();
        if (mMast != null) {
            getIcons();
            displayState(mastState());
            mMast.addPropertyChangeListener(this);
            namedMast = sh;
            pName=sh.getName();
        }
    }
    
     /**
     * Taken from the layout editor
     * Attached a numbered element to this display item
     * @param pName Used as a system/user name to lookup the SignalMast object
     */
    public void setSignalMast(String pName) {
        this.pName = pName;
        mMast = InstanceManager.signalMastManagerInstance().provideSignalMast(pName);
        if (mMast == null) log.warn("did not find a SignalMast named "+pName);
        else {
            namedMast = new NamedBeanHandle<SignalMast>(pName, mMast);
            getIcons();
            displayState(mastState());
            mMast.addPropertyChangeListener(this);
        }
    }

    private void getIcons() {
        _iconMap = new java.util.Hashtable<String, NamedIcon>();
        java.util.Enumeration<String> e = mMast.getAppearanceMap().getAspects();
        boolean error = false;
        while (e.hasMoreElements()) {
            String aspect = e.nextElement();
            String s = mMast.getAppearanceMap().getImageLink(aspect, useIconSet);
            if(s.equals("")){
                log.error("No icon found for appearance " + aspect);
                error=true;
            } else {
                s = s.substring(s.indexOf("resources"));
                NamedIcon n = new NamedIcon(s,s);
                _iconMap.put(s, n);
                if(_rotate!=0){
                    n.rotate(_rotate, this);
                }
                if (_scale!=1.0) {
                    n.scale(_scale, this);
                }
            }
        }
        if(error){
            JOptionPane.showMessageDialog(_editor.getTargetFrame(), 
                java.text.MessageFormat.format(rb.getString("SignalMastIconLoadError"),
				new Object[]{mMast.getDisplayName()}), 
                rb.getString("SignalMastIconLoadErrorTitle"), JOptionPane.ERROR_MESSAGE);
        }
    }

    String pName;
    
    public NamedBeanHandle<SignalMast> getNamedSignalMast() {
        return namedMast;
    }

    public SignalMast getSignalMast(){
        if (namedMast==null)
            return null;
        return namedMast.getBean();
    }

    /**
     * Get current appearance of the mast
     * @return An aspect from the SignalMast
     */
    public String mastState() {
        if (mMast==null) return "<empty>";
        else return mMast.getAspect();
    }

    // update icon as state of turnout changes
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (debug) log.debug("property change: "+e.getPropertyName()
                                            +" current state: "+mastState());
        displayState(mastState());
        _editor.getTargetPanel().repaint(); 
    }

    public String getPName() { return pName; }
    
    public String getNameString() {
        String name;
        if (mMast == null) name = rb.getString("NotConnected");
        else if (mMast.getUserName() == null)
            name = mMast.getSystemName();
        else
            name = mMast.getUserName()+" ("+mMast.getSystemName()+")";
        return name;
    }

    /**
     * Pop-up just displays the name
     */
    public boolean showPopUp(JPopupMenu popup) {
        if (isEditable()) {
            
            JMenu clickMenu = new JMenu(rb.getString("WhenClicked"));
            ButtonGroup clickButtonGroup = new ButtonGroup();
            JRadioButtonMenuItem r;
            r = new JRadioButtonMenuItem(rb.getString("ChangeAspect"));
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
            
            java.util.Vector<String> iconTypes = mMast.getAppearanceMap().getImageTypes(mastState());
            if(iconTypes.size()>1){
                JMenu iconSetMenu = new JMenu(rb.getString("SignalMastIconSet"));
                ButtonGroup iconTypeGroup = new ButtonGroup();
                JRadioButtonMenuItem im;
                for(int i = 0; i<iconTypes.size(); i++){
                    final String icon = iconTypes.get(i);
                    im = new JRadioButtonMenuItem(icon);
                    im.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) { useIconSet(icon); }
                    });
                    iconTypeGroup.add(r);
                    if (useIconSet.equals(icon)) im.setSelected(true);
                    else im.setSelected(false);
                    iconSetMenu.add(im);
                }
                popup.add(iconSetMenu);
            }
            //popup.add(new jmri.jmrit.signalling.SignallingSourceAction("Signalling Pairs", mMast));
            JMenu aspect = new JMenu(rb.getString("ChangeAspect"));
            final java.util.Vector <String> aspects = mMast.getValidAspects();
            for (int i=0; i<aspects.size(); i++){
                final int index = i;
                aspect.add(new AbstractAction(aspects.elementAt(index)){
                    public void actionPerformed(ActionEvent e) {
                        mMast.setAspect(aspects.elementAt(index));
                    }
                });
            }
            popup.add(aspect);
        }
        else {
            final java.util.Vector <String> aspects = mMast.getValidAspects();
            for (int i=0; i<aspects.size(); i++){
                final int index = i;
                popup.add(new AbstractAction(aspects.elementAt(index)){
                    public void actionPerformed(ActionEvent e) {
                        mMast.setAspect(aspects.elementAt(index));
                    }
                });
            }
        }
        return true;
    }
    
    public boolean setRotateOrthogonalMenu(JPopupMenu popup){
        return false;
    }

    SignalMastItemPanel _itemPanel;

    public boolean setEditItemMenu(JPopupMenu popup) {
        String txt = java.text.MessageFormat.format(rb.getString("EditItem"), rb.getString("SignalMast"));
        popup.add(new AbstractAction(txt) {
                public void actionPerformed(ActionEvent e) {
                    editItem();
                }
            });
        return true;
    }
    
    protected void editItem() {
        makePalettteFrame(java.text.MessageFormat.format(rb.getString("EditItem"), rb.getString("SignalMast")));
        _itemPanel = new SignalMastItemPanel(_paletteFrame, "SignalMast", getFamily(),
                                       PickListModel.signalMastPickModelInstance(), _editor);
        ActionListener updateAction = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                updateItem();
            }
        };
        // _iconMap keys with local names - Let SignalHeadItemPanel figure this out
        _itemPanel.init(updateAction, _iconMap);
        _itemPanel.setSelection(getSignalMast());
        _paletteFrame.add(_itemPanel);
        _paletteFrame.pack();
        _paletteFrame.setVisible(true);
    }

    void updateItem() {
        setSignalMast(_itemPanel.getTableSelection().getSystemName());
        setFamily(_itemPanel.getFamilyName());
        _paletteFrame.dispose();
        _paletteFrame = null;
        _itemPanel.dispose();
        _itemPanel = null;
        invalidate();
    }

    /**
     * Change the SignalMast aspect when the icon is clicked.
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
        if (getSignalMast()==null) {
            log.error("No turnout connection, can't process click");
            return;
        }
        switch (clickMode) {
            case 0 :
                java.util.Vector <String> aspects = mMast.getValidAspects();
                int idx = aspects.indexOf(mMast.getAspect()) + 1;
                if (idx >= aspects.size()) {
                    idx = 0;
                }
                mMast.setAspect(aspects.elementAt(idx));
                return;
            case 1 :
                getSignalMast().setLit(!getSignalMast().getLit());
                return;
            case 2 : 
                getSignalMast().setHeld(!getSignalMast().getHeld());
                return;
            default:
                log.error("Click in mode "+clickMode);
        }
    }
    
    String useIconSet = "default";
    
    public void useIconSet(String icon){
        if(useIconSet.equals(icon)){
            return;
        }
        if (useIconSet==null){
            icon = "default";
        }
        //clear the old icon map out.
        _iconMap=null;
        useIconSet = icon;
        getIcons();
        displayState(mastState());
        _editor.getTargetPanel().repaint(); 
    }
    
    public String useIconSet() { return useIconSet; }
    
    /**
     * Drive the current state of the display from the state of the
     * underlying SignalMast object.
     */
    public void displayState(String state) {
        updateSize();
        if (debug) {
            if (mMast == null) {
                log.debug("Display state "+state+", disconnected");
            } else {
                log.debug("Display state "+state+" for "+mMast.getSystemName());
            }
        }
        if (isText()){
            if (mMast.getHeld()) {
                if (isText()) super.setText(rb.getString("Held"));
                return;
            }
            else if (getLitMode() && !getSignalMast().getLit()){
                super.setText(rb.getString("Dark"));
                return;
            }
            super.setText(state);
        }
        if (isIcon()) {
            if (state !=null ) {
                String s = mMast.getAppearanceMap().getImageLink(state, useIconSet);
                if ((mMast.getHeld()) && (mMast.getSpecificAppearance(DefaultSignalAppearanceMap.HELD)!=null)) {
                    s = mMast.getAppearanceMap().getImageLink(mMast.getSpecificAppearance(DefaultSignalAppearanceMap.HELD), useIconSet);
                }
                else if((mMast.getLit()) && (mMast.getSpecificAppearance(DefaultSignalAppearanceMap.DARK)!=null)) {
                    s = mMast.getAppearanceMap().getImageLink(mMast.getSpecificAppearance(DefaultSignalAppearanceMap.DARK), useIconSet);
                }
                s = s.substring(s.indexOf("resources"));
                
                // tiny global cache, due to number of icons
                if (_iconMap==null) getIcons();
                NamedIcon n = _iconMap.get(s);
                super.setIcon(n);
                updateSize();
                setSize(n.getIconWidth(), n.getIconHeight());
            }
        } else {
            super.setIcon(null);
        }
        
        return;
    }
    
    public boolean setEditIconMenu(JPopupMenu popup) {
        return false;
    }
    
    protected void rotateOrthogonal() {
        super.rotateOrthogonal();
        // bug fix, must repaint icons that have same width and height
        displayState(mastState());
        repaint();
    }

    public void rotate(int deg){
        super.rotate(deg);
        displayState(mastState());
    }
    
    public void setScale(double s) {
        super.setScale(s);
        displayState(mastState());
    }
    
    /**
     * What to do on click? 0 means 
     * sequence through aspects; 1 means 
     * alternate the "lit" aspect; 2 means
     * alternate the DefaultSignalAppearanceMap.HELD aspect.
     */
    protected int clickMode = 0;
    
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
     * True means show DefaultSignalAppearanceMap.DARK if lit is set false.
     * <P>
     * Note that setting the appearance "DefaultSignalAppearanceMap.DARK" explicitly
     * will show the dark icon regardless of how this is set.
     */
    protected boolean litMode = false;
    
    public void setLitMode(boolean mode) {
        litMode = mode;
    }
    public boolean getLitMode() {
        return litMode;
    }

    public void dispose() {
        mMast.removePropertyChangeListener(this);        
        super.dispose();
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SignalMastIcon.class.getName());
}
