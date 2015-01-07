package org.jmri.application;

import apps.CreateButtonModel;
import apps.gui3.TabbedPreferences;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import jmri.Application;
import jmri.ConfigureManager;
import jmri.IdTagManager;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.NamedBeanHandleManager;
import jmri.ShutDownManager;
import jmri.UserPreferencesManager;
import jmri.beans.Bean;
import jmri.configurexml.ConfigXmlManager;
import jmri.configurexml.ErrorHandler;
import jmri.configurexml.swing.DialogErrorHandler;
import jmri.implementation.AbstractShutDownTask;
import jmri.jmrit.catalog.configurexml.DefaultCatalogTreeManagerXml;
import jmri.jmrit.decoderdefn.DecoderIndexFile;
import jmri.jmrit.display.layoutEditor.BlockValueFile;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.jmrit.revhistory.FileHistory;
import jmri.jmrit.signalling.EntryExitPairs;
import jmri.managers.DefaultIdTagManager;
import jmri.managers.DefaultUserMessagePreferences;
import jmri.profile.Profile;
import jmri.profile.ProfileManager;
import jmri.profile.ProfileManagerDialog;
import jmri.util.FileUtil;
import jmri.util.PythonInterp;
import jmri.util.zeroconf.ZeroConfService;
import jmri.web.server.WebServerManager;
import org.jmri.managers.NetBeansShutDownManager;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
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
public abstract class JmriApplication extends Bean {

    /**
     * Property that can be listened to so that listeners are informed of
     * changes in the application state.
     *
     * {@value #STATE}
     *
     * @see State
     * @see #getState()
     */
    public static final String STATE = "state";

    /**
     * Possible JMRI application states.
     */
    public static enum State {

        LOADING, STARTING, STARTED, SHOWING, SHOWN, STOPPING, STOPPED
    }

    private State state = State.LOADING;
    private HashMap<Runnable, State> deferredTasks = new HashMap<>();
    private String configFilename;
    private boolean configLoaded = false;
    private boolean deferredConfigLoaded = false;
    private String profileFilename;
    private static final Logger log = LoggerFactory.getLogger(JmriApplication.class);

