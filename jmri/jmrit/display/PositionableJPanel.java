// PositionableJPanel.java

package jmri.jmrit.display;

import java.util.ResourceBundle;


import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.border.LineBorder;

import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;
import java.awt.Container;
import java.awt.event.ActionEvent;
import javax.swing.*;

/**
 * <p> </p>
 *
 * @author  Bob Jacobsen copyright (C) 2009
 * @version $Revision: 1.26 $
 */
public class PositionableJPanel extends JPanel implements Positionable, MouseListener, MouseMotionListener {

    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.DisplayBundle");

   	protected Editor _editor = null;
    protected boolean debug = false;

    private ToolTip _tooltip;
    private boolean _showTooltip =true;
    private boolean _editable = true;
    private boolean _positionable = true;
    private boolean _viewCoordinates = false;
    private boolean _controlling = true;
    private boolean _hidden = false;
	private int _displayLevel;
    private double _scale;         // user's scaling factor

    JMenuItem lock = null;
    JCheckBoxMenuItem showTooltipItem = null;
    
    public PositionableJPanel(Editor editor) {
        _editor = editor;
        _scale = 1.0;
        debug = log.isDebugEnabled();
    }

    public void setPositionable(boolean enabled) {_positionable = enabled;}
    public boolean isPositionable() { return _positionable; }

    public void setEditable(boolean enabled) {_editable = enabled;}
    public boolean isEditable() { return _editable; }
     
    public void setViewCoordinates(boolean enabled) { _viewCoordinates = enabled; }
    public boolean getViewCoordinates() { return _viewCoordinates; }

    public void setControlling(boolean enabled) {_controlling = enabled;}
    public boolean isControlling() { return _controlling; }

    public void setHidden(boolean hide) {_hidden = hide; }
    public boolean isHidden() { return _hidden;  }
    public void showHidden() {
        if(!_hidden || _editor.isEditable()) {
            setVisible(true);
        } else {
            setVisible(false);
        }
    }

    public void setDisplayLevel(int l) {
    	int oldDisplayLevel = _displayLevel;
    	_displayLevel = l;
    	if (oldDisplayLevel!=l){
    		log.debug("Changing label display level from "+oldDisplayLevel+" to "+_displayLevel);
    		_editor.displayLevelChange(this);
    	}
    }
    public int getDisplayLevel() { return _displayLevel; }
    
    public void setShowTooltip(boolean set) {
        _showTooltip = set;
    }
    public boolean showTooltip() {
        return _showTooltip;
    }
    public void setTooltip(ToolTip tip) {
        _tooltip = tip;
    }
    public ToolTip getTooltip() {
        return _tooltip;
    }
    public void setScale(double s) {
        _scale = s;
    }
    public double getScale() {
        return _scale;
    }

    public String getNameString() {
        return getName();
    }

    public Editor getEditor(){
        return _editor;
    }
    
    // overide where used - e.g. momentary
    public void doMousePressed(MouseEvent event) {}
    public void doMouseReleased(MouseEvent event) {}
    public void doMouseClicked(MouseEvent event) {}
    public void doMouseDragged(MouseEvent event) {}
    public void doMouseMoved(MouseEvent event) {}
    public void doMouseEntered(MouseEvent event) {}
    public void doMouseExited(MouseEvent event) {}

    public boolean storeItem() {
        return true;
    }
    public boolean doPopupMenu() {
        return true;
    }
    /**
     * For over-riding in the using classes: add item specific menu choices
     */
    public boolean setRotateOrthogonalMenu(JPopupMenu popup){
        return false;
    }
    public boolean setRotateMenu(JPopupMenu popup){
        return false;
    }
    public boolean setScaleMenu(JPopupMenu popup){
        return false;
    }
    public boolean setDisableControlMenu(JPopupMenu popup) {
        return false;
    }
    public boolean setTextEditMenu(JPopupMenu popup) {
        return false;
    }
    public boolean showPopUp(JPopupMenu popup) {
        return false;
    }

    JFrame _iconEditorFrame;
    IconAdder _iconEditor;
    public boolean setEditIconMenu(JPopupMenu popup) {
        popup.add(new AbstractAction(rb.getString("EditIcon")) {
                public void actionPerformed(ActionEvent e) {
                    edit();
                }
            });
        return true;
    }

    /**
    *  Utility
    */
    protected boolean showIconEditorFrame(Container pos) {
        if (_iconEditorFrame != null) {
            _iconEditorFrame.setLocationRelativeTo(pos);
            _iconEditorFrame.toFront();
            _iconEditorFrame.setVisible(true);
            return true;
        }
        return false;
    }
    void edit() {
    }

    /**************** end Positionable methods **********************/

    /**
     * Removes this object from display and persistance
     */
    public void remove() {
		_editor.removeFromContents(this);
        cleanup();
        // remove from persistance by flagging inactive
        active = false;
    }

    /**
     * To be overridden if any special work needs to be done
     */
    void cleanup() {}

    boolean active = true;
    /**
     * "active" means that the object is still displayed, and should be stored.
     */
    public boolean isActive() {
        return active;
    }
    public void mousePressed(MouseEvent e) {
        _editor.mousePressed(new MouseEvent(this, e.getID(), e.getWhen(), e.getModifiersEx(), 
                                             e.getX()+this.getX(), e.getY()+this.getY(), 
                                             e.getClickCount(), e.isPopupTrigger())); 
    }

