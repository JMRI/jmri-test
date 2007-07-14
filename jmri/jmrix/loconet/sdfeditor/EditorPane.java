// EditorPane.java

package jmri.jmrix.loconet.sdfeditor;

import java.awt.FlowLayout;

import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.util.ResourceBundle;

import jmri.jmrix.loconet.sdf.*;

import java.util.List;

/**
 * Pane for editing Digitrax SDF files.
 *
 * The GUI consists of a tree of instructions on the left, 
 * and on the right an edit panel. The edit panel 
 * has a small detailed view of the instruction over
 * a larger detailed view.
 *
 * @author	    Bob Jacobsen   Copyright (C) 2007
 * @version	    $Revision: 1.4 $
 */
public class EditorPane extends javax.swing.JPanel implements TreeSelectionListener {

    // GUI member declarations
    static ResourceBundle res = ResourceBundle.getBundle("jmri.jmrix.loconet.sdfeditor.Editor");
        
    public EditorPane() {
        // start to configure GUI
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        // set up basic layout; order matters here
        add(newTree());
        add(new JSeparator());
        add(newEditPane());
    }
    
    JTree tree;
    DefaultMutableTreeNode topNode;
    
    JComponent newTree() {
        topNode = new DefaultMutableTreeNode("file");
        tree = new JTree(topNode);
        tree.setMinimumSize(new Dimension(250,600));
        tree.setPreferredSize(new Dimension(250,600));
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);        

        // Listen for when the selection changes.
        tree.addTreeSelectionListener(this);
        
        // install in scroll area
        JScrollPane treeView = new JScrollPane(tree);
        return treeView;
    }
            
    /**
     * Handle tree selection
     */
    public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                           tree.getLastSelectedPathComponent();
    
        if (node == null) return;
    
        SdfMacroEditor nodeInfo = (SdfMacroEditor)node.getUserObject();
        status.setText(nodeInfo.oneInstructionString());

    }
    
    JPanel newEditPane() {
        JPanel p = new JPanel();
        p.setMinimumSize(new Dimension(600,400));
        p.setPreferredSize(new Dimension(600,400));
        p.setMaximumSize(new Dimension(600,400));
        
        // layout is two vertical parts
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        
        // upper part of right window
        p.add(newInstructionPane());
        
        p.add(new JSeparator());
        
        // lower part of right window
        p.add(newDetailPane());
        
        return p;
    }
    
    MonitoringLabel status = new MonitoringLabel();
    
    JComponent newInstructionPane() {
        return status;
    }
    
    JPanel newDetailPane() {
        JPanel p = new JPanel();
        
        p.setLayout(new FlowLayout());
        p.add(new JLabel("Details: "));
        
        return p;
    }
    
    /**
     * Add the instructions to the tree
     */
    void addSdf(SdfBuffer buff) {
        DefaultMutableTreeNode newNode = null;
    
        // make the top elements at the top
        List ops = buff.getArray();
        for (int i=0; i<ops.size(); i++) {
            nestNodes(topNode, (SdfMacro)ops.get(i));
        }

        // don't show the top (single) node, 
        // do show all the ones right under that.
        tree.expandPath(new TreePath(topNode));
        tree.setRootVisible(false);

    }

    void nestNodes(DefaultMutableTreeNode parent, SdfMacro macro) {
        // put in the new topmost node
        DefaultMutableTreeNode newNode 
                = new DefaultMutableTreeNode(
                                    SdfMacroEditor.attachEditor(macro));
        parent.add(newNode);
        
        // recurse for kids
        List children = macro.getChildren();
        if (children == null) return;
        for (int i=0; i<children.size(); i++) {
            nestNodes(newNode, (SdfMacro)children.get(i));
        }
    }
    
    /**
     * Get rid of held resources
     */
    void dispose() {
    }


    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(EditorPane.class.getName());

}
