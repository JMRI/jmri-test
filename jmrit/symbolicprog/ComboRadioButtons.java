//ComboRadioButtons.java

package jmri.jmrit.symbolicprog;

import java.awt.Component;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;

/* Represents a JComboBox as a JPanel of radio buttons.
 *
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			
 */
public class ComboRadioButtons extends JPanel {

	ComboRadioButtons(JComboBox box, EnumVariableValue var) {
		super();
		_var = var;
		_box = box;
		// create the buttons, include in group, listen for changes by name
		ButtonGroup g = new ButtonGroup();
		for (int i=0; i<box.getItemCount(); i++) {
			String name = ((String)(box.getItemAt(i)));
			JRadioButton b = new JRadioButton( name );
			b.setActionCommand(name);
			b.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					thisActionPerformed(e);
				}
			});		
			v.addElement(b);
			add(b);
			g.add(b);
		}
		setColor();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		// listen for changes to original
		_box.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				originalActionPerformed(e);
			}
		});
		// listen for changes to original state
		_var.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
			public void propertyChange(java.beans.PropertyChangeEvent e) {
				originalPropertyChanged(e);
			}
		});		

		// set initial state
		((JRadioButton)(v.elementAt(_box.getSelectedIndex()))).setSelected(true);
	}

	void thisActionPerformed(java.awt.event.ActionEvent e) {
		// update original state to selected button
		_box.setSelectedItem(e.getActionCommand());
	}

	void originalActionPerformed(java.awt.event.ActionEvent e) {
		// update this state to original state
		((JRadioButton)(v.elementAt(_box.getSelectedIndex()))).setSelected(true);
	}
	
	void originalPropertyChanged(java.beans.PropertyChangeEvent e) {
		// update this color from original state
		if (e.getPropertyName().equals("State")) {
			if (log.isDebugEnabled()) log.debug("State change seen");
			setColor();
		}	
	}

	protected void setColor() {
		//setBackground(_var._value.getBackground());
		for (int i = 0; i<v.size(); i++) {
			((JRadioButton)(v.elementAt(i))).setBackground(_var._value.getBackground());
		}
	}
	
	EnumVariableValue _var = null;
	JComboBox _box = null;
	Vector v = new Vector();

	// initialize logging	
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ComboCheckBox.class.getName());

}
