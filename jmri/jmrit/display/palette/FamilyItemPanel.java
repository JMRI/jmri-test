package jmri.jmrit.display.palette;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.awt.datatransfer.DataFlavor;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.swing.*;

import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.Editor;
import jmri.util.JmriJFrame;

/**
*  ItemPanel for for plain icons and backgrounds 
*/
public abstract class FamilyItemPanel extends ItemPanel {

    protected JPanel    _iconFamilyPanel;
    protected JPanel    _iconPanel;     // panel contained in _iconFamilyPanel - all icons in family
    protected JPanel    _dragIconPanel; // contained in _iconFamilyPanel - sample to drag to control panel
    JPanel    _bottom1Panel;  // Typically _showIconsButton and editIconsButton 
    JPanel    _bottom2Panel;  // createIconFamilyButton - when all families deleted 
    JButton   _showIconsButton;
    JButton   _updateButton;
    protected JTextField _familyName;
    protected boolean   _update = false;    // Editing existing icon, do not allow icon dragging
    protected boolean   _updateWithSameMap = false; // when editing existing icon, do not change family
    protected Hashtable<String, NamedIcon> _currentIconMap;

    /**
    * Constructor types with multiple families and multiple icon families
    */
    public FamilyItemPanel(JmriJFrame parentFrame, String type, String family, Editor editor) {
        super(parentFrame, type, family, editor);
    }

    /**
    * Init for creation
    * _bottom1Panel and _bottom2Panel alternate visibility in bottomPanel depending on
    * whether icon families exist.  They are made first because they are referenced in
    * initIconFamiliesPanel()
    * subclasses will insert other panels
    */
    public void init() {
        _update = false;
        _bottom1Panel = makeBottom1Panel();
        _bottom2Panel = makeBottom2Panel();
        initIconFamiliesPanel();
        add(_iconFamilyPanel);
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(_bottom1Panel);
        bottomPanel.add(_bottom2Panel);
        add(bottomPanel);
        if (log.isDebugEnabled()) log.debug("init done for family "+_family);
    }

    /**
    * Init for update of existing track block
    * _bottom3Panel has "Update Panel" button put into _bottom1Panel
    */
    public void init(ActionListener doneAction, Hashtable<String, NamedIcon> iconMap) {
        if (iconMap!=null) {
            checkCurrentMap(iconMap);   // is map in families?, does user want to add it? etc
        }
        _update = true;     // no dragging of a new icon
        _bottom2Panel = makeBottom2Panel();
        _bottom1Panel = makeBottom3Panel(doneAction, makeBottom1Panel());
        initIconFamiliesPanel();
        add(_iconFamilyPanel);
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(_bottom1Panel);
        bottomPanel.add(_bottom2Panel);
        add(bottomPanel);
        if (log.isDebugEnabled()) log.debug("init done for family "+_family);
    }

