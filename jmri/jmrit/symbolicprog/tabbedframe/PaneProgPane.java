// PaneProgPane.java

package jmri.jmrit.symbolicprog.tabbedframe;

import jmri.util.davidflanagan.HardcopyWriter;
import jmri.jmrit.symbolicprog.CvTableModel;
import jmri.jmrit.symbolicprog.CvValue;
import jmri.jmrit.symbolicprog.DccAddressPanel;
import jmri.jmrit.symbolicprog.FnMapPanel;
import jmri.jmrit.symbolicprog.ValueEditor;
import jmri.jmrit.symbolicprog.ValueRenderer;
import jmri.jmrit.symbolicprog.VariableTableModel;
import jmri.jmrit.symbolicprog.VariableValue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.*;

import org.jdom.Attribute;
import org.jdom.Element;
import com.sun.java.util.collections.ArrayList;
import com.sun.java.util.collections.List;   // resolve ambiguity with package-level import

/**
 * Provides the individual panes for the TabbedPaneProgrammer.
 * Note that this is not only the panes carrying variables, but also the
 * special purpose panes for the CV table, etc.
 *<P>
 * This class implements PropertyChangeListener so that it can be notified
 * when a variable changes its busy status at the end of a programming read/write operation
 *
 * @author			Bob Jacobsen   Copyright (C) 2001; D Miller Copyright 2003
 * @version			$Revision: 1.24 $
 */
