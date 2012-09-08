// PaneSet.java

package jmri.jmrit.symbolicprog.tabbedframe;

import org.jdom.*;

import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

//import jmri.*;
import jmri.Programmer;
import jmri.jmrit.decoderdefn.*;
import jmri.jmrit.roster.*;
import jmri.jmrit.symbolicprog.*;


/**
 * Interface for the container of a set of PaneProgPanes.
 * The panes use services provided here to 
 * work with buttons and the busy cursor.
 *
 * TODO:
 * Several methods are copied from PaneProgFrame and should be refactored
 * No programmer support yet
 * No glass pane support
 * Need better support for visible/non-visible panes
 * Special panes (Roster entry, attributes, graphics) not included
 *
 * @see apps.gui3.dp3.DecoderPro3Window
 *
 * @author    Bob Jacobsen Copyright (C) 2010
 * @version		$Revision$
 */
public class PaneSet {

    static final java.util.ResourceBundle rbt = jmri.jmrit.symbolicprog.SymbolicProgBundle.bundle();
    List<PaneProgPane>  paneList     = new ArrayList<PaneProgPane>();
    PaneContainer       container;
    Programmer          mProgrammer;
    CvTableModel        cvModel      = null;
    IndexedCvTableModel iCvModel     = null;
    VariableTableModel  variableModel;
    ResetTableModel     resetModel   = null;
    JLabel              progStatus   = new JLabel(rbt.getString("StateIdle"));

    /**
     * The 'model' element representing the decoder type
     */
    Element modelElem = null;

    
    public PaneSet(PaneContainer container, RosterEntry re, Programmer programmer) {
        this.container = container;
        this.mProgrammer = programmer;

        cvModel       = new CvTableModel(progStatus, mProgrammer);
        iCvModel      = new IndexedCvTableModel(progStatus, mProgrammer);

        variableModel = new VariableTableModel(progStatus, new String[]  {"Name", "Value"},
                                                 cvModel, iCvModel);

        resetModel    = new ResetTableModel(progStatus, mProgrammer);

        if (re.getFileName() != null) {
            // set the loco file name in the roster entry
            re.readFile();  // read, but don't yet process
        }

        // load from decoder file
        loadDecoderFromLoco(re);
        
        // fill the CV values from the specific loco file
        if (re.getFileName() != null) re.loadCvModel(cvModel, iCvModel);
    }

    // copied from PaneProgFrame
    protected void loadDecoderFromLoco(RosterEntry r) {
        // get a DecoderFile from the locomotive xml
        String decoderModel = r.getDecoderModel();
        String decoderFamily = r.getDecoderFamily();
        if (log.isDebugEnabled()) log.debug("selected loco uses decoder "+decoderFamily+" "+decoderModel);
        // locate a decoder like that.
        List<DecoderFile> l = DecoderIndexFile.instance().matchingDecoderList(null, decoderFamily, null, null, null, decoderModel);
        if (log.isDebugEnabled()) log.debug("found "+l.size()+" matches");
        if (l.size() == 0) {
            log.debug("Loco uses "+decoderFamily+" "+decoderModel+" decoder, but no such decoder defined");
            // fall back to use just the decoder name, not family
            l = DecoderIndexFile.instance().matchingDecoderList(null, null, null, null, null, decoderModel);
            if (log.isDebugEnabled()) log.debug("found "+l.size()+" matches without family key");
        }
        if (l.size() > 0) {
            DecoderFile d = l.get(0);
            loadDecoderFile(d, r);
        } else {
            if (decoderModel.equals(""))
                log.debug("blank decoderModel requested, so nothing loaded");
            else
                log.warn("no matching \""+decoderModel+"\" decoder found for loco, no decoder info loaded");
        }
    }
    protected void loadDecoderFile(DecoderFile df, RosterEntry re) {
        if (df == null) {
            log.warn("loadDecoder file invoked with null object");
            return;
        }
        if (log.isDebugEnabled()) log.debug("loadDecoderFile from "+DecoderFile.fileLocation
                                        +" "+df.getFilename());

        try {
            decoderRoot = df.rootFromName(DecoderFile.fileLocation+df.getFilename());
        } catch (Exception e) { log.error("Exception while loading decoder XML file: "+df.getFilename(), e); }
        // load variables from decoder tree
        df.getProductID();
        df.loadVariableModel(decoderRoot.getChild("decoder"), variableModel);
        
        // load reset from decoder tree
        if (variableModel.piCv() >= 0) {
            resetModel.setPiCv(variableModel.piCv());
        }
        if (variableModel.siCv() >= 0) {
            resetModel.setSiCv(variableModel.siCv());
        }
        df.loadResetModel(decoderRoot.getChild("decoder"), resetModel);

        // load function names
        re.loadFunctions(decoderRoot.getChild("decoder").getChild("family").getChild("functionlabels"));
        
//         // get the showEmptyPanes attribute, if yes/no update our state
//         if (decoderRoot.getAttribute("showEmptyPanes") != null) {
//             if (log.isDebugEnabled()) log.debug("Found in decoder "+decoderRoot.getAttribute("showEmptyPanes").getValue());
//             if (decoderRoot.getAttribute("showEmptyPanes").getValue().equals("yes"))
//                 setShowEmptyPanes(true);
//             else if (decoderRoot.getAttribute("showEmptyPanes").getValue().equals("no"))
//                 setShowEmptyPanes(false);
//             // leave alone for "default" value
//             if (log.isDebugEnabled()) log.debug("result "+getShowEmptyPanes());
//         }
        
        // save the pointer to the model element
        modelElem = df.getModelElement();
    }
    Element decoderRoot = null;


    /**
     * Create a set of panes from a 
     * programmer definition and roster entry
     * @param root Root element of programmer XML definition
     * @param r Locomotive to load from
     */
	public void makePanes(Element root, RosterEntry r) {
        // check for "programmer" element at start
        Element base;
        if ( (base = root.getChild("programmer")) == null) {
            log.error("xml file top element is not programmer");
            return;
        }

        @SuppressWarnings("unchecked")
        List<Element> paneList = base.getChildren("pane");
        
        if (log.isDebugEnabled()) log.debug("will process "+paneList.size()+" pane definitions");

        for (Element e : paneList) {
            // load each pane
            String name = e.getAttribute("name").getValue();
            newPane( name, e, modelElem);
        }
    }

    /**
     * Create a single pane from a "pane" element in programmer or decoder definition
     */
    public void newPane(String name, Element pane, Element modelElem) {
        if (log.isDebugEnabled()) log.debug("newPane "+name);
        // create a panel to hold columns
        PaneProgPane p = new PaneProgPane(container, name, pane, cvModel, iCvModel, variableModel, modelElem);

        // and remember it for programming
        paneList.add(p);
    }
    public List<PaneProgPane> getList() { return paneList; }
    
    /**
     * Store current content to file
     */
    public void storeFile(RosterEntry re) {
        // set up file write
        re.ensureFilenameExists();

        // write the RosterEntry to its file
        re.writeFile(cvModel, iCvModel, variableModel );
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PaneSet.class.getName());
}

