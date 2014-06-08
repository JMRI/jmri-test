package org.jmri.app.trainpro;

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
        // Do interesting things here
    }
}
