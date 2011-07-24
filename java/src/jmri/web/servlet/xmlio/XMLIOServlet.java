package jmri.web.servlet.xmlio;

import java.net.*;
import java.io.*;

import java.util.StringTokenizer;

import jmri.web.miniserver.AbstractServlet;
import jmri.web.miniserver.MiniServerManager;

import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.*;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import jmri.web.xmlio.*;

/** 
 * A simple servlet that uses the 
 * {@link jmri.web.xmlio} package to do XML I/O to and 
 * from JMRI using AJAX.  See the examples in the web
 * directory.
 *
 * @author  Modifications by Bob Jacobsen  Copyright 2005, 2006, 2008
 * @version     $Revision$
 */

public class XMLIOServlet extends AbstractServlet implements XmlIORequestor {
    
    public String getServletInfo() { return "XMLIO Servlet"; }
    
    public void service(ServletRequest req, ServletResponse res) throws java.io.IOException {
        // get the context
        getServletConfig();
        getServletContext();
        
        // get the reader from the request
        BufferedReader in = req.getReader();
        
        // read in the info
        String[] inputLines = new String[maxRequestLines];

        int i=0;
        try {
            for (i=0; i<maxRequestLines; i++) {
                inputLines[i] = in.readLine();
                if (inputLines[i] == null) // Client closed connection.
                    break;
                if (inputLines[i].length() == 0) { // Blank line.
                    if (usingPost(inputLines)) {
                        readPostData(inputLines, i, in);
                        i = i + 2;
                    }
                    break;
                }
            }
        } catch (IOException e) {
            log.error("IO Exception reading request: "+e);
        }

        if (log.isDebugEnabled()) {
            log.debug("buffer contains:");
            for (int j = 0; j<i; j++) 
                log.debug(" "+j+":"+inputLines[j].replaceAll("\\n"," ").replaceAll("\\r"," "));
            log.debug("end buffer");
        }
        
        // get the writer from the response
        out = res.getWriter();
        
        // parse request
        String request = parseRequest(inputLines, i);
        if (request == null) {
        	return;  //get out if request not parsed
        }
        
        if (builder == null) builder = jmri.jmrit.XmlFile.getBuilder(false);

        Reader reader = new StringReader(request);
        try {
            doc = builder.build(reader);
        } catch (JDOMException e1) {
            log.error("JDOMException on input: "+e1, e1);
        }
        Element root = doc.getRootElement();
        
        // start processing reply
        if (factory == null) factory = new XmlIOFactory();
        XmlIOServer srv = factory.getServer();

        // if list or throttle elements present, or item elements that do set, do immediate operation
        boolean immediate = false;
        if (root.getChild("list") != null ||
                root.getChild("throttle") != null) {
                immediate = true;
        }
        if (!immediate) {
            for (Object e : root.getChildren()) {
                if (((Element)e).getAttributeValue("set") != null ||
                        ((Element)e).getChild("set") != null) {
                    immediate = true;
                }
                break;
            }
        }
        if (immediate) {
            logComm("immediate reply");
            try {
                srv.immediateRequest(root);  // modifies 'doc' in place
            } catch (jmri.JmriException e1) {
                log.error("JmriException while creating reply: "+e1, e1);
            }
            sendReply(doc);
            return;
        }        
        
        // else do monitoring operation
        try {
            // start processing the request
            thread = Thread.currentThread();
            srv.monitorRequest(root, this);
            logComm("stalling thread, waiting for reply");
            
            try {
                Thread.sleep(10000000000000L);  // really long
//                Thread.sleep(5000);  // not so long (30 secs)
            	logComm("Thread sleep completed.");
            } catch (InterruptedException e) {
            	logComm("Interrupted");
            }
            logComm("thread resumes and replies");
            sendReply(doc); 
        } catch (jmri.JmriException e1) {
            log.error("JmriException while creating reply: "+e1, e1);
        }
    }
    
    Document doc = null;
    PrintWriter out;
    Thread thread;
    
    public void monitorReply(Element e) {
        thread.interrupt();
    }
    
