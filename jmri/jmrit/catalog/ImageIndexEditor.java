// ImageIndexEditor.java

package jmri.jmrit.catalog;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.datatransfer.DataFlavor;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.BoxLayout;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import jmri.CatalogTree;
import jmri.util.JmriJFrame;
import jmri.jmrit.display.IconAdder;
import jmri.jmrit.display.Editor;
import jmri.jmrit.XmlFile;
import jmri.CatalogTreeManager;
import jmri.InstanceManager;

/**
 * A JFrame for creating and editing an Image Index.  This is a singleton class.
 * <P>
 * 
 *
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 * <P>
 *
 * @author			Pete Cressman  Copyright 2009
 *
 */
public class ImageIndexEditor extends JmriJFrame {

    CatalogPanel    _catalog;
    CatalogPanel    _index;

    static ImageIndexEditor _instance;
    static public boolean  _indexChanged = false;
    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.catalog.CatalogBundle");

    public static final String IconDataFlavorMime = DataFlavor.javaJVMLocalObjectMimeType +
               ";class=jmri.jmrit.catalog.NamedIcon";


    private ImageIndexEditor() {
        super();
    }

    private ImageIndexEditor(String name) {
        super(name);
    }

    public static ImageIndexEditor instance(Editor editor) {
        if (_instance == null) {
            _instance = new ImageIndexEditor(rb.getString("editIndexFrame"));
            _instance.init(editor);
        }
        return _instance;
    }

