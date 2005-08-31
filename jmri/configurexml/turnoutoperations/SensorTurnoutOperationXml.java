/**
 * 
 */
package jmri.configurexml.turnoutoperations;

import org.jdom.Element;

import java.lang.reflect.Constructor;

import jmri.NoFeedbackTurnoutOperation;
import jmri.SensorTurnoutOperation;
import jmri.configurexml.turnoutoperations.TurnoutOperationXml;

/**
 * Concrete subclass to save/restore SensorTurnoutOperation object
 * to/from XML. Most of the work is done by CommonTurnoutOperationXml
 * @author John Harper	Copyright 2005
 *
 */
public class SensorTurnoutOperationXml extends CommonTurnoutOperationXml {

	/**
	 * called for a newly-constructed object to load it from an XML element
	 * @param the XML element of type "turnoutOperation"
	 */
	public void load(Element e) {
		try {
			Class myOpClass = Class.forName("jmri.SensorTurnoutOperation");
			super.load(e, myOpClass.getConstructor(new Class[]{String.class, int.class, int.class}),
					NoFeedbackTurnoutOperation.getDefaultIntervalStatic(),
					NoFeedbackTurnoutOperation.getDefaultMaxTriesStatic());
		} catch (Exception except) {
			log.warn("couldn't find constructor for class "+getClass().getName()+" to load XML");
		}
	}
	
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SensorTurnoutOperationXml.class.getName());
}
