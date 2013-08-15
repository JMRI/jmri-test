package org.jmri.app;

import apps.AppConfigBase;
import apps.AppsBase;
import apps.SystemConsole;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.LogManager;
import javax.help.SwingHelpUtilities;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import jmri.IdTagManager;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.JmriPlugin;
import jmri.NamedBeanHandleManager;
import jmri.UserPreferencesManager;
import jmri.configurexml.ConfigXmlManager;
import jmri.configurexml.swing.DialogErrorHandler;
import jmri.implementation.AbstractShutDownTask;
import jmri.jmrit.DebugMenu;
import jmri.jmrit.ToolsMenu;
import jmri.jmrit.automat.SampleAutomaton2Action;
import jmri.jmrit.automat.SampleAutomaton3Action;
import jmri.jmrit.automat.SampleAutomatonAction;
import jmri.jmrit.decoderdefn.DecoderIndexFile;
import jmri.jmrit.decoderdefn.PrintDecoderListAction;
import jmri.jmrit.display.PanelMenu;
import jmri.jmrit.display.layoutEditor.BlockValueFile;
import jmri.jmrit.jython.Jynstrument;
import jmri.jmrit.jython.JynstrumentFactory;
import jmri.jmrit.jython.RunJythonScript;
import jmri.jmrit.operations.OperationsMenu;
import jmri.jmrit.revhistory.FileHistory;
import jmri.jmrit.roster.swing.RosterMenu;
import jmri.jmrit.signalling.EntryExitPairs;
import jmri.jmrit.symbolicprog.autospeed.AutoSpeedAction;
import jmri.jmrit.throttle.ThrottleFrame;
import jmri.jmrit.withrottle.WiThrottleCreationAction;
import jmri.jmrix.ConnectionConfig;
import jmri.jmrix.ConnectionStatus;
import jmri.jmrix.JmrixConfigPane;
import jmri.jmrix.jinput.treecontrol.TreeAction;
import jmri.jmrix.libusb.UsbViewAction;
import jmri.managers.DefaultIdTagManager;
import jmri.managers.DefaultUserMessagePreferences;
import jmri.plaf.macosx.PreferencesHandler;
import jmri.plaf.macosx.QuitHandler;
import jmri.util.FileUtil;
import jmri.util.HelpUtil;
import jmri.util.JmriJFrame;
import jmri.util.SystemType;
import jmri.util.WindowMenu;
import jmri.util.exceptionhandler.AwtHandler;
import jmri.util.exceptionhandler.UncaughtExceptionHandler;
import jmri.util.iharder.dnd.FileDrop;
import jmri.util.swing.FontComboUtil;
import jmri.util.swing.JFrameInterface;
import jmri.util.swing.SliderSnap;
import jmri.util.swing.WindowInterface;
import jmri.web.server.WebServerAction;
import org.jmri.managers.NetBeansShutDownManager;
import org.netbeans.api.options.OptionsDisplayer;
import org.slf4j.LoggerFactory;

/**
 *
 * @author rhwood
 */
public class AppClassic extends JPanel implements PropertyChangeListener, java.awt.event.WindowListener {

    private static final String jmriLog = "****** JMRI log *******";
    static boolean loggingInitialized = false;
    static org.slf4j.Logger log = LoggerFactory.getLogger(AppClassic.class);

