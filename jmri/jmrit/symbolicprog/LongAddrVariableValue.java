// LongAddrVariableValue.java

package jmri.jmrit.symbolicprog;

import jmri.Programmer;
import jmri.InstanceManager;
import jmri.ProgListener;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.util.Vector;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.text.Document;

/**
 * Extends VariableValue to represent a NMRA long address
 * @author			Bob Jacobsen   Copyright (C) 2001, 2002
 * @version			$Revision: 1.12 $
 *
 */
public class LongAddrVariableValue extends VariableValue
    implements ActionListener, PropertyChangeListener, FocusListener {

    public LongAddrVariableValue(String name, String comment,
                                 boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
                                 int cvNum, String mask, int minVal, int maxVal,
                                 Vector v, JLabel status, String stdname) {
        super(name, comment, readOnly, infoOnly, writeOnly, opsOnly, cvNum, mask, v, status, stdname);
        _maxVal = maxVal;
        _minVal = minVal;
        _value = new JTextField("0", 5);
        _defaultColor = _value.getBackground();
        _value.setBackground(COLOR_UNKNOWN);
        // connect to the JTextField value, cv
        _value.addActionListener(this);
        _value.addFocusListener(this);
        // connect for notification
        CvValue cv = ((CvValue)_cvVector.elementAt(getCvNum()));
        cv.addPropertyChangeListener(this);
        cv.setState(CvValue.FROMFILE);
        CvValue cv1 = ((CvValue)_cvVector.elementAt(getCvNum()+1));
        cv1.addPropertyChangeListener(this);
        cv1.setState(CvValue.FROMFILE);
    }

    public CvValue[] usesCVs() {
        return new CvValue[]{
            (CvValue)_cvVector.elementAt(getCvNum()),
            (CvValue)_cvVector.elementAt(getCvNum()+1)};
    }

    public void setTooltipText(String t) {
        super.setTooltipText(t);   // do default stuff
        _value.setToolTipText(t);  // set our value
    }

    // the connection is to cvNum and cvNum+1

    int _maxVal;
    int _minVal;

    public Object rangeVal() {
        return new String("Long address");
    }

    String oldContents = "";

    void enterField() {
        oldContents = _value.getText();
    }
    void exitField() {
        // this _can_ be invoked after dispose, so protect
        if (_value != null & !oldContents.equals(_value.getText())) {
            int newVal = Integer.valueOf(_value.getText()).intValue();
            int oldVal = Integer.valueOf(oldContents).intValue();
            updatedTextField();
            prop.firePropertyChange("Value", new Integer(oldVal), new Integer(newVal));
        }
    }

    void updatedTextField() {
        if (log.isDebugEnabled()) log.debug("actionPerformed");
        // called for new values - set the CV as needed
        CvValue cv17 = (CvValue)_cvVector.elementAt(getCvNum());
        CvValue cv18 = (CvValue)_cvVector.elementAt(getCvNum()+1);
        // no masking involved for long address
        int newVal;
        try { newVal = Integer.valueOf(_value.getText()).intValue(); }
        catch (java.lang.NumberFormatException ex) { newVal = 0; }

        // no masked combining of old value required, as this fills the two CVs
        int newCv17 = ((newVal/256)&0x3F) | 0xc0;
        int newCv18 = newVal & 0xFF;
        cv17.setValue(newCv17);
        cv18.setValue(newCv18);
        if (log.isDebugEnabled()) log.debug("new value "+newVal+" gives CV17="+newCv17+" CV18="+newCv18);
    }

    /** ActionListener implementations */
    public void actionPerformed(ActionEvent e) {
        if (log.isDebugEnabled()) log.debug("actionPerformed");
        int newVal = Integer.valueOf(_value.getText()).intValue();
        updatedTextField();
        prop.firePropertyChange("Value", null, new Integer(newVal));
    }

    /** FocusListener implementations */
    public void focusGained(FocusEvent e) {
        if (log.isDebugEnabled()) log.debug("focusGained");
        enterField();
    }

    public void focusLost(FocusEvent e) {
        if (log.isDebugEnabled()) log.debug("focusLost");
        exitField();
    }

    // to complete this class, fill in the routines to handle "Value" parameter
    // and to read/write/hear parameter changes.
    public String getValueString() {
        return _value.getText();
    }
    public void setIntValue(int i) {
        setValue(i);
    }

    public Component getValue()  {
        if (getReadOnly())  {
            JLabel r = new JLabel(_value.getText());
            updateRepresentation(r);
            return r;
        } else
            return _value;
    }
    public void setValue(int value) {
        int oldVal;
        try { oldVal = Integer.valueOf(_value.getText()).intValue(); }
        catch (java.lang.NumberFormatException ex) { oldVal = -999; }
        if (log.isDebugEnabled()) log.debug("setValue with new value "+value+" old value "+oldVal);
        _value.setText(""+value);
        if (oldVal != value || getState() == VariableValue.UNKNOWN)
            actionPerformed(null);
        prop.firePropertyChange("Value", new Integer(oldVal), new Integer(value));
    }

    Color _defaultColor;
    // implement an abstract member to set colors
    void setColor(Color c) {
        if (c != null) _value.setBackground(c);
        else _value.setBackground(_defaultColor);
        // prop.firePropertyChange("Value", null, null);
    }

    public Component getRep(String format)  {
        return updateRepresentation(new VarTextField(_value.getDocument(),_value.getText(), 5, this));
    }
    private int _progState = 0;
    private static final int IDLE = 0;
    private static final int READING_FIRST = 1;
    private static final int READING_SECOND = 2;
    private static final int WRITING_FIRST = 3;
    private static final int WRITING_SECOND = 4;

    /**
     * Notify the connected CVs of a state change from above
     * @param state
     */
    public void setCvState(int state) {
        ((CvValue)_cvVector.elementAt(getCvNum())).setState(state);
    }

    public boolean isChanged() {
        CvValue cv1 = ((CvValue)_cvVector.elementAt(getCvNum()));
        CvValue cv2 = ((CvValue)_cvVector.elementAt(getCvNum()+1));
        return (considerChanged(cv1)||considerChanged(cv2));
    }

    public void setToRead(boolean state) { 
        ((CvValue)_cvVector.elementAt(getCvNum())).setToRead(state); 
        ((CvValue)_cvVector.elementAt(getCvNum()+1)).setToRead(state); 
    }

    public boolean isToRead() { 
        return ((CvValue)_cvVector.elementAt(getCvNum())).isToRead() || ((CvValue)_cvVector.elementAt(getCvNum()+1)).isToRead();
    }

    public void setToWrite(boolean state) {
        ((CvValue)_cvVector.elementAt(getCvNum())).setToWrite(state);
        ((CvValue)_cvVector.elementAt(getCvNum()+1)).setToWrite(state);
    }

    public boolean isToWrite() { 
        return ((CvValue)_cvVector.elementAt(getCvNum())).isToWrite() || ((CvValue)_cvVector.elementAt(getCvNum()+1)).isToWrite();
    }

    public void readChanges() {
         if (isChanged()) readAll();
    }

    public void writeChanges() {
         if (isChanged()) writeAll();
    }

    public void readAll() {
        if (log.isDebugEnabled()) log.debug("longAddr read() invoked");
        setToRead(false);
        setBusy(true);  // will be reset when value changes
        if (_progState != IDLE) log.warn("Programming state "+_progState+", not IDLE, in read()");
        _progState = READING_FIRST;
        if (log.isDebugEnabled()) log.debug("invoke CV read");
        ((CvValue)_cvVector.elementAt(getCvNum())).read(_status);
    }

    public void writeAll() {
        if (log.isDebugEnabled()) log.debug("write() invoked");
        if (getReadOnly()) log.error("unexpected write operation when readOnly is set");
        setToWrite(false);
        setBusy(true);  // will be reset when value changes
        if (_progState != IDLE) log.warn("Programming state "+_progState+", not IDLE, in write()");
        _progState = WRITING_FIRST;
        if (log.isDebugEnabled()) log.debug("invoke CV write");
        ((CvValue)_cvVector.elementAt(getCvNum())).write(_status);
    }

    // handle incoming parameter notification
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (log.isDebugEnabled()) log.debug("property changed event - name: "
                                            +e.getPropertyName());
        // notification from CV; check for Value being changed
        if (e.getPropertyName().equals("Busy") && ((Boolean)e.getNewValue()).equals(Boolean.FALSE)) {
            // busy transitions drive the state
            switch (_progState) {
            case IDLE:  // no, just a CV update
                if (log.isDebugEnabled()) log.error("Busy goes false with state IDLE");
                return;
            case READING_FIRST:   // read first CV, now read second
                if (log.isDebugEnabled()) log.debug("Busy goes false with state READING_FIRST");
                _progState = READING_SECOND;
                ((CvValue)_cvVector.elementAt(getCvNum()+1)).read(_status);
                return;
            case READING_SECOND:  // finally done, set not busy
                if (log.isDebugEnabled()) log.debug("Busy goes false with state READING_SECOND");
                _progState = IDLE;
                ((CvValue)_cvVector.elementAt(getCvNum())).setState(READ);
                ((CvValue)_cvVector.elementAt(getCvNum()+1)).setState(READ);
                //super.setState(READ);
                setBusy(false);
                return;
            case WRITING_FIRST:  // no, just a CV update
                if (log.isDebugEnabled()) log.debug("Busy goes false with state WRITING_FIRST");
                _progState = WRITING_SECOND;
                ((CvValue)_cvVector.elementAt(getCvNum()+1)).write(_status);
                return;
            case WRITING_SECOND:  // now done with complete request
                if (log.isDebugEnabled()) log.debug("Busy goes false with state WRITING_SECOND");
                _progState = IDLE;
                super.setState(STORED);
                setBusy(false);
                return;
            default:  // unexpected!
                log.error("Unexpected state found: "+_progState);
                _progState = IDLE;
                return;
            }
        }
        else if (e.getPropertyName().equals("State")) {
            CvValue cv = (CvValue)_cvVector.elementAt(getCvNum());
            if (log.isDebugEnabled()) log.debug("CV State changed to "+cv.getState());
            setState(cv.getState());
        }
        else if (e.getPropertyName().equals("Value")) {
            // update value of Variable
            CvValue cv0 = (CvValue)_cvVector.elementAt(getCvNum());
            CvValue cv1 = (CvValue)_cvVector.elementAt(getCvNum()+1);
            int newVal = (cv0.getValue()&0x3f)*256 + cv1.getValue();
            setValue(newVal);  // check for duplicate done inside setVal
            // state change due to CV state change, so propagate that
            setState(cv0.getState());
            // see if this was a read or write operation
            switch (_progState) {
            case IDLE:  // no, just a CV update
                if (log.isDebugEnabled()) log.debug("Value changed with state IDLE");
                return;
            case READING_FIRST:  // yes, now read second
                if (log.isDebugEnabled()) log.debug("Value changed with state READING_FIRST");
                return;
            case READING_SECOND:  // now done with complete request
                if (log.isDebugEnabled()) log.debug("Value changed with state READING_SECOND");
                return;
            default:  // unexpected!
                log.error("Unexpected state found: "+_progState);
                _progState = IDLE;
                return;
            }
        }
    }

    // stored value
    JTextField _value = null;

    /* Internal class extends a JTextField so that its color is consistent with
     * an underlying variable
     *
     * @author			Bob Jacobsen   Copyright (C) 2001
     * @version
     */
    public class VarTextField extends JTextField {

        VarTextField(Document doc, String text, int col, LongAddrVariableValue var) {
            super(doc, text, col);
            _var = var;
            // get the original color right
            setBackground(_var._value.getBackground());
            // listen for changes to ourself
            addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        thisActionPerformed(e);
                    }
                });
            addFocusListener(new java.awt.event.FocusListener() {
                    public void focusGained(FocusEvent e) {
                        if (log.isDebugEnabled()) log.debug("focusGained");
                        enterField();
                    }

                    public void focusLost(FocusEvent e) {
                        if (log.isDebugEnabled()) log.debug("focusLost");
                        exitField();
                    }
                });
            // listen for changes to original state
            _var.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
                    public void propertyChange(java.beans.PropertyChangeEvent e) {
                        originalPropertyChanged(e);
                    }
                });
        }

        LongAddrVariableValue _var;

        void thisActionPerformed(java.awt.event.ActionEvent e) {
            // tell original
            _var.actionPerformed(e);
        }

        void originalPropertyChanged(java.beans.PropertyChangeEvent e) {
            // update this color from original state
            if (e.getPropertyName().equals("State")) {
                setBackground(_var._value.getBackground());
            }
        }

    }

    // clean up connections when done
    public void dispose() {
        if (log.isDebugEnabled()) log.debug("dispose");
        if (_value != null) _value.removeActionListener(this);
        ((CvValue)_cvVector.elementAt(getCvNum())).removePropertyChangeListener(this);
        ((CvValue)_cvVector.elementAt(getCvNum()+1)).removePropertyChangeListener(this);

        _value = null;
        // do something about the VarTextField
    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LongAddrVariableValue.class.getName());

}
