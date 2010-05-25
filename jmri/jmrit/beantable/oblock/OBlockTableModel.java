package jmri.jmrit.beantable.oblock;

/**
 * GUI to define OBlocks 
 *<P> 
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 * <P>
 *
 * @author	Pete Cressman (C) 2010
 * @version     $Revision: 1.3 $
 */

import java.util.List;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import jmri.Block;
import jmri.InstanceManager;
import jmri.Manager;
import jmri.NamedBean;
import jmri.Path;
import jmri.Sensor;

import jmri.jmrit.beantable.AbstractTableAction;

import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;

    /**
     * Duplicates the JTable model for BlockTableAction and adds a column
     * for the occupancy sensor.  Configured for use within an internal frame.
     */
public class OBlockTableModel extends jmri.jmrit.picker.PickListModel {

    static public final int SYSNAMECOL  = 0;
    static public final int USERNAMECOL = 1;
    static public final int COMMENTCOL = 2;
    static public final int SENSORCOL = 3;
    static public final int LENGTHCOL = 4;
    static public final int UNITSCOL = 5;
    static public final int CURVECOL = 6;
    static public final int EDIT_COL = 7;
    static public final int DELETE_COL = 8;
    static public final int NUMCOLS = 9;

    static public final String noneText = AbstractTableAction.rb.getString("BlockNone");
    static public final String gradualText = AbstractTableAction.rb.getString("BlockGradual");
    static public final String tightText = AbstractTableAction.rb.getString("BlockTight");
    static public final String severeText = AbstractTableAction.rb.getString("BlockSevere");
    static public final String[] curveOptions = {noneText, gradualText, tightText, severeText};

	static final ResourceBundle rbo = ResourceBundle.getBundle("jmri.jmrit.beantable.OBlockTableBundle");

    java.text.DecimalFormat twoDigit = new java.text.DecimalFormat("0.00");

    OBlockManager manager;
    private String[] tempRow= new String[NUMCOLS];
    TableFrames _parent;

    public OBlockTableModel(TableFrames parent) {
        super();
        _parent = parent;
        manager = InstanceManager.oBlockManagerInstance();
        initTempRow();
    }

    void initTempRow() {
        for (int i=0; i<LENGTHCOL; i++) {
            tempRow[i] = null;
        }
        tempRow[LENGTHCOL] = twoDigit.format(0.0);
        tempRow[UNITSCOL] = "";
        tempRow[CURVECOL] = noneText;
    }

    public Manager getManager() {
        return manager;
    }
    public NamedBean getBySystemName(String name) {
        return manager.getBySystemName(name);
    }
    // Method name not appropriate (initial use was for Icon Editors)
    public NamedBean addBean(String name) {
        return manager.getOBlock(name);
    }
    public NamedBean addBean(String sysName, String userName) {
        return manager.createNewOBlock(sysName, userName);
    }
    public boolean canAddBean() {
        return true;
    }

    public int getColumnCount () {
        return NUMCOLS;
    }
    public int getRowCount () {
        return super.getRowCount() + 1;
    }

    String _saveBlockName;
    public Object getValueAt(int row, int col) 
    {
        if (super.getRowCount() == row) {
            if (_saveBlockName!=null && _parent.getBlockTablePane()!=null) {
                //String sysName = tempRow[SYSNAMECOL];
                if (!_saveBlockName.startsWith("OB")) {
                    _saveBlockName = "OB"+_saveBlockName;
                }
                OBlock b = manager.provideOBlock(_saveBlockName.toUpperCase());
                if (b!=null) {
                    int idx =  getIndexOf(b);
                    _parent.getBlockTablePane().getVerticalScrollBar().setValue(idx*TableFrames.ROW_HEIGHT);
                    _saveBlockName = null;
                }
            }
            return tempRow[col];
        }
        OBlock b = (OBlock)getBeanAt(row);
        if (b == null) {
            //log.debug("requested getValueAt(\""+row+"\"), Block doesn't exist");
            return "(no Block)";
        }
        switch (col) {
            case COMMENTCOL:
                return b.getComment();
            case SENSORCOL:
                Sensor s = b.getSensor();
                if (s==null) {
                     return "";
                }
                String uName = s.getUserName();
                String name;
                if (uName != null) {
                    name = uName +" ("+s.getSystemName()+")";
                } else {
                    name = s.getSystemName();
                }
                return name;
            case LENGTHCOL:
                if (b.isMetric()) {
                    return (twoDigit.format(b.getLengthCm()));
                } else {
                    return (twoDigit.format(b.getLengthIn()));
                }
            case UNITSCOL:
                return b.isMetric();
            case CURVECOL:
                String c = "";
                if (b.getCurvature()==Block.NONE) c = noneText;
                else if (b.getCurvature()==Block.GRADUAL) c = gradualText;
                else if (b.getCurvature()==Block.TIGHT) c = tightText;
                else if (b.getCurvature()==Block.SEVERE) c = severeText;
                return c;
            case EDIT_COL:
                return rbo.getString("ButtonEditPath");
            case DELETE_COL:
                return AbstractTableAction.rb.getString("ButtonDelete");
        }
        return super.getValueAt(row, col);
    }    		

