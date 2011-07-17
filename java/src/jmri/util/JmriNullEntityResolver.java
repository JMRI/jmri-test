// JmriNullEntityResolver.java

package jmri.util;

/**
 * Entity Resolver to return a null DTD content, used to 
 * bypass verification.
 *
 * @author Bob Jacobsen  Copyright 2007
 * @version $Revision: 1.3 $
 */

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * Return a content-less DTD
 */
public class JmriNullEntityResolver implements EntityResolver {
    public InputSource resolveEntity (String publicId, String systemId) {
        log.debug("resolves "+systemId);
        return new InputSource(new java.io.StringReader(""));
    }

    static private org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(JmriNullEntityResolver.class.getName());

}
 