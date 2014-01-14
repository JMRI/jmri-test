// JsonConsistServer.java
package jmri.jmris.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import jmri.ConsistListListener;
import jmri.ConsistListener;
import jmri.ConsistManager;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.jmris.JmriConnection;
import static jmri.jmris.json.JSON.ADDRESS;
import static jmri.jmris.json.JSON.DATA;
import static jmri.jmris.json.JSON.DELETE;
import static jmri.jmris.json.JSON.IS_LONG_ADDRESS;
import static jmri.jmris.json.JSON.METHOD;
import static jmri.jmris.json.JSON.PUT;
import static jmri.jmris.json.JSON.STATUS;
import jmri.jmrit.consisttool.ConsistFile;
import org.jdom.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author rhwood
 */
public class JsonConsistServer {

    private final JmriConnection connection;
    private final ObjectMapper mapper;
    private final static Logger log = LoggerFactory.getLogger(JsonConsistServer.class);
    private final JsonConsistListListener consistListListener = new JsonConsistListListener();
    private final ArrayList<JsonConsistListener> consistListeners = new ArrayList<JsonConsistListener>();
    private final PropertyChangeListener instanceManagerListener = new PropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(InstanceManager.CONSIST_MANAGER)) {
                if (evt.getNewValue() != null) {
                    InstanceManager.consistManagerInstance().requestUpdateFromLayout();
                    try {
                        (new ConsistFile()).readFile();
                    } catch (IOException e) {
                        log.warn("error reading consist file: {}", e.getLocalizedMessage());
                    } catch (JDOMException e) {
                        log.warn("error reading consist file: {}", e.getLocalizedMessage());
                    }
                    InstanceManager.consistManagerInstance().addConsistListListener(consistListListener);
                    for (JsonConsistListener l : consistListeners) {
                        InstanceManager.consistManagerInstance().getConsist(l.consistAddress).addConsistListener(l);
                    }
                }
                if (evt.getOldValue() != null) {
                    for (JsonConsistListener l : consistListeners) {
                        ((ConsistManager) evt.getOldValue()).getConsist(l.consistAddress).removeConsistListener(l);
                    }
                }
            }
        }
    };

    public JsonConsistServer(JmriConnection connection) {
        this.connection = connection;
        this.mapper = new ObjectMapper();
        if (InstanceManager.consistManagerInstance() != null) {
            InstanceManager.consistManagerInstance().requestUpdateFromLayout();
            try {
                (new ConsistFile()).readFile();
            } catch (IOException e) {
                log.warn("error reading consist file: {}", e.getLocalizedMessage());
            } catch (JDOMException e) {
                log.warn("error reading consist file: {}", e.getLocalizedMessage());
            }
            InstanceManager.consistManagerInstance().addConsistListListener(this.consistListListener);
        }
    }

    public void onClose() {
        InstanceManager.removePropertyChangeListener(this.instanceManagerListener);
        if (InstanceManager.consistManagerInstance() != null) {
            InstanceManager.consistManagerInstance().removeConsistListListener(this.consistListListener);
            for (JsonConsistListener l : new ArrayList<JsonConsistListener>(this.consistListeners)) {
                l.onClose();
                this.consistListeners.remove(l);
            }
        }
    }

    public void parseRequest(JsonNode data) throws IOException, JsonException {
        DccLocoAddress address;
        if (data.path(ADDRESS).canConvertToInt()) {
            address = new DccLocoAddress(data.path(ADDRESS).asInt(), data.path(IS_LONG_ADDRESS).asBoolean(false));
        } else {
            address = JsonUtil.addressForString(data.path(ADDRESS).asText());
        }
        if (data.path(METHOD).asText().equals(PUT)) {
            JsonUtil.putConsist(address, data);
        } else if (data.path(METHOD).asText().equals(DELETE)) {
            JsonUtil.delConsist(address);
        } else {
            JsonUtil.setConsist(address, data);
        }
        this.connection.sendMessage(this.mapper.writeValueAsString(JsonUtil.getConsist(address)));
        InstanceManager.consistManagerInstance().getConsist(address).addConsistListener(new JsonConsistListener(address));
    }

    private class JsonConsistListListener implements ConsistListListener {

        @Override
        public void notifyConsistListChanged() {
            JsonNode message;
            try {
                message = JsonUtil.getConsists();
            } catch (JsonException ex) {
                message = ex.getJsonMessage();
            }
            try {
                connection.sendMessage(mapper.writeValueAsString(message));
            } catch (IOException ex) {
                InstanceManager.consistManagerInstance().removeConsistListListener(this);
            }
        }
    }

    private class JsonConsistListener implements ConsistListener {

        private final DccLocoAddress consistAddress;

        @SuppressWarnings("LeakingThisInConstructor")
        private JsonConsistListener(DccLocoAddress address) {
            this.consistAddress = address;
            consistListeners.add(this);
        }

        @Override
        public void consistReply(DccLocoAddress locoAddress, int status) {
            JsonNode message;
            try {
                message = JsonUtil.getConsist(this.consistAddress);
                if (!locoAddress.equals(this.consistAddress)) {
                    ObjectNode data = (ObjectNode) message.get(DATA);
                    ObjectNode op = data.putObject(STATUS);
                    op.put(ADDRESS, locoAddress.getNumber());
                    op.put(IS_LONG_ADDRESS, locoAddress.isLongAddress());
                    op.put(STATUS, status);
                }
            } catch (JsonException ex) {
                message = ex.getJsonMessage();
            }
            try {
                connection.sendMessage(mapper.writeValueAsString(message));
            } catch (IOException ex) {
                InstanceManager.consistManagerInstance().getConsist(this.consistAddress).removeConsistListener(this);
            }
        }

        private void onClose() {
            if (InstanceManager.consistManagerInstance() != null) {
                InstanceManager.consistManagerInstance().getConsist(this.consistAddress).removeConsistListener(this);
            }
        }
    }
}
