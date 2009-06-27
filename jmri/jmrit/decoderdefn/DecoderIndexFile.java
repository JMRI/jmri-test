// DecoderIndexFile.java

package jmri.jmrit.decoderdefn;

import jmri.jmrit.XmlFile;
import java.io.File;
import java.util.Enumeration;

import javax.swing.JComboBox;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.ProcessingInstruction;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

// try to limit the JDOM to this class, so that others can manipulate...

/**
 * DecoderIndex represents a decoderIndex.xml file in memory.
 * <P>
 * This allows a program
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
 * @version			$Revision: 1.38 $
 *
 */
public class DecoderIndexFile extends XmlFile {

    // fill in abstract members

    protected List<DecoderFile> decoderList = new ArrayList<DecoderFile>();
    public int numDecoders() { return decoderList.size(); }

    int fileVersion = -1;

    // map mfg ID numbers from & to mfg names
    protected Hashtable<String,String> _mfgIdFromNameHash = new Hashtable<String,String>();
    protected Hashtable<String,String> _mfgNameFromIdHash = new Hashtable<String,String>();

    protected ArrayList<String> mMfgNameList = new ArrayList<String>();

    public List<String> getMfgNameList() { return mMfgNameList; }

    public String mfgIdFromName(String name) {
        return _mfgIdFromNameHash.get(name);
    }

    public String mfgNameFromId(String name) {
        return _mfgNameFromIdHash.get(name);
    }

    /**
     *	Get a List of decoders matching some information
     */
    public List<DecoderFile> matchingDecoderList(String mfg, String family, String decoderMfgID, String decoderVersionID, String decoderProductID, String model ) {
        List<DecoderFile> l = new ArrayList<DecoderFile>();
        for (int i = 0; i < numDecoders(); i++) {
            if ( checkEntry(i, mfg, family, decoderMfgID, decoderVersionID, decoderProductID, model )) {
                l.add(decoderList.get(i));
            }
        }
        return l;
    }

    /**
     * Get a JComboBox representing the choices that match
     * some information
     */
    public JComboBox matchingComboBox(String mfg, String family, String decoderMfgID, String decoderVersionID, String decoderProductID, String model ) {
        List<DecoderFile> l = matchingDecoderList(mfg, family, decoderMfgID, decoderVersionID, decoderProductID, model );
        return jComboBoxFromList(l);
    }

    /**
     * Return a JComboBox made with the titles from a list of
     * DecoderFile entries
     */
    static public JComboBox jComboBoxFromList(List<DecoderFile> l) {
        return new JComboBox(jComboBoxModelFromList(l));
    }

    /**
     * Return a new ComboBoxModel made with the titles from a list of
     * DecoderFile entries
     */
    static public javax.swing.ComboBoxModel jComboBoxModelFromList(List<DecoderFile> l) {
        javax.swing.DefaultComboBoxModel b = new javax.swing.DefaultComboBoxModel();
        for (int i = 0; i < l.size(); i++) {
            DecoderFile r = l.get(i);
            b.addElement(r.titleString());
        }
        return b;
    }