    public void mouseReleased(MouseEvent e) {
        _editor.mouseReleased(new MouseEvent(this, e.getID(), e.getWhen(), e.getModifiersEx(), 
                                             e.getX()+this.getX(), e.getY()+this.getY(), 
                                             e.getClickCount(), e.isPopupTrigger())); 
    }

    public void mouseClicked(MouseEvent e) {
        _editor.mouseClicked(new MouseEvent(this, e.getID(), e.getWhen(), e.getModifiersEx(), 
                                             e.getX()+this.getX(), e.getY()+this.getY(), 
                                             e.getClickCount(), e.isPopupTrigger())); 
    }
    public void mouseExited(MouseEvent e) {
        _editor.mouseExited(new MouseEvent(this, e.getID(), e.getWhen(), e.getModifiersEx(), 
                                             e.getX()+this.getX(), e.getY()+this.getY(), 
                                             e.getClickCount(), e.isPopupTrigger())); 
    }
    public void mouseEntered(MouseEvent e) {
        _editor.mouseEntered(new MouseEvent(this, e.getID(), e.getWhen(), e.getModifiersEx(), 
                                             e.getX()+this.getX(), e.getY()+this.getY(), 
                                             e.getClickCount(), e.isPopupTrigger())); 
    }

    public void mouseMoved(MouseEvent e) {
        _editor.mouseMoved(new MouseEvent(this, e.getID(), e.getWhen(), e.getModifiersEx(), 
                                             e.getX()+this.getX(), e.getY()+this.getY(), 
                                             e.getClickCount(), e.isPopupTrigger())); 
    }
    public void mouseDragged(MouseEvent e) {
        _editor.mouseDragged(new MouseEvent(this, e.getID(), e.getWhen(), e.getModifiersEx(), 
                                             e.getX()+this.getX(), e.getY()+this.getY(), 
                                             e.getClickCount(), e.isPopupTrigger())); 
    }

    /***************************************************************/

    PositionablePopupUtil _popupUtil;
    protected void setPopupUtility(PositionablePopupUtil tu) {
        _popupUtil = tu;
    }
    public PositionablePopupUtil getPopupUtility() {
        return _popupUtil;
    }

    /**
     * Update the AWT and Swing size information due to change in internal
     * state, e.g. if one or more of the icons that might be displayed
     * is changed
     */
    public void updateSize() {
        invalidate();
        setSize(maxWidth(), maxHeight());
        if (debug) {
//            javax.swing.JTextField text = (javax.swing.JTextField)_popupUtil._textComponent;
            log.debug("updateSize: "+_popupUtil.toString()+
                      ", text: w="+getFontMetrics(_popupUtil.getFont()).stringWidth(_popupUtil.getText())+
                      "h="+getFontMetrics(_popupUtil.getFont()).getHeight());
        }
        validate();
        repaint();
    }    
    
    public int maxWidth() {
        int max = 0;
        if (_popupUtil!=null) {
            if (_popupUtil.getFixedWidth()!=0) {
                max = _popupUtil.getFixedWidth();
                max += _popupUtil.getMargin()*2;
                if (max < PositionablePopupUtil.MIN_SIZE) {  // don't let item disappear
                    _popupUtil.setFixedWidth(PositionablePopupUtil.MIN_SIZE);
                    max = PositionablePopupUtil.MIN_SIZE;
                }
            } else {
                max = getPreferredSize().width;
                /*
                if(_popupUtil._textComponent instanceof javax.swing.JTextField) {
                    javax.swing.JTextField text = (javax.swing.JTextField)_popupUtil._textComponent;
                    max = getFontMetrics(text.getFont()).stringWidth(text.getText());
                } */
                max += _popupUtil.getMargin()*2;
                if (max < PositionablePopupUtil.MIN_SIZE) {  // don't let item disappear
                    max = PositionablePopupUtil.MIN_SIZE;
                }
            }
        }
        if (debug) log.debug("maxWidth= "+max+" preferred width= "+getPreferredSize().width);
        return max;
    }

    public int maxHeight() {
        int max = 0;
        if (_popupUtil!=null) {
            if (_popupUtil.getFixedHeight()!=0) {
                max = _popupUtil.getFixedHeight();
                max += _popupUtil.getMargin()*2;
                if (max < PositionablePopupUtil.MIN_SIZE) {   // don't let item disappear
                    _popupUtil.setFixedHeight(PositionablePopupUtil.MIN_SIZE);
                    max = PositionablePopupUtil.MIN_SIZE;
                }
            } else {
                max = getPreferredSize().height;
                /*
                if(_popupUtil._textComponent!=null) {
                    max = getFontMetrics(_popupUtil._textComponent.getFont()).getHeight();
                }  */
                if (_popupUtil!=null) {
                    max += _popupUtil.getMargin()*2;
                }
                if (max < PositionablePopupUtil.MIN_SIZE) {  // don't let item disappear
                    max = PositionablePopupUtil.MIN_SIZE;
                }
            }
        }
        if (debug) log.debug("maxHeight= "+max+" preferred height= "+getPreferredSize().height);
        return max;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PositionableJPanel.class.getName());
}
