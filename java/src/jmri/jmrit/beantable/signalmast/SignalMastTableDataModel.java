// SignalMastTableDataModel.java

package jmri.jmrit.beantable.signalmast;

import org.apache.log4j.Logger;
import jmri.*;

import jmri.jmrit.beantable.BeanTableDataModel;
import jmri.InstanceManager;
import javax.swing.*;
import java.util.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import jmri.jmrit.beantable.SignalMastTableAction.MyComboBoxEditor;
import jmri.jmrit.beantable.SignalMastTableAction.MyComboBoxRenderer;
import jmri.jmrit.signalling.SignallingSourceAction;
import jmri.util.com.sun.TableSorter;
import jmri.util.swing.XTableColumnModel;

/**
 * Data model for a SignalMastTable
 *
 * @author	Bob Jacobsen    Copyright (C) 2003, 2009
 * @version     $Revision$
 */

public class SignalMastTableDataModel extends BeanTableDataModel {

    static public final int EDITMASTCOL = NUMCOLUMN;
    static public final int EDITLOGICCOL = EDITMASTCOL+1;
    static public final int LITCOL = EDITLOGICCOL+1;
    static public final int HELDCOL = LITCOL+1;

    public String getValue(String name) {
        return InstanceManager.signalMastManagerInstance().getBySystemName(name).getAspect();
    }

    public int getColumnCount( ){ return NUMCOLUMN+4;}
    public String getColumnName(int col) {
        if (col==VALUECOL) return "Aspect";
        else if (col==EDITMASTCOL) return "Edit";
        else if (col==EDITLOGICCOL) return "Edit Logic";
        else if (col==LITCOL) return "Lit";
        else if (col==HELDCOL) return "Held";
        else return super.getColumnName(col);
    }
    public Class<?> getColumnClass(int col) {
        if (col==VALUECOL) return JComboBox.class;
        else if (col==EDITMASTCOL) return JButton.class;
        else if (col==EDITLOGICCOL) return JButton.class;
        else if (col==LITCOL) return Boolean.class;
        else if (col==HELDCOL) return Boolean.class;
        else return super.getColumnClass(col);
    }
    public int getPreferredWidth(int col) {
        if (col==LITCOL) return new JTextField(4).getPreferredSize().width;
        else if (col==HELDCOL) return new JTextField(4).getPreferredSize().width;
        else if (col==EDITLOGICCOL) return new JTextField(8).getPreferredSize().width;
        else if (col==EDITMASTCOL) return new JTextField(8).getPreferredSize().width;
        else return super.getPreferredWidth(col);
    }
    public boolean isCellEditable(int row, int col) {
        if (col==LITCOL) return true;
        else if (col==EDITLOGICCOL) return true;
        else if (col==EDITMASTCOL){
            String name = sysNameList.get(row);
            SignalMast s = InstanceManager.signalMastManagerInstance().getBySystemName(name);
            if(s instanceof jmri.implementation.TurnoutSignalMast)
                return true;
            else return true;
        }
        else if (col==HELDCOL) return true;
        else return super.isCellEditable(row,col);
    }


    protected Manager getManager() { return InstanceManager.signalMastManagerInstance(); }
    protected NamedBean getBySystemName(String name) { return InstanceManager.signalMastManagerInstance().getBySystemName(name);}
    protected NamedBean getByUserName(String name) { return InstanceManager.signalMastManagerInstance().getByUserName(name);}
    protected String getMasterClassName() { return getClassName(); }

    
    protected void clickOn(NamedBean t) {

    }

    public Object getValueAt(int row, int col) {
        // some error checking
        if (row >= sysNameList.size()){
            log.debug("row is greater than name list");
            return "error";
        }
        String name = sysNameList.get(row);
        SignalMast s = InstanceManager.signalMastManagerInstance().getBySystemName(name);
        if (s==null) return Boolean.valueOf(false); // if due to race condition, the device is going away
        if (col==LITCOL) {
            boolean val = s.getLit();
            return Boolean.valueOf(val);
        }
        else if (col==HELDCOL) {
            boolean val = s.getHeld();
            return Boolean.valueOf(val);
        }
        else if (col==EDITLOGICCOL) {
            return rb.getString("EditSignalLogicButton");
        }        
        else if (col==EDITMASTCOL) {
            if(s instanceof jmri.implementation.TurnoutSignalMast)
                return rb.getString("ButtonEdit");
            return rb.getString("ButtonEdit");
        }
        else return super.getValueAt(row, col);
    }
 
    public void setValueAt(Object value, int row, int col) {
        String name = sysNameList.get(row);
        SignalMast s = InstanceManager.signalMastManagerInstance().getBySystemName(name);
        if (s==null) return;  // device is going away anyway

        if (col==VALUECOL) {
        	if ((String)value != null){
	            s.setAspect((String)value);
	            fireTableRowsUpdated(row, row);
        	}
        } 
        else if (col==LITCOL) {
            boolean b = ((Boolean)value).booleanValue();
            s.setLit(b);
        }
        else if (col==HELDCOL) {
            boolean b = ((Boolean)value).booleanValue();
            s.setHeld(b);
        }
        else if (col==EDITLOGICCOL){
            editLogic(row, col);
        }
        else if (col==EDITMASTCOL){
            editMast(row, col);
        }
        else super.setValueAt(value, row, col);
    }
    
