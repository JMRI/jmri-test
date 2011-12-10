package jmri.web.servlet.frameimage;

import java.net.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

import javax.imageio.*;
import javax.swing.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import jmri.util.JmriJFrame;
import jmri.web.miniserver.MiniServerManager;

import javax.servlet.Servlet;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/** 
 * A simple servlet that returns a JMRI window as
 * a PNG image or enclosing HTML file.
 * <p>
 * The suffix of the request determines which.
 * <dl>
 * <dt>.html<dd>Returns a HTML file that displays the frame enabled for
 *      clicking via server side image map; see the .properties file for the content
 * <dt>.png<dd>Just return the image
 * <dt>no name<dd>Return an HTML page with links to available images
 * </dl>
 *<P>
 * The associated .properties file contains the HTML fragments used to 
 * form replies.
 *<P>
 *  Parts taken from Core Web Programming from 
 *  Prentice Hall and Sun Microsystems Press,
 *  http://www.corewebprogramming.com/.
 *  &copy; 2001 Marty Hall and Larry Brown;
 *  may be freely used or adapted. 
 *
 * @author  Modifications by Bob Jacobsen  Copyright 2005, 2006, 2008
 * @version     $Revision$
 */

public class JmriJFrameServlet implements Servlet {

    static String clickRetryTime = ((Integer) MiniServerManager.miniServerPreferencesInstance().getClickDelay()).toString();
    static String noclickRetryTime = ((Integer) MiniServerManager.miniServerPreferencesInstance().getRefreshDelay()).toString();
    static ArrayList<String> disallowedFrames = new ArrayList<String>(
            Arrays.asList(MiniServerManager.miniServerPreferencesInstance().getDisallowedFrames().split("\n")));
    boolean useAjax = MiniServerManager.miniServerPreferencesInstance().useAjax();
    boolean plain = MiniServerManager.miniServerPreferencesInstance().isPlain();
    boolean protect = false;
    protected int maxRequestLines = 50;
    protected String serverName = "JMRI-JFrameServer";
    static java.util.ResourceBundle rb 
            = java.util.ResourceBundle.getBundle("jmri.web.servlet.frameimage.JmriJFrameServlet");

    public void destroy() {}
    
    public void init(javax.servlet.ServletConfig config) {}
    
    public String getServletInfo() { return ""; }
    
    public javax.servlet.ServletConfig getServletConfig() { return null; }

