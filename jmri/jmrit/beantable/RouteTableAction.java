// RouteTableAction.java

package jmri.jmrit.beantable;

import jmri.InstanceManager;
import jmri.Manager;
import jmri.NamedBean;
import jmri.Route;
import jmri.Turnout;
import jmri.Sensor;
import jmri.DefaultConditional;
import jmri.Conditional;
import jmri.ConditionalAction;
import jmri.ConditionalVariable;
import jmri.Logix;

import java.awt.Container;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import jmri.util.JmriJFrame;

/**
 * Swing action to create and register a Route Table
 
 * Based in part on SignalHeadTableAction.java by Bob Jacobsen
 *
 * @author	Dave Duchamp    Copyright (C) 2004
 * @author Bob Jacobsen Copyright (C) 2007 
 * @author Simon Reader Copyright (C) 2008
 * @author Pete Cressman Copyright (C) 2009
 *
 * @version     $Revision: 1.46 $
 */

public class RouteTableAction extends AbstractTableAction {

	static final ResourceBundle rbx = ResourceBundle
			.getBundle("jmri.jmrit.beantable.LogixTableBundle");

    /**
     * Create an action with a specific title.
     * <P>
     * Note that the argument is the Action title, not the title of the
     * resulting frame.  Perhaps this should be changed?
     * @param s
     */
    public RouteTableAction(String s) {
        super(s);
        // disable ourself if there is no primary Route manager available
        if (jmri.InstanceManager.routeManagerInstance()==null) {
            setEnabled(false);
        }
        
        // check a constraint required by this implementation,
        // because we assume that the codes are the same as the index
        // in a JComboBox
        if ( Route.ONACTIVE != 0 || Route.ONINACTIVE != 1
            || Route.VETOACTIVE != 2 || Route.VETOINACTIVE !=3 )
            log.error("assumption invalid in RouteTable implementation");
    }
    public RouteTableAction() { this("Route Table");}

