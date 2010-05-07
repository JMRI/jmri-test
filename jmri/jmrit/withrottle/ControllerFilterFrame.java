package jmri.jmrit.withrottle;

import java.awt.BorderLayout;
import java.awt.Color;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import jmri.InstanceManager;
import jmri.RouteManager;
import jmri.TurnoutManager;
import jmri.util.JmriJFrame;

/**
 *	@author Brett Hoffman   Copyright (C) 2010
 *	@version $Revision: 1.1 $
 */
public class ControllerFilterFrame extends JmriJFrame implements TableModelListener{

    static final ResourceBundle rbx = ResourceBundle.getBundle("jmri.jmrit.beantable.LogixTableBundle");
    private static String[] COLUMN_NAMES = {rbx.getString("ColumnLabelSystemName"),
                                            rbx.getString("ColumnLabelUserName"),
                                            rbx.getString("ColumnLabelInclude")};
    
    public ControllerFilterFrame(){
        super("Controls Filter");
    }

    public void initComponents() throws Exception {
        JTabbedPane tabbedPane = new JTabbedPane();
        if (InstanceManager.turnoutManagerInstance()!=null) {
            
            tabbedPane.addTab("Turnouts", null, addTurnoutPanel(),"Limit the turnouts controllable by WiFi devices.");
        }
        
        if (InstanceManager.routeManagerInstance()!=null) {
            
            tabbedPane.addTab("Routes", null, addRoutePanel(),"Limit the routes controllable by WiFi devices.");
        }
        
        add(tabbedPane);

        pack();

        addHelpMenu("package.jmri.jmrit.withrottle.UserInterface", true);
    }

    private JPanel addTurnoutPanel(){
        JPanel tPanel = new JPanel();

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(new JLabel("Please select "));
        p.add(new JLabel("Turnouts to "));
        p.add(new JLabel("be controlled "));
        p.add(new JLabel("by WiFi devices."));
        tPanel.add(p);
        
        JTable table = new JTable(new TurnoutFilterModel());
        buildTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        tPanel.add(scrollPane,BorderLayout.CENTER);

        return tPanel;
    }
    
    private JPanel addRoutePanel(){
        JPanel tPanel = new JPanel();

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(new JLabel("Please select "));
        p.add(new JLabel("Routes to "));
        p.add(new JLabel("be controlled "));
        p.add(new JLabel("by WiFi devices."));
        tPanel.add(p);

        JTable table = new JTable(new RouteFilterModel());
        buildTable(table);
        

        JScrollPane scrollPane = new JScrollPane(table);
        tPanel.add(scrollPane,BorderLayout.CENTER);

        return tPanel;
    }

    private void buildTable(JTable table){
        table.getModel().addTableModelListener(this);

        table.setRowSelectionAllowed(false);
        table.setPreferredScrollableViewportSize(new java.awt.Dimension(480,240));

        //table.getTableHeader().setBackground(Color.lightGray);
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(Color.gray);
        table.setRowHeight(30);

        TableColumnModel columnModel = table.getColumnModel();

        TableColumn include = columnModel.getColumn(AbstractFilterModel.INCLUDECOL);
        include.setResizable(false);
        include.setMinWidth(50);
        include.setMaxWidth(80);

        TableColumn sName = columnModel.getColumn(AbstractFilterModel.SNAMECOL);
        sName.setResizable(true);
        sName.setMinWidth(75);
        sName.setMaxWidth(120);

        TableColumn uName = columnModel.getColumn(AbstractFilterModel.UNAMECOL);
        uName.setResizable(true);
        uName.setMinWidth(210);
        uName.setMaxWidth(340);
    }

    protected void handleModified() {
        if (getModifiedFlag()) {
            this.setVisible(true);
            javax.swing.JOptionPane.showMessageDialog(this,
                "Please save changes to your panel file.",
                "Save Warning:",
                javax.swing.JOptionPane.WARNING_MESSAGE

            );

        }
    }

