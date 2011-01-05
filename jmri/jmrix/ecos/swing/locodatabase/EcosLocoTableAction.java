package jmri.jmrix.ecos.swing.locodatabase;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import jmri.NamedBean;
import java.util.List;
import javax.swing.JTable;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.table.TableColumn;
import javax.swing.DefaultCellEditor;
import javax.swing.table.DefaultTableCellRenderer;

import jmri.jmrix.ecos.EcosLocoAddress;
import jmri.jmrix.ecos.EcosLocoAddressManager;
import jmri.jmrix.ecos.EcosSystemConnectionMemo;
import jmri.jmrix.ecos.EcosMessage;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrix.ecos.utilities.EcosLocoToRoster;
import jmri.jmrit.beantable.BeanTableDataModel;

public class EcosLocoTableAction extends jmri.jmrit.beantable.AbstractTableAction {

    /**
     * Create an action with a specific title.
     * <P>
     * Note that the argument is the Action title, not the title of the
     * resulting frame.  Perhaps this should be changed?
     * @param s
     */
   public EcosLocoTableAction(String s) {
        super(s);
    }
    public EcosLocoTableAction() { this("Ecos Loco Table");}
    
    public EcosLocoTableAction(String s, EcosSystemConnectionMemo memo) { 
        this(s);
        setAdapterMemo(memo);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        // create the JTable model, with changes for specific NamedBean
        createModel();

        // create the frame
        f = new jmri.jmrit.beantable.BeanTableFrame(m, helpTarget()){
        };
        setMenuBar(f);
        setTitle();
        addToFrame(f);
        f.pack();
        f.setVisible(true);
    }

    protected EcosSystemConnectionMemo adaptermemo;
    protected EcosLocoAddressManager locoManager;
    protected String rosterAttribute;
    public void setAdapterMemo(EcosSystemConnectionMemo memo) {
        adaptermemo = memo;
        locoManager = adaptermemo.getLocoAddressManager();
        rosterAttribute = adaptermemo.getPreferenceManager().getRosterAttribute();
    }
    
    protected EcosLocoAddress getByEcosObject(String object) {return locoManager.getByEcosObject(object);}
    
    List<String> ecosObjectIdList = null;
    
    protected void createModel() {
        m = new BeanTableDataModel() {
        
            static public final int ADDTOROSTERCOL = 5;
            static public final int SPEEDDIR = 6;
            static public final int STOP = 7;

            //We have to set a manager first off, but this gets replaced.
            protected EcosLocoAddressManager getManager() { return locoManager;}
            protected String getRosterAttribute() { return rosterAttribute; }

            
            /*public EcosLocoAddress getByDccAddress(int address) {return getManager().getByDccAddress(address);}*/
            
            public String getValue(String s) {
                return "Set";
            }
            
            public int getDisplayDeleteMsg() { return -1; }
            public void setDisplayDeleteMsg(int boo) {  }
            public void clickOn(jmri.NamedBean t) { }
            
            @Override
            protected synchronized void updateNameList() {
                // first, remove listeners from the individual objects
                if (ecosObjectIdList != null) {
                    for (int i = 0; i< ecosObjectIdList.size(); i++) {
                        // if object has been deleted, it's not here; ignore it
                        jmri.jmrix.ecos.EcosLocoAddress b = getByEcosObject(ecosObjectIdList.get(i));
                        if (b!=null) b.removePropertyChangeListener(this);
                    }
                }
                ecosObjectIdList = getManager().getEcosObjectList();
                // and add them back in
                for (int i = 0; i< ecosObjectIdList.size(); i++)
                    getByEcosObject(ecosObjectIdList.get(i)).addPropertyChangeListener(this);
            }
            
            @Override
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                updateNameList();
                if (e.getPropertyName().equals("length")) {
                    // a new jmri.jmrix.ecos.EcosLocoAddressManager is available in the manager
                    updateNameList();
                    //log.debug("Table changed length to "+ecosObjectIdList.size());
                    fireTableDataChanged();
                } else if (matchPropertyName(e)) {
                    // a value changed.  Find it, to avoid complete redraw
                    String object = ((jmri.jmrix.ecos.EcosLocoAddress)e.getSource()).getEcosObject();
                    //if (log.isDebugEnabled()) log.debug("Update cell "+ecosObjectIdList.indexOf(name)+","
                     //                                   +VALUECOL+" for "+name);
                    // since we can add columns, the entire row is marked as updated
                    int row = ecosObjectIdList.indexOf(object);
                    fireTableRowsUpdated(row, row);
                }
            }
            @Override
            public int getColumnCount(){ 
                if (showLocoMonitor)
                    return STOP+1;
                else
                    return ADDTOROSTERCOL+1;
            }
            
            @Override
            public void setValueAt(Object value, int row, int col) {
                if (col==COMMENTCOL) {
                    List<RosterEntry> l;
                    if (value==null)
                        return;
                    if (value.equals(" ")){
                        l = Roster.instance().getEntriesWithAttributeKeyValue(getRosterAttribute(), ecosObjectIdList.get(row));
                        if(l.size()!=0){
                            l.get(0).deleteAttribute(getRosterAttribute());
                            getByEcosObject(ecosObjectIdList.get(row)).setRosterId((String) value);
                            l.get(0).updateFile();
                        }
                    } else{
                        l = Roster.instance().matchingList(null, null, null, null, null, null, (String) value);
                        for (int i = 0; i < l.size(); i++) {
                            if ((l.get(i).getAttribute(getRosterAttribute())==null)||(l.get(i).getAttribute(getRosterAttribute()).equals(""))){
                                l.get(i).putAttribute(getRosterAttribute(), ecosObjectIdList.get(row));
                                getByEcosObject(ecosObjectIdList.get(row)).setRosterId((String) value);
                            } else{
                                value=null;
                                //return;
                            }
                            l.get(i).updateFile();
                        }
                    }
                    fireTableRowsUpdated(row, row);
                    Roster.writeRosterFile();

                }  else if (col==ADDTOROSTERCOL) {
                    addToRoster(row, col);
                } else if (col==STOP){
                    stopLoco(row, col);
                }
            }
            
            /**
             * Is this property event announcing a change this table should display?
             * <P>
             * Note that events will come both from the jmri.jmrix.ecos.EcosLocoAddressManagers and also from the manager
             */
            protected boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
                refreshSelections();
                return true;
            }
            
