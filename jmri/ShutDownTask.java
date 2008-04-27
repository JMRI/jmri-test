// ShutDownTask.java

package jmri;

import java.util.Enumeration;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;


/**
 * Execute a specific task before the program terminates.
 *
 * @author      Bob Jacobsen Copyright (C) 2008
 * @version	$Revision: 1.1 $
 */
public interface ShutDownTask {

    /**
     * Take the necessary action.
     * @return true if the shutdown should continue, false
     * to abort.
     */
    public boolean execute();

    /** 
     * Name to be provided to the user
     * when information about this task is presented.
     */
    public String name();
}

/* @(#)ShutDownTask.java */
