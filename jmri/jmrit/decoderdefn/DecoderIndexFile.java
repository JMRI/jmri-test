// DecoderIndexFile.java

package jmri.jmrit.decoderdefn;

import jmri.jmrit.XmlFile;
import java.io.File;
import java.util.Enumeration;

import javax.swing.JComboBox;
import com.sun.java.util.collections.List;
import com.sun.java.util.collections.ArrayList;
import com.sun.java.util.collections.Hashtable;

import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Document;
import org.jdom.DocType;
import org.jdom.output.XMLOutputter;

// try to limit the JDOM to this class, so that others can manipulate...

/**
 * DecoderIndex represents a decoderIndex.xml file in memory, allowing a program
 * to navigate to various decoder descriptions without having to
 * manipulate files.
 *<P>
 * This class doesn't provide tools for defining the index; that's done manually, or
 * at least not done here.
 *<P>
 * Multiple DecoderIndexFile objects don't make sense, so we use an "instance" member
 * to navigate to a single one.
 *
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Revision: 1.4 $
 *
 */
public class DecoderIndexFile extends XmlFile {

	// fill in abstract members

	protected List decoderList = new ArrayList();
	public int numDecoders() { return decoderList.size(); }

	// map mfg ID numbers from & to mfg names
	protected Hashtable _mfgIdFromNameHash = new Hashtable();
	protected Hashtable _mfgNameFromIdHash = new Hashtable();

    protected ArrayList mMfgNameList = new ArrayList();

    public List getMfgNameList() { return mMfgNameList; }

	public String mfgIdFromName(String name) {
		return (String)_mfgIdFromNameHash.get(name);
	}

	public String mfgNameFromId(String name) {
		return (String)_mfgNameFromIdHash.get(name);
	}

	/**
	 *	Get a List of decoders matching some information
	 */
	public List matchingDecoderList(String mfg, String family, String decoderMfgID, String decoderVersionID, String model ) {
		List l = new ArrayList();
		for (int i = 0; i < numDecoders(); i++) {
			if ( checkEntry(i, mfg, family, decoderMfgID, decoderVersionID, model )) {
				l.add(decoderList.get(i));
			}
		}
		return l;
	}

	/**
	 * Get a JComboBox representing the choices that match
	 * some information
	 */
	public JComboBox matchingComboBox(String mfg, String family, String decoderMfgID, String decoderVersionID, String model ) {
		List l = matchingDecoderList(mfg, family, decoderMfgID, decoderVersionID, model );
        return jComboBoxFromList(l);
	}

    /**
     * Return a JComboBox made with the titles from a list of
     * DecoderFile entries
     */
    static public JComboBox jComboBoxFromList(List l) {
		return new JComboBox(jComboBoxModelFromList(l));
    }

    /**
     * Return a new ComboBoxModel made with the titles from a list of
     * DecoderFile entries
     */
    static public javax.swing.ComboBoxModel jComboBoxModelFromList(List l) {
		javax.swing.DefaultComboBoxModel b = new javax.swing.DefaultComboBoxModel();
		for (int i = 0; i < l.size(); i++) {
			DecoderFile r = (DecoderFile)l.get(i);
			b.addElement(r.titleString());
		}
		return b;
    }

	/**
	 * Return DecoderFile from a "title" string, ala selection in matchingComboBox
	 */
	public DecoderFile fileFromTitle(String title ) {
		for (int i = 0; i < numDecoders(); i++) {
			DecoderFile r = (DecoderFile)decoderList.get(i);
			if (r.titleString().equals(title)) return r;
		}
		return null;
	}

	/**
	* Check if an entry consistent with specific properties. A null String entry
	* always matches. Strings are used for convenience in GUI building.
	* Don't bother asking about the model number...
	*
	*/
	public boolean checkEntry(int i, String mfgName, String family, String mfgID, String decoderVersionID, String model) {
		DecoderFile r = (DecoderFile)decoderList.get(i);
		if (mfgName != null && !mfgName.equals(r.getMfg())) return false;
		if (family != null && !family.equals(r.getFamily())) return false;
		if (mfgID != null && !mfgID.equals(r.getMfgID())) return false;
		if (model != null && !model.equals(r.getModel())) return false;
		// check version ID - no match if a range specified and out of range
		if (decoderVersionID != null) {
			int versionID = Integer.valueOf(decoderVersionID).intValue();
            if (!r.isVersion(versionID)) return false;
		}
		return true;
	}

