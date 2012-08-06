// Apps3.java

package apps.gui3;

import apps.AppsBase;
import apps.CreateButtonModel;
import apps.SplashWindow;
import apps.SystemConsole;
import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.ResourceBundle;
import javax.help.SwingHelpUtilities;
import javax.swing.*;
import jmri.InstanceManager;
import jmri.ShutDownTask;
import jmri.plaf.macosx.AboutHandler;
import jmri.plaf.macosx.PreferencesHandler;
import jmri.plaf.macosx.QuitHandler;
import jmri.swing.AboutDialog;
import jmri.util.HelpUtil;
import jmri.util.JmriJFrame;
import jmri.util.SystemType;
import jmri.util.swing.FontComboUtil;
import org.apache.log4j.Logger;


/**
 * Base class for GUI3 JMRI applications.
 * <p>
 * This is a complete re-implementation of the 
 * apps.Apps support for JMRI applications.
 * <p>
 * Each using application provides it's own main() method.
 * See e.g. apps.gui3.demo3.Demo3 for an example.
 * <p>
 * There are a large number of missing features marked with TODO in comments
 * including code from the earlier implementation.
 * <P>
 * @author	Bob Jacobsen   Copyright 2009, 2010
 * @version $Revision$
 */
public abstract class Apps3 extends AppsBase {


    /**
     * Initial actions before 
     * frame is created, invoked in the 
     * applications main() routine.
     */
    static public void preInit(String applicationName) {
        AppsBase.preInit(applicationName);
        
        // Initialise system console
        // Put this here rather than in apps.AppsBase as this is only relevant
        // for GUI applications - non-gui apps will use STDOUT & STDERR
        SystemConsole.create();
        
        splash(true);
        
        setButtonSpace();
        
    }
    
    /**
     * Create and initialize the application object.
     *<p>
     * Expects initialization from preInit() to already be done.
     */
    public Apps3(String applicationName, String configFileDef, String[] args) {
        // pre-GUI work
        super(applicationName, configFileDef, args);

        // Prepare font lists
        prepareFontLists();

        addToActionModel();
        // create GUI
        initializeHelpSystem();
        if (SystemType.isMacOSX()) {
            initMacOSXMenus();
        }
        if(((!configOK) || (!configDeferredLoadOK)) && (!preferenceFileExists)){
            FirstTimeStartUpWizardAction prefsAction = new FirstTimeStartUpWizardAction("Start Up Wizard");
            prefsAction.setApp(this);
            prefsAction.actionPerformed(null);
            return;
        }
        createAndDisplayFrame();
        InstanceManager.shutDownManagerInstance().register(new ShutDownTask() {

            @Override
            public boolean execute() {
                for (JmriJFrame frame : JmriJFrame.getFrameList()) {
                    frame.windowClosing(null);
                }
                return true;
            }

            @Override
            public String name() {
                return "closeJmriJFrameWindows";
            }
        });
    }
    
        /**
    * For compatability with adding in buttons to the toolbar using the existing createbuttonmodel
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
                                                    justification="only one application at a time")
    protected static void setButtonSpace() {
        _buttonSpace = new JPanel();
        _buttonSpace.setLayout(new FlowLayout(FlowLayout.LEFT));
    }
    
    /**
     * Provide access to a place where applications
     * can expect the configuration code to build run-time
     * buttons.
     * @see apps.CreateButtonPanel
     * @return null if no such space exists
     */
    static public JComponent buttonSpace() {
        return _buttonSpace;
    }
    static JComponent _buttonSpace = null;
        
    protected JmriJFrame mainFrame;
    
    protected void initializeHelpSystem() {
        try {

            // initialize help system
            HelpUtil.initOK();
            
            // tell help to use default browser for external types
            SwingHelpUtilities.setContentViewerUI("jmri.util.ExternalLinkContentViewerUI");
    
            // help items are set in the various Tree/Menu/Toolbar constructors        
        } catch (Throwable e3) {
            log.error("Unexpected error creating help: "+e3);
        }
    }

    abstract protected void createMainFrame();
    
    public void createAndDisplayFrame(){
        createMainFrame();
        
        //A Shutdown manager handles the quiting of the application
        mainFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        displayMainFrame(mainFrame.getMaximumSize());
    }
    
