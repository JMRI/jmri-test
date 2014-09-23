package org.jmri.application;

import apps.CreateButtonModel;
import apps.gui3.TabbedPreferences;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.logging.LogManager;
import javax.swing.JOptionPane;
import jmri.Application;
import jmri.ConfigureManager;
import jmri.IdTagManager;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.NamedBeanHandleManager;
import jmri.ShutDownManager;
import jmri.UserPreferencesManager;
import jmri.configurexml.ConfigXmlManager;
import jmri.configurexml.ErrorHandler;
import jmri.configurexml.swing.DialogErrorHandler;
import jmri.implementation.AbstractShutDownTask;
import jmri.jmrit.display.layoutEditor.BlockValueFile;
import jmri.jmrit.revhistory.FileHistory;
import jmri.jmrit.signalling.EntryExitPairs;
import jmri.managers.DefaultIdTagManager;
import jmri.managers.DefaultUserMessagePreferences;
import jmri.profile.Profile;
import jmri.profile.ProfileManager;
import jmri.profile.ProfileManagerDialog;
import jmri.util.FileUtil;
import jmri.web.server.WebServerManager;
import org.jmri.managers.NetBeansShutDownManager;
import org.openide.filesystems.FileObject;
import org.openide.modules.Places;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide JMRI application-level support to JMRI NetBeans modules
 *
 * Since JMRI applications no longer control the main() method when run within a
 * NetBeans RCP, this class provides JMRI-specific application-level support by
 * starting JMRI managers, setting titles, and similar functions.
 *
 * @author Randall Wood 2014
 */
public class JmriApplication {

    private static JmriApplication application = null;
    private String configFilename;
    private Boolean configLoaded = false;
    private String profileFilename;
    private Boolean started = false;
    private Boolean shown = false;
    private Boolean stopped = false;
    private static final Logger log = LoggerFactory.getLogger(JmriApplication.class);

    private JmriApplication(String title) throws IllegalAccessException, IllegalArgumentException {
        Application.setApplicationName(title);
        // configure logging first
        try {
            // TODO allow log file to be specified on CLI
            FileObject logConfig = org.openide.filesystems.FileUtil.toFileObject(new File(Places.getUserDirectory(), "logging.properties"));
            if (logConfig != null) {
                logConfig = org.openide.filesystems.FileUtil.getConfigFile("logging.properties");
            }
            if (logConfig != null) {
                logConfig = org.openide.filesystems.FileUtil.toFileObject((new File("logging.properties")).getCanonicalFile());
            }
            if (logConfig != null) {
                LogManager.getLogManager().readConfiguration(logConfig.getInputStream());
            } else {
                LogManager.getLogManager().readConfiguration();
            }
        } catch (IOException | SecurityException ex) {
            log.error("Unable to configure logging.", ex);
        }
        // need to watch CLI arguments as well
        if (System.getProperty("org.jmri.Apps.configFilename") != null) {
            this.configFilename = System.getProperty("org.jmri.Apps.configFilename");
        } else {
            // if title is "JMRI App", configFile is "JMRIAppConfig.xml"
            this.configFilename = title.replaceAll(" ", "") + "Config.xml";
        }
        log.info("Using config file {}", this.configFilename);
    }

    /**
     * Get the application object, creating it if needed.
     *
     * @param name - The application name
     * @return The object responsible for a JMRI application's life cycle
     * management.
     * @throws IllegalAccessException if the application object has been created
     * and title does not equal the application's current name.
     * @throws IllegalArgumentException if title is null
     * @see jmri.Application
     */
    public static JmriApplication getApplication(String name) throws IllegalAccessException, IllegalArgumentException {
        if (application == null) {
            application = new JmriApplication(name);
        } else if (name == null || !name.equals(Application.getApplicationName())) {
            throw new IllegalAccessException();
        }
        return application;
    }

    /**
     * Get the application object without attempting to create it if needed.
     *
     * @return The object responsible for a JMRI application's life cycle
     * management.
     * @throws NullPointerException if the application has not been created.
     */
    public static JmriApplication getApplication() throws NullPointerException {
        if (application == null) {
            throw new NullPointerException();
        }
        return application;
    }

    /**
     * Check if application is running in a headless environment.
     *
     * @return true if in a headless environment.
     * @see java.awt.GraphicsEnvironment#isHeadless()
     */
    public boolean isHeadless() {
        return GraphicsEnvironment.isHeadless();
    }