    public void setValueAt(Object value, int row, int col) {
        if (log.isDebugEnabled()) log.debug("setValueAt: row= "+row+", col= "+col+", value= "+(String)value);
        if (super.getRowCount() == row) 
        {
            if (col==SYSNAMECOL || col==USERNAMECOL) {
                if (col==SYSNAMECOL) {
                    tempRow[SYSNAMECOL] = (String)value;
                } else {
                    tempRow[USERNAMECOL] = (String)value;
                }
                _saveBlockName = tempRow[SYSNAMECOL];
                OBlock block = manager.createNewOBlock((String)value, tempRow[USERNAMECOL]);
                if (block==null) {
                    block = manager.provideOBlock(tempRow[USERNAMECOL]);
                    String name = "blank";     // zero length string error
                    if (block!=null) {
                        name = block.getDisplayName();
                    }
                    JOptionPane.showMessageDialog(null, java.text.MessageFormat.format(
                        rbo.getString("CreateDuplBlockErr"), name),
                        AbstractTableAction.rb.getString("ErrorTitle"), JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (block!=null) {
                    if (tempRow[SENSORCOL] != null) {
                        Sensor sensor = null;
                        try {
                            sensor = InstanceManager.sensorManagerInstance().provideSensor(tempRow[SENSORCOL]);
                            if (sensor!=null) {
                                block.setSensor(sensor);
                            }
                        } catch (Exception ex) {
                            log.error("No Sensor named \""+(String)value+"\" found. threw exception: "+ ex);
                        }
                        if (sensor==null) {
                            JOptionPane.showMessageDialog(null, java.text.MessageFormat.format(
                                rbo.getString("NoSuchSensorErr"), tempRow[SENSORCOL]),
                                AbstractTableAction.rb.getString("ErrorTitle"), JOptionPane.WARNING_MESSAGE);
                        }
                    }
                    block.setComment(tempRow[COMMENTCOL]);
                    float len = Float.valueOf(tempRow[LENGTHCOL]).floatValue();
                    if (tempRow[UNITSCOL].equals("in")) {
                        block.setLength(len*25.4f);
                        block.setMetricUnits(false);
                    } else {
                        block.setLength(len*10.0f);
                        block.setMetricUnits(true);
                    }
                    if (tempRow[CURVECOL].equals(noneText)) block.setCurvature(Block.NONE);
                    else if (tempRow[CURVECOL].equals(gradualText)) block.setCurvature(Block.GRADUAL);
                    else if (tempRow[CURVECOL].equals(tightText)) block.setCurvature(Block.TIGHT);
                    else if (tempRow[CURVECOL].equals(severeText)) block.setCurvature(Block.SEVERE);
                }  
                //fireTableRowsUpdated(row,row);
                initTempRow();
                fireTableDataChanged();
            } else {
                if (col==UNITSCOL) {
                    if (tempRow[UNITSCOL].equals("in")) {
                        tempRow[UNITSCOL] = "cm";
                    } else {
                        tempRow[UNITSCOL] = "in";
                    }
                } else {
                    tempRow[col] = (String)value;
                }
            }
            return;
        }
        OBlock block = (OBlock)super.getBeanAt(row);
        switch (col) {
            case USERNAMECOL:
                OBlock b = manager.provideOBlock((String)value);
                if (b != null) {
                    JOptionPane.showMessageDialog(null, java.text.MessageFormat.format(
                        rbo.getString("CreateDuplBlockErr"), block.getDisplayName()),
                        AbstractTableAction.rb.getString("ErrorTitle"), JOptionPane.WARNING_MESSAGE);
                    return;
                }
                block.setUserName((String)value);
                fireTableRowsUpdated(row,row);
                return;
            case COMMENTCOL:
                block.setComment((String)value);
                return;
            case SENSORCOL:
                try {
                    if (((String)value).trim().length()==0) {
                        block.setSensor(null);
                        block.setState(OBlock.DARK);
                    } else {
                        Sensor s = InstanceManager.sensorManagerInstance().provideSensor((String)value);
                        if (s!=null) {
                            block.setSensor(s);
                            fireTableRowsUpdated(row,row);
                        }
                    }
                    return;
                } catch (Exception ex) {
                    log.error("provideSensor("+(String)value+") threw exception: "+ ex);
                }
                JOptionPane.showMessageDialog(null, java.text.MessageFormat.format(
                        rbo.getString("NoSuchSensorErr"), (String)value),
                        AbstractTableAction.rb.getString("ErrorTitle"), JOptionPane.WARNING_MESSAGE);
                return;
            case LENGTHCOL:
                float len = Float.valueOf((String)value).floatValue();
                if (block.isMetric()) {
                    block.setLength(len*10.0f);
                } else {
                    block.setLength(len*25.4f);
                }
                fireTableRowsUpdated(row,row);
                return;
            case UNITSCOL:
                block.setMetricUnits(((Boolean)value).booleanValue());
                fireTableRowsUpdated(row,row);
                return;
            case CURVECOL:
                String cName = (String)value;
                if (cName.equals(noneText)) block.setCurvature(Block.NONE);
                else if (cName.equals(gradualText)) block.setCurvature(Block.GRADUAL);
                else if (cName.equals(tightText)) block.setCurvature(Block.TIGHT);
                else if (cName.equals(severeText)) block.setCurvature(Block.SEVERE);
                fireTableRowsUpdated(row,row);
                return;
            case EDIT_COL:
                _parent.openBlockPathFrame(block.getSystemName());
                return;
            case DELETE_COL:
                deleteBean(block);
                block = null;
                return;
        }
        super.setValueAt(value, row, col);					
    }

    public String getColumnName(int col) {
        switch (col) {
            case COMMENTCOL: return AbstractTableAction.rb.getString("Comment");
            case SENSORCOL: return AbstractTableAction.rbean.getString("BeanNameSensor");
            case CURVECOL: return AbstractTableAction.rb.getString("BlockCurveColName");
            case LENGTHCOL: return AbstractTableAction.rb.getString("BlockLengthColName");
            case UNITSCOL: return "";
            case EDIT_COL: return "";
            case DELETE_COL: return "";
        }
        return super.getColumnName(col);
    }

    boolean noWarnDelete = false;

    void deleteBean(OBlock bean) {
        int count = bean.getNumPropertyChangeListeners()-2; // one is this table, other is manager
        if (log.isDebugEnabled()) {
            log.debug("Delete with "+count+" remaining listenner");
            //java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(bean);
            PropertyChangeListener[] listener=((jmri.implementation.AbstractNamedBean)bean).getPropertyChangeListeners();
            for (int i=0; i<listener.length; i++) {
                log.debug(i+") "+listener[i].getClass().getName());
            }
        }
        if (!noWarnDelete) {
            String msg;
            if (count>0) { // warn of listeners attached before delete
                msg = java.text.MessageFormat.format(
                        AbstractTableAction.rb.getString("DeletePrompt")+"\n"
                        +AbstractTableAction.rb.getString("ReminderInUse"),
                        new Object[]{bean.getSystemName(),""+count});
            } else {
                msg = java.text.MessageFormat.format(
                        AbstractTableAction.rb.getString("DeletePrompt"),
                        new Object[]{bean.getSystemName()});
            }

            // verify deletion
            int val = JOptionPane.showOptionDialog(null, 
                    msg, AbstractTableAction.rb.getString("WarningTitle"), 
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                    new Object[]{AbstractTableAction.rb.getString("ButtonYes"),
                                 AbstractTableAction.rb.getString("ButtonYesPlus"),
                                 AbstractTableAction.rb.getString("ButtonNo")},
                    AbstractTableAction.rb.getString("ButtonNo"));
            if (val == 2) return;  // return without deleting
            if (val == 1) { // suppress future warnings
                noWarnDelete = true;
            }
        }
        // finally OK, do the actual delete
        _parent.getPortalModel().deleteBlock(bean);
        List <Path> list = bean.getPaths();
        for (int i=0; i<list.size(); i++) {
            bean.removePath(list.get(i));
        }
        getManager().deregister(bean);
        bean.dispose();
    }

    public Class<?> getColumnClass(int col) {
        if (col == CURVECOL) {
            return JComboBox.class;
        } else if (col==DELETE_COL || col==EDIT_COL) {
            return JButton.class;
        } else if (col==UNITSCOL) {
            return Boolean.class;
        }
        return String.class;
    }

    public int getPreferredWidth(int col) {
        switch (col) {
            case SYSNAMECOL: return new JTextField(15).getPreferredSize().width;
            case USERNAMECOL: return new JTextField(15).getPreferredSize().width;
            case COMMENTCOL: return new JTextField(8).getPreferredSize().width;
            case SENSORCOL: return new JTextField(13).getPreferredSize().width;
            case CURVECOL: return new JTextField(5).getPreferredSize().width;
            case LENGTHCOL: return new JTextField(5).getPreferredSize().width;
            case UNITSCOL: return new JTextField(2).getPreferredSize().width;
            case EDIT_COL: return new JButton("DELETE").getPreferredSize().width;
            case DELETE_COL: return new JButton("DELETE").getPreferredSize().width;
        }
        return 5;
    }

    public boolean isCellEditable(int row, int col) {
        if (super.getRowCount() == row) return true;
        if (col==SYSNAMECOL) return false;
        else return true;
    }

    public void propertyChange(PropertyChangeEvent e) {
        super.propertyChange(e);
        String property = e.getPropertyName();
        _parent.getPortalModel().propertyChange(e);
        _parent.getXRefModel().propertyChange(e);

        if (property.equals("length") || property.equals("UserName")
                            || property.equals("portalCount")) {
            _parent.updateOpenMenu();
        }
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(OBlockTableModel.class.getName());
}
