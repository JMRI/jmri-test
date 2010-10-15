package jmri.jmrit.display.layoutEditor;
/**
 * An icon to display a status of a Memory.<P>
 */
import jmri.jmrit.catalog.NamedIcon;

// This is the same name as display.MemoryIcon, but a very
// separate class. That's not good. Unfortunately, it's too 
// hard to disentangle that now because it's resident in the
// panel file that have been written out, so we just annote 
// the fact.
@edu.umd.cs.findbugs.annotations.SuppressWarnings(value="NM_SAME_SIMPLE_NAME_AS_SUPERCLASS")
public class MemoryIcon extends jmri.jmrit.display.MemoryIcon {

    String defaultText = " ";

    public MemoryIcon(String s, LayoutEditor panel) {
        super(s, panel);
        log.debug("MemoryIcon ctor= "+MemoryIcon.class.getName());
    }

    public void setText(String text) {
        if (text==null || text.length()==0) {
            super.setText(defaultText); 
        } else {
            super.setText(text); 
        }
    }

    public void displayState() {
        log.debug("displayState");
    	if (memory == null) {  // use default if not connected yet
            setText(defaultText);
    		updateSize();
    		return;
    	}
		Object key = memory.getValue();
		if (key != null) {
            java.util.HashMap<String, NamedIcon> map = getMap();
		    if (map == null) {
		        // no map, attempt to show object directly
                Object val = key;
                if (val instanceof String) {
                    if (val.equals(""))
                        setText(defaultText);
                    else
                        setText((String) val);
                    setIcon(null);
                    _text = true;
                    _icon = false;
    		        updateSize();
                    return;
                } else if (val instanceof javax.swing.ImageIcon) {
                    setIcon((javax.swing.ImageIcon) val);
                    setText(null);
                    _text = false;
                    _icon = true;
    		        updateSize();
                    return;
                } else if (val instanceof Number) {
                    setText(val.toString());
                    setIcon(null);
                    _text = true;
                    _icon = false;
    		        updateSize();
                    return;
                } else log.warn("can't display current value of "+memory.getSystemName()+
                                ", val= "+val+" of Class "+val.getClass().getName());
		    } else {
		        // map exists, use it
			    NamedIcon newicon = map.get(key.toString());
			    if (newicon!=null) {
                    
                    setText(null);
				    super.setIcon(newicon);
                    _text = false;
                    _icon = true;
    		        updateSize();
				    return;
			    } else {
			        // no match, use default
		            setIcon(getDefaultIcon());
                    
                    setText(null);
                    _text = false;
                    _icon = true;
    		        updateSize();
			    }
		    }
		} else {
            setIcon(null);
            setText(defaultText);
            _text = true;
            _icon = false;
            updateSize();
        }
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MemoryIcon.class.getName());
}