    protected void storeValues() {
        log.warn("default storeValues does nothing for "+getTitle());
    }

    public void tableChanged(TableModelEvent e) {
        log.debug("Set mod flag true for: "+getTitle());
        this.setModifiedFlag(true);
    }

    
    
    public abstract class AbstractFilterModel extends AbstractTableModel implements PropertyChangeListener{
        
        List<String> sysNameList= null;
        boolean isDirty;

        public Class<?> getColumnClass(int c) {
            if (c == INCLUDECOL) {
                return Boolean.class;
            }
            else {
                return String.class;
            }
        }

        public void propertyChange(java.beans.PropertyChangeEvent e) {
            if (e.getPropertyName().equals("length")) {
                fireTableDataChanged();
            }
        }
        
        public void dispose() {
            InstanceManager.turnoutManagerInstance().removePropertyChangeListener(this);
            InstanceManager.routeManagerInstance().removePropertyChangeListener(this);
        }

        public String getColumnName(int c) {
            return COLUMN_NAMES[c];
        }

        public int getColumnCount () {
            return 3;
        }

        public int getRowCount () {
            return sysNameList.size();
        }
        
        public boolean isCellEditable(int r,int c) {
            return (c==INCLUDECOL);
        }
        
        public static final int SNAMECOL = 0;
        public static final int UNAMECOL = 1;
        public static final int INCLUDECOL = 2;
    }

    class TurnoutFilterModel extends AbstractFilterModel{

        TurnoutManager mgr = InstanceManager.turnoutManagerInstance();
        
        TurnoutFilterModel() {
            
            sysNameList = mgr.getSystemNameList();
            mgr.addPropertyChangeListener(this);
        }

        public Object getValueAt (int r,int c) {
            
            // some error checking
            if (r >= sysNameList.size()){
            	log.debug("row is greater than turnout list size");
            	return null;
            }
            switch (c) {
                case INCLUDECOL:
                    return new Boolean(mgr.getBySystemName(sysNameList.get(r)).getWifiControllable());
                case SNAMECOL:
                    return sysNameList.get(r);
                case UNAMECOL:
                    return mgr.getBySystemName(sysNameList.get(r)).getUserName();
                default:
                    return null;
            }
        }
        
        public void setValueAt(Object type,int r,int c) {
            
            switch (c) {
                case INCLUDECOL:
                    mgr.getBySystemName(sysNameList.get(r)).setWifiControllable(((Boolean)type).booleanValue());
                    if (!isDirty){
                        this.fireTableChanged(new TableModelEvent(this));
                        isDirty = true;
                    }
                    break;
            }
        }
    }

    class RouteFilterModel extends AbstractFilterModel{

        RouteManager mgr = InstanceManager.routeManagerInstance();

        RouteFilterModel() {

            sysNameList = mgr.getSystemNameList();
            mgr.addPropertyChangeListener(this);
        }

        public Object getValueAt (int r,int c) {

            // some error checking
            if (r >= sysNameList.size()){
            	log.debug("row is greater than turnout list size");
            	return null;
            }
            switch (c) {
                case INCLUDECOL:
                    return new Boolean(mgr.getBySystemName(sysNameList.get(r)).getWifiControllable());
                case SNAMECOL:
                    return sysNameList.get(r);
                case UNAMECOL:
                    return mgr.getBySystemName(sysNameList.get(r)).getUserName();
                default:
                    return null;
            }
        }

        public void setValueAt(Object type,int r,int c) {

            switch (c) {
                case INCLUDECOL:
                    mgr.getBySystemName(sysNameList.get(r)).setWifiControllable(((Boolean)type).booleanValue());
                    if (!isDirty){
                        this.fireTableChanged(new TableModelEvent(this));
                        isDirty = true;
                    }
                    break;
            }
        }
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ControllerFilterFrame.class.getName());

}
