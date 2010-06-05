package jmri.jmrit.throttle;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;

import org.jdom.Document;
import org.jdom.Element;

import jmri.jmrit.XmlFile;

public class ThrottlesPreferences {
    private boolean _useExThrottle = true;	
    private boolean _1Win4all = true;
    private boolean _resizeWinImg = false;
    private boolean _useAdvTransition = false;
    private boolean _useRosterImage = true;
    private boolean _useTransparentCtl = false;
    private boolean _enableRosterSearch = true;
    private boolean _enableAutoLoad = true;
    private boolean _hideUndefinedFunButton = false;
    private boolean _ignoreThrottlePosition = true;
    private boolean _cleaningOnClose = false;
    private Dimension _winDim = new Dimension(800,600);
    private String prefFile;
	private ArrayList<PropertyChangeListener> listeners;
    
    public ThrottlesPreferences(String sfile)
    {
    	prefFile = sfile;
		ThrottlesPrefsXml prefs = new ThrottlesPrefsXml();
		File file = new File(prefFile );
   		Element root;
		try {
			root = prefs.rootFromFile(file);
        } catch (java.io.FileNotFoundException e2) {
            log.info("Did not find throttle preferences file.  This is normal if you haven't save the preferences before");
            root = null;
		} catch (Exception e) {
			log.error("Exception while loading throttles preferences: " + e);
			root = null;
		}
		if (root != null)
			load(root.getChild("throttlesPreferences"));
    }
    
    public ThrottlesPreferences() {   }
    
    public void load(org.jdom.Element e)
    {
    	org.jdom.Attribute a;
    	org.jdom.Attribute b;
    	if ((a = e.getAttribute("isUsingExThrottle")) != null )  setUseExThrottle( a.getValue().compareTo("true") == 0 );
    	if ((a = e.getAttribute("isOneWindowForAll")) != null )  setOneWindowForAll( a.getValue().compareTo("true") == 0 );
    	if ((a = e.getAttribute("isResizingWindow")) != null )  setResizeWindow( a.getValue().compareTo("true") == 0 );
    	if (((a = e.getAttribute("windowDimensionWidth")) != null ) && ((b = e.getAttribute("windowDimensionHeight")) != null ))
    		setWindowDimension( new Dimension ( new Integer(a.getValue()),  new Integer( b.getValue()) ));
    	if ((a = e.getAttribute("isUsingAdvTransition")) != null )  setUseAdvTransition( a.getValue().compareTo("true") == 0 );
    	if ((a = e.getAttribute("isUsingRosterImage")) != null )  setUseRosterImage( a.getValue().compareTo("true") == 0 );
    	if ((a = e.getAttribute("isUsingTransparentCtl")) != null )  setUseTransparentCtl( a.getValue().compareTo("true") == 0 );
    	if ((a = e.getAttribute("isEnablingRosterSearch")) != null )  setEnableRosterSearch( a.getValue().compareTo("true") == 0 );
    	if ((a = e.getAttribute("isAutoLoading")) != null )  setAutoLoad( a.getValue().compareTo("true") == 0 );
    	if ((a = e.getAttribute("isHidingUndefinedFunctionButtons")) != null )  setHideUndefinedFuncButt( a.getValue().compareTo("true") == 0 );
    	if ((a = e.getAttribute("isIgnoringThrottlePosition")) != null )  setIgnoreThrottlePosition( a.getValue().compareTo("true") == 0 );
    	if ((a = e.getAttribute("isCleaningOnClose")) != null )  setCleanOnClose( a.getValue().compareTo("true") == 0 );

    }

	/**
	 * An extension of the abstract XmlFile. No changes made to that class.
	 * 
	 */
	class ThrottlesPrefsXml extends XmlFile {

	}
	
