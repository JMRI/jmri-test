// FnMapPanel.java
package jmri.jmrit.symbolicprog;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import jmri.jmrit.symbolicprog.tabbedframe.PaneProgPane;
import jmri.util.jdom.LocaleSelector;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide a graphical representation of the NMRA Standard mapping between cab
 * functions and physical outputs.
 * <p>
 * Uses data from the "model" element from the decoder definition file to
 * configure the number of rows and columns and set up any custom column
 * names:</p>
 * <dl>
 * <dt>numOuts</dt>
 * <dd>Number of physical outputs.</dd>
 * <dd>&nbsp;</dd>
 * <dt>numFns</dt>
 * <dd>Maximum number of function rows to display.</dd>
 * <dd>&nbsp;</dd>
 * <dt>output</dt>
 * <dd>name="n" label="yyy"</dd>
 * <dd>&nbsp;-&nbsp;Set lower line of heading for column number "n" to
 * "yyy".*</dd>
 * <dd>&nbsp;</dd>
 * <dd>name="n" label="xxx|yyy"</dd>
 * <dd>&nbsp;-&nbsp;Set upper line of heading for column number "n" to "xxx" and
 * lower line to "yyy".*</dd>
 * <dd>&nbsp;</dd>
 * <dd>name="n" label="|"</dd>
 * <dd>&nbsp;-&nbsp;Sets both lines of heading for column number "n" to blank,
 * causing the column to be suppressed from the table.*</dd>
 * <dd>&nbsp;</dd>
 * <dd>&nbsp;*&nbsp;The forms above increase the value of numOuts to n if
 * numOuts &lt; n.</dd>
 * <dd>&nbsp;</dd>
 * <dd>name="text1" label="text2"</dd>
 * <dd>&nbsp;-&nbsp;Set upper line of heading of column numOuts+1 to "xxx" and
 * lower line to "yyy". numOuts is then incremented.</dd>
 * <dd>&nbsp;(This is a legacy form, the other forms are preferred.)</dd>
 * </dl>
 * <dl>
 * <dt>Default column headings:</dt>
 * <dd>First row is the column number.</dd>
 * <dd>Second row is defined in "SymbolicProgBundle.properties".</dd>
 * <dd>Column headings can be overridden by the "output" elements documented
 * above.</dd>
 * <dd>&nbsp;</dd>
 * <dt>Two rows are available for column headings:</dt>
 * <dd>Use the "|" character to designate a row break.</dd>
 * </dl>
 * <dl>
 * <dt>Columns will be suppressed if any of the following are true:</dt>
 * <dd>No variables are found for that column.</dd>
 * <dd>The column output name is of the form name="n" label="|".</dd>
 * <dd>Column number is &gt; maxOut (an internal variable, currently 40).</dd>
 * </dl>
 * <dl>
 * <dt>Searches the decoder file for variable definitions of the form:</dt>
 * <dd>"Fd controls output n" (where d is a function number in the range 0-28
 * and n is an output number in the range 0-maxOut)</dd>
 * <dd>"Fd(f) controls output n"</dd>
 * <dd>"Fd(r) controls output n"</dd>
 * <dd>"FL controls output n" (L for light)</dd>
 * <dd>"FL(f) controls output n"</dd>
 * <dd>"FL(r) controls output n"</dd>
 * <dd>"Fd controls output n(alt)" (allows an alternate definition for the same
 * variable, such as used by Tsunami decoders)</dd>
 * <dd>"Fd(f) controls output n(alt)"</dd>
 * <dd>"Fd(r) controls output n(alt)"</dd>
 * <dd>"FL controls output n(alt)"</dd>
 * <dd>"FL(f) controls output n(alt)"</dd>
 * <dd>"FL(r) controls output n(alt)"</dd>
 * </dl>
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 * @author	Dave Heap Copyright (C) 2014
 * @version	$Revision$
 */
