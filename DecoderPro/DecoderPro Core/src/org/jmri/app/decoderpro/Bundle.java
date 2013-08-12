/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jmri.app.decoderpro;

import edu.umd.cs.findbugs.annotations.Nullable;

/**
 *
 * @author rhwood
 */
public class Bundle extends org.jmri.app.Bundle {

    private final static String name = "org.jmri.app.decoderpro.Bundle"; // NOI18N

    //
    // below here is boilerplate to be copied exactly
    //
    
    /**
     * Provides a translated string for a given 
     * key from the package resource bundle or 
     * parent.
     *<p>
     * Note that this is intentionally package-local
     * access.
     * 
     * @param key Bundle key to be translated
     * @return Internationalized text
     */
    static String getMessage(String key) {
        return b.handleGetMessage(key);
    }
    /**
     * Merges user data with a translated string for a given 
     * key from the package resource bundle or 
     * parent.
     *<p>
     * Uses the transformation conventions of 
     * the Java MessageFormat utility.
     *<p>
     * Note that this is intentionally package-local
     * access.
     *
     * @see java.text.MessageFormat
     * @param key Bundle key to be translated
     * @param subs One or more objects to be inserted into the message
     * @return Internationalized text
     */
    static String getMessage(String key, Object ... subs) {
        return b.handleGetMessage(key, subs);
    }
   
    private final static apps.Bundle b = new apps.Bundle();
    @Override @Nullable protected String bundleName() {return name; }
    @Override protected jmri.Bundle getBundle() { return b; }
    @Override protected String retry(String key) { return super.getBundle().handleGetMessage(key); }

}