    abstract protected ResourceBundle getActionModelResourceBundle();
    
    protected void addToActionModel(){
        CreateButtonModel bm = InstanceManager.getDefault(apps.CreateButtonModel.class);
        ResourceBundle rb = getActionModelResourceBundle();
        if (rb==null || bm==null)
            return;
        Enumeration<String> e = rb.getKeys();
        while (e.hasMoreElements()) {
            String key = e.nextElement();
            try {
                bm.addAction(key, rb.getString(key));
            } catch (ClassNotFoundException ex) {
                log.error("Did not find class "+key);
            }
        }
    }
        
    /**
     * Set a toolbar to be initially floating.
     * This doesn't quite work right.
     */
    protected void setFloating(JToolBar toolBar) {
        //((javax.swing.plaf.basic.BasicToolBarUI) toolBar.getUI()).setFloatingLocation(100,100);
        ((javax.swing.plaf.basic.BasicToolBarUI) toolBar.getUI()).setFloating(true, new Point(500,500));
    }
    
    protected void displayMainFrame(Dimension d) {
        mainFrame.setSize(d);
        mainFrame.setVisible(true);
    }
    
    /**
     * Final actions before releasing control of app to user
     */
    protected void start() {
        // TODO: splash(false);
        super.start();
        splash(false);
    }
    
    static protected void splash(boolean show){
        splash(show, false);
    }
    
    static SplashWindow sp = null;
    static AWTEventListener debugListener = null;
    static boolean debugFired = false;
    static boolean debugmsg = false;
    
    static protected void splash(boolean show, boolean debug) {
        if (debugListener == null && debug) {
            // set a global listener for debug options
            debugFired = false;
            debugListener = new AWTEventListener() {

                @Override
                public void eventDispatched(AWTEvent e) {
                    if (!debugFired) {
                        /*We set the debugmsg flag on the first instance of the user pressing any button
                        and the if the debugFired hasn't been set, this allows us to ensure that we don't
                        miss the user pressing F8, while we are checking*/
                        debugmsg = true;
                        if (e.getID() == KeyEvent.KEY_PRESSED) {
                            if (((KeyEvent) e).getKeyCode() == 119) {
                                startupDebug();
                            }
                        } else {
                            debugmsg = false;
                        }
                    }
                }
            };
            Toolkit.getDefaultToolkit().addAWTEventListener(debugListener,
                    AWTEvent.KEY_EVENT_MASK);
        }

        // bring up splash window for startup

        if (sp == null) {
            sp = new SplashWindow((debug) ? splashDebugMsg() : null);
        }
        sp.setVisible(show);
        if (!show) {
            sp.dispose();
            Toolkit.getDefaultToolkit().removeAWTEventListener(debugListener);
            debugListener = null;
            sp = null;
        }
    }
    
    static protected JPanel splashDebugMsg(){
        JLabel panelLabel = new JLabel("Press F8 to disable logixs");
        panelLabel.setFont(panelLabel.getFont().deriveFont(9f));
        JPanel panel = new JPanel();
        panel.add(panelLabel);
        return panel;
    }
    
    static protected void startupDebug(){
        debugFired = true;
        debugmsg=true;

        debugmsg=false;
    }
    
    private void prepareFontLists() {
        // Prepare font lists
        new Thread(new Runnable() {
            public void run() {
                log.debug("Prepare font lists...");
                FontComboUtil.prepareFontLists();
                log.debug("...Font lists built");
            }
        }).start();
    }

    protected void initMacOSXMenus() {
        jmri.plaf.macosx.Application macApp = jmri.plaf.macosx.Application.getApplication();
        macApp.setAboutHandler(new AboutHandler() {

            @Override
            public void handleAbout(EventObject eo) {
                new AboutDialog(null, true).setVisible(true);
            }
        });
        macApp.setPreferencesHandler(new PreferencesHandler() {

            @Override
            public void handlePreferences(EventObject eo) {
                new TabbedPreferencesAction("Preferences").actionPerformed();
            }
        });
        macApp.setQuitHandler(new QuitHandler() {

            @Override
            public boolean handleQuitRequest(EventObject eo) {
                handleQuit();
                return true;
            }
        });
    }

    static Logger log = Logger.getLogger(Apps3.class.getName());
    
}


