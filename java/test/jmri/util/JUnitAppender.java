package jmri.util;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import junit.framework.Assert;

/**
 * Log4J Appender that just publishes what it sees
 *
 * @author	Bob Jacobsen - Copyright 2007
 * @version	$Revision$
 */
public class JUnitAppender extends ConsoleHandler {

    static java.util.ArrayList<LogRecord> list = new java.util.ArrayList<>();

    @Override
    public void publish(LogRecord record) {
        if (hold) {
            list.add(record);
        } else {
            super.publish(record);
        }
    }

    /**
     * Called once options are set.
     *
     * Initializes the static JUnitAppender instance.
     */
    public void activateOptions() {
        if (JUnitAppender.instance != null) {
            System.err.println("JUnitAppender initialized more than once"); // can't count on logging here
        } else {
            JUnitAppender.instance = this;
        }
    }

    /**
     * Do clean-up at end.
     *
     * Currently just reflects back to super-class.
     */
    @Override
    public synchronized void close() {
        super.close();
    }

    static boolean hold = false;

    static private JUnitAppender instance = null;

    /**
     * Tell appender that a JUnit test is starting.
     * <P>
     * This causes log messages to be held for examination.
     */
    public static void start() {
        hold = true;
    }

    /**
     * Tell appender that the JUnit test is ended.
     * <P>
     * Any queued messages at this point will be passed through to the actual
     * log.
     */
    public static void end() {
        hold = false;
        while (!list.isEmpty()) {
            instance().superpublish(list.remove(0));
        }
    }

    void superpublish(LogRecord l) {
        super.publish(l);
    }

    /**
     * Remove any messages stored up, returning how many there were. This is
     * used to skip over messages that don't matter, e.g. during setting up a
     * test.
     */
    public static int clearBacklog() {
        if (list.isEmpty()) {
            return 0;
        }
        int retval = list.size();
        list.clear();
        return retval;
    }

    /**
     * Verify that no messages were emitted, logging any that were. Does not
     * stop the logging. Clears the accumulated list.
     *
     * @return true if no messages logged
     */
    public static boolean verifyNoBacklog() {
        if (list.isEmpty()) {
            return true;
        }
        while (!list.isEmpty()) {
            instance().superpublish(list.remove(0));
        }
        return false;
    }

    /**
     * Check that the next queued message was of Error severity, and has a
     * specific message.
     * <P>
     * Invokes a JUnit Assert if the message doesn't match
     * @param msg
     */
    public static void assertErrorMessage(String msg) {
        if (list.isEmpty()) {
            Assert.fail("No message present: " + msg);
            return;
        }

        LogRecord evt = list.remove(0);

        while ((evt.getLevel() == Level.INFO) || (evt.getLevel() == Level.FINE)) {
            if (list.isEmpty()) {
                Assert.fail("Only debug/info messages present: " + msg);
                return;
            }
            evt = list.remove(0);
        }

        // check the remaining message, if any
        if (evt.getLevel() != Level.SEVERE) {
            Assert.fail("Level mismatch when looking for ERROR message: \"" + msg + "\" found \"" + evt.getMessage() + "\"");
        }

        if (!evt.getMessage().equals(msg)) {
            Assert.fail("Looking for ERROR message \"" + msg + "\" got \"" + evt.getMessage() + "\"");
        }
    }

    /**
     * Check that the next queued message was of Warn severity, and has a
     * specific message.
     * <P>
     * Invokes a JUnit Assert if the message doesn't match
     */
    public static void assertWarnMessage(String msg) {
        if (list.isEmpty()) {
            Assert.fail("No message present: " + msg);
            return;
        }
        LogRecord evt = list.remove(0);

        while ((evt.getLevel() == Level.INFO) || (evt.getLevel() == Level.FINE)) {
            if (list.isEmpty()) {
                Assert.fail("Only debug/info messages present: " + msg);
                return;
            }
            evt = list.remove(0);
        }

        // check the remaining message, if any
        if (evt.getLevel() != Level.WARNING) {
            Assert.fail("Level mismatch when looking for WARN message: \"" + msg + "\" found \"" + evt.getMessage() + "\"");
        }

        if (!evt.getMessage().equals(msg)) {
            Assert.fail("Looking for WARN message \"" + msg + "\" got \"" + evt.getMessage() + "\"");
        }
    }

    public static JUnitAppender instance() {
        return JUnitAppender.instance;
    }
}
