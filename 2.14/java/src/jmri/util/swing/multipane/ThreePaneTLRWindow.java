// ThreePaneTLRWindow.java

package jmri.util.swing.multipane;

import java.awt.*;
import java.io.File;

import javax.swing.*;

import jmri.util.swing.*;

/**
 * MultiPane JMRI window with a "top" area over 
 * "left" and "right" lower panes, 
 * optional toolbar and menu.
 *
 * @author Bob Jacobsen  Copyright 2010
 * @since 2.9.4
 * @version $Revision$
 */

public class ThreePaneTLRWindow extends jmri.util.JmriJFrame {

    /**
     * Create and initialize a multi-pane GUI window.
     */
    public ThreePaneTLRWindow(String name, File menubarFile, File toolbarFile) {
        super(name);
        buildGUI(menubarFile, toolbarFile);
        pack();
    }
    
    JSplitPane      upDownSplitPane;
    JSplitPane      leftRightSplitPane;

    JPanel          top = new JPanel();
    
    JPanel          left = new JPanel();
    JPanel          right = new JPanel();

    public JComponent getTop() {
        return top;
    }
    public JComponent getRight() {
        return right;
    }
    public JComponent getLeft() {
        return left;
    }
    
    WindowInterface rightTopWI;
    
    protected void buildGUI(File menubarFile, File toolbarFile) {
        configureFrame();
        addMainMenuBar(menubarFile);
        addMainToolBar(toolbarFile);
    }
    
    protected void configureFrame() {
                       
        rightTopWI = new jmri.util.swing.sdi.JmriJFrameInterface();  // TODO figure out what WI is used here
 
        //rightTop.setBorder(BorderFactory.createLineBorder(Color.black));
        top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
        right.setLayout(new FlowLayout());
        left.setLayout(new FlowLayout());

        leftRightSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        leftRightSplitPane.setOneTouchExpandable(true);
        leftRightSplitPane.setResizeWeight(0.0);  // emphasize right part
        
        upDownSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, 
                                    top,
                                    leftRightSplitPane);
        upDownSplitPane.setOneTouchExpandable(true);
        upDownSplitPane.setResizeWeight(1.0);  // emphasize top part
        
        add(upDownSplitPane, BorderLayout.CENTER);
    }
                
    public void resetRightToPreferredSizes() { leftRightSplitPane.resetToPreferredSizes(); }
    
    protected void addMainMenuBar(File menuFile) {
        if (menuFile == null) return;
        JMenuBar menuBar = new JMenuBar();
        
        JMenu[] menus = JMenuUtil.loadMenu(menuFile, rightTopWI, null);
        for (JMenu j : menus) 
            menuBar.add(j);

        setJMenuBar(menuBar);
    }

    protected void addMainToolBar(File toolBarFile) {
        if (toolBarFile == null) return;
          
        JToolBar toolBar = JToolBarUtil.loadToolBar(toolBarFile, rightTopWI, null);

        // this takes up space at the top until pulled to floating
        add(toolBar, BorderLayout.NORTH);
    }
    
    /**
     * Only close frame, etc, dispose() disposes of all 
     * cached panes
     */
    public void dispose() {
        rightTopWI.dispose();
        super.dispose();
    }
    
}