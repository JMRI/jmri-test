// Apps.java

package apps;

import jmri.InstanceManager;
import jmri.util.JmriJFrame;
import jmri.util.FileUtil;
import jmri.jmrit.XmlFile;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.*;
import java.io.File;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.help.CSH;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.swing.*;

import net.roydesign.mac.MRJAdapter;

/**
 * Base class for Jmri applications.
 * <P>
 * @author	Bob Jacobsen   Copyright 2003
 * @author  Dennis Miller  Copyright 2005
 * @version     $Revision: 1.43 $
 */
public class Apps extends JPanel {

    boolean onMac = (System.getProperty("mrj.version") != null);

    public Apps(JFrame frame) {

        super(true);

        setButtonSpace();

        // load preference file
        if (configFilename != null) {
            log.debug("configure from specified file "+configFilename);
        } else {
            configFilename = "jmriprefs.xml";
            log.debug("configure from default file "+configFilename);
        }
        XmlFile.ensurePrefsPresent(XmlFile.prefsDir());
        File file = new File(configFilename);
        // decide whether name is absolute or relative
        if (!file.isAbsolute()) {
            // must be relative, but we want it to 
            // be relative to the preferences directory
            file = new File(XmlFile.prefsDir()+configFilename);
        }
        InstanceManager.setConfigureManager(new jmri.configurexml.ConfigXmlManager());
        log.debug("start load config file");
        configOK = InstanceManager.configureManagerInstance().load(file);
        log.debug("end load config file, OK="+configOK);

	// populate GUI
	    log.debug("Start UI");
        setResourceBundle();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        // Create a menu bar
        menuBar = new JMenuBar();

        // Create menu categories and add to the menu bar, add actions to menus
        createMenus(menuBar, frame);

        // if the configuration didn't complete OK, pop the prefs frame
        log.debug("Config go OK? "+configOK);
        if (!configOK) doPreferences();
        log.debug("Done with doPreferences, start statusPanel");

        add(statusPanel());
        log.debug("Done with statusPanel, start buttonSpace");
        add(buttonSpace());

        log.debug("End constructor");
    }
    
    /**
     * Prepare the JPanel to contain buttons in the startup GUI.
     * Since it's possible to add buttons via the preferences,
     * this space may have additional buttons appended to it
     * later.  The default implementation here just creates an
     * empty space for these to be added to.
     */
    protected void setButtonSpace() {
        _buttonSpace = new JPanel();
        _buttonSpace.setLayout(new FlowLayout());
    }

    protected void setResourceBundle() {
        rb = ResourceBundle.getBundle("apps.AppsBundle");
    }
    
    /**
     * Create default menubar.
     * <P>
     * This does not include the development menu.
     *
     * @param menuBar
     * @param frame
     */
    protected void createMenus(JMenuBar menuBar, JFrame frame) {
        // the debugging statements in the following are
        // for testing startup time
        log.debug("start building menus");

        if (onMac) {
        // Let MRJAdapter do all of the dirty work in hooking up the Macintosh application menu
//          MRJAdapter.addAboutListener(new ActionListener() { public void actionPerformed(ActionEvent e) { about(); } });
            MRJAdapter.addPreferencesListener(new ActionListener() { public void actionPerformed(ActionEvent e) { doPreferences(); } });
//          MRJAdapter.addQuitApplicationListener(new ActionListener() { public void actionPerformed(ActionEvent e) { quit(); } });
        }
        
        fileMenu(menuBar, frame);
        editMenu(menuBar, frame);
        toolsMenu(menuBar, frame);
        rosterMenu(menuBar, frame);
        panelMenu(menuBar, frame);
        systemsMenu(menuBar, frame);
        scriptMenu(menuBar, frame);
        debugMenu(menuBar, frame);
        helpMenu(menuBar, frame);
        // windowMenu(menuBar, frame);
        log.debug("end building menus");
    }

