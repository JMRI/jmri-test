package org.jmri.application.decoderpro;

import org.openide.modules.OnStart;

/**
 * This method is in a separate class because the run method must be run from an
 * object with a no-arguments constructor.
 *
 * @author rhwood
 */
@OnStart
public class Initializer implements Runnable {

    @Override
    public void run() {
        String[] args = {};
        DecoderPro.preInit(args);
        (new DecoderPro(args)).start();
    }

}
