// JmriJFrame.java

package jmri.util;

import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.KeyStroke;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
 
/**
 * JFrame extended for common JMRI use.
 * <P>
 * We needed a place to refactor common JFrame additions in JMRI
 * code, so this class was created.
 * <P>
 * Features:
 * <ul>
 * <LI>Size limited to the maximum available on the screen, after
 * removing any menu bars (Mac) and taskbars (Windows)
 * <LI>Cleanup upon closing the frame: When the
 * frame is closed (WindowClosing event), the 
 * dispose() method is invoked to do cleanup. This is
 * inherited from JFrame itself, so super.dispose() needs
 * to be invoked in the over-loading methods.
 * <LI>Maintains a list of existing JmriJFrames
 * </ul>
 *
 * <h3>Window Closing</h3>
 * Normally, a JMRI window wants to be disposed when it closes.
 * This is what's needed when each invocation of the corresponding action
 * can create a new copy of the window.  To do this, you don't have
 * to do anything in your subclass.  This class has
<p><pre><code>
 setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE)
</code></pre>
 * <p>If you want this behavior, but need to do something when the 
 * window is closing, override the {@link #windowClosing(java.awt.event.WindowEvent)} method
 * to do what you want. Also, if you override dispose(),
 * make sure to call super.dispose().
 * <p>
 * If you want the window to just do nothing or just hide, rather than be disposed,
 * when closed, set the DefaultCloseOperation to 
 * DO_NOTHING_ON_CLOSE or HIDE_ON_CLOSE depending on what you're looking for.
 *
 * @author Bob Jacobsen  Copyright 2003, 2008
 * @version $Revision: 1.38 $
 * GT 28-AUG-2008 Added window menu
 */

public class JmriJFrame extends JFrame implements java.awt.event.WindowListener, jmri.ModifiedFlag, java.awt.event.ComponentListener {

    /**
     * Creates a JFrame
     * @param saveSize - Set true to save the last known size
     * @param savePosition - Set true to save the last known location
     */
    public JmriJFrame(boolean saveSize, boolean savePosition) {
	    super();

        p = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
        reuseFrameSavedPosition=savePosition;
        reuseFrameSavedSized=saveSize;
	    self = this;
        addWindowListener(this);
        addComponentListener(this);
        
        /* This ensures that different jframes do not get placed directly on top 
        of each other, but offset by the top inset.  However a saved preferences
        can over ride this */
        for(int i = 0; i<list.size();i++){
            JmriJFrame j = list.get(i);
            if(j.getExtendedState()!=ICONIFIED){
                if ((j.getX()==this.getX()) && (j.getY()==this.getY())){
                    offSetFrameOnScreen(j);
                }
            }
        }
        
        synchronized (list) {
            list.add(this);
        }
	    // Set the image for use when minimized
	    setIconImage(getToolkit().getImage("resources/jmri32x32.gif"));
        // set the close short cut
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        addWindowCloseShortCut();
        
        windowFrameRef = this.getClass().getName();
        if (!this.getClass().getName().equals(JmriJFrame.class.getName())){
            generateWindowRef();
            setFrameLocation();
        }
    }
    
    public JmriJFrame() {
        this(true, true);
    }
    
    public JmriJFrame(String name) {
        this(name, true, true);
    }
    
    /**
     * Creates a JMRI JFrame
     * @param name - Title of the JFrame
     * @param saveSize - Set true to save the last knowm size
     * @param savePosition - Set true to save the last known location
     */
    public JmriJFrame(String name, boolean saveSize, boolean savePosition) {
        this(saveSize, savePosition);
        setTitle(name);
        
        if (this.getClass().getName().equals(JmriJFrame.class.getName())){
            if ((this.getTitle()==null) || (this.getTitle().equals("")))
                return;
        }
        setFrameLocation();
    }
    
    void setFrameLocation(){
        if ((p != null) && (p.isWindowPositionSaved(windowFrameRef))) {
            Dimension screen = getToolkit().getScreenSize();
            if ((reuseFrameSavedPosition) && (!((p.getWindowLocation(windowFrameRef).getX()>=screen.getWidth()) ||
                (p.getWindowLocation(windowFrameRef).getY()>=screen.getHeight())))){
                this.setLocation(p.getWindowLocation(windowFrameRef));
            }
            /* Simple case that if either height or width are zero, then we should
            not set them */
            if ((reuseFrameSavedSized) &&(!((p.getWindowSize(windowFrameRef).getWidth()==0.0) ||
                (p.getWindowSize(windowFrameRef).getHeight()==0.0)))){
                this.setPreferredSize(p.getWindowSize(windowFrameRef));
            }
            
            /* We just check to make sure that having set the location
            that we do not have anther frame with the same class name and title
            in the same location, if it is we offset */
            for(int i = 0; i<list.size();i++){
                JmriJFrame j = list.get(i);
                if(j.getClass().getName().equals(this.getClass().getName()) 
                    && (j.getExtendedState()!=ICONIFIED)
                        && j.getTitle().equals(getTitle())) {
                    if ((j.getX()==this.getX()) && (j.getY()==this.getY())){
                        offSetFrameOnScreen(j);
                    }
                }
            }
        }
    }
    