            @Override
            public String getColumnName(int col) {
                 switch (col) {
                case SYSNAMECOL: return "ECoS Object Id";
                case USERNAMECOL: return "ECoS Descritpion";
                case VALUECOL: return "ECoS Address";
                case COMMENTCOL: return "JMRI Roster Id";
                case DELETECOL: return "ECOS Protocol";
                case ADDTOROSTERCOL: return "Add to Roster";
                case SPEEDDIR: return "Speed Direction";
                case STOP: return "Stop";
                default: return "unknown";
                }
            }
            
            @Override
            public Class<?> getColumnClass(int col) {
                switch (col) {
                case SYSNAMECOL:
                case USERNAMECOL:
                case VALUECOL:
                case DELETECOL:
                case SPEEDDIR:
                    return String.class;
                case ADDTOROSTERCOL:
                case STOP:
                    return JButton.class;
                case COMMENTCOL:
                  return JComboBox.class;
                default:
                    return null;
                }
            }
            
            @Override
            public boolean isCellEditable(int row, int col) {
                switch (col) {
                case COMMENTCOL:
                    return true;
                case ADDTOROSTERCOL:
                    if (getValueAt(row, COMMENTCOL)==null)
                        return true;
                    else
                        return false;
                case STOP:
                    return true;
                default:
                    return false;
                }
            }
            
            JComboBox selections;
    
            public void setUpCOMMENTCOL(TableColumn Rosterid){
                selections = Roster.instance().fullRosterComboBoxGlobal();
                selections.insertItemAt(" ",0);
                selections.setSelectedIndex(-1);
                Rosterid.setCellEditor(new DefaultCellEditor(selections));
                
                DefaultTableCellRenderer renderer =
                        new DefaultTableCellRenderer();
                renderer.setToolTipText("Click for combo box");
                Rosterid.setCellRenderer(renderer);
            }
            
            public void refreshSelections(){
                if (selections==null)
                    return;
                Roster.instance().updateComboBoxGlobal(selections);
                selections.insertItemAt(" ",0);
                selections.setSelectedIndex(-1);
                fireTableRowsUpdated(0, getRowCount());
            }
        
            @Override
            public int getPreferredWidth(int col) {
                switch (col) {
                case SYSNAMECOL:
                    return new JTextField(5).getPreferredSize().width;
                case COMMENTCOL:
                    return 75;
                case USERNAMECOL:
                    return new JTextField(20).getPreferredSize().width;
                case ADDTOROSTERCOL: // not actually used due to the configureTable, setColumnToHoldButton, configureButton
                    return new JTextField(22).getPreferredSize().width;
                case STOP: // not actually used due to the configureTable, setColumnToHoldButton, configureButton
                    return new JTextField(22).getPreferredSize().width;
                case VALUECOL: 
                    return new JTextField(5).getPreferredSize().width;
                case SPEEDDIR: 
                    return new JTextField(5).getPreferredSize().width;
                default:
                    //log.warn("Unexpected column in getPreferredWidth: "+col);
                    return new JTextField(8).getPreferredSize().width;
                }
            }
    
