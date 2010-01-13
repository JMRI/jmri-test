// HelpUtil.java

package jmri.util;

import javax.help.HelpBroker;
import javax.help.HelpSet;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import java.net.URL;

/**
 * Common utility methods for working with Java Help.
 * <P>
 * This class was created to contain common Java Help information.
 * <P>
 * It assumes that Java Help 1.1.8 is in use
 *
 * @author Bob Jacobsen  Copyright 2007
 * @version $Revision: 1.15 $
 */

public class HelpUtil {

    /**
     * @param direct true if this call should complete the help menu
     * by adding the general help
     * @return new Help menu, in case user wants to add more items
     */
    static public JMenu helpMenu(JMenuBar menuBar, final JFrame frame, String ref, boolean direct) {
        JMenu helpMenu = makeHelpMenu(frame, ref, direct);
        menuBar.add(helpMenu);
        return helpMenu;
    }
    
    static java.util.ResourceBundle rb = java.util.ResourceBundle.getBundle("apps.AppsBundle");
    
    static public JMenu makeHelpMenu(final JFrame frame, String ref, boolean direct) {
        if (!initOK()) return null;  // initialization failed
        JMenu helpMenu = new JMenu("Help");
        JMenuItem item = makeHelpMenuItem(ref);
        if (item == null) {
            log.error("Can't make help menu item for "+frame.getTitle());
            return null;
        }
        helpMenu.add(item);
        
        if (direct) {
            item = new JMenuItem(rb.getString("MenuItemHelp"));
            globalHelpBroker.enableHelpOnButton(item, "index", null);
            helpMenu.add(item);
            
            // add standard items
            JMenuItem license = new JMenuItem(rb.getString("MenuItemLicense"));
            helpMenu.add(license);
            license.addActionListener(new apps.LicenseAction());

            JMenuItem directories = new JMenuItem(rb.getString("MenuItemLocations"));
            helpMenu.add(directories);
            directories.addActionListener(new jmri.jmrit.XmlFileLocationAction());
    
            JMenuItem context = new JMenuItem(rb.getString("MenuItemContext"));
            helpMenu.add(context);
            context.addActionListener(new apps.ReportContextAction());

            helpMenu.add(new jmri.jmrit.mailreport.ReportAction());
        }
        return helpMenu;
    }
    
    static public JMenuItem makeHelpMenuItem(String ref) {
        if (!initOK()) return null;  // initialization failed
        
        JMenuItem menuItem = new JMenuItem(rb.getString("MenuItemWindowHelp"));
        globalHelpBroker.enableHelpOnButton(menuItem, ref, null);

        // start help to see what happend
        log.debug("help: "+globalHelpSet.getHomeID()+":"+globalHelpSet.getTitle()
                           +":"+globalHelpSet.getHelpSetURL());

        return menuItem;
    }

    static public void addHelpToComponent(java.awt.Component component, String ref) {
        if (globalHelpBroker!=null)
            globalHelpBroker.enableHelpOnButton(component, ref, null);
    }
    
    static public void displayHelpRef(String ref) {
        try {
            globalHelpBroker.setCurrentID(ref);
            globalHelpBroker.setDisplayed(true);
        } catch (javax.help.BadIDException e) {
            log.error("unable to show help page "+ref+" due to "+e);
        }
    }
    
    static boolean init = false;
    static boolean failed = true;
        
    static boolean initOK() {
        if (!init) {
            init = true;
            try {
                String helpsetName = "help/en/JmriHelp_en.hs";
                URL hsURL;
                try {
                    hsURL = new URL("file:"+helpsetName);
                    globalHelpSet = new HelpSet(null, hsURL);
                } catch (java.lang.NoClassDefFoundError ee) {
                    log.debug("classpath="+System.getProperty("java.class.path","<unknown>"));
                    log.debug("classversion="+System.getProperty("java.class.version","<unknown>"));
                    log.error("Help classes not found, help system omitted");
                    return false;
                } catch (java.lang.Exception e2) {
                    log.error("HelpSet "+helpsetName+" not found, help system omitted");
                    return false;
                }
                globalHelpBroker = globalHelpSet.createHelpBroker();
    
            } catch (java.lang.NoSuchMethodError e2) {
                log.error("Is jh.jar available? Error starting help system: "+e2);
            }
            failed = false;
        }
        return !failed;
    }
    
    static HelpSet globalHelpSet;
    static HelpBroker globalHelpBroker;

    // initialize logging
    static private org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(HelpUtil.class.getName());
}
