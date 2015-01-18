package org.jmri.application;

import apps.AppsBase;
import apps.SystemConsole;
import apps.gui3.Apps3;
import javax.swing.JComponent;
import jmri.InstanceManager;
import jmri.ShutDownManager;
import org.jmri.managers.NetBeansShutDownManager;
import org.openide.util.Lookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Like {@link apps.gui3.Apps3}, but with no splash screen.
 *
 * @author rhwood
 */
public abstract class Gui3Application extends Apps3 {

    private static final Logger log = LoggerFactory.getLogger(Gui3Application.class);

    /**
     * Initial actions before frame is created, invoked in the applications
     * main() routine.
     *
     * @param applicationName
     */
    static public void preInit(String applicationName) {
        AppsBase.preInit(applicationName);

        // Initialise system console
        // Put this here rather than in apps.AppsBase as this is only relevant
        // for GUI applications - non-gui apps will use STDOUT & STDERR
        SystemConsole.create();

        setButtonSpace();

    }

    /**
     * Create and initialize the application object.
     *
     * Expects initialization from preInit() to already be done.
     *
     * @param applicationName
     * @param configFileDef
     * @param args
     */
    public Gui3Application(String applicationName, String configFileDef, String[] args) {
        super(applicationName, configFileDef, args);
    }

    /**
     * Provide access to a place where applications can expect the configuration
     * code to build run-time buttons.
     *
     * @see apps.CreateButtonPanel
     * @return null if no such space exists
     */
    static public JComponent buttonSpace() {
        return _buttonSpace;
    }
    static JComponent _buttonSpace = null;

    /**
     * Final actions before releasing control of app to user
     */
    @Override
    protected void start() {
        super.start();
    }

    @Override
    protected void installShutDownManager() {
        InstanceManager.store(Lookup.getDefault().lookup(NetBeansShutDownManager.class), ShutDownManager.class);
    }
}
