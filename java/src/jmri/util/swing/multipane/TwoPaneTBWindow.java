// TwoPaneTBWindow.java

package jmri.util.swing.multipane;

import java.awt.*;
import java.io.File;

import javax.swing.*;
import javax.swing.border.BevelBorder;

import jmri.util.swing.*;

/**
 * MultiPane JMRI window with a "top" area over 
 * a single bottom lower pane, 
 * optional toolbar and menu.
 *
 * @author Bob Jacobsen  Copyright 2010
 * @since 2.13.1
 * @version $Revision: 17977 $
 */

abstract public class TwoPaneTBWindow extends jmri.util.JmriJFrame {

    /**
     * Create and initialize a multi-pane GUI window.
     */
    public TwoPaneTBWindow(String name, File menubarFile, File toolbarFile) {
        super(name);
        buildGUI(menubarFile, toolbarFile);
        pack();
    }
    
    JSplitPane      upDownSplitPane;

    JPanel          top = new JPanel();
    
    JPanel          bottom = new JPanel();
    
    JPanel          statusBar = new JPanel();
    
    JToolBar toolBar = new JToolBar();

    public JComponent getTop() {
        return top;
    }
    public JComponent getBottom() {
        return bottom;
    }
    
    public JComponent getToolBar() {
        return toolBar;
    }
    
    public JComponent getSplitPane(){
        return upDownSplitPane;
    }
    
    protected void buildGUI(File menubarFile, File toolbarFile) {
        configureFrame();
        addMainMenuBar(menubarFile);
        addMainToolBar(toolbarFile);
        addMainStatusBar();
    }
    
    protected void configureFrame() {
 
        //rightTop.setBorder(BorderFactory.createLineBorder(Color.black));
        top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
        
        upDownSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, 
                                    top,
                                    bottom);
        upDownSplitPane.setOneTouchExpandable(true);
        upDownSplitPane.setResizeWeight(1.0);  // emphasize top part
        
        add(upDownSplitPane, BorderLayout.CENTER);
    }
                
    public void resetTopToPreferredSizes() { upDownSplitPane.resetToPreferredSizes(); }
    
    protected boolean hideBottomPane = false;
    
    public void hideBottomPane(boolean hide) {
        if(hideBottomPane==hide){
            return;
        }
        hideBottomPane = hide;
        if(hide){
            upDownSplitPane.setDividerLocation(1.0d);
        } else {
            resetTopToPreferredSizes();
        }
    }
    
    JMenuBar menuBar = new JMenuBar();
    
    protected void addMainMenuBar(File menuFile) {
        if (menuFile == null) return;
        
        JMenu[] menus = JMenuUtil.loadMenu(menuFile, this, this);
        for (JMenu j : menus) 
            menuBar.add(j);

        setJMenuBar(menuBar);
    }
    
    public JMenuBar getMenu(){
        return menuBar;
    }
    
    protected void addMainToolBar(File toolBarFile) {
        if (toolBarFile == null) return;
          
        toolBar = JToolBarUtil.loadToolBar(toolBarFile, this, this);

        // this takes up space at the top until pulled to floating
        add(toolBar, BorderLayout.NORTH);
    }
    
    abstract public void remoteCalls(String args[]);
    
    protected void addMainStatusBar(){
        statusBar.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 0));
        statusBar.setBorder(new BevelBorder(BevelBorder.LOWERED));
        
        statusBox = Box.createHorizontalBox();
        statusBox.add(Box.createHorizontalGlue());
        statusBar.add(statusBox);
        add(statusBar, BorderLayout.SOUTH);
    }
    
    public void addToStatusBox(JLabel title, JLabel value){
        JPanel statusItemPanel = new JPanel();
        statusItemPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
        //Set the font size of the status bar text to be 2points less than the default configured
        int fontSize = apps.GuiLafConfigPane.getFontSize()-2;
        if(title!=null){
            if(fontSize<=4)
                fontSize = title.getFont().getSize()-2;
            title.setFont(title.getFont().deriveFont((float)fontSize));
            statusItemPanel.add(title);
        }
        if(value!=null){
            if(fontSize<=4)
                fontSize = value.getFont().getSize()-2;
            value.setFont(value.getFont().deriveFont((float)fontSize));
            statusItemPanel.add(value);
        }
        addToStatusBox(statusItemPanel);
    }
    
    Box statusBox;
    int statusBoxIndex = 0;	// index to insert extra stuff
    static final int statusStrutWidth = 10;
    
    public void addToStatusBox(Component comp){
        if(statusBoxIndex!=0){
            statusBox.add(Box.createHorizontalStrut(statusStrutWidth), statusBoxIndex);
            ++statusBoxIndex;
            statusBox.add(new JSeparator(javax.swing.SwingConstants.VERTICAL),statusBoxIndex);
            ++statusBoxIndex;
        }
        statusBox.add(comp, statusBoxIndex);
        ++statusBoxIndex;
    }

    /**
     * Only close frame, etc, dispose() disposes of all 
     * cached panes
     */
    public void dispose() {
        super.dispose();
    }
    
    /*
    The property change listener is located here so that the menus can interact with the front end
    */
    
    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);
    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    protected void firePropertyChange(String p, Object old, Object n) {
        if (pcs==null)
            return;
        pcs.firePropertyChange(p,old,n);
    }
    
}