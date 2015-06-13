package jmri;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represent a Named Bean (e.g.&nbsp;Turnout) and specific setting for it. These
 * can be used e.g. to represent part of a particular path through a layout, or
 * a condition that has to be true as part of something.
 * <p>
 * Objects of this class are immutable, in that once created the selected bean
 * and required setting cannot be changed. However, the value of the
 * <code><a href="#check()">check</a></code> method does change, because it's a
 * function of the current bean setting(s).
 *
 * @author	Bob Jacobsen Copyright (C) 2006, 2008, 2010
 * @version	$Revision$
 */
@net.jcip.annotations.Immutable
public class BeanSetting {

    public BeanSetting(jmri.NamedBean t, String pName, int setting) {
        _setting = setting;
        if (t == null) {
            _namedBean = null;
            return;
        }
        _namedBean = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(pName, t);
    }

    public BeanSetting(jmri.NamedBean t, int setting) {
        _setting = setting;
        if (t == null) {
            _namedBean = null;
            return;
        }
        _namedBean = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(t.getDisplayName(), t);
    }

    /**
     * Convenience method; check if the Bean currently has the desired setting
     */
    public boolean check() {
        if (_namedBean == null) {
            return false;
        }
        return _namedBean.getBean().getState() == _setting;
    }

    public NamedBean getBean() {
        if (_namedBean == null) {
            return null;
        }
        return _namedBean.getBean();
    }

    public String getBeanName() {
        if (_namedBean == null) {
            return "";
        }
        return _namedBean.getName();
    }

    public int getSetting() {
        return _setting;
    }

    private final NamedBeanHandle<NamedBean> _namedBean;
    final private int _setting;

    static final Logger log = LoggerFactory.getLogger(BeanSetting.class.getName());
}
