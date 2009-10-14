// NamedBeanHandle.java

package jmri.util;

import java.util.Locale;

/**
 * Utility class for managing access to a NamedBean
 *
 * @author Bob Jacobsen  Copyright 2009
 * @version $Revision: 1.1 $
 */

public class NamedBeanHandle<T> {

    public NamedBeanHandle(String name, T bean) {
        this.name = name;
        this.bean = bean;
    }

    public String getName() { return name; }
    public T getBean() { return bean; }
    
    String name;
    T bean;
}