// Roster.java

package jmri.jmrit.roster;

import jmri.jmrit.XmlFile;
import java.io.File;

import javax.swing.*;

import java.util.*;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.ProcessingInstruction;

/**
 * Roster manages and manipulates a roster of locomotives.
 * <P>
 * It works
 * with the "roster-config" XML DTD to load and store its information.
 *<P>
 * This is an in-memory representation of the roster xml file (see below
 * for constants defining name and location).  As such, this class is
 * also responsible for the "dirty bit" handling to ensure it gets
 * written.  As a temporary reliability enhancement, all changes to
 * this structure are now being written to a backup file, and a copy
 * is made when the file is opened.
 *<P>
 * Multiple Roster objects don't make sense, so we use an "instance" member
 * to navigate to a single one.
 *<P>
 * The only bound property is the list of RosterEntrys; a PropertyChangedEvent
 * is fired every time that changes.
 * <P>
 * The entries are stored in an ArrayList, sorted alphabetically.  That
 * sort is done manually each time an entry is added.
 * <p>
 * This predates the "XmlFile" base class, so doesn't use it.  Not sure
 * whether it should...
 * <P>
 * The roster is stored in a "Roster Index", which can be read or written.
 * Each individual entry (once stored) contains a filename which can
 * be used to retreive the locomotive information for that roster entry.
 * Note that the RosterEntry information is duplicated in both the Roster
 * (stored in the roster.xml file) and in the specific file for the entry.
 *
 * @author	Bob Jacobsen   Copyright (C) 2001, 2008
 * @author  Dennis Miller Copyright 2004
 * @version	$Revision: 1.45 $
 * @see         jmri.jmrit.roster.RosterEntry
 */
public class Roster extends XmlFile {

    /** record the single instance of Roster **/
    protected static Roster _instance = null;
    


    public synchronized static void resetInstance() { 
        _instance = null; 
    }

    /**
     * Locate the single instance of Roster, loading it if need be.
     * @return The valid Roster object
     */
    public static synchronized Roster instance() {
        if (_instance == null) {
            if (log.isDebugEnabled()) log.debug("Roster creating instance");
            // create and load
            _instance = new Roster();
            try {
                //_instance._rosterGroupList.add("Global");
                _instance.readFile(defaultRosterFilename());
            } catch (Exception e) {
                log.error("Exception during roster reading: "+e);
            }
        }
        if (log.isDebugEnabled()) log.debug("Roster returns instance "+_instance);
        return _instance;
    }

    /**
     * Add a RosterEntry object to the in-memory Roster.
     * @param e Entry to add
     */
    public void addEntry(RosterEntry e) {
        if (log.isDebugEnabled()) log.debug("Add entry "+e);
        int i = _list.size()-1;// Last valid index
        while (i>=0) {
            // compareToIgnoreCase not present in Java 1.1.8
            if (e.getId().toUpperCase().compareTo(_list.get(i).getId().toUpperCase()) > 0 )
                break; // I can never remember whether I want break or continue here
            i--;
        }
        _list.add(i+1, e);
        if(_rostergroup!=null)
            e.putAttribute(getRosterGroupWP(), "yes");
        setDirty(true);
        firePropertyChange("add", null, e);
    }

    /**
     * Remove a RosterEntry object from the in-memory Roster.  This
     * does not delete the file for the RosterEntry!
     * @param e Entry to remove
     */
    public void removeEntry(RosterEntry e) {
        if (log.isDebugEnabled()) log.debug("Remove entry "+e);
        if(_rostergroup!=null){
            e.deleteAttribute(getRosterGroupWP());
            e.updateFile();
        }
        else
            _list.remove(_list.indexOf(e));
        setDirty(true);
        firePropertyChange("remove", null, e);
    }

    /**
     * @return Number of entries in the Roster.
     */
    public int numEntries() { return _list.size(); }

    /**
     * Return a JComboBox containing the entire roster, if a roster group
     * has been selected, it will only return the entries relating to that 
     * group.
     * <P>
     * The JComboBox is based on a common model object, so it updates
     * when the roster changes.
     *
     */
    public JComboBox fullRosterComboBox() {
        return matchingComboBox(null, null, null, null, null, null, null);
    }
    
