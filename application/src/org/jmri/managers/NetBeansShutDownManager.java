/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jmri.managers;

import java.util.ArrayList;
import java.util.ListIterator;
import jmri.ShutDownManager;
import jmri.ShutDownTask;
import org.openide.LifecycleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author rhwood
 */
public class NetBeansShutDownManager implements ShutDownManager {

    private ArrayList<ShutDownTask> tasks = new ArrayList<ShutDownTask>();
    static Boolean shuttingDown = false;
    static Logger log = LoggerFactory.getLogger(NetBeansShutDownManager.class);

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
            shuttingDown = true;
            // Copy this.tasks so a ConcurrentModificationException is not thrown when a task deregisters itself.
            // Use a ListIterator starting at the end of the list to iterate backwards.
            ListIterator<ShutDownTask> li = (new ArrayList<ShutDownTask>(this.tasks)).listIterator(this.tasks.size());
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
            log.info("Normal termination complete");
            if (restart) {
                LifecycleManager.getDefault().markForRestart();
            }
            LifecycleManager.getDefault().exit(status);
        }
        return false;
    }
}
