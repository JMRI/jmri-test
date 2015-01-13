package jmri.jmrit.beantable;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

 /**
   * Handle painting checkbox classes in
   * a JTable.
   * <P>
   * Beyond the normal behavior of providing a checkbox
   * to show the value, this disables the JCheckBox if the cell
   * is not editable.  This makes the visual behavior more in 
   * line with user expectations.
   *
   * @author Bob Jacobsen
   * @version $Revision$
   */
public class EnablingCheckboxRenderer extends JCheckBox implements TableCellRenderer {
   
   /**
	 * 
	 */
	private static final long serialVersionUID = 294261257192050582L;

public EnablingCheckboxRenderer() {
      super();
      setHorizontalAlignment(0);
   }
   
    /**
      * Override this method from the parent class.
      * Only paint the background if the row isn't selected
      * Paint every other row a color very similiar to the base color, just a little
      * darker
      * @param table the JTable component
      * @param value the cell content's object
      * @param isSelected boolean so we know if this is the currently selected row
      * @param hasFocus does this cell currently have focus?
      * @param row the row number
      * @param column the column number
      * @return the JCheckBox to display
      */
   public Component getTableCellRendererComponent(JTable table, java.lang.Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      setSelected(value!=null && ((Boolean)value).booleanValue());
      setEnabled(table.isCellEditable(row, column));
      return this;
   }
}