    /**
     * Return a JComboBox containing the entire roster, regardless of what
     * roster group has been selected.
     * <P>
     * The JComboBox is based on a common model object, so it updates
     * when the roster changes.
     *
     */
    
    public JComboBox fullRosterComboBoxGlobal() {
        return matchingComboBoxGlobal(null, null, null, null, null, null, null);
    }

    /**
     * Get a JComboBox representing the choices that match some information.
     * If a roster group has been selected then the results will be restricted
     * to those in the roster group.
     * <P>
     * The JComboBox is based on a common model object, so it updates
     * when the roster changes.
     */
    public JComboBox matchingComboBox(String roadName, String roadNumber, String dccAddress,
                                      String mfg, String decoderMfgID, String decoderVersionID, String id ) {
        List<RosterEntry> l = matchingList(roadName, roadNumber, dccAddress, mfg, decoderMfgID, decoderVersionID, id );
        JComboBox b = new JComboBox();
        for (int i = 0; i < l.size(); i++) {
            RosterEntry r = _list.get(i);
            if(_rostergroup!=null){
                if(r.getAttribute(getRosterGroupWP())!=null){
                    if(r.getAttribute(getRosterGroupWP()).equals("yes"))
                        b.addItem(r.titleString());
                }
            }
            else
                b.addItem(r.titleString());
        }
        return b;
    }
    /**
     * Get a JComboBox representing the choices that match some information
     * regardless of which roster group has been selected.
     * <P>
     * The JComboBox is based on a common model object, so it updates
     * when the roster changes.
     */
    public JComboBox matchingComboBoxGlobal(String roadName, String roadNumber, String dccAddress,
                                      String mfg, String decoderMfgID, String decoderVersionID, String id ) {
        List<RosterEntry> l = matchingList(roadName, roadNumber, dccAddress, mfg, decoderMfgID, decoderVersionID, id );
        JComboBox b = new JComboBox();
        for (int i = 0; i < l.size(); i++) {
            RosterEntry r = _list.get(i);
            b.addItem(r.titleString());
        }
        return b;
    }
    
    public void updateComboBox(JComboBox box) {
        List<RosterEntry> l = matchingList(null, null, null, null, null, null, null );
        box.removeAllItems();
        for (int i = 0; i < l.size(); i++) {
            RosterEntry r = _list.get(i);
            if(_rostergroup!=null){
                if(r.getAttribute(getRosterGroupWP())!=null){
                    if(r.getAttribute(getRosterGroupWP()).equals("yes"))
                        box.addItem(r.titleString());
                }
            }
            else
                box.addItem(r.titleString());
        }
    }
    
    public void updateComboBoxGlobal(JComboBox box) {
        List<RosterEntry> l = matchingList(null, null, null, null, null, null, null );
        box.removeAllItems();
        for (int i = 0; i < l.size(); i++) {
            RosterEntry r = _list.get(i);
            box.addItem(r.titleString());
        }
    }

    /**
     * Return RosterEntry from a "title" string, ala selection in matchingComboBox.
     */
    public RosterEntry entryFromTitle(String title ) {
        for (int i = 0; i < numEntries(); i++) {
            RosterEntry r = _list.get(i);
            if (r.titleString().equals(title)) return r;
        }
        return null;
    }
    
    /**
     * Return a specific entry by index
     */
    public RosterEntry getEntry(int i ) {
        return _list.get(i);
    }

    /**
     * Return filename from a "title" string, ala selection in matchingComboBox.
     * @return The filename matching this "title", or null if none exists
     */
    public String fileFromTitle(String title ) {
        RosterEntry r = entryFromTitle(title);
        if (r != null) return r.getFileName();
        return null;
    }

    public List<RosterEntry> getEntriesWithAttributeKey(String key) {
        // slow but effective algorithm
        ArrayList<RosterEntry> result = new ArrayList<RosterEntry>();
        java.util.Iterator<RosterEntry> i = _list.iterator();
        while (i.hasNext()) {
            RosterEntry r = i.next();
            if (r.getAttribute(key)!=null) result.add(r);
        }
        return result;
    }

