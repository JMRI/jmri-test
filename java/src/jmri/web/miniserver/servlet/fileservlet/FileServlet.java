package jmri.web.miniserver.servlet.fileservlet;

import java.io.*;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import jmri.web.miniserver.AbstractServlet;

/** 
 * A simple HTTP servlet that returns the contents of a file.
 * The MIME type is taken from a property file, as is
 * the path map.
 *<P>
 *  Parts taken from Core Web Programming from 
 *  Prentice Hall and Sun Microsystems Press,
 *  http://www.corewebprogramming.com/.
 *  &copy; 2001 Marty Hall and Larry Brown;
 *  may be freely used or adapted. 
 *
 * @author  Modifications by Bob Jacobsen  Copyright 2008
 * @version $Revision$
 */

public class FileServlet extends AbstractServlet {

    static java.util.ResourceBundle types = java.util.ResourceBundle.getBundle("jmri.web.miniserver.servlet.fileservlet.FileServletTypes");
    static java.util.ResourceBundle paths = java.util.ResourceBundle.getBundle("jmri.web.miniserver.servlet.fileservlet.FileServletPaths");
    static java.util.ResourceBundle htmlStrings = java.util.ResourceBundle.getBundle("jmri.web.miniserver.Html");

    public void service(ServletRequest req, ServletResponse res) 
        throws java.io.IOException {
        
        // get the reader from the request
        BufferedReader in = req.getReader();
        
        // get the writer from the response
        PrintWriter out = res.getWriter();
        
        // get input        
        getInputLines(in);

        String rqst = getRequest();
        if (rqst == null) return;  //bail if request not passed
        if (rqst.equals("/")) rqst = "/index.html";  //if no path passed, set to index.html

        if (log.isDebugEnabled()) log.debug("request is : "+rqst);
        String filename = getFilename(URLDecoder.decode(rqst.substring(1),  java.nio.charset.Charset.defaultCharset().toString())); // drop leading /
        if (log.isDebugEnabled()) log.debug("resolve to filename: "+filename);
        
        // refuse requests that don't satisfy security check
        if (!isSecurityLimitOK(filename)) {
            log.info("Dropping unauthorized request for \""+filename+"\"");
            printHeader(out, "text/html", "403 Forbidden");
            return;
        }
        // now reply
        if (! isDirectory(filename) ) {
        	File tempFile = new File(filename);
        	if (tempFile.exists()) {
        		printHeader(out, getMimeType(filename), "200 OK", 
        				new Date(tempFile.lastModified()), tempFile.length());
        		copyFileContent(filename, res.getOutputStream());
        	} else {
        		printHeader(out, getMimeType(filename), "404 Not Found");
        	}
        } else {
            if (filename.endsWith("/")) {
                // show content
                printHeader(out, "text/html", "200 OK");
                createDirectoryContent(filename, res.getOutputStream());
            } else {
                // send redirect
                printHeader(out, "text/html", "200 OK");
                OutputStreamWriter writer = new OutputStreamWriter(res.getOutputStream());
                try {
                    writer.write("<head>\n");
                    writer.write("<META HTTP-EQUIV=\"PRAGMA\" CONTENT=\"NO-CACHE\">");
                    writer.write("<META HTTP-EQUIV=\"Refresh\" CONTENT=\"0; URL="+filename+"/ \">");
                    writer.write("</html>");
                    writer.flush();
                } finally {
                    writer.close();
                }
                out.flush();
            }
        }        
    }
    
    public void destroy() {}
    
    public void init(javax.servlet.ServletConfig config) {}
    
    public String getServletInfo() { return ""; }
    
    public javax.servlet.ServletConfig getServletConfig() { return null; }


    /**
     * Items to be substituted into FileServletPaths
     */
    Object[] args;


    /** 
     * Check if the file is a directory
     */
    boolean isDirectory(String filename) {
        return new File(filename).isDirectory();
    }
    
    /**
     * Convert the request to a 
     * filename using the FileServletPaths
     * property list.  
     *<p>
     * This takes pieces off the right side of the 
     * request until it matches an entry.
     */
    String getFilename(String name){
        // initialize substitutions
        if (args == null)
            args = new String[] {jmri.jmrit.XmlFile.prefsDir()};

        // strip any trailing ? info
        int qm = name.indexOf('?');
        if (qm != -1) name = name.substring(0, qm);

        String initialName = name;
        // Seach for longest match
        while (name.length()>0) {
            // name is the piece we're going to replace
            if (log.isDebugEnabled()) log.debug("Check ["+name+"]");
            try {
                String prefix = paths.getString(name);
                // found it!
                if (log.isDebugEnabled()) log.debug("Found prefix "+prefix);

                // do substitution
                prefix = java.text.MessageFormat.format(prefix, args);
                
                // check for special case of complete substitution
                if (name.length() == initialName.length()) {
                    return prefix;
                }

                // now combine
                int offset = name.length()+1;
                if (prefix.length()>0 && !prefix.endsWith("/")) prefix = prefix+"/";
                String result = prefix+initialName.substring(Math.min(offset,initialName.length()), initialName.length());
                log.debug("getFilename result: "+result);
                return result;
            } catch (java.util.MissingResourceException e3) {
                // normal, not a problem
                // just proceed
            } 
            // No luck, remove last token
            int last = name.lastIndexOf("/");
            if (last <0) // nothing to remove
                break;
            name = name.substring(0, last);  
        }
        
        log.debug("No match, return original filename: "+initialName);
        return initialName;
    }
    
