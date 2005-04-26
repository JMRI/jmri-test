// VariableValue.java

package jmri.jmrit.symbolicprog;

import jmri.Programmer;
import jmri.InstanceManager;
import jmri.ProgListener;

import java.util.Vector;
import java.awt.Component;
import javax.swing.*;
import java.awt.Color;

/**
 * Represents a single Variable value; abstract base class.
 *
 * <p>The "dirty" parameter is used to indicate whether this VariableValue object
 * needs to be read or written.  It is set at the beginning of a mass read/write
 * operation, and reset when this object is explicitly read or written.  Most 
 * importantly, it is also reset when the underlying storage (CVs, etc) are
 * read or written successfully.  This allows combining multiple operations into one.
 *
 * @author	Bob Jacobsen   Copyright (C) 2001, 2002, 2003, 2004
 * @version     $Revision: 1.17 $
 *
 */
public abstract class VariableValue extends AbstractValue implements java.beans.PropertyChangeListener {

    // The actual stored value is internal, not showing in the interface.
    // Instead, you can get a (Object) representation for display in
    // a table, etc. Modification of the state of that object then
    // gets reflected back, causing the underlying CV objects to change.
    abstract public Component getValue();	// this one is returning a common value
                                                // and thus can be called without limit
    abstract public Component getRep(String format); // this one is returning a new object
    // and thus should be called a limited number of times

    /**
     * @return String that can be interpreted as an integer
     */
    abstract public String getValueString();
    /**
     * @return User-desired value, which may or may not be an integer
     */
    public String getTextValue() { return getValueString(); }

    abstract public void setIntValue(int i);

    /**
     * Always read the contents of this Variable
     */
    abstract public void readAll();
    /**
     * Always write the contents of this Variable
     */
    abstract public void writeAll();
    
    /**
     * Read the contents of this Variable if it's in a state
     * that indicates it was "changed"
     * @see #isChanged
     */
    abstract public void readChanges();

    /**
     * Write the contents of this Variable if it's in a state
     * that indicates it was "changed"
     * @see #isChanged
     */
    abstract public void writeChanges();

    /**
     * Determine whether this Variable is "changed", so that
     * "read changes" and "write changes" will act on it.
     * @see #considerChanged
     */
    abstract public boolean isChanged();

    /**
     * Default implementation for subclasses to tell if a CV meets a common definition
     * of "changed".  This implementation will only 
     * consider a variable to be changed if the underlying CV(s) state is
     * EDITTED, e.g. if the CV(s) has been manually editted.
     * @param c CV to be examined
     * @return true if to be considered changed
     */
    static public boolean considerChanged(CvValue c) {
        return c.getState() == CvValue.EDITED;
    }

    // handle incoming parameter notification
    abstract public void propertyChange(java.beans.PropertyChangeEvent e);
    abstract public void dispose();

    abstract public Object rangeVal();

    // methods implemented here:
    public VariableValue(String label, String comment, boolean readOnly,
                         int cvNum, String mask, Vector v, JLabel status, String item) {
        _label = label;
        _comment = comment;
        _readOnly = readOnly;
        _cvNum = cvNum;
        _mask = mask;
        _cvVector = v;
        _status = status;
        _item = item;
    }

    /**
     * Create a null object.  Normally only used for tests and to pre-load classes.
     */
    protected VariableValue() {}


    final public boolean isDirty() {return _dirty;}
    private boolean _dirty = false;
    final public void setDirty(boolean d) { 
        if (log.isDebugEnabled()) log.debug("Variable "+_label+"sets dirty "+_dirty);
        _dirty = d;
    }
    
    // common information - none of these are bound
    public String label() { return _label; }
    public String item() { return _item; }
    private String _label;
    private String _item;
    protected Vector _cvVector;   // Vector of CV objects used to look up CVs
    protected JLabel _status = null;

    protected String _tooltipText = null;
    /**
     * Set tooltip text to be used by both the "value" and
     * representations of this Variable.
     * <P>This is expected to be overridden in subclasses to
     * change their internal info.
     * @see #updateRepresentation
     * @param t
     */
    public void setTooltipText(String t) {
        _tooltipText = t;
    }
    /**
     * Add the proper tooltip text to a graphical rep
     * before returning it
     * @param c
     */
    protected JComponent updateRepresentation(JComponent c) {
        c.setToolTipText(_tooltipText);
        return c;
    }

    public String getComment() { return _comment; }
    private String _comment;

    public boolean getReadOnly() { return _readOnly; }
    private boolean _readOnly;

    public int getCvNum() { return _cvNum; }
    private int _cvNum;

    public String getMask() { return _mask; }
    private String _mask;

    public int getState()  { return _state; }
    public void setState(int state) {
        switch (state) {
        case UNKNOWN : setColor(COLOR_UNKNOWN ); break;
        case EDITED : setColor(COLOR_EDITED ); break;
        case READ    : setColor(COLOR_READ    ); break;
        case STORED  : setColor(COLOR_STORED  ); break;
        case FROMFILE: setColor(COLOR_FROMFILE); break;
        default:      log.error("Inconsistent state: "+_state);
        }
        if (_state != state || _state == UNKNOWN) prop.firePropertyChange("State", new Integer(_state), new Integer(state));
        _state = state;
    }
    private int _state = UNKNOWN;

    /**
     * Propogate a state change here to the CVs that are related, which will
     * in turn propagate back to here
     */
    abstract public void setCvState(int state);

    /**
     *  A variable is busy during read, write operations
     */
    public boolean isBusy() { return _busy; }
    protected void setBusy(boolean newBusy) {
        boolean oldBusy = _busy;
        _busy = newBusy;
        if (newBusy != oldBusy) prop.firePropertyChange("Busy", new Boolean(oldBusy), new Boolean(newBusy));
    }
    private boolean _busy = false;

    // handle outgoing parameter notification
    java.beans.PropertyChangeSupport prop = new java.beans.PropertyChangeSupport(this);
    public void removePropertyChangeListener(java.beans.PropertyChangeListener p) { prop.removePropertyChangeListener(p); }
    public void addPropertyChangeListener(java.beans.PropertyChangeListener p) { prop.addPropertyChangeListener(p); }

    // tool to handle masking, updating
    protected int maskVal(String maskString) {
        // convert String mask to int
        int mask = 0;
        for (int i=0; i<8; i++) {
            mask = mask << 1;
            if (maskString.charAt(i) == 'V') {
                mask = mask+1;
            }
        }
        return mask;
    }

    /**
     * Find number of places to shift a value left to align
     * if with a mask.  For example, as mask of "XXVVVXXX"
     * means that the value 5 needs to be shifted left 3 places
     * before being masked and stored as XX101XXX
     */
    protected int offsetVal(String maskString) {
        // convert String mask to int
        int offset = 0;
        for (int i=0; i<8; i++) {
            if (maskString.charAt(i) == 'V') {
                offset = 7-i;  // number of places to shift left
            }
        }
        return offset;
    }

    /**
     *
     * @param oldCv Value of the CV before this update is applied
     * @param newVal Value for this variable (e.g. not the CV value)
     * @param maskString The bit mask for this variable in character form
     * @return int new value for the CV
     */
    protected int newValue(int oldCv, int newVal, String maskString) {
        int mask = maskVal(maskString);
        int offset = offsetVal(maskString);
        return (oldCv & ~mask) + ((newVal << offset) & mask);
    }

    /** 
     * Provide access to CVs referenced by this operation
     */
    abstract public CvValue[] usesCVs();
    
    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(VariableValue.class.getName());

}
