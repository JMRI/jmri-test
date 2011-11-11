// MultiSensorIconDialog.java
package jmri.jmrit.display.palette;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import jmri.jmrit.catalog.ImageIndexEditor;
import jmri.jmrit.catalog.NamedIcon;

/**
 * Icons may be added or deleted from a family
 * @author Pete Cressman  Copyright (c) 2010
 */

public class MultiSensorIconDialog extends IconDialog {

    /**
    * Constructor for existing family to change icons, add/delete icons, or to delete the family
    */
    public MultiSensorIconDialog(String type, String family, ItemPanel parent, 
    						Hashtable <String, NamedIcon> iconMap) {
        super(type, family, parent, iconMap); 
    }
/*
    protected JPanel makeButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        makeAddIconButtonPanel(buttonPanel, "ToolTipAddPosition", "ToolTipDeletePosition");
        makeAddSetButtonPanel(buttonPanel);
        makeDoneButtonPanel(buttonPanel);
        return buttonPanel;
    }
*/
    protected String getIconName() {
        return MultiSensorItemPanel.POSITION[_iconMap.size()-3];
    }
    
    protected void makeAddSetButtonPanel(JPanel buttonPanel) {
        makeAddIconButtonPanel(buttonPanel, "ToolTipAddPosition", "ToolTipDeletePosition");
        super.makeAddSetButtonPanel(buttonPanel);
    }

    /**
    * add/delete icon. For Multisensor, it adds another sensor position.
    */
    protected void makeAddIconButtonPanel(JPanel buttonPanel, String addTip, String deleteTip) {
        JPanel panel2 = new JPanel();
        panel2.setLayout(new FlowLayout());
        JButton addSensor = new JButton(ItemPalette.rbp.getString("addIcon"));
        addSensor.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    if (addNewIcon(getIconName())) {
                        ImageIndexEditor.indexChanged(true);
                        getContentPane().remove(_iconPanel);
                        _iconPanel = makeIconPanel(_iconMap); 
                        getContentPane().add(_iconPanel, 1);
                        pack();
                    }
                }
        });
        addSensor.setToolTipText(ItemPalette.rbp.getString(addTip));
        panel2.add(addSensor);

        JButton deleteSensor = new JButton(ItemPalette.rbp.getString("deleteIcon"));
        deleteSensor.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    if (deleteIcon()) {
                        ImageIndexEditor.indexChanged(true);
                        getContentPane().remove(_iconPanel);
                        _iconPanel = makeIconPanel(_iconMap); 
                        getContentPane().add(_iconPanel, 1);
                        pack();
                    }
                }
        });
        deleteSensor.setToolTipText(ItemPalette.rbp.getString(deleteTip));
        panel2.add(deleteSensor);
        buttonPanel.add(panel2);
    }

    /**
    * Action item for makeAddIconButtonPanel
    */
    protected boolean addNewIcon(String name) {
        if (log.isDebugEnabled()) log.debug("addNewIcon Action: iconMap.size()= "+_iconMap.size());
        if (name==null || name.length()==0) {
            JOptionPane.showMessageDialog(_parent._paletteFrame, ItemPalette.rbp.getString("NoIconName"),
                    ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
            return false;
        } else if (_iconMap.get(name)!=null) {
            JOptionPane.showMessageDialog(_parent._paletteFrame,
                    java.text.MessageFormat.format(ItemPalette.rbp.getString("DuplicateIconName"), name),
                    ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
            return false;
        }
        String fileName = "resources/icons/misc/X-red.gif";
        NamedIcon icon = new jmri.jmrit.catalog.NamedIcon(fileName, fileName);
        _iconMap.put(name, icon);
        return true;
    }

    /**
    * Action item for makeAddIconButtonPanel
    */
    protected boolean deleteIcon() {
        if (log.isDebugEnabled()) log.debug("deleteSensor Action: iconMap.size()= "+_iconMap.size());
        if (_iconMap.size()<4) {
            return false;
        }
        String name = MultiSensorItemPanel.POSITION[_iconMap.size()-4];
        _iconMap.remove(name);
        return true;
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MultiSensorIconDialog.class.getName());
}

