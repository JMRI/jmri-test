// CatalogTreeNode.java

package jmri.jmrit.catalog;

import java.util.ArrayList;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Node of a CatalogTree.
 * <P>
 * Name for the node
 * Path is info needed for leaves.
 *
 * @author			Pete Cressman  Copyright 2009
 *
 */
public class CatalogTreeNode extends DefaultMutableTreeNode {

    // Sorted by height for ease of display in CatalogPanel
    private ArrayList <CatalogTreeLeaf> _leaves = new ArrayList<CatalogTreeLeaf>();

    public CatalogTreeNode(String name) {
        super(name);
    }

    /**
    *  Append leaf to the end of the leaves list
    */
    public void addLeaf(CatalogTreeLeaf leaf) {
        _leaves.add(leaf);
    }

    /**
    * Insert leaf according to height.
    */
    public void addLeaf(String name, String path) {
       // check path
       NamedIcon icon = CatalogPanel.getIconByName(path);
       if (icon == null){
           log.warn("path \""+path+"\" is not a NamedIcon.");
           return;
       }
       int h = icon.getIconHeight();
       for (int i=0; i<_leaves.size(); i++) {
           CatalogTreeLeaf leaf = _leaves.get(i);
           if (h > leaf.getSize()) {
               _leaves.add(i, new CatalogTreeLeaf(name, path, h));
               return;
           }
       }
       _leaves.add(new CatalogTreeLeaf(name, path, h));
   }
   public void deleteLeaf(String name, String path) {
       for (int i=0; i<_leaves.size(); i++) {
           CatalogTreeLeaf leaf = _leaves.get(i);
           if (name.equals(leaf.getName()) && path.equals(leaf.getPath())) {
               _leaves.remove(i);
               return;
           }
       }
   }
   public CatalogTreeLeaf getLeaf(String name, String path) {
       for (int i=0; i<_leaves.size(); i++) {
           CatalogTreeLeaf leaf = _leaves.get(i);
           if (name.equals(leaf.getName()) && path.equals(leaf.getPath())) {
               return leaf;
           }
       }
       return null;
   }
   public ArrayList <CatalogTreeLeaf> getLeaves() {
       return _leaves;
   }
   public int getNumLeaves() {
       return _leaves.size();
   }
   public void setLeaves(ArrayList <CatalogTreeLeaf> leaves) {
       _leaves = leaves;
   }

   static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CatalogTreeNode.class.getName());
}

