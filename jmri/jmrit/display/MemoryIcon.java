package jmri.jmrit.display;

import jmri.InstanceManager;
import jmri.Memory;
import jmri.jmrit.catalog.NamedIcon;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import javax.swing.JTextField;
import javax.swing.JOptionPane;
import java.awt.event.ActionListener;
import jmri.util.NamedBeanHandle;

/**
 * An icon to display a status of a Memory.<P>
 * <P>
 * The value of the memory can't be changed with this icon.
 *<P>
 * @author Bob Jacobsen  Copyright (c) 2004
 * @version $Revision: 1.53 $
 */

public class MemoryIcon extends PositionableLabel implements java.beans.PropertyChangeListener {

	NamedIcon defaultIcon = null;
    // the associated Memory object
    protected Memory memory = null;
    // the map of icons
    java.util.HashMap<String, NamedIcon> map = null;
    private NamedBeanHandle<Memory> namedMemory;
    private boolean _tmpBorder = false;
    
    public MemoryIcon(String s, Editor editor) {
        super(s, editor);
        setDisplayLevel(Editor.MEMORIES);
        resetDefaultIcon();
        //setIcon(defaultIcon);
        _namedIcon=defaultIcon;
        //updateSize();
    }

    public MemoryIcon(NamedIcon s, Editor editor) {
        super(s, editor);
        setDisplayLevel(Editor.LABELS);
        defaultIcon = s;
        //updateSize();
        log.debug("MemoryIcon ctor= "+MemoryIcon.class.getName());
    }

    public void resetDefaultIcon() {
        defaultIcon = new NamedIcon("resources/icons/misc/X-red.gif",
                            "resources/icons/misc/X-red.gif");
    }
    
	public void setDefaultIcon(NamedIcon n) {
        defaultIcon = n;
	}
	
	public NamedIcon getDefaultIcon() {
	    return defaultIcon;
	}
	
	private void setMap() {
        if (map==null) map = new java.util.HashMap<String, NamedIcon>();
	}

    /**
     * Attached a named Memory to this display item
      * @param pName Used as a system/user name to lookup the Memory object
     */
     public void setMemory(String pName) {
         if (InstanceManager.memoryManagerInstance()!=null) {
             memory = InstanceManager.memoryManagerInstance().
                 provideMemory(pName);
             if (memory != null) {
                 setMemory(new NamedBeanHandle<Memory>(pName, memory));
             } else {
                 log.error("Memory '"+pName+"' not available, icon won't see changes");
             }
         } else {
             log.error("No MemoryManager for this protocol, icon won't see changes");
         }
         updateSize();
     }

    /**
     * Attached a named Memory to this display item
     * @param m The Memory object
     */
    public void setMemory(NamedBeanHandle<Memory> m) {
        if (memory != null) {
            memory.removePropertyChangeListener(this);
        }
        memory = InstanceManager.memoryManagerInstance().provideMemory(m.getName());
        if (memory != null) {
            memory.addPropertyChangeListener(this);
            displayState();
            namedMemory = m;
        }
    }
    
    public NamedBeanHandle<Memory> getMemory() { return namedMemory; }

    public java.util.HashMap<String, NamedIcon> getMap() { return map; }

    // display icons

    public void addKeyAndIcon(NamedIcon icon, String keyValue) {
        if (map == null) setMap(); // initialize if needed
    	map.put(keyValue, icon);
    	// drop size cache
    	//height = -1;
    	//width = -1;
        displayState(); // in case changed
    }

