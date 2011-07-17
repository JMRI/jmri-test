// NullAudioSource.java

package jmri.jmrit.audio;

import javax.vecmath.Vector3f;

/**
 * Null audio system implementation of the Audio Source sub-class.
 * <P>
 * For now, no system-specific implementations are forseen - this will remain
 * internal-only
 *
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under
 * the terms of version 2 of the GNU General Public License as published
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * for more details.
 * <P>
 *
 * @author Matthew Harris  copyright (c) 2009
 * @version $Revision: 1.3 $
 */
public class NullAudioSource extends AbstractAudioSource {

    /**
     * True if we've been initialised
     */
    private boolean _initialised = false;

    /**
     * Constructor for new NullAudioSource with system name
     *
     * @param systemName AudioSource object system name (e.g. IAS1)
     */
    public NullAudioSource(String systemName) {
        super(systemName);
        if (log.isDebugEnabled()) log.debug("New NullAudioSource: "+systemName);
        _initialised = init();
    }

    /**
     * Constructor for new NullAudioSource with system name and user name
     *
     * @param systemName AudioSource object system name (e.g. IAS1)
     * @param userName AudioSource object user name
     */
    public NullAudioSource(String systemName, String userName) {
        super(systemName, userName);
        if (log.isDebugEnabled()) log.debug("New NullAudioSource: "+userName+" ("+systemName+")");
        _initialised = init();
    }

    boolean bindAudioBuffer(AudioBuffer audioBuffer) {
        // Don't actually need to do anything specific here
        if (log.isDebugEnabled()) log.debug("Bind NullAudioSource (" + this.getSystemName() +
                                            ") to NullAudioBuffer (" + audioBuffer.getSystemName() + ")");
        return true;
    }

    /**
     * Initialise this AudioSource
     *
     * @return True if initialised
     */
    private boolean init() {
        return true;
    }

    protected void changePosition(Vector3f pos) {
        // Do nothing
    }

    protected void doPlay() {
        if (log.isDebugEnabled()) log.debug("Play NullAudioSource (" + this.getSystemName() + ")");
        if (_initialised && isBound()) {
            doRewind();
            doResume();
        }
    }

    protected void doStop() {
        if (log.isDebugEnabled()) log.debug("Stop NullAudioSource (" + this.getSystemName() + ")");
        if (_initialised && isBound()) {
            // do nothing
            doRewind();
        }
    }

    protected void doPause() {
        if (log.isDebugEnabled()) log.debug("Pause NullAudioSource (" + this.getSystemName() + ")");
        if (_initialised && isBound()) {
            // do nothing
        }
        this.setState(STATE_STOPPED);
    }

    protected void doResume() {
        if (log.isDebugEnabled()) log.debug("Resume NullAudioSource (" + this.getSystemName() + ")");
        if (_initialised && isBound()) {
            // do nothing
        }
        this.setState(STATE_PLAYING);
    }

    protected void doRewind() {
        if (log.isDebugEnabled()) log.debug("Rewind NullAudioSource (" + this.getSystemName() + ")");
        if (_initialised && isBound()) {
            // do nothing
        }
        this.setState(STATE_STOPPED);
    }

    protected void doFadeIn() {
        if (log.isDebugEnabled()) log.debug("Fade-in JoalAudioSource (" + this.getSystemName() + ")");
        if (_initialised && isBound()) {
            doPlay();
        }
    }

    protected void doFadeOut() {
        if (log.isDebugEnabled()) log.debug("Fade-out JoalAudioSource (" + this.getSystemName() + ")");
        if (_initialised && isBound()) {
            doStop();
        }
    }

    protected void cleanUp() {
        if (log.isDebugEnabled()) log.debug("Cleanup NullAudioSource (" + this.getSystemName() + ")");
        this.dispose();
    }

    protected void calculateGain() {
        // do nothing
    }

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(NullAudioSource.class.getName());

}

/* $(#)NullAudioSource.java */