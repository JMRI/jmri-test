// JmriPlugin.java

package jmri;

import javax.swing.*;

/**
 * Method for invoking user code at startup time.
 * <P>
 * This class provides a null static member.  By replacing
 * it with another implemention, the user can update configuration,
 * etc at startup time.
 *
 * @author			Bob Jacobsen Copyright (C) 2003
 * @version			$Revision: 1.1 $
 */
public class JmriPlugin {
     public static void start(JFrame mainFrame, JMenuBar menuBar) {}
}

/* @(#)JmriPlugin.java */
