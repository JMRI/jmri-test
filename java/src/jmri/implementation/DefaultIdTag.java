// DefaultIdTag.java

package jmri.implementation;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import jmri.IdTagManager;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Reporter;
import org.jdom.Element;

/**
 * Concrete implementation of the {@link jmri.IdTag} interface
 * for the Internal system.
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under
 * the terms of version 2 of the GNU General Public License as published
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * for more details.
 * <P>
 *
 * @author      Matthew Harris  Copyright (C) 2011
 * @version     $Revision$
 * @since       2.11.4
 */
public class DefaultIdTag extends AbstractIdTag {

    private int _currentState = UNKNOWN;

    public DefaultIdTag(String systemName) {
        super(systemName.toUpperCase());
        setWhereLastSeen(null);
    }

    public DefaultIdTag(String systemName, String userName) {
        super(systemName.toUpperCase(), userName);
        setWhereLastSeen(null);
    }

    public final void setWhereLastSeen(Reporter r) {
        Reporter oldWhere = this._whereLastSeen;
        Date oldWhen = this._whenLastSeen;
        this._whereLastSeen = r;
        if (r!=null) {
            this._whenLastSeen = InstanceManager.getDefault(IdTagManager.class).isFastClockUsed()?
                InstanceManager.clockControlInstance().getTime():
                Calendar.getInstance().getTime();
        } else {
            this._whenLastSeen = null;
        }
        setCurrentState(r!=null?SEEN:UNSEEN);
        firePropertyChange("whereLastSeen", oldWhere, this._whereLastSeen);
        firePropertyChange("whenLastSeen", oldWhen, this._whenLastSeen);
    }

    private void setCurrentState(int state) {
        try {
            setState(state);
        } catch (JmriException ex) {
            log.warn("Problem setting state of IdTag " + getSystemName());
        }
    }

    public void setState(int s) throws JmriException {
        this._currentState = s;
    }

    public int getState() {
        return this._currentState;
    }

    public Element store(boolean storeState) {
        Element e = new Element("idtag");
        // e.setAttribute("systemName", this.mSystemName); // not needed from 2.11.1
        e.addContent(new Element("systemName").addContent(this.mSystemName));
        if (this.mUserName!=null && this.mUserName.length()>0) {
            // e.setAttribute("userName", this.mUserName); // not needed from 2.11.1
            e.addContent(new Element("userName").addContent(this.mUserName));
        }
        if (this.getComment()!=null && this.getComment().length()>0) {
            e.addContent(new Element("comment").addContent(this.getComment()));
        }
        if (this.getWhereLastSeen()!=null && storeState) {
            e.addContent(new Element("whereLastSeen").addContent(this.getWhereLastSeen().getSystemName()));
        }
        if (this.getWhenLastSeen()!=null && storeState) {
            e.addContent(new Element("whenLastSeen").addContent(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(this.getWhenLastSeen())));
        }
        return e;
    }

    public void load(Element e) {
        if (e.getName().equals("idtag")) {
            if (log.isDebugEnabled())
                log.debug("Load IdTag element for " + this.getSystemName());
            if (e.getChild("userName")!=null)
                this.setUserName(e.getChild("userName").getText());
            if (e.getChild("comment")!=null)
                this.setComment(e.getChild("comment").getText());
            if (e.getChild("whereLastSeen")!=null) {
                this.setWhereLastSeen(
                        InstanceManager.reporterManagerInstance().provideReporter(
                            e.getChild("whereLastSeen").getText()));
                this._whenLastSeen = null;
            }
            if (e.getChild("whenLastSeen")!=null) {
                log.debug("When Last Seen: " + e.getChild("whenLastSeen").getText());
                try {
                    this._whenLastSeen = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).parse(e.getChild("whenLastSeen").getText());
                } catch (ParseException ex) {
                    log.warn("Error parsing when last seen: " + ex);
                }
            }
        } else {
            log.error("Not an IdTag element: " + e.getName());
        }
    }

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DefaultIdTag.class.getName());

}

/* @(#)DefaultIdTag.java */