// HexVariableValue.java
package jmri.jmrit.symbolicprog;

import java.awt.event.ActionEvent;
import java.util.HashMap;
import javax.swing.JLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LIke DecVariableValue, except that the string representation is in
 * hexadecimal
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2014
 * @version $Revision$
 */
public class HexVariableValue extends DecVariableValue {

    public HexVariableValue(String name, String comment, String cvName,
            boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
            String cvNum, String mask, int minVal, int maxVal,
            HashMap<String, CvValue> v, JLabel status, String stdname) {
        super(name, comment, cvName, readOnly, infoOnly, writeOnly, opsOnly, cvNum, mask, minVal, maxVal, v, status, stdname);
    }

    void updatedTextField() {
        if (log.isDebugEnabled()) {
            log.debug("updatedTextField");
        }
        // called for new values - set the CV as needed
        CvValue cv = _cvMap.get(getCvNum());
        // compute new cv value by combining old and request
        int oldCv = cv.getValue();
        int newVal;
        try {
            newVal = Integer.valueOf(_value.getText(), 16).intValue();
        } catch (java.lang.NumberFormatException ex) {
            newVal = 0;
        }
        int newCv = newValue(oldCv, newVal, getMask());
        if (oldCv != newCv) {
            cv.setValue(newCv);
        }
    }

    /**
     * ActionListener implementations
     */
    public void actionPerformed(ActionEvent e) {
        if (log.isDebugEnabled()) {
            log.debug("actionPerformed");
        }
        int newVal = Integer.valueOf(_value.getText(), 16).intValue();
        updatedTextField();
        prop.firePropertyChange("Value", null, Integer.valueOf(newVal));
    }

    public void setValue(int value) {
        int oldVal;
        try {
            oldVal = Integer.valueOf(_value.getText(), 16).intValue();
        } catch (java.lang.NumberFormatException ex) {
            oldVal = -999;
        }
        if (log.isDebugEnabled()) {
            log.debug("setValue with new value " + value + " old value " + oldVal);
        }
        _value.setText(Integer.toHexString(value));
        if (oldVal != value || getState() == VariableValue.UNKNOWN) {
            actionPerformed(null);
        }
        prop.firePropertyChange("Value", Integer.valueOf(oldVal), Integer.valueOf(value));
    }

    // initialize logging
    static Logger log = LoggerFactory.getLogger(HexVariableValue.class.getName());

}
