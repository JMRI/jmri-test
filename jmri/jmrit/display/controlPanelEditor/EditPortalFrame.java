package jmri.jmrit.display.controlPanelEditor;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Map.Entry;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.display.*;

import jmri.jmrit.logix.*;
import jmri.jmrit.catalog.DragJLabel;
import jmri.jmrit.catalog.NamedIcon;

/**
 * <P>
 * @author  Pete Cressman Copyright: Copyright (c) 2011
 * 
 */

public class EditPortalFrame extends jmri.util.JmriJFrame implements ListSelectionListener {

    private OBlock _homeBlock;
    private CircuitBuilder _parent;
    private OBlock _adjacentBlock;

    private JPanel      _portalPanel;   
    private JList       _portalList;
    private PortalListModel _portalListModel; 

    private JTextField  _portalName = new JTextField();

    static java.util.ResourceBundle rbcp = ControlPanelEditor.rbcp;
    static int STRUT_SIZE = 10;

    public EditPortalFrame(String title, CircuitBuilder parent, OBlock block) {
        _homeBlock = block;
        _parent = parent;
        setTitle(java.text.MessageFormat.format(title, _homeBlock.getDisplayName()));

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                closingEvent();
            }
        });
        _parent.setEditColors();

        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        _portalPanel = makePortalPanel();

        contentPane.add(Box.createVerticalStrut(STRUT_SIZE));
        contentPane.add(_portalPanel);
        contentPane.add(Box.createVerticalStrut(STRUT_SIZE));

        JPanel border = new JPanel();
        border.setLayout(new java.awt.BorderLayout(10,10));
        border.add(contentPane);
        setContentPane(border);
        setSize(500, 500);
        pack();
        setVisible(true);
    }

    private JPanel MakeButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        JButton doneButton = new JButton(rbcp.getString("ButtonDone"));
        doneButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    closingEvent();
                }
        });
        panel.add(doneButton);
        buttonPanel.add(panel);

        panel = new JPanel();
        panel.setLayout(new FlowLayout());
        panel.add(buttonPanel);

        return panel;
    }

    private JPanel makePortalPanel() {
        JPanel portalPanel = new JPanel();
        portalPanel.setLayout(new BoxLayout(portalPanel, BoxLayout.Y_AXIS));
        portalPanel.add(Box.createVerticalStrut(STRUT_SIZE));

        JPanel panel = new JPanel();
        panel.add(new JLabel(java.text.MessageFormat.format(
                                    rbcp.getString("PortalTitle"), _homeBlock.getDisplayName())));
        portalPanel.add(panel);

        _portalListModel =  new PortalListModel();
        _portalList = new JList();
        _portalList.setModel(_portalListModel);
        _portalList.setCellRenderer(new PortalCellRenderer());
        _portalList.addListSelectionListener(this);
        portalPanel.add(new JScrollPane(_portalList));

        JButton clearButton = new JButton(rbcp.getString("buttonClearSelection"));
        clearButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    clearListSelection();
                }
        });
        //clearButton.setToolTipText(ItemPalette.rbp.getString("ToolTipClearSelection"));
        panel = new JPanel();
        panel.add(clearButton);
        portalPanel.add(panel);
        portalPanel.add(Box.createVerticalStrut(STRUT_SIZE));

        panel = new JPanel();
        panel.add(CircuitBuilder.makeTextBoxPanel(
                    false, _portalName, "portalName", true, null));
        _portalName.setPreferredSize(new Dimension(300, _portalName.getPreferredSize().height));
        _portalName.setToolTipText(java.text.MessageFormat.format(
                            rbcp.getString("TooltipPortalName"), _homeBlock.getDisplayName()));
        portalPanel.add(panel);

        panel = new JPanel();
