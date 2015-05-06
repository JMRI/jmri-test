package jmri.jmrit.beantable.oblock;

/**
 * GUI to define OBlocks
 * <P>
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <P>
 *
 * @author	Pete Cressman (C) 2010
 * @version $Revision$
 */
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.logix.Portal;
import jmri.jmrit.logix.PortalManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SignalTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1150140866074591437L;
    public static final int NAME_COLUMN = 0;
    public static final int FROM_BLOCK_COLUMN = 1;
    public static final int PORTAL_COLUMN = 2;
    public static final int TO_BLOCK_COLUMN = 3;
    public static final int TIME_OFFSET = 4;
    static public final int DELETE_COL = 5;
    public static final int NUMCOLS = 6;

    private ArrayList<SignalRow> _signalList = new ArrayList<SignalRow>();
    PortalManager _portalMgr;

    static class SignalRow {

        NamedBean _signal;
        OBlock _fromBlock;
        Portal _portal;
        OBlock _toBlock;
        long _delayTime;

        SignalRow(NamedBean signal, OBlock fromBlock, Portal portal, OBlock toBlock, long delayTime) {
            _signal = signal;
            _fromBlock = fromBlock;
            _portal = portal;
            _toBlock = toBlock;
            _delayTime = delayTime;
        }
/*        SignalRow(String[] tempRow) {
            _signal = tempRow[NAME_COLUMN];
            _fromBlock = tempRow[FROM_BLOCK_COLUMN];
            _portal = tempRow[PORTAL_COLUMN];
            _toBlock = tempRow[TO_BLOCK_COLUMN];
            _delayTime = tempRow[TIME_OFFSET];
        }*/

        void setSignal(NamedBean signal) {
            _signal = signal;
        }

        NamedBean getSignal() {
            return _signal;
        }

        void setFromBlock(OBlock fromBlock) {
            _fromBlock = fromBlock;
        }

        OBlock getFromBlock() {
            return _fromBlock;
        }

        void setPortal(Portal portal) {
            _portal = portal;
        }

        Portal getPortal() {
            return _portal;
        }

        void setToBlock(OBlock toBlock) {
            _toBlock = toBlock;
        }

        OBlock getToBlock() {
            return _toBlock;
        }

        void setDelayTime(long time) {
            _delayTime = time;
        }

        long getDelayTime() {
            return _delayTime;
        }
    }

    private String[] tempRow = new String[NUMCOLS];

    TableFrames _parent;

    public SignalTableModel(TableFrames parent) {
        super();
        _parent = parent;
        _portalMgr = InstanceManager.getDefault(PortalManager.class);
    }

    public void init() {
        makeList();
        initTempRow();
    }

    void initTempRow() {
        for (int i = 0; i < NUMCOLS; i++) {
            tempRow[i] = null;
        }
        tempRow[TIME_OFFSET] = "0.0";
        tempRow[DELETE_COL] = Bundle.getMessage("ButtonClear");
    }

    private void makeList() {
        ArrayList<SignalRow> tempList = new ArrayList<SignalRow>();
        // collect signals entered into Portals
        String[] sysNames = _portalMgr.getSystemNameArray();
        for (int i = 0; i < sysNames.length; i++) {
            Portal portal = _portalMgr.getBySystemName(sysNames[i]);
            NamedBean signal = portal.getFromSignal();
            SignalRow sr = null;
            if (signal != null) {
                sr = new SignalRow(signal, portal.getFromBlock(), portal,
                        portal.getToBlock(), portal.getFromSignalDelay());
                addToList(tempList, sr);
            }
            signal = portal.getToSignal();
            if (signal != null) {
                sr = new SignalRow(signal, portal.getToBlock(), portal,
                        portal.getFromBlock(), portal.getToSignalDelay());
                addToList(tempList, sr);
            }
        }
        _signalList = tempList;
        if (log.isDebugEnabled()) {
            log.debug("makeList exit: _signalList has "
                    + _signalList.size() + " rows.");
        }
    }

    static private void addToList(List<SignalRow> tempList, SignalRow sr) {
        // not in list, for the sort, insert at correct position
        boolean add = true;
        for (int j = 0; j < tempList.size(); j++) {
            if (sr.getSignal().getDisplayName().compareTo(tempList.get(j).getSignal().getDisplayName()) < 0) {
                tempList.add(j, sr);
                add = false;
                break;
            }
        }
        if (add) {
            tempList.add(sr);
        }
    }

    private String checkSignalRow(SignalRow sr) {
        Portal portal = sr.getPortal();
        OBlock fromBlock = sr.getFromBlock();
        OBlock toBlock = sr.getToBlock();
        String msg = null;
        if (portal != null) {
            if (toBlock == null && sr.getFromBlock() == null) {
                msg = Bundle.getMessage("SignalDirection",
                        portal.getName(),
                        portal.getFromBlock().getDisplayName(),
                        portal.getToBlock().getDisplayName());
                return msg;
            }
            OBlock pToBlk = portal.getToBlock();
            OBlock pFromBlk = portal.getFromBlock();
            if (pToBlk.equals(toBlock)) {
                if (fromBlock == null) {
                    sr.setFromBlock(pFromBlk);
                    /*    			} else if (!fromBlock.equals(pFromBlk)) {
                     msg = Bundle.getMessage("PortalBlockConflict", portal.getName(), 
                     fromBlock.getDisplayName());    */
                }
            } else if (pFromBlk.equals(toBlock)) {
                if (fromBlock == null) {
                    sr.setFromBlock(pToBlk);
                    /*    			} else if (!toBlock.equals(pToBlk)) {
                     msg = Bundle.getMessage("PortalBlockConflict", portal.getName(),
                     toBlock.getDisplayName()); */
                }
            } else if (pToBlk.equals(fromBlock)) {
                if (toBlock == null) {
                    sr.setToBlock(pFromBlk);
                }
            } else if (pFromBlk.equals(fromBlock)) {
                if (toBlock == null) {
                    sr.setToBlock(pToBlk);
                }
            } else {
                msg = Bundle.getMessage("PortalBlockConflict", portal.getName(),
                        (toBlock != null ? toBlock.getDisplayName() : "(null to-block reference)"));
            }
        } else if (fromBlock != null && toBlock != null) {
            Portal p = getPortalwithBlocks(fromBlock, toBlock);
            if (p == null) {
                msg = Bundle.getMessage("NoSuchPortal", fromBlock.getDisplayName(), toBlock.getDisplayName());
            } else {
                sr.setPortal(p);
            }
        }
        if (msg == null && fromBlock != null && fromBlock.equals(toBlock)) {
            msg = Bundle.getMessage("SametoFromBlock", fromBlock.getDisplayName());
        }
        return msg;
    }

    private Portal getPortalwithBlocks(OBlock fromBlock, OBlock toBlock) {
        String[] sysNames = _portalMgr.getSystemNameArray();
        for (int i = 0; i < sysNames.length; i++) {
            Portal portal = _portalMgr.getBySystemName(sysNames[i]);
            if ((portal.getFromBlock().equals(fromBlock) || portal.getToBlock().equals(fromBlock))
                    && (portal.getFromBlock().equals(toBlock) || portal.getToBlock().equals(toBlock))) {
                return portal;
            }
        }
        return null;
    }

    private String checkDuplicateSignal(NamedBean signal) {
        if (signal == null) {
            return null;
        }
        for (int i = 0; i < _signalList.size(); i++) {
            SignalRow srow = _signalList.get(i);
            if (signal.equals(srow.getSignal())) {
                return Bundle.getMessage("DuplSignalName",
                        signal.getDisplayName(), srow.getToBlock().getDisplayName(),
                        srow.getPortal().getName(), srow.getFromBlock().getDisplayName());

            }
        }
        return null;
    }

    private String checkDuplicateSignal(SignalRow row) {
        NamedBean signal = row.getSignal();
        if (signal == null) {
            return null;
        }
        for (int i = 0; i < _signalList.size(); i++) {
            SignalRow srow = _signalList.get(i);
            if (srow.equals(row)) {
                continue;
            }
            if (signal.equals(srow.getSignal())) {
                return Bundle.getMessage("DuplSignalName",
                        signal.getDisplayName(), srow.getToBlock().getDisplayName(),
                        srow.getPortal().getName(), srow.getFromBlock().getDisplayName());

            }
        }
        return null;
    }

    private String checkDuplicateProtection(SignalRow row) {
        Portal portal = row.getPortal();
        OBlock block = row.getToBlock();
        if (block == null || portal == null) {
            return null;
        }
        for (int i = 0; i < _signalList.size(); i++) {
            SignalRow srow = _signalList.get(i);
            if (srow.equals(row)) {
                continue;
            }
            if (block.equals(srow.getToBlock()) && portal.equals(srow.getPortal())) {
                return Bundle.getMessage("DuplProtection", block.getDisplayName(), portal.getName(),
                        srow.getFromBlock().getDisplayName(), srow.getSignal().getDisplayName());
            }
        }
        return null;

    }

    public int getColumnCount() {
        return NUMCOLS;
    }

    public int getRowCount() {
        return _signalList.size() + 1;
    }

    @Override
    public String getColumnName(int col) {
        switch (col) {
            case NAME_COLUMN:
                return Bundle.getMessage("SignalName");
            case FROM_BLOCK_COLUMN:
                return Bundle.getMessage("FromBlockName");
            case PORTAL_COLUMN:
                return Bundle.getMessage("ThroughPortal");
            case TO_BLOCK_COLUMN:
                return Bundle.getMessage("ToBlockName");
            case TIME_OFFSET:
                return Bundle.getMessage("TimeOffset");
        }
        return "";
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        //if (log.isDebugEnabled()) log.debug("getValueAt rowIndex= "+rowIndex+" _lastIdx= "+_lastIdx);
        if (_signalList.size() == rowIndex) {
            return tempRow[columnIndex];
        }
        switch (columnIndex) {
            case NAME_COLUMN:
                if (_signalList.get(rowIndex).getSignal() != null) {
                    return _signalList.get(rowIndex).getSignal().getDisplayName();
                }
                break;
            case FROM_BLOCK_COLUMN:
                if (_signalList.get(rowIndex).getFromBlock() != null) {
                    return _signalList.get(rowIndex).getFromBlock().getDisplayName();
                }
                break;
            case PORTAL_COLUMN:
                if (_signalList.get(rowIndex).getPortal() != null) {
                    return _signalList.get(rowIndex).getPortal().getName();
                }
                break;
            case TO_BLOCK_COLUMN:
                if (_signalList.get(rowIndex).getToBlock() != null) {
                    return _signalList.get(rowIndex).getToBlock().getDisplayName();
                }
                break;
            case TIME_OFFSET:
                return Float.toString(_signalList.get(rowIndex).getDelayTime()/1000);
            case DELETE_COL:
                return Bundle.getMessage("ButtonDelete");
        }
        return "";
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        String msg = null;
        if (_signalList.size() == row) {
            if (col == DELETE_COL) {
                initTempRow();
                fireTableRowsUpdated(row, row);
                return;
            } else {
                String str = (String) value;
                if (str == null || str.trim().length() == 0) {
                    tempRow[col] = null;
                    return;
              } else {
                    tempRow[col] = str.trim();
                }
            }
            OBlock fromBlock = null;
            OBlock toBlock = null;
            Portal portal = null;
            NamedBean signal = null;
            OBlockManager OBlockMgr = InstanceManager.getDefault(OBlockManager.class);
            if (tempRow[FROM_BLOCK_COLUMN] != null) {
                fromBlock = OBlockMgr.getOBlock(tempRow[FROM_BLOCK_COLUMN]);
                if (fromBlock == null) {
                    msg = Bundle.getMessage("NoSuchBlock", tempRow[FROM_BLOCK_COLUMN]);
                }
            }
            if (msg == null && tempRow[TO_BLOCK_COLUMN] != null) {
                toBlock = OBlockMgr.getOBlock(tempRow[TO_BLOCK_COLUMN]);
                if (toBlock == null) {
                    msg = Bundle.getMessage("NoSuchBlock", tempRow[TO_BLOCK_COLUMN]);
                }
            }
            if (msg == null) {
                if (tempRow[PORTAL_COLUMN] != null) {
                    portal = _portalMgr.getPortal(tempRow[PORTAL_COLUMN]);
                    if (portal == null) {
                        msg = Bundle.getMessage("NoSuchPortalName", tempRow[PORTAL_COLUMN]);
                    }                    
                } else {
                    if (fromBlock != null && toBlock != null) {
                        portal = getPortalwithBlocks(fromBlock, toBlock);
                        if (portal == null) {
                            msg = Bundle.getMessage("NoSuchPortal", tempRow[FROM_BLOCK_COLUMN], tempRow[TO_BLOCK_COLUMN]);
                        } else {
                            tempRow[PORTAL_COLUMN] = portal.getName();
                        }
                    }                    
                }
            }
            if (msg == null && tempRow[NAME_COLUMN] != null) {
                signal = Portal.getSignal(tempRow[NAME_COLUMN]);
                if (signal == null) {
                    msg = Bundle.getMessage("NoSuchSignal", tempRow[NAME_COLUMN]);
                } else {
                    msg = checkDuplicateSignal(signal);
                }
                if (msg==null) {
                    if (fromBlock != null && toBlock != null) {
                        portal = getPortalwithBlocks(fromBlock, toBlock);
                        if (portal == null) {
                            msg = Bundle.getMessage("NoSuchPortal", tempRow[FROM_BLOCK_COLUMN], tempRow[TO_BLOCK_COLUMN]);
                        } else {
                            tempRow[PORTAL_COLUMN] = portal.getName();
                        }
                    } else {
                        return;
                    }
                }
                if (msg == null) {
                    long time = 0;
                    try {
                        float f = Float.parseFloat(tempRow[TIME_OFFSET]);
                        time = (long)f*1000;
                    } catch (NumberFormatException nfe) {
                        msg = Bundle.getMessage("DelayTriggerTime", tempRow[TIME_OFFSET]);
                    }
                    if (time<-30000 || time>30000) {
                        msg = Bundle.getMessage("DelayTriggerTime", tempRow[TIME_OFFSET]);                
                    }
                    if (msg == null) {
                        _signalList.add(new SignalRow(signal, fromBlock, portal, toBlock, time));
                        initTempRow();
                        fireTableDataChanged();                        
                    }
                }
            }
        } else {	// Editing existing signal configurations
            SignalRow signalRow = _signalList.get(row);
            OBlockManager OBlockMgr = InstanceManager.getDefault(OBlockManager.class);
            switch (col) {
                case NAME_COLUMN:
                    NamedBean signal = Portal.getSignal((String) value);
                    if (signal == null) {
                        msg = Bundle.getMessage("NoSuchSignal", (String) value);
//                        signalRow.setSignal(null);                            		
                        break;
                    }
                    Portal portal = signalRow.getPortal();
                    if (portal != null && signalRow.getToBlock() != null) {
                        NamedBean oldSignal = signalRow.getSignal();
                        signalRow.setSignal(signal);
                        msg = checkDuplicateSignal(signalRow);
                        if (msg == null) {
                            deleteSignal(signalRow);    // delete old
                            msg = setSignal(signalRow, false);
                            fireTableRowsUpdated(row, row);
                        } else {
                            signalRow.setSignal(oldSignal);

                        }
                    }
                    break;
                case FROM_BLOCK_COLUMN:
                    OBlock block = OBlockMgr.getOBlock((String) value);
                    if (block == null) {
                        msg = Bundle.getMessage("NoSuchBlock", (String) value);
//                        signalRow.setFromBlock(null);                    	
                        break;
                    }
                    if (block.equals(signalRow.getFromBlock())) {
                        break;      // no change
                    }
                    deleteSignal(signalRow);    // delete old
//                    OBlock oldBlock = signalRow.getFromBlock();
                    signalRow.setFromBlock(block);
                    portal = signalRow.getPortal();
                    if (checkPortalBlock(portal, block)) {
                        signalRow.setToBlock(null);
                    } else {
                        // get new portal
                        portal = getPortalwithBlocks(block, signalRow.getToBlock());
                        signalRow.setPortal(portal);
                    }
                    msg = checkSignalRow(signalRow);
                    if (msg == null) {
                        msg = checkDuplicateProtection(signalRow);
                    } else {
                        signalRow.setPortal(null);
                        break;
                    }
                    if (msg == null && signalRow.getPortal() != null) {
                        msg = setSignal(signalRow, true);
                    } else {
                        signalRow.setPortal(null);
                    }
                    fireTableRowsUpdated(row, row);
                    break;
                case PORTAL_COLUMN:
                    portal = _portalMgr.getPortal((String) value);
                    if (portal == null) {
                        msg = Bundle.getMessage("NoSuchPortalName", (String) value);
//                        signalRow.setPortal(null);
                        break;
                    }
                    deleteSignal(signalRow);    // delete old
                    signalRow.setPortal(portal);
                    block = signalRow.getToBlock();
                    if (checkPortalBlock(portal, block)) {
                        signalRow.setFromBlock(null);
                    } else {
                        block = signalRow.getFromBlock();
                        if (checkPortalBlock(portal, block)) {
                            signalRow.setToBlock(null);
                        }
                    }
                    msg = checkSignalRow(signalRow);
                    if (msg == null) {
                        msg = checkDuplicateProtection(signalRow);
                    } else {
                        signalRow.setToBlock(null);
                        break;
                    }
                    if (msg == null) {
                        signalRow.setPortal(portal);
                        msg = setSignal(signalRow, false);
                        fireTableRowsUpdated(row, row);
                    }
                    break;
                case TO_BLOCK_COLUMN:
                    block = OBlockMgr.getOBlock((String) value);
                    if (block == null) {
                        msg = Bundle.getMessage("NoSuchBlock", (String) value);
//                        signalRow.setToBlock(null);
                        break;
                    }
                    if (block.equals(signalRow.getToBlock())) {
                        break;      // no change
                    }
                    deleteSignal(signalRow);    // delete old
                    signalRow.setToBlock(block);
                    portal = signalRow.getPortal();
                    if (checkPortalBlock(portal, block)) {
                        signalRow.setFromBlock(null);
                    } else {
                        // get new portal
                        portal = getPortalwithBlocks(signalRow.getFromBlock(), block);
                        signalRow.setPortal(portal);
                    }
                    msg = checkSignalRow(signalRow);
                    if (msg == null) {
                        msg = checkDuplicateProtection(signalRow);
                    } else {
                        signalRow.setPortal(null);
                        break;
                    }
                    if (msg == null && signalRow.getPortal() != null) {
                        msg = setSignal(signalRow, true);
                    } else {
                        signalRow.setPortal(null);
                    }
                    fireTableRowsUpdated(row, row);
                    break;
                case TIME_OFFSET:
                    long time = 0;
                    try {
                        float f = Float.parseFloat((String) value);
                        time = (long)f*1000;
                    } catch (NumberFormatException nfe) {
                        msg = Bundle.getMessage("DelayTriggerTime", (String) value);
                        signalRow.setDelayTime(0);
                        break;
                    }
                    if (time<-20000 || time>20000) {
                        msg = Bundle.getMessage("DelayTriggerTime", (String) value);                
                    }
                    signalRow.setDelayTime(time);
                    String m = setSignal(signalRow, false);
                    if (m!=null) {
                        msg = m;
                    }
                    fireTableRowsUpdated(row, row);
                    break;
                case DELETE_COL:
                    deleteSignal(signalRow);
                    _signalList.remove(signalRow);
                    fireTableDataChanged();

            }
        }

        if (msg != null) {
            JOptionPane.showMessageDialog(null, msg,
                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
        }
    }

    private void deleteSignal(SignalRow signalRow) {
        Portal portal = signalRow.getPortal();
        if (portal == null) {
            portal = getPortalwithBlocks(signalRow.getFromBlock(), signalRow.getToBlock());
        }
        if (portal != null) {
            // remove signal from previous portal
            portal.deleteSignal(signalRow.getSignal());
        }
    }

    static private String setSignal(SignalRow signalRow, boolean deletePortal) {
        Portal portal = signalRow.getPortal();
        if (portal.setProtectSignal(signalRow.getSignal(), signalRow.getDelayTime(), signalRow.getToBlock())) {
            if (signalRow.getFromBlock() == null) {
                signalRow.setFromBlock(portal.getOpposingBlock(signalRow.getToBlock()));
            }
        } else {
            if (deletePortal) {
                signalRow.setPortal(null);
            } else {
                signalRow.setToBlock(null);
            }
            return Bundle.getMessage("PortalBlockConflict", portal.getName(),
                    signalRow.getToBlock().getDisplayName());
        }
        return null;
    }

    static private boolean checkPortalBlock(Portal portal, OBlock block) {
        if (block==null) {
            return false;
        }
        return (block.equals(portal.getToBlock()) || block.equals(portal.getFromBlock()));
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return true;
    }

    @Override
    public Class<?> getColumnClass(int col) {
        if (col == DELETE_COL) {
            return JButton.class;
        }
        return String.class;
    }

    public int getPreferredWidth(int col) {
        switch (col) {
            case NAME_COLUMN:
                return new JTextField(18).getPreferredSize().width;
            case FROM_BLOCK_COLUMN:
                return new JTextField(18).getPreferredSize().width;
            case PORTAL_COLUMN:
                return new JTextField(18).getPreferredSize().width;
            case TO_BLOCK_COLUMN:
                return new JTextField(18).getPreferredSize().width;
            case TIME_OFFSET:
                return new JTextField(6).getPreferredSize().width;
            case DELETE_COL:
                return new JButton("DELETE").getPreferredSize().width;
        }
        return 5;
    }

    public void propertyChange(PropertyChangeEvent e) {
        String property = e.getPropertyName();
        if (property.equals("length") || property.equals("portalCount")
                || property.equals("UserName")) {
            makeList();
            fireTableDataChanged();
        }
    }

    static Logger log = LoggerFactory.getLogger(SignalTableModel.class.getName());
}