   public List<RosterEntry> getEntriesWithAttributeKeyValue(String key, String value) {
        // slow but effective algorithm
        ArrayList<RosterEntry> result = new ArrayList<RosterEntry>();
        java.util.Iterator<RosterEntry> i = _list.iterator();
        while (i.hasNext()) {
            RosterEntry r = i.next();
            String v = r.getAttribute(key);
            if ( v!=null && v.equals(value)) 
                result.add(r);
        }
        return result;
   }
   
   public Set<String> getAllAttributeKeys() {
        // slow but effective algorithm
        Set<String> result = new TreeSet<String>();
        java.util.Iterator<RosterEntry> i = _list.iterator();
        while (i.hasNext()) {
            RosterEntry r = i.next();
            Set<String> s = r.getAttributes();
            if (s!=null) result.addAll(s);
        }
        return result;
   }
   
    /**
     * List of contained {@link RosterEntry} elements.
     */
    protected List<RosterEntry> _list = new ArrayList<RosterEntry>();

    /**
     *	Get a List of {@link RosterEntry} objects in Roster matching some information. 
     * The list may have
     *  null contents if there are no matches.
     */
    public List<RosterEntry> matchingList(String roadName, String roadNumber, String dccAddress,
                             String mfg, String decoderMfgID, String decoderVersionID, String id ) {
        List<RosterEntry> l = new ArrayList<RosterEntry>();
        for (int i = 0; i < numEntries(); i++) {
            if ( checkEntry(i, roadName, roadNumber, dccAddress, mfg, decoderMfgID, decoderVersionID, id ))
                l.add(_list.get(i));
        }
        return l;
    }

    /**
     * Check if an entry is consistent with specific properties. 
     *<P>
     * A null String entry
     * always matches. Strings are used for convenience in GUI building.
     *
     */
    public boolean checkEntry(int i, String roadName, String roadNumber, String dccAddress,
                              String mfg, String decoderModel, String decoderFamily,
                              String id ) {
        RosterEntry r = _list.get(i);
        if (id != null && !id.equals(r.getId())) return false;
        if (roadName != null && !roadName.equals(r.getRoadName())) return false;
        if (roadNumber != null && !roadNumber.equals(r.getRoadNumber())) return false;
        if (dccAddress != null && !dccAddress.equals(r.getDccAddress())) return false;
        if (mfg != null && !mfg.equals(r.getMfg())) return false;
        if (decoderModel != null && !decoderModel.equals(r.getDecoderModel())) return false;
        if (decoderFamily != null && !decoderFamily.equals(r.getDecoderFamily())) return false;
        return true;
    }

