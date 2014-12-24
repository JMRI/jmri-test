//ComboOnRadioButton.java

package jmri.jmrit.symbolicprog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.*;

/* Represents a JComboBox as a JPanel containing just the "on" button
 *
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			
 */
public class ComboOnRadioButton extends ComboRadioButtons {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1301681065479961160L;

	ComboOnRadioButton(JComboBox box, EnumVariableValue var) {
		super(box, var);
	}		

	ComboOnRadioButton(JComboBox box, IndexedEnumVariableValue var) {
		super(box, var);
	}		

	/**
	 * Make only the "on" button visible
	 */
	void addToPanel(JRadioButton b, int i) {
		if (i==1) add(b);
	}

	// initialize logging	
    static Logger log = LoggerFactory.getLogger(ComboOnRadioButton.class.getName());

}