public class PaneProgPane extends javax.swing.JPanel
    implements java.beans.PropertyChangeListener  {

    CvTableModel _cvModel;
    VariableTableModel _varModel;

    ActionListener l1;
    ActionListener l2;
    ActionListener l3;
    ActionListener l4;

    String mName = "";
    /**
     * Create a null object.  Normally only used for tests and to pre-load classes.
     */
    public PaneProgPane() {}

    /**
     * Construct the Pane from the XML definition element.
     *
     * @param name  Name to appear on tab of pane
     * @param pane  The JDOM Element for the pane definition
     * @param cvModel Already existing TableModel containing the CV definitions
     * @param varModel Already existing TableModel containing the variable definitions
     * @param modelElem "model" element from the Decoder Index, used to check what decoder options are present.
     */
    public PaneProgPane(String name, Element pane, CvTableModel cvModel, VariableTableModel varModel, Element modelElem) {

        mName = name;
        _cvModel = cvModel;
        _varModel = varModel;

        // This is a JPanel containing a JScrollPane, containing a
        // laid-out JPanel
        setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));

        // find out whether to display "label" (false) or "item" (true)
        boolean showItem = false;
        Attribute nameFmt = pane.getAttribute("nameFmt");
        if (nameFmt!= null && nameFmt.getValue().equals("item")) {
            log.debug("Pane "+name+" will show items, not labels, from decoder file");
            showItem = true;
        }
        // put the columns left to right in a panel
        JPanel p = new JPanel();
        panelList.add(p);
        p.setLayout(new BoxLayout(p,BoxLayout.X_AXIS));

        // handle the xml definition
        // for all "column" elements ...
        List colList = pane.getChildren("column");
        for (int i=0; i<colList.size(); i++) {
            // load each column
            p.add(newColumn( ((Element)(colList.get(i))), showItem, modelElem));
        }
        // for all "row" elements ...
        List rowList = pane.getChildren("row");
        for (int i=0; i<rowList.size(); i++) {
            // load each row
            p.add(newRow( ((Element)(rowList.get(i))), showItem, modelElem));
        }

        // add glue to the right to allow resize - but this isn't working as expected? Alignment?
        add(Box.createHorizontalGlue());

        add(new JScrollPane(p));

        // add buttons in a new panel
        JPanel bottom = new JPanel();
        panelList.add(p);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));

        readButton.setToolTipText("Read highlighted values on this sheet from decoder. Warning: may take a long time!");
        if (cvModel.getProgrammer()!= null
            && !cvModel.getProgrammer().getCanRead()) {
            // can't read, disable the button
            readButton.setEnabled(false);
            readButton.setToolTipText("Button disabled because configured command station can't read CVs");
        }
        readButton.addActionListener( l1 = new ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (readButton.isSelected()) readPane();
                }
            });

        reReadButton.setToolTipText("Read all values on this sheet from decoder. Warning: may take a long time!");
        if (cvModel.getProgrammer()!= null
            && !cvModel.getProgrammer().getCanRead()) {
            // can't read, disable the button
            reReadButton.setEnabled(false);
            reReadButton.setToolTipText("Button disabled because configured command station can't read CVs");
        }
        reReadButton.addActionListener( l2 = new ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (reReadButton.isSelected()) reReadPane();
                }
            });

        writeButton.setToolTipText("Write highlighted values on this sheet to decoder");
        writeButton.addActionListener( l3 = new ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (writeButton.isSelected()) writePane();
                }
            });

        reWriteButton.setToolTipText("Write all values on this sheet to decoder");
        reWriteButton.addActionListener( l4 = new ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (reWriteButton.isSelected()) reWritePane();
                }
            });

        bottom.add(readButton);
        bottom.add(writeButton);
        bottom.add(reReadButton);
        bottom.add(reWriteButton);

        add(bottom);
    }

    /**
     * This remembers the variables on this pane for the Read/Write sheet
     * operation.  They are stored as a list of Integer objects, each of which
     * is the index of the Variable in the VariableTable.
     */
    List varList = new ArrayList();
    /**
     * This remembers the CVs on this pane for the Read/Write sheet
     * operation.  They are stored as a list of Integer objects, each of which
     * is the index of the CV in the CVTable. Note that variables are handled
     * separately, and the CVs that are represented by variables are not
     * entered here.  So far (sic), the only use of this is for the cvtable rep.
     */
    List cvList = new ArrayList();

    JToggleButton readButton     = new JToggleButton("Read changes on sheet");
    JToggleButton reReadButton   = new JToggleButton("Read full sheet");
    JToggleButton writeButton    = new JToggleButton("Write changes on sheet");
    JToggleButton reWriteButton  = new JToggleButton("Write full sheet");

    /**
     * invoked by "Read Pane" button, this sets in motion a
     * continuing sequence of "read" operations on the
     * variables & CVs in the Pane.  Only variables in states
     * UNKNOWN, EDITED, FROMFILE are read; states STORED and READ don't
     * need to be.
     *
     * @return true is a read has been started, false if the pane is complete.
     */
    public boolean readPane() {
        if (log.isDebugEnabled()) log.debug("readPane starts with "
                                            +varList.size()+" vars, "
                                            +cvList.size()+" cvs");
        readButton.setSelected(true);
        return nextRead();
    }

    /**
     * invoked by "Reread Pane" button, this sets in motion a
     * continuing sequence of "read" operations on the
     * variables & CVs in the Pane.  The read mechanism only reads
     * variables in certain states (and needs to do that to handle error
     * processing right now), so this is implemented by first
     * setting all variables and CVs on this pane to FROMFILE
     *
     * @return true is a read has been started, false if the pane is complete.
     */
    public boolean reReadPane() {
        if (log.isDebugEnabled()) log.debug("reReadPane starts with "
                                            +varList.size()+" vars, "
                                            +cvList.size()+" cvs");
        reReadButton.setSelected(true);

        // flag state of variables and CVs
        setAllToFromFile();

        // start operation
        return nextRead();
    }

    /**
     * Set states so that all variables and CVs on this pane will
     * read when a readPane or reReadPane operation comes along.
     */
    void setAllToFromFile() {
        for (int i=0; i<varList.size(); i++) {
            int varNum = ((Integer)varList.get(i)).intValue();
            _varModel.getVariable( varNum ).setCvState( VariableValue.FROMFILE );
        }
        for (int i=0; i<cvList.size(); i++) {
            int cvNum = ((Integer)cvList.get(i)).intValue();
            _cvModel.getCvByRow(cvNum).setState(CvValue.FROMFILE);
        }
    }

    /**
     * If there are any more read operations to be done on this pane,
     * do the next one.
     * <P>
     * Each invocation of this method reads on CV; completion
     * of that request will cause it to happen again, reading the next CV, until
     * there's nothing left to read.
     * <P>
     * @return true is a read has been started, false if the pane is complete.
     */
    boolean nextRead() {
        for (int i=0; i<varList.size(); i++) {
            int varNum = ((Integer)varList.get(i)).intValue();
            int vState = _varModel.getState( varNum );
            if (log.isDebugEnabled()) log.debug("nextRead var index "+varNum+" state "+vState);
            if (vState == VariableValue.UNKNOWN ||
                vState == VariableValue.EDITED ||
                vState == VariableValue.FROMFILE )  {
                if (log.isDebugEnabled()) log.debug("start read of variable "+_varModel.getLabel(varNum));
                setBusy(true);
                if (_programmingVar != null) log.error("listener already set at read start");
                _programmingVar = _varModel.getVariable(varNum);
                _read = true;
                // get notified when that state changes so can repeat
                _programmingVar.addPropertyChangeListener(this);
                // and make the read request
                _programmingVar.read();
                if (log.isDebugEnabled()) log.debug("return from starting var read");
                // the request may have instantaneously been satisfied...
                return true;  // only make one request at a time!
            }
        }
        // found no variables needing read, try CVs
        for (int i=0; i<cvList.size(); i++) {
            int cvNum = ((Integer)cvList.get(i)).intValue();
            int cState = _cvModel.getCvByRow(cvNum).getState();
            if (log.isDebugEnabled()) log.debug("nextRead cv index "+cvNum+" state "+cState);
            if (cState == CvValue.UNKNOWN ||
                cState == CvValue.EDITED ||
                cState == CvValue.FROMFILE )  {
                if (log.isDebugEnabled()) log.debug("start read of cv "+cvNum);
                setBusy(true);
                if (_programmingCV != null) log.error("listener already set at read start");
                _programmingCV = _cvModel.getCvByRow(cvNum);
                _read = true;
                // get notified when that state changes so can repeat
                _programmingCV.addPropertyChangeListener(this);
                // and make the read request
                _programmingCV.read(_cvModel.getStatusLabel());
                if (log.isDebugEnabled()) log.debug("return from starting CV read");
                // the request may have instantateously been satisfied...
                return true;  // only make one request at a time!
            }
        }
        // nothing to program, end politely
        if (log.isDebugEnabled()) log.debug("nextRead found nothing to do");
        readButton.setSelected(false);
        reReadButton.setSelected(false);  // reset both, as that's final state we want
        setBusy(false);
        return false;
    }

    /**
     * invoked by "Write Pane" button, this sets in motion a
     * continuing sequence of "write" operations on the
     * variables in the Pane.  Only variables in states
     * UNKNOWN, EDITED, FROMFILE are read; states STORED and READ don't
     * need to be.  Each invocation of this method writes one CV; completion
     * of that request will cause it to happen again, writing the next CV, until
     * there's nothing left to write.
     * <P>
     * Returns true is a write has been started, false if the pane is complete.
     */
    public boolean writePane() {
        if (log.isDebugEnabled()) log.debug("writePane starts");
        writeButton.setSelected(true);
        return nextWrite();
    }

    public boolean reWritePane() {
        if (log.isDebugEnabled()) log.debug("reWritePane starts");
        reWriteButton.setSelected(true);
        setAllToFromFile();
        return nextWrite();
    }

    boolean nextWrite() {
        for (int i=0; i<varList.size(); i++) {
            int varNum = ((Integer)varList.get(i)).intValue();
            int vState = _varModel.getState( varNum );
            if (log.isDebugEnabled()) log.debug("nextWrite var index "+varNum+" state "+vState);
            if (vState == VariableValue.UNKNOWN ||
                vState == VariableValue.EDITED ||
                vState == VariableValue.FROMFILE )  {
                if (log.isDebugEnabled()) log.debug("start write of variable "+_varModel.getLabel(varNum));
                setBusy(true);
                if (_programmingVar != null) log.error("listener already set at write start");
                _programmingVar = _varModel.getVariable(varNum);
                _read = false;
                // get notified when that state changes so can repeat
                _programmingVar.addPropertyChangeListener(this);
                // and make the read request
                _programmingVar.write();
                if (log.isDebugEnabled()) log.debug("return from starting var write");
                return true;  // only make one request at a time!
            }
        }
        // check for CVs to handle (e.g. for CV table)
        for (int i=0; i<cvList.size(); i++) {
            int cvNum = ((Integer)cvList.get(i)).intValue();
            int cState = _cvModel.getCvByRow( cvNum ).getState();
            if (log.isDebugEnabled()) log.debug("nextWrite cv index "+cvNum+" state "+cState);
            if (cState == CvValue.UNKNOWN ||
                cState == CvValue.EDITED ||
                cState == CvValue.FROMFILE )  {
                if (log.isDebugEnabled()) log.debug("start write of cv index "+cvNum);
                setBusy(true);
                if (_programmingCV != null) log.error("listener already set at write start");
                _programmingCV = _cvModel.getCvByRow(cvNum);
                _read = false;
                // get notified when that state changes so can repeat
                _programmingCV.addPropertyChangeListener(this);
                // and make the read request
                _programmingCV.write(_cvModel.getStatusLabel());
                if (log.isDebugEnabled()) log.debug("return from starting cv write");
                return true;  // only make one request at a time!
            }
        }
        // nothing to program, end politely
        if (log.isDebugEnabled()) log.debug("nextWrite found nothing to do");
        writeButton.setSelected(false);
        reWriteButton.setSelected(false);
        setBusy(false);
        return false;
    }

    // reference to variable being programmed (or null if none)
    VariableValue _programmingVar = null;
    CvValue _programmingCV  = null;
    boolean _read = true;

    // busy during read, write operations
    private boolean _busy = false;
    public boolean isBusy() { return _busy; }

    protected void setBusy(boolean busy) {
        boolean oldBusy = _busy;
        _busy = busy;
        if (oldBusy != busy) prop.firePropertyChange("Busy", new Boolean(oldBusy), new Boolean(busy));
    }

    /**
     * Get notification of a variable property change, specifically "busy" going to
     * false at the end of a programming operation. If we're in a programming
     * operation, we then continue it by reinvoking the nextRead/writePane operation.
     */
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        // check for the right event & condition
        if (_programmingVar == null && _programmingCV == null) {
            log.warn("unexpected propertChange: "+e);
            return;
        } else if (log.isDebugEnabled()) log.debug("property changed: "+e.getPropertyName()
                                                   +" new value: "+e.getNewValue());

        // find the right way to handle this
        if (e.getSource() == _programmingVar &&
            e.getPropertyName().equals("Busy") &&
            ((Boolean)e.getNewValue()).equals(Boolean.FALSE) ) {
            replyWhileProgrammingVar();
            return;
        } else if (e.getSource() == _programmingCV &&
                   e.getPropertyName().equals("Busy") &&
                   ((Boolean)e.getNewValue()).equals(Boolean.FALSE) ) {
            replyWhileProgrammingCV();
            return;
        } else {
            if (log.isDebugEnabled() && e.getPropertyName().equals("Busy"))
                log.debug("ignoring change of Busy "+((Boolean)e.getNewValue())
                          +" "+( ((Boolean)e.getNewValue()).equals(Boolean.FALSE)));
            return;
        }
    }
    public void replyWhileProgrammingVar() {
        if (log.isDebugEnabled()) log.debug("correct event for programming variable, restart operation");
        // remove existing listener
        _programmingVar.removePropertyChangeListener(this);
        _programmingVar = null;
        // restart the operation
        restartProgramming();
    }

    public void replyWhileProgrammingCV() {
        if (log.isDebugEnabled()) log.debug("correct event for programming CV, restart operation");
        // remove existing listener
        _programmingCV.removePropertyChangeListener(this);
        _programmingCV = null;
        // restart the operation
        restartProgramming();
    }

    void restartProgramming() {
        if (_read && readButton.isSelected()) nextRead();
        else if (_read && reReadButton.isSelected()) nextRead();
        else if (writeButton.isSelected()) writePane();
        else if (reWriteButton.isSelected()) writePane();
        else if (log.isDebugEnabled()) log.debug("No operation to restart");
    }

    /**
     * Create a single column from the JDOM column Element
     */
    public JPanel newColumn(Element element, boolean showStdName, Element modelElem) {

        // create a panel to add as a new column or row
        JPanel c = new JPanel();
        panelList.add(c);
        GridBagLayout g = new GridBagLayout();
        GridBagConstraints cs = new GridBagConstraints();
        c.setLayout(g);

        // handle the xml definition
        // for all elements in the column or row
        List elemList = element.getChildren();
        if (log.isDebugEnabled()) log.debug("newColumn starting with "+elemList.size()+" elements");
        for (int i=0; i<elemList.size(); i++) {

            // update the grid position
            cs.gridy++;
            cs.gridx = 0;

            Element e = (Element)(elemList.get(i));
            String name = e.getName();
            if (log.isDebugEnabled()) log.debug("newColumn processing "+name+" element");
            // decode the type
            if (name.equals("display")) { // its a variable
				// load the variable
                newVariable( e, c, g, cs, showStdName);
            }
            else if (name.equals("separator")) { // its a separator
                JSeparator j = new JSeparator(javax.swing.SwingConstants.HORIZONTAL);
                cs.fill = GridBagConstraints.BOTH;
                cs.gridwidth = GridBagConstraints.REMAINDER;
                g.setConstraints(j, cs);
                c.add(j);
                cs.gridwidth = 1;
            }
            else if (name.equals("label")) { // its  a label
                JLabel l = new JLabel(e.getAttribute("label").getValue());
                l.setAlignmentX(1.0f);
                cs.fill = GridBagConstraints.BOTH;
                cs.gridwidth = GridBagConstraints.REMAINDER;
                if (log.isDebugEnabled()) {
                    log.debug("Add label: "+l.getText()+" cs: "
                              +cs.gridwidth+" "+cs.fill+" "
                              +cs.gridx+" "+cs.gridy);
                }
                g.setConstraints(l, cs);
                c.add(l);
                cs.fill = GridBagConstraints.NONE;
                cs.gridwidth = 1;
            }
            else if (name.equals("cvtable")) {
                log.debug("starting to build CvTable pane");
				// this is copied from SymbolicProgFrame
                JTable			cvTable		= new JTable(_cvModel);
                JScrollPane 	cvScroll	= new JScrollPane(cvTable);
                cvTable.setDefaultRenderer(JTextField.class, new ValueRenderer());
                cvTable.setDefaultRenderer(JButton.class, new ValueRenderer());
                cvTable.setDefaultEditor(JTextField.class, new ValueEditor());
                cvTable.setDefaultEditor(JButton.class, new ValueEditor());
                cvTable.setRowHeight(new JButton("X").getPreferredSize().height);
                cvScroll.setColumnHeaderView(cvTable.getTableHeader());
				// have to shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
				// instead of forcing the columns to fill the frame (and only fill)
                cvTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                cs.gridwidth = GridBagConstraints.REMAINDER;
                g.setConstraints(cvScroll, cs);
                c.add(cvScroll);
                cs.gridwidth = 1;

                // remember which CVs to read/write
                for (int j=0; j<_cvModel.getRowCount(); j++) {
                    cvList.add(new Integer(j));
                }

                log.debug("end of building CvTable pane");

            }
            else if (name.equals("fnmapping")) {
                FnMapPanel l = new FnMapPanel(_varModel, varList, modelElem);
                fnMapList.add(l); // remember for deletion
                cs.gridwidth = GridBagConstraints.REMAINDER;
                g.setConstraints(l, cs);
                c.add(l);
                cs.gridwidth = 1;
            }
            else if (name.equals("dccaddress")) {
                JPanel l = addDccAddressPanel(e);
                cs.gridwidth = GridBagConstraints.REMAINDER;
                g.setConstraints(l, cs);
                c.add(l);
                cs.gridwidth = 1;
            }
            else if (name.equals("row")) {
				// nested "row" elements ...
                cs.gridwidth = GridBagConstraints.REMAINDER;
                JPanel l = newRow(e, showStdName, modelElem);
                panelList.add(l);
                g.setConstraints(l, cs);
                c.add(l);
                cs.gridwidth = 1;
            }
            else { // its a mistake
                log.error("No code to handle element of type "+e.getName()+" in newColumn");
            }
        }
        // add glue to the bottom to allow resize
        c.add(Box.createVerticalGlue());

        return c;
    }

    /**
     * Create a single row from the JDOM column Element
     */
    public JPanel newRow(Element element, boolean showStdName, Element modelElem) {

        // create a panel to add as a new column or row
        JPanel c = new JPanel();
        panelList.add(c);
        GridBagLayout g = new GridBagLayout();
        GridBagConstraints cs = new GridBagConstraints();
        c.setLayout(g);

        // handle the xml definition
        // for all elements in the column or row
        List elemList = element.getChildren();
        if (log.isDebugEnabled()) log.debug("newRow starting with "+elemList.size()+" elements");
        for (int i=0; i<elemList.size(); i++) {

            // update the grid position
            cs.gridy = 0;
            cs.gridx++;

            Element e = (Element)(elemList.get(i));
            String name = e.getName();
            if (log.isDebugEnabled()) log.debug("newRow processing "+name+" element");
            // decode the type
            if (name.equals("display")) { // its a variable
		// load the variable
                newVariable( e, c, g, cs, showStdName);
            }
            else if (name.equals("separator")) { // its a separator
                JSeparator j = new JSeparator(javax.swing.SwingConstants.VERTICAL);
                cs.fill = GridBagConstraints.BOTH;
                cs.gridheight = GridBagConstraints.REMAINDER;
                g.setConstraints(j, cs);
                c.add(j);
                cs.fill = GridBagConstraints.NONE;
                cs.gridheight = 1;
            }
            else if (name.equals("label")) { // its  a label
                JLabel l = new JLabel(e.getAttribute("label").getValue());
                l.setAlignmentX(1.0f);
                cs.gridheight = GridBagConstraints.REMAINDER;
                g.setConstraints(l, cs);
                c.add(l);
                cs.gridheight = 1;
            }
            else if (name.equals("cvtable")) {
                log.debug("starting to build CvTable pane");
                // this is copied from SymbolicProgFrame
                JTable			cvTable		= new JTable(_cvModel);
                JScrollPane 	cvScroll	= new JScrollPane(cvTable);
                cvTable.setDefaultRenderer(JTextField.class, new ValueRenderer());
                cvTable.setDefaultRenderer(JButton.class, new ValueRenderer());
                cvTable.setDefaultEditor(JTextField.class, new ValueEditor());
                cvTable.setDefaultEditor(JButton.class, new ValueEditor());
                cvTable.setRowHeight(new JButton("X").getPreferredSize().height);
                cvScroll.setColumnHeaderView(cvTable.getTableHeader());
                // have to shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
                // instead of forcing the columns to fill the frame (and only fill)
                cvTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

                cs.gridheight = GridBagConstraints.REMAINDER;
                g.setConstraints(cvScroll, cs);
                c.add(cvScroll);
                cs.gridheight = 1;

                // remember which CVs to read/write
                for (int j=0; j<_cvModel.getRowCount(); j++) {
                    cvList.add(new Integer(j));
                }

                log.debug("end of building CvTable pane");

            }
            else if (name.equals("fnmapping")) {
                FnMapPanel l = new FnMapPanel(_varModel, varList, modelElem);
                fnMapList.add(l); // remember for deletion
                cs.gridheight = GridBagConstraints.REMAINDER;
                g.setConstraints(l, cs);
                c.add(l);
                cs.gridheight = 1;
            }
            else if (name.equals("dccaddress")) {
                JPanel l = addDccAddressPanel(e);
                cs.gridheight = GridBagConstraints.REMAINDER;
                g.setConstraints(l, cs);
                c.add(l);
                cs.gridheight = 1;
            }
            else if (name.equals("column")) {
                // nested "column" elements ...
                cs.gridheight = GridBagConstraints.REMAINDER;
                JPanel l = newColumn(e, showStdName, modelElem);
                panelList.add(l);
                g.setConstraints(l, cs);
                c.add(l);
                cs.gridheight = 1;
            }
            else { // its a mistake
                log.error("No code to handle element of type "+e.getName()+" in newRow");
            }
        }
        // add glue to the bottom to allow resize
        c.add(Box.createVerticalGlue());

        return c;
    }

    /**
     * Add the representation of a single variable.  The
     * variable is defined by a JDOM variable Element from the XML file.
     */
    public void newVariable( Element var, JComponent col,
                             GridBagLayout g, GridBagConstraints cs, boolean showStdName) {

        // get the name
        String name = var.getAttribute("item").getValue();

        // if it doesn't exist, do nothing
        int i = _varModel.findVarIndex(name);
        if (i<0) {
            if (log.isDebugEnabled()) log.debug("Variable \""+name+"\" not found, omitted");
            return;
        }

        // check label orientation
        Attribute attr;
        String layout ="left";  // this default is also set in the DTD
        if ( (attr = var.getAttribute("layout")) != null && attr.getValue() != null)
            layout = attr.getValue();

        // load label if specified, else use name
        String label = name;
        if (!showStdName) {
            // get name attribute from variable, as that's the mfg name
            label = _varModel.getLabel(i);
        }
        String temp ="";
        if ( (attr = var.getAttribute("label")) != null
             && (temp = attr.getValue()) != null )
            label = temp;
        JLabel l = new JLabel(" "+label+" ");

        // get representation; store into the list to be programmed
        JComponent rep = getRepresentation(name, var);
        if (i>=0) varList.add(new Integer(i));

        // now handle the four orientations
        // assemble v from label, rep

        if (layout.equals("left")) {
            cs.anchor= GridBagConstraints.EAST;
            g.setConstraints(l, cs);
            col.add(l);

            cs.gridx = GridBagConstraints.RELATIVE;
            cs.anchor= GridBagConstraints.WEST;
            g.setConstraints(rep, cs);
            col.add(rep);

        } else if (layout.equals("right")) {
            cs.anchor= GridBagConstraints.EAST;
            g.setConstraints(rep, cs);
            col.add(rep);

            cs.gridx = GridBagConstraints.RELATIVE;
            cs.anchor= GridBagConstraints.WEST;
            g.setConstraints(l, cs);
            col.add(l);

        } else if (layout.equals("below")) {
            // variable in center of upper line
            cs.anchor=GridBagConstraints.CENTER;
            g.setConstraints(rep, cs);
            col.add(rep);

            // label aligned like others
            cs.gridy++;
            cs.anchor= GridBagConstraints.WEST;
            g.setConstraints(l, cs);
            col.add(l);

        } else if (layout.equals("above")) {
            // label aligned like others
            cs.anchor= GridBagConstraints.WEST;
            g.setConstraints(l, cs);
            col.add(l);

            // variable in center of lower line
            cs.gridy++;
            cs.anchor=GridBagConstraints.CENTER;
            g.setConstraints(rep, cs);
            col.add(rep);

        } else {
            log.error("layout internally inconsistent: "+layout);
            return;
        }
    }

    public JComponent getRepresentation(String name, Element var) {
        int i = _varModel.findVarIndex(name);
        JComponent rep = null;
        String format = "default";
        Attribute attr;
        if ( (attr = var.getAttribute("format")) != null && attr.getValue() != null) format = attr.getValue();

        if (i>= 0) {
            rep = getRep(i, format);
            rep.setMaximumSize(rep.getPreferredSize());
            // set tooltip if specified
            if ( (attr = var.getAttribute("tooltip")) != null && attr.getValue() != null)
                rep.setToolTipText(attr.getValue());
        }
        return rep;
    }

    JComponent getRep(int i, String format) {
        return (JComponent)(_varModel.getRep(i, format));
    }

    /** list of fnMapping objects to dispose */
    ArrayList fnMapList = new ArrayList();
    /** list of JPanel objects to removeAll */
    ArrayList panelList = new ArrayList();

    public void dispose() {
        if (log.isDebugEnabled()) log.debug("dispose");

        // remove components
        removeAll();

        readButton.removeActionListener(l1);
        reReadButton.removeActionListener(l2);
        writeButton.removeActionListener(l3);
        reWriteButton.removeActionListener(l4);
        l1 = l2 = l3 = l4 = null;

        if (_programmingVar != null) _programmingVar.removePropertyChangeListener(this);
        if (_programmingCV != null) _programmingCV.removePropertyChangeListener(this);

        prop = null;
        _programmingVar = null;

        varList.clear();
        varList = null;
        cvList.clear();
        cvList = null;

        // dispose of any fnMaps
        for (int i=0; i<panelList.size(); i++) {
            ((JPanel)(panelList.get(i))).removeAll();
        }
        panelList.clear();
        panelList = null;

        // dispose of any fnMaps
        for (int i=0; i<fnMapList.size(); i++) {
            ((FnMapPanel)(fnMapList.get(i))).dispose();
        }
        fnMapList.clear();
        fnMapList = null;

        readButton = null;
        writeButton = null;
        // these two are disposed elsewhere
        _cvModel = null;
        _varModel = null;
    }

    public void printPane(HardcopyWriter w) {
        // if pane is empty, don't print anything
        if (varList.size() == 0 && cvList.size() == 0) return;

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
            String heading1 = "Field";
            String heading2 = "Setting";
            String s;
            int interval = spaces.length()- heading1.length();
            w.setFontStyle(Font.BOLD);
            if (cvList.size() > 0){
              s = mName.toUpperCase();
              w.write(s, 0, s.length());
              w.write(w.getCurrentLineNumber(), 0, w.getCurrentLineNumber() + 1, 0);
              w.write(w.getCurrentLineNumber(), w.getCharactersPerLine() + 1,
                      w.getCurrentLineNumber() + 1, w.getCharactersPerLine() + 1);
              //Draw horizontal dividing line for each Pane section
              w.write(w.getCurrentLineNumber()-1, 0, w.getCurrentLineNumber()-1,
                      w.getCharactersPerLine()+1);
              s = "\n";
              w.write(s,0,s.length());
            }
            else {
              s = mName.toUpperCase();
              w.write(s, 0, s.length());
              w.write(w.getCurrentLineNumber(),0,w.getCurrentLineNumber() + 1,0);
              w.write(w.getCurrentLineNumber(),w.getCharactersPerLine() + 1,w.getCurrentLineNumber() + 1,w.getCharactersPerLine() + 1);
              s = "\n";
              w.write(s,0,s.length());
              w.setFontStyle(Font.BOLD + Font.ITALIC);
              s = "   " + heading1 + spaces.substring(0,interval) + "   Setting";
              w.write(s, 0, s.length());
              w.write(w.getCurrentLineNumber(),0,w.getCurrentLineNumber() + 1,0);
              w.write(w.getCurrentLineNumber(),w.getCharactersPerLine() + 1,w.getCurrentLineNumber() + 1,w.getCharactersPerLine() + 1);
              //Draw horizontal dividing line for each Pane section
              w.write(w.getCurrentLineNumber()-1, 0, w.getCurrentLineNumber()-1,
                      w.getCharactersPerLine()+1);
              s = "\n";
              w.write(s,0,s.length());
            }
            w.setFontStyle(Font.PLAIN);
            // index over variables
            for (int i=0; i<varList.size(); i++) {
                int varNum = ((Integer)varList.get(i)).intValue();
                VariableValue var = _varModel.getVariable(varNum);
                String name = var.label();
                if (name == null) name = var.item();
                String value = var.getTextValue();

               //define index values for name and value substrings
                int nameLeftIndex = 0;
                int nameRightIndex = name.length();
                int valueLeftIndex = 0;
                int valueRightIndex =value.length();
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
                  w.write(w.getCurrentLineNumber(),0,w.getCurrentLineNumber() + 1,0);
                  w.write(w.getCurrentLineNumber(),w.getCharactersPerLine() + 1,w.getCurrentLineNumber() + 1,w.getCharactersPerLine() + 1);
                  s = "\n";
                  w.write(s,0,s.length());
                }
            }

            // index over CVs
            if (cvList.size() > 0){
              w.setFontStyle(Font.BOLD); //set font to Bold
              // print a simple heading
              s = "         Value               Value               Value               Value";
              w.write(s, 0, s.length());
              w.write(w.getCurrentLineNumber(), 0, w.getCurrentLineNumber() + 1, 0);
              w.write(w.getCurrentLineNumber(), w.getCharactersPerLine() + 1,
                      w.getCurrentLineNumber() + 1, w.getCharactersPerLine() + 1);
              s = "\n";
              w.write(s,0,s.length());
              s = "   CV   Dec Hex        CV   Dec Hex        CV   Dec Hex        CV   Dec Hex";
              w.write(s, 0, s.length());
              w.write(w.getCurrentLineNumber(), 0, w.getCurrentLineNumber() + 1, 0);
              w.write(w.getCurrentLineNumber(), w.getCharactersPerLine() + 1,
                      w.getCurrentLineNumber() + 1, w.getCharactersPerLine() + 1);
              s = "\n";
              w.write(s,0,s.length());
              w.setFontStyle(0); //set font back to Normal
              //           }
              /*create an array to hold CV/Value strings to allow reformatting and sorting
                Same size as the table drawn above (4 columns*tableHeight; heading rows
                not included)
               */
              int tableHeight = 20;
              String[] cvStrings = new String[4 * tableHeight];

              //blank the array
              for (int j = 0; j < cvStrings.length; j++)
                cvStrings[j] = "";

                // get each CV and value
              for (int i = 0; i < cvList.size(); i++) {

                int cvNum = ( (Integer) cvList.get(i)).intValue();
                CvValue cv = _cvModel.getCvByRow(cvNum);
      //-------------------------------------------------------------------------

                int num = cv.number();
                int value = cv.getValue();

                //convert and pad numbers as needed
                String numString = Integer.toString(num);
                String valueString = Integer.toString(value);
                String valueStringHex = Integer.toHexString(value).toUpperCase();
                if (value < 16)
                  valueStringHex = "0" + valueStringHex;
                for (int j = 1; j < 3; j++) {
                  if (numString.length() < 3)
                    numString = " " + numString;
                }
                for (int j = 1; j < 3; j++) {
                  if (valueString.length() < 3)
                    valueString = " " + valueString;
                }
                //Create composite string of CV and its decimal and hex values
                s = "  " + numString + "   " + valueString + "  " + valueStringHex +
                    " ";

                //populate printing array - still treated as a single column
                cvStrings[i] = s;
              }
                //sort the array in CV order (just the members with values)
                String temp;
                boolean swap = false;
                do {
                  swap = false;
                  for (int i = 0; i < _cvModel.getRowCount() - 1; i++) {
                    if (Integer.parseInt(cvStrings[i + 1].substring(2, 5).trim()) <
                        Integer.parseInt(cvStrings[i].substring(2, 5).trim())) {
                      temp = cvStrings[i + 1];
                      cvStrings[i + 1] = cvStrings[i];
                      cvStrings[i] = temp;
                      swap = true;
                    }
                  }
                }
                while (swap == true);

                //Print the array in four columns
                for (int i = 0; i < tableHeight; i++) {
                  s = cvStrings[i] + "    " + cvStrings[i + tableHeight] + "    " + cvStrings[i +
                      tableHeight * 2] + "    " + cvStrings[i + tableHeight * 3];
                  w.write(s, 0, s.length());
                  w.write(w.getCurrentLineNumber(), 0, w.getCurrentLineNumber() + 1, 0);
                  w.write(w.getCurrentLineNumber(), w.getCharactersPerLine() + 1,
                          w.getCurrentLineNumber() + 1, w.getCharactersPerLine() + 1);
                  s = "\n";
                  w.write(s,0,s.length());
                }
            }
              s = "\n\n";
              w.write(w.getCurrentLineNumber(), 0, w.getCurrentLineNumber() + 2, 0);
              w.write(w.getCurrentLineNumber(), w.getCharactersPerLine() + 1,
                      w.getCurrentLineNumber() + 2, w.getCharactersPerLine() + 1);
              w.write(s, 0, s.length());

            // handle special cases

        } catch (IOException e) { log.warn("error during printing: "+e);
        }

    }

    private JPanel addDccAddressPanel(Element e) {
        JPanel l;
        if (e.getAttribute("label")!=null)
            l = new DccAddressPanel(_varModel, e.getAttribute("label").getValue());
        else
            l = new DccAddressPanel(_varModel);
        panelList.add(l);
        // make sure this will get read/written, even if real vars not on pane
        int iVar;
        iVar = _varModel.findVarIndex("Address Format");
        if (iVar>=0) varList.add(new Integer(iVar));
        else log.debug("addDccAddressPanel did not find Address Format");

        iVar = _varModel.findVarIndex("Short Address");
        if (iVar>=0) varList.add(new Integer(iVar));
        else log.debug("addDccAddressPanel did not find Short Address");

        iVar = _varModel.findVarIndex("Long Address");
        if (iVar>=0) varList.add(new Integer(iVar));
        else log.debug("addDccAddressPanel did not find Long Address");
        return l;
    }

    // handle outgoing parameter notification for the Busy parameter
    java.beans.PropertyChangeSupport prop = new java.beans.PropertyChangeSupport(this);
    public void removePropertyChangeListener(java.beans.PropertyChangeListener p) { prop.removePropertyChangeListener(p); }
    public void addPropertyChangeListener(java.beans.PropertyChangeListener p) { prop.addPropertyChangeListener(p); }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PaneProgPane.class.getName());

}
