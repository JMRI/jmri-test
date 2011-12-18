// TrainCopyFrame.java

package jmri.jmrit.operations.trains;
 
import jmri.jmrit.operations.OperationsFrame;

import java.awt.GridBagLayout;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * Frame for copying a train for operations.
 *
 * @author		Bob Jacobsen   Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2011
 * @version             $Revision: 17977 $
 */
public class TrainCopyFrame extends OperationsFrame {
	
	static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.trains.JmritOperationsTrainsBundle");

	TrainManager trainManager = TrainManager.instance();
	
	// labels
	javax.swing.JLabel textCopyTrain = new javax.swing.JLabel(rb.getString("SelectTrain"));
	javax.swing.JLabel textTrainName = new javax.swing.JLabel(rb.getString("Name"));
	
	// text field
	javax.swing.JTextField trainNameTextField = new javax.swing.JTextField(20);
	
	// major buttons
	javax.swing.JButton copyButton = new javax.swing.JButton(rb.getString("Copy"));
	
	// combo boxes
	javax.swing.JComboBox trainBox = TrainManager.instance().getComboBox();

    public TrainCopyFrame() {
        super(ResourceBundle.getBundle("jmri.jmrit.operations.trains.JmritOperationsTrainsBundle").getString("TitleTrainCopy"));
        // general GUI config

        getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
	    
        //      Set up the panels
    	JPanel p1 = new JPanel();
    	p1.setLayout(new GridBagLayout());
				
		// Layout the panel by rows
    	// row 1
		addItem(p1, textTrainName, 0, 1);
		addItemWidth(p1, trainNameTextField, 3, 1, 1);
    	
		// row 2
		addItem(p1, textCopyTrain, 0, 2);
		addItemWidth(p1, trainBox, 3, 1, 2);
		
		// row 4
		addItem(p1, copyButton, 1, 4);
		
		getContentPane().add(p1);
    	
        // add help menu to window
    	addHelpMenu("package.jmri.jmrit.operations.Operations_Trains", true);
    	
    	pack();
      	if (getWidth()<400) 
    		setSize(400, getHeight());
    	if (getHeight()<150)
    		setSize(getWidth(), 150);
    	
    	// setup buttons
		addButtonAction(copyButton);
    }
    
    public void setTrainName(String trainName){
    	trainBox.setSelectedItem(trainManager.getTrainByName(trainName));
    }
    
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == copyButton){
			log.debug("copy train button actived");
			if (!checkName())
				return;

			Train newTrain = trainManager.getTrainByName(trainNameTextField.getText());
			if (newTrain != null){
				reportTrainExists();
				return;
			}
			if (trainBox.getSelectedItem() == null || trainBox.getSelectedItem().equals("")){
				reportTrainDoesNotExist();
				return;
			}
			Train oldTrain = (Train)trainBox.getSelectedItem();
			if (oldTrain == null){
				reportTrainDoesNotExist();
				return;
			}
			
			// now copy
			newTrain = trainManager.copyTrain(oldTrain, trainNameTextField.getText());

			TrainEditFrame f = new TrainEditFrame();
			f.initComponents(newTrain);
			f.setTitle(rb.getString("TitleTrainEdit"));
			f.setVisible(true);
		}
	}
	
	private void reportTrainExists(){
		JOptionPane.showMessageDialog(this,
				rb.getString("TrainNameExists"), MessageFormat.format(rb.getString("CanNotTrain"),new Object[]{rb.getString("copy")}),
				JOptionPane.ERROR_MESSAGE);
	}
	
	private void reportTrainDoesNotExist(){
		JOptionPane.showMessageDialog(this,
				rb.getString("SelectTrain"), MessageFormat.format(rb.getString("CanNotTrain"),new Object[]{rb.getString("copy")}),
				JOptionPane.ERROR_MESSAGE);
	}
	
	/**
	 * 
	 * @return true if name is less than 26 characters
	 */
	private boolean checkName(){
		if (trainNameTextField.getText().trim().equals("")){
			JOptionPane.showMessageDialog(this,
					rb.getString("EnterTrainName"), MessageFormat.format(rb.getString("CanNotTrain"),new Object[]{rb.getString("copy")}),
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		if (trainNameTextField.getText().length() > 25){
			JOptionPane.showMessageDialog(this,
					rb.getString("TrainNameLess26"), MessageFormat.format(rb.getString("CanNot"), new Object[] {rb.getString("copy")}),
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

    public void dispose() {
        super.dispose();
    }
    
	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(TrainCopyFrame.class.getName());
}