    protected JmriApplication(String title) {
        try {
            Application.setApplicationName(title);
        } catch (IllegalAccessException | IllegalArgumentException ex) {
            log.error("Unable to set application name", ex);
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
     * Public default constructor so this can be used as a bean.
     *
     * This constructor only logs that it has been used in error.
     */
    public JmriApplication() {
        IllegalAccessException e = new IllegalAccessException();
        log.error("Default constructor for JmriApplication called.", e);
    }

    /**
     * Get the application object.
     *
     * This is a convenience wrapper around
     * <code>org.openide.util.Lookup.getDefault().lookup(org.jmri.application.JmriApplication.class);</code>
     *
     * @return The object managing the JMRI application's life cycle.
     * @see org.openide.util.Lookup
     */
    public static JmriApplication getApplication() {
        return Lookup.getDefault().lookup(JmriApplication.class);
    }

    /**
     * Perform startup tasks.
     *
     * The default implementation is <code>this.start(true);</code>
     *
     * Any actions taken by this method or its overriding implementation, or by
     * objects called in this method or its overriding implementation, must
     * succeed in a headless environment, since there is no guarantee that this
     * method will be called in a GUI environment.
     *
     * Classes that wish to override this method should either re-implement
     * {@link #start(java.lang.Boolean) } or call it in the following pattern: <code>
     * this.setState(State.STARTING);
     * // do something
     * super.start(false);
     * // do something else
     * this.setState(State.STARTED);
     * </code>
     *
     * @see #show()
     * @see org.openide.modules.OnStart
     */
    public void start() {
        this.start(true);
    }

    /**
     * Perform startup tasks.
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
     * This method is final; classes needing to override this method should
     * instead override {@link #start() }.
     *
     * @param setState true if this method should set the state or false if the
     * calling method will set the state
     * @see #show(java.lang.Boolean)
     * @see org.openide.modules.OnStart
     */
    protected final void start(Boolean setState) {
        if (this.state == State.LOADING) {
            if (setState) {
                this.setState(State.STARTING);
            }
            this.getProfile();
            this.doDeferred();
            if (GraphicsEnvironment.isHeadless()) {
                this.startManagers();
                if (!this.loadConfiguration()) {
                    // TODO handle failure to load configuration when headless at this point
                    // should we log an error, write it to STDOUT, and quit now?
                }
                // Always run the WebServer when headless
                WebServerManager.getWebServer().start();
                System.out.println();
                System.out.println("JMRI web service is at:");
                for (InetAddress address : ZeroConfService.hostAddresses()) {
                    System.out.println("    http://" + address.getHostAddress() + ":" + WebServerManager.getWebServerPreferences().getPort());
                    String fqdn = ZeroConfService.FQDN(address);
                    if (!fqdn.equals("computer") && !fqdn.equals(address.getHostAddress() + ".local.")) {
                        System.out.println("    http://" + fqdn + ":" + WebServerManager.getWebServerPreferences().getPort());
                    }
                }
            }
            if (setState) {
                this.setState(State.STARTED);
            }
            this.doDeferred();
        }
    }

    /**
     * Perform tasks when the application's main window is drawn.
     *
     * The default implementation is <code>this.show(true);</code>
     *
     * Actions taken by this method or its overriding implementation are in a
     * GUI environment.
     *
     * Classes that wish to override this method should either re-implement
     * {@link #show(java.lang.Boolean) } or call it in the following pattern: <code>
     * this.setState(State.SHOWING);
     * // do something
     * super.show(false);
     * // do something else
     * this.setState(State.SHOWN);
     * </code>
     *
     * @see #start()
     * @see org.openide.windows.OnShowing
     */
    public void show() {
        this.show(true);
    }

    /**
     * Perform tasks when the application's main window is drawn.
     *
     * Actions taken by this method are in a GUI environment.
     *
     * This method is final; classes needing to override this method should
     * instead override {@link #show() }.
     *
     * @param setState true if this method should set the state or false if the
     * calling method will set the state
     * @see #start(java.lang.Boolean)
     * @see org.openide.windows.OnShowing
     */
    protected final void show(Boolean setState) {
        if (this.state != State.SHOWING && this.state != State.SHOWN) {
            if (setState) {
                this.setState(State.SHOWING);
            }
            this.doDeferred();
            this.startManagers();
            // TODO add user's buttons to toolbar
            if (!this.loadConfiguration()) {
                // TODO handle failure to load configuration by displaying a
                // first time use wizard and the options
            }
            this.initilizePreferencesUI();
            // do any other GUI things that we might need to do
            if (setState) {
                this.setState(State.SHOWN);
            }
            this.doDeferred();
        }
    }

    /**
     * Perform tasks when the application is exiting.
     *
     * The default implementation is <code>this.stop(true);</code>
     *
     * Any actions taken by this method or its overriding implementation, or by
     * objects called in this method or its overriding implementation, must
     * succeed in a headless environment, since there is no guarantee that this
     * method will be called in a GUI environment.
     *
     * Classes that wish to override this method should either re-implement
     * {@link #show(java.lang.Boolean) } or call it in the following pattern: <code>
     * this.setState(State.STOPPING);
     * // do something
     * super.stop(false);
     * // do something else
     * this.setState(State.STOPPED);
     * </code>
     *
     * @see org.openide.modules.OnStop
     */
    public void stop() {
        this.stop(true);
    }

    /**
     * Perform tasks when the application is exiting.
     *
     * Any actions taken by this method, or by objects called in this method,
     * must succeed in a headless environment, since there is no guarantee that
     * this method will be called in a GUI environment.
     *
     * This method is final; classes needing to override this method should
     * instead override {@link #stop() }.
     *
     * @param setState true if this method should set the state or false if the
     * calling method will set the state
     * @see org.openide.modules.OnStop
     */
    protected void stop(Boolean setState) {
        if (this.state != State.STOPPING && this.state != State.STOPPED) {
            if (setState) {
                this.setState(State.STOPPING);
            }
            this.doDeferred();
            if (setState) {
                this.setState(State.STOPPED);
            }
            this.doDeferred();
        }
    }

    /**
     * Defer a task until a later state is reached.
     *
     * Deferring a task to the current state will immediately run the task. Note
     * that a task deferred to an earlier state is not run and is not retained.
     *
     * @param state The state at which the task should be run
     * @param task A task to run
     */
    public void defer(State state, Runnable task) {
        if (state == this.state) {
            task.run();
        } else if (state.compareTo(this.state) > 0) {
            this.deferredTasks.put(task, state);
        }
    }

    protected void doDeferred() {
        Set<Runnable> tasks = new HashSet<>(this.deferredTasks.keySet());
        for (Runnable task : tasks) {
            if (this.state == this.deferredTasks.get(task)) {
                task.run();
                this.deferredTasks.remove(task);
            }
        }
    }

    protected void getProfile() {
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
                    if (GraphicsEnvironment.isHeadless()) {
                        log.info(NbBundle.getMessage(JmriApplication.class, "ConfigMigratedToProfile")); // NOI18N
                    } else {
                        JOptionPane.showMessageDialog(null,
                                NbBundle.getMessage(JmriApplication.class, "ConfigMigratedToProfile"), // NOI18N
                                jmri.Application.getApplicationName(),
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            } catch (IOException | IllegalArgumentException ex) {
                if (!GraphicsEnvironment.isHeadless()) {
                    JOptionPane.showMessageDialog(null,
                            ex.getLocalizedMessage(),
                            jmri.Application.getApplicationName(),
                            JOptionPane.ERROR_MESSAGE);
                }
                log.error(ex.getMessage());
            }
        }
        try {
            if (!GraphicsEnvironment.isHeadless()) {
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
                (GraphicsEnvironment.isHeadless())
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

                if (GraphicsEnvironment.isHeadless()) {
                    System.out.println("Exiting JMRI...");
                }
                // continue shutdown
                return true;
            }
        });
    }

    protected boolean loadConfiguration() {
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
            // TODO: Trap Headless exceptions
            // To avoid possible locks, deferred load should be
            // performed on the Swing thread
            if (SwingUtilities.isEventDispatchThread()) {
                this.deferredConfigLoaded = doDeferredLoad(file);
            } else {
                try {
                    // Use invokeAndWait method as we don't want to
                    // return until deferred load is completed
                    SwingUtilities.invokeAndWait(() -> {
                        this.deferredConfigLoaded = doDeferredLoad(file);
                    });
                } catch (InterruptedException | InvocationTargetException ex) {
                    log.error("Exception creating system console frame", ex);
                }
            }
        }

        // TODO: All these threads should eventually be pushed into Initialzers.
        //Initialise the decoderindex file instance within a seperate thread to help improve first use perfomance
        new Thread(() -> {
            try {
                DecoderIndexFile.instance();
            } catch (Exception ex) {
                log.error("Error in trying to initialize decoder index file " + ex.toString());
            }
        }, "initialize decoder index").start();
        // TODO: Move into @Start initializer in Python Scripting Support module.
        // Once done, JMRI Core dependency on Python Scripting Support module
        // can also be removed.
        if (Boolean.getBoolean("org.jmri.python.preload")) {
            new Thread(() -> {
                try {
                    PythonInterp.getPythonInterpreter();
                } catch (Exception ex) {
                    log.error("Error in trying to initialize python interpreter " + ex.toString());
                }
            }, "initialize python interpreter").start();
        }

        // do final activation
        InstanceManager.logixManagerInstance().activateAllLogixs();
        InstanceManager.getDefault(LayoutBlockManager.class).initializeLayoutBlockPaths();
        new DefaultCatalogTreeManagerXml().readCatalogTrees();

        return this.configLoaded;
    }