    void generateWindowRef(){
        String initref = this.getClass().getName();
        if((this.getTitle()!=null) && (!this.getTitle().equals(""))){
            if (initref.equals(JmriJFrame.class.getName())){
                initref=this.getTitle();
            } else {
                initref = initref + ":" + this.getTitle();
            }
        }
        int refNo = 1;
        String ref = initref;
        for(int i = 0; i<list.size();i++){
            JmriJFrame j = list.get(i);
            if(j!=this && j.getWindowFrameRef().equals(ref)){
                ref = initref+":"+refNo;
                refNo++;
            }
        }
        windowFrameRef = ref;
    
    }

    @Override
    public void pack(){
        super.pack();
        reSizeToFitOnScreen();
    }
    
    void reSizeToFitOnScreen(){
        Dimension dim = getMaximumSize();
        int width = this.getPreferredSize().width;
        int height = this.getPreferredSize().height;

        if ((width+this.getX())>=dim.getWidth()){
            width = width - (int)((width + this.getX())-dim.getWidth());
        }
        if ((height+this.getY())>=dim.getHeight()){
            height = height - (int)((height + this.getY())-dim.getHeight());
        }
        this.setSize(width, height);

    
    }
    
    void offSetFrameOnScreen(JmriJFrame f){
    /* We use the frame that we are moving away from insets, as at this point 
    our own insets have not been correctly built and always return a size of zero */
        int frameOffSetx = this.getX()+f.getInsets().top;
        int frameOffSety = this.getY()+f.getInsets().top;
        Dimension dim = getMaximumSize();
        
        if (frameOffSetx>=(dim.getWidth()*0.75)){
            frameOffSety = 0;
            frameOffSetx = (f.getInsets().top)*2;
        }
        if (frameOffSety>=(dim.getHeight()*0.75)){
            frameOffSety = 0;
            frameOffSetx = (f.getInsets().top)*2;
        }
        /* If we end up with our off Set of X being greater than the width of the
        screen we start back at the beginning but with a half offset */
        if (frameOffSetx>=dim.getWidth())
            frameOffSetx=f.getInsets().top/2;
        this.setLocation(frameOffSetx, frameOffSety);
    }
    
    jmri.UserPreferencesManager p;
    
    String windowFrameRef;
    
    String getWindowFrameRef(){ return windowFrameRef; }

    @Override
    public void setTitle(String t){
        if(t.equals(getTitle()))
            return;
        super.setTitle(t);
        generateWindowRef();
    }
    
    
    /**
     * By default, Swing components should be 
     * created an installed in this method, rather than
     * in the ctor itself.
     */
    public void initComponents() throws Exception {}
    
    /**
     * Add a standard help menu, including window specific help item.
     * @param ref JHelp reference for the desired window-specific help page
     * @param direct true if the help menu goes directly to the help system,
     *        e.g. there are no items in the help menu
     */
    public void addHelpMenu(String ref, boolean direct) {
        // only works if no menu present?
        JMenuBar bar = getJMenuBar();
        if (bar == null) bar = new JMenuBar();
        // add Window menu
		bar.add(new WindowMenu(this)); // * GT 28-AUG-2008 Added window menu
		// add Help menu
        jmri.util.HelpUtil.helpMenu(bar, ref, direct);
        setJMenuBar(bar);
    }
    
