//PoolTrackAction.java

package jmri.jmrit.operations.locations;

import java.awt.GridBagLayout;
import java.awt.event.*;

import javax.swing.*;

import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;

import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;


/**
 * Action to create a track pool and place a track in that pool.
 * @author Daniel Boudreau Copyright (C) 2011
 * @version     $Revision$
 */
public class PoolTrackAction extends AbstractAction {
		
	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.locations.JmritOperationsLocationsBundle");
	
	private TrackEditFrame _tef;
	private PoolTrackFrame _ptf;
	
	public PoolTrackAction(TrackEditFrame tef){
		super(rb.getString("MenuItemPoolTrack"));
		_tef = tef;
	}
	
	 public void actionPerformed(ActionEvent e) {
		 if (_ptf != null)
			 _ptf.dispose();
		 _ptf = new PoolTrackFrame(_tef);
	 }	
}

class PoolTrackFrame extends OperationsFrame implements java.beans.PropertyChangeListener{
	
	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.locations.JmritOperationsLocationsBundle");
	
	// labels
	JLabel name = new JLabel(rb.getString("Name"));
	JLabel minimum = new JLabel(rb.getString("Minimum"));
	JLabel length = new JLabel(rb.getString("Length"));
	
	// text field
	JTextField trackPoolNameTextField = new JTextField(20);
	JTextField trackMinLengthTextField = new JTextField(5);
	
	// combo box
	JComboBox comboBoxPools = new JComboBox();
    
    // major buttons
	JButton addButton = new JButton(rb.getString("Add"));
    JButton saveButton = new JButton(rb.getString("Save"));
    
    // pool status
    JPanel poolStatus = new JPanel();
    
    private TrackEditFrame _tef;
    protected Track _track;
    protected Pool _pool;
	
	public PoolTrackFrame(TrackEditFrame tef){
		super();
		
		// the following code sets the frame's initial state
	    getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
	    
	    _tef = tef;
	    _track = _tef._track;
	    if (_track == null){
	    	log.debug("track is null, pools can not be created");
	    	return;
	    }
	    _track.addPropertyChangeListener(this);
	    _track.getLocation().addPropertyChangeListener(this);
	    _pool = _track.getPool();
	    if (_pool != null)
	    	_pool.addPropertyChangeListener(this);
		
		// load the panel
    	JPanel p1 = new JPanel();
    	p1.setLayout(new BoxLayout(p1,BoxLayout.Y_AXIS));
       	JScrollPane p1Pane = new JScrollPane(p1);
       	p1Pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
       	p1Pane.setBorder(BorderFactory.createTitledBorder(""));

    	JPanel poolName = new JPanel();
    	poolName.setLayout(new GridBagLayout());
    	poolName.setBorder(BorderFactory.createTitledBorder(rb.getString("PoolName")));
    	addItem(poolName, trackPoolNameTextField, 0, 0);
    	addItem(poolName, addButton, 1, 0);
    	
       	JPanel selectPool = new JPanel();
    	selectPool.setLayout(new GridBagLayout());
    	selectPool.setBorder(BorderFactory.createTitledBorder(rb.getString("PoolSelect")));
    	addItem(selectPool, comboBoxPools, 0, 0);
    	
       	JPanel minLengthTrack = new JPanel();
    	minLengthTrack.setLayout(new GridBagLayout());
    	minLengthTrack.setBorder(BorderFactory.createTitledBorder(MessageFormat.format(rb.getString("PoolTrackMinimum"),new Object[]{_track.getName()})));
    	addItem(minLengthTrack, trackMinLengthTextField, 0, 0);
    	
    	trackMinLengthTextField.setText(Integer.toString(_track.getMinimumLength()));
    	
      	JPanel savePool = new JPanel();
    	savePool.setLayout(new GridBagLayout());
    	savePool.setBorder(BorderFactory.createTitledBorder(""));
    	addItem(savePool, saveButton, 0, 0);
    	
    	p1.add(poolName);
    	p1.add(selectPool);
    	p1.add(minLengthTrack);
    	p1.add(savePool);
    	
    	JPanel p2 = new JPanel();
    	p2.setLayout(new BoxLayout(p2,BoxLayout.Y_AXIS));
       	JScrollPane p2Pane = new JScrollPane(p2);
       	p2Pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
       	p2Pane.setBorder(BorderFactory.createTitledBorder(""));
       	
       	// pool status panel
    	poolStatus.setLayout(new GridBagLayout());
    	
    	p2.add(poolStatus);

    	getContentPane().add(p1Pane);
    	getContentPane().add(p2Pane);
    	setTitle(rb.getString("MenuItemPoolTrack"));  	
    	
       	// load comboBox
    	updatePoolsComboBox();
    	updatePoolStatus();
    	
    	// button action
    	addButtonAction(addButton);
    	addButtonAction(saveButton);
    	
    	setVisible(true);    	
	}
	