            public void configureTable(JTable table) {
                setUpCOMMENTCOL(table.getColumnModel().getColumn(COMMENTCOL));
                setColumnToHoldButton(table, ADDTOROSTERCOL, 
                        new JButton("Add to Roster"));
                if(showLocoMonitor)
                    setColumnToHoldButton(table,STOP,stopButton());
                super.configureTable(table);
            }
            public NamedBean getBySystemName(String name) { return null;}
            public NamedBean getByUserName(String name) { return null;}
            
            synchronized public void dispose() {
                if(showLocoMonitor){
                    locoManager.monitorLocos(false);
                    showLocoMonitor=false;
                }
                getManager().removePropertyChangeListener(this);
                if (ecosObjectIdList != null) {
                    for (int i = 0; i< ecosObjectIdList.size(); i++) {
                        jmri.jmrix.ecos.EcosLocoAddress b = getByEcosObject(ecosObjectIdList.get(i));
                        if (b!=null) b.removePropertyChangeListener(this);
                    }
                }
                selections=null;
            }
            
            public int getRowCount() {
                return ecosObjectIdList.size();
            }
            
            @Override
            public Object getValueAt(int row, int col) {
                // some error checking
                if (row >= ecosObjectIdList.size()){
                    log.debug("row is greater than list size");
                    return null;
                }
                jmri.jmrix.ecos.EcosLocoAddress b;
                switch (col) {
                case SYSNAMECOL:
                    return ecosObjectIdList.get(row);
                case USERNAMECOL:  // return user name
                    // sometimes, the TableSorter invokes this on rows that no longer exist, so we check
                    b = getByEcosObject(ecosObjectIdList.get(row));
                    return (b!=null) ? b.getEcosDescription() : null;
                case VALUECOL:  //
                    b = getByEcosObject(ecosObjectIdList.get(row));
                    return (b!=null) ? b.getEcosLocoAddress() : null;
                case COMMENTCOL:
                    b = getByEcosObject(ecosObjectIdList.get(row));
                    if (b!=null){
                        if (b.getRosterId()!=null){
                            return b.getRosterId();
                        }
                    }
                    return (b!=null) ? b.getRosterId() : null;
                case DELETECOL:
                    b = getByEcosObject(ecosObjectIdList.get(row));
                    return (b!=null) ? b.getProtocol() : null;
                case ADDTOROSTERCOL:  //
                    if (getValueAt(row, COMMENTCOL)==null)
                        return "Add To Roster";
                    else
                        return " ";
                case STOP:
                    return "Stop";
                case SPEEDDIR:
                    b = getByEcosObject(ecosObjectIdList.get(row));
                    return (b!=null) ? (b.getDirectionAsString() + " : " + b.getSpeed()) : null;
                    
                default:
                    //log.error("internal state inconsistent with table requst for "+row+" "+col);
                    return null;
                }
            }

            
        };
    }
    
    boolean showLocoMonitor = false;
    void showMonitorChanged() {
        showLocoMonitor = showMonitorLoco.isSelected();
        locoManager.monitorLocos(showLocoMonitor);
        m.fireTableStructureChanged(); // update view
    }
    
    JCheckBox showMonitorLoco = new JCheckBox("Monitor Loco Speed");
    
    /**
     * Create a JButton to edit a turnout operation. 
     * @return	the JButton
     */
    protected JButton stopButton() {
        JButton stopButton = new JButton("STOP");
        return(stopButton);
    }
    
    void stopLoco(int row, int col){
    
        String objectNumber = ecosObjectIdList.get(row);
        EcosMessage m;
        //We will repeat this three times to make sure it gets through.
        for (int x = 0 ;x<3 ; x++){
            m = new EcosMessage("request("+objectNumber+", control, force)");
            adaptermemo.getTrafficController().sendEcosMessage(m, null);
            m = new EcosMessage("set("+objectNumber+", stop)");
            adaptermemo.getTrafficController().sendEcosMessage(m, null);
            m = new EcosMessage("release("+objectNumber+", control)");
            adaptermemo.getTrafficController().sendEcosMessage(m, null);
        }
    }
    
    public void addToPanel(EcosLocoTableTabAction f) {
        f.addToBottomBox(showMonitorLoco, adaptermemo.getUserName());
        showMonitorLoco.setToolTipText("Show extra columns for configuring turnout feedback?");
        showMonitorLoco.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    showMonitorChanged();
                }
            });
    }    
    void addToRoster(int row, int col){
        if (locoManager.getByEcosObject(ecosObjectIdList.get(row)).getRosterId()==null){
            EcosLocoToRoster addLoco = new EcosLocoToRoster();
            addLoco.ecosLocoToRoster(ecosObjectIdList.get(row), adaptermemo);
            m.fireTableRowsUpdated(row, row);
        }
    }

    protected void setTitle() {
        f.setTitle("Ecos Loco Table");
    }

    @Override
    protected String helpTarget() {
        return "package.jmri.jmrix.ecos.ecosLocoTable";
    }
    
    protected void addPressed(ActionEvent e){ }
    
    static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EcosLocoTableAction.class.getName());
}