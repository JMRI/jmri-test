// CatalogTreeFS.java

package jmri.jmrit.catalog;

import jmri.CatalogTree;
import jmri.jmrit.XmlFile;
import java.io.File;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;

/**
 * TreeModel used by CatalogPanel to create a tree of resources.
 * <P>
 * Source of the tree content is the file system. Only directories are
 * included in the tree.  A filter can be set
 * to extract particular file types.
 *
 * @author			Pete Cressman  Copyright 2009
 *
 */
public class CatalogTreeFS extends AbstractCatalogTree {

    String[] _filter;

    public CatalogTreeFS(String sysName, String userName) {
        super(sysName, userName);
    }

    public void setFilter(String[] filter) {
        _filter = filter;
    }

    public String[] getFilter() {
        return _filter;
    }

    boolean filter(String ext) {
        if (ext == null) return false;
        if (_filter == null || _filter.length == 0) return true;
        for (int i=0; i<_filter.length; i++) {
            if (ext.equals(_filter[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Recursively add nodes to the tree
     * @param pName Name of the resource to be scanned; this
     *              is only used for the human-readable tree
     * @param pPath Path to this resource, including the pName part
     * @param pParent Node for the parent of the resource to be scanned, e.g.
     *              where in the tree to insert it.
     */
    public void insertNodes(String pName, String pPath, CatalogTreeNode pParent) {
        File fp = new File(pPath);
        if (!fp.exists()) return;

        // suppress overhead files
        String filename = fp.getName();
        if (filename.startsWith(".")) return;
        if (filename.equals("CVS")) return;

        if (fp.isDirectory()) {
            // first, represent this one
            CatalogTreeNode newElement = new CatalogTreeNode(pName);
            insertNodeInto(newElement, pParent, pParent.getChildCount());
            String[] sp = fp.list();
            for (int i=0; i<sp.length; i++) {
                if (log.isDebugEnabled()) log.debug("Descend into resource: "+sp[i]);
                insertNodes(sp[i], pPath+"/"+sp[i], newElement);
            }
        } else /* leaf */ {
            String ext = jmri.util.FileChooserFilter.getFileExtension(fp);
            if (!filter(ext)) return;
            pParent.addLeaf(filename, pPath); 
        }
    }
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CatalogTreeFS.class.getName());
}

