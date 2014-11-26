package org.jmri.application;

import edu.umd.cs.findbugs.annotations.CheckReturnValue;
import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

@DefaultAnnotation({NonNull.class, CheckReturnValue.class})

@net.jcip.annotations.Immutable

/**
 * Handles translatable resource strings. This particular subclass of
 * {@link jmri.Bundle} has different naming and access conventions so that it
 * does not conflict with NetBeans.
 *
 * @author Randall Wood <randall.h.wood@alexandriasoftware.com>
 */
public class AppsBundle extends jmri.Bundle {

    private final static String name = "apps.AppsBundle"; // NOI18N

    //
    // below here is boilerplate to be copied exactly
    //
    /**
     * Provides a translated string for a given key from the package resource
     * bundle or parent.
     * <p>
     * Note that this is intentionally package-local access.
     *
     * @param key Bundle key to be translated
     * @return Internationalized text
     */
    public static String getMessage(String key) {
        return b.handleGetMessage(key);
    }

    /**
     * Merges user data with a translated string for a given key from the
     * package resource bundle or parent.
     * <p>
     * Uses the transformation conventions of the Java MessageFormat utility.
     * <p>
     * Note that this is intentionally package-local access.
     *
     * @see java.text.MessageFormat
     * @param key Bundle key to be translated
     * @param subs One or more objects to be inserted into the message
     * @return Internationalized text
     */
    public static String getMessage(String key, Object... subs) {
        return b.handleGetMessage(key, subs);
    }

    private final static jmri.Bundle b = new AppsBundle();

    @Override
    @Nullable
    protected String bundleName() {
        return name;
    }

    @Override
    protected jmri.Bundle getBundle() {
        return b;
    }

    @Override
    protected String retry(String key) {
        return super.getBundle().handleGetMessage(key);
    }

}
