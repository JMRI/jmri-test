package org.jmri.application.panelpro;

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
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "PanelPro");
        }
        ClassicApplication.setConfigFilename("PanelProConfig2.xml", args);
        JmriJFrame f = new JmriJFrame("PanelPro");
        PanelPro pp = new PanelPro(f);
        ClassicApplication.createFrame(pp, f);
    }
    
}