    /**
     * Create the JTable DataModel, along with the changes
     * for the specific case of Routes
     */
    void createModel() {
        m = new BeanTableDataModel() {
		    static public final int ENABLECOL = NUMCOLUMN;
		    static public final int LOCKCOL = ENABLECOL+1;
		    static public final int SETCOL = ENABLECOL+2;
    		public int getColumnCount(){ return NUMCOLUMN+3;}

    		public String getColumnName(int col) {
    			if (col==VALUECOL) return "";  // no heading on "Set"
    			if (col==SETCOL) return "";    // no heading on "Edit"
    			if (col==ENABLECOL) return "Enabled";
    			if (col==LOCKCOL) return "Locked";
    			else return super.getColumnName(col);
		    }
    		public Class getColumnClass(int col) {
    			if (col==SETCOL) return JButton.class;
    			if (col==ENABLECOL) return Boolean.class;
    			if (col==LOCKCOL) return Boolean.class;
    			else return super.getColumnClass(col);
		    }
    		public int getPreferredWidth(int col) {
    			if (col==SETCOL) return new JTextField(6).getPreferredSize().width;
    			if (col==ENABLECOL) return new JTextField(6).getPreferredSize().width;
    			if (col==LOCKCOL) return new JTextField(6).getPreferredSize().width;
    			else return super.getPreferredWidth(col);
		    }
    		public boolean isCellEditable(int row, int col) {
    			if (col==SETCOL) return true;
    			if (col==ENABLECOL) return true;
    			// Route lock is available if turnouts are lockable
    			if (col==LOCKCOL) {
    				Route r = (Route)getBySystemName((String)getValueAt(row, SYSNAMECOL));
    				return r.canLock();
    			}
    			else return super.isCellEditable(row,col);
			}    		
    		public Object getValueAt(int row, int col) {
    			if (col==SETCOL) {
    				return "Edit";
    			}
    			else if (col==ENABLECOL) {
    				return new Boolean(((Route)getBySystemName((String)getValueAt(row, SYSNAMECOL))).getEnabled());
    			}
    			else if (col==LOCKCOL) {
    				Route r = (Route)getBySystemName((String)getValueAt(row, SYSNAMECOL));
    				if (r.canLock()){
    					return new Boolean(((Route)getBySystemName((String)getValueAt(row, SYSNAMECOL))).getLocked());
    				}else{
    					// this covers the case when route was locked and lockable turnouts were removed from the route 
    					r.setLocked(false);
    					return new Boolean (false);
    				}
     			}
				else return super.getValueAt(row, col);
			}    		
    		public void setValueAt(Object value, int row, int col) {
    			if (col==SETCOL) {
                    // set up to edit. Use separate Thread so window is created on top
                    class WindowMaker extends Thread {
                        int row;
                        WindowMaker(int r){
                            row = r;
                        }
                        public void run() {
                                Thread.yield();
                                addPressed(null);
                                _systemName.setText((String)getValueAt(row, SYSNAMECOL));
                                editPressed(null); // don't really want to stop Route w/o user action
                            }
                        }
                    WindowMaker t = new WindowMaker(row);
					t.start();
                    /*
                    addPressed(null);
                    _systemName.setText((String)getValueAt(row, SYSNAMECOL));
                    editPressed(null); // don't really want to stop Route w/o user action
                    */
    			}
    			else if (col==ENABLECOL) {
                    // alternate
                    Route r = (Route)getBySystemName((String)getValueAt(row, SYSNAMECOL));
                    boolean v = r.getEnabled();
                    r.setEnabled(!v);
    			}
    			else if (col==LOCKCOL) {
                    // alternate
                    Route r = (Route)getBySystemName((String)getValueAt(row, SYSNAMECOL));
                    boolean v = r.getLocked();
                    r.setLocked(!v);
     			}
    			else super.setValueAt(value, row, col);
    		}
    		
    	      public void configureTable(JTable table) {
                  table.setDefaultRenderer(Boolean.class, new EnablingCheckboxRenderer());
                  table.setDefaultRenderer(JComboBox.class, new jmri.jmrit.symbolicprog.ValueRenderer());
                  table.setDefaultEditor(JComboBox.class, new jmri.jmrit.symbolicprog.ValueEditor());
                  super.configureTable(table);
              }

            /**
             * Delete the bean after all the checking has been done.
             * <P>
             * Deactivate the light, then use the superclass to delete it.
             */
            void doDelete(NamedBean bean) {
                ((Route)bean).deActivateRoute();
                super.doDelete(bean);
            }

    		// want to update when enabled parameter changes
            boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
                if (e.getPropertyName().equals("Enabled")) return true;
                if (e.getPropertyName().equals("Locked")) return true;
                else return super.matchPropertyName(e);
            }

            public Manager getManager() { return jmri.InstanceManager.routeManagerInstance(); }
            public NamedBean getBySystemName(String name) { 
                    return jmri.InstanceManager.routeManagerInstance().getBySystemName(name);
            }
            public NamedBean getByUserName(String name) { 
                return jmri.InstanceManager.routeManagerInstance().getByUserName(name);
            }    
            public void clickOn(NamedBean t) {
               ((Route)t).setRoute();
            }
            public String getValue(String s) {
                return "Set";
            }
            public JButton configureButton() {
                return new JButton(" Set ");
            }
        };
    }
    
    void setTitle() {
        f.setTitle("Route Table");
    }

    String helpTarget() {
        return "package.jmri.jmrit.beantable.RouteTable";
    }

    int sensorModeFromBox(JComboBox box) {
        String mode = (String)box.getSelectedItem();
        int result = jmri.util.StringUtil.getStateFromName(mode, sensorInputModeValues, sensorInputModes);
        
        if (result<0) {
            log.warn("unexpected mode string in sensorMode: "+mode);
            throw new IllegalArgumentException();
        }
        return result;
    }
 
    void setSensorModeBox(int mode, JComboBox box) {
        String result = jmri.util.StringUtil.getNameFromState(mode, sensorInputModeValues, sensorInputModes);
        box.setSelectedItem(result);
    }
       
    int turnoutModeFromBox(JComboBox box) {
        String mode = (String)box.getSelectedItem();
        int result = jmri.util.StringUtil.getStateFromName(mode, turnoutInputModeValues, turnoutInputModes);
        
        if (result<0) {
            log.warn("unexpected mode string in turnoutMode: "+mode);
            throw new IllegalArgumentException();
        }
        return result;
    }
 
    void setTurnoutModeBox(int mode, JComboBox box) {
        String result = jmri.util.StringUtil.getNameFromState(mode, turnoutInputModeValues, turnoutInputModes);
        box.setSelectedItem(result);
    }
       
    JTextField _systemName = new JTextField(10);
    JTextField _userName = new JTextField(22);

    JmriJFrame addFrame = null;
    RouteTurnoutModel _routeTurnoutModel;;
    JScrollPane _routeTurnoutScrollPane;
    RouteSensorModel _routeSensorModel;;
    JScrollPane _routeSensorScrollPane;

    JTextField soundFile = new JTextField(20);
    JTextField scriptFile = new JTextField(20);
    JTextField turnoutsAlignedSensor = new JTextField(8);

    JTextField sensor1 = new JTextField(8);
    JComboBox  sensor1mode = new JComboBox(sensorInputModes);
    JTextField sensor2 = new JTextField(8);
    JComboBox  sensor2mode = new JComboBox(sensorInputModes);
    JTextField sensor3 = new JTextField(8);
    JComboBox  sensor3mode = new JComboBox(sensorInputModes);
    JTextField cTurnout = new JTextField(8);
    JTextField cLockTurnout = new JTextField(8);
	JTextField timeDelay = new JTextField(5);

    JComboBox cTurnoutStateBox = new JComboBox(turnoutInputModes);
    JComboBox cLockTurnoutStateBox = new JComboBox(lockTurnoutInputModes);
    
    ButtonGroup selGroup = null;
    JRadioButton allButton = null;   
    JRadioButton includedButton = null; 
      
    JLabel nameLabel = new JLabel("Route System Name:");
    JLabel userLabel = new JLabel("Route User Name:");
    JLabel fixedSystemName = new JLabel("xxxxxxxxxxx");
    
    JButton createButton = new JButton("Add Route");
    JButton editButton = new JButton("Edit Route");
    JButton deleteButton = new JButton("Delete Route");
    JButton updateButton = new JButton("Update Route");
    JButton cancelButton = new JButton("Cancel");
    JButton exportButton = new JButton("Export to Logix");
    
    static String createInst = "To create a new Route, enter definition, then click 'Add Route'.";
    static String editInst = "To edit an existing Route, enter system name, then click 'Edit Route'.";
    static String updateInst = "To change this Route, make changes above, then click 'Update Route'.";
    static String cancelInst = "To leave Edit mode, without changing this Route, click 'Cancel',";

    JLabel status1 = new JLabel(createInst);
    JLabel status2 = new JLabel(editInst);
    
    JPanel p2xt = null;   // Turnout list table
    JPanel p2xs = null;   // Sensor list table

    Route curRoute = null;
    boolean routeDirty = false;  // true to fire reminder to save work
    boolean editMode = false;

    void addPressed(ActionEvent e) {
		if (editMode) {
			cancelEdit();
		}
        jmri.TurnoutManager tm = InstanceManager.turnoutManagerInstance();
        List systemNameList = tm.getSystemNameList();
        _turnoutList = new ArrayList <RouteTurnout> (systemNameList.size());
        Iterator iter = systemNameList.iterator();
        while (iter.hasNext()) {
            String systemName = (String)iter.next();
            String userName = tm.getBySystemName(systemName).getUserName();
            _turnoutList.add(new RouteTurnout(systemName, userName));
        }
        
        jmri.SensorManager sm = InstanceManager.sensorManagerInstance();
        systemNameList = sm.getSystemNameList();
        _sensorList = new ArrayList <RouteSensor> (systemNameList.size());
        iter = systemNameList.iterator();
        while (iter.hasNext()) {
            String systemName = (String)iter.next();
            String userName = sm.getBySystemName(systemName).getUserName();
            _sensorList.add(new RouteSensor(systemName, userName));
        }
        initializeIncludedList();
        
        // Set up window
        if (addFrame==null) {
            addFrame = new JmriJFrame("Add/Edit Route");
            addFrame.addHelpMenu("package.jmri.jmrit.beantable.RouteAddEdit", true);
            addFrame.setLocation(100,30);
            addFrame.getContentPane().setLayout(new BoxLayout(addFrame.getContentPane(), BoxLayout.Y_AXIS));
            Container contentPane = addFrame.getContentPane();        
            // add system name
            JPanel ps = new JPanel(); 
            ps.setLayout(new FlowLayout());
            ps.add(nameLabel);
            ps.add(_systemName);
            _systemName.setToolTipText("Enter system name for new Route, e.g. R12.");
            ps.add(fixedSystemName);
            fixedSystemName.setVisible(false);
            contentPane.add(ps);
            // add user name
            JPanel p = new JPanel(); 
            p.setLayout(new FlowLayout());
            p.add(userLabel);
            p.add(_userName);
            _userName.setToolTipText("Enter user name for new Route, e.g. Clear Mainline.");
            contentPane.add(p);
            // add Turnout Display Choice
            JPanel py = new JPanel();
            py.add(new JLabel("Show "));
            selGroup = new ButtonGroup();
            allButton = new JRadioButton("All",true);
            selGroup.add(allButton);
            py.add(allButton);
            allButton.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        // Setup for display of all Turnouts, if needed
                        if (!showAll) {
                            showAll = true;
                            _routeTurnoutModel.fireTableDataChanged();
                            _routeSensorModel.fireTableDataChanged();
                        }
                    }
                });
            includedButton = new JRadioButton("Included",false);
            selGroup.add(includedButton);
            py.add(includedButton);
            includedButton.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        // Setup for display of included Turnouts only, if needed
                        if (showAll) {
                            showAll = false;
                            initializeIncludedList();
                            _routeTurnoutModel.fireTableDataChanged();
                            _routeSensorModel.fireTableDataChanged();
                        }
                    }
                });
            py.add(new JLabel("  Turnouts and Sensors"));
            contentPane.add(py);

            // add turnout table
            p2xt = new JPanel();
            JPanel p2xtSpace = new JPanel();
            p2xtSpace.setLayout(new BoxLayout(p2xtSpace, BoxLayout.Y_AXIS));
            p2xtSpace.add(new JLabel("XXX"));
            p2xt.add(p2xtSpace);
            
            JPanel p21t = new JPanel();
            p21t.setLayout(new BoxLayout(p21t, BoxLayout.Y_AXIS));
            p21t.add(new JLabel("Please select "));
            p21t.add(new JLabel(" Turnouts to "));
            p21t.add(new JLabel(" be included "));
            p21t.add(new JLabel(" in this Route."));
            p2xt.add(p21t);
            _routeTurnoutModel = new RouteTurnoutModel();
            JTable routeTurnoutTable = jmri.util.JTableUtil.sortableDataModel(_routeTurnoutModel);
            try {
                jmri.util.com.sun.TableSorter tmodel = ((jmri.util.com.sun.TableSorter)routeTurnoutTable.getModel());
                tmodel.setColumnComparator(String.class, new jmri.util.SystemNameComparator());
                tmodel.setSortingStatus(RouteTurnoutModel.SNAME_COLUMN, jmri.util.com.sun.TableSorter.ASCENDING);
            } catch (ClassCastException e3) {}  // if not a sortable table model
            routeTurnoutTable.setRowSelectionAllowed(false);
            routeTurnoutTable.setPreferredScrollableViewportSize(new 
                                                            java.awt.Dimension(480,80));

            ROW_HEIGHT = routeTurnoutTable.getRowHeight();
            JComboBox stateTCombo = new JComboBox();
   			stateTCombo.addItem(SET_TO_CLOSED);
			stateTCombo.addItem(SET_TO_THROWN);
			stateTCombo.addItem(SET_TO_TOGGLE);
            TableColumnModel routeTurnoutColumnModel = routeTurnoutTable.getColumnModel();
            TableColumn includeColumnT = routeTurnoutColumnModel.
                                                getColumn(RouteTurnoutModel.INCLUDE_COLUMN);
            includeColumnT.setResizable(false);
            includeColumnT.setMinWidth(50);
            includeColumnT.setMaxWidth(60);
            TableColumn sNameColumnT = routeTurnoutColumnModel.
                                                getColumn(RouteTurnoutModel.SNAME_COLUMN);
            sNameColumnT.setResizable(true);
            sNameColumnT.setMinWidth(75);
            sNameColumnT.setMaxWidth(95);
            TableColumn uNameColumnT = routeTurnoutColumnModel.
                                                getColumn(RouteTurnoutModel.UNAME_COLUMN);
            uNameColumnT.setResizable(true);
            uNameColumnT.setMinWidth(210);
            uNameColumnT.setMaxWidth(260);
            TableColumn stateColumnT = routeTurnoutColumnModel.
                                                getColumn(RouteTurnoutModel.STATE_COLUMN);
            stateColumnT.setCellEditor(new DefaultCellEditor(stateTCombo));
            stateColumnT.setResizable(false);
            stateColumnT.setMinWidth(90);
            stateColumnT.setMaxWidth(100);
            _routeTurnoutScrollPane = new JScrollPane(routeTurnoutTable);
            p2xt.add(_routeTurnoutScrollPane,BorderLayout.CENTER);
            contentPane.add(p2xt);
            p2xt.setVisible(true);
 
             // add sensor table
            p2xs = new JPanel();
            JPanel p2xsSpace = new JPanel();
            p2xsSpace.setLayout(new BoxLayout(p2xsSpace, BoxLayout.Y_AXIS));
            p2xsSpace.add(new JLabel("XXX"));
            p2xs.add(p2xsSpace);
            
            JPanel p21s = new JPanel();
            p21s.setLayout(new BoxLayout(p21s, BoxLayout.Y_AXIS));
            p21s.add(new JLabel("Please select "));
            p21s.add(new JLabel(" Sensors to "));
            p21s.add(new JLabel(" be included "));
            p21s.add(new JLabel(" in this Route."));
            p2xs.add(p21s);
            _routeSensorModel = new RouteSensorModel();
            JTable routeSensorTable = jmri.util.JTableUtil.sortableDataModel(_routeSensorModel);
            try {
                jmri.util.com.sun.TableSorter tmodel = ((jmri.util.com.sun.TableSorter)routeSensorTable.getModel());
                tmodel.setColumnComparator(String.class, new jmri.util.SystemNameComparator());
                tmodel.setSortingStatus(RouteSensorModel.SNAME_COLUMN, jmri.util.com.sun.TableSorter.ASCENDING);
            } catch (ClassCastException e3) {}  // if not a sortable table model
            routeSensorTable.setRowSelectionAllowed(false);
            routeSensorTable.setPreferredScrollableViewportSize(new java.awt.Dimension(480,80));
            JComboBox stateSCombo = new JComboBox();
   			stateSCombo.addItem(SET_TO_ACTIVE);
			stateSCombo.addItem(SET_TO_INACTIVE);
			stateSCombo.addItem(SET_TO_TOGGLE);
            TableColumnModel routeSensorColumnModel = routeSensorTable.getColumnModel();
            TableColumn includeColumnS = routeSensorColumnModel.
                                                getColumn(RouteSensorModel.INCLUDE_COLUMN);
            includeColumnS.setResizable(false);
            includeColumnS.setMinWidth(50);
            includeColumnS.setMaxWidth(60);
            TableColumn sNameColumnS = routeSensorColumnModel.
                                                getColumn(RouteSensorModel.SNAME_COLUMN);
            sNameColumnS.setResizable(true);
            sNameColumnS.setMinWidth(75);
            sNameColumnS.setMaxWidth(95);
            TableColumn uNameColumnS = routeSensorColumnModel.
                                                getColumn(RouteSensorModel.UNAME_COLUMN);
            uNameColumnS.setResizable(true);
            uNameColumnS.setMinWidth(210);
            uNameColumnS.setMaxWidth(260);
            TableColumn stateColumnS = routeSensorColumnModel.
                                                getColumn(RouteSensorModel.STATE_COLUMN);
            stateColumnS.setCellEditor(new DefaultCellEditor(stateSCombo));
            stateColumnS.setResizable(false);
            stateColumnS.setMinWidth(90);
            stateColumnS.setMaxWidth(100);
            _routeSensorScrollPane = new JScrollPane(routeSensorTable);
            p2xs.add(_routeSensorScrollPane,BorderLayout.CENTER);
            contentPane.add(p2xs);
            p2xs.setVisible(true);

            // Enter filenames for sound, script
            
            JPanel p25 = new JPanel();
            p25.setLayout(new FlowLayout());
            p25.add(new JLabel("Play sound file:"));
            JButton ss = new JButton("Set");
            ss.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setSoundPressed();
                }
            });
            p25.add(ss);
            p25.add(soundFile);
