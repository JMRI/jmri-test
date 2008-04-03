package jmri.jmrit.display;

import jmri.jmrit.catalog.NamedIcon;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;

/**
 * PositionableLabel is a JLabel that can be dragged around the
 * inside of the enclosing Container using a right-drag.
 * <P>
 * The positionable parameter is a global, set from outside.
 * The 'fixed' parameter is local, set from the popup here.
 *
 * @author Bob Jacobsen Copyright (c) 2002
 * @version $Revision: 1.37 $
 */

public class PositionableLabel extends JLabel
                        implements MouseMotionListener, MouseListener,
                                    Positionable {

    public PositionableLabel(String s) {
        super(s);
        text = true;
        debug = log.isDebugEnabled();
        setProperToolTip();
        connect();
    }
    public PositionableLabel(NamedIcon s) {
        super(s);
        icon = true;
        namedIcon = s;
        updateSize();
        debug = log.isDebugEnabled();
        setProperToolTip();
        connect();
    }

    public void setProperToolTip() {
        setToolTipText("Alt-click to see menu, drag with meta key to move");
    }

    public boolean isIcon() { return icon; }
    boolean icon = false;
    public boolean isText() { return text; }
    boolean text = false;

    NamedIcon namedIcon = null;

    /**
     * Connect listeners
     */
    void connect() {
        addMouseMotionListener(this);
        addMouseListener(this);
    }

    /**
     * Update the AWT and Swing size information due to change in internal
     * state, e.g. if one or more of the icons that might be displayed
     * is changed
     */
    protected void updateSize(){
        setSize(maxWidth(), maxHeight());
    }

    protected int maxWidth(){
        return namedIcon.getIconWidth();
    }
    protected int maxHeight(){
        return namedIcon.getIconHeight();
    }

    private Integer displayLevel;
    public void setDisplayLevel(Integer l) { displayLevel = l; }
    public void setDisplayLevel(int l) { setDisplayLevel(new Integer(l)); }
    public Integer getDisplayLevel() { return displayLevel; }

    // cursor location reference for this move (relative to object)
    int xClick = 0;
    int yClick = 0;

    public void mousePressed(MouseEvent e) {
        // remember where we are
        xClick = e.getX();
        yClick = e.getY();
        // if (debug) log.debug("Pressed: "+where(e));
        if (e.isPopupTrigger()) {
            if (debug) log.debug("show popup");
            showPopUp(e);
        }
    }

    public void mouseReleased(MouseEvent e) {
        // if (debug) log.debug("Release: "+where(e));
        if (e.isPopupTrigger()) {
            if (debug) log.debug("show popup");
            showPopUp(e);
        }
    }

    public void mouseClicked(MouseEvent e) {
        if (debug) log.debug("Clicked: "+where(e));
        if (debug && e.isMetaDown()) log.debug("meta down");
        if (debug && e.isAltDown()) log.debug(" alt down");
        if (e.isPopupTrigger()) {
            if (debug) log.debug("show popup");
            showPopUp(e);
        }
    }
    public void mouseExited(MouseEvent e) {
        // if (debug) log.debug("Exited:  "+where(e));
    }
    public void mouseEntered(MouseEvent e) {
        // if (debug) log.debug("Entered: "+where(e));
    }

    public void mouseMoved(MouseEvent e) {
        //if (debug) log.debug("Moved:   "+where(e));
    }
    public void mouseDragged(MouseEvent e) {
        if (e.isMetaDown()) {
            if (!getPositionable() || getFixed()) return;
            // update object postion by how far dragged
            int xObj = getX()+(e.getX()-xClick);
            int yObj = getY()+(e.getY()-yClick);
            // don't allow negative placement, icon can become unreachable
            if (xObj < 0) xObj = 0;
            if (yObj < 0) yObj = 0;
            this.setLocation(xObj, yObj);
            // and show!
            this.repaint();
        }
    }

    JPopupMenu popup = null;
    JLabel ours;
    /**
     * For over-riding in the using classes: only provides icon rotation
     */
    protected void showPopUp(MouseEvent e) {
        if (!getEditable()) return;
        ours = this;
        if (icon) {
            popup = new JPopupMenu();
            
            // add x and y coordinates
			if (getViewCoordinates()) {
				popup.add("x= " + this.getX());
				popup.add("y= " + this.getY());
				popup.add(new AbstractAction("Set x & y") {
	                public void actionPerformed(ActionEvent e) {
	                	String name = getText();
	                	displayCoordinateEdit(name);
	                }
				});
				
			}
             
            popup.add(new AbstractAction("Rotate") {
                public void actionPerformed(ActionEvent e) {
                    namedIcon.setRotation(namedIcon.getRotation()+1, ours);
                    updateSize();
                    setIcon(namedIcon);
                }
            });
            
            addFixedItem(popup);
            addShowTooltipItem(popup);
            
            popup.add(new AbstractAction("Remove") {
                public void actionPerformed(ActionEvent e) {
                    remove();
                    dispose();
                }
            });
        } else if (text) {
            popup = new JPopupMenu();
            
            // add x and y coordinates
			if (getViewCoordinates()) {
				popup.add("x= " + this.getX());
				popup.add("y= " + this.getY());
				popup.add(new AbstractAction("Set x & y") {
	                public void actionPerformed(ActionEvent e) {
	                	String name = getText();
	                	displayCoordinateEdit(name);
	                }
				});
			}
            popup.add(makeFontSizeMenu());

            popup.add(makeFontStyleMenu());

            popup.add(makeFontColorMenu());

            addFixedItem(popup);
            addShowTooltipItem(popup);
            
            popup.add(new AbstractAction("Remove") {
                public void actionPerformed(ActionEvent e) {
                    remove();
                    dispose();
                }
            });

        } else if (!text && !icon)
            log.warn("showPopUp when neither text nor icon true");
        // show the result
        if (popup != null) popup.show(e.getComponent(), e.getX(), e.getY());
    }

    JMenu makeFontSizeMenu() {
        JMenu sizeMenu = new JMenu("Font size");
        fontButtonGroup = new ButtonGroup();
        addFontMenuEntry(sizeMenu, 6);
        addFontMenuEntry(sizeMenu, 8);
        addFontMenuEntry(sizeMenu, 10);
        addFontMenuEntry(sizeMenu, 11);
        addFontMenuEntry(sizeMenu, 12);
        addFontMenuEntry(sizeMenu, 14);
        addFontMenuEntry(sizeMenu, 16);
        addFontMenuEntry(sizeMenu, 20);
        addFontMenuEntry(sizeMenu, 24);
        addFontMenuEntry(sizeMenu, 28);
        addFontMenuEntry(sizeMenu, 32);
        addFontMenuEntry(sizeMenu, 36);
        return sizeMenu;
    }
    
    void addFontMenuEntry(JMenu menu, final int size) {
        JRadioButtonMenuItem r = new JRadioButtonMenuItem(""+size);
        r.addActionListener(new ActionListener() {
            final float desiredSize = size+0.f;
            public void actionPerformed(ActionEvent e) { setFontSize(desiredSize); }
        });
        fontButtonGroup.add(r);
        if (getFont().getSize() == size) r.setSelected(true);
        else r.setSelected(false);
        menu.add(r);
    }

    public void setFontSize(float newSize) {
        setFont(jmri.util.FontUtil.deriveFont(getFont(), newSize));
        setSize(getPreferredSize().width, getPreferredSize().height);
    }

    JMenu makeFontStyleMenu() {
        JMenu styleMenu = new JMenu("Font style");
        styleMenu.add(italic = newStyleMenuItem(new AbstractAction("Italic") {
            public void actionPerformed(ActionEvent e) {
                if (log.isDebugEnabled())
                    log.debug("When style item selected "+((String)getValue(NAME))
                                +" italic state is "+italic.isSelected());
                if (italic.isSelected()) setFontStyle(Font.ITALIC, 0);
                else setFontStyle(0, Font.ITALIC);
            }
          }, Font.ITALIC));

        styleMenu.add(bold = newStyleMenuItem(new AbstractAction("Bold") {
            public void actionPerformed(ActionEvent e) {
                if (log.isDebugEnabled())
                    log.debug("When style item selected "+((String)getValue(NAME))
                                +" bold state is "+bold.isSelected());
                if (bold.isSelected()) setFontStyle(Font.BOLD, 0);
                else setFontStyle(0, Font.BOLD);
            }
          }, Font.BOLD));
         return styleMenu;     
    }
    
    public void displayCoordinateEdit(String name) {
		if (log.isDebugEnabled())
			log.debug("make new coordinate menu");
		CoordinateEdit f = new CoordinateEdit();
		f.addHelpMenu("package.jmri.jmrit.display.CoordinateEdit", true);
		try {
			f.initComponents(this, name);
			}
		catch (Exception ex) {
			log.error("Exception: "+ex.toString());
			}
		f.setVisible(true);	
	}
 
    
    JMenu makeFontColorMenu() {
        JMenu colorMenu = new JMenu("Font color");
        colorButtonGroup = new ButtonGroup();
        addColorMenuEntry(colorMenu, "Black", Color.black);
        addColorMenuEntry(colorMenu, "Dark Gray",Color.darkGray);
        addColorMenuEntry(colorMenu, "Gray",Color.gray);
        addColorMenuEntry(colorMenu, "Light Gray",Color.lightGray);
        addColorMenuEntry(colorMenu, "White",Color.white);
        addColorMenuEntry(colorMenu, "Red",Color.red);
        addColorMenuEntry(colorMenu, "Orange",Color.orange);
        addColorMenuEntry(colorMenu, "Yellow",Color.yellow);
        addColorMenuEntry(colorMenu, "Green",Color.green);
        addColorMenuEntry(colorMenu, "Blue",Color.blue);
        addColorMenuEntry(colorMenu, "Magenta",Color.magenta);
        return colorMenu;
    }
        
    void addColorMenuEntry(JMenu menu, final String name, final Color color) {
        ActionListener a = new ActionListener() {
            final String desiredName = name;
            final Color desiredColor = color;
            public void actionPerformed(ActionEvent e) { setForeground(desiredColor); }
        };
        JRadioButtonMenuItem r = new JRadioButtonMenuItem(name);
        r.addActionListener(a);
        colorButtonGroup.add(r);
        if (getForeground().getRGB() == color.getRGB())  r.setSelected(true);
        else r.setSelected(false);
        menu.add(r);
    }

    JCheckBoxMenuItem showTooltipItem = null;
    void addShowTooltipItem(JPopupMenu popup) {
        showTooltipItem = new JCheckBoxMenuItem("Tooltip");
        showTooltipItem.setSelected(getShowTooltip());
        popup.add(showTooltipItem);
        showTooltipItem.addActionListener(new ActionListener(){
            public void actionPerformed(java.awt.event.ActionEvent e) {
                setShowTooltip(showTooltipItem.isSelected());
            }
        });
    }
        
    JCheckBoxMenuItem showFixedItem = null;
    void addFixedItem(JPopupMenu popup) {
        showFixedItem = new JCheckBoxMenuItem("Fixed");
        showFixedItem.setSelected(getFixed());
        popup.add(showFixedItem);
        showFixedItem.addActionListener(new ActionListener(){
            public void actionPerformed(java.awt.event.ActionEvent e) {
                setFixed(showFixedItem.isSelected());
            }
        });
    }
        
    JCheckBoxMenuItem disableItem = null;
    void addDisableMenuEntry(JPopupMenu popup) {
        disableItem = new JCheckBoxMenuItem("Disable");
        disableItem.setSelected(getForceControlOff());
        popup.add(disableItem);
        disableItem.addActionListener(new ActionListener(){
            public void actionPerformed(java.awt.event.ActionEvent e) {
                setForceControlOff(disableItem.isSelected());
            }
        });
    }
    
    JCheckBoxMenuItem tristateItem = null;
    void addTristateEntry(JPopupMenu popup) {
    	tristateItem = new JCheckBoxMenuItem("Tristate");
    	tristateItem.setSelected(getTristate());
        popup.add(tristateItem);
        tristateItem.addActionListener(new ActionListener(){
            public void actionPerformed(java.awt.event.ActionEvent e) {
                setTristate(tristateItem.isSelected());
            }
        });
    }
        
    public JMenuItem newStyleMenuItem(AbstractAction a, int mask) {
        // next two lines needed because JCheckBoxMenuItem(AbstractAction) not in 1.1.8
        JCheckBoxMenuItem c = new JCheckBoxMenuItem((String)a.getValue(AbstractAction.NAME));
        c.addActionListener(a);
        if (log.isDebugEnabled()) log.debug("When creating style item "+((String)a.getValue(AbstractAction.NAME))
                                            +" mask was "+mask+" state was "+getFont().getStyle());
        if ( (mask & getFont().getStyle()) == mask ) c.setSelected(true);
        return c;
    }

    JMenuItem italic = null;
    JMenuItem bold = null;
    ButtonGroup fontButtonGroup = null;
    ButtonGroup colorButtonGroup = null;
 
    public void setFontStyle(int addStyle, int dropStyle) {
        int styleValue = (getFont().getStyle() & ~dropStyle) | addStyle;
        if (bold != null) bold.setSelected( (styleValue & Font.BOLD) != 0);
        if (italic != null) italic.setSelected( (styleValue & Font.ITALIC) != 0);
        setFont(jmri.util.FontUtil.deriveFont(getFont(),styleValue));

        setSize(getPreferredSize().width, getPreferredSize().height);
    }

    String where(MouseEvent e) {
        return ""+e.getX()+","+e.getY();
    }

    boolean debug = false;

    public void setPositionable(boolean enabled) { positionable = enabled; }
    public boolean getPositionable() { return positionable; }
    private boolean positionable = true;
    
    public void setViewCoordinates(boolean enabled) { viewCoordinates = enabled; }
    public boolean getViewCoordinates() { return viewCoordinates; }
    private boolean viewCoordinates = false;

    public void setEditable(boolean enabled) {editable = enabled;}
    public boolean getEditable() { return editable; }
    private boolean editable = true;

    public void setFixed(boolean enabled) {
        fixed = enabled;
        if (showFixedItem!=null) showFixedItem.setSelected(getFixed());
    }
    public boolean getFixed() { return fixed; }
    private boolean fixed = false;

    public void setControlling(boolean enabled) {controlling = enabled;}
    public boolean getControlling() { return controlling; }
    private boolean controlling = true;

    public void setForceControlOff(boolean set) {
        forceControlOff = set;
        if (disableItem!=null) disableItem.setSelected(getForceControlOff());
    }
    public boolean getForceControlOff() { return forceControlOff; }
    private boolean forceControlOff = false;
    
    public void setTristate(boolean set) {
    	tristate = set;
    }
    
    public boolean getTristate() { return tristate; }
    private boolean tristate = false;

    public void setShowTooltip(boolean set) {
        if (set)
            setProperToolTip();
        else
            setToolTipText(null);
        showTooltip = set;
        if (showTooltipItem!=null) showTooltipItem.setSelected(getShowTooltip());
    }
    public boolean getShowTooltip() { return showTooltip; }
    private boolean showTooltip = true;

    /**
     * Clean up when this object is no longer needed.  Should not
     * be called while the object is still displayed; see remove()
     */
    void dispose() {
        if (popup != null) popup.removeAll();
        fontButtonGroup = null;
        colorButtonGroup = null;
        popup = null;
        italic = null;
        bold = null;
        ours = null;
    }

    /**
     * Removes this object from display and persistance
     */
    void remove() {
        Point p = this.getLocation();
        int w = this.getWidth();
        int h = this.getHeight();
        Container parent = this.getParent();
        parent.remove(this);
        // force redisplay
        parent.validate();
        parent.repaint(p.x,p.y,w,h);

        // remove from persistance by flagging inactive
        active = false;
    }

    boolean active = true;
    /**
     * "active" means that the object is still displayed, and should be stored.
     */
    public boolean isActive() {
        return active;
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PositionableLabel.class.getName());

}
