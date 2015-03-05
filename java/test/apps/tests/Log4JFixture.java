package apps.tests;

import java.util.logging.Level;
import java.util.logging.Logger;
import jmri.util.JUnitAppender;

public class Log4JFixture {

    public Log4JFixture() {
        initLogging();
    }

    static public void setUp() {
        // always init logging if needed
        initLogging();
        //
        try {
            JUnitAppender.start();
        } catch (Throwable e) {
            System.err.println("Could not start JUnitAppender, but test continues:\n" + e);
        }
    }

    static public void tearDown() {
        JUnitAppender.end();
    }

    static boolean log4jinit = true;

    public static void initLogging() {
        if (log4jinit) {
            log4jinit = false;
            // initialize log4j - from logging control file (lcf) if you can find it
            String logFile = "tests.lcf";
            if (new java.io.File(logFile).canRead()) {
                System.out.println(logFile + " configures logging");
                org.apache.log4j.PropertyConfigurator.configure(logFile);
            } else {
                System.out.println(logFile + " not found, using default logging");

                // create an appender, and load it with a default pattern
                JUnitAppender a = new JUnitAppender();
                a.activateOptions();

                // only log warnings and above
                Logger.getGlobal().setLevel(Level.WARNING);
            }
            // install default exception handlers
            System.setProperty("sun.awt.exception.handler", jmri.util.exceptionhandler.AwtHandler.class.getName());
            Thread.setDefaultUncaughtExceptionHandler(new jmri.util.exceptionhandler.UncaughtExceptionHandler());
        }
    }
}
