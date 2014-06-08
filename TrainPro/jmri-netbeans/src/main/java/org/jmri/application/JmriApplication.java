package org.jmri.application;

import java.io.File;
import java.io.IOException;
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
import jmri.profile.Profile;
import jmri.profile.ProfileManager;
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
    private final String title;
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
        } catch (IOException | SecurityException ex) {
            log.error("Unable to configure logging.", ex);
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

    /**
     * Configure the {@link jmri.profile.Profile} to use for this application.
     * <p>
     * Note that GUI-based applications must override this method, since this
     * method does not provide user feedback.
     */
    protected void configureProfile() {
        String profileFilename;
        FileUtil.createDirectory(FileUtil.getPreferencesPath());
        // Needs to be declared final as we might need to
        // refer to this on the Swing thread
        File profileFile;
        profileFilename = this.configFile.replaceFirst(".xml", ".properties");
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
                if (ProfileManager.defaultManager().migrateToProfiles(this.configFile)) { // migration or first use
                    // GUI should show message here
                    log.info(Bundle.getMessage("ConfigMigratedToProfile"));
                }
            } catch (IOException | IllegalArgumentException ex) {
                // GUI should show message here
                log.error("Profiles not configurable. Using fallback per-application configuration. Error: {}", ex.getMessage());
            }
        }
        try {
            // GUI should use ProfileManagerDialog.getStartingProfile here
            if (ProfileManager.getStartingProfile() != null) {
                // Manually setting the configFilename property since calling
                // Apps.setConfigFilename() does not reset the system property
                System.setProperty("org.jmri.Apps.configFilename", Profile.CONFIG_FILENAME);
                log.info("Starting with profile {}", ProfileManager.defaultManager().getActiveProfile().getId());
            } else {
                log.error("Specify profile to use as command line argument.");
                log.error("If starting with saved profile configuration, ensure the autoStart property is set to \"true\"");
                log.error("Profiles not configurable. Using fallback per-application configuration.");
            }
        } catch (IOException ex) {
            log.info("Profiles not configurable. Using fallback per-application configuration. Error: {}", ex.getMessage());
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
            @Override
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
