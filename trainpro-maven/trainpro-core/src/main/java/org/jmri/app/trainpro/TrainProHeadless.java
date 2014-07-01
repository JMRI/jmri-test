package org.jmri.app.trainpro;

import java.awt.GraphicsEnvironment;
import org.jmri.application.JmriApplication;
import org.openide.modules.OnStart;
import org.openide.util.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initialize TrainPro when running in a headless mode.
 *
 * @author rhwood
 */
@OnStart
public class TrainProHeadless implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(TrainProHeadless.class);

    @Override
    public void run() {
        // Only take action if headless, otherwise TrainPro will take action
        // when the splash screen is displayed
        if (GraphicsEnvironment.isHeadless()) {
            log.info("Running in a headless environment.");
            try {
            // Do interesting things here
            JmriApplication.getApplication("JMRI TrainPro").start();
        } catch (IllegalAccessException | IllegalArgumentException ex) {
            log.error("Unable to start JMRI.", ex);
            Exceptions.printStackTrace(ex);
            // TODO: display error and initiate shutdown
        }
        }
    }
}
