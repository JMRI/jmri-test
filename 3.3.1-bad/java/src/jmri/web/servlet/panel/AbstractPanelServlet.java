/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.web.servlet.panel;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jmri.jmrit.display.Editor;
import jmri.util.JmriJFrame;
import jmri.util.StringUtil;
import jmri.web.server.WebServer;

import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Element;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 *
 * @author rhwood
 */
abstract class AbstractPanelServlet extends HttpServlet {

	protected ObjectMapper mapper;
	private static final long serialVersionUID = 3134679703461026038L;
	protected static final String JSON_CONTENT_TYPE = "application/json; charset=utf-8";
	protected static final String XML_CONTENT_TYPE = "application/xml; charset=utf-8";
    static Logger log = Logger.getLogger(AbstractPanelServlet.class.getName());

    abstract protected String getPanelType();

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        this.mapper = new ObjectMapper();
        this.mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (log.isDebugEnabled()) {
            log.debug("Handling GET request for " + request.getRequestURI());
        }
        if (request.getRequestURI().endsWith("/")) {
            listPanels(request, response);
        } else {
        	boolean useXML = true;
        	if ("json".equals(request.getParameter("format"))) {
        		useXML = false;
        	}
            String[] path = request.getRequestURI().split("/");
            response.setContentType(XML_CONTENT_TYPE);
            String panel = getPanel(StringUtil.unescapeString(path[path.length - 1]), useXML);
            if (panel == null) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "See the JMRI console for details.");
            } else if (panel.startsWith("ERROR")) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, panel.substring(5).trim());
            } else {
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentLength(panel.length());
                response.getWriter().print(panel);
            }
        }
    }

    protected void listPanels(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	if ("json".equals(request.getParameter("format"))) {
    		response.sendRedirect("/json/panels");
    	}
        response.sendRedirect("/xmlio/list?type=panel");
    }

    protected String getPanel(String name, boolean useXML) {
    	if (useXML) {
    		return getXmlPanel(name);
    	} else {
    		return getJsonPanel(name);
    	}
    }
    
    abstract protected String getJsonPanel(String name);
    
    abstract protected String getXmlPanel(String name);

    protected Editor getEditor(String name) {
        List<JmriJFrame> frames = JmriJFrame.getFrameList(Editor.class);
        for (JmriJFrame frame : frames) {
            if (((JmriJFrame) ((Editor) frame).getTargetPanel().getTopLevelAncestor()).getTitle().equals(name)) {
                return (Editor) frame;
            }
        }
        return null;
    }

    protected void parsePortableURIs(Element element) {
        if (element != null) {
            for (Object child : element.getChildren()) {
                parsePortableURIs((Element) child);
                for (Object attr : ((Element) child).getAttributes()) {
                    if (((Attribute) attr).getName().equals("url")) {
                        String url = WebServer.URIforPortablePath(((Attribute) attr).getValue());
                        if (url != null) {
                            ((Attribute) attr).setValue(url);
                        } else {
//                            ((Element) child).removeAttribute("url");  //TODO: this doesn't work, gets comodification error
                        }
                    }
                }
            }

        }
    }

    //build and return an "icons" element containing icon urls for all signalmast states,
    //  element names are aspect names, with blanks replaced by underscores
	Element getSignalMastIconsElement(String name) {
        Element icons = new Element("icons");
        jmri.SignalMast signalMast = jmri.InstanceManager.signalMastManagerInstance().getSignalMast(name);
        java.util.Vector<String> aspects = signalMast.getValidAspects();
        for (int i=0; i<aspects.size(); i++){
        	String aspect = aspects.elementAt(i); 
            Element ea = new Element(aspect.replaceAll(" ", "_")); //create element for aspect after replacing spaces
            String url = signalMast.getAppearanceMap().getImageLink(aspect, "default");  //TODO: use correct imageset
            if(!url.contains("preference:"))
                url = "program:" + url.substring(url.indexOf("resources"));
            ea.setAttribute("url", url); //        
            icons.addContent(ea);
        } 
        String url = signalMast.getAppearanceMap().getImageLink("$held", "default");  //add "Held" aspect if defined
        if (url != "") {
            if(!url.contains("preference:"))
                url = "program:" + url.substring(url.indexOf("resources"));
        	Element ea = new Element("Held");
        	ea.setAttribute("url", url);        
        	icons.addContent(ea);
        }
        url = signalMast.getAppearanceMap().getImageLink("$dark", "default");  //add "Dark" aspect if defined
        if (url != "") {
            if(!url.contains("preference:"))
                url = "program:" + url.substring(url.indexOf("resources"));
        	Element ea = new Element("Dark"); 
        	ea.setAttribute("url", url);        
        	icons.addContent(ea);
        }
    	Element ea = new Element("Unknown"); 
    	ea.setAttribute("url", "program:resources/icons/misc/X-red.gif");  //add icon for unknown state        
    	icons.addContent(ea);

    	return icons;
	}

}
