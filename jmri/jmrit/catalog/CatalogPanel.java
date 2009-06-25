// CatalogPanel.java

package jmri.jmrit.catalog;

import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.JRadioButton;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.RenderingHints;
//import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.datatransfer.Transferable; 
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;

import java.io.IOException;
import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.List;
import java.util.ArrayList;

import jmri.CatalogTree;
import jmri.CatalogTreeManager;
import jmri.InstanceManager;
import jmri.jmrit.display.IconAdder;
import jmri.jmrit.XmlFile;

/**
 * Create a JPanel containing trees of resources to replace default icons.
 * The panel also displays image files files contained in a node of a tree.
 * Drag and Drop is implemented to drag a display of an icon to the
 * display of an icon that may be added to the panel.
 * <P>
 * This panel is used in the Icon Editors and also in the ImageIndex Editor.
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
public class CatalogPanel extends JPanel implements MouseListener {

    JPanel          _selectedImage;
    static Color    _grayColor = new Color(235,235,235);
    Color           _currentBackground = _grayColor;

    JLabel          _previewLabel = new JLabel();
    JPanel          _preview;
    boolean         _noDrag;

    JScrollPane             _treePane;
    JTree                   _dTree;
    DefaultTreeModel        _model;
    ArrayList <CatalogTree> _branchModel = new ArrayList <CatalogTree>();

    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.catalog.CatalogBundle");

    public CatalogPanel(String label1, String label2) {
        super(true);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JPanel p1 = new JPanel();
        p1.setLayout(new BorderLayout());
        p1.add(new JLabel(rb.getString(label1)), BorderLayout.WEST);
        p.add(p1);
        JPanel p2 = new JPanel();
        p2.setLayout(new BorderLayout());
        p2.add(new JLabel(rb.getString(label2)), BorderLayout.WEST);
        p.add(p2);
        p.setMaximumSize(p.getPreferredSize());
        this.add(p);
    }

    public void init(boolean treeDnD) {
        _model =new DefaultTreeModel(new CatalogTreeNode("mainRoot"));
        if (treeDnD){   // index editor (right pane)
            _dTree = new DropJTree(_model);
            _noDrag = true;
        } else {    // Catalog (left pane index editor or all icon editors)
            _dTree = new JTree(_model);
            _noDrag = false;
        }
        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        renderer.setLeafIcon(renderer.getClosedIcon());
        _dTree.setCellRenderer(renderer);
        _dTree.setRootVisible(false);
        _dTree.setShowsRootHandles(true);
        _dTree.setScrollsOnExpand(true);
        //_dTree.setDropMode(DropMode.ON);
        _dTree.getSelectionModel().setSelectionMode(DefaultTreeSelectionModel.SINGLE_TREE_SELECTION);

        _dTree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                if (!_dTree.isSelectionEmpty() && _dTree.getSelectionPath()!=null ) {
                    try {
                        _previewLabel.setText(setIcons());
                    } catch (OutOfMemoryError oome) {
                        resetPanel();
                        if (log.isDebugEnabled()) log.debug("setIcons threw OutOfMemoryError "+oome);
                    }
                } else {
                    _previewLabel.setText(" ");
                }
            }
        });
        jmri.util.JTreeUtil.setExpandsSelectedPaths(_dTree, true);
        _treePane = new JScrollPane(_dTree);
        _treePane.getViewport().setPreferredSize(new Dimension(250, 200));
        add(_treePane, 1);
        setupPanel();
    }

    /**
    * Create a new model and add it to the main root
    */
    public void createNewBranch(String systemName, String userName, String path) {

        CatalogTreeManager manager = InstanceManager.catalogTreeManagerInstance();
        CatalogTree tree = manager.getBySystemName(systemName);
        if (tree == null){
            tree = manager.newCatalogTree(systemName, userName);
            tree.insertNodes(path);
        }
        addTree(tree);
    }

    /**
    *  Extend the Catalog by adding a tree to the root.
    */
    public void addTree(CatalogTree tree) {
        String name = tree.getSystemName();
        for (int i=0; i<_branchModel.size(); i++) {
            if (name.equals(_branchModel.get(i).getSystemName())) {
                return;
            }
        }
        addTreeBranch((CatalogTreeNode)tree.getRoot());
        _branchModel.add(tree);
        _model.reload();
    }

    /**
    * Recursively add the branch nodes to display tree
    */
    @SuppressWarnings("unchecked")
	private void addTreeBranch(CatalogTreeNode node) {
        if (log.isDebugEnabled()) {
            log.debug("addTreeBranch called for node= "+node.toString()+
                      ", has "+node.getChildCount()+" children");
        }
        //String name = node.toString(); 
        CatalogTreeNode root = (CatalogTreeNode)_model.getRoot();
        Enumeration<CatalogTreeNode> e = node.children();
        while (e.hasMoreElements()) {
            CatalogTreeNode n = e.nextElement();
            addNode(root, n);
        }
    }

    /**
    * Clones the node and adds to parent. 
    */
    @SuppressWarnings("unchecked")
	private void addNode(CatalogTreeNode parent, CatalogTreeNode n) {
        CatalogTreeNode node = new CatalogTreeNode((String)n.getUserObject());
        node.setLeaves(n.getLeaves());
        parent.add(node);
        Enumeration<CatalogTreeNode> e = n.children();
        while (e.hasMoreElements()) {
            CatalogTreeNode nChild = e.nextElement();
            addNode(node, nChild);
        }
    }

    /**
    * The tree held in the CatalogTreeManager must be kept in sync with the
    * tree displayed as the Image Index.  Required in order to save
    * the Index to disc.
    */
    private CatalogTreeNode getCorrespondingNode(CatalogTreeNode node) {
        TreeNode[] nodes = node.getPath();
        CatalogTreeNode cNode = null;
        for (int i=0; i<_branchModel.size(); i++) {
            CatalogTreeNode cRoot = (CatalogTreeNode)_branchModel.get(i).getRoot();
            cNode = match(cRoot, nodes, 1);
            if (cNode != null)  {
                break;
            }
        }
        return cNode;
    }

    /**
    *  Find the corresponding node in a CatalogTreeManager tree with a
    * displayed node.
    */
    @SuppressWarnings("unchecked")
	private CatalogTreeNode match(CatalogTreeNode cRoot, TreeNode[] nodes, int idx) {
        if (idx == nodes.length) {
            return cRoot;
        }
        Enumeration<CatalogTreeNode> e = cRoot.children();
        CatalogTreeNode result = null;
        while (e.hasMoreElements()) {
            CatalogTreeNode cNode = e.nextElement();
            if (nodes[idx].toString().equals(cNode.toString())) {
                result = match(cNode, nodes, idx+1);
                break;
            }
        }
        return result;
    }
    
    /**
    *  Find the corresponding CatalogTreeManager tree to the displayed
    * branch
    */
    private CatalogTree getCorespondingModel(CatalogTreeNode node) {
        TreeNode[] nodes = node.getPath();
        CatalogTree model = null;
        for (int i=0; i<_branchModel.size(); i++) {
            model = _branchModel.get(i);
            CatalogTreeNode cRoot = (CatalogTreeNode)model.getRoot();
            if (match(cRoot, nodes, 1) != null) {
                break;
            }
        }
        return model;
    }

    /**
    *  Insert a new node into the displayed tree.
    */
    @SuppressWarnings("unchecked")
	public boolean insertNodeIntoModel(String name, CatalogTreeNode parent) {
        if (!nameOK(parent, name)) {
            return false;
        }
        int index = 0;
        Enumeration<CatalogTreeNode> e = parent.children();
        while (e.hasMoreElements()) {
            CatalogTreeNode n = e.nextElement();
            if (name.compareTo(n.toString()) < 0 ) {
                break;
            }
            index++;
        }
        CatalogTreeNode newChild = new CatalogTreeNode(name);
        _model.insertNodeInto(newChild, parent, index);
        CatalogTreeNode cParent = getCorrespondingNode(parent);
        CatalogTreeNode node = new CatalogTreeNode(name);
        AbstractCatalogTree tree = (AbstractCatalogTree)getCorespondingModel(parent);
        tree.insertNodeInto(node, cParent, index);
        ImageIndexEditor._indexChanged = true;
        return true;
    }

    /**
    *  Delete a node from the displayed tree.
    */
    public void removeNodeFromModel(CatalogTreeNode node) {
        AbstractCatalogTree tree = (AbstractCatalogTree)getCorespondingModel(node);
        tree.removeNodeFromParent(getCorrespondingNode(node));
        _model.removeNodeFromParent(node);
        ImageIndexEditor._indexChanged = true;
    }

    /**
    * Make a change to a node in the displayed tree. Either its name
    * or the contents of its leaves (image references)
    */
    public boolean NodeChange(CatalogTreeNode node, String name) {
        CatalogTreeNode cNode = getCorrespondingNode(node);
        cNode.setLeaves(node.getLeaves());
        AbstractCatalogTree tree = (AbstractCatalogTree)getCorespondingModel(node);

        cNode.setUserObject(name);
        node.setUserObject(name);
        tree.nodeChanged(cNode);
        _model.nodeChanged(node);
        ImageIndexEditor._indexChanged = true;
        return true;
    }

    /**
    *  Node names in the path to the root must be unique
    */
    private boolean nameOK(CatalogTreeNode node, String name) {
        TreeNode[] nodes = node.getPath();
        for (int i=0; i<nodes.length; i++) {
            if (name.equals(nodes[i].toString())) {
                return false;
            }
        }
        return true;
    }

    /**
    *  Only call when log.isDebugEnabled() is true
    *
    public void enumerateTree() {
        CatalogTreeNode root = (CatalogTreeNode)_model.getRoot();
        log.debug("enumerateTree called for root= "+root.toString()+
                      ", has "+root.getChildCount()+" children");
        Enumeration e =root.depthFirstEnumeration();
        while (e.hasMoreElements()) {
            CatalogTreeNode n = (CatalogTreeNode)e.nextElement();
            log.debug("nodeName= "+n.getUserObject()+" has "+n.getLeaves().size()+" leaves.");
        }
    }
    */
    /**
    * Setup a display pane for a tree that shows only directory nodes (no file leaves)
    * The leaves (icon images) will be displayed in this panel.
    */
    private void setupPanel() {
        JPanel previewPanel = new JPanel();
        previewPanel.setLayout(new BoxLayout(previewPanel, BoxLayout.Y_AXIS));
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(_previewLabel);
        previewPanel.add(p);
        _preview = new JPanel();
        JScrollPane js = new JScrollPane(_preview);                       
        previewPanel.add(js);
        //_preview.setMinimumSize(new Dimension(250, 200));
        JRadioButton whiteButton = new JRadioButton(rb.getString("white"),false);
        JRadioButton grayButton = new JRadioButton(rb.getString("lightGray"),true);
        JRadioButton darkButton = new JRadioButton(rb.getString("darkGray"),false);
        whiteButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    setBackGround(Color.white);
                }
            });
        grayButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    setBackGround(_grayColor);
                }
            });
        darkButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    setBackGround(new Color(150,150,150));
                }
            });
        JPanel backgroundPanel = new JPanel(); 
        backgroundPanel.setLayout(new BoxLayout(backgroundPanel, BoxLayout.Y_AXIS));
        JPanel pp = new JPanel();
        pp.add(new JLabel(rb.getString("setBackground")));
        backgroundPanel.add(pp);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        ButtonGroup selGroup = new ButtonGroup();
        selGroup.add(whiteButton);
        selGroup.add(grayButton);
        selGroup.add(darkButton);
        buttonPanel.add(whiteButton);
        buttonPanel.add(grayButton);
        buttonPanel.add(darkButton);
        backgroundPanel.add(buttonPanel);
        backgroundPanel.setMaximumSize(backgroundPanel.getPreferredSize());
        previewPanel.add(backgroundPanel);
        add(previewPanel);
    }

    private void setBackGround(Color color) {
        _preview.setBackground(color);
        _currentBackground = color;
        Component[] comp = _preview.getComponents();
        for (int i=0; i<comp.length; i++){
            JLabel l = null;
            if (comp[i].getClass().getName().equals("javax.swing.JPanel")) {
                JPanel p = (JPanel)comp[i];
                p.setBackground(color);
                l = (JLabel)p.getComponent(0);
            } else if (comp[i].getClass().getName().equals("javax.swing.JLabel")) {
                l = (JLabel)comp[i];
            } else {
                if (log.isDebugEnabled()) log.debug("setBackGround label #"+i+
                                                    ", class= "+comp[i].getClass().getName());
                return;
            }
            l.setBackground(color);
        }
        //setSelectionBackground(Color.cyan); Save for use as alternative to DnD.
        _preview.invalidate();
    }

    /**
    * Save for use as alternative icon change method to DnD.
    *
    private void setSelectionBackground(Color color) {
        if (_selectedImage != null) {
            _selectedImage.getComponent(0).setBackground(color);
            _selectedImage.getComponent(1).setBackground(color);
            _selectedImage.setBackground(color);
        }
    } */

    private void resetPanel() {
        _selectedImage = null;
        if (_preview == null) {
            return;
        }
        Component[] comp = _preview.getComponents();
        for (int i=0; i<comp.length; i++) {
            comp[i].removeMouseListener(this);
        }
        _preview.removeAll();
        _preview.setBackground(_currentBackground);
        _preview.repaint();
    }

    /**
    *  Display the icons in the preview panel
    */
    private String setIcons() throws OutOfMemoryError {
        resetPanel();
        CatalogTreeNode node = getSelectedNode();
        if (node == null) {
            return null;
        }
        List<CatalogTreeLeaf> leaves = node.getLeaves();
        if (leaves == null) {
            return null;
        }
        int cellHeight = 0;
        int cellWidth = 0;
        int numCol = 1;
        while (numCol*numCol <leaves.size()) {
            numCol++;
        }
        if (numCol > 1) {
            numCol--;
        }
        int numRow = leaves.size()/numCol;
        int cnt = 0;
        boolean newCol = false;
        boolean noMemory = false;
        GridBagLayout gridbag = new GridBagLayout();
        _preview.setLayout(gridbag);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.CENTER;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.gridy = 0;
        c.gridx = -1;
        for (int i=0; i<leaves.size(); i++) {
            if (noMemory) {
                cnt++;
                continue;
            }
            CatalogTreeLeaf leaf = leaves.get(i);
            NamedIcon icon = new NamedIcon(leaf.getPath(), leaf.getName());
            int w = icon.getIconWidth();
            int h = icon.getIconHeight();
            if (log.isDebugEnabled()) {
                log.debug("Node= "+node.getUserObject()+", leaf path= "+leaf.getPath()+
                          ", w= "+w+", h= "+h);
            }
            if (3*w*h > 500000)  {
                //byte[] memoryTest = null;
                try {
                	// not sure if we really need to assign the byte array to memoryTest
                	@SuppressWarnings("unused")
					byte[] memoryTest = new byte[3*w*h];
                } catch (OutOfMemoryError me) {
                     log.debug("OutOfMemoryError for "+3*w*h+" bytes");
                     //memoryTest = null;
                     noMemory = true;
                     JOptionPane.showMessageDialog(this, java.text.MessageFormat.format(
                                                        rb.getString("OutOfMemory"), 
                                                        new Object[] {new Integer(cnt)}),
                                                        rb.getString("error"), 
                                                        JOptionPane.INFORMATION_MESSAGE);
                     continue;
                }
                //memoryTest = null;
                System.gc();        // please take the hint...
            }
            double scale = 1;
            if (w > 100) {
                scale = 100.0/w;
            }
            if (h > 100) {
                scale = Math.min(scale, 100.0/h);
            }
            if (scale < 1) { // make a thumbnail
                scale = Math.max(scale, 0.15);  // but not too small
                AffineTransform t = AffineTransform.getScaleInstance(scale, scale);
                BufferedImage bufIm = new BufferedImage((int)Math.ceil(scale*w), 
                                                        (int)Math.ceil(scale*h), 
                                                        BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = bufIm.createGraphics();
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, 
                                     RenderingHints.VALUE_RENDER_QUALITY); 
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                                     RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                                     RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2d.drawImage(icon.getImage(), t, null);
                icon.setImage(bufIm);
                g2d.dispose();
            }
            if (c.gridx < numCol) {
                c.gridx++;
            } else if (c.gridy < numRow) { //start next row
                c.gridy++;
                if (!newCol) {
                    c.gridx=0;
                }
            } else if (!newCol) { // start new column
                c.gridx++;
                numCol++;
                c.gridy = 0;
                newCol = true;
            } else {  // start new row
                c.gridy++;
                numRow++;
                c.gridx = 0;
                newCol = false;
            }
            JLabel image = null;
            c.insets = new Insets(5, 5, 0, 0);
            if (_noDrag) {
                image = new JLabel();
            } else {
                image = new DragJLabel();
            }
            image.setOpaque(true);
            image.setName(leaf.getName());
            image.setBackground(_currentBackground);
            image.setIcon(icon);
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.add(image);
            JLabel nameLabel = new JLabel(leaf.getName());
            p.add(nameLabel);
            JLabel label = new JLabel(java.text.MessageFormat.format(rb.getString("scale"),
                                new Object[] {printDbl(scale,2)}));
            p.add(label);
            if (cellHeight < icon.getIconHeight()) {
                cellHeight = icon.getIconHeight()
                                +label.getPreferredSize().height
                                +nameLabel.getPreferredSize().height;
            }
            if (cellWidth < icon.getIconWidth()) {
                cellWidth = Math.max(nameLabel.getPreferredSize().width,
                                Math.max(label.getPreferredSize().width, icon.getIconWidth()))+10;
            }
            if (_noDrag) {
                p.addMouseListener(this);
            }
            gridbag.setConstraints(p, c);
            _preview.add(p);
            if (log.isDebugEnabled()) {
                log.debug(leaf.getName()+" inserted at ("+c.gridx+", "+c.gridy+
                              ") w= "+icon.getIconWidth()+", h= "+icon.getIconHeight());
            }
            cnt++;
        }
        c.gridy++;
        c.gridx++;
        JLabel bottom = new JLabel();
        gridbag.setConstraints(bottom, c);
        _preview.add(bottom);
        _preview.setPreferredSize(new java.awt.Dimension(numCol*cellWidth, numRow*cellHeight));

        IconAdder.getParentFrame(this).pack();
        return java.text.MessageFormat.format(rb.getString("numImagesInNode"),
                              new Object[] {node.getUserObject(),new Integer(leaves.size())});
    }

    /**
    * Utility
    */
    public static String printDbl(double z, int decimalPlaces)
    {
        if (Double.isNaN(z) || decimalPlaces > 8)
        {
            return Double.toString(z);
        }
        else if (decimalPlaces <= 0)
        {
            return Integer.toString((int)Math.rint(z));
        }
        StringBuffer sb = new StringBuffer();
        if (z < 0) {
            sb.append('-');
        }
        z = Math.abs(z);
        int num = 1;
        int d = decimalPlaces;
        while (d-- > 0) {
            num *= 10;
        }
        int x = (int)Math.rint(z*num);
        int ix = x/num;                     // integer part
        int dx = x - ix*num;
        sb.append(ix);
        if (dx == 0) {
            return sb.toString();
        }
        if (decimalPlaces > 0)
        {
            sb.append('.');
            num /= 10;
            while (num > dx)
            {
                sb.append('0');
                num /= 10;
            }
            sb.append(dx);
        }
        return sb.toString() ;
    }
    
    /**
    * Return the node the user has selected.
    */
    public CatalogTreeNode getSelectedNode() {
        if (!_dTree.isSelectionEmpty() && _dTree.getSelectionPath()!=null ) {
            // somebody has been selected
            if (log.isDebugEnabled()) log.debug("getSelectedNode with "+
                                                _dTree.getSelectionPath().toString());
            TreePath path = _dTree.getSelectionPath();
            return (CatalogTreeNode)path.getLastPathComponent();
        }
        return null;
    }

    void delete(NamedIcon icon) {
        CatalogTreeNode node = getSelectedNode();
        node.deleteLeaf(icon.getName(), icon.getURL());
    }

    void rename(NamedIcon icon) {
        String name = JOptionPane.showInputDialog(IconAdder.getParentFrame(this), 
                                              rb.getString("newIconName"), icon.getName(),
                                              JOptionPane.QUESTION_MESSAGE);
        if (name != null && name.length() > 0) {
            CatalogTreeNode node = getSelectedNode();
            CatalogTreeLeaf leaf = node.getLeaf(icon.getName(), icon.getURL());
            if (leaf != null) {
                leaf.setName(name);
            }
            IconAdder.getParentFrame(this).invalidate();
        }
    }

    /**
    *  Return the icon selected in the preview panel
    *  Save this code in case there is a need to use an alternative
    *  icon changing method rather than DnD.
    *
    public NamedIcon getSelectedIcon() {
        if (_selectedImage != null) {
            JLabel l = (JLabel)_selectedImage.getComponent(0);
            // deselect
            //setSelectionBackground(_currentBackground); Save for use as alternative to DnD.
            _selectedImage = null;
            return (NamedIcon)l.getIcon();
        }
        return null;
    } */

    private void showPopUp(MouseEvent e, NamedIcon icon) {
        if (log.isDebugEnabled()) log.debug("showPopUp "+icon.toString());
        JPopupMenu popup = new JPopupMenu();                                    
        popup.add(new JMenuItem(icon.getName()));
        popup.add(new JMenuItem(icon.getURL()));
        popup.add(new javax.swing.JPopupMenu.Separator());

        popup.add(new AbstractAction(rb.getString("RenameIcon")) {
                NamedIcon icon;    
                public void actionPerformed(ActionEvent e) {
                    rename(icon);
                }
                AbstractAction init(NamedIcon i) {
                    icon = i;
                    return this;
                }
            }.init(icon));
        popup.add(new javax.swing.JPopupMenu.Separator());

        popup.add(new AbstractAction(rb.getString("DeleteIcon")) {
                NamedIcon icon;    
                public void actionPerformed(ActionEvent e) {
                    delete(icon);
                }
                AbstractAction init(NamedIcon i) {
                    icon = i;
                    return this;
                }
            }.init(icon));
        popup.show(e.getComponent(), e.getX(), e.getY());
    }


    public void mouseClicked(MouseEvent e) {
    }
    public void mouseEntered(MouseEvent e) {
    }
    public void mouseExited(MouseEvent e) {
    }
    public void mousePressed(MouseEvent e) {
    }
    public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
            Container con = (Container)e.getSource();
            JLabel label = (JLabel)con.getComponent(0);
            NamedIcon icon = (NamedIcon)label.getIcon();
            showPopUp(e, icon);
        } /* Save this code in case there is a need to use an alternative
            icon changing method to DnD.
        else {
            setSelectionBackground(_currentBackground);

            _selectedImage = (JPanel)e.getSource();
            setSelectionBackground(Color.cyan);
        } */
    }

    class DropJTree extends JTree implements DropTargetListener {
        DataFlavor dataFlavor;
        DropJTree (TreeModel model) {
            super (model);
            try {
                dataFlavor = new DataFlavor(ImageIndexEditor.IconDataFlavorMime);
            } catch (ClassNotFoundException cnfe) {
                cnfe.printStackTrace();
            }
            new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, this);
            //if (log.isDebugEnabled()) log.debug("DropJTree ctor");
        }
        public void dragExit(DropTargetEvent dte) {
            //if (log.isDebugEnabled()) log.debug("DropJTree.dragExit ");
        }
        public void dragEnter(DropTargetDragEvent dtde) {
            //if (log.isDebugEnabled()) log.debug("DropJTree.dragEnter ");
        }
        public void dragOver(DropTargetDragEvent dtde) {
            //if (log.isDebugEnabled()) log.debug("DropJTree.dragOver ");
        }
        public void dropActionChanged(DropTargetDragEvent dtde) {
            //if (log.isDebugEnabled()) log.debug("DropJTree.dropActionChanged ");
        }
        public void drop(DropTargetDropEvent e) {
            try {
                Transferable tr = e.getTransferable();
                if(e.isDataFlavorSupported(dataFlavor)) {
                    NamedIcon icon = (NamedIcon)tr.getTransferData(dataFlavor);
                    Point pt = e.getLocation();
                    if (log.isDebugEnabled()) 
                        log.debug("DropJTree.drop: Point= ("+pt.x+", "+pt.y+")");
                    TreePath path = _dTree.getPathForLocation(pt.x, pt.y);
                    if (path != null) {
                        CatalogTreeNode node = (CatalogTreeNode)path.getLastPathComponent();
                        e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                        node.addLeaf(icon.getName(), icon.getURL());
                        NodeChange(node, node.toString());
                        e.dropComplete(true);
                        if (log.isDebugEnabled()) 
                            log.debug("DropJTree.drop COMPLETED for "+icon.getURL());
                        return;
                    }
                }
            } catch(IOException ioe) {
                ioe.printStackTrace();
            } catch(UnsupportedFlavorException ufe) {
                ufe.printStackTrace();
            }
            if (log.isDebugEnabled()) log.debug("DropJTree.drop REJECTED!");
            e.rejectDrop();
        }
    }

    /**
     * Find the icon corresponding to a name. There are three cases:
     * <UL>
     * <LI> Starts with "resource:", treat the rest as a resource pathname
     *                  in the .jar file
     * <LI> Starts with "file:", treat the rest as an absolute file pathname
     *                  or as a relative path below the resource directory in the preferences directory
     * <LI> Otherwise, treat the name as a resource pathname in the .jar file
     * </UL>
     * @param pName The name string, possibly starting with file: or resource:
     * @return the desired icon with this same pName as its name.
     */
    static public NamedIcon getIconByName(String pName) {
        if (pName == null || pName.length() == 0) {
            return null;
        }
        if (pName.startsWith("resource:"))
            // return new NamedIcon(ClassLoader.getSystemResource(pName.substring(9)), pName);
            return new NamedIcon(pName.substring(9), pName);
        else if (pName.startsWith("file:")) {
            String fileName = pName.substring(5);
            
            // historically, absolute path names could be stored 
            // in the 'file' format.  Check for those, and
            // accept them if present
            if ((new File(fileName)).isAbsolute()) {
                if (log.isDebugEnabled()) log.debug("Load from absolute path: "+fileName);
                return new NamedIcon(fileName, pName);
            }
            // assume this is a relative path from the
            // preferences directory
            fileName = XmlFile.userFileLocationDefault()+File.separator+"resources"+File.separator+fileName;
            if (log.isDebugEnabled()) log.debug("load from user preferences file: "+fileName);
            return new NamedIcon(fileName, pName);
        }
        // else return new NamedIcon(ClassLoader.getSystemResource(pName), pName);
        else return new NamedIcon(pName, pName);
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CatalogPanel.class.getName());
}

