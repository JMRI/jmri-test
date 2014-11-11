package jmri.web.servlet.directory;

import java.io.IOException;
import java.net.MalformedURLException;
import org.eclipse.jetty.util.resource.Resource;
import org.openide.modules.InstalledFileLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Return a web resource (file or directory) from a module.
 *
 * @author Randall Wood randall.h.wood@alexandriasoftware.com
 */
public class ModuleDirectoryHandler extends DirectoryHandler {

    private final static Logger log = LoggerFactory.getLogger(ModuleDirectoryHandler.class);
    private final String installedFileBase;

    public ModuleDirectoryHandler(String resourceBase) {
        super(resourceBase);
        this.installedFileBase = resourceBase;
    }

    /**
     * Get the resource.
     *
     * Note that this does not return a resource for /dist/, but does return
     * resources under /dist/. This is because /dist/ itself has no meaning in a
     * modular context. This also implies that, for any given resource
     * directory, this returns only the first module containing that directory
     * instead of the amalgamation of all directories in all modules matching
     * that path.
     *
     * @param path The web path for the given resource
     * @return A resource
     * @throws MalformedURLException if the path is not valid
     */
    @Override
    public Resource getResource(String path) throws MalformedURLException {
        Resource resource = null;
        if (path.equals("/")) {
            path = this.installedFileBase;
        } else {
            path = this.installedFileBase + path;
        }
        // Trim /s from requests to prevent InstalledFileLocator from throwning
        // an IllegalArgumentExeception
        if (path.startsWith("/")) { // NOI18N
            path = path.substring(1);
        }
        if (path.endsWith("/")) { // NOI18N
            path = path.substring(0, path.length() - 1);
        }
        try {
            resource = Resource.newResource(InstalledFileLocator.getDefault().locate(path, null, true));
        } catch (IOException ex) {
            log.warn("Error locating \"{}\" in modules: {}", path, ex.getMessage());
        }
        if (resource == null || !resource.exists()) {
            resource = null; // TODO: change to empty resource
        }
        return resource;
    }

}
