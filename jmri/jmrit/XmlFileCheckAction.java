// XmlFileCheckAction.java

package jmri.jmrit;

import java.awt.event.*;
import java.io.*;

import javax.swing.*;

/**
 * Make sure an XML file is readable, and validates OK
 *
 * @author	Bob Jacobsen   Copyright (C) 2001
 * @version	$Revision: 1.2 $
 * @see         jmri.jmrit.XmlFile
 */
public class XmlFileCheckAction extends AbstractAction {

    public XmlFileCheckAction(String s, JPanel who) {
        super(s);
        _who = who;
    }

    JFileChooser fci = new JFileChooser(" ");

    JPanel _who;

    public void actionPerformed(ActionEvent e) {

        // request the filename from an open dialog
        fci.rescanCurrentDirectory();
        int retVal = fci.showOpenDialog(_who);
        // handle selection or cancel
        if (retVal == JFileChooser.APPROVE_OPTION) {
            File file = fci.getSelectedFile();
            if (log.isInfoEnabled()) log.info("located file "+file+" for XML processing");
            // handle the file (later should be outside this thread?)
            try {
                readFile(file);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(_who,"Error: "+ex);
                return;
            }
            JOptionPane.showMessageDialog(_who,"OK");
            if (log.isInfoEnabled()) log.info("parsing complete");

        }
        else log.info("XmlFileCheckAction cancelled in open dialog");
    }

    /**
     * Ask SAX to read and verify a file
     */
    void readFile(File file) throws org.jdom.JDOMException, java.io.FileNotFoundException {
        XmlFile xf = new XmlFile(){};   // odd syntax is due to XmlFile being abstract

        xf.rootFromFile(file);

    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XmlFileCheckAction.class.getName());
}
