/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.util;

import java.io.File;
import java.io.FilenameFilter;

/**
 *
 * @author rhwood
 */
public class XmlFilenameFilter implements FilenameFilter {

    @Override
    public boolean accept(File dir, String name) {
        return name.endsWith(".xml"); // NOI18N
    }

}
