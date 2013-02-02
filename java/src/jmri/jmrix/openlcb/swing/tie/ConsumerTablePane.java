// ConsumerTablePane.java

package jmri.jmrix.openlcb.swing.tie;

import org.apache.log4j.Logger;
import java.util.ResourceBundle;

import javax.swing.*;
import javax.swing.table.*;

/**
 * Pane for showing the consumer table
 * @author	 Bob Jacobsen 2008
 * @version	 $Revision$
 * @since 2.3.7
 */
public class ConsumerTablePane extends JPanel {

    static    ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.openlcb.swing.tie.TieBundle");
	
	protected JTable table = null;
	protected TableModel tableModel = null;
							
    public void initComponents() throws Exception {

        // set the frame's initial state
        setSize(500,300);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));


        tableModel = new ConsumerTableModel();        
        table = jmri.util.JTableUtil.sortableDataModel(tableModel);
        table.setRowSelectionAllowed(true);
        table.setPreferredScrollableViewportSize(new java.awt.Dimension(300,350));

        TableColumnModel columnModel = table.getColumnModel();
        TableColumn column;
        column = columnModel.getColumn(ConsumerTableModel.USERNAME_COLUMN);
        column.setMinWidth(20);
        //column.setMaxWidth(40);
        column.setResizable(true);
        column = columnModel.getColumn(ConsumerTableModel.NODE_COLUMN);
        column.setMinWidth(40);
        //column.setMaxWidth(85);
        column.setResizable(true);
        column = columnModel.getColumn(ConsumerTableModel.NUMBER_COLUMN);
        column.setMinWidth(75);
        //column.setMaxWidth(100);
        column.setResizable(true);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane);
        
    }


    // for Print button support, see jmri.jmrix.cmri.serial.assignment.ListFrame

    static Logger log = Logger.getLogger(ConsumerTablePane.class.getName());
	
}

/* @(#)ConsumerTablePane.java */
