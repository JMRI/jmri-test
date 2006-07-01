// PrintDecoderListAction.java

package jmri.jmrit.decoderdefn;

import jmri.*;
import jmri.util.davidflanagan.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import com.sun.java.util.collections.List;


/**
 * Action to print a summary of available decoder definitions
 * <P>
 * This uses the older style printing, for compatibility with Java 1.1.8 in
 * Macintosh MRJ
 *
 * @author	Bob Jacobsen   Copyright (C) 2003
 * @version     $Revision: 1.2 $
 */
public class PrintDecoderListAction  extends AbstractAction {


    public PrintDecoderListAction(Frame frame) {
        this("Print decoder definitions...", frame);
    }

    public PrintDecoderListAction(String actionName, Frame frame) {
        super(actionName);
        mFrame = frame;
    }

    /**
     * Frame hosting the printing
     */
    Frame mFrame;

    public void actionPerformed(ActionEvent e) {

        // obtain a HardcopyWriter to do this
        HardcopyWriter writer = null;
        try {
            writer = new HardcopyWriter(mFrame, "DecoderPro V"+Version.name()+" Decoder Definitions", 10, .5, .5, .5, .5);
        } catch (HardcopyWriter.PrintCanceledException ex) {
            log.debug("Print cancelled");
            return;
        }

        // add the image
        ImageIcon icon = new ImageIcon(ClassLoader.getSystemResource("resources/decoderpro.gif"));
        // we use an ImageIcon because it's guaranteed to have been loaded when ctor is complete
        writer.write(icon.getImage(), new JLabel(icon));

        // Loop through the decoder index, printing as needed
        String lastMfg = "";
        String lastFamily = "";

        DecoderIndexFile f = DecoderIndexFile.instance();
        List l = f.matchingDecoderList(null, null, null, null, null); // take all
        int i=-1;
        log.debug("Roster list size: "+l.size());
        for (i = 0; i<l.size(); i++) {
            DecoderFile d = (DecoderFile)l.get(i);
            if (!d.getMfg().equals(lastMfg)) {
                printMfg(d, writer);
                lastMfg = d.getMfg();
                lastFamily = "";
            }
            if (!d.getFamily().equals(lastFamily)) {
                printFamily(d, writer);
                lastFamily = d.getFamily();
            }
            if (!d.getFamily().equals(d.getModel())) printEntry(d,writer);
        }

        // and force completion of the printing
        writer.close();
    }

    void printEntry(DecoderFile d, HardcopyWriter w) {
        try {
            String s ="\n                       "+d.getModel();
            w.write(s, 0, s.length());
        } catch (java.io.IOException e) {
            log.error("Error printing: "+e);
        }
    }

    void printMfg(DecoderFile d, HardcopyWriter w) {
        try {
            String s ="\n\n"+d.getMfg();
            w.write(s, 0, s.length());
        } catch (java.io.IOException e) {
            log.error("Error printing: "+e);
        }
    }

    void printFamily(DecoderFile d, HardcopyWriter w) {
        try {
            String s ="\n           "+d.getFamily();
            w.write(s, 0, s.length());
        } catch (java.io.IOException e) {
            log.error("Error printing: "+e);
        }
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PrintDecoderListAction.class.getName());
}
