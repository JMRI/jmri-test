package jmri.util.jynstrument;

import javax.swing.JPanel;

public abstract class Jynstrument extends JPanel {	
	private Object mContext;		// Object being extended
	private String jythonFile;		// Name of the Jython file being run
	private String jynstrumentFolder;	// Folder where the script seats (to retrieve resources)
	private String className; // name of the JYnstrument class

	public Object getContext() {
		return mContext;
	}
	public void setContext(Object context) {
		mContext = context;
	}

	public String getJythonFile() {
		return jythonFile;
	}
	public void setJythonFile(String jythonFile) {
		this.jythonFile = jythonFile;
	}

	public String getFolder() {
		return jynstrumentFolder;
	}
	public void setFolder(String jynstrumentFolder) {
		this.jynstrumentFolder = jynstrumentFolder;
	}
	
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
		
	public boolean validateContext() {
		if (getExpectedContextClassName() == null || mContext == null)
			return false;
		try {
			return ( Class.forName( getExpectedContextClassName() ).isAssignableFrom(mContext.getClass()) ) ;
		} catch (ClassNotFoundException e) {
			log.error("Class "+getExpectedContextClassName()+" not found "+e);
			e.printStackTrace();
		}
		return false;
	}
	
	public abstract String getExpectedContextClassName();	
	public abstract void init();
	public abstract void quit();
	
	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Jynstrument.class.getName());
}
