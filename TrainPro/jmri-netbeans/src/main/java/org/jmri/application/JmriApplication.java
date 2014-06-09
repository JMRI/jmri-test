package org.jmri.application;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.util.logging.LogManager;
import javax.swing.JOptionPane;
import jmri.Application;
import jmri.ConfigureManager;
import jmri.IdTagManager;
import jmri.InstanceManager;
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
import org.jmri.managers.NetBeansShutDownManager;
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
    private String profileFilename;
    private Boolean started = false;
    private Boolean shown = false;
    private Boolean stopped = false;
    private static final Logger log = LoggerFactory.getLogger(JmriApplication.class);

    private JmriApplication(String title) throws IllegalAccessException, IllegalArgumentException {
        Application.setApplicationName(title);
        // need to watch CLI arguments as well
        if (System.getProperty("org.jmri.Apps.configFilename") != null) {
            this.configFilename = System.getProperty("org.jmri.Apps.configFilename");
        } else {
            // if title is "JMRI App", configFile is "JMRIAppConfig.xml"
            this.configFilename = title.replaceAll(" ", "") + "Config.xml";
        }
        log.info("Using config file {}", this.configFilename);
        // TODO: better logging handling
        try {
            LogManager.getLogManager().readConfiguration();
        } catch (IOException | SecurityException ex) {
            log.error("Unable to configure logging.", ex);
        }
    }

    public static JmriApplication getApplication(String title) throws IllegalAccessException, IllegalArgumentException {
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

    public boolean isHeadless() {
        return GraphicsEnvironment.isHeadless();
    }

    public void start() {
        if (!started) {
            started = true;
            // get profiles
            this.startManagers();
            // start web server
        }
    }

    public void show() {
        if (!shown) {
            shown = true;
            this.start();
            // do any other GUI things that we might need to do
        }
    }

    public void stop() {
        if (!stopped) {
            stopped = true;
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
                    // TODO: a real migration message
                    JOptionPane.showMessageDialog(null,
                            "WhyWontNetbeansLetMeUseBundle?",
                            //Bundle.getMessage("ConfigMigratedToProfile"),
                            jmri.Application.getApplicationName(),
                            JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (IOException | IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(null,
                        ex.getLocalizedMessage(),
                        jmri.Application.getApplicationName(),
                        JOptionPane.ERROR_MESSAGE);
                log.error(ex.getMessage());
            }
        }
        try {
            ProfileManagerDialog.getStartingProfile(null);
            // Manually setting the configFilename property since calling
            // Apps.setConfigFilename() does not reset the system property
            configFilename = FileUtil.getProfilePath() + Profile.CONFIG_FILENAME;
            System.setProperty("org.jmri.Apps.configFilename", Profile.CONFIG_FILENAME);
            log.info("Starting with profile {}", ProfileManager.defaultManager().getActiveProfile().getId());
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
}
