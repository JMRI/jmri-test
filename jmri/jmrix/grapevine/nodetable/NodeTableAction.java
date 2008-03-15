// NodeTableAction.java

package jmri.jmrix.grapevine.nodetable;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a
 *       			NodeTableFrame object
 *
 * @author	Bob Jacobsen    Copyright (C) 2006, 2008
 * @version	$Revision: 1.1 $
 */
public class NodeTableAction extends AbstractAction {

	public NodeTableAction(String s) { super(s);}

    public NodeTableAction() {
        this("Configure Grapevine Nodes");
    }

    public void actionPerformed(ActionEvent e) {
        NodeTableFrame f = new NodeTableFrame();
        try {
            f.initComponents();
            }
        catch (Exception ex) {
            log.error("Exception: "+ex.toString());
            }
        f.setLocation(100,30);
        f.setVisible(true);
    }
   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NodeTableAction.class.getName());
}


/* @(#)NodeTableAction.java */
