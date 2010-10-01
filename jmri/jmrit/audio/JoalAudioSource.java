// JoalAudioSource.java

package jmri.jmrit.audio;

import javax.vecmath.Vector3f;
import net.java.games.joal.AL;

/**
 * JOAL implementation of the Audio Source sub-class.
 * <p>
 * For now, no system-specific implementations are forseen - this will remain
 * internal-only
 * <br><br><hr><br><b>
 *    This software is based on or using the JOAL Library available from
 *    <a href="http://joal.dev.java.net/">http://joal.dev.java.net/</a>
 * </b><br><br>
 *    JOAL License:
 * <br><i>
 * Copyright (c) 2003 Sun Microsystems, Inc. All  Rights Reserved.
 * <br>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <br>
 * -Redistribution of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * <br>
 * -Redistribution in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <br>
 * Neither the name of Sun Microsystems, Inc. or the names of contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * <br>
 * This software is provided "AS IS," without a warranty of any kind.
 * ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MIDROSYSTEMS, INC. ("SUN") AND ITS
 * LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A
 * RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 * IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT
 * OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR
 * PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY,
 * ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS
 * BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * <br>
 * You acknowledge that this software is not designed or intended for use in the
 * design, construction, operation or maintenance of any nuclear facility.
 * <br><br><br></i>
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under
 * the terms of version 2 of the GNU General Public License as published
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * for more details.
 * <p>
 *
 * @author Matthew Harris  copyright (c) 2009
 * @version $Revision: 1.5 $
 */
public class JoalAudioSource extends AbstractAudioSource {

    private static AL al = JoalAudioFactory.getAL();

    private boolean _initialised = false;

    private int[] _source = new int[1];

    private int[] _alState = new int[1];

    /**
     * Constructor for new JoalAudioSource with system name
     *
     * @param systemName AudioSource object system name (e.g. IAS1)
     */
    public JoalAudioSource(String systemName) {
        super(systemName);
        if (log.isDebugEnabled()) log.debug("New JoalAudioSource: "+systemName);
        _initialised = init();
    }

    /**
     * Constructor for new JoalAudioSource with system name and user name
     *
     * @param systemName AudioSource object system name (e.g. IAS1)
     * @param userName AudioSource object user name
     */
    public JoalAudioSource(String systemName, String userName) {
        super(systemName, userName);
        if (log.isDebugEnabled()) log.debug("New JoalAudioSource: "+userName+" ("+systemName+")");
        _initialised = init();
    }

    /**
     * Initialise this AudioSource
     *
     * @return True if initialised
     */
     private boolean init() {
        // Generate the AudioSource
        al.alGenSources(1, _source, 0);
        if (JoalAudioFactory.checkALEError()) {
            log.warn("Error creating JoalSource (" + this.getSystemName() + ")");
            _source = null;
            return false;
        }

        return true;
    }

    public boolean bindAudioBuffer(AudioBuffer audioBuffer) {
        // First check we've been initialised
        if (!_initialised) {
            return false;
        }
        
        // Bind this AudioSource to the specified AudioBuffer
        al.alSourcei(_source[0], AL.AL_BUFFER, ((JoalAudioBuffer)audioBuffer).getDataStorageBuffer()[0]);
        if (JoalAudioFactory.checkALEError()) {
            log.warn("Error binding JoalSource (" + this.getSystemName() + ") to AudioBuffer (" + this.getAssignedBufferName() +")");
            return false;
        }
        
        if (log.isDebugEnabled()) log.debug("Bind JoalAudioSource (" + this.getSystemName() +
                                            ") to JoalAudioBuffer (" + audioBuffer.getSystemName() + ")");
        return true;
    }

    protected void changePosition(Vector3f pos) {
        if (_initialised) {
            al.alSource3f(_source[0], AL.AL_POSITION, pos.x, pos.y, pos.z);
            if (JoalAudioFactory.checkALEError()) {
                log.warn("Error updating position of JoalAudioSource (" + this.getSystemName() + ")");
            }
        }
    }

    @Override
    public void setPositionRelative(boolean relative) {
        super.setPositionRelative(relative);
        if (_initialised) {
            al.alSourcei(_source[0], AL.AL_SOURCE_RELATIVE, relative?AL.AL_TRUE:AL.AL_FALSE);
            if (JoalAudioFactory.checkALEError()) {
                log.warn("Error updating relative position property of JoalAudioSource (" + this.getSystemName() + ")");
            }
        }
    }

    @Override
    public void setVelocity(Vector3f vel) {
        super.setVelocity(vel);
        if (_initialised) {
            al.alSource3f(_source[0], AL.AL_VELOCITY, vel.x, vel.y, vel.z);
            if (JoalAudioFactory.checkALEError()) {
                log.warn("Error updating velocity of JoalAudioSource (" + this.getSystemName() + ")");
            }
        }
    }

