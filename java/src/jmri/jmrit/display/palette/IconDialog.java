// IconDialog.java
package jmri.jmrit.display.palette;

import org.apache.log4j.Logger;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import jmri.jmrit.catalog.CatalogPanel;
import jmri.jmrit.catalog.NamedIcon;

/**
 *
 * @author Pete Cressman  Copyright (c) 2010, 2011
 */

public class IconDialog extends ItemDialog {

    protected Hashtable <String, NamedIcon>   _iconMap;
    protected JPanel        _iconPanel;
    protected CatalogPanel  _catalog;
    protected JTextField    _familyName;
    protected JButton       _addFamilyButton;
    protected JButton       _deleteButton;
    private boolean 		_newIconSet = false;

    /**
    * Constructor for existing family to change icons, add/delete icons, or to delete the family
    */
    public IconDialog(String type, String family, ItemPanel parent, Hashtable <String, NamedIcon> iconMap ) {
        super(type, family, 
              java.text.MessageFormat.format(ItemPalette.rbp.getString("ShowIconsTitle"), type), parent, true);
        
        _iconMap = clone(iconMap);
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(Box.createVerticalStrut(ItemPalette.STRUT_SIZE));
        
        boolean isUpdate = parent.isUpdate();
        _familyName = new JTextField(family);
        if (_family==null) {
        	_newIconSet = true;
//        	_familyName.setText(ItemPalette.rbp.getString("Unnamed"));
        }
        _familyName.setEditable(!isUpdate);
        panel.add(ItemPalette.makeBannerPanel("IconSetName", _familyName));
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        if (_iconMap != null) {
            makeAddIconButtonPanel(buttonPanel, "ToolTipAddPosition", "ToolTipDeletePosition");
            if (!isUpdate) {
            	makeAddSetButtonPanel(buttonPanel);
            }
            makeDoneButtonPanel(buttonPanel);
        } else {
        	_iconMap = ItemPanel.makeNewIconMap(type);
            makeCreateButtonPanel(buttonPanel);
        }

        _iconPanel = makeIconPanel(_iconMap);
        panel.add(_iconPanel);	// put icons above buttons
        panel.add(buttonPanel);
        
        _catalog = CatalogPanel.makeDefaultCatalog();
        _catalog.setToolTipText(ItemPalette.rb.getString("ToolTipDragIcon"));
        panel.add(new JScrollPane(_catalog));

        setContentPane(panel);
    }