	private void updatePoolsComboBox(){
		_track.getLocation().updatePoolComboBox(comboBoxPools);
		comboBoxPools.setSelectedItem(_track.getPool());
	}
	
	private void updatePoolStatus(){
		poolStatus.removeAll();
		
    	addItemLeft(poolStatus, name, 0, 0);
    	addItem(poolStatus, minimum, 1, 0);
    	addItem(poolStatus, length, 2, 0);
    	
		String poolName = "";
		if (_track.getPool() != null){
			Pool pool = _track.getPool();
			poolName = pool.getName();
			List<Track> tracks = pool.getTracks();
			int totalMinLength = 0;
			int totalLength = 0;
			for (int i=0; i<tracks.size(); i++){
				Track track = tracks.get(i);
				JLabel name = new JLabel();
				name.setText(track.getName());
				
				JLabel minimum = new JLabel();
				minimum.setText(Integer.toString(track.getMinimumLength()));
				totalMinLength = totalMinLength + track.getMinimumLength();
				
				JLabel length = new JLabel();
				length.setText(Integer.toString(track.getLength()));
				totalLength = totalLength + track.getLength();
				
				addItemLeft(poolStatus, name, 0, i+1);
				addItem(poolStatus, minimum, 1, i+1);
				addItem(poolStatus, length, 2, i+1);
			}
			// Summary
			JLabel total = new JLabel(rb.getString("Totals"));
			addItem(poolStatus, total, 0, tracks.size()+1);
			JLabel totalMin = new JLabel();
			totalMin.setText(Integer.toString(totalMinLength));
			addItem(poolStatus, totalMin, 1, tracks.size()+1);
			JLabel totalLen = new JLabel();
			totalLen.setText(Integer.toString(totalLength));
			addItem(poolStatus, totalLen, 2, tracks.size()+1);			
		}
		poolStatus.setBorder(BorderFactory.createTitledBorder(MessageFormat.format(rb.getString("PoolTracks"),new Object[]{poolName})));
		poolStatus.repaint();
		poolStatus.validate();
		setPreferredSize(null);	//kill JMRI window size
		pack();
	}
	
	public void buttonActionPerformed(java.awt.event.ActionEvent ae){
		if (ae.getSource() == addButton){
			Location location = _track.getLocation();
			location.addPool(trackPoolNameTextField.getText());			
		}
		if (ae.getSource() == saveButton){
			try {
				_track.setMinimumLength(Integer.parseInt(trackMinLengthTextField.getText()));
			} catch (NumberFormatException e){
				JOptionPane.showMessageDialog(this,
						rb.getString("TrackMustBeNumber"), rb.getString("ErrorTrackLength"),
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (comboBoxPools.getSelectedItem() != null && comboBoxPools.getSelectedItem().equals("")){
				_track.setPool(null);
				if (_pool != null)
					_pool.removePropertyChangeListener(this);
				_pool = null;
			}
			else if (comboBoxPools.getSelectedItem() != null && !comboBoxPools.getSelectedItem().equals("")){
				if (_pool != null)
					_pool.removePropertyChangeListener(this);
				_pool = (Pool)comboBoxPools.getSelectedItem();
				_pool.addPropertyChangeListener(this);
				_track.setPool(_pool);
			}

			// save location file
			OperationsXml.save();
			if (Setup.isCloseWindowOnSaveEnabled())
				dispose();
		}		
	}
	
	public void dispose(){
		_track.removePropertyChangeListener(this);
		_track.getLocation().removePropertyChangeListener(this);
		if (_pool != null)
			_pool.removePropertyChangeListener(this);
		super.dispose();
	}
	
	public void propertyChange(java.beans.PropertyChangeEvent e) {
		if (Control.showProperty && log.isDebugEnabled()) 
			log.debug("Property change " +e.getPropertyName()+ " old: "+e.getOldValue()+ " new: "+e.getNewValue());
		if (e.getPropertyName().equals(Location.POOL_LENGTH_CHANGED_PROPERTY))
			updatePoolsComboBox();
		if (e.getPropertyName().equals(Pool.LISTCHANGE_CHANGED_PROPERTY)
				|| e.getPropertyName().equals(Location.LENGTH_CHANGED_PROPERTY)
				|| e.getPropertyName().equals(Track.MIN_LENGTH_CHANGED_PROPERTY))
			updatePoolStatus();
	}
	
	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PoolTrackFrame.class.getName());
}
