package org.jmri.application.trainpro;

import java.awt.GraphicsEnvironment;
import org.jmri.application.JmriApplication;
import org.openide.modules.OnStart;
import org.openide.util.Lookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initialize TrainPro when running in a headless mode.
 *
 * @author rhwood
 */
@OnStart
public class StartTrainPro implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(StartTrainPro.class);

    @Override
    public void run() {
        // Get the profiles before @OnShowing is triggered, so that
        // when the main window is opened, we can begin populating it
        Lookup.getDefault().lookup(JmriApplication.class).start();
        if (GraphicsEnvironment.isHeadless()) {
            log.info("Running in a headless environment.");
            // Do interesting things here
        }
    }
}