    /**
     * From a path name ("foo/file.bif"), 
     * determine the mime type.
     *<p>
     * Works with the FileServlet property file
     */
    String getMimeType(String name) {
        // strip any trailing ? info
        int qm = name.indexOf('?');
        if (qm != -1) name = name.substring(0, qm);
        
        name = name.toLowerCase();  //type table is all lowercase

        // Search for longest match
        while (name.length()>0) {
            if (log.isDebugEnabled()) log.debug("Check ["+name+"]");
            try {
                String type = types.getString(name);
                // found it!
                if (log.isDebugEnabled()) log.debug("Found type "+type);
                return type;
            } catch (java.util.MissingResourceException e3) {
                // normal, not a problem
                // just proceed
            } 
            // No luck, try next
            int next = name.indexOf(".",1);
            if (next < 0) break;  // no more dots
            name = name.substring(next, name.length());  
        }
        
        log.debug("No type matches "+name+", return default text type");
        return "text";
    }
    
    static String canonicalWD;
    static String canonicalPrefs;
    
    /**
     * Do security check as to whether file should be served
     *<p>
     * @return true if OK to present file
     */
    boolean isSecurityLimitOK(String filename) 
                throws java.io.IOException {
        if (canonicalWD == null)
            canonicalWD = new File("").getCanonicalPath();
        if (canonicalPrefs == null)
            canonicalPrefs = new File(jmri.jmrit.XmlFile.prefsDir()).getCanonicalPath();

        String thisPath = new File(filename).getCanonicalPath();
        if (thisPath.startsWith(canonicalWD)) return true;
        if (thisPath.startsWith(canonicalPrefs)) return true;
        return false;
    }
    
    
    /* this code based on http://java.sun.com/docs/books/performance/1st_edition/html/JPIOPerformance.fm.html  */
    protected void copyFileContent(String from, OutputStream out) throws IOException{
       InputStream in = null;
       int BUFF_SIZE = 100000;  //set buffer to 100K
       long bytesWritten = 0;
       byte[] buffer = new byte[BUFF_SIZE];
       try {
          in = new FileInputStream(from);
          while (true) {
             synchronized (buffer) {
                int amountRead = in.read(buffer);
                if (amountRead == -1) {
                   break;
                }
                out.write(buffer, 0, amountRead);
                bytesWritten += amountRead;
             }
          } 
    	   
       } finally {
          if (in != null) {
             in.close();
          }
          if (out != null) {
             out.flush();
          }
          if (log.isDebugEnabled()) log.debug("Sent " + bytesWritten + " bytes as " + from + ".");
       }
    }

    
    protected void createDirectoryContent(String filename, OutputStream out) throws IOException {
        log.debug("return directory listing");
        java.text.DateFormat df = java.text.DateFormat.getDateTimeInstance();
        File dir = new File(filename);
        OutputStreamWriter writer = new OutputStreamWriter(out);

        writer.write(htmlStrings.getString("DirectoryFront"));      

        writer.write("<h2>Directory Listing of '" + filename + "'</h2>");
        writer.write("<table class='data'>\r\n<tr><th>Name</th><th>Last modified</th><th>Size</th></tr>\r\n");

        try {
            // write out directory
            for ( File f : dir.listFiles()) {
                // writer.write("<tr><td><a href=\""+f.getName()+"\">"+f.getName()+"</a></td></tr>\n");

                writer.write("<tr><td><a href=\""
                    +f.getName()+(f.isDirectory() ? "/" : "")+"\">"
                    +f.getName()+(f.isDirectory() ? "/" : "")+"</a></td><td align=\"right\">"
                    +df.format(new java.util.Date(f.lastModified()))
                    +"</td><td align=\"right\">"+f.length()+"</td></tr>\r\n");

            }
            writer.write("</table>");
            writer.write("<address>JMRI "+jmri.Version.name()+" Mini Server</address>" );
            writer.write("</body></html>");

        } finally {
            writer.flush();
            out.flush();
        }
    }

    
	static DateFormat dfGMT;

    // Send standard HTTP response (with default values)
    protected void printHeader(PrintWriter out, String mimeType, String responseStatus) {
    	printHeader(out, mimeType, responseStatus, null, -1);
    }
    
    protected void printHeader(PrintWriter out, String mimeType, String responseStatus, Date fileDate) {
    	printHeader(out, mimeType, responseStatus, fileDate, -1);
    }
    
    protected void printHeader(PrintWriter out, String mimeType, String responseStatus, Date fileDate, long fileSize) {

    	if (dfGMT == null) {  //setup format once
    		dfGMT = new SimpleDateFormat("EEE, dd MMM yyyy kk:mm:ss z");
    		dfGMT.setTimeZone(TimeZone.getTimeZone("GMT"));
    	}
    	String s;
    	s = "HTTP/1.1 " + responseStatus + "\r\n" +
    		"Server: JMRI-FileServlet\r\n" +
    		"Content-Type: "+mimeType+"\r\n" +
    		"Connection: Keep-Alive\r\n";
    	if (fileDate != null) {
    		Date now = new Date();
    		s += "Date: " + dfGMT.format(now) + "\r\n";
    		s += "Last-Modified: " + dfGMT.format(fileDate) + "\r\n";
    		s += "Cache-Control: public, max-age=" + 5*60 + "\r\n";  //max-age is in seconds
    		s += "Expires: " + dfGMT.format(new Date(now.getTime() + 5*60*1000)) + "\r\n";  //set expiration to 5 minutes in the future, in ms
    	}
    	if (fileSize != -1) {
    		s += "Content-Length: " + fileSize + "\r\n";
    	}
    	s +=    "\r\n";  //blank line to indicate end of header
    	out.print(s);
    	if (log.isDebugEnabled()) log.debug("Sent Header: "+s.replaceAll("\\r\\n"," | "));
    	out.flush();
    }
    
    
    // initialize logging
    static private org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(FileServlet.class.getName());
}
