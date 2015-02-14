package org.jmri.logging;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.LogManager;
import org.netbeans.api.sendopts.CommandException;
import org.netbeans.spi.sendopts.Arg;
import org.netbeans.spi.sendopts.ArgsProcessor;
import org.netbeans.spi.sendopts.Env;
import org.openide.filesystems.FileObject;
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.OnStart;
import org.openide.modules.Places;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle logging configuration. By default, this reads the files logging.properties
 * from the platform-specified installed and config files locations.
 *
 * @author Randall Wood (C) 2014
 */
@OnStart
public final class LoggingConfigurator implements ArgsProcessor, Runnable {

    /**
     * Path used to get logging configuration. Set by users by specifying the
     * <code>--logging=/path/to/my/logging.properties</code> command line
     * argument. Settings in this logging configuration are added to the default
     * configuration. To specify a completely new logging configuration, set the
     * system property <code>java.util.logging.config.file</code>.
     */
    @Arg(longName = "logging", defaultValue = "")
    public String logConfigPath = "";

    private static final Logger log = LoggerFactory.getLogger(LoggingConfigurator.class);

    /**
     * Configure logging from properties specified in the configuration file. If
     * null is passed, no configuration changes occur.
     *
     * @param config The file containing logging configuration properties.
     */
    public void configureLogging(FileObject config) {
        this.configureLogging(config, false);
    }

    private void configureLogging(FileObject config, Boolean overwrite) {
        if (config != null) {
            // configure logging if custom logging configuration is provided
            try {
                // if a configuration file is specified, set System properties
                // and reload the logging configuration. Simply passing the file
                // will completely overwrite the existing logging configuration.
                // By being additive, different modules can specify default
                // recommended logging levels.
                log.debug("Using logging configuration from {}", config.getPath());
                Properties properties = new Properties();
                properties.load(config.getInputStream());
                for (String key : properties.stringPropertyNames()) {
                    if (overwrite || !System.getProperties().containsKey(key)) {
                        String value = properties.getProperty(key);
                        switch (value) {
                            case "SEVERE":
                                value = Level.SEVERE.toString();
                                break;
                            case "WARNING":
                                value = Level.WARNING.toString();
                                break;
                            case "INFO":
                                value = Level.INFO.toString();
                                break;
                            case "CONFIG":
                                value = Level.CONFIG.toString();
                                break;
                            case "FINE":
                                value = Level.FINE.toString();
                                break;
                            case "FINER":
                                value = Level.FINER.toString();
                                break;
                            case "FINEST":
                                value = Level.FINEST.toString();
                                break;
                            case "OFF":
                                value = Level.OFF.toString();
                                break;
                            case "ALL":
                                value = Level.ALL.toString();
                                break;
                        }
                        System.setProperty(key, value);
                    }
                }
                LogManager.getLogManager().readConfiguration();
            } catch (IOException | SecurityException ex) {
                log.error("Unable to configure logging.", ex);
            }
        }
    }

    @Override
    public void process(Env env) throws CommandException {
        log.info("Processing logging command line argument {}", this.logConfigPath);
        if (!this.logConfigPath.isEmpty()) {
            // get specified configuration
            try {
                this.configureLogging(org.openide.filesystems.FileUtil.toFileObject((new File(this.logConfigPath)).getCanonicalFile()), true);
            } catch (IOException ex) {
                log.error("Unable to use specified logging configuration \"{}\"", this.logConfigPath);
            }
        }
    }

    @Override
    public void run() {
        System.setProperty("jmri.log.path", new File(Places.getUserDirectory(), "var/log").getPath());
        /*
         TODO: how do I get a logging configuration from the user files
         without a dependency on a running JMRI configuation? This cannot
         be done until jmri.util.FileUtil is in its own module.
         this.configureLogging(org.openide.filesystems.FileUtil.toFileObject(FileUtil.getFile("logging.properties")));
         */
        this.configureLogging(org.openide.filesystems.FileUtil.getConfigFile("logging.properties"));
        File installedLogConfig = InstalledFileLocator.getDefault().locate(
                "logging.properties",
                "org.jmri.logging",
                false
        );
        if (installedLogConfig != null) {
            this.configureLogging(org.openide.filesystems.FileUtil.toFileObject(installedLogConfig));
        }
        log.info("Logging to {}", new File(Places.getUserDirectory(), "var/log/messages.log"));
    }
}
