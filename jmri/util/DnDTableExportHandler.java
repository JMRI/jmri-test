package jmri.util;

/**
 * Simple TransferHandler that exports a string value of
 * a cell in a JTable.
 * <P>
 *
 * @author Pete Cressman  Copyright 2010
 * @version $Revision: 1.1 $
 */

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.TransferHandler;
import java.awt.datatransfer.Transferable; 
import java.awt.datatransfer.StringSelection;

    
public class DnDTableExportHandler extends TransferHandler{

    public int getSourceActions(JComponent c) {
        return COPY;
    }

    public Transferable createTransferable(JComponent c) {
        JTable table = (JTable)c;
        int col = table.getSelectedColumn();
        int row = table.getSelectedRow();
        if (col<0 || row<0) {
            return null;
        }
        if (log.isDebugEnabled()) log.debug("TransferHandler.createTransferable: from ("
                                            +row+", "+col+") for \""
                                            +table.getModel().getValueAt(row, col)+"\"");
        Object obj = table.getModel().getValueAt(row, col);
        if (obj instanceof String) {
            return new StringSelection((String)obj);
        } else {
            return new StringSelection(obj.getClass().getName());
        }
    }

    public void exportDone(JComponent c, Transferable t, int action) {
        if (log.isDebugEnabled()) log.debug("TransferHandler.exportDone ");
    }
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DnDTableExportHandler.class.getName());
}


