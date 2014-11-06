package org.jmri.application.trainpro;

import org.jmri.application.JmriApplication;
import org.openide.util.Lookup;
import org.openide.windows.OnShowing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initialize TrainPro when running with a full GUI.
 *
 * @author rhwood
 */
@OnShowing
public class ShowTrainPro implements Runnable {

    private final static Logger log = LoggerFactory.getLogger(ShowTrainPro.class);

    @Override
    public void run() {
        log.info("Running in a GUI environment.");
        Lookup.getDefault().lookup(JmriApplication.class).show();
    }
}
