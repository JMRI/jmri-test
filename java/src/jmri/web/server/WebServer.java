package jmri.web.server;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import jmri.InstanceManager;
import jmri.ShutDownTask;
import jmri.implementation.QuietShutDownTask;
import jmri.jmris.json.JSON;
import jmri.util.FileUtil;
import jmri.util.zeroconf.ZeroConfService;
import jmri.web.servlet.DenialServlet;
import jmri.web.servlet.RedirectionServlet;
import jmri.web.servlet.directory.DirectoryHandler;
import jmri.web.servlet.directory.ModuleDirectoryHandler;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An HTTP server that handles requests for HttpServlets.
 *
 * This server loads HttpServlets that have registered themselves as
 * {@link org.openide.util.lookup.ServiceProvider}s and that are annotated with
 * the {@link javax.servlet.annotation.WebServlet} annotation.
 *
 * @author Bob Jacobsen Copyright 2005, 2006
 * @author Randall Wood Copyright 2012, 2014
 * @version $Revision$
 */
@ServiceProvider(service = WebServer.class)
public final class WebServer implements LifeCycle.Listener {

    protected Server server;
    protected ZeroConfService zeroConfService = null;
    protected ShutDownTask shutDownTask = null;
    static Logger log = LoggerFactory.getLogger(WebServer.class.getName());

    public WebServer() {
        shutDownTask = new QuietShutDownTask("Stop Web Server") { // NOI18N
            @Override
            public boolean execute() {
                try {
                    WebServerManager.getWebServer().stop();
                } catch (Exception ex) {
                    log.warn("Error shutting down WebServer: " + ex);
                    if (log.isDebugEnabled()) {
                        log.debug("Details follow: ", ex);
                    }
                }
                return true;
            }
        };
    }

    public void start() {
        if (server == null) {
            QueuedThreadPool threadPool = new QueuedThreadPool();
            threadPool.setName("WebServer");
            threadPool.setMaxThreads(1000);
            server = new Server(threadPool);
            ServerConnector connector = new ServerConnector(server);
            connector.setIdleTimeout(5 * 60 * 1000); // 5 minutes
            connector.setSoLingerTime(-1);
            connector.setPort(this.getPreferences().getPort());
            server.setConnectors(new Connector[]{connector});
            server.setHandler(new ContextHandlerCollection());

            // Load paths for redirection or file system handling
            Properties services = new Properties();
            Properties filePaths = new Properties();
            try {
                InputStream in;
                in = this.getClass().getResourceAsStream("Services.properties"); // NOI18N
                services.load(in);
                in.close();
                in = this.getClass().getResourceAsStream("FilePaths.properties"); // NOI18N
                filePaths.load(in);
                in.close();
            } catch (IOException e) {
                log.error(e.getMessage());
            }
            services.stringPropertyNames().stream().forEach((path) -> {
                switch (services.getProperty(path)) {
                    case "fileHandler": // NOI18N
                        this.registerResource(path, filePaths.getProperty(path));
                        break;
                    case "redirectHandler": // NOI18N
                        this.registerRedirection(path, filePaths.getProperty(path));
                        break;
                }
            });
            // Load all servlets that provide the HttpServlet service.
            Lookup.getDefault().lookupAll(HttpServlet.class).stream().forEach((servlet) -> {
                this.registerServlet(servlet.getClass(), servlet);
            });

            server.addLifeCycleListener(this);

            Thread serverThread = new ServerThread(server);
            serverThread.setName("WebServer"); // NOI18N
            serverThread.start();

        }

    }

    public void stop() throws Exception {
        server.stop();
    }

    /**
     * Get the public URI for a portable path. This method returns public URIs
     * for only some portable paths, and does not check that the portable path
     * is actually sane. Note that this refuses to return portable paths that
     * are outside of program: and preference:
     *
     * @param path
     * @return The servable URI or null
     * @see jmri.util.FileUtil#getPortableFilename(java.io.File)
     */
    public static String URIforPortablePath(String path) {
        if (path.startsWith(FileUtil.PREFERENCES)) {
            return path.replaceFirst(FileUtil.PREFERENCES, "/prefs/"); // NOI18N
        } else if (path.startsWith(FileUtil.PROGRAM)) {
            return path.replaceFirst(FileUtil.PROGRAM, "/dist/"); // NOI18N
        } else {
            return null;
        }
    }

    public int getPort() {
        return this.getPreferences().getPort();
    }

    public WebServerPreferences getPreferences() {
        return WebServerManager.getWebServerPreferences();
    }

    /**
     * Register a URL pattern to be denied access.
     *
     * @param urlPattern the pattern to deny access to
     */
    public void registerDenial(String urlPattern) {
        ServletContextHandler servletContext = new ServletContextHandler(ServletContextHandler.NO_SECURITY);
        servletContext.setContextPath(urlPattern);
        DenialServlet servlet = new DenialServlet();
        servletContext.addServlet(new ServletHolder(servlet), "/*"); // NOI18N
        ((ContextHandlerCollection) this.server.getHandler()).addHandler(servletContext);
    }

