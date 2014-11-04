package org.jmri.logging;

import java.io.File;
import java.io.IOException;
import java.util.logging.LogManager;
import org.netbeans.api.sendopts.CommandException;
import org.netbeans.spi.sendopts.Arg;
import org.netbeans.spi.sendopts.ArgsProcessor;
import org.netbeans.spi.sendopts.Env;
import org.openide.filesystems.FileObject;
import org.openide.modules.OnStart;
import org.openide.modules.Places;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@OnStart
public final class Installer implements ArgsProcessor, Runnable {

    /**
     * Path used to get logging configuration. Set by users by specifying the
     * <code>--log-config=/path/to/my/logging.properties</code> command line
     * argument. If not specified by a user, JMRI applications will find a
     * usable logging configuration. If empty, the JMRI application could not
     * find a usable logging configuration and is using a built-in default
     * configuration.
     */
    @Arg(longName = "log-config", defaultValue = "")
    public String logConfigPath = "";

    private static final Logger log = LoggerFactory.getLogger(Installer.class);

    public void configureLogging() {
        // configure logging if custom logging configuration is provided
        try {
            FileObject logConfig = null;
            if (!logConfigPath.isEmpty()) {
                logConfig = org.openide.filesystems.FileUtil.toFileObject((new File(logConfigPath)).getCanonicalFile());
            }
            if (logConfig == null) {
                logConfig = org.openide.filesystems.FileUtil.toFileObject(new File(Places.getUserDirectory(), "logging.properties"));
            }
            if (logConfig == null) {
                logConfig = org.openide.filesystems.FileUtil.getConfigFile("logging.properties");
            }
            /*
             TODO: how do I get a logging configuration from the user files
             without a dependency on a running JMRI configuation? or do I care
             for logging?
             if (logConfig == null) {
             logConfig = org.openide.filesystems.FileUtil.toFileObject(FileUtil.getFile("logging.properties"));
             }
             */
            if (logConfig != null) {
                log.info("Using logging configuration from {}", logConfig.getPath());
                LogManager.getLogManager().readConfiguration(logConfig.getInputStream());
            }
        } catch (IOException | SecurityException ex) {
            log.error("Unable to configure logging.", ex);
        }
    }

    @Override
    public void process(Env env) throws CommandException {
        this.configureLogging();
    }

    @Override
    public void run() {
        this.configureLogging();
    }
}