	static DecoderIndexFile _instance = null;
	public synchronized static DecoderIndexFile instance() {
		if (_instance == null) {
			if (log.isDebugEnabled()) log.debug("DecoderIndexFile creating instance");
			// create and load
			_instance = new DecoderIndexFile();
			try {
				_instance.readFile(defaultDecoderIndexFilename());
			} catch (Exception e) {
				log.error("Exception during decoder index reading: "+e);
			}
		}
		if (log.isDebugEnabled()) log.debug("DecoderIndexFile returns instance "+_instance);
		return _instance;
	}

	/**
	 * Read the contents of a decoderIndex XML file into this object. Note that this does not
	 * clear any existing entries.
	 */
	void readFile(String name) throws org.jdom.JDOMException, java.io.FileNotFoundException {
		if (log.isDebugEnabled()) log.debug("readFile "+name);

		// read file, find root
		Element root = rootFromName(name);

		// decode type, invoke proper processing routine if a decoder file
		if (root.getChild("decoderIndex") != null) {
			readMfgSection(root.getChild("decoderIndex"));
			readFamilySection(root.getChild("decoderIndex"));
		}
		else {
			log.error("Unrecognized decoderIndex file contents in file: "+name);
		}
	}

	void readMfgSection(Element decoderIndex) {
		Element mfgList = decoderIndex.getChild("mfgList");
		if (mfgList != null) {

			List l = mfgList.getChildren("manufacturer");
			if (log.isDebugEnabled()) log.debug("readMfgSection sees "+l.size()+" children");
			for (int i=0; i<l.size(); i++) {
				// handle each entry
				Element el = (Element)l.get(i);
				String mfg = el.getAttribute("mfg").getValue();
                mMfgNameList.add(mfg);
				Attribute attr = el.getAttribute("mfgID");
				if (attr != null) {
					_mfgIdFromNameHash.put(mfg, attr.getValue());
					_mfgNameFromIdHash.put(attr.getValue(), mfg);
				}
			}
		} else log.warn("no mfgList found in decoderIndexFile");
	}

	void readFamilySection(Element decoderIndex) {
		Element familyList = decoderIndex.getChild("familyList");
		if (familyList != null) {

			List l = familyList.getChildren("family");
			if (log.isDebugEnabled()) log.debug("readFamilySection sees "+l.size()+" children");
			for (int i=0; i<l.size(); i++) {
				// handle each entry
				Element el = (Element)l.get(i);
				readFamily(el);
			}
		} else log.warn("no familyList found in decoderIndexFile");
	}

	void readFamily(Element family) {
		Attribute attr;
		String filename = family.getAttribute("file").getValue();
		String parentLowVersID = ((attr = family.getAttribute("lowVersionID"))     != null ? attr.getValue() : null );
		String parentHighVersID = ((attr = family.getAttribute("highVersionID"))     != null ? attr.getValue() : null );
		String familyName   = ((attr = family.getAttribute("name"))     != null ? attr.getValue() : null );
		String mfg   = ((attr = family.getAttribute("mfg"))     != null ? attr.getValue() : null );
		String mfgID   = mfgIdFromName(mfg);

		List l = family.getChildren("model");
		if (log.isDebugEnabled()) log.debug("readFamily sees "+l.size()+" children");

        // if there are multiple models in this family, record the family as a type
        //if (l.size()>1){
            DecoderFile vFamilyDecoderFile
                = new DecoderFile( mfg, mfgID, familyName,
                                    parentLowVersID, parentHighVersID,
                                    familyName,
                                    filename,
                                    -1, -1, null); // numFns, numOuts, XML element unknown
            decoderList.add(vFamilyDecoderFile);
        //}

		// record each of the decoders
		for (int i=0; i<l.size(); i++) {
			// handle each entry by creating a DecoderFile object containing all it knows
			Element decoder = (Element)l.get(i);
			String loVersID = ( (attr = decoder.getAttribute("lowVersionID"))     != null ? attr.getValue() : parentLowVersID);
			String hiVersID = ( (attr = decoder.getAttribute("highVersionID"))     != null ? attr.getValue() : parentHighVersID);
			int numFns   = ((attr = decoder.getAttribute("numFns"))     != null ? Integer.valueOf(attr.getValue()).intValue() : -1 );
			int numOuts   = ((attr = decoder.getAttribute("numOuts"))     != null ? Integer.valueOf(attr.getValue()).intValue() : -1 );
			DecoderFile df = new DecoderFile( mfg, mfgID,
									( (attr = decoder.getAttribute("model"))     != null ? attr.getValue() : null ),
									loVersID, hiVersID, familyName, filename, numFns, numOuts, decoder);
			// and store it
			decoderList.add(df);
            // if there are additional version numbers defined, handle them too
            List vcodes = decoder.getChildren("versionCV");
		    for (int j=0; j<vcodes.size(); j++) {
                // for each versionCV element
    			Element vcv = (Element)vcodes.get(j);
    			String vLoVersID = ( (attr = vcv.getAttribute("lowVersionID")) != null ? attr.getValue() : loVersID);
	    		String vHiVersID = ( (attr = vcv.getAttribute("highVersionID"))!= null ? attr.getValue() : hiVersID);
                df.setVersionRange(vLoVersID, vHiVersID);
            }
		}
	}

