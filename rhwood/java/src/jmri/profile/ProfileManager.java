package jmri.profile;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import jmri.beans.Bean;
import jmri.util.FileUtil;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage JMRI configuration profiles.
 *
 * @author rhwood
 */
public class ProfileManager extends Bean {

    private ArrayList<Profile> profiles = new ArrayList<Profile>();
    private ArrayList<File> searchPaths = new ArrayList<File>();
    private Profile activeProfile = null;
    private boolean startWithActiveProfile = true;
    private File catalog;
    private boolean readingProfiles = false;
    private static ProfileManager instance = null;
    private static final String ACTIVE = "activeProfile"; // NOI18N
    private static final String CATALOG = "profiles.xml"; // NOI18N
    private static final String PROFILE = "profile"; // NOI18N
    public static final String PROFILES = "profiles"; // NOI18N
    private static final String PROFILECONFIG = "profile-config"; // NOI18N
    private static final String SEARCHPATHS = "search-paths"; // NOI18N
    private static Logger log = LoggerFactory.getLogger(ProfileManager.class);

    public ProfileManager() {
        this.catalog = new File(FileUtil.getPreferencesPath() + CATALOG);
        try {
            this.readProfiles();
            this.findProfiles();
        } catch (JDOMException ex) {
            log.error(ex.getLocalizedMessage(), ex);
        } catch (IOException ex) {
            log.error(ex.getLocalizedMessage(), ex);
        }
    }

    public static ProfileManager getDefaultManager() {
        if (instance == null) {
            instance = new ProfileManager();
        }
        return instance;
    }

    public Profile getActiveProfile() {
        return activeProfile;
    }

    public void setActiveProfile(String id) {
        if (id == null) {
            activeProfile = null;
            FileUtil.setProfilePath(null);
            return;
        }
        for (Profile p : profiles) {
            if (p.getId().equals(id)) {
                this.setActiveProfile(p);
                return;
            }
        }
    }

    public void setActiveProfile(Profile profile) {
        if (profile == null) {
            activeProfile = null;
            FileUtil.setProfilePath(null);
            return;
        }
        activeProfile = profile;
        FileUtil.setProfilePath(profile.getPath().toString());
    }

    public void saveActiveProfile(File file) throws IOException {
        Properties p = new Properties();
        FileOutputStream os = null;

        if (this.getActiveProfile() != null) {
            p.setProperty(ACTIVE, this.getActiveProfile().getId());
        }
        if (!file.exists() && !file.createNewFile()) {
            throw new IOException("Unable to create file at " + file.getAbsolutePath()); // NOI18N
        }
        try {
            os = new FileOutputStream(file);
            p.storeToXML(os, "Active profile configuration (saved at " + (new Date()).toString() + ")"); // NOI18N
            os.close();
        } catch (IOException ex) {
            if (os != null) {
                os.close();
            }
            throw ex;
        }

    }

    public void readActiveProfile(File file) throws IOException {
        Properties p = new Properties();
        FileInputStream is = null;
        try {
            is = new FileInputStream(file);
            p.loadFromXML(is);
            is.close();
        } catch (IOException ex) {
            if (is != null) {
                is.close();
            }
            throw ex;
        }
        this.setActiveProfile(p.getProperty(ACTIVE));
    }

    public Profile[] getProfiles() {
        return profiles.toArray(new Profile[profiles.size()]);
    }

    public Profile getProfiles(int index) {
        return profiles.get(index);
    }

    public void setProfiles(Profile profile, int index) {
        Profile oldProfile = profiles.get(index);
        if (!this.readingProfiles) {
            profiles.set(index, profile);
            this.fireIndexedPropertyChange(PROFILES, index, oldProfile, profile);
        }
    }

    protected void addProfile(Profile profile) {
        profiles.add(profile);
        if (!this.readingProfiles) {
            int index = profiles.indexOf(profile);
            this.fireIndexedPropertyChange(PROFILES, index, null, profile);
        }
    }

