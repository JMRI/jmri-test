//AbstractMonPane.java

package jmri.jmrix;

import java.awt.*;
import java.io.*;
import java.text.*;
import java.util.*;

import javax.swing.*;
import javax.swing.text.*;

import jmri.util.swing.*;

/**
 * Abstact base class for JPanels displaying communications monitor information
 * @author	Bob Jacobsen   Copyright (C) 2001, 2003, 2010
 * @version	$Revision: 1.1 $
 */
public abstract class AbstractMonPane extends JmriPanel  {

    // template functions to fill in
    public abstract String getTitle();    // provide the title for the frame

    /**
     * Initialize the data source.
     * <P>
     * This is invoked at the end of the GUI initialization phase.
     * Subclass implementations should connect to their data source here.
     */
    protected abstract void init();

    // the subclass also needs a dispose() method to close any specific communications; call super.dispose()
    public void dispose() { super.dispose();}
    // you'll also have to add the message(Foo) members to handle info to be logged.
    // these should call nextLine(String line, String raw) with their updates

    // member declarations
    protected JButton clearButton = new JButton();
    protected JToggleButton freezeButton = new JToggleButton();
    protected JScrollPane jScrollPane1 = new JScrollPane();
    protected JTextArea monTextPane = new JTextArea();
    protected JButton startLogButton = new JButton();
    protected JButton stopLogButton = new JButton();
    protected JCheckBox rawCheckBox = new JCheckBox();
    protected JCheckBox timeCheckBox = new JCheckBox();
    protected JButton openFileChooserButton = new JButton();
    protected JTextField entryField = new JTextField();
    protected JButton enterButton = new JButton();

	// for locking
	AbstractMonPane self;
	
    // to find and remember the log file
    final javax.swing.JFileChooser logFileChooser = new JFileChooser(jmri.jmrit.XmlFile.userFileLocationDefault());

    public AbstractMonPane() {
	    super();
    	self = this;
    }