    /**
     * Write the entire roster to a file. This does not do backup; that has
     * to be done separately. See writeRosterFile() for a public function that
     * finds the default location, does a backup and then calls this.
     * @param name Filename for new file, including path info as needed.
     * @throws IOException
     */
    void writeFile(String name) throws java.io.FileNotFoundException, java.io.IOException {
        if (log.isDebugEnabled()) log.debug("writeFile "+name);
        // This is taken in large part from "Java and XML" page 368
        File file = findFile(name);
        if (file == null) {
            file = new File(name);
        }
        // create root element
        Element root = new Element("roster-config");
        Document doc = newDocument(root, dtdLocation+"roster-config.dtd");

        // add XSLT processing instruction
        // <?xml-stylesheet type="text/xsl" href="XSLT/roster.xsl"?>
        java.util.Map<String, String> m = new java.util.HashMap<String, String>();
        m.put("type", "text/xsl");
        m.put("href", xsltLocation+"roster.xsl");
        ProcessingInstruction p = new ProcessingInstruction("xml-stylesheet", m);
        doc.addContent(0,p);
        
        //Check the Comment and Decoder Comment fields for line breaks and
        //convert them to a processor directive for storage in XML
        //Note: this is also done in the LocoFile.java class to do
        //the same thing in the indidvidual locomotive roster files
        //Note: these changes have to be undone after writing the file
        //since the memory version of the roster is being changed to the
        //file version for writing
        for (int i=0; i<numEntries(); i++){

            //Extract the RosterEntry at this index and inspect the Comment and
            //Decoder Comment fields to change any \n characters to <?p?> processor
            //directives so they can be stored in the xml file and converted
            //back when the file is read.
            RosterEntry r = _list.get(i);
            String tempComment = r.getComment();
            String xmlComment = new String();

            //transfer tempComment to xmlComment one character at a time, except
            //when \n is found.  In that case, insert <?p?>
            for (int k = 0; k < tempComment.length(); k++) {
                if (tempComment.startsWith("\n", k)) {
                    xmlComment = xmlComment + "<?p?>";
                }
                else {
                    xmlComment = xmlComment + tempComment.substring(k, k + 1);
                }
            }
            r.setComment(xmlComment);

            //Now do the same thing for the decoderComment field
            String tempDecoderComment = r.getDecoderComment();
            String xmlDecoderComment = new String();

            for (int k = 0; k < tempDecoderComment.length(); k++) {
                if (tempDecoderComment.startsWith("\n", k)) {
                    xmlDecoderComment = xmlDecoderComment + "<?p?>";
                }
                else {
                    xmlDecoderComment = xmlDecoderComment +
                        tempDecoderComment.substring(k, k + 1);
                }
            }
            r.setDecoderComment(xmlDecoderComment);

        }
        //All Comments and Decoder Comment line feeds have been changed to processor directives


        // add top-level elements
        Element values;
        root.addContent(values = new Element("roster"));
        // add entries
        for (int i=0; i<numEntries(); i++) {
            values.addContent(_list.get(i).store());
        }
        
        if(_rosterGroupList.size()>=1){
            Element rosterGroup = new Element("rosterGroup");
            for (int i=0; i<_rosterGroupList.size(); i++){
                Element group = new Element("group");
                if(!_rosterGroupList.get(i).toString().equals("Global")){
                    group.addContent(_rosterGroupList.get(i).toString());
                    rosterGroup.addContent(group);
                }
            }
            root.addContent(rosterGroup);
        }
        writeXML(file, doc);

        //Now that the roster has been rewritten in file form we need to
        //restore the RosterEntry object to its normal \n state for the
        //Comment and Decoder comment fields, otherwise it can cause problems in
        //other parts of the program (e.g. in copying a roster)
        for (int i=0; i<numEntries(); i++){
            RosterEntry r = _list.get(i);
            String xmlComment = r.getComment();
            String tempComment = new String();

            for (int k = 0; k < xmlComment.length(); k++) {
                if (xmlComment.startsWith("<?p?>", k)) {
                    tempComment = tempComment + "\n";
                    k = k + 4;
                }
                else {
                    tempComment = tempComment + xmlComment.substring(k, k + 1);
                }
            }
            r.setComment(tempComment);

            String xmlDecoderComment = r.getDecoderComment();
            String tempDecoderComment = new String();

            for (int k = 0; k < xmlDecoderComment.length(); k++) {
                if (xmlDecoderComment.startsWith("<?p?>", k)) {
                    tempDecoderComment = tempDecoderComment + "\n";
                    k = k + 4;
                }
                else {
                    tempDecoderComment = tempDecoderComment +
                        xmlDecoderComment.substring(k, k + 1);
                }
            }
            r.setDecoderComment(tempDecoderComment);

        }

        // done - roster now stored, so can't be dirty
        setDirty(false);
    }

    /**
     * Name a valid roster entry filename from an entry name.
     * <p>
     * <ul>
     * <li>Replaces all problematic characters with "_".
     * <li>Append .xml suffix
     * </ul>
     * Does not check for duplicates.
     * @throws IllegalArgumentException if called with null or empty entry name
     * @param entry the getId() entry name from the RosterEntry
     * @see RosterEntry#ensureFilenameExists()
     * @since 2.1.5
     */
    static public String makeValidFilename(String entry) {
        if (entry==null) throw new IllegalArgumentException("makeValidFilename requires non-null argument");
        if (entry.equals("")) throw new IllegalArgumentException("makeValidFilename requires non-empty argument");

        // name sure there are no bogus chars in name        
        String cleanName = entry.replaceAll("[\\W]","_");  // remove \W, all non-word (a-zA-Z0-9_) characters

        // ensure suffix
        return cleanName+".xml";
    }
    
