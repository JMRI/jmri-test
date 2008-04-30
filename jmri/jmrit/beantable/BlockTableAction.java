// BlockTableAction.java

package jmri.jmrit.beantable;

import jmri.InstanceManager;
import jmri.Manager;
import jmri.NamedBean;
import jmri.Block;
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
 * BlockTable GUI.
 *
 * @author	Bob Jacobsen    Copyright (C) 2003, 2008
 * @version     $Revision: 1.5 $
 */

public class BlockTableAction extends AbstractTableAction {

    /**
     * Create an action with a specific title.
     * <P>
     * Note that the argument is the Action title, not the title of the
     * resulting frame.  Perhaps this should be changed?
     * @param actionName
     */
    public BlockTableAction(String actionName) {
	super(actionName);

        // disable ourself if there is no primary Block manager available
        if (jmri.InstanceManager.blockManagerInstance()==null) {
            setEnabled(false);
        }

    }

    public BlockTableAction() { this("Block Table");}

    /**
     * Create the JTable DataModel, along with the changes
     * for the specific case of Block objects
     */
    void createModel() {
        m = new BeanTableDataModel() {

        	static public final int DIRECTIONCOL = NUMCOLUMN;

        	public String getValue(String name) {
        		if (name == null) {
        			super.log.warn("requested getValue(null)");
        			return "(no name)";
        		}
        		Block b = InstanceManager.blockManagerInstance().getBySystemName(name);
        		if (b == null) {
        			super.log.debug("requested getValue(\""+name+"\"), Block doesn't exist");
        			return "(no Block)";
        		}
        		Object m = b.getValue();
            	if (m!=null)
                	return m.toString();
                else
                	return "";
            }
            public Manager getManager() { return InstanceManager.blockManagerInstance(); }
            public NamedBean getBySystemName(String name) { return InstanceManager.blockManagerInstance().getBySystemName(name);}
            public NamedBean getByUserName(String name) { return InstanceManager.blockManagerInstance().getByUserName(name);}

            public void clickOn(NamedBean t) {
            	// don't do anything on click; not used in this class, because 
            	// we override setValueAt
            }

    		public int getColumnCount(){ 
    		    return DIRECTIONCOL+1;
     		}

    		public Object getValueAt(int row, int col) {
	   			if (col==DIRECTIONCOL) {
            		Block b = (Block)getBySystemName((String)sysNameList.get(row));
                    if (b == null) {
                        super.log.debug("requested getValueAt(\""+row+"\"), Block doesn't exist");
                        return "(no Block)";
                    }
					return jmri.Path.decodeDirection(b.getDirection());
    			} else return super.getValueAt(row, col);
			}    		

    		public void setValueAt(Object value, int row, int col) {
        		if (col==VALUECOL) {
            		Block b = (Block)getBySystemName((String)sysNameList.get(row));
					b.setValue(value);
            		fireTableRowsUpdated(row,row);
        		} else super.setValueAt(value, row, col);
    		}

	   		public String getColumnName(int col) {
        		if (col==DIRECTIONCOL) return "Direction";
        		if (col==VALUECOL) return "Value";
        		return super.getColumnName(col);
        	}

    		public Class getColumnClass(int col) {
    			if (col==DIRECTIONCOL) return String.class;
    			if (col==VALUECOL) return String.class;  // not a button
    			else return super.getColumnClass(col);
		    }

    		public int getPreferredWidth(int col) {
    			if (col==DIRECTIONCOL) return new JTextField(7).getPreferredSize().width;
    			else return super.getPreferredWidth(col);
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
        f.setTitle(f.rb.getString("TitleBlockTable"));
    }

    String helpTarget() {
        return "package.jmri.jmrit.beantable.BlockTable";
    }

    JmriJFrame addFrame = null;
    JTextField sysName = new JTextField(5);
    JTextField userName = new JTextField(5);
    JLabel sysNameLabel = new JLabel(rb.getString("LabelSystemName"));
    JLabel userNameLabel = new JLabel(rb.getString("LabelUserName"));

    void addPressed(ActionEvent e) {
        if (addFrame==null) {
            addFrame = new JmriJFrame(rb.getString("TitleAddBlock"));
            addFrame.addHelpMenu("package.jmri.jmrit.beantable.BlockAddEdit", true);
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
        InstanceManager.blockManagerInstance().createNewBlock(sName, user);
    }
    private boolean noWarn = false;

    static final org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(BlockTableAction.class.getName());
}

/* @(#)BlockTableAction.java */