    // update icon as state of Memory changes
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (log.isDebugEnabled()) log.debug("property change: "
                                            +e.getPropertyName()
                                            +" is now "+e.getNewValue());
	if (e.getPropertyName().equals("value")) {
            displayState();
        }
    }

    public String getNameString() {
        String name;
        if (memory == null) name = rb.getString("NotConnected");
        else if (memory.getUserName()!=null)
            name = memory.getUserName()+" ("+memory.getSystemName()+")";
        else
            name = memory.getSystemName();
        return name;
    }


    public void setSelectable(boolean b) {selectable = b;}
    public boolean isSelectable() { return selectable;}
    boolean selectable = false;
    
    public boolean showPopUp(JPopupMenu popup) {
        if (isEditable() && selectable) {
            popup.add(new JSeparator());
    
            java.util.Iterator<String> iterator = map.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next().toString();
                //String value = ((NamedIcon)map.get(key)).getName();
                popup.add(new AbstractAction(key) {
                    public void actionPerformed(ActionEvent e) {
                        String key = e.getActionCommand();
                        memory.setValue(key);
                    }
                });
            }
            return true;
        }  // end of selectable
        return false;
    }

    /**
    * Text edits cannot be done to Memory text - override
    */    
    public boolean setTextEditMenu(JPopupMenu popup) {
        popup.add(new AbstractAction(rb.getString("EditMemoryValue")) {
            public void actionPerformed(ActionEvent e) {
                editMemoryValue();
            }
        });
        return true;
    }

    /**
     * Drive the current state of the display from the state of the
     * Memory.
     */
    public void displayState() {
        if (log.isDebugEnabled()) log.debug("displayState");
    	if (memory == null) {  // use default if not connected yet
            setIcon(defaultIcon);
    		updateSize();
    		return;
    	}
		Object key = memory.getValue();
		if (key != null) {
		    if (map == null) {
		        // no map, attempt to show object directly
                Object val = key;
                if (val instanceof String) {
                    String str = (String)val;
                    setText(str);
                    if (log.isDebugEnabled()) log.debug("String str= \""+str+"\" str.trim().length()= "+str.trim().length());
                    /*  MemoryIconTest says empty strings should show blank */
                    // use a temp border to keep item selectable 
                    if (str.trim().length()==0 && 
                                (maxWidth() < (PositionablePopupUtil.MIN_SIZE+2) ||
                                    maxHeight() < (PositionablePopupUtil.MIN_SIZE+2)) ) {
                        if (getPopupUtility().getMargin()==0) {
                           _tmpBorder = true;
                           getPopupUtility().setMargin(1);
                           getPopupUtility().setBorderSize(1);
                           getPopupUtility().setBorderColor(java.awt.Color.black);
                        }
                       setIcon(defaultIcon);
                    } else {
                        if (_tmpBorder) {
                            _tmpBorder = false;
                            getPopupUtility().setBorderSize(0);
                            getPopupUtility().setMargin(0);
                        }
                    }
                    setIcon(null);
                    _icon = false;
                    _text = true;
                } else if (val instanceof javax.swing.ImageIcon) {
                    setIcon((javax.swing.ImageIcon) val);
                    setText(null);
                    _icon = true;
                    _text = false;
                } else if (val instanceof Number) {
                    setText(val.toString());
                    setIcon(null);
                    _icon = false;
                    _text = true;
                } else log.warn("can't display current value of "+memory.getSystemName()+
                                ", val= "+val+" of Class "+val.getClass().getName());
		    } else {
		        // map exists, use it
			    NamedIcon newicon = map.get(key.toString());
			    if (newicon!=null) {
                    
                    setText(null);
				    super.setIcon(newicon);
			    } else {
			        // no match, use default
		            setIcon(defaultIcon);                    
                    setText(null);
                    _icon = true;
                    _text = false;
			    }
		    }
		} else {
            if (log.isDebugEnabled()) log.debug("memory null");
            setIcon(defaultIcon);
            setText(null);
            _icon = true;
            _text = false;
        }
        updateSize();
    }

    public boolean setEditIconMenu(JPopupMenu popup) {
        popup.add(new AbstractAction(rb.getString("EditIcon")) {
                public void actionPerformed(ActionEvent e) {
                    edit();
                }
            });
        return true;
    }

    protected void edit() {
        if (showIconEditorFrame(this)) {
            return;
        }
        _iconEditor = new IconAdder();
        ActionListener addIconAction = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                editMemory();
            }
        };
        _iconEditorFrame = makeAddIconFrame("ChangeMemory", "addMemValueToPanel", 
                                             "SelectMemory", _iconEditor, this);
        _iconEditor.setPickList(jmri.jmrit.picker.PickListModel.memoryPickModelInstance());
        _iconEditor.complete(addIconAction, null, true, true);
        _iconEditor.setSelection(memory);
    }
    void editMemory() {
        setMemory(_iconEditor.getTableSelection().getDisplayName());
        updateSize();
        _iconEditorFrame.dispose();
        _iconEditorFrame = null;
        _iconEditor = null;
        invalidate();
    }

    public void dispose() {
        memory.removePropertyChangeListener(this);
        memory = null;
        super.dispose();
    }
    
    public void doMouseClicked(java.awt.event.MouseEvent e) {
        //if (log.isDebugEnabled()) log.debug("doMouseReleased");
        if (e.getClickCount() == 2) { // double click?
            editMemoryValue();
        }
    }    
    
    private void editMemoryValue(){
        JTextField newMemory = new JTextField(20);
        if (memory.getValue()!=null)
            newMemory.setText(memory.getValue().toString());
        Object[] options = {"Cancel", "OK", newMemory};
        int retval = JOptionPane.showOptionDialog(this,
                                                  "Edit Current Memory Value", memory.getSystemName(),
                                                  0, JOptionPane.INFORMATION_MESSAGE, null,
                                                  options, options[2] );

        if (retval != 1) return;
        memory.setValue(newMemory.getText());
        updateSize();
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MemoryIcon.class.getName());
}