    /**
     * Read the contents of a roster XML file into this object. 
     * <P>
     * Note that this does not
     * clear any existing entries.
     */
    @SuppressWarnings("unchecked")
	void readFile(String name) throws org.jdom.JDOMException, java.io.IOException {
        // find root
        Element root = rootFromName(name);
        if (root==null) {
            log.warn("roster file could not be read; this is normal if you haven't put decoders in your roster yet");
            return;
        }
        if (log.isDebugEnabled()) XmlFile.dumpElement(root);

        // decode type, invoke proper processing routine if a decoder file
        if (root.getChild("roster") != null) {
            List<Element> l = root.getChild("roster").getChildren("locomotive");
            if (log.isDebugEnabled()) log.debug("readFile sees "+l.size()+" children");
            for (int i=0; i<l.size(); i++) {
                addEntry(new RosterEntry(l.get(i)));
            }

            //Scan the object to check the Comment and Decoder Comment fields for
            //any <?p?> processor directives and change them to back \n characters
            for (int i = 0; i < numEntries(); i++) {
                //Get a RosterEntry object for this index
                RosterEntry r = _list.get(i);

                //Extract the Comment field and create a new string for output
                String tempComment = r.getComment();
                String xmlComment = new String();

                //transfer tempComment to xmlComment one character at a time, except
                //when <?p?> is found.  In that case, insert a \n and skip over those
                //characters in tempComment.
                for (int k = 0; k < tempComment.length(); k++) {
                    if (tempComment.startsWith("<?p?>", k)) {
                        xmlComment = xmlComment + "\n";
                        k = k + 4;
                    }
                    else {
                        xmlComment = xmlComment + tempComment.substring(k, k + 1);
                    }
                }
                r.setComment(xmlComment);

                //Now do the same thing for the decoderComment field
                String tempDecoderComment = r.getDecoderComment();
                String xmlDecoderComment = new String();

                for (int k = 0; k < tempDecoderComment.length(); k++) {
                    if (tempDecoderComment.startsWith("<?p?>", k)) {
                        xmlDecoderComment = xmlDecoderComment + "\n";
                        k = k + 4;
                    }
                    else {
                        xmlDecoderComment = xmlDecoderComment +
                            tempDecoderComment.substring(k, k + 1);
                    }
                }

                r.setDecoderComment(xmlDecoderComment);
            }
        }
        
        else {
            log.error("Unrecognized roster file contents in file: "+name);
        }
        if (root.getChild("rosterGroup") != null) {
            List<Element> g = root.getChild("rosterGroup").getChildren("group");
//            if (g.size()>=1){
                for (int i=0; i<g.size(); i++) {
                    addRosterGroupList(g.get(i).getText().toString());
                }
//            }
        }
    }

    private boolean dirty = false;
    void setDirty(boolean b) {dirty = b;}
    boolean isDirty() {return dirty;}

    public void dispose() {
        if (log.isDebugEnabled()) log.debug("dispose");
        if (dirty) log.error("Dispose invoked on dirty Roster");
    }

    /**
     * Store the roster in the default place, including making a backup if needed.
     * <p>
     * Uses writeFile(String), a protected method that can write to
     * a specific location.
     */
    public static void writeRosterFile() {
        Roster.instance().makeBackupFile(defaultRosterFilename());
        try {
            Roster.instance().writeFile(defaultRosterFilename());
        } catch (Exception e) {
            log.error("Exception while writing the new roster file, may not be complete: "+e);
        }
    }

    /**
     * Update the in-memory Roster to be consistent with
     * the current roster file.  This removes any existing roster entries!
     */
    public void reloadRosterFile() {
        // clear existing
        _list.clear();
        // and read new
        try {
            _instance.readFile(defaultRosterFilename());
        } catch (Exception e) {
            log.error("Exception during roster reading: "+e);
        }
    }