    /**
     * Triggered when the application is started.
     *
     * Any actions taken by this method, or by objects called in this method
     * must succeed in a headless environment, since there is no guarantee that
     * this method will be called in a GUI environment.
     *
     * Since this method is triggered before the main window is available,
     * overriding subclasses should note that the only thing this method does in
     * a GUI environment is get the current {@link jmri.profile.Profile}, and
     * that {@link #show() } performs all initialization in a GUI environment.
     *
     * @see #show()
     * @see org.openide.modules.OnStart
     */
    public void start() {
        if (!started) {
            started = true;
            this.getProfile();
            if (this.isHeadless()) {
                this.startManagers();
                if (!this.loadConfiguration()) {
                    // TODO handle failure to load configuration when headless at this point
                    // should we log an error, write it to STDOUT, and quit now?
                }
                // Always run the WebServer when headless
                WebServerManager.getWebServer().start();
            }
        }
    }

    /**
     * Triggered when the application's main window is drawn.
     *
     * Actions taken by this method are in a GUI environment.
     *
     * @see #start()
     * @see org.openide.windows.OnShowing
     */
    public void show() {
        if (!shown) {
            shown = true;
            this.start();
            this.startManagers();
            // TODO add user's buttons to toolbar
            if (!this.loadConfiguration()) {
                // TODO handle failure to load configuration by displaying a
                // first time use wizard and the options
            }
            this.initilizePreferencesUI();
            // do any other GUI things that we might need to do
        }
    }

    /**
     * Triggered when the application is exiting.
     *
     * Any actions taken by this method, or by objects called in this method
     * must succeed in a headless environment, since there is no guarantee that
     * this method will be called in a GUI environment.
     *
     * @see org.openide.modules.OnStop
     */
    public void stop() {
        if (!stopped) {
            stopped = true;
        }
    }

