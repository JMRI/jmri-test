// FnMapPanel.java

package jmri.jmrit.symbolicprog;

import javax.swing.*;
import java.awt.*;

import java.util.List;
import org.jdom.Element;
import org.jdom.Attribute;

/**
 * Provide a graphical representation of the NMRA S&RP mapping between cab functions
 * and physical outputs.
 *<P>
 * This is mapped via various definition variables.  A -1 means don't provide it. The
 * panel then creates a GridBayLayout: <dl>
 *  <DT>Column cvNum  	<DD> CV number (Typically 0)
 *  <DT>Column fnName  	<DD> Function name (Typically 1)
 *
 *  <DT>Row outputLabel	<DD> "output label" (Typically 0)
 *  <DT>Row outputNum	<DD> "output number" (Typically 1)
 *  <DT>Row outputName	<DD> "output name (or color)" (Typically 2)
 *
 *  <DT>Row firstFn     <DD> Row for first function, usually FL0.  Will go up from this,
 *							 with higher numbered functions in higher numbered columns.
 *  <DT>Column firstOut  <DD> Column for leftmost numbered output
 *</dl>
 *<P>
 * Although support for the "CV label column" is still here, its turned off now.
 *
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Revision: 1.12 $
 */
public class FnMapPanel extends JPanel {
    // columns
    int cvNum = -1;
    int fnName = 0;
    int firstOut = 1;
    
    // rows
    int outputName = 0;
    int outputNum = 1;
    int outputLabel = 2;
    int firstFn = 3;
    
    // these will eventually be passed in from the ctor
    int numFn = 14;  // include FL(f) and FL(r) in the total
    int numOut = 20;
    int maxFn = 30;  // include FL(f) and FL(r) in the total
    int maxOut = 15;
    
    GridBagLayout gl = null;
    GridBagConstraints cs = null;
    VariableTableModel _varModel;
    
    public FnMapPanel(VariableTableModel v, List<Integer> varsUsed, Element model) {
        if (log.isDebugEnabled()) log.debug("Function map starts");
        _varModel = v;
        
        // configure number of channels, arrays
        configOutputs(model);
        
        // initialize the layout
        gl = new GridBagLayout();
        cs = new GridBagConstraints();
        setLayout(gl);
        
        {
            JLabel l = new JLabel("Output wire or operation");
            cs.gridy = outputName;
            cs.gridx = 3;
            cs.gridwidth = GridBagConstraints.REMAINDER;
            gl.setConstraints(l, cs);
            add(l);
            cs.gridwidth = 1;
        }
        // dummy structure until we figure out how to convey CV numbers programmatically
        if (cvNum>=0) {
            labelAt( 0, 0, "CV");
            labelAt( firstFn   , cvNum, "33");
            labelAt( firstFn+ 1, cvNum, "34");
            labelAt( firstFn+ 2, cvNum, "35");
            labelAt( firstFn+ 3, cvNum, "36");
            labelAt( firstFn+ 4, cvNum, "37");
            labelAt( firstFn+ 5, cvNum, "38");
            labelAt( firstFn+ 6, cvNum, "39");
            labelAt( firstFn+ 7, cvNum, "40");
            labelAt( firstFn+ 8, cvNum, "41");
            labelAt( firstFn+ 9, cvNum, "42");
            labelAt( firstFn+10, cvNum, "43");
            labelAt( firstFn+11, cvNum, "44");
            labelAt( firstFn+12, cvNum, "45");
            labelAt( firstFn+13, cvNum, "46");
        }
        
        labelAt(0,fnName, "Description");
        
        labelAt( firstFn   , fnName, "Forward Headlight F0(F)");
        labelAt( firstFn+ 1, fnName, "Reverse Headlight F0(R)");
        if (numFn>2) labelAt( firstFn+ 2, fnName, "Function 1");
        if (numFn>3) labelAt( firstFn+ 3, fnName, "Function 2");
        if (numFn>4) labelAt( firstFn+ 4, fnName, "Function 3");
        if (numFn>5) labelAt( firstFn+ 5, fnName, "Function 4");
        if (numFn>6) labelAt( firstFn+ 6, fnName, "Function 5");
        if (numFn>7) labelAt( firstFn+ 7, fnName, "Function 6");
        if (numFn>8) labelAt( firstFn+ 8, fnName, "Function 7");
        if (numFn>9) labelAt( firstFn+ 9, fnName, "Function 8");
        if (numFn>10) labelAt( firstFn+10, fnName, "Function 9");
        if (numFn>11) labelAt( firstFn+11, fnName, "Function 10");
        if (numFn>12) labelAt( firstFn+12, fnName, "Function 11");
        if (numFn>13) labelAt( firstFn+13, fnName, "Function 12");
        if (numFn>14) labelAt( firstFn+13, fnName, "Function 13");
        if (numFn>15) labelAt( firstFn+13, fnName, "Function 14");
        if (numFn>16) labelAt( firstFn+13, fnName, "Function 15");
        if (numFn>17) labelAt( firstFn+13, fnName, "Function 16");
        if (numFn>18) labelAt( firstFn+13, fnName, "Function 17");
        if (numFn>19) labelAt( firstFn+13, fnName, "Function 18");
        if (numFn>20) labelAt( firstFn+13, fnName, "Function 19");
        if (numFn>21) labelAt( firstFn+13, fnName, "Function 20");
        if (numFn>22) labelAt( firstFn+13, fnName, "Function 21");
        if (numFn>23) labelAt( firstFn+13, fnName, "Function 22");
        if (numFn>24) labelAt( firstFn+13, fnName, "Function 23");
        if (numFn>25) labelAt( firstFn+13, fnName, "Function 24");
        if (numFn>26) labelAt( firstFn+13, fnName, "Function 25");
        if (numFn>27) labelAt( firstFn+13, fnName, "Function 26");
        if (numFn>28) labelAt( firstFn+13, fnName, "Function 27");
        if (numFn>29) labelAt( firstFn+13, fnName, "Function 28");
        
        // label outputs
        for (int iOut=0; iOut<numOut; iOut++) {
            labelAt( outputNum,   firstOut+iOut, outName[iOut]);
            labelAt( outputLabel, firstOut+iOut, outLabel[iOut]);
        }
        
        for (int iFn = 0; iFn < numFn; iFn++) {
            for (int iOut = 0; iOut < numOut; iOut++) {
                // find the variable using the output label
                String name = fnList[iFn]+" controls output "+outName[iOut];
                int iVar = _varModel.findVarIndex(name);
                if (iVar>=0) {
                    if (log.isDebugEnabled()) log.debug("Process var: "+name+" as index "+iVar);
                    varsUsed.add(new Integer(iVar));
                    JComponent j = (JComponent)(_varModel.getRep(iVar, "checkbox"));
                    int row = firstFn+iFn;
                    int column = firstOut+iOut;
                    saveAt(row, column, j);
                } else {
                    if (log.isDebugEnabled()) log.debug("Did not find var: "+name);
                }
            }
        }
        if (log.isDebugEnabled()) log.debug("Function map complete");
    }
    
