// LocoTools.java

package apps.LocoTools;

import apps.AppConfigPanel;
import apps.Apps;

import java.text.MessageFormat;

import javax.swing.JFrame;
import javax.swing.JMenuBar;

/**
 * The JMRI program of LocoNet tools.
 * <P>
 * If an argument is provided at startup, it will be used as the name of
 * the configuration file.  Note that this is just the name, not the path;
 * the file is searched for in the usual way, first in the preferences tree and then in
 * xml/
 * @author	Bob Jacobsen   Copyright 2003
 * @version     $Revision: 1.27 $
 */
public class LocoTools extends Apps {

    LocoTools(JFrame p) {
        super(p);
    }

    protected AppConfigPanel newPrefs() {
        return new AppConfigPanel(configFilename, 1);
    }

    protected void systemsMenu(JMenuBar menuBar, JFrame frame) {
        // separate LocoNet menu
        menuBar.add(new jmri.jmrix.loconet.LocoNetMenu());
    }

    protected String line1() {
        return MessageFormat.format(rb.getString("LocoToolsVersionCredit"),
                                new String[]{jmri.Version.name()});
    }

    protected String line2() {
        return "http://jmri.sf.net/LocoTools.html ";
    }

    // Main entry point
    public static void main(String args[]) {

        // show splash screen early
        splash(true);

        initLog4J();
        log.info(apps.Apps.startupInfo("LocoTools"));

        setConfigFilename("LocoToolsConfig2.xml", args);
        JFrame f = new JFrame("LocoTools");
        createFrame(new LocoTools(f), f);

        log.info("main initialization done");
        splash(false);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LocoTools.class.getName());
}


