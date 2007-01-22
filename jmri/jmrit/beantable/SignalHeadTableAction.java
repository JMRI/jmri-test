// SignalHeadTableAction.java

package jmri.jmrit.beantable;

import jmri.InstanceManager;
import jmri.Manager;
import jmri.NamedBean;
import jmri.SignalHead;
import jmri.Turnout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Swing action to create and register a
 * SignalHeadTable GUI.
 *
 * @author	Bob Jacobsen    Copyright (C) 2003,2006,2007
 * @version     $Revision: 1.19 $
 */

public class SignalHeadTableAction extends AbstractTableAction {

    /**
     * Create an action with a specific title.
     * <P>
     * Note that the argument is the Action title, not the title of the
     * resulting frame.  Perhaps this should be changed?
     * @param s
     */
    public SignalHeadTableAction(String s) {
        super(s);
        // disable ourself if there is no primary Signal Head manager available
        if (jmri.InstanceManager.signalHeadManagerInstance()==null) {
            setEnabled(false);
        }
    }
    public SignalHeadTableAction() { this("Signal Table");}

    /**
     * Create the JTable DataModel, along with the changes
     * for the specific case of SignalHeads
     */
    void createModel() {
        m = new BeanTableDataModel() {
		    static public final int LITCOL = 3;
		    static public final int HELDCOL = 4;
    		public int getColumnCount( ){ return NUMCOLUMN+2;}
    		public String getColumnName(int col) {
    			if (col==LITCOL) return "Lit";
    			else if (col==HELDCOL) return "Held";
    			else return super.getColumnName(col);
		    }
    		public Class getColumnClass(int col) {
    			if (col==LITCOL) return Boolean.class;
    			else if (col==HELDCOL) return Boolean.class;
    			else return super.getColumnClass(col);
		    }
    		public int getPreferredWidth(int col) {
    			if (col==LITCOL) return new JTextField(4).getPreferredSize().width;
    			else if (col==HELDCOL) return new JTextField(4).getPreferredSize().width;
    			else return super.getPreferredWidth(col);
		    }
    		public boolean isCellEditable(int row, int col) {
    			if (col==LITCOL) return true;
    			else if (col==HELDCOL) return true;
    			else return super.isCellEditable(row,col);
			}    		
    		public Object getValueAt(int row, int col) {
    			if (col==LITCOL) {
    				String name = (String)sysNameList.get(row);
    				boolean val = InstanceManager.signalHeadManagerInstance().getBySystemName(name).getLit();
					return new Boolean(val);
    			}
    			else if (col==HELDCOL) {
    				String name = (String)sysNameList.get(row);
    				boolean val = InstanceManager.signalHeadManagerInstance().getBySystemName(name).getHeld();
					return new Boolean(val);
    			}
				else return super.getValueAt(row, col);
			}    		
    		public void setValueAt(Object value, int row, int col) {
    			if (col==LITCOL) {
    				String name = (String)sysNameList.get(row);
    				boolean b = ((Boolean)value).booleanValue();
    				InstanceManager.signalHeadManagerInstance().getBySystemName(name).setLit(b);
    			}
    			else if (col==HELDCOL) {
    				String name = (String)sysNameList.get(row);
    				boolean b = ((Boolean)value).booleanValue();
    				InstanceManager.signalHeadManagerInstance().getBySystemName(name).setHeld(b);
    			}
    			else super.setValueAt(value, row, col);
    		}
    		
            public String getValue(String name) {
                int val = InstanceManager.signalHeadManagerInstance().getBySystemName(name).getAppearance();
                switch (val) {
                case SignalHead.RED: return rbean.getString("SignalHeadStateRed");
                case SignalHead.YELLOW: return rbean.getString("SignalHeadStateYellow");
                case SignalHead.GREEN: return rbean.getString("SignalHeadStateGreen");
                case SignalHead.FLASHRED: return rbean.getString("SignalHeadStateFlashingRed");
                case SignalHead.FLASHYELLOW: return rbean.getString("SignalHeadStateFlashingYellow");
                case SignalHead.FLASHGREEN: return rbean.getString("SignalHeadStateFlashingGreen");
                case SignalHead.DARK: return rbean.getString("SignalHeadStateDark");
                default: return "Unexpected value: "+val;
                }
            }
            public Manager getManager() { return InstanceManager.signalHeadManagerInstance(); }
            public NamedBean getBySystemName(String name) { return InstanceManager.signalHeadManagerInstance().getBySystemName(name);}
            public void clickOn(NamedBean t) {
                int oldState = ((SignalHead)t).getAppearance();
                int newState;
                switch (oldState) {
                case SignalHead.RED: newState = SignalHead.YELLOW; break;
                case SignalHead.YELLOW: newState = SignalHead.GREEN; break;
                case SignalHead.GREEN: newState = SignalHead.FLASHRED; break;
                case SignalHead.FLASHRED: newState = SignalHead.FLASHYELLOW; break;
                case SignalHead.FLASHYELLOW: newState = SignalHead.FLASHGREEN; break;
                case SignalHead.FLASHGREEN: newState = SignalHead.DARK; break;
                case SignalHead.DARK: newState = SignalHead.RED; break;
                default: newState = SignalHead.DARK; this.log.warn("Unexpected state "+oldState+" becomes DARK");break;
                }
               ((SignalHead)t).setAppearance(newState);
            }
            public JButton configureButton() {
                return new JButton(rbean.getString("SignalHeadStateYellow"));
            }
            public boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
		        if (e.getPropertyName().indexOf("Lit")>=0 || e.getPropertyName().indexOf("Held")>=0) return true;
                else return super.matchPropertyName(e);
            }
        };
    }

    void setTitle() {
        f.setTitle(f.rb.getString("TitleSignalTable"));
    }

    JFrame addFrame = null;
    JComboBox typeBox;
    JTextField name = new JTextField(5);
    JTextField to1 = new JTextField(5);
    JTextField to2 = new JTextField(5);
    JTextField to3 = new JTextField(5);
    JLabel nameLabel = new JLabel("");
    JLabel v1Label = new JLabel("");
    JLabel v2Label = new JLabel("");
    JLabel v3Label = new JLabel("");

    String se8c4Aspect = rb.getString("StringSE8c4aspect");
    String tripleTurnout = rb.getString("StringTripleTurnout");
    String doubleTurnout = rb.getString("StringDoubleTurnout");
    String virtualHead = rb.getString("StringVirtual");
    
    void addPressed(ActionEvent e) {
        if (addFrame==null) {
            addFrame = new JFrame(rb.getString("TitleAddSignal"));
            addFrame.getContentPane().setLayout(new BoxLayout(addFrame.getContentPane(), BoxLayout.Y_AXIS));
            addFrame.getContentPane().add(typeBox = new JComboBox(new String[]{
                se8c4Aspect, tripleTurnout, doubleTurnout, virtualHead
            }));
            typeBox.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    typeChanged();
                }
            });
            JPanel p;
            p = new JPanel(); p.setLayout(new FlowLayout());
            p.add(nameLabel);
            p.add(name);
            addFrame.getContentPane().add(p);

            p = new JPanel(); p.setLayout(new FlowLayout());
            p.add(v1Label);
            p.add(to1);
            addFrame.getContentPane().add(p);

            p = new JPanel(); p.setLayout(new FlowLayout());
            p.add(v2Label);
            p.add(to2);
            addFrame.getContentPane().add(p);

            p = new JPanel(); p.setLayout(new FlowLayout());
            p.add(v3Label);
            p.add(to3);
            addFrame.getContentPane().add(p);
            JButton ok;
            addFrame.getContentPane().add(ok = new JButton(rb.getString("ButtonOK")));
            ok.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    okPressed(e);
                }
            });
        }
        typeBox.setSelectedIndex(1);  // force GUI status consistent
        addFrame.pack();
        addFrame.setVisible(true);
    }

    void typeChanged() {
        if (se8c4Aspect.equals(typeBox.getSelectedItem())) {
            nameLabel.setText(rb.getString("LabelUserName"));
            v1Label.setText(rb.getString("LabelTurnoutNumber"));
            v1Label.setVisible(true);
            to1.setVisible(true);
            v2Label.setVisible(false);
            to2.setVisible(false);
            v3Label.setVisible(false);
            to3.setVisible(false);

        } else if (tripleTurnout.equals(typeBox.getSelectedItem())) {
            nameLabel.setText(rb.getString("LabelSystemName"));
            v1Label.setText(rb.getString("LabelGreenTurnoutNumber"));
            v1Label.setVisible(true);
            to1.setVisible(true);
            v2Label.setText(rb.getString("LabelYellowTurnoutNumber"));
            v2Label.setVisible(true);
            to2.setVisible(true);
            v3Label.setText(rb.getString("LabelRedTurnoutNumber"));
            v3Label.setVisible(true);
            to3.setVisible(true);
            
        } else if (doubleTurnout.equals(typeBox.getSelectedItem())) {
            nameLabel.setText(rb.getString("LabelSystemName"));
            v1Label.setText(rb.getString("LabelGreenTurnoutNumber"));
            v1Label.setVisible(true);
            to1.setVisible(true);
            v2Label.setText(rb.getString("LabelRedTurnoutNumber"));
            v2Label.setVisible(true);
            to2.setVisible(true);
            v3Label.setVisible(false);
            to3.setVisible(false);
        } else if (virtualHead.equals(typeBox.getSelectedItem())) {
            nameLabel.setText(rb.getString("LabelSystemName"));
            v1Label.setVisible(false);
            to1.setVisible(false);
            v2Label.setVisible(false);
            to2.setVisible(false);
            v3Label.setVisible(false);
            to3.setVisible(false);
        } else log.error("Unexpected type in typeChanged: "+typeBox.getSelectedItem());
    }

    void okPressed(ActionEvent e) {
        SignalHead s;
        if (se8c4Aspect.equals(typeBox.getSelectedItem())) {
            // the turnout field can hold either a NNN number or a system name
            String num = to1.getText();
            int number;
            if (num.substring(0,2).equals("LT"))
                number = Integer.parseInt(num.substring(2,num.length()));
            else if (num.substring(0,2).equals("lt"))
                number = Integer.parseInt(num.substring(2,num.length()));
            else
                number = Integer.parseInt(num);
            s = new jmri.jmrix.loconet.SE8cSignalHead(number,name.getText());
            InstanceManager.signalHeadManagerInstance().register(s);
        } else if (tripleTurnout.equals(typeBox.getSelectedItem())) {
            Turnout t1 = InstanceManager.turnoutManagerInstance().provideTurnout(to1.getText());
            Turnout t2 = InstanceManager.turnoutManagerInstance().provideTurnout(to2.getText());
            Turnout t3 = InstanceManager.turnoutManagerInstance().provideTurnout(to3.getText());
            if (t1==null) log.warn("Could not provide turnout "+to1.getText());
            if (t2==null) log.warn("Could not provide turnout "+to2.getText());
            if (t3==null) log.warn("Could not provide turnout "+to3.getText());
            if (t3==null || t2==null || t1==null) {
                log.warn("skipping creation of signal "+name.getText()+" due to error");
                return;
            }
            s = new jmri.TripleTurnoutSignalHead(name.getText(),t1, t2, t3);
            InstanceManager.signalHeadManagerInstance().register(s);
        } else if (doubleTurnout.equals(typeBox.getSelectedItem())) {
            Turnout t1 = InstanceManager.turnoutManagerInstance().provideTurnout(to1.getText());
            Turnout t2 = InstanceManager.turnoutManagerInstance().provideTurnout(to2.getText());
            if (t1==null) log.warn("Could not provide turnout "+to1.getText());
            if (t2==null) log.warn("Could not provide turnout "+to2.getText());
            if (t2==null || t1==null) {
                log.warn("skipping creation of signal "+name.getText()+" due to error");
                return;
            }
            s = new jmri.DoubleTurnoutSignalHead(name.getText(),t1, t2);
            InstanceManager.signalHeadManagerInstance().register(s);
        } else if (virtualHead.equals(typeBox.getSelectedItem())) {
            s = new jmri.VirtualSignalHead(name.getText());
            InstanceManager.signalHeadManagerInstance().register(s);
        } else log.error("Unexpected type: "+typeBox.getSelectedItem());
    }

    static final org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SignalHeadTableAction.class.getName());
}
/* @(#)SignalHeadTableAction.java */
