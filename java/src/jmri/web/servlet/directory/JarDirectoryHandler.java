package jmri.web.servlet.directory;

import java.net.MalformedURLException;
import org.eclipse.jetty.util.resource.Resource;

/**
 *
 * @author rhwood
 */
public class JarDirectoryHandler extends DirectoryHandler {

    public JarDirectoryHandler(String resourceBase) {
        super(resourceBase);
    }

    @Override
    public Resource getResource(String path) throws MalformedURLException {
        Resource resource = Resource.newClassPathResource("META-INF/resources" + path);
        if (resource == null || !resource.exists()) {
            resource = Resource.newClassPathResource(path);
        }
        return resource;
    }
}
