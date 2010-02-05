// PanelMenu.java

package jmri.jmrit.display;

import java.util.ResourceBundle;

import javax.swing.JMenu;
import javax.swing.JSeparator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import java.util.ArrayList;
import jmri.util.JmriJFrame;
import jmri.jmrit.display.panelEditor.PanelEditor;
import jmri.jmrit.display.layoutEditor.LayoutEditor;

/**
 * Create the default "Panels" menu for use in a menubar.
 *
 * Also manages the Show Panel menu for both all Editor panels.
 *
 * @author	Bob Jacobsen   Copyright 2003, 2004
 * @author  Dave Duchamp   Copyright 2007
 * @version     $Revision: 1.19 $
 */
public class PanelMenu extends JMenu {
    public PanelMenu() {

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.DisplayBundle");

        this.setText(rb.getString("MenuPanels"));

        // new panel is a submenu
		//add(new jmri.jmrit.display.NewPanelAction());
        JMenu newPanel = new JMenu(rb.getString("MenuItemNew"));
        newPanel.add(new jmri.jmrit.display.panelEditor.PanelEditorAction(rb.getString("PanelEditor")));
        newPanel.add(new jmri.jmrit.display.controlPanelEditor.ControlPanelEditorAction(rb.getString("ControlPanelEditor")));
        newPanel.add(new jmri.jmrit.display.layoutEditor.LayoutEditorAction(rb.getString("LayoutEditor")));
        add(newPanel);
        
        add(new jmri.configurexml.LoadXmlUserAction(rb.getString("MenuItemLoad")));
        add(new jmri.configurexml.StoreXmlUserAction(rb.getString("MenuItemStore")));
        add(new jmri.jmrit.revhistory.swing.FileHistoryAction(rb.getString("MenuItemShowHistory")));
        add(new JSeparator());
		panelsSubMenu = new JMenu(rb.getString("MenuShowPanel"));
		add(panelsSubMenu);
        add(new JSeparator());
        add(new jmri.jmrit.jython.RunJythonScript(rb.getString("MenuItemScript")));
        add(new jmri.jmrit.automat.monitor.AutomatTableAction(rb.getString("MenuItemMonitor")));
        add(new jmri.jmrit.jython.JythonWindow(rb.getString("MenuItemScriptLog")));
        add(new jmri.jmrit.jython.InputWindowAction(rb.getString("MenuItemScriptInput")));
		thisMenu = this;
    }
	
	// operational variables
	private JMenu panelsSubMenu	= null;
	static private PanelMenu thisMenu = null;
    private ArrayList<Editor> panelsList = new ArrayList<Editor>();  
		
	/** 
	 * Provide method to reference this panel menu
	 */	
	static public PanelMenu instance() {
		if (thisMenu==null) thisMenu = new PanelMenu();
		return thisMenu;
	}
	
	/**
	 * Utility routine for getting the number of panels in the Panels sub menu
	 */
	public int getNumberOfPanels() {return panelsList.size();}
	
	/**
	 * Delete a panel from Show Panel sub menu
	 */
	public void deletePanel (Object panel) {
		if (panelsList.size()==0) return;
		for (int i = 0; i<panelsList.size(); i++) {
			Object o = panelsList.get(i);
			if (o == panel) {
				panelsList.remove(i);
				panelsSubMenu.remove(i);
                return;
			}
		}
	}
	
	/**
	 * Add an Editor panel to Show Panels sub menu
	 */
    public void addEditorPanel(final Editor panel) {
		panelsList.add(panel);
        ActionListener a = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
                    if (panel instanceof LayoutEditor) {
                        panel.setVisible(true);
                        panel.repaint();
                    } else {
                        panel.getTargetFrame().setVisible(true);
                    }
					updateEditorPanel(panel);
				}
			};
        JCheckBoxMenuItem r = new JCheckBoxMenuItem(panel.getTitle());
        r.addActionListener(a);
        panelsSubMenu.add(r);
        updateEditorPanel (panel);
    }
	
	/**
	 * Update an Editor type panel in Show Panels sub menu
	 */
	public void updateEditorPanel (Editor panel) {
		if (panelsList.size()==0) return;
		for (int i = 0; i<panelsList.size(); i++) {
			Object o = panelsList.get(i);
			if (o == panel) {
				JCheckBoxMenuItem r = (JCheckBoxMenuItem)panelsSubMenu.getItem(i);
                if (panel instanceof LayoutEditor) {
                    if (panel.isVisible()) r.setSelected(true);
                    else r.setSelected(false);
                } else {
                    if (panel.getTargetFrame().isVisible()) r.setSelected(true);
                    else r.setSelected(false);
                }
                return;
			}
		}
	}	
	
	/**
	 * Rename an Editor type panel in Show Panels sub menu
	 */
	public void renameEditorPanel (Editor panel) {
		if (panelsList.size()==0) return;
		for (int i = 0; i<panelsList.size(); i++) {
			Object o = panelsList.get(i);
			if (o == panel) {
				JCheckBoxMenuItem r = (JCheckBoxMenuItem)panelsSubMenu.getItem(i);
				r.setText(panel.getTitle());
                return;
			}
		}
	}
	
	/**
	 * Determine if named panel already exists
	 * returns true if named panel already loaded
	 */
	public boolean isPanelNameUsed (String name) {
		if (panelsList.size()==0) return false;
		for (int i = 0; i<panelsList.size(); i++) {
			try{
				Editor editor = panelsList.get(i);
				if (editor.getTargetFrame().getTitle().equals(name)) {
					return true;
				}
			} catch(Exception e){
			}
		}
		return false;
	}
	
	public Editor getEditorByName (String name){
		if (panelsList.size()==0) return null;
		for (int i = 0; (i<panelsList.size()); i++) {
			try{
				Editor editor = panelsList.get(i);
				if (editor.getTargetFrame().getTitle().equals(name)) {
					return editor;
				}
			} catch(Exception e){
			}
		}
		return null;
	}

	public ArrayList<Editor> getEditorPanelList() {
		return panelsList;
	}

	public ArrayList<LayoutEditor> getLayoutEditorPanelList() {
		ArrayList<LayoutEditor> lePanelsList = new ArrayList<LayoutEditor>();
		for (int i = 0; (i<panelsList.size()); i++) {
			try{
				LayoutEditor le = (LayoutEditor)panelsList.get(i);
				lePanelsList.add(le);
			} catch(Exception e){
			}
		}				
		return lePanelsList;
	}	
}


