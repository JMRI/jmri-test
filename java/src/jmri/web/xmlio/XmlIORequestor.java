// XmlIORequestor.java

package jmri.web.xmlio;

import org.jdom.*;

/**
 * Interface received a delayed response to a monitoring request.
 *
 * <p>
 * There is no guarantee as to on which thread this takes place.
 *
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
 * @author	Bob Jacobsen  Copyright (C) 2008, 2009, 2010
 * @version	$Revision: 1.1 $
 * @see  jmri.web.xmlio.XmlIOFactory
 */
public interface XmlIORequestor {

    /**
     * Call back with the result of a XmlIOServer.monitorRequest(Element)
     */
    public void monitorReply(Element e);

}

/* @(#)XmlIORequestor.java */
