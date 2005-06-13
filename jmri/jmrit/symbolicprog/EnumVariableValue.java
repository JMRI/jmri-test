// EnumVariableValue.java

package jmri.jmrit.symbolicprog;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.Vector;

import javax.swing.*;

import com.sun.java.util.collections.ArrayList;
import com.sun.java.util.collections.List;

/**
 * Extends VariableValue to represent a enumerated variable.
 *
 * @author	Bob Jacobsen   Copyright (C) 2001, 2002, 2003
 * @version	$Revision: 1.15 $
 *
 */
public class EnumVariableValue extends VariableValue implements ActionListener, PropertyChangeListener {

    public EnumVariableValue(String name, String comment,
                             boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
                             int cvNum, String mask, int minVal, int maxVal,
                             Vector v, JLabel status, String stdname) {
        super(name, comment, readOnly, infoOnly, writeOnly, opsOnly, cvNum, mask, v, status, stdname);
        _maxVal = maxVal;
        _minVal = minVal;
    }

    /**
     * Create a null object.  Normally only used for tests and to pre-load classes.
     */
    public EnumVariableValue() {}

    public CvValue[] usesCVs() {
        return new CvValue[]{(CvValue)_cvVector.elementAt(getCvNum())};
    }

    public void nItems(int n) {
        _itemArray = new String[n];
        _valueArray = new int[n];
        _nstored = 0;
    }

    /**
     * Create a new item in the enumeration, with an associated
     * value one more than the last item (or zero if this is the first
     * one added)
     * @param s  Name of the enumeration item
     */
    public void addItem(String s) {
        if (_nstored == 0) {
            addItem(s, 0);
        } else {
            addItem(s, _valueArray[_nstored-1]+1);
        }
    }

    /**
     * Create a new item in the enumeration, with a specified
     * associated value.
     * @param s  Name of the enumeration item
     */
    public void addItem(String s, int value) {
        _valueArray[_nstored] = value;
        _itemArray[_nstored++] = s;
    }

    public void lastItem() {
        _value = new JComboBox(_itemArray);
        // finish initialization
        _value.setActionCommand("");
        _defaultColor = _value.getBackground();
        _value.setBackground(COLOR_UNKNOWN);
        // connect to the JComboBox model and the CV so we'll see changes.
        _value.addActionListener(this);
        CvValue cv = ((CvValue)_cvVector.elementAt(getCvNum()));
        cv.addPropertyChangeListener(this);
        cv.setState(CvValue.FROMFILE);
    }

    public void setTooltipText(String t) {
        super.setTooltipText(t);   // do default stuff
        _value.setToolTipText(t);  // set our value
    }

    // stored value
    JComboBox _value = null;

    // place to keep the items & associated numbers
    String[] _itemArray = null;
    int[] _valueArray = null;
    int _nstored;

    private int _maxVal;
    private int _minVal;
    Color _defaultColor;

    public Object rangeVal() {
        return new String("enum: "+_minVal+" - "+_maxVal);
    }

    public void actionPerformed(ActionEvent e) {
        // see if this is from _value itself, or from an alternate rep.
        // if from an alternate rep, it will contain the value to select
        if (!(e.getActionCommand().equals(""))) {
            // is from alternate rep
            _value.setSelectedItem(e.getActionCommand());
        }
        if (log.isDebugEnabled()) log.debug("action event: "+e);

        // notify
        prop.firePropertyChange("Value", null, new Integer(getIntValue()));
        // called for new values - set the CV as needed
        CvValue cv = (CvValue)_cvVector.elementAt(getCvNum());
        // compute new cv value by combining old and request
        int oldCv = cv.getValue();
        int newVal = getIntValue();
        int newCv = newValue(oldCv, newVal, getMask());
        if (newCv != oldCv) cv.setValue(newCv);  // to prevent CV going EDITED during loading of decoder file
    }

    // to complete this class, fill in the routines to handle "Value" parameter
    // and to read/write/hear parameter changes.
    public String getValueString() {
        return ""+_value.getSelectedIndex();
    }
    public void setIntValue(int i) {
        selectValue(i);
    }

    public String getTextValue() {
        return _value.getSelectedItem().toString();
    }

    /**
     * Set to a specific value.
     * <P>
     * This searches for the displayed value, and sets the
     * enum to that particular one.  It used to work off an index,
     * but now it looks for the value.
     * <P>
     * If the value is larger than any defined, a new one is created.
     * @param value
     */
    protected void selectValue(int value) {
        if (value>256) log.error("Saw unreasonable internal value: "+value);
        for (int i = 0; i<_valueArray.length; i++)
            if (_valueArray[i]==value) {
                //found it, select it
                _value.setSelectedIndex(i);
                return;
            }

        // We can be commanded to a number that hasn't been defined.
        // But that's OK for certain applications.  Instead, we add them as needed
        log.debug("Create new item with value "+value+" count was "+_value.getItemCount()
                        +" in "+label());
        _value.addItem("Reserved value "+value);
        // and value array is too short
        int[] oldArray = _valueArray;
        _valueArray = new int[oldArray.length+1];
        for (int i = 0; i<oldArray.length; i++) _valueArray[i] = oldArray[i];
        _valueArray[oldArray.length] = value;

        _value.setSelectedItem("Reserved value "+value);
    }

    public int getIntValue() {
        if (_value.getSelectedIndex()>=_valueArray.length || _value.getSelectedIndex()<0)
            log.error("trying to get value "+_value.getSelectedIndex()+" too large"
                    +" for array length "+_valueArray.length);
        return _valueArray[_value.getSelectedIndex()];
    }

    public Component getValue()  { return _value; }

