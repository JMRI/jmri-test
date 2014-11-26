package org.jmri.application.decoderpro;

import javax.swing.UIManager;
import jmri.util.JmriJFrame;
import jmri.util.SystemType;
import org.jmri.application.ClassicApplication;
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
        ClassicApplication.initLogging();
        if (SystemType.isMacOSX()) {
            if (UIManager.getLookAndFeel().isNativeLookAndFeel()) {
                System.setProperty("apple.laf.useScreenMenuBar", "true");
            }
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "DecoderPro");
        }
        ClassicApplication.setConfigFilename("DecoderProConfig2.xml", args);
        JmriJFrame f = new JmriJFrame("DecoderPro");
        DecoderPro dp = new DecoderPro(f);
        ClassicApplication.createFrame(dp, f);
    }
    
}
