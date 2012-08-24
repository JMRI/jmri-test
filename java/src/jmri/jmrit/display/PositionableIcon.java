// PositionableIcon.java

package jmri.jmrit.display;
/**
 * Gather common methods for Turnouts, Semsors, SignalHeads, Masts, etc.
 *
 * @author PeteCressman Copyright (C) 2011
 * @version $Revision$
 */
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;

import jmri.jmrit.catalog.NamedIcon;

public class PositionableIcon extends PositionableLabel {

    protected Hashtable <String, NamedIcon> _iconMap;
    protected String  _iconFamily;
    protected double _scale = 1.0;			// getScale, come from net result found in one of the icons
    protected int _rotate = 0;

    public PositionableIcon(Editor editor) {
        // super ctor call to make sure this is an icon label
        super(new NamedIcon("resources/icons/misc/X-red.gif","resources/icons/misc/X-red.gif"), editor);
        _control = true;
        setPopupUtility(null);
    }
    
    public PositionableIcon(NamedIcon s, Editor editor) {
        // super ctor call to make sure this is an icon label
        super(s, editor);
        _control = true;
        setPopupUtility(null);
    }
    
    public PositionableIcon(String s, Editor editor) {
        // super ctor call to make sure this is an icon label
        super(s, editor);
        _control = true;
        setPopupUtility(null);
    }

    /**
    * Get icon by its bean state name key found in jmri.NamedBeanBundle.properties
    * Get icon by its localized bean state name
    */
    public NamedIcon getIcon(String state) {
        return _iconMap.get(state);
    }

    public String getFamily() {
        return _iconFamily;
    }
    public void setFamily(String family) {
        _iconFamily = family;
    }

    public Enumeration<String> getIconStateNames() {
        return _iconMap.keys(); 
    }

    public int maxHeight() {
        int max = super.maxHeight();
        if (_iconMap!=null) {
            Iterator<NamedIcon> iter = _iconMap.values().iterator();
            while (iter.hasNext()) {
                max = Math.max(iter.next().getIconHeight(), max);
            }        	
        }
        return max;
    }
    public int maxWidth() {
        int max = super.maxWidth();
        if (_iconMap!=null) {
            Iterator<NamedIcon> iter = _iconMap.values().iterator();
            while (iter.hasNext()) {
                max = Math.max(iter.next().getIconWidth(), max);
            }        	
        }
        return max;
    }

    /******** popup AbstractAction method overrides *********/

    protected void rotateOrthogonal() {
        Iterator<Entry<String, NamedIcon>> it = _iconMap.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, NamedIcon> entry = it.next();
            entry.getValue().setRotation(entry.getValue().getRotation()+1, this);
        }
        updateSize();
    }

    public void setScale(double s) {
        _scale = s;
        if (_iconMap==null) {
            return;
        }
        Iterator<Entry<String, NamedIcon>> it = _iconMap.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, NamedIcon> entry = it.next();
            entry.getValue().scale(s, this);
        }
        updateSize();
    }

    public void rotate(int deg) {
    	setDegrees(deg);
    	if (_text && !_icon) {
        	super.rotate(deg);
        	return;
    	}
        if (_iconMap==null) {
            return;
        }
        Iterator<Entry<String, NamedIcon>> it = _iconMap.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, NamedIcon> entry = it.next();
            entry.getValue().rotate(deg, this);
        }        	
        updateSize();
    }

    protected Hashtable<String, NamedIcon> cloneMap(Hashtable<String, NamedIcon> map,
                                                             PositionableLabel pos) {
        Hashtable<String, NamedIcon> clone = new Hashtable<String, NamedIcon>();
        if (map!=null) {
            Iterator<Entry<String, NamedIcon>> it = map.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, NamedIcon> entry = it.next();
                clone.put(entry.getKey(), cloneIcon(entry.getValue(), pos));
            }
        }
        return clone;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PositionableIcon.class.getName());
}