    private void init(Editor editor) {
        JMenuBar menuBar = new JMenuBar();
        JMenu findIcon = new JMenu(IconAdder.rb.getString("MenuFile"));
        menuBar.add(findIcon);
        JMenuItem storeItem = new JMenuItem(IconAdder.rb.getString("MIStoreImageIndex"));
        findIcon.add(storeItem);
        storeItem.addActionListener(new ActionListener() {
                Editor panelEd;
                public void actionPerformed(ActionEvent event) {
					storeImageIndex(panelEd);
                }
                ActionListener init(Editor pe) {
                    panelEd = pe;
                    return this;
                }
        }.init(editor));

        findIcon.addSeparator();
        JMenuItem openItem = new JMenuItem(IconAdder.rb.getString("openDirMenu"));
        class AActionListener implements ActionListener {
            IconAdder editor;
            public AActionListener() {
                editor = new IconAdder();
            }
            public void actionPerformed(ActionEvent e) {
                editor.openDirectory(false);
            }
        }
        openItem.addActionListener(new AActionListener());
        findIcon.add(openItem);

        JMenu editMenu = new JMenu(rb.getString("EditIndexMenu"));
        menuBar.add(editMenu);
        JMenuItem addItem = new JMenuItem(rb.getString("addNode"));
        addItem.addActionListener (new ActionListener () {
                public void actionPerformed(ActionEvent e) {
                    addNode();
                }
            });
        editMenu.add(addItem);
        JMenuItem renameItem = new JMenuItem(rb.getString("renameNode"));
        renameItem.addActionListener (new ActionListener () {
                public void actionPerformed(ActionEvent e) {
                    renameNode();
                }
            });
        editMenu.add(renameItem);
        JMenuItem deleteItem = new JMenuItem(rb.getString("deleteNode"));
        deleteItem.addActionListener (new ActionListener () {
                public void actionPerformed(ActionEvent e) {
                    deleteNode();
                }
            });
        editMenu.add(deleteItem);
        setJMenuBar(menuBar);

        addHelpMenu("package.jmri.jmrit.catalog.ImageIndex", true);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));
        mainPanel.add(makeCatalogPanel());
        mainPanel.add(new JSeparator(SwingConstants.VERTICAL));
        mainPanel.add(makeIndexPanel());
        getContentPane().add(mainPanel);

        // when this window closes, check for saving 
        addWindowListener(new java.awt.event.WindowAdapter() {
            Editor panelEd;
            public void windowClosing(java.awt.event.WindowEvent e) {
                checkImageIndex(panelEd);
            }
            java.awt.event.WindowAdapter init(Editor pe) {
                panelEd = pe;
                return this;
            }
        }.init(editor));
        setLocation(10, 200);
        pack();
        setVisible(true);
    }

    /**
    *  Called from window close of Icon Editors
    */
    public static void checkImageIndex(Editor editor) {
        if (jmri.jmrit.catalog.ImageIndexEditor._indexChanged) {
            int result = JOptionPane.showConfirmDialog(null, rb.getString("SaveImageIndex"), 
                                          rb.getString("question"), JOptionPane.YES_NO_CANCEL_OPTION,
                                                       JOptionPane.QUESTION_MESSAGE);
            if (result == JOptionPane.YES_OPTION) {
                storeImageIndex(editor);
            } else if (result == JOptionPane.NO_OPTION) {
                _indexChanged = false;
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static void storeImageIndex(Editor editor) {
        // build a new Default Icons tree
        CatalogTreeManager manager = InstanceManager.catalogTreeManagerInstance();
        CatalogTree tree = manager.getBySystemName("NXDI");
        /*
        if (tree != null){
            manager.deregister(tree);
        }
        tree = manager.newCatalogTree("NXDI", "Default Icons");
        */
        if (tree == null) {
            tree = manager.newCatalogTree("NXDI", "Default Icons");
        }
        Iterator <IconAdder>iter = editor.getIconEditors().iterator();
        CatalogTreeNode root = (CatalogTreeNode)tree.getRoot();
        while (iter.hasNext()) {
            IconAdder ed = iter.next();
            CatalogTreeNode node = ed.getDefaultIconNode();
            Enumeration<CatalogTreeNode> e = root.children();
            String name = node.toString();
            while (e.hasMoreElements()) {
                CatalogTreeNode nChild = e.nextElement();
                if (name.equals(nChild.toString())) {
                    if (log.isDebugEnabled()) log.debug("Remove node "+nChild);
                    root.remove(nChild);
                    break;
                }
            }
            root.add(node);
            if (log.isDebugEnabled()) log.debug("Add node "+node);
        }

        if (log.isDebugEnabled()) log.debug("Start writing CatalogTree info");
        try {
            new jmri.jmrit.catalog.configurexml.DefaultCatalogTreeManagerXml().writeCatalogTrees();
            _indexChanged = false;
            tree = manager.getBySystemName("IXII");
            if (tree != null) {
                editor.addTreeToEditors(tree);
            }
        } 
        //catch (org.jdom.JDOMException jde) { log.error("Exception writing CatalogTrees: "+jde); }                           
        catch (java.io.IOException ioe) { log.error("Exception writing CatalogTrees: "+ioe); }   
    }
    
    private JPanel makeCatalogPanel() {
        _catalog = new CatalogPanel("catalog", "selectNode");
        _catalog.init(false);
        CatalogTreeManager manager = InstanceManager.catalogTreeManagerInstance();
        List <String> sysNames = manager.getSystemNameList();
        if (sysNames != null) {
            for (int i=0; i<sysNames.size(); i++) {
                String systemName = sysNames.get(i);
                if (systemName.startsWith("IF")) {
                    _catalog.addTree( manager.getBySystemName(systemName));
                }
            }
        }
        _catalog.createNewBranch("IFJAR", "Program Directory", "resources");
        XmlFile.ensurePrefsPresent("resources");
        _catalog.createNewBranch("IFPREF", "Preferences Directory", XmlFile.prefsDir()+"resources");
        return _catalog;
    }

    private JPanel makeIndexPanel() {
        _index = new CatalogPanel("ImageIndex", "selectIndexNode");
        _index.init(true);

        boolean found = false;
        CatalogTreeManager manager = InstanceManager.catalogTreeManagerInstance();
        List <String> sysNames = manager.getSystemNameList();
        if (sysNames != null) {
            for (int i=0; i<sysNames.size(); i++) {
                String systemName = sysNames.get(i);
                if (systemName.startsWith("IX")) {
                    _index.addTree(manager.getBySystemName(systemName));
                    found = true;
                }
            }
        }
        if (!found) {
            _index.createNewBranch("IXII", rb.getString("ImageIndexRoot"), "ImageIndexRoot");
        }
       return _index;
    }

    void addNode() {
        CatalogTreeNode selectedNode = _index.getSelectedNode();
        if (selectedNode == null) {
            JOptionPane.showMessageDialog(this, rb.getString("selectAddNode"), 
                                          rb.getString("info"), JOptionPane.INFORMATION_MESSAGE);
        } else {
            String name = JOptionPane.showInputDialog(this, rb.getString("nameAddNode"), 
                                          rb.getString("question"), JOptionPane.QUESTION_MESSAGE);
            if (name != null) {
                if(!_index.insertNodeIntoModel(name, selectedNode)) {
                    JOptionPane.showMessageDialog(this, java.text.MessageFormat.format(
                                        rb.getString("duplicateNodeName"), new Object[] {name}), 
                                        rb.getString("error"), JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        invalidate();
    }

    void renameNode() {
        CatalogTreeNode selectedNode = _index.getSelectedNode();
        if (selectedNode == null) {
            JOptionPane.showMessageDialog(this, rb.getString("selectRenameNode"), 
                                          rb.getString("info"), JOptionPane.INFORMATION_MESSAGE);
        } else {
            String name = JOptionPane.showInputDialog(this, rb.getString("newNameNode"), 
                                          selectedNode.getUserObject());
            if (name != null) {
                if (!_index.nodeChange(selectedNode, name)){
                    JOptionPane.showMessageDialog(this, java.text.MessageFormat.format(
                                        rb.getString("duplicateNodeName"), new Object[] {name}), 
                                        rb.getString("error"), JOptionPane.ERROR_MESSAGE);
                }

            }
        }
        invalidate();
    }

    void deleteNode() {
        CatalogTreeNode selectedNode = _index.getSelectedNode();
        if (selectedNode == null) {
            JOptionPane.showMessageDialog(this, rb.getString("selectDeleteNode"), 
                                          rb.getString("info"), JOptionPane.INFORMATION_MESSAGE);
        } else {
            int numNodes = countSubNodes(selectedNode);
            int numIcons = countIcons(selectedNode);
            int response = JOptionPane.showConfirmDialog(this, java.text.MessageFormat.format(
                                    rb.getString("confirmDeleteNode"), new Object[] 
                                        {(String)selectedNode.getUserObject(), new Integer(numNodes), new Integer(numIcons)}),
                                        rb.getString("question"), JOptionPane.YES_NO_OPTION,
                                                        JOptionPane.QUESTION_MESSAGE);
            if (response == JOptionPane.YES_OPTION) {
                _index.removeNodeFromModel(selectedNode);
            }
        }
    }

    @SuppressWarnings("unchecked")
	private int countSubNodes(CatalogTreeNode node) {
        int cnt = 0;
        Enumeration<CatalogTreeNode> e =node.children();
        while (e.hasMoreElements()) {
            CatalogTreeNode n = e.nextElement();
            cnt += countSubNodes(n) + 1;
        }
        return cnt;
    }

    @SuppressWarnings("unchecked")
	private int countIcons(CatalogTreeNode node) {
        int cnt = 0;
        Enumeration<CatalogTreeNode> e =node.children();
        while (e.hasMoreElements()) {
            CatalogTreeNode n = e.nextElement();
            cnt += countIcons(n);
        }
        cnt += node.getNumLeaves();
        return cnt;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ImageIndexEditor.class.getName());
}


