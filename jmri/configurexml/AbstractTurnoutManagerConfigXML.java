package jmri.configurexml;

import jmri.InstanceManager;
import jmri.TurnoutManager;
import jmri.Turnout;
import jmri.Sensor;
import jmri.TurnoutOperation;
import jmri.TurnoutOperationManager;
import jmri.configurexml.turnoutoperations.TurnoutOperationXml;

import com.sun.java.util.collections.List;
import java.util.Iterator;
import org.jdom.Element;
import org.jdom.Attribute;

/**
 * Provides the abstract base and store functionality for
 * configuring TurnoutManagers, working with
 * AbstractTurnoutManagers.
 * <P>
 * Typically, a subclass will just implement the load(Element turnouts)
 * class, relying on implementation here to load the individual turnouts.
 * Note that these are stored explicitly, so the
 * resolution mechanism doesn't need to see *Xml classes for each
 * specific Turnout or AbstractTurnout subclass at store time.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @version $Revision: 1.14 $
 */
public abstract class AbstractTurnoutManagerConfigXML implements XmlAdapter {

    public AbstractTurnoutManagerConfigXML() {
    }

    /**
     * Default implementation for storing the contents of a
     * TurnoutManager and associated TurnoutOperation's
     * @param o Object to store, of type TurnoutManager
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        Element turnouts = new Element("turnouts");
        setStoreElementClass(turnouts);
        TurnoutManager tm = (TurnoutManager) o;
        if (tm!=null) {
        	TurnoutOperationManagerXml tomx = new TurnoutOperationManagerXml();
        	Element opElem = tomx.store(TurnoutOperationManager.getInstance());
        	turnouts.addContent(opElem);
            com.sun.java.util.collections.Iterator iter =
                                    tm.getSystemNameList().iterator();

            while (iter.hasNext()) {
                String sname = (String)iter.next();
                if (sname==null) log.error("System name null during store");
                log.debug("system name is "+sname);
                Turnout t = tm.getBySystemName(sname);
                String uname = t.getUserName();
                Element elem = new Element("turnout")
                            .addAttribute("systemName", sname);
                if (uname!=null) elem.addAttribute("userName", uname);
                log.debug("store turnout "+sname+":"+uname);
                
                // include feedback info
                elem.addAttribute("feedback", t.getFeedbackModeName());
                Sensor s;
                s = t.getFirstSensor();
                if (s!=null) elem.addAttribute("sensor1", s.getSystemName());
                s = t.getSecondSensor();
                if (s!=null) elem.addAttribute("sensor2", s.getSystemName());
                
                // add operation stuff
                String opstr = null;
                TurnoutOperation op = t.getTurnoutOperation();
                if (t.getInhibitOperation()) {
                	opstr = "Off";
                } else if (op==null) {
                	opstr = "Default";
                } else if (op.isNonce()) {	// nonce operation appears as subelement
        			TurnoutOperationXml adapter = TurnoutOperationXml.getAdapter(op);
        			if (adapter != null) {
        				Element nonceOpElem = adapter.store(op);
        				if (opElem != null) {
        					elem.addContent(nonceOpElem);
        				}
        			}
                } else {
                	opstr = op.getName();
                }
                if (opstr != null) {
                	elem.addAttribute("automate", opstr);
                }                
                // add element
                turnouts.addContent(elem);

            }
        }
        return turnouts;
    }

    /**
     * Subclass provides implementation to create the correct top
     * element, including the type information.
     * Default implementation is to use the local class here.
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
    	List operationList = turnouts.getChildren("operations");
    	if (operationList.size()>1) {
    		log.warn("unexpected extra elements found in turnout operations list");
    	}
    	if (operationList.size()>0) {
    		TurnoutOperationManagerXml tomx = new TurnoutOperationManagerXml();
    		tomx.load((Element)operationList.get(0));
    	}
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
            if (log.isDebugEnabled()) log.debug("create turnout: ("+sysName+")("+(userName==null?"<null>":userName)+")");
            Turnout t = tm.newTurnout(sysName, userName);
            
            // now add feedback if needed
            Attribute a;
            a = ((Element)(turnoutList.get(i))).getAttribute("feedback");
            if (a!=null) {
            	t.setFeedbackMode(a.getValue());
            }
            a = ((Element)(turnoutList.get(i))).getAttribute("sensor1");
            if (a!=null) { 
                Sensor s = InstanceManager.sensorManagerInstance().provideSensor(a.getValue());
                t.provideFirstFeedbackSensor(s);
            }
            a = ((Element)(turnoutList.get(i))).getAttribute("sensor2");
            if (a!=null) { 
                Sensor s = InstanceManager.sensorManagerInstance().provideSensor(a.getValue());
                t.provideSecondFeedbackSensor(s);
            }
            // operation stuff
            List myOpList = ((Element)turnoutList.get(i)).getChildren();
            if (myOpList.size()>0) {
            	if (myOpList.size()>1) {
            		log.warn("unexpected extra elements found in turnout-specific operations");
            	}
            	TurnoutOperation toper = TurnoutOperationXml.loadOperation((Element)myOpList.get(0));
        		t.setTurnoutOperation(toper);
            } else {
            	a = ((Element)(turnoutList.get(i))).getAttribute("automate");
            	if (a!=null) {
            		String str = a.getValue();
            		if (str.equals("Off")) {
            			t.setInhibitOperation(true);
            		} else if (!str.equals("Default")) {
            			TurnoutOperation toper =
            				TurnoutOperationManager.getInstance().getOperation(str);
            			t.setTurnoutOperation(toper);
            		}
            	}
            }
			
			//  set initial state from sensor feedback if appropriate
			t.setInitialKnownStateFromFeedback();
        }
       
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AbstractTurnoutManagerConfigXML.class.getName());
}