    /**
     * Return the filename String for the default roster file, including location.
     * This is here to allow easy override in tests.
     */
    public static String defaultRosterFilename() { return getFileLocation()+rosterFileName;}

    /**
     * Set the default location for the Roster file, and all
     * individual locomotive files.
     *
     * @param f Absolute pathname to use. A null or "" argument flags
     * a return to the original default in prefsdir.
     */
    public static void setFileLocation(String f) {
        if (f!=null && !f.equals("")) {
            fileLocation = f;
            if (f.endsWith(File.separator))
                LocoFile.setFileLocation(f+"roster");
            else
                LocoFile.setFileLocation(f+File.separator+"roster");
        } else {
            if (log.isDebugEnabled()) log.debug("Roster location reset to default");
            fileLocation = XmlFile.prefsDir();
            LocoFile.setFileLocation(XmlFile.prefsDir()+File.separator+"roster"+File.separator);
        }
        // and make sure next request gets the new one
        resetInstance();
    }

    /**
     * Absolute path to roster file location.
     * <P>
     * Default is in the prefsDir, but can be set to anything.
     * @see XmlFile#prefsDir()
     */
    public static String getFileLocation() { return fileLocation; }
    private static String fileLocation  = XmlFile.prefsDir();

    public static void setRosterGroupName(String name) { rosterFileName = name; }
    private static String rosterFileName = "roster.xml";

    // since we can't do a "super(this)" in the ctor to inherit from PropertyChangeSupport, we'll
    // reflect to it.
    // Note that dispose() doesn't act on these.  Its not clear whether it should...
    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);

    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    protected void firePropertyChange(String p, Object old, Object n) {
        pcs.firePropertyChange(p,old,n);
    }

    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    /**
     * Notify that the ID of an entry has changed.  This doesn't actually change the
     * Roster per se, but triggers recreation.
     */
    public void entryIdChanged(RosterEntry r) {
        log.debug("EntryIdChanged");

        // order may be wrong! Sort
        RosterEntry[] rarray = new RosterEntry[_list.size()];
        for (int i=0; i<rarray.length; i++) rarray[i] =_list.get(i);
        jmri.util.StringUtil.sortUpperCase(rarray);
        for (int i=0; i<rarray.length; i++) _list.set(i,rarray[i]);

        firePropertyChange("change", null, r);
    }
    
        
    protected static String _rostergroup = null;
    
    public static void setRosterGroup(String group){
        if (group==null) _rostergroup=null;
        else if (group.equals("Global"))
            _rostergroup=null;
        else
            _rostergroup = group;
    }
    
    public static String getRosterGroup(){
        return _rostergroup;
    }
    
    public static String getRosterGroupWP(){
        String group = _rosterGroupPrefix+_rostergroup;
        return group;
    }

    
    protected ArrayList<String> _rosterGroupList = new ArrayList<String>();
    
    protected static String _rosterGroupPrefix = "RosterGroup:";
    
    public String getRosterGroupPrefix(){ return _rosterGroupPrefix; }
    
    public void addRosterGroupList(String str){
        for(int i=0; i<_rosterGroupList.size(); i++){
            if (_rosterGroupList.get(i).toString().equals(str))
                return;
        }
        _rosterGroupList.add(str);
        writeRosterFile();
    }
    
    public void delRosterGroupList(String str) {
        _rosterGroupList.remove(str);
        str=_rosterGroupPrefix+str;
        List<RosterEntry> groupentries = getEntriesWithAttributeKey(str);
        for(int i=0; i<groupentries.size();i++){
            groupentries.get(i).deleteAttribute(str);
        }
    }
    
    public void getRosterGroupList(int i) {
        _rosterGroupList.get(i);
    }
    
    public JComboBox rosterGroupBox() {
        JComboBox b = new JComboBox();
        for (int i = 0; i < _rosterGroupList.size(); i++) {
            b.addItem(_rosterGroupList.get(i));
        }
        return b;
    }
    
    public void updateGroupBox(JComboBox box){
        box.removeAllItems();
        for (int i = 0; i < _rosterGroupList.size(); i++) {
            box.addItem(_rosterGroupList.get(i));
        }
    }
    
    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Roster.class.getName());

}