    public void service(ServletRequest req, ServletResponse res) throws java.io.IOException {

        // get the reader from the request
        BufferedReader in = req.getReader();

        // read in the info
        String[] inputLines = new String[maxRequestLines];

        int i = 0;
        try {
            for (i = 0; i < maxRequestLines; i++) {
                inputLines[i] = in.readLine();
                if (inputLines[i] == null) // Client closed connection.
                {
                    break;
                }
                if (inputLines[i].length() == 0) { // Blank line.
                    if (usingPost(inputLines)) {
                        readPostData(inputLines, i, in);
                        i = i + 2;
                    }
                    break;
                }
            }
        } catch (IOException e) {
            log.error("IO Exception reading request: " + e);
        }

        // get the writer from the response
        PrintWriter out = res.getWriter();

        // parse request
        String frameName = parseRequest(inputLines, i);

        //if no frame passed, send list of available frames
        if (frameName.equals("")) {
            listReply(out, res);
            return;
        }

        int x = 0, y = 0;
        boolean click = false;

        HashMap<String, String> modifiers = new HashMap<String, String>();
        if (frameName.contains("?")) {
            String[] s = frameName.split("\\?");
            for (i = 1; i < s.length; i++) {
                if (s[i].length() > 0) {
                    if (s[i].contains("=")) {
                        for (String t : s[i].split("&")) {
                            modifiers.put(t.split("=")[0].toLowerCase(), t.split("=")[1]);
                        }
                    } else if (s[i].contains(",")) {
                        x = Integer.parseInt(s[i].split(",")[0]);
                        y = Integer.parseInt(s[i].split(",")[1]);
                        click = true;
                    }
                }
            }
        }

        if (modifiers.containsKey("retry")) {
            noclickRetryTime = modifiers.get("retry");
        }
        if (modifiers.containsKey("ajax")) {
            useAjax = Boolean.valueOf(modifiers.get("ajax"));
        }
        if (modifiers.containsKey("plain")) {
            plain = Boolean.valueOf(modifiers.get("plain"));
        }
        if (modifiers.containsKey("protect")) {
            protect = Boolean.valueOf(modifiers.get("protect"));
        }

        // remove any type suffix
        String suffix = null;
        if (frameName.contains(".")) {
            int stop = (frameName.contains("?")) ? frameName.indexOf("?") : frameName.length();
            suffix = frameName.substring(frameName.lastIndexOf("."), stop);
            if (suffix.length() > 0) {
                suffix = suffix.substring(1, suffix.length());
            }
            frameName = frameName.substring(0, frameName.indexOf("."));
        }
        if (log.isDebugEnabled()) {
            log.debug("requested frame +[" + frameName + "] suffix [" + suffix + "] modifiers [" + modifiers + "]");
        }

        //check for disallowed frame
        if (disallowedFrames.contains(frameName)) {
            handleError("Frame [" + frameName + "] not allowed (check Prefs)", 403, res);
            return;
        }
        // Find the frame
        JmriJFrame frame = JmriJFrame.getFrame(frameName);
        if (frame == null) {
            handleError("Can't find frame [" + frameName + "]", 404, res);
            return;
        }
        if (!frameName.equals(frame.getTitle())) {
            log.warn("Request for [" + frameName + "] found title [" + frame.getTitle() + "], mismatched");
        }

        // If there's a click modifier, parse it and execute (skip if protect turned on)
        if (click && !protect) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Attempt click at " + x + "," + y);
                }
                Component c = frame.getContentPane().findComponentAt(x, y);
                // ((javax.swing.JButton) frame.getContentPane().findComponentAt(x,y) ).doClick();
                sendClick(frameName, c, x, y, frame.getContentPane());
            } catch (Exception ec) {
                log.error("Exception in click code: " + ec);
            }
        }

        // Send a reply depending on type
        if (suffix == null) {
            imageReply(frameName, out, frame, res);
        } else if (suffix.toLowerCase().equals("png")) {
            imageReply(frameName, out, frame, res);
        } else if (suffix.toLowerCase().equals("html")) {
            htmlReply(frameName, out, frame, res, click);
        } else {
            handleError("Can't handle suffix [" + suffix + "], use .png or .html", 400, res);
        }
    }
   
    void imageReply(String name, PrintWriter out, JmriJFrame frame, ServletResponse res ) 
            throws java.io.IOException {
    	putFrameImage(frame, res.getOutputStream());
    }
    
    //send html list of available frames
    void listReply(PrintWriter out, ServletResponse res) {

        String h = rb.getString("FrameHeader");
        String s = rb.getString("FrameDocType");
        s += rb.getString("ListFront");
    	// list frames, (open JMRI windows)
    	List<JmriJFrame> framesList = JmriJFrame.getFrameList();
    	int framesNumber = framesList.size();
    	for (int i = 0; i < framesNumber; i++) { //get all frame titles
    		JmriJFrame iFrame = framesList.get(i);
    		String frameTitle = iFrame.getTitle();
    		//don't add to list if blank or disallowed
    		if (!frameTitle.equals("") && !disallowedFrames.contains(frameTitle)) {
    			//format a table row for each valid window (frame)
    			String frameURLhtml = "/frame/" + frameTitle.replaceAll(" ", "%20") + ".html";
    			String frameURLpng  = "/frame/" + frameTitle.replaceAll(" ", "%20") + ".png";
    			s += "<TR><TD>" + frameTitle + "</TD>\n";
    			s += "<TD><A href='"+frameURLhtml+"'><IMG src='"+frameURLpng+"' /></A></TD></TR>\n"; 
    		}
    	}

    	s += "</TABLE>";
        
        s += rb.getString("ListFooter");

        h += s.length() + "\n";
        Date now = new Date();
		h += "Date: " + now + "\n";
		h += "Last-Modified: " + now + "\n";
		out.println(h);  //write header with calculated fields
        out.println(s);  //write out rest of html page
        out.flush();
//        out.close();
        if (log.isDebugEnabled()) log.debug("Sent " + s.length() + " bytes html.");
    }
    
    void htmlReply(String name, PrintWriter out, JmriJFrame frame, ServletResponse res, boolean click) {
        // 0 is host
        // 1 is frame name
        // 2 is retry in META tag, click or noclick retry
        // 3 is retry in next URL, future retry
        // 4 is state of plain
        // 5 is the CSS stylesteet name addition, based on "plain"
        // 6 is ajax preference
        // 7 is protect
        Object[] args = new String[]{"localhost",
            name,
            (click ? clickRetryTime : noclickRetryTime),
            noclickRetryTime,
            Boolean.toString(plain),
            (plain ? "-plain" : ""),
            Boolean.toString(useAjax),
            Boolean.toString(protect)};
        String h = rb.getString("FrameHeader");
        String s = rb.getString("FrameDocType");
        s += java.text.MessageFormat.format(rb.getString("FramePart1"), args);
        if (useAjax) {
            s += java.text.MessageFormat.format(rb.getString("FramePart2Ajax"), args);
        } else {
            s += java.text.MessageFormat.format(rb.getString("FramePart2NonAjax"), args);
        }
        s += java.text.MessageFormat.format(rb.getString("FrameFooter"), args);

        h += s.length() + "\n";
        Date now = new Date();
        h += "Date: " + now + "\n";
        h += "Last-Modified: " + now + "\n";
        out.println(h);  //write header with calculated fields
        out.println(s);  //write out rest of html page
        out.flush();
//        out.close();
        if (log.isDebugEnabled()) {
            log.debug("Sent " + s.length() + " bytes jframe html with click=" + (click ? "True" : "False"));
        }
    }
   
    void sendClick(String name, Component c, int xg, int yg, Container FrameContentPane) {  // global positions
        int x = xg-c.getLocation().x;
        int y = yg-c.getLocation().y;
        // log.debug("component is "+c);
        if (log.isDebugEnabled()) log.debug("Local click at "+x+","+y);
        
        if (c.getClass().equals(JButton.class)) {
        	((JButton)c).doClick();
            return;
        }
        else if( c.getClass().equals(JCheckBox.class)) {
        	((JCheckBox)c).doClick();
        	return;
        }
        else if(c.getClass().equals(JRadioButton.class)) {
        	((JRadioButton)c).doClick();
        	return;
        }
        else if (c instanceof MouseListener) {
            if (log.isDebugEnabled()) log.debug("Invoke directly on MouseListener, at "+x+","+y);
            sendClickSequence((MouseListener)c, c, x, y);
            return;

        } else if (c instanceof jmri.jmrit.display.MultiSensorIcon) {
            if (log.isDebugEnabled()) log.debug("Invoke Clicked on MultiSensorIcon");
            MouseEvent e = new MouseEvent(c,
            		MouseEvent.MOUSE_CLICKED,
            		0,      // time
            		0,      // modifiers
            		xg,yg,    // this component expects global positions for some reason
            		1,      // one click
            		false   // not a popup
            );
            ((jmri.jmrit.display.MultiSensorIcon)c).doMouseClicked(e);
            return;
           
        } else if (c instanceof jmri.jmrit.display.Positionable) {
            if (log.isDebugEnabled()) log.debug("Invoke Pressed, Released and Clicked on Positionable");

            MouseEvent e = new MouseEvent(c,
            		MouseEvent.MOUSE_PRESSED,
            		0,      // time
            		0,      // modifiers
            		x,y,    // x, y not in this component?
            		1,      // one click
            		false   // not a popup
            );
            ((jmri.jmrit.display.Positionable)c).doMousePressed(e);

            e = new MouseEvent(c,
            		MouseEvent.MOUSE_RELEASED,
            		0,      // time
            		0,      // modifiers
            		x,y,    // x, y not in this component?
            		1,      // one click
            		false   // not a popup
            );
            ((jmri.jmrit.display.Positionable)c).doMouseReleased(e);

            e = new MouseEvent(c,
            		MouseEvent.MOUSE_CLICKED,
            		0,      // time
            		0,      // modifiers
            		x,y,    // x, y not in this component?
            		1,      // one click
            		false   // not a popup
            );
            ((jmri.jmrit.display.Positionable)c).doMouseClicked(e);
            return;

        } else {
            MouseListener[] la = c.getMouseListeners();
            if (log.isDebugEnabled()) log.debug("Invoke "+la.length+" contained mouse listeners");
            if (log.isDebugEnabled()) log.debug("component is "+c);
            /*  Using c.getLocation() above we adjusted the click position for the offset of the control relative to the frame.
             *  That works fine in the cases above.  
             *  In this case getLocation only provides the offset of the control relative to the Component.  
             *  So we also need to adjust the click position for the offset of the Component relative to the frame.
             */
// was incorrect for zoomed panels, turned off
//            Point pc = c.getLocationOnScreen();
//            Point pf = FrameContentPane.getLocationOnScreen();
//           	x -= (int)(pc.getX() - pf.getX());
//           	y -= (int)(pc.getY() - pf.getY());
           	
            for (int i = 0; i<la.length; i++) {
                if (log.isDebugEnabled()) log.debug("Send click sequence at "+x+","+y);
                sendClickSequence(la[i], c, x, y);
            }
           return;
        }
    }
    
    private void sendClickSequence(MouseListener m, Component c, int x, int y) {
    	/*
    	 * create the sequence of mouse events needed to click on a control:
    	 *  MOUSE_ENTERED
    	 *  MOUSE_PRESSED
    	 *  MOUSE_RELEASED
    	 *  MOUSE_CLICKED
    	 */
    	MouseEvent e = new MouseEvent(c,
    			MouseEvent.MOUSE_ENTERED,
    			0,      // time
    			0,      // modifiers
    			x,y,    // x, y not in this component?
    			1,      // one click
    			false   // not a popup
    	);
    	m.mouseEntered(e);
    	e = new MouseEvent(c,
    			MouseEvent.MOUSE_PRESSED,
    			0,      // time
    			0,      // modifiers
    			x,y,    // x, y not in this component?
    			1,      // one click
    			false,   // not a popup
    			MouseEvent.BUTTON1
    	);
    	m.mousePressed(e);
        e = new MouseEvent(c,
		 		MouseEvent.MOUSE_RELEASED,
		 		0,      // time
		 		0,      // modifiers
		 		x,y,    // x, y not in this component?
		 		1,      // one click
		 		false,   // not a popup
		 		MouseEvent.BUTTON1
 		);
 		m.mouseReleased(e);
 		e = new MouseEvent(c,
                                      MouseEvent.MOUSE_CLICKED,
                                      0,      // time
                                      0,      // modifiers
                                      x,y,    // x, y not in this component?
                                      1,      // one click
                                      false,   // not a popup
                                      MouseEvent.BUTTON1
                                      );
        m.mouseClicked(e);
        e = new MouseEvent(c,
                MouseEvent.MOUSE_EXITED,
                0,      // time
                0,      // modifiers
                x,y,    // x, y not in this component?
                1,      // one click
                false,   // not a popup
                MouseEvent.BUTTON1
                );
        m.mouseExited(e);
    }
    /**
     * Handle an error by returning an error message
     */
    void handleError(String error, int statusCode, ServletResponse res)
            throws java.io.IOException {
        PrintWriter out = res.getWriter();

        String statusDescription;
        switch (statusCode) {
            case 400:
                statusDescription = "Bad Request";
                break;
            case 403:
                statusDescription = "Forbidden";
                break;
            case 404:
                statusDescription = "Not Found";
                break;
            case 503:
                statusDescription = "Service Unavailable";
                break;
            case 200:
                statusDescription = "OK";
                break;
            default:
                statusDescription = "";
                break;
        }
        //set up replacements and put in html
    	Object[] args = new String[] {"localhost", Integer.toString(statusCode), statusDescription, error};
        String s = java.text.MessageFormat.format(rb.getString("ErrorPage"), args);

        out.println(s);
    }
    
    /**
     * Parse input lines to find 
     * frame name.
     */
    String parseRequest(String[] input, int len) {
        // expect "GET /key/Frame HTTP/1.1"
        //
        // remove HTTP from back
    	String part = input[0].substring(0, input[0].lastIndexOf(" HTTP"));
        
        // remove "GET /" from front
        part = part.substring(5, part.length());
        
        //return empty string now if no frame passed
        int pos = part.indexOf('/');
        if (pos == -1) {
            if (log.isDebugEnabled()) log.debug("request is key-only");
        	return "";
        }
        
        //remove key from front
        String rawRequest = part.substring(part.indexOf('/'), part.length());
        
        // decode
        String request = "<error>";
        try {
            URI u = new URI(rawRequest);
            if (log.isDebugEnabled()) log.debug("URI ["+u+"]");
            request = u.getSchemeSpecificPart();
            // drop leading "/"
            request = request.substring(1, request.length());
        } catch (java.net.URISyntaxException e4) {
            log.error("error in URI: "+e4);
        }
        if (log.isDebugEnabled()) log.debug("request is ["+request+"]");
        
        return request;
    }
    
    /** 
     * Get the frame graphics as png and output to browser
     */
    void putFrameImage(JmriJFrame frame, OutputStream outStream) {
    	try {
    		BufferedImage image 
    		= new BufferedImage(frame.getContentPane().getWidth(), 
    				frame.getContentPane().getHeight(), 
    				BufferedImage.TYPE_INT_RGB);
    		frame.getContentPane().paint(image.createGraphics());

    		//put it in a temp file to get post-compression size
    		ByteArrayOutputStream tmpFile = new ByteArrayOutputStream();
    		ImageIO.write(image, "png", tmpFile);
    		tmpFile.close();
    		long contentLength = tmpFile.size();

    		printHeader(new PrintWriter(outStream), contentLength);  //write header with length

    		try {
    			outStream.write(tmpFile.toByteArray());  //write image data
    		} finally {
    			if (outStream != null) {
    				outStream.flush();
    			}
    		}
    		if (log.isDebugEnabled()) log.debug("Sent [" + frame.getTitle() + "] as " + contentLength + " byte png.");

    	} catch (Exception e) {
    		log.error(e.getMessage());
    	}
    }
        
    // Send standard HTTP response for image/png type
   
    private void printHeader(PrintWriter out, long fileSize) {
    	String h;
		h = "HTTP/1.1 200 OK\n" +
                "Server: " + serverName + "\n" +
                "Content-Type: image/png\n" +
                "Cache-Control: no-cache\n" +
                "Connection: Keep-Alive\n" +
                "Keep-Alive: timeout=5, max=100\n" +
                "Content-Length: " + fileSize + "\n";
        		Date now = new Date();
        		h += "Date: " + now + "\n";
        		h += "Last-Modified: " + now + "\n";
        		
                h += "\n";  //blank line to indicate end of header
    	out.print(h);
    	if (log.isDebugEnabled()) log.debug("Sent Header: "+h.replaceAll("\\n"," | "));
        out.flush();
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
        int length = in.read(postData, 0, contentLength);
        inputs[++i] = new String(postData, 0, length);
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
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(JmriJFrameServlet.class.getName());
}
