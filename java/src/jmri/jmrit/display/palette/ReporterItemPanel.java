package jmri.jmrit.display.palette;

import java.awt.Color;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Hashtable;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;

import jmri.NamedBean;
import jmri.util.JmriJFrame;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.ReporterIcon;
import jmri.jmrit.display.Editor;
import jmri.jmrit.picker.PickListModel;

public class ReporterItemPanel extends TableItemPanel {
    ReporterIcon _reporter;

    public ReporterItemPanel(JmriJFrame parentFrame, String  type, String family, PickListModel model, Editor editor) {
        super(parentFrame, type, family, model, editor);
    }

    public void init() {
        add(initTablePanel(_model, _editor));        // NORTH Panel
        initIconFamiliesPanel();
        add(_iconFamilyPanel);
    }

    protected JPanel instructions() {
        JPanel blurb = new JPanel();
        blurb.setLayout(new BoxLayout(blurb, BoxLayout.Y_AXIS));
        blurb.add(Box.createVerticalStrut(ItemPalette.STRUT_SIZE));
        blurb.add(new JLabel(ItemPalette.rbp.getString("AddToPanel")));
        blurb.add(new JLabel(ItemPalette.rbp.getString("DragIconPanel")));
        blurb.add(Box.createVerticalStrut(ItemPalette.STRUT_SIZE));
        JPanel panel = new JPanel();
        panel.add(blurb);
        return panel;
    }

    protected void initIconFamiliesPanel() {
        _iconFamilyPanel = new JPanel();
        _iconFamilyPanel.setLayout(new BoxLayout(_iconFamilyPanel, BoxLayout.Y_AXIS));
        if (!_update) {
            _iconFamilyPanel.add(instructions());
        }
        _dragIconPanel = new JPanel();
        makeDndIconPanel(null, null);

        _iconFamilyPanel.add(_dragIconPanel);
    }

    protected void makeDndIconPanel(Hashtable<String, NamedIcon> iconMap, String displayKey) {
         if (_update) {
             return;
         }
         _dragIconPanel.setToolTipText(ItemPalette.rbp.getString("ToolTipDragIcon"));

         _reporter = new ReporterIcon(_editor);
         JPanel panel = new JPanel();
         String borderName = ItemPalette.convertText("dragToPanel");
         panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), 
                                                          borderName));
         JPanel comp;
         try {
             comp = getDragger(new DataFlavor(Editor.POSITIONABLE_FLAVOR));
             comp.setToolTipText(ItemPalette.rbp.getString("ToolTipDragIcon"));
         } catch (java.lang.ClassNotFoundException cnfe) {
             cnfe.printStackTrace();
             comp = new JPanel();
         }
         comp.add(_reporter);
         panel.add(comp);
         panel.validate();
         int width = Math.max(100, panel.getPreferredSize().width);
         panel.setPreferredSize(new java.awt.Dimension(width, panel.getPreferredSize().height));
         panel.setToolTipText(ItemPalette.rbp.getString("ToolTipDragIcon"));
         _dragIconPanel.add(panel);
     }

    /**
    *  ListSelectionListener action from table
    */
    public void valueChanged(ListSelectionEvent e) {
        if (_table == null) {
            return;
        }
        int row = _table.getSelectedRow();
        if (log.isDebugEnabled()) log.debug("Table valueChanged: row= "+row);
        if (row >= 0) {
            if (_updateButton!=null) {
                _updateButton.setEnabled(true);
                _updateButton.setToolTipText(null);
            }
            NamedBean bean = getNamedBean();
            _reporter.setReporter(bean.getDisplayName());
        } else {
            if (_updateButton!=null) {
                _updateButton.setEnabled(false);
                _updateButton.setToolTipText(ItemPalette.rbp.getString("ToolTipPickFromTable"));
            }
        }
        validate();
    }

    protected IconDragJComponent getDragger(DataFlavor flavor) {
        return new IconDragJComponent(flavor);
    }

    protected class IconDragJComponent extends DragJComponent {

        public IconDragJComponent(DataFlavor flavor) {
            super(flavor);
        }
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException,IOException {
            if (!isDataFlavorSupported(flavor)) {
                return null;
            }
            NamedBean bean = getNamedBean();
            if (bean==null) {
                log.error("IconDragJLabel.getTransferData: NamedBean is null!");
                return null;
            }

            ReporterIcon r = new ReporterIcon(_editor);
            r.setReporter(bean.getDisplayName());
            r.setLevel(Editor.REPORTERS);
            return r;
        }
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ReporterItemPanel.class.getName());
}
