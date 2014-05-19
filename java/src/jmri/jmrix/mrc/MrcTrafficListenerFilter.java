package jmri.jmrix.mrc;

import java.util.Date;

/**
 * MrcTrafficListenerFilter is a helper class used to suppress notifications a
 * client is not interested in.
 *
 * @author	Matthias Keil Copyright (C) 2013
 * @version $Revision: $
 *
 */
public class MrcTrafficListenerFilter {

    /* Overridden to compare the listener, not the filter objects.
     *
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj.getClass().equals(this.getClass())) {
            if (((MrcTrafficListenerFilter) obj).l.equals(this.l)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return l.hashCode();
    }
    /**
     * Type of traffic the listener is interested in.
     */
    int mask = 0;
    /**
     * The listener.
     */
    MrcTrafficListener l = null;

    /**
     * Hide default constructor
     */
    @SuppressWarnings("unused")
    private MrcTrafficListenerFilter() {
    }

    /**
     * Constructor
     *
     * @param _mask Type of traffic the listener is interested in.
     * @param _l The listener interface.
     */
    public MrcTrafficListenerFilter(int _mask, MrcTrafficListener _l) {
        mask = _mask;
        l = _l;
    }

    public void setFilter(int _mask) {
        mask = _mask;
    }

    public void fireXmit(Date timestamp, MrcMessage m) {
        if ((mask & MrcTrafficListener.MRC_TRAFFIC_TX) != 0) {
            l.notifyXmit(timestamp, m);
        }
    }

    public void fireRcv(Date timestamp, MrcMessage m) {
        if ((mask & MrcTrafficListener.MRC_TRAFFIC_RX) != 0) {
            l.notifyRcv(timestamp, m);
        }
    }
}
