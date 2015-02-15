package jmri.util;

import java.awt.Color;
import java.util.HashMap;
import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements a log4j appender which writes to a swing JTextPane
 *
 * @author bender heri
 * @author Randall Wood (C) 2015 See 4/15/2009 Log4J email
 */
public class JTextPaneAppender extends Handler {

    JTextPane myTextPane;
    HashMap<Level, MutableAttributeSet> myAttributeSet;
    private String name;
    private final static Logger log = LoggerFactory.getLogger(JTextPaneAppender.class);

    /**
     * Constructor
     *
     * @param formatter
     * @param name
     * @param aFilterArray
     * @param aTextPane
     */
    public JTextPaneAppender(Formatter formatter, String name, Filter[] aFilterArray, JTextPane aTextPane) {
        this();
        this.setFormatter(formatter);
        this.name = name;
        myTextPane = aTextPane;

        if (aFilterArray != null) {
            for (Filter aFilterArray1 : aFilterArray) {
                if (aFilterArray1 != null) {
                    this.setFilter(aFilterArray1);
                }
            }
        }
        createAttributes();
    }

    /**
     * Constructor
     *
     */
    public JTextPaneAppender() {
        super();
        createAttributes();
    }

    @Override
    public void close() {
        // do nothing
    }

    private void createAttributes() {
        Level[] priorities = new Level[7];
        priorities[0] = Level.SEVERE;
        priorities[1] = Level.WARNING;
        priorities[2] = Level.INFO;
        priorities[3] = Level.CONFIG;
        priorities[4] = Level.FINE;
        priorities[5] = Level.FINER;
        priorities[6] = Level.FINEST;

        myAttributeSet = new HashMap<>();

        for (Level priority : priorities) {
            MutableAttributeSet att = new SimpleAttributeSet();
            myAttributeSet.put(priority, att);
            StyleConstants.setFontSize(att, 14);
        }

        StyleConstants.setForeground(myAttributeSet.get(Level.SEVERE), Color.red);
        StyleConstants.setForeground(myAttributeSet.get(Level.WARNING), Color.orange);
        StyleConstants.setForeground(myAttributeSet.get(Level.INFO), Color.black);
        StyleConstants.setForeground(myAttributeSet.get(Level.CONFIG), Color.black);
        StyleConstants.setForeground(myAttributeSet.get(Level.FINE), Color.black);
        StyleConstants.setForeground(myAttributeSet.get(Level.FINER), Color.black);
        StyleConstants.setForeground(myAttributeSet.get(Level.FINEST), Color.black);
    }

    /**
     * Get current TextPane
     *
     * @return
     */
    public JTextPane getTextPane() {
        return myTextPane;
    }

    /**
     * Set current TextPane
     * <p>
     * @param aTextpane
     */
    public void setTextPane(JTextPane aTextpane) {
        myTextPane = aTextpane;
    }

    private void setColor(Level p, Color v) {
        StyleConstants.setForeground(myAttributeSet.get(p), v);
    }

    private Color getColor(Level p) {
        Color c = StyleConstants.getForeground(myAttributeSet.get(p));
        return c == null ? null : c;
    }

    // ///////////////////////////////////////////////////////////////////
    // option setters and getters
    /**
     * setColorEmerg
     * <p>
     * @param color
     */
    public void setColorEmerg(Color color) {
        setColor(Level.SEVERE, color);
    }

    /**
     * getColorEmerg
     *
     * @return
     */
    public Color getColorEmerg() {
        return getColor(Level.SEVERE);
    }

    /**
     * setColorError
     * <p>
     * @param color
     */
    public void setColorError(Color color) {
        setColor(Level.SEVERE, color);
    }

    /**
     * getColorError
     *
     * @return
     */
    public Color getColorError() {
        return getColor(Level.SEVERE);
    }

    /**
     * setColorWarn
     * <p>
     * @param color
     */
    public void setColorWarn(Color color) {
        setColor(Level.WARNING, color);
    }

    /**
     * getColorWarn
     *
     * @return
     */
    public Color getColorWarn() {
        return getColor(Level.WARNING);
    }

    /**
     * setColorInfo
     * <p>
     * @param color
     */
    public void setColorInfo(Color color) {
        setColor(Level.INFO, color);
    }

    /**
     * getColorInfo
     *
     * @return
     */
    public Color getColorInfo() {
        return getColor(Level.INFO);
    }

    /**
     * setColorDebug
     * <p>
     * @param color
     */
    public void setColorDebug(Color color) {
        setColor(Level.FINE, color);
    }

    /**
     * getColorDebug
     *
     * @return
     */
    public Color getColorDebug() {
        return getColor(Level.FINE);
    }

    /**
     * Sets the font size of all Level's
     * <p>
     * @param aSize
     */
    public void setFontSize(int aSize) {
        this.myAttributeSet.values().stream().forEach((e) -> {
            StyleConstants.setFontSize(e, aSize);
        });
    }

    /**
     * Sets the font size of a particular Level
     *
     * @param aSize
     * @param aLevel
     */
    public void setFontSize(int aSize, Level aLevel) {
        MutableAttributeSet set = myAttributeSet.get(aLevel);
        if (set != null) {
            StyleConstants.setFontSize(set, aSize);
        }
    }

    /**
     * Get the font size for a particular logging level
     *
     * @param aLevel
     * @return
     */
    public int getFontSize(Level aLevel) {
        AttributeSet attrSet = myAttributeSet.get(aLevel);
        if (attrSet == null) {
            throw new IllegalArgumentException("Unhandled Level: " + aLevel.toString());
        }

        return StyleConstants.getFontSize(attrSet);
    }

    /**
     * Sets the font name of all known Level's
     *
     * @param aName
     */
    public void setFontName(String aName) {
        this.myAttributeSet.values().stream().forEach((e) -> {
            StyleConstants.setFontFamily(e, aName);
        });
    }

    /**
     * setFontName
     *
     * @param aName
     * @param aLevel
     */
    public void setFontName(String aName, Level aLevel) {
        MutableAttributeSet set = myAttributeSet.get(aLevel);
        if (set != null) {
            StyleConstants.setFontFamily(set, aName);
        }
    }

    /**
     * Retrieves the font name of a particular Level
     *
     * @param aLevel
     * @return
     */
    public String getFontName(Level aLevel) {
        AttributeSet attrSet = myAttributeSet.get(aLevel);

        if (attrSet == null) {
            throw new IllegalArgumentException("Unhandled Level: " + aLevel.toString());
        } // if attrSet == null

        return StyleConstants.getFontFamily(attrSet);
    }

    @Override
    public void publish(LogRecord record) {
        if (!this.isLoggable(record)) {
            return;
        }

        if (this.myTextPane == null) {
            log.warn("TextPane is not initialized");
            return;
        }

        String text = this.getFormatter().formatMessage(record);
        StackTraceElement[] stackTrace = record.getThrown().getStackTrace();
        if (stackTrace != null) {
            StringBuilder sb = new StringBuilder(text);
            for (StackTraceElement element : stackTrace) {
                sb.append("    ").append(element.toString()).append("\n");
            }

            text = sb.toString();
        }

        StyledDocument myDoc = myTextPane.getStyledDocument();

        try {
            myDoc.insertString(myDoc.getLength(), text, myAttributeSet.get(record.getLevel()));
        } catch (BadLocationException badex) {
            System.err.println(badex);  // can't log this, as it would be recursive error
        }

        myTextPane.setCaretPosition(myDoc.getLength());
    }

    @Override
    public void flush() {
        // do nothing
    }
}
