// DefaultAudioManager.java

package jmri.jmrit.audio;

import jmri.Audio;
import jmri.AudioException;
import jmri.InstanceManager;
import jmri.ShutDownTask;
import jmri.implementation.QuietShutDownTask;
import jmri.managers.AbstractAudioManager;

/**
 * Provide the concrete implementation for the Internal Audio Manager.
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
 * @author  Matthew Harris  copyright (c) 2009
 * @version $Revision: 1.2 $
 */
public class DefaultAudioManager extends AbstractAudioManager {

    private static int countListeners = 0;
    private static int countSources = 0;
    private static int countBuffers = 0;

    /**
     * Reference to the currently active AudioFactory
     */
    private static AudioFactory activeAudioFactory = null;

    private static boolean _initialised = false;

    ShutDownTask audioShutDownTask;

    public char systemLetter() { return 'I'; }

    protected Audio createNewAudio(String systemName, String userName) throws AudioException {

        Audio a = null;

        switch (systemName.charAt(2)) {

            case Audio.BUFFER: {
                if (countBuffers >= MAX_BUFFERS) {
                    log.error("Maximum number of buffers reached ("+countBuffers+") "+MAX_BUFFERS);
                    throw new AudioException("Maximum number of buffers reached ("+countBuffers+") "+MAX_BUFFERS);
                }
                countBuffers++;
                a = activeAudioFactory.createNewBuffer(systemName, userName);
                break;
            }
            case Audio.LISTENER: {
                if (countListeners >= MAX_LISTENERS) {
                    log.error("Maximum number of Listeners reached ("+countListeners+") "+MAX_LISTENERS);
                    throw new AudioException("Maximum number of Listeners reached ("+countListeners+") "+MAX_LISTENERS);
                }
                countListeners++;
                a = activeAudioFactory.createNewListener(systemName, userName);
                break;
            }
            case Audio.SOURCE: {
                if (countSources >= MAX_SOURCES) {
                    log.error("Maximum number of Sources reached ("+countSources+") "+MAX_SOURCES);
                    throw new AudioException("Maximum number of Sources reached ("+countSources+") "+MAX_SOURCES);
                }
                countSources++;
                a = activeAudioFactory.createNewSource(systemName, userName);
                break;
            }
        }

        return a;
    }

    /**
     * Method used to initialise the manager
     */
    public void init() {
        if(!_initialised) {
            // First try to initialise JOAL
            activeAudioFactory = new JoalAudioFactory();

            // If JOAL fails, fall-back to JavaSound
            log.debug("Try to initialise JoalAudioFactory");
            if (!activeAudioFactory.init()) {
                activeAudioFactory = new JavaSoundAudioFactory();
                log.debug("Try to initialise JavaSoundAudioFactory");
                // Finally, if JavaSound fails, fall-back to a Null sound system
                if (!activeAudioFactory.init()) {
                    activeAudioFactory = new NullAudioFactory();
                    log.debug("Try to initialise NullAudioFactory");
                    activeAudioFactory.init();
                }
            }

            // Create default Listener and save in map
            try {
                Audio s = createNewAudio("IAL$", "Default Audio Listener");
                register(s);
            } catch (AudioException ex) {
                log.error("Error creating Default Audio Listener: " + ex);
            }

            // Finally, create and register a shutdown task to ensure clean exit
            if (audioShutDownTask==null) {
                audioShutDownTask = new QuietShutDownTask("AudioFactory Shutdown") {
                    @Override
                    public boolean doAction(){
                        InstanceManager.audioManagerInstance().cleanUp();
                        return true;
                    }
                };
            }
            if (InstanceManager.shutDownManagerInstance() !=null)
                InstanceManager.shutDownManagerInstance().register(audioShutDownTask);

            _initialised = true;
            if (log.isDebugEnabled()) log.debug("Initialised AudioFactory type: " + activeAudioFactory.getClass().getSimpleName());
        }
    }

    public void cleanUp() {
        // Shutdown AudioFactory and close the output device
        log.info("Shutting down active AudioFactory");
        activeAudioFactory.cleanup();
    }

    public AudioFactory getActiveAudioFactory() {
        return activeAudioFactory;
    }

    /**
     * Return the current instance of this object.
     * <p>
     * If not existing, create a new instance.
     *
     * @return reference to currently active AudioManager
     */
    public static DefaultAudioManager instance() {
        if (_instance == null) {
            _instance = new DefaultAudioManager();
            _instance.init();
        }
        return _instance;
    }

    private static DefaultAudioManager _instance;

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DefaultAudioManager.class.getName());

}

/* @(#)DefaultAudioManager.java */
