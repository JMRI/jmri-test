package jmri.configurexml;

import org.jdom.Element;
import com.sun.java.util.collections.List;

import jmri.InstanceManager;
import jmri.TurnoutManager;

/**
 * Provides the abstract base and store functionality for
 * configuring TurnoutManagers, working with
 * AbstractTurnoutManagers.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @version $Revision: 1.2 $
 */
public abstract class AbstractTurnoutManagerConfigXML implements XmlAdapter {

    public AbstractTurnoutManagerConfigXML() {
    }

    /**
     * Default implementation for storing the contents of a
     * TurnoutManager
     * @param o Object to store, of type TurnoutManager
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        Element turnouts = new Element("turnouts");
        setStoreElementClass(turnouts);
        TurnoutManager tm = (TurnoutManager) o;
        if (tm!=null) {
            com.sun.java.util.collections.Iterator iter =
                                    tm.getSystemNameList().iterator();

            while (iter.hasNext()) {
                String sname = (String)iter.next();
                if (sname==null) log.error("System name null during store");
                log.debug("system name is "+sname);
                String uname = tm.getBySystemName(sname).getUserName();
                Element elem = new Element("turnout")
                            .addAttribute("systemName", sname);
                if (uname!=null) elem.addAttribute("userName", uname);
                log.debug("store turnout "+sname+":"+uname);
                turnouts.addContent(elem);

            }
        }
        return turnouts;
    }

    /**
     * Subclass provides implementation to create the correct top
     * element, including the type information.
     * @param turnouts The top-level element being created
     */
    abstract public void setStoreElementClass(Element turnouts);

    /**
     * Create a TurnoutManager object of the correct class, then
     * register and fill it.
     * @param turnouts Top level Element to unpack.
     */
    abstract public void load(Element turnouts);

    /**
     * Utility method to load the individual Turnout objects.
     * If there's no additional info needed for a specific turnout type,
     * invoke this with the parent of the set of Turnout elements.
     * @param turnouts Element containing the Turnout elements to load.
     */
    public void loadTurnouts(Element turnouts) {
        List turnoutList = turnouts.getChildren("turnout");
        if (log.isDebugEnabled()) log.debug("Found "+turnoutList.size()+" turnouts");
        TurnoutManager tm = InstanceManager.turnoutManagerInstance();

        for (int i=0; i<turnoutList.size(); i++) {
            if ( ((Element)(turnoutList.get(i))).getAttribute("systemName") == null) {
                log.warn("unexpected null in systemName "+((Element)(turnoutList.get(i)))+" "+((Element)(turnoutList.get(i))).getAttributes());
                break;
            }
            String sysName = ((Element)(turnoutList.get(i))).getAttribute("systemName").getValue();
            String userName = null;
            if ( ((Element)(turnoutList.get(i))).getAttribute("userName") != null)
            userName = ((Element)(turnoutList.get(i))).getAttribute("userName").getValue();

            tm.newTurnout(sysName, userName);
        }
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ConfigXmlManager.class.getName());

}