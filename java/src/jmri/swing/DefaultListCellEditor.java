/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.swing;

import java.awt.Component;
import javax.swing.*;

/**
 *
 * @author rhwood
 */
public class DefaultListCellEditor<E> extends DefaultCellEditor implements ListCellEditor<E> {

    /**
	 * 
	 */
	private static final long serialVersionUID = 7557104352512667759L;

	public DefaultListCellEditor(final JCheckBox checkBox) {
        super(checkBox);
    }

    public DefaultListCellEditor(final JComboBox comboBox) {
        super(comboBox);
    }

    public DefaultListCellEditor(final JTextField textField) {
        super(textField);
    }

    @Override
    public Component getListCellEditorComponent(JList<E> list, E value, boolean isSelected, int index) {
        delegate.setValue(value);
        return editorComponent;
    }
    
    @Override
    @SuppressWarnings("unchecked") // made safe by construction
    public E getCellEditorValue() { return (E) super.getCellEditorValue(); }

}