    @Override
    public void setGain(float gain) {
        super.setGain(gain);
        if (_initialised) {
            calculateGain();
        }
    }

    @Override
    public void setPitch(float pitch) {
        super.setPitch(pitch);
        if (_initialised) {
            al.alSourcef(_source[0], AL.AL_PITCH, pitch);
            if (JoalAudioFactory.checkALEError()) {
                log.warn("Error updating pitch of JoalAudioSource (" + this.getSystemName() + ")");
            }
        }
    }

    @Override
    public void setReferenceDistance(float referenceDistance) {
        super.setReferenceDistance(referenceDistance);
        if (_initialised) {
            al.alSourcef(_source[0], AL.AL_REFERENCE_DISTANCE, referenceDistance);
            if (JoalAudioFactory.checkALEError()) {
                log.warn("Error updating reference distance of JoalAudioSource (" + this.getSystemName() + ")");
            }
        }
    }

    @Override
    public void setMaximumDistance(float maximumDistance) {
        super.setMaximumDistance(maximumDistance);
        if (_initialised) {
            al.alSourcef(_source[0], AL.AL_MAX_DISTANCE, maximumDistance);
            if (JoalAudioFactory.checkALEError()) {
                log.warn("Error updating maximum distance of JoalAudioSource (" + this.getSystemName() + ")");
            }
        }
    }

    @Override
    public void setRollOffFactor(float rollOffFactor) {
        super.setRollOffFactor(rollOffFactor);
        if (_initialised) {
            al.alSourcef(_source[0], AL.AL_ROLLOFF_FACTOR, rollOffFactor);
            if (JoalAudioFactory.checkALEError()) {
                log.warn("Error updating roll-off factor of JoalAudioSource (" + this.getSystemName() + ")");
            }
        }
    }

    @Override
    public void setDopplerFactor(float dopplerFactor) {
        super.setDopplerFactor(dopplerFactor);
        if (_initialised) {
            al.alSourcef(_source[0], AL.AL_DOPPLER_FACTOR, dopplerFactor);
        }
    }

    @Override
    public int getState() {
        int old = _alState[0];
        al.alGetSourcei(_source[0], AL.AL_SOURCE_STATE, _alState, 0);
        if (_alState[0] != old) {
            if (_alState[0] == AL.AL_PLAYING) {
                this.setState(STATE_PLAYING);
            } else {
                this.setState(STATE_STOPPED);
            }
        }
        return super.getState();
    }

    @Override
    public void stateChanged(int oldState) {
        super.stateChanged(oldState);
        if (_initialised){
            al.alSourcef(_source[0], AL.AL_PITCH, this.getPitch());
            al.alSourcef(_source[0], AL.AL_GAIN, this.getGain());
            al.alSource3f(_source[0], AL.AL_POSITION, this.getCurrentPosition().x, this.getCurrentPosition().y, this.getCurrentPosition().z);
            al.alSource3f(_source[0], AL.AL_VELOCITY, this.getVelocity().x, this.getVelocity().y, this.getVelocity().z);
            al.alSourcei(_source[0], AL.AL_LOOPING, this.isLooped() ? AL.AL_TRUE : AL.AL_FALSE);
            if (JoalAudioFactory.checkALEError()) {
                log.warn("Error updating JoalAudioSource (" + this.getSystemName() + ")");
            }
        } else {
            _initialised = init();
        }
    }

    protected void doPlay() {
        if (log.isDebugEnabled()) log.debug("Play JoalAudioSource (" + this.getSystemName() + ")");
        if (_initialised && isBound()) {
            doRewind();
            doResume();
        }
    }

    protected void doStop() {
        if (log.isDebugEnabled()) log.debug("Stop JoalAudioSource (" + this.getSystemName() + ")");
        if (_initialised && isBound()) {
            al.alSourceStop(_source[0]);
            doRewind();
        }
        this.setState(STATE_STOPPED);
    }

    protected void doPause() {
        if (log.isDebugEnabled()) log.debug("Pause JoalAudioSource (" + this.getSystemName() + ")");
        if (_initialised && isBound()) {
            al.alSourcePause(_source[0]);
        }
        this.setState(STATE_STOPPED);
    }

    protected void doResume() {
        if (log.isDebugEnabled()) log.debug("Resume JoalAudioSource (" + this.getSystemName() + ")");
        if (_initialised && isBound()) {
            calculateGain();
            al.alSourcei(_source[0], AL.AL_LOOPING, this.isLooped() ? AL.AL_TRUE : AL.AL_FALSE);
            al.alSourcePlay(_source[0]);
            int numLoops = this.getNumLoops();
            if (numLoops>0) {
                if (log.isDebugEnabled())
                    log.debug("Create LoopThread for JoalAudioSource " + this.getSystemName());
                AudioSourceLoopThread aslt = new AudioSourceLoopThread(this, numLoops);
                aslt.start();
            }
        }
        this.setState(STATE_PLAYING);
    }

