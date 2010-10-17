// LocoFile.java

package jmri.jmrit.roster;

import jmri.jmrit.XmlFile;
import jmri.jmrit.symbolicprog.CvTableModel;
import jmri.jmrit.symbolicprog.IndexedCvTableModel;
import jmri.jmrit.symbolicprog.CvValue;
import jmri.jmrit.symbolicprog.VariableTableModel;
import java.io.File;

import java.util.List;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.ProcessingInstruction;

/**
 * Represents and manipulates a locomotive definition, both as a file and
 * in memory.  The interal storage is a JDOM tree. See locomotive-config.dtd
 * <P>
 * This class is intended for use by RosterEntry only; you should not use it
 * directly. That's why this is not a public class.
 *
 * @author    Bob Jacobsen     Copyright (C) 2001, 2002, 2008
 * @author    Dennis Miller    Copyright (C) 2004
 * @author    Howard G. Penny  Copyright (C) 2005
 * @version   $Revision: 1.36 $
 * @see       jmri.jmrit.roster.RosterEntry
 * @see       jmri.jmrit.roster.Roster
 */
class LocoFile extends XmlFile {

    /**
     * Convert to a cannonical text form for ComboBoxes, etc
     */
    public String titleString() {
        return "no title form yet";
    }

    /**
     * Load a CvTableModel from the locomotive element in the File
     * @param loco A JDOM Element containing the locomotive definition
     * @param cvModel  An existing CvTableModel object which will have
     *                 the CVs from the loco Element appended.  It is
     *                 intended, but not required, that this be empty.
     */
    @SuppressWarnings("unchecked")
	public static void loadCvModel(Element loco, CvTableModel cvModel, IndexedCvTableModel iCvModel){
        CvValue cvObject;
        // get the CVs and load
        Element values = loco.getChild("values");
        
        // Ugly hack because of bug 1898971 in JMRI 2.1.2 - contents may be directly inside the 
        // locomotive element, instead of in a nested values element
        if (values == null) {
            // check for non-nested content, in which case use loco element
            List<Element> elementList = loco.getChildren("CVvalue");
            if (elementList != null) values = loco;
        }
        
        if (values != null) {
            // get the CV values and load
            List<Element> elementList = values.getChildren("CVvalue");
            if (log.isDebugEnabled()) log.debug("Found "+elementList.size()+" CVvalues");

            for (int i=0; i<elementList.size(); i++) {
                // locate the row
                if ( ((elementList.get(i))).getAttribute("name") == null) {
                    if (log.isDebugEnabled()) log.debug("unexpected null in name "+((elementList.get(i)))+" "+((elementList.get(i))).getAttributes());
                    break;
                }
                if ( ((elementList.get(i))).getAttribute("value") == null) {
                    if (log.isDebugEnabled()) log.debug("unexpected null in value "+((elementList.get(i)))+" "+((elementList.get(i))).getAttributes());
                    break;
                }

                String name = ((elementList.get(i))).getAttribute("name").getValue();
                String value = ((elementList.get(i))).getAttribute("value").getValue();
                if (log.isDebugEnabled()) log.debug("CV: "+i+"th entry, CV number "+name+" has value: "+value);

                int cv = Integer.valueOf(name).intValue();
                cvObject = (cvModel.allCvVector().elementAt(cv));
                if (cvObject == null) {
                    log.warn("CV "+cv+" was in loco file, but not defined by the decoder definition");
                    cvModel.addCV(name, false, false, false);
                    cvObject = (cvModel.allCvVector().elementAt(cv));
                }
                cvObject.setValue(Integer.valueOf(value).intValue());
                cvObject.setState(CvValue.FROMFILE);
            }
            elementList = values.getChildren("indexedCVvalue");
            if (log.isDebugEnabled()) log.debug("Found "+elementList.size()+" indexedCVvalues");
            for (int i=0; i<elementList.size(); i++) {
                if ( ((elementList.get(i))).getAttribute("name") == null) {
                    if (log.isDebugEnabled()) log.debug("unexpected null in name "+((elementList.get(i)))+" "+((elementList.get(i))).getAttributes());
                    break;
                }
                if ( ((elementList.get(i))).getAttribute("value") == null) {
                    if (log.isDebugEnabled()) log.debug("unexpected null in value "+((elementList.get(i)))+" "+((elementList.get(i))).getAttributes());
                    break;
                }

                String name  = ((elementList.get(i))).getAttribute("name").getValue();
                int piCv  = Integer.valueOf(((elementList.get(i))).getAttribute("piCv").getValue()).intValue();
                int piVal = Integer.valueOf(((elementList.get(i))).getAttribute("piVal").getValue()).intValue();
                int siCv  = Integer.valueOf(((elementList.get(i))).getAttribute("siCv").getValue()).intValue();
                int siVal = Integer.valueOf(((elementList.get(i))).getAttribute("siVal").getValue()).intValue();
                int iCv   = Integer.valueOf(((elementList.get(i))).getAttribute("iCv").getValue()).intValue();
                String value = ((elementList.get(i))).getAttribute("value").getValue();
                if (log.isDebugEnabled()) log.debug("CV: "+i+"th entry, CV number "+name+" has value: "+value);

                // cvObject = (iCvModel.allIndxCvVector().elementAt(i));
                cvObject = iCvModel.getMatchingIndexedCV(name);
                if (log.isDebugEnabled())
                    log.debug("Matched name "+name+" with CV "+cvObject);
                    
                if (cvObject == null) {
                    log.warn("Indexed CV "+name+" was in loco file, but not defined by the decoder definition");
                    log.debug("attempt to add "+i+" "+name+" "+piCv+" "+piVal+" "+siCv+" "+siVal+" "+iCv);
                    iCvModel.addIndxCV(i, name, piCv, piVal, siCv, siVal, iCv, false, false, false);
                    cvObject = (iCvModel.allIndxCvVector().elementAt(i));
                }
                cvObject.setValue(Integer.valueOf(value).intValue());
                if ( cvObject.getInfoOnly() ) {
                    cvObject.setState(CvValue.READ);
                } else {
                    cvObject.setState(CvValue.FROMFILE);
                }
            }
        } else log.error("no values element found in config file; CVs not configured");

        // ugly hack - set CV17 back to fromFile if present
        // this is here because setting CV17, then CV18 seems to set
        // CV17 to Edited.  This needs to be understood & fixed.
        cvObject = (cvModel.allCvVector().elementAt(17));
        if (cvObject!=null) cvObject.setState(CvValue.FROMFILE);
    }

