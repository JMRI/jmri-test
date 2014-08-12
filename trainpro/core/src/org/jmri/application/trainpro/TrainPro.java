package org.jmri.application.trainpro;

import org.jmri.application.JmriApplication;
import org.openide.util.Exceptions;
import org.openide.windows.OnShowing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initialize TrainPro when running with a full GUI.
 *
 * @author rhwood
 */
@OnShowing
public class TrainPro implements Runnable {

    private final static Logger log = LoggerFactory.getLogger(TrainPro.class);

    @Override
    public void run() {
        log.info("Running in a GUI environment.");
        try {
            // Do interesting things here
            JmriApplication.getApplication("JMRI TrainPro").show();
        } catch (IllegalAccessException | IllegalArgumentException ex) {
            log.error("Unable to start JMRI.", ex);
            Exceptions.printStackTrace(ex);
            // TODO: display error and initiate shutdown
        }
    }
}
