// DecoderFile.java

package jmri.jmrit.decoderdefn;

import java.io.*;
import com.sun.java.util.collections.List;
import jmri.jmrit.XmlFile;
import jmri.jmrit.symbolicprog.VariableTableModel;
import org.jdom.Element;

// try to limit the JDOM to this class, so that others can manipulate...

/**
 * Represents and manipulates a decoder definition, both as a file and
 * in memory.  The interal storage is a JDOM tree.
 *<P>
 * This object is created by DecoderIndexFile to represent the
 * decoder identification info _before_ the actual decoder file is read.
 *
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Revision: 1.4 $
 * @see jmri.jmrit.decoderdefn.DecoderIndexFile
 */
public class DecoderFile extends XmlFile {

	public DecoderFile() {}

	public DecoderFile(String mfg, String mfgID, String model, String lowVersionID,
						String highVersionID, String family, String filename,
						int numFns, int numOuts, Element decoder) {
		_mfg = mfg;
		_mfgID = mfgID;
		_model = model;
		_family = family;
		_filename = filename;
		_numFns = numFns;
		_numOuts = numOuts;
		_element = decoder;

        // store the default range of version id's
        setVersionRange(lowVersionID, highVersionID);
	}

    // store acceptable version numbers
    boolean versions[] = new boolean[256];
    public void setOneVersion(int i) { versions[i] = true; }
    public void setVersionRange(int low, int high) {
        for (int i=low; i<=high; i++) versions[i] = true;
    }
    public void setVersionRange(String lowVersionID,String highVersionID) {
        if (lowVersionID!=null) {
            // lowVersionID is not null; check high version ID
            if (highVersionID!=null) {
                // low version and high version are not null
                setVersionRange(Integer.valueOf(lowVersionID).intValue(),
                            Integer.valueOf(highVersionID).intValue());
            } else {
                // low version not null, but high is null. This is
                // a single value to match
                setOneVersion(Integer.valueOf(lowVersionID).intValue());
            }
        } else {
            // lowVersionID is null; check high version ID
            if (highVersionID!=null) {
                // low version null, but high is not null
                setOneVersion(Integer.valueOf(highVersionID).intValue());
            } else {
                // both low and high version are null; do nothing
            }
        }
    }

    public boolean isVersion(int i) { return versions[i]; }

	// store indexing information
	String _mfg       = null;
	String _mfgID     = null;
	String _model     = null;
	String _family    = null;
	String _filename  = null;
	int _numFns  = -1;
	int _numOuts  = -1;
	Element _element = null;

	public String getMfg()       { return _mfg; }
	public String getMfgID()     { return _mfgID; }
	public String getModel()     { return _model; }
	public String getFamily()    { return _family; }
	public String getFilename()  { return _filename; }
	public int getNumFunctions()  { return _numFns; }
	public int getNumOutputs()  { return _numOuts; }

	public Element getModelElement() { return _element; }

	// static service methods - extract info from a given Element
	public static String getMfgName(Element decoderElement) {
		return decoderElement.getChild("family").getAttribute("mfg").getValue();
	}

	public static String getMfgID(Element decoderElement) {
		return decoderElement.getChild("id").getAttribute("mfgID").getValue();
	}

	public static String getFamilyName(Element decoderElement) {
		return decoderElement.getChild("id").getAttribute("family").getValue();
	}

	// use the decoder Element from the file to load a VariableTableModel for programming.
	public void loadVariableModel(Element decoderElement,
											VariableTableModel variableModel) {
		// find decoder id, assuming first decoder is fine for now (e.g. one per file)
		Element decoderID = decoderElement.getChild("id");

		// load variables to table
		List varList = decoderElement.getChild("variables").getChildren("variable");
		for (int i=0; i<varList.size(); i++) {
			Element e = (Element)(varList.get(i));
			try {
				// if its associated with an inconsistent number of functions,
				// skip creating it
				if (getNumFunctions() >= 0 && e.getAttribute("minFn") != null
					&& getNumFunctions() < Integer.valueOf(e.getAttribute("minFn").getValue()).intValue() )
						continue;
				// if its associated with an inconsistent number of outputs,
				// skip creating it
				if (getNumOutputs() >= 0 && e.getAttribute("minOut") != null
					&& getNumOutputs() < Integer.valueOf(e.getAttribute("minOut").getValue()).intValue() )
						continue;
			} catch (Exception ex) {
				log.warn("Problem parsing minFn or minOut in decoder file, variable "
							+e.getAttribute("item")+" exception: "+ex);
			}
			// load each row
			variableModel.setRow(i, e);
		}
		// load constants to table
		List consList = decoderElement.getChild("variables").getChildren("constant");
		for (int i=0; i<consList.size(); i++) {
			Element e = (Element)(consList.get(i));
			try {
				// if its associated with an inconsistent number of functions,
				// skip creating it
				if (getNumFunctions() >= 0 && e.getAttribute("minFn") != null
					&& getNumFunctions() < Integer.valueOf(e.getAttribute("minFn").getValue()).intValue() )
						continue;
				// if its associated with an inconsistent number of outputs,
				// skip creating it
				if (getNumOutputs() >= 0 && e.getAttribute("minOut") != null
					&& getNumOutputs() < Integer.valueOf(e.getAttribute("minOut").getValue()).intValue() )
						continue;
			} catch (Exception ex) {
				log.warn("Problem parsing minFn or minOut in decoder file, variable "
							+e.getAttribute("item")+" exception: "+ex);
			}
			// load each row
			variableModel.setConstant(e);
		}
		variableModel.configDone();
	}

	/**
	 * Convert to a cannonical text form for ComboBoxes, etc.
     * Early on, this had been mfg+" "+model, but
     * that resulted in a lot of duplicate mfg names in listings,
     * so now this is the same as the model
	 */
	public String titleString() {
		return getModel();
	}

	static public String fileLocation = "decoders"+File.separator;

	// initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DecoderFile.class.getName());

}