    private boolean doDeferredLoad(File file) {
        boolean result;
        log.debug("start deferred load from config");
        try {
            result = InstanceManager.configureManagerInstance().loadDeferred(file);
        } catch (JmriException e) {
            log.error("Unhandled problem loading deferred configuration", e);
            result = false;
        }
        log.debug("end deferred load from config file, OK={}", result);
        return result;
    }

    protected void initilizePreferencesUI() {
        // Once all preferences have been loaded, initialize the preferences UI
        // Doing it in a thread now means we can let it work in the background
        new Thread(() -> {
            try {
                InstanceManager.tabbedPreferencesInstance().init();
            } catch (Exception ex) {
                log.error("Error trying to setup preferences {}", ex.getLocalizedMessage(), ex);
            }
        }, "initialize preferences").start();
    }

    /**
     * Test if application is started. This test should be used by modules and
     * components that need to run or operate in headless mode.
     *
     * Note this method returns true if the application is started or shown.
     *
     * @return true if application is started, showing, or shown
     * @see #isShown()
     */
    public Boolean isStarted() {
        return this.state == State.STARTED
                || this.state == State.SHOWING
                || this.state == State.SHOWN;
    }

    /**
     * Test if an application is shown. This test should be used by modules and
     * components that need to display graphical user interface elements.
     *
     * @return true if application is shown
     * @see #isStarted()
     */
    public Boolean isShown() {
        return this.state == State.SHOWN;
    }

    /**
     * Test is an application has stopped.
     *
     * @return true if application is stopped
     */
    public Boolean isStopped() {
        return this.state == State.STOPPED;
    }

    /**
     * Get the application state.
     *
     * If a process is only interested in knowing if an application is started
     * or is shown, the {@link #isStarted() } and {@link #isShown() } methods
     * will be easier to use and more complete in most cases.
     *
     * @return the application's state
     */
    public State getState() {
        return state;
    }

    /**
     * @param state the state to set
     */
    protected void setState(State state) {
        if (this.state != state) {
            State oldState = this.state;
            this.state = state;
            propertyChangeSupport.firePropertyChange(STATE, oldState, state);
        }
    }
}
