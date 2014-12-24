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
 * @version     $Revision$
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import javax.swing.table.AbstractTableModel;

import jmri.InstanceManager;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OPath;
import jmri.jmrit.logix.Portal;
import jmri.jmrit.logix.PortalManager;

public class BlockPathTableModel extends AbstractTableModel implements PropertyChangeListener {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = -2472819814795605641L;
	public static final int FROM_PORTAL_COLUMN = 0;
    public static final int NAME_COLUMN = 1;
    public static final int TO_PORTAL_COLUMN = 2;
    public static final int EDIT_COL = 3;
    public static final int DELETE_COL = 4;
    public static final int NUMCOLS = 5;

    private String[] tempRow= new String[NUMCOLS];

    private TableFrames _parent;
    private OBlock _block;

    public BlockPathTableModel() {
        super();
    }

    public BlockPathTableModel(OBlock block, TableFrames parent) {
        super();
        _block = block;
        _parent = parent;
    }

    public void init() {
        initTempRow();
        _block.addPropertyChangeListener(this);
    }

        
    public void removeListener() {
        if (_block==null) return;
        try {
            _block.removePropertyChangeListener(this);
        } catch (NullPointerException npe) { // OK when block is removed
        }
    }
    protected OBlock getBlock() {
        return _block;
    }

     void initTempRow() {
        for (int i=0; i<NUMCOLS; i++) {
            tempRow[i] = null;
        }
        tempRow[DELETE_COL] = Bundle.getMessage("ButtonClear");
    }

    public int getColumnCount () {
        return NUMCOLS;
    }

    public int getRowCount() {
        return _block.getPaths().size() + 1;
    }

    public String getColumnName(int col) {
        switch (col) {
            case FROM_PORTAL_COLUMN: return Bundle.getMessage("FromPortal");
            case NAME_COLUMN: return Bundle.getMessage("PathName");
            case TO_PORTAL_COLUMN: return Bundle.getMessage("ToPortal");
        }
        return "";
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        if (_block.getPaths().size() == rowIndex) {
            return tempRow[columnIndex];
        }
        OPath path = (OPath)_block.getPaths().get(rowIndex);
        switch(columnIndex) {
            case FROM_PORTAL_COLUMN:
                Portal portal = path.getFromPortal();
                if (portal==null) {
                    return "";
                } else {
                    return portal.getName();
                }
            case NAME_COLUMN:
                return path.getName();
            case TO_PORTAL_COLUMN:
                portal = path.getToPortal();
                if (portal==null) {
                    return "";
                } else {
                    return portal.getName();
                }
            case EDIT_COL:
                return Bundle.getMessage("ButtonEditTO");
            case DELETE_COL:
                return Bundle.getMessage("ButtonDelete");
        }
        return "";
    }