    protected void doRewind() {
        if (log.isDebugEnabled()) log.debug("Rewind JoalAudioSource (" + this.getSystemName() + ")");
        if (_initialised && isBound()) {
            al.alSourceRewind(_source[0]);
        }
    }

    protected void doFadeIn() {
        if (log.isDebugEnabled()) log.debug("Fade-in JoalAudioSource (" + this.getSystemName() + ")");
        if (_initialised && isBound()) {
            doPlay();
            AudioSourceFadeThread asft = new AudioSourceFadeThread(this);
            asft.start();
        }
    }

    protected void doFadeOut() {
        if (log.isDebugEnabled()) log.debug("Fade-out JoalAudioSource (" + this.getSystemName() + ")");
        if (_initialised && isBound()) {
            AudioSourceFadeThread asft = new AudioSourceFadeThread(this);
            asft.start();
        }
    }

    protected void cleanUp() {
        if (log.isDebugEnabled()) log.debug("Cleanup JoalAudioSource (" + this.getSystemName() + ")");
        if (_initialised && isBound()) {
            al.alSourceStop(_source[0]);
            al.alDeleteSources(1, _source, 0);
            this._source = null;
            log.debug("...done cleanup");
        }
        this.dispose();
    }

    protected void calculateGain() {

        // Adjust gain based on master gain for this source and any
        // calculated fade gains
        float currentGain = this.getGain() * this.getFadeGain();

        // If playing, update the gain
        if (_initialised) {
            al.alSourcef(_source[0], AL.AL_GAIN, currentGain);
            if (JoalAudioFactory.checkALEError()) {
                log.warn("Error updating gain setting of JoalAudioSource (" + this.getSystemName() + ")");
            }
            if (log.isDebugEnabled())
                log.debug("Set current gain of JoalAudioSource " + this.getSystemName() + " to " + currentGain);
        }
    }

    /**
     * Internal method to return a reference to the OpenAL source buffer
     * @return source buffer
     */
    private int[] getSourceBuffer() {
        return this._source;
    }

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(JoalAudioSource.class.getName());

    /**
     * An internal class used to create a new thread to monitor looping as,
     * unlike JavaSound, OpenAL (and, therefore, JOAL) do not provide a
     * convenient method to loop a sound a specific number of times.
     */
    private static class AudioSourceLoopThread extends AbstractAudioThread {

        /**
         * Number of times to loop this source
         */
        private int numLoops;

        /**
         * Reference to the OpenAL source buffer
         */
        private int[] sourceBuffer;

        /**
         * Constructor that takes handle to looping AudioSource to monitor
         *
         * @param audioSource looping AudioSource to monitor
         * @param numLoops number of loops for this AudioSource to make
         */
        AudioSourceLoopThread(JoalAudioSource audioSource, int numLoops) {
            super();
            this.setName("loopsrc-"+super.getName());
            this.sourceBuffer = audioSource.getSourceBuffer();
            this.numLoops = numLoops;
            if (log.isDebugEnabled()) log.debug("Created AudioSourceLoopThread for AudioSource " + audioSource.getSystemName()
                    + " loopcount " + this.numLoops);
        }

        /**
         * Main processing loop
         */
        @Override
        public void run() {

            // Current loop count
            int loopCount = 0;

            // Previous position
            float oldPos = 0;

            // Current position
            float[] newPos = new float[1];

            // Turn on looping
            JoalAudioSource.al.alSourcei(sourceBuffer[0], AL.AL_LOOPING, AL.AL_TRUE);

            while (!dying()) {

                // Determine current position
                JoalAudioSource.al.alGetSourcef(sourceBuffer[0], AL.AL_SEC_OFFSET, newPos,0);

                // Check if it is smaller than the previous position
                // If so, we've looped so increment the loop counter
                if (oldPos > newPos[0]) {
                    loopCount++;
                    log.debug("Loop count " + loopCount);
                }
                oldPos = newPos[0];

                // Check if we've performed sufficient iterations
                if (loopCount >= numLoops) {
                    die();
                }
                
                // sleep for a while so as not to overload CPU
                snooze(20);
            }

            // Turn off looping
            JoalAudioSource.al.alSourcei(sourceBuffer[0], AL.AL_LOOPING, AL.AL_FALSE);

            // Finish up
            if (log.isDebugEnabled()) log.debug("Clean up thread " + this.getName());
            cleanup();
        }

        /**
         * Shuts this thread down and clears references to created objects
         */
        @Override
        protected void cleanup() {
            // Thread is to shutdown
            die();

            // Clear references to objects
            this.sourceBuffer = null;

            // Finalise cleanup in super-class
            super.cleanup();
        }
    }
}

/* $(#)JoalAudioSource.java */