    /**
    * iconMap is existing map of the icon.  Check whether map is one of the
    * families. if so, return.  if not, does user want to add it to families?
    * if so, add.  If not, save for return when updated.
    */
    protected void checkCurrentMap(Hashtable<String, NamedIcon> iconMap) {
        _currentIconMap = iconMap;
        if (log.isDebugEnabled()) log.debug("checkCurrentMap: for type \""+_itemType+"\", family \""+_family+"\"");
        if (_family!=null && _family.trim().length()>0) {
            Hashtable<String, NamedIcon> map = ItemPalette.getIconMap(_itemType, _family);
            if (map!=null) {
                return;     // Must assume no family names were changed
            }
        }
        int result = JOptionPane.showConfirmDialog(_paletteFrame,
                            ItemPalette.rbp.getString("NoFamilyName"), ItemPalette.rb.getString("questionTitle"),
                            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (result==JOptionPane.NO_OPTION) {
            _updateWithSameMap = true;
            return;
        }
        if (_family!=null && _family.trim().length()>0) {
            if (ItemPalette.addFamily(_paletteFrame, _itemType, _family, iconMap)) {
                return;
            }
        }
        do {
            _family = JOptionPane.showInputDialog(_paletteFrame, ItemPalette.rbp.getString("EnterFamilyName"), 
                    ItemPalette.rb.getString("questionTitle"), JOptionPane.QUESTION_MESSAGE);
            if (_family==null || _family.trim().length()==0) {
                // bail out
                _updateWithSameMap = true;
                return;
            }
        } while (!ItemPalette.addFamily(_paletteFrame, _itemType, _family, iconMap));
    }

    protected void initIconFamiliesPanel() {
        _iconFamilyPanel = new JPanel();
        _iconFamilyPanel.setLayout(new BoxLayout(_iconFamilyPanel, BoxLayout.Y_AXIS));

        Hashtable <String, Hashtable<String, NamedIcon>> families = ItemPalette.getFamilyMaps(_itemType);
        if (families!=null && families.size()>0) {
            JPanel familyPanel = makeFamilyButtons(families.keySet().iterator(),
                                                   (_currentIconMap==null));
            Hashtable<String, NamedIcon> map = _currentIconMap;
            if (map==null) {
                map = families.get(_family);
            }
            // make _iconPanel & _dragIconPanel before calls to add icons
            addFamilyPanels(familyPanel);
            if (map==null) {
                JOptionPane.showMessageDialog(_paletteFrame, 
                        java.text.MessageFormat.format(ItemPalette.rbp.getString("FamilyNotFound"),
                                                       ItemPalette.rbp.getString(_itemType), _family), 
                        ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
            } else {
                addIconsToPanel(map);        // need to have family iconMap identified before calling
                makeDndIconPanel(map, "BeanStateUnknown");
            }
        } else {
            addCreatePanels();
        }
    }

    protected void updateFamiliesPanel() {
        if (log.isDebugEnabled()) log.debug("updateFamiliesPanel for "+_itemType);
        removeIconFamiliesPanel();
        initIconFamiliesPanel();
        add(_iconFamilyPanel, 1);
        reset();
        validate();
    }
    
    protected JPanel makeFamilyButtons (Iterator <String> it, boolean setDefault) {
        JPanel familyPanel = new JPanel();
        familyPanel.setLayout(new BoxLayout(familyPanel, BoxLayout.Y_AXIS));
        String txt = java.text.MessageFormat.format(ItemPalette.rbp.getString("IconFamiliesLabel"),
                                                    ItemPalette.rbp.getString(_itemType));
        JPanel p = new JPanel();
        p.add(new JLabel(txt));
        familyPanel.add(p);
        ButtonGroup group = new ButtonGroup();
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());  //new BoxLayout(p, BoxLayout.Y_AXIS)
        String family = null;
        JRadioButton button = null;
        int count = 0;
        while (it.hasNext()) {
            family = it.next();
            count++;
            button = new JRadioButton(ItemPalette.convertText(family));
            button.addActionListener(new ActionListener() {
                    String family;
                    public void actionPerformed(ActionEvent e) {
                        setFamily(family);
                    }
                    ActionListener init(String f) {
                        family = f;
                        if (log.isDebugEnabled()) log.debug("ActionListener.init : for type \""+_itemType+"\", family \""+family+"\"");
                        return this;
                    }
                }.init(family));
            if (family.equals(_family)) {
                button.setSelected(true);
            }
            if (count>4) {
                count = 0;
                familyPanel.add(buttonPanel);
                buttonPanel = new JPanel();
                buttonPanel.setLayout(new FlowLayout());  //new BoxLayout(p, BoxLayout.Y_AXIS)
            }
            buttonPanel.add(button);
            group.add(button);
        }
        familyPanel.add(buttonPanel);
        if (_family==null && setDefault) {
            _family = family;       // let last familiy be the selected one
            button.setSelected(true);
        }
        familyPanel.add(buttonPanel);
        return familyPanel;
    }

    protected void addFamilyPanels(JPanel familyPanel) {
        _iconPanel = new JPanel();
        _iconFamilyPanel.add(_iconPanel);
        _iconPanel.setVisible(false);
        if (!_update) {
            _dragIconPanel = new JPanel();
            _iconFamilyPanel.add(_dragIconPanel);
            _dragIconPanel.setVisible(true);
        }
        _iconFamilyPanel.add(familyPanel);
        _bottom1Panel.setVisible(true);
        _bottom2Panel.setVisible(false);
    }

    protected void addCreatePanels() {
        JOptionPane.showMessageDialog(_paletteFrame, 
                java.text.MessageFormat.format(ItemPalette.rbp.getString("AllFamiliesDeleted"), 
                                               ItemPalette.rbp.getString(_itemType)), 
                ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
        _bottom1Panel.setVisible(false);
        _bottom2Panel.setVisible(true);
    }
    
    protected void addIconsToPanel(Hashtable<String, NamedIcon> iconMap) {
        if (iconMap==null) {
            log.error("iconMap is null for type "+_itemType+" family "+_family);
            return;
        }
        GridBagLayout gridbag = new GridBagLayout();
        _iconPanel.setLayout(gridbag);

        int numCol = 4;
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.CENTER;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.gridx = -1;
        c.gridy = 0;

        int cnt = iconMap.size();
        Iterator<Entry<String, NamedIcon>> it = iconMap.entrySet().iterator();
        while (it.hasNext()) {
           Entry<String, NamedIcon> entry = it.next();
           NamedIcon icon = new NamedIcon(entry.getValue());    // make copy for possible reduction
           icon.reduceTo(100, 100, 0.2);
           JPanel panel = new JPanel();
           String borderName = ItemPalette.convertText(entry.getKey());
           panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), 
                                                            borderName));
           JLabel image = new JLabel(icon);
           if (icon.getIconWidth()<1 || icon.getIconHeight()<1) {
               image.setText(ItemPalette.rbp.getString("invisibleIcon"));
               image.setForeground(Color.lightGray);
           }
           panel.add(image);
           int width = Math.max(100, panel.getPreferredSize().width);
           panel.setPreferredSize(new java.awt.Dimension(width, panel.getPreferredSize().height));
           c.gridx += 1;
           if (c.gridx >= numCol) { //start next row
               c.gridy++;
               c.gridx = 0;
               if (cnt < numCol-1) { // last row
                   JPanel p =  new JPanel();
                   p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
                   p.add(Box.createHorizontalStrut(100));
                   gridbag.setConstraints(p, c);
                   //if (log.isDebugEnabled()) log.debug("addIconsToPanel: gridx= "+c.gridx+" gridy= "+c.gridy);
                   _iconPanel.add(p);
                   c.gridx = 1;
               }
           }
           cnt--;
           gridbag.setConstraints(panel, c);
           _iconPanel.add(panel);
        }
    }

    abstract protected JLabel getDragger(DataFlavor flavor, Hashtable <String, NamedIcon> map);

    protected void makeDndIconPanel(Hashtable<String, NamedIcon> iconMap, String displayKey) {
        if (_update) {
            return;
        }
        _dragIconPanel.setToolTipText(ItemPalette.rbp.getString("ToolTipDragIcon"));
        if (iconMap!=null) {
            NamedIcon ic = iconMap.get(displayKey);
            if (ic!=null) {
                NamedIcon icon = new NamedIcon(ic);
               JPanel panel = new JPanel();
               String borderName = ItemPalette.convertText("dragToPanel");
               panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), 
                                                                borderName));
               JLabel label;
               try {
                   label = getDragger(new DataFlavor(Editor.POSITIONABLE_FLAVOR), iconMap);
                   label.setToolTipText(ItemPalette.rbp.getString("ToolTipDragIcon"));
               } catch (java.lang.ClassNotFoundException cnfe) {
                   cnfe.printStackTrace();
                   label = new JLabel();
               }
               label.setIcon(icon);
               label.setName(borderName);
               panel.add(label);
               int width = Math.max(100, panel.getPreferredSize().width);
               panel.setPreferredSize(new java.awt.Dimension(width, panel.getPreferredSize().height));
               panel.setToolTipText(ItemPalette.rbp.getString("ToolTipDragIcon"));
               _dragIconPanel.add(panel);
               return;
            }
        } else {
            JOptionPane.showMessageDialog(_paletteFrame, 
                    java.text.MessageFormat.format(ItemPalette.rbp.getString("FamilyNotFound"), 
                                                   ItemPalette.rbp.getString(_itemType), _family), 
                    ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
        }
    }    

    protected JPanel makeBottom1Panel() {
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout());
        _showIconsButton = new JButton(ItemPalette.rbp.getString("ShowIcons"));
        _showIconsButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    if (_iconPanel.isVisible()) {
                        hideIcons();
                    } else {
                        showIcons();
                    }
                }
        });
        _showIconsButton.setToolTipText(ItemPalette.rbp.getString("ToolTipShowIcons"));
        bottomPanel.add(_showIconsButton);

        JButton editIconsButton = new JButton(ItemPalette.rbp.getString("ButtonEditIcons"));
        editIconsButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    openEditDialog();
                }
        });
        editIconsButton.setToolTipText(ItemPalette.rbp.getString("ToolTipEditIcons"));
        bottomPanel.add(editIconsButton);
        return bottomPanel;
    }

    protected void hideIcons() {
        _iconPanel.setVisible(false);
        if (!_update) {
            _dragIconPanel.setVisible(true);
        }
        _showIconsButton.setText(ItemPalette.rbp.getString("ShowIcons"));
    }

    protected void showIcons() {
        _iconPanel.setVisible(true);
        if (!_update) {
            _dragIconPanel.setVisible(false);
        }
        _showIconsButton.setText(ItemPalette.rbp.getString("HideIcons"));
    }
   
    
    /**
    *  Replacement panel for _bottom1Panel when no icon families exist for _itemType 
    */
    protected JPanel makeBottom2Panel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        JButton newFamilyButton = new JButton(ItemPalette.rbp.getString("createNewFamily"));
        newFamilyButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    createNewFamily(_itemType);
                }
        });
        newFamilyButton.setToolTipText(ItemPalette.rbp.getString("ToolTipAddFamily"));
        panel.add(newFamilyButton);

        JButton cancelButton = new JButton(ItemPalette.rbp.getString("cancelButton"));
        cancelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    updateFamiliesPanel();
                }
        });
        panel.add(cancelButton);
        return panel;
    }

    // add update buttons to  bottom1Panel
    protected JPanel makeBottom3Panel(ActionListener doneAction, JPanel bottom1Panel) {
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.add(bottom1Panel);
        JPanel updatePanel = new JPanel();
        _updateButton = new JButton(ItemPalette.rbp.getString("updateButton"));
        _updateButton.addActionListener(doneAction);
        _updateButton.setToolTipText(ItemPalette.rbp.getString("ToolTipPickFromTable"));
        updatePanel.add(_updateButton);
        bottomPanel.add(updatePanel);
        return bottomPanel;
    }
    
    protected void removeIconFamiliesPanel() {
        remove(_iconFamilyPanel);
    }
    protected void reset() {
        hideIcons();
    }
    
    /**
    * Action of family radio button
    */
    protected void setFamily(String family) {
        _family = family;
        if (log.isDebugEnabled()) log.debug("setFamily: for type \""+_itemType+"\", family \""+family+"\"");
        _iconFamilyPanel.remove(_iconPanel);
        _iconPanel = new JPanel();
        _iconFamilyPanel.add(_iconPanel, 0);
        if (!_update) {
            _iconFamilyPanel.remove(_dragIconPanel);
            _dragIconPanel = new JPanel();
            _iconFamilyPanel.add(_dragIconPanel, 0);
        }
        Hashtable<String, NamedIcon> iconMap = ItemPalette.getIconMap(_itemType, _family);
        addIconsToPanel(iconMap);
        makeDndIconPanel(iconMap, "BeanStateUnknown");
        _updateWithSameMap = false;     // not using saved update map
        hideIcons();
    }

    public boolean isUpdateWithSameMap() {
        return _updateWithSameMap;
    }

    // return icon set to panel icon
    public Hashtable <String, NamedIcon> getIconMap() {
        Hashtable <String, NamedIcon> iconMap = null;
        if (_updateWithSameMap) {
            iconMap = _currentIconMap;
        } else {
            iconMap = ItemPalette.getIconMap(_itemType, _family);
        }
        if (iconMap==null) {
            iconMap = ItemPalette.getIconMap(_itemType, _family);
            if (iconMap==null) {
                JOptionPane.showMessageDialog(_paletteFrame, 
                        java.text.MessageFormat.format(ItemPalette.rbp.getString("FamilyNotFound"), 
                                                       ItemPalette.rbp.getString(_itemType), _family), 
                        ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
                return null;
            }
        }        
        return iconMap;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(FamilyItemPanel.class.getName());
}

