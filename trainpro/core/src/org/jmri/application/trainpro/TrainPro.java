package org.jmri.application.trainpro;

import org.jmri.application.JmriApplication;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Randall Wood <randall.h.wood@alexandriasoftware.com>
 */
@ServiceProvider(service = JmriApplication.class)
public class TrainPro extends JmriApplication {

    public TrainPro() {
        super("JMRI TrainPro");
    }
}
