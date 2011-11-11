package jmri.jmrit.throttle;

import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.table.AbstractTableModel;
import jmri.DccThrottle;
import jmri.DccLocoAddress;

public class ThrottlesTableModel extends AbstractTableModel implements AddressListener, java.beans.PropertyChangeListener {

    private ArrayList<ThrottleFrame> throttleFrames = new ArrayList<ThrottleFrame>(5);

    public int getRowCount() {
        return throttleFrames.size();
    }

    public int getColumnCount() {
        return 1;
    }

    public Object getValueAt(int i, int i1) {
        return throttleFrames.get(i);
    }

    public Iterator<ThrottleFrame> iterator() {
        return throttleFrames.iterator();
    }

    public void addThrottleFrame(ThrottleFrame tf) {
        throttleFrames.add(tf);
        fireTableDataChanged();
    }

    public void removeThrottleFrame(ThrottleFrame tf) {
        throttleFrames.remove(tf);
        fireTableDataChanged();
    }

    public void notifyAddressChosen(int newAddress, boolean isLong) {
    }

    public void notifyAddressReleased(int address, boolean isLong) {
        DccLocoAddress la = new DccLocoAddress(address,isLong);
        fireTableDataChanged();
        jmri.InstanceManager.throttleManagerInstance().removeListener(la, this);
    }

    public void notifyAddressThrottleFound(DccThrottle throttle) {
        fireTableDataChanged();
        throttle.addPropertyChangeListener(this);
    }

    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if ((e.getPropertyName().equals("SpeedSetting")) || (e.getPropertyName().equals("SpeedSteps")) || (e.getPropertyName().equals("IsForward"))) {
            fireTableDataChanged();
        }
    }
}
