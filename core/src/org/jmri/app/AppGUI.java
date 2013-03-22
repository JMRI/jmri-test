/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jmri.app;

import apps.AppsBase;
import apps.SystemConsole;
import apps.gui3.Apps3;
import javax.swing.JComponent;
import org.apache.log4j.Logger;

/**
 * Like Apps3, but with no splash screen.
 * 
 * @author rhwood
 */
public abstract class AppGUI extends Apps3 {

    /**
     * Initial actions before frame is created, invoked in the applications
     * main() routine.
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
     * Create and initialize the application object. <p> Expects initialization
     * from preInit() to already be done.
     */
    public AppGUI(String applicationName, String configFileDef, String[] args) {
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

    static Logger log = Logger.getLogger(apps.gui3.Apps3.class.getName());
}
