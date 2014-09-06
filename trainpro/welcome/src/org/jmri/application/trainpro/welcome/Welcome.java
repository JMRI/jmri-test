/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

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
