package jmri.profile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author rhwood
 */
public class Profile {

    private String name;
    private String id;
    private File path;
    private Boolean disabled;
    protected static final String ID = "id"; // NOI18N
    protected static final String NAME = "name"; // NOI18N
    protected static final String PATH = "path"; // NOI18N
    protected static final String DISABLED = "disabled"; // NOI18N
    protected static final String PROPERTIES = "profile.properties"; // NOI18N
    public static final String CONFIG_FILENAME = "ProfileConfig.xml"; // NOI18N

    /**
     * Create a Profile object given just a path to it.
     *
     * @param path The Profile's directory
     * @throws IOException
     */
    public Profile(File path) throws IOException {
        this.path = path;
        this.readProfile();
    }

    /**
     *
     * @param name
     * @param id
     * @param path
     * @throws IOException
     * @throws IllegalArgumentException
     */
    public Profile(String name, String id, File path) throws IOException, IllegalArgumentException {
        if (!path.getName().equals(id)) {
            throw new IllegalArgumentException(id + " " + path.getName() + " do not match");
        }
        this.name = name;
        this.id = id + "." + Integer.toHexString(Float.floatToIntBits((float)Math.random()));
        this.path = path;
        if (!path.exists() && path.mkdir()) {
            this.save();
        }
        if (!path.isDirectory()) {
            throw new IllegalArgumentException(path + " is not a directory"); // NOI18N
        }
        if (!(new File(path, PROPERTIES)).canRead()) {
            throw new IllegalArgumentException(path + " does not contain a profile.properties file"); // NOI18N
        }
    }

    private void save() throws IOException {
        Properties p = new Properties();
        File f = new File(this.path, PROPERTIES);
        FileOutputStream os = null;

        p.setProperty(NAME, this.name);
        p.setProperty(ID, this.id);
        p.setProperty(DISABLED, Boolean.toString(this.disabled));
        if (!f.exists() && !f.createNewFile()) {
            throw new IOException("Unable to create file at " + f.getAbsolutePath()); // NOI18N
        }
        try {
            os = new FileOutputStream(f);
            p.storeToXML(os, "JMRI Profile"); // NOI18N
            os.close();
        } catch (IOException ex) {
            if (os != null) {
                os.close();
            }
            throw ex;
        }
    }

    /**
     * Append ~ to the directory containing the profile so that the Profile's Id
     * is not equal to the Profile's location. This method also removes the
     * Profile from the ProfileManager's list of available Profiles.
     */
    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
        if (disabled) {
            ProfileManager.getDefaultManager().disableProfile(this);
        } else {
            ProfileManager.getDefaultManager().enableProfile(this);
        }
    }

    public Boolean isDisabled() {
        return this.disabled;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    public void setName(String name) throws IOException {
        this.name = name;
        this.save();
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @return the path
     */
    public File getPath() {
        return path;
    }

    private void readProfile() throws IOException {
        Properties p = new Properties();
        File f = new File(this.path, PROPERTIES);
        FileInputStream is = null;
        try {
            is = new FileInputStream(f);
            p.loadFromXML(is);
            is.close();
        } catch (IOException ex) {
            if (is != null) {
                is.close();
            }
            throw ex;
        }
        this.id = p.getProperty(ID);
        this.name = p.getProperty(NAME);
        this.disabled = Boolean.getBoolean(p.getProperty(DISABLED, Boolean.toString(false)));
    }

    @Override
    public String toString() {
        return this.getName();
    }
}
