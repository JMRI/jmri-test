package jmri.util;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Vector;
import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A standalone window for receiving and displaying log outputs
 * <p>
 * Singleton pattern
 * <p>
 * The original version deferred initialization onto the Swing thread; this
 * version does it inline, and must be invoked from the Swing thread.
 * <p>
 * The Frame and the appender are not shown by initializing it but only made
 * ready to receive all log output. It can later be set to visible if desired.
 * TODO: implement also a enable() and disable() method in order to have a
 * minimal impact on performance if not used.
 *
 * @author bender heri See 4/15/2009 Log4J email
 */
public class JLogoutputFrame {

    private static final Logger myLog = LoggerFactory.getLogger(JLogoutputFrame.class);

    private static Formatter myLayout = new SimpleFormatter();
    // "%d{HH:mm:ss.SSS} (%6r) %-5p [%-7t] %F:%L %x - %m%n" );
    private static Vector<Filter> myFilters = new Vector<Filter>();

    private static JLogoutputFrame myInstance = null;
    private JFrame myMainFrame = null;
    private JTextPaneAppender myAppender = null;

    /**
     * Retrieves the singleton instance
     */
    public static JLogoutputFrame getInstance() {
        if (myInstance == null) {
            initInstance();
        } // if myInstance == null

        return myInstance;

    }

    /**
     * initInstance
     * <p>
     */
    private static void initInstance() {
        myInstance = new JLogoutputFrame();
    }

    /**
     * Constructor
     *
     */
    private JLogoutputFrame() {
        super();

        myLog.debug("entering init");

        myMainFrame = createMainFrame();

        myLog.debug("leaving init");
    }

    /**
     * createMainFrame
     * <p>
     * @return the initialized main frame
     */
    private JFrame createMainFrame() {
//        JPanel messagePane = createMessagePane();

        JFrame result = new JFrame();
        result.setPreferredSize(new Dimension(400, 300));

        JTextPane textPane = new JTextPane();
        myAppender = createAppender(textPane);
        textPane.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setPreferredSize(new Dimension(400, 300));
        result.getContentPane().add(scrollPane, BorderLayout.CENTER);

        String fontFamily = "Courier New";
        Font font = new Font(fontFamily, Font.PLAIN, 1);
//        Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
//        for ( int i = 0; i < fonts.length; i++ )
//        {
//            if ( fonts[i].getFamily().equals( fontFamily ) )
//            {
//                textPane.setFont( fonts[i] );
//                break;
//            } // if fonts[i].getFamily().equals( fontFamily )
//        } // for i
        textPane.setFont(font);

        result.pack();

        result.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        return result;
    }

    /**
     * Outputs a message only to the appender which belongs to this frame
     *
     * @param aLevel
     * @param aMsg
     */
    public void log(Level aLevel, String aMsg) {
        if (myAppender == null) {
            return;
        }

        LogRecord record = new LogRecord(aLevel, aMsg);
        this.myAppender.publish(record);
    }

    /**
     * Creates the appender and adds it to all known Loggers whose additivity
     * flag is false, incl. root logger
     *
     * @param aTextPane
     * @return A configured Appender
     */
    public JTextPaneAppender createAppender(JTextPane aTextPane) {
        JTextPaneAppender result = new JTextPaneAppender(myLayout, "Debug", new Filter[0], aTextPane);

        // Note that loggers that explicitly do not inherit configuration from
        // the Global logger will not have this handler added.
        java.util.logging.Logger.getGlobal().addHandler(result);

        return result;
    }

    /**
     * @return the mainFrame
     */
    public JFrame getMainFrame() {
        return myMainFrame;
    }

    /**
     * @return the myLayout
     */
    public static Formatter getLayout() {
        return myLayout;
    }

    /**
     * @param aLayout the Layout to set
     */
    public static void setMyPatternLayout(Formatter aLayout) {
        if (myInstance != null) {
            // TODO: enable swiching layout
            throw new IllegalStateException("Cannot switch Layout after having initialized the frame");
        } // if myInstance != null

        myLayout = aLayout;
    }

    /**
     * @return the myFilters
     */
    public static Vector<Filter> getFilters() {
        return myFilters;
    }

    /**
     * @param aFilters the Filters to set
     */
    public static void setFilters(Vector<Filter> aFilters) {
        if (myInstance != null) {
            // TODO: enable swiching filters
            throw new IllegalStateException("Cannot change filters after having initialized the frame");
        } // if myInstance != null

        myFilters = aFilters;
    }

    /**
     * @param aFilter the Filter to be added
     */
    public static void addFilter(Filter aFilter) {
        if (myInstance != null) {
            // TODO: enable adding filters
            throw new IllegalStateException("Cannot add new filter after having initialized the frame");
        } // if myInstance != null

        myFilters.add(aFilter);
    }

}
