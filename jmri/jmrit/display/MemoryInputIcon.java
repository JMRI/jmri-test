
package jmri.jmrit.display;

import jmri.InstanceManager;
import jmri.Memory;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.FlowLayout;

import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import jmri.util.NamedBeanHandle;

/**
 * An icon to display and input a Memory value in a TextField.
 *<P>
 * Handles the case of either a String or an Integer in the 
 * Memory, preserving what it finds.
 *<P>
 * @author Pete Cressman  Copyright (c) 2009
 * @version $Revision: 1.23 $
 * @since 2.7.2
 */

public class MemoryInputIcon extends PositionableJPanel implements java.beans.PropertyChangeListener {

    JTextField  _textBox = new JTextField();
    int _nCols; 
    
    // the associated Memory object
    Memory memory = null;
    private NamedBeanHandle<Memory> namedMemory;
    
    
    public MemoryInputIcon(int nCols, Editor editor) {
        super(editor);
        _nCols = nCols;
        setDisplayLevel(Editor.LABELS);
        
        setLayout(new java.awt.GridBagLayout());
        add(_textBox, new java.awt.GridBagConstraints());
        _textBox.addKeyListener(new KeyAdapter() {
                public void keyReleased(KeyEvent e){
                    int key = e.getKeyCode();
                    if (key==KeyEvent.VK_ENTER || key==KeyEvent.VK_TAB) {
                        updateMemory();
                    }
                }
            });
        _textBox.setColumns(_nCols);
        _textBox.addMouseMotionListener(this);
        _textBox.addMouseListener(this);
        setPopupUtility(new PositionablePopupUtil(this, _textBox));
    }

    public Positionable deepClone() {
        MemoryInputIcon pos = new MemoryInputIcon(_nCols, _editor);
        return finishClone(pos);
    }

    public Positionable finishClone(Positionable p) {
        MemoryInputIcon pos = (MemoryInputIcon)p;
        pos.setMemory(getMemory().getName());
        return super.finishClone(pos);
    }

    public void mouseExited(java.awt.event.MouseEvent e) {
        updateMemory();
        super.mouseExited(e);
    }

    /**
     * Attached a named Memory to this display item
      * @param pName Used as a system/user name to lookup the Memory object
     */
     public void setMemory(String pName) {
         if (debug) log.debug("setMemory for memory= "+pName);
         if (InstanceManager.memoryManagerInstance()!=null) {
             memory = InstanceManager.memoryManagerInstance().provideMemory(pName);
             if (memory != null) {
                 setMemory(new NamedBeanHandle<Memory>(pName, memory));
             } else {
                 log.error("Memory '"+pName+"' not available, icon won't see changes");
             }
         } else {
             log.error("No MemoryManager for this protocol, icon won't see changes");
         }
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
            displayState();
            memory.addPropertyChangeListener(this);
            namedMemory = m;
        }
    }

    public void setNumColumns(int nCols) {
        _textBox.setColumns(nCols);
        _nCols = nCols;
    }

    public NamedBeanHandle<Memory> getMemory() { return namedMemory; }
    public int getNumColumns() { return _nCols; }
    
    // update icon as state of Memory changes
    public void propertyChange(java.beans.PropertyChangeEvent e) {
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
    
    private void updateMemory() {
        if (memory == null) return;
        String str = _textBox.getText();
        memory.setValue(str);
    }

    public boolean setEditIconMenu(javax.swing.JPopupMenu popup) {
        String txt = java.text.MessageFormat.format(rb.getString("EditItem"), rb.getString("Memory"));
        popup.add(new javax.swing.AbstractAction(txt) {
                public void actionPerformed(ActionEvent e) {
                    edit();
                }
            });
        return true;
    }

    /**
    * Poppup menu iconEditor's ActionListener
    */
    SpinnerNumberModel _spinModel = new SpinnerNumberModel(3,1,100,1);
    protected void edit() {
        IconAdder iconEditor = new IconAdder("Memory") {
                JSpinner spinner = new JSpinner(_spinModel);
                protected void addAdditionalButtons(JPanel p) {
                    ((JSpinner.DefaultEditor)spinner.getEditor()).getTextField().setColumns(2);
                    spinner.setMaximumSize(spinner.getPreferredSize());
                    spinner.setValue(Integer.valueOf(_textBox.getColumns()));
                    JPanel p2 = new JPanel();
                    //p2.setLayout(new BoxLayout(p2, BoxLayout.X_AXIS));
                    p2.setLayout(new FlowLayout(FlowLayout.TRAILING));
                    p2.add(new JLabel(rb.getString("NumColsLabel")));
                    p2.add(spinner);
                    p.add(p2);
                }
        };

        makeIconEditorFrame(this, "Memory", true, iconEditor);
        _iconEditor.setPickList(jmri.jmrit.picker.PickListModel.memoryPickModelInstance());
        ActionListener addIconAction = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                editMemory();
            }
        };
        _iconEditor.complete(addIconAction, false, true, true);
        _iconEditor.setSelection(memory);
    }
    void editMemory() {
        setMemory(_iconEditor.getTableSelection().getDisplayName());
        _nCols = _spinModel.getNumber().intValue();
        _textBox.setColumns(_nCols);
        setSize(getPreferredSize().width+1, getPreferredSize().height);
        _iconEditorFrame.dispose();
        _iconEditorFrame = null;
        _iconEditor = null;
        validate();
    }

    /**
     * Drive the current state of the display from the state of the
     * Memory.
     */
    public void displayState() {
        if (debug) log.debug("displayState");
    	if (memory == null) {  // leave alone if not connected yet
    		return;
    	}
        Object show = memory.getValue();
        if (show!=null)
            _textBox.setText(show.toString());
        else
            _textBox.setText("");            
    }

    void cleanup() {
        if (memory!=null) {
            memory.removePropertyChangeListener(this);
        }
        _textBox.removeMouseMotionListener(this);
        _textBox.removeMouseListener(this);
        _textBox = null;
        memory = null;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MemoryInputIcon.class.getName());
}