    void editLogic(int row, int col){
        class WindowMaker implements Runnable {
            int row;
            WindowMaker(int r){
                row = r;
            }
            public void run() {
                SignallingSourceAction action = new SignallingSourceAction(rb.getString("TitleSignalMastLogicTable"), (SignalMast) getBySystemName(sysNameList.get(row)));
                action.actionPerformed(null);
            }
        }
        WindowMaker t = new WindowMaker(row);
        javax.swing.SwingUtilities.invokeLater(t);
    }
    
    void editMast(int row, int col){
        class WindowMaker implements Runnable {
            int row;
            WindowMaker(int r){
                row = r;
            }
            public void run() {
                AddSignalMastJFrame editFrame = new jmri.jmrit.beantable.signalmast.AddSignalMastJFrame((SignalMast) getBySystemName(sysNameList.get(row)));
                editFrame.setVisible(true);
            }
        }
        WindowMaker t = new WindowMaker(row);
        javax.swing.SwingUtilities.invokeLater(t);
    }
    
    TableSorter sorter;

    public JTable makeJTable(TableSorter srtr) {
        this.sorter = srtr;
        JTable table = new JTable(sorter)  {
            public boolean editCellAt(int row, int column, java.util.EventObject e) {
                boolean res = super.editCellAt(row, column, e);
                java.awt.Component c = this.getEditorComponent();
                if (c instanceof javax.swing.JTextField) {
                    ( (JTextField) c).selectAll();
                }
                return res;
            }
            public TableCellRenderer getCellRenderer(int row, int column) {
                if (column == VALUECOL) {
                    return getRenderer(row);
                } else
                    return super.getCellRenderer(row, column);
            }
            public TableCellEditor getCellEditor(int row, int column) {
                if (column == VALUECOL) {
                    return getEditor(row);
                } else
                    return super.getCellEditor(row, column);
            }
            TableCellRenderer getRenderer(int row) {
                TableCellRenderer retval = rendererMap.get(sorter.getValueAt(row,SYSNAMECOL));
                if (retval == null) {
                    // create a new one with right aspects
                    retval = new MyComboBoxRenderer(getAspectVector(row));
                    rendererMap.put(sorter.getValueAt(row,SYSNAMECOL), retval);
                }
                return retval;
            }
            Hashtable<Object, TableCellRenderer> rendererMap = new Hashtable<Object, TableCellRenderer>();

            TableCellEditor getEditor(int row) {
                TableCellEditor retval = editorMap.get(sorter.getValueAt(row,SYSNAMECOL));
                if (retval == null) {
                    // create a new one with right aspects
                    retval = new MyComboBoxEditor(getAspectVector(row));
                    editorMap.put(sorter.getValueAt(row,SYSNAMECOL), retval);
                }
                return retval;
            }
            Hashtable<Object, TableCellEditor> editorMap = new Hashtable<Object, TableCellEditor>();

            Vector<String> getAspectVector(int row) {
                Vector<String> retval = boxMap.get(sorter.getValueAt(row,SYSNAMECOL));
                if (retval == null) {
                    // create a new one with right aspects
                    Vector<String> v = InstanceManager.signalMastManagerInstance()
                                        .getSignalMast((String)sorter.getValueAt(row,SYSNAMECOL)).getValidAspects();
                    retval = v;
                    boxMap.put(sorter.getValueAt(row,SYSNAMECOL), retval);
                }
                return retval;
            }
            Hashtable<Object, Vector<String>> boxMap = new Hashtable<Object, Vector<String>>();
            
        };
        
        table.getTableHeader().setReorderingAllowed(true);
        table.setColumnModel(new XTableColumnModel());
        table.createDefaultColumnsFromModel();
        
        addMouseListenerToHeader(table);
        return table;
    }
    
    protected String getBeanType(){
        return rbean.getString("BeanNameSignalMast");
    }
    
        		 
    protected boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
        if(e.getPropertyName().indexOf("Aspect")>=0 || e.getPropertyName().indexOf("Lit")>=0 
		        || e.getPropertyName().indexOf("Held")>=0 || e.getPropertyName().indexOf("aspectDisabled")>=0
                || e.getPropertyName().indexOf("aspectEnabled")>=0){
                
            return true;
        }
        return super.matchPropertyName(e);
    }
    
    protected String getClassName() { return jmri.jmrit.beantable.SignalMastTableAction.class.getName(); }
    
    public static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.beantable.BeanTableBundle");
    public String getClassDescription() { return rb.getString("TitleSignalMastTable"); }
    
    static final ResourceBundle rbean = ResourceBundle.getBundle("jmri.NamedBeanBundle");
    static final Logger log = Logger.getLogger(SignalMastTableDataModel.class.getName());

}

/* @(#)SignalMastTableDataModel.java */
