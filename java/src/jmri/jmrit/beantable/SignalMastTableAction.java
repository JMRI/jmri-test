// SignalMastTableAction.java

package jmri.jmrit.beantable;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import jmri.util.com.sun.TableSorter;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a
 * SignalMastTable GUI.
 *
 * @author	Bob Jacobsen    Copyright (C) 2003, 2009, 2010
 * @version     $Revision$
 */

@ActionID(
        id = "jmri.jmrit.beantable.SignalMastTableAction",
        category = "Elements/Signals"
)
@ActionRegistration(
        iconBase = "org/jmri/core/ui/toolbar/generic.gif",
        displayName = "jmri.jmrit.Bundle#MenuItemSignalMastTable",
        iconInMenu = false
)
@ActionReference(
        path = "Menu/Tools/Tables/Signals",
        position = 610
)
public class SignalMastTableAction extends AbstractTableAction {

    /**
     * Create an action with a specific title.
     * <P>
     * Note that the argument is the Action title, not the title of the
     * resulting frame.  Perhaps this should be changed?
     * @param actionName
     */
    public SignalMastTableAction(String actionName) {
        super(actionName);
    }
    public SignalMastTableAction() { this("Signal Mast Table");}

    /**
     * Create the JTable DataModel, along with the changes
     * for the specific case of Sensors
     */
    protected void createModel() {
        m = new jmri.jmrit.beantable.signalmast.SignalMastTableDataModel();
    }

    protected void setTitle() {
        f.setTitle(f.rb.getString("TitleSignalMastTable"));
    }

    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.SignalMastTable";
    }

    jmri.jmrit.beantable.signalmast.AddSignalMastJFrame addFrame = null;

    // has to agree with number in SignalMastDataModel
    final static int VALUECOL = BeanTableDataModel.VALUECOL;
    final static int SYSNAMECOL = BeanTableDataModel.SYSNAMECOL;
    
    public void actionPerformed(ActionEvent e) {
        // create the JTable model, with changes for specific NamedBean
        createModel();
        TableSorter sorter = new TableSorter(m);
    	JTable dataTable = m.makeJTable(sorter);
        sorter.setTableHeader(dataTable.getTableHeader());
        // create the frame
        f = new BeanTableFrame(m, helpTarget(), dataTable){
    
            /**
             * Include an "add" button
             */
            void extras() {
                JButton addButton = new JButton(this.rb.getString("ButtonAdd"));
                addToBottomBox(addButton, this.getClass().getName());
                addButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        addPressed(e);
                    }
                });
            }

        };
        setTitle();
        addToFrame(f);
        f.pack();
        f.setVisible(true);
    }
    
    protected void addPressed(ActionEvent e) {
        if (addFrame==null) {
            addFrame = new jmri.jmrit.beantable.signalmast.AddSignalMastJFrame();
        } else {
            addFrame.refresh();
        }
        addFrame.setVisible(true);
    }
    
    public void setMenuBar(BeanTableFrame f){
        JMenuBar menuBar = f.getJMenuBar();
        JMenu pathMenu = new JMenu(rb.getString("Tools"));
        menuBar.add(pathMenu);
        JMenuItem item = new JMenuItem(rb.getString("MenuItemRepeaters"));
        pathMenu.add(item);
        item.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
                jmri.jmrit.beantable.signalmast.SignalMastRepeaterJFrame frame = new jmri.jmrit.beantable.signalmast.SignalMastRepeaterJFrame(); 
                frame.setVisible(true);
        	}
        });
    }

    static final Logger log = LoggerFactory.getLogger(SignalMastTableAction.class.getName());

    public static class MyComboBoxRenderer extends JComboBox implements TableCellRenderer {
        public MyComboBoxRenderer(Vector<String> items) {
            super(items);
        }
    
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setForeground(table.getSelectionForeground());
                super.setBackground(table.getSelectionBackground());
            } else {
                setForeground(table.getForeground());
                setBackground(table.getBackground());
            }
    
            // Select the current value
            setSelectedItem(value);
            return this;
        }
    }
    
    public static class MyComboBoxEditor extends DefaultCellEditor {
        public MyComboBoxEditor(Vector<String> items) {
            super(new JComboBox(items));
        }
    }
    
    protected String getClassName() { return SignalMastTableAction.class.getName(); }
    
    public String getClassDescription() { return rb.getString("TitleSignalGroupTable"); }
}


/* @(#)SignalMastTableAction.java */
