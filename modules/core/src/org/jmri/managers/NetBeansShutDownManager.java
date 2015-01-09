package org.jmri.managers;

import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.ListIterator;
import jmri.ShutDownManager;
import jmri.ShutDownTask;
import org.jmri.application.JmriApplication;
import org.openide.LifecycleManager;
import org.openide.modules.OnStop;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JMRI Shutdown manager that triggers restart or shutdown using the OpenIDE
 * LifecycleManager for integration into the NetBeans Platform.
 *
 * @author Randall Wood
 */
@ServiceProvider(service = NetBeansShutDownManager.class)
@OnStop
public class NetBeansShutDownManager implements ShutDownManager, Runnable {

    private final ArrayList<ShutDownTask> tasks = new ArrayList<>();
    static boolean shuttingDown = false;
    static Logger log = LoggerFactory.getLogger(NetBeansShutDownManager.class);

    public NetBeansShutDownManager() {
        if (GraphicsEnvironment.isHeadless()) {
            // This shutdown hook allows us to perform a clean shutdown when
            // running in headless mode and SIGINT (Ctrl-C) or SIGTERM. It
            // calls ApplicationStop.run() after ensuring the ShutDownManager
            // will not trigger any calls to System.exit() since calling
            // System.exit() within a shutdown hook will cause the application
            // to hang.
            Runtime.getRuntime().addShutdownHook(new Thread() {

                @Override
                public void run() {
                    NetBeansShutDownManager.this.run();
                }
            });
        }
    }

    @Override
    public void register(ShutDownTask sdt) {
        if (!this.tasks.add(sdt)) {
            log.error("already contains {}", sdt.name());
        }
    }

    @Override
    public void deregister(ShutDownTask sdt) {
        if (!shuttingDown) { // if we are shutting down, leave the tasks list
            if (!this.tasks.remove(sdt)) {
                log.error("does not contain {}", sdt.name());
            }
        }
    }

    @Override
    public Boolean restart() {
        return shutdown(0, true);
    }

    @Override
    public Boolean shutdown() {
        return shutdown(0, false);
    }

    public Boolean shutdown(int status) {
        return this.shutdown(status, false);
    }

    public Boolean shutdown(int status, Boolean restart) {
        if (!shuttingDown) {
            if (restart) {
                LifecycleManager.getDefault().markForRestart();
            }
            LifecycleManager.getDefault().exit(status);
        }
        return true;
    }

    private Boolean runShutdownTasks() {
        // Copy this.tasks so a ConcurrentModificationException is not thrown when a task deregisters itself.
        // Use a ListIterator starting at the end of the list to iterate backwards.
        ListIterator<ShutDownTask> li = (new ArrayList<>(this.tasks)).listIterator(this.tasks.size());
        while (li.hasPrevious()) {
            ShutDownTask task = li.previous();
            try {
                if (!task.execute()) {
                    shuttingDown = false;
                    log.info("Program termination aborted by ShutDownTask {}", task.name());
                    return false;
                }
            } catch (Throwable e) {
                log.error("Error processing ShutDownTask {}", task.name(), e);
            }
        }
        return true;
    }

    /**
     * If a {@link org.jmri.application.JmriApplication} service provider
     * exists, call {@link org.jmri.application.JmriApplication#stop()} or its
     * overriding method.
     */
    @Override
    public void run() {
        if (!shuttingDown) {
            shuttingDown = true;
            JmriApplication application = Lookup.getDefault().lookup(JmriApplication.class);
            if (application != null) {
                application.stop();
            }
            this.runShutdownTasks();
            log.info("Normal termination complete");
        }
    }

}