    /**
     * Write an XML version of this object, including also the RosterEntry
     * information, and memory-resident decoder contents.  
     * 
     * Does not do an automatic backup of the file, so that
     * should be done elsewhere.
     *
     * @param file Destination file. This file is overwritten if it exists.
     * @param cvModel provides the CV numbers and contents
     * @param iCvModel provides the Indexed CV numbers and contents
     * @param variableModel provides the variable names and contents
     * @param r  RosterEntry providing name, etc, information
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="SBSC_USE_STRINGBUFFER_CONCATENATION") 
    // Only used occasionally, so inefficient String processing not really a problem
    // though it would be good to fix it if you're working in this area
    public void writeFile(File file, CvTableModel cvModel, IndexedCvTableModel iCvModel, VariableTableModel variableModel, RosterEntry r) {
        if (log.isDebugEnabled()) log.debug("writeFile to "+file.getAbsolutePath()+" "+file.getName());
        try {
            // This is taken in large part from "Java and XML" page 368

            // create root element
            Element root = new Element("locomotive-config");
            Document doc = newDocument(root, dtdLocation+"locomotive-config.dtd");

            // add XSLT processing instruction
            // <?xml-stylesheet type="text/xsl" href="XSLT/locomotive.xsl"?>
            java.util.Map<String,String> m = new java.util.HashMap<String,String>();
            m.put("type", "text/xsl");
            m.put("href", xsltLocation+"locomotive.xsl");
            ProcessingInstruction p = new ProcessingInstruction("xml-stylesheet", m);
            doc.addContent(0,p);
        
            //Before adding the roster locomotive values, scan the Comment and
            //Decoder Comment fields to change any \n to a <?p?> processor directive.
            //Extract the Comment from the RosterEntry and transfer it one character
            //at a time to the xmlComment string.  If a \n is encountered, insert
            //<?p?> in the xmlComment string instead.  When adding the Attribute
            //to the xml file, use the new xmlComment string instead.  This
            //leaves the RosterEntry model unchanged, but records the values
            //correctly in the xml file
            //Note: an equivalent change also done in the Roster.java class to do the
            //same thing for the roster index file
            String tempComment =  r.getComment();
            String xmlComment = "";
            for (int k = 0; k < tempComment.length(); k++) {
                if (tempComment.startsWith("\n",k)){
                    xmlComment = xmlComment + "<?p?>";
                }
                else {
                    xmlComment = xmlComment + tempComment.substring(k,k+1);
                }
            }

            //Now do the same thing for the Decoder Comment field
            String tempDecoderComment =  r.getDecoderComment();
            String xmlDecoderComment = "";
            for (int k = 0; k < tempDecoderComment.length(); k++) {
                if (tempDecoderComment.startsWith("\n",k)){
                    xmlDecoderComment = xmlDecoderComment + "<?p?>";
                }
                else {
                    xmlDecoderComment = xmlDecoderComment + tempDecoderComment.substring(k,k+1);
                }
            }


            // add top-level elements
            Element locomotive = r.store();   // the locomotive element from the RosterEntry

            root.addContent(locomotive);
            Element values = new Element("values");
            locomotive.addContent(values);

            // Append a decoderDef element to values
            Element decoderDef;
            values.addContent(decoderDef = new Element("decoderDef"));
            // add the variable values to the decoderDef Element
            if (variableModel!=null) {
                for (int i = 0; i < variableModel.getRowCount(); i++) {
                    decoderDef.addContent(new Element("varValue")
                                          .setAttribute("item", variableModel.getLabel(i))
                                          .setAttribute("value", variableModel.getValString(i))
                        );
                }
                // mark file as OK
                variableModel.setFileDirty(false);
            }
            
            // add the CV values to the values Element
            if (cvModel!=null) {
                for (int i = 0; i < cvModel.getRowCount(); i++) {
                    values.addContent(new Element("CVvalue")
                                      .setAttribute("name", cvModel.getName(i))
                                      .setAttribute("value", cvModel.getValString(i))
                        );
                }
            }
            
            // add the Indexed CV values to the
            if (iCvModel!=null) {
                for (int i = 0; i < iCvModel.getRowCount(); i++) {
                    values.addContent(new Element("indexedCVvalue")
                                      .setAttribute("name", iCvModel.getName(i))
                                      .setAttribute("piCv", ""+(iCvModel.getCvByRow(i)).piCv())
                                      .setAttribute("piVal", ""+(iCvModel.getCvByRow(i)).piVal())
                                      .setAttribute("siCv", ""+(iCvModel.getCvByRow(i)).siCv())
                                      .setAttribute("siVal", ""+(iCvModel.getCvByRow(i)).siVal())
                                      .setAttribute("iCv", ""+(iCvModel.getCvByRow(i)).iCv())
                                      .setAttribute("value", iCvModel.getValString(i))
                        );
                }
            }
            
            writeXML(file, doc);

        }
        catch (java.io.IOException ex) {
            log.error("IOException", ex);
        }
    }

    /**
     * Write an XML version of this object from an existing XML tree, 
     * updating only the ID string. 
     * 
     * Does not do an automatic backup of the file, so that
     * should be done elsewhere. This is intended for copy and import
     * operations, where the tree has been read from an existing file.
     * Hence, only the "ID" information in the roster entry is updated.
     * Note that any multi-line comments are not changed here.  Any calling
     * class should ensure that they are in xml file format
     * with embedded <?p?> processor directives for line breaks.
     *
     * @param pFile Destination file. This file is overwritten if it exists.
     * @param pRootElement Root element of the JDOM tree to write.
     *                      This should be of type "locomotive-config", and
     *                      should not be in use elsewhere (clone it first!)
     * @param pEntry RosterEntry providing name, etc, information
     */
    public void writeFile(File pFile, Element pRootElement, RosterEntry pEntry) {
        if (log.isDebugEnabled()) log.debug("writeFile to "+pFile.getAbsolutePath()+" "+pFile.getName());
        try {
            // This is taken in large part from "Java and XML" page 368

            // create root element
            Document doc = newDocument(pRootElement, dtdLocation+"locomotive-config.dtd");

            // Update the locomotive.id element
            pRootElement.getChild("locomotive").getAttribute("id").setValue(pEntry.getId());

            writeXML(pFile, doc);
        }
        catch (Exception ex) {
            // need to trace this one back
            ex.printStackTrace();
        }
    }

