package jmri.configurexml;

import jmri.InstanceManager;
import jmri.TurnoutManager;
import jmri.Turnout;
import jmri.Sensor;
import jmri.TurnoutOperation;
import jmri.TurnoutOperationManager;
import jmri.configurexml.turnoutoperations.TurnoutOperationXml;

import java.util.List;
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
 * @version $Revision: 1.23 $
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
            java.util.Iterator iter =
                                    tm.getSystemNameList().iterator();

            // don't return an element if there are not turnouts to include
            if (!iter.hasNext()) return null;
            
            // store the turnouts
            while (iter.hasNext()) {
                String sname = (String)iter.next();
                if (sname==null) log.error("System name null during store");
                log.debug("system name is "+sname);
                Turnout t = tm.getBySystemName(sname);
                String uname = t.getUserName();
                Element elem = new Element("turnout")
                            .setAttribute("systemName", sname);
                if (uname!=null) elem.setAttribute("userName", uname);
                log.debug("store turnout "+sname+":"+uname);
                
                // include feedback info
                elem.setAttribute("feedback", t.getFeedbackModeName());
                Sensor s;
                s = t.getFirstSensor();
                if (s!=null) elem.setAttribute("sensor1", s.getSystemName());
                s = t.getSecondSensor();
                if (s!=null) elem.setAttribute("sensor2", s.getSystemName());
                
                // include turnout inverted
                elem.setAttribute("inverted", t.getInverted()?"true":"false");
                
                if (t.canLock(Turnout.CABLOCKOUT | Turnout.PUSHBUTTONLOCKOUT)){
                    // include turnout locked
                    elem.setAttribute("locked", t.getLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT)?"true":"false");          
                 	// include turnout lock mode
                	String lockOpr;
                	if (t.canLock(Turnout.CABLOCKOUT) && t.canLock(Turnout.PUSHBUTTONLOCKOUT)){
                		lockOpr = "both"; 
                	} else if (t.canLock(Turnout.CABLOCKOUT)){
                		lockOpr = "cab";
                	} else if (t.canLock(Turnout.PUSHBUTTONLOCKOUT)){
                		lockOpr = "pushbutton";
                	} else {
                		lockOpr = "none";
                	}
                	elem.setAttribute("lockMode", lockOpr);          
                	// include turnout decoder
                	elem.setAttribute("decoder", t.getDecoderName());
                }         
                
				// include number of control bits, if different from one
				int iNum = t.getNumberOutputBits();
				if (iNum!=1) elem.setAttribute("numBits",""+iNum);
				
				// include turnout control type, if different from 0
				int iType = t.getControlType();
				if (iType!=0) elem.setAttribute("controlType",""+iType);

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
                	elem.setAttribute("automate", opstr);
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
            
            // check for turnout inverted
            a = ((Element)(turnoutList.get(i))).getAttribute("inverted");
            if (a!=null) { 
            	t.setInverted(a.getValue().equals("true"));
            }
             
            // check for turnout decoder
            a = ((Element)(turnoutList.get(i))).getAttribute("decoder");
            if (a!=null) { 
            	t.setDecoderName(a.getValue());
            }
            
            // check for turnout lock mode
			a = ((Element) (turnoutList.get(i))).getAttribute("lockMode");
			if (a != null) {
				if (a.getValue().equals("both"))
					t.enableLockOperation(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, true);
				if (a.getValue().equals("cab")) {
					t.enableLockOperation(Turnout.CABLOCKOUT, true);
					t.enableLockOperation(Turnout.PUSHBUTTONLOCKOUT, false);
				}
				if (a.getValue().equals("pushbutton")) {
					t.enableLockOperation(Turnout.PUSHBUTTONLOCKOUT, true);
					t.enableLockOperation(Turnout.CABLOCKOUT, false);
				}
			}
            
            // check for turnout locked
            a = ((Element)(turnoutList.get(i))).getAttribute("locked");
            if (a!=null) { 
            	t.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, a.getValue().equals("true"));
            }
 			
			// number of bits, if present - if not, defaults to 1
			a = ((Element)(turnoutList.get(i))).getAttribute("numBits");
			if (a==null) {
				t.setNumberOutputBits(1);
			}
			else {
				int iNum = Integer.parseInt(a.getValue());
				if ( (iNum==1) || (iNum==2) ) {
					t.setNumberOutputBits(iNum);
				}
				else {
					log.warn("illegal number of output bits for control of turnout "+sysName);
					t.setNumberOutputBits(1);
				}
			}
			
			// control type, if present - if not, defaults to 0
			a = ((Element)(turnoutList.get(i))).getAttribute("controlType");
			if (a==null) {
				t.setControlType(0);
			}
			else {
				int iType = Integer.parseInt(a.getValue());
				if (iType>=0) {
					t.setControlType(iType);
				}
				else {
					log.warn("illegal control type for control of turnout "+sysName);
					t.setControlType(0);
				}
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