    /**
     * Return DecoderFile from a "title" string, ala selection in matchingComboBox.
     */
    public DecoderFile fileFromTitle(String title ) {
        for (int i = numDecoders()-1; i >= 0; i--) {
            DecoderFile r = decoderList.get(i);
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
    public boolean checkEntry(int i, String mfgName, String family, String mfgID, String decoderVersionID, String decoderProductID, String model) {
        DecoderFile r = decoderList.get(i);
        if (mfgName != null && !mfgName.equals(r.getMfg())) return false;
        if (family != null && !family.equals(r.getFamily())) return false;
        if (mfgID != null && !mfgID.equals(r.getMfgID())) return false;
        if (model != null && !model.equals(r.getModel())) return false;
        // check version ID - no match if a range specified and out of range
        if (decoderVersionID != null) {
            int versionID = Integer.valueOf(decoderVersionID).intValue();
            if (!r.isVersion(versionID)) return false;
        }
        if (decoderProductID != null && !decoderProductID.equals(r.getProductID())) return false;
        return true;
    }

    static DecoderIndexFile _instance = null;
    public synchronized static void resetInstance() { _instance = null; }
    public synchronized static DecoderIndexFile instance() {
        if (_instance == null) {
            if (log.isDebugEnabled()) log.debug("DecoderIndexFile creating instance");
            // create and load
            try {
                _instance = new DecoderIndexFile();
                _instance.readFile(defaultDecoderIndexFilename());
            } catch (Exception e) {
                log.error("Exception during decoder index reading: "+e);
                e.printStackTrace();
            }
            // see if needs to be updated
            try {
                if (updateIndexIfNeeded(defaultDecoderIndexFilename())) {
                    try {
                        _instance = new DecoderIndexFile();
                        _instance.readFile(defaultDecoderIndexFilename());
                    } catch (Exception e) {
                        log.error("Exception during decoder index reload: "+e);
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                log.error("Exception during decoder index update: "+e);
                e.printStackTrace();
            }
        }
        if (log.isDebugEnabled()) log.debug("DecoderIndexFile returns instance "+_instance);
        return _instance;
    }

    /**
     * Check whether the user's version of the decoder index file needs to be
     * updated; if it does, then force the update.
     * @return true is the index should be reloaded because it was updated
     * @param name
     * @throws JDOMException
     * @throws FileNotFoundException
     */
    static boolean updateIndexIfNeeded(String name) throws org.jdom.JDOMException, java.io.IOException {
        // get version from master index; if not found, give up
        String masterVersion = null;
        DecoderIndexFile masterXmlFile = new DecoderIndexFile();
        File masterFile = new File("xml"+File.separator+defaultDecoderIndexFilename());
        if (! masterFile.exists()) return false;
        Element masterRoot = masterXmlFile.rootFromFile(masterFile);
        if (masterRoot.getChild("decoderIndex") != null ) {
            if (masterRoot.getChild("decoderIndex").getAttribute("version")!=null)
                masterVersion = masterRoot.getChild("decoderIndex").getAttribute("version").getValue();
            log.debug("master version found, is "+masterVersion);
        }
        else {
            return false;
        }

        // get from user index.  Unless they are equal, force an update.
        // note we find this file via the search path; if not exists, so that
        // the master is found, we still do the right thing (nothing).
        String userVersion = null;
        DecoderIndexFile userXmlFile = new DecoderIndexFile();
        Element userRoot = userXmlFile.rootFromName(defaultDecoderIndexFilename());
        if (userRoot.getChild("decoderIndex") != null ) {
            if (userRoot.getChild("decoderIndex").getAttribute("version")!=null)
                userVersion = userRoot.getChild("decoderIndex").getAttribute("version").getValue();
            log.debug("user version found, is "+userVersion);
        }
        if (masterVersion!=null && masterVersion.equals(userVersion)) return false;

        // force the update, with the version number located earlier
        instance().fileVersion = Integer.parseInt(masterVersion);

        forceCreationOfNewIndex();
        // and force it to be used
        return true;

    }

    /**
     * Force creation of a new user index
     */
    static public void forceCreationOfNewIndex() {
        log.info("update decoder index");
        // make sure we're using only the default manufacturer info
        // to keep from propagating wrong, old stuff
        File oldfile = new File(XmlFile.prefsDir()+File.separator+"decoderIndex.xml");
        if (oldfile.exists()) {
            log.debug("remove existing user decoderIndex.xml file");
            oldfile.delete();
            // force read from distributed file
            resetInstance();
            instance();
        }

        // create an array of file names from decoders dir in preferences, count entries
        ArrayList<String> al = new ArrayList<String>();
        String[] sp = null;
        int i=0;
        XmlFile.ensurePrefsPresent(XmlFile.prefsDir()+DecoderFile.fileLocation);
        File fp = new File(XmlFile.prefsDir()+DecoderFile.fileLocation);
        if (fp.exists()) {
            sp = fp.list();
            for (i=0; i<sp.length; i++) {
                if (sp[i].endsWith(".xml") || sp[i].endsWith(".XML"))
                    al.add(sp[i]);
            }
        } else {
            log.warn(XmlFile.prefsDir()+"decoders was missing, though tried to create it");
        }
        // create an array of file names from xml/decoders, count entries
        String[] sx = (new File(XmlFile.xmlDir()+DecoderFile.fileLocation)).list();
        for (i=0; i<sx.length; i++) {
            if (sx[i].endsWith(".xml") || sx[i].endsWith(".XML")) {
                // Valid name.  Does it exist in preferences xml/decoders?
                if (!al.contains(sx[i])) {
                    // no, include it!
                    al.add(sx[i]);
                }
            }
        }
        // copy the decoder entries to the final array (toArray needs cast of +elements+)
        Object[] aa = al.toArray();
        String sbox[] = new String[al.size()];
        for (i =0; i<sbox.length; i++)
            sbox[i] = (String) aa[i];

        //the resulting array is now sorted on file-name to make it easier
        // for humans to read
        jmri.util.StringUtil.sort(sbox);

        // create a new decoderIndex
        // the existing version is used, so that a new master file
        // with a larger one will force an update
        DecoderIndexFile index = new DecoderIndexFile();
        index.fileVersion = instance().fileVersion;

        // write it out
        try {
            index.writeFile("decoderIndex.xml", _instance, sbox);
        } catch (java.io.IOException ex) {
            log.error("Error writing new decoder index file: "+ex.getMessage());
        } 
    }

    /**
     * Read the contents of a decoderIndex XML file into this object. Note that this does not
     * clear any existing entries; reset the instance to do that.
     */
    void readFile(String name) throws org.jdom.JDOMException, java.io.IOException {
        if (log.isDebugEnabled()) log.debug("readFile "+name);

        // read file, find root
        Element root = rootFromName(name);

        // decode type, invoke proper processing routine if a decoder file
        if (root.getChild("decoderIndex") != null) {
            if (root.getChild("decoderIndex").getAttribute("version")!=null)
                fileVersion = Integer.parseInt(root.getChild("decoderIndex")
                                               .getAttribute("version")
                                               .getValue()
                                               );
            log.debug("found fileVersion of "+fileVersion);
            readMfgSection(root.getChild("decoderIndex"));
            readFamilySection(root.getChild("decoderIndex"));
        }
        else {
            log.error("Unrecognized decoderIndex file contents in file: "+name);
        }
    }

    @SuppressWarnings("unchecked")
	void readMfgSection(Element decoderIndex) {
        Element mfgList = decoderIndex.getChild("mfgList");
        if (mfgList != null) {

            Attribute a;
            a = mfgList.getAttribute("nmraListDate");
            if (a!=null) nmraListDate = a.getValue();
            a = mfgList.getAttribute("updated");
            if (a!=null) updated = a.getValue();
            
            List<Element> l = mfgList.getChildren("manufacturer");
            if (log.isDebugEnabled()) log.debug("readMfgSection sees "+l.size()+" children");
            for (int i=0; i<l.size(); i++) {
                // handle each entry
                Element el = l.get(i);
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

    @SuppressWarnings("unchecked")
	void readFamilySection(Element decoderIndex) {
        Element familyList = decoderIndex.getChild("familyList");
        if (familyList != null) {

            List<Element> l = familyList.getChildren("family");
            if (log.isDebugEnabled()) log.debug("readFamilySection sees "+l.size()+" children");
            for (int i=0; i<l.size(); i++) {
                // handle each entry
                Element el = l.get(i);
                readFamily(el);
            }
        } else log.warn("no familyList found in decoderIndexFile");
    }

    @SuppressWarnings("unchecked")
	void readFamily(Element family) {
        Attribute attr;
        String filename = family.getAttribute("file").getValue();
        String parentLowVersID = ((attr = family.getAttribute("lowVersionID"))     != null ? attr.getValue() : null );
        String parentHighVersID = ((attr = family.getAttribute("highVersionID"))     != null ? attr.getValue() : null );
        String familyName   = ((attr = family.getAttribute("name"))     != null ? attr.getValue() : null );
        String mfg   = ((attr = family.getAttribute("mfg"))     != null ? attr.getValue() : null );
        String mfgID   = mfgIdFromName(mfg);

        List<Element> l = family.getChildren("model");
        if (log.isDebugEnabled()) log.debug("readFamily sees "+l.size()+" children");
        Element modelElement;
        if (l.size()<=0) {
            log.error("Did not find at least one model in the "+familyName+" family");
            modelElement = null;
        } else {
            modelElement = l.get(0);
        }

        // Record the family as a specific model, which allows you to select the
        // family as a possible thing to program
        DecoderFile vFamilyDecoderFile
            = new DecoderFile( mfg, mfgID, familyName,
                               parentLowVersID, parentHighVersID,
                               familyName,
                               filename,
                               -1, -1, modelElement); // numFns, numOuts, XML element equal
                                            // to the first decoder
        decoderList.add(vFamilyDecoderFile);

        // record each of the decoders
        for (int i=0; i<l.size(); i++) {
            // handle each entry by creating a DecoderFile object containing all it knows
            Element decoder = l.get(i);
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
            List<Element> vcodes = decoder.getChildren("versionCV");
            for (int j=0; j<vcodes.size(); j++) {
                // for each versionCV element
                Element vcv = vcodes.get(j);
                String vLoVersID = ( (attr = vcv.getAttribute("lowVersionID")) != null ? attr.getValue() : loVersID);
                String vHiVersID = ( (attr = vcv.getAttribute("highVersionID"))!= null ? attr.getValue() : hiVersID);
                df.setVersionRange(vLoVersID, vHiVersID);
            }
        }
    }

    public void writeFile(String name, DecoderIndexFile oldIndex, String files[]) throws java.io.IOException {
        if (log.isDebugEnabled()) log.debug("writeFile "+name);
        // This is taken in large part from "Java and XML" page 368
        File file = new File(prefsDir()+name);

        // create root element and document
        Element root = new Element("decoderIndex-config");
        Document doc = newDocument(root, dtdLocation+"decoderIndex-config.dtd");

        // add XSLT processing instruction
        // <?xml-stylesheet type="text/xsl" href="XSLT/DecoderID.xsl"?>
        java.util.Map<String,String> m = new java.util.HashMap<String,String>();
        m.put("type", "text/xsl");
        m.put("href", xsltLocation+"DecoderID.xsl");
        ProcessingInstruction p = new ProcessingInstruction("xml-stylesheet", m);
        doc.addContent(0,p);
        
        // add top-level elements
        Element index;
        root.addContent(index = new Element("decoderIndex"));
        index.setAttribute("version", Integer.toString(fileVersion));
        log.debug("version written to file as "+fileVersion);

        // add mfg list from existing DecoderIndexFile item
        Element mfgList = new Element("mfgList");
        // copy dates from original mfgList element
        if (oldIndex.nmraListDate!=null)
            mfgList.setAttribute("nmraListDate", oldIndex.nmraListDate);
        if (oldIndex.updated!=null)
            mfgList.setAttribute("updated", oldIndex.updated);
        
        // We treat "NMRA" special...
        Element mfg = new Element("manufacturer");
        mfg.setAttribute("mfg","NMRA");
        mfg.setAttribute("mfgID","999");
        mfgList.addContent(mfg);
        // start working on the rest of the entries
        Enumeration<String> keys = oldIndex._mfgIdFromNameHash.keys();
        List<String> l = new ArrayList<String>();
        while (keys.hasMoreElements()) {
            l.add(keys.nextElement());
        }
        Object[] s = l.toArray();
        // all of the above mess was to get something we can sort into alpha order
        jmri.util.StringUtil.sort(s);
        for (int i=0; i<s.length; i++) {
            String mfgName = (String)s[i];
            if (!mfgName.equals("NMRA")){
                mfg = new Element("manufacturer");
                mfg.setAttribute("mfg",mfgName);
                mfg.setAttribute("mfgID", oldIndex._mfgIdFromNameHash.get(mfgName));
                mfgList.addContent(mfg);
            }
        }

        // add family list by scanning files
        Element familyList = new Element("familyList");
        for (int i=0; i<files.length; i++) {
            DecoderFile d = new DecoderFile();
            try {
                Element droot = d.rootFromName(DecoderFile.fileLocation+files[i]);
                Element family = (Element)droot.getChild("decoder").getChild("family").clone();
                family.setAttribute("file",files[i]);
                familyList.addContent(family);
            }
            catch (org.jdom.JDOMException exj) {log.error("could not parse "+files[i]+": "+exj.getMessage());}
            catch (java.io.FileNotFoundException exj) {log.error("could not read "+files[i]+": "+exj.getMessage());}
        }

        index.addContent(mfgList);
        index.addContent(familyList);

        writeXML(file, doc);

        // force a read of the new file next time
        _instance = null;
    }

    String nmraListDate = null;
    String updated = null;

    /**
     * Return the filename String for the default decoder index file, including location.
     * This is here to allow easy override in tests.
     */
    protected static String defaultDecoderIndexFilename() { return decoderIndexFileName;}

    static final protected String decoderIndexFileName = "decoderIndex.xml";
    // initialize logging
    static private org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DecoderIndexFile.class.getName());

}
