package jmri.jmrit.throttle;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.border.BevelBorder;
import javax.swing.table.TableCellRenderer;
import jmri.Throttle;
import jmri.jmrit.roster.RosterIconFactory;

public class ThrottlesTableCellRenderer implements TableCellRenderer {
    private static final ResourceBundle throttleBundle = ThrottleBundle.bundle();
    private static final ImageIcon fwdIcon = new ImageIcon("resources/icons/throttles/up-green.png");
    private static final ImageIcon bckIcon = new ImageIcon("resources/icons/throttles/down-green.png");
    private static final ImageIcon estopIcon = new ImageIcon("resources/icons/throttles/estop24.png");
    private static final RosterIconFactory iconFactory =  new RosterIconFactory(32);
    public static int height = 42;

    public Component getTableCellRendererComponent(JTable jtable, Object value, boolean bln, boolean bln1, int i, int i1) {
        JPanel retPanel = new JPanel();
        retPanel.setLayout(new BorderLayout());

        if (value == null) {
            return retPanel;
        }

        ThrottleFrame tf = (ThrottleFrame) value;
        ImageIcon icon = null;
        String text = null;
        if (tf.getRosterEntry() != null) {
            icon = iconFactory.getIcon(tf.getAddressPanel().getRosterEntry());
            text = tf.getAddressPanel().getRosterEntry().getId();
        } else if ((tf.getAddressPanel().getCurrentAddress() != null) && (tf.getAddressPanel().getThrottle() != null)) {
            if (tf.getAddressPanel().getCurrentAddress().getNumber() == 0) {
                text = throttleBundle.getString("ThrottleDCControl") + " - " + tf.getAddressPanel().getCurrentAddress();
            } else if (tf.getAddressPanel().getCurrentAddress().getNumber() == 3) {
                text = throttleBundle.getString("ThrottleDCCControl") + " - " + tf.getAddressPanel().getCurrentAddress();
            } else {
                text = throttleBundle.getString("ThrottleAddress") + " " + tf.getAddressPanel().getCurrentAddress();
            }
        } else {
            text = throttleBundle.getString("ThrottleNotAssigned");
        }
        if (icon != null) {
            icon.setImageObserver(jtable);
        }
        JLabel locoID = new JLabel();
        locoID.setHorizontalAlignment(JLabel.CENTER);
        locoID.setVerticalAlignment(JLabel.CENTER);
        locoID.setIcon(icon);
        locoID.setText(text);
        retPanel.add(locoID, BorderLayout.CENTER);

        if (tf.getAddressPanel().getThrottle()!=null) {
            JPanel ctrlPanel = new JPanel();
            ctrlPanel.setLayout(new BorderLayout());
            Throttle thr = tf.getAddressPanel().getThrottle();
            JLabel dir = new JLabel();
            if (jmri.jmrit.throttle.ThrottleFrameManager.instance().getThrottlesPreferences().isUsingExThrottle()
                    && jmri.jmrit.throttle.ThrottleFrameManager.instance().getThrottlesPreferences().isUsingFunctionIcon()) {
                if (thr.getIsForward()) {
                    dir.setIcon(fwdIcon);
                } else {
                    dir.setIcon(bckIcon);
                }
            } else {
                if (thr.getIsForward()) {
                    dir.setText(throttleBundle.getString("ButtonForward"));
                } else {
                    dir.setText(throttleBundle.getString("ButtonReverse"));
                }
            }
            dir.setVerticalAlignment(JLabel.CENTER);
            ctrlPanel.add(dir, BorderLayout.WEST);
            if (jmri.jmrit.throttle.ThrottleFrameManager.instance().getThrottlesPreferences().isUsingExThrottle()
                    && jmri.jmrit.throttle.ThrottleFrameManager.instance().getThrottlesPreferences().isUsingFunctionIcon()) {
                if (thr.getSpeedSetting()==-1) {
                    JLabel estop = new JLabel();
                    estop.setPreferredSize(new Dimension(64, height - 8));
                    estop.setHorizontalAlignment(JLabel.CENTER);
                    estop.setIcon(estopIcon);
                    ctrlPanel.add(estop, BorderLayout.CENTER);
                } else {
                    JProgressBar speedBar = new javax.swing.JProgressBar();
                    speedBar.setPreferredSize(new Dimension(64, height - 8));
                    speedBar.setMinimum(0);
                    speedBar.setMaximum(100);
                    speedBar.setValue((int) (thr.getSpeedSetting() * 100f));
                    ctrlPanel.add(speedBar, BorderLayout.CENTER);
                }
            } else {
                JLabel speedLabel = new JLabel("");
                if (thr.getSpeedSetting()==-1) {
                     speedLabel.setText(" "+throttleBundle.getString("ButtonEStop")+" ");
                } else {
                    speedLabel.setText(" "+(int)(thr.getSpeedSetting() * 100f)+"% ");
                }
                ctrlPanel.add(speedLabel, BorderLayout.CENTER);
            }
            retPanel.add(ctrlPanel, BorderLayout.EAST);
        }

        retPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        return retPanel;
    }
}
