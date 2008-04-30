// SwingShutDownTask.java

package jmri.implementation.swing;

import jmri.implementation.AbstractShutDownTask;
import java.util.ResourceBundle;
import java.awt.Component;
import javax.swing.JOptionPane;

/**
 * Provides a base for using Swing to ask if shutdown should
 * conditionally continue.
 * <p>
 * Sequence:
 * <ol>
 * <li>checkReady determines if ready to shutdown. If so, 
 * return ready.
 * <li>Issue a prompt, asking if the user wants to continue or do something else
 * <li>Recheck until something decided.
 * </ul>
 *
 * <p>
 * If no "action" name is provided, only the continue and cancel options are shown.
 *
 * @author      Bob Jacobsen Copyright (C) 2008
 * @version	$Revision: 1.1 $
 */
public class SwingShutDownTask extends AbstractShutDownTask {

    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.implementation.swing.MessageBundle");
    
    /** 
     * Constructor specifies the warning message
     * and action to take
     */
    public SwingShutDownTask(String name, String warning, String action, Component component) {
        super(name);
        this.component = component;
        this.warning = warning;
        this.action = action;
    }
    
    String warning;
    String action;
    Component component;
    
    /**
     * Take the necessary action.
     * @return true if the shutdown should continue, false
     * to abort.
     */
    public boolean execute() {
        while (!checkReady()) {
            // issue prompt
            Object[] possibleValues;
            if (action!=null) possibleValues = new Object[] {rb.getString("ButtonContinue"), 
                                       rb.getString("ButtonAbort"), 
                                       action};
            else possibleValues = new Object[] {rb.getString("ButtonContinue"), 
                                       rb.getString("ButtonAbort")}; 

            int selectedValue = JOptionPane.showOptionDialog(component,
                                                             warning,
                                                             rb.getString("ShutDownWarningTitle"),
                                                             JOptionPane.DEFAULT_OPTION,
                                                             JOptionPane.WARNING_MESSAGE, null,
                                                             possibleValues, possibleValues[possibleValues.length-1]);
            if (selectedValue == 1) {
                // abort quit
                return false;
            } else if (selectedValue == 0) {
                // quit anyway
                return true;
            } else if (selectedValue == 2) {
                // take action and try again
                return doAction();
            } else {
                // unexpected value, log but continue
                // (Can see -1 if dialog dismissed)
                log.error("unexpected selection: "+selectedValue);
                return true;
            }
        }
        // break out of loop when ready to continue       
        return true;
    }

    
    /**
     * Provide a subclass-specific check as to whether it's
     * OK to shutdown.  If not, issue a prompt before continuing.
     * Default implementation never passes, causing message to be emitted.
     * @return true if ready to shutdown, and no prompt needed. false to present dialog
     * before shutdown proceeds
     */
    protected boolean checkReady() {
        return false;
    }
    
    /**
     * Provide a subclass-specific method to handle the
     * request to fix the problem. This is a dummy implementation,
     * intended to be overloaded.
     * @return true if ready to shutdown, false to end shutdown
     */
    protected boolean doAction() {
        return true;
    }
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SwingShutDownTask.class.getName());

}

/* @(#)SwingShutDownTask.java */