    final String[] fnList = new String[] { "FL(f)", "FL(r)", "F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8", "F9", "F10", 
                                           "F11", "F12", "F13", "F14", "F15", "F16", "F17", "F18", "F19",
                                           "F21", "F22", "F23", "F24", "F25", "F26", "F27", "F28"
                                            };
    
    final String[] outLabel = new String[] {"White", "Yellow", "Green", "Vlt/Brwn", "", "", "", "", "", "",
                                            "", "", "", "", "","", "", "", "", "",
                                            "", "", "", "", "","", "", "", "", "",
                                            "", "", "", "", "","", "", "", "", ""
                                            };
    
    final String[] outName = new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
                                           "11", "12", "13", "14", "15", "16", "17", "18", "19", "20",
                                           "21", "22", "23", "24", "25", "26", "27", "28", "29", "30",
                                           "31", "32", "33", "34", "35", "36", "37", "38", "39", "40"
                                            };
    
    void saveAt(int row, int column, JComponent j) {
        if (row<0 || column<0) return;
        cs.gridy = row;
        cs.gridx = column;
        gl.setConstraints(j, cs);
        add(j);
    }
    
    void labelAt(int row, int column, String name) {
        if (row<0 || column<0) return;
        JLabel t = new JLabel(" "+name+" ");
        saveAt(row, column, t);
    }
    
    /**
     * Use the "model" element from the decoder definition file
     * to configure the number of outputs and set up any that
     * are named instead of numbered.
     */
    protected void configOutputs(Element model) {
        if (model==null) {
            log.debug("configOutputs was given a null model");
            return;
        }
        // get numOuts, numFns or leave the defaults
        Attribute a = model.getAttribute("numOuts");
        try { if (a!=null) numOut = Integer.valueOf(a.getValue()).intValue();}
        catch (Exception e) {log.error("error handling decoder's numOuts value");}
        a = model.getAttribute("numFns");
        try { if (a!=null) numFn = Integer.valueOf(a.getValue()).intValue();}
        catch (Exception e) {log.error("error handling decoder's numFns value");}
        if (log.isDebugEnabled()) log.debug("numFns, numOuts "+numFn+","+numOut);
        // take all "output" children
        List elemList = model.getChildren("output");
        if (log.isDebugEnabled()) log.debug("output scan starting with "+elemList.size()+" elements");
        for (int i=0; i<elemList.size(); i++) {
            Element e = (Element)(elemList.get(i));
            String name = e.getAttribute("name").getValue();
            // if this a number, or a character name?
            try {
                int outputNum = Integer.valueOf(name).intValue();
                // yes, since it was converted.  All we do with
                // these are store the label index (if it exists)
                Attribute at;
                if ((at=e.getAttribute("label"))!=null && outputNum<=numOut)
                    outLabel[outputNum-1]=at.getValue();
            } catch (java.lang.NumberFormatException ex) {
                // not a number, must be a name
                if (numOut<maxOut) {
                    outName[numOut] = name;
                    Attribute at;
                    if ((at=e.getAttribute("label"))!=null)
                        outLabel[numOut] = at.getValue();
                    else
                        outLabel[numOut] ="";
                    numOut++;
                }
            }
        }
    }
    
    /** clean up at end */
    public void dispose() {
        removeAll();
    }
    
    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(FnMapPanel.class.getName());
}
