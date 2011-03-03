
package jmri.jmrit.display.palette;

import java.awt.Color;

//import java.awt.datatransfer.Transferable; 
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
//import java.awt.dnd.*;
import java.io.IOException;

import jmri.util.JmriJFrame;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import jmri.jmrit.catalog.DragJLabel;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.AnalogClock2Display;

/**
*  ItemPanel for for plain icons and backgrounds 
*/
public class ClockItemPanel extends IconItemPanel {

    Hashtable<String, NamedIcon> _iconMap;

    /**
    * Constructor for plain icons and backgrounds
    */
    public ClockItemPanel(JmriJFrame parentFrame, String type, String family, Editor editor) {
        super(parentFrame,  type, family, editor);
        setToolTipText(ItemPalette.rbp.getString("ToolTipDragIcon"));
    }
    
    protected JPanel instructions() {
        JPanel blurb = new JPanel();
        blurb.setLayout(new BoxLayout(blurb, BoxLayout.Y_AXIS));
        blurb.add(Box.createVerticalStrut(ItemPalette.STRUT_SIZE));
        blurb.add(new JLabel(ItemPalette.rbp.getString("AddClockToPanel")));
        blurb.add(Box.createVerticalStrut(ItemPalette.STRUT_SIZE));
        JPanel panel = new JPanel();
        panel.add(blurb);
        return panel;
    }

    protected void addIconsToPanel(Hashtable<String, NamedIcon> iconMap) {
        Iterator<Entry<String, NamedIcon>> it = iconMap.entrySet().iterator();
        while (it.hasNext()) {
           Entry<String, NamedIcon> entry = it.next();
           NamedIcon icon = new NamedIcon(entry.getValue());    // make copy for possible reduction
           JPanel panel = new JPanel();
           String borderName = ItemPalette.convertText(entry.getKey());
           panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), 
                                                            borderName));
           try {
               JLabel label = new ClockDragJLabel(new DataFlavor(Editor.POSITIONABLE_FLAVOR));
               if (icon.getIconWidth()<1 || icon.getIconHeight()<1) {
                   label.setText(ItemPalette.rbp.getString("invisibleIcon"));
                   label.setForeground(Color.lightGray);
               } else {
                   icon.reduceTo(100, 100, 0.2);
               }
               label.setIcon(icon);
               label.setName(borderName);
               panel.add(label);
           } catch (java.lang.ClassNotFoundException cnfe) {
               cnfe.printStackTrace();
           }
           _iconPanel.add(panel);
        }
    }
    /**
    *  SOUTH Panel
    */
    public void initButtonPanel() {
    }

    public class ClockDragJLabel extends DragJLabel {

        public ClockDragJLabel(DataFlavor flavor) {
            super(flavor);
        }
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException,IOException {
            if (!isDataFlavorSupported(flavor)) {
                return null;
            }
            String url = ((NamedIcon)getIcon()).getURL();
            if (log.isDebugEnabled()) log.debug("DragJLabel.getTransferData url= "+url);
            AnalogClock2Display c = new AnalogClock2Display(_editor);
            c.setOpaque(false);
            c.update();
            c.setLevel(Editor.CLOCK);
            return c;
        }
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ClockItemPanel.class.getName());
}
