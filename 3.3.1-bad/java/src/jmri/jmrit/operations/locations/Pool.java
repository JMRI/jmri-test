package jmri.jmrit.operations.locations;

import java.util.ArrayList;
import java.util.List;

import jmri.jmrit.operations.BeanBase;

/**
 * Represents a pool of tracks that share their length.
 * 
 * @author Daniel Boudreau Copyright (C) 2011
 * @author Gregory Madsen Copyright (C) 2012
 * 
 * @version $Revision$
 */
public class Pool extends BeanBase {

	public static final String LISTCHANGE_CHANGED_PROPERTY = "listChange"; // NOI18N
	public static final String DISPOSE = "dispose"; // NOI18N

	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Pool.class);

	// stores tracks for this pool
	protected List<Track> _tracks = new ArrayList<Track>();

	protected String _id = "";

	public String getId() {
		return _id;
	}

	protected String _name = "";

	public String getName() {
		return _name;
	}

	public void setName(String name) {
		String old = _name;
		_name = name;
		firePropertyChange("Name", old, name);
	}

	public int getSize() {
		return _tracks.size();
	}

	// for combo boxes
	public String toString() {
		return _name;
	}

	public Pool(String id, String name) {
		log.debug("New pool (" + name + ") id: " + id);
		_name = name;
		_id = id;
	}

	public void dispose() {
		firePropertyChange(DISPOSE, null, DISPOSE);
	}

	/**
	 * Adds a track to this pool
	 * 
	 * @param track
	 *            to be added.
	 */
	public void add(Track track) {
		if (!_tracks.contains(track)) {

			int oldSize = _tracks.size();
			_tracks.add(track);

			firePropertyChange(LISTCHANGE_CHANGED_PROPERTY, Integer.valueOf(oldSize),
					Integer.valueOf(_tracks.size()));
		}
	}

	/**
	 * Removes a track from this pool
	 * 
	 * @param track
	 *            to be removed.
	 */
	public void remove(Track track) {
		if (_tracks.contains(track)) {

			int oldSize = _tracks.size();
			_tracks.remove(track);

			firePropertyChange(LISTCHANGE_CHANGED_PROPERTY, Integer.valueOf(oldSize),
					Integer.valueOf(_tracks.size()));
		}
	}

	public List<Track> getTracks() {
		// Return a copy to protect the internal list
		return new ArrayList<Track>(_tracks);
	}

	/**
	 * Request track length from one of the other tracks in this pool. Other tracks in the same pool may have their
	 * length shortened or lengthened by this operation.
	 * 
	 * @param track
	 *            the track requesting additional length
	 * @param length
	 *            the amount of track length requested
	 * @return true if successful
	 */
	public boolean requestTrackLength(Track track, int length) {
		int additionalLength = track.getUsedLength() + track.getReserved() + length
				- track.getLength();

		List<Track> tracks = getTracks();

		for (int i = 0; i < tracks.size(); i++) {
			Track t = tracks.get(i);
			// note that the reserved track length can be both positive and
			// negative, that's the reason
			// for the second check that doesn't include the reserve, this
			// prevent overloading.
			if (t != track
					&& (t.getUsedLength() + t.getReserved() + additionalLength) <= t.getLength()
					&& (t.getLength() - additionalLength) >= t.getMinimumLength()) {
				// allow overloading. Even tracks out of pools experience
				// overloading.
				// && (t.getUsedLength() + additionalLength) <= t.getLength()){
				log.debug("Increasing track (" + track.getName() + ") length (" + additionalLength
						+ ") decreasing (" + t.getName() // NOI18N
						+ ")");
				t.setLength(t.getLength() - additionalLength);
				track.setLength(track.getLength() + additionalLength);
				return true;
			}
		}
		return false;
	}
}