    /**
    * Add/Delete icon family for types that may have more than 1 family
    */
    protected void makeAddSetButtonPanel(JPanel buttonPanel) {
        JPanel panel1 = new JPanel();
        panel1.setLayout(new FlowLayout());
        _addFamilyButton = new JButton(ItemPalette.rbp.getString("addNewFamily"));
        _addFamilyButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    addFamilySet();
                    dispose();
                }
        });
        _addFamilyButton.setToolTipText(ItemPalette.rbp.getString("ToolTipAddFamily"));
        panel1.add(_addFamilyButton);
        if (_newIconSet) {
        	_addFamilyButton.setEnabled(false);
        }

        _deleteButton = new JButton(ItemPalette.rbp.getString("deleteFamily"));
        _deleteButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    deleteFamilySet();
                    dispose();
                }
        });
        _deleteButton.setToolTipText(ItemPalette.rbp.getString("ToolTipDeleteFamily"));
        panel1.add(_deleteButton);
        buttonPanel.add(panel1);
    }

    // Only multiSensor adds and deletes icons 
    protected void makeAddIconButtonPanel(JPanel buttonPanel, String addTip, String deleteTip) {
    }

    /**
    * Action for both create new family and change existing family
    */
    protected boolean doDoneAction() {
       //check text
        String family = _familyName.getText();
        _parent.reset();
        checkIconSizes();
    	((FamilyItemPanel)_parent)._currentIconMap = _iconMap;
        if (_parent.isUpdate()) {  // don't touch palette's maps.  just modify individual device icons
        	return true;
        }
    	if (!_newIconSet && family.equals(_family)) {
            ItemPalette.removeIconMap(_type, _family);
    	}        
    	while (!ItemPalette.addFamily(_parent._paletteFrame, _type, family, _iconMap)) {
    		/*
            family = JOptionPane.showInputDialog(_parent._paletteFrame, ItemPalette.rbp.getString("EnterFamilyName"), 
                    ItemPalette.rb.getString("questionTitle"), JOptionPane.QUESTION_MESSAGE);
            if (family==null || family.trim().length()==0) {
                // bail out
                return false;
            }*/
            return false;
        }
        _parent._family = family;
        _parent.updateFamiliesPanel();
        _parent.setFamily(family);
        return true;
    }

    /**
    * Action item for add new family. set up a dialog with new icons
    */
    protected void addFamilySet() {
        setVisible(false);
//        _parent.createNewFamilySet(_type);
        IconDialog dialog = new IconDialog(_type, null, _parent, null);
        dialog.sizeLocate();
    }

    /**
    * Action item for add delete family
    */
    protected void deleteFamilySet() {
        ItemPalette.removeIconMap(_type, _familyName.getText());
        _parent._family = null;
    	((FamilyItemPanel)_parent)._currentIconMap = null;
        _parent.updateFamiliesPanel();
    }

    protected void makeDoneButtonPanel(JPanel buttonPanel) {
        JPanel panel0 = new JPanel();
        panel0.setLayout(new FlowLayout());
        JButton doneButton = new JButton(ItemPalette.rbp.getString("doneButton"));
        doneButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    if (doDoneAction()) {
                        dispose();
                    }
                }
        });
        panel0.add(doneButton);

        JButton cancelButton = new JButton(ItemPalette.rbp.getString("cancelButton"));
        cancelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    _parent.updateFamiliesPanel();
                    dispose();
                }
        });
        panel0.add(cancelButton);
        buttonPanel.add(panel0);
    }

    protected void makeCreateButtonPanel(JPanel buttonPanel) {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        JButton newFamilyButton = new JButton(ItemPalette.rbp.getString("createNewFamily"));
        newFamilyButton.addActionListener(new ActionListener() {
                //IconDialog dialog; never used?
                public void actionPerformed(ActionEvent a) {
                    if (doDoneAction()) {
                        dispose();
                    }
                }
        });
        newFamilyButton.setToolTipText(ItemPalette.rbp.getString("ToolTipAddFamily"));
        panel.add(newFamilyButton);

        JButton cancelButton = new JButton(ItemPalette.rbp.getString("cancelButton"));
        cancelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    dispose();
                }
        });
        buttonPanel.add(panel);
        panel.add(cancelButton);
    }

    protected JPanel makeIconPanel(Hashtable<String, NamedIcon> iconMap) {
        if (iconMap==null) {
            log.error("iconMap is null for type "+_type+" family "+_family);
            return null;
        }
       JPanel iconPanel = new JPanel();
       GridBagLayout gridbag = new GridBagLayout();
       iconPanel.setLayout(gridbag);

       int cnt = _iconMap.size();
       int numCol = 2;
       if (cnt>6) {
           numCol = 3;
       }
       GridBagConstraints c = new GridBagConstraints();
       c.fill = GridBagConstraints.NONE;
       c.anchor = GridBagConstraints.CENTER;
       c.weightx = 1.0;
       c.weighty = 1.0;
       int gridwidth = cnt%numCol == 0 ? 1 : 2 ;
       c.gridwidth = gridwidth;
       c.gridheight = 1;
       c.gridx = -gridwidth;
       c.gridy = 0;

       if (log.isDebugEnabled()) log.debug("makeIconPanel: for "+iconMap.size()+" icons. gridwidth= "+gridwidth);
       int panelWidth = 0;
       Iterator<Entry<String, NamedIcon>> it = iconMap.entrySet().iterator();
       while (it.hasNext()) {
    	   Entry<String, NamedIcon> entry = it.next();
    	   NamedIcon icon = new NamedIcon(entry.getValue());    // make copy for possible reduction
    	   double scale = icon.reduceTo(100, 100, 0.2);
    	   JPanel panel = new JPanel();
    	   panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    	   String borderName = ItemPalette.convertText(entry.getKey());
    	   panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), 
    			   borderName));
    	   panel.add(Box.createHorizontalStrut(100));
    	   JLabel image = new DropJLabel(icon, _iconMap, _parent.isUpdate());
    	   image.setName(entry.getKey());
    	   if (icon.getIconWidth()<1 || icon.getIconHeight()<1) {
    		   image.setText(ItemPalette.rbp.getString("invisibleIcon"));
    		   image.setForeground(Color.lightGray);
    	   }
    	   JPanel iPanel = new JPanel();
    	   iPanel.add(image);

    	   c.gridx += gridwidth;
    	   if (c.gridx >= numCol*gridwidth) { //start next row
    		   c.gridy++;
    		   if (cnt < numCol) { // last row
    			   JPanel p =  new JPanel();
    			   p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    			   panelWidth = panel.getPreferredSize().width;
    			   p.add(Box.createHorizontalStrut(panelWidth));
    			   c.gridx = 0;
    			   c.gridwidth = 1;
    			   gridbag.setConstraints(p, c);
    			   //if (log.isDebugEnabled()) log.debug("makeIconPanel: gridx= "+c.gridx+" gridy= "+c.gridy);
    			   iconPanel.add(p);
    			   c.gridx = numCol-cnt;
    			   c.gridwidth = gridwidth;
    			   //c.fill = GridBagConstraints.NONE;
    		   } else {
    			   c.gridx = 0;
    		   }
    	   }
    	   cnt--;

    	   if (log.isDebugEnabled()) log.debug("makeIconPanel: icon width= "+icon.getIconWidth()+" height= "+icon.getIconHeight());
    	   if (log.isDebugEnabled()) log.debug("makeIconPanel: gridx= "+c.gridx+" gridy= "+c.gridy);
    	   panel.add(iPanel);
    	   JLabel label = new JLabel(java.text.MessageFormat.format(ItemPalette.rbp.getString("scale"),
    			   new Object[] {CatalogPanel.printDbl(scale,2)}));
    	   JPanel sPanel = new JPanel();
    	   sPanel.add(label);
    	   panel.add(sPanel);
    	   panel.add(Box.createHorizontalStrut(20));
    	   gridbag.setConstraints(panel, c);
    	   iconPanel.add(panel);
       }
       if (panelWidth > 0) {
    	   JPanel p =  new JPanel();
    	   p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    	   p.add(Box.createHorizontalStrut(panelWidth));
    	   c.gridx = numCol*gridwidth-1;
    	   c.gridwidth = 1;
    	   gridbag.setConstraints(p, c);
    	   //if (log.isDebugEnabled()) log.debug("makeIconPanel: gridx= "+c.gridx+" gridy= "+c.gridy);
    	   iconPanel.add(p);
       }
       return iconPanel;
    }

    void checkIconSizes() {
        Iterator <NamedIcon> iter = _iconMap.values().iterator();
        int lastWidth = 0;
        int lastHeight = 0;
        boolean first = true;
        while (iter.hasNext()) {
            NamedIcon icon = iter.next();
        	if (first) {
                lastWidth = icon.getIconWidth();
                lastHeight = icon.getIconHeight();
                first = false;
                continue;
        	}
           int nextWidth = icon.getIconWidth();
           int nextHeight = icon.getIconHeight();
           if ((Math.abs(lastWidth - nextWidth) > 3 || Math.abs(lastHeight - nextHeight) > 3)) {
               JOptionPane.showMessageDialog(_parent._paletteFrame, 
                                             ItemPalette.rb.getString("IconSizeDiff"), ItemPalette.rb.getString("warnTitle"),
                                             JOptionPane.WARNING_MESSAGE);
               return;
           }
            lastWidth = nextWidth;
            lastHeight = nextHeight;
        }
        if (log.isDebugEnabled()) log.debug("Size: width= "+lastWidth+", height= "+lastHeight); 
    }
    
    protected Hashtable<String, NamedIcon> clone(Hashtable<String, NamedIcon> map) {
        Hashtable<String, NamedIcon> clone = null;
        if (map!=null) {
        	clone = new Hashtable<String, NamedIcon>();
            Iterator<Entry<String, NamedIcon>> it = map.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, NamedIcon> entry = it.next();
                clone.put(entry.getKey(), new NamedIcon(entry.getValue()));
            }
        }
        return clone;
    }

    static Logger log = Logger.getLogger(IconDialog.class.getName());
}
