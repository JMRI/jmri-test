//IgnoreUsedTrackAction.java

package jmri.jmrit.operations.locations;

import java.awt.event.*;
import javax.swing.*;
import java.util.ResourceBundle;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.setup.Setup;


/**
 * Action to allow a user to define how much used track space
 * is to be ignored by the program when placing new rolling
 * stock to a track.
 * @author Daniel Boudreau Copyright (C) 2012
 * @version     $Revision: 18559 $
 */
public class IgnoreUsedTrackAction extends AbstractAction {
		
	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.locations.JmritOperationsLocationsBundle");
	
	private TrackEditFrame _tef;
	private IgnoreUsedTrackFrame _iutf;
	
	public IgnoreUsedTrackAction(TrackEditFrame tef){
		super(rb.getString("MenuItemPlannedPickups"));
		_tef = tef;
	}
	
	 public void actionPerformed(ActionEvent e) {
		 if (_iutf != null)
			 _iutf.dispose();
		 _iutf = new IgnoreUsedTrackFrame(_tef);
	 }	
}

class IgnoreUsedTrackFrame extends OperationsFrame {
	
	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.locations.JmritOperationsLocationsBundle");
	
	// radio buttons
	JRadioButton zeroPercent = new JRadioButton(rb.getString("Disabled"));
	JRadioButton twentyfivePercent = new JRadioButton("25%");
	JRadioButton fiftyPercent = new JRadioButton("50%");
	JRadioButton seventyfivePercent = new JRadioButton("75%");
	JRadioButton hundredPercent = new JRadioButton("100%");
    
    // major buttons
    JButton saveButton = new JButton(rb.getString("Save"));
    
    private TrackEditFrame _tef;
    protected Track _track;
	
	public IgnoreUsedTrackFrame(TrackEditFrame tef){
		super();
		
		setTitle(rb.getString("MenuItemPlannedPickups"));
			    
	    _tef = tef;
	    _track = _tef._track;
	    if (_track == null){
	    	log.debug("track is null!");
	    	return;
	    }
		
		// load the panel
	    getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
	    
	    JPanel p1 = new JPanel();
	    p1.setBorder(BorderFactory.createTitledBorder(rb.getString("PrePlanedPickups")));
	    
	    p1.add(zeroPercent);
	    p1.add(twentyfivePercent);
	    p1.add(fiftyPercent);
	    p1.add(seventyfivePercent);
	    p1.add(hundredPercent);
	    
	    ButtonGroup buttonGroup = new ButtonGroup();
	    buttonGroup.add(zeroPercent);
	    buttonGroup.add(twentyfivePercent);
	    buttonGroup.add(fiftyPercent);
	    buttonGroup.add(seventyfivePercent);
	    buttonGroup.add(hundredPercent);
	    
	    // select the correct radio button
	    int percentage = _track.getIgnoreUsedLengthPercentage();
	    zeroPercent.setSelected(percentage >= 0);
	    twentyfivePercent.setSelected(percentage >= 25);
	    fiftyPercent.setSelected(percentage >= 50);
	    seventyfivePercent.setSelected(percentage >= 75);
	    hundredPercent.setSelected(percentage >= 100);
	    
	    getContentPane().add(p1);
	    getContentPane().add(saveButton);

    	addButtonAction(saveButton);
    	
    	pack();
    	setVisible(true);    	
	}
	
	public void buttonActionPerformed(java.awt.event.ActionEvent ae){	
		if (ae.getSource() == saveButton){
			// save percentage selected
			int percentage = 0;
			if (twentyfivePercent.isSelected())
				percentage = 25;
			else if (fiftyPercent.isSelected())
				percentage = 50;
			else if (seventyfivePercent.isSelected())
				percentage = 75;
			else if (hundredPercent.isSelected())
				percentage = 100;
			if (_track != null)
				_track.setIgnoreUsedLengthPercentage(percentage);			
			// save location file
			LocationManagerXml.instance().writeOperationsFile();
			if (Setup.isCloseWindowOnSaveEnabled())
				dispose();
		}		
	}
	
	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(IgnoreUsedTrackFrame.class.getName());
}