public class FnMapPanel extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = -8500513259142259577L;
    // GridBayLayout column numbers
    int fnName = 0;
    int firstOut = 1;

    // GridBayLayout row numbers
    int outputName = 0;
    int outputNum = 1;
    int outputLabel = 2;
    int firstFn = 3;

    // Some limits and defaults
    int highestFn = 28;
    int numFn = (highestFn + 2) * 3;  // include FL and F0, plus all (f) and (r) variants in the total
    int numOut = 20; // default number of physical outputs
    int maxOut = 40; // maximum number of output columns

    final String[] outName = new String[maxOut];
    final String[] outLabel = new String[maxOut];
    final boolean[] outIsUsed = new boolean[maxOut];

    final String[] fnVarList = new String[]{"", "(f)", "(r)"};

    GridBagLayout gl = null;
    GridBagConstraints cs = null;
    VariableTableModel _varModel;

    public FnMapPanel(VariableTableModel v, List<Integer> varsUsed, Element model) {
        if (log.isDebugEnabled()) {
            log.debug("Function map starts");
        }
        _varModel = v;

        // get number of outLabel defaults
        int outLabelDefs_length = Integer.valueOf(ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("FnMapOutLabelDefaults_length"));

        // set up default names and labels
        for (int iOut = 0; iOut < maxOut; iOut++) {
            outName[iOut] = Integer.toString(iOut + 1);
            outLabel[iOut] = "";
            outIsUsed[iOut] = false;
            // get default labels, if any
            if (iOut < outLabelDefs_length) {
                outLabel[iOut] = ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("FnMapOutLabelDefault_" + (iOut + 1));
            }
        }

        // configure number of channels, arrays
        configOutputs(model);

        // initialize the layout
        gl = new GridBagLayout();
        cs = new GridBagConstraints();
        setLayout(gl);

        {
            JLabel l = new JLabel(ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("FnMapOutWireOr"));
            cs.gridy = outputName;
            cs.gridx = firstOut;
            cs.gridwidth = GridBagConstraints.REMAINDER;
            gl.setConstraints(l, cs);
            add(l);
            cs.gridwidth = 1;
        }

        labelAt(0, fnName, ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("FnMapDesc"), GridBagConstraints.LINE_START);

        // Loop through function names and output names looking for variables
        int row = firstFn;
        for (int iFn = -1; iFn <= highestFn; iFn++) {
            if ((row - firstFn) >= numFn) {
                break; // for compatibility with legacy defintions
            }
            for (String fnVar : fnVarList) {
                String fnNameString = "F" + ((iFn == -1) ? "L" : String.valueOf(iFn)) + fnVar;
                boolean rowIsUsed = false;
                String customName = null;
                for (int iOut = 0; iOut < numOut; iOut++) {
                    // if column is not suppressed by blank headers
                    if (!outName[iOut].equals("") || !outLabel[iOut].equals("")) {
                        // find the variable using the output number or label
                        // include an (alt) variant to enable Tsunami function exchange definitions
                        String nameBase = fnNameString + " controls output ";
                        String[] names;
                        if (outName[iOut].equals(Integer.toString(iOut + 1))) {
                            names = new String[]{nameBase + outName[iOut], nameBase + outName[iOut] + "(alt)"};
                        } else {
                            names = new String[]{nameBase + (iOut + 1), nameBase + (iOut + 1) + "(alt)",
                                nameBase + outName[iOut], nameBase + outName[iOut] + "(alt)"};
                        }
                        for (String name : names) {
//                             log.info("Search name='"+name+"'");
                            int iVar = _varModel.findVarIndex(name);
                            if (iVar >= 0) {
                                if (log.isDebugEnabled()) {
                                    log.debug("Process var: " + name + " as index " + iVar);
                                }
                                varsUsed.add(Integer.valueOf(iVar));
                                JComponent j = (JComponent) (_varModel.getRep(iVar, "checkbox"));
                                VariableValue var = _varModel.getVariable(iVar);
                                j.setToolTipText(PaneProgPane.addCvDescription((fnNameString + " "
                                        + ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("FnMapControlsOutput") + " "
                                        + outName[iOut] + " " + outLabel[iOut]), var.getCvDescription(), var.getMask()));
                                String temp = _varModel.getLabel(iVar);
//                                 log.info("label='" + temp + "'");
//                                 log.info("nameBase='" + nameBase + "'");
//                                 log.info("match='" + temp.startsWith(fnNameString + " controls ") + "'");
//                                 if ( !temp.startsWith(fnNameString + " controls ") ) {
//                                     customName = temp;
//                                 }
//                                 log.info("customName='" + customName + "'");
                                int column = firstOut + iOut;
                                saveAt(row, column, j);
                                rowIsUsed = true;
                                outIsUsed[iOut] = true;
                            } else {
                                if (log.isDebugEnabled()) {
                                    log.debug("Did not find var: " + name);
                                }
                            }
                        }
                    }
                }
                if (rowIsUsed) {
                    if (customName != null) {
                        fnNameString = customName;
                    } else if (fnNameString.equals("FL(f)")) {
                        fnNameString = ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("FnMapLightFwd");
                    } else if (fnNameString.equals("FL(r)")) {
                        fnNameString = ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("FnMapLightRev");
                    } else if (fnNameString.endsWith(fnVarList[1])) {
                        fnNameString = ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("FnMapFunctionPrefix") + " "
                                + fnNameString.substring(1, fnNameString.length() - fnVarList[1].length())
                                + ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("FnMapSuffixFwd");
                    } else if (fnNameString.endsWith(fnVarList[2])) {
                        fnNameString = ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("FnMapFunctionPrefix") + " "
                                + fnNameString.substring(1, fnNameString.length() - fnVarList[2].length())
                                + ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("FnMapSuffixRev");
                    } else {
                        fnNameString = ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("FnMapFunctionPrefix") + " "
                                + fnNameString.substring(1);
                    }
                    labelAt(row, fnName, fnNameString, GridBagConstraints.LINE_START);
                    row++;
                }

            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Function map complete");
        }

        // label used outputs only
        for (int iOut = 0; iOut < numOut; iOut++) {
            if (outIsUsed[iOut]) {
                labelAt(outputNum, firstOut + iOut, outName[iOut]);
                labelAt(outputLabel, firstOut + iOut, outLabel[iOut]);
            }
        }

        // padding for the case of few outputs
        cs.gridwidth = GridBagConstraints.REMAINDER;
        labelAt(outputNum, firstOut + numOut, "");
    }

    void saveAt(int row, int column, JComponent j) {
        this.saveAt(row, column, j, GridBagConstraints.CENTER);
    }

    void saveAt(int row, int column, JComponent j, int anchor) {
        if (row < 0 || column < 0) {
            return;
        }
        cs = new GridBagConstraints();
        cs.gridy = row;
        cs.gridx = column;
        cs.anchor = anchor;
        gl.setConstraints(j, cs);
        add(j);
    }

    void labelAt(int row, int column, String name) {
        this.labelAt(row, column, name, GridBagConstraints.CENTER);
    }

    void labelAt(int row, int column, String name, int anchor) {
        if (row < 0 || column < 0) {
            return;
        }
        JLabel t = new JLabel(" " + name + " ");
        saveAt(row, column, t, anchor);
    }

    /**
     * Use the "model" element from the decoder definition file to configure the
     * number of outputs and set up any that are named instead of numbered.
     */
    protected void configOutputs(Element model) {
        if (model == null) {
            log.debug("configOutputs was given a null model");
            return;
        }
        // get numOuts, numFns or leave the defaults
        Attribute a = model.getAttribute("numOuts");
        try {
            if (a != null) {
                numOut = Integer.valueOf(a.getValue()).intValue();
            }
        } catch (Exception e) {
            log.error("error handling decoder's numOuts value");
        }
        a = model.getAttribute("numFns");
        try {
            if (a != null) {
                numFn = Integer.valueOf(a.getValue()).intValue();
            }
        } catch (Exception e) {
            log.error("error handling decoder's numFns value");
        }
        if (log.isDebugEnabled()) {
            log.debug("numFns, numOuts " + numFn + "," + numOut);
        }
        // take all "output" children
        List<Element> elemList = model.getChildren("output");
        if (log.isDebugEnabled()) {
            log.debug("output scan starting with " + elemList.size() + " elements");
        }
        for (int i = 0; i < elemList.size(); i++) {
            Element e = elemList.get(i);
            String name = e.getAttribute("name").getValue();
            // if this a number, or a character name?
            try {
                int outputNum = Integer.valueOf(name).intValue();
                // yes, since it was converted.  All we do with
                // these are store the label index (if it exists)
                String at = LocaleSelector.getAttribute(e, "label");
                if (at != null) {
                    loadSplitLabel(outputNum - 1, at);
                    numOut = Math.max(numOut, outputNum);
                }
            } catch (java.lang.NumberFormatException ex) {
                // not a number, must be a name
                if (numOut < maxOut) {
                    outName[numOut] = name;
                    String at;
                    if ((at = LocaleSelector.getAttribute(e, "label")) != null) {
                        outLabel[numOut] = at;
                    } else {
                        outLabel[numOut] = "";
                    }
                    numOut++;
                }
            }
        }
    }

    // split and load two-line labels
    void loadSplitLabel(int iOut, String theLabel) {
        if (iOut < maxOut) {
            String itemList[] = theLabel.split("\\|");
//             log.info("theLabel=\""+theLabel+"\" itemList.length=\""+itemList.length+"\"");
            if (theLabel.equals("|")) {
                outName[iOut] = "";
                outLabel[iOut] = "";
            } else if (itemList.length == 1) {
                outLabel[iOut] = itemList[0];
            } else if (itemList.length > 1) {
                outName[iOut] = itemList[0];
                outLabel[iOut] = itemList[1];
            }
        }
    }

    /**
     * clean up at end
     */
    public void dispose() {
        removeAll();
    }

    // initialize logging
    static Logger log = LoggerFactory.getLogger(FnMapPanel.class.getName());
}
