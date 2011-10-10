// WindowMenu.java

package jmri.util;

import jmri.util.JmriJFrame;

import java.awt.Frame;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.MenuEvent;

/**
 * Creates a menu showing all open windows 
 * and allows to bring one in front
 * <P>
 * @author	Giorgio Terdina   Copyright 2008
 * @version     $Revision$
 * 18-Nov-2008 GT Replaced blank menu lines, due to untitled windows, with "Untitled" string
 */

public class WindowMenu extends JMenu implements javax.swing.event.MenuListener {

	private JFrame parentFrame;	// Keep note of the window containing the menu
	private List<JmriJFrame> framesList;	// Keep the list of windows, in order to find out which window was selected

    java.util.ResourceBundle rb;
    
    public WindowMenu(JFrame frame) {
        super(java.util.ResourceBundle.getBundle("apps.AppsBundle").getString("MenuWindow"));
		parentFrame = frame;
		addMenuListener(this);
    }
    

	public void menuSelected(MenuEvent e) {
		String windowName;
		framesList = JmriJFrame.getFrameList();
		removeAll();
		if (rb == null) rb=java.util.ResourceBundle.getBundle("apps.AppsBundle");
		
        add(new AbstractAction(rb.getString("MenuItemMinimize")){
            public void actionPerformed(ActionEvent e) {
                // the next line works on Java 2, but not 1.1.8
				parentFrame.setState(Frame.ICONIFIED);
            }
        }); 
		add(new JSeparator());

		int framesNumber = framesList.size();
		for (int i = 0; i < framesNumber; i++) {
			JmriJFrame iFrame = framesList.get(i);
			windowName = iFrame.getTitle();
			if(windowName.equals("")) windowName = "Untitled";
			JCheckBoxMenuItem newItem = new JCheckBoxMenuItem(new AbstractAction(windowName) {
				public void actionPerformed(ActionEvent e) {
					JMenuItem selectedItem = (JMenuItem)e.getSource();
					// Since different windows can have the same name, look for the position of the selected menu item
					int itemCount = getItemCount();
					// Skip possible other items at the top of the menu (e.g. "Minimize")
					int firstItem = itemCount - framesList.size();
					for (int i = firstItem; i < itemCount; i++) {
						if(selectedItem == getItem(i)) {
							i -= firstItem;
							// Retrieve the corresponding window
							if(i < framesList.size()) {	// "i" should always be < framesList.size(), but it's better to make sure
								framesList.get(i).setVisible(true);
								framesList.get(i).setExtendedState(Frame.NORMAL);
								return;
							}
						}
					} 
				}
			});
			if(iFrame == parentFrame) newItem.setState(true);
			add(newItem);
		}
	}
	
	public void menuDeselected(MenuEvent e) {}
	public void menuCanceled(MenuEvent e) {}
	
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(WindowMenu.class.getName());

}