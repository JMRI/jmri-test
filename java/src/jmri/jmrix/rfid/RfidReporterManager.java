// RfidReporterManager.java

package jmri.jmrix.rfid;

import jmri.managers.AbstractReporterManager;

/**
 * Rfid implementation of a ReporterManager.
 * <P>
 * System names are "FRpppp", where ppp is a
 * representation of the RFID reader.
 * <P>
 * @author      Bob Jacobsen    Copyright (C) 2008
 * @author      Matthew Harris  Copyright (C) 2011
 * @version     $Revision: 1.3 $
 * @since       2.11.4
 */
abstract public class RfidReporterManager extends AbstractReporterManager implements RfidListener {

    private String prefix;

    public RfidReporterManager(String prefix) {
        super();
        this.prefix = prefix;
    }

    public String getSystemPrefix() {
        return prefix;
    }

    public void message(RfidMessage m) {
        log.warn("Unexpected message received"+m);
    }

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RfidReporterManager.class.getName());

}

/* @(#)RfidReporterManager.java */
