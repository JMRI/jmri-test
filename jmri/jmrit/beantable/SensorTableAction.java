// SensorTableAction.java

package jmri.jmrit.beantable;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Manager;
import jmri.NamedBean;
import jmri.Sensor;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jmri.util.JmriJFrame;

/**
 * Swing action to create and register a
 * SensorTable GUI.
 *
 * @author	Bob Jacobsen    Copyright (C) 2003
 * @version     $Revision: 1.22 $
 */

public class SensorTableAction extends AbstractTableAction {

    /**
     * Create an action with a specific title.
     * <P>
     * Note that the argument is the Action title, not the title of the
     * resulting frame.  Perhaps this should be changed?
     * @param actionName
     */
    public SensorTableAction(String actionName) {
        super(actionName);

        // disable ourself if there is no primary sensor manager available
        if (jmri.InstanceManager.sensorManagerInstance()==null ||
            (((jmri.managers.AbstractProxyManager)jmri.InstanceManager
                                                 .sensorManagerInstance())
                                                 .systemLetter()=='\0')) {
            setEnabled(false);
        }
    }
    public SensorTableAction() { this("Sensor Table");}

    /**
     * Create the JTable DataModel, along with the changes
     * for the specific case of Sensors
     */
    void createModel() {
        m = new BeanTableDataModel() {
		    static public final int INVERTCOL = NUMCOLUMN;
		    
            public String getValue(String name) {
                int val = InstanceManager.sensorManagerInstance().getBySystemName(name).getKnownState();
                switch (val) {
                case Sensor.ACTIVE: return rbean.getString("SensorStateActive");
                case Sensor.INACTIVE: return rbean.getString("SensorStateInactive");
                case Sensor.UNKNOWN: return rbean.getString("BeanStateUnknown");
                case Sensor.INCONSISTENT: return rbean.getString("BeanStateInconsistent");
                default: return "Unexpected value: "+val;
                }
            }
            public Manager getManager() { return InstanceManager.sensorManagerInstance(); }
            public NamedBean getBySystemName(String name) { return InstanceManager.sensorManagerInstance().getBySystemName(name);}
            public NamedBean getByUserName(String name) { return InstanceManager.sensorManagerInstance().getByUserName(name);}
            public void clickOn(NamedBean t) {
                try {
                    int state = ((Sensor)t).getKnownState();
                    if (state==Sensor.INACTIVE) ((Sensor)t).setKnownState(Sensor.ACTIVE);
                    else ((Sensor)t).setKnownState(Sensor.INACTIVE);
                } catch (JmriException e) { this.log.warn("Error setting state: "+e); }
            }

    		public int getColumnCount( ){ 
    		    return NUMCOLUMN+1;
     		}

    		public String getColumnName(int col) {
    			if (col==INVERTCOL) return "Inverted";
    			else return super.getColumnName(col);
		    }
    		public Class<?> getColumnClass(int col) {
    			if (col==INVERTCOL) return Boolean.class;
    			else return super.getColumnClass(col);
		    }
    		public int getPreferredWidth(int col) {
    			if (col==INVERTCOL) return new JTextField(4).getPreferredSize().width;
    			else return super.getPreferredWidth(col);
		    }
    		public boolean isCellEditable(int row, int col) {
    			if (col==INVERTCOL) return true;
    			else return super.isCellEditable(row,col);
			}    		

    		public Object getValueAt(int row, int col) {
    			if (col==INVERTCOL) {
         			// some error checking
        			if (row >= sysNameList.size()){
        				log.debug("row is greater than name list");
        				return "";
        			}
    				String name = sysNameList.get(row);
    				boolean val = InstanceManager.sensorManagerInstance().getBySystemName(name).getInverted();
					return new Boolean(val);
    			} else return super.getValueAt(row, col);
			}    		
			
    		public void setValueAt(Object value, int row, int col) {
    			if (col==INVERTCOL) {
    				String name = sysNameList.get(row);
    				boolean b = ((Boolean)value).booleanValue();
    				InstanceManager.sensorManagerInstance().getBySystemName(name).setInverted(b);
    			} else super.setValueAt(value, row, col);
    		}
    		
            boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
                if (e.getPropertyName().indexOf("inverted")>=0) return true;
                else return super.matchPropertyName(e);
            }
        };
    }

    void setTitle() {
        f.setTitle(f.rb.getString("TitleSensorTable"));
    }

    String helpTarget() {
        return "package.jmri.jmrit.beantable.SensorTable";
    }

    JmriJFrame addFrame = null;
    JTextField sysName = new JTextField(5);
    JTextField userName = new JTextField(5);
    JLabel sysNameLabel = new JLabel(rb.getString("LabelSystemName"));
    JLabel userNameLabel = new JLabel(rb.getString("LabelUserName"));

    void addPressed(ActionEvent e) {
        if (addFrame==null) {
            addFrame = new JmriJFrame(rb.getString("TitleAddSensor"));
            addFrame.addHelpMenu("package.jmri.jmrit.beantable.SensorAddEdit", true);
            addFrame.getContentPane().setLayout(new BoxLayout(addFrame.getContentPane(), BoxLayout.Y_AXIS));
            JPanel p;
            p = new JPanel(); 
            p.setLayout(new FlowLayout());
            p.setLayout(new java.awt.GridBagLayout());
            java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
            c.gridwidth  = 1;
            c.gridheight = 1;
            c.gridx = 0;
            c.gridy = 0;
            c.anchor = java.awt.GridBagConstraints.EAST;
            p.add(sysNameLabel,c);
            c.gridy = 1;
            p.add(userNameLabel,c);
            c.gridx = 1;
            c.gridy = 0;
            c.anchor = java.awt.GridBagConstraints.WEST;
            c.weightx = 1.0;
            c.fill = java.awt.GridBagConstraints.HORIZONTAL;  // text field will expand
            p.add(sysName,c);
            c.gridy = 1;
            p.add(userName,c);
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
        InstanceManager.sensorManagerInstance().newSensor(sysName.getText().toUpperCase(), user);
    }

    static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SensorTableAction.class.getName());
}


/* @(#)SensorTableAction.java */
