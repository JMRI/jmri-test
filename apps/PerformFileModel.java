// PerformFileModel.java

package apps;

import java.util.ArrayList;
import java.util.List;

/**
 * A PerformFileModel object loads an xml file
 * when the program is started.
 * <P>
 * @author	Bob Jacobsen   Copyright 2003
 * @version     $Revision: 1.4 $
 * @see PerformFilePanel
 */
public class PerformFileModel {

    public PerformFileModel() {
        fileName=null;
    }

    String fileName;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String n) {
        fileName = n;
    }

    static public void rememberObject(PerformFileModel m) {
        l.add(m);
    }
    static public List rememberedObjects() {
        return l;
    }
    static List l = new ArrayList();
}