	public void writeFile(String name, DecoderIndexFile oldIndex, String files[]) throws java.io.IOException {
		if (log.isInfoEnabled()) log.info("writeFile "+name);
		// This is taken in large part from "Java and XML" page 368
		File file = new File(prefsDir()+name);

		// create root element
		Element root = new Element("decoderIndex-config");
		Document doc = new Document(root);
		doc.setDocType(new DocType("decoderIndex-config","decoderIndex-config.dtd"));

		// add top-level elements
		Element index;
		root.addContent(index = new Element("decoderIndex"));

		// add mfg list from existing DecoderIndexFile item
		Element mfgList = new Element("mfgList");
        // We treat "NMRA" special...
        Element mfg = new Element("manufacturer");
        mfg.addAttribute("mfg","NMRA");
        mfg.addAttribute("mfgID","999");
        mfgList.addContent(mfg);
        // start working on the rest of the entries
		Enumeration keys = oldIndex._mfgIdFromNameHash.keys();
        List l = new ArrayList();
		while (keys.hasMoreElements()) {
            l.add((String)keys.nextElement());
        }
        Object[] s = l.toArray();
        // all of the above mess was to get something we can sort into alpha order
        java.util.Arrays.sort(s);
        for (int i=0; i<s.length; i++) {
			String mfgName = (String)s[i];
            if (!mfgName.equals("NMRA")){
                mfg = new Element("manufacturer");
    			mfg.addAttribute("mfg",mfgName);
    			mfg.addAttribute("mfgID",(String)oldIndex._mfgIdFromNameHash.get(mfgName));
    			mfgList.addContent(mfg);
            }
		}

		// add family list by scanning files
		Element familyList = new Element("familyList");
		for (int i=0; i<files.length; i++) {
			DecoderFile d = new DecoderFile();
			try {
				Element droot = d.rootFromName(DecoderFile.fileLocation+files[i]);
				Element family = droot.getChild("decoder").getChild("family").getCopy("family");
				family.addAttribute("file",files[i]);
				familyList.addContent(family);
			}
			catch (org.jdom.JDOMException exj) {log.error("could not parse "+files[i]+": "+exj.getMessage());}
			catch (java.io.FileNotFoundException exj) {log.error("could not read "+files[i]+": "+exj.getMessage());}
		}

		index.addContent(mfgList);
		index.addContent(familyList);

		// write the result to selected file
		java.io.FileOutputStream o = new java.io.FileOutputStream(file);
		XMLOutputter fmt = new XMLOutputter();
		fmt.setNewlines(true);   // pretty printing
		fmt.setIndent(true);
		fmt.output(doc, o);

		// force a read of the new file next time
		_instance = null;
	}


	/**
	* Return the filename String for the default decoder index file, including location.
	* This is here to allow easy override in tests.
	*/
	protected static String defaultDecoderIndexFilename() { return decoderIndexFileName;}

	static final protected String decoderIndexFileName = "decoderIndex.xml";
	// initialize logging
    static private org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DecoderIndexFile.class.getName());

}