    public void setValue(int value) {
        int oldVal = getIntValue();
        selectValue(value);

        if (oldVal != value || getState() == VariableValue.UNKNOWN)
            prop.firePropertyChange("Value", null, new Integer(value));
    }

    public Component getRep(String format) {
        // sort on format type
        if (format.equals("checkbox")) {
            // this only makes sense if there are exactly two options
            ComboCheckBox b = new ComboCheckBox(_value, this);
            comboCBs.add(b);
            updateRepresentation(b);
            return b;
        }
        else if (format.equals("radiobuttons")) {
            ComboRadioButtons b = new ComboRadioButtons(_value, this);
            comboRBs.add(b);
            updateRepresentation(b);
            return b;
        }
        else if (format.equals("onradiobutton")) {
            ComboRadioButtons b = new ComboOnRadioButton(_value, this);
            comboRBs.add(b);
            updateRepresentation(b);
            return b;
        }
        else if (format.equals("offradiobutton")) {
            ComboRadioButtons b = new ComboOffRadioButton(_value, this);
            comboRBs.add(b);
            updateRepresentation(b);
            return b;
        }
        else {
            // return a new JComboBox representing the same model
            VarComboBox b = new VarComboBox(_value.getModel(), this);
            comboVars.add(b);
            updateRepresentation(b);
            return b;
        }
    }

    List comboCBs = new ArrayList();
    List comboVars = new ArrayList();
    List comboRBs = new ArrayList();

    // implement an abstract member to set colors
    void setColor(Color c) {
        if (c != null) _value.setBackground(c);
        else _value.setBackground(_defaultColor);
        // prop.firePropertyChange("Value", null, null);
    }

    /**
     * Notify the connected CVs of a state change from above
     * @param state
     */
    public void setCvState(int state) {
        ((CvValue)_cvVector.elementAt(getCvNum())).setState(state);
    }

    public boolean isChanged() {
        CvValue cv = ((CvValue)_cvVector.elementAt(getCvNum()));
        return considerChanged(cv);
    }

    public void readChanges() {
         if (isChanged()) readAll();
    }

    public void writeChanges() {
         if (isChanged()) writeAll();
    }

    public void readAll() {
        setToRead(false);
        setBusy(true);  // will be reset when value changes
        ((CvValue)_cvVector.elementAt(getCvNum())).read(_status);
    }

    public void writeAll() {
        setToWrite(false);
        if (getReadOnly()) log.error("unexpected write operation when readOnly is set");
        setBusy(true);  // will be reset when value changes
        ((CvValue)_cvVector.elementAt(getCvNum())).write(_status);
    }

    // handle incoming parameter notification
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        // notification from CV; check for Value being changed
        if (e.getPropertyName().equals("Busy")) {
            if (((Boolean)e.getNewValue()).equals(Boolean.FALSE)) {
                setToRead(false);
                setToWrite(false);  // some programming operation just finished
                setBusy(false);
            }
        } else if (e.getPropertyName().equals("State")) {
            CvValue cv = (CvValue)_cvVector.elementAt(getCvNum());
            if (cv.getState() == STORED) setToWrite(false);
            if (cv.getState() == READ) setToRead(false);
            setState(cv.getState());
        } else if (e.getPropertyName().equals("Value")) {
            // update value of Variable
            CvValue cv = (CvValue)_cvVector.elementAt(getCvNum());
            int newVal = (cv.getValue() & maskVal(getMask())) >>> offsetVal(getMask());
            setValue(newVal);  // check for duplicate done inside setVal
        }
    }

    /* Internal class extends a JComboBox so that its color is consistent with
     * an underlying variable; we return one of these in getRep.
     *<P>
     * Unlike similar cases elsewhere, this doesn't have to listen to
     * value changes.  Those are handled automagically since we're sharing the same
     * model between this object and the real JComboBox value.
     *
     * @author			Bob Jacobsen   Copyright (C) 2001
     * @version         $Revision: 1.15 $
     */
    public class VarComboBox extends JComboBox {

        VarComboBox(ComboBoxModel m, EnumVariableValue var) {
            super(m);
            _var = var;
            _l = new java.beans.PropertyChangeListener() {
                    public void propertyChange(java.beans.PropertyChangeEvent e) {
                        if (log.isDebugEnabled()) log.debug("VarComboBox saw property change: "+e);
                        originalPropertyChanged(e);
                    }
                };
            // get the original color right
            setBackground(_var._value.getBackground());
            // listen for changes to original state
            _var.addPropertyChangeListener(_l);
        }

        EnumVariableValue _var;
        java.beans.PropertyChangeListener _l = null;

        void originalPropertyChanged(java.beans.PropertyChangeEvent e) {
            // update this color from original state
            if (e.getPropertyName().equals("State")) {
                setBackground(_var._value.getBackground());
            }
        }

        public void dispose() {
            if (_var != null && _l != null ) _var.removePropertyChangeListener(_l);
            _l = null;
            _var = null;
        }
    }

    // clean up connections when done
    public void dispose() {
        if (log.isDebugEnabled()) log.debug("dispose");
        if (_value != null) _value.removeActionListener(this);
        ((CvValue)_cvVector.elementAt(getCvNum())).removePropertyChangeListener(this);
        for (int i = 0; i<comboCBs.size(); i++) {
            ((ComboCheckBox)comboCBs.get(i)).dispose();
        }
        for (int i = 0; i<comboVars.size(); i++) {
            ((VarComboBox)comboVars.get(i)).dispose();
        }
        for (int i = 0; i<comboRBs.size(); i++) {
            ((ComboRadioButtons)comboRBs.get(i)).dispose();
        }

        _value = null;
        // do something about the VarComboBox

    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(EnumVariableValue.class.getName());

}
