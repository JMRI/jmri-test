package org.jmri.application.trainpro.welcome;

import org.openide.windows.OnShowing;

/**
 *
 * @author rhwood
 */
@OnShowing
public class Welcome implements Runnable {

    @Override
    public void run() {
        WelcomeTopComponent.checkOpen();
    }

}
