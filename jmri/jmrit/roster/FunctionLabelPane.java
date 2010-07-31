// FunctionEntryPane.java

package jmri.jmrit.roster;

import jmri.util.davidflanagan.HardcopyWriter;

import java.awt.*;
import javax.swing.*;
import java.util.ResourceBundle;
import java.io.IOException;
import java.util.Vector;


/**
 * Display and edit the function labels in a RosterEntry
 *
 * @author	Bob Jacobsen   Copyright (C) 2008
 * @version	$Revision: 1.7 $
 */
public class FunctionLabelPane extends javax.swing.JPanel {
    RosterEntry re;
    
    final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.roster.JmritRosterBundle");

    JTextField[] labels;
    JCheckBox[] lockable;
    
    // we're doing a manual allocation of position for
    // now, based on 28 labels
    private int maxfunction = 28;
    
    public FunctionLabelPane(RosterEntry r) {

        re = r;
        
        GridBagLayout gbLayout = new GridBagLayout();
        GridBagConstraints cL = new GridBagConstraints();
        setLayout(gbLayout);

        labels = new JTextField[maxfunction+1];
        lockable = new JCheckBox[maxfunction+1];
        
        cL.gridx = 0;
        cL.gridy = 0;
        cL.ipadx = 3;
        cL.anchor = GridBagConstraints.NORTHWEST;
        cL.insets = new Insets (0,0,0,15);
        cL.fill = GridBagConstraints.HORIZONTAL;
        cL.weighty = 1.0;
        int nextx = 0;
        
        add(new JLabel("fn"), cL);
        cL.gridx++;
        add(new JLabel("label"), cL);
        cL.gridx++;
        add(new JLabel("lock"), cL);
        cL.gridx++;
        add(new JLabel("fn"), cL);
        cL.gridx++;
        add(new JLabel("label"), cL);
        cL.gridx++;
        add(new JLabel("lock"), cL);
        cL.gridx++;
        
        cL.gridx = 0;
        cL.gridy = 1;
        for (int i = 0; i<=maxfunction; i++) {
            // label the row
            add(new JLabel(""+i), cL);
            cL.gridx++;
            
            // add the label
            labels[i] = new JTextField(20);
            if (r.getFunctionLabel(i)!=null) labels[i].setText(r.getFunctionLabel(i));
            add(labels[i], cL);
            cL.gridx++;
            
            // add the checkbox
            lockable[i] = new JCheckBox();
            lockable[i].setSelected(r.getFunctionLockable(i));
            add(lockable[i], cL);
            cL.gridx++;

            // advance position
            cL.gridy++;
            if (cL.gridy-1 == ((maxfunction+1)/2)+1) {
                cL.gridy = 1;  // skip titles
                nextx = nextx+3;
            }
            cL.gridx = nextx;
        }
    }
    
    /**
     * Does the GUI contents agree with a RosterEntry?
     */
    public boolean guiChanged(RosterEntry r) {
        if (labels!=null) {
            for (int i = 0; i<labels.length; i++) 
               if (labels[i]!=null) {
                    if (r.getFunctionLabel(i)==null && !labels[i].getText().equals(""))
                        return true;
                    if (r.getFunctionLabel(i)!=null && !r.getFunctionLabel(i).equals(labels[i].getText()))
                        return true;
                }
        }
        if (lockable!=null) {
            for (int i = 0; i<lockable.length; i++) 
                if (lockable[i]!=null) {
                    if (r.getFunctionLockable(i) && !lockable[i].isSelected())
                        return true;
                    if (!r.getFunctionLockable(i) && lockable[i].isSelected())
                        return true;
                }
        }
        return false;        
    }
        
    /** 
     * Fill a RosterEntry object from GUI contents
     **/
    public void update(RosterEntry r) {
        if (labels!=null) {
            for (int i = 0; i<labels.length; i++) 
                if (labels[i]!=null && !labels[i].getText().equals("")) {
                    r.setFunctionLabel(i, labels[i].getText());
                    r.setFunctionLockable(i, lockable[i].isSelected());
                } else if (labels[i]!=null && labels[i].getText().equals("")) {
                    if (r.getFunctionLabel(i) != null) {
                        r.setFunctionLabel(i, null);
                    }
                }
        }
    }

    public void dispose() {
        if (log.isDebugEnabled()) log.debug("dispose");
    }
    
    public boolean includeInPrint() { return print; }
    public void includeInPrint(boolean inc) { print = inc; }
    boolean print = false;
    