    public void initComponents() throws Exception {
        // the following code sets the frame's initial state

        clearButton.setText("Clear screen");
        clearButton.setVisible(true);
        clearButton.setToolTipText("Clear monitoring history");

        freezeButton.setText("Freeze screen");
        freezeButton.setVisible(true);
        freezeButton.setToolTipText("Stop display scrolling");

        enterButton.setText("Add Message");
        enterButton.setVisible(true);
        enterButton.setToolTipText("Add a text message to the log");

        monTextPane.setVisible(true);
        monTextPane.setToolTipText("Command and reply monitoring information appears here");
        monTextPane.setEditable(false);

        entryField.setToolTipText("Enter text here, then click button to include it in log");
        // cap vertical size to avoid over-growth
        Dimension currentPreferredSize = entryField.getPreferredSize();
        Dimension currentMaximumSize = entryField.getMaximumSize();
        currentMaximumSize.height = currentPreferredSize.height;
        currentMaximumSize.width = currentMaximumSize.width;
        entryField.setMaximumSize(currentMaximumSize);
        
        // fix a width for current character set
        JTextField t = new JTextField(80);
        int x = jScrollPane1.getPreferredSize().width+t.getPreferredSize().width;
        int y = jScrollPane1.getPreferredSize().height+10*t.getPreferredSize().height;

        jScrollPane1.getViewport().add(monTextPane);
        jScrollPane1.setPreferredSize(new Dimension(x, y));
        jScrollPane1.setVisible(true);

        startLogButton.setText("Start logging");
        startLogButton.setVisible(true);
        startLogButton.setToolTipText("start logging to file");

        stopLogButton.setText("Stop logging");
        stopLogButton.setVisible(true);
        stopLogButton.setToolTipText("Stop logging to file");

        rawCheckBox.setText("Show raw data");
        rawCheckBox.setVisible(true);
        rawCheckBox.setToolTipText("If checked, show the raw traffic in hex");

        timeCheckBox.setText("Show timestamps");
        timeCheckBox.setVisible(true);
        timeCheckBox.setToolTipText("If checked, show timestamps before each message");

        openFileChooserButton.setText("Choose log file");
        openFileChooserButton.setVisible(true);
        openFileChooserButton.setToolTipText("Click here to select a new output log file");

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // add items to GUI
        add(jScrollPane1);

        JPanel paneA = new JPanel();
	    paneA.setLayout(new BoxLayout(paneA, BoxLayout.Y_AXIS));
	
        JPanel pane1 = new JPanel();
	    pane1.setLayout(new BoxLayout(pane1, BoxLayout.X_AXIS));
        pane1.add(clearButton);
        pane1.add(freezeButton);
        pane1.add(rawCheckBox);
        pane1.add(timeCheckBox);
	    paneA.add(pane1);

        JPanel pane2 = new JPanel();
	    pane2.setLayout(new BoxLayout(pane2, BoxLayout.X_AXIS));
        pane2.add(openFileChooserButton);
        pane2.add(startLogButton);
        pane2.add(stopLogButton);
	    paneA.add(pane2);

        JPanel pane3 = new JPanel();
	    pane3.setLayout(new BoxLayout(pane3, BoxLayout.X_AXIS));
        pane3.add(enterButton);
        pane3.add(entryField);
	    paneA.add(pane3);

        add(paneA);

        // connect actions to buttons
        clearButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    clearButtonActionPerformed(e);
                }
            });
        startLogButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    startLogButtonActionPerformed(e);
                }
            });
        stopLogButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    stopLogButtonActionPerformed(e);
                }
            });
        openFileChooserButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    openFileChooserButtonActionPerformed(e);
                }
            });

        enterButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    enterButtonActionPerformed(e);
                }
            });

        // set file chooser to a default
        logFileChooser.setSelectedFile(new File("monitorLog.txt"));

        // connect to data source
        init();

    }

    /**
     * Define help menu for this window.
     * <p>
     * By default, provides a generic help page
     * that covers general features.  Specific
     * implementations can override this to 
     * show their own help page if desired.
     */
    public String getHelpTarget() {
    	return "package.jmri.jmrix.AbstractMonFrame";
    }
    
    public void nextLine(String line, String raw) {
        // handle display of traffic
        // line is the traffic in 'normal form', raw is the "raw form"
        // Both should be one or more well-formed lines, e.g. end with \n
        StringBuffer sb = new StringBuffer(120);

        // display the timestamp if requested
        if ( timeCheckBox.isSelected() ) {
            sb.append(df.format(new Date())).append( ": " ) ;
        }

        // display the raw data if requested
        if ( rawCheckBox.isSelected() ) {
            sb.append( '[' ).append(raw).append( "]  " );
        }

        // display decoded data
        sb.append(line);
		synchronized( self )
		{
			linesBuffer.append( sb.toString() );
		}

        // if not frozen, display it in the Swing thread
        if (!freezeButton.isSelected()) {
            Runnable r = new Runnable() {
                public void run() {
					synchronized( self )
					{
						monTextPane.append( linesBuffer.toString() );
						int LineCount = monTextPane.getLineCount() ;
						if( LineCount > MAX_LINES )
						{
							LineCount -= MAX_LINES ;
							try {
								int offset = monTextPane.getLineStartOffset(LineCount);
								monTextPane.getDocument().remove(0, offset ) ;
							}
							catch (BadLocationException ex) {
							}
						}
						linesBuffer.setLength(0) ;
					}
                }
            };
            javax.swing.SwingUtilities.invokeLater(r);
        }

        // if requested, log to a file.
        if (logStream != null) {
            String logLine = sb.toString();
            if (!newline.equals("\n")) {
                // have to massage the line-ends
                int i = 0;
                int lim = sb.length();
                StringBuffer out = new StringBuffer(sb.length()+10);  // arbitrary guess at space
                for ( i = 0; i<lim; i++) {
                    if (sb.charAt(i) == '\n')
                        out.append(newline);
                    else
                        out.append(sb.charAt(i));
                }
                logLine = new String(out);
            }
            logStream.print(logLine);
        }
    }

    String newline = System.getProperty("line.separator");

    public synchronized void clearButtonActionPerformed(java.awt.event.ActionEvent e) {
        // clear the monitoring history
		synchronized( linesBuffer )
		{
			linesBuffer.setLength(0);
			monTextPane.setText("");
		}
    }

    public synchronized void startLogButtonActionPerformed(java.awt.event.ActionEvent e) {
        // start logging by creating the stream
        if ( logStream==null) {  // successive clicks don't restart the file
            // start logging
            try {
                logStream = new PrintStream (new FileOutputStream(logFileChooser.getSelectedFile()));
            } catch (Exception ex) {
                log.error("exception "+ex);
            }
        }
    }

    public synchronized void stopLogButtonActionPerformed(java.awt.event.ActionEvent e) {
        // stop logging by removing the stream
        if (logStream!=null) {
            logStream.flush();
            logStream.close();
        }
        logStream = null;
    }

    public void openFileChooserButtonActionPerformed(java.awt.event.ActionEvent e) {
        // start at current file, show dialog
        int retVal = logFileChooser.showSaveDialog(this);

        // handle selection or cancel
        if (retVal == JFileChooser.APPROVE_OPTION) {
            boolean loggingNow = (logStream != null);
            stopLogButtonActionPerformed(e);  // stop before changing file
            //File file = logFileChooser.getSelectedFile();
            // if we were currently logging, start the new file
            if (loggingNow) startLogButtonActionPerformed(e);
        }
    }

    public void enterButtonActionPerformed(java.awt.event.ActionEvent e) {
        nextLine(entryField.getText()+"\n", "");
    }
    
    public synchronized String getFrameText() {
        return new String(linesBuffer);
    }

    PrintStream logStream = null;

    // to get a time string
    DateFormat df = new SimpleDateFormat("HH:mm:ss.SSS");

	StringBuffer linesBuffer = new StringBuffer();
	static private int MAX_LINES = 500 ;
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractMonFrame.class.getName());
}
