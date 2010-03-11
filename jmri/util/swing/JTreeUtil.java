// JTreeUtil.java

package jmri.util.swing;

import javax.swing.tree.*;
import java.io.File;
import org.jdom.*;

/**
 * Common utility methods for working with JTrees.
 * <P>
 * Chief among these is the loadTree method, for
 * creating a tree from an XML definition
 *
 * @author Bob Jacobsen  Copyright 2003, 2010
 * @version $Revision: 1.5 $
 */

public class JTreeUtil extends GuiUtilBase {

    /**
     * @param file XML file to be read and processed
     * @param wi WindowInterface to be passed to the nodes in the tree
     * @param context Blind context Object passed to the nodes in the tree
     */
    static public DefaultMutableTreeNode loadTree(File file, WindowInterface wi, Object context) {
        Element root = rootFromFile(file);

        return treeFromElement(root, wi, context);
    }
    
    /**
     * @param main Element to be processed
     * @param wi WindowInterface to be passed to the nodes in the tree
     * @param context Blind context Object passed to the nodes in the tree
     */
    static DefaultMutableTreeNode treeFromElement(Element main, WindowInterface wi, Object context) {
        String name = "<none>";
        Element e = main.getChild("name");
        if (e != null) name = e.getText();

        DefaultMutableTreeNode node = new DefaultMutableTreeNode(name);
        node.setUserObject(actionFromNode(main, wi, context));

        for (Object child : main.getChildren("node")) {
            node.add(treeFromElement((Element)child, wi, context));
        }
        return node;
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(JTreeUtil.class.getName());
}