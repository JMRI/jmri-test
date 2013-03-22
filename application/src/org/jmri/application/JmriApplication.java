package org.jmri.application;

/**
 * Provide JMRI application-level support to JMRI NetBeans modules
 * 
 * Since JMRI applications no longer control the main() method when run within
 * a NetBeans RCP, this class provides JMRI-specific application-level support
 * by starting JMRI managers, setting titles, and similar functions.
 *
 * @author rhwood
 */
public final class JmriApplication {

    private static JmriApplication application = null;
    private String title;
    private String configFile;
    private Boolean started = false;
    private Boolean shown = false;
    private Boolean stopped = false;

    private JmriApplication(String title) {
        this.title = title;
        this.configFile = title + "Config.xml";
    }

    public static JmriApplication getApplication(String title) throws IllegalAccessException {
        if (application == null) {
            application = new JmriApplication(title);
        } else {
            throw new IllegalAccessException();
        }
        return application;
    }

    public static JmriApplication getApplication() throws NullPointerException {
        if (application == null) {
            throw new NullPointerException();
        }
        return application;
    }

    public void onStart() {
        if (!started) {
            started = true;
        }
    }

    public void onShown() {
        if (!shown) {
            shown = true;
        }
    }

    public void onStop() {
        if (!stopped) {
            stopped = true;
        }
    }
}
