// QualifierAdder.java

package jmri.jmrit.symbolicprog;

import java.util.*;
import org.jdom.*;
import javax.swing.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base for adding qualifiers to objects
 *
 * @see             jmri.jmrit.symbolicprog.Qualifier
 * @see             jmri.jmrit.symbolicprog.ArithmeticQualifier
 * @see             jmri.jmrit.symbolicprog.tabbedframe.PaneProgFrame
 *
 * @author			Bob Jacobsen   Copyright (C) 2014
 * @version			$Revision$
 *
 */
public abstract class QualifierAdder {
    
    /**
     * Invoked to create the qualifier object
     * and connect as needed. If extra state
     * is needed, provide it via the subclass constructor.
     *
     * @param var      The variable that qualifies this, e.g. the one that's watched
     * @param relation The relation term from the qualifier definition, e.g. greater than
     * @param value    The value for the comparison
     */
    // e.g. return new PaneQualifier(pane, var, Integer.parseInt(value), relation, tabPane, index);
    abstract protected Qualifier createQualifier(VariableValue var, String relation, String value);
    // e.g. arrange for this to be sent a property change event on change of the qualified object
    abstract protected void addListener(java.beans.PropertyChangeListener qc);
    
    public void processModifierElements(Element e, VariableTableModel model) {
        // currently only looks for one instance and one type
        @SuppressWarnings("unchecked")
        List<Element> le = e.getChildren("qualifier");
        ArrayList<Qualifier> lq = new ArrayList<Qualifier>();
        for (Element q : le) {

            String variableRef = q.getChild("variableref").getText();
            String relation = q.getChild("relation").getText();
            String value = q.getChild("value").getText();
    
            // find the variable
            VariableValue var = model.findVar(variableRef);
            
            if (var != null) {
                // found, attach the qualifier object through creating it
                log.debug("Attached {} variable", variableRef);
                
                Qualifier qual = createQualifier(var, relation, value);
                
                qual.update(); 
                lq.add(qual);   
            } else {
                log.error("didn't find {} variable", variableRef, new Exception());
            }
        }
        // Now add the AND logic - listen for change and ensure result correct
        if (lq.size()>1) {
            QualifierCombiner qc = new QualifierCombiner(lq);
            addListener(qc);
        }
    }
      
    static Logger log = LoggerFactory.getLogger(QualifierAdder.class.getName());
  
}
