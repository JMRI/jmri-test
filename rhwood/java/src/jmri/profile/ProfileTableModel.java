package jmri.profile;

import javax.swing.table.AbstractTableModel;

/**
 *
 * @author rhwood
 */
public class ProfileTableModel extends AbstractTableModel {

    @Override
    public int getRowCount() {
        return ProfileManager.defaultManager().getProfiles().length;
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Profile p = ProfileManager.defaultManager().getProfiles(rowIndex);
        switch (columnIndex) {
            case 0:
                return p.getName();
            case 1:
                return p.getPath();
            default:
                return null;
        }
    }

}