    private org.jdom.Element store() {
    	org.jdom.Element e = new org.jdom.Element("throttlesPreferences");
    	e.setAttribute("isUsingExThrottle", ""+isUsingExThrottle());
    	e.setAttribute("isOneWindowForAll", ""+isOneWindowForAll() );
    	e.setAttribute("isResizingWindow", ""+isResizingWindow() );
    	e.setAttribute("windowDimensionWidth", ""+(int)getWindowDimension().getWidth() );
    	e.setAttribute("windowDimensionHeight", ""+(int)getWindowDimension().getHeight() );
    	e.setAttribute("isUsingAdvTransition", ""+isUsingAdvTransition());
    	e.setAttribute("isUsingRosterImage", ""+isUsingRosterImage());
    	e.setAttribute("isUsingTransparentCtl", ""+isUsingTransparentCtl());
    	e.setAttribute("isEnablingRosterSearch", ""+isEnablingRosterSearch());
    	e.setAttribute("isAutoLoading", ""+isAutoLoading());
    	e.setAttribute("isHidingUndefinedFunctionButtons", ""+isHidingUndefinedFuncButt());
    	e.setAttribute("isIgnoringThrottlePosition", ""+isIgnoringThrottlePosition());
    	e.setAttribute("isCleaningOnClose", ""+isCleaningOnClose());

    	return e;
    }

	public void set(ThrottlesPreferences tp)
    {
    	setWindowDimension (tp.getWindowDimension() );
    	setUseExThrottle (tp.isUsingExThrottle() );
    	setOneWindowForAll(tp.isOneWindowForAll() );
    	setResizeWindow(tp.isResizingWindow());
    	setUseAdvTransition(tp.isUsingAdvTransition());
    	setUseRosterImage(tp.isUsingRosterImage());
    	setUseTransparentCtl(tp.isUsingTransparentCtl());
    	setEnableRosterSearch(tp.isEnablingRosterSearch());
    	setAutoLoad(tp.isAutoLoading());
    	setHideUndefinedFuncButt(tp.isHidingUndefinedFuncButt());
    	setIgnoreThrottlePosition(tp.isIgnoringThrottlePosition());
    	setCleanOnClose(tp.isCleaningOnClose() );
    	
    	if (listeners != null)
    		for (int i = 0; i < listeners.size(); i++) {
    			PropertyChangeListener l = listeners.get(i);
    			PropertyChangeEvent e = new PropertyChangeEvent(this, "ThrottlePreferences", null, this );
    			l.propertyChange(e);
    		}
    }
    
    public boolean compareTo(ThrottlesPreferences tp)
    {
    	return( getWindowDimension() != tp.getWindowDimension() ||
    			isUsingExThrottle() != tp.isUsingExThrottle() ||
    			isOneWindowForAll() != tp.isOneWindowForAll() ||
    			isResizingWindow() != tp.isResizingWindow()||
    			isUsingAdvTransition() != tp.isUsingAdvTransition()||
    			isUsingRosterImage() != tp.isUsingRosterImage()||
    			isUsingTransparentCtl() != tp.isUsingTransparentCtl()||
    			isEnablingRosterSearch() != tp.isEnablingRosterSearch()||
    			isAutoLoading() != tp.isAutoLoading() ||
    			isHidingUndefinedFuncButt() != tp.isHidingUndefinedFuncButt() ||
    			isCleaningOnClose() != tp.isCleaningOnClose() );
    }
    