    /**
     * Adds a "Close Window" key short cut to close window on op-W.
     */
    void addWindowCloseShortCut() {
        // modelled after code in JavaDev mailing list item by Bill Tschumy <bill@otherwise.com> 08 Dec 2004 
        AbstractAction act = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                // if (log.isDebugEnabled()) log.debug("keystroke requested close window "+JmriJFrame.this.getTitle());
                JmriJFrame.this.processWindowEvent(
                    new java.awt.event.WindowEvent(JmriJFrame.this, 
                                                java.awt.event.WindowEvent.WINDOW_CLOSING));
            }
        };
        getRootPane().getActionMap().put("close", act);

        int stdMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        InputMap im = getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, stdMask), "close");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close");
    }
        
    JmriJFrame self;
    
    /**
     * Provide a maximum frame size that is limited
     * to what can fit on the screen after toolbars, etc
     * are deducted.
     *<P>
     * Some of the methods used here return null pointers 
     * on some Java implementations, however, so 
     * this will return the superclasses's maximum size
     * if the algorithm used here fails.
     */
    public Dimension getMaximumSize() {
        // adjust maximum size to full screen minus any toolbars
        try {
            // Try our own alorithm.  This throws null-pointer exceptions on
            // some Java installs, however, for unknown reasons, so be
            // prepared to fall back.
            try {
                // First, ask for the physical screen size
                Dimension screen = getToolkit().getScreenSize();

                // Next, ask for any insets on the screen.
                Insets insets = JmriInsets.getInsets();
                int widthInset = insets.right+insets.left;
                int heightInset = insets.top+insets.bottom;
                
                // If insets are zero, guess based on system type
                if (widthInset == 0 && heightInset == 0) {
                    String type = System.getProperty("os.name","");
                    if (type.equals("Linux")) {
                        // Linux generally has a bar across the top and/or bottom
                        // of the screen, but lets you have the full width.
                        heightInset = 70;
                    }
                    // Windows generally has values, but not always,
                    // so we provide observed values just in case
                    else if (type.equals("Windows XP")) {
                        heightInset = 28;  // bottom 28
                    } else if (type.equals("Windows 98")) {
                        heightInset = 28;  // bottom 28
                    } else if (type.equals("Windows 2000")) {
                        heightInset = 28;  // bottom 28
                    }
                }
                
                // Insets may also be provided as system parameters
                String sw = System.getProperty("jmri.inset.width");
                if (sw!=null) try {
                    widthInset = Integer.parseInt(sw);
                } catch (Exception e1) {log.error("Error parsing jmri.inset.width: "+e1);}
                String sh = System.getProperty("jmri.inset.height");
                if (sh!=null) try {
                    heightInset = Integer.parseInt(sh);
                } catch (Exception e1) {log.error("Error parsing jmri.inset.height: "+e1);}
                           
                // calculate size as screen size minus space needed for offsets
                return new Dimension(screen.width-widthInset, screen.height-heightInset);
                
            } catch (NoSuchMethodError e) {
                Dimension screen = getToolkit().getScreenSize();
                return new Dimension(screen.width,
                    screen.height-45);  // approximate this...
            }
        } catch (Exception e2) {
            // failed completely, fall back to standard method
            return super.getMaximumSize();
        }
    }

    /**
     * The preferred size must fit on the physical screen, so 
     * calculate the lesser of either the preferred size from the
     * layout or the screen size.
     */
    public Dimension getPreferredSize() {
        // limit preferred size to size of screen (from getMaximumSize())
        Dimension screen = getMaximumSize();
        int width = Math.min(super.getPreferredSize().width, screen.width);
        int height = Math.min(super.getPreferredSize().height, screen.height);
        return new Dimension(width, height);
    }

    /**
     * Get a List of the currently-existing JmriJFrame objects.
     * The returned list is a copy made at the time of the call,
     * so it can be manipulated as needed by the caller.
     */
    public static java.util.List<JmriJFrame> getFrameList() {
        java.util.List<JmriJFrame> returnList;
        synchronized(list) {
            returnList = new java.util.ArrayList<JmriJFrame>(list);
        }
        return returnList;
    }
    

    /**
     * Get a JmriJFrame of a particular name.
     * If more than one exists, there's no guarantee 
     * as to which is returned.
     */
    public static JmriJFrame getFrame(String name) {
        java.util.List<JmriJFrame> list = getFrameList();  // needed to get synch copy
        for (int i=0; i<list.size(); i++) {
            JmriJFrame j = list.get(i);
            if (j.getTitle().equals(name)) return j;
        }
        return null;
    }
    
    static volatile java.util.ArrayList<JmriJFrame> list = new java.util.ArrayList<JmriJFrame>();
    
    // handle resizing when first shown
    private boolean mShown = false;
    public void addNotify() {
        super.addNotify();
        // log.debug("addNotify window ("+getTitle()+")");
        if (mShown)
            return;
        // resize frame to account for menubar
        JMenuBar jMenuBar = getJMenuBar();
        if (jMenuBar != null) {
            int jMenuBarHeight = jMenuBar.getPreferredSize().height;
            Dimension dimension = getSize();
            dimension.height += jMenuBarHeight;
            setSize(dimension);
        }
        mShown = true;
    }

    /**
     * Set whether the frame Position is saved or not after it has been created.
     */
    public void setSavePosition(boolean save){
        reuseFrameSavedPosition=save;
        p.setSaveWindowLocation(windowFrameRef, save);
    }

    /**
     * Set whether the frame Size is saved or not after it has been created
     */
    public void setSaveSize(boolean save){
        reuseFrameSavedSized=save;
        p.setSaveWindowSize(windowFrameRef, save);
    }

    /**
     * Returns if the frame Position is saved or not
     */
    public boolean getSavePosition(){
        return reuseFrameSavedPosition;
    }

    /**
     * Returns if the frame Size is saved or not
     */
    public boolean getSaveSize(){
        return reuseFrameSavedSized;
    }


    /**
     * A frame is considered "modified" if it has changes
     * that have not been stored.
     */
    public void setModifiedFlag(boolean flag) {
        this.modifiedFlag = flag;
        // mark the window in the GUI
        markWindowModified(this.modifiedFlag);
    }
    /**
     * Get the balue of the modified flag.
     * <p>Not a bound parameter
     */
    public boolean getModifiedFlag() { return modifiedFlag; }
    private boolean modifiedFlag = false;
    
    /**
     * Handle closing a window or quiting the program
     * while the modified bit was set.
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="LI_LAZY_INIT_STATIC", justification="modified is only on Swing thread")
    protected void handleModified() {
        if (getModifiedFlag()) {
            if (rb == null) rb = java.util.ResourceBundle.getBundle("jmri.util.UtilBundle");
            this.setVisible(true);
            int result = javax.swing.JOptionPane.showOptionDialog(this,
                rb.getString("WarnChangedMsg"),
                rb.getString("WarnChangedTitle"),
                javax.swing.JOptionPane.YES_NO_OPTION,
                javax.swing.JOptionPane.WARNING_MESSAGE,
                null, // icon
                new String[]{rb.getString("WarnYesSave"),rb.getString("WarnNoClose")},
                rb.getString("WarnYesSave")
            );
            if (result == javax.swing.JOptionPane.YES_OPTION) {
                // user wants to save
                storeValues();
            }
        }
    }
    static java.util.ResourceBundle rb = null; 
    protected void storeValues() {
        log.error("default storeValues does nothing for "+getTitle());
    }
        
    
    // For marking the window as modified on MacOS X
    // See: http://developer.apple.com/qa/qa2001/qa1146.html
    final static String WINDOW_MODIFIED = "windowModified";
    public void markWindowModified(boolean yes){
        getRootPane().putClientProperty(WINDOW_MODIFIED, yes ? Boolean.TRUE : Boolean.FALSE);
    }
    
    // Window methods
    public void windowOpened(java.awt.event.WindowEvent e) {}
    public void windowClosed(java.awt.event.WindowEvent e) {}
    
    public void windowActivated(java.awt.event.WindowEvent e) {}
    public void windowDeactivated(java.awt.event.WindowEvent e) {}
    public void windowIconified(java.awt.event.WindowEvent e) {}
    public void windowDeiconified(java.awt.event.WindowEvent e) {}

    public void windowClosing(java.awt.event.WindowEvent e) {
        handleModified();
    }
    
    public void componentHidden(java.awt.event.ComponentEvent e) { }
    
    public void componentMoved(java.awt.event.ComponentEvent e) {
        jmri.UserPreferencesManager p = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
        if ((p != null) && (reuseFrameSavedPosition)) {
            p.setWindowLocation(windowFrameRef, this.getLocation());
        }
    }
    
    public void componentResized(java.awt.event.ComponentEvent e) {
        jmri.UserPreferencesManager p = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
        if ((p != null) && (reuseFrameSavedSized)) {
            p.setWindowSize(windowFrameRef, this.getSize());
        }
    }
    
    public void componentShown(java.awt.event.ComponentEvent e) { }
    

    private jmri.implementation.AbstractShutDownTask task = null;
    protected void setShutDownTask() {
        if (jmri.InstanceManager.shutDownManagerInstance()!=null) {
            task = 
                    new jmri.implementation.AbstractShutDownTask(getTitle()){
                        public boolean execute() {
                            handleModified();
                            return true;
                        }
            };
            jmri.InstanceManager.shutDownManagerInstance().register(task);
        }
    }

    protected boolean reuseFrameSavedPosition = true;
    protected boolean reuseFrameSavedSized = true;

    /**
     * When window is finally destroyed, remove it from the 
     * list of windows.
     * <P>
     * Subclasses that over-ride this method must invoke this implementation
     * with super.dispose()
     */
    public void dispose() {
        jmri.UserPreferencesManager p = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
        if (p != null) {
            if (reuseFrameSavedPosition)
                p.setWindowLocation(windowFrameRef, this.getLocation());
            if (reuseFrameSavedSized)
                p.setWindowSize(windowFrameRef, this.getSize());
        }
        log.debug("dispose "+getTitle());
        if (task != null) {
            jmri.InstanceManager.shutDownManagerInstance().deregister(task);
            task = null;
        }
        synchronized (list) {
            list.remove(this);
        }
        super.dispose();
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(JmriJFrame.class.getName());

}