    protected void sendReply(Document doc) {
        // send reply header
        out.print("HTTP/1.1 200 OK\r\n");
        out.print("Server: JMRI-XMLIOServlet\r\n");
        out.print("Content-Type: text/xml\r\n");
        out.print("Cache-Control: no-cache\r\n");
        
        // format reply xml
        if (fmt == null) {
            fmt = new XMLOutputter();
            fmt.setFormat(org.jdom.output.Format.getPrettyFormat());
        }
        strDoc = fmt.outputString(doc);  //format xml doc to a string

        //send length of string to expect
        out.print("Content-Length: " + strDoc.length() + "\r\n");

        //send blank line to indicate end of headers
        out.println();  
        
        out.println(strDoc);  //write out the xml string
        if (log.isInfoEnabled()) { logComm("Returned:" + strDoc.replaceAll("\\n","").replaceAll("\\r","")); }
        
    }
    
    String strDoc = null;
    SAXBuilder builder = null;
    XMLOutputter fmt = null;
    XmlIOFactory factory = null;
    
    /**
     * Parse input lines to extract request
     */
    String parseRequest(String[] input, int len) {
        // expect "GET /xmlio HTTP/1.1"
        // or
        // "POST /xmlio HTTP/1.1
        //
        
        if (input[0].toUpperCase().startsWith("GET")) {
            // remove GET and key from front
            if (log.isDebugEnabled()) log.debug("GET request of "+len+" lines: "+input[0]);
            String part = input[0].substring(5, input[0].length()); //drop the "GET "
            part = part.substring(part.indexOf('?')+1, part.length());  //get portion after the ?
            
            // remove HTTP from back
            String request = part.substring(0, part.lastIndexOf(" HTTP"));
            
            //unencode URL "parms" back to string
            try {
				request = URLDecoder.decode(request, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				request = "<error/>";
			}
            if (log.isInfoEnabled()) logComm("xml request is ["+request.replaceAll("\\n"," ").replaceAll("\\r"," ")+"]");
            
            return request;
        
        } else if (input[0].toUpperCase().startsWith("POST")) {
            if (log.isDebugEnabled()) log.debug("POST request of "+len+" lines: "+input[0]);

            // strip header by finding empty line
            int i = 0;
            while (i<len) {
                if (input[i] == null || input[i].equals(""))
                    break;
                i++;
            }
            i++;  // i is not index of 1st data
            
            StringBuilder request = new StringBuilder();
            while (i<len) {
                request.append(input[i]);
                i++;
            }

            if (log.isInfoEnabled()) logComm("xml request is ["+new String(request).replaceAll("\\n","").replaceAll("\\r","")+"]");
            
            return new String(request);
        } else {
            log.error("Unexpected request format: "+input[0]);
            return null;
        }
    }


    // Normal Web page requests use GET, so this server can simply
    // read a line at a time. However, HTML forms can also use 
    // POST, in which case we have to determine the number of POST
    // bytes that are sent so we know how much extra data to read
    // after the standard HTTP headers.
    
    private boolean usingPost(String[] inputs) {
        return(inputs[0].toUpperCase().startsWith("POST"));
    }
    
    private void readPostData(String[] inputs, int i,
                              BufferedReader in)
        throws IOException {
        int contentLength = contentLength(inputs);
        char[] postData = new char[contentLength];

        //handle reads larger than default block size
        int readLength = -1;
        int totalLength = 0;
        while (totalLength < contentLength && readLength != 0) {
            readLength = in.read(postData, totalLength, contentLength - totalLength);
            totalLength += readLength;
        }
      
        inputs[++i] = new String(postData, 0, totalLength);
    }
    
    // Given a line that starts with Content-Length,
    // this returns the integer value specified.
    
    private int contentLength(String[] inputs) {
        String input;
        for (int i=0; i<inputs.length; i++) {
            if (inputs[i].length() == 0)
                break;
            input = inputs[i].toUpperCase();
            if (input.startsWith("CONTENT-LENGTH"))
                return(getLength(input));
        }
        return(0);
    }
    
    private int getLength(String length) {
        StringTokenizer tok = new StringTokenizer(length);
        tok.nextToken();
        return(Integer.parseInt(tok.nextToken()));
    }
    
    private void logComm(String s) {
    	if (MiniServerManager.miniServerPreferencesInstance().isShowComm()) {
    		log.info(s);
    	} else {
    		log.debug(s);
    	}
    	return;
    }
    
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(XMLIOServlet.class.getName());
}