    public void setValueAt(Object value, int row, int col) {
        String strValue = (String)value;
        if (strValue!=null && strValue.trim().length()==0) {
            strValue =null;
        }
        String msg = null;
        if (_block.getPaths().size() == row) {
            if (col==NAME_COLUMN) {
                if (_block.getPathByName(strValue)!=null) {
                    msg = Bundle.getMessage("DuplPathName", strValue);
                    tempRow[col] = strValue;
                } else {
                    Portal fromPortal = _block.getPortalByName(tempRow[FROM_PORTAL_COLUMN]);
                    Portal toPortal = _block.getPortalByName(tempRow[TO_PORTAL_COLUMN]);
                    OPath path = new OPath(strValue, _block, fromPortal, toPortal, null);

                    if (!_block.addPath(path)) {
                        msg = Bundle.getMessage("AddPathFailed", strValue);
                        tempRow[col] = strValue;
                    } else {
                        initTempRow();
                        _parent.updateOpenMenu();
                    }
                }
                fireTableDataChanged();
            } else if (col==DELETE_COL) {
            	initTempRow();
                fireTableRowsUpdated(row,row);
            }else {
                tempRow[col] = strValue;
            }
            if (msg != null) {
                JOptionPane.showMessageDialog(null, msg,
                        Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            }
            return;
        }

        OPath path =(OPath)_block.getPaths().get(row);

        switch(col) {
            case FROM_PORTAL_COLUMN:
                if (strValue!=null) {
                    Portal portal = _block.getPortalByName(strValue);
                    PortalManager portalMgr = InstanceManager.getDefault(PortalManager.class);
                    if (portal == null || portalMgr.getPortal(strValue)==null) {
                        int response = JOptionPane.showConfirmDialog(null, 
                        		Bundle.getMessage("BlockPortalConflict", value, _block.getDisplayName()),
                        		Bundle.getMessage("WarningTitle"), JOptionPane.YES_NO_OPTION, 
                        		JOptionPane.WARNING_MESSAGE);
                        if (response==JOptionPane.NO_OPTION) {
                            break;
                        }
                        portal = portalMgr.providePortal(strValue);
                        if (portal==null) {
                            msg = Bundle.getMessage("NoSuchPortalName", strValue);
                            break;
                        } else {
                            if ( !portal.setFromBlock(_block, false)) {
                                response = JOptionPane.showConfirmDialog(null, 
                                		Bundle.getMessage("BlockPathsConflict", value, portal.getFromBlockName()),
                                		Bundle.getMessage("WarningTitle"), JOptionPane.YES_NO_OPTION, 
                                		JOptionPane.WARNING_MESSAGE);
                                if (response==JOptionPane.NO_OPTION) {
                                    break;
                                }

                            }
                            portal.setFromBlock(_block, true);
                            _parent.getPortalModel().fireTableDataChanged();
                        }
                    }
                    path.setFromPortal(portal);
                    if (!portal.addPath(path)) {
                        msg = Bundle.getMessage("AddPathFailed", strValue);
                    }
                } else {
                    path.setFromPortal(null);
                }
                fireTableRowsUpdated(row,row);
                break;
            case NAME_COLUMN:
                if (strValue!=null) {
                    if (_block.getPathByName(strValue)!=null) {
                        msg = Bundle.getMessage("DuplPathName", strValue); 
                     }
                    path.setName(strValue);
                    fireTableRowsUpdated(row,row);
                }
                break;
            case TO_PORTAL_COLUMN:
                if (strValue!=null) {
                    PortalManager portalMgr = InstanceManager.getDefault(PortalManager.class);
                    Portal portal = _block.getPortalByName(strValue);
                    if (portal == null || portalMgr.getPortal(strValue)==null) {
                        int response = JOptionPane.showConfirmDialog(null, 
                        		Bundle.getMessage("BlockPortalConflict", value, _block.getDisplayName()),
                        		Bundle.getMessage("WarningTitle"), JOptionPane.YES_NO_OPTION, 
                        		JOptionPane.WARNING_MESSAGE);
                        if (response==JOptionPane.NO_OPTION) {
                            break;
                        }
                        portal = portalMgr.providePortal(strValue);
                        if (portal==null) {
                            msg = Bundle.getMessage("NoSuchPortalName", strValue);
                            break;
                        } else {
                            if ( !portal.setToBlock(_block, false)) {
                                response = JOptionPane.showConfirmDialog(null, 
                                		Bundle.getMessage("BlockPathsConflict", value, portal.getToBlockName()),
                                    Bundle.getMessage("WarningTitle"), JOptionPane.YES_NO_OPTION, 
                                    JOptionPane.WARNING_MESSAGE);
                                if (response==JOptionPane.NO_OPTION) {
                                    break;
                                }

                            }
                            portal.setToBlock(_block, true);
                            _parent.getPortalModel().fireTableDataChanged();
                        }
                    }
                    path.setToPortal(portal);
                    if (!portal.addPath(path)) {
                        msg = Bundle.getMessage("AddPathFailed", strValue);
                    }
                } else {
                    path.setToPortal(null);
                }
                fireTableRowsUpdated(row,row);
                break;
            case EDIT_COL:
                _parent.openPathTurnoutFrame(_parent.makePathTurnoutName(
                                                _block.getSystemName(), path.getName()));
                break;
            case DELETE_COL:
                if (deletePath(path)) { fireTableDataChanged(); }


        }
        if (msg != null) {
            JOptionPane.showMessageDialog(null, msg,
                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
        }
    }

    boolean deletePath(OPath path) {
        if (JOptionPane.showConfirmDialog(null, Bundle.getMessage("DeletePathConfirm",
                    path.getName()), Bundle.getMessage("WarningTitle"),
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)
                ==  JOptionPane.YES_OPTION) {
            _block.removePath(path);
            return true;
        }
        return false;
    }

    public boolean isCellEditable(int row, int col) {
        return true;
    }

    public Class<?> getColumnClass(int col) {
        if (col==DELETE_COL || col==EDIT_COL) {
            return JButton.class;
        }
        return String.class;
    }

    public int getPreferredWidth(int col) {
        switch (col) {
            case FROM_PORTAL_COLUMN:
            case NAME_COLUMN: 
            case TO_PORTAL_COLUMN:
                return new JTextField(18).getPreferredSize().width;
            case EDIT_COL:
                return new JButton("TURNOUT").getPreferredSize().width;
            case DELETE_COL: 
                return new JButton("DELETE").getPreferredSize().width;
        }
        return 5;
    }

    public OPath getPathByName(String name) {
        for (int i=0; i<_block.getPaths().size(); i++) {
            OPath path = (OPath)_block.getPaths().get(i);
            if (name.equals(path.getName()) ) {
                return path;
            }
        }
        return null;
    }

    public void propertyChange(PropertyChangeEvent e) {
        if (_block.equals(e.getSource())) {
            String property = e.getPropertyName();
            if (log.isDebugEnabled()) log.debug("propertyChange \""+property+"\".  source= "+e.getSource());
            if (property.equals("portalCount") || property.equals("pathCount")) {
                fireTableDataChanged();
            }
       }
    }

    static Logger log = LoggerFactory.getLogger(BlockPathTableModel.class.getName());
}
