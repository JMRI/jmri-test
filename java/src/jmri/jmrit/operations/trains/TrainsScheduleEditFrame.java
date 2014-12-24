//TrainsScheduleEditFrame.java

package jmri.jmrit.operations.trains;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Dimension;
import java.awt.GridBagLayout;

import javax.swing.*;

import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.setup.Control;

/**
 * Used to edit train timetable.
 * @author Daniel Boudreau Copyright (C)
 * @version $Revision: 23749 $
 *
 */
public class TrainsScheduleEditFrame extends OperationsFrame implements java.beans.PropertyChangeListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6005860633909479680L;

	// text box
	JTextField addTextBox = new JTextField(Control.max_len_string_attibute);

	// combo box
	JComboBox comboBox;
    
    // major buttons
    JButton addButton = new JButton(Bundle.getMessage("Add"));
    JButton deleteButton = new JButton(Bundle.getMessage("Delete"));
    JButton replaceButton = new JButton(Bundle.getMessage("Replace"));
    
    JButton restoreButton = new JButton(Bundle.getMessage("Restore"));
    
    TrainScheduleManager trainScheduleManager = TrainScheduleManager.instance();
	
    
	public TrainsScheduleEditFrame(){
		super();
		
		// the following code sets the frame's initial state
	    getContentPane().setLayout(new GridBagLayout());
	    
	    trainScheduleManager.addPropertyChangeListener(this);
	    comboBox = trainScheduleManager.getComboBox();
	    
		// row 1

		addItem(addTextBox, 2, 2);
        addItem(addButton, 3, 2);
        
        // row 3
        addItem(comboBox, 2, 3);
        addItem(deleteButton, 3, 3);
        
        // row 4 
        addItem(replaceButton, 3, 4);
        
        // row 5
        addItem(restoreButton, 2, 5);
        
		addButtonAction(addButton);
        addButtonAction(deleteButton);
		addButtonAction(replaceButton);
		addButtonAction(restoreButton);
		
		setTitle(Bundle.getMessage("MenuItemEditSchedule"));
		initMinimumSize(new Dimension(Control.panelWidth300, Control.panelHeight200));

	}
	
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == deleteButton && comboBox.getSelectedItem() != null){
			trainScheduleManager.deregister((TrainSchedule)comboBox.getSelectedItem());
		}
		if (ae.getSource() == restoreButton){
			trainScheduleManager.createDefaultSchedules();
		}		
		// check for valid name
		String s = addTextBox.getText();
		s = s.trim();
		if (s.equals(""))
			return;	// done
		
		if (ae.getSource() == addButton){
			trainScheduleManager.newSchedule(s);
		}
		if (ae.getSource() == replaceButton && comboBox.getSelectedItem() != null){
			TrainSchedule ts = ((TrainSchedule)comboBox.getSelectedItem());
			ts.setName(s);
		}
	}
	
	public void dispose(){
		trainScheduleManager.removePropertyChangeListener(this);
		super.dispose();
	}
	
	public void propertyChange(java.beans.PropertyChangeEvent e) {
		if (Control.showProperty && log.isDebugEnabled())
			log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
					.getNewValue());
		trainScheduleManager.updateComboBox(comboBox);
	}

	
	static Logger log = LoggerFactory.getLogger(TrainsScheduleEditFrame.class.getName());
}