/*
        JButton addButton = new JButton(rbcp.getString("buttonAddPortal"));
        addButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    addPortal();
                }
        });
        addButton.setToolTipText(rbcp.getString("ToolTipAddPortal"));
        panel.add(addButton);
*/ 
        JButton changeButton = new JButton(rbcp.getString("buttonChangeName"));
        changeButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    changePortalName();
                }
        });
        changeButton.setToolTipText(rbcp.getString("ToolTipChangeName"));
        panel.add(changeButton);
 
        JButton deleteButton = new JButton(rbcp.getString("buttonDeletePortal"));
        deleteButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    deletePortal();
                }
        });
        deleteButton.setToolTipText(rbcp.getString("ToolTipDeletePortal"));
        panel.add(deleteButton);
 
        portalPanel.add(panel);
        portalPanel.add(Box.createVerticalStrut(STRUT_SIZE));

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel l = new JLabel(rbcp.getString("enterNameToDrag"));
        l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        panel.add(l);
        l = new JLabel(rbcp.getString("dragNewIcon"));
        l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        panel.add(l);
        panel.add(Box.createVerticalStrut(STRUT_SIZE/2));
        l = new JLabel(rbcp.getString("selectPortal"));
        l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        panel.add(l);
        l = new JLabel(rbcp.getString("dragIcon"));
        l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        panel.add(l);
        JPanel p = new JPanel();
        p.add(panel);
        portalPanel.add(p);

        portalPanel.add(makeDndIconPanel());
        portalPanel.add(Box.createVerticalStrut(STRUT_SIZE));

        portalPanel.add(MakeButtonPanel());
        return portalPanel;
    }

    private static class PortalCellRenderer extends JLabel implements ListCellRenderer {
     
        public Component getListCellRendererComponent(
           JList list,              // the list
           Object value,            // value to display
           int index,               // cell index
           boolean isSelected,      // is the cell selected
           boolean cellHasFocus)    // does the cell have focus
        {
             String s = ((Portal)value).getDescription();
             setText(s);
             if (isSelected) {
                 setBackground(list.getSelectionBackground());
                 setForeground(list.getSelectionForeground());
             } else {
                 setBackground(list.getBackground());
                 setForeground(list.getForeground());
             }
             setEnabled(list.isEnabled());
             setFont(list.getFont());
             setOpaque(true);
             return this;
        }
    }

    private void clearListSelection() {
        _portalList.clearSelection();
        _portalName.setText(null);
    }

    public void valueChanged(ListSelectionEvent e) {
        Portal portal = (Portal)_portalList.getSelectedValue();
        if (portal!=null) {
            _portalName.setText(portal.getName());
        } else {
            _portalName.setText(null);
        }
    }

    class PortalListModel extends AbstractListModel {
        public int getSize() {
            return _homeBlock.getPortals().size();
        }
        public Object getElementAt(int index) {
            return _homeBlock.getPortals().get(index);
        }
        public void dataChange() {
            fireContentsChanged(this, 0, 0);
        }
    }

    /************************* end setup **************************/

    /**
    * Is location of icon reasonable? if so, add it
    */
    private String checkPortalIcon(PortalIcon icon) {
        java.util.List<Positionable> list = _parent.getCircuitIcons(_homeBlock);
        String msg = null;
        if (list==null || list.size()==0) {
            msg = rbcp.getString("needIcons");
            return msg;
        }
        Portal portal = icon.getPortal();
        boolean ok = false;
        Rectangle homeRect = new Rectangle();
        Rectangle adjRect = new Rectangle();
        Positionable comp = null;
        _adjacentBlock = null;
        for (int i=0; i<list.size(); i++) {
            homeRect = list.get(i).getBounds(homeRect);
           if (iconIntersectsRect(icon, homeRect)) {
               ok = true;
               break;
            }
        }
        if (log.isDebugEnabled()) log.debug("checkPortalIcon: "+icon.getName()+" hit homeBlock= "+ok);
        if (!ok) {
            msg = java.text.MessageFormat.format(rbcp.getString("iconNotOnCircuit"), 
                                                 icon.getNameString(), _homeBlock.getDisplayName());
            return msg;
        }

        ok = false;
        OBlockManager manager = InstanceManager.oBlockManagerInstance();
        String[] sysNames = manager.getSystemNameArray();
        for (int j = 0; j < sysNames.length; j++) {
            OBlock block = manager.getBySystemName(sysNames[j]);
            if (!block.equals(_homeBlock)) {
                list = _parent.getCircuitIcons(block);
                for (int i=0; i<list.size(); i++) {
                    comp = list.get(i);
                    if (_parent.isTrack(comp)) {
                        adjRect = comp.getBounds(adjRect);
                        if (iconIntersectsRect(icon, adjRect)) {
                            ok = true;
                            _adjacentBlock = block;
                            break;
                        }
                    }
                }
            }
        }
        if (log.isDebugEnabled()) log.debug("checkPortalIcon: "+icon.getName()+" hit adjacent block= "+ok);
        if (!ok) {
            msg = java.text.MessageFormat.format(rbcp.getString("iconNotOnAdjacent"), 
                                                 icon.getNameString(), _homeBlock.getDisplayName());
            return msg;
        }
        if (portal.getToBlock()!=null && !_adjacentBlock.equals(portal.getToBlock())
                         && !_adjacentBlock.equals(portal.getFromBlock()) ) {
            msg = java.text.MessageFormat.format(rbcp.getString("iconNotOnBlocks"), 
                                                 icon.getNameString(), portal.getFromBlockName(),
                                                 _adjacentBlock.getDisplayName());
            return msg;
        }
        return msg;
    }

    /**
    * Called after click on portal icon
    */
    protected void checkPortalIconForUpdate(PortalIcon icon) {
        String msg = checkPortalIcon(icon);
        if (msg!=null) {
            JOptionPane.showMessageDialog(this, msg, rbcp.getString("makePortal"), 
                                          JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Portal portal = icon.getPortal();
        portal.setToBlock(_adjacentBlock, false);
        _parent.getPortalIconMap().put(icon.getName(), icon);
        _portalListModel.dataChange();
    }

    private void checkPortalIcons() {
        boolean ok = true;
        java.util.List<Portal> portals = _homeBlock.getPortals();
        Hashtable<String, PortalIcon> iconMap = _parent.getPortalIconMap();
        if (log.isDebugEnabled()) log.debug("checkPortalIcons: "+_homeBlock.getDisplayName()+
                                            " has "+portals.size()+" portals, iconMap has "+
                                            iconMap.size()+" icons");
        for (int i=0; i<portals.size(); i++) {
            PortalIcon icon = iconMap.get(portals.get(i).getName());
            if (icon ==null || checkPortalIcon(icon)!=null) {
                ok = false;
                break;
            }
        }
        if (!ok) {
            JOptionPane.showMessageDialog(this, rbcp.getString("portalIconMisplaced"), 
                                          rbcp.getString("makePortal"), JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
    * Check if icon is placed on the icons of a block
    */
    static boolean portalIconOK(java.util.List<Positionable> list, PortalIcon icon) {
        if (icon==null) {
            return false;
        }
        Rectangle homeRect = new Rectangle();
        for (int i=0; i<list.size(); i++) {
            homeRect = list.get(i).getBounds(homeRect);
            if (iconIntersectsRect(icon, homeRect)) {
                return false;
            }
        }
        return true;
    }

    static boolean iconIntersectsRect(Positionable icon, Rectangle rect) {
        Rectangle iconRect = icon.getBounds(new Rectangle());
        return (iconRect.intersects(rect));
    }

    private void changePortalName() {
        Portal portal = (Portal)_portalList.getSelectedValue();
        String name = _portalName.getText();
        if (name==null || name.trim().length()==0 || portal==null) {
            JOptionPane.showMessageDialog(this, rbcp.getString("changePortalName"), 
                            rbcp.getString("makePortal"), JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String msg = portal.setName(name);
        if (msg==null) {
            _portalListModel.dataChange();
        } else {
            JOptionPane.showMessageDialog(this, msg, 
                            rbcp.getString("makePortal"), JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void deletePortal() {
        Portal portal = (Portal)_portalList.getSelectedValue();
        if (portal==null) {
            // check that name was typed in and not selected
            portal = _homeBlock.getPortalByName(_portalName.getText());
        }
        if (portal==null) {
            return;
        }
        int result = JOptionPane.showConfirmDialog(this, java.text.MessageFormat.format(
                            rbcp.getString("confirmPortalDelete"), portal.getName()),
                        rbcp.getString("makePortal"), JOptionPane.YES_NO_OPTION, 
                        JOptionPane.QUESTION_MESSAGE);
        if (result==JOptionPane.YES_OPTION) {
            if (getPortalIcon(portal)!=null) {
                getPortalIcon(portal).remove();
            }
            portal.dispose();
            _portalListModel.dataChange();
        }
    }

    protected void closingEvent() {
        checkPortalIcons();
        _parent.closePortalFrame(_homeBlock);
        dispose();
    }

    protected OBlock getHomeBlock() {
        return _homeBlock;
    }
    
    private PortalIcon getPortalIcon(Portal portal) {
        if (portal==null) {
            return null;
        }
        java.util.List contents = _parent.getContents();
        for (int i=0; i<contents.size(); i++) {
            if (contents.get(i) instanceof PortalIcon) {
                PortalIcon icon = (PortalIcon)contents.get(i);
                    if (portal.equals(icon.getPortal())) {
                        return icon;
                    }
            }
        }
        return null;
    }

    /********************** DnD *****************************/

    protected JPanel makeDndIconPanel() {
        JPanel iconPanel = new JPanel();

        String fileName = "resources/icons/throttles/RoundRedCircle20.png";
        NamedIcon icon = new NamedIcon(fileName, fileName);
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), 
                                                         rbcp.getString("portal")));
        try {
            JLabel label = new IconDragJLabel(new DataFlavor(Editor.POSITIONABLE_FLAVOR));
            label.setIcon(icon);
            label.setName(rbcp.getString("portal"));
            panel.add(label);
        } catch (java.lang.ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }
        iconPanel.add(panel);
        return iconPanel;
    }    

    public class IconDragJLabel extends DragJLabel {

        public IconDragJLabel(DataFlavor flavor) {
            super(flavor);
        }
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException,IOException {
            if (!isDataFlavorSupported(flavor)) {
                return null;
            }
            String name = _portalName.getText();
            if (name==null || name.trim().length()==0) {
                JOptionPane.showMessageDialog(this, rbcp.getString("needPortalName"), 
                                rbcp.getString("makePortal"), JOptionPane.INFORMATION_MESSAGE);
            } else {
                Portal portal = _homeBlock.getPortalByName(name);
                if (portal==null) {
                    portal = new Portal(_homeBlock, name, null);
                    _portalListModel.dataChange();
                }
                PortalIcon pi = _parent.getPortalIconMap().get(name);
                if (pi != null) {
                    JOptionPane.showMessageDialog(this, java.text.MessageFormat.format(
                                    rbcp.getString("portalIconExists"), name), 
                                    rbcp.getString("makePortal"), JOptionPane.INFORMATION_MESSAGE);
                } else {
                    pi = new PortalIcon(_parent, portal);
                    pi.setLevel(Editor.MARKERS);
                    pi.setStatus(PortalIcon.BLOCK);
                }
                _parent.highlight(pi);
                return pi;
            }
            return null;
        }
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditPortalFrame.class.getName());
}