    protected void removeProfile(Profile profile) {
        int index = profiles.indexOf(profile);
        if (profiles.remove(profile)) {
            this.fireIndexedPropertyChange(PROFILES, index, profile, null);
        }
    }

    public ArrayList<File> getSearchPaths() {
        return new ArrayList<File>(searchPaths);
    }

    protected void addSearchPath(File path) {
        searchPaths.add(path);
        this.findProfiles();
    }

    protected void removeSearchPath(File path) {
        searchPaths.remove(path);
    }

    private void readProfiles() throws JDOMException, IOException {
        try {
            if (!catalog.exists()) {
                this.writeProfiles();
            }
            if (!catalog.canRead()) {
                return;
            }
            this.readingProfiles = true;
            Document doc = (new SAXBuilder()).build(catalog);
            profiles.clear();
            for (Element e : (List<Element>) doc.getRootElement().getChild(PROFILES).getChildren()) {
                profiles.add(new Profile(FileUtil.getFile(FileUtil.getExternalFilename(e.getAttributeValue(Profile.PATH)))));
            }
            searchPaths.clear();
            for (Element e : (List<Element>) doc.getRootElement().getChild(SEARCHPATHS).getChildren()) {
                File path = FileUtil.getFile(FileUtil.getExternalFilename(e.getAttributeValue(Profile.PATH)));
                if (!searchPaths.contains(path)) {
                    searchPaths.add(path);
                }
            }
            if (searchPaths.isEmpty()) {
                searchPaths.add(FileUtil.getFile(FileUtil.getPreferencesPath()));
            }
            this.readingProfiles = false;
        } catch (JDOMException ex) {
            this.readingProfiles = false;
            throw ex;
        } catch (IOException ex) {
            this.readingProfiles = false;
            throw ex;
        }
    }

    private void writeProfiles() throws IOException {
        FileWriter fw = null;
        Document doc = new Document();
        doc.setRootElement(new Element(PROFILECONFIG));
        Element profilesElement = new Element(PROFILES);
        Element pathsElement = new Element(SEARCHPATHS);
        for (Profile p : this.profiles) {
            Element e = new Element(PROFILE);
            e.setAttribute(Profile.ID, p.getId());
            e.setAttribute(Profile.PATH, FileUtil.getPortableFilename(p.getPath()));
            profilesElement.addContent(e);
        }
        for (File f : this.searchPaths) {
            Element e = new Element(Profile.PATH);
            e.setAttribute(Profile.PATH, FileUtil.getPortableFilename(f.getPath()));
            pathsElement.addContent(e);
        }
        doc.getRootElement().addContent(profilesElement);
        doc.getRootElement().addContent(pathsElement);
        try {
            fw = new FileWriter(catalog);
            (new XMLOutputter()).output(doc, fw);
            fw.close();
        } catch (IOException ex) {
            // close fw if possible
            if (fw != null) {
                fw.close();
            }
            // rethrow the error
            throw ex;
        }
    }

    /**
     * @return the startWithActiveProfile
     */
    public boolean isStartWithActiveProfile() {
        return startWithActiveProfile;
    }

    /**
     * @param startWithActiveProfile the startWithActiveProfile to set
     */
    public void setStartWithActiveProfile(boolean startWithActiveProfile) {
        this.startWithActiveProfile = startWithActiveProfile;
    }

    private void findProfiles() {
        for (File sp : this.searchPaths) {
            File[] profilePaths = sp.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return (pathname.isDirectory() && Arrays.asList(pathname.list()).contains(Profile.PROPERTIES));
                }
            });
            for (File pp : profilePaths) {
                try {
                    this.addProfile(new Profile(pp));
                } catch (IOException ex) {
                    log.error("Error attempting to read Profile at {}", pp, ex);
                }
            }
        }
    }
}