    public void save() {
    	if (prefFile == null)
    		return;
    	XmlFile xf = new XmlFile(){};   // odd syntax is due to XmlFile being abstract
    	xf.makeBackupFile(prefFile );
    	File file=new File(prefFile );
    	try {
    		//The file does not exist, create it before writing
    		File parentDir=file.getParentFile();
    		if(!parentDir.exists())
    			parentDir.mkdir();
    		file.createNewFile();
    	} catch (Exception exp) {
    		log.error("Exception while writing the new throttles preferences file, may not be complete: "+exp);
    	}

    	try {
    		Element root = new Element("throttles-preferences");
    		Document doc = XmlFile.newDocument(root, XmlFile.dtdLocation+"throttles-preferences.dtd");
    		// add XSLT processing instruction
    		// <?xml-stylesheet type="text/xsl" href="XSLT/throttle.xsl"?>
/*TODO    		java.util.Map<String,String> m = new java.util.HashMap<String,String>();
    		m.put("type", "text/xsl");
    		m.put("href", jmri.jmrit.XmlFile.xsltLocation+"throttles-preferences.xsl");
    		ProcessingInstruction p = new ProcessingInstruction("xml-stylesheet", m);
    		doc.addContent(0,p);*/
    		root.setContent( store() );
    		xf.writeXML(file, doc);
    	}       
    	catch (Exception ex){
    		log.warn("Exception in storing throttles preferences xml: "+ex);
    	}
    }
    
    public Dimension getWindowDimension() {
    	return _winDim ;
    }    
    public void setWindowDimension(Dimension d) {
    	_winDim = d;	
    }    
	public boolean isUsingExThrottle() { 
		return _useExThrottle;
	}
	public void setUseExThrottle(boolean exThrottle) {
		_useExThrottle = exThrottle; 
	}
	public boolean isOneWindowForAll() {
		return _1Win4all;
	}
	public void setOneWindowForAll(boolean win4all) {
		_1Win4all = win4all;
	}
	public boolean isResizingWindow() {
		return _resizeWinImg;
	}
	public void setResizeWindow(boolean winImg) {
		_resizeWinImg = winImg;
	}
	public boolean isUsingAdvTransition() {
		return _useAdvTransition;
	}
	public void setUseAdvTransition(boolean advTransition) {
		_useAdvTransition = advTransition;
	}
	public boolean isUsingRosterImage() {
		return _useRosterImage;
	}
	public void setUseRosterImage(boolean rosterImage) {
		_useRosterImage = rosterImage;
	}
	public boolean isUsingTransparentCtl() {
		return _useTransparentCtl;
	}
	public void setUseTransparentCtl(boolean transparentCtl) {
		_useTransparentCtl = transparentCtl;
	}
    public boolean isEnablingRosterSearch() {
    	return _enableRosterSearch;
	}
	public void setEnableRosterSearch(boolean b) {
		_enableRosterSearch = b;		
	}
	public void setAutoLoad(boolean b) {
		_enableAutoLoad = b;		
	}    
	public boolean isAutoLoading() {
		return _enableAutoLoad;
	}
	public void setHideUndefinedFuncButt(boolean b) {
		_hideUndefinedFunButton = b;
	}
	public boolean isHidingUndefinedFuncButt() {
		return _hideUndefinedFunButton;
	}
	public void setIgnoreThrottlePosition(boolean b) {
		_ignoreThrottlePosition = b;		
	}    
	public boolean isIgnoringThrottlePosition() {
		return _ignoreThrottlePosition;
	}
	public void setCleanOnClose(boolean b) {
		_cleaningOnClose = b;
	}
	public boolean isCleaningOnClose() {
		return _cleaningOnClose;
	}

	
	/**
	 * Add an AddressListener. AddressListeners are notified when the user
	 * selects a new address and when a Throttle is acquired for that address
	 * 
	 * @param l
	 */
	public void addPropertyChangeListener(PropertyChangeListener l) {
		if (listeners == null)
			listeners = new ArrayList<PropertyChangeListener>(2);		
		if (!listeners.contains(l)) 
			listeners.add(l);
	}
	
	/**
	 * Remove an AddressListener. 
	 * 
	 * @param l
	 */
	public void removePropertyChangeListener(PropertyChangeListener l) {
		if (listeners == null) 
			return;
		if (listeners.contains(l)) 
			listeners.remove(l);		
	}
	
	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ThrottlesPreferences.class.getName());
}
