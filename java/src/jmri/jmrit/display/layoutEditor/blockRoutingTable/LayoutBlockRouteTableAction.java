package jmri.jmrit.display.layoutEditor.blockRoutingTable;


import jmri.jmrit.display.layoutEditor.*;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

import java.util.ResourceBundle;

    /**
     * Swing action to create and register a Block Routing Table.
     * <P>
     * @author	Kevin Dickerson   Copyright (C) 2011
     * @version	$Revision$
     */

public class LayoutBlockRouteTableAction extends AbstractAction {

    /**
     * Create an action with a specific title.
     * <P>
     * Note that the argument is the Action title, not the title of the
     * resulting frame.  Perhaps this should be changed?
     * @param s
     */

   public LayoutBlockRouteTableAction(String s, LayoutBlock lBlock) {
        super(s);
        this.lBlock = lBlock;
    }

    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.layoutEditor.LayoutEditorBundle");
    
   LayoutBlock lBlock;
        
    LayoutBlockRouteTable m;
    //LayoutBlockNeighbourTable mn;
    jmri.util.JmriJFrame f;
    void createModel() {
    
        m = new LayoutBlockRouteTable(false, lBlock);

    }

        public void actionPerformed() {
        // create the JTable model, with changes for specific NamedBean
        createModel();

        // create the frame
        f = new jmri.util.JmriJFrame(){            
        };
        f.add(m);
        setTitle();
        f.pack();
        f.setVisible(true);
    }
    

    public void actionPerformed(ActionEvent e) {
        actionPerformed();
    }
    
    void setTitle() {
        if (lBlock!=null)
            f.setTitle(rb.getString("BlockRoutingTableTitle") + " " + lBlock.getDisplayName());
        else
            f.setTitle(rb.getString("BlockRoutingTableTitleShort"));
    }
    
    String helpTarget() {
        return "package.jmri.jmrit.display.layoutEditor";
    }
        
}