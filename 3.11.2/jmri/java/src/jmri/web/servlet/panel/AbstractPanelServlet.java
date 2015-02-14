/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.web.servlet.panel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.JComponent;
import jmri.jmris.json.JSON;
import jmri.jmris.json.JsonUtil;
import jmri.jmrit.display.Editor;
import jmri.util.FileUtil;
import jmri.util.JmriJFrame;
import jmri.util.StringUtil;
import jmri.web.server.WebServer;
import jmri.web.servlet.ServletUtil;
import static jmri.web.servlet.ServletUtil.IMAGE_PNG;
import static jmri.web.servlet.ServletUtil.UTF8_APPLICATION_JSON;
import static jmri.web.servlet.ServletUtil.UTF8_TEXT_HTML;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author rhwood
 */
abstract class AbstractPanelServlet extends HttpServlet {

    protected ObjectMapper mapper;
    private static final long serialVersionUID = 3134679703461026038L;
    static Logger log = LoggerFactory.getLogger(AbstractPanelServlet.class.getName());

    abstract protected String getPanelType();

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        this.mapper = new ObjectMapper();
        this.mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        log.debug("Handling GET request for {}", request.getRequestURI());
        if (request.getParameter(JSON.NAME) != null) {
            response.sendRedirect("/panel/" + request.getParameter(JSON.NAME));
        } else if (request.getRequestURI().endsWith("/")) {
            listPanels(request, response);
        } else {
            String[] path = request.getRequestURI().split("/");
            String panelName = StringUtil.unescapeString(path[path.length - 1]);
            if ("png".equals(request.getParameter("format"))) {
                BufferedImage panel = getPanelImage(panelName);
                if (panel == null) {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "See the JMRI console for details.");
                } else {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(panel, "png", baos);
                    baos.close();
                    response.setContentType(IMAGE_PNG);
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.setContentLength(baos.size());
                    response.getOutputStream().write(baos.toByteArray());
                    response.getOutputStream().close();
                }
            } else if ("html".equals(request.getParameter("format")) || null == request.getParameter("format")) {
                this.listPanels(request, response);
            } else {
                boolean useXML = (!JSON.JSON.equals(request.getParameter("format")));
                response.setContentType(UTF8_APPLICATION_JSON);
                String panel = getPanelText(panelName, useXML);
                if (panel == null) {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "See the JMRI console for details.");
                } else if (panel.startsWith("ERROR")) {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, panel.substring(5).trim());
                } else {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.setContentLength(panel.length());
                    response.getOutputStream().print(panel);
                }
            }
        }
    }

    protected void listPanels(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (JSON.JSON.equals(request.getParameter("format"))) {
            response.setContentType(UTF8_APPLICATION_JSON);
            ServletUtil.getInstance().setNonCachingHeaders(response);
            response.getWriter().print(JsonUtil.getPanels(request.getLocale(), JSON.XML));
        } else {
            response.setContentType(UTF8_TEXT_HTML);
            response.getWriter().print(String.format(request.getLocale(),
                    FileUtil.readURL(FileUtil.findURL(Bundle.getMessage(request.getLocale(), "Panel.html"))),
                    String.format(request.getLocale(),
                            Bundle.getMessage(request.getLocale(), "HtmlTitle"),
                            ServletUtil.getInstance().getRailroadName(false),
                            Bundle.getMessage(request.getLocale(), "PanelsTitle")
                    ),
                    ServletUtil.getInstance().getNavBar(request.getLocale(), "/panel"),
                    ServletUtil.getInstance().getRailroadName(false),
                    ServletUtil.getInstance().getFooter(request.getLocale(), "/panel")
            ));
        }
    }

    protected BufferedImage getPanelImage(String name) {
        JComponent panel = getPanel(name);
        if (panel == null) {
            return null;
        }
        BufferedImage bi = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_ARGB);
        panel.paint(bi.getGraphics());
        return bi;
    }

    abstract protected JComponent getPanel(String name);

    protected String getPanelText(String name, boolean useXML) {
        if (useXML) {
            return getXmlPanel(name);
        } else {
            return getJsonPanel(name);
        }
    }

    abstract protected String getJsonPanel(String name);

    abstract protected String getXmlPanel(String name);

    protected Editor getEditor(String name) {
        for (Editor editor : Editor.getEditors()) {
            if (((JmriJFrame) editor.getTargetPanel().getTopLevelAncestor()).getTitle().equals(name)) {
                return editor;
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
        for (String aspect : signalMast.getValidAspects()) {
            Element ea = new Element(aspect.replaceAll(" ", "_")); //create element for aspect after replacing spaces
            String url = signalMast.getAppearanceMap().getImageLink(aspect, "default");  //TODO: use correct imageset
            if (!url.contains("preference:")) {
                url = "program:" + url.substring(url.indexOf("resources"));
            }
            ea.setAttribute("url", url); //        
            icons.addContent(ea);
        }
        String url = signalMast.getAppearanceMap().getImageLink("$held", "default");  //add "Held" aspect if defined
        if (!url.isEmpty()) {
            if (!url.contains("preference:")) {
                url = "program:" + url.substring(url.indexOf("resources"));
            }
            Element ea = new Element("Held");
            ea.setAttribute("url", url);
            icons.addContent(ea);
        }
        url = signalMast.getAppearanceMap().getImageLink("$dark", "default");  //add "Dark" aspect if defined
        if (!url.isEmpty()) {
            if (!url.contains("preference:")) {
                url = "program:" + url.substring(url.indexOf("resources"));
            }
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
