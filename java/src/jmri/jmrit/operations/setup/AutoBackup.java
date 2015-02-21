// AutoBackup.java
package jmri.jmrit.operations.setup;

import java.io.File;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Specific Backup class for backing up and restoring Operations working files
 * to the Automatic Backup Store. Derived from BackupBase.
 *
 * @author Gregory Madsen Copyright (C) 2012
 */
public class AutoBackup extends BackupBase {

    static Logger log = LoggerFactory
            .getLogger(AutoBackup.class.getName());

    /**
     * Creates an AutoBackup instance and initializes the root directory to the
     * given name.
     */
    public AutoBackup() {
        super("autoBackups"); // NOI18N
    }

    /**
     * Backs up Operations files to a generated directory under the automatic
     * backup root directory.
     *
     * @throws java.io.IOException
     */
    public synchronized void autoBackup() throws IOException {

        // Get a name for this backup set that does not already exist.
        String setName = suggestBackupSetName();

        copyBackupSet(_operationsRoot, new File(_backupRoot, setName));
    }
}
