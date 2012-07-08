// AppsBase.java

package apps;

import jmri.*;
import jmri.InstanceManager;
import jmri.jmrit.XmlFile;

import java.io.File;
import javax.swing.*;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base class for the core of JMRI applications.
 * <p>
 * This provides a non-GUI base for applications.
 * Below this is the {@link apps.gui3.Apps3} class
 * which provides basic Swing GUI support.
 * <p>
 * For an example of using this, see
 * {@link apps.FacelessApp} and comments therein.
 * <p>
 * There are a series of steps in the configuration:
 * <dl>
 * <dt>preInit<dd>Initialize log4j, invoked from the main()
 * <dt>ctor<dd>
 * </dl>
 * <P>
 * @author	Bob Jacobsen   Copyright 2009, 2010
 * @version $Revision$
 */
public abstract class AppsBase {

    /**
     * Initial actions before 
     * frame is created, invoked in the 
     * applications main() routine.
     */
    static public void preInit(String applicationName) {
        if (!log4JSetUp) initLog4J();

        try {
            Application.setApplicationName(applicationName);
        } catch (IllegalAccessException ex) {
            log.info("Unable to set application name");
        } catch (IllegalArgumentException ex) {
            log.info("Unable to set application name");
        }

        //jmri.util.Log4JUtil.initLog4J();
        log.info(jmri.util.Log4JUtil.startupInfo(applicationName));
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="MS_PKGPROTECT",
                                    justification="not a library pattern")
    protected final static String nameString = "JMRI Base";

    private final static String configFilename = "/JmriConfig3.xml";
    protected boolean configOK;
    protected boolean configDeferredLoadOK;
    protected boolean preferenceFileExists;
    