    public void printPane(HardcopyWriter w) {
        // if pane is empty, don't print anything
        //if (varList.size() == 0 && cvList.size() == 0) return;
        // future work needed her to print indexed CVs

        // Define column widths for name and value output.
        // Make col 2 slightly larger than col 1 and reduce both to allow for
        // extra spaces that will be added during concatenation
        int col1Width = w.getCharactersPerLine()/2 -3 - 5;
        int col2Width = w.getCharactersPerLine()/2 -3 + 5;

        try {
            //Create a string of spaces the width of the first column
            String spaces = "";
            for (int i=0; i < col1Width; i++) {
              spaces = spaces + " ";
            }
            // start with pane name in bold
            String heading1 = "Function";
            String heading2 = "Description";
            String s;
            int interval = spaces.length()- heading1.length();
            w.setFontStyle(Font.BOLD);
            // write the section name and dividing line
            s = "FUNCTION LABELS";
            w.write(s, 0, s.length());
            w.writeBorders();
            //Draw horizontal dividing line for each Pane section
            w.write(w.getCurrentLineNumber(), 0, w.getCurrentLineNumber(),
                  w.getCharactersPerLine()+1);
            s = "\n";
            w.write(s,0,s.length());

            w.setFontStyle(Font.BOLD + Font.ITALIC);
            s = "   " + heading1 + spaces.substring(0,interval) + "   " + heading2;
            w.write(s, 0, s.length());
            w.writeBorders();
            s = "\n";
            w.write(s,0,s.length());
            w.setFontStyle(Font.PLAIN);
            // Define a vector to store the names of variables that have been printed
            // already.  If they have been printed, they will be skipped.
            // Using a vector here since we don't know how many variables will
            // be printed and it allows expansion as necessary
            Vector<String> printedVariables = new Vector<String>(10,5);
            // index over variables
            for (int i=0; i<maxfunction; i++) {
                String name = ""+i;
                if(re.getFunctionLockable(i))
                    name = name + " (locked)";
                String value = re.getFunctionLabel(i);
                //Skip Blank functions
                if (value!=null){
                    String originalName = name;

                    //define index values for name and value substrings
                    int nameLeftIndex = 0;
                    int nameRightIndex = name.length();
                    int valueLeftIndex = 0;
                    int valueRightIndex = 0;
                    valueRightIndex =value.length();
                    String trimmedName;
                    String trimmedValue;

                    // Check the name length to see if it is wider than the column.
                    // If so, split it and do the same checks for the Value
                    // Then concatenate the name and value (or the split versions thereof)
                    // before writing - if split, repeat until all pieces have been output
                    while ((valueLeftIndex < value.length()) || (nameLeftIndex < name.length())){
                      // name split code
                      if (name.substring(nameLeftIndex).length() > col1Width){
                        for (int j = 0; j < col1Width; j++) {
                          String delimiter = name.substring(nameLeftIndex + col1Width - j - 1,
                                                           nameLeftIndex + col1Width - j);
                          if (delimiter.equals(" ") || delimiter.equals(";") || delimiter.equals(",")) {
                            nameRightIndex = nameLeftIndex + col1Width - j;
                            break;
                          }
                        }
                        trimmedName = name.substring(nameLeftIndex,nameRightIndex);
                        nameLeftIndex = nameRightIndex;
                        int space = spaces.length()- trimmedName.length();
                        s = "   " + trimmedName + spaces.substring(0,space);
                      }
                      else {
                        trimmedName = name.substring(nameLeftIndex);
                        int space = spaces.length() - trimmedName.length();
                        s = "   " + trimmedName + spaces.substring(0,space);
                        name = "";
                        nameLeftIndex = 0;
                      }
                      // value split code
                      if (value.substring(valueLeftIndex).length() > col2Width){
                        for (int j = 0; j < col2Width; j++){
                          String delimiter = value.substring(valueLeftIndex + col2Width - j - 1, valueLeftIndex + col2Width - j);
                          if (delimiter.equals(" ") || delimiter.equals(";") || delimiter.equals(",")) {
                            valueRightIndex = valueLeftIndex + col2Width - j;
                            break;
                          }
                        }
                        trimmedValue = value.substring(valueLeftIndex,valueRightIndex);
                        valueLeftIndex = valueRightIndex;
                        s= s + "   " + trimmedValue;
                      }
                      else {
                        trimmedValue = value.substring(valueLeftIndex);
                        s = s + "   " + trimmedValue;
                        valueLeftIndex = 0;
                        value = "";
                      }
                      w.write(s,0,s.length());
                      w.writeBorders();
                      s = "\n";
                      w.write(s,0,s.length());
                    }
                // handle special cases
                }
            }
            s = "\n";
            w.writeBorders();
            w.write(s, 0, s.length());
            w.writeBorders();
            w.write(s, 0, s.length());
        } catch (IOException e) { log.warn("error during printing: "+e);
        }

    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(FunctionLabelPane.class.getName());

}
