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
    protected static final String ID = "id"; // NOI18N
    protected static final String NAME = "name"; // NOI18N
    protected static final String PATH = "path"; // NOI18N
    protected static final String PROPERTIES = "profile.properties"; // NOI18N

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
        this.name = name;
        this.id = id;
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
    }

    @Override
    public String toString() {
        return this.getName();
    }
}
