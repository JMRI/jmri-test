/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jmri.app.decoderpro;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import jmri.util.JmriJFrame;
import jmri.util.SystemType;
import org.jmri.app.AppClassic;
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
        AppClassic.initLogging();
        Logger.getGlobal().setLevel(Level.FINE);
        if (SystemType.isMacOSX()) {
            if (UIManager.getLookAndFeel().isNativeLookAndFeel()) {
                System.setProperty("apple.laf.useScreenMenuBar", "true");
            }
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "DecoderPro");
        }
        AppClassic.setConfigFilename("DecoderProConfig2.xml", args);
        JmriJFrame f = new JmriJFrame("DecoderPro");
        DecoderPro dp = new DecoderPro(f);
        AppClassic.createFrame(dp, f);

    }
    
}