    public AppClassic(JFrame frame) {
        super(true);

        setButtonSpace();
        setJynstrumentSpace();

        // Using jmri.Application since Application is ambigious
        jmri.Application.setLogo(logo());
        jmri.Application.setURL(line2());

        // Enable proper snapping of JSliders
        SliderSnap.init();

        // Prepare font lists
        prepareFontLists();

        // install shutdown manager
        InstanceManager.setShutDownManager(new NetBeansShutDownManager());

        // add the default shutdown task to save blocks
        // as a special case, register a ShutDownTask to write out blocks
        InstanceManager.shutDownManagerInstance().
                register(new AbstractShutDownTask("Writing Blocks") {
            @Override
            public boolean execute() {
                // Save block values prior to exit, if necessary
                log.debug("Start writing block info");
                try {
                    new BlockValueFile().writeBlockValues();
                } //catch (org.jdom.JDOMException jde) { log.error("Exception writing blocks: "+jde); }                           
                catch (IOException ioe) {
                    log.error("Exception writing blocks: " + ioe);
                }

                // continue shutdown   
                return true;
            }
        });

        // Install configuration manager and Swing error handler
        ConfigXmlManager cm = new ConfigXmlManager();
        InstanceManager.setConfigureManager(cm);
        ConfigXmlManager.setErrorHandler(new DialogErrorHandler());
        InstanceManager.setConfigureManager(cm);

        // Install a history manager
        InstanceManager.store(new FileHistory(), FileHistory.class);
        // record startup
        InstanceManager.getDefault(FileHistory.class).addOperation("app", nameString, null);

        // Install a user preferences manager
        InstanceManager.store(DefaultUserMessagePreferences.getInstance(), UserPreferencesManager.class);
        InstanceManager.store(new NamedBeanHandleManager(), NamedBeanHandleManager.class);
        // Install an IdTag manager
        InstanceManager.store(new DefaultIdTagManager(), IdTagManager.class);
        //Install Entry Exit Pairs Manager
        InstanceManager.store(new EntryExitPairs(), EntryExitPairs.class);

        // install preference manager
        InstanceManager.setTabbedPreferences(new apps.gui3.TabbedPreferences());

        // Install abstractActionModel
        InstanceManager.store(new apps.CreateButtonModel(), apps.CreateButtonModel.class);

        // find preference file and set location in configuration manager
        FileUtil.createDirectory(FileUtil.getUserFilesPath());
        // Needs to be declared final as we might need to
        // refer to this on the Swing thread
        final File file;
        // decide whether name is absolute or relative
        if (!new File(configFilename).isAbsolute()) {
            // must be relative, but we want it to 
            // be relative to the preferences directory
            file = new File(FileUtil.getUserFilesPath() + configFilename);
        } else {
            file = new File(configFilename);
        }
        cm.setPrefsLocation(file);
        // load config file if it exists
        if (file.exists()) {
            if (log.isDebugEnabled()) {
                log.debug("start load config file " + file.getPath());
            }
            try {
                configOK = InstanceManager.configureManagerInstance().load(file, true);
            } catch (JmriException e) {
                log.error("Unhandled problem loading configuration: " + e);
                configOK = false;
            }
            log.debug("end load config file, OK=" + configOK);
        } else {
            log.info("No saved preferences, will open preferences window.  Searched for " + file.getPath());
            configOK = false;
        }

        //2012/01/21 dboudreau rb needs to be reloaded after reading the configuration file so the locale is set properly.
        rb = ResourceBundle.getBundle("apps.AppsBundle");

        // Add actions to abstractActionModel
        // Done here as initial non-GUI initialisation is completed
        // and UI L&F has been set
        addToActionModel();

        // populate GUI
        log.debug("Start UI");
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        // Create a WindowInterface object based on the passed-in Frame
        JFrameInterface wi = new JFrameInterface(frame);
        // Create a menu bar
        menuBar = new JMenuBar();

        // Create menu categories and add to the menu bar, add actions to menus
        createMenus(menuBar, wi);

        FileUtil.logFilePaths();

        // Now load deferred config items
        if (file.exists()) {
            // To avoid possible locks, deferred load should be
            // performed on the Swing thread
            if (SwingUtilities.isEventDispatchThread()) {
                configDeferredLoadOK = doDeferredLoad(file);
            } else {
                try {
                    // Use invokeAndWait method as we don't want to
                    // return until deferred load is completed
                    SwingUtilities.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            configDeferredLoadOK = doDeferredLoad(file);
                        }
                    });
                } catch (Exception ex) {
                    log.error("Exception creating system console frame: " + ex);
                }
            }
        } else {
            configDeferredLoadOK = false;
        }

        // TODO: All these threds should eventually be pushed into Initialzers.
        /*Once all the preferences have been loaded we can initial the preferences
         doing it in a thread at this stage means we can let it work in the background*/
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    InstanceManager.tabbedPreferencesInstance().init();
                } catch (Exception ex) {
                    log.error("Error in trying to setup preferences {}", ex.toString(), ex);
                }
            }
        };
        Thread thr = new Thread(r, "initialize preferences");
        thr.start();
        //Initialise the decoderindex file instance within a seperate thread to help improve first use perfomance
        r = new Runnable() {
            @Override
            public void run() {
                try {
                    DecoderIndexFile.instance();
                } catch (Exception ex) {
                    log.error("Error in trying to initialize decoder index file " + ex.toString());
                }
            }
        };
        Thread thr2 = new Thread(r, "initialize decoder index");
        thr2.start();

        r = new Runnable() {
            @Override
            public void run() {
                try {
                    jmri.util.PythonInterp.getPythonInterpreter();
                } catch (Exception ex) {
                    log.error("Error in trying to initialize python interpreter " + ex.toString());
                }
            }
        };
        Thread thr3 = new Thread(r, "initialize python interpreter");
        thr3.start();
        // if the configuration didn't complete OK, pop the prefs frame and help
        log.debug("Config go OK? " + (configOK || configDeferredLoadOK));
        if (!configOK || !configDeferredLoadOK) {
            jmri.util.HelpUtil.displayHelpRef("package.apps.AppConfigPanelErrorPage");
            doPreferences();
        }
        log.debug("Done with doPreferences, start statusPanel");

        add(statusPanel());
        log.debug("Done with statusPanel, start buttonSpace");
        add(buttonSpace());
        add(_jynstrumentSpace);

        log.debug("End constructor");
    }

    private boolean doDeferredLoad(File file) {
        boolean result;
        log.debug("start deferred load from config");
        try {
            result = InstanceManager.configureManagerInstance().loadDeferred(file);
        } catch (JmriException e) {
            log.error("Unhandled problem loading deferred configuration: " + e);
            result = false;
        }
        log.debug("end deferred load from config file, OK=" + result);
        return result;
    }

    protected final void addToActionModel() {
        apps.CreateButtonModel bm = InstanceManager.getDefault(apps.CreateButtonModel.class);
        ResourceBundle actionList = ResourceBundle.getBundle("apps.ActionListBundle");
        Enumeration<String> e = actionList.getKeys();
        while (e.hasMoreElements()) {
            String key = e.nextElement();
            try {
                bm.addAction(key, actionList.getString(key));
            } catch (ClassNotFoundException ex) {
                log.error("Did not find class " + key);
            }
        }
    }

    /**
     * Prepare the JPanel to contain buttons in the startup GUI. Since it's
     * possible to add buttons via the preferences, this space may have
     * additional buttons appended to it later. The default implementation here
     * just creates an empty space for these to be added to.
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
            justification = "only one application at a time")
    protected void setButtonSpace() {
        _buttonSpace = new JPanel();
        _buttonSpace.setLayout(new FlowLayout());
    }
    static JComponent _jynstrumentSpace = null;

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
            justification = "only one application at a time")
    protected void setJynstrumentSpace() {
        _jynstrumentSpace = new JPanel();
        _jynstrumentSpace.setLayout(new FlowLayout());
        new FileDrop(_jynstrumentSpace, new FileDrop.Listener() {
            @Override
            public void filesDropped(File[] files) {
                for (int i = 0; i < files.length; i++) {
                    ynstrument(files[i].getPath());
                }
            }
        });
    }

    public static void ynstrument(String path) {
        Jynstrument it = JynstrumentFactory.createInstrument(path, _jynstrumentSpace);
        if (it == null) {
            log.error("Error while creating Jynstrument " + path);
            return;
        }
        ThrottleFrame.setTransparent(it);
        it.setVisible(true);
        _jynstrumentSpace.setVisible(true);
        _jynstrumentSpace.add(it);
    }

    /**
     * Create default menubar. <P> This does not include the development menu.
     *
     * @param menuBar
     * @param wi
     */
    protected void createMenus(JMenuBar menuBar, WindowInterface wi) {
        // the debugging statements in the following are
        // for testing startup time
        log.debug("start building menus");

        if (SystemType.isMacOSX()) {
            jmri.plaf.macosx.Application.getApplication().setQuitHandler(new QuitHandler() {
                @Override
                public boolean handleQuitRequest(EventObject eo) {
                    return handleQuit();
                }
            });
            if (UIManager.getLookAndFeel().isNativeLookAndFeel()) {
                System.setProperty("apple.laf.useScreenMenuBar", "true");
            }
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", jmri.Application.getApplicationName());
        }

        fileMenu(menuBar, wi);
        editMenu(menuBar, wi);
        toolsMenu(menuBar, wi);
        rosterMenu(menuBar, wi);
        panelMenu(menuBar, wi);
        // check to see if operations in main menu
        if (jmri.jmrit.operations.setup.Setup.isMainMenuEnabled()) {
            operationsMenu(menuBar, wi);
        }
        systemsMenu(menuBar, wi);
        scriptMenu(menuBar, wi);
        debugMenu(menuBar, wi);
        menuBar.add(new WindowMenu(wi)); // * GT 28-AUG-2008 Added window menu
        helpMenu(menuBar, wi);
        log.debug("end building menus");
    }

    protected void fileMenu(JMenuBar menuBar, WindowInterface wi) {
        JMenu fileMenu = new JMenu(rb.getString("MenuFile"));
        menuBar.add(fileMenu);

        fileMenu.add(new PrintDecoderListAction(rb.getString("MenuPrintDecoderDefinitions"), wi.getFrame(), false));
        fileMenu.add(new PrintDecoderListAction(rb.getString("MenuPrintPreviewDecoderDefinitions"), wi.getFrame(), true));

        // Use Mac OS X native Quit if using Aqua look and feel
        if (!(SystemType.isMacOSX() && UIManager.getLookAndFeel().isNativeLookAndFeel())) {
            fileMenu.add(new JSeparator());
            fileMenu.add(new AbstractAction(rb.getString("MenuItemQuit")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    handleQuit();
                }
            });
        }
    }
    Action prefsAction;

    public void doPreferences() {
        prefsAction.actionPerformed(null);
    }

    /**
     * Set the location of the window-specific help for the preferences pane.
     * Made a separate method so if can be overridden for application specific
     * preferences help
     */
    protected void setPrefsFrameHelp(JmriJFrame f, String l) {
        f.addHelpMenu(l, true);
    }

    protected void editMenu(JMenuBar menuBar, WindowInterface wi) {

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
        prefsAction = new apps.gui3.TabbedPreferencesAction("Preferences");
        // Put prefs in Apple's prefered area on Mac OS X
        if (SystemType.isMacOSX() && UIManager.getLookAndFeel().isNativeLookAndFeel()) {
            jmri.plaf.macosx.Application.getApplication().setPreferencesHandler(new PreferencesHandler() {
                @Override
                public void handlePreferences(EventObject eo) {
                    doPreferences();
                }
            });
        } else {
            // Include prefs in Edit menu if not on Mac OS X or not using Aqua Look and Feel
            editMenu.addSeparator();
            editMenu.add(prefsAction);
        }

    }

    protected void toolsMenu(JMenuBar menuBar, WindowInterface wi) {
        // menuBar.add(new ToolsMenu(rb.getString("MenuTools")));
        // TEMPORARY UNTIL TABBED PREFERENCES IS REPLACED WITH NETBEANS PREFERENCES
        ToolsMenu toolsMenu = new ToolsMenu(rb.getString("MenuTools"));
        toolsMenu.addSeparator();
        toolsMenu.add(new AbstractAction("NetBeans Options") {
            @Override
            public void actionPerformed(ActionEvent e) {
                OptionsDisplayer.getDefault().open();
            }
        });
        menuBar.add(toolsMenu);
    }

    protected void operationsMenu(JMenuBar menuBar, WindowInterface wi) {
        menuBar.add(new OperationsMenu(rb.getString("MenuOperations")));
    }

    protected void rosterMenu(JMenuBar menuBar, WindowInterface wi) {
        menuBar.add(new RosterMenu(rb.getString("MenuRoster"), RosterMenu.MAINMENU, this));
    }

    protected void panelMenu(JMenuBar menuBar, WindowInterface wi) {
        menuBar.add(PanelMenu.instance());
    }

    /**
     * Show only active systems in the menu bar. <P> Alternately, you might want
     * to do
     * <PRE>
     *    menuBar.add(new jmri.jmrix.SystemsMenu());
     * </PRE>
     *
     * @param menuBar
     * @param wi
     */
    protected void systemsMenu(JMenuBar menuBar, WindowInterface wi) {
        jmri.jmrix.ActiveSystemsMenu.addItems(menuBar);
    }

    protected void debugMenu(JMenuBar menuBar, WindowInterface wi) {
        JMenu d = new DebugMenu(this);

        // also add some tentative items from jmrix
        d.add(new JSeparator());
        d.add(new jmri.jmrix.pricom.PricomMenu());
        d.add(new JSeparator());

        d.add(new TreeAction());
        d.add(new UsbViewAction());

        d.add(new JSeparator());
        d.add(new RunJythonScript("RailDriver Throttle", new File("jython/RailDriver.py")));

        // also add some tentative items from webserver
        d.add(new JSeparator());
        d.add(new WebServerAction());

        d.add(new JSeparator());
        d.add(new WiThrottleCreationAction());
        menuBar.add(d);

    }

    protected void scriptMenu(JMenuBar menuBar, WindowInterface wi) {
        // temporarily remove Scripts menu; note that "Run Script"
        // has been added to the Panels menu
        // JMenu menu = new JMenu("Scripts");
        // menuBar.add(menu);
        // menu.add(new jmri.jmrit.automat.JythonAutomatonAction("Jython script", this));
        // menu.add(new jmri.jmrit.automat.JythonSigletAction("Jython siglet", this));
    }

    protected void developmentMenu(JMenuBar menuBar, WindowInterface wi) {
        JMenu devMenu = new JMenu("Development");
        menuBar.add(devMenu);
        devMenu.add(new AutoSpeedAction("Auto-speed tool"));
        devMenu.add(new JSeparator());
        devMenu.add(new SampleAutomatonAction("Sample automaton 1"));
        devMenu.add(new SampleAutomaton2Action("Sample automaton 2"));
        devMenu.add(new SampleAutomaton3Action("Sample automaton 3"));
        //devMenu.add(new JSeparator());
        //devMenu.add(new jmri.jmrix.serialsensor.SerialSensorAction("Serial port sensors"));
    }

    protected void helpMenu(JMenuBar menuBar, WindowInterface wi) {
        try {

            // create menu and standard items
            JMenu helpMenu = HelpUtil.makeHelpMenu(mainWindowHelpID(), true);

            // tell help to use default browser for external types
            SwingHelpUtilities.setContentViewerUI("jmri.util.ExternalLinkContentViewerUI");

            // use as main help menu 
            menuBar.add(helpMenu);

        } catch (java.lang.Throwable e3) {
            log.error("Unexpected error creating help: " + e3);
        }

    }

    /**
     * Returns the ID for the main window's help, which is application specific
     */
    protected String mainWindowHelpID() {
        return "package.apps.Apps";
    }

    protected String line1() {
        return Bundle.getMessage("DefaultVersionCredit", jmri.Version.name());
    }

    protected String line2() {
        return "http://jmri.org/";
    }

    protected String line3() {
        return " ";
    }
    // line 4
    JLabel cs4 = new JLabel();

    protected void buildLine4(JPanel pane) {
        if (connection[0] != null) {
            buildLine(connection[0], cs4, pane);
        }
    }
    // line 5 optional
    JLabel cs5 = new JLabel();

    protected void buildLine5(JPanel pane) {
        if (connection[1] != null) {
            buildLine(connection[1], cs5, pane);
        }
    }
    // line 6 optional
    JLabel cs6 = new JLabel();

    protected void buildLine6(JPanel pane) {
        if (connection[2] != null) {
            buildLine(connection[2], cs6, pane);
        }
    }
    // line 7 optional
    JLabel cs7 = new JLabel();

    protected void buildLine7(JPanel pane) {
        if (connection[3] != null) {
            buildLine(connection[3], cs7, pane);
        }
    }

    protected void buildLine(ConnectionConfig conn, JLabel cs, JPanel pane) {
        if (conn.name().equals(JmrixConfigPane.NONE)) {
            cs.setText(" ");
            return;
        }
        ConnectionStatus.instance().addConnection(conn.name(), conn.getInfo());
        cs.setFont(pane.getFont());
        updateLine(conn, cs);
        pane.add(cs);
    }

    protected void updateLine(ConnectionConfig conn, JLabel cs) {
        if (conn.getDisabled()) {
            return;
        }
        String name = conn.getConnectionName();
        if (name == null) {
            name = conn.getManufacturer();
        }
        if (ConnectionStatus.instance().isConnectionOk(conn.getInfo())) {
            cs.setForeground(Color.black);
            String cf = Bundle.getMessage("ConnectionSucceeded", name, conn.name(), conn.getInfo());
            cs.setText(cf);
        } else {
            cs.setForeground(Color.red);
            String cf = Bundle.getMessage("ConnectionFailed", name, conn.name(), conn.getInfo());
            cf = cf.toUpperCase();
            cs.setText(cf);
        }


        this.revalidate();
    }

    protected String line8() {
        return " ";
    }

    protected String line9() {
        return Bundle.getMessage("JavaVersionCredit",
                System.getProperty("java.version", "<unknown>"),
                Locale.getDefault().toString());
    }

    protected String logo() {
        return "resources/logo.gif";
    }

    /**
     * Fill in the logo and status panel
     *
     * @return Properly-filled out JPanel
     */
    protected JPanel statusPanel() {
        JPanel pane1 = new JPanel();
        pane1.setLayout(new BoxLayout(pane1, BoxLayout.X_AXIS));
        if (log.isDebugEnabled()) {
            log.debug("Fetch main logo: " + logo() + " " + getToolkit().getImage(logo()));
        }
        pane1.add(new JLabel(new ImageIcon(getToolkit().getImage(FileUtil.findURL(logo())), "JMRI logo"), JLabel.LEFT));
        pane1.add(Box.createRigidArea(new Dimension(15, 0))); // Some spacing between logo and status panel

        log.debug("start labels");
        JPanel pane2 = new JPanel();

        pane2.setLayout(new BoxLayout(pane2, BoxLayout.Y_AXIS));
        pane2.add(new JLabel(line1()));
        pane2.add(new JLabel(line2()));
        pane2.add(new JLabel(line3()));

        // add listerner for Com port updates
        ConnectionStatus.instance().addPropertyChangeListener(this);
        ArrayList<Object> connList = InstanceManager.configureManagerInstance().getInstanceList(ConnectionConfig.class);
        int i = 0;
        if (connList != null) {
            for (int x = 0; x < connList.size(); x++) {
                ConnectionConfig conn = (ConnectionConfig) connList.get(x);
                if (!conn.getDisabled()) {
                    connection[i] = conn;
                    i++;
                }
                if (i > 3) {
                    break;
                }
            }
        }
        buildLine4(pane2);
        buildLine5(pane2);
        buildLine6(pane2);
        buildLine7(pane2);

        pane2.add(new JLabel(line8()));
        pane2.add(new JLabel(line9()));
        pane1.add(pane2);
        return pane1;
    }
    //int[] connection = {-1,-1,-1,-1};
    ConnectionConfig[] connection = {null, null, null, null};

    /**
     * Closing the main window is a shutdown request
     */
    @Override
    public void windowClosing(WindowEvent e) {
        if (JOptionPane.showConfirmDialog(null,
                rb.getString("MessageLongCloseWarning"),
                rb.getString("MessageShortCloseWarning"),
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            handleQuit();
        }
        // if get here, didn't quit, so don't close window
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowOpened(WindowEvent e) {
    }

    static protected void setJmriSystemProperty(String key, String value) {
        try {
            String current = System.getProperty("org.jmri.Apps." + key);
            if (current == null) {
                System.setProperty("org.jmri.Apps." + key, value);
            } else if (!current.equals(value)) {
                log.warn("JMRI property " + key + " already set to " + current
                        + ", skipping reset to " + value);
            }
        } catch (Exception e) {
            log.error("Unable to set JMRI property " + key + " to " + value
                    + "due to exception: " + e);
        }
    }

    /**
     * Provide access to a place where applications can expect the configuration
     * code to build run-time buttons.
     *
     * @see apps.CreateButtonPanel
     * @return null if no such space exists
     */
    static public JComponent buttonSpace() {
        return _buttonSpace;
    }
    static JComponent _buttonSpace = null;
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "MS_PKGPROTECT",
            justification = "Needs protected access so it can be reloaded after reading the configuration file so the locale is set properly")
    protected static ResourceBundle rb = ResourceBundle.getBundle("apps.AppsBundle");
    static AppConfigBase prefs;

    static public AppConfigBase getPrefs() {
        return prefs;
    }

    /**
     * The application decided to quit, handle that.
     */
    static public Boolean handleQuit() {
        return AppsBase.handleQuit();
    }

    /**
     * The application decided to restart, handle that.
     */
    static public Boolean handleRestart() {
        return AppsBase.handleRestart();
    }

    // TODO: rewrite to use JUL LogManager
    // Store default logging instructions in JMRI Logging Resources
    // Read user logging instructions from User directory
    static public void initLogging() {
        if (loggingInitialized) {
            log.debug("initLog4J already initialized!");
            return;
        }
        // Initialise JMRI System Console
        // TODO: figure out how to get logging done before this
        // into the console, since NetBeans starts logging before any JMRI code
        // is called.
        SystemConsole.create();

        loggingInitialized = true;
        // initialize JUL from logging.properties if found.
        String logFile = "logging.properties";
        try {
            File f = new File(jmri.util.FileUtil.getPreferencesPath() + logFile);
            if (!f.canRead()) {
                f = new File(jmri.util.FileUtil.getProgramPath() + logFile);
            }
            if (!f.canRead()) {
                f = org.openide.filesystems.FileUtil.toFile(org.openide.filesystems.FileUtil.getConfigFile(logFile));
            }
            if (f.canRead()) {
                LogManager.getLogManager().readConfiguration(new FileInputStream(f));
            } else {
                java.util.logging.Logger.getGlobal().setLevel(Level.WARNING);
            }
        } catch (IOException e) {
            log.error("Exception configuring logging.", e);
        } catch (NoSuchMethodError e) {
            log.error("Exception starting logging.", e);
        }
        // install default exception handlers
        System.setProperty("sun.awt.exception.handler", AwtHandler.class.getName());
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());

        log = LoggerFactory.getLogger(AppClassic.class);
        // first log entry
        log.info(jmriLog);

        // TODO: dump logging config (especially file names) to console
    }

    /**
     * Set up the configuration file name at startup. <P> The Configuration File
     * name variable holds the name used to load the configuration file during
     * later startup processing. Applications invoke this method to handle the
     * usual startup hierarchy: <UL> <LI>If an absolute filename was provided on
     * the command line, use it <LI>If a filename was provided that's not
     * absolute, consider it to be in the preferences directory <LI>If no
     * filename provided, use a default name (that's application specific) </UL>
     * This name will be used for reading and writing the preferences. It need
     * not exist when the program first starts up. This name may be proceeded
     * with <em>config=</em> and may not contain the equals sign (=).
     *
     * @param def Default value if no other is provided
     * @param args Argument array from the main routine
     */
    static public void setConfigFilename(String def, String[] args) {
        // save the configuration filename if present on the command line
        if (args.length >= 1 && args[0] != null && !args[0].contains("=")) {
            def = args[0];
            log.debug("Config file was specified as: " + args[0]);
        }
        for (String arg : args) {
            String[] split = arg.split("=", 2);
            if (split[0].equalsIgnoreCase("config")) {
                def = split[1];
                log.debug("Config file was specified as: " + arg);
            }
        }
        AppClassic.configFilename = def;
        setJmriSystemProperty("configFilename", def);
    }

    static public String getConfigFileName() {
        return configFilename;
    }

    static public void createFrame(AppClassic containedPane, JFrame frame) {
        // create the main frame and menus

        // invoke plugin, if any
        JmriPlugin.start(frame, containedPane.menuBar);

        frame.setJMenuBar(containedPane.menuBar);
        frame.getContentPane().add(containedPane);

        // handle window close
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(containedPane);

        // pack and center this frame
        frame.pack();
        Dimension screen = frame.getToolkit().getScreenSize();
        Dimension size = frame.getSize();
        frame.setLocation((screen.width - size.width) / 2, (screen.height - size.height) / 2);
        frame.setVisible(true);
    }

    static public void loadFile(String name) {
        URL pFile = InstanceManager.configureManagerInstance().find(name);
        if (pFile != null) {
            try {
                InstanceManager.configureManagerInstance().load(pFile);
            } catch (JmriException e) {
                log.error("Unhandled problem in loadFile: " + e);
            }
        } else {
            log.warn("Could not find " + name + " config file");
        }

    }
    static String configFilename = "jmriconfig2.xml";  // usually overridden, this is default
    // The following MUST be protected for 3rd party applications 
    // (such as CATS) which are derived from this class.
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "MS_PKGPROTECT",
            justification = "The following MUST be protected for 3rd party applications (such as CATS) which are derived from this class.")
    protected static boolean configOK;
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "MS_PKGPROTECT",
            justification = "The following MUST be protected for 3rd party applications (such as CATS) which are derived from this class.")
    protected static boolean configDeferredLoadOK;
    // GUI members
    private JMenuBar menuBar;

    static public String startupInfo(String program) {
        setApplication(program);
        nameString = (program + " version " + jmri.Version.name()
                + " starts under Java " + System.getProperty("java.version", "<unknown>")
                + " at " + (new Date()));
        return nameString;
    }
    static String nameString = "JMRI program";

    protected static void setApplication(String name) {
        try {
            jmri.Application.setApplicationName(name);
        } catch (IllegalArgumentException ex) {
            log.warn("Unable to set application name", ex);
        } catch (IllegalAccessException ex) {
            log.warn("Unable to set application name", ex);
        }
    }

    private void prepareFontLists() {
        // Prepare font lists
        new Thread(new Runnable() {
            @Override
            public void run() {
                log.debug("Prepare font lists...");
                FontComboUtil.prepareFontLists();
                log.debug("...Font lists built");
            }
        }).start();
    }

    @Override
    public void propertyChange(PropertyChangeEvent ev) {
        if (log.isDebugEnabled()) {
            log.debug("property change: comm port status update");
        }
        if (connection[0] != null) {
            updateLine(connection[0], cs4);
        }

        if (connection[1] != null) {
            updateLine(connection[1], cs5);
        }

        if (connection[2] != null) {
            updateLine(connection[2], cs6);
        }

        if (connection[3] != null) {
            updateLine(connection[3], cs7);
        }

    }
}
