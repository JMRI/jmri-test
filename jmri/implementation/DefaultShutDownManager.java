// DefaultShutDownManager.java

package jmri.implementation;

import jmri.ShutDownManager;
import jmri.ShutDownTask;

import java.util.Enumeration;
import java.util.ArrayList;

/**
 * Manage tasks to be completed when the
 * program shuts down normally.
 * Specifically, allows other object to 
 * register and deregister {@link ShutDownTask} objects, 
 * which are invoked in an orderly way when the program is
 * is commanded to terminate.
 * <p>
 * Operations:
 * <ol>
 * <li>Execute each {@link ShutDownTask} in order, 
 *     allowing it to abort the shutdown if needed.
 * <li>If not aborted, terminate the program.
 * </ol>
 * <p>
 * There can only be one instance of this operating,
 * and it is generally obtained via the instance manager.
 * <p>
 * To avoid being unable to quit the program, which annoys people,
 * an exception in a ShutDownTask is treated as permission to continue after
 * logging.
 *
 * @author      Bob Jacobsen Copyright (C) 2008
 * @version	$Revision: 1.1 $
 */
public class DefaultShutDownManager implements ShutDownManager {

    public DefaultShutDownManager() {}
    
    /**
     * Register a task object for later execution.
     */
    public void register(ShutDownTask s) {
        if (!tasks.contains(s)) {
            tasks.add(s);
        } else {
            log.error("already contains "+s);
        }
    }
    
    /**
     * Deregister a task object.  
     * @throws IllegalArgumentException if task object not currently registered
     */
    public void deregister(ShutDownTask s) {
        if (tasks.contains(s)) {
            tasks.remove(s);
        } else {
            throw new IllegalArgumentException("task not registered");
        }
    }
    
    /**
     * Run the shutdown tasks, and 
     * then terminate the program if not aborted.
     * Does not return under normal circumstances.
     * Does return if the shutdown was aborted by the user,
     * in which case the program should continue to operate.
     */
    public void shutdown() {
        for (int i = tasks.size()-1; i>=0; i--) {
            try {
                ShutDownTask t = (ShutDownTask)tasks.get(i);
                boolean ok = t.execute();
                if (!ok) {
                    log.info("Program termination aborted by "+t.name());
                    return;  // abort early
                }
            } catch (Throwable e) {
                log.error("Error during processing of ShutDownTask "+i+": "+e);
            }
        }
        
        // success
        log.info("Normal termination complete");
        // and now terminate forcefully
        System.exit(0);
    }
    
    ArrayList tasks = new ArrayList();
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DefaultShutDownManager.class.getName());
}

/* @(#)DefaultShutDownManager.java */
