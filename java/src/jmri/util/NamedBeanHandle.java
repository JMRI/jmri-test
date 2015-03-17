// NamedBeanHandle.java
package jmri.util;

/**
 * Utility class for managing access to a NamedBean
 *
 * @author Bob Jacobsen Copyright 2009
 * @version $Revision$
 */
public class NamedBeanHandle<T> implements java.io.Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 7220729204802973081L;

    public NamedBeanHandle(String name, T bean) {
        this.name = name;
        this.bean = bean;
    }

    public String getName() {
        return name;
    }

    public T getBean() {
        return bean;
    }

    String name;
    T bean;
}
