// InterchangeEditFrame.java

package jmri.jmrit.operations.locations;
import javax.swing.BorderFactory;


/**
 * Frame for user edit of an interchange track.  Adds two panels 
 * to TrackEditFram for train/route car drops and pick ups.
 * 
 * @author Dan Boudreau Copyright (C) 2008, 2011
 * @version $Revision$
 */

public class InterchangeEditFrame extends TrackEditFrame implements java.beans.PropertyChangeListener {
	
	public InterchangeEditFrame() {
		super();
	}

	public void initComponents(Location location, Track track) {
		_type = Track.INTERCHANGE;
				
		super.initComponents(location, track);
		
		_toolMenu.add(new ChangeTrackTypeAction (this));
		_toolMenu.add(new IgnoreUsedTrackAction (this));
		addHelpMenu("package.jmri.jmrit.operations.Operations_Interchange", true);	// NOI18N
		
		// override text strings for tracks
		//panelTrainDir.setBorder(BorderFactory.createTitledBorder(rb.getString("TrainInterchange")));
		paneCheckBoxes.setBorder(BorderFactory.createTitledBorder(rb.getString("TypesInterchange")));
		//deleteTrackButton.setText(rb.getString("DeleteInterchange"));
		addTrackButton.setText(rb.getString("AddInterchange"));
		saveTrackButton.setText(rb.getString("SaveInterchange"));
		
		// finish
		packFrame();
		setVisible(true);
	}
	
	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(InterchangeEditFrame.class.getName());
}
