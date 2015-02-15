// Log4JTreePane.java

package jmri.jmrit.log;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Show the current Log4J Logger tree; not dynamic.
 *
 * @author Bob Jacobsen  Copyright 2010
 * @since 2.9.4
 * @version $Revision$
 */

public class Log4JTreePane extends jmri.util.swing.JmriPanel {

    /**
     * Provide a help target string which an enclosing
     * frame can provide as a help reference.
     */
    //public String getHelpTarget() { return "Acknowledgements.shtml"; }

    /**
	 * 
	 */
	private static final long serialVersionUID = -8179937923267615464L;

	/**
     * Provide a recommended title for an enclosing frame.
     */
    @Override
    public String getTitle() { 
        return Bundle.getMessage("MenuItemLogTreeAction"); 
    }
    
    /**
     * Provide menu items
     */
    //public List<JMenu> getMenus() { return null; }
    
    public Log4JTreePane() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }
    
    /*
     If a Logger's level is null, get it's parent Logger's level, recursing
     through Loggers until a nonnull level is found (the global Logger has
     a nonnull level).
     */
    private Level getLevel(Logger logger) {
        return (logger.getLevel() != null)
                ? logger.getLevel()
                : this.getLevel(logger.getParent());
    }
    
    /**
     * 2nd stage of initialization, invoked after
     * the constructor is complete.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void initComponents() {
        List<String> list = new ArrayList<>();
        for (Enumeration<String> e = LogManager.getLogManager().getLoggerNames(); e.hasMoreElements();) {
            Logger l = Logger.getLogger(e.nextElement());
            if (l != null) {
                list.add(l.getName() + " - "
                        + (l.getLevel() != null
                                ? "[" + l.getLevel().toString() + "]"
                                : "{" + this.getLevel(l).toString() + "}"));
            }
        }
        java.util.Collections.sort(list);
        StringBuilder result = new StringBuilder();
        for (String s : list) {
            result.append(s).append("\n");
        }
        
        JTextArea text = new JTextArea();
        text.setText(result.toString());
        JScrollPane scroll = new JScrollPane(text);
        add(scroll);
        
        // start scrolled to top
        text.setCaretPosition(0);
        JScrollBar b = scroll.getVerticalScrollBar();
        b.setValue(b.getMaximum());
    }
    
    /**
     * 3rd stage of initialization, invoked after
     * Swing components exist.
     */
    @Override
    public void initContext(Object context) throws Exception {}
    
    @Override
    public void dispose() {}
}