    /**
     * Register a URL pattern to return resources from the file system.
     *
     * @param urlPattern the pattern to get resources for
     * @param filePath the portable path for the resources
     */
    public void registerResource(String urlPattern, String filePath) {
        ServletContextHandler servletContext = new ServletContextHandler(ServletContextHandler.NO_SECURITY);
        servletContext.setContextPath(urlPattern);
        HandlerList handlers = new HandlerList();
        if (filePath.startsWith("program:")) { // NOI18N
            log.debug("Setting up handler chain for {}", urlPattern);
            // make it possible to override anything under program: with an identical path under preference:
            ResourceHandler preferenceHandler = new DirectoryHandler(FileUtil.getAbsoluteFilename(filePath.replace("program:", "preference:"))); // NOI18N
            ResourceHandler moduleHandler = new ModuleDirectoryHandler(filePath.substring(filePath.indexOf(":") + 1)); // NOI18N
            handlers.setHandlers(new Handler[]{preferenceHandler, moduleHandler, new DefaultHandler()});
        } else {
            ResourceHandler handler = new DirectoryHandler(FileUtil.getAbsoluteFilename(filePath));
            handlers.setHandlers(new Handler[]{handler, new DefaultHandler()});
        }
        ContextHandler handlerContext = new ContextHandler();
        handlerContext.setContextPath(urlPattern);
        handlerContext.setHandler(handlers);
        ((ContextHandlerCollection) this.server.getHandler()).addHandler(handlerContext);
    }

    /**
     * Register a URL pattern to be redirected to another resource.
     *
     * @param urlPattern the pattern to be redirected
     * @param redirection the path to which the pattern is redirected
     */
    public void registerRedirection(String urlPattern, String redirection) {
        ServletContextHandler servletContext = new ServletContextHandler(ServletContextHandler.NO_SECURITY);
        servletContext.setContextPath(urlPattern);
        RedirectionServlet servlet = new RedirectionServlet(urlPattern, redirection);
        servletContext.addServlet(new ServletHolder(servlet), ""); // NOI18N
        ((ContextHandlerCollection) this.server.getHandler()).addHandler(servletContext);
    }

    /**
     * Register a {@link javax.servlet.http.HttpServlet } that is annotated with
     * the {@link javax.servlet.annotation.WebServlet } annotation.
     *
     * This method calls
     * {@link #registerServlet(java.lang.Class, javax.servlet.http.HttpServlet)}
     * with a null HttpServlet.
     *
     * @param type The actual class of the servlet.
     */
    public void registerServlet(Class<? extends HttpServlet> type) {
        this.registerServlet(type, null);
    }

    /**
     * Register a {@link javax.servlet.http.HttpServlet } that is annotated with
     * the {@link javax.servlet.annotation.WebServlet } annotation.
     *
     * Registration reads the WebServlet annotation to get the list of paths the
     * servlet should handle and creates instances of the Servlet to handle each
     * path.
     *
     * Note that all HttpServlets registered using this mechanism must have a
     * default constructor.
     *
     * @param type The actual class of the servlet.
     * @param instance An un-initialized, un-registered instance of the servlet.
     */
    public void registerServlet(Class<? extends HttpServlet> type, HttpServlet instance) {
        try {
            for (ServletContextHandler handler : this.registerServlet(
                    ServletContextHandler.NO_SECURITY,
                    type,
                    instance
            )) {
                ((ContextHandlerCollection) this.server.getHandler()).addHandler(handler);
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
            log.error("Unable to register servlet", ex);
        }
    }

    private List<ServletContextHandler> registerServlet(int options, Class<? extends HttpServlet> type, HttpServlet instance)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        WebServlet info = type.getAnnotation(WebServlet.class);
        List<ServletContextHandler> handlers = new ArrayList<>(info.urlPatterns().length);
        for (String pattern : info.urlPatterns()) {
            ServletContextHandler context = new ServletContextHandler(options);
            context.setContextPath(pattern);
            if (instance == null) {
                log.debug("Creating new {} for URL pattern {}", type.getName(), pattern);
                context.addServlet(type, pattern);
            } else {
                log.debug("Using existing {} for URL pattern {}", type.getName(), pattern);
                context.addServlet(new ServletHolder(instance), "/*");
                instance = null; // ensure instance is only used for the first URL pattern
            }
            handlers.add(context);
        }
        return handlers;
    }

    @Override
    public void lifeCycleStarting(LifeCycle lc) {
        if (InstanceManager.shutDownManagerInstance() != null) {
            InstanceManager.shutDownManagerInstance().register(shutDownTask);
        }
        log.info("Starting Web Server on port {}", this.getPreferences().getPort());
    }

    @Override
    public void lifeCycleStarted(LifeCycle lc) {
        HashMap<String, String> properties = new HashMap<>();
        properties.put("path", "/"); // NOI18N
        properties.put(JSON.JSON, JSON.JSON_PROTOCOL_VERSION);
        log.info("Starting ZeroConfService _http._tcp.local for Web Server with properties {}", properties);
        zeroConfService = ZeroConfService.create("_http._tcp.local.", this.getPreferences().getPort(), properties); // NOI18N
        zeroConfService.publish();
        log.debug("Web Server finished starting");
    }

    @Override
    public void lifeCycleFailure(LifeCycle lc, Throwable thrwbl) {
        log.warn("Web Server failed", thrwbl);
    }

    @Override
    public void lifeCycleStopping(LifeCycle lc) {
        if (zeroConfService != null) {
            zeroConfService.stop();
        }
        log.info("Stopping Web Server");
    }

    @Override
    public void lifeCycleStopped(LifeCycle lc) {
        if (InstanceManager.shutDownManagerInstance() != null) {
            InstanceManager.shutDownManagerInstance().deregister(shutDownTask);
        }
        log.debug("Web Server stopped");
    }

    static private class ServerThread extends Thread {

        private final Server server;

        public ServerThread(Server server) {
            this.server = server;
        }

        @Override
        public void run() {
            try {
                server.start();
                server.join();
            } catch (Exception ex) {
                log.error("Exception starting Web Server: " + ex);
            }
        }
    }
}
