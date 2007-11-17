// MemoryTableAction.java

package jmri.jmrit.beantable;

import jmri.InstanceManager;
import jmri.Manager;
import jmri.NamedBean;
import jmri.Memory;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JTable;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jmri.util.JmriJFrame;

/**
 * Swing action to create and register a
 * MemoryTable GUI.
 *
 * @author	Bob Jacobsen    Copyright (C) 2003
 * @version     $Revision: 1.11 $
 */

public class MemoryTableAction extends AbstractTableAction {

    /**
     * Create an action with a specific title.
     * <P>
     * Note that the argument is the Action title, not the title of the
     * resulting frame.  Perhaps this should be changed?
     * @param actionName
     */
    public MemoryTableAction(String actionName) {
	super(actionName);

        // disable ourself if there is no primary Memory manager available
        if (jmri.InstanceManager.memoryManagerInstance()==null) {
            setEnabled(false);
        }

    }

    public MemoryTableAction() { this("Memory Table");}

    /**
     * Create the JTable DataModel, along with the changes
     * for the specific case of Memory objects
     */
    void createModel() {
        m = new BeanTableDataModel() {
            public String getValue(String name) {
            	Object m = InstanceManager.memoryManagerInstance().getBySystemName(name).getValue();
            	if (m!=null)
                	return m.toString();
                else
                	return "";
            }
            public Manager getManager() { return InstanceManager.memoryManagerInstance(); }
            public NamedBean getBySystemName(String name) { return InstanceManager.memoryManagerInstance().getBySystemName(name);}
            public NamedBean getByUserName(String name) { return InstanceManager.memoryManagerInstance().getByUserName(name);}

            public void clickOn(NamedBean t) {
            	// don't do anything on click; not used in this class, because 
            	// we override setValueAt
            }
    		public void setValueAt(Object value, int row, int col) {
        		if (col==VALUECOL) {
            		Memory t = (Memory)getBySystemName((String)sysNameList.get(row));
					t.setValue(value);
            		fireTableRowsUpdated(row,row);
        		} else super.setValueAt(value, row, col);
    		}
	   		public String getColumnName(int col) {
        		if (col==VALUECOL) return "Value";
        		return super.getColumnName(col);
        	}
    		public Class getColumnClass(int col) {
    			if (col==VALUECOL) return String.class;
    			else return super.getColumnClass(col);
		    }
    		public void configValueColumn(JTable table) {
        		// value column isn't button, so config is null
		    }
			boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
			    return true;
				// return (e.getPropertyName().indexOf("alue")>=0);
			}
			public JButton configureButton() {
				super.log.error("configureButton should not have been called");
				return null;
			}
        };
    }

    void setTitle() {
        f.setTitle(f.rb.getString("TitleMemoryTable"));
    }

    String helpTarget() {
        return "package.jmri.jmrit.beantable.MemoryTable";
    }

    JmriJFrame addFrame = null;
    JTextField sysName = new JTextField(5);
    JTextField userName = new JTextField(5);
    JLabel sysNameLabel = new JLabel(rb.getString("LabelSystemName"));
    JLabel userNameLabel = new JLabel(rb.getString("LabelUserName"));

    void addPressed(ActionEvent e) {
        if (addFrame==null) {
            addFrame = new JmriJFrame(rb.getString("TitleAddMemory"));
            addFrame.addHelpMenu("package.jmri.jmrit.beantable.MemoryAddEdit", true);
            addFrame.getContentPane().setLayout(new BoxLayout(addFrame.getContentPane(), BoxLayout.Y_AXIS));
            JPanel p;
            p = new JPanel(); p.setLayout(new FlowLayout());
            p.add(sysNameLabel);
            p.add(sysName);
            addFrame.getContentPane().add(p);

            p = new JPanel(); p.setLayout(new FlowLayout());
            p.add(userNameLabel);
            p.add(userName);
            addFrame.getContentPane().add(p);

            JButton ok;
            addFrame.getContentPane().add(ok = new JButton(rb.getString("ButtonOK")));
            ok.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    okPressed(e);
                }
            });
        }
        addFrame.pack();
        addFrame.setVisible(true);
    }

    void okPressed(ActionEvent e) {
        String user = userName.getText();
        if (user.equals("")) user=null;
        String sName = sysName.getText().toUpperCase();
        InstanceManager.memoryManagerInstance().newMemory(sName, user);
    }
    private boolean noWarn = false;

    static final org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(MemoryTableAction.class.getName());
}

/* @(#)MemoryTableAction.java */