//            contentPane.add(p25);
            
//            JPanel p26 = new JPanel();
//            p26.setLayout(new FlowLayout());
            p25.add(new JLabel("Run script:"));
            ss = new JButton("Set");
            ss.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setScriptPressed();
                }
            });
            p25.add(ss);
            p25.add(scriptFile);
            contentPane.add(p25);
            
            //add turnouts aligned sensor
            JPanel p27 = new JPanel();
            p27.setLayout(new FlowLayout());
            p27.add(new JLabel("Enter Sensor that Activates when Route Turnouts are correctly aligned (optional):"));
            p27.add(turnoutsAlignedSensor);
            turnoutsAlignedSensor.setToolTipText("Enter a Sensor system name or nothing");
            contentPane.add(p27);
           
            // add control sensor table
            JPanel p3 = new JPanel();
            p3.setLayout(new BoxLayout(p3, BoxLayout.Y_AXIS));
            JPanel p31 = new JPanel();
            p31.add(new JLabel("Enter Sensors that trigger this Route (optional)"));
            p3.add(p31);
            JPanel p32 = new JPanel();
            p32.add(new JLabel("Sensors: "));
            p32.add(sensor1);
            p32.add(sensor1mode);
            p32.add(sensor2);
            p32.add(sensor2mode);
            p32.add(sensor3);
            p32.add(sensor3mode);
            sensor1.setText("");
            sensor2.setText("");
            sensor3.setText("");
            String sensorHint = "Enter a Sensor system name or nothing";
            sensor1.setToolTipText(sensorHint);
            sensor2.setToolTipText(sensorHint);
            sensor3.setToolTipText(sensorHint);
            p3.add(p32);
            // add control turnout
            JPanel p33 = new JPanel();
            p33.add(new JLabel("Enter a Turnout that triggers this Route (optional)"));
            p3.add(p33);
            JPanel p34 = new JPanel();
            p34.add(new JLabel("Turnout: "));
            p34.add(cTurnout);
            cTurnout.setText("");
            cTurnout.setToolTipText("Enter a Turnout system name (real or phantom)");
            p34.add(new JLabel("   Condition: "));
            cTurnoutStateBox.setToolTipText("Setting control Turnout to selected state will trigger Route");
            p34.add(cTurnoutStateBox);
            p3.add(p34);
			// add added delay
     //       JPanel p35 = new JPanel();
     //      p35.add(new JLabel("Enter added delay between Turnout Commands (optional)"));
     //       p3.add(p35);
            JPanel p36 = new JPanel();
            p36.add(new JLabel("Enter additional delay between Turnout Commands (optional), added delay: "));
            p36.add(timeDelay);
            timeDelay.setText("0");
            timeDelay.setToolTipText("Enter time to add to the default of 250 milliseconds between turnout commands");
            p36.add(new JLabel(" (milliseconds) "));
            p3.add(p36);
			// complete this panel
            Border p3Border = BorderFactory.createEtchedBorder();
            p3.setBorder(p3Border);
            contentPane.add(p3);
             
            // add lock control table
            JPanel p4 = new JPanel();
            p4.setLayout(new BoxLayout(p4, BoxLayout.Y_AXIS));
            // add lock control turnout
            JPanel p43 = new JPanel();
            p43.add(new JLabel("Enter a Turnout that controls the lock for this Route (optional)"));
            p4.add(p43);
            JPanel p44 = new JPanel();
            p44.add(new JLabel("Turnout: "));
            p44.add(cLockTurnout);
            cLockTurnout.setText("");
            cLockTurnout.setToolTipText("Enter a Turnout system name (real or phantom)");
            p44.add(new JLabel("   Condition: "));
            cLockTurnoutStateBox.setToolTipText("Setting control Turnout to selected state will lock Route");
            p44.add(cLockTurnoutStateBox);
            p4.add(p44);
			// complete this panel
            Border p4Border = BorderFactory.createEtchedBorder();
            p4.setBorder(p4Border);
            contentPane.add(p4);
            
            // add notes panel
            JPanel pa = new JPanel();
            pa.setLayout(new BoxLayout(pa, BoxLayout.Y_AXIS));
            JPanel p1 = new JPanel();
            p1.setLayout(new FlowLayout());
            p1.add(status1);
            JPanel p2 = new JPanel();
            p2.setLayout(new FlowLayout());
            p2.add(status2);
            pa.add(p1);
            pa.add(p2);
            Border pBorder = BorderFactory.createEtchedBorder();
            pa.setBorder(pBorder);
            contentPane.add(pa);
            // add buttons - Add Route button
            JPanel pb = new JPanel();
            pb.setLayout(new FlowLayout());
            pb.add(createButton);
            createButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    createPressed(e);
                }
            });
            createButton.setToolTipText("Add a new Route using data entered above");
            // Edit Route button 
            pb.add(editButton);
            editButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    editPressed(e);
                }
            });
            editButton.setToolTipText("Set up to edit Route in System Name");
            // Delete Route button
            pb.add(deleteButton);
            deleteButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    deletePressed(e);
                }
            });
            deleteButton.setToolTipText("Delete the Route in System Name");
            // Update Route button
            pb.add(updateButton);
            updateButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    updatePressed(e, false);
                }
            });
            updateButton.setToolTipText("Change this Route and leave Edit mode");
            // Cancel button  
            pb.add(cancelButton);
            cancelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    cancelPressed(e);
                }
            });
            cancelButton.setToolTipText("Leave Edit mode without changing the Route");
            // Export button  
            pb.add(exportButton);
            exportButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    exportPressed(e);
                }
            });
            exportButton.setToolTipText("Export Route to Logix Conditionals for further enhancement");
            
            // Show the initial buttons, and hide the others
            exportButton.setVisible(false);
            cancelButton.setVisible(false);
            updateButton.setVisible(true);
            editButton.setVisible(true);
            createButton.setVisible(true);
            deleteButton.setVisible(false);
            contentPane.add(pb);
            // pack and release space
            addFrame.pack();
            p2xsSpace.setVisible(false);
            p2xtSpace.setVisible(false);
        }
        // set listener for window closing
        addFrame.addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowClosing(java.awt.event.WindowEvent e) {
                    // remind to save, if Route was created or edited
                    if (routeDirty) {
                        javax.swing.JOptionPane.showMessageDialog(addFrame,
                            "Remember to save your Route information.","Reminder",
                                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
                        routeDirty = false;
                    }
                    // hide addFrame
                    addFrame.setVisible(false);
                    // if in Edit, cancel edit mode
                    if (editMode) {
                        cancelEdit();
                    }
                    _routeSensorModel.dispose();
                    _routeTurnoutModel.dispose();
                }
            });
        // display the window
        addFrame.setVisible(true);
        //_routeTurnoutModel.fireTableDataChanged();
        //_routeSensorModel.fireTableDataChanged();
    }   // addPressed

    /**
     * Initialize list of included turnout positions
     */
    void initializeIncludedList() {
        _includedTurnoutList = new ArrayList <RouteTurnout> ();
        for (int i=0; i<_turnoutList.size(); i++) {
            if (_turnoutList.get(i).isIncluded()) {
                _includedTurnoutList.add(_turnoutList.get(i));
            }
        }
        _includedSensorList = new ArrayList <RouteSensor> ();
        for (int i=0; i<_sensorList.size(); i++) {
            if (_sensorList.get(i).isIncluded()) {
                _includedSensorList.add(_sensorList.get(i));
            }
        }
    }

    /**
     * Responds to the Add button
     */
    void createPressed(ActionEvent e) {
        if (!checkNewNamesOK()) {
            return;
        }
        updatePressed(e, true);
        status2.setText(editInst);
        // activate the route
    }

    boolean checkNewNamesOK() {
        // Get system name and user name
        String sName = _systemName.getText().toUpperCase();
        String uName = _userName.getText();
        if (sName.length()==0) {
            status1.setText("Please enter a system name and user name.");
            return false;
        }
        Route g = null;
        // check if a Route with the same user name exists
        if (!uName.equals("")) {
            g = jmri.InstanceManager.routeManagerInstance().getByUserName(uName);
            if (g!=null) {
                // Route with this user name already exists
                status1.setText("Error - Route with this user name already exists.");
                return false;
            }
            else {
                return true;
            }
        }
        // check if a Route with this system name already exists
        g = jmri.InstanceManager.routeManagerInstance().getBySystemName(sName);
        if (g!=null) {
            // Route already exists
            status1.setText("Error - Route with this system name already exists.");
            return false;
        }
        return true;
    }

    Route checkNamesOK() {
        // Get system name and user name
        String sName = _systemName.getText().toUpperCase();
        String uName = _userName.getText();
        if (sName.length()==0) {
            status1.setText("Please enter a system name and user name.");
            return null;
        }
        Route g = jmri.InstanceManager.routeManagerInstance().provideRoute(sName, uName);
        if (g==null) {
            // should never get here
            log.error("Unknown failure to create Route with System Name: "+sName);
        }
        g.deActivateRoute();
        return g;
    }

    /**
     * Sets the Turnout information for adding or editting
     */
    int setTurnoutInformation(Route g) {
        for (int i=0; i<_includedTurnoutList.size(); i++) {
            RouteTurnout t = _includedTurnoutList.get(i);
            g.addOutputTurnout(t.getSysName(), t.getState());
        }
        return _includedTurnoutList.size();
    }

    /**
     * Sets the Sensor information for adding or editting
     */
    int setSensorInformation(Route g) {
        for (int i=0; i<_includedSensorList.size(); i++) {
            RouteSensor s = _includedSensorList.get(i);
            g.addOutputSensor(s.getSysName(), s.getState());
        }
        return _includedSensorList.size();
    }

    /**
     * Sets the Sensor, Turnout, and delay control information for adding or editting if any
     */
    void setControlInformation(Route g) {
        // Get sensor control information if any
        String sensorSystemName = sensor1.getText();
        if (sensorSystemName.length() > 0) {
            Sensor s1 = InstanceManager.sensorManagerInstance().
                            provideSensor(sensorSystemName);
            if ( (s1==null) || (!g.addSensorToRoute(sensorSystemName, sensorModeFromBox(sensor1mode))) ) {
                log.error("Unexpected failure to add Sensor '"+sensorSystemName+
                                            "' to Route '"+g.getSystemName()+"'.");
            }
        }
        sensorSystemName = sensor2.getText();
        if (sensorSystemName.length() > 0) {
            Sensor s2 = InstanceManager.sensorManagerInstance().
                            provideSensor(sensorSystemName);
            if ( (s2==null) || (!g.addSensorToRoute(sensorSystemName, sensorModeFromBox(sensor2mode))) ) {
                log.error("Unexpected failure to add Sensor '"+sensorSystemName+
                                            "' to Route '"+g.getSystemName()+"'.");
            }
        }
        sensorSystemName = sensor3.getText();
        if (sensorSystemName.length() > 0) {
            Sensor s3 = InstanceManager.sensorManagerInstance().
                            provideSensor(sensorSystemName);
            if ( (s3==null) || (!g.addSensorToRoute(sensorSystemName, sensorModeFromBox(sensor3mode))) ) {
                log.error("Unexpected failure to add Sensor '"+sensorSystemName+
                                            "' to Route '"+g.getSystemName()+"'.");
            }
        }
        
        //turnouts aligned sensor
        sensorSystemName = turnoutsAlignedSensor.getText();
        if (sensorSystemName.length() > 0) {
            Sensor tas = InstanceManager.sensorManagerInstance().provideSensor(sensorSystemName);
            if (tas==null){
                log.error("Unexpected failure to add Turnouts Aligned Sensor '"+sensorSystemName+
                                            "' to Route '"+g.getSystemName()+"'.");
            }
            else
            {
            g.setTurnoutsAlignedSensor(sensorSystemName);
            }
        }
        
        // Set turnout information if there is any
        String turnoutSystemName = cTurnout.getText();
        if (turnoutSystemName.length() > 0) {
            Turnout t = InstanceManager.turnoutManagerInstance().
                                    provideTurnout(turnoutSystemName);
            if (t!=null) {
                g.setControlTurnout(turnoutSystemName);
                // set up control turnout state
                g.setControlTurnoutState(turnoutModeFromBox(cTurnoutStateBox));
            }
            else {
                g.setControlTurnout("");
                log.error("Unexpected failure to add control Turnout '"+
                        turnoutSystemName+"' to Route '"+g.getSystemName()+"'.");
            }
        } else {
            // No control Turnout was entered
            g.setControlTurnout("");
        }
		// set delay information
		int addDelay = 0;
		try 
		{
			addDelay = Integer.parseInt(timeDelay.getText());
		}
		catch (NumberFormatException e)
		{
			addDelay = 0;
			timeDelay.setText("0");
		}
		if (addDelay<0) 
		{
			// added delay must be a positive integer
			addDelay = 0;
			timeDelay.setText("0");
		}
		g.setRouteCommandDelay(addDelay);
		
	    // Set lock turnout information if there is any
        String turnoutLockSystemName = cLockTurnout.getText();
        if (turnoutLockSystemName.length() > 0) {
            Turnout t = InstanceManager.turnoutManagerInstance().
                                    provideTurnout(turnoutLockSystemName);
            if (t!=null) {
                g.setLockControlTurnout(turnoutLockSystemName);
                // set up control turnout state
                g.setLockControlTurnoutState(turnoutModeFromBox(cLockTurnoutStateBox));
            }
            else {
                g.setLockControlTurnout("");
                log.error("Unexpected failure to add lock control Turnout '"+
                        turnoutLockSystemName+"' to Route '"+g.getSystemName()+"'.");
            }
        } else {
            // No control Turnout was entered
            g.setLockControlTurnout("");
        }
    }

    JFileChooser soundChooser = null;
    
    /**
     * Set the sound file
     */
    void setSoundPressed() {
        if (soundChooser == null) {
            soundChooser = new JFileChooser(jmri.jmrit.XmlFile.userFileLocationDefault());
            soundChooser.setFileFilter(new jmri.util.NoArchiveFileFilter());
        }
        soundChooser.rescanCurrentDirectory();
        int retVal = soundChooser.showOpenDialog(null);
        // handle selection or cancel
        if (retVal == JFileChooser.APPROVE_OPTION) {
            try {
                soundFile.setText(soundChooser.getSelectedFile().getCanonicalPath());
            } catch (java.io.IOException e) {
                log.error("exception setting sound file: "+e);
            }
        }
    }
    
    JFileChooser scriptChooser = null;

    /**
     * Set the script file
     */
    void setScriptPressed() {
        if (scriptChooser == null) {
            scriptChooser = jmri.jmrit.XmlFile.userFileChooser("Python script files", "py");
        }
        scriptChooser.rescanCurrentDirectory();
        int retVal = scriptChooser.showOpenDialog(null);
        // handle selection or cancel
        if (retVal == JFileChooser.APPROVE_OPTION) {
            try {
                scriptFile.setText(scriptChooser.getSelectedFile().getCanonicalPath());
            } catch (java.io.IOException e) {
                log.error("exception setting script file: "+e);
            }
        }
    }
    
    /**
     * Responds to the Edit button
     */
    void editPressed(ActionEvent e) {
        // identify the Route with this name if it already exists
        String sName = _systemName.getText().toUpperCase();
        Route g = jmri.InstanceManager.routeManagerInstance().getBySystemName(sName);
        if (g==null) {
            // Route does not exist, so cannot be edited
            status1.setText("Route with the entered System Name was not found.");
            return;
        }
        // Route was found, make its system name not changeable
        curRoute = g;
        fixedSystemName.setText(sName);
        fixedSystemName.setVisible(true);
        _systemName.setVisible(false);
        // deactivate this Route
        curRoute.deActivateRoute();
        // get information for this route
        _userName.setText(g.getUserName());
        // set up Turnout list for this route
        int setRow = 0;
        for (int i=_turnoutList.size()-1; i>=0; i--) {
            RouteTurnout turnout = _turnoutList.get(i);
            String tSysName = turnout.getSysName();
            if (g.isOutputTurnoutIncluded(tSysName) ) {
                turnout.setIncluded(true);
                turnout.setState(g.getOutputTurnoutSetState(tSysName));
                setRow = i;
            } else {
                turnout.setIncluded(false);
                turnout.setState(Turnout.CLOSED);
            }
        }
        setRow -= 1;
        if (setRow < 0) {
            setRow = 0;
        }
        _routeTurnoutScrollPane.getVerticalScrollBar().setValue(setRow*ROW_HEIGHT);
        _routeTurnoutModel.fireTableDataChanged();      
        // set up Sensor list for this route
        for (int i=_sensorList.size()-1; i>=0; i--) {
            RouteSensor sensor = _sensorList.get(i);
            String tSysName = sensor.getSysName();
            if (g.isOutputSensorIncluded(tSysName) ) {
                sensor.setIncluded(true);
                sensor.setState(g.getOutputSensorSetState(tSysName));
                setRow = i;
            } else {
                sensor.setIncluded(false);
                sensor.setState(Sensor.INACTIVE);
            }
        }
        setRow -= 1;
        if (setRow < 0) {
            setRow = 0;
        }
        _routeSensorScrollPane.getVerticalScrollBar().setValue(setRow*ROW_HEIGHT);
        _routeSensorModel.fireTableDataChanged();      
        // get sound, script names
        scriptFile.setText(g.getOutputScriptName());
        soundFile.setText(g.getOutputSoundName());
        
        // get turnout aligned sensor
        turnoutsAlignedSensor.setText(g.getTurnoutsAlignedSensor());

        // set up Sensors if there are any
        String[] temNames = new String[Route.MAX_CONTROL_SENSORS];
        int[] temModes = new int[Route.MAX_CONTROL_SENSORS];
        for (int k = 0; k<Route.MAX_CONTROL_SENSORS; k++) {
            temNames[k] = g.getRouteSensorName(k);
            temModes[k] = g.getRouteSensorMode(k);
        }
        sensor1.setText(temNames[0]);
        setSensorModeBox(temModes[0], sensor1mode);

        sensor2.setText(temNames[1]);
        setSensorModeBox(temModes[1], sensor2mode);

        sensor3.setText(temNames[2]);
        setSensorModeBox(temModes[2], sensor3mode);

        // set up control Turnout if there is one
        cTurnout.setText(g.getControlTurnout()); 
        
        setTurnoutModeBox(g.getControlTurnoutState(), cTurnoutStateBox);
        
        // set up lock control Turnout if there is one
        cLockTurnout.setText(g.getLockControlTurnout()); 
        
        setTurnoutModeBox(g.getLockControlTurnoutState(), cLockTurnoutStateBox);
        
		// set up additional delay
		timeDelay.setText(Integer.toString(g.getRouteCommandDelay()));
        // begin with showing all Turnouts   
        // set up buttons and notes
        status1.setText(updateInst);
        status2.setText(cancelInst);
        status2.setVisible(true);
        deleteButton.setVisible(true);
        cancelButton.setVisible(true);
        updateButton.setVisible(true);
        exportButton.setVisible(true);
        editButton.setVisible(false);
        createButton.setVisible(false);
        fixedSystemName.setVisible(true);
        _systemName.setVisible(false);
        editMode = true;
    }   // editPressed

    /**
     * Responds to the Delete button
     */
    void deletePressed(ActionEvent e) {
        // route is already deactivated, just delete it
        InstanceManager.routeManagerInstance().deleteRoute(curRoute);

        curRoute = null;
        finishUpdate();
    }

    /**
     * Responds to the Update button - update to Route Table
     */
    void updatePressed(ActionEvent e, boolean newRoute ) {
        // Check if the User Name has been changed
        String uName = _userName.getText();
        String sName = _systemName.getText().toUpperCase();
        Route g = checkNamesOK();
        if (g == null) {
            return;
        }
        // user name is unique, change it
        g.setUserName(uName);
        // clear the current output information for this Route
        g.clearOutputTurnouts();
        g.clearOutputSensors();
        // clear the current Sensor information for this Route
        g.clearRouteSensors();
        // add those indicated in the window
        initializeIncludedList();
        int numTurnoutIncluded = setTurnoutInformation(g);
        int numSensorIncluded = setSensorInformation(g);
        // set the current values of the filenames
        g.setOutputScriptName(scriptFile.getText());
        g.setOutputSoundName(soundFile.getText());
        // add control Sensors and a control Turnout if entered in the window
        setControlInformation(g);        
        curRoute = g;
        finishUpdate();
        status1.setText((newRoute ? "New Route created: ":"Route updated: ")
                        +uName+", "+ _includedTurnoutList.size()
                        +" Turnouts, "+_includedSensorList.size()+" Sensors");
    }

    void finishUpdate() {
        // move to show all turnouts if not there
        cancelIncludedOnly();
        // Provide feedback to user 
        // switch GUI back to selection mode
        status2.setText(editInst);
        status2.setVisible(true);
        deleteButton.setVisible(false);
        cancelButton.setVisible(false);
        updateButton.setVisible(false);
        exportButton.setVisible(false);
        editButton.setVisible(true);
        createButton.setVisible(true);
        fixedSystemName.setVisible(false);
        clearPage();
        _systemName.setVisible(true);
        // reactivate the Route
        routeDirty = true;
        // get out of edit mode
        editMode = false;
        if (curRoute != null) {
            curRoute.activateRoute();    
        }
    }

    void clearPage() {
        _systemName.setVisible(true);
        _systemName.setText("");
        _userName.setText("");
        sensor1.setText("");
        sensor2.setText("");
        sensor3.setText("");
        cTurnout.setText("");
        cLockTurnout.setText("");
        turnoutsAlignedSensor.setText("");
        soundFile.setText("");
        scriptFile.setText("");
        for (int i=_turnoutList.size()-1; i>=0; i--) { 
            _turnoutList.get(i).setIncluded(false);
        }
        for (int i=_sensorList.size()-1; i>=0; i--) {
            _sensorList.get(i).setIncluded(false);
        }
    }


