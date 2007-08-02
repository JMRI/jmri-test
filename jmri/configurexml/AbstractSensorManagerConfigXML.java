package jmri.configurexml;

import jmri.InstanceManager;
import jmri.SensorManager;
import jmri.Sensor;

import java.util.List;
import org.jdom.Element;

/**
 * Provides the abstract base and store functionality for
 * configuring SensorManagers, working with
 * AbstractSensorManagers.
 * <P>
 * Typically, a subclass will just implement the load(Element sensors)
 * class, relying on implementation here to load the individual sensors.
 * Note that these are stored explicitly, so the
 * resolution mechanism doesn't need to see *Xml classes for each
 * specific Sensor or AbstractSensor subclass at store time.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @version $Revision: 1.4 $
 */
public abstract class AbstractSensorManagerConfigXML implements XmlAdapter {

    public AbstractSensorManagerConfigXML() {
    }

    /**
     * Default implementation for storing the contents of a
     * SensorManager
     * @param o Object to store, of type SensorManager
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        Element sensors = new Element("sensors");
        setStoreElementClass(sensors);
        SensorManager tm = (SensorManager) o;
        if (tm!=null) {
            java.util.Iterator iter =
                                    tm.getSystemNameList().iterator();

            while (iter.hasNext()) {
                String sname = (String)iter.next();
                if (sname==null) log.error("System name null during store");
                log.debug("system name is "+sname);
                String uname = tm.getBySystemName(sname).getUserName();
                String inverted = tm.getBySystemName(sname).getInverted() ? "true" : "false";
                Element elem = new Element("sensor")
                            .setAttribute("systemName", sname)
                            .setAttribute("inverted", inverted);
                if (uname!=null) elem.setAttribute("userName", uname);
                log.debug("store sensor "+sname+":"+uname);
                sensors.addContent(elem);

            }
        }
        return sensors;
    }

    /**
     * Subclass provides implementation to create the correct top
     * element, including the type information.
     * Default implementation is to use the local class here.
     * @param sensors The top-level element being created
     */
    abstract public void setStoreElementClass(Element sensors);

    /**
     * Create a SensorManager object of the correct class, then
     * register and fill it.
     * @param sensors Top level Element to unpack.
     */
    abstract public void load(Element sensors);

    /**
     * Utility method to load the individual Sensor objects.
     * If there's no additional info needed for a specific sensor type,
     * invoke this with the parent of the set of Sensor elements.
     * @param sensors Element containing the Sensor elements to load.
     */
    public void loadSensors(Element sensors) {
        List sensorList = sensors.getChildren("sensor");
        if (log.isDebugEnabled()) log.debug("Found "+sensorList.size()+" sensors");
        SensorManager tm = InstanceManager.sensorManagerInstance();

        for (int i=0; i<sensorList.size(); i++) {
            if ( ((Element)(sensorList.get(i))).getAttribute("systemName") == null) {
                log.warn("unexpected null in systemName "+((Element)(sensorList.get(i)))+" "+((Element)(sensorList.get(i))).getAttributes());
                break;
            }
            String sysName = ((Element)(sensorList.get(i))).getAttribute("systemName").getValue();
            String userName = null;
            boolean inverted = false;
            
            if ( ((Element)(sensorList.get(i))).getAttribute("userName") != null)
                userName = ((Element)(sensorList.get(i))).getAttribute("userName").getValue();

            if ( ((Element)(sensorList.get(i))).getAttribute("inverted") != null)
                if (((Element)(sensorList.get(i))).getAttribute("inverted").getValue().equals("true"))
                    inverted = true;

            if (log.isDebugEnabled()) log.debug("create sensor: ("+sysName+")("+(userName==null?"<null>":userName)+")");
            Sensor s = tm.newSensor(sysName, userName);
            s.setInverted(inverted);
        }
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AbstractSensorManagerConfigXML.class.getName());
}