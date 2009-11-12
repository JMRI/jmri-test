package jmri.jmrit.logix;

//import java.util.ArrayList;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;

//import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
//import java.awt.FlowLayout;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.Manager;
import jmri.NamedBean;
import jmri.jmrit.display.PickListModel;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;
import jmri.jmrit.catalog.NamedIcon;

/**
 * An WarrantAction contains the operating permissions and directives needed for
 * a train to proceed from an Origin to a Destination
 * <P>
 * 
 *
 * @author	Pete Cressman  Copyright (C) 2009
 */
public class WarrantTableAction extends AbstractAction {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.logix.WarrantBundle");
    static int STRUT_SIZE = 10;
    static JMenu _warrantMenu;
    private static HashMap <String, WarrantFrame> _frameMap = new HashMap <String, WarrantFrame> ();
    private static TableFrame _tableFrame;

    public WarrantTableAction(String menuOption) {
	    super(rb.getString(menuOption));
    }

    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (rb.getString("ShowWarrants").equals(command)){
            if (_tableFrame==null) {
                _tableFrame = new TableFrame();
                try {
                    _tableFrame.initComponents();
                } catch (Exception ex ) {/*bogus*/ }
            }
            _tableFrame.setVisible(true);
        } else if (rb.getString("CreateWarrant").equals(command)){
            CreateWarrantFrame f = new CreateWarrantFrame();
            try {
                f.initComponents();
            } catch (Exception ex ) {/*bogus*/ }
            f.setVisible(true);
        }
    }

    synchronized public static void updateWarrantMenu() {
        _warrantMenu.removeAll();
        _warrantMenu.add(new WarrantTableAction("ShowWarrants"));
        JMenu editWarrantMenu = new JMenu(rb.getString("EditWarrantMenu"));
        _warrantMenu.add(editWarrantMenu);
        ActionListener editWarrantAction = new ActionListener()
        {
            public void actionPerformed(ActionEvent e) {
                openWarrantFrame(e.getActionCommand());
            }
        };
        WarrantManager manager = InstanceManager.warrantManagerInstance();
        String[] sysNames = manager.getSystemNameArray();
         
        for (int i = 0; i < sysNames.length; i++) {
            Warrant warrant = manager.getBySystemName(sysNames[i]);
            //JMenuItem mi = new JMenuItem(warrant.getDisplayName());
            //mi.setActionCommand(sysNames[i]);
            //mi.addActionListener(openWarrantAction);
            //openWarrantMenu.add(mi);
            JMenuItem mi = new JMenuItem(warrant.getDisplayName());
            mi.setActionCommand(warrant.getDisplayName());
            mi.addActionListener(editWarrantAction);
            editWarrantMenu.add(mi);                                                  
        }

        _warrantMenu.add(new jmri.jmrit.logix.WarrantTableAction("CreateWarrant"));
        if (log.isDebugEnabled()) log.debug("updateMenu to "+sysNames.length+" warrants.");
    }

    synchronized public static void closeWarrantFrame(String key) {
        _frameMap.remove(key);
    }

    synchronized public static void openWarrantFrame(String key) {
        WarrantFrame frame = _frameMap.get(key);
        if (frame==null) {
            frame = new WarrantFrame(key);
            _frameMap.put(key, frame);
        }
        if (log.isDebugEnabled()) log.debug("openWarrantFrame for "+key+", size= "+_frameMap.size());
        frame.setVisible(true);
        frame.toFront();
    }

    /******************* CreateWarrant ***********************/

    class CreateWarrantFrame extends JFrame {

        JTextField _sysNameBox;
        JTextField _userNameBox;

        public CreateWarrantFrame() {
            setTitle(WarrantTableAction.rb.getString("TitleCreateWarrant"));
        }

        public void initComponents() {
            JPanel contentPane = new JPanel();
            contentPane.setLayout(new BorderLayout(10,10));
            JLabel prompt = new JLabel(rb.getString("CreateWarrantPrompt"));
            contentPane.add(prompt, BorderLayout.NORTH);

            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.add(Box.createHorizontalStrut(STRUT_SIZE));
            JPanel p = new JPanel();
            p.add(new JLabel(rb.getString("SystemName")));
            _sysNameBox = new JTextField(15);
            p.add(_sysNameBox);
            panel.add(p);
            panel.add(Box.createHorizontalStrut(STRUT_SIZE));
            p = new JPanel();
            p.add(new JLabel(rb.getString("UserName")));
            _userNameBox = new JTextField(15);
            p.add(_userNameBox);
            panel.add(p);
            panel.add(Box.createHorizontalStrut(STRUT_SIZE));
            contentPane.add(panel, BorderLayout.CENTER);

            panel = new JPanel();
            JButton doneButton = new JButton(rb.getString("ButtonDone"));
            doneButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    makeWarrant();
                }
            });
            doneButton.setPreferredSize(doneButton.getPreferredSize());
            panel.add(doneButton);
            contentPane.add(panel, BorderLayout.SOUTH);
            contentPane.add(Box.createVerticalStrut(STRUT_SIZE), BorderLayout.EAST);
            contentPane.add(Box.createVerticalStrut(STRUT_SIZE), BorderLayout.WEST);

            setContentPane(contentPane);
            setLocationRelativeTo(null);
            setVisible(true);
            pack();
        }

        void makeWarrant() {
            String sysName = _sysNameBox.getText().trim();
            String userName = _userNameBox.getText().trim();
            if (sysName==null || sysName.length()==0 || sysName.toUpperCase().equals("IW")) {
                dispose();
                return;
            }
            if (userName.length()==0) {
                userName = null;
            }
            boolean failed = false;
            Warrant w = InstanceManager.warrantManagerInstance().getBySystemName(sysName);
            if (w != null) {
                failed = true;
            } else {
                w = InstanceManager.warrantManagerInstance().getByUserName(userName);
                if (w != null) {
                    failed = true;
                } else {
                    // register warrant if user saves this instance
                    w = new Warrant(sysName,userName);
                }
            }
            if (failed) {
                JOptionPane.showMessageDialog(null, java.text.MessageFormat.format(
                                rb.getString("WarrantExists"), userName, sysName), 
                                rb.getString("WarningTitle"), JOptionPane.ERROR_MESSAGE);
            } else {
                _frameMap.put(w.getDisplayName(), new WarrantFrame(w, true));
                dispose();
            }
        }

    }

    /**
    *  Note: _warrantMenu is static
    */
    synchronized public static JMenu makeWarrantMenu() {
        _warrantMenu = new JMenu(rb.getString("MenuWarrant"));
        updateWarrantMenu();
        return _warrantMenu;
    }


    /********************** Show Warrants Table *************************/

    static final String halt = rb.getString("Halt");
    static final String resume = rb.getString("Resume");
    static final String abort = rb.getString("Abort");
    static final String[] controls = {halt, resume, abort};

    class TableFrame  extends jmri.util.JmriJFrame // implements ActionListener 
    {
        private WarrantTableModel     _model;


        public TableFrame() 
        {
            setTitle(rb.getString("WarrantTable"));
            _model = new WarrantTableModel();
            _model.init();
            JTable table = new JTable(_model);
            table.setDefaultRenderer(Boolean.class, new ButtonRenderer());
            table.setDefaultRenderer(JComboBox.class, new jmri.jmrit.symbolicprog.ValueRenderer());
            table.setDefaultEditor(JComboBox.class, new jmri.jmrit.symbolicprog.ValueEditor());
            JComboBox box = new JComboBox(controls);
            box.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            table.getColumnModel().getColumn(WarrantTableModel.CONTROL_COLUMN).setCellEditor(new DefaultCellEditor(box));
            table.getColumnModel().getColumn(WarrantTableModel.ROUTE_COLUMN).setCellEditor(new ComboBoxCellEditor(new JComboBox()));
            table.getColumnModel().getColumn(WarrantTableModel.ALLOCATE_COLUMN).setCellEditor(new ButtonEditor(new JButton()));
            table.getColumnModel().getColumn(WarrantTableModel.ALLOCATE_COLUMN).setCellRenderer(new ButtonRenderer());
            table.getColumnModel().getColumn(WarrantTableModel.DEALLOC_COLUMN).setCellEditor(new ButtonEditor(new JButton()));
            table.getColumnModel().getColumn(WarrantTableModel.DEALLOC_COLUMN).setCellRenderer(new ButtonRenderer());
            table.getColumnModel().getColumn(WarrantTableModel.SET_COLUMN).setCellEditor(new ButtonEditor(new JButton()));
            table.getColumnModel().getColumn(WarrantTableModel.SET_COLUMN).setCellRenderer(new ButtonRenderer());
            table.getColumnModel().getColumn(WarrantTableModel.RUN_TRAIN_COLUMN).setCellEditor(new ButtonEditor(new JButton()));
            table.getColumnModel().getColumn(WarrantTableModel.RUN_TRAIN_COLUMN).setCellRenderer(new ButtonRenderer());
            table.getColumnModel().getColumn(WarrantTableModel.EDIT_COLUMN).setCellEditor(new ButtonEditor(new JButton()));
            table.getColumnModel().getColumn(WarrantTableModel.EDIT_COLUMN).setCellRenderer(new ButtonRenderer());
            table.getColumnModel().getColumn(WarrantTableModel.DELETE_COLUMN).setCellEditor(new ButtonEditor(new JButton()));
            table.getColumnModel().getColumn(WarrantTableModel.DELETE_COLUMN).setCellRenderer(new ButtonRenderer());
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            for (int i=0; i<_model.getColumnCount(); i++) {
                int width = _model.getPreferredWidth(i);
                table.getColumnModel().getColumn(i).setPreferredWidth(width);
            }
            JScrollPane tablePane = new JScrollPane(table);
            Dimension dim = table.getPreferredSize();
            dim.height = table.getRowHeight()*12;
            tablePane.getViewport().setPreferredSize(dim);

            JPanel tablePanel = new JPanel();
            tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));
            JLabel title = new JLabel(rb.getString("ShowWarrants"));
            tablePanel.add(title, BorderLayout.NORTH);
            tablePanel.add(tablePane);

            addWindowListener(new java.awt.event.WindowAdapter() {
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        dispose();
                    }
                });			
            JMenuBar menuBar = new JMenuBar();
            JMenu fileMenu = new JMenu(rb.getString("MenuFile"));
            fileMenu.add(new jmri.configurexml.SaveMenu());
            menuBar.add(fileMenu);
            setJMenuBar(menuBar);
            addHelpMenu("package.jmri.jmrit.logix.Warrant", true);

            setContentPane(tablePanel);
            setLocation(0,100);
            setVisible(true);
            pack();
        }
    }

    public class ComboBoxCellEditor extends DefaultCellEditor
    {
        ComboBoxCellEditor() {
            super(new JComboBox());
        }
        ComboBoxCellEditor(JComboBox comboBox) {
            super(comboBox);
            comboBox.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        }
        public Component getTableCellEditorComponent(JTable table, Object value, 
                                         boolean isSelected, int row, int column) 
        {
            WarrantTableModel model = (WarrantTableModel)table.getModel();
            Warrant warrant = (Warrant)model.getBeanAt(row);
            JComboBox comboBox = (JComboBox)getComponent();
            comboBox.removeAllItems();
            List <BlockOrder> orders = warrant.getOrders();
            for (int i=0; i<orders.size(); i++) {
                comboBox.addItem(orders.get(i).getBlock().getDisplayName());
            }
            return comboBox; 
        }
    }
    
    /************************* WarrantTableModel Table ******************************/

    class WarrantTableModel  extends PickListModel 
    {
        public static final int WARRANT_COLUMN = 0;
        public static final int ROUTE_COLUMN =1;
        public static final int TRAIN_ID_COLUMN = 2;
        public static final int ADDRESS_COLUMN = 3;
        public static final int ALLOCATE_COLUMN = 4;
        public static final int DEALLOC_COLUMN = 5;
        public static final int SET_COLUMN = 6;
        public static final int RUN_TRAIN_COLUMN = 7;
        public static final int CONTROL_COLUMN = 8;
        public static final int EDIT_COLUMN = 9;
        public static final int DELETE_COLUMN = 10;
        public static final int NUMCOLS = 11;

        WarrantManager manager;

        public WarrantTableModel() {
            super();
            manager = InstanceManager.warrantManagerInstance();
        }

        public Manager getManager() {
            return manager;
        }
        public NamedBean getBySystemName(String name) {
            return manager.getBySystemName(name);
        }
        public NamedBean addBean(String name) {
            return manager.provideWarrant(name);
        }

        public int getColumnCount () {
            return NUMCOLS;
        }

        public String getColumnName(int col) {
            switch (col) {
                case WARRANT_COLUMN: return WarrantTableAction.rb.getString("Warrant");
                case ROUTE_COLUMN: return WarrantTableAction.rb.getString("Route");
                case TRAIN_ID_COLUMN: return WarrantTableAction.rb.getString("TrainId");
                case ADDRESS_COLUMN: return WarrantTableAction.rb.getString("DccAddress");
                case ALLOCATE_COLUMN: return WarrantTableAction.rb.getString("Allocate");
                case DEALLOC_COLUMN: return WarrantTableAction.rb.getString("Deallocate");
                case SET_COLUMN: return WarrantTableAction.rb.getString("SetRoute");
                case RUN_TRAIN_COLUMN: return WarrantTableAction.rb.getString("Run");
                case CONTROL_COLUMN: return WarrantTableAction.rb.getString("Control");
            }
            return "";
        }


        public boolean isCellEditable(int row, int col) {
            switch (col) {
                case WARRANT_COLUMN:
                case TRAIN_ID_COLUMN:
                case ADDRESS_COLUMN:
                    return false;
                case ROUTE_COLUMN:
                case ALLOCATE_COLUMN:
                case DEALLOC_COLUMN:
                case SET_COLUMN:
                case RUN_TRAIN_COLUMN:
                case CONTROL_COLUMN:
                case EDIT_COLUMN:
                case DELETE_COLUMN:
                    return true;
            }
            return false;
        }

        public Class<?> getColumnClass(int col) {
            switch (col) {
                case WARRANT_COLUMN:  return String.class;
                case ROUTE_COLUMN:    return String.class;  // JComboBox.class;
                case TRAIN_ID_COLUMN: return String.class;
                case ADDRESS_COLUMN:  return String.class;
                case ALLOCATE_COLUMN: return JButton.class;
                case DEALLOC_COLUMN:  return JButton.class;
                case SET_COLUMN:    return JButton.class;
                case RUN_TRAIN_COLUMN: return JButton.class;
                case CONTROL_COLUMN:  return String.class; // JComboBox.class;
                case EDIT_COLUMN:     return JButton.class;
                case DELETE_COLUMN:   return JButton.class;
            }
            return String.class;
        }

        public int getPreferredWidth(int col) {
            switch (col) {
                case WARRANT_COLUMN:
                case ROUTE_COLUMN:
                case TRAIN_ID_COLUMN:
                    return new JTextField(13).getPreferredSize().width;
                case ADDRESS_COLUMN:
                    return new JTextField(8).getPreferredSize().width;
                case ALLOCATE_COLUMN:
                case DEALLOC_COLUMN:
                case SET_COLUMN:
                case RUN_TRAIN_COLUMN:
                    return new JButton("XX").getPreferredSize().width;
                case CONTROL_COLUMN:
                    return new JTextField(20).getPreferredSize().width;
                case EDIT_COLUMN:
                case DELETE_COLUMN:
                    return new JButton("DELETE").getPreferredSize().width;
            }
            return new JTextField(10).getPreferredSize().width;
        }

        public Object getValueAt(int row, int col) {
            //if (log.isDebugEnabled()) log.debug("getValueAt: row= "+row+", column= "+col);
            Warrant w = (Warrant)getBeanAt(row);
            JRadioButton allocButton = new JRadioButton();
            JRadioButton deallocButton = new JRadioButton();
            ButtonGroup group = new ButtonGroup();
            group.add(allocButton);
            group.add(deallocButton);
            switch (col) {
                case WARRANT_COLUMN:
                    return w.getDisplayName();
                case ROUTE_COLUMN:
                    BlockOrder bo = w.getBlockOrderAt(0);
                    if (bo!=null) {
                        return java.text.MessageFormat.format(WarrantTableAction.rb.getString("Origin"),
                                                           bo.getBlock().getDisplayName());
                    }
                    break;
                case TRAIN_ID_COLUMN:
                    return w.getTrainId();
                case ADDRESS_COLUMN:
                    if (w.getDccAddress()!=null) {
                        return w.getDccAddress().toString();
                    }
                    break;
                case ALLOCATE_COLUMN:
                    if (w.isAllocated()) {
                        return new NamedIcon("resources/icons/smallschematics/tracksegments/circuit-occupied.gif", "occupied");
                    } else {
                        return new NamedIcon("resources/icons/smallschematics/tracksegments/circuit-empty.gif", "off");
                    }
                case DEALLOC_COLUMN:
                    if (w.isAllocated()) {
                        return new NamedIcon("resources/icons/smallschematics/tracksegments/circuit-empty.gif", "off");
                    } else {
                        return new NamedIcon("resources/icons/smallschematics/tracksegments/circuit-occupied.gif", "occupied");
                    }
                case SET_COLUMN:
                    if (w.hasRouteSet()) {
                        return new NamedIcon("resources/icons/smallschematics/tracksegments/circuit-green.gif", "off");
                    } else {
                        return new NamedIcon("resources/icons/smallschematics/tracksegments/circuit-empty.gif", "occupied");
                    }
                case RUN_TRAIN_COLUMN:
                    if (w.getRunMode() == Warrant.MODE_NONE) {
                        return new NamedIcon("resources/icons/smallschematics/tracksegments/circuit-empty.gif", "red");
                    } else {
                        return new NamedIcon("resources/icons/smallschematics/tracksegments/circuit-error.gif", "off");
                    }
                case CONTROL_COLUMN:
                    switch (w.getRunMode()) {
                        case Warrant.MODE_NONE:
                            if (w.getOrders().size()==0) {
                                return WarrantTableAction.rb.getString("BlankWarrant");
                            }
                            if (w.getDccAddress()==null){
                                return WarrantTableAction.rb.getString("NoLoco");
                            }
                            if (w.getThrottleCommands().size() == 0) {
                                return java.text.MessageFormat.format(
                                    WarrantTableAction.rb.getString("NoCommands"), w.getDisplayName());
                            }
                            return WarrantTableAction.rb.getString("Idle");
                        case Warrant.MODE_LEARN:
                            return java.text.MessageFormat.format(WarrantTableAction.rb.getString("Learning"),
                                                       w.getCurrentBlockOrder().getBlock().getDisplayName());
                        case Warrant.MODE_RUN:
                            String key;
                            if (w.isWaiting()) {
                                key = "Waiting";
                            } else { 
                                key = "Issued";
                            }
                            return java.text.MessageFormat.format(WarrantTableAction.rb.getString(key),
                                                       w.getCurrentBlockOrder().getBlock().getDisplayName());
                    }
                    break;
                case EDIT_COLUMN:
                    return WarrantTableAction.rb.getString("ButtonEdit");
                case DELETE_COLUMN:
                    return WarrantTableAction.rb.getString("ButtonDelete");
            }
            return "";
        }

        public void setValueAt(Object value, int row, int col) {
            if (log.isDebugEnabled()) log.debug("setValueAt: row= "+row+", column= "+col+", value= "+value.getClass().getName());
            Warrant w = (Warrant)getBeanAt(row);
            String msg = null;
            switch (col) {
                case WARRANT_COLUMN:
                case ROUTE_COLUMN:
                case TRAIN_ID_COLUMN:
                case ADDRESS_COLUMN:
                    return;
                case ALLOCATE_COLUMN:
                    int blockIdx = w.allocateRoute();
                    if (blockIdx > -1) {
                        msg = java.text.MessageFormat.format(WarrantTableAction.rb.getString("BlockNotAllocated"), 
                                w.getBlockOrderAt(blockIdx).getBlock().getDisplayName());
                    }
                    break;
                case DEALLOC_COLUMN:
                    if (w.getRunMode() == Warrant.MODE_NONE) {
                        w.deAllocate();
                    } else {
                        msg = java.text.MessageFormat.format(
                                WarrantTableAction.rb.getString("TrainRunning"), w.getDisplayName());
                    }
                    break;
                case SET_COLUMN:
                    blockIdx = w.setRoute(0, null);
                    if (blockIdx > -1) {
                        BlockOrder bo = w.getBlockOrderAt(blockIdx);
                        msg = java.text.MessageFormat.format(WarrantTableAction.rb.getString("RouteNotSet"),
                                            bo.getPathName(), bo.getBlock().getDisplayName());
                    }
                    break;
                case RUN_TRAIN_COLUMN:
                    if (w.getRunMode() == Warrant.MODE_NONE) {
                        DccLocoAddress address = w.getDccAddress();
                        if (address == null) {
                            msg = java.text.MessageFormat.format(
                                WarrantTableAction.rb.getString("NoAddress"), w.getDisplayName());;
                            break;
                        }
                        if (w.getThrottleCommands().size() == 0) {
                            msg = java.text.MessageFormat.format(
                                    WarrantTableAction.rb.getString("NoCommands"), w.getDisplayName());
                            break;
                        }
                        if (w.getOrders().size() == 0) {
                            msg = WarrantTableAction.rb.getString("EmptyRoute");
                            break;
                        }
                        blockIdx = w.setRoute(0, null);
                        if (blockIdx==0) {
                            msg = java.text.MessageFormat.format(WarrantTableAction.rb.getString("OriginBlockNotSet"), 
                                    w.getBlockOrderAt(blockIdx).getBlock().getDisplayName());
                            break;
                        }
                        msg = w.runAutoTrain(true);
                    } else {
                        msg = java.text.MessageFormat.format(
                                WarrantTableAction.rb.getString("TrainRunning"), w.getDisplayName());
                    }
                    break;
                case CONTROL_COLUMN:
                    if (w.getRunMode() == Warrant.MODE_RUN) {
                        String setting = (String)value;
                        int s = -1;
                        if (setting.equals(halt)) {
                            s = Warrant.HALT; 
                        } else if (setting.equals(resume)) {
                            s = Warrant.RESUME; 
                        } else if (setting.equals(abort)) {
                            s = Warrant.ABORT;
                        }
                        w.controlRunTrain(s);
                    } else {
                        msg = java.text.MessageFormat.format(
                                WarrantTableAction.rb.getString("NotRunning"), w.getDisplayName());
                    }
                    getValueAt(row,col);
                    break;
                case EDIT_COLUMN:
                    WarrantTableAction.openWarrantFrame(w.getDisplayName());
                    break;
                case DELETE_COLUMN:
                    if (w.getRunMode() == Warrant.MODE_NONE) {
                        getManager().deregister(w);
                        w.dispose();
                    } else {
                    }
                    break;
            }
            if (msg!=null) {
                JOptionPane.showMessageDialog(null, msg,
                        WarrantTableAction.rb.getString("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            }
            fireTableRowsUpdated(row, row);
        }
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(WarrantTableAction.class.getName());
}