    /**
     * Write an XML version of this object, updating the RosterEntry
     * information, from an existing XML tree.  
     * 
     * Does not do an automatic backup of the file, so that
     * should be done elsewhere. This is intended for 
     * writing out changes to the RosterEntry information only.
     *
     * @param pFile Destination file. This file is overwritten if it exists.
     * @param pRootElement Root element of the existing JDOM tree containing
     *                           the CV and variable contents
     * @param newLocomotive Element from RosterEntry providing name, etc, information
     */
    public void writeFile(File pFile, Element existingElement, Element newLocomotive) {
        if (log.isDebugEnabled()) log.debug("writeFile to "+pFile.getAbsolutePath()+" "+pFile.getName());
        try {
            // This is taken in large part from "Java and XML" page 368

            // create root element
            Element root = new Element("locomotive-config");
            Document doc = newDocument(root, dtdLocation+"locomotive-config.dtd");
            root.addContent(newLocomotive);
            
            // add XSLT processing instruction
            // <?xml-stylesheet type="text/xsl" href="XSLT/locomotive.xsl"?>
            java.util.Map<String,String> m = new java.util.HashMap<String,String>();
            m.put("type", "text/xsl");
            m.put("href", xsltLocation+"locomotive.xsl");
            ProcessingInstruction p = new ProcessingInstruction("xml-stylesheet", m);
            doc.addContent(0,p);

            // Add the variable info
            Element values = existingElement.getChild("locomotive").getChild("values");
            newLocomotive.addContent((Element)values.clone());
            
            writeXML(pFile, doc);
        }
        catch (Exception ex) {
            // need to trace this one back
            ex.printStackTrace();
        }
    }

    /**
     * Defines the preferences subdirectory in which LocoFiles are kept
     * by default.
     */
    static private String fileLocation = XmlFile.prefsDir()+File.separator+"roster"+File.separator;

    static public String getFileLocation() { return fileLocation; }

    static public void setFileLocation(String loc) {
        fileLocation = loc;
        if (!fileLocation.endsWith(File.separator))
            fileLocation = fileLocation+File.separator;
    }

    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LocoFile.class.getName());

}
