package org.jmri.application;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import jmri.Application;
import jmri.IdTagManager;
import jmri.InstanceManager;
import jmri.NamedBeanHandleManager;
import jmri.UserPreferencesManager;
import jmri.configurexml.ConfigXmlManager;
import jmri.configurexml.swing.DialogErrorHandler;
import jmri.implementation.AbstractShutDownTask;
import jmri.jmrit.display.layoutEditor.BlockValueFile;
import jmri.jmrit.revhistory.FileHistory;
import jmri.jmrit.signalling.EntryExitPairs;
import jmri.managers.DefaultIdTagManager;
import jmri.managers.DefaultShutDownManager;
import jmri.managers.DefaultUserMessagePreferences;
import jmri.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide JMRI application-level support to JMRI NetBeans modules
 *
 * Since JMRI applications no longer control the main() method when run within a
 * NetBeans RCP, this class provides JMRI-specific application-level support by
 * starting JMRI managers, setting titles, and similar functions.
 *
 * @author rhwood
 */
public final class JmriApplication {

    private static JmriApplication application = null;
    private String title;
    private String configFile;
    private Boolean started = false;
    private Boolean shown = false;
    private Boolean stopped = false;
    // Since this is a NetBeans-only class, Logger could be java.util.logging.Logger
    // but use the SLF4J Logger for commonality with rest of JMRI
    private static final Logger log = LoggerFactory.getLogger(JmriApplication.class);

    private JmriApplication(String title) {
        this.title = title;
        // need to watch CLI arguments as well
        if (System.getProperty("org.jmri.Apps.configFilename") != null) {
            this.configFile = System.getProperty("org.jmri.Apps.configFilename");
        } else {
            // if title is "JMRI App", configFile is "JMRIAppConfig.xml"
            this.configFile = title.replaceAll(" ", "") + "Config.xml";
        }
        log.info("Using config file {}", this.configFile);
        // TODO: better logging handling
        try {

            LogManager.getLogManager().readConfiguration();
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(JmriApplication.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            java.util.logging.Logger.getLogger(JmriApplication.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static JmriApplication getApplication(String title) throws IllegalAccessException {
        if (application == null) {
            application = new JmriApplication(title);
        } else {
            throw new IllegalAccessException();
        }
        return application;
    }

    public static JmriApplication getApplication() throws NullPointerException {
        if (application == null) {
            throw new NullPointerException();
        }
        return application;
    }

    public void onStart() {
        if (!started) {
            started = true;
            this.startManagers();
        }
    }

    public void onShown() {
        if (!shown) {
            shown = true;
        }
    }

    public void onStop() {
        if (!stopped) {
            stopped = true;
        }
    }

    protected void startManagers() {
        // Get base configuration
        ConfigXmlManager cm = new ConfigXmlManager();
        FileUtil.createDirectory(FileUtil.getUserFilesPath());
        InstanceManager.setConfigureManager(cm);
        cm.setPrefsLocation(new File(this.configFile));
        log.debug("config manager started");
        // Set the Config Manager error handler
        ConfigXmlManager.setErrorHandler(new DialogErrorHandler());
        // Start a shutdown manager
        InstanceManager.setShutDownManager(new DefaultShutDownManager());
        this.registerDefaultShutDownTasks();
        // Start a history manager
        InstanceManager.store(new FileHistory(), FileHistory.class);
        // record startup
        InstanceManager.getDefault(FileHistory.class).addOperation("app", Application.getApplicationName(), null);
        // Start a user preferences manager
        InstanceManager.store(DefaultUserMessagePreferences.getInstance(), UserPreferencesManager.class);
        // Start the abstract action model that allows items to be added to the, both
        // CreateButton and Perform Action Model use a common Abstract class
        // TODO replace with NBM-based models
        // InstanceManager.store(new CreateButtonModel(), CreateButtonModel.class);
        // Start preference manager
        // NOTE replace with NBM/JMRI-based model
        // InstanceManager.setTabbedPreferences(new TabbedPreferences());
        // Start the named bean handler
        InstanceManager.store(new NamedBeanHandleManager(), NamedBeanHandleManager.class);
        // Start an IdTag manager
        InstanceManager.store(new DefaultIdTagManager(), IdTagManager.class);
        //Start Entry Exit Pairs Manager
        InstanceManager.store(new EntryExitPairs(), EntryExitPairs.class);
    }

    protected void registerDefaultShutDownTasks() {
        InstanceManager.shutDownManagerInstance().register(new AbstractShutDownTask("Writing Blocks") {
            public boolean execute() {
                // Save block values prior to exit, if necessary
                log.debug("Start writing block info");
                try {
                    new BlockValueFile().writeBlockValues();
                } //catch (org.jdom.JDOMException jde) { log.error("Exception writing blocks: "+jde); }
                catch (java.io.IOException ioe) {
                    log.error("Exception writing blocks", ioe);
                }

                // continue shutdown
                return true;
            }
        });
    }
}