    /**
     * Create and initialize the application object.
     *<p>
     * Expects initialization from preInit() to already be done.
     */
     @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="SC_START_IN_CTOR",
                                justification="The thread is only called to help improve user experiance when opening the preferences, it is not critical for it to be run at this stage")
    public AppsBase() {
        
        if (!log4JSetUp) initLog4J();
        
        installConfigurationManager();

        installShutDownManager();

        addDefaultShutDownTasks();

        installManagers();

        setAndLoadPreferenceFile();
        
        Runnable r;
        /*Once all the preferences have been loaded we can initial the preferences
        doing it in a thread at this stage means we can let it work in the background
        if the file doesn't exist then we do not initilise it*/
        if(preferenceFileExists){
            r = new Runnable() {
              public void run() {
                try {
                     jmri.InstanceManager.tabbedPreferencesInstance().init();
                } catch (Exception ex) {
                    log.error(ex.toString());
                }
              }
            };
            Thread thr = new Thread(r);
            thr.start();
        }
        
        r = new Runnable() {
          public void run() {
            try {
                jmri.util.PythonInterp.getPythonInterpreter();
            } catch (Exception ex) {
                log.error("Error in trying to initialize python interpreter " + ex.toString());
            }
          }
        };
        Thread thr2 = new Thread(r, "initialize python interpreter");
        thr2.start();
    }
    
    protected void installConfigurationManager() {
        jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager();
        XmlFile.ensurePrefsPresent(XmlFile.prefsDir());
        InstanceManager.setConfigureManager(cm);
        cm.setPrefsLocation(new File(getConfigFileName()));
        log.debug("config manager installed");
        // Install Config Manager error handler
        jmri.configurexml.ConfigXmlManager.setErrorHandler(new jmri.configurexml.swing.DialogErrorHandler());
    }
    
    protected void installManagers() {
        // Install a history manager
        InstanceManager.store(new jmri.jmrit.revhistory.FileHistory(), jmri.jmrit.revhistory.FileHistory.class);
        // record startup
        InstanceManager.getDefault(jmri.jmrit.revhistory.FileHistory.class).addOperation("app", nameString, null);
        
        // Install a user preferences manager
        InstanceManager.store(jmri.managers.DefaultUserMessagePreferences.getInstance(), jmri.UserPreferencesManager.class);
        
        // install the abstract action model that allows items to be added to the, both 
        // CreateButton and Perform Action Model use a common Abstract class
        InstanceManager.store(new apps.CreateButtonModel(), apps.CreateButtonModel.class);
        
        // install preference manager
        InstanceManager.setTabbedPreferences(new apps.gui3.TabbedPreferences());
    }

    protected void setAndLoadPreferenceFile() {
        XmlFile.ensurePrefsPresent(XmlFile.prefsDir());
        final File file;
        // decide whether name is absolute or relative
        if (!new File(getConfigFileName()).isAbsolute()) {
            // must be relative, but we want it to 
            // be relative to the preferences directory
            file = new File(XmlFile.prefsDir()+ getConfigFileName());
        } else {
            file = new File(getConfigFileName());
        }
        // don't try to load if doesn't exist, but mark as not OK
        if (!file.exists()) {
            preferenceFileExists = false;
            configOK = false;
            log.info("No pre-existing preferences settings");
            ((jmri.configurexml.ConfigXmlManager)InstanceManager.configureManagerInstance())
                    .setPrefsLocation(file);
            return;
        }
        preferenceFileExists = true;
        try {
            ((jmri.configurexml.ConfigXmlManager)InstanceManager.configureManagerInstance())
                                .setPrefsLocation(file);
            configOK = InstanceManager.configureManagerInstance().load(file);
            log.debug("end load config file, OK="+configOK);
        } catch (Exception e) {
            configOK = false;
        }
        
        // To avoid possible locks, deferred load should be
        // performed on the Swing thread
        if (SwingUtilities.isEventDispatchThread()) {
            configDeferredLoadOK = doDeferredLoad(file);
        } else {
            try {
                // Use invokeAndWait method as we don't want to
                // return until deferred load is completed
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        configDeferredLoadOK = doDeferredLoad(file);
                    }
                });
            } catch (Exception ex) {
                log.error("Exception creating system console frame: "+ex);
            }
        }
    }
    
    //abstract protected void addToActionModel();
    
    private boolean doDeferredLoad(File file) {
        boolean result;
        log.debug("start deferred load from config");
        try {
            result = InstanceManager.configureManagerInstance().loadDeferred(file);
        } catch (JmriException e) {
            log.error("Unhandled problem loading deferred configuration: "+e);
            result = false;
        }
        log.debug("end deferred load from config file, OK="+result);
        return result;
    }
    
    protected void installShutDownManager() {
        InstanceManager.setShutDownManager(
                new jmri.managers.DefaultShutDownManager());
    }

    protected void addDefaultShutDownTasks() {
        // add the default shutdown task to save blocks
        // as a special case, register a ShutDownTask to write out blocks
        InstanceManager.shutDownManagerInstance().
            register(new jmri.implementation.AbstractShutDownTask("Writing Blocks"){
                public boolean execute() {
                    // Save block values prior to exit, if necessary
                    log.debug("Start writing block info");
                    try {
                        new jmri.jmrit.display.layoutEditor.BlockValueFile().writeBlockValues();
                    } 
                    //catch (org.jdom.JDOMException jde) { log.error("Exception writing blocks: "+jde); }                           
                    catch (java.io.IOException ioe) { log.error("Exception writing blocks: "+ioe); }   
                    
                    // continue shutdown   
                    return true;
                }
            });
    }
    
    /**
     * Final actions before releasing control of app to user,
     * invoked explicitly after object has been constructed,
     * e.g. in main().
     */
    protected void postInit() {
        log.debug("main initialization done");
    }
    
    /**
     * Set up the configuration file name at startup.
     * <P>
     * The Configuration File name variable holds the name 
     * used to load the configuration file during later startup
     * processing.  Applications invoke this method 
     * to handle the usual startup hierarchy:
     *<UL>
     *<LI>If an absolute filename was provided on the command line, use it
     *<LI>If a filename was provided that's not absolute, consider it to
     *    be in the preferences directory
     *<LI>If no filename provided, use a default name (that's application
     *    specific)
     *</UL>
     * This name will be used for reading and writing the preferences. It
     * need not exist when the program first starts up. This name may be proceeded
     * with <em>config=</em>.
     *
     * @param def Default value if no other is provided
     * @param args Argument array from the main routine
     */
    static protected void setConfigFilename(String def, String[] args) {
        // save the configuration filename if present on the command line
        
        if (args.length >= 1 && args[0] != null && !args[0].contains("=")) {
            def = args[0];
            log.debug("Config file was specified as: " + args[0]);
        }
        for (String arg : args) {
            String[] split = arg.split("=", 2);
            if (split[0].equalsIgnoreCase("config")) {
                def = split[1];
                log.debug("Config file was specified as: " + arg);
            }
        }
        setJmriSystemProperty("configFilename", def);
    }
    
    // We will use the value stored in the system property 
    static public String getConfigFileName(){
        if(System.getProperty("org.jmri.Apps.configFilename")!=null)
            return System.getProperty("org.jmri.Apps.configFilename");
        return configFilename;
    }
    
    static protected void setJmriSystemProperty(String key, String value) {
        try {
            String current = System.getProperty("org.jmri.Apps."+key);
            if ( current == null){
                System.setProperty("org.jmri.Apps."+key, value);
            }else if (!current.equals(value))
                log.warn("JMRI property "+key+" already set to "+current+
                        ", skipping reset to "+value);
        } catch (Exception e) {
            log.error("Unable to set JMRI property "+key+" to "+value+
                        "due to exception: "+e);
        }
    }
    
    static boolean log4JSetUp = false;
    
    static protected void initLog4J() {
    	if (log4JSetUp){
    		log.debug("initLog4J already initialized!");
    		return;
    	}
        // Initialise JMRI System Console
        // Need to do this before initialising log4j so that the new
        // stdout and stderr streams are set-up and usable by the ConsoleAppender
        SystemConsole.create();

        log4JSetUp = true;
        // initialize log4j - from logging control file (lcf) only
        // if can find it!
        String logFile = "default.lcf";
        try {
            if (new java.io.File(logFile).canRead()) {
                org.apache.log4j.PropertyConfigurator.configure(logFile);
            } else {
                org.apache.log4j.BasicConfigurator.configure();
                org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.WARN);
            }
        }
        catch (java.lang.NoSuchMethodError e) { log.error("Exception starting logging: "+e); }
        // install default exception handlers
        System.setProperty("sun.awt.exception.handler", jmri.util.exceptionhandler.AwtHandler.class.getName());
        Thread.setDefaultUncaughtExceptionHandler(new jmri.util.exceptionhandler.UncaughtExceptionHandler());
    
        // first log entry
    	log.info(jmriLog);

        // now indicate logging locations
        @SuppressWarnings("unchecked")
        Enumeration<org.apache.log4j.Logger> e = org.apache.log4j.Logger.getRootLogger().getAllAppenders();
       
        while ( e.hasMoreElements() ) {
            org.apache.log4j.Appender a = (org.apache.log4j.Appender)e.nextElement();
            if ( a instanceof org.apache.log4j.RollingFileAppender ) {
                log.info("This log is stored in file: "+((org.apache.log4j.RollingFileAppender)a).getFile());
            }
            else if ( a instanceof org.apache.log4j.FileAppender ) {
                log.info("This log is stored in file: "+((org.apache.log4j.FileAppender)a).getFile());
            }
        }
    }
    
    /**
     * The application decided to quit, handle that.
     */
    static public void handleQuit() {
        log.debug("Start handleQuit");
        try {
            InstanceManager.shutDownManagerInstance().shutdown();
        } catch (Exception e) {
            log.error("Continuing after error in handleQuit",e);
        }
    }
    
    /**
     * The application decided to restart, handle that.
     */
    static public void handleRestart() {
        log.debug("Start handleRestart");
        try {
            InstanceManager.shutDownManagerInstance().restart();
        } catch (Exception e) {
            log.error("Continuing after error in handleRestart",e);
        }
    }
    
    private static final String jmriLog ="****** JMRI log *******";
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AppsBase.class.getName());
}