/////////////////////// Export to Logix ////////////////////////////
    /**
     * Responds to the Update button - update to Logix
     */
    void exportPressed(ActionEvent e) {
        curRoute = checkNamesOK();
        String sName = _systemName.getText().toUpperCase();
        if (sName.length()==0) {
            sName = fixedSystemName.getText(); 
        }
        String uName = _userName.getText();
        String logixSystemName = LOGIX_SYS_NAME+sName;
        Logix logix = InstanceManager.logixManagerInstance().getBySystemName(logixSystemName);
        if (logix == null) {
            logix = InstanceManager.logixManagerInstance().createNewLogix(logixSystemName, uName);
            if (logix == null) {
                log.error("Failed to create Logix "+logixSystemName+", "+uName);
                return;
            }
        }
        logix.deActivateLogix();
        initializeIncludedList();
        
        /////////////////// Construct output actions for change to true //////////////////////
        ArrayList <ConditionalAction> actionList = new ArrayList<ConditionalAction>();

        for (int i=0; i<_includedSensorList.size(); i++) {
            RouteSensor rSensor = _includedSensorList.get(i);
            String name = rSensor.getUserName();
            if (name == null || name.length() == 0) {
                 name = rSensor.getSysName();
            }
            actionList.add(new ConditionalAction(Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE, 
                                  Conditional.ACTION_SET_SENSOR, name, rSensor.getState(), ""));
        }
        for (int i=0; i<_includedTurnoutList.size(); i++) {
            RouteTurnout rTurnout = _includedTurnoutList.get(i);
            String name = rTurnout.getUserName();
            if (name == null || name.length() == 0) {
                 name = rTurnout.getSysName();
            }
            actionList.add(new ConditionalAction(Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE, 
                                  Conditional.ACTION_SET_TURNOUT, name, rTurnout.getState(), ""));
        }
        String file = soundFile.getText();
        if (file.length() > 0) {
            actionList.add(new ConditionalAction(Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE, 
                                  Conditional.ACTION_RUN_SCRIPT, "", -1, file));
        }
        file = scriptFile.getText();
        if (file.length() > 0) {
            actionList.add(new ConditionalAction(Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE, 
                                  Conditional.ACTION_PLAY_SOUND, "", -1, file));
        }

        ///// Construct 'AND' clause from 'VETO' controls ////////
        ArrayList <ConditionalVariable> vetoList = new ArrayList<ConditionalVariable>();

        String andClause = null;
        ConditionalVariable cVar = makeCtrlSensorVar(sensor1, sensor1mode, true, false);
        if (cVar != null) {
            vetoList.add(cVar);
        }
        cVar = makeCtrlSensorVar(sensor2, sensor2mode, true, false);
        if (cVar != null) {
            vetoList.add(cVar);
        }
        cVar = makeCtrlSensorVar(sensor3, sensor3mode, true, false);
        if (cVar != null) {
            vetoList.add(cVar);
        }
        cVar = makeCtrlTurnoutVar(cTurnout, cTurnoutStateBox, true, false);
        if (cVar != null) {
            vetoList.add(cVar);
        }

        // remove old Conditionals for actions (ver 2.5.2 only -remove a bad idea)
        char[] ch = sName.toCharArray();
        int hash= 0;
        for (int i=0; i<ch.length; i++) {
            hash += ch[i];
        }
        String cSystemName = CONDITIONAL_SYS_PREFIX+"T"+hash;
        removeConditionals(cSystemName, logix);
        cSystemName = CONDITIONAL_SYS_PREFIX+"F"+hash;
        removeConditionals(cSystemName, logix);
        cSystemName = CONDITIONAL_SYS_PREFIX+"A"+hash;
        removeConditionals(cSystemName, logix);
        cSystemName = CONDITIONAL_SYS_PREFIX+"L"+hash;
        removeConditionals(cSystemName, logix);

        int n=0;
        do {
            n++;
            cSystemName = logixSystemName+n+"A";
        } while (removeConditionals(cSystemName, logix));
        n = 0;
        do {
            n++;
            cSystemName = logixSystemName+n+"T";
        } while (removeConditionals(cSystemName, logix));
        cSystemName = logixSystemName+"L";
        removeConditionals(cSystemName, logix);

        String cUserName = null;

        ///////////////// Make Trigger Conditionals //////////////////////
        ArrayList <ConditionalVariable> onChangeList = new ArrayList<ConditionalVariable>();

        int numConds = 1;
        numConds = makeSensorConditional(sensor1, sensor1mode, numConds, false, 
                                         actionList, vetoList, logix, logixSystemName, uName); 
        numConds = makeSensorConditional(sensor2, sensor2mode, numConds, false, 
                                         actionList, vetoList, logix, logixSystemName, uName); 
        numConds = makeSensorConditional(sensor3, sensor3mode, numConds, false, 
                                         actionList, vetoList, logix, logixSystemName, uName); 
        numConds = makeTurnoutConditional(cTurnout, cTurnoutStateBox, numConds, false, 
                                          actionList, vetoList, logix, logixSystemName, uName); 

        ////// Construct actions for false from the 'any change' controls ////////////
        numConds = makeSensorConditional(sensor1, sensor1mode, numConds, true, actionList, vetoList, 
                                         logix, logixSystemName, uName); 
        numConds = makeSensorConditional(sensor2, sensor2mode, numConds, true, actionList, vetoList, 
                                         logix, logixSystemName, uName); 
        numConds = makeSensorConditional(sensor3, sensor3mode, numConds, true, actionList, vetoList, 
                                         logix, logixSystemName, uName); 
        numConds = makeTurnoutConditional(cTurnout, cTurnoutStateBox, numConds, true, actionList, 
                                          vetoList, logix, logixSystemName, uName); 

	    ///////////////// Set up Alignment Sensor, if there is one //////////////////////////
        String sensorSystemName = turnoutsAlignedSensor.getText();
        if (sensorSystemName.length() > 0) {
            // verify name (logix doesn't use "provideXXX") 
            Sensor s = InstanceManager.sensorManagerInstance().getByUserName(sensorSystemName);
            if (s == null) {
                sensorSystemName = sensorSystemName.toUpperCase();
                s = InstanceManager.sensorManagerInstance().getBySystemName(sensorSystemName);
            }
            if (s != null) {
                cSystemName = logixSystemName+"1A";
                cUserName = sensorSystemName+"A "+uName;
                ArrayList <ConditionalVariable> variableList = new ArrayList<ConditionalVariable>();
                for (int i=0; i<_includedTurnoutList.size(); i++) {
                    RouteTurnout rTurnout = _includedTurnoutList.get(i);
                    String name = rTurnout.getUserName();
                    if (name == null || name.length() == 0) {
                         name = rTurnout.getSysName();
                    }
                    // exclude toggled outputs
                    switch (rTurnout.getState()) {
                        case Turnout.CLOSED:
                            variableList.add(new ConditionalVariable(false, Conditional.OPERATOR_AND, 
                                                       Conditional.TYPE_TURNOUT_CLOSED, name, true));
                            break;
                        case Turnout.THROWN:
                            variableList.add(new ConditionalVariable(false, Conditional.OPERATOR_AND, 
                                                       Conditional.TYPE_TURNOUT_THROWN, name, true));
                            break;
                    }
                }
                actionList = new ArrayList<ConditionalAction>();
                actionList.add(new ConditionalAction(Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE, 
                                      Conditional.ACTION_SET_SENSOR, sensorSystemName, Sensor.ACTIVE, ""));
                actionList.add(new ConditionalAction(Conditional.ACTION_OPTION_ON_CHANGE_TO_FALSE, 
                                      Conditional.ACTION_SET_SENSOR, sensorSystemName, Sensor.INACTIVE, ""));

                Conditional c = InstanceManager.conditionalManagerInstance().createNewConditional(cSystemName, cUserName);
                c.setStateVariables(variableList);
                c.setLogicType(Conditional.ALL_AND, "");
                c.setAction(actionList);
                logix.addConditional(cSystemName, 0);
                c.calculate(true, null);
            }
        }

	    ///////////////// Set lock turnout information if there is any //////////////////////////
        String turnoutLockSystemName = cLockTurnout.getText();
        if (turnoutLockSystemName.length() > 0) {
            // verify name (logix doesn't use "provideXXX") 
            Turnout t = InstanceManager.turnoutManagerInstance().getByUserName(turnoutLockSystemName);
            if (t == null) {
                turnoutLockSystemName = turnoutLockSystemName.toUpperCase();
                t = InstanceManager.turnoutManagerInstance().getBySystemName(turnoutLockSystemName);
            }
            if (t != null) {
                cSystemName = logixSystemName+"1L";
                cUserName = turnoutLockSystemName+"L "+uName;
                ArrayList <ConditionalVariable> variableList = new ArrayList<ConditionalVariable>();
                String devName = cTurnout.getText();
                int mode = turnoutModeFromBox(cTurnoutStateBox);
                int type = Conditional.TYPE_TURNOUT_CLOSED;
                if (mode == Route.ONTHROWN) {
                    type = Conditional.TYPE_TURNOUT_THROWN;
                }
                variableList.add(new ConditionalVariable(false, Conditional.OPERATOR_NONE,
                                                         type, turnoutLockSystemName, true));

                actionList = new ArrayList<ConditionalAction>();
                int option = Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE;
                type = Turnout.LOCKED;
                if (mode == Route.ONCHANGE) {
                    option = Conditional.ACTION_OPTION_ON_CHANGE;
                    type = Route.TOGGLE;
                }
                for (int i=0; i<_includedTurnoutList.size(); i++) {
                    RouteTurnout rTurnout = _includedTurnoutList.get(i);
                    String name = rTurnout.getUserName();
                    if (name == null || name.length() == 0) {
                         name = rTurnout.getSysName();
                    }
                    actionList.add(new ConditionalAction(option, Conditional.ACTION_LOCK_TURNOUT,
                                                         name, type, ""));
                }
                if (mode != Route.ONCHANGE) {
                    // add non-toggle actions on
                    option = Conditional.ACTION_OPTION_ON_CHANGE_TO_FALSE;
                    type = Turnout.UNLOCKED;
                    for (int i=0; i<_includedTurnoutList.size(); i++) {
                        RouteTurnout rTurnout = _includedTurnoutList.get(i);
                        String name = rTurnout.getUserName();
                        if (name == null || name.length() == 0) {
                             name = rTurnout.getSysName();
                        }
                        actionList.add(new ConditionalAction(option, Conditional.ACTION_LOCK_TURNOUT,
                                                             name, type, ""));
                    }
                }

               // add new Conditionals for action on 'locks'
                Conditional c = InstanceManager.conditionalManagerInstance().createNewConditional(cSystemName, cUserName);
                c.setStateVariables(variableList);
                c.setLogicType(Conditional.ALL_AND, "");
                c.setAction(actionList);
                logix.addConditional(cSystemName, 0);
                c.calculate(true, null);
            }
        }
        logix.activateLogix();
        if (curRoute != null) {
            jmri.InstanceManager.routeManagerInstance().deleteRoute(curRoute);
            curRoute = null;
        }
        status1.setText("Route \""+uName+"\" exported to Logix: "+ _includedTurnoutList.size()
                        +" Turnouts, "+_includedSensorList.size()+" Sensors");
        finishUpdate();
    }

    boolean removeConditionals(String cSystemName, Logix logix) {
        Conditional c = InstanceManager.conditionalManagerInstance().getBySystemName(cSystemName);
        if (c != null) {
            logix.deleteConditional(cSystemName);
            InstanceManager.conditionalManagerInstance().deleteConditional(c);
            return true;
        }
        return false;
    }

    int makeSensorConditional(JTextField nameText, JComboBox sensorbox, int numConds, 
                        boolean onChange, ArrayList<ConditionalAction>actionList,
                        ArrayList<ConditionalVariable>vetoList, Logix logix, String prefix, String uName) 
    {
        ConditionalVariable cVar = makeCtrlSensorVar(nameText, sensorbox, false, onChange);
        if (cVar != null) {
            ArrayList <ConditionalVariable> varList = new ArrayList<ConditionalVariable>();
            varList.add(cVar);
            for ( int i=0; i<vetoList.size(); i++) {
                varList.add(cloneVariable(vetoList.get(i)));
            }
            String cSystemName = prefix+numConds+"T";
            String cUserName = nameText.getText()+numConds+"C "+uName;
            Conditional c = InstanceManager.conditionalManagerInstance().createNewConditional(cSystemName, cUserName);
            c.setStateVariables(varList);
            int option = onChange ? Conditional.ACTION_OPTION_ON_CHANGE : Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE;
            c.setAction(cloneActionList(actionList, option));
            c.setLogicType(Conditional.ALL_AND, "");
            logix.addConditional(cSystemName, 0);
            c.calculate(true, null);
            numConds++;
        }
        return numConds;
    }

    int makeTurnoutConditional(JTextField nameText, JComboBox box, int numConds, 
                        boolean onChange, ArrayList<ConditionalAction>actionList,
                        ArrayList<ConditionalVariable>vetoList, Logix logix, String prefix, String uName) 
    {
        ConditionalVariable cVar = makeCtrlTurnoutVar(nameText, box, false, onChange);
        if (cVar != null) {
            ArrayList <ConditionalVariable> varList = new ArrayList<ConditionalVariable>();
            varList.add(cVar);
            for ( int i=0; i<vetoList.size(); i++) {
                varList.add(cloneVariable(vetoList.get(i)));
            }
            String cSystemName = prefix+numConds+"T";
            String cUserName = nameText.getText()+numConds+"C "+uName;
            Conditional c = InstanceManager.conditionalManagerInstance().createNewConditional(cSystemName, cUserName);
            c.setStateVariables(varList);
            int option = onChange ? Conditional.ACTION_OPTION_ON_CHANGE : Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE;
            c.setAction(cloneActionList(actionList, option));
            c.setLogicType(Conditional.ALL_AND, "");
            logix.addConditional(cSystemName, 0);
            c.calculate(true, null);
            numConds++;
        }
        return numConds;
    }

    ConditionalVariable cloneVariable(ConditionalVariable v) {
        return new ConditionalVariable(v.isNegated(), v.getOpern(), v.getType(), v.getName(), v.doTriggerActions());
    }

    ArrayList<ConditionalAction> cloneActionList(ArrayList<ConditionalAction> actionList, int option) {
        ArrayList <ConditionalAction> list = new ArrayList <ConditionalAction> ();
		for (int i = 0; i<actionList.size(); i++) {
            ConditionalAction action = actionList.get(i);
            ConditionalAction clone = new ConditionalAction();
            clone.setType(action.getType());
            clone.setOption(option);
            clone.setDeviceName(action.getDeviceName());
            clone.setActionData(action.getActionData());
            clone.setActionString(action.getActionString());
            list.add(clone);
		}
        return list;
    }

    ConditionalVariable makeCtrlSensorVar(JTextField nameText, JComboBox sensorbox,
                                           boolean makeVeto, boolean onChange) {
        String devName = nameText.getText();
        if (devName.length() == 0) {
            return null;
        }
        if (devName != null && devName.length()>0) {
            // verify name (logix doesn't use "provideXXX") 
            Sensor s = InstanceManager.sensorManagerInstance().getByUserName(devName);
            if (s == null) {
                devName = devName.toUpperCase();
                s = InstanceManager.sensorManagerInstance().getBySystemName(devName);
                if (s == null) {
                    return null;
                }
            }
            int oper = Conditional.OPERATOR_AND;
            int mode = sensorModeFromBox(sensorbox);
            boolean trigger = true;
            boolean negated = false;
            int type = 0;
            switch (mode) {
                case Route.ONACTIVE:    // route fires if sensor goes active
                    if (makeVeto || onChange){
                        return null;
                    }
                    type = Conditional.TYPE_SENSOR_ACTIVE;
                    break;
                case Route.ONINACTIVE:  // route fires if sensor goes inactive
                    if (makeVeto || onChange){
                        return null;
                    }
                    type = Conditional.TYPE_SENSOR_INACTIVE;
                    break;
                case Route.ONCHANGE:  // route fires if sensor goes active or inactive 
                    if (makeVeto || !onChange){
                        return null;
                    }
                    type = Conditional.TYPE_SENSOR_ACTIVE;
                    break;
                case Route.VETOACTIVE:  // sensor must be active for route to fire
                    if (!makeVeto || onChange){
                        return null;
                    }
                    type = Conditional.TYPE_SENSOR_ACTIVE;
                    negated = true;
                    trigger = false;
                    break;
                case Route.VETOINACTIVE:
                    if (!makeVeto || onChange){
                        return null;
                    }
                    type = Conditional.TYPE_SENSOR_INACTIVE;
                    negated = true;
                    trigger = false;
                    break;
                default:
                    log.error("Control Sensor "+devName+" has bad mode= "+mode);
                    return null;
            }
            return new ConditionalVariable(negated, oper, type, devName, trigger);
        }
        return null;
    }

    ConditionalVariable makeCtrlTurnoutVar(JTextField nameText, JComboBox box,
                                            boolean makeVeto, boolean onChange) {
        String devName = nameText.getText();
        if (devName.length() == 0) {
            return null;
        }
        if (devName != null && devName.length()>0) {
            Turnout t = InstanceManager.turnoutManagerInstance().getByUserName(devName);
            if (t == null) {
                devName = devName.toUpperCase();
                t = InstanceManager.turnoutManagerInstance().getBySystemName(devName);
                if (t == null) {
                    return null;
                }
            }
            int mode = turnoutModeFromBox(box);
            int oper = Conditional.OPERATOR_AND;;
            int type = 0;
            boolean negated = false;
            boolean trigger = true;
            switch (mode) {
                case Route.ONCLOSED:    // route fires if turnout goes closed
                    if (makeVeto || onChange){
                        return null;
                    }
                    type = Conditional.TYPE_TURNOUT_CLOSED;
                    break;
                case Route.ONTHROWN:  // route fires if turnout goes thrown
                    if (makeVeto || onChange){
                        return null;
                    }
                    type = Conditional.TYPE_TURNOUT_THROWN;
                    break;
                case Route.ONCHANGE:    // route fires if turnout goes active or inactive
                    if (makeVeto || !onChange){
                        return null;
                    }
                    type = Conditional.TYPE_TURNOUT_CLOSED;
                    break;
                case Route.VETOCLOSED:  // turnout must be closed for route to fire
                    if (!makeVeto || onChange){
                        return null;
                    }
                    type = Conditional.TYPE_TURNOUT_CLOSED;
                    trigger = false;
                    negated = true;
                    break;
                case Route.VETOTHROWN:  // turnout must be thrown for route to fire
                    if (!makeVeto || onChange){
                        return null;
                    }
                    type = Conditional.TYPE_TURNOUT_THROWN;
                    trigger = false;
                    negated = true;
                    break;
                default:
                    log.error("Control Turnout "+devName+" has bad mode= "+mode);
                    return null;
            }
            return new ConditionalVariable(negated, oper, type, devName, trigger);
        }
        return null;
    }

    /**
     * Responds to the Cancel button
     */
    void cancelPressed(ActionEvent e) {
        cancelEdit();
    }
    
    /** 
     * Cancels edit mode
     */
    void cancelEdit() {
        if (editMode) {
            status1.setText(createInst);
            status2.setText(editInst);
            finishUpdate();
            // get out of edit mode
            editMode = false;
            curRoute = null;
        }
    }
    
    /** 
     * Cancels included Turnouts only option
     */
    void cancelIncludedOnly() {
        if (!showAll) {
            allButton.doClick();
        }
    }

    /**
     * Base table model for selecting outputs
     */
    public abstract class RouteOutputModel extends AbstractTableModel implements PropertyChangeListener
    {
        public Class getColumnClass(int c) {
            if (c == INCLUDE_COLUMN) {
                return Boolean.class;
            }
            else {
                return String.class;
            }
        }

        public void propertyChange(java.beans.PropertyChangeEvent e) {
            if (e.getPropertyName().equals("length")) {
                // a new NamedBean is available in the manager
                fireTableDataChanged();
            }
        }
        
        public void dispose() {
            InstanceManager.turnoutManagerInstance().removePropertyChangeListener(this);
        }

        public String getColumnName(int c) {return COLUMN_NAMES[c];}

        public int getColumnCount () {return 4;}

        public boolean isCellEditable(int r,int c) {
            return ( (c==INCLUDE_COLUMN) || (c==STATE_COLUMN) );
        }

        public static final int SNAME_COLUMN = 0;
        public static final int UNAME_COLUMN = 1;
        public static final int INCLUDE_COLUMN = 2;
        public static final int STATE_COLUMN = 3;
    }

    /**
     * Table model for selecting Turnouts and Turnout State
     */
    class RouteTurnoutModel extends RouteOutputModel
    {
        RouteTurnoutModel() {
            InstanceManager.turnoutManagerInstance().addPropertyChangeListener(this);
        }

        public int getRowCount () {
            if (showAll)
                return _turnoutList.size();
            else
                return _includedTurnoutList.size();
        }
        public Object getValueAt (int r,int c) {
            ArrayList <RouteTurnout> turnoutList = null;
            if (showAll) {
                turnoutList = _turnoutList;
            }
            else {
                turnoutList = _includedTurnoutList;
            }
            switch (c) {
                case INCLUDE_COLUMN:
                    return new Boolean(turnoutList.get(r).isIncluded());
                case SNAME_COLUMN:  // slot number
                    return turnoutList.get(r).getSysName();
                case UNAME_COLUMN:  //
                    return turnoutList.get(r).getUserName();
                case STATE_COLUMN:  //
                    return turnoutList.get(r).getSetToState();
                default:
                    return null;
            }
        }
        public void setValueAt(Object type,int r,int c) {
            ArrayList <RouteTurnout> turnoutList = null;
            if (showAll) {
                turnoutList = _turnoutList;
            }
            else {
                turnoutList = _includedTurnoutList;
            }
            switch (c) {
                case INCLUDE_COLUMN:
                    turnoutList.get(r).setIncluded(((Boolean)type).booleanValue());  
                    break;
                case STATE_COLUMN: 
                    turnoutList.get(r).setSetToState((String)type);
                    break;
            }
        }
    }

    /**
     * Set up table for selecting Sensors and Sensor State
     */
    class RouteSensorModel extends RouteOutputModel
    {
        RouteSensorModel() {
            InstanceManager.sensorManagerInstance().addPropertyChangeListener(this);
        }

        public int getRowCount () {
            if (showAll)
                return _sensorList.size();
            else
                return _includedSensorList.size();
        }

        public Object getValueAt (int r,int c) {
            ArrayList <RouteSensor> sensorList = null;
            if (showAll) {
                sensorList = _sensorList;
            }
            else {
                sensorList = _includedSensorList;
            }
            switch (c) {
                case INCLUDE_COLUMN:
                    return new Boolean(sensorList.get(r).isIncluded());
                case SNAME_COLUMN:  // slot number
                    return sensorList.get(r).getSysName();
                case UNAME_COLUMN:  //
                    return sensorList.get(r).getUserName();
                case STATE_COLUMN:  //
                    return sensorList.get(r).getSetToState();
                default:
                    return null;
            }
        }

        public void setValueAt(Object type,int r,int c) {
            ArrayList <RouteSensor> sensorList = null;
            if (showAll) {
                sensorList = _sensorList;
            }
            else {
                sensorList = _includedSensorList;
            }
            switch (c) {
                case INCLUDE_COLUMN:
                    sensorList.get(r).setIncluded(((Boolean)type).booleanValue());  
                    break;
                case STATE_COLUMN: 
                    sensorList.get(r).setSetToState((String)type);
                    break;
            }
        }
    }

    private boolean showAll = true;   // false indicates show only included Turnouts

    public final static String LOGIX_SYS_NAME = "RTX";
    public final static String CONDITIONAL_SYS_PREFIX = LOGIX_SYS_NAME+"C";
    private static int ROW_HEIGHT;

    private static String[] COLUMN_NAMES = {rbx.getString("ColumnLabelSystemName"),
                                            rbx.getString("ColumnLabelUserName"),
                                            rbx.getString("ColumnLabelInclude"),
                                            rbx.getString("ColumnLabelSetState")};
    private static String SET_TO_ACTIVE = rbx.getString("Set")+" "+rbx.getString("SensorActive");
    private static String SET_TO_INACTIVE = rbx.getString("Set")+" "+rbx.getString("SensorInactive");
    private static String SET_TO_CLOSED = rbx.getString("Set")+" "+
                                            InstanceManager.turnoutManagerInstance().getClosedText();
    private static String SET_TO_THROWN = rbx.getString("Set")+" "+
                                            InstanceManager.turnoutManagerInstance().getThrownText();
    private static String SET_TO_TOGGLE = rbx.getString("Set")+" "+rbx.getString("Toggle");

    private static String[] sensorInputModes = new String[]{"On Active", "On Inactive", "On Change", "Veto Active", "Veto Inactive"};
    private static int[] sensorInputModeValues = new int[]{Route.ONACTIVE, Route.ONINACTIVE, Route.ONCHANGE,
                                            Route.VETOACTIVE, Route.VETOINACTIVE};
        
    private static String[] turnoutInputModes = new String[]{"On "+InstanceManager.turnoutManagerInstance().getClosedText(),
                                            "On "+InstanceManager.turnoutManagerInstance().getThrownText(),
                                            "On Change", "Veto Closed", "Veto Thrown"};
    private static int[] turnoutInputModeValues = new int[]{Route.ONCLOSED, Route.ONTHROWN, Route.ONCHANGE,
                                            Route.VETOCLOSED, Route.VETOTHROWN};

    private static String[] lockTurnoutInputModes = new String[]{"On "+InstanceManager.turnoutManagerInstance().getClosedText(),
                                            "On "+InstanceManager.turnoutManagerInstance().getThrownText(),
                                            "On Change"};
    private static int[] lockTurnoutInputModeValues = new int[]{Route.ONCLOSED, Route.ONTHROWN, Route.ONCHANGE};    

    private ArrayList <RouteTurnout> _turnoutList;      // array of all Turnouts
    private ArrayList <RouteTurnout> _includedTurnoutList; 
    
    private ArrayList <RouteSensor> _sensorList;        // array of all Sensorsy
    private ArrayList <RouteSensor> _includedSensorList;

    private abstract class RouteElement {
        String _sysName;
        String _userName;
        boolean _included;
        int _setToState;

        RouteElement(String sysName, String userName) {
            _sysName = sysName;
            _userName = userName;
            _included = false;
            _setToState = Sensor.INACTIVE;
        }
        String getSysName() {
            return _sysName;
        }
        String getUserName() {
            return _userName;
        }
        boolean isIncluded() {
            return _included;
        }
        void setIncluded(boolean include) {
            _included = include;
        }
        abstract String getSetToState();
        abstract void setSetToState(String state);

        int getState() {
            return _setToState;
        }
        void setState(int state) {
            _setToState = state;
        }
    }

    private class RouteSensor extends RouteElement {
        RouteSensor (String sysName, String userName) {
            super(sysName, userName);
        }
        String getSetToState() {
            switch (_setToState) {
                case Sensor.INACTIVE:
                    return SET_TO_INACTIVE;
                case Sensor.ACTIVE:
                    return SET_TO_ACTIVE;
                case Route.TOGGLE:
                    return SET_TO_TOGGLE;
            }
            return "";
        }
        void setSetToState(String state) {
            if (SET_TO_INACTIVE.equals(state)) {
                _setToState = Sensor.INACTIVE;
            } else if (SET_TO_ACTIVE.equals(state)) {
                _setToState = Sensor.ACTIVE;
            } else if (SET_TO_TOGGLE.equals(state)) {
                _setToState = Route.TOGGLE;
            }
        }
    }

    private class RouteTurnout extends RouteElement {
        RouteTurnout(String sysName, String userName) {
            super(sysName, userName);
        }
        String getSetToState() {
            switch (_setToState) {
                case Turnout.CLOSED:
                    return SET_TO_CLOSED;
                case Turnout.THROWN:
                    return SET_TO_THROWN;
                case Route.TOGGLE:
                    return SET_TO_TOGGLE;
            }
            return "";
        }
        void setSetToState(String state) {
            if (SET_TO_CLOSED.equals(state)) {
                _setToState = Turnout.CLOSED;
            } else if (SET_TO_THROWN.equals(state)) {
                _setToState = Turnout.THROWN;
            } else if (SET_TO_TOGGLE.equals(state)) {
                _setToState = Route.TOGGLE;
            }
        }
    }
    static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RouteTableAction.class.getName());
}
/* @(#)RouteTableAction.java */