    protected void fileMenu(JMenuBar menuBar, JFrame frame) {
        JMenu fileMenu = new JMenu(rb.getString("MenuFile"));
        menuBar.add(fileMenu);
        fileMenu.add(new jmri.jmrit.decoderdefn.PrintDecoderListAction("Print decoder definitions...", frame, false));
        fileMenu.add(new jmri.jmrit.decoderdefn.PrintDecoderListAction("Print Preview decoder definitions...", frame, true));

        // On a Mac, MRJAdapter already takes care of Quit
        if (!onMac) {
            fileMenu.add(new JSeparator());
            fileMenu.add(new AbstractAction(rb.getString("MenuItemQuit")){
                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                }
            });
        }
    }


    protected void doPreferences() {
        if (prefsFrame == null) {
            prefsFrame = new JmriJFrame(rb.getString("MenuItemPreferences"));
            prefsFrame.getContentPane().setLayout(new BoxLayout(prefsFrame.getContentPane(), BoxLayout.X_AXIS));
            prefs = new AppConfigPanel(configFilename, 2);
            prefsFrame.getContentPane().add(prefs);
            prefsFrame.pack();
        }
        prefsFrame.setVisible(true);
    }

    protected void editMenu(JMenuBar menuBar, JFrame frame) {
        AbstractAction prefsAction = new AbstractAction(rb.getString("MenuItemPreferences")) {
            public void actionPerformed(ActionEvent e) {
                doPreferences();
            }
        };

        JMenu editMenu = new JMenu(rb.getString("MenuEdit"));
        menuBar.add(editMenu);
        
        // cut, copy, paste
        AbstractAction a;
        a = new javax.swing.text.DefaultEditorKit.CutAction();
        a.putValue(javax.swing.Action.NAME, rb.getString("MenuItemCut"));
        editMenu.add(a);
        a = new javax.swing.text.DefaultEditorKit.CopyAction();
        a.putValue(javax.swing.Action.NAME, rb.getString("MenuItemCopy"));
        editMenu.add(a);
        a = new javax.swing.text.DefaultEditorKit.PasteAction();
        a.putValue(javax.swing.Action.NAME, rb.getString("MenuItemPaste"));
        editMenu.add(a);

        // prefs
        editMenu.add(prefsAction); // argument is filename, not action name
    }

    protected void toolsMenu(JMenuBar menuBar, JFrame frame) {
        menuBar.add(new jmri.jmrit.ToolsMenu(rb.getString("MenuTools")));
    }

    protected void rosterMenu(JMenuBar menuBar, JFrame frame) {
        menuBar.add(new jmri.jmrit.roster.RosterMenu(rb.getString("MenuRoster"), jmri.jmrit.roster.RosterMenu.MAINMENU, this));
    }
	
    protected void panelMenu(JMenuBar menuBar, JFrame frame) {
        menuBar.add(jmri.jmrit.display.PanelMenu.instance());
    }

    /**
     * Show only active systems in the menu bar.
     * <P>
     * Alternately, you might want to do
     * <PRE>
     *    menuBar.add(new jmri.jmrix.SystemsMenu());
     * </PRE>
     * @param menuBar
     * @param frame
     */
    protected void systemsMenu(JMenuBar menuBar, JFrame frame) {
        jmri.jmrix.ActiveSystemsMenu.addItems(menuBar);
    }

    protected void debugMenu(JMenuBar menuBar, JFrame frame) {
        JMenu d = new jmri.jmrit.DebugMenu(this);
        menuBar.add(d);
        
        // also add some tentative items from jmrix
        d.add(new JSeparator());
        d.add(new jmri.jmrix.pricom.PricomMenu());
        d.add(new jmri.jmrix.rps.RpsMenu());
    }

    protected void scriptMenu(JMenuBar menuBar, JFrame frame) {
        // temporarily remove Scripts menu; note that "Run Script"
        // has been added to the Panels menu
        // JMenu menu = new JMenu("Scripts");
        // menuBar.add(menu);
        // menu.add(new jmri.jmrit.automat.JythonAutomatonAction("Jython script", this));
        // menu.add(new jmri.jmrit.automat.JythonSigletAction("Jython siglet", this));
    }

    protected void developmentMenu(JMenuBar menuBar, JFrame frame) {
        JMenu devMenu = new JMenu("Development");
        menuBar.add(devMenu);
        devMenu.add(new jmri.jmrit.symbolicprog.autospeed.AutoSpeedAction("Auto-speed tool"));
        devMenu.add(new JSeparator());
        devMenu.add(new jmri.jmrit.automat.SampleAutomatonAction( "Sample automaton 1"));
        devMenu.add(new jmri.jmrit.automat.SampleAutomaton2Action("Sample automaton 2"));
        devMenu.add(new jmri.jmrit.automat.SampleAutomaton3Action("Sample automaton 3"));
        devMenu.add(new JSeparator());
        devMenu.add(new jmri.jmrix.serialsensor.SerialSensorAction("Serial port sensors"));
    }

    protected void windowMenu(JMenuBar menuBar, final JFrame frame) {
        JMenu devMenu = new JMenu("Window");
        menuBar.add(devMenu);
        devMenu.add(new AbstractAction("Minimize"){
            public void actionPerformed(ActionEvent e) {
                // the next line works on Java 2, but not 1.1.8
                // frame.setState(Frame.ICONIFIED);
            }
        });
    }

    static HelpSet globalHelpSet;
    static HelpBroker globalHelpBroker;

    protected void helpMenu(JMenuBar menuBar, final JFrame frame) {
        JMenu helpMenu = new JMenu(rb.getString("MenuHelp"));
        menuBar.add(helpMenu);
        try {
            String helpsetName = "help/en/JmriHelp_en.hs";
            URL hsURL;
            try {
                // HelpSet.findHelpSet doesn't seem to be working, so is temporarily bypassed
                // hsURL = HelpSet.findHelpSet(ClassLoader.getSystemClassLoader(), helpsetName);
                // following line doesn't work on Mac Classic
                // hsURL = new URL("file:"+helpsetName);
                hsURL = new URL(FileUtil.getUrl(new File(helpsetName)));
                globalHelpSet = new HelpSet(null, hsURL);
            } catch (java.lang.NoClassDefFoundError ee) {
                log.debug("classpath="+System.getProperty("java.class.path","<unknown>"));
                log.debug("classversion="+System.getProperty("java.class.version","<unknown>"));
                log.error("Help classes not found, help system omitted");
                return;
            } catch (java.lang.Exception e2) {
                log.error("HelpSet "+helpsetName+" not found, help system omitted");
                return;
            }
            globalHelpBroker = globalHelpSet.createHelpBroker();

            JMenuItem menuItem = new JMenuItem(rb.getString("MenuItemHelp"));
            helpMenu.add(menuItem);
            menuItem.addActionListener(new CSH.DisplayHelpFromSource(globalHelpBroker));

            // start help to see what happend
            log.debug("help: "+globalHelpSet.getHomeID()+":"+globalHelpSet.getTitle()
                               +":"+globalHelpSet.getHelpSetURL());

        } catch (java.lang.NoSuchMethodError e2) {
            log.error("Is jh.jar available? Error starting help system: "+e2);
        }

        JMenuItem license = new JMenuItem(rb.getString("MenuItemLicense"));
        helpMenu.add(license);
        license.addActionListener(new LicenseAction());

        JMenuItem directories = new JMenuItem(rb.getString("MenuItemLocations"));
        helpMenu.add(directories);
        directories.addActionListener(new jmri.jmrit.XmlFileLocationAction());

        JMenuItem context = new JMenuItem(rb.getString("MenuItemContext"));
        helpMenu.add(context);
        context.addActionListener(new apps.ReportContextAction());
    }

    protected String line1() {
        return MessageFormat.format(rb.getString("DefaultVersionCredit"),
                                new String[]{jmri.Version.name()});
    }
    protected String line2() {
        return "http://jmri.sf.net/ ";
    }
    protected String line3() {
        return " ";
    }
    protected String line4() {
        return MessageFormat.format(rb.getString("ConnectionCredit"),
                                new String[]{prefs.getConnection1(), prefs.getPort1()});
    }
    protected String line6() {
        return " ";
    }
    protected String line7() {
        return MessageFormat.format(rb.getString("JavaVersionCredit"),
                                new String[]{System.getProperty("java.version","<unknown>"),
                                            Locale.getDefault().toString()});
    }

    protected String logo() {
        return "resources/logo.gif";
    }

    /**
     * Fill in the logo and status panel
     * @return Properly-filled out JPanel
     */
    protected JPanel statusPanel() {
        JPanel pane1 = new JPanel();
        pane1.setLayout(new FlowLayout());
        log.debug("Fetch main logo: "+logo()+" "+ClassLoader.getSystemResource(logo()));
        pane1.add(new JLabel(new ImageIcon(ClassLoader.getSystemResource(logo()),"JMRI logo"), JLabel.LEFT));

        log.debug("start labels");
        JPanel pane2 = new JPanel();
        pane2.setLayout(new BoxLayout(pane2, BoxLayout.Y_AXIS));
        pane2.add(new JLabel(line1()));
        pane2.add(new JLabel(line2()));
        pane2.add(new JLabel(line3()));
        pane2.add(new JLabel(line4()));

        if (!prefs.getConnection2().equals("(none)")) {
            pane2.add(new JLabel(" "));
            pane2.add(new JLabel(MessageFormat.format(rb.getString("ConnectionCredit"),
                                new String[]{prefs.getConnection2(), prefs.getPort2()}
                            )));
        }

        pane2.add(new JLabel(line6()));
        pane2.add(new JLabel(line7()));
        pane1.add(pane2);
        return pane1;
    }

    /**
     * Provide access to a place where applications
     * can expect the configurion code to build run-time
     * buttons.
     * @see apps.CreateButtonPanel
     * @return null if no such space exists
     */
    static public JComponent buttonSpace() {
        return _buttonSpace;
    }
    static JComponent _buttonSpace = null;

    static protected JFrame prefsFrame = null;
    static protected ResourceBundle rb;

    static protected AppConfigPanel prefs;
    static public AppConfigPanel getPrefs() { return prefs; }
    
    static public String getConnection1() {
            return MessageFormat.format(rb.getString("ConnectionCredit"),
                                new String[]{prefs.getConnection1(), prefs.getPort1()});
    }
    static public String getConnection2() {
            return MessageFormat.format(rb.getString("ConnectionCredit"),
                                new String[]{prefs.getConnection2(), prefs.getPort2()});
    }
    
    static SplashWindow sp = null;
	static java.awt.event.AWTEventListener debugListener = null;
	static boolean debugFired = false;
    static protected void splash(boolean show) {
		if (debugListener == null) {
			// set a global listener for debug options
			debugFired = false;
			java.awt.Toolkit.getDefaultToolkit().addAWTEventListener(
				debugListener = new java.awt.event.AWTEventListener() {
						public void eventDispatched(java.awt.AWTEvent e) {
							if (!debugFired) {
								InstanceManager.logixManagerInstance().setLoadDisabled(true);
								log.debug("Requested load Logixs diabled.");
								debugFired = true;
							}
						}
					},
					java.awt.AWTEvent.KEY_EVENT_MASK
				);
		}
		// bring up splash window for startup
        if (sp==null) sp = new SplashWindow();
        sp.setVisible(show);
        if (!show) {
            sp.dispose();
			java.awt.Toolkit.getDefaultToolkit().removeAWTEventListener(debugListener);            
			sp = null;
        }
    }

    static protected void initLog4J() {
        // initialize log4j - from logging control file (lcf) only
        // if can find it!
        String logFile = "default.lcf";
        try {
            if (new java.io.File(logFile).canRead()) {
                org.apache.log4j.PropertyConfigurator.configure(logFile);
            } else {
                org.apache.log4j.BasicConfigurator.configure();
                org.apache.log4j.Category.getRoot().setPriority(org.apache.log4j.Priority.ERROR);
            }
        }
        catch (java.lang.NoSuchMethodError e) { log.error("Exception starting logging: "+e); }
    }

    /**
     * Set up the configuration file name at startup.
     * <P>
     * The static configFilename variable holds the name 
     * used to load the configuration file during later startup
     * processing.  Applications invoke this method 
     * to handle the usual startup hierarchy:
     *<UL>
     *<LI>If an absolute filename was provided on the command line, use it
     *<LI>If a filename was provided that's not absolute, consider it to
     *    be in the preferences directory
     *<LI>If no filename provided, use a default name (that's application
     *    specific)
     *</UL>
     *This name will be used for reading and writing the preferences. It
     * need not exist when the program first starts up.
     *
     * @param def Default value if no other is provided
     * @param args Argument array from the main routine
     */
    static protected void setConfigFilename(String def, String args[]) {
        // save the configuration filename if present on the command line
        if (args.length>=1 && args[0]!=null) {
            configFilename = args[0];
            log.debug("Config file was specified as: "+configFilename);
        } else{
            configFilename = def;
        }
    }

    static protected void createFrame(Apps containedPane, JFrame frame) {
    	// create the main frame and menus
        frame.addWindowListener(new jmri.util.oreilly.BasicWindowMonitor());

        // invoke plugin, if any
        jmri.JmriPlugin.start(frame, containedPane.menuBar);

        frame.setJMenuBar(containedPane.menuBar);
        frame.getContentPane().add(containedPane);

        // pack and center this frame
        frame.pack();
        Dimension screen = frame.getToolkit().getScreenSize();
        Dimension size = frame.getSize();
        frame.setLocation((screen.width-size.width)/2,(screen.height-size.height)/2);
        frame.setVisible(true);
    }

    static protected void loadFile(String name){
        File pFile = InstanceManager.configureManagerInstance().find(name);
        if (pFile!=null)
            InstanceManager.configureManagerInstance().load(pFile);
        else
            log.warn("Could not find "+name+" config file");

    }

    static protected String configFilename = null;
    static protected boolean configOK;

    // GUI members
    private JMenuBar menuBar;

    static public String startupInfo(String program) {
    	log.info(ignore);
        return (program+" version "+jmri.Version.name()
                +" starts under Java "+System.getProperty("java.version","<unknown>"));
    }
    
    static protected String ignore ="****** Ignore messages above this line *******";

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(Apps.class.getName());
}