    private void getProfile() {
        // Get configuration profile
        // Needs to be done before loading a ConfigManager or UserPreferencesManager
        FileUtil.createDirectory(FileUtil.getPreferencesPath());
        // Needs to be declared final as we might need to
        // refer to this on the Swing thread
        final File profileFile;
        profileFilename = configFilename.replaceFirst(".xml", ".properties");
        // decide whether name is absolute or relative
        if (!new File(profileFilename).isAbsolute()) {
            // must be relative, but we want it to
            // be relative to the preferences directory
            profileFile = new File(FileUtil.getPreferencesPath() + profileFilename);
        } else {
            profileFile = new File(profileFilename);
        }
        ProfileManager.defaultManager().setConfigFile(profileFile);
        // See if the profile to use has been specified on the command line as
        // a system property jmri.profile as a profile id.
        if (System.getProperties().containsKey(ProfileManager.SYSTEM_PROPERTY)) {
            ProfileManager.defaultManager().setActiveProfile(System.getProperty(ProfileManager.SYSTEM_PROPERTY));
        }
        // @see jmri.profile.ProfileManager#migrateToProfiles JavaDoc for conditions handled here
        if (!ProfileManager.defaultManager().getConfigFile().exists()) { // no profile config for this app
            try {
                if (ProfileManager.defaultManager().migrateToProfiles(configFilename)) { // migration or first use
                    // notify user of change only if migration occured
                    // TODO: a real migration message
                    if (this.isHeadless()) {
                        log.info("WhyWontNetbeansLetMeUseBundle?");
                        //log.info(Bundle.getMessage("ConfigMigratedToProfile"));
                    } else {
                        JOptionPane.showMessageDialog(null,
                                "WhyWontNetbeansLetMeUseBundle?",
                                //Bundle.getMessage("ConfigMigratedToProfile"),
                                jmri.Application.getApplicationName(),
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            } catch (IOException | IllegalArgumentException ex) {
                if (!this.isHeadless()) {
                    JOptionPane.showMessageDialog(null,
                            ex.getLocalizedMessage(),
                            jmri.Application.getApplicationName(),
                            JOptionPane.ERROR_MESSAGE);
                }
                log.error(ex.getMessage());
            }
        }
        try {
            if (!this.isHeadless()) {
                ProfileManagerDialog.getStartingProfile(null);
            }
            if (ProfileManager.getStartingProfile() != null) {
                configFilename = FileUtil.getProfilePath() + Profile.CONFIG_FILENAME;
                System.setProperty("org.jmri.Apps.configFilename", Profile.CONFIG_FILENAME);
                log.info("Starting with profile {}", ProfileManager.defaultManager().getActiveProfile().getId());
            } else {
                log.error("Specify profile to use as command line argument.");
                log.error("If starting with saved profile configuration, ensure the autoStart property is set to \"true\"");
                log.error("Profiles not configurable. Using fallback per-application configuration.");
                // TODO: abort execution
            }
        } catch (IOException ex) {
            log.info("Profiles not configurable. Using fallback per-application configuration. Error: {}", ex.getMessage());
        }
    }

    protected void startManagers() {
        // TODO: run this in a seperate thread if GUI -- it blocks the splash screen
        // Get base configuration
        ConfigXmlManager cm = new ConfigXmlManager();
        FileUtil.createDirectory(FileUtil.getUserFilesPath());
        InstanceManager.store(cm, ConfigureManager.class);
        cm.setPrefsLocation(new File(this.configFilename));
        log.debug("config manager started");
        // Set the Config Manager error handler
        ConfigXmlManager.setErrorHandler(
                (this.isHeadless())
                        ? new ErrorHandler()
                        : new DialogErrorHandler()
        );
        // Start a shutdown manager
        InstanceManager.store(new NetBeansShutDownManager(), ShutDownManager.class);
        this.registerDefaultShutDownTasks();
        // Start a history manager
        InstanceManager.store(new FileHistory(), FileHistory.class);
        // record startup
        InstanceManager.getDefault(FileHistory.class).addOperation("app", Application.getApplicationName(), null);
        // Start a user preferences manager
        InstanceManager.store(DefaultUserMessagePreferences.getInstance(), UserPreferencesManager.class);
        // Start a preferences UI manager
        InstanceManager.store(new TabbedPreferences(), TabbedPreferences.class);
        // Start the abstract action model that allows items to be added to the, both
        // CreateButton and Perform Action Model use a common Abstract class
        InstanceManager.store(new CreateButtonModel(), CreateButtonModel.class);
        // Start the named bean handler
        InstanceManager.store(new NamedBeanHandleManager(), NamedBeanHandleManager.class);
        // Start an IdTag manager
        InstanceManager.store(new DefaultIdTagManager(), IdTagManager.class);
        // Start Entry Exit Pairs Manager
        InstanceManager.store(new EntryExitPairs(), EntryExitPairs.class);
    }

    protected void registerDefaultShutDownTasks() {
        InstanceManager.shutDownManagerInstance().register(new AbstractShutDownTask("Writing Blocks") {
            @Override
            public boolean execute() {
                // Save block values prior to exit, if necessary
                log.debug("Start writing block info");
                try {
                    new BlockValueFile().writeBlockValues();
                } //catch (org.jdom.JDOMException jde) { log.error("Exception writing blocks: "+jde); }
                catch (IOException ex) {
                    log.error("Exception writing blocks", ex);
                }

                // continue shutdown
                return true;
            }
        });
    }

    private boolean loadConfiguration() {
        // find preference file and set location in configuration manager
        // Needs to be declared final as we might need to
        // refer to this on the Swing thread
        final File file = new File(this.configFilename);
        // load config file if it exists
        if (file.exists()) {
            if (log.isDebugEnabled()) {
                log.debug("start load config file {}", file.getPath());
            }
            try {
                this.configLoaded = InstanceManager.configureManagerInstance().load(file, true);
            } catch (JmriException ex) {
                log.error("Unhandled problem loading configuration", ex);
                this.configLoaded = false;
            }
            log.debug("end load config file, OK={}", this.configLoaded);
        } else {
            log.info("No saved preferences, will open preferences window. Searched for {}", file.getPath());
            this.configLoaded = false;
        }
        // I'm really sure what this model does for JMRI
        CreateButtonModel model = InstanceManager.getDefault(CreateButtonModel.class);
        ResourceBundle actions = ResourceBundle.getBundle("apps.ActionListBundle"); // NOI18N
        for (String action : actions.keySet()) {
            try {
                model.addAction(action, actions.getString(action));
            } catch (ClassNotFoundException ex) {
                log.error("Did not find class {}", action);
            }
        }
        FileUtil.logFilePaths();
        // here is where we need to respond to an interupted start (F8 for Logix)
        // looking to see if there is a built in way to respond to that first
        if (this.configLoaded) {
            try {
                this.configLoaded = InstanceManager.configureManagerInstance().loadDeferred(file);
            } catch (JmriException ex) {
                log.error("Unhandled problem loading configuration", ex);
                this.configLoaded = false;
            }
            /* Does NetBeans offer a better way? Need to also trap Headless Exceptions
            // To avoid possible locks, deferred load should be
            // performed on the Swing thread
            if (SwingUtilities.isEventDispatchThread()) {
                configDeferredLoadOK = doDeferredLoad(file);
            } else {
                try {
                    // Use invokeAndWait method as we don't want to
                    // return until deferred load is completed
                    SwingUtilities.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            configDeferredLoadOK = doDeferredLoad(file);
                        }
                    });
                } catch (Exception ex) {
                    log.error("Exception creating system console frame", ex);
                }
            }
                    */
        }
        return this.configLoaded;
    }

    private void initilizePreferencesUI() {
        // Once all preferences have been loaded, initialize the preferences UI
        // Doing it in a thread now means we can let it work in the background
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    InstanceManager.tabbedPreferencesInstance().init();
                } catch (Exception ex) {
                    log.error("Error trying to setup preferences {}", ex.getLocalizedMessage(), ex);
                }
            }
        };
        Thread thr = new Thread(r, "initialize preferences");
        thr.start();
    }
}
