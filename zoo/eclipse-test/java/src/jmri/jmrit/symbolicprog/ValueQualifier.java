// ValueQualifier.java

package jmri.jmrit.symbolicprog;

/**
 * Qualify a variable on greater than or equal a number
 *
 * @author			Bob Jacobsen   Copyright (C) 2010
 * @version			$Revision$
 *
 */
public class ValueQualifier extends AbstractQualifier {

    public enum Test {
        GE("ge"), // greater than or equal
        LE("le"),
        GT("gt"), 
        LT("lt"),
        EQ("eq"),
        NE("ne");
        
        Test(String relation) {
            this.relation = relation;
        }
        String relation;
        
        static Test decode(String r) {
            for (Test t : Test.values()) {
                if (t.relation.equals(r)) return t;
            }
            return null;
        }
    }
    
    Test test;
    
    public ValueQualifier(VariableValue qualifiedVal, VariableValue watchedVal, int value, String relation) {
        super(qualifiedVal, watchedVal);

        this.test = Test.decode(relation);
        this.value = value;
        
        setWatchedAvailable(availableStateFromObject(watchedVal.getValueObject()));

    }

    protected boolean availableStateFromObject(Object o) {
        int now = ((Integer) o ).intValue();
        return availableStateFromValue(now);
    }
    
    protected boolean availableStateFromValue(int now) {
        switch (test) {
            case GE: 
                return now >= value;
            case LE: 
                return now <= value;
            case GT: 
                return now > value;
            case LT: 
                return now < value;
            case EQ: 
                return now == value;
            case NE: 
                return now != value;
        }
        return false;       // shouldn't happen?
    }

    int value;
    
}
